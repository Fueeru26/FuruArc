package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * PNA decoder ('PNAP', multi-frame Pulltop / WillPlus / AdvHD), port of
 * GARbro ArcFormats/Will/ArcPNA.cs (morkt/GARbro).
 *
 * Layout (per the source):
 *  - 0x00 "PNAP"; 0x10 entry count (int32); index starts at 0x14, each entry 0x28 bytes:
 *      +8 offsetX, +0xC offsetY, +0x10 width, +0x14 height, +0x24 data size.
 *  - Frame data follows right after the index (start offset = 0x14 + count*0x28), each frame is a complete
 *      PNG file. Entries of size 0 are skipped (same as GARbro).
 *  - After PNG decoding, "premultiplied by alpha" colors are restored: c = c*255/alpha (alpha other than 0 and 255).
 *
 * Since java.awt/ImageIO does not exist on Android, the PNG decoder is written from scratch (all color types, bit depth 1-16,
 * Adam7 interlace, PLTE/tRNS).
 */
object PnaImage {
    /** A single frame with its position on the canvas (from the PNA index). */
    class Frame(val offsetX: Int, val offsetY: Int, val image: RgbaImage)

    fun isPna(head: ByteArray): Boolean =
        head.size >= 4 && head[0] == 'P'.code.toByte() && head[1] == 'N'.code.toByte() &&
            head[2] == 'A'.code.toByte() && head[3] == 'P'.code.toByte()

    /** Decode the first frame (the first entry with size > 0). */
    fun decode(b: ByteArray): RgbaImage {
        val all = decodeFrames(b, true)
        if (all.isEmpty()) throw IOException("PNA tidak berisi frame")
        return all[0].image
    }

    /** Decode all frames (without offsets). */
    fun decodeAll(b: ByteArray): List<RgbaImage> = decodeFrames(b, false).map { it.image }

    /** Decode all frames with offsets; [firstOnly] stops after the first frame. */
    fun decodeFrames(b: ByteArray, firstOnly: Boolean = false): List<Frame> {
        try {
            if (!isPna(b)) throw IOException("Bukan berkas PNA (tanda 'PNAP' tidak ditemukan)")
            if (b.size < 0x14) throw IOException("Header PNA terpotong")
            val count = i32(b, 0x10)
            if (count <= 0 || count > 0x10000) throw IOException("Jumlah frame PNA tidak masuk akal: $count")
            var idx = 0x14L
            var cur = idx + count.toLong() * 0x28
            if (cur > b.size) throw IOException("Indeks PNA terpotong")
            val res = ArrayList<Frame>()
            for (i in 0 until count) {
                val p = idx.toInt()
                val size = i32(b, p + 0x24).toLong() and 0xFFFFFFFFL
                if (size > 0) {
                    if (cur + size > b.size) throw IOException("Data frame PNA #$i melebihi ukuran berkas")
                    val ox = i32(b, p + 8)
                    val oy = i32(b, p + 0xC)
                    val png = PnaPng.decode(b, cur.toInt(), size.toInt())
                    unPremultiply(png.rgba, png.width * png.height)
                    res.add(Frame(ox, oy, png))
                    cur += size
                    if (firstOnly) break
                }
                idx += 0x28
            }
            return res
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data PNA rusak atau tidak lengkap", e)
        }
    }

    private fun unPremultiply(px: ByteArray, n: Int) {
        var i = 0
        for (k in 0 until n) {
            val a = px[i + 3].toInt() and 0xFF
            if (a != 0 && a != 0xFF) {
                for (c in 0..2) {
                    val v = (px[i + c].toInt() and 0xFF) * 0xFF / a
                    px[i + c] = (if (v > 255) 255 else v).toByte()
                }
            }
            i += 4
        }
    }

    private fun i32(b: ByteArray, o: Int): Int =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
}

/** Minimal but complete PNG decoder, result is 8-bit non-premultiplied RGBA. */
private object PnaPng {
    private val SIG = intArrayOf(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

    private fun be32(b: ByteArray, o: Int): Int =
        ((b[o].toInt() and 0xFF) shl 24) or ((b[o + 1].toInt() and 0xFF) shl 16) or
            ((b[o + 2].toInt() and 0xFF) shl 8) or (b[o + 3].toInt() and 0xFF)

    fun decode(b: ByteArray, off: Int, len: Int): RgbaImage {
        val end = off + len
        if (len < 8 + 25) throw IOException("Frame PNA bukan PNG yang valid")
        for (i in 0 until 8) if ((b[off + i].toInt() and 0xFF) != SIG[i]) throw IOException("Frame PNA bukan PNG (tanda tidak cocok)")
        var p = off + 8
        var w = 0
        var h = 0
        var depth = 0
        var ctype = -1
        var interlace = 0
        var pal: ByteArray? = null
        var trns: ByteArray? = null
        val idat = ByteArrayOutputStream()
        var seenEnd = false
        while (p + 12 <= end && !seenEnd) {
            val n = be32(b, p)
            if (n < 0 || p + 12L + n > end) throw IOException("Chunk PNG terpotong")
            val t = String(b, p + 4, 4, Charsets.ISO_8859_1)
            val d = p + 8
            when (t) {
                "IHDR" -> {
                    if (n < 13) throw IOException("IHDR PNG rusak")
                    w = be32(b, d); h = be32(b, d + 4)
                    depth = b[d + 8].toInt() and 0xFF
                    ctype = b[d + 9].toInt() and 0xFF
                    interlace = b[d + 12].toInt() and 0xFF
                }
                "PLTE" -> pal = b.copyOfRange(d, d + n)
                "tRNS" -> trns = b.copyOfRange(d, d + n)
                "IDAT" -> idat.write(b, d, n)
                "IEND" -> seenEnd = true
            }
            p += 12 + n
        }
        if (ctype < 0) throw IOException("PNG tanpa IHDR")
        if (w <= 0 || h <= 0 || w > 16384 || h > 16384 || w.toLong() * h > 100_000_000L)
            throw IOException("Dimensi PNG dalam PNA tidak valid: ${w}x$h")
        val channels = when (ctype) {
            0 -> 1; 2 -> 3; 3 -> 1; 4 -> 2; 6 -> 4
            else -> throw IOException("Tipe warna PNG $ctype tidak valid")
        }
        val okDepth = when (ctype) {
            0 -> depth == 1 || depth == 2 || depth == 4 || depth == 8 || depth == 16
            3 -> depth == 1 || depth == 2 || depth == 4 || depth == 8
            else -> depth == 8 || depth == 16
        }
        if (!okDepth) throw IOException("Bit depth PNG $depth tidak valid untuk tipe warna $ctype")
        if (ctype == 3 && pal == null) throw IOException("PNG berpalet tanpa PLTE")
        if (interlace != 0 && interlace != 1) throw IOException("Metode interlace PNG tidak dikenal")
        val bitsPP = channels * depth
        val bpp = maxOf(1, bitsPP / 8)

        // compute raw data size
        val passes = if (interlace == 0) listOf(intArrayOf(0, 0, 1, 1)) else listOf(
            intArrayOf(0, 0, 8, 8), intArrayOf(4, 0, 8, 8), intArrayOf(0, 4, 4, 8),
            intArrayOf(2, 0, 4, 4), intArrayOf(0, 2, 2, 4), intArrayOf(1, 0, 2, 2), intArrayOf(0, 1, 1, 2)
        )
        var total = 0L
        for (ps in passes) {
            val pw = (w - ps[0] + ps[2] - 1) / ps[2]
            val ph = (h - ps[1] + ps[3] - 1) / ps[3]
            if (pw > 0 && ph > 0) total += ph.toLong() * (1 + (pw.toLong() * bitsPP + 7) / 8)
        }
        if (total > 700_000_000L) throw IOException("Data PNG terlalu besar")
        val raw = ByteArray(total.toInt())
        val inf = Inflater()
        try {
            inf.setInput(idat.toByteArray())
            var got = 0
            while (got < raw.size) {
                val r = inf.inflate(raw, got, raw.size - got)
                if (r == 0) {
                    if (inf.finished() || inf.needsInput() || inf.needsDictionary()) break
                }
                got += r
            }
            if (got < raw.size) throw IOException("Data piksel PNG dalam PNA terpotong")
        } catch (e: DataFormatException) {
            throw IOException("Data zlib PNG dalam PNA rusak", e)
        } finally {
            inf.end()
        }

        val out = ByteArray(w * h * 4)
        var pos = 0
        for (ps in passes) {
            val pw = (w - ps[0] + ps[2] - 1) / ps[2]
            val ph = (h - ps[1] + ps[3] - 1) / ps[3]
            if (pw <= 0 || ph <= 0) continue
            val stride = ((pw.toLong() * bitsPP + 7) / 8).toInt()
            var prev = ByteArray(stride)
            var line = ByteArray(stride)
            for (y in 0 until ph) {
                val ft = raw[pos++].toInt() and 0xFF
                System.arraycopy(raw, pos, line, 0, stride)
                pos += stride
                unfilter(ft, line, prev, bpp)
                val dy = ps[1] + y * ps[3]
                for (x in 0 until pw) {
                    val dx = ps[0] + x * ps[2]
                    putPixel(out, (dy * w + dx) * 4, line, x, ctype, depth, pal, trns)
                }
                val t = prev; prev = line; line = t
            }
        }
        return RgbaImage(w, h, out)
    }

    private fun unfilter(ft: Int, cur: ByteArray, prev: ByteArray, bpp: Int) {
        val n = cur.size
        when (ft) {
            0 -> {}
            1 -> for (i in bpp until n) cur[i] = (cur[i] + cur[i - bpp]).toByte()
            2 -> for (i in 0 until n) cur[i] = (cur[i] + prev[i]).toByte()
            3 -> for (i in 0 until n) {
                val a = if (i >= bpp) cur[i - bpp].toInt() and 0xFF else 0
                cur[i] = (cur[i] + ((a + (prev[i].toInt() and 0xFF)) shr 1)).toByte()
            }
            4 -> for (i in 0 until n) {
                val a = if (i >= bpp) cur[i - bpp].toInt() and 0xFF else 0
                val bb = prev[i].toInt() and 0xFF
                val c = if (i >= bpp) prev[i - bpp].toInt() and 0xFF else 0
                val pp = a + bb - c
                val pa = Math.abs(pp - a)
                val pb = Math.abs(pp - bb)
                val pc = Math.abs(pp - c)
                val pr = if (pa <= pb && pa <= pc) a else if (pb <= pc) bb else c
                cur[i] = (cur[i] + pr).toByte()
            }
            else -> throw IOException("Filter PNG $ft tidak valid")
        }
    }

    /** Get sample [idx] (0-based, in sample units) with bit depth [depth]; 16-bit returns the full 16 bits. */
    private fun sample(line: ByteArray, idx: Int, depth: Int): Int = when (depth) {
        8 -> line[idx].toInt() and 0xFF
        16 -> ((line[idx * 2].toInt() and 0xFF) shl 8) or (line[idx * 2 + 1].toInt() and 0xFF)
        else -> {
            val bit = idx * depth
            val v = line[bit shr 3].toInt() and 0xFF
            (v shr (8 - depth - (bit and 7))) and ((1 shl depth) - 1)
        }
    }

    private fun scale(v: Int, depth: Int): Int = when (depth) {
        8 -> v
        16 -> v shr 8
        1 -> v * 255
        2 -> v * 85
        else -> v * 17
    }

    private fun putPixel(out: ByteArray, o: Int, line: ByteArray, x: Int, ctype: Int, depth: Int, pal: ByteArray?, trns: ByteArray?) {
        var r: Int
        var g: Int
        var bl: Int
        var a = 255
        when (ctype) {
            0 -> {
                val v = sample(line, x, depth)
                val s = scale(v, depth)
                r = s; g = s; bl = s
                if (trns != null && trns.size >= 2) {
                    val key = ((trns[0].toInt() and 0xFF) shl 8) or (trns[1].toInt() and 0xFF)
                    if (v == key) a = 0
                }
            }
            2 -> {
                val vr = sample(line, x * 3, depth)
                val vg = sample(line, x * 3 + 1, depth)
                val vb = sample(line, x * 3 + 2, depth)
                r = scale(vr, depth); g = scale(vg, depth); bl = scale(vb, depth)
                if (trns != null && trns.size >= 6) {
                    val kr = ((trns[0].toInt() and 0xFF) shl 8) or (trns[1].toInt() and 0xFF)
                    val kg = ((trns[2].toInt() and 0xFF) shl 8) or (trns[3].toInt() and 0xFF)
                    val kb = ((trns[4].toInt() and 0xFF) shl 8) or (trns[5].toInt() and 0xFF)
                    if (vr == kr && vg == kg && vb == kb) a = 0
                }
            }
            3 -> {
                val i = sample(line, x, depth)
                val pl = pal!!
                if (i * 3 + 2 < pl.size) {
                    r = pl[i * 3].toInt() and 0xFF; g = pl[i * 3 + 1].toInt() and 0xFF; bl = pl[i * 3 + 2].toInt() and 0xFF
                } else {
                    r = 0; g = 0; bl = 0
                }
                if (trns != null && i < trns.size) a = trns[i].toInt() and 0xFF
            }
            4 -> {
                val s = scale(sample(line, x * 2, depth), depth)
                r = s; g = s; bl = s
                a = scale(sample(line, x * 2 + 1, depth), depth)
            }
            else -> {
                r = scale(sample(line, x * 4, depth), depth)
                g = scale(sample(line, x * 4 + 1, depth), depth)
                bl = scale(sample(line, x * 4 + 2, depth), depth)
                a = scale(sample(line, x * 4 + 3, depth), depth)
            }
        }
        out[o] = r.toByte()
        out[o + 1] = g.toByte()
        out[o + 2] = bl.toByte()
        out[o + 3] = a.toByte()
    }
}
