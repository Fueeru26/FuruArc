package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.furuarc.MainViewModel
import com.furuarc.PluginCheckJobs
import com.furuarc.PluginDiffJobs
import com.furuarc.PluginListJobs
import com.furuarc.PluginScanJobs
import com.furuarc.core.Json
import com.furuarc.core.PItem
import com.furuarc.core.PluginDiffItem
import com.furuarc.core.PluginEntry
import com.furuarc.core.PluginIssue
import com.furuarc.core.RpgmText
import com.furuarc.i18n.PluginStr
import com.furuarc.i18n.pl
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

private const val PLUGIN_PAGE = 50
private val OK_GREEN = Color(0xFF81C784)
private val BAD_RED = Color(0xFFE57373)
private val WARN_AMBER = Color(0xFFFFB74D)

@Composable
private fun PluginPager(page: Int, pages: Int, label: String, onPage: (Int) -> Unit) {
    PageNav(page, pages, onPage)
}

/** Section title: accent text in the center with a line on the left and right (like Translation Area). */
@Composable
private fun PluginSectionTitle(text: String) = GroupTitle(text)

/** Page of all plugin information from the scan: search + 50 per page. */
@Composable
private fun PluginInfoPage(files: List<com.furuarc.core.PluginFileInfo>, onClose: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    var query by remember { mutableStateOf("") }
    val shown = remember(files, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) files else files.filter { it.file.lowercase().contains(q) || it.desc.lowercase().contains(q) }
    }
    val pages = maxOf(1, (shown.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    var page by remember(query, files) { mutableStateOf(0) }
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.infoPageTitle, onClose)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { AppTextField(query, { query = it }, pl.infoSearch) }
        if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
        if (shown.isEmpty()) item { Hint(pl.noneMatch) }
        items(pageItems) { f ->
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(f.file, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (f.desc.isNotEmpty()) Text(f.desc, color = p.muted, fontSize = 12.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text(pl.infoLine(f.params, f.textCount, f.valueCount), color = p.muted, fontSize = 12.sp)
                }
            }
        }
        if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
        item { SecondaryButton(pl.infoClose, onClose) }
    }
}
}

private fun levelColor(level: Int) = when (level) { 3 -> BAD_RED; 2 -> WARN_AMBER; else -> Color(0xFF90A4AE) }
private fun levelLabel(pl: PluginStr, level: Int) = when (level) { 3 -> pl.lvlErr; 2 -> pl.lvlWarn; else -> pl.lvlInfo }

@Composable
private fun IssueCard(pl: PluginStr, i: PluginIssue, onIgnore: ((PluginIssue, Boolean) -> Unit)? = null) {
    val p = LocalPalette.current
    var open by remember(i) { mutableStateOf(false) }
    var ask by remember(i) { mutableStateOf(false) }
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().clickable(enabled = i.code.isNotEmpty()) { open = !open }.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(levelLabel(pl, i.level) + " · " + i.plugin, color = levelColor(i.level), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(i.text, color = p.text, fontSize = 13.sp, lineHeight = 18.sp)
            val conf = if (i.maybe) pl.maybeIntended else if (i.level == 3) pl.sureProblem else null
            if (conf != null) Text(conf, color = p.muted, fontSize = 11.sp)
            if (i.code.isNotEmpty()) {
                if (open) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(p.trailingBg).horizontalScroll(rememberScrollState()).padding(10.dp)) {
                        Text(i.code, color = p.text, fontSize = 11.sp, lineHeight = 16.sp, fontFamily = FontFamily.Monospace, softWrap = false)
                    }
                } else Text("▸ " + pl.tapForCode, color = p.accent, fontSize = 11.sp)
            }
            if (onIgnore != null && i.kind.isNotEmpty()) Text(pl.ignoreBtn, color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { ask = true }.padding(top = 2.dp))
        }
    }
    if (ask && onIgnore != null) {
        AppAlert(
            onDismissRequest = { ask = false },
            title = { Text(pl.ignoreTitle, fontWeight = FontWeight.Bold, color = p.text) },
            text = { Text(pl.kindName(i.kind) + " · " + i.plugin + "\n\n" + pl.ignoreBody, color = p.text, fontSize = 14.sp) },
            buttons = {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { ask = false; onIgnore(i, false) }, modifier = Modifier.fillMaxWidth()) { Text(pl.ignoreThis, color = p.accent) }
                    TextButton(onClick = { ask = false; onIgnore(i, true) }, modifier = Modifier.fillMaxWidth()) { Text(pl.ignoreKind, color = p.accent) }
                    TextButton(onClick = { ask = false }, modifier = Modifier.fillMaxWidth()) { Text(pl.ignoreCancel, color = p.muted) }
                }
            },
        )
    }
}

@Composable
private fun IgnoredDialog(pl: PluginStr, keys: Set<String>, onRestore: (String) -> Unit, onRestoreAll: () -> Unit, onClose: () -> Unit) {
    val p = LocalPalette.current
    AppAlert(
        onDismissRequest = onClose,
        title = { Text(pl.ignoredTitle, fontWeight = FontWeight.Bold, color = p.text) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                keys.sorted().forEach { k ->
                    val kind = k.substringBefore('|'); val who = k.substringAfter('|')
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(pl.kindName(kind) + " · " + (if (who == "*") pl.allPlugins else who), color = p.text, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onRestore(k) }) { Text(pl.restoreOne, color = p.accent) }
                    }
                }
            }
        },
        buttons = {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onRestoreAll) { Text(pl.restoreAll, color = p.accent) }
                TextButton(onClick = onClose) { Text(pl.infoClose, color = p.muted) }
            }
        },
    )
}

// ============================ Plugin Scanner ============================

private class ScanRow(val fileIdx: Int, val itemIdx: Int)

@Composable
fun PluginScanScreen(confirmExit: Boolean = false, onBack: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val st by PluginScanJobs.state.collectAsState()
    val edits = remember { mutableStateMapOf<String, String>() }
    val scope = rememberCoroutineScope()
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) PluginScanJobs.pickFolder(u) }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) PluginScanJobs.setOut(u) }
    val pickJsFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginScanJobs.pickPluginsJs(u) }
    val pickTr = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            scope.launch {
                val r = withContext(kotlinx.coroutines.Dispatchers.Default) { PluginScanJobs.importTr(u) }
                for ((k, v) in r) edits[k.first + "\u0000" + k.second] = v
            }
        }
    }
    LaunchedEffect(Unit) { PluginScanJobs.useDefaultOut() }
    val leaveAsk = confirmExit && edits.isNotEmpty()
    var showLeave by remember { mutableStateOf(false) }
    val back: () -> Unit = { if (leaveAsk) showLeave = true else onBack() }
    var trFmt by remember { mutableStateOf(RpgmText.FMT_TXT) }
    var showInfo by remember { mutableStateOf(false) }
    androidx.activity.compose.BackHandler(enabled = showInfo) { showInfo = false }
    androidx.activity.compose.BackHandler(enabled = leaveAsk && !showInfo) { showLeave = true }
    var filter by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var page by remember(filter, query, st.files) { mutableStateOf(0) }

    LaunchedEffect(st.files) { edits.clear() }

    val totalTexts = st.files.sumOf { it.textCount }
    val totalValues = st.files.sumOf { it.valueCount }
    val rows: List<ScanRow> = remember(st.files, filter, query) {
        val q = query.trim().lowercase()
        val out = ArrayList<ScanRow>()
        for ((fi, f) in st.files.withIndex()) for ((ii, it) in f.items.withIndex()) {
            if (filter == 1 && !it.isText) continue
            if (filter == 2 && !it.isValue) continue
            if (q.isNotEmpty() && !(it.orig.lowercase().contains(q) || it.label.lowercase().contains(q) || f.file.lowercase().contains(q))) continue
            out.add(ScanRow(fi, ii))
        }
        out
    }
    val pages = maxOf(1, (rows.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    val cur = page.coerceIn(0, pages - 1)
    val pageRows = remember(rows, cur) { rows.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }

    if (showLeave) ConfirmDialog(
        pl.unsaved, pl.leaveMsg, pl.leaveYes, pl.leaveNo,
        onConfirm = { showLeave = false; onBack() },
        onDismiss = { showLeave = false },
    )
    if (showInfo) PluginInfoPage(st.files) { showInfo = false } else Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.scanTitle, back)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, pl.folderLabel, st.folder, AppIcons.Folder) { if (!st.busy) pickFolder.launch(null) } }
        item { PickerCard(AppIcons.File, pl.pluginsJsLabel, st.jsName, AppIcons.File) { if (!st.busy) pickJsFile.launch(arrayOf("*/*")) } }
        if (st.folder == null) item { DetailHint(pl.scanHint + " " + pl.pluginsJsHint) }
        item { PrimaryButton(pl.scanBtn, st.folder != null && !st.busy) { PluginScanJobs.scan() } }
        if (st.busy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(pl.scanning, color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
            }
        }
        val err = st.error
        if (err != null) item { ErrorCard(err) }
        if (st.scanned && !st.busy) {
            if (st.files.isEmpty()) item { Hint(pl.noPlugins) }
            else {
                item {
                    SectionCard(pl.infoTitle) {
                        Text(pl.summary(st.files.size, totalTexts, totalValues), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (totalTexts + totalValues == 0) Text(pl.nothingEditable, color = p.muted, fontSize = 13.sp)
                    }
                }
                item { SecondaryButton(pl.infoAll) { showInfo = true } }
                if (totalTexts + totalValues > 0) {
                    item { PluginSectionTitle(pl.areaTitle) }
                    item { DetailHint(pl.areaHint) }
                    item { PickerCard(AppIcons.FolderUpload, pl.outLabel, st.outName, AppIcons.Folder) { pickOut.launch(null) } }
                    item { Hint(pl.outNote + " " + pl.bakNote) }
                    item {
                        SectionCard(pl.trSectionTitle) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                SegmentedChips(listOf("CSV", "TXT", "JSON"), trFmt) { trFmt = it }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Box(Modifier.weight(1f)) { SecondaryButton(pl.trExportBtn, !st.trBusy) { PluginScanJobs.exportTr(trFmt) } }
                                    Box(Modifier.weight(1f)) { SecondaryButton(pl.trImportBtn, !st.trBusy) { pickTr.launch(arrayOf("*/*")) } }
                                }
                            }
                        }
                    }
                    if (st.trBusy) item { BusyBar() }
                    st.trNote?.let { (txt, ok) ->
                        item {
                            AppCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(txt, color = if (ok) OK_GREEN else Color(0xFFE57373), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    SecondaryButton(pl.closeNote) { PluginScanJobs.dismissTr() }
                                }
                            }
                        }
                    }
                    item { SegmentedChips(listOf(pl.filterAll, pl.filterText, pl.filterValue), filter) { filter = it } }
                    item { AppTextField(query, { query = it }, pl.search) }
                    if (rows.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, rows.size)) { page = it } }
                    items(pageRows) { r ->
                        val f = st.files[r.fileIdx]
                        val it = f.items[r.itemIdx]
                        val key = f.file + "\u0000" + r.itemIdx
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val kind = when (it.kind) {
                                    PItem.KIND_CODE_STR -> pl.kindCode
                                    PItem.KIND_ANNO_TEXT -> pl.kindAnno
                                    PItem.KIND_CODE_VALUE -> pl.kindCodeVal
                                    PItem.KIND_ANNO_VALUE -> pl.kindAnnoVal
                                    PItem.KIND_PARAM_TEXT -> pl.kindParamText
                                    else -> pl.kindParamVal
                                }
                                Text(f.file + " · " + kind + (if (it.label.isNotEmpty()) " · " + it.label else "") + (if (it.line > 0) " · L" + it.line else ""), color = p.accent, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(if (it.isValue) pl.currentValue + ": " + it.orig else it.orig, color = p.text, fontSize = 14.sp, lineHeight = 19.sp, maxLines = 8, overflow = TextOverflow.Ellipsis)
                                AppTextField(edits[key] ?: "", { v -> if (v.isEmpty()) { edits.remove(key) } else { edits[key] = v } }, if (it.isText) pl.fieldText else pl.fieldValue, multiline = it.isText)
                            }
                        }
                    }
                    if (rows.isEmpty()) item { Hint(pl.noneMatch) }
                    if (rows.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, rows.size)) { page = it } }
                    item {
                        PrimaryButton(pl.apply, !st.applying) {
                            val grouped = HashMap<String, HashMap<Int, String>>()
                            for ((k, v) in edits) {
                                val i = k.indexOf('\u0000')
                                grouped.getOrPut(k.substring(0, i)) { HashMap() }[k.substring(i + 1).toInt()] = v
                            }
                            PluginScanJobs.apply(grouped)
                        }
                    }
                    if (st.applying) item { BusyBar() }
                    st.message?.let { m -> item { Text(m, color = OK_GREEN, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) } }
                }
            }
        }
    }
}
}

// ============================ Plugin profile (stored in app settings) ============================

private const val PROFILE_KEY = "plugin_profiles"

private fun loadProfiles(vm: MainViewModel): LinkedHashMap<String, Map<String, Boolean>> {
    val r = LinkedHashMap<String, Map<String, Boolean>>()
    val raw = vm.pref(PROFILE_KEY, "")
    if (raw.isBlank()) return r
    try {
        val root = Json.parse(raw) as? Map<*, *>
        if (root != null) for ((k, v) in root) {
            val m = v as? Map<*, *> ?: continue
            val mm = LinkedHashMap<String, Boolean>()
            for ((a, b) in m) if (a is String) mm[a] = (b == true)
            if (k is String) r[k] = mm
        }
    } catch (e: Exception) { }
    return r
}

private fun saveProfiles(vm: MainViewModel, m: Map<String, Map<String, Boolean>>) = vm.setPref(PROFILE_KEY, Json.write(m))

// ============================ Plugin.js Editor ============================

@Composable
fun PluginEditorScreen(vm: MainViewModel, onBack: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val st by PluginListJobs.state.collectAsState()
    val pickJs = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginListJobs.openPluginsJs(u) }
    val pickNew = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginListJobs.pickNewPlugin(u) }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) PluginListJobs.setOut(u) }
    LaunchedEffect(Unit) { PluginListJobs.useDefaultOut() }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) PluginListJobs.pickFolder(u) }
    var filter by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var page by remember(filter, query, st.entries) { mutableStateOf(0) }
    var selected by remember { mutableStateOf<PluginEntry?>(null) }
    var showLeave by remember { mutableStateOf(false) }
    var showSaveProfile by remember { mutableStateOf(false) }
    var showApplyProfile by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf(loadProfiles(vm)) }
    var profMsg by remember { mutableStateOf<String?>(null) }

    // st.rev is included as a key so the list is recomputed when the status/order changes
    val shown: List<PluginEntry> = remember(st.entries, st.rev, filter, query) {
        val q = query.trim().lowercase()
        st.entries.filter { e ->
            (filter == 0 || (filter == 1 && e.enabled) || (filter == 2 && !e.enabled)) &&
                (q.isEmpty() || e.name.lowercase().contains(q))
        }
    }
    val warned: Set<String> = remember(st.issues) { st.issues.filter { it.level >= 2 }.map { it.plugin.lowercase() }.toSet() }
    val pages = maxOf(1, (shown.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }
    val hasList = st.name != null

    val askLeave = vm.settings.collectAsState().value.confirmExit && st.dirty
    androidx.activity.compose.BackHandler(enabled = askLeave) { showLeave = true }
    var showIssues by remember { mutableStateOf(false) }
    var paramEntry by remember { mutableStateOf<PluginEntry?>(null) }
    androidx.activity.compose.BackHandler(enabled = showIssues || paramEntry != null) { showIssues = false; paramEntry = null }

    val pe = paramEntry
    if (pe != null) {
        PluginParamsPage(pe) { paramEntry = null }
    } else if (showIssues) {
        PluginIssuesPage(st.issues) { showIssues = false }
    } else Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.editTitle, { if (askLeave) showLeave = true else onBack() })
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.File, pl.upPluginsJs, st.name, AppIcons.File) { pickJs.launch(arrayOf("*/*")) } }
        item { PickerCard(AppIcons.Folder, pl.folderOptLabel, st.folderName, AppIcons.Folder) { pickFolder.launch(null) } }
        item { PickerCard(AppIcons.FolderUpload, pl.outLabel, st.outName, AppIcons.Folder) { pickOut.launch(null) } }
        item { PrimaryButton(pl.savePluginsJs, hasList && !st.busy) { PluginListJobs.save() } }
        item { DetailHint(pl.saveNote + " " + pl.bakNote) }
        if (st.busy) item { BusyBar() }
        val err = st.error
        if (err != null) item { ErrorCard(err) }
        st.message?.let { m -> item { Text(m, color = OK_GREEN, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) } }
        profMsg?.let { m -> item { Text(m, color = OK_GREEN, fontWeight = FontWeight.SemiBold, fontSize = 14.sp) } }
        if (st.dirty) item { Text(pl.unsaved, color = WARN_AMBER, fontSize = 13.sp) }
        if (hasList) {
            item { SecondaryButton(pl.viewIssues(st.issues.size)) { showIssues = true } }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) { SecondaryButton(pl.profSave) { showSaveProfile = true } }
                    Box(Modifier.weight(1f)) { SecondaryButton(pl.profApply) { profiles = loadProfiles(vm); showApplyProfile = true } }
                }
            }
            item { PickerCard(AppIcons.FolderPlus, pl.upNewPlugin, st.pendingName?.let { pl.pendingNew(it) }, AppIcons.File) { pickNew.launch(arrayOf("*/*")) } }
            item { PrimaryButton(pl.addPlugin, st.pendingName != null) { PluginListJobs.addPending() } }
            panel(pl.listTitle) {
                Text(pl.count(st.entries.size, st.entries.count { it.enabled }), color = p.muted, fontSize = 13.sp)
                AppTextField(query, { query = it }, pl.searchPlugin)
                SegmentedChips(listOf(pl.fAll, pl.fOn, pl.fOff), filter) { filter = it }
                if (shown.size > PLUGIN_PAGE) PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it }
                if (shown.isEmpty()) Hint(pl.noneMatch) else Hint(pl.tapHint)
            }
            items(pageItems) { e ->
                AppCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.clickable { selected = e }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            (if (e.name.lowercase() in warned) "⚠ " else "") + e.name + (if (e.isNew) "  [" + pl.newTag + "]" else ""),
                            color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                        )
                        Text(if (e.enabled) pl.stOn else pl.stOff, color = if (e.enabled) OK_GREEN else BAD_RED, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
        }
    }
}

    val sel = selected
    if (sel != null) PluginInfoDialog(
        entry = sel,
        order = st.entries.indexOf(sel) + 1,
        total = st.entries.size,
        issues = st.issues.filter { it.plugin.equals(sel.name, true) },
        onClose = { selected = null },
        onRemoved = { selected = null },
        onParams = { selected = null; paramEntry = sel },
    )
    if (showLeave) ConfirmDialog(
        pl.unsaved, pl.leaveMsg, pl.leaveYes, pl.leaveNo,
        onConfirm = { showLeave = false; PluginListJobs.clear(); onBack() },
        onDismiss = { showLeave = false },
    )
    if (showSaveProfile) ProfileNameDialog(
        onSave = { name ->
            val m = loadProfiles(vm)
            val snap = PluginListJobs.snapshot()
            m[name] = snap
            saveProfiles(vm, m)
            profiles = m
            profMsg = pl.profSaved(name, snap.size)
            showSaveProfile = false
        },
        onDismiss = { showSaveProfile = false },
    )
    if (showApplyProfile) ProfileListDialog(
        profiles = profiles,
        onApply = { name ->
            profiles[name]?.let { PluginListJobs.applyProfile(name, it) }
            profMsg = null
            showApplyProfile = false
        },
        onDelete = { name ->
            val m = loadProfiles(vm)
            m.remove(name)
            saveProfiles(vm, m)
            profiles = m
        },
        onDismiss = { showApplyProfile = false },
    )
}

@Composable
private fun ProfileNameDialog(onSave: (String) -> Unit, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    var name by remember { mutableStateOf("") }
    AppAlert(
        onDismissRequest = onDismiss,
        title = { DlgTitle(pl.profSave) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(name, { name = it }, pl.profName)
                Text(pl.profNote, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { OneLineText(pl.profOk, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { OneLineText(pl.profCancel, fontSize = 14.sp, color = p.text) }
        },
    )
}

@Composable
private fun ProfileListDialog(profiles: Map<String, Map<String, Boolean>>, onApply: (String) -> Unit, onDelete: (String) -> Unit, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    AppAlert(
        onDismissRequest = onDismiss,
        title = { Text(pl.profApply, fontWeight = FontWeight.Bold, color = p.text) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 380.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (profiles.isEmpty()) Text(pl.profNone, color = p.muted, fontSize = 13.sp)
                profiles.keys.toList().forEach { n ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            n + " (" + (profiles[n]?.size ?: 0) + ")", color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            maxLines = 2, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).clickable { onApply(n) }.padding(vertical = 10.dp),
                        )
                        TextButton(onClick = { onDelete(n) }) { OneLineText(pl.profDelete, fontSize = 13.sp, color = BAD_RED) }
                    }
                }
                Text(pl.profNote, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { OneLineText(pl.dlgClose, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.accent) }
        },
    )
}

@Composable
private fun PluginInfoDialog(entry: PluginEntry, order: Int, total: Int, issues: List<PluginIssue>, onClose: () -> Unit, onRemoved: () -> Unit, onParams: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    var on by remember(entry) { mutableStateOf(entry.enabled) }
    var confirmDel by remember { mutableStateOf(false) }
    val params = remember(entry) { entry.parameters.entries.toList() }
    AppAlert(
        onDismissRequest = onClose,
        title = { Text(entry.name, fontWeight = FontWeight.Bold, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(pl.dlgOrder + ": " + order + " / " + total, color = p.muted, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth().clickable { on = !on; PluginListJobs.setEnabled(entry, on) }, verticalAlignment = Alignment.CenterVertically) {
                    Text(pl.dlgStatus + ": " + (if (on) pl.stOn else pl.stOff), color = p.text, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Switch(
                        checked = on,
                        onCheckedChange = { v -> on = v; PluginListJobs.setEnabled(entry, v) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.45f),
                            uncheckedThumbColor = p.muted, uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
                        ),
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.weight(1f)) { SecondaryButton(pl.moveUp, order > 1) { PluginListJobs.move(entry, -1) } }
                    Box(Modifier.weight(1f)) { SecondaryButton(pl.moveDown, order < total) { PluginListJobs.move(entry, 1) } }
                    Box(Modifier.weight(1f)) { SecondaryButton(pl.remove) { confirmDel = true } }
                }
                if (issues.isNotEmpty()) {
                    Text(pl.dlgIssues, color = WARN_AMBER, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    issues.forEach { i -> Text(levelLabel(pl, i.level) + ": " + i.text, color = levelColor(i.level), fontSize = 12.sp, lineHeight = 16.sp) }
                }
                Text(pl.dlgDesc, color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(entry.description.ifEmpty { pl.dlgNone }, color = p.text, fontSize = 13.sp)
                Text(pl.dlgParams + " (" + params.size + ")", color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (params.isEmpty()) Text(pl.dlgNone, color = p.muted, fontSize = 12.sp)
                else SecondaryButton(pl.viewParams, onClick = onParams)
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { OneLineText(pl.dlgClose, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.accent) }
        },
    )
    if (confirmDel) ConfirmDialog(
        pl.removeTitle, pl.removeMsg(entry.name), pl.remove, pl.profCancel,
        onConfirm = { confirmDel = false; PluginListJobs.remove(entry); onRemoved() },
        onDismiss = { confirmDel = false },
    )
}

// ============================ Plugin Checker ============================

@Composable
fun PluginCheckScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val st by PluginCheckJobs.state.collectAsState()
    val pickJs = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginCheckJobs.pickJs(u) }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) PluginCheckJobs.pickFolder(u) }
    var filter by remember { mutableStateOf(0) }
    var query by remember { mutableStateOf("") }
    var showIgnored by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { PluginCheckJobs.loadIgnored() }
    val visible = remember(st.issues, st.ignored) { st.issues.filter { !com.furuarc.PluginIgnore.matches(it, st.ignored) } }
    var page by remember(filter, visible, query) { mutableStateOf(0) }
    val shown = remember(visible, filter, query) {
        val q = query.trim().lowercase()
        val byLevel = when (filter) { 1 -> visible.filter { it.level == 3 }; 2 -> visible.filter { it.level == 2 }; 3 -> visible.filter { it.level == 1 }; else -> visible }
        if (q.isEmpty()) byLevel else byLevel.filter { it.plugin.lowercase().contains(q) || it.text.lowercase().contains(q) || it.code.lowercase().contains(q) }
    }
    val pages = maxOf(1, (shown.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.chkTitle, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.File, pl.chkJsLabel, st.jsName, AppIcons.File) { pickJs.launch(arrayOf("*/*")) } }
        item { PickerCard(AppIcons.Folder, pl.chkFolderLabel, st.folderName, AppIcons.Folder) { pickFolder.launch(null) } }
        if (!st.ran) item { DetailHint(pl.chkHint) }
        item { PrimaryButton(pl.chkRun, st.jsName != null && !st.busy) { PluginCheckJobs.run() } }
        if (st.busy) item { BusyBar() }
        val err = st.error
        if (err != null) item { ErrorCard(err) }
        if (showIgnored) item { IgnoredDialog(pl, st.ignored, { PluginCheckJobs.unignore(it) }, { PluginCheckJobs.unignoreAll(); showIgnored = false }, { showIgnored = false }) }
        if (st.ran) {
            item {
                SectionCard(pl.issuesTitle) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(st.count.toString() + " plugin", color = p.muted, fontSize = 12.sp)
                        Text(pl.issuesCount(visible.count { it.level == 3 }, visible.count { it.level == 2 }, visible.count { it.level == 1 }), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (visible.isEmpty()) Text(pl.noIssues, color = OK_GREEN, fontSize = 13.sp)
                    }
                }
            }
            if (st.ignored.isNotEmpty()) item { SecondaryButton(pl.ignoredBtn(st.ignored.size)) { showIgnored = true } }
            if (visible.isNotEmpty()) {
                item { SegmentedChips(listOf(pl.chkAll, pl.lvlErr, pl.lvlWarn, pl.lvlInfo), filter, 12.sp) { filter = it } }
                item { AppTextField(query, { query = it }, pl.searchIssues) }
                if (shown.isEmpty()) item { Hint(pl.noneMatch) }
                if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
                items(pageItems) { i -> IssueCard(pl, i) { x, any -> PluginCheckJobs.ignore(x, any) } }
                item {
                    ExportBar(st.jsName ?: "plugins") {
                        visible.joinToString("\n") { levelLabel(pl, it.level) + " | " + it.plugin + " | " + it.text + (if (it.maybe) " | " + pl.maybeIntended else "") }
                    }
                }
            }
        }
    }
}
}

// ============================ plugins.js Comparer ============================

@Composable
fun PluginDiffScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val st by PluginDiffJobs.state.collectAsState()
    val pickA = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginDiffJobs.pick(u, true) }
    val pickB = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u -> if (u != null) PluginDiffJobs.pick(u, false) }
    var filter by remember { mutableStateOf(0) }
    var page by remember(filter, st.items) { mutableStateOf(0) }
    val shown = remember(st.items, filter) {
        when (filter) {
            1 -> st.items.filter { it.kind == PluginDiffItem.ADDED }
            2 -> st.items.filter { it.kind == PluginDiffItem.REMOVED }
            3 -> st.items.filter { it.kind == PluginDiffItem.STATUS }
            4 -> st.items.filter { it.kind == PluginDiffItem.PARAMS }
            5 -> st.items.filter { it.kind == PluginDiffItem.ORDER }
            else -> st.items
        }
    }
    val pages = maxOf(1, (shown.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }
    fun kindLabel(k: Int) = when (k) {
        PluginDiffItem.ADDED -> pl.kAdded; PluginDiffItem.REMOVED -> pl.kRemoved; PluginDiffItem.STATUS -> pl.kStatus
        PluginDiffItem.PARAMS -> pl.kParams; else -> pl.kOrder
    }
    fun kindColor(k: Int) = when (k) { PluginDiffItem.ADDED -> OK_GREEN; PluginDiffItem.REMOVED -> BAD_RED; else -> WARN_AMBER }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.cmpTitle, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.File, pl.cmpA, st.nameA, AppIcons.File) { pickA.launch(arrayOf("*/*")) } }
        item { PickerCard(AppIcons.File, pl.cmpB, st.nameB, AppIcons.File) { pickB.launch(arrayOf("*/*")) } }
        if (!st.compared) item { Hint(pl.cmpHint) }
        val err = st.error
        if (err != null) item { ErrorCard(err) }
        if (st.compared) {
            item {
                SectionCard(pl.cmpTitle) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("A: " + st.countA + " · B: " + st.countB, color = p.muted, fontSize = 12.sp)
                        Text(if (st.items.isEmpty()) pl.cmpSame else pl.cmpSummary(st.items.size), color = if (st.items.isEmpty()) OK_GREEN else p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            if (st.items.isNotEmpty()) {
                item { SegmentedChips(listOf(pl.dAll, pl.dAdded, pl.dRemoved), if (filter <= 2) filter else -1) { filter = it } }
                item { SegmentedChips(listOf(pl.dStatus, pl.dParams, pl.dOrder), if (filter >= 3) filter - 3 else -1) { filter = it + 3 } }
                if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
                items(pageItems) { d ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(kindLabel(d.kind) + " · " + d.name, color = kindColor(d.kind), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(d.detail, color = p.text, fontSize = 12.sp, lineHeight = 17.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
                item {
                    ExportBar((st.nameB ?: "plugins")) {
                        st.items.joinToString("\n") { kindLabel(it.kind) + " | " + it.name + " | " + it.detail.replace("\n", " ; ") }
                    }
                }
            }
        }
    }
}
}

/** Warnings page of the Plugin.js Editor: 50 per page. */
@Composable
private fun PluginIssuesPage(issues: List<PluginIssue>, onClose: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val pages = maxOf(1, (issues.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    var page by remember(issues) { mutableStateOf(0) }
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(issues, cur) { issues.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.issuesPageTitle, onClose)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item {
            Text(
                pl.issuesCount(issues.count { it.level == 3 }, issues.count { it.level == 2 }, issues.count { it.level == 1 }),
                color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
            )
        }
        if (issues.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, issues.size)) { page = it } }
        if (issues.isEmpty()) item { Hint(pl.noneMatch) }
        items(pageItems) { i -> IssueCard(pl, i) }
        if (issues.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, issues.size)) { page = it } }
        item { SecondaryButton(pl.infoClose, onClose) }
    }
}
}

/** Parameters page of one plugin: parameter name + value field, 50 rows per page. */
@Composable
private fun PluginParamsPage(entry: PluginEntry, onClose: () -> Unit) {
    val p = LocalPalette.current
    val pl = LocalStr.current.pl
    val params = remember(entry) { entry.parameters.entries.map { (it.key) to ((it.value as? String) ?: (it.value?.toString() ?: "")) } }
    val edits = remember(entry) { mutableStateMapOf<String, String>() }
    var query by remember { mutableStateOf("") }
    val shown = remember(params, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) params else params.filter { it.first.lowercase().contains(q) }
    }
    val pages = maxOf(1, (shown.size + PLUGIN_PAGE - 1) / PLUGIN_PAGE)
    var page by remember(query) { mutableStateOf(0) }
    val cur = page.coerceIn(0, pages - 1)
    val pageItems = remember(shown, cur) { shown.drop(cur * PLUGIN_PAGE).take(PLUGIN_PAGE) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(pl.paramsPageTitle, onClose)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { Text(entry.name, color = p.accent, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        item { AppTextField(query, { query = it }, pl.paramsSearch) }
        if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
        if (shown.isEmpty()) item { Hint(pl.noneMatch) }
        items(pageItems, key = { it.first }) { kv ->
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(kv.first, color = p.accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                    AppTextField(edits[kv.first] ?: kv.second, { v ->
                        edits[kv.first] = v
                        PluginListJobs.setParam(entry, kv.first, v)
                    }, pl.dlgParams, multiline = true)
                }
            }
        }
        if (shown.size > PLUGIN_PAGE) item { PluginPager(cur, pages, pl.pageLabel(cur + 1, pages, shown.size)) { page = it } }
        item { SecondaryButton(pl.paramsDone, onClose) }
    }
}
}
