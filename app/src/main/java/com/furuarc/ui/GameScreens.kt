package com.furuarc.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.documentfile.provider.DocumentFile
import com.furuarc.core.*
import com.furuarc.i18n.gameStr
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlin.math.abs

private val thumbGate = Semaphore(3)
private val STAR = Color(0xFFFFC107)

private class Loaded(val bmp: Bitmap?)

/** Tap = normal action, long press = start/change selection. The callback is always the latest. */
@Composable
private fun Modifier.tapLong(onTap: () -> Unit, onLong: () -> Unit): Modifier {
    val t by rememberUpdatedState(onTap)
    val l by rememberUpdatedState(onLong)
    return this.pointerInput(Unit) { detectTapGestures(onLongPress = { l() }, onTap = { t() }) }
}

/** Asset thumbnail (image or video frame), loaded lazily and limited to 3 concurrent loads. */
@Composable
private fun AssetThumb(a: GAsset, key: ByteArray?, max: Int, modifier: Modifier) {
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val st by produceState<Loaded?>(null, a.id, max) {
        val k = "t|" + a.id + "|" + max
        val hit = Thumbs.get(k)
        value = if (hit != null) Loaded(hit) else thumbGate.withPermit {
            withContext(Dispatchers.IO) {
                val b = try {
                    if (a.kind == 2) (if (a.arc != null) null else Thumbs.videoFrame(ctx, a.uri, max)) else Thumbs.assetBitmap(ctx, a, key, max)
                } catch (ce: CancellationException) {
                    throw ce
                } catch (e: Throwable) {
                    null
                }
                if (b != null) { Thumbs.put(k, b); GameSession.failed.remove(a.id) }
                else if (!(a.kind == 2 && a.arc != null)) GameSession.markFailed(a.id)
                Loaded(b)
            }
        }
    }
    Box(modifier.background(p.iconBg), contentAlignment = Alignment.Center) {
        val b = st?.bmp
        if (b != null) Image(b.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(if (a.kind == 2) AppIcons.Videocam else AppIcons.Image, null, tint = p.muted, modifier = Modifier.size(28.dp))
    }
}

// ------------------------------------------------------------------ actions (extract / share)

private fun toastMain(ctx: android.content.Context, msg: String) {
    Handler(Looper.getMainLooper()).post { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show() }
}

private class Actions(val extract: (List<XItem>) -> Unit, val share: (List<XItem>) -> Unit)

@Composable
private fun rememberActions(): Actions {
    val ctx = LocalContext.current
    val g = gameStr(LocalLang.current)
    val idx = GameSession.index
    var pending by remember { mutableStateOf<List<XItem>?>(null) }
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        val items = pending
        pending = null
        if (uri != null && items != null) {
            val name = DocumentFile.fromTreeUri(ctx, uri)?.name ?: "folder"
            GameExtract.start(ctx, items, uri, GameSession.entry?.name ?: "game", name, idx?.key)
        }
    }
    ExtractDialog()
    return remember(idx) {
        Actions(
            { items -> pending = items; pick.launch(null) },
            { items ->
                GameShare.share(ctx, items, idx?.key) { code ->
                    toastMain(ctx, when (code) { 1 -> g.shareTooMany; 2 -> g.shareTooBig; else -> g.shareFail })
                }
            },
        )
    }
}

@Composable
private fun ExtractDialog() {
    if (!GameExtract.running && !GameExtract.finished) return
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val running = GameExtract.running
    AlertDialog(
        onDismissRequest = { GameExtract.dismiss() },
        shape = RoundedCornerShape(CardRadius), backgroundColor = p.card, contentColor = p.text,
        title = { Text(g.extractTitle, fontWeight = FontWeight.Bold, color = p.text) },
        text = {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (running) {
                    Text(g.extractProg(GameExtract.done, GameExtract.total), color = p.text)
                    LinearProgressIndicator(
                        progress = if (GameExtract.total == 0) 0f else GameExtract.done.toFloat() / GameExtract.total,
                        color = p.accent, modifier = Modifier.fillMaxWidth(),
                    )
                    Text(GameExtract.current, color = p.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                } else {
                    Text(g.extractDone(GameExtract.total, GameExtract.failed, GameExtract.folder), color = p.text)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (running) GameExtract.cancel() else GameExtract.dismiss() }) {
                OneLineText(if (running) g.extractCancel else g.ok, 14.sp, FontWeight.Bold, p.accent)
            }
        },
    )
}

@Composable
private fun SelectionBar(count: Int, allFav: Boolean, onAll: () -> Unit, onClear: () -> Unit, onExtract: () -> Unit, onShare: () -> Unit, onFav: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(g.selected(count), fontWeight = FontWeight.SemiBold, color = p.text, modifier = Modifier.weight(1f))
                TextButton(onClick = onAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) { OneLineText(g.selAll, 13.sp, color = p.accent) }
                TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) { OneLineText(g.selNone, 13.sp, color = p.text) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { SecondaryButton(g.extract, count > 0, onExtract) }
                Box(Modifier.weight(1f)) { SecondaryButton(g.share, count > 0, onShare) }
                Box(Modifier.weight(1f)) { SecondaryButton(if (allFav) g.favDel else g.favAdd, count > 0, onFav) }
            }
        }
    }
}

// ------------------------------------------------------------------ game page

/** Single game page: 4 menus (Audio, Images, Video, Text) then the game info card. */
@Composable
fun GameScreen(onAudio: () -> Unit, onImage: () -> Unit, onVideo: () -> Unit, onText: () -> Unit, onBack: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val e = GameSession.entry
    val idx = GameSession.index
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(e?.name ?: "", onBack, trailing = {
            IconButton(onClick = { GameSession.reload() }) { Icon(Icons.Default.Refresh, g.retry, tint = p.text) }
        })
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            if (GameSession.loading) {
                Text(g.scanning, color = p.muted)
                BusyBar()
            }
            val err = GameSession.error
            if (err != null) {
                Text(g.loadFail(err), color = Color(0xFFE57373))
                SecondaryButton(g.retry) { GameSession.reload() }
            }
            if (GameSession.updating) {
                Text(g.updating, color = p.muted, fontSize = 13.sp)
                BusyBar()
            }
            GameSession.arcProgress?.let { ap ->
                Text(g.arcScanning(ap.first, ap.second, ap.third), color = p.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                BusyBar()
            }
            if (idx != null) {
                MenuCard(AppIcons.MusicNote, g.menuAudio, g.count(idx.audio.size), onAudio)
                MenuCard(AppIcons.Image, g.menuImage, g.count(idx.images.size), onImage)
                MenuCard(AppIcons.Videocam, g.menuVideo, g.count(idx.videos.size), onVideo)
                MenuCard(AppIcons.Description, g.menuText, GameSession.texts?.let { g.count(it.size) } ?: g.scanning, onText)
                val bad = idx.arcs.filter { it.error != null }
                AppCard(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                        val f = if (e != null) GameLibrary.displayCover(e) else null
                        Box(Modifier.width(96.dp).aspectRatio(0.7f).clip(RoundedCornerShape(10.dp)).background(p.iconBg), contentAlignment = Alignment.Center) {
                            if (f != null) FileImage(f, Modifier.fillMaxSize()) else Icon(AppIcons.Library, null, tint = p.muted, modifier = Modifier.size(32.dp))
                        }
                        Column(Modifier.weight(1f).padding(start = 16.dp)) {
                            Text(g.infoEngine, color = p.text, fontSize = 15.sp)
                            Text(idx.engine.ifEmpty { g.infoUnknown }, color = p.accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            Text(g.infoFiles(idx.files), color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
                            Text(g.infoArcOk(idx.arcs.size - bad.size), color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
                            Text(g.infoArcBad(bad.size), color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
                            val totalMedia = idx.audio.size + idx.images.size + idx.videos.size
                            val staticBad = if (idx.key == null) (idx.audio + idx.images).count { it.enc && !GameSession.failed.containsKey(it.id) } else 0
                            val badMedia = minOf(totalMedia, GameSession.failed.size + staticBad)
                            Text(g.infoMedia(totalMedia - badMedia), color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
                            Text(g.infoMediaBad(badMedia), color = p.text, fontSize = 14.sp, lineHeight = 22.sp)
                        }
                    }
                }
                bad.take(5).forEach { Text(it.path.substringAfterLast('/') + ": " + it.error, color = Color(0xFFE57373), fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis) }
                val hasEnc = idx.audio.any { it.enc } || idx.images.any { it.enc }
                if (hasEnc) Text(if (idx.key != null) g.encNote else g.noKey, color = p.muted, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ------------------------------------------------------------------ common list parts

@Composable
private fun FilterBar(
    showFilters: Boolean, query: String, onQuery: (String) -> Unit, hint: String, sort: Int, onSort: (Int) -> Unit,
    favOnly: Boolean, onFavOnly: (Boolean) -> Unit, selMode: Boolean, onSelMode: () -> Unit,
    shown: Int, total: Int, page: Int, pages: Int, onPage: (Int) -> Unit,
) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (showFilters) {
            AppTextField(query, onQuery, hint)
            AppDropdown(g.sortLabel, sort.toString(), g.sortOpts.mapIndexed { i, l -> Pair(i.toString(), l) }) { if (it == "4") GameShuffle.seed = (System.nanoTime() and 0x7fffffff).toInt(); onSort(it.toInt()) }
        }
        if (pages > 1) RenPager(page, pages, g.pageLabel(page + 1, pages, shown), onPage)
        Row(Modifier.fillMaxWidth().clickable { onFavOnly(!favOnly) }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = favOnly, onCheckedChange = { onFavOnly(it) }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
            Text(g.favOnly, color = p.text, fontSize = 14.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onSelMode) { Icon(AppIcons.Checklist, g.selMode, tint = if (selMode) p.accent else p.text) }
        }
        Text(g.shown(shown, total), color = p.muted, fontSize = 12.sp)
    }
}

/** Random order seed; changed each time the "Random Order" option is selected. */
private object GameShuffle { var seed by mutableStateOf(0) }

private fun <T> sortFilter(list: List<T>, q: String, sort: Int, favOnly: Boolean, fav: Set<String>, id: (T) -> String, name: (T) -> String, size: (T) -> Long): List<T> {
    var f = if (q.isBlank()) list else list.filter { name(it).contains(q.trim(), ignoreCase = true) }
    if (favOnly) f = f.filter { id(it) in fav }
    return when (sort) {
        1 -> f.sortedByDescending { name(it).lowercase() }
        2 -> f.sortedBy { size(it) }
        3 -> f.sortedByDescending { size(it) }
        4 -> f.shuffled(java.util.Random(GameShuffle.seed.toLong()))
        else -> f.sortedBy { name(it).lowercase() }
    }
}

private fun pagesOf(n: Int, per: Int) = maxOf(1, (n + per - 1) / per)

/** List row: tap / long press, highlight when selected, and favorite star on the right. */
@Composable
private fun ItemRow(selected: Boolean, fav: Boolean, onStar: () -> Unit, onTap: () -> Unit, onLong: () -> Unit, content: @Composable RowScope.() -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.background(if (selected) p.accent.copy(alpha = 0.18f) else Color.Transparent).tapLong(onTap, onLong).padding(start = 12.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
            IconButton(onClick = onStar, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Star, null, tint = if (fav) STAR else p.muted.copy(alpha = 0.4f))
            }
        }
    }
}

private fun toggled(sel: Set<String>, id: String) = if (id in sel) sel - id else sel + id

// ------------------------------------------------------------------ Audio

@Composable
fun GameAudioScreen(onBack: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val idx = GameSession.index
    DisposableEffect(Unit) { onDispose { GamePlayer.stop() } }
    var q by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(0) }
    var favOnly by remember { mutableStateOf(false) }
    var filt by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf(setOf<String>()) }
    var selMode by remember { mutableStateOf(false) }
    val fav = GameFav.set
    val acts = rememberActions()
    val all = idx?.audio ?: emptyList()
    val list = remember(all, q, sort, favOnly, fav, GameShuffle.seed) { sortFilter(all, q, sort, favOnly, fav, { it.id }, { it.path }, { it.size }) }
    var page by remember(q, sort, all, favOnly) { mutableStateOf(0) }
    val pages = pagesOf(list.size, 50)
    val pg = page.coerceIn(0, pages - 1)
    val pageItems = remember(list, pg) { list.drop(pg * 50).take(50) }
    val cur = GamePlayer.current
    BackHandler(enabled = selMode) { sel = emptySet(); selMode = false }
    LaunchedEffect(cur, GamePlayer.playing) {
        while (GamePlayer.playing) { GamePlayer.tick(); delay(300) }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(g.menuAudio, onBack, trailing = {
            IconButton(onClick = { filt = !filt; if (!filt) q = "" }) { Icon(Icons.Default.Search, g.search, tint = if (filt) p.accent else p.text) }
        })
        FilterBar(filt, q, { q = it }, g.filterHint, sort, { sort = it }, favOnly, { favOnly = it }, selMode, { selMode = !selMode; if (!selMode) sel = emptySet() }, list.size, all.size, pg, pages) { page = it }
        Spacer(Modifier.height(10.dp))
        if (selMode) {
            val chosen = all.filter { it.id in sel }
            SelectionBar(sel.size, chosen.all { it.id in fav }, { sel = list.map { it.id }.toSet() }, { sel = emptySet(); selMode = false },
                { acts.extract(chosen.map { it.toX() }) }, { acts.share(chosen.map { it.toX() }) },
                { GameFav.setFav(chosen.map { it.id }, !chosen.all { it.id in fav }) })
            Spacer(Modifier.height(8.dp))
        }
        if (all.isEmpty()) Text(g.empty, color = p.muted)
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pageItems, key = { it.id }) { a ->
                val active = cur?.id == a.id
                ItemRow(a.id in sel, a.id in fav, { GameFav.toggle(a.id) },
                    { if (selMode) sel = toggled(sel, a.id) else GamePlayer.play(a, idx?.key) },
                    { selMode = true; sel = toggled(sel, a.id) }) {
                    Icon(AppIcons.MusicNote, null, tint = if (active) p.accent else p.iconFg, modifier = Modifier.size(24.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(a.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (active) p.accent else p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(a.path.substringBeforeLast('/', "") + (if (a.path.contains('/')) " · " else "") + formatBytes(a.size) + (if (a.enc) " · " + g.encBadge else ""), fontSize = 12.sp, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        if (cur != null) {
            Spacer(Modifier.height(8.dp))
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cur.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        IconButton(onClick = { openAssetInPlayer(cur, idx?.key, 2) }, modifier = Modifier.size(32.dp)) { Icon(AppIcons.MediaPlayer, playerStr(LocalLang.current).openInPlayer, tint = p.accent) }
                    }
                    if (GamePlayer.failed) {
                        Text(g.playFail + (SpecialMedia.errorOf(cur.id)?.let { "\n" + it } ?: ""), color = Color(0xFFE57373), fontSize = 12.sp)
                    } else if (GamePlayer.preparing) {
                        Text(if (cur.arc != null) g.arcPrep else g.loadingMedia, color = p.muted, fontSize = 12.sp)
                    } else {
                        val seekSt = remember(cur.id) { SeekState() }
                        val dur = GamePlayer.duration.coerceAtLeast(1)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                if (!GamePlayer.playing && GamePlayer.position == 0) GamePlayer.play(cur, idx?.key) else GamePlayer.toggle()
                            }) {
                                Icon(if (GamePlayer.playing) AppIcons.Pause else Icons.Default.PlayArrow, null, tint = p.accent)
                            }
                            SeekSlider(seekSt, GamePlayer.position, dur, { GamePlayer.seek(it) }, Modifier.weight(1f))
                            Text(fmtMs(seekSt.shown(GamePlayer.position)) + " / " + fmtMs(GamePlayer.duration), fontSize = 11.sp, color = p.muted)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun fmtMs(ms: Int): String {
    val s = ms / 1000
    return String.format("%d:%02d", s / 60, s % 60)
}

// ------------------------------------------------------------------ Images

private fun dimsOf(ctx: android.content.Context, a: GAsset): String? {
    if (a.enc || a.special || a.arc != null) return null
    val o = BitmapFactory.Options()
    o.inJustDecodeBounds = true
    return try {
        ctx.contentResolver.openInputStream(a.uri)?.use { BitmapFactory.decodeStream(it, null, o) }
        if (o.outWidth > 0) o.outWidth.toString() + "×" + o.outHeight else null
    } catch (e: Exception) {
        null
    }
}

@Composable
fun GameImageScreen(onBack: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val idx = GameSession.index
    var q by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(0) }
    var favOnly by remember { mutableStateOf(false) }
    var filt by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf(-1) }
    var sel by remember { mutableStateOf(setOf<String>()) }
    var selMode by remember { mutableStateOf(false) }
    val fav = GameFav.set
    val acts = rememberActions()
    val all = idx?.images ?: emptyList()
    val list = remember(all, q, sort, favOnly, fav, GameShuffle.seed) { sortFilter(all, q, sort, favOnly, fav, { it.id }, { it.path }, { it.size }) }
    var page by remember(q, sort, all, favOnly) { mutableStateOf(0) }
    val per = 60 // 20 rows x 3 columns
    val pages = pagesOf(list.size, per)
    val pg = page.coerceIn(0, pages - 1)
    BackHandler(enabled = open >= 0 || selMode) { if (open >= 0) open = -1 else { sel = emptySet(); selMode = false } }
    if (open in list.indices) {
        ImageViewer(list, open, idx?.key, { open = it }, { open = -1 })
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(g.menuImage, onBack, trailing = {
            IconButton(onClick = { filt = !filt; if (!filt) q = "" }) { Icon(Icons.Default.Search, g.search, tint = if (filt) p.accent else p.text) }
        })
        FilterBar(filt, q, { q = it }, g.filterHint, sort, { sort = it }, favOnly, { favOnly = it }, selMode, { selMode = !selMode; if (!selMode) sel = emptySet() }, list.size, all.size, pg, pages) { page = it }
        Spacer(Modifier.height(10.dp))
        if (selMode) {
            val chosen = all.filter { it.id in sel }
            SelectionBar(sel.size, chosen.all { it.id in fav }, { sel = list.map { it.id }.toSet() }, { sel = emptySet(); selMode = false },
                { acts.extract(chosen.map { it.toX() }) }, { acts.share(chosen.map { it.toX() }) },
                { GameFav.setFav(chosen.map { it.id }, !chosen.all { it.id in fav }) })
            Spacer(Modifier.height(8.dp))
        }
        if (all.isEmpty()) Text(g.empty, color = p.muted)
        val rows = remember(list, pg) { list.withIndex().toList().drop(pg * per).take(per).chunked(3) }
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (iv in row) {
                        val a = iv.value
                        val chosen = a.id in sel
                        Box(
                            Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                                .then(if (chosen) Modifier.border(3.dp, p.accent, RoundedCornerShape(8.dp)) else Modifier)
                                .tapLong({ if (selMode) sel = toggled(sel, a.id) else open = iv.index }, { selMode = true; sel = toggled(sel, a.id) }),
                        ) {
                            AssetThumb(a, idx?.key, 256, Modifier.fillMaxSize())
                            if (a.id in fav) Icon(Icons.Default.Star, null, tint = STAR, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(18.dp))
                            if (chosen) {
                                Box(Modifier.align(Alignment.TopStart).padding(4.dp).size(22.dp).clip(RoundedCornerShape(11.dp)).background(p.accent), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ImageViewer(list: List<GAsset>, index: Int, key: ByteArray?, onIndex: (Int) -> Unit, onClose: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val ctx = LocalContext.current
    val a = list[index]
    var scale by remember(index) { mutableStateOf(1f) }
    var off by remember(index) { mutableStateOf(Offset.Zero) }
    val st by produceState<Loaded?>(null, a.id) {
        value = withContext(Dispatchers.IO) {
            val bm = try { Thumbs.assetBitmap(ctx, a, key, 2048) } catch (ce: CancellationException) { throw ce } catch (e: Throwable) { null }
            if (bm == null) GameSession.markFailed(a.id) else GameSession.failed.remove(a.id)
            Loaded(bm)
        }
    }
    val dims by produceState<String?>(null, a.id) { value = withContext(Dispatchers.IO) { dimsOf(ctx, a) } }
    val cur = rememberUpdatedState(index)
    val isFav = a.id in GameFav.set
    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(index) { detectTapGestures(onDoubleTap = { scale = 1f; off = Offset.Zero }) }
            .pointerInput(index) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        var totalX = 0f
                        var zoomed = false
                        do {
                            val ev = awaitPointerEvent()
                            if (ev.changes.size > 1 || scale > 1.01f) {
                                scale = (scale * ev.calculateZoom()).coerceIn(1f, 6f)
                                off = if (scale <= 1.01f) Offset.Zero else off + ev.calculatePan()
                                ev.changes.forEach { it.consumeAllChanges() }
                                zoomed = true
                            } else {
                                totalX += ev.calculatePan().x
                            }
                        } while (ev.changes.any { it.pressed })
                        if (!zoomed && abs(totalX) > 140f) {
                            val n = cur.value + if (totalX < 0) 1 else -1
                            if (n in list.indices) onIndex(n)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val b = st?.bmp
        if (b != null) {
            Image(
                b.asImageBitmap(), null,
                Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = off.x, translationY = off.y),
                contentScale = ContentScale.Fit,
            )
        } else if (st != null) Text(g.imgFail + (SpecialMedia.errorOf(a.id)?.let { "\n" + it } ?: ""), color = Color.White, modifier = Modifier.padding(24.dp)) else Text(if (a.arc != null) g.arcPrep else g.loadingMedia, color = Color.White)
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, g.close, tint = Color.White) }
            Column(Modifier.weight(1f)) {
                Text(a.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    (index + 1).toString() + "/" + list.size + " · " + a.name.substringAfterLast('.', "").uppercase() + " · " + formatBytes(a.size) +
                        (dims?.let { " · $it" } ?: "") + (if (a.enc) " · " + g.encBadge else ""),
                    color = Color(0xFFCCCCCC), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = { openAssetInPlayer(a, GameSession.index?.key, 1) }) { Icon(AppIcons.MediaPlayer, playerStr(LocalLang.current).openInPlayer, tint = Color.White) }
            IconButton(onClick = { GameFav.toggle(a.id) }) { Icon(Icons.Default.Star, g.favAdd, tint = if (isFav) STAR else Color.White) }
            IconButton(onClick = { if (index > 0) onIndex(index - 1) }, enabled = index > 0) { Icon(Icons.Default.KeyboardArrowLeft, g.prev, tint = if (index > 0) Color.White else Color.Gray) }
            IconButton(onClick = { if (index < list.size - 1) onIndex(index + 1) }, enabled = index < list.size - 1) { Icon(Icons.Default.KeyboardArrowRight, g.nextImg, tint = if (index < list.size - 1) Color.White else Color.Gray) }
        }
    }
}

// ------------------------------------------------------------------ Video

@Composable
fun GameVideoScreen(onBack: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val idx = GameSession.index
    var q by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(0) }
    var favOnly by remember { mutableStateOf(false) }
    var filt by remember { mutableStateOf(false) }
    var play by remember { mutableStateOf<GAsset?>(null) }
    var sel by remember { mutableStateOf(setOf<String>()) }
    var selMode by remember { mutableStateOf(false) }
    val fav = GameFav.set
    val acts = rememberActions()
    val all = idx?.videos ?: emptyList()
    val list = remember(all, q, sort, favOnly, fav, GameShuffle.seed) { sortFilter(all, q, sort, favOnly, fav, { it.id }, { it.path }, { it.size }) }
    var page by remember(q, sort, all, favOnly) { mutableStateOf(0) }
    val pages = pagesOf(list.size, 20)
    val pg = page.coerceIn(0, pages - 1)
    val pageItems = remember(list, pg) { list.drop(pg * 20).take(20) }
    BackHandler(enabled = play != null || selMode) { if (play != null) play = null else { sel = emptySet(); selMode = false } }
    val pa = play
    if (pa != null) {
        VideoPlayer(pa) { play = null }
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(g.menuVideo, onBack, trailing = {
            IconButton(onClick = { filt = !filt; if (!filt) q = "" }) { Icon(Icons.Default.Search, g.search, tint = if (filt) p.accent else p.text) }
        })
        FilterBar(filt, q, { q = it }, g.filterHint, sort, { sort = it }, favOnly, { favOnly = it }, selMode, { selMode = !selMode; if (!selMode) sel = emptySet() }, list.size, all.size, pg, pages) { page = it }
        Spacer(Modifier.height(10.dp))
        if (selMode) {
            val chosen = all.filter { it.id in sel }
            SelectionBar(sel.size, chosen.all { it.id in fav }, { sel = list.map { it.id }.toSet() }, { sel = emptySet(); selMode = false },
                { acts.extract(chosen.map { it.toX() }) }, { acts.share(chosen.map { it.toX() }) },
                { GameFav.setFav(chosen.map { it.id }, !chosen.all { it.id in fav }) })
            Spacer(Modifier.height(8.dp))
        }
        if (all.isEmpty()) Text(g.empty, color = p.muted)
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pageItems, key = { it.id }) { a ->
                ItemRow(a.id in sel, a.id in fav, { GameFav.toggle(a.id) },
                    { if (selMode) sel = toggled(sel, a.id) else play = a },
                    { selMode = true; sel = toggled(sel, a.id) }) {
                    AssetThumb(a, null, 256, Modifier.width(112.dp).height(64.dp).clip(RoundedCornerShape(8.dp)))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(a.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(formatBytes(a.size), fontSize = 12.sp, color = p.muted)
                    }
                }
            }
        }
    }
}

private class VRes(val path: String?)

@Composable
private fun VideoPlayer(a: GAsset, onClose: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val failed = remember(a.id) { mutableStateOf(false) }
    val ref = remember { arrayOfNulls<VideoView>(1) }
    var playing by remember(a.id) { mutableStateOf(true) }
    var dur by remember(a.id) { mutableStateOf(0) }
    var pos by remember(a.id) { mutableStateOf(0) }
    val seekSt = remember(a.id) { SeekState() }
    val mpRef = remember { arrayOfNulls<android.media.MediaPlayer>(1) }
    DisposableEffect(a.id) { onDispose { try { ref[0]?.stopPlayback() } catch (e: Throwable) { } } }
    // videos from archives are extracted to cache first; all failures (including out of memory) are shown as a message
    val res by produceState<VRes?>(null, a.id) {
        value = withContext(Dispatchers.IO) {
            try {
                VRes(if (a.arc != null) GameArc.toFile(ctx, a).absolutePath else null)
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Throwable) {
                VRes("")
            }
        }
    }
    val r = res
    LaunchedEffect(r) {
        while (true) {
            delay(300)
            val v = ref[0]
            if (v != null && seekSt.drag == null) {
                try { pos = v.currentPosition; playing = v.isPlaying } catch (e: Throwable) { }
            }
        }
    }
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        if (r == null) {
            Text(if (a.arc != null) g.arcPrep else g.loadingMedia, color = Color.White)
        } else if (r.path == "") {
            Text(g.playFail, color = Color.White, modifier = Modifier.padding(24.dp))
        } else {
            AndroidView(
                factory = { c ->
                    VideoView(c).also { v ->
                        ref[0] = v
                        v.setOnPreparedListener { mp -> mpRef[0] = mp; dur = mp.duration; v.start() }
                        v.setOnCompletionListener { playing = false }
                        v.setOnErrorListener { _, _, _ -> GameSession.markFailed(a.id); failed.value = true; true }
                        if (r.path != null) v.setVideoPath(r.path) else v.setVideoURI(a.uri)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
            if (failed.value) Text(g.playFail, color = Color.White, modifier = Modifier.padding(24.dp))
            Row(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99000000)).padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    val v = ref[0]
                    if (v != null) {
                        try {
                            if (v.isPlaying) { v.pause(); playing = false } else { v.start(); playing = true }
                        } catch (e: Throwable) { }
                    }
                }) { Icon(if (playing) AppIcons.Pause else Icons.Default.PlayArrow, null, tint = Color.White) }
                val d = dur.coerceAtLeast(1)
                SeekSlider(seekSt, pos, d, { x ->
                    val m = mpRef[0]
                    if (m != null) m.seekExact(x) else try { ref[0]?.seekTo(x) } catch (e: Throwable) { }
                    pos = x
                }, Modifier.weight(1f))
                Text(fmtMs(seekSt.shown(pos)) + " / " + fmtMs(dur), fontSize = 11.sp, color = Color.White)
            }
        }
        Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Default.Close, g.close, tint = Color.White) }
            Text(a.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = { openAssetInPlayer(a, GameSession.index?.key, 3) }) { Icon(AppIcons.MediaPlayer, playerStr(LocalLang.current).openInPlayer, tint = Color.White) }
        }
    }
}

// ------------------------------------------------------------------ Text

@Composable
fun GameTextScreen(onBack: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val texts = GameSession.texts
    var q by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(0) }
    var favOnly by remember { mutableStateOf(false) }
    var filt by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<TextItem?>(null) }
    var sel by remember { mutableStateOf(setOf<String>()) }
    var selMode by remember { mutableStateOf(false) }
    val fav = GameFav.set
    val acts = rememberActions()
    val all = texts ?: emptyList()
    val list = remember(all, q, sort, favOnly, fav, GameShuffle.seed) { sortFilter(all, q, sort, favOnly, fav, { it.id }, { it.path }, { it.size }) }
    var page by remember(q, sort, all, favOnly) { mutableStateOf(0) }
    val pages = pagesOf(list.size, 50)
    val pg = page.coerceIn(0, pages - 1)
    val pageItems = remember(list, pg) { list.drop(pg * 50).take(50) }
    BackHandler(enabled = open != null || selMode) { if (open != null) open = null else { sel = emptySet(); selMode = false } }
    val oi = open
    if (oi != null) {
        TextViewer(oi) { open = null }
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(g.menuText, onBack, trailing = {
            IconButton(onClick = { filt = !filt; if (!filt) q = "" }) { Icon(Icons.Default.Search, g.search, tint = if (filt) p.accent else p.text) }
        })
        if (texts == null) {
            Text(g.textScanning, color = p.muted, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            BusyBar()
            Spacer(Modifier.height(8.dp))
        }
        FilterBar(filt, q, { q = it }, g.filterHint, sort, { sort = it }, favOnly, { favOnly = it }, selMode, { selMode = !selMode; if (!selMode) sel = emptySet() }, list.size, all.size, pg, pages) { page = it }
        Spacer(Modifier.height(10.dp))
        if (selMode) {
            val chosen = all.filter { it.id in sel }
            SelectionBar(sel.size, chosen.all { it.id in fav }, { sel = list.map { it.id }.toSet() }, { sel = emptySet(); selMode = false },
                { acts.extract(chosen.map { it.toX() }) }, { acts.share(chosen.map { it.toX() }) },
                { GameFav.setFav(chosen.map { it.id }, !chosen.all { it.id in fav }) })
            Spacer(Modifier.height(8.dp))
        }
        if (texts != null && all.isEmpty()) Text(g.empty, color = p.muted)
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(pageItems, key = { it.id }) { t ->
                ItemRow(t.id in sel, t.id in fav, { GameFav.toggle(t.id) },
                    { if (selMode) sel = toggled(sel, t.id) else open = t },
                    { selMode = true; sel = toggled(sel, t.id) }) {
                    Icon(AppIcons.Description, null, tint = p.iconFg, modifier = Modifier.size(24.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(t.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        val badge = when (t.type) { 1 -> g.badgeScript; 2 -> g.badgeData; else -> g.badgeText }
                        Text(badge + " · " + formatBytes(t.size) + (if (t.path.contains('/')) " · " + t.path.substringBeforeLast('/') else ""), fontSize = 12.sp, color = p.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

private val ENC_VALUES = listOf("auto", "UTF-8", "Shift_JIS", "GBK", "UTF-16LE")

@Composable
private fun TextViewer(item: TextItem, onClose: () -> Unit) {
    val g = gameStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    var enc by remember(item) { mutableStateOf(0) }
    var mode by remember(item) { mutableStateOf(if (item.type == 0) 1 else 0) }
    var q by remember(item) { mutableStateOf("") }
    val doc by produceState<TextDoc?>(null, item, enc, mode) {
        value = null
        value = withContext(Dispatchers.IO) { GameScan.readText(ctx, item, ENC_VALUES[enc], mode == 1) }
    }
    val d = doc
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(item.name, onClose)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (item.type != 0 && d != null && d.canRaw) SegmentedChips(g.modeOpts, mode, 14.sp) { mode = it }
            if (mode == 1) SegmentedChips(g.encOpts, enc, 12.sp) { enc = it }
            AppTextField(q, { q = it }, g.textFilter)
        }
        Spacer(Modifier.height(8.dp))
        when {
            d == null -> { BusyBar(); Text(if (item.arc != null) g.arcPrep else g.loadingMedia, color = p.muted, fontSize = 13.sp) }
            d.error != null -> Text(if (d.error == "big") g.tooBig else g.readFail(d.error), color = Color(0xFFE57373))
            d.units != null -> {
                val u = remember(d, q) { if (q.isBlank()) d.units else d.units.filter { it.text.contains(q.trim(), true) || (it.speaker ?: "").contains(q.trim(), true) } }
                Text(g.dialogCount(d.units.size), color = p.muted, fontSize = 12.sp)
                if (d.units.isEmpty()) Text(g.noDialog, color = p.muted)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(u) { t ->
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(10.dp)) {
                                if (t.speaker != null) Text(t.speaker, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = p.accent)
                                Text(t.text, fontSize = 14.sp, color = p.text)
                            }
                        }
                    }
                }
            }
            d.lines != null -> {
                val ln = remember(d, q) {
                    val all = d.lines.withIndex().toList()
                    if (q.isBlank()) all else all.filter { it.value.contains(q.trim(), true) }
                }
                if (d.truncated) Text(g.truncated, color = p.muted, fontSize = 12.sp)
                LazyColumn(Modifier.weight(1f)) {
                    items(ln) { l ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                            Text((l.index + 1).toString(), fontSize = 11.sp, color = p.muted, fontFamily = FontFamily.Monospace, modifier = Modifier.width(40.dp))
                            Text(l.value, fontSize = 13.sp, color = p.text, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}

/** Opens a Game Assets media file in the Media Player (extracts / decrypts it first when needed). kind: 1 image, 2 audio, 3 video. */
internal fun openAssetInPlayer(a: GAsset, key: ByteArray?, kind: Int) {
    com.furuarc.Engine.scope.launch {
        val ctx = com.furuarc.Engine.app
        val item: com.furuarc.core.MItem? = try {
            when {
                a.special -> com.furuarc.core.SpecialMedia.audioFile(ctx, a).let { com.furuarc.core.MItem(it.name, null, it.absolutePath, it.length()) }
                a.arc != null -> GameArc.toFile(ctx, a).let { com.furuarc.core.MItem(a.name, null, it.absolutePath, it.length()) }
                a.enc -> {
                    val f = if (key != null) GameCache.decrypted(ctx, a, key) else null
                    if (f != null) com.furuarc.core.MItem(RpgmCrypt.decryptedPath(a.name) ?: a.name, null, f.absolutePath, f.length()) else null
                }
                else -> com.furuarc.core.MItem(a.name, a.uri, null, a.size)
            }
        } catch (e: Throwable) {
            null
        }
        if (item != null) withContext(kotlinx.coroutines.Dispatchers.Main) {
            GamePlayer.stop()
            PlayerHub.openSingle(kind, item, "")
            PlayerHub.navigate?.invoke(kind)
        }
    }
}
