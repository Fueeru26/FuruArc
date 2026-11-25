package com.furuarc.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.Engine
import com.furuarc.defaultOutFolder
import com.furuarc.i18n.checkStr
import com.furuarc.i18n.fileToolsStr
import com.furuarc.storage.TreeWriter
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream

private fun fmtSize(b: Long): String = when {
    b >= 1L shl 30 -> String.format("%.2f GB", b / 1073741824.0)
    b >= 1L shl 20 -> String.format("%.2f MB", b / 1048576.0)
    b >= 1L shl 10 -> String.format("%.1f KB", b / 1024.0)
    else -> "$b B"
}

/** File Cutter: take [n] bytes from the start, middle (from an offset), or end of a file and save as a small file. */
@Composable
fun FileCutterScreen(onBack: () -> Unit) {
    val p = LocalPalette.current
    val c = checkStr(LocalLang.current)
    val ft = fileToolsStr(LocalLang.current)
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var src by remember { mutableStateOf<Uri?>(null) }
    var srcName by remember { mutableStateOf<String?>(null) }
    var srcSize by remember { mutableStateOf(0L) }
    var out by remember { mutableStateOf<Uri?>(null) }
    var outName by remember { mutableStateOf<String?>(null) }
    var part by remember { mutableStateOf(0) } // 0 = start, 1 = middle, 2 = end
    var unit by remember { mutableStateOf(2) } // 0 = B, 1 = KB, 2 = MB
    var amount by remember { mutableStateOf("2") }
    var offsetTxt by remember { mutableStateOf("0") }
    var fileName by remember { mutableStateOf("") }
    var nameEdited by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var note by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    LaunchedEffect(Unit) {
        if (out == null) defaultOutFolder()?.let { out = it.first; outName = it.second }
    }

    val unitMult = longArrayOf(1L, 1024L, 1048576L)
    val unitName = listOf("B", "KB", "MB")

    fun bytes(): Long {
        val n = amount.toLongOrNull() ?: 0L
        return n * unitMult[unit]
    }

    fun offsetBytes(): Long {
        val n = offsetTxt.toLongOrNull() ?: 0L
        return n * unitMult[unit]
    }

    fun autoName() {
        if (nameEdited) return
        val n = srcName ?: return
        val dot = n.lastIndexOf('.')
        val base = if (dot > 0) n.substring(0, dot) else n
        val ext = if (dot > 0) n.substring(dot) else ""
        val tag = when (part) {
            0 -> "_awal_"
            1 -> "_tengah_" + offsetTxt + unitName[unit] + "_"
            else -> "_akhir_"
        }
        fileName = base + tag + amount + unitName[unit] + ext
    }

    val pickSrc = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            src = u; note = null
            srcName = Engine.displayName(u)
            srcSize = try { ctx.contentResolver.openFileDescriptor(u, "r")?.use { it.statSize } ?: 0L } catch (e: Exception) { 0L }
            nameEdited = false
            autoName()
        }
    }
    LaunchedEffect(Unit) {
        PendingFile.take("filetools.cut")?.let { u ->
            src = u; note = null
            srcName = Engine.displayName(u)
            srcSize = try { ctx.contentResolver.openFileDescriptor(u, "r")?.use { it.statSize } ?: 0L } catch (e: Exception) { 0L }
            nameEdited = false
            autoName()
        }
    }
    val pickOut = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            out = u; outName = android.provider.DocumentsContract.getTreeDocumentId(u).substringAfterLast(':').ifEmpty { "/" }
        }
    }

    fun run() {
        val s = src
        val o = out
        if (s == null) { note = c.cutNoFile to false; return }
        if (o == null) { note = c.cutOutFolder + "?" to false; return }
        val want = bytes()
        if (want <= 0L) { note = c.cutFail("0") to false; return }
        val name = fileName.trim().ifEmpty { "sample.bin" }
        busy = true; note = null; progress = 0f
        val fromTail = part == 2
        val startOff = if (part == 1) offsetBytes() else 0L
        val isMid = part == 1
        scope.launch {
            val r = withContext(Dispatchers.IO) {
                try {
                    val pfd = ctx.contentResolver.openFileDescriptor(s, "r") ?: throw java.io.IOException("open")
                    pfd.use { fd ->
                        FileInputStream(fd.fileDescriptor).use { fin ->
                            val ch = fin.channel
                            val len = ch.size()
                            if (isMid && startOff >= len) throw java.io.IOException(ft.cutOffsetBeyond)
                            val n = minOf(want, if (isMid) len - startOff else len)
                            var pos = if (fromTail) len - n else if (isMid) startOff else 0L
                            val end = pos + n
                            val of = TreeWriter(Engine.app, o).open(name) ?: throw java.io.IOException("output")
                            val buf = java.nio.ByteBuffer.allocate(1 shl 20)
                            of.stream.use { os ->
                                while (pos < end) {
                                    buf.clear()
                                    buf.limit(minOf(buf.capacity().toLong(), end - pos).toInt())
                                    val k = ch.read(buf, pos)
                                    if (k <= 0) break
                                    os.write(buf.array(), 0, k)
                                    pos += k
                                    progress = (pos - (end - n)).toFloat() / n
                                }
                            }
                            Result.success(n)
                        }
                    }
                } catch (e: Throwable) { Result.failure<Long>(e) }
            }
            busy = false
            note = r.getOrNull()?.let { c.cutDone(name, fmtSize(it)) to true }
                ?: (c.cutFail(r.exceptionOrNull()?.message ?: "?") to false)
        }
    }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(c.cutTitle, onBack)
LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) { 
        item {
            PickerCard(AppIcons.File, c.cutPicker, srcName?.let { c.cutInfo(it, fmtSize(srcSize)) }, AppIcons.File) { if (!busy) pickSrc.launch(arrayOf("*/*")) }
        }
        panel(c.cutPart) {
        run { SegmentedChips(listOf(c.cutHead, ft.cutMid, c.cutTail), part) { part = it; autoName() } }
        if (part == 1) run {
            AppTextField(value = offsetTxt, onValueChange = { v -> offsetTxt = v.filter { ch -> ch.isDigit() }.take(12); autoName() }, label = ft.cutOffset, number = true)
        }
        run {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    AppTextField(value = amount, onValueChange = { v -> amount = v.filter { ch -> ch.isDigit() }.take(6); autoName() }, label = c.cutAmount, number = true)
                }
                Box(Modifier.weight(1f)) { SegmentedChips(unitName, unit) { unit = it; autoName() } }
            }
        }
        run { DetailHint(c.cutHint) }
        run { AppTextField(value = fileName, onValueChange = { fileName = it; nameEdited = true }, label = c.cutOutName) }
        run { PickerCard(AppIcons.Folder, c.cutOutFolder, outName, AppIcons.Folder) { if (!busy) pickOut.launch(null) } }
        }
        item { PrimaryButton(c.cutRun, src != null && !busy && bytes() > 0) { run() } }
        if (busy) item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(c.cutBusy, color = p.muted, fontSize = 13.sp)
                LinearProgressIndicator(progress, Modifier.fillMaxWidth(), color = p.accent)
            }
        }
        note?.let { (txt, ok) ->
            item {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(txt, color = if (ok) Color(0xFF66BB6A) else Color(0xFFE57373), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        SecondaryButton(c.cutClose) { note = null }
                    }
                }
            }
        }
    }
}
}
