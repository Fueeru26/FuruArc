package com.furuarc.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.furuarc.core.Project
import com.furuarc.core.Projects
import com.furuarc.i18n.projectStr
import com.furuarc.ui.theme.LocalLang
import com.furuarc.ui.theme.LocalPalette

/** Create Project menu: one card per kind of project (for now only the Translation Project). */
@Composable
fun ProjectMenuScreen(onTranslation: () -> Unit, onBack: () -> Unit) {
    val j = projectStr(LocalLang.current)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(j.menuTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            MenuCard(AppIcons.Translate, j.trTitle, j.trSub, onTranslation)
        }
    }
}

/** The projects of the Translation Project type, newest first. */
@Composable
fun ProjectListScreen(onOpen: (String) -> Unit, onBack: () -> Unit) {
    val p = LocalPalette.current
    val j = projectStr(LocalLang.current)
    LaunchedEffect(Unit) { Projects.load() }
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(j.trTitle, onBack, fixed = true, trailing = {
            IconButton(onClick = { name = ""; creating = true }) { Icon(Icons.Default.Add, j.newBtn, tint = p.text) }
        })
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            if (Projects.list.isEmpty()) item { Hint(j.empty) }
            items(Projects.list.size) { i ->
                val pr = Projects.list[i]
                MenuCard(AppIcons.Translate, pr.name, j.progress(pr.doneCount(), Projects.STEPS.size)) { onOpen(pr.id) }
            }
        }
    }
    if (creating) {
        AppDialog(j.newTitle, { creating = false }, buttons = {
            DlgBtn(j.cancel, false) { creating = false }
            DlgBtn(j.create, true, enabled = name.isNotBlank()) { val pr = Projects.create(name); creating = false; onOpen(pr.id) }
        }) {
            AppTextField(name, { name = it }, j.nameLabel)
        }
    }
}

/** One project: the steps of the workflow with their state, and a note. */
@Composable
fun ProjectScreen(id: String, onStep: (Project, Int) -> Unit, onBack: () -> Unit) {
    val p = LocalPalette.current
    val j = projectStr(LocalLang.current)
    val pr = Projects.get(id)
    if (pr == null) { LaunchedEffect(Unit) { onBack() }; return }
    var renaming by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(pr.name, onBack, fixed = true, trailing = {
            IconButton(onClick = { newName = pr.name; renaming = true }) { Icon(AppIcons.Edit, j.rename, tint = p.text) }
            IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Trash, j.delete, tint = p.text) }
        })
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(j.progress(pr.doneCount(), Projects.STEPS.size), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    DetailLink(j.intro, 13.sp, 19.sp)
                }
            }
            item {
                LabelSection(j.stepsTitle) {
                    Projects.STEPS.forEachIndexed { i, step ->
                        val st = pr.state(step)
                        if (i > 0) Divider(color = p.muted.copy(alpha = 0.25f))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${i + 1}. " + j.stepTitle(step), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                Text(
                                    when (st) { Projects.ST_DONE -> j.stDone; Projects.ST_OPEN -> j.stOpen; else -> j.stNone },
                                    color = if (st == Projects.ST_DONE) p.accent else p.muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                                )
                            }
                            Text(j.stepDesc(step), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.weight(1f)) { SecondaryButton(j.open) { onStep(pr, step) } }
                                Row(
                                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).clickable { Projects.setState(pr, step, if (st == Projects.ST_DONE) Projects.ST_OPEN else Projects.ST_DONE) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(checked = st == Projects.ST_DONE, onCheckedChange = { Projects.setState(pr, step, if (it) Projects.ST_DONE else Projects.ST_OPEN) }, colors = CheckboxDefaults.colors(checkedColor = p.accent))
                                    Text(j.done, color = p.text, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
            item {
                LabelSection(j.noteTitle) {
                    AppTextField(pr.note, { pr.note = it; Projects.save() }, j.noteLabel, multiline = true)
                }
            }
        }
    }
    if (renaming) {
        AppDialog(j.rename, { renaming = false }, buttons = {
            DlgBtn(j.cancel, false) { renaming = false }
            DlgBtn(j.create, true, enabled = newName.isNotBlank()) { pr.name = newName.trim(); Projects.save(); renaming = false }
        }) { AppTextField(newName, { newName = it }, j.nameLabel) }
    }
    if (askDelete) {
        ConfirmDialog(j.delete, j.deleteAsk, j.yes, j.cancel, onConfirm = { askDelete = false; Projects.delete(pr.id); onBack() }, onDismiss = { askDelete = false })
    }
}
