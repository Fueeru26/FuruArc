package com.furuarc.core

import java.io.IOException

/**
 * Decompressors used by UnityFS bundles.
 * [Lzma]: raw LZMA1 stream (5-byte properties, no size header; the size comes from the block table).
 * [Lz4]: LZ4 block format (LZ4 and LZ4HC use the same decoder).
 */
object Lzma {
    private const val TOP = 1L shl 24
    private const val MASK = 0xFFFFFFFFL

    private class Rc(val d: ByteArray, var p: Int, val end: Int) {
        var range = MASK
        var code = 0L

        init {
            if (end - p < 5) throw IOException("Data LZMA terlalu pendek")
            p++ // first byte is always 0
            for (i in 0 until 4) code = (code shl 8) or (d[p++].toLong() and 0xFF)
        }

        private fun norm() {
            if (range < TOP) {
                range = (range shl 8) and MASK
                val b = if (p < end) d[p].toLong() and 0xFF else 0L
                p++
                code = ((code shl 8) or b) and MASK
            }
        }

        fun bit(pr: IntArray, i: Int): Int {
            val v = pr[i]
            val bound = (range ushr 11) * v
            val r: Int
            if (code < bound) {
                range = bound
                pr[i] = v + ((2048 - v) ushr 5)
                r = 0
            } else {
                range -= bound
                code -= bound
                pr[i] = v - (v ushr 5)
                r = 1
            }
            norm()
            return r
        }

        fun direct(n: Int): Long {
            var res = 0L
            for (k in 0 until n) {
                range = range ushr 1
                var b = 0L
                if (code >= range) { code -= range; b = 1L }
                res = (res shl 1) or b
                norm()
            }
            return res
        }

        fun tree(pr: IntArray, base: Int, nb: Int): Int {
            var m = 1
            for (k in 0 until nb) m = (m shl 1) or bit(pr, base + m)
            return m - (1 shl nb)
        }

        fun rtree(pr: IntArray, base: Int, nb: Int): Int {
            var m = 1
            var s = 0
            for (k in 0 until nb) {
                val b = bit(pr, base + m)
                m = (m shl 1) or b
                s = s or (b shl k)
            }
            return s
        }
    }

    private class Len {
        val c = IntArray(1) { 1024 }
        val c2 = IntArray(1) { 1024 }
        val low = IntArray(16 * 8) { 1024 }
        val mid = IntArray(16 * 8) { 1024 }
        val high = IntArray(256) { 1024 }
        fun dec(rc: Rc, ps: Int): Int {
            if (rc.bit(c, 0) == 0) return rc.tree(low, ps shl 3, 3)
            if (rc.bit(c2, 0) == 0) return 8 + rc.tree(mid, ps shl 3, 3)
            return 16 + rc.tree(high, 0, 8)
        }
    }

    /** Decode [outSize] bytes. [inp] starts with the 5-byte properties at [off]. */
    fun decode(inp: ByteArray, off: Int, len: Int, outSize: Int): ByteArray {
        if (len < 5 + 5) throw IOException("Data LZMA terlalu pendek")
        var d = inp[off].toInt() and 0xFF
        if (d >= 9 * 5 * 5) throw IOException("Properti LZMA tidak valid")
        val lc = d % 9; d /= 9
        val lp = d % 5
        val pb = d / 5
        val out = ByteArray(outSize)
        val rc = Rc(inp, off + 5, off + len)
        val isM = IntArray(12 * 16) { 1024 }
        val isR = IntArray(12) { 1024 }
        val g0 = IntArray(12) { 1024 }
        val g1 = IntArray(12) { 1024 }
        val g2 = IntArray(12) { 1024 }
        val r0l = IntArray(12 * 16) { 1024 }
        val posSlot = IntArray(4 * 64) { 1024 }
        val posDec = IntArray(115) { 1024 }
        val align = IntArray(16) { 1024 }
        val ln = Len()
        val rl = Len()
        val lit = IntArray(0x300 shl (lc + lp)) { 1024 }
        var state = 0
        var r0 = 0L; var r1 = 0L; var r2 = 0L; var r3 = 0L
        var pos = 0
        val pbMask = (1 shl pb) - 1
        val lpMask = (1 shl lp) - 1
        while (pos < outSize) {
            val ps = pos and pbMask
            if (rc.bit(isM, (state shl 4) + ps) == 0) {
                val prev = if (pos > 0) out[pos - 1].toInt() and 0xFF else 0
                val base = 0x300 * (((pos and lpMask) shl lc) + (prev ushr (8 - lc)))
                var sym = 1
                if (state >= 7) {
                    if (pos - r0 - 1 < 0) throw IOException("Data LZMA rusak")
                    var mb = out[(pos - r0 - 1).toInt()].toInt() and 0xFF
                    while (sym < 0x100) {
                        val m = (mb ushr 7) and 1
                        mb = (mb shl 1) and 0xFF
                        val b = rc.bit(lit, base + ((1 + m) shl 8) + sym)
                        sym = (sym shl 1) or b
                        if (m != b) break
                    }
                }
                while (sym < 0x100) sym = (sym shl 1) or rc.bit(lit, base + sym)
                out[pos++] = sym.toByte()
                state = if (state < 4) 0 else if (state < 10) state - 3 else state - 6
                continue
            }
            var l: Int
            if (rc.bit(isR, state) == 0) {
                r3 = r2; r2 = r1; r1 = r0
                l = ln.dec(rc, ps)
                state = if (state < 7) 7 else 10
                val slot = rc.tree(posSlot, minOf(l, 3) shl 6, 6)
                if (slot < 4) r0 = slot.toLong()
                else {
                    val nd = (slot ushr 1) - 1
                    r0 = (2L or (slot and 1).toLong()) shl nd
                    if (slot < 14) r0 += rc.rtree(posDec, (r0 - slot).toInt(), nd).toLong()
                    else {
                        r0 += rc.direct(nd - 4) shl 4
                        r0 += rc.rtree(align, 0, 4).toLong()
                    }
                }
                if (r0 == MASK) break
            } else {
                if (rc.bit(g0, state) == 0) {
                    if (rc.bit(r0l, (state shl 4) + ps) == 0) {
                        state = if (state < 7) 9 else 11
                        if (pos - r0 - 1 < 0) throw IOException("Data LZMA rusak")
                        out[pos] = out[(pos - r0 - 1).toInt()]; pos++
                        continue
                    }
                } else {
                    val dist: Long
                    if (rc.bit(g1, state) == 0) dist = r1
                    else {
                        if (rc.bit(g2, state) == 0) dist = r2
                        else { dist = r3; r3 = r2 }
                        r2 = r1
                    }
                    r1 = r0; r0 = dist
                }
                l = rl.dec(rc, ps)
                state = if (state < 7) 8 else 11
            }
            l += 2
            if (pos - r0 - 1 < 0) throw IOException("Data LZMA rusak")
            val src0 = (pos - r0 - 1).toInt()
            var k = 0
            while (k < l && pos < outSize) { out[pos] = out[src0 + k]; pos++; k++ }
        }
        if (pos < outSize) throw IOException("Data LZMA berakhir lebih awal")
        return out
    }
}

object Lz4 {
    /** Decode one LZ4 block into exactly [outSize] bytes. */
    fun decode(inp: ByteArray, off: Int, len: Int, outSize: Int): ByteArray {
        val out = ByteArray(outSize)
        var ip = off
        val end = off + len
        var op = 0
        while (ip < end) {
            val tok = inp[ip++].toInt() and 0xFF
            var lit = tok ushr 4
            if (lit == 15) {
                while (true) {
                    if (ip >= end) throw IOException("Data LZ4 terpotong")
                    val b = inp[ip++].toInt() and 0xFF
                    lit += b
                    if (b != 255) break
                }
            }
            if (ip + lit > end || op + lit > outSize) throw IOException("Data LZ4 rusak")
            System.arraycopy(inp, ip, out, op, lit)
            ip += lit; op += lit
            if (ip >= end) break
            if (ip + 2 > end) throw IOException("Data LZ4 terpotong")
            val dist = (inp[ip].toInt() and 0xFF) or ((inp[ip + 1].toInt() and 0xFF) shl 8)
            ip += 2
            var ml = tok and 15
            if (ml == 15) {
                while (true) {
                    if (ip >= end) throw IOException("Data LZ4 terpotong")
                    val b = inp[ip++].toInt() and 0xFF
                    ml += b
                    if (b != 255) break
                }
            }
            ml += 4
            if (dist == 0 || dist > op || op + ml > outSize) throw IOException("Data LZ4 rusak")
            var s = op - dist
            for (k in 0 until ml) out[op++] = out[s++]
        }
        if (op != outSize) throw IOException("Ukuran hasil LZ4 tidak cocok")
        return out
    }
}
