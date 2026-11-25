package com.furuarc.ui

import androidx.compose.foundation.layout.PaddingValues
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import com.furuarc.core.MediaFormats
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.activity.compose.BackHandler
import com.furuarc.ui.theme.LocalStr
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MediaItem
import com.furuarc.MediaJobs
import com.furuarc.MediaTask
import com.furuarc.core.AudioConv
import com.furuarc.core.AudioOpts
import com.furuarc.core.ConflictPolicy
import com.furuarc.core.ImageConv
import com.furuarc.core.ImageOpts
import com.furuarc.core.SpecialConv
import com.furuarc.core.SpecialOpts
import com.furuarc.core.SpEngine
import com.furuarc.core.TextConv
import com.furuarc.core.TextOpts
import com.furuarc.core.VideoConv
import com.furuarc.core.VideoOpts
import com.furuarc.defaultOutFolder
import com.furuarc.i18n.mediaStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.furuarc.Engine
import com.furuarc.MediaPresets
import com.furuarc.core.MediaCtl
import com.furuarc.core.MixedConv
import com.furuarc.core.MEDIA_OK
import com.furuarc.core.MEDIA_SKIP
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import com.furuarc.i18n.MediaStr
import com.furuarc.overall
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.withContext

// ======================= Media Asset Converter menu =======================

@Composable
fun MediaMenuScreen(onAudio: () -> Unit, onImage: () -> Unit, onVideo: () -> Unit, onText: () -> Unit, onSpecial: () -> Unit, onMixed: () -> Unit, onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    MenuListScreen(
        m.menuTitle,
        listOf(
            MenuSpec(AppIcons.MusicNote, m.audioTitle, m.audioSub, onAudio),
            MenuSpec(AppIcons.Image, m.imageTitle, m.imageSub, onImage),
            MenuSpec(AppIcons.Videocam, m.videoTitle, m.videoSub, onVideo),
            MenuSpec(AppIcons.Layers, m.mixTitle, m.mixSub, onMixed),
            MenuSpec(AppIcons.Gamepad, m.specialTitle, m.specialSub, onSpecial),
            MenuSpec(AppIcons.TextFields, m.textTitle, m.textSub, onText),
        ),
        onBack,
    )
}

// ======================= shared parts =======================

private fun baseName(n: String): String = n.substringBeforeLast('.', n)

private fun parseExts(text: String): Set<String> =
    text.split(',', ';', ' ').map { it.trim().trimStart('.').lowercase() }.filter { it.isNotEmpty() }.toSet()

/** Folder contents as a list of source files; [recursive] includes subfolders. The relative path in [MediaItem.rel] is prefixed with [rootName] (the source folder name) so exported results land in "output/rootName/...". */
private fun listMediaFolder(ctx: Context, tree: Uri, recursive: Boolean, rootName: String): List<MediaItem> {
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

/** File types processed by the Mixed Asset Converter; [copyOthers] = unrecognized files are copied as is. */
private data class MixedFilter(val audio: Boolean, val image: Boolean, val video: Boolean, val copyOthers: Boolean) {
    fun allows(c: Int): Boolean = when (c) { 0 -> audio; 1 -> image; 2 -> video; else -> false }
}

private class MediaLock(val locked: Boolean, val onBlocked: () -> Unit)

private val LocalMediaLock = compositionLocalOf { MediaLock(false) { } }

/** While processing runs, all controls inside are untouchable; a tap only shows a message. */
@Composable
private fun LockBox(content: @Composable () -> Unit) {
    val lk = LocalMediaLock.current
    val cb by rememberUpdatedState(lk.onBlocked)
    Box(Modifier.fillMaxWidth()) {
        content()
        if (lk.locked) {
            Box(Modifier.matchParentSize().pointerInput(lk.locked) { detectTapGestures { cb() } })
        }
    }
}

private fun LazyListScope.litem(content: @Composable () -> Unit) {
    item { LockBox(content) }
}

private fun LazyListScope.litems(count: Int, content: @Composable (Int) -> Unit) {
    items(count) { i -> LockBox { content(i) } }
}

/** Open/closed state of each section (similar to the QnA menu in the Guide). */
private class Sections {
    val open = mutableStateMapOf<String, Boolean>()
}

/** Item inside a section panel (same lock rule as [litem]). */
@Composable
private fun pitem(content: @Composable () -> Unit) = LockBox(content)

@Composable
private fun pitems(count: Int, content: @Composable (Int) -> Unit) {
    for (i in 0 until count) LockBox { content(i) }
}

/**
 * A section that can be opened/closed, on its own label background (the same look as the label backgrounds of the settings).
 * The title row toggles it; the contents are only built when open.
 */
private fun LazyListScope.section(sec: Sections, key: String, title: String, defaultOpen: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val isOpen = sec.open[key] ?: defaultOpen
    litem {
        val p = LocalPalette.current
        RowPanel(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().clickable { sec.open[key] = !isOpen }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp, modifier = Modifier.weight(1f))
                    Text(if (isOpen) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
                }
                if (isOpen) Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { content() }
            }
        }
    }
}

/** The error message stays visible even when its section is closed, so the disabled Start button has an explanation. */
private fun LazyListScope.closedErrors(sec: Sections, key: String, vararg errs: String?) {
    if (sec.open[key] == false) {
        for (e in errs) if (e != null) litem { RedText(e) }
    }
}

private class SampleData(val item: MediaItem, val file: File)

private fun decodeScaled(open: () -> InputStream?, maxDim: Int): Pair<ImageBitmap, String>? {
    try {
        val b = BitmapFactory.Options()
        b.inJustDecodeBounds = true
        open()?.use { BitmapFactory.decodeStream(it, null, b) }
        if (b.outWidth <= 0 || b.outHeight <= 0) return null
        var sample = 1
        while (b.outWidth / (sample * 2) >= maxDim || b.outHeight / (sample * 2) >= maxDim) sample *= 2
        val o = BitmapFactory.Options()
        o.inSampleSize = sample
        val bmp = open()?.use { BitmapFactory.decodeStream(it, null, o) } ?: return null
        return Pair(bmp.asImageBitmap(), "${b.outWidth} × ${b.outHeight}")
    } catch (e: Exception) {
        return null
    }
}

/** Sample comparison: side-by-side images, or playable audio (source and result). */
@Composable
private fun SampleDialog(kind: Int, data: SampleData, onClose: () -> Unit) {
    val p = LocalPalette.current
    val m = mediaStr(LocalLang.current)
    val ctx = LocalContext.current
    val outSize = data.file.length()
    var srcBmp by remember { mutableStateOf<Pair<ImageBitmap, String>?>(null) }
    var outBmp by remember { mutableStateOf<Pair<ImageBitmap, String>?>(null) }
    var playing by remember { mutableStateOf(0) } // 0 = idle, 1 = source, 2 = result
    val player = remember { arrayOfNulls<MediaPlayer>(1) }
    fun stop() {
        try { player[0]?.release() } catch (e: Exception) { }
        player[0] = null
        playing = 0
    }
    fun play(which: Int) {
        stop()
        try {
            val mp = MediaPlayer()
            if (which == 1) mp.setDataSource(ctx, data.item.uri) else mp.setDataSource(data.file.absolutePath)
            mp.setOnCompletionListener { stop() }
            mp.setOnErrorListener { _, _, _ -> stop(); true }
            mp.setOnPreparedListener { it.start() }
            player[0] = mp
            playing = which
            mp.prepareAsync()
        } catch (e: Exception) {
            stop()
        }
    }
    DisposableEffect(Unit) { onDispose { stop() } }
    if (kind == 1) {
        LaunchedEffect(data) {
            withContext(Dispatchers.IO) {
                srcBmp = decodeScaled({ ctx.contentResolver.openInputStream(data.item.uri) }, 900)
                outBmp = decodeScaled({ FileInputStream(data.file) }, 900)
            }
        }
    }
    Dialog(onDismissRequest = { onClose() }) {
        RowPanel(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(m.sampleTitle(data.item.name), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    m.compare(fmtBytes(data.item.size), fmtBytes(outSize), m.changeText(if (data.item.size > 0L) (outSize - data.item.size) * 100.0 / data.item.size else 0.0)),
                    color = p.text, fontSize = 14.sp, lineHeight = 20.sp,
                )
                if (kind == 1) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for ((label, bm) in listOf(Pair(m.sampleSrc, srcBmp), Pair(m.sampleOut, outBmp))) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(label + (bm?.let { " · " + it.second } ?: ""), color = p.muted, fontSize = 12.sp)
                                Box(Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(8.dp)).background(p.iconBg), contentAlignment = Alignment.Center) {
                                    if (bm != null) Image(bm.first, null, Modifier.fillMaxWidth().height(170.dp), contentScale = ContentScale.Fit)
                                }
                            }
                        }
                    }
                } else {
                    for ((which, label) in listOf(Pair(1, m.sampleSrc), Pair(2, m.sampleOut))) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Box(Modifier.width(140.dp)) {
                                SecondaryButton(if (playing == which) m.sampleStop else m.samplePlay) { if (playing == which) stop() else play(which) }
                            }
                        }
                    }
                }
                PrimaryButton(m.sampleClose, true) { stop(); onClose() }
            }
        }
    }
}

/** Result row: name, size before/after, and a cancel button. */
@Composable
private fun ResultRow(m: MediaStr, r: com.furuarc.MediaResult, onUndo: () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(r.outName, color = p.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val ch = if (r.inBytes > 0L) m.changeText((r.outBytes - r.inBytes) * 100.0 / r.inBytes) else ""
                Text(fmtBytes(r.inBytes) + " → " + fmtBytes(r.outBytes) + (if (ch.isEmpty()) "" else " ($ch)"), color = p.muted, fontSize = 12.sp)
            }
            when (r.status) {
                0 -> if (r.canUndo) {
                    Box(Modifier.width(110.dp).padding(start = 8.dp)) { SecondaryButton(m.resUndo) { onUndo() } }
                } else {
                    Text(m.resNoUndo, color = p.muted, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
                }
                1 -> Text(m.resUndone, color = p.muted, fontSize = 13.sp, modifier = Modifier.padding(start = 8.dp))
                else -> Column(Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(m.resUndoFail, color = ErrColor, fontSize = 12.sp)
                    Box(Modifier.width(110.dp)) { SecondaryButton(m.resUndo) { onUndo() } }
                }
            }
        }
    }
}

/**
 * Skeleton of all converters: File/Folder buttons (File allows multiple), source choice, converter-specific [options],
 * rule for when the output file already exists, output folder, progress, and result summary.
 * [fixedExts] = extensions processed in Folder mode; null = the user fills them in (text converter).
 */
@Composable
private fun MediaScaffold(
    kind: Int,
    title: String,
    onBack: () -> Unit,
    fixedExts: Set<String>?,
    defaultTextExts: String,
    optionsValid: Boolean,
    makeTask: () -> MediaTask,
    onItems: (List<MediaItem>) -> Unit = {},
    onFolderMode: (Boolean) -> Unit = {},
    estimate: (List<MediaItem>) -> Long? = { null },
    extraEstimate: (List<MediaItem>) -> String? = { null },
    folderOnly: Boolean = false,
    mixed: MixedFilter? = null,
    onScan: (List<MediaItem>) -> Unit = {},
    onInPlace: (Boolean) -> Unit = {},
    options: LazyListScope.(Sections) -> Unit,
) {
    val p = LocalPalette.current
    val m = mediaStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val st by MediaJobs.state.collectAsState()
    val mine = st.kind == kind
    val running = mine && st.running
    val str = LocalStr.current
    var confirmBack by remember { mutableStateOf(false) }
    var confirmCancel by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val requestBack: () -> Unit = { if (running) confirmBack = true else onBack() }
    LaunchedEffect(running) { if (!running) { confirmBack = false; confirmCancel = false } }
    BackHandler(enabled = running) { confirmBack = true }
    var sampling by remember { mutableStateOf(false) }
    val lock = MediaLock(running || sampling) {
        android.widget.Toast.makeText(ctx, if (running) m.busyNote else m.sampleBusy, android.widget.Toast.LENGTH_SHORT).show()
    }
    val sec = remember { Sections() }
    var fmtSel by remember { mutableStateOf("") } // Input Format (Folder mode): "" = all available formats
    var outMode by remember { mutableStateOf(0) } // 0 = Output Folder, 1 = Overwrite Original Files (Folder mode)
    var backup by remember { mutableStateOf(true) }
    var resShown by remember { mutableStateOf(50) }
    var sample by remember { mutableStateOf<SampleData?>(null) }
    var sampleErr by remember { mutableStateOf<String?>(null) }
    var notifAsked by remember { mutableStateOf(false) }
    var showCompat by remember { mutableStateOf(false) }
    var ranInPlace by remember { mutableStateOf(false) }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    var srcMode by remember { mutableStateOf(if (folderOnly) 1 else 0) } // 0 = File, 1 = Folder
    var fileItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var folderUri by remember { mutableStateOf<Uri?>(null) }
    var folderName by remember { mutableStateOf<String?>(null) }
    var folderRoot by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var recIdx by remember { mutableStateOf(0) }
    var allFolderFiles by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var scanning by remember { mutableStateOf(false) }
    var extText by remember { mutableStateOf(defaultTextExts) }
    var polIdx by remember { mutableStateOf(0) }
    var out by remember { mutableStateOf<Uri?>(null) }
    var outName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (out == null) defaultOutFolder()?.let { out = it.first; outName = it.second }
    }

    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                fileItems = withContext(Dispatchers.IO) {
                    uris.map { u ->
                        val (n, sz) = queryNameSize(ctx, u)
                        MediaItem(u, n, "", sz)
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) {
        PendingFile.take("media.", "special:")?.let { u ->
            fileItems = withContext(Dispatchers.IO) { val (n, sz) = queryNameSize(ctx, u); listOf(MediaItem(u, n, "", sz)) }
        }
    }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            folderUri = u
            folderName = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
            folderRoot = DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').substringAfterLast('/').ifEmpty { "folder" }
            fmtSel = ""
        }
    }
    val pickOut = rememberOutPicker { u, n -> out = u; outName = n }

    LaunchedEffect(folderUri, recIdx) {
        val u = folderUri
        if (u == null) {
            allFolderFiles = emptyList()
        } else {
            scanning = true
            allFolderFiles = withContext(Dispatchers.IO) { try { listMediaFolder(ctx, u, recIdx == 1, folderRoot) } catch (e: Exception) { emptyList() } }
            scanning = false
        }
    }

    val extSet = remember(extText, fixedExts) { fixedExts ?: parseExts(extText) }
    val foundExts = remember(allFolderFiles, extSet) {
        allFolderFiles.map { it.name.substringAfterLast('.', "").lowercase() }.filter { extSet.contains(it) }.distinct().sorted()
    }
    val folderItems = remember(allFolderFiles, extSet, fmtSel, mixed) {
        allFolderFiles.filter {
            val e = it.name.substringAfterLast('.', "").lowercase()
            if (mixed != null) {
                val c = MixedConv.classOf(it.name)
                if (c < 0) mixed.copyOthers && fmtSel.isEmpty() else (fmtSel.isEmpty() || e == fmtSel) && mixed.allows(c)
            } else {
                extSet.contains(e) && (fmtSel.isEmpty() || e == fmtSel)
            }
        }
    }
    LaunchedEffect(allFolderFiles) { onScan(allFolderFiles) }
    val fmtMissing = srcMode == 1 && fixedExts != null && fmtSel.isNotEmpty() && folderUri != null && !scanning && !foundExts.contains(fmtSel)
    val items = if (srcMode == 0) fileItems else folderItems
    val totalSize = items.sumOf { it.size }
    LaunchedEffect(items) { onItems(items) }
    LaunchedEffect(srcMode) { onFolderMode(srcMode == 1) }
    LaunchedEffect(running) {
        while (running) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val inPlaceMode = outMode == 1 && srcMode == 1
    val inPlaceActive = inPlaceMode && folderUri != null
    LaunchedEffect(inPlaceMode) { onInPlace(inPlaceMode) }
    val makeSample: () -> Unit = {
        sampleErr = null
        scope.launch {
            sampling = true
            val task = makeTask()
            val item = items[0]
            val dir = File(ctx.cacheDir, "media_sample")
            dir.deleteRecursively()
            dir.mkdirs()
            val tmpd = File(ctx.cacheDir, "media_sample_tmp")
            tmpd.deleteRecursively()
            tmpd.mkdirs()
            val outFile = File(dir, task.outName(item.name))
            val err: String? = withContext(Dispatchers.IO) {
                try {
                    val ctl = MediaCtl(ctx, tmpd, { false }) { }
                    task.run(ctl, item) { FileOutputStream(outFile) }
                    null
                } catch (e: Throwable) {
                    e.message ?: e.javaClass.simpleName
                }
            }
            tmpd.deleteRecursively()
            sampling = false
            if (err != null) sampleErr = err else sample = SampleData(item, outFile)
        }
    }
    val est = if (items.isEmpty() || running) null else estimate(items)
    val larger = if (mine && st.finished) st.results.count { it.status == 0 && it.canUndo && it.outBytes > it.inBytes } else 0
    val selHint: String? = when {
        scanning -> m.scanning
        items.isNotEmpty() && srcMode == 1 -> {
            val counts = items.groupingBy { it.name.substringAfterLast('.', "").lowercase() }.eachCount().entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .joinToString(", ") { "${it.value} ${extLabel(it.key)}" }
            m.selectedFmt(items.size, fmtBytes(totalSize), counts)
        }
        items.isNotEmpty() -> m.selected(items.size, fmtBytes(totalSize))
        srcMode == 1 && folderUri != null -> m.folderEmpty
        else -> null
    }

    CompositionLocalProvider(LocalMediaLock provides lock) {
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(title, requestBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        if (!folderOnly) litem { SegmentedChips(m.srcModes, srcMode) { i -> if (!running) srcMode = i } }
        litem {
            if (srcMode == 0) {
                PickerCard(
                    icon = AppIcons.File,
                    label = m.pickFiles,
                    value = if (fileItems.isEmpty()) null else if (fileItems.size == 1) fileItems[0].name else "${fileItems[0].name} +${fileItems.size - 1}",
                    trailingIcon = AppIcons.FolderPlus,
                    onClick = { if (!running) pickFiles.launch(arrayOf("*/*")) },
                )
            } else {
                PickerCard(
                    icon = AppIcons.Folder,
                    label = m.pickFolder,
                    value = folderName,
                    trailingIcon = AppIcons.FolderPlus,
                    onClick = { if (!running) pickFolder.launch(null) },
                )
            }
        }
        if (srcMode == 1) {
            litem { SegmentedChips(m.recursive, recIdx, 14.sp) { i -> if (!running) recIdx = i } }
            if (fixedExts == null) {
                litem { AppTextField(value = extText, onValueChange = { extText = it }, label = m.extFilter) }
                litem { Hint(m.extFilterHint) }
            } else {
                litem {
                    AppDropdown(m.fmtInput, fmtSel, listOf("" to m.fmtAll) + fixedExts.map { it to extLabel(it) }.sortedBy { it.second }) { if (!running) fmtSel = it }
                }
                if (fmtMissing) litem { RedText(m.fmtMissing) }
                item { DetailHint(m.folderInputHint) }
            }
        }
        if (selHint != null) litem { Hint(selHint) }

        options(sec)

        section(sec, "out", m.secOut) {
            if (srcMode == 1) pitem { SegmentedChips(m.outModes, outMode, 14.sp) { outMode = it } }
            if (!inPlaceMode) {
                if (srcMode == 1) pitem { PickerCard(AppIcons.FolderUpload, m.outFolder, outName, AppIcons.FolderPlus) { pickOut() } }
                pitem { SectionLabel(m.conflict) }
                pitem { SegmentedChips(m.conflictOpts, polIdx, 14.sp) { i -> polIdx = i } }
                if (srcMode == 0) pitem { PickerCard(AppIcons.FolderUpload, m.outFolder, outName, AppIcons.FolderPlus) { pickOut() } }
            } else {
                pitem { CheckRow(m.backupCheck, backup) { backup = it } }
                pitem { DetailHint(m.overwriteHow) }
                if (!backup) pitem { Text(m.backupWarn, color = WarnColor, fontSize = 13.sp, lineHeight = 18.sp) }
            }
            if (srcMode == 1 && kind <= 1) {
                pitem {
                    SecondaryButton(m.sampleBtn, items.isNotEmpty() && optionsValid && !scanning && !MediaJobs.isRunning() && !sampling) { makeSample() }
                }
                pitem { DetailHint(m.sampleHint) }
            }
        }

        if (est != null && totalSize > 0L) {
            litem {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(m.estimate(fmtBytes(totalSize), fmtBytes(est), m.changeText((est - totalSize) * 100.0 / totalSize)), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                    Text(m.estimateNote, color = p.muted, fontSize = 12.sp)
                    if (est != totalSize) extraEstimate(items)?.let { DetailLink(it, 12.sp, 17.sp) }
                }
            }
        }

        if (sampling) litem { Hint(m.sampleMaking) }
        sampleErr?.let { e -> litem { RedText(m.sampleFail(e)) } }
        item {
            if (running) {
                SecondaryButton(m.cancel) { confirmCancel = true }
            } else {
                PrimaryButton(m.run, items.isNotEmpty() && (out != null || inPlaceActive) && !scanning && optionsValid && !MediaJobs.isRunning() && !sampling) {
                    // notification permission (Android 13+) is only requested when Background Processing is enabled in Settings
                    if (Engine.bgEnabled && !notifAsked && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                    ) {
                        notifAsked = true
                        try { notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) } catch (e: Exception) { }
                    }
                    sampleErr = null
                    resShown = 50
                    ranInPlace = inPlaceActive
                    val o = if (inPlaceActive) folderUri else out
                    if (o != null) {
                        val policy = if (inPlaceActive) ConflictPolicy.OVERWRITE else when (polIdx) { 1 -> ConflictPolicy.OVERWRITE; 2 -> ConflictPolicy.SKIP; else -> ConflictPolicy.RENAME }
                        MediaJobs.start(kind, items, o, policy, makeTask(), inPlaceActive, inPlaceActive, backup)
                    }
                }
            }
        }
        if (out == null && !inPlaceMode) litem { Hint(m.needOut) }
        if (running) {
            litem {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(m.progress(minOf(st.index + 1, st.total), st.total, st.currentName), color = p.text, fontSize = 14.sp, maxLines = 2)
                        val f = st.overall()
                        LinearProgressIndicator(f, Modifier.fillMaxWidth(), color = p.accent)
                        val pctI = (f * 100).toInt()
                        val elapsed = now - st.startedAt
                        val label = if (f >= 0.02f && elapsed > 2000L) {
                            m.eta(pctI, fmtDuration((elapsed * (1.0 - f) / f / 1000.0).toLong()))
                        } else {
                            "$pctI% · " + m.etaCalc()
                        }
                        Text(label, color = p.muted, fontSize = 13.sp)
                    }
                }
            }
        }
        if (mine && st.finished) {
            litem {
                SectionCard(m.resultTitle) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(if (st.cancelled) m.cancelledNote else m.doneNote, color = if (st.cancelled || st.failed > 0) WarnColor else OkColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(m.counts(st.ok, st.same, st.skipped, st.failed), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                        if (st.inBytes > 0L) {
                            Text(
                                m.compare(fmtBytes(st.inBytes), fmtBytes(st.outBytes), m.changeText((st.outBytes - st.inBytes) * 100.0 / st.inBytes)),
                                color = p.text, fontSize = 14.sp, lineHeight = 20.sp,
                            )
                        }
                        for (x in st.extStats) {
                            Text(
                                m.extStat(x.ext, x.count, fmtBytes(x.inBytes), fmtBytes(x.outBytes), m.changeText(if (x.inBytes > 0L) (x.outBytes - x.inBytes) * 100.0 / x.inBytes else 0.0)),
                                color = p.muted, fontSize = 13.sp, lineHeight = 18.sp,
                            )
                        }
                        if (st.errors.isNotEmpty()) {
                            Text(m.errorsTitle, color = p.muted, fontSize = 12.sp)
                            st.errors.take(20).forEach { e -> Text(e, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp) }
                            if (st.errors.size > 20) Text(m.moreErrors(st.errors.size - 20), color = p.muted, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        if (larger > 0) {
            litem { SecondaryButton(m.discardBtn(larger)) { confirmDiscard = true } }
            item { DetailHint(m.discardHint) }
        }
        if (mine && st.finished && st.results.isNotEmpty()) {
            litem { SecondaryButton(m.compatBtn) { showCompat = true } }
            val fu = folderUri
            if (!st.cancelled && ranInPlace && fu != null) {
                litem { SecondaryButton(m.repackBtn) { com.furuarc.Pipeline.onRepack?.invoke(fu) } }
                item { DetailHint(m.repackHint) }
            }
        }
        if (mine && st.finished && st.results.isNotEmpty()) {
            section(sec, "results", m.secResults(st.results.size)) {
                pitem { Hint(m.resUndoHint) }
                pitems(minOf(resShown, st.results.size)) { i ->
                    ResultRow(m, st.results[i]) { MediaJobs.undo(i) }
                }
                if (st.results.size > resShown) pitem { SecondaryButton(m.resMore) { resShown += 50 } }
            }
        }
        litem { Hint(m.bgNote) }
    }
}
    if (showCompat) CompatDialog(st.results) { showCompat = false }
    val sd = sample
    if (sd != null) {
        SampleDialog(kind, sd) { sd.file.delete(); sample = null }
    }
    if (confirmCancel) {
        ConfirmDialog(
            str.cancelTitle, m.cancelRunConfirm, str.cancelYes, str.cancelNo,
            onConfirm = { confirmCancel = false; MediaJobs.cancel() },
            onDismiss = { confirmCancel = false },
        )
    }
    if (confirmDiscard) {
        ConfirmDialog(
            m.discardTitle, m.discardConfirm(larger), m.discardYes, m.discardNo,
            onConfirm = { confirmDiscard = false; MediaJobs.discardLarger() },
            onDismiss = { confirmDiscard = false },
        )
    }
    if (confirmBack) {
        ConfirmDialog(
            str.cancelTitle, m.backConfirm, str.cancelYes, str.cancelNo,
            onConfirm = { confirmBack = false; MediaJobs.cancel(); onBack() },
            onDismiss = { confirmBack = false },
        )
    }
    }
}

private fun digits(s: String, max: Int): String = s.filter { it.isDigit() }.take(max)

private fun extOf(name: String): String = name.substringAfterLast('.', "").lowercase()

private fun fmtDuration(sec: Long): String {
    val h = sec / 3600
    val mi = (sec % 3600) / 60
    val se = sec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, mi, se) else "%02d:%02d".format(mi, se)
}

/** Compress settings. [mode] 0 = Quality (slider [pct]), 1 = Size ([kb]). [onlyOver] = only files > [thrKb] KB are compressed. */
private class Comp(val mode: Int, val pct: Int, val kb: Long, val onlyOver: Boolean, val thrKb: Long) {
    /** (target size in bytes or 0, quality percent) for a file of size [size]. */
    fun forItem(size: Long): Pair<Long, Int> {
        if (onlyOver && size <= thrKb * 1024L) return Pair(0L, 100)
        if (mode == 0) return Pair(0L, pct)
        val t = kb * 1024L
        return Pair(if (kb > 0L && size > 0L && t < size) t else 0L, 100)
    }
}

private fun estimateBytes(items: List<MediaItem>, c: Comp, ratio: (MediaItem, Int) -> Double): Long =
    items.sumOf { it ->
        val (t, q) = c.forItem(it.size)
        if (t > 0L) minOf(it.size, t) else (it.size * ratio(it, q)).toLong()
    }

/** True if the entered size (KB) exceeds the size of all source files (nothing can be compressed to that size). */
private fun sizeOverSource(kbText: String, items: List<MediaItem>): Boolean {
    val kb = kbText.toLongOrNull() ?: return false
    if (items.isEmpty()) return false
    return kb * 1024L > items.maxOf { it.size }
}

@Composable
private fun RedText(text: String) {
    Text(text, color = ErrColor, fontSize = 13.sp, lineHeight = 18.sp)
}

private fun sliderValueAt(x: Float, width: Float, thumb: Float, min: Int, max: Int): Int {
    val travel = (width - thumb).coerceAtLeast(1f)
    val f = ((x - thumb / 2f) / travel).coerceIn(0f, 1f)
    return (min + f * (max - min)).roundToInt()
}

/** Per-1% percent slider: label on the left, value on the right, thick rounded track, white thumb with an accent-colored border. */
@Composable
private fun PercentSlider(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
    val p = LocalPalette.current
    val cb by rememberUpdatedState(onChange)
    val thumb = 30.dp
    val trackH = 24.dp
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("$value%", color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(min, max) {
                    detectTapGestures { o -> cb(sliderValueAt(o.x, size.width.toFloat(), thumb.toPx(), min, max)) }
                }
                .pointerInput(min, max) {
                    detectHorizontalDragGestures(
                        onDragStart = { o -> cb(sliderValueAt(o.x, size.width.toFloat(), thumb.toPx(), min, max)) },
                    ) { change, _ -> cb(sliderValueAt(change.position.x, size.width.toFloat(), thumb.toPx(), min, max)) }
                },
        ) {
            val frac = ((value - min).toFloat() / (max - min)).coerceIn(0f, 1f)
            val travel = maxWidth - thumb
            Box(Modifier.align(Alignment.CenterStart).fillMaxWidth().height(trackH).clip(RoundedCornerShape(trackH / 2)).background(p.outline))
            Box(Modifier.align(Alignment.CenterStart).width(thumb / 2 + travel * frac).height(trackH).clip(RoundedCornerShape(trackH / 2)).background(p.accent))
            Box(
                Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = travel * frac)
                    .size(thumb)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(3.dp, p.accent, CircleShape),
            )
        }
    }
}

/** Checkbox with text on its right. */
@Composable
internal fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { onChange(it) }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
        Text(label, color = p.text, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
    }
}

private fun formatOptions(m: com.furuarc.i18n.MediaStr, formats: List<String>): List<Pair<String, String>> =
    listOf("0" to m.formatFollow) + formats.mapIndexed { i, f -> (i + 1).toString() to f }.sortedBy { it.second.lowercase() }

/** Format name for display: uppercase, except names that are genuinely mixed case (WebM, WebP). */
private fun extLabel(e: String): String = when (e.lowercase()) {
    "webm" -> "WebM"
    "webp" -> "WebP"
    else -> e.uppercase()
}

/**
 * "Compress ..." section: Quality/Size toggle. Quality = slider 1..100 (default 100 = no compression) that lowers quality;
 * Size = Estimated size (KB) field that targets the output size. Folder mode: the option only compresses files above the KB limit.
 */
private fun LazyListScope.compressBlock(
    m: com.furuarc.i18n.MediaStr,
    title: String,
    hint: String,
    mode: Int, onMode: (Int) -> Unit,
    pct: Int, onPct: (Int) -> Unit,
    kb: String, onKb: (String) -> Unit,
    unit: Int, onUnit: (Int) -> Unit,
    items: List<MediaItem>,
    isFolder: Boolean,
    onlyOver: Boolean, onOnlyOver: (Boolean) -> Unit,
    thr: String, onThr: (String) -> Unit,
) {
    litem {
    LabelSection(title) {
    run { SegmentedChips(m.compModes, mode, 14.sp) { onMode(it) } }
    if (mode == 0) {
        run { PercentSlider(m.compPercent, pct, 1, 100, onPct) }
    } else {
        val mult = if (unit == 1) 1024L else 1L
        val shown = if (kb.isEmpty()) "" else (kb.toLong() / mult).toString()
        run {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    AppTextField(
                        value = shown,
                        onValueChange = { v ->
                            val d = digits(v, 7)
                            onKb(if (d.isEmpty()) "" else (d.toLong() * mult).toString())
                        },
                        label = m.compSize, number = true,
                    )
                }
                Box(Modifier.weight(1f)) {
                    SegmentedChips(listOf("KB", "MB"), unit) { u ->
                        onUnit(u)
                        onKb(if (shown.isEmpty()) "" else (shown.toLong() * (if (u == 1) 1024L else 1L)).toString())
                    }
                }
            }
        }
        if (sizeOverSource(kb, items)) run { RedText(m.compOver) }
    }
    run { DetailHint(hint + " " + m.compHint) }
    if (isFolder) {
        run { CheckRow(m.onlyOver, onlyOver, onOnlyOver) }
        if (onlyOver) {
            run { AppTextField(value = thr, onValueChange = { onThr(digits(it, 9)) }, label = m.onlyOverKb, number = true) }
            run { DetailHint(m.onlyOverHint) }
        }
    }
    }
    }
}

// ======================= settings presets =======================

private fun Map<String, String>.int(k: String, d: Int): Int = this[k]?.toIntOrNull() ?: d

private fun Map<String, String>.str(k: String, d: String): String = this[k] ?: d

private fun Map<String, String>.flag(k: String, d: Boolean): Boolean = this[k]?.let { it == "1" } ?: d

private fun flagStr(b: Boolean): String = if (b) "1" else "0"

@Composable
private fun PresetBlock(
    kind: Int, m: MediaStr,
    builtins: List<Pair<String, Map<String, String>>>,
    snapshot: () -> Map<String, String>,
    restore: (Map<String, String>) -> Unit,
) {
    var ver by remember { mutableStateOf(0) }
    val users = remember(ver) { MediaPresets.list(kind) }
    val options = ArrayList<Pair<String, String>>()
    builtins.forEachIndexed { i, b -> options.add(Pair("b:$i", m.presetBuiltin(b.first))) }
    for (u in users) options.add(Pair("u:" + u.first, if (u.first == MediaPresets.LAST) m.presetLast else u.first))
    var sel by remember { mutableStateOf("") }
    var naming by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        AppDropdown(m.presetPick, sel, options) { sel = it }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) {
                SecondaryButton(m.presetApply, sel.isNotEmpty()) {
                    if (sel.startsWith("b:")) {
                        builtins.getOrNull(sel.substring(2).toIntOrNull() ?: -1)?.let { restore(it.second) }
                    } else if (sel.startsWith("u:")) {
                        val n = sel.substring(2)
                        users.firstOrNull { it.first == n }?.let { restore(MediaPresets.decode(it.second)) }
                    }
                }
            }
            Box(Modifier.weight(1f)) {
                SecondaryButton(m.presetDelete, sel.startsWith("u:") && sel != "u:" + MediaPresets.LAST) {
                    MediaPresets.delete(kind, sel.substring(2))
                    sel = ""
                    ver++
                }
            }
        }
        PrimaryButton(m.presetSave, true) { name = ""; naming = true }
        Hint(m.presetHint)
    }
    if (naming) {
        AppAlert(
            onDismissRequest = { naming = false },
            title = { DlgTitle(m.presetSave) },
            text = { AppTextField(value = name, onValueChange = { name = it.take(40) }, label = m.presetName) },
            confirmButton = {
                TextButton(onClick = {
                    val n = name.trim()
                    if (n.isNotEmpty() && n != MediaPresets.LAST) {
                        MediaPresets.save(kind, n, snapshot())
                        sel = "u:$n"
                        ver++
                        naming = false
                    }
                }) { Text(m.presetSaveBtn) }
            },
            dismissButton = { TextButton(onClick = { naming = false }) { Text(m.presetCancel) } },
        )
    }
}

private fun LazyListScope.presetSection(
    sec: Sections, kind: Int, m: MediaStr,
    builtins: List<Pair<String, Map<String, String>>>,
    snapshot: () -> Map<String, String>,
    restore: (Map<String, String>) -> Unit,
) {
    section(sec, "preset", m.secPreset) {
        pitem { PresetBlock(kind, m, builtins, snapshot, restore) }
    }
}

// ======================= Audio Asset Converter =======================

@Composable
fun AudioConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    var src by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isFolder by remember { mutableStateOf(false) }
    var fmtSel by remember { mutableStateOf(0) } // 0 = Follow Source, 1.. = M4A, OGG Opus, WAV, MP3, FLAC, OGG Vorbis
    var kbps by remember { mutableStateOf("0") }
    var rate by remember { mutableStateOf("0") }
    var ch by remember { mutableStateOf(0) }
    var vol by remember { mutableStateOf(100) }
    var norm by remember { mutableStateOf(0) }
    var trim by remember { mutableStateOf(false) }
    var keepLoop by remember { mutableStateOf(true) }
    var cMode by remember { mutableStateOf(0) }
    var cPct by remember { mutableStateOf(100) }
    var cKb by remember { mutableStateOf("") }
    var cUnit by remember { mutableStateOf(0) } // 0 = KB, 1 = MB (display of the Estimated size field)
    var onlyOver by remember { mutableStateOf(false) }
    var thr by remember { mutableStateOf("") }

    fun snapshot(): Map<String, String> = mapOf(
        "fmt" to fmtSel.toString(), "kbps" to kbps, "rate" to rate, "ch" to ch.toString(), "vol" to vol.toString(),
        "norm" to norm.toString(), "trim" to flagStr(trim), "loop" to flagStr(keepLoop),
        "cMode" to cMode.toString(), "cPct" to cPct.toString(), "cKb" to cKb, "only" to flagStr(onlyOver), "thr" to thr,
    )
    fun restore(r: Map<String, String>) {
        fmtSel = r.int("fmt", 0); kbps = r.str("kbps", "0"); rate = r.str("rate", "0"); ch = r.int("ch", 0); vol = r.int("vol", 100)
        norm = r.int("norm", 0); trim = r.flag("trim", false); keepLoop = r.flag("loop", true)
        cMode = r.int("cMode", 0); cPct = r.int("cPct", 100); cKb = r.str("cKb", ""); cUnit = 0; onlyOver = r.flag("only", false); thr = r.str("thr", "")
    }
    val builtins = listOf(
        Pair(m.presetAudio1, mapOf("fmt" to "1", "kbps" to "96")),
        Pair(m.presetAudio2, mapOf("fmt" to "2", "kbps" to "64", "ch" to "1")),
        Pair(m.presetAudioRpgmOgg, mapOf("fmt" to "2", "kbps" to "96")),
        Pair(m.presetAudioRpgmM4a, mapOf("fmt" to "1", "kbps" to "96")),
        Pair(m.presetAudioRenpy, mapOf("fmt" to "2", "kbps" to "128")),
        Pair(m.presetAudioKiri, mapOf("fmt" to "3", "rate" to "44100")),
    )

    fun fmtFor(name: String): Int? = if (fmtSel == 0) MediaFormats.audio(extOf(name)) else fmtSel - 1
    val formatBad = fmtSel == 0 && src.any { fmtFor(it.name) == null }
    val kbOver = cMode == 1 && sizeOverSource(cKb, src)
    val valid = !formatBad && !kbOver
    val comp = Comp(cMode, cPct, cKb.toLongOrNull() ?: 0L, onlyOver && isFolder, thr.toLongOrNull() ?: 0L)

    MediaScaffold(
        kind = 0, title = m.audioTitle, onBack = onBack, fixedExts = AudioConv.EXTS, defaultTextExts = "",
        optionsValid = valid,
        onItems = { src = it },
        onFolderMode = { isFolder = it },
        estimate = { list -> if (formatBad) null else estimateBytes(list, comp) { _, q -> q / 100.0 } },
        extraEstimate = { list -> if (fmtSel == 6 || (fmtSel == 0 && list.any { it.name.substringAfterLast('.', "").lowercase().let { e -> e == "ogg" || e == "oga" } })) m.vorbisFloorNote else null },
        makeTask = {
            MediaPresets.save(0, MediaPresets.LAST, snapshot())
            val kbpsV = kbps.toIntOrNull() ?: 0
            val rateV = rate.toIntOrNull() ?: 0
            MediaTask(
                { n -> baseName(n) + "." + (fmtFor(n)?.let { AudioConv.outExt(it) } ?: extOf(n)) },
            ) { ctl, item, sink ->
                val f = fmtFor(item.name) ?: throw java.io.IOException(m.formatNA)
                val (tb, q) = comp.forItem(item.size)
                AudioConv.convert(ctl, item.uri, AudioOpts(f, kbpsV, rateV, ch, vol, tb, q, norm, trim, keepLoop), sink)
            }
        },
    ) { sec ->
        litem { AppDropdown(m.audFormat, fmtSel.toString(), formatOptions(m, m.audFormats)) { fmtSel = it.toInt() } }
        if (formatBad) litem { RedText(m.formatNA) }
        compressBlock(m, m.audCompress, m.audCompressHint, cMode, { cMode = it }, cPct, { cPct = it }, cKb, { cKb = it }, cUnit, { cUnit = it }, src, isFolder, onlyOver, { onlyOver = it }, thr, { thr = it })
        section(sec, "audio", m.secAudio) {
            if (fmtSel != 3 && fmtSel != 5) {
                pitem { AppDropdown(m.audBitrate, kbps, listOf("0" to m.audBitrateAuto) + listOf("64", "96", "128", "160", "192", "256", "320").map { it to it }) { kbps = it } }
            }
            pitem {
                AppDropdown(
                    m.audRate, rate,
                    listOf("0" to m.audRateKeep, "22050" to "22050 Hz", "32000" to "32000 Hz", "44100" to "44100 Hz", "48000" to "48000 Hz"),
                ) { rate = it }
            }
            pitem { SegmentedChips(m.audChannels, ch, 14.sp) { ch = it } }
            pitem { PercentSlider(m.audVolume, vol, 1, 200) { vol = it } }
        }
        section(sec, "polish", m.secPolish) {
            pitem { SectionLabel(m.audNorm) }
            pitem { SegmentedChips(m.audNormOpts, norm, 14.sp) { norm = it } }
            if (norm > 0) pitem { DetailHint(m.audNormHint) }
            pitem { CheckRow(m.audTrim, trim) { trim = it } }
            if (trim) pitem { DetailHint(m.audTrimHint) }
            pitem { CheckRow(m.audKeepLoop, keepLoop) { keepLoop = it } }
            pitem { DetailHint(m.audLoopHint) }
        }
        presetSection(sec, 0, m, builtins, { snapshot() }, { restore(it) })
    }
}

// ======================= Image Asset Converter =======================

@Composable
fun ImageConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    val ctx = LocalContext.current
    var src by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isFolder by remember { mutableStateOf(false) }
    var fmtSel by remember { mutableStateOf(0) } // 0 = Follow Source, 1.. = PNG, JPG, WEBP
    var resize by remember { mutableStateOf(0) }
    var pct by remember { mutableStateOf(50) }
    var boxW by remember { mutableStateOf("") }
    var boxH by remember { mutableStateOf("") }
    var keepRatio by remember { mutableStateOf(true) }
    var mult by remember { mutableStateOf(0) }
    var rotate by remember { mutableStateOf(0) }
    var bg by remember { mutableStateOf(0) }
    var dither by remember { mutableStateOf(true) }
    var cMode by remember { mutableStateOf(0) }
    var cPct by remember { mutableStateOf(100) }
    var cKb by remember { mutableStateOf("") }
    var cUnit by remember { mutableStateOf(0) } // 0 = KB, 1 = MB (display of the Estimated size field)
    var onlyOver by remember { mutableStateOf(false) }
    var thr by remember { mutableStateOf("") }
    var dims by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    val multVals = intArrayOf(0, 2, 4, 8, 16, 24, 32, 48, 64)

    // source pixel size: used to auto-fill the Width/Height fields and check their limits
    LaunchedEffect(src, resize == 2) {
        dims = if (resize != 2) emptyList() else withContext(Dispatchers.IO) { src.mapNotNull { ImageConv.bounds(ctx, it.uri) } }
    }
    LaunchedEffect(dims) {
        if (dims.isNotEmpty() && boxW.isEmpty() && boxH.isEmpty()) {
            boxW = dims[0].first.toString()
            boxH = dims[0].second.toString()
        }
    }

    fun snapshot(): Map<String, String> = mapOf(
        "fmt" to fmtSel.toString(), "resize" to resize.toString(), "pct" to pct.toString(), "w" to boxW, "h" to boxH,
        "keep" to flagStr(keepRatio), "mult" to mult.toString(), "rot" to rotate.toString(), "bg" to bg.toString(),
        "dither" to flagStr(dither), "cMode" to cMode.toString(), "cPct" to cPct.toString(), "cKb" to cKb,
        "only" to flagStr(onlyOver), "thr" to thr,
    )
    fun restore(r: Map<String, String>) {
        fmtSel = r.int("fmt", 0); resize = r.int("resize", 0); pct = r.int("pct", 50); boxW = r.str("w", ""); boxH = r.str("h", "")
        keepRatio = r.flag("keep", true); mult = r.int("mult", 0); rotate = r.int("rot", 0); bg = r.int("bg", 0)
        dither = r.flag("dither", true); cMode = r.int("cMode", 0); cPct = r.int("cPct", 100); cKb = r.str("cKb", ""); cUnit = 0
        onlyOver = r.flag("only", false); thr = r.str("thr", "")
    }
    val builtins = listOf(
        Pair(m.presetImage1, mapOf("fmt" to "1", "cMode" to "0", "cPct" to "80")),
        Pair(m.presetImage2, mapOf("fmt" to "2", "cMode" to "0", "cPct" to "70")),
        Pair(m.presetImageTileset, mapOf("fmt" to "1", "resize" to "1", "pct" to "50", "mult" to "7")),
        Pair(m.presetImageRenpy, mapOf("fmt" to "3", "cMode" to "0", "cPct" to "85")),
        Pair(m.presetImageKiri, mapOf("fmt" to "1", "cMode" to "0", "cPct" to "100")),
    )

    fun fmtFor(name: String): Int? = if (fmtSel == 0) MediaFormats.image(extOf(name)) else fmtSel - 1
    val formatBad = fmtSel == 0 && src.any { fmtFor(it.name) == null }
    val bw = boxW.toIntOrNull() ?: 0
    val bh = boxH.toIntOrNull() ?: 0
    val sizeOver = resize == 2 && dims.isNotEmpty() && (bw > dims.maxOf { it.first } || bh > dims.maxOf { it.second })
    val kbOver = cMode == 1 && sizeOverSource(cKb, src)
    val valid = !formatBad && (resize != 2 || (bw > 0 || bh > 0)) && !sizeOver && !kbOver
    val comp = Comp(cMode, cPct, cKb.toLongOrNull() ?: 0L, onlyOver && isFolder, thr.toLongOrNull() ?: 0L)

    MediaScaffold(
        kind = 1, title = m.imageTitle, onBack = onBack, fixedExts = ImageConv.EXTS, defaultTextExts = "",
        optionsValid = valid,
        onItems = { src = it },
        onFolderMode = { isFolder = it },
        estimate = { list ->
            if (formatBad) null else {
                val scale = if (resize == 1) (pct / 100.0) * (pct / 100.0) else 1.0
                estimateBytes(list, comp) { item, q ->
                    val qf = q / 100.0
                    val r = if (fmtFor(item.name) == 0) 0.3 + 0.7 * qf else 0.1 + 0.9 * qf * qf
                    r * scale
                }
            }
        },
        makeTask = {
            MediaPresets.save(1, MediaPresets.LAST, snapshot())
            MediaTask(
                { n -> baseName(n) + "." + (fmtFor(n)?.let { ImageConv.outExt(it) } ?: extOf(n)) },
            ) { ctl, item, sink ->
                val f = fmtFor(item.name) ?: throw java.io.IOException(m.formatNA)
                val (tb, q) = comp.forItem(item.size)
                val o = ImageOpts(f, q, resize, pct, bw, bh, keepRatio, rotate * 90, bg == 0, tb, multVals[mult], dither)
                ImageConv.convert(ctl, item.uri, o, sink)
            }
        },
    ) { sec ->
        litem { AppDropdown(m.imgFormat, fmtSel.toString(), formatOptions(m, m.imgFormats)) { fmtSel = it.toInt() } }
        if (formatBad) litem { RedText(m.formatNA) }
        compressBlock(m, m.imgCompress, m.imgCompressHint, cMode, { cMode = it }, cPct, { cPct = it }, cKb, { cKb = it }, cUnit, { cUnit = it }, src, isFolder, onlyOver, { onlyOver = it }, thr, { thr = it })
        if (fmtSel == 0 || fmtSel == 1) {
            litem { CheckRow(m.imgDither, dither) { dither = it } }
            if (dither) item { DetailHint(m.imgDitherHint) }
        }
        section(sec, "image", m.secImage) {
            pitem { SectionLabel(m.imgResize) }
            pitem { SegmentedChips(m.imgResizeOpts, resize, 14.sp) { resize = it } }
            if (resize == 1) pitem { PercentSlider(m.imgPercent, pct, 1, 100) { pct = it } }
            if (resize == 2) {
                pitem {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            AppTextField(
                                value = boxW,
                                onValueChange = { v ->
                                    boxW = digits(v, 5)
                                    val d = dims.firstOrNull()
                                    val w = boxW.toIntOrNull()
                                    if (keepRatio && d != null && w != null) boxH = Math.round(w.toFloat() * d.second / d.first).toString()
                                },
                                label = m.imgWidth, number = true,
                            )
                        }
                        Text("x", color = LocalPalette.current.muted, fontSize = 16.sp)
                        Box(Modifier.weight(1f)) {
                            AppTextField(
                                value = boxH,
                                onValueChange = { v ->
                                    boxH = digits(v, 5)
                                    val d = dims.firstOrNull()
                                    val h = boxH.toIntOrNull()
                                    if (keepRatio && d != null && h != null) boxW = Math.round(h.toFloat() * d.first / d.second).toString()
                                },
                                label = m.imgHeight, number = true,
                            )
                        }
                    }
                }
                pitem { CheckRow(m.imgKeepRatio, keepRatio) { keepRatio = it } }
                if (sizeOver) pitem { RedText(m.compOver) }
            }
            if (resize != 0) {
                pitem { AppDropdown(m.imgMultiple, mult.toString(), m.imgMultipleOpts.mapIndexed { i, t -> Pair(i.toString(), t) }) { mult = it.toInt() } }
                pitem { DetailHint(m.imgMultipleHint) }
            }
            pitem { SectionLabel(m.imgRotate) }
            pitem { SegmentedChips(listOf("0°", "90°", "180°", "270°"), rotate, 14.sp) { rotate = it } }
            if (fmtSel == 0 || fmtSel == 2) {
                pitem { SectionLabel(m.imgBg) }
                pitem { SegmentedChips(m.imgBgOpts, bg, 14.sp) { bg = it } }
            }
            pitem { DetailHint(m.imgNote) }
        }
        closedErrors(sec, "image", if (sizeOver) m.compOver else null)
        presetSection(sec, 1, m, builtins, { snapshot() }, { restore(it) })
    }
}

// ======================= Video Asset Converter =======================

@Composable
fun VideoConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    var src by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isFolder by remember { mutableStateOf(false) }
    var fmtSel by remember { mutableStateOf(0) } // 0 = Follow Source, 1.. = MP4 H.264, MP4 H.265, WebM
    var res by remember { mutableStateOf("0") }
    var kbps by remember { mutableStateOf("0") }
    var fps by remember { mutableStateOf(0) }
    var audio by remember { mutableStateOf(0) }
    var cMode by remember { mutableStateOf(0) }
    var cPct by remember { mutableStateOf(100) }
    var cKb by remember { mutableStateOf("") }
    var cUnit by remember { mutableStateOf(0) } // 0 = KB, 1 = MB (display of the Estimated size field)
    var onlyOver by remember { mutableStateOf(false) }
    var thr by remember { mutableStateOf("") }
    val fpsVals = intArrayOf(0, 30, 24, 15)

    fun snapshot(): Map<String, String> = mapOf(
        "fmt" to fmtSel.toString(), "res" to res, "kbps" to kbps, "fps" to fps.toString(), "audio" to audio.toString(),
        "cMode" to cMode.toString(), "cPct" to cPct.toString(), "cKb" to cKb, "only" to flagStr(onlyOver), "thr" to thr,
    )
    fun restore(r: Map<String, String>) {
        fmtSel = r.int("fmt", 0); res = r.str("res", "0"); kbps = r.str("kbps", "0"); fps = r.int("fps", 0); audio = r.int("audio", 0)
        cMode = r.int("cMode", 0); cPct = r.int("cPct", 100); cKb = r.str("cKb", ""); cUnit = 0; onlyOver = r.flag("only", false); thr = r.str("thr", "")
    }
    val builtins = listOf(
        Pair(m.presetVideo1, mapOf("res" to "720", "cMode" to "0", "cPct" to "70")),
        Pair(m.presetVideoRpgmWebm, mapOf("fmt" to "3", "res" to "720", "cMode" to "0", "cPct" to "70")),
        Pair(m.presetVideoRpgmMp4, mapOf("fmt" to "1", "res" to "720", "cMode" to "0", "cPct" to "70")),
    )

    fun fmtFor(name: String): Int? = if (fmtSel == 0) MediaFormats.video(extOf(name)) else fmtSel - 1
    val formatBad = fmtSel == 0 && src.any { fmtFor(it.name) == null }
    val kbOver = cMode == 1 && sizeOverSource(cKb, src)
    val valid = !formatBad && !kbOver
    val comp = Comp(cMode, cPct, cKb.toLongOrNull() ?: 0L, onlyOver && isFolder, thr.toLongOrNull() ?: 0L)

    MediaScaffold(
        kind = 2, title = m.videoTitle, onBack = onBack, fixedExts = VideoConv.EXTS, defaultTextExts = "",
        optionsValid = valid,
        onItems = { src = it },
        onFolderMode = { isFolder = it },
        estimate = { list -> if (formatBad) null else estimateBytes(list, comp) { _, q -> q / 100.0 } },
        makeTask = {
            MediaPresets.save(2, MediaPresets.LAST, snapshot())
            MediaTask(
                { n -> baseName(n) + "." + (fmtFor(n)?.let { VideoConv.outExt(it) } ?: extOf(n)) },
            ) { ctl, item, sink ->
                val f = fmtFor(item.name) ?: throw java.io.IOException(m.formatNA)
                val (tb, q) = comp.forItem(item.size)
                val o = VideoOpts(f, res.toIntOrNull() ?: 0, kbps.toIntOrNull() ?: 0, fpsVals[fps], audio == 0, tb, q)
                VideoConv.convert(ctl, item.uri, o, sink)
            }
        },
    ) { sec ->
        litem { AppDropdown(m.vidFormat, fmtSel.toString(), formatOptions(m, m.vidFormats)) { fmtSel = it.toInt() } }
        if (formatBad) litem { RedText(m.formatNA) }
        item { DetailHint(m.vidFormatHint) }
        compressBlock(m, m.vidCompress, m.vidCompressHint, cMode, { cMode = it }, cPct, { cPct = it }, cKb, { cKb = it }, cUnit, { cUnit = it }, src, isFolder, onlyOver, { onlyOver = it }, thr, { thr = it })
        section(sec, "video", m.secVideo) {
            pitem {
                AppDropdown(
                    m.vidRes, res,
                    listOf("0" to m.vidResKeep, "1080" to "1080p", "720" to "720p", "480" to "480p", "360" to "360p", "240" to "240p"),
                ) { res = it }
            }
            pitem {
                AppDropdown(
                    m.vidBitrate, kbps,
                    listOf("0" to m.vidBitrateAuto) + listOf("500", "1000", "1500", "2000", "3000", "4000", "6000", "8000", "12000", "16000", "20000").map { it to "$it kbps" },
                ) { kbps = it }
            }
            pitem { SectionLabel(m.vidFps) }
            pitem { SegmentedChips(m.vidFpsOpts, fps, 14.sp) { fps = it } }
            pitem { SectionLabel(m.vidAudio) }
            pitem { SegmentedChips(m.vidAudioOpts, audio, 14.sp) { audio = it } }
            pitem { DetailHint(m.vidNote) }
        }
        presetSection(sec, 2, m, builtins, { snapshot() }, { restore(it) })
    }
}

// ======================= Mixed Asset Converter =======================

/** Section title per type: tap the title = open/close the rules, switch on the right = process this type or not. */
@Composable
private fun MixedHeader(title: String, note: String?, open: Boolean, enabled: Boolean, available: Boolean, onToggle: () -> Unit, onEnabled: (Boolean) -> Unit) {
    val p = LocalPalette.current
    val live = available && enabled
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clickable(enabled = live) { onToggle() }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, color = if (live) p.text else p.muted, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp)
                    if (note != null) Text(note, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                }
                if (live) Text(if (open) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(horizontal = 12.dp))
            }
            Switch(
                checked = live, onCheckedChange = { onEnabled(it) }, enabled = available,
                colors = SwitchDefaults.colors(checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.5f)),
            )
        }
    }
}

@Composable
fun MixedConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    val ctx = LocalContext.current
    var src by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var scan by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var inPlace by remember { mutableStateOf(false) }
    var onA by remember { mutableStateOf(true) }
    var onI by remember { mutableStateOf(true) }
    var onV by remember { mutableStateOf(true) }
    var copyOthers by remember { mutableStateOf(false) }
    // audio
    var aFmt by remember { mutableStateOf(0) }
    var aKbps by remember { mutableStateOf("0") }
    var aRate by remember { mutableStateOf("0") }
    var aCh by remember { mutableStateOf(0) }
    var aVol by remember { mutableStateOf(100) }
    var aNorm by remember { mutableStateOf(0) }
    var aTrim by remember { mutableStateOf(false) }
    var aLoop by remember { mutableStateOf(true) }
    var aMode by remember { mutableStateOf(0) }
    var aPct by remember { mutableStateOf(100) }
    var aKb by remember { mutableStateOf("") }
    var aUnit by remember { mutableStateOf(0) }
    var aOnly by remember { mutableStateOf(false) }
    var aThr by remember { mutableStateOf("") }
    // image
    var iFmt by remember { mutableStateOf(0) }
    var iResize by remember { mutableStateOf(0) }
    var iScale by remember { mutableStateOf(50) }
    var iMult by remember { mutableStateOf(0) }
    var iRot by remember { mutableStateOf(0) }
    var iBg by remember { mutableStateOf(0) }
    var iDither by remember { mutableStateOf(true) }
    var iMode by remember { mutableStateOf(0) }
    var iPct by remember { mutableStateOf(100) }
    var iKb by remember { mutableStateOf("") }
    var iUnit by remember { mutableStateOf(0) }
    var iOnly by remember { mutableStateOf(false) }
    var iThr by remember { mutableStateOf("") }
    // video
    var vFmt by remember { mutableStateOf(0) }
    var vRes by remember { mutableStateOf("0") }
    var vKbps by remember { mutableStateOf("0") }
    var vFps by remember { mutableStateOf(0) }
    var vAudio by remember { mutableStateOf(0) }
    var vMode by remember { mutableStateOf(0) }
    var vPct by remember { mutableStateOf(100) }
    var vKb by remember { mutableStateOf("") }
    var vUnit by remember { mutableStateOf(0) }
    var vOnly by remember { mutableStateOf(false) }
    var vThr by remember { mutableStateOf("") }
    val multVals = intArrayOf(0, 2, 4, 8, 16, 24, 32, 48, 64)
    val fpsVals = intArrayOf(0, 30, 24, 15)

    fun snapshot(): Map<String, String> = mapOf(
        "onA" to flagStr(onA), "onI" to flagStr(onI), "onV" to flagStr(onV), "copy" to flagStr(copyOthers),
        "a_fmt" to aFmt.toString(), "a_kbps" to aKbps, "a_rate" to aRate, "a_ch" to aCh.toString(), "a_vol" to aVol.toString(),
        "a_norm" to aNorm.toString(), "a_trim" to flagStr(aTrim), "a_loop" to flagStr(aLoop),
        "a_cMode" to aMode.toString(), "a_cPct" to aPct.toString(), "a_cKb" to aKb, "a_only" to flagStr(aOnly), "a_thr" to aThr,
        "i_fmt" to iFmt.toString(), "i_resize" to iResize.toString(), "i_scale" to iScale.toString(), "i_mult" to iMult.toString(),
        "i_rot" to iRot.toString(), "i_bg" to iBg.toString(), "i_dither" to flagStr(iDither),
        "i_cMode" to iMode.toString(), "i_cPct" to iPct.toString(), "i_cKb" to iKb, "i_only" to flagStr(iOnly), "i_thr" to iThr,
        "v_fmt" to vFmt.toString(), "v_res" to vRes, "v_kbps" to vKbps, "v_fps" to vFps.toString(), "v_audio" to vAudio.toString(),
        "v_cMode" to vMode.toString(), "v_cPct" to vPct.toString(), "v_cKb" to vKb, "v_only" to flagStr(vOnly), "v_thr" to vThr,
    )
    fun restore(r: Map<String, String>) {
        onA = r.flag("onA", true); onI = r.flag("onI", true); onV = r.flag("onV", true); copyOthers = r.flag("copy", false)
        aFmt = r.int("a_fmt", 0); aKbps = r.str("a_kbps", "0"); aRate = r.str("a_rate", "0"); aCh = r.int("a_ch", 0); aVol = r.int("a_vol", 100)
        aNorm = r.int("a_norm", 0); aTrim = r.flag("a_trim", false); aLoop = r.flag("a_loop", true)
        aMode = r.int("a_cMode", 0); aPct = r.int("a_cPct", 100); aKb = r.str("a_cKb", ""); aUnit = 0; aOnly = r.flag("a_only", false); aThr = r.str("a_thr", "")
        iFmt = r.int("i_fmt", 0); iResize = r.int("i_resize", 0).coerceIn(0, 1); iScale = r.int("i_scale", 50); iMult = r.int("i_mult", 0)
        iRot = r.int("i_rot", 0); iBg = r.int("i_bg", 0); iDither = r.flag("i_dither", true)
        iMode = r.int("i_cMode", 0); iPct = r.int("i_cPct", 100); iKb = r.str("i_cKb", ""); iUnit = 0; iOnly = r.flag("i_only", false); iThr = r.str("i_thr", "")
        vFmt = r.int("v_fmt", 0); vRes = r.str("v_res", "0"); vKbps = r.str("v_kbps", "0"); vFps = r.int("v_fps", 0); vAudio = r.int("v_audio", 0)
        vMode = r.int("v_cMode", 0); vPct = r.int("v_cPct", 100); vKb = r.str("v_cKb", ""); vUnit = 0; vOnly = r.flag("v_only", false); vThr = r.str("v_thr", "")
    }
    val builtins = listOf(
        Pair(m.presetMixRpgm, mapOf("a_fmt" to "2", "a_kbps" to "96", "i_fmt" to "1", "v_fmt" to "3", "v_res" to "720", "v_cMode" to "0", "v_cPct" to "70")),
        Pair(m.presetMixRenpy, mapOf("a_fmt" to "2", "a_kbps" to "128", "i_fmt" to "3", "i_cMode" to "0", "i_cPct" to "85", "v_fmt" to "3", "v_res" to "720", "v_cMode" to "0", "v_cPct" to "70")),
        Pair(m.presetMixKiri, mapOf("a_fmt" to "3", "a_rate" to "44100", "i_fmt" to "1", "i_cPct" to "100", "v_fmt" to "1", "v_res" to "720", "v_cMode" to "0", "v_cPct" to "70")),
    )

    fun aFor(name: String): Int? = if (aFmt == 0) MediaFormats.audio(extOf(name)) else aFmt - 1
    fun iFor(name: String): Int? = if (iFmt == 0) MediaFormats.image(extOf(name)) else iFmt - 1
    fun vFor(name: String): Int? = if (vFmt == 0) MediaFormats.video(extOf(name)) else vFmt - 1
    val la = src.filter { MixedConv.classOf(it.name) == 0 }
    val li = src.filter { MixedConv.classOf(it.name) == 1 }
    val lv = src.filter { MixedConv.classOf(it.name) == 2 }
    val lu = src.filter { MixedConv.classOf(it.name) < 0 }
    val stat = remember(scan) {
        val n = LongArray(4)
        val sz = LongArray(4)
        for (x in scan) {
            val c = MixedConv.classOf(x.name).let { if (it < 0) 3 else it }
            n[c]++
            sz[c] += x.size
        }
        Pair(n, sz)
    }
    val hasScan = scan.isNotEmpty()
    val availA = !hasScan || stat.first[0] > 0
    val availI = !hasScan || stat.first[1] > 0
    val availV = !hasScan || stat.first[2] > 0
    val actA = onA && availA
    val actI = onI && availI
    val actV = onV && availV
    val aBad = actA && aFmt == 0 && la.any { aFor(it.name) == null }
    val iBad = actI && iFmt == 0 && li.any { iFor(it.name) == null }
    val vBad = actV && vFmt == 0 && lv.any { vFor(it.name) == null }
    val kbBad = (actA && aMode == 1 && sizeOverSource(aKb, la)) || (actI && iMode == 1 && sizeOverSource(iKb, li)) || (actV && vMode == 1 && sizeOverSource(vKb, lv))
    val doCopy = copyOthers && !inPlace
    val valid = !aBad && !iBad && !vBad && !kbBad && (actA || actI || actV || doCopy)
    val compA = Comp(aMode, aPct, aKb.toLongOrNull() ?: 0L, aOnly, aThr.toLongOrNull() ?: 0L)
    val compI = Comp(iMode, iPct, iKb.toLongOrNull() ?: 0L, iOnly, iThr.toLongOrNull() ?: 0L)
    val compV = Comp(vMode, vPct, vKb.toLongOrNull() ?: 0L, vOnly, vThr.toLongOrNull() ?: 0L)

    fun estI(list: List<MediaItem>): Long {
        val scale = if (iResize == 1) (iScale / 100.0) * (iScale / 100.0) else 1.0
        return estimateBytes(list, compI) { item, q ->
            val qf = q / 100.0
            val r = if (iFor(item.name) == 0) 0.3 + 0.7 * qf else 0.1 + 0.9 * qf * qf
            r * scale
        }
    }

    MediaScaffold(
        kind = 5, title = m.mixTitle, onBack = onBack, fixedExts = MixedConv.EXTS, defaultTextExts = "",
        optionsValid = valid,
        folderOnly = true,
        mixed = MixedFilter(actA, actI, actV, doCopy),
        onItems = { src = it },
        onScan = { scan = it },
        onInPlace = { inPlace = it },
        estimate = { list ->
            if (aBad || iBad || vBad) null else {
                estimateBytes(list.filter { MixedConv.classOf(it.name) == 0 }, compA) { _, q -> q / 100.0 } +
                    estI(list.filter { MixedConv.classOf(it.name) == 1 }) +
                    estimateBytes(list.filter { MixedConv.classOf(it.name) == 2 }, compV) { _, q -> q / 100.0 } +
                    list.filter { MixedConv.classOf(it.name) < 0 }.sumOf { it.size }
            }
        },
        extraEstimate = { list ->
            val parts = ArrayList<String>()
            val xa = list.filter { MixedConv.classOf(it.name) == 0 }
            val xi = list.filter { MixedConv.classOf(it.name) == 1 }
            val xv = list.filter { MixedConv.classOf(it.name) == 2 }
            if (xa.isNotEmpty()) parts.add(m.mixEstLine(m.mixAudioName, xa.size, fmtBytes(xa.sumOf { it.size }), fmtBytes(estimateBytes(xa, compA) { _, q -> q / 100.0 })))
            if (xi.isNotEmpty()) parts.add(m.mixEstLine(m.mixImageName, xi.size, fmtBytes(xi.sumOf { it.size }), fmtBytes(estI(xi))))
            if (xv.isNotEmpty()) parts.add(m.mixEstLine(m.mixVideoName, xv.size, fmtBytes(xv.sumOf { it.size }), fmtBytes(estimateBytes(xv, compV) { _, q -> q / 100.0 })))
            if (aFmt == 6 && xa.isNotEmpty()) parts.add(m.vorbisFloorNote)
            if (parts.isEmpty()) null else parts.joinToString("\n")
        },
        makeTask = {
            MediaPresets.save(5, MediaPresets.LAST, snapshot())
            val kbpsA = aKbps.toIntOrNull() ?: 0
            val rateA = aRate.toIntOrNull() ?: 0
            val resV = vRes.toIntOrNull() ?: 0
            val kbpsV = vKbps.toIntOrNull() ?: 0
            val inPlaceNow = inPlace
            MediaTask({ n ->
                when (MixedConv.classOf(n)) {
                    0 -> baseName(n) + "." + (aFor(n)?.let { AudioConv.outExt(it) } ?: extOf(n))
                    1 -> baseName(n) + "." + (iFor(n)?.let { ImageConv.outExt(it) } ?: extOf(n))
                    2 -> baseName(n) + "." + (vFor(n)?.let { VideoConv.outExt(it) } ?: extOf(n))
                    else -> n
                }
            }) { ctl, item, sink ->
                when (MixedConv.classOf(item.name)) {
                    0 -> {
                        val f = aFor(item.name) ?: throw java.io.IOException(m.formatNA)
                        val (tb, q) = compA.forItem(item.size)
                        AudioConv.convert(ctl, item.uri, AudioOpts(f, kbpsA, rateA, aCh, aVol, tb, q, aNorm, aTrim, aLoop), sink)
                    }
                    1 -> {
                        val f = iFor(item.name) ?: throw java.io.IOException(m.formatNA)
                        val (tb, q) = compI.forItem(item.size)
                        ImageConv.convert(ctl, item.uri, ImageOpts(f, q, iResize, iScale, 0, 0, true, iRot * 90, iBg == 0, tb, multVals[iMult], iDither), sink)
                    }
                    2 -> {
                        val f = vFor(item.name) ?: throw java.io.IOException(m.formatNA)
                        val (tb, q) = compV.forItem(item.size)
                        // videos one at a time (hardware codec); waiting for a turn can still be cancelled
                        while (!MixedConv.videoGate.tryAcquire(300, java.util.concurrent.TimeUnit.MILLISECONDS)) ctl.check()
                        try {
                            VideoConv.convert(ctl, item.uri, VideoOpts(f, resV, kbpsV, fpsVals[vFps], vAudio == 0, tb, q), sink)
                        } finally {
                            MixedConv.videoGate.release()
                        }
                    }
                    else -> {
                        if (inPlaceNow) {
                            MEDIA_SKIP
                        } else {
                            val os = sink()
                            if (os == null) {
                                MEDIA_SKIP
                            } else {
                                val ins = ctx.contentResolver.openInputStream(item.uri) ?: throw java.io.IOException("Tidak bisa membaca file")
                                ins.use { i -> os.use { o -> i.copyTo(o, 256 * 1024) } }
                                ctl.progress(1f)
                                MEDIA_OK
                            }
                        }
                    }
                }
            }
        },
    ) { sec ->
        item { DetailHint(m.mixIntro) }
        if (hasScan) litem { Hint(m.mixScan(stat.first[0], fmtBytes(stat.second[0]), stat.first[1], fmtBytes(stat.second[1]), stat.first[2], fmtBytes(stat.second[2]), stat.first[3])) }
        if (!inPlace) {
            litem { CheckRow(m.mixCopyOthers, copyOthers) { copyOthers = it } }
            item { DetailHint(m.mixCopyHint) }
        }
        if (!(actA || actI || actV || doCopy)) litem { RedText(m.mixNothing) }

        // ---- Audio ----
        val aOpen = sec.open["mixA"] ?: false
        litem {
            MixedHeader(
                m.mixSec(m.mixAudioName, stat.first[0]).takeIf { hasScan } ?: m.mixAudioName,
                if (!availA) m.mixNoneNote else if (!onA) m.mixOffNote else null,
                aOpen, onA, availA, { sec.open["mixA"] = !aOpen }, { onA = it },
            )
        }
        if (!aOpen && aBad) litem { RedText(m.formatNA) }
        if (aOpen && actA) {
            litem { AppDropdown(m.audFormat, aFmt.toString(), formatOptions(m, m.audFormats)) { aFmt = it.toInt() } }
            if (aBad) litem { RedText(m.formatNA) }
            compressBlock(m, m.audCompress, m.audCompressHint, aMode, { aMode = it }, aPct, { aPct = it }, aKb, { aKb = it }, aUnit, { aUnit = it }, la, true, aOnly, { aOnly = it }, aThr, { aThr = it })
            section(sec, "mixAadv", m.secPolish) {
                if (aFmt != 3 && aFmt != 5) {
                    pitem { AppDropdown(m.audBitrate, aKbps, listOf("0" to m.audBitrateAuto) + listOf("64", "96", "128", "160", "192", "256", "320").map { it to it }) { aKbps = it } }
                }
                pitem {
                    AppDropdown(
                        m.audRate, aRate,
                        listOf("0" to m.audRateKeep, "22050" to "22050 Hz", "32000" to "32000 Hz", "44100" to "44100 Hz", "48000" to "48000 Hz"),
                    ) { aRate = it }
                }
                pitem { SegmentedChips(m.audChannels, aCh, 14.sp) { aCh = it } }
                pitem { PercentSlider(m.audVolume, aVol, 1, 200) { aVol = it } }
                pitem { SectionLabel(m.audNorm) }
                pitem { SegmentedChips(m.audNormOpts, aNorm, 14.sp) { aNorm = it } }
                if (aNorm > 0) pitem { DetailHint(m.audNormHint) }
                pitem { CheckRow(m.audTrim, aTrim) { aTrim = it } }
                if (aTrim) pitem { DetailHint(m.audTrimHint) }
                pitem { CheckRow(m.audKeepLoop, aLoop) { aLoop = it } }
                pitem { DetailHint(m.audLoopHint) }
            }
        }

        // ---- Image ----
        val iOpen = sec.open["mixI"] ?: false
        litem {
            MixedHeader(
                m.mixSec(m.mixImageName, stat.first[1]).takeIf { hasScan } ?: m.mixImageName,
                if (!availI) m.mixNoneNote else if (!onI) m.mixOffNote else null,
                iOpen, onI, availI, { sec.open["mixI"] = !iOpen }, { onI = it },
            )
        }
        if (!iOpen && iBad) litem { RedText(m.formatNA) }
        if (iOpen && actI) {
            litem { AppDropdown(m.imgFormat, iFmt.toString(), formatOptions(m, m.imgFormats)) { iFmt = it.toInt() } }
            if (iBad) litem { RedText(m.formatNA) }
            compressBlock(m, m.imgCompress, m.imgCompressHint, iMode, { iMode = it }, iPct, { iPct = it }, iKb, { iKb = it }, iUnit, { iUnit = it }, li, true, iOnly, { iOnly = it }, iThr, { iThr = it })
            if (iFmt == 0 || iFmt == 1) {
                litem { CheckRow(m.imgDither, iDither) { iDither = it } }
                if (iDither) item { DetailHint(m.imgDitherHint) }
            }
            section(sec, "mixIadv", m.secPolish) {
                pitem { SectionLabel(m.imgResize) }
                pitem { SegmentedChips(m.imgResizeOpts.take(2), iResize, 14.sp) { iResize = it } }
                if (iResize == 1) pitem { PercentSlider(m.imgPercent, iScale, 1, 100) { iScale = it } }
                if (iResize != 0) {
                    pitem { AppDropdown(m.imgMultiple, iMult.toString(), m.imgMultipleOpts.mapIndexed { i, t -> Pair(i.toString(), t) }) { iMult = it.toInt() } }
                    pitem { DetailHint(m.imgMultipleHint) }
                }
                pitem { SectionLabel(m.imgRotate) }
                pitem { SegmentedChips(listOf("0°", "90°", "180°", "270°"), iRot, 14.sp) { iRot = it } }
                if (iFmt == 0 || iFmt == 2) {
                    pitem { SectionLabel(m.imgBg) }
                    pitem { SegmentedChips(m.imgBgOpts, iBg, 14.sp) { iBg = it } }
                }
                pitem { DetailHint(m.imgNote) }
            }
        }

        // ---- Video ----
        val vOpen = sec.open["mixV"] ?: false
        litem {
            MixedHeader(
                m.mixSec(m.mixVideoName, stat.first[2]).takeIf { hasScan } ?: m.mixVideoName,
                if (!availV) m.mixNoneNote else if (!onV) m.mixOffNote else null,
                vOpen, onV, availV, { sec.open["mixV"] = !vOpen }, { onV = it },
            )
        }
        if (!vOpen && vBad) litem { RedText(m.formatNA) }
        if (vOpen && actV) {
            litem { AppDropdown(m.vidFormat, vFmt.toString(), formatOptions(m, m.vidFormats)) { vFmt = it.toInt() } }
            if (vBad) litem { RedText(m.formatNA) }
            item { DetailHint(m.vidFormatHint) }
            compressBlock(m, m.vidCompress, m.vidCompressHint, vMode, { vMode = it }, vPct, { vPct = it }, vKb, { vKb = it }, vUnit, { vUnit = it }, lv, true, vOnly, { vOnly = it }, vThr, { vThr = it })
            section(sec, "mixVadv", m.secPolish) {
                pitem {
                    AppDropdown(
                        m.vidRes, vRes,
                        listOf("0" to m.vidResKeep, "1080" to "1080p", "720" to "720p", "480" to "480p", "360" to "360p", "240" to "240p"),
                    ) { vRes = it }
                }
                pitem {
                    AppDropdown(
                        m.vidBitrate, vKbps,
                        listOf("0" to m.vidBitrateAuto) + listOf("500", "1000", "1500", "2000", "3000", "4000", "6000", "8000", "12000", "16000", "20000").map { it to "$it kbps" },
                    ) { vKbps = it }
                }
                pitem { SectionLabel(m.vidFps) }
                pitem { SegmentedChips(m.vidFpsOpts, vFps, 14.sp) { vFps = it } }
                pitem { SectionLabel(m.vidAudio) }
                pitem { SegmentedChips(m.vidAudioOpts, vAudio, 14.sp) { vAudio = it } }
                pitem { DetailHint(m.vidNote) }
            }
        }
        presetSection(sec, 5, m, builtins, { snapshot() }, { restore(it) })
    }
}

// ======================= Text Encoding Converter =======================

@Composable
fun TextConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    var dir by remember { mutableStateOf(0) } // 0 = to UTF-8, 1 = from UTF-8
    var cs by remember { mutableStateOf("windows-31j") }
    var bom by remember { mutableStateOf(0) }
    var nl by remember { mutableStateOf(0) }
    var lenient by remember { mutableStateOf(0) }
    val csOpts = listOf(
        "windows-31j" to "Shift-JIS (CP932)",
        "GBK" to "GBK (Mandarin sederhana)",
        "Big5" to "Big5 (Mandarin tradisional)",
        "EUC-KR" to "EUC-KR (Korea)",
        "EUC-JP" to "EUC-JP (Jepang)",
    )
    fun snapshot(): Map<String, String> = mapOf("dir" to dir.toString(), "cs" to cs, "bom" to bom.toString(), "nl" to nl.toString(), "len" to lenient.toString())
    fun restore(r: Map<String, String>) {
        dir = r.int("dir", 0); cs = r.str("cs", "windows-31j"); bom = r.int("bom", 0); nl = r.int("nl", 0); lenient = r.int("len", 0)
    }
    val builtins = listOf(Pair(m.presetText1, mapOf("dir" to "0", "cs" to "windows-31j")))

    MediaScaffold(
        kind = 3, title = m.textTitle, onBack = onBack, fixedExts = null,
        defaultTextExts = "txt, ks, tjs, ini, csv, srt, ass, lrc, log",
        optionsValid = true,
        makeTask = {
            MediaPresets.save(3, MediaPresets.LAST, snapshot())
            val o = TextOpts(dir == 0, cs, bom == 1, nl, lenient == 1)
            MediaTask({ n -> n }) { ctl, item, sink -> TextConv.convert(ctl, item.uri, o, sink) }
        },
    ) { sec ->
        section(sec, "text", m.secText, true) {
            pitem { SectionLabel(m.txtDirection) }
            pitem { SegmentedChips(m.txtDirections, dir, 14.sp) { dir = it } }
            pitem { AppDropdown(if (dir == 0) m.txtFrom else m.txtTo, cs, csOpts) { cs = it } }
            if (dir == 0) {
                pitem { SectionLabel(m.txtBom) }
                pitem { SegmentedChips(m.txtBomOpts, bom, 14.sp) { bom = it } }
            }
            pitem { SectionLabel(m.txtNewline) }
            pitem { SegmentedChips(m.txtNewlineOpts, nl, 14.sp) { nl = it } }
            pitem { SectionLabel(m.txtStrict) }
            pitem { SegmentedChips(m.txtStrictOpts, lenient, 14.sp) { lenient = it } }
            pitem { DetailHint(m.txtNote) }
        }
        presetSection(sec, 3, m, builtins, { snapshot() }, { restore(it) })
    }
}

// ======================= Special Asset Converter =======================

/** Engine currently open in the Special Asset Converter (null = engine list menu); also used by the system back button. */
object SpecialNav {
    var engine by mutableStateOf<SpEngine?>(null)
    /** Scroll of the engine list; kept here so it survives opening an engine and coming back. Reset when the whole screen is left. */
    var listScroll = androidx.compose.foundation.ScrollState(0)
}

@Composable
fun SpecialConvertScreen(onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    val eng = SpecialNav.engine
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { SpecialNav.listScroll = androidx.compose.foundation.ScrollState(0) } }
    if (eng == null) {
        MenuListScreen(
            m.specialTitle,
            SpecialConv.ENGINES.map { e -> MenuSpec(AppIcons.Gamepad, e.name, m.spEngineSub(e.id)) { MediaJobs.clear(); SpecialNav.engine = e } },
            onBack,
            SpecialNav.listScroll,
        )
    } else {
        androidx.compose.runtime.key(eng.id) { SpecialEngineScreen(eng) { MediaJobs.clear(); SpecialNav.engine = null } }
    }
}

@Composable
private fun SpecialEngineScreen(eng: SpEngine, onBack: () -> Unit) {
    val m = mediaStr(LocalLang.current)
    val ctx = LocalContext.current
    var src by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var audioOut by remember { mutableStateOf(0) } // 0 = automatic, 1 = always WAV, 2 = OGG
    var allFrames by remember { mutableStateOf(false) }
    var imgOut by remember { mutableStateOf(0) } // 0 = automatic, 1 = PNG, 2 = JPG
    val hasImage = eng.exts.any { it !in setOf("pcm", "nwa", "mio", "emi", "ovk") }
    val hasAudio = eng.exts.any { it == "pcm" || it == "nwa" || it == "mio" || it == "emi" }
    val hasFrames = eng.exts.any { it == "hg2" || it == "hg3" || it == "pna" }

    MediaScaffold(
        kind = 4, title = eng.name, onBack = onBack, fixedExts = eng.exts, defaultTextExts = "",
        optionsValid = true,
        onItems = { src = it },
        makeTask = {
            val o = SpecialOpts(audioOut, allFrames, imgOut)
            // companion files (base image, RC8 alpha, etc.) are looked up among the selected files, in the same folder
            val index = HashMap<String, MediaItem>()
            for (x in src) index[(x.rel + "/" + x.name).lowercase()] = x
            MediaTask(
                { n -> baseName(n) + ".out" },
                { item -> SpecialConv.outName(ctx, item, o) },
            ) { ctl, item, sink ->
                SpecialConv.convert(ctl, item, o, { from, name -> index[(from.rel + "/" + name).lowercase()] }, sink)
            }
        },
    ) { sec ->
        item { DetailHint(m.spEngineIntro(eng.id)) }
        if (hasImage) {
            litem { SectionLabel(m.spImageOut) }
            litem { SegmentedChips(m.spImageOpts, imgOut, 14.sp) { imgOut = it } }
        }
        if (hasAudio) {
            litem { SectionLabel(m.spAudioOutG) }
            litem { SegmentedChips(m.spAudioOpts3, audioOut, 14.sp) { audioOut = it } }
            item { DetailHint(m.spAudioHint3(eng.id)) }
        }
        if (hasFrames) {
            litem { CheckRow(m.spAllFrames, allFrames) { allFrames = it } }
            item { DetailHint(m.spAllFramesHint) }
        }
        item { DetailHint(m.spImageNote(eng.id)) }
    }
}
