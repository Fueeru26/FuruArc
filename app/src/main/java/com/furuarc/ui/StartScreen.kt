package com.furuarc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.i18n.NavStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private val STAR_ON = Color(0xFFFFC107)

/** Home tab: feature search, recently used, quick actions and favorites. [onOpen] receives a feature id (see FeatureCatalog). */
@Composable
fun StartScreen(onOpen: (String) -> Unit, onOpenWithFile: (String, android.net.Uri) -> Unit) {
    val p = LocalPalette.current
    val nav = NavStr.of(LocalLang.current)
    val all = FeatureCatalog.rememberAll()
    val byId = remember(all) { all.associateBy { it.id } }
    var q by remember { mutableStateOf("") }
    val searching = q.isNotBlank()
    var editQuick by remember { mutableStateOf(false) }
    var pickedFile by remember { mutableStateOf<String?>(null) }
    var pickedUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val ctx = LocalContext.current
    val pickFile = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) { pickedUri = uri; pickedFile = displayName(ctx, uri) }
    }
    // back while searching only clears the query
    BackHandler(enabled = q.isNotEmpty() && !FocusHub.focused) { q = "" }
    val results = remember(q, all) { if (searching) FeatureCatalog.search(all, q).take(40) else emptyList() }
    val open = { id: String, fromSearch: Boolean ->
        if (fromSearch) HomeStore.addHistory(q)
        onOpen(id)
    }

    StartDialogs(all, byId, editQuick, { editQuick = false }, pickedFile, { pickedFile = null }) { id ->
        val u = pickedUri
        pickedFile = null; pickedUri = null
        if (u != null) { HomeStore.touch(id); onOpenWithFile(id, u) } else open(id, false)
    }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        HomeHeader(nav.tabHome)
        AppTextField(
            value = q,
            onValueChange = { q = it },
            label = nav.searchHint,
            trailing = {
                if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Default.Close, nav.clearSearch, tint = p.muted) }
            },
        )
        Spacer(Modifier.height(ScreenGap))
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp + LocalBarInset.current),
            verticalArrangement = Arrangement.spacedBy(ScreenGap),
        ) {
            if (searching) {
                if (results.isEmpty()) EmptyState(Icons.Default.Search, nav.noResult)
                for (f in results) FeatureRow(f, HomeStore.isFav(f.id), { open(f.id, true) }, { HomeStore.toggleFav(f.id) })
            } else {
                val hist = HomeStore.history
                if (hist.isNotEmpty()) {
                    SectionHeader(nav.history, nav.clear) { HomeStore.clearHistory() }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (h in hist) Chip(h, null) { q = h }
                    }
                }
                val recents = HomeStore.recents.mapNotNull { byId[it] }
                if (recents.isNotEmpty()) {
                    SectionHeader(nav.recents, nav.clear) { HomeStore.clearRecents() }
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (f in recents) Chip(f.title, f) { open(f.id, false) }
                    }
                }
                SectionHeader(nav.quick, nav.editQuick) { editQuick = true }
                val quick = HomeStore.quick.mapNotNull { byId[it] }
                if (quick.isEmpty()) EmptyState(Icons.Default.Add, nav.quickEmpty)
                for (row in quick.chunked(2)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (f in row) QuickTile(f, Modifier.weight(1f)) { open(f.id, false) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
                SectionHeader(nav.startFileHeader, null) {}
                MenuCard(AppIcons.File, nav.startFile, nav.startFileSub) { pickFile.launch(arrayOf("*/*")) }
                SectionHeader(nav.favorites, null) {}
                val favs = HomeStore.favorites.mapNotNull { byId[it] }
                if (favs.isEmpty()) EmptyState(Icons.Default.Star, nav.favEmpty)
                for (f in favs) FeatureRow(f, true, { open(f.id, false) }, { HomeStore.toggleFav(f.id) })
            }
        }
    }
}

@Composable
private fun StartDialogs(
    all: List<Feature>, byId: Map<String, Feature>, editQuick: Boolean, onCloseEdit: () -> Unit,
    pickedFile: String?, onClosePicked: () -> Unit, onOpen: (String) -> Unit,
) {
    if (editQuick) QuickEditor(all, byId, onCloseEdit)
    if (pickedFile != null) FileSuggestDialog(all, pickedFile, onClosePicked, onOpen)
}

@Composable
private fun SectionHeader(title: String, action: String?, onAction: () -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = p.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (action != null) Text(action, color = p.accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onAction).padding(4.dp))
    }
}

@Composable
private fun Chip(text: String, f: Feature?, onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    Row(
        Modifier.clip(shape).background(p.card).border(1.dp, p.cardBorder, shape).clickableScale(pressed = 0.94f, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (f != null) {
            Icon(f.icon, null, tint = p.iconFg, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = p.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun QuickTile(f: Feature, modifier: Modifier, onClick: () -> Unit) {
    val p = LocalPalette.current
    // Blue Blossom style: the same blossom corner as the menu cards, a bit smaller to fit the half-width tile
    AppCard(modifier, deco = true, decoScale = 0.7f) {
        Column(Modifier.cardClickable(onClick).padding(16.dp).fillMaxWidth()) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(IconRadius)).background(p.brush), contentAlignment = Alignment.Center) {
                Icon(f.icon, null, tint = p.onGrad, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(10.dp))
            // always reserve two lines so every tile has the same height; longer labels are cut at two lines with "..."
            val twoLines = with(LocalDensity.current) { 40.sp.toDp() }
            Text(
                f.title, color = p.text, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold,
                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.heightIn(min = twoLines),
            )
        }
    }
}

@Composable
private fun FeatureRow(f: Feature, fav: Boolean, onClick: () -> Unit, onFav: () -> Unit) {
    val p = LocalPalette.current
    val nav = NavStr.of(LocalLang.current)
    AppCard(Modifier.fillMaxWidth()) {
        Row(Modifier.cardClickable(onClick).padding(start = 12.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(IconRadius)).background(p.brush), contentAlignment = Alignment.Center) {
                Icon(f.icon, null, tint = p.onGrad, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(f.title, color = p.text, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(f.path, color = p.muted, fontSize = 12.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onFav) {
                Icon(Icons.Default.Star, if (fav) nav.removeFav else nav.addFav, tint = if (fav) STAR_ON else p.muted.copy(alpha = 0.4f))
            }
        }
    }
}

private fun displayName(ctx: android.content.Context, uri: android.net.Uri): String {
    try {
        ctx.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0)?.let { return it }
        }
    } catch (e: Exception) { }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "file"
}

/** "Find features by file": features that fit the picked file's extension (engine archives, media converters, text tools...). */
@Composable
private fun FileSuggestDialog(all: List<Feature>, name: String, onDismiss: () -> Unit, onOpen: (String) -> Unit) {
    val p = LocalPalette.current
    val nav = NavStr.of(LocalLang.current)
    val ext = name.substringAfterLast('.', "").lowercase()
    val found = remember(all, ext) { FeatureCatalog.forExtension(all, ext) }
    // always offer the generic inspection tools too (they detect the format from the content)
    val extra = remember(all) { listOf("filecheck.file", "filecheck.hex").mapNotNull { id -> all.firstOrNull { it.id == id } } }
    val list = (found + extra).distinctBy { it.id }.filter { FeatureCatalog.canPreload(it.id) }.take(8)
    AppDialog(
        title = nav.startFileTitle,
        onDismiss = onDismiss,
        buttons = { SecondaryButton(nav.close, onDismiss) },
    ) {
        Text(name, color = p.text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (found.isEmpty()) Text(nav.startFileNone, color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
        Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (f in list) FeatureRow(f, HomeStore.isFav(f.id), { onOpen(f.id) }, { HomeStore.toggleFav(f.id) })
        }
    }
}

/**
 * "Edit Quick Actions" window: search a feature to add (+), the list (max 10) with a drag handle (≡) to reorder and an X to remove.
 * The content itself does not scroll (so dragging works); the window is modal, so the Home page behind it cannot be scrolled either.
 */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun QuickEditor(all: List<Feature>, byId: Map<String, Feature>, onDismiss: () -> Unit) {
    val p = LocalPalette.current
    val nav = NavStr.of(LocalLang.current)
    val density = LocalDensity.current
    // row height adapts to the screen so 10 rows + header + button always fit without scrolling
    val rowH = ((LocalConfiguration.current.screenHeightDp - 300) / 10).coerceIn(34, 46).dp
    val rowPx = with(density) { rowH.toPx() }
    val list = remember { mutableStateListOf<String>().apply { addAll(HomeStore.quick) } }
    var q by remember { mutableStateOf("") }
    var dragId by remember { mutableStateOf<String?>(null) }
    var dragOff by remember { mutableStateOf(0f) }
    val results = remember(q, all) { if (q.isBlank()) emptyList() else FeatureCatalog.search(all, q).take(8) }
    AppDialog(
        title = nav.editQuick,
        onDismiss = { HomeStore.updateQuick(list.toList()); onDismiss() },
        buttons = { SecondaryButton(nav.close) { HomeStore.updateQuick(list.toList()); onDismiss() } },
    ) {
        AppTextField(value = q, onValueChange = { q = it }, label = nav.quickSearch, trailing = {
            if (q.isNotEmpty()) IconButton(onClick = { q = "" }) { Icon(Icons.Default.Close, nav.clearSearch, tint = p.muted) }
        })
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (q.isBlank()) nav.quickList else nav.addQuick, color = p.muted, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text("${list.size}/${HomeStore.MAX_QUICK}", color = if (list.size >= HomeStore.MAX_QUICK) p.accent else p.muted, fontSize = 12.sp)
        }
        if (q.isNotBlank()) {
            // search results: tap + to add
            if (results.isEmpty()) Text(nav.noResult, color = p.muted, fontSize = 14.sp)
            Column(Modifier.fillMaxWidth()) {
            for (f in results) {
                val added = f.id in list
                Row(Modifier.fillMaxWidth().height(rowH), verticalAlignment = Alignment.CenterVertically) {
                    Icon(f.icon, null, tint = p.iconFg, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                        Text(f.title, color = p.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(f.path, color = p.muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (added) Text("✓", color = p.accent, fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp))
                    else IconButton(onClick = { if (list.size < HomeStore.MAX_QUICK) { list.add(f.id); HomeStore.updateQuick(list.toList()) } }, modifier = Modifier.size(rowH)) {
                        Icon(Icons.Default.Add, nav.addQuick, tint = if (list.size < HomeStore.MAX_QUICK) p.accent else p.muted.copy(alpha = 0.4f))
                    }
                }
            }
            }
            if (list.size >= HomeStore.MAX_QUICK) Text(nav.quickFull, color = p.muted, fontSize = 12.sp)
        } else {
            if (list.isEmpty()) Text(nav.quickEmpty, color = p.muted, fontSize = 14.sp, lineHeight = 20.sp)
            Column(Modifier.fillMaxWidth()) {
            for (id in list.toList()) key(id) {
                val f = byId[id]
                if (f != null) {
                    val dragging = dragId == id
                    Row(
                        Modifier.fillMaxWidth().height(rowH).zIndex(if (dragging) 1f else 0f)
                            .graphicsLayer { translationY = if (dragging) dragOff else 0f }
                            .clip(RoundedCornerShape(8.dp)).background(if (dragging) p.iconBg else Color.Transparent),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Menu, nav.dragHint, tint = p.muted,
                            modifier = Modifier.size(rowH).padding(8.dp).pointerInput(id) {
                                detectDragGestures(
                                    onDragStart = { dragId = id; dragOff = 0f },
                                    onDragEnd = { dragId = null; dragOff = 0f; HomeStore.updateQuick(list.toList()) },
                                    onDragCancel = { dragId = null; dragOff = 0f; HomeStore.updateQuick(list.toList()) },
                                    onDrag = { change, amount ->
                                        change.consume()
                                        dragOff += amount.y
                                        val i = list.indexOf(id)
                                        if (dragOff > rowPx / 2 && i < list.lastIndex) { list.add(i + 1, list.removeAt(i)); dragOff -= rowPx }
                                        else if (dragOff < -rowPx / 2 && i > 0) { list.add(i - 1, list.removeAt(i)); dragOff += rowPx }
                                    },
                                )
                            },
                        )
                        Icon(f.icon, null, tint = p.iconFg, modifier = Modifier.size(20.dp))
                        Text(f.title, color = p.text, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                        IconButton(onClick = { list.remove(id); HomeStore.updateQuick(list.toList()) }, modifier = Modifier.size(rowH)) {
                            Icon(Icons.Default.Close, nav.removeQuick, tint = p.muted)
                        }
                    }
                }
            }
            }
        }
    }
}
