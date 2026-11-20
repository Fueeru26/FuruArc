package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * PSB reader/writer (Kirikiri .scn / E-mote), versions 3 and 4 (40/44-byte header).
 * The value tree is read fully then rewritten (string table rebuilt); the name table and data chunks are copied as is,
 * so key and resource indices stay the same. The header checksum (if any) is kept as in the original.
 */
object Psb {
    sealed class N
    object Nul : N()
    class Bool(val v: Boolean) : N()
    class I(val v: Long) : N()
    class F(val v: Double, val type: Int) : N()
    class S(var v: String) : N()
    class Chunk(val index: Long) : N()
    class IntArr(val v: LongArray, val es: Int) : N()
    class L(val items: MutableList<N>) : N()
    class D(val keys: IntArray, val vals: MutableList<N>) : N()

    class Doc(val head: ByteArray, val namesRaw: ByteArray, val chunkRaw: ByteArray, val chunkLenOff: Int, val chunkDataOff: Int,
              val names: List<String>, val root: N)

    fun isPsb(d: ByteArray) = d.size >= 44 && d[0] == 'P'.code.toByte() && d[1] == 'S'.code.toByte() && d[2] == 'B'.code.toByte() && d[3].toInt() == 0

    fun isMdf(d: ByteArray) = d.size >= 8 && d[0] == 'm'.code.toByte() && d[1] == 'd'.code.toByte() && d[2] == 'f'.code.toByte() && d[3].toInt() == 0

    /** mdf = "mdf\0" + u32 original size + zlib stream. */
    fun unmdf(d: ByteArray): ByteArray {
        val size = d.le().getInt(4)
        if (size < 0 || size > 512 * 1024 * 1024) throw IOException("Ukuran mdf tidak valid")
        val inf = Inflater()
        inf.setInput(d, 8, d.size - 8)
        val out = ByteArray(size)
        var n = 0
        while (n < size) {
            val k = inf.inflate(out, n, size - n)
            if (k == 0 && (inf.finished() || inf.needsInput())) break
            n += k
        }
        inf.end()
        if (n != size) throw IOException("Dekompresi mdf gagal")
        return out
    }

    fun mdf(d: ByteArray): ByteArray {
        val def = Deflater(9)
        def.setInput(d); def.finish()
        val bo = ByteArrayOutputStream()
        bo.write(byteArrayOf('m'.code.toByte(), 'd'.code.toByte(), 'f'.code.toByte(), 0))
        bo.write(ByteArray(4).also { it.le().putInt(0, d.size) })
        val buf = ByteArray(65536)
        while (!def.finished()) { val k = def.deflate(buf); bo.write(buf, 0, k) }
        def.end()
        return bo.toByteArray()
    }

    private class R(val d: ByteArray) {
        fun u(p: Int, n: Int): Long { var v = 0L; for (i in n - 1 downTo 0) v = (v shl 8) or (d[p + i].toLong() and 0xFF); return v }
        fun s(p: Int, n: Int): Long { val v = u(p, n); return if (n in 1..7 && (v shr (n * 8 - 1)) and 1L == 1L) v - (1L shl (n * 8)) else v }
        var pos = 0
        fun arr(p: Int): Pair<LongArray, Int> {
            val cs = (d[p].toInt() and 0xFF) - 0x0c
            if (cs !in 1..4) throw IOException("Array PSB tidak valid")
            val n = u(p + 1, cs).toInt()
            var q = p + 1 + cs
            val es = (d[q].toInt() and 0xFF) - 0x0c
            q++
            if (n < 0 || es < 0 || es > 8 || q.toLong() + n.toLong() * es > d.size) throw IOException("Array PSB tidak valid")
            val out = LongArray(n)
            for (i in 0 until n) { out[i] = u(q, es); q += es }
            return Pair(out, q)
        }
    }

    fun parse(d: ByteArray): Doc {
        if (!isPsb(d)) throw IOException("Bukan PSB")
        val bb = d.le()
        val ver = bb.getShort(4).toInt() and 0xFFFF
        val hlen = bb.getInt(8)
        if (ver < 3 || hlen < 40 || hlen > 64) throw IOException("Versi PSB belum didukung ($ver)")
        val nameOff = bb.getInt(12); val strOff = bb.getInt(16); val strData = bb.getInt(20)
        val chunkOff = bb.getInt(24); val chunkLen = bb.getInt(28); val chunkData = bb.getInt(32); val entry = bb.getInt(36)
        for (v in intArrayOf(nameOff, strOff, strData, chunkOff, chunkLen, chunkData, entry)) if (v < 0 || v > d.size) throw IOException("Header PSB tidak valid")
        val r = R(d)
        // name table
        val (a, p1) = r.arr(nameOff); val (b, p2) = r.arr(p1); val (c, _) = r.arr(p2)
        val names = ArrayList<String>(c.size)
        for (idx in c) {
            val bytes = ArrayList<Byte>(); var i = idx.toInt(); var guard = 0
            while (true) {
                val par = b[i].toInt()
                bytes.add((i - a[par].toInt()).toByte())
                if (par == 0) break
                i = par
                if (++guard > 512) throw IOException("Tabel nama PSB rusak")
            }
            bytes.reverse()
            names.add(String(bytes.filter { it.toInt() != 0 }.toByteArray(), Charsets.UTF_8))
        }
        // strings
        val (so, _) = r.arr(strOff)
        val strs = Array(so.size) { k ->
            val a0 = strData + so[k].toInt(); var e = a0
            while (e < d.size && d[e].toInt() != 0) e++
            String(d, a0, e - a0, Charsets.UTF_8)
        }
        fun value(p: Int, depth: Int): N {
            if (depth > 200) throw IOException("PSB terlalu dalam")
            val t = d[p].toInt() and 0xFF; val q = p + 1
            return when (t) {
                1 -> Nul
                2 -> Bool(false)
                3 -> Bool(true)
                4 -> I(0)
                in 5..12 -> I(r.s(q, t - 4))
                in 0x0d..0x14 -> { val (v, _) = r.arr(p); IntArr(v, 0) }
                in 0x15..0x18 -> S(strs[r.u(q, t - 0x14).toInt()])
                in 0x19..0x1c -> Chunk(r.u(q, t - 0x18))
                0x1d -> F(0.0, 0x1d)
                0x1e -> F(java.lang.Float.intBitsToFloat(r.u(q, 4).toInt()).toDouble(), 0x1e)
                0x1f -> F(java.lang.Double.longBitsToDouble(r.u(q, 8)), 0x1f)
                0x20 -> {
                    val (offs, e) = r.arr(q)
                    L(offs.map { value(e + it.toInt(), depth + 1) }.toMutableList())
                }
                0x21 -> {
                    val (ks, q2) = r.arr(q); val (offs, e) = r.arr(q2)
                    D(IntArray(ks.size) { ks[it].toInt() }, offs.map { value(e + it.toInt(), depth + 1) }.toMutableList())
                }
                else -> throw IOException("Tipe PSB tidak dikenal: $t")
            }
        }
        val root = value(entry, 0)
        // integer array es is not preserved (recomputed on write)
        return Doc(d.copyOfRange(0, hlen), d.copyOfRange(nameOff, entry), d.copyOfRange(chunkOff, d.size), chunkLen - chunkOff, chunkData - chunkOff, names, root)
    }

    // ---------- writing ----------

    private fun bytesFor(v: Long): Int { var n = 1; var x = v; while (n < 8 && (x ushr (n * 8)) != 0L) n++; return n }

    private fun putU(o: ByteArrayOutputStream, v: Long, n: Int) { for (i in 0 until n) o.write(((v ushr (8 * i)) and 0xFF).toInt()) }

    private fun writeArr(o: ByteArrayOutputStream, v: LongArray, esFixed: Int = -1) {
        val cs = bytesFor(v.size.toLong()).coerceAtMost(4)
        o.write(0x0c + cs); putU(o, v.size.toLong(), cs)
        val es = if (v.isEmpty()) 0 else if (esFixed > 0) esFixed else bytesFor(v.maxOrNull() ?: 0L)
        o.write(0x0c + es)
        for (x in v) putU(o, x, es)
    }

    /** Children with identical content within one container share one copy (like the original file). */
    private fun layout(blobs: List<ByteArray>): Pair<LongArray, List<ByteArray>> {
        val seen = HashMap<List<Byte>, Long>(); val uniq = ArrayList<ByteArray>(); var acc = 0L
        val offs = LongArray(blobs.size)
        for ((i, b) in blobs.withIndex()) {
            val key = b.asList()
            val f = seen[key]
            if (f != null) offs[i] = f else { seen[key] = acc; offs[i] = acc; uniq.add(b); acc += b.size }
        }
        return Pair(offs, uniq)
    }

    private class W(val strIndex: LinkedHashMap<String, Int> = LinkedHashMap())

    private fun write(n: N, w: W, o: ByteArrayOutputStream) {
        when (n) {
            Nul -> o.write(1)
            is Bool -> o.write(if (n.v) 3 else 2)
            is I -> if (n.v == 0L) o.write(4) else {
                var k = 1
                while (k < 8) { val lim = 1L shl (k * 8 - 1); if (n.v >= -lim && n.v < lim) break; k++ }
                o.write(4 + k); putU(o, n.v, k)
            }
            is F -> when (n.type) {
                0x1d -> o.write(0x1d)
                0x1e -> { o.write(0x1e); putU(o, java.lang.Float.floatToRawIntBits(n.v.toFloat()).toLong() and 0xFFFFFFFFL, 4) }
                else -> { o.write(0x1f); putU(o, java.lang.Double.doubleToRawLongBits(n.v), 8) }
            }
            is S -> {
                val i = w.strIndex.getOrPut(n.v) { w.strIndex.size }
                val k = bytesFor(i.toLong()).coerceAtMost(4)
                o.write(0x14 + k); putU(o, i.toLong(), k)
            }
            is Chunk -> { val k = bytesFor(n.index).coerceAtMost(4); o.write(0x18 + k); putU(o, n.index, k) }
            is IntArr -> writeArr(o, n.v)
            is L -> {
                val blobs = n.items.map { ByteArrayOutputStream().also { b -> write(it, w, b) }.toByteArray() }
                val (offs, uniq) = layout(blobs)
                o.write(0x20); writeArr(o, offs)
                for (b in uniq) o.write(b)
            }
            is D -> {
                val blobs = n.vals.map { ByteArrayOutputStream().also { b -> write(it, w, b) }.toByteArray() }
                val (offs, uniq) = layout(blobs)
                o.write(0x21); writeArr(o, LongArray(n.keys.size) { n.keys[it].toLong() }); writeArr(o, offs)
                for (b in uniq) o.write(b)
            }
        }
    }

    fun write(doc: Doc): ByteArray {
        val w = W()
        val ent = ByteArrayOutputStream()
        write(doc.root, w, ent)
        val strOffs = LongArray(w.strIndex.size)
        val sd = ByteArrayOutputStream()
        for ((s, i) in w.strIndex) { strOffs[i] = sd.size().toLong(); sd.write(s.toByteArray(Charsets.UTF_8)); sd.write(0) }
        val so = ByteArrayOutputStream(); writeArr(so, strOffs)
        val hl = doc.head.size
        val entryOff = hl + doc.namesRaw.size
        val strOff = entryOff + ent.size()
        val strDataOff = strOff + so.size()
        val chunkOff = strDataOff + sd.size()
        val head = doc.head.copyOf()
        val hb = head.le()
        hb.putInt(12, hl); hb.putInt(16, strOff); hb.putInt(20, strDataOff); hb.putInt(24, chunkOff)
        hb.putInt(28, chunkOff + doc.chunkLenOff); hb.putInt(32, chunkOff + doc.chunkDataOff); hb.putInt(36, entryOff)
        val out = ByteArrayOutputStream(chunkOff + doc.chunkRaw.size)
        out.write(head); out.write(doc.namesRaw); out.write(ent.toByteArray()); out.write(so.toByteArray()); out.write(sd.toByteArray()); out.write(doc.chunkRaw)
        return out.toByteArray()
    }
}
