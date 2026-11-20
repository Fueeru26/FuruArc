package com.furuarc.core

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import com.furuarc.storage.TreeWriter
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * File Name Optimizer: renames files/folders with special characters (non-ASCII, optionally spaces and special marks)
 * to safe names, then updates their references in the game's text files (json, js, html, ks, ...).
 * All paths are relative to the Source Folder, with "/" as the separator.
 */
object NameOpt {
    const val MAPPING = "rename_mapping.json"
    private const val MAX_TEXT = 24L * 1024 * 1024
    private val SKIP_DIRS = setOf(".git", ".gradle", "node_modules")
    val MEDIA_EXTS = setOf(
        "png", "jpg", "jpeg", "webp", "bmp", "gif", "ico", "svg",
        "ogg", "oga", "m4a", "mp3", "wav", "flac", "opus", "aac", "wma", "mid", "midi",
        "mp4", "webm", "mkv", "mov", "avi", "ogv", "m4v", "3gp", "flv", "wmv",
    )
    const val DEFAULT_TEXT_EXTS = "json,js,html,htm,css,txt,ks,tjs,rpy,xml,csv,ini,yml,yaml"
    private val BAD_FORMATS = setOf("wma", "wmv", "avi", "flv", "mid", "midi", "ape", "swf")

    class Entry(val uri: Uri, val name: String, val isDir: Boolean, val dir: String, val size: Long) {
        val rel: String get() = if (dir.isEmpty()) name else "$dir/$name"
    }

    class Opts(
        val mediaOnly: Boolean,
        val style: Int,          // 0 keep the ASCII part, 1 number only
        val folders: Boolean,
        val symbols: Boolean,
        val wide: Boolean,       // false = safe (whole name), true = broad (all occurrences)
        val caseFix: Boolean,
        val textExts: Set<String>,
    )

    class Move(val entry: Entry, val newName: String)

    class Plan(val moves: List<Move>, val tokens: Map<String, String>, val ci: Map<String, String>) {
        val files: Int get() = moves.count { !it.entry.isDir }
        val dirs: Int get() = moves.count { it.entry.isDir }
    }

    class RefFile(val entry: Entry, val rename: Int, val caseFix: Int)

    class RefScan(val files: List<RefFile>, val used: Set<String>, val skipped: List<String>, val scanned: Int) {
        val replaced: Int get() = files.sumOf { it.rename }
        val caseFixes: Int get() = files.sumOf { it.caseFix }
    }

    class Readiness(
        val files: Int, val dirs: Int, val nonAscii: Int, val symbols: Int,
        val longNames: List<String>, val caseDups: List<String>, val badFormats: Map<String, Int>,
    ) {
        val clean: Boolean get() = nonAscii == 0 && symbols == 0 && longNames.isEmpty() && caseDups.isEmpty() && badFormats.isEmpty()
    }

    class RunResult(
        val files: Int, val dirs: Int, val edited: Int, val replaced: Int, val caseFixes: Int,
        val failed: List<String>, val unreferenced: List<String>, val skipped: List<String>,
        val backedUp: Int, val error: String?,
    )

    class UndoResult(val files: Int, val dirs: Int, val edited: Int, val failed: List<String>, val error: String?)

    // ------------------------------------------------------------ listing

    fun list(ctx: Context, tree: Uri): List<Entry> {
        val out = ArrayList<Entry>()
        val proj = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
        )
        fun walk(docId: String, rel: String) {
            val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
            val subs = ArrayList<Pair<String, String>>()
            ctx.contentResolver.query(childUri, proj, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(0) ?: continue
                    val name = c.getString(1) ?: continue
                    val isDir = c.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR
                    val size = if (c.isNull(3)) 0L else c.getLong(3)
                    out.add(Entry(DocumentsContract.buildDocumentUriUsingTree(tree, id), name, isDir, rel, size))
                    if (isDir && name !in SKIP_DIRS) subs.add(Pair(id, name))
                }
            }
            for ((id, name) in subs) walk(id, if (rel.isEmpty()) name else "$rel/$name")
        }
        walk(DocumentsContract.getTreeDocumentId(tree), "")
        return out
    }

    // ------------------------------------------------------------ name checks

    private fun isAscii(s: String): Boolean = s.all { it.code < 128 }

    private fun safeChar(c: Char): Boolean = c in 'A'..'Z' || c in 'a'..'z' || c in '0'..'9' || c == '.' || c == '_' || c == '-'

    private fun hasSymbols(s: String): Boolean = isAscii(s) && s.any { !safeChar(it) }

    private fun special(name: String, o: Opts): Boolean = !isAscii(name) || (o.symbols && name.any { !safeChar(it) })

    fun extOf(name: String): String = name.substringAfterLast('.', "").lowercase()

    /** Split the base name and extension (".ext"); folders and names without a dot have no extension. */
    private fun split(name: String, isDir: Boolean): Pair<String, String> {
        if (isDir) return Pair(name, "")
        val i = name.lastIndexOf('.')
        if (i <= 0 || i >= name.length - 1) return Pair(name, "")
        val ext = name.substring(i + 1)
        if (ext.length > 12 || ext.contains(' ')) return Pair(name, "")
        return Pair(name.substring(0, i), "." + ext.map { if (safeChar(it) && it != '.') it else '_' }.joinToString(""))
    }

    private fun squash(s: String): String {
        val sb = StringBuilder()
        var pendingSep = false
        for (ch in s) {
            if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9') {
                if (pendingSep && sb.isNotEmpty()) sb.append('_')
                pendingSep = false
                sb.append(ch)
            } else {
                pendingSep = true
            }
        }
        return sb.toString().take(40)
    }

    /** ASCII name for a base name that is already ASCII but has special marks: marks are replaced with "_", dots and dashes are kept. */
    private fun sanitizeAscii(s: String): String {
        val sb = StringBuilder()
        for (ch in s) sb.append(if (safeChar(ch)) ch else '_')
        return sb.toString().replace(Regex("_+"), "_").trim('_').ifEmpty { "file" }
    }

    // ------------------------------------------------------------ plan

    fun buildPlan(entries: List<Entry>, o: Opts): Plan {
        val cands = entries.filter { e ->
            when {
                e.dir.isEmpty() && e.name == MAPPING -> false
                e.isDir -> o.folders && special(e.name, o)
                else -> special(e.name, o) && (!o.mediaOnly || extOf(e.name) in MEDIA_EXTS)
            }
        }
        val candSet = java.util.Collections.newSetFromMap(java.util.IdentityHashMap<Entry, Boolean>())
        candSet.addAll(cands)
        val used = HashMap<String, HashSet<String>>()
        for (e in entries) if (e !in candSet) used.getOrPut(e.dir) { HashSet() }.add(e.name.lowercase())

        val ordered = cands.sortedBy { it.rel.lowercase() }
        val usages = LinkedHashMap<String, ArrayList<Pair<String, String>>>() // base name -> (parent folder, extension)
        for (e in ordered) {
            val (b, x) = split(e.name, e.isDir)
            usages.getOrPut(b) { ArrayList() }.add(Pair(e.dir, x))
        }

        val tokens = HashMap<String, String>()
        val baseNew = HashMap<String, String>()
        var counter = 1
        for ((b, us) in usages) {
            var attempt = 0
            var chosen: String
            while (true) {
                chosen = if (o.style == 1) {
                    "file_%03d".format(counter)
                } else if (isAscii(b)) {
                    val s = sanitizeAscii(b)
                    if (attempt == 0) s else "${s}_${attempt + 1}"
                } else {
                    val r = squash(b)
                    if (r.isEmpty()) "file_%03d".format(counter) else "${r}_%03d".format(counter)
                }
                val ok = us.all { (p, x) -> (chosen + x).lowercase() !in (used[p] ?: emptySet<String>()) }
                if (ok) break
                if (o.style == 1 || !isAscii(b)) counter++ else attempt++
            }
            if (o.style == 1 || !isAscii(b)) counter++
            baseNew[b] = chosen
            for ((p, x) in us) used.getOrPut(p) { HashSet() }.add((chosen + x).lowercase())
        }

        val moves = ArrayList<Move>()
        for (e in ordered) {
            val (b, x) = split(e.name, e.isDir)
            val nn = baseNew[b]!! + x
            moves.add(Move(e, nn))
            tokens.putIfAbsent(e.name, nn)
            if (x.isNotEmpty()) tokens.putIfAbsent(b, baseNew[b]!!)
        }

        val ci = HashMap<String, String>()
        if (o.caseFix) {
            val moved = java.util.IdentityHashMap<Entry, String>()
            for (m in moves) moved[m.entry] = m.newName
            fun put(key: String, actual: String) {
                val k = key.lowercase()
                val cur = ci[k]
                if (cur == null) ci[k] = actual else if (cur != actual) ci[k] = ""
            }
            for (e in entries) {
                val fin = moved[e] ?: e.name
                put(fin, fin)
                val (b, x) = split(fin, e.isDir)
                if (x.isNotEmpty()) put(b, b)
            }
        }
        return Plan(moves, tokens, ci)
    }

    // ------------------------------------------------------------ readiness report

    fun readiness(entries: List<Entry>): Readiness {
        var nonAscii = 0
        var symbols = 0
        val longN = ArrayList<String>()
        val bad = HashMap<String, Int>()
        val groups = HashMap<String, ArrayList<String>>()
        for (e in entries) {
            if (!isAscii(e.name)) nonAscii++ else if (hasSymbols(e.name)) symbols++
            if (e.name.length > 100 || e.rel.length > 200) longN.add(e.rel)
            groups.getOrPut(e.dir.lowercase() + "/" + e.name.lowercase()) { ArrayList() }.add(e.rel)
            if (!e.isDir) {
                val x = extOf(e.name)
                if (x in BAD_FORMATS) bad[x] = (bad[x] ?: 0) + 1
            }
        }
        val dups = groups.values.filter { it.size > 1 }.flatten()
        return Readiness(entries.count { !it.isDir }, entries.count { it.isDir }, nonAscii, symbols, longN, dups, bad)
    }

    // ------------------------------------------------------------ text replacement

    class Doc(val text: String, val enc: Int) // 0 UTF-8, 1 UTF-8 BOM, 2 UTF-16LE BOM, 3 UTF-16BE BOM

    fun decode(b: ByteArray): Doc? {
        fun strict(off: Int): String? = try {
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(b, off, b.size - off)).toString()
        } catch (e: CharacterCodingException) {
            null
        }
        if (b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte()) return strict(3)?.let { Doc(it, 1) }
        if (b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte()) return Doc(String(b, 2, b.size - 2, Charsets.UTF_16LE), 2)
        if (b.size >= 2 && b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte()) return Doc(String(b, 2, b.size - 2, Charsets.UTF_16BE), 3)
        for (i in 0 until minOf(b.size, 4096)) if (b[i] == 0.toByte()) return null // binary
        return strict(0)?.let { Doc(it, 0) }
    }

    fun encode(d: Doc): ByteArray = when (d.enc) {
        1 -> byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + d.text.toByteArray(Charsets.UTF_8)
        2 -> byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + d.text.toByteArray(Charsets.UTF_16LE)
        3 -> byteArrayOf(0xFE.toByte(), 0xFF.toByte()) + d.text.toByteArray(Charsets.UTF_16BE)
        else -> d.text.toByteArray(Charsets.UTF_8)
    }

    private fun isDelim(c: Char): Boolean = when (c) {
        '"', '\'', '/', '=', ',', ';', '(', ')', '[', ']', '{', '}', '<', '>', '|', '\n', '\r', '\t', '`' -> true
        else -> false
    }

    private fun hex(c: Char): Boolean = c in '0'..'9' || c in 'a'..'f' || c in 'A'..'F'

    private fun hexVal(c: Char): Int = Character.digit(c, 16)

    /** Decode \uXXXX and %XX (UTF-8) so names written in escaped form are still recognized. */
    private fun decodeEsc(s: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < s.length) {
            val c = s[i]
            if (c == '\\' && i + 5 < s.length + 0 && s[i + 1] == 'u' && hex(s[i + 2]) && hex(s[i + 3]) && hex(s[i + 4]) && hex(s[i + 5])) {
                sb.append(((hexVal(s[i + 2]) shl 12) or (hexVal(s[i + 3]) shl 8) or (hexVal(s[i + 4]) shl 4) or hexVal(s[i + 5])).toChar())
                i += 6
            } else if (c == '%' && i + 2 < s.length + 0 && hex(s[i + 1]) && hex(s[i + 2])) {
                val bytes = java.io.ByteArrayOutputStream()
                while (i + 2 < s.length + 0 && s[i] == '%' && hex(s[i + 1]) && hex(s[i + 2])) {
                    bytes.write((hexVal(s[i + 1]) shl 4) or hexVal(s[i + 2]))
                    i += 3
                }
                sb.append(String(bytes.toByteArray(), Charsets.UTF_8))
            } else {
                sb.append(c)
                i++
            }
        }
        return sb.toString()
    }

    class Counts {
        var rename = 0
        var caseFix = 0
        val used = HashSet<String>()
    }

    /** Safe mode: only segments that fully equal the name (between quotes, slashes, etc.) are replaced. */
    private fun transformSafe(text: String, tokens: Map<String, String>, ci: Map<String, String>?, c: Counts, maxKey: Int): String? {
        val n = text.length
        var sb: StringBuilder? = null
        var last = 0
        var i = 0
        while (i < n) {
            val s = i
            var j = i
            var esc = false
            while (j < n) {
                val ch = text[j]
                if (ch == '\\') {
                    if (j + 5 < n && text[j + 1] == 'u' && hex(text[j + 2]) && hex(text[j + 3]) && hex(text[j + 4]) && hex(text[j + 5])) {
                        esc = true
                        j += 6
                        continue
                    }
                    break
                }
                if (isDelim(ch)) break
                j++
            }
            if (j > s && j - s <= maxKey * 6 + 12) {
                var a = s
                var b = j
                while (a < b && text[a] == ' ') a++
                while (b > a && text[b - 1] == ' ') b--
                if (b > a) {
                    val raw = text.substring(a, b)
                    val key = if (esc || raw.indexOf('%') >= 0) decodeEsc(raw) else raw
                    var repl: String? = tokens[key]
                    var isCase = false
                    if (repl != null) {
                        c.used.add(key)
                    } else if (ci != null) {
                        val act = ci[key.lowercase()]
                        if (!act.isNullOrEmpty() && act != key) {
                            repl = act
                            isCase = true
                        }
                    }
                    if (repl != null) {
                        if (sb == null) sb = StringBuilder(n + 64)
                        sb.append(text, last, a).append(repl)
                        last = b
                        if (isCase) c.caseFix++ else c.rename++
                    }
                }
            }
            i = if (j < n) j + 1 else j
        }
        if (sb == null) return null
        sb.append(text, last, n)
        return sb.toString()
    }

    private fun escapedForms(key: String): List<String> {
        if (isAscii(key)) return emptyList()
        val lo = StringBuilder()
        val up = StringBuilder()
        for (ch in key) {
            if (ch.code < 128) { lo.append(ch); up.append(ch) } else {
                lo.append("\\u").append("%04x".format(ch.code))
                up.append("\\u").append("%04X".format(ch.code))
            }
        }
        return listOf(lo.toString(), up.toString())
    }

    class Wide(val pattern: Pattern?, val repl: Map<String, String>)

    fun buildWide(tokens: Map<String, String>): Wide {
        val repl = HashMap<String, String>()
        for ((k, v) in tokens) {
            if (k.length < 2) continue // a single character is too dangerous (e.g. "あ")
            repl[k] = v
            for (f in escapedForms(k)) repl[f] = v
        }
        if (repl.isEmpty()) return Wide(null, repl)
        val keys = repl.keys.sortedByDescending { it.length }
        return Wide(Pattern.compile(keys.joinToString("|") { Pattern.quote(it) }), repl)
    }

    private fun transformWide(text: String, w: Wide, c: Counts): String? {
        val p = w.pattern ?: return null
        val m = p.matcher(text)
        var sb: StringBuffer? = null
        while (m.find()) {
            if (sb == null) sb = StringBuffer()
            val g = m.group()
            m.appendReplacement(sb, Matcher.quoteReplacement(w.repl[g] ?: g))
            c.rename++
            c.used.add(g)
        }
        if (sb == null) return null
        m.appendTail(sb)
        return sb.toString()
    }

    class Replacer(val tokens: Map<String, String>, val ci: Map<String, String>?, val wide: Wide?) {
        private val maxKey = (tokens.keys.maxOfOrNull { it.length } ?: 1).coerceAtLeast(ci?.keys?.maxOfOrNull { it.length } ?: 1)
        fun apply(text: String, c: Counts): String? =
            if (wide != null) transformWide(text, wide, c) else transformSafe(text, tokens, ci, c, maxKey)
    }

    fun replacerFor(plan: Plan, o: Opts): Replacer =
        Replacer(plan.tokens, if (o.caseFix && !o.wide) plan.ci else null, if (o.wide) buildWide(plan.tokens) else null)

    private fun textCandidates(entries: List<Entry>, o: Opts): List<Entry> = entries.filter {
        !it.isDir && it.size <= MAX_TEXT && extOf(it.name) in o.textExts && !(it.dir.isEmpty() && it.name == MAPPING)
    }

    private fun readAll(ctx: Context, uri: Uri): ByteArray =
        ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: throw IOException("Tidak bisa membaca")

    /** Scan text files without changing anything: count replacements per file. */
    fun scanRefs(ctx: Context, entries: List<Entry>, plan: Plan, o: Opts, progress: (Int, Int) -> Unit): RefScan {
        val rep = replacerFor(plan, o)
        if (plan.tokens.isEmpty() && rep.ci == null) return RefScan(emptyList(), emptySet(), emptyList(), 0)
        val cands = textCandidates(entries, o)
        val files = ArrayList<RefFile>()
        val skipped = ArrayList<String>()
        val used = HashSet<String>()
        for ((i, e) in cands.withIndex()) {
            progress(i, cands.size)
            try {
                val doc = decode(readAll(ctx, e.uri))
                if (doc == null) {
                    skipped.add(e.rel)
                    continue
                }
                val c = Counts()
                val out = rep.apply(doc.text, c)
                used.addAll(c.used)
                if (out != null && (c.rename > 0 || c.caseFix > 0)) files.add(RefFile(e, c.rename, c.caseFix))
            } catch (ex: Exception) {
                skipped.add(e.rel)
            }
        }
        progress(cands.size, cands.size)
        return RefScan(files, used, skipped, cands.size)
    }

    // ------------------------------------------------------------ execute

    private fun readMapping(tw: TreeWriter): JSONObject? = try {
        tw.readExisting(MAPPING)?.let { JSONObject(String(it, Charsets.UTF_8)) }
    } catch (e: Exception) {
        null
    }

    private fun writeMapping(tw: TreeWriter, obj: JSONObject) {
        val of = tw.open(MAPPING, ConflictPolicy.OVERWRITE) ?: throw IOException("rename_mapping.json")
        of.stream.use { it.write(obj.toString(2).toByteArray(Charsets.UTF_8)) }
    }

    fun hasMapping(entries: List<Entry>): Boolean = entries.any { it.dir.isEmpty() && it.name == MAPPING && !it.isDir }

    /** Apply the plan. [backup] (optional) receives copies of the original text files (original paths) and rename_mapping.json. */
    fun run(
        ctx: Context, tree: Uri, backup: Uri?, plan: Plan, scan: RefScan, o: Opts,
        progress: (phase: Int, done: Int, total: Int) -> Unit,
    ): RunResult {
        val resolver = ctx.contentResolver
        val failed = ArrayList<String>()
        val bw = if (backup != null) TreeWriter(ctx, backup) else null

        // final path of each entry (parent folder names change too)
        val dirNew = HashMap<String, String>()
        val newNameOf = java.util.IdentityHashMap<Entry, String>()
        for (m in plan.moves) {
            newNameOf[m.entry] = m.newName
            if (m.entry.isDir) dirNew[m.entry.rel] = m.newName
        }
        fun finalDir(rel: String): String {
            if (rel.isEmpty()) return ""
            val out = ArrayList<String>()
            var acc = ""
            for (p in rel.split('/')) {
                acc = if (acc.isEmpty()) p else "$acc/$p"
                out.add(dirNew[acc] ?: p)
            }
            return out.joinToString("/")
        }
        fun finalRel(e: Entry): String {
            val d = finalDir(e.dir)
            val n = newNameOf[e] ?: e.name
            return if (d.isEmpty()) n else "$d/$n"
        }

        // 1) write the mapping first (so it can be restored even if the process stops)
        val run = JSONObject()
        run.put("time", System.currentTimeMillis())
        run.put("wide", o.wide)
        val mv = JSONArray()
        for (m in plan.moves) mv.put(JSONObject().put("from", m.entry.rel).put("to", finalRel(m.entry)).put("dir", m.entry.isDir))
        run.put("moves", mv)
        val ed = JSONArray()
        for (f in scan.files) ed.put(finalRel(f.entry))
        run.put("edited", ed)
        var mapObj: JSONObject
        try {
            val tw0 = TreeWriter(ctx, tree)
            mapObj = readMapping(tw0) ?: JSONObject().put("version", 1).put("runs", JSONArray())
            if (!mapObj.has("runs")) mapObj.put("runs", JSONArray())
            mapObj.getJSONArray("runs").put(run)
            writeMapping(tw0, mapObj)
            bw?.let { writeMapping(it, mapObj) }
        } catch (e: Exception) {
            return RunResult(0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList(), 0, e.message ?: "mapping")
        }

        // 2) edit references in text files (back up the originals first if the Backup Folder is set)
        val rep = replacerFor(plan, o)
        var edited = 0
        var replaced = 0
        var caseFixes = 0
        var backedUp = 0
        for ((i, f) in scan.files.withIndex()) {
            progress(0, i, scan.files.size)
            try {
                val raw = readAll(ctx, f.entry.uri)
                val doc = decode(raw) ?: continue
                val c = Counts()
                val out = rep.apply(doc.text, c) ?: continue
                if (bw != null) {
                    val of = bw.open(f.entry.rel, ConflictPolicy.OVERWRITE) ?: throw IOException("cadangan")
                    of.stream.use { it.write(raw) }
                    backedUp++
                }
                resolver.openOutputStream(f.entry.uri, "wt")?.use { it.write(encode(Doc(out, doc.enc))) } ?: throw IOException("tulis")
                edited++
                replaced += c.rename
                caseFixes += c.caseFix
            } catch (e: Exception) {
                failed.add(f.entry.rel + " (" + (e.message ?: "?") + ")")
            }
        }
        progress(0, scan.files.size, scan.files.size)

        // 3) rename files, 4) rename folders from the deepest
        val fileMoves = plan.moves.filter { !it.entry.isDir }
        var fileOk = 0
        for ((i, m) in fileMoves.withIndex()) {
            progress(1, i, fileMoves.size)
            try {
                if (DocumentsContract.renameDocument(resolver, m.entry.uri, m.newName) != null) fileOk++ else failed.add(m.entry.rel)
            } catch (e: Exception) {
                failed.add(m.entry.rel + " (" + (e.message ?: "?") + ")")
            }
        }
        progress(1, fileMoves.size, fileMoves.size)
        val dirMoves = plan.moves.filter { it.entry.isDir }.sortedByDescending { it.entry.rel.count { ch -> ch == '/' } }
        var dirOk = 0
        for ((i, m) in dirMoves.withIndex()) {
            progress(2, i, dirMoves.size)
            try {
                if (DocumentsContract.renameDocument(resolver, m.entry.uri, m.newName) != null) dirOk++ else failed.add(m.entry.rel)
            } catch (e: Exception) {
                failed.add(m.entry.rel + " (" + (e.message ?: "?") + ")")
            }
        }
        progress(2, dirMoves.size, dirMoves.size)

        val unref = ArrayList<String>()
        if (scan.scanned > 0) {
            for (m in fileMoves) {
                val (b, _) = split(m.entry.name, false)
                if (m.entry.name !in scan.used && b !in scan.used) unref.add(m.entry.rel)
            }
        }
        return RunResult(fileOk, dirOk, edited, replaced, caseFixes, failed, unref, scan.skipped, backedUp, null)
    }

    // ------------------------------------------------------------ restore

    /** Undo the last run in rename_mapping.json: references are restored, then file and folder names. */
    fun undo(ctx: Context, tree: Uri, progress: (phase: Int, done: Int, total: Int) -> Unit): UndoResult {
        val resolver = ctx.contentResolver
        val tw = TreeWriter(ctx, tree)
        val mapObj = readMapping(tw) ?: return UndoResult(0, 0, 0, emptyList(), "rename_mapping.json")
        val runs = mapObj.optJSONArray("runs")
        if (runs == null || runs.length() == 0) return UndoResult(0, 0, 0, emptyList(), "rename_mapping.json")
        val run = runs.getJSONObject(runs.length() - 1)
        val moves = run.getJSONArray("moves")
        val edited = run.optJSONArray("edited") ?: JSONArray()
        val wide = run.optBoolean("wide", false)

        val byRel = HashMap<String, Entry>()
        for (e in list(ctx, tree)) byRel[e.rel] = e
        val failed = ArrayList<String>()

        fun nameOf(p: String) = p.substringAfterLast('/')
        val rev = HashMap<String, String>()
        for (i in 0 until moves.length()) {
            val m = moves.getJSONObject(i)
            val from = nameOf(m.getString("from"))
            val to = nameOf(m.getString("to"))
            val isDir = m.optBoolean("dir", false)
            rev.putIfAbsent(to, from)
            val (tb, tx) = split(to, isDir)
            val (fb, _) = split(from, isDir)
            if (tx.isNotEmpty()) rev.putIfAbsent(tb, fb)
        }

        // 1) restore references in the files that were edited earlier
        val rep = Replacer(rev, null, if (wide) buildWide(rev) else null)
        var restored = 0
        for (i in 0 until edited.length()) {
            progress(0, i, edited.length())
            val e = byRel[edited.getString(i)] ?: continue
            try {
                val doc = decode(readAll(ctx, e.uri)) ?: continue
                val out = rep.apply(doc.text, Counts()) ?: continue
                resolver.openOutputStream(e.uri, "wt")?.use { it.write(encode(Doc(out, doc.enc))) } ?: throw IOException("tulis")
                restored++
            } catch (ex: Exception) {
                failed.add(e.rel + " (" + (ex.message ?: "?") + ")")
            }
        }

        // 2) file names, 3) folder names (deepest first)
        var fileOk = 0
        var dirOk = 0
        val fl = ArrayList<JSONObject>()
        val dl = ArrayList<JSONObject>()
        for (i in 0 until moves.length()) {
            val m = moves.getJSONObject(i)
            if (m.optBoolean("dir", false)) dl.add(m) else fl.add(m)
        }
        for ((i, m) in fl.withIndex()) {
            progress(1, i, fl.size)
            val e = byRel[m.getString("to")] ?: continue
            try {
                if (DocumentsContract.renameDocument(resolver, e.uri, nameOf(m.getString("from"))) != null) fileOk++ else failed.add(e.rel)
            } catch (ex: Exception) {
                failed.add(e.rel + " (" + (ex.message ?: "?") + ")")
            }
        }
        dl.sortByDescending { it.getString("to").count { ch -> ch == '/' } }
        for ((i, m) in dl.withIndex()) {
            progress(2, i, dl.size)
            val e = byRel[m.getString("to")] ?: continue
            try {
                if (DocumentsContract.renameDocument(resolver, e.uri, nameOf(m.getString("from"))) != null) dirOk++ else failed.add(e.rel)
            } catch (ex: Exception) {
                failed.add(e.rel + " (" + (ex.message ?: "?") + ")")
            }
        }

        // 4) remove this run from the mapping
        try {
            val nr = JSONArray()
            for (i in 0 until runs.length() - 1) nr.put(runs.getJSONObject(i))
            mapObj.put("runs", nr)
            writeMapping(TreeWriter(ctx, tree), mapObj)
        } catch (e: Exception) {
            failed.add(MAPPING)
        }
        return UndoResult(fileOk, dirOk, restored, failed, null)
    }
}
