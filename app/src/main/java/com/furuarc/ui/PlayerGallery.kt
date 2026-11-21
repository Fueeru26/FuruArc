package com.furuarc.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.furuarc.core.MItem
import com.furuarc.core.MediaPrefs
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.InputStream
import kotlin.math.abs

private fun openStream(ctx: android.content.Context, it: MItem): InputStream? =
    if (it.uri != null) ctx.contentResolver.openInputStream(it.uri) else if (it.path != null) FileInputStream(it.path) else null

internal fun decodeBitmap(ctx: android.content.Context, item: MItem, maxDim: Int): ImageBitmap? {
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

internal fun imageDimensions(ctx: android.content.Context, item: MItem): String {
    return try {
        val b = BitmapFactory.Options()
        b.inJustDecodeBounds = true
        openStream(ctx, item)?.use { BitmapFactory.decodeStream(it, null, b) }
        if (b.outWidth > 0) "${b.outWidth} × ${b.outHeight}" else ""
    } catch (e: Throwable) {
        ""
    }
}

private val thumbCache = android.util.LruCache<String, ImageBitmap>(100)

/** Square thumbnail (cached; decoded lazily while scrolling). */
@Composable
private fun Thumb(item: MItem, modifier: Modifier) {
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val bmp by produceState<ImageBitmap?>(thumbCache.get(item.key), item.key) {
        if (value == null) {
            val b = withContext(Dispatchers.IO) { decodeBitmap(ctx, item, 256) }
            if (b != null) thumbCache.put(item.key, b)
            value = b
        }
    }
    Box(modifier.background(p.iconBg), contentAlignment = Alignment.Center) {
        val b = bmp
        if (b != null) Image(b, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(AppIcons.Image, null, tint = p.iconFg, modifier = Modifier.size(24.dp))
    }
}

// ------------------------------------------------------------------ gallery tab

@Composable
internal fun GalleryTab(onOpen: (Int) -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val items = PlayerHub.image
    val size = MediaPrefs.pageGallery
    val pages = ((items.size + size - 1) / size).coerceAtLeast(1)
    var page by remember { mutableStateOf(0) }
    val pg = page.coerceIn(0, pages - 1)
    val from = pg * size
    val to = minOf(items.size, from + size)
    var menu by remember { mutableStateOf(-1) }
    val grid = MediaPrefs.galleryMode == 1
    val ctx = LocalContext.current
    var selMode by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var pickList by remember { mutableStateOf(false) }
    BackHandler(enabled = selMode) { selMode = false; sel = emptySet() }
    fun chosen(): List<MItem> = sel.sorted().mapNotNull { items.getOrNull(it) }
    fun endSel() { selMode = false; sel = emptySet() }
    fun tap(gi: Int) { if (selMode) sel = if (gi in sel) sel - gi else sel + gi else onOpen(gi) }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val lb = PlayerHub.label[1] ?: ""
            Text(
                if (items.isNotEmpty()) (if (lb.isNotEmpty()) "$lb · " else "") + pl.pageInfo(items.size) else "",
                color = p.muted, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            if (items.isNotEmpty()) RoundIcon(AppIcons.Checklist, pl.selMode, if (selMode) p.accent else p.text, 40, 24) { if (selMode) endSel() else selMode = true }
            RoundIcon(if (grid) AppIcons.ViewList else AppIcons.GridView, if (grid) pl.viewList else pl.viewGrid, p.text, 40, 24) {
                MediaPrefs.galleryMode = if (grid) 0 else 1
                MediaPrefs.save()
            }
        }
        if (selMode) {
            Spacer(Modifier.height(4.dp))
            SelectionBar(
                1, sel.size, { sel = items.indices.toSet() }, { endSel() },
                {
                    val n = MediaLib.addItems(1, MediaLib.FAV, chosen())
                    toastMsg(ctx, if (n > 0) pl.addedTo(n, pl.favorites) else pl.alreadyIn(pl.favorites))
                    endSel()
                },
                { pickList = true },
                {
                    PlayerHub.update(1, items.filterIndexed { i, _ -> i !in sel })
                    endSel()
                },
            )
            Spacer(Modifier.height(6.dp))
        }
        if (items.isEmpty()) {
            EmptyState(AppIcons.Image, pl.emptyGallery, Modifier.fillMaxWidth().padding(top = 24.dp))
        } else {
            val sub = items.subList(from, to)
            if (!grid) {
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(sub, key = { l, it -> (from + l).toString() + it.key }) { l, it ->
                        val gi = from + l
                        AppCard(Modifier.fillMaxWidth()) {
                            Row(
                                Modifier.fillMaxWidth().background(if (gi in sel) p.accent.copy(alpha = 0.18f) else Color.Transparent).cardClickable { tap(gi) }.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (selMode) Checkbox(checked = gi in sel, onCheckedChange = { tap(gi) }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                                Thumb(it, Modifier.size(56.dp).clip(RoundedCornerShape(IconRadius)))
                                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(it.name, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (it.size > 0) Text(sizeText(it.size), color = p.muted, fontSize = 12.sp)
                                }
                                if (!selMode) RoundIcon(AppIcons.MoreHoriz, "", p.text, 40, 22, p.trailingBg) { menu = gi }
                            }
                        }
                    }
                }
            } else {
                val cols = MediaPrefs.gridCols.coerceIn(2, 4)
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    itemsIndexed(sub.chunked(cols)) { r, row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEachIndexed { c, it ->
                                val gi = from + r * cols + c
                                Box(Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(IconRadius)).clickableScale(pressed = 0.96f) { tap(gi) }) {
                                    Thumb(it, Modifier.fillMaxSize())
                                    if (selMode) {
                                        val on = gi in sel
                                        if (on) Box(Modifier.fillMaxSize().background(p.accent.copy(alpha = 0.35f)))
                                        Box(
                                            Modifier.align(Alignment.TopStart).padding(5.dp).size(24.dp).clip(RoundedCornerShape(12.dp))
                                                .background(if (on) p.accent else Color(0x88000000)),
                                            contentAlignment = Alignment.Center,
                                        ) { if (on) Icon(Icons.Default.Check, null, tint = p.onAccent, modifier = Modifier.size(16.dp)) }
                                    } else {
                                        Box(Modifier.align(Alignment.TopEnd).padding(3.dp)) {
                                            RoundIcon(AppIcons.MoreHoriz, "", Color.White, 30, 18, Color(0x88000000)) { menu = gi }
                                        }
                                    }
                                }
                            }
                            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            if (pages > 1) {
                Spacer(Modifier.height(8.dp))
                PageNav(pg, pages, { page = it })
            }
        }
    }
    items.getOrNull(menu)?.let { ItemMenu(1, it, menu) { menu = -1 } }
    if (pickList) PickListDialog(1, { pickList = false }) { l -> addChosen(ctx, pl, 1, chosen(), l); pickList = false; endSel() }
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
    var menu by remember { mutableStateOf(false) }
    val item = items[index.coerceIn(0, items.size - 1)]
    val res by produceState<Pair<Boolean, ImageBitmap?>>(false to null, item.key) {
        value = false to null
        value = true to withContext(Dispatchers.IO) { decodeBitmap(ctx, item, 2400) }
    }
    // "original size": start zoomed so that one image pixel is one screen pixel
    LaunchedEffect(res.second, sz) {
        val b = res.second ?: return@LaunchedEffect
        if (MediaPrefs.imageFit == 1 && sz.width > 0 && sz.height > 0) {
            val fit = minOf(sz.width.toFloat() / b.width, sz.height.toFloat() / b.height)
            scale = (1f / fit).coerceIn(1f, 6f)
        }
    }
    LaunchedEffect(slide, index) {
        if (slide && items.size > 1) {
            delay(MediaPrefs.slideshowSec * 1000L)
            index = (index + 1) % items.size
        }
    }
    fun go(d: Int) { index = (index + d + items.size) % items.size }
    BackHandler { onClose() }
    val bg = when (MediaPrefs.viewerBg) { 1 -> Color(0xFF2B2B2B); 2 -> Color.White; else -> Color.Black }
    Box(
        Modifier.fillMaxSize().background(bg).onSizeChanged { sz = it }
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
            Text(if (res.first) pl.loadFail else pl.loading, color = if (MediaPrefs.viewerBg == 2) Color.Black else Color.White, modifier = Modifier.padding(24.dp))
        }
        if (ui) {
            Row(Modifier.align(Alignment.TopStart).fillMaxWidth().background(Color(0x99000000)).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, pl.close, tint = Color.White) }
                Text(item.name, color = Color.White, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text("${index + 1}/${items.size}", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 6.dp))
                RoundIcon(AppIcons.MoreHoriz, pl.menuTitleItem, Color.White, 40, 22) { menu = true }
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
    if (menu) ItemMenu(1, item, -1) { menu = false }
}
