package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.furuarc.core.FormatRegistry
import com.furuarc.core.ScriptTool
import com.furuarc.core.ScriptTools
import com.furuarc.i18n.ScriptStrings
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr

/** One card in the engine list: icon = [badge] (archive format / 2 RPGM letters), with [name] below. */
class EngineTile(val badge: String, val name: String, val id: String = "", val onClick: () -> Unit)

/** Engine list: 3 columns per row. Portrait cards (like the game list), name below the card, max 2 lines. */
@Composable
fun EngineGrid(title: String, tiles: List<EngineTile>, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
ScreenHeader(title, onBack)
LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) { 
        items(tiles.chunked(3)) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { t -> Box(Modifier.weight(1f)) { EngineCard(t) } }
                // empty cell so the last card does not stretch
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
}

/** Main VN Tools menu: Unpack/Pack Archive (auto-detect), Script Tools, and Engine List. */
@Composable
fun VnToolsScreen(
    onUnpack: () -> Unit,
    onPack: () -> Unit,
    onScripts: () -> Unit,
    onEngines: () -> Unit,
    onBack: () -> Unit,
    onKeyFinder: () -> Unit = {},
) {
    val s = LocalStr.current
    val ts = ScriptStrings.of(LocalLang.current)
    val ck = com.furuarc.i18n.checkStr(LocalLang.current)
    MenuListScreen(
        s.menuVnTitle,
        listOf(
            MenuSpec(AppIcons.Gamepad, ts.engineListTitle, ts.engineListSub, onEngines),
            MenuSpec(AppIcons.Unarchive, ts.autoUnpackTitle, ts.autoUnpackSub, onUnpack),
            MenuSpec(AppIcons.Archive, ts.autoPackTitle, ts.autoPackSub, onPack),
            MenuSpec(AppIcons.Edit, ts.autoScriptsTitle, ts.autoScriptsSub, onScripts),
        ),
        onBack,
    )
}

/** Automatic Script Tools: Export Text, Import Text, Table of Contents (script/archive type recognized automatically). */
@Composable
fun AutoScriptsScreen(onTool: (ScriptTool) -> Unit, onBack: () -> Unit) {
    val ts = ScriptStrings.of(LocalLang.current)
    MenuListScreen(
        ts.autoScriptsTitle,
        listOf(
            MenuSpec(toolIcon(ScriptTool.EXPORT), ts.title(ScriptTool.EXPORT), ts.autoExportSub) { onTool(ScriptTool.EXPORT) },
            MenuSpec(toolIcon(ScriptTool.IMPORT), ts.title(ScriptTool.IMPORT), ts.autoImportSub) { onTool(ScriptTool.IMPORT) },
            MenuSpec(toolIcon(ScriptTool.LIST), ts.autoListTitle, ts.autoListSub) { onTool(ScriptTool.LIST) },
        ),
        onBack,
    )
}

/** VN Engine List: 3 columns, sorted A–Z by name. */
@Composable
fun VnEngineListScreen(onEngine: (VnEngine) -> Unit, onBack: () -> Unit) {
    val ts = ScriptStrings.of(LocalLang.current)
    val sorted = remember { VN_ENGINES.filter { it.parent == null }.sortedBy { it.name.lowercase() } }
    EngineGrid(ts.engineListTitle, sorted.map { e -> EngineTile(e.badge, e.name, e.id) { onEngine(e) } }, onBack)
}

/** Engine card: portrait card containing the letters [badge] (no name text), engine name below the card (max 2 lines). */
@Composable
private fun EngineCard(t: EngineTile) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    Column(
        Modifier.fillMaxWidth().clip(shape).clickable(onClick = t.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val res = remember(t.id) { if (t.id.isEmpty()) 0 else ctx.resources.getIdentifier("eng_" + t.id, "drawable", ctx.packageName) }
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(lerp(p.card, p.accent, 0.22f), lerp(p.card, p.accent, 0.05f))))
                .border(BorderStroke(1.dp, p.accent.copy(alpha = 0.45f)), shape),
            contentAlignment = Alignment.Center,
        ) {
            if (res != 0) androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(res),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(8.dp))
        // fixed space for 2 lines of name below the card, so all rows have the same height
        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.TopCenter) {
            Text(
                t.name,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = p.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Single engine menu: Unpack/Pack (Archive, Asset, or Decrypt/Encrypt) + engine-specific Script Tools. */
@Composable
fun EngineScreen(
    engine: VnEngine,
    onUnpack: () -> Unit,
    onPack: () -> Unit,
    onTool: (ScriptTool) -> Unit,
    onBack: () -> Unit,
    onVariantUnpack: (VnEngine) -> Unit = {},
    onVariantPack: (VnEngine) -> Unit = {},
    onKeyFinder: (String) -> Unit = {},
) {
    val s = LocalStr.current
    val p = LocalPalette.current
    val lang = LocalLang.current
    val variants = remember(engine.id) { VN_ENGINES.filter { it.parent == engine.id } }
    val ts = ScriptStrings.of(lang)
    val tools = ScriptTools.forEngine(engine.id, engine.rpgm)
    val kind = ScriptTools.kindFor(engine.id)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(engine.name, onBack)
    Column(
        Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(ScreenGap),
    ) {
        when {
            engine.crypt -> {
                MenuCard(AppIcons.LockOpen, s.assetDecryptTitle, s.assetDecryptSub(engine.id == "rpgm_mz"), onUnpack)
                MenuCard(AppIcons.Lock, s.assetEncryptTitle, s.assetEncryptSub(engine.id == "rpgm_mz"), onPack)
            }
            engine.rpgm -> {
                MenuCard(AppIcons.Unarchive, s.assetUnpackTitle, s.engineUnpackSub(engine.ext), onUnpack)
                if (FormatRegistry.byId(engine.formatId).canPack) {
                    MenuCard(AppIcons.Archive, s.assetPackTitle, s.enginePackSub(engine.ext), onPack)
                }
            }
            else -> {
                Text(ts.archiveHeader, color = p.accent, fontWeight = FontWeight.SemiBold)
                MenuCard(AppIcons.Unarchive, s.menuUnpackTitle + " (" + engine.ext.uppercase() + ")", s.engineUnpackSub(engine.ext), onUnpack)
                if (FormatRegistry.byId(engine.formatId).canPack) {
                    MenuCard(AppIcons.Archive, s.menuPackTitle + " (" + engine.ext.uppercase() + ")", s.enginePackSub(engine.ext), onPack)
                }
                variants.forEach { v ->
                    MenuCard(AppIcons.Unarchive, s.menuUnpackTitle + " (" + v.suffix + ")", s.engineUnpackSub(v.ext)) { onVariantUnpack(v) }
                    if (FormatRegistry.byId(v.formatId).canPack) {
                        MenuCard(AppIcons.Archive, s.menuPackTitle + " (" + v.suffix + ")", s.enginePackSub(v.ext)) { onVariantPack(v) }
                    }
                }
            }
        }
        val kfModule = when (engine.id) { "siglus" -> "siglus"; "xp3" -> "xp3"; "catsystem2" -> "catsystem2"; "entisgls" -> "entisgls"; "rpgm_mv", "rpgm_mz" -> "rpgm"; else -> null }
        if (kfModule != null) {
            val ck = com.furuarc.i18n.checkStr(lang)
            MenuCard(AppIcons.LockOpen, ck.kfTitle, ck.kfSub) { onKeyFinder(kfModule) }
        }
        if (tools.isNotEmpty()) {
            Text(ts.toolsHeader, color = p.accent, fontWeight = FontWeight.SemiBold)
            tools.forEach { tool -> MenuCard(toolIcon(tool), ts.title(tool), ts.sub(tool, kind)) { onTool(tool) } }
        }
    }
    }
}
