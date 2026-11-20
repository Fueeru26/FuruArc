package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

/**
 * NeXAS .bin script: u64 n at the start, table of (n+1)*8 bytes, then the text block: NUL-terminated segments (Shift-JIS) with the count of trailing
 * NULs recorded per segment; the block ends when the next byte is < 32 or 0xFF; the rest ("tail") is copied as is.
 * Rewrite = header + new text + NUL + tail, without recalculating offsets (following ScriptPacker zbspac).
 * Only segments containing non-ASCII bytes are extracted. Not yet tested on real game scripts.
 */
object NexasScript {
    private class Seg(val start: Int, val len: Int, val nulls: Int)

    fun isScript(h: ByteArray): Boolean {
        if (h.size < 24) return false
        val bb = h.le()
        val n = bb.getLong(0)
        if (n < 1 || n > 1_000_000) return false
        val s = (n + 1) * 8
        if (s + 2 > h.size) return false
        val b = h[s.toInt()].toInt() and 0xFF
        return b >= 32 && b != 0xFF
    }

    private class Parsed(val textStart: Int, val segs: List<Seg>, val textEnd: Int)

    private fun parse(d: ByteArray): Parsed {
        if (d.size < 24) throw IOException("Bukan skrip NeXAS")
        val n = d.le().getLong(0)
        if (n < 1 || n > 1_000_000 || (n + 1) * 8 >= d.size) throw IOException("Bukan skrip NeXAS (kepala tidak valid)")
        val ts = ((n + 1) * 8).toInt()
        val segs = ArrayList<Seg>()
        var p = ts
        while (p < d.size) {
            val b = d[p].toInt() and 0xFF
            if (b < 32 || b == 0xFF) break
            var e = p
            while (e < d.size && d[e].toInt() != 0) e++
            var z = e
            while (z < d.size && d[z].toInt() == 0) z++
            segs += Seg(p, e - p, z - e)
            p = z
        }
        if (segs.isEmpty()) throw IOException("Blok teks NeXAS tidak ditemukan")
        return Parsed(ts, segs, p)
    }

    private fun wanted(d: ByteArray, s: Seg): Boolean {
        for (i in s.start until s.start + s.len) if (d[i].toInt() < 0) return true
        return false
    }

    fun extract(fileKey: String, d: ByteArray, cs: Charset): List<TextUnit> {
        val ps = parse(d)
        val out = ArrayList<TextUnit>(); val seen = HashSet<String>()
        for ((i, s) in ps.segs.withIndex()) {
            if (!wanted(d, s)) continue
            val t = String(d, s.start, s.len, cs)
            if (seen.add(t)) out.add(TextUnit(fileKey, "S$i:" + ScriptText.hash(t), t))
        }
        if (out.isEmpty()) throw IOException("Tidak ada teks non-ASCII")
        return out
    }

    fun apply(d: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val ps = parse(d)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        val bo = ByteArrayOutputStream(d.size + 4096)
        bo.write(d, 0, ps.textStart)
        var changed = 0
        val enc = cs.newEncoder()
        for ((i, s) in ps.segs.withIndex()) {
            var done = false
            if (wanted(d, s)) {
                val t = String(d, s.start, s.len, cs)
                val nt = tr["S$i:${ScriptText.hash(t)}"] ?: byHash[ScriptText.hash(t)]
                if (nt != null && nt != t) {
                    val safe = buildString { for (c in nt) append(if (enc.canEncode(c)) c else '?') }.replace("\u0000", "")
                    bo.write(safe.toByteArray(cs)); changed++; done = true
                }
            }
            if (!done) bo.write(d, s.start, s.len)
            repeat(s.nulls) { bo.write(0) }
        }
        if (changed == 0) return Pair(d, 0)
        bo.write(d, ps.textEnd, d.size - ps.textEnd)
        return Pair(bo.toByteArray(), changed)
    }
}
