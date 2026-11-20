package com.furuarc.core

import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.CRC32
import java.util.zip.Deflater
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

/** CRX decode result: BGR/BGRA pixels (or 8-bit palette indices) with row [stride] (multiple of 4 bytes). */
class CrxImage(
    val width: Int, val height: Int, val bpp: Int, val stride: Int, val data: ByteArray,
    /** RGB palette (3 bytes per color) for 8-bit images, otherwise null */
    val palette: ByteArray?,
    val offsetX: Int, val offsetY: Int,
)

/**
 * Circus image (.crx). CRXG = normal image (compression 1 = LZ, 2/3 = per-row zlib with filter), CRXD = difference image
 * added onto a base image (base name stored in the header). Zstd (new version) is not supported yet.
 */
object Crx {
    class Info(
        val x: Int, val y: Int, val w: Int, val h: Int, val comp: Int, val flags: Int, val colors: Int, val mode: Int,
    ) {
        val bpp: Int get() = if (colors == 0) 24 else if (colors == 1) 32 else 8
    }

    fun isCrxg(b: ByteArray, p: Int = 0) = b.size >= p + 0x14 && b[p] == 'C'.code.toByte() && b[p + 1] == 'R'.code.toByte() && b[p + 2] == 'X'.code.toByte() && b[p + 3] == 'G'.code.toByte()
    fun isCrxd(b: ByteArray, p: Int = 0) = b.size >= p + 0x24 && b[p] == 'C'.code.toByte() && b[p + 1] == 'R'.code.toByte() && b[p + 2] == 'X'.code.toByte() && b[p + 3] == 'D'.code.toByte()
    fun isCrx(b: ByteArray) = isCrxg(b) || isCrxd(b)

    private fun u16(b: ByteArray, p: Int) = (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8)
    private fun s16(b: ByteArray, p: Int) = u16(b, p).toShort().toInt()
    private fun u32(b: ByteArray, p: Int) =
        (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8) or ((b[p + 2].toInt() and 0xFF) shl 16) or ((b[p + 3].toInt() and 0xFF) shl 24)

    fun readInfo(b: ByteArray, p: Int = 0): Info? {
        if (!isCrxg(b, p)) return null
        val comp = u16(b, p + 0xC)
        if (comp < 1 || comp > 3) return null
        return Info(s16(b, p + 4), s16(b, p + 6), u16(b, p + 8), u16(b, p + 10), comp, u16(b, p + 0xE), u16(b, p + 0x10), u16(b, p + 0x12))
    }

    /** Base image name if [b] is a CRXD, otherwise null. */
    fun baseName(b: ByteArray): String? {
        if (!isCrxd(b)) return null
        var e = 0xC
        while (e < 0x20 && b[e].toInt() != 0) e++
        return String(b, 0xC, e - 0xC, Charsets.ISO_8859_1)
    }

    private class Rd(val s: InputStream) {
        fun u8(): Int {
            val v = s.read()
            if (v < 0) throw EOFException("Data CRX terpotong")
            return v
        }

        fun u16(): Int = u8() or (u8() shl 8)
        fun fill(dst: ByteArray, off: Int, n: Int) {
            var k = 0
            while (k < n) {
                val r = s.read(dst, off + k, n - k)
                if (r <= 0) throw EOFException("Data CRX terpotong")
                k += r
            }
        }
    }

    /** Decode a CRXG starting at [p] in [b]. */
    fun decode(b: ByteArray, p: Int = 0): CrxImage {
        val info = readInfo(b, p) ?: throw IOException("Bukan gambar CRX yang didukung")
        val w = info.w
        val h = info.h
        val bpp = info.bpp
        if (w <= 0 || h <= 0) throw IOException("Ukuran gambar CRX tidak valid")
        val stride = (w * bpp / 8 + 3) and 3.inv()
        val size = stride.toLong() * h
        if (size > 400L * 1024 * 1024) throw IOException("Gambar terlalu besar ($w x $h)")
        val out = ByteArray(size.toInt())
        var pos = p + 0x14
        var pal: ByteArray? = null
        if (bpp == 8) {
            val cs = if (info.colors == 0x102) 4 else 3
            val n = minOf(info.colors, 0x100)
            if (pos + n * cs > b.size) throw EOFException("Palet CRX terpotong")
            pal = ByteArray(256 * 3)
            for (i in 0 until n) {
                val r = b[pos + i * cs]
                var g = b[pos + i * cs + 1]
                val bl = b[pos + i * cs + 2]
                if (r.toInt() == -1 && g.toInt() == 0 && bl.toInt() == -1) g = -1
                pal[i * 3] = r; pal[i * 3 + 1] = g; pal[i * 3 + 2] = bl
            }
            pos += n * cs
        }
        if (info.comp >= 3) {
            val count = u32(b, pos)
            pos += 4 + count * 0x10
        }
        if (info.flags and 0x10 != 0) pos += 4
        if (pos >= b.size) throw EOFException("Data CRX terpotong")
        if (b.size - pos >= 4 && b[pos] == 0x28.toByte() && b[pos + 1] == 0xB5.toByte() && b[pos + 2] == 0x2F.toByte() && b[pos + 3] == 0xFD.toByte()) {
            throw IOException("CRX ini memakai kompresi Zstd (versi baru) yang belum didukung")
        }
        if (info.comp == 1) {
            unpackV1(Rd(ByteArrayInputStream(b, pos, b.size - pos)), out)
        } else {
            val z = BufferedInputStream(InflaterInputStream(ByteArrayInputStream(b, pos, b.size - pos)), 1 shl 16)
            unpackV2(Rd(z), out, w, h, bpp, stride)
        }
        if (bpp == 32 && info.mode != 1) {
            val flip = if (info.mode == 2) 0 else 0xFF
            var line = 0
            for (y in 0 until h) {
                var q = line
                for (x in 0 until w) {
                    val a = out[q]
                    val bb = out[q + 1]
                    val g = out[q + 2]
                    val r = out[q + 3]
                    out[q] = bb
                    out[q + 1] = g
                    out[q + 2] = r
                    out[q + 3] = (a.toInt() xor flip).toByte()
                    q += 4
                }
                line += stride
            }
        }
        return CrxImage(w, h, bpp, stride, out, pal, info.x, info.y)
    }

    private fun unpackV1(r: Rd, out: ByteArray) {
        val window = ByteArray(0x10000)
        var flag = 0
        var win = 0
        var dst = 0
        while (dst < out.size) {
            flag = flag shr 1
            if (flag and 0x100 == 0) flag = r.u8() or 0xFF00
            if (flag and 1 != 0) {
                val d = r.u8().toByte()
                window[win] = d
                win = (win + 1) and 0xFFFF
                out[dst++] = d
            } else {
                val ctl = r.u8()
                var count: Int
                var off: Int
                if (ctl >= 0xC0) {
                    off = ((ctl and 3) shl 8) or r.u8()
                    count = 4 + ((ctl shr 2) and 0xF)
                } else if (ctl and 0x80 != 0) {
                    off = ctl and 0x1F
                    count = 2 + ((ctl shr 5) and 3)
                    if (off == 0) off = r.u8()
                } else if (ctl == 0x7F) {
                    count = 2 + r.u16()
                    off = r.u16()
                } else {
                    off = r.u16()
                    count = ctl + 4
                }
                off = win - off
                var k = 0
                while (k < count && dst < out.size) {
                    off = off and 0xFFFF
                    val d = window[off++]
                    window[win] = d
                    win = (win + 1) and 0xFFFF
                    out[dst++] = d
                    k++
                }
            }
        }
    }

    private fun unpackV2(r: Rd, out: ByteArray, w: Int, h: Int, bpp: Int, stride: Int) {
        val ps = bpp / 8
        val srcStride = w * ps
        if (bpp < 24) {
            var dst = 0
            for (y in 0 until h) {
                r.fill(out, dst, srcStride)
                dst += stride
            }
            return
        }
        for (y in 0 until h) {
            val ctl = r.u8()
            var dst = y * stride
            var prev = dst - stride
            if (y == 0 && ctl in 1..3) throw IOException("CRX rusak: baris pertama memakai filter vertikal")
            when (ctl) {
                0 -> {
                    r.fill(out, dst, ps)
                    for (x in ps until srcStride) out[dst + x] = (r.u8() + out[dst + x - ps]).toByte()
                }
                1 -> for (x in 0 until srcStride) out[dst + x] = (r.u8() + out[prev + x]).toByte()
                2 -> {
                    r.fill(out, dst, ps)
                    for (x in ps until srcStride) out[dst + x] = (r.u8() + out[prev + x - ps]).toByte()
                }
                3 -> {
                    var x = srcStride - ps
                    while (x > 0) {
                        out[dst++] = (r.u8() + out[prev++ + ps]).toByte()
                        x--
                    }
                    r.fill(out, dst, ps)
                }
                4 -> {
                    for (i in 0 until ps) {
                        var wl = w
                        var v = r.u8()
                        while (wl > 0) {
                            out[dst] = v.toByte()
                            dst += ps
                            if (--wl == 0) break
                            val next = r.u8()
                            if (v == next) {
                                val cnt = r.u8()
                                for (j in 0 until cnt) {
                                    out[dst] = v.toByte()
                                    dst += ps
                                }
                                wl -= cnt
                                if (wl > 0) v = r.u8()
                            } else {
                                v = next
                            }
                        }
                        dst -= srcStride - 1
                    }
                }
                else -> { }
            }
        }
    }

    /**
     * Decode CRXD: base image ([lookup] is called with the base name from the header, e.g. "ASA_A1_A17.CRX") then the difference
     * is added at its position. Returns the base image if the difference lies outside the image.
     */
    fun decodeCrxd(b: ByteArray, lookup: (String) -> ByteArray?): CrxImage {
        val name = baseName(b) ?: throw IOException("Bukan CRXD")
        if (b.size < 0x28 || !isCrxg(b, 0x20)) throw IOException("CRXD ini merujuk selisih di file lain (CRXJ), belum didukung")
        val baseBytes = lookup(name) ?: throw IOException("Gambar basis tidak ditemukan: $name (pilih folder yang memuat basisnya)")
        val base = decode(baseBytes, 0)
        val diff = decode(b, 0x20)
        if (base.bpp != diff.bpp) throw IOException("Basis dan selisih berbeda format")
        val ps = base.bpp / 8
        // intersection of two rectangles (base and difference coordinates are both in canvas space)
        val x0 = maxOf(diff.offsetX, base.offsetX)
        val y0 = maxOf(diff.offsetY, base.offsetY)
        val x1 = minOf(diff.offsetX + diff.width, base.offsetX + base.width)
        val y1 = minOf(diff.offsetY + diff.height, base.offsetY + base.height)
        if (x1 <= x0 || y1 <= y0) return base
        val img = base.data
        var dst = (y0 - base.offsetY) * base.stride + ps * (x0 - base.offsetX)
        var src = (y0 - diff.offsetY) * diff.stride + ps * (x0 - diff.offsetX)
        val bs = (x1 - x0) * ps
        for (row in 0 until (y1 - y0)) {
            var i = 0
            while (i < bs) {
                img[dst + i] = (img[dst + i] + diff.data[src + i]).toByte()
                img[dst + i + 1] = (img[dst + i + 1] + diff.data[src + i + 1]).toByte()
                img[dst + i + 2] = (img[dst + i + 2] + diff.data[src + i + 2]).toByte()
                if (ps == 4) img[dst + i + 3] = (img[dst + i + 3] - diff.data[src + i + 3]).toByte()
                i += ps
            }
            dst += base.stride
            src += diff.stride
        }
        return base
    }

    /** Write [img] as PNG (RGB, RGBA, or paletted). */
    fun writePng(img: CrxImage, os: OutputStream) {
        val w = img.width
        val h = img.height
        val ct: Int
        val bytesPp: Int
        when {
            img.bpp == 8 -> { ct = 3; bytesPp = 1 }
            img.bpp == 32 -> { ct = 6; bytesPp = 4 }
            else -> { ct = 2; bytesPp = 3 }
        }
        os.write(byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A))
        val ihdr = ByteArray(13)
        putBe(ihdr, 0, w); putBe(ihdr, 4, h)
        ihdr[8] = 8; ihdr[9] = ct.toByte()
        chunk(os, "IHDR", ihdr)
        if (ct == 3) chunk(os, "PLTE", img.palette ?: ByteArray(768))
        val idat = ChunkOut(os, "IDAT")
        val z = DeflaterOutputStream(idat, Deflater(6), 1 shl 16)
        val rowLen = w * bytesPp
        val cur = ByteArray(rowLen + 1)
        for (y in 0 until h) {
            val s = y * img.stride
            // Sub filter (1) so it is smaller than no filter
            cur[0] = 1
            val raw = ByteArray(rowLen)
            when (bytesPp) {
                1 -> System.arraycopy(img.data, s, raw, 0, rowLen)
                3 -> for (x in 0 until w) {
                    raw[x * 3] = img.data[s + x * 3 + 2]
                    raw[x * 3 + 1] = img.data[s + x * 3 + 1]
                    raw[x * 3 + 2] = img.data[s + x * 3]
                }
                else -> for (x in 0 until w) {
                    raw[x * 4] = img.data[s + x * 4 + 2]
                    raw[x * 4 + 1] = img.data[s + x * 4 + 1]
                    raw[x * 4 + 2] = img.data[s + x * 4]
                    raw[x * 4 + 3] = img.data[s + x * 4 + 3]
                }
            }
            for (i in 0 until rowLen) {
                val left = if (i >= bytesPp) raw[i - bytesPp] else 0
                cur[i + 1] = (raw[i] - left).toByte()
            }
            z.write(cur, 0, rowLen + 1)
        }
        z.finish()
        idat.close()
        chunk(os, "IEND", ByteArray(0))
    }

    private fun putBe(b: ByteArray, p: Int, v: Int) {
        b[p] = (v ushr 24).toByte(); b[p + 1] = (v ushr 16).toByte(); b[p + 2] = (v ushr 8).toByte(); b[p + 3] = v.toByte()
    }

    private fun chunk(os: OutputStream, type: String, data: ByteArray) {
        val c = ChunkOut(os, type)
        c.write(data, 0, data.size)
        c.close()
    }

    /** Writes one or more PNG chunks of the same type (each 64 KB piece becomes one chunk). */
    private class ChunkOut(private val os: OutputStream, private val type: String) : OutputStream() {
        private val buf = ByteArray(1 shl 16)
        private var n = 0
        private var wrote = false
        override fun write(b: Int) {
            if (n == buf.size) flushChunk()
            buf[n++] = b.toByte()
        }

        override fun write(b: ByteArray, off: Int, len: Int) {
            var o = off
            var l = len
            while (l > 0) {
                if (n == buf.size) flushChunk()
                val k = minOf(l, buf.size - n)
                System.arraycopy(b, o, buf, n, k)
                n += k; o += k; l -= k
            }
        }

        private fun flushChunk() {
            val head = ByteArray(8)
            putBe(head, 0, n)
            for (i in 0 until 4) head[4 + i] = type[i].code.toByte()
            os.write(head)
            os.write(buf, 0, n)
            val crc = CRC32()
            crc.update(head, 4, 4)
            crc.update(buf, 0, n)
            val c = ByteArray(4)
            putBe(c, 0, crc.value.toInt())
            os.write(c)
            n = 0
            wrote = true
        }

        override fun close() {
            if (n > 0 || !wrote) flushChunk()
        }
    }
}

/** Circus audio (.pcm, header "XPCM"). Mode 5 = Ogg Vorbis inside, 0 = raw PCM, 2 = simple ADPCM; 1/3 (MDCT) not supported yet. */
object CircusPcm {
    class Info(val mode: Int, val extra: Int, val srcSize: Long, val oggSize: Long, val fmt: ByteArray?, val dataOffset: Int)

    fun isPcm(h: ByteArray) = h.size >= 0x10 && h[0] == 'X'.code.toByte() && h[1] == 'P'.code.toByte() && h[2] == 'C'.code.toByte() && h[3] == 'M'.code.toByte()

    private fun u32(b: ByteArray, p: Int): Long =
        ((b[p].toLong() and 0xFF) or ((b[p + 1].toLong() and 0xFF) shl 8) or ((b[p + 2].toLong() and 0xFF) shl 16) or ((b[p + 3].toLong() and 0xFF) shl 24))

    /** [h] = at least the first 0x1C bytes of the file. */
    fun parse(h: ByteArray): Info {
        if (!isPcm(h)) throw IOException("Bukan file XPCM")
        val src = u32(h, 4)
        val m = u32(h, 8).toInt()
        val mode = m and 0xFF
        val extra = (m shr 8) and 0xFF
        if (mode == 5) {
            val og = u32(h, 0xC)
            if (og <= 0L) throw IOException("Ukuran Ogg di header XPCM tidak valid")
            return Info(mode, extra, src, og, null, 0x10)
        }
        if (h.size < 0x1C) throw IOException("Header XPCM terpotong")
        return Info(mode, extra, src, 0L, h.copyOfRange(0xC, 0x1C), 0x1C)
    }

    fun modeName(mode: Int) = when (mode) { 0 -> "PCM mentah"; 1 -> "LZSS+MDCT"; 2 -> "ADPCM"; 3 -> "zlib+MDCT"; 5 -> "Ogg Vorbis"; else -> "mode $mode" }

    private fun skipFully(s: InputStream, n: Long) {
        var left = n
        while (left > 0) {
            val k = s.skip(left)
            if (k <= 0) {
                if (s.read() < 0) throw EOFException()
                left--
            } else left -= k
        }
    }

    /** Copy the Ogg from an XPCM mode 5 file (stream [ins] starting at the beginning of the file). */
    fun copyOgg(ins: InputStream, info: Info, os: OutputStream) {
        skipFully(ins, info.dataOffset.toLong())
        val buf = ByteArray(64 * 1024)
        var left = info.oggSize
        while (left > 0) {
            val r = ins.read(buf, 0, minOf(buf.size.toLong(), left).toInt())
            if (r <= 0) throw EOFException("Data Ogg terpotong")
            os.write(buf, 0, r)
            left -= r
        }
    }

    /** Write a 16-bit WAV from XPCM mode 0 or 2 (stream [ins] starting at the beginning of the file). */
    fun writeWav(ins: InputStream, info: Info, os: OutputStream) {
        val fmt = info.fmt ?: throw IOException("Format audio tidak ada")
        skipFully(ins, info.dataOffset.toLong())
        val pcm: ByteArray
        if (info.mode == 0) {
            if (info.srcSize > 512L * 1024 * 1024) throw IOException("Audio terlalu besar")
            pcm = ByteArray(info.srcSize.toInt())
            var k = 0
            while (k < pcm.size) {
                val r = ins.read(pcm, k, pcm.size - k)
                if (r <= 0) break
                k += r
            }
            if (k < pcm.size) throw EOFException("Data PCM terpotong")
        } else if (info.mode == 2) {
            if (info.srcSize > 512L * 1024 * 1024) throw IOException("Audio terlalu besar")
            pcm = decodeAdpcm(ins, info.srcSize.toInt())
        } else {
            throw IOException("Mode XPCM ${modeName(info.mode)} (${info.mode}) belum didukung")
        }
        val tag = (fmt[0].toInt() and 0xFF) or ((fmt[1].toInt() and 0xFF) shl 8)
        if (tag != 1) throw IOException("Format audio XPCM bukan PCM (tag $tag)")
        val h = ByteArray(44)
        fun put(p: Int, s: String) { for (i in s.indices) h[p + i] = s[i].code.toByte() }
        fun le32(p: Int, v: Long) { for (i in 0 until 4) h[p + i] = (v shr (8 * i)).toByte() }
        put(0, "RIFF"); le32(4, 36L + pcm.size); put(8, "WAVE"); put(12, "fmt "); le32(16, 16)
        System.arraycopy(fmt, 0, h, 20, 16)
        put(36, "data"); le32(40, pcm.size.toLong())
        os.write(h)
        os.write(pcm)
    }

    private fun decodeAdpcm(ins: InputStream, size: Int): ByteArray {
        val out = ByteArray(size)
        val tbl = IntArray(6)
        var ch = 0
        var dst = 0
        while (dst + 1 < size) {
            val d = ins.read()
            if (d < 0) break
            val v = d.toByte().toInt()
            tbl[ch * 3] = tbl[ch * 3] + (v shl (tbl[ch * 3 + 1] and 0xFF))
            if (v == 0) {
                if (tbl[ch * 3 + 1] != 0) tbl[ch * 3 + 1]--
            } else if (v == 0x7F || v == -0x80) {
                if (tbl[ch * 3 + 1] != 8) tbl[ch * 3 + 1]++
            }
            out[dst] = tbl[ch * 3].toByte()
            out[dst + 1] = (tbl[ch * 3] shr 8).toByte()
            ch = 1 - ch
            dst += 2
        }
        return out
    }
}
