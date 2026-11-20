package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * HexenHaus ARCC (.arc), magic "ARCC" (port from msg-tool; READ ONLY).
 * Sections: NAME (0x2A) -> NIDX -> EIDX -> CINF (name list, XOR 0x69) ... ADDR (u64 offset of each entry).
 * Each file starts with a "FILE" block: u32 size at +0x18, data starts at +0x22.
 */
object HexenHausArccFormat : ArchiveFormat {
    override val id = "arcc"
    override val displayName = "ARC (HexenHaus ARCC)"
    override val extensions = listOf("arc")

    override val unpackOptions = listOf(ENCODING_SPEC)
    override val canPack: Boolean get() = false

    override fun matches(head: ByteArray) =
        head.size >= 4 && head[0] == 'A'.code.toByte() && head[1] == 'R'.code.toByte() &&
            head[2] == 'C'.code.toByte() && head[3] == 'C'.code.toByte()

    private fun tag(src: RandomSource, pos: Long, t: String) {
        val b = src.readFully(pos, 4)
        if (String(b, Charsets.US_ASCII) != t) throw IOException("Seksi $t tidak ditemukan di ARCC")
    }

    private fun u16(src: RandomSource, pos: Long) = src.readFully(pos, 2).le().short.toInt() and 0xffff
    private fun u32(src: RandomSource, pos: Long) = src.readFully(pos, 4).le().int.toLong() and 0xffffffffL
    private fun u64(src: RandomSource, pos: Long) = src.readFully(pos, 8).le().long

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x40) throw IOException("File terlalu kecil")
        val count = u32(src, 0x14)
        if (count <= 0 || count > 500_000) throw IOException("Jumlah entri ARCC tidak valid")
        val n = count.toInt()
        var p = 0x2AL
        tag(src, p, "NAME")
        var addr = u64(src, p + 4)
        p += 0x0E
        tag(src, p, "NIDX")
        p += 4 + 8L * n
        tag(src, p, "EIDX")
        p += 4 + 8L * n
        tag(src, p, "CINF")
        p += 4
        val cs = IoUtil.cs(opts)
        val names = ArrayList<String>(n)
        repeat(n) {
            p += 6
            val nl = u16(src, p)
            val nb = if (nl > 0) src.readFully(p + 4, nl) else ByteArray(0)
            for (i in nb.indices) nb[i] = (nb[i].toInt() xor 0x69).toByte()
            p += 6 + nl
            names += String(nb, cs).replace('\\', '/')
        }
        if (addr <= 0 || addr >= len) throw IOException("Offset ADDR tidak valid")
        tag(src, addr, "ADDR")
        addr += 4
        val out = ArrayList<Entry>(n)
        for (i in 0 until n) {
            var off = u64(src, addr + 2)
            addr += 12
            if (off < 0 || off + 0x22 > len) continue
            val t = src.readFully(off, 4)
            if (String(t, Charsets.US_ASCII) != "FILE") continue
            val size = u32(src, off + 0x18)
            off += 0x22
            if (size > 0 && off + size <= len && IoUtil.saneName(names[i])) out += Entry(names[i], off, size)
        }
        if (out.isEmpty()) throw IOException("ARCC tidak berisi file")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        throw IOException("Pack ARCC belum didukung")
    }
}
