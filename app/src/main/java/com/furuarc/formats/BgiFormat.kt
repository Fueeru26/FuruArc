package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Buriko General Interpreter / Ethornell (.arc).
 * Read: "PackFile    " (entry 0x20) and "BURIKO ARC20" (entry 0x80).
 * Write: "BURIKO ARC20".
 * Note: the file contents inside (DSC / CompressedBG / encrypted script) are output as is, not decoded.
 */
object BgiFormat : ArchiveFormat {
    override val id = "bgi"
    override val displayName = "ARC (BGI/Ethornell)"
    override val extensions = listOf("arc")

    private val M_PACK = "PackFile    ".toByteArray(Charsets.US_ASCII)
    private val M_ARC20 = "BURIKO ARC20".toByteArray(Charsets.US_ASCII)

    private fun starts(head: ByteArray, m: ByteArray) =
        head.size >= m.size && m.indices.all { head[it] == m[it] }

    override fun matches(head: ByteArray) = starts(head, M_PACK) || starts(head, M_ARC20)

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val head = src.readFully(0, 16)
        val arc20 = starts(head, M_ARC20)
        val entrySize = if (arc20) 0x80 else 0x20
        val nameLen = if (arc20) 0x60 else 0x10
        val count = head.le().getInt(12)
        if (count <= 0 || count > 500_000 || 16L + count.toLong() * entrySize > src.length)
            throw IOException("Index BGI tidak valid")
        val idx = src.readFully(16, count * entrySize)
        val base = 16L + count.toLong() * entrySize
        val cs = IoUtil.cs(opts)
        val bb = idx.le()
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val o = i * entrySize
            val name = IoUtil.cstr(idx, o, nameLen, cs)
            val off = (bb.getInt(o + nameLen).toLong() and 0xffffffffL) + base
            val size = bb.getInt(o + nameLen + 4).toLong() and 0xffffffffL
            if (off + size > src.length) throw IOException("Entri BGI di luar arsip: $name")
            out += Entry(name, off, size)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        if (opts.bool("bgi_dsc", false) && entry.size in 0x220..(256L * 1024 * 1024)) {
            val raw = src.readFully(entry.offset, entry.size.toInt())
            if (BgiDsc.isDsc(raw)) {
                out.write(try { BgiDsc.decode(raw) } catch (e: IOException) { raw })
                return
            }
            out.write(raw)
            return
        }
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "bgi_ver", kind = OptKind.CHOICE,
            en = "Archive type", id = "Tipe arsip",
            choices = listOf(
                Choice("v2", "BURIKO ARC20 (v2, most games)", "BURIKO ARC20 (v2, kebanyakan game)"),
                Choice("v1", "PackFile (v1, old games)", "PackFile (v1, game lama)"),
            ),
            default = "v2",
            hintEn = "Choose v2 if you are not sure. Names: v2 max 95 bytes, v1 max 15 bytes.",
            hintId = "Pilih v2 jika tidak mengetahuinya. Nama: v2 maks 95 byte, v1 maks 15 byte.",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(
        OptionSpec(
            key = "bgi_dsc", kind = OptKind.BOOL, en = "Decode DSC files (script/data)", id = "Dekode file DSC (skrip/data)", default = "0",
            hintEn = "Files starting with 'DSC FORMAT 1.00' are decompressed. Leave off to keep originals (needed for repacking).",
            hintId = "File berawalan 'DSC FORMAT 1.00' didekompres. Matikan untuk menyimpan aslinya (perlu bila ingin dikemas ulang).",
        ),
        ENCODING_SPEC,
    )

    private fun packV1(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= 0x10) throw IOException("Nama terlalu panjang untuk v1 (maks 15 byte): ${it.path}")
            b
        }
        var total = 0L
        val idx = ByteBuffer.allocate(n * 0x20).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until n) {
            idx.position(i * 0x20)
            idx.put(names[i])
            idx.position(i * 0x20 + 0x10)
            idx.putInt(total.toInt()).putInt(inputs[i].size.toInt())
            total += inputs[i].size
        }
        if (total > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(M_PACK)
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n).array())
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
        if (opts.str("bgi_ver", "v2") == "v1") { packV1(inputs, target, opts, progress); return }
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= 0x60) throw IOException("Nama terlalu panjang (maks 95 byte): ${it.path}")
            b
        }
        val base = 16L + n.toLong() * 0x80
        var total = 0L
        val idx = ByteBuffer.allocate(n * 0x80).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until n) {
            idx.position(i * 0x80)
            idx.put(names[i])
            idx.position(i * 0x80 + 0x60)
            idx.putInt(total.toInt()).putInt(inputs[i].size.toInt())
            total += inputs[i].size
        }
        if (total > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(M_ARC20)
            raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(n).array())
            raf.write(idx.array())
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                IoUtil.appendInput(raf, f, buf)
            }
            progress.update(n, n, "selesai")
        }
    }
}
