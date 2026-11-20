package com.furuarc.core

/**
 * Artemis `.ast` (Lua table as text): `ast = { block_00000 = { {tag...}, text = { ja = { { name = {"id","tampil"}, "baris", {"rt2"} } } } } }`.
 * Extracted: strings inside `text`/`select` of each block (any language key except vo, vl1.., linemode, pagebreak), the 2nd element of `name`
 * (the displayed name), and `{"ruby", text="..."}` text. Tags, file names, and expressions are untouched; only literal contents are replaced,
 * so whitespace, order, and the file's CRLF are preserved. Identical text within one file is merged (one entry, applied to all occurrences).
 */
object AstScript {
    private sealed class AN
    private class ATable(val entries: List<AEntry>) : AN()
    private class AEntry(val key: String?, val value: AN)
    private class AStr(val start: Int, val end: Int, val value: String, val quote: Char) : AN()
    private object AAtom : AN()

    private class Unit(val str: AStr, val kind: Char, val block: String, val n: Int)

    private val HEAD = Regex("(?m)^\\s*ast\\s*=\\s*\\{")

    fun isAst(text: String): Boolean = text.contains("astver") && HEAD.containsMatchIn(text) || HEAD.containsMatchIn(text) && text.contains("block_")

    private class Parser(val s: String) {
        var i = 0
        fun fail(): Nothing = throw java.io.IOException("Format .ast tidak dikenali (posisi $i)")

        fun ws() {
            while (i < s.length) {
                val c = s[i]
                if (c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '﻿') i++
                else if (c == '-' && s.startsWith("--", i)) {
                    if (s.startsWith("--[[", i)) {
                        val e = s.indexOf("]]", i + 4)
                        i = if (e < 0) s.length else e + 2
                    } else {
                        while (i < s.length && s[i] != '\n') i++
                    }
                } else break
            }
        }

        fun str(): AStr {
            val q = s[i]; val st = i; i++
            val sb = StringBuilder()
            while (i < s.length && s[i] != q) {
                val c = s[i]
                if (c == '\\' && i + 1 < s.length) {
                    val n = s[i + 1]
                    when (n) {
                        'n' -> { sb.append('\n'); i += 2 }
                        't' -> { sb.append('\t'); i += 2 }
                        'r' -> { sb.append('\r'); i += 2 }
                        '\\', '"', '\'' -> { sb.append(n); i += 2 }
                        else -> { sb.append(c).append(n); i += 2 }
                    }
                } else { sb.append(c); i++ }
            }
            if (i >= s.length) fail()
            i++
            return AStr(st, i, sb.toString(), q)
        }

        fun isIdStart(c: Char) = c.isLetter() || c == '_'

        fun table(depth: Int): ATable {
            if (depth > 60) fail()
            i++ // {
            val es = ArrayList<AEntry>()
            while (true) {
                ws()
                if (i >= s.length) fail()
                if (s[i] == '}') { i++; break }
                // key?
                var key: String? = null
                val save = i
                if (isIdStart(s[i])) {
                    var j = i
                    while (j < s.length && (s[j].isLetterOrDigit() || s[j] == '_')) j++
                    var k = j
                    while (k < s.length && (s[k] == ' ' || s[k] == '\t')) k++
                    if (k < s.length && s[k] == '=' && (k + 1 >= s.length || s[k + 1] != '=')) { key = s.substring(i, j); i = k + 1 }
                } else if (s[i] == '[' && i + 1 < s.length && (s[i + 1] == '"' || s[i + 1] == '\'')) {
                    i++
                    val ks = str()
                    ws()
                    if (i < s.length && s[i] == ']') { i++; ws(); if (i < s.length && s[i] == '=') { i++; key = ks.value } else i = save } else i = save
                }
                ws()
                val v = value(depth)
                es.add(AEntry(key, v))
                ws()
                if (i < s.length && (s[i] == ',' || s[i] == ';')) i++
            }
            return ATable(es)
        }

        fun value(depth: Int): AN {
            ws()
            if (i >= s.length) fail()
            val c = s[i]
            if (c == '{') return table(depth + 1)
            if (c == '"' || c == '\'') return str()
            // atom: number, true/false/nil, identifier
            val st = i
            while (i < s.length && s[i] != ',' && s[i] != '}' && s[i] != ';' && s[i] != '\n' && s[i] != '\r') i++
            if (i == st) fail()
            return AAtom
        }
    }

    /** Parse the whole file; result = the `ast` table (or null). */
    private fun parseAst(text: String): ATable {
        val p = Parser(text)
        val m = HEAD.find(text) ?: throw java.io.IOException("Bukan skrip .ast")
        p.i = text.indexOf('{', m.range.first)
        return p.table(0)
    }

    private val SKIP_KEYS = Regex("^(vo|vl\\d*|linemode|pagebreak)$")

    private fun collect(root: ATable): List<Unit> {
        val out = ArrayList<Unit>()
        fun walk(t: ATable, block: String) {
            var n = 0
            for (e in t.entries) {
                val v = e.value
                if (v !is ATable) continue
                val b = if (e.key != null && e.key.startsWith("block_")) e.key else block
                if ((e.key == "text" || e.key == "select")) {
                    for (le in v.entries) {
                        val lk = le.key ?: continue
                        val lt = le.value as? ATable ?: continue
                        if (SKIP_KEYS.matches(lk)) continue
                        for (item in lt.entries) {
                            val iv = item.value
                            if (iv is AStr && item.key == null) out.add(Unit(iv, 'T', b, n++))
                            else if (iv is ATable && item.key == null) {
                                for (pe in iv.entries) {
                                    val pv = pe.value
                                    when {
                                        pe.key == "name" && pv is ATable -> {
                                            val strs = pv.entries.filter { it.key == null && it.value is AStr }.map { it.value as AStr }
                                            if (strs.isNotEmpty()) out.add(Unit(if (strs.size >= 2) strs[1] else strs[0], 'N', b, n++))
                                        }
                                        pe.key == null && pv is AStr -> out.add(Unit(pv, 'T', b, n++))
                                        pe.key == null && pv is ATable -> {
                                            val first = pv.entries.firstOrNull()?.value as? AStr
                                            if (first != null && first.value == "ruby") {
                                                val rt = pv.entries.firstOrNull { it.key == "text" }?.value as? AStr
                                                if (rt != null && rt.value.isNotBlank()) out.add(Unit(rt, 'R', b, n++))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else walk(v, b)
            }
        }
        walk(root, "")
        return out
    }

    private fun h(s: String) = String.format("%08x", s.hashCode())

    private fun keyOf(u: Unit) = "${u.block}#${u.n}${u.kind}:${h(u.str.value)}"

    fun extract(fileKey: String, text: String): List<TextUnit> {
        val units = collect(parseAst(text))
        val seen = HashSet<String>()
        val out = ArrayList<TextUnit>()
        for (u in units) {
            val t = u.str.value
            if (t.isBlank()) continue
            if (!seen.add("${u.kind}:$t")) continue
            out.add(TextUnit(fileKey, keyOf(u), t))
        }
        return out
    }

    private fun quote(s: String, q: Char): String {
        val sb = StringBuilder().append(q)
        for (c in s) when (c) {
            '\\' -> sb.append("\\\\")
            '\n' -> sb.append("\\n")
            '\r' -> {}
            q -> sb.append('\\').append(q)
            else -> sb.append(c)
        }
        return sb.append(q).toString()
    }

    /** Apply translations: exact key match, or the same (kind, hash) for all occurrences. */
    fun apply(text: String, tr: Map<String, String>): Pair<String, Int> {
        val byKind = HashMap<String, String>()
        for ((k, v) in tr) {
            val i = k.lastIndexOf('#')
            if (i < 0) continue
            val rest = k.substring(i + 1) // "<n><kind>:<hash>"
            val c = rest.indexOf(':')
            if (c <= 0) continue
            val kind = rest[c - 1]
            byKind.putIfAbsent("$kind:" + rest.substring(c + 1), v)
        }
        val units = collect(parseAst(text))
        val sb = StringBuilder()
        var last = 0
        var changed = 0
        for (u in units.sortedBy { it.str.start }) {
            val nt = tr[keyOf(u)] ?: byKind["${u.kind}:${h(u.str.value)}"] ?: continue
            if (nt == u.str.value) continue
            sb.append(text, last, u.str.start).append(quote(nt, u.str.quote))
            last = u.str.end
            changed++
        }
        if (changed == 0) return Pair(text, 0)
        sb.append(text, last, text.length)
        return Pair(sb.toString(), changed)
    }
}
