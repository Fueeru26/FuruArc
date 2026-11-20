package com.furuarc.core

import java.io.IOException
import java.io.ByteArrayOutputStream

/**
 * SiglusEngine codec library (format reference: SiglusSceneScriptUtility by Jirehlov, MIT license; GalGameTools/SiglusEngine).
 * - Scene.pck scene data: [XOR with 16-byte exe key if the game uses it] -> XOR [EASY] -> LZSS.
 * - Gameexe.dat: 8-byte head (0, mode) + XOR [GEI] payload (+ exe key if mode != 0) -> LZSS -> UTF-16LE text.
 * LZSS = [compressed size u32][original size u32] then an 8-bit flag stream (1 = literal, 0 = 16-bit token: 12-bit offset, 4-bit length + 2).
 */
object SiglusCodec {
    private fun hex(s: String): ByteArray = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    val EASY: ByteArray by lazy {
        hex(
        "70f8a6b0a1a5284fb52f48fae1e94bdeb74f62958be00380e7cf0f6b9201ebf8" +
        "a288ce630438d26d8cd28876a792718f4eb68d017988830af9e92cdb67db9114" +
        "d59a4e79172308960e1d15f9a5a06f5817c8a946da22fffd871242fba9b8676c" +
        "916764f9d11ee450646ff20bde40e747f103cc2aad7f3421a06426986ced69f4" +
        "b523086e7d92f6eb93f07a895ef9f87aafe8a948c2ac116b2b33a7400ddc7da7" +
        "5bcfc831d177528d82ac41b873a54f267c0f39da5b374adea4490b7c17a343ae" +
        "77066473c043a3185a0f9f024c7e8b019f2dae725413ff96ae0b3458cfe30078" +
        "bee3f561e4877cfc80afc48d463a5dd036bce5607768084fbbabe27807e873bf"
        )
    }

    val GEI: ByteArray by lazy {
        hex(
        "d829b9163d1a76d0879b2d0c7bd1a919229f91736a35b17ed1b5e7e6d5f506d6" +
        "babff3453ff161dd4c676a6f74ec7a6f26740edb274ca5f10e2d70c4405d4fda" +
        "9ec5497bbde8dfeecaf492dee47610dd2a52dc734e548c303d9ab29bb8932955" +
        "fa7ac9da1097e5b62302dd384c9b1f9ad549e9340f282d1b52395c368956a796" +
        "14be2ec53e085f47a9df889fd4cc691f309fe7cd8045f3e72a1d16b2f154c86c" +
        "2b0dd465f7e336d4a53bd1794c54f02ab4b256452eab7b88c5fa74ad03b89ed5" +
        "f56fdcfa444931f68332ffc2b1e9e1983d6f310dacb108839d0d10d141f900ba" +
        "1acf1371e486212f2365c345a0c392489deadd312ce9e21022aae1ad2cc42d7f"
        )
    }

    private val EXE_ORG = hex("3659c9732eb509bae44cf26aa234ec7c")

    fun xorCycle(data: ByteArray, code: ByteArray, start: Int = 0) {
        if (code.isEmpty()) return
        var k = start % code.size
        for (i in data.indices) {
            data[i] = (data[i].toInt() xor code[k].toInt()).toByte()
            k++
            if (k == code.size) k = 0
        }
    }

    fun xorCycleCopy(data: ByteArray, code: ByteArray): ByteArray = data.copyOf().also { xorCycle(it, code) }

    /** Exe key (16 bytes) from the 暗号 text (cp932, at least 8 bytes): cyclic XOR against the base table. */
    fun exeElFromAngou(text: String): ByteArray {
        val b = text.trim().toByteArray(java.nio.charset.Charset.forName("windows-31j"))
        if (b.size < 8) throw IOException("Teks 暗号 minimal 8 byte")
        val r = EXE_ORG.copyOf()
        val cnt = if (b.size < r.size) r.size else b.size
        var a = 0
        var d = 0
        for (i in 0 until cnt) {
            r[d] = (r[d].toInt() xor b[a].toInt()).toByte()
            a++; d++
            if (a == b.size) a = 0
            if (d == r.size) d = 0
        }
        return r
    }

    /** Read the key from the option: 16 hex bytes (may be "0x12, 0x34, ..." or "12 34 ...") or 暗号 text. Empty -> null. */
    fun parseKey(text: String): ByteArray? {
        val t = text.trim()
        if (t.isEmpty()) return null
        val h = Regex("0[xX]([0-9a-fA-F]{2})").findAll(t).map { it.groupValues[1] }.toList()
        if (h.size >= 16) return ByteArray(16) { h[it].toInt(16).toByte() }
        val plain = t.filter { !it.isWhitespace() && it != ',' && it != ';' }
        if (plain.length == 32 && plain.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            return ByteArray(16) { plain.substring(it * 2, it * 2 + 2).toInt(16).toByte() }
        }
        val h2 = Regex("\\b([0-9a-fA-F]{2})\\b").findAll(t).map { it.groupValues[1] }.toList()
        if (h2.size >= 16) return ByteArray(16) { h2[it].toInt(16).toByte() }
        return exeElFromAngou(t)
    }

    fun looksLikeLzss(b: ByteArray): Boolean {
        if (b.size < 8) return false
        val packed = b.le().getInt(0).toLong() and 0xffffffffL
        val org = b.le().getInt(4).toLong() and 0xffffffffL
        return packed == b.size.toLong() && org in 1..0x40000000L
    }

    fun lzssUnpack(src: ByteArray): ByteArray {
        if (src.isEmpty()) return src
        if (src.size < 8) throw IOException("LZSS terlalu pendek")
        val org = (src.le().getInt(4).toLong() and 0xffffffffL)
        if (org == 0L) return ByteArray(0)
        if (org > 0x40000000L) throw IOException("LZSS: ukuran asli tidak wajar")
        val out = ByteArray(org.toInt())
        var o = 0
        var si = 8
        while (o < out.size) {
            if (si >= src.size) throw IOException("LZSS: data habis")
            var fl = src[si++].toInt() and 0xff
            for (bit in 0 until 8) {
                if (o >= out.size) break
                if (fl and 1 != 0) {
                    if (si >= src.size) throw IOException("LZSS: data habis")
                    out[o++] = src[si++]
                } else {
                    if (si + 2 > src.size) throw IOException("LZSS: data habis")
                    val tok = (src[si].toInt() and 0xff) or ((src[si + 1].toInt() and 0xff) shl 8)
                    si += 2
                    val off = tok ushr 4
                    val len = (tok and 0xf) + 2
                    if (off == 0 || off > o) throw IOException("LZSS: referensi mundur tidak valid")
                    var st = o - off
                    for (j in 0 until len) {
                        if (o >= out.size) break
                        out[o++] = out[st++]
                    }
                }
                fl = fl ushr 1
            }
        }
        return out
    }

    /** LZSS compression (greedy matching with a hash chain). The result is valid for the game decoder, not necessarily byte-for-byte identical to the original. */
    fun lzssPack(src: ByteArray): ByteArray {
        val bo = ByteArrayOutputStream(src.size / 2 + 16)
        bo.write(ByteArray(8))
        val n = src.size
        val head = IntArray(1 shl 14) { -1 }
        val prev = IntArray(maxOf(n, 1)) { -1 }
        fun h(i: Int) = (((src[i].toInt() and 0xff) shl 6) xor ((src[i + 1].toInt() and 0xff) shl 3) xor (src[i + 2].toInt() and 0xff)) and 0x3fff
        fun insert(i: Int) {
            if (i + 2 < n) { val k = h(i); prev[i] = head[k]; head[k] = i }
        }
        var i = 0
        val group = ByteArray(1 + 16)
        while (i < n) {
            var flags = 0
            var gl = 1
            var items = 0
            while (items < 8 && i < n) {
                var bestLen = 0
                var bestOff = 0
                if (i + 2 < n) {
                    var cand = head[h(i)]
                    var depth = 0
                    while (cand >= 0 && depth < 48) {
                        val off = i - cand
                        if (off > 0xfff) break
                        var l = 0
                        val maxL = minOf(17, n - i)
                        while (l < maxL && src[cand + l] == src[i + l]) l++
                        if (l > bestLen) { bestLen = l; bestOff = off; if (l == maxL) break }
                        cand = prev[cand]
                        depth++
                    }
                }
                if (bestLen >= 3) {
                    val tok = (bestOff shl 4) or (bestLen - 2)
                    group[gl++] = tok.toByte()
                    group[gl++] = (tok ushr 8).toByte()
                    for (k in 0 until bestLen) insert(i + k)
                    i += bestLen
                } else {
                    flags = flags or (1 shl items)
                    group[gl++] = src[i]
                    insert(i)
                    i++
                }
                items++
            }
            group[0] = flags.toByte()
            bo.write(group, 0, gl)
        }
        val out = bo.toByteArray()
        val bb = out.le()
        bb.putInt(0, out.size)
        bb.putInt(4, n)
        return out
    }

    /** Decode one Scene.pck scene blob -> .dat contents. [mod] = scn_data_exe_angou_mod. */
    fun decodeScene(blob: ByteArray, mod: Int, exeEl: ByteArray?): ByteArray {
        var b = blob
        if (mod != 0) {
            if (exeEl == null) throw IOException("Scene.pck ini terenkripsi dengan kunci exe. Isi opsi Kunci (hex 16 byte atau teks 暗号).")
            b = xorCycleCopy(b, exeEl)
        }
        val cand = xorCycleCopy(b, EASY)
        val lz = when {
            looksLikeLzss(cand) -> cand
            looksLikeLzss(b) -> b
            else -> null
        }
        if (lz == null) {
            if (mod != 0) throw IOException("Kunci exe salah atau data scene rusak")
            return b
        }
        return lzssUnpack(lz)
    }

    fun encodeScene(dat: ByteArray, mod: Int, exeEl: ByteArray?): ByteArray {
        if (mod != 0 && exeEl == null) throw IOException("Scene.pck ini terenkripsi dengan kunci exe. Isi opsi Kunci (hex 16 byte atau teks 暗号).")
        val lz = lzssPack(dat)
        xorCycle(lz, EASY)
        if (mod != 0) xorCycle(lz, exeEl!!)
        return lz
    }
}
