package com.furuarc.i18n

import com.furuarc.settings.Lang

/** File Name Optimizer text (Support Tools). */
class NameOptStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    val menuTitle get() = t("Pengoptimal Nama File", "File Name Optimizer")
    val menuSub get() = t("Ganti nama file berkarakter khusus menjadi nama aman dan perbarui referensinya di game", "Rename files with special characters to safe names and update their references in the game")
    val srcFolder get() = t("Folder Sumber", "Source Folder")
    val backupFolder get() = t("Folder Cadangkan (Opsional)", "Backup Folder (Optional)")
    val clearBackup get() = t("Hapus pilihan Folder Cadangkan", "Clear Backup Folder")
    val noFolder get() = t("Pilih Folder Sumber (isi folder game) dulu.", "Pick the Source Folder (the game folder) first.")
    val loading get() = t("Membaca isi folder…", "Reading the folder…")

    val intro get() = t(
        "Fitur ini untuk menyiapkan game (terutama RPG Maker MV/MZ) sebelum dibuat menjadi APK. Aplikasi pembuat APK sering gagal memproses file yang namanya memakai karakter non-Latin (Hanzi, Kana, Hangul, dll.). Pengoptimal Nama File mengganti nama file dan folder tersebut menjadi nama aman (ASCII), lalu memperbarui file-file game yang memanggil nama lama (json, js, html, css, txt, ks, rpy, dll.) supaya game tetap menemukan asetnya. Cara kerja: 1) pilih folder game (seluruh isi folder dipindai, termasuk subfolder); 2) atur cakupan dan opsi; 3) tekan Pindai dan Pratinjau untuk melihat daftar nama baru dan file mana yang akan diubah, tanpa mengubah apa pun; 4) tekan Ganti Nama dan Perbarui Referensi. Isi Folder Cadangkan agar salinan asli file teks yang diedit disimpan; semua perubahan nama juga dicatat di rename_mapping.json di Folder Sumber sehingga bisa dikembalikan lewat tombol Kembalikan Nama Asli. Nama file dienkripsi RPG Maker (.rpgmvp, .rpgmvo, dll.) ikut diganti selama Cakupan file = Semua file berkarakter khusus, karena referensinya memakai nama dasar yang sama.",
        "This feature prepares a game (especially RPG Maker MV/MZ) before it is built into an APK. APK builders often fail on file names with non-Latin characters (Hanzi, Kana, Hangul, etc.). The File Name Optimizer renames those files and folders to safe (ASCII) names, then updates the game files that refer to the old names (json, js, html, css, txt, ks, rpy, etc.) so the game still finds its assets. How it works: 1) pick the game folder (everything inside is scanned, including subfolders); 2) set the scope and options; 3) press Scan and Preview to see the list of new names and which files will change, without changing anything; 4) press Rename and Update References. Fill in the Backup Folder to keep copies of the original text files that get edited; every rename is also recorded in rename_mapping.json in the Source Folder so it can be reverted with the Restore Original Names button. Files encrypted by RPG Maker (.rpgmvp, .rpgmvo, etc.) are renamed too while File scope = All files with special characters, because references use the same base name.",
    )

    val scope get() = t("Cakupan file", "File scope")
    val scopeOpts get() = listOf(t("Semua file berkarakter khusus", "All files with special characters"), t("Hanya aset media berkarakter khusus", "Only media assets with special characters"))
    val scopeHint get() = t(
        "Semua file berkarakter khusus: semua file (dan folder, bila diikutkan) yang namanya mengandung karakter non-ASCII diganti, termasuk file terenkripsi RPG Maker (.rpgmvp, .rpgmvo, .png_, .ogg_), data, plugin, dan file lain. Hanya aset media: hanya gambar, audio, dan video (png, jpg, webp, ogg, m4a, mp3, wav, mp4, webm, dll.) yang diganti; file lain dibiarkan.",
        "All files with special characters: every file (and folder, if included) whose name has non-ASCII characters is renamed, including RPG Maker encrypted files (.rpgmvp, .rpgmvo, .png_, .ogg_), data, plugins and others. Only media assets: only images, audio and video (png, jpg, webp, ogg, m4a, mp3, wav, mp4, webm, etc.) are renamed; other files are left alone.",
    )
    val style get() = t("Gaya nama baru", "New name style")
    val styleOpts get() = listOf(t("Pertahankan bagian ASCII (戦闘BGM → BGM_001)", "Keep the ASCII part (戦闘BGM → BGM_001)"), t("Nomor saja (file_001)", "Number only (file_001)"))
    val styleHint get() = t(
        "Pertahankan bagian ASCII: huruf dan angka Latin pada nama lama dipakai lagi, diikuti nomor urut (BGM_戦闘01 → BGM_01_001); bila tidak ada, dipakai file_001. Nama yang sama di folder berbeda mendapat nama baru yang sama, jadi referensinya tetap konsisten. Nomor dilewati bila bentrok dengan file yang sudah ada.",
        "Keep the ASCII part: Latin letters and digits from the old name are reused, followed by a serial number (BGM_戦闘01 → BGM_01_001); if there are none, file_001 is used. The same name in different folders gets the same new name, so references stay consistent. Numbers are skipped when they clash with existing files.",
    )
    val chkFolders get() = t("Ikutkan nama folder", "Include folder names")
    val chkSymbols get() = t("Ikutkan nama ASCII dengan spasi atau tanda khusus", "Include ASCII names with spaces or special marks")
    val symbolsHint get() = t(
        "Bawaan mati. Bila dicentang, nama yang hanya memakai huruf Latin tetapi mengandung spasi atau tanda seperti & # % ' ( ) ! diganti juga (tanda diganti _, mis. \"Actor 1.png\" → \"Actor_1.png\").",
        "Off by default. When checked, names that use only Latin letters but contain spaces or marks such as & # % ' ( ) ! are renamed too (marks become _, e.g. \"Actor 1.png\" → \"Actor_1.png\").",
    )
    val refMode get() = t("Cara mengganti referensi", "Reference replacement")
    val refModes get() = listOf(t("Aman (nama utuh di antara tanda kutip / garis miring)", "Safe (whole name between quotes / slashes)"), t("Luas (semua kemunculan nama)", "Wide (every occurrence of the name)"))
    val refHint get() = t(
        "Aman (disarankan): sebuah nama hanya diganti bila berdiri utuh sebagai nilai, misalnya \"背景\", \"背景.png\", \"img/pictures/背景\", storage=背景.png, termasuk bentuk \\u80cc\\u666f dan %E8%83%8C yang diurai dulu. Teks dialog yang kebetulan memuat kata yang sama tidak tersentuh. Luas: semua kemunculan nama (mulai 2 karakter) diganti di mana pun, seperti skrip aslinya; berguna bila game menyusun nama dari potongan teks, tetapi bisa ikut mengubah dialog atau nama karakter yang sama. Periksa Pratinjau sebelum memakai mode ini.",
        "Safe (recommended): a name is replaced only when it stands whole as a value, for example \"背景\", \"背景.png\", \"img/pictures/背景\", storage=背景.png, including \\u80cc\\u666f and %E8%83%8C forms which are decoded first. Dialogue text that happens to contain the same word is not touched. Wide: every occurrence of the name (2+ characters) is replaced anywhere, like the original script; useful when the game builds names from pieces of text, but it may also change dialogue or character names that are the same. Check the Preview before using this mode.",
    )
    val chkCase get() = t("Perbaiki huruf besar/kecil referensi", "Fix reference letter case")
    val caseHint get() = t(
        "Android membedakan huruf besar dan kecil, sedangkan Windows tidak. Game dari Windows sering memanggil Bgm1.ogg padahal filenya bgm1.ogg, sehingga aset tidak termuat di APK. Bila dicentang, referensi yang hanya berbeda huruf besar/kecil dari nama file yang sebenarnya diperbaiki (hanya mode Aman). Nama yang ambigu (ada dua file yang hanya beda huruf besar/kecil) dilewati. Perbaikan ini tidak dikembalikan oleh Kembalikan Nama Asli; gunakan Folder Cadangkan bila ingin bisa memulihkannya.",
        "Android is case-sensitive, Windows is not. Games from Windows often call Bgm1.ogg while the file is bgm1.ogg, so the asset fails to load in the APK. When checked, references that differ from the real file name only in letter case are corrected (Safe mode only). Ambiguous names (two files that differ only in case) are skipped. This fix is not reverted by Restore Original Names; use a Backup Folder if you want to be able to recover it.",
    )
    val textExts get() = t("Ekstensi file teks yang diperiksa", "Text file extensions to check")
    val textExtsHint get() = t("Dipisah koma. File yang bukan UTF-8 atau lebih dari 24 MB dilewati dan dilaporkan.", "Comma separated. Files that are not UTF-8 or larger than 24 MB are skipped and reported.")

    // ---- readiness report ----
    val readyTitle get() = t("Laporan Kesiapan Build", "Build Readiness Report")
    fun readyCounts(f: Int, d: Int) = tt("Isi folder: $f file, $d folder", "Folder contents: {0} files, {1} folders", f, d)
    fun readyNonAscii(n: Int) = tt("Nama non-ASCII: $n", "Non-ASCII names: {0}", n)
    fun readySymbols(n: Int) = tt("Nama ASCII dengan spasi/tanda khusus: $n", "ASCII names with spaces/special marks: {0}", n)
    fun readyLong(n: Int) = tt("Nama terlalu panjang (nama > 100 atau jalur > 200 karakter): $n", "Names too long (name > 100 or path > 200 characters): {0}", n)
    fun readyCase(n: Int) = tt("Nama yang hanya beda huruf besar/kecil (bentrok di Windows/Android): $n", "Names that differ only in letter case (clash on Windows/Android): {0}", n)
    fun readyBad(s: String) = tt("Format yang sering tidak didukung WebView Android: $s", "Formats often unsupported by Android WebView: {0}", s)
    val readyOk get() = t("Tidak ada masalah nama atau format yang terdeteksi.", "No name or format problems detected.")
    val readyDetail get() = t("Daftar jalur bermasalah", "Problem paths")

    // ---- scan and preview ----
    val scanBtn get() = t("Pindai dan Pratinjau", "Scan and Preview")
    fun scanning(d: Int, tot: Int) = tt("Memeriksa referensi di file teks… ($d/$tot)", "Checking references in text files… ({0}/{1})", d, tot)
    val planning get() = t("Menyusun nama baru…", "Planning new names…")
    val prevTitle get() = t("Pratinjau", "Preview")
    val prevNone get() = t("Tidak ada nama yang perlu diganti dengan pengaturan ini.", "No names need to be changed with these settings.")
    fun prevSummary(f: Int, d: Int, e: Int, r: Int, c: Int) = t(
        "Akan diganti: $f file dan $d folder. Referensi diperbarui di $e file teks ($r penggantian" + (if (c > 0) ", $c perbaikan huruf besar/kecil" else "") + ").",
        "To rename: $f files and $d folders. References updated in $e text files ($r replacements" + (if (c > 0) ", $c case fixes" else "") + ").",
    )
    val prevOnlyCase get() = t("Tidak ada nama yang diganti, tetapi ada referensi yang huruf besar/kecilnya akan diperbaiki.", "No names will be renamed, but some references will get their letter case corrected.")
    val refill get() = t("Isi ulang", "Refill")
    val checkedBtn get() = t("Daftar file diperiksa", "Checked files list")
    val checkedTitle get() = t("Daftar file diperiksa", "Checked files")
    val tabEdited get() = t("File teks yang diubah", "Changed text files")
    val tabUnref get() = t("File tanpa referensi", "Files without references")
    val listEmpty get() = t("Tidak ada file di daftar ini.", "No files in this list.")
    fun prevUnref(n: Int) = tt("$n file yang diganti namanya tidak ditemukan di file teks mana pun (bisa dipanggil lewat skrip yang menyusun nama, atau memang tidak terpakai).", "{0} renamed files were not found in any text file (they may be loaded by scripts that build names, or simply unused).", n)
    fun prevSkipped(n: Int) = tt("$n file teks dilewati (bukan UTF-8 atau terlalu besar).", "{0} text files skipped (not UTF-8 or too large).", n)
    val prevListTitle get() = t("Daftar", "List")
    fun pageLabel(p: Int, ps: Int, total: Int) = tt("Halaman $p / $ps · $total nama", "Page {0} / {1} · {2} names", p, ps, total)
    val folderTag get() = t("[Folder] ", "[Folder] ")
    val stale get() = t("Pengaturan berubah. Pindai ulang untuk memperbarui pratinjau.", "Settings changed. Scan again to refresh the preview.")

    // ---- run ----
    val runBtn get() = t("Ganti Nama dan Perbarui Referensi", "Rename and Update References")
    fun confirmTitle(n: Int) = tt("Ganti nama $n item?", "Rename {0} items?", n)
    fun confirmBackup(name: String) = tt("Salinan asli file teks yang diedit akan disimpan di Folder Cadangkan ($name). Nama file dan folder dicatat di rename_mapping.json sehingga bisa dikembalikan.", "Original copies of the edited text files will be kept in the Backup Folder ({0}). File and folder names are recorded in rename_mapping.json so they can be restored.", name)
    val confirmNoBackup get() = t(
        "Folder Cadangkan belum diisi. File teks game akan langsung diubah tanpa salinan cadangan. Nama file dan folder tetap bisa dikembalikan lewat rename_mapping.json, tetapi isi file teks hanya dikembalikan dengan penggantian balik (bukan salinan asli) dan perbaikan huruf besar/kecil tidak bisa dikembalikan. Disarankan mencadangkan folder game dulu. Lanjutkan tanpa cadangan?",
        "No Backup Folder is set. The game's text files will be changed directly with no backup copy. File and folder names can still be restored through rename_mapping.json, but text contents are only restored by reverse replacement (not from original copies) and letter-case fixes cannot be restored. Backing up the game folder first is recommended. Continue without a backup?",
    )
    val confirmYes get() = t("Lanjutkan", "Continue")
    val confirmYesNoBackup get() = t("Lanjutkan tanpa cadangan", "Continue without backup")
    val confirmNo get() = t("Batal", "Cancel")
    fun phase(p: Int, d: Int, tot: Int) = when (p) {
        0 -> tt("Mengedit referensi… ($d/$tot)", "Editing references… ({0}/{1})", d, tot)
        1 -> tt("Mengganti nama file… ($d/$tot)", "Renaming files… ({0}/{1})", d, tot)
        else -> tt("Mengganti nama folder… ($d/$tot)", "Renaming folders… ({0}/{1})", d, tot)
    }
    fun undoPhase(p: Int, d: Int, tot: Int) = when (p) {
        0 -> tt("Mengembalikan referensi… ($d/$tot)", "Restoring references… ({0}/{1})", d, tot)
        1 -> tt("Mengembalikan nama file… ($d/$tot)", "Restoring file names… ({0}/{1})", d, tot)
        else -> tt("Mengembalikan nama folder… ($d/$tot)", "Restoring folder names… ({0}/{1})", d, tot)
    }

    // ---- result ----
    fun resDone(f: Int, d: Int, e: Int, r: Int) = tt("Selesai: $f file dan $d folder diganti namanya; $e file teks diperbarui ($r penggantian).", "Done: {0} files and {1} folders renamed; {2} text files updated ({3} replacements).", f, d, e, r)
    fun resBackup(n: Int, name: String) = tt("$n file teks asli dicadangkan ke $name.", "{0} original text files backed up to {1}.", n, name)
    fun resFailed(n: Int) = tt("$n item gagal diproses.", "{0} items failed.", n)
    val resFailedList get() = t("Daftar yang gagal", "Failed items")
    val resUnrefList get() = t("File tanpa referensi", "Files without references")
    val resMapping get() = t("Catatan perubahan disimpan di rename_mapping.json (Folder Sumber).", "Change log saved in rename_mapping.json (Source Folder).")
    val resNext get() = t("Langkah berikutnya: kompres aset untuk APK lewat Konverter Aset Campuran.", "Next step: compress assets for the APK with the Mixed Asset Converter.")
    val gotoMixed get() = t("Buka Konverter Aset Campuran", "Open Mixed Asset Converter")
    fun fail(m: String) = tt("Gagal: $m", "Failed: {0}", m)
    val close get() = t("Tutup", "Close")

    // ---- restore ----
    val undoBtn get() = t("Kembalikan Nama Asli", "Restore Original Names")
    val undoTitle get() = t("Kembalikan nama asli?", "Restore original names?")
    val undoMsg get() = t("Proses terakhir di rename_mapping.json dibatalkan: referensi dikembalikan dengan penggantian balik, lalu nama file dan folder dikembalikan. Perbaikan huruf besar/kecil tidak ikut dikembalikan.", "The last run in rename_mapping.json is reverted: references are restored by reverse replacement, then file and folder names are restored. Letter-case fixes are not reverted.")
    val undoYes get() = t("Kembalikan", "Restore")
    fun undoDone(f: Int, d: Int, e: Int) = tt("Dikembalikan: $f file, $d folder, $e file teks.", "Restored: {0} files, {1} folders, {2} text files.", f, d, e)
    val undoNone get() = t("rename_mapping.json tidak ditemukan atau kosong.", "rename_mapping.json not found or empty.")
}

fun nameOptStr(lang: Lang) = NameOptStr(lang == Lang.ID, lang)
