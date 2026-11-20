package com.furuarc

import android.net.Uri
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
import kotlin.coroutines.coroutineContext

data class InspectState(
    val name: String? = null,
    val busy: Boolean = false,
    val isArchive: Boolean = false,
    val report: Report? = null,
    val hashes: Hashes? = null,
    val hashing: Boolean = false,
    val hashProgress: Float = 0f,
    val deep: List<Finding>? = null,
    val deepRunning: Boolean = false,
    val deepDone: Int = 0,
    val deepTotal: Int = 0,
    val error: String? = null,
    val textPreview: String? = null, // file contents as text ("Lihat isi sebagai teks" dialog)
    val textTruncated: Boolean = false,
)

/** Archive and Script Check: opens the user's chosen file, identifies its type, then builds a report. All offline. */
object InspectJobs {
    private val _state = MutableStateFlow(InspectState())
    val state: StateFlow<InspectState> = _state.asStateFlow()

    private var job: Job? = null
    private var uri: Uri? = null
    val currentUri: Uri? get() = uri
    private var fmt: ArchiveFormat? = null
    private var entries: List<Entry> = emptyList()
    private var key: Int? = null

    private const val HEAD_MAX = 16 * 1024 * 1024
    private const val AUTO_HASH_MAX = 64L * 1024 * 1024

    private val id: Boolean get() = Engine.lang == Lang.ID

    private fun open(u: Uri): ChannelSource {
        val pfd = Engine.app.contentResolver.openFileDescriptor(u, "r") ?: throw java.io.IOException("Tidak bisa membuka file")
        return ChannelSource(pfd)
    }

    /** Engine hint from the extension: used for files that are not recognized archives. */
    internal fun engineHints(name: String, id: Boolean): List<Finding> {
        val ext = name.substringAfterLast('.', "").lowercase()
        if (ext.isEmpty()) return emptyList()
        val hit = (VN_ENGINES + RPGM_ENGINES).filter { it.ext.lowercase() == ext }.map { it.name }.distinct()
        if (hit.isEmpty()) return emptyList()
        val list = hit.joinToString(", ")
        return listOf(
            Finding(
                2,
                if (id) "Ekstensi .$ext biasanya arsip engine: $list, tetapi isinya tidak cocok dengan format yang didukung (mungkin terenkripsi khusus, terpotong, atau versi format belum didukung). Coba lewat Daftar Engine."
                else "The .$ext extension is usually an archive of: $list, but the content does not match a supported format (maybe specially encrypted, truncated or an unsupported version). Try Engine List.",
            ),
        )
    }

    fun cancel() {
        job?.cancel()
        job = null
    }

    fun clear() {
        cancel()
        uri = null; fmt = null; entries = emptyList(); key = null
        _state.value = InspectState()
    }

    fun inspect(u: Uri) {
        cancel()
        uri = u; fmt = null; entries = emptyList(); key = null
        val name = Engine.displayName(u)
        _state.value = InspectState(name = name, busy = true)
        job = Engine.scope.launch {
            try {
                open(u).use { src ->
                    val f = FormatRegistry.detect(src)
                    if (f != null) {
                        fmt = f
                        val engines = (VN_ENGINES + RPGM_ENGINES).filter { it.formatId == f.id }.map { it.name }.distinct().joinToString(", ")
                        val rep = try {
                            val list = f.list(src, Options())
                            entries = list.sortedBy { it.name }
                            key = try { f.detectKey(src, entries) } catch (e: Exception) { null }
                            Inspector.archive(name, src, f, entries, key, engines, id)
                        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                            throw e
                        } catch (e: Throwable) {
                            Inspector.brokenArchive(src, f, engines, e.message ?: e.javaClass.simpleName, id)
                        }
                        // so the "Isi Arsip" button can use the same browser as "Bongkar Arsip"
                        if (entries.isNotEmpty()) {
                            Engine.reset()
                            Engine.setArchive(u, null)
                        }
                        _state.update { it.copy(busy = false, isArchive = entries.isNotEmpty(), report = rep) }
                    } else {
                        val n = minOf(src.length, HEAD_MAX.toLong()).toInt()
                        val head = if (n > 0) src.readFully(0, n) else ByteArray(0)
                        val rep0 = Inspector.file(name, head, src.length, id)
                        val known = rep0.info.any { it.first == "Engine yang didukung" || it.first == "Supported engine" }
                        val rep = Report(rep0.info, if (known) rep0.findings else rep0.findings + engineHints(name, id))
                        _state.update { it.copy(busy = false, isArchive = false, report = rep) }
                        if (src.length in 1..AUTO_HASH_MAX) {
                            val h = Inspector.hashes(src, { }, { })
                            _state.update { it.copy(hashes = h) }
                        }
                    }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(busy = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    /** Read the first 200 KB of the file and show it as text (encoding detected: BOM, UTF-8, then Shift-JIS). */
    fun showText() {
        val u = uri ?: return
        Engine.scope.launch {
            try {
                open(u).use { src ->
                    val max = 200 * 1024
                    val n = minOf(src.length, max.toLong()).toInt()
                    val head = if (n > 0) src.readFully(0, n) else ByteArray(0)
                    // a cut in the middle of a multibyte character at the end does not matter for display
                    val text = ScriptText.decode(ScriptKind.KAG, head, "auto").text
                    _state.update { it.copy(textPreview = text, textTruncated = src.length > max) }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(textPreview = "", textTruncated = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    fun hideText() = _state.update { it.copy(textPreview = null, textTruncated = false) }

    /** Compute CRC32/MD5/SHA-256 of the whole file (for large archives, run on request). */
    fun hash() {
        val u = uri ?: return
        if (_state.value.hashing) return
        _state.update { it.copy(hashing = true, hashProgress = 0f) }
        job = Engine.scope.launch {
            try {
                val ctx = coroutineContext
                open(u).use { src ->
                    val total = src.length.coerceAtLeast(1)
                    var last = 0L
                    val h = Inspector.hashes(
                        src,
                        { p -> if (p - last > total / 100) { last = p; _state.update { it.copy(hashProgress = p.toFloat() / total) } } },
                        { ctx.ensureActive() },
                    )
                    _state.update { it.copy(hashes = h, hashing = false) }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(hashing = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(hashing = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    /** Test-extract all archive files to a "trash" to find broken files. */
    fun deepCheck() {
        val u = uri ?: return
        val f = fmt ?: return
        if (_state.value.deepRunning) return
        _state.update { it.copy(deepRunning = true, deepDone = 0, deepTotal = entries.size, deep = null) }
        job = Engine.scope.launch {
            try {
                val ctx = coroutineContext
                open(u).use { src ->
                    val r = Inspector.deepCheck(
                        src, f, entries, key,
                        { d, t, _ -> if (d % 20 == 0 || d == t) _state.update { it.copy(deepDone = d, deepTotal = t) } },
                        { ctx.ensureActive() },
                    )
                    _state.update { it.copy(deepRunning = false, deep = Inspector.deepFindings(r, id)) }
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                _state.update { it.copy(deepRunning = false) }
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(deepRunning = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }
}
