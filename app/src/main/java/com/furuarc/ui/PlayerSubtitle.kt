package com.furuarc.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.Icon
import androidx.compose.material.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** One subtitle line: [start] / [end] in milliseconds. */
class Cue(val start: Int, val end: Int, val text: String)

/** Subtitles chosen by the user, kept per video while the app is open (not saved; they are tied to the file chosen). */
object SubStore {
    private val map = HashMap<String, List<Cue>>()
    private val offsets = HashMap<String, Int>()
    /** Keys where the automatic search already ran (so it is not repeated all the time). */
    private val tried = HashSet<String>()
    fun get(key: String): List<Cue> = map[key] ?: emptyList()
    fun put(key: String, cues: List<Cue>) { if (cues.isEmpty()) map.remove(key) else map[key] = cues }
    fun offset(key: String): Int = offsets[key] ?: 0
    fun setOffset(key: String, ms: Int) { if (ms == 0) offsets.remove(key) else offsets[key] = ms }
    fun markTried(key: String): Boolean = tried.add(key)
}

/** Subtitle file with the same name next to the media (SRT / VTT / ASS / SSA, or LRC for lyrics). */
object SubFinder {
    fun find(ctx: android.content.Context, item: com.furuarc.core.MItem, lyrics: Boolean): List<Cue> {
        val exts = if (lyrics) listOf("lrc", "LRC") else listOf("srt", "vtt", "ass", "ssa", "SRT", "VTT", "ASS", "SSA")
        try {
            val path = item.path
            if (path != null) {
                val f = java.io.File(path)
                val base = f.nameWithoutExtension
                for (e in exts) {
                    val c = java.io.File(f.parentFile, "$base.$e")
                    if (c.isFile) { val r = SubParser.parse(String(c.readBytes(), Charsets.UTF_8)); if (r.isNotEmpty()) return r }
                }
                return emptyList()
            }
            val uri = item.uri ?: return emptyList()
            val docId = android.provider.DocumentsContract.getDocumentId(uri)
            val dot = docId.lastIndexOf('.')
            val slash = docId.lastIndexOf('/')
            val baseId = if (dot > slash) docId.substring(0, dot) else docId
            val trees = com.furuarc.core.MediaLib.folders.values.flatten().map { android.net.Uri.parse(it.uri) }
            for (e in exts) {
                val sid = "$baseId.$e"
                val cands = ArrayList<android.net.Uri>()
                for (t in trees) {
                    val tid = try { android.provider.DocumentsContract.getTreeDocumentId(t) } catch (x: Throwable) { continue }
                    if (t.authority == uri.authority && sid.startsWith(tid)) cands.add(android.provider.DocumentsContract.buildDocumentUriUsingTree(t, sid))
                }
                cands.add(android.provider.DocumentsContract.buildDocumentUri(uri.authority, sid))
                for (c in cands) {
                    val bytes = try { ctx.contentResolver.openInputStream(c)?.use { it.readBytes() } } catch (x: Throwable) { null } ?: continue
                    val r = SubParser.parse(String(bytes, Charsets.UTF_8))
                    if (r.isNotEmpty()) return r
                }
            }
        } catch (e: Throwable) { }
        return emptyList()
    }
}

/** Bookmarks (positions in ms) per media file, kept on the device. */
object BookmarkStore {
    private fun sp() = com.furuarc.Engine.app.getSharedPreferences("media_bookmarks", android.content.Context.MODE_PRIVATE)
    fun get(key: String): List<Int> = (sp().getString(key, "") ?: "").split(',').mapNotNull { it.toIntOrNull() }.sorted()
    private fun put(key: String, l: List<Int>) { sp().edit().putString(key, l.joinToString(",")).apply() }
    fun add(key: String, ms: Int) { put(key, (get(key) + ms).distinct().sorted()) }
    fun remove(key: String, ms: Int) { put(key, get(key) - ms) }
}

/** Parses SubRip (.srt), WebVTT (.vtt) and the dialogue lines of SubStation Alpha (.ass / .ssa). */
object SubParser {
    private val time = Regex("(?:(\\d+):)?(\\d{1,2}):(\\d{2})[,.](\\d{1,3})")
    private val tags = Regex("<[^>]*>|\\{[^}]*\\}")

    private fun ms(m: MatchResult): Int {
        val h = m.groupValues[1].toIntOrNull() ?: 0
        val frac = m.groupValues[4].padEnd(3, '0').take(3).toInt()
        return ((h * 60 + m.groupValues[2].toInt()) * 60 + m.groupValues[3].toInt()) * 1000 + frac
    }

    private val lrcTag = Regex("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?]")
    private val lrcWord = Regex("<\\d+:\\d+[.:]?\\d*>")

    private fun parseLrc(text: String): List<Cue> {
        val starts = ArrayList<Pair<Int, String>>()
        for (line in text.lines()) {
            val tags = lrcTag.findAll(line).toList()
            if (tags.isEmpty()) continue
            val t = line.substring(tags.last().range.last + 1).replace(lrcWord, "").trim()
            for (m in tags) {
                val frac = m.groupValues[3].padEnd(3, '0').take(3).toIntOrNull() ?: 0
                starts.add(((m.groupValues[1].toInt() * 60 + m.groupValues[2].toInt()) * 1000 + frac) to t)
            }
        }
        starts.sortBy { it.first }
        val out = ArrayList<Cue>()
        for (i in starts.indices) {
            val end = if (i + 1 < starts.size) starts[i + 1].first else starts[i].first + 6000
            out.add(Cue(starts[i].first, end, starts[i].second))
        }
        return out
    }

    fun parse(raw: String): List<Cue> {
        val text = raw.removePrefix("\uFEFF").replace("\r\n", "\n").replace('\r', '\n')
        if (!text.contains("-->") && !text.contains("[Events]") && lrcTag.containsMatchIn(text)) return parseLrc(text)
        val out = ArrayList<Cue>()
        if (text.contains("[Events]")) {
            for (line in text.lines()) {
                if (!line.startsWith("Dialogue:")) continue
                val f = line.substringAfter(':').split(',', limit = 10)
                if (f.size < 10) continue
                val a = assTime(f[1].trim()) ?: continue
                val b = assTime(f[2].trim()) ?: continue
                val t = f[9].replace("\\N", "\n").replace("\\n", "\n").replace(tags, "").trim()
                if (t.isNotEmpty()) out.add(Cue(a, b, t))
            }
        } else {
            for (block in text.split(Regex("\n\\s*\n"))) {
                val lines = block.lines()
                val at = lines.indexOfFirst { it.contains("-->") }
                if (at < 0) continue
                val ts = time.findAll(lines[at]).toList()
                if (ts.size < 2) continue
                val t = lines.drop(at + 1).joinToString("\n") { it.replace(tags, "") }.trim()
                if (t.isNotEmpty()) out.add(Cue(ms(ts[0]), ms(ts[1]), t))
            }
        }
        out.sortBy { it.start }
        return out
    }

    private fun assTime(s: String): Int? {
        val m = Regex("(\\d+):(\\d{2}):(\\d{2})[.](\\d{1,2})").matchEntire(s) ?: return null
        return ((m.groupValues[1].toInt() * 60 + m.groupValues[2].toInt()) * 60 + m.groupValues[3].toInt()) * 1000 + m.groupValues[4].padEnd(2, '0').toInt() * 10
    }
}

/** Window to upload (or remove) the subtitle / lyrics file of the current media, with timing and size adjustment. */
@Composable
internal fun SubtitleDialog(
    key: String, lyrics: Boolean, count: Int, offset: Int, onChange: (List<Cue>) -> Unit, onOffset: (Int) -> Unit, onDismiss: () -> Unit,
) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var msg by remember { mutableStateOf<String?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) scope.launch {
            val cues = withContext(Dispatchers.IO) {
                try {
                    val bytes = ctx.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: ByteArray(0)
                    SubParser.parse(String(bytes, Charsets.UTF_8))
                } catch (e: Throwable) { emptyList() }
            }
            if (cues.isEmpty()) msg = pl.subFail else {
                SubStore.put(key, cues)
                onChange(cues)
                msg = null
            }
        }
    }
    AppDialog(if (lyrics) pl.lyrics else pl.subtitle, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (lyrics) pl.lyricsHint else pl.subHint, color = p.muted, fontSize = 13.sp)
            SecondaryButton(if (lyrics) pl.lyricsPick else pl.subPick, true) { pick.launch(arrayOf("*/*")) }
            if (count > 0) {
                Text(pl.subLoaded(count), color = p.text, fontSize = 13.sp)
                Text(pl.subOffset(offset), color = p.muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { SecondaryButton("−0.5s", true) { onOffset(offset - 500) } }
                    Box(Modifier.weight(1f)) { SecondaryButton("0", true) { onOffset(0) } }
                    Box(Modifier.weight(1f)) { SecondaryButton("+0.5s", true) { onOffset(offset + 500) } }
                }
                Text(pl.subSizeLabel(com.furuarc.core.MediaPrefs.subSize), color = p.muted, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) { SecondaryButton("A−", true) { com.furuarc.core.MediaPrefs.subSize = (com.furuarc.core.MediaPrefs.subSize - 10).coerceAtLeast(60); com.furuarc.core.MediaPrefs.save() } }
                    Box(Modifier.weight(1f)) { SecondaryButton("A+", true) { com.furuarc.core.MediaPrefs.subSize = (com.furuarc.core.MediaPrefs.subSize + 10).coerceAtMost(200); com.furuarc.core.MediaPrefs.save() } }
                }
                SecondaryButton(if (lyrics) pl.lyricsRemove else pl.subRemove, true) { SubStore.put(key, emptyList()); SubStore.setOffset(key, 0); onChange(emptyList()); onOffset(0); onDismiss() }
            }
            msg?.let { Text(it, color = androidx.compose.ui.graphics.Color(0xFFE57373), fontSize = 13.sp) }
        }
    }
}

/** Bookmarks of the current media: add the current position, tap one to jump, ✕ to delete. */
@Composable
internal fun BookmarkDialog(key: String, pos: Int, onSeek: (Int) -> Unit, onDismiss: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    var list by remember(key) { mutableStateOf(BookmarkStore.get(key)) }
    AppDialog(pl.bookmarks, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            SecondaryButton(pl.addBookmark(fmtClock(pos)), true) { BookmarkStore.add(key, pos); list = BookmarkStore.get(key) }
            if (list.isEmpty()) Text(pl.noBookmarks, color = p.muted, fontSize = 13.sp)
            for (ms in list) {
                Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { onSeek(ms); onDismiss() }, verticalAlignment = Alignment.CenterVertically) {
                    Icon(AppIcons.Bookmark, null, tint = p.accent, modifier = Modifier.size(20.dp))
                    Text(fmtClock(ms), color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 10.dp, top = 10.dp, bottom = 10.dp))
                    TextButton(onClick = { BookmarkStore.remove(key, ms); list = BookmarkStore.get(key) }) { Text("✕", color = p.muted) }
                }
            }
        }
    }
}
