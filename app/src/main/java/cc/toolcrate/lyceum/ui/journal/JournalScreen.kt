package cc.toolcrate.lyceum.ui.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.MonthGrid
import cc.toolcrate.lyceum.ui.components.EmptyNote
import cc.toolcrate.lyceum.ui.components.EntryCard
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val MonthTitle = DateTimeFormatter.ofPattern("MMMM yyyy")

/**
 * A month of pages. Written days carry a small dot; there are no counts. Tapping a written day opens
 * it; tapping an earlier unwritten day lets you write it now.
 */
@Composable
fun JournalScreen(
    onOpen: (LocalDate) -> Unit,
    onWrite: (LocalDate) -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    val written = state.written

    Column(Modifier.fillMaxSize()) {
        LyceumTopBar("Journal", actions = { IconButton(onClick = onOpenSettings) { Icon(Icons.Outlined.Settings, "Settings") } })
        PageColumn {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    state.month.format(MonthTitle), Modifier.weight(1f).clickable { viewModel.thisMonth() },
                    style = MaterialTheme.typography.headlineSmall, color = paper.ink,
                )
                IconButton(onClick = { viewModel.shift(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month") }
                IconButton(onClick = { viewModel.shift(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month") }
            }
            Gap(12)
            Row(Modifier.fillMaxWidth()) {
                MonthGrid.weekdays().forEach {
                    Text(
                        it.getDisplayName(TextStyle.NARROW, Locale.getDefault()), Modifier.weight(1f),
                        textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, color = paper.muted,
                    )
                }
            }
            Gap(4)
            MonthGrid.cells(state.month).chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                            if (day != null) {
                                DayCell(
                                    day = day,
                                    written = day in written,
                                    isToday = day == state.today,
                                    future = day.isAfter(state.today),
                                    onClick = { if (day in written) onOpen(day) else onWrite(day) },
                                )
                            }
                        }
                    }
                }
            }
            Gap(20)
            Hairline()
            if (state.entries.isEmpty()) {
                EmptyNote("No pages this month.")
            } else {
                state.entries.asReversed().forEach { entry ->
                    EntryCard(entry, state.today, onClick = { onOpen(entry.date) })
                    Hairline()
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: LocalDate, written: Boolean, isToday: Boolean, future: Boolean, onClick: () -> Unit) {
    val paper = LyceumTheme.paper
    Column(
        Modifier
            .padding(3.dp)
            .fillMaxSize()
            .let { if (isToday) it.border(1.dp, paper.accent, CircleShape) else it }
            .let { if (future) it else it.clickable(onClick = onClick) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (future) paper.hairline else paper.ink,
        )
        Box(Modifier.size(4.dp).background(if (written) paper.ink else paper.page, CircleShape))
    }
}
