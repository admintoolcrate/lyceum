package cc.toolcrate.lyceum.ui.prompts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.Prompt
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PagePadding
import cc.toolcrate.lyceum.ui.editor.paperFieldColors
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

@Composable
fun PromptsScreen(onBack: () -> Unit, viewModel: PromptsViewModel = hiltViewModel()) {
    val prompts by viewModel.prompts.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    var editing by remember { mutableStateOf<Prompt?>(null) }
    var newText by remember { mutableStateOf("") }
    var confirmRestore by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        LyceumTopBar("Prompts", onBack = onBack)
        LazyColumn(Modifier.weight(1f)) {
            item {
                Text(
                    "One prompt is shown each day, taking turns in this order. Switch off any you'd rather not see.",
                    Modifier.padding(horizontal = PagePadding, vertical = 16.dp),
                    style = MaterialTheme.typography.bodyMedium, color = paper.muted,
                )
            }
            itemsIndexed(prompts, key = { _, p -> p.id }) { index, prompt ->
                Row(
                    Modifier.fillMaxWidth().padding(start = PagePadding, end = 8.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        prompt.text,
                        Modifier.weight(1f).clickable { editing = prompt }.padding(vertical = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (prompt.enabled) paper.ink else paper.muted,
                    )
                    Column {
                        IconButton(onClick = { viewModel.move(index, -1) }, enabled = index > 0) {
                            Icon(Icons.Filled.KeyboardArrowUp, "Move up")
                        }
                        IconButton(onClick = { viewModel.move(index, 1) }, enabled = index < prompts.lastIndex) {
                            Icon(Icons.Filled.KeyboardArrowDown, "Move down")
                        }
                    }
                    Switch(
                        checked = prompt.enabled,
                        onCheckedChange = { viewModel.setEnabled(prompt.id, it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = paper.accent, checkedThumbColor = paper.page),
                    )
                }
                Hairline(Modifier.padding(horizontal = PagePadding))
            }
            item {
                Column(Modifier.padding(PagePadding)) {
                    OutlinedTextField(
                        value = newText,
                        onValueChange = { newText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Write your own prompt", color = paper.muted) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { viewModel.add(newText); newText = "" }),
                        colors = paperFieldColors(),
                    )
                    Row {
                        TextButton(onClick = { viewModel.add(newText); newText = "" }, enabled = newText.isNotBlank()) {
                            Text("Add", color = paper.accent)
                        }
                        TextButton(onClick = { confirmRestore = true }) { Text("Restore the original prompts", color = paper.muted) }
                    }
                    Gap(24)
                }
            }
        }
    }

    editing?.let { prompt ->
        var text by remember(prompt.id) { mutableStateOf(prompt.text) }
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Edit prompt") },
            text = { OutlinedTextField(text, { text = it }, colors = paperFieldColors()) },
            confirmButton = {
                TextButton(onClick = { viewModel.edit(prompt.id, text); editing = null }, enabled = text.isNotBlank()) { Text("Save") }
            },
            dismissButton = {
                Row {
                    if (!prompt.builtIn) {
                        IconButton(onClick = { viewModel.delete(prompt.id); editing = null }) { Icon(Icons.Outlined.Delete, "Delete") }
                    }
                    TextButton(onClick = { editing = null }) { Text("Cancel") }
                }
            },
            containerColor = paper.card,
        )
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("Restore the original prompts?") },
            text = { Text("The prompts Lyceum came with return as they were. Your own prompts stay, after them.") },
            confirmButton = { TextButton(onClick = { viewModel.restoreDefaults(); confirmRestore = false }) { Text("Restore") } },
            dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text("Cancel") } },
            containerColor = paper.card,
        )
    }
}
