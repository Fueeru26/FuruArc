package com.furuarc.core

import android.net.Uri
import com.furuarc.MediaItem
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * [audio]: 0 = automatic (original Ogg if the XPCM contains Ogg, otherwise WAV), 1 = always WAV, 2 = OGG Vorbis (PCM audio is encoded).
 * [imgOut]: 0 = automatic (PNG if there are transparent pixels, otherwise JPG), 1 = PNG, 2 = JPG.
 * [allFrames]: HG-2/HG-3 and PNA contain many frames; true = all frames in one .zip, false = first frame only (.png).
 */
class SpecialOpts(val audio: Int, val allFrames: Boolean = false, val imgOut: Int = 0) {
    val audioWav: Boolean get() = audio == 1
}

/** One engine in the Special Asset Converter menu: [exts] = special asset extensions that can be converted. */
class SpEngine(val id: String, val name: String, val exts: Set<String>)

/**
 * Special Asset Converter: engine-native game asset formats that ordinary tools cannot open.
 * Currently: Circus (Adv32) .pcm (XPCM) -> OGG/WAV and .crx (CRXG/CRXD) -> PNG.
 */
object SpecialConv {
    val EXTS = setOf("pcm", "crx")

    val ENGINES = listOf(
        SpEngine("circus", "Adv32 (Circus)", setOf("crx", "pgd", "pna", "pcm")),
        SpEngine("artemis", "Artemis", setOf("pgd")),
        SpEngine("bgi", "BGI / Ethornell", setOf("ovk")),
        SpEngine("catsystem2", "CatSystem 2", setOf("hg2", "hg3")),
        SpEngine("entisgls", "EntisGLS", setOf("eri", "emi", "mio")),
        SpEngine("kirikiri", "Kirikiri", setOf("tlg", "ovk", "mio")),
        SpEngine("majiro", "Majiro", setOf("rct", "rc8")),
        SpEngine("nexas", "NeXAS", setOf("spm")),
        SpEngine("siglus", "Siglus Engine", setOf("g00", "nwa")),
        SpEngine("yuris", "Yu-Ris", setOf("pgd", "ycg")),
    )

    private val IMG = setOf("crx", "pgd", "pna", "ycg", "hg2", "hg3", "tlg", "eri", "g00", "spm", "rct", "rc8")
    private val AUD_PCM = setOf("nwa", "mio", "emi")

    private fun base(n: String) = n.substringBeforeLast('.', n)
    private fun ext(n: String) = n.substringAfterLast('.', "").lowercase()

    private fun head(ctl: MediaCtl, uri: Uri, n: Int): ByteArray {
        val ins = ctl.ctx.contentResolver.openInputStream(uri) ?: throw IOException("Tidak bisa membaca file")
        ins.use {
            val b = ByteArray(n)
            var k = 0
            while (k < n) {
                val r = it.read(b, k, n - k)
                if (r <= 0) break
                k += r
            }
            return b.copyOf(k)
        }
    }

    /** Output name: .png for CRX; XPCM becomes .ogg if it contains Ogg (and WAV is not forced), otherwise .wav. */
    fun outName(ctx: android.content.Context, item: MediaItem, o: SpecialOpts): String {
        val e = ext(item.name)
        if (e == "ovk") return base(item.name) + ".zip"
        if ((e == "hg2" || e == "hg3" || e == "pna") && o.allFrames) return base(item.name) + ".zip"
        if (e in IMG) return base(item.name) + imgExt(ctx, item, o)
        if (e in AUD_PCM) return base(item.name) + if (o.audio == 2) ".ogg" else ".wav"
        if (e == "pcm" && !o.audioWav) {
            try {
                val ins = ctx.contentResolver.openInputStream(item.uri)
                if (ins != null) {
                    ins.use {
                        val b = ByteArray(0x10)
                        var k = 0
                        while (k < b.size) {
                            val r = it.read(b, k, b.size - k)
                            if (r <= 0) break
                            k += r
                        }
                        if (k >= 0x10 && CircusPcm.isPcm(b) && (b[8].toInt() and 0xFF) == 5) return base(item.name) + ".ogg"
                    }
                }
            } catch (ex: Exception) {
            }
        }
        return base(item.name) + ".wav"
    }

    /**
     * [lookup] finds the CRXD base image file by name in the same folder as [item] (null if absent).
     */
    fun convert(ctl: MediaCtl, item: MediaItem, o: SpecialOpts, lookup: (MediaItem, String) -> MediaItem?, sink: () -> OutputStream?): Int {
        return when (ext(item.name)) {
            "pcm" -> pcm(ctl, item, o, sink)
            in IMG, in AUD_PCM, "ovk" -> generic(ctl, item, o, lookup, sink)
            else -> throw IOException("Jenis file tidak didukung")
        }
    }

    private fun readAll(ctl: MediaCtl, uri: Uri, limit: Long): ByteArray {
        val ins = ctl.ctx.contentResolver.openInputStream(uri) ?: throw IOException("Tidak bisa membaca file")
        ins.use {
            val bo = ByteArrayOutputStream()
            val buf = ByteArray(256 * 1024)
            var total = 0L
            while (true) {
                val r = it.read(buf)
                if (r <= 0) break
                total += r
                if (total > limit) throw IOException("File terlalu besar")
                bo.write(buf, 0, r)
            }
            return bo.toByteArray()
        }
    }

    private fun pcm(ctl: MediaCtl, item: MediaItem, o: SpecialOpts, sink: () -> OutputStream?): Int {
        val info = CircusPcm.parse(head(ctl, item.uri, 0x1C))
        if (info.mode == 1 || info.mode == 3) {
            throw IOException("XPCM mode ${info.mode} (${CircusPcm.modeName(info.mode)}) belum didukung. Yang didukung: Ogg, PCM mentah, ADPCM")
        }
        if (info.mode != 5 && info.mode != 0 && info.mode != 2) throw IOException("XPCM mode ${info.mode} tidak dikenal")
        if (info.mode == 5 && !o.audioWav) {
            val os = sink() ?: return MEDIA_SKIP
            val ins = ctl.ctx.contentResolver.openInputStream(item.uri) ?: throw IOException("Tidak bisa membaca file")
            ins.use { i -> os.use { out -> CircusPcm.copyOgg(i, info, out) } }
            ctl.progress(1f)
            return MEDIA_OK
        }
        if (info.mode == 5) {
            // Ogg Vorbis inside XPCM -> WAV via the built-in Android decoder
            val tmpOgg = File.createTempFile("xpcm", ".ogg", ctl.tmpDir)
            val tmpWav = File.createTempFile("xpcm", ".wav", ctl.tmpDir)
            try {
                val ins = ctl.ctx.contentResolver.openInputStream(item.uri) ?: throw IOException("Tidak bisa membaca file")
                ins.use { i -> tmpOgg.outputStream().use { out -> CircusPcm.copyOgg(i, info, out) } }
                ctl.check()
                AudioConv.convertToFile(ctl.sub(0f, 0.9f), Uri.fromFile(tmpOgg), AudioOpts(2, 0, 0, 0, 100, keepLoop = false), tmpWav)
                ctl.check()
                val os = sink() ?: return MEDIA_SKIP
                tmpWav.inputStream().use { i -> os.use { out -> i.copyTo(out, 256 * 1024) } }
            } finally {
                tmpOgg.delete()
                tmpWav.delete()
            }
            ctl.progress(1f)
            return MEDIA_OK
        }
        // mode 0 / 2: PCM -> WAV
        val bo = ByteArrayOutputStream()
        val ins = ctl.ctx.contentResolver.openInputStream(item.uri) ?: throw IOException("Tidak bisa membaca file")
        ins.use { CircusPcm.writeWav(it, info, bo) }
        ctl.check()
        val os = sink() ?: return MEDIA_SKIP
        os.use { it.write(bo.toByteArray()) }
        ctl.progress(1f)
        return MEDIA_OK
    }

    // ---------- other formats (pure decoders in core/Img*.kt and core/Aud*.kt) ----------

    private fun toPng(img: RgbaImage, os: OutputStream) {
        val n = img.width * img.height
        val bgra = ByteArray(n * 4)
        val src = img.rgba
        var i = 0
        while (i < n * 4) {
            bgra[i] = src[i + 2]; bgra[i + 1] = src[i + 1]; bgra[i + 2] = src[i]; bgra[i + 3] = src[i + 3]
            i += 4
        }
        Crx.writePng(CrxImage(img.width, img.height, 32, img.width * 4, bgra, null, 0, 0), os)
    }

    internal fun installJpeg() {
        if (HgImage.jpegDecoder != null) return
        HgImage.jpegDecoder = { jb -> androidImage(jb) }
    }

    internal fun wavBytes(a: PcmAudio): ByteArray {
        val n = a.pcm.size
        val bo = ByteArrayOutputStream(n + 44)
        fun le32(v: Int) { bo.write(v and 0xff); bo.write((v shr 8) and 0xff); bo.write((v shr 16) and 0xff); bo.write((v shr 24) and 0xff) }
        fun le16(v: Int) { bo.write(v and 0xff); bo.write((v shr 8) and 0xff) }
        bo.write("RIFF".toByteArray()); le32(36 + n); bo.write("WAVEfmt ".toByteArray())
        le32(16); le16(1); le16(a.channels); le32(a.rate); le32(a.rate * a.channels * 2); le16(a.channels * 2); le16(16)
        bo.write("data".toByteArray()); le32(n)
        bo.write(a.pcm)
        return bo.toByteArray()
    }

    private fun writePcm(ctl: MediaCtl, a: PcmAudio, o: SpecialOpts, sink: () -> OutputStream?): Int {
        val wav = wavBytes(a)
        ctl.check()
        if (o.audio != 2) {
            val os = sink() ?: return MEDIA_SKIP
            os.use { it.write(wav) }
            ctl.progress(1f)
            return MEDIA_OK
        }
        val tmpWav = File.createTempFile("spc", ".wav", ctl.tmpDir)
        val tmpOgg = File.createTempFile("spc", ".ogg", ctl.tmpDir)
        try {
            tmpWav.writeBytes(wav)
            AudioConv.convertToFile(ctl.sub(0.2f, 0.95f), Uri.fromFile(tmpWav), AudioOpts(5, 160, 0, 0, 100, keepLoop = false), tmpOgg)
            ctl.check()
            val os = sink() ?: return MEDIA_SKIP
            tmpOgg.inputStream().use { i -> os.use { out -> i.copyTo(out, 256 * 1024) } }
        } finally {
            tmpWav.delete(); tmpOgg.delete()
        }
        ctl.progress(1f)
        return MEDIA_OK
    }

    private fun writeFrames(ctl: MediaCtl, name: String, frames: List<RgbaImage>, sink: () -> OutputStream?): Int {
        val os = sink() ?: return MEDIA_SKIP
        java.util.zip.ZipOutputStream(BufferedOutputStream(os, 64 * 1024)).use { z ->
            for ((i, f) in frames.withIndex()) {
                ctl.check()
                z.putNextEntry(java.util.zip.ZipEntry(base(name) + "_" + (i + 1).toString().padStart(3, '0') + ".png"))
                toPng(f, z)
                z.closeEntry()
                ctl.progress(0.7f + 0.3f * (i + 1) / frames.size)
            }
        }
        return MEDIA_OK
    }

    private fun generic(ctl: MediaCtl, item: MediaItem, o: SpecialOpts, lookup: (MediaItem, String) -> MediaItem?, sink: () -> OutputStream?): Int {
        val e = ext(item.name)
        val b = readAll(ctl, item.uri, 512L * 1024 * 1024)
        ctl.check()
        ctl.progress(0.3f)
        fun sib(name: String): ByteArray? {
            val bi = lookup(item, name) ?: return null
            return readAll(ctl, bi.uri, 256L * 1024 * 1024)
        }
        if (e == "ovk") {
            if (!OvkAudio.isOvk(b)) throw IOException("Bukan file OVK yang dikenali")
            val files = OvkAudio.extract(b)
            if (files.isEmpty()) throw IOException("OVK kosong")
            val os = sink() ?: return MEDIA_SKIP
            java.util.zip.ZipOutputStream(BufferedOutputStream(os, 64 * 1024)).use { z ->
                for ((i, f) in files.withIndex()) {
                    ctl.check()
                    z.putNextEntry(java.util.zip.ZipEntry(base(item.name) + "_" + f.name))
                    z.write(f.data)
                    z.closeEntry()
                    ctl.progress(0.3f + 0.7f * (i + 1) / files.size)
                }
            }
            return MEDIA_OK
        }
        if (e == "nwa") {
            if (!NwaAudio.isNwa(b)) throw IOException("Bukan file NWA yang dikenali")
            return writePcm(ctl, NwaAudio.decode(b), o, sink)
        }
        if (e == "mio" || e == "emi") {
            if (!MioAudio.isMio(b)) throw IOException("Bukan file MIO/EMI yang dikenali")
            if (o.audio == 2) {
                val og = MioAudio.oggOrNull(b)
                if (og != null) {
                    val os = sink() ?: return MEDIA_SKIP
                    os.use { it.write(og) }
                    return MEDIA_OK
                }
            }
            return writePcm(ctl, MioAudio.decode(b), o, sink)
        }
        val stem = base(item.name)
        if ((e == "hg2" || e == "hg3") && o.allFrames) {
            installJpeg()
            return writeFrames(ctl, item.name, HgImage.decodeAll(b), sink)
        }
        if (e == "pna" && o.allFrames) return writeFrames(ctl, item.name, PnaImage.decodeAll(b), sink)
        val img = decodeImage(e, b, stem) { n -> sib(n) }
        ctl.check()
        ctl.progress(0.8f)
        val jpg = isJpg(img, o)
        val os = sink() ?: return MEDIA_SKIP
        BufferedOutputStream(os, 64 * 1024).use { if (jpg) toJpg(img, it) else toPng(img, it) }
        ctl.progress(1f)
        return MEDIA_OK
    }

    // ---------- images: decode, standard format detection, PNG/JPG ----------

    /** Some games put plain WebP/PNG/JPEG under special extensions (e.g. .tlg containing WebP): recognize it from the file head. */
    fun isStdImage(b: ByteArray): Boolean {
        if (b.size < 12) return false
        fun at(i: Int) = b[i].toInt() and 0xFF
        return (at(0) == 0x89 && at(1) == 0x50 && at(2) == 0x4E && at(3) == 0x47) ||
            (at(0) == 0xFF && at(1) == 0xD8 && at(2) == 0xFF) ||
            (at(0) == 0x52 && at(1) == 0x49 && at(2) == 0x46 && at(3) == 0x46 && at(8) == 0x57 && at(9) == 0x45 && at(10) == 0x42 && at(11) == 0x50) ||
            (at(0) == 0x42 && at(1) == 0x4D) || (at(0) == 0x47 && at(1) == 0x49 && at(2) == 0x46 && at(3) == 0x38)
    }

    /** Decode via the Android decoders (PNG/JPEG/WebP/BMP/GIF). */
    fun androidImage(b: ByteArray): RgbaImage? {
        val bm = android.graphics.BitmapFactory.decodeByteArray(b, 0, b.size) ?: return null
        val w = bm.width
        val h = bm.height
        val px = IntArray(w * h)
        bm.getPixels(px, 0, w, 0, 0, w, h)
        bm.recycle()
        val out = ByteArray(w * h * 4)
        for (k in px.indices) {
            val c = px[k]
            out[k * 4] = (c shr 16).toByte(); out[k * 4 + 1] = (c shr 8).toByte(); out[k * 4 + 2] = c.toByte(); out[k * 4 + 3] = (c ushr 24).toByte()
        }
        return RgbaImage(w, h, out)
    }

    /** Decode one special image ([e] = extension, [stem] = name without extension, [sib] = look for the companion file). */
    fun decodeImage(e: String, b: ByteArray, stem: String, sib: (String) -> ByteArray?): RgbaImage {
        if (isStdImage(b)) return androidImage(b) ?: throw IOException("Dekoder Android gagal membaca gambar")
        return when (e) {
            "crx" ->
                if (Crx.isCrxd(b)) SpecialMedia.crxToRgba(Crx.decodeCrxd(b) { n -> sib(n) })
                else if (Crx.isCrxg(b)) SpecialMedia.crxToRgba(Crx.decode(b))
                else throw IOException("Bukan file CRX (kepala CRXG/CRXD tidak ditemukan)")
            "pgd" -> PgdImage.decode(b) { n -> sib(n) ?: sib(n + ".pgd") }
            "pna" -> PnaImage.decode(b)
            "ycg" -> YcgImage.decode(b)
            "hg2", "hg3" -> { installJpeg(); HgImage.decode(b) }
            "tlg" -> TlgImage.decode(b)
            "eri" -> EriImage.decode(b)
            "g00" -> G00Image.decode(b)
            "spm" -> SpmImage.decode(b)
            "rct" -> MajiroImage.decode(b, sib(stem + "_.rc8") ?: sib(stem + ".rc8"), null) { n -> sib(n) }
            "rc8" -> MajiroImage.decode(b)
            else -> throw IOException("Jenis file tidak didukung")
        }
    }

    private fun hasAlpha(img: RgbaImage): Boolean {
        val s = img.rgba
        var i = 3
        val n = img.width * img.height * 4
        while (i < n) { if (s[i].toInt() != -1) return true; i += 4 }
        return false
    }

    private fun isJpg(img: RgbaImage, o: SpecialOpts): Boolean = when (o.imgOut) { 1 -> false; 2 -> true; else -> !hasAlpha(img) }

    private val autoCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    /** Output image extension. Auto mode must decode to know whether there is transparency (the result is remembered). */
    private fun imgExt(ctx: android.content.Context, item: MediaItem, o: SpecialOpts): String {
        if (o.imgOut == 1) return ".png"
        if (o.imgOut == 2) return ".jpg"
        val key = item.uri.toString()
        autoCache[key]?.let { return it }
        val r = try {
            val e = ext(item.name)
            val b = ctx.contentResolver.openInputStream(item.uri)?.use { it.readBytes() } ?: return ".png"
            val img = decodeImage(e, b, base(item.name)) { null }
            if (hasAlpha(img)) ".png" else ".jpg"
        } catch (ex: Throwable) {
            ".png"
        }
        autoCache[key] = r
        return r
    }

    private fun toJpg(img: RgbaImage, os: OutputStream) {
        val n = img.width * img.height
        val px = IntArray(n)
        val s = img.rgba
        for (i in 0 until n) {
            px[i] = (0xFF shl 24) or ((s[i * 4].toInt() and 0xFF) shl 16) or ((s[i * 4 + 1].toInt() and 0xFF) shl 8) or (s[i * 4 + 2].toInt() and 0xFF)
        }
        val bm = android.graphics.Bitmap.createBitmap(px, img.width, img.height, android.graphics.Bitmap.Config.ARGB_8888)
        try {
            if (!bm.compress(android.graphics.Bitmap.CompressFormat.JPEG, 92, os)) throw IOException("Gagal menulis JPG")
        } finally {
            bm.recycle()
        }
    }
}
