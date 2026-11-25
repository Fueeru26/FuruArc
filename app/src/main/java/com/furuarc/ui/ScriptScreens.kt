package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MainViewModel
import com.furuarc.core.OptionSpec
import com.furuarc.core.ScriptKind
import com.furuarc.core.ScriptTool
import com.furuarc.core.ScriptTools
import com.furuarc.core.formatBytes
import com.furuarc.i18n.ScriptStrings
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

fun toolIcon(tool: ScriptTool): ImageVector = when (tool) {
    ScriptTool.RPYC -> AppIcons.Unarchive
    ScriptTool.EXPORT -> AppIcons.FolderUpload
    ScriptTool.IMPORT -> AppIcons.Translate
    ScriptTool.NS_DECODE -> AppIcons.LockOpen
    ScriptTool.NS_ENCODE -> AppIcons.Lock
    ScriptTool.LIST -> AppIcons.File
}

/** Job screen for one script tool ([tool]) for engine [engine]. */
@Composable
fun ToolJobScreen(vm: MainViewModel, engine: VnEngine?, tool: ScriptTool, onBack: () -> Unit, onTranslateText: (android.net.Uri) -> Unit = {}) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val settings by vm.settings.collectAsState()
    val ts = ScriptStrings.of(settings.lang)
    val auto = engine == null
    val engineKey = engine?.id ?: ScriptTools.AUTO
    val kind = ScriptTools.kindFor(engineKey)
    val usesEnc = auto && (tool == ScriptTool.EXPORT || tool == ScriptTool.IMPORT) || (kind == ScriptKind.NSCRIPT || kind == ScriptKind.QLIE || kind == ScriptKind.ARTEMIS || kind == ScriptKind.CST || kind == ScriptKind.ESCUDE || kind == ScriptKind.HCB || kind == ScriptKind.WS2 || kind == ScriptKind.SILKY || kind == ScriptKind.BGI || kind == ScriptKind.ITUFURU || kind == ScriptKind.NEXAS || kind == ScriptKind.YSTB) && (tool == ScriptTool.EXPORT || tool == ScriptTool.IMPORT)
    var fmt by remember { mutableStateOf(when (vm.pref("tr_fmt", "txt")) { "csv" -> 0; "json" -> 2; "vntp" -> 3; else -> 1 }) }
    var outBase by remember { mutableStateOf("translate") }
    LaunchedEffect(Unit) { vm.setExportBase(outBase) }
    val encSpec = remember(settings.lang, auto) {
        val e = ts.encSpec
        if (auto) OptionSpec(e.key, e.kind, e.en, e.id, e.choices, "auto", e.hintEn, e.hintId, e.showIf) else e
    }
    val optVals = remember(engineKey) {
        mutableStateMapOf<String, String>().apply { put("enc", vm.pref("sc_enc_" + engineKey, encSpec.default)) }
    }

    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.rpgmInputFolder(uri)
    }
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.rpgmInputFile(uri)
    }
    val pickArchive = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.setArchive(uri, engine?.formatId)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }

    JobScreen(
        vm, if (auto && tool == ScriptTool.LIST) ts.autoListTitle else ts.title(tool), engine?.name ?: ts.autoLabel, onBack,
        summaryTitle = ts.summaryTitle(tool.mode),
        okLabel = ts.okLabel(tool.mode),
        onTranslate = if (tool == ScriptTool.EXPORT && vm.state.value.done) vm.exportedFile()?.let { u -> { onTranslateText(u) } } else null,
    ) { st, onCancel ->
        val r = st.rpgm
        if (tool == ScriptTool.LIST) {
            item {
                PickerCard(
                    icon = AppIcons.File,
                    label = s.archiveFile,
                    value = st.archiveName,
                    trailingIcon = AppIcons.Folder,
                    onClick = { if (!st.running) pickArchive.launch(arrayOf("*/*")) },
                )
            }
            val aerr = st.archiveError
            if (aerr != null) item { ErrorCard(aerr) }
            if (st.archiveName != null && aerr == null) {
                item { InfoCard(listOf(Pair(s.infoFormat, st.formatName ?: "-"), Pair(s.infoFiles, st.entryCount.toString()))) }
            }
        } else {
            item {
                PickerCard(
                    icon = AppIcons.Folder,
                    label = when (tool) {
                        ScriptTool.RPYC -> ts.folderRpyc
                        ScriptTool.NS_DECODE, ScriptTool.NS_ENCODE -> ts.folderNs
                        else -> ts.folderScript
                    },
                    value = r.inName,
                    trailingIcon = AppIcons.FolderPlus,
                    onClick = { if (!st.running) pickFolder.launch(null) },
                )
            }
            if (r.inName != null) {
                item {
                    Text(
                        if (r.scanning) s.packScanning else ts.found(r.found, formatBytes(r.foundBytes)),
                        color = p.muted, fontSize = 13.sp,
                    )
                }
            }
        }
        if (tool == ScriptTool.IMPORT) {
            item {
                PickerCard(
                    icon = AppIcons.File,
                    label = ts.translationFile,
                    value = r.fileName,
                    trailingIcon = AppIcons.Folder,
                    onClick = { if (!st.running) pickFile.launch(arrayOf("*/*")) },
                )
            }
        }
        // every setting of the tool sits in one "Options" label background
        item {
            LabelSection(optionsTitle(settings.lang)) {
                if (tool == ScriptTool.EXPORT) {
                    SectionLabel(ts.formatLabel)
                    val pickFmt = { i: Int ->
                        if (!st.running) {
                            fmt = i
                            vm.setPref("tr_fmt", when (i) { 1 -> "txt"; 2 -> "json"; 3 -> "vntp"; else -> "csv" })
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SegmentedChips(listOf("CSV", "TXT"), if (fmt < 2) fmt else -1) { i -> pickFmt(i) }
                        SegmentedChips(listOf("JSON", "JSON VNTP"), if (fmt >= 2) fmt - 2 else -1) { i -> pickFmt(i + 2) }
                    }
                    if (fmt == 3) DetailHint(ts.vntpHint)
                    FileNameField(outBase, "." + com.furuarc.core.RpgmText.extFor(fmt), s.x.outFileName) {
                        outBase = it; vm.setExportBase(it)
                    }
                }
                if (usesEnc) {
                    OptionColumn(listOf(encSpec), optVals, settings.lang, !st.running) { k, v ->
                        optVals[k] = v
                        vm.setPref("sc_enc_" + engineKey, v)
                    }
                }
                val nt = ts.note(tool, kind)
                if (nt.length >= DETAIL_MIN_CHARS) DetailLink(nt, 13.sp, 19.sp) else Text(nt, color = p.muted, fontSize = 13.sp)
            }
        }
        val err = r.error
        if (err != null && tool != ScriptTool.LIST) item { ErrorCard(err) }
        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }
        runButton(st, ts.btn(tool), vm.canRunTool(tool) && (tool != ScriptTool.EXPORT || outBase.isNotBlank()), onCancel) {
            vm.runTool(tool, engineKey, if (usesEnc) (optVals["enc"] ?: "sjis") else "auto", fmt)
        }
    }
}
