package com.furuarc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text as MText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.ui.theme.LocalPalette

/** Blok Markdown hasil parse sederhana. */
internal sealed class MdBlock {
    class Heading(val level: Int, val text: String) : MdBlock()
    class Para(val text: String) : MdBlock()
    class Item(val marker: String, val text: String, val indent: Int) : MdBlock()
    class Quote(val text: String) : MdBlock()
    class Code(val text: String) : MdBlock()
    object Rule : MdBlock()
}

private val MD_HEADING = Regex("^(#{1,6})\\s+(.*)$")
private val MD_ITEM = Regex("^(\\s*)([-*+]|\\d+\\.)\\s+(.*)$")
private val MD_RULE = Regex("^(-{3,}|\\*{3,}|_{3,})$")
private val MD_INLINE = Regex("""\*\*(.+?)\*\*|(?<!\w)__(.+?)__(?!\w)|~~(.+?)~~|\*(.+?)\*|(?<!\w)_(.+?)_(?!\w)|`(.+?)`|\[(.+?)\]\((.+?)\)""")

internal fun parseMd(text: String): List<MdBlock> {
    val out = ArrayList<MdBlock>()
    val lines = text.split("\n")
    val para = StringBuilder()
    fun flush() {
        if (para.isNotEmpty()) {
            out.add(MdBlock.Para(para.toString().trim()))
            para.setLength(0)
        }
    }
    var i = 0
    while (i < lines.size) {
        val l = lines[i]
        val t = l.trim()
        val h = MD_HEADING.matchEntire(t)
        val li = MD_ITEM.matchEntire(l)
        if (t.startsWith("```")) {
            flush()
            val sb = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                sb.append(lines[i]).append('\n')
                i++
            }
            out.add(MdBlock.Code(sb.toString().trimEnd('\n')))
        } else if (t.isEmpty()) {
            flush()
        } else if (MD_RULE.matches(t)) {
            flush()
            out.add(MdBlock.Rule)
        } else if (h != null) {
            flush()
            out.add(MdBlock.Heading(h.groupValues[1].length, h.groupValues[2]))
        } else if (t.startsWith(">")) {
            flush()
            out.add(MdBlock.Quote(t.removePrefix(">").trim()))
        } else if (li != null) {
            flush()
            out.add(MdBlock.Item(li.groupValues[2], li.groupValues[3], li.groupValues[1].length / 2))
        } else {
            if (para.isNotEmpty()) para.append(' ')
            para.append(t)
        }
        i++
    }
    flush()
    return out
}

internal fun mdInline(s: String, accent: Color, codeBg: Color): AnnotatedString = buildAnnotatedString {
    var last = 0
    for (m in MD_INLINE.findAll(s)) {
        append(s.substring(last, m.range.first))
        val g = m.groupValues
        if (g[1].isNotEmpty()) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[1]) }
        else if (g[2].isNotEmpty()) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[2]) }
        else if (g[3].isNotEmpty()) withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(g[3]) }
        else if (g[4].isNotEmpty()) withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[4]) }
        else if (g[5].isNotEmpty()) withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[5]) }
        else if (g[6].isNotEmpty()) withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = codeBg)) { append(g[6]) }
        else withStyle(SpanStyle(color = accent, textDecoration = TextDecoration.Underline)) { append(g[7]) }
        last = m.range.last + 1
    }
    append(s.substring(last))
}

/** Pratinjau Markdown sederhana: judul, paragraf, daftar, kutipan, kode, garis, tebal/miring/kode/tautan. */
@Composable
internal fun MarkdownPreview(text: String, modifier: Modifier) {
    val p = LocalPalette.current
    val blocks = remember(text) { parseMd(text) }
    LazyColumn(
        modifier.background(p.background),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(blocks.size) { i ->
            val b = blocks[i]
            if (b is MdBlock.Heading) {
                MText(mdInline(b.text, p.accent, p.card), color = p.text, fontWeight = FontWeight.Bold, fontSize = (26 - b.level * 2).sp)
            } else if (b is MdBlock.Para) {
                MText(mdInline(b.text, p.accent, p.card), color = p.text, fontSize = 15.sp, lineHeight = 22.sp)
            } else if (b is MdBlock.Item) {
                Row(Modifier.padding(start = (b.indent * 16).dp)) {
                    MText(if (b.marker[0].isDigit()) b.marker else "•", color = p.accent, fontSize = 15.sp)
                    Spacer(Modifier.width(8.dp))
                    MText(mdInline(b.text, p.accent, p.card), color = p.text, fontSize = 15.sp, lineHeight = 22.sp)
                }
            } else if (b is MdBlock.Quote) {
                MText(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = p.accent)) { append("▍ ") }
                        append(mdInline(b.text, p.accent, p.card))
                    },
                    color = p.muted, fontSize = 15.sp, lineHeight = 22.sp,
                )
            } else if (b is MdBlock.Code) {
                Box(Modifier.fillMaxWidth().background(p.card).padding(8.dp)) {
                    MText(b.text, color = p.text, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                }
            } else {
                Box(Modifier.fillMaxWidth().height(1.dp).background(p.outline))
            }
        }
    }
}
