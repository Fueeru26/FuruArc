package com.furuarc.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.Editable
import android.text.Layout
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.furuarc.MediaItem
import com.furuarc.core.ConflictPolicy
import com.furuarc.core.Highlight
import com.furuarc.core.TxtDoc
import com.furuarc.defaultOutFolder
import com.furuarc.i18n.TextEditStr
import com.furuarc.i18n.mediaStr
import com.furuarc.settings.SettingsStore
import com.furuarc.storage.TreeWriter
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

// ======================= collapsible sections (hide / unhide) =======================

/** Open/closed state of each section; a closed section does not compose its content. */
internal class FoldState {
    val open = mutableStateMapOf<String, Boolean>()
}

@Composable
internal fun FoldHeader(title: String, open: Boolean, onToggle: () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().clickable { onToggle() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
            Text(if (open) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
        }
    }
}

/** Sections that can be opened/closed (closed by default so the screen is not too long). */
@Composable
internal fun FoldSection(fold: FoldState, key: String, title: String, defaultOpen: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val isOpen = fold.open[key] ?: defaultOpen
    // header and contents sit together on one label background
    val p = LocalPalette.current
    RowPanel(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().clickable { fold.open[key] = !isOpen }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                Text(if (isOpen) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
            }
            if (isOpen) Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
        }
    }
}

// ======================= editor settings =======================

/** File Text Editor settings (persisted) and the Output & Results choices (this session). */
@Stable
internal class EditorPrefs(private val st: SettingsStore) {
    var font by mutableStateOf((st.getStr("te_font", "14").toIntOrNull() ?: 14).coerceIn(10, 24))
        private set
    var wrap by mutableStateOf(st.getStr("te_wrap", "1") == "1")
        private set
    var enc by mutableStateOf(st.getStr("te_enc", "auto"))
        private set
    var backup by mutableStateOf(st.getStr("te_backup", "1") == "1")
        private set
    var pol by mutableStateOf((st.getStr("te_pol", "0").toIntOrNull() ?: 0).coerceIn(0, 2))
        private set
    /** Initial JSON view: 0 = tree, 1 = text. */
    var jsonView by mutableStateOf((st.getStr("te_jview", "0").toIntOrNull() ?: 0).coerceIn(0, 1))
        private set
    /** JSON save format from the tree view: 0 = pretty, 1 = compact. */
    var jsonFmt by mutableStateOf((st.getStr("te_jfmt", "0").toIntOrNull() ?: 0).coerceIn(0, 1))
        private set

    var symbols by mutableStateOf(st.getStr("te_sym", "1") == "1")
        private set
    var hl by mutableStateOf(st.getStr("te_hl", "1") == "1")
        private set
    /** CSV delimiter: 0 auto, 1 comma, 2 semicolon, 3 tab, 4 vertical bar. */
    var csvDelim by mutableStateOf((st.getStr("te_delim", "0").toIntOrNull() ?: 0).coerceIn(0, 4))
        private set
    var csvHeader by mutableStateOf(st.getStr("te_csvhdr", "1") == "1")
        private set
    /** CSV quoting on save: 0 follow the original file, 1 only when needed, 2 all cells. */
    var csvQuote by mutableStateOf((st.getStr("te_csvq", "0").toIntOrNull() ?: 0).coerceIn(0, 2))
        private set

    var lineNo by mutableStateOf(st.getStr("te_lines", "0") == "1")
        private set
    var showWs by mutableStateOf(st.getStr("te_ws", "0") == "1")
        private set

    /** 0 = Output Folder, 1 = Overwrite Original File (Folder mode only). */
    var outMode by mutableStateOf(0)
    var out by mutableStateOf<Uri?>(null)
    var outName by mutableStateOf<String?>(null)

    fun saveFont(v: Int) { font = v; st.putStr("te_font", v.toString()) }
    fun saveWrap(v: Boolean) { wrap = v; st.putStr("te_wrap", if (v) "1" else "0") }
    fun saveEnc(v: String) { enc = v; st.putStr("te_enc", v) }
    fun saveBackup(v: Boolean) { backup = v; st.putStr("te_backup", if (v) "1" else "0") }
    fun savePol(v: Int) { pol = v; st.putStr("te_pol", v.toString()) }
    fun saveJsonView(v: Int) { jsonView = v; st.putStr("te_jview", v.toString()) }
    fun saveJsonFmt(v: Int) { jsonFmt = v; st.putStr("te_jfmt", v.toString()) }
    fun saveLineNo(v: Boolean) { lineNo = v; st.putStr("te_lines", if (v) "1" else "0") }
    fun saveShowWs(v: Boolean) { showWs = v; st.putStr("te_ws", if (v) "1" else "0") }
    fun saveSymbols(v: Boolean) { symbols = v; st.putStr("te_sym", if (v) "1" else "0") }
    fun saveHl(v: Boolean) { hl = v; st.putStr("te_hl", if (v) "1" else "0") }
    fun saveCsvDelim(v: Int) { csvDelim = v; st.putStr("te_delim", v.toString()) }
    fun saveCsvHeader(v: Boolean) { csvHeader = v; st.putStr("te_csvhdr", if (v) "1" else "0") }
    fun saveCsvQuote(v: Int) { csvQuote = v; st.putStr("te_csvq", v.toString()) }

    fun policy(): ConflictPolicy = when (pol) { 1 -> ConflictPolicy.OVERWRITE; 2 -> ConflictPolicy.SKIP; else -> ConflictPolicy.RENAME }
}

@Composable
internal fun rememberEditorPrefs(): EditorPrefs {
    val ctx = LocalContext.current
    val ep = remember { EditorPrefs(SettingsStore(ctx)) }
    // default output folder from Tool Settings, same as the Asset Converter
    LaunchedEffect(Unit) {
        if (ep.out == null) defaultOutFolder()?.let { ep.out = it.first; ep.outName = it.second }
    }
    return ep
}

/** Per-document choices: encoding and line ending when saving. */
@Stable
internal class DocFormat {
    var saveEnc by mutableStateOf("same")
    /** auto = follow the original file, lf, crlf */
    var eol by mutableStateOf("auto")
}

internal fun encLabel(te: TextEditStr, e: String): String = when (e) {
    "auto" -> te.encAuto
    "same" -> te.encSame
    "utf8" -> "UTF-8"
    "utf8bom" -> "UTF-8 (BOM)"
    "sjis" -> "Shift-JIS"
    "gbk" -> "GBK"
    "big5" -> "Big5"
    "euckr" -> "EUC-KR"
    "utf16le" -> "UTF-16 LE (BOM)"
    "utf16be" -> "UTF-16 BE (BOM)"
    else -> e
}

internal fun encDisplay(te: TextEditStr, d: TxtDoc.Loaded): String =
    encLabel(te, if (d.enc == "utf8" && d.bom) "utf8bom" else d.enc)

/** Contents of the "Editor View" section: font size and line wrapping. */
@Composable
internal fun EditorLookBlock(ep: EditorPrefs, te: TextEditStr) {
    val sizes = listOf(12, 14, 16, 18, 20)
    SectionLabel(te.fontSize)
    SegmentedChips(sizes.map { it.toString() }, sizes.indexOf(ep.font).let { if (it < 0) 1 else it }, 14.sp) { i -> ep.saveFont(sizes[i]) }
    CheckRow(te.wrapLines, ep.wrap) { ep.saveWrap(it) }
    CheckRow(te.showLineNumbers, ep.lineNo) { ep.saveLineNo(it) }
    CheckRow(te.showWhitespace, ep.showWs) { ep.saveShowWs(it) }
    CheckRow(te.symbolBar, ep.symbols) { ep.saveSymbols(it) }
    CheckRow(te.syntaxHl, ep.hl) { ep.saveHl(it) }
    DetailHint(te.hlHint)
}

/** Encoding when opening (applies to the next file). [onReload] != null shows a button to reload the currently open file. */
@Composable
internal fun OpenEncBlock(ep: EditorPrefs, te: TextEditStr, onReload: (() -> Unit)?) {
    AppDropdown(te.openEnc, ep.enc, TxtDoc.OPEN_ENCODINGS.map { it to encLabel(te, it) }) { ep.saveEnc(it) }
    if (onReload != null) SecondaryButton(te.reload) { onReload() }
    DetailHint(te.encHint)
}

/** Encoding and line ending when saving the currently open file. */
@Composable
internal fun SaveFormatBlock(te: TextEditStr, fmt: DocFormat) {
    AppDropdown(te.saveAs, fmt.saveEnc, TxtDoc.SAVE_ENCODINGS.map { it to encLabel(te, it) }) { fmt.saveEnc = it }
    SectionLabel(te.eolLabel)
    val eols = listOf("auto", "lf", "crlf")
    SegmentedChips(listOf(te.eolAuto, "LF", "CRLF"), eols.indexOf(fmt.eol).let { if (it < 0) 0 else it }, 14.sp) { i -> fmt.eol = eols[i] }
}

// ======================= Output & Results (same as the Asset Converter) =======================

/**
 * Contents of "Output & Results": Output Folder / Overwrite Original File mode (Folder mode only), output folder,
 * "If the result file already exists", and Backup. [outFileName] != null shows the result file name field.
 */
@Composable
internal fun OutputBlock(
    ep: EditorPrefs,
    te: TextEditStr,
    folderMode: Boolean,
    pickOut: () -> Unit,
    outFileName: String? = null,
    onOutFileName: (String) -> Unit = {},
) {
    val p = LocalPalette.current
    val m = mediaStr(LocalLang.current)
    val inPlace = folderMode && ep.outMode == 1
    if (folderMode) SegmentedChips(m.outModes, ep.outMode, 14.sp) { ep.outMode = it }
    if (!inPlace) {
        if (folderMode) PickerCard(AppIcons.FolderUpload, m.outFolder, ep.outName, AppIcons.FolderPlus) { pickOut() }
        SectionLabel(m.conflict)
        SegmentedChips(m.conflictOpts, ep.pol, 14.sp) { ep.savePol(it) }
        if (!folderMode) PickerCard(AppIcons.FolderUpload, m.outFolder, ep.outName, AppIcons.FolderPlus) { pickOut() }
        if (outFileName != null) AppTextField(value = outFileName, onValueChange = onOutFileName, label = te.outFileName)
        if (ep.out == null) Hint(m.needOut)
        if (!folderMode) DetailHint(te.fileModeHint)
    } else {
        CheckRow(m.backupCheck, ep.backup) { ep.saveBackup(it) }
        DetailHint(te.overwriteHow)
        if (!ep.backup) Text(m.backupWarn, color = WarnColor, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

/** Per-document note so that the backup and result name are not created repeatedly on the second and later saves. */
internal class SaveMemo {
    var backedUp = false
    var savedUri: Uri? = null
}

internal class SaveOutcome(val ok: Boolean, val msg: String)

/** Write [bytes] according to the Output & Results choices. Called on the IO thread. */
internal fun teSave(
    ctx: Context, te: TextEditStr, ep: EditorPrefs, item: MediaItem, tree: Uri?,
    bytes: ByteArray, outFileName: String, memo: SaveMemo, needOut: String,
): SaveOutcome {
    try {
        val relOut = if (tree != null) item.rel.substringAfter('/', "") else ""
        if (tree != null && ep.outMode == 1) {
            if (ep.backup && !memo.backedUp) {
                val bpath = "_backup/" + (if (relOut.isEmpty()) "" else "$relOut/") + item.name
                val bf = TreeWriter(ctx, tree).open(bpath, ConflictPolicy.RENAME) ?: throw IOException("-")
                bf.stream.use { os ->
                    (ctx.contentResolver.openInputStream(item.uri) ?: throw IOException(te.cantReadOriginal)).use { it.copyTo(os, 256 * 1024) }
                }
                memo.backedUp = true
            }
            (ctx.contentResolver.openOutputStream(item.uri, "wt") ?: throw IOException(te.cantWrite)).use { it.write(bytes) }
            return SaveOutcome(true, te.savedInPlace(item.name))
        }
        val saved = memo.savedUri
        if (saved != null) {
            (ctx.contentResolver.openOutputStream(saved, "wt") ?: throw IOException(te.cantWrite)).use { it.write(bytes) }
            return SaveOutcome(true, te.savedAgain(outFileName))
        }
        val out = ep.out ?: return SaveOutcome(false, needOut)
        val path = (if (relOut.isEmpty()) "" else "$relOut/") + outFileName
        val of = TreeWriter(ctx, out).open(path, ep.policy()) ?: return SaveOutcome(false, te.skippedExists(outFileName))
        of.stream.use { it.write(bytes) }
        memo.savedUri = of.uri
        return SaveOutcome(true, te.savedTo(path))
    } catch (e: Exception) {
        return SaveOutcome(false, te.saveFail(e.message ?: e.javaClass.simpleName))
    }
}

// ======================= source: File / Folder =======================

@Stable
internal class SrcState(defaultExts: String) {
    /** 0 = File, 1 = Folder */
    var mode by mutableStateOf(0)
    var folderUri by mutableStateOf<Uri?>(null)
    var folderName by mutableStateOf<String?>(null)
    var folderRoot by mutableStateOf("")
    var rec by mutableStateOf(0)
    var extText by mutableStateOf(defaultExts)
    var all by mutableStateOf<List<MediaItem>>(emptyList())
    var scanning by mutableStateOf(false)
    var query by mutableStateOf("")
    var page by mutableStateOf(0)
}

internal class TeSource(val st: SrcState, val pickFile: () -> Unit, val pickFolder: () -> Unit)

private fun listTextFolder(ctx: Context, tree: Uri, recursive: Boolean, rootName: String): List<MediaItem> {
    val out = ArrayList<MediaItem>()
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
                if (isDir) {
                    if (recursive && name != "_backup") subs.add(Pair(id, name))
                } else {
                    out.add(MediaItem(DocumentsContract.buildDocumentUriUsingTree(tree, id), name, rel, size))
                }
            }
        }
        for ((id, name) in subs) walk(id, if (rel.isEmpty()) name else "$rel/$name")
    }
    walk(DocumentsContract.getTreeDocumentId(tree), rootName)
    return out
}

internal fun parseExtSet(text: String): Set<String> =
    text.split(',', ' ', ';', '\n').map { it.trim().lowercase().removePrefix(".").removePrefix("*.") }.filter { it.isNotEmpty() }.toSet()

/** Pick file/folder; [onOpenFile] is called when a file is chosen (File mode). The folder list is scanned automatically. */
@Composable
internal fun rememberTeSource(defaultExts: String, onOpenFile: (MediaItem) -> Unit): TeSource {
    val ctx = LocalContext.current
    val st = remember { SrcState(defaultExts) }
    val latest by rememberUpdatedState(onOpenFile)
    val fileL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            val (n, sz) = queryNameSize(ctx, u)
            latest(MediaItem(u, n, "", sz))
        }
    }
    LaunchedEffect(Unit) {
        PendingFile.take("textedit.plain", "textedit.struct")?.let { u ->
            val (n, sz) = queryNameSize(ctx, u)
            latest(MediaItem(u, n, "", sz))
        }
    }
    val folderL = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            } catch (e: Exception) { }
            val id = DocumentsContract.getTreeDocumentId(u)
            st.folderUri = u
            st.folderName = id.substringAfterLast(':').ifEmpty { "/" }
            st.folderRoot = id.substringAfterLast(':').substringAfterLast('/').ifEmpty { "folder" }
            st.page = 0
        }
    }
    LaunchedEffect(st.folderUri, st.rec) {
        val u = st.folderUri
        if (u == null) {
            st.all = emptyList()
        } else {
            st.scanning = true
            st.all = withContext(Dispatchers.IO) {
                try { listTextFolder(ctx, u, st.rec == 1, st.folderRoot) } catch (e: Exception) { emptyList() }
            }
            st.scanning = false
        }
    }
    return remember { TeSource(st, { fileL.launch(arrayOf("*/*")) }, { folderL.launch(null) }) }
}

@Composable
private fun TeFileRow(name: String, sub: String, onClick: () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, color = p.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private const val FILES_PER_PAGE = 50

/** Folder files that pass the extension filter and name search, ordered by path. */
internal fun teFiltered(st: SrcState): List<MediaItem> {
    val exts = parseExtSet(st.extText)
    val q = st.query.trim()
    return st.all.filter {
        val e = it.name.substringAfterLast('.', "").lowercase()
        (exts.isEmpty() || e in exts) && (q.isEmpty() || it.name.contains(q, ignoreCase = true))
    }.sortedBy { (it.rel + "/" + it.name).lowercase() }
}

/**
 * Source section for the File Text Editor screen: File/Folder choice, picker, extension filter, and the folder file list.
 * Tapping a file in the list calls [onOpen] together with its source folder.
 */
internal fun LazyListScope.teSourceItems(src: TeSource, te: TextEditStr, busy: Boolean, showModes: Boolean = true, onOpen: (MediaItem, Uri) -> Unit) {
    val st = src.st
    if (showModes) item { SegmentedChips(te.srcModes, st.mode, 14.sp) { if (!busy) st.mode = it } }
    item {
        if (st.mode == 0) {
            PickerCard(AppIcons.File, te.pickFile, null, AppIcons.FolderPlus) { if (!busy) src.pickFile() }
        } else {
            PickerCard(AppIcons.Folder, te.pickFolder, st.folderName, AppIcons.FolderPlus) { if (!busy) src.pickFolder() }
        }
    }
    if (st.mode == 1) {
        item { SegmentedChips(te.recOpts, st.rec, 14.sp) { if (!busy) st.rec = it } }
        item { AppTextField(value = st.extText, onValueChange = { st.extText = it; st.page = 0 }, label = te.extFilter) }
        item { Hint(te.extFilterHint) }
        if (st.folderUri != null) {
            val exts = parseExtSet(st.extText)
            val q = st.query.trim()
            val filtered = st.all.filter {
                val e = it.name.substringAfterLast('.', "").lowercase()
                (exts.isEmpty() || e in exts) && (q.isEmpty() || it.name.contains(q, ignoreCase = true))
            }.sortedBy { (it.rel + "/" + it.name).lowercase() }
            val pages = maxOf(1, (filtered.size + FILES_PER_PAGE - 1) / FILES_PER_PAGE)
            val page = st.page.coerceIn(0, pages - 1)
            val shown = filtered.drop(page * FILES_PER_PAGE).take(FILES_PER_PAGE)
            item { AppTextField(value = st.query, onValueChange = { st.query = it; st.page = 0 }, label = te.searchName) }
            item {
                Hint(if (st.scanning) te.scanning else if (filtered.isEmpty()) te.noFiles else te.filesFound(filtered.size))
            }
            item {
                LabelSection(null) {
                    if (filtered.size > FILES_PER_PAGE) PageNav(page, pages, onPage = { st.page = it })
            repeat(shown.size) { i ->
                val fi = shown[i]
                val rel = fi.rel.substringAfter('/', "")
                TeFileRow(fi.name, (if (rel.isEmpty()) "" else "$rel/ · ") + formatSize(fi.size)) {
                    val tree = st.folderUri
                    if (!busy && tree != null) onOpen(fi, tree)
                }
            }
                    if (filtered.size > FILES_PER_PAGE) PageNav(page, pages, onPage = { st.page = it })
                }
            }
        }
    }
}

internal fun formatSize(n: Long): String = when {
    n >= 1024L * 1024 -> String.format("%.1f MB", n / 1024.0 / 1024.0)
    n >= 1024 -> String.format("%.1f KB", n / 1024.0)
    else -> "$n B"
}

/** Read file contents with a size limit; throws [com.furuarc.core.TextTooBigException] if the limit is exceeded. */
internal fun readCapped(ctx: Context, uri: Uri, cap: Long): ByteArray {
    val ins = ctx.contentResolver.openInputStream(uri) ?: throw IOException("-")
    ins.use {
        val bos = java.io.ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        var total = 0L
        while (true) {
            val n = it.read(buf)
            if (n < 0) break
            total += n
            if (total > cap) throw com.furuarc.core.TextTooBigException(total)
            bos.write(buf, 0, n)
        }
        return bos.toByteArray()
    }
}

// ======================= text editor field (EditText) =======================

internal class HlSpan(color: Int) : ForegroundColorSpan(color)

/** Single thread (large stack) to compute syntax highlighting off the main thread. */
private val HL_EXEC: java.util.concurrent.ExecutorService = java.util.concurrent.Executors.newSingleThreadExecutor { r ->
    val t = Thread(null, r, "furuarc-highlight", 16L * 1024 * 1024)
    t.isDaemon = true
    t
}

internal class CodeEditText(c: Context) : EditText(c) {
    var onSel: ((Int, Int) -> Unit)? = null
    var showLines = false
    var showWs = false
    var gutterBg = 0
    var gutterFg = 0
    var digits = 2
    private val dens = c.resources.displayMetrics.density
    private val gPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cacheOff = 0
    private var cacheLine = 1

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSel?.invoke(selStart, selEnd)
    }

    fun resetLineCache() { cacheOff = 0; cacheLine = 1 }

    private fun gutterPx(): Int {
        if (!showLines) return 0
        gPaint.typeface = Typeface.MONOSPACE
        gPaint.textSize = this.textSize * 0.8f
        return (gPaint.measureText("0") * digits + 14f * dens).toInt()
    }

    fun applyPadding() {
        setPadding(gutterPx() + (10 * dens).toInt(), (8 * dens).toInt(), (10 * dens).toInt(), (8 * dens).toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!showLines && !showWs) return
        val lay = this.layout ?: return
        val txt = this.text ?: return
        try {
            val top = this.extendedPaddingTop
            val first = lay.getLineForVertical(scrollY - top).coerceAtLeast(0)
            val last = lay.getLineForVertical(scrollY + height - top).coerceAtMost(lay.lineCount - 1)
            if (showWs) drawWhitespace(canvas, lay, txt, first, last, top)
            if (showLines) drawGutter(canvas, lay, txt, first, last, top)
        } catch (e: Exception) {
            // extra drawing must not make the editor fail
        }
    }

    private fun drawWhitespace(canvas: Canvas, lay: Layout, txt: CharSequence, first: Int, last: Int, top: Int) {
        gPaint.color = gutterFg
        gPaint.alpha = 150
        gPaint.typeface = Typeface.MONOSPACE
        gPaint.textSize = this.textSize
        gPaint.textAlign = Paint.Align.LEFT
        val spaceW = gPaint.measureText(" ")
        val padL = this.compoundPaddingLeft.toFloat()
        var budget = 3000
        for (i in first..last) {
            val s = lay.getLineStart(i)
            val e = lay.getLineEnd(i)
            val base = (lay.getLineBaseline(i) + top).toFloat()
            var o = s
            while (o < e && budget > 0) {
                val ch = txt[o]
                if (ch == ' ' || ch == '\t' || ch == '\n') {
                    budget--
                    val x = padL + lay.getPrimaryHorizontal(o)
                    if (ch == ' ') canvas.drawText("·", x + spaceW * 0.25f, base, gPaint)
                    else if (ch == '\t') canvas.drawText("→", x, base, gPaint)
                    else canvas.drawText("¶", x, base, gPaint)
                }
                o++
            }
        }
        gPaint.alpha = 255
    }

    private fun drawGutter(canvas: Canvas, lay: Layout, txt: CharSequence, first: Int, last: Int, top: Int) {
        val gw = gutterPx().toFloat()
        gPaint.style = Paint.Style.FILL
        gPaint.color = gutterBg
        canvas.drawRect(scrollX.toFloat(), scrollY.toFloat(), scrollX + gw, (scrollY + height).toFloat(), gPaint)
        gPaint.color = gutterFg
        gPaint.typeface = Typeface.MONOSPACE
        gPaint.textSize = this.textSize * 0.8f
        gPaint.textAlign = Paint.Align.RIGHT
        val startOff = lay.getLineStart(first)
        var line: Int
        var from: Int
        if (startOff >= cacheOff) { line = cacheLine; from = cacheOff } else { line = 1; from = 0 }
        for (k in from until startOff) if (txt[k] == '\n') line++
        cacheOff = startOff
        cacheLine = line
        for (i in first..last) {
            val s = lay.getLineStart(i)
            val isStart = s == 0 || txt[s - 1] == '\n'
            if (i > first && isStart) line++
            if (isStart) canvas.drawText(line.toString(), scrollX + gw - 6f * dens, (lay.getLineBaseline(i) + top).toFloat(), gPaint)
        }
    }
}

/** Editor controller: the text lives in the EditText (not Compose state) so large files stay smooth. */
internal class EditorCtl {
    var et: CodeEditText? = null
    var pending: String? = null
    var programmatic = false

    fun text(): String = et?.text?.toString() ?: pending ?: ""

    fun setText(s: String, keepSel: Boolean = true) {
        val e = et
        if (e == null) { pending = s; return }
        val keep = if (keepSel) e.selectionStart.coerceAtLeast(0) else 0
        programmatic = true
        try {
            e.setText(s)
            e.setSelection(keep.coerceIn(0, s.length))
        } finally {
            programmatic = false
        }
    }

    fun select(a: Int, b: Int) {
        val e = et ?: return
        val len = e.text?.length ?: 0
        e.requestFocus()
        e.setSelection(a.coerceIn(0, len), b.coerceIn(0, len))
    }

    fun selection(): IntArray {
        val e = et ?: return intArrayOf(0, 0)
        val a = e.selectionStart.coerceAtLeast(0)
        val b = e.selectionEnd.coerceAtLeast(0)
        return intArrayOf(minOf(a, b), maxOf(a, b))
    }

    var kind: String? = null
    var hlOn = true
    var hlColors = IntArray(Highlight.ROLES)
    private var hlKey = ""

    /** Recompute the line-number column width (digit count) and redraw. */
    fun refreshGutter() {
        val e = et ?: return
        val cs = e.text ?: return
        if (e.showLines) {
            val d = maxOf(2, TxtDoc.lineCount(cs).toString().length)
            if (d != e.digits) e.digits = d
        }
        e.applyPadding()
        e.invalidate()
    }

    /** Called when a new EditText is created so highlighting is recomputed for it. */
    fun resetHl() { hlKey = "" }

    /** Set the type and colors; re-highlights only if something changed. */
    fun configureHl(kind: String?, on: Boolean, colors: IntArray) {
        val key = (kind ?: "-") + on + colors.joinToString(",")
        this.kind = kind; hlOn = on; hlColors = colors
        if (key != hlKey) { hlKey = key; rehighlight() }
    }

    private val hlGen = java.util.concurrent.atomic.AtomicInteger(0)

    private fun clearHl(ed: Editable) {
        try {
            for (sp in ed.getSpans(0, ed.length, HlSpan::class.java)) ed.removeSpan(sp)
        } catch (e: Exception) {
            // highlighting must not make the editor fail
        }
    }

    /**
     * Re-highlight. Color block computation runs on a separate thread (does not freeze the screen and cannot
     * crash the app if the regex runs out of stack); only applying the result happens on the main thread.
     */
    fun rehighlight() {
        val e = et ?: return
        val ed = e.text ?: return
        val k = kind
        val gen = hlGen.incrementAndGet()
        if (!hlOn || k == null || ed.length > Highlight.MAX_CHARS) { clearHl(ed); return }
        val snap = ed.toString()
        val colors = hlColors
        HL_EXEC.execute {
            if (gen != hlGen.get()) return@execute
            val spans = Highlight.spans(snap, k)
            e.post {
                if (gen != hlGen.get() || et !== e) return@post
                val cur = e.text ?: return@post
                try {
                    clearHl(cur)
                    val len = cur.length
                    for (sp in spans) {
                        val a = sp.start.coerceIn(0, len)
                        val b = sp.end.coerceIn(0, len)
                        if (b > a) cur.setSpan(HlSpan(colors[sp.role]), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                } catch (t: Throwable) {
                    // skip: text is shown without color
                }
            }
        }
    }

    fun insertAtCursor(s: String) {
        val e = et ?: return
        val a = minOf(e.selectionStart.coerceAtLeast(0), e.selectionEnd.coerceAtLeast(0))
        val b = maxOf(e.selectionStart.coerceAtLeast(0), e.selectionEnd.coerceAtLeast(0))
        e.text?.replace(a, b, s)
        e.setSelection((a + s.length).coerceAtMost(e.text?.length ?: 0))
    }

    /** Replace the range [a, b) with [s] through the Editable (recorded as a user change). */
    fun replaceRange(a: Int, b: Int, s: String) {
        val e = et ?: return
        e.text?.replace(a, b, s)
    }
}

/** Undo/redo history based on text copies (limited by count and total characters). */
internal class TextHistory {
    private val snaps = ArrayList<String>()
    private var idx = -1
    private var chars = 0L

    fun reset(s: String) {
        snaps.clear(); snaps.add(s); idx = 0; chars = s.length.toLong()
    }

    fun commit(s: String) {
        if (idx >= 0 && snaps[idx] == s) return
        while (snaps.size > idx + 1) chars -= snaps.removeAt(snaps.size - 1).length
        snaps.add(s); idx++; chars += s.length
        while ((snaps.size > 60 || chars > 80_000_000L) && snaps.size > 1) {
            chars -= snaps.removeAt(0).length; idx--
        }
    }

    fun undo(current: String): String? {
        commit(current)
        if (idx <= 0) return null
        idx--
        return snaps[idx]
    }

    fun redo(): String? {
        if (idx < 0 || idx >= snaps.size - 1) return null
        idx++
        return snaps[idx]
    }
}

@Composable
internal fun CodeEditor(
    ctl: EditorCtl,
    fontSp: Int,
    wrap: Boolean,
    modifier: Modifier,
    onChanged: () -> Unit,
    onSelection: (Int, Int) -> Unit,
    kind: String? = null,
    highlight: Boolean = true,
    showLines: Boolean = false,
    showWs: Boolean = false,
    readOnly: Boolean = false,
) {
    val p = LocalPalette.current
    val hlColors = intArrayOf(
        p.muted.toArgb(),
        (if (p.dark) 0xFF81C784 else 0xFF2E7D32).toInt(),
        (if (p.dark) 0xFFFFB74D else 0xFFE65100).toInt(),
        (if (p.dark) 0xFFCE93D8 else 0xFF7B1FA2).toInt(),
        p.accent.toArgb(),
        (if (p.dark) 0xFF4FC3F7 else 0xFF0277BD).toInt(),
    )
    val density = LocalDensity.current.density
    val accentArgb = p.accent.toArgb()
    val latestChanged by rememberUpdatedState(onChanged)
    val latestSel by rememberUpdatedState(onSelection)
    Box(modifier.border(1.dp, p.outline, RoundedCornerShape(ControlRadius))) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { c ->
                val et = CodeEditText(c)
                et.background = null
                et.typeface = Typeface.MONOSPACE
                et.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                et.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
                et.setSingleLine(false)
                et.gravity = Gravity.TOP or Gravity.START
                et.setPadding((10 * density).toInt(), (8 * density).toInt(), (10 * density).toInt(), (8 * density).toInt())
                et.isVerticalScrollBarEnabled = true
                // a finger swipe inside the editor scrolls its own text (not the page behind it) as long as it can still scroll
                et.setOnTouchListener { v, ev ->
                    when (ev.actionMasked) {
                        android.view.MotionEvent.ACTION_DOWN, android.view.MotionEvent.ACTION_MOVE ->
                            if (v.canScrollVertically(1) || v.canScrollVertically(-1) || v.canScrollHorizontally(1) || v.canScrollHorizontally(-1))
                                v.parent?.requestDisallowInterceptTouchEvent(true)
                        android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                et.highlightColor = (accentArgb and 0x00FFFFFF) or 0x59000000
                if (Build.VERSION.SDK_INT >= 29) {
                    val cursor = GradientDrawable()
                    cursor.setColor(accentArgb)
                    cursor.setSize((2 * density).toInt().coerceAtLeast(2), (20 * density).toInt())
                    et.textCursorDrawable = cursor
                }
                ctl.pending?.let {
                    ctl.programmatic = true
                    et.setText(it)
                    ctl.programmatic = false
                    ctl.pending = null
                }
                val hlRun = Runnable { ctl.rehighlight(); ctl.refreshGutter() }
                if (readOnly) {
                    et.keyListener = null
                    et.setTextIsSelectable(true)
                    et.showSoftInputOnFocus = false
                }
                et.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        et.resetLineCache()
                        et.removeCallbacks(hlRun)
                        et.postDelayed(hlRun, 400)
                        if (!ctl.programmatic) latestChanged()
                    }
                })
                et.onSel = { a, b -> latestSel(a, b) }
                ctl.et = et
                ctl.resetHl()
                et
            },
            update = { et ->
                et.setTextColor(p.text.toArgb())
                et.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSp.toFloat())
                et.setHorizontallyScrolling(!wrap)
                val gutterChanged = et.showLines != showLines
                et.showLines = showLines
                et.showWs = showWs
                et.gutterBg = p.background.toArgb()
                et.gutterFg = p.muted.toArgb()
                if (gutterChanged) ctl.refreshGutter() else et.invalidate()
                ctl.configureHl(kind, highlight, hlColors)
            },
        )
    }
}

// ======================= Find & Replace =======================

/** State of the Find & Replace panel for one editor. */
@Stable
internal class FindState(private val te: TextEditStr, private val ctl: EditorCtl) {
    var open by mutableStateOf(false)
    var q by mutableStateOf("")
    var rep by mutableStateOf("")
    var caseS by mutableStateOf(false)
    var useRe by mutableStateOf(false)
    var msg by mutableStateOf<String?>(null)

    private fun query() = TxtDoc.Query(q, caseS, useRe)

    fun step(forward: Boolean) {
        val query = query()
        if (TxtDoc.regexError(query) != null) { msg = te.badRegex; return }
        if (q.isEmpty()) { msg = null; return }
        val t = ctl.text()
        val sel = ctl.selection()
        val r = if (forward) TxtDoc.findNext(t, query, sel[1]) else TxtDoc.findPrev(t, query, sel[0])
        if (r == null) { msg = te.noMatch; return }
        ctl.select(r[0], r[1])
        msg = te.matchCount(TxtDoc.count(t, query))
    }

    fun replaceCurrent() {
        if (q.isEmpty()) return
        val query = query()
        if (TxtDoc.regexError(query) != null) { msg = te.badRegex; return }
        val t = ctl.text()
        val sel = ctl.selection()
        val hit = if (sel[1] > sel[0]) TxtDoc.findNext(t, query, sel[0]) else null
        if (hit != null && hit[0] == sel[0] && hit[1] == sel[1]) {
            val r = TxtDoc.replaceOne(t.substring(sel[0], sel[1]), query, rep)
            if (r == null) { msg = te.badRegex; return }
            ctl.replaceRange(sel[0], sel[1], r)
            val n = TxtDoc.findNext(ctl.text(), query, sel[0] + r.length)
            if (n != null) ctl.select(n[0], n[1])
            msg = null
        } else {
            step(true)
        }
    }

    /** true if the text changed. */
    fun replaceAll(): Boolean {
        if (q.isEmpty()) return false
        val query = query()
        val r = TxtDoc.replaceAll(ctl.text(), query, rep)
        if (r == null) { msg = te.badRegex; return false }
        if (r.second == 0) { msg = te.noMatch; return false }
        ctl.setText(r.first)
        msg = te.replacedCount(r.second)
        return true
    }
}

/** Find & Replace panel (opened/closed with the Find button). */
@Composable
internal fun FindPanel(te: TextEditStr, f: FindState, readOnly: Boolean = false, onReplaceAll: () -> Unit) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppTextField(value = f.q, onValueChange = { f.q = it; f.msg = null }, label = te.findLabel, mono = true)
        if (!readOnly) AppTextField(value = f.rep, onValueChange = { f.rep = it }, label = te.replaceLabel, mono = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { CheckRow(te.caseSensitive, f.caseS) { f.caseS = it } }
            Box(Modifier.weight(1f)) { CheckRow(te.useRegex, f.useRe) { f.useRe = it } }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton("‹") { f.step(false) } }
            Box(Modifier.weight(2.2f)) { SecondaryButton(te.nextBtn) { f.step(true) } }
            if (!readOnly) Box(Modifier.weight(2f)) { SecondaryButton(te.replaceBtn) { f.replaceCurrent() } }
            if (!readOnly) Box(Modifier.weight(2.4f)) { SecondaryButton(te.replaceAllBtn) { onReplaceAll() } }
        }
        val m = f.msg
        if (m != null) Text(m, color = p.muted, fontSize = 12.sp)
    }
}

/** Prepare bytes for saving: check for characters that cannot be encoded, then encode. Pair (bytes, error message). */
internal fun encodeForSave(te: TextEditStr, text: String, loaded: TxtDoc.Loaded, fmt: DocFormat): Pair<ByteArray?, String?> {
    val (enc, bom) = TxtDoc.resolveSave(fmt.saveEnc, loaded)
    val crlf = when (fmt.eol) { "lf" -> false; "crlf" -> true; else -> loaded.crlf }
    val bad = TxtDoc.badChars(text, enc)
    if (bad > 0) return Pair(null, te.unencodable(bad, encLabel(te, if (enc == "utf8" && bom) "utf8bom" else enc)))
    return Pair(TxtDoc.encode(text, enc, bom, crlf), null)
}

// ======================= symbol bar and XML check =======================

private val SYMBOLS = listOf("⇥", "{", "}", "[", "]", "(", ")", "\"", "'", ":", ";", "=", "<", ">", "/", "\\", "#", "*", "_", "-", "&", "|", "!", "?", ",", "`")

/** Row of symbol buttons above the keyboard; tap to insert at the cursor position (⇥ = Tab). */
@Composable
internal fun SymbolBar(ctl: EditorCtl) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (sym in SYMBOLS) {
            Box(
                Modifier.clip(RoundedCornerShape(8.dp)).background(p.card).border(1.dp, p.outline, RoundedCornerShape(8.dp))
                    .clickable { ctl.insertAtCursor(if (sym == "⇥") "\t" else sym) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(sym, color = p.text, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

/** null if the XML is well-formed; otherwise an error message with line and column. */
internal fun checkXml(text: String, te: TextEditStr): String? = try {
    val parser = android.util.Xml.newPullParser()
    parser.setInput(java.io.StringReader(text))
    while (parser.next() != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) { }
    null
} catch (e: org.xmlpull.v1.XmlPullParserException) {
    te.xmlBad(e.lineNumber, e.columnNumber, (e.message ?: "").substringBefore(" (position").substringBefore(" @"))
} catch (e: Exception) {
    te.xmlBad(0, 0, e.message ?: "")
}

/** Pretty-print XML (2-space indentation). Pair (result, error). Documents with a DOCTYPE are not reformatted to avoid data loss. */
internal fun tidyXml(text: String, te: TextEditStr): Pair<String?, String?> {
    if (text.contains("<!DOCTYPE")) return Pair(null, te.xmlTidyDoctype)
    return try {
        val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(org.xml.sax.InputSource(java.io.StringReader(text)))
        stripBlank(doc.documentElement)
        val tf = javax.xml.transform.TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes")
        tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        tf.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes")
        val sw = java.io.StringWriter()
        tf.transform(javax.xml.transform.dom.DOMSource(doc), javax.xml.transform.stream.StreamResult(sw))
        val decl = Regex("^\\s*<\\?xml[^>]*\\?>").find(text)?.value?.trim()
        val body = sw.toString().trim()
        Pair((if (decl != null) decl + "\n" else "") + body + "\n", null)
    } catch (e: Exception) {
        Pair(null, te.xmlTidyFail(e.message ?: e.javaClass.simpleName))
    }
}

private fun stripBlank(el: org.w3c.dom.Node) {
    val kids = el.childNodes
    var mixed = false
    for (i in 0 until kids.length) {
        val k = kids.item(i)
        if (k.nodeType == org.w3c.dom.Node.TEXT_NODE && (k.nodeValue ?: "").isNotBlank()) mixed = true
    }
    if (!mixed) {
        var i = kids.length - 1
        while (i >= 0) {
            val k = kids.item(i)
            if (k.nodeType == org.w3c.dom.Node.TEXT_NODE) el.removeChild(k)
            i--
        }
    }
    for (i in 0 until el.childNodes.length) {
        val k = el.childNodes.item(i)
        if (k.nodeType == org.w3c.dom.Node.ELEMENT_NODE) stripBlank(k)
    }
}

/** Auto-draft of unsaved changes (stored in the app's private folder). */
internal object Drafts {
    private fun file(ctx: Context, key: String): java.io.File {
        val dir = java.io.File(ctx.filesDir, "text_drafts")
        dir.mkdirs()
        return java.io.File(dir, Integer.toHexString(key.hashCode()) + "_" + key.length)
    }

    fun save(ctx: Context, key: String, text: String) {
        try { file(ctx, key).writeText(text, Charsets.UTF_8) } catch (e: Exception) { }
    }

    fun load(ctx: Context, key: String): String? = try {
        val f = file(ctx, key)
        if (f.isFile) f.readText(Charsets.UTF_8) else null
    } catch (e: Exception) {
        null
    }

    fun clear(ctx: Context, key: String) {
        try { file(ctx, key).delete() } catch (e: Exception) { }
    }
}

// ======================= Create File / Upload File (replaces File/Folder mode) =======================

/** Area title: accent text in the center with a line on the left and right ("Translation Area" style). */
@Composable
internal fun AreaTitle(text: String) = GroupTitle(text)

/** Fixed height of the Text Editor Area (the editor lives inside a scrollable page, not a new page). */
@Composable
internal fun editorAreaHeight(factor: Float = 0.6f): androidx.compose.ui.unit.Dp {
    val h = LocalConfiguration.current.screenHeightDp * factor
    return h.coerceIn(320f, 640f).dp
}

/** File title row under the area title: file name, marked with ● when there are unsaved changes. */
@Composable
internal fun AreaFileName(name: String, dirty: Boolean) {
    val p = LocalPalette.current
    Text(
        (if (dirty) "● " else "") + name, color = p.muted, fontSize = 13.sp, maxLines = 1,
        overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
    )
}

/** File picker that requests read AND write permission (needed for Overwrite Original File). */
internal class OpenDocumentRw : ActivityResultContract<Array<String>, Uri?>() {
    override fun createIntent(context: Context, input: Array<String>): android.content.Intent =
        android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(android.content.Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(android.content.Intent.EXTRA_MIME_TYPES, input)
            .addFlags(
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION,
            )

    override fun parseResult(resultCode: Int, intent: android.content.Intent?): Uri? =
        if (resultCode == android.app.Activity.RESULT_OK) intent?.data else null
}

@Composable
internal fun rememberRwFilePicker(onPicked: (Uri) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val latest by rememberUpdatedState(onPicked)
    val launcher = rememberLauncherForActivityResult(OpenDocumentRw()) { u ->
        if (u != null) {
            val rw = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            try {
                ctx.contentResolver.takePersistableUriPermission(u, rw)
            } catch (e: Exception) {
                try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e2: Exception) { }
            }
            latest(u)
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}

/**
 * State of the panel at the top of the File Text Editor screen: Create / Edit / View choice and its inputs.
 * [handoffKind] = which file waiting in [TextViewHandoff] ("Open with") belongs to this screen.
 */
@Stable
internal class TePanel(val exts: List<String>, val handoffKind: String = "plain") {
    /** 0 = Buat, 1 = Edit (upload a file to edit), 2 = Lihat (read-only) */
    var mode by mutableStateOf(0)
    /** The file shown in the Lihat mode. */
    var viewItem by mutableStateOf<MediaItem?>(null)
    var ext by mutableStateOf(exts.first())
    var base by mutableStateOf("text_file")
    var picked by mutableStateOf<MediaItem?>(null)

    fun pickedOk(): Boolean {
        val n = picked?.name ?: return false
        return n.substringAfterLast('.', "").lowercase() in exts
    }
}

/**
 * Output & Results for the File Text Editor. [canOverwrite] = show the Output Folder / Overwrite Original File choice (Upload File).
 * With Overwrite Original File, the backup of the original file is saved to the _backup folder inside the Output Folder.
 */
@Composable
internal fun EditorOutBlock(
    ep: EditorPrefs,
    te: TextEditStr,
    pickOut: () -> Unit,
    canOverwrite: Boolean,
    outFileName: String? = null,
    onOutFileName: (String) -> Unit = {},
) {
    val m = mediaStr(LocalLang.current)
    val inPlace = canOverwrite && ep.outMode == 1
    if (canOverwrite) SegmentedChips(m.outModes, ep.outMode, 14.sp) { ep.outMode = it }
    if (!inPlace) {
        if (canOverwrite) PickerCard(AppIcons.FolderUpload, m.outFolder, ep.outName, AppIcons.FolderPlus) { pickOut() }
        SectionLabel(m.conflict)
        SegmentedChips(m.conflictOpts, ep.pol, 14.sp) { ep.savePol(it) }
        if (!canOverwrite) PickerCard(AppIcons.FolderUpload, m.outFolder, ep.outName, AppIcons.FolderPlus) { pickOut() }
        if (outFileName != null) AppTextField(value = outFileName, onValueChange = onOutFileName, label = te.outFileName)
        if (ep.out == null) Hint(m.needOut)
    } else {
        CheckRow(m.backupCheck, ep.backup) { ep.saveBackup(it) }
        if (ep.backup) {
            PickerCard(AppIcons.FolderUpload, te.backupFolder, ep.outName, AppIcons.FolderPlus) { pickOut() }
            if (ep.out == null) Hint(m.needOut)
        }
        DetailHint(te.overwriteHowFile)
        if (!ep.backup) Text(m.backupWarn, color = WarnColor, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

/** Top panel: Create / Edit / View toggle, format dropdown, file name, Output & Results, buttons, and the "Lihat detail (?)" note. */
@Composable
internal fun TePanelUi(
    panel: TePanel,
    ep: EditorPrefs,
    te: TextEditStr,
    fold: FoldState,
    busy: Boolean,
    pickOut: () -> Unit,
    createNote: String?,
    onCreate: (String) -> Unit,
    onEdit: (MediaItem) -> Unit,
) {
    val ctx = LocalContext.current
    val m = mediaStr(LocalLang.current)
    val pickFile = rememberRwFilePicker { u ->
        val ns = queryNameSize(ctx, u)
        panel.picked = MediaItem(u, ns.first, "", ns.second)
    }
    val pickView = rememberRwFilePicker { u ->
        val ns = queryNameSize(ctx, u)
        panel.viewItem = MediaItem(u, ns.first, "", ns.second)
    }
    // a file handed over by "Open with" / "Find features by file" opens straight in the Lihat mode
    LaunchedEffect(panel, TextViewHandoff.token) {
        TextViewHandoff.take(panel.handoffKind)?.let { u ->
            val ns = queryNameSize(ctx, u)
            panel.viewItem = MediaItem(u, ns.first, "", ns.second)
            panel.mode = 2
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
        SegmentedChips(te.panelModes, panel.mode, 14.sp) { if (!busy) panel.mode = it }
        if (panel.mode == 0) {
            if (panel.exts.size > 1) {
                AppDropdown(te.fileFormat, panel.ext, panel.exts.map { it to ("." + it) }) { panel.ext = it }
            }
            FileNameField(panel.base, "." + panel.ext, te.createName) { panel.base = it }
            FoldSection(fold, "out", m.secOut) { EditorOutBlock(ep, te, pickOut, false) }
            PrimaryButton(te.createBtn, !busy && ep.out != null) {
                onCreate(panel.base.trim().ifEmpty { "text_file" } + "." + panel.ext)
            }
            DetailHint(te.createHow(createNote))
        } else if (panel.mode == 1) {
            PickerCard(AppIcons.File, te.uploadLabel, panel.picked?.name, AppIcons.FolderPlus) { if (!busy) pickFile() }
            if (panel.picked != null && !panel.pickedOk()) {
                Text(te.unsupportedFormat, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
            }
            FoldSection(fold, "out", m.secOut) { EditorOutBlock(ep, te, pickOut, true) }
            PrimaryButton(te.editBtn, !busy && panel.pickedOk()) { panel.picked?.let { onEdit(it) } }
            DetailHint(te.editHow)
        } else {
            // Lihat: only the upload field; the content appears below it, read-only, with Edit | Copy under it
            PickerCard(AppIcons.File, te.uploadLabel, panel.viewItem?.name, AppIcons.FolderPlus) { if (!busy) pickView() }
            val vi = panel.viewItem
            if (vi != null) TeViewPane(vi, te, ep) { panel.picked = vi; panel.mode = 1; if (panel.pickedOk()) onEdit(vi) }
            DetailHint(te.viewHow)
        }
    }
}

/** Initial content of a new file: JSON gets an empty object so it can be edited right away; other formats are empty. */
internal fun newFileContent(ext: String): String = if (ext == "json") "{}" else ""

internal class CreatedFile(val ok: Boolean, val msg: String, val uri: Uri?, val name: String)

/** Create a new file in the Output Folder (following "If the file already exists"). Called on the IO thread. */
internal fun createTextFile(ctx: Context, te: TextEditStr, ep: EditorPrefs, fileName: String, content: ByteArray, needOut: String): CreatedFile {
    try {
        val out = ep.out ?: return CreatedFile(false, needOut, null, fileName)
        val of = TreeWriter(ctx, out).open(fileName, ep.policy()) ?: return CreatedFile(false, te.skippedExists(fileName), null, fileName)
        of.stream.use { it.write(content) }
        val real = try { queryNameSize(ctx, of.uri).first } catch (e: Exception) { fileName }
        return CreatedFile(true, te.createdMsg(real), of.uri, real)
    } catch (e: Exception) {
        return CreatedFile(false, te.createFail(e.message ?: e.javaClass.simpleName), null, fileName)
    }
}

/**
 * Save the File Text Editor document. [created] = file created via Create File (always writes to the same file in the Output Folder).
 * Uploaded document: Overwrite Original File writes to the uploaded file (backup to _backup in the Output Folder), otherwise to the Output Folder.
 */
internal fun teSaveDoc(
    ctx: Context, te: TextEditStr, ep: EditorPrefs, item: MediaItem, created: Boolean,
    bytes: ByteArray, outFileName: String, memo: SaveMemo, needOut: String,
): SaveOutcome {
    try {
        if (!created && ep.outMode == 1) {
            if (ep.backup && !memo.backedUp) {
                val out = ep.out ?: return SaveOutcome(false, needOut)
                val bf = TreeWriter(ctx, out).open("_backup/" + item.name, ConflictPolicy.RENAME) ?: throw IOException("-")
                bf.stream.use { os ->
                    (ctx.contentResolver.openInputStream(item.uri) ?: throw IOException(te.cantReadOriginal)).use { it.copyTo(os, 256 * 1024) }
                }
                memo.backedUp = true
            }
            try {
                (ctx.contentResolver.openOutputStream(item.uri, "wt") ?: throw IOException(te.cantWrite)).use { it.write(bytes) }
            } catch (e: SecurityException) {
                return SaveOutcome(false, te.cantWriteOriginal)
            }
            return SaveOutcome(true, te.savedInPlace(item.name))
        }
        val saved = memo.savedUri
        if (saved != null) {
            (ctx.contentResolver.openOutputStream(saved, "wt") ?: throw IOException(te.cantWrite)).use { it.write(bytes) }
            return SaveOutcome(true, te.savedAgain(outFileName))
        }
        val out = ep.out ?: return SaveOutcome(false, needOut)
        val of = TreeWriter(ctx, out).open(outFileName, ep.policy()) ?: return SaveOutcome(false, te.skippedExists(outFileName))
        of.stream.use { it.write(bytes) }
        memo.savedUri = of.uri
        return SaveOutcome(true, te.savedTo(outFileName))
    } catch (e: Exception) {
        return SaveOutcome(false, te.saveFail(e.message ?: e.javaClass.simpleName))
    }
}

/** May press Save: Create File = the file already exists in the Output Folder; Overwrite Original File = no folder needed unless Backup is active. */
internal fun teCanSave(ep: EditorPrefs, created: Boolean, memo: SaveMemo): Boolean = when {
    created -> true
    ep.outMode == 1 -> !ep.backup || memo.backedUp || ep.out != null
    else -> ep.out != null || memo.savedUri != null
}
