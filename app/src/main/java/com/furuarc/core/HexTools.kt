package com.furuarc.core

import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/** Pure logic of the Hex Viewer. */
object HexTools {
    private const val DIG = "0123456789ABCDEF"
    private const val CHUNK = 1 shl 20
    const val MAX_PATTERN = 64

    /** One hex dump page: offset, [bpr] hex bytes per row, then the ASCII column. */
    fun page(data: ByteArray, base: Long, bpr: Int): String {
        val sb = StringBuilder(data.size * 4 + 64)
        var i = 0
        while (i < data.size) {
            sb.append(String.format("%08X", base + i)).append("  ")
            for (j in 0 until bpr) {
                if (i + j < data.size) {
                    val v = data[i + j].toInt() and 0xFF
                    sb.append(DIG[v shr 4]).append(DIG[v and 15]).append(' ')
                } else {
                    sb.append("   ")
                }
            }
            sb.append(' ')
            for (j in 0 until bpr) {
                if (i + j < data.size) {
                    val v = data[i + j].toInt() and 0xFF
                    sb.append(if (v in 32..126) v.toChar() else '.')
                }
            }
            sb.append('\n')
            i += bpr
        }
        return sb.toString().trimEnd('\n')
    }

    /** "4D 5A 90" / "4D5A90" / "0x4D5A" -> bytes; null if not valid even-length hex. */
    fun parseHex(s: String): ByteArray? {
        val t = s.replace("0x", "", ignoreCase = true).filter { !it.isWhitespace() && it != ',' }
        if (t.isEmpty() || t.length % 2 != 0) return null
        val out = ByteArray(t.length / 2)
        for (i in out.indices) {
            val h = Character.digit(t[2 * i], 16)
            val l = Character.digit(t[2 * i + 1], 16)
            if (h < 0 || l < 0) return null
            out[i] = ((h shl 4) or l).toByte()
        }
        return out
    }

    /** Hex offset ("1A2B" or "0x1A2B"); null if invalid. */
    fun parseOffset(s: String): Long? {
        val t = s.trim().removePrefix("0x").removePrefix("0X")
        if (t.isEmpty()) return null
        return t.toLongOrNull(16)
    }

    /** Search for [pat] starting at [from]. Result: offset, -1 if not found, -2 if cancelled via [active]. */
    fun find(ch: FileChannel, pat: ByteArray, from: Long, active: () -> Boolean): Long {
        if (pat.isEmpty()) return -1L
        val len = ch.size()
        val buf = ByteBuffer.allocate(CHUNK + pat.size - 1)
        var pos = from
        while (pos < len) {
            if (!active()) return -2L
            buf.clear()
            val k = ch.read(buf, pos)
            if (k <= 0) break
            val arr = buf.array()
            val last = k - pat.size
            var i = 0
            while (i <= last) {
                if (arr[i] == pat[0]) {
                    var j = 1
                    while (j < pat.size && arr[i + j] == pat[j]) j++
                    if (j == pat.size) return pos + i
                }
                i++
            }
            if (pos + k >= len) break
            val step = k - (pat.size - 1)
            if (step <= 0) break
            pos += step
        }
        return -1L
    }
}
