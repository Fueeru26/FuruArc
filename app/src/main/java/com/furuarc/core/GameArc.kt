package com.furuarc.core

import android.content.Context
import com.furuarc.Engine
import com.furuarc.storage.ChannelSource
import kotlinx.coroutines.ensureActive
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.coroutines.coroutineContext

/** Preview of game archive contents (xp3, rpa, pfs, etc.) using the formats already in FormatRegistry. */
object GameArc {
    private val EXTS: Set<String> by lazy { FormatRegistry.all.flatMap { it.extensions }.toSet() - setOf("zip") }

    private fun ext(p: String) = p.substringAfterLast('/').substringAfterLast('.', "").lowercase()

    fun candidates(files: List<GFile>): List<GFile> = files.filter { it.size > 1024 && ext(it.path) in EXTS }

    private fun detectFmt(src: RandomSource, e: String): ArchiveFormat? {
        val n = minOf(16L, src.length).toInt()
        if (n <= 0) return null
        val head = src.readFully(0, n)
        return FormatRegistry.all.firstOrNull { e in it.extensions && runCatching { it.detects(src, head) }.getOrDefault(false) }
    }

    /**
     * Index all archives: each finished archive is reported immediately via [onArchive]
     * (status, media assets, text items) so the list can appear incrementally.
     */
    suspend fun scan(
        ctx: Context,
        files: List<GFile>,
        onProgress: (Int, Int, String) -> Unit,
        onArchive: (ArcStatus, List<GAsset>, List<TextItem>) -> Unit,
    ) {
        val cands = candidates(files)
        for ((i, f) in cands.withIndex()) {
            coroutineContext.ensureActive()
            onProgress(i, cands.size, f.path.substringAfterLast('/'))
            try {
                val pfd = ctx.contentResolver.openFileDescriptor(f.uri, "r") ?: continue
                ChannelSource(pfd).use { src ->
                    val fmt = detectFmt(src, ext(f.path)) ?: return@use
                    val list = fmt.list(src, Options())
                    val key = try { fmt.detectKey(src, list)?.takeIf { it != 0 } } catch (e: Exception) { null }
                    val ref = ArcRef(f.uri, fmt.id, key, f.path)
                    val assets = ArrayList<GAsset>()
                    val texts = ArrayList<TextItem>()
                    for (e in list) {
                        val nm = e.name.replace('\\', '/')
                        val full = f.path + "/" + nm
                        val k = GameScan.kindOfName(nm)
                        when {
                            k >= 0 -> assets.add(GAsset(full, e.size, f.uri, k, false, ref, e.name, GameScan.isSpecial(nm)))
                            GameScan.isArcScript(nm) -> texts.add(TextItem(full, e.size, f.uri, 1, null, ref, e.name))
                            GameScan.isRawText(nm) -> texts.add(TextItem(full, e.size, f.uri, 0, null, ref, e.name))
                        }
                    }
                    onArchive(ArcStatus(ref, f.path, fmt.displayName, list.size, null), assets, texts)
                }
            } catch (ce: kotlinx.coroutines.CancellationException) {
                throw ce
            } catch (ex: Exception) {
                onArchive(ArcStatus(null, f.path, "", 0, ex.message ?: ex.toString()), emptyList(), emptyList())
            }
        }
        onProgress(cands.size, cands.size, "")
    }

    // ---- open a single entry ----
    private class Open(val src: ChannelSource, val fmt: ArchiveFormat, val map: Map<String, Entry>, val opts: Options)

    private val pool = LinkedHashMap<String, Open>()

    private fun openArc(ctx: Context, ref: ArcRef): Open {
        val k = ref.uri.toString()
        synchronized(pool) { pool.remove(k)?.also { pool[k] = it } }?.let { return it }
        val pfd = ctx.contentResolver.openFileDescriptor(ref.uri, "r") ?: throw IOException("open")
        val src = ChannelSource(pfd)
        try {
            val fmt = FormatRegistry.byId(ref.fmtId)
            val opts = Options(xorKey = ref.key)
            val map = HashMap<String, Entry>()
            for (e in fmt.list(src, Options())) map[e.name] = e
            val o = Open(src, fmt, map, opts)
            synchronized(pool) {
                pool[k] = o
                while (pool.size > 4) {
                    val first = pool.keys.first()
                    pool.remove(first)?.src?.close()
                }
            }
            return o
        } catch (e: Exception) {
            src.close()
            throw e
        }
    }

    fun closeAll() {
        synchronized(pool) {
            pool.values.forEach { try { it.src.close() } catch (e: Exception) { } }
            pool.clear()
        }
    }

    /** Contents of a single entry as bytes (in memory). */
    fun bytes(ctx: Context, ref: ArcRef, entry: String, limit: Long): ByteArray {
        val o = openArc(ctx, ref)
        val e = o.map[entry] ?: throw IOException("entry")
        if (e.size > limit) throw IOException("big")
        val bo = ByteArrayOutputStream(e.size.toInt().coerceIn(16, 64 * 1024 * 1024))
        o.fmt.extract(o.src, e, bo, o.opts)
        return bo.toByteArray()
    }

    /** Write a single entry directly to [out] (no large in-memory buffer for formats that support it). */
    fun extractTo(ctx: Context, ref: ArcRef, entry: String, out: java.io.OutputStream) {
        val o = openArc(ctx, ref)
        val e = o.map[entry] ?: throw IOException("entry")
        o.fmt.extract(o.src, e, out, o.opts)
    }

    /** Extract a single entry to the cache (for audio/video) and return its file. */
    fun toFile(ctx: Context, a: GAsset): File {
        val ref = a.arc ?: throw IOException("arc")
        val ext = a.name.substringAfterLast('.', "bin")
        val f = File(GameCache.dir(), Integer.toHexString(a.id.hashCode()) + "_" + a.size + "." + ext)
        if (f.exists() && f.length() > 0) {
            f.setLastModified(System.currentTimeMillis())
            return f
        }
        val o = openArc(ctx, ref)
        val e = o.map[a.entry] ?: throw IOException("entry")
        val tmp = File(f.path + ".part")
        FileOutputStream(tmp).use { o.fmt.extract(o.src, e, it, o.opts) }
        tmp.renameTo(f)
        GameCache.trim()
        return f
    }
}
