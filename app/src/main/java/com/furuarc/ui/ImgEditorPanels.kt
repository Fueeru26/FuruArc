package com.furuarc.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.Brushes
import com.furuarc.core.ImgRender
import com.furuarc.core.LKind
import com.furuarc.core.Layer
import com.furuarc.ui.theme.LocalPalette
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private fun Layer.uniformScale() = (abs(sx) + abs(sy)) / 2f

// ---------------------------------------------------------------- brush

@Composable
internal fun DrawPanel(st: EdState) {
    val e = st.e
    @Suppress("UNUSED_VARIABLE") val rev = st.brushRev
    var more by remember { mutableStateOf(false) }
    val b = st.brush
    IPanel {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            e.brushNames.forEachIndexed { i, n -> IChip(n, b.type == i) { Brushes.preset(b, i); st.brushChanged(); st.savePrefs() } }
        }
        ISlider(e.size, b.size, 1f, 300f, { b.size = it; st.sizePreview = true; st.brushChanged() }, { st.sizePreview = false; st.savePrefs() }, suffix = " px")
        ISlider(e.opacity, b.opacity * 100f, 1f, 100f, { b.opacity = it / 100f; st.brushChanged() }, { st.savePrefs() }, suffix = "%")
        if (more) {
            ISlider(e.hardness, b.hardness * 100f, 0f, 100f, { b.hardness = it / 100f; st.brushChanged() }, { st.savePrefs() }, suffix = "%")
            ISlider(e.flow, b.flow * 100f, 1f, 100f, { b.flow = it / 100f; st.brushChanged() }, { st.savePrefs() }, suffix = "%")
            ISlider(e.smooth, b.smooth * 100f, 0f, 100f, { b.smooth = it / 100f; st.brushChanged() }, { st.savePrefs() }, suffix = "%")
        }
        if (b.type != Brushes.ERASER) IColorRow(b.color, { b.color = it; st.brushChanged(); st.savePrefs() }, { st.colorDlg = 1 }, { st.pick(ETool.PIPET) })
        IChip(e.advanced + if (more) " ▴" else " ▾", more) { more = !more }
    }
}

// ---------------------------------------------------------------- select

@Composable
internal fun SelectPanel(st: EdState) {
    val e = st.e
    val doc = st.doc
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev
    val l = doc.active()
    IPanel {
        if (l == null) { Text(e.noLayer, color = LocalPalette.current.muted, fontSize = 13.sp); return@IPanel }
        if (l.kind != LKind.FILL) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                fun align(h: Int, v: Int) {
                    if (l.locked) { st.toast(e.lockedLayer); return }
                    val c = ImgRender.corners(l, doc)
                    val minX = min(min(c[0], c[2]), min(c[4], c[6])); val maxX = max(max(c[0], c[2]), max(c[4], c[6]))
                    val minY = min(min(c[1], c[3]), min(c[5], c[7])); val maxY = max(max(c[1], c[3]), max(c[5], c[7]))
                    when (h) { 0 -> l.cx += -minX; 1 -> l.cx += doc.w / 2f - (minX + maxX) / 2f; 2 -> l.cx += doc.w - maxX }
                    when (v) { 0 -> l.cy += -minY; 1 -> l.cy += doc.h / 2f - (minY + maxY) / 2f; 2 -> l.cy += doc.h - maxY }
                    doc.commit(e.stepNames["move"] ?: "~")
                }
                ISmallBtn(e.alignL) { align(0, -1) }
                ISmallBtn(e.alignCH) { align(1, -1) }
                ISmallBtn(e.alignR) { align(2, -1) }
                ISmallBtn(e.alignT) { align(-1, 0) }
                ISmallBtn(e.alignCV) { align(-1, 1) }
                ISmallBtn(e.alignB) { align(-1, 2) }
            }
            ISlider(e.rotation, ((l.rot % 360f) + 540f) % 360f - 180f, -180f, 180f, { if (!l.locked) { l.rot = it; doc.touch() } }, { if (!l.locked) doc.commit(e.stepNames["move"] ?: "~") }, suffix = "°")
            ISlider(e.scale, l.uniformScale() * 100f, 5f, 400f, {
                if (!l.locked) {
                    val sgx = if (l.sx < 0) -1f else 1f; val sgy = if (l.sy < 0) -1f else 1f
                    l.sx = sgx * it / 100f; l.sy = sgy * it / 100f; doc.touch()
                }
            }, { if (!l.locked) doc.commit(e.stepNames["move"] ?: "~") }, suffix = "%")
        }
        ISlider(e.opacity, l.opacity * 100f, 0f, 100f, { l.opacity = it / 100f; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = "%")
        if (l.kind == LKind.FILL) IColorRow(l.fill, { l.fill = it; doc.commit(e.stepNames["edit"] ?: "~") }, { st.colorDlg = 2 })
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ISmallBtn(e.duplicate) { st.step("add") { doc.duplicate(l, e.copySuffix) } }
            ISmallBtn(e.delete) { st.deleteActive() }
            ISmallBtn(if (l.locked) e.unlock else e.lock) { l.locked = !l.locked; doc.commit(e.stepNames["edit"] ?: "~") }
            ISmallBtn(e.up) { st.step("order") { doc.move(l, true) } }
            ISmallBtn(e.down) { st.step("order") { doc.move(l, false) } }
        }
        Text(e.selectHint, color = LocalPalette.current.muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ---------------------------------------------------------------- text

@Composable
internal fun TextPanel(st: EdState) {
    val e = st.e
    val doc = st.doc
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev
    val l = doc.active()
    val t = if (l?.kind == LKind.TEXT) l.text else null
    IPanel {
        if (l == null || t == null) {
            ISmallBtn(e.addText) { st.safe { doc.addText(e.layerText); doc.commit(e.stepNames["add"] ?: "+"); st.textDlg = true } }
            return@IPanel
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ISmallBtn(e.editText) { st.textDlg = true }
            ISmallBtn(e.addText) { st.safe { doc.addText(e.layerText); doc.commit(e.stepNames["add"] ?: "+"); st.textDlg = true } }
            e.alignNames.forEachIndexed { i, n -> IChip(n, t.align == i) { t.align = i; doc.commit(e.stepNames["edit"] ?: "~") } }
        }
        ISlider(e.size, t.size, 8f, 400f, { t.size = it; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = " px")
        IColorRow(t.color, { t.color = it; doc.commit(e.stepNames["edit"] ?: "~") }, { st.colorDlg = 3 })
        ISlider(e.opacity, l.opacity * 100f, 0f, 100f, { l.opacity = it / 100f; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = "%")
        Text(e.selectHint, color = LocalPalette.current.muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ---------------------------------------------------------------- shapes

@Composable
internal fun ShapePanel(st: EdState) {
    val e = st.e
    val doc = st.doc
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev
    val l = doc.active()
    val s = if (l?.kind == LKind.SHAPE) l.shape else null
    IPanel {
        Text(e.newShape, color = LocalPalette.current.muted, fontSize = 13.sp)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            e.shapeNames.forEachIndexed { i, n -> IChip(n, false) { st.step("add") { doc.addShape(i, e.layerShape) } } }
        }
        if (l != null && s != null) {
            val isLine = s.kind == 3 || s.kind == 4
            if (!isLine) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IChip(e.fill, s.fillOn) { s.fillOn = !s.fillOn; doc.commit(e.stepNames["edit"] ?: "~") }
                }
                if (s.fillOn) IColorRow(s.fill, { s.fill = it; doc.commit(e.stepNames["edit"] ?: "~") }, { st.colorDlg = 4 })
            }
            Text(e.stroke, color = LocalPalette.current.muted, fontSize = 13.sp)
            IColorRow(s.stroke, { s.stroke = it; doc.commit(e.stepNames["edit"] ?: "~") }, { st.colorDlg = 5 })
            ISlider(e.strokeW, s.strokeW, 0f, 60f, { s.strokeW = it; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = " px")
            if (s.kind == 1) ISlider(e.radius, s.radius, 0f, 300f, { s.radius = it; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = " px")
            if (s.kind == 5) ISlider(e.sides, s.sides.toFloat(), 3f, 12f, { s.sides = it.toInt(); doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") })
            ISlider(e.opacity, l.opacity * 100f, 0f, 100f, { l.opacity = it / 100f; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = "%")
        }
        Text(e.selectHint, color = LocalPalette.current.muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ---------------------------------------------------------------- crop

@Composable
internal fun CropPanel(st: EdState) {
    val e = st.e
    val names = listOf(e.cropFree, e.cropOriginal, "1:1", "4:3", "3:2", "16:9", "9:16")
    IPanel {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            names.forEachIndexed { i, n -> IChip(n, st.cropRatio == i) { st.setCropRatio(i) } }
        }
        Text("${(st.cR - st.cL).toInt()} × ${(st.cB - st.cT).toInt()} px", color = LocalPalette.current.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ISmallBtn(e.reset) { st.initCrop() }
            ISmallBtn(e.cancel) { st.tool = ETool.SELECT }
            ISmallBtn(e.apply) { st.applyCrop() }
        }
        Text(e.cropHint, color = LocalPalette.current.muted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

// ---------------------------------------------------------------- layers

@Composable
internal fun LayersSheet(st: EdState, onAddImage: () -> Unit) {
    val p = LocalPalette.current
    val e = st.e
    val doc = st.doc
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev
    var addMenu by remember { mutableStateOf(false) }
    val act = doc.active()
    Column(Modifier.fillMaxWidth().height(330.dp).background(p.card)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(e.layersTitle, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Box {
                IIconBtn(Icons.Default.Add, tint = p.accent) { addMenu = true }
                DropdownMenu(expanded = addMenu, onDismissRequest = { addMenu = false }) {
                    DropdownMenuItem(onClick = { addMenu = false; onAddImage() }) { Text(e.addImage, color = p.text) }
                    DropdownMenuItem(onClick = { addMenu = false; st.step("add") { doc.addDraw(e.layerDraw) } }) { Text(e.addDraw, color = p.text) }
                    DropdownMenuItem(onClick = { addMenu = false; st.step("add") { doc.addText(e.layerText) } }) { Text(e.addTextL, color = p.text) }
                    DropdownMenuItem(onClick = { addMenu = false; st.step("add") { doc.addShape(0, e.layerShape) } }) { Text(e.addShapeL, color = p.text) }
                    DropdownMenuItem(onClick = { addMenu = false; st.step("add") { doc.addFill(st.brush.color, e.layerFill) } }) { Text(e.addFillL, color = p.text) }
                }
            }
            IIconBtn(AppIcons.ArrowDown) { st.sheet = 0 }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in doc.layers.indices.reversed()) {
                val l = doc.layers[i]
                val sel = l.id == doc.activeId
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(if (sel) p.accent.copy(alpha = 0.16f) else p.field)
                        .clickable { doc.activeId = l.id; doc.touch() }.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LayerThumb(l, 38.dp)
                    Text(l.name.ifEmpty { "-" }, color = p.text, fontSize = 14.sp, maxLines = 1, modifier = Modifier.weight(1f), fontWeight = if (sel) FontWeight.Bold else null)
                    IIconBtn(if (l.visible) AppIcons.Eye else AppIcons.EyeOff, size = 34.dp) { l.visible = !l.visible; doc.commit(e.stepNames["edit"] ?: "~") }
                    IIconBtn(if (l.locked) AppIcons.Lock else AppIcons.LockOpen, size = 34.dp) { l.locked = !l.locked; doc.commit(e.stepNames["edit"] ?: "~") }
                    IIconBtn(AppIcons.ArrowUp, size = 34.dp, enabled = i < doc.layers.size - 1) { st.step("order") { doc.move(l, true) } }
                    IIconBtn(AppIcons.ArrowDown, size = 34.dp, enabled = i > 0) { st.step("order") { doc.move(l, false) } }
                }
            }
            Text(e.layerHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(top = 4.dp, start = 4.dp))
            if (act != null) {
                ISlider(e.opacity, act.opacity * 100f, 0f, 100f, { act.opacity = it / 100f; doc.touch() }, { doc.commit(e.stepNames["edit"] ?: "~") }, suffix = "%")
                Text(e.blend, color = p.muted, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    e.blendNames.forEachIndexed { i, n -> IChip(n, act.blend == i) { act.blend = i; doc.commit(e.stepNames["edit"] ?: "~") } }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ISmallBtn(e.rename) { st.layerRename = act.id }
                    ISmallBtn(e.duplicate) { st.step("add") { doc.duplicate(act, e.copySuffix) } }
                    ISmallBtn(e.mergeDown, doc.layers.indexOf(act) > 0) { st.step("merge") { doc.mergeDown(act, e.layerMerged) } }
                    ISmallBtn(e.delete) { st.deleteActive() }
                    ISmallBtn(e.flatten, doc.layers.size > 1) { st.flattenDlg = true }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- more / history

@Composable
internal fun MoreSheet(st: EdState, onExport: () -> Unit, onSave: () -> Unit) {
    val p = LocalPalette.current
    val e = st.e
    val tiles = listOf<Pair<String, () -> Unit>>(
        e.rotL to { st.rotate(false) },
        e.rotR to { st.rotate(true) },
        e.flipH to { st.flip(true) },
        e.flipV to { st.flip(false) },
        e.straighten to { st.canvasDlg = 3 },
        e.scaleAll to { st.canvasDlg = 1 },
        e.canvasSizeBtn to { st.canvasDlg = 2 },
        e.canvasBg to { st.canvasDlg = 4 },
        (if (st.showGrid) "✓ " else "") + e.grid to { st.showGrid = !st.showGrid; st.savePrefs() },
        (if (st.snap) "✓ " else "") + e.snap to { st.snap = !st.snap; st.savePrefs() },
        e.history to { st.sheet = 3 },
        e.save to onSave,
        e.export to onExport,
    )
    Column(Modifier.fillMaxWidth().height(300.dp).background(p.card).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(e.tMore, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IIconBtn(AppIcons.ArrowDown) { st.sheet = 0 }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { (label, act) ->
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(p.field).clickable { act() }.padding(horizontal = 10.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart,
                        ) { Text(label, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2) }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Text(e.moreHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
internal fun HistorySheet(st: EdState) {
    val p = LocalPalette.current
    val e = st.e
    val doc = st.doc
    @Suppress("UNUSED_VARIABLE") val rev = doc.rev
    val labels = doc.historyLabels
    Column(Modifier.fillMaxWidth().height(300.dp).background(p.card).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(e.history, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IIconBtn(AppIcons.ArrowDown) { st.sheet = 0 }
        }
        if (labels.size <= 1) Text(e.historyEmpty, color = p.muted, fontSize = 13.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            for (i in labels.indices.reversed()) {
                val cur = i == doc.historyIndex
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (cur) p.accent.copy(alpha = 0.18f) else p.field)
                        .clickable { doc.jump(i) }.padding(horizontal = 10.dp, vertical = 9.dp),
                ) { Text("${i}. ${labels[i]}", color = if (cur) p.accent else p.text, fontSize = 13.sp, fontWeight = if (cur) FontWeight.Bold else null, maxLines = 1) }
            }
        }
    }
}
