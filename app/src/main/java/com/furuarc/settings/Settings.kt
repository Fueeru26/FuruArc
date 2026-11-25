package com.furuarc.settings

import android.content.Context

enum class AccentColor(val id: String) {
    BLUE("blue"), LIGHT_BLUE("lightblue"), GREEN("green"), TEAL("teal"), PURPLE("purple"),
    PINK("pink"), ORANGE("orange"), RED("red"), YELLOW("yellow"),
}

enum class Lang(val code: String) { EN("en"), ID("id"), JA("ja"), ZH("zh"), KO("ko") }

/** Saved language choice: "system" or a [Lang] code. An unsupported system language uses English. */
fun resolveLang(pref: String): Lang {
    Lang.values().firstOrNull { it.code == pref }?.let { return it }
    return when (java.util.Locale.getDefault().language) {
        "id", "in" -> Lang.ID
        "ja" -> Lang.JA
        "zh" -> Lang.ZH
        "ko" -> Lang.KO
        else -> Lang.EN
    }
}

/** Display Styles offered in Settings: Blue Blossom (12, default), Classic (0), Minimal (1), Soft (2). */
val SHOWN_UI_STYLES = setOf(12, 0, 1, 2)

/** Navigation Bar Styles offered in Settings: Classic Floating (7, default), Classic (0), Floating (1), Floating Icons (5), Floating Button (6). */
val SHOWN_NAV_STYLES = setOf(7, 0, 1, 5, 6)

const val DEFAULT_IGNORE = "Thumbs.db, .DS_Store, desktop.ini"

data class AppSettings(
    /** 0 = follow system, 1 = light, 2 = dark, 3 = schedule. */
    val themeMode: Int = 0,
    val accent: AccentColor = AccentColor.BLUE,
    /** Display style offered in Settings: 12 = Blue Blossom (default), 0 = Classic, 1 = Minimal, 2 = Soft (other ids are no longer selectable and are replaced by 12 on load). */
    val uiStyle: Int = 12,
    /** Navigation bar style offered in Settings: 7 = Classic Floating (default), 0 = Classic, 1 = Floating, 5 = Floating Icons, 6 = Floating Button (other ids are replaced by 7 on load). */
    val navStyle: Int = 7,
    /** Press animations and screen transitions. */
    val animations: Boolean = true,
    /** Light vibration on taps of tabs and buttons. */
    val haptics: Boolean = false,
    /** Theme mode 3 (schedule): dark from [darkFrom] o'clock until [darkTo] o'clock. */
    val darkFrom: Int = 18,
    val darkTo: Int = 6,
    /** "system" or a language code (en, ja, zh, ko, id). */
    val langPref: String = "system",
    // archive
    val conflict: Int = 0, // 0 = overwrite, 1 = skip, 2 = rename
    val ignore: String = DEFAULT_IGNORE,
    val defaultOutUri: String = "",
    val defaultOutName: String = "",
    // system
    val bgProcess: Boolean = false,
    val keepScreenOn: Boolean = false,
    val confirmExit: Boolean = true,
    val nativeInput: Boolean = true,
    val showEngineLabel: Boolean = false,
) {
    val lang: Lang get() = resolveLang(langPref)
}

class SettingsStore(ctx: Context) {
    private val sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun load(): AppSettings {
        val accentId = sp.getString("accent", null)
        val langCode = sp.getString("lang", null)
        // one-time switch to the Blossom look (Display Style 12 + Glass navigation bar 7); later changes in Settings are kept
        if (!sp.getBoolean("lookBlossom", false)) {
            sp.edit().putInt("uiStyle", 12).putInt("navStyle", 7).putBoolean("lookBlossom", true).apply()
        }
        return AppSettings(
            themeMode = if (sp.contains("themeMode")) sp.getInt("themeMode", 0).coerceIn(0, 3)
            else if (sp.contains("dark")) (if (sp.getBoolean("dark", true)) 2 else 1) else 0,
            accent = AccentColor.values().firstOrNull { it.id == accentId } ?: AccentColor.BLUE,
            // styles removed from Settings (Gradient, Solid, themed ones, Seasonal) fall back to Blue Blossom
            uiStyle = sp.getInt("uiStyle", 12).let { if (it in SHOWN_UI_STYLES) it else 12 },
            animations = sp.getBoolean("animations", true),
            haptics = sp.getBoolean("haptics", false),
            darkFrom = sp.getInt("darkFrom", 18).coerceIn(0, 23),
            darkTo = sp.getInt("darkTo", 6).coerceIn(0, 23),
            // bars removed from Settings (Icons Only, Sliding Pill, Top Line) fall back to Classic Floating
            navStyle = sp.getInt("navStyle", 7).let { if (it in SHOWN_NAV_STYLES) it else 7 },
            langPref = sp.getString("langPref", null) ?: (langCode?.takeIf { c -> Lang.values().any { it.code == c } } ?: "system"),
            conflict = sp.getInt("conflict", 0).coerceIn(0, 2),
            ignore = sp.getString("ignore", DEFAULT_IGNORE) ?: DEFAULT_IGNORE,
            defaultOutUri = sp.getString("defaultOutUri", "") ?: "",
            defaultOutName = sp.getString("defaultOutName", "") ?: "",
            bgProcess = sp.getBoolean("bgProcess", false),
            keepScreenOn = sp.getBoolean("keepScreenOn", false),
            confirmExit = sp.getBoolean("confirmExit", true),
            nativeInput = sp.getBoolean("nativeInput", true),
            showEngineLabel = sp.getBoolean("showEngineLabel", false),
        )
    }

    /** Small preferences (last format/name, filters, etc.) so forms remember the last entries. */
    fun getStr(key: String, def: String): String = sp.getString("p_$key", def) ?: def

    fun putStr(key: String, value: String) {
        sp.edit().putString("p_$key", value).apply()
    }

    fun save(s: AppSettings) {
        sp.edit()
            .putInt("themeMode", s.themeMode)
            .putString("accent", s.accent.id)
            .putInt("uiStyle", s.uiStyle)
            .putBoolean("animations", s.animations)
            .putBoolean("haptics", s.haptics)
            .putInt("darkFrom", s.darkFrom)
            .putInt("darkTo", s.darkTo)
            .putInt("navStyle", s.navStyle)
            .putString("langPref", s.langPref)
            .putInt("conflict", s.conflict)
            .putString("ignore", s.ignore)
            .putString("defaultOutUri", s.defaultOutUri)
            .putString("defaultOutName", s.defaultOutName)
            .putBoolean("bgProcess", s.bgProcess)
            .putBoolean("keepScreenOn", s.keepScreenOn)
            .putBoolean("confirmExit", s.confirmExit)
            .putBoolean("nativeInput", s.nativeInput)
            .putBoolean("showEngineLabel", s.showEngineLabel)
            .apply()
    }
}
