package com.furuarc.formats

import com.furuarc.core.*
import java.io.IOException
import java.io.OutputStream

internal fun rotr4(b: ByteArray, off: Int = 0, len: Int = b.size - off) {
    for (i in off until off + len) { val v = b[i].toInt() and 0xff; b[i] = (((v ushr 4) or (v shl 4)) and 0xff).toByte() }
}

/**
 * HexenHaus ODIO (.dat/.odio audio), magic "ODIO" (port from msg-tool; READ ONLY).
 * Header: "ODIO", u32 0, check u32 0xCCAE01FF at 0x0A; index starts at 0x12, 6 bytes/entry (u32 offset first).
 * Contents are named NNNN.ogg. Blocks starting with "ONCE": 0x2C header bytes are dropped, the rest is nibble-swapped (rotate 4).
 */
object HexenHausOdioFormat : ArchiveFormat {
    override val id = "odio"
    override val displayName = "ODIO (HexenHaus)"
    override val extensions = listOf("dat", "odio")

    override val canPack: Boolean get() = false

    override fun matches(head: ByteArray) =
        head.size >= 4 && String(head, 0, 4, Charsets.US_ASCII) == "ODIO"

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x20) throw IOException("File terlalu kecil")
        if (src.readFully(4, 4).le().int != 0 || src.readFully(0x0A, 4).le().int != 0xCCAE01FF.toInt())
            throw IOException("Header ODIO tidak valid")
        val first = src.readFully(0x12, 4).le().int.toLong() and 0xffffffffL
        if (first < 0x12 || first > len || (first - 0x12) % 6 != 0L) throw IOException("Indeks ODIO tidak valid")
        val n = ((first - 0x12) / 6).toInt()
        if (n <= 0) throw IOException("ODIO kosong")
        val idx = src.readFully(0x12, n * 6 + 0)
        val bb = idx.le()
        val out = ArrayList<Entry>()
        var cur = first
        for (i in 0 until n) {
            val next = if (i + 1 == n) len else (bb.getInt((i + 1) * 6).toLong() and 0xffffffffL)
            if (next > len || next < cur) throw IOException("Offset ODIO tidak berurutan")
            val size = next - cur
            if (size > 0) {
                var off = cur; var sz = size; var meta: Any? = null
                if (size >= 0x2C && String(src.readFully(cur, 4), Charsets.US_ASCII) == "ONCE") { off += 0x2C; sz -= 0x2C; meta = "once" }
                out += Entry(String.format("%04d.ogg", i), off, sz, meta = meta)
            }
            cur = next
        }
        if (out.isEmpty()) throw IOException("ODIO kosong")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        if (entry.meta != "once") { IoUtil.copyRange(src, entry.offset, entry.size, out); return }
        var pos = entry.offset; var left = entry.size
        val buf = ByteArray(64 * 1024)
        while (left > 0) {
            val n = minOf(left, buf.size.toLong()).toInt()
            val b = src.readFully(pos, n)
            rotr4(b)
            out.write(b)
            pos += n; left -= n
        }
    }

    override fun pack(inputs: List<PackInput>, target: java.io.File, opts: Options, progress: Progress) {
        throw IOException("Pack ODIO belum didukung")
    }
}
