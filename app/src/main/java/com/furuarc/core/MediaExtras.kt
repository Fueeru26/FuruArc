package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import java.util.zip.Deflater

/**
 * Media converter extras: OGG loop tags (LOOPSTART/LOOPLENGTH), PNG palette quantization, and audio scanning
 * (peak, RMS, start/end of sound) for volume normalization and silence trimming.
 */

// ============================================================ OGG loop tags ============================================================

object OggTags {
    class Loop(val start: Long, val length: Long)

    private val crcTable: IntArray = IntArray(256).also { t ->
        for (i in 0 until 256) {
            var r = i shl 24
            for (j in 0 until 8) {
                r = if ((r and 0x80000000.toInt()) != 0) (r shl 1) xor 0x04C11DB7 else (r shl 1)
            }
            t[i] = r
        }
    }

    private fun crc(b: ByteArray, off: Int, len: Int): Int {
        var c = 0
        for (i in off until off + len) {
            c = (c shl 8) xor crcTable[((c ushr 24) xor (b[i].toInt() and 0xFF)) and 0xFF]
        }
        return c
    }

    private class Page(val headerLen: Int, val dataLen: Int, val segs: IntArray)

    private fun pageAt(b: ByteArray, pos: Int): Page? {
        if (pos < 0 || pos + 27 > b.size) return null
        if (b[pos] != 0x4F.toByte() || b[pos + 1] != 0x67.toByte() || b[pos + 2] != 0x67.toByte() || b[pos + 3] != 0x53.toByte()) return null
        val nseg = b[pos + 26].toInt() and 0xFF
        if (pos + 27 + nseg > b.size) return null
        val segs = IntArray(nseg) { b[pos + 27 + it].toInt() and 0xFF }
        var dl = 0
        for (s in segs) dl += s
        if (pos + 27 + nseg + dl > b.size) return null
        return Page(27 + nseg, dl, segs)
    }

    /** Assemble packets from consecutive pages, at most [maxPackets] packets. */
    private fun packets(b: ByteArray, maxPackets: Int): List<ByteArray> {
        val out = ArrayList<ByteArray>()
        var cur = ByteArrayOutputStream()
        var pos = 0
        while (out.size < maxPackets) {
            val pg = pageAt(b, pos) ?: break
            var dp = pos + pg.headerLen
            for (s in pg.segs) {
                cur.write(b, dp, s)
                dp += s
                if (s < 255) {
                    out.add(cur.toByteArray())
                    cur = ByteArrayOutputStream()
                    if (out.size >= maxPackets) break
                }
            }
            pos += pg.headerLen + pg.dataLen
        }
        return out
    }

    private fun u32(p: ByteArray, o: Int): Int =
        (p[o].toInt() and 0xFF) or ((p[o + 1].toInt() and 0xFF) shl 8) or ((p[o + 2].toInt() and 0xFF) shl 16) or ((p[o + 3].toInt() and 0xFF) shl 24)

    private fun isOpusTags(p: ByteArray): Boolean = p.size > 8 && String(p, 0, 8, Charsets.US_ASCII) == "OpusTags"

    private fun isVorbisComment(p: ByteArray): Boolean = p.size > 7 && p[0].toInt() == 3 && String(p, 1, 6, Charsets.US_ASCII) == "vorbis"

    private fun commentsOf(p: ByteArray): List<String>? {
        var off = if (isOpusTags(p)) 8 else if (isVorbisComment(p)) 7 else return null
        try {
            val vl = u32(p, off)
            off += 4 + vl
            val n = u32(p, off)
            off += 4
            val list = ArrayList<String>()
            for (i in 0 until n) {
                val l = u32(p, off)
                off += 4
                list.add(String(p, off, l, Charsets.UTF_8))
                off += l
            }
            return list
        } catch (e: Exception) {
            return null
        }
    }

    /** Read LOOPSTART/LOOPLENGTH from an OGG file (Vorbis or Opus); null if absent. */
    fun readLoop(ctx: Context, uri: Uri): Loop? {
        try {
            val bytes: ByteArray = ctx.contentResolver.openInputStream(uri)?.use { ins ->
                val buf = ByteArray(512 * 1024)
                var n = 0
                while (n < buf.size) {
                    val r = ins.read(buf, n, buf.size - n)
                    if (r < 0) break
                    n += r
                }
                buf.copyOf(n)
            } ?: return null
            val pk = packets(bytes, 2)
            if (pk.size < 2) return null
            val c = commentsOf(pk[1]) ?: return null
            var s = -1L
            var l = -1L
            for (x in c) {
                val i = x.indexOf('=')
                if (i <= 0) continue
                val k = x.substring(0, i).uppercase()
                val v = x.substring(i + 1).trim().toLongOrNull() ?: continue
                if (k == "LOOPSTART") s = v else if (k == "LOOPLENGTH") l = v
            }
            return if (s >= 0L && l > 0L) Loop(s, l) else null
        } catch (e: Exception) {
            return null
        }
    }

    /** Write LOOPSTART/LOOPLENGTH into the OpusTags page of the resulting Opus file. Silently aborts if the structure is not as expected. */
    fun writeLoop(f: File, start: Long, length: Long) {
        try {
            val b = f.readBytes()
            val p0 = pageAt(b, 0) ?: return
            val p1pos = p0.headerLen + p0.dataLen
            val p1 = pageAt(b, p1pos) ?: return
            if (p1.segs.isEmpty() || p1.segs[p1.segs.size - 1] == 255) return
            for (i in 0 until p1.segs.size - 1) if (p1.segs[i] < 255) return // the page contains more than one packet
            val dataStart = p1pos + p1.headerLen
            val pkt = b.copyOfRange(dataStart, dataStart + p1.dataLen)
            if (!isOpusTags(pkt)) return
            val old = commentsOf(pkt) ?: return
            val vl = u32(pkt, 8)
            val vendor = pkt.copyOfRange(12, 12 + vl)
            val comments = ArrayList<String>()
            for (x in old) {
                val k = x.substringBefore('=').uppercase()
                if (k != "LOOPSTART" && k != "LOOPLENGTH") comments.add(x)
            }
            comments.add("LOOPSTART=$start")
            comments.add("LOOPLENGTH=$length")

            val np = ByteArrayOutputStream()
            fun w32(v: Int) {
                np.write(v and 0xFF); np.write((v shr 8) and 0xFF); np.write((v shr 16) and 0xFF); np.write((v shr 24) and 0xFF)
            }
            np.write("OpusTags".toByteArray(Charsets.US_ASCII))
            w32(vendor.size)
            np.write(vendor)
            w32(comments.size)
            for (c in comments) {
                val cb = c.toByteArray(Charsets.UTF_8)
                w32(cb.size)
                np.write(cb)
            }
            val data = np.toByteArray()
            val nseg = data.size / 255 + 1
            if (nseg > 255) return
            val page = ByteArrayOutputStream()
            page.write(b, p1pos, 26) // OggS, version, flags, granule, serial, sequence number, crc (crc is recomputed)
            page.write(nseg)
            for (i in 0 until nseg - 1) page.write(255)
            page.write(data.size % 255)
            page.write(data)
            val pb = page.toByteArray()
            for (i in 22 until 26) pb[i] = 0
            val cc = crc(pb, 0, pb.size)
            pb[22] = (cc and 0xFF).toByte()
            pb[23] = ((cc shr 8) and 0xFF).toByte()
            pb[24] = ((cc shr 16) and 0xFF).toByte()
            pb[25] = ((cc shr 24) and 0xFF).toByte()

            val out = ByteArrayOutputStream()
            out.write(b, 0, p1pos)
            out.write(pb)
            val rest = p1pos + p1.headerLen + p1.dataLen
            out.write(b, rest, b.size - rest)
            f.writeBytes(out.toByteArray())
        } catch (e: Exception) {
            // leave the file as is
        }
    }
}

// ============================================================ Paletted PNG ============================================================

/**
 * Color quantization (median cut, optional Floyd-Steinberg dithering) then writes a paletted PNG (PLTE + tRNS) with the
 * smallest possible bit depth. Too many colors are grouped first; null from [create] only for huge images (>12 million pixels).
 */
class PngPal private constructor(
    private val w: Int,
    private val h: Int,
    private val px: IntArray,
    private val uniq: IntArray,
    private val cnt: IntArray,
    private val pixU: IntArray,
) {
    private val n = uniq.size

    private class Built(val uniq: IntArray, val cnt: IntArray, val pixU: IntArray)

    companion object {
        private const val MAX_UNIQUE = 200_000
        private const val MAX_PIXELS = 12_000_000

        // perceptual weight per channel (alpha, R, G, B) for color distance
        private const val WA = 4
        private const val WR = 3
        private const val WG = 4
        private const val WB = 2

        fun create(bmp: Bitmap): PngPal? {
            val w = bmp.width
            val h = bmp.height
            if (w.toLong() * h > MAX_PIXELS) return null
            return try {
                val px = IntArray(w * h)
                bmp.getPixels(px, 0, w, 0, 0, w, h)
                createFromPixels(px, w, h)
            } catch (e: OutOfMemoryError) {
                null
            }
        }

        /** Unique colors are grouped when there are too many (gradient photos/illustrations), so it no longer falls back to coarse posterization. */
        fun createFromPixels(px: IntArray, w: Int, h: Int): PngPal? {
            for (i in px.indices) if ((px[i] ushr 24) == 0) px[i] = 0
            val masks = intArrayOf(-1, 0xFFFCFCFC.toInt(), 0xFFF8F8F8.toInt(), 0xFFF0F0F0.toInt(), 0xF0F0F0F0.toInt())
            for (m in masks) {
                val b = build(px, m) ?: continue
                return PngPal(w, h, px, b.uniq, b.cnt, b.pixU)
            }
            return null
        }

        private fun build(px: IntArray, mask: Int): Built? {
            val map = HashMap<Int, Int>()
            var cn = IntArray(256)
            var sa = LongArray(256)
            var sr = LongArray(256)
            var sg = LongArray(256)
            var sb = LongArray(256)
            var n = 0
            val pixU = IntArray(px.size)
            for (i in px.indices) {
                val c = px[i]
                val key = if (c == 0) 0 else c and mask
                var u = map[key]
                if (u == null) {
                    if (n >= MAX_UNIQUE) return null
                    if (n == cn.size) {
                        val nn = n * 2
                        cn = cn.copyOf(nn); sa = sa.copyOf(nn); sr = sr.copyOf(nn); sg = sg.copyOf(nn); sb = sb.copyOf(nn)
                    }
                    u = n
                    map[key] = n
                    n++
                }
                cn[u]++
                sa[u] += (c ushr 24).toLong()
                sr[u] += ((c shr 16) and 255).toLong()
                sg[u] += ((c shr 8) and 255).toLong()
                sb[u] += (c and 255).toLong()
                pixU[i] = u
            }
            val uq = IntArray(n)
            for (u in 0 until n) {
                val t = cn[u].toLong()
                val a = ((sa[u] + t / 2) / t).toInt()
                uq[u] = if (a == 0) 0 else (a shl 24) or (((sr[u] + t / 2) / t).toInt() shl 16) or (((sg[u] + t / 2) / t).toInt() shl 8) or ((sb[u] + t / 2) / t).toInt()
            }
            return Built(uq, cn.copyOf(n), pixU)
        }

        private fun ch(c: Int, k: Int): Int = when (k) { 0 -> c ushr 24; 1 -> (c shr 16) and 255; 2 -> (c shr 8) and 255; else -> c and 255 }
    }

    fun encode(maxColors: Int, dither: Boolean): ByteArray {
        val k = maxColors.coerceIn(2, 256)
        val pal: IntArray = if (n <= k) uniq.copyOf() else refine(medianCut(k))
        val idx = ByteArray(w * h)
        if (dither && pal.size > 1 && n > pal.size) {
            ditherMap(pal, idx)
        } else {
            val m = IntArray(n) { nearest(pal, uniq[it]) }
            for (i in idx.indices) idx[i] = m[pixU[i]].toByte()
        }
        return writePng(pal, idx)
    }

    private fun nearest(pal: IntArray, c: Int): Int {
        val a = c ushr 24
        val r = (c shr 16) and 255
        val g = (c shr 8) and 255
        val b = c and 255
        var best = 0
        var bd = Int.MAX_VALUE
        for (i in pal.indices) {
            val p = pal[i]
            val da = (p ushr 24) - a
            val dr = ((p shr 16) and 255) - r
            val dg = ((p shr 8) and 255) - g
            val db = (p and 255) - b
            val d = da * da * WA + dr * dr * WR + dg * dg * WG + db * db * WB
            if (d < bd) {
                bd = d
                best = i
                if (d == 0) break
            }
        }
        return best
    }

    private class Box(var s: Int, var e: Int) {
        var sse = 0.0
        var chn = 1
    }

    private fun medianCut(k: Int): IntArray {
        val order = IntArray(n) { it }
        val boxes = ArrayList<Box>()
        val wts = doubleArrayOf(WA.toDouble(), WR.toDouble(), WG.toDouble(), WB.toDouble())
        fun stats(b: Box) {
            val s = DoubleArray(4)
            val ss = DoubleArray(4)
            var tot = 0.0
            for (i in b.s until b.e) {
                val u = order[i]
                val wg = cnt[u].toDouble()
                val c = uniq[u]
                tot += wg
                for (t in 0 until 4) {
                    val v = ch(c, t).toDouble()
                    s[t] += v * wg
                    ss[t] += v * v * wg
                }
            }
            var best = -1.0
            var sum = 0.0
            for (t in 0 until 4) {
                val wv = (ss[t] - s[t] * s[t] / tot) * wts[t]
                sum += wv
                if (wv > best) {
                    best = wv
                    b.chn = t
                }
            }
            b.sse = sum
        }
        val first = Box(0, n)
        stats(first)
        boxes.add(first)
        while (boxes.size < k) {
            var bi = -1
            var best = 0.0
            for (i in boxes.indices) {
                val b = boxes[i]
                if (b.e - b.s >= 2 && b.sse > best) {
                    best = b.sse
                    bi = i
                }
            }
            if (bi < 0) break
            val b = boxes[bi]
            val len = b.e - b.s
            val keys = LongArray(len)
            for (i in 0 until len) {
                val u = order[b.s + i]
                keys[i] = (ch(uniq[u], b.chn).toLong() shl 32) or u.toLong()
            }
            java.util.Arrays.sort(keys)
            var total = 0L
            for (i in 0 until len) {
                val u = (keys[i] and 0xFFFFFFFFL).toInt()
                order[b.s + i] = u
                total += cnt[u]
            }
            var acc = 0L
            var mid = b.s + 1
            for (i in b.s until b.e - 1) {
                acc += cnt[order[i]]
                mid = i + 1
                if (acc * 2 >= total) break
            }
            val nb = Box(mid, b.e)
            b.e = mid
            stats(b)
            stats(nb)
            boxes.add(nb)
        }
        val pal = IntArray(boxes.size)
        for ((bi, b) in boxes.withIndex()) {
            var sa = 0L
            var sr = 0L
            var sg = 0L
            var sb = 0L
            var tot = 0L
            for (i in b.s until b.e) {
                val u = order[i]
                val c = uniq[u]
                val wgt = cnt[u].toLong()
                sa += (c ushr 24) * wgt
                sr += ((c shr 16) and 255) * wgt
                sg += ((c shr 8) and 255) * wgt
                sb += (c and 255) * wgt
                tot += wgt
            }
            if (tot == 0L) tot = 1L
            val a = (sa / tot).toInt()
            pal[bi] = if (a == 0) 0 else (a shl 24) or ((sr / tot).toInt() shl 16) or ((sg / tot).toInt() shl 8) or (sb / tot).toInt()
        }
        return pal
    }

    /** Palette refinement (weighted k-means): moves each palette color to the average of the pixels using it. */
    private fun refine(pal0: IntArray): IntArray {
        val k = pal0.size
        val budget = n.toLong() * k
        val iters = if (budget <= 6_000_000L) 6 else if (budget <= 30_000_000L) 3 else 1
        var pal = pal0
        val sa = LongArray(k)
        val sr = LongArray(k)
        val sg = LongArray(k)
        val sb = LongArray(k)
        val tt = LongArray(k)
        for (it in 0 until iters) {
            java.util.Arrays.fill(sa, 0L); java.util.Arrays.fill(sr, 0L); java.util.Arrays.fill(sg, 0L)
            java.util.Arrays.fill(sb, 0L); java.util.Arrays.fill(tt, 0L)
            for (u in 0 until n) {
                val c = uniq[u]
                val j = nearest(pal, c)
                val wg = cnt[u].toLong()
                sa[j] += (c ushr 24) * wg
                sr[j] += ((c shr 16) and 255) * wg
                sg[j] += ((c shr 8) and 255) * wg
                sb[j] += (c and 255) * wg
                tt[j] += wg
            }
            val np = pal.copyOf()
            var moved = false
            for (j in 0 until k) {
                if (tt[j] == 0L) continue
                val a = ((sa[j] + tt[j] / 2) / tt[j]).toInt()
                val c = if (a == 0) 0 else (a shl 24) or (((sr[j] + tt[j] / 2) / tt[j]).toInt() shl 16) or (((sg[j] + tt[j] / 2) / tt[j]).toInt() shl 8) or ((sb[j] + tt[j] / 2) / tt[j]).toInt()
                if (c != np[j]) { np[j] = c; moved = true }
            }
            pal = np
            if (!moved) break
        }
        return pal
    }

    /** Serpentine Floyd-Steinberg for all pixels (no size limit), color lookup via a 6-bit table for speed. */
    private fun ditherMap(pal: IntArray, idx: ByteArray) {
        var cur = FloatArray(3 * (w + 2))
        var nxt = FloatArray(3 * (w + 2))
        val transIdx = nearest(pal, 0)
        var opaque = 0
        for (p in pal) if ((p ushr 24) == 255) opaque++
        val op = IntArray(opaque)
        run {
            var j = 0
            for (i in pal.indices) if ((pal[i] ushr 24) == 255) op[j++] = i
        }
        val lut = IntArray(1 shl 18) { -1 }
        fun lookup(r: Int, g: Int, b: Int): Int {
            val key = ((r shr 2) shl 12) or ((g shr 2) shl 6) or (b shr 2)
            var v = lut[key]
            if (v < 0) {
                val cr = (r shr 2) * 4 + 2
                val cg = (g shr 2) * 4 + 2
                val cb = (b shr 2) * 4 + 2
                var bd = Int.MAX_VALUE
                v = op[0]
                for (i in op) {
                    val p = pal[i]
                    val dr = ((p shr 16) and 255) - cr
                    val dg = ((p shr 8) and 255) - cg
                    val db = (p and 255) - cb
                    val d = dr * dr * WR + dg * dg * WG + db * db * WB
                    if (d < bd) { bd = d; v = i }
                }
                lut[key] = v
            }
            return v
        }
        val strength = 0.9f
        val lim = 64f
        for (y in 0 until h) {
            java.util.Arrays.fill(nxt, 0f)
            val ltr = (y and 1) == 0
            val dir = if (ltr) 1 else -1
            for (step in 0 until w) {
                val x = if (ltr) step else w - 1 - step
                val c = px[y * w + x]
                val a = c ushr 24
                if (a == 0) {
                    idx[y * w + x] = transIdx.toByte()
                    continue
                }
                val o = (x + 1) * 3
                val r = (((c shr 16) and 255) + cur[o]).coerceIn(0f, 255f)
                val g = (((c shr 8) and 255) + cur[o + 1]).coerceIn(0f, 255f)
                val b = ((c and 255) + cur[o + 2]).coerceIn(0f, 255f)
                val ri = Math.round(r)
                val gi = Math.round(g)
                val bi = Math.round(b)
                val pi = if (a == 255 && opaque > 0) lookup(ri, gi, bi) else nearest(pal, (a shl 24) or (ri shl 16) or (gi shl 8) or bi)
                idx[y * w + x] = pi.toByte()
                val pc = pal[pi]
                for (t in 0 until 3) {
                    val pv = when (t) { 0 -> (pc shr 16) and 255; 1 -> (pc shr 8) and 255; else -> pc and 255 }
                    val cv = when (t) { 0 -> r; 1 -> g; else -> b }
                    val e = ((cv - pv) * strength).coerceIn(-lim, lim)
                    cur[o + 3 * dir + t] += e * 7f / 16f
                    nxt[o - 3 * dir + t] += e * 3f / 16f
                    nxt[o + t] += e * 5f / 16f
                    nxt[o + 3 * dir + t] += e * 1f / 16f
                }
            }
            val tmp = cur
            cur = nxt
            nxt = tmp
        }
    }

    private fun writePng(pal: IntArray, idx: ByteArray): ByteArray {
        val bits = if (pal.size <= 2) 1 else if (pal.size <= 4) 2 else if (pal.size <= 16) 4 else 8
        val rowBytes = (w * bits + 7) / 8
        val raw = ByteArray((rowBytes + 1) * h)
        for (y in 0 until h) {
            val ro = y * (rowBytes + 1)
            raw[ro] = 0
            if (bits == 8) {
                System.arraycopy(idx, y * w, raw, ro + 1, w)
            } else {
                for (x in 0 until w) {
                    val v = idx[y * w + x].toInt() and 0xFF
                    val bitPos = x * bits
                    val shift = 8 - bits - (bitPos and 7)
                    val bi = ro + 1 + (bitPos shr 3)
                    raw[bi] = (raw[bi].toInt() or (v shl shift)).toByte()
                }
            }
        }
        val out = ByteArrayOutputStream()
        out.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        fun chunk(type: String, data: ByteArray) {
            val len = data.size
            out.write(byteArrayOf((len ushr 24).toByte(), (len ushr 16).toByte(), (len ushr 8).toByte(), len.toByte()))
            val tb = type.toByteArray(Charsets.US_ASCII)
            out.write(tb)
            out.write(data)
            val crc = CRC32()
            crc.update(tb)
            crc.update(data)
            val cv = crc.value.toInt()
            out.write(byteArrayOf((cv ushr 24).toByte(), (cv ushr 16).toByte(), (cv ushr 8).toByte(), cv.toByte()))
        }
        val ihdr = ByteBuffer.allocate(13)
        ihdr.putInt(w)
        ihdr.putInt(h)
        ihdr.put(bits.toByte())
        ihdr.put(3) // palette
        ihdr.put(0)
        ihdr.put(0)
        ihdr.put(0)
        chunk("IHDR", ihdr.array())
        val plte = ByteArray(pal.size * 3)
        for (i in pal.indices) {
            plte[i * 3] = ((pal[i] shr 16) and 255).toByte()
            plte[i * 3 + 1] = ((pal[i] shr 8) and 255).toByte()
            plte[i * 3 + 2] = (pal[i] and 255).toByte()
        }
        chunk("PLTE", plte)
        var lastNon = -1
        for (i in pal.indices) if ((pal[i] ushr 24) != 255) lastNon = i
        if (lastNon >= 0) {
            val trns = ByteArray(lastNon + 1) { (pal[it] ushr 24).toByte() }
            chunk("tRNS", trns)
        }
        val def = Deflater(9)
        def.setInput(raw)
        def.finish()
        val z = ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        while (!def.finished()) {
            val k = def.deflate(buf)
            z.write(buf, 0, k)
        }
        def.end()
        chunk("IDAT", z.toByteArray())
        chunk("IEND", ByteArray(0))
        return out.toByteArray()
    }
}

// ============================================================ Audio scanning ============================================================

/** [peak] 0..32768, [rms] 0..32768, [first]/[last] = index of the first/last frame that passes the threshold (-1 if completely silent). */
class AudioScan(val rate: Int, val peak: Int, val rms: Double, val first: Long, val last: Long, val frames: Long)

object AudioScanner {
    private fun fi(f: MediaFormat, k: String, d: Int): Int = if (f.containsKey(k)) f.getInteger(k) else d

    private fun fl(f: MediaFormat, k: String, d: Long): Long = if (f.containsKey(k)) f.getLong(k) else d

    /** Decode the whole first audio track and compute its statistics; [thr] = amplitude threshold (0..32767) for "has sound". */
    fun scan(ctl: MediaCtl, src: Uri, thr: Int): AudioScan? {
        val ex = MediaExtractor()
        var dec: MediaCodec? = null
        try {
            ex.setDataSource(ctl.ctx, src, null)
            var track = -1
            for (i in 0 until ex.trackCount) {
                val m = ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
                if (m.startsWith("audio/")) { track = i; break }
            }
            if (track < 0) return null
            ex.selectTrack(track)
            val fmt = ex.getTrackFormat(track)
            val mime = fmt.getString(MediaFormat.KEY_MIME) ?: return null
            val durUs = fl(fmt, MediaFormat.KEY_DURATION, 0L)
            var rate = fi(fmt, MediaFormat.KEY_SAMPLE_RATE, 44100)
            var ch = fi(fmt, MediaFormat.KEY_CHANNEL_COUNT, 2)
            var pcmEnc = 2
            val d = MediaCodec.createDecoderByType(mime)
            dec = d
            d.configure(fmt, null, null, 0)
            d.start()

            var peak = 0
            var sumSq = 0.0
            var samples = 0L
            var frames = 0L
            var first = -1L
            var last = -1L
            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var shorts = ShortArray(0)
            while (!outputDone) {
                ctl.check()
                if (!inputDone) {
                    val ii = d.dequeueInputBuffer(10_000L)
                    if (ii >= 0) {
                        val ib = d.getInputBuffer(ii) ?: return null
                        val n = ex.readSampleData(ib, 0)
                        if (n < 0) {
                            d.queueInputBuffer(ii, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            d.queueInputBuffer(ii, 0, n, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }
                val oi = d.dequeueOutputBuffer(info, 10_000L)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val of = d.outputFormat
                    rate = fi(of, MediaFormat.KEY_SAMPLE_RATE, rate)
                    ch = fi(of, MediaFormat.KEY_CHANNEL_COUNT, ch)
                    pcmEnc = fi(of, MediaFormat.KEY_PCM_ENCODING, 2)
                } else if (oi >= 0) {
                    if (info.size > 0) {
                        val ob = d.getOutputBuffer(oi)
                        if (ob != null) {
                            ob.position(info.offset)
                            ob.limit(info.offset + info.size)
                            ob.order(ByteOrder.LITTLE_ENDIAN)
                            val count: Int
                            when (pcmEnc) {
                                4 -> {
                                    count = info.size / 4
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    val fb = ob.asFloatBuffer()
                                    for (i in 0 until count) {
                                        val v = fb.get(i) * 32767f
                                        shorts[i] = (if (v > 32767f) 32767f else if (v < -32768f) -32768f else v).toInt().toShort()
                                    }
                                }
                                3 -> {
                                    count = info.size
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    for (i in 0 until count) shorts[i] = (((ob.get(ob.position() + i).toInt() and 0xFF) - 128) shl 8).toShort()
                                }
                                else -> {
                                    count = info.size / 2
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    ob.asShortBuffer().get(shorts, 0, count)
                                }
                            }
                            val c = if (ch < 1) 1 else ch
                            val nf = count / c
                            for (f in 0 until nf) {
                                var m = 0
                                for (k in 0 until c) {
                                    val s = shorts[f * c + k].toInt()
                                    val a = if (s < 0) -s else s
                                    if (a > m) m = a
                                    sumSq += s.toDouble() * s
                                }
                                samples += c
                                if (m > peak) peak = m
                                if (m > thr) {
                                    if (first < 0L) first = frames
                                    last = frames
                                }
                                frames++
                            }
                        }
                        if (durUs > 0L) ctl.progress((info.presentationTimeUs.toFloat() / durUs).coerceIn(0f, 1f))
                    }
                    d.releaseOutputBuffer(oi, false)
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                }
            }
            val rms = if (samples > 0L) Math.sqrt(sumSq / samples) else 0.0
            return AudioScan(rate, peak, rms, first, last, frames)
        } finally {
            try { dec?.stop() } catch (e: Exception) { }
            try { dec?.release() } catch (e: Exception) { }
            try { ex.release() } catch (e: Exception) { }
        }
    }

    /** Normalization gain: [level] 1 = RMS -20 dBFS, 2 = -16 dBFS, 3 = -12 dBFS; limited so the peak is <= -1 dBFS. */
    fun normGain(sc: AudioScan?, level: Int): Float {
        if (sc == null || level <= 0 || sc.peak <= 0 || sc.rms < 1.0) return 1f
        val targetDb = when (level) { 1 -> -20.0; 2 -> -16.0; else -> -12.0 }
        val want = Math.pow(10.0, targetDb / 20.0) * 32768.0 / sc.rms
        val limit = Math.pow(10.0, -1.0 / 20.0) * 32768.0 / sc.peak
        return minOf(want, limit).coerceIn(0.1, 8.0).toFloat()
    }
}
