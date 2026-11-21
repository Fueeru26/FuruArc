package com.furuarc.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
    fun get(key: String): List<Cue> = map[key] ?: emptyList()
    fun put(key: String, cues: List<Cue>) { if (cues.isEmpty()) map.remove(key) else map[key] = cues }
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

    fun parse(raw: String): List<Cue> {
        val text = raw.removePrefix("﻿").replace("\r\n", "\n").replace('\r', '\n')
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

/** Window to upload (or remove) the subtitle file of the current video. */
@Composable
internal fun SubtitleDialog(key: String, current: Int, onChange: (List<Cue>) -> Unit, onDismiss: () -> Unit) {
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
                onDismiss()
            }
        }
    }
    AppDialog(pl.subtitle, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(pl.subHint, color = p.muted, fontSize = 13.sp)
            SecondaryButton(pl.subPick, true) { pick.launch(arrayOf("application/x-subrip", "text/vtt", "text/*", "application/octet-stream", "*/*")) }
            if (current > 0) {
                Text(pl.subLoaded(current), color = p.text, fontSize = 13.sp)
                SecondaryButton(pl.subRemove, true) { SubStore.put(key, emptyList()); onChange(emptyList()); onDismiss() }
            }
            msg?.let { Text(it, color = androidx.compose.ui.graphics.Color(0xFFE57373), fontSize = 13.sp) }
        }
    }
}
