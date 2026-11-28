package com.furuarc.ui

import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Path
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.furuarc.core.Doc
import com.furuarc.core.ImgExport
import com.furuarc.core.ImgRender
import com.furuarc.core.ImgStore
import com.furuarc.core.LKind
import com.furuarc.i18n.imageEditStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/** Image Editor: the home page (projects) or, when a document is open, the layered editor. */
@Composable
fun ImageEditorScreen(onBack: () -> Unit) {
    val doc = ImgSession.doc
    if (doc == null) ImgHome(onBack) else ImgEditor(doc)
}

// ====================================================================== home

@Composable
private fun ImgHome(onBack: () -> Unit) {
    val p = LocalPalette.current
    val e = imageEditStr(LocalLang.current)
    val ctx = LocalContext.current
    var refresh by remember { mutableStateOf(0) }
    val projects = remember(refresh) { ImgStore.list(ctx) }
    var newDlg by remember { mutableStateOf(false) }
    var renameId by remember { mutableStateOf<String?>(null) }
    var deleteId by remember { mutableStateOf<String?>(null) }
    val fmt = remember { java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US) }

    fun open(doc: Doc?) {
        if (doc == null) { android.widget.Toast.makeText(ctx, e.loadFail, android.widget.Toast.LENGTH_SHORT).show(); return }
        ImgSession.doc = doc
    }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            try {
                val b = ImgStore.decode(ctx, u, Doc.MAX_SIDE)
                if (b == null) { open(null); return@rememberLauncherForActivityResult }
                val d = Doc(b.width, b.height)
                d.bg = if (b.hasAlpha()) 0 else 0xFFFFFFFF.toInt()
                d.name = ImgStore.displayName(ctx, u).substringBeforeLast('.')
                d.addBitmap(LKind.IMAGE, b, e.layerImage)
                d.resetHistory(e.stepNames["open"] ?: "open")
                ImgStore.save(ctx, d)
                open(d)
            } catch (x: OutOfMemoryError) { android.widget.Toast.makeText(ctx, e.noMemory, android.widget.Toast.LENGTH_SHORT).show() }
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(e.menuTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            PrimaryButton(e.newProject, true) { newDlg = true }
            SecondaryButton(e.openImage) { pick.launch(arrayOf("image/*")) }
            Text(e.recent, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (projects.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    EmptyArt(AppIcons.Brush)
                    Text(e.noRecent, color = p.text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(e.noRecentSub, color = p.muted, fontSize = 14.sp, lineHeight = 20.sp)
                    SecondaryButton(e.blankCanvas) { newDlg = true }
                }
            }
            projects.forEach { m ->
                var menu by remember { mutableStateOf(false) }
                val thumb = remember(m.id, m.modified) { try { BitmapFactory.decodeFile(ImgStore.thumb(ctx, m.id).path)?.asImageBitmap() } catch (x: Throwable) { null } }
                AppCard(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            try { open(ImgStore.load(ctx, m.id)) } catch (x: OutOfMemoryError) { android.widget.Toast.makeText(ctx, e.noMemory, android.widget.Toast.LENGTH_SHORT).show() }
                        }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(Modifier.size(64.dp).clip(RoundedCornerShape(10.dp)).background(p.field), contentAlignment = Alignment.Center) {
                            if (thumb != null) Image(thumb, null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                            else androidx.compose.material.Icon(AppIcons.Image, null, tint = p.muted)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(m.name.ifEmpty { e.untitled }, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text("${m.w} × ${m.h}  ·  ${fmt.format(java.util.Date(m.modified))}", color = p.muted, fontSize = 12.sp)
                        }
                        Box {
                            IIconBtn(AppIcons.MoreHoriz) { menu = true }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(onClick = { menu = false; renameId = m.id }) { Text(e.rename, color = p.text) }
                                DropdownMenuItem(onClick = { menu = false; ImgStore.duplicate(ctx, m.id, e.copySuffix); refresh++ }) { Text(e.duplicate, color = p.text) }
                                DropdownMenuItem(onClick = { menu = false; deleteId = m.id }) { Text(e.delete, color = p.text) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (newDlg) NewProjectDialog(e, { newDlg = false }) { name, w, h, bg ->
        newDlg = false
        try {
            val d = Doc(w, h)
            d.bg = bg; d.name = name.ifEmpty { e.untitled }
            d.resetHistory(e.stepNames["new"] ?: "new")
            ImgStore.save(ctx, d)
            open(d)
        } catch (x: OutOfMemoryError) { android.widget.Toast.makeText(ctx, e.noMemory, android.widget.Toast.LENGTH_SHORT).show() }
    }
    renameId?.let { id ->
        NameDialog(e.rename, projects.firstOrNull { it.id == id }?.name ?: "", e, { renameId = null }) { ImgStore.rename(ctx, id, it); renameId = null; refresh++ }
    }
    deleteId?.let { id ->
        ConfirmDialog(e.delete, e.deleteAsk, e.delete, e.cancel, onConfirm = { ImgStore.delete(ctx, id); deleteId = null; refresh++ }, onDismiss = { deleteId = null })
    }
}

// ====================================================================== editor

@Composable
private fun ImgEditor(doc: Doc) {
    val p = LocalPalette.current
    val e = imageEditStr(LocalLang.current)
    val ctx = LocalContext.current
    val density = LocalDensity.current.density
    val st = remember(doc) { EdState(doc, ctx, e, density) }
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev

    // Back closes sheets / crop first
    DisposableEffect(st) {
        ImgSession.backHook = {
            when {
                st.sheet != 0 -> { st.sheet = 0; true }
                st.tool == ETool.CROP -> { st.tool = ETool.SELECT; true }
                else -> false
            }
        }
        onDispose { ImgSession.backHook = null; st.stroker?.release(); st.savePrefs() }
    }
    // save when the app goes to the background
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, st) {
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_PAUSE && doc.dirty) ImgStore.save(ctx, doc) }
        owner.lifecycle.addObserver(obs)
        onDispose { owner.lifecycle.removeObserver(obs) }
    }
    // autosave shortly after the last change
    LaunchedEffect(doc.rev) { delay(2500); if (doc.dirty) ImgStore.save(ctx, doc) }

    val imgPick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) st.addImage(u) }
    var pendingExport by remember { mutableStateOf<ExportParams?>(null) }
    val createDoc = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { u ->
        val pe = pendingExport
        pendingExport = null
        if (u != null && pe != null) st.safe {
            val bmp = ImgExport.render(doc, pe.width, pe.transparent)
            val ok = ImgExport.write(ctx, bmp, u, pe.fmt, pe.quality)
            bmp.recycle()
            st.toast(if (ok) e.exported else e.exportFail)
        }
    }

    Column(Modifier.fillMaxSize().background(p.background)) {
        // ---- top bar
        Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IIconBtn(Icons.Default.ArrowBack) { ImgSession.close(ctx) }
            Text(
                doc.name.ifEmpty { e.untitled }, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1,
                modifier = Modifier.weight(1f).clickable { st.renameDlg = true }.padding(horizontal = 6.dp),
            )
            IIconBtn(AppIcons.Undo, enabled = doc.canUndo()) { doc.undo() }
            IIconBtn(AppIcons.Redo, enabled = doc.canRedo()) { doc.redo() }
            IIconBtn(AppIcons.MoreHoriz) { st.sheet = if (st.sheet == 2) 0 else 2 }
        }

        // ---- canvas
        Box(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
            val accent = p.accent.toArgb()
            Canvas(
                Modifier.fillMaxSize().onSizeChanged { st.viewSized(it.width, it.height) }.pointerInput(st) { editorGestures(st) },
            ) {
                @Suppress("UNUSED_VARIABLE") val r1 = doc.rev
                @Suppress("UNUSED_VARIABLE") val r2 = st.strokeTick
                @Suppress("UNUSED_VARIABLE") val r3 = st.brushRev
                drawIntoCanvas { cv -> drawEditor(cv.nativeCanvas, st, density, accent) }
            }
        }

        // ---- bottom panel
        when (st.sheet) {
            1 -> LayersSheet(st) { imgPick.launch(arrayOf("image/*")) }
            2 -> MoreSheet(st, { st.sheet = 0; st.exportDlg = true }, { ImgStore.save(ctx, doc); st.toast(e.saved) })
            3 -> HistorySheet(st)
            else -> when (st.tool) {
                ETool.SELECT -> SelectPanel(st)
                ETool.DRAW, ETool.PIPET -> DrawPanel(st)
                ETool.TEXT -> TextPanel(st)
                ETool.SHAPE -> ShapePanel(st)
                ETool.CROP -> CropPanel(st)
            }
        }

        // ---- tool dock
        Row(Modifier.fillMaxWidth().background(p.card).horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            IDockBtn(AppIcons.Cursor, e.tSelect, st.sheet == 0 && st.tool == ETool.SELECT) { st.pick(ETool.SELECT) }
            IDockBtn(AppIcons.Brush, e.tDraw, st.sheet == 0 && (st.tool == ETool.DRAW || st.tool == ETool.PIPET)) { st.pick(ETool.DRAW) }
            IDockBtn(AppIcons.TextFields, e.tText, st.sheet == 0 && st.tool == ETool.TEXT) { st.pick(ETool.TEXT) }
            IDockBtn(AppIcons.Shapes, e.tShape, st.sheet == 0 && st.tool == ETool.SHAPE) { st.pick(ETool.SHAPE) }
            IDockBtn(AppIcons.Crop, e.tCrop, st.sheet == 0 && st.tool == ETool.CROP) { st.pick(ETool.CROP) }
            IDockBtn(AppIcons.Layers, e.tLayers, st.sheet == 1) { st.sheet = if (st.sheet == 1) 0 else 1 }
            IDockBtn(AppIcons.MoreHoriz, e.tMore, st.sheet == 2 || st.sheet == 3) { st.sheet = if (st.sheet == 2) 0 else 2 }
        }
    }

    // ---- dialogs
    if (st.textDlg) TextDialog(st)
    if (st.exportDlg) ExportDialog(st) { pe -> pendingExport = pe; createDoc.launch(pe.name + "." + ImgExport.ext(pe.fmt)) }
    CanvasDialogs(st)
    if (st.renameDlg) NameDialog(e.rename, doc.name, e, { st.renameDlg = false }) { doc.name = it; doc.touch(); st.renameDlg = false }
    st.layerRename?.let { id ->
        val l = doc.layers.firstOrNull { it.id == id }
        if (l == null) st.layerRename = null
        else NameDialog(e.layerName, l.name, e, { st.layerRename = null }) { l.name = it; st.layerRename = null; doc.commit(e.stepNames["edit"] ?: "~") }
    }
    if (st.colorDlg != 0) {
        val act = doc.active()
        val init = when (st.colorDlg) {
            1 -> st.brush.color
            2 -> act?.fill ?: 0xFFFFFFFF.toInt()
            3 -> act?.text?.color ?: 0xFF000000.toInt()
            4 -> act?.shape?.fill ?: 0xFFFFFFFF.toInt()
            else -> act?.shape?.stroke ?: 0xFF000000.toInt()
        }
        IColorDialog(init, e, { st.colorDlg = 0 }) { c ->
            when (st.colorDlg) {
                1 -> { st.brush.color = c; st.brushChanged(); st.savePrefs() }
                2 -> act?.let { it.fill = c; doc.commit(e.stepNames["edit"] ?: "~") }
                3 -> act?.text?.let { it.color = c; doc.commit(e.stepNames["edit"] ?: "~") }
                4 -> act?.shape?.let { it.fill = c; doc.commit(e.stepNames["edit"] ?: "~") }
                else -> act?.shape?.let { it.stroke = c; doc.commit(e.stepNames["edit"] ?: "~") }
            }
            st.colorDlg = 0
        }
    }
    if (st.flattenDlg) ConfirmDialog(e.flatten, e.flattenAsk, e.ok, e.cancel, onConfirm = {
        st.flattenDlg = false; st.step("merge") { doc.flatten(e.layerImage) }
    }, onDismiss = { st.flattenDlg = false })
}

/** One finger works the tool, two fingers zoom and pan the canvas. */
private suspend fun PointerInputScope.editorGestures(st: EdState) {
    awaitPointerEventScope {
        var mode = 0 // 0 idle, 1 tool, 2 view
        while (true) {
            val ev = awaitPointerEvent()
            val pressed = ev.changes.filter { it.pressed }
            if (pressed.size >= 2) {
                if (mode == 1) st.cancel()
                mode = 2
                var cx = 0f; var cy = 0f; var px = 0f; var py = 0f
                for (c in pressed) { cx += c.position.x; cy += c.position.y; px += c.previousPosition.x; py += c.previousPosition.y }
                val n = pressed.size
                cx /= n; cy /= n; px /= n; py /= n
                var span = 0f; var pspan = 0f
                for (c in pressed) {
                    span += hypot(c.position.x - cx, c.position.y - cy)
                    pspan += hypot(c.previousPosition.x - px, c.previousPosition.y - py)
                }
                val zoom = if (pspan > 1f) span / pspan else 1f
                st.pinch(Offset(px, py), Offset(cx, cy), zoom)
                ev.changes.forEach { it.consumeAllChanges() }
            } else if (pressed.size == 1) {
                val c = pressed[0]
                if (mode == 0 && !c.previousPressed) {
                    mode = 1
                    st.down(c.position.x, c.position.y)
                    c.consumeAllChanges()
                } else if (mode == 1) {
                    st.move(c.position.x, c.position.y)
                    c.consumeAllChanges()
                }
            } else {
                if (mode == 1) st.up()
                mode = 0
            }
        }
    }
}

private val checkPaintA = Paint().apply { color = 0xFFC9C9C9.toInt() }

/** Draws the checkerboard, the document and the tool overlays (selection box, grid, guides, crop frame, brush cursor). */
private fun drawEditor(c: android.graphics.Canvas, st: EdState, density: Float, accent: Int) {
    val doc = st.doc
    val sc = st.scale; val ox = st.ox; val oy = st.oy
    val w = doc.w * sc; val h = doc.h * sc

    // checkerboard under the canvas
    c.save()
    c.clipRect(ox, oy, ox + w, oy + h)
    c.drawColor(0xFFEFEFEF.toInt())
    val tile = 14f * density
    val tx0 = max(0, floor((0f - ox) / tile).toInt()); val tx1 = min(ceil(w / tile).toInt(), ceil((st.viewW - ox) / tile).toInt())
    val ty0 = max(0, floor((0f - oy) / tile).toInt()); val ty1 = min(ceil(h / tile).toInt(), ceil((st.viewH - oy) / tile).toInt())
    for (ty in ty0 until ty1) for (tx in tx0 until tx1) {
        if ((tx + ty) % 2 == 0) c.drawRect(ox + tx * tile, oy + ty * tile, ox + (tx + 1) * tile, oy + (ty + 1) * tile, checkPaintA)
    }
    c.restore()

    // the document
    c.save()
    c.translate(ox, oy)
    c.scale(sc, sc)
    c.clipRect(0f, 0f, doc.w.toFloat(), doc.h.toFloat())
    ImgRender.drawDoc(c, doc, true, st.stroker?.overlay())
    c.restore()

    val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeWidth = 1f * density }
    // outline of the canvas
    line.color = 0x66000000
    c.drawRect(ox, oy, ox + w, oy + h, line)

    fun thirds(l: Float, t: Float, r: Float, b: Float) {
        line.color = 0x88FFFFFF.toInt()
        for (i in 1..2) {
            val x = l + (r - l) * i / 3f; val y = t + (b - t) * i / 3f
            c.drawLine(x, t, x, b, line); c.drawLine(l, y, r, y, line)
        }
    }

    when (st.tool) {
        ETool.CROP -> {
            val a = st.toScreen(st.cL, st.cT); val b = st.toScreen(st.cR, st.cB)
            val dim = Paint().apply { color = 0x99000000.toInt() }
            c.drawRect(ox, oy, ox + w, a.y, dim); c.drawRect(ox, b.y, ox + w, oy + h, dim)
            c.drawRect(ox, a.y, a.x, b.y, dim); c.drawRect(b.x, a.y, ox + w, b.y, dim)
            line.color = 0xFFFFFFFF.toInt(); line.strokeWidth = 2f * density
            c.drawRect(a.x, a.y, b.x, b.y, line)
            line.strokeWidth = 1f * density
            thirds(a.x, a.y, b.x, b.y)
            val hp = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
            val s = 7f * density
            for (px in floatArrayOf(a.x, b.x)) for (py in floatArrayOf(a.y, b.y)) c.drawRect(px - s, py - s, px + s, py + s, hp)
        }
        else -> {
            if (st.showGrid) thirds(ox, oy, ox + w, oy + h)
            // selection box and the corner handle
            val l = doc.active()
            if ((st.tool == ETool.SELECT || st.tool == ETool.TEXT || st.tool == ETool.SHAPE) && l != null && l.kind != LKind.FILL && l.visible) {
                val k = ImgRender.corners(l, doc)
                val path = Path()
                for (i in 0 until 4) {
                    val pt = st.toScreen(k[i * 2], k[i * 2 + 1])
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                path.close()
                line.color = accent; line.strokeWidth = 2f * density
                c.drawPath(path, line)
                if (!l.locked) {
                    val hp = st.toScreen(k[4], k[5])
                    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
                    c.drawCircle(hp.x, hp.y, 10f * density, fill)
                    line.color = 0xFFFFFFFF.toInt(); line.strokeWidth = 2f * density
                    c.drawCircle(hp.x, hp.y, 10f * density, line)
                }
            }
            // snap guides
            line.color = 0xFFFF2D95.toInt(); line.strokeWidth = 1.5f * density
            st.guideX?.let { gx -> val x = ox + gx * sc; c.drawLine(x, oy, x, oy + h, line) }
            st.guideY?.let { gy -> val y = oy + gy * sc; c.drawLine(ox, y, ox + w, y, line) }
        }
    }

    // brush cursor and the size preview
    if (st.tool == ETool.DRAW) {
        val r = st.brush.size * sc / 2f
        val cur = st.cursor
        val centre = if (cur != null) cur else if (st.sizePreview) Offset(st.viewW / 2f, st.viewH / 2f) else null
        if (centre != null) {
            line.strokeWidth = 2f * density; line.color = 0xFF000000.toInt()
            c.drawCircle(centre.x, centre.y, r + 1f, line)
            line.strokeWidth = 1f * density; line.color = 0xFFFFFFFF.toInt()
            c.drawCircle(centre.x, centre.y, r, line)
        }
    }
}
