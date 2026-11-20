package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Text of the File Tools menu (File Splitter, File Merger, Bulk Rename). File Cutter stays in [CheckStr]. */
class FileToolsStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    // ---- menu ----
    val menuTitle get() = t("Alat Bantu File", "File Toolkit")
    val menuSub get() = t("Potong, bagi, gabung, dan ganti nama file secara massal", "Cut, split, join and batch-rename files")

    // ---- common ----
    val source get() = t("File sumber", "Source file")
    val outFolder get() = t("Folder Output", "Output Folder")
    val noFile get() = t("Pilih file sumber dulu.", "Pick the source file first.")
    val noOut get() = t("Pilih Folder Output dulu.", "Pick the Output Folder first.")
    fun fail(m: String) = tt("Gagal: $m", "Failed: {0}", m)
    val close get() = t("Tutup", "Close")
    fun info(n: String, size: String) = "$n · $size"

    // ---- File Splitter ----
    val splitTitle get() = t("Pembagi File", "File Splitter")
    val splitSub get() = t("Bagi file besar menjadi beberapa bagian bernomor (.001, .002, …)", "Split a large file into numbered parts (.001, .002, …)")
    val splitBy get() = t("Cara membagi", "Split by")
    val splitModes get() = listOf(t("Per ukuran", "By size"), t("Per jumlah bagian", "By part count"))
    val splitSize get() = t("Ukuran tiap bagian", "Size of each part")
    val splitCount get() = t("Jumlah bagian", "Number of parts")
    val splitBase get() = t("Nama dasar bagian", "Part base name")
    val splitRun get() = t("Bagi File", "Split File")
    val splitBusy get() = t("Membagi…", "Splitting…")
    val splitHint get() = t(
        "Hasilnya diberi nomor .001, .002, dst. Gabungkan kembali dengan Penggabung File (atau alat lain seperti HJSplit). File sumber tidak diubah.",
        "Parts are numbered .001, .002, etc. Join them back with File Joiner (or another tool such as HJSplit). The source file is not changed.",
    )
    fun splitInfo(n: Int, size: String, last: String) = tt("Akan menjadi $n bagian (tiap bagian $size, bagian terakhir $last).", "Will make {0} parts ({1} each, last part {2}).", n, size, last,
    )
    val splitOne get() = t("Ukuran bagian sama dengan atau lebih besar dari file, hasilnya hanya 1 bagian.", "The part size is not smaller than the file, so the result is a single part.")
    fun tooMany(max: Int) = tt("Terlalu banyak bagian (maksimal $max). Perbesar ukuran bagian.", "Too many parts (maximum {0}). Increase the part size.", max)
    val emptyFile get() = t("File sumber kosong.", "The source file is empty.")
    val readFail get() = t("File sumber berhenti terbaca sebelum selesai.", "The source file stopped being readable before the end.")
    fun splitDone(n: Int, base: String, size: String) = tt("Berhasil: file $size dibagi menjadi $n bagian ($base.001 dst.) di folder output.", "Done: the {0} file was split into {1} parts ({2}.001 etc.) in the output folder.", size, n, base,
    )

    // ---- File Merger ----
    val joinTitle get() = t("Penggabung File", "File Joiner")
    val joinSub get() = t("Gabungkan bagian bernomor (.001, .002, …) menjadi satu file", "Join numbered parts (.001, .002, …) back into one file")
    val joinPicker get() = t("Bagian file", "File parts")
    fun joinPicked(n: Int) = tt("$n bagian dipilih", "{0} parts selected", n)
    val joinHint get() = t(
        "Tahan sebuah file di pemilih file untuk memilih semua bagian (.001, .002, …) sekaligus. Urutan penggabungan mengikuti nomor di akhir nama, bukan urutan pilihan.",
        "Long-press a file in the file picker to select all parts (.001, .002, …) at once. Parts are joined in the order of the number at the end of the name, not the order you picked them.",
    )
    val joinOutName get() = t("Nama file hasil", "Output file name")
    val joinRun get() = t("Gabung File", "Join Files")
    val joinBusy get() = t("Menggabungkan…", "Joining…")
    fun joinInfo(n: Int, total: String, first: String, last: String) = tt("$n bagian · $total\n$first … $last", "{0} parts · {1}\n{2} … {3}", n, total, first, last,
    )
    fun notPart(n: String) = tt("\"$n\" bukan bagian bernomor (nama harus berakhiran .001, .002, …).", "\"{0}\" is not a numbered part (the name must end with .001, .002, …).", n)
    fun baseDiff(n: String) = tt("\"$n\" berasal dari file yang berbeda. Pilih bagian dari satu file saja.", "\"{0}\" belongs to a different file. Pick parts of one file only.", n)
    fun dup(n: String) = tt("Nomor bagian $n terpilih lebih dari sekali.", "Part number {0} was selected more than once.", n)
    fun missing(l: String) = tt("Bagian hilang: nomor $l.", "Missing parts: number {0}.", l)
    val onlyOne get() = t("Pilih minimal 2 bagian.", "Pick at least 2 parts.")
    val startWarn get() = t("Peringatan: urutan tidak dimulai dari .001, pastikan tidak ada bagian awal yang tertinggal.", "Warning: the sequence does not start at .001, make sure no leading part is missing.")
    fun joinDone(n: Int, name: String, size: String) = tt("Berhasil: $n bagian digabung menjadi $name ($size) di folder output.", "Done: {0} parts joined into {1} ({2}) in the output folder.", n, name, size,
    )

    // ---- Bulk Rename ----
    val renTitle get() = t("Rename Massal", "Batch Rename")
    val renSub get() = t("Ganti nama banyak file sekaligus, dengan pratinjau sebelum diterapkan", "Rename many files at once, with a preview before applying")
    val renFolder get() = t("Folder berisi file", "Folder with the files")
    val renNoFolder get() = t("Pilih folder dulu.", "Pick a folder first.")
    val renExt get() = t("Hanya ekstensi (opsional)", "Only extensions (optional)")
    val renExtHint get() = t("Contoh: png, jpg. Kosongkan untuk semua file.", "Example: png, jpg. Leave empty for all files.")
    val renModes get() = listOf(t("Cari & Ganti", "Find & Replace"), t("Pola Nomor", "Number Pattern"), t("Awalan/Akhiran", "Prefix/Suffix"), t("Perbaiki Nama Rusak", "Fix Garbled Names"))
    val renFind get() = t("Cari", "Find")
    val renReplace get() = t("Ganti dengan", "Replace with")
    val renCaseMatch get() = listOf(t("Bedakan huruf", "Match case"), t("Abaikan huruf", "Ignore case"))
    val renPattern get() = t("Pola nama", "Name pattern")
    val renPatternHint get() = t("{nama} = nama asli tanpa ekstensi, {no} = nomor urut (dimulai ulang di tiap folder). Ekstensi file selalu dipertahankan.", "{nama} = original name without extension, {no} = sequence number (restarts in each folder). The file extension is always kept.")
    val renStart get() = t("Mulai dari", "Start at")
    val renDigits get() = t("Jumlah digit", "Digits")
    val renPrefix get() = t("Awalan", "Prefix")
    val renSuffix get() = t("Akhiran (sebelum ekstensi)", "Suffix (before extension)")
    val renLetters get() = listOf(t("Huruf asli", "Keep case"), t("huruf kecil", "lowercase"), t("HURUF BESAR", "UPPERCASE"))
    val renPreview get() = t("Pratinjau", "Preview")
    fun renSummary(total: Int, change: Int, same: Int, bad: Int) = tt("$total file · $change diubah · $same tetap · $bad bermasalah", "{0} files · {1} renamed · {2} unchanged · {3} with problems", total, change, same, bad,
    )
    val renSearch get() = t("Cari nama file di pratinjau", "Search file names in preview")
    val renNoMatch get() = t("Tidak ada file yang cocok dengan pencarian.", "No files match the search.")
    fun renPageLabel(page: Int, pages: Int, total: Int) = tt("Halaman $page / $pages · $total file", "Page {0} / {1} · {2} files", page, pages, total)
    val renEmpty get() = t("Tidak ada file yang cocok di folder ini.", "No matching files in this folder.")
    val renBad get() = t("Ada nama yang bentrok atau tidak valid (ditandai merah). Ubah pengaturan supaya bisa diterapkan.", "Some names clash or are invalid (marked red). Change the settings so it can be applied.")
    val renRun get() = t("Ganti Nama", "Rename")
    val renBusy get() = t("Mengganti nama…", "Renaming…")
    fun renConfirmTitle(n: Int) = tt("Ganti nama $n file?", "Rename {0} files?", n)
    val renConfirmMsg get() = t("Nama file di folder akan diubah. Setelah selesai kamu masih bisa membatalkannya dengan tombol Urungkan.", "File names in the folder will change. When it finishes you can still revert with the Undo button.")
    val renNo get() = t("Batal", "Cancel")
    fun renDone(n: Int) = tt("Berhasil: $n file diganti namanya.", "Done: {0} files renamed.", n)
    fun renPartial(n: Int, failed: Int) = tt("$n file berhasil diganti namanya, $failed gagal dan dikembalikan ke nama lama.", "{0} files renamed, {1} failed and were restored to their old names.", n, failed,
    )
    val renUndo get() = t("Urungkan", "Undo")
    fun renUndone(n: Int) = tt("Dikembalikan: $n file ke nama sebelumnya.", "Restored: {0} files to their previous names.", n)
    // ---- File Cutter: middle mode ----
    val cutMid get() = t("Tengah", "Middle")
    val cutOffset get() = t("Offset awal", "Start offset")
    val cutOffsetBeyond get() = t("Offset melebihi ukuran file.", "The offset is beyond the end of the file.")

    // ---- Splitter/Merger: checksum & folder ----
    val sumOpts get() = listOf(t("Buat checksum (.sha256)", "Create checksum (.sha256)"), t("Tanpa checksum", "No checksum"))
    fun splitSumNote(base: String) = tt("\nChecksum SHA-256 disimpan di $base.sha256.", "\nSHA-256 checksum saved as {0}.sha256.", base)
    val joinFolderPicker get() = t("Atau pilih folder berisi bagian", "Or pick a folder with parts")
    val joinFolderHint get() = t(
        "Pilih juga berkas .sha256 bila ada untuk memverifikasi hasil. Dengan memilih folder, semua set bagian bernomor di dalamnya terdeteksi otomatis (satu tingkat) dan berkas .sha256 yang cocok dipakai otomatis.",
        "Also pick the .sha256 file if there is one to verify the result. When you pick a folder, all numbered part sets inside are detected automatically (one level) and a matching .sha256 file is used automatically.",
    )
    val joinNoSets get() = t("Tidak ada bagian bernomor (.001, .002, …) di folder ini.", "No numbered parts (.001, .002, …) in this folder.")
    fun joinSetInfo(n: Int, size: String) = tt("$n bagian · $size", "{0} parts · {1}", n, size)
    val joinSetOk get() = t("Siap digabung", "Ready to join")
    fun sumFound(name: String) = tt("Checksum ditemukan: $name", "Checksum found: {0}", name)
    fun joinRunAll(n: Int) = tt("Gabung $n set", "Join {0} sets", n)
    val verNone get() = t("tanpa verifikasi", "not verified")
    val verOk get() = t("checksum cocok", "checksum matches")
    fun verBadPart(p: String) = tt("checksum TIDAK cocok pada $p", "checksum does NOT match on {0}", p)
    val verBadWhole get() = t("checksum file utuh TIDAK cocok", "whole-file checksum does NOT match")
    val verEmpty get() = t("checksum tidak memuat bagian ini", "checksum has no entry for these parts")
    fun joinLineOk(name: String, size: String, ver: String) = t("✓ $name ($size) — $ver", "✓ $name ($size) — $ver")
    fun joinLineBad(name: String, ver: String) = tt("✗ $name — $ver (file hasil mungkin rusak)", "✗ {0} — {1} (the result may be corrupt)", name, ver)
    fun joinLineErr(name: String, msg: String) = tt("✗ $name — gagal: $msg", "✗ {0} — failed: {1}", name, msg)

    // ---- Bulk Rename: subfolders & fixing broken names ----
    val renRule get() = t("Aturan", "Rule")
    val renRecursive get() = listOf(t("Hanya folder ini", "This folder only"), t("Termasuk subfolder", "Include subfolders"))
    val renWrong get() = t("Nama terbaca salah sebagai", "Names were wrongly read as")
    val renRight get() = t("Seharusnya penyandian", "Should be encoding")
    val renFixHint get() = t(
        "Untuk nama file yang rusak (mis. ƒXƒNƒŠƒvƒg) akibat salah penyandian saat bongkar arsip. Nama yang sudah benar tidak diubah. Nama folder tidak ikut diperbaiki.",
        "For garbled file names (e.g. ƒXƒNƒŠƒvƒg) caused by a wrong encoding when extracting archives. Names that are already correct are left alone. Folder names are not repaired.",
    )

    // ---- Duplicate File Finder ----
    val dupTitle get() = t("Pencari File Duplikat", "Duplicate File Finder")
    val dupSub get() = t("Cari file kembar (isi sama persis) di satu folder beserta subfoldernya", "Find identical files in a folder and its subfolders")
    val dupPicker get() = t("Folder yang dipindai", "Folder to scan")
    val dupHint get() = t(
        "Pilih folder, aplikasi mengelompokkan file berdasarkan ukuran lalu membandingkan isinya dengan SHA-256. Di tiap grup satu file selalu dipertahankan. Penghapusan bersifat permanen.",
        "Pick a folder; the app groups files by size and then compares their content with SHA-256. One file in each group is always kept. Deletion is permanent.",
    )
    val dupListing get() = t("Membaca isi folder…", "Reading the folder…")
    fun dupHashing(done: Int, total: Int) = tt("Memeriksa isi file… $done/$total", "Checking file contents… {0}/{1}", done, total)
    val dupNone get() = t("Tidak ada file duplikat di folder ini.", "No duplicate files in this folder.")
    fun dupSummary(groups: Int, files: Int, size: String) = tt("$groups grup duplikat · $files file kelebihan · $size bisa dihemat", "{0} duplicate groups · {1} extra files · {2} can be saved", groups, files, size,
    )
    fun dupGroup(n: Int, size: String) = tt("$n file · masing-masing $size", "{0} files · {1} each", n, size)
    fun dupSelected(n: Int, size: String) = tt("Terpilih $n file ($size)", "{0} files selected ({1})", n, size)
    val dupKeepOne get() = t("Pilih semua kecuali satu", "Select all but one")
    val dupClear get() = t("Kosongkan pilihan", "Clear selection")
    fun dupDelete(n: Int) = tt("Hapus $n file terpilih", "Delete {0} selected files", n)
    fun dupConfirmTitle(n: Int) = tt("Hapus $n file?", "Delete {0} files?", n)
    val dupConfirmMsg get() = t("File yang dipilih dihapus permanen dan tidak bisa dikembalikan. Di setiap grup minimal satu file tetap ada.", "Selected files are deleted permanently and cannot be restored. At least one file stays in every group.")
    val dupYes get() = t("Hapus", "Delete")
    fun dupDone(n: Int, failed: Int) = if (failed == 0) tt("Berhasil: $n file dihapus.", "Done: {0} files deleted.", n) else tt("$n file dihapus, $failed gagal dihapus.", "{0} files deleted, {1} could not be deleted.", n, failed)
    val cancel get() = t("Batal", "Cancel")

    // ---- Hex Viewer ----
    val hexTitle get() = t("Penampil Hex", "Hex Viewer")
    val hexSub get() = t("Lihat isi file mentah per offset, cari teks atau byte", "View raw file content by offset, search text or bytes")
    val hexPicker get() = t("File", "File")
    val hexPerRow get() = listOf(t("8 byte/baris", "8 bytes/row"), t("16 byte/baris", "16 bytes/row"))
    fun hexRange(from: String, to: String, total: String) = tt("Offset $from – $to dari $total", "Offset {0} – {1} of {2}", from, to, total)
    /** Page navigation symbols: first, back, forward, last. */
    val hexNav get() = listOf("«", "‹", "›", "»")
    val hexJump get() = t("Ke offset (hex)", "Go to offset (hex)")
    val hexGo get() = t("Pergi", "Go")
    val hexQuery get() = t("Teks atau byte yang dicari", "Text or bytes to find")
    val hexModes get() = listOf(t("Teks UTF-8", "UTF-8 text"), t("Teks Shift-JIS", "Shift-JIS text"), t("Hex", "Hex"))
    val hexSearch get() = t("Cari", "Find")
    val hexSearchNext get() = t("Cari berikutnya", "Find next")
    val hexSearching get() = t("Mencari…", "Searching…")
    fun hexFound(off: String) = tt("Ditemukan di offset $off (ditampilkan di baris pertama).", "Found at offset {0} (shown on the first row).", off)
    val hexNotFound get() = t("Tidak ditemukan sampai akhir file.", "Not found up to the end of the file.")
    val hexBadQuery get() = t("Isi pencarian tidak valid (hex harus genap, maksimal 64 byte).", "Invalid search (hex must have an even length, 64 bytes at most).")
    val hexBadOffset get() = t("Offset hex tidak valid.", "Invalid hex offset.")
    val hexHint get() = t("Offset dan pencarian hex memakai bilangan hex, contoh pencarian: 4D 5A 90.", "Offsets and hex search use hexadecimal, e.g. search: 4D 5A 90.")
    val hexEmpty get() = t("File kosong.", "The file is empty.")
}

fun fileToolsStr(lang: Lang) = FileToolsStr(lang == Lang.ID, lang)
