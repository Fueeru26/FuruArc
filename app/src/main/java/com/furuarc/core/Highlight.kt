package com.furuarc.core

import java.util.BitSet

/** Lightweight syntax highlighting for the Text File Editor (regex-based, no full parser). */
object Highlight {
    const val COMMENT = 0
    const val STRING = 1
    const val NUMBER = 2
    const val KEYWORD = 3
    const val TAG = 4
    const val KEY = 5
    const val ROLES = 6

    /** Length limit of the text to colorize (above it, highlighting is off so things stay smooth). */
    const val MAX_CHARS = 300_000

    class Span(val start: Int, val end: Int, val role: Int)

    /**
     * Coloring rules. [mode] != 0 uses a hand-written scanner (no regex) so very long strings/comments
     * do not cause StackOverflowError or hangs: 1 = string "..." without escapes, 2 = JSON string (key/value),
     * 3 = string "..." and '...' with escapes, 4 = block comment /* ... */, 5 = Markdown code block ```.
     */
    private class Rule(val re: Regex?, val role: Int, val group: Int = 0, val mode: Int = 0)

    private fun r(p: String, role: Int, group: Int = 0, multiline: Boolean = false): Rule {
        val pat = p.replace('~', '"')
        return Rule(if (multiline) Regex(pat, RegexOption.MULTILINE) else Regex(pat), role, group)
    }

    private fun scan(role: Int, mode: Int): Rule = Rule(null, role, 0, mode)

    private val NUM = """(?<![\w.])-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?(?![\w.])"""

    private val JSON = listOf(
        scan(STRING, 2),
        r(NUM, NUMBER),
        r("""\b(?:true|false|null)\b""", KEYWORD),
    )

    private val INI = listOf(
        r("""^[ \t]*[;#].*$""", COMMENT, multiline = true),
        r("""^[ \t]*\[[^\]\n]*\]""", TAG, multiline = true),
        r("""^[ \t]*[^=\n;#\[\s][^=\n]*?(?=\s*=)""", KEY, multiline = true),
        scan(STRING, 1),
        r(NUM, NUMBER),
    )

    private val KS = listOf(
        scan(COMMENT, 4),
        r("""^[ \t]*;.*$""", COMMENT, multiline = true),
        r("""\[[^\]\n]*\]""", TAG),
        r("""^[ \t]*@\w+""", TAG, multiline = true),
        r("""^[ \t]*\*[^\n|]*""", KEYWORD, multiline = true),
        scan(STRING, 1),
        r(NUM, NUMBER),
    )

    private val RPY = listOf(
        scan(STRING, 3),
        r("""#.*$""", COMMENT, multiline = true),
        r("""\b(?:label|menu|jump|call|return|scene|show|hide|with|define|default|init|python|if|elif|else|while|pass|play|stop|queue|transform|screen|image|at|as|behind|zorder|window|pause|nvl|extend|style|translate|from|import)\b""", KEYWORD),
        r(NUM, NUMBER),
    )

    private val MD = listOf(
        scan(STRING, 5),
        r("""^#{1,6}[ \t].*$""", TAG, multiline = true),
        r("""`[^`\n]+`""", STRING),
        r("""\*\*[^*\n]+\*\*|__[^_\n]+__""", KEYWORD),
        r("""\[[^\]\n]*\]\([^)\n]*\)""", KEY),
        r("""^>.*$""", COMMENT, multiline = true),
        r("""^[ \t]*(?:[-*+]|\d+\.)[ \t]""", NUMBER, multiline = true),
    )

    private val XML_TAG = Regex("""<[^<>]*>""")
    private val XML_NAME = Regex("""^</?[^\s>/!?]+""")
    private val XML_ATTR = Regex("""([\w:.-]+)\s*=\s*(~[^~]*~|'[^']*')""".replace('~', '"'))

    /** Highlight type from the file extension (null = no coloring). */
    fun kindFor(fileName: String): String? = when (fileName.substringAfterLast('.', "").lowercase()) {
        "json" -> "json"
        "xml", "html", "htm", "svg", "xaml", "plist", "resx", "rss" -> "xml"
        "ini", "cfg", "conf", "properties", "desktop", "toml" -> "ini"
        "ks", "kag" -> "ks"
        "rpy", "rpym" -> "rpy"
        "md", "markdown" -> "md"
        else -> null
    }

    private fun free(bits: BitSet, s: Int, e: Int): Boolean {
        val n = bits.nextSetBit(s)
        return n < 0 || n >= e
    }

    private fun take(bits: BitSet, out: ArrayList<Span>, s: Int, e: Int, role: Int) {
        if (e <= s || !free(bits, s, e)) return
        bits.set(s, e)
        out.add(Span(s, e, role))
    }

    /** Limit on the number of color blocks; above it the rest is left plain so the editor stays smooth. */
    private const val MAX_SPANS = 8000

    private fun isWs(c: Char) = c == ' ' || c == '\t' || c == '\n' || c == '\r'

    /** String "..." (and '...' if [sq]); [escapes] = backslash skips the next character; [keys] = a string before ':' becomes KEY. */
    private fun scanStrings(text: String, sq: Boolean, escapes: Boolean, keys: Boolean, role: Int, bits: BitSet, out: ArrayList<Span>) {
        val n = text.length
        var i = 0
        while (i < n) {
            val c = text[i]
            if (c != '"' && !(sq && c == '\'')) { i++; continue }
            var j = i + 1
            var closed = false
            while (j < n) {
                val d = text[j]
                if (d == '\n') break
                if (escapes && d == '\\') {
                    if (j + 1 < n && text[j + 1] != '\n') { j += 2; continue }
                    break
                }
                if (d == c) { closed = true; break }
                j++
            }
            if (!closed) { i++; continue }
            var rl = role
            if (keys) {
                var k = j + 1
                while (k < n && isWs(text[k])) k++
                if (k < n && text[k] == ':') rl = KEY
            }
            take(bits, out, i, j + 1, rl)
            i = j + 1
            if (out.size > MAX_SPANS) return
        }
    }

    /** Block comment /* ... */ (matched pairs only). */
    private fun scanBlockComments(text: String, role: Int, bits: BitSet, out: ArrayList<Span>) {
        var from = 0
        while (true) {
            val a = text.indexOf("/*", from)
            if (a < 0) return
            val b = text.indexOf("*/", a + 2)
            if (b < 0) return
            take(bits, out, a, b + 2, role)
            from = b + 2
            if (out.size > MAX_SPANS) return
        }
    }

    /** Markdown code block: opening ``` line through ``` at the start of the closing line (matched pairs only). */
    private fun scanFences(text: String, role: Int, bits: BitSet, out: ArrayList<Span>) {
        val n = text.length
        var pos = 0
        var open = -1
        while (pos < n) {
            var e = text.indexOf('\n', pos)
            if (e < 0) e = n
            if (text.startsWith("```", pos)) {
                if (open < 0) {
                    open = pos
                } else {
                    take(bits, out, open, pos + 3, role)
                    open = -1
                    if (out.size > MAX_SPANS) return
                }
            }
            pos = e + 1
        }
    }

    private fun runScan(rule: Rule, text: String, bits: BitSet, out: ArrayList<Span>) {
        when (rule.mode) {
            1 -> scanStrings(text, false, false, false, rule.role, bits, out)
            2 -> scanStrings(text, false, true, true, rule.role, bits, out)
            3 -> scanStrings(text, true, true, false, rule.role, bits, out)
            4 -> scanBlockComments(text, rule.role, bits, out)
            5 -> scanFences(text, rule.role, bits, out)
        }
    }

    /** XML/HTML highlighting. Comments and CDATA are found with indexOf (no regex) so long blocks are safe. */
    private fun xmlSpans(text: String, bits: BitSet, out: ArrayList<Span>) {
        var from = 0
        while (true) {
            val a = text.indexOf("<!--", from)
            if (a < 0) break
            val b = text.indexOf("-->", a + 4)
            if (b < 0) break
            take(bits, out, a, b + 3, COMMENT)
            from = b + 3
        }
        from = 0
        while (true) {
            val a = text.indexOf("<![CDATA[", from)
            if (a < 0) break
            val b = text.indexOf("]]>", a + 9)
            if (b < 0) break
            take(bits, out, a, b + 3, STRING)
            from = b + 3
        }
        for (m in XML_TAG.findAll(text)) {
            val s = m.range.first
            val e = m.range.last + 1
            if (!free(bits, s, e)) continue
            val body = m.value
            val nm = XML_NAME.find(body)
            if (nm != null) out.add(Span(s, s + nm.range.last + 1, TAG))
            out.add(Span(e - 1, e, TAG))
            // very long tags (e.g. attributes holding base64 data) do not get their attributes searched: attribute search can be slow
            if (body.length <= 2000) {
                for (a in XML_ATTR.findAll(body)) {
                    val k = a.groups[1] ?: continue
                    val v = a.groups[2] ?: continue
                    out.add(Span(s + k.range.first, s + k.range.last + 1, KEY))
                    out.add(Span(s + v.range.first, s + v.range.last + 1, STRING))
                }
            }
            bits.set(s, e)
            if (out.size > MAX_SPANS) return
        }
    }

    /**
     * Compute the color blocks. Never throws: if regex runs out of stack or memory, a partial
     * (or empty) result is returned and the text is shown without colors.
     */
    fun spans(text: String, kind: String): List<Span> {
        val out = ArrayList<Span>()
        if (text.isEmpty() || text.length > MAX_CHARS) return out
        try {
            val bits = BitSet(text.length)
            if (kind == "xml") {
                xmlSpans(text, bits, out)
                return out
            }
            val rules = when (kind) {
                "json" -> JSON
                "ini" -> INI
                "ks" -> KS
                "rpy" -> RPY
                "md" -> MD
                else -> return out
            }
            for (rule in rules) {
                val re = rule.re
                if (re == null) {
                    runScan(rule, text, bits, out)
                } else {
                    for (m in re.findAll(text)) {
                        val g = if (rule.group == 0) m.range else (m.groups[rule.group]?.range ?: continue)
                        take(bits, out, g.first, g.last + 1, rule.role)
                        if (out.size > MAX_SPANS) break
                    }
                }
                if (out.size > MAX_SPANS) break
            }
        } catch (t: Throwable) {
            // StackOverflowError / OutOfMemoryError / regex error: keep only what we have
        }
        return out
    }
}
