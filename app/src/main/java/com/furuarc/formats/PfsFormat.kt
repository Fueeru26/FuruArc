package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.MessageDigest

/**
 * Artemis PFS (pf2 / pf6 / pf8).
 * Read: pf6 (plain), pf8 (XOR with SHA-1 of the index), pf2 (plain, Shift-JIS; different index layout).
 * Write: pf6/pf8 follow the msg-tool writer (index = count + entries, no trailer table).
 */
object PfsFormat : ArchiveFormat {
    override val id = "pfs"
    override val displayName = "PFS (Artemis)"
    override val extensions = listOf("pfs", "000", "001", "002")

    override fun matches(head: ByteArray) =
        head.size >= 11 && head[0] == 'p'.code.toByte() && head[1] == 'f'.code.toByte() &&
            (head[2] == '2'.code.toByte() || head[2] == '6'.code.toByte() || head[2] == '8'.code.toByte())

    private fun charsetFor(ver: Char): Charset =
        if (ver == '8') Charsets.UTF_8
        else try { Charset.forName("windows-31j") } catch (e: Exception) { Charsets.UTF_8 }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val h = src.readFully(0, 11).le()
        val ver = h.get(2).toInt().toChar()
        val indexSize = h.getInt(3)
        if (indexSize <= 4 || 7L + indexSize > src.length) throw IOException("Index PFS tidak valid")
        val idx = src.readFully(7, indexSize)
        if (ver == '2') return listPf2(idx, IoUtil.cs(opts))
        val bb = idx.le()
        val count = bb.int
        val cs = IoUtil.cs(opts, charsetFor(ver))
        val key = if (ver == '8') MessageDigest.getInstance("SHA-1").digest(idx) else null

        val out = ArrayList<Entry>(count)
        repeat(count) {
            val nb = ByteArray(bb.int)
            bb.get(nb)
            bb.int // reserved (0)
            val off = bb.int.toLong() and 0xffffffffL
            val size = bb.int.toLong() and 0xffffffffL
            out += Entry(String(nb, cs).replace('\\', '/'), off, size, size, key)
        }
        return out
    }

    /** pf2: count at index+4, each entry: u32 name length, name, 0x0C reserved bytes, u32 offset, u32 size. */
    private fun listPf2(idx: ByteArray, cs: Charset): List<Entry> {
        val bb = idx.le()
        val count = bb.getInt(4)
        if (count < 0 || count > 500_000) throw IOException("Jumlah entri pf2 tidak valid")
        var p = 8
        val out = ArrayList<Entry>(count)
        repeat(count) {
            if (p + 4 > idx.size) throw IOException("Index pf2 terpotong")
            val nl = bb.getInt(p)
            if (nl < 0 || p.toLong() + nl + 0x10 + 8 > idx.size) throw IOException("Index pf2 terpotong")
            val name = String(idx, p + 4, nl, cs).replace('\\', '/')
            p += nl + 0x10
            val off = bb.getInt(p).toLong() and 0xffffffffL
            val size = bb.getInt(p + 4).toLong() and 0xffffffffL
            p += 8
            out += Entry(name, off, size)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out, entry.meta as? ByteArray)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "pfs_ver", kind = OptKind.CHOICE,
            en = "Archive version", id = "Versi arsip",
            choices = listOf(
                Choice("auto", "Auto (pf8, most common)", "Auto (pf8, paling umum)"),
                Choice("pf8", "pf8 (encrypted, newer Artemis)", "pf8 (terenkripsi, Artemis baru)"),
                Choice("pf6", "pf6 (plain)", "pf6 (polos)"),
                Choice("pf2", "pf2 (old, plain)", "pf2 (lama, polos)"),
            ),
            default = "auto",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(ENCODING_SPEC)

    private fun packPf2(inputs: List<PackInput>, target: File, cs: Charset, progress: Progress) {
        val n = inputs.size
        val names = inputs.map { it.path.replace('/', '\\').toByteArray(cs) }
        val indexSize = 8 + names.sumOf { 4 + it.size + 12 + 8 }
        val dataStart = 7L + indexSize
        val idx = ByteBuffer.allocate(indexSize).order(ByteOrder.LITTLE_ENDIAN)
        idx.putInt(0).putInt(n)
        var pos = dataStart
        for (i in 0 until n) {
            idx.putInt(names[i].size).put(names[i])
            idx.putInt(0x10).putInt(0).putInt(0)
            idx.putInt(pos.toInt()).putInt(inputs[i].size.toInt())
            pos += inputs[i].size
        }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(byteArrayOf('p'.code.toByte(), 'f'.code.toByte(), '2'.code.toByte()))
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(indexSize).array())
            raf.write(idx.array())
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                IoUtil.appendInput(raf, f, buf)
            }
            progress.update(n, n, "selesai")
        }
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val ver = when (opts.extra["pfs_ver"]) {
            "pf6" -> '6'
            "pf2" -> '2'
            "pf8", "auto" -> '8'
            else -> opts.pfsVersion
        }
        val cs = IoUtil.cs(opts, charsetFor(ver))
        if (ver == '2') { packPf2(inputs, target, cs, progress); return }
        val names = inputs.map { it.path.replace('/', '\\').toByteArray(cs) }
        val n = inputs.size

        val entriesSize = names.sumOf { 4 + it.size + 12 }
        val indexSize = 4 + entriesSize
        val dataStart = 7L + indexSize

        val idx = ByteBuffer.allocate(indexSize).order(ByteOrder.LITTLE_ENDIAN)
        idx.putInt(n)
        var pos = dataStart
        for (i in 0 until n) {
            idx.putInt(names[i].size).put(names[i]).putInt(0)
            idx.putInt(pos.toInt()).putInt(inputs[i].size.toInt())
            pos += inputs[i].size
        }
        val indexBytes = idx.array()
        val key = if (ver == '8') MessageDigest.getInstance("SHA-1").digest(indexBytes) else null

        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(byteArrayOf('p'.code.toByte(), 'f'.code.toByte(), ver.code.toByte()))
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(indexSize).array())
            raf.write(indexBytes)
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                var done = 0L
                f.open().use { ins ->
                    while (true) {
                        val r = ins.read(buf)
                        if (r < 0) break
                        if (key != null) IoUtil.xorKey(buf, r, done, key)
                        raf.write(buf, 0, r)
                        done += r
                    }
                }
            }
            progress.update(n, n, "selesai")
        }
    }
}
