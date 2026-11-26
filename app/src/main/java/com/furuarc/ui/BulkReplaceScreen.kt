package com.furuarc.ui

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.core.TxtDoc
import com.furuarc.i18n.TextEditStr
import com.furuarc.i18n.mediaStr
import com.furuarc.i18n.textEditStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class BulkRow(val item: MediaItem, val count: Int)

private fun countIn(ctx: Context, item: MediaItem, q: TxtDoc.Query, enc: String): Int = try {
    val b = readCapped(ctx, item.uri, TxtDoc.EDIT_LIMIT)
    if (TxtDoc.looksBinary(b)) -1 else TxtDoc.count(TxtDoc.decode(b, enc).text, q)
} catch (e: Exception) {
    -1
}

/** Replace in one file: (success, message, replacement count). The original encoding and line ending are preserved. */
private fun applyOne(
    ctx: Context, te: TextEditStr, ep: EditorPrefs, item: MediaItem, tree: Uri?,
    q: TxtDoc.Query, rep: String, needOut: String,
): Triple<Boolean, String, Int> {
    return try {
        val b = readCapped(ctx, item.uri, TxtDoc.EDIT_LIMIT)
        val ld = TxtDoc.decode(b, ep.enc)
        val r = TxtDoc.replaceAll(ld.text, q, rep) ?: return Triple(false, te.badRegex, 0)
        if (r.second == 0) return Triple(true, "", 0)
        val enc = TxtDoc.resolveSave("same", ld)
        val bad = TxtDoc.badChars(r.first, enc.first)
        if (bad > 0) return Triple(false, te.unencodable(bad, enc.first), 0)
        val bytes = TxtDoc.encode(r.first, enc.first, enc.second, ld.crlf)
        val res = teSave(ctx, te, ep, item, tree, bytes, item.name, SaveMemo(), needOut)
        Triple(res.ok, res.msg, r.second)
    } catch (e: Exception) {
        Triple(false, e.message ?: e.javaClass.simpleName, 0)
    }
}

/** Bulk Replace: find and replace text in many files in one folder, with the same Output & Results. */
@Composable
fun BulkReplaceScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val lang = LocalLang.current
    val te = remember(lang) { textEditStr(lang) }
    val m = remember(lang) { mediaStr(lang) }
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    val ep = rememberEditorPrefs()
    val fold = remember { FoldState() }
    val pickOut = rememberOutPicker { u, n -> ep.out = u; ep.outName = n }
    val src = rememberTeSource("txt, json, ini, cfg, ks, rpy, csv, xml, html, js, md, yaml, yml, lua, py") { _ -> }
    LaunchedEffect(Unit) { src.st.mode = 1 }
    var findQ by remember { mutableStateOf("") }
    var repQ by remember { mutableStateOf("") }
    var caseS by remember { mutableStateOf(false) }
    var useRe by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<BulkRow>?>(null) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var confirm by remember { mutableStateOf(false) }

    fun scan() {
        if (busy) return
        val q = TxtDoc.Query(findQ, caseS, useRe)
        if (src.st.folderUri == null) { note = Pair(te.bulkNoFolder, false); return }
        if (findQ.isEmpty()) { note = Pair(te.bulkNoQuery, false); return }
        if (TxtDoc.regexError(q) != null) { note = Pair(te.badRegex, false); return }
        val files = teFiltered(src.st)
        busy = true; note = null; rows = null
        scope.launch {
            val found = ArrayList<BulkRow>()
            var skip = 0
            for (i in files.indices) {
                progress = te.bulkProgress(i + 1, files.size)
                val fi = files[i]
                val c = withContext(Dispatchers.IO) { countIn(ctx, fi, q, ep.enc) }
                if (c < 0) skip++ else if (c > 0) found.add(BulkRow(fi, c))
            }
            var total = 0
            for (r in found) total += r.count
            rows = found
            progress = ""
            busy = false
            note = Pair(te.bulkResult(found.size, files.size, total) + (if (skip > 0) " " + te.bulkSkipped(skip) else ""), true)
        }
    }

    fun apply() {
        val list = rows ?: return
        if (busy) return
        val tree = src.st.folderUri
        val q = TxtDoc.Query(findQ, caseS, useRe)
        val rep = repQ
        busy = true; note = null
        scope.launch {
            var ok = 0
            var fail = 0
            var reps = 0
            val fails = ArrayList<String>()
            for (i in list.indices) {
                progress = te.bulkProgress(i + 1, list.size)
                val fi = list[i].item
                val r = withContext(Dispatchers.IO) { applyOne(ctx, te, ep, fi, tree, q, rep, m.needOut) }
                if (r.first) { ok++; reps += r.third } else { fail++; fails.add(fi.name + ": " + r.second) }
            }
            progress = ""
            busy = false
            rows = null
            val head = te.bulkApplied(ok, fail, reps)
            note = Pair(if (fails.isEmpty()) head else head + "\n" + fails.take(5).joinToString("\n"), fail == 0)
        }
    }

    val rs = rows
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(title = te.bulkTitle, onBack = onBack)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            teSourceItems(src, te, busy, false) { _, _ -> }
            item { AppTextField(value = findQ, onValueChange = { findQ = it; rows = null }, label = te.findLabel, mono = true) }
            item { AppTextField(value = repQ, onValueChange = { repQ = it }, label = te.replaceLabel, mono = true) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.foundation.layout.Box(Modifier.weight(1f)) { CheckRow(te.caseSensitive, caseS) { caseS = it; rows = null } }
                    androidx.compose.foundation.layout.Box(Modifier.weight(1f)) { CheckRow(te.useRegex, useRe) { useRe = it; rows = null } }
                }
            }
            item { FoldSection(fold, "out", m.secOut) { OutputBlock(ep, te, true, pickOut) } }
            item { SecondaryButton(te.bulkPreview) { scan() } }
            if (progress.isNotEmpty()) item { Hint(progress) }
            val nt = note
            if (nt != null) item { NoteCard(nt, te.close) { note = null } }
            if (rs != null) {
                item {
                    PagedPanel(null, rs, 50) { r ->
                        val rel = r.item.rel.substringAfter('/', "")
                        Column(Modifier.fillMaxWidth()) {
                            Text((if (rel.isEmpty()) "" else "$rel/") + r.item.name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(te.matchCount(r.count), color = p.muted, fontSize = 12.sp)
                        }
                    }
                }
                item { PrimaryButton(te.bulkApply, rs.isNotEmpty() && !busy) { confirm = true } }
            }
        }
    }

    if (confirm) {
        ConfirmDialog(
            te.bulkConfirmTitle, te.bulkConfirmMsg(rs?.size ?: 0), te.bulkApply, te.cancel,
            { confirm = false; apply() },
            { confirm = false },
        )
    }
}
