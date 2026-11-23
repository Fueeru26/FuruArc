package com.furuarc.core

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

/**
 * The real-archive Format Test on archives that the app packs itself: for every format that can be packed, the file must be
 * accepted by [FormatRealTest.check] and the test must not fail (warnings are allowed).
 */
class FormatRealTestTest {
    @Test
    fun everyPackableFormatPassesTheRealArchiveTest() {
        val work = Files.createTempDirectory("furuarc-rt").toFile()
        val bad = ArrayList<String>()
        try {
            val files = FormatSelfTest.sampleFiles()
            val seen = HashSet<String>()
            for (c in FormatSelfTest.cases()) {
                if (c.skip != null || c.sample != "generic" || !seen.add(c.formatId)) continue
                val fmt = FormatRegistry.byId(c.formatId)
                val arc = File(work, "t." + (fmt.packExtension(c.extra) ?: fmt.extensions.first()))
                val inputs = files.map { (n, b) -> PackInput(n, b.size.toLong()) { ByteArrayInputStream(b) } }
                fmt.pack(inputs, arc, Options(xorKey = c.extra["xor"]?.toIntOrNull(), compress = c.extra["compress"]?.let { it == "1" } ?: true, extra = c.extra)) { _, _, _ -> }
                FileSource(arc).use { s ->
                    val ck = FormatRealTest.check(s, arc.name, fmt, c.unpackExtra)
                    if (ck.entries <= 0) { bad.add("${c.formatId}: not accepted (${ck.error})"); return@use }
                    val lines = FormatRealTest.run(s, arc.name, fmt, c.unpackExtra, c.unpackKey, c.extra, 1_000_000, work, Random(1))
                    lines.filter { it.level == FtLevel.FAIL }.forEach { bad.add("${c.formatId}: ${it.kind} ${it.arg}") }
                }
            }
            // a file of another format must be refused
            val junk = File(work, "junk.rpa").also { it.writeBytes(ByteArray(2000) { i -> (i * 7).toByte() }) }
            FileSource(junk).use { s -> if (FormatRealTest.check(s, junk.name, FormatRegistry.byId("xp3"), emptyMap()).entries > 0) bad.add("junk accepted") }
        } finally {
            work.deleteRecursively()
        }
        assertTrue("Real archive test failures:\n" + bad.joinToString("\n"), bad.isEmpty())
    }
}
