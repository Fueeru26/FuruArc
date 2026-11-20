package com.furuarc

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.furuarc.core.NameOpt
import com.furuarc.i18n.nameOptStr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** File Name Optimizer screen choices when the job starts (so the screen can be restored after it is left). */
class NameOptSnap(
    val src: Uri, val srcName: String, val backup: Uri?, val backupName: String?,
    val scopeIdx: Int, val styleIdx: Int, val folders: Boolean, val symbols: Boolean, val wide: Boolean, val caseFix: Boolean, val textExts: String,
)

/** kind: 0 scan (preview), 1 apply, 2 undo. */
data class NameOptState(val running: Boolean = false, val kind: Int = -1, val text: String = "", val progress: Float = 0f, val serial: Int = 0)

/** Job result not yet picked up by the screen. */
class NameOptDone(
    val kind: Int, val snap: NameOptSnap, val entries: List<NameOpt.Entry>?,
    val plan: NameOpt.Plan?, val scan: NameOpt.RefScan?, val key: String,
    val run: NameOpt.RunResult?, val undo: NameOpt.UndoResult?,
)

/**
 * Runs the File Name Optimizer in its own scope (keeps running when the screen is left). If "Run in Background"
 * is enabled, [ProcessService] shows the progress notification and the completion notification.
 */
object NameOptJobs {
    private const val NOTIF_CHANNEL = "furuarc_media_done"
    private const val NOTIF_ID = 1004

    private val _state = MutableStateFlow(NameOptState())
    val state: StateFlow<NameOptState> = _state.asStateFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile private var snap: NameOptSnap? = null
    @Volatile private var done: NameOptDone? = null

    fun isRunning() = _state.value.running

    /** Screen choices if a job is running or a result has not been picked up. */
    fun pendingSnap(): NameOptSnap? = if (_state.value.running || done != null) snap else null

    fun take(): NameOptDone? {
        val d = done
        done = null
        return d
    }

    private fun begin(kind: Int, s: NameOptSnap, text: String): Boolean {
        if (_state.value.running) return false
        snap = s
        done = null
        _state.value = _state.value.copy(running = true, kind = kind, text = text, progress = 0f)
        if (Engine.bgEnabled) ProcessService.start(Engine.app)
        return true
    }

    private fun progress(text: String, d: Int, t: Int) {
        _state.value = _state.value.copy(text = text, progress = if (t == 0) 0f else d.toFloat() / t)
    }

    private fun finish(d: NameOptDone) {
        done = d
        _state.value = NameOptState(running = false, kind = d.kind, serial = _state.value.serial + 1)
        notifyDone(d)
    }

    fun scan(s: NameOptSnap, o: NameOpt.Opts, key: String) {
        val n = nameOptStr(Engine.lang)
        if (!begin(0, s, n.planning)) return
        scope.launch {
            val ctx = Engine.app
            var entries: List<NameOpt.Entry>? = null
            var plan: NameOpt.Plan? = null
            var sc: NameOpt.RefScan? = null
            try {
                val l = try { NameOpt.list(ctx, s.src) } catch (e: Exception) { emptyList() }
                entries = l
                plan = NameOpt.buildPlan(l, o)
                sc = NameOpt.scanRefs(ctx, l, plan, o) { d, t -> progress(n.scanning(d, t), d, t) }
            } catch (e: Throwable) {
                plan = null
                sc = null
            }
            finish(NameOptDone(0, s, entries, plan, sc, key, null, null))
        }
    }

    fun run(s: NameOptSnap, plan: NameOpt.Plan, scan: NameOpt.RefScan, o: NameOpt.Opts) {
        val n = nameOptStr(Engine.lang)
        if (!begin(1, s, n.phase(0, 0, 0))) return
        scope.launch {
            val r = try {
                NameOpt.run(Engine.app, s.src, s.backup, plan, scan, o) { ph, d, t -> progress(n.phase(ph, d, t), d, t) }
            } catch (e: Throwable) {
                NameOpt.RunResult(0, 0, 0, 0, 0, emptyList(), emptyList(), emptyList(), 0, e.message ?: e.toString())
            }
            finish(NameOptDone(1, s, null, null, null, "", r, null))
        }
    }

    fun undo(s: NameOptSnap) {
        val n = nameOptStr(Engine.lang)
        if (!begin(2, s, n.undoPhase(0, 0, 0))) return
        scope.launch {
            val r = try {
                NameOpt.undo(Engine.app, s.src) { ph, d, t -> progress(n.undoPhase(ph, d, t), d, t) }
            } catch (e: Throwable) {
                NameOpt.UndoResult(0, 0, 0, emptyList(), e.message ?: e.toString())
            }
            finish(NameOptDone(2, s, null, null, null, "", null, r))
        }
    }

    private fun notifyDone(d: NameOptDone) {
        try {
            if (!Engine.bgEnabled) return
            val ctx = Engine.app
            val nm = NotificationManagerCompat.from(ctx)
            if (!nm.areNotificationsEnabled()) return
            if (Build.VERSION.SDK_INT >= 26) {
                val ch = NotificationChannel(NOTIF_CHANNEL, "FuruArc", NotificationManager.IMPORTANCE_DEFAULT)
                ctx.getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
            }
            val n = nameOptStr(Engine.lang)
            val text = when (d.kind) {
                0 -> d.plan?.let { n.prevSummary(it.files, it.dirs, d.scan?.files?.size ?: 0, d.scan?.replaced ?: 0, d.scan?.caseFixes ?: 0) } ?: n.fail("")
                1 -> d.run?.let { if (it.error != null) n.fail(it.error) else n.resDone(it.files, it.dirs, it.edited, it.replaced) } ?: ""
                else -> d.undo?.let { if (it.error != null) n.undoNone else n.undoDone(it.files, it.dirs, it.edited) } ?: ""
            }
            val i = Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pi = PendingIntent.getActivity(ctx, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val b = NotificationCompat.Builder(ctx, NOTIF_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(n.menuTitle)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            nm.notify(NOTIF_ID, b)
        } catch (e: Exception) {
            // the notification is only extra
        }
    }
}
