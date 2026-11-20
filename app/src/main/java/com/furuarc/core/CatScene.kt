package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * CatSystem2 script (.cst, magic "CatScene"). Dialog (type 0x20), speaker name (0x21) and command text
 * (0x30, e.g. choices) are taken as [TextUnit] keyed `S<index>:<hash>`. Follows the format in msg-tool (MIT).
 */
object CatScene {
    private class Str(val index: Int, val type: Int, val text: String, val address: Int, val len: Int)

    private class Parsed(val inner: ByteArray, val compressed: Boolean, val strs: List<Str>, val addrOff: Int, val strOff: Int)

    private val CMD = Regex("^\\d+\\s+\\w+\\s+(.+)")

    fun isCst(b: ByteArray): Boolean = b.size >= 0x20 && String(b, 0, 8, Charsets.ISO_8859_1) == "CatScene"

    private fun u32(b: ByteArray, o: Int): Int = ByteBuffer.wrap(b, o, 4).order(ByteOrder.LITTLE_ENDIAN).int

    private fun put32(b: ByteArray, o: Int, v: Int) {
        ByteBuffer.wrap(b, o, 4).order(ByteOrder.LITTLE_ENDIAN).putInt(v)
    }

    private fun parse(b: ByteArray, cs: Charset): Parsed {
        require(isCst(b)) { "bukan file CatScene" }
        val comp = u32(b, 8)
        val unc = u32(b, 12)
        val inner: ByteArray = if (comp == 0) b.copyOfRange(0x10, b.size) else {
            val inf = Inflater()
            inf.setInput(b, 0x10, b.size - 0x10)
            val out = ByteArray(unc)
            var n = 0
            while (n < unc && !inf.finished()) {
                val r = inf.inflate(out, n, unc - n)
                if (r == 0 && (inf.needsInput() || inf.needsDictionary())) break
                n += r
            }
            inf.end()
            require(n == unc) { "zlib tidak lengkap" }
            out
        }
        require(inner.size >= 0x10) { "data terlalu kecil" }
        val addrOff = 0x10 + u32(inner, 8)
        val strOff = 0x10 + u32(inner, 12)
        require(addrOff in 0x10..strOff && strOff <= inner.size) { "header tidak valid" }
        val count = (strOff - addrOff) / 4
        val list = ArrayList<Str>(count)
        for (i in 0 until count) {
            val off = u32(inner, addrOff + i * 4) + strOff
            require(off in 0 until inner.size - 1 && inner[off].toInt() == 1) { "penanda string $i tidak valid" }
            val type = inner[off + 1].toInt() and 0xff
            var e = off + 2
            while (e < inner.size && inner[e].toInt() != 0) e++
            require(e < inner.size) { "string $i tidak berakhir" }
            list.add(Str(i, type, String(inner, off + 2, e - off - 2, cs), off, e - off - 1))
        }
        return Parsed(inner, comp != 0, list, addrOff, strOff)
    }

    /** Summary for Check: string count, compressed or not, text count, and structural problems. */
    class Stats(val strings: Int, val compressed: Boolean, val units: Int, val problems: List<String>)

    fun stats(b: ByteArray, cs: Charset): Stats {
        val p = parse(b, cs)
        val problems = ArrayList<String>()
        if (u32(p.inner, 0) != p.inner.size - 0x10) problems.add("data_length")
        var units = 0
        for (s in p.strs) when (s.type) {
            0x20, 0x21 -> if (s.text.isNotEmpty()) units++
            0x30 -> if (CMD.containsMatchIn(s.text)) units++
        }
        return Stats(p.strs.size, p.compressed, units, problems)
    }

    private fun hash(s: String) = ScriptText.hash(s)

    private fun unit(file: String, s: Str, text: String) = TextUnit(file, "S${s.index}:${hash(s.text)}", text)

    fun extract(fileKey: String, b: ByteArray, cs: Charset): List<TextUnit> {
        val p = parse(b, cs)
        val r = ArrayList<TextUnit>()
        for (s in p.strs) when (s.type) {
            0x20 -> if (s.text.isNotEmpty()) r.add(unit(fileKey, s, s.text.replace("\\n", "\n")))
            0x21 -> if (s.text.isNotEmpty()) r.add(unit(fileKey, s, s.text))
            0x30 -> CMD.find(s.text)?.let { r.add(unit(fileKey, s, it.groupValues[1])) }
        }
        return r
    }

    /** Apply [tr]; returns (new file, number of texts replaced). Text that cannot be encoded in [cs] throws an error. */
    fun apply(b: ByteArray, tr: Map<String, String>, cs: Charset): Pair<ByteArray, Int> {
        val p = parse(b, cs)
        val base = p.inner.copyOf()
        val extra = ByteArrayOutputStream()
        var changed = 0
        val enc = cs.newEncoder()
        for (s in p.strs) {
            val t = tr["S${s.index}:${hash(s.text)}"] ?: continue
            val newText: String = when (s.type) {
                0x20 -> t.replace("\r\n", "\n").replace("\n", "\\n")
                0x21 -> t.replace("\r\n", " ").replace("\n", " ")
                0x30 -> {
                    val m = CMD.find(s.text) ?: continue
                    val g = m.groups[1]!!.range
                    s.text.substring(0, g.first) + t.replace("\r\n", " ").replace("\n", " ") + s.text.substring(g.last + 1)
                }
                else -> continue
            }
            if (newText == s.text) continue
            require(enc.canEncode(newText)) { "teks \"${t.take(20)}…\" memuat huruf yang tidak ada di encoding ${cs.name()}" }
            val data = newText.toByteArray(cs)
            val pos: Int
            if (data.size + 1 > s.len) {
                // longer than the old space: write at the end of the data and point the address table there
                pos = base.size + extra.size()
                extra.write(1); extra.write(s.type); extra.write(data); extra.write(0)
                put32(base, p.addrOff + s.index * 4, pos - p.strOff)
            } else {
                pos = s.address
                base[pos] = 1; base[pos + 1] = s.type.toByte()
                System.arraycopy(data, 0, base, pos + 2, data.size)
                base[pos + 2 + data.size] = 0
            }
            changed++
        }
        if (changed == 0) return Pair(b, 0)
        val inner = base + extra.toByteArray()
        put32(inner, 0, inner.size - 0x10)
        val out = ByteArrayOutputStream()
        out.write("CatScene".toByteArray(Charsets.ISO_8859_1))
        val head = ByteArray(8)
        out.write(head)
        if (p.compressed) {
            val d = Deflater(6)
            d.setInput(inner); d.finish()
            val buf = ByteArray(65536)
            while (!d.finished()) { val n = d.deflate(buf); out.write(buf, 0, n) }
            d.end()
        } else out.write(inner)
        val res = out.toByteArray()
        put32(res, 8, if (p.compressed) res.size - 0x10 else 0)
        put32(res, 12, inner.size)
        return Pair(res, changed)
    }
}
