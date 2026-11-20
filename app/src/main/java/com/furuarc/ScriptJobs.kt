package com.furuarc

import com.furuarc.core.*
import com.furuarc.i18n.ScriptStrings
import com.furuarc.storage.TreeWriter
import kotlinx.coroutines.ensureActive
import java.io.IOException

/**
 * Script Tools jobs per engine (modes 20..25): script text export/import, rpyc -> rpy, nscript.dat decode/encode,
 * archive table of contents. Uses the state/progress/log of [Engine] and the folder scanning of [RpgmJobs].
 */
object ScriptJobs {
    private val str get() = ScriptStrings.of(Engine.lang)

    /** Number and size of files relevant to mode [mode] (used when scanning the input folder). */
    fun count(engineId: String?, mode: Int, list: List<PackInput>): Pair<Int, Long> {
        val kind = ScriptTools.kindFor(engineId)
        val auto = engineId == ScriptTools.AUTO
        var n = 0
        var b = 0L
        for (f in list) {
            val ok = when (mode) {
                20, 21 -> if (auto) detectOf(f) != null else kind != null && ScriptText.isScriptFile(kind, f.path)
                22 -> ScriptTools.isRpyc(f.path)
                23 -> ScriptTools.isNsDat(f.path)
                24 -> ScriptTools.isNsText(f.path)
                else -> false
            }
            if (ok) { n++; b += f.size }
        }
        return Pair(n, b)
    }

    /** Script type of one file for auto-detect mode (reads the start of the file). */
    private fun detectOf(f: PackInput): ScriptKind? {
        if (!ScriptText.maybeScript(f.path)) return null
        return try {
            val head = f.open().use { ins -> val b = ByteArray(65536); val r = IoUtil.fill(ins, b); b.copyOf(r) }
            ScriptText.detectKind(f.path, head)
        } catch (e: Exception) {
            null
        }
    }

    /** Script files to process along with their type. [kind] null = auto-detect per file. */
    private fun pick(kind: ScriptKind?): List<Pair<PackInput, ScriptKind>> =
        RpgmJobs.scannedFiles().mapNotNull { f ->
            if (kind != null) (if (ScriptText.isScriptFile(kind, f.path)) f to kind else null)
            else detectOf(f)?.let { k -> f to k }
        }

    fun canRun(tool: ScriptTool): Boolean = when (tool) {
        ScriptTool.IMPORT -> RpgmJobs.canRunText(true)
        ScriptTool.LIST -> {
            val s = Engine._state.value
            !s.running && s.outName != null && s.entryCount > 0 && s.archiveError == null
        }
        else -> RpgmJobs.canRunText(false)
    }

    fun run(tool: ScriptTool, engineId: String?, enc: String, fmt: Int, policy: ConflictPolicy) {
        val kind = ScriptTools.kindFor(engineId)
        when (tool) {
            ScriptTool.EXPORT -> if (kind != null || engineId == ScriptTools.AUTO) exportText(kind, enc, fmt, policy)
            ScriptTool.IMPORT -> if (kind != null || engineId == ScriptTools.AUTO) importText(kind, enc, policy)
            ScriptTool.RPYC -> decompile(policy)
            ScriptTool.NS_DECODE -> nsDecode(policy)
            ScriptTool.NS_ENCODE -> nsEncode(policy)
            ScriptTool.LIST -> listArchive(policy)
        }
    }

    private fun key(path: String) = path.replace('\\', '/')

    private fun finish(writer: TreeWriter, out: android.net.Uri, ok: Int, failed: Int, skipped: Int, bytes: Long, t0: Long) {
        val sec = (System.currentTimeMillis() - t0) / 1000.0
        Engine.resultUri = writer.dirUri(emptyList()) ?: Engine.treeDocUri(out)
        Engine.finishJob(Summary(ok, failed, skipped, bytes, sec, false))
    }

    // ---------- text export ----------

    private fun exportText(kind: ScriptKind?, enc: String, fmt: Int, policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val files = pick(kind)
        if (files.isEmpty()) return
        RpgmJobs.upd { it.copy(error = null) }
        Engine.exportUri = null
        val total = files.sumOf { it.first.size }
        Engine.launchJob(total) {
            val t0 = System.currentTimeMillis()
            val units = ArrayList<TextUnit>()
            var failed = 0
            var done = 0L
            for ((i, fk) in files.withIndex()) {
                val f = fk.first
                ensureActive()
                try {
                    units.addAll(ScriptText.extractFile(fk.second, key(f.path), f.open().use { it.readBytes() }, enc, fmt == RpgmText.FMT_VNTP))
                } catch (ex: kotlinx.coroutines.CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    failed++
                    Engine.log(Engine.str.logFailed(f.path, ex.message ?: ""))
                }
                done += f.size
                Engine.report(i + 1, files.size, done, total, f.path, false)
            }
            if (units.isEmpty()) throw IOException(str.noText)
            val data = RpgmText.export(units, fmt).toByteArray(Charsets.UTF_8)
            val writer = TreeWriter(RpgmJobs.app, out)
            val name = Engine.exportBase.trim().ifEmpty { "translate" } + "." + RpgmText.extFor(fmt)
            val of = writer.open(name, policy)
            of?.stream?.use { it.write(data) }
            val wrote = of != null
            Engine.exportUri = of?.uri
            Engine.report(files.size, files.size, total, total, "", true)
            Engine.log(str.logExported(units.size, name))
            finish(writer, out, units.size, failed, if (wrote) 0 else 1, data.size.toLong(), t0)
        }
    }

    // ---------- text import ----------

    private fun importText(kind: ScriptKind?, enc: String, policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val tf = RpgmJobs.inputFile() ?: return
        val files = pick(kind)
        if (files.isEmpty()) return
        RpgmJobs.upd { it.copy(error = null) }
        val total = files.sumOf { it.first.size }
        Engine.launchJob(total) {
            val t0 = System.currentTimeMillis()
            val text = RpgmJobs.readText(tf)
            val name = Engine._state.value.rpgm.fileName ?: ""
            val all = RpgmText.readAny(name, text).map
            if (all.isEmpty()) throw IOException(str.emptyTr)
            val byFile = HashMap<String, HashMap<String, String>>()
            for ((k, v) in all) byFile.getOrPut(k.first) { HashMap() }[k.second] = v

            fun find(path: String): HashMap<String, String>? {
                val p = key(path)
                byFile[p]?.let { return it }
                val hits = byFile.keys.filter { p.endsWith("/$it") || it.endsWith("/$p") }
                if (hits.size == 1) return byFile[hits[0]]
                val base = p.substringAfterLast('/')
                val same = byFile.keys.filter { it.substringAfterLast('/') == base }
                return if (same.size == 1) byFile[same[0]] else null
            }

            val writer = TreeWriter(RpgmJobs.app, out)
            var applied = 0
            var written = 0
            var failed = 0
            var skipped = 0
            var stale = 0
            var bytes = 0L
            var done = 0L
            for ((i, fk) in files.withIndex()) {
                val f = fk.first
                val kind = fk.second
                ensureActive()
                val tr = find(f.path)
                if (tr != null) {
                    try {
                        val raw = f.open().use { it.readBytes() }
                        val keys = ScriptText.extractFile(kind, "", raw, enc).map { it.path }.toSet()
                        stale += tr.keys.count { it !in keys }
                        val r = ScriptText.applyFile(kind, raw, tr, enc)
                        if (r.second > 0) {
                            val data = r.first
                            if (RpgmJobs.writeBytes(writer, f.path, data, policy)) {
                                written++; applied += r.second; bytes += data.size
                                Engine.log(str.logFile(f.path, r.second))
                            } else skipped++
                        }
                    } catch (ex: kotlinx.coroutines.CancellationException) {
                        throw ex
                    } catch (ex: Exception) {
                        failed++
                        Engine.log(Engine.str.logFailed(f.path, ex.message ?: ""))
                    }
                }
                done += f.size
                Engine.report(i + 1, files.size, done, total, f.path, false)
            }
            Engine.report(files.size, files.size, total, total, "", true)
            if (applied == 0 && failed == 0) throw IOException(str.noMatch)
            Engine.log(str.logImported(applied, written, stale))
            finish(writer, out, applied, failed, skipped, bytes, t0)
        }
    }

    // ---------- rpyc -> rpy ----------

    private fun decompile(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val files = RpgmJobs.scannedFiles().filter { ScriptTools.isRpyc(it.path) }
        if (files.isEmpty()) return
        RpgmJobs.upd { it.copy(error = null) }
        val total = files.sumOf { it.size }
        Engine.launchJob(total) {
            val t0 = System.currentTimeMillis()
            val writer = TreeWriter(RpgmJobs.app, out)
            var ok = 0
            var failed = 0
            var skipped = 0
            var bytes = 0L
            var done = 0L
            for ((i, f) in files.withIndex()) {
                ensureActive()
                try {
                    val text = Rpyc.decompile(f.open().use { it.readBytes() })
                    val data = text.toByteArray(Charsets.UTF_8)
                    val op = f.path.dropLast(1) // .rpyc -> .rpy, .rpymc -> .rpym
                    if (RpgmJobs.writeBytes(writer, op, data, policy)) {
                        ok++; bytes += data.size
                        Engine.log(str.logDecompiled(f.path))
                    } else skipped++
                } catch (ex: kotlinx.coroutines.CancellationException) {
                    throw ex
                } catch (ex: Throwable) {
                    failed++
                    Engine.log(Engine.str.logFailed(f.path, ex.message ?: ex.javaClass.simpleName))
                }
                done += f.size
                Engine.report(i + 1, files.size, done, total, f.path, false)
            }
            Engine.report(files.size, files.size, total, total, "", true)
            if (ok == 0 && failed > 0) throw IOException(str.logUnsupportedNote(failed))
            finish(writer, out, ok, failed, skipped, bytes, t0)
        }
    }

    // ---------- nscript.dat ----------

    private fun nsDecode(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val files = RpgmJobs.scannedFiles().filter { ScriptTools.isNsDat(it.path) }
        if (files.isEmpty()) return
        val total = files.sumOf { it.size }
        Engine.launchJob(total) {
            val t0 = System.currentTimeMillis()
            val writer = TreeWriter(RpgmJobs.app, out)
            var ok = 0
            var failed = 0
            var skipped = 0
            var bytes = 0L
            for ((i, f) in files.withIndex()) {
                ensureActive()
                try {
                    val raw = f.open().use { it.readBytes() }
                    val dec = ScriptText.nsXor(raw)
                    if (!ScriptText.nsLooksDecoded(dec)) {
                        throw IOException(if (ScriptText.nsLooksDecoded(raw)) str.nsAlreadyText else "XOR 0x84")
                    }
                    val dir = key(f.path).substringBeforeLast('/', "")
                    val base = f.path.substringAfterLast('/').substringAfterLast('\\')
                    val outName = if (base.equals("nscript.dat", true)) "0.txt" else "$base.txt"
                    val op = if (dir.isEmpty()) outName else "$dir/$outName"
                    if (RpgmJobs.writeBytes(writer, op, dec, policy)) {
                        ok++; bytes += dec.size
                        Engine.log(str.logDecoded(f.path, op))
                    } else skipped++
                } catch (ex: kotlinx.coroutines.CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    failed++
                    Engine.log(Engine.str.logFailed(f.path, ex.message ?: ""))
                }
                Engine.report(i + 1, files.size, 0, total, f.path, false)
            }
            Engine.report(files.size, files.size, total, total, "", true)
            if (ok == 0 && failed > 0) throw IOException(str.nsAlreadyText)
            finish(writer, out, ok, failed, skipped, bytes, t0)
        }
    }

    private fun nsEncode(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val cand = RpgmJobs.scannedFiles().filter { ScriptTools.isNsText(it.path) }
        val f = cand.sortedWith(compareBy({ !it.path.substringAfterLast('/').equals("0.txt", true) }, { it.path.length })).firstOrNull() ?: return
        Engine.launchJob(f.size) {
            val t0 = System.currentTimeMillis()
            val writer = TreeWriter(RpgmJobs.app, out)
            val data = ScriptText.nsXor(f.open().use { it.readBytes() })
            val dir = key(f.path).substringBeforeLast('/', "")
            val op = if (dir.isEmpty()) "nscript.dat" else "$dir/nscript.dat"
            val wrote = RpgmJobs.writeBytes(writer, op, data, policy)
            Engine.report(1, 1, f.size, f.size, "", true)
            Engine.log(str.logEncoded(f.path, op))
            finish(writer, out, if (wrote) 1 else 0, 0, if (wrote) 0 else 1, data.size.toLong(), t0)
        }
    }

    // ---------- archive table of contents ----------

    private fun listArchive(policy: ConflictPolicy) {
        val out = Engine.outTree ?: return
        val entries = Engine.entryList()
        if (entries.isEmpty()) return
        val aname = Engine._state.value.archiveName ?: "archive"
        Engine.launchJob(0) {
            val t0 = System.currentTimeMillis()
            val sb = StringBuilder()
            for (e in entries) sb.append(e.size).append('\t').append(e.name).append('\n')
            val data = sb.toString().toByteArray(Charsets.UTF_8)
            val writer = TreeWriter(RpgmJobs.app, out)
            val name = "$aname.txt"
            val wrote = RpgmJobs.writeBytes(writer, name, data, policy)
            Engine.report(1, 1, 0, 0, "", true)
            Engine.log(str.logListed(entries.size, name))
            finish(writer, out, if (wrote) entries.size else 0, 0, if (wrote) 0 else 1, data.size.toLong(), t0)
        }
    }
}
