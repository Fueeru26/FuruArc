package com.furuarc.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
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
    /** Fill of input-like controls (dropdown, text field, pop-up menu): a stronger theme tint than the card. */
    val field: Color = card,
    /** Gradient pair derived from the accent (hue shifted a little each way), used by buttons and icon badges. */
    val gradA: Color = accent,
    val gradB: Color = accent,
    /** Content colour (icon / text) that stays readable on top of the gradient. */
    val onGrad: Color = onAccent,
    /** Border of input-like controls when not focused: softer than [outline], so fields do not look heavy. */
    val fieldBorder: Color = outline,
) {
    /** Diagonal gradient used by buttons, icon badges and selected chips. */
    val brush: Brush get() = Brush.linearGradient(listOf(gradA, gradB))
    /** Soft gradient (tint of the gradient over the card) for secondary buttons and unselected chips. */
    val softBrush: Brush get() = Brush.linearGradient(listOf(lerp(card, gradA, if (dark) 0.30f else 0.26f), lerp(card, gradB, if (dark) 0.22f else 0.38f)))
}

/** Rotates the hue of [c] by [deg] degrees and blends it with white/black by [light] (-1..1). */
private fun shiftHue(c: Color, deg: Float, light: Float): Color {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(c.toArgb(), hsv)
    hsv[0] = ((hsv[0] + deg) % 360f + 360f) % 360f
    val r = Color(android.graphics.Color.HSVToColor(hsv))
    return if (light >= 0f) lerp(r, Color.White, light) else lerp(r, Color.Black, -light)
}

/**
 * Adds the gradient pair, the field colour and the field border to a finished palette.
 * The gradient starts at the accent itself (no white blended in, hue moved only a little) and ends in a deeper shade, so the
 * top-left corner of buttons and icon badges is never brighter than the accent. The field is only a light tint of the card.
 */
private fun finish(p: Palette): Palette {
    // Blossom dark: icon badges go from a sky blue to a deep indigo
    val blossomDark = p.dark && p.background == Color(0xFF0C142D)
    val a = if (blossomDark) Color(0xFF3B9BDF) else shiftHue(p.accent, -8f, if (p.dark) -0.08f else -0.03f)
    val b = if (blossomDark) Color(0xFF1A2F9C) else shiftHue(p.accent, 20f, if (p.dark) -0.16f else -0.12f)
    // icon / text colour on the gradient: decided by the accent (a darker end of the gradient does not flip it)
    val on = if (p.accent.luminance() > 0.42f) Color(0xFF101214) else Color.White
    val field = if (p.dark) lerp(lerp(p.card, Color.White, 0.03f), p.accent, 0.05f) else lerp(p.card, p.accent, 0.06f)
    val fieldBorder = lerp(p.outline, p.card, if (p.dark) 0.30f else 0.25f)
    return Palette(
        p.dark, p.background, p.card, p.iconBg, p.iconFg, p.trailingBg, p.trailingFg, p.text, p.muted, p.accent, p.onAccent,
        p.disabledBg, p.disabledFg, p.outline, p.cardBorder, field, a, b, on, fieldBorder,
    )
}

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

/** Palette for the chosen display [style]; [UiStyle.CLASSIC] is the original palette. */
fun buildPalette(dark: Boolean, accent: AccentColor, style: UiStyle = UiStyle.CLASSIC): Palette {
    val b = basePalette(dark, accent)
    fun mk(bg: Color, card: Color, border: Color, iconMix: Float = 0.25f) = Palette(
        dark = dark, background = bg, card = card,
        iconBg = lerp(card, b.accent, iconMix), iconFg = b.accent,
        trailingBg = lerp(card, b.accent, iconMix * 0.7f), trailingFg = b.trailingFg,
        text = b.text, muted = b.muted, accent = b.accent, onAccent = b.onAccent,
        disabledBg = lerp(card, if (dark) Color.White else Color.Black, 0.08f), disabledFg = b.disabledFg,
        outline = b.outline, cardBorder = border,
    )
    return finish(when (style) {
        UiStyle.CLASSIC -> b
        // flat: no tint, cards blend into the background and are only outlined
        UiStyle.MINIMAL -> if (dark) mk(Color(0xFF121212), Color(0xFF121212), Color(0xFF3A3D40), 0.18f)
                           else mk(lerp(Color.White, b.accent, 0.05f), lerp(Color.White, b.accent, 0.07f), lerp(Color.White, b.accent, 0.28f), 0.16f)
        // soft: pastel-tinted background, lighter cards, hardly any outline
        UiStyle.SOFT -> if (dark) mk(lerp(Color(0xFF07090C), b.accent, 0.06f), lerp(Color(0xFF22262B), b.accent, 0.10f), Color.Transparent, 0.26f)
                        else mk(lerp(Color(0xFFF1F3F6), b.accent, 0.16f), lerp(Color.White, b.accent, 0.09f), lerp(Color.White, b.accent, 0.18f), 0.18f)
        // gradient: the app background is a gradient (see MainActivity); cards are a little translucent-looking
        UiStyle.GRADIENT -> if (dark) mk(lerp(Color(0xFF101318), b.accent, 0.10f), lerp(Color(0xFF1B1F26), b.accent, 0.14f), lerp(Color(0xFF1B1F26), b.accent, 0.35f), 0.30f)
                            else mk(lerp(Color(0xFFF4F6F8), b.accent, 0.18f), lerp(Color.White, b.accent, 0.10f), lerp(Color.White, b.accent, 0.34f), 0.22f)
        // pure black for OLED screens (light mode just gets pure white)
        UiStyle.SOLID -> if (dark) mk(Color.Black, Color(0xFF0C0C0E), Color(0xFF26282B), 0.22f)
                         else mk(lerp(Color.White, b.accent, 0.04f), lerp(Color.White, b.accent, 0.08f), lerp(Color.White, b.accent, 0.24f), 0.16f)
        // themed styles (fixed colours, independent of the accent colour)
        UiStyle.SAKURA -> if (dark) mk(Color(0xFF1A1316), Color(0xFF281C22), Color(0xFF4A2F3B), 0.22f)
                          else mk(Color(0xFFFFF1F6), Color(0xFFFFFAFC), Color(0xFFF5CADB), 0.16f)
        UiStyle.OCEAN -> if (dark) mk(Color(0xFF0A1822), Color(0xFF102534), Color(0xFF1F4258), 0.22f)
                         else mk(Color(0xFFE8F4FA), Color(0xFFF6FBFE), Color(0xFFBFDDEB), 0.16f)
        UiStyle.FOREST -> if (dark) mk(Color(0xFF0F1712), Color(0xFF17231B), Color(0xFF2A4333), 0.22f)
                          else mk(Color(0xFFEEF5EC), Color(0xFFFAFDF8), Color(0xFFC9DEC5), 0.16f)
        UiStyle.SUNSET -> if (dark) mk(Color(0xFF1A1218), Color(0xFF261A22), Color(0xFF4A2F3C), 0.22f)
                          else mk(Color(0xFFFFF3E8), Color(0xFFFFFAF5), Color(0xFFF3D2B5), 0.16f)
        UiStyle.NEON -> if (dark) mk(Color(0xFF07070F), Color(0xFF0E0E1C), lerp(Color(0xFF0E0E1C), b.accent, 0.65f), 0.22f)
                        else mk(Color(0xFFF7F7FF), Color.White, lerp(Color.White, b.accent, 0.55f), 0.14f)
        UiStyle.PAPER -> if (dark) mk(Color(0xFF1B1914), Color(0xFF24211A), Color(0xFF3D382C), 0.20f)
                         else mk(Color(0xFFF5EEDC), Color(0xFFFBF6E9), Color(0xFFDDD1B3), 0.14f)
        // blossom: blue sakura artwork behind glass cards (the background image is drawn in MainActivity)
        UiStyle.BLOSSOM -> if (dark) Palette(
            dark = true, background = Color(0xFF0C142D), card = Color(0xFF17213F),
            iconBg = Color(0xFF22346E), iconFg = Color(0xFF8FB4FF),
            trailingBg = Color(0xFF1B2850), trailingFg = Color(0xFFD8E6FF),
            text = Color(0xFFFFFFFF), muted = Color(0xFF8197D6), accent = b.accent, onAccent = b.onAccent,
            disabledBg = Color(0xFF1A2342), disabledFg = Color(0xFF6C7BA8),
            outline = Color(0xFF4D6099), cardBorder = Color(0xFF364673),
        ) else Palette(
            dark = false, background = Color(0xFFE8EEFF), card = Color(0xFFF4F7FF),
            iconBg = Color(0xFFD3DEFF), iconFg = b.accent,
            trailingBg = Color(0xFFDCE5FF), trailingFg = b.accent,
            text = Color(0xFF14214F), muted = Color(0xFF5B6791), accent = b.accent, onAccent = Color.White,
            disabledBg = Color(0xFFDDE3F5), disabledFg = Color(0xFF9AA5C7),
            outline = Color(0xFF6F82C8), cardBorder = Color(0xFFFFFFFF),
        )
    })
}

/** Top colour of the app background gradient for [style], or null when the background is a plain colour. */
fun gradientTop(style: UiStyle, p: Palette): Color? = when (style) {
    UiStyle.GRADIENT -> lerp(p.background, p.accent, if (p.dark) 0.16f else 0.18f)
    UiStyle.OCEAN -> if (p.dark) Color(0xFF123A52) else Color(0xFFCDE8F5)
    UiStyle.SUNSET -> if (p.dark) Color(0xFF3A2037) else Color(0xFFFFD6B0)
    else -> null
}

private fun basePalette(dark: Boolean, accent: AccentColor): Palette {
    val a = accentColor(accent, dark)
    return if (dark) {
        // the card is clearly lighter than the background (cards stand out, as in a card-based layout)
        val card = lerp(Color(0xFF23272D), a, 0.06f)
        Palette(
            dark = true,
            background = lerp(Color(0xFF050608), a, 0.03f),
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
            cardBorder = lerp(card, Color.White, 0.10f),
        )
    } else {
        // near-white cards with a soft shadow (see AppCard) on a tinted background
        val card = lerp(Color(0xFFFFFFFF), a, 0.02f)
        Palette(
            dark = false,
            background = lerp(Color(0xFFF1F3FA), a, 0.10f),
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
            cardBorder = lerp(card, a, 0.14f),
        )
    }
}

/** Effective theme: 0 = follow system, 1 = light, 2 = dark. */
@Composable
fun AppSettings.isDark(): Boolean {
    // schedule mode: the hour is re-checked every minute
    var hour by remember { mutableStateOf(java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)) }
    if (themeMode == 3) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            }
        }
    }
    val sys = androidx.compose.foundation.isSystemInDarkTheme()
    return when (themeMode) {
        1 -> false
        2 -> true
        3 -> if (darkFrom > darkTo) hour >= darkFrom || hour < darkTo else if (darkFrom < darkTo) hour in darkFrom until darkTo else false
        else -> sys
    }
}

@OptIn(androidx.compose.material.ExperimentalMaterialApi::class)
@Composable
fun FuruArcTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = settings.isDark()
    val p = remember(dark, settings.accent, settings.uiStyle) { buildPalette(dark, settings.accent, UiStyle.of(settings.uiStyle)) }
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
