package com.furuarc.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.i18n.ImageEditStr
import com.furuarc.ui.theme.LocalPalette
import kotlin.math.roundToInt

internal val IMG_SWATCHES = intArrayOf(
    0xFF000000.toInt(), 0xFFFFFFFF.toInt(), 0xFF808080.toInt(), 0xFFE53935.toInt(), 0xFFFB8C00.toInt(), 0xFFFDD835.toInt(),
    0xFF43A047.toInt(), 0xFF00ACC1.toInt(), 0xFF1E88E5.toInt(), 0xFF8E24AA.toInt(), 0xFFEC407A.toInt(), 0xFF6D4C41.toInt(),
)

internal fun hexOf(c: Int) = String.format("%06X", c and 0xFFFFFF)
internal fun parseHex(s: String): Int? {
    val t = s.trim().removePrefix("#")
    if (t.length != 6) return null
    return try { t.toLong(16).toInt() or 0xFF000000.toInt() } catch (e: Exception) { null }
}

/** Label with its value on one line and a slider below. */
@Composable
internal fun ISlider(
    label: String, value: Float, min: Float, max: Float,
    onChange: (Float) -> Unit, onFinish: () -> Unit = {}, decimals: Int = 0, suffix: String = "",
) {
    val p = LocalPalette.current
    val shown = if (decimals == 0) value.roundToInt().toString() else String.format("%.${decimals}f", value)
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = p.muted, fontSize = 13.sp)
            Text(shown + suffix, color = p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value.coerceIn(min, max), onValueChange = onChange, valueRange = min..max, onValueChangeFinished = onFinish,
            colors = SliderDefaults.colors(thumbColor = p.accent, activeTrackColor = p.accent),
        )
    }
}

@Composable
internal fun IChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) p.accent else p.field).clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
    ) {
        Text(label, color = if (selected) p.onAccent else p.text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Icon with a small label, used in the tool dock. */
@Composable
internal fun IDockBtn(icon: ImageVector, label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val p = LocalPalette.current
    Column(
        Modifier.width(66.dp).clip(RoundedCornerShape(12.dp)).background(if (selected) p.accent.copy(alpha = 0.18f) else Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, null, tint = if (!enabled) p.muted.copy(alpha = 0.5f) else if (selected) p.accent else p.text, modifier = Modifier.size(24.dp))
        Text(label, color = if (selected) p.accent else p.muted, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
internal fun ISmallBtn(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(p.field).clickable(enabled = enabled, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) { Text(label, color = if (enabled) p.text else p.muted.copy(alpha = 0.5f), fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

@Composable
internal fun IIconBtn(icon: ImageVector, tint: Color? = null, size: Dp = 36.dp, enabled: Boolean = true, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(Modifier.size(size).clip(CircleShape).clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = (tint ?: p.text).let { if (enabled) it else it.copy(alpha = 0.35f) }, modifier = Modifier.size(size * 0.6f))
    }
}

@Composable
internal fun IColorRow(color: Int, onChange: (Int) -> Unit, onMore: () -> Unit, onPipet: (() -> Unit)? = null) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(color)).border(2.dp, p.accent, CircleShape).clickable(onClick = onMore))
        IMG_SWATCHES.forEach { c ->
            Box(Modifier.size(28.dp).clip(CircleShape).background(Color(c)).border(1.dp, p.cardBorder, CircleShape).clickable { onChange(c) })
        }
        Box(Modifier.size(28.dp).clip(CircleShape).background(p.field).clickable(onClick = onMore), contentAlignment = Alignment.Center) {
            Text("+", color = p.text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        if (onPipet != null) IIconBtn(AppIcons.Colorize, size = 36.dp, onClick = onPipet)
    }
}

/** Full colour picker: hue, saturation, brightness and a hex code. */
@Composable
internal fun IColorDialog(initial: Int, e: ImageEditStr, onDismiss: () -> Unit, onOk: (Int) -> Unit) {
    val hsv = remember { FloatArray(3).also { android.graphics.Color.colorToHSV(initial, it) } }
    var h by remember { mutableStateOf(hsv[0]) }
    var s by remember { mutableStateOf(hsv[1]) }
    var v by remember { mutableStateOf(hsv[2]) }
    var hex by remember { mutableStateOf(hexOf(initial)) }
    val cur = android.graphics.Color.HSVToColor(floatArrayOf(h, s, v))
    fun upd(nh: Float, ns: Float, nv: Float) { h = nh; s = ns; v = nv; hex = hexOf(android.graphics.Color.HSVToColor(floatArrayOf(nh, ns, nv))) }
    AppDialog(e.customColor, onDismiss, buttons = {
        DlgBtn(e.cancel, false, onClick = onDismiss)
        DlgBtn(e.ok, true) { onOk(cur or 0xFF000000.toInt()) }
    }) {
        Box(Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(cur)))
        ISlider(e.hue, h, 0f, 360f, { upd(it, s, v) })
        ISlider(e.sat, s * 100f, 0f, 100f, { upd(h, it / 100f, v) })
        ISlider(e.value, v * 100f, 0f, 100f, { upd(h, s, it / 100f) })
        AppTextField(hex, { hex = it; parseHex(it)?.let { c -> val a = FloatArray(3); android.graphics.Color.colorToHSV(c, a); h = a[0]; s = a[1]; v = a[2] } }, e.hex, mono = true)
    }
}

@Composable
internal fun IPanel(content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    Column(
        Modifier.fillMaxWidth().background(p.card).heightIn(max = 250.dp).verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp), content = content,
    )
}

/** Thumbnail of a layer in the layer list. */
@Composable
internal fun LayerThumb(l: com.furuarc.core.Layer, size: Dp) {
    val p = LocalPalette.current
    Box(Modifier.size(size).clip(RoundedCornerShape(6.dp)).background(Color(0x33888888)).border(1.dp, p.cardBorder, RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
        val b = l.bmp
        when {
            b != null -> Image(b.asImageBitmap(), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            l.kind == com.furuarc.core.LKind.FILL -> Box(Modifier.fillMaxSize().background(Color(l.fill)))
            l.kind == com.furuarc.core.LKind.TEXT -> Icon(AppIcons.TextFields, null, tint = p.text, modifier = Modifier.size(size * 0.6f))
            else -> Icon(AppIcons.Shapes, null, tint = p.text, modifier = Modifier.size(size * 0.6f))
        }
    }
}
