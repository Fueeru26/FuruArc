package com.furuarc.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.documentfile.provider.DocumentFile
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
fun ProjectMenuScreen(onTranslation: () -> Unit, onCompress: () -> Unit, onExtract: () -> Unit, onPatch: () -> Unit, onAudit: () -> Unit, onClean: () -> Unit, onLocalize: () -> Unit, onBack: () -> Unit) {
    val j = projectStr(LocalLang.current)
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(j.menuTitle, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            MenuCard(AppIcons.Translate, j.trTitle, j.trSub, onTranslation)
            MenuCard(AppIcons.Folder, j.cmpTitle, j.cmpSub, onCompress)
            MenuCard(AppIcons.Image, j.extTitle, j.extSub, onExtract)
            MenuCard(AppIcons.Edit, j.patTitle, j.patSub, onPatch)
            MenuCard(AppIcons.Checklist, j.audTitle, j.audSub, onAudit)
            MenuCard(AppIcons.Trash, j.clnTitle, j.clnSub, onClean)
            MenuCard(AppIcons.Brush, j.locTitle, j.locSub, onLocalize)
        }
    }
}

/** The projects of one type (translation or compress game), newest first. */
@Composable
fun ProjectListScreen(type: Int, onOpen: (String) -> Unit, onBack: () -> Unit) {
    val p = LocalPalette.current
    val j = projectStr(LocalLang.current)
    LaunchedEffect(Unit) { Projects.load() }
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var newMech by remember { mutableStateOf(Projects.MECH_VN) }
    val shown = Projects.list.filter { it.type == type }
    val typeIcon = when (type) { Projects.TYPE_COMPRESS -> AppIcons.Folder; Projects.TYPE_EXTRACT -> AppIcons.Image; Projects.TYPE_PATCH -> AppIcons.Edit; Projects.TYPE_AUDIT -> AppIcons.Checklist; Projects.TYPE_CLEAN -> AppIcons.Trash; Projects.TYPE_LOCALIZE -> AppIcons.Brush; else -> AppIcons.Translate }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(j.typeTitle(type), onBack, fixed = true, trailing = {
            IconButton(onClick = { name = ""; newMech = Projects.MECH_VN; creating = true }) { Icon(Icons.Default.Add, j.newBtn, tint = p.text) }
        })
        if (shown.isEmpty()) {
            Column(Modifier.weight(1f).fillMaxWidth().padding(bottom = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                EmptyArt(typeIcon, 132.dp)
                Spacer(Modifier.height(16.dp))
                Text(j.emptyTitle, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = p.text)
                Spacer(Modifier.height(6.dp))
                Text(j.emptySub, fontSize = 14.sp, color = p.muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Box(Modifier.width(200.dp)) { PrimaryButton(j.createBtn, true) { name = ""; newMech = Projects.MECH_VN; creating = true } }
            }
        } else LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            items(shown.size) { i ->
                val pr = shown[i]
                MenuCard(typeIcon, pr.name, j.progress(pr.doneCount(), pr.steps.size)) { onOpen(pr.id) }
            }
        }
    }
    if (creating) {
        AppDialog(j.typeNew(type), { creating = false }, buttons = {
            DlgBtn(j.cancel, false) { creating = false }
            DlgBtn(j.create, true, enabled = name.isNotBlank()) { val pr = Projects.create(name, type, newMech); creating = false; onOpen(pr.id) }
        }) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(name, { name = it }, j.nameLabel)
                Text(j.mechTitle, color = p.muted, fontSize = 13.sp)
                SegmentedChips(listOf(j.mechMv, j.mechXp, j.mechVn), newMech, 12.sp) { newMech = it }
            }
        }
    }
}

/** One project: the steps of the workflow with their state, and a note. */
@Composable
fun ProjectScreen(id: String, onStep: (Project, Int) -> Unit, onDuplicate: (String) -> Unit = {}, onBack: () -> Unit) {
    val p = LocalPalette.current
    val j = projectStr(LocalLang.current)
    val pr = Projects.get(id)
    if (pr == null) { LaunchedEffect(Unit) { onBack() }; return }
    var renaming by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var askDelete by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val pickSource = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            pr.sourceUri = u.toString()
            pr.sourceName = try { DocumentFile.fromSingleUri(ctx, u)?.name ?: "" } catch (e: Exception) { "" }
            Projects.save()
        }
    }
    val pickWork = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION) } catch (e: Exception) { }
            pr.workUri = u.toString()
            pr.workName = try { DocumentFile.fromTreeUri(ctx, u)?.name ?: "" } catch (e: Exception) { "" }
            Projects.save()
        }
    }
    val pickSourceTree = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { u ->
        if (u != null) {
            try { ctx.contentResolver.takePersistableUriPermission(u, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (e: Exception) { }
            pr.sourceUri = u.toString()
            pr.sourceName = try { DocumentFile.fromTreeUri(ctx, u)?.name ?: "" } catch (e: Exception) { "" }
            Projects.save()
        }
    }
    val mv = pr.mech == Projects.MECH_MV
    // the first step that is not done is the one to continue with
    val nextStep = pr.steps.firstOrNull { pr.state(it) != Projects.ST_DONE }
    Column(Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp)) {
        ScreenHeader(pr.name, onBack, fixed = true, trailing = {
            IconButton(onClick = { val c = Projects.duplicate(pr, j.copySuffix); onDuplicate(c.id) }) { Icon(AppIcons.ContentCopy, j.duplicate, tint = p.text) }
            IconButton(onClick = { newName = pr.name; renaming = true }) { Icon(AppIcons.Edit, j.rename, tint = p.text) }
            IconButton(onClick = { askDelete = true }) { Icon(AppIcons.Trash, j.delete, tint = p.text) }
        })
        LazyColumn(state = rememberLazyListState(), modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ScreenGap)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(j.progress(pr.doneCount(), pr.steps.size), color = p.text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    DetailLink(j.typeIntro(pr.type), 13.sp, 19.sp)
                }
            }
            item {
                LabelSection(j.mechTitle) {
                    SegmentedChips(listOf(j.mechMv, j.mechXp, j.mechVn), pr.mech, 12.sp) { m ->
                        if (m != pr.mech) {
                            // a game folder (MV / MZ) and a source archive are different kinds of source
                            if ((m == Projects.MECH_MV) != (pr.mech == Projects.MECH_MV)) { pr.sourceUri = ""; pr.sourceName = "" }
                            pr.mech = m
                            Projects.save()
                        }
                    }
                    DetailLink(j.mechNote, 12.sp, 17.sp)
                }
            }
            if (mv) item { PickerCard(AppIcons.Folder, j.mvSourceLabel, pr.sourceName.ifEmpty { null }, AppIcons.Folder) { pickSourceTree.launch(null) } }
            else item { PickerCard(AppIcons.File, j.sourceLabel, pr.sourceName.ifEmpty { null }, AppIcons.Folder) { pickSource.launch(arrayOf("*/*")) } }
            item { PickerCard(AppIcons.Folder, if (mv) j.mvWorkLabel else j.workLabel, pr.workName.ifEmpty { null }, AppIcons.Folder) { pickWork.launch(null) } }
            item { DetailLink(if (mv) j.mvPickNote else j.pickNote, 13.sp, 19.sp) }
            item {
                LabelSection(j.stepsTitle) {
                    pr.steps.forEachIndexed { i, step ->
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
                            Text(j.stepDesc(step, pr.type, pr.mech), color = p.muted, fontSize = 13.sp, lineHeight = 18.sp)
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(Modifier.weight(1f)) { if (step == nextStep) PrimaryButton(j.next, true) { onStep(pr, step) } else SecondaryButton(j.open) { onStep(pr, step) } }
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
            item { ExportBar(pr.name) { j.reportText(pr) } }
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
