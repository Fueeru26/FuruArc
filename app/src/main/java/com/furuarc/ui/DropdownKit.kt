package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.furuarc.ui.theme.LocalPalette

/** Height of one dropdown option. */
val DropdownItemHeight = 48.dp

/** Number of options shown at once; beyond this the menu scrolls. */
const val DROPDOWN_MAX_VISIBLE = 5

private val DropdownVPad = 4.dp
private val DropdownMaxHeight = DropdownItemHeight * DROPDOWN_MAX_VISIBLE + DropdownVPad * 2

/** Records the width of the trigger component (in dp) into [width], to match the dropdown menu width. */
@Composable
fun Modifier.trackWidth(width: MutableState<Dp>): Modifier {
    val density = LocalDensity.current
    return this.onSizeChanged { width.value = with(density) { it.width.toDp() } }
}

/** The menu appears right below the trigger (with a small gap); if it does not fit, it appears above. */
private class BelowAnchorProvider(private val gapPx: Int) : PopupPositionProvider {
    override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
        val x = anchorBounds.left.coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
        val below = anchorBounds.bottom + gapPx
        val y = if (below + popupContentSize.height <= windowSize.height) below
        else (anchorBounds.top - gapPx - popupContentSize.height).coerceAtLeast(0)
        return IntOffset(x, y)
    }
}

/** Thin scroll bar on the right side; only shown when the content is taller than the menu. */
private fun Modifier.dropdownScrollbar(state: ScrollState, color: androidx.compose.ui.graphics.Color): Modifier = drawWithContent {
    drawContent()
    val max = state.maxValue
    if (max > 0 && max < Int.MAX_VALUE) {
        val viewH = size.height
        val inset = 8.dp.toPx()
        val track = viewH - inset * 2
        val thumbH = (track * viewH / (viewH + max)).coerceAtLeast(24.dp.toPx())
        val top = inset + (track - thumbH) * (state.value.toFloat() / max)
        val w = 4.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width - w - 4.dp.toPx(), top),
            size = Size(w, thumbH),
            cornerRadius = CornerRadius(w / 2, w / 2),
        )
    }
}

/**
 * Field-style dropdown menu: same width as the trigger field ([anchorWidth]), with a border and rounded corners,
 * maximum height of 5 options; with more options the content scrolls and a scroll bar appears on the right.
 */
@Composable
fun FieldDropdownMenu(
    expanded: Boolean,
    anchorWidth: Dp,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val p = LocalPalette.current
    val density = LocalDensity.current
    val provider = remember(density) { BelowAnchorProvider(with(density) { 4.dp.roundToPx() }) }
    // uses Compose's own rememberScrollState directly (not the screen position keeper): the menu always starts from the top
    val state = androidx.compose.foundation.rememberScrollState()
    Popup(popupPositionProvider = provider, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        Surface(
            // elevation 0 (shadow drawn by Modifier.shadow): Material's dark-theme elevation overlay would make the menu lighter than the trigger
            modifier = (if (anchorWidth > 0.dp) Modifier.width(anchorWidth) else Modifier).shadow(4.dp, RoundedCornerShape(CardRadius)),
            shape = RoundedCornerShape(CardRadius),
            color = p.field,
            border = BorderStroke(1.dp, p.fieldBorder),
            elevation = 0.dp,
        ) {
            Box(Modifier.heightIn(max = DropdownMaxHeight).dropdownScrollbar(state, p.muted.copy(alpha = 0.7f))) {
                Column(Modifier.verticalScroll(state).padding(vertical = DropdownVPad), content = content)
            }
        }
    }
}

/** One option in [FieldDropdownMenu]: fixed height, single-line text, the selected option is accent-colored. */
@Composable
fun ColumnScope.DropdownOption(text: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    DropdownMenuItem(onClick = onClick, modifier = Modifier.fillMaxWidth().height(DropdownItemHeight)) {
        Text(
            text, color = if (selected) p.accent else p.text, fontSize = 15.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.fillMaxWidth(),
        )
    }
}
