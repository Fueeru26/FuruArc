package com.furuarc.formats

import com.furuarc.core.*
import java.io.IOException
import java.io.OutputStream

/**
 * HexenHaus WAG (.wag), magic "IAF_" (port from msg-tool; READ-ONLY).
 * All data after the header is nibble-swapped (rotate 4). u32 offset table at 0x4A; each entry starts with "DATA"
 * with IMGD (image) and FNNE (name) sections. Contents are output as an IMGD block (0x10 header + data).
 */
object HexenHausWagFormat : ArchiveFormat {
    override val id = "wag"
    override val displayName = "WAG (HexenHaus)"
    override val extensions = listOf("wag")

    override val unpackOptions = listOf(ENCODING_SPEC)
    override val canPack: Boolean get() = false

    override fun matches(head: ByteArray) =
        head.size >= 4 && String(head, 0, 4, Charsets.US_ASCII) == "IAF_"

    private const val DATA = 0x41544144
    private const val IMGD = 0x4447_4D49
    private const val FNNE = 0x454E_4E46

    private fun rd(src: RandomSource, pos: Long, n: Int): ByteArray { val b = src.readFully(pos, n); rotr4(b); return b }
    private fun u32(src: RandomSource, pos: Long) = rd(src, pos, 4).le().int

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x50) throw IOException("File terlalu kecil")
        val count = src.readFully(6, 4).le().int
        if (count <= 0 || count > 500_000 || 0x4AL + count * 4L > len) throw IOException("Jumlah entri WAG tidak valid")
        val offBytes = rd(src, 0x4A, count * 4).le()
        val cs = IoUtil.cs(opts)
        val out = ArrayList<Entry>()
        for (k in 0 until count) {
            val offset = offBytes.getInt(k * 4).toLong() and 0xffffffffL
            if (offset + 10 > len) continue
            val h = rd(src, offset, 10).le()
            if (h.getInt(0) != DATA) continue
            val sections = h.getInt(4)
            var name: String? = null
            var dOff = 0L; var dSize = 0L
            var pos = offset + 10
            for (s in 0 until minOf(sections, 64)) {
                if (pos + 8 > len) break
                val sig = u32(src, pos); pos += 4
                when (sig) {
                    IMGD -> {
                        val sz = u32(src, pos).toLong() and 0xffffffffL
                        dOff = pos - 4; dSize = sz + 0x10
                        pos += 4 + sz + 2
                    }
                    FNNE -> {
                        val raw = u32(src, pos).toLong() and 0xffffffffL
                        pos += 4 + 2
                        val nl = maxOf(raw - 2, 0)
                        if (nl > 0) {
                            if (pos + nl > len) break
                            val nb = rd(src, pos, nl.toInt())
                            name = String(nb, cs).trimEnd('\u0000')
                            pos += nl
                        }
                        pos += 2
                    }
                    else -> {
                        val sz = u32(src, pos).toLong() and 0xffffffffL
                        pos += 4 + sz + 2
                    }
                }
            }
            if (dSize == 0L || dOff + dSize > len) continue
            if (!name.isNullOrEmpty() && IoUtil.saneName(name)) out += Entry(name, dOff, dSize)
        }
        if (out.isEmpty()) throw IOException("WAG tidak berisi entri yang terbaca")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        var pos = entry.offset; var left = entry.size
        while (left > 0) {
            val n = minOf(left, 64 * 1024L).toInt()
            out.write(rd(src, pos, n)); pos += n; left -= n
        }
    }

    override fun pack(inputs: List<PackInput>, target: java.io.File, opts: Options, progress: Progress) {
        throw IOException("Pack WAG belum didukung")
    }
}
