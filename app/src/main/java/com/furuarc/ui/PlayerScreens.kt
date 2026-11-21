package com.furuarc.ui

import android.graphics.BitmapFactory
import android.media.PlaybackParams
import android.net.Uri
import android.os.SystemClock
import android.provider.OpenableColumns
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.furuarc.core.AudioEngine
import com.furuarc.core.MediaPrefs
import com.furuarc.core.MediaScan
import com.furuarc.core.MItem
import com.furuarc.core.seekExact
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.abs

/** Lists of the Media Player screens (kept while the app is open) and the hand-off used by "Open with" and Game Assets. */
object PlayerHub {
    var image by mutableStateOf<List<MItem>>(emptyList())
    var audio by mutableStateOf<List<MItem>>(emptyList())
    var video by mutableStateOf<List<MItem>>(emptyList())
    var label by mutableStateOf<Map<Int, String>>(emptyMap())
    val tree = HashMap<Int, Uri>()

    /** (kind, index) opened automatically when the list screen of that kind shows up. */
    var autoOpen: Pair<Int, Int>? = null

    /** Set by MainActivity: switches to the Media Player screen of a kind (1 image, 2 audio, 3 video). */
    var navigate: ((Int) -> Unit)? = null

    fun items(kind: Int): List<MItem> = when (kind) { 1 -> image; 2 -> audio; else -> video }

    fun set(kind: Int, list: List<MItem>, text: String) {
        when (kind) { 1 -> image = list; 2 -> audio = list; else -> video = list }
        label = label + (kind to text)
    }

    /** Puts one item into the list of its kind and asks the list screen to open it. */
    fun openSingle(kind: Int, item: MItem, text: String) {
        set(kind, listOf(item), text)
        tree.remove(kind)
        autoOpen = kind to 0
    }
}

private val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

private fun nextSpeed(cur: Float): Float = SPEEDS.firstOrNull { it > cur + 0.01f } ?: SPEEDS.first()

fun itemOfUri(ctx: android.content.Context, uri: Uri): MItem {
    var name = ""
    var size = 0L
    try {
        ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                name = c.getString(0) ?: ""
                size = if (c.isNull(1)) 0L else c.getLong(1)
            }
        }
    } catch (e: Exception) { }
    if (name.isEmpty()) name = uri.lastPathSegment?.substringAfterLast('/') ?: "file"
    return MItem(name, uri, null, size)
}

private fun openStream(ctx: android.content.Context, it: MItem): InputStream? =
    if (it.uri != null) ctx.contentResolver.openInputStream(it.uri) else if (it.path != null) FileInputStream(it.path) else null

private fun decodeBitmap(ctx: android.content.Context, item: MItem, maxDim: Int): ImageBitmap? {
    return try {
        val b = BitmapFactory.Options()
        b.inJustDecodeBounds = true
        openStream(ctx, item)?.use { BitmapFactory.decodeStream(it, null, b) }
        if (b.outWidth <= 0 || b.outHeight <= 0) return null
        var sample = 1
        while (b.outWidth / (sample * 2) >= maxDim || b.outHeight / (sample * 2) >= maxDim) sample *= 2
        val o = BitmapFactory.Options()
        o.inSampleSize = sample
        openStream(ctx, item)?.use { BitmapFactory.decodeStream(it, null, o) }?.asImageBitmap()
    } catch (e: Throwable) {
        null
    }
}

private fun sizeText(b: Long): String = when {
    b >= 1048576L -> "%.1f MB".format(b / 1048576.0)
    b >= 1024L -> "%.0f KB".format(b / 1024.0)
    else -> "$b B"
}

// ------------------------------------------------------------------ menu

@Composable
fun PlayerMenuScreen(onImage: () -> Unit, onAudio: () -> Unit, onVideo: () -> Unit, onSettings: () -> Unit, onBack: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(pl.menuTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            MenuCard(AppIcons.Image, pl.imageTitle, pl.imageSub, onImage)
            MenuCard(AppIcons.MusicNote, pl.audioTitle, pl.audioSub, onAudio)
            MenuCard(AppIcons.Videocam, pl.videoTitle, pl.videoSub, onVideo)
            MenuCard(AppIcons.Gear, pl.settingsTitle, pl.settingsSub, onSettings)
        }
    }
}

// ------------------------------------------------------------------ list

@Composable
fun PlayerListScreen(kind: Int, onBack: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val items = PlayerHub.items(kind)
    var scanning by remember { mutableStateOf(false) }
    var scanCount by remember { mutableStateOf(0) }
    var job by remember { mutableStateOf<Job?>(null) }
    var open by remember { mutableStateOf(-1) }
    val title = when (kind) { 1 -> pl.imageTitle; 2 -> pl.audioTitle; else -> pl.videoTitle }
    val mime = when (kind) { 1 -> "image/*"; 2 -> "audio/*"; else -> "video/*" }

    fun scan(tree: Uri) {
        job?.cancel()
        PlayerHub.tree[kind] = tree
        job = scope.launch {
            scanning = true
            scanCount = 0
            try {
                val r = withContext(Dispatchers.IO) { MediaScan.scanTree(ctx, tree, kind, MediaPrefs.subfolders) { scanCount = it } }
                val name = tree.lastPathSegment?.substringAfterLast(':')?.substringAfterLast('/') ?: ""
                PlayerHub.set(kind, r, name)
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
            } finally {
                scanning = false
            }
        }
    }

    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            for (u in uris) try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            scope.launch {
                val list = withContext(Dispatchers.IO) { uris.map { itemOfUri(ctx, it) } }
                PlayerHub.tree.remove(kind)
                PlayerHub.set(kind, list, "")
            }
        }
    }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            scan(uri)
        }
    }

    BackHandler(enabled = open >= 0) { open = -1 }
    DisposableEffect(kind) {
        onDispose { if (kind == 2 && !MediaPrefs.background) AudioEngine.stop() }
    }
    LaunchedEffect(Unit) {
        val a = PlayerHub.autoOpen
        if (a != null && a.first == kind) {
            PlayerHub.autoOpen = null
            if (kind == 2) AudioEngine.setQueue(PlayerHub.audio, a.second) else open = a.second
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            ScreenHeader(title, onBack)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { PrimaryButton(pl.pickFiles, !scanning) { pickFiles.launch(arrayOf(mime, "application/octet-stream")) } }
                Box(Modifier.weight(1f)) { SecondaryButton(pl.pickFolder, !scanning) { pickFolder.launch(null) } }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.98f, ripple = false) {
                MediaPrefs.subfolders = !MediaPrefs.subfolders
                MediaPrefs.save()
                PlayerHub.tree[kind]?.let { scan(it) }
            }, verticalAlignment = Alignment.CenterVertically) {
                Text(pl.subfolders, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = MediaPrefs.subfolders,
                    onCheckedChange = {
                        MediaPrefs.subfolders = it
                        MediaPrefs.save()
                        PlayerHub.tree[kind]?.let { t -> scan(t) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.45f),
                        uncheckedThumbColor = p.muted, uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
                    ),
                )
            }
            Spacer(Modifier.height(4.dp))
            if (scanning) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = p.accent, strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text(pl.scanning + " " + pl.found(scanCount), color = p.muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { job?.cancel(); scanning = false }) { Text("✕", color = p.accent) }
                }
            } else if (items.isNotEmpty()) {
                val lb = PlayerHub.label[kind] ?: ""
                Text((if (lb.isNotEmpty()) "$lb · " else "") + pl.found(items.size), color = p.muted, fontSize = 13.sp)
            }
            Spacer(Modifier.height(6.dp))
            if (items.isEmpty() && !scanning) {
                EmptyState(when (kind) { 1 -> AppIcons.Image; 2 -> AppIcons.MusicNote; else -> AppIcons.Videocam }, pl.emptyHint, Modifier.fillMaxWidth().padding(top = 24.dp))
            }
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(items, key = { i, it -> i.toString() + it.key }) { i, it ->
                    val active = kind == 2 && AudioEngine.current?.key == it.key
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.fillMaxWidth().cardClickable {
                                when (kind) {
                                    2 -> if (AudioEngine.queue === items) AudioEngine.playIndex(i) else AudioEngine.setQueue(items, i)
                                    else -> open = i
                                }
                            }.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (kind == 1) Thumb(it) else Box(Modifier.size(44.dp).clip(RoundedCornerShape(IconRadius)).background(p.iconBg), contentAlignment = Alignment.Center) {
                                Icon(if (kind == 2) AppIcons.MusicNote else AppIcons.Videocam, null, tint = if (active) p.accent else p.iconFg, modifier = Modifier.size(24.dp))
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(it.name, color = if (active) p.accent else p.text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (it.size > 0) Text(sizeText(it.size), color = p.muted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            if (kind == 2 && AudioEngine.current != null) {
                Spacer(Modifier.height(8.dp))
                AudioPanel()
            }
            Spacer(Modifier.height(12.dp))
        }
        if (open >= 0 && kind == 1) ImageViewer(items, open) { open = -1 }
        if (open >= 0 && kind == 3) VideoOverlay(items, open) { open = -1 }
    }
}

@Composable
private fun Thumb(item: MItem) {
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val bmp by produceState<ImageBitmap?>(null, item.key) { value = withContext(Dispatchers.IO) { decodeBitmap(ctx, item, 160) } }
    Box(Modifier.size(56.dp).clip(RoundedCornerShape(IconRadius)).background(p.iconBg), contentAlignment = Alignment.Center) {
        val b = bmp
        if (b != null) Image(b, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(AppIcons.Image, null, tint = p.iconFg, modifier = Modifier.size(24.dp))
    }
}

// ------------------------------------------------------------------ audio panel

@Composable
private fun PChip(text: String, active: Boolean = false, onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(ControlRadius)
    Box(
        Modifier.clip(shape).background(if (active) p.accent.copy(alpha = 0.2f) else p.trailingBg)
            .border(1.dp, if (active) p.accent else p.cardBorder, shape)
            .clickableScale(pressed = 0.94f, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) { Text(text, fontSize = 13.sp, color = if (active) p.accent else p.text, maxLines = 1) }
}

@Composable
private fun Glyph(text: String, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)).clickableScale(pressed = 0.88f, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 22.sp, color = tint)
    }
}

@Composable
private fun AudioPanel() {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val cur = AudioEngine.current ?: return
    val seek = remember { SeekState() }
    var now by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(AudioEngine.sleepAt) {
        while (AudioEngine.sleepAt != 0L) { now = SystemClock.elapsedRealtime(); delay(15000) }
    }
    val remainMin = if (AudioEngine.sleepAt == 0L) 0 else (((AudioEngine.sleepAt - now) / 60000L).toInt() + 1).coerceAtLeast(1)
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(cur.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (AudioEngine.failed) {
                Text(pl.loadFail, color = Color(0xFFE57373), fontSize = 12.sp)
            } else if (AudioEngine.preparing) {
                Text(pl.loading, color = p.muted, fontSize = 12.sp)
            } else {
                SeekSlider(seek, AudioEngine.position, AudioEngine.duration, { AudioEngine.seek(it) })
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(fmtClock(seek.shown(AudioEngine.position)), fontSize = 11.sp, color = p.muted)
                    Spacer(Modifier.weight(1f))
                    Text(fmtClock(AudioEngine.duration), fontSize = 11.sp, color = p.muted)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                Glyph("⏮", p.text) { AudioEngine.previous() }
                Box(Modifier.size(52.dp).clip(RoundedCornerShape(26.dp)).background(p.accent.copy(alpha = 0.2f)).clickableScale(pressed = 0.9f) { AudioEngine.toggle() }, contentAlignment = Alignment.Center) {
                    Icon(if (AudioEngine.playing) AppIcons.Pause else Icons.Default.PlayArrow, null, tint = p.accent, modifier = Modifier.size(30.dp))
                }
                Glyph("⏭", p.text) { AudioEngine.next() }
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PChip(pl.shuffle, MediaPrefs.shuffle) { MediaPrefs.shuffle = !MediaPrefs.shuffle; MediaPrefs.save() }
                PChip(pl.repeat(MediaPrefs.repeat), MediaPrefs.repeat != 0) { MediaPrefs.repeat = (MediaPrefs.repeat + 1) % 3; MediaPrefs.save() }
                PChip(pl.speed(MediaPrefs.speed), MediaPrefs.speed != 1f) { AudioEngine.setSpeed(nextSpeed(MediaPrefs.speed)) }
                PChip(pl.abLoop(AudioEngine.loopA, AudioEngine.loopB), AudioEngine.loopA != null) { AudioEngine.cycleLoop() }
                PChip(pl.sleep(remainMin), remainMin > 0) {
                    val next = when { remainMin <= 0 -> 15; remainMin <= 15 -> 30; remainMin <= 30 -> 60; else -> 0 }
                    AudioEngine.setSleep(next)
                    now = SystemClock.elapsedRealtime()
                }
                PChip(pl.stop) { AudioEngine.stop() }
            }
        }
    }
}

// ------------------------------------------------------------------ image viewer

@Composable
fun ImageViewer(items: List<MItem>, start: Int, onClose: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val ctx = LocalContext.current
    if (items.isEmpty()) { onClose(); return }
    var index by remember { mutableStateOf(start.coerceIn(0, items.size - 1)) }
    var scale by remember(index) { mutableStateOf(1f) }
    var offset by remember(index) { mutableStateOf(Offset.Zero) }
    var ui by remember { mutableStateOf(true) }
    var slide by remember { mutableStateOf(false) }
    var sz by remember { mutableStateOf(IntSize.Zero) }
    val item = items[index]
    val res by produceState<Pair<Boolean, ImageBitmap?>>(false to null, item.key) {
        value = false to null
        value = true to withContext(Dispatchers.IO) { decodeBitmap(ctx, item, 2400) }
    }
    LaunchedEffect(slide, index) {
        if (slide && items.size > 1) {
            delay(MediaPrefs.slideshowSec * 1000L)
            index = (index + 1) % items.size
        }
    }
    fun go(d: Int) { index = (index + d + items.size) % items.size }
    BackHandler { onClose() }
    Box(
        Modifier.fillMaxSize().background(Color.Black).onSizeChanged { sz = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { ui = !ui },
                    onDoubleTap = { if (scale > 1f) { scale = 1f; offset = Offset.Zero } else scale = 2.5f },
                )
            }
            .pointerInput(index) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitFirstDown(requireUnconsumed = false)
                        var dx = 0f
                        var multi = false
                        do {
                            val ev = awaitPointerEvent()
                            val pressed = ev.changes.count { it.pressed }
                            val pan = ev.calculatePan()
                            if (pressed >= 2) {
                                multi = true
                                scale = (scale * ev.calculateZoom()).coerceIn(1f, 6f)
                                offset = if (scale > 1f) offset + pan else Offset.Zero
                                ev.changes.forEach { it.consumeAllChanges() }
                            } else if (pressed == 1 && pan != Offset.Zero) {
                                if (scale > 1f) offset += pan else dx += pan.x
                                ev.changes.forEach { it.consumeAllChanges() }
                            }
                            val mx = sz.width * (scale - 1f) / 2f
                            val my = sz.height * (scale - 1f) / 2f
                            offset = Offset(offset.x.coerceIn(-mx, mx), offset.y.coerceIn(-my, my))
                        } while (ev.changes.any { it.pressed })
                        if (!multi && scale <= 1f && abs(dx) > 110f) go(if (dx < 0) 1 else -1)
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        val b = res.second
        if (b != null) {
            Image(
                b, null,
                Modifier.fillMaxSize().graphicsLayer { scaleX = scale; scaleY = scale; translationX = offset.x; translationY = offset.y },
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(if (res.first) pl.loadFail else pl.loading, color = Color.White, modifier = Modifier.padding(24.dp))
        }
        if (ui) {
            Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, pl.close, tint = Color.White) }
                Text(item.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("${index + 1}/${items.size}", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
            }
            Row(
                Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
            ) {
                Glyph("‹", Color.White) { go(-1) }
                PChip(pl.slideshow, slide) { slide = !slide }
                if (scale > 1f) PChip(pl.resetZoom) { scale = 1f; offset = Offset.Zero }
                Glyph("›", Color.White) { go(1) }
            }
        }
    }
}

// ------------------------------------------------------------------ video

@Composable
fun VideoOverlay(items: List<MItem>, start: Int, onClose: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    if (items.isEmpty()) { onClose(); return }
    var index by remember { mutableStateOf(start.coerceIn(0, items.size - 1)) }
    val item = items[index]
    val view = remember { arrayOfNulls<VideoView>(1) }
    val mpRef = remember { arrayOfNulls<android.media.MediaPlayer>(1) }
    var playing by remember(item.key) { mutableStateOf(false) }
    var dur by remember(item.key) { mutableStateOf(0) }
    var pos by remember(item.key) { mutableStateOf(0) }
    var failed by remember(item.key) { mutableStateOf(false) }
    var ui by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(MediaPrefs.speed) }
    val seek = remember(item.key) { SeekState() }

    fun applySpeed() {
        try { mpRef[0]?.let { if (it.isPlaying) it.playbackParams = PlaybackParams().setSpeed(speed) } } catch (e: Throwable) { }
    }
    fun saveNow() {
        try { view[0]?.let { MediaPrefs.savePos(item.key, it.currentPosition, dur) } } catch (e: Throwable) { }
    }
    fun go(d: Int) { saveNow(); index = (index + d + items.size) % items.size }
    fun finished() {
        MediaPrefs.savePos(item.key, 0, dur)
        when {
            MediaPrefs.repeat == 2 -> { mpRef[0]?.seekExact(0); view[0]?.start(); applySpeed(); playing = true }
            MediaPrefs.autoNext && items.size > 1 && (index + 1 < items.size || MediaPrefs.repeat == 1) -> go(1)
            else -> playing = false
        }
    }
    BackHandler { saveNow(); onClose() }
    DisposableEffect(item.key) { onDispose { saveNow(); try { view[0]?.stopPlayback() } catch (e: Throwable) { } } }
    LaunchedEffect(item.key) {
        while (true) {
            delay(300)
            val v = view[0]
            if (v != null && seek.drag == null) try { pos = v.currentPosition; playing = v.isPlaying } catch (e: Throwable) { }
        }
    }
    // hide the controls a few seconds after they were shown while playing
    LaunchedEffect(ui, playing) { if (ui && playing) { delay(4000); ui = false } }

    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        key(item.key) {
            AndroidView(
                factory = { c ->
                    VideoView(c).also { v ->
                        view[0] = v
                        v.keepScreenOn = MediaPrefs.keepScreenOn
                        v.setOnPreparedListener { mp ->
                            mpRef[0] = mp
                            dur = mp.duration
                            val saved = MediaPrefs.savedPos(item.key)
                            if (saved > 0 && saved < dur - 3000) { mp.seekExact(saved); pos = saved }
                            if (MediaPrefs.videoAutoPlay) { v.start(); playing = true; applySpeed() }
                        }
                        v.setOnCompletionListener { finished() }
                        v.setOnErrorListener { _, _, _ -> failed = true; true }
                        if (item.uri != null) v.setVideoURI(item.uri) else v.setVideoPath(item.path)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        // taps on the video toggle the controls
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { ui = !ui }) })
        if (failed) Text(pl.loadFail, color = Color.White, modifier = Modifier.padding(24.dp))
        if (ui) {
            Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { saveNow(); onClose() }) { Icon(Icons.Default.Close, pl.close, tint = Color.White) }
                Text(item.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (items.size > 1) Text("${index + 1}/${items.size}", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
            }
            Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99000000)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fmtClock(seek.shown(pos)), fontSize = 11.sp, color = Color.White)
                    SeekSlider(seek, pos, dur, { ms ->
                        val m = mpRef[0]
                        if (m != null) m.seekExact(ms) else view[0]?.seekTo(ms)
                        pos = ms
                    }, Modifier.weight(1f))
                    Text(fmtClock(dur), fontSize = 11.sp, color = Color.White)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    if (items.size > 1) Glyph("⏮", Color.White) { go(-1) }
                    Glyph("-10", Color.White) { val np = (pos - 10000).coerceAtLeast(0); mpRef[0]?.seekExact(np); pos = np }
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(26.dp)).background(p.accent.copy(alpha = 0.3f)).clickableScale(pressed = 0.9f) {
                        val v = view[0]
                        if (v != null) try {
                            if (v.isPlaying) { v.pause(); playing = false } else { v.start(); playing = true; applySpeed() }
                        } catch (e: Throwable) { }
                    }, contentAlignment = Alignment.Center) {
                        Icon(if (playing) AppIcons.Pause else Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                    Glyph("+10", Color.White) { val np = (pos + 10000).coerceAtMost(dur); mpRef[0]?.seekExact(np); pos = np }
                    if (items.size > 1) Glyph("⏭", Color.White) { go(1) }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PChip(pl.speed(speed), speed != 1f) {
                        speed = nextSpeed(speed)
                        MediaPrefs.speed = speed
                        MediaPrefs.save()
                        applySpeed()
                    }
                    PChip(pl.repeat(MediaPrefs.repeat), MediaPrefs.repeat != 0) { MediaPrefs.repeat = (MediaPrefs.repeat + 1) % 3; MediaPrefs.save() }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ------------------------------------------------------------------ settings

@Composable
private fun PSwitch(label: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.99f, ripple = false) { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (desc.isNotEmpty()) Text(desc, color = p.muted, fontSize = 12.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.45f),
                uncheckedThumbColor = p.muted, uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
            ),
        )
    }
}

@Composable
fun PlayerSettingsScreen(onBack: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    var msg by remember { mutableStateOf("") }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            msg = try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(MediaPrefs.exportJson().toByteArray(Charsets.UTF_8)) }
                pl.exported
            } catch (e: Exception) { pl.importFail }
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = try { ctx.contentResolver.openInputStream(uri)?.use { String(it.readBytes(), Charsets.UTF_8) } } catch (e: Exception) { null }
            msg = if (text != null && MediaPrefs.importJson(text)) pl.imported else pl.importFail
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(pl.settingsTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    PSwitch(pl.setAutoNext, pl.setAutoNextDesc, MediaPrefs.autoNext) { MediaPrefs.autoNext = it; MediaPrefs.save() }
                    PSwitch(pl.setShuffle, "", MediaPrefs.shuffle) { MediaPrefs.shuffle = it; MediaPrefs.save() }
                    PSwitch(pl.setRemember, pl.setRememberDesc, MediaPrefs.remember) { MediaPrefs.remember = it; MediaPrefs.save() }
                    PSwitch(pl.setBackground, pl.setBackgroundDesc, MediaPrefs.background) { MediaPrefs.background = it; MediaPrefs.save() }
                    PSwitch(pl.setVideoAuto, "", MediaPrefs.videoAutoPlay) { MediaPrefs.videoAutoPlay = it; MediaPrefs.save() }
                    PSwitch(pl.setKeepOn, "", MediaPrefs.keepScreenOn) { MediaPrefs.keepScreenOn = it; MediaPrefs.save() }
                    PSwitch(pl.subfolders, "", MediaPrefs.subfolders) { MediaPrefs.subfolders = it; MediaPrefs.save() }
                }
            }
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(pl.setRepeat, fontSize = 12.sp, color = p.muted)
                        AppDropdown(pl.setRepeat, MediaPrefs.repeat.toString(), listOf("0" to pl.repeat(0), "1" to pl.repeat(1), "2" to pl.repeat(2)), showLabel = false) {
                            MediaPrefs.repeat = it.toInt(); MediaPrefs.save()
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(pl.setSpeed, fontSize = 12.sp, color = p.muted)
                        AppDropdown(pl.setSpeed, MediaPrefs.speed.toString(), SPEEDS.map { it.toString() to pl.speed(it) }, showLabel = false) {
                            MediaPrefs.speed = it.toFloat(); MediaPrefs.save()
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(pl.setSlideshow, fontSize = 12.sp, color = p.muted)
                        AppDropdown(pl.setSlideshow, MediaPrefs.slideshowSec.toString(), listOf(2, 3, 4, 5, 8, 10, 15, 30).map { it.toString() to pl.seconds(it) }, showLabel = false) {
                            MediaPrefs.slideshowSec = it.toInt(); MediaPrefs.save()
                        }
                    }
                }
            }
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(pl.setBackup, fontSize = 12.sp, color = p.muted)
                    SecondaryButton(pl.exportBtn) { export.launch("furuarc-media-settings.json") }
                    SecondaryButton(pl.importBtn) { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                    SecondaryButton(pl.resetBtn) { MediaPrefs.reset(); msg = pl.resetDone }
                    if (msg.isNotEmpty()) Text(msg, color = p.muted, fontSize = 13.sp)
                }
            }
        }
    }
}
