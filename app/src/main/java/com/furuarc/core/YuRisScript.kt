package com.furuarc.core

import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Yu-Ris script (.ybn, magic "YSTB"; ysbin/yst*.ybn in data.ypf / Scripts.ypf).
 * Header 0x20: "YSTB", u32 version, u32 command count, u32 sizes of the cmd/args/exprs/lines sections.
 * Sections (encoded with 4-byte XOR): cmd [id u8, arg count u8, u16] ×N; args [u16 id, u8 type, u8 aop, u32 size, u32 offset] ×M;
 * exprs = expression chunks [op u8, u16 length, content]; lines = u32 ×N.
 * The XOR key is read automatically from the first arg (its offset is always 0 → bytes 8..11 of the args section = key).
 * Text: WORD command (id 106; all its arguments = raw text) and non-ASCII string literals of expressions (op 'M', "...").
 * Import re-joins the exprs section, then shifts the offset/size of all arguments and rewrites the section sizes.
 */
object YuRisScript {
    private const val WORD = 106

    fun isScript(h: ByteArray) = h.size >= 32 && h[0] == 'Y'.code.toByte() && h[1] == 'S'.code.toByte() &&
        h[2] == 'T'.code.toByte() && h[3] == 'B'.code.toByte()

    private class Edit(val pos: Int, val oldLen: Int, val body: ByteArray, val word: Boolean)

    private class Parsed(
        val head: ByteArray, val key: ByteArray, val cmd: ByteArray, val args: ByteArray,
        val ex: ByteArray, val ln: ByteArray, val tail: ByteArray, val cc: Int,
    )

    private fun xor(b: ByteArray, k: ByteArray): ByteArray = ByteArray(b.size) { (b[it].toInt() xor k[it and 3].toInt()).toByte() }

    private fun parse(d: ByteArray): Parsed {
        if (!isScript(d)) throw IOException("Bukan skrip YSTB")
        val bb = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN)
        val cc = bb.getInt(8)
        val cs = bb.getInt(12); val a = bb.getInt(16); val es = bb.getInt(20); val ls = bb.getInt(24)
        if (cc < 0 || cs < 0 || a < 12 || es < 0 || ls < 0) throw IOException("Tidak ada teks (skrip kosong)")
        val total = 32L + cs + a + es + ls
        if (total > d.size || cs != cc * 4 || a % 12 != 0) throw IOException("Header YSTB tidak valid")
        val key = d.copyOfRange(32 + cs + 8, 32 + cs + 12)
        var p = 32
        fun sec(n: Int): ByteArray { val r = xor(d.copyOfRange(p, p + n), key); p += n; return r }
        val cmd = sec(cs); val args = sec(a); val ex = sec(es); val ln = sec(ls)
        return Parsed(d.copyOfRange(0, 32), key, cmd, args, ex, ln, d.copyOfRange(p, d.size), cc)
    }

    private fun u16(b: ByteArray, p: Int) = (b[p].toInt() and 0xff) or ((b[p + 1].toInt() and 0xff) shl 8)
    private fun u32(b: ByteArray, p: Int) = u16(b, p).toLong() or (u16(b, p + 2).toLong() shl 16)
    private fun hasHigh(b: ByteArray, from: Int, to: Int): Boolean { for (i in from until to) if (b[i].toInt() < 0) return true; return false }

    private fun edits(s: Parsed): java.util.TreeMap<Int, Edit> {
        val out = java.util.TreeMap<Int, Edit>()
        val ex = s.ex
        var ai = 0
        for (i in 0 until s.cc) {
            val cid = s.cmd[i * 4].toInt() and 0xff
            val ac = s.cmd[i * 4 + 1].toInt() and 0xff
            for (j in 0 until ac) {
                if ((ai + 1) * 12 > s.args.size) return out
                val esz = u32(s.args, ai * 12 + 4)
                val eoff = u32(s.args, ai * 12 + 8)
                ai++
                if (eoff < 0 || esz < 0 || eoff + esz > ex.size) continue
                val o = eoff.toInt(); val e = (eoff + esz).toInt()
                if (cid == WORD) {
                    if (hasHigh(ex, o, e)) out[o] = Edit(o, e - o, ex.copyOfRange(o, e), true)
                } else {
                    var p = o
                    while (p + 3 <= e) {
                        val l = u16(ex, p + 1)
                        if (p + 3 + l > e) break
                        if (ex[p].toInt() == 0x4d && hasHigh(ex, p + 3, p + 3 + l)) out[p] = Edit(p, 3 + l, ex.copyOfRange(p + 3, p + 3 + l), false)
                        p += 3 + l
                    }
                }
            }
        }
        return out
    }

    private fun textOf(e: Edit, cs: Charset): String {
        var b = e.body
        if (!e.word && b.size >= 2 && b[0].toInt() == 0x22 && b[b.size - 1].toInt() == 0x22) b = b.copyOfRange(1, b.size - 1)
        return String(b, cs)
    }

    fun extract(fileKey: String, d: ByteArray, cs: Charset): List<TextUnit> {
        val ed = edits(parse(d))
        val out = ArrayList<TextUnit>()
        for ((pos, e) in ed) {
            val t = textOf(e, cs)
            if (t.isNotEmpty()) out.add(TextUnit(fileKey, "Y$pos:" + ScriptText.hash(t), t))
        }
        if (out.isEmpty()) throw IOException("Tidak ada teks non-ASCII")
        return out
    }

    fun apply(d: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val s = parse(d)
        val ed = edits(s)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("Y") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        val enc = cs.newEncoder()
        val rep = ArrayList<Pair<Edit, ByteArray>>()
        for ((pos, e) in ed) {
            val t = textOf(e, cs)
            val nt = tr["Y$pos:${ScriptText.hash(t)}"] ?: byHash[ScriptText.hash(t)]
            if (nt == null || nt == t) continue
            val safe = buildString {
                for (c in nt) append(when {
                    c == '\u0000' -> ""
                    !e.word && c == '"' -> '”'
                    enc.canEncode(c) -> c
                    else -> '?'
                })
            }
            val body = safe.toByteArray(cs)
            val nb = if (e.word) body else {
                if (body.size + 2 > 0xffff) continue
                val l = body.size + 2
                ByteArray(l + 3).also {
                    it[0] = 0x4d; it[1] = (l and 0xff).toByte(); it[2] = (l shr 8).toByte()
                    it[3] = 0x22; System.arraycopy(body, 0, it, 4, body.size); it[it.size - 1] = 0x22
                }
            }
            rep.add(Pair(e, nb))
        }
        if (rep.isEmpty()) return Pair(d, 0)
        val ex = s.ex
        val bo = java.io.ByteArrayOutputStream(ex.size + 4096)
        var last = 0
        for ((e, nb) in rep) { bo.write(ex, last, e.pos - last); bo.write(nb); last = e.pos + e.oldLen }
        bo.write(ex, last, ex.size - last)
        val nex = bo.toByteArray()
        val args = s.args.copyOf()
        val nargs = args.size / 12
        for (ai in 0 until nargs) {
            val esz = u32(args, ai * 12 + 4).toInt(); val eoff = u32(args, ai * 12 + 8).toInt()
            var before = 0; var inside = 0
            for ((e, nb) in rep) {
                val dl = nb.size - e.oldLen
                if (e.pos + e.oldLen <= eoff) before += dl
                else if (e.pos >= eoff && e.pos + e.oldLen <= eoff + esz) inside += dl
            }
            putU32(args, ai * 12 + 4, esz + inside); putU32(args, ai * 12 + 8, eoff + before)
        }
        val head = s.head.copyOf(); putU32(head, 20, nex.size)
        val res = java.io.ByteArrayOutputStream(d.size + 4096)
        res.write(head); res.write(xor(s.cmd, s.key)); res.write(xor(args, s.key)); res.write(xor(nex, s.key)); res.write(xor(s.ln, s.key)); res.write(s.tail)
        return Pair(res.toByteArray(), rep.size)
    }

    private fun putU32(b: ByteArray, p: Int, v: Int) {
        b[p] = v.toByte(); b[p + 1] = (v shr 8).toByte(); b[p + 2] = (v shr 16).toByte(); b[p + 3] = (v shr 24).toByte()
    }
}
