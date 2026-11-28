package com.furuarc.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.furuarc.core.BrushSpec
import com.furuarc.core.Brushes
import com.furuarc.core.Doc
import com.furuarc.core.ImgRender
import com.furuarc.core.ImgStore
import com.furuarc.core.LKind
import com.furuarc.core.Layer
import com.furuarc.core.Stroker
import com.furuarc.i18n.ImageEditStr
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal enum class ETool { SELECT, DRAW, TEXT, SHAPE, CROP, PIPET }

/** The document that is open in the editor (null = the home page of the Image Editor is shown). */
object ImgSession {
    var doc by mutableStateOf<Doc?>(null)
    internal var backHook: (() -> Boolean)? = null

    fun close(ctx: Context) {
        doc?.let { ImgStore.save(ctx, it) }
        doc = null
        backHook = null
    }

    /** Hardware Back: returns true when the screen itself may be left. */
    fun tryExit(ctx: Context): Boolean {
        if (backHook?.invoke() == true) return false
        if (doc != null) { close(ctx); return false }
        return true
    }
}

/** State and touch logic of one open document. */
internal class EdState(val doc: Doc, val ctx: Context, val e: ImageEditStr, val density: Float) {
    var tool by mutableStateOf(ETool.SELECT)
    private var prevTool = ETool.DRAW
    val brush = BrushSpec()
    var brushRev by mutableStateOf(0)

    var scale by mutableStateOf(1f)
    var ox by mutableStateOf(0f)
    var oy by mutableStateOf(0f)
    var viewW = 0
    var viewH = 0
    private var fitted = false

    var stroker: Stroker? = null
    var strokeTick by mutableStateOf(0)
    var cursor by mutableStateOf<Offset?>(null)
    var sizePreview by mutableStateOf(false)
    var guideX by mutableStateOf<Float?>(null)
    var guideY by mutableStateOf<Float?>(null)
    var showGrid by mutableStateOf(false)
    var snap by mutableStateOf(true)

    /** 0 none, 1 layers, 2 more, 3 history */
    var sheet by mutableStateOf(0)

    // dialogs
    var textDlg by mutableStateOf(false)
    var exportDlg by mutableStateOf(false)
    var renameDlg by mutableStateOf(false)
    var canvasDlg by mutableStateOf(0)
    var colorDlg by mutableStateOf(0)
    var flattenDlg by mutableStateOf(false)
    var layerRename by mutableStateOf<Long?>(null)

    // crop
    var cL by mutableStateOf(0f)
    var cT by mutableStateOf(0f)
    var cR by mutableStateOf(0f)
    var cB by mutableStateOf(0f)
    var cropRatio by mutableStateOf(0)

    init {
        val pr = ImgStore.prefs(ctx)
        brush.type = pr.getInt("b_type", Brushes.BRUSH)
        Brushes.preset(brush, brush.type)
        brush.size = pr.getFloat("b_size", brush.size); brush.opacity = pr.getFloat("b_op", brush.opacity)
        brush.flow = pr.getFloat("b_flow", brush.flow); brush.hardness = pr.getFloat("b_hard", brush.hardness)
        brush.smooth = pr.getFloat("b_smooth", brush.smooth); brush.color = pr.getInt("b_color", brush.color)
        snap = pr.getBoolean("snap", true)
        showGrid = pr.getBoolean("grid", false)
    }

    fun savePrefs() {
        ImgStore.prefs(ctx).edit().putInt("b_type", brush.type).putFloat("b_size", brush.size).putFloat("b_op", brush.opacity)
            .putFloat("b_flow", brush.flow).putFloat("b_hard", brush.hardness).putFloat("b_smooth", brush.smooth).putInt("b_color", brush.color)
            .putBoolean("snap", snap).putBoolean("grid", showGrid).apply()
    }

    fun toast(s: String) = Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show()

    /** Runs a step that may need a lot of memory; on failure the user gets a message instead of a crash. */
    fun safe(block: () -> Unit) {
        try { block() } catch (x: OutOfMemoryError) { toast(e.noMemory) }
    }

    // ---------- view ----------
    fun toDoc(sx: Float, sy: Float) = Offset((sx - ox) / scale, (sy - oy) / scale)
    fun toScreen(x: Float, y: Float) = Offset(x * scale + ox, y * scale + oy)

    fun fit() {
        if (viewW <= 0 || viewH <= 0) return
        val m = 20f * density
        scale = max(0.01f, min((viewW - 2 * m) / doc.w, (viewH - 2 * m) / doc.h))
        ox = (viewW - doc.w * scale) / 2f
        oy = (viewH - doc.h * scale) / 2f
        fitted = true
    }

    fun viewSized(w: Int, h: Int) {
        viewW = w; viewH = h
        if (!fitted) fit()
    }

    fun pinch(prev: Offset, cur: Offset, zoom: Float) {
        val ns = (scale * zoom).coerceIn(0.02f, 48f)
        val dx = (prev.x - ox) / scale
        val dy = (prev.y - oy) / scale
        scale = ns
        ox = cur.x - dx * ns
        oy = cur.y - dy * ns
    }

    // ---------- tool switching ----------
    fun pick(t: ETool) {
        if (t == tool) return
        if (tool == ETool.DRAW) savePrefs()
        if (t == ETool.PIPET) prevTool = tool
        tool = t
        sheet = 0
        if (t == ETool.CROP) initCrop()
        if (t == ETool.TEXT) {
            val a = doc.active()
            if (a?.kind != LKind.TEXT) { safe { doc.addText(e.layerText); doc.commit(e.stepNames["add"] ?: "+"); textDlg = true } }
        }
    }

    fun brushChanged() { brushRev++ }

    // ---------- touch ----------
    private var mode = 0 // 0 none, 1 move layer, 2 scale/rotate handle
    private var dragLayer: Layer? = null
    private var downX = 0f
    private var downY = 0f
    private var downSx = 0f
    private var downSy = 0f
    private var startCx = 0f
    private var startCy = 0f
    private var startSx = 1f
    private var startSy = 1f
    private var startRot = 0f
    private var startAng = 0f
    private var startLen = 1f
    private var moved = false
    private var wasActive = false

    private fun nearHandle(l: Layer, sx: Float, sy: Float): Boolean {
        if (l.kind == LKind.FILL) return false
        val c = ImgRender.corners(l, doc)
        val p = toScreen(c[4], c[5])
        return hypot(p.x - sx, p.y - sy) < 34f * density
    }

    fun down(sx: Float, sy: Float) {
        cursor = Offset(sx, sy)
        when (tool) {
            ETool.DRAW -> drawDown(sx, sy)
            ETool.PIPET -> pipetAt(sx, sy)
            ETool.CROP -> cropDown(sx, sy)
            else -> selDown(sx, sy)
        }
    }

    fun move(sx: Float, sy: Float) {
        cursor = Offset(sx, sy)
        when (tool) {
            ETool.DRAW -> drawMove(sx, sy)
            ETool.PIPET -> pipetAt(sx, sy)
            ETool.CROP -> cropMove(sx, sy)
            else -> selMove(sx, sy)
        }
    }

    fun up() {
        when (tool) {
            ETool.DRAW -> drawUp()
            ETool.PIPET -> { tool = prevTool }
            ETool.CROP -> cropCode = 0
            else -> selUp()
        }
        cursor = null
    }

    /** A second finger came down: the stroke or drag in progress is dropped. */
    fun cancel() {
        stroker?.release(); stroker = null
        val l = dragLayer
        if (l != null && moved) {
            // put the layer back where it was
            l.cx = startCx; l.cy = startCy; l.sx = startSx; l.sy = startSy; l.rot = startRot
            doc.touch()
        }
        dragLayer = null; mode = 0; moved = false; guideX = null; guideY = null; cropCode = 0; cursor = null
        strokeTick++
    }

    // ----- select / text / shape -----
    private fun selDown(sx: Float, sy: Float) {
        val d = toDoc(sx, sy)
        downX = d.x; downY = d.y; downSx = sx; downSy = sy
        moved = false
        val act = doc.active()
        if (act != null && !act.locked && act.visible && nearHandle(act, sx, sy)) {
            dragLayer = act; mode = 2; wasActive = true
            startCx = act.cx; startCy = act.cy; startSx = act.sx; startSy = act.sy; startRot = act.rot
            startLen = max(1f, hypot(d.x - act.cx, d.y - act.cy))
            startAng = Math.toDegrees(atan2((d.y - act.cy).toDouble(), (d.x - act.cx).toDouble())).toFloat()
            return
        }
        var target: Layer? = null
        if (act != null && act.visible && ImgRender.hit(act, doc, d.x, d.y)) target = act
        else for (i in doc.layers.indices.reversed()) {
            val l = doc.layers[i]
            if (l.visible && ImgRender.hit(l, doc, d.x, d.y)) { target = l; break }
        }
        if (target == null) { dragLayer = null; mode = 0; return }
        wasActive = target.id == doc.activeId
        if (!wasActive) { doc.activeId = target.id; doc.touch() }
        if (target.locked) { toast(e.lockedLayer); dragLayer = null; mode = 0; return }
        dragLayer = target; mode = 1
        startCx = target.cx; startCy = target.cy; startSx = target.sx; startSy = target.sy; startRot = target.rot
    }

    private fun selMove(sx: Float, sy: Float) {
        val l = dragLayer ?: return
        val d = toDoc(sx, sy)
        if (!moved && hypot(sx - downSx, sy - downSy) < 6f * density) return
        moved = true
        if (mode == 1) {
            l.cx = startCx + (d.x - downX)
            l.cy = startCy + (d.y - downY)
            guideX = null; guideY = null
            if (snap) {
                val c = ImgRender.corners(l, doc)
                val xs = floatArrayOf(min(min(c[0], c[2]), min(c[4], c[6])), 0f, max(max(c[0], c[2]), max(c[4], c[6])))
                xs[1] = (xs[0] + xs[2]) / 2f
                val ys = floatArrayOf(min(min(c[1], c[3]), min(c[5], c[7])), 0f, max(max(c[1], c[3]), max(c[5], c[7])))
                ys[1] = (ys[0] + ys[2]) / 2f
                val th = 8f * density / scale
                var bx: Float? = null
                for (v in xs) for (t in floatArrayOf(0f, doc.w / 2f, doc.w.toFloat())) {
                    val df = t - v
                    if (abs(df) < th && (bx == null || abs(df) < abs(bx))) { bx = df; guideX = t }
                }
                var by: Float? = null
                for (v in ys) for (t in floatArrayOf(0f, doc.h / 2f, doc.h.toFloat())) {
                    val df = t - v
                    if (abs(df) < th && (by == null || abs(df) < abs(by))) { by = df; guideY = t }
                }
                if (bx != null) l.cx += bx
                if (by != null) l.cy += by
            }
        } else if (mode == 2) {
            val len = max(1f, hypot(d.x - l.cx, d.y - l.cy))
            val f = len / startLen
            l.sx = startSx * f; l.sy = startSy * f
            val ang = Math.toDegrees(atan2((d.y - l.cy).toDouble(), (d.x - l.cx).toDouble())).toFloat()
            var r = startRot + (ang - startAng)
            if (snap) { val k = (r / 45f).roundToInt() * 45f; if (abs(r - k) < 3f) r = k }
            l.rot = r
        }
        doc.touch()
    }

    private fun selUp() {
        val l = dragLayer
        if (l != null && moved) doc.commit(e.stepNames["move"] ?: "~")
        else if (l != null && !moved && wasActive && l.kind == LKind.TEXT && mode == 1) textDlg = true
        guideX = null; guideY = null
        dragLayer = null; mode = 0; moved = false
    }

    // ----- drawing -----
    private fun drawDown(sx: Float, sy: Float) {
        val d = toDoc(sx, sy)
        var l = doc.active()
        if (l != null && l.hasBitmap && l.locked) { toast(e.lockedLayer); return }
        if (l == null || !l.hasBitmap) {
            if (l != null && l.locked) { toast(e.lockedLayer); return }
            try { l = doc.addDraw(e.layerDraw); doc.commit(e.stepNames["add"] ?: "+") } catch (x: OutOfMemoryError) { toast(e.noMemory); return }
        }
        val lay = l ?: return
        if (!lay.visible) { lay.visible = true; doc.touch() }
        try {
            val s = Stroker(lay, doc, brush)
            s.begin(d.x, d.y)
            stroker = s
            strokeTick++
        } catch (x: OutOfMemoryError) { toast(e.noMemory) }
    }

    private fun drawMove(sx: Float, sy: Float) {
        val s = stroker ?: return
        val d = toDoc(sx, sy)
        s.move(d.x, d.y)
        strokeTick++
    }

    private fun drawUp() {
        val s = stroker ?: return
        stroker = null
        val l = doc.active()
        if (l == null) { s.release(); return }
        try {
            val nb = s.finish()
            l.bmp = nb
            doc.commit(if (s.erase) (e.stepNames["erase"] ?: "-") else (e.stepNames["brush"] ?: "*"))
        } catch (x: OutOfMemoryError) { s.release(); toast(e.noMemory) }
        strokeTick++
    }

    // ----- eyedropper -----
    private fun pipetAt(sx: Float, sy: Float) {
        val d = toDoc(sx, sy)
        val c = ImgRender.sample(doc, d.x, d.y)
        if ((c ushr 24) > 0) { brush.color = c or 0xFF000000.toInt(); brushRev++ }
    }

    // ----- crop -----
    private var cropCode = 0
    private var csL = 0f
    private var csT = 0f
    private var csR = 0f
    private var csB = 0f

    fun initCrop() { cL = 0f; cT = 0f; cR = doc.w.toFloat(); cB = doc.h.toFloat(); cropRatio = 0 }

    private fun ratioValue(): Float {
        val portrait = doc.h > doc.w
        return when (cropRatio) {
            1 -> doc.w / doc.h.toFloat()
            2 -> 1f
            3 -> if (portrait) 3f / 4f else 4f / 3f
            4 -> if (portrait) 2f / 3f else 3f / 2f
            5 -> if (portrait) 9f / 16f else 16f / 9f
            6 -> 9f / 16f
            else -> 0f
        }
    }

    fun pickRatio(i: Int) {
        cropRatio = i
        val r = ratioValue()
        if (r <= 0f) return
        var w = doc.w.toFloat(); var h = w / r
        if (h > doc.h) { h = doc.h.toFloat(); w = h * r }
        cL = (doc.w - w) / 2f; cR = cL + w; cT = (doc.h - h) / 2f; cB = cT + h
    }

    private fun cropDown(sx: Float, sy: Float) {
        val d = toDoc(sx, sy)
        downX = d.x; downY = d.y
        csL = cL; csT = cT; csR = cR; csB = cB
        val a = toScreen(cL, cT); val b = toScreen(cR, cB)
        val tol = 30f * density
        val inX = sx > a.x - tol && sx < b.x + tol
        val inY = sy > a.y - tol && sy < b.y + tol
        var code = 0
        if (inY && abs(sx - a.x) < tol) code = code or 1
        if (inY && abs(sx - b.x) < tol) code = code or 4
        if (inX && abs(sy - a.y) < tol) code = code or 2
        if (inX && abs(sy - b.y) < tol) code = code or 8
        if (code == 0 && sx > a.x && sx < b.x && sy > a.y && sy < b.y) code = 16
        cropCode = code
    }

    private fun cropMove(sx: Float, sy: Float) {
        if (cropCode == 0) return
        val d = toDoc(sx, sy)
        val dx = d.x - downX; val dy = d.y - downY
        val W = doc.w.toFloat(); val H = doc.h.toFloat()
        val minS = 16f
        if (cropCode == 16) {
            val w = csR - csL; val h = csB - csT
            cL = (csL + dx).coerceIn(0f, W - w); cR = cL + w
            cT = (csT + dy).coerceIn(0f, H - h); cB = cT + h
            return
        }
        var nl = csL; var nt = csT; var nr = csR; var nb = csB
        if (cropCode and 1 != 0) nl = (csL + dx).coerceIn(0f, csR - minS)
        if (cropCode and 4 != 0) nr = (csR + dx).coerceIn(csL + minS, W)
        if (cropCode and 2 != 0) nt = (csT + dy).coerceIn(0f, csB - minS)
        if (cropCode and 8 != 0) nb = (csB + dy).coerceIn(csT + minS, H)
        val r = ratioValue()
        if (r > 0f) {
            val horiz = cropCode and 5 != 0
            val vert = cropCode and 10 != 0
            if (horiz && vert) {
                val w = nr - nl; val h = w / r
                if (cropCode and 2 != 0) nt = nb - h else nb = nt + h
            } else if (horiz) {
                val h = (nr - nl) / r; val cy = (csT + csB) / 2f
                nt = cy - h / 2f; nb = cy + h / 2f
            } else {
                val w = (nb - nt) * r; val cx = (csL + csR) / 2f
                nl = cx - w / 2f; nr = cx + w / 2f
            }
            if (nl < 0f || nt < 0f || nr > W || nb > H || nr - nl < minS || nb - nt < minS) return
        }
        cL = nl; cT = nt; cR = nr; cB = nb
    }

    fun applyCrop() {
        val l = cL.roundToInt(); val t = cT.roundToInt()
        val r = cR.roundToInt(); val b = cB.roundToInt()
        if (r - l < 1 || b - t < 1) return
        doc.cropTo(l, t, r, b)
        doc.commit(e.stepNames["crop"] ?: "crop")
        tool = ETool.SELECT
        fit()
    }

    // ---------- document operations ----------
    fun step(key: String, block: () -> Unit) {
        safe { block(); doc.commit(e.stepNames[key] ?: key) }
    }

    fun rotate(cw: Boolean) { step("rotate") { doc.rotate90(cw) }; fit() }
    fun flip(h: Boolean) { step("flip") { doc.flip(h) } }

    fun addImage(uri: Uri) {
        safe {
            val b = ImgStore.decode(ctx, uri, Doc.MAX_SIDE)
            if (b == null) { toast(e.loadFail); return@safe }
            doc.addBitmap(LKind.IMAGE, b, ImgStore.displayName(ctx, uri).substringBeforeLast('.'))
            doc.commit(e.stepNames["add"] ?: "+")
            tool = ETool.SELECT
        }
    }

    fun deleteActive() {
        val l = doc.active() ?: return
        if (l.locked) { toast(e.lockedLayer); return }
        step("del") { doc.remove(l) }
    }

    fun layerBitmapOf(l: Layer): Bitmap? = l.bmp
}
