package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

/**
 * Yaneurao Itufuru script (port of msg-tool yaneurao/itufuru/script.rs). No header: the file is scanned for u16 instructions;
 * opcode 0x02 (text, must end with "\n") and 0x1E followed by u16 length + NUL-terminated string. Lengths are rewritten on import.
 * No other addresses need updating. Not auto-detected (choose via the Itufuru engine menu).
 */
object ItufuruScript {
    private class St(val instr: Int, val lenPos: Int, val len: Int)

    private fun scan(d: ByteArray): List<St> {
        val out = ArrayList<St>()
        var pos = 0
        fun u16(p: Int) = (d[p].toInt() and 0xff) or ((d[p + 1].toInt() and 0xff) shl 8)
        while (pos + 1 < d.size) {
            val instr = u16(pos); pos += 2
            if (instr == 0x2 || instr == 0x1e || instr == 0x1 || instr == 0x13 || instr == 0x27) {
                if (pos + 2 > d.size) break
                val lenPos = pos
                val len = u16(pos); pos += 2
                var e = pos
                while (e < d.size && d[e].toInt() != 0) e++
                if (e >= d.size) { pos = lenPos; continue }
                val slen = e - pos + 1
                val endsNl = e > pos && d[e - 1].toInt() == '\n'.code
                if (slen != len || (instr == 0x2 && !endsNl) || len < 3) { pos = lenPos; continue }
                pos = e + 1
                if (instr != 0x2 && instr != 0x1e) continue
                out.add(St(instr, lenPos, len))
            }
        }
        return out
    }

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val strs = scan(b)
        if (strs.isEmpty()) throw IOException("Tidak ada teks Itufuru ditemukan")
        val out = ArrayList<TextUnit>(); val seen = HashSet<String>()
        for ((i, s) in strs.withIndex()) {
            val t = String(b, s.lenPos + 2, s.len - 1, cs)
            if (t.isBlank()) continue
            if (seen.add(t)) out.add(TextUnit(fileKey, "S$i:" + ScriptText.hash(t), t))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val strs = scan(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        val out = ByteArrayOutputStream()
        var old = 0; var changed = 0
        for ((i, s) in strs.withIndex()) {
            val t = String(b, s.lenPos + 2, s.len - 1, cs)
            var nt = tr["S$i:${ScriptText.hash(t)}"] ?: byHash[ScriptText.hash(t)] ?: continue
            if (nt == t) continue
            if (s.instr == 0x2 && !nt.endsWith("\n")) nt += "\n"
            val enc = nt.toByteArray(cs)
            if (enc.size + 1 > 0xffff) continue
            out.write(b, old, s.lenPos - old)
            val nl = enc.size + 1
            out.write(nl and 0xff); out.write((nl ushr 8) and 0xff)
            out.write(enc); out.write(0)
            old = s.lenPos + 2 + s.len
            changed++
        }
        if (changed == 0) return Pair(b, 0)
        out.write(b, old, b.size - old)
        return Pair(out.toByteArray(), changed)
    }
}
