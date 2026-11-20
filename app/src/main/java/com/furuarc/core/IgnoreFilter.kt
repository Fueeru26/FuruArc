package com.furuarc.core

/**
 * Name filter for files/folders in Pack. Patterns are separated by comma/semicolon/newline and may use * and ?.
 * Patterns are matched against EVERY path segment (folder names and file names), case-insensitively.
 * Example: "Thumbs.db, *.rpyc, .git" skips all Thumbs.db, all *.rpyc, and the entire contents of .git folders.
 */
class IgnoreFilter(text: String) {
    private val patterns: List<Regex> = text
        .split(',', ';', '\n')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { globToRegex(it) }

    fun matches(path: String): Boolean {
        if (patterns.isEmpty()) return false
        val segs = path.replace('\\', '/').split('/')
        for (seg in segs) {
            if (seg.isEmpty()) continue
            for (p in patterns) if (p.matches(seg)) return true
        }
        return false
    }

    private fun globToRegex(g: String): Regex {
        val sb = StringBuilder()
        for (ch in g) {
            if (ch == '*') sb.append(".*")
            else if (ch == '?') sb.append('.')
            else sb.append(Regex.escape(ch.toString()))
        }
        return Regex(sb.toString(), RegexOption.IGNORE_CASE)
    }
}
