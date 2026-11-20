package com.furuarc.core

/** Result of checking the parts to be merged. [order] = input indices by part number. */
class JoinCheck(
    val code: Int,
    val arg: String,
    val order: List<Int>,
    val base: String,
    val total: Long,
    val startWarn: Boolean,
) {
    val ok get() = code == OK

    companion object {
        const val OK = 0
        const val EMPTY = 1
        const val NOT_PART = 2
        const val BASE_DIFF = 3
        const val DUP = 4
        const val MISSING = 5
        const val ONE = 6
    }
}

/** Bulk Rename settings. [mode]: 0 find & replace, 1 number pattern, 2 prefix/suffix. [caseMode]: 0 original, 1 lowercase, 2 uppercase. */
class RenameOpts(
    val mode: Int,
    val find: String,
    val replace: String,
    val ignoreCase: Boolean,
    val pattern: String,
    val start: Int,
    val digits: Int,
    val prefix: String,
    val suffix: String,
    val caseMode: Int,
    /** Mode 3 (fix broken names): the wrong encoding used to read the name, and the encoding it should have been. */
    val wrongCs: String = "windows-1252",
    val rightCs: String = "Shift_JIS",
)

class Checksums(val whole: String?, val parts: Map<String, String>)

class RenameRow(val old: String, val new: String, val status: Int) {
    companion object {
        const val SAME = 0
        const val CHANGE = 1
        const val CONFLICT = 2
        const val INVALID = 3
    }
}

class RenamePlan(val rows: List<RenameRow>) {
    fun count(status: Int) = rows.count { it.status == status }
    val bad get() = count(RenameRow.CONFLICT) + count(RenameRow.INVALID)
}

/** Pure logic of the File Tools: File Splitter, File Merger, and Bulk Rename. */
object FileOps {
    const val MAX_PARTS = 9999
    const val BUF = 1 shl 20

    // ======================= Splitter / Merger =======================

    /** Name of part number [index] (starting at 1): "name.001". Number width is at least 3 digits. */
    fun partName(base: String, index: Int, total: Int): String {
        val w = maxOf(3, total.toString().length)
        return base + "." + index.toString().padStart(w, '0')
    }

    /** Size of each part when a file of length [len] is split into [parts] parts (rounded up). */
    fun sizeForParts(len: Long, parts: Int): Long =
        if (parts <= 0 || len <= 0L) 0L else (len + parts - 1) / parts

    /** Number of parts when a file of length [len] is cut every [partSize] bytes. */
    fun partCount(len: Long, partSize: Long): Int {
        if (len <= 0L || partSize <= 0L) return 0
        val n = (len + partSize - 1) / partSize
        return if (n > Int.MAX_VALUE) Int.MAX_VALUE else n.toInt()
    }

    private val PART = Regex("^(.+)\\.(\\d{1,4})$")

    /** "film.mkv.003" -> ("film.mkv", 3); null if it is not a numbered part. */
    fun parsePartName(name: String): Pair<String, Int>? {
        val m = PART.matchEntire(name) ?: return null
        return Pair(m.groupValues[1], m.groupValues[2].toInt())
    }

    /** Check the list of parts: must be numbered, share one base name, have no duplicate numbers, and be sequential with no gaps. */
    fun checkJoin(names: List<String>, sizes: List<Long>): JoinCheck {
        if (names.isEmpty()) return JoinCheck(JoinCheck.EMPTY, "", emptyList(), "", 0L, false)
        val parsed = ArrayList<Pair<String, Int>>()
        for (n in names) {
            val p = parsePartName(n) ?: return JoinCheck(JoinCheck.NOT_PART, n, emptyList(), "", 0L, false)
            parsed.add(p)
        }
        val base = parsed[0].first
        if (names.size < 2) return JoinCheck(JoinCheck.ONE, "", emptyList(), base, 0L, false)
        for (i in parsed.indices) {
            if (!parsed[i].first.equals(base, ignoreCase = true)) {
                return JoinCheck(JoinCheck.BASE_DIFF, names[i], emptyList(), base, 0L, false)
            }
        }
        val order = names.indices.sortedBy { parsed[it].second }
        for (k in 1 until order.size) {
            if (parsed[order[k]].second == parsed[order[k - 1]].second) {
                return JoinCheck(JoinCheck.DUP, parsed[order[k]].second.toString(), emptyList(), base, 0L, false)
            }
        }
        val first = parsed[order[0]].second
        val last = parsed[order[order.size - 1]].second
        if (last - first + 1 != order.size) {
            val have = HashSet<Int>()
            for (i in order) have.add(parsed[i].second)
            val missing = ArrayList<Int>()
            for (x in first..last) if (x !in have) missing.add(x)
            val txt = missing.take(5).joinToString(", ") + (if (missing.size > 5) ", …" else "")
            return JoinCheck(JoinCheck.MISSING, txt, emptyList(), base, 0L, false)
        }
        var total = 0L
        for (i in order) total += maxOf(0L, sizes[i])
        return JoinCheck(JoinCheck.OK, "", order, base, total, first > 1)
    }

    // ======================= Bulk Rename =======================

    /** ("foto.png") -> ("foto", ".png"); files without an extension or starting with a dot are not split. */
    fun splitExt(name: String): Pair<String, String> {
        val dot = name.lastIndexOf('.')
        return if (dot > 0) Pair(name.substring(0, dot), name.substring(dot)) else Pair(name, "")
    }

    /** [exts] = list of extensions separated by comma/space (without the dot); empty = all files. */
    fun matchesExt(name: String, exts: String): Boolean {
        val list = exts.lowercase().split(',', ' ', ';').map { it.trim().trimStart('.') }.filter { it.isNotEmpty() }
        if (list.isEmpty()) return true
        val ext = splitExt(name).second.trimStart('.').lowercase()
        return ext in list
    }

    /** Natural order: "file2" before "file10", case-insensitive. */
    fun naturalCompare(a: String, b: String): Int {
        var i = 0
        var j = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]
            if (ca.isDigit() && cb.isDigit()) {
                var ei = i
                while (ei < a.length && a[ei].isDigit()) ei++
                var ej = j
                while (ej < b.length && b[ej].isDigit()) ej++
                val da = a.substring(i, ei).trimStart('0')
                val db = b.substring(j, ej).trimStart('0')
                if (da.length != db.length) return da.length - db.length
                val c = da.compareTo(db)
                if (c != 0) return c
                i = ei
                j = ej
            } else {
                val c = ca.lowercaseChar().compareTo(cb.lowercaseChar())
                if (c != 0) return c
                i++
                j++
            }
        }
        return (a.length - i) - (b.length - j)
    }

    /** New name for [old] at position [idx] (starting at 0). The extension is always preserved. */
    fun newName(old: String, idx: Int, o: RenameOpts): String {
        if (o.mode == 3) {
            val (fb, fe) = splitExt(fixMojibake(old, o.wrongCs, o.rightCs))
            val cb = when (o.caseMode) { 1 -> fb.lowercase(); 2 -> fb.uppercase(); else -> fb }
            return cb + fe
        }
        val (base, ext) = splitExt(old)
        var nb = when (o.mode) {
            0 -> if (o.find.isEmpty()) base else base.replace(o.find, o.replace, o.ignoreCase)
            1 -> {
                val pat = if (o.pattern.isEmpty()) "{nama}" else o.pattern
                val num = (o.start + idx).toString().padStart(o.digits, '0')
                pat.replace("{nama}", base).replace("{no}", num)
            }
            else -> o.prefix + base + o.suffix
        }
        nb = when (o.caseMode) {
            1 -> nb.lowercase()
            2 -> nb.uppercase()
            else -> nb
        }
        return nb + ext
    }

    fun validName(n: String): Boolean =
        n.isNotBlank() && n != "." && n != ".." && n.none { it == '/' || it == '\u0000' }

    /**
     * Rename plan. [names] = files to be processed (already sorted), [existing] = all names in the folder
     * (including subfolders and files not being processed). A conflict occurs when the new name equals another renamed
     * name or a name that stays in the folder (case-insensitive, for FAT folders).
     */
    fun planRename(names: List<String>, existing: Collection<String>, o: RenameOpts): RenamePlan {
        val finals = ArrayList<String>()
        for (i in names.indices) finals.add(newName(names[i], i, o))
        val processed = names.toHashSet()
        val count = HashMap<String, Int>()
        for (f in finals) {
            val k = f.lowercase()
            count[k] = (count[k] ?: 0) + 1
        }
        for (e in existing) {
            if (e in processed) continue
            val k = e.lowercase()
            count[k] = (count[k] ?: 0) + 1
        }
        val rows = ArrayList<RenameRow>()
        for (i in names.indices) {
            val old = names[i]
            val nw = finals[i]
            val status = when {
                nw == old -> RenameRow.SAME
                !validName(nw) -> RenameRow.INVALID
                (count[nw.lowercase()] ?: 0) > 1 -> RenameRow.CONFLICT
                else -> RenameRow.CHANGE
            }
            rows.add(RenameRow(old, nw, status))
        }
        return RenamePlan(rows)
    }

    // ======================= checksum (.sha256) =======================

    fun hex(b: ByteArray): String {
        val d = "0123456789abcdef"
        val sb = StringBuilder(b.size * 2)
        for (x in b) {
            val v = x.toInt() and 0xFF
            sb.append(d[v shr 4]).append(d[v and 15])
        }
        return sb.toString()
    }

    fun isChecksumName(name: String) = name.endsWith(".sha256", ignoreCase = true)

    /** "film.mkv.sha256" -> "film.mkv". */
    fun checksumBase(name: String) = name.dropLast(7)

    /** Checksum file contents: three comment lines, then one "hash  name" line per part (compatible with sha256sum -c). */
    fun checksumText(name: String, size: Long, whole: String, partNames: List<String>, partHashes: List<String>): String {
        val sb = StringBuilder()
        sb.append("# FuruArc checksum (SHA-256)\n")
        sb.append("# file: ").append(name).append('\n')
        sb.append("# size: ").append(size).append('\n')
        sb.append("# sha256: ").append(whole).append('\n')
        for (i in partNames.indices) sb.append(partHashes[i]).append("  ").append(partNames[i]).append('\n')
        return sb.toString()
    }

    private val SUM_LINE = Regex("^([0-9a-fA-F]{64})\\s+\\*?(.+)$")

    fun parseChecksum(text: String): Checksums {
        var whole: String? = null
        val parts = HashMap<String, String>()
        for (raw in text.split('\n')) {
            val line = raw.trim()
            if (line.startsWith("# sha256:")) {
                whole = line.removePrefix("# sha256:").trim().lowercase()
                continue
            }
            val m = SUM_LINE.matchEntire(line) ?: continue
            parts[m.groupValues[2].trim().lowercase()] = m.groupValues[1].lowercase()
        }
        return Checksums(whole, parts)
    }

    // ======================= broken file names (mojibake) =======================

    /**
     * Fix wrongly decoded names: the name is converted back to its original bytes using encoding [wrong],
     * then read with encoding [right]. Names that do not match (already correct, pure ASCII, or the result
     * is invalid) are returned unchanged.
     */
    fun fixMojibake(name: String, wrong: String, right: String): String {
        return try {
            val w = java.nio.charset.Charset.forName(wrong)
            val r = java.nio.charset.Charset.forName(right)
            if (name.all { it.code < 128 }) return name
            if (!w.newEncoder().canEncode(name)) return name
            val bytes = name.toByteArray(w)
            val dec = r.newDecoder()
                .onMalformedInput(java.nio.charset.CodingErrorAction.REPORT)
                .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPORT)
            dec.decode(java.nio.ByteBuffer.wrap(bytes)).toString()
        } catch (e: Exception) {
            name
        }
    }
}
