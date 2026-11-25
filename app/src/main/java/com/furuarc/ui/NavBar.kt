package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.shadow
import com.furuarc.ui.theme.AppStyle
import com.furuarc.ui.theme.NavStyle
import com.furuarc.ui.theme.LocalPalette

/** [badge] = small red dot on the icon (e.g. an app update is available). */
class NavItem(val icon: ImageVector, val label: String, val badge: Boolean = false)

/** Bottom navigation. The look depends on the selected [NavStyle] (Settings > Display). */
@Composable
fun AppBottomBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    when (AppStyle.nav) {
        NavStyle.CLASSIC -> ClassicBar(items, selected, onSelect)
        NavStyle.FLOATING -> Unit // drawn over the content by FloatingNav (see MainActivity)
        NavStyle.ICONS -> IconsBar(items, selected, onSelect)
        NavStyle.PILL -> PillBar(items, selected, onSelect)
        NavStyle.TOPLINE -> TopLineBar(items, selected, onSelect)
        NavStyle.FLOAT_ICONS -> Unit
        NavStyle.FAB -> Unit // drawn as an overlay by FabNav (see MainActivity)
    }
}

@Composable
private fun NavIcon(item: NavItem, tint: Color, size: androidx.compose.ui.unit.Dp = 24.dp) {
    Box(contentAlignment = Alignment.Center) {
        Icon(imageVector = item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
        if (item.badge) Box(Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-2).dp).size(9.dp).clip(CircleShape).background(BadgeRed))
    }
}

@Composable
private fun NavLabel(text: String, sel: Boolean, color: Color) {
    Text(
        text, fontSize = 12.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, color = color,
        maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
    )
}

/** Classic: the active tab is marked by an accent pill behind the icon + accent label. */
@Composable
private fun ClassicBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(p.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.cardBorder))
        Row(Modifier.fillMaxWidth().height(76.dp).padding(horizontal = 8.dp)) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                val pill by animateColorAsState(if (sel) p.accent.copy(alpha = 0.20f) else Color.Transparent)
                Column(
                    Modifier.weight(1f).fillMaxHeight().clickableScale(pressed = 0.9f, ripple = false) { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        Modifier.size(width = 64.dp, height = 34.dp).clip(RoundedCornerShape(CardRadius)).background(pill),
                        contentAlignment = Alignment.Center,
                    ) { NavIcon(item, if (sel) p.accent else p.muted) }
                    Spacer(Modifier.height(3.dp))
                    NavLabel(item.label, sel, if (sel) p.accent else p.muted)
                }
            }
        }
    }
}

/** The floating bars (Floating, Floating Icons) are drawn over the content, so nothing sits behind or below them. */
@Composable
fun FloatingNav(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    if (AppStyle.nav == NavStyle.FLOAT_ICONS) FloatingIconsBar(items, selected, onSelect) else FloatingBar(items, selected, onSelect)
}

/** Floating: a rounded bar that hovers above the bottom edge; the active tab expands into icon + label. */
@Composable
private fun FloatingBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(32.dp)
    Box(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp)) {
        Row(
            Modifier.fillMaxWidth().height(66.dp).shadow(10.dp, shape).clip(shape)
                .background(p.card).border(1.dp, p.cardBorder, shape).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                val w by animateFloatAsState(if (sel) 1.9f else 1f)
                val pill by animateColorAsState(if (sel) p.accent.copy(alpha = 0.22f) else Color.Transparent)
                Box(Modifier.weight(w).padding(horizontal = 3.dp), contentAlignment = Alignment.Center) {
                    Row(
                        Modifier.fillMaxWidth().height(50.dp).clip(RoundedCornerShape(25.dp)).background(pill)
                            .clickableScale(pressed = 0.9f, ripple = false) { onSelect(i) },
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NavIcon(item, if (sel) p.accent else p.muted)
                        if (sel) {
                            Spacer(Modifier.width(6.dp))
                            Text(item.label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.accent, maxLines = 1, overflow = TextOverflow.Clip)
                        }
                    }
                }
            }
        }
    }
}

/** Floating icons: like Floating (rounded bar with background), but icons only and larger; the active icon sits in an accent circle. */
@Composable
private fun FloatingIconsBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(32.dp)
    Box(Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 10.dp)) {
    Row(Modifier.fillMaxWidth().height(68.dp).shadow(10.dp, shape).clip(shape).background(p.card).border(1.dp, p.cardBorder, shape).padding(horizontal = 8.dp)) {
        items.forEachIndexed { i, item ->
            val sel = i == selected
            val sc by animateFloatAsState(if (sel) 1.1f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            val circle by animateColorAsState(if (sel) p.accent.copy(alpha = 0.22f) else Color.Transparent)
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(56.dp).scale(sc).clip(CircleShape).background(circle)
                        .clickableScale(pressed = 0.88f, ripple = false) { onSelect(i) },
                    contentAlignment = Alignment.Center,
                ) { NavIcon(item, if (sel) p.accent else p.muted, 34.dp) }
            }
        }
    }
    }
}

/** Floating button: one round button (bottom right) that opens the tabs as a fan of round buttons. Drawn over the screen. */
@Composable
fun FabNav(items: List<NavItem>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    var open by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val prog by animateFloatAsState(if (open) 1f else 0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow))
    val n = items.size
    val radius = 128f
    Box(modifier.fillMaxSize()) {
        if (open) Box(
            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f * prog))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { open = false },
        )
        Box(Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp).size(60.dp)) {
            // the fan: first tab at the top, last tab at the left
            items.forEachIndexed { k, item ->
                val ang = Math.toRadians((180.0 + (n - 1 - k) * 90.0 / (n - 1).coerceAtLeast(1)))
                val dx = (radius * Math.cos(ang)).toFloat() * prog
                val dy = (radius * Math.sin(ang)).toFloat() * prog
                if (prog > 0.02f) FabButton(
                    Modifier.align(Alignment.Center).offset(x = dx.dp, y = dy.dp).size(52.dp).scale(0.6f + 0.4f * prog).alpha(prog.coerceIn(0f, 1f)),
                    item, k == selected,
                ) { open = false; onSelect(k) }
            }
            val cur = items.getOrNull(selected)
            val anyBadge = items.any { it.badge }
            Box(
                Modifier.align(Alignment.Center).size(60.dp).shadow(if (p.dark) 0.dp else 6.dp, CircleShape).clip(CircleShape).background(p.card)
                    .border(1.dp, if (open) p.accent else p.cardBorder, CircleShape)
                    .clickableScale(pressed = 0.9f, ripple = false) { open = !open },
                contentAlignment = Alignment.Center,
            ) {
                if (open) Icon(androidx.compose.material.icons.Icons.Default.Close, null, tint = p.accent, modifier = Modifier.size(28.dp))
                else if (cur != null) NavIcon(NavItem(cur.icon, cur.label, anyBadge), p.accent, 28.dp)
            }
        }
    }
}

@Composable
private fun FabButton(modifier: Modifier, item: NavItem, sel: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier.shadow(if (p.dark) 0.dp else 4.dp, CircleShape).clip(CircleShape).background(p.card)
            .border(1.dp, if (sel) p.accent else p.cardBorder, CircleShape)
            .clickableScale(pressed = 0.88f, ripple = false, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { NavIcon(item, if (sel) p.accent else p.muted, 26.dp) }
}

/** Icons only: no labels, the active tab has a larger accent icon and a small dot. */
@Composable
private fun IconsBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(p.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.cardBorder))
        Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 8.dp)) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                val sc by animateFloatAsState(if (sel) 1.18f else 1f)
                val dot by animateFloatAsState(if (sel) 1f else 0f)
                Column(
                    Modifier.weight(1f).fillMaxHeight().clickableScale(pressed = 0.88f, ripple = false) { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.scale(sc)) { NavIcon(item, if (sel) p.accent else p.muted, 26.dp) }
                    Spacer(Modifier.height(5.dp))
                    Box(Modifier.size(5.dp).scale(dot).clip(CircleShape).background(p.brush))
                }
            }
        }
    }
}

/** Sliding pill: one accent pill glides from tab to tab behind the icon and label. */
@Composable
private fun PillBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(p.card)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(p.cardBorder))
        BoxWithConstraints(Modifier.fillMaxWidth().height(70.dp).padding(horizontal = 8.dp, vertical = 6.dp)) {
            val cell = maxWidth / items.size.coerceAtLeast(1)
            val x by animateDpAsState(cell * selected, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow))
            Box(Modifier.offset(x = x).width(cell).fillMaxHeight().padding(horizontal = 4.dp).clip(RoundedCornerShape(CardRadius + 6.dp)).background(p.softBrush))
            Row(Modifier.fillMaxSize()) {
                items.forEachIndexed { i, item ->
                    val sel = i == selected
                    Column(
                        Modifier.weight(1f).fillMaxHeight().clickableScale(pressed = 0.92f, ripple = false) { onSelect(i) },
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    ) {
                        NavIcon(item, if (sel) p.accent else p.muted)
                        Spacer(Modifier.height(2.dp))
                        NavLabel(item.label, sel, if (sel) p.accent else p.muted)
                    }
                }
            }
        }
    }
}

/** Top line: a short accent bar slides along the top edge of the bar; active tab is icon + label without a pill. */
@Composable
private fun TopLineBar(items: List<NavItem>, selected: Int, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Column(Modifier.fillMaxWidth().background(p.card)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(3.dp).background(p.cardBorder)) {
            val cell = maxWidth / items.size.coerceAtLeast(1)
            val x by animateDpAsState(cell * selected + (cell - 40.dp) / 2, spring(stiffness = Spring.StiffnessMediumLow))
            Box(Modifier.offset(x = x).width(40.dp).fillMaxHeight().clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)).background(p.brush))
        }
        Row(Modifier.fillMaxWidth().height(66.dp).padding(horizontal = 8.dp)) {
            items.forEachIndexed { i, item ->
                val sel = i == selected
                val sc by animateFloatAsState(if (sel) 1.1f else 1f)
                Column(
                    Modifier.weight(1f).fillMaxHeight().clickableScale(pressed = 0.9f, ripple = false) { onSelect(i) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.scale(sc)) { NavIcon(item, if (sel) p.accent else p.muted) }
                    Spacer(Modifier.height(3.dp))
                    NavLabel(item.label, sel, if (sel) p.accent else p.muted)
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
                .clickableScale(pressed = 0.95f, onClick = onClick),
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
