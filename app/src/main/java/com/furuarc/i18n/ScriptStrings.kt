package com.furuarc.i18n

import com.furuarc.core.OptKind
import com.furuarc.core.OptionSpec
import com.furuarc.core.Choice
import com.furuarc.core.ScriptKind
import com.furuarc.core.ScriptTool
import com.furuarc.settings.Lang

/** Texts for Script Tools per engine. Separated from [Str] so Str's parameter count does not exceed the JVM limit (255). */
class ScriptStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) {
    private fun t(idText: String, en: String) = if (id) idText else (Tr.get(lang, en) ?: en)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    val toolsHeader get() = t("Alat Skrip", "Script Tools")
    val browseBtn get() = t("Lihat isi arsip", "View archive contents")
    val browseTitle get() = t("Isi Arsip", "Archive Contents")
    val browseHint get() = t("Ketuk nama file gambar atau teks untuk Pratinjau", "Tap an image or text file name to Preview")
    // ---- Archive and Script Check ----
    val inspectTitle get() = t("Pemeriksaan File", "File Inspection")
    val inspectSub get() = t("Periksa arsip atau skrip: format, checksum, file rusak, enkripsi, dan kode yang salah", "Inspect an archive or script: format, checksum, corrupt files, encryption and wrong code")
    val inspectPicker get() = t("File Arsip / Skrip", "Archive / Script File")
    val inspectWorking get() = t("Memeriksa file…", "Inspecting file…")
    val inspectHint get() = t("Pilih file arsip (VN atau RPGM) atau skrip untuk diperiksa. Semua pemeriksaan dilakukan offline di perangkat.", "Pick an archive (VN or RPGM) or a script to inspect. Everything is checked offline on your device.")
    val inspectInfo get() = t("Informasi", "Information")
    val inspectFindings get() = t("Hasil pemeriksaan", "Inspection results")
    val inspectChecksum get() = t("Checksum", "Checksum")
    val inspectDeep get() = t("Periksa Integritas", "Check Integrity")
    val inspectDeepSub get() = t("Mengekstrak semua file ke memori untuk mencari file rusak. Bisa lama pada arsip besar.", "Extracts every file in memory to look for corrupt files. May take long for big archives.")
    val inspectDeepRun get() = t("Memeriksa file", "Checking files")
    val inspectHash get() = t("Hitung Checksum", "Compute Checksum")
    val inspectHashRun get() = t("Menghitung checksum…", "Computing checksum…")
    val inspectCancel get() = t("Batalkan", "Cancel")
    val inspectDeepResult get() = t("Hasil pemeriksaan integritas", "Integrity check results")
    val archiveHeader get() = t("Alat Arsip", "Archive Tools")

    // ---- VN Tools menu (automatic detection) ----
    val autoUnpackTitle get() = t("Bongkar Arsip", "Unpack Archive")
    val autoUnpackSub get() = t("Format arsip dikenali otomatis, lalu isinya diekstrak", "The archive format is detected automatically, then extracted")
    val autoPackTitle get() = t("Kemas Arsip", "Pack Archive")
    val autoPackSub get() = t("Pilih format arsip, lalu kemas isi sebuah folder", "Pick an archive format, then pack a folder")
    val autoScriptsTitle get() = t("Alat Skrip", "Script Tools")
    val autoScriptsSub get() = t("Ekspor/impor teks dan daftar isi, jenis dikenali otomatis", "Export/import text and contents list, type detected automatically")
    val engineListTitle get() = t("Daftar Engine", "Engine List")
    val engineListSub get() = t("Bongkar, kemas arsip, dan kelola skrip berdasarkan engine", "Unpack, pack archives and manage scripts per engine")
    val autoListTitle get() = t("Daftar Isi Skrip", "Script Contents List")
    val autoListSub get() = t("Catat semua nama file di dalam arsip ke satu file teks", "Write every file name inside an archive to a text file")
    val autoExportSub get() = t("Ambil dialog dan pilihan dari semua skrip di folder, jenisnya dikenali otomatis", "Collect dialogue and choices from every script in a folder, type detected automatically")
    val autoImportSub get() = t("Masukkan terjemahan ke semua skrip di folder, jenisnya dikenali otomatis", "Apply translations to every script in a folder, type detected automatically")
    val autoLabel get() = t("Deteksi otomatis", "Auto-detect")
    val packFormat get() = t("Format arsip", "Archive format")
    val packFormatPick get() = t("Pilih format arsip…", "Choose archive format…")
    val unknownArchive get() = t("Format arsip tidak dikenali. Coba lewat Daftar Engine bila engine-nya sudah diketahui.", "Archive format not recognised. Try Engine List if you know the engine.")
    val autoNote get() = t(
        "Jenis skrip dikenali otomatis per file: .rpy (Ren'Py), .ks (Kirikiri/Tyrano), .s (QLIE), .cst (CatSystem 2), .dat (SiglusEngine), dan .txt/.ast (NScripter/Artemis). Encoding \"Auto\" cocok untuk sebagian besar file.",
        "Script type is detected per file: .rpy (Ren'Py), .ks (Kirikiri/Tyrano), .s (QLIE), .cst (CatSystem 2), .dat (SiglusEngine) and .txt/.ast (NScripter/Artemis). \"Auto\" encoding fits most files.",
    )

    fun title(tool: ScriptTool) = when (tool) {
        ScriptTool.RPYC -> t("Dekompilasi rpyc → rpy", "Decompile rpyc → rpy")
        ScriptTool.EXPORT -> t("Ekspor Teks Skrip", "Export Script Text")
        ScriptTool.IMPORT -> t("Impor Teks Skrip", "Import Script Text")
        ScriptTool.NS_DECODE -> t("Dekode nscript.dat", "Decode nscript.dat")
        ScriptTool.NS_ENCODE -> t("Enkode ke nscript.dat", "Encode to nscript.dat")
        ScriptTool.LIST -> t("Daftar Isi Arsip", "Archive Contents List")
    }

    fun sub(tool: ScriptTool, kind: ScriptKind?) = when (tool) {
        ScriptTool.RPYC -> t("Ubah file .rpyc menjadi skrip .rpy yang bisa dibaca dan diedit", "Turn .rpyc files into readable, editable .rpy scripts")
        ScriptTool.EXPORT -> tt("Ambil dialog dan pilihan dari ${files(kind)} ke satu file CSV/TXT/JSON", "Collect dialogue and choices from {0} into one CSV/TXT/JSON file", files(kind))
        ScriptTool.IMPORT -> tt("Masukkan terjemahan CSV/TXT/JSON kembali ke ${files(kind)}", "Put a CSV/TXT/JSON translation back into {0}", files(kind))
        ScriptTool.NS_DECODE -> t("Ubah nscript.dat menjadi 0.txt yang bisa dibaca", "Turn nscript.dat into a readable 0.txt")
        ScriptTool.NS_ENCODE -> t("Ubah 0.txt kembali menjadi nscript.dat", "Turn 0.txt back into nscript.dat")
        ScriptTool.LIST -> t("Simpan daftar nama dan ukuran file di dalam arsip ke teks", "Save the names and sizes of the files inside an archive as text")
    }

    private fun files(kind: ScriptKind?) = when (kind) {
        ScriptKind.RPY -> "file .rpy"
        ScriptKind.KAG -> "file .ks / .scn"
        ScriptKind.TYRANO -> "file .ks"
        ScriptKind.NSCRIPT -> t("skrip 0.txt", "0.txt scripts")
        ScriptKind.QLIE -> "file .s"
        ScriptKind.ARTEMIS -> "file .txt"
        ScriptKind.CST -> "file .cst"
        ScriptKind.SIGLUS -> t("scene .dat", "scene .dat")
        ScriptKind.ESCUDE -> t("skrip .bin", ".bin scripts")
        ScriptKind.HCB -> "file .hcb"
        ScriptKind.WS2 -> "file .ws2"
        ScriptKind.SILKY -> "file .mes"
        ScriptKind.CIRCUS -> "file .mes"
        ScriptKind.BGI -> t("skrip tanpa ekstensi", "extension-less scripts")
        ScriptKind.ITUFURU -> t("skrip tanpa ekstensi", "extension-less scripts")
        ScriptKind.CSX -> "file .csx"
        ScriptKind.NEXAS -> t("skrip .bin", ".bin scripts")
        ScriptKind.YSTB -> "file .ybn"
        null -> t("skrip", "scripts")
    }

    val folderRpyc get() = t("Folder game (berisi .rpyc)", "Game folder (with .rpyc)")
    val folderScript get() = t("Folder hasil bongkar (berisi skrip)", "Unpacked folder (with scripts)")
    val folderNs get() = t("Folder hasil bongkar (berisi nscript.dat / 0.txt)", "Unpacked folder (with nscript.dat / 0.txt)")

    fun found(n: Int, size: String) = tt("Ditemukan $n file ($size)", "Found {0} files ({1})", n, size)

    fun noFiles(mode: Int) = when (mode) {
        22 -> t("Tidak ada file .rpyc di folder ini.", "No .rpyc files in this folder.")
        23 -> t("nscript.dat tidak ditemukan di folder ini.", "nscript.dat was not found in this folder.")
        24 -> t("0.txt / 00.txt tidak ditemukan di folder ini.", "0.txt / 00.txt was not found in this folder.")
        else -> t("Tidak ada file skrip yang dikenali di folder ini.", "No recognised script files in this folder.")
    }

    fun note(tool: ScriptTool, kind: ScriptKind?): String = when (tool) {
        ScriptTool.RPYC -> t(
            "Hasil ditulis ke folder output dengan struktur yang sama (script.rpyc → script.rpy). Layar (screen) belum didekompilasi dan ditulis sebagai komentar.",
            "Output keeps the folder structure (script.rpyc → script.rpy). Screens are not decompiled yet and are written as comments.",
        )
        ScriptTool.EXPORT -> when (kind) {
            ScriptKind.RPY -> t(
                "Teks diambil mentah seperti di .rpy (\\n, \\\" dan tag {i} dipertahankan). Dekompilasi .rpyc dulu bila belum ada .rpy.",
                "Text is taken raw as in the .rpy (\\n, \\\" and {i} tags kept). Decompile the .rpyc first if there is no .rpy.",
            )
            ScriptKind.KAG -> t(
                "Skrip .ks: tiap baris teks (termasuk tag inline seperti [r] [p]) jadi satu entri; jangan ubah atau hapus tag. Skrip .scn (PSB): nama pembicara, pesan, dan pilihan; baris baru = baris baru biasa. Bila berkas sudah punya slot bahasa tambahan, terjemahan ditulis ke slot terakhir; bila tidak, teks asli ditimpa.",
                "\".ks\" scripts: each text line (inline tags such as [r] [p] included) becomes one entry; do not change or remove tags. \".scn\" (PSB) scripts: speaker names, messages and choices; newline = real newline. If the file already has an extra language slot, the translation goes to the last slot; otherwise the original text is overwritten.",
            )
            ScriptKind.TYRANO -> t(
                "Baris teks, nama pembicara (#nama) dan teks tombol [glink] diambil. Jangan ubah atau hapus tag.",
                "Text lines, speaker names (#name) and [glink] button texts are collected. Do not change or remove tags.",
            )
            ScriptKind.NSCRIPT -> t(
                "Baris teks dan pilihan select diambil dari 0.txt hasil dekode. Pilih encoding sesuai game.",
                "Text lines and select choices are taken from the decoded 0.txt. Pick the encoding that matches the game.",
            )
            ScriptKind.QLIE -> t(
                "Baris dialog (penanda [n] = baris baru, jangan dihapus) dan pilihan ^select diambil dari skrip .s teks. Pilih encoding sesuai game.",
                "Dialogue lines ([n] = line break, keep it) and ^select choices are taken from text .s scripts. Pick the encoding that matches the game.",
            )
            ScriptKind.ARTEMIS -> t(
                "Semua baris dialog/narasi di file .txt Artemis diambil (baris # nama, * label, [tag] dan // komentar dilewati). Pilih encoding sesuai game. File .ast: dialog, pilihan, nama pembicara, dan teks ruby diambil; tag dan nama file tidak diubah.",
                "All dialogue/narration lines of Artemis .txt files are collected (# names, * labels, [tags] and // comments skipped). Pick the encoding that matches the game. .ast files: dialogue, choices, speaker names and ruby text are collected; tags and file names stay untouched.",
            )
            ScriptKind.CST -> t(
                "Dialog, nama pembicara dan teks perintah pilihan dari file .cst diambil. Pilih encoding sesuai game (umumnya Shift-JIS).",
                "Dialogue, speaker names and choice command texts are collected from .cst files. Pick the game's encoding (usually Shift-JIS).",
            )
            ScriptKind.SIGLUS -> t(
                "Teks dialog/nama/pilihan dari scene .dat SiglusEngine (hasil bongkar Scene.pck) diambil lewat penyaringan string non-ASCII, bukan analisis bytecode, jadi bisa ada sedikit string internal yang ikut. Hapus saja barisnya dari hasil ekspor bila tidak ingin diubah.",
                "Dialogue/name/choice texts are collected from SiglusEngine scene .dat files (from an unpacked Scene.pck) by filtering non-ASCII strings, not by bytecode analysis, so a few internal strings may show up. Delete such lines from the export if you do not want them changed.",
            )
            ScriptKind.ESCUDE -> t(
                "Semua string dari skrip biner Escu:de (ESCR1_00, file .bin hasil bongkar) diambil; nama sumber daya internal disaring. Tabel string ditulis ulang dengan offset baru saat impor. Pilih encoding Shift-JIS untuk game asli.",
                "All strings from Escu:de binary scripts (ESCR1_00, unpacked .bin files) are collected; internal resource names are filtered. The string table is rewritten with new offsets on import. Use Shift-JIS for original games.",
            )
            ScriptKind.HCB -> t(
                "Teks pushstring dari skrip Favorite/FVP .hcb diambil (nama sumber daya ASCII dilewati). Alamat lompat/panggil dihitung ulang saat impor. Teks lebih dari 254 byte tidak diubah. Pilih Shift-JIS untuk game asli.",
                "pushstring texts of Favorite/FVP .hcb scripts are collected (ASCII resource names skipped). Jump/call addresses are recomputed on import. Texts over 254 bytes are left unchanged. Use Shift-JIS for original games.",
            )
            ScriptKind.WS2 -> t(
                "Pesan, pilihan dan nama pembicara dari skrip WillPlus/AdvHD .ws2 diambil lewat disassembler (file terenkripsi dikenali otomatis). Awalan %LC/%LF pada nama dan akhiran %K%P pada pesan dipertahankan; kode % lain di dalam teks biarkan apa adanya.",
                "Messages, choices and speaker names of WillPlus/AdvHD .ws2 scripts are collected through a disassembler (encrypted files are detected automatically). The %LC/%LF name prefix and %K%P message suffix are preserved; leave other % codes inside texts untouched.",
            )
            ScriptKind.SILKY -> t(
                "Pesan dan nama pembicara dari skrip Silky Engine .mes (varian Plus dan Ai6Win dikenali otomatis). Baris baru = baris baru biasa, ruby = [teks]; jangan menyarangkan [ ] atau memberi baris baru di dalamnya. Alamat dihitung ulang saat impor.",
                "Messages and speaker names of Silky Engine .mes scripts (Plus and Ai6Win variants detected automatically). Newline = real newline, ruby = [text]; do not nest [ ] or put newlines inside. Addresses are recomputed on import.",
            )
            ScriptKind.CIRCUS -> t(
                "Nama pembicara dan pesan dari skrip Adv32 (Circus) .mes. Huruf Latin otomatis diubah dari lebar-penuh ke ASCII saat ekspor dan dikembalikan ke lebar-penuh saat impor (font game tidak punya huruf setengah-lebar). Baris baru = baris baru biasa. Tabel offset di kepala dihitung ulang saat impor; karakter di luar Shift-JIS diganti ？.",
                "Speaker names and messages of Adv32 (Circus) .mes scripts. Latin letters are converted from full-width to ASCII on export and back to full-width on import (the game font has no half-width letters). Newline = real newline. The header offset table is recomputed on import; characters outside Shift-JIS become ？.",
            )
            ScriptKind.BGI -> t(
                "Pesan, nama dan pilihan dari skrip BGI/Ethornell (V0, V1, dan berkepala BurikoCompiledScriptVer1.00). Skrip di arsip biasanya terkompresi DSC: bongkar arsip dengan opsi Dekode DSC dulu. Teks yang berubah ditambahkan di akhir file dan pointer diarahkan ulang. Ruby tidak diubah. Skrip V0 lama tidak terdeteksi otomatis: pilih lewat menu engine BGI.",
                "Messages, names and choices of BGI/Ethornell scripts (V0, V1, and BurikoCompiledScriptVer1.00 with header). Scripts in archives are usually DSC-compressed: unpack with the Decode DSC option first. Changed texts are appended to the file end and pointers redirected. Ruby is left unchanged. Old V0 scripts are not auto-detected: use the BGI engine menu.",
            )
            ScriptKind.ITUFURU -> t(
                "Teks skrip Yaneurao Itufuru (dipindai dari opcode teks 0x02 dan 0x1E). Panjang ditulis ulang saat impor; baris teks 0x02 harus berakhir baris baru (ditambahkan otomatis). Tidak terdeteksi otomatis: jalankan lewat menu engine Itufuru.",
                "Yaneurao Itufuru script texts (scanned from text opcodes 0x02 and 0x1E). Lengths are rewritten on import; 0x02 lines must end with a newline (added automatically). Not auto-detected: run via the Itufuru engine menu.",
            )
            ScriptKind.CSX -> t(
                "Teks (UTF-16) dari seksi conststr skrip EntisGLS .csx versi 2.x–3.x; hanya string bukan-ASCII yang diambil. Skrip diambil dari arsip NOA (script.noa) — sandi arsip bisa dicari dengan Pencari Kunci. CSX 1.x (string di seksi image) belum didukung.",
                "Texts (UTF-16) from the conststr section of EntisGLS .csx scripts, version 2.x–3.x; only non-ASCII strings are collected. Scripts come from the NOA archive (script.noa) — Key Finder can find the archive password. CSX 1.x (strings in the image section) is not supported.",
            )
            ScriptKind.YSTB -> t(
                "Teks Shift-JIS dari skrip Yu-Ris .ybn (ysbin/yst*.ybn di data.ypf): perintah WORD (dialog) dan literal string bukan-ASCII di ekspresi (pilihan, nama, dll). Kunci XOR dibaca otomatis. Impor menyambung ulang seksi ekspresi dan menggeser semua offset, jadi panjang teks bebas. Ukuran tidak terkompresi file .ybn berubah, jadi kemas ulang .ypf lewat menu Kemas.",
                "Shift-JIS texts from Yu-Ris .ybn scripts (ysbin/yst*.ybn in data.ypf): WORD commands (dialogue) and non-ASCII string literals inside expressions (choices, names, etc.). The XOR key is read automatically. Import rebuilds the expression section and shifts every offset, so text length is free. The .ybn size changes, so repack the .ypf from the Pack menu.",
            )
            ScriptKind.NEXAS -> t(
                "Teks Shift-JIS dari blok teks skrip NeXAS .bin (Script.pac); hanya segmen bukan-ASCII yang diambil. Impor menulis ulang blok teks tanpa menghitung ulang offset — panjang teks bebas, tetapi uji di game.",
                "Shift-JIS texts from the text block of NeXAS .bin scripts (Script.pac); only non-ASCII segments are collected. Import rewrites the text block without recalculating offsets — length is free, but test in the game.",
            )
            null -> autoNote
        }
        ScriptTool.IMPORT -> when (kind) {
            ScriptKind.NSCRIPT -> t(
                "Entri yang teks aslinya sudah berubah dilewati. Teks Latin otomatis diberi awalan ` agar dibaca sebagai teks, bukan perintah.",
                "Entries whose original text changed are skipped. Latin text automatically gets a ` prefix so it is read as text, not a command.",
            )
            ScriptKind.KAG -> t(
                "Entri yang teks aslinya sudah berubah dilewati. File .ks hasil ditulis dalam UTF-16 (BOM) yang didukung Kirikiri.",
                "Entries whose original text changed are skipped. Output .ks files are UTF-16 (BOM), which Kirikiri supports.",
            )
            else -> t(
                "Entri yang teks aslinya sudah berubah sejak diekspor dilewati.",
                "Entries whose original text changed since export are skipped.",
            )
        }
        ScriptTool.NS_DECODE -> t("Hasil: 0.txt (teks asli, encoding game). Pakai alat Ekspor Teks untuk menerjemahkannya.", "Result: 0.txt (original text, game encoding). Use Export Script Text to translate it.")
        ScriptTool.NS_ENCODE -> t("Hasil: nscript.dat. Salin ke folder game menggantikan yang lama.", "Result: nscript.dat. Copy it into the game folder in place of the old one.")
        ScriptTool.LIST -> t("Hasil: <nama arsip>.txt berisi ukuran dan nama setiap file.", "Result: <archive name>.txt with the size and name of every file.")
    }

    fun btn(tool: ScriptTool) = when (tool) {
        ScriptTool.RPYC -> t("Dekompilasi", "Decompile")
        ScriptTool.EXPORT -> t("Ekspor Teks", "Export Text")
        ScriptTool.IMPORT -> t("Impor Teks", "Import Text")
        ScriptTool.NS_DECODE -> t("Dekode", "Decode")
        ScriptTool.NS_ENCODE -> t("Enkode", "Encode")
        ScriptTool.LIST -> t("Simpan Daftar", "Save List")
    }

    fun summaryTitle(mode: Int) = when (mode) {
        20 -> t("Ekspor selesai", "Export finished")
        21 -> t("Impor selesai", "Import finished")
        22 -> t("Dekompilasi selesai", "Decompilation finished")
        23 -> t("Dekode selesai", "Decode finished")
        24 -> t("Enkode selesai", "Encode finished")
        else -> t("Daftar disimpan", "List saved")
    }

    fun okLabel(mode: Int) = when (mode) {
        20 -> t("Teks diekspor", "Texts exported")
        21 -> t("Teks diganti", "Texts replaced")
        22 -> t("File didekompilasi", "Files decompiled")
        25 -> t("File dicatat", "Files listed")
        else -> t("Berhasil", "Succeeded")
    }

    val translationFile get() = t("Berkas terjemahan (CSV/TXT/JSON)", "Translation file (CSV/TXT/JSON)")
    val vntpHint get() = t(
        "JSON VNTP berisi nama dan dialog saja (urut, tanpa penggabungan baris sama). Berkas ini untuk penerjemah dan tidak bisa diimpor ke Editor Terjemahan.",
        "VNTP JSON holds only names and dialogue (in order, duplicates kept). It is meant for translators and cannot be imported into the Translation Editor.",
    )
    val formatLabel get() = t("Format berkas", "File format")
    val exportedFile get() = t("terjemahan_skrip", "script_translation")

    fun logExported(n: Int, name: String) = tt("Selesai: $n teks ditulis ke $name", "Done: {0} texts written to {1}", n, name)
    fun logImported(n: Int, files: Int, stale: Int) =
        t("Selesai: $n teks diganti di $files file" + (if (stale > 0) " ($stale entri dilewati karena teks asli berubah)" else ""),
            "Done: $n texts replaced in $files files" + (if (stale > 0) " ($stale entries skipped, original text changed)" else ""))
    fun logFile(path: String, n: Int) = tt("$path: $n teks diganti", "{0}: {1} texts replaced", path, n)
    fun logDecompiled(path: String) = tt("Didekompilasi: $path", "Decompiled: {0}", path)
    fun logUnsupportedNote(n: Int) = tt("$n file tidak bisa didekompilasi (lihat log)", "{0} files could not be decompiled (see log)", n)
    val noText get() = t("Tidak ada teks yang bisa diambil dari file-file ini.", "No text could be collected from these files.")
    val emptyTr get() = t("Berkas terjemahan kosong atau formatnya tidak dikenali.", "The translation file is empty or its format is not recognised.")
    val noMatch get() = t("Tidak ada entri yang cocok dengan skrip di folder ini.", "No entries matched the scripts in this folder.")
    val nsAlreadyText get() = t("File ini tampak sudah berupa teks biasa, bukan nscript.dat terenkripsi.", "This file already looks like plain text, not an encrypted nscript.dat.")
    val noEntries get() = t("Pilih arsip yang berisi file lebih dulu.", "Pick an archive that contains files first.")
    fun logDecoded(name: String, out: String) = tt("Dekode: $name → $out", "Decoded: {0} → {1}", name, out)
    fun logEncoded(name: String, out: String) = tt("Enkode: $name → $out", "Encoded: {0} → {1}", name, out)
    fun logListed(n: Int, name: String) = tt("$n file dicatat ke $name", "{0} files listed in {1}", n, name)

    /** Encoding dropdown for NScripter script files (default Shift-JIS). */
    val encSpec: OptionSpec
        get() = OptionSpec(
            key = "enc", kind = OptKind.CHOICE,
            en = "Script encoding", id = "Encoding skrip",
            choices = listOf(
                Choice("auto", "Auto", "Auto"),
                Choice("sjis", "Shift-JIS (Japanese)", "Shift-JIS (Jepang)"),
                Choice("gbk", "GBK (Simplified Chinese)", "GBK (Mandarin Sederhana)"),
                Choice("big5", "Big5 (Traditional Chinese)", "Big5 (Mandarin Tradisional)"),
                Choice("utf8", "UTF-8", "UTF-8"),
            ),
            default = "sjis",
        )
}

object ScriptStrings {
    private val EN = ScriptStr(false)
    private val ID = ScriptStr(true)
    private val cache = HashMap<Lang, ScriptStr>()
    fun of(lang: Lang): ScriptStr = cache.getOrPut(lang) { ScriptStr(lang == Lang.ID, lang) }
}
