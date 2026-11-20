package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.ExifInterface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.os.Build
import android.view.Surface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.CharBuffer
import java.nio.FloatBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/*
 * Media Asset Converter engine. Everything uses built-in Android APIs (BitmapFactory, MediaCodec, MediaMuxer, EGL),
 * with no extra libraries. As a consequence, output formats are limited by the encoders Android provides:
 *  - Image : PNG, JPG, WEBP
 *  - Audio : M4A (AAC), OGG (Opus, Android 10+), WAV (PCM 16-bit); MP3, FLAC, OGG (Vorbis) via pure-Java encoders (AudioWriters.kt)
 *  - Video : MP4 (H.264 or H.265), WebM (VP9, if the device has the encoder)
 *  - Text  : UTF-8 <-> Shift-JIS/GBK/Big5/EUC-KR/EUC-JP
 */

/** Thrown when the user cancels the process. */
class MediaCancelled : RuntimeException("cancelled")

/** Result of converting one file. */
const val MEDIA_OK = 0
const val MEDIA_SAME = 1
const val MEDIA_SKIP = 2

/** Context of one job: Context, temp folder, cancellation check, and a 0..1 progress reporter. */
class MediaCtl(
    val ctx: Context,
    val tmpDir: File,
    val cancelled: () -> Boolean,
    val progress: (Float) -> Unit,
) {
    fun check() {
        if (cancelled()) throw MediaCancelled()
    }

    /** Progress reporter mapped onto the [from]..[to] range of the parent progress. */
    fun sub(from: Float, to: Float): MediaCtl =
        MediaCtl(ctx, tmpDir, cancelled) { p -> progress(from + (to - from) * p.coerceIn(0f, 1f)) }
}

private fun copyFileTo(f: File, os: OutputStream) {
    os.use { out -> FileInputStream(f).use { ins -> ins.copyTo(out, 256 * 1024) } }
}

private fun findTrack(ex: MediaExtractor, prefix: String): Int {
    for (i in 0 until ex.trackCount) {
        val m = ex.getTrackFormat(i).getString(MediaFormat.KEY_MIME) ?: continue
        if (m.startsWith(prefix)) return i
    }
    return -1
}

private fun fmtInt(f: MediaFormat, key: String, def: Int): Int =
    try { if (f.containsKey(key)) f.getInteger(key) else def } catch (e: Exception) { def }

private fun fmtFloat(f: MediaFormat, key: String, def: Float): Float {
    if (!f.containsKey(key)) return def
    return try { f.getInteger(key).toFloat() } catch (e: Exception) {
        try { f.getFloat(key) } catch (e2: Exception) { def }
    }
}

private fun fmtLong(f: MediaFormat, key: String, def: Long): Long =
    try { if (f.containsKey(key)) f.getLong(key) else def } catch (e: Exception) { def }

// ============================================================ Image ============================================================

/** "Follow Source" mapping: the output format matching the source extension, or null if Android cannot encode it. */
object MediaFormats {
    fun audio(ext: String): Int? = when (ext.lowercase()) {
        "m4a" -> 0
        "opus" -> if (Build.VERSION.SDK_INT >= 29) 1 else null
        "wav" -> 2
        "mp3" -> 3
        "flac" -> 4
        "ogg", "oga" -> 5
        else -> null
    }

    fun image(ext: String): Int? = when (ext.lowercase()) { "png" -> 0; "jpg", "jpeg" -> 1; "webp" -> 2; else -> null }

    fun video(ext: String): Int? = when (ext.lowercase()) { "mp4", "m4v" -> 0; "webm" -> 2; else -> null }
}

/**
 * [format]: 0 PNG, 1 JPG, 2 WEBP, 3 WEBP lossless. [quality] 1..100 (upper bound when compression is on).
 * [resize]: 0 original, 1 percent ([pct]), 2 box size [boxW] x [boxH] px (0 = unbounded); [keepRatio] = fit inside the box keeping the source ratio.
 * Images are never enlarged. [rotate]: 0/90/180/270. [bgWhite]: JPG background for transparent images.
 * [targetBytes] > 0: find the highest quality whose result is still <= the target (lossy JPG/WEBP only).
 */
class ImageOpts(
    val format: Int, val quality: Int, val resize: Int, val pct: Int, val boxW: Int, val boxH: Int,
    val keepRatio: Boolean, val rotate: Int, val bgWhite: Boolean, val targetBytes: Long,
    /** > 1: round the output width/height to a multiple of this (for tilesets/spritesheets). */
    val multiple: Int = 0,
    /** dithering when PNG colors are reduced. */
    val dither: Boolean = false,
)

object ImageConv {
    val EXTS = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif", "heic", "heif")

    fun outExt(format: Int): String = when (format) { 1 -> "jpg"; 2, 3 -> "webp"; else -> "png" }

    /** Image width x height without decoding its pixels; null if it is not a recognized image. */
    fun bounds(ctx: Context, src: Uri): Pair<Int, Int>? = try {
        val b = BitmapFactory.Options()
        b.inJustDecodeBounds = true
        ctx.contentResolver.openInputStream(src)?.use { BitmapFactory.decodeStream(it, null, b) }
        if (b.outWidth > 0 && b.outHeight > 0) Pair(b.outWidth, b.outHeight) else null
    } catch (e: Exception) { null }

    /** Reduce the number of colors per channel (quality 1..99 -> 2..255 levels) so the PNG compresses better. */
    private fun posterize(src: Bitmap, quality: Int): Bitmap {
        val levels = (2 + (quality - 1) * 254 / 99).coerceIn(2, 255)
        val lut = IntArray(256) { v -> Math.round(Math.round(v * (levels - 1) / 255f) * 255f / (levels - 1)) }
        val w = src.width
        val h = src.height
        val px = IntArray(w * h)
        src.getPixels(px, 0, w, 0, 0, w, h)
        for (i in px.indices) {
            val c = px[i]
            px[i] = (c and 0xFF000000.toInt()) or (lut[(c shr 16) and 255] shl 16) or (lut[(c shr 8) and 255] shl 8) or lut[c and 255]
        }
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        out.setPixels(px, 0, w, 0, 0, w, h)
        return out
    }

    fun convert(ctl: MediaCtl, src: Uri, o: ImageOpts, sink: () -> OutputStream?): Int {
        val resolver = ctl.ctx.contentResolver
        val bounds = BitmapFactory.Options()
        bounds.inJustDecodeBounds = true
        (resolver.openInputStream(src) ?: throw IOException("Tidak bisa membuka file")).use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Format gambar tidak didukung")

        val w0 = bounds.outWidth
        val h0 = bounds.outHeight
        var tw = w0
        var th = h0
        if (o.resize == 1) {
            val sc = o.pct.coerceIn(1, 100) / 100f
            tw = Math.round(w0 * sc)
            th = Math.round(h0 * sc)
        } else if (o.resize == 2) {
            if (o.keepRatio) {
                var sc = 1f
                if (o.boxW > 0) sc = minOf(sc, o.boxW.toFloat() / w0)
                if (o.boxH > 0) sc = minOf(sc, o.boxH.toFloat() / h0)
                tw = Math.round(w0 * sc)
                th = Math.round(h0 * sc)
            } else {
                if (o.boxW > 0) tw = minOf(o.boxW, w0)
                if (o.boxH > 0) th = minOf(o.boxH, h0)
            }
        }
        if (o.multiple > 1 && o.resize != 0) {
            fun snap(v: Int, srcSize: Int): Int {
                var x = Math.round(v.toFloat() / o.multiple) * o.multiple
                if (x < o.multiple) x = o.multiple
                if (x > srcSize) x = (srcSize / o.multiple) * o.multiple
                return x
            }
            tw = snap(tw, w0)
            th = snap(th, h0)
        }
        tw = maxOf(1, tw)
        th = maxOf(1, th)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= tw && bounds.outHeight / (sample * 2) >= th) sample *= 2

        var exifDeg = 0
        try {
            resolver.openInputStream(src)?.use { ins ->
                val ori = ExifInterface(ins).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                exifDeg = when (ori) { ExifInterface.ORIENTATION_ROTATE_90 -> 90; ExifInterface.ORIENTATION_ROTATE_180 -> 180; ExifInterface.ORIENTATION_ROTATE_270 -> 270; else -> 0 }
            }
        } catch (e: Exception) { }

        ctl.check()
        var bmp: Bitmap? = null
        try {
            val dec = BitmapFactory.Options()
            dec.inSampleSize = sample
            dec.inPreferredConfig = Bitmap.Config.ARGB_8888
            val decoded: Bitmap = (resolver.openInputStream(src) ?: throw IOException("Tidak bisa membuka file")).use { BitmapFactory.decodeStream(it, null, dec) }
                ?: throw IOException("Gambar tidak bisa dibaca")
            bmp = decoded

            val m = Matrix()
            val cur: Bitmap = decoded
            if (cur.width != tw || cur.height != th) m.postScale(tw.toFloat() / cur.width, th.toFloat() / cur.height)
            val rot = (exifDeg + o.rotate) % 360
            if (rot != 0) m.postRotate(rot.toFloat())
            if (!m.isIdentity) {
                val nb = Bitmap.createBitmap(cur, 0, 0, cur.width, cur.height, m, true)
                if (nb !== cur) cur.recycle()
                bmp = nb
            }
            var work: Bitmap = bmp ?: throw IOException("Gambar tidak bisa dibaca")
            if (o.format == 1 && work.hasAlpha()) {
                val flat = Bitmap.createBitmap(work.width, work.height, Bitmap.Config.ARGB_8888)
                val cv = Canvas(flat)
                cv.drawColor(if (o.bgWhite) Color.WHITE else Color.BLACK)
                cv.drawBitmap(work, 0f, 0f, null)
                work.recycle()
                work = flat
                bmp = flat
            }
            val fmt: Bitmap.CompressFormat = when (o.format) {
                1 -> Bitmap.CompressFormat.JPEG
                2 -> if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                3 -> if (Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSLESS else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.PNG
            }
            val q = if (o.format == 3) 100 else o.quality.coerceIn(1, 100)
            val lossy = o.format in 0..2 // PNG "compressed" by reducing the color count (posterization), JPG/WEBP via quality
            var pal: PngPal? = null
            var palTried = false
            fun encode(quality: Int): ByteArray {
                if (o.format == 0 && quality < 100) {
                    if (!palTried) {
                        palTried = true
                        pal = PngPal.create(work)
                    }
                    val pp = pal
                    // paletted PNG: color count = quality% of 256 (huge photos fall back to posterization)
                    if (pp != null) return pp.encode((256 * quality / 100).coerceIn(2, 256), o.dither)
                }
                val pb: Bitmap = if (o.format == 0 && quality < 100) posterize(work, quality) else work
                try {
                    val bos = ByteArrayOutputStream()
                    if (!pb.compress(fmt, if (o.format == 0) 100 else quality, bos)) throw IOException("Gagal menyandikan gambar")
                    return bos.toByteArray()
                } finally {
                    if (pb !== work) pb.recycle()
                }
            }
            val data: ByteArray
            if (o.targetBytes > 0L && lossy) {
                // find the highest quality (<= the limit) whose size is still under the target; quality 1 if it is still too large
                var lo = 1
                var hi = q
                var best: ByteArray? = null
                // try the highest quality first: if it already fits, no search is needed
                val top = encode(hi)
                if (top.size <= o.targetBytes) {
                    best = top
                    lo = hi + 1
                } else {
                    hi -= 1
                }
                var tries = 0
                while (lo <= hi && tries < 6) {
                    ctl.check()
                    tries++
                    val mid = (lo + hi) / 2
                    val b = encode(mid)
                    if (b.size <= o.targetBytes) {
                        best = b
                        lo = mid + 1
                        if (b.size >= o.targetBytes * 9 / 10) break // close enough (>= 90% of target)
                    } else {
                        hi = mid - 1
                    }
                }
                data = best ?: encode(1)
            } else {
                data = encode(q)
            }
            ctl.check()
            val os = sink() ?: return MEDIA_SKIP
            os.use { it.write(data) }
            ctl.progress(1f)
            return MEDIA_OK
        } catch (e: OutOfMemoryError) {
            throw IOException("Memori tidak cukup untuk gambar ini (coba kecilkan ukurannya)")
        } finally {
            try { bmp?.recycle() } catch (e: Exception) { }
        }
    }
}

// ============================================================ Text ============================================================

/** [toUtf8]: true = [charset] -> UTF-8, false = UTF-8 -> [charset]. [newline]: 0 original, 1 LF, 2 CRLF. [lenient]: replace invalid characters. */
class TextOpts(val toUtf8: Boolean, val charset: String, val bom: Boolean, val newline: Int, val lenient: Boolean)

object TextConv {
    private fun strictDecode(b: ByteArray, cs: Charset): String? = try {
        cs.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b)).toString()
    } catch (e: CharacterCodingException) { null }

    private fun isAscii(b: ByteArray): Boolean {
        for (x in b) if (x < 0) return false
        return true
    }

    private fun fixNewlines(t: String, mode: Int): String {
        if (mode == 0) return t
        val lf = t.replace("\r\n", "\n").replace('\r', '\n')
        return if (mode == 2) lf.replace("\n", "\r\n") else lf
    }

    /** Result of converting the contents of file [b]. */
    fun transform(b: ByteArray, o: TextOpts): ByteArray {
        val utf8 = Charsets.UTF_8
        val legacy = Charset.forName(o.charset)
        val hasBom = b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte()
        val body = if (hasBom) b.copyOfRange(3, b.size) else b
        val asUtf8 = strictDecode(body, utf8)
        val text: String
        if (o.toUtf8) {
            text = if (asUtf8 != null) {
                asUtf8 // already UTF-8 (or pure ASCII)
            } else if (o.lenient) {
                String(body, legacy)
            } else {
                strictDecode(body, legacy) ?: throw IOException("Bukan teks ${o.charset} yang valid (pilih penyandian lain atau aktifkan Ganti karakter rusak)")
            }
        } else {
            if (asUtf8 == null) {
                // not UTF-8: if it is already valid in the target encoding, leave it as is
                if (strictDecode(body, legacy) != null) return b
                if (!o.lenient) throw IOException("Bukan teks UTF-8 yang valid")
                text = String(body, utf8)
            } else {
                text = asUtf8
            }
        }
        val fixed = fixNewlines(text, o.newline)
        if (o.toUtf8) {
            val raw = fixed.toByteArray(utf8)
            if (!o.bom) return raw
            val out = ByteArray(raw.size + 3)
            out[0] = 0xEF.toByte(); out[1] = 0xBB.toByte(); out[2] = 0xBF.toByte()
            System.arraycopy(raw, 0, out, 3, raw.size)
            return out
        }
        val enc = legacy.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
        if (o.lenient) {
            enc.onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(byteArrayOf('?'.code.toByte()))
        } else {
            enc.onUnmappableCharacter(CodingErrorAction.REPORT)
        }
        return try {
            val bb = enc.encode(CharBuffer.wrap(fixed))
            val out = ByteArray(bb.remaining())
            bb.get(out)
            out
        } catch (e: CharacterCodingException) {
            throw IOException("Ada karakter yang tidak ada di ${o.charset} (aktifkan Ganti karakter rusak untuk menggantinya dengan ?)")
        }
    }

    fun convert(ctl: MediaCtl, src: Uri, o: TextOpts, sink: () -> OutputStream?): Int {
        val bytes = (ctl.ctx.contentResolver.openInputStream(src) ?: throw IOException("Tidak bisa membuka file")).use { it.readBytes() }
        if (bytes.size > 128 * 1024 * 1024) throw IOException("File terlalu besar untuk file teks")
        val out = transform(bytes, o)
        val os = sink() ?: return MEDIA_SKIP
        os.use { it.write(out) }
        ctl.progress(1f)
        return if (out.contentEquals(bytes)) MEDIA_SAME else MEDIA_OK
    }
}

// ============================================================ Audio ============================================================

/**
 * [format]: 0 M4A (AAC-LC), 1 OGG (Opus), 2 WAV (PCM 16-bit), 3 MP3, 4 FLAC, 5 OGG (Vorbis). [kbps] bitrate (unused for WAV and FLAC).
 * [rate]: output sample rate, 0 = follow source. [channels]: 0 follow source (max 2), 1 mono, 2 stereo. [gainPct]: volume, 100 = unchanged.
 */
/** [qualityPct] < 100: compress quality (bitrate multiplied by the percent; WAV: sample rate lowered). [targetBytes] > 0 overrides it. */
class AudioOpts(
    val format: Int, val kbps: Int, val rate: Int, val channels: Int, val gainPct: Int,
    val targetBytes: Long = 0L, val qualityPct: Int = 100,
    /** volume normalization: 0 off, 1..3 = RMS -20/-16/-12 dBFS (peak limited to -1 dBFS). */
    val normalize: Int = 0,
    /** trim silence at the start and end (threshold about -50 dBFS). */
    val trim: Boolean = false,
    /** copy OGG loop tags (LOOPSTART/LOOPLENGTH) from the source to the OGG output (Opus or Vorbis). */
    val keepLoop: Boolean = true,
)

/** Audio conversion result: source/output sample rate and the range of source frames used ([endFrame] = -1 if not trimmed). */
class AudioResult(val inRate: Int, val outRate: Int, val startFrame: Long, val endFrame: Long)

private interface PcmSink {
    fun write(pcm: ShortArray, n: Int)
    fun finish()
    fun release()
}

/** Write 16-bit PCM to a WAV file (header filled in at the end). */
private class WavSink(file: File, private val rate: Int, private val ch: Int) : PcmSink {
    private val raf = RandomAccessFile(file, "rw")
    private var bytes = 0L

    init {
        raf.setLength(0)
        raf.write(ByteArray(44))
    }

    override fun write(pcm: ShortArray, n: Int) {
        val b = ByteArray(n * 2)
        for (i in 0 until n) {
            b[2 * i] = pcm[i].toInt().toByte()
            b[2 * i + 1] = (pcm[i].toInt() shr 8).toByte()
        }
        raf.write(b)
        bytes += b.size
    }

    override fun finish() {
        val h = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        h.put("RIFF".toByteArray(Charsets.US_ASCII)).putInt((36L + bytes).toInt())
        h.put("WAVE".toByteArray(Charsets.US_ASCII))
        h.put("fmt ".toByteArray(Charsets.US_ASCII)).putInt(16).putShort(1.toShort()).putShort(ch.toShort())
        h.putInt(rate).putInt(rate * ch * 2).putShort((ch * 2).toShort()).putShort(16.toShort())
        h.put("data".toByteArray(Charsets.US_ASCII)).putInt(bytes.toInt())
        raf.seek(0)
        raf.write(h.array())
    }

    override fun release() {
        try { raf.close() } catch (e: Exception) { }
    }
}

/** CBR MP3 via a pure-Java encoder. */
private class Mp3Sink(file: File, rate: Int, ch: Int, kbps: Int) : PcmSink {
    private val os = java.io.BufferedOutputStream(java.io.FileOutputStream(file), 64 * 1024)
    private val w = Mp3Writer(os, rate, ch, kbps)
    override fun write(pcm: ShortArray, n: Int) = w.write(pcm, n)
    override fun finish() { w.finish(); os.flush() }
    override fun release() { try { os.close() } catch (e: Exception) { } }
}

/** OGG Vorbis via a pure-Java encoder; [tags] are written to the header (e.g. LOOPSTART/LOOPLENGTH). */
private class VorbisSink(file: File, rate: Int, ch: Int, bitrate: Int, tags: List<Pair<String, String>>) : PcmSink {
    private val os = java.io.BufferedOutputStream(java.io.FileOutputStream(file), 64 * 1024)
    private val w = VorbisWriter(os, rate, ch, bitrate, tags)
    override fun write(pcm: ShortArray, n: Int) = w.write(pcm, n)
    override fun finish() { w.finish(); os.flush() }
    override fun release() { try { os.close() } catch (e: Exception) { } }
}

/** Lossless FLAC (16-bit). */
private class FlacSink(file: File, rate: Int, ch: Int) : PcmSink {
    private val raf = RandomAccessFile(file, "rw").also { it.setLength(0) }
    private val w = FlacWriter(raf, rate, ch)
    override fun write(pcm: ShortArray, n: Int) = w.write(pcm, n)
    override fun finish() = w.finish()
    override fun release() { try { raf.close() } catch (e: Exception) { } }
}

/** Encode 16-bit PCM to AAC (MP4/M4A) or Opus (OGG) via MediaCodec + MediaMuxer. */
private class EncSink(file: File, format: Int, private val rate: Int, private val ch: Int, bitrate: Int) : PcmSink {
    private val enc: MediaCodec
    private val muxer: MediaMuxer
    private var track = -1
    private var started = false
    private var framesFed = 0L
    private val info = MediaCodec.BufferInfo()

    init {
        if (format == 1 && Build.VERSION.SDK_INT < 29) throw IOException("OGG (Opus) memerlukan Android 10 ke atas")
        val mime = if (format == 1) "audio/opus" else MediaFormat.MIMETYPE_AUDIO_AAC
        val f = MediaFormat.createAudioFormat(mime, rate, ch)
        f.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
        if (format == 0) {
            f.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
            f.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384 * ch)
        }
        enc = try { MediaCodec.createEncoderByType(mime) } catch (e: Exception) { throw IOException("Perangkat ini tidak punya encoder $mime") }
        try {
            enc.configure(f, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            enc.start()
        } catch (e: Exception) {
            try { enc.release() } catch (e2: Exception) { }
            throw IOException("Encoder menolak pengaturan ini: ${e.message}")
        }
        muxer = try {
            MediaMuxer(file.absolutePath, if (format == 1) MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG else MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        } catch (e: Exception) {
            try { enc.stop(); enc.release() } catch (e2: Exception) { }
            throw IOException("Gagal membuat file Output: ${e.message}")
        }
    }

    private fun drain(eos: Boolean) {
        var idle = 0
        while (true) {
            val oi = enc.dequeueOutputBuffer(info, if (eos) 10_000L else 0L)
            if (oi == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!eos) return
                if (++idle > 3000) throw IOException("Encoder tidak merespons")
            } else if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                track = muxer.addTrack(enc.outputFormat)
                muxer.start()
                started = true
            } else if (oi >= 0) {
                val ob = enc.getOutputBuffer(oi)
                if ((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) info.size = 0
                if (info.size > 0 && started && ob != null) {
                    ob.position(info.offset)
                    ob.limit(info.offset + info.size)
                    muxer.writeSampleData(track, ob, info)
                }
                enc.releaseOutputBuffer(oi, false)
                if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return
            }
        }
    }

    override fun write(pcm: ShortArray, n: Int) {
        var off = 0
        while (off < n) {
            val ii = enc.dequeueInputBuffer(10_000L)
            if (ii < 0) {
                drain(false)
                continue
            }
            val ib = enc.getInputBuffer(ii) ?: throw IOException("Buffer encoder tidak tersedia")
            ib.clear()
            val cap = ib.capacity() / 2
            val cnt = minOf(cap - cap % ch, n - off)
            if (cnt <= 0) throw IOException("Buffer encoder terlalu kecil")
            ib.order(ByteOrder.LITTLE_ENDIAN)
            ib.asShortBuffer().put(pcm, off, cnt)
            enc.queueInputBuffer(ii, 0, cnt * 2, framesFed * 1_000_000L / rate, 0)
            framesFed += cnt / ch
            off += cnt
            drain(false)
        }
    }

    override fun finish() {
        var queued = false
        while (!queued) {
            val ii = enc.dequeueInputBuffer(10_000L)
            if (ii >= 0) {
                enc.queueInputBuffer(ii, 0, 0, framesFed * 1_000_000L / rate, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                queued = true
            } else {
                drain(false)
            }
        }
        drain(true)
        if (!started) throw IOException("Audio kosong atau tidak bisa disandikan")
        muxer.stop()
    }

    override fun release() {
        try { enc.stop() } catch (e: Exception) { }
        try { enc.release() } catch (e: Exception) { }
        try { muxer.release() } catch (e: Exception) { }
    }
}

/**
 * PCM processor: channel mixing, volume, then linear resampling (streaming). Input is interleaved 16-bit PCM.
 * With more than 2 channels, only the first two (left/right) are taken when converting to stereo.
 */
private class PcmProc(
    private val inRate: Int,
    private val inCh: Int,
    outRate: Int,
    private val outCh: Int,
    private val gain: Float,
    private val sink: PcmSink,
) {
    private val resample = inRate != outRate
    private val step = inRate.toDouble() / outRate
    private val buf = ShortArray(8192 * outCh)
    private var bl = 0
    private var first = true
    private var idx = 0L
    private var nextPos = 0.0
    private var lastL = 0f
    private var lastR = 0f

    private fun clip(v: Float): Short {
        val x = Math.round(v)
        return (if (x > 32767) 32767 else if (x < -32768) -32768 else x).toShort()
    }

    private fun put(l: Float, r: Float) {
        buf[bl++] = clip(l)
        if (outCh == 2) buf[bl++] = clip(r)
        if (bl >= buf.size) {
            sink.write(buf, bl)
            bl = 0
        }
    }

    private fun push(l: Float, r: Float) {
        if (first) {
            first = false
            idx = 0
            while (nextPos <= 0.0) {
                put(l, r)
                nextPos += step
            }
            lastL = l; lastR = r
            return
        }
        idx++
        while (nextPos <= idx) {
            val a = (nextPos - (idx - 1)).toFloat()
            put(lastL * (1f - a) + l * a, lastR * (1f - a) + r * a)
            nextPos += step
        }
        lastL = l; lastR = r
    }

    fun feed(s: ShortArray, nShorts: Int) {
        val frames = nShorts / inCh
        for (f in 0 until frames) {
            val base = f * inCh
            var l: Float
            var r: Float
            if (outCh == 1) {
                var sum = 0
                for (c in 0 until inCh) sum += s[base + c]
                l = sum.toFloat() / inCh
                r = l
            } else if (inCh == 1) {
                l = s[base].toFloat()
                r = l
            } else {
                l = s[base].toFloat()
                r = s[base + 1].toFloat()
            }
            l *= gain
            r *= gain
            if (resample) push(l, r) else put(l, r)
        }
    }

    fun flush() {
        if (bl > 0) {
            sink.write(buf, bl)
            bl = 0
        }
    }
}

object AudioConv {
    val EXTS = setOf("mp3", "ogg", "oga", "m4a", "aac", "wav", "flac", "opus", "wma", "amr")
    private val AAC_RATES = intArrayOf(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000)
    private val OPUS_RATES = intArrayOf(8000, 12000, 16000, 24000, 48000)

    fun outExt(format: Int): String = when (format) { 1, 5 -> "ogg"; 2 -> "wav"; 3 -> "mp3"; 4 -> "flac"; else -> "m4a" }

    /** Bitrate limits (bps) per format: AAC 16/32-320 kbps, MP3 16-320, Vorbis 48-256, Opus 12-256. */
    private fun clampBitrate(format: Int, bps: Int, outCh: Int): Int = when (format) {
        0 -> bps.coerceIn(if (outCh == 1) 16000 else 32000, 320000)
        3 -> bps.coerceIn(16000, 320000)
        5 -> bps.coerceIn(48000, 256000)
        else -> bps.coerceIn(12000, 256000)
    }

    private fun nearest(v: Int, list: IntArray): Int {
        var best = list[0]
        for (x in list) if (Math.abs(x - v) < Math.abs(best - v)) best = x
        return best
    }

    fun convert(ctl: MediaCtl, src: Uri, o: AudioOpts, sink: () -> OutputStream?): Int {
        val tmp = File.createTempFile("aud", ".tmp", ctl.tmpDir)
        try {
            val r = convertToFile(ctl, src, o, tmp)
            if (o.format == 1 && o.keepLoop && r != null && r.inRate > 0) {
                val lp = OggTags.readLoop(ctl.ctx, src)
                if (lp != null) {
                    val k = r.outRate.toDouble() / r.inRate
                    val s0 = maxOf(0L, lp.start - r.startFrame)
                    var len = lp.length
                    if (r.endFrame >= 0L) len = minOf(lp.start + lp.length, r.endFrame + 1) - maxOf(lp.start, r.startFrame)
                    if (len > 0L) OggTags.writeLoop(tmp, Math.round(s0 * k), Math.round(len * k))
                }
            }
            ctl.check()
            val os = sink() ?: return MEDIA_SKIP
            copyFileTo(tmp, os)
            ctl.progress(1f)
            return MEDIA_OK
        } finally {
            tmp.delete()
        }
    }

    /** The first audio track of [src] (may be a video file) converted into [dst]. Also used by the Video Converter. */
    fun convertToFile(ctl0: MediaCtl, src: Uri, o: AudioOpts, dst: File): AudioResult? {
        if (o.format == 1 && Build.VERSION.SDK_INT < 29) throw IOException("OGG (Opus) memerlukan Android 10 ke atas")
        // normalization/silence trimming needs a scanning pass first (the first quarter of progress)
        val needScan = o.normalize > 0 || o.trim
        val scan: AudioScan? = if (needScan) AudioScanner.scan(ctl0.sub(0f, 0.25f), src, 103) else null
        val ctl: MediaCtl = if (needScan) ctl0.sub(0.25f, 1f) else ctl0
        val normGain = AudioScanner.normGain(scan, o.normalize)
        var startF = 0L
        var endF = Long.MAX_VALUE - 1
        var trimOn = false
        if (o.trim && scan != null && scan.first >= 0L && scan.last >= scan.first) {
            val pad = (scan.rate / 100).toLong()
            startF = maxOf(0L, scan.first - pad)
            endF = minOf(scan.frames - 1, scan.last + pad)
            trimOn = startF > 0L || endF < scan.frames - 1
        }
        var rInRate = 0
        var rOutRate = 0
        var frameIdx = 0L
        val ex = MediaExtractor()
        var dec: MediaCodec? = null
        var sinkObj: PcmSink? = null
        var proc: PcmProc? = null
        var success = false
        try {
            try {
                ex.setDataSource(ctl.ctx, src, null)
            } catch (e: Exception) {
                throw IOException("Format file tidak didukung (${e.message})")
            }
            val track = findTrack(ex, "audio/")
            if (track < 0) throw IOException("Tidak ada trek audio di file ini")
            ex.selectTrack(track)
            val fmt = ex.getTrackFormat(track)
            val mime = fmt.getString(MediaFormat.KEY_MIME) ?: throw IOException("Trek audio tidak dikenal")
            val durUs = fmtLong(fmt, MediaFormat.KEY_DURATION, 0L)
            var inRate = fmtInt(fmt, MediaFormat.KEY_SAMPLE_RATE, 44100)
            var inCh = fmtInt(fmt, MediaFormat.KEY_CHANNEL_COUNT, 2)
            var pcmEnc = 2 // ENCODING_PCM_16BIT

            val d = try { MediaCodec.createDecoderByType(mime) } catch (e: Exception) { throw IOException("Perangkat tidak bisa mendekode $mime") }
            dec = d
            try {
                d.configure(fmt, null, null, 0)
                d.start()
            } catch (e: Exception) {
                throw IOException("Gagal memulai decoder $mime: ${e.message}")
            }

            fun ensurePipeline() {
                if (proc != null) return
                if (inCh < 1) inCh = 1
                var outCh = when (o.channels) { 1 -> 1; 2 -> 2; else -> minOf(inCh, 2) }
                var outRate = if (o.rate > 0) o.rate else inRate
                rInRate = inRate
                val lossless = o.format == 2 || o.format == 4
                if (o.format == 0) outRate = nearest(outRate, AAC_RATES)
                else if (o.format == 1) outRate = nearest(outRate, OPUS_RATES)
                else if (o.format == 3) outRate = nearest(outRate, Mp3Info.RATES)
                else if (o.format == 5) outRate = VorbisInfo.snapRate(outRate)
                var bitrate = if (o.kbps > 0) o.kbps * 1000 else {
                    // Auto: follow the source bitrate (64-192 kbps), 128 kbps if unknown or the source is lossless; mono is smaller; Opus is more efficient
                    val srcBr = fmtInt(fmt, MediaFormat.KEY_BIT_RATE, 0)
                    var b = (if (srcBr in 32000..400000) srcBr else 128000).coerceIn(64000, 192000)
                    if (outCh == 1 && inCh >= 2) b = b * 6 / 10
                    if (o.format == 1) b = b * 3 / 4
                    b.coerceIn(32000, 256000)
                }
                if (o.targetBytes <= 0L && o.qualityPct in 1..99) {
                    if (lossless) {
                        val want = outRate * o.qualityPct / 100
                        val rates = intArrayOf(48000, 44100, 32000, 22050, 16000, 11025, 8000)
                        outRate = rates.firstOrNull { it <= want && it <= outRate } ?: 8000
                    } else {
                        bitrate = clampBitrate(o.format, (bitrate * o.qualityPct / 100), outCh)
                    }
                }
                if (o.targetBytes > 0L && durUs > 0L) {
                    val sec = durUs / 1_000_000.0
                    if (lossless) {
                        // WAV/FLAC have no bitrate: lower the sample rate (then channels) until it fits the target
                        var found = false
                        val rates = intArrayOf(48000, 44100, 32000, 22050, 16000, 11025, 8000)
                        search@ for (c in outCh downTo 1) {
                            for (r in rates) {
                                if (r <= outRate && r.toDouble() * c * 2 * sec <= o.targetBytes) {
                                    outRate = r; outCh = c; found = true
                                    break@search
                                }
                            }
                        }
                        if (!found) { outRate = 8000; outCh = 1 }
                    } else {
                        val bps = (o.targetBytes * 8 / sec).toInt()
                        bitrate = clampBitrate(o.format, bps, outCh)
                    }
                }
                if (o.format == 3 || o.format == 5) bitrate = clampBitrate(o.format, bitrate, outCh)
                val sk: PcmSink = when (o.format) {
                    2 -> WavSink(dst, outRate, outCh)
                    3 -> Mp3Sink(dst, outRate, outCh, Mp3Info.validKbps(outRate, bitrate / 1000))
                    4 -> FlacSink(dst, outRate, outCh)
                    5 -> {
                        val tags = ArrayList<Pair<String, String>>()
                        if (o.keepLoop && inRate > 0) {
                            val lp = OggTags.readLoop(ctl.ctx, src)
                            if (lp != null) {
                                val k = outRate.toDouble() / inRate
                                val s0 = maxOf(0L, lp.start - startF)
                                var len = lp.length
                                if (trimOn) len = minOf(lp.start + lp.length, endF + 1) - maxOf(lp.start, startF)
                                if (len > 0L) {
                                    tags.add("LOOPSTART" to Math.round(s0 * k).toString())
                                    tags.add("LOOPLENGTH" to Math.round(len * k).toString())
                                }
                            }
                        }
                        VorbisSink(dst, outRate, outCh, bitrate, tags)
                    }
                    else -> EncSink(dst, o.format, outRate, outCh, bitrate)
                }
                sinkObj = sk
                rOutRate = outRate
                proc = PcmProc(inRate, inCh, outRate, outCh, o.gainPct / 100f * normGain, sk)
            }

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var shorts = ShortArray(0)
            while (!outputDone) {
                ctl.check()
                if (!inputDone) {
                    val ii = d.dequeueInputBuffer(10_000L)
                    if (ii >= 0) {
                        val ib = d.getInputBuffer(ii) ?: throw IOException("Buffer decoder tidak tersedia")
                        val n = ex.readSampleData(ib, 0)
                        if (n < 0) {
                            d.queueInputBuffer(ii, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            d.queueInputBuffer(ii, 0, n, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }
                val oi = d.dequeueOutputBuffer(info, 10_000L)
                if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val of = d.outputFormat
                    inRate = fmtInt(of, MediaFormat.KEY_SAMPLE_RATE, inRate)
                    inCh = fmtInt(of, MediaFormat.KEY_CHANNEL_COUNT, inCh)
                    pcmEnc = fmtInt(of, MediaFormat.KEY_PCM_ENCODING, 2)
                } else if (oi >= 0) {
                    if (info.size > 0) {
                        ensurePipeline()
                        val ob = d.getOutputBuffer(oi)
                        if (ob != null) {
                            ob.position(info.offset)
                            ob.limit(info.offset + info.size)
                            ob.order(ByteOrder.LITTLE_ENDIAN)
                            val count: Int
                            when (pcmEnc) {
                                4 -> { // float
                                    count = info.size / 4
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    val fb = ob.asFloatBuffer()
                                    for (i in 0 until count) {
                                        val v = fb.get(i) * 32767f
                                        shorts[i] = (if (v > 32767f) 32767f else if (v < -32768f) -32768f else v).toInt().toShort()
                                    }
                                }
                                3 -> { // 8-bit unsigned
                                    count = info.size
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    for (i in 0 until count) shorts[i] = (((ob.get(ob.position() + i).toInt() and 0xFF) - 128) shl 8).toShort()
                                }
                                else -> {
                                    count = info.size / 2
                                    if (shorts.size < count) shorts = ShortArray(count)
                                    ob.asShortBuffer().get(shorts, 0, count)
                                }
                            }
                            val nAll = count - count % inCh
                            if (trimOn) {
                                val frames = nAll / inCh
                                val f0 = frameIdx
                                frameIdx += frames
                                val from = maxOf(0L, startF - f0).toInt()
                                val to = minOf(frames.toLong(), endF + 1 - f0).toInt()
                                if (to > from) proc!!.feed(shorts.copyOfRange(from * inCh, to * inCh), (to - from) * inCh)
                                if (frameIdx > endF) outputDone = true
                            } else {
                                proc!!.feed(shorts, nAll)
                            }
                        }
                        if (durUs > 0L) ctl.progress((info.presentationTimeUs.toFloat() / durUs).coerceIn(0f, 1f))
                    }
                    d.releaseOutputBuffer(oi, false)
                    if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) outputDone = true
                }
            }
            if (proc == null) throw IOException("Tidak ada data audio yang bisa didekode")
            proc!!.flush()
            sinkObj!!.finish()
            success = true
        } finally {
            try { dec?.stop() } catch (e: Exception) { }
            try { dec?.release() } catch (e: Exception) { }
            try { sinkObj?.release() } catch (e: Exception) { }
            try { ex.release() } catch (e: Exception) { }
            if (!success) dst.delete()
        }
        return AudioResult(rInRate, rOutRate, if (trimOn) startF else 0L, if (trimOn) endF else -1L)
    }
}

// ============================================================ Video ============================================================

/**
 * [format]: 0 MP4 (H.264), 1 MP4 (H.265), 2 WebM (VP9). [shortSide]: shortest side limit in px (0 = original).
 * [kbps]: video bitrate (0 = auto). [fps]: frame rate cap (0 = original, only lowers). [keepAudio]: include audio.
 */
class VideoOpts(val format: Int, val shortSide: Int, val kbps: Int, val fps: Int, val keepAudio: Boolean, val targetBytes: Long = 0L, val qualityPct: Int = 100)

/** Simple EGL + GLES: draws frames from the decoder (SurfaceTexture) onto the encoder's input surface, with automatic scaling. */
private class GlPipe(private val outSurface: Surface, private val w: Int, private val h: Int) {
    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var context: EGLContext = EGL14.EGL_NO_CONTEXT
    private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE
    private var program = 0
    private var texId = 0
    private var aPos = 0
    private var aTex = 0
    private var uMat = 0
    private val st: SurfaceTexture
    val decoderSurface: Surface
    private val frames = Semaphore(0)
    private val mat = FloatArray(16)
    private val posBuf: FloatBuffer = floatBuf(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
    private val texBuf: FloatBuffer = floatBuf(floatArrayOf(0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f))

    private fun floatBuf(a: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(a.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        fb.put(a)
        fb.position(0)
        return fb
    }

    private fun shader(type: Int, src: String): Int {
        val s = GLES20.glCreateShader(type)
        GLES20.glShaderSource(s, src)
        GLES20.glCompileShader(s)
        val ok = IntArray(1)
        GLES20.glGetShaderiv(s, GLES20.GL_COMPILE_STATUS, ok, 0)
        if (ok[0] == 0) throw IOException("Shader GL gagal dikompilasi")
        return s
    }

    init {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        val ver = IntArray(2)
        if (display == EGL14.EGL_NO_DISPLAY || !EGL14.eglInitialize(display, ver, 0, ver, 1)) throw IOException("EGL tidak bisa diinisialisasi")
        val cfgAttr = intArrayOf(
            EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8, EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
            0x3142, 1, // EGL_RECORDABLE_ANDROID
            EGL14.EGL_NONE,
        )
        val cfgs = arrayOfNulls<EGLConfig>(1)
        val num = IntArray(1)
        if (!EGL14.eglChooseConfig(display, cfgAttr, 0, cfgs, 0, 1, num, 0) || num[0] <= 0 || cfgs[0] == null) throw IOException("Konfigurasi EGL tidak ditemukan")
        context = EGL14.eglCreateContext(display, cfgs[0], EGL14.EGL_NO_CONTEXT, intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE), 0)
        eglSurface = EGL14.eglCreateWindowSurface(display, cfgs[0], outSurface, intArrayOf(EGL14.EGL_NONE), 0)
        if (!EGL14.eglMakeCurrent(display, eglSurface, eglSurface, context)) throw IOException("EGL gagal dipakai")

        val vs = shader(
            GLES20.GL_VERTEX_SHADER,
            "uniform mat4 uMat;\nattribute vec4 aPos;\nattribute vec4 aTex;\nvarying vec2 vTex;\n" +
                "void main() { gl_Position = aPos; vTex = (uMat * aTex).xy; }",
        )
        val fs = shader(
            GLES20.GL_FRAGMENT_SHADER,
            "#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTex;\n" +
                "uniform samplerExternalOES sTex;\nvoid main() { gl_FragColor = texture2D(sTex, vTex); }",
        )
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vs)
        GLES20.glAttachShader(program, fs)
        GLES20.glLinkProgram(program)
        val linked = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0)
        if (linked[0] == 0) throw IOException("Program GL gagal di-link")
        aPos = GLES20.glGetAttribLocation(program, "aPos")
        aTex = GLES20.glGetAttribLocation(program, "aTex")
        uMat = GLES20.glGetUniformLocation(program, "uMat")

        val t = IntArray(1)
        GLES20.glGenTextures(1, t, 0)
        texId = t[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        st = SurfaceTexture(texId)
        st.setOnFrameAvailableListener { frames.release() }
        decoderSurface = Surface(st)
    }

    /** Call before releaseOutputBuffer(render = true). */
    fun prepareFrame() {
        frames.drainPermits()
    }

    fun awaitFrame() {
        if (!frames.tryAcquire(5, TimeUnit.SECONDS)) throw IOException("Frame video tidak sampai ke GL (timeout)")
    }

    fun draw(ptsNs: Long) {
        st.updateTexImage()
        st.getTransformMatrix(mat)
        GLES20.glViewport(0, 0, w, h)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texId)
        GLES20.glUniformMatrix4fv(uMat, 1, false, mat, 0)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 8, posBuf)
        GLES20.glEnableVertexAttribArray(aTex)
        GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 8, texBuf)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aTex)
        EGLExt.eglPresentationTimeANDROID(display, eglSurface, ptsNs)
        EGL14.eglSwapBuffers(display, eglSurface)
    }

    fun release() {
        try {
            if (display != EGL14.EGL_NO_DISPLAY) {
                EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                if (eglSurface != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, eglSurface)
                if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
                EGL14.eglReleaseThread()
                EGL14.eglTerminate(display)
            }
        } catch (e: Exception) { }
        try { decoderSurface.release() } catch (e: Exception) { }
        try { st.release() } catch (e: Exception) { }
        try { outSurface.release() } catch (e: Exception) { }
    }
}

object VideoConv {
    val EXTS = setOf("mp4", "mkv", "webm", "3gp", "mov", "m4v", "ts", "avi", "flv", "ogv")

    fun outExt(format: Int): String = if (format == 2) "webm" else "mp4"

    private fun containerOf(format: Int): Int =
        if (format == 2) MediaMuxer.OutputFormat.MUXER_OUTPUT_WEBM else MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4

    fun convert(ctl: MediaCtl, src: Uri, o: VideoOpts, sink: () -> OutputStream?): Int {
        val vTmp = File.createTempFile("vid", ".tmp", ctl.tmpDir)
        val aTmp = File.createTempFile("aud", ".tmp", ctl.tmpDir)
        val fTmp = File.createTempFile("fin", ".tmp", ctl.tmpDir)
        try {
            val rot = encodeVideo(ctl.sub(0f, 0.8f), src, o, vTmp)
            var result = vTmp
            if (o.keepAudio) {
                val audioSrc = prepareAudio(ctl.sub(0.8f, 0.93f), src, o, aTmp)
                if (audioSrc != null) {
                    muxVideoAudio(ctl.sub(0.93f, 1f), vTmp, audioSrc.first, audioSrc.second, fTmp, containerOf(o.format), rot)
                    result = fTmp
                }
            }
            ctl.check()
            val os = sink() ?: return MEDIA_SKIP
            copyFileTo(result, os)
            ctl.progress(1f)
            return MEDIA_OK
        } finally {
            vTmp.delete(); aTmp.delete(); fTmp.delete()
        }
    }

    private fun audioMime(ctx: Context, src: Uri): String? {
        val ex = MediaExtractor()
        try {
            ex.setDataSource(ctx, src, null)
            val t = findTrack(ex, "audio/")
            if (t < 0) return null
            return ex.getTrackFormat(t).getString(MediaFormat.KEY_MIME)
        } finally {
            try { ex.release() } catch (e: Exception) { }
        }
    }

    /**
     * Prepares the audio source for muxing. Returns (Uri, File?) : if File is non-null the audio is read from that file
     * (conversion result), otherwise directly from [src] (copied without re-encoding). Null = no audio.
     */
    private fun prepareAudio(ctl: MediaCtl, src: Uri, o: VideoOpts, tmp: File): Pair<Uri, File?>? {
        val mime = audioMime(ctl.ctx, src) ?: return null
        val webm = o.format == 2
        if (!webm && mime == "audio/mp4a-latm") return Pair(src, null)
        if (webm && (mime == "audio/opus" || mime == "audio/vorbis")) return Pair(src, null)
        if (webm && Build.VERSION.SDK_INT < 29) return null // WebM needs Opus; the Opus encoder only exists from Android 10
        AudioConv.convertToFile(ctl, src, AudioOpts(if (webm) 1 else 0, 128, 0, 0, 100), tmp)
        return Pair(src, tmp)
    }

    private fun encodeVideo(ctl: MediaCtl, src: Uri, o: VideoOpts, dst: File): Int {
        val ex = MediaExtractor()
        var dec: MediaCodec? = null
        var enc: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var gl: GlPipe? = null
        var muxStarted = false
        try {
            try {
                ex.setDataSource(ctl.ctx, src, null)
            } catch (e: Exception) {
                throw IOException("Format file tidak didukung (${e.message})")
            }
            val tr = findTrack(ex, "video/")
            if (tr < 0) throw IOException("Tidak ada trek video di file ini")
            ex.selectTrack(tr)
            val fmt = ex.getTrackFormat(tr)
            val mime = fmt.getString(MediaFormat.KEY_MIME) ?: throw IOException("Trek video tidak dikenal")
            val w = fmtInt(fmt, MediaFormat.KEY_WIDTH, 0)
            val h = fmtInt(fmt, MediaFormat.KEY_HEIGHT, 0)
            if (w <= 0 || h <= 0) throw IOException("Ukuran video tidak diketahui")
            val rot = ((fmtInt(fmt, MediaFormat.KEY_ROTATION, 0) % 360) + 360) % 360
            val durUs = fmtLong(fmt, MediaFormat.KEY_DURATION, 0L)
            val srcFps = fmtFloat(fmt, MediaFormat.KEY_FRAME_RATE, 30f).let { if (it < 1f) 30f else it }

            var ow = w
            var oh = h
            if (o.shortSide > 0) {
                val s = minOf(w, h)
                if (s > o.shortSide) {
                    val k = o.shortSide.toDouble() / s
                    ow = Math.round(w * k).toInt()
                    oh = Math.round(h * k).toInt()
                }
            }
            ow = maxOf(2, ow and 1.inv())
            oh = maxOf(2, oh and 1.inv())
            val dropFps = o.fps > 0 && o.fps < srcFps
            val outFps = if (o.fps > 0 && o.fps < srcFps) o.fps else Math.round(srcFps).coerceIn(1, 120)
            val factor = when (o.format) { 1 -> 0.07; 2 -> 0.08; else -> 0.1 }
            var bitrate = if (o.kbps > 0) o.kbps * 1000 else (ow.toDouble() * oh * outFps * factor).toLong().coerceIn(200_000L, 50_000_000L).toInt()
            if (o.targetBytes <= 0L && o.qualityPct in 1..99) bitrate = (bitrate.toLong() * o.qualityPct / 100).coerceAtLeast(100_000L).toInt()
            if (o.targetBytes > 0L && durUs > 0L) {
                // compress: bitrate is computed from the target size (minus an estimated 128 kbps of audio), overriding the bitrate choice
                val total = (o.targetBytes * 8 / (durUs / 1_000_000.0)).toLong()
                bitrate = (total - (if (o.keepAudio) 128_000L else 0L)).coerceIn(100_000L, 50_000_000L).toInt()
            }
            val emime = when (o.format) { 1 -> "video/hevc"; 2 -> "video/x-vnd.on2.vp9"; else -> "video/avc" }

            val e = try { MediaCodec.createEncoderByType(emime) } catch (x: Exception) { throw IOException("Perangkat ini tidak punya encoder video $emime") }
            enc = e
            val ef = MediaFormat.createVideoFormat(emime, ow, oh)
            ef.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            ef.setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            ef.setInteger(MediaFormat.KEY_FRAME_RATE, outFps)
            ef.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            try {
                ef.setInteger(MediaFormat.KEY_PRIORITY, 1) // 1 = not realtime: the encoder runs as fast as possible
                ef.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE.toInt())
            } catch (x: Exception) { }
            val inSurface: Surface = try {
                e.configure(ef, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                val surf = e.createInputSurface()
                e.start()
                surf
            } catch (x: Exception) {
                throw IOException("Encoder video menolak pengaturan ini (${ow}x$oh, ${bitrate / 1000} kbps): ${x.message}")
            }

            val g = GlPipe(inSurface, ow, oh)
            gl = g

            // Rotation is left to metadata (orientation hint) in the output file, not applied by the decoder
            fmt.setInteger(MediaFormat.KEY_ROTATION, 0)
            val d = try { MediaCodec.createDecoderByType(mime) } catch (x: Exception) { throw IOException("Perangkat tidak bisa mendekode video $mime") }
            dec = d
            try {
                fmt.setInteger(MediaFormat.KEY_PRIORITY, 1)
                fmt.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE.toInt())
            } catch (x: Exception) { }
            try {
                d.configure(fmt, g.decoderSurface, null, 0)
                d.start()
            } catch (x: Exception) {
                throw IOException("Gagal memulai decoder video $mime: ${x.message}")
            }

            val mx = MediaMuxer(dst.absolutePath, containerOf(o.format))
            muxer = mx
            if (o.format != 2) mx.setOrientationHint(rot)
            var vTrack = -1

            val einfo = MediaCodec.BufferInfo()
            fun drainEnc(eos: Boolean) {
                var idle = 0
                while (true) {
                    val oi = e.dequeueOutputBuffer(einfo, if (eos) 10_000L else 0L)
                    if (oi == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!eos) return
                        if (++idle > 3000) throw IOException("Encoder video tidak merespons")
                    } else if (oi == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        vTrack = mx.addTrack(e.outputFormat)
                        mx.start()
                        muxStarted = true
                    } else if (oi >= 0) {
                        val ob = e.getOutputBuffer(oi)
                        if ((einfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) einfo.size = 0
                        if (einfo.size > 0 && muxStarted && ob != null) {
                            ob.position(einfo.offset)
                            ob.limit(einfo.offset + einfo.size)
                            mx.writeSampleData(vTrack, ob, einfo)
                        }
                        e.releaseOutputBuffer(oi, false)
                        if ((einfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return
                    }
                }
            }

            val info = MediaCodec.BufferInfo()
            var inputDone = false
            var outputDone = false
            var firstPts = -1L
            var nextKeepUs = 0.0
            val keepEveryUs = if (dropFps) 1_000_000.0 / o.fps else 0.0
            while (!outputDone) {
                ctl.check()
                if (!inputDone) {
                    val ii = d.dequeueInputBuffer(10_000L)
                    if (ii >= 0) {
                        val ib = d.getInputBuffer(ii) ?: throw IOException("Buffer decoder tidak tersedia")
                        val n = ex.readSampleData(ib, 0)
                        if (n < 0) {
                            d.queueInputBuffer(ii, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            d.queueInputBuffer(ii, 0, n, ex.sampleTime, 0)
                            ex.advance()
                        }
                    }
                }
                val oi = d.dequeueOutputBuffer(info, 10_000L)
                if (oi >= 0) {
                    val eosFlag = (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                    var render = info.size > 0
                    var rel = 0L
                    if (render) {
                        if (firstPts < 0L) firstPts = info.presentationTimeUs
                        rel = maxOf(0L, info.presentationTimeUs - firstPts)
                        if (keepEveryUs > 0.0) {
                            if (rel + 1000.0 >= nextKeepUs) {
                                nextKeepUs += keepEveryUs
                                if (nextKeepUs < rel) nextKeepUs = rel.toDouble()
                            } else {
                                render = false
                            }
                        }
                    }
                    if (render) g.prepareFrame()
                    d.releaseOutputBuffer(oi, render)
                    if (render) {
                        g.awaitFrame()
                        g.draw(rel * 1000L)
                        drainEnc(false)
                        if (durUs > 0L) ctl.progress((info.presentationTimeUs.toFloat() / durUs).coerceIn(0f, 1f))
                    }
                    if (eosFlag) outputDone = true
                }
            }
            e.signalEndOfInputStream()
            drainEnc(true)
            if (!muxStarted) throw IOException("Tidak ada frame video yang berhasil disandikan")
            mx.stop()
            muxStarted = false
            muxer = null
            mx.release()
            return rot
        } finally {
            try { dec?.stop() } catch (x: Exception) { }
            try { dec?.release() } catch (x: Exception) { }
            try { gl?.release() } catch (x: Exception) { }
            try { enc?.stop() } catch (x: Exception) { }
            try { enc?.release() } catch (x: Exception) { }
            try { muxer?.release() } catch (x: Exception) { }
            try { ex.release() } catch (x: Exception) { }
        }
    }

    /** Merge the video track from [vFile] with the audio track (from [aFile] if present, otherwise from [aUri]) into [out], in time order. */
    private fun muxVideoAudio(ctl: MediaCtl, vFile: File, aUri: Uri, aFile: File?, out: File, container: Int, rot: Int) {
        val vex = MediaExtractor()
        val aex = MediaExtractor()
        var mux: MediaMuxer? = null
        try {
            vex.setDataSource(vFile.absolutePath)
            val vt = findTrack(vex, "video/")
            if (vt < 0) throw IOException("Trek video hasil tidak ditemukan")
            vex.selectTrack(vt)
            if (aFile != null) aex.setDataSource(aFile.absolutePath) else aex.setDataSource(ctl.ctx, aUri, null)
            val at = findTrack(aex, "audio/")
            if (at < 0) throw IOException("Trek audio tidak ditemukan")
            aex.selectTrack(at)

            val m = MediaMuxer(out.absolutePath, container)
            mux = m
            if (container == MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4) m.setOrientationHint(rot)
            val vf = vex.getTrackFormat(vt)
            val af = aex.getTrackFormat(at)
            val vi = m.addTrack(vf)
            val ai = m.addTrack(af)
            m.start()

            val cap = maxOf(2 * 1024 * 1024, fmtInt(vf, MediaFormat.KEY_MAX_INPUT_SIZE, 0), fmtInt(af, MediaFormat.KEY_MAX_INPUT_SIZE, 0))
            val buf = ByteBuffer.allocate(cap)
            val info = MediaCodec.BufferInfo()
            val vDur = fmtLong(vf, MediaFormat.KEY_DURATION, 0L)
            var vt0 = vex.sampleTime
            var at0 = aex.sampleTime
            while (vt0 >= 0L || at0 >= 0L) {
                ctl.check()
                val useVideo = at0 < 0L || (vt0 in 0L..at0)
                val ex = if (useVideo) vex else aex
                val idx = if (useVideo) vi else ai
                buf.clear()
                val n = ex.readSampleData(buf, 0)
                if (n < 0) {
                    if (useVideo) vt0 = -1L else at0 = -1L
                    continue
                }
                info.set(0, n, ex.sampleTime, ex.sampleFlags)
                m.writeSampleData(idx, buf, info)
                ex.advance()
                if (useVideo) {
                    if (vDur > 0L) ctl.progress((info.presentationTimeUs.toFloat() / vDur).coerceIn(0f, 1f))
                    vt0 = ex.sampleTime
                } else {
                    at0 = ex.sampleTime
                }
            }
            m.stop()
        } finally {
            try { mux?.release() } catch (e: Exception) { }
            try { vex.release() } catch (e: Exception) { }
            try { aex.release() } catch (e: Exception) { }
        }
    }
}


/** File grouping for the Mixed Asset Converter: 0 audio, 1 image, 2 video, -1 unrecognized. Video extensions are checked first (e.g. .mp4 = video). */
object MixedConv {
    val EXTS: Set<String> get() = VideoConv.EXTS + AudioConv.EXTS + ImageConv.EXTS

    fun classOf(name: String): Int {
        val e = name.substringAfterLast('.', "").lowercase()
        return when {
            VideoConv.EXTS.contains(e) -> 2
            AudioConv.EXTS.contains(e) -> 0
            ImageConv.EXTS.contains(e) -> 1
            else -> -1
        }
    }

    /** Processing order: audio, images, files copied as-is, then video (slowest, so results appear sooner). */
    fun rank(name: String): Int = when (classOf(name)) { 0 -> 0; 1 -> 1; 2 -> 3; else -> 2 }

    /** Video is processed one at a time (hardware codec); other files may run in parallel. */
    val videoGate = java.util.concurrent.Semaphore(1)
}
