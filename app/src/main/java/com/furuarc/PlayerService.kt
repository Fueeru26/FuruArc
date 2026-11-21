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
import androidx.compose.ui.graphics.asAndroidBitmap
import kotlinx.coroutines.launch
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
        private var session: android.media.session.MediaSession? = null
        private var artKey: String? = null
        private var artBmp: android.graphics.Bitmap? = null
        private val main = android.os.Handler(android.os.Looper.getMainLooper())

        private fun pi(ctx: Context, action: String, code: Int): PendingIntent {
            val i = Intent(ctx, PlayerReceiver::class.java).setAction(action)
            return PendingIntent.getBroadcast(ctx, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        private fun icon(ctx: Context, name: String): android.graphics.drawable.Icon {
            val id = ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
            return android.graphics.drawable.Icon.createWithResource(ctx, id)
        }

        private fun ensureSession(ctx: Context): android.media.session.MediaSession {
            session?.let { return it }
            val s = android.media.session.MediaSession(ctx, "FuruArc")
            s.setCallback(object : android.media.session.MediaSession.Callback() {
                override fun onPlay() { if (!AudioEngine.playing) AudioEngine.toggle() }
                override fun onPause() { if (AudioEngine.playing) AudioEngine.toggle() }
                override fun onSkipToNext() { AudioEngine.next() }
                override fun onSkipToPrevious() { AudioEngine.previous() }
                override fun onSeekTo(pos: Long) { AudioEngine.seek(pos.toInt()) }
                override fun onStop() { AudioEngine.stop() }
                override fun onCustomAction(action: String, extras: android.os.Bundle?) {
                    if (action == "fav") toggleFav(ctx.applicationContext)
                }
            }, main)
            s.isActive = true
            session = s
            return s
        }

        fun toggleFav(ctx: Context) {
            val c = AudioEngine.current ?: return
            com.furuarc.core.MediaLib.toggleFav(2, c)
            sync(ctx)
        }

        private fun loadArt(ctx: Context) {
            val c = AudioEngine.current ?: return
            if (artKey == c.key) return
            artKey = c.key
            artBmp = null
            com.furuarc.Engine.scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val m = try { com.furuarc.core.MediaMeta.load(ctx, c, false, 512) } catch (e: Throwable) { null }
                val bmp = m?.art?.asAndroidBitmap()
                if (bmp != null && artKey == c.key) {
                    artBmp = bmp
                    main.post { sync(ctx) }
                }
            }
        }

        private fun updateSession(ctx: Context): android.media.session.MediaSession {
            val s = ensureSession(ctx)
            val c = AudioEngine.current
            val meta = c?.let { com.furuarc.core.MediaMeta.cached(it.key) }
            val mb = android.media.MediaMetadata.Builder()
                .putString(android.media.MediaMetadata.METADATA_KEY_TITLE, meta?.title ?: c?.name ?: "FuruArc")
                .putString(android.media.MediaMetadata.METADATA_KEY_ARTIST, meta?.artist ?: "")
                .putString(android.media.MediaMetadata.METADATA_KEY_ALBUM, meta?.album ?: "")
                .putLong(android.media.MediaMetadata.METADATA_KEY_DURATION, AudioEngine.duration.toLong())
            artBmp?.let { mb.putBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART, it) }
            s.setMetadata(mb.build())
            val st = if (AudioEngine.playing) android.media.session.PlaybackState.STATE_PLAYING else android.media.session.PlaybackState.STATE_PAUSED
            val acts = android.media.session.PlaybackState.ACTION_PLAY or android.media.session.PlaybackState.ACTION_PAUSE or
                android.media.session.PlaybackState.ACTION_PLAY_PAUSE or android.media.session.PlaybackState.ACTION_SKIP_TO_NEXT or
                android.media.session.PlaybackState.ACTION_SKIP_TO_PREVIOUS or android.media.session.PlaybackState.ACTION_SEEK_TO or
                android.media.session.PlaybackState.ACTION_STOP
            val ps = android.media.session.PlaybackState.Builder()
                .setActions(acts)
                .setState(st, AudioEngine.position.toLong(), if (AudioEngine.playing) com.furuarc.core.MediaPrefs.speed else 0f)
                .build()
            s.setPlaybackState(ps)
            return s
        }

        fun build(ctx: Context): Notification {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= 26 && nm.getNotificationChannel(CH) == null) {
                val ch = NotificationChannel(CH, "Media Player", NotificationManager.IMPORTANCE_LOW)
                ch.setShowBadge(false)
                nm.createNotificationChannel(ch)
            }
            loadArt(ctx)
            val s = updateSession(ctx)
            val open = PendingIntent.getActivity(
                ctx, 0, Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val c = AudioEngine.current
            val meta = c?.let { com.furuarc.core.MediaMeta.cached(it.key) }
            val title = meta?.title ?: c?.name ?: "FuruArc"
            val fav = c != null && com.furuarc.core.MediaLib.isFav(2, c)
            val b = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(ctx, CH) else @Suppress("DEPRECATION") Notification.Builder(ctx)
            b.setSmallIcon(ctx.resources.getIdentifier("ic_np_small", "drawable", ctx.packageName))
                .setContentTitle(title)
                .setContentText(meta?.artist ?: "")
                .setContentIntent(open)
                .setOnlyAlertOnce(true)
                .setOngoing(AudioEngine.playing)
                .setShowWhen(false)
                .setCategory(Notification.CATEGORY_TRANSPORT)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
            artBmp?.let { b.setLargeIcon(it) } ?: meta?.art?.let { b.setLargeIcon(it.asAndroidBitmap()) }
            b.addAction(Notification.Action.Builder(icon(ctx, if (fav) "ic_np_heart_on" else "ic_np_heart"), "Favorit", pi(ctx, "fav", 5)).build())
            b.addAction(Notification.Action.Builder(icon(ctx, "ic_np_prev"), "Prev", pi(ctx, "prev", 1)).build())
            b.addAction(Notification.Action.Builder(icon(ctx, if (AudioEngine.playing) "ic_np_pause" else "ic_np_play"), "Play", pi(ctx, "toggle", 2)).build())
            b.addAction(Notification.Action.Builder(icon(ctx, "ic_np_next"), "Next", pi(ctx, "next", 3)).build())
            b.addAction(Notification.Action.Builder(icon(ctx, "ic_np_close"), "Close", pi(ctx, "stop", 4)).build())
            b.setStyle(Notification.MediaStyle().setMediaSession(s.sessionToken).setShowActionsInCompactView(1, 2, 3))
            return b.build()
        }

        /** Starts, updates or stops the notification service so that it matches the engine state. */
        fun sync(ctx: Context) {
            try {
                val app = ctx.applicationContext
                val active = AudioEngine.current != null && !AudioEngine.failed && com.furuarc.core.MediaPrefs.background
                if (!active) {
                    app.stopService(Intent(app, PlayerService::class.java))
                    try { session?.isActive = false; session?.release() } catch (e: Throwable) { }
                    session = null
                    artKey = null
                    artBmp = null
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
            "fav" -> PlayerService.toggleFav(context)
        }
    }
}
