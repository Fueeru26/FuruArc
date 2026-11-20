package com.furuarc

import android.net.Uri
import android.provider.DocumentsContract
import com.furuarc.core.*
import com.furuarc.settings.Lang
import com.furuarc.storage.ChannelSource
import com.furuarc.ui.RPGM_ENGINES
import com.furuarc.ui.VN_ENGINES
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.util.zip.CRC32
import kotlin.coroutines.coroutineContext

private val id: Boolean get() = Engine.lang == Lang.ID

private fun openSrc(u: Uri): ChannelSource {
    val pfd = Engine.app.contentResolver.openFileDescriptor(u, "r") ?: throw java.io.IOException("Tidak bisa membuka file")
    return ChannelSource(pfd)
}

private fun enginesOf(f: ArchiveFormat): String =
    (VN_ENGINES + RPGM_ENGINES).filter { it.formatId == f.id }.map { it.name }.distinct().joinToString(", ")

private fun msg(e: Throwable) = e.message ?: e.javaClass.simpleName

// ============================ Folder Check ============================

class FolderItem(
    val path: String,
    val size: Long,
    val uri: Uri,
    val kind: String,
    val verdict: Verdict,
    val summary: String,
    val findings: List<Finding>,
    val engines: String = "", // engine(s) owning the detected archive formats (separated by ", ")
)

data class FolderState(
    val name: String? = null,
    val running: Boolean = false,
    val done: Int = 0,
    val total: Int = 0,
    val items: List<FolderItem> = emptyList(),
    val skipped: Int = 0,
    val remaining: Int = 0, // files not yet checked (continue with the next batch)
    val stats: FolderStats? = null,
    val error: String? = null,
)

object FolderJobs {
    private val _state = MutableStateFlow(FolderState())
    val state: StateFlow<FolderState> = _state.asStateFlow()
    private var job: Job? = null

    private const val BATCH = 5000
    private const val HEAD = 256 * 1024
    private val MEDIA = setOf(
        "png", "jpg", "jpeg", "gif", "bmp", "webp", "ogg", "mp3", "wav", "m4a", "flac", "mp4", "webm", "avi", "mkv", "wmv",
        "ttf", "otf", "ico", "svg", "ogv", "mid", "midi", "wma", "mpg", "flv",
    )

    private class FileRef(val path: String, val size: Long, val uri: Uri)

    private var allRefs: List<FileRef> = emptyList()
    private var nextIdx = 0
    private val acc = ArrayList<FolderItem>()
    private var skippedAcc = 0
    private val engineFiles = LinkedHashMap<String, MutableList<String>>()

    /** List of ALL files (no limit) and the folder count; one query per folder. */
    private fun listFiles(tree: Uri, out: MutableList<FileRef>): Int {
        val resolver = Engine.app.contentResolver
        val proj = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE,
        )
        var folders = 0
        fun walk(docId: String, prefix: String) {
            val kids = DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
            val dirs = ArrayList<Pair<String, String>>()
            resolver.query(kids, proj, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val did = c.getString(0) ?: continue
                    val nm = c.getString(1) ?: continue
                    if (c.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) dirs.add(Pair(did, nm))
                    else out.add(FileRef(prefix + nm, if (c.isNull(3)) 0L else c.getLong(3), DocumentsContract.buildDocumentUriUsingTree(tree, did)))
                }
            }
            folders += dirs.size
            for ((d, n) in dirs) walk(d, "$prefix$n/")
        }
        walk(DocumentsContract.getTreeDocumentId(tree), "")
        return folders
    }

    fun cancel() { job?.cancel(); job = null }
    fun clear() { cancel(); _state.value = FolderState() }

    fun scan(tree: Uri) {
        cancel()
        val title = DocumentsContract.getTreeDocumentId(tree).substringAfterLast(':').ifEmpty { "/" }
        _state.value = FolderState(name = title, running = true)
        acc.clear(); skippedAcc = 0; nextIdx = 0; allRefs = emptyList(); engineFiles.clear()
        job = Engine.scope.launch {
            try {
                val refs = ArrayList<FileRef>()
                val folders = listFiles(tree, refs)
                refs.sortBy { it.path.lowercase() }
                allRefs = refs
                val stats = FolderAnalyzer.analyze(refs.map { Pair(it.path, it.size) }, folders, id)
                _state.update { it.copy(total = refs.size, stats = stats, remaining = refs.size) }
                runBatch()
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(running = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(running = false, error = msg(e)) }
            }
        }
    }

    /** Continue checking the next batch of files (5000 files per batch). */
    fun scanMore() {
        val st = _state.value
        if (st.running || st.remaining <= 0 || allRefs.isEmpty()) return
        _state.update { it.copy(running = true) }
        job = Engine.scope.launch {
            try {
                runBatch()
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(running = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(running = false, error = msg(e)) }
            }
        }
    }

    private suspend fun runBatch() {
        val ctx = coroutineContext
        val end = minOf(allRefs.size, nextIdx + BATCH)
        var i = nextIdx
        while (i < end) {
            ctx.ensureActive()
            val r = allRefs[i]
            val ext = r.path.substringAfterLast('.', "").lowercase()
            if (ext in MEDIA) {
                skippedAcc++
            } else {
                val it = checkOne(r)
                acc.add(it)
                if (it.engines.isNotEmpty()) for (e in it.engines.split(", ")) engineFiles.getOrPut(e) { ArrayList() }.add(r.path)
            }
            i++
            nextIdx = i
            if (i % 5 == 0 || i == end) {
                val snap = acc.sortedWith(compareByDescending<FolderItem> { it.verdict.level }.thenBy { it.path.lowercase() })
                _state.update { it.copy(done = i, items = snap, skipped = skippedAcc, remaining = allRefs.size - i) }
            }
        }
        // merge engines from the file contents (detected archive formats) into the summary
        val base = FolderAnalyzer.analyze(allRefs.map { Pair(it.path, it.size) }, _state.value.stats?.folders ?: 0, id, engineFiles)
        _state.update { it.copy(running = false, done = i, skipped = skippedAcc, remaining = allRefs.size - i, stats = base) }
    }

    private fun checkOne(r: FileRef): FolderItem {
        return try {
            openSrc(r.uri).use { src ->
                val f = FormatRegistry.detect(src)
                val rep: Report
                var kind: String
                if (f != null) {
                    kind = f.displayName
                    rep = try {
                        val list = f.list(src, Options()).sortedBy { it.name }
                        val key = try { f.detectKey(src, list) } catch (e: Exception) { null }
                        Inspector.archive(r.path, src, f, list, key, enginesOf(f), id)
                    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        Inspector.brokenArchive(src, f, enginesOf(f), msg(e), id)
                    }
                } else {
                    val n = minOf(src.length, HEAD.toLong()).toInt()
                    val head = if (n > 0) src.readFully(0, n) else ByteArray(0)
                    val rf = Inspector.file(r.path, head, src.length, id, partial = true)
                    val known = rf.info.any { it.first == "Engine yang didukung" || it.first == "Supported engine" }
                    rep = Report(rf.info, if (known) rf.findings else rf.findings + InspectJobs.engineHints(r.path, id))
                    kind = r.path.substringAfterLast('.', "").lowercase().let { if (it.isEmpty()) "-" else ".$it" }
                }
                val v = verdictOf(rep.findings)
                val worst = rep.findings.firstOrNull { it.level == 3 } ?: rep.findings.firstOrNull { it.level == 2 }
                FolderItem(r.path, r.size, r.uri, kind, v, worst?.text ?: (if (id) "Sehat" else "Healthy"), rep.findings, if (f != null) enginesOf(f) else "")
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (e: Throwable) {
            val f = Finding(3, (if (id) "Gagal membaca file: " else "Could not read the file: ") + msg(e))
            FolderItem(r.path, r.size, r.uri, "?", Verdict(3, 0, 1), f.text, listOf(f))
        }
    }

    fun reportText(): String {
        val st = _state.value
        val sb = ReportText.header(if (id) "Laporan Pemeriksaan Folder" else "Folder Inspection Report", id)
        sb.append(if (id) "Folder: " else "Folder: ").append(st.name ?: "").append('\n')
        val ok = st.items.count { it.verdict.level == 1 }
        val w = st.items.count { it.verdict.level == 2 }
        val b = st.items.count { it.verdict.level == 3 }
        ReportText.section(sb, if (id) "Ringkasan" else "Summary")
        sb.append(if (id) "Diperiksa: " else "Checked: ").append(st.items.size).append('\n')
        sb.append(if (id) "Sehat: " else "Healthy: ").append(ok).append('\n')
        sb.append(if (id) "Peringatan: " else "Warnings: ").append(w).append('\n')
        sb.append(if (id) "Masalah: " else "Problems: ").append(b).append('\n')
        sb.append(if (id) "Dilewati (gambar/audio/video biasa): " else "Skipped (plain image/audio/video): ").append(st.skipped).append('\n')
        st.stats?.let { x ->
            ReportText.section(sb, if (id) "Isi folder" else "Folder contents")
            sb.append(if (id) "Ukuran total: " else "Total size: ").append(formatBytes(x.bytes)).append('\n')
            sb.append(if (id) "Jumlah file: " else "Files: ").append(x.files).append(if (id) ", folder: " else ", folders: ").append(x.folders).append('\n')
            if (x.largest.isNotEmpty()) {
                sb.append(if (id) "File terbesar:\n" else "Largest files:\n")
                for ((n, sz) in x.largest) sb.append("  ").append(n).append(" (").append(formatBytes(sz)).append(")\n")
            }
            if (x.exts.isNotEmpty()) sb.append(if (id) "Ekstensi terbanyak: " else "Top extensions: ").append(x.exts.joinToString(", ") { it.first + " ×" + it.second }).append('\n')
            if (x.engines.isNotEmpty()) {
                sb.append(if (id) "Engine terdeteksi:\n" else "Detected engines:\n")
                for (e in x.engines) sb.append("  ").append(e.name).append(" (").append(e.count).append(if (id) " file: " else " files: ").append(e.samples.joinToString(", ")).append(")\n")
            }
        }
        if (st.remaining > 0) sb.append(if (id) "Belum diperiksa: " else "Not yet checked: ").append(st.remaining).append(if (id) " file (batch berikutnya)\n" else " files (next batch)\n")
        ReportText.section(sb, if (id) "Daftar file" else "Files")
        for (it in st.items) {
            sb.append(ReportText.mark(it.verdict.level)).append(' ').append(it.path).append(" (").append(formatBytes(it.size)).append(", ").append(it.kind).append(")\n")
            for (f in it.findings) if (f.level >= 2) sb.append("      ").append(ReportText.mark(f.level)).append(' ').append(f.text).append('\n')
        }
        return sb.toString()
    }
}

// ============================ Compare Checks ============================

data class CompareState(
    val nameA: String? = null,
    val nameB: String? = null,
    val deep: Boolean = false,
    val running: Boolean = false,
    val progress: Float = 0f,
    val label: String = "",
    val kind: Int = 0, // 1 archive, 2 text, 3 translation, 4 binary file
    val diff: DiffResult? = null,
    val text: TextDiff? = null,
    val tr: TrDiff? = null,
    val bin: List<Pair<String, String>>? = null,
    val binSame: Boolean = false,
    val error: String? = null,
)

object CompareJobs {
    private val _state = MutableStateFlow(CompareState())
    val state: StateFlow<CompareState> = _state.asStateFlow()
    private var job: Job? = null
    private var uriA: Uri? = null
    private var uriB: Uri? = null

    private const val TEXT_MAX = 8 * 1024 * 1024

    private class CrcSink : OutputStream() {
        val crc = CRC32()
        override fun write(b: Int) = crc.update(b)
        override fun write(b: ByteArray, off: Int, len: Int) = crc.update(b, off, len)
    }

    fun cancel() { job?.cancel(); job = null }
    fun clear() { cancel(); uriA = null; uriB = null; _state.value = CompareState() }

    private fun reset(block: CompareState.() -> CompareState) {
        _state.update { it.block().copy(kind = 0, diff = null, text = null, tr = null, bin = null, error = null) }
    }

    fun setA(u: Uri) { uriA = u; reset { copy(nameA = Engine.displayName(u)) } }
    fun setB(u: Uri) { uriB = u; reset { copy(nameB = Engine.displayName(u)) } }
    fun setDeep(v: Boolean) { _state.update { it.copy(deep = v) } }

    fun run() {
        val ua = uriA ?: return
        val ub = uriB ?: return
        if (_state.value.running) return
        val deep = _state.value.deep
        val nameA = _state.value.nameA ?: ""
        val nameB = _state.value.nameB ?: ""
        reset { copy(running = true, progress = 0f, label = "") }
        job = Engine.scope.launch {
            val ctx = coroutineContext
            try {
                openSrc(ua).use { a ->
                    openSrc(ub).use { b ->
                        val fa = FormatRegistry.detect(a)
                        val fb = FormatRegistry.detect(b)
                        when {
                            fa != null && fb != null -> archives(a, b, fa, fb, deep) { ctx.ensureActive() }
                            fa != null || fb != null -> _state.update {
                                it.copy(running = false, error = if (id) "Jenis kedua file berbeda: satu arsip, satu bukan arsip." else "The two files are of different kinds: one is an archive, the other is not.")
                            }
                            else -> plain(a, b, nameA, nameB)
                        }
                    }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(running = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(running = false, error = msg(e)) }
            }
        }
    }

    private fun archives(a: ChannelSource, b: ChannelSource, fa: ArchiveFormat, fb: ArchiveFormat, deep: Boolean, check: () -> Unit) {
        val la = fa.list(a, Options()).sortedBy { it.name }
        val lb = fb.list(b, Options()).sortedBy { it.name }
        val ka = try { fa.detectKey(a, la) } catch (e: Exception) { null }
        val kb = try { fb.detectKey(b, lb) } catch (e: Exception) { null }
        var n = 0
        val same: ((Entry, Entry) -> Boolean)? = if (!deep) null else fun(ea: Entry, eb: Entry): Boolean {
            n++
            _state.update { it.copy(progress = n.toFloat() / la.size.coerceAtLeast(1), label = ea.name) }
            val sa = CrcSink(); fa.extract(a, ea, sa, Options(xorKey = ka))
            val sb = CrcSink(); fb.extract(b, eb, sb, Options(xorKey = kb))
            return sa.crc.value == sb.crc.value
        }
        val d = Compare.archives(la, lb, same, check)
        _state.update { it.copy(running = false, kind = 1, diff = d) }
    }

    private fun readAll(src: ChannelSource): ByteArray = src.readFully(0, minOf(src.length, TEXT_MAX.toLong()).toInt())

    private fun plain(a: ChannelSource, b: ChannelSource, nameA: String, nameB: String) {
        val ba = readAll(a)
        val bb = readAll(b)
        val trExt = setOf("csv", "txt", "json")
        val extA = nameA.substringAfterLast('.', "").lowercase()
        val extB = nameB.substringAfterLast('.', "").lowercase()
        val binary = ba.take(4096).any { it.toInt() == 0 } || bb.take(4096).any { it.toInt() == 0 }
        if (!binary && extA in trExt && extB in trExt) {
            try {
                val ta = RpgmText.readEntriesAny(nameA, String(ba, Charsets.UTF_8)).first.items
                val tb = RpgmText.readEntriesAny(nameB, String(bb, Charsets.UTF_8)).first.items
                if (ta.isNotEmpty() && tb.isNotEmpty()) {
                    _state.update { it.copy(running = false, kind = 3, tr = Compare.translations(ta, tb)) }
                    return
                }
            } catch (e: Exception) { /* not a translation file: continue as text */ }
        }
        if (!binary) {
            val da = ScriptText.decode(ScriptKind.RPY, ba).text
            val db = ScriptText.decode(ScriptKind.RPY, bb).text
            _state.update { it.copy(running = false, kind = 2, text = Compare.text(da, db)) }
            return
        }
        val ha = Inspector.hashes(a, { }, { })
        val hb = Inspector.hashes(b, { }, { })
        val rows = listOf(
            Pair(if (id) "Ukuran A" else "Size A", "${formatBytes(a.length)} (${a.length} B)"),
            Pair(if (id) "Ukuran B" else "Size B", "${formatBytes(b.length)} (${b.length} B)"),
            Pair("MD5 A", ha.md5), Pair("MD5 B", hb.md5),
            Pair("SHA-256 A", ha.sha256), Pair("SHA-256 B", hb.sha256),
        )
        _state.update { it.copy(running = false, kind = 4, bin = rows, binSame = ha.sha256 == hb.sha256) }
    }

    fun reportText(): String {
        val st = _state.value
        val sb = ReportText.header(if (id) "Laporan Bandingkan Pemeriksaan" else "Comparison Report", id)
        sb.append("A: ").append(st.nameA ?: "").append('\n').append("B: ").append(st.nameB ?: "").append('\n')
        when (st.kind) {
            1 -> st.diff?.let { d ->
                ReportText.section(sb, if (id) "Ringkasan" else "Summary")
                sb.append(if (id) "Ditambah: " else "Added: ").append(d.count(DiffItem.ADDED)).append('\n')
                sb.append(if (id) "Berubah: " else "Changed: ").append(d.count(DiffItem.CHANGED)).append('\n')
                sb.append(if (id) "Dihapus: " else "Removed: ").append(d.count(DiffItem.REMOVED)).append('\n')
                sb.append(if (id) "Sama: " else "Same: ").append(d.count(DiffItem.SAME)).append('\n')
                sb.append(if (id) "Mode: " else "Mode: ").append(if (st.deep) (if (id) "teliti (isi dibandingkan)" else "thorough (content compared)") else (if (id) "cepat (nama dan ukuran)" else "fast (name and size)")).append('\n')
                ReportText.section(sb, if (id) "Perbedaan" else "Differences")
                for (x in d.items) if (x.kind != DiffItem.SAME) {
                    val m = when (x.kind) { DiffItem.ADDED -> "+"; DiffItem.REMOVED -> "-"; else -> "~" }
                    sb.append(m).append(' ').append(x.name).append("  ").append(
                        when (x.kind) { DiffItem.ADDED -> formatBytes(x.sizeB); DiffItem.REMOVED -> formatBytes(x.sizeA); else -> formatBytes(x.sizeA) + " -> " + formatBytes(x.sizeB) },
                    ).append('\n')
                }
            }
            2 -> st.text?.let { t ->
                ReportText.section(sb, if (id) "Ringkasan" else "Summary")
                sb.append(if (id) "Baris A: " else "Lines A: ").append(t.linesA).append('\n')
                sb.append(if (id) "Baris B: " else "Lines B: ").append(t.linesB).append('\n')
                sb.append(if (id) "Sama: " else "Same: ").append(t.same).append('\n')
                sb.append(if (id) "Ditambah di B: " else "Added in B: ").append(t.added.size).append('\n')
                sb.append(if (id) "Hilang dari A: " else "Removed from A: ").append(t.removed.size).append('\n')
                ReportText.section(sb, if (id) "Baris ditambah" else "Added lines")
                for (l in t.added.take(500)) sb.append("+ ").append(l).append('\n')
                ReportText.section(sb, if (id) "Baris hilang" else "Removed lines")
                for (l in t.removed.take(500)) sb.append("- ").append(l).append('\n')
            }
            3 -> st.tr?.let { t ->
                ReportText.section(sb, if (id) "Ringkasan" else "Summary")
                sb.append(if (id) "Entri A/B: " else "Entries A/B: ").append(t.totalA).append(" / ").append(t.totalB).append('\n')
                sb.append(if (id) "Terisi A/B: " else "Filled A/B: ").append(t.filledA).append(" / ").append(t.filledB).append('\n')
                sb.append(if (id) "Entri baru: " else "New entries: ").append(t.count(TrDiffItem.ADDED)).append('\n')
                sb.append(if (id) "Entri hilang: " else "Removed entries: ").append(t.count(TrDiffItem.REMOVED)).append('\n')
                sb.append(if (id) "Teks asli berubah: " else "Original changed: ").append(t.count(TrDiffItem.ORIG)).append('\n')
                sb.append(if (id) "Terjemahan berubah: " else "Translation changed: ").append(t.count(TrDiffItem.TR)).append('\n')
                ReportText.section(sb, if (id) "Perbedaan" else "Differences")
                for (x in t.items.take(1000)) sb.append("* ").append(x.file).append(" | ").append(x.path).append('\n').append("  A: ").append(x.a.replace("\n", "\\n")).append('\n').append("  B: ").append(x.b.replace("\n", "\\n")).append('\n')
            }
            4 -> {
                sb.append(if (st.binSame) (if (id) "HASIL: kedua file identik\n" else "RESULT: both files are identical\n") else (if (id) "HASIL: kedua file berbeda\n" else "RESULT: the files differ\n"))
                for ((k, v) in st.bin ?: emptyList()) sb.append(k).append(": ").append(v).append('\n')
            }
        }
        return sb.toString()
    }
}

// ============================ Translation Scan ============================

data class TrScanState(
    val name: String? = null,
    val busy: Boolean = false,
    val maxLen: Int = 40,
    val result: TrScanResult? = null,
    val error: String? = null,
)

object TrScanJobs {
    private val _state = MutableStateFlow(TrScanState())
    val state: StateFlow<TrScanState> = _state.asStateFlow()
    private var items: List<TrItem> = emptyList()
    private var job: Job? = null

    private const val MAX_BYTES = 64 * 1024 * 1024

    fun clear() { job?.cancel(); items = emptyList(); _state.value = TrScanState() }

    fun open(u: Uri) {
        job?.cancel()
        val name = Engine.displayName(u)
        val max = _state.value.maxLen
        _state.value = TrScanState(name = name, busy = true, maxLen = max)
        job = Engine.scope.launch {
            try {
                val bytes = Engine.app.contentResolver.openInputStream(u)?.use { it.readBytes() } ?: throw java.io.IOException("Tidak bisa membuka file")
                if (bytes.size > MAX_BYTES) throw java.io.IOException(if (id) "File terlalu besar untuk dipindai." else "File too large to scan.")
                val text = String(bytes, Charsets.UTF_8)
                val parsed = RpgmText.readEntriesAny(name, text).first.items
                if (parsed.isEmpty()) throw IllegalArgumentException(if (id) "Tidak ada entri terjemahan. Pilih hasil ekspor (CSV/TXT/JSON) dari FuruArc." else "No translation entries found. Pick an export (CSV/TXT/JSON) made by FuruArc.")
                items = parsed
                _state.update { it.copy(busy = false, result = TrScan.scan(parsed, it.maxLen)) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = msg(e)) }
            }
        }
    }

    fun setMax(n: Int) {
        if (items.isEmpty()) { _state.update { it.copy(maxLen = n) }; return }
        _state.update { it.copy(maxLen = n, result = TrScan.scan(items, n)) }
    }

    fun reportText(): String {
        val st = _state.value
        val r = st.result ?: return ""
        return TrScan.report(id, st.name ?: "", r, st.maxLen)
    }
}
