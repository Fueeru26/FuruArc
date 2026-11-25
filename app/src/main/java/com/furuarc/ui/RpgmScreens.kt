package com.furuarc.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import com.furuarc.i18n.x
import com.furuarc.i18n.ot
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.IconButton
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.furuarc.core.EditField
import com.furuarc.core.EditSection
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MainViewModel
import com.furuarc.UiState
import com.furuarc.core.RpgmSave
import com.furuarc.core.RpgmText
import com.furuarc.core.TrTools
import com.furuarc.core.formatBytes
import com.furuarc.i18n.r
import com.furuarc.i18n.pl
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import android.widget.Toast
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.security.SecureRandom
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** RPGM Tools menu: Engine List, Data Translation, Cheat Editor. */
@Composable
fun RpgmToolsScreen(onAssets: () -> Unit, onTranslate: () -> Unit, onCheat: () -> Unit, onBack: () -> Unit, onPlugins: () -> Unit = {}) {
    val s = LocalStr.current
    Column(
        Modifier.fillMaxSize().padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        ScreenHeader(s.menuRpgmTitle, onBack)
        MenuCard(AppIcons.Image, s.rpgmAssetsTitle, s.rpgmAssetsSub, onAssets)
        MenuCard(AppIcons.Translate, s.rpgmTranslateTitle, s.rpgmTranslateSub, onTranslate)
        MenuCard(AppIcons.Edit, s.rpgmCheatTitle, s.rpgmCheatSub, onCheat)
        val plg = s.pl
        MenuCard(AppIcons.Build, plg.mgrTitle, plg.mgrSub, onPlugins)
    }
}

/** RPGM engine list (Daftar Engine): 3 columns, icon = first 2 letters of the RPGM name. */
@Composable
fun RpgmAssetsScreen(onEngine: (VnEngine) -> Unit, onBack: () -> Unit) {
    val s = LocalStr.current
    EngineGrid(s.rpgmAssetsTitle, RPGM_ENGINES.map { e -> EngineTile(e.badge, e.name, e.id) { onEngine(e) } }, onBack)
}

/** Menu with two rows (Export / Import). */
@Composable
fun TwoMenuScreen(
    title: String,
    aIcon: ImageVector, aTitle: String, aSub: String, onA: () -> Unit,
    bIcon: ImageVector, bTitle: String, bSub: String, onB: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        ScreenHeader(title, onBack)
        MenuCard(aIcon, aTitle, aSub, onA)
        MenuCard(bIcon, bTitle, bSub, onB)
    }
}

@Composable
internal fun ErrorCard(msg: String) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(msg, color = Color(0xFFE57373), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(18.dp))
    }
}

@Composable
private fun WarnCard(msg: String) {
    AppCard(Modifier.fillMaxWidth()) {
        Text(msg, color = Color(0xFFFFB74D), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(18.dp))
    }
}

/** Page navigation (50 columns per page). */
@Composable
private fun PagerBar(page: Int, pages: Int, onPage: (Int) -> Unit) {
    PageNav(page, pages, onPage)
}

private const val PER_PAGE = 10 // Value Editor
private const val TR_PER_PAGE = 50 // Translation Editor
private val AUTO_MINUTES = listOf(1, 2, 5) // draft autosave interval (minutes)

/** "Back up original save" checkbox (unchecked by default). */
@Composable
private fun BackupCheck(checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    val s = LocalStr.current
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled) { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { v -> onChange(v) },
            enabled = enabled,
            colors = CheckboxDefaults.colors(checkedColor = p.accent, uncheckedColor = p.muted, checkmarkColor = p.onAccent),
        )
        Text(s.r.backupLabel, color = p.text, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

/** Preview of one asset image (decoded small to save memory). */
@Composable
private fun PreviewCard(name: String?, bytes: ByteArray) {
    val p = LocalPalette.current
    val s = LocalStr.current
    val bmp = remember(bytes) {
        try {
            val o = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, o)
            var sample = 1
            while (o.outWidth / sample > 1024 || o.outHeight / sample > 1024) sample *= 2
            val o2 = BitmapFactory.Options().apply { inSampleSize = sample }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, o2)?.asImageBitmap()
        } catch (e: Throwable) {
            null
        }
    }
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bmp != null) {
                Image(bitmap = bmp, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp))
            } else {
                Text(s.r.previewFail, color = Color(0xFFE57373))
            }
            if (name != null) Text(name, color = p.muted, fontSize = 12.sp)
        }
    }
}

/** Job screen scaffold: header, cancel dialog while a process is running, and the progress/summary/log sections. */
@Composable
internal fun JobScreen(
    vm: MainViewModel,
    title: String,
    subtitle: String?,
    onBack: () -> Unit,
    tail: (LazyListScope.(UiState) -> Unit)? = null,
    listState: LazyListState? = null,
    summaryTitle: String? = null,
    okLabel: String? = null,
    onTranslate: (() -> Unit)? = null,
    body: LazyListScope.(UiState, () -> Unit) -> Unit,
) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val st by vm.state.collectAsState()
    // 0 = no dialog, 1 = from the Cancel button, 2 = from the back button
    var confirm by remember { mutableStateOf(0) }
    val requestBack: () -> Unit = { if (st.running) confirm = 2 else onBack() }
    LaunchedEffect(st.running) { if (!st.running) confirm = 0 }
    BackHandler(enabled = st.running) { confirm = 2 }

    val ownState = rememberLazyListState()
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(title, requestBack)
LazyColumn(
        Modifier.weight(1f).fillMaxWidth(),
        state = listState ?: ownState,
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) { 
        if (subtitle != null) item { Text(subtitle, color = p.accent, fontWeight = FontWeight.SemiBold) }
        this.body(st) { confirm = 1 }
        processItems(
            st,
            onOpenFolder = { openFolder(ctx, vm.resultFolder(), s.openWith, s.cannotOpenFolder) },
            onCopyLog = { copyToClipboard(ctx, st.log.joinToString("\n"), s.logCopied) },
            onVerify = null,
            summaryTitle = summaryTitle ?: s.rpgmSummaryTitle(st.mode),
            okLabel = okLabel ?: s.rpgmOkLabel(st.mode),
            onTranslate = onTranslate,
        )
        if (tail != null) this.tail(st)
    }
}

    if (confirm != 0 && st.running) {
        CancelDialog(
            onConfirm = {
                val fromBack = confirm == 2
                confirm = 0
                vm.cancel()
                if (fromBack) onBack()
            },
            onDismiss = { confirm = 0 },
        )
    }
}

/** Main button, or the Cancel button while a process is running. */
internal fun LazyListScope.runButton(st: UiState, text: String, enabled: Boolean, onCancel: () -> Unit, onRun: () -> Unit) {
    item {
        val str = LocalStr.current
        if (st.running) {
            SecondaryButton(str.btnCancel, onCancel)
        } else {
            PrimaryButton(text = text, enabled = enabled, onClick = onRun)
        }
    }
}

// ---------- Decrypt / Encrypt MV & MZ assets ----------

@Composable
fun AssetJobScreen(vm: MainViewModel, engine: VnEngine, encrypt: Boolean, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    var keyText by remember { mutableStateOf("") }
    var recIdx by remember { mutableStateOf(0) } // 0 = this folder only, 1 = include subfolders
    val recLabels = com.furuarc.i18n.fileToolsStr(com.furuarc.ui.theme.LocalLang.current).renRecursive
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.rpgmInputFolder(uri, recIdx == 1)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }
    val pickSys = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.rpgmSystemJson(uri)
    }
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) vm.rpgmInputFiles(uris)
    }
    var srcMode by remember { mutableStateOf(0) } // 0 = File, 1 = Folder
    // an uploaded System.json fills the key field automatically
    val cur by vm.state.collectAsState()
    LaunchedEffect(cur.rpgm.sysKeyHex) {
        val k = cur.rpgm.sysKeyHex
        if (k != null) keyText = k
    }
    JobScreen(
        vm, if (encrypt) s.assetEncryptTitle else s.assetDecryptTitle, engine.name, onBack,
    ) { st, onCancel ->
        val r = st.rpgm
        item {
            SegmentedChips(listOf(s.assetSrcFile, s.assetSrcFolder), srcMode) { i -> if (!st.running) srcMode = i }
        }
        item {
            if (srcMode == 0) {
                PickerCard(
                    icon = AppIcons.File,
                    label = s.assetFile,
                    value = r.inName,
                    trailingIcon = AppIcons.FolderPlus,
                    onClick = { if (!st.running) pickFiles.launch(arrayOf("*/*")) },
                )
            } else {
                PickerCard(
                    icon = AppIcons.Folder,
                    label = s.assetFolder,
                    value = r.inName,
                    trailingIcon = AppIcons.FolderPlus,
                    onClick = { if (!st.running) pickFolder.launch(null) },
                )
            }
        }
        if (srcMode == 1) {
            item {
                SegmentedChips(recLabels, recIdx, 14.sp) { i ->
                    if (!st.running && i != recIdx) {
                        recIdx = i
                        vm.rpgmRescanFolder(i == 1)
                    }
                }
            }
        }
        item {
            PickerCard(
                icon = AppIcons.File,
                label = s.sysFileLabel,
                value = r.sysName,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!st.running) pickSys.launch(arrayOf("*/*")) },
            )
        }
        item { Text(s.r.sysHint, color = p.muted, fontSize = 13.sp) }
        val sysErr = r.sysError
        if (sysErr != null) item { ErrorCard(sysErr) }
        if (r.inName != null) {
            item {
                Text(
                    if (r.scanning) s.packScanning else s.rpgmFound(r.found, formatBytes(r.foundBytes)),
                    color = p.muted, fontSize = 13.sp,
                )
            }
            if (!r.scanning && r.found > 0) {
                item { Text(s.r.breakdown(r.imgCount, r.audCount, r.otherCount), color = p.muted, fontSize = 13.sp) }
                item { Text(s.keyInfo(r.keySource, r.keyHex), color = p.muted, fontSize = 13.sp) }
            }
        }
        item {
            AppTextField(
                value = keyText,
                onValueChange = { keyText = it.filter { ch -> ch.isLetterOrDigit() }.take(32).lowercase() },
                label = s.keyManualLabel,
                mono = true,
            )
        }
        if (encrypt) {
            item {
                SecondaryButton(s.btnRandomKey) {
                    val b = ByteArray(16)
                    SecureRandom().nextBytes(b)
                    keyText = b.joinToString("") { x -> String.format("%02x", x.toInt() and 0xFF) }
                }
            }
            item { DetailLink(s.assetEncryptNote, 13.sp, 18.sp) }
        }
        if (!r.scanning && r.imgCount > 0) {
            item {
                SecondaryButton(if (r.previewBytes != null) s.r.previewNext else s.r.previewBtn) {
                    if (!st.running) vm.previewAsset(keyText, r.previewBytes != null)
                }
            }
            val pe = r.previewError
            if (pe != null) item { Text(pe, color = p.muted, fontSize = 13.sp) }
            val pb = r.previewBytes
            if (pb != null) item(key = "preview") { PreviewCard(r.previewName, pb) }
        }
        val err = r.error
        if (err != null) item { ErrorCard(err) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        runButton(st, if (encrypt) s.btnEncrypt else s.btnDecrypt, vm.canRunAssets(), onCancel) {
            vm.runAssets(encrypt, keyText)
        }
    }
}

// ---------- Data Translation ----------

@Composable
fun TranslateScreen(vm: MainViewModel, import: Boolean, onBack: () -> Unit, onTranslateText: (android.net.Uri) -> Unit = {}) {
    val s = LocalStr.current
    val p = LocalPalette.current
    var fmt by remember { mutableStateOf(when (vm.pref("tr_fmt", "txt")) { "csv" -> 0; "json" -> 2; else -> 1 }) }
    var outBase by remember { mutableStateOf("translate") }
    LaunchedEffect(Unit) { vm.setExportBase(outBase) }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.rpgmInputFolder(uri)
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.rpgmInputFile(uri)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }
    val trSt by vm.state.collectAsState()
    JobScreen(
        vm, if (import) s.trImportTitle else s.trExportTitle, null, onBack,
        onTranslate = if (!import && trSt.done) vm.exportedFile()?.let { u -> { onTranslateText(u) } } else null,
    ) { st, onCancel ->
        val r = st.rpgm
        item {
            PickerCard(
                icon = AppIcons.Folder,
                label = s.gameFolder,
                value = r.inName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickFolder.launch(null) },
            )
        }
        if (r.inName != null) {
            item {
                Text(
                    if (r.scanning) s.packScanning else s.rpgmFound(r.found, formatBytes(r.foundBytes)),
                    color = p.muted, fontSize = 13.sp,
                )
            }
        }
        if (import) {
            item {
                PickerCard(
                    icon = AppIcons.File,
                    label = s.trFile,
                    value = r.fileName,
                    trailingIcon = AppIcons.Folder,
                    onClick = { if (!st.running) pickFile.launch(arrayOf("*/*")) },
                )
            }
        } else {
            item {
                SectionCard(s.trFormat) {
                    SegmentedChips(listOf("CSV", "TXT", "JSON"), fmt) { i ->
                        if (!st.running) {
                            fmt = i
                            vm.setPref("tr_fmt", when (i) { 1 -> "txt"; 2 -> "json"; else -> "csv" })
                        }
                    }
                }
            }
            item {
                FileNameField(outBase, "." + com.furuarc.core.RpgmText.extFor(fmt), s.x.outFileName) {
                    outBase = it; vm.setExportBase(it)
                }
            }
        }
        if (import) item { Text(s.trImportNote, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp) }
        item { DetailLink(s.r.trNoteCodes, 13.sp, 18.sp) }
        val err = r.error
        if (err != null) item { ErrorCard(err) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        runButton(st, if (import) s.btnTrImport else s.btnTrExport, vm.canRunText(import) && (import || outBase.isNotBlank()), onCancel) {
            if (import) vm.translateImport() else vm.translateExport(fmt)
        }
        if (import && !st.running && r.report.isNotEmpty()) item(key = "report") { InfoCard(r.report) }
    }
}

// ---------- Cheat Editor (export / import save) ----------

@Composable
fun SaveScreen(vm: MainViewModel, import: Boolean, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    // save format created on import: 0 = MV, 1 = MZ (automatic from the JSON file name if recognized)
    var target by remember { mutableStateOf(if (vm.pref("save_target", "mv") == "mz") 1 else 0) }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.rpgmInputFile(uri)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }
    var backup by remember { mutableStateOf(false) }
    val detected by vm.state.collectAsState()
    LaunchedEffect(detected.rpgm.saveKind) {
        if (import && detected.rpgm.saveKind > 0) target = detected.rpgm.saveKind - 1
    }
    JobScreen(vm, if (import) s.saveImportTitle else s.saveExportTitle, null, onBack) { st, onCancel ->
        val r = st.rpgm
        item {
            PickerCard(
                icon = AppIcons.File,
                label = if (import) s.jsonFile else s.saveFile,
                value = r.fileName,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!st.running) pickFile.launch(arrayOf("*/*")) },
            )
        }
        if (!import && r.saveKind > 0 && r.error == null) {
            item { InfoCard(listOf(Pair(s.infoFormat, s.saveKindName(r.saveKind)))) }
        }
        if (import) {
            item {
                SectionCard(s.saveTargetLabel) {
                    SegmentedChips(listOf("RPGM MV", "RPGM MZ"), target) { i ->
                        if (!st.running) {
                            target = i
                            vm.setPref("save_target", if (i == 1) "mz" else "mv")
                        }
                    }
                }
            }
            item { Text(s.saveBackupNote, color = p.muted, fontSize = 13.sp) }
        } else {
            item { Text(s.saveExportNote, color = p.muted, fontSize = 13.sp) }
        }
        val err = r.error
        if (err != null) item { ErrorCard(err) }
        val warn = r.warn
        if (import && warn != null && err == null) item { WarnCard(warn) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        if (import) item { BackupCheck(backup, !st.running) { v -> backup = v } }
        runButton(st, if (import) s.btnSaveImport else s.btnSaveExport, vm.canRunSave(), onCancel) {
            if (import) vm.saveImport(if (target == 1) RpgmSave.MZ else RpgmSave.MV, backup) else vm.saveExport()
        }
    }
}

/** Menu list with any number of items (used by Cheat Editor: Export, Import, Value Editor). */
class MenuSpec(val icon: ImageVector, val title: String, val sub: String, val onClick: () -> Unit)

@Composable
fun MenuListScreen(title: String, items: List<MenuSpec>, onBack: () -> Unit, scroll: androidx.compose.foundation.ScrollState = rememberScrollState()) {
    // verticalScroll: long menu lists (e.g. File Check, 6 cards) are no longer cut off on small screens
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(title, onBack)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(scroll).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        items.forEach { m -> MenuCard(m.icon, m.title, m.sub, m.onClick) }
    }
    }
}

// ---------- Value Editor ----------

@Composable
private fun GroupPicker(titles: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    var open by remember { mutableStateOf(false) }
    val anchorW = remember { mutableStateOf(0.dp) }
    Box(Modifier.fillMaxWidth().trackWidth(anchorW)) {
        AppCard(Modifier.fillMaxWidth()) {
            Text(
                titles.getOrElse(selected) { "" },
                color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth().clickable { open = true }.padding(16.dp),
            )
        }
        FieldDropdownMenu(expanded = open, anchorWidth = anchorW.value, onDismiss = { open = false }) {
            titles.forEachIndexed { i, t ->
                DropdownOption(t, i == selected) { open = false; onSelect(i) }
            }
        }
    }
}

@Composable
private fun ValueRow(f: EditField, edits: SnapshotStateMap<String, String>) {
    val p = LocalPalette.current
    val cur = edits[f.key] ?: f.value
    if (f.type == 1) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(f.label, color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = cur == "true",
                onCheckedChange = { v -> edits[f.key] = v.toString() },
                colors = SwitchDefaults.colors(checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.5f)),
            )
        }
    } else {
        AppTextField(value = cur, onValueChange = { v -> edits[f.key] = v }, label = f.label, number = f.type == 0)
    }
}

/** Value Editor view state shared by all sections. */
private class ValueView(
    val edits: SnapshotStateMap<String, String>,
    val query: String,
    val filter: Int, // 0 all, 1 filled (not 0/false), 2 changed
    val group: Int,
    val bulk: SnapshotStateMap<String, String>,
    val stat: Int,
    val pages: SnapshotStateMap<String, Int>,
)

private fun isListSection(id: String) = id != "general" && id != "chars"

/** Rows shown for [sec] according to search and filter. */
private fun visibleFields(sec: EditSection, v: ValueView): List<EditField> {
    val g = if (sec.id == "chars") sec.groups.getOrNull(v.group) else sec.groups.firstOrNull()
    val q = v.query.trim().lowercase()
    return (g?.fields ?: emptyList()).filter { f ->
        // search and "Filled" use the initial value, so rows do not disappear while typing;
        // the edited value is only read for the "Changed" filter
        val textOk = q.isEmpty() || sec.id == "general" ||
            f.label.lowercase().contains(q) || f.value.lowercase().contains(q)
        val filterOk = when (v.filter) {
            1 -> !isListSection(sec.id) || (f.value != "0" && f.value != "false" && f.value.isNotEmpty())
            2 -> (v.edits[f.key] ?: f.value) != f.value
            else -> true
        }
        textOk && filterOk
    }
}

private val BULK_NUM = Regex("-?\\d+")
private val STAT_KEYS = listOf("/_level", "/_hp", "/_mp", "/_tp")
private val STAT_NAMES = listOf("Level", "HP", "MP", "TP")

/** Bulk action for one section: fills the edit field of the visible rows; the result is written after pressing Apply. */
@Composable
private fun BulkBar(sec: EditSection, shown: List<EditField>, v: ValueView, onStat: (Int) -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val text = v.bulk[sec.id] ?: "99"
    val valid = BULK_NUM.matches(text.trim())
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(s.r.bulkTitle, color = p.text, fontWeight = FontWeight.SemiBold)
            if (sec.id == "switches") {
                val sw = shown.filter { it.type == 1 }
                SecondaryButton(s.r.bulkAllOn) { for (f in sw) v.edits[f.key] = "true" }
                SecondaryButton(s.r.bulkAllOff) { for (f in sw) v.edits[f.key] = "false" }
            } else {
                AppTextField(value = text, onValueChange = { t -> v.bulk[sec.id] = t.filter { c -> c.isDigit() || c == '-' } }, label = s.r.bulkValue, number = true)
                if (sec.id == "chars") {
                    SegmentedChips(STAT_NAMES, v.stat, onStat)
                    SecondaryButton(s.r.bulkChars) {
                        if (valid) for (g in sec.groups) for (f in g.fields) {
                            if (f.key.endsWith(STAT_KEYS[v.stat])) v.edits[f.key] = text.trim()
                        }
                    }
                } else {
                    val nums = shown.filter { it.type == 0 }
                    SecondaryButton(s.r.bulkSetShown(nums.size)) {
                        if (valid) for (f in nums) v.edits[f.key] = text.trim()
                    }
                }
            }
            DetailLink(s.r.bulkNote, 12.sp, 17.sp)
        }
    }
}

private fun LazyListScope.valueSectionItems(
    sec: EditSection,
    title: String,
    v: ValueView,
    onGroup: (Int) -> Unit,
    onStat: (Int) -> Unit,
    scrollTo: (String) -> Unit,
    accent: Color,
    muted: Color,
    addedNote: String,
): IntArray {
    // counted manually: [0] = number of items added, [1] = number of items after the top pager (for jumping to the top of the page)
    var added = 0
    item(key = "h-" + sec.id) { Text(title, color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
    added++
    if (sec.id == "chars") {
        val titles = sec.groups.map { it.title }
        item(key = "pick-chars") { GroupPicker(titles, v.group, onGroup) }
        added++
    }
    val fields = visibleFields(sec, v)
    if (sec.id != "general") {
        item(key = "bulk-" + sec.id) { BulkBar(sec, fields, v, onStat) }
        added++
    }
    if (sec.id == "items" || sec.id == "weapons" || sec.id == "armors") {
        if (sec.groups.firstOrNull()?.fields?.any { it.value == "0" } == true) {
            item(key = "note-" + sec.id) { Text(addedNote, color = muted, fontSize = 12.sp) }
            added++
        }
    }
    val pageKey = if (sec.id == "chars") "chars:" + v.group else sec.id
    val pages = maxOf(1, (fields.size + PER_PAGE - 1) / PER_PAGE)
    val page = (v.pages[pageKey] ?: 0).coerceIn(0, pages - 1)
    val slice = fields.subList(page * PER_PAGE, minOf(fields.size, (page + 1) * PER_PAGE))
    if (pages > 1) {
        item(key = "pg-top-" + sec.id) { PagerBar(page, pages) { np -> v.pages[pageKey] = np } }
        added++
    }
    items(slice, key = { f -> f.key }) { f -> ValueRow(f, v.edits) }
    added += slice.size
    var afterTop = 0
    if (pages > 1) {
        item(key = "pg-bot-" + sec.id) {
            PagerBar(page, pages) { np ->
                v.pages[pageKey] = np
                scrollTo(sec.id)
            }
        }
        added++
        afterTop = slice.size + 1
    }
    return intArrayOf(added, afterTop)
}

@Composable
fun ValueScreen(vm: MainViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val edits = remember { mutableStateMapOf<String, String>() }
    val bulk = remember { mutableStateMapOf<String, String>() }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(0) }
    var actor by remember { mutableStateOf(0) }
    var stat by remember { mutableStateOf(0) }
    var backup by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    var baseline by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var pending by remember { mutableStateOf<Map<String, String>?>(null) }
    val appSettings by vm.settings.collectAsState()
    val cur by vm.state.collectAsState()
    LaunchedEffect(cur.done) {
        if (cur.done) pending?.let { baseline = it; pending = null }
    }
    val requestBack: () -> Unit = {
        if (appSettings.confirmExit && edits.toMap() != baseline) showLeave = true else onBack()
    }
    BackHandler(enabled = appSettings.confirmExit) { requestBack() }
    val pages = remember { mutableStateMapOf<String, Int>() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // sec.id -> number of items from after that section's top pager to the end of the list (filled when the list is built)
    val jump = remember { HashMap<String, Int>() }
    val scrollUp: (String) -> Unit = { id ->
        scope.launch {
            val after = jump[id]
            if (after != null) listState.scrollToItem((listState.layoutInfo.totalItemsCount - after - 1).coerceAtLeast(0))
        }
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            edits.clear(); bulk.clear(); pages.clear(); actor = 0; query = ""; filter = 0
            baseline = emptyMap(); pending = null
            vm.rpgmInputFile(uri)
        }
    }
    val pickNames = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.rpgmNamesFolder(uri)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }
    val view = ValueView(edits, query, filter, actor, bulk, stat, pages)
    val accent = p.accent
    val muted = p.muted
    JobScreen(
        vm, s.menuValueTitle, null, requestBack,
        tail = { st ->
            val secs = st.rpgm.edit
            if (secs.isNotEmpty()) {
                item(key = "val-title") { ColumnTitle(s.r.valueColTitle) }
                item(key = "search") {
                    AppTextField(value = query, onValueChange = { v -> query = v; pages.clear() }, label = s.r.searchNameId)
                }
                item(key = "filter") {
                    SegmentedChips(listOf(s.r.filterAll, s.r.filterFilled, s.r.filterChanged), filter) { i -> filter = i; pages.clear() }
                }
                val res = secs.map { sec ->
                    valueSectionItems(sec, s.valueSection(sec.id), view, { i -> actor = i }, { i -> stat = i }, scrollUp, accent, muted, s.r.addedItemsNote)
                }
                for ((k, sec) in secs.withIndex()) {
                    var later = 0
                    for (j in k + 1 until secs.size) later += res[j][0]
                    jump[sec.id] = res[k][1] + later
                }
            }
        },
        listState = listState,
    ) { st, onCancel ->
        val r = st.rpgm
        item {
            PickerCard(
                icon = AppIcons.File,
                label = s.valueFile,
                value = r.fileName,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!st.running) pickFile.launch(arrayOf("*/*")) },
            )
        }
        item {
            PickerCard(
                icon = AppIcons.Folder,
                label = s.r.namesFolderLabel,
                value = r.namesName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickNames.launch(null) },
            )
        }
        val info = r.namesInfo
        item {
            if (info != null) Text(info, color = if (r.namesOk) p.muted else Color(0xFFE57373), fontSize = 13.sp)
            else Text(s.r.namesHint, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
        }
        val err = r.error
        if (err != null) item { ErrorCard(err) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        item { BackupCheck(backup, !st.running) { v -> backup = v } }
        if (r.fileName == null) item { DetailLink(s.valueHint, 13.sp, 18.sp) }
        runButton(st, s.btnApply, vm.canRunEdit(), onCancel) {
            pending = edits.toMap()
            vm.applyEdits(edits.toMap(), backup)
        }
    }
    if (showLeave) {
        ConfirmDialog(
            title = s.unsavedTitle,
            message = s.r.exitMsg,
            yes = s.unsavedYes,
            no = s.unsavedNo,
            onConfirm = { showLeave = false; onBack() },
            onDismiss = { showLeave = false },
        )
    }
}

// ---------- Translation Editor ----------

/** Editor section title: theme-colored text in the middle with theme-colored lines on its left and right. */
@Composable
private fun ColumnTitle(text: String) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(2.dp).background(p.accent))
        Text(
            text, color = p.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 14.dp),
        )
        Box(Modifier.weight(1f).height(2.dp).background(p.accent))
    }
}

/** Window scaffold: [header] stays at the top and [actions] + Close button stay at the bottom; only [content] scrolls. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
internal fun DialogFrame(
    onDismiss: () -> Unit,
    header: @Composable ColumnScope.() -> Unit = {},
    actions: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    val s = LocalStr.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(Modifier.padding(16.dp)) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp)) {
                    Column(Modifier.padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { header() }
                    Column(
                        Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) { content() }
                    Spacer(Modifier.height(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        actions()
                        SecondaryButton(s.btnClose, onDismiss)
                    }
                }
            }
        }
    }
}

/** A single checkbox row. */
@Composable
internal fun CheckLine(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked, onCheckedChange = { v -> onChange(v) },
            colors = CheckboxDefaults.colors(checkedColor = p.accent, uncheckedColor = p.muted, checkmarkColor = p.onAccent),
        )
        Text(label, color = p.text, fontSize = 14.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

/** Editor Settings page: each Apply button closes the page and immediately applies its section's settings. */
@Composable
private fun EditorSettingsPage(
    texts: List<String>,
    onDismiss: () -> Unit,
    onFirst: (Boolean) -> Unit,
    onWrap: (Int, Boolean) -> Unit,
    onReplace: (String, String, Boolean, Boolean) -> Unit,
    onFill: () -> Unit,
    onCodes: (Boolean) -> Unit,
    onGlossImport: (Boolean) -> Unit,
    onGlossExport: () -> Unit,
    autoOn: Boolean,
    autoMin: Int,
    onAutoOn: (Boolean) -> Unit,
    onAutoMin: (Int) -> Unit,
) {
    val s = LocalStr.current
    val p = LocalPalette.current
    var letter by remember { mutableStateOf(0) } // 0 = uppercase, 1 = lowercase
    var wrapText by remember { mutableStateOf("40") }
    var keep by remember { mutableStateOf(true) }
    var find by remember { mutableStateOf("") }
    var rep by remember { mutableStateOf("") }
    var ignoreCase by remember { mutableStateOf(true) }
    var whole by remember { mutableStateOf(false) }
    var codesEmpty by remember { mutableStateOf(false) }
    var csvMode by remember { mutableStateOf(0) } // 0 = merge, 1 = replace
    val wrapN = wrapText.toIntOrNull() ?: 0
    // preview the number of matches before applying
    val matches = remember(find, ignoreCase, whole, texts) {
        var words = 0
        var nTexts = 0
        if (find.isNotEmpty()) {
            for (t in texts) {
                val c = TrTools.count(t, find, ignoreCase, whole)
                if (c > 0) {
                    words += c
                    nTexts++
                }
            }
        }
        Pair(words, nTexts)
    }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(s.r.edSettingsBtn, onDismiss)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // first letter
        Text(s.r.setFirstLetter, color = p.accent, fontWeight = FontWeight.SemiBold)
        SegmentedChips(listOf(s.r.setUpper, s.r.setLower), letter) { i -> letter = i }
        PrimaryButton(s.btnApply, true) { onFirst(letter == 0) }
        // auto wrap
        Text(s.r.setWrap, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        AppTextField(
            value = wrapText, onValueChange = { v -> wrapText = v.filter { c -> c.isDigit() }.take(4) },
            label = s.r.setWrapMax, number = true,
        )
        CheckLine(s.r.setKeepWords, keep) { v -> keep = v }
        PrimaryButton(s.btnApply, wrapN > 0) { onWrap(wrapN, keep) }
        // replace
        Text(s.r.setReplace, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        AppTextField(value = find, onValueChange = { v -> find = v }, label = s.r.setFind)
        AppTextField(value = rep, onValueChange = { v -> rep = v }, label = s.r.setReplaceWith)
        CheckLine(s.r.setIgnoreCase, ignoreCase) { v -> ignoreCase = v }
        CheckLine(s.r.setWholeWord, whole) { v -> whole = v }
        if (find.isNotEmpty()) Text(s.r.edMatches(matches.first, matches.second), color = p.muted, fontSize = 13.sp)
        PrimaryButton(s.btnApply, find.isNotEmpty()) { onReplace(find, rep, ignoreCase, whole) }
        // auto-fill identical entries
        Text(s.r.fillTitle, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text(s.r.fillHint, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
        PrimaryButton(s.btnApply, true) { onFill() }
        // auto-fill control codes
        Text(s.r.codesTitle, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text(s.r.codesHint, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
        CheckLine(s.r.codesEmpty, codesEmpty) { v -> codesEmpty = v }
        PrimaryButton(s.btnApply, true) { onCodes(codesEmpty) }
        // import / export glossary (CSV)
        Text(s.r.gcsvTitle, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text(s.r.gcsvHint, color = p.muted, fontSize = 13.sp)
        SegmentedChips(listOf(s.r.gcsvMerge, s.r.gcsvReplace), csvMode) { i -> csvMode = i }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton(s.r.gcsvImport, true) { onGlossImport(csvMode == 1) } }
            Box(Modifier.weight(1f)) { SecondaryButton(s.r.gcsvExport, true) { onGlossExport() } }
        }
        // draft autosave (takes effect immediately, no Apply button)
        Text(s.r.autoTitle, color = p.accent, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        Text(s.r.autoHint, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
        SegmentedChips(listOf(s.r.autoOff, s.r.autoOn), if (autoOn) 1 else 0) { i -> onAutoOn(i == 1) }
        if (autoOn) {
            SegmentedChips(AUTO_MINUTES.map { m -> s.r.autoMin(m) }, AUTO_MINUTES.indexOf(autoMin).coerceAtLeast(0)) { i ->
                onAutoMin(AUTO_MINUTES[i])
            }
        }
    }
    }
}

/** Glossary window: list of terms (original -> translation), add/delete, and the Apply glossary button. */
@Composable
private fun GlossaryDialog(
    glossary: List<Pair<String, String>>,
    onGlossary: (List<Pair<String, String>>) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
) {
    val s = LocalStr.current
    val p = LocalPalette.current
    var gSrc by remember { mutableStateOf("") }
    var gDst by remember { mutableStateOf("") }
    DialogFrame(
        onDismiss,
        header = {
            Text(
                s.r.glossTitle, color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            DetailLink(s.r.glossHint, 13.sp, 18.sp)
            AppTextField(value = gSrc, onValueChange = { v -> gSrc = v }, label = s.r.glossSource)
            AppTextField(value = gDst, onValueChange = { v -> gDst = v }, label = s.r.edTranslation)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    SecondaryButton(s.r.glossAdd, gSrc.isNotBlank() && gDst.isNotBlank()) {
                        onGlossary(glossary + Pair(gSrc.trim(), gDst.trim()))
                        gSrc = ""
                        gDst = ""
                    }
                }
                Box(Modifier.weight(1f)) { PrimaryButton(s.r.glossApply, glossary.isNotEmpty()) { onApply() } }
            }
        },
    ) {
        // only the term list scrolls
        if (glossary.isEmpty()) Text(s.r.glossEmpty, color = p.muted, fontSize = 13.sp)
        glossary.forEachIndexed { gi, g ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.first + "  →  " + g.second, color = p.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = { onGlossary(glossary.filterIndexed { k, _ -> k != gi }) }) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = p.muted)
                }
            }
        }
    }
}

/** One edit history row. [kind]: 1 = newly translated, 2 = edited. */
private class HistRow(val idx: Int, val kind: Int, val orig: String, val tr: String)

/** Edit History window: newest entries on top; Clear History and Close are always visible. */
@Composable
private fun HistoryDialog(rows: List<HistRow>, total: Int, onClear: () -> Unit, onOpen: (Int) -> Unit, onDismiss: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    DialogFrame(
        onDismiss,
        header = {
            Text(
                s.r.histTitle, color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
            )
            if (rows.isNotEmpty()) Text(s.r.histNote(rows.size, total), color = p.muted, fontSize = 13.sp)
        },
        actions = { SecondaryButton(s.r.histClear, rows.isNotEmpty(), onClear) },
    ) {
        if (rows.isEmpty()) {
            Text(s.r.histEmpty, color = p.muted, fontSize = 14.sp)
        } else {
            for (r in rows) {
                Column(
                    Modifier.fillMaxWidth().clickable { onOpen(r.idx) }.padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        (if (r.kind == 1) s.r.histNew else s.r.histEdited) + "  #" + (r.idx + 1),
                        color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    )
                    androidx.compose.material.Text(r.orig, color = p.muted, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    androidx.compose.material.Text(r.tr.ifBlank { "—" }, color = p.text, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Row of five icons in the Translation Editor: Undo, History, Glossary, Online Translation, Editor Settings. */
@Composable
private fun EditorIconBar(canUndo: Boolean, onUndo: () -> Unit, onHistory: () -> Unit, onGlossary: () -> Unit, onOnline: () -> Unit, onSettings: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.size(52.dp)) {
            Icon(AppIcons.Undo, contentDescription = s.r.edUndo, tint = if (canUndo) p.accent else p.muted.copy(alpha = 0.4f), modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onHistory, modifier = Modifier.size(52.dp)) {
            Icon(AppIcons.History, contentDescription = s.r.histTitle, tint = p.accent, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onGlossary, modifier = Modifier.size(52.dp)) {
            Icon(AppIcons.MenuBook, contentDescription = s.r.glossTitle, tint = p.accent, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onOnline, modifier = Modifier.size(52.dp)) {
            Icon(AppIcons.Language, contentDescription = s.ot.title, tint = p.accent, modifier = Modifier.size(30.dp))
        }
        IconButton(onClick = onSettings, modifier = Modifier.size(52.dp)) {
            Icon(AppIcons.Gear, contentDescription = s.r.edSettingsBtn, tint = p.accent, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun TrRow(orig: String, text: String, glossary: List<Pair<String, String>>, onText: (String) -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    // computed once per original text: rows are typed into repeatedly, so heavy work must not be repeated on every keystroke
    val origCodes = remember(orig) { RpgmText.controlCodes(orig) }
    val sugAll = remember(orig, glossary) { if (glossary.isEmpty()) emptyList() else TrTools.suggestions(orig, glossary) }
    val codeWarn = text.isNotBlank() && origCodes != RpgmText.controlCodes(text)
    // every translation row (Original Text + translation field) sits on its own label background
    RowPanel(Modifier.fillMaxWidth()) {
    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(s.r.edOriginal, color = p.muted, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { onText(orig) }) {
                Icon(AppIcons.MoveToInbox, contentDescription = s.r.edInsert, tint = p.muted)
            }
            IconButton(onClick = { copyToClipboard(ctx, orig, s.r.edCopied) }) {
                Icon(AppIcons.ContentCopy, contentDescription = s.r.edCopied, tint = p.muted)
            }
        }
        Text(orig, color = p.text, fontSize = 15.sp)
        AppTextField(
            value = text, onValueChange = onText, label = s.r.edTranslation, multiline = true,
            trailing = {
                IconButton(onClick = { copyToClipboard(ctx, text, s.r.edTrCopied) }) {
                    Icon(AppIcons.ContentCopy, contentDescription = s.r.edCopyTr, tint = p.muted)
                }
            },
        )
        if (codeWarn) Text(s.r.edCodeWarn, color = Color(0xFFFFB74D), fontSize = 12.sp)
        // glossary suggestions: terms found in the original text; tap to insert its translation
        val sug = sugAll.filter { g -> !text.contains(g.second, true) }
        if (sug.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(s.r.edSuggest, color = p.muted, fontSize = 12.sp)
                for (g in sug) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(CardRadius))
                            .background(p.accent.copy(alpha = 0.18f))
                            .clickable { onText(if (text.isBlank()) g.second else text.trimEnd() + " " + g.second) }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(g.first + " → " + g.second, color = p.accent, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
    }
}

@Composable
fun TranslateEditScreen(vm: MainViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val edits = remember { mutableStateMapOf<Int, String>() }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(0) }
    var page by remember { mutableStateOf(0) }
    var ver by remember { mutableStateOf(0) } // incremented on every bulk apply / undo, so the displayed list is recomputed
    var showSettings by remember { mutableStateOf(false) }
    var showGlossary by remember { mutableStateOf(false) }
    var showOnline by remember { mutableStateOf(false) }
    // rows from Online Translation that have not been manually changed (for the "Online results" filter)
    val onlineSet = remember { HashSet<Int>() }
    var showHistory by remember { mutableStateOf(false) }
    var histTick by remember { mutableStateOf(0) } // incremented when the history is cleared so the window is redrawn
    // edit history: entry index -> 1 newly translated / 2 edited (order = oldest to newest)
    val hist = remember { LinkedHashMap<Int, Int>() }
    var glossary by remember { mutableStateOf(TrTools.parseGlossary(vm.pref("tr_glossary", ""))) }
    var importReplace by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    var autoOn by remember { mutableStateOf(vm.pref("tr_auto", "0") == "1") }
    var autoMin by remember { mutableStateOf((vm.pref("tr_auto_min", "2").toIntOrNull() ?: 2).let { m -> if (m in AUTO_MINUTES) m else 2 }) }
    var autoTime by remember { mutableStateOf<String?>(null) }
    // field contents at last load / save, used to detect unsaved changes
    var baseline by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var pending by remember { mutableStateOf<Map<Int, String>?>(null) }
    val appSettings by vm.settings.collectAsState()
    // bulk-apply history: index -> previous text (null = no value yet)
    val undo = remember { mutableStateListOf<Map<Int, String?>>() }
    val ctx = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            edits.clear(); undo.clear(); hist.clear(); onlineSet.clear(); query = ""; filter = 0; page = 0
            vm.rpgmInputFile(uri)
        }
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }
    val cur by vm.state.collectAsState()
    val trItems = cur.rpgm.trItems
    // a draft restores its contents; a regular file starts with empty fields
    LaunchedEffect(trItems) {
        edits.clear()
        undo.clear()
        hist.clear()
        onlineSet.clear()
        trItems.forEachIndexed { i, row -> if (row.tr.isNotEmpty()) edits[i] = row.tr }
        baseline = edits.filterValues { it.isNotBlank() }.toMap()
    }
    LaunchedEffect(cur.done) {
        if (cur.done) pending?.let { baseline = it; pending = null }
    }
    // The list of displayed rows is computed when search/filter changes, so rows do not disappear while typing.
    val lowerOrig = remember(trItems) { trItems.map { row -> row.orig.lowercase() } }
    val visible: List<Int> = remember(trItems, query, filter, ver) {
        val q = query.trim().lowercase()
        val out = ArrayList<Int>()
        for ((i, row) in trItems.withIndex()) {
            val t = edits[i] ?: ""
            if (q.isNotEmpty() && !(lowerOrig[i].contains(q) || t.lowercase().contains(q))) continue
            val ok = when (filter) {
                1 -> t.isNotBlank()
                2 -> t.isBlank()
                3 -> i in onlineSet
                else -> true
            }
            if (ok) out.add(i)
        }
        out
    }
    val filledCount = edits.values.count { it.isNotBlank() }
    fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
    val pickGlossIn = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                val text = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: throw IllegalStateException()
                val g = TrTools.glossaryFromCsv(text)
                if (g.isEmpty()) {
                    toast(s.r.gcsvNoTerms)
                } else {
                    glossary = if (importReplace) g else TrTools.mergeGlossary(glossary, g)
                    vm.setPref("tr_glossary", TrTools.writeGlossary(glossary))
                    toast(s.r.gcsvImported(g.size))
                }
            } catch (e: Exception) {
                toast(s.r.gcsvFail)
            }
        }
    }
    val pickGlossOut = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            try {
                val data = TrTools.glossaryToCsv(glossary).toByteArray(Charsets.UTF_8)
                ctx.contentResolver.openOutputStream(uri, "wt")?.use { it.write(data) } ?: throw IllegalStateException()
                toast(s.r.gcsvExported(glossary.size))
            } catch (e: Exception) {
                toast(s.r.gcsvFail)
            }
        }
    }
    // record a single entry's change in the history (compared with the file's initial contents)
    val track: (Int, String) -> Unit = { idx, text ->
        val base = trItems.getOrNull(idx)?.tr ?: ""
        hist.remove(idx)
        if (text != base && !(text.isBlank() && base.isBlank())) hist[idx] = if (base.isBlank()) 1 else 2
    }
    // apply bulk changes (index -> new text) and save their history for Undo
    val commit: (Map<Int, String>) -> Unit = { changes ->
        if (changes.isNotEmpty()) {
            undo.add(changes.keys.associateWith { k -> edits[k] })
            if (undo.size > 10) undo.removeAt(0)
            for ((k, t) in changes) {
                edits[k] = t
                track(k, t)
            }
            ver++
        }
        toast(s.r.edApplied(changes.size))
    }
    // change all filled translations (not only the displayed ones) with [f]
    val applyAll: ((String) -> String) -> Unit = { f ->
        val changes = LinkedHashMap<Int, String>()
        for ((i, t) in edits.entries.toList()) {
            if (t.isBlank()) continue
            val nt = f(t)
            if (nt != t) changes[i] = nt
        }
        commit(changes)
    }
    val doUndo: () -> Unit = {
        if (undo.isNotEmpty()) {
            val snap = undo.removeAt(undo.size - 1)
            for ((k, old) in snap) {
                if (old == null) edits.remove(k) else edits[k] = old
                track(k, old ?: "")
            }
            ver++
            toast(s.r.edUndone(snap.size))
        }
    }
    val pages = maxOf(1, (visible.size + TR_PER_PAGE - 1) / TR_PER_PAGE)
    val pg = page.coerceIn(0, pages - 1)
    val slice = visible.subList(pg * TR_PER_PAGE, minOf(visible.size, (pg + 1) * TR_PER_PAGE))
    // draft autosave: only if there are changes since the last save
    val doAuto: () -> Unit = {
        val snap = edits.filterValues { it.isNotBlank() }.toMap()
        if (trItems.isNotEmpty() && snap != baseline) {
            vm.autoSaveDraft(edits.toMap()) { ok ->
                if (ok) {
                    baseline = snap
                    autoTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                }
            }
        }
    }
    val latestAuto by rememberUpdatedState(doAuto)
    LaunchedEffect(autoOn, autoMin) {
        if (autoOn) {
            while (true) {
                delay(autoMin * 60_000L)
                latestAuto()
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, autoOn) {
        val obs = LifecycleEventObserver { _, ev -> if (ev == Lifecycle.Event.ON_STOP && autoOn) latestAuto() }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            if (autoOn) latestAuto() // leaving the editor
        }
    }
    val requestBack: () -> Unit = {
        val dirty = edits.filterValues { it.isNotBlank() }.toMap() != baseline
        if (appSettings.confirmExit && dirty) showLeave = true else onBack()
    }
    BackHandler(enabled = appSettings.confirmExit) { requestBack() }
    // tail is the last part of the list: after the top pager come this page's rows + the bottom pager
    val scrollUp: (Int) -> Unit = { n ->
        scope.launch { listState.scrollToItem((listState.layoutInfo.totalItemsCount - (n + 1) - 1).coerceAtLeast(0)) }
    }
    // open entry [target]: the view returns to All without search, moves to its page, then scrolls to its row
    val goTo: (Int) -> Unit = { target ->
        val n = trItems.size
        query = ""; filter = 0; ver++
        val tp = target / TR_PER_PAGE
        page = tp
        val size = minOf(n, (tp + 1) * TR_PER_PAGE) - tp * TR_PER_PAGE
        val allPages = maxOf(1, (n + TR_PER_PAGE - 1) / TR_PER_PAGE)
        val after = (size - (target % TR_PER_PAGE) - 1) + (if (allPages > 1) 1 else 0)
        scope.launch {
            delay(150) // wait for the list to be rebuilt for the new page
            listState.scrollToItem((listState.layoutInfo.totalItemsCount - after - 1).coerceAtLeast(0))
        }
    }
    BackHandler(enabled = showOnline || showSettings) { showOnline = false; showSettings = false }
    if (showOnline) {
        OnlineTranslatePage(
            origs = trItems.map { it.orig },
            trs = List(trItems.size) { i -> edits[i] ?: "" },
            pageIdx = slice,
            editorGlossary = glossary,
            onCommit = { ch, tr -> commit(ch); onlineSet.addAll(tr) },
            onBack = { showOnline = false },
        )
    } else if (showSettings) {
        EditorSettingsPage(
            texts = edits.values.filter { it.isNotBlank() },
            onDismiss = { showSettings = false },
            onFirst = { upper -> showSettings = false; applyAll { t -> TrTools.firstLetter(t, upper) } },
            onWrap = { n, keep -> showSettings = false; applyAll { t -> TrTools.wrap(t, n, keep) } },
            onReplace = { f, r, ic, whole -> showSettings = false; applyAll { t -> TrTools.replace(t, f, r, ic, whole) } },
            onFill = {
                showSettings = false
                val origs = trItems.map { it.orig }
                val trs = List(trItems.size) { i -> edits[i] ?: "" }
                commit(TrTools.fillIdentical(origs, trs))
            },
            onCodes = { includeEmpty ->
                showSettings = false
                val changes = LinkedHashMap<Int, String>()
                for ((i, row) in trItems.withIndex()) {
                    val cur = edits[i] ?: ""
                    val nt = TrTools.fillCodes(row.orig, cur, includeEmpty)
                    if (nt != cur) changes[i] = nt
                }
                commit(changes)
            },
            onGlossImport = { rep -> importReplace = rep; pickGlossIn.launch(arrayOf("*/*")) },
            onGlossExport = {
                if (glossary.isEmpty()) toast(s.r.gcsvNoTerms) else pickGlossOut.launch("glosarium.csv")
            },
            autoOn = autoOn,
            autoMin = autoMin,
            onAutoOn = { v -> autoOn = v; vm.setPref("tr_auto", if (v) "1" else "0") },
            onAutoMin = { m -> autoMin = m; vm.setPref("tr_auto_min", m.toString()) },
        )
    } else JobScreen(
        vm, s.r.edTitle, null, requestBack,
        tail = { _ ->
            if (trItems.isNotEmpty()) {
                item(key = "tr-title") { ColumnTitle(s.r.edColTitle) }
                item(key = "tr-stats") {
                    Column(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // whole-number percentage (rounded down), so it only changes every 1%
                        val pct = if (trItems.isEmpty()) 0 else (filledCount.toLong() * 100 / trItems.size).toInt().coerceIn(0, 100)
                        Text(
                            s.r.edStats(filledCount, trItems.size, pct), color = p.muted, fontSize = 14.sp,
                            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                        )
                        if (autoOn) {
                            Text(
                                autoTime?.let { t -> s.r.autoSavedAt(t) } ?: s.r.autoWaiting(autoMin),
                                color = p.muted, fontSize = 12.sp,
                                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
                item(key = "tr-icons") {
                    EditorIconBar(
                        canUndo = undo.isNotEmpty(),
                        onUndo = { doUndo() },
                        onHistory = { showHistory = true },
                        onGlossary = { showGlossary = true },
                        onOnline = { showOnline = true },
                        onSettings = { showSettings = true },
                    )
                }
                item(key = "tr-search") {
                    AppTextField(value = query, onValueChange = { v -> query = v; page = 0 }, label = s.r.edSearch)
                }
                item(key = "tr-filter") {
                    SegmentedChips(listOf(s.r.filterAll, s.r.filterFilled, s.r.filterEmpty, s.ot.filterOnline), filter, 14.sp) { i -> filter = i; page = 0 }
                }
                if (pages > 1) item(key = "tr-pg-top") { PagerBar(pg, pages) { np -> page = np } }
                items(slice, key = { idx -> idx }) { idx ->
                    TrRow(trItems[idx].orig, edits[idx] ?: "", glossary) { v -> edits[idx] = v; onlineSet.remove(idx); track(idx, v) }
                }
                if (pages > 1) {
                    item(key = "tr-pg-bot") {
                        PagerBar(pg, pages) { np ->
                            page = np
                            scrollUp(slice.size)
                        }
                    }
                }
            }
        },
        listState = listState,
        summaryTitle = s.r.edSummaryTitle,
        okLabel = s.r.edOkLabel,
    ) { st, onCancel ->
        val r = st.rpgm
        item {
            PickerCard(
                icon = AppIcons.File,
                label = s.r.edFileLabel,
                value = r.fileName,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!st.running) pickFile.launch(arrayOf("*/*")) },
            )
        }
        val info = r.trInfo
        item {
            if (info != null && r.error == null) Text(info, color = p.muted, fontSize = 13.sp)
            else if (r.fileName == null) DetailLink(s.r.edHint, 13.sp, 18.sp)
        }
        val err = r.error
        if (err != null) item { ErrorCard(err) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        if (!st.running) {
            item {
                SecondaryButton(s.r.edSaveDraft, vm.canRunTrEdit()) {
                    pending = edits.filterValues { it.isNotBlank() }.toMap()
                    vm.saveTranslation(edits.toMap(), true)
                }
            }
        }
        runButton(st, s.r.edApply, vm.canRunTrEdit(), onCancel) {
            pending = edits.filterValues { it.isNotBlank() }.toMap()
            vm.saveTranslation(edits.toMap(), false)
        }
    }
    if (showLeave) {
        ConfirmDialog(
            title = s.unsavedTitle,
            message = s.r.exitMsg,
            yes = s.unsavedYes,
            no = s.unsavedNo,
            onConfirm = { showLeave = false; onBack() },
            onDismiss = { showLeave = false },
        )
    }
    if (showHistory) {
        val tick = histTick
        val all = hist.entries.toList().asReversed()
        HistoryDialog(
            rows = all.take(200).map { e -> HistRow(e.key, e.value, trItems.getOrNull(e.key)?.orig ?: "", edits[e.key] ?: "") },
            total = all.size,
            onClear = { hist.clear(); histTick = tick + 1 },
            onOpen = { idx -> showHistory = false; goTo(idx) },
            onDismiss = { showHistory = false },
        )
    }
    if (showGlossary) {
        GlossaryDialog(
            glossary = glossary,
            onGlossary = { g ->
                glossary = g
                vm.setPref("tr_glossary", TrTools.writeGlossary(g))
            },
            onDismiss = { showGlossary = false },
            onApply = {
                showGlossary = false
                val origs = trItems.map { it.orig }
                val trs = List(trItems.size) { i -> edits[i] ?: "" }
                commit(TrTools.glossaryChanges(origs, trs, glossary))
            },
        )
    }
}
