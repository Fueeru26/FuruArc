package com.furuarc.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TriStateCheckbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.MainViewModel
import com.furuarc.core.FileTree
import com.furuarc.i18n.ScriptStrings
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.BitSet

private val IMAGE_EXT = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif")
private val TEXT_EXT = setOf(
    "rpy", "ks", "tjs", "txt", "json", "csv", "ini", "xml", "py", "log", "md", "html", "tsv", "lua", "cfg", "yml", "yaml",
)

private fun extOf(name: String): String = name.substringAfterLast('.', "").lowercase()
private fun previewable(name: String): Boolean = extOf(name).let { it in IMAGE_EXT || it in TEXT_EXT || it in AUDIO_EXT }

/**
 * Pick the files/folders to extract. Folders can be expanded/collapsed; checking a folder = all its contents.
 * [browse] = only view the archive contents (no checkboxes, select all, clear, and apply).
 * The search box shows a result list (can be selected at once). Tap an image/text name = preview.
 */
@Composable
fun SelectScreen(vm: MainViewModel, onClose: () -> Unit, browse: Boolean = false) {
    val s = LocalStr.current
    val ts = ScriptStrings.of(LocalLang.current)
    val p = LocalPalette.current
    val names = remember { vm.entryNames() }
    val tree = remember { FileTree(names) }
    var sel by remember { mutableStateOf(vm.selectionCopy() ?: (if (browse) BitSet().also { it.set(0, tree.size) } else BitSet())) }
    var expanded by remember { mutableStateOf(setOf<String>()) }
    var query by remember { mutableStateOf("") }
    var previewIndex by remember { mutableStateOf(-1) }

    val q = query.trim()
    val searching = q.isNotEmpty()
    // Search covers folders and files. Files that match only because their parent folder matches are not repeated.
    val rows = remember(expanded, q) {
        if (q.isEmpty()) {
            tree.visible(expanded)
        } else {
            val dirs = tree.dirNodes.filter { it.path.contains(q, ignoreCase = true) }
            val covered = BitSet()
            for (d in dirs) covered.set(d.from, d.to)
            val files = names.indices.mapNotNull { i ->
                val n = tree.fileNodes[i] ?: return@mapNotNull null
                val hit = if (covered.get(i)) n.name.contains(q, ignoreCase = true) else n.path.contains(q, ignoreCase = true)
                if (hit) n else null
            }
            dirs + files
        }
    }
    val counts = remember(sel) { tree.counts(sel) }

    BackHandler(enabled = previewIndex >= 0) { previewIndex = -1 }

    if (previewIndex >= 0) {
        PreviewPane(vm, previewIndex, names[previewIndex]) { previewIndex = -1 }
        return
    }

    Column(
        Modifier.fillMaxSize().padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ScreenHeader(if (browse) ts.browseTitle else s.selectTitle, onClose)

        AppTextField(value = query, onValueChange = { query = it }, label = s.searchHint)

        if (!browse) SegmentedChips(
            items = if (searching) listOf(s.selectResults, s.clearResults) else listOf(s.selectAll, s.clearAll),
            selected = -1,
            onSelect = { i ->
                val b = if (searching) sel.clone() as BitSet else BitSet()
                if (searching) {
                    for (n in rows) b.set(n.from, n.to, i == 0)
                } else if (i == 0) {
                    b.set(0, tree.size)
                }
                sel = b
            },
        )
        if (browse) {
            if (searching) Text(s.searchResults(rows.size), color = p.accent, fontWeight = FontWeight.SemiBold)
            Text(ts.browseHint, color = p.muted, fontSize = 12.sp)
        } else {
            Text(
                (if (searching) s.searchResults(rows.size) + " · " else "") + s.selectedInfo(counts.first, counts.second),
                color = p.accent,
                fontWeight = FontWeight.SemiBold,
            )
            if (!searching) Text(s.previewHint, color = p.muted, fontSize = 11.sp)
        }

        if (searching && rows.isEmpty()) {
            Text(s.searchNone, color = p.muted)
        }

        AppCard(Modifier.fillMaxWidth().weight(1f)) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)) {
            items(rows, key = { (if (it.isDir) "d:" else "f:") + it.path }) { node ->
                val state = tree.stateOf(sel, node)
                val open = expanded.contains(node.path)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (node.isDir && searching) {
                                // open the search-result folder in the normal tree
                                val parts = node.path.split('/')
                                expanded = expanded + (1..parts.size).map { parts.take(it).joinToString("/") }
                                query = ""
                            } else if (node.isDir) {
                                expanded = if (open) expanded - node.path else expanded + node.path
                            } else if (previewable(node.name)) {
                                previewIndex = node.from
                            } else if (!browse) {
                                sel = tree.toggle(sel, node)
                            }
                        }
                        .padding(start = (if (searching) 0 else node.depth * 18).dp)
                        .padding(vertical = if (!browse) 0.dp else if (node.isDir) 6.dp else 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (node.isDir) {
                        Icon(
                            imageVector = if (open) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = p.muted,
                        )
                    } else if (!searching) {
                        Spacer(Modifier.width(24.dp))
                    }
                    if (!browse) TriStateCheckbox(
                        state = when (state) {
                            FileTree.ALL -> ToggleableState.On
                            FileTree.PARTIAL -> ToggleableState.Indeterminate
                            else -> ToggleableState.Off
                        },
                        onClick = { sel = tree.toggle(sel, node) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = p.accent,
                            uncheckedColor = p.muted,
                            checkmarkColor = p.onAccent,
                        ),
                    ) else Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (node.isDir) AppIcons.Folder else AppIcons.File,
                        contentDescription = null,
                        tint = if (node.isDir) p.accent else p.muted,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (searching) node.path else node.name,
                        color = p.text,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (node.isDir) {
                        Text("${node.count}", color = p.muted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                }
            }
        }
        }

        if (!browse) PrimaryButton(
            text = s.btnApply,
            enabled = counts.first > 0,
            onClick = {
                vm.applySelection(sel)
                onClose()
            },
        )
    }
}

/** Preview one file (image or text) directly from the archive, without writing to storage. */
@Composable
private fun PreviewPane(vm: MainViewModel, index: Int, name: String, onClose: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    var bmp by remember(index) { mutableStateOf<ImageBitmap?>(null) }
    var audio by remember(index) { mutableStateOf<ByteArray?>(null) }
    var text by remember(index) { mutableStateOf<String?>(null) }
    var error by remember(index) { mutableStateOf<String?>(null) }
    var loading by remember(index) { mutableStateOf(true) }

    LaunchedEffect(index) {
        val isImage = extOf(name) in IMAGE_EXT
        val isAudio = extOf(name) in AUDIO_EXT
        val msgBinary = s.previewBinary
        val msgError = s.previewError
        withContext(Dispatchers.IO) {
            val d = vm.loadPreview(index, if (isImage || isAudio) 32L * 1024 * 1024 else 2L * 1024 * 1024)
            val b = d.bytes
            if (b == null) {
                error = d.error ?: msgError
            } else if (isAudio) {
                audio = b
            } else if (isImage) {
                val bounds = BitmapFactory.Options()
                bounds.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(b, 0, b.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > 2048 || bounds.outHeight / sample > 2048) sample *= 2
                val opts = BitmapFactory.Options()
                opts.inSampleSize = sample
                val bm = BitmapFactory.decodeByteArray(b, 0, b.size, opts)
                if (bm == null) error = msgError else bmp = bm.asImageBitmap()
            } else {
                var zeros = 0
                for (i in 0 until minOf(b.size, 2000)) if (b[i].toInt() == 0) zeros++
                if (zeros > 0) error = msgBinary else text = String(b, Charsets.UTF_8).removePrefix("﻿")
            }
        }
        loading = false
    }

    Column(Modifier.fillMaxSize().padding(ScreenPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = s.back, tint = p.text)
            }
            Text(
                name,
                color = p.text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            val b = bmp
            val t = text
            val e = error
            if (loading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CircularProgressIndicator(color = p.accent)
                    Text(LocalStr.current.x.processing, color = p.muted, fontSize = 13.sp)
                }
            } else if (e != null) {
                Text(e, color = p.muted)
            } else if (b != null) {
                Image(bitmap = b, contentDescription = name, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            } else if (audio != null) {
                AudioPreview(audio!!, extOf(name), name)
            } else if (t != null) {
                PagedText(t, Modifier.fillMaxSize())
            }
        }
        SecondaryButton(s.btnClose, onClose)
    }
}
