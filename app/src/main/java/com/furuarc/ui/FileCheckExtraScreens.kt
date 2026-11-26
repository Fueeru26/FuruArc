package com.furuarc.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.FileOps
import com.furuarc.core.HexTools
import com.furuarc.i18n.fileToolsStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.security.MessageDigest

// ======================= Duplicate File Finder =======================

private class DupFile(val uri: Uri, val path: String, val size: Long)
private class DupGroup(val size: Long, val files: List<DupFile>)

/** All files in [tree] and its subfolders, with relative paths. */
private fun walkFiles(ctx: Context, tree: Uri): List<DupFile> {
    val out = ArrayList<DupFile>()
    val proj = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
    )
    fun walk(docId: String, rel: String) {
        val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, docId)
        val subs = ArrayList<Pair<String, String>>()
        ctx.contentResolver.query(childUri, proj, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0) ?: continue
                val name = c.getString(1) ?: continue
                val isDir = c.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR
                val path = if (rel.isEmpty()) name else "$rel/$name"
                if (isDir) {
                    subs.add(Pair(id, path))
                } else {
                    val size = if (c.isNull(3)) 0L else c.getLong(3)
                    out.add(DupFile(DocumentsContract.buildDocumentUriUsingTree(tree, id), path, size))
                }
            }
        }
        for ((id, path) in subs) walk(id, path)
    }
    walk(DocumentsContract.getTreeDocumentId(tree), "")
    return out
}

/** Duplicate File Finder: group files of the same size, compare SHA-256, then choose which to delete (one per group always remains). */
@Composable
fun DupFinderScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val f = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var dirName by remember { mutableStateOf<String?>(null) }
    var phase by remember { mutableStateOf(0) } // 0 idle, 1 reading folder, 2 checking contents, 3 done
    var doneFiles by remember { mutableStateOf(0) }
    var totalFiles by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0f) }
    var groups by remember { mutableStateOf<List<DupGroup>>(emptyList()) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var busy by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }

    fun keepOne(gs: List<DupGroup>): Set<String> {
        val s = HashSet<String>()
        for (g in gs) for (i in 1 until g.files.size) s.add(g.files[i].uri.toString())
        return s
    }

    fun scan(tree: Uri) {
        job?.cancel()
        groups = emptyList(); selected = emptySet(); note = null
        phase = 1; progress = 0f; doneFiles = 0; totalFiles = 0
        job = scope.launch {
            val res = withContext(Dispatchers.IO) {
                try {
                    val all = walkFiles(ctx, tree)
                    val cand = all.filter { it.size > 0L }.groupBy { it.size }.filter { it.value.size > 1 }.values.toList()
                    var total = 0
                    var bytes = 0L
                    for (g in cand) {
                        total += g.size
                        for (x in g) bytes += x.size
                    }
                    totalFiles = total
                    phase = 2
                    var doneB = 0L
                    var doneN = 0
                    val buf = ByteArray(256 * 1024)
                    val found = ArrayList<DupGroup>()
                    for (g in cand) {
                        val byHash = LinkedHashMap<String, MutableList<DupFile>>()
                        for (x in g) {
                            ensureActive()
                            val ins = try { ctx.contentResolver.openInputStream(x.uri) } catch (e: Exception) { null }
                            if (ins == null) {
                                doneN++
                                doneFiles = doneN
                                continue
                            }
                            val md = MessageDigest.getInstance("SHA-256")
                            var ok = true
                            try {
                                ins.use { input ->
                                    while (true) {
                                        val k = input.read(buf)
                                        if (k < 0) break
                                        md.update(buf, 0, k)
                                        doneB += k
                                        if (bytes > 0L) progress = (doneB.toFloat() / bytes).coerceAtMost(1f)
                                    }
                                }
                            } catch (e: IOException) {
                                ok = false
                            }
                            if (ok) byHash.getOrPut(FileOps.hex(md.digest())) { ArrayList() }.add(x)
                            doneN++
                            doneFiles = doneN
                        }
                        for (l in byHash.values) {
                            if (l.size > 1) found.add(DupGroup(l[0].size, l.sortedBy { it.path.lowercase() }))
                        }
                    }
                    found.sortedByDescending { it.size * (it.files.size - 1) }
                } catch (e: Throwable) {
                    if (e is CancellationException) throw e
                    emptyList<DupGroup>()
                }
            }
            groups = res
            selected = keepOne(res)
            phase = 3
        }
    }

    val pickDir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            dirName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
            scan(u)
        }
    }

    fun toggle(g: DupGroup, x: DupFile) {
        val key = x.uri.toString()
        if (key in selected) {
            selected = selected - key
        } else {
            val remaining = g.files.count { it !== x && it.uri.toString() !in selected }
            if (remaining >= 1) selected = selected + key
        }
    }

    var selCount = 0
    var selBytes = 0L
    var extraFiles = 0
    var wasted = 0L
    for (g in groups) {
        extraFiles += g.files.size - 1
        wasted += g.size * (g.files.size - 1)
        for (x in g.files) if (x.uri.toString() in selected) { selCount++; selBytes += x.size }
    }

    fun doDelete() {
        val targets = ArrayList<DupFile>()
        for (g in groups) for (x in g.files) if (x.uri.toString() in selected) targets.add(x)
        if (targets.isEmpty()) return
        busy = true; note = null
        scope.launch {
            var ok = 0
            var fail = 0
            val deleted = HashSet<String>()
            withContext(Dispatchers.IO) {
                for (x in targets) {
                    val r = try { DocumentsContract.deleteDocument(ctx.contentResolver, x.uri) } catch (e: Exception) { false }
                    if (r) { ok++; deleted.add(x.uri.toString()) } else fail++
                }
            }
            groups = groups.map { g -> DupGroup(g.size, g.files.filter { it.uri.toString() !in deleted }) }.filter { it.files.size > 1 }
            selected = selected - deleted
            busy = false
            note = f.dupDone(ok, fail) to (fail == 0)
        }
    }

    if (confirm) {
        ConfirmDialog(
            title = f.dupConfirmTitle(selCount),
            message = f.dupConfirmMsg,
            yes = f.dupYes,
            no = f.cancel,
            onConfirm = { confirm = false; doDelete() },
            onDismiss = { confirm = false },
        )
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(f.dupTitle, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, f.dupPicker, dirName, AppIcons.Folder) { if (!busy) pickDir.launch(null) } }
        item { DetailHint(f.dupHint) }

        if (phase == 1 || phase == 2) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (phase == 1) f.dupListing else f.dupHashing(doneFiles, totalFiles), color = p.muted, fontSize = 13.sp)
                    if (phase == 1) LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
                    else LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
                    SecondaryButton(f.cancel) { job?.cancel(); phase = 0 }
                }
            }
        }
        if (phase == 3) {
            if (groups.isEmpty()) {
                item { Hint(f.dupNone) }
            } else {
                item { Hint(f.dupSummary(groups.size, extraFiles, fmtBytes(wasted))) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) { SecondaryButton(f.dupKeepOne, !busy) { selected = keepOne(groups) } }
                        Box(Modifier.weight(1f)) { SecondaryButton(f.dupClear, !busy) { selected = emptySet() } }
                    }
                }
                item { PagedPanel(f.dupTitle, groups, 50) { g ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                            Text(f.dupGroup(g.files.size, fmtBytes(g.size)), color = p.muted, fontSize = 12.sp)
                            for (x in g.files) {
                                Row(
                                    Modifier.fillMaxWidth().clickable(enabled = !busy) { toggle(g, x) }.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        checked = x.uri.toString() in selected,
                                        onCheckedChange = { if (!busy) toggle(g, x) },
                                        colors = CheckboxDefaults.colors(checkedColor = p.accent),
                                    )
                                    Text(x.path, color = p.text, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } }
                item { Hint(f.dupSelected(selCount, fmtBytes(selBytes))) }
                item { PrimaryButton(f.dupDelete(selCount), selCount > 0 && !busy) { confirm = true } }
            }
        }
        if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent) }
        note?.let { n -> item { NoteCard(n, f.close) { note = null } } }
    }
}
}

// ======================= Hex Viewer =======================

/** Four symbol pagination buttons (« ‹ › »): first, back, forward, last. Used above and below the hex dump. */
@Composable
private fun HexNavBar(labels: List<String>, canBack: Boolean, canNext: Boolean, onFirst: () -> Unit, onBack: () -> Unit, onNext: () -> Unit, onLast: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.weight(1f)) { SecondaryButton(labels[0], canBack) { onFirst() } }
        Box(Modifier.weight(1f)) { SecondaryButton(labels[1], canBack) { onBack() } }
        Box(Modifier.weight(1f)) { SecondaryButton(labels[2], canNext) { onNext() } }
        Box(Modifier.weight(1f)) { SecondaryButton(labels[3], canNext) { onLast() } }
    }
}

/** Hex Viewer: hex dump per page (128 rows), jump to offset, search text or bytes. */
@Composable
fun HexViewerScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val f = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var src by remember { mutableStateOf<Uri?>(null) }
    var srcName by remember { mutableStateOf<String?>(null) }
    var srcSize by remember { mutableStateOf(0L) }
    var offset by remember { mutableStateOf(0L) }
    var bprIdx by remember { mutableStateOf(0) } // 0 = 8 bytes/row, 1 = 16
    var pageText by remember { mutableStateOf("") }
    var jump by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var sMode by remember { mutableStateOf(0) }
    var lastHit by remember { mutableStateOf(-1L) }
    var searching by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var job by remember { mutableStateOf<Job?>(null) }

    val bpr = if (bprIdx == 0) 8 else 16
    val pageBytes = bpr * 128

    val pickSrc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            val (n, sz) = queryNameSize(ctx, u)
            src = u; srcName = n; srcSize = sz
            offset = 0L; lastHit = -1L; note = null; pageText = ""
        }
    }

    LaunchedEffect(Unit) {
        PendingFile.take("filecheck.hex")?.let { u ->
            val (n, sz) = queryNameSize(ctx, u)
            src = u; srcName = n; srcSize = sz
            offset = 0L; lastHit = -1L; note = null; pageText = ""
        }
    }

    LaunchedEffect(src, offset, bpr) {
        val u = src
        if (u == null) {
            pageText = ""
            return@LaunchedEffect
        }
        val size = srcSize
        val off = offset
        pageText = withContext(Dispatchers.IO) {
            try {
                ctx.contentResolver.openFileDescriptor(u, "r")?.use { fd ->
                    FileInputStream(fd.fileDescriptor).use { fin ->
                        val ch = fin.channel
                        val n = minOf(pageBytes.toLong(), size - off).toInt()
                        if (n <= 0) {
                            ""
                        } else {
                            val buf = ByteBuffer.allocate(n)
                            var got = 0
                            while (got < n) {
                                val k = ch.read(buf, off + got)
                                if (k <= 0) break
                                got += k
                            }
                            HexTools.page(buf.array().copyOf(got), off, bpr)
                        }
                    }
                } ?: ""
            } catch (e: Exception) {
                ""
            }
        }
    }

    fun go(newOff: Long) {
        val max = if (srcSize > 0L) ((srcSize - 1L) / bpr) * bpr else 0L
        val o = newOff.coerceIn(0L, max)
        offset = o - o % bpr
    }

    fun search() {
        val u = src ?: return
        val pat: ByteArray? = when (sMode) {
            0 -> if (query.isEmpty()) null else query.toByteArray(Charsets.UTF_8)
            1 -> if (query.isEmpty()) null else try { query.toByteArray(java.nio.charset.Charset.forName("Shift_JIS")) } catch (e: Exception) { null }
            else -> HexTools.parseHex(query)
        }
        if (pat == null || pat.isEmpty() || pat.size > HexTools.MAX_PATTERN) {
            note = f.hexBadQuery to false
            return
        }
        val from = if (lastHit >= 0L) lastHit + 1L else offset
        searching = true; note = null
        job = scope.launch {
            val r = withContext(Dispatchers.IO) {
                try {
                    ctx.contentResolver.openFileDescriptor(u, "r")?.use { fd ->
                        FileInputStream(fd.fileDescriptor).use { fin -> HexTools.find(fin.channel, pat, from) { isActive } }
                    } ?: -1L
                } catch (e: IOException) {
                    -1L
                }
            }
            searching = false
            if (r >= 0L) {
                lastHit = r
                go(r)
                note = f.hexFound(String.format("%08X", r)) to true
            } else {
                lastHit = -1L
                note = f.hexNotFound to false
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(f.hexTitle, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item {
            PickerCard(AppIcons.File, f.hexPicker, srcName?.let { f.info(it, fmtBytes(srcSize)) }, AppIcons.File) { if (!searching) pickSrc.launch(arrayOf("*/*")) }
        }
        item { Hint(f.hexHint) }
        if (src != null) {
            if (srcSize <= 0L) {
                item { Hint(f.hexEmpty) }
            } else {
                item { SegmentedChips(f.hexPerRow, bprIdx, 14.sp) { bprIdx = it; offset -= offset % (if (it == 0) 8 else 16) } }
                item {
                    val end = minOf(offset + pageBytes, srcSize) - 1L
                    Text(
                        f.hexRange(String.format("%08X", offset), String.format("%08X", end), String.format("%08X", srcSize)),
                        color = p.muted, fontSize = 12.sp,
                    )
                }
                // controls above the hex dump: jump to offset, search mode & box, Search button, then pagination
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            AppTextField(value = jump, onValueChange = { jump = it.take(16) }, label = f.hexJump)
                        }
                        Box(Modifier.width(96.dp)) {
                            SecondaryButton(f.hexGo) {
                                val o = HexTools.parseOffset(jump)
                                if (o == null) { note = f.hexBadOffset to false } else { note = null; lastHit = -1L; go(o) }
                            }
                        }
                    }
                }
                item { SegmentedChips(f.hexModes, sMode, 14.sp) { sMode = it; lastHit = -1L } }
                item { AppTextField(value = query, onValueChange = { query = it; lastHit = -1L }, label = f.hexQuery) }
                item {
                    if (searching) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(f.hexSearching, color = p.muted, fontSize = 13.sp)
                            LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
                            SecondaryButton(f.cancel) { job?.cancel(); searching = false }
                        }
                    } else {
                        PrimaryButton(if (lastHit >= 0L) f.hexSearchNext else f.hexSearch, query.isNotEmpty()) { search() }
                    }
                }
                // search results / errors appear right below the Search button (its controls are above)
                note?.let { n -> item { NoteCard(n, f.close) { note = null } } }
                item { HexNavBar(f.hexNav, offset > 0L, offset + pageBytes < srcSize, { lastHit = -1L; go(0L) }, { go(offset - pageBytes) }, { go(offset + pageBytes) }, { lastHit = -1L; go(((srcSize - 1L) / pageBytes) * pageBytes) }) }
                item {
                    AppCard(Modifier.fillMaxWidth()) {
                        Box(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(12.dp)) {
                            Text(
                                pageText, color = p.text, fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp, lineHeight = 16.sp, softWrap = false,
                            )
                        }
                    }
                }
                item { HexNavBar(f.hexNav, offset > 0L, offset + pageBytes < srcSize, { lastHit = -1L; go(0L) }, { go(offset - pageBytes) }, { go(offset + pageBytes) }, { lastHit = -1L; go(((srcSize - 1L) / pageBytes) * pageBytes) }) }
            }
        }
    }
}
}
