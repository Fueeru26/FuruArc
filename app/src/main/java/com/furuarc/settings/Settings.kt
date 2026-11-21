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

const val DEFAULT_IGNORE = "Thumbs.db, .DS_Store, desktop.ini"

data class AppSettings(
    /** 0 = follow system, 1 = light, 2 = dark. */
    val themeMode: Int = 0,
    val accent: AccentColor = AccentColor.BLUE,
    /** Display style (0 = Classic, 1 = Minimal, 2 = Soft, 3 = Gradient, 4 = Solid, 5..10 = Sakura, Ocean, Forest, Sunset, Neon, Paper). */
    val uiStyle: Int = 0,
    /** Navigation bar style (0 = Classic, 1 = Floating, 2 = Icons Only, 3 = Sliding Pill, 4 = Top Line, 5 = Floating Icons, 6 = Floating Button). */
    val navStyle: Int = 0,
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
    val confirmExit: Boolean = false,
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
        return AppSettings(
            themeMode = if (sp.contains("themeMode")) sp.getInt("themeMode", 0).coerceIn(0, 2)
            else if (sp.contains("dark")) (if (sp.getBoolean("dark", true)) 2 else 1) else 0,
            accent = AccentColor.values().firstOrNull { it.id == accentId } ?: AccentColor.BLUE,
            uiStyle = sp.getInt("uiStyle", 0).coerceIn(0, 10),
            navStyle = sp.getInt("navStyle", 0).coerceIn(0, 6),
            langPref = sp.getString("langPref", null) ?: (langCode?.takeIf { c -> Lang.values().any { it.code == c } } ?: "system"),
            conflict = sp.getInt("conflict", 0).coerceIn(0, 2),
            ignore = sp.getString("ignore", DEFAULT_IGNORE) ?: DEFAULT_IGNORE,
            defaultOutUri = sp.getString("defaultOutUri", "") ?: "",
            defaultOutName = sp.getString("defaultOutName", "") ?: "",
            bgProcess = sp.getBoolean("bgProcess", false),
            keepScreenOn = sp.getBoolean("keepScreenOn", false),
            confirmExit = sp.getBoolean("confirmExit", sp.getBoolean("confirmExitTr", false) || sp.getBoolean("confirmExitVal", false)),
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
