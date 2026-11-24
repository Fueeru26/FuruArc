package com.furuarc.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.core.TxtDoc

/**
 * A file waiting to be shown in the "Lihat" (View) mode of the File Text Editor. Set by "Open with" / "Share" and by
 * "Find features by file"; the Text Editor screen of the matching [kind] takes it when it opens.
 * Kinds: "plain" (Plain Text) or a structured format: "json", "csv", "ini", "xml", "md".
 */
object TextViewHandoff {
    private var uri: Uri? = null
    private var kind: String = ""

    /** Changes with every new waiting file, so a Text Editor screen that is already open notices it too. */
    var token by mutableStateOf(0)
        private set

    fun set(u: Uri, k: String) { uri = u; kind = k; token++ }
    fun clear() { uri = null; kind = "" }

    /** Structured Text hub: entry of the format list the waiting file belongs to (1 JSON, 2 CSV, 3 INI, 4 XML, 5 Markdown), or 0. */
    fun structIndex(): Int = if (uri == null) 0 else when (kind) {
        "json" -> 1; "csv" -> 2; "ini" -> 3; "xml" -> 4; "md" -> 5; else -> 0
    }

    /** Returns (and consumes) the waiting file when it is meant for the screen of [k]. */
    fun take(k: String): Uri? {
        val u = uri ?: return null
        if (kind != k) return null
        clear()
        return u
    }

    /** Which Text Editor screen a file extension belongs to: "plain" for .txt, a structured kind for a registered format, else null. */
    fun kindFor(ext: String): String? = when (ext.lowercase().trimStart('.')) {
        "txt" -> "plain"
        "json" -> "json"
        "csv", "tsv" -> "csv"
        "ini", "cfg", "conf", "properties", "desktop" -> "ini"
        "xml", "html", "htm", "svg", "xaml", "plist", "resx", "rss" -> "xml"
        "md", "markdown" -> "md"
        else -> null
    }
}

/** The first 16 KB of the file look like text (no null bytes). Decides where a file of an unknown format goes. Call on the IO thread. */
fun looksLikeText(ctx: Context, uri: Uri): Boolean = try {
    ctx.contentResolver.openInputStream(uri)?.use { ins ->
        val buf = ByteArray(16 * 1024)
        var n = 0
        while (n < buf.size) {
            val r = ins.read(buf, n, buf.size - n)
            if (r < 0) break
            n += r
        }
        !TxtDoc.looksBinary(buf.copyOf(n))
    } ?: false
} catch (e: Throwable) {
    false
}
