package com.furuarc.core

import com.furuarc.formats.NoaFormat
import com.furuarc.formats.SiglusGameexeFormat
import com.furuarc.formats.SiglusPckFormat
import java.io.ByteArrayOutputStream

/** One file in the scanned folder. [open] opens a random-access read source (called only when needed). */
class KfFile(val path: String, val size: Long, val open: () -> RandomSource)

/** One key found. [status]: 2 = verified (the key actually decrypts data), 1 = candidate. */
class KeyHit(
    val engineId: String,
    val engine: String,
    val keyKind: String,
    val key: String,
    /** value that can be pasted directly into the Key option (null if the key is not used in an option) */
    val optionValue: String?,
    val status: Int,
    val source: String,
    val note: String,
    val files: List<String>,
)

class KeyReport(val hits: List<KeyHit>, val notes: List<String>, val scanned: Int)

/**
 * Key Finder: scans the game folder, looking for encryption keys and the list of files that use them.
 * Modules: SiglusEngine (recovered from Scene.pck), RPGM MV/MZ (System.json or PNG header), and archives with [ArchiveFormat.detectKey] (XP3).
 * Keys that are not stored raw in a file (scrambled/assembled while the game runs) can only be found if the data itself
 * gives hints, like Siglus.
 */
object KeyFinder {
    const val ALL = "auto"
    val ENGINES = listOf("siglus", "rpgm", "xp3", "catsystem2", "entisgls")

    private val RPGM_EXT = setOf("rpgmvp", "rpgmvm", "rpgmvo", "png_", "ogg_", "m4a_")
    private val SKIP = setOf(
        "png", "jpg", "jpeg", "gif", "bmp", "webp", "ogg", "mp3", "wav", "m4a", "flac", "mp4", "webm", "avi", "mkv", "wmv",
        "ttf", "otf", "ico", "svg", "ogv", "mid", "midi", "wma", "mpg", "flv", "g00", "txt", "json", "js", "html", "css", "ini",
    )

    private fun ext(p: String) = p.substringAfterLast('.', "").lowercase()
    private fun name(p: String) = p.replace('\\', '/').substringAfterLast('/')
    private fun hex(b: ByteArray) = b.joinToString("") { String.format("%02x", it.toInt() and 0xff) }
    private fun hex0x(b: ByteArray) = b.joinToString(", ") { String.format("0x%02x", it.toInt() and 0xff) }

    fun analyze(
        files: List<KfFile>,
        engine: String = ALL,
        id: Boolean = true,
        progress: (done: Int, total: Int, label: String) -> Unit = { _, _, _ -> },
        cancelled: () -> Boolean = { false },
    ): KeyReport {
        fun t(a: String, b: String) = if (id) a else b
        val hits = ArrayList<KeyHit>()
        val notes = ArrayList<String>()
        val want = { e: String -> engine == ALL || engine == e }
        var step = 0
        val steps = ENGINES.count { want(it) }
        fun next(label: String) { progress(step++, steps, label) }

        if (want("siglus")) { next("Siglus Engine"); siglus(files, id, hits, notes, cancelled) }
        if (want("rpgm")) { next("RPG Maker MV/MZ"); rpgm(files, id, hits, notes, cancelled) }
        if (want("xp3")) { next("Kirikiri XP3"); archives(files, id, hits, notes, cancelled) }
        if (want("catsystem2")) { next("CatSystem2"); catsystem2(files, id, hits, notes, cancelled) }
        if (want("entisgls")) { next("EntisGLS"); entisgls(files, id, hits, notes, cancelled) }
        progress(steps, steps, "")
        if (hits.isEmpty() && notes.isEmpty()) {
            notes.add(t("Tidak ada file terenkripsi yang dikenali di folder ini.", "No recognised encrypted files in this folder."))
        }
        return KeyReport(hits, notes, files.size)
    }

    // ---------------- Siglus ----------------

    private fun siglus(files: List<KfFile>, id: Boolean, hits: MutableList<KeyHit>, notes: MutableList<String>, cancelled: () -> Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val pcks = files.filter { ext(it.path) == "pck" && it.size > 92 }
        val gameexes = files.filter { name(it.path).equals("Gameexe.dat", true) }
        val hasExe = files.any { name(it.path).equals("SiglusEngine.exe", true) }
        var foundPck = false
        for (f in pcks) {
            if (cancelled()) return
            try {
                f.open().use { src ->
                    val head = src.readFully(0, minOf(16L, src.length).toInt())
                    if (!SiglusPckFormat.detects(src, head)) return@use
                    foundPck = true
                    val mod = src.readFully(0, 92).le().getInt(21 * 4)
                    if (mod == 0) {
                        notes.add(t("${f.path}: Scene.pck Siglus ini tidak memakai kunci exe.", "${f.path}: this Siglus Scene.pck uses no exe key."))
                        return@use
                    }
                    val r = SiglusPckFormat.findKey(src, cancelled)
                    if (r == null) {
                        notes.add(t("${f.path}: kunci Siglus tidak ditemukan (data scene tidak cocok dengan pola yang dikenal).", "${f.path}: Siglus key not found (scene data does not match the known pattern)."))
                        return@use
                    }
                    val enc = ArrayList<String>()
                    enc.add(f.path)
                    // scene list inside Scene.pck
                    try {
                        val opt = Options(extra = mapOf("skey" to hex(r.key)))
                        for (e in SiglusPckFormat.list(src, opt)) if (e.name != SiglusPckFormat.META) enc.add(f.path + " › " + e.name)
                    } catch (e: Exception) { /* list only */ }
                    val dir = f.path.replace('\\', '/').substringBeforeLast('/', "")
                    var gx = 0
                    for (g in gameexes) {
                        if (g.path.replace('\\', '/').substringBeforeLast('/', "") != dir) continue
                        if (siglusGameexeOk(g, r.key)) { enc.add(g.path); gx++ }
                    }
                    val verified = if (r.ok) 2 else 1
                    val note = StringBuilder()
                    note.append(t("Dipulihkan dari data Scene.pck: ${r.verified} dari ${r.total} scene berhasil didekripsi dengan kunci ini.", "Recovered from the Scene.pck data: ${r.verified} of ${r.total} scenes decrypt correctly with this key."))
                    if (gx > 0) note.append(' ').append(t("Gameexe.dat juga cocok dengan kunci ini.", "Gameexe.dat matches this key too."))
                    else if (gameexes.isNotEmpty()) note.append(' ').append(t("Gameexe.dat di folder ini tidak cocok dengan kunci ini.", "Gameexe.dat here does not match this key."))
                    if (hasExe) note.append(' ').append(t("Kunci tidak tersimpan utuh di SiglusEngine.exe (dirakit saat game berjalan), jadi dicari dari data scene.", "The key is not stored whole in SiglusEngine.exe (it is assembled at run time), so it is derived from the scene data."))
                    hits.add(KeyHit("siglus", "Siglus Engine", t("Kunci exe (16 byte)", "Exe key (16 bytes)"), hex(r.key), hex(r.key), verified, f.path, note.toString(), enc))
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) {
                notes.add("${f.path}: " + (e.message ?: e.javaClass.simpleName))
            }
        }
        if (!foundPck && gameexes.any { gameexeEncrypted(it) }) {
            notes.add(t("Gameexe.dat Siglus terenkripsi, tetapi tidak ada Scene.pck di folder ini untuk mencari kuncinya.", "Siglus Gameexe.dat is encrypted, but there is no Scene.pck here to derive the key from."))
        }
    }

    private fun gameexeEncrypted(g: KfFile): Boolean = try {
        g.open().use { s ->
            val h = s.readFully(0, minOf(16L, s.length).toInt())
            SiglusGameexeFormat.detects(s, h) && h.le().getInt(4) != 0
        }
    } catch (e: Exception) { false }

    private fun siglusGameexeOk(g: KfFile, key: ByteArray): Boolean = try {
        g.open().use { s ->
            val h = s.readFully(0, minOf(16L, s.length).toInt())
            if (!SiglusGameexeFormat.detects(s, h) || h.le().getInt(4) == 0) return false
            val bo = ByteArrayOutputStream()
            val o = Options(extra = mapOf("skey" to hex(key)))
            SiglusGameexeFormat.extract(s, SiglusGameexeFormat.list(s, o)[0], bo, o)
            bo.size() > 0
        }
    } catch (e: Exception) { false }

    // ---------------- RPG Maker MV/MZ ----------------

    private fun rpgm(files: List<KfFile>, id: Boolean, hits: MutableList<KeyHit>, notes: MutableList<String>, cancelled: () -> Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val enc = files.filter { ext(it.path) in RPGM_EXT }
        if (enc.isEmpty()) return
        var fromJson: ByteArray? = null
        var jsonPath: String? = null
        for (f in files) {
            if (!name(f.path).equals("System.json", true) || f.size > 4L * 1024 * 1024) continue
            try {
                val txt = f.open().use { String(it.readFully(0, it.length.toInt()), Charsets.UTF_8) }
                val k = RpgmCrypt.keyFromSystemJson(txt)
                if (k != null) { fromJson = k; jsonPath = f.path; break }
            } catch (e: Exception) { /* continue */ }
        }
        var fromHeader: ByteArray? = null
        var headerSrc: String? = null
        for (f in enc) {
            if (cancelled()) return
            if (ext(f.path) != "rpgmvp" && ext(f.path) != "png_") continue
            try {
                val k = f.open().use { s -> RpgmCrypt.detectKey(s.readFully(0, minOf(32L, s.length).toInt())) }
                if (k != null) { fromHeader = k; headerSrc = f.path; break }
            } catch (e: Exception) { /* continue */ }
        }
        val list = enc.map { it.path }.sorted()
        val j = fromJson; val h = fromHeader
        when {
            j != null && h != null && j.contentEquals(h) -> hits.add(KeyHit("rpgm", "RPG Maker MV/MZ", t("Kunci enkripsi aset (16 byte)", "Asset encryption key (16 bytes)"), hex(j), hex(j), 2, jsonPath!!,
                t("Tertulis di System.json (encryptionKey) dan cocok dengan header PNG terenkripsi ($headerSrc).", "Stored in System.json (encryptionKey) and matches the encrypted PNG header ($headerSrc)."), list))
            j != null && h != null -> {
                hits.add(KeyHit("rpgm", "RPG Maker MV/MZ", t("Kunci enkripsi aset (16 byte)", "Asset encryption key (16 bytes)"), hex(h), hex(h), 2, headerSrc!!,
                    t("Diturunkan dari header PNG terenkripsi; ini kunci yang benar-benar dipakai aset.", "Derived from an encrypted PNG header; this is the key the assets really use."), list))
                notes.add(t("System.json ($jsonPath) memuat kunci berbeda (${hex(j)}); kunci dari header aset yang dipakai.", "System.json ($jsonPath) holds a different key (${hex(j)}); the key from the asset header is used."))
            }
            j != null -> hits.add(KeyHit("rpgm", "RPG Maker MV/MZ", t("Kunci enkripsi aset (16 byte)", "Asset encryption key (16 bytes)"), hex(j), hex(j), 1, jsonPath!!,
                t("Tertulis di System.json (encryptionKey); belum bisa dicocokkan dengan header PNG (tidak ada .rpgmvp/.png_).", "Stored in System.json (encryptionKey); could not be checked against a PNG header (no .rpgmvp/.png_)."), list))
            h != null -> hits.add(KeyHit("rpgm", "RPG Maker MV/MZ", t("Kunci enkripsi aset (16 byte)", "Asset encryption key (16 bytes)"), hex(h), hex(h), 2, headerSrc!!,
                t("System.json tidak memuat kunci; kunci diturunkan dari header PNG terenkripsi.", "System.json has no key; derived from an encrypted PNG header."), list))
            else -> notes.add(t("Ada ${enc.size} aset RPGM terenkripsi, tetapi kunci tidak bisa ditemukan (tidak ada System.json dengan encryptionKey dan tidak ada gambar PNG terenkripsi).", "${enc.size} encrypted RPGM assets found, but no key could be found (no System.json with encryptionKey and no encrypted PNG)."))
        }
    }

    // ---------------- archives with an XOR key (XP3) ----------------

    private fun archives(files: List<KfFile>, id: Boolean, hits: MutableList<KeyHit>, notes: MutableList<String>, cancelled: () -> Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        for (f in files) {
            if (cancelled()) return
            if (ext(f.path) in SKIP || ext(f.path) in RPGM_EXT || f.size < 16) continue
            try {
                f.open().use { src ->
                    val fmt = FormatRegistry.detect(src) ?: return@use
                    if (fmt.id.startsWith("siglus")) return@use
                    val list = fmt.list(src, Options())
                    val k = fmt.detectKey(src, list) ?: return@use
                    if (k == 0) {
                        notes.add(t("${f.path}: ${fmt.displayName} tidak memakai kunci XOR.", "${f.path}: ${fmt.displayName} uses no XOR key."))
                        return@use
                    }
                    val kh = String.format("0x%02x", k)
                    hits.add(KeyHit(fmt.id, fmt.displayName, t("Kunci XOR 1 byte", "1-byte XOR key"), kh, kh, 1, f.path,
                        t("Ditebak dari header isi arsip yang diketahui (kandidat dari suara terbanyak). ${list.size} entri.", "Guessed from known file headers inside the archive (majority vote). ${list.size} entries."),
                        listOf(f.path) + list.take(300).map { f.path + " › " + it.name }))
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) { /* not an archive / unreadable */ }
        }
    }

    // ---------------- CatSystem2 (INT password from the exe) ----------------

    private val CS_EXT = setOf("cst", "cct", "anm", "fes", "hg3", "hg2", "ogg", "wav", "png", "dat", "csv", "txt", "ini", "bmp", "jpg", "tga", "mpg", "wmv", "avi", "fnt", "cfg", "sys", "map", "ttf")

    private fun catsystem2(files: List<KfFile>, id: Boolean, hits: MutableList<KeyHit>, notes: MutableList<String>, cancelled: () -> Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        // encrypted .int archive
        val encInts = ArrayList<KfFile>()
        for (f in files) {
            if (cancelled()) return
            if (ext(f.path) != "int" || f.size < 0x60) continue
            try {
                f.open().use { src ->
                    val h = src.readFully(0, 0x14)
                    if (h[0] == 'K'.code.toByte() && h[1] == 'I'.code.toByte() && h[2] == 'F'.code.toByte() &&
                        IoUtil.cstr(h, 8, 12, Charsets.US_ASCII) == "__key__.dat") encInts.add(f)
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) { }
        }
        if (encInts.isEmpty()) return
        val passwords = LinkedHashMap<String, String>() // password -> exe
        for (f in files) {
            if (cancelled()) return
            if (ext(f.path) != "exe" || f.size < 0x1000 || f.size > 96L * 1024 * 1024) continue
            try {
                val pe = f.open().use { it.readFully(0, f.size.toInt()) }
                val pw = CatSystemCrypt.passwordFromExe(pe)
                if (pw != null && pw !in passwords) passwords[pw] = f.path
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) { }
        }
        if (passwords.isEmpty()) {
            passwords["windmill"] = t("(bawaan, tidak ada resource V_CODE2 di exe)", "(default, no V_CODE2 resource in exe)")
        }
        for ((pw, exe) in passwords) {
            val okFiles = ArrayList<String>()
            var good = 0
            for (f in encInts) {
                if (cancelled()) return
                try {
                    f.open().use { src ->
                        val n = src.readFully(4, 4).le().int
                        val idx = CatSystemCrypt.readIndex(src, n, pw, Charsets.UTF_8) ?: return@use
                        val ex = idx.entries.map { it.first.substringAfterLast('.', "").lowercase() }
                        val plausible = ex.isNotEmpty() && ex.count { it in CS_EXT } * 10 >= ex.size * 7
                        if (plausible) good++
                        okFiles.add(f.path + " (" + idx.entries.size + t(" entri", " entries") + (if (plausible) "" else t(", nama meragukan", ", names doubtful")) + ")")
                    }
                } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                    throw e
                } catch (e: Throwable) { }
            }
            if (okFiles.isEmpty()) continue
            hits.add(KeyHit("catsystem2", "CatSystem2", t("Password arsip INT", "INT archive password"), pw, pw,
                if (good == encInts.size) 2 else 1, exe,
                t("Password dari resource V_CODE2 pada exe. Nama file di indeks ${encInts.size} arsip .int terenkripsi dibaca dengan password ini.",
                    "Password from the exe's V_CODE2 resource. File names in ${encInts.size} encrypted .int archive(s) read with it."),
                okFiles))
        }
        if (hits.none { it.engineId == "catsystem2" })
            notes.add(t("Arsip .int terenkripsi ditemukan tetapi password tidak bisa dipulihkan. Sertakan file .exe game di folder yang sama.",
                "Encrypted .int archives found but the password could not be recovered. Include the game's .exe in the folder."))
    }

    /** EntisGLS: NOA archive password from the IDR_COTOMI resource (#10) in the exe (ERISA-N compressed XML: archive[@path][@key]). */
    private fun entisgls(files: List<KfFile>, id: Boolean, hits: MutableList<KeyHit>, notes: MutableList<String>, cancelled: () -> Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val noas = files.filter { f ->
            val e = ext(f.path)
            (e == "noa" || e == "dat" || e == "rsa" || e == "arc" || e == "emc") && f.size >= 0x50 && try {
                f.open().use { NoaFormat.matches(it.readFully(0, 16)) }
            } catch (e2: kotlin.coroutines.cancellation.CancellationException) { throw e2 } catch (e2: Throwable) { false }
        }
        if (noas.isEmpty()) return
        val keys = LinkedHashMap<String, Pair<String, String>>() // archive name (lowercase) -> (key, exe)
        for (f in files) {
            if (cancelled()) return
            if (ext(f.path) != "exe" || f.size < 0x1000 || f.size > 256L * 1024 * 1024) continue
            try {
                val pe = f.open().use { it.readFully(0, f.size.toInt()) }
                val res = CatSystemCrypt.findResource(pe, "#10", "IDR_COTOMI") ?: continue
                val raw = EntisCodec.nemesisDecode(res, 0, res.size, 8 * 1024 * 1024)
                val xml = if (raw.size >= 2 && raw[0] == 0xFF.toByte() && raw[1] == 0xFE.toByte()) String(raw, 2, raw.size - 2, Charsets.UTF_16LE) else String(raw, Charsets.UTF_8)
                for (m in Regex("<archive\\b[^>]*>").findAll(xml)) {
                    val tag = m.value
                    val path = Regex("\\bpath\\s*=\\s*\"([^\"]*)\"").find(tag)?.groupValues?.get(1) ?: continue
                    val key = Regex("\\bkey\\s*=\\s*\"([^\"]*)\"").find(tag)?.groupValues?.get(1) ?: continue
                    keys.putIfAbsent(name(path).lowercase(), Pair(key, f.path))
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
            } catch (e: Throwable) { }
        }
        for (f in noas) {
            val k = keys[name(f.path).lowercase()] ?: continue
            hits.add(KeyHit("entisgls", "EntisGLS", t("Password arsip NOA", "NOA archive password"), k.first, k.first, 1, k.second,
                t("Password dari resource IDR_COTOMI pada exe.",
                    "Password from the exe's IDR_COTOMI resource."),
                listOf(f.path)))
        }
        if (hits.none { it.engineId == "entisgls" })
            notes.add(t("Arsip NOA ditemukan tetapi password tidak ditemukan di exe (mungkin tidak terenkripsi, atau exe tidak ada/terbungkus). Arsip tak terenkripsi dibuka tanpa password.",
                "NOA archives found but no password in the exe (maybe not encrypted, or the exe is missing/packed). Unencrypted archives open without a password."))
    }

    // ---------------- report text ----------------

    fun reportText(folder: String, r: KeyReport, id: Boolean): String {
        fun t(a: String, b: String) = if (id) a else b
        val sb = ReportText.header(t("Laporan Pencari Kunci", "Key Finder Report"), id)
        sb.append(t("Folder: ", "Folder: ")).append(folder).append('\n')
        sb.append(t("File dipindai: ", "Files scanned: ")).append(r.scanned).append('\n')
        sb.append(t("Kunci ditemukan: ", "Keys found: ")).append(r.hits.size).append('\n')
        for ((i, h) in r.hits.withIndex()) {
            ReportText.section(sb, "${i + 1}. ${h.engine}")
            sb.append(t("Status: ", "Status: ")).append(if (h.status == 2) t("Terverifikasi", "Verified") else t("Kandidat", "Candidate")).append('\n')
            sb.append(t("Jenis: ", "Type: ")).append(h.keyKind).append('\n')
            sb.append(t("Kunci: ", "Key: ")).append(h.key).append('\n')
            sb.append(t("Sumber: ", "Source: ")).append(h.source).append('\n')
            sb.append(h.note).append('\n')
            sb.append(t("File terenkripsi (", "Encrypted files (")).append(h.files.size).append("):\n")
            for (f in h.files) sb.append("  ").append(f).append('\n')
        }
        if (r.notes.isNotEmpty()) {
            ReportText.section(sb, t("Catatan", "Notes"))
            for (n in r.notes) sb.append("- ").append(n).append('\n')
        }
        return sb.toString()
    }
}
