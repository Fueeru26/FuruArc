package com.furuarc.core

import java.io.ByteArrayInputStream
import java.util.regex.Pattern
import java.util.zip.InflaterInputStream

/**
 * Decompile .rpyc / .rpymc (Ren'Py) into .rpy.
 *
 * Partial port of unrpyc (https://github.com/CensoredUsername/unrpyc, MIT license,
 * (c) 2012-2024 Yuri K. Schlesner, CensoredUsername, Jackmcbarn). Supports story flow (label, say, menu, jump/call,
 * if/while, scene/show/hide/with, ATL), define/default/init/python, style, translate. Screens (screen language),
 * testcase, and very specialized ATL blocks are written as comments / skipped.
 */
object Rpyc {
    /** Get the list of statements (AST) from the contents of a .rpyc file. */
    fun readAst(raw: ByteArray): List<Any?> {
        val blob: ByteArray
        if (raw.size >= 10 && String(raw, 0, 10, Charsets.ISO_8859_1) == "RENPY RPC2") {
            var pos = 10
            val slots = HashMap<Int, Pair<Int, Int>>()
            val bb = java.nio.ByteBuffer.wrap(raw).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            while (pos + 12 <= raw.size) {
                val slot = bb.getInt(pos); val start = bb.getInt(pos + 4); val len = bb.getInt(pos + 8)
                pos += 12
                if (slot == 0) break
                slots[slot] = Pair(start, len)
            }
            val sl = slots[1] ?: slots.values.firstOrNull() ?: throw IllegalArgumentException("Slot data tidak ada")
            require(sl.first >= 0 && sl.second >= 0 && sl.first + sl.second <= raw.size) { "Header rpyc rusak" }
            blob = inflate(raw.copyOfRange(sl.first, sl.first + sl.second))
        } else {
            blob = try {
                inflate(raw)
            } catch (e: Exception) {
                throw IllegalArgumentException("Bukan file rpyc Ren'Py")
            }
        }
        val top = PyPickle.parse(blob)
        val stmts = (top as? List<*>)?.getOrNull(1) as? List<*> ?: throw IllegalArgumentException("Struktur rpyc tidak dikenali")
        return stmts
    }

    private fun inflate(b: ByteArray): ByteArray = InflaterInputStream(ByteArrayInputStream(b)).use { it.readBytes() }

    fun decompile(raw: ByteArray): String {
        val d = RpycDecompiler()
        return d.dump(readAst(raw))
    }
}

// ---------- object access helpers ----------

private fun PyObj.has(n: String) = attrs.containsKey(n)

/** Text from str / PyExpr / bytes; null for None or other types. */
internal fun pyStr(x: Any?): String? = when (x) {
    is String -> x
    is PyBytes -> x.text()
    is PyObj -> (x.args.firstOrNull() as? String) ?: (x.args.firstOrNull() as? PyBytes)?.text()
    else -> null
}

private fun pyList(x: Any?): List<Any?> = when (x) {
    is List<*> -> x
    is PyObj -> x.items
    else -> emptyList()
}

private fun pyIsList(x: Any?) = x is List<*> || (x is PyObj && x.cls.endsWith("List"))

private fun PyObj.s(n: String): String? = pyStr(attrs[n])
private fun PyObj.b(n: String, def: Boolean): Boolean = (attrs[n] as? Boolean) ?: def
private fun PyObj.i(n: String): Long? = attrs[n] as? Long

private fun lineOf(x: Any?): Long? {
    if (x is PyObj) {
        x.i("linenumber")?.let { return it }
        // PyExpr: 3rd argument = line number
        if (x.cls.endsWith("PyExpr")) return x.args.getOrNull(2) as? Long
    }
    return null
}

private fun isPyExpr(x: Any?) = x is PyObj && x.cls.endsWith("PyExpr")

private fun stringEscape(s: String) = s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")

private val SPACE_AFTER_SPACE = Regex("(?<= ) ")

private fun encodeSayString(s: String): String {
    var t = s.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"")
    t = SPACE_AFTER_SPACE.replace(t, "\\\\ ")
    return "\"$t\""
}

private class Words(var needsSpace: Boolean, val reorderable: Boolean = false) {
    val words = ArrayList<String>()
    fun add(vararg a: String?) { for (x in a) if (!x.isNullOrEmpty()) words.add(x) }
    fun join(): String {
        if (words.isEmpty()) return ""
        if (reorderable && words.last().last() == ' ') {
            for (i in words.indices.reversed()) {
                if (words[i].last() != ' ') { words.add(words.removeAt(i)); break }
            }
        }
        val last = words.last()
        val rest = words.subList(0, words.size - 1).map { if (it.last() == ' ') it.dropLast(1) else it }
        val rv = (if (needsSpace) " " else "") + (rest + last).joinToString(" ")
        needsSpace = rv.last() != ' '
        return rv
    }
}

private val STRING_RE: Pattern = Pattern.compile(
    "u?(\"(?:\"\")?|'(?:'')?).*?(?<=[^\\\\])(?:\\\\\\\\)*\\1", Pattern.DOTALL,
)

/** Split Python code into logical lines (multi-line strings and parentheses are not split). */
internal fun splitLogicalLines(s: String): List<String> {
    val lines = ArrayList<String>()
    var pos = 0
    var start = 0
    var contained = 0
    val n = s.length
    val m = STRING_RE.matcher(s)
    m.useTransparentBounds(true).useAnchoringBounds(false)
    while (pos < n) {
        val c = s[pos]
        if (c == '\n' && contained == 0 && (pos == 0 || s[pos - 1] != '\\')) {
            lines.add(s.substring(start, pos)); pos++; start = pos; continue
        }
        if (c == '(' || c == '[' || c == '{') { contained++; pos++; continue }
        if ((c == ')' || c == ']' || c == '}') && contained > 0) { contained--; pos++; continue }
        if (c == '#') { while (pos < n && s[pos] != '\n') pos++; continue }
        m.region(pos, n)
        if (m.lookingAt()) { pos = m.end(); continue }
        // one word, consecutive spaces, or one symbol
        var j = pos
        if (s[j].isLetterOrDigit() || s[j] == '_') { while (j < n && (s[j].isLetterOrDigit() || s[j] == '_')) j++ }
        else if (s[j] == ' ') { while (j < n && s[j] == ' ') j++ }
        else j++
        pos = j
    }
    if (pos != start) lines.add(s.substring(start))
    return lines
}

private fun reconstructParamInfo(p: Any?): String {
    if (p !is PyObj) return ""
    val rv = StringBuilder("(")
    var first = true
    fun sep() { if (!first) rv.append(", "); first = false }

    fun nameDefault(x: Any?): Pair<String, String?> {
        val l = x as? List<*>
        return Pair(pyStr(l?.getOrNull(0)) ?: "", pyStr(l?.getOrNull(1)))
    }

    if (p.has("positional_only")) {
        val po = pyList(p["positional_only"]).map { nameDefault(it) }
        val ko = pyList(p["keyword_only"]).map { nameDefault(it) }
        val acc = (po + ko).map { it.first }.toSet()
        val other = pyList(p["parameters"]).map { nameDefault(it) }.filter { it.first !in acc }
        for ((n, d) in po) { sep(); rv.append(n); if (d != null) rv.append("=").append(d) }
        if (po.isNotEmpty()) { sep(); rv.append("/") }
        for ((n, d) in other) { sep(); rv.append(n); if (d != null) rv.append("=").append(d) }
        val extrapos = pyStr(p["extrapos"])
        if (!extrapos.isNullOrEmpty()) { sep(); rv.append("*").append(extrapos) }
        else if (ko.isNotEmpty()) { sep(); rv.append("*") }
        for ((n, d) in ko) { sep(); rv.append(n); if (d != null) rv.append("=").append(d) }
        val extrakw = pyStr(p["extrakw"])
        if (!extrakw.isNullOrEmpty()) { sep(); rv.append("**").append(extrakw) }
    } else if (p.has("extrapos")) {
        val params = pyList(p["parameters"]).map { nameDefault(it) }
        val positional = pyList(p["positional"]).mapNotNull { pyStr(it) }.toSet()
        val pos = params.filter { it.first in positional }
        val nameonly = params.filter { it.first !in positional }
        for ((n, d) in pos) { sep(); rv.append(n); if (d != null) rv.append("=").append(d) }
        val extrapos = pyStr(p["extrapos"])
        if (!extrapos.isNullOrEmpty()) { sep(); rv.append("*").append(extrapos) }
        if (nameonly.isNotEmpty()) {
            if (extrapos.isNullOrEmpty()) { sep(); rv.append("*") }
            for ((n, d) in nameonly) { sep(); rv.append(n); if (d != null) rv.append("=").append(d) }
        }
        val extrakw = pyStr(p["extrakw"])
        if (!extrakw.isNullOrEmpty()) { sep(); rv.append("**").append(extrakw) }
    } else {
        var state = 1
        val params = (p["parameters"] as? PyObj)?.entries?.values ?: pyList(p["parameters"])
        for (pr in params) {
            val q = pr as? PyObj ?: continue
            sep()
            val name = q.s("name") ?: ""
            val kind = (q.attrs["kind"] as? Long)?.toInt() ?: ((q.attrs["_kind"] as? Long)?.toInt() ?: 1)
            val def = pyStr(q.attrs["default"]) ?: pyStr(q.attrs["_default"])
            when (kind) {
                0 -> { state = 0; rv.append(name); if (def != null) rv.append("=").append(def) }
                else -> {
                    if (state == 0) { state = 1; rv.append("/"); sep() }
                    when (kind) {
                        1 -> { rv.append(name); if (def != null) rv.append("=").append(def) }
                        2 -> { state = 2; rv.append("*").append(name) }
                        3 -> {
                            if (state == 1) { state = 2; rv.append("*"); sep() }
                            rv.append(name); if (def != null) rv.append("=").append(def)
                        }
                        4 -> { state = 3; rv.append("**").append(name) }
                    }
                }
            }
        }
    }
    rv.append(")")
    return rv.toString()
}

private fun reconstructArgInfo(a: Any?): String {
    if (a !is PyObj) return ""
    val rv = StringBuilder("(")
    var first = true
    fun sep() { if (!first) rv.append(", "); first = false }
    val args = pyList(a["arguments"])
    if (a.has("starred_indexes") || !a.has("extrapos")) {
        val star = pyList(a["starred_indexes"]).toSet()
        val dstar = pyList(a["doublestarred_indexes"]).toSet()
        for ((i, x) in args.withIndex()) {
            val l = x as? List<*> ?: continue
            sep()
            val name = pyStr(l.getOrNull(0))
            if (name != null) rv.append(name).append("=")
            else if (i.toLong() in star) rv.append("*")
            else if (i.toLong() in dstar) rv.append("**")
            rv.append(pyStr(l.getOrNull(1)) ?: "")
        }
    } else {
        for (x in args) {
            val l = x as? List<*> ?: continue
            sep()
            val name = pyStr(l.getOrNull(0))
            if (name != null) rv.append(name).append("=")
            rv.append(pyStr(l.getOrNull(1)) ?: "")
        }
        val ep = pyStr(a["extrapos"]); if (!ep.isNullOrEmpty()) { sep(); rv.append("*").append(ep) }
        val ek = pyStr(a["extrakw"]); if (!ek.isNullOrEmpty()) { sep(); rv.append("**").append(ek) }
    }
    rv.append(")")
    return rv.toString()
}

internal class RpycDecompiler {
    private var out = StringBuilder()
    private var linenumber = 1
    private var indentLevel = 0
    private var skipIndent = true
    private val blockStack = ArrayList<List<Any?>>()
    private val indexStack = ArrayList<Int>()

    private var paired: Any = false // false | true | String
    private var sayInsideMenu: PyObj? = null
    private var labelInsideMenu: PyObj? = null
    private var inInit = false
    private var missingInit = false
    private var seenLabel = false
    private val rpyArgs = ArrayList<String>()

    fun dump(ast: List<Any?>): String {
        printNodes(ast)
        write("\n# Decompiled by FuruArc (port of unrpyc: https://github.com/CensoredUsername/unrpyc)\n")
        return out.toString()
    }

    // ---------- writing ----------

    private fun write(s: String) {
        for (c in s) if (c == '\n') linenumber++
        skipIndent = false
        out.append(s)
    }

    private fun indent() {
        if (!skipIndent) write("\n" + "    ".repeat(indentLevel))
    }

    private fun advanceToLine(n: Long?) {
        if (n == null) return
        if (linenumber < n) write("\n".repeat(maxOf(0L, n - linenumber - 1).toInt()))
    }

    private inline fun withIndent(extra: Int, f: () -> Unit) {
        indentLevel += extra
        try { f() } finally { indentLevel -= extra }
    }

    private val block get() = blockStack.last()
    private val index get() = indexStack.last()
    private val parent: Any? get() = if (blockStack.size < 2) null else blockStack[blockStack.size - 2][indexStack[indexStack.size - 2]]

    private fun printNodes(ast: List<Any?>, extra: Int = 0) {
        withIndent(extra) {
            blockStack.add(ast); indexStack.add(0)
            for ((i, node) in ast.withIndex()) {
                indexStack[indexStack.size - 1] = i
                printNode(node)
            }
            blockStack.removeAt(blockStack.size - 1); indexStack.removeAt(indexStack.size - 1)
        }
    }

    private fun failure(msg: String) {
        indent(); write("pass # <<<COULD NOT DECOMPILE: $msg>>>")
    }

    private fun printNode(node: Any?) {
        if (node !is PyObj) { failure("Unknown AST node: $node"); return }
        val t = node.shortName
        if (t == "Say" && handleSayPossiblyInsideMenu(node)) return
        if (node.has("linenumber") && t !in setOf("TranslateString", "With", "Label", "Pass", "Return")) {
            advanceToLine(node.i("linenumber"))
        }
        when (t) {
            "Image" -> printImage(node)
            "Transform" -> printTransform(node)
            "Show" -> printShow(node)
            "ShowLayer" -> printShowLayer(node)
            "Scene" -> printScene(node)
            "Hide" -> printHide(node)
            "With" -> printWith(node)
            "Camera" -> printCamera(node)
            "Label" -> printLabel(node)
            "Jump" -> printJump(node)
            "Call" -> printCall(node)
            "Return" -> printReturn(node)
            "If" -> printIf(node)
            "While" -> printWhile(node)
            "Pass" -> printPass(node)
            "Init" -> printInit(node)
            "Menu" -> printMenu(node)
            "Python" -> printPython(node, false)
            "EarlyPython" -> printPython(node, true)
            "Define" -> printDefine(node)
            "Default" -> printDefault(node)
            "Say" -> printSay(node, false)
            "UserStatement" -> printUserStatement(node)
            "Style" -> printStyle(node)
            "Translate" -> printTranslate(node)
            "EndTranslate" -> {}
            "TranslateString" -> printTranslateString(node)
            "TranslateBlock", "TranslateEarlyBlock" -> printTranslateBlock(node)
            "TranslateSay" -> printTranslateSay(node)
            "Screen" -> printScreen(node)
            "Testcase" -> { requireInit(); indent(); write("# [FuruArc] testcase ${node.s("label")} tidak didekompilasi") }
            "RPY" -> printRpy(node)
            "TranslatePython" -> printTranslateBlock(node)
            else -> failure("Unknown AST node: ${node.cls}")
        }
    }

    // ---------- displayable ----------

    private fun printImspec(im: Any?): Boolean {
        val l = im as? List<*> ?: return false
        val begin = if (l.getOrNull(1) != null) "expression ${pyStr(l[1])}" else pyList(l.getOrNull(0)).joinToString(" ") { pyStr(it) ?: "" }
        val w = Words(begin.isNotEmpty() && begin.last() != ' ', true)
        pyStr(l.getOrNull(2))?.let { w.add("as $it") }
        val behind = pyList(l.getOrNull(6))
        if (behind.isNotEmpty()) w.add("behind ${behind.joinToString(", ") { pyStr(it) ?: "" }}")
        val layer = l.getOrNull(4)
        if (layer is String || layer is PyBytes) w.add("onlayer ${pyStr(layer)}")
        l.getOrNull(5)?.let { w.add("zorder ${pyStr(it) ?: it}") }
        val at = pyList(l.getOrNull(3))
        if (at.isNotEmpty()) w.add("at ${at.joinToString(", ") { pyStr(it) ?: "" }}")
        write(begin + w.join())
        return w.needsSpace
    }

    private fun truthy(x: Any) = x != false && x != ""

    private fun withPaired(needsSpace: Boolean) {
        if (truthy(paired)) {
            if (needsSpace) write(" ")
            write("with $paired")
            paired = true
        }
    }

    private fun printImage(a: PyObj) {
        requireInit(); indent()
        write("image ${pyList(a["imgname"]).joinToString(" ") { pyStr(it) ?: "" }}")
        val code = a["code"] as? PyObj
        if (code != null) write(" = ${code.s("source")}")
        else if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    private fun printTransform(a: PyObj) {
        requireInit(); indent()
        var priority = ""
        val par = parent
        if (par is PyObj && par.shortName == "Init") {
            val pr = par.i("priority") ?: 0
            if (pr != 0L && par.attrs["block"].let { pyList(it).size == 1 } && !shouldComeBefore(par, a)) priority = " $pr"
        }
        write("transform$priority ${a.s("varname")}")
        if (a["parameters"] != null) write(reconstructParamInfo(a["parameters"]))
        if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    private fun printShow(a: PyObj) {
        indent(); write("show ")
        val ns = printImspec(a["imspec"])
        withPaired(ns)
        if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    private fun printShowLayer(a: PyObj) {
        indent(); write("show layer ${a.s("layer") ?: "master"}")
        val at = pyList(a["at_list"])
        if (at.isNotEmpty()) write(" at ${at.joinToString(", ") { pyStr(it) ?: "" }}")
        if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    private fun printScene(a: PyObj) {
        indent(); write("scene")
        val ns: Boolean
        if (a["imspec"] == null) {
            val layer = a["layer"]
            if (layer is String) write(" onlayer $layer")
            ns = true
        } else {
            write(" ")
            ns = printImspec(a["imspec"])
        }
        withPaired(ns)
        if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    private fun printHide(a: PyObj) {
        indent(); write("hide ")
        val ns = printImspec(a["imspec"])
        withPaired(ns)
    }

    private fun printWith(a: PyObj) {
        val pr = a.s("paired")
        if (pr != null) {
            val nx = block.getOrNull(index + 2) as? PyObj
            if (!(nx != null && nx.shortName == "With" && nx.s("expr") == pr)) {
                throw IllegalStateException("Unmatched paired with $pr != ${a.s("expr")}")
            }
            paired = pr
        } else if (truthy(paired)) {
            if (paired != true) write(" with ${a.s("expr")}")
            paired = false
        } else {
            advanceToLine(a.i("linenumber"))
            indent(); write("with ${a.s("expr")}")
            paired = false
        }
    }

    private fun printCamera(a: PyObj) {
        indent(); write("camera")
        val layer = a.s("layer") ?: "master"
        if (layer != "master") write(" $layer")
        val at = pyList(a["at_list"])
        if (at.isNotEmpty()) write(" at ${at.joinToString(", ") { pyStr(it) ?: "" }}")
        if (a["atl"] != null) { write(":"); printAtl(a["atl"]) }
    }

    // ---------- flow ----------

    private fun labelName(a: PyObj) = a.s("name") ?: a.s("_name") ?: ""

    private fun printLabel(a: PyObj) {
        seenLabel = true
        if (index > 0 && (block[index - 1] as? PyObj)?.shortName == "Call") return

        val blk = pyList(a["block"])
        if (blk.isEmpty() && a["parameters"] == null) {
            val remaining = block.size - index
            if (remaining > 1) {
                val next = block[index + 1] as? PyObj
                if (next != null && next.shortName == "Menu" && next.i("linenumber") == a.i("linenumber")) {
                    labelInsideMenu = a; return
                }
                if (remaining > 2) {
                    val nn = block[index + 2] as? PyObj
                    if (next != null && next.shortName == "Say" && nn != null && nn.shortName == "Menu" &&
                        nn.i("linenumber") == a.i("linenumber") && sayBelongsToMenu(next, nn)
                    ) {
                        labelInsideMenu = a; return
                    }
                }
            }
        }
        advanceToLine(a.i("linenumber"))
        indent()
        val orig = out
        out = StringBuilder()
        val prevMissing = missingInit
        missingInit = false
        try {
            write("label ${labelName(a)}${reconstructParamInfo(a["parameters"])}${if (a.b("hide", false)) " hide" else ""}:")
            printNodes(blk, 1)
        } finally {
            if (missingInit) orig.append("init ")
            missingInit = prevMissing
            orig.append(out)
            out = orig
        }
    }

    private fun printJump(a: PyObj) {
        indent()
        write("jump ${if (a.b("expression", false)) "expression " else ""}${a.s("target")}")
    }

    private fun printCall(a: PyObj) {
        indent()
        val w = Words(false)
        w.add("call")
        val expr = a.b("expression", false)
        if (expr) w.add("expression")
        w.add(a.s("label"))
        if (a["arguments"] != null) {
            if (expr) w.add("pass")
            w.add(reconstructArgInfo(a["arguments"]))
        }
        val next = block.getOrNull(index + 1) as? PyObj
        if (next != null && next.shortName == "Label") w.add("from ${labelName(next)}")
        write(w.join())
    }

    private fun printReturn(a: PyObj) {
        if (a["expression"] == null && parent == null && index + 1 == block.size && index > 0) {
            val prev = block[index - 1] as? PyObj
            if (a.i("linenumber") == prev?.i("linenumber") || prev?.shortName == "Return" || prev?.shortName == "Jump" || !seenLabel) return
        }
        advanceToLine(a.i("linenumber"))
        indent(); write("return")
        a.s("expression")?.let { write(" $it") }
    }

    private fun printIf(a: PyObj) {
        val entries = pyList(a["entries"])
        for ((i, e) in entries.withIndex()) {
            val l = e as? List<*> ?: continue
            val cond = l.getOrNull(0)
            val blk = pyList(l.getOrNull(1))
            if (i + 1 == entries.size && !isPyExpr(cond)) {
                indent(); write("else:")
            } else {
                lineOf(cond)?.let { advanceToLine(it) }
                indent(); write("${if (i == 0) "if" else "elif"} ${pyStr(cond)}:")
            }
            printNodes(blk, 1)
        }
    }

    private fun printWhile(a: PyObj) {
        indent(); write("while ${a.s("condition")}:")
        printNodes(pyList(a["block"]), 1)
    }

    private fun printPass(a: PyObj) {
        if (index > 0 && (block[index - 1] as? PyObj)?.shortName == "Call") return
        if (index > 1 && (block[index - 2] as? PyObj)?.shortName == "Call" && (block[index - 1] as? PyObj)?.shortName == "Label" &&
            (block[index - 2] as? PyObj)?.i("linenumber") == a.i("linenumber")
        ) return
        advanceToLine(a.i("linenumber"))
        indent(); write("pass")
    }

    private fun shouldComeBefore(first: PyObj, second: PyObj): Boolean {
        val a = first.i("linenumber"); val b = second.i("linenumber")
        return a != null && b != null && a < b
    }

    private fun requireInit() { if (!inInit) missingInit = true }

    private fun printInit(a: PyObj) {
        val wasInit = inInit
        inInit = true
        try {
            val blk = pyList(a["block"])
            val pr = a.i("priority") ?: 0
            val b0 = blk.firstOrNull() as? PyObj
            val t0 = b0?.shortName
            if (blk.size == 1 && b0 != null && !shouldComeBefore(a, b0) && (
                    t0 == "Define" || t0 == "Default" || t0 == "Transform" ||
                        (pr == -500L && t0 == "Screen") ||
                        (pr == 0L && t0 == "Style") ||
                        (pr == 500L && t0 == "Testcase") ||
                        (pr == 0L && t0 == "UserStatement" && (b0.s("line") ?: "").startsWith("layeredimage ")) ||
                        (pr == 500L && t0 == "Image")
                    )
            ) {
                printNodes(blk)
            } else if (blk.isNotEmpty() && pr == 0L && blk.all { (it as? PyObj)?.shortName == "TranslateString" } &&
                blk.all { (it as PyObj).s("language") == (blk[0] as PyObj).s("language") }
            ) {
                printNodes(blk)
            } else {
                indent(); write("init")
                if (pr != 0L) write(" $pr")
                if (blk.size == 1 && b0 != null && !shouldComeBefore(a, b0)) {
                    write(" ")
                    skipIndent = true
                    printNodes(blk)
                } else {
                    write(":")
                    printNodes(blk, 1)
                }
            }
        } finally {
            inInit = wasInit
        }
    }

    // ---------- menu / say ----------

    private fun sayBelongsToMenu(say: PyObj, menu: PyObj): Boolean {
        if (menu.shortName != "Menu") return false
        val items = pyList(menu["items"])
        val firstBlock = (items.firstOrNull() as? List<*>)?.getOrNull(2)
        return !say.b("interact", true) && say.s("who") != null && say.s("with_") == null && say["attributes"] == null &&
            firstBlock != null && !shouldComeBefore(say, menu)
    }

    private fun handleSayPossiblyInsideMenu(a: PyObj): Boolean {
        val next = block.getOrNull(index + 1) as? PyObj ?: return false
        if (sayBelongsToMenu(a, next)) { sayInsideMenu = a; return true }
        return false
    }

    private fun printSayInsideMenu() {
        val s = sayInsideMenu ?: return
        printSay(s, true)
        sayInsideMenu = null
    }

    private fun printMenuItem(label: String, cond: Any?, blk: Any?, args: Any?) {
        indent()
        write("\"${stringEscape(label)}\"")
        if (args != null) write(reconstructArgInfo(args))
        if (blk != null) {
            if (isPyExpr(cond)) write(" if ${pyStr(cond)}")
            write(":")
            printNodes(pyList(blk), 1)
        }
    }

    private fun printMenu(a: PyObj) {
        indent(); write("menu")
        labelInsideMenu?.let { write(" ${labelName(it)}"); labelInsideMenu = null }
        if (a["arguments"] != null) write(reconstructArgInfo(a["arguments"]))
        write(":")
        withIndent(1) {
            a.s("with_")?.let { indent(); write("with $it") }
            a.s("set")?.let { indent(); write("set $it") }
            val items = pyList(a["items"])
            val itemArgs = a["item_arguments"]?.let { pyList(it) }
            for ((i, it) in items.withIndex()) {
                val l = it as? List<*> ?: continue
                val label = pyStr(l.getOrNull(0)) ?: ""
                val cond = l.getOrNull(1)
                val blk = l.getOrNull(2)
                val args = itemArgs?.getOrNull(i)
                if (sayInsideMenu != null) {
                    val cl = lineOf(cond)
                    if (cl != null) {
                        if (cl > linenumber + 1) printSayInsideMenu()
                        advanceToLine(cl)
                    } else {
                        printSayInsideMenu()
                    }
                }
                printMenuItem(label, cond, blk, args)
            }
            if (sayInsideMenu != null) printSayInsideMenu()
        }
    }

    private fun sayCode(a: PyObj, inMenu: Boolean): String {
        val rv = ArrayList<String>()
        a.s("who")?.takeIf { it.isNotEmpty() }?.let { rv.add(it) }
        if (a["attributes"] != null) pyList(a["attributes"]).forEach { rv.add(pyStr(it) ?: "") }
        if (a["temporary_attributes"] != null) {
            rv.add("@")
            pyList(a["temporary_attributes"]).forEach { rv.add(pyStr(it) ?: "") }
        }
        rv.add(encodeSayString(a.s("what") ?: ""))
        if (!a.b("interact", true) && !inMenu) rv.add("nointeract")
        if (a.b("explicit_identifier", false)) {
            rv.add("id"); rv.add(a.s("identifier") ?: "")
        } else if (a.s("identifier") != null) {
            rv.add("id"); rv.add(a.s("identifier")!!)
        }
        if (a["arguments"] != null) rv.add(reconstructArgInfo(a["arguments"]))
        a.s("with_")?.takeIf { it.isNotEmpty() }?.let { rv.add("with"); rv.add(it) }
        return rv.joinToString(" ")
    }

    private fun printSay(a: PyObj, inMenu: Boolean) {
        indent(); write(sayCode(a, inMenu))
    }

    // ---------- python ----------

    private fun printPython(a: PyObj, early: Boolean) {
        indent()
        val code = (a["code"] as? PyObj)?.s("source") ?: ""
        val indented = code.isNotEmpty() && code[0] == ' '
        val leadingNl = code.isNotEmpty() && code[0] == '\n'
        if (!(indented || leadingNl)) { write("$ $code"); return }
        val body = if (leadingNl) code.substring(1) else code
        write("python")
        if (early) write(" early")
        if (a.b("hide", false)) write(" hide")
        val store = a.s("store") ?: "store"
        if (store != "store") write(" in " + store.removePrefix("store."))
        write(":")
        if (indented) {
            write("\n$body")
        } else {
            withIndent(1) {
                for (line in splitLogicalLines(body)) {
                    if (line.isEmpty()) write("\n") else { indent(); write(line) }
                }
            }
        }
    }

    private fun initPriority(a: PyObj): String {
        val par = parent
        if (par is PyObj && par.shortName == "Init") {
            val pr = par.i("priority") ?: 0
            if (pr != 0L && pyList(par["block"]).size == 1 && !shouldComeBefore(par, a)) return " $pr"
        }
        return ""
    }

    private fun storeName(a: PyObj, name: String): String {
        val st = a.s("store") ?: "store"
        return if (st == "store") name else st.removePrefix("store.") + "." + name
    }

    private fun printDefine(a: PyObj) {
        requireInit(); indent()
        val idx = (a["index"] as? PyObj)?.s("source")?.let { "[$it]" } ?: ""
        val op = a.s("operator") ?: "="
        write("define${initPriority(a)} ${storeName(a, a.s("varname") ?: "")}$idx $op ${(a["code"] as? PyObj)?.s("source")}")
    }

    private fun printDefault(a: PyObj) {
        requireInit(); indent()
        write("default${initPriority(a)} ${storeName(a, a.s("varname") ?: "")} = ${(a["code"] as? PyObj)?.s("source")}")
    }

    private fun printUserStatement(a: PyObj) {
        indent(); write(a.s("line") ?: "")
        val blk = a["block"]
        if (blk != null && pyList(blk).isNotEmpty()) withIndent(1) { printLex(pyList(blk)) }
    }

    private fun printLex(lex: List<Any?>) {
        for (e in lex) {
            val l = e as? List<*> ?: continue
            val (lineno, content, blk) = if (l.size == 4) Triple(l[1], l[2], l[3]) else Triple(l.getOrNull(1), l.getOrNull(3), l.getOrNull(4))
            advanceToLine(lineno as? Long)
            indent(); write(pyStr(content) ?: "")
            val sub = pyList(blk)
            if (sub.isNotEmpty()) withIndent(1) { printLex(sub) }
        }
    }

    private fun printStyle(a: PyObj) {
        requireInit()
        val ln = a.i("linenumber") ?: 0L
        val kw = java.util.TreeMap<Long, Words>()
        fun w(line: Long) = kw.getOrPut(line) { Words(false, line == ln) }
        a.s("parent")?.let { w(ln).add("is $it") }
        if (a.b("clear", false)) w(ln).add("clear")
        a.s("take")?.let { w(ln).add("take $it") }
        pyList(a["delattr"]).forEach { w(ln).add("del ${pyStr(it)}") }
        val variant = a["variant"]
        if (variant != null) w(lineOf(variant) ?: ln).add("variant ${pyStr(variant)}")
        val props = (a["properties"] as? Map<*, *>) ?: (a["properties"] as? PyObj)?.entries ?: emptyMap<Any?, Any?>()
        for ((k, v) in props) w(lineOf(v) ?: ln).add("${pyStr(k)} ${pyStr(v)}")
        val keywords = kw.entries.map { Pair(it.key, it.value.join()) }
        indent(); write("style ${a.s("style_name")}")
        if (keywords.isNotEmpty() && keywords[0].second.isNotEmpty()) write(" ${keywords[0].second}")
        if (keywords.size > 1) {
            write(":")
            withIndent(1) {
                for (k in keywords.drop(1)) { advanceToLine(k.first); indent(); write(k.second) }
            }
        }
    }

    // ---------- translate ----------

    private fun lang(a: PyObj) = a.s("language") ?: "None"

    private fun printTranslate(a: PyObj) {
        indent(); write("translate ${lang(a)} ${a.s("identifier")}:")
        printNodes(pyList(a["block"]), 1)
    }

    private fun printTranslateString(a: PyObj) {
        requireInit()
        val prev = block.getOrNull(index - 1) as? PyObj
        if (!(index > 0 && prev != null && prev.shortName == "TranslateString" && prev.s("language") == a.s("language"))) {
            indent(); write("translate ${lang(a)} strings:")
        }
        withIndent(1) {
            advanceToLine(a.i("linenumber"))
            indent(); write("old \"${stringEscape(a.s("old") ?: "")}\"")
            (a["newloc"] as? List<*>)?.getOrNull(1)?.let { advanceToLine(it as? Long) }
            indent(); write("new \"${stringEscape(a.s("new") ?: "")}\"")
        }
    }

    private fun printTranslateBlock(a: PyObj) {
        indent(); write("translate ${lang(a)} ")
        skipIndent = true
        val was = inInit
        val blk = pyList(a["block"])
        val t0 = (blk.firstOrNull() as? PyObj)?.shortName
        if (blk.size == 1 && (t0 == "Python" || t0 == "Style")) inInit = true
        try { printNodes(blk) } finally { inInit = was }
    }

    private fun printTranslateSay(a: PyObj) {
        val language = a.s("language")
        if (!language.isNullOrEmpty()) {
            indent(); write("translate $language ${a.s("identifier")}:")
            withIndent(1) { printSay(a, true) }
        } else printSay(a, false)
    }

    private fun printScreen(a: PyObj) {
        requireInit(); indent()
        val name = (a["screen"] as? PyObj)?.s("name") ?: ""
        write("# [FuruArc] screen $name tidak didekompilasi (screen language)")
    }

    private fun printRpy(a: PyObj) {
        val rest = a["rest"] as? List<*> ?: return
        val command = pyStr(rest.getOrNull(0)) ?: return
        rpyArgs.add(pyStr(rest.getOrNull(1)) ?: "")
        val nx = block.getOrNull(index + 1) as? PyObj
        if (nx != null && nx.shortName == "RPY" && a.i("linenumber") == nx.i("linenumber") &&
            command == pyStr((nx["rest"] as? List<*>)?.getOrNull(0))
        ) return
        indent(); write("rpy $command ${rpyArgs.joinToString(", ")}")
    }

    // ---------- ATL ----------

    private fun locLine(a: PyObj): Long? = (a["loc"] as? List<*>)?.getOrNull(1) as? Long

    private fun locEmpty(a: PyObj): Boolean {
        val l = a["loc"] as? List<*> ?: return false
        return pyStr(l.getOrNull(0)) == "" && (l.getOrNull(1) as? Long) == 0L
    }

    private fun printAtl(block: Any?) {
        val b = block as? PyObj ?: return
        printAtlBlock(b)
    }

    private fun advanceToBlock(b: PyObj) {
        if (!locEmpty(b)) locLine(b)?.let { advanceToLine(it - 1) }
    }

    private fun printAtlBlock(b: PyObj) {
        withIndent(1) {
            if (b.b("animation", false)) { indent(); write("animation") }
            val stmts = pyList(b["statements"])
            if (stmts.isNotEmpty()) printAtlNodes(stmts)
            else if (!locEmpty(b)) { indent(); write("pass") }
        }
    }

    private fun printAtlNodes(stmts: List<Any?>) {
        for ((i, n) in stmts.withIndex()) {
            val a = n as? PyObj ?: continue
            if (a.has("loc")) {
                if (a.shortName == "RawBlock") advanceToBlock(a) else locLine(a)?.let { advanceToLine(it) }
            }
            val nextIs = { t: String -> (stmts.getOrNull(i + 1) as? PyObj)?.shortName == t }
            when (a.shortName) {
                "RawMultipurpose" -> printAtlMulti(a)
                "RawBlock" -> { indent(); write("block:"); printAtlBlock(a) }
                "RawChild" -> for (c in pyList(a["children"])) {
                    (c as? PyObj)?.let { advanceToBlock(it); indent(); write("contains:"); printAtlBlock(it) }
                }
                "RawChoice" -> {
                    for (ch in pyList(a["choices"])) {
                        val l = ch as? List<*> ?: continue
                        val blk = l.getOrNull(1) as? PyObj ?: continue
                        advanceToBlock(blk)
                        indent(); write("choice")
                        val chance = pyStr(l.getOrNull(0))
                        if (chance != null && chance != "1.0") write(" $chance")
                        write(":"); printAtlBlock(blk)
                    }
                    if (nextIs("RawChoice")) { indent(); write("pass") }
                }
                "RawContainsExpr" -> { indent(); write("contains ${a.s("expression")}") }
                "RawEvent" -> { indent(); write("event ${a.s("name")}") }
                "RawFunction" -> { indent(); write("function ${a.s("expr")}") }
                "RawOn" -> {
                    val hs = ((a["handlers"] as? Map<*, *>) ?: (a["handlers"] as? PyObj)?.entries ?: emptyMap<Any?, Any?>())
                        .entries.filter { it.value is PyObj }.sortedBy { locLine(it.value as PyObj) ?: 0L }
                    for ((k, v) in hs) {
                        val blk = v as PyObj
                        advanceToBlock(blk); indent(); write("on ${pyStr(k)}:"); printAtlBlock(blk)
                    }
                }
                "RawParallel" -> {
                    for (bl in pyList(a["blocks"])) {
                        (bl as? PyObj)?.let { advanceToBlock(it); indent(); write("parallel:"); printAtlBlock(it) }
                    }
                    if (nextIs("RawParallel")) { indent(); write("pass") }
                }
                "RawRepeat" -> {
                    indent(); write("repeat")
                    val r = a["repeats"]
                    if (r != null && r != false && r != "") write(" ${pyStr(r) ?: r}")
                }
                "RawTime" -> { indent(); write("time ${a.s("time")}") }
                else -> failure("Unknown ATL node: ${a.cls}")
            }
        }
    }

    private fun printAtlMulti(a: PyObj) {
        val warpWords = Words(false)
        val wf = a.s("warp_function")
        val warper = a.s("warper")
        val duration = a.s("duration")
        if (!wf.isNullOrEmpty()) warpWords.add("warp", wf, duration)
        else if (!warper.isNullOrEmpty()) warpWords.add(warper, duration)
        else if (duration != null && duration != "0") warpWords.add("pause", duration)
        val warp = warpWords.join()
        val words = Words(warp.isNotEmpty() && warp.last() != ' ', true)
        a.s("revolution")?.let { words.add(it) }
        val circles = a.s("circles")
        if (circles != null && circles != "0") words.add("circles $circles")
        val sp = Words(false)
        for (s in pyList(a["splines"])) {
            val l = s as? List<*> ?: continue
            val exprs = pyList(l.getOrNull(1))
            sp.add(pyStr(l.getOrNull(0)), pyStr(exprs.lastOrNull()))
            for (e in exprs.dropLast(1)) sp.add("knot", pyStr(e))
        }
        words.add(sp.join())
        val pw = Words(false)
        for (p in pyList(a["properties"])) {
            val l = p as? List<*> ?: continue
            pw.add(pyStr(l.getOrNull(0)), pyStr(l.getOrNull(1)))
        }
        words.add(pw.join())
        val ex = Words(false)
        val exprs = pyList(a["expressions"])
        val needsPass = exprs.size > 1
        for (e in exprs) {
            val l = e as? List<*> ?: continue
            ex.add(pyStr(l.getOrNull(0)))
            pyStr(l.getOrNull(1))?.takeIf { it.isNotEmpty() }?.let { ex.add("with", it) }
            if (needsPass) ex.add("pass")
        }
        words.add(ex.join())
        val toWrite = warp + words.join()
        if (toWrite.isNotEmpty()) { indent(); write(toWrite) } else write(",")
    }
}
