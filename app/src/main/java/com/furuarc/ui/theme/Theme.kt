package com.furuarc.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.furuarc.R
import com.furuarc.i18n.Str
import com.furuarc.i18n.Strings
import com.furuarc.settings.AccentColor
import com.furuarc.settings.AppSettings

class Palette(
    val dark: Boolean,
    val background: Color,
    val card: Color,
    val iconBg: Color,
    val iconFg: Color,
    val trailingBg: Color,
    val trailingFg: Color,
    val text: Color,
    val muted: Color,
    val accent: Color,
    val onAccent: Color,
    val disabledBg: Color,
    val disabledFg: Color,
    val outline: Color,
    val cardBorder: Color,
)

/** Qanelas font (Regular, SemiBold, Bold, ExtraBold). Other weights use the nearest one. */
val Qanelas = FontFamily(
    Font(R.font.qanelas_regular, FontWeight.Normal),
    Font(R.font.qanelas_regular, FontWeight.Medium),
    Font(R.font.qanelas_semibold, FontWeight.SemiBold),
    Font(R.font.qanelas_bold, FontWeight.Bold),
    Font(R.font.qanelas_extrabold, FontWeight.ExtraBold),
)

val LocalNativeInput = staticCompositionLocalOf { true }
val LocalShowEngineLabel = staticCompositionLocalOf { false }

val LocalPalette = staticCompositionLocalOf { buildPalette(true, AccentColor.LIGHT_BLUE) }
val LocalStr = staticCompositionLocalOf<Str> { Strings.EN }
val LocalLang = staticCompositionLocalOf { com.furuarc.settings.Lang.EN }

/** Accent color: light for dark mode, solid for light mode. */
fun accentColor(a: AccentColor, dark: Boolean): Color = when (a) {
    AccentColor.BLUE -> Color(0xFF307DEF) // exactly the same as the "Scan depth" slider blue
    AccentColor.LIGHT_BLUE -> if (dark) Color(0xFF81D4FA) else Color(0xFF0277BD)
    AccentColor.GREEN -> if (dark) Color(0xFF6BDB8F) else Color(0xFF1B7F3B)
    AccentColor.TEAL -> if (dark) Color(0xFF5EDBC8) else Color(0xFF00796B)
    AccentColor.PURPLE -> if (dark) Color(0xFFD0BCFF) else Color(0xFF6750A4)
    AccentColor.PINK -> if (dark) Color(0xFFFFB0CC) else Color(0xFFB0285E)
    AccentColor.ORANGE -> if (dark) Color(0xFFFFB86B) else Color(0xFFB75C00)
    AccentColor.RED -> if (dark) Color(0xFFFF9A94) else Color(0xFFB3261E)
    AccentColor.YELLOW -> if (dark) Color(0xFFF2D95B) else Color(0xFF8A6F00)
}

fun buildPalette(dark: Boolean, accent: AccentColor): Palette {
    val a = accentColor(accent, dark)
    return if (dark) {
        val card = lerp(Color(0xFF1A1D21), a, 0.08f)
        Palette(
            dark = true,
            background = lerp(Color(0xFF0F1114), a, 0.05f),
            card = card,
            iconBg = lerp(card, a, 0.30f),
            iconFg = a,
            trailingBg = lerp(card, a, 0.20f),
            trailingFg = lerp(Color.White, a, 0.45f),
            text = Color(0xFFE6E8EB),
            muted = lerp(Color(0xFFB0B5BA), a, 0.15f),
            accent = a,
            onAccent = Color(0xFF101214),
            disabledBg = lerp(card, Color.White, 0.08f),
            disabledFg = Color(0xFF8C9196),
            outline = lerp(Color(0xFF8A8F94), a, 0.30f),
            cardBorder = lerp(card, Color.White, 0.11f),
        )
    } else {
        val card = lerp(Color(0xFFFFFFFF), a, 0.08f)
        Palette(
            dark = false,
            background = lerp(Color(0xFFF4F6F8), a, 0.06f),
            card = card,
            iconBg = lerp(card, a, 0.18f),
            iconFg = a,
            trailingBg = lerp(card, a, 0.12f),
            trailingFg = a,
            text = Color(0xFF1B1C1E),
            muted = lerp(Color(0xFF5F6368), a, 0.10f),
            accent = a,
            onAccent = Color.White,
            disabledBg = lerp(card, Color.Black, 0.08f),
            disabledFg = Color(0xFF9AA0A6),
            outline = lerp(Color(0xFF6B7075), a, 0.40f),
            cardBorder = lerp(card, Color.Black, 0.12f),
        )
    }
}

/** Effective theme: 0 = follow system, 1 = light, 2 = dark. */
@Composable
fun AppSettings.isDark(): Boolean = when (themeMode) {
    1 -> false
    2 -> true
    else -> androidx.compose.foundation.isSystemInDarkTheme()
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun FuruArcTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = settings.isDark()
    val p = remember(dark, settings.accent) { buildPalette(dark, settings.accent) }
    val str = Strings.of(settings.lang)
    val colors = if (dark) {
        darkColors(
            primary = p.accent, primaryVariant = p.accent, secondary = p.accent,
            background = p.background, surface = p.card,
            onPrimary = p.onAccent, onSecondary = p.onAccent,
            onBackground = p.text, onSurface = p.text,
        )
    } else {
        lightColors(
            primary = p.accent, primaryVariant = p.accent, secondary = p.accent,
            background = p.background, surface = p.card,
            onPrimary = p.onAccent, onSecondary = p.onAccent,
            onBackground = p.text, onSurface = p.text,
        )
    }
    CompositionLocalProvider(
        LocalPalette provides p, LocalStr provides str, LocalLang provides settings.lang,
        LocalNativeInput provides settings.nativeInput,
        LocalShowEngineLabel provides settings.showEngineLabel,
        // no elevation overlay in dark mode: pop-up menus keep exactly the same color as their trigger (p.card)
        androidx.compose.material.LocalElevationOverlay provides null,
    ) {
        // CJK languages: Qanelas has no kanji/hanzi/hangul glyphs, so Android uses the system font as a fallback;
        // localeList ensures the correct glyph variant (Japanese/Chinese/Korean) is chosen.
        val loc = androidx.compose.ui.text.intl.LocaleList(settings.lang.code)
        MaterialTheme(colors = colors, typography = Typography(defaultFontFamily = Qanelas), content = {
            androidx.compose.material.ProvideTextStyle(androidx.compose.ui.text.TextStyle(localeList = loc), content)
        })
    }
}
