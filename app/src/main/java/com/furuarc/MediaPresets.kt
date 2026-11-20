package com.furuarc

import android.content.Context
import java.net.URLDecoder
import java.net.URLEncoder

/** Storage of Media Asset Converter settings presets (per kind: 0 audio, 1 image, 2 video, 3 text). */
object MediaPresets {
    /** Special name for "last used settings". */
    const val LAST = "__last__"

    private fun sp() = Engine.app.getSharedPreferences("media_presets", Context.MODE_PRIVATE)

    fun encode(m: Map<String, String>): String =
        m.entries.joinToString("&") { URLEncoder.encode(it.key, "UTF-8") + "=" + URLEncoder.encode(it.value, "UTF-8") }

    fun decode(s: String): Map<String, String> {
        val out = HashMap<String, String>()
        for (part in s.split('&')) {
            val i = part.indexOf('=')
            if (i <= 0) continue
            out[URLDecoder.decode(part.substring(0, i), "UTF-8")] = URLDecoder.decode(part.substring(i + 1), "UTF-8")
        }
        return out
    }

    /** User-made presets (including [LAST]) as name -> content pairs, sorted by name. */
    fun list(kind: Int): List<Pair<String, String>> {
        val prefix = "p$kind|"
        val out = ArrayList<Pair<String, String>>()
        for ((k, v) in sp().all) {
            if (k.startsWith(prefix) && v is String) out.add(Pair(k.substring(prefix.length), v))
        }
        return out.sortedBy { it.first.lowercase() }
    }

    fun save(kind: Int, name: String, data: Map<String, String>) {
        sp().edit().putString("p$kind|$name", encode(data)).apply()
    }

    fun delete(kind: Int, name: String) {
        sp().edit().remove("p$kind|$name").apply()
    }
}
