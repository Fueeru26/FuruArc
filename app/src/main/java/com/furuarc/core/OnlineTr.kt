package com.furuarc.core

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Online Translation provider. */
enum class OtProvider(val id: String) {
    LINGVA("lingva"), GOOGLE("google"), DEEPL("deepl"), GEMINI("gemini"), OPENAI("openai"),
    DEEPSEEK("deepseek"), CLAUDE("claude"), GROQ("groq"), OPENROUTER("openrouter"), CUSTOM("custom"), BILIBILI("bilibili");

    val isLlm get() = this != LINGVA && this != GOOGLE && this != DEEPL && this != BILIBILI
    /** Providers that use a model name (LLM + Bilibili). */
    val hasModel get() = isLlm || this == BILIBILI
    /** OpenAI-style API (chat/completions). */
    val isOpenAiLike get() = this == OPENAI || this == DEEPSEEK || this == GROQ || this == OPENROUTER || this == CUSTOM

    companion object { fun of(id: String?) = values().firstOrNull { it.id == id } ?: LINGVA }
}

class OtConfig(
    val provider: OtProvider,
    val apiKey: String,
    val model: String,
    val src: String,
    val tgt: String,
    val instances: List<String>,
    val baseUrl: String = "",
    /** Context notes for the LLM (genre, tone, etc.). */
    val note: String = "",
    /** natural | casual | formal | literal (LLM only). */
    val style: String = "natural",
    /** Send a few previous lines as context (LLM only). */
    val useContext: Boolean = false,
    /** Number of parallel requests (ignored for Lingva). */
    val concurrency: Int = 1,
    /** >0: wrap the result at this width when the original text is multi-line. */
    val wrapWidth: Int = 0,
)

class OtResp(val code: Int, val body: String, val retryAfter: Int = 0)

fun interface OtHttp {
    fun request(method: String, url: String, headers: Map<String, String>, body: String?): OtResp
}

/** Real HTTP via HttpURLConnection. */
object OtHttpImpl : OtHttp {
    override fun request(method: String, url: String, headers: Map<String, String>, body: String?): OtResp {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.requestMethod = method
            c.connectTimeout = 20_000
            c.readTimeout = 90_000
            c.setRequestProperty("User-Agent", "FuruArc")
            for ((k, v) in headers) c.setRequestProperty(k, v)
            if (body != null) {
                c.doOutput = true
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = c.responseCode
            val s = (if (code >= 400) c.errorStream else c.inputStream)
            val text = s?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
            return OtResp(code, text, c.getHeaderField("Retry-After")?.trim()?.toIntOrNull() ?: 0)
        } finally {
            c.disconnect()
        }
    }
}

class OtException(msg: String, val fatal: Boolean = false) : Exception(msg)

/** Protected text: control codes and glossary terms replaced with tokens, restored after translation. */
class OtMasked(val text: String, val tokens: List<String>, val escQuote: Boolean = false)

object OtProtect {
    val CODE_RE get() = CODE
    private val CODE = Regex(
        "\\\\[A-Za-z]{1,4}\\[[^\\]\\n]*\\]|\\\\[A-Za-z]|\\\\[{}$.|!><^\\\\]|%\\d+\\$[sdf]|%\\d*[sdif]|\\{[A-Za-z0-9_.]{1,30}\\}|<[^<>\\n]{1,40}>|\\[[A-Za-z0-9_=:#. -]{1,40}\\]",
    )
    private val BACK = Regex("(?i)Q\\s*X\\s*(\\d{1,4})\\s*X\\s*Q")

    fun token(i: Int) = "QX${i}XQ"

    private fun termRegex(terms: List<Pair<String, String>>): Regex? {
        val ts = terms.filter { it.first.isNotBlank() }.sortedByDescending { it.first.length }
        if (ts.isEmpty()) return null
        val alt = ts.joinToString("|") { (a, _) ->
            val q = Regex.escape(a.trim())
            val l = if (a.trim().first().isLetterOrDigit()) "(?:(?<![\\p{L}\\p{N}])|(?<=QX\\d{1,4}XQ))" else ""
            val r = if (a.trim().last().isLetterOrDigit()) "(?:(?![\\p{L}\\p{N}])|(?=QX\\d{1,4}XQ))" else ""
            l + q + r
        }
        return Regex(alt, RegexOption.IGNORE_CASE)
    }

    /** [terms] only for providers without a built-in glossary (Lingva/Google/DeepL); LLMs get the glossary via the prompt. */
    fun mask(text0: String, terms: List<Pair<String, String>>): OtMasked {
        // Ren'Py/script-formatted text: quotes are escaped (\"). Unescape first so the translator sees plain quotes, then re-apply afterwards.
        val esc = text0.contains("\\\"")
        val text = if (esc) text0.replace("\\\"", "\"") else text0
        val toks = ArrayList<String>()
        var t = CODE.replace(text) { m -> toks.add(m.value); token(toks.size - 1) }
        val tr = termRegex(terms)
        if (tr != null) {
            val map = HashMap<String, String>()
            for ((a, b) in terms) map.putIfAbsent(a.trim().lowercase(), b)
            t = tr.replace(t) { m ->
                val d = map[m.value.lowercase()] ?: m.value
                toks.add(d); token(toks.size - 1)
            }
        }
        return OtMasked(t, toks, esc)
    }

    /** null = a token was lost (result is not trusted). */
    private val RAW_QUOTE = Regex("(?<!\\\\)\"")

    fun restore(translated: String, m: OtMasked): String? {
        val r = restore0(translated, m) ?: return null
        return if (m.escQuote) RAW_QUOTE.replace(r) { "\\\"" } else r
    }

    private fun restore0(translated: String, m: OtMasked): String? {
        if (m.tokens.isEmpty()) return translated
        val seen = BooleanArray(m.tokens.size)
        val out = BACK.replace(translated) { r ->
            val i = r.groupValues[1].toInt()
            if (i in m.tokens.indices) { seen[i] = true; m.tokens[i] } else r.value
        }
        return if (seen.all { it }) out else null
    }

    /** Text without letters (only codes/numbers/symbols) does not need translating. */
    fun translatable(m: OtMasked): Boolean = BACK.replace(m.text, "").any { it.isLetter() }
}

/** Translation cache (original text + settings -> result) to avoid repeating requests. */
class OtCache(private val map: LinkedHashMap<String, String> = LinkedHashMap()) {
    @Synchronized fun get(k: String): String? = map[k]
    @Synchronized fun put(k: String, v: String) { map[k] = v }
    @Synchronized fun size(): Int = map.size
    @Synchronized fun clear() { map.clear() }

    @Synchronized fun toJson(max: Int = 30000): String {
        while (map.size > max) map.remove(map.keys.first())
        return Json.write(LinkedHashMap<String, Any?>(map))
    }

    companion object {
        fun key(sig: String, text: String): String {
            val d = java.security.MessageDigest.getInstance("SHA-1").digest((sig + "\u0001" + text).toByteArray(Charsets.UTF_8))
            return d.joinToString("") { String.format("%02x", it) }.take(24)
        }
        fun fromJson(s: String): OtCache {
            val c = OtCache()
            try {
                val o = Json.parse(s) as? Map<*, *> ?: return c
                for ((k, v) in o) if (k is String && v is String) c.put(k, v)
            } catch (e: RuntimeException) { }
            return c
        }
        fun load(f: java.io.File): OtCache = try { if (f.exists()) fromJson(f.readText(Charsets.UTF_8)) else OtCache() } catch (e: java.io.IOException) { OtCache() }
        fun save(c: OtCache, f: java.io.File) { try { f.writeText(c.toJson(), Charsets.UTF_8) } catch (e: java.io.IOException) { } }
    }
}

object OtWrap {
    /** If [orig] is multi-line and the result [t] is a single overlong line, split at spaces with width [width]. */
    fun wrap(orig: String, t: String, width: Int): String {
        if (width < 10 || !orig.contains('\n') || t.contains('\n') || t.length <= width) return t
        val nl = if (orig.contains("\r\n")) "\r\n" else "\n"
        val sb = StringBuilder(); var len = 0
        for (w in t.split(' ')) {
            if (len > 0 && len + 1 + w.length > width) { sb.append(nl); len = 0 } else if (len > 0) { sb.append(' '); len++ }
            sb.append(w); len += w.length
        }
        return sb.toString()
    }
}

class OtRun(val results: Map<Int, String>, val failed: Map<Int, String>, val skipped: Int, val aborted: String?, val skippedIdx: List<Int> = emptyList(), val cached: Int = 0)

class OtEstimate(val lines: Int, val chars: Int, val requests: Int)

/** Translator: splits into groups, protects codes, calls the provider, restores and checks the result. */
class OnlineTranslator(
    private val cfg: OtConfig,
    private val glossary: List<Pair<String, String>>,
    private val http: OtHttp = OtHttpImpl,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) },
) {
    private var lingvaIdx = 0

    private fun batchLimit() = when (cfg.provider) {
        OtProvider.LINGVA, OtProvider.BILIBILI -> 1
        OtProvider.GOOGLE, OtProvider.DEEPL -> 40
        else -> 15
    }
    private val fallbackGtx = true
    private val charLimit get() = when (cfg.provider) { OtProvider.LINGVA -> 1400; OtProvider.BILIBILI -> 3000; OtProvider.GOOGLE, OtProvider.DEEPL -> 12000; else -> 3500 }

    fun estimate(texts: List<String>): OtEstimate {
        var chars = 0; var n = 0
        val batches = batches(texts.mapIndexed { i, t -> i to OtProtect.mask(t, if (cfg.provider.isLlm) emptyList() else glossary) }.filter { OtProtect.translatable(it.second) })
        for (b in batches) { n += 1; for (x in b) chars += x.second.text.length }
        return OtEstimate(batches.sumOf { it.size }, chars, n)
    }

    private fun batches(items: List<Pair<Int, OtMasked>>): List<List<Pair<Int, OtMasked>>> {
        val out = ArrayList<List<Pair<Int, OtMasked>>>()
        var cur = ArrayList<Pair<Int, OtMasked>>(); var len = 0
        for (it in items) {
            if (cur.isNotEmpty() && (cur.size >= batchLimit() || len + it.second.text.length > charLimit)) { out.add(cur); cur = ArrayList(); len = 0 }
            cur.add(it); len += it.second.text.length
        }
        if (cur.isNotEmpty()) out.add(cur)
        return out
    }

    private fun glossHash(): String = glossary.joinToString("|") { it.first + "=" + it.second }.hashCode().toString()

    /** Settings signature that affects the result; used as the cache key. */
    fun signature(): String = listOf(cfg.provider.id, if (cfg.provider.hasModel) cfg.model.trim() else "", cfg.src, cfg.tgt,
        if (cfg.provider.isLlm) cfg.style + cfg.note.hashCode() else "", glossHash()).joinToString("/")

    private class RunCtx(
        val results: MutableMap<Int, String>, val failed: MutableMap<Int, String>,
        val orig: Map<Int, String>, val contextOf: ((Int) -> List<String>)?, val cache: OtCache?, val sig: String,
    )

    /**
     * Translate [items] (index -> original text). [isCancelled] is checked between requests; [onProgress] (done, total).
     * [cache] (optional) is used/filled; [contextOf] supplies previous lines for the LLM. Partial results are still returned
     * on a fatal error ([OtRun.aborted] is set).
     */
    fun run(
        items: List<Pair<Int, String>>, isCancelled: () -> Boolean,
        cache: OtCache? = null, contextOf: ((Int) -> List<String>)? = null, onProgress: (Int, Int) -> Unit,
    ): OtRun {
        val results = java.util.Collections.synchronizedMap(LinkedHashMap<Int, String>())
        val failed = java.util.Collections.synchronizedMap(LinkedHashMap<Int, String>())
        var skipped = 0
        var cachedN = 0
        val skippedIdx = ArrayList<Int>()
        val terms = if (cfg.provider.isLlm) emptyList() else glossary
        val sig = signature()
        val orig = HashMap<Int, String>()
        val work = ArrayList<Pair<Int, OtMasked>>()
        for ((i, t) in items) {
            val m = OtProtect.mask(t, terms)
            if (!OtProtect.translatable(m)) { skipped++; skippedIdx.add(i); continue }
            val hit = cache?.get(OtCache.key(sig, t))
            if (hit != null) { results[i] = OtWrap.wrap(t, hit, cfg.wrapWidth); cachedN++; continue }
            orig[i] = t
            work.add(i to m)
        }
        val total = work.size
        val rc = RunCtx(results, failed, orig, if (cfg.provider.isLlm && cfg.useContext) contextOf else null, cache, sig)
        val bs = batches(work)
        val abortedRef = java.util.concurrent.atomic.AtomicReference<String?>(null)
        val lock = Any()
        var done = 0
        var streak = 0
        fun handle(b: List<Pair<Int, OtMasked>>) {
            if (abortedRef.get() != null || isCancelled()) return
            try {
                translateBatch(b, rc)
                synchronized(lock) { streak = 0 }
            } catch (e: OtException) {
                val msg = e.message ?: "?"
                for (x in b) failed.putIfAbsent(x.first, msg)
                // fatal error (wrong key, etc.) or 3 consecutive groups failed: stop
                synchronized(lock) { streak++; if (e.fatal || streak >= 3) abortedRef.compareAndSet(null, msg) }
            }
            synchronized(lock) { done += b.size; onProgress(done, total) }
        }
        val conc = if (cfg.provider == OtProvider.LINGVA) 1 else cfg.concurrency.coerceIn(1, 4)
        if (conc <= 1 || bs.size <= 1) {
            for (b in bs) { if (abortedRef.get() != null || isCancelled()) break; handle(b) }
        } else {
            val pool = java.util.concurrent.Executors.newFixedThreadPool(conc)
            try {
                bs.map { b -> pool.submit { handle(b) } }.forEach { it.get() }
            } finally { pool.shutdown() }
        }
        return OtRun(LinkedHashMap(results), LinkedHashMap(failed), skipped, abortedRef.get(), skippedIdx, cachedN)
    }

    private fun translateBatch(b: List<Pair<Int, OtMasked>>, rc: RunCtx) {
        val outs: List<String> = try {
            callProvider(b.map { it.second.text }, rc.contextOf?.invoke(b[0].first) ?: emptyList())
        } catch (e: OtMismatch) {
            if (b.size > 1) {
                val h = b.size / 2
                translateBatch(b.subList(0, h), rc); translateBatch(b.subList(h, b.size), rc)
                return
            }
            throw OtException(e.message ?: "?")
        }
        for ((k, x) in b.withIndex()) {
            val r = OtProtect.restore(outs[k], x.second)
            if (r == null) rc.failed[x.first] = "kode"
            else {
                val o = rc.orig[x.first] ?: ""
                rc.cache?.put(OtCache.key(rc.sig, o), r)
                rc.results[x.first] = OtWrap.wrap(o, r, cfg.wrapWidth)
            }
        }
    }

    /** Connection test: translate one word. Returns the result or throws [OtException]. */
    fun test(): String {
        try { return callProvider(listOf("Hello"), emptyList()).firstOrNull()?.trim().orEmpty() }
        catch (e: OtMismatch) { throw OtException("bad response format") }
    }

    /** Glossary candidate terms: Latin names capitalized mid-sentence, katakana runs, and Japanese names + honorifics. */
    fun candidates(texts: List<String>, minCount: Int = 3, max: Int = 60): List<String> {
        val cnt = HashMap<String, Int>()
        val latin = Regex("\\p{Lu}\\p{Ll}{2,}")
        val kata = Regex("[\u30A1-\u30FA\u30FC]{3,}")
        val hon = Regex("([\u4E00-\u9FFF]{2,3})(?:\u3055\u3093|\u304F\u3093|\u3061\u3083\u3093|\u5148\u8F29|\u5148\u751F|\u69D8|\u6BBF|\u541B)")
        for (raw in texts) {
            val t = OtProtect.CODE_RE.replace(raw, " ")
            for (m in latin.findAll(t)) {
                val before = t.substring(0, m.range.first).trimEnd()
                if (before.isEmpty() || before.last() in ".!?\u2026\"\u201C\u300C:") continue
                if (m.range.first > 0 && (t[m.range.first - 1].isLetterOrDigit())) continue
                cnt.merge(m.value, 1, Int::plus)
            }
            for (m in kata.findAll(t)) cnt.merge(m.value, 1, Int::plus)
            for (m in hon.findAll(t)) cnt.merge(m.groupValues[1], 1, Int::plus)
        }
        return cnt.filter { it.value >= minCount }.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }).take(max).map { it.key }
    }

    /** Build glossary suggestions: find candidate terms in [texts] then translate them via the provider. Pairs whose result equals the source are dropped. */
    fun suggestGlossary(texts: List<String>, isCancelled: () -> Boolean = { false }): List<Pair<String, String>> {
        val cands = candidates(texts).let { if (cfg.provider == OtProvider.LINGVA) it.take(40) else it }
        val out = ArrayList<Pair<String, String>>()
        val step = if (cfg.provider == OtProvider.LINGVA) 1 else 25
        for (ch in cands.chunked(step)) {
            if (isCancelled()) break
            val r = try { callProvider(ch, emptyList()) } catch (e: OtMismatch) { continue }
            for ((i, t) in r.withIndex()) {
                val tt = t.trim()
                if (tt.isNotEmpty() && !tt.equals(ch[i], true)) out.add(ch[i] to tt)
            }
        }
        return out
    }

    private class OtMismatch(msg: String) : Exception(msg)

    // ---------- HTTP with retries ----------

    private fun send(method: String, url: String, headers: Map<String, String>, body: String?): OtResp {
        var last = ""
        for (attempt in 0 until 4) {
            try {
                val r = http.request(method, url, headers, body)
                if (r.code in 200..299) return r
                if (r.code == 401 || r.code == 403) throw OtException("HTTP ${r.code}: " + clip(r.body), true)
                if (r.code == 429 || r.code >= 500) {
                    last = "HTTP ${r.code}: " + clip(r.body)
                    sleeper(maxOf(r.retryAfter * 1000L, 1000L shl attempt).coerceAtMost(20_000))
                    continue
                }
                throw OtException("HTTP ${r.code}: " + clip(r.body), true)
            } catch (e: IOException) {
                last = e.message ?: e.javaClass.simpleName
                sleeper(1000L shl attempt)
            }
        }
        throw OtException(last.ifEmpty { "network" })
    }

    private fun clip(s: String) = s.replace(Regex("\\s+"), " ").take(200).let { if (cfg.apiKey.length >= 8) it.replace(cfg.apiKey, "***") else it }

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8").replace("+", "%20")

    // ---------- providers ----------

    private fun callProvider(texts: List<String>, ctx: List<String>): List<String> = when (cfg.provider) {
        OtProvider.LINGVA -> listOf(lingva(texts[0]))
        OtProvider.GOOGLE -> google(texts)
        OtProvider.DEEPL -> deepl(texts)
        OtProvider.BILIBILI -> listOf(bilibili(texts[0]))
        else -> llm(texts, ctx)
    }

    /**
     * Bilibili Index-Translate (free, no API key; OpenAI chat/completions compatible at index-translate.bilibili.com).
     * One text per request with a plain translation instruction; thinking mode is turned off.
     */
    private fun bilibili(text: String): String {
        val auto = cfg.src.isBlank() || cfg.src == "auto"
        val prompt = "Translate the following text " + (if (auto) "" else "from ${langName(cfg.src)} ") + "into ${langName(cfg.tgt)}. " +
            "Output only the translation, without any explanation or extra text. Keep line breaks and any symbols or placeholders unchanged.\n\n" + text
        val body = LinkedHashMap<String, Any?>()
        body["model"] = cfg.model.trim().ifEmpty { "Index-Translate-9B" }
        body["messages"] = listOf(mapOf("role" to "user", "content" to prompt))
        body["temperature"] = JNum("0")
        body["stream"] = false
        body["chat_template_kwargs"] = mapOf("enable_thinking" to false)
        val r = send("POST", "https://index-translate.bilibili.com/v1/chat/completions",
            mapOf("Content-Type" to "application/json", "User-Agent" to "Index-Translate-Client/1.0"), Json.write(body))
        val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
        val ch = (o["choices"] as? List<*>)?.firstOrNull() as? Map<*, *> ?: throw OtException("empty response")
        val c = ((ch["message"] as? Map<*, *>)?.get("content") as? String) ?: throw OtException("empty response")
        val out = c.replace(Regex("(?s)<think>.*?</think>"), "").trim()
        if (out.isEmpty()) throw OtException("empty response")
        return out
    }

    private fun lingvaLang(c: String) = when (c) { "zh-CN" -> "zh"; "zh-TW" -> "zh_HANT"; else -> c }

    private fun lingva(text: String): String {
        val list = cfg.instances.map { it.trim().trimEnd('/') }.filter { it.startsWith("http") }
        val errs = ArrayList<String>()
        val src = lingvaLang(cfg.src.ifBlank { "auto" })
        val tgt = lingvaLang(cfg.tgt)
        for (k in list.indices) {
            val base = list[(lingvaIdx + k) % list.size]
            val host = base.removePrefix("https://").removePrefix("http://")
            try {
                val r = http.request("GET", base + "/api/v1/" + enc(src) + "/" + enc(tgt) + "/" + enc(text), emptyMap(), null)
                if (r.code in 200..299) {
                    val o = try { Json.parse(r.body) as? Map<*, *> } catch (e: RuntimeException) { null }
                    val t = o?.get("translation") as? String
                    if (t != null) { lingvaIdx = (lingvaIdx + k) % list.size; sleeper(120); return t }
                    errs.add("$host: bad response")
                } else errs.add("$host: HTTP ${r.code}")
            } catch (e: IOException) {
                errs.add("$host: " + (e.message ?: e.javaClass.simpleName).take(60))
            }
        }
        // last fallback: the free Google Translate endpoint (unofficial)
        if (fallbackGtx) {
            try {
                val url = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&sl=" + enc(googleLang(cfg.src.ifBlank { "auto" })) +
                    "&tl=" + enc(googleLang(cfg.tgt)) + "&q=" + enc(text)
                val r = http.request("GET", url, emptyMap(), null)
                if (r.code in 200..299) {
                    val arr = (Json.parse(r.body) as? List<*>)?.firstOrNull() as? List<*>
                    if (arr != null) {
                        val sb = StringBuilder()
                        for (seg in arr) (((seg as? List<*>)?.firstOrNull()) as? String)?.let { sb.append(it) }
                        if (sb.isNotEmpty()) { sleeper(150); return sb.toString() }
                    }
                    errs.add("google: bad response")
                } else errs.add("google: HTTP ${r.code}")
            } catch (e: IOException) {
                errs.add("google: " + (e.message ?: e.javaClass.simpleName).take(60))
            } catch (e: RuntimeException) {
                errs.add("google: " + (e.message ?: "?").take(60))
            }
        }
        throw OtException(if (errs.isEmpty()) "Lingva: no server" else errs.joinToString("; "))
    }

    private fun googleLang(c: String) = c

    private fun google(texts: List<String>): List<String> {
        val body = LinkedHashMap<String, Any?>()
        body["q"] = texts; body["target"] = googleLang(cfg.tgt); body["format"] = "text"
        if (cfg.src.isNotBlank() && cfg.src != "auto") body["source"] = googleLang(cfg.src)
        val r = send("POST", "https://translation.googleapis.com/language/translate/v2", mapOf("Content-Type" to "application/json; charset=utf-8", "X-goog-api-key" to cfg.apiKey), Json.write(body))
        val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
        val tl = ((o["data"] as? Map<*, *>)?.get("translations") as? List<*>) ?: throw OtException("bad response")
        if (tl.size != texts.size) throw OtMismatch("count")
        return tl.map { htmlDecode(((it as? Map<*, *>)?.get("translatedText") as? String) ?: "") }
    }

    private fun htmlDecode(s: String) = if (!s.contains('&')) s else s.replace("&quot;", "\"").replace("&#39;", "'").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")

    private fun deeplLang(c: String, target: Boolean): String {
        val u = c.trim().uppercase().replace('_', '-')
        if (!target) return u.substringBefore('-')
        return when (u) { "EN" -> "EN-US"; "PT" -> "PT-BR"; "ZH-CN" -> "ZH-HANS"; "ZH-TW" -> "ZH-HANT"; else -> u }
    }

    private fun deepl(texts: List<String>): List<String> {
        val host = if (cfg.apiKey.trim().endsWith(":fx")) "https://api-free.deepl.com" else "https://api.deepl.com"
        val body = LinkedHashMap<String, Any?>()
        body["text"] = texts; body["target_lang"] = deeplLang(cfg.tgt, true)
        if (cfg.src.isNotBlank() && cfg.src != "auto") body["source_lang"] = deeplLang(cfg.src, false)
        val r = send("POST", "$host/v2/translate", mapOf("Content-Type" to "application/json", "Authorization" to "DeepL-Auth-Key " + cfg.apiKey.trim()), Json.write(body))
        val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
        val tl = o["translations"] as? List<*> ?: throw OtException("bad response")
        if (tl.size != texts.size) throw OtMismatch("count")
        return tl.map { ((it as? Map<*, *>)?.get("text") as? String) ?: "" }
    }

    private val LANG_NAMES = mapOf(
        "en" to "English", "id" to "Indonesian", "ja" to "Japanese", "ko" to "Korean", "zh" to "Chinese", "zh-cn" to "Simplified Chinese",
        "zh-tw" to "Traditional Chinese", "ms" to "Malay", "es" to "Spanish", "fr" to "French", "de" to "German", "ru" to "Russian",
        "pt" to "Portuguese", "it" to "Italian", "th" to "Thai", "vi" to "Vietnamese", "ar" to "Arabic", "tr" to "Turkish",
        "hi" to "Hindi", "nl" to "Dutch", "pl" to "Polish", "uk" to "Ukrainian",
    )

    private fun langName(c: String): String = LANG_NAMES[c.trim().lowercase().replace('_', '-')] ?: c

    fun systemPrompt(): String {
        val src = if (cfg.src.isBlank() || cfg.src == "auto") "the source language (detect it)" else langName(cfg.src)
        val sb = StringBuilder()
        sb.append("You are a professional video game localization translator. Translate each string from ").append(src)
            .append(" into ").append(langName(cfg.tgt)).append(".\n")
            .append("Rules:\n- Tokens that look like QX0XQ, QX1XQ are placeholders: copy them exactly, never translate, remove, split or add them.\n")
            .append("- Keep line breaks and leading/trailing spaces. Keep names and honorific style consistent. Translate naturally for game dialogue.\n")
            .append("- Do not add notes. Return only JSON: {\"translations\": [...]} with exactly the same number of strings in the same order.\n")
        sb.append(when (cfg.style) {
            "casual" -> "- Style: casual, colloquial everyday speech as spoken by the characters.\n"
            "formal" -> "- Style: polite and formal.\n"
            "literal" -> "- Style: stay close to the source wording (literal), but still grammatical.\n"
            else -> "- Style: natural and fluent, as a professional localizer would write.\n"
        })
        if (cfg.useContext) sb.append("- The input may be an object {\"context_before\": [...], \"strings\": [...]}. context_before holds the preceding lines for context only: never translate or return them; translate only \"strings\".\n")
        if (cfg.note.isNotBlank()) sb.append("Notes about this game (context, tone, characters):\n").append(cfg.note.trim().take(1500)).append('\n')
        val g = glossary.filter { it.first.isNotBlank() && it.second.isNotBlank() }
        if (g.isNotEmpty()) {
            sb.append("Glossary (use these translations exactly whenever the source term appears):\n")
            for ((a, b) in g.take(200)) sb.append("- ").append(a.trim()).append(" => ").append(b.trim()).append('\n')
        }
        return sb.toString()
    }

    private fun parseLlmList(content: String): List<String>? {
        var c = content.trim()
        if (c.startsWith("```")) c = c.removePrefix("```json").removePrefix("```JSON").removePrefix("```").removeSuffix("```").trim()
        val v = try { Json.parse(c) } catch (e: Exception) {
            // the model added text outside the JSON: take from the first { to the last }
            val a = c.indexOf('{'); val b = c.lastIndexOf('}')
            if (a < 0 || b <= a) return null
            try { Json.parse(c.substring(a, b + 1)) } catch (e2: Exception) { return null }
        }
        val l = (v as? Map<*, *>)?.get("translations") ?: v
        return (l as? List<*>)?.map { (it as? String) ?: it?.toString() ?: "" }
    }

    private fun openAiBase(): String = when (cfg.provider) {
        OtProvider.OPENAI -> "https://api.openai.com/v1"
        OtProvider.DEEPSEEK -> "https://api.deepseek.com"
        OtProvider.GROQ -> "https://api.groq.com/openai/v1"
        OtProvider.OPENROUTER -> "https://openrouter.ai/api/v1"
        else -> cfg.baseUrl.trim().trimEnd('/').removeSuffix("/chat/completions")
    }

    private fun llm(texts: List<String>, ctx: List<String>): List<String> {
        val userJson = if (cfg.useContext && ctx.isNotEmpty())
            Json.write(linkedMapOf<String, Any?>("context_before" to ctx.map { it.take(240) }, "strings" to texts))
        else Json.write(texts)
        val content: String
        when {
            cfg.provider == OtProvider.GEMINI -> {
                val body = LinkedHashMap<String, Any?>()
                body["systemInstruction"] = mapOf("parts" to listOf(mapOf("text" to systemPrompt())))
                body["contents"] = listOf(mapOf("role" to "user", "parts" to listOf(mapOf("text" to userJson))))
                body["generationConfig"] = mapOf("temperature" to JNum("0.3"), "responseMimeType" to "application/json")
                val r = send("POST", "https://generativelanguage.googleapis.com/v1beta/models/" + enc(cfg.model.trim()) + ":generateContent",
                    mapOf("Content-Type" to "application/json", "x-goog-api-key" to cfg.apiKey.trim()), Json.write(body))
                val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
                val cand = (o["candidates"] as? List<*>)?.firstOrNull() as? Map<*, *> ?: throw OtException("Gemini: empty response (blocked?)")
                val parts = (cand["content"] as? Map<*, *>)?.get("parts") as? List<*> ?: throw OtException("Gemini: empty response")
                content = parts.joinToString("") { ((it as? Map<*, *>)?.get("text") as? String) ?: "" }
            }
            cfg.provider == OtProvider.CLAUDE -> {
                val body = LinkedHashMap<String, Any?>()
                body["model"] = cfg.model.trim(); body["max_tokens"] = JNum("8192"); body["temperature"] = JNum("0.3")
                body["system"] = systemPrompt()
                body["messages"] = listOf(mapOf("role" to "user", "content" to userJson))
                val r = send("POST", "https://api.anthropic.com/v1/messages",
                    mapOf("Content-Type" to "application/json", "x-api-key" to cfg.apiKey.trim(), "anthropic-version" to "2023-06-01"), Json.write(body))
                val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
                val parts = o["content"] as? List<*> ?: throw OtException("Claude: empty response")
                content = parts.joinToString("") { ((it as? Map<*, *>)?.get("text") as? String) ?: "" }
            }
            else -> {
                val base = openAiBase()
                if (!base.startsWith("http")) throw OtException("URL?", true)
                fun call(json: Boolean): OtResp {
                    val body = LinkedHashMap<String, Any?>()
                    body["model"] = cfg.model.trim()
                    body["messages"] = listOf(mapOf("role" to "system", "content" to systemPrompt()), mapOf("role" to "user", "content" to userJson))
                    if (json) body["response_format"] = mapOf("type" to "json_object")
                    return send("POST", "$base/chat/completions", mapOf("Content-Type" to "application/json", "Authorization" to "Bearer " + cfg.apiKey.trim()), Json.write(body))
                }
                val r = try { call(true) } catch (e: OtException) {
                    // some models/providers do not support JSON mode: retry without response_format
                    if (e.message?.startsWith("HTTP 400") == true) call(false) else throw e
                }
                val o = Json.parse(r.body) as? Map<*, *> ?: throw OtException("bad response")
                val ch = (o["choices"] as? List<*>)?.firstOrNull() as? Map<*, *> ?: throw OtException("empty response")
                content = ((ch["message"] as? Map<*, *>)?.get("content") as? String) ?: throw OtException("empty response")
            }
        }
        val l = parseLlmList(content) ?: throw OtMismatch("parse")
        if (l.size != texts.size) throw OtMismatch("count")
        return l
    }

    /** List of models available for this account/URL (requires network). */
    fun listModels(): List<String> {
        val key = cfg.apiKey.trim()
        val out = ArrayList<String>()
        when {
            cfg.provider == OtProvider.GEMINI -> {
                val r = send("GET", "https://generativelanguage.googleapis.com/v1beta/models?pageSize=200", mapOf("x-goog-api-key" to key), null)
                val l = (Json.parse(r.body) as? Map<*, *>)?.get("models") as? List<*> ?: throw OtException("bad response")
                for (m in l) {
                    val mm = m as? Map<*, *> ?: continue
                    val methods = mm["supportedGenerationMethods"] as? List<*>
                    val name = (mm["name"] as? String)?.removePrefix("models/") ?: continue
                    if (methods == null || methods.contains("generateContent")) if (name.startsWith("gemini")) out.add(name)
                }
            }
            cfg.provider == OtProvider.CLAUDE -> {
                val r = send("GET", "https://api.anthropic.com/v1/models?limit=100", mapOf("x-api-key" to key, "anthropic-version" to "2023-06-01"), null)
                val l = (Json.parse(r.body) as? Map<*, *>)?.get("data") as? List<*> ?: throw OtException("bad response")
                for (m in l) ((m as? Map<*, *>)?.get("id") as? String)?.let { out.add(it) }
            }
            cfg.provider.isOpenAiLike -> {
                val base = openAiBase()
                if (!base.startsWith("http")) throw OtException("URL?", true)
                val r = send("GET", "$base/models", mapOf("Authorization" to "Bearer $key"), null)
                val l = (Json.parse(r.body) as? Map<*, *>)?.get("data") as? List<*> ?: throw OtException("bad response")
                val bad = listOf("embed", "whisper", "tts", "dall-e", "image", "audio", "realtime", "moderation", "transcribe", "guard", "rerank")
                for (m in l) {
                    val id = (m as? Map<*, *>)?.get("id") as? String ?: continue
                    if (cfg.provider == OtProvider.OPENAI && !(id.startsWith("gpt") || id.startsWith("o1") || id.startsWith("o3") || id.startsWith("o4") || id.startsWith("chatgpt"))) continue
                    if (bad.any { id.contains(it, true) }) continue
                    out.add(id)
                }
            }
            else -> throw OtException("no model list")
        }
        return out.distinct().sorted()
    }
}
