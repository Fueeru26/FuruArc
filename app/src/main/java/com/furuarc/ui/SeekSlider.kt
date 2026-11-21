package com.furuarc.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.furuarc.ui.theme.LocalPalette
import kotlinx.coroutines.delay

/**
 * State of a seek bar. While the user drags or taps, [drag] holds the wanted position; after the seek has been sent,
 * [settle] keeps showing it for a moment so the bar does not jump back before the player reports the new position.
 */
class SeekState {
    var drag by mutableStateOf<Float?>(null)
    var settle by mutableStateOf<Float?>(null)

    /** Position to show: the wanted one while dragging / settling, otherwise the player's [current] position. */
    fun shown(current: Int): Int = (drag ?: settle ?: current.toFloat()).toInt()
}

/**
 * Seek bar for media players. The seek is sent shortly after the value stops changing instead of in onValueChangeFinished:
 * on a tap Compose reports "finished" before the new value arrives, which made taps move the thumb without seeking the media.
 */
@Composable
fun SeekSlider(state: SeekState, position: Int, duration: Int, onSeek: (Int) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val p = LocalPalette.current
    val seek by rememberUpdatedState(onSeek)
    val d = duration.coerceAtLeast(1)
    LaunchedEffect(state.drag) {
        val target = state.drag ?: return@LaunchedEffect
        delay(150)
        seek(target.toInt())
        state.settle = target
        state.drag = null
    }
    LaunchedEffect(state.settle) {
        if (state.settle != null) {
            delay(800)
            state.settle = null
        }
    }
    Slider(
        value = (state.drag ?: state.settle ?: position.toFloat()).coerceIn(0f, d.toFloat()),
        onValueChange = { state.settle = null; state.drag = it },
        valueRange = 0f..d.toFloat(),
        colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent),
        modifier = modifier,
    )
}

fun fmtClock(ms: Int): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return if (s >= 3600) "%d:%02d:%02d".format(s / 3600, (s / 60) % 60, s % 60) else "%d:%02d".format(s / 60, s % 60)
}
