package com.furuarc.ui

import com.furuarc.i18n.SysStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.i18n.gameStr
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import com.furuarc.MainViewModel
import com.furuarc.i18n.Strings
import com.furuarc.i18n.r
import com.furuarc.settings.AccentColor
import com.furuarc.settings.Lang
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import com.furuarc.ui.theme.accentColor

@Composable
private fun SectionTitle(text: String) {
    val p = LocalPalette.current
    Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.accent, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
}

@Composable
private fun SwitchRow(label: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(desc, color = p.muted, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = p.accent,
                checkedTrackColor = p.accent.copy(alpha = 0.45f),
                uncheckedThumbColor = p.muted,
                uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
            ),
        )
    }
}

/** App Settings: every change is applied immediately and saved automatically. */
@Composable
fun SettingsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    // Settings are applied and saved as soon as they are changed (no Save button).
    val draft by vm.settings.collectAsState()
    fun commit(n: com.furuarc.settings.AppSettings) = vm.saveSettings(n)

    // Notification permission (Android 13+) is requested when "Background Processing" is enabled.
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(title = s.menuSettingsTitle, onBack = onBack)

    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {

        // ---------- Appearance ----------
        SectionTitle(s.secDisplay)
        SectionCard(s.themeMode) {
            val sys = SysStr.of(LocalLang.current)
            AppDropdown(
                sys.themeLabel, draft.themeMode.toString(),
                listOf("0" to sys.themeSystem, "1" to sys.themeLight, "2" to sys.themeDark),
            ) { v -> commit(draft.copy(themeMode = v.toInt())) }
        }
        SectionCard(s.themeColor) {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                AccentColor.values().forEach { a ->
                    val selected = a == draft.accent
                    Column(
                        Modifier.clickable { commit(draft.copy(accent = a)) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape).background(accentColor(a, p.dark)),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (p.dark) Color(0xFF101214) else Color.White,
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            s.colorNames[a] ?: a.id,
                            fontSize = 11.sp,
                            color = if (selected) p.text else p.muted,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // ---------- Language ----------
        SectionTitle(s.secLanguage)
        SectionCard(s.pickLanguage) {
            val sys = SysStr.of(LocalLang.current)
            AppDropdown(
                sys.langLabel, draft.langPref,
                listOf("system" to sys.langSystem, "en" to sys.langEn, "ja" to sys.langJa, "zh" to sys.langZh, "ko" to sys.langKo, "id" to sys.langId),
            ) { v -> commit(draft.copy(langPref = v)) }
            if (LocalLang.current != Lang.ID && LocalLang.current != Lang.EN || draft.langPref in listOf("ja", "zh", "ko")) Text(sys.langNote, color = p.muted, fontSize = 12.sp)
        }

        // ---------- System ----------
        SectionTitle(s.secSystem)
        AppCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SwitchRow(
                label = gameStr(LocalLang.current).engineLabelTitle,
                desc = gameStr(LocalLang.current).engineLabelDesc,
                checked = draft.showEngineLabel,
                onChange = { on -> commit(draft.copy(showEngineLabel = on)) },
            )
            SwitchRow(
                label = s.bgLabel,
                desc = s.bgDesc,
                checked = draft.bgProcess,
                onChange = { on ->
                    commit(draft.copy(bgProcess = on))
                    if (on && Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            )
            SwitchRow(
                label = s.keepOnLabel,
                desc = s.keepOnDesc,
                checked = draft.keepScreenOn,
                onChange = { on -> commit(draft.copy(keepScreenOn = on)) },
            )
            SwitchRow(
                label = s.r.exitTrLabel,
                desc = s.r.exitDesc,
                checked = draft.confirmExit,
                onChange = { on -> commit(draft.copy(confirmExit = on)) },
            )
            SwitchRow(
                label = s.r.nativeInputLabel,
                desc = s.r.nativeInputDesc,
                checked = draft.nativeInput,
                onChange = { on -> commit(draft.copy(nativeInput = on)) },
            )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
    }
}

/**
 * Tool Settings: shown directly in the Settings tab (without a section title), saved automatically as soon as they are changed.
 * Contents: the action when a file already exists on export + the Default Output Folder.
 */
@Composable
fun ToolSettingsBlock(vm: MainViewModel) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val ctx = LocalContext.current
    val cur by vm.settings.collectAsState()
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
                )
            } catch (e: Exception) {
                // stays in use for this session
            }
            val name = DocumentFile.fromTreeUri(ctx, uri)?.name ?: "folder"
            vm.saveSettings(vm.settings.value.copy(defaultOutUri = uri.toString(), defaultOutName = name))
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
        SectionCard(s.confExtractLabel) {
            SegmentedChips(
                items = listOf(s.conflictOverwrite, s.conflictSkip, s.conflictRename),
                selected = cur.conflict,
                onSelect = { i -> vm.saveSettings(vm.settings.value.copy(conflict = i)) },
            )
        }
        SectionCard(s.defaultOutputDir) {
            Surface(
                shape = RoundedCornerShape(CardRadius), color = Color.Transparent,
                border = androidx.compose.foundation.BorderStroke(1.dp, p.outline),
                modifier = Modifier.fillMaxWidth().clickable { pickFolder.launch(null) },
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(AppIcons.FolderUpload, null, tint = p.accent, modifier = Modifier.size(24.dp))
                    Text(
                        cur.defaultOutName.ifBlank { s.tapToSelect }, color = if (cur.defaultOutName.isBlank()) p.muted else p.text, fontSize = 16.sp,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    )
                    Icon(AppIcons.FolderPlus, null, tint = p.muted, modifier = Modifier.size(22.dp))
                }
            }
            if (cur.defaultOutUri.isNotEmpty()) {
                SecondaryButton(s.clearDefault) { vm.saveSettings(vm.settings.value.copy(defaultOutUri = "", defaultOutName = "")) }
            }
            Text(s.defaultOutHint, color = p.muted, fontSize = 12.sp)
        }
    }
}
