package cc.toolcrate.lyceum.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate

/** A page in a list: its date, mood word, a few lines and its tags. */
@Composable
fun EntryCard(
    entry: Entry,
    today: LocalDate,
    onClick: () -> Unit,
    preview: AnnotatedString = AnnotatedString(entry.body),
    dateLabel: String = entry.date.long(today),
) {
    val paper = LyceumTheme.paper
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(dateLabel, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, color = paper.ink)
            entry.mood?.let { Text(it.label, style = MaterialTheme.typography.labelMedium, color = paper.muted) }
        }
        if (preview.isNotBlank()) {
            Text(preview, style = MaterialTheme.typography.bodyMedium, color = paper.ink, maxLines = 3, overflow = TextOverflow.Ellipsis)
        }
        entry.learned?.let {
            Text("Learned: $it", style = MaterialTheme.typography.bodySmall, color = paper.muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (entry.tags.isNotEmpty()) {
            Text(entry.tags.joinToString("  ·  "), style = MaterialTheme.typography.labelSmall, color = paper.accent)
        }
    }
}

/** [text] with every word starting with one of [terms] in bold. */
fun highlight(text: String, terms: List<String>, color: androidx.compose.ui.graphics.Color): AnnotatedString {
    if (terms.isEmpty()) return AnnotatedString(text)
    val pattern = Regex("\\b(" + terms.joinToString("|") { Regex.escape(it) } + ")[\\p{L}\\p{N}]*", RegexOption.IGNORE_CASE)
    return buildAnnotatedString {
        var at = 0
        pattern.findAll(text).forEach { m ->
            append(text.substring(at, m.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = color)) { append(m.value) }
            at = m.range.last + 1
        }
        append(text.substring(at))
    }
}
