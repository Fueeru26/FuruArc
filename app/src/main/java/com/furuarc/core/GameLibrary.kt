package com.furuarc.core

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.Engine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/** A game in the Game Assets list. [cover]/[auto] = file names in the covers folder (chosen cover / automatic game icon). */
data class GameEntry(
    val id: String,
    val name: String,
    val tree: String,
    val cover: String? = null,
    val auto: String? = null,
    val engine: String = "",
    val missing: Boolean = false,
)

/** Game list (stored in game_library.json). Only list entries; the original folders are never modified/deleted. */
object GameLibrary {
    var entries by mutableStateOf<List<GameEntry>>(emptyList())
        private set
    var busy by mutableStateOf(false)
        private set
    /** Find automatic covers for many games: (done, total, game name). */
    var coverJob by mutableStateOf<Triple<Int, Int, String>?>(null)
        private set
    /** Summary of the cover search result (shown once, then cleared via [clearCoverResult]). */
    var coverResult by mutableStateOf<Pair<Int, Int>?>(null)
        private set
    private var coverRun: Job? = null
    private var loaded = false
    private val app get() = Engine.app
    private fun store() = File(app.filesDir, "game_library.json")
    fun coverDir(): File = File(app.filesDir, "covers").also { it.mkdirs() }
    private fun coverFile(name: String?): File? = if (name == null) null else File(coverDir(), name).takeIf { it.exists() }
    fun displayCover(e: GameEntry): File? = coverFile(e.cover) ?: coverFile(e.auto)

    @Synchronized
    fun load() {
        if (loaded) return
        loaded = true
        try {
            val f = store()
            if (!f.exists()) return
            val root = Json.parse(f.readText()) as? Map<*, *> ?: return
            val arr = root["games"] as? List<*> ?: return
            entries = arr.mapNotNull { m ->
                val mm = m as? Map<*, *> ?: return@mapNotNull null
                val id = mm["id"] as? String ?: return@mapNotNull null
                val tree = mm["tree"] as? String ?: return@mapNotNull null
                GameEntry(id, mm["name"] as? String ?: "?", tree, mm["cover"] as? String, mm["auto"] as? String, mm["engine"] as? String ?: "")
            }
        } catch (e: Exception) {
            // corrupt file: start from an empty list
        }
    }

    private fun save() {
        try {
            val list = ArrayList<Any?>()
            for (e in entries) {
                val m = LinkedHashMap<String, Any?>()
                m["id"] = e.id
                m["name"] = e.name
                m["tree"] = e.tree
                if (e.cover != null) m["cover"] = e.cover
                if (e.auto != null) m["auto"] = e.auto
                m["engine"] = e.engine
                list.add(m)
            }
            val root = LinkedHashMap<String, Any?>()
            root["games"] = list
            val tmp = File(store().path + ".tmp")
            tmp.writeText(Json.write(root, true))
            tmp.renameTo(store())
        } catch (e: Exception) {
            // best effort
        }
    }

    private fun sameFolder(a: Uri, b: Uri): Boolean =
        a == b || (a.authority == b.authority && GameScan.rootDocId(app, a) == GameScan.rootDocId(app, b))

    fun has(tree: Uri) = entries.any { sameFolder(Uri.parse(it.tree), tree) }

    private fun persist(tree: Uri) {
        try {
            app.contentResolver.takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Exception) {
            // still usable as long as the session permission exists
        }
    }

    /** The original folder is untouched: only a copy of the cover (<=600 px) is stored in app storage. */
    private fun saveCover(src: Uri, name: String): String? {
        val bmp = Thumbs.decodeScaled({ app.contentResolver.openInputStream(src) }, 600) ?: return null
        FileOutputStream(File(coverDir(), name)).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return name
    }

    private fun scanMeta(e: GameEntry): GameEntry {
        val files = GameScan.walk(app, Uri.parse(e.tree))
        val engine = GameScan.engineOf(files)
        var auto = e.auto
        if (e.cover == null && (auto == null || coverFile(auto) == null)) {
            val ic = GameScan.findIcon(files)
            if (ic != null) {
                try {
                    val key = GameScan.sysInfo(app, files).first
                    val a = GameScan.classify(ic)
                    val bmp = if (a != null) Thumbs.assetBitmap(app, a, key, 600) else null
                    if (bmp != null) {
                        val n = "a_${e.id}.png"
                        FileOutputStream(File(coverDir(), n)).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
                        auto = n
                    }
                } catch (ex: Exception) {
                    // no automatic cover
                }
            }
        }
        return e.copy(engine = engine, auto = auto, missing = false)
    }

    fun add(name: String, tree: Uri, cover: Uri?, onDone: (String?) -> Unit) {
        busy = true
        Engine.scope.launch {
            var err: String? = null
            try {
                persist(tree)
                val id = java.lang.Long.toString(System.currentTimeMillis(), 36)
                val cv = if (cover != null) saveCover(cover, "c_$id.png") else null
                var e = GameEntry(id, name.trim(), tree.toString(), cv)
                try {
                    e = scanMeta(e)
                } catch (ex: Exception) {
                    err = ex.message ?: ex.toString()
                }
                entries = entries + e
                save()
            } catch (ex: Exception) {
                err = ex.message ?: ex.toString()
            } finally {
                busy = false
                onDone(err)
            }
        }
    }

    /** Add many games at once: each direct subfolder of [tree] becomes one entry. Existing ones are skipped. */
    fun addMain(tree: Uri, onDone: (Int, Int) -> Unit) {
        busy = true
        Engine.scope.launch {
            var added = 0
            var skipped = 0
            try {
                persist(tree)
                val dirs = androidx.documentfile.provider.DocumentFile.fromTreeUri(app, tree)?.listFiles()
                    ?.filter { it.isDirectory }?.sortedBy { (it.name ?: "").lowercase() } ?: emptyList()
                val base = System.currentTimeMillis()
                for ((i, d) in dirs.withIndex()) {
                    val uri = d.uri
                    if (has(uri)) { skipped++; continue }
                    var e = GameEntry(java.lang.Long.toString(base + i, 36), (d.name ?: "game").trim(), uri.toString(), null)
                    try { e = scanMeta(e) } catch (ex: Exception) { /* still added without engine info */ }
                    entries = entries + e
                    added++
                    save()
                }
            } catch (ex: Exception) {
                // ignore; partial results are still reported
            } finally {
                busy = false
                onDone(added, skipped)
            }
        }
    }

    /** Clear the whole list (entries only, not the original folders). */
    fun clearAll() {
        for (e in entries.toList()) remove(e.id)
    }

    fun rename(id: String, name: String) {
        entries = entries.map { if (it.id == id) it.copy(name = name.trim()) else it }
        save()
    }

    fun setCover(id: String, uri: Uri) {
        Engine.scope.launch {
            try {
                val old = entries.firstOrNull { it.id == id }?.cover
                val n = saveCover(uri, "c_${id}_${System.currentTimeMillis()}.png") ?: return@launch
                entries = entries.map { if (it.id == id) it.copy(cover = n) else it }
                if (old != null) File(coverDir(), old).delete()
                save()
            } catch (ex: Exception) {
                // ignore
            }
        }
    }

    fun clearCoverResult() {
        coverResult = null
    }

    fun cancelCovers() {
        coverRun?.cancel()
    }

    /** Search the internet for covers (VNDB, Steam, Google) for all games, or only those without a chosen cover. */
    fun autoCovers(onlyMissing: Boolean) {
        if (coverRun?.isActive == true) return
        val targets = entries.filter { !onlyMissing || it.cover == null }
        if (targets.isEmpty()) return
        coverRun = Engine.scope.launch {
            var found = 0
            try {
                for ((i, e) in targets.withIndex()) {
                    coverJob = Triple(i, targets.size, e.name)
                    val r = try { CoverSearch.auto(e.name, e.id) } catch (ce: CancellationException) { throw ce } catch (x: Exception) { null } ?: continue
                    if (entries.none { it.id == e.id }) continue
                    val old = entries.firstOrNull { it.id == e.id }?.cover
                    val n = saveCover(Uri.fromFile(r.first), "c_${e.id}_${System.currentTimeMillis()}.png") ?: continue
                    entries = entries.map { if (it.id == e.id) it.copy(cover = n) else it }
                    if (old != null) File(coverDir(), old).delete()
                    save()
                    found++
                }
            } finally {
                coverJob = null
                coverResult = Pair(found, targets.size)
            }
        }
    }

    /** Remove the entry from the list only (not the original folder/file). */
    fun remove(id: String) {
        val e = entries.firstOrNull { it.id == id } ?: return
        entries = entries.filter { it.id != id }
        GameSession.forget(id)
        e.cover?.let { File(coverDir(), it).delete() }
        e.auto?.let { File(coverDir(), it).delete() }
        if (entries.none { it.tree == e.tree }) {
            try {
                app.contentResolver.releasePersistableUriPermission(Uri.parse(e.tree), Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (ex: Exception) {
                // permission was never saved
            }
        }
        save()
    }

    /** Refresh: check the folder can still be opened, update the engine badge and automatic cover. */
    fun refresh() {
        if (busy) return
        busy = true
        Engine.scope.launch {
            try {
                val out = ArrayList<GameEntry>()
                for (e in entries) {
                    out.add(try { scanMeta(e) } catch (ex: Exception) { e.copy(missing = true) })
                }
                entries = out
                save()
                GameSession.invalidate()
            } finally {
                busy = false
            }
        }
    }
}

/** Decode small images + memory cache. */
object Thumbs {
    private val cache = object : LruCache<String, Bitmap>(32 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, b: Bitmap) {
        cache.put(key, b)
    }
    fun clear() = cache.evictAll()

    fun decodeScaled(open: () -> InputStream?, max: Int): Bitmap? {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        open()?.use { BitmapFactory.decodeStream(it, null, o) }
        if (o.outWidth <= 0 || o.outHeight <= 0) return null
        var s = 1
        while (o.outWidth / (s * 2) >= max && o.outHeight / (s * 2) >= max) s *= 2
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = s
        return open()?.use { BitmapFactory.decodeStream(it, null, o2) }
    }

    /** Asset image (encrypted RPGM is decrypted in memory). null = failed / key missing. */
    fun assetBitmap(ctx: Context, a: GAsset, key: ByteArray?, max: Int): Bitmap? {
        if (a.special) return SpecialMedia.bitmap(ctx, a, max)
        if (a.arc != null) {
            val bytes = GameArc.bytes(ctx, a.arc, a.entry, 40L * 1024 * 1024)
            return decodeScaled({ ByteArrayInputStream(bytes) }, max)
        }
        if (a.enc) {
            if (key == null || a.size > 40L * 1024 * 1024) return null
            val bo = ByteArrayOutputStream()
            val ok = ctx.contentResolver.openInputStream(a.uri)?.use { RpgmCrypt.decrypt(it, bo, key); true } ?: false
            if (!ok) return null
            val bytes = bo.toByteArray()
            return decodeScaled({ ByteArrayInputStream(bytes) }, max)
        }
        return decodeScaled({ ctx.contentResolver.openInputStream(a.uri) }, max)
    }

    fun videoFrame(ctx: Context, uri: Uri, max: Int): Bitmap? {
        val r = MediaMetadataRetriever()
        return try {
            r.setDataSource(ctx, uri)
            val f = r.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC) ?: r.getFrameAtTime(0)
            if (f == null) null else {
                val sc = max.toFloat() / maxOf(f.width, f.height)
                if (sc < 1f) Bitmap.createScaledBitmap(f, (f.width * sc).toInt().coerceAtLeast(1), (f.height * sc).toInt().coerceAtLeast(1), true) else f
            }
        } catch (e: Exception) {
            null
        } finally {
            try { r.release() } catch (e: Exception) { }
        }
    }
}

/** Temporary cache (audio/video from archives, decryption backups) in cacheDir/gameview, limited by [limit]. */
object GameCache {
    const val DEFAULT_MB = 1024
    val CHOICES_MB = listOf(200, 500, 1024, 2048, 5120, 10240)
    private fun sp() = Engine.app.getSharedPreferences("game_cache", Context.MODE_PRIVATE)
    fun limitMb(): Int = sp().getInt("limit_mb", DEFAULT_MB)
    fun setLimitMb(mb: Int) {
        sp().edit().putInt("limit_mb", mb).apply()
        trim()
    }
    fun limit(): Long = limitMb().toLong() * 1024 * 1024
    fun dir() = File(Engine.app.cacheDir, "gameview").also { it.mkdirs() }
    fun size(): Long {
        var t = 0L
        dir().listFiles()?.forEach { t += it.length() }
        return t
    }
    fun clear() {
        dir().listFiles()?.forEach { it.delete() }
        Thumbs.clear()
    }
    fun trim() {
        val fs = dir().listFiles()?.sortedBy { it.lastModified() } ?: return
        var tot = 0L
        fs.forEach { tot += it.length() }
        for (f in fs) {
            if (tot <= limit()) break
            tot -= f.length()
            f.delete()
        }
    }
    fun decrypted(ctx: Context, a: GAsset, key: ByteArray): File? {
        val ext = RpgmCrypt.decryptedPath(a.name)?.substringAfterLast('.') ?: "bin"
        val name = Integer.toHexString(a.uri.toString().hashCode()) + "_" + a.size + "." + ext
        val f = File(dir(), name)
        if (f.exists() && f.length() > 0) {
            f.setLastModified(System.currentTimeMillis())
            return f
        }
        val tmp = File(dir(), "$name.part")
        val ok = ctx.contentResolver.openInputStream(a.uri)?.use { ins ->
            FileOutputStream(tmp).use { RpgmCrypt.decrypt(ins, it, key) }
            true
        } ?: false
        if (!ok) return null
        tmp.renameTo(f)
        trim()
        return f
    }
}
