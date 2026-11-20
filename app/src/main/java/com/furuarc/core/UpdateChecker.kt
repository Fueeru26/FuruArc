package com.furuarc.core

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.furuarc.Engine
import com.furuarc.i18n.NavStr
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Latest GitHub release: [apkUrl] = direct download of the first .apk asset (null if the release has none). */
class UpdateInfo(val tag: String, val name: String, val notes: String, val pageUrl: String, val apkUrl: String?, val apkSize: Long)

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    object UpToDate : UpdateState()
    /** The repository has no published release yet (HTTP 404). */
    object NoRelease : UpdateState()
    class Available(val info: UpdateInfo) : UpdateState()
    class Failed(val reason: String) : UpdateState()
}

/**
 * Checks https://github.com/Fueeru26/FuruArc/releases for a newer version than the installed one.
 * The app never downloads or installs anything itself: the user is sent to the release page / APK link in the browser.
 */
object UpdateChecker {
    const val REPO = "Fueeru26/FuruArc"
    const val RELEASES_URL = "https://github.com/$REPO/releases"
    private const val API_URL = "https://api.github.com/repos/$REPO/releases/latest"
    private const val NOTIF_CHANNEL = "furuarc_update"
    private const val NOTIF_ID = 7301
    private const val AUTO_INTERVAL_MS = 6L * 60 * 60 * 1000

    var state by mutableStateOf<UpdateState>(UpdateState.Idle)
        private set

    /** Set when an automatic check finds a new version that was not announced yet: the app shows a pop-up once per version. */
    var announce by mutableStateOf<UpdateInfo?>(null)

    val available: Boolean get() = state is UpdateState.Available

    @Volatile private var running = false

    fun installedVersion(ctx: Context): String =
        try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "" } catch (e: Exception) { "" }

    /** Compare dotted versions ("v1.06" vs "1.5.2") by their numbers; true if [latest] is newer than [current]. */
    fun isNewer(latest: String, current: String): Boolean {
        fun nums(v: String) = Regex("\\d+").findAll(v).map { it.value.toIntOrNull() ?: 0 }.toList()
        val a = nums(latest)
        val b = nums(current)
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    /** Manual check (menu): always asks GitHub and shows the outcome in [state]. */
    fun check(ctx: Context) = start(ctx.applicationContext, manual = true)

    /** Automatic check at app start: silent, at most every 6 hours, and nothing happens without a connection. */
    fun autoCheck(ctx: Context) {
        val c = ctx.applicationContext
        val sp = c.getSharedPreferences("update", Context.MODE_PRIVATE)
        val last = sp.getLong("last", 0L)
        if (System.currentTimeMillis() - last < AUTO_INTERVAL_MS && state !is UpdateState.Idle) return
        start(c, manual = false)
    }

    private fun start(c: Context, manual: Boolean) {
        if (running) return
        running = true
        if (manual) state = UpdateState.Checking
        Thread {
            val r = try { fetch(c) } catch (e: Exception) { UpdateState.Failed(e.message ?: e.javaClass.simpleName) }
            val sp = c.getSharedPreferences("update", Context.MODE_PRIVATE)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                running = false
                if (r is UpdateState.Failed && !manual) return@post // silent: offline etc.
                state = r
                sp.edit().putLong("last", System.currentTimeMillis()).apply()
                if (!manual && r is UpdateState.Available && sp.getString("announced", "") != r.info.tag) {
                    sp.edit().putString("announced", r.info.tag).apply()
                    announce = r.info
                    notifyAvailable(c, r.info)
                }
            }
        }.start()
    }

    /**
     * System notification for a new version (once per version). It follows the "Run in background" setting,
     * which is also where the Android 13+ notification permission is requested; without it nothing is shown.
     */
    private fun notifyAvailable(c: Context, info: UpdateInfo) {
        try {
            if (!Engine.bgEnabled) return
            val nm = NotificationManagerCompat.from(c)
            if (!nm.areNotificationsEnabled()) return
            if (Build.VERSION.SDK_INT >= 26) {
                val ch = NotificationChannel(NOTIF_CHANNEL, "FuruArc updates", NotificationManager.IMPORTANCE_DEFAULT)
                c.getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
            }
            val nav = NavStr.of(Engine.lang)
            val open = Intent(Intent.ACTION_VIEW, Uri.parse(info.pageUrl)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val pi = PendingIntent.getActivity(c, 1, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val n = NotificationCompat.Builder(c, NOTIF_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(nav.updNew)
                .setContentText(info.tag + " · " + nav.updNotifTap)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .build()
            nm.notify(NOTIF_ID, n)
        } catch (e: Exception) {
            // the notification is only an extra
        }
    }

    private fun fetch(c: Context): UpdateState {
        val con = URL(API_URL).openConnection() as HttpURLConnection
        try {
            con.connectTimeout = 8000
            con.readTimeout = 8000
            con.setRequestProperty("Accept", "application/vnd.github+json")
            con.setRequestProperty("User-Agent", "FuruArc")
            val code = con.responseCode
            if (code == 404) return UpdateState.NoRelease
            if (code != 200) return UpdateState.Failed("HTTP $code")
            val j = JSONObject(con.inputStream.bufferedReader().use { it.readText() })
            val tag = j.optString("tag_name")
            if (tag.isEmpty()) return UpdateState.NoRelease
            var apk: String? = null
            var size = 0L
            val assets = j.optJSONArray("assets")
            if (assets != null) for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name").endsWith(".apk", ignoreCase = true)) {
                    apk = a.optString("browser_download_url")
                    size = a.optLong("size")
                    break
                }
            }
            val info = UpdateInfo(tag, j.optString("name").ifEmpty { tag }, j.optString("body"), j.optString("html_url").ifEmpty { RELEASES_URL }, apk, size)
            return if (isNewer(tag, installedVersion(c))) UpdateState.Available(info) else UpdateState.UpToDate
        } finally {
            con.disconnect()
        }
    }
}
