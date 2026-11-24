package com.furuarc.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.*
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.seekExact
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/** Size of one text preview page (characters ≈ bytes for plain text). */
const val TEXT_PAGE_CHARS = 50_000

/** Split text into pages of ±50 KB, cut at line ends so lines are not split. */
fun paginateText(text: String, pageChars: Int = TEXT_PAGE_CHARS): List<String> {
    if (text.length <= pageChars) return listOf(text)
    val out = ArrayList<String>()
    var start = 0
    while (start < text.length) {
        var end = minOf(text.length, start + pageChars)
        if (end < text.length) {
            val nl = text.lastIndexOf('\n', end - 1)
            if (nl > start + pageChars / 2) end = nl + 1
        }
        out.add(text.substring(start, end))
        start = end
    }
    return out
}

/** Current page of a [PagedText] kept by the caller (so a button outside the text can copy the page that is shown). */
class PageSel {
    var page by mutableStateOf(0)
}

/**
 * A match to show in a [PagedText]: the page, the line inside that page and the column range to highlight.
 * [token] changes for every new match, so the list scrolls to it again even when it is the same place.
 */
class TextFocus(val page: Int, val line: Int, val colStart: Int, val colEnd: Int, val token: Int)

/** Where the range [start, end) of the whole text falls in the [pages] made by [paginateText] (lines are split like [PagedText] does). */
fun locateInPages(pages: List<String>, start: Int, end: Int, token: Int): TextFocus {
    var base = 0
    var page = 0
    while (page < pages.size - 1 && start >= base + pages[page].length) { base += pages[page].length; page++ }
    val s = pages[page]
    val off = (start - base).coerceIn(0, s.length)
    var line = 0
    var lineStart = 0
    var k = 0
    while (k < off) {
        val c = s[k]
        if (c == '\n' || (c == '\r' && !(k + 1 < s.length && s[k + 1] == '\n'))) { line++; lineStart = k + 1 }
        k++
    }
    val col = off - lineStart
    return TextFocus(page, line, col, col + (end - start).coerceAtLeast(0), token)
}

/**
 * Text with pages (50 KB per page) and previous/next/copy buttons. Each page is shown as
 * a list of lazy lines (LazyColumn) to stay light; no SelectionContainer (unstable with lazy lists).
 * [showCopy] = false hides the copy button (the caller has its own); [sel] = the caller keeps the current page;
 * [focus] = a match to scroll to and highlight (the caller also sets [sel] to its page).
 */
@Composable
fun PagedText(
    text: String, modifier: Modifier = Modifier, listHeight: androidx.compose.ui.unit.Dp? = null,
    showCopy: Boolean = true, sel: PageSel? = null, focus: TextFocus? = null,
) {
    val p = LocalPalette.current
    val x = LocalStr.current.x
    val pages = remember(text) { paginateText(text) }
    var localPage by remember(text) { mutableStateOf(0) }
    val cur = (sel?.page ?: localPage).coerceIn(0, pages.size - 1)
    val lines = remember(pages, cur) { pages[cur].replace("\r\n", "\n").replace('\r', '\n').split('\n') }
    val ctx = LocalContext.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    // a new page starts at the top; a match scrolls to its line (two lines above it stay visible)
    LaunchedEffect(cur, focus?.token) {
        val f = focus
        if (f != null && f.page == cur) listState.scrollToItem((f.line - 2).coerceAtLeast(0)) else listState.scrollToItem(0)
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (pages.size > 1) {
            PageNav(cur, pages.size, { if (sel != null) sel.page = it else localPage = it })
        }
        val listMod = if (listHeight != null) Modifier.fillMaxWidth().height(listHeight) else Modifier.fillMaxWidth().weight(1f, fill = true)
        LazyColumn(listMod, state = listState) {
            items(lines.size) { i ->
                val f = focus
                if (f != null && f.page == cur && f.line == i) {
                    val line = lines[i].ifEmpty { " " }
                    val a = f.colStart.coerceIn(0, line.length)
                    val b = f.colEnd.coerceIn(a, line.length)
                    val shown = androidx.compose.ui.text.buildAnnotatedString {
                        append(line)
                        if (b > a) addStyle(androidx.compose.ui.text.SpanStyle(background = p.accent.copy(alpha = 0.35f)), a, b)
                    }
                    androidx.compose.material.Text(
                        shown, color = p.text, fontSize = 12.sp, lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                } else {
                    Text(
                        lines[i].ifEmpty { " " }, color = p.text, fontSize = 12.sp, lineHeight = 17.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
        if (showCopy) SecondaryButton(x.copyPage) { copyToClipboard(ctx, pages[cur], x.copied) }
    }
}

val AUDIO_EXT = setOf("ogg", "mp3", "wav", "m4a", "aac", "flac", "oga", "opus", "mid", "midi")

/** Simple audio player for in-memory preview: write to cache, play with MediaPlayer, clean up when closed. */
@Composable
fun AudioPreview(bytes: ByteArray, ext: String, name: String) {
    val p = LocalPalette.current
    val x = LocalStr.current.x
    val ctx = LocalContext.current
    var error by remember(bytes) { mutableStateOf<String?>(null) }
    var ready by remember(bytes) { mutableStateOf(false) }
    var playing by remember(bytes) { mutableStateOf(false) }
    var pos by remember(bytes) { mutableStateOf(0) }
    var dur by remember(bytes) { mutableStateOf(0) }
    val holder = remember(bytes) { arrayOfNulls<MediaPlayer>(1) }
    val seekSt = remember(bytes) { SeekState() }

    DisposableEffect(bytes) {
        onDispose {
            try { holder[0]?.release() } catch (e: Throwable) { }
            holder[0] = null
        }
    }
    LaunchedEffect(bytes) {
        withContext(Dispatchers.IO) {
            var f: File? = null
            try {
                f = File(ctx.cacheDir, "preview-${System.nanoTime()}.$ext")
                f.writeBytes(bytes)
                val mp = MediaPlayer()
                mp.setDataSource(f.absolutePath)
                mp.prepare()
                mp.setOnCompletionListener { playing = false; pos = 0; try { it.seekTo(0) } catch (e: Throwable) { } }
                holder[0] = mp
                dur = mp.duration
                ready = true
            } catch (e: Throwable) {
                error = e.message ?: "?"
            } finally {
                // MediaPlayer has loaded the file; the cache copy is no longer needed
                try { f?.delete() } catch (e: Throwable) { }
            }
        }
    }
    LaunchedEffect(playing) {
        while (playing) {
            val mp = holder[0]
            if (mp != null) try { pos = mp.currentPosition } catch (e: Throwable) { }
            delay(250)
        }
    }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val e = error
        if (e != null) {
            Text(x.audioFail + ": " + e, color = p.muted)
        } else if (!ready) {
            Text(x.audioLoading, color = p.muted)
        } else {
            Text(fmtTime(seekSt.shown(pos)) + " / " + fmtTime(dur), color = p.text, fontSize = 14.sp)
            SeekSlider(seekSt, pos, dur, { ms -> pos = ms; holder[0]?.seekExact(ms) })
            PrimaryButton(if (playing) x.audioPause else x.audioPlay, true) {
                val mp = holder[0]
                if (mp != null) try {
                    if (playing) { mp.pause(); playing = false } else { mp.start(); playing = true }
                } catch (ex: Throwable) { error = ex.message ?: "?" }
            }
        }
    }
}

private fun fmtTime(ms: Int): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
