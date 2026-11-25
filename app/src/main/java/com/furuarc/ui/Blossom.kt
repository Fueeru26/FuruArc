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
import androidx.compose.foundation.layout.matchParentSize
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
 * Soft blue glow below and around a glass surface. The surface itself is cut out of the glow (ClipOp.Difference), so nothing
 * shines through the translucent fill.
 */
fun Modifier.glassShadow(radius: Dp, dark: Boolean): Modifier = this.drawBehind {
    val r = radius.toPx()
    val outline = Path().apply { addRoundRect(RoundRect(0f, 0f, size.width, size.height, r, r)) }
    clipPath(outline, ClipOp.Difference) {
        val steps = 12
        val reach = 16.dp.toPx()
        val dy = 6.dp.toPx()
        val base = if (dark) Color(0xFF3A5BC8) else Color(0xFF3C5BDB)
        val peak = if (dark) 0.03f else 0.04f
        for (i in steps downTo 1) {
            val grow = reach * i / steps
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

/** Glass card of the Blossom style: translucent gradient fill, light rim, blue glow; [deco] adds the small flowers in the bottom-right corner. */
@Composable
fun BlossomCard(modifier: Modifier, src: MutableInteractionSource, deco: Boolean, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    val fill = if (p.dark) Brush.linearGradient(listOf(Color(0xFF3A4F94).copy(alpha = 0.30f), Color(0xFF16204A).copy(alpha = 0.42f)))
    else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.80f), Color(0xFFE4EBFF).copy(alpha = 0.58f)))
    val rim = if (p.dark) Brush.linearGradient(listOf(Color(0xFF5B72B0).copy(alpha = 0.90f), Color(0xFF2C3A6A).copy(alpha = 0.70f)))
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
                    modifier = Modifier.align(Alignment.BottomEnd).requiredSize(width = 190.dp, height = 110.dp),
                )
            }
        }
        CompositionLocalProvider(LocalCardSource provides src) { content() }
    }
}

/** Thin ">" arrow at the right edge of menu cards. */
@Composable
fun BlossomChevron(color: Color) {
    Canvas(Modifier.size(width = 14.dp, height = 24.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.12f)
            lineTo(w * 0.82f, h * 0.5f)
            lineTo(w * 0.18f, h * 0.88f)
        }
        drawPath(path, color, style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

/** Menu row of the Blossom style: gradient icon badge, thin divider, title + subtitle, arrow. */
@Composable
fun BlossomMenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, badge: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    val badgeShape = RoundedCornerShape(IconRadius)
    AppCard(Modifier.fillMaxWidth(), deco = true) {
        Row(
            Modifier.cardClickable(onClick).padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(62.dp).clip(badgeShape).background(p.brush)
                    .then(if (p.dark) Modifier.border(1.dp, Color.White.copy(alpha = 0.18f), badgeShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.material.Icon(imageVector = icon, contentDescription = null, tint = p.onGrad, modifier = Modifier.size(32.dp))
                if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 5.dp, end = 5.dp).size(10.dp).clip(CircleShape).background(BadgeRed))
            }
            Box(Modifier.padding(horizontal = 13.dp).width(1.dp).height(58.dp).background(if (p.dark) Color(0xFF5F77B8).copy(alpha = 0.55f) else p.muted.copy(alpha = 0.30f)))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 19.5.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = p.text)
                Text(subtitle, fontSize = 14.5.sp, lineHeight = 18.5.sp, color = p.muted)
            }
            Spacer(Modifier.width(8.dp))
            BlossomChevron(if (p.dark) Color(0xFFD8E6FF) else p.accent)
        }
    }
}

/** Tab title of the Blossom style: blue blossom icon, bold title and a gradient line that fades out to the right. */
@Composable
fun BlossomHeader(title: String) {
    val p = LocalPalette.current
    val iconRes = drawableId(if (p.dark) "blossom_icon_dark" else "blossom_icon")
    val c0 = if (p.dark) Color.White else p.accent
    val c1 = if (p.dark) Color(0xFF5D78BD) else p.accent.copy(alpha = 0.55f)
    Column(Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 26.dp)) {
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            if (iconRes != 0) Image(painter = painterResource(iconRes), contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(14.dp))
            Text(title, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = p.text, maxLines = 1)
        }
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier.fillMaxWidth(0.68f).height(3.dp).clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(c0, c1, Color.Transparent))),
        )
    }
}
