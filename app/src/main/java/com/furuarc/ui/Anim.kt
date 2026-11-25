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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Springy "press" animation (Tyranor-style): the element shrinks quickly while it is pressed (stiff, well damped spring) and
 * bounces back with a visible overshoot on release (soft spring, damping 0.36).
 */
fun Modifier.pressScale(source: InteractionSource, pressed: Float = 0.96f): Modifier = composed {
    val down by source.collectIsPressedAsState()
    val on = down && com.furuarc.ui.theme.AppStyle.animations
    val sc by animateFloatAsState(
        targetValue = if (on) pressed else 1f,
        animationSpec = if (on) spring(dampingRatio = 0.58f, stiffness = 1275f) else spring(dampingRatio = 0.36f, stiffness = 241f),
    )
    this.graphicsLayer { scaleX = sc; scaleY = sc }
}

/** [Modifier.clickable] (with the normal ripple) plus the press animation. */
fun Modifier.clickableScale(enabled: Boolean = true, pressed: Float = 0.96f, ripple: Boolean = true, onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    this.pressScale(src, pressed).clickable(
        interactionSource = src,
        indication = if (ripple) LocalIndication.current else null,
        enabled = enabled,
        onClick = {
            if (com.furuarc.ui.theme.AppStyle.haptics) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
            onClick()
        },
    )
}

/** Interaction source of the nearest [AppCard]; lets the whole card (not just its content) play the press animation. */
val LocalCardSource = androidx.compose.runtime.compositionLocalOf<MutableInteractionSource?> { null }

/** Clickable for content inside an [AppCard]: the card itself shrinks while pressed. Outside a card it scales itself. */
fun Modifier.cardClickable(onClick: () -> Unit): Modifier = composed {
    val src = LocalCardSource.current
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    if (src != null) this.clickable(interactionSource = src, indication = LocalIndication.current, onClick = {
        if (com.furuarc.ui.theme.AppStyle.haptics) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        onClick()
    })
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


/**
 * Scroll jelly (Tyranor-style): the faster a list scrolls, the more its cards are stretched along the scroll direction;
 * they spring back with a small undershoot when the scrolling stops. Cards only read [amount] in their graphics layer,
 * so scrolling causes no recomposition.
 */
class ScrollJelly(private val scope: kotlinx.coroutines.CoroutineScope, private val maxVelocityPx: Float) {
    private var live by androidx.compose.runtime.mutableStateOf(0f)
    private var lastNanos = 0L
    private var smoothed = 0f
    private var watcher: kotlinx.coroutines.Job? = null
    private var releasing = false

    /** 0 = still, 1 = fast scrolling; slightly negative during the undershoot of the spring-back. */
    val amount: Float get() = live

    /** Observes (does not consume) the vertical scrolling of everything below it. */
    val connection = object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
        override fun onPostScroll(
            consumed: androidx.compose.ui.geometry.Offset,
            available: androidx.compose.ui.geometry.Offset,
            source: androidx.compose.ui.input.nestedscroll.NestedScrollSource,
        ): androidx.compose.ui.geometry.Offset {
            if (consumed.y != 0f && com.furuarc.ui.theme.AppStyle.animations) onScrolled(consumed.y)
            return androidx.compose.ui.geometry.Offset.Zero
        }
    }

    private fun onScrolled(dy: Float) {
        val now = System.nanoTime()
        var dt = (now - lastNanos) / 1_000_000_000f
        lastNanos = now
        if (releasing) {
            watcher?.cancel(); watcher = null; releasing = false
            smoothed = live.coerceAtLeast(0f) * maxVelocityPx
        }
        if (dt > 0.12f) { smoothed = live.coerceAtLeast(0f) * maxVelocityPx; dt = 0.016f }
        val velocity = kotlin.math.abs(dy) / dt.coerceAtLeast(0.004f)
        smoothed += (velocity - smoothed) * 0.3f
        live = (smoothed / maxVelocityPx).coerceIn(0f, 1f)
        if (watcher == null) watcher = scope.launch { releaseWhenIdle() }
    }

    private suspend fun releaseWhenIdle() {
        while (true) {
            val wait = 48_000_000L - (System.nanoTime() - lastNanos)
            if (wait <= 0L) break
            kotlinx.coroutines.delay(wait / 1_000_000L + 1L)
        }
        releasing = true
        smoothed = 0f
        androidx.compose.animation.core.animate(
            initialValue = live, targetValue = 0f,
            animationSpec = spring(dampingRatio = 0.36f, stiffness = 241f, visibilityThreshold = 0.001f),
        ) { v, _ -> live = v.coerceAtLeast(-0.6f) }
        releasing = false
        watcher = null
    }
}

val LocalScrollJelly = androidx.compose.runtime.compositionLocalOf<ScrollJelly?> { null }

/** Put on a card: stretches it a little (3.5% at most) while the screen scrolls fast. */
fun Modifier.scrollJelly(strength: Float = 0.035f): Modifier = composed {
    val j = LocalScrollJelly.current
    if (j == null || !com.furuarc.ui.theme.AppStyle.animations) this else this.graphicsLayer {
        val a = j.amount
        if (a != 0f) {
            scaleY = 1f + strength * a
            scaleX = 1f - strength * 0.5f * a
        }
    }
}

/**
 * Staggered entrance: during a short window after a screen change, cards that are composed for the first time pop in one after
 * the other from top to bottom (fade, rise and grow). Items composed later (for example while scrolling a long list) show at once.
 */
object EntranceClock {
    private var startedAt = 0L
    private var slot = 0
    fun restart() { startedAt = android.os.SystemClock.uptimeMillis(); slot = 0 }
    internal fun inWindow(): Boolean = android.os.SystemClock.uptimeMillis() - startedAt < 450L
    internal fun take(): Int = slot++
}

fun Modifier.staggeredEntrance(): Modifier = composed {
    val slot = remember { if (com.furuarc.ui.theme.AppStyle.animations && EntranceClock.inWindow()) EntranceClock.take() else -1 }
    val progress = remember { androidx.compose.animation.core.Animatable(if (slot >= 0) 0f else 1f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (slot >= 0) {
            kotlinx.coroutines.delay(minOf(slot * 45L, 360L))
            progress.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 300f))
        }
    }
    if (slot < 0) this else this.graphicsLayer {
        val t = progress.value
        alpha = (t * 1.6f).coerceIn(0f, 1f)
        translationY = (1f - t) * 28.dp.toPx()
        val sc = 0.92f + 0.08f * t
        scaleX = sc; scaleY = sc
    }
}
