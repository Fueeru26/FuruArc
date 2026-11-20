package com.furuarc.core

import java.io.IOException
import java.nio.charset.Charset
import java.util.zip.CRC32

/**
 * Majiro RCT / RC8 image decoder, port of GARbro ArcFormats/Majiro/ImageRCT.cs and ImageRC8.cs.
 *
 *  - RCT: signature 98 5A 92 9A + 'T' + 'C'|'S' + '0' + '0'|'1'. 'S' = encrypted (XOR with a 0x400-byte table from the CRC32 of the
 *      cp932 password). Version '1' has a base image name (overlay): pixels (B=0,G=0,R=255) are replaced by base pixels.
 *  - RC8: same signature + "8_00", 256-color RGB palette at 0x14, then compressed data.
 *  - Paired RC8 file "<name>_.rc8" = RCT alpha channel: alpha = ~((B+G+R)/3) from the palette.
 */
object MajiroImage {
    private val SIG = intArrayOf(0x98, 0x5A, 0x92, 0x9A)

    private fun hasSig(h: ByteArray): Boolean {
        if (h.size < 8) return false
        for (i in 0 until 4) if ((h[i].toInt() and 0xFF) != SIG[i]) return false
        return true
    }

    fun isRct(head: ByteArray): Boolean {
        if (!hasSig(head)) return false
        if (head[4].toInt() != 'T'.code) return false
        val e = head[5].toInt()
        if (e != 'C'.code && e != 'S'.code) return false
        if (head[6].toInt() != '0'.code) return false
        val v = head[7].toInt()
        return v == '0'.code || v == '1'.code
    }

    fun isRc8(head: ByteArray): Boolean =
        hasSig(head) && head[4].toInt() == '8'.code && head[5].toInt() == '_'.code &&
            head[6].toInt() == '0'.code && head[7].toInt() == '0'.code

    /**
     * Decode RCT or RC8 to RGBA.
     * [alphaRc8]: contents of the "<name>_.rc8" file for RCT (ignored if dimensions differ, like GARbro).
     * [password]: for encrypted RCT. [base]: caller for the base image name for version 1 RCT (optional).
     */
    fun decode(
        b: ByteArray,
        alphaRc8: ByteArray? = null,
        password: String? = null,
        base: ((String) -> ByteArray?)? = null
    ): RgbaImage {
        try {
            if (isRc8(b)) return decodeRc8(b)
            if (isRct(b)) return decodeRct(b, alphaRc8, password, base)
            throw IOException("Bukan berkas Majiro RCT/RC8")
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data gambar Majiro rusak atau tidak lengkap", e)
        }
    }

    // ------------------------------------------------------------ util

    private fun u16(b: ByteArray, o: Int): Int = (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)

    private fun i32(b: ByteArray, o: Int): Int =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)

    private fun cp932(): Charset =
        try { Charset.forName("windows-31j") } catch (e: Exception) { Charset.forName("Shift_JIS") }

    private fun checkDim(w: Int, h: Int) {
        if (w <= 0 || h <= 0) throw IOException("Dimensi gambar Majiro tidak valid")
        if (w > 16384 || h > 16384 || w.toLong() * h > 100_000_000L)
            throw IOException("Dimensi gambar Majiro terlalu besar: ${w}x$h")
    }

    private class Cursor(val d: ByteArray, var pos: Int) {
        fun byte(): Int {
            if (pos >= d.size) throw IOException("Data Majiro terpotong")
            return d[pos++].toInt() and 0xFF
        }
        fun u16(): Int {
            if (pos + 2 > d.size) throw IOException("Data Majiro terpotong")
            val v = (d[pos].toInt() and 0xFF) or ((d[pos + 1].toInt() and 0xFF) shl 8)
            pos += 2
            return v
        }
        fun read(dst: ByteArray, off: Int, n: Int) {
            if (n < 0 || pos + n > d.size) throw IOException("Data Majiro terpotong")
            System.arraycopy(d, pos, dst, off, n)
            pos += n
        }
    }

    private val RCT_SHIFT = intArrayOf(
        -16, -32, -48, -64, -80, -96,
        49, 33, 17, 1, -15, -31, -47,
        50, 34, 18, 2, -14, -30, -46,
        51, 35, 19, 3, -13, -29, -45,
        36, 20, 4, -12, -28
    )

    private val RC8_SHIFT = intArrayOf(
        -16, -32, -48, -64,
        49, 33, 17, 1, -15, -31, -47,
        34, 18, 2, -14, -30
    )

    private fun copyOverlapped(a: ByteArray, src: Int, dst: Int, n: Int) {
        for (i in 0 until n) a[dst + i] = a[src + i]
    }

    /** RCT decompression: BGR24 result (w*h*3). */
    private fun unpackRct(c: Cursor, w: Int, h: Int): ByteArray {
        val data = ByteArray(w * h * 3)
        var remaining = data.size
        var pos = 0
        var eax = 0
        while (remaining > 0) {
            var count = eax * 3 + 3
            if (count > remaining) throw IOException("Data RCT rusak")
            remaining -= count
            c.read(data, pos, count)
            pos += count
            while (remaining > 0) {
                eax = c.byte()
                if ((eax and 0x80) == 0) {
                    if (eax == 0x7F) eax += c.u16()
                    break
                }
                val si = eax shr 2
                eax = eax and 3
                if (eax == 3) eax += c.u16()
                count = eax * 3 + 3
                if (remaining < count) throw IOException("Data RCT rusak")
                remaining -= count
                var shift = RCT_SHIFT[si and 0x1F]
                val row = (shift and 0x0F) * w
                shift = (shift shr 4) - row
                shift *= 3
                if (shift >= 0 || pos + shift < 0) throw IOException("Referensi RCT tidak valid")
                copyOverlapped(data, pos + shift, pos, count)
                pos += count
            }
        }
        return data
    }

    private class Rc8(val w: Int, val h: Int, val pal: ByteArray, val data: ByteArray)

    private fun readRc8(b: ByteArray): Rc8 {
        if (b.size < 0x314) throw IOException("Header RC8 terpotong")
        val w = i32(b, 8)
        val h = i32(b, 12)
        checkDim(w, h)
        val pal = b.copyOfRange(0x14, 0x314)
        val data = ByteArray(w * h)
        val c = Cursor(b, 0x314)
        var remaining = data.size
        var pos = 0
        var eax = 0
        while (remaining > 0) {
            var count = eax + 1
            if (count > remaining) throw IOException("Data RC8 rusak")
            remaining -= count
            c.read(data, pos, count)
            pos += count
            while (remaining > 0) {
                eax = c.byte()
                if ((eax and 0x80) == 0) {
                    if (eax == 0x7F) eax += c.u16()
                    break
                }
                val si = eax shr 3
                eax = eax and 7
                if (eax == 7) eax += c.u16()
                count = eax + 3
                if (remaining < count) throw IOException("Data RC8 rusak")
                remaining -= count
                var shift = RC8_SHIFT[si and 0x0F]
                val row = (shift and 0x0F) * w
                shift = (shift shr 4) - row
                if (shift >= 0 || pos + shift < 0) throw IOException("Referensi RC8 tidak valid")
                copyOverlapped(data, pos + shift, pos, count)
                pos += count
            }
        }
        return Rc8(w, h, pal, data)
    }

    private fun decodeRc8(b: ByteArray): RgbaImage {
        val r = readRc8(b)
        val out = ByteArray(r.w * r.h * 4)
        var d = 0
        for (v in r.data) {
            val i = (v.toInt() and 0xFF) * 3
            out[d] = r.pal[i]
            out[d + 1] = r.pal[i + 1]
            out[d + 2] = r.pal[i + 2]
            out[d + 3] = 0xFF.toByte()
            d += 4
        }
        return RgbaImage(r.w, r.h, out)
    }

    // ------------------------------------------------------------ RCT

    private class Rct(
        val w: Int, val h: Int, val encrypted: Boolean, val version: Int,
        val dataOffset: Int, val dataSize: Int, val baseNameLen: Int
    )

    private fun readRct(b: ByteArray): Rct {
        if (b.size < 0x14) throw IOException("Header RCT terpotong")
        val w = i32(b, 8)
        val h = i32(b, 12)
        val size = i32(b, 16)
        val version = b[7].toInt() - '0'.code
        var off = 0x14
        var add = 0
        if (version == 1) {
            if (b.size < 0x16) throw IOException("Header RCT terpotong")
            add = u16(b, 0x14)
            off = 0x16
        }
        checkDim(w, h)
        return Rct(w, h, b[5].toInt() == 'S'.code, version, off, size, add)
    }

    private fun keyTable(password: String): ByteArray {
        val pb = password.toByteArray(cp932())
        val crc = CRC32()
        crc.update(pb, 0, pb.size)
        val c = crc.value.toInt()
        val key = ByteArray(0x400)
        for (i in 0 until 0x100) {
            val v = c xor crcTable[(i + c) and 0xFF]
            key[i * 4] = v.toByte()
            key[i * 4 + 1] = (v shr 8).toByte()
            key[i * 4 + 2] = (v shr 16).toByte()
            key[i * 4 + 3] = (v shr 24).toByte()
        }
        return key
    }

    private val crcTable: IntArray by lazy {
        IntArray(256) { n ->
            var c = n
            for (k in 0 until 8) c = if ((c and 1) != 0) (0xEDB88320.toInt() xor (c ushr 1)) else (c ushr 1)
            c
        }
    }

    /** Raw BGR24 pixels from RCT (after decryption/overlay). */
    private fun rctPixels(b: ByteArray, password: String?, base: ((String) -> ByteArray?)?, depth: Int): Pair<Rct, ByteArray> {
        val m = readRct(b)
        var baseImg: ByteArray? = null
        if (m.baseNameLen > 0 && base != null && depth < 8 && m.dataOffset + m.baseNameLen <= b.size) {
            try {
                var e = m.dataOffset
                val lim = m.dataOffset + m.baseNameLen
                while (e < lim && b[e].toInt() != 0) e++
                val name = String(b, m.dataOffset, e - m.dataOffset, cp932())
                val bb = base(name)
                if (bb != null && isRct(bb)) {
                    val bi = rctPixels(bb, password, base, depth + 1)
                    if (bi.first.w == m.w && bi.first.h == m.h) baseImg = bi.second
                }
            } catch (e: Exception) { /* ignore base image errors, like GARbro */ }
        }
        val start = m.dataOffset + m.baseNameLen
        if (start > b.size) throw IOException("Data RCT terpotong")
        val c: Cursor
        if (m.encrypted) {
            if (password.isNullOrEmpty()) throw IOException("RCT terenkripsi: password diperlukan")
            if (m.dataSize < 0 || start.toLong() + m.dataSize > b.size) throw IOException("Data RCT terpotong")
            val key = keyTable(password)
            val d = b.copyOfRange(start, start + m.dataSize)
            for (i in d.indices) d[i] = (d[i].toInt() xor key[i and 0x3FF].toInt()).toByte()
            c = Cursor(d, 0)
        } else {
            c = Cursor(b, start)
        }
        val px = try {
            unpackRct(c, m.w, m.h)
        } catch (e: IOException) {
            if (m.encrypted) throw IOException("Dekripsi RCT gagal (password kemungkinan salah): ${e.message}", e)
            throw e
        }
        if (baseImg != null) {
            var i = 2
            while (i < px.size) {
                if (px[i - 2].toInt() == 0 && px[i - 1].toInt() == 0 && (px[i].toInt() and 0xFF) == 0xFF) {
                    px[i - 2] = baseImg[i - 2]
                    px[i - 1] = baseImg[i - 1]
                    px[i] = baseImg[i]
                }
                i += 3
            }
        }
        return Pair(m, px)
    }

    private fun decodeRct(b: ByteArray, alphaRc8: ByteArray?, password: String?, base: ((String) -> ByteArray?)?): RgbaImage {
        val (m, px) = rctPixels(b, password, base, 0)
        var mask: Rc8? = null
        if (alphaRc8 != null && isRc8(alphaRc8)) {
            try {
                val r = readRc8(alphaRc8)
                if (r.w == m.w && r.h == m.h) mask = r
            } catch (e: IOException) { /* ignore mask errors, like GARbro */ }
        }
        val n = m.w * m.h
        val out = ByteArray(n * 4)
        var s = 0
        var d = 0
        for (i in 0 until n) {
            out[d] = px[s + 2]
            out[d + 1] = px[s + 1]
            out[d + 2] = px[s]
            var a = 0xFF
            if (mask != null) {
                val k = (mask.data[i].toInt() and 0xFF) * 3
                val sum = (mask.pal[k].toInt() and 0xFF) + (mask.pal[k + 1].toInt() and 0xFF) + (mask.pal[k + 2].toInt() and 0xFF)
                a = (sum / 3).inv() and 0xFF
            }
            out[d + 3] = a.toByte()
            s += 3
            d += 4
        }
        return RgbaImage(m.w, m.h, out)
    }

    // ------------------------------------------------------------ key from start.mjo

    /**
     * Find the RCT password inside the start.mjo script (contents from scenario.arc), port of GARbro FindImageKey().
     * Returns null if not found.
     */
    fun findKeyInStartMjo(mjo: ByteArray): String? {
        try {
            val script = mjo.copyOf()
            if (ascii(script, "MajiroObjX1.000")) decryptMjo(script)
            else if (!ascii(script, "MjPlainBytecode")) return null
            val n = i32(script, 0x18)
            var off = 0x20 + n * 8
            while (off < script.size - 4) {
                while (off < script.size && script[off].toInt() != 1) off++
                if (off >= script.size) break
                if (off + 1 < script.size && script[off + 1].toInt() == 8) {
                    val len = u16(script, off + 2)
                    if (len != 0 && len + 12 <= script.size - off &&
                        u16(script, off + len + 4) == 0x0835 &&
                        i32(script, off + len + 6) == 0x7A7B6ED4
                    ) {
                        val st = off + 4
                        var e = st
                        while (e < st + len && script[e].toInt() != 0) e++
                        return String(script, st, e - st, cp932())
                    }
                }
                off++
            }
        } catch (e: RuntimeException) { /* ignore */ }
        return null
    }

    private fun ascii(b: ByteArray, s: String): Boolean {
        if (b.size < s.length) return false
        for (i in s.indices) if (b[i].toInt() != s[i].code) return false
        return true
    }

    private fun decryptMjo(data: ByteArray) {
        var off = 0x1C + 8 * i32(data, 0x18)
        if (off < 0x1C || off >= data.size - 4) return
        val count = i32(data, off)
        off += 4
        if (count <= 0 || count > data.size - off) return
        val t = crcTable
        for (i in 0 until count) {
            val k = i and 0x3FF
            val v = t[k shr 2]
            data[off + i] = (data[off + i].toInt() xor (v shr ((k and 3) * 8))).toByte()
        }
    }
}
