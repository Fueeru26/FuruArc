package com.furuarc.core

import java.io.IOException
import java.io.ByteArrayOutputStream

/**
 * Codec for the NeXAS PAC archive index: MSB-first bit Huffman (tree stored in preorder: 1 = inner node [left, right],
 * 0 = leaf + 8 symbol bits; walk 0 = left, 1 = right). The index stored at the end of the archive is also XOR-ed with 0xFF.
 * Follows the zbspac/GARbro summary; not yet tested on real game archives.
 */
object NexasCodec {
    private class Node(val sym: Int, val left: Node?, val right: Node?, val freq: Long = 0)

    private class BitIn(val b: ByteArray, var pos: Int = 0, val end: Int = b.size) {
        var cur = 0; var left = 0
        fun bit(): Int {
            if (left == 0) {
                if (pos >= end) throw IOException("Data Huffman NeXAS habis")
                cur = b[pos++].toInt() and 0xFF; left = 8
            }
            left--
            return (cur shr left) and 1
        }
        fun bits(n: Int): Int { var v = 0; repeat(n) { v = (v shl 1) or bit() }; return v }
    }

    private class BitOut {
        val bo = ByteArrayOutputStream()
        var cur = 0; var n = 0
        fun bit(v: Int) { cur = (cur shl 1) or (v and 1); n++; if (n == 8) { bo.write(cur); cur = 0; n = 0 } }
        fun bits(v: Int, c: Int) { for (i in c - 1 downTo 0) bit((v shr i) and 1) }
        fun finish(): ByteArray { while (n != 0) bit(0); return bo.toByteArray() }
    }

    private fun readTree(r: BitIn, depth: Int, leaves: IntArray): Node {
        if (depth > 300 || leaves[0] > 512) throw IOException("Pohon Huffman NeXAS tidak valid")
        return if (r.bit() == 1) {
            val l = readTree(r, depth + 1, leaves)
            val rt = readTree(r, depth + 1, leaves)
            Node(-1, l, rt)
        } else { leaves[0]++; Node(r.bits(8), null, null) }
    }

    /** Decode [outLen] bytes from [data] (offset [off], length [len]). */
    fun huffDecode(data: ByteArray, off: Int, len: Int, outLen: Int): ByteArray {
        val r = BitIn(data, off, off + len)
        val root = readTree(r, 0, IntArray(1))
        val out = ByteArray(outLen)
        if (root.left == null) { java.util.Arrays.fill(out, root.sym.toByte()); return out }
        for (i in 0 until outLen) {
            var n = root
            while (n.left != null) n = if (r.bit() == 0) n.left!! else n.right!!
            out[i] = n.sym.toByte()
        }
        return out
    }

    fun huffEncode(src: ByteArray): ByteArray {
        val freq = LongArray(256)
        for (b in src) freq[b.toInt() and 0xFF]++
        val q = java.util.PriorityQueue<Node>(compareBy<Node> { it.freq }.thenBy { it.sym })
        for (s in 0 until 256) if (freq[s] > 0) q.add(Node(s, null, null, freq[s]))
        if (q.size == 1) q.add(Node(if (q.peek().sym == 0) 1 else 0, null, null, 0))
        if (q.isEmpty()) q.add(Node(0, null, null, 0)).also { q.add(Node(1, null, null, 0)) }
        while (q.size > 1) {
            val a = q.poll(); val b = q.poll()
            q.add(Node(-1, a, b, a.freq + b.freq))
        }
        val root = q.poll()
        val codes = arrayOfNulls<Pair<Int, Int>>(256)
        val w = BitOut()
        fun walk(n: Node, code: Int, len: Int) {
            if (n.left == null) { codes[n.sym] = Pair(code, len); w.bit(0); w.bits(n.sym, 8) }
            else { w.bit(1); walk(n.left, code shl 1, len + 1); walk(n.right!!, (code shl 1) or 1, len + 1) }
        }
        walk(root, 0, 0)
        for (b in src) { val c = codes[b.toInt() and 0xFF]!!; w.bits(c.first, c.second) }
        return w.finish()
    }

    fun xorFF(b: ByteArray): ByteArray { for (i in b.indices) b[i] = (b[i].toInt() xor 0xFF).toByte(); return b }
}
