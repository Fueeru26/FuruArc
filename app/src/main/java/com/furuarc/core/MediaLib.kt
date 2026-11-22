package com.furuarc.core

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONArray
import org.json.JSONObject

/** A saved file of a playlist / collection (document URI as text). */
class LItem(val name: String, val uri: String, val size: Long)

/** Playlist (audio, video) or collection (images). The list with id [MediaLib.FAV] is the built-in Favorites list. */
class MList(val id: String, val name: String, val items: List<LItem>)

/** A folder the user added to a Folder tab. */
class MFolder(val uri: String, val name: String)

/**
 * Folders, playlists / collections, favorites and the last queue of the Media Player.
 * Everything is kept per kind (1 = images, 2 = audio, 3 = video), saved on every change, and part of the settings export.
 */
object MediaLib {
    const val FAV = "fav"
    private var sp: SharedPreferences? = null

    var folders by mutableStateOf<Map<Int, List<MFolder>>>(emptyMap())
        private set
    var lists by mutableStateOf<Map<Int, List<MList>>>(emptyMap())
        private set

    fun init(ctx: Context) {
        if (sp != null) return
        val s = ctx.applicationContext.getSharedPreferences("media_lib", Context.MODE_PRIVATE)
        sp = s
        val f = HashMap<Int, List<MFolder>>()
        val l = HashMap<Int, List<MList>>()
        for (k in 1..3) {
            f[k] = parseFolders(s.getString("folders_$k", null))
            l[k] = withFav(parseLists(s.getString("lists_$k", null)))
        }
        folders = f
        lists = l
    }

    private fun parseFolders(text: String?): List<MFolder> {
        val out = ArrayList<MFolder>()
        try {
            val a = JSONArray(text ?: "[]")
            for (i in 0 until a.length()) { val o = a.getJSONObject(i); out.add(MFolder(o.getString("u"), o.optString("n", ""))) }
        } catch (e: Exception) { }
        return out
    }

    private fun parseLists(text: String?): List<MList> {
        val out = ArrayList<MList>()
        try {
            val a = JSONArray(text ?: "[]")
            for (i in 0 until a.length()) {
                val o = a.getJSONObject(i)
                val items = ArrayList<LItem>()
                val ia = o.optJSONArray("i")
                if (ia != null) for (j in 0 until ia.length()) { val x = ia.getJSONObject(j); items.add(LItem(x.optString("n", ""), x.getString("u"), x.optLong("s", 0L))) }
                out.add(MList(o.getString("id"), o.optString("n", ""), items))
            }
        } catch (e: Exception) { }
        return out
    }

    private fun foldersJson(l: List<MFolder>): JSONArray {
        val a = JSONArray()
        for (f in l) a.put(JSONObject().put("u", f.uri).put("n", f.name))
        return a
    }

    private fun listsJson(l: List<MList>): JSONArray {
        val a = JSONArray()
        for (x in l) {
            val ia = JSONArray()
            for (i in x.items) ia.put(JSONObject().put("n", i.name).put("u", i.uri).put("s", i.size))
            a.put(JSONObject().put("id", x.id).put("n", x.name).put("i", ia))
        }
        return a
    }

    /** Favorites is always the first list. */
    private fun withFav(l: List<MList>): List<MList> {
        val fav = l.firstOrNull { it.id == FAV } ?: MList(FAV, "", emptyList())
        return listOf(fav) + l.filter { it.id != FAV }
    }

    fun foldersOf(kind: Int): List<MFolder> = folders[kind] ?: emptyList()
    fun listsOf(kind: Int): List<MList> = lists[kind] ?: listOf(MList(FAV, "", emptyList()))

    private fun saveFolders(kind: Int, l: List<MFolder>) {
        folders = folders + (kind to l)
        sp?.edit()?.putString("folders_$kind", foldersJson(l).toString())?.apply()
    }

    private fun saveLists(kind: Int, l: List<MList>) {
        val n = withFav(l)
        lists = lists + (kind to n)
        sp?.edit()?.putString("lists_$kind", listsJson(n).toString())?.apply()
    }

    // ---- folders
    fun addFolder(kind: Int, uri: Uri, name: String): Boolean {
        val u = uri.toString()
        if (foldersOf(kind).any { it.uri == u }) return false
        saveFolders(kind, foldersOf(kind) + MFolder(u, name))
        return true
    }

    fun removeFolder(kind: Int, uri: String) = saveFolders(kind, foldersOf(kind).filter { it.uri != uri })

    // ---- lists
    fun createList(kind: Int, name: String): String {
        val id = java.lang.Long.toString(System.currentTimeMillis(), 36) + (0..999).random()
        saveLists(kind, listsOf(kind) + MList(id, name.trim(), emptyList()))
        return id
    }

    fun renameList(kind: Int, id: String, name: String) {
        if (id == FAV) return
        saveLists(kind, listsOf(kind).map { if (it.id == id) MList(it.id, name.trim(), it.items) else it })
    }

    /** Removes every item of a list (the list itself stays; also works for Favorites). */
    fun clearList(kind: Int, id: String) {
        saveLists(kind, listsOf(kind).map { if (it.id == id) MList(it.id, it.name, emptyList()) else it })
    }

    fun deleteList(kind: Int, id: String) {
        if (id == FAV) return
        saveLists(kind, listsOf(kind).filter { it.id != id })
    }

    /** Adds items that have a document URI (temporary files cannot be saved). Returns how many were really added. */
    fun addItems(kind: Int, id: String, items: List<MItem>): Int {
        var added = 0
        var changed = false
        val res = listsOf(kind).map { l ->
            if (l.id != id) l else {
                val have = l.items.map { it.uri }.toHashSet()
                val extra = ArrayList<LItem>()
                for (it in items) {
                    val u = it.uri?.toString() ?: continue
                    if (have.add(u)) { extra.add(LItem(it.name, u, it.size)); added++ }
                }
                if (extra.isNotEmpty()) changed = true
                MList(l.id, l.name, l.items + extra)
            }
        }
        if (changed) saveLists(kind, res)
        return added
    }

    fun removeItem(kind: Int, id: String, uri: String) {
        saveLists(kind, listsOf(kind).map { l -> if (l.id != id) l else MList(l.id, l.name, l.items.filter { it.uri != uri }) })
    }

    fun isFav(kind: Int, item: MItem): Boolean {
        val u = item.uri?.toString() ?: return false
        return listsOf(kind).firstOrNull { it.id == FAV }?.items?.any { it.uri == u } == true
    }

    /** Returns true when the item is a favorite afterwards. */
    fun toggleFav(kind: Int, item: MItem): Boolean {
        val u = item.uri?.toString() ?: return false
        return if (isFav(kind, item)) { removeItem(kind, FAV, u); false } else { addItems(kind, FAV, listOf(item)); true }
    }

    fun toItems(l: MList): List<MItem> = l.items.map { MItem(it.name, Uri.parse(it.uri), null, it.size) }

    // ---- last queue (restored when the Media Player opens)
    fun saveQueue(kind: Int, items: List<MItem>, label: String) {
        val a = JSONArray()
        for (it in items) {
            val u = it.uri?.toString() ?: continue
            a.put(JSONObject().put("n", it.name).put("u", u).put("s", it.size))
            if (a.length() >= 5000) break
        }
        sp?.edit()?.putString("queue_$kind", a.toString())?.putString("queueLabel_$kind", label)?.apply()
    }

    fun loadQueue(kind: Int): Pair<List<MItem>, String> {
        val out = ArrayList<MItem>()
        try {
            val a = JSONArray(sp?.getString("queue_$kind", "[]") ?: "[]")
            for (i in 0 until a.length()) { val o = a.getJSONObject(i); out.add(MItem(o.optString("n", ""), Uri.parse(o.getString("u")), null, o.optLong("s", 0L))) }
        } catch (e: Exception) { }
        return out to (sp?.getString("queueLabel_$kind", "") ?: "")
    }

    // ---- export / import (part of the Media Settings file)
    fun exportJson(): JSONObject {
        val o = JSONObject()
        for (k in 1..3) o.put(k.toString(), JSONObject().put("folders", foldersJson(foldersOf(k))).put("lists", listsJson(listsOf(k))))
        return o
    }

    fun importJson(o: JSONObject) {
        for (k in 1..3) {
            val x = o.optJSONObject(k.toString()) ?: continue
            saveFolders(k, parseFolders(x.optJSONArray("folders")?.toString()))
            saveLists(k, parseLists(x.optJSONArray("lists")?.toString()))
        }
    }

    fun clearAll() {
        for (k in 1..3) { saveFolders(k, emptyList()); saveLists(k, emptyList()) }
    }
}
