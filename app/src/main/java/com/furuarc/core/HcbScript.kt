package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Favorite/FVP .hcb script (port of msg-tool favorite/disasm.rs + hcb.rs).
 * Layout: u32 code length (script_len), instructions up to script_len, u32 main script address @script_len,
 * then extra data (including the syscall list). call/jmp/jmpcond (0x02/0x06/0x07) addresses are absolute and recomputed on import;
 * so is the pushint (0x0a) address right before the ThreadStart syscall.
 * Text = the string operand of pushstring (0x0e); strings that are entirely ASCII identifiers (resource names) are skipped.
 */
object HcbScript {
    private val OPS: Map<Int, String> = HashMap<Int, String>().also { m ->
        // B=1, W=2, D=4, S=length-prefixed string, F=4
        val t = listOf(
            0x00 to "", 0x01 to "BB", 0x02 to "D", 0x03 to "W", 0x04 to "", 0x05 to "", 0x06 to "D", 0x07 to "D",
            0x08 to "", 0x09 to "", 0x0a to "D", 0x0b to "W", 0x0c to "B", 0x0d to "F", 0x0e to "S", 0x0f to "W",
            0x10 to "B", 0x11 to "W", 0x12 to "B", 0x13 to "", 0x14 to "", 0x15 to "W", 0x16 to "B", 0x17 to "W", 0x18 to "B",
        )
        for ((k, v) in t) m[k] = v
        for (k in 0x19..0x27) m[k] = ""
        for (k in intArrayOf(0x33, 0x3f, 0x40, 0xb3, 0xb8, 0xd8, 0xf0, 0x52, 0x9e)) m[k] = ""
    }

    class Ins(val pos: Int, val op: Int, val argStart: Int, val end: Int, val str: ByteArray?)

    private class Parsed(val ins: List<Ins>, val scriptLen: Int, val threadStartId: Int)

    fun isHcb(b: ByteArray): Boolean {
        if (b.size < 16) return false
        val n = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt(0)
        return n in 8 until b.size - 4 && try { parse(b); true } catch (e: Exception) { false }
    }

    private fun parse(b: ByteArray): Parsed {
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        val sl = bb.getInt(0)
        if (sl < 5 || sl.toLong() + 4 > b.size) throw IOException("Bukan skrip HCB")
        val ins = ArrayList<Ins>()
        var p = 4
        while (p < sl) {
            val op = b[p].toInt() and 0xff
            val sig = OPS[op] ?: throw IOException("Opcode HCB tidak dikenal: 0x%02x di 0x%x".format(op, p))
            var q = p + 1
            var str: ByteArray? = null
            for (c in sig) {
                when (c) {
                    'B' -> q += 1
                    'W' -> q += 2
                    'D', 'F' -> q += 4
                    'S' -> {
                        val len = b[q].toInt() and 0xff
                        if (len < 1 || q + 1 + len > b.size || b[q + len].toInt() != 0) throw IOException("String HCB rusak di 0x%x".format(p))
                        str = b.copyOfRange(q + 1, q + len)
                        q += 1 + len
                    }
                }
            }
            if (q > sl) throw IOException("Instruksi HCB melewati batas")
            ins.add(Ins(p, op, p + 1, q, str))
            p = q
        }
        // syscall list -> ThreadStart id
        var ts = -1
        try {
            var off = sl + 10
            off += 1 + (b[off].toInt() and 0xff)
            val n = (b[off].toInt() and 0xff) or ((b[off + 1].toInt() and 0xff) shl 8)
            off += 2
            for (i in 0 until n) {
                val len = b[off + 1].toInt() and 0xff
                val s = String(b, off + 2, maxOf(len - 1, 0), Charsets.US_ASCII)
                if (s == "ThreadStart") ts = i
                off += 2 + len
            }
        } catch (e: Exception) { }
        return Parsed(ins, sl, ts)
    }

    private fun translatable(s: String): Boolean =
        s.isNotBlank() && (s.any { it.code >= 0x80 } || (SiglusScene.translatable(s) && s.any { it == ' ' || it == '\n' }))

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b)
        val out = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        for ((i, x) in p.ins.withIndex()) {
            val raw = x.str ?: continue
            val s = String(raw, cs)
            if (translatable(s) && seen.add(s)) out.add(TextUnit(fileKey, "S$i:${ScriptText.hash(s)}", s.replace("\r\n", "\n")))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        val newStr = HashMap<Int, ByteArray>()
        var changed = 0
        for ((i, x) in p.ins.withIndex()) {
            val raw = x.str ?: continue
            val s = String(raw, cs)
            val t = tr["S$i:${ScriptText.hash(s)}"] ?: byHash[ScriptText.hash(s)] ?: continue
            if (t == s) continue
            val enc = t.replace("\r\n", "\n").toByteArray(cs)
            if (enc.size + 1 > 255) continue // too long for a 1-byte length: left as is
            newStr[i] = enc; changed++
        }
        if (changed == 0) return Pair(b, 0)
        // rebuild the code + position map
        val code = ByteArrayOutputStream()
        val map = HashMap<Int, Int>()
        val fixups = ArrayList<Int>() // positions (in the output) of u32 operands that hold an address
        for ((i, x) in p.ins.withIndex()) {
            map[x.pos] = 4 + code.size()
            val ns = newStr[i]
            if (ns != null) {
                code.write(x.op); code.write(ns.size + 1); code.write(ns); code.write(0)
            } else {
                if (x.op == 0x02 || x.op == 0x06 || x.op == 0x07) fixups.add(4 + code.size() + 1)
                if (x.op == 0x03 && i > 0 && p.threadStartId >= 0) {
                    val id = (b[x.argStart].toInt() and 0xff) or ((b[x.argStart + 1].toInt() and 0xff) shl 8)
                    val prev = p.ins[i - 1]
                    if (id == p.threadStartId && prev.op == 0x0a) fixups.add(map[prev.pos]!! + 1)
                }
                code.write(b, x.pos, x.end - x.pos)
            }
        }
        val newLen = 4 + code.size()
        map[p.scriptLen] = newLen
        val body = code.toByteArray()
        val out = ByteArrayOutputStream()
        out.write(ByteArray(4)); out.write(body)
        out.write(b, p.scriptLen, b.size - p.scriptLen)
        val r = out.toByteArray()
        val rb = ByteBuffer.wrap(r).order(ByteOrder.LITTLE_ENDIAN)
        rb.putInt(0, newLen)
        for (pos in fixups) {
            val old = rb.getInt(pos)
            val nw = map[old] ?: throw IOException("Alamat lompat 0x%x tidak menuju awal instruksi".format(old))
            rb.putInt(pos, nw)
        }
        // main script address at script_len
        val oldMain = rb.getInt(newLen)
        map[oldMain]?.let { rb.putInt(newLen, it) }
        return Pair(r, changed)
    }
}
