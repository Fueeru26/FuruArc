package com.furuarc.core

import java.io.IOException

/**
 * PGD image decoder (Softpal / Amuse Craft and its subsidiaries), port of
 * GARbro ArcFormats/Softpal/ImagePGD.cs (morkt/GARbro).
 *
 * Covered variants (all present in the GARbro source):
 *  - 'GE ' (0x20 00)  : method 1 (planes A,R,G,B), method 2 (YCbCr 4:2:0 -> BGR), method 3 (per-row prediction)
 *  - 'GE\u001C' + "11_C" : LZ-compressed B,G,R,A planes (look-behind 0xFFC)
 *  - "00_C" at offset 0x18 : contains an LZ-compressed TGA (look-behind 3000)
 *  - TGA with a 0x18-byte header prefix (x,y,w,h) : plain TGA after offset 0x18
 *  - 'PGD3' / 'PGD2'  : difference (XOR) image against the 'GE ' base image named in the header
 *
 * Honest note about other engines: in the whole GARbro source there is only ONE PGD family (Softpal).
 * No evidence was found of PGD variants specific to Circus/Adv32, Artemis, or Yu-Ris (Yu-Ris uses .ycg,
 * see YcgImage; Artemis has no .pgd format in GARbro; Circus uses CRX). So no other
 * variants are invented here.
 */
object PgdImage {
    private const val V_NONE = 0
    private const val V_GE = 1
    private const val V_11C = 2
    private const val V_00C = 3
    private const val V_TGA = 4
    private const val V_PGD3 = 5

    /** Lightweight metadata from reading the header. [baseName] is only filled for PGD3/PGD2. */
    class Info(
        val variant: String,
        val width: Int,
        val height: Int,
        val offsetX: Int,
        val offsetY: Int,
        val baseName: String?
    )

    fun isPgd(head: ByteArray): Boolean = variantOf(head) != V_NONE

    /** Base image name required by PGD3/PGD2, or null if it is not PGD3. */
    fun baseNameOf(head: ByteArray): String? =
        if (variantOf(head) == V_PGD3) readBaseName(head) else null

    fun info(b: ByteArray): Info {
        return when (variantOf(b)) {
            V_GE -> Info("GE", i32(b, 0x0C), i32(b, 0x10), i32(b, 4), i32(b, 8), null)
            V_11C -> Info("11_C", i32(b, 0x0C), i32(b, 0x10), i32(b, 4), i32(b, 8), null)
            V_00C -> Info("00_C", i32(b, 8), i32(b, 12), i32(b, 0), i32(b, 4), null)
            V_TGA -> Info("TGA", i32(b, 8), i32(b, 12), i32(b, 0), i32(b, 4), null)
            V_PGD3 -> Info("PGD3", u16(b, 8), u16(b, 0xA), u16(b, 4), u16(b, 6), readBaseName(b))
            else -> throw IOException("Bukan berkas PGD yang dikenal")
        }
    }

    /**
     * Decode PGD to RGBA. For PGD3/PGD2 (difference), [base] is called with the base image name
     * (e.g. "bg01.pgd") and must return the contents of that file (or null if it does not exist).
     */
    fun decode(b: ByteArray, base: ((String) -> ByteArray?)? = null): RgbaImage {
        try {
            return when (variantOf(b)) {
                V_GE -> rawToRgba(decodeGe(b))
                V_11C -> decode11(b)
                V_00C -> decode00(b)
                V_TGA -> PgdTga.decode(b, 0x18)
                V_PGD3 -> decodeInc(b, base)
                else -> throw IOException("Bukan berkas PGD yang dikenal")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data PGD rusak atau tidak lengkap", e)
        }
    }

    // ---------------------------------------------------------------- detection

    private fun variantOf(h: ByteArray): Int {
        val n = h.size
        if (n >= 4) {
            if (h[0] == 'G'.code.toByte() && h[1] == 'E'.code.toByte()) {
                if (h[2].toInt() == 0x20) return V_GE
                if (h[2].toInt() == 0x1C) {
                    return if (n >= 0x20 && ascii(h, 0x1C, "11_C")) V_11C else V_NONE
                }
            }
            if (h[0] == 'P'.code.toByte() && h[1] == 'G'.code.toByte() && h[2] == 'D'.code.toByte() &&
                (h[3] == '3'.code.toByte() || h[3] == '2'.code.toByte())
            ) return V_PGD3
        }
        if (n >= 0x1C && ascii(h, 0x18, "00_C")) return V_00C
        if (n >= 0x2A) {
            val x = i32(h, 0)
            val y = i32(h, 4)
            if (Math.abs(x.toLong()) <= 0x2000 && Math.abs(y.toLong()) <= 0x2000) {
                val w = i32(h, 8)
                val hh = i32(h, 12)
                val tt = h[0x18 + 2].toInt() and 0xFF
                if (w > 0 && hh > 0 && w == u16(h, 0x24) && hh == u16(h, 0x26) &&
                    (tt == 1 || tt == 2 || tt == 3 || tt == 9 || tt == 10 || tt == 11)
                ) return V_TGA
            }
        }
        return V_NONE
    }

    private fun ascii(b: ByteArray, off: Int, s: String): Boolean {
        if (off + s.length > b.size) return false
        for (i in s.indices) if (b[off + i].toInt() != s[i].code) return false
        return true
    }

    private fun u16(b: ByteArray, o: Int): Int = (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)

    private fun i32(b: ByteArray, o: Int): Int =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)

    private fun readBaseName(b: ByteArray): String? {
        if (b.size < 0x30) return null
        var end = 0xE
        while (end < 0x30 && b[end].toInt() != 0) end++
        if (end == 0xE) return null
        return String(b, 0xE, end - 0xE, Charsets.ISO_8859_1)
    }

    private fun checkDim(w: Int, h: Int) {
        if (w <= 0 || h <= 0) throw IOException("Dimensi gambar PGD tidak valid")
        if (w > 16384 || h > 16384 || w.toLong() * h.toLong() > 100_000_000L)
            throw IOException("Dimensi gambar PGD terlalu besar: ${w}x$h")
    }

    // ---------------------------------------------------------------- raw result

    /** BGR (bpp=3) or BGRA (bpp=4) pixels, top to bottom. */
    private class Raw(val width: Int, val height: Int, val bpp: Int, val data: ByteArray)

    private fun rawToRgba(r: Raw): RgbaImage {
        checkDim(r.width, r.height)
        val n = r.width * r.height
        if (r.data.size < n * r.bpp) throw IOException("Data piksel PGD kurang")
        val out = ByteArray(n * 4)
        var s = 0
        var d = 0
        for (i in 0 until n) {
            out[d] = r.data[s + 2]
            out[d + 1] = r.data[s + 1]
            out[d + 2] = r.data[s]
            out[d + 3] = if (r.bpp == 4) r.data[s + 3] else 0xFF.toByte()
            s += r.bpp
            d += 4
        }
        return RgbaImage(r.width, r.height, out)
    }

    // ---------------------------------------------------------------- variants

    private fun decode11(b: ByteArray): RgbaImage {
        val w = i32(b, 0x0C)
        val h = i32(b, 0x10)
        checkDim(w, h)
        val planes = unpackPlain(b, 0x20, w.toLong() * h * 4 + 0x1000, 0xFFC)
        val ps = w * h
        if (planes.size < ps * 4) throw IOException("Data PGD 11_C kurang")
        val out = ByteArray(ps * 4)
        var d = 0
        for (i in 0 until ps) {
            out[d++] = planes[2 * ps + i] // R
            out[d++] = planes[ps + i]     // G
            out[d++] = planes[i]          // B
            out[d++] = planes[3 * ps + i] // A
        }
        return RgbaImage(w, h, out)
    }

    private fun decode00(b: ByteArray): RgbaImage {
        val w = i32(b, 8)
        val h = i32(b, 12)
        checkDim(w, h)
        val data = unpackPlain(b, 0x1C, w.toLong() * h * 4 + 0x100000, 3000)
        return PgdTga.decode(data, 0)
    }

    /** 'GE ' (three methods). */
    private fun decodeGe(b: ByteArray): Raw {
        if (b.size < 0x28) throw IOException("Header PGD terpotong")
        var w = i32(b, 0x0C)
        var h = i32(b, 0x10)
        val method = u16(b, 0x1C)
        checkDim(w, h)
        val out = unpackGe(b, 0x20, w.toLong() * h * 4 + h + 0x10000)
        when (method) {
            1 -> {
                val ps = w * h
                if (out.size < ps * 4) throw IOException("Data PGD GE kurang")
                val o = ByteArray(ps * 4)
                var d = 0
                for (i in 0 until ps) {
                    o[d++] = out[3 * ps + i] // B
                    o[d++] = out[2 * ps + i] // G
                    o[d++] = out[ps + i]     // R
                    o[d++] = out[i]          // A
                }
                return Raw(w, h, 4, o)
            }
            2 -> return Raw(w, h, 3, postProcess2(out, w, h))
            3 -> {
                val bpp = u16(out, 2)
                if (bpp != 32 && bpp != 24) throw IOException("PGD GE method 3: bpp tidak valid")
                w = u16(out, 4)
                h = u16(out, 6)
                checkDim(w, h)
                return Raw(w, h, bpp / 8, postProcessPal(out, 8, bpp / 8, w, h))
            }
            else -> throw IOException("Kompresi PGD GE method $method belum didukung")
        }
    }

    private fun decodeInc(b: ByteArray, base: ((String) -> ByteArray?)?): RgbaImage {
        if (b.size < 0x38) throw IOException("Header PGD3 terpotong")
        val offX = u16(b, 4)
        val offY = u16(b, 6)
        val w = u16(b, 8)
        val h = u16(b, 0xA)
        val bpp = u16(b, 0xC)
        val baseName = readBaseName(b) ?: throw IOException("PGD3 tidak menyebut nama gambar dasar")
        checkDim(w, h)
        if (bpp != 24 && bpp != 32) throw IOException("PGD3: bpp $bpp belum didukung")
        if (base == null) throw IOException("PGD3 butuh gambar dasar '$baseName' (callback base tidak diberikan)")
        val baseBytes = base(baseName) ?: throw IOException("Gambar dasar PGD3 '$baseName' tidak ditemukan")
        if (variantOf(baseBytes) != V_GE) throw IOException("Format gambar dasar PGD3 tidak valid (harus PGD 'GE')")
        val img = decodeGe(baseBytes)
        if (offX + w > img.width || offY + h > img.height)
            throw IOException("Dimensi gambar dasar PGD3 tidak cocok")

        val overlay = unpackGe(b, 0x30, w.toLong() * h * 4 + h + 0x10000)
        val ov = postProcessPal(overlay, 0, bpp / 8, w, h)

        val ovBpp = bpp / 8
        val baseBpp = img.bpp
        val image = img.data
        var dst = (offY * img.width + offX) * baseBpp
        val gap = (img.width - w) * baseBpp
        var src = 0
        val applyAlpha = ovBpp == 4 && baseBpp == 4
        for (y in 0 until h) {
            for (x in 0 until w) {
                image[dst] = (image[dst].toInt() xor ov[src].toInt()).toByte()
                image[dst + 1] = (image[dst + 1].toInt() xor ov[src + 1].toInt()).toByte()
                image[dst + 2] = (image[dst + 2].toInt() xor ov[src + 2].toInt()).toByte()
                if (applyAlpha) image[dst + 3] = (image[dst + 3].toInt() xor ov[src + 3].toInt()).toByte()
                dst += baseBpp
                src += ovBpp
            }
            dst += gap
        }
        return rawToRgba(img)
    }

    // ---------------------------------------------------------------- LZ decompression

    private fun u32le(b: ByteArray, o: Int): Long = i32(b, o).toLong() and 0xFFFFFFFFL

    private fun allocUnpacked(b: ByteArray, pos: Int, limit: Long): ByteArray {
        if (pos + 8 > b.size) throw IOException("Data PGD terpotong")
        val size = u32le(b, pos)
        if (size <= 0 || size > limit) throw IOException("Ukuran data PGD tidak masuk akal ($size)")
        return ByteArray(size.toInt())
    }

    /** GARbro's Unpack(): used by 00_C and 11_C. Data starts at pos+8. */
    private fun unpackPlain(b: ByteArray, pos: Int, limit: Long, lookBehind: Int): ByteArray {
        val out = allocUnpacked(b, pos, limit)
        var ip = pos + 8
        var dst = 0
        var ctl = 2
        while (dst < out.size) {
            ctl = ctl shr 1
            if (ctl == 1) {
                if (ip >= b.size) throw IOException("Data PGD terpotong")
                ctl = (b[ip++].toInt() and 0xFF) or 0x100
            }
            var count: Int
            if ((ctl and 1) != 0) {
                if (ip + 3 > b.size) throw IOException("Data PGD terpotong")
                var src = u16(b, ip)
                count = b[ip + 2].toInt() and 0xFF
                ip += 3
                if (dst > lookBehind) src += dst - lookBehind
                if (src < 0 || src > dst) throw IOException("Referensi LZ PGD tidak valid")
                if (dst + count > out.size) count = out.size - dst
                for (i in 0 until count) out[dst + i] = out[src + i]
            } else {
                if (ip >= b.size) throw IOException("Data PGD terpotong")
                count = b[ip++].toInt() and 0xFF
                if (ip + count > b.size) throw IOException("Data PGD terpotong")
                if (dst + count > out.size) count = out.size - dst
                System.arraycopy(b, ip, out, dst, count)
                ip += count
                // if count is truncated, the loop ends
                if (count < 0) count = 0
            }
            dst += count
        }
        return out
    }

    /** GARbro's UnpackGePre(): used by GE and the PGD3 overlay. */
    private fun unpackGe(b: ByteArray, pos: Int, limit: Long): ByteArray {
        val out = allocUnpacked(b, pos, limit)
        var ip = pos + 8
        var dst = 0
        var ctl = 2
        while (dst < out.size) {
            ctl = ctl shr 1
            if (ctl == 1) {
                if (ip >= b.size) throw IOException("Data PGD terpotong")
                ctl = (b[ip++].toInt() and 0xFF) or 0x100
            }
            var count: Int
            if ((ctl and 1) != 0) {
                if (ip + 2 > b.size) throw IOException("Data PGD terpotong")
                var offset = u16(b, ip)
                ip += 2
                count = offset and 7
                if ((offset and 8) == 0) {
                    if (ip >= b.size) throw IOException("Data PGD terpotong")
                    count = (count shl 8) or (b[ip++].toInt() and 0xFF)
                }
                count += 4
                offset = offset shr 4
                val src = dst - offset
                if (src < 0) throw IOException("Referensi LZ PGD tidak valid")
                if (dst + count > out.size) count = out.size - dst
                for (i in 0 until count) out[dst + i] = out[src + i]
            } else {
                if (ip >= b.size) throw IOException("Data PGD terpotong")
                count = b[ip++].toInt() and 0xFF
                if (ip + count > b.size) throw IOException("Data PGD terpotong")
                if (dst + count > out.size) count = out.size - dst
                System.arraycopy(b, ip, out, dst, count)
                ip += count
            }
            dst += count
        }
        return out
    }

    // ---------------------------------------------------------------- post-processing

    private fun clamp(v: Int): Byte = (if (v > 255) 255 else if (v < 0) 0 else v).toByte()

    /** PostProcess2: YCbCr 4:2:0 -> BGR24. */
    private fun postProcess2(input: ByteArray, w: Int, h: Int): ByteArray {
        val stride = w * 3
        val segment = w * h / 4
        var src0 = 0
        var src1 = segment
        var src2 = segment + src1
        val output = ByteArray(stride * h)
        var dst = 0
        val points = intArrayOf(0, 1, w, w + 1)
        for (y in h / 2 downTo 1) {
            for (x in w / 2 downTo 1) {
                val i0 = input[src0].toInt()
                val i1 = input[src1].toInt()
                val bb = 226 * i0
                val gg = -43 * i0 - 89 * i1
                val rr = 179 * i1
                src0++
                src1++
                for (p in points) {
                    val baseValue = (input[src2 + p].toInt() and 0xFF) shl 7
                    var o = dst + 3 * p
                    output[o++] = clamp((baseValue + bb) shr 7)
                    output[o++] = clamp((baseValue + gg) shr 7)
                    output[o] = clamp((baseValue + rr) shr 7)
                }
                src2 += 2
                dst += 6
            }
            src2 += w
            dst += stride
        }
        return output
    }

    /** PostProcessPal: per-row prediction (flag 1 = left, 2 = up, otherwise = average). */
    private fun postProcessPal(input: ByteArray, start: Int, px: Int, w: Int, h: Int): ByteArray {
        val stride = w * px
        val output = ByteArray(h * stride)
        var ctl = start
        var src = start + h
        var dst = 0
        for (row in 0 until h) {
            val c = input[ctl++].toInt()
            if ((c and 1) != 0) {
                var prev = dst
                System.arraycopy(input, src, output, dst, px)
                src += px
                dst += px
                var count = stride - px
                while (count-- > 0) {
                    output[dst++] = (output[prev++].toInt() - input[src++].toInt()).toByte()
                }
            } else if ((c and 2) != 0) {
                var prev = dst - stride
                var count = stride
                while (count-- > 0) {
                    output[dst++] = (output[prev++].toInt() - input[src++].toInt()).toByte()
                }
            } else {
                System.arraycopy(input, src, output, dst, px)
                dst += px
                src += px
                var prev = dst - stride
                var count = stride - px
                while (count-- > 0) {
                    val avg = ((output[prev++].toInt() and 0xFF) + (output[dst - px].toInt() and 0xFF)) / 2
                    output[dst] = (avg - (input[src++].toInt() and 0xFF)).toByte()
                    dst++
                }
            }
        }
        return output
    }
}

/** Minimal TGA decoder (types 1,2,3,9,10,11; 8/15/16/24/32 bpp) for PGD variants containing a TGA. */
private object PgdTga {
    fun decode(b: ByteArray, off: Int): RgbaImage {
        if (off + 18 > b.size) throw IOException("Header TGA dalam PGD terpotong")
        val idLen = b[off].toInt() and 0xFF
        val cmType = b[off + 1].toInt() and 0xFF
        val type = b[off + 2].toInt() and 0xFF
        val cmLen = u16(b, off + 5)
        val cmDepth = b[off + 7].toInt() and 0xFF
        val w = u16(b, off + 12)
        val h = u16(b, off + 14)
        val bpp = b[off + 16].toInt() and 0xFF
        val desc = b[off + 17].toInt() and 0xFF
        if (w <= 0 || h <= 0 || w > 16384 || h > 16384 || w.toLong() * h > 100_000_000L)
            throw IOException("Dimensi TGA dalam PGD tidak valid: ${w}x$h")
        if (cmType > 1) throw IOException("TGA dalam PGD: tipe colormap tidak didukung")
        if (bpp != 8 && bpp != 15 && bpp != 16 && bpp != 24 && bpp != 32)
            throw IOException("TGA dalam PGD: bpp $bpp tidak didukung")
        if (type != 1 && type != 2 && type != 3 && type != 9 && type != 10 && type != 11)
            throw IOException("TGA dalam PGD: tipe gambar $type belum didukung")
        val ps = (bpp + 7) / 8
        var pos = off + 18 + idLen
        var pal: IntArray? = null
        if (cmType == 1) {
            if (cmDepth != 24 && cmDepth != 32) throw IOException("TGA dalam PGD: kedalaman palet tidak didukung")
            val es = cmDepth / 8
            if (pos + cmLen * es > b.size) throw IOException("Palet TGA terpotong")
            pal = IntArray(cmLen)
            for (i in 0 until cmLen) {
                val o = pos + i * es
                pal[i] = ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 1].toInt() and 0xFF) shl 8) or (b[o].toInt() and 0xFF)
            }
            pos += cmLen * es
        }
        if (type == 1 || type == 9) {
            if (pal == null || bpp != 8) throw IOException("TGA berpalet tidak valid")
        }
        val stride = w * ps
        val raw = ByteArray(stride * h)
        if (type == 1 || type == 2 || type == 3) {
            val n = Math.min(raw.size, b.size - pos)
            if (n < raw.size) throw IOException("Data piksel TGA dalam PGD terpotong")
            System.arraycopy(b, pos, raw, 0, raw.size)
        } else {
            var dst = 0
            while (dst < raw.size && pos < b.size) {
                val pk = b[pos++].toInt() and 0xFF
                var count = (pk and 0x7F) + 1
                if ((pk and 0x80) != 0) {
                    if (pos + ps > b.size) break
                    val s = dst
                    System.arraycopy(b, pos, raw, dst, Math.min(ps, raw.size - dst))
                    pos += ps
                    dst += ps
                    var i = 1
                    while (i < count && dst + ps <= raw.size) {
                        System.arraycopy(raw, s, raw, dst, ps)
                        dst += ps
                        i++
                    }
                } else {
                    count *= ps
                    if (pos + count > b.size) break
                    val c = Math.min(count, raw.size - dst)
                    System.arraycopy(b, pos, raw, dst, c)
                    pos += count
                    dst += c
                }
            }
        }
        val topDown = (desc and 0x20) != 0
        val out = ByteArray(w * h * 4)
        for (y in 0 until h) {
            val sy = if (topDown) y else h - 1 - y
            var s = sy * stride
            var d = y * w * 4
            for (x in 0 until w) {
                var r: Int
                var g: Int
                var bl: Int
                var a = 255
                when (bpp) {
                    8 -> {
                        val v = raw[s].toInt() and 0xFF
                        if (pal != null) {
                            val c = if (v < pal.size) pal[v] else 0
                            r = (c shr 16) and 0xFF; g = (c shr 8) and 0xFF; bl = c and 0xFF
                        } else {
                            r = v; g = v; bl = v
                        }
                    }
                    15, 16 -> {
                        val v = u16(raw, s)
                        val b5 = v and 31
                        val g5 = (v shr 5) and 31
                        val r5 = (v shr 10) and 31
                        bl = (b5 shl 3) or (b5 shr 2)
                        g = (g5 shl 3) or (g5 shr 2)
                        r = (r5 shl 3) or (r5 shr 2)
                    }
                    24 -> {
                        bl = raw[s].toInt() and 0xFF; g = raw[s + 1].toInt() and 0xFF; r = raw[s + 2].toInt() and 0xFF
                    }
                    else -> {
                        bl = raw[s].toInt() and 0xFF; g = raw[s + 1].toInt() and 0xFF; r = raw[s + 2].toInt() and 0xFF
                        a = raw[s + 3].toInt() and 0xFF
                    }
                }
                out[d] = r.toByte()
                out[d + 1] = g.toByte()
                out[d + 2] = bl.toByte()
                out[d + 3] = a.toByte()
                s += ps
                d += 4
            }
        }
        return RgbaImage(w, h, out)
    }

    private fun u16(b: ByteArray, o: Int): Int = (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)
}
