package com.furuarc.core

import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Inflater

/**
 * Yu-Ris YCG image decoder, port of GARbro ArcFormats/YuRis/ImageYCG.cs (morkt/GARbro).
 *
 * 0x38-byte header: "YCG" (+0), width (+4), height (+8), bpp (+0xC), compression method (+0x10),
 * original/compressed size of block 1 (+0x20/+0x24), block 2 (+0x30/+0x34).
 * Method 1: two consecutive zlib streams (starting at 0x38 and 0x38+compressed1) joined into BGRA pixels.
 * Method 2 ("YSSnp") is NOT implemented in GARbro itself, so it is rejected here too with IOException.
 */
object YcgImage {
    fun isYcg(head: ByteArray): Boolean =
        head.size >= 4 && head[0] == 'Y'.code.toByte() && head[1] == 'C'.code.toByte() &&
            head[2] == 'G'.code.toByte() && head[3].toInt() == 0

    fun decode(b: ByteArray): RgbaImage {
        try {
            if (!isYcg(b)) throw IOException("Bukan berkas YCG (tanda 'YCG' tidak ditemukan)")
            if (b.size < 0x38) throw IOException("Header YCG terpotong")
            val w = i32(b, 4)
            val h = i32(b, 8)
            val method = i32(b, 0x10)
            val un1 = i32(b, 0x20)
            val cm1 = i32(b, 0x24)
            val un2 = i32(b, 0x30)
            val cm2 = i32(b, 0x34)
            if (w <= 0 || h <= 0 || w > 16384 || h > 16384 || w.toLong() * h > 100_000_000L)
                throw IOException("Dimensi gambar YCG tidak valid: ${w}x$h")
            if (method == 2) throw IOException("YCG dengan kompresi YSSnp (metode 2) belum didukung")
            if (method != 1) throw IOException("Metode kompresi YCG tidak dikenal: $method")
            val total = w * h * 4
            if (un1 < 0 || un2 < 0 || cm1 < 0 || cm2 < 0 || un1.toLong() + un2 > total)
                throw IOException("Ukuran blok YCG tidak valid")
            if (0x38L + cm1 > b.size) throw IOException("Data YCG terpotong")
            val px = ByteArray(total)
            inflate(b, 0x38, px, 0, un1)
            inflate(b, 0x38 + cm1, px, un1, un2)
            var i = 0
            while (i < total) {
                val t = px[i]
                px[i] = px[i + 2]
                px[i + 2] = t
                i += 4
            }
            return RgbaImage(w, h, px)
        } catch (e: IOException) {
            throw e
        } catch (e: RuntimeException) {
            throw IOException("Data YCG rusak atau tidak lengkap", e)
        }
    }

    private fun inflate(src: ByteArray, off: Int, dst: ByteArray, dpos: Int, n: Int) {
        if (n == 0) return
        val inf = Inflater()
        try {
            inf.setInput(src, off, src.size - off)
            var got = 0
            while (got < n) {
                val r = inf.inflate(dst, dpos + got, n - got)
                if (r == 0 && (inf.finished() || inf.needsInput() || inf.needsDictionary()))
                    throw IOException("Aliran zlib YCG terpotong")
                got += r
            }
        } catch (e: DataFormatException) {
            throw IOException("Aliran zlib YCG rusak", e)
        } finally {
            inf.end()
        }
    }

    private fun i32(b: ByteArray, o: Int): Int =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
            ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
}
