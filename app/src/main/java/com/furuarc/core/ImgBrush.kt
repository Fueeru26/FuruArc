package com.furuarc.core

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt

/** Brush settings. [type] is one of the constants of [Brushes]. */
class BrushSpec {
    var type = Brushes.BRUSH
    /** diameter in document pixels */
    var size = 24f
    var opacity = 1f
    var flow = 1f
    var hardness = 0.8f
    var color = 0xFF000000.toInt()
    var smooth = 0.3f
}

object Brushes {
    const val BRUSH = 0
    const val PENCIL = 1
    const val MARKER = 2
    const val HIGHLIGHT = 3
    const val SOFT = 4
    const val HARD = 5
    const val AIR = 6
    const val ERASER = 7
    const val COUNT = 8

    /** Applies the preset of [type] to [s] (colour stays). */
    fun preset(s: BrushSpec, type: Int) {
        s.type = type
        when (type) {
            BRUSH -> { s.size = 24f; s.opacity = 1f; s.flow = 1f; s.hardness = 0.8f; s.smooth = 0.3f }
            PENCIL -> { s.size = 4f; s.opacity = 1f; s.flow = 1f; s.hardness = 1f; s.smooth = 0.2f }
            MARKER -> { s.size = 32f; s.opacity = 0.9f; s.flow = 1f; s.hardness = 0.9f; s.smooth = 0.3f }
            HIGHLIGHT -> { s.size = 44f; s.opacity = 0.4f; s.flow = 1f; s.hardness = 1f; s.smooth = 0.4f }
            SOFT -> { s.size = 60f; s.opacity = 1f; s.flow = 0.6f; s.hardness = 0.1f; s.smooth = 0.3f }
            HARD -> { s.size = 30f; s.opacity = 1f; s.flow = 1f; s.hardness = 1f; s.smooth = 0.3f }
            AIR -> { s.size = 70f; s.opacity = 1f; s.flow = 0.12f; s.hardness = 0f; s.smooth = 0.3f }
            ERASER -> { s.size = 40f; s.opacity = 1f; s.flow = 1f; s.hardness = 0.8f; s.smooth = 0.3f }
        }
    }
}

/**
 * Paints one stroke on a bitmap layer. The stroke is drawn into a temporary bitmap of the layer's size (so the opacity
 * of the whole stroke is applied once and overlapping dabs do not add up) and [finish] merges it into a copy of the
 * layer bitmap, which becomes the new bitmap of the layer (copy-on-write: older history steps keep the old one).
 */
class Stroker(private val layer: Layer, doc: Doc, private val spec: BrushSpec) {
    private val src: Bitmap = layer.bmp!!
    private val tmp: Bitmap = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(tmp)
    private val inv = Matrix()
    val erase = spec.type == Brushes.ERASER
    private val radius: Float
    private val step: Float
    private val color = if (erase) 0xFF000000.toInt() else spec.color or 0xFF000000.toInt()
    private val square = spec.type == Brushes.HIGHLIGHT
    private val dab: Bitmap?
    private val dabSize: Int
    private val dabPaint = Paint(Paint.FILTER_BITMAP_FLAG)
    private val solid = Paint(Paint.ANTI_ALIAS_FLAG)
    private var fx = 0f
    private var fy = 0f
    private var lx = 0f
    private var ly = 0f
    private var nextAt = 0f
    private var started = false

    init {
        ImgRender.matrix(layer, doc).invert(inv)
        val sc = max(0.05f, (abs(layer.sx) + abs(layer.sy)) / 2f)
        radius = max(0.5f, spec.size / sc / 2f)
        step = max(0.75f, radius * 2f * 0.12f)
        val a = (spec.flow * 255f).roundToInt().coerceIn(1, 255)
        solid.color = color
        solid.alpha = a
        dabPaint.alpha = a
        if (spec.hardness < 0.98f && !square) {
            dabSize = ceil(radius * 2f).toInt() + 2
            val b = Bitmap.createBitmap(dabSize, dabSize, Bitmap.Config.ARGB_8888)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            val c0 = color or 0xFF000000.toInt()
            val c1 = color and 0x00FFFFFF
            val h = spec.hardness.coerceIn(0.01f, 0.97f)
            p.shader = RadialGradient(dabSize / 2f, dabSize / 2f, dabSize / 2f, intArrayOf(c0, c0, c1), floatArrayOf(0f, h, 1f), Shader.TileMode.CLAMP)
            Canvas(b).drawCircle(dabSize / 2f, dabSize / 2f, dabSize / 2f, p)
            dab = b
        } else { dab = null; dabSize = 0 }
    }

    private fun local(x: Float, y: Float): FloatArray { val p = floatArrayOf(x, y); inv.mapPoints(p); return p }

    private fun stamp(x: Float, y: Float) {
        if (dab != null) canvas.drawBitmap(dab, x - dabSize / 2f, y - dabSize / 2f, dabPaint)
        else if (square) canvas.drawRect(RectF(x - radius, y - radius, x + radius, y + radius), solid)
        else canvas.drawCircle(x, y, radius, solid)
    }

    /** [x], [y] in document coordinates. */
    fun begin(x: Float, y: Float) {
        fx = x; fy = y
        val l = local(x, y)
        lx = l[0]; ly = l[1]
        stamp(lx, ly)
        nextAt = step
        started = true
    }

    fun move(x: Float, y: Float) {
        if (!started) { begin(x, y); return }
        val k = 1f - spec.smooth.coerceIn(0f, 0.95f) * 0.85f
        fx += (x - fx) * k; fy += (y - fy) * k
        val l = local(fx, fy)
        val dx = l[0] - lx; val dy = l[1] - ly
        val len = hypot(dx, dy)
        if (len <= 0f) return
        var s = nextAt
        while (s <= len) { stamp(lx + dx * s / len, ly + dy * s / len); s += step }
        nextAt = s - len
        lx = l[0]; ly = l[1]
    }

    fun overlay() = ImgRender.Stroke(layer.id, tmp, spec.opacity, erase)

    /** Merges the stroke into a copy of the bitmap and returns it. */
    fun finish(): Bitmap {
        val nb = src.copy(Bitmap.Config.ARGB_8888, true)
        val p = Paint(Paint.FILTER_BITMAP_FLAG)
        p.alpha = (spec.opacity * 255f).roundToInt().coerceIn(0, 255)
        if (erase) p.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        Canvas(nb).drawBitmap(tmp, 0f, 0f, p)
        release()
        return nb
    }

    fun release() { try { tmp.recycle(); dab?.recycle() } catch (e: Exception) { } }
}
