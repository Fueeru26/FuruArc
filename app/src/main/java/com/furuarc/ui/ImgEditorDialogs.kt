package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.Doc
import com.furuarc.core.ImgExport
import com.furuarc.core.ImgStore
import com.furuarc.core.TextSpec
import com.furuarc.i18n.ImageEditStr
import com.furuarc.ui.theme.LocalPalette
import kotlin.math.max
import kotlin.math.roundToInt

// ---------------------------------------------------------------- text layer

/** Edits the text layer in place (the canvas behind the dialog updates live); Cancel restores the old settings. */
@Composable
internal fun TextDialog(st: EdState) {
    val e = st.e
    val doc = st.doc
    val ctx = LocalContext.current
    val layer = doc.active()
    val t = layer?.text
    if (layer == null || t == null) { st.textDlg = false; return }
    val orig = remember { t.copy() }
    var rev by remember { mutableStateOf(0) }
    var picking by remember { mutableStateOf(0) }
    fun ch() { rev++; doc.touch() }
    val fontPick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            val path = ImgStore.importFont(ctx, u)
            if (path != null) { t.fontPath = path; ch() } else st.toast(e.loadFail)
        }
    }
    @Suppress("UNUSED_VARIABLE") val r = rev
    val cancel = { layer.text = orig; doc.touch(); st.textDlg = false }
    AppDialog(e.editText, cancel, buttons = {
        DlgBtn(e.cancel, false, onClick = cancel)
        DlgBtn(e.ok, true) { st.textDlg = false; doc.commit(e.stepNames["edit"] ?: "~") }
    }) {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(t.text, { t.text = it; ch() }, e.textContent, multiline = true)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val keys = listOf("sans", "serif", "mono", "cursive", "casual")
                e.fontNames.forEachIndexed { i, n -> IChip(n, t.fontPath.isEmpty() && t.font == keys[i]) { t.font = keys[i]; t.fontPath = ""; ch() } }
                IChip(if (t.fontPath.isNotEmpty()) e.fontFileOn else e.fontFile, t.fontPath.isNotEmpty()) { fontPick.launch(arrayOf("*/*")) }
            }
            if (t.fontPath.isNotEmpty()) ISmallBtn(e.fontReset) { t.fontPath = ""; ch() }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IChip(e.bold, t.bold) { t.bold = !t.bold; ch() }
                IChip(e.italic, t.italic) { t.italic = !t.italic; ch() }
                IChip(e.underline, t.underline) { t.underline = !t.underline; ch() }
                e.alignNames.forEachIndexed { i, n -> IChip(n, t.align == i) { t.align = i; ch() } }
            }
            ISlider(e.size, t.size, 8f, 400f, { t.size = it; ch() }, suffix = " px")
            Text(e.color, color = LocalPalette.current.muted, fontSize = 13.sp)
            IColorRow(t.color, { t.color = it; ch() }, { picking = 1 })
            ISlider(e.boxWidth, t.boxW, 100f, max(2000f, doc.w * 2f), { t.boxW = it; ch() }, suffix = " px")
            ISlider(e.lineSpacing, t.lineSpacing, 0.6f, 3f, { t.lineSpacing = it; ch() }, decimals = 2)
            ISlider(e.letterSpacing, t.letterSpacing, -0.1f, 0.6f, { t.letterSpacing = it; ch() }, decimals = 2)
            ISlider(e.outlineW, t.strokeW, 0f, 40f, { t.strokeW = it; ch() }, suffix = " px")
            if (t.strokeW > 0f) IColorRow(t.strokeColor, { t.strokeColor = it; ch() }, { picking = 2 })
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IChip(e.shadow, t.shadow) { t.shadow = !t.shadow; ch() }
                IChip(e.textBg, t.bgOn) { t.bgOn = !t.bgOn; ch() }
            }
            if (t.bgOn) IColorRow(t.bgColor, { t.bgColor = it; ch() }, { picking = 3 })
        }
    }
    if (picking != 0) {
        val init = when (picking) { 1 -> t.color; 2 -> t.strokeColor; else -> t.bgColor }
        IColorDialog(init, e, { picking = 0 }) { c -> when (picking) { 1 -> t.color = c; 2 -> t.strokeColor = c; else -> t.bgColor = c }; picking = 0; ch() }
    }
}

// ---------------------------------------------------------------- canvas dialogs

@Composable
internal fun CanvasDialogs(st: EdState) {
    val e = st.e
    val doc = st.doc
    val p = LocalPalette.current
    val kind = st.canvasDlg
    if (kind == 0) return
    val close = { st.canvasDlg = 0 }
    when (kind) {
        1 -> { // scale everything
            var w by remember { mutableStateOf(doc.w.toString()) }
            AppDialog(e.scaleAll, close, buttons = {
                DlgBtn(e.cancel, false, onClick = close)
                DlgBtn(e.ok, true, enabled = (w.toIntOrNull() ?: 0) in 1..Doc.MAX_SIDE) {
                    val nw = w.toInt(); close()
                    st.step("canvas") { doc.scaleAll(nw / doc.w.toFloat()) }; st.fit()
                }
            }) {
                AppTextField(w, { w = it.filter { c -> c.isDigit() } }, "${e.width} (max ${Doc.MAX_SIDE})", number = true)
                val nw = w.toIntOrNull() ?: 0
                Text("${doc.w} × ${doc.h}  →  $nw × ${(doc.h * nw / doc.w.toFloat()).roundToInt()}", color = p.muted, fontSize = 13.sp)
            }
        }
        2 -> { // new canvas size, content not scaled
            var w by remember { mutableStateOf(doc.w.toString()) }
            var h by remember { mutableStateOf(doc.h.toString()) }
            val ok = (w.toIntOrNull() ?: 0) in 1..Doc.MAX_SIDE && (h.toIntOrNull() ?: 0) in 1..Doc.MAX_SIDE
            AppDialog(e.expandCanvas, close, buttons = {
                DlgBtn(e.cancel, false, onClick = close)
                DlgBtn(e.ok, true, enabled = ok) {
                    val nw = w.toInt(); val nh = h.toInt(); close()
                    st.step("canvas") { doc.resizeCanvas(nw, nh) }; st.fit()
                }
            }) {
                AppTextField(w, { w = it.filter { c -> c.isDigit() } }, "${e.width} (max ${Doc.MAX_SIDE})", number = true)
                AppTextField(h, { h = it.filter { c -> c.isDigit() } }, "${e.height} (max ${Doc.MAX_SIDE})", number = true)
            }
        }
        3 -> { // straighten
            var a by remember { mutableStateOf(0f) }
            AppDialog(e.straighten, close, buttons = {
                DlgBtn(e.cancel, false, onClick = close)
                DlgBtn(e.ok, true, enabled = a != 0f) { val v = a; close(); st.step("rotate") { doc.straighten(v) } }
            }) {
                ISlider(e.angle, a, -45f, 45f, { a = it }, decimals = 1, suffix = "°")
                Text(e.moreHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        else -> { // canvas background colour
            var picking by remember { mutableStateOf(false) }
            AppDialog(e.canvasBg, close, buttons = { DlgBtn(e.close, true, onClick = close) }) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IChip(e.transparent, doc.bg == 0) { doc.bg = 0; doc.commit(e.stepNames["canvas"] ?: "~") }
                }
                IColorRow(if (doc.bg == 0) 0xFFFFFFFF.toInt() else doc.bg, { doc.bg = it; doc.commit(e.stepNames["canvas"] ?: "~") }, { picking = true })
            }
            if (picking) IColorDialog(if (doc.bg == 0) 0xFFFFFFFF.toInt() else doc.bg, e, { picking = false }) { c -> doc.bg = c; picking = false; doc.commit(e.stepNames["canvas"] ?: "~") }
        }
    }
}

// ---------------------------------------------------------------- names

@Composable
internal fun NameDialog(title: String, initial: String, e: ImageEditStr, onDismiss: () -> Unit, onOk: (String) -> Unit) {
    var v by remember { mutableStateOf(initial) }
    AppDialog(title, onDismiss, buttons = {
        DlgBtn(e.cancel, false, onClick = onDismiss)
        DlgBtn(e.ok, true, enabled = v.isNotBlank()) { onOk(v.trim()) }
    }) { AppTextField(v, { v = it }, title) }
}

// ---------------------------------------------------------------- export

internal class ExportParams(val fmt: Int, val quality: Int, val width: Int, val transparent: Boolean, val name: String)

@Composable
internal fun ExportDialog(st: EdState, onChoose: (ExportParams) -> Unit) {
    val e = st.e
    val doc = st.doc
    val ctx = LocalContext.current
    val pr = remember { ImgStore.prefs(ctx) }
    var fmt by remember { mutableStateOf(pr.getInt("x_fmt", ImgExport.PNG)) }
    var quality by remember { mutableStateOf(pr.getFloat("x_q", 92f)) }
    var custom by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(doc.w.toString()) }
    var transparent by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(doc.name.ifEmpty { "image" }) }
    val w = if (custom) (width.toIntOrNull() ?: 0) else doc.w
    val ok = name.isNotBlank() && w in 1..8192
    val close = { st.exportDlg = false }
    AppDialog(e.exportTitle, close, buttons = {
        DlgBtn(e.cancel, false, onClick = close)
        DlgBtn(e.exportBtn, true, enabled = ok) {
            pr.edit().putInt("x_fmt", fmt).putFloat("x_q", quality).apply()
            close()
            onChoose(ExportParams(fmt, quality.roundToInt(), w, transparent && fmt != ImgExport.JPG, name.trim()))
        }
    }) {
        Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(name, { name = it }, e.fileName)
            Text(e.format, color = LocalPalette.current.muted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("PNG", "JPEG", "WebP").forEachIndexed { i, n -> IChip(n, fmt == i) { fmt = i } }
            }
            if (fmt != ImgExport.PNG) ISlider(e.quality, quality, 10f, 100f, { quality = it }, suffix = "%")
            Text(e.exportSize, color = LocalPalette.current.muted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IChip(e.originalSize + " (${doc.w}×${doc.h})", !custom) { custom = false }
                IChip("…", custom) { custom = true }
            }
            if (custom) {
                AppTextField(width, { width = it.filter { c -> c.isDigit() } }, e.exportSize, number = true)
                Text("${w} × ${(doc.h * w / doc.w.toFloat()).roundToInt()}", color = LocalPalette.current.muted, fontSize = 13.sp)
            }
            if (fmt != ImgExport.JPG) IChip(e.transparentBg, transparent) { transparent = !transparent }
        }
    }
}

// ---------------------------------------------------------------- new project

@Composable
internal fun NewProjectDialog(e: ImageEditStr, onDismiss: () -> Unit, onCreate: (name: String, w: Int, h: Int, bg: Int) -> Unit) {
    val ctx = LocalContext.current
    val presets = listOf(1080 to 1080, 1080 to 1350, 1920 to 1080, 1080 to 1920, 2480 to 3508)
    var name by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("1080") }
    var h by remember { mutableStateOf("1080") }
    var bg by remember { mutableStateOf(0xFFFFFFFF.toInt()) }
    var picking by remember { mutableStateOf(false) }
    val wi = w.toIntOrNull() ?: 0
    val hi = h.toIntOrNull() ?: 0
    val ok = wi in 1..Doc.MAX_SIDE && hi in 1..Doc.MAX_SIDE
    AppDialog(e.newTitle, onDismiss, buttons = {
        DlgBtn(e.cancel, false, onClick = onDismiss)
        DlgBtn(e.create, true, enabled = ok) { onCreate(name.trim(), wi, hi, bg) }
    }) {
        Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextField(name, { name = it }, e.projName)
            Text(e.canvasSize, color = LocalPalette.current.muted, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.forEach { (pw, ph) -> IChip("$pw × $ph", wi == pw && hi == ph) { w = pw.toString(); h = ph.toString() } }
            }
            AppTextField(w, { w = it.filter { c -> c.isDigit() } }, "${e.width} (max ${Doc.MAX_SIDE})", number = true)
            AppTextField(h, { h = it.filter { c -> c.isDigit() } }, "${e.height} (max ${Doc.MAX_SIDE})", number = true)
            Text(e.bgColor, color = LocalPalette.current.muted, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { IChip(e.transparent, bg == 0) { bg = 0 } }
            IColorRow(if (bg == 0) 0xFFFFFFFF.toInt() else bg, { bg = it }, { picking = true })
        }
    }
    if (picking) IColorDialog(if (bg == 0) 0xFFFFFFFF.toInt() else bg, e, { picking = false }) { c -> bg = c; picking = false }
}
