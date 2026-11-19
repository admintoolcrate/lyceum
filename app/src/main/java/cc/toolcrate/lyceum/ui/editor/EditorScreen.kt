package cc.toolcrate.lyceum.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.components.PaperChip
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.components.long
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.QuoteStyle

/**
 * Writes or edits one day's page. On the Today tab [onBack] is null and Settings sits in the top bar;
 * opened for a past day it has a back arrow and returns once the quote is dismissed.
 */
@Composable
fun EditorScreen(
    onBack: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    goalSlot: @Composable (goal: GoalRef?, onPick: (GoalRef?) -> Unit) -> Unit = { goal, onPick -> GoalChip(goal, onPick) },
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val known by viewModel.knownTags.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    var confirmLeave by remember { mutableStateOf(false) }
    // Leaving a past page with changes not yet saved asks first; Today keeps its draft while you look around.
    val back: (() -> Unit)? = onBack?.let { leave -> { if (state.dirty) confirmLeave = true else leave() } }
    BackHandler(enabled = onBack != null && state.dirty) { confirmLeave = true }

    Column(Modifier.fillMaxSize()) {
        LyceumTopBar(
            title = if (onBack == null) "Today" else "Edit page",
            onBack = back,
            actions = {
                if (onBack == null) IconButton(onClick = onOpenSettings) { Icon(Icons.Outlined.Settings, "Settings") }
            },
        )
        PageColumn(Modifier.imePadding()) {
            Text(state.date.long(state.today), style = MaterialTheme.typography.headlineMedium, color = paper.ink)
            Gap(12)
            Text(state.prompt, style = QuoteStyle, color = paper.accent)
            Gap(16)
            TextField(
                value = state.body,
                onValueChange = viewModel::setBody,
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                placeholder = { Text("A few lines are plenty.", color = paper.muted) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = paper.ink),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = paper.card, unfocusedContainerColor = paper.card,
                    focusedIndicatorColor = paper.accent, unfocusedIndicatorColor = paper.hairline,
                    cursorColor = paper.accent,
                ),
            )
            Gap(28)
            SectionLabel("Mood · optional")
            Gap(8)
            MoodRow(state.mood, viewModel::toggleMood)
            Gap(24)
            SectionLabel("One thing I learned · optional")
            Gap(8)
            OutlinedTextField(
                value = state.learned,
                onValueChange = viewModel::setLearned,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("In a line", color = paper.muted) },
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                colors = paperFieldColors(),
            )
            Gap(24)
            SectionLabel("Tags · optional")
            Gap(8)
            TagEditor(
                tags = state.tags,
                input = state.tagInput,
                suggestions = known.filter { k -> state.tags.none { it.equals(k, ignoreCase = true) } }.take(8),
                onInput = viewModel::setTagInput,
                onAdd = { viewModel.addTag(it) },
                onRemove = viewModel::removeTag,
            )
            Gap(20)
            goalSlot(state.goal, viewModel::setGoal)
            Gap(28)
            Hairline()
            Gap(16)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = viewModel::save,
                    enabled = state.canSave && (state.dirty || !state.existing),
                    colors = ButtonDefaults.buttonColors(containerColor = paper.ink, contentColor = paper.page),
                ) { Text(if (state.existing) "Save changes" else "Save") }
                if (state.existing && !state.dirty) {
                    Text("Saved.", Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyMedium, color = paper.muted)
                }
            }
            Gap(40)
        }
    }

    if (confirmLeave && onBack != null) {
        AlertDialog(
            onDismissRequest = { confirmLeave = false },
            title = { Text("Leave without saving?") },
            text = { Text("Your changes to this page haven't been saved.") },
            confirmButton = { TextButton(onClick = { confirmLeave = false; onBack() }) { Text("Leave") } },
            dismissButton = { TextButton(onClick = { confirmLeave = false }) { Text("Keep writing") } },
            containerColor = paper.card,
        )
    }

    state.quote?.let { quote ->
        QuoteSheet(quote) {
            viewModel.dismissQuote()
            onBack?.invoke()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MoodRow(selected: Mood?, onToggle: (Mood) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Mood.entries.forEach { mood -> PaperChip(mood.label, selected == mood, onClick = { onToggle(mood) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditor(
    tags: List<String>,
    input: String,
    suggestions: List<String>,
    onInput: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit,
) {
    val paper = LyceumTheme.paper
    if (tags.isNotEmpty()) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tags.forEach { tag ->
                InputChip(
                    selected = true,
                    onClick = { onRemove(tag) },
                    label = { Text(tag) },
                    trailingIcon = { Icon(Icons.Filled.Close, "Remove $tag", Modifier.size(16.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = paper.accentSoft, selectedLabelColor = paper.ink,
                        selectedTrailingIconColor = paper.muted,
                    ),
                    border = null,
                )
            }
        }
        Gap(8)
    }
    OutlinedTextField(
        value = input,
        onValueChange = onInput,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Add a tag", color = paper.muted) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onAdd(input) }),
        colors = paperFieldColors(),
    )
    if (suggestions.isNotEmpty()) {
        Gap(8)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { PaperChip(it, selected = false, onClick = { onAdd(it) }) }
        }
    }
}

/** Shows a linked Stagira goal with a way to remove it. The picker is added where Stagira is installed. */
@Composable
fun GoalChip(goal: GoalRef?, onPick: (GoalRef?) -> Unit) {
    if (goal == null) return
    val paper = LyceumTheme.paper
    Column {
        SectionLabel("Stagira goal")
        Gap(8)
        InputChip(
            selected = true,
            onClick = { onPick(null) },
            label = { Text(goal.title.ifBlank { "A goal" }) },
            trailingIcon = { Icon(Icons.Filled.Close, "Unlink goal", Modifier.size(16.dp)) },
            colors = InputChipDefaults.inputChipColors(selectedContainerColor = paper.card, selectedLabelColor = paper.ink),
        )
    }
}

@Composable
fun paperFieldColors() = LyceumTheme.paper.let { paper ->
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor = paper.accent,
        unfocusedBorderColor = paper.hairline,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        cursorColor = paper.accent,
        focusedTextColor = paper.ink,
        unfocusedTextColor = paper.ink,
    )
}
