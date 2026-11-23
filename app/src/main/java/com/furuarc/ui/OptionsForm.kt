package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.OptKind
import com.furuarc.core.OptionSpec
import com.furuarc.settings.Lang
import com.furuarc.ui.theme.LocalPalette

private fun OptionSpec.label(lang: Lang) = if (lang == Lang.ID) id else en
private fun OptionSpec.hint(lang: Lang) = if (lang == Lang.ID) hintId else hintEn

/** Initial option values: last saved entries, or the spec defaults. */
fun optionDefaults(specs: List<OptionSpec>, load: (OptionSpec) -> String?): Map<String, String> =
    specs.associate { sp -> sp.key to (load(sp) ?: sp.default) }

/**
 * Add one list item per currently visible option (dropdown / text field / number / checkbox).
 * [values] must be a state (e.g. mutableStateMapOf) so conditional options ([OptionSpec.showIf]) change too.
 */
fun LazyListScope.optionItems(
    specs: List<OptionSpec>,
    values: Map<String, String>,
    lang: Lang,
    enabled: Boolean,
    keyPrefix: String = "opt_",
    onChange: (String, String) -> Unit,
) {
    for (sp in specs) {
        val cond = sp.showIf
        if (cond != null && (values[cond.first] ?: specs.firstOrNull { it.key == cond.first }?.default) != cond.second) continue
        item(key = keyPrefix + sp.key) {
            OptionRow(sp, values[sp.key] ?: sp.default, lang, enabled) { v -> onChange(sp.key, v) }
        }
    }
}

@Composable
private fun OptionRow(sp: OptionSpec, value: String, lang: Lang, enabled: Boolean, onChange: (String) -> Unit) {
    val p = LocalPalette.current
    when (sp.kind) {
        OptKind.CHOICE -> {
            var open by remember { mutableStateOf(false) }
            val labels = sp.choices.map { if (lang == Lang.ID) it.id else it.en }
            val cur = sp.choices.indexOfFirst { it.value == value }.let { if (it < 0) 0 else it }
            val anchorW = remember { mutableStateOf(0.dp) }
            Column {
                Box(Modifier.fillMaxWidth().trackWidth(anchorW)) {
                    AppCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.clickable(enabled = enabled) { open = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(sp.label(lang), fontSize = 12.sp, lineHeight = 16.sp, color = p.muted)
                                Text(
                                    labels.getOrElse(cur) { value },
                                    fontSize = 16.sp,
                                    lineHeight = 24.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = p.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = p.muted)
                        }
                    }
                    FieldDropdownMenu(expanded = open, anchorWidth = anchorW.value, onDismiss = { open = false }) {
                        sp.choices.forEachIndexed { i, ch ->
                            DropdownOption(labels[i], i == cur) { open = false; onChange(ch.value) }
                        }
                    }
                }
                HintText(sp.hint(lang))
            }
        }
        OptKind.FILE -> {
            val ctx = LocalContext.current
            val scope = rememberCoroutineScope()
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) scope.launch {
                    val f = withContext(Dispatchers.IO) {
                        try {
                            var nm = "file"
                            ctx.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                                if (c.moveToFirst()) c.getString(0)?.let { nm = it }
                            }
                            val dst = java.io.File(ctx.cacheDir, "opt_" + sp.key + "_" + nm.replace(Regex("[^A-Za-z0-9._-]"), "_"))
                            ctx.contentResolver.openInputStream(uri)?.use { i -> dst.outputStream().use { o -> i.copyTo(o) } }
                            dst.takeIf { it.isFile && it.length() <= 64L * 1024 * 1024 }
                        } catch (e: Exception) { null }
                    }
                    if (f != null) onChange(f.absolutePath)
                }
            }
            val shown = if (value.isEmpty()) (if (lang == Lang.ID) "Belum dipilih" else "Not selected")
                else java.io.File(value).name.removePrefix("opt_" + sp.key + "_")
            Column {
                AppCard(Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.clickable(enabled = enabled) { launcher.launch(arrayOf("*/*")) }.padding(16.dp),
                    ) {
                        Text(sp.label(lang), fontSize = 12.sp, lineHeight = 16.sp, color = p.muted)
                        Text(
                            shown, fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium,
                            color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                HintText(sp.hint(lang))
            }
        }
        OptKind.BOOL -> {
            val on = value == "1"
            Row(
                Modifier.clickable(enabled = enabled) { onChange(if (on) "0" else "1") },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = on,
                    onCheckedChange = { v -> onChange(if (v) "1" else "0") },
                    enabled = enabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = p.accent,
                        uncheckedColor = p.muted,
                        checkmarkColor = p.onAccent,
                    ),
                )
                Text(sp.label(lang), color = p.text)
            }
        }
        OptKind.TEXT, OptKind.NUMBER -> {
            Column {
                AppTextField(
                    value = value,
                    onValueChange = { t ->
                        onChange(if (sp.kind == OptKind.NUMBER) t.filter { ch -> ch.isDigit() }.take(3) else t.take(64))
                    },
                    label = sp.label(lang),
                    number = sp.kind == OptKind.NUMBER,
                )
                HintText(sp.hint(lang))
            }
        }
    }
}

@Composable
private fun HintText(t: String) {
    if (t.isEmpty()) return
    val p = LocalPalette.current
    Text(t, fontSize = 12.sp, color = p.muted, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
}
