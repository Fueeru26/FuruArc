package com.furuarc.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile

/**
 * Format Test: for every archive format that can be packed, builds a small archive from generated sample files,
 * unpacks it again with the same format and compares names and contents.
 * Pure logic (no Android) so the same code can run on a computer. Texts are chosen by the UI from [FtKind].
 */
enum class FtLevel { PASS, WARN, FAIL, SKIP }

enum class FtKind {
    OK,            // identical after pack + unpack
    ERROR,         // an exception: arg = message
    COUNT,         // different number of files: n1 = expected, n2 = got, arg = names that came back
    MISSING,       // arg = name that did not come back
    CONTENT,       // arg = name whose content differs
    DETECT_OTHER,  // round trip is fine but automatic detection picks another format: arg = its name
    DETECT_NONE,   // round trip is fine but automatic detection does not recognise the archive
    NEEDS_GAME_FILES, // skipped: the format needs files that only come from a real game archive (index file, original metadata)
}

/** One test. [extra] = options for packing; [unpackExtra] / [unpackKey] = options for unpacking. */
class FtCase(
    val formatId: String,
    val variant: String,
    val extra: Map<String, String> = emptyMap(),
    val unpackExtra: Map<String, String> = emptyMap(),
    val unpackKey: Int? = null,
    val skip: FtKind? = null,
    val sample: String = "generic",   // which sample set to pack: generic, ovk (numbered .ogg files) or gameexe (Gameexe.ini)
    val byOrder: Boolean = false,      // compare contents in name order instead of by name (the format names its files itself)
)

class FtResult(
    val formatId: String,
    val engine: String,
    val variant: String,
    val level: FtLevel,
    val kind: FtKind,
    val arg: String = "",
    val n1: Long = 0,
    val n2: Long = 0,
    val files: Int = 0,
    val bytes: Long = 0,
    val ms: Long = 0,
)

object FormatSelfTest {
    /** The sample files: a short text, 5000 random-looking bytes and 70000 compressible bytes. */
    fun sampleFiles(): List<Pair<String, ByteArray>> {
        val text = ("FuruArc format test.\nThe quick brown fox jumps over the lazy dog.\n").toByteArray(Charsets.UTF_8)
        val rnd = ByteArray(5000)
        var s = 0x1234567L
        for (i in rnd.indices) {
            s = s * 6364136223846793005L + 1442695040888963407L
            rnd[i] = (s ushr 33).toByte()
        }
        val rep = ByteArray(70000) { ((it / 7) % 251).toByte() }
        return listOf("a.txt" to text, "b.bin" to rnd, "c.dat" to rep)
    }

    /** All tests, in the order shown. A format that cannot be packed has no test. */
    fun cases(): List<FtCase> {
        val l = ArrayList<FtCase>()
        fun add(id: String, variant: String, extra: Map<String, String> = emptyMap(), unpackExtra: Map<String, String> = emptyMap(), key: Int? = null,
                sample: String = "generic", byOrder: Boolean = false) {
            l.add(FtCase(id, variant, extra, unpackExtra, key, null, sample, byOrder))
        }
        add("rpa", "RPA-3.0")
        add("rpa", "RPA-2.0", mapOf("rpa_ver" to "2.0"))
        add("xp3", "deflate")
        add("xp3", "stored", mapOf("compress" to "0"))
        add("xp3", "XOR 0x5A", mapOf("xor" to "90"), key = 90)
        for (v in listOf("auto", "pf8", "pf6", "pf2")) add("pfs", v, mapOf("pfs_ver" to v))
        add("rgss1", "")
        add("rgss3", "")
        add("bgi", "ARC20", mapOf("bgi_ver" to "v2"))
        add("bgi", "v1", mapOf("bgi_ver" to "v1"))
        add("tyrano", "deflate")
        add("tyrano", "stored", mapOf("compress" to "0"))
        add("catsystem2", "name 64", mapOf("int_name" to "64"))
        add("catsystem2", "name 32", mapOf("int_name" to "32"))
        for (v in listOf("494", "480", "500")) add("yuris", "v$v", mapOf("ypf_ver" to v))
        add("escude", "")
        add("itufuru", "")
        add("circuscrm", "")
        add("noa", "raw")
        add("noa", "password", mapOf("noapass" to "furu"), mapOf("noapass" to "furu"))
        add("nexas", "zlib + Huffman")
        add("nexas", "stored", mapOf("nxcomp" to "none"))
        add("nexas", "plain index", mapOf("nxindex" to "plain"))
        add("majiro", "")
        add("softpal", "classic 32", mapOf("pac_var" to "classic", "pac_name" to "32"))
        add("softpal", "classic 16", mapOf("pac_var" to "classic", "pac_name" to "16"))
        add("softpal", "Amuse Craft", mapOf("pac_var" to "amuse"))
        add("nscripter", "SAR")
        add("nscripter", "NSA", mapOf("ns_type" to "nsa"))
        for (v in listOf("0", "8", "12")) add("willplus", "name $v", mapOf("wp_name" to v))
        add("favorite", "")
        add("circus", "")
        for (v in listOf("60", "48", "36")) add("circusdat", "name $v", mapOf("dat_nl" to v))
        for (v in listOf("32", "30", "20")) add("silky", "name $v", mapOf("silky_name" to v))
        add("silky", "XOR 0x5A", mapOf("silky_xor" to "90"), key = 90)
        l.add(FtCase("siglus", "", skip = FtKind.NEEDS_GAME_FILES))
        add("siglus_ovk", "", sample = "ovk", byOrder = true)
        add("siglus_gameexe", "", sample = "gameexe")
        add("unity", "UnityFS v7", mapOf("ufsver" to "7"))
        add("unity", "UnityFS v6", mapOf("ufsver" to "6"))
        add("unity", "UnityFS v6 (Unity 2018)", mapOf("ufsver" to "6", "ufsunity" to "2018.4.36f1"))
        add("shiinario", "WARC 1.1")
        l.add(FtCase("age", "", skip = FtKind.NEEDS_GAME_FILES))
        return l
    }

    /** Sample files for [FtCase.sample]. */
    fun sampleFor(kind: String): List<Pair<String, ByteArray>> = when (kind) {
        "ovk" -> (1..3).map { n -> ("v$n.ogg") to (byteArrayOf(0x4F, 0x67, 0x67, 0x53) + ByteArray(300 * n) { ((it * 31 + n) and 0xFF).toByte() }) }
        "gameexe" -> listOf("Gameexe.ini" to "#CAPTION = \"FuruArc test\"\r\n#SCREEN_SIZE_MODE = 1\r\n#NAMAE.000 = \"Test\"\r\n".toByteArray(Charsets.UTF_8))
        else -> sampleFiles()
    }

    private class FileSrc(f: File) : RandomSource {
        private val raf = RandomAccessFile(f, "r")
        override val length: Long = raf.length()
        override fun readAt(pos: Long, buf: ByteArray, off: Int, len: Int): Int = synchronized(raf) { raf.seek(pos); raf.read(buf, off, len) }
        override fun close() = raf.close()
    }

    private fun norm(n: String) = n.replace('\\', '/').trimStart('/')

    private fun short(s: String, n: Int = 160) = if (s.length <= n) s else s.substring(0, n) + "…"

    /**
     * Runs every test. [workDir] must be writable (a temporary folder is created inside it and removed at the end).
     * [progress] = (tests done, tests total, label of the test about to run). [onResult] is called after each test.
     */
    fun run(
        workDir: File,
        isCancelled: () -> Boolean = { false },
        progress: (Int, Int, String) -> Unit = { _, _, _ -> },
        onResult: (FtResult) -> Unit = {},
    ): List<FtResult> {
        val dir = File(workDir, "formattest-" + System.nanoTime())
        dir.mkdirs()
        val cases = cases()
        val out = ArrayList<FtResult>()
        try {
            for ((i, c) in cases.withIndex()) {
                if (isCancelled()) break
                val label = FormatRegistry.byId(c.formatId).displayName + (if (c.variant.isEmpty()) "" else " · " + c.variant)
                progress(i, cases.size, label)
                val r = runCase(c, i, dir)
                out.add(r)
                onResult(r)
            }
        } finally {
            dir.deleteRecursively()
        }
        return out
    }

    fun runCase(c: FtCase, index: Int, dir: File): FtResult {
        val fmt = FormatRegistry.byId(c.formatId)
        val files = sampleFor(c.sample)
        val t0 = System.nanoTime()
        fun ms() = (System.nanoTime() - t0) / 1_000_000
        fun res(level: FtLevel, kind: FtKind, arg: String = "", n1: Long = 0, n2: Long = 0, ok: Boolean = false) =
            FtResult(fmt.id, fmt.displayName, c.variant, level, kind, arg, n1, n2,
                if (ok) files.size else 0, if (ok) files.sumOf { it.second.size.toLong() } else 0, ms())
        if (c.skip != null) return res(FtLevel.SKIP, c.skip)
        val target = File(dir, "t$index." + (fmt.packExtension(c.extra) ?: fmt.extensions.first()))
        try {
            val inputs = files.map { (n, b) -> PackInput(n, b.size.toLong()) { ByteArrayInputStream(b) } }
            val packOpts = Options(
                xorKey = c.extra["xor"]?.toIntOrNull()?.takeIf { it in 0..255 },
                compress = c.extra["compress"]?.let { it == "1" } ?: true,
                extra = c.extra,
            )
            fmt.pack(inputs, target, packOpts) { _, _, _ -> }
            FileSrc(target).use { src ->
                val listOpts = Options(extra = c.unpackExtra + ("_arc" to target.name))
                val entries = fmt.list(src, listOpts)
                if (entries.size != files.size)
                    return res(FtLevel.FAIL, FtKind.COUNT, short(entries.joinToString(", ") { it.name }), files.size.toLong(), entries.size.toLong())
                val byName = HashMap<String, Entry>()
                for (e in entries) byName[norm(e.name)] = e
                val sortedEntries = entries.sortedBy { norm(it.name) }
                val xo = Options(xorKey = c.unpackKey, extra = c.unpackExtra + ("_arc" to target.name))
                for ((i, f) in files.sortedBy { it.first }.withIndex()) {
                    val (n, b) = f
                    val e = (if (c.byOrder) sortedEntries[i] else byName[n])
                        ?: return res(FtLevel.FAIL, FtKind.MISSING, n + " (" + short(entries.joinToString(", ") { it.name }, 80) + ")")
                    val bo = ByteArrayOutputStream(b.size)
                    fmt.extract(src, e, bo, xo)
                    if (!bo.toByteArray().contentEquals(b)) return res(FtLevel.FAIL, FtKind.CONTENT, n)
                }
                if (!fmt.explicitOnly) {
                    val d = FormatRegistry.detect(src)
                    if (d == null) return res(FtLevel.WARN, FtKind.DETECT_NONE, ok = true)
                    if (d.id != fmt.id) return res(FtLevel.WARN, FtKind.DETECT_OTHER, d.displayName, ok = true)
                }
                return res(FtLevel.PASS, FtKind.OK, ok = true)
            }
        } catch (e: Throwable) {
            return res(FtLevel.FAIL, FtKind.ERROR, short((e.message ?: "") .ifBlank { e.javaClass.simpleName }))
        } finally {
            target.delete()
        }
    }
}
