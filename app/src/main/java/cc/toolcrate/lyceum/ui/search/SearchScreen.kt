package cc.toolcrate.lyceum.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.Snippet
import cc.toolcrate.lyceum.ui.components.EmptyNote
import cc.toolcrate.lyceum.ui.components.EntryCard
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PagePadding
import cc.toolcrate.lyceum.ui.components.PaperChip
import cc.toolcrate.lyceum.ui.components.highlight
import cc.toolcrate.lyceum.ui.editor.paperFieldColors
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(onOpen: (LocalDate) -> Unit, viewModel: SearchViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val tag by viewModel.tag.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    val terms = Snippet.terms(results.query)

    Column(Modifier.fillMaxSize().imePadding()) {
        LyceumTopBar("Search")
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = PagePadding)) {
            item {
                Gap(16)
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    placeholder = { Text("Words from your pages", color = paper.muted) },
                    colors = paperFieldColors(),
                )
                if (tags.isNotEmpty()) {
                    Gap(12)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tags.forEach { PaperChip(it, tag == it, onClick = { viewModel.toggleTag(it) }) }
                    }
                }
                Gap(12)
                Hairline()
            }
            if (results.searched && results.entries.isEmpty()) {
                item { EmptyNote("No pages match.") }
            }
            items(results.entries, key = { it.date.toEpochDay() }) { entry ->
                EntryCard(
                    entry, viewModel.today, onClick = { onOpen(entry.date) },
                    preview = highlight(Snippet.excerpt(entry.body, results.query), terms, paper.accent),
                )
                Hairline()
            }
            item { Gap(24) }
        }
    }
}
