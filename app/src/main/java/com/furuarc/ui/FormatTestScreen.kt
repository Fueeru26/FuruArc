package com.furuarc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.FormatTestJobs
import com.furuarc.core.FtLevel
import com.furuarc.core.FtResult
import com.furuarc.i18n.checkStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private val FT_GREEN = Color(0xFF81C784)
private val FT_AMBER = Color(0xFFFFB74D)
private val FT_RED = Color(0xFFE57373)

/**
 * Format Test: packs sample files with every packable format, unpacks them again and compares the result.
 * Runs on this device, so a failure points at the format or at the device (storage, encoding), not at a game file.
 */
@Composable
fun FormatTestScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val st by FormatTestJobs.state.collectAsState()
    var filter by remember { mutableStateOf(0) }

    val pass = st.results.count { it.level == FtLevel.PASS }
    val warn = st.results.count { it.level == FtLevel.WARN }
    val fail = st.results.count { it.level == FtLevel.FAIL }
    val skip = st.results.count { it.level == FtLevel.SKIP }
    val shown = remember(st.results, filter) {
        when (filter) {
            1 -> st.results.filter { it.level == FtLevel.WARN }
            2 -> st.results.filter { it.level == FtLevel.FAIL }
            3 -> st.results.filter { it.level == FtLevel.PASS }
            else -> st.results
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(c.ftTitle, onBack)
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            if (st.results.isEmpty() && !st.running) item { Hint(c.ftHint) }
            item {
                if (st.running) SecondaryButton(c.ftStop) { FormatTestJobs.stopRun() }
                else PrimaryButton(if (st.results.isEmpty()) c.ftStart else c.ftAgain, true) { filter = 0; FormatTestJobs.start() }
            }
            if (st.running) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(if (st.total > 0) st.done.toFloat() / st.total else 0f, Modifier.fillMaxWidth(), color = p.accent)
                        Text(c.ftProgress(st.done, st.total, st.label), color = p.muted, fontSize = 13.sp)
                    }
                }
            }
            st.error?.let { e -> item { ErrorCard(e) } }
            if (st.results.isNotEmpty()) {
                if (!st.running) {
                    item { FtBanner(pass, warn, fail, skip, st.stopped) }
                }
                item { SegmentedChips(listOf(c.filterAll, c.filterWarn, c.filterBad, c.filterOk), filter) { filter = it } }
                items(shown) { r -> FtRow(r) }
                if (!st.running) {
                    item { Hint(c.ftNote) }
                    item { ExportBar("uji_coba_format") { FormatTestJobs.reportText() } }
                }
            }
        }
    }
}

@Composable
private fun FtBanner(pass: Int, warn: Int, fail: Int, skip: Int, stopped: Boolean) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val level = if (fail > 0) 3 else if (warn > 0) 2 else 1
    val color = when (level) { 3 -> FT_RED; 2 -> FT_AMBER; else -> FT_GREEN }
    val mark = when (level) { 3 -> "✕"; 2 -> "!"; else -> "✓" }
    val title = when (level) { 3 -> c.verdictBad; 2 -> c.verdictWarn; else -> c.ftAllPass }
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mark, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(c.ftSummary(pass, warn, fail, skip), color = p.muted, fontSize = 13.sp)
                if (stopped) Text(c.ftStopped, color = p.muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FtRow(r: FtResult) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val color = when (r.level) { FtLevel.PASS -> FT_GREEN; FtLevel.WARN -> FT_AMBER; FtLevel.FAIL -> FT_RED; FtLevel.SKIP -> p.muted }
    val mark = when (r.level) { FtLevel.PASS -> "✓"; FtLevel.WARN -> "!"; FtLevel.FAIL -> "✕"; FtLevel.SKIP -> "–" }
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Text(mark, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.width(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(if (r.variant.isEmpty()) r.engine else r.engine + " · " + r.variant, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(c.ftDetail(r), color = if (r.level == FtLevel.PASS || r.level == FtLevel.SKIP) p.muted else color, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}
