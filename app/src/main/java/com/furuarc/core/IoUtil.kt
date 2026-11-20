package com.furuarc.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

fun ByteArray.le(): ByteBuffer = ByteBuffer.wrap(this).order(ByteOrder.LITTLE_ENDIAN)

fun RandomSource.readFully(pos: Long, len: Int): ByteArray {
    val b = ByteArray(len)
    var r = 0
    while (r < len) {
        val n = readAt(pos + r, b, r, len - r)
        if (n <= 0) throw EOFException("Arsip terpotong di posisi ${pos + r}")
        r += n
    }
    return b
}

object IoUtil {
    fun inflate(b: ByteArray): ByteArray =
        InflaterInputStream(ByteArrayInputStream(b)).use { it.readBytes() }

    fun deflate(b: ByteArray, level: Int = java.util.zip.Deflater.DEFAULT_COMPRESSION): ByteArray {
        val bo = ByteArrayOutputStream()
        val d = java.util.zip.Deflater(level)
        try {
            DeflaterOutputStream(bo, d).use { it.write(b) }
        } finally {
            d.end()
        }
        return bo.toByteArray()
    }

    /** Fill the buffer as much as possible; returns the number of bytes read (0 = exhausted). */
    fun fill(ins: InputStream, buf: ByteArray): Int {
        var t = 0
        while (t < buf.size) {
            val n = ins.read(buf, t, buf.size - t)
            if (n < 0) break
            t += n
        }
        return t
    }

    /** XOR the buffer with a repeating key; [startPos] = position relative to the start of the file. */
    fun xorKey(buf: ByteArray, n: Int, startPos: Long, key: ByteArray) {
        var k = (startPos % key.size).toInt()
        for (i in 0 until n) {
            buf[i] = (buf[i].toInt() xor key[k].toInt()).toByte()
            k++
            if (k == key.size) k = 0
        }
    }

    /** Copy [len] bytes from [pos] to [out], optionally XOR-ed with [key]. */
    fun copyRange(src: RandomSource, pos: Long, len: Long, out: OutputStream, key: ByteArray? = null) {
        val buf = ByteArray(1024 * 1024)
        var done = 0L
        while (done < len) {
            val want = minOf(buf.size.toLong(), len - done).toInt()
            val n = src.readAt(pos + done, buf, 0, want)
            if (n <= 0) throw EOFException("Arsip terpotong")
            if (key != null) xorKey(buf, n, done, key)
            out.write(buf, 0, n)
            done += n
        }
    }

    /** Write the contents of [input] at the RandomAccessFile's current position; returns the number of bytes written. */
    fun appendInput(raf: RandomAccessFile, input: PackInput, buf: ByteArray): Long {
        var total = 0L
        input.open().use { ins ->
            while (true) {
                val n = ins.read(buf)
                if (n < 0) break
                raf.write(buf, 0, n)
                total += n
            }
        }
        return total
    }

    /** Read a NUL-terminated string from [b] starting at [off], at most [max] bytes. */
    fun cstr(b: ByteArray, off: Int, max: Int, cs: java.nio.charset.Charset): String {
        var n = 0
        while (n < max && b[off + n].toInt() != 0) n++
        return String(b, off, n, cs)
    }

    fun sjis(): java.nio.charset.Charset =
        try { java.nio.charset.Charset.forName("windows-31j") } catch (e: Exception) { Charsets.UTF_8 }

    /** File name charset according to the "enc" option (auto = [def]). */
    fun cs(opts: Options, def: java.nio.charset.Charset = sjis()): java.nio.charset.Charset {
        val n = when (opts.extra["enc"]) {
            "sjis" -> return sjis()
            "utf8" -> return Charsets.UTF_8
            "gbk" -> "GBK"
            "big5" -> "Big5"
            "euckr" -> "EUC-KR"
            else -> return def
        }
        return try { java.nio.charset.Charset.forName(n) } catch (e: Exception) { def }
    }

    /** File name is sensible: not empty and without control characters. */
    fun saneName(n: String): Boolean = n.isNotEmpty() && n.none { it.code < 0x20 || it.code == 0x7f }
}
