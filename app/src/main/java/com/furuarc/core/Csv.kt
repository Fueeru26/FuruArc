package com.furuarc.core

/** CSV/TSV for the Text File Editor: separator detection, parsing (double quotes, quotes inside cells, newlines inside cells), and writing back. */
object Csv {
    class Parsed(val rows: ArrayList<ArrayList<String>>, val quoteAll: Boolean)

    private val CANDIDATES = charArrayOf(',', ';', '\t', '|')

    /** The separator that appears most often in the first few lines (outside quotes). */
    fun detectDelimiter(text: String, ext: String?): Char {
        if (ext != null && ext.equals("tsv", true)) return '\t'
        val counts = IntArray(CANDIDATES.size)
        var line = 0
        var inQ = false
        var i = 0
        while (i < text.length && line < 5) {
            val c = text[i]
            if (c == '"') inQ = !inQ
            else if (!inQ) {
                if (c == '\n') line++
                else {
                    val k = CANDIDATES.indexOf(c)
                    if (k >= 0) counts[k]++
                }
            }
            i++
        }
        var best = 0
        for (k in 1 until counts.size) if (counts[k] > counts[best]) best = k
        return if (counts[best] == 0) ',' else CANDIDATES[best]
    }

    fun parse(text: String, delim: Char): Parsed {
        val rows = ArrayList<ArrayList<String>>()
        var row = ArrayList<String>()
        val sb = StringBuilder()
        var inQ = false
        var any = false
        var cells = 0
        var quoted = 0
        var cellQuoted = false
        val n = text.length
        var i = 0
        while (i < n) {
            val c = text[i]
            if (inQ) {
                if (c == '"') {
                    if (i + 1 < n && text[i + 1] == '"') { sb.append('"'); i++ } else inQ = false
                } else sb.append(c)
            } else if (c == '"' && sb.isEmpty() && !cellQuoted) {
                inQ = true; any = true; cellQuoted = true
            } else if (c == delim) {
                row.add(sb.toString()); sb.setLength(0); cells++; if (cellQuoted) quoted++; cellQuoted = false; any = true
            } else if (c == '\n') {
                row.add(sb.toString()); sb.setLength(0); cells++; if (cellQuoted) quoted++; cellQuoted = false
                rows.add(row); row = ArrayList(); any = false
            } else {
                sb.append(c); any = true
            }
            i++
        }
        if (any || sb.isNotEmpty() || row.isNotEmpty()) {
            row.add(sb.toString()); cells++; if (cellQuoted) quoted++
            rows.add(row)
        }
        return Parsed(rows, cells > 0 && quoted * 10 >= cells * 9)
    }

    private fun needsQuote(s: String, d: Char): Boolean =
        s.indexOf(d) >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0

    fun cell(s: String, d: Char, quoteAll: Boolean): String =
        if (quoteAll || needsQuote(s, d)) "\"" + s.replace("\"", "\"\"") + "\"" else s

    fun serialize(rows: List<List<String>>, delim: Char, quoteAll: Boolean): String {
        val sb = StringBuilder()
        for (r in rows.indices) {
            val row = rows[r]
            for (c in row.indices) {
                if (c > 0) sb.append(delim)
                sb.append(cell(row[c], delim, quoteAll))
            }
            sb.append('\n')
        }
        return sb.toString()
    }

    fun columns(rows: List<List<String>>): Int {
        var m = 0
        for (r in rows) if (r.size > m) m = r.size
        return m
    }

    /** Spreadsheet-style column names: A, B, ..., Z, AA, ... */
    fun colName(i: Int): String {
        var n = i
        val sb = StringBuilder()
        do {
            sb.append('A' + n % 26)
            n = n / 26 - 1
        } while (n >= 0)
        return sb.reverse().toString()
    }
}
