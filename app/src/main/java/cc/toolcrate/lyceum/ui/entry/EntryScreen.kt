package cc.toolcrate.lyceum.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.ui.components.EmptyNote
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.components.long
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.QuoteStyle
import java.time.LocalDate

/** One past page, to read. Edit and delete sit in the top bar; a deletion can be undone here. */
@Composable
fun EntryScreen(
    onBack: () -> Unit,
    onEdit: (LocalDate) -> Unit,
    onOpenGoal: ((GoalRef) -> Unit)? = null,
    viewModel: EntryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    var confirmDelete by remember { mutableStateOf(false) }
    val entry = state.entry

    Column(Modifier.fillMaxSize()) {
        LyceumTopBar(
            title = "",
            onBack = onBack,
            actions = {
                if (entry != null) {
                    IconButton(onClick = { onEdit(state.date) }) { Icon(Icons.Outlined.Edit, "Edit") }
                    IconButton(onClick = { confirmDelete = true }) { Icon(Icons.Outlined.Delete, "Delete") }
                }
            },
        )
        PageColumn {
            Text(state.date.long(state.today), style = MaterialTheme.typography.headlineMedium, color = paper.ink)
            Gap(12)
            when {
                entry != null -> EntryBody(entry, onOpenGoal)
                state.deleted != null -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("This page was deleted.", Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = paper.muted)
                        TextButton(onClick = viewModel::undoDelete) { Text("Undo", color = paper.accent) }
                    }
                }
                state.loaded -> EmptyNote("Nothing written on this day.")
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this page?") },
            text = { Text("You can undo this until you leave the page.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(); confirmDelete = false }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            containerColor = paper.card,
        )
    }
}

@Composable
private fun EntryBody(entry: Entry, onOpenGoal: ((GoalRef) -> Unit)?) {
    val paper = LyceumTheme.paper
    Text(entry.prompt, style = QuoteStyle, color = paper.accent)
    Gap(16)
    Text(entry.body, style = MaterialTheme.typography.bodyLarge, color = paper.ink)
    Gap(24)
    Hairline()
    entry.mood?.let { Detail("Mood", it.label) }
    entry.learned?.let { Detail("One thing I learned", it) }
    if (entry.tags.isNotEmpty()) Detail("Tags", entry.tags.joinToString("  ·  "))
    entry.goal?.let { goal ->
        Gap(16)
        SectionLabel("Stagira goal")
        if (onOpenGoal != null) {
            TextButton(onClick = { onOpenGoal(goal) }) { Text(goal.title.ifBlank { "Open goal" }, color = paper.accent) }
        } else {
            Text(goal.title, style = MaterialTheme.typography.bodyLarge, color = paper.ink)
        }
    }
}

@Composable
private fun Detail(label: String, value: String) {
    Gap(16)
    SectionLabel(label)
    Gap(4)
    Text(value, style = MaterialTheme.typography.bodyLarge, color = LyceumTheme.paper.ink)
}
