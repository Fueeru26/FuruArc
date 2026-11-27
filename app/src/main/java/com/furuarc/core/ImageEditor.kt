package com.furuarc.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.Engine
import com.furuarc.storage.listRootId
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Image Editor state and bitmap operations. One image is edited at a time; every change keeps the
 * previous bitmap on an undo list (a few steps, to limit memory). Large images are decoded at a reduced size.
 */
object ImageEditor {
    /** Longest side kept when an image is opened (larger images are decoded smaller). */
    const val MAX_SIDE = 3072
    private const val MAX_UNDO = 6

    const val FMT_PNG = 0
    const val FMT_JPG = 1
    const val FMT_WEBP = 2

    var bitmap by mutableStateOf<Bitmap?>(null)
        private set
    /** Bumped after an in-place change so the canvas redraws. */
    var version by mutableStateOf(0)
        private set
    var name by mutableStateOf("")
    var sourceUri: Uri? = null
    var dirty by mutableStateOf(false)
    var scaledDown by mutableStateOf(false)
    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set
    /** Shown by the screen when Back is pressed with unsaved changes. */
    var askDiscard by mutableStateOf(false)

    private val undo = ArrayList<Bitmap>()
    private val redo = ArrayList<Bitmap>()

    /** True when it is fine to leave; otherwise [askDiscard] is raised and the screen asks first. */
    fun tryExit(): Boolean {
        if (bitmap != null && dirty) { askDiscard = true; return false }
        close()
        return true
    }

    fun close() {
        bitmap = null; undo.clear(); redo.clear()
        canUndo = false; canRedo = false; dirty = false; askDiscard = false
        sourceUri = null; name = ""; scaledDown = false
        folder = emptyList(); folderIndex = -1; folderLoading = false; folderName = ""; overwriteOk = false
    }

    /** Opens an image (decoded at a reduced size when its longest side is above [MAX_SIDE]). */
    fun open(ctx: Context, uri: Uri, displayName: String): Boolean {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE) sample *= 2
            val o = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
            val b = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, o) } ?: return false
            bitmap = mutable(b)
            undo.clear(); redo.clear(); canUndo = false; canRedo = false
            dirty = false; askDiscard = false
            sourceUri = uri; name = displayName; scaledDown = sample > 1
            version++
            true
        } catch (e: Throwable) {
            false
        }
    }

    private fun mutable(b: Bitmap): Bitmap =
        if (b.isMutable && b.config == Bitmap.Config.ARGB_8888) b else b.copy(Bitmap.Config.ARGB_8888, true)

    /** Keeps a copy of the current bitmap before an in-place change. */
    private fun pushCopy() {
        val b = bitmap ?: return
        undo.add(b.copy(Bitmap.Config.ARGB_8888, true))
        while (undo.size > MAX_UNDO) undo.removeAt(0)
        redo.clear(); canUndo = true; canRedo = false; dirty = true
    }

    /** A change that produces a new bitmap (crop, rotate, resize): the old bitmap goes on the undo list as it is. */
    private fun replace(nb: Bitmap) {
        val b = bitmap ?: return
        undo.add(b)
        while (undo.size > MAX_UNDO) undo.removeAt(0)
        redo.clear(); canUndo = true; canRedo = false; dirty = true
        bitmap = mutable(nb)
        version++
    }

    fun undo() {
        val b = bitmap ?: return
        if (undo.isEmpty()) return
        redo.add(b)
        bitmap = undo.removeAt(undo.size - 1)
        canUndo = undo.isNotEmpty(); canRedo = true; dirty = true; version++
    }

    fun redo() {
        val b = bitmap ?: return
        if (redo.isEmpty()) return
        undo.add(b)
        bitmap = redo.removeAt(redo.size - 1)
        canUndo = true; canRedo = redo.isNotEmpty(); dirty = true; version++
    }

    fun markSaved() { dirty = false }

    fun pixelAt(x: Int, y: Int): Int? {
        val b = bitmap ?: return null
        if (x < 0 || y < 0 || x >= b.width || y >= b.height) return null
        return b.getPixel(x, y)
    }

    // ---- Transforms ----

    fun rotate(deg: Float) {
        val b = bitmap ?: return
        val m = Matrix().apply { postRotate(deg) }
        replace(Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true))
    }

    fun flip(horizontal: Boolean) {
        val b = bitmap ?: return
        val m = Matrix().apply { if (horizontal) preScale(-1f, 1f) else preScale(1f, -1f) }
        replace(Bitmap.createBitmap(b, 0, 0, b.width, b.height, m, true))
    }

    fun crop(r: Rect) {
        val b = bitmap ?: return
        val c = clamp(r, b) ?: return
        replace(Bitmap.createBitmap(b, c.left, c.top, c.width(), c.height()).copy(Bitmap.Config.ARGB_8888, true))
    }

    /** True when the new size is allowed (1..8192 per side, at most 40 million pixels). */
    fun sizeOk(w: Int, h: Int) = w in 1..8192 && h in 1..8192 && w.toLong() * h <= 40_000_000L

    fun resize(w: Int, h: Int) {
        val b = bitmap ?: return
        if (!sizeOk(w, h)) return
        replace(Bitmap.createScaledBitmap(b, w, h, true))
    }

    // ---- Painting ----

    private fun clamp(r: Rect, b: Bitmap): Rect? {
        val c = Rect(maxOf(0, r.left), maxOf(0, r.top), minOf(b.width, r.right), minOf(b.height, r.bottom))
        return if (c.width() >= 1 && c.height() >= 1) c else null
    }

    /** Fills the rectangle with [color] exactly (alpha included). */
    fun fill(r: Rect, color: Int) {
        val b = bitmap ?: return
        val c = clamp(r, b) ?: return
        pushCopy()
        val p = Paint().apply { this.color = color; style = Paint.Style.FILL; xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }
        Canvas(b).drawRect(c, p)
        version++
    }

    /** Makes the rectangle transparent. */
    fun clear(r: Rect) = fill(r, 0x00000000)

    /** Draws a stroke through [pts] (bitmap coordinates). [erase] makes the touched pixels transparent. */
    fun stroke(pts: List<PointF>, color: Int, size: Float, erase: Boolean) {
        val b = bitmap ?: return
        if (pts.isEmpty()) return
        pushCopy()
        val cv = Canvas(b)
        val p = Paint().apply {
            isAntiAlias = true
            this.color = if (erase) 0x00000000 else color
            strokeWidth = size
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            if (erase) xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }
        if (pts.size == 1) {
            p.style = Paint.Style.FILL
            cv.drawCircle(pts[0].x, pts[0].y, size / 2f, p)
        } else {
            p.style = Paint.Style.STROKE
            val path = android.graphics.Path()
            path.moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) path.lineTo(pts[i].x, pts[i].y)
            cv.drawPath(path, p)
        }
        version++
    }

    /** Draws [text] centered on ([cx], [cy]); several lines are centered too. [outlineW] > 0 adds an outline. */
    fun text(text: String, cx: Float, cy: Float, size: Float, color: Int, outlineColor: Int, outlineW: Float, typeface: Typeface) {
        val b = bitmap ?: return
        if (text.isEmpty()) return
        pushCopy()
        val cv = Canvas(b)
        val fill = Paint().apply {
            isAntiAlias = true; this.color = color; textSize = size; this.typeface = typeface; textAlign = Paint.Align.CENTER; style = Paint.Style.FILL
        }
        val line = Paint(fill).apply {
            style = Paint.Style.STROKE; this.color = outlineColor; strokeWidth = outlineW; strokeJoin = Paint.Join.ROUND
        }
        val lines = text.split("\n")
        val fm = fill.fontMetrics
        val lh = fm.descent - fm.ascent + fm.leading
        val top = cy - lh * lines.size / 2f
        lines.forEachIndexed { i, s ->
            val y = top + i * lh - fm.ascent
            if (outlineW > 0f) cv.drawText(s, cx, y, line)
            cv.drawText(s, cx, y, fill)
        }
        version++
    }

    /** A font file chosen by the user, copied to the cache so it can be loaded. Null when it is not a usable font. */
    fun loadFont(ctx: Context, uri: Uri): Typeface? {
        return try {
            val f = File(ctx.cacheDir, "imageedit_font.ttf")
            ctx.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } } ?: return null
            Typeface.createFromFile(f)
        } catch (e: Throwable) {
            null
        }
    }

    // ---- Saving ----

    /** Format for a file name by its extension, or null when unknown. */
    fun formatOf(fileName: String): Int? = when (fileName.substringAfterLast('.', "").lowercase()) {
        "png" -> FMT_PNG
        "jpg", "jpeg" -> FMT_JPG
        "webp" -> FMT_WEBP
        else -> null
    }

    fun mimeOf(fmt: Int) = when (fmt) { FMT_JPG -> "image/jpeg"; FMT_WEBP -> "image/webp"; else -> "image/png" }
    fun extOf(fmt: Int) = when (fmt) { FMT_JPG -> "jpg"; FMT_WEBP -> "webp"; else -> "png" }

    /** Writes the image to [uri]. JPEG has no transparency, so the picture is put on white first. [truncate] for overwriting an existing file. */
    fun write(ctx: Context, uri: Uri, fmt: Int, quality: Int, truncate: Boolean): Boolean {
        val b = bitmap ?: return false
        return try {
            val q = quality.coerceIn(1, 100)
            val out = ctx.contentResolver.openOutputStream(uri, if (truncate) "wt" else "w") ?: return false
            out.use { o ->
                when (fmt) {
                    FMT_JPG -> {
                        val flat = Bitmap.createBitmap(b.width, b.height, Bitmap.Config.ARGB_8888)
                        Canvas(flat).apply { drawColor(0xFFFFFFFF.toInt()); drawBitmap(b, 0f, 0f, null) }
                        flat.compress(Bitmap.CompressFormat.JPEG, q, o)
                    }
                    FMT_WEBP -> {
                        @Suppress("DEPRECATION")
                        val f = if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                        b.compress(f, q, o)
                    }
                    else -> b.compress(Bitmap.CompressFormat.PNG, 100, o)
                }
            }
            true
        } catch (e: Throwable) {
            false
        }
    }

    // ---- Folder of images (browse and save in place) ----

    class FolderImg(val path: String, val uri: Uri)

    var folder by mutableStateOf<List<FolderImg>>(emptyList())
        private set
    var folderIndex by mutableStateOf(-1)
        private set
    var folderLoading by mutableStateOf(false)
        private set
    var folderName by mutableStateOf("")
        private set
    /** True once the user confirmed overwriting the files of the folder ("Save and next"). */
    var overwriteOk = false

    private val IMAGE_EXT = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif")

    private fun walkImages(ctx: Context, tree: Uri): List<FolderImg> {
        val out = ArrayList<FolderImg>()
        val proj = arrayOf(
            android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        fun walk(docId: String, rel: String) {
            val childUri = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
            val subs = ArrayList<Pair<String, String>>()
            ctx.contentResolver.query(childUri, proj, null, null, null)?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getString(0) ?: continue
                    val nm = c.getString(1) ?: continue
                    val path = if (rel.isEmpty()) nm else "$rel/$nm"
                    if (c.getString(2) == android.provider.DocumentsContract.Document.MIME_TYPE_DIR) subs.add(Pair(id, path))
                    else if (nm.substringAfterLast('.', "").lowercase() in IMAGE_EXT) {
                        out.add(FolderImg(path, android.provider.DocumentsContract.buildDocumentUriUsingTree(tree, id)))
                    }
                }
            }
            for ((id, path) in subs) walk(id, path)
        }
        walk(listRootId(tree), "")
        return out.sortedBy { it.path.lowercase() }
    }

    /** Lists the images of a folder (and its sub-folders) in the background; [openFirst] opens the first one when the list is ready. */
    fun loadFolder(ctx: Context, tree: Uri, openFirst: Boolean) {
        folderLoading = true
        folderName = try { androidx.documentfile.provider.DocumentFile.fromTreeUri(ctx, tree)?.name ?: "" } catch (e: Exception) { "" }
        Engine.scope.launch {
            val l = try { walkImages(ctx, tree) } catch (e: Throwable) { emptyList() }
            folder = l
            folderIndex = -1
            folderLoading = false
            if (openFirst && l.isNotEmpty() && bitmap == null) openIndex(ctx, 0)
        }
    }

    fun openIndex(ctx: Context, i: Int): Boolean {
        val f = folder.getOrNull(i) ?: return false
        val ok = open(ctx, f.uri, f.path.substringAfterLast('/'))
        if (ok) folderIndex = i
        return ok
    }

    /** Writes the picture back to the file it was opened from, in that file's own format. */
    fun saveInPlace(ctx: Context): Boolean {
        val src = sourceUri ?: return false
        val f = formatOf(name) ?: FMT_PNG
        val ok = write(ctx, src, f, 92, true)
        if (ok) markSaved()
        return ok
    }

    // ---- Retouching ----

    private fun mix(a: Int, b: Int, t: Float): Int {
        fun ch(sh: Int): Int = (((a ushr sh) and 255) * (1f - t) + ((b ushr sh) and 255) * t).toInt().coerceIn(0, 255)
        return (ch(24) shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    private fun avgArea(b: Bitmap, x0: Int, y0: Int, x1: Int, y1: Int): Int? {
        val l = maxOf(0, x0); val t = maxOf(0, y0); val r = minOf(b.width, x1); val bt = minOf(b.height, y1)
        if (r <= l || bt <= t) return null
        val w = r - l; val h = bt - t
        val px = IntArray(w * h)
        b.getPixels(px, 0, w, l, t, w, h)
        var a = 0L; var rr = 0L; var g = 0L; var bl = 0L
        for (p in px) { a += p ushr 24; rr += (p shr 16) and 255; g += (p shr 8) and 255; bl += p and 255 }
        val n = px.size
        return ((a / n).toInt() shl 24) or ((rr / n).toInt() shl 16) or ((g / n).toInt() shl 8) or (bl / n).toInt()
    }

    /**
     * Fills the rectangle from the colors just outside it (a thin ring on each side): between the top and bottom edge and the
     * left and right edge, so a flat or smoothly shaded background gets back without the old text.
     */
    fun autoFill(r: Rect) {
        val b = bitmap ?: return
        val c = clamp(r, b) ?: return
        val ring = 3
        val top = avgArea(b, c.left, c.top - ring, c.right, c.top)
        val bottom = avgArea(b, c.left, c.bottom, c.right, c.bottom + ring)
        val left = avgArea(b, c.left - ring, c.top, c.left, c.bottom)
        val right = avgArea(b, c.right, c.top, c.right + ring, c.bottom)
        if (top == null && bottom == null && left == null && right == null) return
        pushCopy()
        val w = c.width(); val h = c.height()
        val row = IntArray(w)
        for (y in 0 until h) {
            val ty = if (h > 1) y / (h - 1f) else 0f
            val v = if (top != null && bottom != null) mix(top, bottom, ty) else (top ?: bottom)
            for (x in 0 until w) {
                val tx = if (w > 1) x / (w - 1f) else 0f
                val hz = if (left != null && right != null) mix(left, right, tx) else (left ?: right)
                row[x] = if (v != null && hz != null) mix(v, hz, 0.5f) else (v ?: hz ?: 0)
            }
            b.setPixels(row, 0, w, c.left, c.top + y, w, 1)
        }
        version++
    }

    /** Blurs ([pixelate] = false) or pixelates the rectangle; [k] is the strength (2..60). */
    fun blur(r: Rect, k: Int, pixelate: Boolean) {
        val b = bitmap ?: return
        val c = clamp(r, b) ?: return
        val f = k.coerceIn(2, 60)
        pushCopy()
        val region = Bitmap.createBitmap(b, c.left, c.top, c.width(), c.height())
        val small = Bitmap.createScaledBitmap(region, maxOf(1, c.width() / f), maxOf(1, c.height() / f), true)
        val big = Bitmap.createScaledBitmap(small, c.width(), c.height(), !pixelate)
        val p = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC) }
        Canvas(b).drawBitmap(big, c.left.toFloat(), c.top.toFloat(), p)
        version++
    }

    /** Clone stamp: paints round dabs along [pts] copied from the same picture shifted by ([dx], [dy]) (source minus start). */
    fun cloneStroke(pts: List<PointF>, dx: Float, dy: Float, size: Float) {
        val b = bitmap ?: return
        if (pts.isEmpty()) return
        pushCopy()
        val src = b.copy(Bitmap.Config.ARGB_8888, false)
        val cv = Canvas(b)
        val radius = size / 2f
        val step = maxOf(1f, radius / 2f)
        val paint = Paint().apply { isAntiAlias = true }
        // a dab is the copy of the source shifted by the offset: draw the source at (-offset) so that the pixel at q+offset lands on q
        fun dab2(x: Float, y: Float) {
            cv.save()
            val path = android.graphics.Path().apply { addCircle(x, y, radius, android.graphics.Path.Direction.CW) }
            cv.clipPath(path)
            cv.drawBitmap(src, -dx, -dy, paint)
            cv.restore()
        }
        var last: PointF? = null
        for (p in pts) {
            val l = last
            if (l == null) dab2(p.x, p.y) else {
                val dist = Math.hypot((p.x - l.x).toDouble(), (p.y - l.y).toDouble()).toFloat()
                val n = maxOf(1, (dist / step).toInt())
                for (i in 1..n) dab2(l.x + (p.x - l.x) * i / n, l.y + (p.y - l.y) * i / n)
            }
            last = p
        }
        version++
    }

    // ---- Text presets ----

    class TextPreset(val name: String, val size: Float, val outline: Float, val bold: Boolean, val font: Int, val color: Int, val color2: Int)

    var presets by mutableStateOf<List<TextPreset>>(emptyList())
        private set
    private var presetsLoaded = false

    private fun prefs() = Engine.app.getSharedPreferences("imageedit", Context.MODE_PRIVATE)

    fun loadPresets() {
        if (presetsLoaded) return
        presetsLoaded = true
        presets = try {
            val a = JSONArray(prefs().getString("presets", "[]") ?: "[]")
            (0 until a.length()).map { i ->
                val o = a.getJSONObject(i)
                TextPreset(o.getString("name"), o.optDouble("size", 48.0).toFloat(), o.optDouble("outline", 0.0).toFloat(), o.optBoolean("bold"), o.optInt("font"), o.optInt("color", 0xFF000000.toInt()), o.optInt("color2", 0xFFFFFFFF.toInt()))
            }
        } catch (e: Exception) { emptyList() }
    }

    private fun savePresets() {
        val a = JSONArray()
        for (p in presets) a.put(JSONObject().put("name", p.name).put("size", p.size.toDouble()).put("outline", p.outline.toDouble()).put("bold", p.bold).put("font", p.font).put("color", p.color).put("color2", p.color2))
        prefs().edit().putString("presets", a.toString()).apply()
    }

    /** Adds a preset (a preset with the same name is replaced). */
    fun addPreset(p: TextPreset) {
        loadPresets()
        presets = presets.filter { it.name != p.name } + p
        savePresets()
    }

    fun removePreset(name: String) {
        loadPresets()
        presets = presets.filter { it.name != name }
        savePresets()
    }
}
