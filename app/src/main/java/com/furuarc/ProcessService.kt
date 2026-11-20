package com.furuarc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.furuarc.core.formatBytes
import com.furuarc.i18n.Strings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Foreground service: keeps the process alive while unpack/pack runs and shows a
 * progress notification. The work itself is run by [Engine]; this service only observes its state.
 */
class ProcessService : Service() {
    private val uiScope = CoroutineScope(Dispatchers.Main)
    private var watcher: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(CHANNEL, "FuruArc", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val first = currentNotification()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(ID, first, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(ID, first)
        }
        if (watcher?.isActive != true) {
            watcher = uiScope.launch {
                var sawEngine = false
                while (true) {
                    val st = Engine.state.value
                    val ms = MediaJobs.state.value
                    if (st.running) sawEngine = true
                    if (!st.running && !ms.running && !NameOptJobs.state.value.running) {
                        finish(st, sawEngine)
                        break
                    }
                    notificationManager().notify(ID, currentNotification())
                    delay(700)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun currentNotification(): Notification {
        val st = Engine.state.value
        val ms = MediaJobs.state.value
        val ns = NameOptJobs.state.value
        return when {
            st.running -> progressNotification(st)
            ms.running -> mediaProgressNotification(ms)
            ns.running -> nameOptNotification(ns)
            else -> progressNotification(st)
        }
    }

    private fun nameOptNotification(ns: NameOptState): Notification {
        val s = Strings.of(Engine.lang)
        val pct = (ns.progress * 100).toInt().coerceIn(0, 100)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("${s.processing} $pct%")
            .setContentText(ns.text)
            .setProgress(100, pct, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    private fun notificationManager(): NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private fun finish(st: UiState, sawEngine: Boolean) {
        val sum = st.summary
        if (sawEngine && st.done && sum != null) {
            val s = Strings.of(Engine.lang)
            val text = if (sum.pack) {
                "${sum.ok} · ${formatBytes(sum.bytes)}"
            } else {
                "${s.sumOk}: ${sum.ok} · ${s.sumFailed}: ${sum.failed} · ${s.sumSkipped}: ${sum.skipped}"
            }
            val n = NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(if (sum.pack) s.summaryPack else s.summaryUnpack)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(openAppIntent())
                .build()
            notificationManager().notify(DONE_ID, n)
        }
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun openAppIntent(): PendingIntent {
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun progressNotification(st: UiState): Notification {
        val s = Strings.of(Engine.lang)
        val pct = (st.progress * 100).toInt().coerceIn(0, 100)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("${s.processing} $pct%")
            .setContentText(st.current)
            .setProgress(100, pct, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    private fun mediaProgressNotification(ms: MediaState): Notification {
        val s = Strings.of(Engine.lang)
        val pct = (ms.overall() * 100).toInt().coerceIn(0, 100)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("${s.processing} $pct%")
            .setContentText(ms.currentName)
            .setProgress(100, pct, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    override fun onDestroy() {
        uiScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "furuarc_process"
        private const val ID = 1001
        private const val DONE_ID = 1002

        fun start(ctx: Context) {
            try {
                ContextCompat.startForegroundService(ctx, Intent(ctx, ProcessService::class.java))
            } catch (e: Exception) {
                // The process keeps running without the service (it is just not protected when the app is in the background).
            }
        }
    }
}
