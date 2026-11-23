package com.furuarc

import android.os.Build
import com.furuarc.core.*
import com.furuarc.i18n.checkStr
import com.furuarc.settings.Lang
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** State of the Format Test screen. [finished] = a complete run; [stopped] = the user stopped it. */
data class FormatTestState(
    val running: Boolean = false,
    val done: Int = 0,
    val total: Int = 0,
    val label: String = "",
    val results: List<FtResult> = emptyList(),
    val finished: Boolean = false,
    val stopped: Boolean = false,
    val error: String? = null,
)

/** Runs [FormatSelfTest] in the background and keeps the results for the screen and the report. */
/** Real (golden) archives shipped as Android assets in assets/formattest/. */
private class AssetGolden : FtGoldenSource {
    private val am get() = Engine.app.assets
    override fun names(): List<String> = try { am.list("formattest")?.filter { it.endsWith(".golden") } ?: emptyList() } catch (e: Exception) { emptyList() }
    override fun read(name: String): ByteArray? = try { am.open("formattest/$name").use { it.readBytes() } } catch (e: Exception) { null }
}

object FormatTestJobs {
    private val _state = MutableStateFlow(FormatTestState())
    val state: StateFlow<FormatTestState> = _state.asStateFlow()

    @Volatile private var stop = false
    @Volatile private var run = 0
    private var job: Job? = null

    /** Leave the screen: stop a running test and forget the results. */
    fun clear() {
        stop = true; run++
        job?.cancel()
        _state.value = FormatTestState()
    }

    fun stopRun() { stop = true }

    fun start() {
        if (_state.value.running) return
        stop = false
        val me = ++run
        _state.value = FormatTestState(running = true, total = FormatSelfTest.totalCount(AssetGolden()))
        job = Engine.scope.launch(Dispatchers.IO) {
            try {
                FormatSelfTest.run(
                    Engine.app.cacheDir,
                    isCancelled = { stop || me != run },
                    progress = { d, n, label -> if (me == run) _state.update { it.copy(done = d, total = n, label = label) } },
                    onResult = { r -> if (me == run) _state.update { it.copy(results = it.results + r, done = it.results.size + 1) } },
                    golden = AssetGolden(),
                )
                if (me == run) _state.update { it.copy(running = false, finished = !stop, stopped = stop, label = "") }
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
        if (st.results.isEmpty()) return ""
        val lang = Engine.lang
        val id = lang == Lang.ID
        val c = checkStr(lang)
        val sb = ReportText.header(c.ftReportTitle, id)
        val version = try { Engine.app.packageManager.getPackageInfo(Engine.app.packageName, 0).versionName ?: "?" } catch (e: Exception) { "?" }
        sb.append(if (id) "Aplikasi: FuruArc " else "App: FuruArc ").append(version).append('\n')
        sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n")
        sb.append(if (id) "Perangkat: " else "Device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n')
        val pass = st.results.count { it.level == FtLevel.PASS }
        val warn = st.results.count { it.level == FtLevel.WARN }
        val fail = st.results.count { it.level == FtLevel.FAIL }
        val skip = st.results.count { it.level == FtLevel.SKIP }
        sb.append('\n').append(c.ftSummary(pass, warn, fail, skip)).append('\n')
        if (st.stopped) sb.append(c.ftStopped).append('\n')
        ReportText.section(sb, c.ftTitle)
        for (r in st.results) {
            val mark = when (r.level) { FtLevel.PASS -> 1; FtLevel.WARN -> 2; FtLevel.FAIL -> 3; FtLevel.SKIP -> 0 }
            sb.append(ReportText.mark(mark)).append(' ').append(r.engine)
            if (r.variant.isNotEmpty()) sb.append(" · ").append(r.variant)
            sb.append(" - ").append(c.ftDetail(r)).append('\n')
        }
        return sb.toString()
    }
}
