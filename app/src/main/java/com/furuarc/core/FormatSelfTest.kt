package com.furuarc.core

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

/**
 * Format Test. Pure logic (no Android) so the same code runs on the device (Format Test screen) and on a computer
 * (unit test in CI). Texts are chosen by the UI from [FtKind]. Groups of tests:
 *  1. Round trip: for every archive format that can be packed, builds an archive from generated sample files,
 *     unpacks it, compares, packs the unpacked files again (must give the same archive) and checks automatic detection.
 *     Besides the normal samples there are edge samples (empty file, exactly 1 MiB, Unicode names, subfolders, 300 files);
 *     a problem found with those is a warning, because it can be a known limit of the format.
 *  2. Real archives (golden files): small archives from real games listed in the .golden manifests in assets/formattest are unpacked and
 *     the SHA-256 of each file is compared. This is what proves that the format matches the game, not only itself.
 *  3. Codecs (LZMA, LZ4, LZSS) against reference data.
 *  4. Script text: export and import must not change a script when the texts are unchanged.
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
    REPACK_DIFF,   // packing the unpacked files again gives a different archive (files are the same)
    REPACK_CONTENT, // the archive packed from the unpacked files has different contents: arg = name
    HASH_MISMATCH, // real archive: the SHA-256 of a file differs from the expected one: arg = name
    SCRIPT_EMPTY,  // no text found in the sample script
    SCRIPT_CHANGED, // import with unchanged texts changed the script
    SCRIPT_APPLY,  // import of changed texts did not work: arg = detail
}

/** One test. [extra] = options for packing; [unpackExtra] / [unpackKey] = options for unpacking. */
class FtCase(
    val formatId: String,
    val variant: String,
    val extra: Map<String, String> = emptyMap(),
    val unpackExtra: Map<String, String> = emptyMap(),
    val unpackKey: Int? = null,
    val skip: FtKind? = null,
    val sample: String = "generic",   // which sample set to pack: generic, ovk, gameexe, edge, unicode, nested or many
    val byOrder: Boolean = false,      // compare contents in name order instead of by name (the format names its files itself)
    val soft: Boolean = false,         // a failure is shown as a warning (edge samples: may be a known limit of the format)
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
    val detectedBy: List<String> = emptyList(),   // every format whose detection accepts the archive
)

/** Where real (golden) archives come from: Android assets on the device, a folder in the unit test. */
interface FtGoldenSource {
    fun names(): List<String>          // *.golden manifest names
    fun read(name: String): ByteArray? // manifest or archive bytes, null if missing
}

object FormatSelfTest {
    private const val V_LZMA1 = "5d0000010000231d4a477fe2a9602ce0d75202b78b151e4f418c9b04954a8e00b689207028f2fca5eae85410604815c3260fb2ec54eecc3517082af19b0458ad775783ff66b80c83abb3721ad24b33c28289786cc946bb0eda4adbea14a9c8c734bc1d758fd0691b52ae83662830f013b3fa6a5e92ccc39246c13c837dee2382f139ce5aca39ea4d3ab02ab47e2cccb892c2f0b8b9ebfdcd45af759efcb82353dbac1deb14916f7adeddb69c63332011ac30cf563e2f7d8b457c0a48a93fc1546d272b0576d42a940a8df346532efefe6651004fa9ad87b003b18b3d465dd6b6b7dd93e396f4a3e9e277dfc3367f84bcae189339ab0acf74776a6c53df048dd60a70176cc6da15ffffc6cd0000"
    private const val V_LZMA2 = "5d0000010000000893710648c5a8162b41362bc3223a1c5883d741a2028fdd3789210f02e08c235f61559e551aa9f16b3b05197335088e3454145892cff3483f24d13f6f6f53a35ca16ec8ef370ebb0dd6d58112f9703e8ee5bffe5bb5b05267c4dedb656b1b3d0e141c95a2ce77035c87e40d3a84dcc0fac4014b01f2c2581b0f478f25315f53b5c5db062af3172a4e010a75926f448530e54830b59ed42c96b27770aeb2f045f61e86a713703c1c11697fc44222d294fc92f46a0c79b7136e9224474eb2b8109d7d68805aa1083f73f5054e3d6c4687a2e8b3376f3da23cc9387e95af216282696171fb7e706b1ea46b3c6837b804a4d39ed7b1a550d3087cbcb8b6d3fbbed5e841ca455d46180a19c86741e7a6f819bae48ffa805197284922ce108233e347b3cf5e407ff620bcb932686e3c888472f9f382d843406d1017cb702c23c1825115c79c231d39b6fe20687a284a90fc39ebd08194df1289f11bc11a5f6f5b39e32fd8ed105b4666835b9981fb19ce64d33ef5c0f9e70932ad9884720f78647eacdf5c67376fdff958c5240bbf22d002d90155d844f06af441ef478ece9e825f4b3ed4abd1fe9e8cd744d46f2d91c78c50d0fa20cd9ba75589e091a0fb579170dc91d87704696cd8509a0b213efe1781c1502ea9e96926e2702565a56aca14b297e800648068a360d71a8a44d5ad3424ce6b148d035ae4c52082161254e1a7a954afd5ef7602793ee979284a1e9346537304a1b92191992d9ed51c8d57ff6d06602248dad61a66aec86f069dff9aea762bc0dfd7649d8d078e1a82f1a283a35b0828904662864852b6960e1394e72eac2b3c9c2e7235b1e8c0cc91e8307263bdc98eac8e41da4280cc7c26b0ab06062a5b5d97436dd88ea69671483b6b0814794771d9246cf63ddd367bada3527f3a1614c9687d8f6a527f7948dcb7bb747716f80a3e5500662b4508cae66d8fa1b013a89a82148f9eaeb35b4ecfce63cd8929e059f5623ff87ce821c5f868e14cb8db30789ec8661fb045c6c9d67d045c741eb8e7b1285fe82957e6f3632913a1368c8a333e6b4d022bedeb044dbc62689fc5ee517586251be2e2f3e19d41978475ea193b32ac0aeff02140cc432e8da74656a6fffffd23e120"
    private const val V_LZ4A = "ff064675727541726320666f726d617420746573742e201500ff7df0f1000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"
    private const val V_LZ4B = "1f410100ffffffd73f78797aea03ffffabf019000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f2021222324252627"
    private const val V_LZSS_IN = "fd06ebf044415441312eff414c4600055cdce8ff0f010911af7700025f0000080014e5f60be7f4efd0ac9b090700009043eb24011605ccfff0d9f9b6fb76e61f00a0eb0f01005d01dffc4b9d09eaf13853007d03ebf028179d0964e3f8fff049"
    private const val V_LZSS_OUT = "0600000044415441312e414c4600055cdce80f010911af77000200000800140000000000000000000b00000000000000d0ac9b0902000000904324010b00000000000000cce80f01d9f9b676e6ac9b09a0eb0f0100010000000000000000000000000000004b9d0900000000384b9d090300000028179d09640000000000000000000000f049"

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

    /** All round-trip tests, in the order shown. A format that cannot be packed has no test. */
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
        // edge samples: one set per format (the first normal case of the format supplies the options)
        val seen = HashSet<String>()
        val edge = ArrayList<FtCase>()
        for (c in l) {
            if (c.skip != null || c.sample != "generic" || !seen.add(c.formatId)) continue
            for ((kind, label) in EDGE) {
                edge.add(FtCase(c.formatId, "edge · $label", c.extra, c.unpackExtra, c.unpackKey, null, kind, false, true))
            }
        }
        l.addAll(edge)
        return l
    }

    private val EDGE = listOf(
        "edge" to "empty file + exactly 1 MiB",
        "unicode" to "Unicode and space in names",
        "nested" to "subfolders",
        "many" to "300 files",
    )

    /** Sample files for [FtCase.sample]. */
    fun sampleFor(kind: String): List<Pair<String, ByteArray>> = when (kind) {
        "ovk" -> (1..3).map { n -> ("v$n.ogg") to (byteArrayOf(0x4F, 0x67, 0x67, 0x53) + ByteArray(300 * n) { ((it * 31 + n) and 0xFF).toByte() }) }
        "gameexe" -> listOf("Gameexe.ini" to "#CAPTION = \"FuruArc test\"\r\n#SCREEN_SIZE_MODE = 1\r\n#NAMAE.000 = \"Test\"\r\n".toByteArray(Charsets.UTF_8))
        "edge" -> sampleFiles() + listOf("e.bin" to ByteArray(0), "m.bin" to ByteArray(1 shl 20) { ((it * 131) xor (it shr 8)).toByte() })
        "unicode" -> listOf(
            "あいう.txt" to "unicode name".toByteArray(Charsets.UTF_8),
            "space name.txt" to "name with a space".toByteArray(Charsets.UTF_8),
            "UPPER.TXT" to "UPPER CASE".toByteArray(Charsets.UTF_8),
        )
        "nested" -> listOf(
            "dir/a.txt" to "a in dir".toByteArray(Charsets.UTF_8),
            "dir/sub/b.bin" to ByteArray(700) { (it * 3).toByte() },
            "c.txt" to "c at the top".toByteArray(Charsets.UTF_8),
        )
        "many" -> (0 until 300).map { n -> String.format("f%03d.dat", n) to ByteArray(10 + n) { ((it * 7 + n) and 0xFF).toByte() } }
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

    private fun sha256(b: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(b).joinToString("") { String.format("%02x", it) }

    private class Job(val label: String, val body: (File) -> FtResult)

    private fun jobs(golden: FtGoldenSource?): List<Job> {
        val out = ArrayList<Job>()
        for ((i, c) in cases().withIndex()) {
            val label = FormatRegistry.byId(c.formatId).displayName + (if (c.variant.isEmpty()) "" else " · " + c.variant)
            out.add(Job(label) { dir -> runCase(c, i, dir) })
        }
        if (golden != null) for (n in golden.names().sorted()) out.add(Job("Real archive · $n") { dir -> runGolden(n, golden, dir) })
        for (t in codecTests()) out.add(Job("Codec · ${t.first}") { _ -> t.second() })
        for (t in scriptTests()) out.add(Job("Script · ${t.first}") { _ -> t.second() })
        return out
    }

    fun totalCount(golden: FtGoldenSource? = null): Int = jobs(golden).size

    /**
     * Runs every test. [workDir] must be writable (a temporary folder is created inside it and removed at the end).
     * [progress] = (tests done, tests total, label of the test about to run). [onResult] is called after each test.
     */
    fun run(
        workDir: File,
        isCancelled: () -> Boolean = { false },
        progress: (Int, Int, String) -> Unit = { _, _, _ -> },
        onResult: (FtResult) -> Unit = {},
        golden: FtGoldenSource? = null,
    ): List<FtResult> {
        val dir = File(workDir, "formattest-" + System.nanoTime())
        dir.mkdirs()
        val list = jobs(golden)
        val out = ArrayList<FtResult>()
        try {
            for ((i, j) in list.withIndex()) {
                if (isCancelled()) break
                progress(i, list.size, j.label)
                val r = j.body(dir)
                out.add(r)
                onResult(r)
            }
        } finally {
            dir.deleteRecursively()
        }
        return out
    }

    // ------------------------------------------------------------------ round trip

    private class Back(val fail: FtResult? = null, val data: Map<String, ByteArray> = emptyMap(), val detected: List<String> = emptyList())

    private fun readBack(
        fmt: ArchiveFormat, c: FtCase, target: File, files: List<Pair<String, ByteArray>>, detect: Boolean,
        mk: (FtLevel, FtKind, String, Long, Long) -> FtResult,
    ): Back {
        FileSrc(target).use { src ->
            val listOpts = Options(extra = c.unpackExtra + ("_arc" to target.name))
            val entries = fmt.list(src, listOpts)
            if (entries.size != files.size)
                return Back(mk(FtLevel.FAIL, FtKind.COUNT, short(entries.joinToString(", ") { it.name }), files.size.toLong(), entries.size.toLong()))
            val byName = HashMap<String, Entry>()
            for (e in entries) byName[norm(e.name)] = e
            val sortedEntries = entries.sortedBy { norm(it.name) }
            val xo = Options(xorKey = c.unpackKey, extra = c.unpackExtra + ("_arc" to target.name))
            val data = LinkedHashMap<String, ByteArray>()
            for ((i, f) in files.sortedBy { it.first }.withIndex()) {
                val (n, b) = f
                val e = (if (c.byOrder) sortedEntries[i] else byName[norm(n)])
                    ?: return Back(mk(FtLevel.FAIL, FtKind.MISSING, n + " (" + short(entries.joinToString(", ") { it.name }, 80) + ")", 0, 0))
                val bo = ByteArrayOutputStream(b.size)
                fmt.extract(src, e, bo, xo)
                val got = bo.toByteArray()
                if (!got.contentEquals(b)) return Back(mk(FtLevel.FAIL, FtKind.CONTENT, n, 0, 0))
                data[n] = got
            }
            val det = if (detect) FormatRegistry.detectAll(src).map { it.displayName } else emptyList()
            return Back(null, data, det)
        }
    }

    private fun packTo(fmt: ArchiveFormat, c: FtCase, files: List<Pair<String, ByteArray>>, target: File) {
        val inputs = files.map { (n, b) -> PackInput(n, b.size.toLong()) { ByteArrayInputStream(b) } }
        val packOpts = Options(
            xorKey = c.extra["xor"]?.toIntOrNull()?.takeIf { it in 0..255 },
            compress = c.extra["compress"]?.let { it == "1" } ?: true,
            extra = c.extra,
        )
        fmt.pack(inputs, target, packOpts) { _, _, _ -> }
    }

    fun runCase(c: FtCase, index: Int, dir: File): FtResult {
        val fmt = FormatRegistry.byId(c.formatId)
        val files = sampleFor(c.sample)
        val t0 = System.nanoTime()
        fun ms() = (System.nanoTime() - t0) / 1_000_000
        fun res(level: FtLevel, kind: FtKind, arg: String = "", n1: Long = 0, n2: Long = 0, ok: Boolean = false, det: List<String> = emptyList()) =
            FtResult(fmt.id, fmt.displayName, c.variant, level, kind, arg, n1, n2,
                if (ok) files.size else 0, if (ok) files.sumOf { it.second.size.toLong() } else 0, ms(), det)
        // a failure on an edge sample is only a warning: it can be a known limit of the format
        fun mk(level: FtLevel, kind: FtKind, arg: String, n1: Long, n2: Long) =
            res(if (c.soft && level == FtLevel.FAIL) FtLevel.WARN else level, kind, arg, n1, n2)
        if (c.skip != null) return res(FtLevel.SKIP, c.skip)
        val ext = fmt.packExtension(c.extra) ?: fmt.extensions.first()
        val target = File(dir, "t$index.$ext")
        val target2 = File(dir, "r$index.$ext")
        try {
            packTo(fmt, c, files, target)
            val b1 = readBack(fmt, c, target, files, c.sample == "generic", ::mk)
            b1.fail?.let { return it }
            // pack the unpacked files again: same files, same order -> must give the same archive
            val again = files.map { (n, _) -> n to (b1.data[n] ?: ByteArray(0)) }
            packTo(fmt, c, again, target2)
            val b2 = readBack(fmt, c, target2, files, false) { lv, k, a, x, y ->
                mk(lv, if (k == FtKind.CONTENT || k == FtKind.MISSING || k == FtKind.COUNT) FtKind.REPACK_CONTENT else k, if (k == FtKind.COUNT) "" else a, x, y)
            }
            b2.fail?.let { return it }
            val det = b1.detected
            if (c.sample == "generic" && !fmt.explicitOnly) {
                if (det.isEmpty()) return res(FtLevel.WARN, FtKind.DETECT_NONE, ok = true)
                if (det.first() != fmt.displayName) return res(FtLevel.WARN, FtKind.DETECT_OTHER, det.first(), ok = true, det = det)
            }
            if (target.length() != target2.length() || !target.readBytes().contentEquals(target2.readBytes()))
                return res(FtLevel.WARN, FtKind.REPACK_DIFF, ok = true, det = det)
            return res(FtLevel.PASS, FtKind.OK, ok = true, det = det)
        } catch (e: Throwable) {
            val lv = if (c.soft) FtLevel.WARN else FtLevel.FAIL
            return res(lv, FtKind.ERROR, short((e.message ?: "").ifBlank { e.javaClass.simpleName }))
        } finally {
            target.delete(); target2.delete()
        }
    }

    // ------------------------------------------------------------------ real archives

    /**
     * Manifest (*.golden, text): `format=<id>`, `archive=<file>`, `key=<0..255>` (XOR key), `opt.<name>=<value>` (a value
     * starting with @ names a file next to the manifest, passed to the format as a path), `file=<name>:<sha256>` per file to check.
     */
    private fun runGolden(name: String, g: FtGoldenSource, dir: File): FtResult {
        val t0 = System.nanoTime()
        fun ms() = (System.nanoTime() - t0) / 1_000_000
        var fid = "?"
        var engine = name
        fun res(level: FtLevel, kind: FtKind, arg: String = "", files: Int = 0, bytes: Long = 0) = FtResult(fid, engine, "real archive", level, kind, arg, 0, 0, files, bytes, ms())
        try {
            val text = String(g.read(name) ?: return res(FtLevel.FAIL, FtKind.MISSING, name), Charsets.UTF_8)
            var archive = ""
            var key: Int? = null
            val opts = LinkedHashMap<String, String>()
            val expect = ArrayList<Pair<String, String>>()
            for (raw in text.lines()) {
                val line = raw.trim()
                if (line.isEmpty() || line.startsWith("#")) continue
                val k = line.substringBefore('=').trim()
                val v = line.substringAfter('=', "").trim()
                when {
                    k == "format" -> fid = v
                    k == "archive" -> archive = v
                    k == "key" -> key = v.toIntOrNull()
                    k.startsWith("opt.") -> opts[k.substring(4)] = v
                    k == "file" -> expect.add(v.substringBeforeLast(':') to v.substringAfterLast(':').lowercase())
                }
            }
            val fmt = FormatRegistry.all.firstOrNull { it.id == fid } ?: return res(FtLevel.FAIL, FtKind.ERROR, "format $fid")
            engine = fmt.displayName
            val data = g.read(archive) ?: return res(FtLevel.FAIL, FtKind.MISSING, archive)
            val arc = File(dir, "g_" + archive)
            arc.writeBytes(data)
            val x = LinkedHashMap<String, String>()
            for ((k, v) in opts) {
                if (v.startsWith("@")) {
                    val side = File(dir, "g_" + v.substring(1))
                    side.writeBytes(g.read(v.substring(1)) ?: return res(FtLevel.FAIL, FtKind.MISSING, v.substring(1)))
                    x[k] = side.absolutePath
                } else x[k] = v
            }
            x["_arc"] = archive
            FileSrc(arc).use { src ->
                val entries = fmt.list(src, Options(xorKey = key, extra = x))
                val byName = HashMap<String, Entry>()
                for (e in entries) byName[norm(e.name).lowercase()] = e
                var bytes = 0L
                for ((n, h) in expect) {
                    val e = byName[norm(n).lowercase()] ?: return res(FtLevel.FAIL, FtKind.MISSING, n)
                    val bo = ByteArrayOutputStream()
                    fmt.extract(src, e, bo, Options(xorKey = key, extra = x))
                    val b = bo.toByteArray()
                    bytes += b.size
                    if (sha256(b) != h) return res(FtLevel.FAIL, FtKind.HASH_MISMATCH, n)
                }
                return res(FtLevel.PASS, FtKind.OK, files = expect.size, bytes = bytes)
            }
        } catch (e: Throwable) {
            return res(FtLevel.FAIL, FtKind.ERROR, short((e.message ?: "").ifBlank { e.javaClass.simpleName }))
        }
    }

    // ------------------------------------------------------------------ codecs

    private fun unhex(s: String) = ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private fun raw1(): ByteArray = ("FuruArc format test. ".repeat(20)).toByteArray(Charsets.US_ASCII) + ByteArray(256) { it.toByte() }
    private fun raw2(): ByteArray = ByteArray(2000) { ((it * it * 31 + it / 3) and 0xFF).toByte() }
    private fun raw3(): ByteArray = ByteArray(1000) { 'A'.code.toByte() } + "xyz".toByteArray() + ByteArray(700) { 'A'.code.toByte() } + ByteArray(40) { it.toByte() }

    // reference data made with the xz library (LZMA) and by hand from the LZ4 block format; the LZSS input is taken
    // from a real Eushully index (SYS4INI.BIN) and the output was checked against the real archive

    private fun codecTests(): List<Pair<String, () -> FtResult>> {
        fun task(name: String, body: () -> String?): Pair<String, () -> FtResult> = name to {
            val t0 = System.nanoTime()
            val r = try {
                val bad = body()
                if (bad == null) FtResult("codec", "Codec", name, FtLevel.PASS, FtKind.OK, files = 1, ms = (System.nanoTime() - t0) / 1_000_000)
                else FtResult("codec", "Codec", name, FtLevel.FAIL, FtKind.CONTENT, bad, ms = (System.nanoTime() - t0) / 1_000_000)
            } catch (e: Throwable) {
                FtResult("codec", "Codec", name, FtLevel.FAIL, FtKind.ERROR, short((e.message ?: "").ifBlank { e.javaClass.simpleName }))
            }
            r
        }
        fun lzma(hex: String, raw: ByteArray): String? {
            val d = unhex(hex)
            return if (Lzma.decode(d, 0, d.size, raw.size).contentEquals(raw)) null else "LZMA"
        }
        fun lz4(hex: String, raw: ByteArray): String? {
            val d = unhex(hex)
            return if (Lz4.decode(d, 0, d.size, raw.size).contentEquals(raw)) null else "LZ4"
        }
        return listOf(
            task("LZMA (text + bytes)") { lzma(V_LZMA1, raw1()) },
            task("LZMA (2000 bytes)") { lzma(V_LZMA2, raw2()) },
            task("LZ4 (short matches)") { lz4(V_LZ4A, raw1()) },
            task("LZ4 (long runs, length extension)") { lz4(V_LZ4B, raw3()) },
            task("LZ4 (corrupt data is rejected)") {
                val d = unhex(V_LZ4B)
                d[d.size - 1] = (d[d.size - 1].toInt() xor 0x55).toByte()
                try { Lz4.decode(d, 0, d.size - 3, 1743); "LZ4 accepted corrupt data" } catch (e: java.io.IOException) { null }
            },
            task("LZSS (Eushully index, real data)") {
                val d = unhex(V_LZSS_IN)
                if (com.furuarc.formats.AgeFormat.lzss(d, 0, d.size).contentEquals(unhex(V_LZSS_OUT))) null else "LZSS"
            },
            task("LZSS (literal-only writer, round trip)") {
                val raw = raw2()
                if (com.furuarc.formats.AgeFormat.lzss(com.furuarc.formats.AgeFormat.lzssLiteral(raw), 0, 1 shl 20).contentEquals(raw)) null else "LZSS"
            },
        )
    }

    // ------------------------------------------------------------------ script text

    private fun scriptTests(): List<Pair<String, () -> FtResult>> {
        fun task(kind: ScriptKind, sample: String): Pair<String, () -> FtResult> = kind.id to {
            val name = kind.id
            val t0 = System.nanoTime()
            fun res(level: FtLevel, k: FtKind, arg: String = "", n: Int = 0) =
                FtResult("script", "Script", name, level, k, arg, files = n, ms = (System.nanoTime() - t0) / 1_000_000)
            try {
                val bytes = sample.toByteArray(Charsets.UTF_8)
                val units = ScriptText.extractFile(kind, "t", bytes, "utf8")
                if (units.isEmpty()) res(FtLevel.WARN, FtKind.SCRIPT_EMPTY)
                else {
                    val same = units.associate { it.path to it.text }
                    val r0 = ScriptText.applyFile(kind, bytes, same, "utf8")
                    if (!r0.first.contentEquals(bytes)) res(FtLevel.FAIL, FtKind.SCRIPT_CHANGED)
                    else {
                        val changed = units.associate { it.path to "<<" + it.text + ">>" }
                        val r1 = ScriptText.applyFile(kind, bytes, changed, "utf8")
                        val back = ScriptText.extractFile(kind, "t", r1.first, "utf8")
                        if (r1.second <= 0) res(FtLevel.FAIL, FtKind.SCRIPT_APPLY, "0 / ${units.size}")
                        else if (back.size != units.size) res(FtLevel.FAIL, FtKind.SCRIPT_APPLY, "${back.size} / ${units.size}")
                        else if (back.any { !it.text.contains("<<") }) res(FtLevel.FAIL, FtKind.SCRIPT_APPLY, back.first { !it.text.contains("<<") }.text)
                        else res(FtLevel.PASS, FtKind.OK, n = units.size)
                    }
                }
            } catch (e: Throwable) {
                res(FtLevel.FAIL, FtKind.ERROR, short((e.message ?: "").ifBlank { e.javaClass.simpleName }))
            }
        }
        return listOf(
            task(ScriptKind.RPY, "label start:\n    e \"Hello there.\"\n    \"A narrated line.\"\n"),
            task(ScriptKind.KAG, "*start\nこんにちは、世界。[p]\n次の行です。[p]\n"),
            task(ScriptKind.TYRANO, "*start\n#taro\nこんにちは、世界。[p]\n次の行です。[p]\n"),
            task(ScriptKind.NSCRIPT, "*start\nこんにちは、世界。\\\n次の行です。\\\n"),
        )
    }
}
