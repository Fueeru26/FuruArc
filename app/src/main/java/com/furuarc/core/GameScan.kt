package com.furuarc.core

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.ensureActive
import java.io.IOException
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction
import kotlin.coroutines.coroutineContext

/** Recognized game archive (xp3, rpa, etc.); used to open its contents. */
class ArcRef(val uri: Uri, val fmtId: String, val key: Int?, val path: String)

/** Status of one scanned archive ([error] != null = failed to read). */
class ArcStatus(val ref: ArcRef?, val path: String, val fmt: String, val count: Int, val error: String?)

class GFile(val path: String, val size: Long, val uri: Uri, val mtime: Long = 0L)

/** Media asset. [kind]: 0 audio, 1 image, 2 video. [enc] = RPGM encrypted. [arc]/[entry] = asset is inside an archive. */
class GAsset(val path: String, val size: Long, val uri: Uri, val kind: Int, val enc: Boolean, val arc: ArcRef? = null, val entry: String = "", val special: Boolean = false) {
    val name: String get() = path.substringAfterLast('/')
    val id: String get() = if (arc == null) uri.toString() else arc.uri.toString() + "#" + entry
}

/** Text menu item. [type]: 0 text file, 1 script (dialog extracted), 2 RPGM data (json). */
class TextItem(val path: String, val size: Long, val uri: Uri, val type: Int, val kind: ScriptKind?, val arc: ArcRef? = null, val entry: String = "") {
    val name: String get() = path.substringAfterLast('/')
    val id: String get() = if (arc == null) uri.toString() else arc.uri.toString() + "#" + entry
}

data class GameIndex(
    val engine: String,
    val title: String?,
    val key: ByteArray?,
    val files: Int,
    val audio: List<GAsset>,
    val images: List<GAsset>,
    val videos: List<GAsset>,
    val textCand: List<GFile>,
    val arcs: List<ArcStatus> = emptyList(),
    val sig: Long = 0L,
)

/** Result of reading one text: [units] (dialog) or [lines] (raw). */
class TextDoc(
    val units: List<TextUnit>?,
    val lines: List<String>?,
    val canRaw: Boolean,
    val truncated: Boolean,
    val error: String?,
)

object GameScan {
    private val AUDIO = setOf("ogg", "mp3", "wav", "m4a", "aac", "flac", "opus", "wma", "mid", "midi", "oga")
    private val IMAGE = setOf("png", "jpg", "jpeg", "gif", "bmp", "webp")
    private val VIDEO = setOf("mp4", "webm", "mkv", "avi", "mov", "wmv", "mpg", "mpeg", "m4v", "ogv", "flv")
    private val ENC_AUDIO = setOf("rpgmvo", "ogg_", "rpgmvm", "m4a_")
    private val ENC_IMAGE = setOf("rpgmvp", "png_")
    private val RAW_TEXT = setOf("txt", "csv", "ini", "log", "md", "xml", "yml", "yaml", "cfg", "conf", "json", "tjs", "lua")
    const val RAW_LIMIT = 4 * 1024 * 1024
    const val DIALOG_LIMIT = 32L * 1024 * 1024

    private fun ext(p: String) = p.substringAfterLast('/').substringAfterLast('.', "").lowercase()
    private fun low(p: String) = p.lowercase().replace('\\', '/')

    /** List of all files (recursive) with their URIs. Throws IOException if the folder cannot be opened. */
    /** Document ID of the intended folder: a folder inside a tree (…/tree/A/document/B) -> B, a plain tree -> A. */
    fun rootDocId(ctx: Context, tree: Uri): String =
        if (DocumentsContract.isDocumentUri(ctx, tree)) DocumentsContract.getDocumentId(tree) else DocumentsContract.getTreeDocumentId(tree)

    fun walk(ctx: Context, tree: Uri): List<GFile> {
        val r = ctx.contentResolver
        val proj = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        )
        val out = ArrayList<GFile>()
        val stack = ArrayDeque<Pair<String, String>>()
        // a "folder inside a tree" URI (result of the main Folder) is scanned starting from its own folder, not from the tree root
        stack.add(Pair(rootDocId(ctx, tree), ""))
        var first = true
        while (stack.isNotEmpty()) {
            val cur = stack.removeFirst()
            val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, cur.first)
            val c = try {
                r.query(childUri, proj, null, null, null)
            } catch (e: SecurityException) {
                throw IOException("Izin folder hilang")
            }
            if (c == null) {
                if (first) throw IOException("Folder tidak dapat dibuka")
                continue
            }
            first = false
            c.use {
                while (it.moveToNext()) {
                    val id = it.getString(0) ?: continue
                    val name = it.getString(1) ?: continue
                    if (it.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) {
                        stack.add(Pair(id, cur.second + name + "/"))
                    } else {
                        out.add(GFile(cur.second + name, if (it.isNull(3)) 0L else it.getLong(3), DocumentsContract.buildDocumentUriUsingTree(tree, id), if (it.isNull(4)) 0L else it.getLong(4)))
                    }
                }
            }
        }
        return out
    }

    fun engineOf(files: List<GFile>): String =
        FolderAnalyzer.analyze(files.map { Pair(it.path, it.size) }, 0).engines.firstOrNull()?.name ?: ""

    /** Media type from the name: 0 audio, 1 image, 2 video, -1 none. */
    fun kindOfName(name: String): Int {
        val e = ext(name)
        return when {
            e in AUDIO -> 0
            e in SpecialMedia.AUD -> 0
            e in IMAGE -> 1
            e in SpecialMedia.IMG -> 1
            e in VIDEO -> 2
            else -> -1
        }
    }

    fun isSpecial(name: String) = ext(name).let { it in SpecialMedia.AUD || it in SpecialMedia.IMG }

    fun isRawText(name: String) = ext(name) in RAW_TEXT
    private val ARC_SCRIPT = setOf("ks", "rpy", "ast", "scn", "cst", "s", "hcb", "ws2", "mes", "csx")
    fun isArcScript(name: String) = ext(name) in ARC_SCRIPT

    /** Folder signature (count, names, sizes, modification times) for detecting changes. */
    fun signature(files: List<GFile>): Long {
        var s = files.size.toLong()
        for (f in files) s += (f.path.hashCode().toLong() * 31L + f.size * 17L + f.mtime) * -7046029254386353131L
        return s
    }

    fun classify(f: GFile): GAsset? {
        val e = ext(f.path)
        return when {
            e in AUDIO -> GAsset(f.path, f.size, f.uri, 0, false)
            e in SpecialMedia.AUD -> GAsset(f.path, f.size, f.uri, 0, false, null, "", true)
            e in SpecialMedia.IMG -> GAsset(f.path, f.size, f.uri, 1, false, null, "", true)
            e in ENC_AUDIO -> GAsset(f.path, f.size, f.uri, 0, true)
            e in IMAGE -> GAsset(f.path, f.size, f.uri, 1, false)
            e in ENC_IMAGE -> GAsset(f.path, f.size, f.uri, 1, true)
            e in VIDEO -> GAsset(f.path, f.size, f.uri, 2, false)
            else -> null
        }
    }

    /** Game icon: icon/icon.png (RPGM) or an image named icon/cover/logo/title in the top folder. */
    fun findIcon(files: List<GFile>): GFile? {
        files.firstOrNull { val p = low(it.path); p.endsWith("icon/icon.png") || p.endsWith("icon/icon.png_") || p.endsWith("icon/icon.rpgmvp") }?.let { return it }
        val names = setOf("icon.png", "icon.jpg", "cover.png", "cover.jpg", "logo.png", "logo.jpg", "title.png", "title.jpg", "thumbnail.png", "thumbnail.jpg")
        return files.firstOrNull { val p = low(it.path); p.count { c -> c == '/' } <= 2 && p.substringAfterLast('/') in names }
    }

    /** RPGM encryption key (System.json, or guessed from an encrypted PNG) + game title. */
    fun sysInfo(ctx: Context, files: List<GFile>): Pair<ByteArray?, String?> {
        var key: ByteArray? = null
        var title: String? = null
        val sys = files.firstOrNull { low(it.path).endsWith("data/system.json") }
        if (sys != null) {
            try {
                val t = ctx.contentResolver.openInputStream(sys.uri)?.use { it.readBytes() }?.toString(Charsets.UTF_8)
                if (t != null) {
                    key = RpgmCrypt.keyFromSystemJson(t)
                    title = ((Json.parse(t) as? Map<*, *>)?.get("gameTitle") as? String)?.takeIf { it.isNotBlank() }
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        if (key == null) {
            val enc = files.firstOrNull { ext(it.path) in ENC_IMAGE }
            if (enc != null) {
                try {
                    val head = ByteArray(32)
                    val n = ctx.contentResolver.openInputStream(enc.uri)?.use { IoUtil.fill(it, head) } ?: 0
                    if (n >= 32) key = RpgmCrypt.detectKey(head)
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
        return Pair(key, title)
    }

    fun buildIndex(ctx: Context, files: List<GFile>): GameIndex {
        val cmp = compareBy<GAsset> { it.path.lowercase() }
        val a = ArrayList<GAsset>()
        val i = ArrayList<GAsset>()
        val v = ArrayList<GAsset>()
        val t = ArrayList<GFile>()
        for (f in files) {
            val s = classify(f)
            if (s != null) {
                when (s.kind) { 0 -> a.add(s); 1 -> i.add(s); else -> v.add(s) }
                continue
            }
            val e = ext(f.path)
            val lp = low(f.path)
            if (e in RAW_TEXT || (e == "json" && lp.contains("data/")) || ScriptText.maybeScript(f.path)) t.add(f)
        }
        val info = if (files.any { low(it.path).endsWith("data/system.json") } || a.any { it.enc } || i.any { it.enc }) sysInfo(ctx, files) else Pair(null, null)
        return GameIndex(engineOf(files), info.second, info.first, files.size, a.sortedWith(cmp), i.sortedWith(cmp), v.sortedWith(cmp), t.sortedBy { it.path.lowercase() }, emptyList(), signature(files))
    }

    private fun readHead(ctx: Context, uri: Uri, n: Int): ByteArray {
        val b = ByteArray(n)
        val got = ctx.contentResolver.openInputStream(uri)?.use { IoUtil.fill(it, b) } ?: 0
        return if (got == n) b else b.copyOf(got)
    }

    /** Builds the Text menu list: text files, scripts (dialog), and RPGM data. */
    suspend fun loadTexts(ctx: Context, idx: GameIndex): List<TextItem> {
        val out = ArrayList<TextItem>()
        for (f in idx.textCand) {
            coroutineContext.ensureActive()
            val lp = low(f.path)
            val e = ext(f.path)
            if (e == "json" && lp.contains("data/") && RpgmText.isTextFile(f.path)) {
                out.add(TextItem(f.path, f.size, f.uri, 2, null))
                continue
            }
            var kind: ScriptKind? = null
            if (ScriptText.maybeScript(f.path) && f.size in 1..(64L * 1024 * 1024)) {
                try {
                    kind = ScriptText.detectKind(f.path, readHead(ctx, f.uri, 65536))
                } catch (ex: Exception) {
                    kind = null
                }
            }
            if (kind != null) out.add(TextItem(f.path, f.size, f.uri, 1, kind))
            else if (e in RAW_TEXT) out.add(TextItem(f.path, f.size, f.uri, 0, null))
        }
        return out
    }

    private fun autoDecode(b: ByteArray): String {
        if (b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte()) return String(b, 2, b.size - 2, Charsets.UTF_16LE)
        if (b.size >= 2 && b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte()) return String(b, 2, b.size - 2, Charsets.UTF_16BE)
        if (b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte()) return String(b, 3, b.size - 3, Charsets.UTF_8)
        return try {
            Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(b)).toString()
        } catch (e: CharacterCodingException) {
            String(b, Charset.forName("Shift_JIS"))
        }
    }

    /** [enc]: "auto", "UTF-8", "Shift_JIS", "GBK", "UTF-16LE". [raw] = true → raw text, false → extracted dialog. */
    fun readText(ctx: Context, item: TextItem, enc: String, raw: Boolean): TextDoc {
        try {
            val arc = item.arc
            if (arc != null && item.size > DIALOG_LIMIT) return TextDoc(null, null, false, false, "big")
            // contents: file in the folder (partially read for raw mode) or the extraction result from an archive
            var bytes: ByteArray? = if (arc != null) GameArc.bytes(ctx, arc, item.entry, DIALOG_LIMIT) else null
            var kind = item.kind
            if (arc != null && item.type == 1 && kind == null) kind = ScriptText.detectKind(item.path, bytes!!.copyOf(minOf(bytes.size, 65536)))
            val type = if (arc != null && item.type == 1 && kind == null) 0 else item.type
            val textual = type == 0 || (type == 1 && kind != null && item.name.substringAfterLast('.', "").lowercase() in setOf("txt", "ks", "rpy", "ast", "s"))
            if (raw || type == 0) {
                val head = bytes ?: readHead(ctx, item.uri, RAW_LIMIT + 1)
                val trunc = head.size > RAW_LIMIT
                val b = if (trunc) head.copyOf(RAW_LIMIT) else head
                val s = if (enc == "auto") autoDecode(b) else String(b, Charset.forName(enc))
                return TextDoc(null, s.split("\n").map { it.trimEnd('\r') }, textual, trunc, null)
            }
            if (item.size > DIALOG_LIMIT) return TextDoc(null, null, textual, false, "big")
            if (bytes == null) bytes = ctx.contentResolver.openInputStream(item.uri)?.use { it.readBytes() } ?: throw IOException("open")
            val units = if (type == 2) {
                RpgmText.extract(RpgmText.fileKey(item.path), RpgmText.parseData(item.path, bytes.toString(Charsets.UTF_8)).root)
            } else {
                ScriptText.extractFile(kind!!, item.path, bytes, "auto")
            }
            return TextDoc(units, null, textual, false, null)
        } catch (e: Exception) {
            return TextDoc(null, null, false, false, e.message ?: e.toString())
        }
    }
}
