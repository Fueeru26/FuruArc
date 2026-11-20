package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks menu Konverter Aset Media (Audio, Gambar, Video, Encoding Teks). */
class MediaStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    // ---- menu ----
    val menuTitle get() = t("Konverter Aset Media", "Media Asset Converter")
    val menuSub get() = t("Ubah format audio, gambar, video, dan penyandian teks", "Convert audio, image and video formats, and text encoding")
    val mixTitle get() = t("Konverter Aset Campuran", "Mixed Asset Converter")
    val mixSub get() = t("Ubah format dan kompres audio, gambar, dan video sekaligus dalam satu folder", "Convert and compress audio, images and video together from one folder")
    val mixIntro get() = t(
        "Pilih satu folder yang isinya campuran audio, gambar, dan video. Aplikasi mengelompokkan file menurut ekstensinya, lalu tiap jenis memakai aturannya sendiri (format output, kompres, dan Pengaturan Lanjutan seperti di konverter Audio, Gambar, dan Video). Aturan tiap jenis dibuka dengan mengetuk judulnya; sakelar di kanan menentukan apakah jenis itu diproses. Jenis yang dimatikan tidak disentuh, dan jenis yang tidak ada di folder otomatis nonaktif. Audio dan gambar diproses lebih dulu, video terakhir (satu per satu). Pada mode Timpa File Asli, file yang formatnya tetap tertimpa di tempat, sedangkan file yang formatnya berubah menghasilkan file baru di sebelahnya. Konverter Teks dan Aset Khusus tidak termasuk.",
        "Pick one folder that holds a mix of audio, images and video. The app groups files by extension, and each type uses its own rules (output format, compression and Advanced Settings, same as the Audio, Image and Video converters). Tap a type's title to open its rules; the switch on the right decides whether that type is processed. Switched-off types are left untouched, and types missing from the folder are disabled automatically. Audio and images are processed first, video last (one at a time). In Overwrite Originals mode files whose format stays the same are replaced in place, while files whose format changes produce a new file next to them. The Text and Special converters are not included.",
    )
    fun mixScan(na: Long, sa: String, ni: Long, si: String, nv: Long, sv: String, nu: Long) = tt("Isi folder: Audio $na file ($sa) · Gambar $ni file ($si) · Video $nv file ($sv) · Tidak dikenali $nu file", "Folder contents: Audio {0} files ({1}) · Images {2} files ({3}) · Video {4} files ({5}) · Unrecognized {6} files", na, sa, ni, si, nv, sv, nu,
    )
    val mixCopyOthers get() = t("Salin file tidak dikenali apa adanya", "Copy unrecognized files as they are")
    val mixCopyHint get() = t(
        "Bawaan mati: file yang bukan audio, gambar, atau video dilewati dan tidak muncul di folder output. Bila dicentang, file tersebut disalin tanpa diubah ke folder output (struktur folder dipertahankan) sehingga folder hasil lengkap. Tidak berlaku pada mode Timpa File Asli karena file asli sudah ada di tempatnya.",
        "Off by default: files that are not audio, images or video are skipped and do not appear in the output folder. When checked, they are copied unchanged into the output folder (folder structure kept) so the result folder is complete. Not used in Overwrite Originals mode because the originals are already in place.",
    )
    fun mixSec(name: String, n: Long) = tt("$name · $n file", "{0} · {1} files", name, n)
    val mixAudioName get() = t("Audio", "Audio")
    val mixImageName get() = t("Gambar", "Images")
    val mixVideoName get() = t("Video", "Video")
    val mixOffNote get() = t("Dilewati (sakelar mati)", "Skipped (switch off)")
    val mixNoneNote get() = t("Tidak ada file jenis ini di folder", "No files of this type in the folder")
    val mixNothing get() = t("Aktifkan minimal satu jenis (Audio, Gambar, atau Video) atau salin file tidak dikenali.", "Enable at least one type (Audio, Images or Video) or copy unrecognized files.")
    fun mixEstLine(name: String, n: Int, from: String, to: String) = tt("$name: $n file, $from → ± $to", "{0}: {1} files, {2} → ~{3}", name, n, from, to)
    val mixResize get() = t("Resolusi gambar", "Image resolution")
    val presetMixRpgm get() = t("RPG Maker MV/MZ (campuran)", "RPG Maker MV/MZ (mixed)")
    val presetMixRenpy get() = t("Ren'Py (campuran)", "Ren'Py (mixed)")
    val presetMixKiri get() = t("Kirikiri (campuran)", "Kirikiri (mixed)")
    val audioTitle get() = t("Konverter Aset Audio", "Audio Asset Converter")
    val audioSub get() = t("Ubah format audio dan kompres dengan bitrate, sample rate, kanal, dan volume", "Convert audio formats and compress with bitrate, sample rate, channels and volume")
    val imageTitle get() = t("Konverter Aset Gambar", "Image Asset Converter")
    val imageSub get() = t("Ubah format gambar dan kompres dengan kualitas, ukuran, dan rotasi", "Convert image formats and compress with quality, size and rotation")
    val videoTitle get() = t("Konverter Aset Video", "Video Asset Converter")
    val videoSub get() = t("Ubah format video dan kompres dengan resolusi, bitrate, dan frame rate", "Convert video formats and compress with resolution, bitrate and frame rate")
    val textTitle get() = t("Konverter Encoding Teks", "Text Encoding Converter")
    val textSub get() = t("Ubah file teks antara Shift-JIS/GBK/Big5 dan UTF-8", "Convert text files between Shift-JIS/GBK/Big5 and UTF-8")

    // ---- sumber, output, jalan ----
    val srcModes get() = listOf(t("File", "File"), t("Folder", "Folder"))
    val pickFiles get() = t("File sumber (boleh banyak)", "Source files (multiple allowed)")
    val pickFolder get() = t("Folder sumber", "Source folder")
    val recursive get() = listOf(t("Hanya folder ini", "This folder only"), t("Termasuk subfolder", "Include subfolders"))
    val scanning get() = t("Memindai folder…", "Scanning folder…")
    val folderEmpty get() = t("Tidak ada file yang cocok di folder ini.", "No matching files in this folder.")
    fun selected(n: Int, size: String) = tt("$n file dipilih · $size", "{0} file(s) selected · {1}", n, size)
    fun selectedFmt(n: Int, size: String, counts: String) = tt("$n file dipilih · $size · $counts", "{0} file(s) selected · {1} · {2}", n, size, counts)
    val fmtInput get() = t("Format Input", "Input format")
    val fmtAll get() = t("Semua Format Tersedia", "All Available Formats")
    val fmtMissing get() = t("Format yang dipilih tidak tersedia di dalam folder", "The selected format is not available in the folder")
    val folderInputHint get() = t("Jika di dalam folder berisi beberapa file dengan format berbeda, ubah Format Input untuk menentukan format file yang akan dikonversi. Struktur subfolder dipertahankan di folder output.", "If the folder contains files of different formats, change Input format to choose which files are converted. The subfolder structure is kept in the output folder.")
    fun folderTypes(list: String) = tt("Folder: hanya file bertipe $list. Struktur subfolder dipertahankan di folder output.", "Folder: only {0} files. The subfolder structure is kept in the output folder.", list)
    val extFilter get() = t("Hanya ekstensi", "Only extensions")
    val extFilterHint get() = t("Pisahkan dengan koma. Wajib diisi agar file biner tidak ikut terubah.", "Separate with commas. Required, so binary files are not touched.")
    val outFolder get() = t("Folder output", "Output folder")
    val conflict get() = t("Jika file hasil sudah ada", "If the result file already exists")
    val conflictOpts get() = listOf(t("Ganti nama", "Rename"), t("Timpa", "Overwrite"), t("Lewati", "Skip"))
    val run get() = t("Mulai Konversi", "Start Conversion")
    val cancel get() = t("Batal", "Cancel")
    val close get() = t("Tutup", "Close")
    val needOut get() = t("Pilih folder output dulu.", "Pick an output folder first.")
    fun progress(i: Int, total: Int, name: String) = tt("Memproses $i dari $total: $name", "Processing {0} of {1}: {2}", i, total, name)
    val resultTitle get() = t("Hasil", "Result")
    val cancelledNote get() = t("Dibatalkan oleh pengguna.", "Cancelled by the user.")
    val doneNote get() = t("Selesai.", "Finished.")
    fun counts(ok: Int, same: Int, skipped: Int, failed: Int) = tt("Berhasil: $ok · Tidak berubah: $same · Dilewati: $skipped · Gagal: $failed", "Converted: {0} · Unchanged: {1} · Skipped: {2} · Failed: {3}", ok, same, skipped, failed,
    )
    fun sizes(inB: String, outB: String) = tt("Ukuran: $inB → $outB", "Size: {0} → {1}", inB, outB)
    val errorsTitle get() = t("Yang gagal", "Failures")
    fun moreErrors(n: Int) = tt("… dan $n lainnya", "… and {0} more", n)
    val bgNote get() = t(
        "Disarankan untuk mengaktifkan \"Proses di Latar Belakang\" dan \"Layar tetap menyala saat proses berjalan\" di Pengaturan.",
        "Recommended: enable \"Background processing\" and \"Keep screen on while running\" in Settings.",
    )

    // ---- bersama: format Output, kompres ----
    val formatFollow get() = t("Ikuti Sumber", "Follow Source")
    val formatNA get() = t("Format Output tidak tersedia, silahkan pilih manual!", "Output format not available, please choose manually!")
    val compModes get() = listOf(t("Kualitas", "Quality"), t("Ukuran", "Size"))
    val compPercent get() = t("Kualitas", "Quality")
    val compSize get() = t("Estimasi ukuran", "Estimated size")
    val compOver get() = t("Ukuran yang dimasukkan melebihi ukuran file sumber!", "The entered size exceeds the source file size!")
    val compHint get() = t(
        "Kualitas: 100% = tanpa kompres. Ukuran: ukuran hasil per file dicari mendekati angka itu (perkiraan). Jika format Output berbeda, file dikonversi sekaligus dikompres. File yang sudah lebih kecil dari target tidak dikompres.",
        "Quality: 100% = no compression. Size: each result is tuned to be close to that number (approximate). If the output format differs, the file is converted and compressed in one pass. Files already smaller than the target are not compressed.",
    )
    val busyNote get() = t("Proses sedang berlangsung, silahkan batalkan terlebih dahulu", "A process is running, please cancel it first")
    val cancelRunConfirm get() = t("Batalkan proses konversi yang sedang berjalan? File yang sudah selesai dikonversi tetap tersimpan, file yang sedang diproses tidak ditulis.", "Cancel the running conversion? Files already converted are kept; the file being processed is not written.")
    fun discardBtn(n: Int) = tt("Buang hasil lebih besar ($n)", "Discard larger results ({0})", n)
    val discardHint get() = t(
        "Tombol ini muncul bila ada hasil yang ukurannya lebih besar dari file sumbernya (bisa terjadi saat kompres OGG (Vorbis), konversi ke WAV/FLAC, atau format lain yang memang lebih besar). Menekannya menghapus file hasil yang lebih besar tersebut. Bila hasil menimpa file asli dan ada cadangan di _backup, file asli dikembalikan. Hasil tanpa cadangan tidak bisa dibuang dan dibiarkan. File sumber tidak pernah dihapus.",
        "This button appears when some results are larger than their source files (this can happen with OGG (Vorbis) compression, conversion to WAV/FLAC, or any other format that is simply bigger). Pressing it deletes those larger result files. If a result replaced the original and a backup exists in _backup, the original is restored. Results without a backup cannot be discarded and are left as they are. Source files are never deleted.",
    )
    val discardTitle get() = t("Buang hasil lebih besar?", "Discard larger results?")
    fun discardConfirm(n: Int) = tt("$n file hasil yang lebih besar dari sumbernya akan dihapus (atau file asli dikembalikan dari _backup bila hasil menimpa file asli). Tindakan ini tidak bisa dibatalkan.", "{0} result files larger than their sources will be deleted (or the originals restored from _backup if the results replaced them). This cannot be undone.", n)
    val discardYes get() = t("Ya, buang", "Yes, discard")
    val discardNo get() = t("Tidak", "No")
    val backConfirm get() = t("Proses sedang berlangsung, yakin ingin membatalkan dan kembali?", "A process is running. Cancel it and go back?")
    val onlyOver get() = t("Hanya kompres jika lebih besar dari", "Only compress if larger than")
    val onlyOverKb get() = t("Batas ukuran file (KB)", "File size limit (KB)")
    val onlyOverHint get() = t("File yang ukurannya sama atau lebih kecil dari batas ini tidak dikompres (tetap dikonversi bila format berbeda).", "Files equal to or smaller than this limit are not compressed (still converted if the format differs).")
    fun estimate(from: String, to: String, pct: String) = tt("Perkiraan ukuran hasil: ± $to dari $from ($pct)", "Estimated result size: ~{0} from {1} ({2})", to, from, pct)
    val vorbisFloorNote get() = t(
        "OGG (Vorbis) tidak bisa lebih kecil dari sekitar 50 kbps (stereo), jadi file sumber yang bitrate-nya sudah rendah tidak akan mengecil dan hasilnya bisa lebih besar. Hasil yang lebih besar dari sumber bisa dibuang lewat tombol Buang hasil lebih besar setelah proses selesai. Untuk kompres lebih kecil pilih OGG (Opus) atau M4A.",
        "OGG (Vorbis) cannot go below about 50 kbps (stereo), so sources that are already low-bitrate will not shrink and the result can be larger. Results larger than the source can be removed with the Discard larger results button after the process finishes. For smaller files choose OGG (Opus) or M4A.",
    )
    val estimateNote get() = t("Perkiraan kasar; ukuran sebenarnya bisa berbeda.", "Rough estimate; the actual size may differ.")
    fun eta(pct: Int, left: String) = tt("$pct% · sisa waktu ± $left", "{0}% · about {1} left", pct, left)
    fun etaCalc() = t("Menghitung sisa waktu…", "Estimating time left…")
    fun compare(src: String, res: String, change: String) = tt("Sumber: $src\nHasil: $res\nPerubahan: $change", "Source: {0}\nResult: {1}\nChange: {2}", src, res, change)
    fun changeText(pct: Double) = if (Math.abs(pct) < 0.05) t("tidak berubah (0%)", "unchanged (0%)") else if (pct < 0) t("lebih kecil %.1f%%".format(-pct), "%.1f%% smaller".format(-pct)) else t("lebih besar %.1f%%".format(pct), "%.1f%% larger".format(pct))


    // ---- Audio ----
    val audFormat get() = t("Format Output", "Output format")
    val audFormats get() = listOf("M4A (AAC)", "OGG (Opus)", "WAV (PCM 16-bit)", "MP3", "FLAC", "OGG (Vorbis)")
    val audBitrate get() = t("Bitrate (kbps)", "Bitrate (kbps)")
    val audBitrateAuto get() = t("Otomatis", "Automatic")
    val audRate get() = t("Sample rate", "Sample rate")
    val audRateKeep get() = t("Ikuti sumber", "Keep source")
    val audChannels get() = listOf(t("Ikuti sumber", "Keep source"), t("Mono", "Mono"), t("Stereo", "Stereo"))
    val audVolume get() = t("Volume", "Volume")
    val audCompress get() = t("Kompres Audio", "Compress Audio")
    val audCompressHint get() = t("Kualitas: bitrate AAC/Opus/MP3/Vorbis dikalikan persen (WAV dan FLAC: sample rate diturunkan). Ukuran: bitrate dihitung dari target (WAV dan FLAC: sample rate dan kanal diturunkan; ukuran FLAC bisa lebih kecil dari target).", "Quality: AAC/Opus/MP3/Vorbis bitrate is multiplied by the percentage (WAV and FLAC: lower sample rate). Size: bitrate is calculated from the target (WAV and FLAC: lower sample rate and channels; a FLAC result may end up smaller than the target).")

    // ---- Gambar ----
    val imgFormat get() = t("Format Output", "Output format")
    val imgFormats get() = listOf("PNG", "JPG", "WebP")
    val imgCompress get() = t("Kompres Gambar", "Compress Image")
    val imgCompressHint get() = t("Kualitas: JPG/WEBP memakai kualitas sebesar slider; PNG dikompres menjadi PNG berpalet (jumlah warna = slider × 256, transparansi tetap). Ukuran: kualitas/jumlah warna dicari otomatis sampai hasil mendekati target.", "Quality: JPG/WEBP use the slider value as quality; PNG becomes a palette PNG (color count = slider × 256, transparency kept). Size: quality/color count is searched automatically until the result is near the target.")
    val imgQuality get() = t("Kualitas (1-100)", "Quality (1-100)")
    val imgQualityHint get() = t("PNG dan WEBP lossless tidak memakai kualitas (selalu tanpa kehilangan).", "PNG and lossless WEBP ignore quality (always lossless).")
    val imgResize get() = t("Resolusi", "Resolution")
    val imgResizeOpts get() = listOf(t("Asli", "Original"), t("Persen", "Percent"), t("Ukuran", "Size"))
    val imgPercent get() = t("Skala", "Scale")
    val imgWidth get() = "Width (px)"
    val imgHeight get() = "Height (px)"
    val imgKeepRatio get() = t("Pertahankan Rasio file sumber", "Keep source aspect ratio")
    val imgRotate get() = t("Putar searah jarum jam", "Rotate clockwise")
    val imgBg get() = t("Latar untuk gambar transparan (JPG)", "Background for transparent images (JPG)")
    val imgBgOpts get() = listOf(t("Putih", "White"), t("Hitam", "Black"))
    val imgNote get() = t("Orientasi EXIF diterapkan otomatis. GIF animasi hanya frame pertama yang diambil.", "EXIF orientation is applied automatically. For animated GIFs only the first frame is used.")

    // ---- Video ----
    val vidFormat get() = t("Format Output", "Output format")
    val vidFormats get() = listOf("MP4 (H.264)", "MP4 (H.265/HEVC)", "WebM (VP9)")
    val vidFormatHint get() = t(
        "Encoder dipakai dari perangkat. H.265 dan VP9 tidak ada di semua perangkat; kalau tidak ada, file ditandai gagal. WebM butuh Android 10+ agar audionya ikut (Opus).",
        "Encoders come from the device. H.265 and VP9 are not on every device; if missing, the file is marked as failed. WebM needs Android 10+ to keep its audio (Opus).",
    )
    val vidRes get() = t("Resolusi (sisi terpendek)", "Resolution (shorter side)")
    val vidResKeep get() = t("Asli", "Original")
    val vidBitrate get() = t("Bitrate Video (kbps)", "Video bitrate (kbps)")
    val vidBitrateAuto get() = t("Otomatis", "Automatic")
    val vidCompress get() = t("Kompres Video", "Compress Video")
    val vidCompressHint get() = t("Kualitas: bitrate video dikalikan persen. Ukuran: bitrate dihitung otomatis dari target dan mengabaikan pilihan Bitrate Video.", "Quality: the video bitrate is multiplied by the percentage. Size: the bitrate is calculated from the target and the Video Bitrate choice is ignored.")
    val vidFps get() = t("Frame rate", "Frame rate")
    val vidFpsOpts get() = listOf(t("Asli", "Original"), "30", "24", "15")
    val vidAudio get() = t("Audio", "Audio")
    val vidAudioOpts get() = listOf(t("Pertahankan", "Keep"), t("Hapus", "Remove"))
    val vidNote get() = t(
        "Resolusi hanya diperkecil (tidak diperbesar), frame rate hanya diturunkan. Audio AAC disalin tanpa disandikan ulang; audio lain disandikan ke AAC 128 kbps.",
        "Resolution is only reduced (never enlarged) and frame rate only lowered. AAC audio is copied without re-encoding; other audio is encoded to AAC 128 kbps.",
    )

    // ---- Teks ----
    val txtDirection get() = t("Arah konversi", "Direction")
    val txtDirections get() = listOf(t("Ke UTF-8", "To UTF-8"), t("Dari UTF-8", "From UTF-8"))
    val txtFrom get() = t("Penyandian asal", "Source encoding")
    val txtTo get() = t("Penyandian tujuan", "Target encoding")
    val txtBom get() = t("BOM UTF-8", "UTF-8 BOM")
    val txtBomOpts get() = listOf(t("Tanpa BOM", "No BOM"), t("Dengan BOM", "With BOM"))
    val txtNewline get() = t("Akhir baris", "Line endings")
    val txtNewlineOpts get() = listOf(t("Asli", "Keep"), "LF", "CRLF")
    val txtStrict get() = t("Karakter tidak valid", "Invalid characters")
    val txtStrictOpts get() = listOf(t("Gagalkan file", "Fail the file"), t("Ganti (?)", "Replace (?)"))
    val txtNote get() = t(
        "File yang sudah UTF-8 (atau sudah di penyandian tujuan) tidak diubah isinya dan dihitung \"tidak berubah\". Shift-JIS memakai CP932 (windows-31j) yang mencakup karakter tambahan Windows.",
        "Files that are already UTF-8 (or already in the target encoding) keep their content and count as \"unchanged\". Shift-JIS uses CP932 (windows-31j), which includes the Windows extra characters.",
    )

    // ---- bagian (view/hide) ----
    val secPreset get() = t("Preset Pengaturan", "Settings Presets")
    val secFormat get() = t("Format Output & Kompres", "Output Format & Compression")
    val secAudio get() = t("Pengaturan Audio", "Audio Settings")
    val secPolish get() = t("Pengaturan Lanjutan", "Advanced Settings")
    val secImage get() = t("Pengaturan Gambar", "Image Settings")
    val secRes get() = t("Resolusi", "Resolution")
    val secMisc get() = t("Rotasi & Latar", "Rotation & Background")
    val secVideo get() = t("Pengaturan Video", "Video Settings")
    val secText get() = t("Pengaturan Teks", "Text Settings")
    fun secExt(sel: Int, all: Int) = tt("Ekstensi Sumber ($sel/$all)", "Source Extensions ({0}/{1})", sel, all)
    val secOut get() = t("Output & Hasil", "Output & Result")
    fun secResults(n: Int) = tt("Daftar File Hasil ($n)", "Result Files ({0})", n)

    // ---- preset ----
    val presetPick get() = t("Pilih preset", "Choose a preset")
    val presetApply get() = t("Terapkan", "Apply")
    val presetSave get() = t("Simpan pengaturan ini sebagai preset", "Save these settings as a preset")
    val presetDelete get() = t("Hapus preset", "Delete preset")
    val presetName get() = t("Nama preset", "Preset name")
    val presetSaveBtn get() = t("Simpan", "Save")
    val presetCancel get() = t("Batal", "Cancel")
    val presetLast get() = t("Pengaturan terakhir dipakai", "Last used settings")
    fun presetBuiltin(n: String) = tt("Bawaan: $n", "Built-in: {0}", n)
    val presetHint get() = t("Preset menyimpan semua pengaturan konverter ini (kecuali file sumber dan folder).", "A preset stores all settings of this converter (except the source files and folders).")
    val presetAudio1 get() = t("BGM ringan (M4A 96 kbps)", "Light BGM (M4A 96 kbps)")
    val presetAudio2 get() = t("SE kecil (OGG 64 kbps, mono)", "Small SE (OGG 64 kbps, mono)")
    val presetImage1 get() = t("PNG aset hemat (kualitas 80%)", "Lean PNG assets (80% quality)")
    val presetImage2 get() = t("JPG ringan (kualitas 70%)", "Light JPG (70% quality)")
    val presetVideo1 get() = t("Video 720p ringan", "Light 720p video")
    val presetText1 get() = t("Shift-JIS ke UTF-8", "Shift-JIS to UTF-8")

    // ---- ekstensi, sampel, ganti asli, hasil ----
    val extPickHint get() = t("Centang ekstensi yang ingin diproses.", "Tick the extensions to process.")
    val sampleOn get() = t("Buat sampel dan bandingkan dengan sumber", "Create a sample and compare with the source")
    val sampleHint get() = t("Mengonversi satu file (yang pertama di folder) dengan pengaturan saat ini, lalu menampilkan perbandingan sebelum dan sesudah (audio bisa diputar). Tidak ada file yang ditulis ke folder; proses semua file tetap dimulai dari tombol Mulai Konversi.", "Converts one file (the first in the folder) with the current settings and shows a before/after comparison (audio is playable). Nothing is written to your folders; processing all files still starts from the Start Conversion button.")
    val sampleMaking get() = t("Membuat sampel…", "Creating sample…")
    val sampleBusy get() = t("Sedang membuat sampel, mohon tunggu", "Creating a sample, please wait")
    fun sampleTitle(n: String) = tt("Sampel: $n", "Sample: {0}", n)
    val sampleSrc get() = t("Sumber", "Source")
    val sampleOut get() = t("Hasil", "Result")
    val sampleGo get() = t("Lanjutkan proses semua", "Continue with all files")
    val sampleClose get() = t("Tutup", "Close")
    val samplePlay get() = t("Putar", "Play")
    val sampleStop get() = t("Henti", "Stop")
    fun sampleFail(msg: String) = tt("Gagal membuat sampel: $msg", "Could not create a sample: {0}", msg)
    val inPlace get() = t("Ganti file asli (cadangkan ke folder _backup)", "Replace original files (back up to _backup)")
    val inPlaceHint get() = t("Hanya mode Folder. Hasil ditulis di folder sumber. Jika nama file hasil sama, file asli dicadangkan dulu ke folder _backup di folder sumber; jika ekstensi berubah, file asli dibiarkan dan hasil ditambahkan di sampingnya.", "Folder mode only. Results are written into the source folder. If the result has the same name, the original is backed up first to a _backup folder inside the source folder; if the extension changes, the original is kept and the result is added next to it.")
    val resUndo get() = t("Batalkan", "Undo")
    val resUndone get() = t("Dibatalkan", "Undone")
    val resUndoFail get() = t("Gagal dibatalkan", "Undo failed")
    val resMore get() = t("Tampilkan lebih banyak", "Show more")
    val resUndoHint get() = t("Batalkan = hapus file hasil, atau kembalikan file asli dari _backup bila hasil menimpa file asli.", "Undo = delete the result file, or restore the original from _backup if the result replaced it.")
    fun extStat(ext: String, n: Int, a: String, b: String, change: String) = tt("${ext.uppercase().ifEmpty { "?" }}: $n file, $a → $b ($change)", "{0}: {1} file(s), {2} → {3} ({4})", ext.uppercase().ifEmpty { "?" }, n, a, b, change)
    val notifDone get() = t("Konversi selesai", "Conversion finished")
    val notifFail get() = t("Konversi gagal", "Conversion failed")

    // ---- audio lanjutan ----
    val audNorm get() = t("Normalisasi volume", "Volume normalization")
    val audNormOpts get() = listOf(t("Mati", "Off"), t("Ringan", "Light"), t("Sedang", "Medium"), t("Keras", "Loud"))
    val audNormHint get() = t("Menyamakan kenyaringan antar file (RMS): Ringan -20 dB, Sedang -16 dB, Keras -12 dB. Puncak dibatasi -1 dB agar tidak pecah. Butuh satu putaran pemindaian tambahan, jadi sedikit lebih lama.", "Evens out loudness across files (RMS): Light -20 dB, Medium -16 dB, Loud -12 dB. Peaks are limited to -1 dB to avoid clipping. Needs an extra scan pass, so it takes a bit longer.")
    val audTrim get() = t("Potong senyap di awal dan akhir", "Trim silence at start and end")
    val audKeepLoop get() = t("Pertahankan tag loop OGG (LOOPSTART/LOOPLENGTH)", "Keep OGG loop tags (LOOPSTART/LOOPLENGTH)")
    val audLoopHint get() = t("Tag loop dibaca dari file OGG sumber, disesuaikan dengan sample rate/pemotongan, lalu ditulis ke OGG hasil. Tag ikut ditulis di OGG (Vorbis) dan OGG (Opus). Pembaca loop bawaan RPGM MV/MZ mengenali tag OGG Vorbis, jadi untuk BGM berloop pilih OGG (Vorbis).", "Loop tags are read from the source OGG, adjusted for sample rate/trimming, and written to the result OGG. The tags are written to both OGG (Vorbis) and OGG (Opus). The built-in RPGM MV/MZ loop reader understands OGG Vorbis tags, so choose OGG (Vorbis) for looping BGM.")

    // ---- gambar lanjutan ----
    val imgMultiple get() = t("Bulatkan ke kelipatan", "Round to a multiple of")
    val imgMultipleOpts get() = listOf(t("Tidak", "No"), "2", "4", "8", "16", "24", "32", "48", "64")
    val imgMultipleHint get() = t("Untuk tileset/spritesheet agar ukuran tetap pas grid (mis. 48). Berlaku untuk Resolusi Persen dan Ukuran; tidak pernah melebihi ukuran sumber.", "For tilesets/spritesheets so sizes stay grid-aligned (e.g. 48). Applies to Percent and Size resolution; never exceeds the source size.")
    val imgDither get() = t("Dithering saat PNG dikurangi warnanya", "Dithering when PNG colors are reduced")

    val outModes get() = listOf(t("Folder Output", "Output Folder"), t("Timpa File Asli", "Overwrite Originals"))
    val backupCheck get() = t("Cadangkan ke folder _backup", "Back up to the _backup folder")
    val overwriteHow get() = t("Hasil ditulis langsung di folder sumber. Jika nama file hasil sama dengan file asli, file asli ditimpa; bila dicentang, file asli disalin dulu ke folder _backup di folder sumber (nama yang bentrok memakai (1)). Jika ekstensi berubah, file asli dibiarkan dan hasil ditambahkan di sampingnya.", "Results are written straight into the source folder. If a result has the same name as the original, the original is overwritten; when ticked, the original is first copied to a _backup folder inside the source folder (clashing names get (1)). If the extension changes, the original is kept and the result is added next to it.")
    val backupWarn get() = t("Peringatan: tanpa cadangan, file asli tertimpa permanen dan hasilnya tidak bisa dibatalkan. Pastikan kamu sudah punya salinan lain.", "Warning: without a backup the originals are overwritten permanently and the result cannot be undone. Make sure you have another copy.")
    val sampleBtn get() = t("Buat sampel pembandingan", "Create comparison sample")
    val resNoUndo get() = t("Tanpa cadangan", "No backup")
    val audTrimHint get() = t("Bagian senyap di awal dan akhir (di bawah sekitar -50 dBFS) dibuang, dengan sisa jeda singkat 10 ms. Sisa tengah file tidak diubah. Berguna untuk SE/BGM yang punya jeda kosong; durasi hasil jadi lebih pendek, dan tag loop disesuaikan otomatis.", "Silence at the start and end (below about -50 dBFS) is removed, leaving a 10 ms margin. The middle of the file is untouched. Handy for SE/BGM with blank gaps; the result gets shorter and loop tags are adjusted automatically.")
    val imgDitherHint get() = t("Saat warna PNG dikurangi (Kualitas di bawah 100%), kesalahan warna disebar ke piksel sekitar sehingga gradasi (kulit, langit, bayangan) tampak halus tanpa pita warna, tetapi bertekstur noise halus dan file bisa beberapa kali lebih besar daripada tanpa dithering. Cocok untuk gambar bergradasi; untuk pixel art biarkan mati.", "When PNG colors are reduced (Quality below 100%), color error is spread to neighboring pixels so gradients (skin, sky, shadows) look smooth without color bands, but slightly noisy and the file can be several times larger than without dithering. Good for gradient images; leave it off for pixel art.")

    // ---- Konverter Aset Khusus ----
    val specialTitle get() = t("Konverter Aset Khusus", "Special Asset Converter")
    val specialSub get() = t("Ubah format media khusus dari beberapa engine", "Convert special media formats from several engines")
    val secSpecial get() = t("Aset Circus (Adv32)", "Circus (Adv32) assets")
    val specialIntro get() = t(
        "Pilih file .pcm (XPCM) dan/atau .crx hasil ekstrak arsip Circus (PCK/CRM). Jenis file dikenali dari ekstensinya, dan hasilnya memakai nama yang sama dengan ekstensi baru.",
        "Pick .pcm (XPCM) and/or .crx files extracted from Circus archives (PCK/CRM). The type is recognised by extension and the result keeps the name with the new extension.",
    )
    val spAudioOut get() = t("Hasil audio .pcm", "Result of .pcm audio")
    val spAudioOutG get() = t("Format hasil audio", "Audio output format")
    val spImageOut get() = t("Format hasil gambar", "Image output format")
    val spImageOpts get() = listOf(t("Otomatis", "Automatic"), "PNG", "JPG")
    val spAudioOpts get() = listOf(t("Otomatis", "Automatic"), t("Selalu WAV", "Always WAV"))
    val spAudioHint get() = t(
        "Otomatis: XPCM yang berisi Ogg Vorbis menjadi .ogg asli tanpa diubah sedikit pun (kualitas dan tag loop utuh), selain itu menjadi .wav. Selalu WAV: Ogg didekode dulu lewat dekoder Android. Yang didukung: Ogg, PCM mentah, dan ADPCM; XPCM lama berkompresi MDCT (mode 1 dan 3) belum didukung dan akan dilaporkan sebagai gagal.",
        "Automatic: XPCM containing Ogg Vorbis becomes the original .ogg untouched (quality and loop tags kept), anything else becomes .wav. Always WAV: Ogg is decoded with Android's decoder first. Supported: Ogg, raw PCM and ADPCM; old MDCT-compressed XPCM (modes 1 and 3) is not supported yet and is reported as failed.",
    )
    val spImageHint get() = t(
        "Gambar .crx menjadi .png (24-bit, 32-bit beralpha, atau berpalet). CRX selisih (CRXD, mis. ekspresi wajah) digabung otomatis dengan gambar basisnya; file basis harus ikut dipilih/ada di folder yang sama (nama basis ada di dalam file). CRX berkompresi Zstd (versi baru) belum didukung.",
        "CRX images become .png (24-bit, 32-bit with alpha, or paletted). Differential CRX (CRXD, e.g. facial expressions) is merged with its base image automatically; the base file must be selected/present in the same folder (its name is stored in the file). Zstd-compressed CRX (newer versions) is not supported yet.",
    )

    fun spEngineSub(id: String): String = when (id) {
        "circus" -> t("Gambar .crx, .pgd, .pna · audio .pcm", "Images .crx, .pgd, .pna · audio .pcm")
        "artemis" -> t("Gambar .pgd", "Images .pgd")
        "bgi" -> t("Audio .ovk (wadah Ogg)", "Audio .ovk (Ogg container)")
        "catsystem2" -> t("Gambar .hg2, .hg3", "Images .hg2, .hg3")
        "entisgls" -> t("Gambar .eri · audio .emi, .mio", "Images .eri · audio .emi, .mio")
        "kirikiri" -> t("Gambar .tlg · audio .ovk, .mio", "Images .tlg · audio .ovk, .mio")
        "majiro" -> t("Gambar .rct, .rc8", "Images .rct, .rc8")
        "nexas" -> t("Gambar .spm", "Images .spm")
        "siglus" -> t("Gambar .g00 · audio .nwa", "Images .g00 · audio .nwa")
        else -> t("Gambar .pgd, .ycg", "Images .pgd, .ycg")
    }
    fun spEngineIntro(id: String): String = when (id) {
        "circus" -> t(
            "Pilih file .crx, .pgd, .pna, dan/atau .pcm (XPCM) hasil ekstrak arsip Circus. Jenis file dikenali dari ekstensinya, dan hasilnya memakai nama yang sama dengan ekstensi baru. CRX selisih (CRXD) dan PGD selisih memerlukan file dasarnya ikut dipilih (satu folder). PNA (AdvHD) adalah atlas beberapa gambar.",
            "Pick .crx, .pgd, .pna and/or .pcm (XPCM) files extracted from Circus archives. The type is recognised by extension and the result keeps the name with the new extension. Differential CRX (CRXD) and differential PGD need their base file selected too (same folder). PNA (AdvHD) is an atlas of several images.",
        )
        "artemis" -> t(
            "Pilih file .pgd. Dukungan memakai dekoder PGD yang terdokumentasi (keluarga Softpal: GE, PGD3, PGD2). Belum ditemukan sumber publik tentang varian PGD khusus Artemis; bila file tidak dikenali, akan dilaporkan gagal.",
            "Pick .pgd files. Support uses the documented PGD decoder (Softpal family: GE, PGD3, PGD2). No public source for an Artemis-specific PGD variant was found; unrecognised files are reported as failed.",
        )
        "bgi" -> t(
            "Pilih file .ovk. Setiap OVK berisi banyak Ogg Vorbis, sehingga hasilnya satu .zip berisi file-file .ogg (tanpa diubah). Tabel entri divalidasi terhadap ukuran file karena OVK tidak punya tanda pengenal.",
            "Pick .ovk files. Each OVK holds many Ogg Vorbis streams, so the result is one .zip of the .ogg files (untouched). The entry table is validated against the file size because OVK has no signature.",
        )
        "catsystem2" -> t(
            "Pilih file .hg2/.hg3 hasil ekstrak arsip CatSystem2. Lapisan/frame digabung ke kanvas penuh. HG-3 yang berisi JPEG didekode lewat dekoder Android.",
            "Pick .hg2/.hg3 files extracted from CatSystem2 archives. Layers/frames are composed onto the full canvas. HG-3 containing JPEG is decoded with Android's decoder.",
        )
        "entisgls" -> t(
            "Pilih file .eri (gambar), .mio (audio Entis) dan/atau .emi. Gambar ERI: hanya jalur lossless (RunLength, Huffman, Nemesis). Audio MIO: lossless dan lossy (LOT) diport dari GARbro/vgmstream, tetapi jalur lossy belum teruji sama sekali. Catatan jujur: di GARbro, .emi ternyata gambar Entis (EMSAC), bukan audio, sehingga .emi akan dilaporkan gagal.",
            "Pick .eri (image), .mio (Entis audio) and/or .emi files. ERI images: lossless paths only (RunLength, Huffman, Nemesis). MIO audio: lossless and lossy (LOT) ported from GARbro/vgmstream, but the lossy path is completely untested. Honest note: in GARbro .emi is actually an Entis image (EMSAC), not audio, so .emi files are reported as failed.",
        )
        "kirikiri" -> t(
            "Pilih file .tlg (gambar TLG0/5/6), .ovk (wadah Ogg, hasil .zip), dan/atau .mio (audio MIO).",
            "Pick .tlg (TLG0/5/6 images), .ovk (Ogg container, .zip result) and/or .mio (MIO audio) files.",
        )
        "majiro" -> t(
            "Pilih file .rct dan .rc8. RC8 pasangan RCT (namanya \"<nama>_.rc8\") dipakai otomatis sebagai kanal alpha bila ikut dipilih di folder yang sama; RC8 berdiri sendiri dikonversi sebagai gambar berpalet.",
            "Pick .rct and .rc8 files. The RC8 paired with an RCT (named \"<name>_.rc8\") is used automatically as the alpha channel when selected in the same folder; a standalone RC8 is converted as a paletted image.",
        )
        "nexas" -> t(
            "Pilih file .spm. Peringatan jujur: SPM NeXAS yang terdokumentasi (Karaik/NeXAS_DX) berisi peta sprite (nama gambar, hitbox, animasi), bukan data piksel, sehingga belum bisa diubah menjadi PNG. File akan dilaporkan gagal sampai ada sampel yang membuktikan lain.",
            "Pick .spm files. Honest warning: the documented NeXAS SPM (Karaik/NeXAS_DX) holds a sprite map (image names, hitboxes, animation), not pixel data, so it cannot be turned into PNG yet. Files are reported as failed until a sample proves otherwise.",
        )
        "siglus" -> t(
            "Pilih file .g00 (gambar tipe 0, 1, 2) dan/atau .nwa (audio). G00 tipe 3 (JPEG ber-XOR) belum didukung.",
            "Pick .g00 (type 0, 1, 2 images) and/or .nwa (audio) files. G00 type 3 (XOR'd JPEG) is not supported yet.",
        )
        else -> t(
            "Pilih file .pgd dan/atau .ycg. YCG hanya metode 1 (dua aliran zlib); metode 2 (YSSnp) belum didukung. PGD memakai dekoder keluarga Softpal; tidak ditemukan varian PGD khusus Yu-Ris di sumber referensi (ekstensi gambar Yu-Ris yang terdokumentasi adalah .ycg).",
            "Pick .pgd and/or .ycg files. YCG covers method 1 only (two zlib streams); method 2 (YSSnp) is not supported yet. PGD uses the Softpal-family decoder; no Yu-Ris-specific PGD variant exists in the reference sources (the documented Yu-Ris image extension is .ycg).",
        )
    }
    val spAudioOpts3 get() = listOf(t("Otomatis", "Automatic"), t("Selalu WAV", "Always WAV"), "OGG")
    fun spAudioHint3(id: String): String = when (id) {
        "circus" -> spAudioHint
        else -> t(
            "Otomatis dan Selalu WAV menghasilkan .wav (PCM 16-bit). OGG: audio hasil dekode dienkode ke Ogg Vorbis 160 kbps (44,1/48 kHz); bila file berisi Ogg di dalamnya, Ogg itu disalin apa adanya.",
            "Automatic and Always WAV produce .wav (16-bit PCM). OGG: the decoded audio is encoded to Ogg Vorbis 160 kbps (44.1/48 kHz); if the file holds Ogg inside, that Ogg is copied untouched.",
        )
    }
    val spAllFrames get() = t("Semua frame (jadi .zip)", "All frames (as .zip)")
    val spAllFramesHint get() = t(
        "Mati: hanya frame/lapisan pertama menjadi .png. Hidup: semua frame disimpan dalam satu .zip berisi PNG bernomor.",
        "Off: only the first frame/layer becomes a .png. On: all frames are saved in one .zip of numbered PNGs.",
    )
    fun spImageNote(id: String): String = when (id) {
        "circus" -> spImageHint
        else -> t(
            "Semua dekoder ini ditulis dari kode sumber referensi (GARbro dan proyek lain) tanpa sampel file asli; hasil perlu diuji di game masing-masing. Gambar disimpan sebagai PNG 32-bit.",
            "All these decoders were written from reference source code (GARbro and other projects) without real sample files; results need testing on each game. Images are saved as 32-bit PNG.",
        )
    }

    // ---- Pemeriksa Kompatibilitas ----
    val compatBtn get() = t("Periksa Kompatibilitas", "Check Compatibility")
    val compatTitle get() = t("Pemeriksa Kompatibilitas", "Compatibility Checker")
    val compatEngine get() = t("Engine tujuan", "Target engine")
    val compatLoading get() = t("Memeriksa file hasil…", "Checking result files…")
    fun compatSummary(n: Int, bad: Int, warn: Int, info: Int) = tt("$n file diperiksa · Tidak cocok: $bad · Peringatan: $warn · Info: $info", "{0} file(s) checked · Incompatible: {1} · Warnings: {2} · Info: {3}", n, bad, warn, info,
    )
    val compatClean get() = t("Tidak ada masalah yang terdeteksi untuk engine ini.", "No problems detected for this engine.")
    val compatNote get() = t(
        "Aturan umum (format, nama file, ukuran gambar, penyandian teks), bukan jaminan game pasti jalan. Versi engine dan plugin bisa berbeda; uji di game tetap perlu.",
        "General rules (format, file names, image size, text encoding), not a guarantee the game will run. Engine versions and plugins differ; still test in the game.",
    )
    val compatLevels get() = listOf(t("Info", "Info"), t("Peringatan", "Warning"), t("Tidak cocok", "Incompatible"))
    fun compatMore(n: Int) = tt("… dan $n temuan lain", "… and {0} more", n)
    val repackBtn get() = t("Kemas ulang folder ini", "Repack this folder")
    val repackHint get() = t(
        "Membuka menu Kemas Ulang dengan folder sumber ini sudah terpilih (folder _backup tidak ikut dikemas). Pilih engine/format arsipnya di sana.",
        "Opens Repack with this source folder already chosen (the _backup folder is not packed). Choose the engine/archive format there.",
    )

    // ---- preset per engine ----
    val presetAudioRpgmOgg get() = t("RPGM MV/MZ: OGG 96 kbps", "RPGM MV/MZ: OGG 96 kbps")
    val presetAudioRpgmM4a get() = t("RPGM MV/MZ: M4A 96 kbps (pasangan OGG)", "RPGM MV/MZ: M4A 96 kbps (OGG counterpart)")
    val presetAudioRenpy get() = t("Ren'Py: OGG 128 kbps", "Ren'Py: OGG 128 kbps")
    val presetAudioKiri get() = t("Kirikiri: WAV 44100 Hz (paling aman)", "Kirikiri: WAV 44100 Hz (safest)")
    val presetImageTileset get() = t("RPGM tileset: PNG 50%, kelipatan 48", "RPGM tileset: PNG 50%, multiple of 48")
    val presetImageRenpy get() = t("Ren'Py: WEBP 85% (hemat)", "Ren'Py: WEBP 85% (lean)")
    val presetImageKiri get() = t("Kirikiri: PNG kualitas penuh", "Kirikiri: full-quality PNG")
    val presetVideoRpgmWebm get() = t("RPGM MV/MZ: WebM 720p", "RPGM MV/MZ: WebM 720p")
    val presetVideoRpgmMp4 get() = t("RPGM mobile/iOS: MP4 H.264 720p", "RPGM mobile/iOS: MP4 H.264 720p")
}

fun mediaStr(lang: Lang) = MediaStr(lang == Lang.ID, lang)
