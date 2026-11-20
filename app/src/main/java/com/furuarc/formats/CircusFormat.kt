package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Circus (.pck), no magic bytes (detected via structure).
 * Layout (per msg-tool & GARbro): u32 count; table (offset u32, size u32) x count;
 * then 0x40-byte entry table x count: name[0x38] (Shift-JIS, NUL-terminated), offset u32 (absolute), size u32; then data.
 * File contents (.crx, .crm, .pcm, etc.) are output as is (not decoded).
 */
object CircusFormat : ArchiveFormat {
    override val id = "circus"
    override val displayName = "PCK (Adv32 / Circus)"
    override val extensions = listOf("pck")

    private const val ENTRY = 0x40
    private const val NAME = 0x38

    override fun matches(head: ByteArray) = false // no magic; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index PCK tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset = IoUtil.sjis()): List<Entry> {
        val len = src.length
        if (len < 4 + 8 + ENTRY) throw IOException("File terlalu kecil")
        val count = src.readFully(0, 4).le().int
        if (count <= 0 || count > 500_000) throw IOException("Jumlah entri tidak valid")
        val tblPos = 4L + count.toLong() * 8
        val dataStart = tblPos + count.toLong() * ENTRY
        if (dataStart > len) throw IOException("Index di luar arsip")
        val pairs = src.readFully(4, count * 8).le()
        val idx = src.readFully(tblPos, count * ENTRY)
        val bb = idx.le()
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val o = i * ENTRY
            val name = IoUtil.cstr(idx, o, NAME, cs)
            val off = bb.getInt(o + NAME).toLong() and 0xffffffffL
            val size = bb.getInt(o + NAME + 4).toLong() and 0xffffffffL
            if (!IoUtil.saneName(name) || off < dataStart || off + size > len ||
                pairs.getInt(i * 8).toLong() and 0xffffffffL != off || pairs.getInt(i * 8 + 4).toLong() and 0xffffffffL != size)
                throw IOException("Entri PCK tidak valid")
            out += Entry(name, off, size)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override val packOptions = listOf(ENCODING_SPEC)
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= NAME) throw IOException("Nama terlalu panjang (maks 55 byte): ${it.path}")
            b
        }
        val tblPos = 4 + n * 8
        var pos = tblPos.toLong() + n.toLong() * ENTRY
        val head = ByteBuffer.allocate(pos.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.putInt(n)
        for (i in 0 until n) {
            head.position(4 + i * 8)
            head.putInt((pos + 0).toInt()).putInt(inputs[i].size.toInt())
            val o = tblPos + i * ENTRY
            head.position(o)
            head.put(names[i])
            head.position(o + NAME)
            head.putInt(pos.toInt()).putInt(inputs[i].size.toInt())
            pos += inputs[i].size
        }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
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
