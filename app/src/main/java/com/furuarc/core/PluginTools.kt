package com.furuarc.core

/** A single item that can be changed in a .js plugin. */
class PItem(
    val kind: Int,        // KIND_*
    val label: String,    // parameter name / location description
    val orig: String,     // original content shown (JS escapes already decoded)
    val start: Int,       // range in the source text that gets replaced
    val end: Int,
    val quote: Char,      // quote character of the code string (0 for others)
    val line: Int,
    val entry: Int = -1,  // plugins.js item: index of the plugin entry
    val key: String = "", // plugins.js item: parameter name
) {
    val isText get() = kind == KIND_CODE_STR || kind == KIND_ANNO_TEXT || kind == KIND_PARAM_TEXT
    val isValue get() = !isText

    companion object {
        const val KIND_CODE_STR = 0   // string inside code
        const val KIND_ANNO_TEXT = 1  // @plugindesc / @text / @desc
        const val KIND_CODE_VALUE = 2 // default value in code: parameters['x'] || 10
        const val KIND_ANNO_VALUE = 3 // non-text @default in annotation
        const val KIND_PARAM_TEXT = 4  // text parameter in plugins.js
        const val KIND_PARAM_VALUE = 5 // number/boolean parameter in plugins.js
    }
}

/** Summary of a single plugin file. */
class PluginFileInfo(
    val file: String,
    val size: Int,
    val desc: String,
    val params: Int,
    val items: List<PItem>,
) {
    val textCount get() = items.count { it.isText }
    val valueCount get() = items.count { it.isValue }
}

object PluginScan {
    private val VALUE_TYPES = setOf(
        "number", "boolean", "select", "combo", "switch", "variable", "color", "actor", "class", "skill", "item",
        "weapon", "armor", "enemy", "troop", "state", "animation", "tileset", "common_event", "map",
    )
    private val TEXT_TYPES = setOf("", "string", "multiline_string", "note")
    private val CODE_DEFAULT = Regex("""\[\s*(['"])([^'"\n]{1,80})\1\s*\]\s*\|\|\s*(-?\d+(?:\.\d+)?|true|false)\b""")
    private val NUMBER = Regex("-?\\d+(\\.\\d+)?")

    fun hasText(s: String): Boolean {
        for (c in s) if (c.code >= 0x80 && c.isLetter()) return true
        return false
    }

    fun unescape(raw: String): String {
        if (raw.indexOf('\\') < 0) return raw
        val sb = StringBuilder()
        var i = 0
        while (i < raw.length) {
            val c = raw[i]
            if (c != '\\' || i + 1 >= raw.length) { sb.append(c); i++; continue }
            val n = raw[i + 1]
            i += 2
            when (n) {
                'n' -> sb.append('\n'); 'r' -> sb.append('\r'); 't' -> sb.append('\t')
                'b' -> sb.append('\b'); 'f' -> sb.append('\u000c'); 'v' -> sb.append('\u000b'); '0' -> sb.append('\u0000')
                'u' -> {
                    if (i < raw.length && raw[i] == '{') {
                        val e = raw.indexOf('}', i)
                        val cp = if (e > 0) raw.substring(i + 1, e).toIntOrNull(16) else null
                        if (cp != null) { sb.appendCodePoint(cp); i = e + 1 } else sb.append('u')
                    } else {
                        val h = if (i + 4 <= raw.length) raw.substring(i, i + 4).toIntOrNull(16) else null
                        if (h != null) { sb.append(h.toChar()); i += 4 } else sb.append('u')
                    }
                }
                'x' -> {
                    val h = if (i + 2 <= raw.length) raw.substring(i, i + 2).toIntOrNull(16) else null
                    if (h != null) { sb.append(h.toChar()); i += 2 } else sb.append('x')
                }
                '\n' -> {}
                '\r' -> { if (i < raw.length && raw[i] == '\n') i++ }
                else -> sb.append(n)
            }
        }
        return sb.toString()
    }

    fun escape(s: String, q: Char): String {
        val sb = StringBuilder()
        for (c in s) {
            when {
                c == '\\' -> sb.append("\\\\")
                c == q -> sb.append('\\').append(c)
                c == '\n' -> sb.append("\\n")
                c == '\r' -> sb.append("\\r")
                c == ' ' -> sb.append("\\u2028")
                c == ' ' -> sb.append("\\u2029")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    private class Lexed(val strings: List<IntArray>, val comments: List<IntArray>) // string: [start,end,quote]; comment: [start,end]

    private val REGEX_AFTER_WORD = setOf("return", "typeof", "case", "in", "of", "delete", "void", "throw", "new", "else", "do", "instanceof")

    private fun lex(s: String): Lexed {
        val strs = ArrayList<IntArray>()
        val coms = ArrayList<IntArray>()
        var i = 0
        val n = s.length
        var lastSig = '\u0000'
        var lastWord = ""
        while (i < n) {
            val c = s[i]
            when {
                c == '/' && i + 1 < n && s[i + 1] == '/' -> {
                    var e = s.indexOf('\n', i); if (e < 0) e = n
                    coms.add(intArrayOf(i, e)); i = e
                }
                c == '/' && i + 1 < n && s[i + 1] == '*' -> {
                    var e = s.indexOf("*/", i + 2); e = if (e < 0) n else e + 2
                    coms.add(intArrayOf(i, e)); i = e
                }
                c == '"' || c == '\'' -> {
                    var j = i + 1
                    while (j < n && s[j] != c && s[j] != '\n') { if (s[j] == '\\') j++; j++ }
                    if (j < n && s[j] == c) { strs.add(intArrayOf(i, j + 1, c.code)); i = j + 1 } else i = minOf(n, maxOf(j, i + 1))
                    lastSig = 'a'; lastWord = ""
                }
                c == '`' -> {
                    var j = i + 1
                    while (j < n && s[j] != '`') {
                        if (s[j] == '\\') j++
                        else if (s[j] == '$' && j + 1 < n && s[j + 1] == '{') {
                            var d = 1; j += 2
                            while (j < n && d > 0) { if (s[j] == '{') d++ else if (s[j] == '}') d--; j++ }
                            continue
                        }
                        j++
                    }
                    i = minOf(n, j + 1); lastSig = 'a'; lastWord = ""
                }
                c == '/' -> {
                    val regexOk = lastSig == '\u0000' || lastSig in "(,=:[!&|?{};+-*%<>~^" || lastWord in REGEX_AFTER_WORD
                    if (regexOk) {
                        var j = i + 1; var cls = false
                        while (j < n && s[j] != '\n') {
                            val d = s[j]
                            if (d == '\\') j++
                            else if (d == '[') cls = true
                            else if (d == ']') cls = false
                            else if (d == '/' && !cls) break
                            j++
                        }
                        i = if (j < n && s[j] == '/') j + 1 else i + 1
                        while (i < n && s[i].isLetter()) i++
                        lastSig = 'a'; lastWord = ""
                    } else { i++; lastSig = '/'; lastWord = "" }
                }
                c.isLetterOrDigit() || c == '_' || c == '$' -> {
                    var j = i
                    while (j < n && (s[j].isLetterOrDigit() || s[j] == '_' || s[j] == '$')) j++
                    lastWord = s.substring(i, j); lastSig = 'a'; i = j
                }
                c.isWhitespace() -> i++
                else -> { lastSig = c; lastWord = ""; i++ }
            }
        }
        return Lexed(strs, coms)
    }

    private fun lineOf(starts: IntArray, pos: Int): Int {
        var lo = 0; var hi = starts.size - 1
        while (lo < hi) { val m = (lo + hi + 1) / 2; if (starts[m] <= pos) lo = m else hi = m - 1 }
        return lo + 1
    }

    fun scan(file: String, src: String): PluginFileInfo {
        val text = src.removePrefix("﻿")
        val off = src.length - text.length
        val lx = lex(text)
        val ls = ArrayList<Int>(); ls.add(0)
        for (i in text.indices) if (text[i] == '\n') ls.add(i + 1)
        val lineStarts = ls.toIntArray()
        val items = ArrayList<PItem>()

        // 1) strings in code (outside comments) that contain non-ASCII letters
        for (t in lx.strings) {
            val raw = text.substring(t[0] + 1, t[1] - 1)
            val orig = unescape(raw)
            if (!hasText(orig)) continue
            items.add(PItem(PItem.KIND_CODE_STR, "", orig, t[0] + 1 + off, t[1] - 1 + off, t[2].toChar(), lineOf(lineStarts, t[0])))
        }
        // 2) default values in code
        for (m in CODE_DEFAULT.findAll(text)) {
            val g = m.groups[3]!!
            if (lx.comments.any { g.range.first >= it[0] && g.range.first < it[1] }) continue
            if (lx.strings.any { g.range.first >= it[0] && g.range.first < it[1] }) continue
            items.add(PItem(PItem.KIND_CODE_VALUE, m.groupValues[2], g.value, g.range.first + off, g.range.last + 1 + off, '\u0000', lineOf(lineStarts, g.range.first)))
        }
        // 3) annotations /*: ... */
        var desc = ""
        var params = 0
        for (cm in lx.comments) {
            val body = text.substring(cm[0], cm[1])
            if (!body.startsWith("/*:")) continue
            var pos = cm[0]
            var curParam = ""
            var curType = ""
            val pending = ArrayList<IntArray>() // [start,end] default
            fun flush() {
                for (p in pending) {
                    val v = text.substring(p[0], p[1])
                    val t = curType.lowercase()
                    if (t in VALUE_TYPES && v.isNotBlank()) items.add(PItem(PItem.KIND_ANNO_VALUE, curParam, v, p[0] + off, p[1] + off, '\u0000', lineOf(lineStarts, p[0])))
                    else if (t in TEXT_TYPES && hasText(v)) items.add(PItem(PItem.KIND_ANNO_TEXT, "$curParam @default", v, p[0] + off, p[1] + off, '\u0000', lineOf(lineStarts, p[0])))
                }
                pending.clear(); curType = ""
            }
            for (line in body.split('\n')) {
                val lineStart = pos
                pos += line.length + 1
                var k = 0
                while (k < line.length && (line[k].isWhitespace() || line[k] == '*')) k++
                if (k >= line.length || line[k] != '@') continue
                var e = k + 1
                while (e < line.length && (line[e].isLetter() || line[e] == '_')) e++
                val tag = line.substring(k + 1, e).lowercase()
                var vs = e
                while (vs < line.length && (line[vs] == ' ' || line[vs] == '\t')) vs++
                var ve = line.length
                while (ve > vs && (line[ve - 1].isWhitespace() || line[ve - 1] == '\r')) ve--
                if (ve <= vs && tag != "default") { if (tag == "param" || tag == "arg" || tag == "command") { flush(); curParam = "" }; continue }
                val value = line.substring(vs, ve)
                val abs0 = lineStart + vs; val abs1 = lineStart + ve
                when (tag) {
                    "param", "arg" -> { flush(); curParam = value; params++ }
                    "command" -> { flush(); curParam = value }
                    "plugindesc" -> {
                        if (desc.isEmpty()) desc = value
                        if (hasText(value)) items.add(PItem(PItem.KIND_ANNO_TEXT, "@plugindesc", value, abs0 + off, abs1 + off, '\u0000', lineOf(lineStarts, abs0)))
                    }
                    "text", "desc" -> if (hasText(value)) items.add(PItem(PItem.KIND_ANNO_TEXT, (if (curParam.isEmpty()) "" else "$curParam ") + "@$tag", value, abs0 + off, abs1 + off, '\u0000', lineOf(lineStarts, abs0)))
                    "type" -> curType = value
                    "default" -> pending.add(intArrayOf(abs0, abs1))
                }
            }
            flush()
        }
        items.sortBy { it.start }
        return PluginFileInfo(file, src.length, desc, params, items)
    }

    /** Apply changes ([edits]: item index -> new content). Empty/identical item = unchanged. Returns the new text. */
    fun apply(src: String, info: PluginFileInfo, edits: Map<Int, String>): String {
        val sb = StringBuilder(src)
        val order = edits.keys.filter { it in info.items.indices }.sortedByDescending { info.items[it].start }
        for (k in order) {
            val it = info.items[k]
            var nv = edits[k] ?: continue
            if (nv.isBlank() || nv == it.orig) continue
            val rep = when (it.kind) {
                PItem.KIND_CODE_STR -> escape(nv, it.quote)
                PItem.KIND_CODE_VALUE -> { nv = nv.trim(); if (!(NUMBER.matches(nv) || nv == "true" || nv == "false")) continue; nv }
                else -> nv.replace('\n', ' ').replace('\r', ' ').trim()
            }
            sb.replace(it.start, it.end, rep)
        }
        return sb.toString()
    }
}

/** plugins.js: list of plugins ($plugins) with order, enabled status, and parameters. */
class PluginEntry(val map: LinkedHashMap<String, Any?>, val isNew: Boolean = false, val sourceText: String? = null) {
    var name: String
        get() = (map["name"] as? String) ?: ""
        set(v) { map["name"] = v }
    var enabled: Boolean
        get() = map["status"] == true
        set(v) { map["status"] = v }
    val description: String get() = (map["description"] as? String) ?: ""
    @Suppress("UNCHECKED_CAST")
    val parameters: Map<String, Any?> get() = (map["parameters"] as? Map<String, Any?>) ?: emptyMap()
}

class PluginsJs(val prefix: String, val entries: MutableList<PluginEntry>, val suffix: String) {
    fun write(): String {
        val sb = StringBuilder(prefix)
        sb.append("[\n")
        for ((i, e) in entries.withIndex()) {
            sb.append(Json.write(e.map))
            if (i < entries.size - 1) sb.append(',')
            sb.append('\n')
        }
        sb.append(']')
        sb.append(suffix)
        return sb.toString()
    }

    fun indexOfName(n: String) = entries.indexOfFirst { it.name.equals(n, true) }

    companion object {
        fun parse(text: String): PluginsJs {
            val t = text.removePrefix("﻿")
            val from = t.indexOf("\$plugins").let { if (it < 0) 0 else it }
            val a = t.indexOf('[', from)
            val z = t.lastIndexOf(']')
            if (a < 0 || z < a) throw IllegalArgumentException("plugins.js")
            val root = Json.parse(t.substring(a, z + 1)) as? List<*> ?: throw IllegalArgumentException("plugins.js")
            val list = ArrayList<PluginEntry>()
            for (x in root) {
                @Suppress("UNCHECKED_CAST")
                val m = x as? LinkedHashMap<String, Any?> ?: continue
                list.add(PluginEntry(m))
            }
            return PluginsJs(t.substring(0, a), list, t.substring(z + 1))
        }

        /** New entry from a .js plugin file: name = file name, description from @plugindesc, parameters from @param + @default. */
        fun entryFromSource(fileName: String, src: String): PluginEntry {
            val name = fileName.substringAfterLast('/').removeSuffix(".js").removeSuffix(".JS")
            val info = PluginScan.scan(fileName, src)
            val params = LinkedHashMap<String, Any?>()
            val body = src.removePrefix("﻿")
            val block = Regex("/\\*:[^\\n]*[\\s\\S]*?\\*/").find(body)?.value ?: ""
            var cur = ""
            for (line in block.split('\n')) {
                val t = line.trim().trimStart('*').trim()
                if (t.startsWith("@param ") || t.startsWith("@arg ")) { cur = t.substringAfter(' ').trim(); if (cur.isNotEmpty() && !params.containsKey(cur)) params[cur] = "" }
                else if (t.startsWith("@command")) cur = ""
                else if (t.startsWith("@default") && cur.isNotEmpty()) params[cur] = t.removePrefix("@default").trim()
            }
            val m = LinkedHashMap<String, Any?>()
            m["name"] = name
            m["status"] = true
            m["description"] = info.desc
            m["parameters"] = params
            return PluginEntry(m, true, src)
        }
    }
}

/** Scan of parameters in plugins.js: non-ASCII text (not nested JSON) and number/boolean values. */
object PluginParams {
    private val NUMBER = Regex("-?\\d+(\\.\\d+)?")

    fun scan(file: String, pj: PluginsJs, textLen: Int): PluginFileInfo {
        val items = ArrayList<PItem>()
        var params = 0
        for ((i, e) in pj.entries.withIndex()) {
            for ((k, v) in e.parameters) {
                params++
                val sv = v as? String ?: continue
                val t = sv.trim()
                if (t.isEmpty()) continue
                val label = e.name + " · " + k
                if (!t.startsWith("{") && !t.startsWith("[") && PluginScan.hasText(sv)) {
                    items.add(PItem(PItem.KIND_PARAM_TEXT, label, sv, 0, 0, '\u0000', 0, i, k))
                } else if (NUMBER.matches(t) || t == "true" || t == "false") {
                    items.add(PItem(PItem.KIND_PARAM_VALUE, label, sv, 0, 0, '\u0000', 0, i, k))
                }
            }
        }
        return PluginFileInfo(file, textLen, "", params, items)
    }

    /** Apply to [pj] (modified in place). Returns the number of items changed. */
    fun apply(pj: PluginsJs, info: PluginFileInfo, edits: Map<Int, String>): Int {
        var n = 0
        for ((idx, nv0) in edits) {
            val it = info.items.getOrNull(idx) ?: continue
            val nv = nv0.trim()
            if (nv.isEmpty() || nv0 == it.orig) continue
            if (it.kind == PItem.KIND_PARAM_VALUE && !(NUMBER.matches(nv) || nv == "true" || nv == "false")) continue
            @Suppress("UNCHECKED_CAST")
            val m = pj.entries.getOrNull(it.entry)?.map?.get("parameters") as? MutableMap<String, Any?> ?: continue
            m[it.key] = if (it.kind == PItem.KIND_PARAM_TEXT) nv0 else nv
            n++
        }
        return n
    }
}

/** Plugin text export/import uses the same format as Data Translation (CSV/TXT/JSON). */
object PluginTr {
    fun pathOf(it: PItem) = if (it.entry >= 0) "plugins.js/" + it.entry + "/" + it.key else "@" + it.start + ":" + it.label

    fun export(infos: List<PluginFileInfo>, fmt: Int): String {
        val units = ArrayList<TextUnit>()
        for (f in infos) for (it in f.items) if (it.isText) units.add(TextUnit(f.file, pathOf(it), it.orig))
        return RpgmText.export(units, fmt)
    }

    /** Match translation file contents to items. Returns (file, item index) -> translation, and the number matched. */
    fun match(infos: List<PluginFileInfo>, name: String, text: String): Map<Pair<String, Int>, String> {
        val tf = RpgmText.readAny(name, text)
        val out = LinkedHashMap<Pair<String, Int>, String>()
        val byOrig = HashMap<String, String>()
        val entries = try { RpgmText.readEntriesAny(name, text).first.items } catch (e: Exception) { emptyList() }
        for (en in entries) if (en.tr.isNotBlank() && en.orig.isNotEmpty()) byOrig.putIfAbsent(en.orig, en.tr)
        for (f in infos) for ((i, it) in f.items.withIndex()) {
            if (!it.isText) continue
            val tr = tf.map[Pair(f.file, pathOf(it))]
            if (!tr.isNullOrBlank() && tr != it.orig) { out[Pair(f.file, i)] = tr; continue }
            val o = byOrig[it.orig]
            if (o != null && o != it.orig) out[Pair(f.file, i)] = o
        }
        return out
    }
}

/**
 * level 1 info, 2 warning, 3 error. [kind] = type (for the ignore feature), [maybe] = possibly intentional,
 * [code] = code description (plugins.js entry / header line) that can be opened in the card.
 */
class PluginIssue(val level: Int, val plugin: String, val text: String, val kind: String = "", val maybe: Boolean = false, val code: String = "")

object PluginCheck {
    private val DEP = Regex("""^[ \t*]*@(base|orderAfter|orderBefore)[ \t]+([^\s*]+)""", RegexOption.MULTILINE)

    /** Dependencies from annotations: type (base/orderAfter/orderBefore) -> plugin name. */
    fun depsOf(src: String): List<Pair<String, String>> =
        DEP.findAll(src).map { Pair(it.groupValues[1], it.groupValues[2]) }.toList()

    fun key(name: String) = name.substringAfterLast('/').removeSuffix(".js").removeSuffix(".JS").lowercase()

    private val MULTI = Regex("(multiple|more than once|multi[- ]?instance|several (copies|instances)|can be (used|loaded|added|installed) (multiple|more|twice)|duplicat|boleh (ganda|dipasang|dipakai)|berkali)", RegexOption.IGNORE_CASE)
    private val HDR = Regex("^[ \t]*\\*?[ \t]*@(plugindesc|author|target|base|orderAfter|orderBefore)\\b.*$", RegexOption.MULTILINE)

    private fun line(i: Int, e: PluginEntry) = "#" + (i + 1) + (if (e.enabled) "  ON   " else "  OFF  ") + e.name

    private fun headerLines(src: String?, only: Set<String>? = null): List<String> {
        if (src == null) return emptyList()
        return HDR.findAll(src.take(8000)).map { it.value.trim().trimStart('*').trim() }
            .filter { only == null || only.any { k -> it.startsWith("@$k") } }.take(8).toList()
    }

    private fun paramDiff(a: PluginEntry, b: PluginEntry, l: PluginStr2): String {
        val keys = LinkedHashSet<String>(); keys.addAll(a.parameters.keys); keys.addAll(b.parameters.keys)
        val ch = ArrayList<String>()
        for (k in keys) {
            val va = a.parameters[k]?.let { Json.write(it) }; val vb = b.parameters[k]?.let { Json.write(it) }
            if (va != vb) ch.add(k + ": " + (va ?: "-").take(40) + "  ->  " + (vb ?: "-").take(40))
        }
        if (ch.isEmpty()) return ""
        return l.snipParams + "\n" + ch.take(6).joinToString("\n") + if (ch.size > 6) "\n... +" + (ch.size - 6) else ""
    }

    /** [files]: plugin name (lowercase, without .js) -> content (may be only the beginning); null = plugin folder not available. */
    fun check(entries: List<PluginEntry>, files: Map<String, String>?, sizes: Map<String, Long>?, l: PluginStr2): List<PluginIssue> {
        val out = ArrayList<PluginIssue>()
        val seen = HashMap<String, Int>()
        val pos = HashMap<String, Int>()
        for ((i, e) in entries.withIndex()) {
            val k = key(e.name)
            val fi = seen[k]
            if (fi != null) {
                val first = entries[fi]
                val same = Json.write(first.parameters) == Json.write(e.parameters)
                val multi = files?.get(k)?.let { MULTI.find(it.take(8000)) }
                val code = StringBuilder(l.snipList).append('\n').append(line(fi, first)).append('\n').append(line(i, e))
                if (!same) code.append("\n\n").append(paramDiff(first, e, l))
                if (multi != null) code.append("\n\n").append(l.snipHeader).append('\n').append(multi.value.let { m ->
                    files[k]!!.lineSequence().firstOrNull { it.contains(m) }?.trim()?.trimStart('*')?.trim()?.take(160) ?: m
                })
                val (lv, maybe, text) = when {
                    !first.enabled || !e.enabled -> Triple(1, true, l.dupOff(fi + 1, i + 1))
                    multi != null -> Triple(1, true, l.dupMulti(fi + 1, i + 1))
                    !same -> Triple(1, true, l.dupParams(fi + 1, i + 1))
                    else -> Triple(2, false, l.dup(fi + 1, i + 1))
                }
                out.add(PluginIssue(lv, e.name, text, "dup", maybe, code.toString().trim()))
            } else { seen[k] = i; pos[k] = i }
        }
        if (files != null) {
            val unreg = ArrayList<String>()
            val reg = entries.map { key(it.name) }.toSet()
            for ((i, e) in entries.withIndex()) {
                val k = key(e.name)
                if (!files.containsKey(k)) {
                    if (e.enabled) out.add(PluginIssue(3, e.name, l.missingOn, "missing_on", false, line(i, e)))
                    else out.add(PluginIssue(1, e.name, l.missingOff, "missing_off", true, line(i, e)))
                } else if ((sizes?.get(k) ?: 1L) == 0L) out.add(PluginIssue(2, e.name, l.emptyFile, "empty", false, line(i, e)))
            }
            for (k in files.keys.sorted()) if (k !in reg) unreg.add(k)
            if (unreg.isNotEmpty()) out.add(PluginIssue(1, "(" + unreg.size + ")", l.unregGroup(unreg.size), "unreg", true, unreg.joinToString("\n") { "$it.js" }))
            for ((i, e) in entries.withIndex()) {
                if (!e.enabled) continue
                val src = files[key(e.name)] ?: continue
                for ((type, dep) in depsOf(src)) {
                    val dk = key(dep)
                    val di = pos[dk]
                    val code = (headerLines(src, setOf("base", "orderAfter", "orderBefore")).joinToString("\n") + "\n\n" + l.snipList + "\n" + line(i, e) +
                        (if (di != null) "\n" + line(di, entries[di]) else "")).trim()
                    when (type) {
                        "base" -> when {
                            di == null -> out.add(PluginIssue(3, e.name, l.baseMissing(dep), "base_missing", false, code))
                            !entries[di].enabled -> out.add(PluginIssue(3, e.name, l.baseOff(dep), "base_off", false, code))
                            di > i -> out.add(PluginIssue(3, e.name, l.baseOrder(dep), "base_order", false, code))
                        }
                        "orderAfter" -> if (di != null && di > i) out.add(PluginIssue(2, e.name, l.afterOrder(dep), "after", true, code))
                        "orderBefore" -> if (di != null && di < i) out.add(PluginIssue(2, e.name, l.beforeOrder(dep), "before", true, code))
                    }
                }
            }
        }
        return out.sortedWith(compareByDescending<PluginIssue> { it.level }.thenBy { it.plugin.lowercase() })
    }
}

/** Text for check messages (filled from UI strings so the core stays free of Android). */
interface PluginStr2 {
    fun dup(a: Int, b: Int): String
    fun dupOff(a: Int, b: Int): String
    fun dupMulti(a: Int, b: Int): String
    fun dupParams(a: Int, b: Int): String
    fun unregGroup(n: Int): String
    val snipList: String
    val snipParams: String
    val snipHeader: String
    val missingOn: String
    val missingOff: String
    val emptyFile: String
    fun baseMissing(d: String): String
    fun baseOff(d: String): String
    fun baseOrder(d: String): String
    fun afterOrder(d: String): String
    fun beforeOrder(d: String): String
}

class PluginDiffItem(val kind: Int, val name: String, val detail: String) {
    companion object { const val ADDED = 1; const val REMOVED = 2; const val STATUS = 3; const val PARAMS = 4; const val ORDER = 5 }
}

object PluginDiff {
    fun diff(a: PluginsJs, b: PluginsJs): List<PluginDiffItem> {
        val out = ArrayList<PluginDiffItem>()
        val ia = HashMap<String, Int>(); a.entries.forEachIndexed { i, e -> ia.putIfAbsent(PluginCheck.key(e.name), i) }
        val ib = HashMap<String, Int>(); b.entries.forEachIndexed { i, e -> ib.putIfAbsent(PluginCheck.key(e.name), i) }
        for ((k, j) in ib) if (k !in ia) out.add(PluginDiffItem(PluginDiffItem.ADDED, b.entries[j].name, "#" + (j + 1) + (if (b.entries[j].enabled) " ✓" else " ✗")))
        for ((k, i) in ia) if (k !in ib) out.add(PluginDiffItem(PluginDiffItem.REMOVED, a.entries[i].name, "#" + (i + 1)))
        val common = a.entries.filter { PluginCheck.key(it.name) in ib }.map { PluginCheck.key(it.name) }
        val commonB = b.entries.filter { PluginCheck.key(it.name) in ia }.map { PluginCheck.key(it.name) }
        val orderSame = common == commonB
        for (e in a.entries) {
            val k = PluginCheck.key(e.name)
            val j = ib[k] ?: continue
            val f = b.entries[j]
            if (e.enabled != f.enabled) out.add(PluginDiffItem(PluginDiffItem.STATUS, e.name, (if (e.enabled) "✓" else "✗") + " → " + (if (f.enabled) "✓" else "✗")))
            val pa = e.parameters; val pb = f.parameters
            val keys = LinkedHashSet<String>(); keys.addAll(pa.keys); keys.addAll(pb.keys)
            val ch = ArrayList<String>()
            for (kk in keys) {
                val va = pa[kk]?.let { Json.write(it) }; val vb = pb[kk]?.let { Json.write(it) }
                if (va != vb) ch.add(kk + ": " + (va ?: "∅").take(60) + " → " + (vb ?: "∅").take(60))
            }
            if (ch.isNotEmpty()) out.add(PluginDiffItem(PluginDiffItem.PARAMS, e.name, ch.take(8).joinToString("\n") + if (ch.size > 8) "\n… +" + (ch.size - 8) else ""))
        }
        if (!orderSame) {
            for ((rank, k) in common.withIndex()) {
                val rb = commonB.indexOf(k)
                if (rb != rank) out.add(PluginDiffItem(PluginDiffItem.ORDER, a.entries[ia[k]!!].name, "#" + (rank + 1) + " → #" + (rb + 1)))
            }
        }
        return out
    }
}
