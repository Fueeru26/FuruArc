package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Favorite (FVP / Favorite View Point) .bin, without magic bytes (detected via structure).
 * Layout: u32 count, u32 name table size; then index [count x 12 bytes]:
 * (name offset u32 relative to the name table, data offset u32 absolute, size u32);
 * then the name table (NUL-terminated strings, Shift-JIS); then data.
 * File contents (.hzc, script .bin, etc.) are extracted as is.
 */
object FavoriteFormat : ArchiveFormat {
    override val id = "favorite"
    override val displayName = "BIN (FVP / Favorite)"
    override val extensions = listOf("bin")

    override fun matches(head: ByteArray) = false // no magic; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index BIN (Favorite) tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset = IoUtil.sjis()): List<Entry> {
        val len = src.length
        if (len < 8 + 12) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 8).le()
        val count = h.getInt(0)
        val namesSize = h.getInt(4).toLong() and 0xffffffffL
        if (count <= 0 || count > 500_000 || namesSize <= 0 || namesSize > 64L * 1024 * 1024)
            throw IOException("Header BIN tidak valid")
        val idxSize = count * 12L
        val namesPos = 8L + idxSize
        val dataStart = namesPos + namesSize
        if (dataStart > len) throw IOException("Index di luar arsip")
        val idx = src.readFully(8, idxSize.toInt()).le()
        val names = src.readFully(namesPos, namesSize.toInt())
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val nameOff = idx.getInt(i * 12).toLong() and 0xffffffffL
            val off = idx.getInt(i * 12 + 4).toLong() and 0xffffffffL
            val size = idx.getInt(i * 12 + 8).toLong() and 0xffffffffL
            if (nameOff >= namesSize) throw IOException("Offset nama tidak valid")
            val name = IoUtil.cstr(names, nameOff.toInt(), (namesSize - nameOff).toInt(), cs)
            if (!IoUtil.saneName(name) || off < dataStart || off + size > len)
                throw IOException("Entri BIN tidak valid")
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
        val nameBytes = inputs.map { it.path.toByteArray(cs) }
        val nameOffs = LongArray(n)
        var namesSize = 0L
        for (i in 0 until n) {
            nameOffs[i] = namesSize
            namesSize += nameBytes[i].size + 1
        }
        var pos = 8L + n * 12L + namesSize
        val head = ByteBuffer.allocate(pos.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.putInt(n).putInt(namesSize.toInt())
        for (i in 0 until n) {
            head.putInt(nameOffs[i].toInt()).putInt(pos.toInt()).putInt(inputs[i].size.toInt())
            pos += inputs[i].size
        }
        for (b in nameBytes) head.put(b).put(0.toByte())
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
