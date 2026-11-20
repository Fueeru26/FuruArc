package com.furuarc.ui

import android.net.Uri

/**
 * A file picked on the Home tab ("Find features by file"), waiting to be loaded by the target feature.
 * Screens that keep their own file state call [take] once when they open; features driven by the
 * ViewModel/job objects are preloaded directly from MainActivity.
 */
object PendingFile {
    private var uri: Uri? = null
    private var target: String = ""

    fun set(targetId: String, u: Uri) { target = targetId; uri = u }
    fun clear() { uri = null; target = "" }

    /** Returns (and consumes) the pending file when the target feature id starts with one of [prefixes]. */
    fun take(vararg prefixes: String): Uri? {
        val u = uri ?: return null
        if (prefixes.none { target.startsWith(it) }) return null
        clear()
        return u
    }
}
