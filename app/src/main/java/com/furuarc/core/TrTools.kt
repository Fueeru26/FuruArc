package com.furuarc.core

/**
 * Bulk tools for translation text in the Translation Editor. RPG Maker control codes (\C[6], \N[1], \!, etc.)
 * are not counted as displayed characters and are never cut.
 */
object TrTools {
    private val CODE = Regex("\\\\(?:[A-Za-z]+(?:\\[\\d+\\])?|[{}$.|!><^\\\\])")

    /** Change the first letter of the text (after control codes and leading punctuation) to upper/lower case. A leading digit = unchanged. */
    fun firstLetter(text: String, upper: Boolean): String {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '\\') {
                val m = CODE.find(text, i)
                i = if (m != null && m.range.first == i) m.range.last + 1 else i + 1
                continue
            }
            if (c.isLetter()) {
                val n = if (upper) c.uppercaseChar() else c.lowercaseChar()
                return if (n == c) text else text.substring(0, i) + n + text.substring(i + 1)
            }
            if (c.isDigit()) return text
            i++
        }
        return text
    }

    private class Atom(val s: String, val w: Int)

    private fun atoms(text: String): List<Atom> {
        val out = ArrayList<Atom>()
        var i = 0
        while (i < text.length) {
            if (text[i] == '\\') {
                val m = CODE.find(text, i)
                if (m != null && m.range.first == i) {
                    out.add(Atom(m.value, 0))
                    i = m.range.last + 1
                    continue
                }
            }
            out.add(Atom(text[i].toString(), 1))
            i++
        }
        return out
    }

    /**
     * Re-wrap [text] into lines of at most [max] displayed characters (old line breaks are joined first).
     * [keepWords] = true: break lines only at spaces (words longer than [max] stay intact);
     * false: cut exactly at [max] characters.
     */
    fun wrap(text: String, max: Int, keepWords: Boolean): String {
        if (max < 1) return text
        val flat = text.replace(Regex("\\s*\\n\\s*"), " ").trim()
        if (flat.isEmpty()) return text
        val lines = ArrayList<String>()
        val cur = StringBuilder()
        var w = 0
        fun newLine() {
            lines.add(cur.toString().trimEnd())
            cur.setLength(0)
            w = 0
        }
        if (keepWords) {
            val words = ArrayList<List<Atom>>()
            var word = ArrayList<Atom>()
            for (a in atoms(flat)) {
                if (a.s == " ") {
                    if (word.isNotEmpty()) words.add(word)
                    word = ArrayList()
                } else word.add(a)
            }
            if (word.isNotEmpty()) words.add(word)
            for (wd in words) {
                val ww = wd.sumOf { it.w }
                if (w > 0 && w + 1 + ww > max) newLine()
                if (w > 0) {
                    cur.append(' ')
                    w++
                }
                for (a in wd) cur.append(a.s)
                w += ww
            }
        } else {
            for (a in atoms(flat)) {
                if (a.w > 0 && w + a.w > max) newLine()
                if (a.s == " " && w == 0) continue
                cur.append(a.s)
                w += a.w
            }
        }
        if (cur.isNotEmpty()) newLine()
        return lines.joinToString("\n")
    }

    private fun pattern(find: String, ignoreCase: Boolean, whole: Boolean): Regex {
        val q = Regex.escape(find)
        val body = if (whole) "(?<![\\p{L}\\p{N}_])" + q + "(?![\\p{L}\\p{N}_])" else q
        return if (ignoreCase) Regex(body, RegexOption.IGNORE_CASE) else Regex(body)
    }

    /** Replace all [find] with [rep]. [whole] = whole words only. */
    fun replace(text: String, find: String, rep: String, ignoreCase: Boolean = false, whole: Boolean = false): String =
        if (find.isEmpty()) text else pattern(find, ignoreCase, whole).replace(text) { rep }

    /** Number of occurrences of [find] in [text]. */
    fun count(text: String, find: String, ignoreCase: Boolean, whole: Boolean): Int =
        if (find.isEmpty()) 0 else pattern(find, ignoreCase, whole).findAll(text).count()

    /** Entries with exactly the same source text: empty ones are filled with the translation of a similar filled entry. Result: index -> new text. */
    fun fillIdentical(origs: List<String>, trs: List<String>): Map<Int, String> {
        val first = HashMap<String, String>()
        for (i in origs.indices) if (origs[i].isNotBlank() && trs[i].isNotBlank()) first.putIfAbsent(origs[i], trs[i])
        val out = LinkedHashMap<Int, String>()
        for (i in origs.indices) {
            if (trs[i].isBlank() && origs[i].isNotBlank()) first[origs[i]]?.let { out[i] = it }
        }
        return out
    }

    // ---------- glossary (source term -> translation) ----------

    private fun clean(s: String) = s.replace('\t', ' ').replace('\n', ' ').trim()

    fun parseGlossary(text: String): List<Pair<String, String>> = text.split("\n").mapNotNull { l ->
        val i = l.indexOf('\t')
        if (i <= 0) null else Pair(l.substring(0, i), l.substring(i + 1)).takeIf { it.second.isNotEmpty() }
    }

    fun writeGlossary(g: List<Pair<String, String>>): String = g.joinToString("\n") { clean(it.first) + "\t" + clean(it.second) }

    /** Glossary terms that appear (whole words, case-insensitive) in the source text [orig]. */
    fun suggestions(orig: String, gloss: List<Pair<String, String>>): List<Pair<String, String>> =
        gloss.filter { count(orig, it.first, true, true) > 0 }

    /** Merge [add] into [base]; the same source term (case-insensitive) gets its translation replaced. */
    fun mergeGlossary(base: List<Pair<String, String>>, add: List<Pair<String, String>>): List<Pair<String, String>> {
        val out = ArrayList(base)
        for (g in add) {
            val k = out.indexOfFirst { it.first.equals(g.first, true) }
            if (k >= 0) out[k] = g else out.add(g)
        }
        return out
    }

    private fun csvCell(x: String): String =
        if (x.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + x.replace("\"", "\"\"") + "\"" else x

    /** Glossary as a simple CSV: header row then two columns (source, translation). */
    fun glossaryToCsv(g: List<Pair<String, String>>): String =
        "original,translation\n" + g.joinToString("") { csvCell(it.first) + "," + csvCell(it.second) + "\n" }

    private val HEAD_SRC = setOf("original", "asli", "source", "term", "istilah")
    private val HEAD_DST = setOf("translation", "terjemahan", "target")

    /** Read a two-column CSV (separator comma, semicolon, or tab; double quotes supported). The header row is skipped. */
    fun glossaryFromCsv(text: String): List<Pair<String, String>> {
        val t = text.removePrefix("\uFEFF")
        val first = t.lineSequence().firstOrNull { it.isNotBlank() } ?: return emptyList()
        val delim = listOf(',', '\t', ';').firstOrNull { first.indexOf(it) >= 0 } ?: ','
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val sb = StringBuilder()
        var quoted = false
        var i = 0
        while (i < t.length) {
            val c = t[i]
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < t.length && t[i + 1] == '"') {
                        sb.append('"')
                        i++
                    } else {
                        quoted = false
                    }
                } else {
                    sb.append(c)
                }
            } else if (c == '"' && sb.isEmpty()) {
                quoted = true
            } else if (c == delim) {
                row.add(sb.toString())
                sb.setLength(0)
            } else if (c == '\n') {
                row.add(sb.toString())
                sb.setLength(0)
                rows.add(row)
                row = ArrayList()
            } else if (c != '\r') {
                sb.append(c)
            }
            i++
        }
        if (sb.isNotEmpty() || row.isNotEmpty()) {
            row.add(sb.toString())
            rows.add(row)
        }
        val out = ArrayList<Pair<String, String>>()
        for ((k, r) in rows.withIndex()) {
            if (r.size < 2) continue
            val a = r[0].trim()
            val b = r[1].trim()
            if (k == 0 && a.lowercase() in HEAD_SRC && b.lowercase() in HEAD_DST) continue
            if (a.isEmpty() || b.isEmpty()) continue
            if (out.any { it.first.equals(a, true) }) continue
            out.add(Pair(a, b))
        }
        return out
    }

    /**
     * Apply the glossary: an empty entry whose source text exactly equals a term is filled with that term's translation;
     * in an already filled translation, remaining source terms (whole words, case-insensitive) are replaced.
     * Pairs whose translation contains the source term are skipped in the replace step to avoid repetition. Result: index -> new text.
     */
    fun glossaryChanges(origs: List<String>, trs: List<String>, gloss: List<Pair<String, String>>): Map<Int, String> {
        val out = LinkedHashMap<Int, String>()
        for (i in origs.indices) {
            var t = trs[i]
            if (t.isBlank()) {
                val o = origs[i].trim()
                val g = gloss.firstOrNull { it.first.equals(o, true) } ?: continue
                t = g.second
            } else {
                for ((src, dst) in gloss) {
                    if (dst.contains(src, true)) continue
                    t = replace(t, src, dst, true, true)
                }
            }
            if (t != trs[i]) out[i] = t
        }
        return out
    }

    // ---------- auto-fill control codes ----------

    /** Control codes lined up at the start and end of [text] (original tokens). If the whole text is only codes, all go into "start". */
    fun edgeCodes(text: String): Pair<List<String>, List<String>> {
        val ms = CODE.findAll(text).toList()
        val lead = ArrayList<String>()
        var pos = 0
        for (m in ms) {
            if (m.range.first == pos) {
                lead.add(m.value)
                pos = m.range.last + 1
            } else break
        }
        val trail = ArrayList<String>()
        var end = text.length
        for (m in ms.asReversed()) {
            if (m.range.last + 1 == end && m.range.first >= pos) {
                trail.add(0, m.value)
                end = m.range.first
            } else break
        }
        return Pair(lead, trail)
    }

    /**
     * Complete [tr] with control codes at the start/end of [orig] that are missing in the translation (codes mid-sentence are not placed).
     * An empty translation is filled only if [includeEmpty] (its content is just start + end control codes).
     */
    fun fillCodes(orig: String, tr: String, includeEmpty: Boolean): String {
        val (lead, trail) = edgeCodes(orig)
        if (lead.isEmpty() && trail.isEmpty()) return tr
        if (tr.isBlank()) return if (includeEmpty) lead.joinToString("") + trail.joinToString("") else tr
        val have = HashMap<String, Int>()
        for (m in CODE.findAll(tr)) {
            val k = m.value.uppercase()
            have[k] = (have[k] ?: 0) + 1
        }
        fun missing(c: String): Boolean {
            val k = c.uppercase()
            val n = have[k] ?: 0
            if (n > 0) {
                have[k] = n - 1
                return false
            }
            return true
        }
        val pre = lead.filter { missing(it) }
        val post = trail.filter { missing(it) }
        if (pre.isEmpty() && post.isEmpty()) return tr
        return pre.joinToString("") + tr + post.joinToString("")
    }
}
