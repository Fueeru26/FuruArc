package com.furuarc.core

import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * CatSystem2 HG-2 / HG-3 (.hg2/.hg3) image decoder, pure JVM (java.util.zip for zlib).
 *
 * Sources (faithful port): GARbro ArcFormats/CatSystem/ImageHG3.cs + ImageHG2.cs (morkt/GARbro),
 * trigger-segfault/TriggersTools.CatSystem2 (HG-3 tag list, frame chain, canvas/offset, img_al, img_jpg),
 * HSLL175848494/Catsystem2 (confirms segment layout & payload sizes).
 *
 * Format summary:
 *  - 12-byte header: "HG-2"/"HG-3", header size (12), version/type. Frames are chained via OffsetNext (relative to frame start).
 *  - HG-3: each frame = [OffsetNext, Id] then a tag chain (8-byte name, offsetNext, length): "stdinfo" (w,h,bpp,offset,total),
 *    "img####" (pixel bitmap), "img_jpg"+"img_al" (JPEG + alpha mask), "img_al" alone, "imgmode", "cptype", "ats####" (ignored).
 *  - HG-2: version 0x10 (header 0x30), 0x20 (0x50), 0x25 (0x58); one frame per structure, chained via OffsetNext (0x20/0x25).
 *  - Pixels: two zlib streams (data + ctl). ctl = LSB-first bits: copy/skip flag + Elias-gamma run; the result is split into
 *    four 2-bit planes, un-zigzagged, then horizontal (first row) and vertical delta filters are applied.
 *
 * Covered variants: HG-3 img0000 (24/32 bpp, vertically flipped), HG-3 img_jpg (+img_al), HG-3 img_al only
 * (gray mask, like TriggersTools), HG-2 v0x10 (not flipped, alpha inverted, depth scaling), HG-2 v0x20/0x25 (flipped).
 *
 * Limitations: HG-3 "img_wbp" (WebP) is not supported (GARbro does not support it either). JPEG is decoded via [jpegDecoder] if set
 * (on Android, set it to BitmapFactory), otherwise the internal baseline JPEG decoder.
 * Swapping R/B like GARbro's Android-IRIS "imgmode" is not applied.
 */
object HgImage {
    private const val MAX_DIM = 16384
    private const val MAX_PIXELS = 100_000_000L
    private const val MAX_FRAMES = 4096

    /**
     * Optional hook for JPEG (img_jpg). Called with the JPEG bytes, returns RGBA or null on failure.
     * On Android, install a BitmapFactory decoder here so progressive JPEGs can be read too.
     */
    @Volatile
    var jpegDecoder: ((ByteArray) -> RgbaImage?)? = null

    fun isHg(head: ByteArray): Boolean =
        head.size >= 4 && head[0] == 'H'.code.toByte() && head[1] == 'G'.code.toByte() &&
            head[2] == '-'.code.toByte() && (head[3] == '2'.code.toByte() || head[3] == '3'.code.toByte())

    /** All frames/layers, each already composed onto the full canvas (TotalWidth x TotalHeight) when needed. */
    fun decodeAll(b: ByteArray): List<RgbaImage> = run(b, false)

    /** Main result = first frame. */
    fun decode(b: ByteArray): RgbaImage = run(b, true)[0]

    private fun run(b: ByteArray, firstOnly: Boolean): List<RgbaImage> {
        try {
            if (!isHg(b) || b.size < 12) throw IOException("Bukan berkas HG-2/HG-3")
            return if (b[3] == '3'.code.toByte()) decodeHg3(b, firstOnly) else decodeHg2(b, firstOnly)
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data HG rusak atau tidak lengkap", e)
        }
    }

    // ---------------------------------------------------------------- util

    private fun i32(b: ByteArray, o: Int): Int {
        if (o < 0 || o + 4 > b.size) throw IOException("Data HG terpotong")
        return (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
    }

    private fun i16(b: ByteArray, o: Int): Int {
        if (o < 0 || o + 2 > b.size) throw IOException("Data HG terpotong")
        return (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)
    }

    private fun checkDims(w: Int, h: Int) {
        if (w <= 0 || h <= 0 || w > MAX_DIM || h > MAX_DIM || w.toLong() * h > MAX_PIXELS) {
            throw IOException("Dimensi gambar HG tidak wajar: ${w}x$h")
        }
    }

    private fun inflate(b: ByteArray, off: Int, len: Int, expected: Int, lenient: Boolean): ByteArray {
        if (off < 0 || len < 0 || expected < 0 || off.toLong() + len > b.size) throw IOException("Data HG terpotong")
        val out = ByteArray(expected)
        val inf = Inflater()
        var n = 0
        try {
            inf.setInput(b, off, len)
            while (n < expected) {
                val r = inf.inflate(out, n, expected - n)
                if (r == 0) break
                n += r
            }
        } catch (e: DataFormatException) {
            if (!lenient) throw IOException("Aliran zlib HG rusak", e)
        } finally {
            inf.end()
        }
        if (n < expected && !lenient) throw IOException("Aliran zlib HG lebih pendek dari ukuran yang dinyatakan")
        return out
    }

    // ---------------------------------------------------------------- pixels (data + ctl)

    private class Raw(val px: ByteArray, val stride: Int)

    private class Bits(val d: ByteArray) {
        var pos = 0
        var bit = 0
        fun next(): Int {
            if (pos >= d.size) throw IOException("Data kontrol HG habis")
            val v = (d[pos].toInt() shr bit) and 1
            if (++bit == 8) { bit = 0; pos++ }
            return v
        }
        fun gamma(): Int {
            var n = 0
            while (next() == 0) {
                if (++n >= 32) throw IOException("Overflow Elias-gamma HG")
            }
            var v = 1L
            while (n-- > 0) v = (v shl 1) or next().toLong()
            if (v > Int.MAX_VALUE) throw IOException("Nilai Elias-gamma HG terlalu besar")
            return v.toInt()
        }
    }

    private val T0 = IntArray(256)
    private val T1 = IntArray(256)
    private val T2 = IntArray(256)
    private val T3 = IntArray(256)

    init {
        for (i in 0 until 256) {
            var v = i and 0xC0
            v = v shl 6
            v = v or (i and 0x30)
            v = v shl 6
            v = v or (i and 0x0C)
            v = v shl 6
            v = v or (i and 0x03)
            T0[i] = v shl 6
            T1[i] = v shl 4
            T2[i] = v shl 2
            T3[i] = v
        }
    }

    private fun conv(v: Int): Int {
        val c = v and 1
        val x = (v and 0xFF) ushr 1
        return if (c != 0) x xor 0xFF else x
    }

    /** Decompress the data+ctl streams into BGR(A) pixels with the delta already reversed. */
    private fun unpack(
        b: ByteArray, dataOff: Int, dPacked: Int, dUnpacked: Int, cPacked: Int, cUnpacked: Int,
        w: Int, h: Int, bpp: Int
    ): Raw {
        if (bpp != 24 && bpp != 32) throw IOException("Kedalaman warna HG tidak didukung: $bpp bpp")
        checkDims(w, h)
        if (dPacked <= 0 || dUnpacked < 0 || cPacked <= 0 || cUnpacked <= 0) throw IOException("Ukuran aliran HG tidak valid")
        val px = bpp / 8
        val aligned = (w * px + 3) and 3.inv()
        val limit = aligned.toLong() * h * 2 + 0x1000
        if (dUnpacked > limit || cUnpacked > limit) throw IOException("Ukuran aliran HG tidak wajar")
        val data = inflate(b, dataOff, dPacked, dUnpacked, false)
        val ctl = inflate(b, dataOff + dPacked, cPacked, cUnpacked, false)

        val bits = Bits(ctl)
        var copy = bits.next() != 0
        val size = bits.gamma()
        if (size.toLong() > limit) throw IOException("Ukuran data HG tidak wajar")
        val mixed = ByteArray(size)
        var src = 0
        var dst = 0
        while (dst < size) {
            val count = bits.gamma()
            if (count > size - dst) throw IOException("Run HG melebihi ukuran buffer")
            if (copy) {
                if (count > data.size - src) throw IOException("Data HG lebih pendek dari yang dijelaskan peta")
                System.arraycopy(data, src, mixed, dst, count)
                src += count
            }
            dst += count
            copy = !copy
        }

        // Pick the stride: 4-byte aligned (asmodean/TriggersTools) or packed (GARbro), depending on the actual buffer size.
        val plain = w * px
        val stride = when {
            aligned == plain -> plain
            size == aligned * h -> aligned
            size == plain * h -> plain
            size >= aligned * h -> aligned
            size >= plain * h -> plain
            else -> throw IOException("Buffer piksel HG lebih kecil dari ukuran gambar")
        }
        val total = stride * h
        val quarter = size / 4
        val words = (total + 3) / 4
        if (words > quarter) throw IOException("Buffer piksel HG lebih kecil dari ukuran gambar")
        val out = ByteArray(words * 4)
        var o = 0
        for (i in 0 until words) {
            val v = T0[mixed[i].toInt() and 0xFF] or T1[mixed[quarter + i].toInt() and 0xFF] or
                T2[mixed[quarter * 2 + i].toInt() and 0xFF] or T3[mixed[quarter * 3 + i].toInt() and 0xFF]
            out[o++] = conv(v).toByte()
            out[o++] = conv(v ushr 8).toByte()
            out[o++] = conv(v ushr 16).toByte()
            out[o++] = conv(v ushr 24).toByte()
        }
        for (x in px until stride) out[x] = (out[x] + out[x - px]).toByte()
        for (y in 1 until h) {
            val line = y * stride
            val prev = line - stride
            for (x in 0 until stride) out[line + x] = (out[line + x] + out[prev + x]).toByte()
        }
        return Raw(out, stride)
    }

    /**
     * BGR(A) -> RGBA. [flip]: rows are flipped; [depth] 1..7: color channel scaling (HG-2 v0x10); [invA]: invert alpha (HG-2 v0x10).
     */
    private fun toRgba(r: Raw, w: Int, h: Int, bpp: Int, flip: Boolean, depth: Int, invA: Boolean): RgbaImage {
        val px = bpp / 8
        val out = ByteArray(w * h * 4)
        val max = if (depth in 1..7) (1 shl depth) - 1 else 0
        var o = 0
        for (y in 0 until h) {
            var s = (if (flip) h - 1 - y else y) * r.stride
            for (x in 0 until w) {
                var bl = r.px[s].toInt() and 0xFF
                var gr = r.px[s + 1].toInt() and 0xFF
                var rd = r.px[s + 2].toInt() and 0xFF
                if (max != 0) {
                    bl = (bl * 0xFF / max) and 0xFF
                    gr = (gr * 0xFF / max) and 0xFF
                    rd = (rd * 0xFF / max) and 0xFF
                }
                var a = 0xFF
                if (px == 4) {
                    a = r.px[s + 3].toInt() and 0xFF
                    if (invA) a = a xor 0xFF
                }
                out[o] = rd.toByte()
                out[o + 1] = gr.toByte()
                out[o + 2] = bl.toByte()
                out[o + 3] = a.toByte()
                o += 4
                s += px
            }
        }
        return RgbaImage(w, h, out)
    }

    /** Paste the image onto a totalW x totalH canvas at (offX, offY), clipped at the edges. */
    private fun compose(img: RgbaImage, offX: Int, offY: Int, totW: Int, totH: Int): RgbaImage {
        if (totW <= 0 || totH <= 0) return img
        if (totW == img.width && totH == img.height && offX == 0 && offY == 0) return img
        if (totW > MAX_DIM || totH > MAX_DIM || totW.toLong() * totH > MAX_PIXELS) return img
        val out = ByteArray(totW * totH * 4)
        for (y in 0 until img.height) {
            val dy = y + offY
            if (dy < 0 || dy >= totH) continue
            val x0 = maxOf(0, -offX)
            val x1 = minOf(img.width, totW - offX)
            if (x1 <= x0) continue
            System.arraycopy(img.rgba, (y * img.width + x0) * 4, out, (dy * totW + offX + x0) * 4, (x1 - x0) * 4)
        }
        return RgbaImage(totW, totH, out)
    }

    // ---------------------------------------------------------------- HG-3

    private class Tag(val pos: Int, val name: String, val length: Int)

    private fun tagName(b: ByteArray, pos: Int): String {
        if (pos < 0 || pos + 16 > b.size) throw IOException("Data HG terpotong")
        val sb = StringBuilder()
        for (i in 0 until 8) {
            val c = b[pos + i].toInt() and 0xFF
            if (c == 0) break
            sb.append(c.toChar())
        }
        return sb.toString()
    }

    private fun isImgNumbered(n: String): Boolean {
        if (n.length < 7 || !n.startsWith("img")) return false
        for (i in 3 until n.length) if (n[i] !in '0'..'9') return false
        return true
    }

    private fun decodeHg3(b: ByteArray, firstOnly: Boolean): List<RgbaImage> {
        val frames = ArrayList<RgbaImage>()
        var fs = 12
        var count = 0
        while (true) {
            if (++count > MAX_FRAMES) throw IOException("Terlalu banyak frame HG-3")
            val nextFrame = i32(b, fs)
            var tagPos = fs + 8
            val first = tagName(b, tagPos)
            if (!first.startsWith("stdinfo")) throw IOException("HG-3: tag \"stdinfo\" tidak ditemukan")
            val sp = tagPos + 16
            val w = i32(b, sp)
            val h = i32(b, sp + 4)
            val depth = i32(b, sp + 8)
            val offX = i32(b, sp + 12)
            val offY = i32(b, sp + 16)
            val totW = i32(b, sp + 20)
            val totH = i32(b, sp + 24)
            var img: Tag? = null
            var jpg: Tag? = null
            var al: Tag? = null
            var guard = 0
            while (true) {
                val nextTag = i32(b, tagPos + 8)
                if (nextTag == 0) break
                if (nextTag < 16 || ++guard > 256 || tagPos.toLong() + nextTag + 16 > b.size) {
                    throw IOException("Rantai tag HG-3 rusak")
                }
                tagPos += nextTag
                val name = tagName(b, tagPos)
                val t = Tag(tagPos, name, i32(b, tagPos + 12))
                when {
                    isImgNumbered(name) -> if (img == null) img = t
                    name == "img_jpg" -> if (jpg == null) jpg = t
                    name == "img_al" -> if (al == null) al = t
                }
            }
            val frame = when {
                img != null -> frameImg(b, img, w, h, depth)
                jpg != null -> frameJpeg(b, jpg, al, w, h)
                al != null -> frameAlpha(b, al, w, h)
                else -> throw IOException("HG-3: tidak ada tag gambar pada frame ${frames.size}")
            }
            frames.add(compose(frame, offX, offY, totW, totH))
            if (firstOnly || nextFrame == 0) break
            if (nextFrame < 0 || fs.toLong() + nextFrame + 8 >= b.size) throw IOException("Rantai frame HG-3 rusak")
            fs += nextFrame
        }
        return frames
    }

    private fun frameImg(b: ByteArray, t: Tag, w: Int, h: Int, depth: Int): RgbaImage {
        val p = t.pos + 16 + 8
        val r = unpack(b, t.pos + 0x28, i32(b, p), i32(b, p + 4), i32(b, p + 8), i32(b, p + 12), w, h, depth)
        return toRgba(r, w, h, depth, true, 0, false)
    }

    private fun readAlpha(b: ByteArray, t: Tag, n: Int): ByteArray {
        val packed = i32(b, t.pos + 16)
        val raw = i32(b, t.pos + 20)
        if (packed < 0 || raw < 0 || raw > n * 2 + 1024) throw IOException("Ukuran img_al tidak valid")
        val a = inflate(b, t.pos + 24, packed, raw, true)
        return if (a.size >= n) a else a.copyOf(n)
    }

    private fun frameJpeg(b: ByteArray, t: Tag, al: Tag?, w: Int, h: Int): RgbaImage {
        checkDims(w, h)
        val len = t.length
        if (len <= 0 || t.pos.toLong() + 16 + len > b.size) throw IOException("Data JPEG HG-3 terpotong")
        val jpeg = b.copyOfRange(t.pos + 16, t.pos + 16 + len)
        val j = decodeJpeg(jpeg)
        val rgba = j.rgba
        val n = j.width * j.height
        if (al != null) {
            val a = readAlpha(b, al, n)
            var o = 3
            for (i in 0 until n) { rgba[o] = a[i]; o += 4 }
        } else {
            var o = 3
            for (i in 0 until n) { rgba[o] = -1; o += 4 }
        }
        return j
    }

    private fun frameAlpha(b: ByteArray, t: Tag, w: Int, h: Int): RgbaImage {
        checkDims(w, h)
        val n = w * h
        val a = readAlpha(b, t, n)
        val out = ByteArray(n * 4)
        var o = 0
        for (i in 0 until n) {
            val v = (255 - (a[i].toInt() and 0xFF)).toByte()
            out[o] = v; out[o + 1] = v; out[o + 2] = v; out[o + 3] = -1
            o += 4
        }
        return RgbaImage(w, h, out)
    }

    // ---------------------------------------------------------------- HG-2

    private fun decodeHg2(b: ByteArray, firstOnly: Boolean): List<RgbaImage> {
        val version = i32(b, 8)
        val hdr = when (version) { 0x25 -> 0x58; 0x20 -> 0x50; 0x10 -> 0x30; else -> throw IOException("Versi HG-2 tidak didukung: 0x${Integer.toHexString(version)}") }
        val frames = ArrayList<RgbaImage>()
        var fs = 12
        var count = 0
        while (true) {
            if (++count > MAX_FRAMES) throw IOException("Terlalu banyak frame HG-2")
            val w = i32(b, fs)
            val h = i32(b, fs + 4)
            val bpp = i16(b, fs + 8)
            val depth = i16(b, fs + 10)
            val dPacked = i32(b, fs + 0x14)
            val dUnpacked = i32(b, fs + 0x18)
            val cPacked = i32(b, fs + 0x1C)
            val cUnpacked = i32(b, fs + 0x20)
            val dataOff = fs + hdr - 12
            val r = unpack(b, dataOff, dPacked, dUnpacked, cPacked, cUnpacked, w, h, bpp)
            var img: RgbaImage
            var next = 0
            if (version > 0x10) {
                img = toRgba(r, w, h, bpp, true, 0, false)
                val totW = i32(b, fs + 0x2C)
                val totH = i32(b, fs + 0x30)
                val offX = i32(b, fs + 0x34)
                val offY = i32(b, fs + 0x38)
                next = i32(b, fs + 0x40)
                img = compose(img, offX, offY, totW, totH)
            } else {
                img = toRgba(r, w, h, bpp, false, depth, bpp == 32)
            }
            frames.add(img)
            if (firstOnly || next == 0) break
            if (next < 0 || fs.toLong() + next + hdr >= b.size) throw IOException("Rantai frame HG-2 rusak")
            fs += next
        }
        return frames
    }

    // ---------------------------------------------------------------- JPEG

    private fun decodeJpeg(jpeg: ByteArray): RgbaImage {
        val hook = jpegDecoder
        if (hook != null) {
            try {
                val r = hook(jpeg)
                if (r != null) return RgbaImage(r.width, r.height, r.rgba.copyOf(r.width * r.height * 4))
            } catch (e: Throwable) {
                // continue to another decoder
            }
        }
        try {
            val r = MiniJpeg.decode(jpeg)
            if (r != null) return r
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            // try another path
        }
        throw IOException("JPEG di dalam HG-3 tidak dapat didekode (progresif/CMYK memerlukan HgImage.jpegDecoder)")
    }

    /** Baseline/sequential Huffman 8-bit JPEG decoder (1 or 3 components). Returns null if it is not that kind. */
    private object MiniJpeg {
        private val ZZ = intArrayOf(
            0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63
        )
        private val COS = Array(8) { x -> FloatArray(8) { u ->
            ((if (u == 0) Math.sqrt(0.5) else 1.0) * Math.cos((2 * x + 1) * u * Math.PI / 16.0) / 2.0).toFloat()
        } }

        private class Huff(counts: IntArray, val symbols: IntArray) {
            val minCode = IntArray(17)
            val maxCode = IntArray(18) { -1 }
            val valPtr = IntArray(17)
            init {
                var code = 0
                var k = 0
                for (l in 1..16) {
                    valPtr[l] = k
                    minCode[l] = code
                    code += counts[l - 1]
                    k += counts[l - 1]
                    maxCode[l] = if (counts[l - 1] > 0) code - 1 else -1
                    code = code shl 1
                }
            }
        }

        private class Comp(val id: Int, val h: Int, val v: Int, val tq: Int) {
            var td = 0
            var ta = 0
            var bw = 0
            var bh = 0
            var pred = 0
            lateinit var plane: ByteArray
        }

        private class Reader(val d: ByteArray, var pos: Int) {
            var acc = 0
            var cnt = 0
            var hitMarker = false
            fun bit(): Int {
                if (cnt == 0) {
                    var v = 0
                    if (!hitMarker && pos < d.size) {
                        v = d[pos].toInt() and 0xFF
                        if (v == 0xFF) {
                            val n = if (pos + 1 < d.size) d[pos + 1].toInt() and 0xFF else 0xD9
                            if (n == 0) pos += 2
                            else { hitMarker = true; v = 0 }
                        } else pos++
                    }
                    acc = v
                    cnt = 8
                }
                cnt--
                return (acc shr cnt) and 1
            }
            fun recv(n: Int): Int { var v = 0; for (i in 0 until n) v = (v shl 1) or bit(); return v }
            fun reset() { acc = 0; cnt = 0; hitMarker = false }
        }

        private fun huff(r: Reader, t: Huff): Int {
            var code = 0
            for (l in 1..16) {
                code = (code shl 1) or r.bit()
                val mx = t.maxCode[l]
                if (mx >= 0 && code <= mx && code >= t.minCode[l]) {
                    val idx = t.valPtr[l] + code - t.minCode[l]
                    if (idx >= t.symbols.size) throw IOException("Huffman JPEG rusak")
                    return t.symbols[idx]
                }
            }
            throw IOException("Kode Huffman JPEG tidak valid")
        }

        private fun extend(v: Int, t: Int): Int = if (t == 0) 0 else if (v < (1 shl (t - 1))) v - (1 shl t) + 1 else v

        fun decode(d: ByteArray): RgbaImage? {
            if (d.size < 4 || (d[0].toInt() and 0xFF) != 0xFF || (d[1].toInt() and 0xFF) != 0xD8) return null
            val qt = Array(4) { IntArray(64) { 1 } }
            val dcT = arrayOfNulls<Huff>(4)
            val acT = arrayOfNulls<Huff>(4)
            var comps: Array<Comp>? = null
            var width = 0
            var height = 0
            var restart = 0
            var adobe = -1
            var hmax = 1
            var vmax = 1
            var pos = 2
            var scans = 0
            while (pos + 4 <= d.size) {
                if ((d[pos].toInt() and 0xFF) != 0xFF) { pos++; continue }
                val m = d[pos + 1].toInt() and 0xFF
                if (m == 0xFF) { pos++; continue }
                if (m == 0 || m == 0x01 || m in 0xD0..0xD7) { pos += 2; continue }
                if (m == 0xD9) break
                val len = ((d[pos + 2].toInt() and 0xFF) shl 8) or (d[pos + 3].toInt() and 0xFF)
                val seg = pos + 4
                val end = pos + 2 + len
                if (len < 2 || end > d.size) throw IOException("Segmen JPEG terpotong")
                when (m) {
                    0xC0, 0xC1 -> {
                        if ((d[seg].toInt() and 0xFF) != 8) return null
                        height = ((d[seg + 1].toInt() and 0xFF) shl 8) or (d[seg + 2].toInt() and 0xFF)
                        width = ((d[seg + 3].toInt() and 0xFF) shl 8) or (d[seg + 4].toInt() and 0xFF)
                        val nf = d[seg + 5].toInt() and 0xFF
                        if (nf != 1 && nf != 3) return null
                        checkDims(width, height)
                        val cs = Array(nf) { i ->
                            val o = seg + 6 + i * 3
                            val hv = d[o + 1].toInt() and 0xFF
                            Comp(d[o].toInt() and 0xFF, hv shr 4, hv and 15, d[o + 2].toInt() and 3)
                        }
                        for (c in cs) if (c.h !in 1..4 || c.v !in 1..4) return null
                        hmax = cs.maxOf { it.h }
                        vmax = cs.maxOf { it.v }
                        val mx = (width + 8 * hmax - 1) / (8 * hmax)
                        val my = (height + 8 * vmax - 1) / (8 * vmax)
                        for (c in cs) {
                            c.bw = mx * c.h
                            c.bh = my * c.v
                            c.plane = ByteArray(c.bw * 8 * c.bh * 8)
                        }
                        comps = cs
                    }
                    0xC2, 0xC3, 0xC5, 0xC6, 0xC7, 0xC9, 0xCA, 0xCB, 0xCD, 0xCE, 0xCF -> return null
                    0xC4 -> {
                        var o = seg
                        while (o < end) {
                            val tcth = d[o++].toInt() and 0xFF
                            val counts = IntArray(16) { d[o + it].toInt() and 0xFF }
                            o += 16
                            val n = counts.sum()
                            if (o + n > end) throw IOException("DHT JPEG rusak")
                            val sy = IntArray(n) { d[o + it].toInt() and 0xFF }
                            o += n
                            val t = Huff(counts, sy)
                            if ((tcth shr 4) == 0) dcT[tcth and 3] = t else acT[tcth and 3] = t
                        }
                    }
                    0xDB -> {
                        var o = seg
                        while (o < end) {
                            val pq = d[o].toInt() and 0xFF
                            val q = qt[pq and 3]
                            o++
                            for (i in 0 until 64) {
                                if ((pq shr 4) != 0) { q[i] = ((d[o].toInt() and 0xFF) shl 8) or (d[o + 1].toInt() and 0xFF); o += 2 }
                                else { q[i] = d[o].toInt() and 0xFF; o++ }
                            }
                        }
                    }
                    0xDD -> restart = ((d[seg].toInt() and 0xFF) shl 8) or (d[seg + 1].toInt() and 0xFF)
                    0xEE -> if (len >= 14 && d[seg] == 'A'.code.toByte() && d[seg + 1] == 'd'.code.toByte() && d[seg + 2] == 'o'.code.toByte()) {
                        adobe = d[seg + 11].toInt() and 0xFF
                    }
                    0xDA -> {
                        val cs = comps ?: throw IOException("SOS JPEG sebelum SOF")
                        val ns = d[seg].toInt() and 0xFF
                        val sc = ArrayList<Comp>()
                        for (i in 0 until ns) {
                            val id = d[seg + 1 + i * 2].toInt() and 0xFF
                            val tt = d[seg + 2 + i * 2].toInt() and 0xFF
                            val c = cs.firstOrNull { it.id == id } ?: throw IOException("Komponen JPEG tidak dikenal")
                            c.td = (tt shr 4) and 3
                            c.ta = tt and 3
                            sc.add(c)
                        }
                        val ss = d[seg + 1 + ns * 2].toInt() and 0xFF
                        val se = d[seg + 2 + ns * 2].toInt() and 0xFF
                        if (ss != 0 || (se != 63 && se != 0)) return null
                        pos = scan(d, end, sc, cs, qt, dcT, acT, restart, width, height, hmax, vmax)
                        scans++
                        continue
                    }
                }
                pos = end
            }
            val cs = comps ?: return null
            if (scans == 0) return null
            return output(cs, width, height, hmax, vmax, adobe)
        }

        private fun scan(
            d: ByteArray, start: Int, sc: List<Comp>, all: Array<Comp>, qt: Array<IntArray>,
            dcT: Array<Huff?>, acT: Array<Huff?>, restart: Int, width: Int, height: Int, hmax: Int, vmax: Int
        ): Int {
            val r = Reader(d, start)
            for (c in all) c.pred = 0
            val coef = FloatArray(64)
            val tmp = FloatArray(64)
            val inter = sc.size > 1
            val mcusX: Int
            val mcusY: Int
            if (inter) {
                mcusX = (width + 8 * hmax - 1) / (8 * hmax)
                mcusY = (height + 8 * vmax - 1) / (8 * vmax)
            } else {
                val c = sc[0]
                mcusX = ((width * c.h + hmax - 1) / hmax + 7) / 8
                mcusY = ((height * c.v + vmax - 1) / vmax + 7) / 8
            }
            var n = 0
            for (my in 0 until mcusY) for (mx in 0 until mcusX) {
                if (restart > 0 && n > 0 && n % restart == 0) {
                    // RSTn synchronization
                    r.reset()
                    var p = r.pos
                    while (p + 1 < d.size && !((d[p].toInt() and 0xFF) == 0xFF && (d[p + 1].toInt() and 0xFF) in 0xD0..0xD7)) p++
                    r.pos = minOf(p + 2, d.size)
                    for (c in all) c.pred = 0
                }
                n++
                for (c in sc) {
                    val dc = dcT[c.td] ?: throw IOException("Tabel DC JPEG hilang")
                    val ac = acT[c.ta] ?: throw IOException("Tabel AC JPEG hilang")
                    val q = qt[c.tq]
                    val bhn = if (inter) c.v else 1
                    val bwn = if (inter) c.h else 1
                    for (by in 0 until bhn) for (bx in 0 until bwn) {
                        java.util.Arrays.fill(coef, 0f)
                        val t = huff(r, dc)
                        c.pred += extend(r.recv(t), t)
                        coef[0] = (c.pred * q[0]).toFloat()
                        var k = 1
                        while (k < 64) {
                            val rs = huff(r, ac)
                            val s = rs and 15
                            val run = rs shr 4
                            if (s == 0) {
                                if (run == 15) { k += 16; continue }
                                break
                            }
                            k += run
                            if (k > 63) break
                            coef[ZZ[k]] = (extend(r.recv(s), s) * q[k]).toFloat()
                            k++
                        }
                        val bx0 = if (inter) mx * c.h + bx else mx
                        val by0 = if (inter) my * c.v + by else my
                        if (bx0 >= c.bw || by0 >= c.bh) continue
                        idct(coef, tmp, c.plane, (by0 * 8) * (c.bw * 8) + bx0 * 8, c.bw * 8)
                    }
                }
            }
            // position after the entropy data: find the next marker
            var p = if (r.hitMarker) r.pos else r.pos
            while (p + 1 < d.size) {
                if ((d[p].toInt() and 0xFF) == 0xFF) {
                    val m = d[p + 1].toInt() and 0xFF
                    if (m != 0 && m != 0xFF && m !in 0xD0..0xD7) break
                }
                p++
            }
            return p
        }

        private fun idct(c: FloatArray, tmp: FloatArray, out: ByteArray, o: Int, stride: Int) {
            for (v in 0 until 8) for (x in 0 until 8) {
                var s = 0f
                for (u in 0 until 8) s += COS[x][u] * c[v * 8 + u]
                tmp[v * 8 + x] = s
            }
            for (x in 0 until 8) for (y in 0 until 8) {
                var s = 0f
                for (v in 0 until 8) s += COS[y][v] * tmp[v * 8 + x]
                val r = Math.round(s) + 128
                out[o + y * stride + x] = (if (r < 0) 0 else if (r > 255) 255 else r).toByte()
            }
        }

        private fun clamp(v: Float): Byte {
            val r = Math.round(v)
            return (if (r < 0) 0 else if (r > 255) 255 else r).toByte()
        }

        private fun output(cs: Array<Comp>, w: Int, h: Int, hmax: Int, vmax: Int, adobe: Int): RgbaImage {
            val out = ByteArray(w * h * 4)
            var o = 0
            val rgb = cs.size == 3 && (adobe == 0 || (adobe < 0 && cs[0].id == 'R'.code && cs[1].id == 'G'.code && cs[2].id == 'B'.code))
            for (y in 0 until h) for (x in 0 until w) {
                val v = IntArray(3)
                for (i in cs.indices) {
                    val c = cs[i]
                    val sx = x * c.h / hmax
                    val sy = y * c.v / vmax
                    v[i] = c.plane[sy * (c.bw * 8) + sx].toInt() and 0xFF
                }
                if (cs.size == 1) {
                    val g = v[0].toByte()
                    out[o] = g; out[o + 1] = g; out[o + 2] = g
                } else if (rgb) {
                    out[o] = v[0].toByte(); out[o + 1] = v[1].toByte(); out[o + 2] = v[2].toByte()
                } else {
                    val yy = v[0].toFloat()
                    val cb = v[1] - 128f
                    val cr = v[2] - 128f
                    out[o] = clamp(yy + 1.402f * cr)
                    out[o + 1] = clamp(yy - 0.344136f * cb - 0.714136f * cr)
                    out[o + 2] = clamp(yy + 1.772f * cb)
                }
                out[o + 3] = -1
                o += 4
            }
            return RgbaImage(w, h, out)
        }
    }
}
