package com.furuarc.core

/** Difference between two line lists, for "View changes". */
object Diff {
    /** kind: 0 = same, 1 = added, 2 = removed, 3 = separator for a skipped section. */
    class Line(val kind: Int, val text: String)

    class Result(val lines: List<Line>, val added: Int, val removed: Int, val tooBig: Boolean)

    private const val MAX_CELLS = 3_000_000L

    fun lines(a: List<String>, b: List<String>, context: Int = 2): Result {
        var s = 0
        while (s < a.size && s < b.size && a[s] == b[s]) s++
        var ea = a.size
        var eb = b.size
        while (ea > s && eb > s && a[ea - 1] == b[eb - 1]) { ea--; eb-- }
        val n = ea - s
        val m = eb - s
        if (n.toLong() * m > MAX_CELLS) return Result(emptyList(), n, m, true)
        val dp = Array(n + 1) { IntArray(m + 1) }
        for (i in n - 1 downTo 0) {
            for (j in m - 1 downTo 0) {
                dp[i][j] = if (a[s + i] == b[s + j]) dp[i + 1][j + 1] + 1 else maxOf(dp[i + 1][j], dp[i][j + 1])
            }
        }
        val all = ArrayList<Line>()
        for (k in 0 until s) all.add(Line(0, a[k]))
        var i = 0
        var j = 0
        var add = 0
        var rem = 0
        while (i < n && j < m) {
            if (a[s + i] == b[s + j]) { all.add(Line(0, a[s + i])); i++; j++ }
            else if (dp[i + 1][j] >= dp[i][j + 1]) { all.add(Line(2, a[s + i])); i++; rem++ }
            else { all.add(Line(1, b[s + j])); j++; add++ }
        }
        while (i < n) { all.add(Line(2, a[s + i])); i++; rem++ }
        while (j < m) { all.add(Line(1, b[s + j])); j++; add++ }
        for (k in ea until a.size) all.add(Line(0, a[k]))

        val keep = BooleanArray(all.size)
        for (k in all.indices) {
            if (all[k].kind != 0) {
                for (x in maxOf(0, k - context)..minOf(all.size - 1, k + context)) keep[x] = true
            }
        }
        val out = ArrayList<Line>()
        var gap = false
        for (k in all.indices) {
            if (keep[k]) { out.add(all[k]); gap = false }
            else if (!gap && out.isNotEmpty()) { out.add(Line(3, "")); gap = true }
        }
        return Result(out, add, rem, false)
    }
}
