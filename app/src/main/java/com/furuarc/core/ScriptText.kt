package com.furuarc.core

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

/** Kinds of text scripts whose text can be exported/imported. */
enum class ScriptKind(val id: String) {
    RPY("rpy"), // Ren'Py .rpy
    KAG("kag"), // Kirikiri .ks (KAG)
    TYRANO("tyrano"), // TyranoScript .ks
    NSCRIPT("nscript"), // NScripter / ONScripter 0.txt (decoded nscript.dat)
    QLIE("qlie"), // QLIE .s (decompiled/extracted text script)
    ARTEMIS("artemis"), // Artemis .txt
    CST("cst"), // CatSystem2 .cst (binary)
    SIGLUS("siglus"), // SiglusEngine scene .dat (binary, UTF-16 text)
    ESCUDE("escude"), // Escu:de binary script ESCR1_00 (.bin)
    HCB("hcb"), // Favorite/FVP .hcb (binary)
    WS2("ws2"), // WillPlus/AdvHD .ws2 (binary)
    SILKY("silky"), // Silky Engine .mes (binary)
    BGI("bgi"), // BGI/Ethornell script (binary, no extension)
    ITUFURU("itufuru"), // Yaneurao Itufuru (binary, no extension, not auto-detected)
    CSX("csx"), // EntisGLS/Cotopha .csx (binary, conststr section)
    CIRCUS("circus"), // Adv32 (Circus) .mes (binary)
    NEXAS("nexas"), // NeXAS .bin (binary, Shift-JIS text block)
    YSTB("yuris"), // Yu-Ris .ybn (binary, WORD command + string literal)
    ;

    companion object {
        fun of(id: String?) = values().firstOrNull { it.id == id }
    }
}

/** Contents of a decoded script file, plus how to write it back. */
class DecodedScript(val text: String, val charset: Charset, val bom: ByteArray)

/**
 * Export/import text from text-based scripts. Each text = one [TextUnit] with key `L<line>[.<n>]:<hash>`;
 * the hash is of the original text, so import does not wrongly overwrite if the script changed since export.
 */
object ScriptText {
    // ---------- encoding ----------

    private fun cs(name: String): Charset = try {
        Charset.forName(name)
    } catch (e: Exception) {
        Charsets.UTF_8
    }

    val SJIS: Charset by lazy { try { Charset.forName("windows-31j") } catch (e: Exception) { cs("Shift_JIS") } }

    /** [enc]: "auto", "sjis", "utf8", "gbk", "big5", "euckr". */
    fun charsetOf(enc: String): Charset? = when (enc) {
        "sjis" -> SJIS
        "utf8" -> Charsets.UTF_8
        "gbk" -> cs("GBK")
        "big5" -> cs("Big5")
        "euckr" -> cs("EUC-KR")
        else -> null
    }

    private fun strictUtf8(b: ByteArray, off: Int): String? = try {
        Charsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b, off, b.size - off)).toString()
    } catch (e: CharacterCodingException) {
        null
    }

    fun decode(kind: ScriptKind, b: ByteArray, enc: String = "auto"): DecodedScript {
        val forced = charsetOf(enc)
        if (b.size >= 2 && (b[0].toInt() and 0xff) == 0xFF && (b[1].toInt() and 0xff) == 0xFE) {
            return DecodedScript(String(b, 2, b.size - 2, Charsets.UTF_16LE), Charsets.UTF_16LE, byteArrayOf(0xFF.toByte(), 0xFE.toByte()))
        }
        if (b.size >= 2 && (b[0].toInt() and 0xff) == 0xFE && (b[1].toInt() and 0xff) == 0xFF) {
            return DecodedScript(String(b, 2, b.size - 2, Charsets.UTF_16BE), Charsets.UTF_16BE, byteArrayOf(0xFE.toByte(), 0xFF.toByte()))
        }
        if (b.size >= 3 && (b[0].toInt() and 0xff) == 0xEF && (b[1].toInt() and 0xff) == 0xBB && (b[2].toInt() and 0xff) == 0xBF) {
            return DecodedScript(String(b, 3, b.size - 3, Charsets.UTF_8), Charsets.UTF_8, byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
        }
        if (forced != null) return DecodedScript(String(b, forced), forced, ByteArray(0))
        val u = strictUtf8(b, 0)
        if (u != null) return DecodedScript(u, Charsets.UTF_8, ByteArray(0))
        val fallback = if (kind == ScriptKind.NSCRIPT || kind == ScriptKind.KAG) SJIS else SJIS
        return DecodedScript(String(b, fallback), fallback, ByteArray(0))
    }

    /** Kirikiri and Tyrano: the result is always Unicode (KAG: UTF-16LE + BOM; Tyrano: UTF-8), safe for all translated letters. */
    fun encode(kind: ScriptKind, d: DecodedScript, text: String): ByteArray = when (kind) {
        ScriptKind.KAG -> byteArrayOf(0xFF.toByte(), 0xFE.toByte()) + text.toByteArray(Charsets.UTF_16LE)
        ScriptKind.TYRANO -> d.bom + text.toByteArray(Charsets.UTF_8)
        else -> d.bom + text.toByteArray(d.charset)
    }

    // ---------- file identification ----------

    fun isScriptFile(kind: ScriptKind, path: String): Boolean {
        val p = path.lowercase().replace('\\', '/')
        return when (kind) {
            ScriptKind.RPY -> p.endsWith(".rpy")
            ScriptKind.KAG -> p.endsWith(".ks") || p.endsWith(".scn")
            ScriptKind.TYRANO -> p.endsWith(".ks")
            ScriptKind.QLIE -> p.endsWith(".s")
            ScriptKind.ARTEMIS -> p.endsWith(".txt") || p.endsWith(".ast")
            ScriptKind.CST -> p.endsWith(".cst")
            ScriptKind.SIGLUS -> p.endsWith(".dat")
            ScriptKind.ESCUDE -> p.endsWith(".bin")
            ScriptKind.HCB -> p.endsWith(".hcb")
            ScriptKind.WS2 -> p.endsWith(".ws2")
            ScriptKind.SILKY -> p.endsWith(".mes")
            ScriptKind.ITUFURU -> !p.substringAfterLast('/').contains('.')
            ScriptKind.CSX -> p.endsWith(".csx")
            ScriptKind.CIRCUS -> p.endsWith(".mes")
            ScriptKind.NEXAS -> p.endsWith(".bin")
            ScriptKind.YSTB -> p.endsWith(".ybn")
            ScriptKind.BGI -> !p.substringAfterLast('/').contains('.') || p.endsWith("._bs") || p.endsWith(".bs")
            ScriptKind.NSCRIPT -> {
                val n = p.substringAfterLast('/')
                n.endsWith(".txt") && (n == "0.txt" || n == "00.txt" || n.matches(Regex("\\d{1,2}\\.txt")) || n.startsWith("nscript"))
            }
        }
    }

    // ---------- automatic script type detection ----------

    /** Extensions that may be scripts; used to filter before reading the file contents. */
    fun maybeScript(path: String): Boolean {
        val n = path.lowercase()
        return n.endsWith(".rpy") || n.endsWith(".ast") || n.endsWith(".ks") || n.endsWith(".scn") || n.endsWith(".s") || n.endsWith(".cst") || n.endsWith(".txt") || n.endsWith(".dat") || n.endsWith(".bin") || n.endsWith(".hcb") || n.endsWith(".ws2") || n.endsWith(".mes") || n.endsWith(".csx") || n.endsWith(".ybn") || !n.substringAfterLast('/').contains('.')
    }

    private val TYRANO_TAG = Regex("\\[(tb_|glink|chara_new|ptext|mtext|layopt)")
    private val LABEL_LINE = Regex("(?m)^\\*[^\\s*]")
    private val TAG_LINE = Regex("(?m)^[\\[#]")

    /**
     * Guess the script type from the name and the start of the contents ([head], max ~64 KB). null = not a recognized script.
     * .rpy → Ren'Py, .cst → CatSystem2 (magic), .ks → Tyrano if it contains its typical tags, otherwise KAG, .s → QLIE,
     * .txt → NScripter (name 0.txt/nscript) or Artemis (label * and tags [ ] / # name).
     */
    fun detectKind(path: String, head: ByteArray): ScriptKind? {
        val p = path.lowercase().replace('\\', '/')
        val name = p.substringAfterLast('/')
        fun text(): String = decode(ScriptKind.KAG, head, "auto").text
        return when {
            name.endsWith(".rpy") -> ScriptKind.RPY
            name.endsWith(".ast") -> if (AstScript.isAst(text())) ScriptKind.ARTEMIS else null
            name.endsWith(".cst") -> if (CatScene.isCst(head)) ScriptKind.CST else null
            !name.contains('.') || name.endsWith("._bs") -> if (BgiScript.isHeader(head)) ScriptKind.BGI else null
            name.endsWith(".csx") -> if (EntisCsx.isCsx(head)) ScriptKind.CSX else null
            name.endsWith(".mes") -> if (CircusMes.isMes(head)) ScriptKind.CIRCUS else if (SilkyMes.isMes(head)) ScriptKind.SILKY else null
            name.endsWith(".scn") -> if (KrScn.isScript(head)) ScriptKind.KAG else null
            name.endsWith(".ws2") -> if (Ws2Script.isWs2(head)) ScriptKind.WS2 else null
            name.endsWith(".hcb") -> if (HcbScript.isHcb(head)) ScriptKind.HCB else null
            name.endsWith(".bin") -> if (EscudeScript.isScript(head)) ScriptKind.ESCUDE else if (NexasScript.isScript(head)) ScriptKind.NEXAS else null
            name.endsWith(".ybn") -> if (YuRisScript.isScript(head)) ScriptKind.YSTB else null
            name.endsWith(".dat") -> if (SiglusScene.isDatHead(head)) ScriptKind.SIGLUS else null
            name.endsWith(".ks") ->
                if (p.contains("tyrano") || TYRANO_TAG.containsMatchIn(text())) ScriptKind.TYRANO else ScriptKind.KAG
            name.endsWith(".s") -> if (head.take(4096).none { it.toInt() == 0 }) ScriptKind.QLIE else null
            name.endsWith(".txt") -> when {
                isScriptFile(ScriptKind.NSCRIPT, p) -> ScriptKind.NSCRIPT
                head.take(4096).any { it.toInt() == 0 } -> null
                else -> {
                    val t = text()
                    if (!t.contains("*define") && LABEL_LINE.containsMatchIn(t) && TAG_LINE.containsMatchIn(t)) ScriptKind.ARTEMIS else null
                }
            }
            else -> null
        }
    }

    // ---------- common interface (text and binary) ----------

    fun extractFile(kind: ScriptKind, fileKey: String, bytes: ByteArray, enc: String, full: Boolean = false): List<TextUnit> {
        if (kind == ScriptKind.CST) return CatScene.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.SIGLUS) return SiglusScene.extract(fileKey, bytes)
        if (kind == ScriptKind.ESCUDE) return EscudeScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.HCB) return HcbScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.WS2) return Ws2Script.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.KAG && KrScn.isScn(bytes)) return KrScn.extract(fileKey, bytes, full)
        if (kind == ScriptKind.CIRCUS) return CircusMes.extract(fileKey, bytes, full)
        if (kind == ScriptKind.SILKY) return SilkyMes.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.BGI) return BgiScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.ITUFURU) return ItufuruScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.CSX) return EntisCsx.extract(fileKey, bytes)
        if (kind == ScriptKind.NEXAS) return NexasScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.YSTB) return YuRisScript.extract(fileKey, bytes, charsetOf(enc) ?: SJIS)
        return extract(kind, fileKey, decode(kind, bytes, enc).text)
    }

    /** Apply translations to the file contents; returns (new bytes, number replaced). */
    fun applyFile(kind: ScriptKind, bytes: ByteArray, tr: Map<String, String>, enc: String): Pair<ByteArray, Int> {
        if (kind == ScriptKind.CST) return CatScene.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.SIGLUS) return SiglusScene.apply(bytes, tr)
        if (kind == ScriptKind.ESCUDE) return EscudeScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.HCB) return HcbScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.WS2) return Ws2Script.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.KAG && KrScn.isScn(bytes)) return KrScn.apply(bytes, tr)
        if (kind == ScriptKind.CIRCUS) return CircusMes.apply(bytes, tr)
        if (kind == ScriptKind.SILKY) return SilkyMes.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.BGI) return BgiScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.ITUFURU) return ItufuruScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.CSX) return EntisCsx.apply(bytes, tr)
        if (kind == ScriptKind.NEXAS) return NexasScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        if (kind == ScriptKind.YSTB) return YuRisScript.apply(bytes, tr, charsetOf(enc) ?: SJIS)
        val d = decode(kind, bytes, enc)
        val r = apply(kind, d.text, tr)
        return if (r.second == 0) Pair(bytes, 0) else Pair(encode(kind, d, r.first), r.second)
    }

    // ---------- util ----------

    internal fun hash(s: String): String = String.format("%08x", s.hashCode())

    private fun key(line: Int, sub: Int, text: String): String =
        "L" + (line + 1) + (if (sub > 0) ".$sub" else "") + ":" + hash(text)

    /** Split by line without losing the original line separators: each element = (content without \r, remaining "\r" or ""). */
    private fun splitLines(text: String): List<Pair<String, String>> {
        val out = ArrayList<Pair<String, String>>()
        val parts = text.split("\n")
        for ((i, p) in parts.withIndex()) {
            val last = i == parts.size - 1
            val cr = if (p.endsWith("\r")) "\r" else ""
            out.add(Pair(if (cr.isEmpty()) p else p.dropLast(1), cr + (if (last) "" else "\n")))
        }
        return out
    }

    /** One text candidate on a line: the range [start, end) within the line and its text. */
    private class Hit(val start: Int, val end: Int, val text: String, val whole: Boolean = true)

    /** Result of scanning one line: the list of translatable texts on that line. */
    private fun scan(kind: ScriptKind, lines: List<String>): List<List<Hit>> {
        val res = ArrayList<List<Hit>>(lines.size)
        when (kind) {
            ScriptKind.RPY -> scanRpy(lines, res)
            ScriptKind.KAG, ScriptKind.TYRANO -> scanKag(kind, lines, res)
            ScriptKind.NSCRIPT -> scanNs(lines, res)
            ScriptKind.QLIE -> scanQlie(lines, res)
            ScriptKind.ARTEMIS -> scanArtemis(lines, res)
            ScriptKind.CST, ScriptKind.SIGLUS, ScriptKind.ESCUDE, ScriptKind.HCB, ScriptKind.WS2, ScriptKind.SILKY, ScriptKind.CIRCUS, ScriptKind.BGI, ScriptKind.ITUFURU, ScriptKind.CSX -> throw IllegalStateException("Skrip biner")
        }
        return res
    }

    fun extract(kind: ScriptKind, fileKey: String, text: String): List<TextUnit> {
        if (kind == ScriptKind.ARTEMIS && AstScript.isAst(text)) return AstScript.extract(fileKey, text)
        val lines = splitLines(text).map { it.first }
        val hits = scan(kind, lines)
        val r = ArrayList<TextUnit>()
        for ((i, hs) in hits.withIndex()) {
            for ((k, h) in hs.withIndex()) r.add(TextUnit(fileKey, key(i, if (hs.size > 1) k + 1 else 0, h.text), h.text))
        }
        return r
    }

    /** Apply translations [tr] (key -> text). Returns the new text and the number of texts replaced. */
    fun apply(kind: ScriptKind, text: String, tr: Map<String, String>): Pair<String, Int> {
        if (kind == ScriptKind.ARTEMIS && AstScript.isAst(text)) return AstScript.apply(text, tr)
        val sl = splitLines(text)
        val hits = scan(kind, sl.map { it.first })
        var changed = 0
        val sb = StringBuilder()
        for ((i, p) in sl.withIndex()) {
            var line = p.first
            val hs = hits.getOrNull(i) ?: emptyList()
            // from the end so positions do not shift
            for ((k, h) in hs.withIndex().reversed()) {
                val t = tr[key(i, if (hs.size > 1) k + 1 else 0, h.text)] ?: continue
                if (t == h.text) continue
                line = line.substring(0, h.start) + fit(kind, t, h.whole) + line.substring(h.end)
                changed++
            }
            sb.append(line).append(p.second)
        }
        return Pair(sb.toString(), changed)
    }

    /** Adjust the translated text so it is valid in the script syntax. */
    private fun fit(kind: ScriptKind, t: String, whole: Boolean): String = when (kind) {
        // inside "...": quotes and newlines must be escaped
        ScriptKind.RPY -> t.replace(Regex("(?<!\\\\)\""), "\\\\\"").replace("\r\n", "\n").replace("\n", "\\n")
        ScriptKind.NSCRIPT -> if (!whole) t.replace("\"", "").replace("\n", " ") else {
            val s = t.replace("\r\n", "\n")
            val first = s.firstOrNull { !it.isWhitespace() }
            // NScripter lines starting with an ASCII letter are treated as commands; Latin text must start with the `
            if (s.contains('\n')) s else if (first != null && first.code < 0x80 && first != '`' && first != '\\' && first != '@' && first != '!') "`$s" else s
        }
        // one line = one text; a real newline is used as the [n] marker by QLIE
        ScriptKind.QLIE -> if (!whole) {
            val v = t.replace("\"", "").replace("\r\n", " ").replace("\n", " ")
            if (v.contains(',') || v.contains(':')) "\"$v\"" else v
        } else t.replace("\r\n", "\n").replace("\n", "[n]")
        ScriptKind.ARTEMIS -> t.replace("\r\n", " ").replace("\n", " ")
        ScriptKind.CST, ScriptKind.SIGLUS, ScriptKind.ESCUDE, ScriptKind.HCB, ScriptKind.WS2, ScriptKind.SILKY, ScriptKind.CIRCUS, ScriptKind.BGI, ScriptKind.ITUFURU, ScriptKind.CSX -> t
        else -> t.replace("\r\n", "\n")
    }

    // ---------- Ren'Py ----------

    private val RPY_SKIP_BLOCK = setOf("python", "screen", "transform", "style", "image", "layeredimage", "testcase")
    private val RPY_KEYWORDS = setOf(
        "scene", "show", "hide", "with", "window", "pause", "nvl", "play", "stop", "queue", "voice", "camera", "call",
        "jump", "return", "label", "menu", "if", "elif", "else", "while", "for", "pass", "init", "define", "default",
        "at", "behind", "expression", "rpy", "on", "contains", "use", "old", "sound", "music", "image", "style",
        "transform", "screen", "python", "translate", "layeredimage", "testcase", "add", "text", "key", "hotspot",
    )
    private val RPY_WORD = Regex("^[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*$")
    private val RPY_ATTR = Regex("^[A-Za-z_@\\-][A-Za-z0-9_@\\-.]*$")

    /** Find the end of the Ren'Py string opened at [start] (index of the opening quote); -1 if unterminated. */
    private fun endOfString(s: String, start: Int): Int {
        val q = s[start]
        var i = start + 1
        while (i < s.length) {
            val c = s[i]
            if (c == '\\') { i += 2; continue }
            if (c == q) return i
            i++
        }
        return -1
    }

    private fun indentOf(s: String): Int {
        var n = 0
        for (c in s) { if (c == ' ') n++ else if (c == '\t') n += 4 else break }
        return n
    }

    private fun scanRpy(lines: List<String>, res: MutableList<List<Hit>>) {
        var skipIndent = -1
        for (line in lines) {
            val t = line.trim()
            if (t.isEmpty()) { res.add(emptyList()); continue }
            val ind = indentOf(line)
            if (skipIndent >= 0) {
                if (ind > skipIndent) { res.add(emptyList()); continue }
                skipIndent = -1
            }
            if (t.startsWith("#") || t.startsWith("$")) { res.add(emptyList()); continue }
            val first = t.takeWhile { it.isLetterOrDigit() || it == '_' }
            if (t.endsWith(":") && !t.startsWith("\"") && !t.startsWith("'")) {
                val fw = first.lowercase()
                val isTranslatePy = fw == "translate" && Regex("^translate\\s+\\S+\\s+(python|style)\\b").containsMatchIn(t)
                if (fw in RPY_SKIP_BLOCK || (fw == "init" && Regex("\\bpython\\b").containsMatchIn(t)) || isTranslatePy) {
                    skipIndent = ind; res.add(emptyList()); continue
                }
            }
            val lead = line.length - line.trimStart().length
            // "new "..."" line in a translate ... strings block
            if (first == "new") {
                val qi = line.indexOf('"', lead + 3)
                if (qi > 0 && line.substring(lead + 3, qi).isBlank()) {
                    val e = endOfString(line, qi)
                    if (e > qi + 1) { res.add(listOf(Hit(qi + 1, e, line.substring(qi + 1, e)))); continue }
                }
                res.add(emptyList()); continue
            }
            if (first.isNotEmpty() && first.lowercase() in RPY_KEYWORDS && RPY_WORD.matches(first)) { res.add(emptyList()); continue }
            // [who attr...] "text" [...]
            var pos = lead
            var ok = true
            while (pos < line.length && line[pos] != '"' && line[pos] != '\'') {
                var e = pos
                while (e < line.length && !line[e].isWhitespace() && line[e] != '"' && line[e] != '\'') e++
                val word = line.substring(pos, e)
                if (word.isEmpty() || !(if (pos == lead) RPY_WORD.matches(word) else RPY_ATTR.matches(word))) { ok = false; break }
                if (pos == lead && word.lowercase() in RPY_KEYWORDS) { ok = false; break }
                while (e < line.length && (line[e] == ' ' || line[e] == '\t')) e++
                if (e == pos) { ok = false; break }
                pos = e
            }
            if (!ok || pos >= line.length) { res.add(emptyList()); continue }
            val end = endOfString(line, pos)
            if (end < 0) { res.add(emptyList()); continue }
            // triple-quote is not a simple line
            if (line.startsWith("\"\"\"", pos)) { res.add(emptyList()); continue }
            val body = line.substring(pos + 1, end)
            if (body.isBlank()) { res.add(emptyList()); continue }
            // after the string only these may follow: with/nointeract/id/arguments, or (menu) "if ...:" / ":"
            val rest = line.substring(end + 1).trim()
            val restOk = rest.isEmpty() || rest == ":" || rest.startsWith("with ") || rest.startsWith("nointeract") ||
                rest.startsWith("id ") || rest.startsWith("(") || rest.startsWith("if ") || rest.startsWith("@") || rest.startsWith("(")
            if (!restOk) { res.add(emptyList()); continue }
            res.add(listOf(Hit(pos + 1, end, body)))
        }
    }

    // ---------- KAG (Kirikiri / Tyrano) ----------

    private val KAG_ATTR_TAGS = Regex("\\[(glink|ptext|mtext)\\b[^\\]]*?\\btext\\s*=\\s*(\"([^\"]*)\"|'([^']*)')")

    /** Visible text: strip [tag] ([[ = literal bracket). */
    private fun kagVisible(t: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < t.length) {
            val c = t[i]
            if (c == '[') {
                if (i + 1 < t.length && t[i + 1] == '[') { sb.append('['); i += 2; continue }
                val e = t.indexOf(']', i)
                i = if (e < 0) t.length else e + 1
                continue
            }
            sb.append(c); i++
        }
        return sb.toString()
    }

    private fun scanKag(kind: ScriptKind, lines: List<String>, res: MutableList<List<Hit>>) {
        var inScript = false
        for (line in lines) {
            val t = line.trim()
            val lead = line.length - line.trimStart().length
            if (t.isEmpty() || t.startsWith(";")) { res.add(emptyList()); continue }
            if (inScript) {
                if (t.contains("[endscript]") || t.startsWith("@endscript")) inScript = false
                res.add(emptyList()); continue
            }
            if (t.startsWith("@")) {
                if (t.startsWith("@iscript")) inScript = true
                res.add(emptyList()); continue
            }
            if (t.startsWith("*")) { res.add(emptyList()); continue }
            if (t.contains("[iscript]")) { inScript = !t.contains("[endscript]"); res.add(emptyList()); continue }
            if (kind == ScriptKind.TYRANO && t.startsWith("#")) {
                if (t.length > 1) res.add(listOf(Hit(lead, lead + t.length, t))) else res.add(emptyList())
                continue
            }
            val vis = kagVisible(t)
            if (vis.isNotBlank()) {
                res.add(listOf(Hit(lead, lead + t.length, t)))
            } else if (kind == ScriptKind.TYRANO) {
                val hs = ArrayList<Hit>()
                for (m in KAG_ATTR_TAGS.findAll(line)) {
                    val g = if (m.groups[3] != null) m.groups[3]!! else m.groups[4]!!
                    if (g.value.isNotBlank()) hs.add(Hit(g.range.first, g.range.last + 1, g.value, false))
                }
                res.add(hs)
            } else res.add(emptyList())
        }
    }

    // ---------- NScripter ----------

    private val NS_SELECT = Regex("^(select|selgosub|selnum)\\b", RegexOption.IGNORE_CASE)

    private fun scanNs(lines: List<String>, res: MutableList<List<Hit>>) {
        for (line in lines) {
            val t = line.trim()
            val lead = line.length - line.trimStart().length
            if (t.isEmpty() || t.startsWith(";") || t.startsWith("*")) { res.add(emptyList()); continue }
            val c = t[0]
            if (c.code > 0x7f || c == '`') {
                res.add(listOf(Hit(lead, lead + t.length, t)))
            } else if (NS_SELECT.containsMatchIn(t)) {
                val hs = ArrayList<Hit>()
                var i = lead
                while (i < line.length) {
                    if (line[i] == '"') {
                        val e = line.indexOf('"', i + 1)
                        if (e < 0) break
                        val body = line.substring(i + 1, e)
                        if (body.any { it.code > 0x7f }) hs.add(Hit(i + 1, e, body, false))
                        i = e + 1
                    } else i++
                }
                res.add(hs)
            } else res.add(emptyList())
        }
    }

    // ---------- QLIE (.s) ----------

    /** Split QLIE tag arguments (comma, wrapped in quotes). Returns the range [start,end) of each argument in [s]. */
    private fun qlieArgs(s: String, from: Int): List<IntArray> {
        val out = ArrayList<IntArray>()
        var inQ = false
        var st = from
        for (i in from until s.length) {
            val c = s[i]
            if (c == '"') inQ = !inQ
            else if (c == ',' && !inQ) { out.add(intArrayOf(st, i)); st = i + 1 }
        }
        out.add(intArrayOf(st, s.length))
        return out
    }

    private fun scanQlie(lines: List<String>, res: MutableList<List<Hit>>) {
        for (line in lines) {
            val t = line.trim()
            val lead = line.length - line.trimStart().length
            if (t.isEmpty()) { res.add(emptyList()); continue }
            val c = t[0]
            if (c == '@' || c == '\\' || c == '％' || c == '%' || (c == '【' && t.endsWith("】"))) { res.add(emptyList()); continue }
            if (c == '^') {
                val low = t.lowercase()
                val hs = ArrayList<Hit>()
                val args = qlieArgs(line, lead + 1)
                // args[0] = tag name
                val name = line.substring(args[0][0], args[0][1]).trim().lowercase()
                if (name == "select") {
                    for (a in args.drop(1)) addQlieArg(line, a, hs)
                } else if (name == "savetext" && args.size >= 2) {
                    addQlieArg(line, args[1], hs)
                }
                if (low.isEmpty()) hs.clear()
                res.add(hs); continue
            }
            // plain text: [n] = newline inside the game, left as is
            res.add(listOf(Hit(lead, lead + t.length, t)))
        }
    }

    private fun addQlieArg(line: String, a: IntArray, hs: MutableList<Hit>) {
        var s = a[0]; var e = a[1]
        while (s < e && line[s] == ' ') s++
        while (e > s && line[e - 1] == ' ') e--
        if (s >= e) return
        val raw = line.substring(s, e)
        if (raw.contains(':') && !raw.startsWith("\"")) return // key:value, not text
        val v = raw.replace("\"", "")
        if (v.isBlank()) return
        hs.add(Hit(s, e, v, false))
    }

    // ---------- Artemis (.txt) ----------

    private fun scanArtemis(lines: List<String>, res: MutableList<List<Hit>>) {
        for (line in lines) {
            val t = line.trim()
            val lead = line.length - line.trimStart().length
            if (t.isEmpty() || t.startsWith("//") || t[0] == '*' || t[0] == '[' || t[0] == '#') { res.add(emptyList()); continue }
            res.add(listOf(Hit(lead, lead + t.length, t)))
        }
    }

    // ---------- nscript.dat ----------

    /** nscript.dat / nscr_sec.dat: XOR 0x84 over the whole contents (also for re-encoding). */
    fun nsXor(b: ByteArray): ByteArray {
        val o = ByteArray(b.size)
        for (i in b.indices) o[i] = (b[i].toInt() xor 0x84).toByte()
        return o
    }

    /** Does [b] look like decoded NScripter text (not XOR)? */
    fun nsLooksDecoded(b: ByteArray): Boolean {
        val n = minOf(b.size, 4096)
        if (n == 0) return false
        var ok = 0
        for (i in 0 until n) {
            val v = b[i].toInt() and 0xff
            if (v == 0x0a || v == 0x0d || v == 0x09 || (v in 0x20..0x7e) || v >= 0x80) ok++
        }
        return ok * 100 / n >= 97
    }
}
