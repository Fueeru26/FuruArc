package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Silky's / AI5WIN (.arc), no magic bytes (detected by structure).
 * Layout: u32 count; then entries [N-byte name + 8]: name (N = 0x14, 0x1E, or 0x20; XOR-ed with 1 byte),
 * u32 size, u32 offset (absolute). Both size/offset orders are tried when reading.
 * The name XOR key is guessed from the padding bytes (NUL padding -> the most frequent byte = key).
 * File contents (.mes, .grp, etc.) are extracted as is (not decoded / not decompressed).
 */
object SilkyFormat : ArchiveFormat {
    override val id = "silky"
    override val displayName = "ARC (Silky Engine)"
    override val extensions = listOf("arc")

    override fun matches(head: ByteArray) = false // no magic; see detects()

    private class Meta(val key: Int)

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index ARC (Silky) tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset = IoUtil.sjis()): List<Entry> {
        var last: Exception? = null
        for (nameLen in intArrayOf(0x20, 0x1E, 0x14)) for (sizeFirst in booleanArrayOf(true, false)) {
            try {
                return parseWith(src, nameLen, sizeFirst, cs)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index ARC (Silky) tidak valid")
    }

    private fun parseWith(src: RandomSource, nameLen: Int, sizeFirst: Boolean, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        if (len < 4 + nameLen + 8) throw IOException("File terlalu kecil")
        val count = src.readFully(0, 4).le().int
        if (count <= 0 || count > 500_000) throw IOException("Jumlah entri tidak valid")
        val entrySize = nameLen + 8
        val dataStart = 4L + count.toLong() * entrySize
        if (dataStart > len) throw IOException("Index di luar arsip")
        val idx = src.readFully(4, count * entrySize)
        val bb = idx.le()
        // guess the XOR key: the last byte of each name = padding (NUL) if the name is shorter than N
        val freq = IntArray(256)
        for (i in 0 until count) freq[idx[i * entrySize + nameLen - 1].toInt() and 0xff]++
        var key = 0
        for (k in 1..255) if (freq[k] > freq[key]) key = k
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val o = i * entrySize
            val nb = ByteArray(nameLen) { (idx[o + it].toInt() xor key).toByte() }
            val name = IoUtil.cstr(nb, 0, nameLen, cs)
            val a = bb.getInt(o + nameLen).toLong() and 0xffffffffL
            val b = bb.getInt(o + nameLen + 4).toLong() and 0xffffffffL
            val size = if (sizeFirst) a else b
            val off = if (sizeFirst) b else a
            if (!IoUtil.saneName(name) || off < dataStart || off + size > len)
                throw IOException("Entri ARC (Silky) tidak valid")
            out += Entry(name, off, size, meta = Meta(key))
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "silky_name", kind = OptKind.CHOICE,
            en = "Name field size", id = "Ukuran kolom nama",
            choices = listOf(Choice("32", "32 bytes (0x20)", "32 byte (0x20)"), Choice("30", "30 bytes (0x1E)", "30 byte (0x1E)"), Choice("20", "20 bytes (0x14)", "20 byte (0x14)")),
            default = "32",
            hintEn = "Choose 32 if you are not sure.", hintId = "Pilih 32 jika tidak mengetahuinya.",
        ),
        OptionSpec(
            key = "silky_xor", kind = OptKind.NUMBER,
            en = "Name XOR key 0-255 (0 = none)", id = "Kunci XOR nama 0-255 (0 = tanpa)",
            default = "0",
            hintEn = "Same key the original game uses. 0 for most.", hintId = "Samakan dengan kunci game asli. 0 untuk kebanyakan.",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val nameLen = when (opts.int("silky_name", 32)) { 20 -> 0x14; 30 -> 0x1E; else -> 0x20 }
        val key = (opts.int("silky_xor", opts.xorKey ?: 0)) and 0xff
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= nameLen) throw IOException("Nama terlalu panjang (maks ${nameLen - 1} byte): ${it.path}")
            b
        }
        val entrySize = nameLen + 8
        var pos = 4L + n.toLong() * entrySize
        val head = ByteBuffer.allocate(pos.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.putInt(n)
        for (i in 0 until n) {
            val nb = ByteArray(nameLen)
            names[i].copyInto(nb)
            for (k in nb.indices) nb[k] = (nb[k].toInt() xor key).toByte()
            head.put(nb).putInt(inputs[i].size.toInt()).putInt(pos.toInt())
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
