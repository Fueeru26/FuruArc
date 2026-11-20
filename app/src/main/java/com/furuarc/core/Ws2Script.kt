package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * WillPlus/AdvHD .ws2 script (port of msg-tool will_plus/ws2_disasm.rs + ws2.rs).
 * Read with an opcode disassembler (tables V1..V3, tried in order); the file may be encrypted (rotate-left 2 per byte).
 * Text: messages (opcode 0x14, 0x0F choice, 0x3F "char", 0x72) and speaker names (0x15). Strings may change length:
 * all addresses (operand A) are recomputed on import. The "%LC"/"%LF" prefixes on names and the "%K%P" suffix on messages are preserved.
 */
object Ws2Script {
    private const val NAME = 0
    private const val MSG = 1
    private const val INTERNAL = 2

    private class Str(val off: Int, val len: Int, var typ: Int) { // len includes NUL
        val textLen get() = len - 1
    }

    private class Parsed(val data: ByteArray, val encrypted: Boolean, val strs: List<Str>, val addrs: List<Int>)

    private class Fail : Exception()

    private class Dis(val d: ByteArray, val ops: Map<Int, String>) {
        var pos = 0
        val texts = ArrayList<Str>()
        val addrs = ArrayList<Int>()
        fun u8(): Int { if (pos >= d.size) throw Fail(); return d[pos++].toInt() and 0xff }
        fun skip(n: Int) { if (pos + n > d.size) throw Fail(); pos += n }

        fun readOperand(c: Char): Any = when (c) {
            'B' -> u8()
            'H' -> { skip(2); 0 }
            'I' -> { skip(4); 0 }
            'F' -> { skip(4); 0 }
            'A' -> { addrs.add(pos); skip(4); 0 }
            'S' -> {
                val s = pos
                var e = pos
                while (e < d.size && d[e].toInt() != 0) e++
                if (e >= d.size) throw Fail()
                pos = e + 1
                Str(s, pos - s, INTERNAL)
            }
            else -> throw Fail()
        }

        fun readOperands(sig: String): MutableList<Any> {
            val r = ArrayList<Any>()
            var i = 0
            while (i < sig.length) {
                val c = sig[i]
                if (i < sig.length - 1 && sig[i + 1] == 'R') {
                    i++
                    val n = u8()
                    repeat(n) { r.add(readOperand(c)) }
                } else r.add(readOperand(c))
                i++
            }
            return r
        }

        fun readInstruction(): Pair<Int, MutableList<Any>> {
            val op = u8()
            val sig = ops[op] ?: throw Fail()
            return Pair(op, readOperands(sig))
        }

        fun run() {
            val max = d.size - 8
            while (pos < max) {
                val (op, opers) = readInstruction()
                when (op) {
                    0x0F -> {
                        val n = opers.removeAt(0) as Int
                        repeat(n) {
                            val o = readOperands("HSBH")
                            val s = o[1] as Str
                            if (s.len > 1) { s.typ = MSG; texts.add(s) }
                            readInstruction()
                        }
                    }
                    0x14 -> {
                        if (opers.size < 3) throw Fail()
                        val s = opers.removeAt(2) as? Str ?: throw Fail()
                        if (s.len > 1) { s.typ = MSG; texts.add(s) }
                    }
                    0x15 -> {
                        if (opers.isEmpty()) throw Fail()
                        val s = opers.removeAt(0) as? Str ?: throw Fail()
                        if (s.len > 1) { s.typ = NAME; texts.add(s) }
                    }
                    0x3F -> {
                        var preChar = false
                        for (o in opers) {
                            val s = o as? Str ?: throw Fail()
                            if (s.len == 5 && String(d, s.off, 4, Charsets.US_ASCII) == "char") preChar = true
                            else if (preChar && s.len > 1) { s.typ = MSG; preChar = false }
                        }
                    }
                    0x72 -> {
                        if (opers.size < 2) throw Fail()
                        (opers[1] as? Str ?: throw Fail()).typ = MSG
                    }
                }
                for (o in opers) if (o is Str && o.len > 1) texts.add(o)
            }
            texts.sortBy { it.off }
        }
    }

    private fun rotl2(b: ByteArray) = ByteArray(b.size) { val v = b[it].toInt() and 0xff; (((v shl 2) or (v ushr 6)) and 0xff).toByte() }
    private fun rotr2(b: ByteArray) = ByteArray(b.size) { val v = b[it].toInt() and 0xff; (((v ushr 2) or (v shl 6)) and 0xff).toByte() }

    private fun tryParse(d: ByteArray, enc: Boolean): Parsed? {
        for (ops in Ws2Ops.ALL) {
            try {
                val x = Dis(d, ops); x.run()
                return Parsed(d, enc, x.texts, x.addrs)
            } catch (e: Fail) { } catch (e: IndexOutOfBoundsException) { }
        }
        return null
    }

    private fun parse(b: ByteArray): Parsed {
        if (b.size < 16) throw IOException("Bukan skrip WS2")
        return tryParse(b, false) ?: tryParse(rotr2(b), true) ?: throw IOException("Bukan skrip WS2 yang dikenali (opcode tidak cocok V1–V3)")
    }

    fun isWs2(b: ByteArray): Boolean = b.size >= 16 && try { parse(b); true } catch (e: Exception) { false }

    private fun txt(p: Parsed, s: Str, cs: Charset) = String(p.data, s.off, s.textLen, cs)

    private fun split(typ: Int, t: String): Triple<String, String, String> = // (prefix, content, suffix)
        if (typ == NAME) {
            val pre = if (t.startsWith("%LC")) "%LC" else if (t.startsWith("%LF")) "%LF" else ""
            Triple(pre, t.substring(pre.length), "")
        } else {
            val suf = if (t.endsWith("%K%P")) "%K%P" else ""
            Triple("", t.substring(0, t.length - suf.length), suf)
        }

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b)
        val out = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        for ((i, s) in p.strs.withIndex()) {
            if (s.typ == INTERNAL) continue
            val body = split(s.typ, txt(p, s, cs)).second
            if (body.isBlank()) continue
            if (seen.add((if (s.typ == NAME) "N" else "M") + body))
                out.add(TextUnit(fileKey, (if (s.typ == NAME) "N" else "M") + "$i:" + ScriptText.hash(body), body.replace("\r\n", "\n")))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (c > 1 && (k[0] == 'N' || k[0] == 'M')) byHash.putIfAbsent(k[0] + k.substring(c + 1), v) }
        class Rep(val off: Int, val oldLen: Int, val bytes: ByteArray)
        val reps = ArrayList<Rep>()
        for ((i, s) in p.strs.withIndex()) {
            if (s.typ == INTERNAL) continue
            val (pre, body, suf) = split(s.typ, txt(p, s, cs))
            val tag = if (s.typ == NAME) "N" else "M"
            val h = ScriptText.hash(body)
            val t = tr["$tag$i:$h"] ?: byHash[tag + h] ?: continue
            if (t == body) continue
            val nb = (pre + t.replace("\r\n", "\n") + suf).toByteArray(cs)
            reps.add(Rep(s.off, s.len, nb + byteArrayOf(0)))
        }
        if (reps.isEmpty()) return Pair(b, 0)
        reps.sortBy { it.off }
        val out = ByteArrayOutputStream()
        var cur = 0
        for (r in reps) { out.write(p.data, cur, r.off - cur); out.write(r.bytes); cur = r.off + r.oldLen }
        out.write(p.data, cur, p.data.size - cur)
        val res = out.toByteArray()
        // old -> new position map
        fun map(old: Int): Int {
            var delta = 0
            for (r in reps) if (r.off + r.oldLen <= old) delta += r.bytes.size - r.oldLen
            return old + delta
        }
        val bb = ByteBuffer.wrap(res).order(ByteOrder.LITTLE_ENDIAN)
        for (a in p.addrs) {
            val np = map(a)
            val v = bb.getInt(np)
            bb.putInt(np, map(v))
        }
        return Pair(if (p.encrypted) rotl2(res) else res, reps.size)
    }
}
