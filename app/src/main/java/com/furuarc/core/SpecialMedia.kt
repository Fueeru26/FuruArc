package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Reads engine-specific media assets for the Game Assets list (image preview, audio playback):
 * images are decoded to Bitmap, audio is decoded to a .wav/.ogg file in the cache and then played as usual.
 * Paired files (base image PGD/CRXD, alpha RC8) are not looked up here, so difference images may fail to load.
 */
object SpecialMedia {
    val IMG = setOf("crx", "pgd", "pna", "ycg", "hg2", "hg3", "tlg", "eri", "g00", "rct", "rc8")
    val AUD = setOf("pcm", "nwa", "mio", "ovk")
    private const val LIMIT = 128L * 1024 * 1024

    /** Last error message per asset (asset id -> message), shown in the viewer/player. */
    val errors = java.util.concurrent.ConcurrentHashMap<String, String>()

    fun errorOf(id: String): String? = errors[id]

    fun describe(e: Throwable): String = (e.javaClass.simpleName + (e.message?.let { ": $it" } ?: "")).take(200)

    private fun ext(n: String) = n.substringAfterLast('/').substringAfterLast('.', "").lowercase()

    fun isImage(name: String) = ext(name) in IMG
    fun isAudio(name: String) = ext(name) in AUD

    private fun bytes(ctx: Context, a: GAsset): ByteArray {
        val arc = a.arc
        if (arc != null) return GameArc.bytes(ctx, arc, a.entry, LIMIT)
        if (a.size > LIMIT) throw IOException("File terlalu besar")
        return ctx.contentResolver.openInputStream(a.uri)?.use { it.readBytes() } ?: throw IOException("Tidak bisa membaca file")
    }

    internal fun crxToRgba(c: CrxImage): RgbaImage {
        val w = c.width
        val h = c.height
        val out = ByteArray(w * h * 4)
        for (y in 0 until h) {
            val s = y * c.stride
            for (x in 0 until w) {
                val o = (y * w + x) * 4
                when (c.bpp) {
                    8 -> {
                        val i = (c.data[s + x].toInt() and 0xff) * 3
                        val pal = c.palette
                        if (pal != null && i + 2 < pal.size) { out[o] = pal[i]; out[o + 1] = pal[i + 1]; out[o + 2] = pal[i + 2] }
                        else { out[o] = c.data[s + x]; out[o + 1] = c.data[s + x]; out[o + 2] = c.data[s + x] }
                        out[o + 3] = -1
                    }
                    24 -> { out[o] = c.data[s + x * 3 + 2]; out[o + 1] = c.data[s + x * 3 + 1]; out[o + 2] = c.data[s + x * 3]; out[o + 3] = -1 }
                    else -> { out[o] = c.data[s + x * 4 + 2]; out[o + 1] = c.data[s + x * 4 + 1]; out[o + 2] = c.data[s + x * 4]; out[o + 3] = c.data[s + x * 4 + 3] }
                }
            }
        }
        return RgbaImage(w, h, out)
    }

    fun image(ctx: Context, a: GAsset): RgbaImage {
        errors.remove(a.id)
        val b = bytes(ctx, a)
        return SpecialConv.decodeImage(ext(a.name), b, "") { null }
    }

    /** Special image -> Bitmap, downscaled so the longest side is <= [max]. null = failed. */
    fun bitmap(ctx: Context, a: GAsset, max: Int): Bitmap? {
        return try {
            val img = image(ctx, a)
            val n = img.width * img.height
            val px = IntArray(n)
            val s = img.rgba
            for (i in 0 until n) {
                px[i] = ((s[i * 4 + 3].toInt() and 0xff) shl 24) or ((s[i * 4].toInt() and 0xff) shl 16) or
                    ((s[i * 4 + 1].toInt() and 0xff) shl 8) or (s[i * 4 + 2].toInt() and 0xff)
            }
            val bm = Bitmap.createBitmap(px, img.width, img.height, Bitmap.Config.ARGB_8888)
            val big = maxOf(img.width, img.height)
            if (big > max) {
                val k = max.toFloat() / big
                Bitmap.createScaledBitmap(bm, (img.width * k).toInt().coerceAtLeast(1), (img.height * k).toInt().coerceAtLeast(1), true)
            } else bm
        } catch (e: Throwable) {
            errors[a.id] = describe(e)
            null
        }
    }

    /** Special audio -> .wav/.ogg file in the cache (reused if it already exists). */
    fun audioFile(ctx: Context, a: GAsset): File {
        errors.remove(a.id)
        try {
            return audioFile0(ctx, a)
        } catch (e: Throwable) {
            errors[a.id] = describe(e)
            throw e
        }
    }

    private fun audioFile0(ctx: Context, a: GAsset): File {
        val base = Integer.toHexString(a.id.hashCode()) + "_" + a.size + ".sp"
        for (x in arrayOf("ogg", "wav")) {
            val f = File(GameCache.dir(), "$base.$x")
            if (f.exists() && f.length() > 0) { f.setLastModified(System.currentTimeMillis()); return f }
        }
        val b = bytes(ctx, a)
        var outExt = "wav"
        val data: ByteArray = when (ext(a.name)) {
            "pcm" -> {
                val info = CircusPcm.parse(b.copyOf(minOf(b.size, 0x1C)))
                if (info.mode == 1 || info.mode == 3) throw IOException("XPCM mode ${info.mode} belum didukung")
                val bo = ByteArrayOutputStream()
                if (info.mode == 5) { outExt = "ogg"; CircusPcm.copyOgg(ByteArrayInputStream(b), info, bo) }
                else CircusPcm.writeWav(ByteArrayInputStream(b), info, bo)
                bo.toByteArray()
            }
            "nwa" -> SpecialConv.wavBytes(NwaAudio.decode(b))
            "mio" -> {
                val og = MioAudio.oggOrNull(b)
                if (og != null) { outExt = "ogg"; og } else SpecialConv.wavBytes(MioAudio.decode(b))
            }
            "ovk" -> {
                // OVK contains many Oggs; only the first entry is played (all of them: Special Asset Converter)
                outExt = "ogg"
                OvkAudio.extract(b).firstOrNull()?.data ?: throw IOException("OVK kosong")
            }
            else -> throw IOException("Jenis audio tidak didukung")
        }
        val f = File(GameCache.dir(), "$base.$outExt")
        val tmp = File(f.path + ".part")
        FileOutputStream(tmp).use { it.write(data) }
        tmp.renameTo(f)
        GameCache.trim()
        return f
    }
}
