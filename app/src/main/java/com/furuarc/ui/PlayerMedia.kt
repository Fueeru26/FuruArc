package com.furuarc.ui

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.media.AudioManager
import android.media.PlaybackParams
import android.os.SystemClock
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.furuarc.core.AudioEngine
import com.furuarc.core.MItem
import com.furuarc.core.MMeta
import com.furuarc.core.MediaLib
import com.furuarc.core.MediaMeta
import com.furuarc.core.MediaPrefs
import com.furuarc.core.seekExact
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

internal val SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

internal fun nextSpeed(cur: Float): Float = SPEEDS.firstOrNull { it > cur + 0.01f } ?: SPEEDS.first()

internal fun Context.findActivity(): android.app.Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is android.app.Activity) return c
        c = c.baseContext
    }
    return null
}

@Composable
internal fun Glyph(text: String, tint: Color, onClick: () -> Unit) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(23.dp)).clickableScale(pressed = 0.88f, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 18.sp, color = tint, fontWeight = FontWeight.Medium)
    }
}

/** Big round play / pause button. */
@Composable
private fun PlayButton(playing: Boolean, tint: Color, bg: Color, onClick: () -> Unit) {
    Box(Modifier.size(62.dp).clip(RoundedCornerShape(31.dp)).background(bg).clickableScale(pressed = 0.9f, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(if (playing) AppIcons.Pause else Icons.Default.PlayArrow, null, tint = tint, modifier = Modifier.size(38.dp))
    }
}

// ------------------------------------------------------------------ audio player tab

@Composable
internal fun AudioPane() {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val cur = AudioEngine.current
    if (cur == null) {
        EmptyState(AppIcons.MusicNote, pl.emptyPlayer(pl.audioWord), Modifier.fillMaxWidth().padding(top = 24.dp))
        return
    }
    val meta by produceState<MMeta?>(MediaMeta.cached(cur.key), cur.key) {
        value = withContext(Dispatchers.IO) { MediaMeta.load(ctx, cur, false, 720) }
    }
    val seek = remember(cur.key) { SeekState() }
    var now by remember { mutableStateOf(SystemClock.elapsedRealtime()) }
    var menu by remember { mutableStateOf(-1) }
    LaunchedEffect(AudioEngine.sleepAt) {
        while (AudioEngine.sleepAt != 0L) { now = SystemClock.elapsedRealtime(); delay(15000) }
    }
    val remainMin = if (AudioEngine.sleepAt == 0L) 0 else (((AudioEngine.sleepAt - now) / 60000L).toInt() + 1).coerceAtLeast(1)
    val title = (if (MediaPrefs.tagTitle) meta?.title else null) ?: cur.name
    val fav = MediaLib.isFav(2, cur)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.widthIn(max = 340.dp).fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(CardRadius)).background(p.iconBg),
            contentAlignment = Alignment.Center,
        ) {
            val art = meta?.art
            if (MediaPrefs.showCover && art != null) Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Icon(AppIcons.MusicNote, null, tint = p.iconFg, modifier = Modifier.fillMaxSize(0.4f))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
        meta?.artist?.let { Text(it, color = p.muted, fontSize = 15.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth()) }
        meta?.album?.let { Text(it, color = p.muted, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth()) }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(if (fav) AppIcons.Heart else AppIcons.HeartOutline, pl.favorites, if (fav) p.accent else p.text) {
                if (cur.uri == null) toastMsg(ctx, pl.tempCannotSave) else MediaLib.toggleFav(2, cur)
            }
            RoundIcon(AppIcons.InfoOutline, pl.infoSong, p.text) { menu = 1 }
            RoundIcon(AppIcons.PlaylistMusic, pl.addToPlaylist, p.text) { menu = 2 }
            RoundIcon(AppIcons.MoreHoriz, pl.menuTitleItem, p.text) { menu = 0 }
            Spacer(Modifier.weight(1f))
            RoundIcon(if (MediaPrefs.repeat == 2) AppIcons.RepeatOne else AppIcons.Repeat, pl.repeat(MediaPrefs.repeat), if (MediaPrefs.repeat != 0) p.accent else p.text) {
                MediaPrefs.repeat = (MediaPrefs.repeat + 1) % 3; MediaPrefs.save()
            }
            RoundIcon(AppIcons.Shuffle, pl.shuffle, if (MediaPrefs.shuffle) p.accent else p.text) { MediaPrefs.shuffle = !MediaPrefs.shuffle; MediaPrefs.save() }
        }
        if (AudioEngine.failed) {
            Text(pl.loadFail, color = Color(0xFFE57373), fontSize = 13.sp)
        } else if (AudioEngine.preparing) {
            Text(pl.loading, color = p.muted, fontSize = 13.sp)
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(fmtClock(seek.shown(AudioEngine.position)), fontSize = 12.sp, color = p.muted, modifier = Modifier.width(44.dp))
                SeekSlider(seek, AudioEngine.position, AudioEngine.duration, { AudioEngine.seek(it) }, Modifier.weight(1f))
                Text(fmtClock(AudioEngine.duration), fontSize = 12.sp, color = p.muted, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(AppIcons.SkipPrevious, "", p.text, 56, 36) { AudioEngine.previous() }
            PlayButton(AudioEngine.playing, p.accent, p.accent.copy(alpha = 0.2f)) { AudioEngine.toggle() }
            RoundIcon(AppIcons.SkipNext, "", p.text, 56, 36) { AudioEngine.next() }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PChip(pl.speed(MediaPrefs.speed), MediaPrefs.speed != 1f) { AudioEngine.setSpeed(nextSpeed(MediaPrefs.speed)) }
            PChip(pl.abLoop(AudioEngine.loopA, AudioEngine.loopB), AudioEngine.loopA != null) { AudioEngine.cycleLoop() }
            PChip(pl.sleep(remainMin), remainMin > 0) {
                val next = when { remainMin <= 0 -> 15; remainMin <= 15 -> 30; remainMin <= 30 -> 60; else -> 0 }
                AudioEngine.setSleep(next)
                now = SystemClock.elapsedRealtime()
            }
            PChip(pl.equalizer) {
                try {
                    val i = Intent(android.media.audiofx.AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                    i.putExtra(android.media.audiofx.AudioEffect.EXTRA_AUDIO_SESSION, AudioEngine.sessionId)
                    i.putExtra(android.media.audiofx.AudioEffect.EXTRA_CONTENT_TYPE, android.media.audiofx.AudioEffect.CONTENT_TYPE_MUSIC)
                    ctx.startActivity(i)
                } catch (e: Exception) { toastMsg(ctx, pl.noEqualizer) }
            }
            PChip(pl.stop) { AudioEngine.stop() }
        }
        Spacer(Modifier.height(8.dp))
    }
    if (menu >= 0) ItemMenu(2, cur, -1, menu) { menu = -1 }
}

// ------------------------------------------------------------------ video player tab / fullscreen

@Composable
internal fun VideoPane(fullscreen: Boolean = false) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    if (!fullscreen && PlayerHub.videoFull) return
    val items = PlayerHub.video
    val index = PlayerHub.videoIndex
    if (index !in items.indices) {
        EmptyState(AppIcons.Videocam, pl.emptyPlayer(pl.videoWord), Modifier.fillMaxWidth().padding(top = 24.dp))
        return
    }
    val item = items[index]
    val act = remember { ctx.findActivity() }
    val land = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val view = remember(item.key, fullscreen) { arrayOfNulls<VideoView>(1) }
    val mpRef = remember(item.key, fullscreen) { arrayOfNulls<android.media.MediaPlayer>(1) }
    var playing by remember(item.key, fullscreen) { mutableStateOf(false) }
    var dur by remember(item.key, fullscreen) { mutableStateOf(0) }
    var pos by remember(item.key, fullscreen) { mutableStateOf(0) }
    var failed by remember(item.key, fullscreen) { mutableStateOf(false) }
    var ui by remember { mutableStateOf(true) }
    var speed by remember { mutableStateOf(MediaPrefs.speed) }
    var hint by remember { mutableStateOf("") }
    var menu by remember { mutableStateOf(-1) }
    val seek = remember(item.key, fullscreen) { SeekState() }
    val skip = MediaPrefs.skipSec * 1000
    val fav = MediaLib.isFav(3, item)

    fun applySpeed() {
        try { mpRef[0]?.let { if (it.isPlaying) it.playbackParams = PlaybackParams().setSpeed(speed) } } catch (e: Throwable) { }
    }
    fun saveNow() {
        try { view[0]?.let { MediaPrefs.savePos(item.key, it.currentPosition, dur) } } catch (e: Throwable) { }
    }
    fun go(d: Int) { saveNow(); PlayerHub.videoIndex = (index + d + items.size) % items.size }
    fun seekTo(ms: Int) {
        val np = ms.coerceIn(0, dur.coerceAtLeast(0))
        val m = mpRef[0]
        if (m != null) m.seekExact(np) else view[0]?.seekTo(np)
        pos = np
    }
    fun togglePlay() {
        val v = view[0] ?: return
        try {
            if (v.isPlaying) { v.pause(); playing = false } else { v.start(); playing = true; applySpeed() }
        } catch (e: Throwable) { }
    }
    fun finished() {
        MediaPrefs.savePos(item.key, 0, dur)
        when {
            MediaPrefs.repeat == 2 -> { mpRef[0]?.seekExact(0); view[0]?.start(); applySpeed(); playing = true }
            MediaPrefs.autoNext && items.size > 1 && (index + 1 < items.size || MediaPrefs.repeat == 1) -> go(1)
            else -> playing = false
        }
    }
    fun setFull(on: Boolean) {
        saveNow()
        PlayerHub.videoResume = pos
        PlayerHub.videoFull = on
    }
    fun rotate() {
        try { act?.requestedOrientation = if (land) ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT else ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE } catch (e: Exception) { }
        if (!land && !fullscreen) setFull(true)
    }

    if (fullscreen) BackHandler { setFull(false) }
    DisposableEffect(item.key, fullscreen) { onDispose { saveNow(); try { view[0]?.stopPlayback() } catch (e: Throwable) { } } }
    LaunchedEffect(item.key, fullscreen) {
        while (true) {
            delay(300)
            val v = view[0]
            if (v != null && seek.drag == null) try { pos = v.currentPosition; playing = v.isPlaying } catch (e: Throwable) { }
        }
    }
    LaunchedEffect(ui, playing, fullscreen) { if (fullscreen && ui && playing) { delay(4000); ui = false } }
    LaunchedEffect(hint) { if (hint.isNotEmpty()) { delay(900); hint = "" } }

    val videoArea: @Composable (Modifier) -> Unit = { mod ->
        Box(mod.background(Color.Black), contentAlignment = Alignment.Center) {
            key(item.key) {
                AndroidView(
                    factory = { c ->
                        VideoView(c).also { v ->
                            view[0] = v
                            v.keepScreenOn = MediaPrefs.keepScreenOn
                            v.setOnPreparedListener { mp ->
                                mpRef[0] = mp
                                dur = mp.duration
                                val resume = PlayerHub.videoResume
                                PlayerHub.videoResume = 0
                                val saved = if (resume > 0) resume else MediaPrefs.savedPos(item.key)
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
            // taps and gestures on the picture
            Box(
                Modifier.fillMaxSize()
                    .pointerInput(MediaPrefs.doubleTap, skip) {
                        detectTapGestures(
                            onTap = { ui = !ui },
                            onDoubleTap = { off ->
                                if (MediaPrefs.doubleTap) {
                                    val w = size.width
                                    when {
                                        off.x < w / 3f -> { seekTo(pos - skip); hint = "-${MediaPrefs.skipSec}s" }
                                        off.x > w * 2f / 3f -> { seekTo(pos + skip); hint = "+${MediaPrefs.skipSec}s" }
                                        else -> togglePlay()
                                    }
                                } else togglePlay()
                            },
                        )
                    }
                    .pointerInput(MediaPrefs.swipeGestures) {
                        if (!MediaPrefs.swipeGestures) return@pointerInput
                        var left = true
                        var level = 0f
                        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        detectVerticalDragGestures(
                            onDragStart = { off ->
                                left = off.x < size.width / 2f
                                level = if (left) {
                                    val b = act?.window?.attributes?.screenBrightness ?: -1f
                                    if (b < 0f) 0.5f else b
                                } else {
                                    am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                                }
                            },
                        ) { change, dy ->
                            change.consumeAllChanges()
                            level = (level - dy / size.height.coerceAtLeast(1) * 1.4f).coerceIn(0f, 1f)
                            if (left) {
                                val w = act?.window
                                if (w != null) { val lp = w.attributes; lp.screenBrightness = level.coerceAtLeast(0.02f); w.attributes = lp }
                                hint = pl.brightness + " " + (level * 100).roundToInt() + "%"
                            } else {
                                val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                                try { am.setStreamVolume(AudioManager.STREAM_MUSIC, (level * max).roundToInt(), 0) } catch (e: Exception) { }
                                hint = pl.volume + " " + (level * 100).roundToInt() + "%"
                            }
                        }
                    },
            )
            if (failed) Text(pl.loadFail, color = Color.White, modifier = Modifier.padding(24.dp))
            if (hint.isNotEmpty()) {
                Text(hint, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color(0x99000000), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 8.dp))
            }
        }
    }

    val seekRow: @Composable (Color) -> Unit = { tint ->
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(fmtClock(seek.shown(pos)), fontSize = 12.sp, color = tint, modifier = Modifier.width(44.dp))
            SeekSlider(seek, pos, dur, { seekTo(it) }, Modifier.weight(1f))
            Text(fmtClock(dur), fontSize = 12.sp, color = tint, modifier = Modifier.width(44.dp), textAlign = TextAlign.End)
        }
    }
    val controlRow: @Composable (Color) -> Unit = { tint ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            if (items.size > 1) RoundIcon(AppIcons.SkipPrevious, "", tint, 52, 32) { go(-1) }
            Glyph("-${MediaPrefs.skipSec}", tint) { seekTo(pos - skip) }
            PlayButton(playing, tint, p.accent.copy(alpha = if (fullscreen) 0.35f else 0.2f)) { togglePlay() }
            Glyph("+${MediaPrefs.skipSec}", tint) { seekTo(pos + skip) }
            if (items.size > 1) RoundIcon(AppIcons.SkipNext, "", tint, 52, 32) { go(1) }
        }
    }
    val chipRow: @Composable () -> Unit = {
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PChip(pl.speed(speed), speed != 1f) {
                speed = nextSpeed(speed)
                MediaPrefs.speed = speed
                MediaPrefs.save()
                applySpeed()
            }
            PChip(pl.repeat(MediaPrefs.repeat), MediaPrefs.repeat != 0) { MediaPrefs.repeat = (MediaPrefs.repeat + 1) % 3; MediaPrefs.save() }
        }
    }

    if (fullscreen) {
        Box(Modifier.fillMaxSize().background(Color.Black)) {
            videoArea(Modifier.fillMaxSize())
            if (ui) {
                Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RoundIcon(AppIcons.FullscreenExit, pl.exitFullscreen, Color.White) { setFull(false) }
                    Text(item.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 6.dp))
                    RoundIcon(AppIcons.ScreenRotation, pl.rotate, Color.White) { rotate() }
                }
                Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0x99000000)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    seekRow(Color.White)
                    controlRow(Color.White)
                    chipRow()
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        videoArea(Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(CardRadius)))
        Spacer(Modifier.height(10.dp))
        Text(item.name, color = p.text, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth())
        Text("${index + 1} / ${items.size}", color = p.muted, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundIcon(if (fav) AppIcons.Heart else AppIcons.HeartOutline, pl.favorites, if (fav) p.accent else p.text) {
                if (item.uri == null) toastMsg(ctx, pl.tempCannotSave) else MediaLib.toggleFav(3, item)
            }
            RoundIcon(AppIcons.InfoOutline, pl.infoFile, p.text) { menu = 1 }
            RoundIcon(AppIcons.PlaylistMusic, pl.addToPlaylist, p.text) { menu = 2 }
            RoundIcon(AppIcons.MoreHoriz, pl.menuTitleItem, p.text) { menu = 0 }
            Spacer(Modifier.weight(1f))
            RoundIcon(AppIcons.ScreenRotation, pl.rotate, p.text) { rotate() }
            RoundIcon(AppIcons.Fullscreen, pl.fullscreen, p.text) { setFull(true) }
        }
        seekRow(p.muted)
        controlRow(p.text)
        Spacer(Modifier.height(8.dp))
        chipRow()
        Spacer(Modifier.height(8.dp))
    }
    if (menu >= 0) ItemMenu(3, item, -1, menu) { menu = -1 }
}
