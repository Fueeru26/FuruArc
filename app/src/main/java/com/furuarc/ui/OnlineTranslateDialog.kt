package com.furuarc.ui

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.OnlineTranslator
import com.furuarc.core.OtCache
import com.furuarc.core.OtConfig
import com.furuarc.core.OtProvider
import com.furuarc.core.TrTools
import com.furuarc.i18n.Strings
import com.furuarc.i18n.ot
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Storage for Online Translation settings. A separate file so the API key can be excluded from backups. */
private class OtPrefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("furuarc_online", Context.MODE_PRIVATE)
    fun get(k: String, d: String) = sp.getString(k, d) ?: d
    fun put(k: String, v: String) { sp.edit().putString(k, v).apply() }
}

private const val DEFAULT_INSTANCES =
    "https://lingva.ml\nhttps://translate.igna.wtf\nhttps://lingva.lunar.icu\nhttps://translate.plausibility.cloud\n" +
        "https://lingva.garudalinux.org\nhttps://translate.dr460nf1r3.org\nhttps://translate.jae.fi"

private val BILI_MODELS = listOf("Index-Translate-9B", "Index-Translate-35B-A3B", "Index-Translate-2B", "Index-Nailong-9B", "Index-Nailong-2B")

private class ProvInfo(val p: OtProvider, val label: String, val keyUrl: String?, val models: List<String>)

private val PROVIDERS = listOf(
    ProvInfo(OtProvider.GOOGLE, "Google", "https://console.cloud.google.com/apis/credentials", emptyList()),
    ProvInfo(OtProvider.DEEPL, "DeepL", "https://www.deepl.com/your-account/keys", emptyList()),
    ProvInfo(OtProvider.GEMINI, "Gemini", "https://aistudio.google.com/app/apikey", listOf("gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro")),
    ProvInfo(OtProvider.OPENAI, "OpenAI", "https://platform.openai.com/api-keys", listOf("gpt-4o-mini", "gpt-4o", "gpt-4.1-mini", "gpt-4.1")),
    ProvInfo(OtProvider.DEEPSEEK, "DeepSeek", "https://platform.deepseek.com/api_keys", listOf("deepseek-chat", "deepseek-reasoner")),
    ProvInfo(OtProvider.CLAUDE, "Claude", "https://console.anthropic.com/settings/keys", listOf("claude-sonnet-5-5", "claude-haiku-5-5", "claude-opus-5-5")),
    ProvInfo(OtProvider.GROQ, "Groq", "https://console.groq.com/keys", listOf("llama-3.3-70b-versatile", "llama-3.1-8b-instant", "openai/gpt-oss-120b", "openai/gpt-oss-20b")),
    ProvInfo(OtProvider.OPENROUTER, "OpenRouter", "https://openrouter.ai/settings/keys", listOf("google/gemini-2.5-flash", "deepseek/deepseek-chat", "openai/gpt-4o-mini", "anthropic/claude-sonnet-4.5")),
    ProvInfo(OtProvider.CUSTOM, "Custom", null, emptyList()),
)

/** Language code + name (Indonesian, English). */
private val LANGS = listOf(
    Triple("id", "Indonesia", "Indonesian"), Triple("en", "Inggris", "English"), Triple("ja", "Jepang", "Japanese"),
    Triple("ko", "Korea", "Korean"), Triple("zh-CN", "Mandarin (Sederhana)", "Chinese (Simplified)"),
    Triple("zh-TW", "Mandarin (Tradisional)", "Chinese (Traditional)"), Triple("ms", "Melayu", "Malay"),
    Triple("es", "Spanyol", "Spanish"), Triple("fr", "Prancis", "French"), Triple("de", "Jerman", "German"),
    Triple("ru", "Rusia", "Russian"), Triple("pt", "Portugis", "Portuguese"), Triple("it", "Italia", "Italian"),
    Triple("th", "Thai", "Thai"), Triple("vi", "Vietnam", "Vietnamese"), Triple("ar", "Arab", "Arabic"),
    Triple("tr", "Turki", "Turkish"), Triple("hi", "Hindi", "Hindi"), Triple("nl", "Belanda", "Dutch"),
    Triple("pl", "Polandia", "Polish"), Triple("uk", "Ukraina", "Ukrainian"),
)

@Composable
private fun SectionTitle(text: String, first: Boolean = false) = GroupTitle(text)

/**
 * Full page for Online Translation (not a dialog). The bottom bar (progress/estimate/buttons) stays fixed, the content scrolls.
 * [origs]/[trs] = original texts and current entries; [pageIdx] = row index in the open editor page;
 * [onCommit] applies the result to the editor; [onBack] closes the page.
 */
@Composable
fun OnlineTranslatePage(
    origs: List<String>,
    trs: List<String>,
    pageIdx: List<Int>,
    editorGlossary: List<Pair<String, String>>,
    onCommit: (Map<Int, String>, Set<Int>) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalStr.current
    val o = s.ot
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val uri = LocalUriHandler.current
    val prefs = remember { OtPrefs(ctx) }
    val scope = rememberCoroutineScope()
    val idUi = s === Strings.ID

    // 0 = Lingva, 1 = API Key, 2 = Bilibili
    var mode by remember { mutableStateOf(when (prefs.get("mode", "lingva")) { "api" -> 1; "bilibili" -> 2; else -> 0 }) }
    var biliModel by remember { mutableStateOf(prefs.get("bili_model", BILI_MODELS.first())) }
    var provIdx by remember { mutableStateOf(PROVIDERS.indexOfFirst { it.p.id == prefs.get("provider", "google") }.coerceAtLeast(0)) }
    val info = PROVIDERS[provIdx]
    val prov = info.p
    val keys = remember { mutableStateMapOf<String, String>().apply { PROVIDERS.forEach { put(it.p.id, prefs.get("key_" + it.p.id, "")) } } }
    val models = remember {
        mutableStateMapOf<String, String>().apply { PROVIDERS.forEach { put(it.p.id, prefs.get("model_" + it.p.id, it.models.firstOrNull() ?: "")) } }
    }
    val modelLists = remember {
        mutableStateMapOf<String, List<String>>().apply {
            PROVIDERS.forEach { pi ->
                val saved = prefs.get("mlist_" + pi.p.id, "").lines().filter { it.isNotBlank() }
                put(pi.p.id, if (saved.isNotEmpty()) saved else pi.models)
            }
        }
    }
    var customUrl by remember { mutableStateOf(prefs.get("custom_url", "")) }
    var instances by remember { mutableStateOf(prefs.get("instances2", DEFAULT_INSTANCES)) }
    var src by remember { mutableStateOf(prefs.get("src", "auto")) }
    var tgt by remember { mutableStateOf(prefs.get("tgt", "id")) }
    var fillOrig by remember { mutableStateOf(prefs.get("fillorig", "1") == "1") }
    var scopeMode by remember { mutableStateOf(0) } // 0 empty, 1 this page, 2 all
    var showKey by remember { mutableStateOf(false) }
    var glossary by remember { mutableStateOf(TrTools.parseGlossary(prefs.get("gloss", ""))) }
    var ctxNote by remember { mutableStateOf(prefs.get("ctxnote", "")) }
    var style by remember { mutableStateOf(prefs.get("style", "natural")) }
    var useCtx by remember { mutableStateOf(prefs.get("usectx", "0") == "1") }
    var conc by remember { mutableStateOf(prefs.get("conc", "1").toIntOrNull()?.coerceIn(1, 4) ?: 1) }
    var useCache by remember { mutableStateOf(prefs.get("cache", "1") == "1") }
    var wrapOn by remember { mutableStateOf(prefs.get("wrapon", "0") == "1") }
    var wrapW by remember { mutableStateOf(prefs.get("wrapw", "45")) }
    var failedIdx by remember { mutableStateOf<List<Int>>(emptyList()) }
    var testing by remember { mutableStateOf(false) }
    var suggesting by remember { mutableStateOf(false) }
    val cacheFile = remember { File(ctx.cacheDir, "ot_cache.json") }
    val cacheRef = remember { arrayOfNulls<OtCache>(1) }
    var cacheN by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { cacheRef[0] = OtCache.load(cacheFile) }
        cacheN = cacheRef[0]?.size() ?: 0
    }
    var gSrc by remember { mutableStateOf("") }
    var gDst by remember { mutableStateOf("") }

    var job by remember { mutableStateOf<Job?>(null) }
    var loadingModels by remember { mutableStateOf(false) }
    var modelMsg by remember { mutableStateOf<String?>(null) }
    var prog by remember { mutableStateOf(0 to 0) }
    var status by remember { mutableStateOf<String?>(null) }
    val running = job != null || testing || suggesting

    val effective = when (mode) { 0 -> OtProvider.LINGVA; 2 -> OtProvider.BILIBILI; else -> prov }
    val targets: List<Int> = remember(scopeMode, origs, trs, pageIdx) {
        when (scopeMode) {
            0 -> origs.indices.filter { i -> trs[i].isBlank() && origs[i].isNotBlank() }
            1 -> pageIdx.filter { i -> origs[i].isNotBlank() }
            else -> origs.indices.filter { i -> origs[i].isNotBlank() }
        }
    }
    fun config() = OtConfig(
        effective, if (mode == 2) "" else keys[prov.id].orEmpty().trim(), if (mode == 2) biliModel else models[prov.id].orEmpty().trim(),
        src.ifBlank { "auto" }, tgt.ifBlank { "id" }, instances.lines().map { it.trim() }.filter { it.isNotEmpty() }, customUrl.trim(),
        note = ctxNote.trim(), style = style, useContext = useCtx, concurrency = conc,
        wrapWidth = if (wrapOn) (wrapW.toIntOrNull() ?: 0) else 0,
    )
    val est = remember(targets, effective, glossary) {
        OnlineTranslator(config(), glossary).estimate(targets.map { origs[it] })
    }

    fun saveAll() {
        prefs.put("mode", when (mode) { 1 -> "api"; 2 -> "bilibili"; else -> "lingva" }); prefs.put("bili_model", biliModel)
        prefs.put("provider", prov.id)
        PROVIDERS.forEach {
            prefs.put("key_" + it.p.id, keys[it.p.id].orEmpty()); prefs.put("model_" + it.p.id, models[it.p.id].orEmpty())
        }
        prefs.put("custom_url", customUrl); prefs.put("instances2", instances); prefs.put("src", src); prefs.put("tgt", tgt)
        prefs.put("fillorig", if (fillOrig) "1" else "0")
        prefs.put("gloss", TrTools.writeGlossary(glossary))
        prefs.put("ctxnote", ctxNote); prefs.put("style", style); prefs.put("usectx", if (useCtx) "1" else "0")
        prefs.put("conc", conc.toString()); prefs.put("cache", if (useCache) "1" else "0")
        prefs.put("wrapon", if (wrapOn) "1" else "0"); prefs.put("wrapw", wrapW)
    }

    fun validate(cfg: OtConfig): Boolean {
        if (mode == 1 && cfg.apiKey.isEmpty()) { status = o.needKey; return false }
        if (mode == 1 && prov == OtProvider.CUSTOM && !cfg.baseUrl.startsWith("http")) { status = o.customUrl + "?"; return false }
        if (mode == 1 && prov.isLlm && cfg.model.isEmpty()) { status = o.needModel; return false }
        return true
    }

    fun testConn() {
        val cfg = config()
        if (!validate(cfg)) return
        testing = true; status = o.testing
        scope.launch(Dispatchers.IO) {
            val r = try { Result.success(OnlineTranslator(cfg, emptyList()).test()) } catch (e: Exception) { Result.failure(e) }
            withContext(Dispatchers.Main) {
                testing = false
                status = r.getOrNull()?.let { o.testOk(it) } ?: o.testFail(r.exceptionOrNull()?.message ?: "?")
            }
        }
    }

    fun autoGlossary() {
        val cfg = config()
        if (!validate(cfg)) return
        val texts = origs.filter { it.isNotBlank() }
        suggesting = true; status = o.glossAutoBusy
        scope.launch(Dispatchers.IO) {
            val r = try { Result.success(OnlineTranslator(cfg, glossary).suggestGlossary(texts) { !isActive }) } catch (e: Exception) { Result.failure(e) }
            withContext(Dispatchers.Main) {
                suggesting = false
                val l = r.getOrNull()
                if (l == null) status = o.testFail(r.exceptionOrNull()?.message ?: "?")
                else {
                    val known = glossary.map { it.first.trim().lowercase() }.toSet()
                    val add = l.filter { it.first.trim().lowercase() !in known }
                    glossary = glossary + add
                    prefs.put("gloss", TrTools.writeGlossary(glossary))
                    status = if (add.isEmpty()) o.glossAutoNone else o.glossAutoDone(add.size)
                }
            }
        }
    }

    fun start(only: List<Int>? = null) {
        val cfg = config()
        if (!validate(cfg)) return
        val idxs = only ?: targets
        if (idxs.isEmpty()) { status = o.nothing; return }
        saveAll()
        status = null
        prog = 0 to idxs.size
        val items = idxs.map { it to origs[it] }
        val gl = glossary
        val fill = fillOrig
        val useC = useCache
        job = scope.launch(Dispatchers.IO) {
            val cache = if (useC) (cacheRef[0] ?: OtCache.load(cacheFile).also { cacheRef[0] = it }) else null
            val run = OnlineTranslator(cfg, gl).run(items, { !isActive }, cache, { i -> (1..3).map { i - it }.filter { it >= 0 }.reversed().map { origs[it] }.filter { it.isNotBlank() } }) { d, n -> prog = d to n }
            if (cache != null) { OtCache.save(cache, cacheFile) }
            val wasCancelled = !isActive
            withContext(NonCancellable + Dispatchers.Main) {
                val all = LinkedHashMap<Int, String>(run.results)
                if (fill) for (i in run.skippedIdx) all[i] = origs[i]
                if (all.isNotEmpty()) onCommit(all, run.results.keys.toSet())
                failedIdx = run.failed.keys.sorted()
                cacheN = cacheRef[0]?.size() ?: 0
                status = when {
                    run.aborted != null -> o.aborted(run.aborted, run.results.size)
                    wasCancelled -> o.cancelled
                    else -> o.done(run.results.size, run.failed.size, run.skipped, run.cached)
                }
                job = null
            }
        }
    }

    fun loadModels() {
        val cfg = config()
        if (cfg.apiKey.isEmpty()) { modelMsg = o.needKey; return }
        loadingModels = true; modelMsg = o.loadingModels
        val pid = prov.id
        scope.launch(Dispatchers.IO) {
            val r = try { Result.success(OnlineTranslator(cfg, emptyList()).listModels()) } catch (e: Exception) { Result.failure(e) }
            withContext(Dispatchers.Main) {
                loadingModels = false
                val l = r.getOrNull()
                if (l != null && l.isNotEmpty()) {
                    modelLists[pid] = l
                    prefs.put("mlist_$pid", l.joinToString("\n"))
                    if (models[pid].orEmpty() !in l) models[pid] = l.first()
                    modelMsg = o.modelsLoaded(l.size)
                } else modelMsg = o.modelsFailed(r.exceptionOrNull()?.message ?: "?")
            }
        }
    }

    val back: () -> Unit = {
        job?.cancel()
        saveAll()
        onBack()
    }
    androidx.activity.compose.BackHandler(onBack = back)

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).fillMaxWidth().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(o.title, back)
LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 

            // ---- Translation Service ----
            item { SectionTitle(o.secService, first = true) }
            item { RadioLine(o.optLingva, mode == 0, !running) { mode = 0 } }
            item { RadioLine(o.optBilibili, mode == 2, !running) { mode = 2 } }
            item { RadioLine(o.optApi, mode == 1, !running) { mode = 1 } }
            if (mode == 2) {
                item { DetailLink(o.biliNote, 12.sp, 17.sp) }
                item { AppDropdown(o.biliModel, biliModel, BILI_MODELS.map { it to it }, !running) { biliModel = it } }
                item { DetailLink(o.biliModelHint, 12.sp, 17.sp) }
                item { AppDropdown(o.parallel, conc.toString(), listOf(1, 2, 3, 4).map { it.toString() to it.toString() }, !running) { conc = it.toInt() } }
                item { Text(o.parallelHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp) }
            } else if (mode == 0) {
                item { DetailLink(o.lingvaNote, 12.sp, 17.sp) }
                item { AppTextField(value = instances, onValueChange = { instances = it }, label = o.instances, mono = true, multiline = true) }
            } else {
                item {
                    AppDropdown(o.provider, prov.id, PROVIDERS.map { it.p.id to it.label }, !running) { id ->
                        provIdx = PROVIDERS.indexOfFirst { it.p.id == id }.coerceAtLeast(0); modelMsg = null
                    }
                }
                item {
                    val ph = o.providerHint(prov.id)
                    if (ph.length >= DETAIL_MIN_CHARS) DetailLink(ph, 12.sp, 17.sp) else Text(ph, color = p.muted, fontSize = 12.sp)
                }
                val keyUrl = info.keyUrl
                if (keyUrl != null) item {
                    Text(
                        o.getKey(info.label), color = p.accent, fontSize = 14.sp, textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { try { uri.openUri(keyUrl) } catch (e: Exception) { status = e.message } },
                    )
                }
                if (prov == OtProvider.CUSTOM) {
                    item { AppTextField(value = customUrl, onValueChange = { customUrl = it.trim() }, label = o.customUrl, mono = true) }
                    item { Text(o.customUrlHint, color = p.muted, fontSize = 12.sp) }
                }
                item {
                    AppTextField(
                        value = keys[prov.id].orEmpty(), onValueChange = { keys[prov.id] = it.trim() }, label = o.apiKey, mono = true,
                        password = !showKey,
                        trailing = {
                            Text(
                                if (showKey) o.hide else o.show, color = p.accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable { showKey = !showKey }.padding(horizontal = 12.dp, vertical = 8.dp),
                            )
                        },
                    )
                }
                if (prov.isLlm) {
                    val list = modelLists[prov.id].orEmpty()
                    val cur = models[prov.id].orEmpty()
                    val opts = (if (cur.isNotEmpty() && cur !in list) listOf(cur) else emptyList()) + list
                    if (opts.isNotEmpty()) item { AppDropdown(o.model, cur, opts.map { it to it }, !running) { models[prov.id] = it } }
                    else item { Text(o.modelNone, color = p.muted, fontSize = 12.sp) }
                    if (prov == OtProvider.CUSTOM && opts.isEmpty()) item {
                        AppTextField(value = cur, onValueChange = { models[prov.id] = it.trim() }, label = o.modelManual, mono = true)
                    }
                    item { SecondaryButton(if (loadingModels) o.loadingModels else o.loadModels, !loadingModels && !running) { loadModels() } }
                    modelMsg?.let { m -> item { Text(m, color = p.muted, fontSize = 12.sp) } }
                }
                item { DetailLink(o.keyNote, 12.sp, 17.sp) }
                item { AppDropdown(o.parallel, conc.toString(), listOf(1, 2, 3, 4).map { it.toString() to it.toString() }, !running) { conc = it.toInt() } }
                item { Text(o.parallelHint, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp) }
            }
            if (mode == 0) item { SecondaryButton(o.refill, !running && instances != DEFAULT_INSTANCES) { instances = DEFAULT_INSTANCES } }
            item { SecondaryButton(if (testing) o.testing else o.testConn, !running) { testConn() } }
            item { CheckLine(o.fillOrig, fillOrig) { fillOrig = it } }

            // ---- Quality (LLM) ----
            item { SectionTitle(o.secQuality) }
            if (mode == 1 && prov.isLlm) {
                item {
                    AppDropdown(o.style, style, listOf("natural" to o.styleNatural, "casual" to o.styleCasual, "formal" to o.styleFormal, "literal" to o.styleLiteral), !running) { style = it }
                }
                item { AppTextField(value = ctxNote, onValueChange = { ctxNote = it.take(1500) }, label = o.noteLabel, multiline = true) }
                item { DetailLink(o.noteHint, 12.sp, 17.sp) }
                item { CheckLine(o.useContext, useCtx) { useCtx = it } }
                item { Text(o.useContextHint, color = p.muted, fontSize = 12.sp) }
            } else item { Text(o.llmOnly, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp) }

            // ---- Translation Language ----
            item { SectionTitle(o.secLang) }
            item {
                AppDropdown(o.srcLang, src, listOf("auto" to o.autoLang) + LANGS.map { it.first to (if (idUi) it.second else it.third) }, !running) { src = it }
            }
            item {
                AppDropdown(o.tgtLang, tgt, LANGS.map { it.first to (if (idUi) it.second else it.third) }, !running) { tgt = it }
            }

            // ---- Translation Rows ----
            item { SectionTitle(o.secRows) }
            item { SegmentedChips(listOf(o.scEmpty, o.scPage, o.scAll), scopeMode, 13.sp) { i -> if (!running) scopeMode = i } }
            item { Text(o.scopeNote, color = p.muted, fontSize = 12.sp) }

            // ---- Translation Results ----
            item { SectionTitle(o.secResult) }
            item { CheckLine(o.useCache, useCache) { useCache = it } }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(o.cacheInfo(cacheN), color = p.muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Box(Modifier.weight(1f)) {
                        SecondaryButton(o.clearCache, cacheN > 0 && !running) {
                            cacheRef[0]?.clear(); cacheRef[0]?.let { OtCache.save(it, cacheFile) }; cacheN = 0
                        }
                    }
                }
            }
            item { CheckLine(o.wrapOn, wrapOn) { wrapOn = it } }
            if (wrapOn) item {
                AppTextField(value = wrapW, onValueChange = { v -> wrapW = v.filter { c -> c.isDigit() }.take(3) }, label = o.wrapWidth, number = true)
            }

            // ---- Glossary ----
            item { SectionTitle(o.glossTitle) }
            item { DetailLink(o.glossHint, 12.sp, 17.sp) }
            item { AppTextField(value = gSrc, onValueChange = { gSrc = it }, label = o.glossSrc) }
            item { AppTextField(value = gDst, onValueChange = { gDst = it }, label = o.glossDst) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) {
                        SecondaryButton(o.glossAdd, gSrc.isNotBlank() && gDst.isNotBlank() && !running) {
                            glossary = glossary.filter { !it.first.trim().equals(gSrc.trim(), true) } + Pair(gSrc.trim(), gDst.trim())
                            gSrc = ""; gDst = ""
                            prefs.put("gloss", TrTools.writeGlossary(glossary))
                        }
                    }
                    Box(Modifier.weight(1f)) {
                        SecondaryButton(o.glossFromEditor, editorGlossary.isNotEmpty() && !running) {
                            glossary = TrTools.mergeGlossary(glossary, editorGlossary)
                            prefs.put("gloss", TrTools.writeGlossary(glossary))
                        }
                    }
                }
            }
            item { SecondaryButton(if (suggesting) o.glossAutoBusy else o.glossAuto, !running) { autoGlossary() } }
            item { Text(o.glossAutoNote, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp) }
            if (glossary.isEmpty()) item { Text(o.glossEmpty, color = p.muted, fontSize = 13.sp) }
            itemsIndexed(glossary) { gi, g ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(g.first + "  →  " + g.second, color = p.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        if (!running) {
                            glossary = glossary.filterIndexed { k, _ -> k != gi }
                            prefs.put("gloss", TrTools.writeGlossary(glossary))
                        }
                    }) { Icon(Icons.Filled.Close, contentDescription = null, tint = p.muted) }
                }
            }
            item { Text(o.closeWarn, color = p.muted, fontSize = 11.sp) }
        }
}
        // bottom bar: fixed
        Surface(color = p.background) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val st = status
                if (running) {
                    LinearProgressIndicator(if (prog.second > 0) prog.first.toFloat() / prog.second else 0f, Modifier.fillMaxWidth(), color = p.accent)
                    Text(o.progress(prog.first, prog.second), color = p.muted, fontSize = 13.sp)
                    SecondaryButton(o.cancel) { job?.cancel() }
                } else {
                    if (st != null) Text(st, color = p.accent, fontSize = 13.sp)
                    Text(o.estimate(est.lines, est.chars, est.requests), color = p.muted, fontSize = 12.sp)
                    if (failedIdx.isNotEmpty()) SecondaryButton(o.retry(failedIdx.size)) { start(failedIdx) }
                    PrimaryButton(o.run, targets.isNotEmpty() && !testing && !suggesting) { start() }
                }
            }
        }
    }
}

@Composable
private fun RadioLine(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().height(30.dp).clickable(enabled = enabled) { onClick() }, verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected, onClick = if (enabled) onClick else null,
            colors = RadioButtonDefaults.colors(selectedColor = p.accent, unselectedColor = p.muted),
        )
        Text(label, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
    }
}
