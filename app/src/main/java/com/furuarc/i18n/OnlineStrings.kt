package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks jendela Terjemahan Online (Editor Terjemahan). */
class OnlineStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    val title get() = t("Terjemahan Online", "Online Translation")
    val optLingva get() = "Lingva Translate (Free)"
    val secService get() = t("Layanan Penerjemahan", "Translation Service")
    val secLang get() = t("Terjemahan Bahasa", "Translation Languages")
    val secRows get() = t("Baris Penerjemahan", "Translation Rows")
    val optApi get() = t("Gunakan API Key", "Use API Key")
    val lingvaNote get() = t(
        "Tanpa API key, memakai server Lingva publik (hasil setara Google Translate). Bila semua server gagal, otomatis dicoba endpoint gratis Google Translate (tidak resmi, bisa dibatasi). Teks game dikirim ke server pihak ketiga; server publik bisa lambat atau mati.",
        "No API key needed; uses public Lingva servers (Google Translate quality). If every server fails, the free Google Translate endpoint (unofficial, may be rate limited) is tried. Game text is sent to a third-party server; public servers can be slow or down.",
    )
    val optBilibili get() = "Bilibili Translate (Free)"
    val biliNote get() = t(
        "Gratis, tanpa API key. Dijalankan oleh Bilibili (Index-Translate) yang tidak menerbitkan batas pemakaian. Teks yang diambil dikirim ke Bilibili untuk diterjemahkan.",
        "Free, no API key needed. Run by Bilibili (Index-Translate), which publishes no usage limits. Captured text is sent to Bilibili for translation.",
    )
    val biliModel get() = t("Model Bilibili", "Bilibili model")
    val biliModelHint get() = t(
        "9B = seimbang (disarankan), 35B-A3B = paling akurat tetapi masih pratinjau, 2B = paling cepat, Nailong = untuk teks panjang. Glosarium memakai token pengganti.",
        "9B = balanced (recommended), 35B-A3B = most accurate but still a preview, 2B = fastest, Nailong = long texts. The glossary uses placeholders.",
    )
    val refill get() = t("Isi ulang", "Refill")
    val instances get() = t("Server Lingva (satu per baris)", "Lingva servers (one per line)")
    val provider get() = t("Penyedia", "Provider")
    val apiKey get() = t("API Key", "API Key")
    fun getKey(name: String) = tt("Dapatkan $name API Key", "Get {0} API Key", name)
    val customUrl get() = t("URL Penyedia", "Provider URL")
    val customUrlHint get() = t("Alamat dasar API gaya OpenAI, mis. https://host/v1 (tanpa /chat/completions).", "Base URL of an OpenAI-style API, e.g. https://host/v1 (without /chat/completions).")
    val loadModels get() = t("Muat daftar model", "Load model list")
    val loadingModels get() = t("Memuat model…", "Loading models…")
    fun modelsLoaded(n: Int) = tt("$n model dimuat.", "{0} model(s) loaded.", n)
    fun modelsFailed(m: String) = tt("Gagal memuat model: $m", "Could not load models: {0}", m)
    val modelNone get() = t("Isi API Key lalu tekan Muat daftar model.", "Enter the API key, then press Load model list.")
    val modelManual get() = t("Model (manual, jika daftar tidak tersedia)", "Model (manual, if no list is available)")
    val fillOrig get() = t("Isi otomatis bagian yang tidak diterjemahkan dengan Teks Asli", "Auto-fill parts that are not translated with the Original Text")
    val autoLang get() = t("Otomatis (Auto)", "Automatic (Auto)")
    val show get() = t("Lihat", "Show")
    val hide get() = t("Sembunyi", "Hide")
    val model get() = t("Model", "Model")
    val modelHint get() = t("Isi nama model yang dipakai akunmu.", "Enter a model name your account can use.")
    val keyNote get() = t(
        "Kunci hanya disimpan di perangkat ini, tidak ikut cadangan, dan tidak masuk log. Teks yang diterjemahkan dikirim ke penyedia yang dipilih.",
        "The key is stored on this device only, excluded from backups and never logged. Text to translate is sent to the selected provider.",
    )
    fun providerHint(p: String) = when (p) {
        "google" -> t("Google Cloud Translation API (v2). Glosarium memakai token pengganti.", "Google Cloud Translation API (v2). The glossary uses placeholders.")
        "openrouter" -> t("OpenRouter: satu kunci untuk banyak model (nama model berformat penyedia/model; ada model berakhiran :free). Glosarium dikirim lewat prompt.", "OpenRouter: one key for many models (model names look like provider/model; some end with :free). The glossary is sent in the prompt.")
        "deepl" -> t("DeepL API (akhiran :fx = paket Free). Glosarium memakai token pengganti.", "DeepL API (suffix :fx = Free plan). The glossary uses placeholders.")
        else -> t("Model bahasa (LLM): glosarium dikirim lewat prompt.", "Language model (LLM): the glossary is sent in the prompt.")
    }
    val srcLang get() = t("Bahasa Sumber", "Source Language")
    val tgtLang get() = t("Bahasa Tujuan", "Target Language")
    val langHint get() = ""
    val scope get() = t("Yang diterjemahkan", "Translate")
    val scEmpty get() = t("Kolom kosong", "Empty fields")
    val scPage get() = t("Halaman ini", "This page")
    val scAll get() = t("Semua baris", "All rows")
    val scopeNote get() = t("Semua baris dan Halaman ini menimpa isian yang sudah ada (bisa dibatalkan dengan Undo).", "All rows and This page overwrite existing entries (Undo can revert).")
    val glossTitle get() = t("Glosarium Terjemahan Online", "Online Translation Glossary")
    val glossHint get() = t(
        "Khusus Terjemahan Online, terpisah dari Glosarium editor. Gemini/OpenAI menerimanya lewat prompt; Lingva, Google, dan DeepL memakai pengganti sementara (cocok untuk nama/istilah, kurang pas untuk kata umum seperti \"I\").",
        "For Online Translation only, separate from the editor glossary. Gemini/OpenAI receive it in the prompt; Lingva, Google and DeepL use placeholders (good for names/terms, less precise for common words like \"I\").",
    )
    val glossSrc get() = t("Teks asli", "Source text")
    val glossDst get() = t("Terjemahan", "Translation")
    val glossAdd get() = t("Tambah", "Add")
    val glossFromEditor get() = t("Salin dari Glosarium editor", "Copy from editor glossary")
    val glossEmpty get() = t("Belum ada istilah.", "No terms yet.")
    fun estimate(lines: Int, chars: Int, req: Int) = tt("$lines baris · ±$chars karakter · $req permintaan", "{0} rows · ~{1} characters · {2} request(s)", lines, chars, req)
    val run get() = t("Terjemahkan", "Translate")
    val cancel get() = t("Batalkan", "Cancel")
    fun progress(d: Int, n: Int) = tt("Menerjemahkan $d / $n…", "Translating {0} / {1}…", d, n)
    fun done(ok: Int, codes: Int, skipped: Int, cached: Int = 0) = t(
        "Selesai: $ok baris diterjemahkan" + (if (cached > 0) " ($cached dari cache)" else "") + (if (codes > 0) ", $codes gagal (kode kontrol hilang/galat), bisa dicoba lagi" else "") + (if (skipped > 0) ", $skipped tanpa teks dilewati" else "") + ".",
        "Done: $ok row(s) translated" + (if (cached > 0) " ($cached from cache)" else "") + (if (codes > 0) ", $codes failed (control codes lost/error), you can retry" else "") + (if (skipped > 0) ", $skipped without text skipped" else "") + ".",
    )
    // ---- ide tambahan ----
    val secQuality get() = t("Kualitas Terjemahan (LLM)", "Translation Quality (LLM)")
    val style get() = t("Gaya bahasa", "Language style")
    val styleNatural get() = t("Natural", "Natural")
    val styleCasual get() = t("Santai", "Casual")
    val styleFormal get() = t("Formal", "Formal")
    val styleLiteral get() = t("Harfiah (dekat dengan teks asli)", "Literal (close to the source)")
    val noteLabel get() = t("Catatan konteks (opsional)", "Context notes (optional)")
    val noteHint get() = t("Mis. genre, suasana, hubungan antar tokoh, cara memanggil nama. Dikirim ke model pada setiap permintaan.", "E.g. genre, mood, relationships, how names are addressed. Sent to the model with every request.")
    val useContext get() = t("Sertakan 3 baris sebelumnya sebagai konteks", "Include the 3 previous rows as context")
    val useContextHint get() = t("Membantu pronomina dan nada percakapan; menambah jumlah token.", "Helps pronouns and conversation tone; uses more tokens.")
    val llmOnly get() = t("Bagian ini hanya berlaku untuk penyedia LLM (Gemini, OpenAI, DeepSeek, Claude, Groq, Custom).", "This section only applies to LLM providers (Gemini, OpenAI, DeepSeek, Claude, Groq, Custom).")
    val testConn get() = t("Uji koneksi", "Test connection")
    val testing get() = t("Menguji…", "Testing…")
    fun testOk(r: String) = tt("Koneksi berhasil. \"Hello\" → \"$r\"", "Connection OK. \"Hello\" → \"{0}\"", r)
    fun testFail(m: String) = tt("Koneksi gagal: $m", "Connection failed: {0}", m)
    val parallel get() = t("Permintaan paralel", "Parallel requests")
    val parallelHint get() = t("Lebih cepat, tetapi lebih mudah terkena batas kecepatan (HTTP 429). Tidak berlaku untuk Lingva.", "Faster, but more likely to hit rate limits (HTTP 429). Not used for Lingva.")
    val secResult get() = t("Hasil Terjemahan", "Translation Results")
    val useCache get() = t("Pakai cache (lewati teks yang sudah pernah diterjemahkan dengan pengaturan sama)", "Use cache (skip text already translated with the same settings)")
    fun cacheInfo(n: Int) = tt("Cache: $n entri", "Cache: {0} entries", n)
    val clearCache get() = t("Hapus cache", "Clear cache")
    val wrapOn get() = t("Atur baris otomatis bila teks asli berbaris banyak", "Auto-wrap lines when the original has several lines")
    val wrapWidth get() = t("Lebar maks. per baris (karakter)", "Max line width (characters)")
    fun retry(n: Int) = tt("Coba lagi yang gagal ($n)", "Retry failed ({0})", n)
    val glossAuto get() = t("Buat otomatis dari teks", "Generate from text")
    val glossAutoBusy get() = t("Mencari istilah…", "Finding terms…")
    fun glossAutoDone(n: Int) = tt("$n istilah ditambahkan. Periksa dan hapus yang kurang tepat.", "{0} term(s) added. Review them and remove any that are wrong.", n)
    val glossAutoNone get() = t("Tidak ada istilah berulang yang ditemukan.", "No repeated terms were found.")
    val glossAutoNote get() = t("Mencari nama/istilah yang berulang (nama Latin kapital, katakana, nama + honorifik) lalu menerjemahkannya dengan penyedia terpilih.", "Finds repeated names/terms (capitalised Latin names, katakana, names with honorifics) and translates them with the selected provider.")
    val filterOnline get() = t("Hasil online", "Online")
    fun aborted(msg: String, ok: Int) = tt("Berhenti: $msg. $ok baris sudah diterapkan.", "Stopped: {0}. {1} row(s) already applied.", msg, ok)
    val cancelled get() = t("Dibatalkan; hasil sebagian sudah diterapkan.", "Cancelled; partial results were applied.")
    val nothing get() = t("Tidak ada baris yang perlu diterjemahkan.", "Nothing to translate.")
    val needKey get() = t("Isi API Key dulu.", "Enter the API key first.")
    val needModel get() = t("Isi nama model dulu.", "Enter the model name first.")
    companion object {
        val ID = OnlineStr(true)
        val EN = OnlineStr(false)
        private val cache = HashMap<Lang, OnlineStr>()
        fun of(lang: Lang): OnlineStr = cache.getOrPut(lang) { OnlineStr(lang == Lang.ID, lang) }
    }

    val closeWarn get() = t("Kembali saat proses berjalan membatalkannya; hasil sebagian tetap diterapkan.", "Going back while running cancels it; partial results are still applied.")
}

val Str.ot: OnlineStr get() = OnlineStr.of(this.langOf())
