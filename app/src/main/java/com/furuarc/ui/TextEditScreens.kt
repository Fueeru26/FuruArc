package com.furuarc.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.core.Diff
import com.furuarc.core.Highlight
import com.furuarc.core.NotTextException
import com.furuarc.core.TextTooBigException
import com.furuarc.core.TxtDoc
import com.furuarc.i18n.TextEditStr
import com.furuarc.i18n.mediaStr
import com.furuarc.i18n.textEditStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** File Text Editor menu (Support Tools): Plain Text Files, Structured Text Files, and Bulk Replace. */
@Composable
fun TextEditMenuScreen(onPlain: () -> Unit, onStruct: () -> Unit, onBulk: () -> Unit, onBack: () -> Unit) {
    val te = textEditStr(LocalLang.current)
    MenuListScreen(
        te.menuTitle,
        listOf(
            MenuSpec(AppIcons.TextFields, te.plainTitle, te.plainSub, onPlain),
            MenuSpec(AppIcons.Layers, te.structTitle, te.structSub, onStruct),
            MenuSpec(AppIcons.SwapHoriz, te.bulkTitle, te.bulkSub, onBulk),
        ),
        onBack,
    )
}

/** Small text button for the dialog button row. */
@Composable
internal fun DlgBtn(text: String, accent: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val p = LocalPalette.current
    TextButton(onClick = onClick, enabled = enabled) {
        OneLineText(text, fontSize = 14.sp, fontWeight = if (accent) FontWeight.Bold else null, color = if (accent) p.accent else p.text)
    }
}

internal class OpenResult(val bytes: ByteArray?, val loaded: TxtDoc.Loaded?, val error: String?, val readOnly: Boolean = false)

/** Large files (above the edit limit) are opened read-only, page by page. */
private fun openReadOnly(ctx: android.content.Context, te: TextEditStr, item: MediaItem, encPref: String, size: Long): OpenResult = try {
    val all = readCapped(ctx, item.uri, TxtDoc.READ_LIMIT)
    if (TxtDoc.looksBinary(all)) throw NotTextException()
    val head = all.copyOfRange(0, minOf(all.size, 262144))
    val ld = TxtDoc.decode(head, encPref)
    if (ld.enc.startsWith("utf16")) OpenResult(null, null, te.tooBig(formatSize(size), formatSize(TxtDoc.EDIT_LIMIT)))
    else OpenResult(all, ld, null, true)
} catch (e: NotTextException) {
    OpenResult(null, null, te.notText)
} catch (e: OutOfMemoryError) {
    OpenResult(null, null, te.tooBig(formatSize(size), formatSize(TxtDoc.EDIT_LIMIT)))
} catch (e: Exception) {
    OpenResult(null, null, te.openFail(e.message ?: e.javaClass.simpleName))
} catch (e: Throwable) {
    OpenResult(null, null, te.openFail(e.javaClass.simpleName))
}

/** Read and decode a file for the File Text Editor (IO thread). All errors, including out of memory, become messages (the app does not close). */
internal fun openTextFile(ctx: android.content.Context, te: TextEditStr, item: MediaItem, encPref: String, allowReadOnly: Boolean = false): OpenResult = try {
    val bytes = readCapped(ctx, item.uri, TxtDoc.EDIT_LIMIT)
    if (TxtDoc.looksBinary(bytes)) throw NotTextException()
    OpenResult(bytes, TxtDoc.decode(bytes, encPref), null)
} catch (e: TextTooBigException) {
    val real = if (item.size > 0) item.size else queryNameSize(ctx, item.uri).second
    if (allowReadOnly && real <= TxtDoc.READ_LIMIT) openReadOnly(ctx, te, item, encPref, real)
    else OpenResult(null, null, te.tooBig(formatSize(real), formatSize(TxtDoc.EDIT_LIMIT)))
} catch (e: NotTextException) {
    OpenResult(null, null, te.notText)
} catch (e: OutOfMemoryError) {
    OpenResult(null, null, te.tooBig(formatSize(item.size), formatSize(TxtDoc.EDIT_LIMIT)))
} catch (e: Exception) {
    OpenResult(null, null, te.openFail(e.message ?: e.javaClass.simpleName))
} catch (e: Throwable) {
    OpenResult(null, null, te.openFail(e.javaClass.simpleName))
}

internal class PlainDoc(
    val item: MediaItem, val created: Boolean, bytes0: ByteArray, loaded0: TxtDoc.Loaded, val serial: Int,
    val readOnly: Boolean = false, val pages: IntArray = IntArray(0),
) {
    var bytes by mutableStateOf(bytes0)
    var loaded by mutableStateOf(loaded0)
    /** Last saved/opened text, the baseline for "Lihat perubahan". */
    var base: String = if (readOnly) "" else loaded0.text
    var page by mutableStateOf(0)
    val memo = SaveMemo()
    val fmt = DocFormat()
    var outName by mutableStateOf(item.name)
}

@Composable
private fun DiffRow(l: Diff.Line) {
    val p = LocalPalette.current
    val bg = when (l.kind) {
        1 -> OkColor.copy(alpha = 0.18f)
        2 -> ErrColor.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    val mark = when (l.kind) {
        1 -> "+ "
        2 -> "− "
        3 -> "··· "
        else -> "  "
    }
    Text(
        mark + l.text, color = if (l.kind == 1 || l.kind == 2) p.text else p.muted, fontSize = 12.sp,
        fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth().background(bg).padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

internal const val PLAIN_EXTS = "txt, ini, cfg, conf, log, md, csv, xml, html, css, js, yaml, yml, ks, tjs, rpy, ast, lua, py, sh"

/** Split "txt, ini, ks" into a list of lowercase extensions. */
internal fun extList(text: String): List<String> =
    text.split(',', ' ', ';').map { it.trim().lowercase().removePrefix(".") }.filter { it.isNotEmpty() }.distinct()

/**
 * Plain Text Files (also used for XML and Markdown): the Create File / Upload File panel on top, then the "Text Editor Area"
 * below it on the same page (without opening a new full page).
 */
@Composable
fun PlainTextScreen(onBack: () -> Unit, title: String? = null, exts: String? = null, forcedKind: String? = null, xmlTools: Boolean = false) {
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val te = remember(lang) { textEditStr(lang) }
    val m = remember(lang) { mediaStr(lang) }
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    val ep = rememberEditorPrefs()
    val fold = remember { FoldState() }
    val ctl = remember { EditorCtl() }
    val hist = remember { TextHistory() }
    val find = remember(te) { FindState(te, ctl) }
    val panel = remember { TePanel(extList(exts ?: PLAIN_EXTS)) }
    val pageScroll = rememberScrollState()
    val areaH = editorAreaHeight()
    var doc by remember { mutableStateOf<PlainDoc?>(null) }
    var serial by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var dirty by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var selPos by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("") }
    var gotoOpen by remember { mutableStateOf(false) }
    var optOpen by remember { mutableStateOf(false) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReload by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var saving by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf(false) }
    var previewSrc by remember { mutableStateOf("") }
    var diffRes by remember { mutableStateOf<Diff.Result?>(null) }
    var draftOffer by remember { mutableStateOf<String?>(null) }
    val pickOut = rememberOutPicker { u, n -> ep.out = u; ep.outName = n }

    fun resetEditorState() {
        dirty = false; note = null; find.open = false; find.msg = null; ctl.et = null; ctl.pending = null
        statusText = ""; preview = false; draftOffer = null; tick = 0; selPos = 0
    }

    /** Attach a new document to the Text Editor Area. */
    fun showDoc(nd: PlainDoc, first: String) {
        resetEditorState()
        ctl.pending = first
        hist.reset(first)
        doc = nd
    }

    fun openItem(item: MediaItem) {
        if (busy) return
        busy = true; err = null; note = null
        scope.launch {
            // all heavy work (read, decode, page count) on the IO thread
            val res = withContext(Dispatchers.IO) {
                val o = openTextFile(ctx, te, item, ep.enc, true)
                val ld = o.loaded
                val b = o.bytes
                var pages = IntArray(0)
                var first = ld?.text ?: ""
                if (o.readOnly && ld != null && b != null) {
                    try {
                        pages = TxtDoc.pageStarts(b)
                        first = TxtDoc.pageText(b, pages, 0, ld.enc)
                    } catch (t: Throwable) {
                        pages = IntArray(0); first = ""
                    }
                }
                Triple(o, pages, first)
            }
            busy = false
            val r = res.first
            val ld = r.loaded
            val bytes = r.bytes
            if (ld == null || bytes == null) {
                err = r.error
            } else {
                serial++
                val nd = PlainDoc(item, false, bytes, ld, serial, r.readOnly, res.second)
                showDoc(nd, res.third)
                if (!r.readOnly) {
                    val dr = Drafts.load(ctx, item.uri.toString())
                    if (dr != null && dr != ld.text) draftOffer = dr
                }
            }
        }
    }

    fun createItem(fileName: String) {
        if (busy) return
        busy = true; err = null; note = null
        val content = newFileContent(fileName.substringAfterLast('.', "").lowercase())
        scope.launch {
            val cf = withContext(Dispatchers.IO) { createTextFile(ctx, te, ep, fileName, content.toByteArray(Charsets.UTF_8), m.needOut) }
            busy = false
            val uri = cf.uri
            if (!cf.ok || uri == null) {
                err = cf.msg
            } else {
                serial++
                val ld = TxtDoc.Loaded(content, "utf8", false, false)
                val nd = PlainDoc(MediaItem(uri, cf.name, "", content.length.toLong()), true, content.toByteArray(Charsets.UTF_8), ld, serial)
                nd.memo.savedUri = uri
                showDoc(nd, content)
                note = Pair(cf.msg, true)
            }
        }
    }

    /** Replace the currently open document: if there are unsaved changes, ask first. */
    fun guarded(action: () -> Unit) {
        if (doc != null && dirty) pendingStart = action else action()
    }

    fun reload() {
        val d = doc ?: return
        if (d.readOnly) return
        val ld = TxtDoc.decode(d.bytes, ep.enc)
        d.loaded = ld
        d.base = ld.text
        ctl.setText(ld.text, keepSel = false)
        hist.reset(ld.text)
        dirty = false; tick++
    }

    fun undo() { val s = hist.undo(ctl.text()); if (s != null) { ctl.setText(s); dirty = true } }
    fun redo() { val s = hist.redo(); if (s != null) { ctl.setText(s); dirty = true } }

    fun tidy() {
        val r = tidyXml(ctl.text(), te)
        val out = r.first
        if (out == null) {
            note = Pair(r.second ?: "", false)
        } else {
            ctl.setText(out, keepSel = false)
            dirty = true; tick++
            note = Pair(te.xmlTidyDone, true)
        }
    }

    fun showDiff() {
        val d = doc ?: return
        val cur = ctl.text()
        val baseText = d.base
        scope.launch {
            diffRes = withContext(Dispatchers.Default) { Diff.lines(baseText.split("\n"), cur.split("\n")) }
        }
    }

    fun save() {
        val d = doc ?: return
        if (saving || d.readOnly) return
        val text = ctl.text()
        val prep = encodeForSave(te, text, d.loaded, d.fmt)
        val bytes = prep.first
        if (bytes == null) { note = Pair(prep.second ?: "", false); return }
        saving = true; note = null
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                teSaveDoc(ctx, te, ep, d.item, d.created, bytes, d.outName.ifBlank { d.item.name }, d.memo, m.needOut)
            }
            saving = false
            note = Pair(res.msg, res.ok)
            if (res.ok) {
                dirty = false; d.bytes = bytes; d.base = text
                Drafts.clear(ctx, d.item.uri.toString())
            }
        }
    }

    val requestBack: () -> Unit = {
        if (doc != null && dirty) confirmBack = true else onBack()
    }
    BackHandler(enabled = true) { requestBack() }

    // undo history: save after typing pauses briefly
    LaunchedEffect(tick) {
        if (tick > 0) { delay(700); hist.commit(ctl.text()) }
    }
    // auto draft: write 2 seconds after the last change
    LaunchedEffect(tick, dirty) {
        val dd = doc
        if (dd != null && !dd.readOnly && dirty && tick > 0) {
            delay(2000)
            val t = ctl.text()
            withContext(Dispatchers.IO) { Drafts.save(ctx, dd.item.uri.toString(), t) }
        }
    }
    // cursor position in the status bar
    LaunchedEffect(selPos, tick, doc) {
        if (doc != null) {
            delay(150)
            val cs = ctl.et?.text
            if (cs != null) {
                val lc = TxtDoc.lineCol(cs, selPos)
                statusText = te.status(lc.first, lc.second, cs.length)
            }
        }
    }
    // when the Text Editor Area first appears, scroll the page to it
    LaunchedEffect(doc?.serial) {
        if (doc != null) { delay(150); pageScroll.animateScrollTo(pageScroll.maxValue) }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(title = title ?: te.plainTitle, onBack = requestBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(pageScroll),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            TePanelUi(
                panel, ep, te, fold, busy, pickOut, null,
                onCreate = { name -> guarded { createItem(name) } },
                onEdit = { item -> guarded { openItem(item) } },
            )
            if (busy) Hint(te.opening)
            val e = err
            if (e != null) Text(e, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
            val d = doc
            if (d != null) {
                AreaTitle(te.editorArea)
                AreaFileName(d.item.name, dirty)
                val kind = forcedKind ?: Highlight.kindFor(d.item.name)
                Column(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!d.readOnly) {
                            Box(Modifier.weight(0.7f)) { SecondaryButton("↶") { undo() } }
                            Box(Modifier.weight(0.7f)) { SecondaryButton("↷") { redo() } }
                        }
                        Box(Modifier.weight(1.2f)) { SecondaryButton(te.findBtn) { find.open = !find.open } }
                        Box(Modifier.weight(1.2f)) { SecondaryButton(te.lineBtn) { gotoOpen = true } }
                        Box(Modifier.weight(1.2f)) { SecondaryButton(te.optBtn) { optOpen = true } }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (d.readOnly) {
                        Hint(te.readOnlyNote(formatSize(d.bytes.size.toLong())))
                        if (d.pages.size > 1) {
                            PageNav(d.page, d.pages.size, onPage = { np ->
                                d.page = np
                                ctl.setText(TxtDoc.pageText(d.bytes, d.pages, np, d.loaded.enc), keepSel = false)
                                selPos = 0
                            })
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (xmlTools && !d.readOnly) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                SecondaryButton(te.xmlCheck) {
                                    val r = checkXml(ctl.text(), te)
                                    note = Pair(r ?: te.xmlOk, r == null)
                                }
                            }
                            Box(Modifier.weight(1f)) { SecondaryButton(te.xmlTidy) { tidy() } }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (kind == "md") {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                SecondaryButton(if (preview) te.menuEdit else te.mdPreview) {
                                    if (!preview) previewSrc = ctl.text()
                                    preview = !preview
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    if (find.open) {
                        FindPanel(te, find, d.readOnly) { if (find.replaceAll()) { dirty = true; tick++ } }
                        Spacer(Modifier.height(8.dp))
                    }
                    val nt = note
                    if (nt != null) {
                        NoteCard(nt, te.close) { note = null }
                        Spacer(Modifier.height(8.dp))
                    }
                    Box(Modifier.height(areaH).fillMaxWidth()) {
                        key(d.serial) {
                            CodeEditor(
                                ctl, ep.font, ep.wrap, Modifier.fillMaxSize(),
                                onChanged = { dirty = true; tick++ },
                                onSelection = { a, _ -> selPos = a },
                                kind = kind, highlight = ep.hl, showLines = ep.lineNo, showWs = ep.showWs, readOnly = d.readOnly,
                            )
                        }
                        if (preview) MarkdownPreview(previewSrc, Modifier.fillMaxSize())
                    }
                    if (ep.symbols && !d.readOnly) {
                        Spacer(Modifier.height(6.dp))
                        SymbolBar(ctl)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        statusText + (if (statusText.isEmpty()) "" else " · ") + encDisplay(te, d.loaded) + " · " + (if (d.loaded.crlf) "CRLF" else "LF"),
                        color = p.muted, fontSize = 12.sp, maxLines = 1,
                    )
                    Spacer(Modifier.height(8.dp))
                    if (!d.readOnly) {
                        PrimaryButton(if (saving) te.saving else te.saveBtn, !saving && teCanSave(ep, d.created, d.memo)) { save() }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (gotoOpen) {
        var txt by remember { mutableStateOf("") }
        val total = remember { ctl.et?.text?.let { TxtDoc.lineCount(it) } ?: 1 }
        AppDialog(
            te.gotoTitle, { gotoOpen = false },
            buttons = {
                DlgBtn(te.cancel, false) { gotoOpen = false }
                DlgBtn(te.go, true) {
                    val n = txt.toIntOrNull()
                    if (n != null) {
                        val off = TxtDoc.lineOffset(ctl.text(), n)
                        ctl.select(off, off)
                    }
                    gotoOpen = false
                }
            },
        ) {
            AppTextField(value = txt, onValueChange = { txt = it.filter { c -> c.isDigit() }.take(9) }, label = te.lineNumber, number = true)
            Hint(te.lineRange(total))
        }
    }

    if (optOpen && doc != null) {
        val d = doc!!
        AppDialog(te.optTitle, { optOpen = false }, buttons = { DlgBtn(te.close, true) { optOpen = false } }) {
            Column(
                Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ScreenGap),
            ) {
                if (!d.readOnly) SecondaryButton(te.viewChanges) { optOpen = false; showDiff() }
                FoldSection(fold, "look", te.secLook) { EditorLookBlock(ep, te) }
                if (!d.readOnly) {
                    FoldSection(fold, "enc", te.secEnc) {
                        OpenEncBlock(ep, te) { if (dirty) confirmReload = true else reload() }
                        SaveFormatBlock(te, d.fmt)
                    }
                    if (!d.created) {
                        FoldSection(fold, "out", m.secOut) {
                            EditorOutBlock(ep, te, pickOut, true, d.outName) { d.outName = it }
                        }
                    }
                }
            }
        }
    }

    val dr = diffRes
    if (dr != null) {
        AppDialog(te.diffTitle, { diffRes = null }, buttons = { DlgBtn(te.close, true) { diffRes = null } }) {
            if (dr.tooBig) {
                Hint(te.diffTooBig)
            } else if (dr.lines.isEmpty()) {
                Hint(te.diffNone)
            } else {
                Hint(te.diffSummary(dr.added, dr.removed))
                LazyColumn(Modifier.heightIn(max = 380.dp).fillMaxWidth()) {
                    items(dr.lines.size) { i -> DiffRow(dr.lines[i]) }
                }
            }
        }
    }

    val offer = draftOffer
    if (offer != null) {
        ConfirmDialog(
            te.draftTitle, te.draftMsg, te.draftRestore, te.draftIgnore,
            {
                ctl.setText(offer, keepSel = false)
                dirty = true; tick++
                note = Pair(te.draftRestored, true)
                draftOffer = null
            },
            { draftOffer = null },
        )
    }

    if (confirmBack) {
        ConfirmDialog(
            te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel,
            { confirmBack = false; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; onBack() },
            { confirmBack = false },
        )
    }
    val ps = pendingStart
    if (ps != null) {
        ConfirmDialog(
            te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel,
            { pendingStart = null; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; ps() },
            { pendingStart = null },
        )
    }
    if (confirmReload) {
        ConfirmDialog(te.reloadTitle, te.reloadMsg, te.reloadYes, te.cancel, { confirmReload = false; reload() }, { confirmReload = false })
    }
}
