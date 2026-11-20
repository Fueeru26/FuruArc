package com.furuarc

import com.furuarc.i18n.ScriptStrings
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.furuarc.core.*
import com.furuarc.i18n.r
import com.furuarc.storage.TreeWriter
import com.furuarc.storage.listTree
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * RPGM Tools jobs: MV/MZ asset decryption/encryption, translation text export/import, save export/import.
 * Uses the state, progress, log, and output folder of [Engine] (modes 3..8).
 *
 * Mode: 3 = decrypt assets, 4 = encrypt assets, 5 = export text, 6 = import text, 7 = export save, 8 = import save.
 */
object RpgmJobs {
    private const val WORKERS = 4
    private var inTree: Uri? = null
    private var inFile: Uri? = null

    @Volatile
    private var scanned: List<PackInput> = emptyList()

    @Volatile
    private var sysKey: ByteArray? = null
    private var scanJob: Job? = null
    private var fileJob: Job? = null

    private val str get() = Engine.str
    internal val app get() = Engine.app

    @Volatile
    private var editText: String? = null

    @Volatile
    private var editRecipe: String? = null // null = JSON file; otherwise the wrapper recipe of the original save

    @Volatile
    private var names: GameNames? = null

    private var previewIdx = 0

    internal fun scannedFiles(): List<PackInput> = scanned
    internal fun inputFile(): Uri? = inFile

    internal fun upd(f: (RpgmUi) -> RpgmUi) = Engine._state.update { it.copy(rpgm = f(it.rpgm)) }

    fun clear() {
        scanJob?.cancel(); fileJob?.cancel()
        inTree = null; inFile = null; scanned = emptyList(); sysKey = null
        names = null; editText = null; editRecipe = null; previewIdx = 0
    }

    private fun fail(msg: String) {
        upd { it.copy(error = msg) }
        Engine.log(msg)
    }

    // ---------- pick folder / file ----------

    /** Pick the game folder / data folder; its contents are scanned in the background. */
    /** Rescan the chosen folder with/without subfolders (does nothing if a file was chosen). */
    fun rescanFolder(recursive: Boolean) {
        val t = inTree ?: return
        setInputFolder(t, recursive)
    }

    fun setInputFolder(uri: Uri, recursive: Boolean = true) {
        inTree = uri; scanned = emptyList(); sysKey = null
        Engine.clearResult()
        upd {
            it.copy(
                inName = DocumentFile.fromTreeUri(app, uri)?.name ?: "folder", scanning = true,
                found = 0, foundBytes = 0, keyHex = null, keySource = 0, error = null,
                report = emptyList(), imgCount = 0, audCount = 0, otherCount = 0,
                previewName = null, previewBytes = null, previewError = null,
            )
        }
        previewIdx = 0
        scanJob?.cancel()
        scanJob = Engine.scope.launch {
            try {
                scanned = listTree(app, uri, recursive)
                recount()
                upd { it.copy(scanning = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                upd { it.copy(scanning = false) }
                fail(str.logError(e.message ?: ""))
            }
        }
    }

    /** Pick several asset files at once (alternative to a folder); treated like the contents of one flat folder. */
    fun setInputFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        inTree = null; sysKey = null
        Engine.clearResult()
        val list = ArrayList<PackInput>()
        val used = HashSet<String>()
        for (u in uris) {
            var n = Engine.displayName(u)
            if (!used.add(n.lowercase())) {
                var k = 2
                while (!used.add(("(" + k + ")" + n).lowercase())) k++
                n = "(" + k + ")" + n
            }
            val size = DocumentFile.fromSingleUri(app, u)?.length() ?: 0L
            list.add(PackInput(n, size) { app.contentResolver.openInputStream(u) ?: throw IOException(n) })
        }
        scanned = list
        previewIdx = 0
        upd {
            it.copy(
                inName = if (list.size == 1) list[0].path else list.size.toString() + " file", scanning = true,
                found = 0, foundBytes = 0, keyHex = null, keySource = 0, error = null,
                report = emptyList(), imgCount = 0, audCount = 0, otherCount = 0,
                previewName = null, previewBytes = null, previewError = null,
            )
        }
        scanJob?.cancel()
        scanJob = Engine.scope.launch {
            try { recount(); upd { it.copy(scanning = false) } }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { upd { it.copy(scanning = false) }; fail(str.logError(e.message ?: "")) }
        }
    }

    private fun isSystemJson(path: String) = path.replace('\\', '/').lowercase().endsWith("data/system.json")

    private fun systemEntry(): PackInput? = scanned.filter { isSystemJson(it.path) }.minByOrNull { it.path.length }

    private fun readHead(p: PackInput, n: Int): ByteArray =
        p.open().use { ins ->
            val b = ByteArray(n)
            val r = IoUtil.fill(ins, b)
            b.copyOf(r)
        }

    /** Count files to process + find the key, according to the screen mode. */
    private fun recount() {
        val mode = Engine._state.value.mode
        val list = scanned
        var n = 0
        var bytes = 0L
        var key: ByteArray? = null
        var src = 0
        var img = 0
        var aud = 0
        var oth = 0
        when (mode) {
            3, 4 -> {
                for (f in list) {
                    val ok = if (mode == 3) RpgmCrypt.decryptedPath(f.path) != null else RpgmCrypt.encryptKind(f.path) != 0
                    if (ok) {
                        n++; bytes += f.size
                        val kind = if (mode == 3) {
                            when (RpgmCrypt.decryptedPath(f.path)?.substringAfterLast('.', "")?.lowercase()) {
                                "png" -> 1
                                "ogg", "m4a" -> 2
                                else -> 0
                            }
                        } else RpgmCrypt.encryptKind(f.path)
                        when (kind) { 1 -> img++; 2 -> aud++; else -> oth++ }
                    }
                }
                systemEntry()?.let { e ->
                    try {
                        key = e.open().use { it.readBytes() }.toString(Charsets.UTF_8).let { RpgmCrypt.keyFromSystemJson(it) }
                        if (key != null) src = 1
                    } catch (ex: Exception) {
                    }
                }
                if (key == null && mode == 3) {
                    // no System.json: guess the key from the encrypted PNG header
                    val png = list.firstOrNull { val x = it.path.lowercase(); x.endsWith(".rpgmvp") || x.endsWith(".png_") }
                    if (png != null) {
                        try {
                            key = RpgmCrypt.detectKey(readHead(png, 32))
                            if (key != null) src = 2
                        } catch (ex: Exception) {
                        }
                    }
                }
            }
            5, 6 -> for (f in list) if (RpgmText.isTextFile(f.path)) { n++; bytes += f.size }
            in 20..24 -> {
                // VN Script Tools (text export/import, rpyc, nscript.dat)
                val c = ScriptJobs.count(Engine._state.value.engineId, mode, list)
                n = c.first; bytes = c.second
                if (n == 0) {
                    upd { it.copy(found = 0, foundBytes = 0, keyHex = null, keySource = 0) }
                    fail(ScriptStrings.of(Engine.lang).noFiles(mode))
                    return
                }
            }
        }
        sysKey = key
        upd {
            it.copy(
                found = n, foundBytes = bytes, keyHex = key?.let { k -> RpgmCrypt.keyToHex(k) }, keySource = src,
                imgCount = img, audCount = aud, otherCount = oth,
            )
        }
        if (n == 0) fail(if (mode >= 5) str.trNoFiles else str.rpgmNoAssets)
    }

    /** Pick a single file (translation file, save, or exported json). */
    fun setInputFile(uri: Uri) {
        inFile = uri
        Engine.clearResult()
        val name = Engine.displayName(uri)
        upd { it.copy(fileName = name, error = null, warn = null, report = emptyList(), saveKind = 0, edit = emptyList()) }
        val mode = Engine._state.value.mode
        editText = null
        if (mode == 10) {
            upd { it.copy(trItems = emptyList(), trInfo = null) }
            fileJob?.cancel()
            fileJob = Engine.scope.launch {
                try {
                    val text = readText(uri)
                    val (f, fmt) = RpgmText.readEntriesAny(name, text)
                    if (f.items.isEmpty()) {
                        fail(str.r.edNoEntries)
                        return@launch
                    }
                    val draft = f.marker || RpgmText.isDraftName(name)
                    // a regular file always shows with an empty translation column; only a draft restores the entries
                    val items = if (draft) f.items else f.items.map { it.copy(tr = "") }
                    val filled = items.count { it.tr.isNotBlank() }
                    upd { it.copy(trItems = items, trFmt = fmt, trDraft = draft, trInfo = str.r.edLoaded(draft, filled, items.size)) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    fail(str.logError(e.message ?: ""))
                }
            }
            return
        }
        if (mode == 9) {
            fileJob?.cancel()
            fileJob = Engine.scope.launch {
                try {
                    val bytes = readBytes(uri)
                    val text = String(bytes, Charsets.UTF_8)
                    // exported JSON file, or an original save file (MV/MZ)
                    var kind = 0
                    var recipe: String? = null
                    var json = text
                    var root: Any? = try {
                        Json.parse(text)
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                    if (root !is Map<*, *>) {
                        val d = RpgmSave.decode(bytes)
                        if (d == null) {
                            fail(str.saveUnknown)
                            return@launch
                        }
                        kind = d.kind
                        recipe = d.recipe
                        rememberRecipe(d.recipe)
                        json = d.json
                        root = Json.parse(json)
                    }
                    val secs = SaveEditor.sections(root, names)
                    if (secs.isEmpty()) {
                        fail(str.valueNone)
                        return@launch
                    }
                    editText = json
                    editRecipe = recipe
                    upd { it.copy(saveKind = kind, edit = secs) }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    fail(str.jsonInvalid(e.message ?: ""))
                }
            }
            return
        }
        if (mode == 7 || mode == 8) {
            fileJob?.cancel()
            fileJob = Engine.scope.launch {
                try {
                    if (mode == 7) {
                        val d = RpgmSave.decode(readBytes(uri))
                        if (d == null) fail(str.saveUnknown) else {
                            rememberRecipe(d.recipe)
                            upd { it.copy(saveKind = d.kind) }
                        }
                    } else {
                        try {
                            val chk = SaveEditor.validate(Json.parse(readText(uri)))
                            when (chk.fatal) {
                                1 -> fail(str.r.saveNotObject)
                                2 -> fail(str.r.saveNotSave)
                                else -> upd { it.copy(warn = checkWarning(chk)) }
                            }
                        } catch (e: IllegalArgumentException) {
                            fail(str.jsonInvalid(e.message ?: ""))
                        }
                        val low = name.lowercase()
                        val k = if (low.contains(".rmmzsave")) 2 else if (low.contains(".rpgsave")) 1 else 0
                        upd { it.copy(saveKind = k) }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    fail(str.logError(e.message ?: ""))
                }
            }
        }
    }

    /** Warning message for non-fatal [SaveEditor.validate] results (null if none). */
    private fun checkWarning(c: SaveCheck): String? {
        val parts = ArrayList<String>()
        if (c.missing.isNotEmpty()) parts.add(str.r.saveWarnMissing(c.missing.joinToString(", ")))
        if (c.badTypes.isNotEmpty()) parts.add(str.r.saveWarnTypes(c.badTypes.joinToString(", ")))
        return if (parts.isEmpty()) null else parts.joinToString("\n")
    }

    /** Pick the game / data folder (optional) for the real names of items, characters, variables, and switches in the Value Editor. */
    fun setNamesFolder(uri: Uri) {
        names = null
        upd { it.copy(namesName = DocumentFile.fromTreeUri(app, uri)?.name ?: "folder", namesInfo = null, namesOk = false) }
        Engine.scope.launch {
            try {
                val list = listTree(app, uri)
                fun text(n: String): String? {
                    val low = n.lowercase()
                    val hit = list.filter {
                        val q = it.path.replace('\\', '/').lowercase()
                        q == "$low.json" || q.endsWith("/data/$low.json") || q.endsWith("/$low.json")
                    }.minByOrNull { it.path.length } ?: return null
                    return try {
                        hit.open().use { it.readBytes() }.toString(Charsets.UTF_8)
                    } catch (e: Exception) {
                        null
                    }
                }
                val gn = SaveEditor.namesFrom(text("System"), text("Items"), text("Weapons"), text("Armors"), text("Actors"))
                if (gn.total == 0) {
                    upd { it.copy(namesInfo = str.r.namesNone, namesOk = false) }
                } else {
                    names = gn
                    upd { it.copy(namesInfo = str.r.namesLoaded(gn.total), namesOk = true) }
                    refreshSections()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                upd { it.copy(namesInfo = str.logError(e.message ?: ""), namesOk = false) }
            }
        }
    }

    /** Rebuild the Value Editor sections (e.g. after the database names are loaded). Values being edited are kept because the keys are the same. */
    private fun refreshSections() {
        val t = editText ?: return
        try {
            val secs = SaveEditor.sections(Json.parse(t), names)
            if (secs.isNotEmpty()) upd { it.copy(edit = secs) }
        } catch (e: Exception) {
        }
    }

    /** Preview one asset image (decrypted in memory in decrypt mode). [next] = next image. */
    fun previewAsset(manualKey: String, next: Boolean) {
        val mode = Engine._state.value.mode
        val imgs = scanned.filter { f ->
            if (mode == 3) RpgmCrypt.decryptedPath(f.path)?.lowercase()?.endsWith(".png") == true
            else RpgmCrypt.encryptKind(f.path) == 1
        }
        if (imgs.isEmpty()) {
            upd { it.copy(previewBytes = null, previewName = null, previewError = str.r.previewNone) }
            return
        }
        previewIdx = if (next) (previewIdx + 1) % imgs.size else previewIdx.coerceIn(0, imgs.size - 1)
        val f = imgs[previewIdx]
        val manual = manualKey.trim()
        val key = if (manual.isNotEmpty()) RpgmCrypt.parseKey(manual) else sysKey
        if (mode == 3 && key == null) {
            upd { it.copy(previewBytes = null, previewName = null, previewError = str.r.previewNeedKey) }
            return
        }
        Engine.scope.launch {
            try {
                if (f.size > 12L * 1024 * 1024) throw IOException(str.r.previewFail)
                val bo = ByteArrayOutputStream()
                f.open().use { ins -> if (mode == 3 && key != null) RpgmCrypt.decrypt(ins, bo, key) else ins.copyTo(bo) }
                upd { it.copy(previewBytes = bo.toByteArray(), previewName = f.path, previewError = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                upd { it.copy(previewBytes = null, previewName = f.path, previewError = str.r.previewFail) }
            }
        }
    }

    /** Upload System.json (optional): take its encryptionKey to fill the key field. */
    fun setSystemJson(uri: Uri) {
        val name = Engine.displayName(uri)
        upd { it.copy(sysName = name, sysKeyHex = null, sysError = null) }
        Engine.scope.launch {
            try {
                val root = try {
                    Json.parse(readText(uri))
                } catch (e: IllegalArgumentException) {
                    null
                } as? Map<*, *>
                val looksRight = root != null && (name.lowercase().startsWith("system") || root.containsKey("gameTitle"))
                if (!looksRight) {
                    upd { it.copy(sysError = str.sysNotSystem) }
                    return@launch
                }
                val key = (root?.get("encryptionKey") as? String)?.let { RpgmCrypt.parseKey(it) }
                if (key == null) upd { it.copy(sysError = str.sysNoKey) }
                else upd { it.copy(sysKeyHex = RpgmCrypt.keyToHex(key)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                upd { it.copy(sysError = str.sysNotSystem) }
            }
        }
    }

    private fun readBytes(uri: Uri): ByteArray =
        (app.contentResolver.openInputStream(uri) ?: throw IOException(str.errCannotOpen)).use { it.readBytes() }

    internal fun readText(uri: Uri): String = readBytes(uri).toString(Charsets.UTF_8)

    // wrapper recipe of the last MZ save read, reused for Import Save from JSON
    private fun rememberRecipe(r: String) {
        if (r == "L") return
        try {
            app.getSharedPreferences("furuarc_rpgm", 0).edit().putString("mz_recipe", r).apply()
        } catch (e: Exception) {
        }
    }

    private fun savedRecipe(): String? = try {
        app.getSharedPreferences("furuarc_rpgm", 0).getString("mz_recipe", null)
    } catch (e: Exception) {
        null
    }

    internal fun writeBytes(w: TreeWriter, path: String, data: ByteArray, policy: ConflictPolicy): Boolean {
        val of = w.open(path, policy) ?: return false
        of.stream.use { it.write(data) }
        return true
    }

    /**
     * Back up the file to be overwritten: copy the contents of the existing [target] in the output folder to "[target].bak"
     * (or [fallback] if it does not exist yet). Old backups are never overwritten (the name is made unique).
     */
    private fun backupExisting(w: TreeWriter, target: String, fallback: ByteArray?) {
        val old = w.readExisting(target) ?: fallback
        if (old == null) {
            Engine.log(str.r.logBackupNone)
            return
        }
        val bak = "$target.bak"
        val of = w.open(bak, ConflictPolicy.RENAME) ?: return
        of.stream.use { it.write(old) }
        Engine.log(str.r.logBackup(bak))
    }

    // ---------- MV/MZ assets ----------

    fun canRunAssets(): Boolean {
        val s = Engine._state.value
        return !s.running && !s.rpgm.scanning && s.rpgm.found > 0 && s.outName != null
    }

    fun assets(encrypt: Boolean, manualKey: String, policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val mz = Engine._state.value.engineId == "rpgm_mz"
        val manual = manualKey.trim()
        val key: ByteArray? = if (manual.isNotEmpty()) {
            RpgmCrypt.parseKey(manual) ?: run { fail(str.rpgmBadKey); return }
        } else sysKey
        if (key == null) { fail(str.rpgmNoKey); return }
        upd { it.copy(error = null) }

        val jobs = ArrayList<Pair<PackInput, String>>()
        for (f in scanned) {
            val o = if (encrypt) RpgmCrypt.encryptedPath(f.path, mz) else RpgmCrypt.decryptedPath(f.path)
            if (o != null) jobs.add(Pair(f, o))
        }
        if (jobs.isEmpty()) return
        val total = jobs.sumOf { it.first.size }
        val sys = if (encrypt) systemEntry() else null

        Engine.launchJob(total) {
            val t0 = System.currentTimeMillis()
            val writer = TreeWriter(app, out)
            val next = AtomicInteger(0)
            val finished = AtomicInteger(0)
            val failed = AtomicInteger(0)
            val skipped = AtomicInteger(0)
            val doneBytes = AtomicLong(0)
            val okBytes = AtomicLong(0)
            val imgOk = AtomicInteger(0)
            val audOk = AtomicInteger(0)
            Engine.log(str.logRpgmStart(jobs.size, encrypt))
            coroutineScope {
                repeat(WORKERS) {
                    launch {
                        while (true) {
                            ensureActive()
                            val i = next.getAndIncrement()
                            if (i >= jobs.size) break
                            val (inp, op) = jobs[i]
                            try {
                                val of = writer.open(op, policy)
                                if (of == null) {
                                    skipped.incrementAndGet()
                                } else {
                                    inp.open().use { ins ->
                                        of.stream.use { os ->
                                            if (encrypt) RpgmCrypt.encrypt(ins, os, key) else RpgmCrypt.decrypt(ins, os, key)
                                        }
                                    }
                                    okBytes.addAndGet(inp.size)
                                    if (encrypt) {
                                        if (RpgmCrypt.encryptKind(inp.path) == 1) imgOk.incrementAndGet() else audOk.incrementAndGet()
                                    }
                                }
                            } catch (ex: CancellationException) {
                                throw ex
                            } catch (ex: Exception) {
                                failed.incrementAndGet()
                                Engine.log(str.logFailed(inp.path, ex.message ?: ""))
                            }
                            val b = doneBytes.addAndGet(inp.size)
                            Engine.report(finished.incrementAndGet(), jobs.size, b, total, inp.path, false)
                        }
                    }
                }
            }
            // Encrypt: update System.json in the output folder so the game uses the correct key & flags.
            if (sys != null && (imgOk.get() + audOk.get()) > 0) {
                try {
                    val m = Json.parse(sys.open().use { it.readBytes() }.toString(Charsets.UTF_8)) as? MutableMap<String, Any?>
                    if (m != null) {
                        m["encryptionKey"] = RpgmCrypt.keyToHex(key)
                        if (imgOk.get() > 0) m["hasEncryptedImages"] = true
                        if (audOk.get() > 0) m["hasEncryptedAudio"] = true
                        val wrote = writeBytes(writer, sys.path, Json.write(m).toByteArray(Charsets.UTF_8), policy)
                        if (wrote) Engine.log(str.logSystemUpdated)
                    }
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    Engine.log(str.logFailed(sys.path, ex.message ?: ""))
                }
            }
            Engine.report(jobs.size, jobs.size, total, total, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            val ok = jobs.size - failed.get() - skipped.get()
            Engine.log(str.logDone(ok, failed.get(), sec))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(ok, failed.get(), skipped.get(), okBytes.get(), sec, false))
        }
    }

    // ---------- translation ----------

    fun canRunText(needFile: Boolean): Boolean {
        val s = Engine._state.value
        return !s.running && !s.rpgm.scanning && s.rpgm.found > 0 && s.outName != null &&
            (!needFile || (s.rpgm.fileName != null && s.rpgm.error == null))
    }

    fun translateExport(fmt: Int, policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val files = scanned.filter { RpgmText.isTextFile(it.path) }
        if (files.isEmpty()) return
        upd { it.copy(error = null) }
        Engine.exportUri = null
        Engine.launchJob(files.sumOf { it.size }) {
            val t0 = System.currentTimeMillis()
            val units = ArrayList<TextUnit>()
            var failed = 0
            var done = 0L
            for ((i, f) in files.withIndex()) {
                ensureActive()
                try {
                    val root = RpgmText.parseData(f.path, f.open().use { it.readBytes() }.toString(Charsets.UTF_8)).root
                    units.addAll(RpgmText.extract(RpgmText.fileKey(f.path), root))
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    failed++
                    Engine.log(str.logFailed(f.path, ex.message ?: ""))
                }
                done += f.size
                Engine.report(i + 1, files.size, done, files.sumOf { it.size }, f.path, false)
            }
            if (units.isEmpty()) throw IOException(str.trNoText)
            val data = RpgmText.export(units, fmt).toByteArray(Charsets.UTF_8)
            val writer = TreeWriter(app, out)
            val name = Engine.exportBase.trim().ifEmpty { "translate" } + "." + RpgmText.extFor(fmt)
            val of = writer.open(name, policy)
            of?.stream?.use { it.write(data) }
            val wrote = of != null
            Engine.exportUri = of?.uri
            Engine.report(files.size, files.size, 1, 1, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            Engine.log(str.logTrExported(units.size, name))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(units.size, failed, if (wrote) 0 else 1, data.size.toLong(), sec, false))
        }
    }

    fun translateImport(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val tf = inFile ?: return
        val files = scanned.filter { RpgmText.isTextFile(it.path) }
        if (files.isEmpty()) return
        upd { it.copy(error = null, report = emptyList()) }
        Engine.launchJob(files.sumOf { it.size }) {
            val t0 = System.currentTimeMillis()
            val text = readText(tf)
            val name = Engine._state.value.rpgm.fileName ?: ""
            val tfile = RpgmText.readAny(name, text)
            val all = tfile.map
            if (all.isEmpty()) throw IOException(str.trEmpty)
            val stats = RpgmText.ApplyStats()
            val byFile = HashMap<String, HashMap<String, String>>()
            for ((k, v) in all) byFile.getOrPut(k.first) { HashMap() }[k.second] = v
            val writer = TreeWriter(app, out)
            var applied = 0
            var written = 0
            var failed = 0
            var skipped = 0
            var bytes = 0L
            var done = 0L
            val total = files.sumOf { it.size }
            for ((i, f) in files.withIndex()) {
                ensureActive()
                val key = RpgmText.fileKey(f.path)
                val tr = byFile[key]
                if (tr != null) {
                    try {
                        val df = RpgmText.parseData(f.path, f.open().use { it.readBytes() }.toString(Charsets.UTF_8))
                        val n = RpgmText.apply(key, df.root, tr, stats)
                        if (n > 0) {
                            val data = df.write().toByteArray(Charsets.UTF_8)
                            if (writeBytes(writer, f.path, data, policy)) {
                                written++; applied += n; bytes += data.size
                                Engine.log(str.logTrFile(f.path, n))
                            } else skipped++
                        }
                    } catch (ex: CancellationException) {
                        throw ex
                    } catch (ex: Exception) {
                        failed++
                        Engine.log(str.logFailed(f.path, ex.message ?: ""))
                    }
                }
                done += f.size
                Engine.report(i + 1, files.size, done, total, f.path, false)
            }
            Engine.report(files.size, files.size, total, total, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            val rp = str.r
            upd {
                it.copy(
                    report = listOf(
                        rp.repEntries to tfile.rows.toString(),
                        rp.repReplaced to applied.toString(),
                        rp.repUnmatched to (all.size - stats.matched).coerceAtLeast(0).toString(),
                        rp.repEmpty to tfile.empty.toString(),
                        rp.repWarnings to stats.warnings.size.toString(),
                    ),
                )
            }
            for (w in stats.warnings.take(30)) Engine.log(rp.logCodeWarn(w))
            if (stats.warnings.size > 30) Engine.log(rp.logCodeWarnMore(stats.warnings.size - 30))
            if (applied == 0 && failed == 0) throw IOException(str.trNoMatch)
            Engine.log(str.logTrImported(applied, written))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(applied, failed, skipped, bytes, sec, false))
        }
    }

    // ---------- save ----------

    fun canRunSave(): Boolean {
        val s = Engine._state.value
        return !s.running && s.outName != null && s.rpgm.fileName != null && s.rpgm.error == null
    }

    fun saveExport(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val f = inFile ?: return
        val name = Engine._state.value.rpgm.fileName ?: "save"
        upd { it.copy(error = null) }
        Engine.launchJob(0) {
            val t0 = System.currentTimeMillis()
            val d = RpgmSave.decode(readBytes(f)) ?: throw IOException(str.saveUnknown)
            rememberRecipe(d.recipe)
            val data = Json.write(Json.parse(d.json), true).toByteArray(Charsets.UTF_8)
            val writer = TreeWriter(app, out)
            val outName = "$name.json"
            val wrote = writeBytes(writer, outName, data, policy)
            Engine.report(1, 1, 0, 0, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            Engine.log(str.logSaveExported(outName))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(if (wrote) 1 else 0, 0, if (wrote) 0 else 1, data.size.toLong(), sec, false))
        }
    }

    /** [kind] = RpgmSave.MV / RpgmSave.MZ. */
    fun saveImport(kind: Int, policy: ConflictPolicy, backup: Boolean) {
        val out = Engine.outTree ?: return
        val f = inFile ?: return
        val name = Engine._state.value.rpgm.fileName ?: "save.json"
        upd { it.copy(error = null) }
        Engine.launchJob(0) {
            val t0 = System.currentTimeMillis()
            val root = try {
                Json.parse(readText(f))
            } catch (e: IllegalArgumentException) {
                throw IOException(str.jsonInvalid(e.message ?: ""))
            }
            val chk = SaveEditor.validate(root)
            if (chk.fatal == 1) throw IOException(str.r.saveNotObject)
            if (chk.fatal == 2) throw IOException(str.r.saveNotSave)
            checkWarning(chk)?.let { Engine.log(it) }
            val jsonText = Json.write(root)
            val enc = RpgmSave.encode(jsonText, RpgmSave.recipeFor(kind, savedRecipe()))
            if (RpgmSave.decode(enc)?.json != jsonText) throw IOException(str.r.saveVerifyFail)
            var base = name.removeSuffix(".json").removeSuffix(".JSON")
            for (e in listOf(".rpgsave", ".rmmzsave")) if (base.lowercase().endsWith(e)) base = base.dropLast(e.length)
            val outName = base + "." + RpgmSave.ext(kind)
            val writer = TreeWriter(app, out)
            if (backup) backupExisting(writer, outName, null)
            val wrote = writeBytes(writer, outName, enc, policy)
            Engine.report(1, 1, 0, 0, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            Engine.log(str.logSaveImported(outName))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(if (wrote) 1 else 0, 0, if (wrote) 0 else 1, enc.size.toLong(), sec, false))
        }
    }

    // ---------- Translation Editor ----------

    fun canRunTrEdit(): Boolean {
        val s = Engine._state.value
        return !s.running && s.outName != null && s.rpgm.trItems.isNotEmpty()
    }

    /**
     * Write the Translation Editor result. [texts]: entry index -> filled-in translation. [draft] = true saves as
     * "name_draf.ext" (entries are restored when uploaded again); false writes the final file with the name without "_draf".
     */
    fun saveTranslation(texts: Map<Int, String>, draft: Boolean, policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val st = Engine._state.value.rpgm
        val items = st.trItems
        if (items.isEmpty()) return
        val src = st.fileName ?: "terjemahan"
        val merged = items.mapIndexed { i, it -> it.copy(tr = texts[i] ?: it.tr) }
        if (!draft && merged.none { it.tr.isNotBlank() }) {
            upd { it.copy(error = str.r.edNothing) }
            return
        }
        upd { it.copy(error = null) }
        Engine.launchJob(0) {
            val t0 = System.currentTimeMillis()
            val ext = src.substringAfterLast('.', RpgmText.extFor(st.trFmt))
            var base = if (src.contains('.')) src.substringBeforeLast('.') else src
            val di = base.lowercase().lastIndexOf("_draf")
            if (di >= 0) base = base.substring(0, di) + base.substring(di + 5)
            val name = (if (draft) base + "_draf" else base) + "." + ext
            val text = RpgmText.write(merged, draft, st.trFmt)
            val data = text.toByteArray(Charsets.UTF_8)
            val writer = TreeWriter(app, out)
            val wrote = writeBytes(writer, name, data, policy)
            Engine.report(1, 1, 0, 0, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            Engine.log(if (draft) str.r.logDraftSaved(name) else str.r.logTrSaved(name))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(merged.size, 0, if (wrote) 0 else 1, data.size.toLong(), sec, false))
        }
    }

    @Volatile
    private var autoBusy = false

    /**
     * Auto-save the Translation Editor draft without a summary card: the same "_draf" file is always overwritten.
     * Skipped if there is no Output Folder yet, another process is running, or the previous auto-save has not finished.
     * [onDone] is called with true if the write succeeded.
     */
    fun autoSaveDraft(texts: Map<Int, String>, onDone: (Boolean) -> Unit) {
        val out = Engine.outTree ?: return
        val full = Engine._state.value
        val st = full.rpgm
        val items = st.trItems
        if (items.isEmpty() || full.running || autoBusy) return
        autoBusy = true
        val src = st.fileName ?: "terjemahan"
        val fmt = st.trFmt
        val merged = items.mapIndexed { i, it -> it.copy(tr = texts[i] ?: it.tr) }
        Engine.scope.launch {
            var ok = false
            try {
                val ext = src.substringAfterLast('.', RpgmText.extFor(fmt))
                var base = if (src.contains('.')) src.substringBeforeLast('.') else src
                val di = base.lowercase().lastIndexOf("_draf")
                if (di >= 0) base = base.substring(0, di) + base.substring(di + 5)
                val data = RpgmText.write(merged, true, fmt).toByteArray(Charsets.UTF_8)
                ok = writeBytes(TreeWriter(Engine.app, out), base + "_draf." + ext, data, ConflictPolicy.OVERWRITE)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                ok = false
            } finally {
                autoBusy = false
            }
            onDone(ok)
        }
    }

    // ---------- Value Editor ----------

    fun canRunEdit(): Boolean {
        val s = Engine._state.value
        return !s.running && s.outName != null && s.rpgm.edit.isNotEmpty()
    }

    /** Apply the changed values ([edits]: key -> text) then write to the output folder with the same file name. */
    fun applyEdits(edits: Map<String, String>, policy: ConflictPolicy, backup: Boolean) {
        val out = Engine.outTree ?: return
        val text = editText ?: return
        val recipe = editRecipe
        val name = Engine._state.value.rpgm.fileName ?: "save.json"
        val sections = Engine._state.value.rpgm.edit
        upd { it.copy(error = null) }
        Engine.launchJob(0) {
            val t0 = System.currentTimeMillis()
            val root = Json.parse(text)
            val n = try {
                SaveEditor.apply(root, edits)
            } catch (e: IllegalArgumentException) {
                val key = e.message ?: ""
                val label = sections.flatMap { it.groups }.flatMap { it.fields }.firstOrNull { it.key == key }?.label ?: key
                val msg = str.valueInvalid(label)
                upd { it.copy(error = msg) }
                throw IOException(msg)
            }
            if (n == 0) {
                upd { it.copy(error = str.valueNoChange) }
                throw IOException(str.valueNoChange)
            }
            val data = if (recipe == null) Json.write(root, true).toByteArray(Charsets.UTF_8)
            else RpgmSave.encode(Json.write(root), recipe)
            val writer = TreeWriter(app, out)
            if (backup) {
                val orig = try {
                    inFile?.let { readBytes(it) }
                } catch (e: Exception) {
                    null
                }
                backupExisting(writer, name, orig)
            }
            val wrote = writeBytes(writer, name, data, policy)
            Engine.report(1, 1, 0, 0, "", true)
            val sec = (System.currentTimeMillis() - t0) / 1000.0
            Engine.log(str.logSaveExported(name))
            Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
            Engine.finishJob(Summary(n, 0, if (wrote) 0 else 1, data.size.toLong(), sec, false))
        }
    }
}
