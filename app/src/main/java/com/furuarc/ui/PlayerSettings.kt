package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.MediaPrefs
import com.furuarc.i18n.playerStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

@Composable
private fun PSwitch(label: String, desc: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().clickableScale(pressed = 0.99f, ripple = false) { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(label, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            if (desc.isNotEmpty()) Text(desc, color = p.muted, fontSize = 12.sp)
        }
        Switch(
            checked = checked, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = p.accent, checkedTrackColor = p.accent.copy(alpha = 0.45f),
                uncheckedThumbColor = p.muted, uncheckedTrackColor = p.muted.copy(alpha = 0.3f),
            ),
        )
    }
}

@Composable
private fun PDrop(label: String, value: Int, options: List<Pair<Int, String>>, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, fontSize = 12.sp, color = p.muted)
        AppDropdown(label, value.toString(), options.map { it.first.toString() to it.second }, showLabel = false) { onSelect(it.toInt()); MediaPrefs.save() }
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    LabelSection(title) { content() }
}

@Composable
fun PlayerSettingsScreen(onBack: () -> Unit) {
    val pl = playerStr(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    remember { PlayerHub.init(ctx); true }
    var msg by remember { mutableStateOf("") }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) {
            msg = try {
                ctx.contentResolver.openOutputStream(uri)?.use { it.write(MediaPrefs.exportJson().toByteArray(Charsets.UTF_8)) }
                pl.exported
            } catch (e: Exception) { pl.importFail }
        }
    }
    val import = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val text = try { ctx.contentResolver.openInputStream(uri)?.use { String(it.readBytes(), Charsets.UTF_8) } } catch (e: Exception) { null }
            msg = if (text != null && MediaPrefs.importJson(text)) pl.imported else pl.importFail
        }
    }
    val rep = listOf(0, 1, 2).map { it to pl.repeat(it) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        ScreenHeader(pl.settingsTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            Section(pl.secLists) {
                PSwitch(pl.subfolders, "", MediaPrefs.subfolders) { MediaPrefs.subfolders = it; MediaPrefs.save() }
                PDrop(pl.setSort, MediaPrefs.sortOrder, listOf(0 to pl.sortName, 1 to pl.sortDate, 2 to pl.sortSize)) { MediaPrefs.sortOrder = it }
                PDrop(pl.setListOrder, MediaPrefs.listOrder, listOf(0 to pl.orderDrag, 1 to pl.orderArrows)) { MediaPrefs.listOrder = it }
                PSwitch(pl.setTagTitle, pl.setTagTitleDesc, MediaPrefs.tagTitle) { MediaPrefs.tagTitle = it; MediaPrefs.save() }
                PSwitch(pl.setShowCover, "", MediaPrefs.showCover) { MediaPrefs.showCover = it; MediaPrefs.save() }
                PDrop(pl.setPageQueue, MediaPrefs.pageQueue, listOf(25, 50, 100, 200).map { it to pl.pageInfo(it) }) { MediaPrefs.pageQueue = it }
            }
            Section(pl.secAudio) {
                PSwitch(pl.setAutoNext, pl.setAutoNextDesc, MediaPrefs.autoNext) { MediaPrefs.autoNext = it; MediaPrefs.save() }
                PSwitch(pl.setShuffle, "", MediaPrefs.shuffle) { MediaPrefs.shuffle = it; MediaPrefs.save() }
                PDrop(pl.setRepeat, MediaPrefs.repeat, rep) { MediaPrefs.repeat = it }
                PDrop(pl.setSpeed, (MediaPrefs.speed * 100).toInt(), listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).map { (it * 100).toInt() to pl.speed(it) }) { MediaPrefs.speed = it / 100f }
                PSwitch(pl.setRemember, pl.setRememberDesc, MediaPrefs.rememberAudio) { MediaPrefs.rememberAudio = it; MediaPrefs.save() }
                PSwitch(pl.setBackground, pl.setBackgroundDesc, MediaPrefs.background) { MediaPrefs.background = it; MediaPrefs.save() }
                PSwitch(pl.setPauseUnplug, "", MediaPrefs.pauseUnplug) { MediaPrefs.pauseUnplug = it; MediaPrefs.save() }
                PSwitch(pl.setAudioFocus, pl.setAudioFocusDesc, MediaPrefs.audioFocus) { MediaPrefs.audioFocus = it; MediaPrefs.save() }
                PSwitch(pl.setRestoreQueue, pl.setRestoreQueueDesc, MediaPrefs.restoreQueue) { MediaPrefs.restoreQueue = it; MediaPrefs.save() }
                PDrop(pl.setMinAudio, MediaPrefs.minAudioKb, listOf(0, 50, 100, 300, 1000).map { it to pl.kb(it) }) { MediaPrefs.minAudioKb = it }
                Text(pl.setMinAudioDesc, color = p.muted, fontSize = 12.sp)
            }
            Section(pl.secVideo) {
                PSwitch(pl.setRememberVideo, pl.setRememberDesc, MediaPrefs.rememberVideo) { MediaPrefs.rememberVideo = it; MediaPrefs.save() }
                PSwitch(pl.setAutoSub, pl.setAutoSubDesc, MediaPrefs.autoSub) { MediaPrefs.autoSub = it; MediaPrefs.save() }
                PDrop(pl.setSubSize, MediaPrefs.subSize, listOf(70, 85, 100, 125, 150, 180).map { it to "$it%" }) { MediaPrefs.subSize = it }
                PSwitch(pl.setVideoAuto, "", MediaPrefs.videoAutoPlay) { MediaPrefs.videoAutoPlay = it; MediaPrefs.save() }
                PSwitch(pl.setKeepOn, "", MediaPrefs.keepScreenOn) { MediaPrefs.keepScreenOn = it; MediaPrefs.save() }
                PDrop(pl.setSkip, MediaPrefs.skipSec, listOf(5, 10, 15, 30).map { it to pl.seconds(it) }) { MediaPrefs.skipSec = it }
                PSwitch(pl.setDoubleTap, pl.setDoubleTapDesc, MediaPrefs.doubleTap) { MediaPrefs.doubleTap = it; MediaPrefs.save() }
                PSwitch(pl.setSwipe, pl.setSwipeDesc, MediaPrefs.swipeGestures) { MediaPrefs.swipeGestures = it; MediaPrefs.save() }
                PDrop(pl.setVideoOrient, MediaPrefs.videoOrient, listOf(0 to pl.orientAuto, 1 to pl.orientLandscape, 2 to pl.orientPortrait)) { MediaPrefs.videoOrient = it }
            }
            Section(pl.secGallery) {
                PDrop(pl.setPageGallery, MediaPrefs.pageGallery, listOf(30, 60, 100, 200).map { it to pl.pageInfo(it) }) { MediaPrefs.pageGallery = it }
                PDrop(pl.setGridCols, MediaPrefs.gridCols, listOf(2, 3, 4).map { it to it.toString() }) { MediaPrefs.gridCols = it }
                PDrop(pl.setImageFit, MediaPrefs.imageFit, listOf(0 to pl.fitScreen, 1 to pl.fitWidth)) { MediaPrefs.imageFit = it }
                PDrop(pl.setViewerBg, MediaPrefs.viewerBg, listOf(0 to pl.bgBlack, 1 to pl.bgGray, 2 to pl.bgWhite)) { MediaPrefs.viewerBg = it }
                PDrop(pl.setSlideshow, MediaPrefs.slideshowSec, listOf(2, 3, 4, 5, 8, 10, 15, 30).map { it to pl.seconds(it) }) { MediaPrefs.slideshowSec = it }
            }
            Section(pl.secData) {
                Text(pl.setBackup, fontSize = 12.sp, color = p.muted)
                SecondaryButton(pl.exportBtn) { export.launch("furuarc-media-settings.json") }
                SecondaryButton(pl.importBtn) { import.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
                Text(pl.backupNote, fontSize = 12.sp, color = p.muted)
                SecondaryButton(pl.clearPositions) { MediaPrefs.clearPositions(); msg = pl.clearPositionsDone }
                SecondaryButton(pl.resetBtn) { MediaPrefs.reset(); msg = pl.resetDone }
                if (msg.isNotEmpty()) Text(msg, color = p.muted, fontSize = 13.sp)
            }
        }
    }
}
