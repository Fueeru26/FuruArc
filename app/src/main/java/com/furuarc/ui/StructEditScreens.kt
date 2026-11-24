package com.furuarc.ui

import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.core.Csv
import com.furuarc.core.Ini
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

/** Structured Text File: choose a format (JSON, CSV/TSV, INI/CFG, XML, Markdown), then open its editor. */
@Composable
fun StructuredHubScreen(onBack: () -> Unit) {
    val te = textEditStr(LocalLang.current)
    // a file handed over by "Open with" opens straight in the editor of its format
    var sel by remember { mutableStateOf(TextViewHandoff.structIndex()) }
    // a file handed over while this hub is already open: go to the editor of its format
    val handoffToken = TextViewHandoff.token
    LaunchedEffect(handoffToken) {
        val i = TextViewHandoff.structIndex()
        if (i != 0 && i != sel) sel = i
    }
    when (sel) {
        1 -> JsonEditScreen { sel = 0 }
        2 -> CsvEditScreen { sel = 0 }
        3 -> IniEditScreen { sel = 0 }
        4 -> PlainTextScreen({ sel = 0 }, title = "XML", exts = "xml, html, htm, svg, xaml, plist, resx, rss", forcedKind = "xml", xmlTools = true)
        5 -> PlainTextScreen({ sel = 0 }, title = "Markdown", exts = "md, markdown, txt", forcedKind = "md")
        else -> MenuListScreen(
            te.structTitle,
            listOf(
                MenuSpec(AppIcons.Description, "JSON", te.hubJsonSub) { sel = 1 },
                MenuSpec(AppIcons.Library, "CSV / TSV", te.hubCsvSub) { sel = 2 },
                MenuSpec(AppIcons.Tune, "INI / CFG", te.hubIniSub) { sel = 3 },
                MenuSpec(AppIcons.Language, "XML", te.hubXmlSub) { sel = 4 },
                MenuSpec(AppIcons.MenuBook, "Markdown", te.hubMdSub) { sel = 5 },
            ),
            onBack,
        )
    }
}

// ============================ CSV / TSV ============================

internal class CsvDoc(
    val item: MediaItem, val created: Boolean, bytes0: ByteArray, loaded0: TxtDoc.Loaded,
    rows0: ArrayList<ArrayList<String>>, delim0: Char, quoteAll0: Boolean,
) {
    var bytes by mutableStateOf(bytes0)
    var loaded by mutableStateOf(loaded0)
    var rows: ArrayList<ArrayList<String>> = rows0
    var delim by mutableStateOf(delim0)
    var quoteAll = quoteAll0
    var rev by mutableStateOf(0)
    var pristine = true
    val memo = SaveMemo()
    val fmt = DocFormat()
    var outName by mutableStateOf(item.name)

    private val undoS = ArrayList<String>()
    private val redoS = ArrayList<String>()
    private var chars = 0L

    private fun snap() = Csv.serialize(rows, delim, true)

    fun pushUndo() {
        val s = snap()
        undoS.add(s); chars += s.length
        while ((undoS.size > 40 || chars > 24_000_000L) && undoS.size > 1) chars -= undoS.removeAt(0).length
        redoS.clear()
    }

    fun undo(): Boolean {
        if (undoS.isEmpty()) return false
        redoS.add(snap())
        val s = undoS.removeAt(undoS.size - 1)
        chars -= s.length
        rows = Csv.parse(s, delim).rows; rev++; pristine = false
        return true
    }

    fun redo(): Boolean {
        if (redoS.isEmpty()) return false
        undoS.add(snap())
        rows = Csv.parse(redoS.removeAt(redoS.size - 1), delim).rows; rev++; pristine = false
        return true
    }

    fun cols(): Int = Csv.columns(rows)

    fun setCell(r: Int, c: Int, v: String) {
        while (rows.size <= r) rows.add(ArrayList())
        val row = rows[r]
        while (row.size <= c) row.add("")
        row[c] = v
    }

    fun insertRow(at: Int) {
        val row = ArrayList<String>()
        repeat(cols()) { row.add("") }
        rows.add(at.coerceIn(0, rows.size), row)
    }

    fun duplicateRow(r: Int) { if (r in rows.indices) rows.add(r + 1, ArrayList(rows[r])) }
    fun deleteRow(r: Int) { if (r in rows.indices) rows.removeAt(r) }

    fun insertCol(at: Int) {
        for (row in rows) if (row.size >= at) row.add(at, "")
    }

    fun deleteCol(c: Int) {
        for (row in rows) if (c < row.size) row.removeAt(c)
    }

    /** Sort data rows (starting at index [first]) by column [col]; numbers are compared as numbers. */
    fun sortRows(col: Int, asc: Boolean, first: Int) {
        if (first >= rows.size) return
        val sub = rows.subList(first, rows.size)
        java.util.Collections.sort(sub, Comparator<ArrayList<String>> { a, b ->
            val x = a.getOrElse(col) { "" }
            val y = b.getOrElse(col) { "" }
            val dx = x.trim().toDoubleOrNull()
            val dy = y.trim().toDoubleOrNull()
            val r = if (dx != null && dy != null) dx.compareTo(dy) else x.compareTo(y, ignoreCase = true)
            if (asc) r else -r
        })
    }
}

private fun delimName(te: TextEditStr, d: Char): String = when (d) {
    ',' -> te.delimComma
    ';' -> te.delimSemi
    '\t' -> te.delimTab
    '|' -> te.delimPipe
    else -> d.toString()
}

@Composable
private fun CsvOptionsBlock(ep: EditorPrefs, te: TextEditStr) {
    AppDropdown(
        te.csvDelimLabel, ep.csvDelim.toString(),
        listOf("0" to te.delimAuto, "1" to te.delimComma, "2" to te.delimSemi, "3" to te.delimTab, "4" to te.delimPipe),
    ) { ep.saveCsvDelim(it.toInt()) }
    CheckRow(te.csvHeaderRow, ep.csvHeader) { ep.saveCsvHeader(it) }
    SectionLabel(te.csvQuoteLabel)
    SegmentedChips(listOf(te.quoteKeep, te.quoteMin, te.quoteAll), ep.csvQuote, 13.sp) { ep.saveCsvQuote(it) }
    DetailHint(te.csvHint)
}

/** Column separator: manual choice (1-4) or guessed from the contents; a new .tsv file uses tab and .csv uses comma. */
private fun csvDelimFor(pref: Int, text: String, name: String): Char = when (pref) {
    1 -> ','
    2 -> ';'
    3 -> '\t'
    4 -> '|'
    else -> Csv.detectDelimiter(text, name.substringAfterLast('.', ""))
}

/** CSV/TSV table editor: Create File / Upload File panel on top, the table in the "Text Editor Area" below it. */
@Composable
fun CsvEditScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val te = remember(lang) { textEditStr(lang) }
    val m = remember(lang) { mediaStr(lang) }
    val scope = rememberCoroutineScope()
    val ep = rememberEditorPrefs()
    val fold = remember { FoldState() }
    val panel = remember { TePanel(listOf("csv", "tsv", "txt"), "csv") }
    val pageScroll = rememberScrollState()
    val areaH = editorAreaHeight(0.75f)
    var doc by remember { mutableStateOf<CsvDoc?>(null) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var dirty by remember { mutableStateOf(false) }
    var optOpen by remember { mutableStateOf(false) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReload by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var saving by remember { mutableStateOf(false) }
    var draftOffer by remember { mutableStateOf<String?>(null) }
    val pickOut = rememberOutPicker { u, n -> ep.out = u; ep.outName = n }

    fun delimOf(text: String, name: String): Char = csvDelimFor(ep.csvDelim, text, name)

    fun openItem(item: MediaItem) {
        if (busy) return
        busy = true; err = null; note = null
        val pref = ep.csvDelim
        scope.launch {
            // read, decode, and parse the table on the IO thread so the screen does not freeze
            val res = withContext(Dispatchers.IO) {
                val o = openTextFile(ctx, te, item, ep.enc)
                val ld = o.loaded
                if (ld != null && o.bytes != null) {
                    try {
                        val dl = csvDelimFor(pref, ld.text, item.name)
                        Triple(o, dl, Csv.parse(ld.text, dl))
                    } catch (t: Throwable) {
                        Triple(OpenResult(null, null, te.openFail(t.javaClass.simpleName)), ',', null)
                    }
                } else {
                    Triple(o, ',', null)
                }
            }
            busy = false
            val r = res.first
            val ld = r.loaded
            val bytes = r.bytes
            val ps = res.third
            if (ld == null || bytes == null || ps == null) {
                err = r.error
            } else {
                dirty = false; note = null; draftOffer = null
                doc = CsvDoc(item, false, bytes, ld, ps.rows, res.second, ps.quoteAll)
                val dr0 = Drafts.load(ctx, item.uri.toString())
                if (dr0 != null && dr0 != ld.text) draftOffer = dr0
            }
        }
    }

    fun createItem(fileName: String) {
        if (busy) return
        busy = true; err = null; note = null
        val ext = fileName.substringAfterLast('.', "").lowercase()
        scope.launch {
            val cf = withContext(Dispatchers.IO) { createTextFile(ctx, te, ep, fileName, ByteArray(0), m.needOut) }
            busy = false
            val uri = cf.uri
            if (!cf.ok || uri == null) {
                err = cf.msg
            } else {
                val dl = when (ext) { "tsv" -> '\t'; "csv" -> ','; else -> if (ep.csvDelim == 0) ',' else csvDelimFor(ep.csvDelim, "", "") }
                val ld = TxtDoc.Loaded("", "utf8", false, false)
                dirty = false; draftOffer = null
                val nd = CsvDoc(MediaItem(uri, cf.name, "", 0L), true, ByteArray(0), ld, ArrayList(), dl, false)
                nd.memo.savedUri = uri
                doc = nd
                note = Pair(cf.msg, true)
            }
        }
    }

    fun guarded(action: () -> Unit) {
        if (doc != null && dirty) pendingStart = action else action()
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
        d.delim = delimOf(ld.text, d.item.name)
        val ps = Csv.parse(ld.text, d.delim)
        d.rows = ps.rows; d.quoteAll = ps.quoteAll
        d.pristine = true; d.rev++
        dirty = false
    }

    fun save() {
        val d = doc ?: return
        if (saving) return
        val text: String = if (d.pristine) {
            d.loaded.text
        } else {
            val qa = when (ep.csvQuote) { 0 -> d.quoteAll; 1 -> false; else -> true }
            val ser = Csv.serialize(d.rows, d.delim, qa)
            if (!d.loaded.text.endsWith("\n") && ser.endsWith("\n")) ser.dropLast(1) else ser
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

    fun paste() {
        val d = doc ?: return
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip
        val txt = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).coerceToText(ctx)?.toString() else null
        if (txt == null || txt.isEmpty()) { note = Pair(te.csvClipEmpty, false); return }
        val dl = if (txt.contains('\t')) '\t' else d.delim
        val rows = Csv.parse(txt.replace("\r\n", "\n"), dl).rows
        if (rows.isEmpty()) { note = Pair(te.csvClipEmpty, false); return }
        mutate { for (r in rows) d.rows.add(r) }
        note = Pair(te.csvPasted(rows.size), true)
    }

    val rv = doc?.rev ?: 0
    LaunchedEffect(rv, dirty) {
        val dd = doc
        if (dd != null && dirty && rv > 0) {
            delay(2000)
            Drafts.save(ctx, dd.item.uri.toString(), Csv.serialize(dd.rows, dd.delim, true))
        }
    }
    LaunchedEffect(doc) {
        if (doc != null) { delay(150); pageScroll.animateScrollTo(pageScroll.maxValue) }
    }

    val requestBack: () -> Unit = {
        if (doc != null && dirty) confirmBack = true else onBack()
    }
    BackHandler(enabled = true) { requestBack() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(title = "CSV / TSV", onBack = requestBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(pageScroll),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            TePanelUi(
                panel, ep, te, fold, busy, pickOut, null,
                onCreate = { name -> guarded { createItem(name) } },
                onEdit = { item -> guarded { openItem(item) } },
            )
            if (busy && panel.mode != 2) Hint(te.opening)
            val e = err
            if (e != null && panel.mode != 2) Text(e, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
            val d = doc
            if (d != null && panel.mode != 2) {
                AreaTitle(te.editorArea)
                AreaFileName(d.item.name, dirty)
                CsvTableView(
                    d, te, ep, note, { note = null },
                    onUndo = { if (d.undo()) dirty = true },
                    onRedo = { if (d.redo()) dirty = true },
                    onMutate = { mutate(it) }, onOptions = { optOpen = true }, onPaste = { paste() },
                    saveText = if (saving) te.saving else te.saveBtn, canSave = !saving && teCanSave(ep, d.created, d.memo), onSave = { save() },
                    modifier = Modifier.height(areaH).fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (optOpen && doc != null) {
        val d = doc!!
        AppDialog(te.optTitle, { optOpen = false }, buttons = { DlgBtn(te.close, true) { optOpen = false } }) {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
                FoldSection(fold, "csv", te.secCsv) { CsvOptionsBlock(ep, te) }
                FoldSection(fold, "enc", te.secEnc) {
                    OpenEncBlock(ep, te) { if (dirty) confirmReload = true else reload() }
                    SaveFormatBlock(te, d.fmt)
                }
                if (!d.created) FoldSection(fold, "out", m.secOut) { EditorOutBlock(ep, te, pickOut, true, d.outName) { d.outName = it } }
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
                    d.pushUndo()
                    d.rows = Csv.parse(offer, d.delim).rows
                    d.pristine = false; d.rev++
                    dirty = true
                    note = Pair(te.draftRestored, true)
                }
                draftOffer = null
            },
            { draftOffer = null },
        )
    }
    if (confirmBack) ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { confirmBack = false; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; onBack() }, { confirmBack = false })
    val ps = pendingStart
    if (ps != null) ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { pendingStart = null; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; ps() }, { pendingStart = null })
    if (confirmReload) ConfirmDialog(te.reloadTitle, te.reloadMsg, te.reloadYes, te.cancel, { confirmReload = false; reload() }, { confirmReload = false })
}

@Composable
private fun CsvCell(text: String, header: Boolean, hl: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier.width(150.dp).heightIn(min = 44.dp)
            .background(if (hl) p.accent.copy(alpha = 0.22f) else Color.Transparent)
            .border(0.5.dp, p.outline.copy(alpha = 0.5f))
            .clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text.replace("\n", "↵"), color = if (header) p.accent else p.text,
            fontWeight = if (header) FontWeight.Bold else null, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MenuItemText(text: String, danger: Boolean = false, onClick: () -> Unit) {
    val p = LocalPalette.current
    DropdownMenuItem(onClick = onClick) { Text(text, color = if (danger) ErrColor else p.text) }
}

@Composable
private fun CsvTableView(
    d: CsvDoc, te: TextEditStr, ep: EditorPrefs, note: Pair<String, Boolean>?, onNoteClose: () -> Unit,
    onUndo: () -> Unit, onRedo: () -> Unit, onMutate: ((() -> Unit)) -> Unit, onOptions: () -> Unit, onPaste: () -> Unit,
    saveText: String, canSave: Boolean, onSave: () -> Unit, modifier: Modifier,
) {
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val hs: ScrollState = rememberScrollState()
    val cellPx = with(LocalDensity.current) { 150.dp.toPx() }.toInt()
    var searchOpen by remember { mutableStateOf(false) }
    var searchMode by remember { mutableStateOf(0) }
    var searchQ by remember { mutableStateOf("") }
    var searchCase by remember { mutableStateOf(false) }
    var hits by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    var hitIdx by remember { mutableStateOf(0) }
    var editing by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var colMenu by remember { mutableStateOf(-1) }
    var rowMenu by remember { mutableStateOf(-1) }

    val rev = d.rev
    val hdr = ep.csvHeader && d.rows.isNotEmpty()
    val first = if (hdr) 1 else 0
    val cols = maxOf(d.cols(), 1)
    val filtering = searchOpen && searchMode == 1 && searchQ.isNotEmpty()
    val shown: List<Int> = remember(rev, first, filtering, searchQ, searchCase) {
        val all = ArrayList<Int>()
        for (r in first until d.rows.size) {
            if (!filtering || d.rows[r].any { it.contains(searchQ, ignoreCase = !searchCase) }) all.add(r)
        }
        all
    }

    LaunchedEffect(searchQ, searchCase, rev, searchOpen, searchMode) {
        if (!searchOpen || searchQ.isEmpty() || searchMode == 1) { hits = emptyList(); return@LaunchedEffect }
        delay(250)
        hits = withContext(Dispatchers.Default) {
            val out = ArrayList<Pair<Int, Int>>()
            loop@ for (r in d.rows.indices) {
                val row = d.rows[r]
                for (c in row.indices) {
                    if (row[c].contains(searchQ, ignoreCase = !searchCase)) {
                        out.add(Pair(r, c))
                        if (out.size >= 2000) break@loop
                    }
                }
            }
            out
        }
        hitIdx = 0
    }

    fun gotoHit(i: Int) {
        if (hits.isEmpty()) return
        val k = ((i % hits.size) + hits.size) % hits.size
        hitIdx = k
        val h = hits[k]
        scope.launch {
            listState.scrollToItem(shown.indexOf(h.first).coerceAtLeast(0))
            hs.scrollTo(h.second * cellPx)
        }
    }

    fun cur(): Pair<Int, Int>? = if (hits.isNotEmpty()) hits[hitIdx.coerceIn(0, hits.size - 1)] else null

    Column(modifier) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(0.7f)) { SecondaryButton("↶") { onUndo() } }
            Box(Modifier.weight(0.7f)) { SecondaryButton("↷") { onRedo() } }
            Box(Modifier.weight(1.1f)) { SecondaryButton(te.findBtn) { searchOpen = !searchOpen } }
            Box(Modifier.weight(1.3f)) { SecondaryButton(te.csvPaste) { onPaste() } }
            Box(Modifier.weight(1.1f)) { SecondaryButton(te.optBtn) { onOptions() } }
        }
        Spacer(Modifier.height(8.dp))
        if (searchOpen) {
            SegmentedChips(listOf(te.findBtn, te.csvFilter), searchMode, 13.sp) { searchMode = it }
            AppTextField(value = searchQ, onValueChange = { searchQ = it }, label = te.searchCells)
            if (searchMode == 0) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1.6f)) { CheckRow(te.caseSensitive, searchCase) { searchCase = it } }
                    Box(Modifier.weight(0.6f)) { SecondaryButton("‹") { gotoHit(hitIdx - 1) } }
                    Box(Modifier.weight(0.6f)) { SecondaryButton("›") { gotoHit(hitIdx + 1) } }
                }
                if (searchQ.isNotEmpty()) Hint(te.hitCount(hits.size))
            } else {
                CheckRow(te.caseSensitive, searchCase) { searchCase = it }
                if (searchQ.isNotEmpty()) Hint(te.csvFilterCount(shown.size, maxOf(d.rows.size - first, 0)))
            }
            Spacer(Modifier.height(4.dp))
        }
        if (note != null) {
            NoteCard(note, te.close) { onNoteClose() }
            Spacer(Modifier.height(8.dp))
        }
        Text(te.csvInfo(d.rows.size, d.cols(), delimName(te, d.delim)), color = p.muted, fontSize = 12.sp)
        Spacer(Modifier.height(4.dp))
        // column headers (or column letters)
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(44.dp).height(44.dp))
            Box(Modifier.weight(1f)) {
                Row(Modifier.horizontalScroll(hs)) {
                    for (c in 0 until cols) {
                        Box {
                            val label = if (hdr) d.rows[0].getOrElse(c) { "" } else Csv.colName(c)
                            CsvCell(label, true, false) { colMenu = c }
                            DropdownMenu(expanded = colMenu == c, onDismissRequest = { colMenu = -1 }) {
                                if (hdr) MenuItemText(te.colRename) { colMenu = -1; editing = Pair(0, c) }
                                MenuItemText(te.csvSortAsc) { colMenu = -1; onMutate { d.sortRows(c, true, first) } }
                                MenuItemText(te.csvSortDesc) { colMenu = -1; onMutate { d.sortRows(c, false, first) } }
                                MenuItemText(te.colInsertLeft) { colMenu = -1; onMutate { d.insertCol(c) } }
                                MenuItemText(te.colInsertRight) { colMenu = -1; onMutate { d.insertCol(c + 1) } }
                                MenuItemText(te.colDelete, true) { colMenu = -1; onMutate { d.deleteCol(c) } }
                            }
                        }
                    }
                }
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState) {
            if (shown.isEmpty()) item { Box(Modifier.padding(16.dp)) { Hint(if (filtering) te.noMatch else te.csvEmpty) } }
            items(shown.size) { i ->
                val r = shown[i]
                val row = d.rows[r]
                Row(Modifier.fillMaxWidth()) {
                    Box {
                        Box(
                            Modifier.width(44.dp).heightIn(min = 44.dp).border(0.5.dp, p.outline.copy(alpha = 0.5f))
                                .clickable { rowMenu = r },
                            contentAlignment = Alignment.Center,
                        ) { Text((r + 1).toString(), color = p.muted, fontSize = 12.sp) }
                        DropdownMenu(expanded = rowMenu == r, onDismissRequest = { rowMenu = -1 }) {
                            MenuItemText(te.rowInsertAbove) { rowMenu = -1; onMutate { d.insertRow(r) } }
                            MenuItemText(te.rowInsertBelow) { rowMenu = -1; onMutate { d.insertRow(r + 1) } }
                            MenuItemText(te.menuDuplicate) { rowMenu = -1; onMutate { d.duplicateRow(r) } }
                            MenuItemText(te.menuDelete, true) { rowMenu = -1; onMutate { d.deleteRow(r) } }
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        Row(Modifier.horizontalScroll(hs)) {
                            for (c in 0 until cols) {
                                val here = cur()
                                CsvCell(row.getOrElse(c) { "" }, false, here != null && here.first == r && here.second == c) { editing = Pair(r, c) }
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton("＋ " + te.csvAddRow) { onMutate { d.insertRow(d.rows.size) } } }
            Box(Modifier.weight(1f)) { SecondaryButton("＋ " + te.csvAddCol) { onMutate { d.insertCol(d.cols()) } } }
            Box(Modifier.weight(1.1f)) { PrimaryButton(saveText, canSave) { onSave() } }
        }
    }

    val ed = editing
    if (ed != null) {
        var txt by remember(ed) { mutableStateOf(d.rows.getOrNull(ed.first)?.getOrNull(ed.second) ?: "") }
        AppDialog(
            te.cellTitle(Csv.colName(ed.second), ed.first + 1), { editing = null },
            buttons = {
                DlgBtn(te.cancel, false) { editing = null }
                DlgBtn(te.saveBtn, true) { onMutate { d.setCell(ed.first, ed.second, txt) }; editing = null }
            },
        ) {
            AppTextField(value = txt, onValueChange = { txt = it }, label = te.valueText, multiline = true)
        }
    }
}

// ============================ INI / CFG ============================

internal class IniDoc(val item: MediaItem, val created: Boolean, bytes0: ByteArray, loaded0: TxtDoc.Loaded, lines0: ArrayList<String>) {
    var bytes by mutableStateOf(bytes0)
    var loaded by mutableStateOf(loaded0)
    var lines: ArrayList<String> = lines0
    var rev by mutableStateOf(0)
    var pristine = true
    /** null = section list; otherwise the name of the currently open section ("" = global section). */
    var section by mutableStateOf<String?>(null)
    val memo = SaveMemo()
    val fmt = DocFormat()
    var outName by mutableStateOf(item.name)

    private val undoS = ArrayList<String>()
    private val redoS = ArrayList<String>()
    private var chars = 0L

    fun pushUndo() {
        val s = Ini.join(lines)
        undoS.add(s); chars += s.length
        while ((undoS.size > 40 || chars > 24_000_000L) && undoS.size > 1) chars -= undoS.removeAt(0).length
        redoS.clear()
    }

    private fun fix() {
        val sec = section
        if (sec != null && sec != Ini.GLOBAL && Ini.parse(lines).none { it.isSection && it.section == sec }) section = null
    }

    fun undo(): Boolean {
        if (undoS.isEmpty()) return false
        redoS.add(Ini.join(lines))
        val s = undoS.removeAt(undoS.size - 1)
        chars -= s.length
        lines = Ini.split(s); rev++; pristine = false; fix()
        return true
    }

    fun redo(): Boolean {
        if (redoS.isEmpty()) return false
        undoS.add(Ini.join(lines))
        lines = Ini.split(redoS.removeAt(redoS.size - 1)); rev++; pristine = false; fix()
        return true
    }
}

@Composable
private fun KeyValueDialog(
    te: TextEditStr, title: String, keyLabel: String, keyEditable: Boolean, valueLabel: String?,
    initKey: String, initValue: String, others: Set<String>, onSave: (String, String) -> Unit, onDismiss: () -> Unit,
) {
    var key by remember { mutableStateOf(initKey) }
    var value by remember { mutableStateOf(initValue) }
    val badKey = keyEditable && (key.isEmpty() || key.contains('=') || key.contains(']') || key.contains('\n'))
    val dup = keyEditable && key != initKey && key in others
    AppDialog(
        title, onDismiss,
        buttons = {
            DlgBtn(te.cancel, false) { onDismiss() }
            DlgBtn(te.saveBtn, true, !badKey && !dup) { onSave(key.trim(), value) }
        },
    ) {
        Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (keyEditable) AppTextField(value = key, onValueChange = { key = it.replace("\n", "") }, label = keyLabel)
            if (dup) Text(te.keyExists, color = ErrColor, fontSize = 13.sp)
            if (valueLabel != null) AppTextField(value = value, onValueChange = { value = it.replace("\n", "") }, label = valueLabel)
        }
    }
}

/** INI/CFG editor: Create File / Upload File panel on top, section and key list in the "Text Editor Area". Comments and original formatting are left unchanged. */
@Composable
fun IniEditScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val te = remember(lang) { textEditStr(lang) }
    val m = remember(lang) { mediaStr(lang) }
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    val ep = rememberEditorPrefs()
    val fold = remember { FoldState() }
    val panel = remember { TePanel(extList("ini, cfg, conf, properties, desktop, txt"), "ini") }
    val pageScroll = rememberScrollState()
    val areaH = editorAreaHeight(0.75f)
    var doc by remember { mutableStateOf<IniDoc?>(null) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var dirty by remember { mutableStateOf(false) }
    var optOpen by remember { mutableStateOf(false) }
    var confirmBack by remember { mutableStateOf(false) }
    var confirmReload by remember { mutableStateOf(false) }
    var pendingStart by remember { mutableStateOf<(() -> Unit)?>(null) }
    var saving by remember { mutableStateOf(false) }
    var draftOffer by remember { mutableStateOf<String?>(null) }
    var editLine by remember { mutableStateOf(-1) }
    var addKey by remember { mutableStateOf(false) }
    var addSec by remember { mutableStateOf(false) }
    var renameSec by remember { mutableStateOf<String?>(null) }
    var menuFor by remember { mutableStateOf(-1) }
    val pickOut = rememberOutPicker { u, n -> ep.out = u; ep.outName = n }

    fun openItem(item: MediaItem) {
        if (busy) return
        busy = true; err = null; note = null
        scope.launch {
            val res = withContext(Dispatchers.IO) {
                val o = openTextFile(ctx, te, item, ep.enc)
                val ld = o.loaded
                Pair(o, if (ld != null) Ini.split(ld.text) else null)
            }
            busy = false
            val r = res.first
            val ld = r.loaded
            val bytes = r.bytes
            val lines = res.second
            if (ld == null || bytes == null || lines == null) {
                err = r.error
            } else {
                dirty = false; note = null; draftOffer = null
                doc = IniDoc(item, false, bytes, ld, lines)
                val dr0 = Drafts.load(ctx, item.uri.toString())
                if (dr0 != null && dr0 != ld.text) draftOffer = dr0
            }
        }
    }

    fun createItem(fileName: String) {
        if (busy) return
        busy = true; err = null; note = null
        scope.launch {
            val cf = withContext(Dispatchers.IO) { createTextFile(ctx, te, ep, fileName, ByteArray(0), m.needOut) }
            busy = false
            val uri = cf.uri
            if (!cf.ok || uri == null) {
                err = cf.msg
            } else {
                dirty = false; draftOffer = null
                val nd = IniDoc(MediaItem(uri, cf.name, "", 0L), true, ByteArray(0), TxtDoc.Loaded("", "utf8", false, false), Ini.split(""))
                nd.memo.savedUri = uri
                doc = nd
                note = Pair(cf.msg, true)
            }
        }
    }

    fun guarded(action: () -> Unit) {
        if (doc != null && dirty) pendingStart = action else action()
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
        d.loaded = ld; d.lines = Ini.split(ld.text); d.section = null
        d.pristine = true; d.rev++; dirty = false
    }

    fun save() {
        val d = doc ?: return
        if (saving) return
        val text: String = if (d.pristine) {
            d.loaded.text
        } else {
            val j = Ini.join(d.lines)
            if (!d.loaded.text.endsWith("\n") && j.endsWith("\n")) j.dropLast(1) else j
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

    val rv = doc?.rev ?: 0
    LaunchedEffect(rv, dirty) {
        val dd = doc
        if (dd != null && dirty && rv > 0) {
            delay(2000)
            Drafts.save(ctx, dd.item.uri.toString(), Ini.join(dd.lines))
        }
    }
    LaunchedEffect(doc) {
        if (doc != null) { delay(150); pageScroll.animateScrollTo(pageScroll.maxValue) }
    }

    val requestBack: () -> Unit = {
        val d0 = doc
        if (d0 != null && d0.section != null) d0.section = null
        else if (d0 != null && dirty) confirmBack = true
        else onBack()
    }
    BackHandler(enabled = true) { requestBack() }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(title = "INI / CFG", onBack = requestBack)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(pageScroll),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            TePanelUi(
                panel, ep, te, fold, busy, pickOut, null,
                onCreate = { name -> guarded { createItem(name) } },
                onEdit = { item -> guarded { openItem(item) } },
            )
            if (busy && panel.mode != 2) Hint(te.opening)
            val e = err
            if (e != null && panel.mode != 2) Text(e, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
            val d = doc
            if (d != null && panel.mode != 2) {
                AreaTitle(te.editorArea)
                AreaFileName(d.item.name, dirty)
                Column(Modifier.height(areaH).fillMaxWidth()) {
            val canSave = teCanSave(ep, d.created, d.memo)
            val rev = d.rev
            val entries = remember(rev) { Ini.parse(d.lines) }
            val sec = d.section
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(0.8f)) { SecondaryButton("↶") { if (d.undo()) dirty = true } }
                Box(Modifier.weight(0.8f)) { SecondaryButton("↷") { if (d.redo()) dirty = true } }
                Box(Modifier.weight(1.6f)) { SecondaryButton(te.optBtn) { optOpen = true } }
            }
            Spacer(Modifier.height(8.dp))
            val nt = note
            if (nt != null) {
                NoteCard(nt, te.close) { note = null }
                Spacer(Modifier.height(8.dp))
            }
            // breadcrumb
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "←", color = if (sec == null) p.muted else p.accent, fontSize = 22.sp,
                    modifier = Modifier.clickable(enabled = sec != null) { d.section = null }.padding(end = 12.dp),
                )
                Text(te.rootName, color = if (sec == null) p.text else p.accent, fontSize = 15.sp, modifier = Modifier.clickable { d.section = null })
                if (sec != null) {
                    Text("  ›  ", color = p.muted, fontSize = 15.sp)
                    Text(if (sec == Ini.GLOBAL) te.iniGlobal else "[$sec]", color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline))
            if (sec == null) {
                val names = ArrayList<String>()
                if (entries.any { !it.isSection && it.section == Ini.GLOBAL }) names.add(Ini.GLOBAL)
                for (e in entries) if (e.isSection) names.add(e.section)
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    if (names.isEmpty()) item { Box(Modifier.padding(16.dp)) { Hint(te.iniEmpty) } }
                    items(names.size) { i ->
                        val nm = names[i]
                        val count = entries.count { !it.isSection && it.section == nm }
                        val headLine = entries.firstOrNull { it.isSection && it.section == nm }?.line ?: -1
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().clickable { d.section = nm }.padding(start = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(if (nm == Ini.GLOBAL) te.iniGlobal else "[$nm]", color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(te.itemCount(count), color = p.muted, fontSize = 14.sp)
                                }
                                Text("›", color = p.accent, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                if (nm != Ini.GLOBAL) Box {
                                    Text("⋮", color = p.muted, fontSize = 22.sp, modifier = Modifier.clickable { menuFor = headLine }.padding(horizontal = 14.dp, vertical = 8.dp))
                                    DropdownMenu(expanded = menuFor == headLine, onDismissRequest = { menuFor = -1 }) {
                                        MenuItemText(te.iniRenameSection) { menuFor = -1; renameSec = nm }
                                        MenuItemText(te.menuDelete, true) { menuFor = -1; mutate { Ini.removeSection(d.lines, headLine) } }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline.copy(alpha = 0.4f)))
                        }
                    }
                }
            } else {
                val keys = entries.filter { !it.isSection && it.section == sec }
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    if (keys.isEmpty()) item { Box(Modifier.padding(16.dp)) { Hint(te.emptyContainer) } }
                    items(keys.size) { i ->
                        val e = keys[i]
                        Column(Modifier.fillMaxWidth()) {
                            Row(Modifier.fillMaxWidth().clickable { editLine = e.line }.padding(start = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(e.key, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(if (e.value.isEmpty()) "\"\"" else e.value, color = p.accent, fontSize = 14.sp, fontFamily = FontFamily.Monospace, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                                Box {
                                    Text("⋮", color = p.muted, fontSize = 22.sp, modifier = Modifier.clickable { menuFor = e.line }.padding(horizontal = 14.dp, vertical = 8.dp))
                                    DropdownMenu(expanded = menuFor == e.line, onDismissRequest = { menuFor = -1 }) {
                                        MenuItemText(te.menuEdit) { menuFor = -1; editLine = e.line }
                                        MenuItemText(te.menuDelete, true) { menuFor = -1; mutate { d.lines.removeAt(e.line) } }
                                    }
                                }
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline.copy(alpha = 0.4f)))
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SecondaryButton("＋ " + (if (sec == null) te.iniAddSection else te.iniAddKey)) { if (sec == null) addSec = true else addKey = true } }
                Box(Modifier.weight(1f)) { PrimaryButton(if (saving) te.saving else te.saveBtn, !saving && canSave) { save() } }
            }
            Spacer(Modifier.height(12.dp))
            if (editLine >= 0 && editLine < d.lines.size) {
                val en = entries.firstOrNull { it.line == editLine && !it.isSection }
                if (en != null) {
                    val line = editLine
                    KeyValueDialog(
                        te, te.editTitle(en.key), te.keyLabel, true, te.valueText, en.key, en.value,
                        entries.filter { !it.isSection && it.section == en.section }.map { it.key }.toSet(),
                        onSave = { k, v -> mutate { Ini.setValue(d.lines, line, v); if (k != en.key) Ini.setKey(d.lines, line, k) }; editLine = -1 },
                        onDismiss = { editLine = -1 },
                    )
                }
            }
            if (addKey && sec != null) {
                KeyValueDialog(
                    te, te.iniAddKey, te.keyLabel, true, te.valueText, "", "",
                    entries.filter { !it.isSection && it.section == sec }.map { it.key }.toSet(),
                    onSave = { k, v -> mutate { Ini.addKey(d.lines, sec, k, v) }; addKey = false },
                    onDismiss = { addKey = false },
                )
            }
            if (addSec) {
                KeyValueDialog(
                    te, te.iniAddSection, te.iniSectionName, true, null, "", "",
                    entries.filter { it.isSection }.map { it.section }.toSet(),
                    onSave = { k, _ -> mutate { Ini.addSection(d.lines, k) }; addSec = false },
                    onDismiss = { addSec = false },
                )
            }
            val rs = renameSec
            if (rs != null) {
                val head = entries.firstOrNull { it.isSection && it.section == rs }
                if (head != null) {
                    KeyValueDialog(
                        te, te.iniRenameSection, te.iniSectionName, true, null, rs, "",
                        entries.filter { it.isSection }.map { it.section }.toSet(),
                        onSave = { k, _ -> mutate { Ini.renameSection(d.lines, head.line, k) }; renameSec = null },
                        onDismiss = { renameSec = null },
                    )
                }
            }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    if (optOpen && doc != null) {
        val d = doc!!
        AppDialog(te.optTitle, { optOpen = false }, buttons = { DlgBtn(te.close, true) { optOpen = false } }) {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
                FoldSection(fold, "enc", te.secEnc) {
                    OpenEncBlock(ep, te) { if (dirty) confirmReload = true else reload() }
                    SaveFormatBlock(te, d.fmt)
                }
                if (!d.created) FoldSection(fold, "out", m.secOut) { EditorOutBlock(ep, te, pickOut, true, d.outName) { d.outName = it } }
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
                    d.pushUndo()
                    d.lines = Ini.split(offer)
                    d.pristine = false; d.rev++
                    dirty = true
                    note = Pair(te.draftRestored, true)
                }
                draftOffer = null
            },
            { draftOffer = null },
        )
    }
    if (confirmBack) ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { confirmBack = false; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; onBack() }, { confirmBack = false })
    val ps = pendingStart
    if (ps != null) ConfirmDialog(te.unsavedTitle, te.unsavedMsg, te.leave, te.cancel, { pendingStart = null; doc?.let { Drafts.clear(ctx, it.item.uri.toString()) }; ps() }, { pendingStart = null })
    if (confirmReload) ConfirmDialog(te.reloadTitle, te.reloadMsg, te.reloadYes, te.cancel, { confirmReload = false; reload() }, { confirmReload = false })
}
