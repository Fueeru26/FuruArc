package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * NScripter / ONScripter: SAR (arc.sar) and NSA (arc.nsa), no magic bytes (detected via structure).
 * All numbers big-endian. Header: u16 count, u32 data start offset.
 * SAR : name\0, u32 offset, u32 size.
 * NSA : name\0, u8 compression type, u32 offset, u32 size, u32 original size.
 * Offsets are relative to the data start offset.
 * Read: SAR and NSA type 0 (uncompressed). Compressed NSA (SPB/LZSS/NBZ) not supported yet. Write: SAR.
 */
object NScripterFormat : ArchiveFormat {
    override val id = "nscripter"
    override val displayName = "SAR/NSA (NScripter)"
    override val extensions = listOf("sar", "nsa")

    override fun matches(head: ByteArray) = false // no magic; see detects()

    private class Meta(val type: Int)

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index SAR/NSA tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset = IoUtil.sjis()): List<Entry> {
        var last: Exception? = null
        for (nsa in booleanArrayOf(false, true)) {
            try {
                return parseWith(src, nsa, cs)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index SAR/NSA tidak valid")
    }

    private fun parseWith(src: RandomSource, nsa: Boolean, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        if (len < 6) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 6)
        val count = ((h[0].toInt() and 0xff) shl 8) or (h[1].toInt() and 0xff)
        val base = ByteBuffer.wrap(h, 2, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xffffffffL
        val fixed = if (nsa) 13 else 8
        if (count == 0 || base > len || base < 6L + count * (1L + fixed) || base - 6 > count * 300L)
            throw IOException("Header SAR/NSA tidak valid")
        val idx = src.readFully(6, (base - 6).toInt())
        val bb = ByteBuffer.wrap(idx).order(ByteOrder.BIG_ENDIAN)
        val out = ArrayList<Entry>(count)
        var p = 0
        repeat(count) {
            var e = p
            while (e < idx.size && idx[e].toInt() != 0) {
                if (idx[e] in 0..0x1f) throw IOException("Nama tidak valid")
                e++
            }
            if (e == p || e - p > 255 || e + 1 + fixed > idx.size) throw IOException("Entri tidak valid")
            val name = String(idx, p, e - p, cs)
            var q = e + 1
            var type = 0
            if (nsa) { type = idx[q].toInt() and 0xff; q++ }
            val off = (bb.getInt(q).toLong() and 0xffffffffL) + base
            val size = bb.getInt(q + 4).toLong() and 0xffffffffL
            if (nsa && type !in 0..4) throw IOException("Tipe kompresi tidak valid")
            if (off + size > len) throw IOException("Entri di luar arsip: $name")
            out += Entry(name, off, size, size, Meta(type))
            p = q + 8 + (if (nsa) 4 else 0)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val type = (entry.meta as? Meta)?.type ?: 0
        if (type != 0) throw IOException("Kompresi NSA tipe $type belum didukung")
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun packExtension(vals: Map<String, String>): String? = if (vals["ns_type"] == "nsa") "nsa" else "sar"

    override val packOptions = listOf(
        OptionSpec(
            key = "ns_type", kind = OptKind.CHOICE,
            en = "Archive type", id = "Tipe arsip",
            choices = listOf(
                Choice("sar", "SAR (arc.sar, no compression)", "SAR (arc.sar, tanpa kompresi)"),
                Choice("nsa", "NSA (arc.nsa, stored)", "NSA (arc.nsa, tanpa kompresi)"),
            ),
            default = "sar",
            hintEn = "Choose SAR if you are not sure. Pick NSA if the game expects arc.nsa. Files are stored uncompressed.",
            hintId = "Pilih SAR jika tidak mengetahuinya. Pilih NSA bila game memakai arc.nsa. Isi file disimpan tanpa kompresi.",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n > 0xffff) throw IOException("Terlalu banyak file (maks 65535)")
        val nsa = opts.str("ns_type", "sar") == "nsa"
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.replace('/', '\\').toByteArray(cs)
            if (b.size > 255) throw IOException("Nama terlalu panjang: ${it.path}")
            b
        }
        val rec = if (nsa) 1 + 12 else 8
        val base = 6L + names.sumOf { it.size + 1L + rec }
        var total = 0L
        val head = ByteBuffer.allocate(base.toInt()).order(ByteOrder.BIG_ENDIAN)
        head.putShort(n.toShort()).putInt(base.toInt())
        for (i in 0 until n) {
            head.put(names[i]).put(0.toByte())
            if (nsa) head.put(0.toByte())
            head.putInt(total.toInt()).putInt(inputs[i].size.toInt())
            if (nsa) head.putInt(inputs[i].size.toInt())
            total += inputs[i].size
        }
        if (base + total > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
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
