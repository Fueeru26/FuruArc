package com.furuarc.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.i18n.AboutStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

private fun openLink(ctx: Context, url: String, fail: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, fail, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AboutMenuScreen(onHow: () -> Unit, onQna: () -> Unit, onTrouble: () -> Unit, onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    MenuListScreen(
        a.title,
        listOf(
            MenuSpec(AppIcons.File, a.howTitle, a.howSub, onHow),
            MenuSpec(AppIcons.Edit, a.qnaTitle, a.qnaSub, onQna),
            MenuSpec(AppIcons.Build, a.troubleTitle, a.troubleSub, onTrouble),
        ),
        onBack,
    )
}

/** Case-insensitive highlight of every [q] in [text] (the searched word is marked). */
private fun highlight(text: String, q: String, color: androidx.compose.ui.graphics.Color): androidx.compose.ui.text.AnnotatedString =
    androidx.compose.ui.text.buildAnnotatedString {
        val needle = q.trim().lowercase()
        if (needle.isEmpty()) { append(text); return@buildAnnotatedString }
        val low = text.lowercase()
        // lowercase() can change the length of rare characters; then the text is shown unmarked instead of wrongly marked
        if (low.length != text.length) { append(text); return@buildAnnotatedString }
        var i = 0
        while (true) {
            val j = low.indexOf(needle, i)
            if (j < 0) { append(text.substring(i)); break }
            append(text.substring(i, j))
            pushStyle(androidx.compose.ui.text.SpanStyle(background = color))
            append(text.substring(j, j + needle.length))
            pop()
            i = j + needle.length
        }
    }

/**
 * Guide page (How to Use, Q&A, Troubleshooting): collapsible entries, a header that keeps its size while scrolling and a
 * search icon at its right end. The search bar (with previous / next buttons) opens below the header; entries that contain
 * the word open by themselves and the word is marked. [bodyMuted] = the answer uses the muted colour (Q&A); the other pages keep the normal text colour.
 */
@Composable
private fun GuideListScreen(title: String, entries: List<Pair<String, String>>, bodyMuted: Boolean, onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    val p = LocalPalette.current
    var open by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var searching by remember { mutableStateOf(false) }
    var q by remember { mutableStateOf("") }
    var cur by remember { mutableStateOf(0) }
    val state = rememberLazyListState()
    val needle = if (searching) q.trim() else ""
    val matches = remember(needle, entries) {
        if (needle.isEmpty()) emptyList() else entries.indices.filter { entries[it].first.contains(needle, true) || entries[it].second.contains(needle, true) }
    }
    // a matching entry opens by itself (the user can still close it afterwards)
    LaunchedEffect(matches) {
        cur = 0
        if (matches.isNotEmpty()) {
            open = open + matches
            state.animateScrollToItem(matches[0])
        }
    }
    fun go(step: Int) {
        if (matches.isEmpty()) return
        cur = (cur + step + matches.size) % matches.size
    }
    LaunchedEffect(cur) { if (matches.isNotEmpty()) state.animateScrollToItem(matches[cur.coerceIn(0, matches.size - 1)]) }
    val mark = p.accent.copy(alpha = 0.40f)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(title, onBack, fixed = true, trailing = {
            IconButton(onClick = { searching = !searching; if (!searching) q = "" }) {
                Icon(Icons.Default.Search, a.searchBtn, tint = if (searching) p.accent else p.text)
            }
        })
        if (searching) {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { AppTextField(q, { q = it }, a.searchHint) }
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).clickableScale(enabled = matches.size > 1, pressed = 0.88f) { go(-1) }, contentAlignment = Alignment.Center) {
                    Text("‹", color = if (matches.size > 1) p.accent else p.muted, fontSize = 26.sp)
                }
                Box(Modifier.size(44.dp).clip(RoundedCornerShape(22.dp)).clickableScale(enabled = matches.size > 1, pressed = 0.88f) { go(1) }, contentAlignment = Alignment.Center) {
                    Text("›", color = if (matches.size > 1) p.accent else p.muted, fontSize = 26.sp)
                }
            }
            if (needle.isNotEmpty()) {
                Text(
                    if (matches.isEmpty()) a.searchNone else "${cur.coerceIn(0, matches.size - 1) + 1} / ${matches.size}",
                    color = p.muted, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        LazyColumn(state = state, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            items(entries.size) { i ->
                val (h, b) = entries[i]
                val isOpen = i in open
                AppCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.clickable { open = if (isOpen) open - i else open + i }.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row {
                            Text(highlight(h, needle, mark), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 21.sp, modifier = Modifier.weight(1f))
                            Text(if (isOpen) "−" else "+", color = p.accent, fontSize = 20.sp, modifier = Modifier.padding(start = 12.dp))
                        }
                        if (isOpen) Text(highlight(b, needle, mark), color = if (bodyMuted) p.muted else p.text, fontSize = 14.sp, lineHeight = 21.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun HowToScreen(onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    GuideListScreen(a.howTitle, a.howTo, false, onBack)
}

@Composable
fun QnaScreen(onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    GuideListScreen(a.qnaTitle, a.qna, true, onBack)
}

@Composable
fun TroubleScreen(onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    GuideListScreen(a.troubleTitle, a.trouble, false, onBack)
}

@Composable
fun SourceScreen(onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
    ScreenHeader(a.srcTitle, onBack)
    LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
        item { Text(a.srcIntro, color = p.muted, fontSize = 13.sp, lineHeight = 19.sp) }
        items(a.sources.size) { i ->
            val s = a.sources[i]
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.clickable { openLink(ctx, s.url, a.openFail) }.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(s.name, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(s.desc, color = p.muted, fontSize = 14.sp, lineHeight = 20.sp)
                    Text(s.url, color = p.accent, fontSize = 13.sp, textDecoration = TextDecoration.Underline)
                }
            }
        }
        item { Text(a.srcOutro, color = p.muted, fontSize = 12.sp, lineHeight = 18.sp) }
    }
    }
}

@Composable
fun DeveloperScreen(vm: com.furuarc.MainViewModel, onBack: () -> Unit) {
    val a = AboutStr.of(LocalLang.current)
    val p = LocalPalette.current
    val ctx = LocalContext.current
    // icon + hyperlinked label (only the label text opens the link)
    @Composable
    fun Link(c: com.furuarc.i18n.ContactItem) {
        val icon = if (c.label == "Github") AppIcons.Github else AppIcons.Email
        Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = p.iconFg, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                c.label, color = p.accent, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline,
                maxLines = 1, softWrap = false,
                modifier = Modifier.clickable { openLink(ctx, c.url, a.openFail) }.padding(vertical = 4.dp),
            )
        }
    }
    // "Ko-fi (For Global)": only the platform name is the hyperlink
    @Composable
    fun Donate(d: com.furuarc.i18n.DonateItem) {
        Text(
            d.name, color = p.accent, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline,
            maxLines = 1, softWrap = false,
            modifier = Modifier.clickable { openLink(ctx, d.url, a.openFail) }.padding(vertical = 4.dp),
        )
    }
    val st by vm.state.collectAsState()
    var withLog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
    ScreenHeader(a.devTitle, onBack)
    LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(a.devName, color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(a.devRole, color = p.muted, fontSize = 14.sp)
                    }
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(com.furuarc.R.drawable.app_icon),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                    )
                }
            }
        }
        item { SectionCard(a.contactHeader) { Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { a.contacts.forEach { Link(it) } } } }
        item {
            SectionCard(a.donateHeader) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) { a.donations.forEach { Donate(it) } }
                    Text(a.donateNote, color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                }
            }
        }
        item {
            AppCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(a.collabHeader, fontSize = 12.sp, color = p.muted, modifier = Modifier.weight(1f))
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(com.furuarc.R.drawable.furukammi_logo),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                    Text(a.collabName, color = p.text, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(a.collabThanks, color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                    Text(
                        a.collabVisit, color = p.accent, fontWeight = FontWeight.SemiBold, textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable { openLink(ctx, a.collabUrl, a.openFail) }.padding(vertical = 4.dp),
                    )
                }
            }
        }
        item {
            SectionCard(a.reportHeader) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(a.reportNote, color = p.muted, fontSize = 13.sp, lineHeight = 19.sp)
                    Row(Modifier.fillMaxWidth().clickable { withLog = !withLog }, verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material.Checkbox(checked = withLog, onCheckedChange = { withLog = it })
                        Text(a.reportLog, color = p.text, fontSize = 14.sp)
                    }
                    PrimaryButton(a.reportBtn, true) { sendReport(ctx, a, if (withLog) st.log else emptyList()) }
                }
            }
        }
        item { SectionCard(a.licenseHeader) { Text(a.licenseBody, color = p.text, fontSize = 14.sp, lineHeight = 21.sp) } }
    }
    }
}

private fun sendReport(ctx: Context, a: AboutStr, log: List<String>) {
    val version = try { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName } catch (e: Exception) { "?" }
    val body = StringBuilder()
    body.append(a.reportIntro).append("\n\n\n")
    body.append("---\n")
    body.append("FuruArc ").append(version).append('\n')
    body.append("Android ").append(android.os.Build.VERSION.RELEASE).append(" (SDK ").append(android.os.Build.VERSION.SDK_INT).append(")\n")
    body.append(android.os.Build.MANUFACTURER).append(' ').append(android.os.Build.MODEL).append('\n')
    if (log.isNotEmpty()) {
        body.append("\nLog:\n")
        log.takeLast(40).forEach { body.append(it).append('\n') }
    }
    val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        .putExtra(Intent.EXTRA_EMAIL, arrayOf("fueerugame@gmail.com"))
        .putExtra(Intent.EXTRA_SUBJECT, a.reportSubject + " " + version)
        .putExtra(Intent.EXTRA_TEXT, body.toString())
    try {
        ctx.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(ctx, a.noMailApp, Toast.LENGTH_SHORT).show()
    }
}
