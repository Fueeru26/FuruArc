package com.furuarc.ui

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.furuarc.MediaResult
import com.furuarc.core.CompatCheck
import com.furuarc.core.CompatFile
import com.furuarc.core.ImageConv
import com.furuarc.i18n.mediaStr
import com.furuarc.settings.Lang
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction

private val IMG_EXTS = setOf("png", "jpg", "jpeg", "webp", "bmp", "gif")
private val TXT_EXTS = setOf("json", "js", "rpy", "ks", "tjs", "txt", "html")

private fun docPath(uri: Uri): String = try {
    DocumentsContract.getDocumentId(uri).substringAfter(':')
} catch (e: Exception) {
    ""
}

/** Encoding type of a text file (reads at most the first 2 MB). */
private fun textKind(ctx: Context, uri: Uri): Int = try {
    val ins = ctx.contentResolver.openInputStream(uri)
    if (ins == null) {
        CompatFile.TEXT_NA
    } else {
        ins.use {
            val buf = ByteArray(2 * 1024 * 1024)
            var k = 0
            while (k < buf.size) {
                val r = it.read(buf, k, buf.size - k)
                if (r <= 0) break
                k += r
            }
            val b = buf.copyOf(k)
            val dec = Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT)
            // endOfInput=false: a sequence cut at the 2 MB boundary is not treated as malformed
            val res = dec.decode(ByteBuffer.wrap(b), CharBuffer.allocate(b.size + 1), false)
            CompatCheck.classifyText(b, !res.isError)
        }
    }
} catch (e: Exception) {
    CompatFile.TEXT_NA
}

private suspend fun collectFiles(ctx: Context, results: List<MediaResult>): List<CompatFile> = withContext(Dispatchers.IO) {
    val out = ArrayList<CompatFile>()
    for (r in results) {
        if (r.status != 0) continue
        val u = r.outUri ?: continue
        val path = docPath(u)
        val name = path.substringAfterLast('/').ifEmpty { r.outName }
        val folder = path.substringBeforeLast('/', "").lowercase()
        val e = name.substringAfterLast('.', "").lowercase()
        var w = 0
        var h = 0
        var tx = CompatFile.TEXT_NA
        if (e in IMG_EXTS) {
            ImageConv.bounds(ctx, u)?.let { w = it.first; h = it.second }
        } else if (e in TXT_EXTS) {
            tx = textKind(ctx, u)
        }
        out.add(CompatFile(name, folder, r.outBytes, w, h, tx))
    }
    out
}

/** Compatibility Checker dialog: matches converted files against the common rules of the target engine. */
@Composable
internal fun CompatDialog(results: List<MediaResult>, onClose: () -> Unit) {
    val p = LocalPalette.current
    val lang = LocalLang.current
    val id = lang == Lang.ID
    val m = mediaStr(lang)
    val ctx = LocalContext.current
    var files by remember { mutableStateOf<List<CompatFile>?>(null) }
    var eng by remember { mutableStateOf(CompatCheck.RPGM) }
    LaunchedEffect(results) { files = collectFiles(ctx, results) }
    val list = files
    val issues = remember(list, eng) { list?.let { CompatCheck.check(eng, it, id) } }
    Dialog(onDismissRequest = { onClose() }) {
        RowPanel(Modifier.fillMaxWidth().padding(12.dp)) {
            Column(Modifier.padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(m.compatTitle, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                AppDropdown(m.compatEngine, eng, CompatCheck.ENGINES.map { it to CompatCheck.engineName(it, id) }) { eng = it }
                if (issues == null || list == null) {
                    Text(m.compatLoading, color = p.muted, fontSize = 13.sp)
                } else {
                    val bad = issues.count { it.level == 2 }
                    val warn = issues.count { it.level == 1 }
                    val info = issues.count { it.level == 0 }
                    Text(m.compatSummary(list.size, bad, warn, info), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
                    if (issues.isEmpty()) {
                        Text(m.compatClean, color = OkColor, fontSize = 14.sp, lineHeight = 20.sp)
                    } else {
                        val levels = m.compatLevels
                        for (i in issues.take(150)) {
                            val c = when (i.level) { 2 -> ErrColor; 1 -> WarnColor; else -> p.muted }
                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                Text(levels[i.level] + " · " + i.file, color = c, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(i.text, color = p.text, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                        if (issues.size > 150) Text(m.compatMore(issues.size - 150), color = p.muted, fontSize = 12.sp)
                    }
                }
                Text(m.compatNote, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                PrimaryButton(m.close, true) { onClose() }
            }
        }
    }
}
