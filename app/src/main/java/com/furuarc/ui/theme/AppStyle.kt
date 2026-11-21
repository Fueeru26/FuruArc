package com.furuarc.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Overall look of the app ("Display Style" in Settings). [CLASSIC] is the original look. */
enum class UiStyle(val id: Int) {
    CLASSIC(0), MINIMAL(1), SOFT(2), GRADIENT(3), SOLID(4), SAKURA(5), OCEAN(6), FOREST(7), SUNSET(8), NEON(9), PAPER(10);

    companion object {
        fun of(id: Int): UiStyle {
            if (id == 11) {
                // seasonal (automatic): by month of the year
                return when (java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1) {
                    3, 4, 5 -> SAKURA
                    6, 7, 8 -> OCEAN
                    9, 10, 11 -> FOREST
                    else -> SUNSET
                }
            }
            return values().firstOrNull { it.id == id } ?: CLASSIC
        }
    }
}

/** Bottom navigation bar look ("Navigation Bar Style" in Settings). */
enum class NavStyle(val id: Int) {
    CLASSIC(0), FLOATING(1), ICONS(2), PILL(3), TOPLINE(4), FLOAT_ICONS(5), FAB(6);

    companion object { fun of(id: Int): NavStyle = values().firstOrNull { it.id == id } ?: CLASSIC }
}

/**
 * The currently selected styles. They are set from the settings (see MainViewModel) outside of composition and are
 * observable state, so every composable that reads a radius or a style recomposes when the user changes it.
 */
object AppStyle {
    var ui by mutableStateOf(UiStyle.CLASSIC)
    var nav by mutableStateOf(NavStyle.CLASSIC)
    var animations by mutableStateOf(true)
    var haptics by mutableStateOf(false)

    val cardRadius: Dp get() = when (ui) {
        UiStyle.CLASSIC -> 10.dp
        UiStyle.MINIMAL -> 4.dp
        UiStyle.SOFT -> 22.dp
        UiStyle.GRADIENT -> 16.dp
        UiStyle.SOLID -> 12.dp
        UiStyle.SAKURA -> 20.dp
        UiStyle.OCEAN -> 16.dp
        UiStyle.FOREST -> 14.dp
        UiStyle.SUNSET -> 18.dp
        UiStyle.NEON -> 8.dp
        UiStyle.PAPER -> 6.dp
    }
    val controlRadius: Dp get() = when (ui) {
        UiStyle.CLASSIC -> 10.dp
        UiStyle.MINIMAL -> 4.dp
        UiStyle.SOFT -> 22.dp
        UiStyle.GRADIENT -> 14.dp
        UiStyle.SOLID -> 12.dp
        UiStyle.SAKURA -> 20.dp
        UiStyle.OCEAN -> 14.dp
        UiStyle.FOREST -> 12.dp
        UiStyle.SUNSET -> 16.dp
        UiStyle.NEON -> 8.dp
        UiStyle.PAPER -> 6.dp
    }
    val iconRadius: Dp get() = when (ui) {
        UiStyle.CLASSIC -> 8.dp
        UiStyle.MINIMAL -> 4.dp
        UiStyle.SOFT -> 16.dp
        UiStyle.GRADIENT -> 12.dp
        UiStyle.SOLID -> 10.dp
        UiStyle.SAKURA -> 16.dp
        UiStyle.OCEAN -> 12.dp
        UiStyle.FOREST -> 10.dp
        UiStyle.SUNSET -> 14.dp
        UiStyle.NEON -> 6.dp
        UiStyle.PAPER -> 4.dp
    }

    fun apply(uiId: Int, navId: Int, anim: Boolean = true, haptic: Boolean = false) {
        ui = UiStyle.of(uiId)
        nav = NavStyle.of(navId)
        animations = anim
        haptics = haptic
    }
}
