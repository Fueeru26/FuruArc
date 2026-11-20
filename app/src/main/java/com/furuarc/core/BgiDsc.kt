package com.furuarc.core

import java.io.IOException

/** BGI/Ethornell "DSC FORMAT 1.00": Huffman + LZ (port of msg-tool, archive/dsc.rs). Decode only; [encodeSimple] is for testing. */
object BgiDsc {
    private const val MAGIC = "DSC FORMAT 1.00"

    fun isDsc(b: ByteArray) = b.size >= 0x20 && String(b, 0, 15, Charsets.US_ASCII) == MAGIC

    private class Keyer(var key: Int, val magic: Int) {
        fun next(): Int {
            val v0 = 20021 * (key and 0xffff)
            var v1 = magic or (key ushr 16)
            v1 = v1 * 20021 + key * 346
            v1 = (v1 + (v0 ushr 16)) and 0xffff
            key = (v1 shl 16) + (v0 and 0xffff) + 1
            return v1 and 0xff
        }
    }

    fun decode(d: ByteArray): ByteArray {
        if (!isDsc(d)) throw IOException("Bukan DSC")
        val bb = d.le()
        val magic = ((d[0].toInt() and 0xff) or ((d[1].toInt() and 0xff) shl 8)) shl 16
        val keyer = Keyer(bb.getInt(0x10), magic)
        val outSize = bb.getInt(0x14)
        val decCount = bb.getInt(0x18)
        if (outSize < 0 || outSize > 512 * 1024 * 1024 || decCount < 0 || d.size < 0x220) throw IOException("Header DSC tidak valid")
        // code list (code, depth), sorted by (depth, code)
        val codes = ArrayList<IntArray>()
        for (i in 0 until 512) {
            val depth = ((d[0x20 + i].toInt() and 0xff) - keyer.next()) and 0xff
            if (depth > 0) codes.add(intArrayOf(i, depth))
        }
        codes.sortWith(compareBy({ it[1] }, { it[0] }))
        // tree
        val isParent = BooleanArray(1024); val code = IntArray(1024) { -1 }
        val left = IntArray(1024); val right = IntArray(1024)
        val li = IntArray(512); val ri = IntArray(512)
        var next = 1; var depthNodes = 1; var depth = 0; var leftChild = true; var n = 0
        while (n < codes.size) {
            val useLeft = leftChild; leftChild = !leftChild
            var existed = 0
            while (n < codes.size && codes[n][1] == depth) {
                val idx = if (useLeft) li[existed] else ri[existed]
                code[idx] = codes[n][0]
                n++; existed++
            }
            val create = depthNodes - existed
            if (create < 0) throw IOException("Tabel Huffman DSC tidak valid")
            for (i in 0 until create) {
                val idx = if (useLeft) li[existed + i] else ri[existed + i]
                if (next + 2 > 1024) throw IOException("Tabel Huffman DSC tidak valid")
                isParent[idx] = true
                if (leftChild) {
                    li[i * 2] = next; left[idx] = next++
                    li[i * 2 + 1] = next; right[idx] = next++
                } else {
                    ri[i * 2] = next; left[idx] = next++
                    ri[i * 2 + 1] = next; right[idx] = next++
                }
            }
            depth++; depthNodes = create * 2
        }
        val out = ByteArray(outSize)
        var dst = 0
        var pos = 0x220; var bit = 0; var cur = 0
        fun getBit(): Int {
            if (bit == 0) { if (pos >= d.size) throw IOException("DSC terpotong"); cur = d[pos++].toInt() and 0xff; bit = 8 }
            bit--
            return (cur ushr bit) and 1
        }
        fun bits(c: Int): Int { var v = 0; repeat(c) { v = (v shl 1) or getBit() }; return v }
        for (k in 0 until decCount) {
            var node = 0
            do { node = if (getBit() == 0) left[node] else right[node] } while (isParent[node])
            val c = code[node]
            if (c < 0) throw IOException("Kode DSC tidak valid")
            if (c >= 256) {
                val off = bits(12) + 2
                val cnt = (c and 0xff) + 2
                if (dst - off < 0 || dst + cnt > outSize) throw IOException("Referensi DSC tidak valid")
                for (j in 0 until cnt) out[dst + j] = out[dst - off + j]
                dst += cnt
            } else {
                if (dst >= outSize) throw IOException("DSC melebihi ukuran")
                out[dst++] = c.toByte()
            }
        }
        return out
    }

    /** Simple encoding (all symbols at depth 9, literals only) for round-trip tests. */
    fun encodeSimple(data: ByteArray, key: Int = 0x1234abcd): ByteArray {
        val hdr = ByteArray(0x20 + 512)
        MAGIC.toByteArray().copyInto(hdr)
        val bb = java.nio.ByteBuffer.wrap(hdr).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        bb.putInt(0x10, key); bb.putInt(0x14, data.size); bb.putInt(0x18, data.size)
        val kr = Keyer(key, ((hdr[0].toInt() and 0xff) or ((hdr[1].toInt() and 0xff) shl 8)) shl 16)
        for (i in 0 until 512) hdr[0x20 + i] = (9 + kr.next()).toByte()
        val bo = java.io.ByteArrayOutputStream(); bo.write(hdr)
        var acc = 0; var nb = 0
        for (b in data) {
            val v = b.toInt() and 0xff
            for (i in 8 downTo 0) { acc = (acc shl 1) or ((v ushr i) and 1); nb++; if (nb == 8) { bo.write(acc); acc = 0; nb = 0 } }
        }
        if (nb > 0) bo.write(acc shl (8 - nb))
        return bo.toByteArray()
    }
}
