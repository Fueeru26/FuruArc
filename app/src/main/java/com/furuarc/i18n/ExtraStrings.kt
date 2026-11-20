package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Additional text (pack integrity check, view as text, folder summary). Separated from [Str] because of the parameter limit. */
class ExtraStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    // Preview: pages & audio
    fun pageOf(a: Int, b: Int) = tt("Halaman $a dari $b (50 KB/halaman)", "Page {0} of {1} (50 KB/page)", a, b)
    val copyPage get() = t("Salin halaman ini", "Copy this page")
    val copied get() = t("Disalin", "Copied")
    val audioLoading get() = t("Memuat audio…", "Loading audio…")
    val audioFail get() = t("Audio tidak bisa diputar", "Audio cannot be played")
    val audioPlay get() = t("Putar", "Play")
    val audioPause get() = t("Jeda", "Pause")
    val homeKeyFinderNote get() = t("", "")
    val processing get() = t("Sedang memproses...", "Processing...")
    val outFileName get() = t("Nama file output", "Output file name")

    // Pack integrity check
    val btnTranslateText get() = t("Terjemahkan teks", "Translate text")
    val btnVerifyPack get() = t("Cek integritas pengemasan", "Check packing integrity")
    val vPackTitle get() = t("Integritas pengemasan", "Packing integrity")
    val vPackChecked get() = t("File sumber diperiksa", "Source files checked")
    val vPackSizeOk get() = t("Ukuran sama dengan sumber", "Size matches source")
    val vPackSizeBad get() = t("Ukuran berbeda", "Size differs")
    val vPackMissing get() = t("Tidak ada di arsip / tidak terbaca", "Missing from archive / unreadable")
    val vPackContent get() = t("Isi sama dengan sumber (CRC32)", "Content matches source (CRC32)")
    val vPackAllOk get() = t("Arsip cocok dengan folder sumber", "The archive matches the source folder")
    fun vPackProblems(n: Int) = tt("$n masalah ditemukan", "{0} problem(s) found", n)
    fun vPackMissingFile(n: String) = tt("Tidak ada di arsip: $n", "Missing from archive: {0}", n)
    fun vPackExtra(n: String) = tt("Ada di arsip tapi bukan dari folder sumber: $n", "In the archive but not in the source folder: {0}", n)
    fun vPackSize(n: String, got: String, want: String) = tt("Ukuran berbeda: $n ($got, seharusnya $want)", "Size differs: {0} ({1}, expected {2})", n, got, want)
    fun vPackContentBad(n: String) = tt("Isi berbeda: $n", "Content differs: {0}", n)
    val vPackNote get() = t("Arsip dibaca ulang dan tiap file diekstrak ke memori lalu dibandingkan dengan folder sumber. Format yang menyusun ulang isi (mis. Scene.pck) tetap dinilai dari isi file hasil ekstrak, bukan byte arsip.", "The archive is read back and every file is extracted to memory and compared to the source folder. For formats that rebuild their contents (e.g. Scene.pck) the extracted files are compared, not the archive bytes.")

    // View contents as text
    val viewText get() = t("Lihat isi sebagai teks", "View contents as text")
    val viewTextTitle get() = t("Isi file sebagai teks", "File contents as text")
    val viewTextTruncated get() = t("(ditampilkan 1 MB pertama)", "(first 1 MB shown)")
    val viewTextClose get() = t("Tutup", "Close")
    val viewTextFail get() = t("Isi file tidak bisa dibaca", "The file could not be read")

    companion object {
        val ID = ExtraStr(true)
        val EN = ExtraStr(false)
        private val cache = HashMap<Lang, ExtraStr>()
        fun of(lang: Lang): ExtraStr = cache.getOrPut(lang) { ExtraStr(lang == Lang.ID, lang) }
    }
}

val Str.x: ExtraStr get() = ExtraStr.of(this.langOf())
