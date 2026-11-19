package cc.toolcrate.lyceum.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Horizontal page padding used everywhere. */
val PagePadding = 24.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyceumTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val paper = LyceumTheme.paper
    Column {
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = paper.page,
                titleContentColor = paper.ink,
                navigationIconContentColor = paper.ink,
                actionIconContentColor = paper.muted,
            ),
        )
        Hairline()
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) = HorizontalDivider(modifier, thickness = 0.5.dp, color = LyceumTheme.paper.hairline)

/** A small upper-case label above a group. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(Locale.getDefault()),
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        color = LyceumTheme.paper.muted,
    )
}

/** A scrolling page column with the standard margins. */
@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PagePadding, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        content = content,
    )
}

@Composable
fun Gap(height: Int) = Spacer(Modifier.height(height.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaperChip(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val paper = LyceumTheme.paper
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = paper.page,
            labelColor = paper.muted,
            selectedContainerColor = paper.accentSoft,
            selectedLabelColor = paper.ink,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true, selected = selected,
            borderColor = paper.hairline, selectedBorderColor = paper.accent.copy(alpha = 0.5f),
        ),
    )
}

@Composable
fun EmptyNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier.fillMaxWidth().padding(vertical = 32.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = LyceumTheme.paper.muted,
    )
}

/** "Tuesday, 10 March" style. */
val LongDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
val LongDateYear: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")
val ShortDate: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM")

fun LocalDate.long(today: LocalDate): String =
    format(if (year == today.year) LongDate else LongDateYear)
