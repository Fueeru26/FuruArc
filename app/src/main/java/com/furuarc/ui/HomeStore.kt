package com.furuarc.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Persistent state of the Home tab: recently used features, favorites and recent searches (stored in SharedPreferences "home"). */
object HomeStore {
    private const val MAX_RECENT = 8
    private const val MAX_HISTORY = 6
    const val MAX_QUICK = 10
    val DEFAULT_QUICK = listOf("vn.unpack", "editor", "textedit", "media")
    private var sp: android.content.SharedPreferences? = null

    var recents by mutableStateOf<List<String>>(emptyList())
        private set
    var favorites by mutableStateOf<List<String>>(emptyList())
        private set
    var history by mutableStateOf<List<String>>(emptyList())
        private set
    /** Quick actions (ordered, max [MAX_QUICK]); defaults until the user edits them. */
    var quick by mutableStateOf(DEFAULT_QUICK)
        private set

    fun init(ctx: Context) {
        if (sp != null) return
        val p = ctx.applicationContext.getSharedPreferences("home", Context.MODE_PRIVATE)
        sp = p
        recents = load(p, "recents")
        favorites = load(p, "favorites")
        history = load(p, "history")
        if (p.contains("quick")) quick = load(p, "quick").take(MAX_QUICK)
    }

    private fun load(p: android.content.SharedPreferences, key: String): List<String> =
        (p.getString(key, "") ?: "").split('\n').filter { it.isNotBlank() }

    private fun save(key: String, list: List<String>) {
        sp?.edit()?.putString(key, list.joinToString("\n"))?.apply()
    }

    fun touch(id: String) {
        if (id.isBlank() || sp == null) return
        val n = (listOf(id) + recents.filter { it != id }).take(MAX_RECENT)
        if (n == recents) return
        recents = n
        save("recents", n)
    }

    fun clearRecents() { recents = emptyList(); save("recents", recents) }

    fun isFav(id: String) = id in favorites

    fun toggleFav(id: String) {
        favorites = if (id in favorites) favorites.filter { it != id } else favorites + id
        save("favorites", favorites)
    }

    fun addHistory(q: String) {
        val s = q.trim().replace('\n', ' ')
        if (s.length < 2) return
        history = (listOf(s) + history.filter { !it.equals(s, ignoreCase = true) }).take(MAX_HISTORY)
        save("history", history)
    }

    fun updateQuick(list: List<String>) {
        quick = list.distinct().take(MAX_QUICK)
        save("quick", quick) // an empty list is stored too: the user removed everything on purpose
    }

    fun clearHistory() { history = emptyList(); save("history", history) }
}
