package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * Circus (.dat) (port of msg-tool & GARbro). No magic, detected via structure.
 * Layout: u32 count; then (count) entries: name[N] (N = 0x24, 0x30, or 0x3C; Shift-JIS), offset u32.
 * All entries are real (the original file has no terminator); size = next offset difference, the last entry runs to the end of the file. A terminator entry (empty name/offset out of range) is ignored if present.
 */
object CircusDatFormat : ArchiveFormat {
    override val id = "circusdat"
    override val displayName = "DAT (Adv32 / Circus)"
    override val extensions = listOf("dat")

    override val unpackOptions = listOf(ENCODING_SPEC)
    override val packOptions = listOf(
        OptionSpec(
            key = "dat_nl", kind = OptKind.CHOICE, en = "Name field width", id = "Lebar kolom nama",
            choices = listOf(
                Choice("60", "60 bytes (VOICE.DAT of D.C. II)", "60 byte (VOICE.DAT D.C. II)"),
                Choice("48", "48 bytes", "48 byte"),
                Choice("36", "36 bytes", "36 byte"),
            ),
            default = "60",
            hintEn = "Choose 60 if you are not sure. Names use backslash folders and must fit width-1 bytes.",
            hintId = "Pilih 60 jika tidak mengetahuinya. Nama memakai folder dengan backslash dan harus muat (lebar-1) byte.",
        ),
        ENCODING_SPEC,
    )

    override fun matches(head: ByteArray) = false // no magic; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src, IoUtil.sjis()).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index DAT (Circus) tidak valid")
        return r
    }

    private fun u32(src: RandomSource, pos: Long): Long = src.readFully(pos, 4).le().int.toLong() and 0xffffffffL

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        if (len < 16) throw IOException("File terlalu kecil")
        val total = u32(src, 0)
        if (total <= 1 || total > 0xfffff) throw IOException("Jumlah entri tidak valid")
        var last: Exception? = null
        for (nl in intArrayOf(0x24, 0x30, 0x3C)) {
            try {
                return readIndex(src, total.toInt(), nl, cs, len)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index DAT tidak valid")
    }

    private fun readIndex(src: RandomSource, total: Int, nl: Int, cs: java.nio.charset.Charset, len: Long): List<Entry> {
        val indexSize = (nl + 4).toLong() * total
        if (4 + indexSize > len) throw IOException("Index di luar arsip")
        val count = total
        var next = u32(src, (4 + nl).toLong())
        if (next < 4 + indexSize) throw IOException("Offset pertama tidak valid")
        val firstSize = u32(src, nl.toLong())
        val secondOff = u32(src, 4L + nl * 2L + 4)
        if (((secondOff - next) and 0xffffffffL) == firstSize) throw IOException("Layout tidak cocok")
        val idx = src.readFully(4, (indexSize).toInt())
        val bb = idx.le()
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val name = IoUtil.cstr(idx, i * (nl + 4), nl, cs)
            val off = next
            if (i + 1 == count && (name.isEmpty() || off >= len)) break // terminator entry (if any)
            if (name.isEmpty() || !IoUtil.saneName(name)) throw IOException("Nama kosong/tidak valid")
            next = if (i + 1 == count) len else bb.getInt((i + 1) * (nl + 4) + nl).toLong() and 0xffffffffL
            if (next < off || off < indexSize || next > len) throw IOException("Offset tidak valid")
            out += Entry(name.replace('\\', '/'), off, next - off)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n < 2) throw IOException("DAT Circus membutuhkan minimal 2 file")
        val nl = opts.int("dat_nl", 60).let { if (it == 36 || it == 48) it else 60 }
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.replace('/', '\\').toByteArray(cs)
            if (b.size >= nl) throw IOException("Nama terlalu panjang (maks ${nl - 1} byte): ${it.path}")
            b
        }
        val entry = nl + 4
        var pos = 4L + n.toLong() * entry
        val head = java.nio.ByteBuffer.allocate(pos.toInt()).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        head.putInt(n)
        for (i in 0 until n) {
            head.position(4 + i * entry)
            head.put(names[i])
            head.position(4 + i * entry + nl)
            head.putInt(pos.toInt())
            pos += inputs[i].size
        }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        java.io.RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(head.array())
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                IoUtil.appendInput(raf, f, buf)
            }
            progress.update(n, n, "selesai")
        }
    }
}
