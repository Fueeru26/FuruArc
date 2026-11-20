package com.furuarc.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.furuarc.MainViewModel
import com.furuarc.core.FormatRegistry
import com.furuarc.i18n.r
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

@Composable
fun UnpackScreen(vm: MainViewModel, engine: VnEngine?, onSelect: () -> Unit, onBrowse: () -> Unit, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val st by vm.state.collectAsState()
    var keyText by remember { mutableStateOf("") }
    val settings by vm.settings.collectAsState()
    val ts = com.furuarc.i18n.ScriptStrings.of(settings.lang)
    // Auto mode (engine == null): the format is detected from the file; the options follow the detected format.
    var detectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(st.formatId) { if (st.formatId != null) detectedId = st.formatId }
    val optId: String? = if (engine != null) engine.id else detectedId
    val optFormat = if (engine != null) engine.formatId else detectedId
    // per-engine unpack options (e.g. file name encoding); changes reload the file list
    val uspecs = remember(optId) { optFormat?.let { FormatRegistry.byId(it).unpackOptions } ?: emptyList() }
    val uVals = remember(optId) {
        mutableStateMapOf<String, String>().apply {
            uspecs.forEach { sp -> put(sp.key, vm.pref("uopt_${optId}_${sp.key}", sp.default)) }
        }
    }
    LaunchedEffect(uVals.toMap()) { vm.setUnpackExtra(uVals.toMap()) }
    // 0 = no dialog, 1 = from the Cancel button, 2 = from the back button
    var confirm by remember { mutableStateOf(0) }
    val requestBack: () -> Unit = {
        if (st.running) {
            confirm = 2
        } else {
            onBack()
        }
    }
    LaunchedEffect(st.running) { if (!st.running) confirm = 0 }
    BackHandler(enabled = st.running) { confirm = 2 }

    // hold a file in the file picker to select several archives at once (bulk unpack)
    val pickArchive = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) vm.setArchives(uris, engine?.formatId)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setOutputTree(uri)
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(if (engine?.rpgm == true) s.assetUnpackTitle else s.unpackTitle, requestBack)
LazyColumn(
        Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) { 
        item {
            Text(
                if (engine != null) "${engine.name} · .${engine.ext}" else ts.autoLabel,
                color = p.accent, fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            PickerCard(
                icon = AppIcons.File,
                label = s.archiveFile,
                value = st.archiveName,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!st.running) pickArchive.launch(arrayOf("*/*")) },
            )
        }

        // the chosen file is not a format of this engine
        val err = st.archiveError
        if (err != null) {
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Text(err, color = Color(0xFFE57373), fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(18.dp))
                }
            }
        }

        if (st.archiveName != null && err == null) {
            item {
                val lines = ArrayList<Pair<String, String>>()
                lines.add(Pair(s.infoFormat, st.formatName ?: "-"))
                if (st.batchCount > 0) lines.add(Pair(s.r.batchLabel, s.r.batchInfo(st.batchCount, st.batchSkipped)))
                lines.add(Pair(s.infoFiles, st.entryCount.toString()))
                if (st.formatId == "xp3" && st.keyChecked) {
                    val k = st.detectedKey
                    lines.add(
                        Pair(
                            s.infoXor,
                            when {
                                k == null -> s.xorUnknown
                                k == 0 -> s.xorNone
                                else -> s.xorAuto(k)
                            },
                        ),
                    )
                }
                InfoCard(lines)
            }
            if (st.entryCount > 0 && st.batchCount == 0) {
                item { SecondaryButton(ts.browseBtn) { if (!st.running) onBrowse() } }
            }
        }

        // All / Partial (does not apply to bulk unpack)
        if (st.entryCount > 0 && st.batchCount == 0) {
            item {
                SectionCard(s.extractScope) {
                    SegmentedChips(
                        items = listOf(s.scopeAll, s.scopePartial),
                        selected = if (st.scopePartial) 1 else 0,
                        onSelect = { i ->
                            if (!st.running) {
                                if (i == 0) vm.clearSelection() else onSelect()
                            }
                        },
                    )
                    if (st.scopePartial) {
                        Text(
                            s.selectedInfo(st.selFiles, st.selFolders),
                            color = p.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.outName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }

        optionItems(uspecs, uVals, settings.lang, !st.running) { k, v ->
            uVals[k] = v
            vm.setPref("uopt_${optId}_$k", v)
        }

        if (st.formatId == "xp3") {
            item {
                AppTextField(
                    value = keyText,
                    onValueChange = { keyText = it.filter { ch -> ch.isDigit() }.take(3) },
                    label = s.xorKeyAutoLabel,
                    number = true,
                )
            }
        }

        item {
            if (st.running) {
                SecondaryButton(s.btnCancel) { confirm = 1 }
            } else {
                PrimaryButton(
                    text = s.btnExtract,
                    enabled = vm.canUnpack(),
                    onClick = {
                        vm.unpack(keyText.toIntOrNull()?.takeIf { v -> v in 0..255 })
                    },
                )
            }
        }

        processItems(
            st,
            onOpenFolder = { openFolder(ctx, vm.resultFolder(), s.openWith, s.cannotOpenFolder) },
            onCopyLog = { copyToClipboard(ctx, st.log.joinToString("\n"), s.logCopied) },
            onVerify = { vm.verify() },
        )
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
