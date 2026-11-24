package com.furuarc.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.core.TextTooBigException
import com.furuarc.core.TxtDoc
import com.furuarc.i18n.TextEditStr
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** What the View mode shows for one file: the decoded text or an error, and whether only the first part of a big file is shown. */
internal class ViewLoad(val text: String?, val error: String?, val truncated: Boolean)

/** Bigger than the edit limit: only this many bytes from the start are shown (the same 1 MB as "Lihat isi sebagai teks"). */
private const val VIEW_HEAD_BYTES = 1024 * 1024

/** Copy button: the whole text up to this many characters; longer text copies the page that is shown. */
private const val VIEW_COPY_ALL_MAX = 400_000

/** Drop a multibyte character cut in half at the end, so encoding detection is not confused by the cut. */
private fun trimTail(b: ByteArray): ByteArray {
    var n = b.size
    var cont = 0
    while (n > 0 && cont < 3 && (b[n - 1].toInt() and 0xC0) == 0x80) { n--; cont++ }
    if (n > 0) {
        val lead = b[n - 1].toInt() and 0xFF
        val need = when { lead >= 0xF0 -> 4; lead >= 0xE0 -> 3; lead >= 0xC0 -> 2; else -> 1 }
        if (need > 1 && cont + 1 < need) n-- else n += cont   // cut in half: drop it; complete (or not UTF-8): keep it
    } else {
        n += cont
    }
    return if (n == b.size) b else b.copyOf(n)
}

private fun readHead(ctx: Context, uri: Uri, max: Int): ByteArray {
    val ins = ctx.contentResolver.openInputStream(uri) ?: throw IOException("-")
    ins.use {
        val buf = ByteArray(max)
        var n = 0
        while (n < max) {
            val r = it.read(buf, n, max - n)
            if (r < 0) break
            n += r
        }
        return trimTail(buf.copyOf(n))
    }
}

/** Read a file for the View mode (IO thread). All errors, including out of memory, become messages. */
internal fun loadForView(ctx: Context, te: TextEditStr, item: MediaItem, encPref: String): ViewLoad = try {
    val bytes = readCapped(ctx, item.uri, TxtDoc.EDIT_LIMIT)
    // an encoding chosen by the user is trusted (UTF-16 text has null bytes); "auto" refuses binary files
    if (encPref == "auto" && TxtDoc.looksBinary(bytes)) ViewLoad(null, te.notText, false)
    else ViewLoad(TxtDoc.decode(bytes, encPref).text, null, false)
} catch (e: TextTooBigException) {
    try {
        val head = readHead(ctx, item.uri, VIEW_HEAD_BYTES)
        if (encPref == "auto" && TxtDoc.looksBinary(head)) ViewLoad(null, te.notText, false)
        else ViewLoad(TxtDoc.decode(head, encPref).text, null, true)
    } catch (e2: Throwable) {
        ViewLoad(null, te.openFail(e2.message ?: e2.javaClass.simpleName), false)
    }
} catch (e: OutOfMemoryError) {
    ViewLoad(null, te.tooBig(formatSize(item.size), formatSize(TxtDoc.EDIT_LIMIT)), false)
} catch (e: Throwable) {
    ViewLoad(null, te.openFail(e.message ?: e.javaClass.simpleName), false)
}

/** State of the Find panel of the View mode: the query, and the match that is shown now. */
@Stable
internal class ViewFindState(private val te: TextEditStr) {
    var open by mutableStateOf(false)
    var q by mutableStateOf("")
    var caseS by mutableStateOf(false)
    var useRe by mutableStateOf(false)
    var msg by mutableStateOf<String?>(null)
    var focus by mutableStateOf<TextFocus?>(null)
    private var token = 0
    private var from = 0   // the search goes on from the end of the last match
    private var to = 0     // ... and backwards from its start
    private var has = false

    /** The query changed or the text was reloaded: forget the match (the next search starts at the top). */
    fun reset() { focus = null; msg = null; has = false }

    fun step(text: String, pages: List<String>, sel: PageSel, forward: Boolean) {
        if (q.isEmpty()) { reset(); return }
        val query = TxtDoc.Query(q, caseS, useRe)
        if (TxtDoc.regexError(query) != null) { msg = te.badRegex; return }
        val r = if (forward) TxtDoc.findNext(text, query, if (!has) 0 else if (from > to) from else from + 1)
        else TxtDoc.findPrev(text, query, if (!has) text.length else to)
        if (r == null) { focus = null; has = false; msg = te.noMatch; return }
        has = true; to = r[0]; from = r[1]
        val f = locateInPages(pages, r[0], r[1], ++token)
        sel.page = f.page
        focus = f
        msg = te.matchCount(TxtDoc.count(text, query))
    }
}

@Composable
private fun ViewFindPanel(te: TextEditStr, f: ViewFindState, onStep: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppTextField(value = f.q, onValueChange = { f.q = it; f.reset() }, label = te.findLabel, mono = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { CheckRow(te.caseSensitive, f.caseS) { f.caseS = it; f.reset() } }
            Box(Modifier.weight(1f)) { CheckRow(te.useRegex, f.useRe) { f.useRe = it; f.reset() } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton("‹") { onStep(false) } }
            Box(Modifier.weight(2.2f)) { SecondaryButton(te.nextBtn) { onStep(true) } }
        }
        val m = f.msg
        if (m != null) Text(m, color = p.muted, fontSize = 12.sp)
    }
}

/**
 * "Lihat" mode of the File Text Editor: the content of [item] read-only, in the same look as the "Lihat isi sebagai teks"
 * window of File Inspection (pages, monospace lines), with an Encoding choice and a Find button above it and an
 * Edit | Copy button row below it. [onEdit] moves the file to the Edit mode.
 * The Encoding choice is the same setting as "Encoding saat membuka" of the editor ([EditorPrefs.enc]).
 */
@Composable
internal fun TeViewPane(item: MediaItem, te: TextEditStr, ep: EditorPrefs, onEdit: () -> Unit) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val x = LocalStr.current.x
    var load by remember(item.uri) { mutableStateOf<ViewLoad?>(null) }
    val find = remember(item.uri) { ViewFindState(te) }
    LaunchedEffect(item.uri, ep.enc) {
        load = null
        find.reset()
        load = withContext(Dispatchers.IO) { loadForView(ctx, te, item, ep.enc) }
    }
    val ld = load
    val err = ld?.error
    val text = ld?.text
    AreaTitle(te.viewArea)
    AreaFileName(item.name, false)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            AppDropdown(te.viewEnc, ep.enc, TxtDoc.OPEN_ENCODINGS.map { it to encLabel(te, it) }) { ep.saveEnc(it) }
        }
        Box(Modifier.weight(1f)) { SecondaryButton(te.findBtn) { find.open = !find.open } }
    }
    if (ld == null) {
        Hint(te.opening)
    } else if (err != null) {
        Text(err, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
    } else if (text != null) {
        val pages = remember(text) { paginateText(text) }
        val sel = remember(text) { PageSel() }
        if (find.open) ViewFindPanel(te, find) { forward -> find.step(text, pages, sel, forward) }
        if (ld.truncated) Text(x.viewTextTruncated, color = p.muted, fontSize = 12.sp)
        PagedText(text, Modifier.fillMaxWidth(), listHeight = editorAreaHeight(), showCopy = false, sel = sel, focus = find.focus)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton(te.viewEditBtn) { onEdit() } }
            Box(Modifier.weight(1f)) {
                SecondaryButton(te.viewCopyBtn) {
                    if (text.length <= VIEW_COPY_ALL_MAX) {
                        copyToClipboard(ctx, text, x.copied)
                    } else {
                        val cur = sel.page.coerceIn(0, pages.size - 1)
                        copyToClipboard(ctx, pages[cur], te.viewCopiedPage(cur + 1, pages.size))
                    }
                }
            }
        }
    }
}
