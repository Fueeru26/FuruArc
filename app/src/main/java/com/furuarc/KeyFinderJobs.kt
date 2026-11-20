package com.furuarc

import android.net.Uri
import android.provider.DocumentsContract
import com.furuarc.core.*
import com.furuarc.settings.Lang
import com.furuarc.storage.ChannelSource
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

data class KeyFinderState(
    val name: String? = null,
    val running: Boolean = false,
    val listing: Boolean = false,
    val label: String = "",
    val step: Int = 0,
    val steps: Int = 0,
    val report: KeyReport? = null,
    val error: String? = null,
)

/** Key Finder: scans a game folder and looks for the encryption key (see [KeyFinder]). */
object KeyFinderJobs {
    private val _state = MutableStateFlow(KeyFinderState())
    val state: StateFlow<KeyFinderState> = _state.asStateFlow()
    private var job: Job? = null
    private var tree: Uri? = null
    private const val MAX_FILES = 30000

    private val id: Boolean get() = Engine.lang == Lang.ID

    private class Ref(val path: String, val size: Long, val uri: Uri)

    private fun listFiles(root: Uri, out: MutableList<Ref>) {
        val resolver = Engine.app.contentResolver
        val proj = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE,
        )
        fun walk(docId: String, prefix: String) {
            if (out.size >= MAX_FILES) return
            val kids = DocumentsContract.buildChildDocumentsUriUsingTree(root, docId)
            val dirs = ArrayList<Pair<String, String>>()
            resolver.query(kids, proj, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val did = c.getString(0) ?: continue
                    val nm = c.getString(1) ?: continue
                    if (c.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) dirs.add(Pair(did, nm))
                    else if (out.size < MAX_FILES) out.add(Ref(prefix + nm, if (c.isNull(3)) 0L else c.getLong(3), DocumentsContract.buildDocumentUriUsingTree(root, did)))
                }
            }
            for ((d, n) in dirs) walk(d, "$prefix$n/")
        }
        walk(DocumentsContract.getTreeDocumentId(root), "")
    }

    private fun open(u: Uri): RandomSource {
        val pfd = Engine.app.contentResolver.openFileDescriptor(u, "r") ?: throw java.io.IOException("Tidak bisa membuka file")
        return ChannelSource(pfd)
    }

    fun pick(uri: Uri) {
        cancel()
        tree = uri
        val title = DocumentsContract.getTreeDocumentId(uri).substringAfterLast(':').ifEmpty { "/" }
        _state.value = KeyFinderState(name = title)
    }

    fun cancel() { job?.cancel(); job = null }
    fun clear() { cancel(); tree = null; _state.value = KeyFinderState() }

    fun analyze(engine: String) {
        val root = tree ?: return
        cancel()
        _state.update { it.copy(running = true, listing = true, report = null, error = null, step = 0, steps = 0, label = "") }
        job = Engine.scope.launch {
            val ctx = coroutineContext
            try {
                val refs = ArrayList<Ref>()
                listFiles(root, refs)
                refs.sortBy { it.path.lowercase() }
                val files = refs.map { r -> KfFile(r.path, r.size) { open(r.uri) } }
                _state.update { it.copy(listing = false) }
                val rep = KeyFinder.analyze(files, engine, id, { d, t, l -> _state.update { s -> s.copy(step = d, steps = t, label = l) } }, { !ctx.isActive })
                ctx.ensureActive()
                _state.update { it.copy(running = false, report = rep) }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(running = false, listing = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(running = false, listing = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    fun reportText(): String {
        val st = _state.value
        val r = st.report ?: return ""
        return KeyFinder.reportText(st.name ?: "", r, id)
    }
}
