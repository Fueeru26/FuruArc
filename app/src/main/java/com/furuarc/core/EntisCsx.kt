package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * EntisGLS/Cotopha .csx script (versions 2.x–3.x, strings in the "conststr" section; following CSXToolPlus).
 * File: 0x40 header ("Entis\x1a", "Cotopha Image file"), then records [id 8 bytes][length u64][contents].
 * conststr section: u32 count; each entry: u32 length (characters) + UTF-16LE, then u32 ref count + u32 refs.
 * Code uses string INDEXES, so text length can change freely; other sections are copied as is.
 * Only strings containing non-ASCII characters (Japanese/Chinese text) are selected; ASCII resource names are skipped.
 * CSX 1.x (strings in the image section) is NOT supported.
 */
object EntisCsx {
    fun isCsx(h: ByteArray): Boolean {
        if (h.size < 0x22) return false
        val sig = "Entis".toByteArray(Charsets.US_ASCII)
        for (i in sig.indices) if (h[i] != sig[i]) return false
        if (h[5].toInt() != 0x1a) return false
        return String(h, 0x10, 18, Charsets.US_ASCII) == "Cotopha Image file"
    }

    private class Rec(val id: String, val start: Int, val bodyStart: Int, val len: Int)
    private class Str(var text: String, val refs: IntArray)

    private fun records(d: ByteArray): List<Rec> {
        if (!isCsx(d)) throw IOException("Bukan file CSX EntisGLS")
        val bb = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN)
        val out = ArrayList<Rec>()
        var p = 0x40
        while (p + 16 <= d.size) {
            val id = String(d, p, 8, Charsets.ISO_8859_1)
            if (bb.getLong(p) == 0L) break
            val len = bb.getLong(p + 8)
            if (len < 0 || p + 16 + len > d.size) throw IOException("Rekaman CSX rusak")
            out += Rec(id, p, p + 16, len.toInt())
            p += 16 + len.toInt()
        }
        return out
    }

    private fun readStrings(d: ByteArray, r: Rec): List<Str> {
        val bb = ByteBuffer.wrap(d, r.bodyStart, r.len).order(ByteOrder.LITTLE_ENDIAN)
        val end = r.bodyStart + r.len
        val n = bb.getInt()
        if (n < 0 || n > 5_000_000) throw IOException("Jumlah string CSX tidak valid")
        val out = ArrayList<Str>(n)
        for (i in 0 until n) {
            val l = bb.getInt()
            if (l < 0 || bb.position() + l * 2 > end) throw IOException("conststr rusak")
            val s = if (l > 0) String(d, bb.position(), l * 2, Charsets.UTF_16LE) else ""
            bb.position(bb.position() + l * 2)
            val rc = bb.getInt()
            if (rc < 0 || bb.position() + rc * 4 > end) throw IOException("conststr rusak")
            val refs = IntArray(rc) { bb.getInt() }
            out += Str(s, refs)
        }
        return out
    }

    private fun conststr(d: ByteArray): Pair<List<Rec>, Rec> {
        val recs = records(d)
        val cs = recs.firstOrNull { it.id == "conststr" }
            ?: throw IOException("CSX tanpa seksi conststr (kemungkinan CSX 1.x: belum didukung)")
        return Pair(recs, cs)
    }

    private fun wanted(t: String) = t.any { it.code >= 0x80 }

    fun extract(fileKey: String, d: ByteArray): List<TextUnit> {
        val (_, cs) = conststr(d)
        val strs = readStrings(d, cs)
        val out = ArrayList<TextUnit>(); val seen = HashSet<String>()
        for ((i, s) in strs.withIndex()) {
            if (!wanted(s.text)) continue
            if (seen.add(s.text)) out.add(TextUnit(fileKey, "S$i:" + ScriptText.hash(s.text), s.text))
        }
        if (out.isEmpty()) throw IOException("Tidak ada teks non-ASCII di conststr")
        return out
    }

    fun apply(d: ByteArray, tr: Map<String, String>): Pair<ByteArray, Int> {
        val (recs, cs) = conststr(d)
        val strs = readStrings(d, cs)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        var changed = 0
        for ((i, s) in strs.withIndex()) {
            if (!wanted(s.text)) continue
            val nt = tr["S$i:${ScriptText.hash(s.text)}"] ?: byHash[ScriptText.hash(s.text)] ?: continue
            if (nt == s.text) continue
            s.text = nt; changed++
        }
        if (changed == 0) return Pair(d, 0)
        val body = ByteBuffer.allocate(4 + strs.sumOf { 8 + it.text.length * 2 + it.refs.size * 4 }).order(ByteOrder.LITTLE_ENDIAN)
        body.putInt(strs.size)
        for (s in strs) {
            body.putInt(s.text.length)
            body.put(s.text.toByteArray(Charsets.UTF_16LE))
            body.putInt(s.refs.size)
            for (r in s.refs) body.putInt(r)
        }
        val nb = body.array().copyOf(body.position())
        val out = ByteArrayOutputStream(d.size + 4096)
        out.write(d, 0, cs.start)
        out.write(d, cs.start, 8)
        val ln = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(nb.size.toLong()).array()
        out.write(ln); out.write(nb)
        val after = cs.bodyStart + cs.len
        out.write(d, after, d.size - after)
        return Pair(out.toByteArray(), changed)
    }
}
