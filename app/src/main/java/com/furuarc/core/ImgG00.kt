package com.furuarc.core

import java.io.IOException

/**
 * G00 image decoder (RealLive / Siglus). Port of GARbro RealLive/ImageG00.cs and
 * SiglusSceneScriptUtility g00.py + native_ops.py (lzss / lzss32).
 *
 * Type 0: 24bpp (LZ, 3-byte literal; BGR(A) pixels -> alpha 255).
 * Type 1: 8-bit paletted (LZ; BGRA palette, 4 bytes per entry).
 * Type 2: BGRA 32bpp atlas containing several "cuts"; composited onto the full canvas (first non-empty cut, like GARbro).
 * Type 3 (XOR-ed JPEG) is not supported.
 */
object G00Image {
    private const val MAX_DIM = 16384
    private const val MAX_PIXELS = 100_000_000L

    /** The G00 signature is weak (type byte + dimensions + length), so it is checked as strictly as possible from the header. */
    fun isG00(head: ByteArray): Boolean {
        if (head.size < 9) return false
        val t = head[0].toInt() and 0xff
        if (t > 2) return false
        val w = u16(head, 1)
        val h = u16(head, 3)
        if (w == 0 || h == 0 || w > 0x8000 || h > 0x8000) return false
        if (t == 2) {
            val n = i32(head, 5)
            return n in 1..0x1000
        }
        return i32(head, 5) >= 8
    }

    fun decode(b: ByteArray): RgbaImage {
        if (b.size < 9) throw IOException("File G00 terlalu pendek")
        val type = b[0].toInt() and 0xff
        if (type == 3) throw IOException("G00 tipe 3 (JPEG ber-XOR) belum didukung")
        if (type > 2) throw IOException("Tipe G00 tidak dikenal: $type")
        val w = u16(b, 1)
        val h = u16(b, 3)
        if (w == 0 || h == 0) throw IOException("Dimensi G00 tidak valid")
        if (w > MAX_DIM || h > MAX_DIM || w.toLong() * h > MAX_PIXELS)
            throw IOException("Dimensi G00 terlalu besar: ${w}x$h")
        return when (type) {
            0 -> decodeV0(b, w, h)
            1 -> decodeV1(b, w, h)
            else -> decodeV2(b, w, h)
        }
    }

    private fun u16(b: ByteArray, o: Int) = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8)
    private fun i32(b: ByteArray, o: Int) =
        (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8) or
            ((b[o + 2].toInt() and 0xff) shl 16) or ((b[o + 3].toInt() and 0xff) shl 24)

    /**
     * G00 LZ decompression on b starting at [start] (header: compressed size including the 8-byte header, original size).
     * lit = bytes copied per literal, pad = append a 255 byte after a literal (lzss32), unit = offset/length unit,
     * minCount = minimum length in units.
     */
    private fun lz(b: ByteArray, start: Int, minCount: Int, lit: Int, pad: Boolean, unit: Int, expectOut: Long): ByteArray {
        if (start + 8 > b.size) throw IOException("Data G00 terpotong")
        val packed = i32(b, start)
        val org = i32(b, start + 4)
        if (org < 0 || org.toLong() > 0x7fffffffL / 2) throw IOException("Ukuran data G00 tidak valid")
        if (expectOut >= 0 && org.toLong() != expectOut) throw IOException("Ukuran data G00 tidak cocok dengan dimensi ($org != $expectOut)")
        var end = b.size
        if (packed >= 8 && start.toLong() + packed < end) end = start + packed
        val out = ByteArray(org)
        var p = start + 8
        var d = 0
        val outLit = if (pad) lit + 1 else lit
        while (d < org) {
            if (p >= end) throw IOException("Data G00 terpotong")
            var flags = b[p++].toInt() and 0xff
            for (k in 0 until 8) {
                if (d >= org) break
                if (flags and 1 != 0) {
                    if (p + lit > end) throw IOException("Data G00 terpotong")
                    if (d + outLit > org) {
                        // last literal that does not fit: ignore the rest
                        d = org
                        break
                    }
                    for (j in 0 until lit) out[d + j] = b[p + j]
                    if (pad) out[d + lit] = 0xff.toByte()
                    p += lit
                    d += outLit
                } else {
                    if (p + 2 > end) throw IOException("Data G00 terpotong")
                    val tok = (b[p].toInt() and 0xff) or ((b[p + 1].toInt() and 0xff) shl 8)
                    p += 2
                    val off = (tok shr 4) * unit
                    val cnt = ((tok and 0xF) + minCount) * unit
                    if (off == 0) throw IOException("Referensi LZ G00 tidak valid (offset 0)")
                    val s = d - off
                    if (s < 0) throw IOException("Referensi LZ G00 di luar jendela")
                    var i = 0
                    while (i < cnt && d < org) { out[d] = out[s + i]; d++; i++ }
                }
                flags = flags shr 1
            }
        }
        return out
    }

    private fun decodeV0(b: ByteArray, w: Int, h: Int): RgbaImage {
        if (b.size < 13) throw IOException("Data G00 terpotong")
        val org = i32(b, 9)
        val n = w * h
        val data: ByteArray
        val bpp: Int
        if (org.toLong() == n.toLong() * 4) {
            data = lz(b, 5, 1, 3, true, 4, -1)
            bpp = 4
        } else if (org.toLong() == n.toLong() * 3) {
            data = lz(b, 5, 1, 3, false, 3, -1)
            bpp = 3
        } else throw IOException("Ukuran data G00 tipe 0 tidak cocok dengan dimensi ($org)")
        val rgba = ByteArray(n * 4)
        var s = 0
        var d = 0
        for (i in 0 until n) {
            rgba[d] = data[s + 2]
            rgba[d + 1] = data[s + 1]
            rgba[d + 2] = data[s]
            rgba[d + 3] = if (bpp == 4) data[s + 3] else 0xff.toByte()
            s += bpp; d += 4
        }
        return RgbaImage(w, h, rgba)
    }

    private fun decodeV1(b: ByteArray, w: Int, h: Int): RgbaImage {
        val unp = lz(b, 5, 2, 1, false, 1, -1)
        if (unp.size < 2) throw IOException("Data G00 tipe 1 terlalu pendek")
        val pc = u16(unp, 0)
        val po = 2 + pc * 4
        val n = w * h
        if (unp.size.toLong() < po.toLong() + n) throw IOException("Data G00 tipe 1 terpotong")
        val rgba = ByteArray(n * 4)
        var d = 0
        for (i in 0 until n) {
            val idx = unp[po + i].toInt() and 0xff
            if (idx >= pc) {
                // index outside the palette: transparent black
                d += 4
                continue
            }
            val q = 2 + idx * 4
            rgba[d] = unp[q + 2]
            rgba[d + 1] = unp[q + 1]
            rgba[d + 2] = unp[q]
            rgba[d + 3] = unp[q + 3]
            d += 4
        }
        return RgbaImage(w, h, rgba)
    }

    private fun decodeV2(b: ByteArray, w: Int, h: Int): RgbaImage {
        val count = i32(b, 5)
        if (count <= 0 || count > 0x1000) throw IOException("Jumlah cut G00 tidak valid: $count")
        val hdrEnd = 9 + count * 24
        if (hdrEnd + 8 > b.size) throw IOException("Header G00 tipe 2 terpotong")
        val tx = IntArray(count)
        val ty = IntArray(count)
        for (i in 0 until count) {
            tx[i] = i32(b, 9 + i * 24)
            ty[i] = i32(b, 9 + i * 24 + 4)
        }
        val unp = lz(b, hdrEnd, 2, 1, false, 1, -1)
        if (unp.size < 4 + count * 8) throw IOException("Data G00 tipe 2 terpotong")
        if (i32(unp, 0) != count) throw IOException("Jumlah cut G00 tidak konsisten")
        var sel = -1
        var selOff = 0
        var selLen = 0
        for (i in 0 until count) {
            val off = i32(unp, 4 + i * 8)
            val len = i32(unp, 8 + i * 8)
            if (len != 0) { sel = i; selOff = off; selLen = len; break }
        }
        if (sel < 0) throw IOException("G00 tipe 2 tidak berisi cut")
        if (selOff < 0 || selLen < 116 || selOff.toLong() + selLen > unp.size) throw IOException("Cut G00 di luar data")
        val blkEnd = selOff + selLen
        val ctype = u16(unp, selOff)
        val chips = u16(unp, selOff + 2)
        if (ctype != 1) throw IOException("Tipe cut G00 tidak didukung: $ctype")
        val out = ByteArray(w * h * 4)
        var p = selOff + 4 + 0x70
        for (c in 0 until chips) {
            if (p + 92 > blkEnd) throw IOException("Header chip G00 terpotong")
            val cx = u16(unp, p) + tx[sel]
            val cy = u16(unp, p + 2) + ty[sel]
            val cw = u16(unp, p + 6)
            val ch = u16(unp, p + 8)
            p += 92
            if (cx < 0 || cy < 0 || cx.toLong() + cw > w || cy.toLong() + ch > h) throw IOException("Chip G00 di luar kanvas")
            if (p.toLong() + cw.toLong() * ch * 4 > blkEnd) throw IOException("Data chip G00 terpotong")
            for (row in 0 until ch) {
                var s = p + row * cw * 4
                var d = ((cy + row) * w + cx) * 4
                for (x in 0 until cw) {
                    out[d] = unp[s + 2]
                    out[d + 1] = unp[s + 1]
                    out[d + 2] = unp[s]
                    out[d + 3] = unp[s + 3]
                    s += 4; d += 4
                }
            }
            p += cw * ch * 4
        }
        return RgbaImage(w, h, out)
    }
}
