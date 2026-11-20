package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * Escu:de binary script "ESCR1_00" (port from msg-tool, escude/script.rs).
 * Layout: "ESCR1_00", u32 string count, u32 offset[count], u32 VM size, VM bytes, u32 unk, then NUL-terminated strings.
 * Bytecode references strings by index, so rewriting the string table (offsets recomputed) is safe.
 * In Shift-JIS, bytes 0xA0..0xDE are used by the game as glyphs (！？　。「」…、kana) and mapped to full-width characters like msg-tool.
 * "<r>" = newline.
 */
object EscudeScript {
    private const val MAGIC = "ESCR1_00"
    private const val KANA = "！？　。「」、…をぁぃぅぇぉゃゅょっーあいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわん゛゜"

    private class Parsed(val vm: ByteArray, val unk: Int, val strs: List<ByteArray>)

    fun isScript(b: ByteArray) = b.size >= 16 && String(b, 0, 8, Charsets.US_ASCII) == MAGIC

    private fun parse(b: ByteArray): Parsed {
        if (!isScript(b)) throw IOException("Bukan skrip Escu:de (ESCR1_00)")
        val bb = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN)
        val n = bb.getInt(8)
        if (n < 0 || 12L + n * 4L + 4 > b.size) throw IOException("Jumlah string tidak valid")
        var p = 12 + n * 4
        val vmSize = bb.getInt(p); p += 4
        if (vmSize < 0 || p.toLong() + vmSize + 4 > b.size) throw IOException("Ukuran VM tidak valid")
        val vm = b.copyOfRange(p, p + vmSize); p += vmSize
        val unk = bb.getInt(p); p += 4
        val strs = ArrayList<ByteArray>(n)
        repeat(n) {
            var e = p
            while (e < b.size && b[e].toInt() != 0) e++
            if (e >= b.size) throw IOException("String terpotong")
            strs.add(b.copyOfRange(p, e)); p = e + 1
        }
        return Parsed(vm, unk, strs)
    }

    private fun isSjis(cs: Charset) = cs.name().equals("windows-31j", true) || cs.name().equals("Shift_JIS", true)

    private val FWD: Map<Int, Char> by lazy {
        val m = HashMap<Int, Char>()
        // byte 0xA0.. -> the 2nd character.. in KANA (the first two entries for '!' and '?' are skipped)
        for (i in 2 until KANA.length) m[0xA0 + i - 2] = KANA[i]
        m
    }
    private val REV: Map<Char, Int> by lazy { FWD.entries.associate { it.value to it.key } }

    private fun decodeStr(raw: ByteArray, cs: Charset): String {
        if (!isSjis(cs)) return String(raw, cs)
        val sb = StringBuilder()
        var i = 0
        while (i < raw.size) {
            val v = raw[i].toInt() and 0xff
            val lead = (v in 0x81..0x9f) || (v in 0xe0..0xfc)
            if (lead && i + 1 < raw.size) { sb.append(String(raw, i, 2, cs)); i += 2 }
            else {
                val g = FWD[v]
                if (g != null) sb.append(g) else sb.append(String(raw, i, 1, cs))
                i++
            }
        }
        return sb.toString().replace("<r>", "\n")
    }

    private fun encodeStr(s0: String, cs: Charset): ByteArray {
        val s = s0.replace("\r\n", "\n").replace("\n", "<r>")
        if (!isSjis(cs)) return s.toByteArray(cs)
        val bo = ByteArrayOutputStream()
        for (c in s) {
            val g = REV[c]
            if (g != null) bo.write(g) else bo.write(c.toString().toByteArray(cs))
        }
        return bo.toByteArray()
    }

    fun translatable(s: String): Boolean = SiglusScene.translatable(s)

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b)
        val out = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        for ((i, raw) in p.strs.withIndex()) {
            val s = decodeStr(raw, cs)
            if (translatable(s) && seen.add(s)) out.add(TextUnit(fileKey, "S$i:${ScriptText.hash(s)}", s))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        var changed = 0
        val strs = ArrayList<ByteArray>(p.strs.size)
        for ((i, raw) in p.strs.withIndex()) {
            val s = decodeStr(raw, cs)
            val t = tr["S$i:${ScriptText.hash(s)}"] ?: byHash[ScriptText.hash(s)]
            if (t == null || t == s) { strs.add(raw); continue }
            strs.add(encodeStr(t, cs)); changed++
        }
        if (changed == 0) return Pair(b, 0)
        val bo = ByteArrayOutputStream()
        fun w32(v: Int) { for (k in 0..3) bo.write((v ushr (8 * k)) and 0xff) }
        bo.write(MAGIC.toByteArray(Charsets.US_ASCII))
        w32(strs.size)
        var off = 0
        for (s in strs) { w32(off); off += s.size + 1 }
        w32(p.vm.size); bo.write(p.vm); w32(p.unk)
        for (s in strs) { bo.write(s); bo.write(0) }
        return Pair(bo.toByteArray(), changed)
    }
}
