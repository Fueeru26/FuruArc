package com.furuarc

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.furuarc.ui.clearFocusOnOutsideTap
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.furuarc.ui.SupportScreen
import com.furuarc.ui.MoreScreen
import com.furuarc.ui.AppBottomBar
import com.furuarc.ui.NavItem
import com.furuarc.i18n.NavStr
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import com.furuarc.ui.PackScreen
import com.furuarc.ui.SelectScreen
import com.furuarc.ui.SettingsScreen
import com.furuarc.i18n.r
import com.furuarc.ui.AppIcons
import com.furuarc.ui.AssetJobScreen
import com.furuarc.ui.EngineScreen
import com.furuarc.ui.RpgmAssetsScreen
import com.furuarc.ui.RpgmToolsScreen
import com.furuarc.ui.MenuListScreen
import com.furuarc.ui.MenuSpec
import com.furuarc.ui.SaveScreen
import com.furuarc.ui.ValueScreen
import com.furuarc.ui.TranslateScreen
import com.furuarc.ui.TranslateEditScreen
import com.furuarc.ui.TwoMenuScreen
import com.furuarc.ui.UnpackScreen
import com.furuarc.ui.VnToolsScreen
import com.furuarc.ui.VnEngineListScreen
import com.furuarc.ui.AutoScriptsScreen
import com.furuarc.ui.ToolJobScreen
import com.furuarc.core.ScriptTool
import com.furuarc.core.ScriptTools
import com.furuarc.ui.vnEngine
import com.furuarc.ui.InspectScreen
import com.furuarc.ui.PluginScanScreen
import com.furuarc.ui.PluginEditorScreen
import com.furuarc.ui.PluginCheckScreen
import com.furuarc.ui.PluginDiffScreen
import com.furuarc.i18n.pl
import com.furuarc.ui.FileCheckMenuScreen
import com.furuarc.ui.FolderCheckScreen
import com.furuarc.ui.KeyFinderScreen
import com.furuarc.core.KeyFinder
import com.furuarc.ui.CompareScreen
import com.furuarc.ui.TrScanScreen
import com.furuarc.ui.TroubleScreen
import com.furuarc.ui.AboutMenuScreen
import com.furuarc.ui.HowToScreen
import com.furuarc.ui.QnaScreen
import com.furuarc.ui.SourceScreen
import com.furuarc.ui.DeveloperScreen
import com.furuarc.core.GamePlayer
import com.furuarc.core.GameSession
import com.furuarc.ui.theme.FuruArcTheme
import com.furuarc.ui.theme.LocalPalette

enum class Screen {
    HOME, LIBRARY, SUPPORT, MORE, FILECHECK, INSPECT, INSPECT_FOLDER, INSPECT_COMPARE, INSPECT_DUP, INSPECT_HEX, INSPECT_FORMATTEST, INSPECT_TR, PROJECT_MENU, PROJECT_LIST, PROJECT, ABOUT, ABOUT_TROUBLE, ABOUT_HOW, ABOUT_QNA, ABOUT_SRC, ABOUT_DEV,
    VN_TOOLS, VN_ENGINES, AUTO_SCRIPTS, KEYFINDER, TEXTEDIT, TEXTEDIT_PLAIN, TEXTEDIT_STRUCT, TEXTEDIT_BULK, FILETOOLS, NAMEOPT, FILECUT, FILESPLIT, FILEJOIN, FILERENAME, MEDIA, MEDIA_AUDIO, MEDIA_IMAGE, MEDIA_VIDEO, MEDIA_MIXED, MEDIA_TEXT, MEDIA_SPECIAL, ENGINE, UNPACK, PACK, SELECT, BROWSE, SETTINGS,
    GAME, GAME_AUDIO, GAME_IMAGE, GAME_VIDEO, GAME_TEXT,
    MPLAYER, MPLAYER_IMAGE, MPLAYER_AUDIO, MPLAYER_VIDEO, MPLAYER_SETTINGS,
    RPGM_TOOLS, PLUGIN_MENU, PLUGIN_SCAN, PLUGIN_EDIT, PLUGIN_CHECK, PLUGIN_DIFF, RPGM_ASSETS, RPGM_TR_MENU, RPGM_CHEAT_MENU, ASSET_JOB, TR_JOB, SAVE_JOB, VALUE_JOB, TR_EDIT_JOB, TOOL_JOB,
}

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.furuarc.core.MediaPrefs.init(this)
        com.furuarc.core.Projects.load()
        if (savedInstanceState == null) com.furuarc.ui.IncomingFiles.handle(intent)
        setContent {
            val settings by vm.settings.collectAsState()
            val st by vm.state.collectAsState()
            // "Keep screen on while a process is running" (System Settings)
            val mj by MediaJobs.state.collectAsState()
            val keepOn = settings.keepScreenOn && (st.running || mj.running)
            DisposableEffect(keepOn) {
                if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                onDispose { window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
            }
            FuruArcTheme(settings) {
                val p = LocalPalette.current
                // status bar & navigation bar colors follow the theme
                val darkNow = p.dark
                val gradTop = com.furuarc.ui.theme.gradientTop(com.furuarc.ui.theme.UiStyle.of(settings.uiStyle), p)
                val gradient = gradTop != null
                val blossom = com.furuarc.ui.theme.UiStyle.of(settings.uiStyle) == com.furuarc.ui.theme.UiStyle.BLOSSOM
                val top = gradTop ?: p.background
                SideEffect {
                    window.statusBarColor = top.toArgb()
                    val controller = WindowCompat.getInsetsController(window, window.decorView)
                    controller?.isAppearanceLightStatusBars = !darkNow
                    controller?.isAppearanceLightNavigationBars = !darkNow
                }
                Box(Modifier.fillMaxSize()) {
                    if (blossom) {
                        val bgCtx = androidx.compose.ui.platform.LocalContext.current
                        val bgRes = remember(darkNow) { bgCtx.resources.getIdentifier(if (darkNow) "bg_blossom_dark" else "bg_blossom_light", "drawable", bgCtx.packageName) }
                        if (bgRes != 0) androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(bgRes),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            alignment = androidx.compose.ui.Alignment.TopCenter,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    Surface(
                        Modifier.fillMaxSize().then(
                            if (gradient) Modifier.background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(top, p.background))) else Modifier
                        ),
                        color = if (gradient || blossom) androidx.compose.ui.graphics.Color.Transparent else p.background,
                    ) { App(vm) }
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        com.furuarc.ui.IncomingFiles.handle(intent)
    }

    override fun onDestroy() {
        // Without "Run in Background", the process is not continued after the app is fully closed.
        if (isFinishing && !Engine.bgEnabled && Engine.isRunning()) Engine.cancel()
        super.onDestroy()
    }
}

@OptIn(androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun App(vm: MainViewModel) {
    val s = com.furuarc.ui.theme.LocalStr.current
    // If a process is still running when the app is opened again, go straight back to its process screen.
    // Mode: 1 unpack, 2 pack, 3 decrypt assets, 4 encrypt assets, 5/6 export/import text, 7/8 export/import save,
    // 9 value editor, 10 translation editor, 20..25 VN script tools.
    var engineId by remember { mutableStateOf(vm.engineId() ?: "pfs") }
    val navState = remember {
        NavState(
            when (vm.mode()) {
                1 -> Screen.UNPACK
                2 -> Screen.PACK
                3, 4 -> Screen.ASSET_JOB
                5, 6 -> Screen.TR_JOB
                7, 8 -> Screen.SAVE_JOB
                9 -> Screen.VALUE_JOB
                10 -> Screen.TR_EDIT_JOB
                in 20..25 -> Screen.TOOL_JOB
                else -> Screen.HOME
            },
        )
    }
    var screen by navState
    val engine = vnEngine(engineId)
    // VN Tools > Unpack/Pack/Script Tools automatically use a special engine id (no specific engine)
    val auto = engineId == ScriptTools.AUTO
    val engineOrNull = if (auto) null else engine

    // from the Unpack/Pack screen (or Decrypt/Encrypt/script tools) go back to the originating menu
    val toEngineMenu = {
        vm.setMode(0)
        if (com.furuarc.core.Projects.active != null) {
            // the tool was opened from a project: Back returns to it (a successful run marks the step as done)
            val es = com.furuarc.Engine.state.value
            com.furuarc.core.Projects.autoMark(es.done && es.summary?.let { it.failed == 0 && it.ok > 0 } == true)
            screen = Screen.PROJECT
        } else {
            engine.parent?.let { engineId = it }
            screen = if (!auto) Screen.ENGINE else if (screen == Screen.TOOL_JOB) Screen.AUTO_SCRIPTS else Screen.VN_TOOLS
        }
    }
    // from an RPGM job screen go back to its parent menu
    val toParent = { parent: Screen ->
        vm.setMode(0)
        screen = if (parent == Screen.SUPPORT && com.furuarc.core.Projects.active != null) Screen.PROJECT else parent
    }
    var projectId by remember { mutableStateOf<String?>(null) }
    var projectType by remember { mutableStateOf(0) }
    // a media converter / folder check opened from a project: Back returns to it (a converter run without failures marks the step as done)
    val backToProject = {
        val ms = MediaJobs.state.value
        com.furuarc.core.Projects.autoMark(ms.finished && ms.failed == 0 && ms.ok > 0)
        MediaJobs.clear()
        screen = Screen.PROJECT
    }
    var kfEngine by remember { mutableStateOf(KeyFinder.ALL) }
    var kfFrom by remember { mutableStateOf(Screen.VN_TOOLS) }
    var browseFromInspect by remember { mutableStateOf(false) }
    var inspectFromFolder by remember { mutableStateOf(false) }
    BackHandler(enabled = screen != Screen.HOME) {
        when (screen) {
            Screen.BROWSE -> screen = if (browseFromInspect) Screen.INSPECT else Screen.UNPACK
            Screen.ABOUT_HOW, Screen.ABOUT_QNA, Screen.ABOUT_TROUBLE -> screen = Screen.ABOUT
            Screen.ABOUT_SRC, Screen.ABOUT_DEV -> screen = Screen.MORE
            Screen.INSPECT -> {
                InspectJobs.clear(); vm.reset()
                screen = if (inspectFromFolder) Screen.INSPECT_FOLDER else Screen.FILECHECK
                inspectFromFolder = false
            }
            Screen.INSPECT_FOLDER -> { FolderJobs.clear(); if (com.furuarc.core.Projects.active != null) { com.furuarc.core.Projects.autoMark(false); screen = Screen.PROJECT } else screen = Screen.FILECHECK }
            Screen.KEYFINDER -> { KeyFinderJobs.clear(); screen = kfFrom }
            Screen.GAME -> { GameSession.close(); GamePlayer.stop(); screen = Screen.LIBRARY }
            Screen.GAME_AUDIO, Screen.GAME_IMAGE, Screen.GAME_VIDEO, Screen.GAME_TEXT -> { GamePlayer.stop(); screen = Screen.GAME }
            Screen.VN_TOOLS, Screen.RPGM_TOOLS -> screen = Screen.SUPPORT
            Screen.FILETOOLS -> screen = Screen.SUPPORT
            Screen.NAMEOPT -> screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT
            Screen.MEDIA -> screen = Screen.SUPPORT
            Screen.MPLAYER -> screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT
            Screen.MPLAYER_IMAGE, Screen.MPLAYER_AUDIO, Screen.MPLAYER_VIDEO, Screen.MPLAYER_SETTINGS -> screen = Screen.MPLAYER
            Screen.TEXTEDIT -> screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT
            Screen.TEXTEDIT_PLAIN, Screen.TEXTEDIT_STRUCT, Screen.TEXTEDIT_BULK -> screen = Screen.TEXTEDIT
            Screen.MEDIA_AUDIO, Screen.MEDIA_IMAGE, Screen.MEDIA_VIDEO, Screen.MEDIA_MIXED, Screen.MEDIA_TEXT -> { if (com.furuarc.core.Projects.active != null) backToProject() else { MediaJobs.clear(); screen = Screen.MEDIA } }
            Screen.MEDIA_SPECIAL -> { MediaJobs.clear(); if (com.furuarc.ui.SpecialNav.engine != null) com.furuarc.ui.SpecialNav.engine = null else screen = Screen.MEDIA }
            Screen.FILERENAME -> screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILETOOLS
            Screen.FILECUT, Screen.FILESPLIT, Screen.FILEJOIN -> screen = Screen.FILETOOLS
            Screen.INSPECT_COMPARE -> { CompareJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
            Screen.INSPECT_DUP -> screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK
            Screen.INSPECT_HEX -> screen = Screen.FILECHECK
            Screen.INSPECT_FORMATTEST -> { FormatTestJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
            Screen.PROJECT_MENU -> screen = Screen.SUPPORT
            Screen.PROJECT_LIST -> screen = Screen.PROJECT_MENU
            Screen.PROJECT -> { com.furuarc.core.Projects.active = null; screen = Screen.PROJECT_LIST }
            Screen.INSPECT_TR -> { TrScanJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
            Screen.SELECT -> screen = Screen.UNPACK
            Screen.UNPACK, Screen.PACK -> toEngineMenu()
            Screen.ASSET_JOB, Screen.TOOL_JOB -> toEngineMenu()
            Screen.ABOUT, Screen.SETTINGS -> screen = Screen.MORE
            Screen.FILECHECK -> screen = Screen.SUPPORT
            Screen.ENGINE -> screen = if (engine.rpgm) Screen.RPGM_ASSETS else Screen.VN_ENGINES
            Screen.VN_ENGINES, Screen.AUTO_SCRIPTS -> screen = Screen.VN_TOOLS
            Screen.RPGM_ASSETS, Screen.RPGM_TR_MENU, Screen.RPGM_CHEAT_MENU, Screen.PLUGIN_MENU -> screen = Screen.RPGM_TOOLS
            Screen.PLUGIN_SCAN -> { PluginScanJobs.clear(); screen = Screen.PLUGIN_MENU }
            Screen.PLUGIN_EDIT -> { PluginListJobs.clear(); screen = Screen.PLUGIN_MENU }
            Screen.PLUGIN_CHECK -> { PluginCheckJobs.clear(); screen = Screen.PLUGIN_MENU }
            Screen.PLUGIN_DIFF -> { PluginDiffJobs.clear(); screen = Screen.PLUGIN_MENU }
            Screen.TR_JOB -> toParent(Screen.RPGM_TR_MENU)
            Screen.TR_EDIT_JOB -> toParent(Screen.SUPPORT)
            Screen.SAVE_JOB, Screen.VALUE_JOB -> toParent(Screen.RPGM_CHEAT_MENU)
            else -> screen = Screen.HOME
        }
    }

    fun enter(mode: Int, next: Screen, engineIdArg: String? = null) {
        Pipeline.ignoreBackup = false
        vm.reset()
        vm.setMode(mode, engineIdArg)
        screen = next
    }

    // Home tab: open a feature by id (see ui/FeatureCatalog); the screen's usual "back" then goes to its parent menu
    fun openFeature0(id: String) {
        com.furuarc.ui.HomeStore.touch(id)
        if (id.startsWith("engine:")) { engineId = id.substring(7); screen = Screen.ENGINE; return }
        if (id.startsWith("special:")) {
            MediaJobs.clear()
            com.furuarc.ui.SpecialNav.engine = com.furuarc.core.SpecialConv.ENGINES.firstOrNull { it.id == id.substring(8) }
            screen = Screen.MEDIA_SPECIAL
            return
        }
        when (id) {
            "library" -> screen = Screen.LIBRARY
            "vn" -> screen = Screen.VN_TOOLS
            "rpgm" -> screen = Screen.RPGM_TOOLS
            "editor" -> enter(10, Screen.TR_EDIT_JOB)
            "textedit" -> screen = Screen.TEXTEDIT
            "textedit.plain" -> screen = Screen.TEXTEDIT_PLAIN
            "textedit.struct" -> screen = Screen.TEXTEDIT_STRUCT
            "textedit.bulk" -> screen = Screen.TEXTEDIT_BULK
            "media" -> screen = Screen.MEDIA
            "player" -> screen = Screen.MPLAYER
            "player.image" -> screen = Screen.MPLAYER_IMAGE
            "player.audio" -> screen = Screen.MPLAYER_AUDIO
            "player.video" -> screen = Screen.MPLAYER_VIDEO
            "player.settings" -> screen = Screen.MPLAYER_SETTINGS
            "media.audio" -> { MediaJobs.clear(); screen = Screen.MEDIA_AUDIO }
            "media.image" -> { MediaJobs.clear(); screen = Screen.MEDIA_IMAGE }
            "media.video" -> { MediaJobs.clear(); screen = Screen.MEDIA_VIDEO }
            "media.mixed" -> { MediaJobs.clear(); screen = Screen.MEDIA_MIXED }
            "media.text" -> { MediaJobs.clear(); screen = Screen.MEDIA_TEXT }
            "media.special" -> { MediaJobs.clear(); com.furuarc.ui.SpecialNav.engine = null; screen = Screen.MEDIA_SPECIAL }
            "filecheck" -> { vm.reset(); InspectJobs.clear(); screen = Screen.FILECHECK }
            "filecheck.file" -> { vm.reset(); InspectJobs.clear(); inspectFromFolder = false; screen = Screen.INSPECT }
            "filecheck.folder" -> screen = Screen.INSPECT_FOLDER
            "filecheck.compare" -> screen = Screen.INSPECT_COMPARE
            "filecheck.dup" -> screen = Screen.INSPECT_DUP
            "filecheck.hex" -> screen = Screen.INSPECT_HEX
            "filecheck.formattest" -> { FormatTestJobs.clear(); screen = Screen.INSPECT_FORMATTEST }
            "filecheck.tr" -> screen = Screen.INSPECT_TR
            "filetools" -> screen = Screen.FILETOOLS
            "filetools.cut" -> screen = Screen.FILECUT
            "filetools.split" -> screen = Screen.FILESPLIT
            "filetools.join" -> screen = Screen.FILEJOIN
            "filetools.rename" -> screen = Screen.FILERENAME
            "nameopt" -> screen = Screen.NAMEOPT
            "keyfinder" -> { kfEngine = KeyFinder.ALL; kfFrom = Screen.SUPPORT; screen = Screen.KEYFINDER }
            "vn.engines" -> screen = Screen.VN_ENGINES
            "vn.unpack" -> { enter(1, Screen.UNPACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO }
            "vn.pack" -> { enter(2, Screen.PACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO }
            "vn.scripts" -> screen = Screen.AUTO_SCRIPTS
            "vn.script.export" -> { engineId = ScriptTools.AUTO; enter(ScriptTool.EXPORT.mode, Screen.TOOL_JOB, ScriptTools.AUTO) }
            "vn.script.import" -> { engineId = ScriptTools.AUTO; enter(ScriptTool.IMPORT.mode, Screen.TOOL_JOB, ScriptTools.AUTO) }
            "vn.script.list" -> { engineId = ScriptTools.AUTO; enter(ScriptTool.LIST.mode, Screen.TOOL_JOB, ScriptTools.AUTO) }
            "rpgm.assets" -> screen = Screen.RPGM_ASSETS
            "rpgm.translate" -> screen = Screen.RPGM_TR_MENU
            "rpgm.tr.export" -> enter(5, Screen.TR_JOB)
            "rpgm.tr.import" -> enter(6, Screen.TR_JOB)
            "rpgm.cheat" -> screen = Screen.RPGM_CHEAT_MENU
            "rpgm.save.export" -> enter(7, Screen.SAVE_JOB)
            "rpgm.save.import" -> enter(8, Screen.SAVE_JOB)
            "rpgm.value" -> enter(9, Screen.VALUE_JOB)
            "rpgm.plugins" -> screen = Screen.PLUGIN_MENU
            "rpgm.plugins.scan" -> { PluginScanJobs.clear(); screen = Screen.PLUGIN_SCAN }
            "rpgm.plugins.edit" -> { PluginListJobs.clear(); screen = Screen.PLUGIN_EDIT }
            "rpgm.plugins.check" -> { PluginCheckJobs.clear(); screen = Screen.PLUGIN_CHECK }
            "rpgm.plugins.diff" -> { PluginDiffJobs.clear(); screen = Screen.PLUGIN_DIFF }
            "settings" -> screen = Screen.SETTINGS
            "guide" -> screen = Screen.ABOUT
            "howto" -> screen = Screen.ABOUT_HOW
            "qna" -> screen = Screen.ABOUT_QNA
            "trouble" -> screen = Screen.ABOUT_TROUBLE
            "source" -> screen = Screen.ABOUT_SRC
            "dev" -> screen = Screen.ABOUT_DEV
        }
    }
    fun openFeature(id: String) {
        com.furuarc.ui.PendingFile.clear()
        navState.clearHome()
        openFeature0(id)
        navState.enterFromHome()
    }
    // Home tab "Find features by file": open the feature and load the picked file into it right away
    fun openFeatureWithFile(id: String, uri: android.net.Uri) {
        com.furuarc.ui.PendingFile.clear()
        navState.clearHome()
        val e = if (id.startsWith("engine:")) vnEngine(id.substring(7)) else null
        if (e != null) {
            // plain VN engine: go straight to its unpack screen
            com.furuarc.ui.HomeStore.touch(id)
            engineId = e.id
            enter(1, Screen.UNPACK, e.id)
            vm.setArchives(listOf(uri), e.formatId)
        } else {
            openFeature0(id)
            when (id) {
                "vn.unpack" -> vm.setArchives(listOf(uri), null)
                "vn.script.list" -> vm.setArchive(uri, null)
                "editor", "rpgm.save.export", "rpgm.save.import" -> vm.rpgmInputFile(uri)
                "player.image", "player.audio", "player.video" ->
                    com.furuarc.ui.PlayerHub.openSingle(when (id) { "player.image" -> 1; "player.audio" -> 2; else -> 3 }, com.furuarc.ui.itemOfUri(Engine.app, uri), "")
                "filecheck.file" -> InspectJobs.inspect(uri)
                // Text Editor: the file opens read-only in "Lihat" (Structured Text picks the editor of its format)
                "textedit.plain" -> com.furuarc.ui.TextViewHandoff.set(uri, "plain")
                "textedit.struct" -> com.furuarc.ui.TextViewHandoff.kindFor(com.furuarc.core.MediaScan.ext(com.furuarc.ui.itemOfUri(Engine.app, uri).name))
                    ?.takeIf { it != "plain" }?.let { com.furuarc.ui.TextViewHandoff.set(uri, it) }
                "filecheck.tr" -> TrScanJobs.open(uri)
                "rpgm.plugins.scan" -> PluginScanJobs.pickPluginsJs(uri)
                "rpgm.plugins.edit" -> PluginListJobs.openPluginsJs(uri)
                "rpgm.plugins.check" -> PluginCheckJobs.pickJs(uri)
                // screens that keep their own file state pick the file up themselves when they open
                else -> com.furuarc.ui.PendingFile.set(id, uri)
            }
        }
        navState.enterFromHome()
    }
    SideEffect {
        com.furuarc.ui.PlayerHub.openInspect = { u -> openFeatureWithFile("filecheck.file", u) }
        com.furuarc.ui.PlayerHub.navigate = { k -> screen = when (k) { 1 -> Screen.MPLAYER_IMAGE; 2 -> Screen.MPLAYER_AUDIO; else -> Screen.MPLAYER_VIDEO } }
    }
    // "Open with…" / "Share": media goes to the Media Player, everything else to the feature that matches the file type
    val allFeatures = com.furuarc.ui.FeatureCatalog.rememberAll()
    val inCtx = LocalContext.current
    val incomingToken = com.furuarc.ui.IncomingFiles.token
    LaunchedEffect(incomingToken) {
        val uris = com.furuarc.ui.IncomingFiles.take() ?: return@LaunchedEffect
        val items = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { uris.map { com.furuarc.ui.itemOfUri(inCtx, it) } }
        val first = items[0]
        var kind = com.furuarc.core.MediaScan.kindOf(first.name)
        if (kind == 0) {
            val mt = try { inCtx.contentResolver.getType(uris[0]) ?: "" } catch (e: Exception) { "" }
            kind = when { mt.startsWith("image/") -> 1; mt.startsWith("audio/") -> 2; mt.startsWith("video/") -> 3; else -> 0 }
        }
        navState.clearHome()
        if (kind in 1..3) {
            val same = items.filter { com.furuarc.core.MediaScan.kindOf(it.name) == kind || items.size == 1 }
            // temporary: the stored gallery / queue is restored when the screen is left
            com.furuarc.ui.PlayerHub.openTemp(kind, same.ifEmpty { listOf(first) })
            screen = when (kind) { 1 -> Screen.MPLAYER_IMAGE; 2 -> Screen.MPLAYER_AUDIO; else -> Screen.MPLAYER_VIDEO }
        } else {
            val ext = com.furuarc.core.MediaScan.ext(first.name)
            val textKind = com.furuarc.ui.TextViewHandoff.kindFor(ext)
            if (textKind != null) {
                // .txt -> View of Plain Text; a registered structured format -> View of its Structured Text editor
                openFeatureWithFile(if (textKind == "plain") "textedit.plain" else "textedit.struct", uris[0])
            } else {
                val hit = com.furuarc.ui.FeatureCatalog.forExtension(allFeatures, ext)
                    .firstOrNull { !it.id.startsWith("media.") && com.furuarc.ui.FeatureCatalog.canPreload(it.id) }
                if (hit != null) {
                    openFeatureWithFile(hit.id, uris[0])
                } else {
                    // unknown format: a text file opens in View of Plain Text, anything else (binary) in File Inspection
                    val isText = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { com.furuarc.ui.looksLikeText(inCtx, uris[0]) }
                    openFeatureWithFile(if (isText) "textedit.plain" else "filecheck.file", uris[0])
                }
            }
        }
    }
    // Home tab: fill "Recently used" when a screen that is a feature on its own is opened
    val appCtx = LocalContext.current
    remember { com.furuarc.ui.HomeStore.init(appCtx); 0 }
    var showUpdate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { com.furuarc.core.UpdateChecker.autoCheck(appCtx) }
    LaunchedEffect(screen) {
        val ep = if (screen == Screen.ENGINE) vnEngine(engineId).parent else null
        com.furuarc.ui.FeatureCatalog.idFor(screen, vm.mode(), engineId, ep)?.let { com.furuarc.ui.HomeStore.touch(it) }
    }

    val p = LocalPalette.current
    val nav = NavStr.of(com.furuarc.ui.theme.LocalLang.current)
    val tab = when (screen) { Screen.HOME -> 0; Screen.LIBRARY -> 1; Screen.SUPPORT -> 2; Screen.MORE -> 3; else -> -1 }
    val navItems = listOf(
        NavItem(Icons.Default.Home, nav.tabHome),
        NavItem(AppIcons.Library, nav.tabAssets),
        NavItem(AppIcons.Gamepad, nav.tabTools),
        NavItem(Icons.Default.Settings, nav.tabSettings, com.furuarc.core.UpdateChecker.available),
    )
    // the system navigation bar blends with the app's bottom navigation when shown
    val activity = LocalContext.current as? Activity
    SideEffect {
        val navSt = com.furuarc.ui.theme.AppStyle.nav
        val solidBar = tab >= 0 && navSt != com.furuarc.ui.theme.NavStyle.FLOATING && navSt != com.furuarc.ui.theme.NavStyle.FLOAT_ICONS && navSt != com.furuarc.ui.theme.NavStyle.GLASS && navSt != com.furuarc.ui.theme.NavStyle.FAB
        activity?.window?.navigationBarColor = (if (solidBar) p.card else p.background).toArgb()
    }
    // workflow: result of Media Asset Converter (Overwrite Original Files) -> Repack with that folder already selected
    SideEffect {
        Pipeline.onRepack = { uri ->
            enter(2, Screen.PACK, ScriptTools.AUTO)
            engineId = ScriptTools.AUTO
            vm.setPackFolder(uri)
            Pipeline.ignoreBackup = true
        }
    }
    // each screen's scroll position is kept when returning from a menu (going forward = start from the top)
    val scrollKeeper = remember { com.furuarc.ui.ScrollKeeper() }
    remember(screen) { scrollKeeper.onNavigate(screen.name) }
    androidx.compose.runtime.LaunchedEffect(screen) { com.furuarc.ui.HeaderState.collapsed = false }
    val headerScroll = remember { com.furuarc.ui.HeaderScrollConnection() }
    val onNavSelect: (Int) -> Unit = { i ->
        com.furuarc.core.Projects.active = null
        val target = when (i) { 0 -> Screen.HOME; 1 -> Screen.LIBRARY; 2 -> Screen.SUPPORT; else -> Screen.MORE }
        navState.clearHome()
        if (target != screen) scrollKeeper.clearAll()
        screen = target
    }
    val hostView = androidx.compose.ui.platform.LocalView.current
    val focusMgr = androidx.compose.ui.platform.LocalFocusManager.current
    val barInset = if (tab < 0) 0.dp else when (com.furuarc.ui.theme.AppStyle.nav) {
        com.furuarc.ui.theme.NavStyle.FLOATING -> 92.dp
        com.furuarc.ui.theme.NavStyle.FLOAT_ICONS -> 94.dp
        com.furuarc.ui.theme.NavStyle.GLASS -> 92.dp
        com.furuarc.ui.theme.NavStyle.FAB -> 76.dp
        else -> 0.dp
    }
    val jellyScope = androidx.compose.runtime.rememberCoroutineScope()
    val jellyDensity = androidx.compose.ui.platform.LocalDensity.current.density
    val jelly = remember(jellyDensity) { com.furuarc.ui.ScrollJelly(jellyScope, 3200f * jellyDensity) }
    CompositionLocalProvider(com.furuarc.ui.LocalScrollKeeper provides scrollKeeper, com.furuarc.ui.LocalBarInset provides barInset, com.furuarc.ui.LocalScrollJelly provides jelly) {
    Column(Modifier.fillMaxSize().clearFocusOnOutsideTap()) {
        Box(Modifier.weight(1f).fillMaxWidth().nestedScroll(headerScroll).nestedScroll(jelly.connection)) {
            androidx.compose.animation.AnimatedContent(
                targetState = screen,
                // Tyranor-style: tabs slide sideways, menus/pages rise from the bottom, back sinks down (see ScreenTransitions)
                transitionSpec = { ScreenTransitions.spec(this) },
            ) { sc ->
            when (sc) {
                Screen.HOME -> com.furuarc.ui.StartScreen(onOpen = { id -> openFeature(id) }, onOpenWithFile = { id, u -> openFeatureWithFile(id, u) })
                Screen.LIBRARY -> com.furuarc.ui.LibraryScreen(
                    onOpen = { e -> GameSession.open(e); screen = Screen.GAME },
                )
                Screen.GAME -> com.furuarc.ui.GameScreen(
                    onAudio = { screen = Screen.GAME_AUDIO },
                    onImage = { screen = Screen.GAME_IMAGE },
                    onVideo = { screen = Screen.GAME_VIDEO },
                    onText = { screen = Screen.GAME_TEXT },
                    onBack = { GameSession.close(); GamePlayer.stop(); screen = Screen.LIBRARY },
                )
                Screen.GAME_AUDIO -> com.furuarc.ui.GameAudioScreen { GamePlayer.stop(); screen = Screen.GAME }
                Screen.GAME_IMAGE -> com.furuarc.ui.GameImageScreen { screen = Screen.GAME }
                Screen.GAME_VIDEO -> com.furuarc.ui.GameVideoScreen { screen = Screen.GAME }
                Screen.GAME_TEXT -> com.furuarc.ui.GameTextScreen { screen = Screen.GAME }
                Screen.SUPPORT -> SupportScreen(
                    onVnTools = { screen = Screen.VN_TOOLS },
                    onRpgmTools = { screen = Screen.RPGM_TOOLS },
                    onEditor = { enter(10, Screen.TR_EDIT_JOB) },
                    onInspect = { vm.reset(); InspectJobs.clear(); screen = Screen.FILECHECK },
                    onKeyFinder = { kfEngine = KeyFinder.ALL; kfFrom = Screen.SUPPORT; screen = Screen.KEYFINDER },
                    onFileTools = { screen = Screen.FILETOOLS },
                    onNameOpt = { screen = Screen.NAMEOPT },
                    onTextEdit = { screen = Screen.TEXTEDIT },
                    onMedia = { screen = Screen.MEDIA },
                    onPlayer = { screen = Screen.MPLAYER },
                    onProjects = { screen = Screen.PROJECT_MENU },
                )
                Screen.MPLAYER -> com.furuarc.ui.PlayerMenuScreen(
                    onImage = { screen = Screen.MPLAYER_IMAGE },
                    onAudio = { screen = Screen.MPLAYER_AUDIO },
                    onVideo = { screen = Screen.MPLAYER_VIDEO },
                    onSettings = { screen = Screen.MPLAYER_SETTINGS },
                    onBack = { screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT },
                )
                Screen.MPLAYER_IMAGE -> com.furuarc.ui.PlayerListScreen(1) { screen = Screen.MPLAYER }
                Screen.MPLAYER_AUDIO -> com.furuarc.ui.PlayerListScreen(2) { screen = Screen.MPLAYER }
                Screen.MPLAYER_VIDEO -> com.furuarc.ui.PlayerListScreen(3) { screen = Screen.MPLAYER }
                Screen.MPLAYER_SETTINGS -> com.furuarc.ui.PlayerSettingsScreen { screen = Screen.MPLAYER }
                Screen.TEXTEDIT -> com.furuarc.ui.TextEditMenuScreen(
                    onPlain = { screen = Screen.TEXTEDIT_PLAIN },
                    onStruct = { screen = Screen.TEXTEDIT_STRUCT },
                    onBulk = { screen = Screen.TEXTEDIT_BULK },
                    onBack = { screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT },
                )
                Screen.TEXTEDIT_PLAIN -> com.furuarc.ui.PlainTextScreen(onBack = { screen = Screen.TEXTEDIT })
                Screen.TEXTEDIT_STRUCT -> com.furuarc.ui.StructuredHubScreen { screen = Screen.TEXTEDIT }
                Screen.TEXTEDIT_BULK -> com.furuarc.ui.BulkReplaceScreen { screen = Screen.TEXTEDIT }
                Screen.MEDIA -> com.furuarc.ui.MediaMenuScreen(
                    onAudio = { MediaJobs.clear(); screen = Screen.MEDIA_AUDIO },
                    onImage = { MediaJobs.clear(); screen = Screen.MEDIA_IMAGE },
                    onVideo = { MediaJobs.clear(); screen = Screen.MEDIA_VIDEO },
                    onText = { MediaJobs.clear(); screen = Screen.MEDIA_TEXT },
                    onSpecial = { MediaJobs.clear(); com.furuarc.ui.SpecialNav.engine = null; screen = Screen.MEDIA_SPECIAL },
                    onMixed = { MediaJobs.clear(); screen = Screen.MEDIA_MIXED },
                    onBack = { screen = Screen.SUPPORT },
                )
                Screen.MEDIA_AUDIO -> com.furuarc.ui.AudioConvertScreen { if (com.furuarc.core.Projects.active != null) backToProject() else { MediaJobs.clear(); screen = Screen.MEDIA } }
                Screen.MEDIA_IMAGE -> com.furuarc.ui.ImageConvertScreen { if (com.furuarc.core.Projects.active != null) backToProject() else { MediaJobs.clear(); screen = Screen.MEDIA } }
                Screen.MEDIA_VIDEO -> com.furuarc.ui.VideoConvertScreen { if (com.furuarc.core.Projects.active != null) backToProject() else { MediaJobs.clear(); screen = Screen.MEDIA } }
                Screen.MEDIA_MIXED -> com.furuarc.ui.MixedConvertScreen { if (com.furuarc.core.Projects.active != null) backToProject() else { MediaJobs.clear(); screen = Screen.MEDIA } }
                Screen.MEDIA_TEXT -> com.furuarc.ui.TextConvertScreen { MediaJobs.clear(); screen = Screen.MEDIA }
                Screen.MEDIA_SPECIAL -> com.furuarc.ui.SpecialConvertScreen { MediaJobs.clear(); com.furuarc.ui.SpecialNav.engine = null; screen = Screen.MEDIA }
                Screen.NAMEOPT -> com.furuarc.ui.NameOptScreen(
                    onBack = { screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.SUPPORT },
                    onMixed = { MediaJobs.clear(); screen = Screen.MEDIA_MIXED },
                )
                Screen.FILETOOLS -> com.furuarc.ui.FileToolsMenuScreen(
                    onCut = { screen = Screen.FILECUT },
                    onSplit = { screen = Screen.FILESPLIT },
                    onJoin = { screen = Screen.FILEJOIN },
                    onRename = { screen = Screen.FILERENAME },
                    onBack = { screen = Screen.SUPPORT },
                )
                Screen.FILECUT -> com.furuarc.ui.FileCutterScreen { screen = Screen.FILETOOLS }
                Screen.FILESPLIT -> com.furuarc.ui.FileSplitterScreen { screen = Screen.FILETOOLS }
                Screen.FILEJOIN -> com.furuarc.ui.FileJoinerScreen { screen = Screen.FILETOOLS }
                Screen.FILERENAME -> com.furuarc.ui.BatchRenameScreen { screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILETOOLS }
                Screen.MORE -> MoreScreen(
                    vm,
                    onSettings = { screen = Screen.SETTINGS },
                    onGuide = { screen = Screen.ABOUT },
                    onSource = { screen = Screen.ABOUT_SRC },
                    onUpdate = { com.furuarc.core.UpdateChecker.check(appCtx); showUpdate = true },
                    onDev = { screen = Screen.ABOUT_DEV },
                )
                Screen.VN_TOOLS -> VnToolsScreen(
                    onUnpack = { enter(1, Screen.UNPACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO },
                    onPack = { enter(2, Screen.PACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO },
                    onScripts = { screen = Screen.AUTO_SCRIPTS },
                    onEngines = { screen = Screen.VN_ENGINES },
                    onBack = { screen = Screen.SUPPORT },
                )
                Screen.KEYFINDER -> KeyFinderScreen(kfEngine) { KeyFinderJobs.clear(); screen = kfFrom }
                Screen.VN_ENGINES -> VnEngineListScreen(
                    onEngine = { e -> engineId = e.id; screen = Screen.ENGINE },
                    onBack = { screen = Screen.VN_TOOLS },
                )
                Screen.AUTO_SCRIPTS -> AutoScriptsScreen(
                    onTool = { tool -> engineId = ScriptTools.AUTO; enter(tool.mode, Screen.TOOL_JOB, ScriptTools.AUTO) },
                    onBack = { screen = Screen.VN_TOOLS },
                )
                Screen.ENGINE -> EngineScreen(
                    engine = engine.parent?.let { vnEngine(it) } ?: engine,
                    onKeyFinder = { m -> kfEngine = m; kfFrom = Screen.ENGINE; screen = Screen.KEYFINDER },
                    onVariantUnpack = { v -> engineId = v.id; enter(1, Screen.UNPACK, v.id) },
                    onVariantPack = { v -> engineId = v.id; enter(2, Screen.PACK, v.id) },
                    onUnpack = {
                        if (engine.crypt) enter(3, Screen.ASSET_JOB, engine.id) else enter(1, Screen.UNPACK, engine.id)
                    },
                    onPack = {
                        if (engine.crypt) enter(4, Screen.ASSET_JOB, engine.id) else enter(2, Screen.PACK, engine.id)
                    },
                    onTool = { tool -> enter(tool.mode, Screen.TOOL_JOB, engine.parent ?: engine.id) },
                    onBack = { screen = if (engine.rpgm) Screen.RPGM_ASSETS else Screen.VN_ENGINES },
                )
                Screen.TOOL_JOB -> ToolJobScreen(vm, engineOrNull, ScriptTool.ofMode(vm.mode()) ?: ScriptTool.EXPORT, onBack = toEngineMenu, onTranslateText = { u -> enter(10, Screen.TR_EDIT_JOB); vm.rpgmInputFile(u) })
                Screen.UNPACK -> UnpackScreen(vm, engineOrNull, onSelect = { screen = Screen.SELECT }, onBrowse = { browseFromInspect = false; screen = Screen.BROWSE }, onBack = toEngineMenu)
                Screen.SELECT -> SelectScreen(vm, { screen = Screen.UNPACK })
                Screen.BROWSE -> SelectScreen(vm, { screen = if (browseFromInspect) Screen.INSPECT else Screen.UNPACK }, browse = true)
                Screen.ABOUT -> AboutMenuScreen(
                    onHow = { screen = Screen.ABOUT_HOW }, onQna = { screen = Screen.ABOUT_QNA }, onTrouble = { screen = Screen.ABOUT_TROUBLE },
                    onBack = { screen = Screen.MORE },
                )
                Screen.ABOUT_HOW -> HowToScreen { screen = Screen.ABOUT }
                Screen.ABOUT_QNA -> QnaScreen { screen = Screen.ABOUT }
                Screen.ABOUT_TROUBLE -> TroubleScreen { screen = Screen.ABOUT }
                Screen.ABOUT_SRC -> SourceScreen { screen = Screen.MORE }
                Screen.ABOUT_DEV -> DeveloperScreen(vm) { screen = Screen.MORE }
                Screen.FILECHECK -> FileCheckMenuScreen(
                    onFile = { inspectFromFolder = false; screen = Screen.INSPECT },
                    onFolder = { screen = Screen.INSPECT_FOLDER },
                    onCompare = { screen = Screen.INSPECT_COMPARE },
                    onDup = { screen = Screen.INSPECT_DUP },
                    onHex = { screen = Screen.INSPECT_HEX },
                    onFormatTest = { FormatTestJobs.clear(); screen = Screen.INSPECT_FORMATTEST },
                    onTr = { screen = Screen.INSPECT_TR },
                    onBack = { screen = Screen.SUPPORT },
                )
                Screen.INSPECT -> InspectScreen(
                    vm,
                    onBrowse = { browseFromInspect = true; screen = Screen.BROWSE },
                    onUnpack = {
                        val u = InspectJobs.currentUri
                        if (u != null) {
                            InspectJobs.clear()
                            inspectFromFolder = false
                            enter(1, Screen.UNPACK, ScriptTools.AUTO)
                            engineId = ScriptTools.AUTO
                            vm.setArchive(u, null)
                        }
                    },
                    onFormatTest = {
                        val u = InspectJobs.currentUri
                        val fid = InspectJobs.state.value.formatId
                        val nm = InspectJobs.state.value.name
                        if (u != null && fid != null && nm != null) {
                            InspectJobs.clear(); vm.reset()
                            inspectFromFolder = false
                            FormatTestJobs.clear()
                            FormatTestJobs.setPrefill(FtPrefill(u, nm, fid))
                            screen = Screen.INSPECT_FORMATTEST
                        }
                    },
                    onBack = {
                        InspectJobs.clear(); vm.reset()
                        screen = if (inspectFromFolder) Screen.INSPECT_FOLDER else Screen.FILECHECK
                        inspectFromFolder = false
                    },
                )
                Screen.INSPECT_FOLDER -> FolderCheckScreen(
                    onOpenFile = { item -> inspectFromFolder = true; InspectJobs.inspect(item.uri); screen = Screen.INSPECT },
                    onBack = { FolderJobs.clear(); if (com.furuarc.core.Projects.active != null) { com.furuarc.core.Projects.autoMark(false); screen = Screen.PROJECT } else screen = Screen.FILECHECK },
                )
                Screen.INSPECT_COMPARE -> CompareScreen { CompareJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
                Screen.INSPECT_DUP -> com.furuarc.ui.DupFinderScreen { screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
                Screen.INSPECT_HEX -> com.furuarc.ui.HexViewerScreen { screen = Screen.FILECHECK }
                Screen.INSPECT_FORMATTEST -> com.furuarc.ui.FormatTestScreen { FormatTestJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
                Screen.PROJECT_MENU -> com.furuarc.ui.ProjectMenuScreen(onTranslation = { projectType = 0; screen = Screen.PROJECT_LIST }, onCompress = { projectType = 1; screen = Screen.PROJECT_LIST }, onExtract = { projectType = 2; screen = Screen.PROJECT_LIST }, onPatch = { projectType = 3; screen = Screen.PROJECT_LIST }, onAudit = { projectType = 4; screen = Screen.PROJECT_LIST }, onClean = { projectType = 5; screen = Screen.PROJECT_LIST }, onBack = { screen = Screen.SUPPORT })
                Screen.PROJECT_LIST -> com.furuarc.ui.ProjectListScreen(
                    type = projectType,
                    onOpen = { id -> projectId = id; com.furuarc.core.Projects.active = null; screen = Screen.PROJECT },
                    onBack = { screen = Screen.PROJECT_MENU },
                )
                Screen.PROJECT -> com.furuarc.ui.ProjectScreen(
                    id = projectId ?: "",
                    onStep = { pr, step ->
                        com.furuarc.core.Projects.opened(pr, step)
                        val work = try { if (pr.workUri.isNotEmpty()) android.net.Uri.parse(pr.workUri) else null } catch (e: Exception) { null }
                        val src = try { if (pr.sourceUri.isNotEmpty()) android.net.Uri.parse(pr.sourceUri) else null } catch (e: Exception) { null }
                        // the tools start with the archive and the work folder of the project already chosen
                        when (step) {
                            com.furuarc.core.Projects.UNPACK -> {
                                enter(1, Screen.UNPACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO
                                try { src?.let { vm.setArchive(it, null) }; work?.let { vm.setOutputTree(it) } } catch (e: Exception) { }
                            }
                            com.furuarc.core.Projects.EXPORT -> {
                                enter(ScriptTool.EXPORT.mode, Screen.TOOL_JOB, ScriptTools.AUTO); engineId = ScriptTools.AUTO
                                try { work?.let { vm.setOutputTree(it) } } catch (e: Exception) { }
                            }
                            com.furuarc.core.Projects.TRANSLATE -> enter(10, Screen.TR_EDIT_JOB)
                            com.furuarc.core.Projects.IMPORT -> {
                                enter(ScriptTool.IMPORT.mode, Screen.TOOL_JOB, ScriptTools.AUTO); engineId = ScriptTools.AUTO
                                try { work?.let { vm.setOutputTree(it) } } catch (e: Exception) { }
                            }
                            com.furuarc.core.Projects.PACK -> {
                                enter(2, Screen.PACK, ScriptTools.AUTO); engineId = ScriptTools.AUTO
                                try { work?.let { vm.setPackOutput(it) } } catch (e: Exception) { }
                            }
                            com.furuarc.core.Projects.SCAN -> screen = Screen.INSPECT_FOLDER
                            com.furuarc.core.Projects.NAMES -> screen = Screen.NAMEOPT
                            com.furuarc.core.Projects.TRCHECK -> screen = Screen.INSPECT_TR
                            com.furuarc.core.Projects.EDIT -> screen = Screen.TEXTEDIT
                            com.furuarc.core.Projects.COMPARE -> screen = Screen.INSPECT_COMPARE
                            com.furuarc.core.Projects.VIEW -> screen = Screen.MPLAYER
                            com.furuarc.core.Projects.CONVERT -> { MediaJobs.clear(); screen = Screen.MEDIA_MIXED }
                            com.furuarc.core.Projects.DUPES -> screen = Screen.INSPECT_DUP
                            com.furuarc.core.Projects.RENAME -> screen = Screen.FILERENAME
                            com.furuarc.core.Projects.AUDIO -> { MediaJobs.clear(); screen = Screen.MEDIA_AUDIO }
                            com.furuarc.core.Projects.IMAGE -> { MediaJobs.clear(); screen = Screen.MEDIA_IMAGE }
                            com.furuarc.core.Projects.VIDEO -> { MediaJobs.clear(); screen = Screen.MEDIA_VIDEO }
                            else -> { FormatTestJobs.clear(); screen = Screen.INSPECT_FORMATTEST }
                        }
                    },
                    onBack = { com.furuarc.core.Projects.active = null; screen = Screen.PROJECT_LIST },
                )
                Screen.INSPECT_TR -> TrScanScreen { TrScanJobs.clear(); screen = if (com.furuarc.core.Projects.active != null) Screen.PROJECT else Screen.FILECHECK }
                Screen.PACK -> PackScreen(vm, engineOrNull, onBack = toEngineMenu)
                Screen.SETTINGS -> SettingsScreen(vm) { screen = Screen.MORE }

                Screen.RPGM_TOOLS -> RpgmToolsScreen(
                    onAssets = { screen = Screen.RPGM_ASSETS },
                    onTranslate = { screen = Screen.RPGM_TR_MENU },
                    onCheat = { screen = Screen.RPGM_CHEAT_MENU },
                    onBack = { screen = Screen.SUPPORT },
                    onPlugins = { screen = Screen.PLUGIN_MENU },
                )
                Screen.PLUGIN_MENU -> MenuListScreen(
                    s.pl.mgrTitle,
                    listOf(
                        MenuSpec(AppIcons.Folder, s.pl.scanTitle, s.pl.scanSub) { PluginScanJobs.clear(); screen = Screen.PLUGIN_SCAN },
                        MenuSpec(AppIcons.Edit, s.pl.editTitle, s.pl.editSub) { PluginListJobs.clear(); screen = Screen.PLUGIN_EDIT },
                        MenuSpec(AppIcons.Build, s.pl.chkTitle, s.pl.chkSub) { PluginCheckJobs.clear(); screen = Screen.PLUGIN_CHECK },
                        MenuSpec(AppIcons.Archive, s.pl.cmpTitle, s.pl.cmpSub) { PluginDiffJobs.clear(); screen = Screen.PLUGIN_DIFF },
                    ),
                    onBack = { screen = Screen.RPGM_TOOLS },
                )
                Screen.PLUGIN_SCAN -> PluginScanScreen(confirmExit = vm.settings.value.confirmExit) { PluginScanJobs.clear(); screen = Screen.PLUGIN_MENU }
                Screen.PLUGIN_EDIT -> PluginEditorScreen(vm) { PluginListJobs.clear(); screen = Screen.PLUGIN_MENU }
                Screen.PLUGIN_CHECK -> PluginCheckScreen { PluginCheckJobs.clear(); screen = Screen.PLUGIN_MENU }
                Screen.PLUGIN_DIFF -> PluginDiffScreen { PluginDiffJobs.clear(); screen = Screen.PLUGIN_MENU }
                Screen.RPGM_ASSETS -> RpgmAssetsScreen(
                    onEngine = { e -> engineId = e.id; screen = Screen.ENGINE },
                    onBack = { screen = Screen.RPGM_TOOLS },
                )
                Screen.RPGM_TR_MENU -> MenuListScreen(
                    s.rpgmTranslateTitle,
                    listOf(
                        MenuSpec(AppIcons.FolderUpload, s.trExportTitle, s.trExportSub) { enter(5, Screen.TR_JOB) },
                        MenuSpec(AppIcons.Translate, s.trImportTitle, s.trImportSub) { enter(6, Screen.TR_JOB) },
                    ),
                    onBack = { screen = Screen.RPGM_TOOLS },
                )
                Screen.RPGM_CHEAT_MENU -> MenuListScreen(
                    s.rpgmCheatTitle,
                    listOf(
                        MenuSpec(AppIcons.FolderUpload, s.saveExportTitle, s.saveExportSub) { enter(7, Screen.SAVE_JOB) },
                        MenuSpec(AppIcons.Save, s.saveImportTitle, s.saveImportSub) { enter(8, Screen.SAVE_JOB) },
                        MenuSpec(AppIcons.Edit, s.menuValueTitle, s.menuValueSub) { enter(9, Screen.VALUE_JOB) },
                    ),
                    onBack = { screen = Screen.RPGM_TOOLS },
                )
                Screen.ASSET_JOB -> AssetJobScreen(vm, engine, encrypt = vm.mode() == 4, onBack = toEngineMenu)
                Screen.TR_JOB -> TranslateScreen(vm, import = vm.mode() == 6, onBack = { toParent(Screen.RPGM_TR_MENU) }, onTranslateText = { u -> enter(10, Screen.TR_EDIT_JOB); vm.rpgmInputFile(u) })
                Screen.TR_EDIT_JOB -> TranslateEditScreen(vm, onBack = { toParent(Screen.SUPPORT) })
                Screen.VALUE_JOB -> ValueScreen(vm, onBack = { toParent(Screen.RPGM_CHEAT_MENU) })
                Screen.SAVE_JOB -> SaveScreen(vm, import = vm.mode() == 8, onBack = { toParent(Screen.RPGM_CHEAT_MENU) })
            }
            }
            if (tab >= 0 && com.furuarc.ui.theme.AppStyle.nav == com.furuarc.ui.theme.NavStyle.FAB) com.furuarc.ui.FabNav(navItems, tab, Modifier, onNavSelect)
            if (tab >= 0 && (com.furuarc.ui.theme.AppStyle.nav == com.furuarc.ui.theme.NavStyle.FLOATING || com.furuarc.ui.theme.AppStyle.nav == com.furuarc.ui.theme.NavStyle.FLOAT_ICONS || com.furuarc.ui.theme.AppStyle.nav == com.furuarc.ui.theme.NavStyle.GLASS))
                Box(Modifier.align(Alignment.BottomCenter)) { com.furuarc.ui.FloatingNav(navItems, tab, onNavSelect) }
        }
        if (tab >= 0) AppBottomBar(navItems, tab, onNavSelect)
    }
    }
    if (showUpdate) com.furuarc.ui.UpdateDialog { showUpdate = false }
    com.furuarc.core.UpdateChecker.announce?.let { info ->
        com.furuarc.ui.UpdateAnnounceDialog(info, onDownload = { showUpdate = true }) { com.furuarc.core.UpdateChecker.announce = null }
    }
    // registered last = highest priority: while a text field is in typing mode, back only leaves typing mode
    BackHandler(enabled = com.furuarc.ui.FocusHub.focused) {
        com.furuarc.ui.FocusHub.clear(hostView, focusMgr)
    }
}
