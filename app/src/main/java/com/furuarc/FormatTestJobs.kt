package com.furuarc

import android.net.Uri
import android.os.Build
import com.furuarc.core.*
import com.furuarc.i18n.checkStr
import com.furuarc.settings.Lang
import com.furuarc.storage.ChannelSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/** Is the chosen file an archive of the chosen format? [bad] = no: the screen shows a red error ([foundName] = what it is). */
data class FtCheckState(
    val busy: Boolean = false,
    val entries: Int = 0,
    val bad: Boolean = false,
    val foundName: String? = null,
)

/** State of the Format Test screen. [finished] = the run ended by itself; [stopped] = the user stopped it. */
data class FormatTestState(
    val check: FtCheckState = FtCheckState(),
    val running: Boolean = false,
    val lines: List<RtLine> = emptyList(),
    val finished: Boolean = false,
    val stopped: Boolean = false,
    val error: String? = null,
    val formatName: String = "",
    val archiveName: String = "",
)

/**
 * Format Test with a real archive: checks the file against the chosen format, then runs [FormatRealTest] in the background
 * and keeps the lines for the screen and the report.
 */
object FormatTestJobs {
    private val _state = MutableStateFlow(FormatTestState())
    val state: StateFlow<FormatTestState> = _state.asStateFlow()

    @Volatile private var stop = false
    @Volatile private var run = 0
    private var job: Job? = null
    private var checkJob: Job? = null

    /** Leave the screen: stop everything and forget the results. */
    fun clear() {
        stop = true; run++
        job?.cancel(); checkJob?.cancel()
        _state.value = FormatTestState()
    }

    fun stopRun() { stop = true }

    /** Forget the check (no file or no engine chosen yet). */
    fun resetCheck() {
        checkJob?.cancel()
        _state.update { it.copy(check = FtCheckState()) }
    }

    /** Check that [uri] is an archive of [formatId]; the result goes to [FormatTestState.check]. */
    fun check(uri: Uri, name: String, formatId: String, unpackExtra: Map<String, String>) {
        checkJob?.cancel()
        _state.update { it.copy(check = FtCheckState(busy = true), lines = emptyList(), finished = false, stopped = false, error = null) }
        checkJob = Engine.scope.launch(Dispatchers.IO) {
            try {
                val pfd = Engine.app.contentResolver.openFileDescriptor(uri, "r") ?: error("open")
                val r = ChannelSource(pfd).use { src -> FormatRealTest.check(src, name, FormatRegistry.byId(formatId), unpackExtra) }
                _state.update { it.copy(check = FtCheckState(entries = r.entries, bad = r.entries <= 0, foundName = r.foundName)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                _state.update { it.copy(check = FtCheckState(bad = true)) }
            }
        }
    }

    fun start(
        uri: Uri, archiveName: String, formatId: String, unpackExtra: Map<String, String>, unpackKey: Int?,
        packExtra: Map<String, String>, maxBytes: Long,
    ) {
        if (_state.value.running) return
        stop = false
        val me = ++run
        val fmt = FormatRegistry.byId(formatId)
        _state.update { it.copy(running = true, lines = emptyList(), finished = false, stopped = false, error = null, formatName = fmt.displayName, archiveName = archiveName) }
        job = Engine.scope.launch(Dispatchers.IO) {
            try {
                val pfd = Engine.app.contentResolver.openFileDescriptor(uri, "r") ?: error("open")
                ChannelSource(pfd).use { src ->
                    FormatRealTest.run(
                        src, archiveName, fmt, unpackExtra, unpackKey, packExtra, maxBytes,
                        File(Engine.app.cacheDir, "formattest"),
                        isCancelled = { stop || me != run },
                        onLine = { l -> if (me == run) _state.update { it.copy(lines = it.lines + l) } },
                    )
                }
                if (me == run) _state.update { it.copy(running = false, finished = !stop, stopped = stop) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                if (me == run) _state.update { it.copy(running = false, error = e.message ?: e.javaClass.simpleName) }
            }
        }
    }

    /** Text report (copy / export .txt); includes app and device info so it can be pasted into a bug report. */
    fun reportText(): String {
        val st = _state.value
        if (st.lines.isEmpty()) return ""
        val lang = Engine.lang
        val id = lang == Lang.ID
        val c = checkStr(lang)
        val sb = ReportText.header(c.ftReportTitle, id)
        val version = try { Engine.app.packageManager.getPackageInfo(Engine.app.packageName, 0).versionName ?: "?" } catch (e: Exception) { "?" }
        sb.append(if (id) "Aplikasi: FuruArc " else "App: FuruArc ").append(version).append('\n')
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
        sb.append(if (id) "Perangkat: " else "Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n')
        sb.append(if (id) "Format: " else "Format: ").append(st.formatName).append('\n')
        sb.append(if (id) "Arsip: " else "Archive: ").append(st.archiveName).append('\n')
        if (st.stopped) sb.append(c.ftStopped).append('\n')
        ReportText.section(sb, c.ftResults)
        for (l in st.lines) {
            val mark = when (l.level) { FtLevel.PASS -> 1; FtLevel.WARN -> 2; FtLevel.FAIL -> 3; FtLevel.SKIP -> 0 }
            sb.append(ReportText.mark(mark)).append(' ').append(c.rtDetail(l)).append('\n')
        }
        return sb.toString()
    }
}
