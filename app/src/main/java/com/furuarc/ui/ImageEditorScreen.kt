package com.furuarc.ui

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Typeface
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.furuarc.core.ImageEditor
import com.furuarc.i18n.imageEditStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class ITool { MOVE, SELECT, BRUSH, TEXT, PICK, CLONE }

private val IMG_PALETTE = intArrayOf(
    0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF808080.toInt(), 0xFFE53935.toInt(), 0xFFFB8C00.toInt(), 0xFFFDD835.toInt(),
    0xFF43A047.toInt(), 0xFF00ACC1.toInt(), 0xFF1E88E5.toInt(), 0xFF8E24AA.toInt(), 0xFFEC407A.toInt(), 0xFF6D4C41.toInt(),
)

private fun hexOf(c: Int) = String.format("%06X", c and 0xFFFFFF)
private fun parseHex(s: String): Int? {
    val t = s.trim().removePrefix("#")
    if (t.length != 6) return null
    return try { (t.toLong(16).toInt()) or 0xFF000000.toInt() } catch (e: Exception) { null }
}

/** Where the image sits on the canvas: top-left corner and scale (screen px per image px). */
private class IView(val left: Float, val top: Float, val s: Float)

/** Image Editor: crop, resize, rotate/flip, selection fill, brush, text with outline and an eyedropper; saves PNG / JPG / WebP. */
@Composable
fun ImageEditorScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val e = imageEditStr(LocalLang.current)
    val ctx = LocalContext.current
    val bmp = ImageEditor.bitmap

    var tool by remember { mutableStateOf(ITool.MOVE) }
    var zoom by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var color by remember { mutableStateOf(0xFF000000.toInt()) }
    var color2 by remember { mutableStateOf(0xFFFFFFFF.toInt()) }
    var brushSize by remember { mutableStateOf(12f) }
    var eraser by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf<Rect?>(null) }
    val strokePts = remember { mutableStateListOf<PointF>() }
    var anchor by remember { mutableStateOf<PointF?>(null) }
    var cloneSrc by remember { mutableStateOf<PointF?>(null) }
    var cloneOff by remember { mutableStateOf<PointF?>(null) }
    var strength by remember { mutableStateOf(12f) }
    var pendingIdx by remember { mutableStateOf<Int?>(null) }
    var askSaveNext by remember { mutableStateOf(false) }
    var showList by remember { mutableStateOf(false) }
    var listPage by remember { mutableStateOf(0) }
    var presetName by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { ImageEditor.loadPresets() }

    var showTransform by remember { mutableStateOf(false) }
    var showResize by remember { mutableStateOf(false) }
    var showSave by remember { mutableStateOf(false) }
    var askOverwrite by remember { mutableStateOf(false) }
    var askReplace by remember { mutableStateOf(false) }
    var textPos by remember { mutableStateOf<PointF?>(null) }

    // text options
    var text by remember { mutableStateOf("") }
    var textSize by remember { mutableStateOf(48f) }
    var outlineW by remember { mutableStateOf(0f) }
    var bold by remember { mutableStateOf(false) }
    var fontIdx by remember { mutableStateOf(0) }
    var customFont by remember { mutableStateOf<Typeface?>(null) }
    var customFontName by remember { mutableStateOf("") }

    // save options
    var fmt by remember { mutableStateOf(ImageEditor.FMT_PNG) }
    var quality by remember { mutableStateOf(90f) }
    var saveName by remember { mutableStateOf("") }

    val openLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (ex: Exception) { }
            val nm = try { DocumentFile.fromSingleUri(ctx, u)?.name ?: "image.png" } catch (ex: Exception) { "image.png" }
            if (ImageEditor.open(ctx, u, nm)) {
                zoom = 1f; pan = Offset.Zero; sel = null; textPos = null
                ImageEditor.formatOf(nm)?.let { fmt = it }
                saveName = nm.substringBeforeLast('.') + "_edit"
            } else Toast.makeText(ctx, e.loadFail, Toast.LENGTH_SHORT).show()
        }
    }
    val fontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            val tf = ImageEditor.loadFont(ctx, u)
            if (tf != null) {
                customFont = tf
                customFontName = try { DocumentFile.fromSingleUri(ctx, u)?.name ?: "" } catch (ex: Exception) { "" }
            } else Toast.makeText(ctx, e.fontFail, Toast.LENGTH_SHORT).show()
        }
    }
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { u ->
        if (u != null) {
            if (ImageEditor.write(ctx, u, fmt, quality.roundToInt(), false)) { ImageEditor.markSaved(); Toast.makeText(ctx, e.saved, Toast.LENGTH_SHORT).show() }
            else Toast.makeText(ctx, e.saveFail, Toast.LENGTH_SHORT).show()
        }
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (ex: Exception) { }
            ImageEditor.loadFolder(ctx, u, true)
        }
    }
    // open image number [i] of the folder; unsaved changes are asked about first
    fun goTo(i: Int) {
        if (i < 0 || i >= ImageEditor.folder.size) return
        if (ImageEditor.bitmap != null && ImageEditor.dirty) pendingIdx = i
        else if (!ImageEditor.openIndex(ctx, i)) Toast.makeText(ctx, e.loadFail, Toast.LENGTH_SHORT).show()
    }
    fun doSaveNext() {
        if (ImageEditor.saveInPlace(ctx)) {
            val nx = ImageEditor.folderIndex + 1
            if (nx < ImageEditor.folder.size) goTo(nx) else Toast.makeText(ctx, e.lastImage, Toast.LENGTH_SHORT).show()
        } else Toast.makeText(ctx, e.saveFail, Toast.LENGTH_SHORT).show()
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(e.menuTitle, { if (ImageEditor.tryExit()) onBack() }, fixed = true, trailing = {
            if (bmp != null) {
                IconButton(onClick = { ImageEditor.undo(); sel = null }, enabled = ImageEditor.canUndo) {
                    Icon(AppIcons.Undo, e.undo, tint = if (ImageEditor.canUndo) p.text else p.muted.copy(alpha = 0.4f))
                }
                IconButton(onClick = { ImageEditor.redo(); sel = null }, enabled = ImageEditor.canRedo) {
                    Icon(AppIcons.Redo, e.redo, tint = if (ImageEditor.canRedo) p.text else p.muted.copy(alpha = 0.4f))
                }
                IconButton(onClick = { showSave = true }) { Icon(AppIcons.Save, e.save, tint = p.text) }
            }
        })

        if (bmp == null) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                EmptyArt(AppIcons.Brush, 132.dp)
                Spacer(Modifier.height(16.dp))
                Text(e.emptyTitle, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                Spacer(Modifier.height(6.dp))
                Text(e.emptySub, fontSize = 14.sp, color = p.muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.width(200.dp)) { PrimaryButton(e.openBtn, true) { openLauncher.launch(arrayOf("image/*")) } }
                Spacer(Modifier.height(10.dp))
                Box(Modifier.width(200.dp)) { SecondaryButton(e.openFolder) { folderLauncher.launch(null) } }
                if (ImageEditor.folderLoading) { Spacer(Modifier.height(10.dp)); Text(e.folderLoading, color = p.muted, fontSize = 13.sp) }
                else if (ImageEditor.folderName.isNotEmpty() && ImageEditor.folder.isEmpty()) { Spacer(Modifier.height(10.dp)); Text(e.noImages, color = p.muted, fontSize = 13.sp) }
            }
        } else {
            val bw = bmp.width
            val bh = bmp.height
            // a new picture size (crop, resize, rotate) starts at "fit"
            LaunchedEffect(bmp) { zoom = 1f; pan = Offset.Zero; sel = null; textPos = null }

            fun viewOf(sz: Size): IView {
                val fs = min(sz.width / bw, sz.height / bh)
                val s = fs * zoom
                return IView((sz.width - bw * s) / 2f + pan.x, (sz.height - bh * s) / 2f + pan.y, s)
            }
            fun toBmp(o: Offset, sz: Size): PointF {
                val v = viewOf(sz)
                return PointF((o.x - v.left) / v.s, (o.y - v.top) / v.s)
            }
            fun clampPt(pt: PointF) = PointF(pt.x.coerceIn(0f, bw.toFloat()), pt.y.coerceIn(0f, bh.toFloat()))

            val paint = remember { android.graphics.Paint() }
            val checkA = if (p.dark) Color(0xFF3A3A3A) else Color(0xFFE6E6E6)
            val checkB = if (p.dark) Color(0xFF2A2A2A) else Color(0xFFCFCFCF)

            Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.card)) {
                Canvas(
                    Modifier
                        .fillMaxSize()
                        .pointerInput(tool) {
                            if (tool == ITool.MOVE) detectTransformGestures { c, dp, z, _ ->
                                val sz = Size(size.width.toFloat(), size.height.toFloat())
                                val nz = (zoom * z).coerceIn(0.2f, 40f)
                                val f = nz / zoom
                                val ctr = Offset(sz.width / 2f, sz.height / 2f)
                                pan = c - ctr - (c - ctr - pan) * f + dp
                                zoom = nz
                            }
                        }
                        .pointerInput(tool) {
                            if (tool != ITool.MOVE) detectTapGestures { o ->
                                val sz = Size(size.width.toFloat(), size.height.toFloat())
                                val pt = toBmp(o, sz)
                                val inside = pt.x >= 0f && pt.y >= 0f && pt.x < bw && pt.y < bh
                                when (tool) {
                                    ITool.SELECT -> sel = null
                                    ITool.BRUSH -> if (inside) ImageEditor.stroke(listOf(pt), color, brushSize, eraser)
                                    ITool.TEXT -> if (inside) textPos = pt
                                    ITool.CLONE -> if (inside) cloneSrc = pt
                                    ITool.PICK -> if (inside) {
                                        ImageEditor.pixelAt(pt.x.toInt(), pt.y.toInt())?.let { px ->
                                            color = px or 0xFF000000.toInt()
                                            Toast.makeText(ctx, e.picked(hexOf(px)), Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        }
                        .pointerInput(tool) {
                            if (tool == ITool.SELECT || tool == ITool.BRUSH || tool == ITool.CLONE) detectDragGestures(
                                onDragStart = { o ->
                                    val sz = Size(size.width.toFloat(), size.height.toFloat())
                                    val pt = toBmp(o, sz)
                                    if (tool == ITool.SELECT) { anchor = clampPt(pt); sel = null }
                                    else {
                                        strokePts.clear(); strokePts.add(pt)
                                        if (tool == ITool.CLONE) cloneOff = cloneSrc?.let { PointF(it.x - pt.x, it.y - pt.y) }
                                    }
                                },
                                onDragEnd = {
                                    if (tool == ITool.BRUSH) {
                                        if (strokePts.isNotEmpty()) ImageEditor.stroke(strokePts.toList(), color, brushSize, eraser)
                                        strokePts.clear()
                                    } else if (tool == ITool.CLONE) {
                                        val off = cloneOff
                                        if (off != null && strokePts.isNotEmpty()) ImageEditor.cloneStroke(strokePts.toList(), off.x, off.y, brushSize)
                                        strokePts.clear()
                                    }
                                },
                                onDragCancel = { strokePts.clear() },
                            ) { change, _ ->
                                val sz = Size(size.width.toFloat(), size.height.toFloat())
                                val pt = toBmp(change.position, sz)
                                if (tool == ITool.SELECT) {
                                    val a = anchor
                                    if (a != null) {
                                        val b = clampPt(pt)
                                        val r = Rect(min(a.x, b.x).toInt(), min(a.y, b.y).toInt(), max(a.x, b.x).toInt(), max(a.y, b.y).toInt())
                                        sel = if (r.width() >= 1 && r.height() >= 1) r else null
                                    }
                                } else strokePts.add(pt)
                            }
                        },
                ) {
                    ImageEditor.version // redraw after in-place edits
                    val v = viewOf(size)
                    val w = bw * v.s
                    val h = bh * v.s
                    clipRect(v.left, v.top, v.left + w, v.top + h) {
                        val cell = 14.dp.toPx()
                        var yy = v.top
                        var row = 0
                        while (yy < v.top + h) {
                            var xx = v.left
                            var col = row % 2
                            while (xx < v.left + w) {
                                drawRect(if (col % 2 == 0) checkA else checkB, Offset(xx, yy), Size(cell, cell))
                                xx += cell; col++
                            }
                            yy += cell; row++
                        }
                    }
                    paint.isFilterBitmap = v.s < 3f
                    drawIntoCanvas { c ->
                        c.nativeCanvas.drawBitmap(bmp, null, android.graphics.RectF(v.left, v.top, v.left + w, v.top + h), paint)
                    }
                    sel?.let { r ->
                        drawRect(
                            color = p.accent,
                            topLeft = Offset(v.left + r.left * v.s, v.top + r.top * v.s),
                            size = Size(r.width() * v.s, r.height() * v.s),
                            style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                        )
                    }
                    if (tool == ITool.CLONE) cloneSrc?.let { sp ->
                        val cx = v.left + sp.x * v.s
                        val cy = v.top + sp.y * v.s
                        drawCircle(Color.White, radius = max(6f, brushSize * v.s / 2f), center = Offset(cx, cy), style = Stroke(width = 3.dp.toPx()))
                        drawCircle(p.accent, radius = max(6f, brushSize * v.s / 2f), center = Offset(cx, cy), style = Stroke(width = 1.5.dp.toPx()))
                    }
                    if (strokePts.size > 1) {
                        val path = Path()
                        strokePts.forEachIndexed { i, pt ->
                            val x = v.left + pt.x * v.s
                            val y = v.top + pt.y * v.s
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(
                            path,
                            color = if (tool == ITool.CLONE || (tool == ITool.BRUSH && eraser)) Color.White.copy(alpha = 0.6f) else Color(color),
                            style = Stroke(width = max(1f, brushSize * v.s), cap = StrokeCap.Round, join = StrokeJoin.Round),
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            SegmentedChips(listOf(e.toolMove, e.toolSelect, e.toolBrush, e.toolText, e.toolPick, e.toolClone), tool.ordinal, 12.sp) { tool = ITool.values()[it] }
            Spacer(Modifier.height(8.dp))
            Column(
                Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(rememberScrollState()).padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (tool) {
                    ITool.MOVE -> {
                        Text(e.sizeInfo(bw, bh, (zoom * 100f).roundToInt()), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        DetailLink(e.hintMove, 13.sp, 19.sp)
                        if (ImageEditor.scaledDown) DetailLink(e.scaledNote, 13.sp, 19.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) { SecondaryButton(e.fit) { zoom = 1f; pan = Offset.Zero } }
                            Box(Modifier.weight(1f)) { SecondaryButton(e.transform) { showTransform = true } }
                        }
                        if (ImageEditor.folder.isNotEmpty()) {
                            val fi = ImageEditor.folderIndex
                            Text(e.folderInfo(maxOf(fi, 0), ImageEditor.folder.size, ImageEditor.folderName), color = p.text, fontSize = 13.sp)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.weight(1f)) { SecondaryButton(e.prev, fi > 0) { goTo(fi - 1) } }
                                Box(Modifier.weight(1f)) { SecondaryButton(e.next, fi + 1 < ImageEditor.folder.size) { goTo(fi + 1) } }
                            }
                            PrimaryButton(e.saveNext, ImageEditor.sourceUri != null) { if (ImageEditor.overwriteOk) doSaveNext() else askSaveNext = true }
                            SecondaryButton(e.imageList) { listPage = maxOf(fi, 0) / 50; showList = true }
                        }
                        SecondaryButton(e.openFolder) { folderLauncher.launch(null) }
                        SecondaryButton(e.openOther) { if (ImageEditor.dirty) askReplace = true else openLauncher.launch(arrayOf("image/*")) }
                    }
                    ITool.SELECT -> {
                        DetailLink(e.hintSelect, 13.sp, 19.sp)
                        val r = sel
                        Text(if (r == null) e.noSel else e.selInfo(r.left, r.top, r.width(), r.height()), color = p.muted, fontSize = 13.sp)
                        ColorRow(e.color, color) { color = it }
                        SecondaryButton(e.selAll) { sel = Rect(0, 0, bw, bh) }
                        SecondaryButton(e.crop, r != null) { r?.let { ImageEditor.crop(it); sel = null } }
                        SecondaryButton(e.fillSel, r != null) { r?.let { ImageEditor.fill(it, color) } }
                        SecondaryButton(e.clearSel, r != null) { r?.let { ImageEditor.clear(it) } }
                        SecondaryButton(e.autoFill, r != null) { r?.let { ImageEditor.autoFill(it) } }
                        DetailLink(e.autoFillNote, 12.sp, 17.sp)
                        LabeledSlider(e.strength, strength, 2f, 60f) { strength = it }
                        SecondaryButton(e.blurSel, r != null) { r?.let { ImageEditor.blur(it, strength.roundToInt(), false) } }
                        SecondaryButton(e.pixelSel, r != null) { r?.let { ImageEditor.blur(it, strength.roundToInt(), true) } }
                    }
                    ITool.BRUSH -> {
                        DetailLink(e.hintBrush, 13.sp, 19.sp)
                        SegmentedChips(listOf(e.brushMode, e.eraserMode), if (eraser) 1 else 0, 14.sp) { eraser = it == 1 }
                        LabeledSlider(e.brushSize, brushSize, 1f, max(100f, min(bw, bh) / 4f)) { brushSize = it }
                        if (!eraser) ColorRow(e.color, color) { color = it }
                    }
                    ITool.TEXT -> {
                        DetailLink(e.hintText, 13.sp, 19.sp)
                        ColorRow(e.color, color) { color = it }
                    }
                    ITool.CLONE -> {
                        DetailLink(e.hintClone, 13.sp, 19.sp)
                        if (cloneSrc == null) Text(e.cloneNoSrc, color = p.muted, fontSize = 13.sp)
                        LabeledSlider(e.brushSize, brushSize, 1f, max(100f, min(bw, bh) / 4f)) { brushSize = it }
                    }
                    ITool.PICK -> {
                        DetailLink(e.hintPick, 13.sp, 19.sp)
                        ColorRow(e.color, color) { color = it }
                    }
                }
            }
        }
    }

    // ---- Dialogs ----
    if (showTransform) {
        AppDialog(e.transform, { showTransform = false }, buttons = { DlgBtn(e.close, false) { showTransform = false } }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton(e.rotL) { ImageEditor.rotate(-90f); sel = null }
                SecondaryButton(e.rotR) { ImageEditor.rotate(90f); sel = null }
                SecondaryButton(e.flipH) { ImageEditor.flip(true) }
                SecondaryButton(e.flipV) { ImageEditor.flip(false) }
                SecondaryButton(e.resize) { showTransform = false; showResize = true }
            }
        }
    }
    if (showResize) {
        val cur = ImageEditor.bitmap
        val ow = cur?.width ?: 1
        val oh = cur?.height ?: 1
        var wTxt by remember { mutableStateOf(ow.toString()) }
        var hTxt by remember { mutableStateOf(oh.toString()) }
        var keep by remember { mutableStateOf(true) }
        val nw = wTxt.toIntOrNull()
        val nh = hTxt.toIntOrNull()
        val ok = nw != null && nh != null && ImageEditor.sizeOk(nw, nh)
        AppDialog(e.resize, { showResize = false }, buttons = {
            DlgBtn(e.cancel, false) { showResize = false }
            DlgBtn(e.apply, true, enabled = ok) { if (nw != null && nh != null) { ImageEditor.resize(nw, nh); sel = null }; showResize = false }
        }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(wTxt, { v ->
                    wTxt = v.filter { it.isDigit() }.take(5)
                    if (keep) wTxt.toIntOrNull()?.let { hTxt = max(1, (it.toLong() * oh / ow).toInt()).toString() }
                }, e.width, number = true)
                AppTextField(hTxt, { v ->
                    hTxt = v.filter { it.isDigit() }.take(5)
                    if (keep) hTxt.toIntOrNull()?.let { wTxt = max(1, (it.toLong() * ow / oh).toInt()).toString() }
                }, e.height, number = true)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { keep = !keep },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = keep, onCheckedChange = { keep = it }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                    Text(e.keepRatio, color = p.text, fontSize = 14.sp)
                }
                DetailLink(e.sizeLimit, 12.sp, 17.sp)
            }
        }
    }
    textPos?.let { pos ->
        AppDialog(e.textTitle, { textPos = null }, buttons = {
            DlgBtn(e.cancel, false) { textPos = null }
            DlgBtn(e.add, true, enabled = text.isNotBlank()) {
                val base = customFont ?: when (fontIdx) { 1 -> Typeface.SERIF; 2 -> Typeface.MONOSPACE; else -> Typeface.SANS_SERIF }
                val tf = Typeface.create(base, if (bold) Typeface.BOLD else Typeface.NORMAL)
                ImageEditor.text(text, pos.x, pos.y, textSize, color, color2, outlineW, tf)
                textPos = null
            }
        }) {
            Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(text, { text = it }, e.textLabel, multiline = true)
                val cur = ImageEditor.bitmap
                LabeledSlider(e.textSize, textSize, 8f, max(120f, (cur?.height ?: 400) / 2f)) { textSize = it }
                LabeledSlider(e.outlineW, outlineW, 0f, 30f) { outlineW = it }
                ColorRow(e.color, color) { color = it }
                if (outlineW > 0f) ColorRow(e.color2, color2) { color2 = it }
                if (ImageEditor.presets.isNotEmpty()) {
                    Text(e.presetsTitle, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    ImageEditor.presets.forEach { pr ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                pr.name, color = p.accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).clickable {
                                    textSize = pr.size; outlineW = pr.outline; bold = pr.bold; fontIdx = pr.font; color = pr.color; color2 = pr.color2; customFont = null; customFontName = ""
                                }.padding(vertical = 8.dp),
                            )
                            IconButton(onClick = { ImageEditor.removePreset(pr.name) }) { Icon(AppIcons.Trash, e.delete, tint = p.muted) }
                        }
                    }
                }
                if (customFont == null) SegmentedChips(listOf("Sans", "Serif", "Mono"), fontIdx, 14.sp) { fontIdx = it }
                else DetailLink(e.fontUsing(customFontName), 13.sp, 19.sp)
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { bold = !bold },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = bold, onCheckedChange = { bold = it }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                    Text(e.bold, color = p.text, fontSize = 14.sp)
                }
                if (customFont == null) SecondaryButton(e.fontFile) { fontLauncher.launch(arrayOf("*/*")) }
                else SecondaryButton(e.fontReset) { customFont = null; customFontName = "" }
                AppTextField(presetName, { presetName = it }, e.presetName)
                SecondaryButton(e.presetSave, presetName.isNotBlank()) {
                    ImageEditor.addPreset(ImageEditor.TextPreset(presetName.trim(), textSize, outlineW, bold, fontIdx, color, color2)); presetName = ""
                }
                DetailLink(e.presetNote, 12.sp, 17.sp)
            }
        }
    }
    if (showSave) {
        val src = ImageEditor.sourceUri
        AppDialog(e.saveTitle, { showSave = false }, buttons = {
            DlgBtn(e.cancel, false) { showSave = false }
            if (src != null) DlgBtn(e.overwrite, false) { askOverwrite = true }
            DlgBtn(e.saveAsNew, true, enabled = saveName.isNotBlank()) {
                showSave = false
                saveLauncher.launch(saveName.trim() + "." + ImageEditor.extOf(fmt))
            }
        }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SegmentedChips(listOf("PNG", "JPG", "WebP"), fmt, 14.sp) { fmt = it }
                if (fmt != ImageEditor.FMT_PNG) LabeledSlider(e.quality, quality, 1f, 100f) { quality = it }
                if (fmt == ImageEditor.FMT_JPG) DetailLink(e.jpgNote, 12.sp, 17.sp)
                AppTextField(saveName, { saveName = it }, e.fileName)
            }
        }
    }
    if (askOverwrite) {
        ConfirmDialog(e.overwrite, e.overwriteAsk, e.yes, e.cancel, onConfirm = {
            askOverwrite = false; showSave = false
            val src = ImageEditor.sourceUri
            // the original file keeps its own format when it is a known image type
            val f = ImageEditor.formatOf(ImageEditor.name) ?: fmt
            if (src != null && ImageEditor.write(ctx, src, f, quality.roundToInt(), true)) { ImageEditor.markSaved(); Toast.makeText(ctx, e.saved, Toast.LENGTH_SHORT).show() }
            else Toast.makeText(ctx, e.saveFail, Toast.LENGTH_SHORT).show()
        }, onDismiss = { askOverwrite = false })
    }
    pendingIdx?.let { idx ->
        ConfirmDialog(e.discardTitle, e.discardMsg, e.yes, e.cancel, onConfirm = {
            pendingIdx = null
            if (!ImageEditor.openIndex(ctx, idx)) Toast.makeText(ctx, e.loadFail, Toast.LENGTH_SHORT).show()
        }, onDismiss = { pendingIdx = null })
    }
    if (askSaveNext) {
        ConfirmDialog(e.saveNext, e.saveNextAsk, e.yes, e.cancel, onConfirm = {
            askSaveNext = false; ImageEditor.overwriteOk = true; doSaveNext()
        }, onDismiss = { askSaveNext = false })
    }
    if (showList) {
        val items = ImageEditor.folder
        val pages = maxOf(1, (items.size + 49) / 50)
        val pg = listPage.coerceIn(0, pages - 1)
        AppDialog(e.imageList, { showList = false }, buttons = { DlgBtn(e.close, false) { showList = false } }) {
            Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (items.size > 50) PageNav(pg, pages, { listPage = it })
                items.drop(pg * 50).take(50).forEachIndexed { k, it ->
                    val idx = pg * 50 + k
                    Text(
                        it.path,
                        color = if (idx == ImageEditor.folderIndex) p.accent else p.text,
                        fontSize = 13.sp,
                        fontWeight = if (idx == ImageEditor.folderIndex) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { showList = false; goTo(idx) }.padding(vertical = 8.dp, horizontal = 4.dp),
                    )
                }
            }
        }
    }
    if (askReplace) {
        ConfirmDialog(e.discardTitle, e.discardMsg, e.yes, e.cancel, onConfirm = {
            askReplace = false; openLauncher.launch(arrayOf("image/*"))
        }, onDismiss = { askReplace = false })
    }
    if (ImageEditor.askDiscard) {
        ConfirmDialog(e.discardTitle, e.discardMsg, e.yes, e.cancel, onConfirm = {
            ImageEditor.askDiscard = false; ImageEditor.close(); onBack()
        }, onDismiss = { ImageEditor.askDiscard = false })
    }
}

/** Label and value on one line with a slider below. */
@Composable
private fun LabeledSlider(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    val p = LocalPalette.current
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(value.roundToInt().toString(), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value.coerceIn(min, max), onValueChange = onChange, valueRange = min..max,
            colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent),
        )
    }
}

/** A row of color swatches and a hex field; the chosen color is shown at the right of the label. */
@Composable
private fun ColorRow(label: String, color: Int, onChange: (Int) -> Unit) {
    val p = LocalPalette.current
    val e = imageEditStr(LocalLang.current)
    var hex by remember(color) { mutableStateOf(hexOf(color)) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box(Modifier.size(26.dp).clip(CircleShape).background(Color(color)).border(1.dp, p.cardBorder, CircleShape))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IMG_PALETTE.forEach { c ->
                Box(Modifier.size(32.dp).clip(CircleShape).background(Color(c)).border(1.dp, p.cardBorder, CircleShape).clickable { onChange(c) })
            }
        }
        AppTextField(hex, { hex = it; parseHex(it)?.let(onChange) }, e.hexLabel, mono = true)
    }
}
