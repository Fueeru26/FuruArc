package com.furuarc.core

/**
 * INI/CFG/properties for the Text File Editor. Line-based model: comments, blank lines, and the original format
 * are preserved; only changed lines are rewritten.
 */
object Ini {
    const val GLOBAL = ""

    class Entry(val line: Int, val isSection: Boolean, val section: String, val key: String, val value: String)

    private val SECTION = Regex("^(\\s*)\\[([^\\]]*)\\](.*)$")
    private val KEYVAL = Regex("^(\\s*)([^=;#\\[\\s][^=]*?)(\\s*)=(\\s*)(.*)$")

    fun parse(lines: List<String>): List<Entry> {
        val out = ArrayList<Entry>()
        var sec = GLOBAL
        for (i in lines.indices) {
            val l = lines[i]
            val s = SECTION.matchEntire(l)
            if (s != null && l.trimStart().startsWith("[")) {
                sec = s.groupValues[2]
                out.add(Entry(i, true, sec, "", ""))
                continue
            }
            val k = KEYVAL.matchEntire(l)
            if (k != null) out.add(Entry(i, false, sec, k.groupValues[2], k.groupValues[5]))
        }
        return out
    }

    fun split(text: String): ArrayList<String> {
        val l = ArrayList(text.split("\n"))
        if (l.size > 1 && l[l.size - 1].isEmpty()) l.removeAt(l.size - 1)
        return l
    }

    fun join(lines: List<String>): String = lines.joinToString("\n") + "\n"

    fun setValue(lines: MutableList<String>, idx: Int, value: String) {
        val k = KEYVAL.matchEntire(lines[idx]) ?: return
        lines[idx] = k.groupValues[1] + k.groupValues[2] + k.groupValues[3] + "=" + k.groupValues[4] + value
    }

    fun setKey(lines: MutableList<String>, idx: Int, key: String) {
        val k = KEYVAL.matchEntire(lines[idx]) ?: return
        lines[idx] = k.groupValues[1] + key + k.groupValues[3] + "=" + k.groupValues[4] + k.groupValues[5]
    }

    fun renameSection(lines: MutableList<String>, idx: Int, name: String) {
        val s = SECTION.matchEntire(lines[idx]) ?: return
        lines[idx] = s.groupValues[1] + "[" + name + "]" + s.groupValues[3]
    }

    /** Separator style used by the file: " = " if the first line has spaces around "=", otherwise "=". */
    private fun sep(lines: List<String>): String {
        for (l in lines) {
            val k = KEYVAL.matchEntire(l) ?: continue
            return if (k.groupValues[3].isNotEmpty() || k.groupValues[4].isNotEmpty()) " = " else "="
        }
        return "="
    }

    /** Insert key=value at the end of section [section]; the result is the new line's index. */
    fun addKey(lines: MutableList<String>, section: String, key: String, value: String): Int {
        val es = parse(lines)
        var at = -1
        if (section == GLOBAL) {
            val firstSection = es.firstOrNull { it.isSection }?.line ?: lines.size
            var last = -1
            for (e in es) if (!e.isSection && e.section == GLOBAL) last = e.line
            at = if (last >= 0) last + 1 else 0
            if (at > firstSection) at = firstSection
        } else {
            val head = es.firstOrNull { it.isSection && it.section == section } ?: return -1
            var last = head.line
            for (e in es) if (!e.isSection && e.section == section && e.line > head.line) last = e.line
            at = last + 1
        }
        lines.add(at, key + sep(lines) + value)
        return at
    }

    fun addSection(lines: MutableList<String>, name: String) {
        if (lines.isNotEmpty() && lines[lines.size - 1].isNotBlank()) lines.add("")
        lines.add("[$name]")
    }

    /** Delete a section with its contents (up to just before the next section). */
    fun removeSection(lines: MutableList<String>, idx: Int) {
        var end = idx + 1
        while (end < lines.size && !(SECTION.matchEntire(lines[end]) != null && lines[end].trimStart().startsWith("["))) end++
        for (i in end - 1 downTo idx) lines.removeAt(i)
    }
}
