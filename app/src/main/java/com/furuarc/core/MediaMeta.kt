package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/** Tags and cover of a media file. [art] is a small cover (audio) or a video frame. */
class MMeta(val title: String?, val artist: String?, val album: String?, val durationMs: Int, val art: ImageBitmap?)

/** Reads tags / covers with MediaMetadataRetriever and keeps the most recent results (lists load them lazily while scrolling). */
object MediaMeta {
    private val cache = LruCache<String, MMeta>(200)

    fun cached(key: String): MMeta? = cache.get(key)

    /** Blocking; call on an IO dispatcher. Small results are cached, big covers (artMax > 200) are not. */
    fun load(ctx: Context, item: MItem, video: Boolean, artMax: Int = 160): MMeta {
        if (artMax <= 200) cache.get(item.key)?.let { return it }
        val r = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var dur = 0
        var art: ImageBitmap? = null
        try {
            if (item.uri != null) r.setDataSource(ctx, item.uri) else r.setDataSource(item.path)
            title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()?.ifEmpty { null }
            artist = (r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST))?.trim()?.ifEmpty { null }
            album = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)?.trim()?.ifEmpty { null }
            dur = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
            if (video) {
                val f = r.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (f != null) art = scale(f, artMax).asImageBitmap()
            } else {
                val pic = r.embeddedPicture
                if (pic != null) {
                    val b = BitmapFactory.Options()
                    b.inJustDecodeBounds = true
                    BitmapFactory.decodeByteArray(pic, 0, pic.size, b)
                    var sample = 1
                    while (b.outWidth / (sample * 2) >= artMax && b.outHeight / (sample * 2) >= artMax) sample *= 2
                    val o = BitmapFactory.Options()
                    o.inSampleSize = sample
                    BitmapFactory.decodeByteArray(pic, 0, pic.size, o)?.let { art = it.asImageBitmap() }
                }
            }
        } catch (e: Throwable) {
        } finally {
            try { r.release() } catch (e: Throwable) { }
        }
        val m = MMeta(title, artist, album, dur, art)
        if (artMax <= 200) cache.put(item.key, m)
        return m
    }

    private val infoCache = java.util.concurrent.ConcurrentHashMap<String, MMeta>()

    /** Tags and duration only (no cover, no video frame): cheap enough to read for a whole list when sorting. Blocking; call on an IO dispatcher. */
    fun info(ctx: Context, item: MItem): MMeta {
        cache.get(item.key)?.let { return it }
        infoCache[item.key]?.let { return it }
        val r = MediaMetadataRetriever()
        var title: String? = null
        var artist: String? = null
        var dur = 0
        try {
            if (item.uri != null) r.setDataSource(ctx, item.uri) else r.setDataSource(item.path)
            title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.trim()?.ifEmpty { null }
            artist = (r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST))?.trim()?.ifEmpty { null }
            dur = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toIntOrNull() ?: 0
        } catch (e: Throwable) {
        } finally {
            try { r.release() } catch (e: Throwable) { }
        }
        val m = MMeta(title, artist, null, dur, null)
        infoCache[item.key] = m
        return m
    }

    private fun scale(b: Bitmap, max: Int): Bitmap {
        val m = maxOf(b.width, b.height)
        if (m <= max) return b
        val k = max.toFloat() / m
        return Bitmap.createScaledBitmap(b, (b.width * k).toInt().coerceAtLeast(1), (b.height * k).toInt().coerceAtLeast(1), true)
    }
}
