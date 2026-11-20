package com.furuarc.core

import java.io.IOException

/**
 * Text inside a SiglusEngine scene .dat (unpacked from Scene.pck).
 * 132-byte header (33 int32). String list = pairs (offset, length) in UTF-16 units + a UTF-16LE blob;
 * each string is XORed with a 16-bit key (0x7087 * string index) & 0xFFFF.
 * Exported: strings that contain non-ASCII letters (dialog, names, choices, UI text). ASCII strings (labels, file names,
 * commands) are not exported so the script is not broken. Unit key: `S<index>:<hash>`; import skips entries whose original text has changed.
 */
object SiglusScene {
    const val HDR = 132
    private const val MUL = 0x7087

    private val OFS_FIELDS = intArrayOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31) // *_ofs in the header

    /** (index of the *_ofs field in the header, size of one item) */
    private val SECTIONS = listOf(7 to 4, 9 to 4, 11 to 8, 13 to 8, 15 to 8, 19 to 4, 21 to 8, 25 to 8, 29 to 4, 31 to 4)

    class Parsed(val b: ByteArray, val strs: List<String>, val idx: List<Pair<Int, Int>>, val blobOfs: Int, val blobEnd: Int)

    private fun i32(b: ByteArray, p: Int) = b.le().getInt(p)

    fun isDat(b: ByteArray): Boolean = try { parseOrNull(b) != null } catch (e: Exception) { false }

    /** Quick check from the file header only (for auto-detection; [head] may be truncated). */
    fun isDatHead(h: ByteArray): Boolean {
        if (h.size < HDR || i32(h, 0) != HDR) return false
        val idxOfs = i32(h, 12); val cnt = i32(h, 16); val blobOfs = i32(h, 20); val sc = i32(h, 24)
        if (cnt != sc || cnt < 0 || cnt > 5_000_000 || idxOfs < HDR) return false
        return blobOfs.toLong() >= idxOfs.toLong() + cnt * 8L || (cnt == 0)
    }

    private fun key(i: Int) = (MUL * i) and 0xFFFF

    fun parseOrNull(b: ByteArray): Parsed? {
        if (b.size < HDR) return null
        if (i32(b, 0) < HDR || i32(b, 0) > b.size) return null
        val scnOfs = i32(b, 4)
        val scnSize = i32(b, 8)
        if (scnOfs < 0 || scnSize < 0 || scnOfs > b.size || scnOfs.toLong() + scnSize > b.size) return null
        val idxOfs = i32(b, 12)
        val idxCnt = i32(b, 16)
        val blobOfs = i32(b, 20)
        val strCnt = i32(b, 24)
        if (idxCnt < 0 || idxCnt != strCnt || idxOfs < 0 || (idxCnt > 0 && idxOfs < HDR) || idxOfs.toLong() + idxCnt.toLong() * 8 > b.size) return null
        if (blobOfs < 0 || (idxCnt > 0 && blobOfs < HDR) || blobOfs > b.size) return null
        // other sections must lie inside the file
        for ((ofsField, itemSize) in SECTIONS) {
            val o = i32(b, ofsField * 4)
            val c = i32(b, (ofsField + 1) * 4)
            if (o < 0 || c < 0 || o.toLong() + c.toLong() * itemSize > b.size || (c > 0 && o < HDR)) return null
        }
        val bb = b.le()
        val idx = ArrayList<Pair<Int, Int>>(idxCnt)
        var end = 0L
        for (i in 0 until idxCnt) {
            val o = bb.getInt(idxOfs + i * 8)
            val l = bb.getInt(idxOfs + i * 8 + 4)
            if (o < 0 || l < 0) return null
            idx.add(Pair(o, l))
            if (o.toLong() + l > end) end = o.toLong() + l
        }
        val blobEnd = blobOfs.toLong() + end * 2
        if (blobEnd > b.size) return null
        val strs = ArrayList<String>(idxCnt)
        for (i in 0 until idxCnt) {
            val (o, l) = idx[i]
            val a = blobOfs + o * 2
            val k = key(i)
            val chars = CharArray(l)
            for (j in 0 until l) {
                val w = ((b[a + j * 2].toInt() and 0xff) or ((b[a + j * 2 + 1].toInt() and 0xff) shl 8)) xor k
                chars[j] = (w and 0xFFFF).toChar()
            }
            strs.add(String(chars))
        }
        return Parsed(b, strs, idx, blobOfs, blobEnd.toInt())
    }

    private val ID_LIKE = Regex("^[A-Za-z0-9_$.\\-/]+$")

    /**
     * Strings that are likely text (dialog, speaker name, choices). Internal resource names/labels
     * (bg_white, cg01_a, $$debug...) are dropped. Games already translated to a Latin-script language (e.g. Indonesian) are still readable
     * because they are recognized by spaces/punctuation or names starting with a capital letter. This is a heuristic, not bytecode analysis.
     */
    fun translatable(s: String): Boolean {
        if (s.isBlank()) return false
        if (s.any { it.code >= 0x80 }) return true
        if (s.any { it == ' ' || it == '\n' || it == '\r' || it == '\t' }) return true
        if (ID_LIKE.matches(s)) return s.length <= 24 && s[0].isUpperCase() && s.all { it.isLetter() }
        return true
    }

    private fun unit(file: String, i: Int, s: String) = TextUnit(file, "S$i:${ScriptText.hash(s)}", s.replace("\r\n", "\n"))

    fun extract(fileKey: String, b: ByteArray): List<TextUnit> {
        val p = parseOrNull(b) ?: throw IOException("Bukan scene .dat Siglus")
        val r = ArrayList<TextUnit>()
        val seen = HashSet<String>()
        // identical text (e.g. copies for log/backlog) is shown once; its translation applies to all copies
        for ((i, s) in p.strs.withIndex()) if (translatable(s) && seen.add(s)) r.add(unit(fileKey, i, s))
        return r
    }

    /** Number of translatable strings (for checking). */
    fun stats(b: ByteArray): Pair<Int, Int>? {
        val p = parseOrNull(b) ?: return null
        return Pair(p.strs.size, p.strs.filter { translatable(it) }.toSet().size)
    }

    /** Apply [tr]; returns (new file, number of strings replaced). Other parts of the file are unchanged, only the string blob is rebuilt. */
    fun apply(b: ByteArray, tr: Map<String, String>): Pair<ByteArray, Int> {
        val p = parseOrNull(b) ?: throw IOException("Bukan scene .dat Siglus")
        val newStrs = ArrayList<String>(p.strs)
        var changed = 0
        val byHash = HashMap<String, String>()
        for ((k, v) in tr) { val c = k.indexOf(':'); if (k.startsWith("S") && c > 0) byHash.putIfAbsent(k.substring(c + 1), v) }
        for ((i, s) in p.strs.withIndex()) {
            val h = ScriptText.hash(s)
            val t = tr["S$i:$h"] ?: byHash[h] ?: continue
            val nt = if (s.contains("\r\n")) t.replace("\r\n", "\n").replace("\n", "\r\n") else t.replace("\r\n", "\n")
            if (nt == s) continue
            newStrs[i] = nt
            changed++
        }
        if (changed == 0) return Pair(b, 0)
        val idxOfs = i32(b, 12)
        val oldBlobBytes = p.blobEnd - p.blobOfs
        // new blob in order of string index
        val blob = ByteArray(newStrs.sumOf { it.length } * 2)
        val newIdx = ArrayList<Pair<Int, Int>>(newStrs.size)
        var pos = 0
        for ((i, s) in newStrs.withIndex()) {
            newIdx.add(Pair(pos / 2, s.length))
            val k = key(i)
            for (ch in s) {
                val w = ch.code xor k
                blob[pos++] = w.toByte()
                blob[pos++] = (w ushr 8).toByte()
            }
        }
        val delta = blob.size - oldBlobBytes
        val out = ByteArray(b.size + delta)
        System.arraycopy(b, 0, out, 0, p.blobOfs)
        System.arraycopy(blob, 0, out, p.blobOfs, blob.size)
        System.arraycopy(b, p.blobEnd, out, p.blobOfs + blob.size, b.size - p.blobEnd)
        val bo = out.le()
        // index table (may be located before or after the blob)
        for ((i, pr) in newIdx.withIndex()) {
            val at = if (idxOfs >= p.blobEnd) idxOfs + delta else idxOfs
            bo.putInt(at + i * 8, pr.first)
            bo.putInt(at + i * 8 + 4, pr.second)
        }
        // shift all sections located after the old blob
        for (f in OFS_FIELDS) {
            val o = bo.getInt(f * 4)
            if (o >= p.blobEnd) bo.putInt(f * 4, o + delta)
        }
        return Pair(out, changed)
    }
}
