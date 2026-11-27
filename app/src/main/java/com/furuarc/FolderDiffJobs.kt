package com.furuarc

import android.net.Uri
import com.furuarc.core.ConflictPolicy
import com.furuarc.core.PackInput
import com.furuarc.storage.TreeWriter
import com.furuarc.storage.listTree
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.zip.CRC32
import kotlin.coroutines.coroutineContext

/** Result of comparing two folders (paths are relative to each folder). */
data class FolderDiffState(
    val nameA: String? = null,
    val nameB: String? = null,
    val nameOut: String? = null,
    val deep: Boolean = true,
    val running: Boolean = false,
    val progress: Float = 0f,
    val label: String = "",
    /** true once a comparison has finished */
    val done: Boolean = false,
    val changed: List<String> = emptyList(),
    val added: List<String> = emptyList(),
    val removed: List<String> = emptyList(),
    val same: Int = 0,
    val copying: Boolean = false,
    val copied: Int = 0,
    val copyFailed: Int = 0,
    val copyDone: Boolean = false,
    val error: String? = null,
)

/**
 * Compares an original folder (A) with an edited folder (B): which files changed, which are new in B and which are missing.
 * The changed and new files can be copied into an output folder (keeping their sub-folders), for example to build a small patch.
 */
object FolderDiffJobs {
    private val _state = MutableStateFlow(FolderDiffState())
    val state: StateFlow<FolderDiffState> = _state.asStateFlow()
    private var job: Job? = null
    private var uriA: Uri? = null
    private var uriB: Uri? = null
    var outUri: Uri? = null
        private set
    private var filesB: Map<String, PackInput> = emptyMap()

    fun cancel() { job?.cancel(); job = null }
    fun clear() {
        cancel(); uriA = null; uriB = null; outUri = null; filesB = emptyMap()
        _state.value = FolderDiffState()
    }

    private fun resetResult() {
        _state.update { it.copy(done = false, changed = emptyList(), added = emptyList(), removed = emptyList(), same = 0, copied = 0, copyFailed = 0, copyDone = false, error = null, progress = 0f, label = "") }
    }

    fun setA(u: Uri) { uriA = u; _state.update { it.copy(nameA = folderName(u)) }; resetResult() }
    fun setB(u: Uri) { uriB = u; _state.update { it.copy(nameB = folderName(u)) }; resetResult() }
    fun setOut(u: Uri) { outUri = u; _state.update { it.copy(nameOut = folderName(u), copied = 0, copyFailed = 0, copyDone = false) } }
    fun setDeep(v: Boolean) { _state.update { it.copy(deep = v) }; resetResult() }
    fun hasA() = uriA != null
    fun hasB() = uriB != null
    fun hasOut() = outUri != null

    private fun folderName(u: Uri): String =
        try {
            val doc = u.pathSegments.size >= 4 && u.pathSegments[2] == "document"
            (if (doc) androidx.documentfile.provider.DocumentFile.fromSingleUri(Engine.app, u) else androidx.documentfile.provider.DocumentFile.fromTreeUri(Engine.app, u))?.name ?: "folder"
        } catch (e: Exception) { "folder" }

    private fun crcOf(x: PackInput): Long {
        val crc = CRC32()
        val buf = ByteArray(128 * 1024)
        x.open().use { i ->
            while (true) {
                val n = i.read(buf)
                if (n < 0) break
                crc.update(buf, 0, n)
            }
        }
        return crc.value
    }

    fun run() {
        val a = uriA ?: return
        val b = uriB ?: return
        if (_state.value.running) return
        val deep = _state.value.deep
        resetResult()
        _state.update { it.copy(running = true) }
        job = Engine.scope.launch {
            val ctx = coroutineContext
            try {
                val la = listTree(Engine.app, a).associateBy { it.path }
                val lb = listTree(Engine.app, b).associateBy { it.path }
                filesB = lb
                val changed = ArrayList<String>()
                val added = ArrayList<String>()
                val removed = ArrayList<String>()
                var same = 0
                var n = 0
                val total = maxOf(1, lb.size)
                for ((path, xb) in lb) {
                    ctx.ensureActive()
                    val xa = la[path]
                    if (xa == null) added.add(path)
                    else if (xa.size != xb.size) changed.add(path)
                    else if (deep && xb.size > 0L && crcOf(xa) != crcOf(xb)) changed.add(path)
                    else same++
                    n++
                    if (n % 20 == 0) _state.update { it.copy(progress = n.toFloat() / total, label = path) }
                }
                for (path in la.keys) if (!lb.containsKey(path)) removed.add(path)
                _state.update {
                    it.copy(running = false, done = true, progress = 1f, label = "", changed = changed.sorted(), added = added.sorted(), removed = removed.sorted(), same = same)
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(running = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(running = false, error = e.message ?: e.toString()) }
            }
        }
    }

    /** Copies the changed and new files of B into the output folder. */
    fun copyChanged() {
        val st = _state.value
        if (st.copying || !st.done) return
        val list = (st.changed + st.added).mapNotNull { filesB[it] }
        copyFiles(list, st.changed.size + st.added.size)
    }

    /** Backup: copies every file of the edited folder (B) into the output folder, without comparing first. */
    fun copyAll() {
        val b = uriB ?: return
        if (_state.value.copying || _state.value.running) return
        outUri ?: return
        _state.update { it.copy(copying = true, copied = 0, copyFailed = 0, copyDone = false, progress = 0f, label = "", error = null) }
        job = Engine.scope.launch {
            try {
                val l = listTree(Engine.app, b)
                filesB = l.associateBy { it.path }
                _state.update { it.copy(copying = false) }
                copyFiles(l, l.size)
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(copying = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(copying = false, error = e.message ?: e.toString()) }
            }
        }
    }

    private fun copyFiles(list: List<PackInput>, expected: Int) {
        val out = outUri ?: return
        if (list.isEmpty()) return
        _state.update { it.copy(copying = true, copied = 0, copyFailed = 0, copyDone = false, progress = 0f) }
        val missing = maxOf(0, expected - list.size)
        job = Engine.scope.launch {
            val ctx = coroutineContext
            try {
                val w = TreeWriter(Engine.app, out)
                var ok = 0
                var fail = missing
                val buf = ByteArray(128 * 1024)
                for ((i, x) in list.withIndex()) {
                    ctx.ensureActive()
                    try {
                        val o = w.open(x.path, ConflictPolicy.OVERWRITE)
                        if (o == null) { fail++; continue }
                        o.stream.use { os ->
                            x.open().use { ins ->
                                while (true) {
                                    val n = ins.read(buf)
                                    if (n < 0) break
                                    os.write(buf, 0, n)
                                }
                            }
                        }
                        ok++
                    } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        fail++
                    }
                    if (i % 5 == 0) _state.update { it.copy(progress = (i + 1f) / list.size, label = x.path, copied = ok, copyFailed = fail) }
                }
                _state.update { it.copy(copying = false, copyDone = true, copied = ok, copyFailed = fail, progress = 1f, label = "") }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(copying = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(copying = false, error = e.message ?: e.toString()) }
            }
        }
    }

    fun reportText(id: Boolean): String {
        val s = _state.value
        val sb = StringBuilder()
        sb.append(if (id) "Perbandingan folder\n" else "Folder comparison\n")
        sb.append(if (id) "Asli: " else "Original: ").append(s.nameA ?: "-").append('\n')
        sb.append(if (id) "Diedit: " else "Edited: ").append(s.nameB ?: "-").append('\n')
        sb.append(if (id) "Berubah: " else "Changed: ").append(s.changed.size)
            .append(if (id) ", baru: " else ", new: ").append(s.added.size)
            .append(if (id) ", hilang: " else ", missing: ").append(s.removed.size)
            .append(if (id) ", sama: " else ", same: ").append(s.same).append("\n\n")
        fun block(title: String, l: List<String>) {
            if (l.isEmpty()) return
            sb.append(title).append('\n')
            for (x in l) sb.append("  ").append(x).append('\n')
            sb.append('\n')
        }
        block(if (id) "Berubah" else "Changed", s.changed)
        block(if (id) "Baru di folder diedit" else "New in the edited folder", s.added)
        block(if (id) "Hilang dari folder diedit" else "Missing from the edited folder", s.removed)
        return sb.toString()
    }
}
