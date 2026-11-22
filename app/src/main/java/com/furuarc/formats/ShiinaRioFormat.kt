package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ShiinaRio WARC archive, read only. Layout follows GARbro ArcFormats/ShiinaRio/ArcWARC.cs (MIT).
 * Header: "WARC", " 1.", digit (version 1.x), u32 index offset XOR 0xF182AD82. Index at the end of the file,
 * records: name[0x10], u32 offset, u32 size, u32 unpacked size, i64 file time, u32 flags (v1.1x).
 * Entries: "YPK" (zlib) is unpacked; stored entries (size == unpacked size) are copied as they are.
 * Not supported: WARC 1.2+ (index/data encrypted with a per-title scheme), YH1 and YLZ packing.
 */
object ShiinaRioFormat : ArchiveFormat {
    override val id = "shiinario"
    override val displayName = "ShiinaRio (WARC)"
    override val extensions = listOf("war", "warc")
    override val canPack: Boolean get() = false

    private const val IDX_KEY = 0xF182AD82L
    private const val REC = 0x10 + 24

    private class Meta(val packed: Boolean)

    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun matches(head: ByteArray): Boolean {
        if (head.size < 12) return false
        return head[0].toInt() == 'W'.code && head[1].toInt() == 'A'.code && head[2].toInt() == 'R'.code &&
            head[3].toInt() == 'C'.code && head[4].toInt() == ' '.code && head[5].toInt() == '1'.code &&
            head[6].toInt() == '.'.code && head[7] in '0'.code.toByte()..'9'.code.toByte()
    }

    private fun parse(idx: ByteArray, len: Long, cs: java.nio.charset.Charset): List<Entry>? {
        if (idx.size < REC || idx.size % REC != 0) return null
        val bb = idx.le()
        val out = ArrayList<Entry>()
        val seen = HashSet<String>()
        var p = 0
        while (p + REC <= idx.size) {
            val name = IoUtil.cstr(idx, p, 0x10, cs)
            val off = bb.getInt(p + 0x10).toLong() and 0xFFFFFFFFL
            val size = bb.getInt(p + 0x14).toLong() and 0xFFFFFFFFL
            val unp = bb.getInt(p + 0x18).toLong() and 0xFFFFFFFFL
            p += REC
            if (name.isEmpty()) continue
            if (!IoUtil.saneName(name) || off + size > len || off < 12) return null
            if (!seen.add(name.lowercase())) continue
            out += Entry(name, off, unp, packedSize = size, meta = Meta(size != unp))
        }
        return out.takeIf { it.isNotEmpty() }
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x20) throw IOException("File terlalu kecil")
        val head = src.readFully(0, 12)
        if (!matches(head)) throw IOException("Bukan arsip WARC ShiinaRio")
        val digit = head[7].toInt() - 0x30
        val version = 100 + digit * 10
        if (version != 110)
            throw IOException("WARC versi 1.$digit memakai enkripsi per judul game — belum didukung (hanya WARC 1.1 yang terbaca)")
        val indexOff = IDX_KEY xor (head.le().getInt(8).toLong() and 0xFFFFFFFFL)
        if (indexOff < 12 || indexOff >= len) throw IOException("Offset indeks WARC tidak valid")
        val n = len - indexOff
        if (n > 0x50000 * 4L) throw IOException("Indeks WARC terlalu besar")
        val raw = src.readFully(indexOff, n.toInt())
        val cs = IoUtil.cs(opts)
        // the index words are XOR-ed with the index offset; try plain first, then XOR-ed
        parse(raw, len, cs)?.let { return it }
        val x = raw.copyOf()
        val bb = ByteBuffer.wrap(x).order(ByteOrder.LITTLE_ENDIAN)
        var i = 0
        while (i + 4 <= x.size) { bb.putInt(i, bb.getInt(i) xor indexOff.toInt()); i += 4 }
        return parse(x, len, cs) ?: throw IOException("Indeks WARC tidak terbaca (mungkin skema enkripsi lain)")
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val packed = (entry.meta as? Meta)?.packed ?: false
        val psize = entry.packedSize
        if (!packed || psize <= 8) {
            IoUtil.copyRange(src, entry.offset, psize, out)
            return
        }
        if (psize > Int.MAX_VALUE) throw IOException("Entri terlalu besar")
        val d = src.readFully(entry.offset, psize.toInt())
        val sig = (d[0].toInt() and 0xFF) or ((d[1].toInt() and 0xFF) shl 8) or ((d[2].toInt() and 0xFF) shl 16)
        when (sig) {
            0x4b5059 -> { // YPK
                if (d[3].toInt() != 0) {
                    val key = 0x4b4d4b4d.inv()
                    val bb = d.le()
                    var i = 8
                    while (i + 4 <= d.size) { bb.putInt(i, bb.getInt(i) xor key); i += 4 }
                    while (i < d.size) { d[i] = (d[i].toInt() xor (key and 0xFF)).toByte(); i++ }
                }
                if (d[8].toInt() != 0x78) throw IOException("Data YPK rusak: ${entry.name}")
                val inf = java.util.zip.InflaterInputStream(java.io.ByteArrayInputStream(d, 8, d.size - 8))
                inf.use { it.copyTo(out) }
            }
            0x314859 -> throw IOException("Pengemasan YH1 belum didukung: ${entry.name}")
            0x5a4c59 -> throw IOException("Pengemasan YLZ belum didukung: ${entry.name}")
            else -> out.write(d)
        }
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        throw IOException("Pengemasan WARC belum didukung")
    }
}
