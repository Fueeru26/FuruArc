package com.furuarc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.furuarc.core.AudioEngine

/** Foreground service that keeps music playing (with a notification and controls) while the app is in the background. */
class PlayerService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (AudioEngine.current == null || !com.furuarc.core.MediaPrefs.background) {
            stopSelf()
            return START_NOT_STICKY
        }
        val n = build(this)
        try {
            if (Build.VERSION.SDK_INT >= 29) startForeground(ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK) else startForeground(ID, n)
            running = true
        } catch (e: Exception) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        running = false
        super.onDestroy()
    }

    companion object {
        private const val ID = 4107
        private const val CH = "furuarc_player"
        @Volatile var running = false

        private fun pi(ctx: Context, action: String, code: Int): PendingIntent {
            val i = Intent(ctx, PlayerReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(ctx, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        fun build(ctx: Context): Notification {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CH) == null) {
                val ch = NotificationChannel(CH, "Media Player", NotificationManager.IMPORTANCE_LOW)
                ch.setShowBadge(false)
                nm.createNotificationChannel(ch)
            }
            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val title = AudioEngine.current?.let { com.furuarc.core.MediaMeta.cached(it.key)?.title ?: it.name } ?: "FuruArc"
            val b = NotificationCompat.Builder(ctx, CH)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(if (AudioEngine.playing) "▶" else "❚❚")
                .setContentIntent(open)
                .setOnlyAlertOnce(true)
                .setOngoing(AudioEngine.playing)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(android.R.drawable.ic_media_previous, "⏮", pi(ctx, "prev", 1))
                .addAction(if (AudioEngine.playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play, if (AudioEngine.playing) "❚❚" else "▶", pi(ctx, "toggle", 2))
                .addAction(android.R.drawable.ic_media_next, "⏭", pi(ctx, "next", 3))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "✕", pi(ctx, "stop", 4))
            return b.build()
        }

        /** Starts, updates or stops the notification service so that it matches the engine state. */
        fun sync(ctx: Context) {
            try {
                val app = ctx.applicationContext
                val active = AudioEngine.current != null && !AudioEngine.failed && com.furuarc.core.MediaPrefs.background
                if (!active) {
                    app.stopService(Intent(app, PlayerService::class.java))
                    return
                }
                if (running) {
                    (app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(ID, build(app))
                } else if (Build.VERSION.SDK_INT >= 26) {
                    app.startForegroundService(Intent(app, PlayerService::class.java))
                } else {
                    app.startService(Intent(app, PlayerService::class.java))
                }
            } catch (e: Exception) { }
        }
    }
}

/** Receives the notification buttons of the player. */
class PlayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "toggle" -> AudioEngine.toggle()
            "next" -> AudioEngine.next()
            "prev" -> AudioEngine.previous()
            "stop" -> AudioEngine.stop()
        }
    }
}
