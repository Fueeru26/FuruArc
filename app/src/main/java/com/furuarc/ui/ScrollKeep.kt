package com.furuarc.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * Keeps scroll positions across screens. When entering a menu and coming back, the parent screen's scroll position is restored.
 * Entering a new screen (forward) always starts from the top.
 *
 * How it works: [onNavigate] is called on every screen change. If the screen is already in the history it means going back
 * (position kept, screens above it dropped); if not, it means going forward (that screen's old position is removed).
 * Positions are saved when the screen leaves the composition, by [rememberScrollState] and [rememberLazyListState] below.
 */
class ScrollKeeper {
    private val scrolls = HashMap<String, Int>()
    private val lists = HashMap<String, Pair<Int, Int>>()
    private val stack = ArrayList<String>()
    private var current = ""
    private var counter = 0

    fun onNavigate(name: String) {
        current = name
        counter = 0
        val i = stack.indexOf(name)
        if (i >= 0) {
            while (stack.size > i + 1) forget(stack.removeAt(stack.size - 1))
        } else {
            forget(name)
            stack.add(name)
        }
    }

    /** Switch bottom tab: all history and positions are discarded, the target tab starts from the top. */
    fun clearAll() {
        scrolls.clear(); lists.clear(); stack.clear()
    }

    private fun forget(name: String) {
        val prefix = "$name#"
        scrolls.keys.removeAll { it.startsWith(prefix) }
        lists.keys.removeAll { it.startsWith(prefix) }
    }

    internal fun nextKey(): String = current + "#" + (counter++)
    internal fun scrollOf(key: String): Int? = scrolls[key]
    internal fun listOf(key: String): Pair<Int, Int>? = lists[key]
    internal fun putScroll(key: String, v: Int) { scrolls[key] = v }
    internal fun putList(key: String, v: Pair<Int, Int>) { lists[key] = v }
}

val LocalScrollKeeper = compositionLocalOf<ScrollKeeper?> { null }

/** Replacement for androidx.compose.foundation.rememberScrollState: its position is remembered per screen (see [ScrollKeeper]). */
@Composable
fun rememberScrollState(initial: Int = 0): ScrollState {
    val keeper = LocalScrollKeeper.current
    val key = remember(keeper) { keeper?.nextKey() }
    val state = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(if (keeper != null && key != null) keeper.scrollOf(key) ?: initial else initial)
    }
    if (keeper != null && key != null) {
        DisposableEffect(key) { onDispose { keeper.putScroll(key, state.value) } }
    }
    return state
}

/** Replacement for androidx.compose.foundation.lazy.rememberLazyListState with the same behavior. */
@Composable
fun rememberLazyListState(initialFirstVisibleItemIndex: Int = 0, initialFirstVisibleItemScrollOffset: Int = 0): LazyListState {
    val keeper = LocalScrollKeeper.current
    val key = remember(keeper) { keeper?.nextKey() }
    val state = rememberSaveable(saver = LazyListState.Saver) {
        val saved = if (keeper != null && key != null) keeper.listOf(key) else null
        LazyListState(saved?.first ?: initialFirstVisibleItemIndex, saved?.second ?: initialFirstVisibleItemScrollOffset)
    }
    if (keeper != null && key != null) {
        DisposableEffect(key) { onDispose { keeper.putList(key, state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset) } }
    }
    return state
}
