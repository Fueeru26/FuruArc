package com.furuarc.core

/** Standard Base64 (with padding) without depending on newer Android/Java APIs. */
object Base64Lite {
    private const val A = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encode(b: ByteArray): String {
        val sb = StringBuilder((b.size + 2) / 3 * 4)
        var i = 0
        while (i < b.size) {
            val b0 = b[i].toInt() and 0xFF
            val b1 = if (i + 1 < b.size) b[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < b.size) b[i + 2].toInt() and 0xFF else 0
            sb.append(A[b0 shr 2])
            sb.append(A[((b0 and 3) shl 4) or (b1 shr 4)])
            sb.append(if (i + 1 < b.size) A[((b1 and 15) shl 2) or (b2 shr 6)] else '=')
            sb.append(if (i + 2 < b.size) A[b2 and 63] else '=')
            i += 3
        }
        return sb.toString()
    }

    /** null if any character is outside the base64 alphabet (whitespace/newlines are ignored). */
    fun decode(s: String): ByteArray? {
        val out = java.io.ByteArrayOutputStream(s.length * 3 / 4)
        var acc = 0
        var bits = 0
        for (c in s) {
            if (c == '=' || c == ' ' || c == '\n' || c == '\r' || c == '\t') continue
            val v = A.indexOf(c)
            if (v < 0) return null
            acc = (acc shl 6) or v
            bits += 6
            if (bits >= 8) {
                bits -= 8
                out.write((acc shr bits) and 0xFF)
            }
        }
        return out.toByteArray()
    }
}
