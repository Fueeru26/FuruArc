package com.furuarc.ui

import androidx.compose.foundation.layout.PaddingValues
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.NameOptDone
import com.furuarc.NameOptJobs
import com.furuarc.NameOptSnap
import com.furuarc.core.NameOpt
import com.furuarc.i18n.fileToolsStr
import com.furuarc.i18n.nameOptStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val NO_PER_PAGE = 50

/** List of paths for the "View details (?)" dialog, truncated so the dialog is not too long. */
private fun listText(items: List<String>, max: Int = 80): String {
    val shown = items.take(max).joinToString("\n")
    return if (items.size > max) shown + "\n… +" + (items.size - max) else shown
}

private class Preview(val plan: NameOpt.Plan, val scan: NameOpt.RefScan, val key: String)

/** File Name Optimizer: renames files/folders with special characters and updates their references in the game's text files. */
@Composable
fun NameOptScreen(onBack: () -> Unit, onMixed: () -> Unit) {
    val p = LocalPalette.current
    val lang = LocalLang.current
    val n = nameOptStr(lang)
    val f = fileToolsStr(lang)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var src by remember { mutableStateOf<Uri?>(null) }
    var srcName by remember { mutableStateOf<String?>(null) }
    var backup by remember { mutableStateOf<Uri?>(null) }
    var backupName by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<NameOpt.Entry>>(emptyList()) }
    var listing by remember { mutableStateOf(false) }

    var scopeIdx by remember { mutableStateOf(0) }
    var styleIdx by remember { mutableStateOf(0) }
    var folders by remember { mutableStateOf(true) }
    var symbols by remember { mutableStateOf(false) }
    var wide by remember { mutableStateOf(false) }
    var caseFix by remember { mutableStateOf(false) }
    var textExts by remember { mutableStateOf(NameOpt.DEFAULT_TEXT_EXTS) }

    var preview by remember { mutableStateOf<Preview?>(null) }
    val job by NameOptJobs.state.collectAsState()
    val busy = job.running
    val busyText: String? = if (busy) job.text else null
    val progress = job.progress
    var confirm by remember { mutableStateOf(false) }
    var confirmUndo by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<NameOpt.RunResult?>(null) }
    var resultNote by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var page by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var showList by remember { mutableStateOf(false) }
    var listTab by remember { mutableStateOf(0) }
    var listQuery by remember { mutableStateOf("") }
    var listPage by remember { mutableStateOf(0) }

    val scanBusy = busy && job.kind == 0
    BackHandler(enabled = showList) { showList = false }

    fun opts() = NameOpt.Opts(
        mediaOnly = scopeIdx == 1, style = styleIdx, folders = folders, symbols = symbols, wide = wide,
        caseFix = caseFix && !wide,
        textExts = textExts.split(',').map { it.trim().trimStart('.').lowercase() }.filter { it.isNotEmpty() }.toSet(),
    )
    val key = listOf(scopeIdx, styleIdx, folders, symbols, wide, caseFix, textExts).joinToString("|")
    val fresh = preview?.key == key

    fun reload() {
        val u = src ?: return
        listing = true
        scope.launch {
            val l = withContext(Dispatchers.IO) { try { NameOpt.list(ctx, u) } catch (e: Exception) { emptyList() } }
            entries = l
            listing = false
        }
    }

    val pickSrc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            src = u
            srcName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
            preview = null; result = null; resultNote = null; page = 0
            reload()
        }
    }
    val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            backup = u
            backupName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
        }
    }

    val ready = remember(entries) { NameOpt.readiness(entries) }
    val hasMapping = remember(entries) { NameOpt.hasMapping(entries) }

    fun snap(u: Uri) = NameOptSnap(u, srcName ?: "", backup, backupName, scopeIdx, styleIdx, folders, symbols, wide, caseFix, textExts)

    fun restoreSnap(sn: NameOptSnap) {
        src = sn.src; srcName = sn.srcName; backup = sn.backup; backupName = sn.backupName
        scopeIdx = sn.scopeIdx; styleIdx = sn.styleIdx; folders = sn.folders; symbols = sn.symbols
        wide = sn.wide; caseFix = sn.caseFix; textExts = sn.textExts
    }

    fun doScan() {
        val u = src ?: return
        resultNote = null; result = null; showList = false
        NameOptJobs.scan(snap(u), opts(), key)
    }

    fun doRun() {
        val u = src ?: return
        val pv = preview ?: return
        resultNote = null
        NameOptJobs.run(snap(u), pv.plan, pv.scan, opts())
    }

    fun doUndo() {
        val u = src ?: return
        resultNote = null; result = null
        NameOptJobs.undo(snap(u))
    }

    fun apply(d: NameOptDone) {
        restoreSnap(d.snap)
        when (d.kind) {
            0 -> {
                d.entries?.let { entries = it }
                val pl = d.plan
                val sc = d.scan
                if (pl != null && sc != null) {
                    preview = Preview(pl, sc, d.key)
                    page = 0; query = ""
                } else resultNote = n.fail("") to false
            }
            1 -> {
                val r = d.run
                result = r
                preview = null
                if (r?.error != null) resultNote = n.fail(r.error) to false
                reload()
            }
            else -> {
                val r = d.undo
                if (r != null) {
                    resultNote = if (r.error != null) n.undoNone to false
                    else (n.undoDone(r.files, r.dirs, r.edited) + (if (r.failed.isNotEmpty()) " " + n.resFailed(r.failed.size) else "")) to r.failed.isEmpty()
                }
                result = null
                preview = null
                reload()
            }
        }
    }

    LaunchedEffect(Unit) {
        NameOptJobs.pendingSnap()?.let { restoreSnap(it); reload() }
    }
    LaunchedEffect(job.serial) {
        NameOptJobs.take()?.let { apply(it) }
    }

    if (confirm) {
        val total = preview?.plan?.moves?.size ?: 0
        val has = backup != null
        ConfirmDialog(
            title = n.confirmTitle(total),
            message = if (has) n.confirmBackup(backupName ?: "") else n.confirmNoBackup,
            yes = if (has) n.confirmYes else n.confirmYesNoBackup,
            no = n.confirmNo,
            onConfirm = { confirm = false; doRun() },
            onDismiss = { confirm = false },
        )
    }
    if (confirmUndo) {
        ConfirmDialog(
            title = n.undoTitle, message = n.undoMsg, yes = n.undoYes, no = n.confirmNo,
            onConfirm = { confirmUndo = false; doUndo() },
            onDismiss = { confirmUndo = false },
        )
    }

    val pv = preview?.takeIf { fresh }
    val moves = pv?.plan?.moves ?: emptyList()
    val visible = remember(moves, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) moves else moves.filter { it.entry.rel.lowercase().contains(q) || it.newName.lowercase().contains(q) }
    }
    val pages = maxOf(1, (visible.size + NO_PER_PAGE - 1) / NO_PER_PAGE)
    val curPage = page.coerceIn(0, pages - 1)
    val pageItems = remember(visible, curPage) { visible.drop(curPage * NO_PER_PAGE).take(NO_PER_PAGE) }
    val unref = remember(pv) {
        if (pv == null || pv.scan.scanned == 0) emptyList()
        else pv.plan.moves.filter { m -> !m.entry.isDir && m.entry.name !in pv.scan.used && m.entry.name.substringBeforeLast('.') !in pv.scan.used }.map { it.entry.rel }
    }

    if (showList && pv != null) {
        CheckedListPage(n, f, pv.scan.files.map { it.entry.rel to (it.rename + it.caseFix) }, unref, listTab, { listTab = it; listPage = 0 }, listQuery, { listQuery = it; listPage = 0 }, listPage, { listPage = it }) { showList = false }
        return
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(n.menuTitle, { if (!busy) onBack() })
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, n.srcFolder, srcName, AppIcons.Folder) { if (!busy) pickSrc.launch(null) } }
        item { DetailHint(n.intro) }

        item { AppDropdown(n.scope, scopeIdx.toString(), n.scopeOpts.mapIndexed { i, s -> i.toString() to s }, !busy) { scopeIdx = it.toInt() } }
        item { DetailHint(n.scopeHint) }
        item { AppDropdown(n.style, styleIdx.toString(), n.styleOpts.mapIndexed { i, s -> i.toString() to s }, !busy) { styleIdx = it.toInt() } }
        item { DetailHint(n.styleHint) }
        item { CheckRow(n.chkFolders, folders) { if (!busy) folders = it } }
        item { CheckRow(n.chkSymbols, symbols) { if (!busy) symbols = it } }
        item { DetailHint(n.symbolsHint) }
        item { AppDropdown(n.refMode, if (wide) "1" else "0", n.refModes.mapIndexed { i, s -> i.toString() to s }, !busy) { wide = it == "1" } }
        item { DetailHint(n.refHint) }
        if (!wide) {
            item { CheckRow(n.chkCase, caseFix) { if (!busy) caseFix = it } }
            item { DetailHint(n.caseHint) }
        }
        item { AppTextField(value = textExts, onValueChange = { if (!busy) textExts = it.take(120) }, label = n.textExts) }
        item { SecondaryButton(n.refill, !busy && textExts != NameOpt.DEFAULT_TEXT_EXTS) { textExts = NameOpt.DEFAULT_TEXT_EXTS } }
        item { Hint(n.textExtsHint) }

        item { PickerCard(AppIcons.Folder, n.backupFolder, backupName, AppIcons.Folder) { if (!busy) pickBackup.launch(null) } }
        if (backup != null) item { SecondaryButton(n.clearBackup, !busy) { backup = null; backupName = null } }

        if (src == null) {
            item { Hint(n.noFolder) }
        } else if (listing) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Hint(n.loading)
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
                }
            }
        } else {
            // ---- Build Readiness Report ----
            item { SectionLabel(n.readyTitle) }
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(n.readyCounts(ready.files, ready.dirs), color = p.text, fontSize = 14.sp)
                        if (ready.nonAscii > 0) Text(n.readyNonAscii(ready.nonAscii), color = WarnColor, fontSize = 14.sp)
                        if (ready.symbols > 0) Text(n.readySymbols(ready.symbols), color = WarnColor, fontSize = 14.sp)
                        if (ready.longNames.isNotEmpty()) Text(n.readyLong(ready.longNames.size), color = WarnColor, fontSize = 14.sp)
                        if (ready.caseDups.isNotEmpty()) Text(n.readyCase(ready.caseDups.size), color = ErrColor, fontSize = 14.sp)
                        if (ready.badFormats.isNotEmpty()) Text(n.readyBad(ready.badFormats.entries.joinToString(", ") { it.key.uppercase() + " ×" + it.value }), color = WarnColor, fontSize = 14.sp)
                        if (ready.clean) Text(n.readyOk, color = OkColor, fontSize = 14.sp)
                        val probs = ready.longNames + ready.caseDups
                        if (probs.isNotEmpty()) DetailLink(n.readyDetail + ":\n" + listText(probs))
                    }
                }
            }

            item { PrimaryButton(n.scanBtn, !busy && entries.isNotEmpty()) { doScan() } }
            if (busy && scanBusy) item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(busyText ?: "", color = p.muted, fontSize = 13.sp)
                    LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
                }
            }
            if (preview != null && !fresh) item { Hint(n.stale) }

            // ---- Preview ----
            if (pv != null) {
                item { SectionLabel(n.prevTitle) }
                if (moves.isEmpty() && pv.scan.files.isEmpty()) {
                    item { Hint(n.prevNone) }
                } else {
                    if (moves.isEmpty()) item { Hint(n.prevOnlyCase) }
                    item { Hint(n.prevSummary(pv.plan.files, pv.plan.dirs, pv.scan.files.size, pv.scan.replaced, pv.scan.caseFixes)) }
                    if (pv.scan.skipped.isNotEmpty()) {
                        item { Hint(n.prevSkipped(pv.scan.skipped.size)) }
                        item { DetailLink(listText(pv.scan.skipped)) }
                    }
                    if (unref.isNotEmpty()) item { Hint(n.prevUnref(unref.size)) }
                    item { SecondaryButton(n.checkedBtn, !busy) { showList = true; listTab = 0; listQuery = ""; listPage = 0 } }
                    item { AppTextField(value = query, onValueChange = { v -> query = v; page = 0 }, label = f.renSearch) }
                    if (visible.isEmpty()) item { Hint(f.renNoMatch) }
                    if (pages > 1) item { RenPager(curPage, pages, n.pageLabel(curPage + 1, pages, visible.size)) { np -> page = np } }
                    items(pageItems) { m ->
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text((if (m.entry.isDir) n.folderTag else "") + m.entry.rel, color = p.muted, fontSize = 12.sp, lineHeight = 16.sp)
                                Text("→ " + m.newName, color = p.text, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (pages > 1) item { RenPager(curPage, pages, n.pageLabel(curPage + 1, pages, visible.size)) { np -> page = np } }
                }
            }

            item { PrimaryButton(n.runBtn, !busy && pv != null && (moves.isNotEmpty() || (pv.scan.files.isNotEmpty()))) { confirm = true } }
            if (hasMapping) item { SecondaryButton(n.undoBtn, !busy) { confirmUndo = true } }
        }

        if (busy && !scanBusy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(busyText ?: "", color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
            }
        }

        // ---- Result ----
        result?.let { r ->
            if (r.error == null) item {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(n.resDone(r.files, r.dirs, r.edited, r.replaced) + (if (r.caseFixes > 0) " (+${r.caseFixes})" else ""), color = if (r.failed.isEmpty()) OkColor else WarnColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (r.backedUp > 0 && backupName != null) Text(n.resBackup(r.backedUp, backupName ?: ""), color = p.text, fontSize = 13.sp)
                        Text(n.resMapping, color = p.muted, fontSize = 13.sp)
                        if (r.failed.isNotEmpty()) {
                            Text(n.resFailed(r.failed.size), color = ErrColor, fontSize = 13.sp)
                            DetailLink(n.resFailedList + ":\n" + listText(r.failed))
                        }
                        if (r.unreferenced.isNotEmpty()) {
                            Text(n.prevUnref(r.unreferenced.size), color = p.muted, fontSize = 13.sp)
                            DetailLink(n.resUnrefList + ":\n" + listText(r.unreferenced))
                        }
                        if (r.skipped.isNotEmpty()) {
                            Text(n.prevSkipped(r.skipped.size), color = p.muted, fontSize = 13.sp)
                            DetailLink(listText(r.skipped))
                        }
                        Text(n.resNext, color = p.muted, fontSize = 13.sp)
                        SecondaryButton(n.gotoMixed, !busy) { onMixed() }
                        SecondaryButton(n.close) { result = null }
                    }
                }
            }
        }
        resultNote?.let { nt -> item { NoteCard(nt, n.close) { resultNote = null } } }
    }
}
}

/** "Checked files list" page: Changed text files / Files without references, each with search and 50 per page. */
@Composable
private fun CheckedListPage(
    n: com.furuarc.i18n.NameOptStr,
    f: com.furuarc.i18n.FileToolsStr,
    edited: List<Pair<String, Int>>,
    unref: List<String>,
    tab: Int,
    onTab: (Int) -> Unit,
    query: String,
    onQuery: (String) -> Unit,
    page: Int,
    onPage: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val p = LocalPalette.current
    val all: List<String> = if (tab == 0) edited.map { it.first + "  (" + it.second + ")" } else unref
    val shown = remember(all, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) all else all.filter { it.lowercase().contains(q) }
    }
    val pages = maxOf(1, (shown.size + NO_PER_PAGE - 1) / NO_PER_PAGE)
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * NO_PER_PAGE).take(NO_PER_PAGE) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(n.checkedTitle, onClose)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { SegmentedChips(listOf(n.tabEdited, n.tabUnref), tab, 14.sp) { onTab(it) } }
        item { AppTextField(value = query, onValueChange = { onQuery(it) }, label = f.renSearch) }
        if (all.isEmpty()) item { Hint(n.listEmpty) } else if (shown.isEmpty()) item { Hint(f.renNoMatch) }
        if (pages > 1) item { RenPager(cur, pages, f.renPageLabel(cur + 1, pages, shown.size)) { onPage(it) } }
        items(pageItems) { line ->
            AppCard(Modifier.fillMaxWidth()) {
                Text(line, color = p.text, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
            }
        }
        if (pages > 1) item { RenPager(cur, pages, f.renPageLabel(cur + 1, pages, shown.size)) { onPage(it) } }
    }
}
}
