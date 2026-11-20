package com.furuarc.core

/** Port lz-string (compressToBase64 / decompressFromBase64), dipakai save RPGM MV. */
object LzString {
    private const val KEY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

    fun compressToBase64(input: String): String {
        val res = compress(input, 6)
        return when (res.length % 4) {
            1 -> "$res==="
            2 -> "$res=="
            3 -> "$res="
            else -> res
        }
    }

    fun decompressFromBase64(input: String): String? {
        val s = input.replace(' ', '+')
        if (s.isEmpty()) return null
        return decompress(s.length, 32) { idx -> if (idx < s.length) KEY.indexOf(s[idx]).let { if (it < 0) 0 else it } else 0 }
    }

    private class BitOut(val bitsPerChar: Int) {
        val sb = StringBuilder()
        var v = 0
        var pos = 0
        fun bit(b: Int) {
            v = (v shl 1) or b
            if (pos == bitsPerChar - 1) {
                pos = 0
                sb.append(KEY[v])
                v = 0
            } else pos++
        }
        fun bits(value: Int, n: Int) {
            var x = value
            for (k in 0 until n) { bit(x and 1); x = x shr 1 }
        }
        fun flush() {
            while (true) {
                v = v shl 1
                if (pos == bitsPerChar - 1) { sb.append(KEY[v]); break } else pos++
            }
        }
    }

    private fun compress(input: String, bitsPerChar: Int): String {
        if (input.isEmpty()) return ""
        val dict = HashMap<String, Int>()
        val toCreate = HashSet<String>()
        var w = ""
        var enlargeIn = 2
        var dictSize = 3
        var numBits = 2
        val out = BitOut(bitsPerChar)

        fun grow() {
            enlargeIn--
            if (enlargeIn == 0) {
                enlargeIn = 1 shl numBits
                numBits++
            }
        }

        fun emitW(word: String) {
            if (toCreate.contains(word)) {
                val ch = word[0].code
                if (ch < 256) {
                    out.bits(0, numBits)
                    out.bits(ch, 8)
                } else {
                    out.bits(1, numBits)
                    out.bits(ch, 16)
                }
                grow()
                toCreate.remove(word)
            } else {
                out.bits(dict[word]!!, numBits)
            }
            grow()
        }

        for (c in input) {
            val cs = c.toString()
            if (!dict.containsKey(cs)) {
                dict[cs] = dictSize++
                toCreate.add(cs)
            }
            val wc = w + cs
            if (dict.containsKey(wc)) {
                w = wc
            } else {
                emitW(w)
                dict[wc] = dictSize++
                w = cs
            }
        }
        if (w.isNotEmpty()) emitW(w)
        out.bits(2, numBits) // penanda akhir
        out.flush()
        return out.sb.toString()
    }

    private fun decompress(length: Int, resetValue: Int, next: (Int) -> Int): String? {
        val dict = ArrayList<String?>()
        var enlargeIn = 4
        var dictSize = 4
        var numBits = 3
        var index = 1
        var v = next(0)
        var position = resetValue
        for (i in 0 until 3) dict.add(i.toString())

        fun readBits(n: Int): Int {
            var bits = 0
            var power = 1
            val max = 1 shl n
            while (power != max) {
                val resb = v and position
                position = position shr 1
                if (position == 0) {
                    position = resetValue
                    v = next(index++)
                }
                if (resb > 0) bits = bits or power
                power = power shl 1
            }
            return bits
        }

        val first = when (readBits(2)) {
            0 -> readBits(8).toChar().toString()
            1 -> readBits(16).toChar().toString()
            else -> return ""
        }
        dict.add(first) // indeks 3
        var w = first
        val result = StringBuilder(first)
        while (true) {
            if (index > length) return ""
            var c = readBits(numBits)
            when (c) {
                0 -> {
                    dict.add(readBits(8).toChar().toString()); dictSize++
                    c = dictSize - 1
                    enlargeIn--
                }
                1 -> {
                    dict.add(readBits(16).toChar().toString()); dictSize++
                    c = dictSize - 1
                    enlargeIn--
                }
                2 -> return result.toString()
            }
            if (enlargeIn == 0) {
                enlargeIn = 1 shl numBits
                numBits++
            }
            val entry: String = if (c < dict.size && dict[c] != null) {
                dict[c]!!
            } else if (c == dictSize) {
                w + w[0]
            } else {
                return null
            }
            result.append(entry)
            dict.add(w + entry[0]); dictSize++
            enlargeIn--
            w = entry
            if (enlargeIn == 0) {
                enlargeIn = 1 shl numBits
                numBits++
            }
        }
    }
}
