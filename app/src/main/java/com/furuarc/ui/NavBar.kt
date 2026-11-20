package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.ui.theme.LocalPalette

/** [badge] = small red dot on the icon (e.g. an app update is available). */
class NavItem(val icon: ImageVector, val label: String, val badge: Boolean = false)

/** Bottom navigation: the active tab is marked by an accent pill behind the icon + accent label. */
@Composable
fun AppBottomBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(p.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.cardBorder))
        Row(Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 8.dp)) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(i) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier
                            .size(width = 64.dp, height = 34.dp)
                            .clip(RoundedCornerShape(CardRadius))
                            .background(if (sel) p.accent.copy(alpha = 0.20f) else Color.Transparent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (sel) p.accent else p.muted,
                            modifier = Modifier.size(24.dp),
                        )
                        if (item.badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 12.dp).size(9.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BadgeRed))
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        item.label,
                        fontSize = 12.sp,
                        fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                        color = if (sel) p.accent else p.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

/** Large square card (icon in the center) with a title + description below the card. Used in the Game Tools tab. */
@Composable
fun BigToolCard(icon: ImageVector, title: String, subtitle: String = "", modifier: Modifier = Modifier, imageName: String = "", onClick: () -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(CardRadius)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(lerp(p.card, p.accent, 0.26f), lerp(p.card, p.accent, 0.06f))))
                .border(BorderStroke(1.5.dp, p.accent.copy(alpha = 0.75f)), shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            val res = remember(imageName) { if (imageName.isEmpty()) 0 else ctx.resources.getIdentifier(imageName, "drawable", ctx.packageName) }
            if (res != 0) androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(res),
                contentDescription = null,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.size(80.dp).clip(RoundedCornerShape(CardRadius)).background(if (res != 0) Color.Black.copy(alpha = 0.40f) else p.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = if (res != 0) Color.White else p.accent, modifier = Modifier.size(48.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            title, fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold,
            color = if (p.dark) Color.White else p.accent, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle, fontSize = 12.sp, lineHeight = 16.sp,
                color = p.muted, textAlign = TextAlign.Center, maxLines = 3, overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
