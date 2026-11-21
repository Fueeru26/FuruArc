package com.furuarc.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** Files handed to the app by "Open with…" (ACTION_VIEW) or "Share" (ACTION_SEND); App() routes them by file type. */
object IncomingFiles {
    private var pending: List<Uri> = emptyList()
    var token by mutableStateOf(0)
        private set

    @Suppress("DEPRECATION")
    fun handle(intent: Intent?) {
        if (intent == null) return
        val list = ArrayList<Uri>()
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.let { list.add(it) }
            Intent.ACTION_SEND -> (intent.getParcelableExtra<android.os.Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { list.add(it) }
            Intent.ACTION_SEND_MULTIPLE -> intent.getParcelableArrayListExtra<android.os.Parcelable>(Intent.EXTRA_STREAM)?.forEach { (it as? Uri)?.let { u -> list.add(u) } }
        }
        if (list.isEmpty()) return
        // handled once: a later recreation (rotation) must not open the file again
        intent.action = Intent.ACTION_MAIN
        pending = list
        token++
    }

    fun take(): List<Uri>? {
        val p = pending
        pending = emptyList()
        return p.ifEmpty { null }
    }
}
