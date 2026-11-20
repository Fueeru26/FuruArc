package com.furuarc.core

/** One translatable text: [file] = data file name, [path] = location inside that file. */
/** [speaker] = speaker name if known (used for VNTP JSON export). */
class TextUnit(val file: String, val path: String, val text: String, val speaker: String? = null)

/** One entry in the Translation Editor: original text [orig] and translation [tr] (empty if not yet filled). */
data class TrItem(val file: String, val path: String, val orig: String, val tr: String)

/** Contents of a Translation Editor file. [marker] = this file is marked as a draft in its contents. */
class TrEditFile(val items: List<TrItem>, val marker: Boolean)

/** Result of a translation import: number of texts replaced per file. */
class ApplyResult(val changed: Int)

/**
 * Export/import of text from RPGM MV/MZ data (.json files in the data folder): dialogue, choices, database names & descriptions,
 * system terms. Consecutive dialogue lines (401/405) are merged into one multi-line text.
 */
object RpgmText {
    private val DB_FIELDS = mapOf(
        "Actors" to listOf("name", "nickname", "profile"),
        "Classes" to listOf("name"),
        "Skills" to listOf("name", "description", "message1", "message2"),
        "Items" to listOf("name", "description"),
        "Weapons" to listOf("name", "description"),
        "Armors" to listOf("name", "description"),
        "Enemies" to listOf("name"),
        "States" to listOf("name", "message1", "message2", "message3", "message4"),
        "MapInfos" to listOf("name"),
        "Tilesets" to listOf("name"),
        "Animations" to listOf("name"),
    )
    private const val PLUGINS = "plugins.js"
    private val TEXTY_KEY = Regex("text|name|msg|message|word|term|label|title|desc|vocab|help", RegexOption.IGNORE_CASE)
    private val MAP_RE = Regex("^Map\\d+$")

    private fun baseName(path: String) = path.substringAfterLast('/').removeSuffix(".json").removeSuffix(".JSON")

    /** Does this file contain recognized text? */
    fun isTextFile(path: String): Boolean {
        if (path.replace('\\', '/').substringAfterLast('/').equals(PLUGINS, true)) return true
        if (!path.lowercase().endsWith(".json")) return false
        val b = baseName(path)
        return MAP_RE.matches(b) || b == "CommonEvents" || b == "Troops" || b == "System" || DB_FIELDS.containsKey(b)
    }

    /** File key in the translation file: starts from the last "data" folder (so it does not depend on the parent folder). */
    fun fileKey(relPath: String): String {
        val parts = relPath.replace('\\', '/').split('/').filter { it.isNotEmpty() }
        val di = parts.indexOfLast { it.equals("data", ignoreCase = true) }
        return if (di >= 0 && di < parts.size - 1) parts.subList(di, parts.size).joinToString("/") else parts.last()
    }

    /** Handler for one text: receives the key & original text, returns the new text or null if unchanged. */
    fun interface Handler {
        fun text(path: String, original: String): String?
    }

    @Suppress("UNCHECKED_CAST")
    private fun map(v: Any?) = v as? MutableMap<String, Any?>

    @Suppress("UNCHECKED_CAST")
    private fun list(v: Any?) = v as? MutableList<Any?>

    private fun codeOf(cmd: Any?): Int? = (map(cmd)?.get("code") as? JNum)?.int()

    private fun params(cmd: Any?): MutableList<Any?>? = list(map(cmd)?.get("parameters"))

    /** Run [h] on every text in [root] (the parse result of one file). Modifies [root] if [h] returns a new text. */
    fun walk(fileName: String, root: Any?, h: Handler) {
        val b = if (fileName.substringAfterLast('/').equals(PLUGINS, true)) PLUGINS else baseName(fileName)
        when {
            b == PLUGINS -> {
                val a = list(root) ?: return
                for ((i, pl) in a.withIndex()) {
                    val pm = map(pl) ?: continue
                    if (pm["status"] == false) continue
                    val name = (pm["name"] as? String) ?: "plugin"
                    val ps = map(pm["parameters"]) ?: continue
                    for (k in ps.keys.toList()) {
                        val v = ps[k] as? String ?: continue
                        if (looksLikeText(k, v)) h.text("$i/$name/$k", v)?.let { ps[k] = it }
                    }
                }
            }
            MAP_RE.matches(b) -> {
                val m = map(root) ?: return
                (m["displayName"] as? String)?.let { s -> if (s.isNotBlank()) h.text("displayName", s)?.let { m["displayName"] = it } }
                val ev = list(m["events"]) ?: return
                for ((ei, e) in ev.withIndex()) {
                    val pages = list(map(e)?.get("pages")) ?: continue
                    for ((pi, pg) in pages.withIndex()) {
                        list(map(pg)?.get("list"))?.let { walkList(it, "events/$ei/pages/$pi/list", h) }
                    }
                }
            }
            b == "CommonEvents" -> {
                val a = list(root) ?: return
                for ((i, e) in a.withIndex()) list(map(e)?.get("list"))?.let { walkList(it, "$i/list", h) }
            }
            b == "Troops" -> {
                val a = list(root) ?: return
                for ((i, t) in a.withIndex()) {
                    val pages = list(map(t)?.get("pages")) ?: continue
                    for ((pi, pg) in pages.withIndex()) list(map(pg)?.get("list"))?.let { walkList(it, "$i/pages/$pi/list", h) }
                }
            }
            b == "System" -> {
                val m = map(root) ?: return
                for (k in listOf("gameTitle", "currencyUnit")) field(m, k, k, h)
                for (k in listOf("armorTypes", "elements", "equipTypes", "skillTypes", "weaponTypes")) strings(list(m[k]), k, h)
                val terms = map(m["terms"]) ?: return
                for ((k, v) in terms) {
                    if (v is MutableList<*>) strings(list(v), "terms/$k", h)
                    else map(v)?.let { mm -> for (kk in mm.keys.toList()) field(mm, kk, "terms/$k/$kk", h) }
                }
            }
            else -> {
                val fields = DB_FIELDS[b] ?: return
                val a = list(root) ?: return
                for ((i, o) in a.withIndex()) {
                    val m = map(o) ?: continue
                    for (f in fields) field(m, f, "$i/$f", h)
                }
            }
        }
    }

    /** Plugin parameters that appear to be text displayed in the game (not numbers, true/false, JSON, or file names). */
    private fun looksLikeText(key: String, v: String): Boolean {
        val t = v.trim()
        if (t.isEmpty() || t == "true" || t == "false" || t.toDoubleOrNull() != null) return false
        if (t.startsWith("[") || t.startsWith("{")) return false
        if (!t.any { it.isLetter() }) return false
        if (t.any { it == ' ' } || t.any { it.code > 127 }) return true
        return TEXTY_KEY.containsMatchIn(key)
    }

    /** Contents of one data file: plain JSON, or plugins.js (a JSON array inside a script). */
    class DataFile(val prefix: String, val root: Any?, val suffix: String) {
        fun write(): String = if (prefix.isEmpty() && suffix.isEmpty()) Json.write(root) else prefix + Json.write(root) + suffix
    }

    fun parseData(path: String, text: String): DataFile {
        if (path.replace('\\', '/').substringAfterLast('/').equals(PLUGINS, true)) {
            val from = text.indexOf("\$plugins").let { if (it < 0) 0 else it }
            val a = text.indexOf('[', from)
            val z = text.lastIndexOf(']')
            if (a < 0 || z < a) throw IllegalArgumentException("plugins.js")
            return DataFile(text.substring(0, a), Json.parse(text.substring(a, z + 1)), text.substring(z + 1))
        }
        return DataFile("", Json.parse(text), "")
    }

    private fun field(m: MutableMap<String, Any?>, key: String, path: String, h: Handler) {
        val s = m[key] as? String ?: return
        if (s.isBlank()) return
        h.text(path, s)?.let { m[key] = it }
    }

    private fun strings(a: MutableList<Any?>?, base: String, h: Handler) {
        if (a == null) return
        for (i in a.indices) {
            val s = a[i] as? String ?: continue
            if (s.isBlank()) continue
            h.text("$base/$i", s)?.let { a[i] = it }
        }
    }

    private fun walkList(lst: MutableList<Any?>, base: String, h: Handler) {
        val out = ArrayList<Any?>(lst.size)
        var changed = false
        var i = 0
        while (i < lst.size) {
            val cmd = lst[i]
            when (val code = codeOf(cmd)) {
                401, 405 -> {
                    var j = i
                    while (j < lst.size && codeOf(lst[j]) == code) j++
                    val lines = ArrayList<String>()
                    for (k in i until j) lines.add((params(lst[k])?.getOrNull(0) as? String) ?: "")
                    val original = lines.joinToString("\n")
                    val nt = if (original.isBlank()) null else h.text("$base/$i", original)
                    if (nt == null || nt == original) {
                        for (k in i until j) out.add(lst[k])
                    } else {
                        changed = true
                        val proto = map(cmd)!!
                        for (line in nt.split("\n")) {
                            val c = LinkedHashMap<String, Any?>(proto)
                            c["parameters"] = arrayListOf<Any?>(line)
                            out.add(c)
                        }
                    }
                    i = j
                    continue
                }
                101 -> {
                    // MZ: parameters[4] = speaker name
                    val p = params(cmd)
                    val sp = p?.getOrNull(4) as? String
                    if (p != null && sp != null && sp.isNotBlank()) {
                        h.text("$base/$i#n", sp)?.let { p[4] = it }
                    }
                    out.add(cmd)
                }
                102 -> {
                    val ch = list(params(cmd)?.getOrNull(0))
                    strings(ch, "$base/$i/c", h)
                    out.add(cmd)
                }
                320, 324, 325 -> {
                    val p = params(cmd)
                    val s = p?.getOrNull(1) as? String
                    if (p != null && s != null && s.isNotBlank()) h.text("$base/$i", s)?.let { p[1] = it }
                    out.add(cmd)
                }
                else -> out.add(cmd)
            }
            i++
        }
        if (changed) {
            lst.clear()
            lst.addAll(out)
        }
    }

    /** Collect all texts from one file (parse result). */
    fun extract(fileKey: String, root: Any?): List<TextUnit> {
        val r = ArrayList<TextUnit>()
        walk(fileKey, root) { path, text -> r.add(TextUnit(fileKey, path, text)); null }
        return r
    }

    /** Import statistics: [matched] = translation entries found in the game, [warnings] = "file | path" with differing control codes. */
    class ApplyStats {
        var matched = 0
        val warnings = ArrayList<String>()
    }

    private val CODE_RE = Regex("\\\\(?:[A-Za-z]+(?:\\[\\d+\\])?|[{}$.|!><^\\\\])")

    /** RPG Maker message control codes (\\C[2], \\N[1], \\V[3], \\!, etc.) in [s], sorted. */
    fun controlCodes(s: String): List<String> = CODE_RE.findAll(s).map { it.value.uppercase() }.sorted().toList()

    /** Apply translations [tr] (key "path") to [root]. Returns the number of texts replaced. */
    fun apply(fileKey: String, root: Any?, tr: Map<String, String>, stats: ApplyStats? = null): Int {
        var n = 0
        walk(fileKey, root) { path, text ->
            val t = tr[path]
            if (t != null) stats?.let { it.matched++ }
            if (t != null && t != text) {
                n++
                if (stats != null && controlCodes(text) != controlCodes(t)) stats.warnings.add("$fileKey | $path")
                t
            } else null
        }
        return n
    }

    // ---------- translation file: CSV ----------

    fun toCsv(units: List<TextUnit>): String {
        val sb = StringBuilder("﻿")
        sb.append("file,path,original,translation\r\n")
        for (u in units) {
            sb.append(csv(u.file)).append(',').append(csv(u.path)).append(',').append(csv(u.text)).append(",\r\n")
        }
        return sb.toString()
    }

    private fun csv(s: String): String =
        if (s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + s.replace("\"", "\"\"") + "\"" else s

    fun parseCsv(text0: String): List<List<String>> {
        val text = if (text0.startsWith("﻿")) text0.substring(1) else text0
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val cell = StringBuilder()
        var inQ = false
        var i = 0
        var any = false
        while (i < text.length) {
            val c = text[i]
            if (inQ) {
                if (c == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') { cell.append('"'); i++ } else inQ = false
                } else cell.append(c)
            } else when (c) {
                '"' -> { inQ = true; any = true }
                ',' -> { row.add(cell.toString()); cell.setLength(0); any = true }
                '\r' -> {}
                '\n' -> {
                    if (any || cell.isNotEmpty()) { row.add(cell.toString()); rows.add(row) }
                    row = ArrayList(); cell.setLength(0); any = false
                }
                else -> { cell.append(c); any = true }
            }
            i++
        }
        if (any || cell.isNotEmpty()) { row.add(cell.toString()); rows.add(row) }
        return rows
    }

    /** Contents of a translation file: [map] = only filled entries; [rows] = all entries; [empty] = empty / unchanged entries. */
    class TrFile(val map: Map<Pair<String, String>, String>, val rows: Int, val empty: Int)

    fun readCsv(text: String): TrFile {
        val r = LinkedHashMap<Pair<String, String>, String>()
        var rows = 0
        var empty = 0
        for ((n, row) in parseCsv(text).withIndex()) {
            if (row.size < 4) continue
            if (n == 0 && row[0].equals("file", true) && row[1].equals("path", true)) continue
            rows++
            if (row[3].isNotEmpty()) r[Pair(row[0], row[1])] = row[3] else empty++
        }
        return TrFile(r, rows, empty)
    }

    /** CSV -> map (file, path) -> translation; rows with an empty translation column are skipped. */
    fun fromCsv(text: String): Map<Pair<String, String>, String> = readCsv(text).map

    // ---------- translation file: TXT ----------

    fun toTxt(units: List<TextUnit>): String {
        val sb = StringBuilder()
        sb.append("# FuruArc translation v1\n")
        sb.append("# Ubah hanya baris [T]. Baris [O] adalah teks asli (jangan diubah). Pisahkan blok dengan baris kosong.\n\n")
        for (u in units) {
            sb.append("[#] ").append(u.file).append(" | ").append(u.path).append('\n')
            for (l in u.text.split("\n")) sb.append("[O] ").append(l).append('\n')
            for (l in u.text.split("\n")) sb.append("[T] ").append(l).append('\n')
            sb.append('\n')
        }
        return sb.toString()
    }

    fun fromTxt(text: String): Map<Pair<String, String>, String> = readTxt(text).map

    fun readTxt(text0: String): TrFile {
        val text = if (text0.startsWith("﻿")) text0.substring(1) else text0
        val r = LinkedHashMap<Pair<String, String>, String>()
        var rows = 0
        var empty = 0
        var key: Pair<String, String>? = null
        var orig = ArrayList<String>()
        var tr: ArrayList<String>? = null
        fun flush() {
            val k = key
            val t = tr
            if (k != null) {
                rows++
                val s = t?.joinToString("\n")
                if (s != null && s != orig.joinToString("\n") && s.isNotBlank()) r[k] = s else empty++
            }
            key = null; orig = ArrayList(); tr = null
        }
        fun tail(l: String, n: Int) = l.substring(n).removePrefix(" ")
        for (raw in text.split("\n")) {
            val l = raw.removeSuffix("\r")
            when {
                l.startsWith("[#]") -> {
                    flush()
                    val h = tail(l, 3)
                    val cut = h.lastIndexOf(" | ")
                    if (cut > 0) key = Pair(h.substring(0, cut), h.substring(cut + 3))
                }
                l.startsWith("[O]") -> orig.add(tail(l, 3))
                l.startsWith("[T]") -> (tr ?: ArrayList<String>().also { tr = it }).add(tail(l, 3))
            }
        }
        flush()
        return TrFile(r, rows, empty)
    }

    // ---------- Translation Editor: read & write all entries (including empty ones) ----------

    /** This file name indicates a draft (contains "_draf"). */
    fun isDraftName(name: String) = name.lowercase().contains("_draf")

    fun readEntries(text0: String, csv: Boolean): TrEditFile {
        val text = if (text0.startsWith("﻿")) text0.substring(1) else text0
        val items = ArrayList<TrItem>()
        var marker = false
        if (csv) {
            for ((n, row) in parseCsv(text).withIndex()) {
                if (row.size < 4) continue
                if (n == 0 && row[0].equals("file", true) && row[1].equals("path", true)) {
                    marker = row.size >= 5 && row[4].equals("draft", true)
                    continue
                }
                items.add(TrItem(row[0], row[1], row[2], row[3]))
            }
            return TrEditFile(items, marker)
        }
        var key: Pair<String, String>? = null
        var orig = ArrayList<String>()
        var tr: ArrayList<String>? = null
        fun flush() {
            val k = key
            if (k != null) {
                val t = tr?.joinToString("\n") ?: ""
                items.add(TrItem(k.first, k.second, orig.joinToString("\n"), if (t.isBlank()) "" else t))
            }
            key = null; orig = ArrayList(); tr = null
        }
        fun tail(l: String, n: Int) = l.substring(n).removePrefix(" ")
        for (raw in text.split("\n")) {
            val l = raw.removeSuffix("\r")
            when {
                l.startsWith("# FuruArc draft") || l.startsWith("# GameArc draft") -> marker = true
                l.startsWith("[#]") -> {
                    flush()
                    val h = tail(l, 3)
                    val cut = h.lastIndexOf(" | ")
                    if (cut > 0) key = Pair(h.substring(0, cut), h.substring(cut + 3))
                }
                l.startsWith("[O]") -> orig.add(tail(l, 3))
                l.startsWith("[T]") -> (tr ?: ArrayList<String>().also { tr = it }).add(tail(l, 3))
            }
        }
        flush()
        return TrEditFile(items, marker)
    }

    /** Write CSV from [items]. Drafts get a "draft" marker column in the header row. */
    fun writeCsv(items: List<TrItem>, draft: Boolean): String {
        val sb = StringBuilder("﻿")
        sb.append("file,path,original,translation").append(if (draft) ",draft" else "").append("\r\n")
        for (u in items) sb.append(csv(u.file)).append(',').append(csv(u.path)).append(',').append(csv(u.orig)).append(',').append(csv(u.tr)).append("\r\n")
        return sb.toString()
    }

    /**
     * Write TXT from [items]. Draft: the [T] line contains the translation as is (empty if not yet filled).
     * Not a draft: an empty translation is written as a copy of the original text, like the Export Text result.
     */
    fun writeTxt(items: List<TrItem>, draft: Boolean): String {
        val sb = StringBuilder()
        sb.append("# FuruArc translation v1\n")
        if (draft) sb.append("# FuruArc draft\n")
        sb.append("# Ubah hanya baris [T]. Baris [O] adalah teks asli (jangan diubah). Pisahkan blok dengan baris kosong.\n\n")
        for (u in items) {
            sb.append("[#] ").append(u.file).append(" | ").append(u.path).append('\n')
            for (l in u.orig.split("\n")) sb.append("[O] ").append(l).append('\n')
            val t = if (u.tr.isNotBlank()) u.tr else if (draft) "" else u.orig
            for (l in t.split("\n")) sb.append("[T] ").append(l).append('\n')
            sb.append('\n')
        }
        return sb.toString()
    }

    // ---------- translation file formats: CSV (0) / TXT (1) / JSON (2) ----------

    const val FMT_CSV = 0
    const val FMT_TXT = 1
    const val FMT_JSON = 2
    /** VNTP-style JSON: names and dialogue only; cannot be imported back. */
    const val FMT_VNTP = 3

    fun extFor(fmt: Int) = when (fmt) { FMT_TXT -> "txt"; FMT_JSON, FMT_VNTP -> "json"; else -> "csv" }

    /** Guess the translation file format from its name and contents. */
    fun formatOf(name: String, text: String): Int {
        val low = name.lowercase()
        if (low.endsWith(".json")) return FMT_JSON
        if (low.endsWith(".txt")) return FMT_TXT
        if (low.endsWith(".csv")) return FMT_CSV
        val head = text.trimStart('\uFEFF', ' ', '\n', '\r', '\t')
        if (head.startsWith("{") || (head.startsWith("[") && !head.startsWith("[#]") && !head.startsWith("[O]"))) return FMT_JSON
        return if (text.contains("[#] ")) FMT_TXT else FMT_CSV
    }

    /** Export result: all texts as a file in format [fmt]. */
    fun export(units: List<TextUnit>, fmt: Int): String = when (fmt) {
        FMT_TXT -> toTxt(units)
        FMT_JSON -> toJson(units.map { TrItem(it.file, it.path, it.text, "") }, false)
        FMT_VNTP -> toVntp(units)
        else -> toCsv(units)
    }

    /** [{"name": "...", "message": "..."}, ...] — "name" only when present; line breaks are written as \r\n. */
    fun toVntp(units: List<TextUnit>): String {
        val arr = ArrayList<Any?>()
        for (u in units) {
            val m = LinkedHashMap<String, Any?>()
            if (!u.speaker.isNullOrEmpty()) m["name"] = u.speaker
            m["message"] = u.text.replace("\r\n", "\n").replace("\n", "\r\n")
            arr.add(m)
        }
        return Json.write(arr, true)
    }

    fun toJson(items: List<TrItem>, draft: Boolean): String {
        val root = LinkedHashMap<String, Any?>()
        root["format"] = "FuruArc translation v1"
        if (draft) root["draft"] = true
        val arr = ArrayList<Any?>()
        for (u in items) {
            val m = LinkedHashMap<String, Any?>()
            m["file"] = u.file; m["path"] = u.path; m["original"] = u.orig; m["translation"] = u.tr
            arr.add(m)
        }
        root["entries"] = arr
        return Json.write(root, true) + "\n"
    }

    private fun jsonEntries(text: String): Pair<List<TrItem>, Boolean> {
        val root = Json.parse(text)
        val draft = (root as? Map<*, *>)?.get("draft") == true
        if (root is List<*> && root.firstOrNull().let { it is Map<*, *> && it.containsKey("message") && !it.containsKey("path") })
            throw IllegalArgumentException("JSON VNTP hanya untuk dibaca penerjemah; tidak bisa diimpor. Impor berkas JSON/CSV/TXT dari ekspor FuruArc.")
        val list = when (root) {
            is List<*> -> root
            is Map<*, *> -> root["entries"] as? List<*> ?: throw IllegalArgumentException("JSON: kunci \"entries\" tidak ada")
            else -> throw IllegalArgumentException("JSON: bentuk berkas tidak dikenali")
        }
        val out = ArrayList<TrItem>()
        for (e in list) {
            val m = e as? Map<*, *> ?: continue
            val file = m["file"] as? String ?: continue
            val path = m["path"] as? String ?: continue
            val orig = m["original"] as? String ?: ""
            val tr = m["translation"] as? String ?: ""
            out.add(TrItem(file, path, orig, tr))
        }
        return Pair(out, draft)
    }

    fun readJson(text: String): TrFile {
        val r = LinkedHashMap<Pair<String, String>, String>()
        var empty = 0
        val (items, _) = jsonEntries(text)
        for (it in items) if (it.tr.isNotBlank() && it.tr != it.orig) r[Pair(it.file, it.path)] = it.tr else empty++
        return TrFile(r, items.size, empty)
    }

    /** Read a translation file (filled entries only) in any format. */
    fun readAny(name: String, text: String): TrFile = when (formatOf(name, text)) {
        FMT_JSON -> readJson(text)
        FMT_TXT -> readTxt(text)
        else -> readCsv(text)
    }

    /** For the Translation Editor: all entries. */
    fun readEntriesAny(name: String, text: String): Pair<TrEditFile, Int> {
        val fmt = formatOf(name, text)
        if (fmt == FMT_JSON) {
            val (items, draft) = jsonEntries(text)
            return Pair(TrEditFile(items, draft), fmt)
        }
        return Pair(readEntries(text, fmt == FMT_CSV), fmt)
    }

    fun write(items: List<TrItem>, draft: Boolean, fmt: Int): String = when (fmt) {
        FMT_JSON -> toJson(items, draft)
        FMT_TXT -> writeTxt(items, draft)
        else -> writeCsv(items, draft)
    }
}
