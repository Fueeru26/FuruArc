package com.furuarc

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.furuarc.core.ConflictPolicy
import com.furuarc.settings.AppSettings
import com.furuarc.settings.SettingsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.BitSet

/**
 * Only display settings + the entry point to [Engine]. The unpack/pack process deliberately does not live in the
 * ViewModel so it keeps running when the Activity is closed (guarded by [ProcessService]).
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val store = SettingsStore(app)
    private val _settings = MutableStateFlow(store.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        Engine.init(app)
        applyToEngine(_settings.value)
    }

    private fun applyToEngine(n: AppSettings) {
        Engine.lang = n.lang
        Engine.bgEnabled = n.bgProcess
        Engine.setDefaultOutput(n.defaultOutUri)
    }

    /** Called from the Save button on the Settings screen. */
    fun saveSettings(n: AppSettings) {
        _settings.value = n
        store.save(n)
        applyToEngine(n)
    }

    // last-entered preferences (format, file name, filter, etc.)
    fun pref(key: String, def: String): String = store.getStr(key, def)
    fun setPref(key: String, value: String) = store.putStr(key, value)

    // ---------- process ----------
    val state: StateFlow<UiState> get() = Engine.state

    fun mode(): Int = Engine.state.value.mode
    fun engineId(): String? = Engine.state.value.engineId
    fun setMode(m: Int, engine: String? = null) = Engine.setMode(m, engine)
    fun isRunning(): Boolean = Engine.isRunning()
    fun reset() = Engine.reset()
    fun cancel() = Engine.cancel()
    fun resultFolder(): Uri? = Engine.resultFolder()

    fun setArchive(uri: Uri, expectedId: String?) = Engine.setArchive(uri, expectedId)
    fun setArchives(uris: List<Uri>, expectedId: String?) = Engine.setArchives(uris, expectedId)
    fun setOutputTree(uri: Uri) = Engine.setOutputTree(uri)
    fun setPackFolder(uri: Uri) = Engine.setPackFolder(uri)
    fun setPackOutput(uri: Uri) = Engine.setPackOutput(uri)
    fun setIgnore(text: String) = Engine.setIgnore(text)

    fun entryNames(): List<String> = Engine.entryNames()
    fun selectionCopy(): BitSet? = Engine.selectionCopy()
    fun clearSelection() = Engine.clearSelection()
    fun applySelection(sel: BitSet) = Engine.applySelection(sel)

    fun canUnpack() = Engine.canUnpack()
    fun unpack(xorKey: Int?) =
        Engine.unpack(xorKey, ConflictPolicy.values()[_settings.value.conflict.coerceIn(0, 2)])
    fun canPack() = Engine.canPack()
    fun pack(formatId: String, fileName: String, extra: Map<String, String>) =
        Engine.pack(formatId, fileName, extra, _settings.value.ignore)
    fun setUnpackExtra(extra: Map<String, String>) = Engine.setUnpackExtra(extra)

    fun verify() = Engine.verify()
    fun verifyPack() = Engine.verifyPack()
    fun loadPreview(index: Int, maxBytes: Long): PreviewData = Engine.loadPreview(index, maxBytes)

    // ---------- RPGM Tools ----------
    private fun policy() = ConflictPolicy.values()[_settings.value.conflict.coerceIn(0, 2)]
    fun rpgmInputFolder(uri: Uri, recursive: Boolean = true) = RpgmJobs.setInputFolder(uri, recursive)
    fun rpgmRescanFolder(recursive: Boolean) = RpgmJobs.rescanFolder(recursive)
    fun rpgmInputFiles(uris: List<Uri>) = RpgmJobs.setInputFiles(uris)
    fun rpgmInputFile(uri: Uri) = RpgmJobs.setInputFile(uri)
    fun exportedFile(): Uri? = Engine.exportUri
    fun canRunAssets() = RpgmJobs.canRunAssets()
    fun canRunText(needFile: Boolean) = RpgmJobs.canRunText(needFile)
    fun canRunSave() = RpgmJobs.canRunSave()
    fun runAssets(encrypt: Boolean, key: String) = RpgmJobs.assets(encrypt, key, policy())
    fun setExportBase(n: String) { Engine.exportBase = n }
    fun translateExport(fmt: Int) = RpgmJobs.translateExport(fmt, policy())
    fun translateImport() = RpgmJobs.translateImport(policy())
    fun saveExport() = RpgmJobs.saveExport(policy())
    fun rpgmSystemJson(uri: Uri) = RpgmJobs.setSystemJson(uri)
    fun canRunTrEdit() = RpgmJobs.canRunTrEdit()
    fun saveTranslation(texts: Map<Int, String>, draft: Boolean) = RpgmJobs.saveTranslation(texts, draft, policy())
    fun autoSaveDraft(texts: Map<Int, String>, onDone: (Boolean) -> Unit) = RpgmJobs.autoSaveDraft(texts, onDone)
    fun canRunEdit() = RpgmJobs.canRunEdit()
    fun applyEdits(edits: Map<String, String>, backup: Boolean) = RpgmJobs.applyEdits(edits, policy(), backup)
    fun rpgmNamesFolder(uri: Uri) = RpgmJobs.setNamesFolder(uri)
    fun previewAsset(key: String, next: Boolean) = RpgmJobs.previewAsset(key, next)
    fun saveImport(kind: Int, backup: Boolean) = RpgmJobs.saveImport(kind, policy(), backup)

    // ---------- Per-engine Script Tools ----------
    fun canRunTool(tool: com.furuarc.core.ScriptTool) = ScriptJobs.canRun(tool)
    fun runTool(tool: com.furuarc.core.ScriptTool, engineId: String?, enc: String, fmt: Int) =
        ScriptJobs.run(tool, engineId, enc, fmt, policy())
}
