package com.furuarc.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.Summary
import com.furuarc.UiState
import com.furuarc.core.formatBytes
import com.furuarc.core.formatDuration
import com.furuarc.i18n.x
import com.furuarc.ui.theme.LocalNativeInput
import com.furuarc.ui.theme.LocalPalette
import com.furuarc.ui.theme.LocalStr
import com.furuarc.ui.theme.Qanelas

/** Card & button corners are small (following the cover cards in the reference design): 10dp, 8dp square icon, 16dp spacing between items. */
val CardRadius = 10.dp
val ControlRadius = 10.dp
val IconRadius = 8.dp

/** Spacing between items within one screen. */
val ScreenGap = 16.dp

/** Screen padding: 16dp left/right/bottom. The top is handled by [ScreenHeader] (64dp tall). */
val ScreenPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)

/** Base card: theme-colored background, large corners, thin border. */
@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    Surface(
        shape = RoundedCornerShape(CardRadius),
        color = p.card,
        border = BorderStroke(1.dp, p.cardBorder),
        modifier = modifier,
        content = content,
    )
}

/** 64dp-tall title bar: 24dp back icon aligned with the content edge, 22sp title. */
@Composable
fun ScreenHeader(title: String, onBack: () -> Unit, trailing: (@Composable () -> Unit)? = null) {
    val p = LocalPalette.current
    val s = LocalStr.current
    Row(
        Modifier.fillMaxWidth().height(64.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onBack,
            // 48dp touch area, but only 24dp occupies the layout so the icon aligns with the content edge
            modifier = Modifier.layout { m, c ->
                val inset = 12.dp.roundToPx()
                val pl = m.measure(c)
                layout(pl.width - 2 * inset, pl.height) { pl.place(-inset, 0) }
            },
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = s.back, tint = p.text)
        }
        Spacer(Modifier.width(16.dp))
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = p.text, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (trailing != null) trailing()
    }
}

/** Main tab title (no back button): accented, same height as [ScreenHeader]. */
@Composable
fun HomeHeader(title: String) {
    val p = LocalPalette.current
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.CenterStart) {
        val dark = p.background.luminance() < 0.5f
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (dark) Color.White else p.accent)
    }
}

@Composable
private fun IconBox(icon: ImageVector, bg: Color, fg: Color) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(IconRadius)).background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(24.dp))
    }
}

/** Picker card: icon, small label, large value, small button on the right. */
@Composable
fun PickerCard(
    icon: ImageVector,
    label: String,
    value: String?,
    trailingIcon: ImageVector,
    onClick: () -> Unit,
) {
    val p = LocalPalette.current
    val s = LocalStr.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBox(icon, p.iconBg, p.iconFg)
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(label, fontSize = 12.sp, lineHeight = 16.sp, color = p.muted)
                Text(
                    value ?: s.tapToSelect,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = p.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconBox(trailingIcon, p.trailingBg, p.trailingFg)
        }
    }
}

val BadgeRed = Color(0xFFFF2D2D)

/** Menu card: 48dp rounded-square icon on the left, bold 16sp title + 14sp subtitle (no arrow). */
@Composable
fun MenuCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) = MenuCard(icon, title, subtitle, false, onClick)

/** Same as above; [badge] draws a small red dot at the top-right of the icon. */
@Composable
fun MenuCard(icon: ImageVector, title: String, subtitle: String, badge: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.clickable(onClick = onClick).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(48.dp).clip(RoundedCornerShape(IconRadius)).background(p.iconBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = p.iconFg, modifier = Modifier.size(24.dp))
                if (badge) Box(Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp).size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BadgeRed))
            }
            Column(Modifier.weight(1f).padding(start = 20.dp)) {
                Text(title, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                Text(subtitle, fontSize = 14.sp, lineHeight = 20.sp, color = p.muted)
            }
        }
    }
}

/** Wrapper card with a small label above its content. */
@Composable
fun SectionCard(label: String, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(label, fontSize = 12.sp, color = p.muted)
            content()
        }
    }
}

@Composable
fun SegmentedChips(items: List<String>, selected: Int, onSelect: (Int) -> Unit) = SegmentedChips(items, selected, 16.sp, onSelect)

@Composable
fun SegmentedChips(items: List<String>, selected: Int, fontSize: androidx.compose.ui.unit.TextUnit, onSelect: (Int) -> Unit) {
    val p = LocalPalette.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEachIndexed { i, label ->
            val sel = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(ControlRadius - 4.dp))
                    .background(if (sel) p.accent else p.trailingBg)
                    .clickable { onSelect(i) },
                contentAlignment = Alignment.Center,
            ) {
                OneLineText(
                    label,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = if (sel) p.onAccent else p.text,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
            }
        }
    }
}

/** Dropdown choice: field-styled box that opens a list menu. [options] = (id, label). */
@Composable
fun AppDropdown(label: String, value: String, options: List<Pair<String, String>>, enabled: Boolean = true, onSelect: (String) -> Unit) {
    val p = LocalPalette.current
    var open by remember { mutableStateOf(false) }
    val shown = options.firstOrNull { it.first == value }?.second ?: value
    val anchorW = remember { mutableStateOf(0.dp) }
    Box(Modifier.fillMaxWidth().trackWidth(anchorW)) {
        Surface(
            shape = RoundedCornerShape(CardRadius), color = p.card,
            border = BorderStroke(1.dp, p.outline),
            modifier = Modifier.fillMaxWidth().clickable(enabled = enabled) { open = true },
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Text(label, color = p.muted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (shown.isEmpty()) "-" else shown, color = p.text, fontSize = 16.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                    )
                    Text("▾", color = p.muted, fontSize = 16.sp)
                }
            }
        }
        FieldDropdownMenu(expanded = open, anchorWidth = anchorW.value, onDismiss = { open = false }) {
            options.forEach { (id, lbl) ->
                DropdownOption(lbl, id == value) { open = false; onSelect(id) }
            }
        }
    }
}

/** File name field: the [ext] extension part stays fixed on the right and cannot be edited/deleted. */
@Composable
fun FileNameField(base: String, ext: String, label: String, onChange: (String) -> Unit) {
    val p = LocalPalette.current
    AppTextField(
        value = base,
        onValueChange = { onChange(it.filter { c -> c !in "/\\:*?\"<>|" && c != '\n' }) },
        label = label,
        mono = true,
        trailing = { Text(ext, color = p.muted, fontFamily = FontFamily.Monospace, fontSize = 16.sp, modifier = Modifier.padding(end = 12.dp)) },
    )
}

/** Indeterminate progress indicator with a caption below it. */
@Composable
fun BusyBar() {
    val p = LocalPalette.current
    val x = LocalStr.current.x
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(Modifier.fillMaxWidth(), color = p.accent)
        Text(x.processing, color = p.muted, fontSize = 13.sp)
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    mono: Boolean = false,
    number: Boolean = false,
    multiline: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    password: Boolean = false,
) {
    // "Raw text input" (System Settings): Android EditText, so a held delete key keeps deleting
    if (LocalNativeInput.current && !password) {
        NativeTextField(value, onValueChange, label, mono, number, multiline, trailing)
        return
    }
    val p = LocalPalette.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        singleLine = !multiline,
        maxLines = if (multiline) 8 else 1,
        trailingIcon = trailing,
        shape = RoundedCornerShape(ControlRadius),
        textStyle = TextStyle(
            fontFamily = if (mono) FontFamily.Monospace else Qanelas,
            fontSize = 16.sp,
        ),
        visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            textColor = p.text,
            cursorColor = p.accent,
            focusedBorderColor = p.accent,
            unfocusedBorderColor = p.outline,
            focusedLabelColor = p.accent,
            unfocusedLabelColor = p.muted,
        ),
        modifier = Modifier.fillMaxWidth().reportFocus(),
    )
}

/**
 * Single-line text for buttons: if too long, the font size is reduced gradually (min 10sp)
 * until it fits, so the text never wraps to a second line or gets cut off.
 */
@Composable
fun OneLineText(
    text: String,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    var size by remember(text, fontSize) { mutableStateOf(fontSize.value) }
    Text(
        text,
        fontSize = size.sp,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        textAlign = TextAlign.Center,
        modifier = modifier,
        onTextLayout = { r -> if (r.didOverflowWidth && size > 10f) size -= 0.5f },
    )
}

@Composable
fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(ControlRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = p.accent,
            contentColor = p.onAccent,
            disabledBackgroundColor = p.disabledBg,
            disabledContentColor = p.disabledFg,
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth().height(44.dp),
    ) {
        OneLineText(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) = SecondaryButton(text, true, onClick)

@Composable
fun SecondaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    val p = LocalPalette.current
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(ControlRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = p.trailingBg,
            contentColor = p.text,
            disabledBackgroundColor = p.disabledBg,
            disabledContentColor = p.disabledFg,
        ),
        elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
        modifier = Modifier.fillMaxWidth().height(44.dp),
    ) {
        OneLineText(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoCard(lines: List<Pair<String, String>>) {
    val p = LocalPalette.current
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            lines.forEach { line ->
                Row {
                    Text(line.first + ": ", color = p.muted)
                    Text(line.second, color = p.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** Progress bar + "Processing … NN%" + size/estimated remaining time + the file currently being processed. */
@Composable
fun ProgressBlock(s: UiState) {
    val p = LocalPalette.current
    val str = LocalStr.current
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(
            progress = s.progress,
            color = p.accent,
            backgroundColor = p.trailingBg,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (s.running) str.processing else str.finished,
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = p.muted, modifier = Modifier.weight(1f),
            )
            Text(
                "${(s.progress * 100).toInt().coerceIn(0, 100)}%",
                fontSize = 13.sp, fontWeight = FontWeight.Bold, color = p.accent,
            )
        }
        if (s.running && s.totalBytes > 0) {
            var line = str.bytesProgress(formatBytes(s.doneBytes), formatBytes(s.totalBytes))
            if (s.etaSec >= 0) line += " · " + str.etaLeft(formatDuration(s.etaSec))
            Text(line, fontSize = 12.sp, color = p.muted)
        }
        if (s.current.isNotEmpty()) {
            Text(s.current, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = p.text)
        }
    }
}

/** Result summary card + Open folder / Copy log buttons. */
@Composable
fun SummaryCard(
    sum: Summary,
    onOpenFolder: () -> Unit,
    onCopyLog: () -> Unit,
    onVerify: (() -> Unit)?,
    title: String? = null,
    okLabel: String? = null,
    onTranslate: (() -> Unit)? = null,
) {
    val p = LocalPalette.current
    val s = LocalStr.current
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title ?: if (sum.pack) s.summaryPack else s.summaryUnpack,
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.accent,
            )
            val rows = ArrayList<Pair<String, String>>()
            if (sum.pack) {
                rows.add(Pair(s.infoFiles, sum.ok.toString()))
                if (sum.skipped > 0) rows.add(Pair(s.sumSkipped, sum.skipped.toString()))
            } else {
                rows.add(Pair(okLabel ?: s.sumOk, sum.ok.toString()))
                rows.add(Pair(s.sumFailed, sum.failed.toString()))
                rows.add(Pair(s.sumSkipped, sum.skipped.toString()))
            }
            rows.add(Pair(s.sumSize, formatBytes(sum.bytes)))
            rows.add(Pair(s.sumTime, formatDuration(sum.seconds.toLong())))
            rows.forEach { r ->
                Row {
                    Text(r.first + ": ", color = p.muted)
                    Text(r.second, color = p.text, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { SecondaryButton(s.btnOpenFolder, onOpenFolder) }
                Box(Modifier.weight(1f)) { SecondaryButton(s.btnCopyLog, onCopyLog) }
            }
            if (onTranslate != null) SecondaryButton(s.x.btnTranslateText, onTranslate)
            if (onVerify != null) SecondaryButton(if (sum.pack) s.x.btnVerifyPack else s.btnVerify, onVerify)
        }
    }
}

/** Result / progress of "Check extraction result". */
@Composable
fun VerifyCard(st: UiState) {
    val p = LocalPalette.current
    val s = LocalStr.current
    val r = st.verifyResult
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (st.verifyPack) s.x.vPackTitle else s.vTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.accent)
            if (st.verifying) {
                LinearProgressIndicator(
                    progress = if (st.verifyTotal == 0) 0f else st.verifyDone / st.verifyTotal.toFloat(),
                    color = p.accent,
                    backgroundColor = p.trailingBg,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(s.vChecking(st.verifyDone, st.verifyTotal), color = p.muted, fontSize = 13.sp)
            } else if (r != null) {
                val rows = ArrayList<Pair<String, String>>()
                if (st.verifyPack) {
                    val x = s.x
                    rows.add(Pair(x.vPackChecked, r.checked.toString()))
                    rows.add(Pair(x.vPackSizeOk, r.sizeOk.toString()))
                    rows.add(Pair(x.vPackSizeBad, r.sizeBad.toString()))
                    rows.add(Pair(x.vPackMissing, r.unreadable.toString()))
                    rows.add(Pair(x.vPackContent, r.sumOk.toString() + " / " + r.sumChecked))
                    rows.forEach { row ->
                        Row {
                            Text(row.first + ": ", color = p.muted, fontSize = 14.sp)
                            Text(row.second, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                    Text(
                        if (r.problems.isEmpty()) x.vPackAllOk else x.vPackProblems(r.problems.size),
                        color = if (r.problems.isEmpty()) p.accent else Color(0xFFE57373),
                        fontWeight = FontWeight.Bold,
                    )
                    r.problems.take(20).forEach { line -> Text(line, color = p.muted, fontSize = 12.sp) }
                    if (r.problems.size > 20) Text("… +${r.problems.size - 20}", color = p.muted, fontSize = 12.sp)
                    Text(x.vPackNote, color = p.muted, fontSize = 12.sp, lineHeight = 17.sp)
                } else {
                rows.add(Pair(s.vChecked, r.checked.toString()))
                rows.add(Pair(s.vSizeOk, r.sizeOk.toString()))
                rows.add(Pair(s.vSizeBadLbl, r.sizeBad.toString()))
                rows.add(Pair(s.vUnreadableLbl, r.unreadable.toString()))
                rows.add(Pair(s.vTotal, formatBytes(r.actualBytes) + " / " + formatBytes(r.expectedBytes)))
                rows.add(Pair(s.vAdler, if (r.sumChecked > 0) s.vAdlerFmt(r.sumOk, r.sumChecked) else s.vAdlerNA))
                rows.forEach { row ->
                    // long rows (total size, checksum): the value moves to a new line so it is not cut off
                    if (row.first == s.vTotal || row.first == s.vAdler) {
                        Column {
                            Text(row.first + ":", color = p.muted, fontSize = 14.sp)
                            Text(row.second, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    } else Row {
                        Text(row.first + ": ", color = p.muted, fontSize = 14.sp)
                        Text(row.second, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
                val bad = r.sizeBad + r.unreadable + r.sumBad
                Text(
                    if (bad == 0) s.vAllOk else s.vProblems(bad),
                    color = if (bad == 0) p.accent else Color(0xFFE57373),
                    fontWeight = FontWeight.Bold,
                )
                if (r.sumBad > 0) Text(s.vXorHint, color = p.muted, fontSize = 12.sp)
                r.problems.take(20).forEach { line -> Text(line, color = p.muted, fontSize = 12.sp) }
                if (r.problems.size > 20) Text("… +${r.problems.size - 20}", color = p.muted, fontSize = 12.sp)
                }
            }
        }
    }
}

/** Bottom section content of the process screen: progress, summary, checks, then log. Used inside a LazyColumn. */
fun LazyListScope.processItems(
    s: UiState,
    onOpenFolder: () -> Unit,
    onCopyLog: () -> Unit,
    onVerify: (() -> Unit)?,
    summaryTitle: String? = null,
    okLabel: String? = null,
    onTranslate: (() -> Unit)? = null,
) {
    if (s.running || s.done) item(key = "progress") { ProgressBlock(s) }
    val sum = s.summary
    if (sum != null && s.done && !s.running) {
        item(key = "summary") {
            SummaryCard(sum, onOpenFolder, onCopyLog, if (s.verifying) null else onVerify, summaryTitle, okLabel, onTranslate)
        }
        if (s.verifying || s.verifyResult != null) item(key = "verify") { VerifyCard(s) }
    }
    items(s.log) { line -> Text(line, fontSize = 12.sp, color = LocalPalette.current.muted) }
}

/** General confirmation dialog. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    yes: String,
    no: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val p = LocalPalette.current
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(CardRadius),
        backgroundColor = p.card,
        contentColor = p.text,
        title = { Text(title, fontWeight = FontWeight.Bold, color = p.text) },
        text = { Text(message, color = p.muted) },
        confirmButton = {
            TextButton(onClick = onConfirm) { OneLineText(yes, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = p.accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { OneLineText(no, fontSize = 14.sp, color = p.text) }
        },
    )
}

/** Confirmation dialog while a process is running. */
@Composable
fun CancelDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val s = LocalStr.current
    ConfirmDialog(s.cancelTitle, s.cancelMessage, s.cancelYes, s.cancelNo, onConfirm, onDismiss)
}

/** Section title (above a toggle/choice), equivalent to the Quality/Volume label style. */
@Composable
fun SectionLabel(text: String) {
    Text(text, color = LocalPalette.current.text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

/** Text length (characters) from which a description is collapsed into "View details (?)". */
const val DETAIL_MIN_CHARS = 90

/**
 * "View details (?)" text (underlined, theme accent color, same size as the description).
 * Tap -> a window containing [text] without buttons; tap anywhere to close.
 */
@Composable
fun DetailLink(text: String, fontSize: androidx.compose.ui.unit.TextUnit = 13.sp, lineHeight: androidx.compose.ui.unit.TextUnit = 19.sp, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    val id = com.furuarc.ui.theme.LocalLang.current == com.furuarc.settings.Lang.ID
    var open by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Text(
        if (id) "Lihat detail (?)" else "See details (?)",
        color = p.accent, fontSize = fontSize, lineHeight = lineHeight,
        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
        modifier = modifier.clickable { open = true },
    )
    if (open) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { open = false }) {
            androidx.compose.material.Surface(
                Modifier.fillMaxWidth().clickable { open = false },
                shape = RoundedCornerShape(CardRadius), color = p.card,
            ) {
                Text(text, color = p.text, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

/** Long description that gets collapsed: [DetailLink]. */
@Composable
fun DetailHint(text: String) = DetailLink(text, 13.sp, 19.sp)


/** Dialog title with spacing to the content below it (built inside the title slot so the spacing is exact). */
@Composable
fun DlgTitle(text: String) {
    val p = LocalPalette.current
    Column {
        Text(text, fontWeight = FontWeight.Bold, color = p.text)
        Spacer(Modifier.height(14.dp))
    }
}


/** Wide dialog window (follows the screen width) with title, content, and button row; its size adapts to the content. */
@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun AppDialog(title: String, onDismiss: () -> Unit, buttons: @Composable RowScope.() -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val p = LocalPalette.current
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss, properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(CardRadius), color = p.card, contentColor = p.text,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 24.dp),
        ) {
            Column(Modifier.clearFocusOnOutsideTap().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = p.text)
                content()
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically, content = buttons)
            }
        }
    }
}

/** Page navigation: ‹  Page x / y  ›. The center text can be tapped to open the "Go to page" dialog. [page] is 0-based. */
@Composable
fun PageNav(page: Int, pages: Int, onPage: (Int) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val p = LocalPalette.current
    val lang = com.furuarc.ui.theme.LocalLang.current
    var dlg by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val word = when (lang) {
        com.furuarc.settings.Lang.ID -> "Halaman"
        com.furuarc.settings.Lang.JA -> "ページ"
        com.furuarc.settings.Lang.ZH -> "第"
        com.furuarc.settings.Lang.KO -> "페이지"
        else -> "Page"
    }
    val label = if (lang == com.furuarc.settings.Lang.ZH) "第 ${page + 1} / $pages 页" else "$word ${page + 1} / $pages"
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) { SecondaryButton("‹", page > 0) { onPage(page - 1) } }
        Box(
            Modifier.weight(2f).clip(RoundedCornerShape(8.dp)).clickable { text = (page + 1).toString(); dlg = true }.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) { Text(label, color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, textAlign = TextAlign.Center, maxLines = 1) }
        Box(Modifier.weight(1f)) { SecondaryButton("›", page < pages - 1) { onPage(page + 1) } }
    }
    if (dlg) {
        val title = when (lang) {
            com.furuarc.settings.Lang.ID -> "Pergi ke halaman"
            com.furuarc.settings.Lang.JA -> "ページへ移動"
            com.furuarc.settings.Lang.ZH -> "转到页面"
            com.furuarc.settings.Lang.KO -> "페이지로 이동"
            else -> "Go to page"
        }
        val go = when (lang) {
            com.furuarc.settings.Lang.ID -> "Pergi"
            com.furuarc.settings.Lang.JA -> "移動"
            com.furuarc.settings.Lang.ZH -> "转到"
            com.furuarc.settings.Lang.KO -> "이동"
            else -> "Go"
        }
        val cancel = when (lang) {
            com.furuarc.settings.Lang.ID -> "Batal"
            com.furuarc.settings.Lang.JA -> "キャンセル"
            com.furuarc.settings.Lang.ZH -> "取消"
            com.furuarc.settings.Lang.KO -> "취소"
            else -> "Cancel"
        }
        val target = text.toIntOrNull()
        val doGo = {
            val n = text.toIntOrNull()
            if (n != null) { onPage((n - 1).coerceIn(0, pages - 1)); dlg = false }
        }
        androidx.compose.ui.window.Dialog(onDismissRequest = { dlg = false }) {
            Surface(shape = RoundedCornerShape(CardRadius), color = p.card, contentColor = p.text) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = p.text)
                    AppTextField(text, { s -> text = s.filter { it.isDigit() }.take(6) }, "1 – $pages", number = true)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material.TextButton(onClick = { dlg = false }) { Text(cancel, color = p.text, fontSize = 14.sp) }
                        androidx.compose.material.TextButton(enabled = target != null, onClick = { doGo() }) {
                            Text(go, color = if (target != null) p.accent else p.muted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
