package cc.toolcrate.lyceum.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import cc.toolcrate.lyceum.stagira.StagiraContract
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.components.LyceumTopBar
import cc.toolcrate.lyceum.ui.components.PageColumn
import cc.toolcrate.lyceum.ui.components.PaperChip
import cc.toolcrate.lyceum.ui.components.SectionLabel
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.ThemeMode

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenPrompts: () -> Unit,
    onOpenAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val message by viewModel.message.collectAsStateWithLifecycle()
    message?.let { text ->
        AlertDialog(
            onDismissRequest = viewModel::clearMessage,
            text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("OK") } },
            containerColor = LyceumTheme.paper.card,
        )
    }
    Column(Modifier.fillMaxSize()) {
        LyceumTopBar("Settings", onBack = onBack)
        PageColumn {
            SectionLabel("Appearance")
            Gap(8)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    PaperChip(mode.name, settings.theme == mode, onClick = { viewModel.setTheme(mode) })
                }
            }
            Gap(20)
            Hairline()
            SettingRow("Prompts", "Edit, reorder or switch off the daily prompts.", onClick = onOpenPrompts)
            Hairline()
            Gap(12)
            SectionLabel("Reminder")
            ReminderSection(settings) { on, time -> viewModel.setReminder(on, time) }
            Hairline()
            Gap(12)
            SectionLabel("Privacy")
            LockSection(
                settings,
                onEnable = viewModel::enableLock,
                onDisable = viewModel::disableLock,
                onBiometric = viewModel::setBiometric,
                launch = viewModel::launch,
            )
            Hairline()
            Gap(12)
            SectionLabel("Your pages")
            DataSection(onBeforePicker = viewModel::expectReturn, onExport = viewModel::export, onImport = viewModel::import)
            Hairline()
            Gap(12)
            SectionLabel("Backup to Google Drive · optional")
            Gap(8)
            BackupSection()
            Hairline()
            if (remember { StagiraContract.canPick(context) }) {
                SettingSwitch(
                    "Stagira goals",
                    "Link a page to one of your Stagira goals. Only the goal's name and id are kept with the page.",
                    settings.stagiraLinkEnabled,
                    viewModel::setStagiraLink,
                )
                Hairline()
            }
            SettingRow("About", "Principles, fonts and the source of the quotations.", onClick = onOpenAbout)
            Gap(40)
        }
    }
}

@Composable
fun SettingRow(title: String, detail: String? = null, onClick: (() -> Unit)? = null, trailing: @Composable (() -> Unit)? = null) {
    val paper = LyceumTheme.paper
    Row(
        Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = paper.ink)
            if (detail != null) Text(detail, style = MaterialTheme.typography.bodyMedium, color = paper.muted)
        }
        trailing?.invoke()
    }
}

@Composable
fun SettingSwitch(title: String, detail: String?, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val paper = LyceumTheme.paper
    SettingRow(title, detail, onClick = if (enabled) ({ onChange(!checked) }) else null) {
        Switch(
            checked = checked, onCheckedChange = onChange, enabled = enabled,
            colors = SwitchDefaults.colors(checkedTrackColor = paper.accent, checkedThumbColor = paper.page),
        )
    }
}
