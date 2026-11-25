package com.furuarc.ui

import com.furuarc.ui.theme.LocalShowEngineLabel
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.Surface
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.furuarc.core.CoverHit
import com.furuarc.core.CoverSearch
import com.furuarc.core.GameCache
import com.furuarc.core.GameEntry
import com.furuarc.core.GameLibrary
import com.furuarc.core.Thumbs
import com.furuarc.core.formatBytes
import com.furuarc.i18n.gameStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Image from a local file (cover) with memory cache. */
@Composable
fun FileImage(file: File?, modifier: Modifier, max: Int = 400) {
    val bmp by produceState<Bitmap?>(null, file?.path, file?.lastModified()) {
        value = if (file == null) null else withContext(Dispatchers.IO) {
            val k = "f|" + file.path + "|" + file.lastModified()
            Thumbs.get(k) ?: Thumbs.decodeScaled({ file.inputStream() }, max)?.also { Thumbs.put(k, it) }
        }
    }
    val b = bmp
    if (b != null) Image(b.asImageBitmap(), null, modifier, contentScale = ContentScale.Crop)
}

private fun toastMain(ctx: android.content.Context, msg: String) {
    Handler(Looper.getMainLooper()).post { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show() }
}

/** Game Assets tab: game list (3 columns, A–Z) with Add, Search, Refresh icons. */
@Composable
fun LibraryScreen(onOpen: (GameEntry) -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    remember { GameLibrary.load(); 0 }
    val all = GameLibrary.entries
    val busy = GameLibrary.busy

    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) } // 0 closed, 1 folder+name, 2 cover
    var tree by remember { mutableStateOf<Uri?>(null) }
    var treeName by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var nameEdited by remember { mutableStateOf(false) }
    var cover by remember { mutableStateOf<Uri?>(null) }
    var coverName by remember { mutableStateOf<String?>(null) }
    var dup by remember { mutableStateOf(false) }
    var renameFor by remember { mutableStateOf<GameEntry?>(null) }
    var renameText by remember { mutableStateOf("") }
    var coverFor by remember { mutableStateOf<GameEntry?>(null) }
    var removeFor by remember { mutableStateOf<GameEntry?>(null) }
    var coverPrev by remember { mutableStateOf<File?>(null) }
    var coverBusy by remember { mutableStateOf(false) }
    var coverMsg by remember { mutableStateOf<String?>(null) }
    var manualFor by remember { mutableStateOf<String?>(null) }
    var cloudAsk by remember { mutableStateOf(false) }
    var panel by remember { mutableStateOf(false) }
    var sortIdx by remember { mutableStateOf(0) }
    var shuffleSeed by remember { mutableStateOf(0) }
    var mainDlg by remember { mutableStateOf(false) }
    var mainTree by remember { mutableStateOf<Uri?>(null) }
    var mainName by remember { mutableStateOf<String?>(null) }
    var delAllAsk by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val cj = GameLibrary.coverJob
    val cres = GameLibrary.coverResult
    LaunchedEffect(cres) {
        if (cres != null) {
            toastMain(ctx, g.coverDone(cres.first, cres.second))
            GameLibrary.clearCoverResult()
        }
    }

    fun resetAdd() {
        step = 0; tree = null; treeName = null; name = ""; nameEdited = false; cover = null; coverName = null; dup = false
        coverPrev = null; coverBusy = false; coverMsg = null; manualFor = null
    }

    val pickTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            tree = uri
            dup = false
            val n = DocumentFile.fromTreeUri(ctx, uri)?.name ?: ""
            treeName = n.ifBlank { uri.lastPathSegment ?: "folder" }
            if (!nameEdited) name = n
        }
    }
    val pickMain = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            mainTree = uri
            val n = DocumentFile.fromTreeUri(ctx, uri)?.name ?: ""
            mainName = n.ifBlank { uri.lastPathSegment ?: "folder" }
        }
    }
    val pickCover = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            cover = uri
            coverPrev = null
            coverMsg = null
            coverName = DocumentFile.fromSingleUri(ctx, uri)?.name ?: "cover"
        }
    }

    val shown = remember(all, query, sortIdx, shuffleSeed) {
        val f = all.filter { query.isBlank() || it.name.contains(query.trim(), ignoreCase = true) }
        when (sortIdx) {
            1 -> f.sortedByDescending { it.name.lowercase() }
            2 -> f.reversed()
            3 -> f
            4 -> f.shuffled(java.util.Random(shuffleSeed.toLong()))
            else -> f.sortedBy { it.name.lowercase() }
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            val dark = p.background.luminance() < 0.5f
            Text(g.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else p.accent, modifier = Modifier.weight(1f))
            IconButton(onClick = { resetAdd(); step = 1 }, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Add, g.add, tint = p.text) }
            IconButton(onClick = { searching = !searching; if (!searching) query = "" }, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Search, g.search, tint = if (searching) p.accent else p.text) }
            IconButton(onClick = { panel = !panel }, modifier = Modifier.size(38.dp)) { Icon(AppIcons.Tune, g.settingsBtn, tint = if (panel) p.accent else p.text) }
            IconButton(onClick = { GameLibrary.refresh() }, enabled = !busy, modifier = Modifier.size(38.dp)) { Icon(Icons.Default.Refresh, g.refresh, tint = if (busy) p.muted else p.text) }
        }
        if (searching) {
            AppTextField(query, { query = it }, g.searchHint)
            Spacer(Modifier.height(12.dp))
        }
        if (panel) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppDropdown(g.sortLabel, sortIdx.toString(), g.libSortOpts.mapIndexed { i, l -> Pair(i.toString(), l) }) {
                    if (it == "4") shuffleSeed = (System.nanoTime() and 0x7fffffff).toInt()
                    sortIdx = it.toInt()
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val cloudOn = cj == null && all.isNotEmpty()
                    val delOn = all.isNotEmpty() && !busy
                    @Composable
                    fun Tool(icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String, on: Boolean, tint: Color, click: () -> Unit) {
                        Box(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(p.card).clickable(enabled = on) { click() }.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) { Icon(icon, desc, tint = if (on) tint else p.muted) }
                    }
                    Tool(AppIcons.Cloud, g.cloud, cloudOn, p.text) { cloudAsk = true }
                    Tool(AppIcons.Folder, g.addMain, !busy, p.text) { mainTree = null; mainName = null; mainDlg = true }
                    Tool(Icons.Default.Delete, g.deleteAll, delOn, Color(0xFFE57373)) { delAllAsk = true }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        if (cj != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.coverProg(cj.first + 1, cj.second, cj.third), color = p.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                TextButton(onClick = { GameLibrary.cancelCovers() }) { OneLineText(g.cancel, 13.sp, color = p.accent) }
            }
            BusyBar()
            Spacer(Modifier.height(10.dp))
        }
        if (busy) {
            Text(g.working, color = p.muted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            BusyBar()
            Spacer(Modifier.height(10.dp))
        }
        if (all.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                EmptyArt(AppIcons.Library, 132.dp)
                Spacer(Modifier.height(16.dp))
                Text(g.emptyTitle, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                Spacer(Modifier.height(6.dp))
                Text(g.emptySub, fontSize = 14.sp, color = p.muted, textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.width(200.dp)) { PrimaryButton(g.addFolder, true) { resetAdd(); step = 1 } }
            }
        } else if (shown.isEmpty()) {
            EmptyState(Icons.Default.Search, g.noResult, Modifier.padding(top = 24.dp).fillMaxWidth())
        } else {
            val rows = shown.chunked(3)
            LazyColumn(state = rememberLazyListState(), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 16.dp + LocalBarInset.current)) {
                items(rows) { row ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (e in row) {
                            GameCard(
                                e, Modifier.weight(1f),
                                onOpen = { onOpen(e) },
                                onRename = { renameText = e.name; renameFor = e },
                                onCover = { cover = null; coverName = null; coverPrev = null; coverMsg = null; coverFor = e },
                                onRemove = { removeFor = e },
                            )
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }

    fun runAuto(n: String) {
        coverBusy = true
        coverMsg = null
        scope.launch {
            val r = withContext(Dispatchers.IO) { try { CoverSearch.auto(n, "dlg") } catch (e: Exception) { null } }
            coverBusy = false
            if (r != null) {
                cover = Uri.fromFile(r.first)
                coverName = g.fromNet(r.second)
                coverPrev = r.first
            } else {
                coverMsg = g.autoNone
            }
        }
    }

    // ---- add dialog: step 1 (folder + name) ----
    if (step == 1) {
        AppDialog(
            g.dlgAddTitle, { resetAdd() },
            buttons = {
                TextButton(onClick = { resetAdd() }) { OneLineText(g.cancel, 14.sp, color = p.text) }
                TextButton(enabled = tree != null && name.isNotBlank(), onClick = {
                    val t = tree
                    if (t != null && GameLibrary.has(t)) dup = true else step = 2
                }) { OneLineText(g.next, 14.sp, FontWeight.Bold, if (tree != null && name.isNotBlank()) p.accent else p.muted) }
            },
        ) {
            PickerCard(AppIcons.Folder, g.dlgFolder, treeName, AppIcons.FolderUpload) { pickTree.launch(null) }
            AppTextField(name, { name = it; nameEdited = true }, g.dlgName)
            if (dup) Text(g.exists, color = Color(0xFFE57373), fontSize = 13.sp)
        }
    }
    // ---- step 2 (optional cover) ----
    if (step == 2) {
        AppDialog(
            g.coverTitle, { resetAdd() },
            buttons = {
                TextButton(onClick = { resetAdd() }) { OneLineText(g.cancel, 14.sp, color = p.text) }
                TextButton(enabled = !coverBusy, onClick = {
                    val t = tree
                    if (t != null) {
                        GameLibrary.add(name, t, cover) { err -> if (err != null) toastMain(ctx, g.loadFail(err)) }
                    }
                    resetAdd()
                }) { OneLineText(g.addBtn, 14.sp, FontWeight.Bold, p.accent) }
            },
        ) {
            CoverBody(name, coverName, coverPrev, coverBusy, coverMsg,
                onLocal = { pickCover.launch(arrayOf("image/*")) }, onAuto = { runAuto(name) }, onManual = { manualFor = name })
        }
    }
    // ---- rename ----
    renameFor?.let { e ->
        AlertDialog(
            onDismissRequest = { renameFor = null },
            shape = RoundedCornerShape(CardRadius), backgroundColor = p.card, contentColor = p.text,
            title = { DlgTitle(g.rename) },
            text = { AppTextField(renameText, { renameText = it }, g.dlgName) },
            confirmButton = {
                TextButton(enabled = renameText.isNotBlank(), onClick = { GameLibrary.rename(e.id, renameText); renameFor = null }) {
                    OneLineText(g.save, 14.sp, FontWeight.Bold, if (renameText.isNotBlank()) p.accent else p.muted)
                }
            },
            dismissButton = { TextButton(onClick = { renameFor = null }) { OneLineText(g.cancel, 14.sp, color = p.text) } },
        )
    }
    // ---- change cover ----
    coverFor?.let { e ->
        AppDialog(
            g.changeCover, { coverFor = null; coverPrev = null; coverMsg = null },
            buttons = {
                TextButton(onClick = { coverFor = null; coverPrev = null; coverMsg = null }) { OneLineText(g.cancel, 14.sp, color = p.text) }
                TextButton(enabled = cover != null && !coverBusy, onClick = { cover?.let { GameLibrary.setCover(e.id, it) }; coverFor = null; coverPrev = null; coverMsg = null }) {
                    OneLineText(g.save, 14.sp, FontWeight.Bold, if (cover != null) p.accent else p.muted)
                }
            },
        ) {
            CoverBody(e.name, coverName, coverPrev, coverBusy, coverMsg,
                onLocal = { pickCover.launch(arrayOf("image/*")) }, onAuto = { runAuto(e.name) }, onManual = { manualFor = e.name })
        }
    }
    // ---- manual internet search ----
    manualFor?.let { init ->
        ManualCoverDialog(
            init,
            onPick = { f, label ->
                cover = Uri.fromFile(f)
                coverName = g.fromNet(label)
                coverPrev = f
                coverMsg = null
                manualFor = null
            },
            onClose = { manualFor = null },
        )
    }
    // ---- search covers for all games ----
    if (cloudAsk) {
        AppDialog(
            g.cloud, { cloudAsk = false },
            buttons = { TextButton(onClick = { cloudAsk = false }) { OneLineText(g.cancel, 14.sp, color = p.text) } },
        ) {
            Text(g.cloudMsg, color = p.muted)
            SecondaryButton(g.cloudMissing) { cloudAsk = false; GameLibrary.autoCovers(true) }
            SecondaryButton(g.cloudAll) { cloudAsk = false; GameLibrary.autoCovers(false) }
        }
    }
    // ---- add main folder ----
    if (mainDlg) {
        AppDialog(
            g.addMain, { mainDlg = false },
            buttons = {
                TextButton(onClick = { mainDlg = false }) { OneLineText(g.cancel, 14.sp, color = p.text) }
                TextButton(enabled = mainTree != null, onClick = {
                    val t = mainTree
                    if (t != null) GameLibrary.addMain(t) { a, s -> toastMain(ctx, g.addMainDone(a, s)) }
                    mainDlg = false
                }) { OneLineText(g.addMainBtn, 14.sp, FontWeight.Bold, if (mainTree != null) p.accent else p.muted) }
            },
        ) {
            Text(g.addMainMsg, color = p.muted, fontSize = 13.sp)
            PickerCard(AppIcons.Folder, g.mainFolderLabel, mainName, AppIcons.FolderUpload) { pickMain.launch(null) }
        }
    }
    // ---- delete entire list ----
    if (delAllAsk) {
        ConfirmDialog(g.delAllTitle, g.delAllMsg, g.delAllYes, g.cancel, { GameLibrary.clearAll(); delAllAsk = false }, { delAllAsk = false })
    }
    // ---- delete folder (list entry only) ----
    removeFor?.let { e ->
        ConfirmDialog(g.removeTitle, g.removeMsg(e.name), g.removeYes, g.cancel, { GameLibrary.remove(e.id); removeFor = null }, { removeFor = null })
    }
}

/** Cover dialog contents: local upload, automatic / manual internet search, and preview. */
@Composable
private fun CoverBody(
    name: String, coverName: String?, preview: File?, busy: Boolean, msg: String?,
    onLocal: () -> Unit, onAuto: () -> Unit, onManual: () -> Unit,
) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PickerCard(AppIcons.Image, g.dlgCover, coverName, AppIcons.FolderUpload, onLocal)
        if (preview != null) {
            FileImage(preview, Modifier.height(150.dp).aspectRatio(0.7f).clip(RoundedCornerShape(10.dp)).align(Alignment.CenterHorizontally))
        }
        Text(g.coverNetTitle, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton(g.autoBtn, !busy && name.isNotBlank(), onAuto) }
            Box(Modifier.weight(1f)) { SecondaryButton(g.manualBtn, !busy, onManual) }
        }
        if (busy) {
            Text(g.searching, color = p.muted, fontSize = 13.sp)
            BusyBar()
        }
        if (msg != null) Text(msg, color = Color(0xFFE57373), fontSize = 13.sp)
        Text(g.dlgCoverHint, color = p.muted, fontSize = 13.sp)
    }
}

/** "Search manually on the internet" dialog: search field, source dropdown, and max. 6 results (3 columns x 2 rows). */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun ManualCoverDialog(initial: String, onPick: (File, String) -> Unit, onClose: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    var q by remember { mutableStateOf(initial) }
    var src by remember { mutableStateOf(CoverSearch.VNDB) }
    var hits by remember { mutableStateOf<List<CoverHit>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    var picking by remember { mutableStateOf(false) }
    fun run() {
        loading = true; msg = null; hits = emptyList()
        val s = src
        val query = q
        scope.launch {
            try {
                val r = withContext(Dispatchers.IO) { CoverSearch.search(s, query, 6) }
                hits = r
                if (r.isEmpty()) msg = g.noHits
            } catch (e: Exception) {
                msg = g.netFail(e.message ?: "")
            }
            loading = false
        }
    }
    LaunchedEffect(Unit) { run() }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(CardRadius), color = p.card, contentColor = p.text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp),
        ) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(g.manualTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.text)
                AppTextField(q, { q = it }, g.search, trailing = {
                    IconButton(onClick = { run() }, enabled = !loading && q.isNotBlank()) { Icon(Icons.Default.Search, g.search, tint = if (!loading && q.isNotBlank()) p.accent else p.muted) }
                })
                AppDropdown(g.sourceLabel, src, CoverSearch.SOURCES.map { Pair(it, CoverSearch.label(it)) }) { src = it; run() }
                if (loading || picking) {
                    Text(if (picking) g.loadingMedia else g.searching, color = p.muted, fontSize = 13.sp)
                    BusyBar()
                }
                if (msg != null) Text(msg ?: "", color = Color(0xFFE57373), fontSize = 13.sp)
                for (row in hits.chunked(3)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (h in row) {
                            HitCell(h, Modifier.weight(1f)) {
                                if (!picking) {
                                    picking = true
                                    scope.launch {
                                        val f = CoverSearch.tmpFile("dlg.img")
                                        val ok = withContext(Dispatchers.IO) { try { CoverSearch.download(h, f) } catch (e: Exception) { false } }
                                        picking = false
                                        if (ok) onPick(f, CoverSearch.label(h.source)) else msg = g.dlFail
                                    }
                                }
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onClose) { OneLineText(g.cancel, 14.sp, color = p.text) }
                }
            }
        }
    }
}

@Composable
private fun HitCell(h: CoverHit, modifier: Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    val bmp by produceState<Bitmap?>(null, h.thumb) {
        val k = "n|" + h.thumb
        value = Thumbs.get(k) ?: withContext(Dispatchers.IO) {
            try { CoverSearch.thumbBitmap(h, 300)?.also { Thumbs.put(k, it) } } catch (e: Exception) { null }
        }
    }
    Box(modifier.staggeredEntrance().scrollJelly().clickableScale(pressed = 0.92f, ripple = false, onClick = onClick).aspectRatio(0.66f).clip(RoundedCornerShape(8.dp)).background(p.iconBg), contentAlignment = Alignment.BottomCenter) {
        val b = bmp
        if (b != null) Image(b.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        if (h.title.isNotEmpty()) {
            Text(h.title, color = Color.White, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().background(Color(0x99000000)).padding(horizontal = 4.dp, vertical = 2.dp))
        }
    }
}

@Composable
private fun GameCard(e: GameEntry, modifier: Modifier, onOpen: () -> Unit, onRename: () -> Unit, onCover: () -> Unit, onRemove: () -> Unit) {
    val p = LocalPalette.current
    val g = gameStr(LocalLang.current)
    val shape = RoundedCornerShape(CardRadius)
    var menu by remember { mutableStateOf(false) }
    // the entrance, jelly and press animations sit on the outermost node, so the cover, its shadow and the name move together
    Column(modifier.staggeredEntrance().scrollJelly().clickableScale(pressed = 0.92f, ripple = false, onClick = onOpen), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(0.75f).cardShadow(CardRadius).clip(shape)
                .background(Brush.verticalGradient(listOf(lerp(p.card, p.accent, 0.22f), lerp(p.card, p.accent, 0.05f)))),
            contentAlignment = Alignment.Center,
        ) {
            val f = GameLibrary.displayCover(e)
            if (f == null) Icon(AppIcons.Library, null, tint = p.accent.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
            else FileImage(f, Modifier.fillMaxSize())
            val showEngine = LocalShowEngineLabel.current
            if ((showEngine && e.engine.isNotEmpty()) || e.missing) {
                Box(Modifier.align(Alignment.BottomStart).padding(6.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xB3000000)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(if (e.missing) g.missing else e.engine, color = if (e.missing) Color(0xFFFFB74D) else Color.White, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Box(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0x99000000)).clickable { menu = true }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MoreVert, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(onClick = { menu = false; onRename() }) { Text(g.rename) }
                    DropdownMenuItem(onClick = { menu = false; onCover() }) { Text(g.changeCover) }
                    DropdownMenuItem(onClick = { menu = false; onRemove() }) { Text(g.removeFolder) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.TopCenter) {
            Text(e.name, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
        }
    }
}

private fun fmtMb(mb: Int) = if (mb >= 1024) (mb / 1024).toString() + " GB" else "$mb MB"

/** Card in Settings: preview cache size, cache limit, and clear button. */
@Composable
fun GameCacheCard() {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val scope = rememberCoroutineScope()
    var size by remember { mutableStateOf(0L) }
    var limit by remember { mutableStateOf(GameCache.limitMb()) }
    LaunchedEffect(Unit) { size = withContext(Dispatchers.IO) { GameCache.size() } }
    SectionCard(g.cacheTitle) {
        Text(g.cacheSub(formatBytes(size)), color = p.muted, fontSize = 13.sp)
        Text(g.cacheNote, color = p.muted, fontSize = 13.sp)
        AppDropdown(g.cacheLimit, limit.toString(), GameCache.CHOICES_MB.map { Pair(it.toString(), fmtMb(it)) }) { v ->
            limit = v.toInt()
            scope.launch { withContext(Dispatchers.IO) { GameCache.setLimitMb(limit); size = GameCache.size() } }
        }
        SecondaryButton(g.cacheClear) { scope.launch { withContext(Dispatchers.IO) { GameCache.clear() }; size = 0L } }
    }
}
