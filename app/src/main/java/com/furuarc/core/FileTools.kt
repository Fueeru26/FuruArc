package com.furuarc.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Status summary of a findings list: [level] 1 healthy, 2 has warnings, 3 has problems. */
class Verdict(val level: Int, val warn: Int, val bad: Int)

fun verdictOf(findings: List<Finding>): Verdict {
    val w = findings.count { it.level == 2 }
    val b = findings.count { it.level == 3 }
    return Verdict(if (b > 0) 3 else if (w > 0) 2 else 1, w, b)
}

/** Match the hash text pasted by the user against the computed results. Returns the matching algorithm name, or null. */
fun matchHash(input: String, h: Hashes): String? {
    val x = input.trim().lowercase().filter { it.isLetterOrDigit() }
    if (x.isEmpty()) return null
    return when {
        x == h.crc32.lowercase() -> "CRC32"
        x == h.md5.lowercase() -> "MD5"
        x == h.sha256.lowercase() -> "SHA-256"
        else -> null
    }
}

/** Plausible hex lengths for known hashes (8/32/64), so the UI can flag incomplete input. */
fun hashLooksComplete(input: String): Boolean {
    val x = input.trim().filter { it.isLetterOrDigit() }
    return x.length == 8 || x.length == 32 || x.length == 64
}

// ======================= report text (.txt) =======================

object ReportText {
    fun mark(level: Int) = when (level) { 1 -> "[OK]"; 2 -> "[!] "; 3 -> "[X] "; else -> "[i] " }

    fun header(title: String, id: Boolean): StringBuilder {
        val sb = StringBuilder()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
        sb.append("FuruArc - ").append(title).append('\n')
        sb.append(if (id) "Dibuat: " else "Created: ").append(date).append('\n')
        sb.append("=".repeat(48)).append('\n')
        return sb
    }

    fun section(sb: StringBuilder, name: String) {
        sb.append('\n').append(name).append('\n').append("-".repeat(name.length.coerceAtLeast(8))).append('\n')
    }

    fun verdictLine(v: Verdict, id: Boolean): String = when (v.level) {
        3 -> if (id) "STATUS: ADA MASALAH (${v.bad} masalah, ${v.warn} peringatan)" else "STATUS: PROBLEMS FOUND (${v.bad} problems, ${v.warn} warnings)"
        2 -> if (id) "STATUS: ADA PERINGATAN (${v.warn} peringatan)" else "STATUS: WARNINGS (${v.warn} warnings)"
        else -> if (id) "STATUS: SEHAT" else "STATUS: HEALTHY"
    }

    fun findings(sb: StringBuilder, list: List<Finding>) {
        for (f in list) sb.append(mark(f.level)).append(' ').append(f.text).append('\n')
    }

    /** Report for one file (archive or script). */
    fun single(id: Boolean, rep: Report, hashes: Hashes?, deep: List<Finding>?): String {
        val sb = header(if (id) "Laporan Pemeriksaan Arsip/Skrip" else "Archive/Script Inspection Report", id)
        val all = rep.findings + (deep ?: emptyList())
        sb.append(verdictLine(verdictOf(all), id)).append('\n')
        section(sb, if (id) "Informasi" else "Information")
        for ((k, v) in rep.info) sb.append(k).append(": ").append(v).append('\n')
        section(sb, if (id) "Hasil pemeriksaan" else "Results")
        findings(sb, rep.findings)
        if (hashes != null) {
            section(sb, "Checksum")
            sb.append("CRC32:   ").append(hashes.crc32).append('\n')
            sb.append("MD5:     ").append(hashes.md5).append('\n')
            sb.append("SHA-256: ").append(hashes.sha256).append('\n')
        }
        if (deep != null) {
            section(sb, if (id) "Pemeriksaan integritas" else "Integrity check")
            findings(sb, deep)
        }
        return sb.toString()
    }
}

// ======================= compare =======================

class DiffItem(val kind: Int, val name: String, val sizeA: Long, val sizeB: Long) {
    companion object { const val SAME = 0; const val ADDED = 1; const val CHANGED = 2; const val REMOVED = 3 }
}

class DiffResult(val items: List<DiffItem>) {
    fun count(kind: Int) = items.count { it.kind == kind }
}

class TextDiff(val added: List<String>, val removed: List<String>, val same: Int, val linesA: Int, val linesB: Int)

class TrDiffItem(val kind: Int, val file: String, val path: String, val a: String, val b: String) {
    companion object { const val ADDED = 1; const val REMOVED = 2; const val ORIG = 3; const val TR = 4 }
}

class TrDiff(val items: List<TrDiffItem>, val filledA: Int, val filledB: Int, val totalA: Int, val totalB: Int) {
    fun count(kind: Int) = items.count { it.kind == kind }
}

object Compare {
    private fun norm(n: String) = n.replace('\\', '/')

    /** [sameContent] is called only for files with the same name and size; null = assume the same. */
    fun archives(a: List<Entry>, b: List<Entry>, sameContent: ((Entry, Entry) -> Boolean)?, check: () -> Unit = {}): DiffResult {
        val mb = LinkedHashMap<String, Entry>()
        for (e in b) mb[norm(e.name)] = e
        val out = ArrayList<DiffItem>()
        val seen = HashSet<String>()
        for (ea in a) {
            check()
            val k = norm(ea.name)
            seen.add(k)
            val eb = mb[k]
            if (eb == null) out.add(DiffItem(DiffItem.REMOVED, k, ea.size, -1))
            else {
                val changed = ea.size != eb.size || (sameContent != null && !sameContent(ea, eb))
                out.add(DiffItem(if (changed) DiffItem.CHANGED else DiffItem.SAME, k, ea.size, eb.size))
            }
        }
        for ((k, eb) in mb) if (k !in seen) out.add(DiffItem(DiffItem.ADDED, k, -1, eb.size))
        return DiffResult(out.sortedWith(compareBy({ if (it.kind == DiffItem.SAME) 1 else 0 }, { it.name })))
    }

    /** Compare lines as a multiset (line order is not considered). */
    fun text(a: String, b: String): TextDiff {
        val la = a.split("\n").map { it.removeSuffix("\r") }
        val lb = b.split("\n").map { it.removeSuffix("\r") }
        val cnt = HashMap<String, Int>()
        for (l in la) cnt[l] = (cnt[l] ?: 0) + 1
        val added = ArrayList<String>()
        var same = 0
        for (l in lb) {
            val c = cnt[l] ?: 0
            if (c > 0) { cnt[l] = c - 1; same++ } else added.add(l)
        }
        val removed = ArrayList<String>()
        val left = HashMap(cnt)
        for (l in la) {
            val c = left[l] ?: 0
            if (c > 0) { left[l] = c - 1; removed.add(l) }
        }
        return TextDiff(added, removed, same, la.size, lb.size)
    }

    fun translations(a: List<TrItem>, b: List<TrItem>): TrDiff {
        val ma = LinkedHashMap<Pair<String, String>, TrItem>()
        for (x in a) ma[Pair(x.file, x.path)] = x
        val mb = LinkedHashMap<Pair<String, String>, TrItem>()
        for (x in b) mb[Pair(x.file, x.path)] = x
        val out = ArrayList<TrDiffItem>()
        for ((k, xa) in ma) {
            val xb = mb[k]
            if (xb == null) out.add(TrDiffItem(TrDiffItem.REMOVED, k.first, k.second, xa.orig, ""))
            else if (xa.orig != xb.orig) out.add(TrDiffItem(TrDiffItem.ORIG, k.first, k.second, xa.orig, xb.orig))
            else if (xa.tr != xb.tr) out.add(TrDiffItem(TrDiffItem.TR, k.first, k.second, xa.tr, xb.tr))
        }
        for ((k, xb) in mb) if (k !in ma) out.add(TrDiffItem(TrDiffItem.ADDED, k.first, k.second, "", xb.orig))
        return TrDiff(out, a.count { it.tr.isNotBlank() }, b.count { it.tr.isNotBlank() }, a.size, b.size)
    }
}

// ======================= translation scan =======================

class TrScanResult(
    val total: Int,
    val filled: Int,
    val empty: List<TrItem>,
    val sameAsOrig: List<TrItem>,
    val ctrlMismatch: List<TrItem>,
    val tooLong: List<TrItem>,
    val inconsistent: List<TrItem>,
    val dupKeys: Int,
    val longest: Int,
)

object TrScan {
    private val CODE = Regex("\\\\(?:[A-Za-z]+(?:\\[\\d+\\])?|[{}$.|!><^\\\\])")

    /** Visible length of one line: control codes are not counted. */
    fun visibleLen(line: String) = CODE.replace(line, "").length

    fun scan(items: List<TrItem>, maxLen: Int): TrScanResult {
        val empty = ArrayList<TrItem>()
        val same = ArrayList<TrItem>()
        val ctrl = ArrayList<TrItem>()
        val long = ArrayList<TrItem>()
        var filled = 0
        var longest = 0
        val byOrig = HashMap<String, MutableList<TrItem>>()
        val keys = HashSet<Pair<String, String>>()
        var dup = 0
        for (it in items) {
            if (!keys.add(Pair(it.file, it.path))) dup++
            if (it.tr.isBlank()) { empty.add(it); continue }
            filled++
            if (it.tr == it.orig) same.add(it)
            if (RpgmText.controlCodes(it.orig) != RpgmText.controlCodes(it.tr)) ctrl.add(it)
            val ml = it.tr.split("\n").maxOf { l -> visibleLen(l) }
            if (ml > longest) longest = ml
            if (maxLen > 0 && ml > maxLen) long.add(it)
            if (it.orig.isNotBlank()) byOrig.getOrPut(it.orig) { ArrayList() }.add(it)
        }
        val incons = ArrayList<TrItem>()
        for ((_, l) in byOrig) if (l.map { it.tr }.toSet().size > 1) incons.addAll(l)
        return TrScanResult(items.size, filled, empty, same, ctrl, long, incons, dup, longest)
    }

    fun report(id: Boolean, name: String, r: TrScanResult, maxLen: Int, limit: Int = 200): String {
        fun t(a: String, b: String) = if (id) a else b
        val sb = ReportText.header(t("Laporan Pemindaian Terjemahan", "Translation Scan Report"), id)
        sb.append(t("File: ", "File: ")).append(name).append('\n')
        ReportText.section(sb, t("Ringkasan", "Summary"))
        val pct = if (r.total > 0) r.filled * 100 / r.total else 0
        sb.append(t("Total entri: ", "Total entries: ")).append(r.total).append('\n')
        sb.append(t("Terisi: ", "Filled: ")).append(r.filled).append(" ($pct%)\n")
        sb.append(t("Kosong: ", "Empty: ")).append(r.empty.size).append('\n')
        sb.append(t("Sama dengan teks asli: ", "Same as original: ")).append(r.sameAsOrig.size).append('\n')
        sb.append(t("Kode kontrol berbeda: ", "Control codes differ: ")).append(r.ctrlMismatch.size).append('\n')
        sb.append(t("Baris lebih panjang dari $maxLen: ", "Lines longer than $maxLen: ")).append(r.tooLong.size).append('\n')
        sb.append(t("Terjemahan tidak konsisten (teks asli sama, terjemahan beda): ", "Inconsistent (same original, different translation): ")).append(r.inconsistent.size).append('\n')
        if (r.dupKeys > 0) sb.append(t("Kunci (file+path) ganda: ", "Duplicate keys (file+path): ")).append(r.dupKeys).append('\n')
        sb.append(t("Baris terpanjang: ", "Longest line: ")).append(r.longest).append('\n')
        fun block(title: String, l: List<TrItem>) {
            if (l.isEmpty()) return
            ReportText.section(sb, "$title (${l.size})")
            for (x in l.take(limit)) {
                sb.append("* ").append(x.file).append(" | ").append(x.path).append('\n')
                sb.append("  O: ").append(x.orig.replace("\n", "\\n")).append('\n')
                if (x.tr.isNotBlank()) sb.append("  T: ").append(x.tr.replace("\n", "\\n")).append('\n')
            }
            if (l.size > limit) sb.append(t("… dan ${l.size - limit} lainnya\n", "… and ${l.size - limit} more\n"))
        }
        block(t("Kode kontrol berbeda", "Control codes differ"), r.ctrlMismatch)
        block(t("Baris terlalu panjang", "Lines too long"), r.tooLong)
        block(t("Tidak konsisten", "Inconsistent"), r.inconsistent)
        block(t("Sama dengan teks asli", "Same as original"), r.sameAsOrig)
        block(t("Kosong", "Empty"), r.empty)
        return sb.toString()
    }
}
