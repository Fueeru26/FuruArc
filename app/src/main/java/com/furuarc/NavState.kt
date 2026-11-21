package com.furuarc

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KProperty

/**
 * Holder of the current screen (used as a property delegate: `var screen by navState`).
 * When a feature was opened from the Home tab ([enterFromHome]), going "back" into one of its parent menus
 * ([MENUS]) is redirected to Home instead, so back always returns to Home.
 */
class NavState(initial: Screen) {
    private var raw by mutableStateOf(initial)
    private var fromHome = false
    private var entry: Screen? = null

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Screen = raw

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Screen) {
        if (fromHome) {
            if (value == Screen.HOME || value == Screen.LIBRARY) fromHome = false
            else if (value in MENUS && value != entry) { fromHome = false; raw = Screen.HOME; return }
        }
        raw = value
    }

    /** Call after navigating to the opened feature's screen: the next "back" into a parent menu goes to Home. */
    fun enterFromHome() { fromHome = true; entry = raw }

    /** Leave "opened from Home" mode (e.g. a bottom tab was tapped). */
    fun clearHome() { fromHome = false }

    companion object {
        val MENUS = setOf(
            Screen.SUPPORT, Screen.MORE, Screen.MPLAYER, Screen.VN_TOOLS, Screen.RPGM_TOOLS, Screen.TEXTEDIT, Screen.MEDIA,
            Screen.FILECHECK, Screen.FILETOOLS, Screen.RPGM_TR_MENU, Screen.RPGM_CHEAT_MENU, Screen.PLUGIN_MENU,
            Screen.AUTO_SCRIPTS, Screen.VN_ENGINES, Screen.RPGM_ASSETS, Screen.ABOUT, Screen.SETTINGS,
        )
    }
}
