package com.furuarc.core

/**
 * One converted file to be checked. [folder] = parent folder path (lowercase, separator '/'), [w]/[h] = image size (0 if not an image),
 * [text] = TEXT_* for text files (TEXT_NA if not text or not yet known).
 */
class CompatFile(val name: String, val folder: String, val size: Long, val w: Int = 0, val h: Int = 0, val text: Int = TEXT_NA) {
    companion object {
        const val TEXT_NA = -1
        const val TEXT_UTF8 = 0
        const val TEXT_UTF8_BOM = 1
        const val TEXT_UTF16 = 2
        const val TEXT_LEGACY = 3 // not valid UTF-8 (Shift-JIS, GBK, etc.)
    }
}

/** [level]: 0 info, 1 warning, 2 mismatch. */
class CompatIssue(val level: Int, val file: String, val text: String)

/**
 * Compatibility Checker: matches converted files against the general rules of an engine
 * (recognized formats, file names, image size, text encoding). General rules, not a guarantee it works on every engine version.
 */
object CompatCheck {
    const val RPGM = "rpgm"
    const val RENPY = "renpy"
    const val KIRIKIRI = "kirikiri"
    const val TYRANO = "tyrano"
    val ENGINES = listOf(RPGM, RENPY, KIRIKIRI, TYRANO)

    private val AUDIO = setOf("ogg", "oga", "opus", "m4a", "aac", "mp3", "wav", "flac", "wma", "mid", "midi")
    private val IMAGE = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif", "tga", "tlg", "avif")
    private val VIDEO = setOf("mp4", "m4v", "webm", "ogv", "mkv", "avi", "mpg", "mpeg", "wmv")
    private val TEXT = setOf("txt", "ks", "tjs", "json", "rpy", "csv", "ini", "js", "html")

    private class Rules(
        val audioOk: Set<String>, val imageOk: Set<String>, val videoOk: Set<String>,
        /** true = formats outside the OK list are treated as mismatches (level 2), false = warning only */
        val strictAudio: Boolean, val strictImage: Boolean, val strictVideo: Boolean,
    )

    private val RULES = mapOf(
        RPGM to Rules(setOf("ogg", "m4a"), setOf("png"), setOf("webm", "mp4"), true, true, false),
        RENPY to Rules(setOf("ogg", "opus", "mp3", "wav", "flac"), setOf("png", "jpg", "jpeg", "webp", "avif"), setOf("webm", "ogv", "mp4"), false, false, false),
        KIRIKIRI to Rules(setOf("ogg", "wav"), setOf("png", "jpg", "jpeg", "bmp", "tlg"), setOf("mpg", "mpeg", "mp4", "wmv"), false, false, false),
        TYRANO to Rules(setOf("ogg", "m4a", "mp3"), setOf("png", "jpg", "jpeg", "webp", "gif"), setOf("mp4", "webm"), false, false, false),
    )

    fun engineName(e: String, id: Boolean) = when (e) {
        RPGM -> "RPG Maker MV/MZ"
        RENPY -> "Ren'Py"
        KIRIKIRI -> if (id) "Kirikiri / Kirikiroid2" else "Kirikiri / Kirikiroid2"
        else -> "TyranoScript"
    }

    private fun ext(n: String) = n.substringAfterLast('.', "").lowercase()

    private fun hasSeg(folder: String, seg: String): Boolean = folder.split('/').any { it == seg }

    fun check(engine: String, files: List<CompatFile>, id: Boolean): List<CompatIssue> {
        fun t(a: String, b: String) = if (id) a else b
        val r = RULES[engine] ?: return emptyList()
        val out = ArrayList<CompatIssue>()
        fun add(l: Int, f: CompatFile, text: String) { out.add(CompatIssue(l, f.name, text)) }
        val eng = engineName(engine, id)
        val webBased = engine == RPGM || engine == TYRANO

        // audio .ogg/.m4a pairs (RPGM): base name -> extensions present in the same folder
        val pairs = HashMap<String, MutableSet<String>>()
        if (engine == RPGM) {
            for (f in files) {
                val e = ext(f.name)
                if (e == "ogg" || e == "m4a") pairs.getOrPut(f.folder + "/" + f.name.substringBeforeLast('.').lowercase()) { HashSet() }.add(e)
            }
        }

        for (f in files) {
            val e = ext(f.name)
            if (f.size <= 0L) add(2, f, t("File kosong (0 byte).", "Empty file (0 bytes)."))

            // file name
            if (webBased && (f.name.contains('#') || f.name.contains('%'))) {
                add(2, f, t("Nama file berisi # atau %, yang merusak alamat file di engine berbasis web.", "File name contains # or %, which breaks the file address in web-based engines."))
            } else if (f.name.any { it.code > 126 } || f.name.contains(' ')) {
                if (engine == RENPY && f.name.contains(' ') && e in IMAGE) {
                    add(1, f, t("Ren'Py membaca spasi di nama gambar sebagai pemisah atribut, jadi nama aslinya tidak terbaca utuh.", "Ren'Py treats spaces in image names as attribute separators, so the full name is not read as one."))
                } else {
                    add(1, f, t("Nama berisi spasi/karakter non-ASCII; di Android (huruf besar-kecil dibedakan) nama harus cocok persis dengan yang dirujuk game.", "Name has spaces or non-ASCII characters; on Android (case-sensitive) it must match exactly what the game refers to."))
                }
            }

            when {
                e in AUDIO -> {
                    if (e !in r.audioOk) {
                        val lvl = if (r.strictAudio) 2 else 1
                        add(lvl, f, when (engine) {
                            RPGM -> t(".$e tidak diputar $eng; hanya .ogg dan .m4a.", ".$e is not played by $eng; only .ogg and .m4a.")
                            RENPY -> t(".$e tidak termasuk format audio resmi $eng (Opus, Ogg Vorbis, MP3, WAV, FLAC).", ".$e is not an official $eng audio format (Opus, Ogg Vorbis, MP3, WAV, FLAC).")
                            KIRIKIRI -> t(".$e hanya jalan bila engine/plugin-nya mendukung; yang aman .ogg atau .wav.", ".$e works only if the engine/plugin supports it; .ogg or .wav are safe.")
                            else -> t(".$e kurang lazim di $eng; yang aman .ogg, .m4a, atau .mp3.", ".$e is uncommon in $eng; .ogg, .m4a or .mp3 are safe.")
                        })
                    } else if (engine == RPGM && hasSeg(f.folder, "audio")) {
                        val set = pairs[f.folder + "/" + f.name.substringBeforeLast('.').lowercase()]
                        if (set != null && set.size < 2) {
                            add(0, f, t("Pasangan .${if (e == "ogg") "m4a" else "ogg"} tidak ada. RPG Maker memakai .ogg di Windows/Android dan .m4a di iOS/Safari; tanpa pasangan, audio bisa diam di platform lain.", "No .${if (e == "ogg") "m4a" else "ogg"} counterpart. RPG Maker uses .ogg on Windows/Android and .m4a on iOS/Safari; without it audio may be silent on other platforms."))
                        }
                    }
                    if (e == "wav" && engine == TYRANO) add(0, f, t("WAV sangat besar untuk HTML5; pertimbangkan .ogg atau .m4a.", "WAV is very large for HTML5; consider .ogg or .m4a."))
                }
                e in IMAGE -> {
                    if (e !in r.imageOk) {
                        add(if (r.strictImage) 2 else 1, f, when (engine) {
                            RPGM -> t("$eng memuat gambar sebagai .png (nama dilengkapi .png oleh engine); .$e tidak akan terbaca.", "$eng loads images as .png (the engine appends .png); .$e will not be found.")
                            RENPY -> t(".$e tidak termasuk format gambar $eng (PNG, JPG, WEBP, AVIF).", ".$e is not an $eng image format (PNG, JPG, WEBP, AVIF).")
                            KIRIKIRI -> t(".$e tidak dibaca KAG standar; yang aman PNG, JPG, BMP, TLG.", ".$e is not read by standard KAG; PNG, JPG, BMP, TLG are safe.")
                            else -> t(".$e kurang lazim di $eng; yang aman PNG, JPG, WEBP.", ".$e is uncommon in $eng; PNG, JPG, WEBP are safe.")
                        })
                    }
                    if (f.w > 4096 || f.h > 4096) {
                        add(1, f, t("Ukuran ${f.w}×${f.h} melebihi 4096 px; GPU banyak HP gagal memuat tekstur sebesar ini.", "Size ${f.w}×${f.h} exceeds 4096 px; many phone GPUs fail to load textures this large."))
                    }
                    if (engine == RPGM && f.w > 0 && f.h > 0 && e == "png") rpgmGrid(f, ::add, id)
                }
                e in VIDEO -> {
                    if (e !in r.videoOk) {
                        add(1, f, when (engine) {
                            RPGM -> t(".$e kurang lazim di $eng; yang aman .webm (VP8/VP9) dan .mp4 (H.264).", ".$e is uncommon in $eng; .webm (VP8/VP9) and .mp4 (H.264) are safe.")
                            RENPY -> t(".$e belum tentu diputar $eng; yang aman .webm, .ogv, .mp4.", ".$e may not play in $eng; .webm, .ogv, .mp4 are safe.")
                            KIRIKIRI -> t(".$e hanya jalan dengan plugin video tertentu; yang lazim .mpg/.wmv/.mp4.", ".$e needs a specific video plugin; .mpg/.wmv/.mp4 are common.")
                            else -> t(".$e kurang lazim di $eng; yang aman .mp4 dan .webm.", ".$e is uncommon in $eng; .mp4 and .webm are safe.")
                        })
                    }
                }
                e in TEXT -> textRules(engine, f, e, ::add, id)
            }
        }
        // order: mismatches first, then warnings, then info
        return out.sortedWith(compareByDescending<CompatIssue> { it.level }.thenBy { it.file.lowercase() })
    }

    private fun rpgmGrid(f: CompatFile, add: (Int, CompatFile, String) -> Unit, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val n = f.name.lowercase()
        fun bad(mw: Int, mh: Int, what: String) {
            if (f.w % mw != 0 || f.h % mh != 0) {
                add(1, f, t("Ukuran ${f.w}×${f.h} bukan kelipatan $mw×$mh (aturan $what); bagian gambar bisa terpotong atau bergeser.", "Size ${f.w}×${f.h} is not a multiple of $mw×$mh (rule for $what); parts of the image may be cut off or shifted."))
            }
        }
        when {
            hasSeg(f.folder, "tilesets") -> bad(48, 48, t("tileset", "tilesets"))
            hasSeg(f.folder, "faces") -> bad(144, 144, t("face", "faces"))
            hasSeg(f.folder, "characters") -> if (n.startsWith("$")) bad(3, 4, t("karakter tunggal", "single character")) else bad(12, 8, t("lembar karakter", "character sheet"))
            hasSeg(f.folder, "sv_actors") -> bad(9, 6, "sv_actors")
            hasSeg(f.folder, "system") && n == "iconset.png" -> bad(32, 32, "Iconset")
        }
    }

    private fun textRules(engine: String, f: CompatFile, e: String, add: (Int, CompatFile, String) -> Unit, id: Boolean) {
        fun t(a: String, b: String) = if (id) a else b
        val eng = engineName(engine, id)
        val relevant = when (engine) {
            RPGM -> setOf("json", "js")
            RENPY -> setOf("rpy")
            KIRIKIRI -> setOf("ks", "tjs", "txt")
            else -> setOf("ks", "js", "html", "json")
        }
        if (e !in relevant) return
        when (f.text) {
            CompatFile.TEXT_NA -> { }
            CompatFile.TEXT_LEGACY -> {
                if (engine != KIRIKIRI) add(2, f, t("Bukan UTF-8; $eng membaca teks sebagai UTF-8 sehingga huruf jadi rusak.", "Not UTF-8; $eng reads text as UTF-8 so characters will be garbled."))
            }
            CompatFile.TEXT_UTF16 -> {
                if (engine != KIRIKIRI) add(2, f, t("UTF-16; $eng mengharapkan UTF-8.", "UTF-16; $eng expects UTF-8."))
            }
            CompatFile.TEXT_UTF8_BOM -> {
                if (engine == RPGM && e == "json") add(1, f, t("UTF-8 dengan BOM; JSON.parse di engine web bisa gagal membaca file ini.", "UTF-8 with BOM; JSON.parse in the web engine may fail to read this file."))
                else if (engine == TYRANO && e == "ks") add(1, f, t("UTF-8 dengan BOM; BOM bisa merusak tag pertama di skrip.", "UTF-8 with BOM; the BOM can break the first tag of the script."))
            }
            CompatFile.TEXT_UTF8 -> {
                if (engine == KIRIKIRI && (e == "ks" || e == "tjs")) {
                    add(1, f, t("UTF-8 tanpa BOM; Kirikiri membaca teks tanpa BOM sebagai ANSI (Shift-JIS), sehingga huruf bisa rusak. Beri BOM UTF-8 atau pakai UTF-16.", "UTF-8 without BOM; Kirikiri reads text without BOM as ANSI (Shift-JIS) so characters can be garbled. Add a UTF-8 BOM or use UTF-16."))
                }
            }
        }
    }

    /** Determines the encoding type from the start of the file ([head], at least 4 bytes if available) and whether the whole content is valid UTF-8 ([utf8Valid]). */
    fun classifyText(head: ByteArray, utf8Valid: Boolean): Int {
        if (head.size >= 3 && head[0] == 0xEF.toByte() && head[1] == 0xBB.toByte() && head[2] == 0xBF.toByte()) return CompatFile.TEXT_UTF8_BOM
        if (head.size >= 2 && ((head[0] == 0xFF.toByte() && head[1] == 0xFE.toByte()) || (head[0] == 0xFE.toByte() && head[1] == 0xFF.toByte()))) return CompatFile.TEXT_UTF16
        return if (utf8Valid) CompatFile.TEXT_UTF8 else CompatFile.TEXT_LEGACY
    }
}
