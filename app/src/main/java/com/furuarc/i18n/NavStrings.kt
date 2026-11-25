package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Bottom navigation text and tab titles. Split from [Str] because of the parameter limit. */
class NavStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    /** Pick the text by language; CJK languages without a translation use English. */
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val tabAssets get() = t("Aset Game", "Game Assets")
    val tabGame get() = t("Alat Game", "Game Tools")
    val tabSupport get() = t("Alat Pendukung", "Support Tools")
    val tabTools get() = t("Alat Game", "Game Tools")
    val tabSettings get() = t("Pengaturan", "Settings")
    val tabHome get() = t("Beranda", "Home")
    val grpToolSettings get() = p("Pengaturan Alat", "Tool Settings", "ツール設定", "工具设置", "도구 설정")
    val grpApp get() = p("Aplikasi", "App", "アプリ", "应用", "앱")

    // Home tab
    val searchHint get() = t("Cari fitur, engine, atau ekstensi…", "Search features, engines, or extensions…")
    val clearSearch get() = t("Hapus pencarian", "Clear search")
    val recents get() = t("Terakhir dipakai", "Recently used")
    val clear get() = t("Hapus", "Clear")
    val quick get() = t("Aksi cepat", "Quick actions")
    val favorites get() = t("Favorit", "Favorites")
    val favEmpty get() = t("Belum ada favorit. Cari fitur lalu ketuk bintangnya untuk menambahkan.", "No favorites yet. Search for a feature and tap its star to add it.")
    val noResult get() = t("Tidak ada fitur yang cocok.", "No matching features.")
    val history get() = t("Pencarian terakhir", "Recent searches")
    val addFav get() = t("Tambah ke favorit", "Add to favorites")
    val editQuick get() = t("Atur Aksi Cepat", "Edit Quick Actions")
    val quickSearch get() = t("Cari fitur untuk ditambahkan…", "Search a feature to add…")
    val quickList get() = t("Daftar Aksi Cepat", "Quick actions list")
    val quickEmpty get() = t("Belum ada aksi cepat. Cari fitur di atas untuk menambahkannya.", "No quick actions yet. Search a feature above to add one.")
    val quickFull get() = t("Maksimal 10 aksi cepat.", "Maximum 10 quick actions.")
    val dragHint get() = t("Geser untuk mengurutkan", "Drag to reorder")
    val removeQuick get() = t("Hapus dari Aksi Cepat", "Remove from Quick actions")
    val addQuick get() = t("Tambah ke Aksi Cepat", "Add to Quick actions")
    val close get() = t("Tutup", "Close")
    val startFileHeader get() = t("Cari fitur melalui file", "Find features by file")
    val startFile get() = t("Unggah file", "Upload file")
    val startFileSub get() = t("Pilih file, lalu pilih fitur yang cocok untuknya", "Pick a file, then choose a feature that fits it")
    val startFileTitle get() = t("Fitur untuk file ini", "Features for this file")
    val startFileNone get() = t("Ekstensi ini tidak dikenal. Coba Pemeriksaan File atau Penampil Hex.", "This extension is not recognized. Try File Inspection or the Hex Viewer.")
    val updTitle get() = t("Periksa pembaruan", "Check for updates")
    val updSub get() = t("Ambil informasi pembaruan dari GitHub", "Fetch update information from GitHub")
    val updChecking get() = t("Memeriksa pembaruan…", "Checking for updates…")
    val updLatest get() = t("Kamu sudah memakai versi terbaru.", "You are using the latest version.")
    val updNone get() = t("Pembaruan aplikasi belum tersedia saat ini.", "App updates are not available right now.")
    val updFail get() = t("Gagal memeriksa pembaruan. Pastikan perangkat terhubung ke internet.", "Could not check for updates. Make sure the device is connected to the internet.")
    val updNew get() = t("Versi baru tersedia", "A new version is available")
    val updInstalled get() = t("Terpasang", "Installed")
    val updLatestLabel get() = t("Terbaru", "Latest")
    val updNotes get() = t("Catatan rilis", "Release notes")
    val updDownload get() = t("Download versi terbaru", "Download latest version")
    val updDownloading get() = t("Mengunduh pembaruan", "Downloading update")
    val updDownloadDone get() = t("Unduhan selesai. Tekan Install aplikasi untuk memasang.", "Download complete. Tap Install app to install it.")
    val updDownloadDoneShort get() = t("Unduhan selesai", "Download complete")
    val updInstall get() = t("Install aplikasi", "Install app")
    val updDownFail get() = t("Unduhan gagal.", "The download failed.")
    val updRetry get() = t("Coba lagi", "Try again")
    val updCancelDl get() = t("Batalkan unduhan", "Cancel download")
    val updNotifInstall get() = t("Ketuk untuk memasang.", "Tap to install.")
    val updPage get() = t("Halaman rilis", "Release page")
    val updLater get() = t("Nanti", "Later")
    val updNoApk get() = t("Rilis ini belum melampirkan file APK; halaman rilis akan dibuka.", "This release has no APK attached; the release page will be opened.")
    val updNotifTap get() = t("Ketuk untuk membuka halaman rilis.", "Tap to open the release page.")
    val removeFav get() = t("Hapus dari favorit", "Remove from favorites")

    companion object {
        fun of(lang: Lang) = NavStr(lang == Lang.ID, lang)
    }
}
