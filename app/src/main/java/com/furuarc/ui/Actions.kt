package com.furuarc.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import android.widget.Toast

/** Open the result folder (SAF document URI) via the "Open with" dialog. */
fun openFolder(ctx: Context, folder: Uri?, title: String, failMsg: String) {
    if (folder == null) return
    try {
        val i = Intent(Intent.ACTION_VIEW)
            .setDataAndType(folder, DocumentsContract.Document.MIME_TYPE_DIR)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val chooser = Intent.createChooser(i, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(ctx, failMsg, Toast.LENGTH_SHORT).show()
    }
}

fun copyToClipboard(ctx: Context, text: String, doneMsg: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("FuruArc log", text))
    Toast.makeText(ctx, doneMsg, Toast.LENGTH_SHORT).show()
}
