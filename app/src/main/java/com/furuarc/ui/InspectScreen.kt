package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.InspectJobs
import com.furuarc.MainViewModel
import com.furuarc.core.Finding
import com.furuarc.core.hashLooksComplete
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalStr
import com.furuarc.core.matchHash
import com.furuarc.core.ReportText
import com.furuarc.core.verdictOf
import com.furuarc.i18n.ScriptStrings
import com.furuarc.i18n.checkStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

@Composable
fun InspectScreen(vm: MainViewModel, onBrowse: () -> Unit, onUnpack: () -> Unit, onBack: () -> Unit) {
    val p = LocalPalette.current
    val lang = LocalLang.current
    val ts = ScriptStrings.of(lang)
    val c = checkStr(lang)
    val st by InspectJobs.state.collectAsState()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) InspectJobs.inspect(uri)
    }
    val working = st.busy || st.hashing || st.deepRunning
    var pasted by remember(st.name) { mutableStateOf("") }

    val tp = st.textPreview
    androidx.activity.compose.BackHandler(enabled = tp != null) { InspectJobs.hideText() }
    if (tp != null) TextFullView(st.name ?: "", tp, st.textTruncated) { InspectJobs.hideText() } else Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(ts.inspectTitle, onBack)
LazyColumn(
        Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) { 
        item {
            PickerCard(
                icon = AppIcons.File,
                label = ts.inspectPicker,
                value = st.name,
                trailingIcon = AppIcons.Folder,
                onClick = { if (!working) pick.launch(arrayOf("*/*")) },
            )
        }
        if (st.name == null) item { Hint(ts.inspectHint) }
        if (st.busy) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(ts.inspectWorking, color = p.muted, fontSize = 13.sp)
                    LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
                }
            }
        }
        st.error?.let { e -> item { SectionCard("Error") { Text(e, color = Color(0xFFE57373)) } } }
        st.report?.let { rep ->
            val verdict = verdictOf(rep.findings + (st.deep ?: emptyList()))
            item { VerdictBanner(verdict, c) }
            if (st.isArchive) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(Modifier.weight(1f)) { PrimaryButton(ts.browseTitle, !working, onBrowse) }
                        Box(Modifier.weight(1f)) { SecondaryButton(c.unpackThis, !working, onUnpack) }
                    }
                }
            }
            if (rep.findings.any { it.tag == Finding.TAG_TEXT }) {
                item { SecondaryButton(LocalStr.current.x.viewText, !working) { InspectJobs.showText() } }
            }
            item { SectionCard(ts.inspectInfo) { InfoLines(rep.info) } }
            item {
                SectionCard(ts.inspectFindings) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { rep.findings.forEach { FindingRow(it) } }
                }
            }
            st.hashes?.let { h ->
                item {
                    SectionCard(ts.inspectChecksum) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            HashLine("CRC32", h.crc32)
                            HashLine("MD5", h.md5)
                            HashLine("SHA-256", h.sha256)
                        }
                    }
                }
                item {
                    SectionCard(c.hashMatchLabel) {
                        AppTextField(pasted, { pasted = it }, c.hashMatchHint, mono = true)
                        if (pasted.isNotBlank()) {
                            val m = matchHash(pasted, h)
                            when {
                                m != null -> Text(c.hashMatchOk(m), color = Color(0xFF81C784), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                                !hashLooksComplete(pasted) -> Text(c.hashMatchShort, color = Color(0xFFFFB74D), fontSize = 13.sp)
                                else -> Text(c.hashMatchBad, color = Color(0xFFE57373), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            if (st.hashing) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(ts.inspectHashRun, color = p.muted, fontSize = 13.sp)
                        LinearProgressIndicator(st.hashProgress, Modifier.fillMaxWidth(), color = p.accent)
                        SecondaryButton(ts.inspectCancel) { InspectJobs.cancel() }
                    }
                }
            } else if (st.hashes == null && !st.busy) {
                item { SecondaryButton(ts.inspectHash, !working) { InspectJobs.hash() } }
            }
            if (st.isArchive) {
                if (st.deepRunning) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${ts.inspectDeepRun}: ${st.deepDone}/${st.deepTotal}", color = p.muted, fontSize = 13.sp)
                            LinearProgressIndicator(
                                if (st.deepTotal > 0) st.deepDone.toFloat() / st.deepTotal else 0f,
                                Modifier.fillMaxWidth(), color = p.accent,
                            )
                            SecondaryButton(ts.inspectCancel) { InspectJobs.cancel() }
                        }
                    }
                } else {
                    st.deep?.let { d ->
                        item {
                            SectionCard(ts.inspectDeepResult) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { d.forEach { FindingRow(it) } }
                            }
                        }
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SecondaryButton(ts.inspectDeep, !working) { InspectJobs.deepCheck() }
                            Hint(ts.inspectDeepSub)
                        }
                    }
                }
            }
            if (!working) item { ExportBar(st.name ?: "file") { ReportText.single(lang == com.furuarc.settings.Lang.ID, rep, st.hashes, st.deep) } }
        }
    }
}
}

/** Full screen showing the file contents as text (50 KB pages), like the preview inside an archive. */
@Composable
private fun TextFullView(name: String, text: String, truncated: Boolean, onClose: () -> Unit) {
    val p = LocalPalette.current
    val x = LocalStr.current.x
    Column(Modifier.fillMaxSize().padding(ScreenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenHeader(if (name.isEmpty()) x.viewTextTitle else name, onClose)
        if (truncated) Text(x.viewTextTruncated, color = p.muted, fontSize = 12.sp)
        PagedText(text, Modifier.fillMaxWidth().weight(1f))
        SecondaryButton(x.viewTextClose, onClose)
    }
}
