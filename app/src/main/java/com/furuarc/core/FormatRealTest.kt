package com.furuarc.core

import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.random.Random

/** Random access to a plain file (the pack result and, in unit tests, the archive under test). */
class FileSource(f: File) : RandomSource {
    private val stream = FileInputStream(f)
    private val ch = stream.channel
    override val length: Long = ch.size()
    override fun readAt(pos: Long, buf: ByteArray, off: Int, len: Int): Int = ch.read(ByteBuffer.wrap(buf, off, len), pos)
    override fun close() = stream.close()
}

/** What one line of the real-archive Format Test reports. */
enum class RtKind {
    FULL_FORCED,    // this format can only be packed with all its files: every file is tested and compared
    SEED,           // the random seed used: n1 = seed
    PICKED,         // the files used: arg = names, n1 = total bytes, n2 = number of files
    NO_FILE_FITS,   // no file is small enough: n1 = limit (bytes), n2 = file count
    NAME_NOT_FOUND, // the requested file name is not in the archive: arg = name
    TOO_BIG,        // too much data for the chosen mode: n1 = bytes, n2 = limit
    UNPACKED,       // the files were extracted: n1 = bytes, n2 = ms
    PACKED,         // the files were packed again: n1 = archive bytes, n2 = ms
    CONTENT_OK,     // the new archive gives the same files back (SHA-256 equal): arg = hash prefix, n1 = files
    CONTENT_BAD,    // different data after unpacking the new archive
    COUNT,          // the new archive does not hold the expected number of files: n1 = found, n2 = expected
    NAME,           // another file name: arg = name found
    HEADER,         // header compared with the original: n1 = equal leading bytes, n2 = bytes compared
    NOT_DETECTED,   // the new archive is not recognised as this format
    DETECTED_BY,    // formats that accept the new archive: arg = names
    SIZES,          // sizes: n1 = original, n2 = new
    STRUCT_SAME,    // the new archive is identical to the original, byte for byte
    STRUCT_DATA,    // first difference is inside the data of a file: arg = name, n1 = offset, n2 = different bytes
    STRUCT_INDEX,   // first difference is in the header / index area: n1 = offset, n2 = different bytes
    ERROR_UNPACK,   // extracting failed: arg = message
    ERROR_PACK,     // packing failed: arg = message
    ERROR_READ,     // reading the new archive failed: arg = message
    CANCELLED,
}

class RtLine(val level: FtLevel, val kind: RtKind, val arg: String = "", val n1: Long = 0, val n2: Long = 0)

/** Result of [FormatRealTest.check]: [entries] > 0 = the file is an archive of the chosen format. */
class RtCheck(val entries: Int, val foundName: String?, val error: String?, val detail: String? = null)

/**
 * Which files the test uses. [mode]: [ONE] one random file (or the file named [pickName]), [N] [count] random files,
 * [ALL] every file up to [maxBytes], [FULL] every file, then the new archive is compared byte for byte with the original.
 * The same [seed] picks the same files again.
 */
class RtPlan(val mode: Int, val count: Int, val pickName: String, val seed: Long, val maxBytes: Long) {
    companion object {
        const val ONE = 0
        const val N = 1
        const val ALL = 2
        const val FULL = 3
    }
}

/**
 * Format Test with a real archive: the archive is opened with the chosen format, some of its files are extracted and packed
 * again, and the new archive is read back and compared with the original.
 * Pure Kotlin (no Android classes), so it also runs in unit tests.
 */
object FormatRealTest {
    /** Largest total size for [RtPlan.ALL] and for a file picked by name ([RtPlan.FULL] has no limit: the user asked for every file). */
    private const val CAP_ALL = 256L * 1024 * 1024
    private const val CAP_FILES = 5000

    private fun norm(n: String) = n.replace('\\', '/').trimStart('/')

    /** Writes to [out] (when not null) while it computes the SHA-256 and the size. */
    private class HashSink(private val out: OutputStream?) : OutputStream() {
        private val md = MessageDigest.getInstance("SHA-256")
        var size = 0L
            private set
        override fun write(b: Int) { md.update(b.toByte()); out?.write(b); size++ }
        override fun write(b: ByteArray, off: Int, len: Int) { md.update(b, off, len); out?.write(b, off, len); size += len }
        fun hex(): String = md.digest().joinToString("") { "%02x".format(it) }
    }

    /** The extra options used to read an archive: the form values plus the archive's own file name. */
    private fun readExtra(extra: Map<String, String>, name: String) = extra + ("_arc" to name)

    /**
     * Is [src] an archive of [fmt]? Formats without magic bytes cannot always be detected, so the file is accepted when the
     * format can read its file list. [RtCheck.foundName] = the format that was detected instead (for the error message).
     */
    fun check(src: RandomSource, archiveName: String, fmt: ArchiveFormat, unpackExtra: Map<String, String>): RtCheck {
        val n = minOf(16L, src.length).toInt()
        if (n <= 0) return RtCheck(0, null, "empty")
        val head = src.readFully(0, n)
        var detail: String? = null
        val list = try {
            fmt.list(src, Options(extra = readExtra(unpackExtra, archiveName)))
        } catch (e: Exception) {
            detail = e.message
            null
        }
        val other = runCatching { FormatRegistry.detect(src) }.getOrNull()
        if (list == null || list.isEmpty()) {
            return RtCheck(0, other?.takeIf { it.id != fmt.id }?.displayName, if (list == null) "unreadable" else "no files", detail)
        }
        // another format with magic bytes recognises it and this one does not: the wrong file
        val own = runCatching { fmt.matches(head) || fmt.detects(src, head) }.getOrDefault(false)
        if (!own && other != null && other.id != fmt.id) return RtCheck(0, other.displayName, "other format")
        return RtCheck(list.size, null, null)
    }

    /** Names for the "picked" line: the first three, then "…". */
    private fun nameList(l: List<Entry>): String =
        l.take(3).joinToString(", ") { it.name } + if (l.size > 3) ", …" else ""

    /** First difference between two sources: offset (-1 = none) and the number of bytes that differ (a longer file counts its extra bytes). */
    private fun firstDiff(a: RandomSource, b: RandomSource): Pair<Long, Long> {
        val common = minOf(a.length, b.length)
        val ba = ByteArray(1 shl 20)
        val bb = ByteArray(1 shl 20)
        var pos = 0L
        var first = -1L
        var diff = 0L
        while (pos < common) {
            val n = minOf(ba.size.toLong(), common - pos).toInt()
            val ra = a.readAt(pos, ba, 0, n)
            val rb = b.readAt(pos, bb, 0, n)
            val m = minOf(ra, rb)
            if (m <= 0) break
            for (i in 0 until m) if (ba[i] != bb[i]) { if (first < 0) first = pos + i; diff++ }
            pos += m
        }
        val extra = Math.abs(a.length - b.length)
        if (extra > 0 && first < 0) first = common
        return Pair(first, diff + extra)
    }

    /**
     * Runs the test. Lines come back in the order of the steps; the first FAIL ends the test.
     * [keepDir] (when not null) receives the extracted files and the new archive (for "save samples").
     */
    fun run(
        src: RandomSource,
        archiveName: String,
        fmt: ArchiveFormat,
        unpackExtra: Map<String, String>,
        unpackKey: Int?,
        packExtra: Map<String, String>,
        plan: RtPlan,
        workDir: File,
        keepDir: File? = null,
        isCancelled: () -> Boolean = { false },
        onLine: (RtLine) -> Unit = {},
    ): List<RtLine> {
        val out = ArrayList<RtLine>()
        fun add(l: RtLine): RtLine { out.add(l); onLine(l); return l }
        fun fail(kind: RtKind, arg: String = "") = add(RtLine(FtLevel.FAIL, kind, arg))
        fun msg(e: Throwable) = e.message ?: e.javaClass.simpleName
        fun cancelled(): Boolean { if (!isCancelled()) return false; add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return true }

        add(RtLine(FtLevel.PASS, RtKind.SEED, "", plan.seed))
        val mode = if (fmt.packNeedsAllFiles) RtPlan.FULL else plan.mode
        if (mode != plan.mode) add(RtLine(FtLevel.PASS, RtKind.FULL_FORCED))
        // settings with the same name are shared: an index file chosen for unpacking is also used for packing
        val packEx = unpackExtra.filterValues { it.isNotEmpty() } + packExtra.filterValues { it.isNotEmpty() }
        val rnd = Random(plan.seed)
        val rOpts = Options(xorKey = unpackKey, extra = readExtra(unpackExtra, archiveName))
        val entries = try { fmt.list(src, rOpts) } catch (e: Exception) { fail(RtKind.ERROR_UNPACK, msg(e)); return out }

        // ---- which files ----
        val need = fmt.minPackFiles.coerceAtLeast(1)
        val fits = entries.filter { it.size in 1..plan.maxBytes }
        val chosen = ArrayList<Entry>()
        when (mode) {
            RtPlan.FULL -> chosen.addAll(entries)
            RtPlan.ALL -> {
                var total = 0L
                for (e in fits) {
                    if (chosen.size >= CAP_FILES || total + e.size > CAP_ALL) break
                    chosen.add(e); total += e.size
                }
            }
            RtPlan.N -> chosen.addAll(fits.shuffled(rnd).take(plan.count.coerceAtLeast(1)))
            else -> {
                val want = norm(plan.pickName.trim()).lowercase()
                if (want.isNotEmpty()) {
                    val hit = entries.filter { norm(it.name).lowercase() == want }.ifEmpty { entries.filter { norm(it.name).lowercase().contains(want) } }.firstOrNull()
                    if (hit == null) { fail(RtKind.NAME_NOT_FOUND, plan.pickName.trim()); return out }
                    if (hit.size > CAP_ALL) { add(RtLine(FtLevel.WARN, RtKind.TOO_BIG, "", hit.size, CAP_ALL)); return out }
                    chosen.add(hit)
                } else if (fits.isNotEmpty()) chosen.add(fits[rnd.nextInt(fits.size)])
            }
        }
        // a format that cannot hold a single file gets the smallest number it needs
        if (chosen.isNotEmpty() && chosen.size < need) {
            for (e in (fits + entries.filter { it.size in 1..(16L shl 20) }).distinct().filter { f -> chosen.none { it === f } }.shuffled(rnd)) { if (chosen.size >= need) break; chosen.add(e) }
        }
        if (chosen.isEmpty() || chosen.size < need) {
            add(RtLine(FtLevel.FAIL, RtKind.NO_FILE_FITS, "", plan.maxBytes, entries.size.toLong()))
            return out
        }
        add(RtLine(FtLevel.PASS, RtKind.PICKED, nameList(chosen), chosen.sumOf { it.size }, chosen.size.toLong()))
        if (cancelled()) return out

        workDir.mkdirs()
        val stamp = System.nanoTime()
        val tmpFiles = chosen.indices.map { File(workDir, "rt-$stamp-$it.src") }
        val target = File(workDir, "rt-$stamp.pack")
        val names = chosen.map { norm(it.name) }
        // samples are only kept when they are small: a game archive of several GB would fill the cache folder
        val keepWanted = keepDir != null && chosen.sumOf { it.size } <= CAP_ALL
        try {
            // 1. extract the chosen files (to disk; only the SHA-256 and size are kept in memory)
            val t0 = System.nanoTime()
            val hashes = ArrayList<String>()
            val sizes = ArrayList<Long>()
            try {
                val key = unpackKey ?: runCatching { fmt.detectKey(src, entries) }.getOrNull()?.takeIf { it != 0 }
                val xo = rOpts.copy(xorKey = key)
                for ((i, c) in chosen.withIndex()) {
                    if (isCancelled()) { add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return out }
                    tmpFiles[i].outputStream().buffered(1 shl 16).use { fo ->
                        val sink = HashSink(fo)
                        fmt.extract(src, c, sink, xo)
                        hashes.add(sink.hex()); sizes.add(sink.size)
                    }
                }
            } catch (x: Exception) { fail(RtKind.ERROR_UNPACK, msg(x)); return out }
            add(RtLine(FtLevel.PASS, RtKind.UNPACKED, "", sizes.sum(), (System.nanoTime() - t0) / 1_000_000))
            if (cancelled()) return out

            // 2. pack them again
            val t1 = System.nanoTime()
            val packKey = packEx["xor"]?.toIntOrNull()?.takeIf { it in 0..255 }
            try {
                val packOpts = Options(
                    xorKey = packKey,
                    compress = packEx["compress"]?.let { it == "1" } ?: true,
                    extra = packEx,
                )
                val inputs = chosen.indices.map { i -> PackInput(names[i], sizes[i]) { tmpFiles[i].inputStream() } }
                fmt.pack(inputs, target, packOpts) { _, _, _ -> }
            } catch (x: Exception) { fail(RtKind.ERROR_PACK, msg(x)); return out }
            add(RtLine(FtLevel.PASS, RtKind.PACKED, "", target.length(), (System.nanoTime() - t1) / 1_000_000))
            if (!keepWanted) tmpFiles.forEach { it.delete() } // free the space before reading back
            if (cancelled()) return out

            // 3. read the new archive back
            FileSource(target).use { s2 ->
                val o2 = Options(xorKey = packKey ?: unpackKey, extra = readExtra(packEx + unpackExtra + fmt.readBackExtra(target, packEx), target.name))
                val back = try { fmt.list(s2, o2) } catch (x: Exception) { fail(RtKind.ERROR_READ, msg(x)); return out }
                if (back.size != chosen.size) { add(RtLine(FtLevel.FAIL, RtKind.COUNT, "", back.size.toLong(), chosen.size.toLong())); return out }
                val byName = back.associateBy { norm(it.name) }
                for (i in chosen.indices) {
                    if (isCancelled()) { add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return out }
                    val b = byName[names[i]] ?: run { fail(RtKind.NAME, back.joinToString(", ") { it.name }); return out }
                    val sink = HashSink(null)
                    try { fmt.extract(s2, b, sink, o2) } catch (x: Exception) { fail(RtKind.ERROR_READ, msg(x)); return out }
                    if (sink.size != sizes[i] || sink.hex() != hashes[i]) { fail(RtKind.CONTENT_BAD, names[i]); return out }
                }
                add(RtLine(FtLevel.PASS, RtKind.CONTENT_OK, hashes[0].take(12), chosen.size.toLong()))

                // 4. compare with the original archive: header, detection, sizes and (full mode) every byte
                val n = minOf(32L, src.length, s2.length).toInt()
                val h1 = src.readFully(0, n)
                val h2 = s2.readFully(0, n)
                var same = 0
                while (same < n && h1[same] == h2[same]) same++
                // only a warning when the original has magic bytes that the new archive does not carry
                val magicLost = n >= 4 && runCatching { fmt.matches(h1) && !fmt.matches(h2) }.getOrDefault(false)
                add(RtLine(if (magicLost) FtLevel.WARN else FtLevel.PASS, RtKind.HEADER, "", same.toLong(), n.toLong()))
                if (!fmt.explicitOnly) {
                    val head = s2.readFully(0, minOf(16L, s2.length).toInt())
                    val ok = runCatching { fmt.matches(head) || fmt.detects(s2, head) }.getOrDefault(false)
                    if (!ok) add(RtLine(FtLevel.WARN, RtKind.NOT_DETECTED))
                    else {
                        val all = runCatching { FormatRegistry.detectAll(s2) }.getOrDefault(emptyList())
                        if (all.size > 1) add(RtLine(FtLevel.PASS, RtKind.DETECTED_BY, all.joinToString(", ") { it.displayName }))
                    }
                }
                add(RtLine(FtLevel.PASS, RtKind.SIZES, "", src.length, s2.length))
                if (mode == RtPlan.FULL) {
                    val (first, count) = firstDiff(src, s2)
                    if (first < 0) add(RtLine(FtLevel.PASS, RtKind.STRUCT_SAME))
                    else {
                        // where does the first difference lie: in the data of a file of the original, or in the header / index?
                        val inData = entries.firstOrNull { first >= it.offset && first < it.offset + maxOf(it.packedSize, 1L) }
                        if (inData != null) add(RtLine(FtLevel.WARN, RtKind.STRUCT_DATA, inData.name, first, count))
                        else add(RtLine(FtLevel.WARN, RtKind.STRUCT_INDEX, "", first, count))
                    }
                }
            }
        } finally {
            if (keepWanted && keepDir != null) runCatching { keep(keepDir, names, tmpFiles, target, fmt.packExtension(packExtra) ?: fmt.extensions.first()) }
            tmpFiles.forEach { it.delete() }
            target.delete()
            File(target.path + ".x").deleteRecursively()
        }
        return out
    }

    /** Copy the extracted files and the new archive into [dir] (replacing its old content). */
    private fun keep(dir: File, names: List<String>, files: List<File>, target: File, ext: String) {
        dir.deleteRecursively()
        File(dir, "files").mkdirs()
        for ((i, f) in files.withIndex()) {
            if (!f.isFile) continue
            val rel = names[i].split('/').filter { it.isNotEmpty() && it != "." && it != ".." }.joinToString("/").ifEmpty { "file$i" }
            val dst = File(File(dir, "files"), rel)
            dst.parentFile?.mkdirs()
            f.copyTo(dst, overwrite = true)
        }
        if (target.isFile) target.copyTo(File(dir, "repacked.$ext"), overwrite = true)
        File(target.path + ".x").listFiles()?.forEach { it.copyTo(File(dir, it.name), overwrite = true) }
    }
}
