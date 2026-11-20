package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Adv32 (Circus) .mes script. Structure (from analysis of 138 D.C. II Side Episodes scripts):
 *   u32 N, N x u32 offsets (relative to 4+4N; point to the start of each message/voice name), then code.
 *   Op 0x4B (speaker name) and 0x4C (message) are followed by NUL-terminated text; each text byte = Shift-JIS byte - 0x20 (mod 256),
 *   so a newline (0x0A) is stored as 0xEA. Op 0x36 = voice file name (not extracted).
 * Jumps use label names, so when text is replaced only the offset table in the head is shifted.
 * Latin text is stored as full-width letters; on export it is converted to plain ASCII and on import restored
 * to full-width (space -> ideographic space) because the game font has no half-width letters.
 */
object CircusMes {
    private val SJIS = charset("windows-31j")
    private const val NAME = 0x4B
    private const val MSG = 0x4C
    private const val VOICE = 0x36

    private class Seg(val pos: Int, val s: Int, val e: Int)
    private class Parsed(val n: Int, val offs: IntArray, val base: Int, val segs: List<Seg>)

    fun isMes(h: ByteArray): Boolean {
        if (h.size < 12) return false
        val bb = h.le()
        val n = bb.getInt(0)
        if (n < 1 || n > 1_000_000) return false
        val avail = minOf(n, (h.size - 4) / 4)
        if (avail < 1) return false
        val base = 4L + 4L * n
        var prev = -1L; var hit = 0; var checked = 0; var text = 0
        for (i in 0 until avail) {
            val o = bb.getInt(4 + 4 * i).toLong() and 0xffffffffL
            if (o < prev) return false
            prev = o
            val p = base + o
            if (p >= h.size) break
            checked++
            val op = h[p.toInt()].toInt() and 0xFF
            if (op == NAME || op == MSG) { hit++; text++ } else if (op == VOICE) hit++
            if (checked >= 64) break
        }
        return checked >= 1 && text >= 1 && hit * 10 >= checked * 9
    }

    private fun parse(d: ByteArray): Parsed {
        if (d.size < 12) throw IOException("Bukan skrip Circus .mes")
        val bb = d.le()
        val n = bb.getInt(0)
        if (n < 1 || n > 1_000_000 || 4L + 4L * n >= d.size) throw IOException("Bukan skrip Circus .mes")
        val base = 4 + 4 * n
        val offs = IntArray(n)
        var prev = -1L
        for (i in 0 until n) {
            val o = bb.getInt(4 + 4 * i).toLong() and 0xffffffffL
            if (o < prev || base + o >= d.size) throw IOException("Tabel offset Circus tidak valid")
            prev = o; offs[i] = o.toInt()
        }
        val segs = ArrayList<Seg>()
        var last = -1
        for (o in offs) {
            val p = base + o
            if (p == last) continue
            last = p
            val op = d[p].toInt() and 0xFF
            if (op != NAME && op != MSG) continue
            var e = p + 1
            while (e < d.size && d[e].toInt() != 0) e++
            if (e >= d.size) throw IOException("Teks Circus tidak berakhir NUL")
            segs += Seg(p, p + 1, e)
        }
        return Parsed(n, offs, base, segs)
    }

    private fun decode(d: ByteArray, s: Seg): String {
        val raw = ByteArray(s.e - s.s) { ((d[s.s + it].toInt() and 0xFF) + 0x20).toByte() }
        val sb = StringBuilder()
        for (c in String(raw, SJIS)) {
            sb.append(when {
                c == '　' -> ' '
                c in '！'..'～' -> (c.code - 0xFEE0).toChar()
                else -> c
            })
        }
        return sb.toString()
    }

    private fun encode(t: String): ByteArray {
        val enc = SJIS.newEncoder()
        val sb = StringBuilder()
        for (c in t) {
            if (c == '\r') continue
            val m = when {
                c == '\n' -> c
                c == ' ' -> '　'
                c in '!'..'~' -> (c.code + 0xFEE0).toChar()
                else -> c
            }
            sb.append(if (m == '\n' || enc.canEncode(m)) m else '？')
        }
        val b = sb.toString().toByteArray(SJIS)
        return ByteArray(b.size) { ((b[it].toInt() and 0xFF) - 0x20).toByte() }
    }

    /** [full] = all lines in order with speaker names (op 0x4B before message 0x4C) for VNTP export. */
    fun extract(fileKey: String, d: ByteArray, full: Boolean = false): List<TextUnit> {
        val ps = parse(d)
        val out = ArrayList<TextUnit>(); val seen = HashSet<String>()
        var speaker: String? = null
        for ((i, s) in ps.segs.withIndex()) {
            if (s.e == s.s) continue
            val t = decode(d, s)
            if (t.isEmpty()) continue
            if (full) {
                if ((d[s.pos].toInt() and 0xFF) == NAME) { speaker = t; continue }
                out.add(TextUnit(fileKey, "M$i:" + ScriptText.hash(t), t, speaker)); speaker = null
            } else if (seen.add(t)) out.add(TextUnit(fileKey, "M$i:" + ScriptText.hash(t), t))
        }
        if (out.isEmpty()) throw IOException("Tidak ada teks")
        return out
    }

    fun apply(d: ByteArray, tr: Map<String, String>): Pair<ByteArray, Int> {
        val ps = parse(d)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("M") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        val bo = ByteArrayOutputStream(d.size + 4096)
        bo.write(d, 0, ps.base)
        var cur = ps.base
        var changed = 0
        val deltaPos = ArrayList<Int>(); val deltaVal = ArrayList<Int>()
        for ((i, s) in ps.segs.withIndex()) {
            if (s.e == s.s) continue
            val t = decode(d, s)
            val nt = tr["M$i:${ScriptText.hash(t)}"] ?: byHash[ScriptText.hash(t)]
            if (nt == null || nt == t) continue
            val nb = encode(nt)
            if (nb.any { it.toInt() == 0 }) continue
            bo.write(d, cur, s.s - cur)
            bo.write(nb)
            cur = s.e
            changed++
            deltaPos += s.pos; deltaVal += nb.size - (s.e - s.s)
        }
        if (changed == 0) return Pair(d, 0)
        bo.write(d, cur, d.size - cur)
        val res = bo.toByteArray()
        val bb = res.le()
        var j = 0; var acc = 0
        for (i in 0 until ps.n) {
            val p = ps.base + ps.offs[i]
            while (j < deltaPos.size && deltaPos[j] < p) { acc += deltaVal[j]; j++ }
            bb.putInt(4 + 4 * i, ps.offs[i] + acc)
        }
        return Pair(res, changed)
    }
}
