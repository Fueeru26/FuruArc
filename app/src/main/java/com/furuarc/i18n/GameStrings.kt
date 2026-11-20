package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks Aset Game (perpustakaan game + halaman game). */
class GameStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)
    /** Pick the text for the current language; CJK languages without a translation fall back to English. */
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val title get() = t("Aset Game", "Game Assets")
    val add get() = t("Tambah", "Add")
    val search get() = t("Cari", "Search")
    val refresh get() = t("Segarkan", "Refresh")
    val emptyTitle get() = t("Belum ada game", "No games yet")
    val emptySub get() = t("Tambahkan folder game lalu lihat aset-asetnya", "Add a game folder and browse its assets")
    val addFolder get() = t("Tambah folder", "Add folder")
    val searchHint get() = t("Cari game…", "Search games…")
    val noResult get() = t("Tidak ada hasil", "No results")
    val working get() = t("Memindai folder…", "Scanning folders…")
    val missing get() = t("Folder tidak dapat diakses", "Folder not accessible")

    val dlgAddTitle get() = t("Tambah Game", "Add Game")
    val dlgFolder get() = t("Folder game", "Game folder")
    val dlgName get() = t("Nama Game", "Game name")
    val dlgCover get() = t("Cover (opsional)", "Cover (optional)")
    val dlgCoverHint get() = t("Jika gambar cover tidak tersedia, akan mencari di dalam folder jika ada", "If no cover image is set, the game folder is searched for one")
    val cancel get() = t("Batal", "Cancel")
    val next get() = t("Lanjut", "Next")
    val addBtn get() = t("Tambahkan", "Add")
    val save get() = t("Simpan", "Save")
    val exists get() = t("Folder ini sudah ada di daftar.", "This folder is already in the list.")

    val rename get() = t("Ganti Nama", "Rename")
    val changeCover get() = t("Ganti Cover", "Change Cover")
    val removeFolder get() = t("Hapus Folder", "Delete Folder")
    val removeTitle get() = t("Hapus folder dari daftar?", "Remove folder from the list?")
    fun removeMsg(n: String) = tt("Hanya entri \"$n\" di daftar Aset Game yang dihapus. Folder dan file aslinya di penyimpanan TIDAK dihapus.", "Only the \"{0}\" entry in the Game Assets list is removed. The real folder and its files in storage are NOT deleted.", n,
    )
    val removeYes get() = t("Hapus", "Delete")

    val menuAudio get() = t("Audio", "Audio")
    val menuImage get() = t("Gambar", "Images")
    val menuVideo get() = t("Video", "Video")
    val menuText get() = t("Teks", "Text")
    fun count(n: Int) = p("$n file", "$n files", "${n}個のファイル", "${n}个文件", "파일 ${n}개")
    val scanning get() = t("Memindai…", "Scanning…")
    fun loadFail(m: String) = tt("Gagal membaca folder: $m", "Could not read the folder: {0}", m)
    val retry get() = t("Pindai ulang", "Scan again")
    fun gameInfo(engine: String, files: Int) = (if (engine.isNotEmpty()) "$engine · " else "") + count(files)
    val encNote get() = t("Aset RPG Maker yang terenkripsi dipratinjau otomatis (didekripsi sementara, file asli tidak diubah).", "Encrypted RPG Maker assets are previewed automatically (decrypted temporarily; original files are not changed).")
    val noKey get() = t("Kunci enkripsi tidak ditemukan, aset terenkripsi tidak bisa dipratinjau.", "Encryption key not found, encrypted assets cannot be previewed.")

    val filterHint get() = t("Cari file…", "Search files…")
    val sortLabel get() = t("Urutkan", "Sort")
    val sortOpts get() = listOf(
        t("Urutkan Nama A ➞ Z", "Sort by Name A ➞ Z"), t("Urutkan Nama Z ➞ A", "Sort by Name Z ➞ A"),
        t("Urutkan Ukuran Kecil ➞ Besar", "Sort by Size Small ➞ Large"), t("Urutkan Ukuran Besar ➞ Kecil", "Sort by Size Large ➞ Small"),
        t("Urutkan Acak", "Sort Random"),
    )
    fun pageLabel(pg: Int, ps: Int, total: Int) = p("Halaman $pg / $ps · $total file", "Page $pg / $ps · $total files", "ページ $pg / $ps · ${total}ファイル", "第 $pg / $ps 页 · ${total}个文件", "페이지 $pg / $ps · 파일 ${total}개")
    fun arcScanning(d: Int, tt: Int, n: String) = tt("Memindai arsip ($d/$tt): $n", "Scanning archives ({0}/{1}): {2}", d, tt, n)
    val updating get() = t("Folder berubah, memperbarui daftar…", "Folder changed, updating the list…")
    fun arcSummary(ok: Int, bad: Int) = t("Arsip terbaca: $ok" + (if (bad > 0) ", gagal: $bad" else ""), "Archives read: $ok" + (if (bad > 0) ", failed: $bad" else ""))
    val arcPrep get() = t("Menyiapkan file dari arsip…", "Preparing file from archive…")
    val cacheLimit get() = t("Batas cache", "Cache limit")
    val cacheNote get() = t("Cache dipakai untuk audio/video dari dalam arsip. Aset RPG Maker terenkripsi diputar tanpa cache.", "The cache is used for audio/video from inside archives. Encrypted RPG Maker assets play without any cache.")
    fun shown(a: Int, b: Int) = p("$a dari $b file", "$a of $b files", "${b}ファイル中 ${a}", "共 $b 个文件，显示 $a 个", "파일 ${b}개 중 ${a}개")
    val empty get() = t("Tidak ada file.", "No files.")
    val encBadge get() = t("Terenkripsi", "Encrypted")
    val playFail get() = t("Gagal memutar. Format ini mungkin tidak didukung perangkat.", "Playback failed. This format may not be supported by the device.")
    val loadingMedia get() = t("Memuat…", "Loading…")
    val imgFail get() = t("Gambar tidak bisa ditampilkan.", "The image cannot be shown.")
    val prev get() = t("Sebelumnya", "Previous")
    val nextImg get() = t("Berikutnya", "Next")
    val close get() = t("Tutup", "Close")
    val pageInfo get() = t("Cubit untuk zoom, geser untuk pindah gambar, ketuk dua kali untuk reset.", "Pinch to zoom, swipe to change image, double-tap to reset.")

    val textScanning get() = t("Memindai teks dan skrip…", "Scanning text and scripts…")
    val encLabel get() = t("Pengodean", "Encoding")
    val encOpts get() = listOf(t("Otomatis", "Auto"), "UTF-8", "Shift-JIS", "GBK", "UTF-16")
    val modeOpts get() = listOf(t("Dialog", "Dialogue"), t("Mentah", "Raw"))
    val badgeScript get() = t("Dialog skrip", "Script dialogue")
    val badgeData get() = t("Data RPG Maker", "RPG Maker data")
    val badgeText get() = t("Teks", "Text")
    fun dialogCount(n: Int) = tt("$n teks dialog ditemukan", "{0} dialogue texts found", n)
    val noDialog get() = t("Tidak ada dialog yang dikenali di file ini.", "No dialogue recognised in this file.")
    val tooBig get() = t("File terlalu besar untuk diekstrak (maks. 32 MB).", "File too large to extract (max 32 MB).")
    val truncated get() = t("Hanya 4 MB pertama yang ditampilkan.", "Only the first 4 MB is shown.")
    fun readFail(m: String) = tt("Gagal membaca: $m", "Could not read: {0}", m)
    val textFilter get() = t("Cari di teks…", "Search in text…")

    val cacheTitle get() = t("Cache Pratinjau Aset Game", "Game Assets Preview Cache")
    fun cacheSub(used: String) = tt("Terpakai $used. Berisi file sementara untuk pratinjau; aman dihapus.", "Using {0}. Holds temporary files for previews; safe to delete.", used)
    val cacheClear get() = t("Hapus cache", "Clear cache")
    // ---- info game ----
    val infoEngine get() = t("Engine Terdeteksi:", "Detected Engine:")
    val infoUnknown get() = t("Tidak diketahui", "Unknown")
    fun infoFiles(n: Int) = p("Total file di folder : $n", "Total files in folder : $n", "フォルダ内の総ファイル数 : $n", "文件夹内文件总数 : $n", "폴더 내 총 파일 수 : $n")
    fun infoArcOk(n: Int) = p("Arsip terbaca : $n", "Archives read : $n", "読み取れたアーカイブ : $n", "已读取的压缩包 : $n", "읽은 아카이브 : $n")
    fun infoArcBad(n: Int) = p("Arsip gagal terbaca : $n", "Archives failed : $n", "読み取れなかったアーカイブ : $n", "读取失败的压缩包 : $n", "읽지 못한 아카이브 : $n")
    fun infoMediaBad(n: Int) = p("Media gagal dimuat : $n", "Media failed to load : $n", "読み込みに失敗したメディア : $n", "加载失败的媒体 : $n", "불러오기 실패한 미디어 : $n")
    fun infoMedia(n: Int) = p("Media berhasil dimuat : $n", "Media loaded : $n", "読み込めたメディア : $n", "已加载的媒体 : $n", "불러온 미디어 : $n")

    // ---- cover internet ----
    val autoBtn get() = p("Cari otomatis", "Auto search", "自動検索", "自动搜索", "자동 검색")
    val manualBtn get() = p("Cari manual", "Manual search", "手動検索", "手动搜索", "수동 검색")
    val coverTitle get() = p("Gambar Cover", "Cover Image", "カバー画像", "封面图片", "커버 이미지")
    val coverNetTitle get() = p("Cari gambar cover di Internet", "Search for a cover image on the Internet", "インターネットでカバー画像を検索", "在互联网上搜索封面图片", "인터넷에서 커버 이미지 검색")
    val settingsBtn get() = p("Pengaturan", "Settings", "設定", "设置", "설정")
    val libSortOpts get() = listOf(
        p("Nama (A–Z)", "Name (A–Z)", "名前（A–Z）", "名称（A–Z）", "이름 (A–Z)"),
        p("Nama (Z–A)", "Name (Z–A)", "名前（Z–A）", "名称（Z–A）", "이름 (Z–A)"),
        p("Terakhir ditambahkan", "Recently added", "最近追加した順", "最近添加", "최근 추가순"),
        p("Pertama ditambahkan", "First added", "追加が古い順", "最早添加", "오래된 추가순"),
        p("Acak", "Random", "ランダム", "随机", "무작위"),
    )
    val addMain get() = p("Tambahkan folder utama", "Add main folder", "メインフォルダを追加", "添加主文件夹", "메인 폴더 추가")
    val addMainMsg get() = p("Pilih folder yang berisi folder-folder game. Setiap subfolder di dalamnya ditambahkan sebagai satu game; yang sudah ada dilewati.", "Pick a folder that contains game folders. Each subfolder inside is added as one game; ones already in the list are skipped.", "ゲームフォルダが入っているフォルダを選んでください。中の各サブフォルダが1つのゲームとして追加され、すでにあるものはスキップされます。", "请选择包含多个游戏文件夹的文件夹。其中每个子文件夹会作为一个游戏添加，已在列表中的会被跳过。", "게임 폴더들이 들어 있는 폴더를 선택하세요. 안의 각 하위 폴더가 게임 하나로 추가되며, 이미 있는 것은 건너뜁니다.")
    val mainFolderLabel get() = p("Folder utama", "Main folder", "メインフォルダ", "主文件夹", "메인 폴더")
    val addMainBtn get() = p("Tambahkan", "Add", "追加", "添加", "추가")
    fun addMainDone(added: Int, skipped: Int) = p("$added game ditambahkan, $skipped dilewati", "$added game(s) added, $skipped skipped", "${added}件のゲームを追加、${skipped}件をスキップ", "已添加 $added 个游戏，跳过 $skipped 个", "게임 ${added}개 추가, ${skipped}개 건너뜀")
    val deleteAll get() = p("Hapus seluruh daftar", "Delete the whole list", "リストをすべて削除", "删除整个列表", "목록 전체 삭제")
    val delAllTitle get() = p("Hapus seluruh daftar game?", "Delete the whole game list?", "ゲームリストをすべて削除しますか？", "删除整个游戏列表？", "게임 목록을 모두 삭제할까요?")
    val delAllMsg get() = p("Semua entri di daftar Aset Game dihapus. Folder dan file aslinya di penyimpanan TIDAK dihapus.", "All entries in the Game Assets list are removed. The real folders and files in storage are NOT deleted.", "ゲームアセットのリストの全エントリが削除されます。ストレージ内の実際のフォルダとファイルは削除されません。", "游戏资源列表中的所有条目将被删除。存储中的实际文件夹和文件不会被删除。", "게임 에셋 목록의 모든 항목이 삭제됩니다. 저장소의 실제 폴더와 파일은 삭제되지 않습니다.")
    val delAllYes get() = p("Hapus semua", "Delete all", "すべて削除", "全部删除", "모두 삭제")
    val manualTitle get() = t("Cari manual di internet", "Manual search on the internet")
    val sourceLabel get() = t("Sumber", "Source")
    val searching get() = t("Mencari…", "Searching…")
    val autoNone get() = t("Tidak ada cover yang cocok ditemukan.", "No matching cover was found.")
    fun fromNet(src: String) = tt("Dari internet ($src)", "From the internet ({0})", src)
    val noHits get() = t("Tidak ada hasil.", "No results.")
    fun netFail(m: String) = tt("Gagal mencari: $m", "Search failed: {0}", m)
    val letmoeNA get() = t("Sumber letmoe belum bisa dipakai di aplikasi ini.", "The letmoe source is not usable in this app yet.")
    val dlFail get() = t("Gagal mengunduh gambar.", "Could not download the image.")
    val cloud get() = t("Cari cover di internet", "Find covers online")
    val cloudMsg get() = t("Cover dicari otomatis di VNDB, NextMoe, Steam, lalu Bing berdasarkan nama game. Yang dipakai adalah hasil pertama dari sumber pertama yang punya hasil.", "Covers are searched automatically on VNDB, NextMoe, Steam, then Bing by game name. The first result of the first source that has one is used.")
    val cloudMissing get() = t("Hanya yang belum punya cover", "Only games without a cover")
    val cloudAll get() = t("Ganti semua cover game", "Replace all game covers")
    fun coverProg(d: Int, tt: Int, n: String) = tt("Mencari cover ($d/$tt): $n", "Searching covers ({0}/{1}): {2}", d, tt, n)
    fun coverDone(f: Int, tt: Int) = tt("Cover ditemukan: $f dari $tt game", "Covers found: {0} of {1} games", f, tt)
    val pickClose get() = t("Tutup", "Close")

    // ---- tahap 3: pilih, ekstrak, bagikan, favorit ----
    fun selected(n: Int) = p("$n dipilih", "$n selected", "${n}件選択中", "已选择 $n 项", "${n}개 선택됨")
    val selAll get() = t("Pilih semua", "Select all")
    val selNone get() = t("Batal pilih", "Clear")
    val extract get() = t("Ekstrak", "Extract")
    val share get() = t("Bagikan", "Share")
    val favAdd get() = t("Favorit", "Favorite")
    val favDel get() = t("Hapus favorit", "Unfavorite")
    val engineLabelTitle get() = t("Label engine di cover Aset Game", "Engine label on Game Assets covers")
    val engineLabelDesc get() = t("Tampilkan nama engine (mis. Kirikiri / KAG) di pojok kiri bawah cover game.", "Show the engine name (e.g. Kirikiri / KAG) at the bottom-left of each game cover.")
    val selMode get() = t("Mode pilih", "Select mode")
    val favOnly get() = t("Hanya favorit", "Favorites only")
    val longPressHint get() = t("Tekan lama untuk memilih beberapa file.", "Long-press to select multiple files.")
    val extractTitle get() = t("Mengekstrak", "Extracting")
    fun extractProg(d: Int, tt: Int) = tt("$d / $tt file", "{0} / {1} files", d, tt)
    fun extractDone(tt: Int, bad: Int, f: String) = t("Selesai: $tt file" + (if (bad > 0) ", gagal $bad" else "") + ". Folder: $f", "Done: $tt files" + (if (bad > 0) ", $bad failed" else "") + ". Folder: $f")
    val extractCancel get() = t("Hentikan", "Stop")
    val ok get() = t("OK", "OK")
    val shareTooMany get() = t("Terlalu banyak file untuk dibagikan sekaligus (maks. 20). Gunakan Ekstrak.", "Too many files to share at once (max 20). Use Extract.")
    val shareTooBig get() = t("Total ukuran lebih dari 200 MB. Gunakan Ekstrak.", "Total size is over 200 MB. Use Extract.")
    val shareFail get() = t("Gagal membagikan file.", "Could not share the files.")
    val pickFolder get() = t("Pilih folder tujuan", "Choose destination folder")
}


fun gameStr(lang: Lang) = GameStr(lang == Lang.ID, lang)
