package com.furuarc.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import com.furuarc.Screen
import com.furuarc.core.ScriptTool
import com.furuarc.core.ScriptTools
import com.furuarc.core.SpecialConv
import com.furuarc.i18n.NavStr
import com.furuarc.i18n.Str
import com.furuarc.i18n.pl
import com.furuarc.i18n.r
import com.furuarc.i18n.ScriptStrings
import com.furuarc.settings.Lang
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalStr

/** One searchable feature: [id] is resolved to a navigation action in MainActivity (openFeature). [keys] = extra search words (ID + EN). */
class Feature(val id: String, val icon: ImageVector, val title: String, val path: String, val keys: String) {
    val tl = title.lowercase()
    val pl = path.lowercase()
    val kl = keys.lowercase()
}

object FeatureCatalog {
    @Composable
    fun rememberAll(): List<Feature> {
        val lang = LocalLang.current
        val s = LocalStr.current
        return remember(lang) { build(lang, s) }
    }

    private fun build(lang: Lang, s: Str): List<Feature> {
        val nav = NavStr.of(lang)
        val md = com.furuarc.i18n.mediaStr(lang)
        val tx = com.furuarc.i18n.textEditStr(lang)
        val ck = com.furuarc.i18n.checkStr(lang)
        val ft = com.furuarc.i18n.fileToolsStr(lang)
        val no = com.furuarc.i18n.nameOptStr(lang)
        val ts = ScriptStrings.of(lang)
        val ab = com.furuarc.i18n.AboutStr.of(lang)
        val pls = com.furuarc.i18n.playerStr(lang)
        val out = ArrayList<Feature>()
        fun f(id: String, icon: ImageVector, title: String, path: String, keys: String) { out.add(Feature(id, icon, title, path, keys)) }
        val tab = nav.tabTools
        val vnP = "$tab › ${s.menuVnTitle}"
        val rpP = "$tab › ${s.menuRpgmTitle}"
        val mdP = "$tab › ${md.menuTitle}"
        val txP = "$tab › ${tx.menuTitle}"
        val ckP = "$tab › ${ck.menuTitle}"
        val ftP = "$tab › ${ft.menuTitle}"
        val plP = "$rpP › ${s.pl.mgrTitle}"

        f("library", AppIcons.Library, nav.tabAssets, nav.tabAssets, "game library pustaka aset play putar gambar audio video teks image browse")
        // Game Tools tab
        f("vn", AppIcons.MenuBook, s.menuVnTitle, tab, "vn visual novel engine")
        f("rpgm", AppIcons.Gamepad, s.menuRpgmTitle, tab, "rpg maker rpgm mv mz xp vx ace")
        f("editor", AppIcons.Edit, s.r.edTitle, tab, "terjemahan translation editor draft json csv tl translate")
        f("textedit", AppIcons.Description, tx.menuTitle, tab, "text editor teks txt json csv xml ubah edit")
        val ie = com.furuarc.i18n.imageEditStr(lang)
        f("imageedit", AppIcons.Brush, ie.menuTitle, tab, "image editor editor gambar potong crop resize ubah ukuran teks text kuas brush warna color pipet png jpg webp lokalisasi")
        f("textedit.plain", AppIcons.TextFields, tx.plainTitle, txP, "txt text plain teks biasa encoding")
        f("textedit.struct", AppIcons.Layers, tx.structTitle, txP, "json csv xml structured terstruktur tree pohon")
        f("textedit.bulk", AppIcons.SwapHoriz, tx.bulkTitle, txP, "replace ganti massal bulk regex cari")
        val mpP = "$tab › ${pls.menuTitle}"
        f("player", AppIcons.MediaPlayer, pls.menuTitle, tab, "media player pemutar putar play gambar audio video galeri gallery slideshow")
        f("player.image", AppIcons.Image, pls.imageTitle, mpP, "png jpg jpeg webp bmp gif heic gambar image galeri gallery view lihat")
        f("player.audio", AppIcons.MusicNote, pls.audioTitle, mpP, "mp3 ogg wav flac aac m4a opus audio musik music playlist")
        f("player.video", AppIcons.Videocam, pls.videoTitle, mpP, "mp4 mkv webm 3gp avi mov video film movie")
        f("player.settings", AppIcons.Gear, pls.settingsTitle, mpP, "pengaturan media settings ekspor impor export import")
        f("media", AppIcons.SwapHoriz, md.menuTitle, tab, "media converter konverter konversi convert audio gambar image video")
        f("media.audio", AppIcons.MusicNote, md.audioTitle, mdP, "mp3 ogg wav flac aac m4a opus audio suara sound")
        f("media.image", AppIcons.Image, md.imageTitle, mdP, "png jpg jpeg webp bmp gif gambar image")
        f("media.video", AppIcons.Videocam, md.videoTitle, mdP, "mp4 mkv avi webm mpg video")
        f("media.mixed", AppIcons.Layers, md.mixTitle, mdP, "campuran mixed folder audio gambar video sekaligus")
        f("media.special", AppIcons.Gamepad, md.specialTitle, mdP, "khusus special engine crx pgd pna hg2 hg3 tlg eri g00 rct rc8 ycg pcm nwa mio emi ovk spm xpcm")
        f("media.text", AppIcons.TextFields, md.textTitle, mdP, "encoding utf-8 shift-jis sjis gbk teks text konversi")
        for (e in SpecialConv.ENGINES) {
            f("special:" + e.id, AppIcons.Gamepad, e.name + " · " + md.specialTitle, "$mdP › ${md.specialTitle}", e.exts.joinToString(" ") + " " + e.id + " convert konversi gambar image audio")
        }
        f("filecheck", AppIcons.Archive, ck.menuTitle, tab, "pemeriksa pemeriksaan periksa check inspect inspector arsip skrip file folder uji test")
        f("filecheck.file", AppIcons.Archive, ck.fileTitle, ckP, "periksa file archive arsip format deteksi detect inspect")
        f("filecheck.folder", AppIcons.Folder, ck.folderTitle, ckP, "periksa folder statistik stats")
        f("filecheck.compare", AppIcons.File, ck.compareTitle, ckP, "bandingkan compare diff")
        f("filecheck.hex", AppIcons.MenuBook, ft.hexTitle, ckP, "hex viewer penampil biner binary")
        f("filecheck.formattest", AppIcons.Checklist, ck.ftTitle, ckP, "uji coba format test round trip kemas bongkar pack unpack self test diagnosa")
        val fd = com.furuarc.i18n.folderDiffStr(lang)
        f("filecheck.folderdiff", AppIcons.Folder, fd.title, ckP, "bandingkan folder compare folders diff perbedaan berubah changed salin copy patch")
        f("filecheck.dup", AppIcons.ContentCopy, ft.dupTitle, ckP, "duplikat duplicate kembar hash")
        f("filecheck.tr", AppIcons.Translate, ck.trScanTitle, ckP, "terjemahan scan translation pindai teks")
        f("filetools", AppIcons.Build, ft.menuTitle, tab, "alat file potong pisah gabung rename ganti nama")
        f("filetools.cut", AppIcons.File, ck.cutTitle, ftP, "potong cut trim file")
        f("filetools.split", AppIcons.Unarchive, ft.splitTitle, ftP, "pisah split bagi part")
        f("filetools.join", AppIcons.Archive, ft.joinTitle, ftP, "gabung join merge part")
        f("filetools.rename", AppIcons.Edit, ft.renTitle, ftP, "rename ganti nama massal batch")
        f("nameopt", AppIcons.RenameOutline, no.menuTitle, tab, "nama non-latin ascii rename optimasi optimizer jepang cina japanese chinese apk")
        f("keyfinder", AppIcons.LockOpen, ck.kfTitle, tab, "key kunci password cari find decrypt dekripsi xor")
        // VN tools
        f("vn.engines", AppIcons.Gamepad, ts.engineListTitle, vnP, "daftar engine list")
        f("vn.unpack", AppIcons.Unarchive, ts.autoUnpackTitle, vnP, "bongkar unpack extract ekstrak arsip archive xp3 pfs rpa pck arc dat pac")
        f("vn.pack", AppIcons.Archive, ts.autoPackTitle, vnP, "kemas pack repack arsip archive buat")
        f("vn.scripts", AppIcons.Edit, ts.autoScriptsTitle, vnP, "skrip script teks text ekspor impor export import")
        f("vn.script.export", AppIcons.Edit, ts.title(ScriptTool.EXPORT), "$vnP › ${ts.autoScriptsTitle}", "ekspor export teks text skrip script terjemahan dialog")
        f("vn.script.import", AppIcons.Edit, ts.title(ScriptTool.IMPORT), "$vnP › ${ts.autoScriptsTitle}", "impor import teks text skrip script terjemahan dialog")
        f("vn.script.list", AppIcons.Checklist, ts.autoListTitle, "$vnP › ${ts.autoScriptsTitle}", "daftar isi list contents arsip archive")
        for (e in com.furuarc.ui.VN_ENGINES) {
            if (e.parent != null) continue
            val vars = com.furuarc.ui.VN_ENGINES.filter { it.parent == e.id }
            val exts = (listOf(e.ext, e.badge, e.id) + vars.flatMap { listOf(it.ext, it.badge, it.suffix) }).filter { it.isNotBlank() }.distinct().joinToString(" ")
            f("engine:" + e.id, AppIcons.Archive, e.name, "$vnP › ${ts.engineListTitle}", "$exts unpack pack bongkar kemas arsip archive engine")
        }
        // RPGM tools
        f("rpgm.assets", AppIcons.Image, s.rpgmAssetsTitle, rpP, "daftar engine aset asset decrypt enkripsi rgss mv mz xp vx ace")
        for (e in com.furuarc.ui.RPGM_ENGINES) {
            f("engine:" + e.id, AppIcons.Archive, e.name, "$rpP › ${s.rpgmAssetsTitle}", "${e.ext} ${e.badge} rpgm rpg maker decrypt encrypt dekripsi enkripsi aset")
        }
        f("rpgm.translate", AppIcons.Translate, s.rpgmTranslateTitle, rpP, "terjemahan translate translation teks text ekspor impor")
        f("rpgm.tr.export", AppIcons.FolderUpload, s.trExportTitle, "$rpP › ${s.rpgmTranslateTitle}", "ekspor export teks text dialog json csv")
        f("rpgm.tr.import", AppIcons.Translate, s.trImportTitle, "$rpP › ${s.rpgmTranslateTitle}", "impor import teks text terjemahan translation")
        f("rpgm.cheat", AppIcons.Edit, s.rpgmCheatTitle, rpP, "cheat curang save simpanan nilai value uang gold")
        f("rpgm.save.export", AppIcons.FolderUpload, s.saveExportTitle, "$rpP › ${s.rpgmCheatTitle}", "save simpanan ekspor export rpgsave rmmzsave")
        f("rpgm.save.import", AppIcons.Save, s.saveImportTitle, "$rpP › ${s.rpgmCheatTitle}", "save simpanan impor import rpgsave rmmzsave")
        f("rpgm.value", AppIcons.Edit, s.menuValueTitle, "$rpP › ${s.rpgmCheatTitle}", "nilai value editor uang gold switch variabel variable")
        f("rpgm.plugins", AppIcons.Build, s.pl.mgrTitle, rpP, "plugin plugins js manager pengelola")
        f("rpgm.plugins.scan", AppIcons.Folder, s.pl.scanTitle, plP, "plugin scan pindai js")
        f("rpgm.plugins.edit", AppIcons.Edit, s.pl.editTitle, plP, "plugin edit ubah daftar list js")
        f("rpgm.plugins.check", AppIcons.Build, s.pl.chkTitle, plP, "plugin check periksa cek js")
        f("rpgm.plugins.diff", AppIcons.Archive, s.pl.cmpTitle, plP, "plugin compare bandingkan diff js")
        // Settings tab
        f("settings", AppIcons.Gear, s.menuSettingsTitle, nav.tabSettings, "pengaturan settings tema theme bahasa language aksen accent warna")
        f("guide", AppIcons.MenuBook, ab.title, nav.tabSettings, "panduan guide tentang about cara pakai")
        f("howto", AppIcons.File, ab.howTitle, "${nav.tabSettings} › ${ab.title}", "cara penggunaan how to guide panduan")
        f("qna", AppIcons.Edit, ab.qnaTitle, "${nav.tabSettings} › ${ab.title}", "qna faq tanya jawab pertanyaan")
        f("trouble", AppIcons.Build, ab.troubleTitle, "${nav.tabSettings} › ${ab.title}", "masalah troubleshooting error gagal")
        f("source", AppIcons.Folder, ab.srcTitle, nav.tabSettings, "sumber source github lisensi license terima kasih credits")
        f("dev", AppIcons.Gamepad, ab.devTitle, nav.tabSettings, "pengembang developer kontak donasi donate")
        return out
    }

    /** Feature id for a screen that is a feature on its own (used to fill "Recently used"); null = not recorded. */
    fun idFor(screen: Screen, mode: Int, engineId: String?, engineParent: String?): String? = when (screen) {
        Screen.LIBRARY -> "library"
        Screen.MPLAYER -> "player"
        Screen.MPLAYER_IMAGE -> "player.image"
        Screen.MPLAYER_AUDIO -> "player.audio"
        Screen.MPLAYER_VIDEO -> "player.video"
        Screen.MPLAYER_SETTINGS -> "player.settings"
        Screen.TR_EDIT_JOB -> "editor"
        Screen.IMAGEEDIT -> "imageedit"
        Screen.TEXTEDIT_PLAIN -> "textedit.plain"
        Screen.TEXTEDIT_STRUCT -> "textedit.struct"
        Screen.TEXTEDIT_BULK -> "textedit.bulk"
        Screen.MEDIA_AUDIO -> "media.audio"
        Screen.MEDIA_IMAGE -> "media.image"
        Screen.MEDIA_VIDEO -> "media.video"
        Screen.MEDIA_MIXED -> "media.mixed"
        Screen.MEDIA_TEXT -> "media.text"
        Screen.MEDIA_SPECIAL -> "media.special"
        Screen.INSPECT -> "filecheck.file"
        Screen.INSPECT_FOLDER -> "filecheck.folder"
        Screen.INSPECT_COMPARE -> "filecheck.compare"
        Screen.INSPECT_HEX -> "filecheck.hex"
        Screen.INSPECT_FORMATTEST -> "filecheck.formattest"
        Screen.INSPECT_DUP -> "filecheck.dup"
        Screen.INSPECT_FOLDERDIFF -> "filecheck.folderdiff"
        Screen.INSPECT_TR -> "filecheck.tr"
        Screen.FILECUT -> "filetools.cut"
        Screen.FILESPLIT -> "filetools.split"
        Screen.FILEJOIN -> "filetools.join"
        Screen.FILERENAME -> "filetools.rename"
        Screen.NAMEOPT -> "nameopt"
        Screen.KEYFINDER -> "keyfinder"
        Screen.RPGM_ASSETS -> "rpgm.assets"
        Screen.PLUGIN_SCAN -> "rpgm.plugins.scan"
        Screen.PLUGIN_EDIT -> "rpgm.plugins.edit"
        Screen.PLUGIN_CHECK -> "rpgm.plugins.check"
        Screen.PLUGIN_DIFF -> "rpgm.plugins.diff"
        Screen.TR_JOB -> if (mode == 6) "rpgm.tr.import" else "rpgm.tr.export"
        Screen.SAVE_JOB -> if (mode == 8) "rpgm.save.import" else "rpgm.save.export"
        Screen.VALUE_JOB -> "rpgm.value"
        Screen.UNPACK -> if (engineId == ScriptTools.AUTO) "vn.unpack" else null
        Screen.PACK -> if (engineId == ScriptTools.AUTO) "vn.pack" else null
        Screen.TOOL_JOB -> if (engineId == ScriptTools.AUTO) when (mode) { 20 -> "vn.script.export"; 21 -> "vn.script.import"; 25 -> "vn.script.list"; else -> null } else null
        Screen.ENGINE -> engineId?.let { "engine:" + (engineParent ?: it) }
        else -> null
    }

    /** Features whose keywords contain exactly the file extension [ext] (without dot), plus the generic inspection tools as a fallback. */
    private val PRELOAD = setOf(
        "vn.unpack", "vn.script.list", "editor", "rpgm.save.export", "rpgm.save.import",
        "filecheck.file", "filecheck.tr", "filecheck.hex", "filetools.cut", "filetools.split",
        "media.audio", "media.image", "media.video", "media.text", "media.special", "player.image", "player.audio", "player.video",
        "textedit.plain", "textedit.struct", "rpgm.plugins.scan", "rpgm.plugins.edit", "rpgm.plugins.check",
    )

    /** True when the feature can load a file picked on the Home tab straight away (no second pick needed). */
    fun canPreload(id: String): Boolean {
        if (id in PRELOAD || id.startsWith("special:")) return true
        if (!id.startsWith("engine:")) return false
        val e = com.furuarc.ui.vnEngine(id.substring(7))
        return !e.rpgm && !e.crypt && com.furuarc.ui.VN_ENGINES.none { it.parent == e.id }
    }

    fun forExtension(all: List<Feature>, ext: String): List<Feature> {
        val e = ext.lowercase().trimStart('.')
        val hits = if (e.isEmpty()) emptyList() else all.filter { f -> !f.id.startsWith("special:") && f.kl.split(' ').any { it == e } }
        return hits.sortedBy { if (it.id.startsWith("engine:")) 0 else if (it.id.startsWith("media.")) 1 else 2 }
    }

    /** Search: every word of the query must match (title, extra keywords, or path). Best matches first. */
    fun search(all: List<Feature>, q: String): List<Feature> {
        val toks = q.lowercase().split(Regex("\\s+")).map { it.trimStart('.') }.filter { it.isNotEmpty() }
        if (toks.isEmpty()) return emptyList()
        val scored = ArrayList<Pair<Feature, Int>>()
        for (f in all) {
            var total = 0
            var ok = true
            for (t in toks) {
                val sc = score(f, t)
                if (sc == 0) { ok = false; break }
                total += sc
            }
            if (ok) scored.add(f to total)
        }
        return scored.sortedWith(compareByDescending<Pair<Feature, Int>> { it.second }.thenBy { it.first.title }).map { it.first }
    }

    private fun score(f: Feature, t: String): Int {
        var s = 0
        val words = f.tl.split(' ', '/', '(', ')', '-', '·', '—', '›')
        if (f.tl == t) s = 120
        else if (f.tl.startsWith(t)) s = 100
        else if (words.any { it.startsWith(t) }) s = 85
        else if (f.tl.contains(t)) s = 60
        val kw = f.kl.split(' ')
        if (kw.any { it == t }) s = maxOf(s, 90)
        else if (kw.any { it.startsWith(t) }) s = maxOf(s, 55)
        else if (t.length >= 3 && f.kl.contains(t)) s = maxOf(s, 35)
        if (s == 0 && f.pl.contains(t)) s = 15
        return s
    }
}
