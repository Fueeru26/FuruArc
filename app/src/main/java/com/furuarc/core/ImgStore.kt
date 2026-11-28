package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

/** Saves Image Editor projects as folders (doc.json + one PNG per bitmap layer + thumbnail) inside the app. */
object ImgStore {
    class Meta(val id: String, val name: String, val modified: Long, val w: Int, val h: Int)

    private val io = Executors.newSingleThreadExecutor()

    fun root(ctx: Context): File = File(ctx.filesDir, "imgedit/projects").also { it.mkdirs() }
    fun dir(ctx: Context, id: String): File = File(root(ctx), id)
    fun thumb(ctx: Context, id: String): File = File(dir(ctx, id), "thumb.jpg")
    fun newId() = "p" + System.currentTimeMillis()

    fun list(ctx: Context): List<Meta> {
        val out = ArrayList<Meta>()
        root(ctx).listFiles()?.forEach { d ->
            val f = File(d, "meta.json")
            if (d.isDirectory && f.exists()) try {
                val o = JSONObject(f.readText())
                out.add(Meta(d.name, o.optString("name"), o.optLong("modified"), o.optInt("w"), o.optInt("h")))
            } catch (e: Exception) { }
        }
        return out.sortedByDescending { it.modified }
    }

    fun delete(ctx: Context, id: String) { try { dir(ctx, id).deleteRecursively() } catch (e: Exception) { } }

    fun rename(ctx: Context, id: String, name: String) {
        val f = File(dir(ctx, id), "meta.json")
        try {
            val o = JSONObject(f.readText()); o.put("name", name); f.writeText(o.toString())
            val d = File(dir(ctx, id), "doc.json")
            val od = JSONObject(d.readText()); od.put("name", name); d.writeText(od.toString())
        } catch (e: Exception) { }
    }

    fun duplicate(ctx: Context, id: String, suffix: String): String? = try {
        val nid = newId() + "c"
        dir(ctx, id).copyRecursively(dir(ctx, nid), true)
        rename(ctx, nid, (list(ctx).firstOrNull { it.id == id }?.name ?: "") + suffix)
        nid
    } catch (e: Exception) { null }

    private fun specJson(t: TextSpec) = JSONObject().put("text", t.text).put("size", t.size.toDouble()).put("color", t.color).put("font", t.font)
        .put("fontPath", t.fontPath).put("bold", t.bold).put("italic", t.italic).put("underline", t.underline).put("align", t.align)
        .put("ls", t.lineSpacing.toDouble()).put("lt", t.letterSpacing.toDouble()).put("sw", t.strokeW.toDouble()).put("sc", t.strokeColor)
        .put("sh", t.shadow).put("shc", t.shadowColor).put("bgOn", t.bgOn).put("bg", t.bgColor).put("boxW", t.boxW.toDouble())

    private fun shapeJson(s: ShapeSpec) = JSONObject().put("kind", s.kind).put("w", s.w.toDouble()).put("h", s.h.toDouble()).put("fillOn", s.fillOn)
        .put("fill", s.fill).put("stroke", s.stroke).put("sw", s.strokeW.toDouble()).put("r", s.radius.toDouble()).put("n", s.sides)

    /** Writes the project; the pixels are written on a background thread (only layers whose bitmap changed). */
    fun save(ctx: Context, doc: Doc) {
        if (doc.id.isEmpty()) doc.id = newId()
        val d = dir(ctx, doc.id)
        d.mkdirs()
        val now = System.currentTimeMillis()
        val arr = JSONArray()
        val writes = ArrayList<Pair<String, Bitmap>>()
        val keep = HashSet<String>()
        for (l in doc.layers) {
            val o = JSONObject().put("id", l.id).put("kind", l.kind.name).put("name", l.name).put("vis", l.visible).put("lock", l.locked)
                .put("op", l.opacity.toDouble()).put("blend", l.blend).put("cx", l.cx.toDouble()).put("cy", l.cy.toDouble())
                .put("sx", l.sx.toDouble()).put("sy", l.sy.toDouble()).put("rot", l.rot.toDouble()).put("fill", l.fill)
            val b = l.bmp
            if (b != null) {
                val fn = "l${l.id}.png"
                o.put("file", fn); keep.add(fn)
                if (doc.savedBmps[l.id] !== b) { writes.add(fn to b); doc.savedBmps[l.id] = b }
            }
            l.text?.let { o.put("text", specJson(it)) }
            l.shape?.let { o.put("shape", shapeJson(it)) }
            arr.put(o)
        }
        val docJson = JSONObject().put("name", doc.name).put("w", doc.w).put("h", doc.h).put("bg", doc.bg).put("active", doc.activeId)
            .put("next", doc.nextId).put("layers", arr)
        val meta = JSONObject().put("name", doc.name).put("modified", now).put("w", doc.w).put("h", doc.h)
        val thumbBmp = try { ImgExport.render(doc, 192, false, true) } catch (e: Throwable) { null }
        doc.dirty = false
        io.execute {
            try {
                for ((fn, b) in writes) {
                    val tmp = File(d, "$fn.tmp")
                    tmp.outputStream().use { b.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    tmp.renameTo(File(d, fn))
                }
                d.listFiles()?.forEach { f -> if (f.name.startsWith("l") && f.name.endsWith(".png") && f.name !in keep) f.delete() }
                File(d, "doc.json").writeText(docJson.toString())
                File(d, "meta.json").writeText(meta.toString())
                if (thumbBmp != null) { File(d, "thumb.jpg").outputStream().use { thumbBmp.compress(Bitmap.CompressFormat.JPEG, 80, it) }; thumbBmp.recycle() }
            } catch (e: Exception) { }
        }
    }

    fun load(ctx: Context, id: String): Doc? {
        return try {
            val d = dir(ctx, id)
            val o = JSONObject(File(d, "doc.json").readText())
            val doc = Doc(o.getInt("w"), o.getInt("h"))
            doc.id = id; doc.name = o.optString("name"); doc.bg = o.optInt("bg", 0xFFFFFFFF.toInt())
            doc.activeId = o.optLong("active", -1L); doc.nextId = o.optLong("next", 1L)
            val arr = o.getJSONArray("layers")
            for (i in 0 until arr.length()) {
                val lo = arr.getJSONObject(i)
                val l = Layer(lo.getLong("id"), try { LKind.valueOf(lo.getString("kind")) } catch (e: Exception) { LKind.DRAW })
                l.name = lo.optString("name"); l.visible = lo.optBoolean("vis", true); l.locked = lo.optBoolean("lock", false)
                l.opacity = lo.optDouble("op", 1.0).toFloat(); l.blend = lo.optInt("blend", 0)
                l.cx = lo.optDouble("cx").toFloat(); l.cy = lo.optDouble("cy").toFloat()
                l.sx = lo.optDouble("sx", 1.0).toFloat(); l.sy = lo.optDouble("sy", 1.0).toFloat(); l.rot = lo.optDouble("rot", 0.0).toFloat()
                l.fill = lo.optInt("fill", 0xFFFFFFFF.toInt())
                if (l.hasBitmap) {
                    val opt = BitmapFactory.Options().also { it.inMutable = true; it.inPreferredConfig = Bitmap.Config.ARGB_8888 }
                    val f = File(d, lo.optString("file"))
                    val b = if (f.exists()) BitmapFactory.decodeFile(f.path, opt) else null
                    l.bmp = b ?: Bitmap.createBitmap(max(1, doc.w), max(1, doc.h), Bitmap.Config.ARGB_8888)
                    doc.savedBmps[l.id] = l.bmp!!
                }
                lo.optJSONObject("text")?.let { t ->
                    val s = TextSpec()
                    s.text = t.optString("text"); s.size = t.optDouble("size", 72.0).toFloat(); s.color = t.optInt("color"); s.font = t.optString("font", "sans")
                    s.fontPath = t.optString("fontPath"); s.bold = t.optBoolean("bold"); s.italic = t.optBoolean("italic"); s.underline = t.optBoolean("underline")
                    s.align = t.optInt("align", 1); s.lineSpacing = t.optDouble("ls", 1.0).toFloat(); s.letterSpacing = t.optDouble("lt", 0.0).toFloat()
                    s.strokeW = t.optDouble("sw", 0.0).toFloat(); s.strokeColor = t.optInt("sc"); s.shadow = t.optBoolean("sh"); s.shadowColor = t.optInt("shc")
                    s.bgOn = t.optBoolean("bgOn"); s.bgColor = t.optInt("bg"); s.boxW = t.optDouble("boxW", 800.0).toFloat()
                    l.text = s
                }
                lo.optJSONObject("shape")?.let { sh ->
                    val s = ShapeSpec()
                    s.kind = sh.optInt("kind"); s.w = sh.optDouble("w", 400.0).toFloat(); s.h = sh.optDouble("h", 300.0).toFloat(); s.fillOn = sh.optBoolean("fillOn", true)
                    s.fill = sh.optInt("fill"); s.stroke = sh.optInt("stroke"); s.strokeW = sh.optDouble("sw", 0.0).toFloat(); s.radius = sh.optDouble("r", 48.0).toFloat(); s.sides = sh.optInt("n", 5)
                    l.shape = s
                }
                doc.layers.add(l)
            }
            if (doc.layers.none { it.id == doc.activeId }) doc.activeId = doc.layers.lastOrNull()?.id ?: -1L
            doc.resetHistory("-")
            doc.dirty = false
            doc
        } catch (e: Throwable) { null }
    }

    /** Decodes an image so that its longest side is at most [maxSide] (EXIF rotation applied). */
    fun decode(ctx: Context, uri: Uri, maxSide: Int): Bitmap? {
        return try {
            val bo = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bo) }
            if (bo.outWidth <= 0) return null
            var sample = 1
            while (max(bo.outWidth, bo.outHeight) / sample > maxSide * 2) sample *= 2
            val o = BitmapFactory.Options().also { it.inSampleSize = sample; it.inPreferredConfig = Bitmap.Config.ARGB_8888; it.inMutable = true }
            var b = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) } ?: return null
            var m: Matrix? = null
            val big = max(b.width, b.height)
            if (big > maxSide) { m = Matrix(); val f = maxSide / big.toFloat(); m.postScale(f, f) }
            try {
                val deg = ctx.contentResolver.openInputStream(uri)?.use {
                    when (android.media.ExifInterface(it).getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, 1)) {
                        6 -> 90f; 3 -> 180f; 8 -> 270f; else -> 0f
                    }
                } ?: 0f
                if (deg != 0f) { if (m == null) m = Matrix(); m.postRotate(deg) }
            } catch (e: Exception) { }
            if (m != null) b = Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true)
            if (!b.isMutable) b = b.copy(Bitmap.Config.ARGB_8888, true)
            b
        } catch (e: Throwable) { null }
    }

    fun displayName(ctx: Context, uri: Uri): String = try { DocumentFile.fromSingleUri(ctx, uri)?.name ?: "image" } catch (e: Exception) { "image" }

    /** Copies a font file into the app and returns its path. */
    fun importFont(ctx: Context, uri: Uri): String? = try {
        val dirF = File(ctx.filesDir, "imgedit/fonts").also { it.mkdirs() }
        val f = File(dirF, displayName(ctx, uri).replace('/', '_'))
        ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
        android.graphics.Typeface.createFromFile(f)
        f.path
    } catch (e: Throwable) { null }

    fun prefs(ctx: Context) = ctx.getSharedPreferences("imgedit2", Context.MODE_PRIVATE)
}

object ImgExport {
    const val PNG = 0
    const val JPG = 1
    const val WEBP = 2

    fun ext(f: Int) = when (f) { JPG -> "jpg"; WEBP -> "webp"; else -> "png" }
    fun mime(f: Int) = when (f) { JPG -> "image/jpeg"; WEBP -> "image/webp"; else -> "image/png" }

    /** Renders the document at width [outW] (height follows). For JPEG or when not [transparent] a missing background is white. */
    fun render(doc: Doc, outW: Int, transparent: Boolean, fitSide: Boolean = false): Bitmap {
        val f = if (fitSide) outW / max(doc.w, doc.h).toFloat() else outW / doc.w.toFloat()
        val w = max(1, (doc.w * f).roundToInt()); val h = max(1, (doc.h * f).roundToInt())
        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        if (!transparent && doc.bg == 0) c.drawColor(0xFFFFFFFF.toInt())
        c.scale(w / doc.w.toFloat(), h / doc.h.toFloat())
        ImgRender.drawDoc(c, doc, !transparent, null)
        return b
    }

    fun write(ctx: Context, bmp: Bitmap, uri: Uri, fmt: Int, quality: Int): Boolean = try {
        val cf = when (fmt) { JPG -> Bitmap.CompressFormat.JPEG; WEBP -> Bitmap.CompressFormat.WEBP; else -> Bitmap.CompressFormat.PNG }
        ctx.contentResolver.openOutputStream(uri, "wt")!!.use { bmp.compress(cf, quality.coerceIn(1, 100), it) }
    } catch (e: Throwable) { false }
}
