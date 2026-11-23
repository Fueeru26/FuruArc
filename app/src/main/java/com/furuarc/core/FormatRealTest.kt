package com.furuarc.core

import java.io.ByteArrayOutputStream
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
    PICKED,        // the random file that was used: arg = name, n1 = size
    NO_FILE_FITS,  // no file is small enough: n1 = limit (bytes), n2 = file count
    UNPACKED,      // the file was extracted: n1 = bytes, n2 = ms
    PACKED,        // the file was packed again: n1 = archive bytes, n2 = ms
    CONTENT_OK,    // the packed archive gives the same file back (SHA-256 equal): arg = hash prefix
    CONTENT_BAD,   // different data after unpacking the new archive
    COUNT,         // the new archive does not hold exactly one file: n1 = count
    NAME,          // another file name: arg = name found
    HEADER,        // header compared with the original: n1 = equal leading bytes, n2 = bytes compared
    NOT_DETECTED,  // the new archive is not recognised as this format
    DETECTED_BY,   // formats that accept the new archive: arg = names
    SIZES,         // sizes: n1 = original, n2 = new
    ERROR_UNPACK,  // extracting failed: arg = message
    ERROR_PACK,    // packing failed: arg = message
    ERROR_READ,    // reading the new archive failed: arg = message
    CANCELLED,
}

class RtLine(val level: FtLevel, val kind: RtKind, val arg: String = "", val n1: Long = 0, val n2: Long = 0)

/** Result of [FormatRealTest.check]: [entries] > 0 = the file is an archive of the chosen format. */
class RtCheck(val entries: Int, val foundName: String?, val error: String?)

/**
 * Format Test with a real archive: the archive is opened with the chosen format, one random file (not larger than a limit)
 * is extracted and packed again alone, and the new archive is read back and compared with the original.
 * Pure Kotlin (no Android classes), so it also runs in unit tests.
 */
object FormatRealTest {
    private fun norm(n: String) = n.replace('\\', '/').trimStart('/')

    private fun sha(b: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { "%02x".format(it) }

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
        val list = try {
            fmt.list(src, Options(extra = readExtra(unpackExtra, archiveName)))
        } catch (e: Exception) {
            null
        }
        val other = runCatching { FormatRegistry.detect(src) }.getOrNull()
        if (list == null || list.isEmpty()) {
            return RtCheck(0, other?.takeIf { it.id != fmt.id }?.displayName, if (list == null) "unreadable" else "no files")
        }
        // another format with magic bytes recognises it and this one does not: the wrong file
        val own = runCatching { fmt.matches(head) || fmt.detects(src, head) }.getOrDefault(false)
        if (!own && other != null && other.id != fmt.id) return RtCheck(0, other.displayName, "other format")
        return RtCheck(list.size, null, null)
    }

    /**
     * Runs the test. [maxBytes] = largest file that may be chosen. Lines come back in the order of the steps;
     * the first FAIL ends the test.
     */
    fun run(
        src: RandomSource,
        archiveName: String,
        fmt: ArchiveFormat,
        unpackExtra: Map<String, String>,
        unpackKey: Int?,
        packExtra: Map<String, String>,
        maxBytes: Long,
        workDir: File,
        random: Random = Random.Default,
        isCancelled: () -> Boolean = { false },
        onLine: (RtLine) -> Unit = {},
    ): List<RtLine> {
        val out = ArrayList<RtLine>()
        fun add(l: RtLine): RtLine { out.add(l); onLine(l); return l }
        fun fail(kind: RtKind, arg: String = "") = add(RtLine(FtLevel.FAIL, kind, arg))
        fun msg(e: Throwable) = e.message ?: e.javaClass.simpleName

        val rOpts = Options(xorKey = unpackKey, extra = readExtra(unpackExtra, archiveName))
        val entries = try { fmt.list(src, rOpts) } catch (e: Exception) { fail(RtKind.ERROR_UNPACK, msg(e)); return out }
        val need = fmt.minPackFiles.coerceAtLeast(1)
        val fits = entries.filter { it.size in 1..maxBytes }
        if (fits.size < need) {
            add(RtLine(FtLevel.FAIL, RtKind.NO_FILE_FITS, "", maxBytes, entries.size.toLong()))
            return out
        }
        // one random file; a format that cannot hold a single file gets the smallest number it needs
        val chosen = fits.shuffled(random).take(need)
        add(RtLine(FtLevel.PASS, RtKind.PICKED, chosen.joinToString(", ") { it.name }, chosen.sumOf { it.size }))
        if (isCancelled()) { add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return out }

        workDir.mkdirs()
        val stamp = System.nanoTime()
        val tmpFiles = chosen.indices.map { File(workDir, "rt-$stamp-$it.src") }
        val target = File(workDir, "rt-$stamp.pack")
        try {
            // 1. extract the chosen file(s)
            val t0 = System.nanoTime()
            val origs = ArrayList<ByteArray>()
            try {
                val key = unpackKey ?: runCatching { fmt.detectKey(src, entries) }.getOrNull()?.takeIf { it != 0 }
                for (c in chosen) {
                    val bo = ByteArrayOutputStream(c.size.coerceAtMost(1L shl 26).toInt())
                    fmt.extract(src, c, bo, rOpts.copy(xorKey = key))
                    origs.add(bo.toByteArray())
                }
            } catch (x: Exception) { fail(RtKind.ERROR_UNPACK, msg(x)); return out }
            for (i in chosen.indices) tmpFiles[i].writeBytes(origs[i])
            add(RtLine(FtLevel.PASS, RtKind.UNPACKED, "", origs.sumOf { it.size.toLong() }, (System.nanoTime() - t0) / 1_000_000))
            if (isCancelled()) { add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return out }

            // 2. pack them again
            val names = chosen.map { norm(it.name) }
            val t1 = System.nanoTime()
            val packKey = packExtra["xor"]?.toIntOrNull()?.takeIf { it in 0..255 }
            try {
                val packOpts = Options(
                    xorKey = packKey,
                    compress = packExtra["compress"]?.let { it == "1" } ?: true,
                    extra = packExtra,
                )
                val inputs = chosen.indices.map { i -> PackInput(names[i], origs[i].size.toLong()) { tmpFiles[i].inputStream() } }
                fmt.pack(inputs, target, packOpts) { _, _, _ -> }
            } catch (x: Exception) { fail(RtKind.ERROR_PACK, msg(x)); return out }
            add(RtLine(FtLevel.PASS, RtKind.PACKED, "", target.length(), (System.nanoTime() - t1) / 1_000_000))
            if (isCancelled()) { add(RtLine(FtLevel.WARN, RtKind.CANCELLED)); return out }

            // 3. read the new archive back
            FileSource(target).use { s2 ->
                val o2 = Options(xorKey = packKey ?: unpackKey, extra = readExtra(packExtra + unpackExtra, target.name))
                val back = try { fmt.list(s2, o2) } catch (x: Exception) { fail(RtKind.ERROR_READ, msg(x)); return out }
                if (back.size != chosen.size) { add(RtLine(FtLevel.FAIL, RtKind.COUNT, "", back.size.toLong(), chosen.size.toLong())); return out }
                val byName = back.associateBy { norm(it.name) }
                var hash = ""
                for (i in chosen.indices) {
                    val b = byName[names[i]] ?: run { fail(RtKind.NAME, back.joinToString(", ") { it.name }); return out }
                    val got = try {
                        val bo = ByteArrayOutputStream(origs[i].size)
                        fmt.extract(s2, b, bo, o2)
                        bo.toByteArray()
                    } catch (x: Exception) { fail(RtKind.ERROR_READ, msg(x)); return out }
                    if (!got.contentEquals(origs[i])) { fail(RtKind.CONTENT_BAD, names[i]); return out }
                    if (i == 0) hash = sha(origs[0]).take(12)
                }
                add(RtLine(FtLevel.PASS, RtKind.CONTENT_OK, hash))

                // 4. compare with the original archive: header, detection and sizes
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
            }
        } finally {
            tmpFiles.forEach { it.delete() }
            target.delete()
            File(target.path + ".x").deleteRecursively()
        }
        return out
    }
}
