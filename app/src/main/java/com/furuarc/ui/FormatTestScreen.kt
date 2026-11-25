package com.furuarc.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.Engine
import com.furuarc.FormatTestJobs
import com.furuarc.core.Choice
import com.furuarc.core.FormatRegistry
import com.furuarc.core.FtLevel
import com.furuarc.core.OptKind
import com.furuarc.core.OptionSpec
import com.furuarc.core.RtLine
import com.furuarc.core.RtPlan
import com.furuarc.i18n.checkStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

private val FT_GREEN = Color(0xFF81C784)
private val FT_AMBER = Color(0xFFFFB74D)
private val FT_RED = Color(0xFFE57373)

/** Engines that can pack: VN engines first (A-Z), then RPG Maker XP / VX / VX Ace (their archives). */
private val FT_ENGINES: List<VnEngine> by lazy {
    VN_ENGINES.filter { FormatRegistry.byId(it.formatId).canPack }.sortedBy { it.name.lowercase() } +
        RPGM_ENGINES.filter { it.rpgm && !it.crypt }
}

/** The Format Test engine for an archive format found by File Inspection (several engines can share a format: the extension decides). */
fun ftEngineFor(formatId: String, fileName: String): VnEngine? {
    val l = FT_ENGINES.filter { it.formatId == formatId }
    if (l.isEmpty()) return null
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return l.firstOrNull { it.ext.lowercase() == ext } ?: l.first()
}

/**
 * Format Test with a real archive: choose the engine (format), upload an archive of that format, and the app extracts one
 * random file, packs it again and compares the new archive with the original. Nothing is written to a folder.
 */
@Composable
fun FormatTestScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val s = LocalStr.current
    val lang = LocalLang.current
    val st by FormatTestJobs.state.collectAsState()

    var engineId by remember { mutableStateOf("") }
    var uri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    val eng = FT_ENGINES.firstOrNull { it.id == engineId }
    val fmt = eng?.let { FormatRegistry.byId(it.formatId) }
    val uspecs = remember(eng?.id) { fmt?.unpackOptions ?: emptyList() }
    val pspecs = remember(eng?.id) { fmt?.packOptions ?: emptyList() }
    val uVals = remember(eng?.id) { mutableStateMapOf<String, String>().apply { uspecs.forEach { put(it.key, it.default) } } }
    val pVals = remember(eng?.id) { mutableStateMapOf<String, String>().apply { pspecs.forEach { put(it.key, it.default) } } }
    var keyText by remember(eng?.id) { mutableStateOf("") }
    var maxText by remember { mutableStateOf("5") }
    var unit by remember { mutableStateOf(1) } // 0 = KB, 1 = MB
    val ftVals = remember { mutableStateMapOf("mode" to "one", "count" to "3", "pick" to "", "seed" to "") }
    val ctx = LocalContext.current
    var openUnpack by remember { mutableStateOf(false) }
    var openPack by remember { mutableStateOf(false) }
    // a file that cannot be read yet: open the unpack settings (index file, archive name ...)
    LaunchedEffect(st.check.bad, eng?.id) { if (st.check.bad && uspecs.isNotEmpty()) openUnpack = true }

    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) { uri = u; fileName = Engine.displayName(u) }
    }
    val saveTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { t ->
        if (t != null) FormatTestJobs.saveSample(t) { ok -> Toast.makeText(ctx, if (ok) c.ftSampleSaved else c.ftSampleFail, Toast.LENGTH_SHORT).show() }
    }
    // opened from File Inspection: the archive and its engine are already chosen
    LaunchedEffect(Unit) {
        FormatTestJobs.takePrefill()?.let { pf ->
            ftEngineFor(pf.formatId, pf.name)?.let { e -> engineId = e.id; uri = pf.uri; fileName = pf.name }
        }
    }
    val modeSpecs = remember(lang) {
        listOf(
            OptionSpec(
                key = "mode", kind = OptKind.CHOICE, en = c.ftModeLabel, id = c.ftModeLabel,
                choices = listOf(
                    Choice("one", c.ftModeOne, c.ftModeOne), Choice("n", c.ftModeN, c.ftModeN),
                    Choice("all", c.ftModeAll, c.ftModeAll), Choice("full", c.ftModeFull, c.ftModeFull),
                ),
                default = "one",
            ),
            OptionSpec(key = "count", kind = OptKind.NUMBER, en = c.ftCount, id = c.ftCount, default = "3", showIf = Pair("mode", "n")),
            OptionSpec(key = "pick", kind = OptKind.TEXT, en = c.ftPick, id = c.ftPick, default = "", showIf = Pair("mode", "one")),
            OptionSpec(key = "seed", kind = OptKind.TEXT, en = c.ftSeed, id = c.ftSeed, default = ""),
        )
    }
    val engineSpec = remember(lang) {
        OptionSpec(
            key = "engine", kind = OptKind.CHOICE, en = c.ftEngine, id = c.ftEngine,
            choices = listOf(Choice("", c.ftEnginePick, c.ftEnginePick)) +
                FT_ENGINES.map { e -> Choice(e.id, "${e.name} (.${e.ext})", "${e.name} (.${e.ext})") },
            default = "",
        )
    }

    // check the file against the chosen format (also when an unpack option such as the encoding changes)
    val uExtra = uVals.toMap()
    LaunchedEffect(uri, eng?.id, uExtra) {
        val u = uri
        val e = eng
        if (u != null && e != null) {
            kotlinx.coroutines.delay(400) // while a setting is typed, wait for a pause before checking again
            FormatTestJobs.check(u, fileName ?: "archive", e.formatId, uExtra)
        } else FormatTestJobs.resetCheck()
    }

    val maxBytes = (maxText.toLongOrNull() ?: 0L) * (if (unit == 1) 1024L * 1024L else 1024L)
    val full = ftVals["mode"] == "full"
    fun plan(): RtPlan {
        val mode = when (ftVals["mode"]) { "n" -> RtPlan.N; "all" -> RtPlan.ALL; "full" -> RtPlan.FULL; else -> RtPlan.ONE }
        val seedText = (ftVals["seed"] ?: "").trim()
        val seed = if (seedText.isEmpty()) System.nanoTime() else (seedText.toLongOrNull() ?: seedText.hashCode().toLong())
        return RtPlan(mode, (ftVals["count"]?.toIntOrNull() ?: 3).coerceIn(1, 500), ftVals["pick"] ?: "", seed, maxBytes)
    }
    val ok = eng != null && uri != null && !st.check.busy && !st.check.bad && st.check.entries > 0
    val fail = st.lines.any { it.level == FtLevel.FAIL }
    val warn = st.lines.any { it.level == FtLevel.WARN }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(c.ftTitle, onBack)
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            item { Hint(c.ftHint) }
            optionPanel(null, listOf(engineSpec), mapOf("engine" to engineId), lang, !st.running, "ft_engine_") { _, v ->
                engineId = v
            }
            item {
                PickerCard(
                    icon = AppIcons.File,
                    label = c.ftFile,
                    value = fileName,
                    trailingIcon = AppIcons.Folder,
                    onClick = { if (!st.running) pick.launch(arrayOf("*/*")) },
                )
            }
            if (eng != null && uri != null) {
                item {
                    when {
                        st.check.busy -> Hint(c.ftChecking)
                        st.check.bad -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(s.wrongFile(st.check.foundName, eng.name), color = FT_RED, fontSize = 13.sp, lineHeight = 18.sp)
                            // some formats can only be read with settings (an index file, an archive name): explain where to set them
                            val needs = uspecs.any { it.kind == OptKind.FILE || it.kind == OptKind.TEXT }
                            val d = st.check.detail
                            if (needs && d != null) Text(com.furuarc.i18n.Tr.msg(lang, d), color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                            if (needs) Hint(c.ftNeedOptions)
                        }
                        else -> Text(c.ftMatch(st.check.entries), color = FT_GREEN, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            // the unpack settings are shown as soon as a file is chosen: some formats cannot be read without them
            if (eng != null && uri != null) {
                item { FtSection(c.ftUnpackSection, openUnpack, { openUnpack = !openUnpack }) {
                    OptionColumn(uspecs, uVals, lang, !st.running) { k, v -> uVals[k] = v }
                    if (fmt?.id == "xp3") run {
                        AppTextField(
                            value = keyText,
                            onValueChange = { keyText = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = s.xorKeyAutoLabel,
                            number = true,
                        )
                    }
                    OptionColumn(modeSpecs, ftVals, lang, !st.running) { k, v -> ftVals[k] = v }
                    if (!full) run {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.weight(1f)) {
                                AppTextField(
                                    value = maxText,
                                    onValueChange = { maxText = it.filter { ch -> ch.isDigit() }.take(7) },
                                    label = c.ftMaxSize,
                                    number = true,
                                )
                            }
                            Box(Modifier.width(128.dp)) { SegmentedChips(listOf("KB", "MB"), unit) { unit = it } }
                        }
                    }
                } }
            }
            if (ok) {
                item { FtSection(c.ftPackSection, openPack, { openPack = !openPack }) {
                    if (pspecs.isEmpty()) Hint(c.ftNoPackOptions)
                    else OptionColumn(pspecs, pVals, lang, !st.running) { k, v -> pVals[k] = v }
                } }
                item {
                    if (st.running) SecondaryButton(c.ftStop) { FormatTestJobs.stopRun() }
                    else PrimaryButton(if (st.lines.isEmpty()) c.ftStart else c.ftAgain, maxBytes > 0 || full) {
                        FormatTestJobs.start(
                            uri!!, fileName ?: "archive", eng!!.formatId, uExtra, keyText.toIntOrNull()?.takeIf { it in 0..255 },
                            pVals.toMap(), plan(),
                        )
                    }
                }
                if (maxBytes <= 0 && !full) item { Text(c.ftMaxSizeBad, color = FT_RED, fontSize = 13.sp) }
            }

            st.error?.let { e -> item { ErrorCard(e) } }
            if (st.lines.isNotEmpty()) {
                item { SectionLabel(c.ftResults) }
                if (!st.running) item { FtBanner(fail, warn, st.stopped) }
                items(st.lines.size) { i -> FtRow(st.lines[i]) }
                if (!st.running) {
                    item { Hint(c.ftNote) }
                    if (st.hasSample) item { SecondaryButton(c.ftSaveSample) { saveTree.launch(null) } }
                    item { ExportBar("uji_coba_format") { FormatTestJobs.reportText() } }
                }
            }
        }
    }
}

/** Open/closed header (same look as the sections of the Media Converter). */
@Composable
private fun FtSection(title: String, open: Boolean, onToggle: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    RowPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().clickable { onToggle() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                Text(if (open) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
            }
            if (open) Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
        }
    }
}

@Composable
private fun FtBanner(fail: Boolean, warn: Boolean, stopped: Boolean) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val level = if (fail) 3 else if (warn) 2 else 1
    val color = when (level) { 3 -> FT_RED; 2 -> FT_AMBER; else -> FT_GREEN }
    val mark = when (level) { 3 -> "✕"; 2 -> "!"; else -> "✓" }
    val title = when (level) { 3 -> c.verdictBad; 2 -> c.verdictWarn; else -> c.ftPassed }
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mark, color = color, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
            Column {
                Text(title, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (stopped) Text(c.ftStopped, color = p.muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun FtRow(l: RtLine) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val color = when (l.level) { FtLevel.PASS -> FT_GREEN; FtLevel.WARN -> FT_AMBER; FtLevel.FAIL -> FT_RED; FtLevel.SKIP -> p.muted }
    val mark = when (l.level) { FtLevel.PASS -> "✓"; FtLevel.WARN -> "!"; FtLevel.FAIL -> "✕"; FtLevel.SKIP -> "–" }
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Text(mark, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.width(24.dp))
            Text(c.rtDetail(l), color = if (l.level == FtLevel.PASS) p.text else color, fontSize = 13.sp, lineHeight = 18.sp)
        }
    }
}
