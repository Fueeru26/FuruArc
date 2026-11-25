package com.furuarc

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import com.furuarc.ui.theme.AppStyle

/**
 * Screen transitions, modelled on the Tyranor Next app:
 * - switching between the four main tabs: the pages slide sideways (in the direction of the tab order) while cross-fading;
 * - opening a menu or page: the new page rises from the bottom;
 * - going back: the page on top sinks back down to the bottom, revealing the previous page.
 */
@OptIn(ExperimentalAnimationApi::class)
object ScreenTransitions {
    private fun tabIndex(s: Screen): Int = when (s) { Screen.HOME -> 0; Screen.LIBRARY -> 1; Screen.SUPPORT -> 2; Screen.MORE -> 3; else -> -1 }

    // history of the screens we came from; a move to the screen on top of it is a "back"
    private val stack = ArrayList<Screen>()
    private var lastFrom: Screen? = null
    private var lastTo: Screen? = null
    private var lastBack = false

    /** The spec may be evaluated more than once per change, so the stack is only touched when the (from, to) pair is new. */
    private fun isBack(from: Screen, to: Screen): Boolean {
        if (from == lastFrom && to == lastTo) return lastBack
        lastFrom = from; lastTo = to
        com.furuarc.ui.EntranceClock.restart()
        val a = tabIndex(from); val b = tabIndex(to)
        val back = if (a >= 0 && b >= 0) { stack.clear(); false }
        else if (stack.isNotEmpty() && stack[stack.size - 1] == to) { stack.removeAt(stack.size - 1); true }
        else { stack.add(from); if (stack.size > 40) stack.removeAt(0); false }
        lastBack = back
        return back
    }

    // springs from Tyranor Next: page spring (0.4s, damping 0.86) for tabs
    private val tabSlide = androidx.compose.animation.core.spring<androidx.compose.ui.unit.IntOffset>(
        dampingRatio = 0.86f, stiffness = 319f)
    private val tabScale = androidx.compose.animation.core.spring<Float>(dampingRatio = 0.86f, stiffness = 319f)

    fun spec(scope: AnimatedContentScope<Screen>): ContentTransform = with(scope) {
        if (!AppStyle.animations) return ContentTransform(EnterTransition.None, ExitTransition.None)
        val from = initialState
        val to = targetState
        val back = isBack(from, to)
        val a = tabIndex(from); val b = tabIndex(to)
        if (a >= 0 && b >= 0) {
            // whole-width sideways move (in the direction of the tab order) with a depth scale of 6% and a fade
            val dir = if (b > a) 1 else -1
            ContentTransform(
                fadeIn(tween(280, easing = LinearOutSlowInEasing)) +
                    slideInHorizontally(tabSlide) { dir * it } +
                    scaleIn(tabScale, initialScale = 0.94f),
                fadeOut(tween(180)) +
                    slideOutHorizontally(tabSlide) { -dir * it } +
                    scaleOut(tabScale, targetScale = 0.94f),
            )
        } else if (back) {
            // the leaving page sinks down (full height, 240ms) on top of the page we return to, which comes back from a slight depth
            ContentTransform(
                fadeIn(tween(240)) + scaleIn(tween(260, easing = FastOutSlowInEasing), initialScale = 0.94f),
                slideOutVertically(tween(240, easing = FastOutSlowInEasing)) { it },
            ).apply { targetContentZIndex = -1f }
        } else {
            // the new page rises from the bottom (full height, 260ms); the old page recedes (smaller, dimmer) behind it
            ContentTransform(
                slideInVertically(tween(260, easing = FastOutSlowInEasing)) { it },
                fadeOut(tween(260, easing = FastOutSlowInEasing)) + scaleOut(tween(260, easing = FastOutSlowInEasing), targetScale = 0.94f),
            )
        }
    }
}
