package cc.toolcrate.lyceum.ui.week

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.WeekRange
import cc.toolcrate.lyceum.ui.components.EmptyNote
import cc.toolcrate.lyceum.ui.components.EntryCard
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DayName = DateTimeFormatter.ofPattern("EEEE")
private val DayMonth = DateTimeFormatter.ofPattern("d MMMM")

private fun WeekRange.title(): String =
    if (start.month == end.month) "${start.dayOfMonth} – ${end.format(DayMonth)}"
    else "${start.format(DayMonth)} – ${end.format(DayMonth)}"

/** The week's pages read together: what was written, what was learned, the words for how it felt. */
@Composable
fun WeekScreen(onOpen: (LocalDate) -> Unit, viewModel: WeekViewModel = hiltViewModel()) {
    val review by viewModel.review.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper

    Column(Modifier.fillMaxSize()) {
        LyceumTopBar("Week")
        PageColumn {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    if (viewModel.isThisWeek(review.week)) {
                        Text("This week", style = MaterialTheme.typography.labelMedium, color = paper.muted)
                    }
                    Text(review.week.title(), style = MaterialTheme.typography.headlineSmall, color = paper.ink)
                }
                IconButton(onClick = { viewModel.shift(-1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous week") }
                IconButton(onClick = { viewModel.shift(1) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next week") }
            }
            Gap(16)
            if (review.entries.isEmpty()) {
                Hairline()
                EmptyNote("No pages this week. Whenever you like, there's room for a few lines.")
                return@PageColumn
            }
            if (review.learned.isNotEmpty()) {
                SectionLabel("What you learned")
                Gap(8)
                review.learned.forEach { (day, line) ->
                    Text("${day.format(DayName)}  ·  $line", style = MaterialTheme.typography.bodyLarge, color = paper.ink)
                    Gap(4)
                }
                Gap(16)
            }
            if (review.moods.isNotEmpty()) {
                SectionLabel("How the days felt")
                Gap(8)
                Text(
                    review.moods.joinToString("  ·  ") { (day, mood) -> "${day.format(DateTimeFormatter.ofPattern("EEE"))} ${mood.label.lowercase()}" },
                    style = MaterialTheme.typography.bodyMedium, color = paper.ink,
                )
                Gap(16)
            }
            if (review.tags.isNotEmpty()) {
                SectionLabel("Threads")
                Gap(8)
                Text(review.tags.joinToString("  ·  "), style = MaterialTheme.typography.bodyMedium, color = paper.accent)
                Gap(16)
            }
            SectionLabel("Pages")
            Hairline()
            review.entries.forEach { entry ->
                EntryCard(entry, viewModel.today, onClick = { onOpen(entry.date) }, dateLabel = entry.date.format(DayName))
                Hairline()
            }
            Gap(24)
        }
    }
}
