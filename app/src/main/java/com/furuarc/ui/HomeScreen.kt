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
fun SupportScreen(onVnTools: () -> Unit, onRpgmTools: () -> Unit, onEditor: () -> Unit, onInspect: () -> Unit, onKeyFinder: () -> Unit, onFileTools: () -> Unit = {}, onNameOpt: () -> Unit = {}, onMedia: () -> Unit = {}, onTextEdit: () -> Unit = {}) {
    val md = com.furuarc.i18n.mediaStr(LocalLang.current)
    val tx = com.furuarc.i18n.textEditStr(LocalLang.current)
    val s = LocalStr.current
    val ck = com.furuarc.i18n.checkStr(LocalLang.current)
    val ft = com.furuarc.i18n.fileToolsStr(LocalLang.current)
    val no = com.furuarc.i18n.nameOptStr(LocalLang.current)
    val nav = NavStr.of(LocalLang.current)
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
        SupportTitle(nav.tabSupport)
        MenuCard(AppIcons.Edit, s.r.edTitle, s.r.edSub, onEditor)
        MenuCard(AppIcons.Description, tx.menuTitle, tx.menuSub, onTextEdit)
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
        ToolSettingsBlock(vm)
        GameCacheCard()
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
private fun SupportTitle(text: String) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(2.dp).background(p.accent))
        Text(text, color = p.accent, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 14.dp))
        Box(Modifier.weight(1f).height(2.dp).background(p.accent))
    }
}
