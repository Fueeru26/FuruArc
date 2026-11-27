package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.FolderDiffJobs
import com.furuarc.i18n.folderDiffStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

/** Folder Compare: original folder vs edited folder, with the option to copy the changed and new files to another folder. */
@Composable
fun FolderDiffScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val lang = LocalLang.current
    val d = folderDiffStr(lang)
    val st by FolderDiffJobs.state.collectAsState()
    val ctx = LocalContext.current
    fun grant(u: android.net.Uri, write: Boolean) {
        try {
            val fl = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or (if (write) android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
            ctx.contentResolver.takePersistableUriPermission(u, fl)
        } catch (e: Exception) { }
    }
    val pickA = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) { grant(u, false); FolderDiffJobs.setA(u) } }
    val pickB = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) { grant(u, false); FolderDiffJobs.setB(u) } }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u -> if (u != null) { grant(u, true); FolderDiffJobs.setOut(u) } }
    val busy = st.running || st.copying
    val total = st.changed.size + st.added.size

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(d.title, onBack, fixed = true)
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            item { DetailLink(d.intro, 13.sp, 19.sp) }
            item { PickerCard(AppIcons.Folder, d.folderA, st.nameA, AppIcons.Folder) { pickA.launch(null) } }
            item { PickerCard(AppIcons.Folder, d.folderB, st.nameB, AppIcons.Folder) { pickB.launch(null) } }
            item {
                LabelSection(d.optTitle) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { FolderDiffJobs.setDeep(!st.deep) },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(checked = st.deep, onCheckedChange = { FolderDiffJobs.setDeep(it) }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                        Text(d.deep, color = p.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    }
                    DetailLink(d.deepNote, 12.sp, 17.sp)
                    AppTextField(st.ignore, { FolderDiffJobs.setIgnore(it) }, d.ignoreLabel)
                    DetailLink(d.ignoreNote, 12.sp, 17.sp)
                }
            }
            item { PickerCard(AppIcons.FolderUpload, d.folderOut, st.nameOut, AppIcons.Folder) { pickOut.launch(null) } }
            item {
                if (st.running) SecondaryButton(d.cancel) { FolderDiffJobs.cancel() }
                else PrimaryButton(d.compare, FolderDiffJobs.hasA() && FolderDiffJobs.hasB() && !busy) { FolderDiffJobs.run() }
            }
            item { SecondaryButton(d.backupAll, FolderDiffJobs.hasB() && FolderDiffJobs.hasOut() && !busy) { FolderDiffJobs.copyAll() } }
            item { DetailLink(d.backupNote, 12.sp, 17.sp) }
            if (st.copyDone && !st.done) item { Text(d.copyResult(st.copied, st.copyFailed), color = if (st.copyFailed > 0) androidx.compose.ui.graphics.Color(0xFFE53935) else p.accent, fontSize = 14.sp) }
            if (busy) item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LinearProgressIndicator(progress = st.progress, modifier = Modifier.fillMaxWidth(), color = p.accent)
                    if (st.label.isNotEmpty()) Text(st.label, color = p.muted, fontSize = 12.sp, maxLines = 1)
                }
            }
            st.error?.let { err -> item { Text(err, color = androidx.compose.ui.graphics.Color(0xFFE53935), fontSize = 13.sp) } }
            if (st.done) {
                item {
                    LabelSection(d.summary) {
                        Text(d.counts(st.changed.size, st.added.size, st.removed.size, st.same), color = p.text, fontSize = 14.sp, lineHeight = 21.sp)
                        if (total == 0 && st.removed.isEmpty()) Text(d.noChange, color = p.muted, fontSize = 13.sp)
                    }
                }
                if (st.changed.isNotEmpty()) item { PagedPanel(d.changedList, st.changed, 50, st.changed.size) { Text(it, color = p.text, fontSize = 13.sp, fontFamily = FontFamily.Monospace) } }
                if (st.added.isNotEmpty()) item { PagedPanel(d.addedList, st.added, 50, st.added.size) { Text(it, color = p.text, fontSize = 13.sp, fontFamily = FontFamily.Monospace) } }
                if (st.removed.isNotEmpty()) item { PagedPanel(d.removedList, st.removed, 50, st.removed.size) { Text(it, color = p.text, fontSize = 13.sp, fontFamily = FontFamily.Monospace) } }
                item { DetailLink(d.copyNote, 12.sp, 17.sp) }
                item { PrimaryButton(d.copy, FolderDiffJobs.outUri != null && total > 0 && !busy) { FolderDiffJobs.copyChanged() } }
                if (st.copyDone) item { Text(d.copyResult(st.copied, st.copyFailed), color = if (st.copyFailed > 0) androidx.compose.ui.graphics.Color(0xFFE53935) else p.accent, fontSize = 14.sp) }
                item { ExportBar(st.nameB ?: "folder") { FolderDiffJobs.reportText(lang == com.furuarc.settings.Lang.ID) } }
            }
        }
    }
}
