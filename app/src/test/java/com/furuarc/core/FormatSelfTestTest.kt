package com.furuarc.core

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

/**
 * Runs the same Format Test as the app (round trips, edge samples, real archives from assets/formattest, codecs and
 * script text) on the JVM, so CI catches a broken format before a release. Warnings (for example an edge sample that a
 * format cannot store) do not fail the build; failures do.
 */
class FormatSelfTestTest {
    private class DirGolden(private val dir: File) : FtGoldenSource {
        override fun names(): List<String> = dir.listFiles()?.map { it.name }?.filter { it.endsWith(".golden") } ?: emptyList()
        override fun read(name: String): ByteArray? = File(dir, name).takeIf { it.isFile }?.readBytes()
    }

    @Test
    fun formatTestHasNoFailures() {
        val work = Files.createTempDirectory("furuarc-ft").toFile()
        try {
            val golden = DirGolden(File("src/main/assets/formattest"))
            val results = FormatSelfTest.run(work, golden = golden)
            assertTrue("no tests ran", results.isNotEmpty())
            val bad = results.filter { it.level == FtLevel.FAIL }
            val warn = results.count { it.level == FtLevel.WARN }
            println("Format Test: ${results.count { it.level == FtLevel.PASS }} passed, $warn warnings, ${bad.size} failed")
            for (r in results.filter { it.level == FtLevel.WARN }) println("WARN  ${r.engine} ${r.variant}: ${r.kind} ${r.arg}")
            assertTrue(
                "Format Test failures:\n" + bad.joinToString("\n") { "  ${it.engine} ${it.variant}: ${it.kind} ${it.arg}" },
                bad.isEmpty(),
            )
        } finally {
            work.deleteRecursively()
        }
    }
}
