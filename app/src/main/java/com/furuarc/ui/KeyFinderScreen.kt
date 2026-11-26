package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.KeyFinderJobs
import com.furuarc.core.KeyFinder
import com.furuarc.i18n.checkStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private const val KF_PAGE = 50

/** Key Finder. [engine] = KeyFinder.ALL or a module id ("siglus", "rpgm", "xp3"). */
@Composable
fun KeyFinderScreen(engine: String, onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val ctx = LocalContext.current
    val st by KeyFinderJobs.state.collectAsState()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) KeyFinderJobs.pick(uri)
    }
    val pages = remember { mutableStateMapOf<Int, Int>() }
    val rep = st.report
    val scope = if (engine == KeyFinder.ALL) c.kfScopeAll else when (engine) {
        "siglus" -> "Siglus Engine"
        "rpgm" -> "RPG Maker MV/MZ"
        "xp3" -> "Kirikiri XP3"
        "catsystem2" -> "CatSystem2"
        "entisgls" -> "EntisGLS"
        else -> engine
    }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(c.kfTitle, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, c.kfPicker, st.name, AppIcons.Folder) { if (!st.running) { pages.clear(); pick.launch(null) } } }
        item { Hint("${c.kfScope}: $scope") }
        if (st.name == null) item { DetailHint(c.kfHint) }
        item { PrimaryButton(c.kfAnalyze, st.name != null && !st.running) { pages.clear(); KeyFinderJobs.analyze(engine) } }
        st.error?.let { e -> item { ErrorCard(e) } }
        if (st.running) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (st.listing) "…" else c.kfProgress(st.label, st.step, st.steps), color = p.muted, fontSize = 13.sp)
                    LinearProgressIndicator(
                        if (st.steps > 0) st.step.toFloat() / st.steps else 0f, Modifier.fillMaxWidth(), color = p.accent,
                    )
                    SecondaryButton(if (c.kfAnalyze == "Analisis") "Batalkan" else "Cancel") { KeyFinderJobs.cancel() }
                }
            }
        }
        if (rep != null && !st.running) {
            item {
                SectionCard(c.kfTitle) {
                    Text(c.kfFound(rep.hits.size, rep.scanned), color = p.text, fontSize = 14.sp)
                    if (rep.hits.isEmpty()) Text(c.kfNoKey, color = p.muted, fontSize = 13.sp)
                }
            }
            rep.hits.forEachIndexed { hi, h ->
                item(key = "hit$hi") {
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(h.engine, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Text(
                                    if (h.status == 2) "✓ ${c.kfVerified}" else "! ${c.kfCandidate}",
                                    color = if (h.status == 2) Color(0xFF81C784) else Color(0xFFFFB74D),
                                    fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                                )
                            }
                            Text(h.keyKind, color = p.muted, fontSize = 12.sp)
                            Text(c.kfKey, color = p.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text(h.key, color = p.text, fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 20.sp)
                            Text("${c.kfSource}: ${h.source}", color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                            Text(h.note, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                            if (h.optionValue != null) Text(c.kfUseHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                            SecondaryButton(c.kfCopyKey) { copyToClipboard(ctx, h.optionValue ?: h.key, c.kfKeyCopied) }
                        }
                    }
                }
                item(key = "hitfiles$hi") {
                    PagedPanel(c.kfFiles(h.files.size), h.files, KF_PAGE, hi) { f ->
                        Text(f, color = p.text, fontSize = 13.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if (rep.notes.isNotEmpty()) {
                item {
                    SectionCard(c.kfNotes) {
                        rep.notes.forEach { Text("• $it", color = p.muted, fontSize = 13.sp, lineHeight = 18.sp) }
                    }
                }
            }
            item { ExportBar(st.name ?: "kunci") { KeyFinderJobs.reportText() } }
        }
    }
}
}
