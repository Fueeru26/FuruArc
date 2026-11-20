package com.furuarc.core

import java.io.IOException

/**
 * Kirikiri .scn script (PSB, sometimes wrapped in mdf). Structure used:
 *   scenes[].texts[] = [name, [language variants...], voice]; variant 0 = [name/null, text, character count, (text without \n, text without \n)]
 *   scenes[].selects[] = {text, language:[{text}...], ...}
 * Newlines inside text are a literal "\n"; the editor shows them as regular line breaks.
 * Import: if the file already has extra language slots (result of a multi-language patch), the translation is written to the last slot;
 * otherwise the original text (slot 0) is overwritten and the character count and the copies without \n are updated too.
 */
object KrScn {
    fun isScn(d: ByteArray): Boolean = Psb.isPsb(d) || Psb.isMdf(d)

    private fun load(d: ByteArray): Triple<Psb.Doc, Boolean, Map<String, Int>> {
        val mdf = Psb.isMdf(d)
        val raw = if (mdf) Psb.unmdf(d) else d
        val doc = Psb.parse(raw)
        val idx = HashMap<String, Int>(); doc.names.forEachIndexed { i, s -> idx.putIfAbsent(s, i) }
        return Triple(doc, mdf, idx)
    }

    private fun Psb.D.get(names: Map<String, Int>, key: String): Psb.N? {
        val k = names[key] ?: return null
        val i = keys.indexOf(k)
        return if (i < 0) null else vals[i]
    }

    private fun Psb.D.set(names: Map<String, Int>, key: String, v: Psb.N): Boolean {
        val k = names[key] ?: return false
        val i = keys.indexOf(k)
        if (i < 0) return false
        vals[i] = v; return true
    }

    /** Quick detection from the start of the file (PSB v3/4 without mdf; mdf accepted as is). */
    fun isScript(head: ByteArray): Boolean = Psb.isMdf(head) || (Psb.isPsb(head) && head[4].toInt() >= 3)

    private fun disp(s: String) = s.replace("\r\n", "\n").replace("\\n", "\n")
    private fun raw(s: String) = s.replace("\r\n", "\n").replace("\r", "").replace("\n", "\\n")
    private val RUBY = Regex("\\[[^\\]]*\\]")
    private fun visibleLen(s: String) = s.replace("\\n", "").replace(RUBY, "").length

    private class Hit(val kind: Char, val text: String, val speaker: String? = null, val set: (String) -> Unit)

    private fun hits(doc: Psb.Doc, names: Map<String, Int>): List<Hit> {
        val out = ArrayList<Hit>()
        val root = doc.root as? Psb.D ?: throw IOException("Bukan skrip scn")
        val scenes = root.get(names, "scenes") as? Psb.L ?: throw IOException("Bukan skrip scn")
        for (sc in scenes.items) {
            sc as? Psb.D ?: continue
            val texts = sc.get(names, "texts") as? Psb.L
            if (texts != null) for (t in texts.items) {
                t as? Psb.L ?: continue
                if (t.items.size < 2) continue
                val vars = t.items[1] as? Psb.L ?: continue
                val v0 = vars.items.firstOrNull() as? Psb.L ?: continue
                val patched = vars.items.size >= 2 && vars.items.last() is Psb.L
                val nameNode = t.items[0]
                if (nameNode is Psb.S && nameNode.v.isNotEmpty()) {
                    out.add(Hit('N', nameNode.v) { nt ->
                        if (patched) { val vl = vars.items.last() as Psb.L; if (vl.items.isNotEmpty()) vl.items[0] = Psb.S(nt) }
                        else t.items[0] = Psb.S(nt)
                    })
                }
                val tn = v0.items.getOrNull(1) as? Psb.S ?: continue
                if (tn.v.isEmpty()) continue
                out.add(Hit('M', disp(tn.v), (nameNode as? Psb.S)?.v?.takeIf { it.isNotEmpty() }) { nt ->
                    val r = raw(nt)
                    if (patched) {
                        val vl = vars.items.last() as Psb.L
                        while (vl.items.size < 2) vl.items.add(Psb.S(""))
                        vl.items[1] = Psb.S(r)
                    } else {
                        v0.items[1] = Psb.S(r)
                        if (v0.items.size > 2 && v0.items[2] is Psb.I) v0.items[2] = Psb.I(visibleLen(r).toLong())
                        val plain = r.replace("\\n", "")
                        for (k in 3 until v0.items.size) if (v0.items[k] is Psb.S) v0.items[k] = Psb.S(plain)
                    }
                })
            }
            val sels = sc.get(names, "selects") as? Psb.L
            if (sels != null) for (s in sels.items) {
                s as? Psb.D ?: continue
                val tn = s.get(names, "text") as? Psb.S ?: continue
                if (tn.v.isEmpty()) continue
                out.add(Hit('S', disp(tn.v)) { nt ->
                    val r = raw(nt)
                    val lang = s.get(names, "language") as? Psb.L
                    val last = lang?.items?.takeIf { it.size >= 2 }?.last() as? Psb.D
                    if (last != null) last.set(names, "text", Psb.S(r)) else s.set(names, "text", Psb.S(r))
                    if (last == null) { val f = lang?.items?.firstOrNull() as? Psb.D; f?.set(names, "text", Psb.S(r)) }
                })
            }
        }
        return out
    }

    /** [full] = all lines in order (without merging identical ones, name becomes the speaker caption) for VNTP export. */
    fun extract(fileKey: String, d: ByteArray, full: Boolean = false): List<TextUnit> {
        val (doc, _, names) = load(d)
        val out = ArrayList<TextUnit>(); val seen = HashSet<String>()
        for ((i, h) in hits(doc, names).withIndex()) {
            if (full) { if (h.kind != 'N') out.add(TextUnit(fileKey, "${h.kind}$i:" + ScriptText.hash(h.text), h.text, h.speaker)) }
            else if (seen.add(h.kind + h.text)) out.add(TextUnit(fileKey, "${h.kind}$i:" + ScriptText.hash(h.text), h.text))
        }
        if (out.isEmpty()) throw IOException("Tidak ada teks")
        return out
    }

    fun apply(d: ByteArray, tr: Map<String, String>): Pair<ByteArray, Int> {
        val (doc, mdf, names) = load(d)
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (c > 1) byHash.putIfAbsent(k[0] + k.substring(c + 1), v) }
        var changed = 0
        for ((i, h) in hits(doc, names).withIndex()) {
            val hs = ScriptText.hash(h.text)
            val nt = tr["${h.kind}$i:$hs"] ?: byHash[h.kind + hs] ?: continue
            if (nt == h.text) continue
            h.set(nt); changed++
        }
        if (changed == 0) return Pair(d, 0)
        val out = Psb.write(doc)
        return Pair(if (mdf) Psb.mdf(out) else out, changed)
    }
}
