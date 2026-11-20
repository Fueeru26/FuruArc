package com.furuarc.core

import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/** File too large to edit in the File Text Editor. */
class TextTooBigException(val size: Long) : java.io.IOException("too big")

/** File looks binary (not text). */
class NotTextException : java.io.IOException("not text")

/**
 * Load and save text for the File Text Editor: encoding/BOM/line ending detection, plus find and replace.
 * Text in the editor always uses "\n"; the original line ending (CRLF) is restored on save.
 */
object TxtDoc {
    /** Size limit for files that may be edited (bytes). */
    const val EDIT_LIMIT = 4L * 1024 * 1024

    /** Limit for files that may still be opened read-only (bytes) and the size of one page. */
    const val READ_LIMIT = 40L * 1024 * 1024
    const val PAGE_BYTES = 512 * 1024

    /** Start of each page (bytes), cut at a line boundary so multibyte characters are not split. */
    fun pageStarts(b: ByteArray): IntArray {
        val out = ArrayList<Int>()
        var s = 0
        while (s < b.size) {
            out.add(s)
            var e = minOf(s + PAGE_BYTES, b.size)
            if (e < b.size) {
                var k = e
                val lim = minOf(b.size, e + 65536)
                while (k < lim && b[k] != 0x0A.toByte()) k++
                if (k < lim) e = k + 1
            }
            s = e
        }
        return out.toIntArray()
    }

    fun pageText(b: ByteArray, starts: IntArray, page: Int, enc: String): String {
        if (starts.isEmpty()) return ""
        val pg = page.coerceIn(0, starts.size - 1)
        var s = starts[pg]
        val e = if (pg + 1 < starts.size) starts[pg + 1] else b.size
        if (pg == 0 && enc == "utf8" && b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte()) s += 3
        val raw = String(b, s, e - s, charset(enc))
        return if (raw.contains("\r\n")) raw.replace("\r\n", "\n") else raw
    }

    /** Encodings that can be chosen when opening. */
    val OPEN_ENCODINGS = listOf("auto", "utf8", "sjis", "gbk", "big5", "euckr", "utf16le", "utf16be")

    /** Save encoding choices ("same" = follow the original file). */
    val SAVE_ENCODINGS = listOf("same", "utf8", "utf8bom", "sjis", "gbk", "big5", "euckr", "utf16le", "utf16be")

    class Loaded(val text: String, val enc: String, val bom: Boolean, val crlf: Boolean)

    fun charset(enc: String): Charset = try {
        when (enc) {
            "sjis" -> try { Charset.forName("windows-31j") } catch (e: Exception) { Charset.forName("Shift_JIS") }
            "gbk" -> Charset.forName("GBK")
            "big5" -> Charset.forName("Big5")
            "euckr" -> Charset.forName("EUC-KR")
            "utf16le" -> Charsets.UTF_16LE
            "utf16be" -> Charsets.UTF_16BE
            else -> Charsets.UTF_8
        }
    } catch (e: Exception) {
        Charsets.UTF_8
    }

    private fun isUtf8(b: ByteArray): Boolean = try {
        Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(b))
        true
    } catch (e: Exception) {
        false
    }

    private fun hasUtf16Bom(b: ByteArray): Boolean =
        b.size >= 2 && ((b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte()) || (b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte()))

    /** Contains a zero byte at the start of the file (and not UTF-16): probably not text. */
    fun looksBinary(b: ByteArray): Boolean {
        if (hasUtf16Bom(b)) return false
        val n = minOf(b.size, 8000)
        for (i in 0 until n) if (b[i] == 0.toByte()) return true
        return false
    }

    /** Read [b] with the chosen encoding ("auto" = detect BOM, then UTF-8, then Shift-JIS). */
    fun decode(b: ByteArray, encPref: String): Loaded {
        var enc = encPref
        var bom = false
        var off = 0
        if (b.size >= 3 && b[0] == 0xEF.toByte() && b[1] == 0xBB.toByte() && b[2] == 0xBF.toByte() && (enc == "auto" || enc == "utf8")) {
            enc = "utf8"; bom = true; off = 3
        } else if (b.size >= 2 && b[0] == 0xFF.toByte() && b[1] == 0xFE.toByte() && (enc == "auto" || enc == "utf16le")) {
            enc = "utf16le"; bom = true; off = 2
        } else if (b.size >= 2 && b[0] == 0xFE.toByte() && b[1] == 0xFF.toByte() && (enc == "auto" || enc == "utf16be")) {
            enc = "utf16be"; bom = true; off = 2
        } else if (enc == "auto") {
            enc = if (isUtf8(b)) "utf8" else "sjis"
        }
        val raw = String(b, off, b.size - off, charset(enc))
        val crlf = raw.contains("\r\n")
        return Loaded(if (crlf) raw.replace("\r\n", "\n") else raw, enc, bom, crlf)
    }

    /** Pair (encoding, BOM) from the "Save as" choice. */
    fun resolveSave(sel: String, d: Loaded): Pair<String, Boolean> = when (sel) {
        "same" -> Pair(d.enc, d.bom)
        "utf8" -> Pair("utf8", false)
        "utf8bom" -> Pair("utf8", true)
        "utf16le" -> Pair("utf16le", true)
        "utf16be" -> Pair("utf16be", true)
        else -> Pair(sel, false)
    }

    /** Number of characters that cannot be saved in [enc] (0 for UTF). */
    fun badChars(text: String, enc: String): Int {
        if (enc.startsWith("utf")) return 0
        val e = charset(enc).newEncoder()
        var n = 0
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (Character.isHighSurrogate(c) && i + 1 < text.length) {
                if (!e.canEncode(text.substring(i, i + 2))) n++
                i += 2
            } else {
                if (!e.canEncode(c)) n++
                i++
            }
        }
        return n
    }

    fun encode(text: String, enc: String, bom: Boolean, crlf: Boolean): ByteArray {
        val t = if (crlf) text.replace("\n", "\r\n") else text
        val body = t.toByteArray(charset(enc))
        if (!bom) return body
        val head = when (enc) {
            "utf8" -> byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
            "utf16le" -> byteArrayOf(0xFF.toByte(), 0xFE.toByte())
            "utf16be" -> byteArrayOf(0xFE.toByte(), 0xFF.toByte())
            else -> ByteArray(0)
        }
        return head + body
    }

    // ---------------- find and replace ----------------

    class Query(val find: String, val caseSensitive: Boolean, val regex: Boolean)

    private fun re(q: Query): Regex? = try {
        val opts = HashSet<RegexOption>()
        if (!q.caseSensitive) opts.add(RegexOption.IGNORE_CASE)
        if (q.regex) opts.add(RegexOption.MULTILINE)
        Regex(if (q.regex) q.find else Regex.escape(q.find), opts)
    } catch (e: Exception) {
        null
    }

    /** Regex pattern error message (null if valid or not regex mode). */
    fun regexError(q: Query): String? {
        if (!q.regex || q.find.isEmpty()) return null
        return try { Regex(q.find); null } catch (e: Exception) { e.message ?: "regex" }
    }

    /** Next match starting from [from] (wraps to the start if there are none left): [start, end). */
    fun findNext(text: String, q: Query, from: Int): IntArray? {
        if (q.find.isEmpty()) return null
        val r = re(q) ?: return null
        val m = r.find(text, from.coerceIn(0, text.length)) ?: r.find(text, 0) ?: return null
        return intArrayOf(m.range.first, m.range.last + 1)
    }

    /** Match before [before] (wraps to the end if there is none). */
    fun findPrev(text: String, q: Query, before: Int): IntArray? {
        if (q.find.isEmpty()) return null
        val r = re(q) ?: return null
        var last: MatchResult? = null
        var lastAll: MatchResult? = null
        for (m in r.findAll(text)) {
            lastAll = m
            if (m.range.first < before) last = m
        }
        val m = last ?: lastAll ?: return null
        return intArrayOf(m.range.first, m.range.last + 1)
    }

    fun count(text: String, q: Query): Int {
        if (q.find.isEmpty()) return 0
        val r = re(q) ?: return 0
        var n = 0
        for (m in r.findAll(text)) { n++; if (n >= 100000) break }
        return n
    }

    /** Replace all. null = invalid pattern/replacement. */
    fun replaceAll(text: String, q: Query, rep: String): Pair<String, Int>? {
        if (q.find.isEmpty()) return Pair(text, 0)
        val r = re(q) ?: return null
        val n = count(text, q)
        if (n == 0) return Pair(text, 0)
        return try {
            Pair(r.replace(text, if (q.regex) rep else Regex.escapeReplacement(rep)), n)
        } catch (e: Exception) {
            null
        }
    }

    /** Replacement result for one selected text [matched] (supports $1 etc. in regex mode). */
    fun replaceOne(matched: String, q: Query, rep: String): String? {
        val r = re(q) ?: return null
        return try {
            r.replaceFirst(matched, if (q.regex) rep else Regex.escapeReplacement(rep))
        } catch (e: Exception) {
            null
        }
    }

    // ---------------- lines ----------------

    /** Start position of line [line] (1-based); past the end of the text -> end of text. */
    fun lineOffset(text: String, line: Int): Int {
        if (line <= 1) return 0
        var cur = 1
        var i = 0
        while (i < text.length) {
            if (text[i] == '\n') {
                cur++
                if (cur == line) return i + 1
            }
            i++
        }
        return text.length
    }

    /** Pair (line, column) 1-based for position [pos]. */
    fun lineCol(cs: CharSequence, pos: Int): Pair<Int, Int> {
        var line = 1
        var lastNl = -1
        val end = pos.coerceIn(0, cs.length)
        for (i in 0 until end) if (cs[i] == '\n') { line++; lastNl = i }
        return Pair(line, end - lastNl)
    }

    fun lineCount(cs: CharSequence): Int {
        var n = 1
        for (i in 0 until cs.length) if (cs[i] == '\n') n++
        return n
    }
}
