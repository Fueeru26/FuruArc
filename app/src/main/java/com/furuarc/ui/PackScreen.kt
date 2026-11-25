package com.furuarc.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.furuarc.MainViewModel
import com.furuarc.core.Choice
import com.furuarc.core.FormatRegistry
import com.furuarc.core.OptKind
import com.furuarc.core.OptionSpec
import com.furuarc.core.formatBytes
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

@Composable
fun PackScreen(vm: MainViewModel, engine: VnEngine?, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val st by vm.state.collectAsState()
    val settings by vm.settings.collectAsState()
    val ts = com.furuarc.i18n.ScriptStrings.of(settings.lang)
    // Automatic mode (engine == null): the format is chosen via dropdown; only engines that can pack.
    val packable = remember { VN_ENGINES.filter { FormatRegistry.byId(it.formatId).canPack }.sortedBy { it.name.lowercase() } }
    var picked by remember { mutableStateOf<VnEngine?>(null) }
    val eng: VnEngine? = engine ?: picked
    // The format is determined by the engine; the output file name is filled in automatically (e.g. root.pfs for Artemis)
    var fileName by remember(eng?.id) {
        val d = eng?.defaultOut ?: ""
        mutableStateOf(if (eng != null && d.lowercase().endsWith("." + eng.ext.lowercase())) d.dropLast(eng.ext.length + 1) else d)
    }
    val fmt = eng?.let { FormatRegistry.byId(it.formatId) }
    // per-engine options (dropdown / text / checkbox); the last entries are remembered
    val specs = fmt?.packOptions ?: emptyList()
    val optVals = remember(eng?.id) {
        mutableStateMapOf<String, String>().apply {
            specs.forEach { sp -> put(sp.key, vm.pref("opt_${eng?.id}_${sp.key}", sp.default)) }
        }
    }
    val formatSpec = remember(settings.lang) {
        OptionSpec(
            key = "fmt", kind = OptKind.CHOICE, en = ts.packFormat, id = ts.packFormat,
            choices = listOf(Choice("", ts.packFormatPick, ts.packFormatPick)) +
                packable.map { e -> Choice(e.id, "${e.name} (.${e.ext})", "${e.name} (.${e.ext})") },
            default = "",
        )
    }

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
    // the "ignore" patterns are set in Settings; recount files if the patterns change
    LaunchedEffect(settings.ignore) { vm.setIgnore(settings.ignore + (if (com.furuarc.Pipeline.ignoreBackup) ",_backup" else "")) }

    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setPackFolder(uri)
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setPackOutput(uri)
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(if (engine?.rpgm == true) s.assetPackTitle else s.packTitle, requestBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) { 
        if (engine != null) {
            item { Text("${engine.name} · .${engine.ext}", color = p.accent, fontWeight = FontWeight.SemiBold) }
        }

        item {
            PickerCard(
                icon = AppIcons.Folder,
                label = s.inputDir,
                value = st.packFolderName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickFolder.launch(null) },
            )
        }

        // file count & estimated size of the folder contents
        if (st.packFolderName != null) {
            item {
                Text(
                    if (st.packScanning) s.packScanning else s.packStats(st.packFiles, formatBytes(st.packBytes), st.packIgnored),
                    color = p.muted,
                    fontSize = 13.sp,
                )
            }
        }

        if (engine == null) {
            optionPanel(null, listOf(formatSpec), mapOf("fmt" to (picked?.id ?: "")), settings.lang, !st.running, "fmt_") { _, v ->
                picked = packable.firstOrNull { it.id == v }
            }
        }

        if (eng != null) {
            optionPanel(optionsTitle(settings.lang), specs, optVals, settings.lang, !st.running) { k, v -> optVals[k] = v }

            item {
                FileNameField(fileName, "." + (fmt?.packExtension(optVals.toMap()) ?: eng.ext), s.outputFilename) { fileName = it }
            }
        }

        item {
            PickerCard(
                icon = AppIcons.FolderUpload,
                label = s.outputDir,
                value = st.packOutName,
                trailingIcon = AppIcons.FolderPlus,
                onClick = { if (!st.running) pickOut.launch(null) },
            )
        }

        item {
            if (st.running) {
                SecondaryButton(s.btnCancel) { confirm = 1 }
            } else {
                PrimaryButton(
                    text = s.btnCreate,
                    enabled = eng != null && fmt != null && vm.canPack() && fileName.isNotBlank(),
                    onClick = {
                        if (eng == null || fmt == null) return@PrimaryButton
                        specs.forEach { sp -> vm.setPref("opt_${eng.id}_${sp.key}", optVals[sp.key] ?: sp.default) }
                        // make sure the extension matches the engine
                        val finalName = fileName.trim() + "." + (fmt.packExtension(optVals.toMap()) ?: eng.ext)
                        vm.pack(fmt.id, finalName, optVals.toMap())
                    },
                )
            }
        }

        processItems(
            st,
            onOpenFolder = { openFolder(ctx, vm.resultFolder(), s.openWith, s.cannotOpenFolder) },
            onCopyLog = { copyToClipboard(ctx, st.log.joinToString("\n"), s.logCopied) },
            onVerify = { vm.verifyPack() },
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
