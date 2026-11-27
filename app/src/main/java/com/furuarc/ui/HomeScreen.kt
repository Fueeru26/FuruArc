package com.furuarc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.furuarc.ui.theme.LocalPalette
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.furuarc.i18n.NavStr
import com.furuarc.i18n.r
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalStr

/** Tab 2 — Game Tools: RPGM Tools / VN Tools cards, then the Supporting Tools label with its menus. */
@Composable
fun SupportScreen(onVnTools: () -> Unit, onRpgmTools: () -> Unit, onEditor: () -> Unit, onInspect: () -> Unit, onKeyFinder: () -> Unit, onFileTools: () -> Unit = {}, onNameOpt: () -> Unit = {}, onMedia: () -> Unit = {}, onTextEdit: () -> Unit = {}, onImageEdit: () -> Unit = {}, onPlayer: () -> Unit = {}, onProjects: () -> Unit = {}) {
    val md = com.furuarc.i18n.mediaStr(LocalLang.current)
    val tx = com.furuarc.i18n.textEditStr(LocalLang.current)
    val ie = com.furuarc.i18n.imageEditStr(LocalLang.current)
    val s = LocalStr.current
    val ck = com.furuarc.i18n.checkStr(LocalLang.current)
    val ft = com.furuarc.i18n.fileToolsStr(LocalLang.current)
    val no = com.furuarc.i18n.nameOptStr(LocalLang.current)
    val nav = NavStr.of(LocalLang.current)
    val pls = com.furuarc.i18n.playerStr(LocalLang.current)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        HomeHeader(nav.tabTools)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp + LocalBarInset.current),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            BigToolCard(AppIcons.Gamepad, s.menuRpgmTitle, modifier = Modifier.weight(1f), imageName = "card_rpgm", onClick = onRpgmTools)
            BigToolCard(AppIcons.MenuBook, s.menuVnTitle, modifier = Modifier.weight(1f), imageName = "card_vn", onClick = onVnTools)
        }
        val pj = com.furuarc.i18n.projectStr(LocalLang.current)
        MenuCard(AppIcons.Checklist, pj.menuTitle, pj.menuSub, onProjects)
        SupportTitle(nav.tabSupport)
        MenuCard(AppIcons.Edit, s.r.edTitle, s.r.edSub, onEditor)
        MenuCard(AppIcons.Description, tx.menuTitle, tx.menuSub, onTextEdit)
        MenuCard(AppIcons.Brush, ie.menuTitle, ie.menuSub, onImageEdit)
        MenuCard(AppIcons.MediaPlayer, pls.menuTitle, pls.menuSub, onPlayer)
        MenuCard(AppIcons.SwapHoriz, md.menuTitle, md.menuSub, onMedia)
        MenuCard(AppIcons.Archive, ck.menuTitle, ck.menuSub, onInspect)
        MenuCard(AppIcons.Build, ft.menuTitle, ft.menuSub, onFileTools)
        MenuCard(AppIcons.RenameOutline, no.menuTitle, no.menuSub, onNameOpt)
        MenuCard(AppIcons.LockOpen, ck.kfTitle, ck.kfSub, onKeyFinder)
    }
    }
}

/** Tab 3 — Settings: Tool Settings (directly on the screen), App Settings menu, Guide, Source, Developer. */
@Composable
fun MoreScreen(vm: com.furuarc.MainViewModel, onSettings: () -> Unit, onGuide: () -> Unit, onSource: () -> Unit, onUpdate: () -> Unit, onDev: () -> Unit) {
    val s = LocalStr.current
    val nav = NavStr.of(LocalLang.current)
    val ab = com.furuarc.i18n.AboutStr.of(LocalLang.current)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        HomeHeader(nav.tabSettings)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp + LocalBarInset.current),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        GroupTitle(nav.grpToolSettings)
        ToolSettingsBlock(vm)
        GameCacheCard()
        GroupTitle(nav.grpApp)
        MenuCard(AppIcons.Gear, s.menuSettingsTitle, s.menuSettingsSub, onSettings)
        MenuCard(AppIcons.MenuBook, ab.title, ab.sub, onGuide)
        MenuCard(AppIcons.Folder, ab.srcTitle, ab.srcSub, onSource)
        MenuCard(AppIcons.Cloud, nav.updTitle, nav.updSub, com.furuarc.core.UpdateChecker.available, onUpdate)
        MenuCard(AppIcons.Gamepad, ab.devTitle, ab.devSub, onDev)
    }
    }
}

/** "Supporting Tools" title: theme text in the middle with lines on the left and right (Translation Area style), same size as the RPGM/VN Tools card titles. */
@Composable
private fun SupportTitle(text: String) = GroupTitle(text)
