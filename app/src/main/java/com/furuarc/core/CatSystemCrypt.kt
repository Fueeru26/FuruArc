package com.furuarc.core

import java.io.IOException
import java.nio.charset.Charset

/**
 * CatSystem2 encrypted INT crypto (port from msg-tool, scripts/cat_system/archive).
 * Index: first entry "__key__.dat"; MT seed = u32 at 0x4C; Blowfish key = first rand() (4 bytes LE);
 * the i-th entry name is scrambled with an MT seeded with (crc(password) + i); offset/size are Blowfish-encrypted.
 */
object CatSystemCrypt {
    private class Twister(seed: Int) {
        private val mt = IntArray(624)
        private var mti = 624
        init { sRand(seed) }
        fun sRand(seed0: Int) {
            var seed = seed0
            for (i in 0 until 624) {
                val upper = seed and -0x10000
                seed = seed * 69069 + 1
                mt[i] = upper or ((seed and -0x10000) ushr 16)
                seed = seed * 69069 + 1
            }
            mti = 624
        }
        fun rand(): Int {
            if (mti >= 624) {
                val mag = intArrayOf(0, -0x66f74f21) // 0x9908B0DF
                for (kk in 0 until 624 - 397) {
                    val y = (mt[kk] and Int.MIN_VALUE) or (mt[kk + 1] and 0x7fffffff)
                    mt[kk] = mt[kk + 397] xor (y ushr 1) xor mag[y and 1]
                }
                for (kk in 624 - 397 until 623) {
                    val y = (mt[kk] and Int.MIN_VALUE) or (mt[kk + 1] and 0x7fffffff)
                    mt[kk] = mt[kk - (624 - 397)] xor (y ushr 1) xor mag[y and 1]
                }
                val y = (mt[623] and Int.MIN_VALUE) or (mt[0] and 0x7fffffff)
                mt[623] = mt[396] xor (y ushr 1) xor mag[y and 1]
                mti = 0
            }
            var y = mt[mti++]
            y = y xor (y ushr 11)
            y = y xor ((y shl 7) and -0x62d3a980) // 0x9D2C5680
            y = y xor ((y shl 15) and -0x103a0000) // 0xEFC60000
            y = y xor (y ushr 18)
            return y
        }
    }

    private val CRC = IntArray(256).also { t ->
        for (i in 0 until 256) {
            var c = i shl 24
            for (k in 0 until 8) c = if (c and Int.MIN_VALUE != 0) (c shl 1) xor 0x04C11DB7 else c shl 1
            t[i] = c
        }
    }

    private val SJIS: Charset = try { Charset.forName("windows-31j") } catch (e: Exception) { Charsets.UTF_8 }

    /** testing: Blowfish key from the seed, and name key from the per-entry seed. */
    fun testKey(seed: Int): Int = Twister(seed).rand()
    fun testNameKey(seed: Int): Int = Twister(seed).rand()

    fun passwordKey(password: String): Int {
        var key = -1
        for (b in password.toByteArray(SJIS)) {
            key = CRC[((key ushr 24) xor (b.toInt() and 0xff)) and 0xff].inv() xor (key shl 8)
        }
        return key
    }

    private fun idx(c: Int): Int = if (c in 'a'.code..'z'.code) 'z'.code - c else 'Z'.code - c + 26
    private fun chr(i: Int): Int = if (i < 26) 'z'.code - i else 'Z'.code - (i - 26)
    private fun isAlpha(c: Int) = c in 'a'.code..'z'.code || c in 'A'.code..'Z'.code

    fun decryptName(raw: ByteArray, key: Int): ByteArray {
        var k = ((key ushr 24) + (key ushr 16) + (key ushr 8) + key) and 0xff
        var n = 0
        while (n < raw.size && raw[n].toInt() != 0) n++
        val out = raw.copyOf(n)
        for (i in 0 until n) {
            val v = out[i].toInt() and 0xff
            if (isAlpha(v)) {
                var j = idx(v) - (k % 0x34)
                if (j < 0) j += 0x34
                j = 0x33 - j
                out[i] = chr(j).toByte()
            }
            k++
        }
        return out
    }

    /** inverse of [decryptName] (used by tests and for building test archives). */
    fun encryptName(plain: ByteArray, key: Int): ByteArray {
        var k = ((key ushr 24) + (key ushr 16) + (key ushr 8) + key) and 0xff
        val out = plain.copyOf()
        for (i in out.indices) {
            val v = out[i].toInt() and 0xff
            if (isAlpha(v)) {
                val d = idx(v)
                val j = 0x33 - d
                val orig = (j + (k % 0x34)) % 0x34
                out[i] = chr(orig).toByte()
            }
            k++
        }
        return out
    }

    class Index(val cipher: Blowfish, val entries: List<Triple<String, Long, Long>>)

    /**
     * Read the encrypted index. [src] must start with "KIF\0" with "__key__.dat" as the first entry.
     * Returns null if the decrypted size/offset is implausible (wrong password).
     */
    fun readIndex(src: RandomSource, count: Int, password: String, cs: Charset): Index? {
        val key = passwordKey(password)
        val seedB = src.readFully(0x4C, 4)
        val seed = java.nio.ByteBuffer.wrap(seedB).order(java.nio.ByteOrder.LITTLE_ENDIAN).int
        val tw = Twister(seed)
        val k = tw.rand()
        val bf = Blowfish(byteArrayOf(k.toByte(), (k ushr 8).toByte(), (k ushr 16).toByte(), (k ushr 24).toByte()))
        val n = count - 1
        if (n < 0 || 0x50L + n.toLong() * 0x48 > src.length) return null
        val raw = src.readFully(0x50, n * 0x48)
        val bb = java.nio.ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        val out = ArrayList<Triple<String, Long, Long>>(n)
        for (e in 0 until n) {
            val i = e + 1
            val o = e * 0x48
            val off = bb.getInt(o + 0x40) + i
            val size = bb.getInt(o + 0x44)
            val d = bf.decrypt(off, size)
            tw.sRand(key + i)
            val nameKey = tw.rand()
            val name = String(decryptName(raw.copyOfRange(o, o + 0x40), nameKey), cs)
            val offL = d[0].toLong() and 0xffffffffL
            val sizeL = d[1].toLong() and 0xffffffffL
            if (!IoUtil.saneName(name) || offL + sizeL > src.length) return null
            out += Triple(name, offL, sizeL)
        }
        return Index(bf, out)
    }

    /** For testing: build an encrypted single-entry index (offset/size already known). */
    fun encryptIndexEntry(bf: Blowfish, i: Int, off: Int, size: Int): Pair<Int, Int> {
        val d = bf.encrypt(off, size)
        return Pair(d[0] - i, d[1])
    }

    // ---- password from the exe (PE32) ----
    private fun u16(b: ByteArray, o: Int) = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8)
    private fun u32(b: ByteArray, o: Int) = u16(b, o) or (u16(b, o + 2) shl 16)

    /** Find a nested resource named [type]/[name] in a PE; null if absent. */
    fun findResource(pe: ByteArray, type: String, name: String): ByteArray? {
        try {
            if (pe.size < 0x100 || pe[0] != 'M'.code.toByte() || pe[1] != 'Z'.code.toByte()) return null
            val pep = u32(pe, 0x3C)
            if (u32(pe, pep) != 0x4550) return null
            val nsec = u16(pe, pep + 6)
            val optSize = u16(pe, pep + 20)
            val opt = pep + 24
            val magic = u16(pe, opt)
            val ddOff = if (magic == 0x20b) opt + 112 else opt + 96
            val resRva = u32(pe, ddOff + 16)
            if (resRva == 0) return null
            val secs = opt + optSize
            fun rvaToOff(rva: Int): Int {
                for (s in 0 until nsec) {
                    val so = secs + s * 40
                    val va = u32(pe, so + 12)
                    val vs = maxOf(u32(pe, so + 8), u32(pe, so + 16))
                    val raw = u32(pe, so + 20)
                    if (rva >= va && rva < va + vs) return rva - va + raw
                }
                return -1
            }
            val base = rvaToOff(resRva)
            if (base < 0) return null
            fun nameAt(rel: Int): String {
                val o = base + (rel and 0x7fffffff)
                val n = u16(pe, o)
                return String(pe, o + 2, n * 2, Charsets.UTF_16LE)
            }
            // return the child directory offset (relative to base) for the named entry
            fun child(dirRel: Int, want: String?): Int {
                val d = base + dirRel
                val nn = u16(pe, d + 12)
                val ni = u16(pe, d + 14)
                for (k in 0 until nn + ni) {
                    val e = d + 16 + k * 8
                    val id = u32(pe, e)
                    val tgt = u32(pe, e + 4)
                    val match = if (want == null) true
                        else if (want.startsWith("#")) (k >= nn && id == want.substring(1).toIntOrNull())
                        else (k < nn && id < 0 && nameAt(id).equals(want, true))
                    if (match) return tgt
                }
                return -1
            }
            val t1 = child(0, type)
            if (t1 == -1 || t1 >= 0) { /* t1 bit31 = subdir */ }
            if (t1 == -1) return null
            val d1 = t1 and 0x7fffffff
            val t2 = child(d1, name)
            if (t2 == -1) return null
            val d2 = t2 and 0x7fffffff
            val t3 = child(d2, null)
            if (t3 == -1) return null
            val de = base + (t3 and 0x7fffffff)
            val rva = u32(pe, de)
            val size = u32(pe, de + 4)
            val off = rvaToOff(rva)
            if (off < 0 || size < 0 || off + size > pe.size) return null
            return pe.copyOfRange(off, off + size)
        } catch (e: Exception) {
            return null
        }
    }

    /** Get the INT password from the CatSystem2 exe (resource V_CODE2/DATA, key KEY_CODE/KEY ^ 0xCD, default "windmill"). */
    fun passwordFromExe(pe: ByteArray): String? {
        val code = findResource(pe, "V_CODE2", "DATA") ?: return null
        if (code.size < 8) return null
        val key = findResource(pe, "KEY_CODE", "KEY")?.let { k -> ByteArray(k.size) { (k[it].toInt() xor 0xCD).toByte() } }
            ?: "windmill".toByteArray()
        if (key.size < 4 || key.size > 56) return null
        Blowfish(key).decryptBlocks(code, 0, code.size, true)
        var n = 0
        while (n < code.size && code[n].toInt() != 0) n++
        if (n == 0) return null
        return String(code, 0, n, SJIS)
    }
}
