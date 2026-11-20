package com.furuarc.core

import android.media.MediaDataSource
import android.media.MediaPlayer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.Engine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer

/**
 * The currently open game. The index is stored (memory + disk) so the next open shows immediately without
 * "Scanning"; in the background the folder is checked silently and only rescanned if something changed.
 */
object GameSession {
    var entry by mutableStateOf<GameEntry?>(null)
        private set
    var loading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var index by mutableStateOf<GameIndex?>(null)
        private set
    var texts by mutableStateOf<List<TextItem>?>(null)
        private set
    /** Archive scan in progress: (done, total, name). */
    var arcProgress by mutableStateOf<Triple<Int, Int, String>?>(null)
        private set
    /** Folder changed, list is being updated. */
    var updating by mutableStateOf(false)
        private set

    /** ids of assets that failed to display/play (encrypted, unsupported format, etc.); filled when opened. */
    val failed = androidx.compose.runtime.mutableStateMapOf<String, Boolean>()

    fun markFailed(id: String?) {
        if (id != null) failed[id] = true
    }

    private class Cached(val index: GameIndex, val texts: List<TextItem>, var verifiedAt: Long)

    private val mem = LinkedHashMap<String, Cached>()
    private var job: Job? = null
    private const val VERIFY_MS = 5 * 60 * 1000L

    private fun remember(id: String, c: Cached) {
        mem.remove(id)
        mem[id] = c
        while (mem.size > 3) mem.remove(mem.keys.first())
    }

    fun open(e: GameEntry) {
        if (entry?.id == e.id && (index != null || loading)) return
        job?.cancel()
        entry = e
        GameFav.load(e.id)
        index = null; texts = null; error = null; arcProgress = null; updating = false
        failed.clear()
        val tree = Uri.parse(e.tree)
        val c = mem[e.id] ?: GameIdx.load(e.id, tree)?.let { Cached(it.first, it.second, 0L) }?.also { remember(e.id, it) }
        if (c != null) {
            index = c.index; texts = c.texts; loading = false
            if (System.currentTimeMillis() - c.verifiedAt > VERIFY_MS) {
                job = Engine.scope.launch {
                    try {
                        val files = GameScan.walk(Engine.app, tree)
                        if (GameScan.signature(files) == c.index.sig) {
                            c.verifiedAt = System.currentTimeMillis()
                        } else {
                            updating = true
                            fullScan(e, files)
                        }
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (ex: Exception) {
                        // folder cannot be checked right now: keep using the stored list
                    } finally {
                        updating = false
                    }
                }
            }
            return
        }
        loading = true
        job = Engine.scope.launch {
            try {
                fullScan(e, GameScan.walk(Engine.app, tree))
            } catch (ce: CancellationException) {
                throw ce
            } catch (ex: Exception) {
                error = ex.message ?: ex.toString()
                loading = false
            }
        }
    }

    private suspend fun fullScan(e: GameEntry, files: List<GFile>) {
        val app = Engine.app
        var idx = GameScan.buildIndex(app, files)
        index = idx
        loading = false
        var tx: List<TextItem> = GameScan.loadTexts(app, idx)
        texts = tx
        val arcs = ArrayList<ArcStatus>()
        val a = ArrayList<GAsset>(idx.audio)
        val im = ArrayList<GAsset>(idx.images)
        val vi = ArrayList<GAsset>(idx.videos)
        val tt = ArrayList<TextItem>(tx)
        try {
            GameArc.scan(
                app, files,
                onProgress = { d, t, n -> arcProgress = if (t == 0 || d >= t) null else Triple(d, t, n) },
                onArchive = { st, assets, tx2 ->
                    arcs.add(st)
                    for (x in assets) when (x.kind) { 0 -> a.add(x); 1 -> im.add(x); else -> vi.add(x) }
                    tt.addAll(tx2)
                    idx = idx.copy(audio = ArrayList(a), images = ArrayList(im), videos = ArrayList(vi), arcs = ArrayList(arcs))
                    index = idx
                    texts = ArrayList(tt)
                },
            )
        } finally {
            arcProgress = null
        }
        tx = ArrayList(tt)
        remember(e.id, Cached(idx, tx, System.currentTimeMillis()))
        GameIdx.save(e.id, idx, tx)
    }

    /** Force a rescan (reload button on the game page). */
    fun reload() {
        val e = entry ?: return
        job?.cancel()
        mem.remove(e.id)
        GameIdx.delete(e.id)
        entry = null
        open(e)
    }

    /** Mark all indexes as needing a recheck (after Refresh). */
    fun invalidate() {
        mem.values.forEach { it.verifiedAt = 0L }
    }

    fun forget(id: String) {
        mem.remove(id)
        GameIdx.delete(id)
        GameFav.delete(id)
        if (entry?.id == id) close()
    }

    fun close() {
        job?.cancel()
        entry = null
        index = null
        texts = null
        error = null
        loading = false
        arcProgress = null
        updating = false
        GameFav.unload()
        GameArc.closeAll()
    }
}

/** MediaPlayer data source for encrypted RPGM assets: decrypts directly while reading, without a cache file. */
class RpgmDataSource(private val pfd: ParcelFileDescriptor, private val key: ByteArray) : MediaDataSource() {
    private val stream = FileInputStream(pfd.fileDescriptor)
    private val ch = stream.channel
    private val total = ch.size() - 32

    override fun getSize(): Long = total

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, size: Int): Int {
        if (position >= total) return -1
        val n = minOf(size.toLong(), total - position).toInt()
        var got = 0
        while (got < n) {
            val r = ch.read(ByteBuffer.wrap(buffer, offset + got, n - got), position + 32 + got)
            if (r <= 0) break
            got += r
        }
        if (got <= 0) return -1
        var p = position
        while (p < 16 && p < position + got) {
            val j = (offset + (p - position)).toInt()
            buffer[j] = (buffer[j].toInt() xor key[p.toInt()].toInt()).toByte()
            p++
        }
        return got
    }

    override fun close() {
        try { stream.close() } catch (e: Exception) { }
        try { pfd.close() } catch (e: Exception) { }
    }
}

/** Mini audio player (one track at a time). */
object GamePlayer {
    var current by mutableStateOf<GAsset?>(null)
        private set
    var playing by mutableStateOf(false)
        private set
    var preparing by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set
    var duration by mutableStateOf(0)
        private set
    var position by mutableStateOf(0)
    private var mp: MediaPlayer? = null
    private var token = 0

    fun play(a: GAsset, key: ByteArray?) {
        stop()
        val my = ++token
        current = a
        preparing = true
        failed = false
        Engine.scope.launch {
            val p = MediaPlayer()
            try {
                val ctx = Engine.app
                if (a.special) {
                    p.setDataSource(SpecialMedia.audioFile(ctx, a).absolutePath)
                } else if (a.arc != null) {
                    p.setDataSource(GameArc.toFile(ctx, a).absolutePath)
                } else if (a.enc) {
                    if (key == null) throw IOException("key")
                    // always decrypt to a cache file first: MediaDataSource fails at prepare (async error) on some devices
                    val f = GameCache.decrypted(ctx, a, key) ?: throw IOException("dec")
                    p.setDataSource(f.absolutePath)
                } else {
                    p.setDataSource(ctx, a.uri)
                }
                mp = p
                p.setOnPreparedListener {
                    if (token == my) {
                        duration = it.duration
                        it.start()
                        playing = true
                        preparing = false
                    }
                }
                p.setOnCompletionListener {
                    if (token == my) {
                        playing = false
                        position = 0
                    }
                }
                p.setOnErrorListener { _, _, _ ->
                    if (token == my) {
                        GameSession.markFailed(current?.id)
                        failed = true
                        playing = false
                        preparing = false
                    }
                    true
                }
                p.prepareAsync()
            } catch (e: Exception) {
                try { p.release() } catch (x: Exception) { }
                if (token == my) {
                    SpecialMedia.errors[a.id] = SpecialMedia.describe(e)
                    failed = true
                    preparing = false
                }
            }
        }
    }

    fun toggle() {
        val p = mp ?: return
        try {
            if (p.isPlaying) {
                p.pause()
                playing = false
            } else {
                p.start()
                playing = true
            }
        } catch (e: Exception) {
            failed = true
        }
    }

    fun seek(ms: Int) {
        try { mp?.seekTo(ms); position = ms } catch (e: Exception) { }
    }

    fun tick() {
        try { mp?.let { if (it.isPlaying) position = it.currentPosition } } catch (e: Exception) { }
    }

    fun stop() {
        token++
        try { mp?.release() } catch (e: Exception) { }
        mp = null
        current = null
        playing = false
        preparing = false
        failed = false
        position = 0
        duration = 0
    }
}
