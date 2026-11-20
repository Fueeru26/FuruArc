package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Silky Engine .mes script (port of msg-tool silky/disasm.rs + mes.rs). Two variants: "Plus" and "Ai6Win"
 * (distinguished like msg-tool). Message text = a sequence of message/escape/yield instructions; speaker name = push_string
 * before the set-character-name syscall. Text: newline = "\n", ruby = "[...]". Messages are rewritten as message2 instructions;
 * all addresses (A operands big-endian + the message table in the header) are recomputed on import.
 */
object SilkyMes {
    private class Op(val yieldOp: Int, val add: Int, val esc: Int, val msg1: Int, val msg2: Int, val pushInt: Int, val pushStr: Int,
                     val syscall: Int, val line: Int, val nop1: Int, val nop2: Int, val obf: Boolean,
                     val table: Map<Int, String>, val sys: List<IntArray>, val plus: Boolean)

    private val PLUS = Op(0x00, 0x34, 0x1c, 0x0a, 0x0b, 0x32, 0x33, 0x18, 0xff, 0xfc, 0xfd, true,
        mapOf(0x00 to "", 0x01 to "", 0x02 to "", 0x03 to "", 0x04 to "", 0x05 to "", 0x06 to "", 0x07 to "", 0x08 to "", 0x09 to "", 0x0A to "S", 0x0B to "T", 0x0C to "", 0x0D to "", 0x0E to "", 0x0F to "", 0x10 to "", 0x11 to "", 0x12 to "", 0x13 to "", 0x14 to "A", 0x15 to "A", 0x16 to "A", 0x17 to "", 0x18 to "", 0x19 to "I", 0x1A to "I", 0x1B to "A", 0x1C to "B", 0x32 to "I", 0x33 to "S", 0x34 to "", 0x35 to "", 0x36 to "", 0x37 to "", 0x38 to "", 0x39 to "", 0x3A to "", 0x3B to "", 0x3C to "", 0x3D to "", 0x3E to "", 0x3F to "", 0x40 to "", 0x41 to "", 0x42 to "", 0x43 to "", 0xFA to "", 0xFB to "", 0xFC to "", 0xFD to "", 0xFE to "", 0xFF to ""), listOf(intArrayOf(29, 11), intArrayOf(29, 15)), true)
    private val AI6 = Op(0x00, 0x34, 0x1b, 0x0a, 0x0b, 0x32, 0x33, 0x18, 0xff, 0xfc, 0xfd, false,
        mapOf(0x00 to "", 0x01 to "", 0x02 to "", 0x03 to "", 0x04 to "", 0x05 to "", 0x06 to "", 0x07 to "", 0x08 to "", 0x09 to "", 0x0A to "S", 0x0B to "S", 0x0C to "", 0x0D to "", 0x0E to "", 0x0F to "", 0x10 to "", 0x11 to "", 0x12 to "", 0x13 to "", 0x14 to "A", 0x15 to "A", 0x16 to "A", 0x17 to "", 0x18 to "", 0x19 to "I", 0x1A to "A", 0x1B to "B", 0x32 to "I", 0x33 to "S", 0x34 to "", 0x35 to "", 0x36 to "", 0x37 to "", 0x38 to "", 0x39 to "", 0x3A to "", 0x3B to "", 0x3C to "", 0x3D to "", 0x3E to "", 0x3F to "", 0x40 to "", 0x41 to "", 0x42 to "", 0x43 to "", 0xFE to "", 0xFF to ""), listOf(intArrayOf(31, 15)), false)

    private const val INTERNAL = 0
    private const val MESSAGE = 1
    private const val NAME = 2

    private class Txt(val start: Int, val len: Int, val typ: Int)

    private class Ins(val pos: Int, val op: Int, val operands: List<Any>) // Int (B/I) or IntArray(start,len) for strings

    private class Parsed(val d: ByteArray, val o: Op, val codeOffset: Int, val texts: List<Txt>, val be: List<Int>, val le: List<Int>)

    private fun parse(d: ByteArray): Parsed {
        if (d.size < 16) throw IOException("Bukan skrip Silky .mes")
        val bb = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN)
        val n = bb.getInt(0)
        if (n < 0 || n > 1_000_000 || 4L + n * 4 + 1 > d.size) throw IOException("Bukan skrip Silky .mes")
        val ai6code = 4 + n * 4
        val first = bb.getInt(4).toLong() + ai6code
        val useAi6 = first >= 0 && first + 5 <= d.size && (d[first.toInt()].toInt() and 0xff) == 0x19 && bb.getInt(first.toInt() + 1) == 0
        val o = if (useAi6) AI6 else PLUS
        val le = ArrayList<Int>()
        val code: Int
        if (o.plus) {
            val sp = bb.getInt(4)
            code = 8 + (n + sp) * 4
            if (sp < 0 || sp > 1_000_000 || code > d.size || code < 8) throw IOException("Header Silky tidak valid")
            for (i in 0 until n + sp) le.add(8 + i * 4)
        } else {
            code = ai6code
            for (i in 0 until n) le.add(4 + i * 4)
        }
        val be = ArrayList<Int>()
        val texts = ArrayList<Txt>()
        var pos = code
        fun u8() = d[pos++].toInt() and 0xff
        fun readIns(): Ins {
            val at = pos
            val op = u8()
            val sig = o.table[op] ?: ""
            val ops = ArrayList<Any>()
            for (c in sig) when (c) {
                'B' -> ops.add(u8())
                'I' -> { ops.add(ByteBuffer.wrap(d, pos, 4).order(ByteOrder.BIG_ENDIAN).int); pos += 4 }
                'A' -> { be.add(pos); ops.add(ByteBuffer.wrap(d, pos, 4).order(ByteOrder.BIG_ENDIAN).int); pos += 4 }
                'S', 'T' -> {
                    val s = pos
                    var e = pos
                    while (e < d.size && d[e].toInt() != 0) e++
                    if (e >= d.size) throw IOException("String Silky terpotong")
                    pos = e + 1
                    ops.add(intArrayOf(s, pos - s))
                }
            }
            if (pos > d.size) throw IOException("Instruksi Silky terpotong")
            return Ins(at, op, ops)
        }
        val stack = ArrayList<Any>()
        var msgStart = -1
        var inRuby = false
        try {
            while (pos < d.size) {
                val at = pos
                val ins = readIns()
                val op = ins.op
                if (op == o.msg1 || op == o.msg2) {
                    if (msgStart < 0) msgStart = at
                } else if (op == o.esc) {
                    if ((ins.operands.firstOrNull() as? Int) == 1) inRuby = true
                } else if (op == o.yieldOp && inRuby) {
                    inRuby = false
                } else if (op == o.pushInt && at + 5 < d.size && (d[at + 5].toInt() and 0xff) == o.line) {
                } else if (op == o.line || op == o.nop1 || op == o.nop2) {
                } else {
                    if (msgStart >= 0) texts.add(Txt(msgStart, at - msgStart, MESSAGE))
                    msgStart = -1; inRuby = false
                }
                if (op == o.pushInt || op == o.pushStr) {
                    if (ins.operands.isNotEmpty()) stack.add(ins.operands[0])
                } else if (op == o.add && stack.size >= 2) {
                    val v1 = stack.removeAt(stack.size - 1); val v2 = stack.removeAt(stack.size - 1)
                    if (v1 is Int && v2 is Int) stack.add(v1 + v2)
                } else if (op == o.syscall && stack.size >= 3) {
                    val fn = stack.removeAt(stack.size - 1); val ex = stack.removeAt(stack.size - 1); val nm = stack.removeAt(stack.size - 1)
                    if (fn is Int && ex is Int && nm is IntArray) {
                        for (sc in o.sys) if (fn == sc[0] && ex == sc[1]) texts.add(Txt(nm[0] - 1, nm[1] + 1, NAME))
                    }
                    stack.clear()
                } else stack.clear()
            }
        } catch (e: IndexOutOfBoundsException) { throw IOException("Skrip Silky rusak") }
        return Parsed(d, o, code, texts, be, le)
    }

    fun isMes(b: ByteArray): Boolean = try { val p = parse(b); p.texts.isNotEmpty() } catch (e: Exception) { false }

    /** Text of one code range (message or name) -> display string. */
    private fun toText(p: Parsed, t: Txt, cs: Charset): String {
        val d = p.d; val o = p.o
        val sb = StringBuilder()
        var pos = t.start
        val end = t.start + t.len
        while (pos < end) {
            val op = d[pos++].toInt() and 0xff
            val sig = o.table[op] ?: ""
            var strS = -1; var strL = 0; var esc = -1
            for (c in sig) when (c) {
                'B' -> esc = d[pos++].toInt() and 0xff
                'I' -> pos += 4
                'A' -> pos += 4
                'S', 'T' -> { strS = pos; var e = pos; while (d[e].toInt() != 0) e++; strL = e - pos; pos = e + 1 }
            }
            if (op == o.pushStr || (op == o.msg1 && !o.obf) || op == o.msg2) {
                if (strS >= 0) sb.append(String(d, strS, strL, cs))
            } else if (op == o.msg1 && o.obf) {
                if (strS >= 0) {
                    val out = ByteArrayOutputStream()
                    var i = 0
                    while (i < strL) {
                        val b = d[strS + i].toInt() and 0xff; i++
                        if (b in 0x81..0x9f || b in 0xe0..0xef) { out.write(b); out.write(d[strS + i].toInt() and 0xff); i++ }
                        else { val c = b - 0x7D62; out.write((c shr 8) and 0xff); out.write(c and 0xff) }
                    }
                    sb.append(String(out.toByteArray(), cs))
                }
            } else if (op == o.esc) {
                when (esc) { 0 -> sb.append('\n'); 1 -> sb.append('[') }
            } else if (op == o.yieldOp) sb.append(']')
        }
        return sb.toString()
    }

    private fun fromText(o: Op, typ: Int, s: String, cs: Charset): ByteArray {
        val m = ByteArrayOutputStream()
        if (typ == NAME) { m.write(o.pushStr); m.write(s.toByteArray(cs)); m.write(0); return m.toByteArray() }
        var inRuby = false; var inText = false
        for (c in s.replace("\r\n", "\n")) {
            when (c) {
                '[' -> {
                    if (inRuby) throw IOException("Ruby bersarang tidak diizinkan")
                    if (inText) { m.write(0); inText = false }
                    inRuby = true; m.write(o.esc); m.write(1); m.write(o.msg2)
                }
                ']' -> { if (!inRuby) throw IOException("Penutup ruby tanpa pembuka"); inRuby = false; m.write(0); m.write(o.yieldOp) }
                '\n' -> {
                    if (inRuby) throw IOException("Baris baru di dalam ruby tidak diizinkan")
                    if (inText) { m.write(0); inText = false }
                    m.write(o.esc); m.write(0)
                }
                else -> {
                    if (!inRuby && !inText) { inText = true; m.write(o.msg2) }
                    m.write(c.toString().toByteArray(cs))
                }
            }
        }
        if (inRuby) { m.write(0); m.write(o.yieldOp) }
        if (inText) m.write(0)
        return m.toByteArray()
    }

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b)
        val out = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        for ((i, t) in p.texts.withIndex()) {
            if (t.typ == INTERNAL) continue
            val s = toText(p, t, cs)
            if (s.isBlank()) continue
            val tag = if (t.typ == NAME) "N" else "M"
            if (seen.add(tag + s)) out.add(TextUnit(fileKey, "$tag$i:" + ScriptText.hash(s), s))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (c > 1 && (k[0] == 'N' || k[0] == 'M')) byHash.putIfAbsent(k[0] + k.substring(c + 1), v) }
        class Rep(val start: Int, val len: Int, val bytes: ByteArray)
        val reps = ArrayList<Rep>()
        for ((i, t) in p.texts.withIndex()) {
            if (t.typ == INTERNAL) continue
            val s = toText(p, t, cs)
            val tag = if (t.typ == NAME) "N" else "M"
            val h = ScriptText.hash(s)
            val nt = tr["$tag$i:$h"] ?: byHash[tag + h] ?: continue
            if (nt == s) continue
            reps.add(Rep(t.start, t.len, fromText(p.o, t.typ, nt, cs)))
        }
        if (reps.isEmpty()) return Pair(b, 0)
        reps.sortBy { it.start }
        val out = ByteArrayOutputStream()
        var cur = 0
        for (r in reps) { out.write(b, cur, r.start - cur); out.write(r.bytes); cur = r.start + r.len }
        out.write(b, cur, b.size - cur)
        val res = out.toByteArray()
        fun map(old: Int): Int { var dl = 0; for (r in reps) if (r.start + r.len <= old) dl += r.bytes.size - r.len; return old + dl }
        val ob = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        val obe = ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN)
        val rb = ByteBuffer.wrap(res).order(ByteOrder.LITTLE_ENDIAN)
        val rbe = ByteBuffer.wrap(res).order(ByteOrder.BIG_ENDIAN)
        for (a in p.le) rb.putInt(map(a), map(ob.getInt(a) + p.codeOffset) - p.codeOffset)
        for (a in p.be) rbe.putInt(map(a), map(obe.getInt(a) + p.codeOffset) - p.codeOffset)
        return Pair(res, reps.size)
    }
}
