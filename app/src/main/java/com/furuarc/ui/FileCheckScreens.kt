package com.furuarc.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.CompareJobs
import com.furuarc.FolderItem
import com.furuarc.FolderJobs
import com.furuarc.TrScanJobs
import com.furuarc.core.*
import com.furuarc.i18n.CheckStr
import com.furuarc.i18n.checkStr
import com.furuarc.settings.Lang
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private val GREEN = Color(0xFF81C784)
private val AMBER = Color(0xFFFFB74D)
private val RED = Color(0xFFE57373)

private fun levelColor(level: Int, muted: Color) = when (level) { 1 -> GREEN; 2 -> AMBER; 3 -> RED; else -> muted }

// ======================= shared components =======================

@Composable
fun FindingRow(f: Finding) {
    val p = LocalPalette.current
    val mark = when (f.level) { 1 -> "✓"; 2 -> "!"; 3 -> "✕"; else -> "•" }
    Row(verticalAlignment = Alignment.Top) {
        Text(mark, color = levelColor(f.level, p.muted), fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.width(22.dp))
        Text(f.text, color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
fun InfoLines(lines: List<Pair<String, String>>) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        lines.forEach { (k, v) ->
            Column {
                Text(k, color = p.muted, fontSize = 12.sp)
                Text(v, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
fun HashLine(k: String, v: String) {
    val p = LocalPalette.current
    Column {
        Text(k, color = p.muted, fontSize = 12.sp)
        Text(v, color = p.text, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

/** Status summary strip: green = healthy, yellow = has warnings, red = has problems. */
@Composable
fun VerdictBanner(v: Verdict, c: CheckStr) {
    val p = LocalPalette.current
    val color = levelColor(v.level, p.muted)
    val mark = when (v.level) { 3 -> "✕"; 2 -> "!"; else -> "✓" }
    val title = when (v.level) { 3 -> c.verdictBad; 2 -> c.verdictWarn; else -> c.verdictOk }
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mark, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (v.level > 1) Text(c.verdictDetail(v.warn, v.bad), color = p.muted, fontSize = 13.sp)
            }
        }
    }
}

/** Buttons below each result: copy and export the .txt report. */
@Composable
fun ExportBar(baseName: String, text: () -> String) {
    val c = checkStr(LocalLang.current)
    val ctx = LocalContext.current
    val latest by rememberUpdatedState(text)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(latest().toByteArray(Charsets.UTF_8)) }
                Toast.makeText(ctx, c.saved, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(ctx, c.saveFail, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val safe = baseName.substringBeforeLast('.').ifBlank { "laporan" }.replace(Regex("[^A-Za-z0-9._-]"), "_")
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SecondaryButton(c.exportCopy) { copyToClipboard(ctx, latest(), c.copied) } }
        Box(Modifier.weight(1f)) { SecondaryButton(c.exportSave) { launcher.launch("${safe}_laporan.txt") } }
    }
}

@Composable
fun Hint(text: String) {
    val p = LocalPalette.current
    Text(text, color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
}

// ======================= File Check menu =======================

@Composable
fun FileCheckMenuScreen(onFile: () -> Unit, onFolder: () -> Unit, onCompare: () -> Unit, onDup: () -> Unit, onHex: () -> Unit, onTr: () -> Unit, onBack: () -> Unit) {
    val c = checkStr(LocalLang.current)
    val ft = com.furuarc.i18n.fileToolsStr(LocalLang.current)
    MenuListScreen(
        c.menuTitle,
        listOf(
            MenuSpec(AppIcons.Archive, c.fileTitle, c.fileSub, onFile),
            MenuSpec(AppIcons.Folder, c.folderTitle, c.folderSub, onFolder),
            MenuSpec(AppIcons.File, c.compareTitle, c.compareSub, onCompare),
            MenuSpec(AppIcons.ContentCopy, ft.dupTitle, ft.dupSub, onDup),
            MenuSpec(AppIcons.MenuBook, ft.hexTitle, ft.hexSub, onHex),
            MenuSpec(AppIcons.Translate, c.trScanTitle, c.trScanSub, onTr),
        ),
        onBack,
    )
}

// ======================= Folder Check =======================

@Composable
fun FolderCheckScreen(onOpenFile: (FolderItem) -> Unit, onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val st by FolderJobs.state.collectAsState()
    var filter by remember { mutableStateOf(0) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) FolderJobs.scan(uri)
    }
    var query by remember { mutableStateOf("") }
    val shown = remember(st.items, filter, query) {
        val byLevel = when (filter) {
            1 -> st.items.filter { it.verdict.level == 2 }
            2 -> st.items.filter { it.verdict.level == 3 }
            3 -> st.items.filter { it.verdict.level == 1 }
            else -> st.items
        }
        val q = query.trim().lowercase()
        if (q.isEmpty()) byLevel else byLevel.filter { it.path.lowercase().contains(q) || it.kind.lowercase().contains(q) || it.summary.lowercase().contains(q) }
    }
    // pagination: 50 files per page, enabled when there are more than 50 results
    val pageSize = 50
    val pages = maxOf(1, (shown.size + pageSize - 1) / pageSize)
    var page by remember(filter, query) { mutableStateOf(0) }
    val curPage = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, curPage) { shown.drop(curPage * pageSize).take(pageSize) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(c.folderTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, c.folderPicker, st.name, AppIcons.Folder) { if (!st.running) pick.launch(null) } }
        if (st.name == null) item { Hint(c.folderHint) }
        st.error?.let { e -> item { ErrorCard(e) } }
        if (st.running) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(c.folderProgress(st.done, st.total), color = p.muted, fontSize = 13.sp)
                    LinearProgressIndicator(if (st.total > 0) st.done.toFloat() / st.total else 0f, Modifier.fillMaxWidth(), color = p.accent)
                    SecondaryButton(checkCancel(LocalLang.current)) { FolderJobs.cancel() }
                }
            }
        }
        if (st.name != null && (st.items.isNotEmpty() || !st.running)) {
            val ok = st.items.count { it.verdict.level == 1 }
            val w = st.items.count { it.verdict.level == 2 }
            val b = st.items.count { it.verdict.level == 3 }
            item {
                SectionCard(c.folderSummary) {
                    Text(c.folderCounts(st.items.size, ok, w, b), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                    if (st.skipped > 0) Text(c.folderSkipped(st.skipped), color = p.muted, fontSize = 12.sp)
                    if (st.remaining > 0) Text(c.folderRemaining(st.remaining), color = AMBER, fontSize = 12.sp)
                }
            }
            st.stats?.let { x ->
                item {
                    SectionCard(c.folderStatsTitle) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(c.folderSize(formatBytes(x.bytes)), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(c.folderFileFolder(x.files, x.folders), color = p.text, fontSize = 14.sp)
                            if (x.emptyFiles > 0) Text(c.folderEmptyFiles(x.emptyFiles), color = p.muted, fontSize = 13.sp)
                            Text(if (x.engines.size > 1) c.folderEngines else c.folderEngine, color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                            if (x.engines.isEmpty()) Text(c.folderEngineNone, color = p.muted, fontSize = 13.sp)
                            x.engines.forEach { e -> Text(c.folderEngineLine(e.name, e.count, e.samples.joinToString(", ")), color = p.text, fontSize = 13.sp, lineHeight = 18.sp) }
                            if (x.engines.isNotEmpty()) DetailLink(c.folderEngineNote, 11.sp, 15.sp)
                            if (x.largest.isNotEmpty()) {
                                Text(c.folderLargest, color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                                x.largest.forEach { (n, sz) -> Text("$n — ${formatBytes(sz)}", color = p.text, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                            }
                            if (x.exts.isNotEmpty()) {
                                Text(c.folderExts, color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
                                Text(x.exts.joinToString("  ·  ") { it.first + " ×" + it.second }, color = p.text, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
            if (st.remaining > 0 && !st.running) {
                item { SecondaryButton(c.folderNextBatch(st.remaining)) { FolderJobs.scanMore() } }
            }
            item { AppTextField(query, { query = it }, c.folderSearch) }
            item { SegmentedChips(listOf(c.filterAll, c.filterWarn, c.filterBad, c.filterOk), filter) { filter = it } }
            if (shown.size > pageSize) {
                item {
                    PageNav(curPage, pages, { page = it })
                }
            }
            if (st.items.isNotEmpty()) item { Hint(c.tapToOpen) }
            if (shown.isEmpty() && !st.running) item { Hint(c.folderNone) }
            items(pageItems) { it ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.clickable { onOpenFile(it) }.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Text(
                            when (it.verdict.level) { 3 -> "✕"; 2 -> "!"; else -> "✓" },
                            color = levelColor(it.verdict.level, p.muted), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                            modifier = Modifier.width(26.dp),
                        )
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(it.path, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text("${it.kind} · ${formatBytes(it.size)}", color = p.muted, fontSize = 12.sp)
                            Text(it.summary, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            if (st.items.isNotEmpty() && !st.running) {
                item { ExportBar(st.name ?: "folder") { FolderJobs.reportText() } }
            }
        }
    }
}
}

private fun checkCancel(l: Lang) = if (l == Lang.ID) "Batalkan" else "Cancel"

// ======================= Compare Checks =======================

@Composable
fun CompareScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val st by CompareJobs.state.collectAsState()
    val pickA = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) CompareJobs.setA(u) }
    val pickB = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) CompareJobs.setB(u) }
    var filter by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(c.compareTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.File, c.labelA, st.nameA, AppIcons.Folder) { if (!st.running) pickA.launch(arrayOf("*/*")) } }
        item { PickerCard(AppIcons.File, c.labelB, st.nameB, AppIcons.Folder) { if (!st.running) pickB.launch(arrayOf("*/*")) } }
        if (st.nameA == null || st.nameB == null) item { Hint(c.compareHint) }
        item { SegmentedChips(listOf(c.modeFast, c.modeDeep), if (st.deep) 1 else 0) { CompareJobs.setDeep(it == 1) } }
        item { DetailHint(c.modeNote) }
        item { PrimaryButton(c.compareBtn, st.nameA != null && st.nameB != null && !st.running) { CompareJobs.run() } }
        if (st.running) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (st.label.isEmpty()) "…" else st.label, color = p.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    LinearProgressIndicator(st.progress, Modifier.fillMaxWidth(), color = p.accent)
                    SecondaryButton(checkCancel(LocalLang.current)) { CompareJobs.cancel() }
                }
            }
        }
        st.error?.let { e -> item { ErrorCard(e) } }

        when (st.kind) {
            1 -> st.diff?.let { d ->
                item {
                    SectionCard(c.diffList) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("+ ${c.added}: ${d.count(DiffItem.ADDED)}", color = GREEN, fontSize = 14.sp)
                            Text("~ ${c.changed}: ${d.count(DiffItem.CHANGED)}", color = AMBER, fontSize = 14.sp)
                            Text("− ${c.removed}: ${d.count(DiffItem.REMOVED)}", color = RED, fontSize = 14.sp)
                            Text("= ${c.same}: ${d.count(DiffItem.SAME)}", color = p.muted, fontSize = 14.sp)
                        }
                    }
                }
                item { SegmentedChips(listOf(c.filterAll, "+", "~", "−"), filter) { filter = it } }
                item { AppTextField(query, { query = it }, c.searchHint) }
                val kindOf = intArrayOf(-1, DiffItem.ADDED, DiffItem.CHANGED, DiffItem.REMOVED)
                val rows = d.items.filter { (filter == 0 && it.kind != DiffItem.SAME || kindOf[filter] == it.kind) && (query.isBlank() || it.name.contains(query, true)) }
                items(rows.take(500)) { x ->
                    val m = when (x.kind) { DiffItem.ADDED -> "+"; DiffItem.REMOVED -> "−"; DiffItem.CHANGED -> "~"; else -> "=" }
                    val col = when (x.kind) { DiffItem.ADDED -> GREEN; DiffItem.REMOVED -> RED; DiffItem.CHANGED -> AMBER; else -> p.muted }
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(m, color = col, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
                        Column(Modifier.weight(1f)) {
                            Text(x.name, color = p.text, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                when (x.kind) { DiffItem.ADDED -> formatBytes(x.sizeB); DiffItem.REMOVED -> formatBytes(x.sizeA); else -> formatBytes(x.sizeA) + " → " + formatBytes(x.sizeB) },
                                color = p.muted, fontSize = 12.sp,
                            )
                        }
                    }
                }
                item { ExportBar(st.nameB ?: "banding") { CompareJobs.reportText() } }
            }
            2 -> st.text?.let { t ->
                item { SectionCard(c.diffList) { Text(c.textSummary(t.linesA, t.linesB, t.same, t.added.size, t.removed.size), color = p.text, fontSize = 14.sp, lineHeight = 20.sp) } }
                item { DetailHint(c.textNote) }
                if (t.added.isNotEmpty()) item {
                    SectionCard(c.addedLines) { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { t.added.take(100).forEach { Text("+ $it", color = GREEN, fontSize = 13.sp, fontFamily = FontFamily.Monospace) } } }
                }
                if (t.removed.isNotEmpty()) item {
                    SectionCard(c.removedLines) { Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { t.removed.take(100).forEach { Text("− $it", color = RED, fontSize = 13.sp, fontFamily = FontFamily.Monospace) } } }
                }
                item { ExportBar(st.nameB ?: "banding") { CompareJobs.reportText() } }
            }
            3 -> st.tr?.let { t ->
                item {
                    SectionCard(c.diffList) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(c.trSummary(t.totalA, t.totalB, t.filledA, t.filledB), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                            Text("+ ${c.added}: ${t.count(TrDiffItem.ADDED)}", color = GREEN, fontSize = 14.sp)
                            Text("− ${c.removed}: ${t.count(TrDiffItem.REMOVED)}", color = RED, fontSize = 14.sp)
                            Text("~ ${c.origChanged}: ${t.count(TrDiffItem.ORIG)}", color = AMBER, fontSize = 14.sp)
                            Text("~ ${c.trChanged}: ${t.count(TrDiffItem.TR)}", color = AMBER, fontSize = 14.sp)
                        }
                    }
                }
                items(t.items.take(300)) { x ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${x.file} | ${x.path}", color = p.muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (x.a.isNotEmpty()) Text("A: ${x.a}", color = RED, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            if (x.b.isNotEmpty()) Text("B: ${x.b}", color = GREEN, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                item { ExportBar(st.nameB ?: "banding") { CompareJobs.reportText() } }
            }
            4 -> st.bin?.let { rows ->
                item { Text(if (st.binSame) c.identical else c.differ, color = if (st.binSame) GREEN else RED, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                item { SectionCard(c.diffList) { InfoLines(rows) } }
                item { ExportBar(st.nameB ?: "banding") { CompareJobs.reportText() } }
            }
        }
    }
}
}

// ======================= Translation Scan =======================

@Composable
fun TrScanScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val st by TrScanJobs.state.collectAsState()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) TrScanJobs.open(u) }
    var cat by remember { mutableStateOf(0) }
    val limits = listOf(30, 40, 50, 60)

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(c.trScanTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Translate, c.trPicker, st.name, AppIcons.Folder) { if (!st.busy) pick.launch(arrayOf("*/*")) } }
        if (st.name == null) item { Hint(c.trHint) }
        if (st.busy) item { BusyBar() }
        st.error?.let { e -> item { ErrorCard(e) } }
        val r = st.result
        if (r != null) {
            val pct = if (r.total > 0) r.filled * 100 / r.total else 0
            item {
                SectionCard(c.folderSummary) {
                    InfoLines(
                        listOf(
                            Pair(c.trTotal, r.total.toString()),
                            Pair(c.trFilled, "${r.filled} ($pct%)"),
                            Pair(c.trLongest, r.longest.toString()),
                        ) + (if (r.dupKeys > 0) listOf(Pair(c.trDupKeys, r.dupKeys.toString())) else emptyList()),
                    )
                    LinearProgressIndicator(pct / 100f, Modifier.fillMaxWidth(), color = p.accent)
                }
            }
            item { Text(c.trMaxLen, color = p.muted, fontSize = 12.sp) }
            item { SegmentedChips(limits.map { it.toString() }, limits.indexOf(st.maxLen)) { TrScanJobs.setMax(limits[it]) } }

            val cats = listOf(
                Triple(c.trCtrl, r.ctrlMismatch, RED),
                Triple(c.trLong, r.tooLong, AMBER),
                Triple(c.trIncons, r.inconsistent, AMBER),
                Triple(c.trSame, r.sameAsOrig, p.muted),
                Triple(c.trEmpty, r.empty, p.muted),
            )
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(vertical = 6.dp)) {
                        cats.forEachIndexed { i, (label, list, col) ->
                            Row(
                                Modifier.fillMaxWidth().clickable { cat = i }.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(label, color = if (i == cat) p.accent else p.text, fontWeight = if (i == cat) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                                Text(list.size.toString(), color = if (list.isEmpty()) GREEN else col, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            val (_, shown, _) = cats[cat]
            if (cat == 2) item { Hint(c.trInconsNote) }
            if (shown.isEmpty()) item { Text(c.trAllGood, color = GREEN, fontSize = 14.sp) }
            else {
                item { Text("${c.trShowing} ${minOf(shown.size, 200)} / ${shown.size}", color = p.muted, fontSize = 12.sp) }
                items(shown.take(200)) { x ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("${x.file} | ${x.path}", color = p.muted, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${c.trOrig}: ${x.orig}", color = p.text, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            if (x.tr.isNotBlank()) Text("${c.trTr}: ${x.tr}", color = p.accent, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
            item { ExportBar(st.name ?: "terjemahan") { TrScanJobs.reportText() } }
        }
    }
}
}
