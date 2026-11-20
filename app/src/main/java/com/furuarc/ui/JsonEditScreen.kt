package com.furuarc.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.core.JNum
import com.furuarc.core.Json
import com.furuarc.core.JsonTree
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

/** The currently open JSON document: tree (root), current path, and undo/redo history based on text copies. */
internal class JsonDoc(val item: MediaItem, val created: Boolean, bytes0: ByteArray, loaded0: TxtDoc.Loaded, root0: Any?, valid0: Boolean, val serial: Int) {
    var bytes by mutableStateOf(bytes0)
    var loaded by mutableStateOf(loaded0)
    var root by mutableStateOf<Any?>(root0, referentialEqualityPolicy())
    var valid by mutableStateOf(valid0)
    var rev by mutableStateOf(0)
    var path by mutableStateOf<List<Any>>(emptyList())
    /** true while the tree is unchanged: the original text (original formatting) is used for the Text view and saving. */
    var pristine = true
    val memo = SaveMemo()
    val fmt = DocFormat()
    var outName by mutableStateOf(item.name)

    private val undoS = ArrayList<String>()
    private val redoS = ArrayList<String>()
    private var chars = 0L

    fun pushUndo() {
        if (!valid) return
        val s = Json.write(root, false)
        undoS.add(s); chars += s.length
        while ((undoS.size > 40 || chars > 24_000_000L) && undoS.size > 1) chars -= undoS.removeAt(0).length
        redoS.clear()
    }

    private fun fixPath() {
        while (path.isNotEmpty() && (!JsonTree.exists(root, path) || !JsonTree.isContainer(JsonTree.get(root, path)))) path = path.dropLast(1)
    }

    fun undo(): Boolean {
        if (undoS.isEmpty()) return false
        redoS.add(Json.write(root, false))
        val s = undoS.removeAt(undoS.size - 1)
        chars -= s.length
        root = Json.parse(s); rev++; pristine = false; fixPath()
        return true
    }

    fun redo(): Boolean {
        if (redoS.isEmpty()) return false
        undoS.add(Json.write(root, false))
        root = Json.parse(redoS.removeAt(redoS.size - 1)); rev++; pristine = false; fixPath()
        return true
    }
}

private fun keyLabel(k: Any): String = if (k is Int) "[$k]" else k.toString()

/** Structured Text File: edit JSON as a tree or as text. */
@Composable
fun JsonEditScreen(onBack: () -> Unit) {
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
    val panel = remember { TePanel(listOf("json")) }
    val pageScroll = rememberScrollState()
    val areaH = editorAreaHeight(0.75f)
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var doc by remember { mutableStateOf<JsonDoc?>(null) }
    var serial by remember { mutableStateOf(0) }
    var view by remember { mutableStateOf(0) } // 0 = tree, 1 = text
    var textSerial by remember { mutableStateOf(0) }
    var textEdited by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var dirty by remember { mutableStateOf(false) }
    var tick by remember { mutableStateOf(0) }
    var selPos by remember { mutableStateOf(0) }
    var statusText by remember { mutableStateOf("") }
    var optOpen by remember { mutableStateOf(false) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReload by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var draftOffer by remember { mutableStateOf<String?>(null) }
    val pickOut = rememberOutPicker { u, n -> ep.out = u; ep.outName = n }

    fun resetState() {
        dirty = false; note = null; find.open = false; find.msg = null; ctl.et = null; ctl.pending = null; statusText = ""; textEdited = false
        tick = 0; selPos = 0; draftOffer = null
    }

    fun enterText(d: JsonDoc) {
        val t = if (d.pristine || !d.valid) d.loaded.text else Json.write(d.root, true)
        ctl.et = null; ctl.pending = t
        hist.reset(t); textSerial++; textEdited = false; selPos = 0; statusText = ""
        find.open = false; find.msg = null
        view = 1
    }

    fun openItem(item: MediaItem) {
        if (busy) return
        busy = true; err = null; note = null
        scope.launch {
            // read, decode, and parse JSON on the IO thread
            val res = withContext(Dispatchers.IO) {
                val o = openTextFile(ctx, te, item, ep.enc)
                val ld = o.loaded
                var root: Any? = null
                var valid = true
                var perr: String? = null
                if (ld != null) {
                    try { root = Json.parse(ld.text) } catch (e: Exception) { valid = false; perr = e.message } catch (t: Throwable) { valid = false; perr = t.javaClass.simpleName }
                }
                Triple(o, root, if (valid) null else (perr ?: ""))
            }
            busy = false
            val r = res.first
            val ld = r.loaded
            val bytes = r.bytes
            if (ld == null || bytes == null) {
                err = r.error
            } else {
                val root = res.second
                val perr = res.third
                val valid = perr == null
                resetState()
                serial++
                val d = JsonDoc(item, false, bytes, ld, root, valid, serial)
                doc = d
                val dr0 = Drafts.load(ctx, item.uri.toString())
                if (dr0 != null && dr0 != ld.text) draftOffer = dr0
                if (!valid || !JsonTree.isContainer(root) || ep.jsonView == 1) {
                    enterText(d)
                    if (!valid) note = Pair(perr ?: "", false)
                    else if (!JsonTree.isContainer(root)) note = Pair(te.rootScalar, false)
                } else {
                    view = 0
                }
            }
        }
    }

    fun createItem(fileName: String) {
        if (busy) return
        busy = true; err = null; note = null
        val content = newFileContent("json")
        scope.launch {
            val cf = withContext(Dispatchers.IO) { createTextFile(ctx, te, ep, fileName, content.toByteArray(Charsets.UTF_8), m.needOut) }
            busy = false
            val uri = cf.uri
            if (!cf.ok || uri == null) {
                err = cf.msg
            } else {
                resetState()
                serial++
                val d = JsonDoc(MediaItem(uri, cf.name, "", content.length.toLong()), true, content.toByteArray(Charsets.UTF_8), TxtDoc.Loaded(content, "utf8", false, false), Json.parse(content), true, serial)
                d.memo.savedUri = uri
                doc = d
                view = if (ep.jsonView == 1) 1 else 0
                if (view == 1) enterText(d)
                note = Pair(cf.msg, true)
            }
        }
    }

    fun guarded(action: () -> Unit) {
        if (doc != null && dirty) pendingStart = action else action()
    }

    fun switchView(to: Int) {
        val d = doc ?: return
        if (to == view) return
        if (to == 1) {
            enterText(d)
        } else {
            if (textEdited || !d.valid) {
                val parsed = try { Json.parse(ctl.text()) } catch (e: Exception) { note = Pair(e.message ?: "", false); return }
                if (!JsonTree.isContainer(parsed)) { note = Pair(te.rootScalar, false); return }
                d.pushUndo()
                d.root = parsed; d.valid = true; d.pristine = false; d.rev++; d.path = emptyList()
            }
            view = 0
        }
    }

    fun mutate(block: () -> Unit) {
        val d = doc ?: return
        d.pushUndo()
        block()
        d.pristine = false
        d.rev++
        dirty = true
    }

    fun reload() {
        val d = doc ?: return
        val ld = TxtDoc.decode(d.bytes, ep.enc)
        d.loaded = ld
        var perr: String? = null
        try { d.root = Json.parse(ld.text); d.valid = true } catch (e: Exception) { d.valid = false; perr = e.message }
        d.pristine = true; d.path = emptyList(); d.rev++
        dirty = false; note = perr?.let { Pair(it, false) }
        if (view == 1 || !d.valid) enterText(d)
    }

    fun prettify() {
        try {
            val r = Json.parse(ctl.text())
            ctl.setText(Json.write(r, true))
            dirty = true; textEdited = true; tick++
        } catch (e: Exception) {
            note = Pair(e.message ?: "", false)
        }
    }

    fun textUndo() { val s = hist.undo(ctl.text()); if (s != null) { ctl.setText(s); dirty = true; textEdited = true } }
    fun textRedo() { val s = hist.redo(); if (s != null) { ctl.setText(s); dirty = true; textEdited = true } }

    fun treeUndo() { val d = doc ?: return; try { if (d.undo()) dirty = true } catch (e: Exception) { note = Pair(e.message ?: "", false) } }
    fun treeRedo() { val d = doc ?: return; try { if (d.redo()) dirty = true } catch (e: Exception) { note = Pair(e.message ?: "", false) } }

    fun save() {
        val d = doc ?: return
        if (saving) return
        val text: String
        if (view == 1) {
            val t = ctl.text()
            try { Json.parse(t) } catch (e: Exception) { note = Pair(te.jsonNotSaved(e.message ?: ""), false); return }
            text = t
        } else {
            text = if (d.pristine) d.loaded.text else Json.write(d.root, ep.jsonFmt == 0)
        }
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
            if (res.ok) { dirty = false; d.bytes = bytes; Drafts.clear(ctx, d.item.uri.toString()) }
        }
    }

    val requestBack: () -> Unit = {
        if (doc != null && dirty) confirmBack = true else onBack()
    }
    BackHandler(enabled = true) { requestBack() }
    LaunchedEffect(doc?.serial) {
        if (doc != null) { delay(150); pageScroll.animateScrollTo(pageScroll.maxValue) }
    }

    LaunchedEffect(tick) {
        if (tick > 0) { delay(700); hist.commit(ctl.text()) }
    }
    val rv = doc?.rev ?: 0
    LaunchedEffect(tick, rv, dirty) {
        val dd = doc
        if (dd != null && dirty && (tick > 0 || rv > 0)) {
            delay(2000)
            val txt = if (view == 1 || !dd.valid) ctl.text() else Json.write(dd.root, true)
            Drafts.save(ctx, dd.item.uri.toString(), txt)
        }
    }
    LaunchedEffect(selPos, tick, doc, view) {
        if (doc != null && view == 1) {
            delay(150)
            val cs = ctl.et?.text
            if (cs != null) {
                val lc = TxtDoc.lineCol(cs, selPos)
                statusText = te.status(lc.first, lc.second, cs.length)
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(title = "JSON", onBack = requestBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(pageScroll),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            TePanelUi(
                panel, ep, te, fold, busy, pickOut, te.createJsonNote,
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
                Column(Modifier.height(areaH).fillMaxWidth()) {
            SegmentedChips(listOf(te.viewTree, te.viewText), view, 14.sp) { switchView(it) }
            Spacer(Modifier.height(8.dp))
            val canSave = teCanSave(ep, d.created, d.memo)
            if (view == 0 && d.valid && JsonTree.isContainer(d.root)) {
                JsonTreeView(
                    d = d, te = te, note = note, onNoteClose = { note = null },
                    onUndo = { treeUndo() }, onRedo = { treeRedo() }, onMutate = { mutate(it) },
                    onOptions = { optOpen = true },
                    saveText = if (saving) te.saving else te.saveBtn, canSave = !saving && canSave, onSave = { save() },
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(0.7f)) { SecondaryButton("↶") { textUndo() } }
                    Box(Modifier.weight(0.7f)) { SecondaryButton("↷") { textRedo() } }
                    Box(Modifier.weight(1.2f)) { SecondaryButton(te.findBtn) { find.open = !find.open } }
                    Box(Modifier.weight(1.4f)) { SecondaryButton(te.tidyBtn) { prettify() } }
                    Box(Modifier.weight(1.2f)) { SecondaryButton(te.optBtn) { optOpen = true } }
                }
                Spacer(Modifier.height(8.dp))
                if (find.open) {
                    FindPanel(te, find) { if (find.replaceAll()) { dirty = true; textEdited = true; tick++ } }
                    Spacer(Modifier.height(8.dp))
                }
                val nt = note
                if (nt != null) {
                    NoteCard(nt, te.close) { note = null }
                    Spacer(Modifier.height(8.dp))
                }
                key(textSerial) {
                    CodeEditor(
                        ctl, ep.font, ep.wrap, Modifier.weight(1f).fillMaxWidth(),
                        onChanged = { dirty = true; textEdited = true; tick++ },
                        onSelection = { a, _ -> selPos = a },
                        kind = "json", highlight = ep.hl, showLines = ep.lineNo, showWs = ep.showWs,
                    )
                }
                if (ep.symbols) {
                    Spacer(Modifier.height(6.dp))
                    SymbolBar(ctl)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    statusText + (if (statusText.isEmpty()) "" else " · ") + encDisplay(te, d.loaded) + " · " + (if (d.loaded.crlf) "CRLF" else "LF"),
                    color = p.muted, fontSize = 12.sp, maxLines = 1,
                )
                Spacer(Modifier.height(8.dp))
                PrimaryButton(if (saving) te.saving else te.saveBtn, !saving && canSave) { save() }
            }
            Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (optOpen && doc != null) {
        val d = doc!!
        AppDialog(te.optTitle, { optOpen = false }, buttons = { DlgBtn(te.close, true) { optOpen = false } }) {
            Column(
                Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(ScreenGap),
            ) {
                FoldSection(fold, "look", te.secLook) { EditorLookBlock(ep, te) }
                FoldSection(fold, "json", te.secJson) { JsonOptionsBlock(ep, te) }
                FoldSection(fold, "enc", te.secEnc) {
                    OpenEncBlock(ep, te) { if (dirty) confirmReload = true else reload() }
                    SaveFormatBlock(te, d.fmt)
                }
                if (!d.created) FoldSection(fold, "out", m.secOut) {
                    EditorOutBlock(ep, te, pickOut, true, d.outName) { d.outName = it }
                }
            }
        }
    }
    val offer = draftOffer
    if (offer != null) {
        ConfirmDialog(
            te.draftTitle, te.draftMsg, te.draftRestore, te.draftIgnore,
            {
                val d = doc
                if (d != null) {
                    try {
                        val r = Json.parse(offer)
                        d.pushUndo()
                        d.root = r; d.valid = true; d.pristine = false; d.rev++; d.path = emptyList()
                        dirty = true
                        if (view == 1 || !JsonTree.isContainer(r)) enterText(d)
                        note = Pair(te.draftRestored, true)
                    } catch (e: Exception) {
                        note = Pair(e.message ?: "", false)
                    }
                }
                draftOffer = null
            },
            { draftOffer = null },
        )
    }
    if (confirmBack) {
        ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { confirmBack = false; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; onBack() }, { confirmBack = false })
    }
    val ps = pendingStart
    if (ps != null) {
        ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { pendingStart = null; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; ps() }, { pendingStart = null })
    }
    if (confirmReload) {
        ConfirmDialog(te.reloadTitle, te.reloadMsg, te.reloadYes, te.cancel, { confirmReload = false; reload() }, { confirmReload = false })
    }
}

/** JSON-specific settings: initial view and save format from the tree view. */
@Composable
private fun JsonOptionsBlock(ep: EditorPrefs, te: TextEditStr) {
    SectionLabel(te.jsonStartView)
    SegmentedChips(listOf(te.viewTree, te.viewText), ep.jsonView, 14.sp) { ep.saveJsonView(it) }
    SectionLabel(te.jsonSaveFormat)
    SegmentedChips(listOf(te.fmtPretty, te.fmtMinify), ep.jsonFmt, 14.sp) { ep.saveJsonFmt(it) }
    DetailHint(te.jsonFmtHint)
}

@Composable
private fun JsonTypeChip(name: String) {
    val p = LocalPalette.current
    Box(Modifier.clip(RoundedCornerShape(50)).background(p.accent.copy(alpha = 0.18f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text(name, color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun JsonRow(
    te: TextEditStr, k: Any, v: Any?, highlight: Boolean,
    onClick: () -> Unit, onEdit: () -> Unit, onDup: () -> Unit, onUp: () -> Unit, onDown: () -> Unit,
    onSort: (() -> Unit)?, onCopyPath: () -> Unit, onDelete: () -> Unit,
) {
    val p = LocalPalette.current
    var menu by remember { mutableStateOf(false) }
    val container = JsonTree.isContainer(v)
    Column(Modifier.fillMaxWidth().background(if (highlight) p.accent.copy(alpha = 0.12f) else Color.Transparent)) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(keyLabel(k), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (container) {
                    Text(if (v is Map<*, *>) te.propCount(JsonTree.size(v)) else te.itemCount(JsonTree.size(v)), color = p.muted, fontSize = 14.sp)
                    if (JsonTree.size(v) > 0) Text(JsonTree.inner(v), color = p.muted, fontSize = 12.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Text(JsonTree.scalarText(v), color = p.accent, fontSize = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
            if (container) Text("›", color = p.accent, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
            else JsonTypeChip(te.typeName(JsonTree.typeOf(v)))
            Box {
                Text("⋮", color = p.muted, fontSize = 22.sp, modifier = Modifier.clickable { menu = true }.padding(horizontal = 14.dp, vertical = 8.dp))
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(onClick = { menu = false; onEdit() }) { Text(te.menuEdit, color = p.text) }
                    DropdownMenuItem(onClick = { menu = false; onDup() }) { Text(te.menuDuplicate, color = p.text) }
                    DropdownMenuItem(onClick = { menu = false; onUp() }) { Text(te.menuUp, color = p.text) }
                    DropdownMenuItem(onClick = { menu = false; onDown() }) { Text(te.menuDown, color = p.text) }
                    if (onSort != null) DropdownMenuItem(onClick = { menu = false; onSort() }) { Text(te.menuSort, color = p.text) }
                    DropdownMenuItem(onClick = { menu = false; onCopyPath() }) { Text(te.menuCopyPath, color = p.text) }
                    DropdownMenuItem(onClick = { menu = false; onDelete() }) { Text(te.menuDelete, color = ErrColor) }
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline.copy(alpha = 0.4f)))
    }
}

/** JSON tree view: breadcrumb, child list, search, add, edit. */
@Composable
private fun JsonTreeView(
    d: JsonDoc, te: TextEditStr, note: Pair<String, Boolean>?, onNoteClose: () -> Unit,
    onUndo: () -> Unit, onRedo: () -> Unit, onMutate: ((() -> Unit)) -> Unit, onOptions: () -> Unit,
    saveText: String, canSave: Boolean, onSave: () -> Unit, modifier: Modifier,
) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val listState = rememberLazyListState()
    var searchOpen by remember { mutableStateOf(false) }
    var searchQ by remember { mutableStateOf("") }
    var searchCase by remember { mutableStateOf(false) }
    var hits by remember { mutableStateOf<List<List<Any>>>(emptyList()) }
    var highlight by remember { mutableStateOf<Any?>(null) }
    var editing by remember { mutableStateOf<Any?>(null) }
    var adding by remember { mutableStateOf(false) }

    val path = d.path
    val cur = remember(d.rev, path) { JsonTree.get(d.root, path) }
    val kids = remember(d.rev, path) { JsonTree.children(cur) }
    val isObj = cur is Map<*, *>

    LaunchedEffect(searchQ, searchCase, d.rev, searchOpen) {
        if (!searchOpen || searchQ.isEmpty()) { hits = emptyList(); return@LaunchedEffect }
        delay(300)
        hits = withContext(Dispatchers.Default) { JsonTree.search(d.root, searchQ, searchCase) }
    }
    LaunchedEffect(path, highlight) {
        val h = highlight
        if (h != null) {
            val i = kids.indexOfFirst { it.first == h }
            if (i >= 0) listState.scrollToItem(i)
        }
    }

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(0.8f)) { SecondaryButton("↶") { onUndo() } }
            Box(Modifier.weight(0.8f)) { SecondaryButton("↷") { onRedo() } }
            Box(Modifier.weight(1.4f)) { SecondaryButton(te.findBtn) { searchOpen = !searchOpen } }
            Box(Modifier.weight(1.4f)) { SecondaryButton(te.optBtn) { onOptions() } }
        }
        Spacer(Modifier.height(8.dp))
        if (searchOpen) {
            AppTextField(value = searchQ, onValueChange = { searchQ = it }, label = te.searchKeyValue)
            CheckRow(te.caseSensitive, searchCase) { searchCase = it }
            if (searchQ.isNotEmpty()) Hint(te.hitCount(hits.size))
            Spacer(Modifier.height(4.dp))
        }
        if (note != null) {
            NoteCard(note, te.close) { onNoteClose() }
            Spacer(Modifier.height(8.dp))
        }
        // breadcrumb
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "←", color = if (path.isEmpty()) p.muted else p.accent, fontSize = 22.sp,
                modifier = Modifier.clickable(enabled = path.isNotEmpty()) { d.path = path.dropLast(1); highlight = null }.padding(end = 12.dp),
            )
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    te.rootName, color = if (path.isEmpty()) p.text else p.accent, fontSize = 15.sp,
                    textDecoration = if (path.isEmpty()) null else TextDecoration.Underline,
                    modifier = Modifier.clickable { d.path = emptyList(); highlight = null },
                )
                path.forEachIndexed { i, k ->
                    Text("  ›  ", color = p.muted, fontSize = 15.sp)
                    Text(
                        keyLabel(k), color = if (i == path.size - 1) p.text else p.accent, fontSize = 15.sp, maxLines = 1,
                        fontWeight = if (i == path.size - 1) FontWeight.SemiBold else null,
                        textDecoration = if (i == path.size - 1) null else TextDecoration.Underline,
                        modifier = Modifier.clickable { d.path = path.take(i + 1); highlight = null },
                    )
                }
            }
            Box(Modifier.padding(start = 8.dp)) { JsonTypeChip(te.typeName(JsonTree.typeOf(cur))) }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline))

        if (searchOpen && searchQ.isNotEmpty()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items(hits.size) { i ->
                    val h = hits[i]
                    val v = JsonTree.get(d.root, h)
                    Column(
                        Modifier.fillMaxWidth().clickable {
                            d.path = h.dropLast(1); highlight = h.last(); searchOpen = false
                        }.padding(vertical = 10.dp, horizontal = 8.dp),
                    ) {
                        Text(JsonTree.pathString(h), color = p.accent, fontSize = 13.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (JsonTree.isContainer(v)) te.typeName(JsonTree.typeOf(v)) else JsonTree.scalarText(v, 80), color = p.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline.copy(alpha = 0.4f)))
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState) {
                if (kids.isEmpty()) item { Box(Modifier.padding(16.dp)) { Hint(te.emptyContainer) } }
                items(kids.size) { i ->
                    val k = kids[i].first
                    val v = kids[i].second
                    JsonRow(
                        te, k, v, highlight == k,
                        onClick = { if (JsonTree.isContainer(v)) { d.path = path + k; highlight = null } else editing = k },
                        onEdit = { editing = k },
                        onDup = {
                            onMutate {
                                val copy = JsonTree.deepCopy(v)
                                JsonTree.insertAfter(cur, k, if (cur is Map<*, *>) JsonTree.uniqueKey(JsonTree.obj(cur), k.toString()) else "", copy)
                            }
                        },
                        onUp = { onMutate { JsonTree.move(cur, k, -1) } },
                        onDown = { onMutate { JsonTree.move(cur, k, 1) } },
                        onSort = if (v is Map<*, *>) ({ onMutate { JsonTree.sortKeys(JsonTree.obj(v)) } }) else null,
                        onCopyPath = {
                            val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("path", JsonTree.pathString(path + k)))
                            Toast.makeText(ctx, te.copied, Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { onMutate { JsonTree.remove(cur, k) } },
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton("＋ " + te.addBtn) { adding = true } }
            Box(Modifier.weight(1f)) { PrimaryButton(saveText, canSave) { onSave() } }
        }
    }

    val ek = editing
    if (ek != null && JsonTree.exists(cur, listOf(ek))) {
        val others: Set<String> = if (cur is Map<*, *>) cur.keys.map { it as String }.toSet() - (ek as? String ?: "") else emptySet()
        JsonEditDialog(
            te, te.editTitle(keyLabel(ek)), keyEditable = isObj, requireKey = false, initKey = ek as? String ?: "",
            indexLabel = if (isObj) null else keyLabel(ek), initial = JsonTree.get(cur, listOf(ek)), others = others,
            onSave = { nk, nv ->
                onMutate {
                    if (isObj) {
                        val mm = JsonTree.obj(cur)
                        JsonTree.renameKey(mm, ek as String, nk)
                        JsonTree.put(mm, nk, nv)
                    } else {
                        JsonTree.put(cur, ek, nv)
                    }
                }
                editing = null
            },
            onDismiss = { editing = null },
        )
    }
    if (adding) {
        val others: Set<String> = if (cur is Map<*, *>) cur.keys.map { it as String }.toSet() else emptySet()
        JsonEditDialog(
            te, te.addTitle, keyEditable = isObj, requireKey = true, initKey = "",
            indexLabel = if (isObj) null else "[" + JsonTree.size(cur) + "]", initial = "", others = others,
            onSave = { nk, nv -> onMutate { JsonTree.add(cur, nk, nv) }; adding = false },
            onDismiss = { adding = false },
        )
    }
}

/** Edit/add one value dialog: key, type, and value according to type (switch for boolean, − / + for numbers). */
@Composable
private fun JsonEditDialog(
    te: TextEditStr, title: String, keyEditable: Boolean, requireKey: Boolean, initKey: String, indexLabel: String?,
    initial: Any?, others: Set<String>, onSave: (String, Any?) -> Unit, onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    var key by remember { mutableStateOf(initKey) }
    var type by remember { mutableStateOf(JsonTree.typeOf(initial)) }
    var textV by remember { mutableStateOf((initial as? String) ?: "") }
    var numV by remember { mutableStateOf((initial as? JNum)?.raw ?: "0") }
    var boolV by remember { mutableStateOf(initial == true) }
    var step by remember { mutableStateOf("1") }
    var container by remember { mutableStateOf(if (JsonTree.isContainer(initial)) initial else null) }

    fun build(): Any? = when (type) {
        JsonTree.TEXT -> textV
        JsonTree.NUMBER -> JNum(numV.trim())
        JsonTree.BOOLEAN -> boolV
        JsonTree.NULL -> null
        else -> container ?: JsonTree.defaultFor(type)
    }

    fun changeType(nt: String) {
        val nv = JsonTree.convert(build(), nt)
        type = nt
        when (nt) {
            JsonTree.TEXT -> textV = (nv as? String) ?: ""
            JsonTree.NUMBER -> numV = (nv as? JNum)?.raw ?: "0"
            JsonTree.BOOLEAN -> boolV = nv == true
            JsonTree.OBJECT, JsonTree.ARRAY -> container = nv
            else -> { }
        }
    }

    val keyDup = keyEditable && key in others
    val keyEmpty = keyEditable && requireKey && key.isEmpty()
    val numBad = type == JsonTree.NUMBER && !JsonTree.isNumber(numV)
    val valid = !keyDup && !keyEmpty && !numBad

    AppDialog(
        title, onDismiss,
        buttons = {
            DlgBtn(te.cancel, false) { onDismiss() }
            DlgBtn(te.saveBtn, true, valid) { onSave(key, build()) }
        },
    ) {
        Column(Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (keyEditable) AppTextField(value = key, onValueChange = { key = it }, label = te.keyLabel)
            else if (indexLabel != null) Text(indexLabel, color = p.muted, fontSize = 14.sp)
            if (keyDup) Text(te.keyExists, color = ErrColor, fontSize = 13.sp)
            if (keyEmpty) Text(te.keyEmpty, color = ErrColor, fontSize = 13.sp)
            AppDropdown(te.typeLabel, type, JsonTree.TYPES.map { it to te.typeName(it) }) { changeType(it) }
            when (type) {
                JsonTree.TEXT -> AppTextField(value = textV, onValueChange = { textV = it }, label = te.valueText, multiline = true)
                JsonTree.NUMBER -> {
                    AppTextField(value = numV, onValueChange = { numV = it }, label = te.valueNumber, mono = true)
                    if (numBad) Text(te.badNumber, color = ErrColor, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) { SecondaryButton("−") { JsonTree.step(numV, step, -1)?.let { numV = it } } }
                        Box(Modifier.weight(1.3f)) { AppTextField(value = step, onValueChange = { step = it }, label = te.stepLabel, mono = true) }
                        Box(Modifier.weight(1f)) { SecondaryButton("+") { JsonTree.step(numV, step, 1)?.let { numV = it } } }
                    }
                }
                JsonTree.BOOLEAN -> Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (boolV) "true" else "false", color = p.text, fontSize = 18.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = boolV, onCheckedChange = { boolV = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.5f)),
                    )
                }
                JsonTree.NULL -> Text("null", color = p.text, fontSize = 18.sp)
                else -> Hint(te.containerNote(JsonTree.size(container)))
            }
        }
    }
}
