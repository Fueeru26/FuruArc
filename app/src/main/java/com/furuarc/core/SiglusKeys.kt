package com.furuarc.core

/**
 * Recovers the SiglusEngine exe key from Scene.pck (without SiglusEngine.exe).
 *
 * The key (16 bytes) is not stored whole in the exe; it is assembled while the game runs. But every scene in Scene.pck is encrypted
 * with that key XOR (16-byte cycle, from the start of the blob), then the EASY table XOR, then LZSS. The start of each decrypted scene
 * has a fixed pattern that can be used as known plaintext:
 *  - byte 0..3  = blob size (packed size) -> key[0..3]
 *  - byte 7 = 0, 8 = 0xFF (LZSS flag), 9..12 = 84 00 00 00 (132-byte scene header), 15 = 0, 20 = 0, 21 = 0, 22 = 0xC2, 30 = 0
 * The remaining bytes (4, 13, 14) are found with an LZSS decoder "oracle": the correct key makes decompression finish exactly at the end of the blob.
 * The final result is verified on all scenes. Tested on three real Siglus games.
 */
object SiglusKeys {
    class Result(val key: ByteArray, val verified: Int, val total: Int) {
        val ok get() = total > 0 && verified * 100 >= total * 95
    }

    private fun xorDec(s: ByteArray, key: ByteArray): ByteArray {
        val t = ByteArray(s.size)
        val easy = SiglusCodec.EASY
        for (i in s.indices) t[i] = (s[i].toInt() xor key[i and 15].toInt() xor easy[i and 255].toInt()).toByte()
        return t
    }

    /** (number of output bytes successfully decoded, finished exactly at the end of the blob?) */
    fun probe(src: ByteArray): Pair<Int, Boolean> {
        if (src.size < 9) return Pair(0, false)
        val org = (src[4].toLong() and 0xff) or ((src[5].toLong() and 0xff) shl 8) or ((src[6].toLong() and 0xff) shl 16) or ((src[7].toLong() and 0xff) shl 24)
        if (org <= 0L || org > 0x4000000L) return Pair(0, false)
        val out = ByteArray(org.toInt())
        var o = 0
        var si = 8
        val n = src.size
        while (o < out.size) {
            if (si >= n) return Pair(o, false)
            var fl = src[si++].toInt() and 0xff
            for (bit in 0 until 8) {
                if (o >= out.size) break
                if (fl and 1 != 0) {
                    if (si >= n) return Pair(o, false)
                    out[o++] = src[si++]
                } else {
                    if (si + 2 > n) return Pair(o, false)
                    val tok = (src[si].toInt() and 0xff) or ((src[si + 1].toInt() and 0xff) shl 8)
                    si += 2
                    val off = tok ushr 4
                    val len = (tok and 0xf) + 2
                    if (off == 0 || off > o) return Pair(o, false)
                    var st = o - off
                    for (j in 0 until len) { if (o >= out.size) break; out[o++] = out[st++] }
                }
                fl = fl ushr 1
            }
        }
        return Pair(o, si == n)
    }

    private fun modeAt(blobs: List<ByteArray>, p: Int, known: Int): Int {
        val cnt = IntArray(256)
        var any = false
        for (s in blobs) {
            if (p >= s.size) continue
            any = true
            cnt[(s[p].toInt() xor SiglusCodec.EASY[p and 255].toInt() xor known) and 255]++
        }
        if (!any) return 0
        var best = 0
        for (v in 1..255) if (cnt[v] > cnt[best]) best = v
        return best
    }

    /** [blobs] = raw (encrypted) scene blobs from Scene.pck. */
    fun find(all: List<ByteArray>, cancelled: () -> Boolean = { false }): Result? {
        val blobs = all.filter { it.size >= 32 }
        if (blobs.isEmpty()) return null
        val key = ByteArray(16)
        for (j in 0 until 4) {
            val cnt = IntArray(256)
            for (s in blobs) cnt[(s[j].toInt() xor SiglusCodec.EASY[j].toInt() xor ((s.size ushr (8 * j)) and 255)) and 255]++
            var best = 0
            for (v in 1..255) if (cnt[v] > cnt[best]) best = v
            key[j] = best.toByte()
        }
        val known = listOf(21 to 0, 22 to 0xc2, 7 to 0, 8 to 0xff, 9 to 0x84, 10 to 0, 11 to 0, 12 to 0, 15 to 0, 20 to 0, 30 to 0)
        for ((p, pt) in known) key[p and 15] = modeAt(blobs, p, pt).toByte()

        val small = blobs.sortedBy { it.size }.take(8)
        fun score(): Long {
            var sc = 0L
            for (s in small) { val r = probe(xorDec(s, key)); sc += (if (r.second) 100000L else 0L) + r.first }
            return sc
        }
        var best = score()
        val full = small.size * 100000L
        for (round in 0 until 3) {
            for (j in intArrayOf(13, 14, 4)) {
                if (cancelled()) return null
                var bv = key[j]
                for (v in 0..255) {
                    key[j] = v.toByte()
                    val s = score()
                    if (s > best) { best = s; bv = v.toByte() }
                }
                key[j] = bv
            }
            if (best >= full) break
        }
        if (best < full) {
            // fallback: search byte 13 and 14 as a pair on the smallest scene
            val one = small[0]
            var bestOne = -1
            var b13 = key[13]; var b14 = key[14]
            for (a in 0..255) {
                if (cancelled()) return null
                for (b in 0..255) {
                    key[13] = a.toByte(); key[14] = b.toByte()
                    val r = probe(xorDec(one, key))
                    val s = (if (r.second) 1_000_000 else 0) + r.first
                    if (s > bestOne) { bestOne = s; b13 = a.toByte(); b14 = b.toByte() }
                }
            }
            key[13] = b13; key[14] = b14
        }
        var ok = 0
        for (s in blobs) if (probe(xorDec(s, key)).second) ok++
        return Result(key, ok, blobs.size)
    }
}
