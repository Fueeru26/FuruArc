package com.furuarc.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/** Springy "press" animation: the element shrinks a little while it is pressed and bounces back on release. */
fun Modifier.pressScale(source: InteractionSource, pressed: Float = 0.96f): Modifier = composed {
    val down by source.collectIsPressedAsState()
    val sc by animateFloatAsState(
        targetValue = if (down) pressed else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    )
    this.graphicsLayer { scaleX = sc; scaleY = sc }
}

/** [Modifier.clickable] (with the normal ripple) plus the press animation. */
fun Modifier.clickableScale(enabled: Boolean = true, pressed: Float = 0.96f, ripple: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    this.pressScale(src, pressed).clickable(
        interactionSource = src,
        indication = if (ripple) LocalIndication.current else null,
        enabled = enabled,
        onClick = onClick,
    )
}

/** Interaction source of the nearest [AppCard]; lets the whole card (not just its content) play the press animation. */
val LocalCardSource = androidx.compose.runtime.compositionLocalOf<MutableInteractionSource?> { null }

/** Clickable for content inside an [AppCard]: the card itself shrinks while pressed. Outside a card it scales itself. */
fun Modifier.cardClickable(onClick: () -> Unit): Modifier = composed {
    val src = LocalCardSource.current
    if (src != null) this.clickable(interactionSource = src, indication = LocalIndication.current, onClick = onClick)
    else this.clickableScale(onClick = onClick)
}

/** Whether screen headers are shrunk (set by scrolling; see [HeaderScrollConnection]). */
object HeaderState {
    var collapsed by androidx.compose.runtime.mutableStateOf(false)
}

/** Observes vertical scrolling of any child (without consuming it): scrolling down shrinks the headers, scrolling up restores them. */
class HeaderScrollConnection : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
    private var acc = 0f
    override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
        val dy = available.y
        if (kotlin.math.abs(dy) > kotlin.math.abs(available.x)) {
            if ((dy < 0f) != (acc < 0f)) acc = 0f
            acc += dy
            if (acc < -60f) HeaderState.collapsed = true else if (acc > 30f) HeaderState.collapsed = false
        }
        return androidx.compose.ui.geometry.Offset.Zero
    }
}

/** Extra bottom space the main tab screens leave for a navigation bar that floats over the content. */
val LocalBarInset = androidx.compose.runtime.compositionLocalOf { androidx.compose.ui.unit.Dp(0f) }
