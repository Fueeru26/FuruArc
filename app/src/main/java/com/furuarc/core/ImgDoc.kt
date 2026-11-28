package com.furuarc.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.util.IdentityHashMap
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/*
 * Layered document model of the Image Editor.
 *
 * - A [Doc] is a canvas size, a background colour and a list of [Layer]s (bottom to top).
 * - Every layer keeps its own transform in document coordinates (centre, scale, rotation), so moving, scaling,
 *   rotating, cropping or flipping the canvas never touches the pixels (non-destructive).
 * - Bitmaps are treated as immutable once they are part of the history: a brush stroke makes a copy first
 *   (copy-on-write), so a history step is only a list of layers that share the unchanged bitmaps.
 */

enum class LKind { IMAGE, DRAW, TEXT, SHAPE, FILL }

/** Text layer settings. Sizes are in document pixels. */
class TextSpec {
    var text = "Teks"
    var size = 72f
    var color = 0xFF000000.toInt()
    /** sans, serif, mono, cursive, casual */
    var font = "sans"
    /** path of a font file copied into the app (overrides [font]) */
    var fontPath = ""
    var bold = false
    var italic = false
    var underline = false
    /** 0 left, 1 centre, 2 right */
    var align = 1
    var lineSpacing = 1f
    /** in em */
    var letterSpacing = 0f
    var strokeW = 0f
    var strokeColor = 0xFFFFFFFF.toInt()
    var shadow = false
    var shadowColor = 0x99000000.toInt()
    var bgOn = false
    var bgColor = 0xFFFFFFFF.toInt()
    var boxW = 800f

    fun copy(): TextSpec {
        val t = TextSpec()
        t.text = text; t.size = size; t.color = color; t.font = font; t.fontPath = fontPath; t.bold = bold; t.italic = italic
        t.underline = underline; t.align = align; t.lineSpacing = lineSpacing; t.letterSpacing = letterSpacing
        t.strokeW = strokeW; t.strokeColor = strokeColor; t.shadow = shadow; t.shadowColor = shadowColor
        t.bgOn = bgOn; t.bgColor = bgColor; t.boxW = boxW
        return t
    }

    fun sig() = "$text|$size|$color|$font|$fontPath|$bold|$italic|$underline|$align|$lineSpacing|$letterSpacing|$strokeW|$strokeColor|$shadow|$shadowColor|$bgOn|$bgColor|$boxW"
}

/** Shape layer settings. kind: 0 rectangle, 1 rounded rectangle, 2 ellipse, 3 line, 4 arrow, 5 polygon. */
class ShapeSpec {
    var kind = 0
    var w = 400f
    var h = 300f
    var fillOn = true
    var fill = 0xFF1E88E5.toInt()
    var stroke = 0xFF000000.toInt()
    var strokeW = 0f
    var radius = 48f
    var sides = 5

    fun copy(): ShapeSpec {
        val s = ShapeSpec()
        s.kind = kind; s.w = w; s.h = h; s.fillOn = fillOn; s.fill = fill; s.stroke = stroke; s.strokeW = strokeW; s.radius = radius; s.sides = sides
        return s
    }
}

class Layer(val id: Long, var kind: LKind) {
    var name = ""
    var visible = true
    var locked = false
    var opacity = 1f
    /** 0 normal, 1 multiply, 2 screen, 3 overlay, 4 darken, 5 lighten */
    var blend = 0
    var cx = 0f
    var cy = 0f
    var sx = 1f
    var sy = 1f
    var rot = 0f
    var bmp: Bitmap? = null
    var text: TextSpec? = null
    var shape: ShapeSpec? = null
    var fill = 0xFFFFFFFF.toInt()

    fun copyState(newId: Long = id): Layer {
        val l = Layer(newId, kind)
        l.name = name; l.visible = visible; l.locked = locked; l.opacity = opacity; l.blend = blend
        l.cx = cx; l.cy = cy; l.sx = sx; l.sy = sy; l.rot = rot
        l.bmp = bmp; l.text = text?.copy(); l.shape = shape?.copy(); l.fill = fill
        return l
    }

    val hasBitmap get() = kind == LKind.IMAGE || kind == LKind.DRAW
}

private class Snap(val label: String, val w: Int, val h: Int, val bg: Int, val layers: List<Layer>, val activeId: Long)

class Doc(var w: Int, var h: Int) {
    var id = ""
    var name = ""
    /** background colour; 0 = transparent */
    var bg = 0xFFFFFFFF.toInt()
    val layers = ArrayList<Layer>()
    var activeId = -1L
    var nextId = 1L
    /** bumped on every change so the canvas redraws */
    var rev by mutableStateOf(0)
    var dirty = false
    /** bitmap of each layer as last written to disk (identity compare) */
    val savedBmps = HashMap<Long, Bitmap>()

    private val snaps = ArrayList<Snap>()
    private var idx = -1

    fun newId() = nextId++
    fun active(): Layer? = layers.firstOrNull { it.id == activeId }
    fun touch() { rev++; dirty = true }

    // ---------- history ----------
    fun canUndo() = idx > 0
    fun canRedo() = idx < snaps.size - 1
    val historyLabels: List<String> get() = snaps.map { it.label }
    val historyIndex get() = idx

    /** Records the current state as a new history step. */
    fun commit(label: String) {
        while (snaps.size - 1 > idx) snaps.removeAt(snaps.size - 1)
        snaps.add(Snap(label, w, h, bg, layers.map { it.copyState() }, activeId))
        idx = snaps.size - 1
        trim()
        touch()
    }

    fun undo() { if (canUndo()) { idx--; restore(snaps[idx]) } }
    fun redo() { if (canRedo()) { idx++; restore(snaps[idx]) } }
    fun jump(i: Int) { if (i in 0 until snaps.size && i != idx) { idx = i; restore(snaps[idx]) } }

    private fun restore(s: Snap) {
        w = s.w; h = s.h; bg = s.bg
        layers.clear()
        layers.addAll(s.layers.map { it.copyState() })
        activeId = if (layers.any { it.id == s.activeId }) s.activeId else (layers.lastOrNull()?.id ?: -1L)
        touch()
    }

    /** Keeps the history inside a memory budget (bitmaps shared between steps are counted once). */
    private fun trim() {
        while (snaps.size > MAX_STEPS) { snaps.removeAt(0); idx-- }
        while (idx > 1 && snaps.size > 3 && historyBytes() > BUDGET) { snaps.removeAt(0); idx-- }
    }

    private fun historyBytes(): Long {
        val seen = IdentityHashMap<Bitmap, Boolean>()
        var total = 0L
        for (s in snaps) for (l in s.layers) { val b = l.bmp ?: continue; if (seen.put(b, true) == null) total += b.byteCount.toLong() }
        return total
    }

    /** Clears the history and records the current state as the first step. */
    fun resetHistory(label: String) { snaps.clear(); idx = -1; commit(label) }

    // ---------- layers ----------
    private fun place(l: Layer, name: String, above: Boolean = true) {
        if (l.name.isEmpty()) l.name = name
        val at = layers.indexOfFirst { it.id == activeId }
        if (above && at >= 0) layers.add(at + 1, l) else layers.add(l)
        activeId = l.id
    }

    fun addBitmap(kind: LKind, bmp: Bitmap, name: String): Layer {
        val l = Layer(newId(), kind)
        l.bmp = bmp
        l.cx = w / 2f; l.cy = h / 2f
        if (kind == LKind.IMAGE) {
            // an image bigger than the canvas is fitted in
            val f = min(1f, min(w / bmp.width.toFloat(), h / bmp.height.toFloat()))
            l.sx = f; l.sy = f
        }
        place(l, name)
        return l
    }

    fun addDraw(name: String): Layer {
        val b = Bitmap.createBitmap(max(1, w), max(1, h), Bitmap.Config.ARGB_8888)
        return addBitmap(LKind.DRAW, b, name)
    }

    fun addText(name: String): Layer {
        val l = Layer(newId(), LKind.TEXT)
        l.text = TextSpec().also { it.boxW = (w * 0.7f).coerceAtLeast(200f); it.size = (w / 12f).coerceIn(24f, 200f) }
        l.cx = w / 2f; l.cy = h / 2f
        place(l, name)
        return l
    }

    fun addShape(kind: Int, name: String): Layer {
        val l = Layer(newId(), LKind.SHAPE)
        val s = ShapeSpec()
        s.kind = kind
        val side = (min(w, h) * 0.4f).coerceAtLeast(60f)
        s.w = side; s.h = if (kind == 3 || kind == 4) 40f else side
        if (kind == 3 || kind == 4) { s.w = side * 1.4f; s.strokeW = 10f; s.stroke = 0xFF000000.toInt() }
        l.shape = s
        l.cx = w / 2f; l.cy = h / 2f
        place(l, name)
        return l
    }

    fun addFill(color: Int, name: String): Layer {
        val l = Layer(newId(), LKind.FILL)
        l.fill = color
        place(l, name)
        return l
    }

    fun duplicate(src: Layer, suffix: String): Layer {
        val l = src.copyState(newId())
        l.name = src.name + suffix
        l.cx += 24f; l.cy += 24f
        val at = layers.indexOf(src)
        layers.add(at + 1, l)
        activeId = l.id
        return l
    }

    fun remove(l: Layer) {
        val at = layers.indexOf(l)
        layers.remove(l)
        activeId = layers.getOrNull(min(at, layers.size - 1))?.id ?: -1L
    }

    fun move(l: Layer, up: Boolean) {
        val at = layers.indexOf(l)
        val to = if (up) at + 1 else at - 1
        if (at < 0 || to < 0 || to >= layers.size) return
        layers.removeAt(at); layers.add(to, l)
    }

    /** Merges [l] into the layer below it (the result is a drawing layer the size of the canvas). */
    fun mergeDown(l: Layer, name: String): Boolean {
        val at = layers.indexOf(l)
        if (at <= 0) return false
        val below = layers[at - 1]
        val out = Bitmap.createBitmap(max(1, w), max(1, h), Bitmap.Config.ARGB_8888)
        val c = Canvas(out)
        ImgRender.drawLayer(c, this, below, null)
        ImgRender.drawLayer(c, this, l, null)
        val m = Layer(newId(), LKind.DRAW)
        m.bmp = out; m.name = name; m.cx = w / 2f; m.cy = h / 2f
        layers.removeAt(at); layers.removeAt(at - 1); layers.add(at - 1, m)
        activeId = m.id
        return true
    }

    /** Flattens every visible layer into one image layer (the background colour stays a canvas setting). */
    fun flatten(name: String) {
        val out = Bitmap.createBitmap(max(1, w), max(1, h), Bitmap.Config.ARGB_8888)
        ImgRender.drawDoc(Canvas(out), this, false, null)
        val m = Layer(newId(), LKind.IMAGE)
        m.bmp = out; m.name = name; m.cx = w / 2f; m.cy = h / 2f
        layers.clear(); layers.add(m)
        activeId = m.id
    }

    // ---------- canvas operations (layers only get new transforms) ----------
    fun cropTo(l: Int, t: Int, r: Int, b: Int) {
        for (ly in layers) { ly.cx -= l; ly.cy -= t }
        w = max(1, r - l); h = max(1, b - t)
    }

    fun rotate90(cw: Boolean) {
        val ow = w; val oh = h
        for (ly in layers) {
            val x = ly.cx; val y = ly.cy
            if (cw) { ly.cx = oh - y; ly.cy = x; ly.rot += 90f } else { ly.cx = y; ly.cy = ow - x; ly.rot -= 90f }
        }
        w = oh; h = ow
    }

    fun flip(horizontal: Boolean) {
        for (ly in layers) {
            if (horizontal) { ly.cx = w - ly.cx; ly.sx = -ly.sx } else { ly.cy = h - ly.cy; ly.sy = -ly.sy }
            ly.rot = -ly.rot
        }
    }

    /** Rotates the whole picture by [deg] around the middle of the canvas (straighten). */
    fun straighten(deg: Float) {
        val rad = deg * PI.toFloat() / 180f
        val ccx = w / 2f; val ccy = h / 2f
        for (ly in layers) {
            val dx = ly.cx - ccx; val dy = ly.cy - ccy
            ly.cx = ccx + dx * cos(rad) - dy * sin(rad)
            ly.cy = ccy + dx * sin(rad) + dy * cos(rad)
            ly.rot += deg
        }
    }

    /** Scales the canvas and everything on it by [f]. */
    fun scaleAll(f: Float) {
        for (ly in layers) { ly.cx *= f; ly.cy *= f; ly.sx *= f; ly.sy *= f }
        w = max(1, (w * f).roundToInt()); h = max(1, (h * f).roundToInt())
    }

    /** Changes the canvas size without scaling the content (it stays centred). */
    fun resizeCanvas(nw: Int, nh: Int) {
        val dx = (nw - w) / 2f; val dy = (nh - h) / 2f
        for (ly in layers) { ly.cx += dx; ly.cy += dy }
        w = max(1, nw); h = max(1, nh)
    }

    companion object {
        const val MAX_STEPS = 60
        const val BUDGET = 110L * 1024 * 1024
        const val MAX_SIDE = 3072
    }
}

object ImgRender {
    private val bmpPaint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

    class Stroke(val layerId: Long, val tmp: Bitmap, val opacity: Float, val erase: Boolean)

    fun blendMode(b: Int): PorterDuff.Mode? = when (b) {
        1 -> PorterDuff.Mode.MULTIPLY
        2 -> PorterDuff.Mode.SCREEN
        3 -> PorterDuff.Mode.OVERLAY
        4 -> PorterDuff.Mode.DARKEN
        5 -> PorterDuff.Mode.LIGHTEN
        else -> null
    }

    /** Size of the layer content in its own coordinates. */
    fun size(l: Layer, doc: Doc): PointF = when (l.kind) {
        LKind.IMAGE, LKind.DRAW -> PointF(l.bmp?.width?.toFloat() ?: 1f, l.bmp?.height?.toFloat() ?: 1f)
        LKind.TEXT -> { val b = TextRender.build(l.text!!); PointF(b.w, b.h) }
        LKind.SHAPE -> PointF(l.shape!!.w, l.shape!!.h)
        LKind.FILL -> PointF(doc.w.toFloat(), doc.h.toFloat())
    }

    /** Layer coordinates -> document coordinates. */
    fun matrix(l: Layer, doc: Doc): Matrix {
        val s = size(l, doc)
        val m = Matrix()
        if (l.kind == LKind.FILL) return m
        m.postTranslate(-s.x / 2f, -s.y / 2f)
        m.postScale(l.sx, l.sy)
        m.postRotate(l.rot)
        m.postTranslate(l.cx, l.cy)
        return m
    }

    /** Draws the whole document into [c] (document coordinates). */
    fun drawDoc(c: Canvas, doc: Doc, withBg: Boolean, stroke: Stroke?) {
        val sc = c.saveLayer(0f, 0f, doc.w.toFloat(), doc.h.toFloat(), null)
        if (withBg && doc.bg != 0) {
            val p = Paint(); p.color = doc.bg
            c.drawRect(0f, 0f, doc.w.toFloat(), doc.h.toFloat(), p)
        }
        for (l in doc.layers) if (l.visible) drawLayer(c, doc, l, stroke)
        c.restoreToCount(sc)
    }

    fun drawLayer(c: Canvas, doc: Doc, l: Layer, stroke: Stroke?) {
        val alpha = (l.opacity * 255f).roundToInt().coerceIn(0, 255)
        val mode = blendMode(l.blend)
        var sc = -1
        if (alpha < 255 || mode != null) {
            val lp = Paint()
            lp.alpha = alpha
            if (mode != null) lp.xfermode = PorterDuffXfermode(mode)
            sc = c.saveLayer(0f, 0f, doc.w.toFloat(), doc.h.toFloat(), lp)
        }
        if (l.kind == LKind.FILL) {
            val p = Paint(); p.color = l.fill
            c.drawRect(0f, 0f, doc.w.toFloat(), doc.h.toFloat(), p)
        } else {
            c.save()
            c.concat(matrix(l, doc))
            when (l.kind) {
                LKind.IMAGE, LKind.DRAW -> drawBitmapLayer(c, l, stroke)
                LKind.TEXT -> TextRender.draw(c, l.text!!)
                LKind.SHAPE -> ShapeRender.draw(c, l.shape!!)
                else -> {}
            }
            c.restore()
        }
        if (sc >= 0) c.restoreToCount(sc)
    }

    private fun drawBitmapLayer(c: Canvas, l: Layer, stroke: Stroke?) {
        val b = l.bmp ?: return
        if (stroke != null && stroke.layerId == l.id) {
            if (stroke.erase) {
                val s2 = c.saveLayer(0f, 0f, b.width.toFloat(), b.height.toFloat(), null)
                c.drawBitmap(b, 0f, 0f, bmpPaint)
                val ep = Paint(Paint.FILTER_BITMAP_FLAG)
                ep.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
                ep.alpha = (stroke.opacity * 255f).roundToInt().coerceIn(0, 255)
                c.drawBitmap(stroke.tmp, 0f, 0f, ep)
                c.restoreToCount(s2)
            } else {
                c.drawBitmap(b, 0f, 0f, bmpPaint)
                val ap = Paint(Paint.FILTER_BITMAP_FLAG)
                ap.alpha = (stroke.opacity * 255f).roundToInt().coerceIn(0, 255)
                c.drawBitmap(stroke.tmp, 0f, 0f, ap)
            }
        } else c.drawBitmap(b, 0f, 0f, bmpPaint)
    }

    /** Colour of the finished picture at a document point (the checkerboard is not included). */
    fun sample(doc: Doc, x: Float, y: Float): Int {
        val xi = x.toInt(); val yi = y.toInt()
        if (xi < 0 || yi < 0 || xi >= doc.w || yi >= doc.h) return 0
        val b = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.translate(-xi.toFloat(), -yi.toFloat())
        drawDoc(c, doc, true, null)
        val px = b.getPixel(0, 0)
        b.recycle()
        return px
    }

    /** Corners of the layer in document coordinates (top-left, top-right, bottom-right, bottom-left). */
    fun corners(l: Layer, doc: Doc): FloatArray {
        val s = size(l, doc)
        val pts = floatArrayOf(0f, 0f, s.x, 0f, s.x, s.y, 0f, s.y)
        matrix(l, doc).mapPoints(pts)
        return pts
    }

    /** Is the document point on the layer (for bitmaps: on a visible pixel)? */
    fun hit(l: Layer, doc: Doc, x: Float, y: Float): Boolean {
        if (l.kind == LKind.FILL) return false
        val inv = Matrix()
        if (!matrix(l, doc).invert(inv)) return false
        val p = floatArrayOf(x, y)
        inv.mapPoints(p)
        val s = size(l, doc)
        if (p[0] < 0 || p[1] < 0 || p[0] >= s.x || p[1] >= s.y) return false
        val b = l.bmp
        if (b != null) {
            val px = b.getPixel(p[0].toInt().coerceIn(0, b.width - 1), p[1].toInt().coerceIn(0, b.height - 1))
            return Color.alpha(px) > 10
        }
        return true
    }
}

object TextRender {
    class Built(val fill: StaticLayout, val outline: StaticLayout?, val w: Float, val h: Float, val pad: Float, val spec: TextSpec)

    private val cache = object : LinkedHashMap<String, Built>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Built>?) = size > 24
    }
    private val fontCache = HashMap<String, Typeface?>()

    fun typeface(s: TextSpec): Typeface {
        val style = (if (s.bold) Typeface.BOLD else 0) or (if (s.italic) Typeface.ITALIC else 0)
        var base: Typeface? = null
        if (s.fontPath.isNotEmpty()) {
            base = fontCache.getOrPut(s.fontPath) { try { Typeface.createFromFile(File(s.fontPath)) } catch (e: Exception) { null } }
        }
        if (base == null) base = when (s.font) {
            "serif" -> Typeface.SERIF
            "mono" -> Typeface.MONOSPACE
            "cursive" -> Typeface.create("cursive", Typeface.NORMAL)
            "casual" -> Typeface.create("casual", Typeface.NORMAL)
            else -> Typeface.SANS_SERIF
        }
        return Typeface.create(base, style)
    }

    @Synchronized
    fun build(s: TextSpec): Built {
        val key = s.sig()
        cache[key]?.let { return it }
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        paint.textSize = s.size
        paint.color = s.color
        paint.typeface = typeface(s)
        paint.letterSpacing = s.letterSpacing
        paint.isUnderlineText = s.underline
        if (s.shadow) paint.setShadowLayer(s.size * 0.08f, s.size * 0.05f, s.size * 0.05f, s.shadowColor)
        val width = max(1, s.boxW.toInt())
        val al = when (s.align) { 0 -> Layout.Alignment.ALIGN_NORMAL; 2 -> Layout.Alignment.ALIGN_OPPOSITE; else -> Layout.Alignment.ALIGN_CENTER }
        val txt = if (s.text.isEmpty()) " " else s.text
        val fill = StaticLayout.Builder.obtain(txt, 0, txt.length, paint, width).setAlignment(al).setLineSpacing(0f, s.lineSpacing).setIncludePad(false).build()
        var outline: StaticLayout? = null
        if (s.strokeW > 0f) {
            val sp = TextPaint(Paint.ANTI_ALIAS_FLAG)
            sp.textSize = s.size
            sp.typeface = paint.typeface
            sp.letterSpacing = s.letterSpacing
            sp.style = Paint.Style.STROKE
            sp.strokeWidth = s.strokeW * 2f
            sp.strokeJoin = Paint.Join.ROUND
            sp.color = s.strokeColor
            outline = StaticLayout.Builder.obtain(txt, 0, txt.length, sp, width).setAlignment(al).setLineSpacing(0f, s.lineSpacing).setIncludePad(false).build()
        }
        val pad = s.size * 0.2f + s.strokeW
        val b = Built(fill, outline, width + pad * 2f, fill.height + pad * 2f, pad, s)
        cache[key] = b
        return b
    }

    fun draw(c: Canvas, s: TextSpec) {
        val b = build(s)
        if (s.bgOn) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.color = s.bgColor
            c.drawRoundRect(RectF(0f, 0f, b.w, b.h), s.size * 0.2f, s.size * 0.2f, p)
        }
        c.save()
        c.translate(b.pad, b.pad)
        b.outline?.draw(c)
        b.fill.draw(c)
        c.restore()
    }
}

object ShapeRender {
    fun draw(c: Canvas, s: ShapeSpec) {
        val fp = Paint(Paint.ANTI_ALIAS_FLAG)
        fp.style = Paint.Style.FILL; fp.color = s.fill
        val sw = if ((s.kind == 3 || s.kind == 4) && s.strokeW < 2f) 6f else s.strokeW
        val sp = Paint(Paint.ANTI_ALIAS_FLAG)
        sp.style = Paint.Style.STROKE; sp.color = s.stroke; sp.strokeWidth = sw
        sp.strokeJoin = Paint.Join.ROUND; sp.strokeCap = Paint.Cap.ROUND
        val half = sw / 2f
        val r = RectF(half, half, max(half + 1f, s.w - half), max(half + 1f, s.h - half))
        when (s.kind) {
            0 -> { if (s.fillOn) c.drawRect(r, fp); if (sw > 0f) c.drawRect(r, sp) }
            1 -> {
                val rad = min(s.radius, min(r.width(), r.height()) / 2f)
                if (s.fillOn) c.drawRoundRect(r, rad, rad, fp)
                if (sw > 0f) c.drawRoundRect(r, rad, rad, sp)
            }
            2 -> { if (s.fillOn) c.drawOval(r, fp); if (sw > 0f) c.drawOval(r, sp) }
            3 -> c.drawLine(half, s.h / 2f, s.w - half, s.h / 2f, sp)
            4 -> {
                val head = max(sw * 3.5f, 28f)
                c.drawLine(half, s.h / 2f, s.w - head * 0.8f, s.h / 2f, sp)
                val hp = Paint(Paint.ANTI_ALIAS_FLAG)
                hp.color = s.stroke; hp.style = Paint.Style.FILL
                val path = Path()
                path.moveTo(s.w, s.h / 2f)
                path.lineTo(s.w - head, s.h / 2f - head * 0.5f)
                path.lineTo(s.w - head, s.h / 2f + head * 0.5f)
                path.close()
                c.drawPath(path, hp)
            }
            else -> {
                val n = s.sides.coerceIn(3, 12)
                val path = Path()
                for (i in 0 until n) {
                    val a = (-PI / 2.0 + 2.0 * PI * i / n).toFloat()
                    val x = r.centerX() + r.width() / 2f * cos(a)
                    val y = r.centerY() + r.height() / 2f * sin(a)
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                if (s.fillOn) c.drawPath(path, fp)
                if (sw > 0f) c.drawPath(path, sp)
            }
        }
    }
}
