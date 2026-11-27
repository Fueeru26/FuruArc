package com.furuarc.core

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.Engine
import org.json.JSONArray
import org.json.JSONObject

/** One Translation Project: a name, a note and the state of each step of the workflow. */
class Project(val id: String, name: String, val created: Long, note: String = "", val type: Int = 0, mech: Int = Projects.MECH_VN) {
    var name by mutableStateOf(name)
    var note by mutableStateOf(note)
    /** Mechanism of the game ([Projects.MECH_MV], [Projects.MECH_XP], [Projects.MECH_VN]); decides the steps. Step states are kept per step number, so switching keeps shared steps. */
    var mech by mutableStateOf(mech)
    /** Source archive and work folder of the project (document / tree URIs with a persisted permission; empty = not chosen). */
    var sourceUri by mutableStateOf("")
    var sourceName by mutableStateOf("")
    var workUri by mutableStateOf("")
    var workName by mutableStateOf("")
    /** Step number -> state ([Projects.ST_NONE], [Projects.ST_OPEN], [Projects.ST_DONE]). */
    val status = mutableStateMapOf<Int, Int>()

    fun state(step: Int) = status[step] ?: Projects.ST_NONE
    val steps get() = Projects.stepsOf(type, mech)
    fun doneCount() = steps.count { state(it) == Projects.ST_DONE }
}

/**
 * Projects (Create Project menu). A project only remembers where the user is in a workflow;
 * each step opens the existing tool of the app. Stored as JSON in the app preferences.
 */
object Projects {
    const val ST_NONE = 0
    const val ST_OPEN = 1
    const val ST_DONE = 2

    // Steps of the Translation Project, in order
    const val UNPACK = 1
    const val EXPORT = 2
    const val TRANSLATE = 3
    const val IMPORT = 4
    const val PACK = 5
    const val TEST = 6
    // extra steps of the Compress Game project
    const val SCAN = 7
    const val AUDIO = 8
    const val IMAGE = 9
    const val VIDEO = 10
    // extra steps of the Extract Assets project
    const val VIEW = 11
    const val CONVERT = 12
    const val DUPES = 13
    const val RENAME = 14
    // extra steps of the Make Patch project
    const val EDIT = 15
    const val COMPARE = 16
    // extra step of the Audit Game project
    const val TRCHECK = 17
    // extra step of the Clean Up project
    const val NAMES = 18
    // extra step of the Localize Assets project
    const val IMGEDIT = 19
    // backup step of the Clean Up project
    const val BACKUP = 21
    // RPG Maker MV / MZ steps (no archive to unpack: assets are decrypted / encrypted per file)
    const val DECRYPT = 22
    const val ENCRYPT = 23
    const val PLUGINS = 24

    const val MECH_MV = 0
    const val MECH_XP = 1
    const val MECH_VN = 2

    // RPG Maker MV / MZ step lists
    private val MV_TRANSLATION = listOf(EXPORT, TRANSLATE, IMPORT, PLUGINS, COMPARE)
    private val MV_COMPRESS = listOf(DECRYPT, SCAN, AUDIO, IMAGE, VIDEO, ENCRYPT, COMPARE)
    private val MV_EXTRACT = listOf(DECRYPT, SCAN, VIEW, CONVERT, DUPES, RENAME)
    private val MV_PATCH = listOf(DECRYPT, EDIT, PLUGINS, ENCRYPT, COMPARE, TEST)
    private val MV_AUDIT = listOf(SCAN, TRCHECK, DUPES, TEST)
    private val MV_CLEAN = listOf(BACKUP, SCAN, DUPES, NAMES, CONVERT)
    private val MV_LOCALIZE = listOf(DECRYPT, VIEW, IMGEDIT, ENCRYPT, COMPARE)
    val STEPS = listOf(UNPACK, EXPORT, TRANSLATE, IMPORT, PACK, TEST)
    val COMPRESS_STEPS = listOf(UNPACK, SCAN, AUDIO, IMAGE, VIDEO, PACK)

    val EXTRACT_STEPS = listOf(UNPACK, SCAN, VIEW, CONVERT, DUPES, RENAME)

    val PATCH_STEPS = listOf(UNPACK, EDIT, COMPARE, PACK, TEST)

    val AUDIT_STEPS = listOf(UNPACK, SCAN, TRCHECK, DUPES, TEST)

    val CLEAN_STEPS = listOf(UNPACK, BACKUP, SCAN, DUPES, NAMES, CONVERT, PACK)

    val LOCALIZE_STEPS = listOf(UNPACK, VIEW, IMGEDIT, CONVERT, COMPARE, PACK)

    const val TYPE_LOCALIZE = 6
    const val TYPE_CLEAN = 5
    const val TYPE_AUDIT = 4
    const val TYPE_PATCH = 3
    const val TYPE_EXTRACT = 2
    const val TYPE_TRANSLATION = 0
    const val TYPE_COMPRESS = 1
    fun stepsOf(type: Int, mech: Int = MECH_VN): List<Int> = if (mech == MECH_MV) when (type) {
        TYPE_COMPRESS -> MV_COMPRESS; TYPE_EXTRACT -> MV_EXTRACT; TYPE_PATCH -> MV_PATCH; TYPE_AUDIT -> MV_AUDIT
        TYPE_CLEAN -> MV_CLEAN; TYPE_LOCALIZE -> MV_LOCALIZE; else -> MV_TRANSLATION
    } else stepsOf0(type)
    private fun stepsOf0(type: Int) = when (type) { TYPE_COMPRESS -> COMPRESS_STEPS; TYPE_EXTRACT -> EXTRACT_STEPS; TYPE_PATCH -> PATCH_STEPS; TYPE_AUDIT -> AUDIT_STEPS; TYPE_CLEAN -> CLEAN_STEPS; TYPE_LOCALIZE -> LOCALIZE_STEPS; else -> STEPS }

    val list = mutableStateListOf<Project>()
    /** Project whose step was opened; the tool's Back button returns to the project while this is set. */
    var active: String? = null
    /** Step of the active project that is currently open in a tool. */
    var activeStep: Int = 0
    private var loaded = false

    private fun prefs() = Engine.app.applicationContext.getSharedPreferences("projects", Context.MODE_PRIVATE)

    fun load() {
        if (loaded) return
        loaded = true
        try {
            val a = JSONArray(prefs().getString("list", "[]") ?: "[]")
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val p = Project(o.getString("id"), o.optString("name"), o.optLong("created"), o.optString("note"), o.optInt("type", 0), o.optInt("mech", MECH_VN))
                p.sourceUri = o.optString("sourceUri"); p.sourceName = o.optString("sourceName")
                p.workUri = o.optString("workUri"); p.workName = o.optString("workName")
                val s = o.optJSONObject("status")
                if (s != null) for (k in s.keys()) p.status[k.toInt()] = s.optInt(k)
                list.add(p)
            }
        } catch (e: Exception) { }
    }

    fun save() {
        val a = JSONArray()
        for (p in list) {
            val s = JSONObject()
            for ((k, v) in p.status) s.put(k.toString(), v)
            a.put(JSONObject().put("id", p.id).put("name", p.name).put("created", p.created).put("note", p.note).put("type", p.type).put("mech", p.mech).put("status", s)
                .put("sourceUri", p.sourceUri).put("sourceName", p.sourceName).put("workUri", p.workUri).put("workName", p.workName))
        }
        prefs().edit().putString("list", a.toString()).apply()
    }

    fun create(name: String, type: Int = TYPE_TRANSLATION, mech: Int = MECH_VN): Project {
        load()
        val p = Project(System.currentTimeMillis().toString(36) + list.size, name.trim(), System.currentTimeMillis(), "", type, mech)
        list.add(0, p)
        save()
        return p
    }

    /** A new project of the same kind with the same name (+ suffix), note, source archive and work folder, and all steps reset. */
    fun duplicate(p: Project, suffix: String): Project {
        load()
        val c = Project(System.currentTimeMillis().toString(36) + list.size, p.name + suffix, System.currentTimeMillis(), p.note, p.type, p.mech)
        c.sourceUri = p.sourceUri; c.sourceName = p.sourceName; c.workUri = p.workUri; c.workName = p.workName
        list.add(0, c)
        save()
        return c
    }

    fun get(id: String?): Project? { load(); return list.firstOrNull { it.id == id } }

    fun delete(id: String) {
        list.removeAll { it.id == id }
        if (active == id) active = null
        save()
    }

    fun setState(p: Project, step: Int, st: Int) {
        if (st == ST_NONE) p.status.remove(step) else p.status[step] = st
        save()
    }

    /** Marks a step as opened (unless it is already done). */
    fun opened(p: Project, step: Int) {
        if (p.state(step) == ST_NONE) setState(p, step, ST_OPEN)
        active = p.id
        activeStep = step
    }

    /** The step that is open in a tool and ran without failures becomes Done (only the steps that produce a result: unpack, export, import, pack). */
    fun autoMark(ok: Boolean) {
        val p = get(active) ?: return
        val st = activeStep
        if (ok && (st == UNPACK || st == DECRYPT || st == ENCRYPT || st == EXPORT || st == IMPORT || st == PACK || st == AUDIO || st == IMAGE || st == VIDEO || st == CONVERT || st == COMPARE || st == BACKUP)) setState(p, st, ST_DONE)
        activeStep = 0
    }

    /**
     * The folder the project's archive was unpacked into: a sub-folder of the work folder named after the archive
     * (also with the "_ekstrak" suffix or without the extension); when there is exactly one sub-folder that one; otherwise the work folder.
     * Returns a document URI inside the work folder tree, which the pack input accepts.
     */
    fun unpackedFolder(ctx: Context, p: Project): android.net.Uri? {
        if (p.workUri.isEmpty()) return null
        val tree = android.net.Uri.parse(p.workUri)
        val root = androidx.documentfile.provider.DocumentFile.fromTreeUri(ctx, tree) ?: return null
        val dirs = root.listFiles().filter { it.isDirectory }
        val src = p.sourceName
        val base = src.substringBeforeLast('.')
        val pick = dirs.firstOrNull { it.name == src }
            ?: dirs.firstOrNull { it.name == base }
            ?: dirs.firstOrNull { src.isNotEmpty() && (it.name ?: "").startsWith(src) }
            ?: if (dirs.size == 1) dirs[0] else null
        return pick?.uri ?: tree
    }

    /** MV/MZ game folder: MZ when its js folder (also under www) holds rmmz_*.js files; otherwise MV. */
    fun isMz(ctx: Context, p: Project): Boolean {
        return try {
            if (p.sourceUri.isEmpty()) return false
            val root = androidx.documentfile.provider.DocumentFile.fromTreeUri(ctx, android.net.Uri.parse(p.sourceUri)) ?: return false
            val js = root.findFile("js") ?: root.findFile("www")?.findFile("js") ?: return false
            js.listFiles().any { (it.name ?: "").startsWith("rmmz_") }
        } catch (e: Exception) { false }
    }

    /** Settings backup: all projects as JSON. */
    fun exportJson(): JSONArray { load(); return JSONArray(prefs().getString("list", "[]") ?: "[]") }
}
