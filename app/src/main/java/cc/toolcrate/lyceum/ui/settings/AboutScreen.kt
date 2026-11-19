package cc.toolcrate.lyceum.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cc.toolcrate.lyceum.BuildConfig
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val paper = LyceumTheme.paper
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        LyceumTopBar("About", onBack = onBack)
        PageColumn {
            Text("Lyceum", style = MaterialTheme.typography.headlineLarge, color = paper.ink)
            Text("Version ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodySmall, color = paper.muted)
            Gap(16)
            Text(
                "A quiet place for a few lines each day. Free, with no ads, no accounts and no analytics. " +
                    "Your pages stay on this phone unless you export them or choose an encrypted backup to your own Google Drive.",
                style = MaterialTheme.typography.bodyLarge, color = paper.ink,
            )
            Gap(24)
            SectionLabel("Quotations")
            Gap(8)
            Text(
                "Passages from Aristotle's Nicomachean Ethics in H. Rackham's translation (1926), " +
                    "as published by the Perseus Digital Library under CC BY-SA 4.0.",
                style = MaterialTheme.typography.bodyMedium, color = paper.ink,
            )
            Gap(24)
            SectionLabel("Fonts")
            Gap(8)
            listOf("EB Garamond" to "licenses/eb-garamond-OFL.txt", "Inter" to "licenses/inter-OFL.txt").forEach { (name, path) ->
                Text(name, style = MaterialTheme.typography.titleMedium, color = paper.ink)
                val text = runCatching { context.assets.open(path).bufferedReader().use { it.readText() } }.getOrDefault("")
                Text(text.lineSequence().take(4).joinToString("\n"), style = MaterialTheme.typography.bodySmall, color = paper.muted)
                Text("SIL Open Font License 1.1", style = MaterialTheme.typography.bodySmall, color = paper.muted)
                Gap(12)
                Hairline()
                Gap(12)
            }
        }
    }
}
