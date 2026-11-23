package com.furuarc.core

import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.Adler32
import java.util.zip.CRC32

/** One inspection finding. [level]: 0 info, 1 good, 2 warning, 3 problem. */
class Finding(val level: Int, val text: String, val tag: Int = 0) {
    companion object { const val TAG_TEXT = 1 } // file readable as text (View as text button)
}

/** Inspection result: info lines + list of findings. */
class Report(val info: List<Pair<String, String>>, val findings: List<Finding>)

class Hashes(val crc32: String, val md5: String, val sha256: String)

class DeepResult(val checked: Int, val failed: List<String>, val sumChecked: Int, val sumBad: List<String>, val bytes: Long)

/**
 * File Inspection (one archive or script): pure logic (no Android) so it can be tested. Bilingual text is chosen via [id]
 * (true = Indonesian).
 */
object Inspector {
    private fun hex(b: ByteArray) = b.joinToString("") { String.format("%02x", it.toInt() and 0xff) }

    // ================= archives =================

    fun archive(
        fileName: String, src: RandomSource, fmt: ArchiveFormat, entries: List<Entry>, key: Int?, engines: String, id: Boolean,
    ): Report {
        fun t(a: String, b: String) = if (id) a else b
        val info = ArrayList<Pair<String, String>>()
        val f = ArrayList<Finding>()
        info.add(Pair(t("Ukuran file", "File size"), "${formatBytes(src.length)} (${src.length} B)"))
        info.add(Pair(t("Format", "Format"), fmt.displayName))
        if (engines.isNotBlank()) info.add(Pair(t("Engine yang didukung", "Supported engine"), engines))
        info.add(Pair(t("Jumlah file", "File count"), entries.size.toString()))
        val dirs = HashSet<String>()
        var total = 0L
        var packed = 0L
        var zero = 0
        var longest = 0
        var biggest: Entry? = null
        val extCount = HashMap<String, Int>()
        for (e in entries) {
            val p = e.name.replace('\\', '/')
            val slash = p.lastIndexOf('/')
            if (slash > 0) {
                var d = p.substring(0, slash)
                while (d.isNotEmpty() && dirs.add(d)) d = if (d.contains('/')) d.substringBeforeLast('/') else ""
            }
            total += e.size.coerceAtLeast(0)
            packed += e.packedSize.coerceAtLeast(0)
            if (e.size == 0L) zero++
            if (p.length > longest) longest = p.length
            if (biggest == null || e.size > biggest.size) biggest = e
            val x = p.substringAfterLast('/').substringAfterLast('.', "").lowercase()
            extCount[x] = (extCount[x] ?: 0) + 1
        }
        info.add(Pair(t("Jumlah folder", "Folder count"), dirs.size.toString()))
        info.add(Pair(t("Total ukuran asli", "Total original size"), formatBytes(total)))
        if (packed > 0 && packed != total) {
            val ratio = if (total > 0) packed * 100.0 / total else 0.0
            info.add(Pair(t("Total terkompresi", "Total packed"), "${formatBytes(packed)} (${String.format("%.1f", ratio)}%)"))
        }
        biggest?.let { info.add(Pair(t("File terbesar", "Largest file"), "${it.name.substringAfterLast('/')} (${formatBytes(it.size)})")) }
        if (extCount.isNotEmpty()) {
            val top = extCount.entries.sortedByDescending { it.value }.take(5)
                .joinToString(", ") { (if (it.key.isEmpty()) t("(tanpa ekstensi)", "(no ext)") else "." + it.key) + " ×" + it.value }
            info.add(Pair(t("Jenis file teratas", "Top file types"), top))
        }
        if (key != null && fmt.id == "xp3") {
            info.add(Pair(t("Kunci XOR", "XOR key"), if (key == 0) t("tidak ada", "none") else key.toString()))
        }
        if (longest > 0) info.add(Pair(t("Path terpanjang", "Longest path"), t("$longest karakter", "$longest characters")))

        // ---- findings ----
        f.add(Finding(1, t("Isi arsip sesuai format ${fmt.displayName}: indeks berhasil dibaca.", "Archive matches the ${fmt.displayName} format: the index was read successfully.")))
        val ext = fileName.substringAfterLast('.', "").lowercase()
        if (ext.isNotEmpty() && fmt.extensions.none { it.removePrefix(".").lowercase() == ext }) {
            f.add(Finding(2, t("Ekstensi .$ext tidak lazim untuk format ini (biasanya ${fmt.extensions.joinToString("/") { "." + it.removePrefix(".") }}), tapi isinya terbaca benar.", "Extension .$ext is unusual for this format (usually ${fmt.extensions.joinToString("/") { "." + it.removePrefix(".") }}), but the content reads fine.")))
        }
        if (entries.isEmpty()) f.add(Finding(2, t("Arsip tidak berisi file apa pun.", "The archive contains no files.")))
        val badOff = entries.filter { it.offset < 0 || it.offset > src.length || it.size < 0 }
        if (badOff.isEmpty()) f.add(Finding(1, t("Semua posisi file berada di dalam ukuran arsip.", "Every file position lies within the archive size.")))
        else f.add(Finding(3, t("${badOff.size} file menunjuk posisi di luar arsip (arsip terpotong/rusak), mis. ${badOff.take(3).joinToString { it.name }}", "${badOff.size} files point outside the archive (truncated/corrupt), e.g. ${badOff.take(3).joinToString { it.name }}")))
        val over = entries.count { it.packedSize > 0 && it.offset in 0..src.length && it.offset + it.packedSize > src.length }
        if (over > 0) f.add(Finding(2, t("$over file memiliki ukuran yang melewati akhir arsip.", "$over files extend past the end of the archive.")))
        val seen = HashSet<String>()
        var dup = 0
        for (e in entries) if (!seen.add(e.name.replace('\\', '/').lowercase())) dup++
        if (dup > 0) f.add(Finding(2, t("$dup nama file ganda (tidak peka huruf besar/kecil); sebagian bisa saling menimpa saat diekstrak.", "$dup duplicate file names (case-insensitive); some may overwrite each other on extract.")))
        val badName = entries.count { !IoUtil.saneName(it.name) }
        if (badName > 0) f.add(Finding(2, t("$badName nama file memuat karakter kontrol/tidak valid (mungkin salah encoding nama).", "$badName file names contain control/invalid characters (possibly a wrong filename encoding).")))
        if (longest > 255) f.add(Finding(2, t("Ada path sepanjang $longest karakter; sebagian sistem file menolak path > 255.", "A path is $longest characters long; some file systems reject paths over 255.")))
        if (zero > 0) f.add(Finding(0, t("$zero file berukuran 0 byte.", "$zero files are 0 bytes.")))
        if (entries.isNotEmpty() && fmt.expectedChecksum(entries[0]) != null) {
            f.add(Finding(0, t("Format ini menyimpan checksum Adler32 tiap file; dicek penuh lewat Periksa Integritas.", "This format stores an Adler32 checksum per file; checked in full by Integrity Check.")))
        }
        if (fmt.id.startsWith("siglus")) siglusFindings(src, fmt, entries, info, f, id)
        f.add(Finding(0, t("Pemeriksaan di atas hanya membaca indeks. Gunakan Periksa Integritas untuk menguji isi tiap file.", "The checks above only read the index. Use Integrity Check to test each file's content.")))
        return Report(info, f)
    }

    private fun siglusFindings(
        src: RandomSource, fmt: ArchiveFormat, entries: List<Entry>, info: MutableList<Pair<String, String>>, f: MutableList<Finding>, id: Boolean,
    ) {
        fun t(a: String, b: String) = if (id) a else b
        try {
            when (fmt.id) {
                "siglus" -> {
                    val h = src.readFully(0, 92).le()
                    val mod = h.getInt(21 * 4)
                    info.add(Pair(t("Jumlah scene", "Scene count"), (entries.size - 1).coerceAtLeast(0).toString()))
                    info.add(Pair(t("Mode enkripsi exe", "Exe encryption mode"), if (mod == 0) t("tidak ada", "none") else t("ada (butuh kunci)", "yes (key needed)")))
                    if (mod == 0) f.add(Finding(1, t("Scene.pck tidak memakai kunci exe; bisa dibongkar tanpa kunci.", "This Scene.pck uses no exe key; it can be unpacked without a key.")))
                    else {
                        val r = try { com.furuarc.formats.SiglusPckFormat.findKey(src) } catch (e: Exception) { null }
                        f.add(
                            if (r != null && r.ok) Finding(1, t("Scene.pck memakai kunci exe; kunci berhasil dipulihkan otomatis (${r.verified}/${r.total} scene terverifikasi).", "This Scene.pck uses an exe key; it was recovered automatically (${r.verified}/${r.total} scenes verified)."))
                            else Finding(2, t("Scene.pck memakai kunci exe dan kunci tidak bisa dipulihkan otomatis. Coba Pencari Kunci atau isi opsi Kunci.", "This Scene.pck uses an exe key that could not be recovered automatically. Try Key Finder or fill the Key option.")),
                        )
                    }
                    if (h.getInt(22 * 4) != 0) f.add(Finding(0, t("Scene.pck memuat potongan sumber asli (original source); tidak ikut dikemas ulang oleh FuruArc.", "This Scene.pck has original-source chunks; FuruArc does not repack them.")))
                    f.add(Finding(0, t("Kemas Arsip butuh file _FuruArc_Siglus.meta hasil bongkar di folder yang sama.", "Pack Archive needs the _FuruArc_Siglus.meta file from unpacking in the same folder.")))
                }
                "siglus_gameexe" -> {
                    val mode = src.readFully(0, 8).le().getInt(4)
                    f.add(if (mode == 0) Finding(1, t("Gameexe.dat tanpa kunci exe.", "Gameexe.dat without an exe key.")) else Finding(2, t("Gameexe.dat memakai kunci exe; cari kuncinya dengan Pencari Kunci lalu isi opsi Kunci.", "Gameexe.dat uses an exe key; find it with Key Finder, then fill the Key option.")))
                }
                "siglus_ovk" -> f.add(Finding(1, t("Setiap entri berisi audio Ogg (suara karakter).", "Every entry holds Ogg audio (character voice).")))
            }
        } catch (e: Exception) { /* additional info only */ }
    }

    /** Magic matches [fmt] but the index failed to read (archive truncated/corrupt/needs another option). */
    fun brokenArchive(src: RandomSource, fmt: ArchiveFormat, engines: String, error: String, id: Boolean): Report {
        fun t(a: String, b: String) = if (id) a else b
        val info = ArrayList<Pair<String, String>>()
        info.add(Pair(t("Ukuran file", "File size"), "${formatBytes(src.length)} (${src.length} B)"))
        info.add(Pair(t("Format", "Format"), fmt.displayName))
        if (engines.isNotBlank()) info.add(Pair(t("Engine yang didukung", "Supported engine"), engines))
        val f = listOf(
            Finding(1, t("Tanda pengenal (magic) sesuai format ${fmt.displayName}.", "The signature (magic) matches the ${fmt.displayName} format.")),
            Finding(3, t("Indeks arsip gagal dibaca: $error. Arsip kemungkinan terpotong/rusak, atau butuh opsi lain (mis. encoding nama file).", "The archive index could not be read: $error. The archive may be truncated/corrupt, or need another option (e.g. filename encoding).")),
        )
        return Report(info, f)
    }

    /** Compute CRC32, MD5, and SHA-256 of the whole source. [progress] is called with the number of bytes read; [check] may throw to cancel. */
    fun hashes(src: RandomSource, progress: (Long) -> Unit, check: () -> Unit): Hashes {
        val crc = CRC32()
        val md5 = MessageDigest.getInstance("MD5")
        val sha = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(1 shl 20)
        var pos = 0L
        while (pos < src.length) {
            check()
            val n = src.readAt(pos, buf, 0, minOf(buf.size.toLong(), src.length - pos).toInt())
            if (n <= 0) break
            crc.update(buf, 0, n); md5.update(buf, 0, n); sha.update(buf, 0, n)
            pos += n
            progress(pos)
        }
        return Hashes(String.format("%08x", crc.value), hex(md5.digest()), hex(sha.digest()))
    }

    fun hashes(data: ByteArray): Hashes {
        val crc = CRC32().also { it.update(data) }
        return Hashes(String.format("%08x", crc.value), hex(MessageDigest.getInstance("MD5").digest(data)), hex(MessageDigest.getInstance("SHA-256").digest(data)))
    }

    private class CountingSink : OutputStream() {
        var n = 0L
        val adler = Adler32()
        override fun write(b: Int) { n++; adler.update(b) }
        override fun write(b: ByteArray, off: Int, len: Int) { n += len; adler.update(b, off, len) }
    }

    /** Test-extract all files to the trash (compute size & Adler32). Returns the names of files that failed/mismatched. */
    fun deepCheck(
        src: RandomSource, fmt: ArchiveFormat, entries: List<Entry>, key: Int?,
        progress: (Int, Int, String) -> Unit, check: () -> Unit,
    ): DeepResult {
        val failed = ArrayList<String>()
        val sumBad = ArrayList<String>()
        var sumChecked = 0
        var bytes = 0L
        val opts = Options(xorKey = key)
        for ((i, e) in entries.withIndex()) {
            check()
            progress(i, entries.size, e.name)
            try {
                val sink = CountingSink()
                fmt.extract(src, e, sink, opts)
                bytes += sink.n
                if (e.size >= 0 && sink.n != e.size) failed.add(e.name + " (size ${sink.n} ≠ ${e.size})")
                else fmt.expectedChecksum(e)?.let { want ->
                    sumChecked++
                    if (sink.adler.value != want) sumBad.add(e.name)
                }
            } catch (ex: java.util.concurrent.CancellationException) {
                throw ex
            } catch (ex: Exception) {
                failed.add(e.name + " (" + (ex.message ?: ex.javaClass.simpleName) + ")")
            }
        }
        progress(entries.size, entries.size, "")
        return DeepResult(entries.size, failed, sumChecked, sumBad, bytes)
    }

    fun deepFindings(r: DeepResult, id: Boolean): List<Finding> {
        fun t(a: String, b: String) = if (id) a else b
        val f = ArrayList<Finding>()
        if (r.failed.isEmpty()) f.add(Finding(1, t("${r.checked} file berhasil dibaca dan didekompresi tanpa galat (${formatBytes(r.bytes)}).", "${r.checked} files were read and decompressed without errors (${formatBytes(r.bytes)}).")))
        else f.add(Finding(3, t("${r.failed.size} dari ${r.checked} file gagal dibaca/ukurannya tidak cocok: ${r.failed.take(5).joinToString("; ")}${if (r.failed.size > 5) " …" else ""}. Arsip mungkin rusak, atau butuh kunci/opsi yang berbeda.", "${r.failed.size} of ${r.checked} files failed to read / had a size mismatch: ${r.failed.take(5).joinToString("; ")}${if (r.failed.size > 5) " …" else ""}. The archive may be corrupt, or need a different key/option.")))
        if (r.sumChecked > 0) {
            if (r.sumBad.isEmpty()) f.add(Finding(1, t("Checksum Adler32 cocok untuk ${r.sumChecked} file.", "Adler32 checksum matches for ${r.sumChecked} files.")))
            else f.add(Finding(3, t("Checksum Adler32 tidak cocok pada ${r.sumBad.size} file: ${r.sumBad.take(5).joinToString("; ")}${if (r.sumBad.size > 5) " …" else ""}", "Adler32 mismatch on ${r.sumBad.size} files: ${r.sumBad.take(5).joinToString("; ")}${if (r.sumBad.size > 5) " …" else ""}")))
        }
        return f
    }

    // ================= scripts / other files =================

    private val E_RENPY = "Ren'Py"
    private val E_KAG = "Kirikiri / KAG (XP3)"
    private val E_TYRANO = "Tyrano (TyranoScript)"
    private val E_NS = "NScripter / ONScripter"
    private val E_QLIE = "QLIE"
    private val E_ARTEMIS = "Artemis"
    private val E_CST = "CatSystem 2"
    private val E_SIGLUS = "Siglus Engine"
    private val E_MVMZ = "RPG Maker MV / MZ"
    private val E_RGSS = "RPG Maker XP / VX / VX Ace"

    private fun entropy(b: ByteArray, n: Int): Double {
        if (n <= 0) return 0.0
        val c = IntArray(256)
        for (i in 0 until n) c[b[i].toInt() and 0xff]++
        var e = 0.0
        for (x in c) if (x > 0) { val p = x.toDouble() / n; e -= p * (Math.log(p) / Math.log(2.0)) }
        return e
    }

    private fun startsWith(b: ByteArray, vararg v: Int): Boolean {
        if (b.size < v.size) return false
        for (i in v.indices) if ((b[i].toInt() and 0xff) != v[i]) return false
        return true
    }

    private fun asciiAt(b: ByteArray, off: Int, s: String): Boolean {
        if (b.size < off + s.length) return false
        for (i in s.indices) if (b[off + i].toInt() != s[i].code) return false
        return true
    }

    /** Inspect a single file (script/data/asset). [head] = start of the file (up to a few MB), [size] = actual size. */
    fun file(name: String, head: ByteArray, size: Long, id: Boolean, partial: Boolean = false): Report {
        fun t(a: String, b: String) = if (id) a else b
        val info = ArrayList<Pair<String, String>>()
        val f = ArrayList<Finding>()
        val base = name.substringAfterLast('/').substringAfterLast('\\')
        val ext = base.substringAfterLast('.', "").lowercase()
        info.add(Pair(t("Nama file", "File name"), base))
        info.add(Pair(t("Ukuran", "Size"), "${formatBytes(size)} ($size B)"))
        if (ext.isNotEmpty()) info.add(Pair(t("Ekstensi", "Extension"), ".$ext"))
        if (head.size < size && !partial) f.add(Finding(2, t("File besar: hanya ${formatBytes(head.size.toLong())} pertama yang dianalisis isinya.", "Large file: only the first ${formatBytes(head.size.toLong())} were analysed.")))
        if (size == 0L) {
            f.add(Finding(3, t("File kosong (0 byte).", "The file is empty (0 bytes).")))
            return Report(info, f)
        }
        val n = head.size

        // ---- 1. binary signature ----
        fun kindLine(kind: String, engine: String? = null) {
            info.add(Pair(t("Jenis file", "File type"), kind))
            if (engine != null) info.add(Pair(t("Engine yang didukung", "Supported engine"), engine))
        }
        when {
            RpgmCrypt.isEncryptedHeader(head) -> {
                kindLine(t("Aset RPG Maker terenkripsi", "Encrypted RPG Maker asset"), E_MVMZ)
                f.add(Finding(2, t("File ini TERENKRIPSI (header RPGMV). Dekripsi lewat Alat RPGM > Aset > Dekripsi.", "This file is ENCRYPTED (RPGMV header). Decrypt it via RPGM Tools > Assets > Decrypt.")))
                RpgmCrypt.detectKey(head)?.let { k -> if (ext == "rpgmvp" || ext == "png_") info.add(Pair(t("Kunci terdeteksi", "Detected key"), RpgmCrypt.keyToHex(k))) }
                return Report(info, f)
            }
            startsWith(head, 0x89, 0x50, 0x4E, 0x47) -> return media(info, f, id, t("Gambar PNG", "PNG image"), ext, "png")
            startsWith(head, 0xFF, 0xD8, 0xFF) -> return media(info, f, id, t("Gambar JPEG", "JPEG image"), ext, "jpg")
            asciiAt(head, 0, "GIF8") -> return media(info, f, id, t("Gambar GIF", "GIF image"), ext, "gif")
            asciiAt(head, 0, "BM") && n > 14 && ext == "bmp" -> return media(info, f, id, t("Gambar BMP", "BMP image"), ext, "bmp")
            asciiAt(head, 0, "RIFF") && asciiAt(head, 8, "WEBP") -> return media(info, f, id, t("Gambar WebP", "WebP image"), ext, "webp")
            asciiAt(head, 0, "RIFF") && asciiAt(head, 8, "WAVE") -> return media(info, f, id, t("Audio WAV", "WAV audio"), ext, "wav")
            asciiAt(head, 0, "OggS") -> return media(info, f, id, t("Audio/video Ogg", "Ogg audio/video"), ext, "ogg")
            asciiAt(head, 0, "ID3") || (startsWith(head, 0xFF) && n > 1 && (head[1].toInt() and 0xE0) == 0xE0) ->
                if (ext == "mp3") return media(info, f, id, t("Audio MP3", "MP3 audio"), ext, "mp3")
            asciiAt(head, 4, "ftyp") -> return media(info, f, id, t("Audio/video MP4/M4A", "MP4/M4A media"), ext, "m4a")
            startsWith(head, 0x50, 0x4B, 3, 4) -> {
                kindLine(t("Arsip ZIP", "ZIP archive"), null)
                f.add(Finding(0, t("Ini arsip ZIP. Jika dari game Tyrano, pakai Bongkar Arsip (format Tyrano).", "This is a ZIP archive. For a Tyrano game, use Unpack Archive (Tyrano format).")))
                return Report(info, f)
            }
            asciiAt(head, 0, "Rar!") || startsWith(head, 0x37, 0x7A, 0xBC, 0xAF) -> {
                kindLine(t("Arsip RAR/7z (tidak didukung)", "RAR/7z archive (unsupported)"), null)
                f.add(Finding(2, t("Format ini tidak didukung FuruArc. Ekstrak dulu dengan aplikasi arsip lain.", "FuruArc does not support this format. Extract it with another archive tool first.")))
                return Report(info, f)
            }
            asciiAt(head, 0, "CatScene") -> return cst(info, f, head, id)
            asciiAt(head, 0, "TJS2") -> {
                kindLine(t("Bytecode TJS2 (skrip Kirikiri terkompilasi)", "TJS2 bytecode (compiled Kirikiri script)"), E_KAG)
                f.add(Finding(2, t("Skrip sudah terkompilasi menjadi bytecode; tidak bisa dibaca/diterjemahkan sebagai teks.", "The script is compiled bytecode; it cannot be read/translated as text.")))
                return Report(info, f)
            }
            asciiAt(head, 0, "PSB") || (startsWith(head, 0x50, 0x53, 0x42, 0)) -> {
                kindLine(t("Skrip PSB (.scn Kirikiri/E-mote)", "PSB script (Kirikiri/E-mote .scn)"), E_KAG)
                f.add(Finding(2, t("Format PSB biner; belum ada alat teks untuknya di FuruArc.", "Binary PSB format; FuruArc has no text tool for it yet.")))
                return Report(info, f)
            }
            startsWith(head, 0xFE, 0xFE) && n >= 5 -> {
                val mode = head[2].toInt()
                kindLine(t("Skrip teks Kirikiri (header FE FE)", "Kirikiri text script (FE FE header)"), E_KAG)
                f.add(
                    when (mode) {
                        0 -> Finding(1, t("Mode 0: UTF-16 polos (tidak dienkripsi).", "Mode 0: plain UTF-16 (not encrypted)."))
                        1 -> Finding(2, t("Mode 1: TERENKRIPSI (XOR). Butuh dekripsi bawaan Kirikiri/Kirikiroid.", "Mode 1: ENCRYPTED (XOR). Needs Kirikiri/Kirikiroid decryption."))
                        2 -> Finding(2, t("Mode 2: TERKOMPRESI/terenkripsi (zlib). Butuh dekode bawaan Kirikiri.", "Mode 2: COMPRESSED/encrypted (zlib). Needs Kirikiri decoding."))
                        else -> Finding(3, t("Mode $mode tidak dikenal.", "Unknown mode $mode."))
                    },
                )
                return Report(info, f)
            }
        }
        if (ScriptTools.isRpyc(name) || asciiAt(head, 0, "RENPY RPC")) return rpyc(info, f, head, id)

        // ---- 2. RPGM save ----
        if (ext == "rpgsave" || ext == "rmmzsave") {
            kindLine(t("File save RPG Maker", "RPG Maker save file"), E_MVMZ)
            val d = if (head.size.toLong() == size) RpgmSave.decode(head) else null
            if (d == null) f.add(Finding(3, t("Isi save tidak bisa didekode (rusak atau bukan save MV/MZ).", "The save content cannot be decoded (corrupt or not an MV/MZ save).")))
            else {
                f.add(Finding(1, t("Save berhasil didekode (${if (d.kind == RpgmSave.MV) "MV" else "MZ"}, resep ${d.recipe}).", "Save decoded successfully (${if (d.kind == RpgmSave.MV) "MV" else "MZ"}, recipe ${d.recipe}).")))
                try { Json.parse(d.json); f.add(Finding(1, t("JSON di dalam save valid.", "The JSON inside the save is valid."))) } catch (e: Exception) { f.add(Finding(3, e.message ?: "JSON")) }
            }
            return Report(info, f)
        }

        // ---- 3. Ruby Marshal (RPG Maker XP/VX/Ace) ----
        if (head.size >= 2 && head[0].toInt() == 4 && head[1].toInt() == 8 && ext in setOf("rxdata", "rvdata", "rvdata2", "rgssad")) {
            val ver = when (ext) { "rxdata" -> "RPG Maker XP"; "rvdata" -> "RPG Maker VX"; else -> "RPG Maker VX Ace" }
            kindLine(t("Data Ruby Marshal ($ver)", "Ruby Marshal data ($ver)"), E_RGSS)
            f.add(Finding(1, t("Header Marshal 4.8 valid.", "Valid Marshal 4.8 header.")))
            f.add(Finding(0, t("Ini data biner game, bukan teks; sunting lewat Alat RPGM atau editor RPG Maker.", "This is binary game data, not text; edit it with RPGM Tools or the RPG Maker editor.")))
            if (base.lowercase().startsWith("scripts")) f.add(Finding(0, t("File Scripts berisi skrip Ruby yang dikompresi zlib.", "The Scripts file holds zlib-compressed Ruby scripts.")))
            return Report(info, f)
        }

        // ---- 4. nscript.dat ----
        if (ScriptTools.isNsDat(name)) {
            kindLine(t("Skrip NScripter (nscript.dat)", "NScripter script (nscript.dat)"), E_NS)
            if (ScriptText.nsLooksDecoded(head)) f.add(Finding(2, t("Isinya sudah tampak berupa teks biasa (tidak terenkripsi XOR 0x84).", "The content already looks like plain text (not XOR 0x84 encrypted).")))
            else if (ScriptText.nsLooksDecoded(ScriptText.nsXor(head))) f.add(Finding(2, t("TERENKRIPSI (XOR 0x84). Dekode lewat Alat Skrip NScripter > Dekode nscript.dat.", "ENCRYPTED (XOR 0x84). Decode it with NScripter Script Tools > Decode nscript.dat.")))
            else f.add(Finding(2, t("Isi tidak dikenali sebagai nscript.dat (bukan XOR 0x84 sederhana).", "The content is not recognised as nscript.dat (not a simple XOR 0x84).")))
            return Report(info, f)
        }

        // ---- 4b. SiglusEngine scene .dat ----
        if (ext == "dat" && SiglusScene.isDatHead(head)) return siglusDat(info, f, head, size, id)

        // ---- 5. text file ----
        val kind = ScriptText.detectKind(name, head)
        val isJson = ext == "json" || (kind == null && looksJson(head))
        val isJs = ext == "js"
        if (kind == null && !isJson && !isJs) {
            val nul = head.take(4096).count { it.toInt() == 0 }
            if (nul > 0 || ext in setOf("bin", "dat", "dll", "exe", "so")) {
                val e = entropy(head, minOf(n, 65536))
                kindLine(t("Data biner / tidak dikenal", "Binary / unknown data"))
                info.add(Pair(t("Entropi", "Entropy"), String.format("%.2f bit/byte", e)))
                f.add(
                    if (e > 7.5) Finding(2, t("Entropi sangat tinggi: isi kemungkinan besar TERENKRIPSI atau terkompresi.", "Very high entropy: the content is most likely ENCRYPTED or compressed."))
                    else Finding(0, t("File biner yang tidak dikenali FuruArc sebagai skrip.", "A binary file FuruArc does not recognise as a script.")),
                )
                f.add(Finding(0, t("Jika ini arsip game, coba Bongkar Arsip; format tak dikenal tidak bisa diperiksa lebih lanjut.", "If this is a game archive, try Unpack Archive; unknown formats cannot be inspected further.")))
                return Report(info, f)
            }
        }
        val dec = ScriptText.decode(kind ?: ScriptKind.KAG, head, "auto")
        val text = dec.text
        textGeneral(info, f, head, dec, id)
        when {
            isJson -> json(info, f, name, text, id)
            kind == ScriptKind.RPY -> renpy(info, f, text, id)
            kind == ScriptKind.KAG || kind == ScriptKind.TYRANO -> kag(info, f, text, kind, id)
            kind == ScriptKind.NSCRIPT -> ns(info, f, text, id)
            kind == ScriptKind.QLIE -> qlie(info, f, text, id)
            kind == ScriptKind.ARTEMIS -> artemis(info, f, text, id)
            isJs -> {
                kindLine(t("JavaScript (plugin/skrip RPG Maker MV/MZ)", "JavaScript (RPG Maker MV/MZ plugin/script)"), E_MVMZ)
                val open = text.count { it == '{' }; val close = text.count { it == '}' }
                f.add(if (open == close) Finding(1, t("Kurung kurawal seimbang ({ } = $open).", "Braces balanced ({ } = $open).")) else Finding(2, t("Kurung kurawal tidak seimbang: { = $open, } = $close (bisa terpengaruh kurung dalam string/komentar).", "Unbalanced braces: { = $open, } = $close (may be skewed by braces in strings/comments).")))
                if (base == "plugins.js") f.add(Finding(0, t("Daftar plugin game; urutannya menentukan urutan pemuatan.", "The game's plugin list; its order is the load order.")))
            }
            else -> {
                kindLine(t("Teks biasa", "Plain text"))
                f.add(Finding(0, t("Tidak dikenali sebagai skrip VN/RPGM tertentu. Untuk skrip bernama khusus, ganti ekstensinya (.rpy, .ks, .s, .txt Artemis).", "Not recognised as a specific VN/RPGM script. For custom-named scripts, give it the right extension (.rpy, .ks, .s, Artemis .txt).")))
            }
        }
        return Report(info, f)
    }

    private fun looksJson(b: ByteArray): Boolean {
        var i = 0
        if (startsWith(b, 0xEF, 0xBB, 0xBF)) i = 3
        while (i < b.size && (b[i].toInt() == ' '.code || b[i].toInt() == '\n'.code || b[i].toInt() == '\r'.code || b[i].toInt() == '\t'.code)) i++
        return i < b.size && (b[i].toInt() == '{'.code || b[i].toInt() == '['.code)
    }

    private fun media(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, id: Boolean, kind: String, ext: String, want: String): Report {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), kind))
        f.add(Finding(0, t("Ini file aset, bukan skrip.", "This is an asset file, not a script.")))
        val ok = ext == want || (want == "jpg" && ext == "jpeg") || (want == "m4a" && (ext == "mp4" || ext == "m4a"))
        if (ext.isNotEmpty() && !ok) f.add(Finding(2, t("Ekstensi .$ext tidak cocok dengan isinya ($want).", "Extension .$ext does not match the content ($want).")))
        else f.add(Finding(1, t("Ekstensi sesuai dengan isinya.", "The extension matches the content.")))
        return Report(info, f)
    }

    // ---- general text ----

    private fun textGeneral(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, raw: ByteArray, dec: DecodedScript, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val text = dec.text
        val enc = dec.charset.name() + (if (dec.bom.isNotEmpty()) " + BOM" else "")
        info.add(Pair(t("Encoding", "Encoding"), enc))
        val lines = if (text.isEmpty()) 0 else text.count { it == '\n' } + 1
        info.add(Pair(t("Jumlah baris", "Line count"), lines.toString()))
        val crlf = text.split("\r\n").size - 1
        val lf = text.count { it == '\n' } - crlf
        info.add(Pair(t("Akhir baris", "Line endings"), if (crlf > 0 && lf > 0) t("campur (CRLF $crlf, LF $lf)", "mixed (CRLF $crlf, LF $lf)") else if (crlf > 0) "CRLF" else "LF"))
        val bad = text.count { it == '\uFFFD' }
        if (bad > 0) f.add(Finding(2, t("$bad karakter tidak valid (�): encoding mungkin bukan ${dec.charset.name()}.", "$bad invalid characters (�): the encoding may not be ${dec.charset.name()}.")))
        val nul = raw.take(65536).count { it.toInt() == 0 }
        if (nul > 0 && dec.charset != Charsets.UTF_16LE) f.add(Finding(2, t("Ada byte NUL ($nul) di dalam teks; file mungkin biner atau terenkripsi.", "There are NUL bytes ($nul) in the text; the file may be binary or encrypted.")))
        if (dec.bom.isEmpty() && dec.charset == Charsets.UTF_8) f.add(Finding(0, t("UTF-8 tanpa BOM.", "UTF-8 without BOM.")))
        f.add(Finding(1, t("Isi file terbaca sebagai teks (tidak terenkripsi).", "The file reads as text (not encrypted)."), Finding.TAG_TEXT))
    }

    private fun units(kind: ScriptKind, text: String): Int = try { ScriptText.extract(kind, "x", text).size } catch (e: Exception) { -1 }

    private fun addUnits(info: MutableList<Pair<String, String>>, kind: ScriptKind, text: String, id: Boolean) {
        val u = units(kind, text)
        if (u >= 0) info.add(Pair(if (id) "Teks yang bisa diekspor" else "Exportable texts", u.toString()))
    }

    // ---- Ren'Py ----

    private fun renpy(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, text: String, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Skrip Ren'Py (.rpy)", "Ren'Py script (.rpy)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_RENPY))
        addUnits(info, ScriptKind.RPY, text, id)
        val lines = text.split("\n").map { it.removeSuffix("\r") }
        val labels = HashMap<String, Int>()
        val dupLabels = ArrayList<String>()
        val tabLines = ArrayList<Int>()
        val quoteLines = ArrayList<Int>()
        val braceLines = ArrayList<Int>()
        var inTriple = false
        val labelRe = Regex("^\\s*label\\s+([A-Za-z_][\\w.]*)")
        for ((i, l) in lines.withIndex()) {
            val tc = l.count { it == '"' } // only for regular lines
            val tripleCount = Regex("\"\"\"").findAll(l).count()
            if (tripleCount % 2 == 1) inTriple = !inTriple
            if (inTriple || tripleCount > 0) continue
            val s = l.trimStart()
            if (s.startsWith("#") || s.isEmpty()) continue
            if (l.startsWith("\t") || Regex("^ *\t").containsMatchIn(l)) tabLines.add(i + 1)
            labelRe.find(l)?.let { m ->
                val nm = m.groupValues[1]
                if (labels.containsKey(nm)) dupLabels.add("$nm (${i + 1})") else labels[nm] = i + 1
            }
            // quotes: count the ones that are not escaped
            var q = 0
            var j = 0
            var inStr = false
            var braces = 0
            while (j < l.length) {
                val c = l[j]
                if (c == '\\') { j += 2; continue }
                if (c == '"') { q++; inStr = !inStr }
                else if (inStr && c == '{') braces++
                else if (inStr && c == '}') braces--
                j++
            }
            if (q % 2 == 1) quoteLines.add(i + 1)
            if (braces != 0 && q % 2 == 0) braceLines.add(i + 1)
            if (tc < 0) break
        }
        info.add(Pair(t("Jumlah label", "Label count"), labels.size.toString()))
        f.add(Finding(0, t("Struktur indentasi/blok tidak diperiksa penuh; ini pemeriksaan dangkal.", "Indentation/block structure is not fully checked; this is a shallow check.")))
        if (tabLines.isEmpty()) f.add(Finding(1, t("Tidak ada indentasi tab (Ren'Py menolak tab).", "No tab indentation (Ren'Py rejects tabs).")))
        else f.add(Finding(3, t("${tabLines.size} baris berindentasi TAB (Ren'Py error), mulai baris ${tabLines.first()}.", "${tabLines.size} lines are indented with TABs (Ren'Py errors), starting at line ${tabLines.first()}.")))
        if (dupLabels.isEmpty()) f.add(Finding(1, t("Tidak ada label ganda.", "No duplicate labels.")))
        else f.add(Finding(3, t("Label ganda: ${dupLabels.take(5).joinToString()}", "Duplicate labels: ${dupLabels.take(5).joinToString()}")))
        if (quoteLines.isEmpty()) f.add(Finding(1, t("Tanda kutip \" berpasangan di semua baris.", "Double quotes \" are paired on every line.")))
        else f.add(Finding(2, t("${quoteLines.size} baris dengan tanda kutip tidak berpasangan, mulai baris ${quoteLines.take(5).joinToString()}.", "${quoteLines.size} lines with unpaired quotes, e.g. lines ${quoteLines.take(5).joinToString()}.")))
        if (braceLines.isNotEmpty()) f.add(Finding(2, t("Tag teks { } tidak seimbang di ${braceLines.size} baris, mis. baris ${braceLines.take(5).joinToString()}.", "Text tags { } unbalanced on ${braceLines.size} lines, e.g. lines ${braceLines.take(5).joinToString()}.")))
        else f.add(Finding(1, t("Tag teks { } seimbang di dalam string.", "Text tags { } are balanced inside strings.")))
    }

    // ---- KAG / Tyrano ----

    private fun kag(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, text: String, kind: ScriptKind, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val tyrano = kind == ScriptKind.TYRANO
        info.add(Pair(t("Jenis file", "File type"), if (tyrano) t("Skrip TyranoScript (.ks)", "TyranoScript (.ks)") else t("Skrip KAG Kirikiri (.ks)", "Kirikiri KAG script (.ks)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), if (tyrano) E_TYRANO else E_KAG))
        addUnits(info, kind, text, id)
        val lines = text.split("\n").map { it.removeSuffix("\r") }
        val labels = HashMap<String, Int>()
        val dup = ArrayList<String>()
        val bracket = ArrayList<Int>()
        var inScript = false
        var inBlock = false
        val pairs = mapOf("if" to "endif", "macro" to "endmacro", "iscript" to "endscript", "ignore" to "endignore")
        val open = HashMap<String, Int>()
        val close = HashMap<String, Int>()
        val jumps = ArrayList<Pair<String, Int>>()
        val tagRe = Regex("\\[\\s*([A-Za-z_][\\w]*)([^\\]]*)\\]")
        for ((i, l) in lines.withIndex()) {
            val s = l.trim()
            if (s.startsWith(";") || s.isEmpty()) continue
            if (inScript) {
                if (s.contains("[endscript]") || s == "@endscript") inScript = false
                continue
            }
            if (s.startsWith("/*")) inBlock = true
            if (inBlock) { if (s.contains("*/")) inBlock = false; continue }
            if (s.startsWith("*")) {
                val nm = s.substringAfter('*').substringBefore('|').trim()
                if (labels.containsKey(nm)) dup.add("*$nm (${i + 1})") else labels[nm] = i + 1
                continue
            }
            if (s.startsWith("@")) {
                val tag = s.substring(1).trim().split(Regex("\\s+"))[0].lowercase()
                for ((o, c) in pairs) { if (tag == o) open[o] = (open[o] ?: 0) + 1; if (tag == c) close[o] = (close[o] ?: 0) + 1 }
                if (tag == "iscript") inScript = true
                continue
            }
            var depth = 0
            for (ch in s) { if (ch == '[') depth++ else if (ch == ']') depth-- }
            if (depth != 0) bracket.add(i + 1)
            for (m in tagRe.findAll(s)) {
                val tag = m.groupValues[1].lowercase()
                for ((o, c) in pairs) { if (tag == o) open[o] = (open[o] ?: 0) + 1; if (tag == c) close[o] = (close[o] ?: 0) + 1 }
                if (tag == "iscript") inScript = true
                if ((tag == "jump" || tag == "call") && !m.groupValues[2].contains("storage")) {
                    Regex("target\\s*=\\s*\"?\\*([^\\s\"\\]]+)").find(m.groupValues[2])?.let { jumps.add(Pair(it.groupValues[1], i + 1)) }
                }
            }
        }
        info.add(Pair(t("Jumlah label", "Label count"), labels.size.toString()))
        if (bracket.isEmpty()) f.add(Finding(1, t("Semua tag [ ] tertutup di tiap baris.", "Every [ ] tag is closed on its line.")))
        else f.add(Finding(2, t("${bracket.size} baris dengan kurung [ ] tidak seimbang, mis. baris ${bracket.take(5).joinToString()} (tag lintas baris juga terdeteksi di sini).", "${bracket.size} lines with unbalanced [ ], e.g. lines ${bracket.take(5).joinToString()} (multi-line tags are flagged here too).")))
        for ((o, c) in pairs) {
            val a = open[o] ?: 0
            val b = close[o] ?: 0
            if (a != b) f.add(Finding(3, t("[$o] ($a) dan [$c] ($b) tidak seimbang.", "[$o] ($a) and [$c] ($b) are unbalanced.")))
        }
        if (dup.isEmpty()) f.add(Finding(1, t("Tidak ada label ganda.", "No duplicate labels.")))
        else f.add(Finding(3, t("Label ganda: ${dup.take(5).joinToString()}", "Duplicate labels: ${dup.take(5).joinToString()}")))
        val missing = jumps.filter { !labels.containsKey(it.first) }
        if (missing.isNotEmpty()) f.add(Finding(2, t("Lompatan ke label yang tidak ada di file ini: ${missing.take(5).joinToString { "*" + it.first + " (baris " + it.second + ")" }}", "Jumps to labels not in this file: ${missing.take(5).joinToString { "*" + it.first + " (line " + it.second + ")" }}")))
    }

    // ---- NScripter ----

    private fun ns(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, text: String, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Skrip NScripter/ONScripter (0.txt)", "NScripter/ONScripter script (0.txt)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_NS))
        addUnits(info, ScriptKind.NSCRIPT, text, id)
        val labels = HashMap<String, Int>()
        val dup = ArrayList<String>()
        for ((i, l) in text.split("\n").withIndex()) {
            val s = l.trim()
            if (s.startsWith("*")) {
                val nm = s.takeWhile { it != ' ' && it != '\t' && it != ',' }.removePrefix("*")
                if (nm.isNotEmpty()) { if (labels.containsKey(nm)) dup.add("*$nm (${i + 1})") else labels[nm] = i + 1 }
            }
        }
        info.add(Pair(t("Jumlah label", "Label count"), labels.size.toString()))
        f.add(if (text.contains("*define")) Finding(1, t("Blok *define ditemukan.", "*define block found.")) else Finding(2, t("Blok *define tidak ada (mungkin bukan 0.txt utama).", "No *define block (may not be the main 0.txt).")))
        if (dup.isEmpty()) f.add(Finding(1, t("Tidak ada label ganda.", "No duplicate labels."))) else f.add(Finding(3, t("Label ganda: ${dup.take(5).joinToString()}", "Duplicate labels: ${dup.take(5).joinToString()}")))
    }

    // ---- QLIE ----

    private fun qlie(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, text: String, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Skrip QLIE (.s teks)", "QLIE script (.s text)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_QLIE))
        addUnits(info, ScriptKind.QLIE, text, id)
        val lines = text.split("\n").map { it.trim() }
        val sel = lines.count { it.lowercase().startsWith("^select") }
        info.add(Pair(t("Pilihan (^select)", "Choices (^select)"), sel.toString()))
        val badQ = lines.withIndex().filter { (_, l) -> l.startsWith("^") && l.count { it == '"' } % 2 == 1 }.map { it.index + 1 }
        f.add(if (badQ.isEmpty()) Finding(1, t("Tanda kutip pada baris perintah ^ berpasangan.", "Quotes on ^ command lines are paired.")) else Finding(3, t("Tanda kutip tidak berpasangan di baris ${badQ.take(5).joinToString()}.", "Unpaired quotes on lines ${badQ.take(5).joinToString()}.")))
        val labels = lines.filter { it.startsWith("@@") && !it.startsWith("@@@") }.map { it.removePrefix("@@") }
        val dup = labels.groupBy { it }.filter { it.value.size > 1 }.keys
        f.add(if (dup.isEmpty()) Finding(1, t("Tidak ada label (@@) ganda.", "No duplicate (@@) labels.")) else Finding(3, t("Label @@ ganda: ${dup.take(5).joinToString()}", "Duplicate @@ labels: ${dup.take(5).joinToString()}")))
    }

    // ---- Artemis ----

    private fun artemis(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, text: String, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        if (AstScript.isAst(text)) {
            info.add(Pair(t("Jenis file", "File type"), t("Skrip Artemis (.ast)", "Artemis script (.ast)")))
            info.add(Pair(t("Engine yang didukung", "Supported engine"), E_ARTEMIS))
            try {
                val us = AstScript.extract("x", text)
                info.add(Pair(t("Teks yang bisa diekspor", "Exportable texts"), us.size.toString()))
                f.add(Finding(1, t("Struktur tabel .ast terbaca utuh.", "The .ast table structure parsed fully.")))
            } catch (e: Exception) {
                f.add(Finding(3, t("Struktur .ast tidak terbaca: ${e.message}", "Could not parse the .ast structure: ${e.message}")))
            }
            return
        }
        info.add(Pair(t("Jenis file", "File type"), t("Skrip Artemis (.txt)", "Artemis script (.txt)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_ARTEMIS))
        addUnits(info, ScriptKind.ARTEMIS, text, id)
        val lines = text.split("\n").map { it.trim() }
        val labels = lines.filter { it.startsWith("*") }
        info.add(Pair(t("Jumlah label", "Label count"), labels.size.toString()))
        val dup = labels.groupBy { it }.filter { it.value.size > 1 }.keys
        f.add(if (dup.isEmpty()) Finding(1, t("Tidak ada label ganda.", "No duplicate labels.")) else Finding(3, t("Label ganda: ${dup.take(5).joinToString()}", "Duplicate labels: ${dup.take(5).joinToString()}")))
        val bad = lines.withIndex().filter { (_, l) -> l.startsWith("[") && l.count { it == '[' } != l.count { it == ']' } }.map { it.index + 1 }
        f.add(if (bad.isEmpty()) Finding(1, t("Tag [ ] seimbang di setiap baris tag.", "[ ] tags are balanced on every tag line.")) else Finding(2, t("Kurung [ ] tidak seimbang di baris ${bad.take(5).joinToString()}.", "Unbalanced [ ] on lines ${bad.take(5).joinToString()}.")))
    }

    // ---- CatSystem2 .cst ----

    private fun cst(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, head: ByteArray, id: Boolean): Report {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Skrip CatSystem 2 (.cst)", "CatSystem 2 script (.cst)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_CST))
        try {
            val s = CatScene.stats(head, ScriptText.SJIS)
            info.add(Pair(t("Jumlah string", "String count"), s.strings.toString()))
            info.add(Pair(t("Teks yang bisa diekspor", "Exportable texts"), s.units.toString()))
            info.add(Pair(t("Kompresi", "Compression"), if (s.compressed) "zlib" else t("tidak ada", "none")))
            f.add(Finding(1, t("Header dan tabel string CatScene valid.", "CatScene header and string table are valid.")))
            f.add(if (s.problems.isEmpty()) Finding(1, t("Panjang data cocok dengan header.", "Data length matches the header.")) else Finding(2, t("Panjang data di header tidak cocok dengan isi.", "The data length in the header does not match the content.")))
            f.add(Finding(1, t("Tidak terenkripsi.", "Not encrypted.")))
        } catch (e: Exception) {
            f.add(Finding(3, t("Struktur CatScene tidak valid: ${e.message}", "Invalid CatScene structure: ${e.message}")))
        }
        return Report(info, f)
    }

    // ---- SiglusEngine scene .dat ----

    private fun siglusDat(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, head: ByteArray, size: Long, id: Boolean): Report {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Scene SiglusEngine (.dat, hasil bongkar Scene.pck)", "SiglusEngine scene (.dat, from an unpacked Scene.pck)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_SIGLUS))
        if (head.size.toLong() < size) {
            f.add(Finding(1, t("Kepala scene Siglus valid.", "Siglus scene header is valid.")))
            f.add(Finding(0, t("File besar; statistik string tidak dihitung. Ekspor Teks tetap bisa dipakai.", "Large file; string statistics were skipped. Export Text still works.")))
            return Report(info, f)
        }
        val s = SiglusScene.stats(head)
        if (s == null) {
            f.add(Finding(3, t("Struktur scene Siglus tidak valid (terpotong/rusak).", "Invalid Siglus scene structure (truncated/corrupt).")))
        } else {
            info.add(Pair(t("Jumlah string", "String count"), s.first.toString()))
            info.add(Pair(t("Teks yang bisa diekspor", "Exportable texts"), s.second.toString()))
            f.add(Finding(1, t("Kepala dan tabel string scene valid.", "Scene header and string table are valid.")))
            f.add(Finding(0, t("Teks dipilih dengan menyaring string non-ASCII, bukan analisis bytecode.", "Texts are picked by filtering non-ASCII strings, not by bytecode analysis.")))
        }
        return Report(info, f)
    }

    // ---- rpyc ----

    private fun rpyc(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, head: ByteArray, id: Boolean): Report {
        fun t(a: String, b: String) = if (id) a else b
        info.add(Pair(t("Jenis file", "File type"), t("Skrip Ren'Py terkompilasi (.rpyc)", "Compiled Ren'Py script (.rpyc)")))
        info.add(Pair(t("Engine yang didukung", "Supported engine"), E_RENPY))
        info.add(Pair(t("Versi kontainer", "Container version"), if (asciiAt(head, 0, "RENPY RPC2")) "RPC2" else "RPC1 / zlib"))
        try {
            val ast = Rpyc.readAst(head)
            info.add(Pair(t("Jumlah statement teratas", "Top-level statements"), ast.size.toString()))
            f.add(Finding(1, t("Kontainer dan pickle berhasil dibaca.", "Container and pickle were read successfully.")))
            try {
                val out = Rpyc.decompile(head)
                info.add(Pair(t("Baris hasil dekompilasi", "Decompiled lines"), out.count { it == '\n' }.toString()))
                f.add(Finding(1, t("Bisa didekompilasi ke .rpy lewat Alat Skrip Ren'Py.", "Can be decompiled to .rpy via the Ren'Py script tool.")))
            } catch (e: Exception) {
                f.add(Finding(2, t("Terbaca, tapi gagal didekompilasi: ${e.message}", "Readable, but decompilation failed: ${e.message}")))
            }
        } catch (e: Exception) {
            f.add(Finding(3, t("Bukan rpyc yang valid atau rusak: ${e.message}", "Not a valid rpyc or corrupt: ${e.message}")))
        }
        return Report(info, f)
    }

    // ---- JSON (data RPG Maker) ----

    private fun json(info: MutableList<Pair<String, String>>, f: MutableList<Finding>, name: String, text: String, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val base = name.substringAfterLast('/').substringAfterLast('\\')
        info.add(Pair(t("Jenis file", "File type"), t("Data JSON", "JSON data")))
        val root: Any?
        try {
            root = Json.parse(text)
            f.add(Finding(1, t("JSON valid.", "Valid JSON.")))
        } catch (e: Exception) {
            f.add(Finding(3, e.message ?: "JSON"))
            return
        }
        val rootKind = when (root) { is Map<*, *> -> t("objek (${root.size} kunci)", "object (${root.size} keys)"); is List<*> -> t("array (${root.size} elemen)", "array (${root.size} items)"); else -> t("nilai tunggal", "single value") }
        info.add(Pair(t("Akar", "Root"), rootKind))
        val low = base.lowercase()
        val rpgmNames = setOf("actors.json", "classes.json", "skills.json", "items.json", "weapons.json", "armors.json", "enemies.json", "troops.json", "states.json", "animations.json", "tilesets.json", "commonevents.json", "system.json", "mapinfos.json")
        val isRpgm = low in rpgmNames || Regex("map\\d+\\.json").matches(low)
        if (isRpgm) {
            info.add(Pair(t("Engine yang didukung", "Supported engine"), E_MVMZ))
            info.add(Pair(t("Data RPG Maker", "RPG Maker data"), low))
        }
        if (RpgmText.isTextFile(name)) {
            try {
                val df = RpgmText.parseData(name, text)
                val u = RpgmText.extract(RpgmText.fileKey(name), df.root).size
                info.add(Pair(t("Teks yang bisa diekspor", "Exportable texts"), u.toString()))
                f.add(Finding(1, t("Struktur data RPG Maker dikenali dan bisa diekspor lewat Terjemahan Data.", "RPG Maker data structure recognised; exportable via Data Translation.")))
            } catch (e: Exception) {
                f.add(Finding(2, t("JSON valid tetapi struktur RPG Maker tidak dikenali: ${e.message}", "Valid JSON but the RPG Maker structure is not recognised: ${e.message}")))
            }
        }
        if (low == "system.json" && root is Map<*, *>) {
            (root["encryptionKey"] as? String)?.let { info.add(Pair(t("Kunci enkripsi aset", "Asset encryption key"), it)) }
            val img = root["hasEncryptedImages"]; val aud = root["hasEncryptedAudio"]
            if (img != null || aud != null) f.add(Finding(0, t("Gambar terenkripsi: ${img ?: "-"}, audio terenkripsi: ${aud ?: "-"}.", "Encrypted images: ${img ?: "-"}, encrypted audio: ${aud ?: "-"}.")))
            (root["gameTitle"] as? String)?.let { info.add(Pair(t("Judul game", "Game title"), it)) }
        }
    }
}
