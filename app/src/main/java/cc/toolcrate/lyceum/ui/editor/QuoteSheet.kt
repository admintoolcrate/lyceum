package cc.toolcrate.lyceum.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cc.toolcrate.lyceum.quotes.Quote
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.PagePadding
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.QuoteStyle

/** The passage shown after saving: nothing to do but read it and close. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteSheet(quote: Quote, onDismiss: () -> Unit) {
    val paper = LyceumTheme.paper
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = paper.card,
        contentColor = paper.ink,
        tonalElevation = 0.dp,
    ) {
        QuoteContent(quote, onDismiss)
    }
}

/** The passage itself, apart from the sheet that holds it (so it can be drawn on its own). */
@Composable
fun QuoteContent(quote: Quote, onDismiss: () -> Unit) {
    val paper = LyceumTheme.paper
    Column(
        Modifier.fillMaxWidth().padding(horizontal = PagePadding).padding(bottom = 16.dp).navigationBarsPadding(),
    ) {
        Text("Saved.", style = MaterialTheme.typography.labelMedium, color = paper.muted)
        Gap(16)
        Text("“${quote.text}”", style = QuoteStyle, color = paper.ink)
        Gap(12)
        Text(quote.citation, style = MaterialTheme.typography.bodySmall, color = paper.muted)
        Text("tr. H. Rackham", style = MaterialTheme.typography.bodySmall, color = paper.muted)
        Gap(16)
        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text("Close", color = paper.accent)
        }
    }
}
