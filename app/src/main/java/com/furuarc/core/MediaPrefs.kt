package com.furuarc.core

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.json.JSONObject

/** Exact seek: the default seekTo() snaps to the previous sync frame, which makes taps on the seek bar look ignored. */
fun MediaPlayer.seekExact(ms: Int) {
    try {
        if (Build.VERSION.SDK_INT >= 26) seekTo(ms.toLong(), MediaPlayer.SEEK_CLOSEST) else seekTo(ms)
    } catch (e: Throwable) { }
}

/** One playable / viewable item of the Media Player (a document URI or a plain file path). */
class MItem(val name: String, val uri: Uri?, val path: String?, val size: Long) {
    val key: String get() = uri?.toString() ?: path ?: name
}

/** Settings of the Media Player (Media Settings page). Saved on every change; can be exported to / imported from a JSON file. */
object MediaPrefs {
    private var sp: SharedPreferences? = null

    var subfolders by mutableStateOf(true)
    var autoNext by mutableStateOf(true)
    var shuffle by mutableStateOf(false)
    /** 0 = off, 1 = all, 2 = one. */
    var repeat by mutableStateOf(0)
    var speed by mutableStateOf(1f)
    var remember by mutableStateOf(true)
    var background by mutableStateOf(true)
    var slideshowSec by mutableStateOf(4)
    var videoAutoPlay by mutableStateOf(true)
    var keepScreenOn by mutableStateOf(true)

    fun init(ctx: Context) {
        if (sp != null) return
        val s = ctx.applicationContext.getSharedPreferences("media_player", Context.MODE_PRIVATE)
        sp = s
        subfolders = s.getBoolean("subfolders", true)
        autoNext = s.getBoolean("autoNext", true)
        shuffle = s.getBoolean("shuffle", false)
        repeat = s.getInt("repeat", 0).coerceIn(0, 2)
        speed = s.getFloat("speed", 1f).coerceIn(0.25f, 3f)
        remember = s.getBoolean("remember", true)
        background = s.getBoolean("background", true)
        slideshowSec = s.getInt("slideshowSec", 4).coerceIn(1, 30)
        videoAutoPlay = s.getBoolean("videoAutoPlay", true)
        keepScreenOn = s.getBoolean("keepScreenOn", true)
    }

    fun save() {
        sp?.edit()
            ?.putBoolean("subfolders", subfolders)?.putBoolean("autoNext", autoNext)?.putBoolean("shuffle", shuffle)
            ?.putInt("repeat", repeat)?.putFloat("speed", speed)?.putBoolean("remember", remember)
            ?.putBoolean("background", background)?.putInt("slideshowSec", slideshowSec)
            ?.putBoolean("videoAutoPlay", videoAutoPlay)?.putBoolean("keepScreenOn", keepScreenOn)
            ?.apply()
    }

    fun reset() {
        subfolders = true; autoNext = true; shuffle = false; repeat = 0; speed = 1f; remember = true
        background = true; slideshowSec = 4; videoAutoPlay = true; keepScreenOn = true
        save()
    }

    fun exportJson(): String {
        val o = JSONObject()
        o.put("subfolders", subfolders); o.put("autoNext", autoNext); o.put("shuffle", shuffle)
        o.put("repeat", repeat); o.put("speed", speed.toDouble()); o.put("remember", remember)
        o.put("background", background); o.put("slideshowSec", slideshowSec)
        o.put("videoAutoPlay", videoAutoPlay); o.put("keepScreenOn", keepScreenOn)
        return JSONObject().put("furuarc_media_settings", 1).put("settings", o).toString(2)
    }

    fun importJson(text: String): Boolean {
        return try {
            val root = JSONObject(text)
            if (!root.has("furuarc_media_settings")) return false
            val o = root.getJSONObject("settings")
            subfolders = o.optBoolean("subfolders", subfolders)
            autoNext = o.optBoolean("autoNext", autoNext)
            shuffle = o.optBoolean("shuffle", shuffle)
            repeat = o.optInt("repeat", repeat).coerceIn(0, 2)
            speed = o.optDouble("speed", speed.toDouble()).toFloat().coerceIn(0.25f, 3f)
            remember = o.optBoolean("remember", remember)
            background = o.optBoolean("background", background)
            slideshowSec = o.optInt("slideshowSec", slideshowSec).coerceIn(1, 30)
            videoAutoPlay = o.optBoolean("videoAutoPlay", videoAutoPlay)
            keepScreenOn = o.optBoolean("keepScreenOn", keepScreenOn)
            save()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---- remembered playback positions (audio / video), newest last, at most 300 entries
    private var posMap: LinkedHashMap<String, Int>? = null

    private fun posStore(): LinkedHashMap<String, Int> {
        posMap?.let { return it }
        val m = LinkedHashMap<String, Int>()
        try {
            val o = JSONObject(sp?.getString("positions", "{}") ?: "{}")
            for (k in o.keys()) m[k] = o.getInt(k)
        } catch (e: Exception) { }
        posMap = m
        return m
    }

    fun savedPos(key: String): Int = if (remember) posStore()[key] ?: 0 else 0

    fun savePos(key: String, ms: Int, duration: Int) {
        if (!remember) return
        val m = posStore()
        m.remove(key)
        // near the start or the end: forget the position so the next play starts from the beginning
        if (ms > 3000 && duration > 15000 && ms < duration - 4000) m[key] = ms
        while (m.size > 300) m.remove(m.keys.first())
        val o = JSONObject()
        for ((k, v) in m) o.put(k, v)
        sp?.edit()?.putString("positions", o.toString())?.apply()
    }
}

/** Finds images / audio / video in a picked folder. */
object MediaScan {
    val IMG = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif", "heic", "heif")
    val AUD = setOf("mp3", "ogg", "oga", "wav", "m4a", "aac", "flac", "opus", "wma", "mid", "amr", "mka", "ac3")
    val VID = setOf("mp4", "mkv", "webm", "3gp", "avi", "mov", "m4v", "ts", "wmv", "flv", "mpg", "mpeg")

    fun ext(name: String): String = name.substringAfterLast('.', "").lowercase()

    /** 1 = image, 2 = audio, 3 = video, 0 = other. */
    fun kindOf(name: String): Int {
        val e = ext(name)
        return when { e in IMG -> 1; e in AUD -> 2; e in VID -> 3; else -> 0 }
    }

    private fun natural(a: String, b: String): Int {
        val x = a.lowercase(); val y = b.lowercase()
        var i = 0; var j = 0
        while (i < x.length && j < y.length) {
            if (x[i].isDigit() && y[j].isDigit()) {
                var i2 = i; while (i2 < x.length && x[i2].isDigit()) i2++
                var j2 = j; while (j2 < y.length && y[j2].isDigit()) j2++
                val n1 = x.substring(i, i2).trimStart('0'); val n2 = y.substring(j, j2).trimStart('0')
                if (n1.length != n2.length) return n1.length - n2.length
                val c = n1.compareTo(n2)
                if (c != 0) return c
                i = i2; j = j2
            } else {
                if (x[i] != y[j]) return x[i] - y[j]
                i++; j++
            }
        }
        return (x.length - i) - (y.length - j)
    }

    /** Files of [kind] in the picked folder (and its subfolders when [recursive]); sorted by folder, then name (numbers sorted naturally). */
    suspend fun scanTree(ctx: Context, tree: Uri, kind: Int, recursive: Boolean, onCount: (Int) -> Unit = {}): List<MItem> {
        val out = ArrayList<Pair<String, MItem>>()
        val cr = ctx.contentResolver
        val rootId = try {
            DocumentsContract.getTreeDocumentId(tree)
        } catch (e: Exception) {
            DocumentsContract.getDocumentId(tree)
        }
        val stack = ArrayList<Pair<String, String>>()
        stack.add(rootId to "")
        while (stack.isNotEmpty() && out.size < 30000) {
            currentCoroutineContext().ensureActive()
            val (docId, rel) = stack.removeAt(stack.size - 1)
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
            try {
                cr.query(
                    childrenUri,
                    arrayOf(
                        DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                        DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE,
                    ),
                    null, null, null,
                )?.use { c ->
                    while (c.moveToNext()) {
                        val id = c.getString(0) ?: continue
                        val name = c.getString(1) ?: continue
                        val mime = c.getString(2) ?: ""
                        if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                            if (recursive) stack.add(id to (if (rel.isEmpty()) name else "$rel/$name"))
                        } else if (kindOf(name) == kind) {
                            val size = if (c.isNull(3)) 0L else c.getLong(3)
                            out.add(rel to MItem(name, DocumentsContract.buildDocumentUriUsingTree(tree, id), null, size))
                        }
                    }
                }
            } catch (e: Exception) { }
            onCount(out.size)
        }
        out.sortWith { a, b ->
            val c = natural(a.first, b.first)
            if (c != 0) c else natural(a.second.name, b.second.name)
        }
        return out.map { it.second }
    }
}
