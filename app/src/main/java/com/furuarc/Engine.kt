package com.furuarc

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import com.furuarc.core.*
import com.furuarc.i18n.Str
import com.furuarc.i18n.Strings
import com.furuarc.i18n.r
import com.furuarc.i18n.x
import kotlinx.coroutines.withContext
import com.furuarc.settings.Lang
import com.furuarc.storage.ChannelSource
import com.furuarc.storage.TreeWriter
import com.furuarc.storage.listTree
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.BitSet
import java.util.zip.Adler32
import java.util.zip.CRC32
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/** Summary of unpack/pack results. */
data class Summary(
    val ok: Int,
    val failed: Int,
    val skipped: Int,
    val bytes: Long,
    val seconds: Double,
    val pack: Boolean,
)

/** Result of "Verify extraction". */
data class VerifyResult(
    val checked: Int,
    val sizeOk: Int,
    val sizeBad: Int,
    val unreadable: Int,
    val sumChecked: Int,
    val sumOk: Int,
    val sumBad: Int,
    val expectedBytes: Long,
    val actualBytes: Long,
    val problems: List<String>,
)

/** Raw contents of one archive file for preview (up to a few MB). */
class PreviewData(val name: String, val size: Long, val bytes: ByteArray?, val error: String?)

/** Input state of the RPGM Tools screen (MV/MZ assets, translation, save). */
data class RpgmUi(
    val inName: String? = null, // input folder
    val fileName: String? = null, // input file (translation file / save / json)
    val scanning: Boolean = false,
    val found: Int = 0,
    val foundBytes: Long = 0,
    val keyHex: String? = null,
    val keySource: Int = 0, // 0 = none, 1 = System.json, 2 = detected from PNG
    val saveKind: Int = 0, // 0 = unknown, 1 = MV, 2 = MZ
    val error: String? = null,
    val sysName: String? = null, // uploaded System.json (MV/MZ)
    val sysKeyHex: String? = null, // key from the uploaded System.json
    val sysError: String? = null,
    val edit: List<EditSection> = emptyList(), // sections of the Value Editor
    val namesName: String? = null, // game database folder (optional, for names in the Value Editor)
    val namesInfo: String? = null, // result of reading the names, or an error message
    val namesOk: Boolean = false,
    val warn: String? = null, // warning (non-blocking), e.g. incomplete save structure
    val report: List<Pair<String, String>> = emptyList(), // translation import report
    val imgCount: Int = 0, // asset breakdown (RPGM Engine List)
    val audCount: Int = 0,
    val otherCount: Int = 0,
    val previewName: String? = null, // asset image preview
    val previewBytes: ByteArray? = null,
    val previewError: String? = null,
    val trItems: List<TrItem> = emptyList(), // Translation Editor: entries from the text/draft file
    val trFmt: Int = 0,
    val trDraft: Boolean = false,
    val trInfo: String? = null,
)

data class UiState(
    // screen: 0 = menu, 1 = unpack, 2 = pack (so the screen can be restored when the app is reopened)
    val mode: Int = 0,
    val engineId: String? = null,
    // unpack
    val archiveError: String? = null,
    val archiveName: String? = null,
    val formatId: String? = null,
    val formatName: String? = null,
    val entryCount: Int = 0,
    val batchCount: Int = 0, // > 0: several archives selected at once (bulk unpack)
    val batchSkipped: Int = 0,
    val outName: String? = null,
    val keyChecked: Boolean = false,
    val detectedKey: Int? = null,
    val scopePartial: Boolean = false,
    val selFiles: Int = 0,
    val selFolders: Int = 0,
    // pack
    val packFolderName: String? = null,
    val packOutName: String? = null,
    val packScanning: Boolean = false,
    val packFiles: Int = 0,
    val packBytes: Long = 0,
    val packIgnored: Int = 0,
    // process
    val running: Boolean = false,
    val done: Boolean = false,
    val progress: Float = 0f,
    val current: String = "",
    val doneBytes: Long = 0,
    val totalBytes: Long = 0,
    val etaSec: Long = -1,
    val summary: Summary? = null,
    val verifying: Boolean = false,
    val verifyDone: Int = 0,
    val verifyTotal: Int = 0,
    val verifyResult: VerifyResult? = null,
    val verifyPack: Boolean = false, // the verification result is a packing integrity check
    val rpgm: RpgmUi = RpgmUi(),
    val log: List<String> = emptyList(),
)

/**
 * All unpack/pack state and processing. Lives at process level (not in the ViewModel), so it keeps
 * running when the Activity is closed as long as [ProcessService] keeps the process alive.
 */
object Engine {
    internal val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    internal lateinit var app: Application

    @Volatile
    var lang: Lang = Lang.EN
    internal val str: Str get() = Strings.of(lang)

    fun init(a: Application) {
        app = a
    }

    internal val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var archiveUri: Uri? = null
    internal var outTree: Uri? = null
    private var packFolder: Uri? = null
    private var packOutDir: Uri? = null
    private var format: ArchiveFormat? = null
    private var unpackExtra: Map<String, String> = emptyMap()
    private var expectedFormatId: String? = null

    @Volatile
    private var entries: List<Entry> = emptyList()

    @Volatile
    private var selection: BitSet? = null

    /** Bulk unpack: valid archives from the scan, and the list of selected URIs. */
    private class BatchItem(val uri: Uri, val name: String, val fmt: ArchiveFormat, val entries: List<Entry>)

    private var batchUris: List<Uri> = emptyList()

    @Volatile
    private var batchItems: List<BatchItem> = emptyList()

    @Volatile
    private var scanned: List<PackInput> = emptyList()
    private var ignore = IgnoreFilter("")
    internal var job: Job? = null
    private var verifyJob: Job? = null
    private var loadJob: Job? = null
    private var scanJob: Job? = null
    private val lastProgress = AtomicLong(0L)

    @Volatile
    private var startMs = 0L

    // settings read by the engine (filled in by MainViewModel)
    @Volatile
    var bgEnabled = false
    internal var defaultOut: Uri? = null

    // data for "Verify extraction"
    private class VCtx(val fmt: ArchiveFormat, val archive: Uri, var key: Int?)
    private class PackCtx(val fmt: ArchiveFormat, val uri: Uri, val inputs: List<PackInput>, val opts: Options)
    private var lastPack: PackCtx? = null
    private var lastCtx: List<VCtx> = emptyList() // parallel to lastChosen/lastWritten
    private var lastChosen: List<Entry> = emptyList()
    private var lastWritten: Array<Uri?> = emptyArray()
    internal var resultUri: Uri? = null
    /** Text file from the last Text Export (for the "Translate text" button). */
    internal var exportUri: Uri? = null
    /** Base name (without extension) of the Text Export file. */
    @Volatile internal var exportBase: String = "translate"

    // ---------- general ----------

    fun setMode(m: Int, engine: String? = null) = _state.update { it.copy(mode = m, engineId = engine) }

    fun reset() {
        job?.cancel(); loadJob?.cancel(); scanJob?.cancel(); verifyJob?.cancel()
        archiveUri = null; outTree = null; packFolder = null; packOutDir = null
        batchUris = emptyList(); batchItems = emptyList()
        expectedFormatId = null
        format = null; entries = emptyList(); selection = null; scanned = emptyList()
        lastCtx = emptyList(); lastChosen = emptyList(); lastWritten = emptyArray(); resultUri = null
        RpgmJobs.clear()
        _state.value = UiState()
        applyDefaultOutput()
    }

    /** Default output folder (from Settings). Empty/null = none. */
    fun setDefaultOutput(uri: String) {
        defaultOut = if (uri.isBlank()) null else Uri.parse(uri)
    }

    /** If no output folder is chosen, use the default folder (if still accessible). */
    private fun applyDefaultOutput() {
        val u = defaultOut ?: return
        val name = try {
            DocumentFile.fromTreeUri(app, u)?.takeIf { it.exists() }?.name
        } catch (e: Exception) {
            null
        } ?: return
        outTree = u
        packOutDir = u
        _state.update { it.copy(outName = name, packOutName = name) }
    }

    fun cancel() {
        job?.cancel()
        _state.update { it.copy(running = false) }
        log(str.cancelled)
    }

    fun isRunning() = _state.value.running

    // ---------- used by RPGM Tools ([RpgmJobs]) ----------

    /** Run another job using the same progress/summary/log. Errors are written to the log. */
    internal fun launchJob(totalBytes: Long, block: suspend CoroutineScope.() -> Unit) {
        startRun(totalBytes)
        job = scope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(str.logError(e.message ?: ""))
                _state.update { it.copy(running = false) }
            }
        }
    }

    internal fun finishJob(summary: Summary) {
        _state.update { it.copy(running = false, done = true, summary = summary, progress = 1f) }
    }

    internal fun clearResult() {
        resultUri = null
        _state.update { it.copy(done = false, summary = null, log = emptyList()) }
    }

    internal fun entryList(): List<Entry> = entries

    /** Document URI of the result folder (extract: subfolder named after the archive; pack: output folder). */
    fun resultFolder(): Uri? = resultUri

    internal fun treeDocUri(tree: Uri): Uri =
        DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))

    // ---------- input selection ----------

    /** [expectedId] = format opened from the engine menu; files of other formats are rejected. */
    /** Unpack options (e.g. name encoding). If they change and an archive is already chosen, the file list is reloaded. */
    fun setUnpackExtra(extra: Map<String, String>) {
        // Reload only when an option value actually changed. An empty/subset map (e.g. screen reopened
        // before the format was detected) must not reset a partial extraction selection.
        if (extra.none { (k, v) -> unpackExtra[k] != v }) return
        unpackExtra = unpackExtra + extra
        val u = archiveUri
        if (u != null) setArchive(u, expectedFormatId) else if (batchUris.size > 1) scanBatch(batchUris, expectedFormatId)
    }

    /** [expectedId] null = automatic Unpack Archive: any recognized format is accepted. */
    fun setArchive(uri: Uri, expectedId: String?) {
        expectedFormatId = expectedId
        archiveUri = uri
        batchUris = emptyList(); batchItems = emptyList()
        selection = null
        val name = displayName(uri)
        _state.update {
            it.copy(
                batchCount = 0, batchSkipped = 0,
                archiveName = name, archiveError = null, formatId = null, formatName = str.detecting, entryCount = 0,
                done = false, summary = null, log = emptyList(), keyChecked = false, detectedKey = null,
                scopePartial = false, selFiles = 0, selFolders = 0,
            )
        }
        loadJob?.cancel()
        loadJob = scope.launch {
            try {
                val pfd = app.contentResolver.openFileDescriptor(uri, "r") ?: error(str.errCannotOpen)
                ChannelSource(pfd).use { src ->
                    // Formats without magic bytes can look alike: check the format of the chosen engine first.
                    val want = expectedId?.let { FormatRegistry.byId(it) }
                    val fmt = if (want != null && runCatching { want.detects(src, src.readFully(0, minOf(16L, src.length).toInt())) }
                            .getOrDefault(false)) want else FormatRegistry.detect(src)
                    if (fmt == null || (expectedId != null && fmt.id != expectedId)) {
                        // not the chosen engine's format (or unknown)
                        format = null; entries = emptyList()
                        val msg = if (want == null) com.furuarc.i18n.ScriptStrings.of(lang).unknownArchive else str.wrongFile(fmt?.displayName, want.displayName)
                        _state.update { it.copy(formatName = null, archiveError = msg) }
                        log(msg)
                    } else {
                        // sorted so folder contents are in order (needed by the selection tree)
                        val list = fmt.list(src, Options(extra = unpackExtra + ("_arc" to name))).sortedBy { it.name }
                        val key = try {
                            fmt.detectKey(src, list)
                        } catch (e: Exception) {
                            null
                        }
                        format = fmt; entries = list
                        _state.update {
                            it.copy(
                                formatId = fmt.id, formatName = fmt.displayName, entryCount = list.size,
                                keyChecked = true, detectedKey = key,
                            )
                        }
                        log(str.logFound(fmt.displayName, list.size))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                format = null; entries = emptyList()
                _state.update { it.copy(formatName = str.readFailed, entryCount = 0) }
                log(str.logError(e.message ?: ""))
            }
        }
    }

    /** Several archives at once (multi-file pick): scanned one by one, then unpacked in order. */
    fun setArchives(uris: List<Uri>, expectedId: String?) {
        if (uris.size <= 1) {
            if (uris.size == 1) setArchive(uris[0], expectedId)
            return
        }
        expectedFormatId = expectedId
        archiveUri = null
        selection = null
        scanBatch(uris, expectedId)
    }

    private fun scanBatch(uris: List<Uri>, expectedId: String?) {
        batchUris = uris
        batchItems = emptyList()
        format = null; entries = emptyList()
        _state.update {
            it.copy(
                archiveName = str.r.batchSelected(uris.size), archiveError = null, formatId = null, formatName = str.detecting,
                entryCount = 0, batchCount = uris.size, batchSkipped = 0, done = false, summary = null, log = emptyList(),
                keyChecked = false, detectedKey = null, scopePartial = false, selFiles = 0, selFolders = 0,
            )
        }
        loadJob?.cancel()
        loadJob = scope.launch {
            val items = ArrayList<BatchItem>()
            var bad = 0
            for (uri in uris) {
                ensureActive()
                val name = displayName(uri)
                try {
                    val pfd = app.contentResolver.openFileDescriptor(uri, "r") ?: error(str.errCannotOpen)
                    ChannelSource(pfd).use { src ->
                        val want = expectedId?.let { FormatRegistry.byId(it) }
                        val fmt = if (want != null && runCatching { want.detects(src, src.readFully(0, minOf(16L, src.length).toInt())) }
                                .getOrDefault(false)) want else FormatRegistry.detect(src)
                        if (fmt == null || (expectedId != null && fmt.id != expectedId)) {
                            bad++
                            log(str.r.batchSkip(name, com.furuarc.i18n.ScriptStrings.of(lang).unknownArchive))
                        } else {
                            val list = fmt.list(src, Options(extra = unpackExtra + ("_arc" to name))).sortedBy { it.name }
                            items.add(BatchItem(uri, name, fmt, list))
                            log(str.logFound(name + " · " + fmt.displayName, list.size))
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    bad++
                    log(str.r.batchSkip(name, e.message ?: ""))
                }
            }
            batchItems = items
            if (items.isEmpty()) {
                _state.update { it.copy(formatName = null, archiveError = str.r.batchNone, batchSkipped = bad) }
            } else {
                val names = items.map { it.fmt.displayName }.distinct().joinToString(", ")
                val sameId = items.map { it.fmt.id }.distinct().singleOrNull()
                _state.update {
                    it.copy(
                        formatId = sameId, formatName = names, entryCount = items.sumOf { b -> b.entries.size },
                        batchCount = items.size, batchSkipped = bad,
                    )
                }
            }
        }
    }

    fun setOutputTree(uri: Uri) {
        outTree = uri
        _state.update { it.copy(outName = DocumentFile.fromTreeUri(app, uri)?.name ?: "folder") }
    }

    fun setPackOutput(uri: Uri) {
        packOutDir = uri
        _state.update { it.copy(packOutName = DocumentFile.fromTreeUri(app, uri)?.name ?: "folder") }
    }

    /** Pick the pack input folder, then scan its contents (count & size) in the background. */
    fun setPackFolder(uri: Uri) {
        packFolder = uri
        scanned = emptyList()
        _state.update {
            it.copy(
                packFolderName = DocumentFile.fromTreeUri(app, uri)?.name ?: "folder",
                packScanning = true, packFiles = 0, packBytes = 0, packIgnored = 0, done = false, summary = null,
            )
        }
        scanJob?.cancel()
        scanJob = scope.launch {
            try {
                val list = listTree(app, uri)
                scanned = list
                recomputePackStats()
                _state.update { it.copy(packScanning = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(packScanning = false) }
                log(str.logError(e.message ?: ""))
            }
        }
    }

    /** Ignore pattern changed: recompute the count/size of files to be included. */
    fun setIgnore(text: String) {
        ignore = IgnoreFilter(text)
        recomputePackStats()
    }

    private fun recomputePackStats() {
        val f = ignore
        var n = 0
        var bytes = 0L
        var skipped = 0
        for (x in scanned) {
            if (f.matches(x.path)) skipped++ else {
                n++
                bytes += x.size
            }
        }
        _state.update { it.copy(packFiles = n, packBytes = bytes, packIgnored = skipped) }
    }

    // ---------- partial selection ----------

    fun entryNames(): List<String> = entries.map { it.name }

    /** Copy of the current selection; null = everything. */
    fun selectionCopy(): BitSet? = (selection?.clone() as? BitSet)

    fun clearSelection() {
        selection = null
        _state.update { it.copy(scopePartial = false, selFiles = 0, selFolders = 0) }
    }

    fun applySelection(sel: BitSet) {
        val total = entries.size
        if (sel.cardinality() >= total) {
            clearSelection()
            return
        }
        selection = sel.clone() as BitSet
        val c = FileTree(entryNames()).counts(sel)
        _state.update { it.copy(scopePartial = true, selFiles = c.first, selFolders = c.second) }
    }

    // ---------- unpack ----------

    fun canUnpack(): Boolean {
        val s = _state.value
        return s.outName != null && s.entryCount > 0 && !s.running && (!s.scopePartial || s.selFiles > 0)
    }

    /** Unpack all archives from [setArchives] in order; each archive goes to a subfolder named after it. One failure does not stop the others. */
    private fun unpackBatch(xorKey: Int?, policy: ConflictPolicy, tree: Uri) {
        val items = batchItems
        if (items.isEmpty()) return
        val totalBytes = items.sumOf { b -> b.entries.sumOf { e -> e.size } }
        val totalCount = items.sumOf { b -> b.entries.size }
        resultUri = null
        val ctxByArchive = items.map { b -> VCtx(b.fmt, b.uri, xorKey) }
        run {
            val ctxs = ArrayList<VCtx>(); val ents = ArrayList<Entry>()
            for ((k, b) in items.withIndex()) { for (e in b.entries) { ctxs.add(ctxByArchive[k]); ents.add(e) } }
            lastCtx = ctxs; lastChosen = ents; lastWritten = arrayOfNulls(ents.size)
        }
        val batchWritten = lastWritten
        val batchBase = IntArray(items.size)
        for (k in 1 until items.size) batchBase[k] = batchBase[k - 1] + items[k - 1].entries.size
        verifyJob?.cancel()
        startRun(totalBytes)
        job = scope.launch {
            val t0 = System.currentTimeMillis()
            try {
                val writer = TreeWriter(app, tree)
                val finished = AtomicInteger(0)
                val failed = AtomicInteger(0)
                val skipped = AtomicInteger(0)
                val doneBytes = AtomicLong(0)
                val okBytes = AtomicLong(0)
                for ((ai, b) in items.withIndex()) {
                    ensureActive()
                    val chosen = b.entries
                    val rootName = b.name.replace('/', '_').replace('\\', '_').ifBlank { "archive" }
                    log(str.r.batchStart(ai + 1, items.size, b.name))
                    try {
                        val pfd = app.contentResolver.openFileDescriptor(b.uri, "r") ?: error(str.errCannotOpen)
                        ChannelSource(pfd).use { src ->
                            val key = xorKey ?: runCatching { b.fmt.detectKey(src, b.entries) }.getOrNull()?.takeIf { k -> k != 0 }
                            ctxByArchive[ai].key = key
                            val opts = Options(xorKey = key, extra = unpackExtra)
                            val next = AtomicInteger(0)
                            coroutineScope {
                                repeat(WORKERS) {
                                    launch {
                                        while (true) {
                                            ensureActive()
                                            val i = next.getAndIncrement()
                                            if (i >= chosen.size) break
                                            val e = chosen[i]
                                            try {
                                                val of = writer.open(rootName + "/" + e.name, policy)
                                                if (of == null) {
                                                    skipped.incrementAndGet()
                                                } else {
                                                    of.stream.use { o -> b.fmt.extract(src, e, o, opts) }
                                                    batchWritten[batchBase[ai] + i] = of.uri
                                                    okBytes.addAndGet(e.size)
                                                }
                                            } catch (ex: CancellationException) {
                                                throw ex
                                            } catch (ex: Exception) {
                                                failed.incrementAndGet()
                                                log(str.logFailed(e.name, ex.message ?: ""))
                                            }
                                            val bytes = doneBytes.addAndGet(e.size)
                                            report(finished.incrementAndGet(), totalCount, bytes, totalBytes, e.name, false)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (ex: CancellationException) {
                        throw ex
                    } catch (ex: Exception) {
                        // this archive cannot be opened: all its contents count as failed, continue with the next archive
                        failed.addAndGet(chosen.size)
                        finished.addAndGet(chosen.size)
                        doneBytes.addAndGet(chosen.sumOf { e -> e.size })
                        log(str.r.batchFailed(b.name, ex.message ?: ""))
                    }
                }
                report(totalCount, totalCount, totalBytes, totalBytes, "", true)
                val sec = (System.currentTimeMillis() - t0) / 1000.0
                val ok = totalCount - failed.get() - skipped.get()
                log(str.logDone(ok, failed.get(), sec))
                resultUri = treeDocUri(tree)
                _state.update { it.copy(running = false, done = true, summary = Summary(ok, failed.get(), skipped.get(), okBytes.get(), sec, false)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(str.logError(e.message ?: ""))
                _state.update { it.copy(running = false) }
            }
        }
    }

    fun unpack(xorKey: Int?, policy: ConflictPolicy) {
        val tree0 = outTree
        if (tree0 != null && batchItems.isNotEmpty()) {
            unpackBatch(xorKey, policy, tree0)
            return
        }
        val uri = archiveUri ?: return
        val tree = outTree ?: return
        val fmt = format ?: return
        val sel = selection
        val all = entries
        val chosen = if (sel == null) all else all.filterIndexed { i, _ -> sel.get(i) }
        if (chosen.isEmpty()) return
        val key = xorKey ?: _state.value.detectedKey?.takeIf { it != 0 }
        val opts = Options(xorKey = key, extra = unpackExtra)
        val totalBytes = chosen.sumOf { it.size }
        // Results go into a subfolder named after the archive: "Archive/" + root.pfs -> "Archive/root.pfs/"
        val rootName = (_state.value.archiveName ?: "archive").replace('/', '_').replace('\\', '_').ifBlank { "archive" }
        val written = arrayOfNulls<Uri>(chosen.size)
        val vc = VCtx(fmt, uri, key)
        lastCtx = List(chosen.size) { vc }; lastChosen = chosen; lastWritten = written; resultUri = null
        verifyJob?.cancel()

        startRun(totalBytes)
        job = scope.launch {
            val t0 = System.currentTimeMillis()
            try {
                val pfd = app.contentResolver.openFileDescriptor(uri, "r") ?: error(str.errCannotOpen)
                var summary: Summary? = null
                ChannelSource(pfd).use { src ->
                    val writer = TreeWriter(app, tree)
                    val next = AtomicInteger(0)
                    val finished = AtomicInteger(0)
                    val failed = AtomicInteger(0)
                    val skipped = AtomicInteger(0)
                    val doneBytes = AtomicLong(0)
                    val okBytes = AtomicLong(0)
                    // Several parallel workers: creating files via SAF is slow (IPC),
                    // so overlapping the work cuts total time.
                    coroutineScope {
                        repeat(WORKERS) {
                            launch {
                                while (true) {
                                    ensureActive()
                                    val i = next.getAndIncrement()
                                    if (i >= chosen.size) break
                                    val e = chosen[i]
                                    try {
                                        val of = writer.open(rootName + "/" + e.name, policy)
                                        if (of == null) {
                                            skipped.incrementAndGet()
                                        } else {
                                            of.stream.use { o -> fmt.extract(src, e, o, opts) }
                                            written[i] = of.uri
                                            okBytes.addAndGet(e.size)
                                        }
                                    } catch (ex: CancellationException) {
                                        throw ex
                                    } catch (ex: Exception) {
                                        failed.incrementAndGet()
                                        log(str.logFailed(e.name, ex.message ?: ""))
                                    }
                                    val b = doneBytes.addAndGet(e.size)
                                    report(finished.incrementAndGet(), chosen.size, b, totalBytes, e.name, false)
                                }
                            }
                        }
                    }
                    report(chosen.size, chosen.size, totalBytes, totalBytes, "", true)
                    val sec = (System.currentTimeMillis() - t0) / 1000.0
                    val ok = chosen.size - failed.get() - skipped.get()
                    log(str.logDone(ok, failed.get(), sec))
                    summary = Summary(ok, failed.get(), skipped.get(), okBytes.get(), sec, false)
                    resultUri = writer.dirUri(listOf(rootName)) ?: treeDocUri(tree)
                }
                _state.update { it.copy(running = false, done = true, summary = summary) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(str.logError(e.message ?: ""))
                _state.update { it.copy(running = false) }
            }
        }
    }

    // ---------- pack ----------

    fun canPack(): Boolean {
        val s = _state.value
        return s.packOutName != null && s.packFiles > 0 && !s.packScanning && !s.running
    }

    fun pack(formatId: String, fileName: String, extra: Map<String, String>, ignoreText: String) {
        val outDir = packOutDir ?: return
        val fmt = FormatRegistry.byId(formatId)
        val filter = IgnoreFilter(ignoreText)
        val safeName0 = fileName.trim().replace('/', '_').replace('\\', '_')
        // Output folder is the same as the input folder: do not include earlier result files in the new archive, and do not
        // overwrite source files that happen to have the same name (the result is saved under another name).
        val sameDir = packFolder?.let { f -> sameTree(f, outDir) } == true
        val inputs = scanned.filter { !filter.matches(it.path) && !(sameDir && it.path == safeName0) }
        if (inputs.isEmpty()) return
        val opts = Options(
            xorKey = extra["xor"]?.toIntOrNull()?.takeIf { it in 0..255 },
            compress = extra["compress"]?.let { it == "1" } ?: true,
            extra = extra,
        )
        val safeName = fileName.trim().replace('/', '_').replace('\\', '_')
        if (safeName.isEmpty()) return
        val ignored = scanned.size - inputs.size
        resultUri = null; lastPack = null

        // cumulative bytes per file, for percentage & remaining-time estimate
        val prefix = LongArray(inputs.size + 1)
        for (i in inputs.indices) prefix[i + 1] = prefix[i] + inputs[i].size
        val totalBytes = prefix[inputs.size]

        startRun(totalBytes)
        job = scope.launch {
            val t0 = System.currentTimeMillis()
            val tmp = File(app.cacheDir, "pack-${System.nanoTime()}.tmp")
            try {
                log(str.logPacking(inputs.size, fmt.displayName))
                fmt.pack(inputs, tmp, opts) { done, total, name ->
                    ensureActive() // so Cancel really stops the pack
                    report(done, total, prefix[done.coerceIn(0, inputs.size)], totalBytes, name, false)
                }
                ensureActive()
                log(str.logSaving)
                val out = TreeWriter(app, outDir).open(safeName, if (sameDir) ConflictPolicy.RENAME else ConflictPolicy.OVERWRITE)
                    ?: throw IOException(str.errWriteFailed)
                out.stream.use { o -> tmp.inputStream().use { it.copyTo(o, 1024 * 1024) } }
                // extra files made by the format (for example the new AGE index) are saved next to the archive
                File(tmp.path + ".x").listFiles()?.forEach { xf ->
                    TreeWriter(app, outDir).open(xf.name, ConflictPolicy.RENAME)?.stream?.use { o -> xf.inputStream().use { it.copyTo(o, 1024 * 1024) } }
                }
                lastPack = PackCtx(fmt, out.uri, inputs, opts)
                resultUri = treeDocUri(outDir)
                val size = tmp.length()
                val sec = (System.currentTimeMillis() - t0) / 1000.0
                report(inputs.size, inputs.size, totalBytes, totalBytes, "", true)
                log(str.logPackDone(size, sec))
                _state.update {
                    it.copy(running = false, done = true, summary = Summary(inputs.size, 0, ignored, size, sec, true))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                log(str.logError(e.message ?: ""))
                _state.update { it.copy(running = false) }
            } finally {
                tmp.delete()
                File(tmp.path + ".x").deleteRecursively()
            }
        }
    }

    private fun sameTree(a: Uri, b: Uri): Boolean = try {
        DocumentsContract.getTreeDocumentId(a) == DocumentsContract.getTreeDocumentId(b) && a.authority == b.authority
    } catch (e: Exception) {
        a == b
    }

    // ---------- verify extraction ----------

    private fun querySize(uri: Uri): Long = try {
        app.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) c.getLong(0) else -1L
        } ?: -1L
    } catch (e: Exception) {
        -1L
    }

    /**
     * Check extracted files: size must match the archive index; for XP3 the Adler32 of the file contents
     * is also computed and compared with the checksum in the index.
     */
    fun verify() {
        val list = lastChosen
        val uris = lastWritten
        val ctxs = lastCtx
        val st = _state.value
        if (st.running || st.verifying || list.isEmpty() || ctxs.size != list.size) return
        _state.update { it.copy(verifying = true, verifyPack = false, verifyDone = 0, verifyTotal = list.size, verifyResult = null) }
        verifyJob = scope.launch {
            var curCtx: VCtx? = null
            var curSrc: ChannelSource? = null
            try {
                var checked = 0; var sizeOk = 0; var sizeBad = 0; var unreadable = 0
                var sumChecked = 0; var sumOk = 0; var sumBad = 0
                var expected = 0L; var actual = 0L
                val problems = ArrayList<String>()
                val buf = ByteArray(256 * 1024)
                for (i in list.indices) {
                    ensureActive()
                    val uri = uris[i]
                    if (uri != null) {
                        val e = list[i]
                        val ctx = ctxs[i]
                        val want = ctx.fmt.expectedChecksum(e)
                        checked++
                        expected += e.size
                        try {
                            // one read of the result file: size + Adler32 (if the archive has a checksum) + CRC32 of contents
                            val ad = Adler32()
                            val crc = CRC32()
                            var size = -1L
                            app.contentResolver.openInputStream(uri)?.use { ins ->
                                var total = 0L
                                while (true) {
                                    val n = ins.read(buf)
                                    if (n < 0) break
                                    if (want != null) ad.update(buf, 0, n) else crc.update(buf, 0, n)
                                    total += n
                                }
                                size = total
                            }
                            if (size < 0) {
                                unreadable++
                                problems.add(str.vProblemUnreadable(e.name))
                            } else {
                                actual += size
                                if (size == e.size) sizeOk++ else {
                                    sizeBad++
                                    problems.add(str.vProblemSize(e.name, formatBytes(size), formatBytes(e.size)))
                                }
                                if (want != null) {
                                    sumChecked++
                                    if (ad.value == want) sumOk++ else {
                                        sumBad++
                                        problems.add(str.vProblemChecksum(e.name))
                                    }
                                } else {
                                    // Archive without checksum: re-extract to memory (CRC32) and compare with the result file.
                                    val re = reextractCrc(ctx, e, curCtx, curSrc)
                                    curCtx = re.second; curSrc = re.third
                                    val c = re.first
                                    if (c != null) {
                                        sumChecked++
                                        if (c == crc.value) sumOk++ else {
                                            sumBad++
                                            problems.add(str.vProblemChecksum(e.name))
                                        }
                                    }
                                }
                            }
                        } catch (ex: CancellationException) {
                            throw ex
                        } catch (ex: Exception) {
                            unreadable++
                            problems.add(str.vProblemUnreadable(e.name))
                        }
                    }
                    if (i % 10 == 0) _state.update { it.copy(verifyDone = i + 1) }
                }
                for (line in problems.take(200)) log(line)
                val r = VerifyResult(
                    checked, sizeOk, sizeBad, unreadable, sumChecked, sumOk, sumBad,
                    expected, actual, problems.take(200),
                )
                _state.update { it.copy(verifying = false, verifyDone = list.size, verifyResult = r) }
            } catch (e: CancellationException) {
                _state.update { it.copy(verifying = false) }
                throw e
            } finally {
                runCatching { curSrc?.close() }
            }
        }
    }

    /** CRC32 of [e] contents re-extracted from the archive (null if it cannot be re-extracted). Also returns the source currently open. */
    private fun reextractCrc(ctx: VCtx, e: Entry, curCtx: VCtx?, curSrc: ChannelSource?): Triple<Long?, VCtx?, ChannelSource?> {
        var cc = curCtx; var cs = curSrc
        if (cc !== ctx || cs == null) {
            runCatching { cs?.close() }
            cs = null
            cc = ctx
            cs = try {
                ChannelSource(app.contentResolver.openFileDescriptor(ctx.archive, "r") ?: return Triple(null, cc, null))
            } catch (ex: Exception) {
                return Triple(null, cc, null)
            }
        }
        val crc = CRC32()
        val sink = object : java.io.OutputStream() {
            override fun write(b: Int) { crc.update(b) }
            override fun write(b: ByteArray, off: Int, len: Int) { crc.update(b, off, len) }
        }
        return try {
            ctx.fmt.extract(cs!!, e, sink, Options(xorKey = ctx.key, extra = unpackExtra))
            Triple(crc.value, cc, cs)
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            Triple(null, cc, cs)
        }
    }

    /** Post-pack integrity check: re-read the result archive, extract each file to memory, compare with the source folder. */
    fun verifyPack() {
        val pc = lastPack ?: return
        val st = _state.value
        if (st.running || st.verifying) return
        val inputs = pc.inputs
        _state.update { it.copy(verifying = true, verifyPack = true, verifyDone = 0, verifyTotal = inputs.size, verifyResult = null) }
        val x = str.x
        verifyJob = scope.launch {
            try {
                var sizeOk = 0; var sizeBad = 0; var missing = 0
                var sumChecked = 0; var sumOk = 0; var sumBad = 0
                var expected = 0L; var actual = 0L
                val problems = ArrayList<String>()
                withContext(Dispatchers.IO) {
                    val pfd = app.contentResolver.openFileDescriptor(pc.uri, "r") ?: error(str.errCannotOpen)
                    ChannelSource(pfd).use { src ->
                        val opts = Options(xorKey = pc.opts.xorKey, extra = pc.opts.extra)
                        val list = pc.fmt.list(src, opts)
                        fun norm(n: String) = n.replace('\\', '/').trimStart('/')
                        val byName = HashMap<String, Entry>()
                        val byLower = HashMap<String, Entry>()
                        for (e in list) { byName.putIfAbsent(norm(e.name), e); byLower.putIfAbsent(norm(e.name).lowercase(), e) }
                        val used = HashSet<Entry>()
                        val buf = ByteArray(256 * 1024)
                        for ((i, f) in inputs.withIndex()) {
                            ensureActive()
                            val key = norm(f.path)
                            val e = byName[key] ?: byLower[key.lowercase()]
                            expected += f.size
                            if (e == null) {
                                missing++
                                problems.add(x.vPackMissingFile(f.path))
                            } else {
                                used.add(e)
                                if (e.size == f.size) sizeOk++ else {
                                    sizeBad++
                                    problems.add(x.vPackSize(f.path, formatBytes(e.size), formatBytes(f.size)))
                                }
                                try {
                                    val c1 = CRC32()
                                    f.open().use { ins ->
                                        while (true) { val n = ins.read(buf); if (n < 0) break; c1.update(buf, 0, n) }
                                    }
                                    val c2 = CRC32()
                                    val sink = object : java.io.OutputStream() {
                                        var total = 0L
                                        override fun write(b: Int) { c2.update(b); total++ }
                                        override fun write(b: ByteArray, off: Int, len: Int) { c2.update(b, off, len); total += len }
                                    }
                                    pc.fmt.extract(src, e, sink, opts)
                                    actual += sink.total
                                    sumChecked++
                                    if (c1.value == c2.value) sumOk++ else {
                                        sumBad++
                                        problems.add(x.vPackContentBad(f.path))
                                    }
                                } catch (ex: CancellationException) {
                                    throw ex
                                } catch (ex: Exception) {
                                    missing++
                                    problems.add(x.vPackMissingFile(f.path) + " (" + (ex.message ?: "") + ")")
                                }
                            }
                            if (i % 5 == 0) _state.update { it.copy(verifyDone = i + 1) }
                        }
                        for (e in list) if (e !in used) problems.add(x.vPackExtra(e.name))
                    }
                }
                for (line in problems.take(200)) log(line)
                val r = VerifyResult(inputs.size, sizeOk, sizeBad, missing, sumChecked, sumOk, sumBad, expected, actual, problems.take(200))
                _state.update { it.copy(verifying = false, verifyDone = inputs.size, verifyResult = r) }
            } catch (e: CancellationException) {
                _state.update { it.copy(verifying = false) }
                throw e
            } catch (e: Throwable) {
                log(str.logError(e.message ?: ""))
                _state.update { it.copy(verifying = false) }
            }
        }
    }

    // ---------- preview ----------

    /** Read one archive file into memory for preview (without writing to storage). Call on the IO thread. */
    fun loadPreview(index: Int, maxBytes: Long): PreviewData {
        val fmt = format
        val uri = archiveUri
        val e = entries.getOrNull(index)
        if (fmt == null || uri == null || e == null) return PreviewData("", 0, null, str.previewError)
        if (e.size > maxBytes) return PreviewData(e.name, e.size, null, str.previewTooBig(formatBytes(maxBytes)))
        return try {
            val pfd = app.contentResolver.openFileDescriptor(uri, "r") ?: error(str.errCannotOpen)
            ChannelSource(pfd).use { src ->
                val bo = ByteArrayOutputStream()
                val key = _state.value.detectedKey?.takeIf { it != 0 }
                fmt.extract(src, e, bo, Options(xorKey = key, extra = unpackExtra))
                PreviewData(e.name, e.size, bo.toByteArray(), null)
            }
        } catch (ex: Exception) {
            PreviewData(e.name, e.size, null, ex.message ?: str.previewError)
        }
    }

    // ---------- util ----------

    internal fun startRun(totalBytes: Long) {
        job?.cancel()
        verifyJob?.cancel()
        startMs = System.currentTimeMillis()
        _state.update {
            it.copy(
                running = true, done = false, progress = 0f, current = "", doneBytes = 0,
                totalBytes = totalBytes, etaSec = -1, summary = null, log = emptyList(),
                verifying = false, verifyDone = 0, verifyTotal = 0, verifyResult = null, verifyPack = false,
            )
        }
        // the service is started AFTER running = true so it shows the progress notification immediately.
        // Only if "Run in Background" is enabled in Settings.
        if (bgEnabled) ProcessService.start(app)
    }

    /** Progress updates are limited to ~10x/second so the UI is not overwhelmed. */
    internal fun report(done: Int, total: Int, bytes: Long, totalBytes: Long, name: String, force: Boolean) {
        if (!force) {
            val now = System.currentTimeMillis()
            val prev = lastProgress.get()
            if (now - prev < 100 || !lastProgress.compareAndSet(prev, now)) return
        }
        val frac: Float = when {
            totalBytes > 0 -> (bytes.toDouble() / totalBytes).toFloat()
            total > 0 -> done / total.toFloat()
            else -> 0f
        }.coerceIn(0f, 1f)
        val elapsed = System.currentTimeMillis() - startMs
        val eta: Long = if (!force && frac > 0.02f) {
            val f = frac.toDouble()
            (elapsed * (1.0 - f) / f / 1000.0).toLong()
        } else {
            -1L
        }
        _state.update {
            it.copy(
                progress = frac,
                doneBytes = bytes,
                etaSec = eta,
                current = if (name.isEmpty()) it.current else name,
            )
        }
    }

    internal fun log(msg: String) {
        _state.update { it.copy(log = (it.log + msg).takeLast(1000)) }
    }

    internal fun displayName(uri: Uri): String =
        app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        } ?: uri.lastPathSegment ?: "file"

    private const val WORKERS = 4
}
