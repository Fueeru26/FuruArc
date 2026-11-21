package com.furuarc.ui

import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.Engine
import com.furuarc.core.AudioEngine
import com.furuarc.core.MItem
import com.furuarc.core.MList
import com.furuarc.core.MMeta
import com.furuarc.core.MediaLib
import com.furuarc.core.MediaMeta
import com.furuarc.core.MediaPrefs
import com.furuarc.core.MediaScan
import com.furuarc.i18n.PlayerStr
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lists of the Media Player screens (kept while the app is open) and the hand-off used by "Open with" and Game Assets.
 * Kinds: 1 = images (the gallery list), 2 = audio (the queue), 3 = video (the queue).
 */
object PlayerHub {
    var image by mutableStateOf<List<MItem>>(emptyList())
    var audio by mutableStateOf<List<MItem>>(emptyList())
    var video by mutableStateOf<List<MItem>>(emptyList())
    var label by mutableStateOf<Map<Int, String>>(emptyMap())
    val tree = HashMap<Int, Uri>()

    /** Index of the video shown in the Player tab (-1 = none). */
    var videoIndex by mutableStateOf(-1)
    var videoFull by mutableStateOf(false)
    /** Position (ms) the next video start resumes from (used when the video view is rebuilt, e.g. fullscreen). */
    var videoResume = 0

    /** Selected tab per kind, kept while the app is open. */
    val tab = HashMap<Int, Int>()

    /** (kind, index) opened automatically by the list screen of that kind; set by "Open with", Game Assets etc. */
    var autoOpen by mutableStateOf<Pair<Int, Int>?>(null)

    /** Set by MainActivity: switches to the Media Player screen of a kind (1 image, 2 audio, 3 video). */
    var navigate: ((Int) -> Unit)? = null

    /** Set by MainActivity: opens a file in File Inspection. */
    var openInspect: ((Uri) -> Unit)? = null

    private var inited = false

    fun init(ctx: android.content.Context) {
        if (inited) return
        inited = true
        MediaPrefs.init(ctx)
        MediaLib.init(ctx)
        if (MediaPrefs.restoreQueue) {
            for (k in 1..3) {
                if (items(k).isNotEmpty()) continue
                val (l, lb) = MediaLib.loadQueue(k)
                if (l.isNotEmpty()) setRaw(k, l, lb)
            }
        }
    }

    fun items(kind: Int): List<MItem> = when (kind) { 1 -> image; 2 -> audio; else -> video }

    private fun setRaw(kind: Int, list: List<MItem>, text: String) {
        when (kind) { 1 -> image = list; 2 -> audio = list; else -> video = list }
        label = label + (kind to text)
    }

    /** Replaces the list of a kind (a folder, playlist ...). */
    fun set(kind: Int, list: List<MItem>, text: String) {
        init(Engine.app)
        stash.remove(kind)
        if (kind == 2) AudioEngine.stop()
        setRaw(kind, list, text)
        if (kind == 3) videoIndex = -1
        MediaLib.saveQueue(kind, list, text)
    }

    /** "Muat" (replace) or "Tambahkan" (append, without duplicates) a folder / playlist / collection to the gallery or queue. */
    fun apply(kind: Int, list: List<MItem>, text: String, add: Boolean) {
        init(Engine.app)
        val cur = items(kind)
        if (!add || cur.isEmpty()) { set(kind, list, text); return }
        val have = cur.map { it.key }.toHashSet()
        update(kind, cur + list.filter { have.add(it.key) })
    }

    /** Replaces the list after reordering / removing / adding, keeping the playing item. */
    fun update(kind: Int, list: List<MItem>) {
        val old = items(kind)
        if (list.isEmpty()) {
            // an empty queue also empties the player
            if (kind == 2) AudioEngine.stop()
            if (kind == 3) videoIndex = -1
        }
        when (kind) {
            2 -> {
                val same = AudioEngine.queue === old
                setRaw(kind, list, label[kind] ?: "")
                if (same) AudioEngine.updateQueue(list)
            }
            3 -> {
                val key = old.getOrNull(videoIndex)?.key
                setRaw(kind, list, label[kind] ?: "")
                videoIndex = if (key == null) -1 else list.indexOfFirst { it.key == key }
            }
            else -> setRaw(kind, list, label[kind] ?: "")
        }
        if (!isTemp(kind)) MediaLib.saveQueue(kind, list, label[kind] ?: "")
    }

    // ---- temporary lists ("Open with…", Game Assets): the stored gallery / queue is stashed and restored afterwards
    private class Stash(val items: List<MItem>, val label: String, val videoIndex: Int)
    private val stash = HashMap<Int, Stash>()

    fun isTemp(kind: Int): Boolean = stash.containsKey(kind)

    /** Shows [list] temporarily: nothing is saved, and the previous list comes back with [endTemp]. */
    fun openTemp(kind: Int, list: List<MItem>) {
        init(Engine.app)
        if (!stash.containsKey(kind)) stash[kind] = Stash(items(kind), label[kind] ?: "", videoIndex)
        if (kind == 2) AudioEngine.stop()
        setRaw(kind, list, "")
        if (kind == 3) videoIndex = -1
        tree.remove(kind)
        tab[kind] = 0
        autoOpen = kind to 0
    }

    /** Restores the list that was replaced by [openTemp] (does nothing when the list is not temporary). */
    fun endTemp(kind: Int) {
        val st = stash.remove(kind) ?: return
        setRaw(kind, st.items, st.label)
        if (kind == 3) videoIndex = -1
    }

    /** Puts one item temporarily into the list of its kind and asks the list screen to open it. */
    fun openSingle(kind: Int, item: MItem, text: String) = openTemp(kind, listOf(item))
}

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

internal fun sizeText(b: Long): String = when {
    b >= 1048576L -> "%.1f MB".format(b / 1048576.0)
    b >= 1024L -> "%.0f KB".format(b / 1024.0)
    else -> "$b B"
}

internal fun toastMsg(ctx: android.content.Context, msg: String) {
    try { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() } catch (e: Exception) { }
}

internal fun listTitle(pl: PlayerStr, l: MList): String = if (l.id == MediaLib.FAV) pl.favorites else l.name

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

// ------------------------------------------------------------------ shared small pieces

@Composable
internal fun PChip(text: String, active: Boolean = false, onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(ControlRadius)
    Box(
        Modifier.clip(shape).background(if (active) p.accent.copy(alpha = 0.2f) else p.trailingBg)
            .border(1.dp, if (active) p.accent else p.cardBorder, shape)
            .clickableScale(pressed = 0.94f, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) { Text(text, fontSize = 13.sp, color = if (active) p.accent else p.text, maxLines = 1) }
}

/** Round icon button (no ripple clutter, press animation). */
@Composable
internal fun RoundIcon(icon: ImageVector, desc: String, tint: Color, size: Int = 44, iconSize: Int = 24, bg: Color = Color.Transparent, onClick: () -> Unit) {
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size / 2).dp)).background(bg).clickableScale(pressed = 0.88f, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, desc, tint = tint, modifier = Modifier.size(iconSize.dp)) }
}

@Composable
private fun TabBar(icons: List<ImageVector>, descs: List<String>, sel: Int, onSel: (Int) -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(ControlRadius)
    Row(
        Modifier.fillMaxWidth().clip(shape).background(p.trailingBg).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icons.forEachIndexed { i, ic ->
            val on = i == sel
            Box(
                Modifier.weight(1f).height(44.dp).clip(shape)
                    .background(if (on) p.accent.copy(alpha = 0.22f) else Color.Transparent)
                    .clickableScale(pressed = 0.94f, ripple = false) { onSel(i) },
                contentAlignment = Alignment.Center,
            ) { Icon(ic, descs[i], tint = if (on) p.accent else p.muted, modifier = Modifier.size(24.dp)) }
        }
    }
}

@Composable
internal fun OptionsDialog(title: String, onDismiss: () -> Unit, options: List<Pair<String, () -> Unit>>) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    AppDialog(title, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            for ((t, a) in options) {
                Text(
                    t, color = p.text, fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { a() }.padding(vertical = 13.dp),
                )
            }
        }
    }
}

@Composable
internal fun NameDialog(title: String, label: String, initial: String, okText: String, onDismiss: () -> Unit, onOk: (String) -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    var t by remember { mutableStateOf(initial) }
    AppDialog(
        title, onDismiss,
        buttons = {
            TextButton(onClick = onDismiss) { Text(pl.cancel, color = p.muted) }
            TextButton(onClick = { onOk(t.trim()) }, enabled = t.isNotBlank()) { Text(okText, color = if (t.isNotBlank()) p.accent else p.muted) }
        },
    ) { AppTextField(t, { t = it }, label) }
}

@Composable
private fun InfoDialog(kind: Int, item: MItem, onDismiss: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val meta by produceState<MMeta?>(MediaMeta.cached(item.key), item.key) {
        if (kind != 1 && value == null) value = withContext(Dispatchers.IO) { MediaMeta.load(ctx, item, kind == 3) }
    }
    val dim by produceState("", item.key) {
        if (kind == 1) value = withContext(Dispatchers.IO) { imageDimensions(ctx, item) }
    }
    val rows = ArrayList<Pair<String, String>>()
    rows.add(pl.infoName to item.name)
    meta?.let { m ->
        m.title?.let { rows.add(pl.infoTitle to it) }
        m.artist?.let { rows.add(pl.infoArtist to it) }
        m.album?.let { rows.add(pl.infoAlbum to it) }
        if (m.durationMs > 0) rows.add(pl.infoDuration to fmtClock(m.durationMs))
    }
    if (dim.isNotEmpty()) rows.add(pl.infoDim to dim)
    if (item.size > 0) rows.add(pl.infoSize to sizeText(item.size))
    val ext = MediaScan.ext(item.name)
    if (ext.isNotEmpty()) rows.add(pl.infoFormat to ext.uppercase())
    val loc = item.uri?.let { Uri.decode(it.toString()) } ?: item.path
    if (loc != null) rows.add(pl.infoLocation to loc)
    AppDialog(if (kind == 2) pl.infoSong else pl.infoFile, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for ((k, v) in rows) {
                Column {
                    Text(k, color = p.muted, fontSize = 12.sp)
                    Text(v, color = p.text, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun AddToListDialog(kind: Int, item: MItem, onDismiss: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    var newDlg by remember { mutableStateOf(false) }
    val lists = MediaLib.listsOf(kind)
    fun addTo(l: MList) {
        val n = MediaLib.addItems(kind, l.id, listOf(item))
        toastMsg(ctx, if (n > 0) pl.addedTo(n, listTitle(pl, l)) else pl.alreadyIn(listTitle(pl, l)))
        onDismiss()
    }
    if (newDlg) {
        NameDialog(
            if (kind == 1) pl.addCollection else pl.addPlaylist, if (kind == 1) pl.collectionName else pl.playlistName, "", pl.add,
            { newDlg = false }, { name ->
                val id = MediaLib.createList(kind, name)
                MediaLib.listsOf(kind).firstOrNull { it.id == id }?.let { addTo(it) }
            },
        )
        return
    }
    AppDialog(pl.chooseList, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            for (l in lists) {
                Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { addTo(l) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (l.id == MediaLib.FAV) AppIcons.Heart else if (kind == 1) AppIcons.Collections else AppIcons.PlaylistMusic, null, tint = p.accent, modifier = Modifier.size(22.dp))
                    Text(listTitle(pl, l), color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(l.items.size.toString(), color = p.muted, fontSize = 13.sp)
                }
            }
            Text(pl.newList, color = p.accent, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { newDlg = true }.padding(vertical = 13.dp))
        }
    }
}

/**
 * The "⋯" window of an item: info, favorites, lists, share, File Inspection and (inside a queue / gallery) remove / play next.
 * [index] is the position in the list of [kind] when the item comes from it, otherwise -1.
 */
@Composable
internal fun ItemMenu(kind: Int, item: MItem, index: Int, startSub: Int = 0, onDismiss: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val ctx = LocalContext.current
    var sub by remember { mutableStateOf(startSub) }
    when (sub) {
        1 -> { InfoDialog(kind, item, onDismiss); return }
        2 -> { AddToListDialog(kind, item, onDismiss); return }
    }
    val opts = ArrayList<Pair<String, () -> Unit>>()
    opts.add((if (kind == 2) pl.infoSong else pl.infoFile) to { sub = 1 })
    val items = PlayerHub.items(kind)
    if (index in items.indices) {
        if (kind != 1) opts.add(pl.playNext to {
            val cur = when (kind) {
                2 -> if (AudioEngine.queue === items) AudioEngine.index else -1
                else -> PlayerHub.videoIndex
            }
            val m = items.toMutableList()
            val moved = m.removeAt(index)
            var at = if (cur < 0) 0 else cur + 1
            if (index < cur) at -= 1
            m.add(at.coerceIn(0, m.size), moved)
            PlayerHub.update(kind, m)
            onDismiss()
        })
        opts.add((if (kind == 1) pl.removeFromGallery else pl.removeFromQueue) to {
            val m = items.toMutableList()
            m.removeAt(index)
            PlayerHub.update(kind, m)
            onDismiss()
        })
    }
    val fav = MediaLib.isFav(kind, item)
    opts.add((if (fav) pl.removeFav else pl.addFav) to {
        if (item.uri == null) toastMsg(ctx, pl.tempCannotSave) else {
            val now = MediaLib.toggleFav(kind, item)
            toastMsg(ctx, if (now) pl.addedTo(1, pl.favorites) else pl.removeFav)
        }
        onDismiss()
    })
    opts.add((if (kind == 1) pl.addToCollection else pl.addToPlaylist) to {
        if (item.uri == null) { toastMsg(ctx, pl.tempCannotSave); onDismiss() } else sub = 2
    })
    if (item.uri != null) {
        opts.add(pl.share to {
            try {
                val mime = when (kind) { 1 -> "image/*"; 2 -> "audio/*"; else -> "video/*" }
                val i = Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM, item.uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                ctx.startActivity(Intent.createChooser(i, pl.share).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) { }
            onDismiss()
        })
        opts.add(pl.openInspect to { onDismiss(); PlayerHub.openInspect?.invoke(item.uri); Unit })
    }
    OptionsDialog(item.name, onDismiss, opts)
}

// ------------------------------------------------------------------ screen

@Composable
fun PlayerListScreen(kind: Int, onBack: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val ctx = LocalContext.current
    remember { PlayerHub.init(ctx); true }
    val isImg = kind == 1
    val queueTab = if (isImg) 0 else 1
    val folderTab = if (isImg) 1 else 2
    val listTab = if (isImg) 2 else 3
    var tab by remember { mutableStateOf((PlayerHub.tab[kind] ?: 0).coerceIn(0, listTab)) }
    var viewer by remember { mutableStateOf(-1) }
    fun goTab(t: Int) { tab = t; PlayerHub.tab[kind] = t }
    val title = when (kind) { 1 -> pl.imageTitle; 2 -> pl.audioTitle; else -> pl.videoTitle }

    DisposableEffect(kind) {
        onDispose {
            if (kind == 2 && !MediaPrefs.background) AudioEngine.stop()
            PlayerHub.endTemp(kind)
            if (kind == 3) {
                PlayerHub.videoFull = false
                val a = ctx.findActivity()
                try { a?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED } catch (e: Exception) { }
                try { a?.window?.let { w -> val lp = w.attributes; lp.screenBrightness = -1f; w.attributes = lp } } catch (e: Exception) { }
            }
        }
    }
    LaunchedEffect(kind) {
        if (kind == 3 && MediaPrefs.videoOrient != 0) {
            try {
                ctx.findActivity()?.requestedOrientation =
                    if (MediaPrefs.videoOrient == 1) android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            } catch (e: Exception) { }
        }
    }
    val auto = PlayerHub.autoOpen
    LaunchedEffect(auto) {
        val a = auto ?: return@LaunchedEffect
        if (a.first != kind) return@LaunchedEffect
        PlayerHub.autoOpen = null
        goTab(0)
        when (kind) {
            2 -> AudioEngine.setQueue(PlayerHub.audio, a.second)
            3 -> PlayerHub.videoIndex = a.second
            else -> viewer = a.second
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            ScreenHeader(title, onBack)
            if (isImg) {
                TabBar(listOf(AppIcons.Image, AppIcons.Folder, AppIcons.Collections), listOf(pl.tabGallery, pl.tabFolder, pl.tabCollection), tab) { goTab(it) }
            } else {
                TabBar(
                    listOf(Icons.Default.PlayArrow, AppIcons.QueueMusic, AppIcons.Folder, AppIcons.PlaylistMusic),
                    listOf(pl.tabPlayer, pl.tabQueue, pl.tabFolder, pl.tabPlaylist), tab,
                ) { goTab(it) }
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    isImg && tab == 0 -> GalleryTab { viewer = it }
                    !isImg && tab == 0 -> if (kind == 2) AudioPane() else VideoPane()
                    !isImg && tab == 1 -> QueueTab(kind) { goTab(0) }
                    tab == folderTab -> FolderTab(kind) { goTab(queueTab) }
                    else -> ListsTab(kind) { goTab(queueTab) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        if (viewer >= 0 && isImg) ImageViewer(PlayerHub.image, viewer) { viewer = -1 }
        if (kind == 3 && PlayerHub.videoFull && PlayerHub.videoIndex in PlayerHub.video.indices) VideoPane(fullscreen = true)
    }
}

// ------------------------------------------------------------------ selection mode (queue and gallery)

/** Window to pick a playlist / collection (Favorites is not offered) for the selected items. */
@Composable
internal fun PickListDialog(kind: Int, onDismiss: () -> Unit, onPick: (MList) -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    var newDlg by remember { mutableStateOf(false) }
    if (newDlg) {
        NameDialog(
            if (kind == 1) pl.addCollection else pl.addPlaylist, if (kind == 1) pl.collectionName else pl.playlistName, "", pl.add,
            { newDlg = false },
        ) { name ->
            val id = MediaLib.createList(kind, name)
            MediaLib.listsOf(kind).firstOrNull { it.id == id }?.let { onPick(it) }
        }
        return
    }
    val lists = MediaLib.listsOf(kind).filter { it.id != MediaLib.FAV }
    AppDialog(pl.chooseList, onDismiss, buttons = { TextButton(onClick = onDismiss) { Text(pl.close, color = p.accent) } }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            for (l in lists) {
                Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { onPick(l) }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (kind == 1) AppIcons.Collections else AppIcons.PlaylistMusic, null, tint = p.accent, modifier = Modifier.size(22.dp))
                    Text(l.name, color = p.text, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 12.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(l.items.size.toString(), color = p.muted, fontSize = 13.sp)
                }
            }
            Text(pl.newList, color = p.accent, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth().clickableScale(pressed = 0.98f) { newDlg = true }.padding(vertical = 13.dp))
        }
    }
}

/** Same layout as the Game Assets selection bar: count, select all / clear, then Favorites, list and delete icons. */
@Composable
internal fun SelectionBar(kind: Int, count: Int, onAll: () -> Unit, onClear: () -> Unit, onFav: () -> Unit, onList: () -> Unit, onDelete: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val red = Color(0xFFE53935)
    val shape = RoundedCornerShape(ControlRadius)
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(pl.selected(count), fontWeight = FontWeight.SemiBold, color = p.text, modifier = Modifier.weight(1f))
                TextButton(onClick = onAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) { Text(pl.selAll, fontSize = 13.sp, color = p.accent, maxLines = 1) }
                TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) { Text(pl.selNone, fontSize = 13.sp, color = p.text, maxLines = 1) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val on = count > 0
                val alpha = if (on) 1f else 0.35f
                Box(Modifier.weight(1f).height(44.dp).clip(shape).background(p.trailingBg).then(if (on) Modifier.clickableScale(pressed = 0.94f, onClick = onFav) else Modifier), contentAlignment = Alignment.Center) {
                    Icon(AppIcons.Heart, pl.addFav, tint = p.accent.copy(alpha = alpha), modifier = Modifier.size(24.dp))
                }
                Box(Modifier.weight(1f).height(44.dp).clip(shape).background(p.trailingBg).then(if (on) Modifier.clickableScale(pressed = 0.94f, onClick = onList) else Modifier), contentAlignment = Alignment.Center) {
                    Icon(if (kind == 1) AppIcons.Collections else AppIcons.PlaylistMusic, if (kind == 1) pl.addToCollection else pl.addToPlaylist, tint = p.text.copy(alpha = alpha), modifier = Modifier.size(24.dp))
                }
                Box(Modifier.weight(1f).height(44.dp).clip(shape).background(p.trailingBg).then(if (on) Modifier.clickableScale(pressed = 0.94f, onClick = onDelete) else Modifier), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Delete, pl.removeSel, tint = red.copy(alpha = alpha), modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/** Adds the chosen items to a list and reports the result. */
internal fun addChosen(ctx: android.content.Context, pl: PlayerStr, kind: Int, chosen: List<MItem>, l: MList) {
    val n = MediaLib.addItems(kind, l.id, chosen)
    toastMsg(ctx, if (n > 0) pl.addedTo(n, listTitle(pl, l)) else pl.alreadyIn(listTitle(pl, l)))
}

// ------------------------------------------------------------------ queue (audio / video)

@Composable
private fun QueueTab(kind: Int, onPlayVideo: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val items = PlayerHub.items(kind)
    val size = MediaPrefs.pageQueue
    val pages = ((items.size + size - 1) / size).coerceAtLeast(1)
    val curIdx = if (kind == 2 && AudioEngine.queue === items) AudioEngine.index else if (kind == 3) PlayerHub.videoIndex else -1
    var page by remember { mutableStateOf(if (curIdx >= 0) curIdx / size else 0) }
    val pg = page.coerceIn(0, pages - 1)
    val from = pg * size
    val to = minOf(items.size, from + size)
    var menu by remember { mutableStateOf(-1) }
    var selMode by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var pickList by remember { mutableStateOf(false) }
    BackHandler(enabled = selMode) { selMode = false; sel = emptySet() }

    fun move(a: Int, b: Int) {
        if (a !in items.indices || b !in items.indices) return
        val m = items.toMutableList()
        val x = m[a]; m[a] = m[b]; m[b] = x
        PlayerHub.update(kind, m)
    }
    fun chosen(): List<MItem> = sel.sorted().mapNotNull { items.getOrNull(it) }
    fun endSel() { selMode = false; sel = emptySet() }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val lb = PlayerHub.label[kind] ?: ""
            Text(
                if (items.isNotEmpty()) (if (lb.isNotEmpty()) "$lb · " else "") + pl.pageInfo(items.size) else "",
                color = p.muted, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
            )
            if (items.isNotEmpty()) RoundIcon(AppIcons.Checklist, pl.selMode, if (selMode) p.accent else p.text, 40, 24) { if (selMode) endSel() else selMode = true }
        }
        if (selMode) {
            Spacer(Modifier.height(4.dp))
            SelectionBar(
                kind, sel.size, { sel = items.indices.toSet() }, { endSel() },
                {
                    val c = chosen()
                    val n = MediaLib.addItems(kind, MediaLib.FAV, c)
                    toastMsg(ctx, if (n > 0) pl.addedTo(n, pl.favorites) else pl.alreadyIn(pl.favorites))
                    endSel()
                },
                { pickList = true },
                {
                    PlayerHub.update(kind, items.filterIndexed { i, _ -> i !in sel })
                    endSel()
                },
            )
            Spacer(Modifier.height(6.dp))
        }
        if (items.isEmpty()) {
            EmptyState(if (kind == 2) AppIcons.MusicNote else AppIcons.Videocam, pl.emptyQueue, Modifier.fillMaxWidth().padding(top = 24.dp))
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(items.subList(from, to), key = { l, it -> (from + l).toString() + it.key }) { l, it ->
                    val gi = from + l
                    val active = if (kind == 2) AudioEngine.current?.key == it.key && AudioEngine.queue === items else PlayerHub.videoIndex == gi
                    QueueRow(
                        kind, it, active, selMode, gi in sel,
                        onClick = {
                            if (selMode) {
                                sel = if (gi in sel) sel - gi else sel + gi
                            } else if (kind == 2) {
                                if (AudioEngine.queue === items) AudioEngine.playIndex(gi) else AudioEngine.setQueue(items, gi)
                            } else {
                                PlayerHub.videoIndex = gi
                                onPlayVideo()
                            }
                        },
                        onUp = if (gi > 0) ({ move(gi, gi - 1) }) else null,
                        onDown = if (gi < items.size - 1) ({ move(gi, gi + 1) }) else null,
                        onMenu = { menu = gi },
                    )
                }
            }
            if (pages > 1) {
                Spacer(Modifier.height(8.dp))
                PageNav(pg, pages, { page = it })
            }
            if (kind == 2 && AudioEngine.current != null) {
                Spacer(Modifier.height(8.dp))
                MiniBar()
            }
        }
    }
    items.getOrNull(menu)?.let { ItemMenu(kind, it, menu) { menu = -1 } }
    if (pickList) PickListDialog(kind, { pickList = false }) { l -> addChosen(ctx, pl, kind, chosen(), l); pickList = false; endSel() }
}

@Composable
private fun QueueRow(kind: Int, item: MItem, active: Boolean, selMode: Boolean, selected: Boolean, onClick: () -> Unit, onUp: (() -> Unit)?, onDown: (() -> Unit)?, onMenu: () -> Unit) {
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val meta by produceState<MMeta?>(MediaMeta.cached(item.key), item.key) {
        if (value == null) value = withContext(Dispatchers.IO) { MediaMeta.load(ctx, item, kind == 3) }
    }
    val title = (if (MediaPrefs.tagTitle) meta?.title else null) ?: item.name
    val sub2 = meta?.album ?: if (item.size > 0) sizeText(item.size) else ""
    val dur = meta?.durationMs ?: 0
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().background(if (selected) p.accent.copy(alpha = 0.18f) else if (active) p.accent.copy(alpha = 0.12f) else Color.Transparent).cardClickable(onClick).padding(vertical = 8.dp, horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selMode) {
                Box(Modifier.width(30.dp), contentAlignment = Alignment.Center) {
                    Checkbox(checked = selected, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                }
            } else {
                Column(Modifier.width(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(30.dp, 26.dp).then(if (onUp != null) Modifier.clickableScale(pressed = 0.8f, onClick = onUp) else Modifier), contentAlignment = Alignment.Center) {
                        Icon(AppIcons.ArrowUp, null, tint = if (onUp != null) p.text else p.muted.copy(alpha = 0.25f), modifier = Modifier.size(24.dp))
                    }
                    Box(Modifier.size(30.dp, 26.dp).then(if (onDown != null) Modifier.clickableScale(pressed = 0.8f, onClick = onDown) else Modifier), contentAlignment = Alignment.Center) {
                        Icon(AppIcons.ArrowDown, null, tint = if (onDown != null) p.text else p.muted.copy(alpha = 0.25f), modifier = Modifier.size(24.dp))
                    }
                }
            }
            Box(Modifier.padding(start = 4.dp).size(54.dp).clip(RoundedCornerShape(IconRadius)).background(p.iconBg), contentAlignment = Alignment.Center) {
                val art = meta?.art
                if (MediaPrefs.showCover && art != null) Image(art, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                else Icon(if (kind == 2) AppIcons.MusicNote else AppIcons.Videocam, null, tint = if (active) p.accent else p.iconFg, modifier = Modifier.size(26.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                Text(title, color = if (active) p.accent else p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                meta?.artist?.let { Text(it, color = if (active) p.accent else p.text, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(sub2, color = p.muted, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    if (dur > 0) Text(fmtClock(dur), color = p.muted, fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp))
                }
            }
            if (!selMode) RoundIcon(AppIcons.MoreHoriz, "", p.text, 40, 22, p.trailingBg, onMenu)
        }
    }
}

@Composable
private fun MiniBar() {
    val p = LocalPalette.current
    val cur = AudioEngine.current ?: return
    val meta = MediaMeta.cached(cur.key)
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(meta?.title ?: cur.name, color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            RoundIcon(AppIcons.SkipPrevious, "", p.text) { AudioEngine.previous() }
            RoundIcon(if (AudioEngine.playing) AppIcons.Pause else Icons.Default.PlayArrow, "", p.accent) { AudioEngine.toggle() }
            RoundIcon(AppIcons.SkipNext, "", p.text) { AudioEngine.next() }
        }
    }
}

// ------------------------------------------------------------------ folders

@Composable
internal fun FolderTab(kind: Int, onLoaded: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val folders = MediaLib.folders[kind] ?: emptyList()
    var scanning by remember { mutableStateOf(false) }
    var scanCount by remember { mutableStateOf(0) }
    var job by remember { mutableStateOf<Job?>(null) }
    var menu by remember { mutableStateOf<String?>(null) }
    val mime = when (kind) { 1 -> "image/*"; 2 -> "audio/*"; else -> "video/*" }

    var pending by remember { mutableStateOf<Pair<List<MItem>, String>?>(null) }
    var addMode by remember { mutableStateOf(false) }

    fun load(uri: String, name: String, add: Boolean = false) {
        addMode = add
        job?.cancel()
        job = scope.launch {
            scanning = true
            scanCount = 0
            try {
                val r = withContext(Dispatchers.IO) { MediaScan.scanTree(ctx, Uri.parse(uri), kind, MediaPrefs.subfolders) { scanCount = it } }
                if (r.isEmpty()) toastMsg(ctx, pl.nothingFound) else {
                    if (addMode) {
                        PlayerHub.apply(kind, r, name, true)
                        onLoaded()
                    } else pending = r to name
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
            } finally {
                scanning = false
            }
        }
    }

    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            val name = uri.lastPathSegment?.substringAfterLast(':')?.substringAfterLast('/')?.ifEmpty { null } ?: pl.tabFolder
            MediaLib.addFolder(kind, uri, name)
        }
    }
    val pickFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            for (u in uris) try { ctx.contentResolver.takePersistableUriPermission(u, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            scope.launch {
                val list = withContext(Dispatchers.IO) { uris.map { itemOfUri(ctx, it) } }
                PlayerHub.set(kind, PlayerHub.items(kind) + list, PlayerHub.label[kind] ?: "")
                onLoaded()
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { PrimaryButton(pl.addFolder, !scanning) { pickFolder.launch(null) } }
            Box(Modifier.weight(1f)) { SecondaryButton(pl.addFiles, !scanning) { pickFiles.launch(arrayOf(mime, "application/octet-stream")) } }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.98f, ripple = false) { MediaPrefs.subfolders = !MediaPrefs.subfolders; MediaPrefs.save() }, verticalAlignment = Alignment.CenterVertically) {
            Text(pl.subfolders, color = p.text, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Switch(
                checked = MediaPrefs.subfolders, onCheckedChange = { MediaPrefs.subfolders = it; MediaPrefs.save() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.45f),
                    uncheckedThumbColor = p.muted, uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
                ),
            )
        }
        if (scanning) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                CircularProgressIndicator(Modifier.size(18.dp), color = p.accent, strokeWidth = 2.dp)
                Spacer(Modifier.width(10.dp))
                Text(pl.scanning + " " + pl.found(scanCount), color = p.muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { job?.cancel(); scanning = false }) { Text("✕", color = p.accent) }
            }
        }
        Spacer(Modifier.height(6.dp))
        if (folders.isEmpty()) {
            EmptyState(AppIcons.Folder, pl.emptyFolders, Modifier.fillMaxWidth().padding(top = 24.dp))
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(folders, key = { _, f -> f.uri }) { _, f ->
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().cardClickable { if (!scanning) load(f.uri, f.name) }.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(IconRadius)).background(p.iconBg), contentAlignment = Alignment.Center) {
                                Icon(AppIcons.Folder, null, tint = p.iconFg, modifier = Modifier.size(24.dp))
                            }
                            Text(f.name, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                            RoundIcon(AppIcons.MoreHoriz, "", p.text, 40, 22, p.trailingBg) { menu = f.uri }
                        }
                    }
                }
            }
        }
    }
    ApplyHost(kind, pending, { pending = null }, onLoaded)
    val m = menu
    val mf = folders.firstOrNull { it.uri == m }
    if (mf != null) {
        OptionsDialog(
            mf.name, { menu = null },
            listOf(
                pl.loadFolder(kind == 1) to { menu = null; if (!scanning) load(mf.uri, mf.name, false); Unit },
                pl.addFolderTo(kind == 1) to { menu = null; if (!scanning) load(mf.uri, mf.name, true); Unit },
                pl.removeFolder to { MediaLib.removeFolder(kind, mf.uri); menu = null },
            ),
        )
    }
}

// ------------------------------------------------------------------ playlists / collections

@Composable
internal fun ListsTab(kind: Int, onLoaded: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val lists = MediaLib.lists[kind] ?: MediaLib.listsOf(kind)
    var pending by remember { mutableStateOf<Pair<List<MItem>, String>?>(null) }
    var nameDlg by remember { mutableStateOf(false) }
    var renameId by remember { mutableStateOf<String?>(null) }
    var menuId by remember { mutableStateOf<String?>(null) }
    var delId by remember { mutableStateOf<String?>(null) }
    val addLabel = if (kind == 1) pl.addCollection else pl.addPlaylist
    val nameLabel = if (kind == 1) pl.collectionName else pl.playlistName

    Column(Modifier.fillMaxSize()) {
        PrimaryButton(addLabel, true) { nameDlg = true }
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            itemsIndexed(lists.chunked(3)) { _, row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (l in row) {
                        Column(Modifier.weight(1f)) {
                            val fav = l.id == MediaLib.FAV
                            Box(
                                Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(CardRadius)).background(p.iconBg)
                                    .border(1.dp, p.cardBorder, RoundedCornerShape(CardRadius))
                                    .clickableScale(pressed = 0.95f) {
                                        pending = MediaLib.toItems(l) to listTitle(pl, l)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(if (fav) AppIcons.Heart else if (kind == 1) AppIcons.Collections else AppIcons.PlaylistMusic, null, tint = if (fav) p.accent else p.iconFg, modifier = Modifier.fillMaxSize(0.42f))
                                if (!fav) {
                                    Box(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                                        RoundIcon(AppIcons.MoreHoriz, "", p.text, 32, 20, p.card.copy(alpha = 0.85f)) { menuId = l.id }
                                    }
                                }
                            }
                            Text(listTitle(pl, l), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
                            Text(pl.pageInfo(l.items.size), color = p.muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                        }
                    }
                    repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    ApplyHost(kind, pending, { pending = null }, onLoaded)
    if (nameDlg) NameDialog(addLabel, nameLabel, "", pl.add, { nameDlg = false }) { MediaLib.createList(kind, it); nameDlg = false }
    val rid = renameId
    val rl = lists.firstOrNull { it.id == rid }
    if (rl != null) NameDialog(pl.rename, nameLabel, rl.name, pl.save, { renameId = null }) { MediaLib.renameList(kind, rl.id, it); renameId = null }
    val ml = lists.firstOrNull { it.id == menuId }
    if (ml != null) {
        OptionsDialog(
            ml.name, { menuId = null },
            listOf(pl.rename to { renameId = ml.id; menuId = null }, pl.delete to { delId = ml.id; menuId = null }),
        )
    }
    val dl = lists.firstOrNull { it.id == delId }
    if (dl != null) ConfirmDialog(pl.delete, pl.deleteListAsk(dl.name), pl.delete, pl.cancel, { MediaLib.deleteList(kind, dl.id); delId = null }, { delId = null })
}


// ------------------------------------------------------------------ "Terapkan ke Galeri / Antrian"

/**
 * Loads [list] into the gallery / queue of [kind]. An empty target is filled directly; otherwise the user chooses
 * Muat (replace, after a warning) or Tambahkan (append).
 */
@Composable
internal fun ApplyHost(kind: Int, pending: Pair<List<MItem>, String>?, onClose: () -> Unit, onDone: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    var confirm by remember(pending) { mutableStateOf(false) }
    val pend = pending ?: return
    val gallery = kind == 1
    LaunchedEffect(pend) {
        if (PlayerHub.items(kind).isEmpty()) {
            PlayerHub.set(kind, pend.first, pend.second)
            onClose()
            onDone()
        }
    }
    if (PlayerHub.items(kind).isEmpty()) return
    if (confirm) {
        ConfirmDialog(
            pl.applyTitle(gallery), pl.overwriteAsk(gallery), pl.load, pl.cancel,
            { PlayerHub.set(kind, pend.first, pend.second); onClose(); onDone() },
            { confirm = false },
        )
        return
    }
    AppDialog(pl.applyTitle(gallery), onClose, buttons = { TextButton(onClick = onClose) { Text(pl.cancel, color = p.accent) } }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f)) { SecondaryButton(pl.load, true) { confirm = true } }
            Box(Modifier.weight(1f)) { SecondaryButton(pl.add2, true) { PlayerHub.apply(kind, pend.first, pend.second, true); onClose(); onDone() } }
        }
    }
}
