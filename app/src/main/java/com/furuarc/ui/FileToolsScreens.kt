package com.furuarc.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.Engine
import com.furuarc.core.FileOps
import com.furuarc.core.Checksums
import com.furuarc.core.JoinCheck
import com.furuarc.core.RenameOpts
import com.furuarc.core.RenameRow
import com.furuarc.defaultOutFolder
import com.furuarc.i18n.checkStr
import com.furuarc.i18n.fileToolsStr
import com.furuarc.storage.TreeWriter
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest

internal val OkColor = Color(0xFF66BB6A)
internal val ErrColor = Color(0xFFE57373)
internal val WarnColor = Color(0xFFFFB74D)

internal fun fmtBytes(b: Long): String = when {
    b >= 1L shl 30 -> String.format("%.2f GB", b / 1073741824.0)
    b >= 1L shl 20 -> String.format("%.2f MB", b / 1048576.0)
    b >= 1L shl 10 -> String.format("%.1f KB", b / 1024.0)
    else -> "$b B"
}

/** File name and size from a document URI. */
internal fun queryNameSize(ctx: Context, uri: Uri): Pair<String, Long> {
    var name: String? = null
    var size = -1L
    try {
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(0)
                if (!c.isNull(1)) size = c.getLong(1)
            }
        }
    } catch (e: Exception) { }
    if (size < 0L) {
        size = try { ctx.contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L } catch (e: Exception) { 0L }
    }
    return Pair(name ?: uri.lastPathSegment ?: "file", size)
}

/** Pick the output folder and remember its permission. */
@Composable
internal fun rememberOutPicker(onPicked: (Uri, String) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            onPicked(u, DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" })
        }
    }
    return { launcher.launch(null) }
}

@Composable
internal fun NoteCard(note: Pair<String, Boolean>, closeText: String, onClose: () -> Unit) {
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(note.first, color = if (note.second) OkColor else ErrColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            SecondaryButton(closeText) { onClose() }
        }
    }
}

// ======================= File Tools menu =======================

@Composable
fun FileToolsMenuScreen(onCut: () -> Unit, onSplit: () -> Unit, onJoin: () -> Unit, onRename: () -> Unit, onBack: () -> Unit) {
    val lang = LocalLang.current
    val f = fileToolsStr(lang)
    val ck = checkStr(lang)
    MenuListScreen(
        f.menuTitle,
        listOf(
            MenuSpec(AppIcons.File, ck.cutTitle, ck.cutSub, onCut),
            MenuSpec(AppIcons.Unarchive, f.splitTitle, f.splitSub, onSplit),
            MenuSpec(AppIcons.Archive, f.joinTitle, f.joinSub, onJoin),
            MenuSpec(AppIcons.Edit, f.renTitle, f.renSub, onRename),
        ),
        onBack,
    )
}

// ======================= File Splitter =======================

/** File Splitter: split one file into numbered parts (.001, .002, …), by size or by number of parts. */
@Composable
fun FileSplitterScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val f = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var src by remember { mutableStateOf<Uri?>(null) }
    var srcName by remember { mutableStateOf<String?>(null) }
    var srcSize by remember { mutableStateOf(0L) }
    var out by remember { mutableStateOf<Uri?>(null) }
    var outName by remember { mutableStateOf<String?>(null) }
    var mode by remember { mutableStateOf(0) } // 0 = by size, 1 = by number of parts
    var unit by remember { mutableStateOf(1) } // 0 = KB, 1 = MB
    var amount by remember { mutableStateOf("50") }
    var parts by remember { mutableStateOf("2") }
    var sumIdx by remember { mutableStateOf(0) } // 0 = create checksum, 1 = none
    var baseName by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        if (out == null) defaultOutFolder()?.let { out = it.first; outName = it.second }
    }

    val partSize: Long = if (mode == 0) {
        (amount.toLongOrNull() ?: 0L) * (if (unit == 0) 1024L else 1048576L)
    } else {
        FileOps.sizeForParts(srcSize, parts.toIntOrNull() ?: 0)
    }
    val count = if (src != null) FileOps.partCount(srcSize, partSize) else 0

    val pickSrc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            val (n, sz) = queryNameSize(ctx, u)
            src = u; srcName = n; srcSize = sz; baseName = n; note = null
        }
    }
    LaunchedEffect(Unit) {
        PendingFile.take("filetools.split")?.let { u ->
            val (n, sz) = queryNameSize(ctx, u)
            src = u; srcName = n; srcSize = sz; baseName = n; note = null
        }
    }
    val pickOut = rememberOutPicker { u, n -> out = u; outName = n }

    fun run() {
        val s = src
        val o = out
        if (s == null) { note = f.noFile to false; return }
        if (o == null) { note = f.noOut to false; return }
        val size = partSize
        val name = baseName.trim().ifEmpty { srcName ?: "file" }
        val withSum = sumIdx == 0
        busy = true; note = null; progress = 0f
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                try {
                    val pfd = ctx.contentResolver.openFileDescriptor(s, "r") ?: throw IOException("open")
                    pfd.use { fd ->
                        FileInputStream(fd.fileDescriptor).use { fin ->
                            val ch = fin.channel
                            val len = ch.size()
                            if (len <= 0L) throw IOException(f.emptyFile)
                            val total = FileOps.partCount(len, size)
                            if (total < 1 || total > FileOps.MAX_PARTS) throw IOException(f.tooMany(FileOps.MAX_PARTS))
                            val tw = TreeWriter(Engine.app, o)
                            val buf = java.nio.ByteBuffer.allocate(FileOps.BUF)
                            var pos = 0L
                            val wholeMd = MessageDigest.getInstance("SHA-256")
                            val pNames = ArrayList<String>()
                            val pHashes = ArrayList<String>()
                            for (i in 1..total) {
                                val end = minOf(len, pos + size)
                                val pn = FileOps.partName(name, i, total)
                                val partMd = MessageDigest.getInstance("SHA-256")
                                val of = tw.open(pn) ?: throw IOException("output")
                                of.stream.use { os ->
                                    while (pos < end) {
                                        buf.clear()
                                        buf.limit(minOf(buf.capacity().toLong(), end - pos).toInt())
                                        val k = ch.read(buf, pos)
                                        if (k <= 0) throw IOException(f.readFail)
                                        os.write(buf.array(), 0, k)
                                        if (withSum) {
                                            wholeMd.update(buf.array(), 0, k)
                                            partMd.update(buf.array(), 0, k)
                                        }
                                        pos += k
                                        progress = pos.toFloat() / len
                                    }
                                }
                                if (withSum) {
                                    pNames.add(pn)
                                    pHashes.add(FileOps.hex(partMd.digest()))
                                }
                            }
                            if (withSum) {
                                val txt = FileOps.checksumText(name, len, FileOps.hex(wholeMd.digest()), pNames, pHashes)
                                val sf = tw.open("$name.sha256") ?: throw IOException("output")
                                sf.stream.use { it.write(txt.toByteArray(Charsets.UTF_8)) }
                            }
                            Result.success(Pair(total, len))
                        }
                    }
                } catch (e: Throwable) { Result.failure<Pair<Int, Long>>(e) }
            }
            busy = false
            val ok = r.getOrNull()
            note = if (ok != null) (f.splitDone(ok.first, name, fmtBytes(ok.second)) + (if (withSum) f.splitSumNote(name) else "")) to true
            else f.fail(r.exceptionOrNull()?.message ?: "?") to false
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(f.splitTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item {
            PickerCard(AppIcons.File, f.source, srcName?.let { f.info(it, fmtBytes(srcSize)) }, AppIcons.File) { if (!busy) pickSrc.launch(arrayOf("*/*")) }
        }
        item { SectionLabel(f.splitBy) }
        item { SegmentedChips(f.splitModes, mode) { mode = it } }
        if (mode == 0) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        AppTextField(value = amount, onValueChange = { v -> amount = v.filter { ch -> ch.isDigit() }.take(7) }, label = f.splitSize, number = true)
                    }
                    Box(Modifier.weight(1f)) { SegmentedChips(listOf("KB", "MB"), unit) { unit = it } }
                }
            }
        } else {
            item {
                AppTextField(value = parts, onValueChange = { v -> parts = v.filter { ch -> ch.isDigit() }.take(5) }, label = f.splitCount, number = true)
            }
        }
        if (src != null && srcSize > 0L && partSize > 0L) item {
            when {
                count > FileOps.MAX_PARTS -> Text(f.tooMany(FileOps.MAX_PARTS), color = ErrColor, fontSize = 13.sp, lineHeight = 19.sp)
                count == 1 -> Text(f.splitOne, color = WarnColor, fontSize = 13.sp, lineHeight = 19.sp)
                count > 1 -> Hint(f.splitInfo(count, fmtBytes(partSize), fmtBytes(srcSize - (count - 1).toLong() * partSize)))
                else -> {}
            }
        }
        item { DetailHint(f.splitHint) }
        item { SegmentedChips(f.sumOpts, sumIdx, 14.sp) { sumIdx = it } }
        item { AppTextField(value = baseName, onValueChange = { baseName = it }, label = f.splitBase) }
        item { PickerCard(AppIcons.Folder, f.outFolder, outName, AppIcons.Folder) { if (!busy) pickOut() } }
        item { PrimaryButton(f.splitRun, src != null && !busy && count in 1..FileOps.MAX_PARTS) { run() } }
        if (busy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(f.splitBusy, color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
            }
        }
        note?.let { n -> item { NoteCard(n, f.close) { note = null } } }
    }
}
}

// ======================= folder listing (used by Merger & Bulk Rename) =======================

private class RenEntry(val uri: Uri, val name: String, val isDir: Boolean, val dir: String, val size: Long)

/** Folder contents: files and subfolders. [dir] = path relative to the parent folder ("" = the selected folder). */
private fun listFolder(ctx: Context, tree: Uri, recursive: Boolean): List<RenEntry> {
    val out = ArrayList<RenEntry>()
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
                val size = if (c.isNull(3)) 0L else c.getLong(3)
                out.add(RenEntry(DocumentsContract.buildDocumentUriUsingTree(tree, id), name, isDir, rel, size))
                if (isDir && recursive) subs.add(Pair(id, name))
            }
        }
        for ((id, name) in subs) walk(id, if (rel.isEmpty()) name else "$rel/$name")
    }
    walk(DocumentsContract.getTreeDocumentId(tree), "")
    return out
}

// ======================= File Merger =======================

/** One set of parts. [check.order] = indices into [uris]/[names]/[sizes]. [sum] = the .sha256 file if present. */
private class JoinSet(
    val base: String,
    val uris: List<Uri>,
    val names: List<String>,
    val sizes: List<Long>,
    val sum: Uri?,
    val sumName: String?,
    val check: JoinCheck,
)

/** Manual selection: all files are treated as one set; .sha256 files are separated out. */
private fun buildManualSet(infos: List<Triple<Uri, String, Long>>): JoinSet {
    val sumItem = infos.firstOrNull { FileOps.isChecksumName(it.second) }
    val parts = infos.filter { !FileOps.isChecksumName(it.second) }
    val names = parts.map { it.second }
    val sizes = parts.map { it.third }
    val ck = FileOps.checkJoin(names, sizes)
    return JoinSet(ck.base, parts.map { it.first }, names, sizes, sumItem?.first, sumItem?.second, ck)
}

/** Folder selection: all numbered parts are grouped by base name; sets with only 1 part are ignored. */
private fun buildFolderSets(entries: List<RenEntry>): List<JoinSet> {
    val files = entries.filter { !it.isDir }
    val sums = HashMap<String, RenEntry>()
    for (e in files) if (FileOps.isChecksumName(e.name)) sums[FileOps.checksumBase(e.name).lowercase()] = e
    val groups = LinkedHashMap<String, MutableList<RenEntry>>()
    for (e in files) {
        val pn = FileOps.parsePartName(e.name) ?: continue
        groups.getOrPut(pn.first.lowercase()) { ArrayList() }.add(e)
    }
    val out = ArrayList<JoinSet>()
    for ((_, list) in groups) {
        if (list.size < 2) continue
        val names = list.map { it.name }
        val sizes = list.map { it.size }
        val base = FileOps.parsePartName(list[0].name)?.first ?: continue
        val sumE = sums[base.lowercase()]
        out.add(JoinSet(base, list.map { it.uri }, names, sizes, sumE?.uri, sumE?.name, FileOps.checkJoin(names, sizes)))
    }
    return out.sortedWith(Comparator<JoinSet> { a, b -> FileOps.naturalCompare(a.base, b.base) })
}

/** ver: 0 no verification, 1 match, 2 part mismatch, 3 whole file mismatch, 4 checksum does not contain this part. */
private class JoinRes(val bytes: Long, val ver: Int, val badPart: String?)

private fun joinOne(ctx: Context, tw: TreeWriter, set: JoinSet, outName: String, onBytes: (Long) -> Unit): JoinRes {
    val sumUri = set.sum
    val cs: Checksums? = if (sumUri == null) null else try {
        ctx.contentResolver.openInputStream(sumUri)?.use { FileOps.parseChecksum(String(it.readBytes(), Charsets.UTF_8)) }
    } catch (e: Exception) { null }

    val wholeMd = MessageDigest.getInstance("SHA-256")
    var badPart: String? = null
    var done = 0L
    val buf = ByteArray(FileOps.BUF)
    val of = tw.open(outName) ?: throw IOException("output")
    of.stream.use { os ->
        for (idx in set.check.order) {
            val partMd = MessageDigest.getInstance("SHA-256")
            val ins = ctx.contentResolver.openInputStream(set.uris[idx]) ?: throw IOException(set.names[idx])
            ins.use { input ->
                while (true) {
                    val k = input.read(buf)
                    if (k < 0) break
                    os.write(buf, 0, k)
                    if (cs != null) {
                        wholeMd.update(buf, 0, k)
                        partMd.update(buf, 0, k)
                    }
                    done += k
                    onBytes(k.toLong())
                }
            }
            if (cs != null && badPart == null) {
                val exp = cs.parts[set.names[idx].lowercase()]
                if (exp != null && exp != FileOps.hex(partMd.digest())) badPart = set.names[idx]
            }
        }
    }
    var ver = 0
    if (cs != null) {
        val anyPart = set.check.order.any { cs.parts.containsKey(set.names[it].lowercase()) }
        val whole = cs.whole
        ver = when {
            badPart != null -> 2
            whole != null && whole != FileOps.hex(wholeMd.digest()) -> 3
            anyPart || whole != null -> 1
            else -> 4
        }
    }
    return JoinRes(done, ver, badPart)
}

/** File Merger: the reverse of File Splitter. Parts are chosen manually or detected from one folder; the .sha256 checksum is verified if present. */
@Composable
fun FileJoinerScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val f = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var sets by remember { mutableStateOf<List<JoinSet>>(emptyList()) }
    var folderMode by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf<String?>(null) }
    var outFileName by remember { mutableStateOf("") }
    var out by remember { mutableStateOf<Uri?>(null) }
    var outName by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        if (out == null) defaultOutFolder()?.let { out = it.first; outName = it.second }
    }

    val pickParts = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { list ->
        if (list.isNotEmpty()) {
            val infos = list.map { u -> val ns = queryNameSize(ctx, u); Triple(u, ns.first, ns.second) }
            val set = buildManualSet(infos)
            sets = listOf(set)
            folderMode = false
            folderName = null
            outFileName = set.base
            note = null
        }
    }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            val entries = try { listFolder(ctx, u, false) } catch (e: Exception) { emptyList() }
            sets = buildFolderSets(entries)
            folderMode = true
            folderName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
            note = null
        }
    }
    val pickOut = rememberOutPicker { u, n -> out = u; outName = n }

    val valid = sets.filter { it.check.ok }
    val manual = if (!folderMode) sets.firstOrNull() else null

    fun problemOf(ck: JoinCheck): String? = when (ck.code) {
        JoinCheck.NOT_PART -> f.notPart(ck.arg)
        JoinCheck.BASE_DIFF -> f.baseDiff(ck.arg)
        JoinCheck.DUP -> f.dup(ck.arg)
        JoinCheck.MISSING -> f.missing(ck.arg)
        JoinCheck.ONE -> f.onlyOne
        else -> null
    }

    fun startJoin() {
        val o = out
        if (o == null) { note = f.noOut to false; return }
        val todo = valid
        if (todo.isEmpty()) return
        val single = !folderMode
        val custom = outFileName.trim()
        busy = true; note = null; progress = 0f
        scope.launch {
            val lines = ArrayList<String>()
            var allOk = true
            withContext(Dispatchers.IO) {
                val tw = TreeWriter(Engine.app, o)
                var grand = 0L
                for (s in todo) grand += s.check.total
                var doneAll = 0L
                for (set in todo) {
                    val name = if (single && custom.isNotEmpty()) custom else set.base
                    try {
                        val r = joinOne(ctx, tw, set, name) { k ->
                            doneAll += k
                            if (grand > 0L) progress = (doneAll.toFloat() / grand).coerceAtMost(1f)
                        }
                        val ver = when (r.ver) {
                            1 -> f.verOk
                            2 -> f.verBadPart(r.badPart ?: "?")
                            3 -> f.verBadWhole
                            4 -> f.verEmpty
                            else -> f.verNone
                        }
                        if (r.ver == 2 || r.ver == 3) {
                            allOk = false
                            lines.add(f.joinLineBad(name, ver))
                        } else {
                            lines.add(f.joinLineOk(name, fmtBytes(r.bytes), ver))
                        }
                    } catch (e: Throwable) {
                        allOk = false
                        lines.add(f.joinLineErr(name, e.message ?: "?"))
                    }
                }
            }
            busy = false
            note = lines.joinToString("\n") to allOk
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(f.joinTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item {
            PickerCard(AppIcons.File, f.joinPicker, if (manual == null) null else f.joinPicked(manual.names.size), AppIcons.File) {
                if (!busy) pickParts.launch(arrayOf("*/*"))
            }
        }
        item { PickerCard(AppIcons.Folder, f.joinFolderPicker, folderName, AppIcons.Folder) { if (!busy) pickFolder.launch(null) } }
        item { DetailHint(f.joinHint + "\n" + f.joinFolderHint) }

        if (manual != null) {
            val pr = problemOf(manual.check)
            if (pr != null) item { Text(pr, color = ErrColor, fontSize = 13.sp, lineHeight = 19.sp) }
            if (manual.check.ok) {
                item {
                    val first = manual.names[manual.check.order[0]]
                    val last = manual.names[manual.check.order[manual.check.order.size - 1]]
                    Hint(f.joinInfo(manual.check.order.size, fmtBytes(manual.check.total), first, last))
                }
                if (manual.check.startWarn) item { Text(f.startWarn, color = WarnColor, fontSize = 13.sp, lineHeight = 19.sp) }
                if (manual.sumName != null) item { Text(f.sumFound(manual.sumName), color = OkColor, fontSize = 13.sp, lineHeight = 19.sp) }
            }
            item { AppTextField(value = outFileName, onValueChange = { outFileName = it }, label = f.joinOutName) }
        }
        if (folderMode) {
            if (sets.isEmpty()) {
                item { Hint(f.joinNoSets) }
            } else {
                for (set in sets) {
                    item {
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(set.base, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                Text(f.joinSetInfo(set.names.size, fmtBytes(set.sizes.sum())), color = p.muted, fontSize = 13.sp)
                                val pr = problemOf(set.check)
                                if (pr != null) {
                                    Text(pr, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
                                } else {
                                    Text(f.joinSetOk, color = OkColor, fontSize = 13.sp)
                                    if (set.check.startWarn) Text(f.startWarn, color = WarnColor, fontSize = 12.sp, lineHeight = 17.sp)
                                    if (set.sumName != null) Text(f.sumFound(set.sumName), color = OkColor, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { PickerCard(AppIcons.Folder, f.outFolder, outName, AppIcons.Folder) { if (!busy) pickOut() } }
        item {
            val label = if (folderMode && valid.size > 1) f.joinRunAll(valid.size) else f.joinRun
            PrimaryButton(label, valid.isNotEmpty() && !busy) { startJoin() }
        }
        if (busy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(f.joinBusy, color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
            }
        }
        note?.let { n -> item { NoteCard(n, f.close) { note = null } } }
    }
}
}

// ======================= Bulk Rename =======================

/** Rename result: [undo] holds (current uri, current name, old name) for the Undo button. */
private class RenResult(val done: Int, val failed: Int, val undo: List<Triple<Uri, String, String>>, val error: String?)

/**
 * Rename [items] = (uri, current name, new name) in two phases: all files are first given a temporary
 * name, then the final name. This makes name swaps (a->b, b->a) and chained names safe.
 * Files that fail in the second phase are restored to their old names.
 */
private fun applyRename(ctx: Context, items: List<Triple<Uri, String, String>>): RenResult {
    val resolver = ctx.contentResolver
    val stamp = System.currentTimeMillis().toString(36)
    val temp = ArrayList<Triple<Uri, String, String>>() // (temporary uri, old name, new name)
    try {
        for ((i, x) in items.withIndex()) {
            val tmpName = ".furuarc_tmp_${stamp}_$i"
            val nu = DocumentsContract.renameDocument(resolver, x.first, tmpName) ?: throw IOException(x.second)
            temp.add(Triple(nu, x.second, x.third))
        }
    } catch (e: Exception) {
        for (t in temp) {
            try { DocumentsContract.renameDocument(resolver, t.first, t.second) } catch (e2: Exception) { }
        }
        return RenResult(0, items.size, emptyList(), e.message ?: "?")
    }
    val undo = ArrayList<Triple<Uri, String, String>>()
    var failed = 0
    for (t in temp) {
        val nu = try { DocumentsContract.renameDocument(resolver, t.first, t.third) } catch (e: Exception) { null }
        if (nu != null) {
            undo.add(Triple(nu, t.third, t.second))
        } else {
            try { DocumentsContract.renameDocument(resolver, t.first, t.second) } catch (e: Exception) { }
            failed++
        }
    }
    return RenResult(undo.size, failed, undo, null)
}

private class PlanItem(val entry: RenEntry, val row: RenameRow)

private const val REN_PER_PAGE = 50 // Bulk Rename preview, same as the Translation Editor

/** Preview page navigation: ‹ page label › (same look as the Translation Editor). */
@Composable
internal fun RenPager(page: Int, pages: Int, label: String, onPage: (Int) -> Unit) {
    PageNav(page, pages, onPage)
}

/** Bulk Rename: pick a folder (optionally with subfolders), set the new-name rules, view the preview, then apply (can be undone). */
@Composable
fun BatchRenameScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val f = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var dir by remember { mutableStateOf<Uri?>(null) }
    var dirName by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<RenEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var recIdx by remember { mutableStateOf(0) } // 0 this folder only, 1 including subfolders
    var extFilter by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf(0) }
    var find by remember { mutableStateOf("") }
    var replace by remember { mutableStateOf("") }
    var caseIdx by remember { mutableStateOf(0) } // 0 case-sensitive, 1 ignore case
    var pattern by remember { mutableStateOf("{nama}_{no}") }
    var start by remember { mutableStateOf("1") }
    var digits by remember { mutableStateOf("3") }
    var prefix by remember { mutableStateOf("") }
    var suffix by remember { mutableStateOf("") }
    var wrongCs by remember { mutableStateOf("windows-1252") }
    var rightCs by remember { mutableStateOf("Shift_JIS") }
    var letters by remember { mutableStateOf(0) }
    var busy by remember { mutableStateOf(false) }
    var confirm by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var undo by remember { mutableStateOf<List<Triple<Uri, String, String>>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(0) }
    val listState = rememberLazyListState()

    fun load() {
        val d = dir ?: return
        val rec = recIdx == 1
        loading = true
        scope.launch {
            val l = withContext(Dispatchers.IO) { try { listFolder(ctx, d, rec) } catch (e: Exception) { emptyList() } }
            entries = l
            loading = false
        }
    }

    val pickDir = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            dir = u
            dirName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
            note = null
            undo = emptyList()
            query = ""; page = 0
            load()
        }
    }

    val files = remember(entries, extFilter) {
        entries.filter { !it.isDir && FileOps.matchesExt(it.name, extFilter) }
            .sortedWith(Comparator<RenEntry> { a, b ->
                val d = FileOps.naturalCompare(a.dir, b.dir)
                if (d != 0) d else {
                    val c = FileOps.naturalCompare(a.name, b.name)
                    if (c != 0) c else a.name.compareTo(b.name)
                }
            })
    }
    val opts = RenameOpts(
        mode = mode,
        find = find,
        replace = replace,
        ignoreCase = caseIdx == 1,
        pattern = pattern,
        start = start.toIntOrNull() ?: 1,
        digits = (digits.toIntOrNull() ?: 1).coerceIn(1, 9),
        prefix = prefix,
        suffix = suffix,
        caseMode = letters,
        wrongCs = wrongCs,
        rightCs = rightCs,
    )
    val plan = remember(files, entries, mode, find, replace, caseIdx, pattern, start, digits, prefix, suffix, letters, wrongCs, rightCs) {
        val byDir = entries.groupBy { it.dir }
        val res = ArrayList<PlanItem>()
        for ((d, fs) in files.groupBy { it.dir }) {
            val existing = (byDir[d] ?: emptyList()).map { it.name }
            val pl = FileOps.planRename(fs.map { it.name }, existing, opts)
            for (i in fs.indices) res.add(PlanItem(fs[i], pl.rows[i]))
        }
        res
    }
    val changed = plan.count { it.row.status == RenameRow.CHANGE }
    val same = plan.count { it.row.status == RenameRow.SAME }
    val bad = plan.count { it.row.status == RenameRow.CONFLICT || it.row.status == RenameRow.INVALID }
    // search in the preview (old name, new name, or folder); only filters the display, not what gets renamed
    val visible = remember(plan, query) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) plan else plan.filter { it.row.old.lowercase().contains(q) || it.row.new.lowercase().contains(q) || it.entry.dir.lowercase().contains(q) }
    }
    val pages = maxOf(1, (visible.size + REN_PER_PAGE - 1) / REN_PER_PAGE)
    val curPage = page.coerceIn(0, pages - 1)
    val pageItems = remember(visible, curPage) { visible.drop(curPage * REN_PER_PAGE).take(REN_PER_PAGE) }
    val pageLabel = f.renPageLabel(curPage + 1, pages, visible.size)
    // index of the Preview title item in the LazyColumn: 6 fixed items + rule items (depending on mode) + 1 letter-case row
    val previewIdx = 6 + (if (mode == 2) 2 else 3) + 1

    fun doRename() {
        val items = ArrayList<Triple<Uri, String, String>>()
        for (pi in plan) {
            if (pi.row.status == RenameRow.CHANGE) items.add(Triple(pi.entry.uri, pi.row.old, pi.row.new))
        }
        if (items.isEmpty()) return
        busy = true; note = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { applyRename(ctx, items) }
            busy = false
            if (r.error != null) {
                note = f.fail(r.error) to false
                undo = emptyList()
            } else {
                note = (if (r.failed == 0) f.renDone(r.done) else f.renPartial(r.done, r.failed)) to (r.failed == 0)
                undo = r.undo
            }
            load()
        }
    }

    fun doUndo() {
        val items = undo
        if (items.isEmpty()) return
        busy = true; note = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { applyRename(ctx, items) }
            busy = false
            note = if (r.error != null) f.fail(r.error) to false else f.renUndone(r.done) to (r.failed == 0)
            undo = emptyList()
            load()
        }
    }

    if (confirm) {
        ConfirmDialog(
            title = f.renConfirmTitle(changed),
            message = f.renConfirmMsg,
            yes = f.renRun,
            no = f.renNo,
            onConfirm = { confirm = false; doRename() },
            onDismiss = { confirm = false },
        )
    }

    val wrongOpts = listOf("windows-1252" to "Windows-1252", "ISO-8859-1" to "ISO-8859-1", "IBM437" to "CP437 (DOS)")
    val rightOpts = listOf("Shift_JIS" to "Shift-JIS (JP)", "GBK" to "GBK (CN)", "Big5" to "Big5 (TW)", "EUC-KR" to "EUC-KR (KR)", "UTF-8" to "UTF-8")

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(f.renTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = listState, contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item { PickerCard(AppIcons.Folder, f.renFolder, dirName, AppIcons.Folder) { if (!busy) pickDir.launch(null) } }
        item { SegmentedChips(f.renRecursive, recIdx, 14.sp) { recIdx = it; undo = emptyList(); load() } }
        item { AppTextField(value = extFilter, onValueChange = { extFilter = it }, label = f.renExt) }
        item { Hint(f.renExtHint) }
        item {
            AppDropdown(f.renRule, mode.toString(), f.renModes.mapIndexed { i, s -> i.toString() to s }, !busy) { mode = it.toInt() }
        }
        when (mode) {
            0 -> {
                item { AppTextField(value = find, onValueChange = { find = it }, label = f.renFind) }
                item { AppTextField(value = replace, onValueChange = { replace = it }, label = f.renReplace) }
                item { SegmentedChips(f.renCaseMatch, caseIdx) { caseIdx = it } }
            }
            1 -> {
                item { AppTextField(value = pattern, onValueChange = { pattern = it }, label = f.renPattern) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(Modifier.weight(1f)) {
                            AppTextField(value = start, onValueChange = { v -> start = v.filter { ch -> ch.isDigit() }.take(6) }, label = f.renStart, number = true)
                        }
                        Box(Modifier.weight(1f)) {
                            AppTextField(value = digits, onValueChange = { v -> digits = v.filter { ch -> ch.isDigit() }.take(1) }, label = f.renDigits, number = true)
                        }
                    }
                }
                item { DetailHint(f.renPatternHint) }
            }
            2 -> {
                item { AppTextField(value = prefix, onValueChange = { prefix = it }, label = f.renPrefix) }
                item { AppTextField(value = suffix, onValueChange = { suffix = it }, label = f.renSuffix) }
            }
            else -> {
                item { AppDropdown(f.renWrong, wrongCs, wrongOpts, !busy) { wrongCs = it } }
                item { AppDropdown(f.renRight, rightCs, rightOpts, !busy) { rightCs = it } }
                item { DetailHint(f.renFixHint) }
            }
        }
        item { SegmentedChips(f.renLetters, letters, 14.sp) { letters = it } }

        if (dir == null) {
            item { Hint(f.renNoFolder) }
        } else if (loading) {
            item { Hint(f.dupListing) }
        } else if (files.isEmpty()) {
            item { Hint(f.renEmpty) }
        } else {
            item { SectionLabel(f.renPreview) }
            item { Hint(f.renSummary(files.size, changed, same, bad)) }
            item { AppTextField(value = query, onValueChange = { v -> query = v; page = 0 }, label = f.renSearch) }
            if (visible.isEmpty()) item { Hint(f.renNoMatch) }
            if (pages > 1) item { RenPager(curPage, pages, pageLabel) { np -> page = np } }
            items(pageItems) { pi ->
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Text((if (pi.entry.dir.isEmpty()) "" else pi.entry.dir + "/") + pi.row.old, color = p.muted, fontSize = 12.sp, lineHeight = 16.sp)
                        val color = when (pi.row.status) {
                            RenameRow.SAME -> p.muted
                            RenameRow.CHANGE -> p.text
                            else -> ErrColor
                        }
                        Text("→ " + pi.row.new, color = color, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (pages > 1) {
                item {
                    RenPager(curPage, pages, pageLabel) { np ->
                        page = np
                        scope.launch { listState.scrollToItem(previewIdx) }
                    }
                }
            }
            if (bad > 0) item { Text(f.renBad, color = ErrColor, fontSize = 13.sp, lineHeight = 19.sp) }
        }

        item { PrimaryButton(f.renRun, dir != null && !busy && !loading && changed > 0 && bad == 0) { confirm = true } }
        if (undo.isNotEmpty()) item { SecondaryButton(f.renUndo, !busy) { doUndo() } }
        if (busy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(f.renBusy, color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
            }
        }
        note?.let { n -> item { NoteCard(n, f.close) { note = null } } }
    }
}
}
