package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/**
 * BGI/Ethornell script (port of msg-tool bgi/parser.rs + script.rs). Supports forms V0 (16-bit opcodes), V1 (32-bit opcodes,
 * headerless) and "BurikoCompiledScriptVer1.00" (V1 with header). Text (messages, names, choices) are strings pointed to by pointers
 * inside the code. On import, changed strings are written at the END of the file and their pointers redirected there (msg-tool's method).
 * Ruby is unchanged (ruby strings are left alone). BGI archive files are usually DSC-compressed: unpack with the "Decode DSC" option.
 */
object BgiScript {
    private const val V1_MAGIC = "BurikoCompiledScriptVer1.00"
    private const val NAME = 1
    private const val MSG = 2
    private const val INTERNAL = 0
    private const val RUBY = 3

    private val V0 = mapOf(0x0010 to "IIM", 0x0012 to "ZZ", 0x0013 to "Z", 0x0014 to "Z", 0x0018 to "IIIII", 0x0019 to "IIII", 0x001A to "III", 0x001B to "ZIII", 0x001F to "I", 0x0022 to "I", 0x0024 to "IIIII", 0x0025 to "II", 0x0028 to "ZI", 0x0029 to "ZZI", 0x002A to "I", 0x002B to "ZI", 0x002C to "ZIIIIIIII", 0x002D to "ZIIIIIIII", 0x002E to "IIIII", 0x0030 to "ZI", 0x0031 to "ZII", 0x0032 to "I", 0x0033 to "I", 0x0034 to "II", 0x0035 to "I", 0x0036 to "I", 0x0038 to "IZIIIII", 0x0039 to "II", 0x003A to "IZIIIIIIII", 0x003B to "IIIIII", 0x003C to "IIIIIIIIII", 0x003D to "IIIIIIIIIII", 0x003F to "I", 0x0040 to "IIZII", 0x0041 to "IIZII", 0x0042 to "IIZI", 0x0043 to "IIZI", 0x0044 to "IIZI", 0x0045 to "IIZI", 0x0046 to "IZI", 0x0047 to "IZI", 0x0048 to "II", 0x0049 to "II", 0x004A to "IZI", 0x004C to "ZI", 0x004D to "ZI", 0x004E to "I", 0x004F to "I", 0x0050 to "ZI", 0x0051 to "ZZI", 0x0052 to "I", 0x0053 to "ZI", 0x0054 to "ZII", 0x0060 to "IIIII", 0x0061 to "II", 0x0062 to "IIIIII", 0x0065 to "I", 0x0066 to "II", 0x0067 to "I", 0x0068 to "I", 0x0069 to "I", 0x006A to "I", 0x006B to "I", 0x006C to "I", 0x006E to "III", 0x006F to "I", 0x0070 to "IZI", 0x0071 to "I", 0x0072 to "III", 0x0073 to "III", 0x0074 to "IZI", 0x0075 to "I", 0x0076 to "III", 0x0078 to "IZI", 0x0079 to "I", 0x007A to "III", 0x0080 to "IZII", 0x0081 to "Z", 0x0082 to "I", 0x0083 to "I", 0x0084 to "IZI", 0x0085 to "Z", 0x0086 to "I", 0x0087 to "I", 0x0088 to "Z", 0x008C to "I", 0x008D to "I", 0x008E to "I", 0x0090 to "I", 0x0091 to "I", 0x0092 to "I", 0x0093 to "I", 0x0094 to "I", 0x0098 to "II", 0x0099 to "II", 0x009A to "II", 0x009B to "II", 0x009C to "II", 0x009D to "II", 0x00A0 to "C", 0x00A1 to "IC", 0x00A2 to "IC", 0x00A3 to "IIC", 0x00A4 to "IIC", 0x00A5 to "IIC", 0x00A6 to "IIC", 0x00A7 to "IIC", 0x00A8 to "IIC", 0x00AC to "C", 0x00AE to "I", 0x00C0 to "Z", 0x00C1 to "Z", 0x00C4 to "I", 0x00C8 to "Z", 0x00CA to "I", 0x00D4 to "I", 0x00D8 to "I", 0x00D9 to "I", 0x00DA to "I", 0x00DB to "I", 0x00DC to "I", 0x00F8 to "Z", 0x00F9 to "ZI", 0x00FE to "H", 0x0110 to "ZZ", 0x0111 to "I", 0x0120 to "I", 0x0121 to "I", 0x0128 to "ZII", 0x012A to "II", 0x0134 to "II", 0x0135 to "I", 0x0136 to "I", 0x0138 to "IZIIIIZIII", 0x013B to "IIIIIIII", 0x0140 to "IIZIIII", 0x0141 to "IIZIIII", 0x0142 to "IIZIII", 0x0143 to "IIZIII", 0x0144 to "IIZIII", 0x0145 to "IIZIII", 0x0146 to "IZIII", 0x0147 to "IZIII", 0x0148 to "II", 0x0149 to "II", 0x014B to "ZIIZ", 0x0150 to "ZII", 0x0151 to "ZIII", 0x0152 to "II", 0x0153 to "III", 0x016E to "IIIIII", 0x016F to "IIIIIII", 0x0170 to "IZZII", 0x01C0 to "ZZ", 0x01C1 to "ZZ", 0x0249 to "Z", 0x024C to "ZZIII", 0x024D to "Z", 0x024E to "ZZ", 0x024F to "Z")
    private val V1 = mapOf(0x0000 to "I", 0x0001 to "C", 0x0002 to "I", 0x0008 to "I", 0x0009 to "I", 0x000A to "I", 0x0017 to "I", 0x0019 to "I", 0x003F to "I", 0x007B to "III", 0x007E to "I", 0x007F to "II")

    private class S(val ptr: Int, val addr: Int, val typ: Int)
    private class Parsed(val d: ByteArray, val offset: Int, val strs: List<S>, val v1: Boolean)
    private class Fail : Exception()

    private class Rd(val d: ByteArray, var pos: Int) {
        fun u16(): Int { if (pos + 2 > d.size) throw Fail(); val v = (d[pos].toInt() and 0xff) or ((d[pos + 1].toInt() and 0xff) shl 8); pos += 2; return v }
        fun u32(): Int { if (pos + 4 > d.size) throw Fail(); val v = ByteBuffer.wrap(d, pos, 4).order(ByteOrder.LITTLE_ENDIAN).int; pos += 4; return v }
        fun cstr() { while (true) { if (pos >= d.size) throw Fail(); if (d[pos++].toInt() == 0) return } }
    }

    private fun v0(d: ByteArray): List<S> {
        val r = Rd(d, 0); val out = ArrayList<S>(); var largest = 0L
        fun code() { val a = r.u32().toLong() and 0xffffffffL; if (a > largest) largest = a }
        fun opers(t: String) {
            for (c in t) when (c) {
                'H' -> r.pos += 2
                'I' -> r.pos += 4
                'C' -> code()
                'M' -> { val p = r.pos; val a = r.u32(); out.add(S(p, a, MSG)) }
                'N' -> { val p = r.pos; val a = r.u32(); out.add(S(p, a, NAME)) }
                'Z' -> r.cstr()
            }
            if (r.pos > d.size) throw Fail()
        }
        while (true) {
            val op = r.u16()
            when (op) {
                0xa9 -> { val n = r.u32(); if (n < 0 || n > d.size) throw Fail(); repeat(n) { code() } }
                0xb0, 0xb4 -> { val n = r.u32(); if (n < 0 || n > d.size) throw Fail(); repeat(n) { r.cstr() } }
                0xfd -> { val n = r.u32(); if (n < 0 || n > d.size) throw Fail(); repeat(n) { r.cstr(); code() } }
                else -> V0[op]?.let { opers(it) }
            }
            if (op == 0xc2 && largest < r.pos) break
        }
        return out
    }

    private class Stk(val ptr: Int, val addr: Int)

    private fun v1(d: ByteArray, offset: Int): List<S> {
        val r = Rd(d, offset); val out = ArrayList<S>(); var largest = 0L
        val stack = ArrayList<Stk>()
        fun empty(a: Int): Boolean { val p = offset + a; if (p < 0 || p >= d.size) throw Fail(); return d[p].toInt() == 0 }
        fun str(a: Int): String { val p = offset + a; if (p < 0 || p >= d.size) throw Fail(); var e = p; while (e < d.size && d[e].toInt() != 0) e++; return String(d, p, e - p, Charsets.ISO_8859_1) }
        fun code() { val a = r.u32().toLong() and 0xffffffffL; if (a > largest) largest = a }
        fun opers(t: String) {
            for (c in t) when (c) {
                'H' -> r.pos += 2
                'I' -> r.pos += 4
                'C' -> code()
                'M' -> { val p = r.pos; val a = r.u32(); out.add(S(p, a, MSG)) }
                'N' -> { val p = r.pos; val a = r.u32(); out.add(S(p, a, NAME)) }
                'Z' -> r.cstr()
            }
            if (r.pos > d.size) throw Fail()
        }
        fun pop(): Stk { if (stack.isEmpty()) throw Fail(); return stack.removeAt(stack.size - 1) }
        fun choice() { val l = ArrayList<Stk>(); while (stack.isNotEmpty()) l.add(0, stack.removeAt(stack.size - 1)); for (c in l) out.add(S(c.ptr, c.addr, MSG)) }
        fun internals() { while (stack.isNotEmpty()) { val s = stack.removeAt(stack.size - 1); out.add(S(s.ptr, s.addr, INTERNAL)) } }
        while (true) {
            val op = r.u32()
            when {
                op == 0x03 -> { val p = r.pos; val a = r.u32(); stack.add(Stk(p, a)) }
                op == 0x1c -> {
                    if (stack.isNotEmpty()) {
                        val it = stack.removeAt(stack.size - 1)
                        out.add(S(it.ptr, it.addr, INTERNAL))
                        val fn = str(it.addr)
                        if (fn == "_SelectEx" || fn == "_SelectExtend") choice()
                    }
                }
                op == 0x140 || op == 0x143 -> {
                    val item = pop()
                    if (stack.isNotEmpty()) { val s = stack.removeAt(stack.size - 1); out.add(S(s.ptr, s.addr, if (empty(s.addr)) INTERNAL else NAME)) }
                    out.add(S(item.ptr, item.addr, if (empty(item.addr)) INTERNAL else MSG))
                }
                op == 0x160 -> choice()
                V1[op] != null -> opers(V1[op]!!)
                op == 0x145 -> {
                    val item = pop()
                    if (stack.isNotEmpty()) {
                        val s = stack.removeAt(stack.size - 1)
                        out.add(S(item.ptr, item.addr, if (empty(item.addr)) INTERNAL else NAME))
                        out.add(S(s.ptr, s.addr, if (empty(s.addr)) INTERNAL else MSG))
                    } else out.add(S(item.ptr, item.addr, if (empty(item.addr)) INTERNAL else MSG))
                }
                op == 0x14e -> { val dst = pop(); val ori = pop(); out.add(S(ori.ptr, ori.addr, RUBY)); out.add(S(dst.ptr, dst.addr, RUBY)) }
            }
            if ((op == 0x1b || op == 0xf4) && largest < r.pos - offset) break
            if (op == 0x7e || op == 0x7f || op == 0xfe || op == 0x1b5) internals()
        }
        internals()
        return out
    }

    private fun parse(d: ByteArray): Parsed {
        if (d.size < 32) throw IOException("Bukan skrip BGI")
        val hdr = String(d, 0, V1_MAGIC.length, Charsets.ISO_8859_1) == V1_MAGIC && d[V1_MAGIC.length].toInt() == 0
        if (hdr) {
            val off = 28 + ByteBuffer.wrap(d, 28, 4).order(ByteOrder.LITTLE_ENDIAN).int
            if (off < 32 || off >= d.size) throw IOException("Kepala BGI tidak valid")
            try { return Parsed(d, off, v1(d, off), true) } catch (e: Fail) { throw IOException("Skrip BGI tidak terbaca") }
        }
        try { return Parsed(d, 0, v0(d), false) } catch (e: Fail) { }
        try { return Parsed(d, 0, v1(d, 0), false) } catch (e: Fail) { }
        throw IOException("Bukan skrip BGI yang dikenali")
    }

    fun isHeader(b: ByteArray) = b.size >= 32 && String(b, 0, V1_MAGIC.length, Charsets.ISO_8859_1) == V1_MAGIC

    private fun read(p: Parsed, s: S, cs: Charset): String {
        val st = p.offset + s.addr
        if (st < 0 || st >= p.d.size) throw IOException("Pointer string BGI di luar file")
        var e = st; while (e < p.d.size && p.d[e].toInt() != 0) e++
        return String(p.d, st, e - st, cs)
    }

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b)
        val out = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        for ((i, s) in p.strs.withIndex()) {
            if (s.typ != NAME && s.typ != MSG) continue
            val t = read(p, s, cs)
            if (t.isBlank()) continue
            val tag = if (s.typ == NAME) "N" else "M"
            if (seen.add(tag + t)) out.add(TextUnit(fileKey, "$tag$i:" + ScriptText.hash(t), t))
        }
        return out
    }

    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (c > 1 && (k[0] == 'N' || k[0] == 'M')) byHash.putIfAbsent(k[0] + k.substring(c + 1), v) }
        val tail = ByteArrayOutputStream()
        val added = HashMap<String, Int>() // text -> relative address
        val res = b.copyOf()
        val bb = ByteBuffer.wrap(res).order(ByteOrder.LITTLE_ENDIAN)
        var changed = 0
        for ((i, s) in p.strs.withIndex()) {
            if (s.typ != NAME && s.typ != MSG) continue
            val t = read(p, s, cs)
            val tag = if (s.typ == NAME) "N" else "M"
            val nt = tr["$tag$i:${ScriptText.hash(t)}"] ?: byHash[tag + ScriptText.hash(t)] ?: continue
            if (nt == t) continue
            val addr = added.getOrPut(nt) {
                val a = b.size + tail.size() - p.offset
                tail.write(nt.toByteArray(cs)); tail.write(0)
                a
            }
            bb.putInt(s.ptr, addr)
            changed++
        }
        if (changed == 0) return Pair(b, 0)
        val out = ByteArrayOutputStream()
        out.write(res); out.write(tail.toByteArray())
        return Pair(out.toByteArray(), changed)
    }
}
