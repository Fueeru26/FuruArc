package com.furuarc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.DownloadState
import com.furuarc.core.UpdateChecker
import com.furuarc.core.UpdateInstaller
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

/** Buttons stacked vertically (full width), outside any scrolling area. */
@Composable
private fun RowScope.StackedButtons(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
}

private fun mb(b: Long): String = String.format(java.util.Locale.US, "%.1f MB", b / 1048576.0)

/** Plain-text view of GitHub release notes (Markdown): drops heading marks, bold marks and list dashes. */
private fun cleanNotes(s: String): String = s.trim().lines().joinToString("\n") { l ->
    l.replace(Regex("^#+\\s*"), "")
        .replace("**", "")
        .replace("`", "")
        .replace(Regex("^\\s*[-*]\\s+"), "\u2022 ")
}

private val DialogHPad = 4.dp

/** "Check for updates" window: shows the check result and downloads / installs the latest release. */
@Composable
fun UpdateDialog(onDismiss: () -> Unit) {
    val nav = NavStr.of(LocalLang.current)
    val ctx = LocalContext.current
    val p = LocalPalette.current
    val st = UpdateChecker.state
    val dl = UpdateInstaller.state
    val installed = UpdateChecker.installedVersion(ctx)
    val info = (st as? UpdateState.Available)?.info
    AppDialogW(
        title = if (dl is DownloadState.Running || dl is DownloadState.Done) nav.updDownloading else nav.updTitle,
        onDismiss = onDismiss,
        hPad = DialogHPad,
        buttons = {
            StackedButtons {
                when {
                    dl is DownloadState.Running -> {
                        SecondaryButton(nav.updCancelDl) { UpdateInstaller.cancel() }
                        SecondaryButton(nav.close, onDismiss)
                    }
                    dl is DownloadState.Done -> {
                        PrimaryButton(nav.updInstall, true) { UpdateInstaller.install(ctx, dl.file) }
                        SecondaryButton(nav.close, onDismiss)
                    }
                    info != null -> {
                        PrimaryButton(nav.updDownload, true) {
                            if (info.apkUrl != null) UpdateInstaller.start(ctx, info) else { openUrl(ctx, info.pageUrl); onDismiss() }
                        }
                        SecondaryButton(nav.close, onDismiss)
                    }
                    st is UpdateState.NoRelease || st is UpdateState.Failed -> {
                        PrimaryButton(nav.updTitle, true) { UpdateChecker.check(ctx) }
                        SecondaryButton(nav.close, onDismiss)
                    }
                    else -> SecondaryButton(nav.close, onDismiss)
                }
            }
        },
    ) {
        when {
            dl is DownloadState.Running -> {
                Text(info?.tag ?: "", color = p.text, fontSize = 15.sp)
                LinearProgressIndicator(if (dl.total > 0) dl.done.toFloat() / dl.total else 0f, Modifier.fillMaxWidth(), color = p.accent)
                Text(
                    if (dl.total > 0) "${mb(dl.done)} / ${mb(dl.total)}  ·  ${(dl.done * 100 / dl.total).toInt()}%" else mb(dl.done),
                    color = p.muted, fontSize = 13.sp,
                )
            }
            dl is DownloadState.Done -> Text(nav.updDownloadDone, color = p.text, fontSize = 15.sp)
            st is UpdateState.Checking || st is UpdateState.Idle -> Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(22.dp), color = p.accent, strokeWidth = 2.dp)
                Spacer(Modifier.width(12.dp))
                Text(nav.updChecking, color = p.text, fontSize = 15.sp)
            }
            st is UpdateState.UpToDate -> {
                Text(nav.updLatest, color = p.text, fontSize = 15.sp)
                Text("${nav.updInstalled}: $installed", color = p.muted, fontSize = 13.sp)
            }
            st is UpdateState.NoRelease -> Text(nav.updNone, color = p.text, fontSize = 15.sp)
            st is UpdateState.Failed -> {
                Text(nav.updFail, color = p.text, fontSize = 15.sp)
                Text(st.reason, color = p.muted, fontSize = 12.sp)
            }
            info != null -> {
                if (dl is DownloadState.Failed) {
                    Text(nav.updDownFail, color = p.text, fontSize = 14.sp)
                    Text(dl.reason, color = p.muted, fontSize = 12.sp)
                }
                UpdateBody(info, installed)
            }
        }
    }
}

/** Pop-up shown once when an automatic check finds a new version. [onDownload] runs after the download has started. */
@Composable
fun UpdateAnnounceDialog(info: UpdateInfo, onDownload: () -> Unit, onDismiss: () -> Unit) {
    val nav = NavStr.of(LocalLang.current)
    val ctx = LocalContext.current
    AppDialogW(
        title = nav.updNew,
        onDismiss = onDismiss,
        hPad = DialogHPad,
        buttons = {
            StackedButtons {
                PrimaryButton(nav.updDownload, true) {
                    if (info.apkUrl != null) { UpdateInstaller.start(ctx, info); onDownload() } else openUrl(ctx, info.pageUrl)
                    onDismiss()
                }
                SecondaryButton(nav.updLater, onDismiss)
            }
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
        Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
            Text(cleanNotes(info.notes), color = p.text, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
    if (info.apkUrl == null) Text(nav.updNoApk, color = p.muted, fontSize = 12.sp)
}
