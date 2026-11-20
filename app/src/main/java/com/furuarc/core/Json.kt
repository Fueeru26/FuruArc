package com.furuarc.core

/** JSON numbers are stored as-is (original text) so rewriting does not change the number format. */
class JNum(val raw: String) {
    fun int(): Int? = raw.toIntOrNull()
    override fun toString() = raw
}

/**
 * Minimalist JSON: object = LinkedHashMap (key order preserved), array = ArrayList, number = [JNum].
 * Used for RPGM data (.json files in the data folder) and save files, which must be written back without losing content.
 */
object Json {
    fun parse(text: String): Any? {
        val p = Parser(if (text.startsWith("﻿")) text.substring(1) else text)
        p.ws()
        val v = p.value(0)
        p.ws()
        if (p.i < p.s.length) p.fail("karakter tak terduga")
        return v
    }

    private class Parser(val s: String) {
        var i = 0

        fun fail(msg: String): Nothing {
            var line = 1
            var col = 1
            for (k in 0 until minOf(i, s.length)) {
                if (s[k] == '\n') { line++; col = 1 } else col++
            }
            throw IllegalArgumentException("JSON tidak valid ($msg) di baris $line, kolom $col")
        }

        fun ws() {
            while (i < s.length && (s[i] == ' ' || s[i] == '\n' || s[i] == '\r' || s[i] == '\t')) i++
        }

        fun value(depth: Int): Any? {
            if (depth > 400) fail("terlalu dalam")
            if (i >= s.length) fail("data berakhir terlalu cepat")
            return when (val c = s[i]) {
                '{' -> obj(depth)
                '[' -> arr(depth)
                '"' -> str()
                't' -> lit("true", true)
                'f' -> lit("false", false)
                'n' -> lit("null", null)
                else -> if (c == '-' || c in '0'..'9') num() else fail("karakter '$c'")
            }
        }

        fun lit(word: String, v: Any?): Any? {
            if (!s.startsWith(word, i)) fail("literal salah")
            i += word.length
            return v
        }

        fun num(): JNum {
            val st = i
            while (i < s.length && (s[i] in '0'..'9' || s[i] == '-' || s[i] == '+' || s[i] == '.' || s[i] == 'e' || s[i] == 'E')) i++
            if (i == st) fail("angka kosong")
            return JNum(s.substring(st, i))
        }

        fun str(): String {
            i++ // "
            var sb: StringBuilder? = null
            var st = i
            while (true) {
                if (i >= s.length) fail("string tidak ditutup")
                val c = s[i]
                if (c == '"') {
                    val r = if (sb == null) s.substring(st, i) else sb.append(s, st, i).toString()
                    i++
                    return r
                }
                if (c == '\\') {
                    if (sb == null) sb = StringBuilder()
                    sb.append(s, st, i)
                    i++
                    if (i >= s.length) fail("escape terpotong")
                    when (val e = s[i]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (i + 4 >= s.length) fail("escape \\u terpotong")
                            val h = s.substring(i + 1, i + 5).toIntOrNull(16) ?: fail("escape \\u salah")
                            sb.append(h.toChar())
                            i += 4
                        }
                        else -> fail("escape \\$e tidak dikenal")
                    }
                    i++
                    st = i
                } else {
                    i++
                }
            }
        }

        fun arr(depth: Int): ArrayList<Any?> {
            i++
            val r = ArrayList<Any?>()
            ws()
            if (i < s.length && s[i] == ']') { i++; return r }
            while (true) {
                ws()
                r.add(value(depth + 1))
                ws()
                if (i >= s.length) fail("array tidak ditutup")
                if (s[i] == ',') { i++; continue }
                if (s[i] == ']') { i++; return r }
                fail("diharapkan , atau ]")
            }
        }

        fun obj(depth: Int): LinkedHashMap<String, Any?> {
            i++
            val r = LinkedHashMap<String, Any?>()
            ws()
            if (i < s.length && s[i] == '}') { i++; return r }
            while (true) {
                ws()
                if (i >= s.length || s[i] != '"') fail("diharapkan nama kunci")
                val k = str()
                ws()
                if (i >= s.length || s[i] != ':') fail("diharapkan :")
                i++
                ws()
                r[k] = value(depth + 1)
                ws()
                if (i >= s.length) fail("objek tidak ditutup")
                if (s[i] == ',') { i++; continue }
                if (s[i] == '}') { i++; return r }
                fail("diharapkan , atau }")
            }
        }
    }

    /** [pretty] = tidy with 2-space indentation; arrays of simple values stay on one line. */
    fun write(v: Any?, pretty: Boolean = false): String {
        val sb = StringBuilder()
        put(sb, v, pretty, 0)
        return sb.toString()
    }

    private fun scalar(v: Any?) = v !is Map<*, *> && v !is List<*>

    private fun nl(sb: StringBuilder, depth: Int) {
        sb.append('\n')
        for (k in 0 until depth) sb.append("  ")
    }

    private fun put(sb: StringBuilder, v: Any?, pretty: Boolean, depth: Int) {
        when (v) {
            null -> sb.append("null")
            is String -> quote(sb, v)
            is JNum -> sb.append(v.raw)
            is Boolean -> sb.append(if (v) "true" else "false")
            is Number -> sb.append(v.toString())
            is Map<*, *> -> {
                if (v.isEmpty()) { sb.append("{}"); return }
                sb.append('{')
                var first = true
                for ((k, x) in v) {
                    if (!first) sb.append(',')
                    first = false
                    if (pretty) nl(sb, depth + 1)
                    quote(sb, k as String)
                    sb.append(if (pretty) ": " else ":")
                    put(sb, x, pretty, depth + 1)
                }
                if (pretty) nl(sb, depth)
                sb.append('}')
            }
            is List<*> -> {
                if (v.isEmpty()) { sb.append("[]"); return }
                val flat = !pretty || v.all { scalar(it) }
                sb.append('[')
                var first = true
                for (x in v) {
                    if (!first) sb.append(',')
                    first = false
                    if (!flat) nl(sb, depth + 1)
                    put(sb, x, pretty, depth + 1)
                }
                if (!flat) nl(sb, depth)
                sb.append(']')
            }
            else -> quote(sb, v.toString())
        }
    }

    private fun quote(sb: StringBuilder, s: String) {
        sb.append('"')
        for (c in s) {
            when {
                c == '"' -> sb.append("\\\"")
                c == '\\' -> sb.append("\\\\")
                c == '\n' -> sb.append("\\n")
                c == '\r' -> sb.append("\\r")
                c == '\t' -> sb.append("\\t")
                c == '\b' -> sb.append("\\b")
                c == '\u000C' -> sb.append("\\f")
                c < ' ' || c == ' ' || c == ' ' -> sb.append("\\u").append(String.format("%04x", c.code))
                else -> sb.append(c)
            }
        }
        sb.append('"')
    }
}
