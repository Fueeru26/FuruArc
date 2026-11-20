package com.furuarc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.furuarc.core.ConflictPolicy
import com.furuarc.core.MEDIA_OK
import com.furuarc.core.MEDIA_SAME
import com.furuarc.core.MEDIA_SKIP
import com.furuarc.core.MediaCancelled
import com.furuarc.core.MediaCtl
import com.furuarc.core.formatBytes
import com.furuarc.i18n.mediaStr
import com.furuarc.storage.TreeWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.FilterOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicInteger

/** One source file: [rel] = relative folder path under the selected folder ("" = top level). */
class MediaItem(val uri: Uri, val name: String, val rel: String, val size: Long)

/** How to convert one file: [outName] computes the output name, [run] converts and writes via a sink (null = skipped). */
class MediaTask(
    val outName: (String) -> String,
    /** if present, used instead of [outName] (output name that depends on file contents, e.g. XPCM -> OGG or WAV). */
    val outNameItem: ((MediaItem) -> String)? = null,
    val run: (MediaCtl, MediaItem, () -> OutputStream?) -> Int,
)

/** One output file. [status]: 0 = exists, 1 = cancelled (deleted/restored), 2 = cancel failed. */
data class MediaResult(
    val name: String,
    val outName: String,
    val inBytes: Long,
    val outBytes: Long,
    val outUri: Uri?,
    val backupUri: Uri?,
    val status: Int,
    /** false if the result overwrites the original file without a backup (cancelling means deleting the only copy). */
    val canUndo: Boolean = true,
)

/** Summary per source extension. */
class ExtStat(val ext: String, val count: Int, val inBytes: Long, val outBytes: Long)

/** kind: 0 audio, 1 image, 2 video, 3 text, 4 game-specific asset, 5 mixed (audio + image + video). */
data class MediaState(
    val kind: Int = -1,
    val running: Boolean = false,
    val finished: Boolean = false,
    val cancelled: Boolean = false,
    val total: Int = 0,
    val index: Int = 0,
    val currentName: String = "",
    val currentProgress: Float = 0f,
    val ok: Int = 0,
    val same: Int = 0,
    val skipped: Int = 0,
    val failed: Int = 0,
    val errors: List<String> = emptyList(),
    val inBytes: Long = 0L,
    val outBytes: Long = 0L,
    val startedAt: Long = 0L,
    val totalBytes: Long = 0L,
    val doneBytes: Long = 0L,
    val curBytes: Long = 0L,
    val results: List<MediaResult> = emptyList(),
    val extStats: List<ExtStat> = emptyList(),
)

/** Overall progress 0..1, weighted by byte size when known. */
fun MediaState.overall(): Float {
    val f = if (totalBytes > 0L) ((doneBytes + currentProgress * curBytes) / totalBytes).toFloat()
    else (index + currentProgress) / maxOf(1, total)
    return f.coerceIn(0f, 1f)
}

private class CountingOut(out: OutputStream, private val onCount: (Long) -> Unit) : FilterOutputStream(out) {
    private var n = 0L
    override fun write(b: Int) {
        out.write(b)
        n++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        n += len
    }

    override fun close() {
        try { flush() } catch (e: Exception) { }
        out.close()
        onCount(n)
    }
}

/**
 * Runs batch conversion in the background (own scope, so it keeps running when switching screens) and stores its progress.
 * Several files are processed in parallel (the count depends on the type). Progress is throttled to ~6 times/second so the UI is not flooded.
 */
object MediaJobs {
    private const val NOTIF_CHANNEL = "furuarc_media_done"
    private const val NOTIF_ID = 1003

    private val _state = MutableStateFlow(MediaState())
    val state: StateFlow<MediaState> = _state.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    @Volatile private var cancelFlag = false

    fun isRunning(): Boolean = _state.value.running

    fun cancel() {
        cancelFlag = true
    }

    /** Delete the last result (only when not running). */
    fun clear() {
        if (!_state.value.running) _state.value = MediaState()
    }

    /**
     * [inPlace]: results are written to the source folder ([outTree] = source folder); original files that would be overwritten are backed up first
     * to "_backup/" if [backup] is enabled (otherwise originals are overwritten without a backup). [stripRoot]: drop the first segment of [MediaItem.rel] (the source folder name) from the output path.
     */
    fun start(
        kind: Int, itemsIn: List<MediaItem>, outTree: Uri, policy: ConflictPolicy, task: MediaTask,
        inPlace: Boolean = false, stripRoot: Boolean = false, backup: Boolean = true,
    ) {
        if (_state.value.running || itemsIn.isEmpty()) return
        val items = if (kind == 5) itemsIn.sortedBy { com.furuarc.core.MixedConv.rank(it.name) } else itemsIn
        cancelFlag = false
        _state.value = MediaState(
            kind = kind, running = true, total = items.size,
            startedAt = System.currentTimeMillis(), totalBytes = items.sumOf { it.size },
        )
        // background service (progress notification) only if "Run in Background" is enabled in Settings
        if (Engine.bgEnabled) ProcessService.start(Engine.app)
        job = scope.launch {
            val app = Engine.app
            val tmp = File(app.cacheDir, "media_tmp")
            tmp.deleteRecursively()
            tmp.mkdirs()
            val lock = Any()
            var ok = 0
            var same = 0
            var skipped = 0
            var failed = 0
            var inB = 0L
            var outB = 0L
            var completed = 0
            var doneB = 0L
            var lastName = ""
            var lastPush = 0L
            val errors = ArrayList<String>()
            val results = ArrayList<MediaResult>()
            val exts = LinkedHashMap<String, LongArray>() // ext -> [count, in, out]
            val active = HashMap<Int, Float>()
            var wasCancelled = false

            // Call inside synchronized(lock). Progress of files currently running is combined, weighted by file size.
            fun push(force: Boolean) {
                val now = System.currentTimeMillis()
                if (!force && now - lastPush < 160L) return
                lastPush = now
                var cb = 0L
                var w = 0.0
                for ((idx, pr) in active) {
                    val sz = items[idx].size
                    cb += sz
                    w += pr.toDouble() * sz
                }
                val prog = if (cb > 0L) (w / cb).toFloat() else if (active.isEmpty()) 0f else active.values.average().toFloat()
                _state.update { s ->
                    s.copy(
                        index = completed, currentName = lastName, currentProgress = prog, curBytes = cb, doneBytes = doneB,
                        ok = ok, same = same, skipped = skipped, failed = failed, errors = errors.toList(), inBytes = inB, outBytes = outB,
                    )
                }
            }

            try {
                val tw = TreeWriter(app, outTree)
                val cores = Runtime.getRuntime().availableProcessors()
                // video uses hardware codecs (one at a time); images are limited because they use a lot of memory
                val workers = when (kind) { 0 -> minOf(2, cores); 1 -> minOf(3, cores); 2 -> 1; 4 -> minOf(2, cores); 5 -> minOf(3, cores); else -> minOf(4, cores) }.coerceAtLeast(1)
                val next = AtomicInteger(0)
                val threads = (0 until minOf(workers, items.size)).map {
                    Thread {
                        while (true) {
                            if (cancelFlag) {
                                synchronized(lock) { wasCancelled = true }
                                break
                            }
                            val i = next.getAndIncrement()
                            if (i >= items.size) break
                            val item = items[i]
                            synchronized(lock) {
                                active[i] = 0f
                                lastName = item.name
                                push(true)
                            }
                            val ctl = MediaCtl(app, tmp, { cancelFlag }) { p ->
                                synchronized(lock) {
                                    active[i] = p
                                    push(false)
                                }
                            }
                            var written = 0L
                            var outUri: Uri? = null
                            var backupUri: Uri? = null
                            val relOut = if (stripRoot) item.rel.substringAfter('/', "") else item.rel
                            val outName = task.outNameItem?.invoke(item) ?: task.outName(item.name)
                            val outRel = (if (relOut.isEmpty()) "" else "$relOut/") + outName
                            val sameName = outName == item.name
                            val pol = if (inPlace) (if (sameName) ConflictPolicy.OVERWRITE else ConflictPolicy.RENAME) else policy
                            val sink: () -> OutputStream? = {
                                if (inPlace && sameName && backup) {
                                    // back up the original file before overwriting; backup failure = this file is left unchanged
                                    val bpath = "_backup/" + (if (relOut.isEmpty()) "" else "$relOut/") + item.name
                                    try {
                                        val bf = tw.open(bpath, ConflictPolicy.RENAME) ?: throw IOException("-")
                                        bf.stream.use { os ->
                                            (app.contentResolver.openInputStream(item.uri) ?: throw IOException("Tidak bisa membaca file asli")).use { ins ->
                                                ins.copyTo(os, 256 * 1024)
                                            }
                                        }
                                        backupUri = bf.uri
                                    } catch (e: Exception) {
                                        throw IOException("Gagal mencadangkan file asli: " + (e.message ?: ""))
                                    }
                                }
                                val of = tw.open(outRel, pol)
                                if (of == null) {
                                    null
                                } else {
                                    outUri = of.uri
                                    CountingOut(of.stream) { n -> written = n }
                                }
                            }
                            var res = -1
                            var err: String? = null
                            var cancelled = false
                            try {
                                res = task.run(ctl, item, sink)
                            } catch (e: MediaCancelled) {
                                cancelled = true
                            } catch (e: Throwable) {
                                err = item.name + ": " + (e.message ?: e.javaClass.simpleName)
                            }
                            synchronized(lock) {
                                active.remove(i)
                                if (cancelled) {
                                    wasCancelled = true
                                } else {
                                    completed++
                                    doneB += item.size
                                    if (err != null) {
                                        failed++
                                        errors.add(err)
                                    } else {
                                        val wrote = res == MEDIA_OK || res == MEDIA_SAME
                                        when (res) {
                                            MEDIA_SKIP -> skipped++
                                            MEDIA_SAME -> { same++; inB += item.size; outB += written }
                                            MEDIA_OK -> { ok++; inB += item.size; outB += written }
                                            else -> ok++
                                        }
                                        if (wrote) {
                                            results.add(MediaResult(item.name, outName, item.size, written, outUri, backupUri, 0, !(inPlace && sameName && !backup)))
                                            val ext = item.name.substringAfterLast('.', "").lowercase()
                                            val a = exts.getOrPut(ext) { LongArray(3) }
                                            a[0]++
                                            a[1] += item.size
                                            a[2] += written
                                        }
                                    }
                                }
                                push(true)
                            }
                            if (cancelled) break
                        }
                    }
                }
                threads.forEach { it.start() }
                threads.forEach { it.join() }
            } catch (e: Throwable) {
                synchronized(lock) {
                    failed++
                    errors.add(e.message ?: e.javaClass.simpleName)
                }
            } finally {
                tmp.deleteRecursively()
                var fin = MediaState()
                synchronized(lock) {
                    _state.update { s ->
                        s.copy(
                            running = false, finished = true, cancelled = wasCancelled, ok = ok, same = same, skipped = skipped,
                            failed = failed, errors = errors.toList(), inBytes = inB, outBytes = outB, currentName = "", currentProgress = 0f,
                            curBytes = 0L, results = results.sortedBy { it.name.lowercase() },
                            extStats = exts.entries.map { ExtStat(it.key, it.value[0].toInt(), it.value[1], it.value[2]) },
                        )
                    }
                    fin = _state.value
                }
                if (!wasCancelled) notifyDone(app, fin)
            }
        }
    }

    /** Cancel one result: delete the output file, or restore the original file from the backup if the result overwrote it. */
    fun undo(index: Int) {
        val cur = _state.value.results.getOrNull(index) ?: return
        if (cur.status == 1 || !cur.canUndo) return
        scope.launch {
            val cr = Engine.app.contentResolver
            var st = 1
            try {
                val ou = cur.outUri ?: throw IOException("-")
                val bu = cur.backupUri
                if (bu != null) {
                    (cr.openInputStream(bu) ?: throw IOException("-")).use { ins ->
                        (cr.openOutputStream(ou, "wt") ?: throw IOException("-")).use { os -> ins.copyTo(os, 256 * 1024) }
                    }
                    try { DocumentsContract.deleteDocument(cr, bu) } catch (e: Exception) { }
                } else {
                    if (!DocumentsContract.deleteDocument(cr, ou)) throw IOException("-")
                }
            } catch (e: Throwable) {
                st = 2
            }
            _state.update { s ->
                s.copy(results = s.results.mapIndexed { i, r -> if (i == index) r.copy(status = st) else r })
            }
        }
    }

    /** Cancel all results whose size is larger than their source (delete the result / restore the original file from the backup). */
    fun discardLarger() {
        if (_state.value.running) return
        val idx = _state.value.results.mapIndexedNotNull { i, r -> if (r.status == 0 && r.canUndo && r.outBytes > r.inBytes) i else null }
        for (i in idx) undo(i)
    }

    private fun notifyDone(ctx: Context, st: MediaState) {
        try {
            if (!Engine.bgEnabled) return // follows the Run in Background setting
            val nm = NotificationManagerCompat.from(ctx)
            if (!nm.areNotificationsEnabled()) return
            if (Build.VERSION.SDK_INT >= 26) {
                val ch = NotificationChannel(NOTIF_CHANNEL, "FuruArc", NotificationManager.IMPORTANCE_DEFAULT)
                ctx.getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
            }
            val m = mediaStr(Engine.lang)
            val title = if (st.failed > 0 && st.ok + st.same == 0) m.notifFail else m.notifDone
            var text = m.counts(st.ok, st.same, st.skipped, st.failed)
            if (st.inBytes > 0L) text += " · " + formatBytes(st.inBytes) + " → " + formatBytes(st.outBytes)
            val i = Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pi = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val n = NotificationCompat.Builder(ctx, NOTIF_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            nm.notify(NOTIF_ID, n)
        } catch (e: Exception) {
            // the notification is only an extra
        }
    }
}
