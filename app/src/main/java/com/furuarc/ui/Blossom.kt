package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.ui.theme.LocalPalette

/** Looks up a drawable by name (the blossom art lives in res/drawable-nodpi); 0 when it does not exist. */
@Composable
private fun drawableId(name: String): Int {
    val ctx = LocalContext.current
    return remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName) }
}

/**
 * Soft blue glow below and around a surface, [reach] thick (4dp). The surface itself is cut out of the glow (ClipOp.Difference),
 * so nothing shines through its edges.
 */
fun Modifier.glassShadow(radius: Dp, dark: Boolean, reach: Dp = 4.dp): Modifier = this.drawBehind {
    val r = radius.toPx()
    val outline = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, r, r)) }
    clipPath(outline, ClipOp.Difference) {
        val steps = 8
        val reachPx = reach.toPx()
        val dy = reachPx * 0.4f
        val base = if (dark) Color(0xFF3A5BC8) else Color(0xFF3C5BDB)
        val peak = if (dark) 0.05f else 0.06f
        for (i in steps downTo 1) {
            val grow = reachPx * i / steps
            val f = 1f - (i - 1) / steps.toFloat()
            drawRoundRect(
                color = base.copy(alpha = peak * f),
                topLeft = Offset(-grow, -grow + dy),
                size = Size(size.width + 2 * grow, size.height + 2 * grow),
                cornerRadius = CornerRadius(r + grow),
            )
        }
    }
}

/** Glass card of the Blossom style: translucent gradient fill, light rim, blue glow; [deco] adds the small flowers in the bottom-right corner ([decoScale] shrinks them for small cards). */
@Composable
fun BlossomCard(modifier: Modifier, src: MutableInteractionSource, deco: Boolean, decoScale: Float = 1f, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    // opaque (nothing shines through); blue glow shadow, 4dp thick
    val fill = if (p.dark) Brush.linearGradient(listOf(Color(0xFF1F2B4F), Color(0xFF151F40)))
    else Brush.linearGradient(listOf(Color.White, Color(0xFFE9EEFF)))
    val rim = if (p.dark) Brush.linearGradient(listOf(Color(0xFF5B72B0), Color(0xFF2C3A6A)))
    else Brush.linearGradient(listOf(Color.White, Color(0xFFC9D6FF)))
    val decoRes = drawableId(if (p.dark) "blossom_deco_dark" else "blossom_deco_light")
    Box(modifier.glassShadow(CardRadius, p.dark).clip(shape).background(fill).border(BorderStroke(1.5.dp, rim), shape)) {
        if (deco && decoRes != 0) {
            Box(Modifier.matchParentSize()) {
                Image(
                    painter = painterResource(decoRes),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    alpha = if (p.dark) 0.85f else 0.95f,
                    modifier = Modifier.align(Alignment.BottomEnd).requiredSize(width = 190.dp * decoScale, height = 110.dp * decoScale),
                )
            }
        }
        CompositionLocalProvider(LocalCardSource provides src) { content() }
    }
}

/** Menu row of the Blossom style: same sizes and positions as the Classic menu card, with a thin divider after the icon badge. */
@Composable
fun BlossomMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, badge: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth(), deco = true) {
        Row(
            Modifier.cardClickable(onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(IconRadius)).background(p.brush),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(imageVector = icon, contentDescription = null, tint = p.onGrad, modifier = Modifier.size(24.dp))
                if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp).size(10.dp).clip(CircleShape).background(BadgeRed))
            }
            Box(Modifier.padding(horizontal = 10.dp).width(1.dp).height(44.dp).background(if (p.dark) Color(0xFF5F77B8).copy(alpha = 0.55f) else p.muted.copy(alpha = 0.30f)))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                Text(subtitle, fontSize = 14.sp, lineHeight = 20.sp, color = p.muted)
            }
        }
    }
}

/** Title with the blue blossom icon; same text size and colour as the Classic tab title. */
@Composable
fun BlossomTitle(title: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val iconRes = drawableId(if (p.dark) "blossom_icon_dark" else "blossom_icon")
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        if (iconRes != 0) Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(10.dp))
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (p.dark) Color.White else p.accent, maxLines = 1)
    }
}

/** Gradient line under a tab title; it fades out to the right. */
@Composable
fun BlossomUnderline(modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val c0 = if (p.dark) Color.White else p.accent
    val c1 = if (p.dark) Color(0xFF5D78BD) else p.accent.copy(alpha = 0.55f)
    // raised by 8dp so the line never touches the card below the header
    Box(
        modifier.padding(bottom = 8.dp).fillMaxWidth(0.68f).height(3.dp).clip(CircleShape)
            .background(Brush.horizontalGradient(listOf(c0, c1, Color.Transparent))),
    )
}

/** Tab title bar of the Blossom style: 64dp tall like the Classic one, with the gradient line at its bottom edge. */
@Composable
fun BlossomHeader(title: String) {
    Box(Modifier.fillMaxWidth().height(64.dp)) {
        BlossomTitle(title, Modifier.align(Alignment.CenterStart))
        BlossomUnderline(Modifier.align(Alignment.BottomStart))
    }
}

/**
 * Label background of one row in a list (a translation row of the Translation Editor): rounded panel with a thin rim.
 * Blue Blossom uses the same fill and rim as its cards (without glow and flowers); the other styles use the card colour.
 * It does not animate or react to touch, so typing inside it is never disturbed.
 */
@Composable
fun RowPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    val blossom = com.furuarc.ui.theme.AppStyle.ui == com.furuarc.ui.theme.UiStyle.BLOSSOM
    val fill = when {
        blossom && p.dark -> Brush.linearGradient(listOf(Color(0xFF1F2B4F), Color(0xFF151F40)))
        blossom -> Brush.linearGradient(listOf(Color.White, Color(0xFFE9EEFF)))
        else -> Brush.linearGradient(listOf(p.card, p.card))
    }
    val rim = when {
        blossom && p.dark -> Brush.linearGradient(listOf(Color(0xFF5B72B0), Color(0xFF2C3A6A)))
        blossom -> Brush.linearGradient(listOf(Color.White, Color(0xFFC9D6FF)))
        else -> Brush.linearGradient(listOf(p.cardBorder, p.cardBorder))
    }
    Box(modifier.clip(shape).background(fill).border(BorderStroke(1.dp, rim), shape)) { content() }
}
