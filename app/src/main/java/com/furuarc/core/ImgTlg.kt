package com.furuarc.core

import java.io.IOException

/**
 * Kirikiri TLG image decoder (TLG5.0 and TLG6.0, plus the TLG0.0 "sds" wrapper), pure JVM.
 * Port of vn-tools/tlg2png (Tlg5Reader / Tlg6Reader / Tlg0Reader) and GARbro
 * ArcFormats/KiriKiri/ImageTLG.cs (ReadV5, ReadV6, header detection).
 *
 * Variants covered:
 *  - "TLG0.0\0sds\x1a" : SDS container; 4-byte data size then the TLG5/TLG6 image at offset 0x0F. The trailing "tags" chunk is ignored.
 *  - "TLG5.0\0raw\x1a" : 3/4 channels, LZSS blocks (or raw if mark != 0), per-pixel prediction + top row.
 *  - "TLG6.0\0raw\x1a" : 1/3/4 channels, Golomb + MED/AVG + color correlation filter (32 types); entropy method 0 (Golomb) only.
 *  - "idx" header (`\0idx\x1a`), first byte 0xAB, and scrambled headers "XXXYYY", "XXXZZZ", "JKMXE8" (as in GARbro).
 *
 * Limitations: TLGmux/TLGqoi/TLGref (GARbro extensions), diff images based on the "tags" chunk (need a base image),
 * and external Golomb tables are not supported. 1-channel TLG6 is output as grayscale (R=G=B).
 */
object TlgImage {
    private const val MAX_DIM = 16384
    private const val MAX_PIXELS = 100_000_000L
    private const val SDS_OFFSET = 15

    private class Hdr(val version: Int, val colors: Int, val width: Int, val height: Int, val dataOffset: Int)

    private val SDS_MAGIC = byteArrayOf(0x54, 0x4c, 0x47, 0x30, 0x2e, 0x30, 0x00, 0x73, 0x64, 0x73, 0x1a)

    fun isTlg(head: ByteArray): Boolean {
        if (startsWith(head, 0, SDS_MAGIC)) return true
        return nameAt(head, 0) != null
    }

    fun decode(b: ByteArray): RgbaImage {
        try {
            val off = if (startsWith(b, 0, SDS_MAGIC)) SDS_OFFSET else 0
            val h = readHeader(b, off)
            return when (h.version) {
                5 -> decodeV5(b, h)
                6 -> decodeV6(b, h)
                else -> throw IOException("Varian TLG ini belum didukung (hanya TLG5.0 dan TLG6.0)")
            }
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data TLG rusak atau tidak lengkap", e)
        }
    }

    // ---------------------------------------------------------------- header

    private fun startsWith(b: ByteArray, off: Int, m: ByteArray): Boolean {
        if (b.size < off + m.size) return false
        for (i in m.indices) if (b[off + i] != m[i]) return false
        return true
    }

    private fun ascii(b: ByteArray, off: Int, s: String): Boolean {
        if (b.size < off + s.length) return false
        for (i in s.indices) if ((b[off + i].toInt() and 0xFF) != s[i].code) return false
        return true
    }

    /** Magic name at [off] ("TLG6.0", "TLG5.0", ..., "XXXYYY") or null; requirement: followed by "\0raw\x1a" or "\0idx\x1a". */
    private fun nameAt(b: ByteArray, off: Int): String? {
        if (b.size < off + 11) return null
        val tail = "\u0000raw\u001a"
        val tail2 = "\u0000idx\u001a"
        if (!ascii(b, off + 6, tail) && !ascii(b, off + 6, tail2)) return null
        val first = b[off].toInt() and 0xFF
        val pre = StringBuilder()
        pre.append(if (first == 0xAB) 'T' else first.toChar())
        for (i in 1 until 6) pre.append((b[off + i].toInt() and 0xFF).toChar())
        val s = pre.toString()
        return when (s) {
            "TLG6.0", "TLG5.0", "TLGmux", "TLGqoi", "TLGref", "XXXYYY", "XXXZZZ", "JKMXE8" -> s
            else -> null
        }
    }

    private fun u32(b: ByteArray, o: Int): Int {
        if (o < 0 || o + 4 > b.size) throw IOException("Data TLG terpotong")
        return (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
    }

    private fun readHeader(b: ByteArray, off: Int): Hdr {
        val name = nameAt(b, off) ?: throw IOException("Bukan berkas TLG yang dikenal")
        val h = b.copyOf(minOf(b.size, off + 0x26))
        val version: Int
        when (name) {
            "TLG6.0" -> version = 6
            "TLG5.0" -> version = 5
            "XXXYYY" -> { version = 5; xor(h, off + 0x0C, 0xAB); xor(h, off + 0x10, 0xAC) }
            "XXXZZZ" -> { version = 6; xor(h, off + 0x0F, 0xAB); xor(h, off + 0x13, 0xAC) }
            "JKMXE8" -> { version = 5; xor(h, off + 0x0C, 0x1A); xor(h, off + 0x10, 0x1C) }
            else -> throw IOException("Varian TLG '$name' belum didukung (hanya TLG5.0 dan TLG6.0)")
        }
        if (h.size < off + 12) throw IOException("Header TLG terpotong")
        val colors = h[off + 11].toInt() and 0xFF
        val base: Int
        if (version == 6) {
            if (colors != 1 && colors != 3 && colors != 4) throw IOException("Jumlah kanal TLG6 tidak didukung: $colors")
            if (h.size < off + 15) throw IOException("Header TLG terpotong")
            if (h[off + 12].toInt() != 0 || h[off + 13].toInt() != 0 || h[off + 14].toInt() != 0) {
                throw IOException("TLG6 dengan tabel Golomb eksternal / flag khusus belum didukung")
            }
            base = off + 15
        } else {
            if (colors != 3 && colors != 4) throw IOException("Jumlah kanal TLG5 tidak didukung: $colors")
            base = off + 12
        }
        val w = u32(h, base)
        val ht = u32(h, base + 4)
        if (w <= 0 || ht <= 0 || w > MAX_DIM || ht > MAX_DIM || w.toLong() * ht > MAX_PIXELS) {
            throw IOException("Dimensi gambar TLG tidak wajar: ${w}x$ht")
        }
        return Hdr(version, colors, w, ht, base + 8)
    }

    private fun xor(h: ByteArray, i: Int, k: Int) {
        if (i < h.size) h[i] = (h[i].toInt() xor k).toByte()
    }

    // ---------------------------------------------------------------- LZSS (TLG5 / filter TLG6)

    /** TLG5-style LZSS decompression; [text] holds the 4096-byte window, [r0] is the initial write position; returns the final r. */
    private fun lzss(out: ByteArray, inb: ByteArray, inSize: Int, text: ByteArray, r0: Int): Int {
        var r = r0
        var flags = 0
        var o = 0
        var i = 0
        while (i < inSize) {
            flags = flags ushr 1
            if ((flags and 0x100) == 0) {
                flags = (inb[i++].toInt() and 0xFF) or 0xFF00
            }
            if ((flags and 1) != 0) {
                var mpos = (inb[i].toInt() and 0xFF) or ((inb[i + 1].toInt() and 0x0F) shl 8)
                var mlen = ((inb[i + 1].toInt() and 0xF0) shr 4) + 3
                i += 2
                if (mlen == 18) mlen += inb[i++].toInt() and 0xFF
                while (mlen-- > 0) {
                    val c = text[mpos]
                    if (o < out.size) out[o++] = c
                    text[r] = c
                    r = (r + 1) and 4095
                    mpos = (mpos + 1) and 4095
                }
            } else {
                val c = inb[i++]
                if (o < out.size) out[o++] = c
                text[r] = c
                r = (r + 1) and 4095
            }
        }
        return r
    }

    // ---------------------------------------------------------------- TLG5

    private fun decodeV5(b: ByteArray, h: Hdr): RgbaImage {
        val w = h.width
        val ht = h.height
        val colors = h.colors
        val blockH = u32(b, h.dataOffset)
        if (blockH <= 0) throw IOException("Tinggi blok TLG5 tidak valid")
        val blockCount = (ht - 1) / blockH + 1
        var pos = h.dataOffset + 4 + blockCount * 4 // skip the block size table
        if (pos > b.size) throw IOException("Data TLG terpotong")
        val rows = minOf(blockH, ht)
        val bufLen = w * rows
        val out = ByteArray(w * ht * 4)
        val text = ByteArray(4096)
        var r = 0
        val chan = Array(colors) { ByteArray(bufLen) }
        var inbuf = ByteArray(0)
        var y0 = 0
        while (y0 < ht) {
            for (c in 0 until colors) {
                if (pos + 5 > b.size) throw IOException("Data TLG terpotong")
                val mark = b[pos].toInt() and 0xFF
                val size = u32(b, pos + 1)
                pos += 5
                if (size < 0 || size > b.size - pos) throw IOException("Data TLG terpotong")
                if (mark == 0) {
                    if (inbuf.size < size) inbuf = ByteArray(size)
                    System.arraycopy(b, pos, inbuf, 0, size)
                    r = lzss(chan[c], inbuf, size, text, r)
                } else {
                    System.arraycopy(b, pos, chan[c], 0, minOf(size, bufLen))
                }
                pos += size
            }
            val yLim = minOf(y0 + blockH, ht)
            var bp = 0
            for (y in y0 until yLim) {
                val cur = y * w * 4
                val up = cur - w * 4
                var pr = 0; var pg = 0; var pb = 0; var pa = 0
                var o = cur
                var u = up
                for (x in 0 until w) {
                    val g = chan[1][bp + x].toInt() and 0xFF
                    val bb = ((chan[0][bp + x].toInt() and 0xFF) + g) and 0xFF
                    val rr = ((chan[2][bp + x].toInt() and 0xFF) + g) and 0xFF
                    pb = (pb + bb) and 0xFF
                    pg = (pg + g) and 0xFF
                    pr = (pr + rr) and 0xFF
                    var oa = 0xFF
                    if (colors == 4) {
                        pa = (pa + (chan[3][bp + x].toInt() and 0xFF)) and 0xFF
                        oa = pa
                    }
                    if (y > 0) {
                        out[o] = ((pr + (out[u].toInt() and 0xFF)) and 0xFF).toByte()
                        out[o + 1] = ((pg + (out[u + 1].toInt() and 0xFF)) and 0xFF).toByte()
                        out[o + 2] = ((pb + (out[u + 2].toInt() and 0xFF)) and 0xFF).toByte()
                        if (colors == 4) oa = (pa + (out[u + 3].toInt() and 0xFF)) and 0xFF
                    } else {
                        out[o] = pr.toByte()
                        out[o + 1] = pg.toByte()
                        out[o + 2] = pb.toByte()
                    }
                    out[o + 3] = oa.toByte()
                    o += 4
                    u += 4
                }
                bp += w
            }
            y0 += blockH
        }
        return RgbaImage(w, ht, out)
    }

    // ---------------------------------------------------------------- TLG6

    private const val W_BLOCK = 8
    private const val H_BLOCK = 8
    private const val GOLOMB_N = 4
    private const val LZ_BITS = 12
    private const val LZ_SIZE = 1 shl LZ_BITS

    private val LEADING_ZERO = ByteArray(LZ_SIZE).also { t ->
        for (i in 0 until LZ_SIZE) {
            var cnt = 0
            var j = 1
            while (j != LZ_SIZE && (i and j) == 0) { j = j shl 1; cnt++ }
            cnt++
            if (j == LZ_SIZE) cnt = 0
            t[i] = cnt.toByte()
        }
    }

    private val GOLOMB_BITS: Array<ByteArray> = run {
        val comp = arrayOf(
            shortArrayOf(3, 7, 15, 27, 63, 108, 223, 448, 130),
            shortArrayOf(3, 5, 13, 24, 51, 95, 192, 384, 257),
            shortArrayOf(2, 5, 12, 21, 39, 86, 155, 320, 384),
            shortArrayOf(2, 3, 9, 18, 33, 61, 129, 258, 511)
        )
        val t = Array(GOLOMB_N * 2 * 128) { ByteArray(GOLOMB_N) }
        for (n in 0 until GOLOMB_N) {
            var a = 0
            for (i in 0 until 9) for (j in 0 until comp[n][i]) t[a++][n] = i.toByte()
        }
        t
    }

    private fun rd32(p: ByteArray, o: Int): Int =
        (p[o].toInt() and 0xFF) or ((p[o + 1].toInt() and 0xFF) shl 8) or
            ((p[o + 2].toInt() and 0xFF) shl 16) or ((p[o + 3].toInt() and 0xFF) shl 24)

    /**
     * Decode Golomb values from [pool] into channel bytes in [pixelBuf]. [first] = true: the whole 32-bit word
     * is overwritten (first channel, not 1-channel); otherwise only the byte at [shift] is replaced.
     */
    private fun golomb(pixelBuf: IntArray, shift: Int, count: Int, pool: ByteArray, first: Boolean) {
        val mask = (0xFF shl shift).inv()
        var pi = 0
        var n = GOLOMB_N - 1
        var a = 0
        var bitPos = 1
        var zero = (pool[0].toInt() and 1) == 0
        var pixel = 0
        while (pixel < count) {
            var t0 = rd32(pool, pi) ushr bitPos
            var b0 = LEADING_ZERO[t0 and (LZ_SIZE - 1)].toInt()
            var bc0 = b0
            while (b0 == 0) {
                bc0 += LZ_BITS
                bitPos += LZ_BITS
                pi += bitPos shr 3
                bitPos = bitPos and 7
                t0 = rd32(pool, pi) ushr bitPos
                b0 = LEADING_ZERO[t0 and (LZ_SIZE - 1)].toInt()
                bc0 += b0
            }
            bitPos += b0
            pi += bitPos shr 3
            bitPos = bitPos and 7
            bc0--
            if (bc0 > 30) throw IOException("Data TLG6 rusak")
            var run = 1 shl bc0
            run += (rd32(pool, pi) ushr bitPos) and (run - 1)
            bitPos += bc0
            pi += bitPos shr 3
            bitPos = bitPos and 7
            if (zero) {
                var c = run
                while (c-- > 0 && pixel < count) {
                    if (first) pixelBuf[pixel] = 0 else pixelBuf[pixel] = pixelBuf[pixel] and mask
                    pixel++
                }
            } else {
                var c = run
                while (c-- > 0 && pixel < count) {
                    val k = GOLOMB_BITS[a][n].toInt()
                    var t = rd32(pool, pi) ushr bitPos
                    var bitCount: Int
                    var bb: Int
                    if (t != 0) {
                        bb = LEADING_ZERO[t and (LZ_SIZE - 1)].toInt()
                        bitCount = bb
                        while (bb == 0) {
                            bitCount += LZ_BITS
                            bitPos += LZ_BITS
                            pi += bitPos shr 3
                            bitPos = bitPos and 7
                            t = rd32(pool, pi) ushr bitPos
                            bb = LEADING_ZERO[t and (LZ_SIZE - 1)].toInt()
                            bitCount += bb
                        }
                        bitCount--
                    } else {
                        pi += 5
                        bitCount = pool[pi - 1].toInt() and 0xFF
                        bitPos = 0
                        t = rd32(pool, pi)
                        bb = 0
                    }
                    var v = (bitCount shl k) + ((t ushr bb) and ((1 shl k) - 1))
                    val sign = (v and 1) - 1
                    v = v shr 1
                    a += v
                    val value = ((v xor sign) + sign + 1) and 0xFF
                    pixelBuf[pixel] = if (first) value else (pixelBuf[pixel] and mask) or (value shl shift)
                    pixel++
                    bitPos += bb
                    bitPos += k
                    pi += bitPos shr 3
                    bitPos = bitPos and 7
                    if (--n < 0) {
                        a = a shr 1
                        n = GOLOMB_N - 1
                    }
                }
            }
            zero = !zero
        }
    }

    private val C_FE = 0xFEFEFEFE.toInt()
    private val C_7F = 0x7F7F7F7F
    private val C_80 = 0x80808080.toInt()
    private const val C_01 = 0x01010101
    private const val C_0100 = 0x01010100

    private fun gtMask(a: Int, b: Int): Int {
        val tmp2 = b.inv()
        val tmp = ((a and tmp2) + (((a xor tmp2) ushr 1) and C_7F)) and C_80
        return ((tmp ushr 7) + C_7F) xor C_7F
    }

    private fun packedAdd(a: Int, b: Int): Int = a + b - ((((a and b) shl 1) + ((a xor b) and C_FE)) and C_0100)

    private fun med(a: Int, b: Int, c: Int, v: Int): Int {
        val aaGtBb = gtMask(a, b)
        val x = (a xor b) and aaGtBb
        val aa = x xor a
        val bb = x xor b
        val n = gtMask(c, bb)
        val nn = gtMask(aa, c)
        val m = (n or nn).inv()
        return packedAdd((n and aa) or (nn and bb) or ((bb and m) - (c and m) + (aa and m)), v)
    }

    private fun avg(a: Int, b: Int, c: Int, v: Int): Int =
        packedAdd(((a and b) + (((a xor b) and C_FE) ushr 1)) + ((a xor b) and C_01), v)

    /** Color correlation filter: type 0..15 (= filter type / 2). */
    private fun chroma(t: Int, v: Int): Int {
        val b = v and 0xFF
        val g = (v ushr 8) and 0xFF
        val r = (v ushr 16) and 0xFF
        val nr: Int
        val ng: Int
        val nb: Int
        when (t) {
            0 -> { nr = r; ng = g; nb = b }
            1 -> { nr = r + g; ng = g; nb = b + g }
            2 -> { nr = r + b + g; ng = g + b; nb = b }
            3 -> { nr = r; ng = g + r; nb = b + r + g }
            4 -> { nr = r + b + r + g; ng = g + b + r; nb = b + r }
            5 -> { nr = r; ng = g + b + r; nb = b + r }
            6 -> { nr = r; ng = g; nb = b + g }
            7 -> { nr = r; ng = g + b; nb = b }
            8 -> { nr = r + g; ng = g; nb = b }
            9 -> { nr = r + b; ng = g + r + b; nb = b + g + r + b }
            10 -> { nr = r; ng = g + r; nb = b + r }
            11 -> { nr = r + b; ng = g + b; nb = b }
            12 -> { nr = r + b; ng = g + r + b; nb = b }
            13 -> { nr = r + b + g; ng = g + r + b + g; nb = b + g }
            14 -> { nr = r + b + g + r; ng = g + r; nb = b + g + r }
            else -> { nr = r + (b shl 1); ng = g + (b shl 1); nb = b }
        }
        return (v and -0x1000000) or ((nr and 0xFF) shl 16) or ((ng and 0xFF) shl 8) or (nb and 0xFF)
    }

    private fun decodeLine(
        prev: IntArray, prevIdx0: Int, cur: IntArray, curIdx0: Int, width: Int,
        startBlock: Int, blockLimit: Int, ft: ByteArray, ftIdx: Int, skipBlockBytes: Int,
        inb: IntArray, inIdx0: Int, initialP: Int, oddSkip: Int, dir: Int
    ) {
        var prevIdx = prevIdx0
        var curIdx = curIdx0
        var inIdx = inIdx0
        var p: Int
        var up: Int
        if (startBlock != 0) {
            prevIdx += startBlock * W_BLOCK
            curIdx += startBlock * W_BLOCK
            p = cur[curIdx - 1]
            up = prev[prevIdx - 1]
        } else {
            p = initialP
            up = initialP
        }
        inIdx += skipBlockBytes * startBlock
        val step = if ((dir and 1) != 0) 1 else -1
        for (i in startBlock until blockLimit) {
            var w = width - i * W_BLOCK
            if (w > W_BLOCK) w = W_BLOCK
            val ww = w
            if (step == -1) inIdx += ww - 1
            if ((i and 1) != 0) inIdx += oddSkip * ww
            val type = ft[ftIdx + i].toInt() and 0xFF
            if (type >= 32) throw IOException("Tipe filter TLG6 tidak valid: $type")
            val useAvg = (type and 1) != 0
            val ct = type shr 1
            do {
                val u = prev[prevIdx]
                val v = chroma(ct, inb[inIdx])
                p = if (useAvg) avg(p, u, up, v) else med(p, u, up, v)
                up = u
                cur[curIdx] = p
                curIdx++
                prevIdx++
                inIdx += step
            } while (--w != 0)
            if (step == 1) inIdx += skipBlockBytes - ww else inIdx += skipBlockBytes + 1
            if ((i and 1) != 0) inIdx -= oddSkip * ww
        }
    }

    private fun decodeV6(b: ByteArray, h: Hdr): RgbaImage {
        val width = h.width
        val height = h.height
        val colors = h.colors
        var pos = h.dataOffset
        u32(b, pos) // max_bit_length (unused; the buffer is sized per channel)
        pos += 4
        val xBlocks = (width - 1) / W_BLOCK + 1
        val yBlocks = (height - 1) / H_BLOCK + 1
        val mainCount = width / W_BLOCK
        val fraction = width - mainCount * W_BLOCK

        val bits = IntArray(width * height)
        val pixelBuf = IntArray(width * H_BLOCK + 1)
        val filterTypes = ByteArray(xBlocks * yBlocks)
        val zeroLine = IntArray(width)
        val zeroColor = if (colors == 3) -0x1000000 else 0
        java.util.Arrays.fill(zeroLine, zeroColor)
        var prevLine = zeroLine
        var prevIdx = 0

        // initial LZSS window for the filter types
        val text = ByteArray(4096)
        var p = 0
        for (i in 0 until 32) {
            for (j in 0 until 16) {
                for (k in 0 until 4) text[p++] = i.toByte()
                for (k in 0 until 4) text[p++] = j.toByte()
            }
        }
        val inSize = u32(b, pos)
        pos += 4
        if (inSize < 0 || inSize > b.size - pos) throw IOException("Data TLG terpotong")
        lzss(filterTypes, b.copyOfRange(pos, pos + inSize), inSize, text, 0)
        pos += inSize

        var pool = ByteArray(1024)
        var y = 0
        while (y < height) {
            val ylim = minOf(y + H_BLOCK, height)
            val pixelCount = (ylim - y) * width
            for (c in 0 until colors) {
                var bitLength = u32(b, pos)
                pos += 4
                val method = (bitLength shr 30) and 3
                bitLength = bitLength and 0x3FFFFFFF
                val byteLength = bitLength / 8 + (if (bitLength % 8 != 0) 1 else 0)
                if (byteLength > b.size - pos) throw IOException("Data TLG terpotong")
                if (pool.size < byteLength + 16) pool = ByteArray(byteLength + 16)
                System.arraycopy(b, pos, pool, 0, byteLength)
                java.util.Arrays.fill(pool, byteLength, byteLength + 16, 0)
                pos += byteLength
                if (method != 0) throw IOException("Metode entropi TLG6 belum didukung: $method")
                if (byteLength == 0) throw IOException("Data TLG6 rusak")
                golomb(pixelBuf, c * 8, pixelCount, pool, c == 0 && colors != 1)
            }
            val ft = (y / H_BLOCK) * xBlocks
            val skipBytes = (ylim - y) * W_BLOCK
            for (yy in y until ylim) {
                val curLine = yy * width
                val dir = (yy and 1) xor 1
                val oddSkip = (ylim - yy - 1) - (yy - y)
                if (mainCount != 0) {
                    val start = (if (width < W_BLOCK) width else W_BLOCK) * (yy - y)
                    decodeLine(prevLine, prevIdx, bits, curLine, width, 0, mainCount, filterTypes, ft,
                        skipBytes, pixelBuf, start, zeroColor, oddSkip, dir)
                }
                if (mainCount != xBlocks) {
                    var ww = fraction
                    if (ww > W_BLOCK) ww = W_BLOCK
                    val start = ww * (yy - y)
                    decodeLine(prevLine, prevIdx, bits, curLine, width, mainCount, xBlocks, filterTypes, ft,
                        skipBytes, pixelBuf, start, zeroColor, oddSkip, dir)
                }
                prevLine = bits
                prevIdx = curLine
            }
            y += H_BLOCK
        }

        val rgba = ByteArray(width * height * 4)
        var o = 0
        for (i in 0 until width * height) {
            val v = bits[i]
            if (colors == 1) {
                val g = (v and 0xFF).toByte()
                rgba[o] = g; rgba[o + 1] = g; rgba[o + 2] = g; rgba[o + 3] = -1
            } else {
                rgba[o] = (v ushr 16).toByte()
                rgba[o + 1] = (v ushr 8).toByte()
                rgba[o + 2] = v.toByte()
                rgba[o + 3] = if (colors == 3) -1 else (v ushr 24).toByte()
            }
            o += 4
        }
        return RgbaImage(width, height, rgba)
    }
}
