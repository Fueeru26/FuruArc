package com.furuarc.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import com.furuarc.Engine
import com.furuarc.storage.TreeWriter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.OutputStream

/** Items that can be extracted/shared (media assets and text files, from a folder or from an archive). */
class XItem(val path: String, val size: Long, val uri: Uri, val enc: Boolean, val arc: ArcRef?, val entry: String) {
    val name: String get() = path.substringAfterLast('/')
    /** Output name: encrypted RPGM assets get their original name (png/ogg/m4a). */
    val outPath: String
        get() = if (enc) (RpgmCrypt.decryptedPath(path) ?: path) else path
}

fun GAsset.toX() = XItem(path, size, uri, enc, arc, entry)
fun TextItem.toX() = XItem(path, size, uri, false, arc, entry)

/** Per-game favorites (stored in filesDir/gamefav/<id>.txt, one asset id per line). */
object GameFav {
    var set by mutableStateOf<Set<String>>(emptySet())
        private set
    private var gid: String? = null
    private fun file(id: String) = File(File(Engine.app.filesDir, "gamefav").also { it.mkdirs() }, "$id.txt")

    fun load(gameId: String) {
        if (gid == gameId) return
        gid = gameId
        set = try { file(gameId).readLines().filter { it.isNotEmpty() }.toSet() } catch (e: Exception) { emptySet() }
    }

    fun setFav(ids: Collection<String>, on: Boolean) {
        val g = gid ?: return
        set = if (on) set + ids else set - ids.toSet()
        try { file(g).writeText(set.joinToString("\n")) } catch (e: Exception) { }
    }

    fun toggle(id: String) = setFav(listOf(id), id !in set)

    fun delete(gameId: String) {
        try { file(gameId).delete() } catch (e: Exception) { }
        if (gid == gameId) { gid = null; set = emptySet() }
    }

    fun unload() {
        gid = null
        set = emptySet()
    }
}

/** Extract selected items to the chosen folder (folder structure preserved, under a subfolder named after the game). */
object GameExtract {
    var running by mutableStateOf(false)
        private set
    var finished by mutableStateOf(false)
        private set
    var done by mutableStateOf(0)
        private set
    var total by mutableStateOf(0)
        private set
    var failed by mutableStateOf(0)
        private set
    var current by mutableStateOf("")
        private set
    var folder by mutableStateOf("")
        private set
    private var job: Job? = null

    fun start(ctx: Context, items: List<XItem>, tree: Uri, root: String, folderName: String, key: ByteArray?) {
        if (running) return
        running = true; finished = false; done = 0; failed = 0; total = items.size; current = ""; folder = folderName
        job = Engine.scope.launch {
            try {
                val w = TreeWriter(ctx, tree)
                val base = root.replace(Regex("[\\\\/:*?\"<>|]"), "_").trim().ifEmpty { "game" }
                for (it in items) {
                    kotlinx.coroutines.currentCoroutineContext().ensureActive()
                    current = it.name
                    try {
                        val of = w.open(base + "/" + it.outPath.replace('\\', '/'), ConflictPolicy.OVERWRITE)
                        if (of != null) of.stream.use { o -> copy(ctx, it, o, key) }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        failed++
                    }
                    done++
                }
            } finally {
                running = false
                finished = true
            }
        }
    }

    fun copy(ctx: Context, it: XItem, o: OutputStream, key: ByteArray?) {
        val arc = it.arc
        if (arc != null) {
            GameArc.extractTo(ctx, arc, it.entry, o)
        } else {
            val ins = ctx.contentResolver.openInputStream(it.uri) ?: throw java.io.IOException("open")
            ins.use { i ->
                if (it.enc) {
                    if (key == null) throw java.io.IOException("key")
                    RpgmCrypt.decrypt(i, o, key)
                } else {
                    i.copyTo(o)
                }
            }
        }
    }

    fun cancel() {
        job?.cancel()
    }

    fun dismiss() {
        if (!running) finished = false
    }
}

/** Share items via another app: items are first copied to the cache (max. 20 files / 200 MB) then shared via FileProvider. */
object GameShare {
    const val MAX_FILES = 20
    const val MAX_BYTES = 200L * 1024 * 1024
    var busy by mutableStateOf(false)
        private set

    fun share(ctx: Context, items: List<XItem>, key: ByteArray?, onError: (Int) -> Unit) {
        if (busy) return
        if (items.size > MAX_FILES) { onError(1); return }
        if (items.sumOf { it.size } > MAX_BYTES) { onError(2); return }
        busy = true
        Engine.scope.launch {
            try {
                val dir = File(ctx.cacheDir, "share")
                dir.deleteRecursively()
                dir.mkdirs()
                val uris = ArrayList<Uri>()
                for ((i, it) in items.withIndex()) {
                    val f = File(dir, i.toString() + "_" + it.outPath.substringAfterLast('/'))
                    f.outputStream().use { o -> GameExtract.copy(ctx, it, o, key) }
                    uris.add(FileProvider.getUriForFile(ctx, ctx.packageName + ".gamefiles", f))
                }
                val intent = if (uris.size == 1) Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, uris[0])
                else Intent(Intent.ACTION_SEND_MULTIPLE).putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                intent.type = if (uris.size == 1) mimeOf(items[0].outPath) else "*/*"
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val chooser = Intent.createChooser(intent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Handler(Looper.getMainLooper()).post { try { ctx.startActivity(chooser) } catch (e: Exception) { onError(3) } }
            } catch (e: Exception) {
                onError(3)
            } finally {
                busy = false
            }
        }
    }

    private fun mimeOf(p: String): String = when (p.substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"; "jpg", "jpeg" -> "image/jpeg"; "gif" -> "image/gif"; "webp" -> "image/webp"; "bmp" -> "image/bmp"
        "ogg", "oga", "opus" -> "audio/ogg"; "mp3" -> "audio/mpeg"; "wav" -> "audio/wav"; "m4a", "aac" -> "audio/mp4"; "flac" -> "audio/flac"
        "mp4", "m4v" -> "video/mp4"; "webm" -> "video/webm"; "mkv" -> "video/x-matroska"
        "txt", "ks", "rpy", "csv", "ini", "log", "md", "json", "xml" -> "text/plain"
        else -> "*/*"
    }
}
