package com.furuarc.i18n

import com.furuarc.core.FtKind
import com.furuarc.core.FtLevel
import com.furuarc.core.RtKind
import com.furuarc.core.RtLine
import com.furuarc.core.FtResult
import com.furuarc.settings.Lang

/** Text for the File & Folder Inspector menu (seven tools). Separated from [Str] because of the parameter limit. */
class CheckStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    // menu
    val menuTitle get() = t("Pemeriksa File dan Folder", "File & Folder Inspector")
    val menuSub get() = t("Periksa, bandingkan, dan pindai arsip, skrip, folder, dan terjemahan", "Inspect, compare and scan archives, scripts, folders and translations")
    val fileTitle get() = t("Pemeriksaan File", "File Inspection")
    val fileSub get() = t("Format, checksum, file rusak, enkripsi, dan kode salah pada satu file", "Format, checksum, corrupt files, encryption and wrong code in one file")
    val folderTitle get() = t("Pemeriksaan Folder", "Folder Inspection")
    val folderSub get() = t("Periksa semua arsip dan skrip dalam satu folder sekaligus", "Inspect every archive and script in a folder at once")
    val compareTitle get() = t("Bandingkan Pemeriksaan", "Compare Files")
    val compareSub get() = t("Bandingkan dua arsip, skrip, atau file terjemahan", "Compare two archives, scripts or translation files")
    val trScanTitle get() = t("Pemindaian Terjemahan", "Translation Scan")
    val trScanSub get() = t("Cek kode kontrol, baris panjang, entri kosong, dan terjemahan tidak konsisten", "Check control codes, long lines, empty entries and inconsistent translations")

    // Format Test
    val ftTitle get() = t("Uji Coba Format", "Format Test")
    val ftSub get() = t(
        "Uji format dengan arsip asli: ambil 1 file acak, kemas ulang, lalu bandingkan",
        "Test a format with a real archive: take one random file, pack it again and compare",
    )
    val ftHint get() = t(
        "Pilih engine (format), lalu unggah arsip asli dari engine itu. Aplikasi mengekstrak 1 file acak dari arsip, mengemasnya kembali menjadi arsip baru, membongkar arsip baru itu, dan membandingkannya dengan arsip aslimu. Tidak ada folder output; hasilnya tercatat di bawah.",
        "Choose an engine (format), then upload a real archive of that engine. The app extracts one random file from the archive, packs it into a new archive, unpacks that new archive and compares it with your original. There is no output folder; the result is recorded below.",
    )
    val ftNote get() = t(
        "Arsip hasil kemas hanya berisi file yang diuji, jadi ukurannya berbeda dari arsip asli. Uji coba ini membuktikan bahwa kemas dan bongkar bekerja untuk format ini; game asli tetap bisa menolak arsip bila engine-nya butuh kunci atau tata letak khusus.",
        "The new archive holds only the tested file, so its size differs from the original. The test shows that packing and unpacking work for this format; a real game may still reject an archive if its engine needs a key or a special layout.",
    )
    val ftStart get() = t("Mulai uji coba", "Start test")
    val ftAgain get() = t("Ulangi uji coba", "Run again")
    val ftStop get() = t("Hentikan", "Stop")
    fun ftProgress(d: Int, n: Int, name: String) = tt("Menguji $d/$n: $name…", "Testing {0}/{1}: {2}…", d, n, name)
    fun ftSummary(p: Int, w: Int, f: Int, s: Int) = tt("$p lulus · $w peringatan · $f gagal · $s dilewati", "{0} passed · {1} warnings · {2} failed · {3} skipped", p, w, f, s)
    val ftAllPass get() = t("SEMUA LULUS", "ALL PASSED")
    val ftStopped get() = t("Dihentikan sebelum selesai", "Stopped before finishing")
    val ftReportTitle get() = t("Laporan Uji Coba Format", "Format Test Report")

    // Format Test with a real archive
    val ftPassed get() = t("LULUS", "PASSED")
    val ftEngine get() = t("Engine (Format)", "Engine (Format)")
    val ftEnginePick get() = t("Pilih engine (format)", "Choose an engine (format)")
    val ftFile get() = t("Arsip untuk diuji", "Archive to test")
    val ftChecking get() = t("Memeriksa arsip…", "Checking the archive…")
    fun ftMatch(n: Int) = tt("✓ Format cocok · $n file di arsip", "✓ Format matches · {0} files in the archive", n)
    val ftUnpackSection get() = t("Bongkar Arsip", "Unpack Archive")
    val ftPackSection get() = t("Kemas Arsip", "Pack Archive")
    val ftMaxSize get() = t("Ukuran maksimal file yang diekstrak", "Maximum size of the extracted file")
    val ftMaxSizeBad get() = t("Isi ukuran maksimal dengan angka lebih dari 0.", "Enter a maximum size greater than 0.")
    val ftNoPackOptions get() = t("Format ini tidak punya pengaturan kemas.", "This format has no pack settings.")
    val ftNeedOptions get() = t("Beberapa format hanya bisa dibaca dengan pengaturan khusus (misalnya file indeks atau nama arsip). Buka Bongkar Arsip di bawah, isi pengaturannya, lalu arsip diperiksa ulang otomatis.", "Some formats can only be read with special settings (for example an index file or the archive name). Open Unpack Archive below, fill in the settings and the archive is checked again automatically.")
    val ftModeLabel get() = t("Jumlah file yang diuji", "Files to test")
    val ftModeOne get() = t("1 file acak", "One random file")
    val ftModeN get() = t("Beberapa file acak", "Several random files")
    val ftModeAll get() = t("Semua file (sampai batas ukuran)", "All files (up to the size limit)")
    val ftModeFull get() = t("Semua file + bandingkan byte demi byte dengan arsip asli", "All files + compare byte for byte with the original archive")
    val ftCount get() = t("Jumlah file acak", "Number of random files")
    val ftPick get() = t("Nama file tertentu (kosong = acak)", "Specific file name (empty = random)")
    val ftSeed get() = t("Seed acak (kosong = baru)", "Random seed (empty = new)")
    val ftSaveSample get() = t("Simpan sampel ke folder", "Save samples to a folder")
    val ftSampleSaved get() = t("Sampel disimpan", "Samples saved")
    val ftSampleFail get() = t("Gagal menyimpan sampel", "Could not save the samples")
    val ftTestThis get() = t("Uji format ini", "Test this format")
    val ftResults get() = t("Hasil uji coba format", "Format test results")

    private fun sz(n: Long): String = when {
        n < 1024 -> "$n B"
        n < 1024L * 1024 -> String.format(java.util.Locale.ROOT, "%.1f KB", n / 1024.0)
        else -> String.format(java.util.Locale.ROOT, "%.1f MB", n / 1048576.0)
    }

    /** One line of the real-archive Format Test. */
    fun rtDetail(l: RtLine): String = when (l.kind) {
        RtKind.PICKED -> if (l.n2 > 1) tt("${l.n2} file diuji: ${l.arg} (${sz(l.n1)})", "{0} files tested: {1} ({2})", l.n2, l.arg, sz(l.n1))
            else tt("File yang diuji: ${l.arg} (${sz(l.n1)})", "File tested: {0} ({1})", l.arg, sz(l.n1))
        RtKind.FULL_FORCED -> t("Format ini hanya bisa dikemas dengan semua file arsip (indeks menentukan isinya), jadi semua file diuji dan arsip baru dibandingkan byte demi byte", "This format can only be packed with all the files of the archive (an index decides the content), so all files are tested and the new archive is compared byte for byte")
        RtKind.SEED -> tt("Seed acak: ${l.n1} (isi kolom Seed dengan angka ini untuk mengulang uji yang sama)", "Random seed: {0} (enter it in the Seed field to repeat the same test)", l.n1)
        RtKind.NAME_NOT_FOUND -> tt("Tidak ada file bernama ${l.arg} di arsip", "No file named {0} in the archive", l.arg)
        RtKind.TOO_BIG -> tt("Data terlalu besar untuk mode ini (${sz(l.n1)}; batas ${sz(l.n2)})", "Too much data for this mode ({0}; limit {1})", sz(l.n1), sz(l.n2))
        RtKind.STRUCT_SAME -> t("Arsip baru identik dengan arsip asli, byte demi byte", "The new archive is identical to the original, byte for byte")
        RtKind.STRUCT_DATA -> tt("Beda dengan arsip asli mulai dari offset ${l.n1}, di dalam data ${l.arg}; ${l.n2} byte berbeda (bisa wajar bila format menyimpan waktu, acak, atau urutan lain)", "Differs from the original from offset {0}, inside the data of {1}; {2} bytes differ (can be normal if the format stores a time, random data or another order)", l.n1, l.arg, l.n2)
        RtKind.STRUCT_INDEX -> tt("Beda dengan arsip asli mulai dari offset ${l.n1}, di area header/indeks; ${l.n2} byte berbeda (bisa wajar bila format menyimpan waktu, acak, atau urutan lain)", "Differs from the original from offset {0}, in the header / index area; {1} bytes differ (can be normal if the format stores a time, random data or another order)", l.n1, l.n2)
        RtKind.NO_FILE_FITS -> tt("Tidak ada file ≤ ${sz(l.n1)} di arsip (${l.n2} file). Naikkan ukuran maksimal.", "No file ≤ {0} in the archive ({1} files). Raise the maximum size.", sz(l.n1), l.n2)
        RtKind.UNPACKED -> tt("Diekstrak dari arsip asli: ${sz(l.n1)} · ${l.n2} ms", "Extracted from the original archive: {0} · {1} ms", sz(l.n1), l.n2)
        RtKind.PACKED -> tt("Dikemas menjadi arsip baru: ${sz(l.n1)} · ${l.n2} ms", "Packed into a new archive: {0} · {1} ms", sz(l.n1), l.n2)
        RtKind.CONTENT_OK -> if (l.n1 > 1) tt("Arsip baru dibongkar lagi dan ${l.n1} file sama persis (SHA-256 file pertama ${l.arg}…)", "The new archive was unpacked again and all {0} files are identical (SHA-256 of the first {1}…)", l.n1, l.arg)
            else tt("Arsip baru dibongkar lagi dan isi file sama persis (SHA-256 ${l.arg}…)", "The new archive was unpacked again and the file is identical (SHA-256 {0}…)", l.arg)
        RtKind.CONTENT_BAD -> tt("Isi file berbeda setelah dibongkar dari arsip baru: ${l.arg}", "File content differs after unpacking the new archive: {0}", l.arg)
        RtKind.COUNT -> tt("Arsip baru berisi ${l.n1} file, seharusnya ${l.n2}", "The new archive holds {0} files, expected {1}", l.n1, l.n2)
        RtKind.NAME -> tt("Nama file di arsip baru berbeda: ${l.arg}", "The file name in the new archive differs: {0}", l.arg)
        RtKind.HEADER -> tt("Header dibanding arsip asli: ${l.n1} dari ${l.n2} byte pertama sama", "Header compared with the original: first {0} of {1} bytes are equal", l.n1, l.n2) +
            (if (l.level == FtLevel.WARN) t(" (tanda pengenal format hilang)", " (format signature lost)") else "")
        RtKind.NOT_DETECTED -> t("Arsip baru tidak dikenali sebagai format ini oleh deteksi otomatis", "The new archive is not recognised as this format by automatic detection")
        RtKind.DETECTED_BY -> tt("Arsip baru juga cocok dengan: ${l.arg}", "The new archive also matches: {0}", l.arg)
        RtKind.SIZES -> tt("Ukuran: arsip asli ${sz(l.n1)}, arsip baru ${sz(l.n2)}", "Sizes: original archive {0}, new archive {1}", sz(l.n1), sz(l.n2))
        RtKind.ERROR_UNPACK -> tt("Gagal mengekstrak dari arsip asli: ${l.arg}", "Could not extract from the original archive: {0}", Tr.msg(lang, l.arg))
        RtKind.ERROR_PACK -> tt("Gagal mengemas: ${l.arg}", "Could not pack: {0}", Tr.msg(lang, l.arg))
        RtKind.ERROR_READ -> tt("Gagal membaca arsip baru: ${l.arg}", "Could not read the new archive: {0}", Tr.msg(lang, l.arg))
        RtKind.CANCELLED -> t("Dihentikan sebelum selesai", "Stopped before finishing")
    }

    /** Lists every format that accepts the archive when more than one does. */
    private fun detectNote(r: FtResult): String =
        if (r.detectedBy.size > 1) tt(" · cocok dengan: ${r.detectedBy.joinToString(", ")}", " · also accepted by: {0}", r.detectedBy.joinToString(", ")) else ""

    /** One line that explains a result of the Format Test. */
    fun ftDetail(r: FtResult): String = when (r.kind) {
        FtKind.OK -> tt("${r.files} file · ${(r.bytes + 512) / 1024} KB · ${r.ms} ms", "{0} files · {1} KB · {2} ms", r.files, (r.bytes + 512) / 1024, r.ms) + detectNote(r)
        FtKind.ERROR -> tt("Gagal: ${r.arg}", "Failed: {0}", Tr.msg(lang, r.arg))
        FtKind.COUNT -> tt("Jumlah file berbeda: seharusnya ${r.n1}, hasil ${r.n2} (${r.arg})", "Different number of files: expected {0}, got {1} ({2})", r.n1, r.n2, r.arg)
        FtKind.MISSING -> tt("File tidak kembali setelah dibongkar: ${r.arg}", "File did not come back after unpacking: {0}", r.arg)
        FtKind.CONTENT -> tt("Isi file berbeda setelah dibongkar: ${r.arg}", "File content differs after unpacking: {0}", r.arg)
        FtKind.DETECT_OTHER -> tt("Lulus, tetapi deteksi otomatis memilih ${r.arg}", "Passed, but automatic detection picks {0}", r.arg) + detectNote(r)
        FtKind.DETECT_NONE -> t("Lulus, tetapi deteksi otomatis tidak mengenali arsip ini", "Passed, but automatic detection does not recognise this archive")
        FtKind.REPACK_DIFF -> t("Lulus, tetapi kemas ulang dari hasil bongkar menghasilkan arsip yang berbeda byte-nya (isi file sama; mungkin wajar bila format menyimpan waktu atau acak)", "Passed, but packing the unpacked files again gives a different archive (same files; can be normal if the format stores a time or random data)")
        FtKind.REPACK_CONTENT -> tt("Arsip hasil kemas ulang berisi data berbeda: ${r.arg}", "The archive packed again has different data: {0}", r.arg)
        FtKind.HASH_MISMATCH -> tt("SHA-256 file tidak cocok dengan yang diharapkan: ${r.arg}", "File SHA-256 does not match the expected one: {0}", r.arg)
        FtKind.SCRIPT_EMPTY -> t("Tidak ada teks yang ditemukan di skrip contoh (contoh mungkin tidak sesuai dengan format skrip)", "No text was found in the sample script (the sample may not match the script format)")
        FtKind.SCRIPT_CHANGED -> t("Impor tanpa perubahan teks mengubah skrip", "Importing unchanged texts changed the script")
        FtKind.SCRIPT_APPLY -> tt("Impor teks yang diubah tidak berhasil: ${r.arg}", "Importing changed texts did not work: {0}", r.arg)
        FtKind.NEEDS_GAME_FILES -> t("Dilewati: butuh file dari game asli (file indeks atau hasil bongkar arsip aslinya)", "Skipped: needs files from a real game (an index file or the unpacked original archive)")
    }

    // status
    val verdictOk get() = t("SEHAT", "HEALTHY")
    val verdictWarn get() = t("ADA PERINGATAN", "WARNINGS")
    val verdictBad get() = t("ADA MASALAH", "PROBLEMS FOUND")
    fun verdictDetail(w: Int, b: Int) = tt("$b masalah · $w peringatan", "{0} problems · {1} warnings", b, w)

    // Archive and Script Check
    val unpackThis get() = t("Bongkar arsip ini", "Unpack this archive")
    val hashMatchLabel get() = t("Cocokkan dengan hash dari sumber", "Match with a hash from the source")
    val hashMatchHint get() = t("Tempel CRC32, MD5, atau SHA-256", "Paste a CRC32, MD5 or SHA-256")
    fun hashMatchOk(algo: String) = tt("✓ Cocok dengan $algo file ini", "✓ Matches this file's {0}", algo)
    val hashMatchBad get() = t("✕ Tidak cocok dengan CRC32, MD5, maupun SHA-256 file ini", "✕ Does not match this file's CRC32, MD5 or SHA-256")
    val hashMatchShort get() = t("Panjang hash tidak lazim (CRC32 = 8, MD5 = 32, SHA-256 = 64 karakter).", "Unusual hash length (CRC32 = 8, MD5 = 32, SHA-256 = 64 characters).")

    // report export
    val exportCopy get() = t("Salin laporan", "Copy report")
    val exportSave get() = t("Ekspor .txt", "Export .txt")
    val copied get() = t("Laporan disalin", "Report copied")
    val saved get() = t("Laporan disimpan", "Report saved")
    val saveFail get() = t("Gagal menyimpan laporan", "Could not save the report")

    // Folder Check
    val folderPicker get() = t("Folder yang diperiksa", "Folder to inspect")
    val folderHint get() = t("Pilih folder game. Semua arsip dan skrip di dalamnya diperiksa (hanya indeks/awal file, jadi cepat). Gambar, audio, dan video biasa dilewati.", "Pick a game folder. Every archive and script inside is checked (index/file start only, so it is fast). Plain image, audio and video files are skipped.")
    fun folderProgress(d: Int, n: Int) = tt("Memeriksa $d/$n…", "Checking {0}/{1}…", d, n)
    val folderStatsTitle get() = t("Isi folder", "Folder contents")
    val folderSummary get() = t("Ringkasan", "Summary")
    fun folderCounts(n: Int, ok: Int, w: Int, b: Int) = tt("$n file diperiksa · $ok sehat · $w peringatan · $b masalah", "{0} files checked · {1} healthy · {2} warnings · {3} problems", n, ok, w, b)
    fun folderSkipped(n: Int) = tt("$n gambar/audio/video biasa dilewati", "{0} plain image/audio/video files skipped", n)
    fun folderRemaining(n: Int) = tt("$n file belum diperiksa (pemeriksaan per batch 5000 file).", "{0} files not checked yet (checked in batches of 5000).", n)
    fun folderNextBatch(n: Int) = tt("Lanjutkan batch berikutnya ($n tersisa)", "Continue with the next batch ({0} left)", n)
    val folderSearch get() = t("Cari file (nama, jenis, atau keterangan)", "Search files (name, type or note)")
    fun folderSize(bytes: String) = tt("Ukuran folder: $bytes", "Folder size: {0}", bytes)
    fun folderFileFolder(files: Int, folders: Int) = tt("$files file di $folders folder", "{0} files in {1} folders", files, folders)
    fun folderEmptyFiles(n: Int) = tt("$n file kosong (0 byte)", "{0} empty files (0 bytes)", n)
    val folderLargest get() = t("File terbesar", "Largest files")
    val folderExts get() = t("Ekstensi terbanyak", "Top extensions")
    val folderEngine get() = t("Engine game terdeteksi", "Detected game engine")
    val folderEngines get() = t("Folder ini memuat file dari beberapa engine", "This folder contains files from several engines")
    val folderEngineNone get() = t("Engine game tidak dikenali dari nama file", "No game engine recognised from the file names")
    fun folderEngineLine(name: String, n: Int, samples: String) = tt("$name — $n file ($samples)", "{0} — {1} files ({2})", name, n, samples)
    val folderEngineNote get() = t("Pengenalan engine berdasarkan nama/ekstensi file dan format arsip yang terbaca; ekstensi umum bisa saja salah tebak.", "Engines are recognised from file names/extensions and readable archive formats; common extensions can be misleading.")
    fun pageLabel(page: Int, pages: Int, total: Int) = tt("Halaman $page / $pages · $total file", "Page {0} / {1} · {2} files", page, pages, total)
    val filterAll get() = t("Semua", "All")
    val filterWarn get() = "!"
    val filterBad get() = "✕"
    val filterOk get() = "✓"
    val folderNone get() = t("Tidak ada file yang cocok dengan filter.", "No files match the filter.")
    val tapToOpen get() = t("Ketuk file untuk melihat hasil lengkapnya.", "Tap a file to see its full result.")

    // Compare
    val labelA get() = t("File A (lama)", "File A (old)")
    val labelB get() = t("File B (baru)", "File B (new)")
    val compareBtn get() = t("Bandingkan", "Compare")
    val compareHint get() = t("Pilih dua file dengan jenis sama: dua arsip, dua skrip/teks, atau dua file terjemahan (CSV/TXT/JSON).", "Pick two files of the same kind: two archives, two scripts/text files, or two translation files (CSV/TXT/JSON).")
    val modeFast get() = t("Cepat", "Fast")
    val modeDeep get() = t("Teliti", "Thorough")
    val modeNote get() = t("Cepat: membandingkan nama dan ukuran file di dalam arsip. Teliti: juga membandingkan isi tiap file yang ukurannya sama (lebih lama).", "Fast: compares file names and sizes inside archives. Thorough: also compares the content of same-size files (slower).")
    val added get() = t("Ditambah", "Added")
    val changed get() = t("Berubah", "Changed")
    val removed get() = t("Dihapus", "Removed")
    val same get() = t("Sama", "Same")
    val origChanged get() = t("Teks asli berubah", "Original changed")
    val trChanged get() = t("Terjemahan berubah", "Translation changed")
    val identical get() = t("✓ Kedua file identik", "✓ Both files are identical")
    val differ get() = t("✕ Kedua file berbeda", "✕ The files differ")
    val textNote get() = t("Perbandingan teks menghitung baris yang ditambah/dihapus tanpa memperhatikan urutan baris.", "The text comparison counts added/removed lines without regard to line order.")
    fun textSummary(a: Int, b: Int, same: Int, add: Int, rem: Int) = tt("Baris A: $a · Baris B: $b · sama: $same · ditambah: $add · hilang: $rem", "Lines A: {0} · Lines B: {1} · same: {2} · added: {3} · removed: {4}", a, b, same, add, rem)
    fun trSummary(ta: Int, tb: Int, fa: Int, fb: Int) = tt("Entri A/B: $ta / $tb · Terisi A/B: $fa / $fb", "Entries A/B: {0} / {1} · Filled A/B: {2} / {3}", ta, tb, fa, fb)
    val addedLines get() = t("Baris ditambah di B", "Lines added in B")
    val removedLines get() = t("Baris hilang dari A", "Lines removed from A")
    val diffList get() = t("Perbedaan", "Differences")
    val searchHint get() = t("Cari…", "Search…")

    // Translation Scan
    val trPicker get() = t("File terjemahan (CSV/TXT/JSON)", "Translation file (CSV/TXT/JSON)")
    val trHint get() = t("Pilih hasil ekspor terjemahan dari FuruArc (RPGM maupun VN).", "Pick a translation export made by FuruArc (RPGM or VN).")
    val trMaxLen get() = t("Batas panjang baris (karakter)", "Line length limit (characters)")
    val trTotal get() = t("Total entri", "Total entries")
    val trFilled get() = t("Terisi", "Filled")
    val trEmpty get() = t("Kosong", "Empty")
    val trSame get() = t("Sama dengan asli", "Same as original")
    val trCtrl get() = t("Kode kontrol berbeda", "Control codes differ")
    val trLong get() = t("Baris terlalu panjang", "Lines too long")
    val trIncons get() = t("Tidak konsisten", "Inconsistent")
    val trInconsNote get() = t("Teks asli sama tetapi terjemahannya berbeda-beda.", "Same original text but different translations.")
    val trLongest get() = t("Baris terpanjang", "Longest line")
    val trDupKeys get() = t("Kunci (file+path) ganda", "Duplicate keys (file+path)")
    val trShowing get() = t("Menampilkan", "Showing")
    val trOrig get() = t("Asli", "Original")
    val trTr get() = t("Terjemahan", "Translation")
    // ---- File Cutter ----
    val cutTitle get() = t("Pemotong File", "File Cutter")
    val cutSub get() = t("Ambil bagian awal, tengah, atau akhir file besar menjadi file kecil, misalnya untuk dikirim sebagai sampel", "Take the start, middle or end of a large file as a small file, e.g. to send as a sample")
    val cutPicker get() = t("File sumber", "Source file")
    val cutPart get() = t("Bagian yang diambil", "Part to take")
    val cutHead get() = t("Awal file", "Start")
    val cutTail get() = t("Akhir file", "End")
    val cutSize get() = t("Ukuran bagian", "Part size")
    val cutAmount get() = t("Jumlah", "Amount")
    val cutHint get() = t("Indeks arsip biasanya ada di awal file (kadang di akhir). 1-2 MB dari awal biasanya cukup untuk menganalisis struktur arsip.", "An archive index is usually at the start of the file (sometimes at the end). 1-2 MB from the start is usually enough to analyse the structure.")
    val cutOutName get() = t("Nama file hasil", "Output file name")
    val cutOutFolder get() = t("Folder Output", "Output Folder")
    val cutRun get() = t("Potong File", "Cut File")
    val cutNoFile get() = t("Pilih file sumber dulu.", "Pick the source file first.")
    fun cutInfo(n: String, size: String) = t("$n · $size", "$n · $size")
    fun cutDone(name: String, bytes: String) = tt("Berhasil: $name ($bytes) disimpan di folder output.", "Done: {0} ({1}) saved in the output folder.", name, bytes)
    fun cutFail(m: String) = tt("Gagal: $m", "Failed: {0}", m)
    val cutClose get() = t("Tutup", "Close")
    val cutBusy get() = t("Memotong…", "Cutting…")
    // ---- Key Finder ----
    val kfTitle get() = t("Pencari Kunci", "Key Finder")
    val kfSub get() = t("Cari kunci enkripsi di folder game dan daftar file yang memakainya", "Find the encryption key in a game folder and the files that use it")
    val kfPicker get() = t("Folder game", "Game folder")
    val kfHint get() = t("Pilih folder game lalu tekan Analisis. Aplikasi memindai seluruh isi folder, mencari kunci enkripsi, dan menampilkan file mana saja yang terenkripsi dengan kunci itu. Semua dilakukan offline.", "Pick the game folder and press Analyze. The app scans the whole folder, looks for the encryption key and shows which files are encrypted with it. Everything runs offline.")
    val kfAnalyze get() = t("Analisis", "Analyze")
    val kfScope get() = t("Mencari", "Looking for")
    val kfScopeAll get() = t("Semua engine yang didukung", "All supported engines")
    fun kfProgress(label: String, n: Int, total: Int) = tt("Memindai: $label ($n/$total)", "Scanning: {0} ({1}/{2})", label, n, total)
    fun kfFound(n: Int, files: Int) = tt("$n kunci ditemukan · $files file dipindai", "{0} keys found · {1} files scanned", n, files)
    val kfNoKey get() = t("Tidak ada kunci yang bisa ditemukan.", "No key could be found.")
    val kfVerified get() = t("Terverifikasi", "Verified")
    val kfCandidate get() = t("Kandidat", "Candidate")
    val kfKey get() = t("Kunci", "Key")
    val kfSource get() = t("Ditemukan di", "Found in")
    val kfCopyKey get() = t("Salin kunci", "Copy key")
    val kfKeyCopied get() = t("Kunci disalin", "Key copied")
    fun kfFiles(n: Int) = tt("File terenkripsi ($n)", "Encrypted files ({0})", n)
    val kfNotes get() = t("Catatan", "Notes")
    fun kfPage(page: Int, pages: Int) = tt("Halaman $page / $pages", "Page {0} / {1}", page, pages)
    val kfUseHint get() = t("Tempel kunci ini di opsi Kunci saat Bongkar/Kemas Arsip bila diminta.", "Paste this key into the Key option when Unpack/Pack asks for it.")
    val trAllGood get() = t("✓ Tidak ada temuan di kategori ini.", "✓ Nothing found in this category.")
}

fun checkStr(lang: Lang) = CheckStr(lang == Lang.ID, lang)
