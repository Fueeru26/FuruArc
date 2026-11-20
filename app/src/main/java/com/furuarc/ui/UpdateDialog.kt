package com.furuarc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.UpdateChecker
import com.furuarc.core.UpdateInfo
import com.furuarc.core.UpdateState
import com.furuarc.i18n.NavStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private fun openUrl(ctx: Context, url: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        android.widget.Toast.makeText(ctx, url, android.widget.Toast.LENGTH_LONG).show()
    }
}

/** "Check for updates" window: shows the check result and sends the user to the latest GitHub release to download the app. */
@Composable
fun UpdateDialog(onDismiss: () -> Unit) {
    val nav = NavStr.of(LocalLang.current)
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val st = UpdateChecker.state
    val installed = UpdateChecker.installedVersion(ctx)
    AppDialog(
        title = nav.updTitle,
        onDismiss = onDismiss,
        buttons = {
            SecondaryButton(nav.close, onDismiss)
            if (st is UpdateState.Available) {
                Spacer(Modifier.width(8.dp))
                PrimaryButton(nav.updDownload, true) { openUrl(ctx, st.info.apkUrl ?: st.info.pageUrl); onDismiss() }
            } else if (st is UpdateState.NoRelease || st is UpdateState.Failed) {
                Spacer(Modifier.width(8.dp))
                PrimaryButton(nav.updTitle, true) { UpdateChecker.check(ctx) }
            }
        },
    ) {
        when (st) {
            is UpdateState.Checking, is UpdateState.Idle -> Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(22.dp), color = p.accent, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(nav.updChecking, color = p.text, fontSize = 15.sp)
            }
            is UpdateState.UpToDate -> {
                Text(nav.updLatest, color = p.text, fontSize = 15.sp)
                Text("${nav.updInstalled}: $installed", color = p.muted, fontSize = 13.sp)
            }
            is UpdateState.NoRelease -> Text(nav.updNone, color = p.text, fontSize = 15.sp)
            is UpdateState.Failed -> {
                Text(nav.updFail, color = p.text, fontSize = 15.sp)
                Text(st.reason, color = p.muted, fontSize = 12.sp)
            }
            is UpdateState.Available -> UpdateBody(st.info, installed)
        }
    }
}

/** Pop-up shown once when an automatic check finds a new version. */
@Composable
fun UpdateAnnounceDialog(info: UpdateInfo, onDismiss: () -> Unit) {
    val nav = NavStr.of(LocalLang.current)
    val ctx = LocalContext.current
    AppDialog(
        title = nav.updNew,
        onDismiss = onDismiss,
        buttons = {
            SecondaryButton(nav.updLater, onDismiss)
            Spacer(Modifier.width(8.dp))
            PrimaryButton(nav.updDownload, true) { openUrl(ctx, info.apkUrl ?: info.pageUrl); onDismiss() }
        },
    ) { UpdateBody(info, UpdateChecker.installedVersion(ctx)) }
}

@Composable
private fun UpdateBody(info: UpdateInfo, installed: String) {
    val nav = NavStr.of(LocalLang.current)
    val p = LocalPalette.current
    Text(info.name, color = p.text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
    Text("${nav.updInstalled}: $installed  ·  ${nav.updLatestLabel}: ${info.tag}", color = p.muted, fontSize = 13.sp)
    if (info.notes.isNotBlank()) {
        Text(nav.updNotes, color = p.muted, fontSize = 12.sp)
        Column(Modifier.heightIn(max = 180.dp).verticalScroll(rememberScrollState())) {
            Text(info.notes.trim().take(1500), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
    if (info.apkUrl == null) Text(nav.updNoApk, color = p.muted, fontSize = 12.sp)
}
