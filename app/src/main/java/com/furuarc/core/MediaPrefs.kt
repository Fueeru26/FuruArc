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
class MItem(val name: String, val uri: Uri?, val path: String?, val size: Long, val modified: Long = 0L) {
    val key: String get() = uri?.toString() ?: path ?: name
}

/** Settings of the Media Player (Media Settings page). Saved on every change; exported to / imported from a JSON file together with [MediaLib]. */
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
    // audio
    var pauseUnplug by mutableStateOf(true)
    var audioFocus by mutableStateOf(true)
    var restoreQueue by mutableStateOf(true)
    /** Audio files smaller than this many KB are skipped by folder scans (0 = keep all). */
    var minAudioKb by mutableStateOf(0)
    // video
    var skipSec by mutableStateOf(10)
    var doubleTap by mutableStateOf(true)
    var swipeGestures by mutableStateOf(true)
    /** 0 = automatic, 1 = landscape, 2 = portrait. */
    var videoOrient by mutableStateOf(0)
    // lists
    /** 0 = name, 1 = newest first, 2 = largest first. */
    var sortOrder by mutableStateOf(0)
    var tagTitle by mutableStateOf(true)
    var showCover by mutableStateOf(true)
    var pageQueue by mutableStateOf(50)
    // gallery
    var pageGallery by mutableStateOf(60)
    /** 0 = list, 1 = grid. */
    var galleryMode by mutableStateOf(1)
    var gridCols by mutableStateOf(3)
    /** 0 = fit to screen, 1 = fill the screen width. */
    var imageFit by mutableStateOf(0)
    /** 0 = black, 1 = dark gray, 2 = white. */
    var viewerBg by mutableStateOf(0)

    private class B(val k: String, val get: () -> Boolean, val set: (Boolean) -> Unit, val def: Boolean)
    private class I(val k: String, val get: () -> Int, val set: (Int) -> Unit, val def: Int, val lo: Int, val hi: Int)

    private val bools = listOf(
        B("subfolders", { subfolders }, { subfolders = it }, true), B("autoNext", { autoNext }, { autoNext = it }, true),
        B("shuffle", { shuffle }, { shuffle = it }, false), B("remember", { remember }, { remember = it }, true),
        B("background", { background }, { background = it }, true), B("videoAutoPlay", { videoAutoPlay }, { videoAutoPlay = it }, true),
        B("keepScreenOn", { keepScreenOn }, { keepScreenOn = it }, true), B("pauseUnplug", { pauseUnplug }, { pauseUnplug = it }, true),
        B("audioFocus", { audioFocus }, { audioFocus = it }, true), B("restoreQueue", { restoreQueue }, { restoreQueue = it }, true),
        B("doubleTap", { doubleTap }, { doubleTap = it }, true), B("swipeGestures", { swipeGestures }, { swipeGestures = it }, true),
        B("tagTitle", { tagTitle }, { tagTitle = it }, true), B("showCover", { showCover }, { showCover = it }, true),
    )
    private val ints = listOf(
        I("repeat", { repeat }, { repeat = it }, 0, 0, 2), I("slideshowSec", { slideshowSec }, { slideshowSec = it }, 4, 1, 30),
        I("minAudioKb", { minAudioKb }, { minAudioKb = it }, 0, 0, 10000), I("skipSec", { skipSec }, { skipSec = it }, 10, 1, 120),
        I("videoOrient", { videoOrient }, { videoOrient = it }, 0, 0, 2), I("sortOrder", { sortOrder }, { sortOrder = it }, 0, 0, 2),
        I("pageQueue", { pageQueue }, { pageQueue = it }, 50, 10, 500), I("pageGallery", { pageGallery }, { pageGallery = it }, 60, 10, 500),
        I("galleryMode", { galleryMode }, { galleryMode = it }, 1, 0, 1), I("gridCols", { gridCols }, { gridCols = it }, 3, 2, 4),
        I("imageFit", { imageFit }, { imageFit = it }, 0, 0, 1), I("viewerBg", { viewerBg }, { viewerBg = it }, 0, 0, 2),
    )

    fun init(ctx: Context) {
        if (sp != null) return
        val s = ctx.applicationContext.getSharedPreferences("media_player", Context.MODE_PRIVATE)
        sp = s
        for (b in bools) b.set(s.getBoolean(b.k, b.def))
        for (i in ints) i.set(s.getInt(i.k, i.def).coerceIn(i.lo, i.hi))
        speed = s.getFloat("speed", 1f).coerceIn(0.25f, 3f)
    }

    fun save() {
        val e = sp?.edit() ?: return
        for (b in bools) e.putBoolean(b.k, b.get())
        for (i in ints) e.putInt(i.k, i.get())
        e.putFloat("speed", speed)
        e.apply()
    }

    fun reset() {
        for (b in bools) b.set(b.def)
        for (i in ints) i.set(i.def)
        speed = 1f
        save()
    }

    /** Settings plus every folder, playlist, collection and favorite. */
    fun exportJson(): String {
        val o = JSONObject()
        for (b in bools) o.put(b.k, b.get())
        for (i in ints) o.put(i.k, i.get())
        o.put("speed", speed.toDouble())
        return JSONObject().put("furuarc_media_settings", 2).put("settings", o).put("library", MediaLib.exportJson()).toString(2)
    }

    fun importJson(text: String): Boolean {
        return try {
            val root = JSONObject(text)
            if (!root.has("furuarc_media_settings")) return false
            val o = root.getJSONObject("settings")
            for (b in bools) b.set(o.optBoolean(b.k, b.get()))
            for (i in ints) i.set(o.optInt(i.k, i.get()).coerceIn(i.lo, i.hi))
            speed = o.optDouble("speed", speed.toDouble()).toFloat().coerceIn(0.25f, 3f)
            root.optJSONObject("library")?.let { MediaLib.importJson(it) }
            save()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clearPositions() {
        posMap = LinkedHashMap()
        sp?.edit()?.putString("positions", "{}")?.apply()
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
                        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
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
                            val mod = if (c.isNull(4)) 0L else c.getLong(4)
                            if (kind == 2 && MediaPrefs.minAudioKb > 0 && size in 1 until MediaPrefs.minAudioKb * 1024L) continue
                            out.add(rel to MItem(name, DocumentsContract.buildDocumentUriUsingTree(tree, id), null, size, mod))
                        }
                    }
                }
            } catch (e: Exception) { }
            onCount(out.size)
        }
        when (MediaPrefs.sortOrder) {
            1 -> out.sortByDescending { it.second.modified }
            2 -> out.sortByDescending { it.second.size }
            else -> out.sortWith { a, b ->
                val c = natural(a.first, b.first)
                if (c != 0) c else natural(a.second.name, b.second.name)
            }
        }
        return out.map { it.second }
    }
}
