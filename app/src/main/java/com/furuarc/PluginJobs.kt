package com.furuarc

import android.net.Uri
import com.furuarc.core.*
import com.furuarc.i18n.PluginStr
import com.furuarc.settings.Lang
import com.furuarc.storage.TreeWriter
import com.furuarc.storage.listTree
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val pstr: PluginStr get() = PluginStr.of(Engine.lang)

private fun treeName(u: Uri): String = android.provider.DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }

private fun readText(u: Uri): String =
    Engine.app.contentResolver.openInputStream(u)?.use { String(it.readBytes(), Charsets.UTF_8) } ?: throw java.io.IOException("Tidak bisa membuka file")

/** Pick plugin files from the listTree result: js/plugins/, plugins/, or all .js (excluding plugins.js) if the plugin folder is chosen directly. */
private fun pluginFilesOf(all: List<com.furuarc.core.PackInput>): List<com.furuarc.core.PackInput> {
    val js = all.filter { it.path.endsWith(".js", true) }
    val low = js.map { it.path.lowercase() }
    val a = js.filterIndexed { i, _ -> low[i].startsWith("js/plugins/") }
    if (a.isNotEmpty()) return a
    val b = js.filterIndexed { i, _ -> low[i].startsWith("plugins/") }
    if (b.isNotEmpty()) return b
    return js.filter { !it.path.equals("plugins.js", true) }
}

private fun readHead(f: com.furuarc.core.PackInput, max: Int = 64 * 1024): String =
    f.open().use { ins ->
        val buf = ByteArray(max)
        var n = 0
        while (n < max) { val r = ins.read(buf, n, max - n); if (r < 0) break; n += r }
        String(buf, 0, n, Charsets.UTF_8)
    }

/** Back up then write [data] to [path] in the output folder. Returns true if an existing file was backed up. */
private fun writeWithBackup(tw: TreeWriter, path: String, data: ByteArray): Boolean {
    val old = tw.readExisting(path)
    var bak = false
    if (old != null) {
        tw.open("$path.bak")?.stream?.use { it.write(old) }
        bak = true
    }
    tw.open(path)!!.stream.use { it.write(data) }
    return bak
}

// ============================ Plugin Scanner ============================

data class PluginScanState(
    val folder: String? = null,
    val jsName: String? = null,
    val busy: Boolean = false,
    val files: List<PluginFileInfo> = emptyList(),
    val scanned: Boolean = false,
    val outName: String? = null,
    val applying: Boolean = false,
    val message: String? = null,
    val error: String? = null,
    /** Result of translation export/import: (text, success?). */
    val trNote: Pair<String, Boolean>? = null,
    val trBusy: Boolean = false,
)

/** Default output folder from Settings if still accessible: (uri, name). */
internal fun defaultOutFolder(): Pair<Uri, String>? {
    val u = Engine.defaultOut ?: return null
    val name = try {
        androidx.documentfile.provider.DocumentFile.fromTreeUri(Engine.app, u)?.takeIf { it.exists() }?.name
    } catch (e: Exception) { null } ?: return null
    return u to name
}

object PluginScanJobs {
    private val _state = MutableStateFlow(PluginScanState())
    val state: StateFlow<PluginScanState> = _state.asStateFlow()
    private var job: Job? = null
    private var tree: Uri? = null
    private var out: Uri? = null
    private val sources = HashMap<String, String>()
    private var codeInfos: List<PluginFileInfo> = emptyList()
    private var pj: PluginsJs? = null
    private var pjLen = 0
    private var autoPj = false
    private const val MAX_BYTES = 6 * 1024 * 1024
    const val PLUGINS_JS = "plugins.js"

    fun clear() { job?.cancel(); sources.clear(); codeInfos = emptyList(); pj = null; autoPj = false; tree = null; out = null; _state.value = PluginScanState() }

    fun pickFolder(u: Uri) {
        tree = u
        sources.clear(); codeInfos = emptyList()
        if (autoPj) { pj = null; autoPj = false }
        _state.update { PluginScanState(folder = treeName(u), jsName = if (autoPj || pj == null) null else it.jsName, outName = it.outName) }
    }

    fun pickPluginsJs(u: Uri) {
        try {
            val text = readText(u)
            pj = PluginsJs.parse(text); pjLen = text.length; autoPj = false
            _state.update { it.copy(jsName = Engine.displayName(u), error = null, message = null) }
            rebuild()
        } catch (e: Exception) {
            _state.update { it.copy(error = pstr.badJs, message = null) }
        }
    }

    fun setOut(u: Uri) {
        out = u
        _state.update { it.copy(outName = treeName(u), message = null) }
    }

    /** Use the default output folder (Settings) if no output folder has been chosen. */
    fun useDefaultOut() {
        if (out != null) return
        val d = defaultOutFolder() ?: return
        out = d.first
        _state.update { it.copy(outName = d.second) }
    }

    fun dismissTr() { _state.update { it.copy(trNote = null) } }

    private fun rebuild() {
        val st = _state.value
        if (!st.scanned && codeInfos.isEmpty()) return
        val all = ArrayList<PluginFileInfo>()
        pj?.let { all.add(PluginParams.scan(PLUGINS_JS, it, pjLen)) }
        all.addAll(codeInfos)
        _state.update { it.copy(files = all) }
    }

    fun scan() {
        val t = tree ?: return
        job?.cancel()
        _state.update { it.copy(busy = true, files = emptyList(), scanned = false, error = null, message = null) }
        job = Engine.scope.launch {
            try {
                val all = listTree(Engine.app, t)
                val use = pluginFilesOf(all)
                // automatic plugins.js from the game/js folder if not chosen manually
                if (pj == null) {
                    val cand = all.firstOrNull { it.path.equals("js/plugins.js", true) } ?: all.firstOrNull { it.path.equals("plugins.js", true) }
                    if (cand != null) {
                        try {
                            val text = cand.open().use { String(it.readBytes(), Charsets.UTF_8) }
                            pj = PluginsJs.parse(text); pjLen = text.length; autoPj = true
                            _state.update { it.copy(jsName = cand.path) }
                        } catch (e: Exception) { /* ignore; invalid plugins.js */ }
                    }
                }
                val infos = ArrayList<PluginFileInfo>()
                sources.clear()
                for (f in use) {
                    if (f.size > MAX_BYTES) continue
                    val text = f.open().use { String(it.readBytes(), Charsets.UTF_8) }
                    infos.add(PluginScan.scan(f.path, text))
                    sources[f.path] = text
                }
                infos.sortBy { it.file.lowercase() }
                codeInfos = infos
                val merged = ArrayList<PluginFileInfo>()
                pj?.let { merged.add(PluginParams.scan(PLUGINS_JS, it, pjLen)) }
                merged.addAll(infos)
                _state.update { it.copy(busy = false, files = merged, scanned = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    /** [edits]: file name -> (item index -> new content). Only changed files are written; old files are backed up as .bak. */
    fun apply(edits: Map<String, Map<Int, String>>) {
        val o = out
        val s = pstr
        if (o == null) { _state.update { it.copy(error = s.needOut, message = null) }; return }
        if (edits.values.all { m -> m.values.all { it.isBlank() } }) { _state.update { it.copy(error = s.nothingChanged, message = null) }; return }
        job?.cancel()
        _state.update { it.copy(applying = true, error = null, message = null) }
        job = Engine.scope.launch {
            try {
                val tw = TreeWriter(Engine.app, o)
                var files = 0; var items = 0
                val skipped = ArrayList<String>()
                for (info in _state.value.files) {
                    val m = edits[info.file] ?: continue
                    if (info.file == PLUGINS_JS) {
                        val cur = pj ?: continue
                        // copy so changes do not accumulate if Apply is pressed again
                        val copy = PluginsJs.parse(cur.write())
                        val n = PluginParams.apply(copy, info, m)
                        if (n > 0) {
                            writeWithBackup(tw, PLUGINS_JS, copy.write().toByteArray(Charsets.UTF_8))
                            files++; items += n
                        }
                        continue
                    }
                    val src = sources[info.file] ?: continue
                    val valid = HashMap<Int, String>()
                    for ((i, v) in m) {
                        val it = info.items.getOrNull(i) ?: continue
                        if (v.isBlank() || v == it.orig) continue
                        if (it.kind == PItem.KIND_CODE_VALUE) {
                            val tv = v.trim()
                            if (!(Regex("-?\\d+(\\.\\d+)?").matches(tv) || tv == "true" || tv == "false")) { skipped.add(s.badValue(it.label)); continue }
                        }
                        valid[i] = v
                    }
                    if (valid.isEmpty()) continue
                    val text = PluginScan.apply(src, info, valid)
                    if (text == src) continue
                    writeWithBackup(tw, info.file, text.toByteArray(Charsets.UTF_8))
                    files++; items += valid.size
                }
                val msg = if (files == 0 && skipped.isNotEmpty()) skipped.joinToString("\n") else
                    (if (files == 0) s.nothingChanged else s.applied(files, items)) + (if (skipped.isNotEmpty()) "\n" + skipped.joinToString("\n") else "")
                _state.update { it.copy(applying = false, message = msg) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(applying = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    /** Export all scanned texts to the output folder (CSV/TXT/JSON, same as Data Translation). */
    fun exportTr(fmt: Int) {
        val o = out
        val s = pstr
        if (o == null) { _state.update { it.copy(trNote = s.needOut to false) }; return }
        val infos = _state.value.files
        val n = infos.sumOf { f -> f.items.count { it.isText } }
        if (n == 0) { _state.update { it.copy(trNote = s.trNoText to false) }; return }
        if (_state.value.trBusy) return
        _state.update { it.copy(trBusy = true, trNote = null) }
        Engine.scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val name = "plugin_teks." + RpgmText.extFor(fmt)
                val data = PluginTr.export(infos, fmt).toByteArray(Charsets.UTF_8)
                TreeWriter(Engine.app, o).open(name)!!.stream.use { it.write(data) }
                _state.update { it.copy(trBusy = false, trNote = s.trExported(n, name) to true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(trBusy = false, trNote = (s.trFailed(e.message ?: e.javaClass.simpleName)) to false) }
            }
        }
    }

    /** Read a translation file; returns (file, item index) -> translation to fill into the editor column. */
    fun importTr(u: Uri): Map<Pair<String, Int>, String> {
        val s = pstr
        _state.update { it.copy(trBusy = true, trNote = null) }
        return try {
            val r = PluginTr.match(_state.value.files, Engine.displayName(u), readText(u))
            _state.update { it.copy(trBusy = false, trNote = if (r.isEmpty()) s.trNoMatch to false else s.trImported(r.size) to true) }
            r
        } catch (e: Throwable) {
            _state.update { it.copy(trBusy = false, trNote = s.trFailed(e.message ?: e.javaClass.simpleName) to false) }
            emptyMap()
        }
    }
}

// ============================ Plugin.js Editor ============================

data class PluginListState(
    val name: String? = null,
    val entries: List<PluginEntry> = emptyList(),
    val rev: Int = 0,               // increments on every list/status change
    val pendingName: String? = null,
    val outName: String? = null,
    val folderName: String? = null,
    val issues: List<PluginIssue> = emptyList(),
    val dirty: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

object PluginListJobs {
    private val _state = MutableStateFlow(PluginListState())
    val state: StateFlow<PluginListState> = _state.asStateFlow()
    private var js: PluginsJs? = null
    private var pending: PluginEntry? = null
    private var out: Uri? = null
    private var files: Map<String, String>? = null
    private var sizes: Map<String, Long>? = null

    fun clear() { js = null; pending = null; out = null; files = null; sizes = null; _state.value = PluginListState() }

    /** Use the default output folder (Settings) if no output folder has been chosen. */
    fun useDefaultOut() {
        if (out != null) return
        val d = defaultOutFolder() ?: return
        out = d.first
        _state.update { it.copy(outName = d.second) }
    }

    private fun recheck(): List<PluginIssue> {
        val p = js ?: return emptyList()
        // new plugin not yet in the folder: include its source so its dependencies are checked too
        var f = files
        var sz = sizes
        if (f != null) {
            val extra = p.entries.filter { it.sourceText != null }
            if (extra.isNotEmpty()) {
                val m = HashMap(f); val z = HashMap(sz ?: emptyMap())
                for (e in extra) { m[PluginCheck.key(e.name)] = e.sourceText!!; z[PluginCheck.key(e.name)] = e.sourceText.length.toLong() }
                f = m; sz = z
            }
        }
        return PluginIgnore.filter(PluginCheck.check(p.entries, f, sz, pstr))
    }

    private fun refresh(dirty: Boolean, msg: String? = null, err: String? = null) {
        val p = js ?: return
        val issues = recheck()
        _state.update { it.copy(entries = p.entries.toList(), rev = it.rev + 1, issues = issues, dirty = dirty || it.dirty, message = msg, error = err) }
    }

    fun openPluginsJs(u: Uri) {
        val s = pstr
        try {
            val p = try { PluginsJs.parse(readText(u)) } catch (e: Exception) { _state.update { it.copy(error = s.badJs, message = null) }; return }
            js = p
            _state.update { it.copy(name = Engine.displayName(u), dirty = false) }
            refresh(false)
        } catch (e: Throwable) {
            _state.update { it.copy(error = e.message ?: s.badJs, message = null) }
        }
    }

    fun pickFolder(u: Uri) {
        _state.update { it.copy(busy = true, folderName = treeName(u), error = null, message = null) }
        Engine.scope.launch {
            try {
                val use = pluginFilesOf(listTree(Engine.app, u))
                val m = HashMap<String, String>(); val z = HashMap<String, Long>()
                for (f in use) { val k = PluginCheck.key(f.path); m[k] = readHead(f); z[k] = f.size }
                files = m; sizes = z
                _state.update { it.copy(busy = false) }
                refresh(false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    fun pickNewPlugin(u: Uri) {
        try {
            val name = Engine.displayName(u)
            pending = PluginsJs.entryFromSource(name, readText(u))
            _state.update { it.copy(pendingName = pending?.name, error = null, message = null) }
        } catch (e: Throwable) {
            _state.update { it.copy(error = e.message ?: "?", message = null) }
        }
    }

    fun addPending() {
        val s = pstr
        val p = js ?: run { _state.update { it.copy(error = s.needJs, message = null) }; return }
        val e = pending ?: return
        if (p.indexOfName(e.name) >= 0) { _state.update { it.copy(error = s.dup(e.name), message = null) }; return }
        p.entries.add(e)
        pending = null
        _state.update { it.copy(pendingName = null) }
        refresh(true, s.added(e.name))
    }

    /** Change the value of one plugin parameter (stored as text, as in plugins.js). */
    @Suppress("UNCHECKED_CAST")
    fun setParam(entry: PluginEntry, key: String, value: String) {
        val cur = entry.map["parameters"] as? Map<String, Any?> ?: return
        if ((cur[key] as? String) == value) return
        val nm = LinkedHashMap<String, Any?>(cur)
        nm[key] = value
        entry.map["parameters"] = nm
        _state.update { it.copy(dirty = true, message = null) }
    }

    fun setEnabled(entry: PluginEntry, on: Boolean) {
        entry.enabled = on
        refresh(true)
    }

    /** Move a plugin by [delta] positions (negative = up, toward the start of the load list). */
    fun move(entry: PluginEntry, delta: Int) {
        val p = js ?: return
        val i = p.entries.indexOf(entry)
        val j = i + delta
        if (i < 0 || j < 0 || j >= p.entries.size) return
        p.entries.removeAt(i)
        p.entries.add(j, entry)
        refresh(true)
    }

    fun remove(entry: PluginEntry) {
        val p = js ?: return
        if (p.entries.remove(entry)) refresh(true, pstr.removeMsg(entry.name))
    }

    /** Current enabled state of each plugin (to be saved as a profile). */
    fun snapshot(): Map<String, Boolean> = js?.entries?.associate { it.name to it.enabled } ?: emptyMap()

    fun applyProfile(name: String, prof: Map<String, Boolean>) {
        val p = js ?: return
        var n = 0
        for (e in p.entries) {
            val v = prof[e.name] ?: prof.entries.firstOrNull { it.key.equals(e.name, true) }?.value ?: continue
            if (e.enabled != v) { e.enabled = v; n++ }
        }
        refresh(n > 0, pstr.profApplied(name, n))
    }

    fun setOut(u: Uri) {
        out = u
        _state.update { it.copy(outName = treeName(u), message = null) }
    }

    fun save() {
        val s = pstr
        val p = js ?: run { _state.update { it.copy(error = s.needJs, message = null) }; return }
        val o = out ?: run { _state.update { it.copy(error = s.needOut, message = null) }; return }
        _state.update { it.copy(busy = true, error = null, message = null) }
        Engine.scope.launch {
            try {
                val tw = TreeWriter(Engine.app, o)
                writeWithBackup(tw, "plugins.js", p.write().toByteArray(Charsets.UTF_8))
                var nf = 0
                for (e in p.entries) {
                    val src = e.sourceText ?: continue
                    tw.open("plugins/" + e.name + ".js")!!.stream.use { it.write(src.toByteArray(Charsets.UTF_8)) }
                    nf++
                }
                _state.update { it.copy(busy = false, dirty = false, message = s.saved(p.entries.size, nf)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }
}

// ============================ Plugin Checker ============================

/** Ignored plugin checker findings (stored on the device, applies to all games). Key: "type|lowercase plugin name" or "type|*". */
object PluginIgnore {
    private fun sp() = Engine.app.getSharedPreferences("plugin_ignore", android.content.Context.MODE_PRIVATE)
    fun all(): Set<String> = try { HashSet(sp().getStringSet("keys", emptySet()) ?: emptySet()) } catch (e: Exception) { emptySet() }
    private fun put(s: Set<String>) { try { sp().edit().putStringSet("keys", HashSet(s)).apply() } catch (e: Exception) { } }
    fun keyOf(i: PluginIssue, anyPlugin: Boolean) = i.kind + "|" + (if (anyPlugin) "*" else i.plugin.lowercase())
    fun matches(i: PluginIssue, set: Set<String>) = i.kind.isNotEmpty() && (keyOf(i, false) in set || keyOf(i, true) in set)
    fun filter(list: List<PluginIssue>): List<PluginIssue> { val s = all(); return if (s.isEmpty()) list else list.filter { !matches(it, s) } }
    fun add(k: String): Set<String> { val s = all() + k; put(s); return s }
    fun remove(k: String): Set<String> { val s = all() - k; put(s); return s }
    fun clear(): Set<String> { put(emptySet()); return emptySet() }
}

data class PluginCheckState(
    val jsName: String? = null,
    val folderName: String? = null,
    val busy: Boolean = false,
    val ran: Boolean = false,
    val count: Int = 0,
    val issues: List<PluginIssue> = emptyList(),
    val ignored: Set<String> = emptySet(),
    val error: String? = null,
)

object PluginCheckJobs {
    private val _state = MutableStateFlow(PluginCheckState())
    val state: StateFlow<PluginCheckState> = _state.asStateFlow()
    private var pj: PluginsJs? = null
    private var tree: Uri? = null

    fun clear() { pj = null; tree = null; _state.value = PluginCheckState() }

    fun loadIgnored() { _state.update { it.copy(ignored = PluginIgnore.all()) } }
    fun ignore(i: PluginIssue, anyPlugin: Boolean) { val s = PluginIgnore.add(PluginIgnore.keyOf(i, anyPlugin)); _state.update { it.copy(ignored = s) } }
    fun unignore(k: String) { val s = PluginIgnore.remove(k); _state.update { it.copy(ignored = s) } }
    fun unignoreAll() { _state.update { it.copy(ignored = PluginIgnore.clear()) } }

    fun pickJs(u: Uri) {
        try {
            pj = PluginsJs.parse(readText(u))
            _state.update { it.copy(jsName = Engine.displayName(u), ran = false, issues = emptyList(), error = null) }
        } catch (e: Throwable) {
            _state.update { it.copy(error = pstr.badJs) }
        }
    }

    fun pickFolder(u: Uri) { tree = u; _state.update { it.copy(folderName = treeName(u), ran = false, issues = emptyList(), error = null) } }

    fun run() {
        val p = pj ?: run { _state.update { it.copy(error = pstr.chkNeed) }; return }
        _state.update { it.copy(busy = true, error = null) }
        Engine.scope.launch {
            try {
                var f: Map<String, String>? = null
                var z: Map<String, Long>? = null
                tree?.let { t ->
                    val m = HashMap<String, String>(); val sz = HashMap<String, Long>()
                    for (x in pluginFilesOf(listTree(Engine.app, t))) { val k = PluginCheck.key(x.path); m[k] = readHead(x); sz[k] = x.size }
                    f = m; z = sz
                }
                val r = PluginCheck.check(p.entries, f, z, pstr)
                _state.update { it.copy(busy = false, ran = true, issues = r, count = p.entries.size) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }
}

// ============================ plugins.js Comparer ============================

data class PluginDiffState(
    val nameA: String? = null,
    val nameB: String? = null,
    val items: List<PluginDiffItem> = emptyList(),
    val compared: Boolean = false,
    val countA: Int = 0,
    val countB: Int = 0,
    val error: String? = null,
)

object PluginDiffJobs {
    private val _state = MutableStateFlow(PluginDiffState())
    val state: StateFlow<PluginDiffState> = _state.asStateFlow()
    private var a: PluginsJs? = null
    private var b: PluginsJs? = null

    fun clear() { a = null; b = null; _state.value = PluginDiffState() }

    fun pick(u: Uri, isA: Boolean) {
        try {
            val p = PluginsJs.parse(readText(u))
            if (isA) { a = p; _state.update { it.copy(nameA = Engine.displayName(u), error = null) } }
            else { b = p; _state.update { it.copy(nameB = Engine.displayName(u), error = null) } }
            val x = a; val y = b
            if (x != null && y != null) _state.update { it.copy(items = PluginDiff.diff(x, y), compared = true, countA = x.entries.size, countB = y.entries.size) }
        } catch (e: Throwable) {
            _state.update { it.copy(error = pstr.badJs) }
        }
    }
}
