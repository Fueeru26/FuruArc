package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * QLIE (.pack), trailer "FilePackVer1.0/2.0/3.0" (last 0x1C bytes):
 * magic[16], u32 count, u64 index offset.
 * Entry: u16 name length, name (XOR-ed; v2/v3 = UTF-16LE), u64 offset, u32 size, u32 original size,
 * u32 is_packed, u32 is_encrypted, u32 hash (optional). The name XOR key is guessed per entry.
 * READ-ONLY, and only files that are neither compressed nor encrypted. Other files need the game key
 * + QLIE decompression and will fail with a clear message. Pack is not supported yet.
 */
object QlieFormat : ArchiveFormat {
    override val id = "qlie"
    override val displayName = "PACK (QLIE)"
    override val extensions = listOf("pack")

    /** Read-only: the Pack Archive menu is hidden for QLIE (see [pack]). */
    override val canPack: Boolean get() = false

    private const val TRAILER = 0x1C
    private val PREFIX = "FilePackVer".toByteArray(Charsets.US_ASCII)

    private class Meta(val packed: Boolean, val encrypted: Boolean)

    override fun matches(head: ByteArray) = false // magic is at the end of the file; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean {
        if (src.length < TRAILER + 8) return false
        val t = src.readFully(src.length - TRAILER, 16)
        return PREFIX.indices.all { t[it] == PREFIX[it] }
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        val t = src.readFully(len - TRAILER, TRAILER)
        if (!PREFIX.indices.all { t[it] == PREFIX[it] }) throw IOException("Bukan arsip QLIE")
        val major = t[11].toInt() - '0'.code
        val bb = t.le()
        val count = bb.getInt(16)
        val idxOff = bb.getLong(20)
        val idxEnd = len - TRAILER
        if (count <= 0 || count > 500_000 || idxOff <= 0 || idxOff >= idxEnd) throw IOException("Index QLIE tidak valid")
        val idx = src.readFully(idxOff, (idxEnd - idxOff).toInt())
        var last: Exception? = null
        for (tail in intArrayOf(28, 24)) {
            try {
                return parse(idx, count, tail, major, len)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index QLIE tidak valid")
    }

    private fun parse(idx: ByteArray, count: Int, tail: Int, major: Int, len: Long): List<Entry> {
        val bb = idx.le()
        val wide = major >= 2
        val out = ArrayList<Entry>(count)
        var p = 0
        for (i in 0 until count) {
            if (p + 2 > idx.size) throw IOException("Index terpotong")
            val n = bb.getShort(p).toInt() and 0xffff
            p += 2
            val nb = if (wide) n * 2 else n
            if (n == 0 || p + nb + tail > idx.size) throw IOException("Panjang nama tidak valid")
            val name = decodeName(idx, p, nb, n, wide) ?: throw IOException("Nama QLIE tidak terbaca")
            p += nb
            val off = bb.getLong(p)
            val size = bb.getInt(p + 8).toLong() and 0xffffffffL
            val packed = bb.getInt(p + 16) != 0
            val enc = bb.getInt(p + 20) != 0
            p += tail
            if (off < 0 || off + size > len) throw IOException("Entri QLIE di luar arsip")
            out += Entry(name, off, size, meta = Meta(packed, enc))
        }
        if (p != idx.size) throw IOException("Index QLIE tidak pas")
        return out
    }

    /** Try several XOR forms + key 0..255 until the name contains plausible characters. */
    private fun decodeName(idx: ByteArray, p: Int, nb: Int, n: Int, wide: Boolean): String? {
        val cs = if (wide) Charsets.UTF_16LE else IoUtil.sjis()
        for (form in 0..1) for (k in 0..255) {
            val b = ByteArray(nb) {
                val x = if (form == 0) ((it + 1) xor k) else k
                (idx[p + it].toInt() xor x).toByte()
            }
            val s = String(b, cs)
            if (IoUtil.saneName(s) && s.all { c -> c.code >= 0x20 && c != '\uFFFD' } && s.contains('.')) return s
        }
        return null
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val m = entry.meta as? Meta
        if (m != null && (m.packed || m.encrypted))
            throw IOException("File terkompresi/terenkripsi QLIE (butuh kunci game, belum didukung)")
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        throw IOException("Pack QLIE belum didukung")
    }
}
