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
class Project(val id: String, name: String, val created: Long, note: String = "") {
    var name by mutableStateOf(name)
    var note by mutableStateOf(note)
    /** Step number -> state ([Projects.ST_NONE], [Projects.ST_OPEN], [Projects.ST_DONE]). */
    val status = mutableStateMapOf<Int, Int>()

    fun state(step: Int) = status[step] ?: Projects.ST_NONE
    fun doneCount() = Projects.STEPS.count { state(it) == Projects.ST_DONE }
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
    val STEPS = listOf(UNPACK, EXPORT, TRANSLATE, IMPORT, PACK, TEST)

    val list = mutableStateListOf<Project>()
    /** Project whose step was opened; the tool's Back button returns to the project while this is set. */
    var active: String? = null
    private var loaded = false

    private fun prefs() = Engine.app.applicationContext.getSharedPreferences("projects", Context.MODE_PRIVATE)

    fun load() {
        if (loaded) return
        loaded = true
        try {
            val a = JSONArray(prefs().getString("list", "[]") ?: "[]")
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val p = Project(o.getString("id"), o.optString("name"), o.optLong("created"), o.optString("note"))
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
            a.put(JSONObject().put("id", p.id).put("name", p.name).put("created", p.created).put("note", p.note).put("status", s))
        }
        prefs().edit().putString("list", a.toString()).apply()
    }

    fun create(name: String): Project {
        load()
        val p = Project(System.currentTimeMillis().toString(36) + list.size, name.trim(), System.currentTimeMillis())
        list.add(0, p)
        save()
        return p
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
    }

    /** Settings backup: all projects as JSON. */
    fun exportJson(): JSONArray { load(); return JSONArray(prefs().getString("list", "[]") ?: "[]") }
}
