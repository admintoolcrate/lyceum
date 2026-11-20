package cc.toolcrate.lyceum.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.backup.Envelope
import cc.toolcrate.lyceum.backup.drive.DriveAccess
import cc.toolcrate.lyceum.data.settings.BackupMode
import cc.toolcrate.lyceum.ui.components.Gap
import cc.toolcrate.lyceum.ui.components.PaperChip
import cc.toolcrate.lyceum.ui.editor.paperFieldColors
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** The optional encrypted backup to the user's own Google Drive: Off, Manual, or Weekly. */
@Composable
fun BackupSection(viewModel: BackupViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val paper = LyceumTheme.paper
    val activity = LocalContext.current as Activity
    val scope = rememberCoroutineScope()
    var asking by remember { mutableStateOf<BackupAction?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    val consent = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        viewModel.onAccess(viewModel.resultFromIntent(result.resultCode == Activity.RESULT_OK, result.data))
    }
    fun go(action: BackupAction, pass: CharArray?) {
        viewModel.prepare(action, pass)
        scope.launch {
            when (val access = viewModel.authorize(activity)) {
                is DriveAccess.NeedsUser -> consent.launch(IntentSenderRequest.Builder(access.intent).build())
                else -> viewModel.onAccess(access)
            }
        }
    }
    fun start(action: BackupAction) {
        if (viewModel.needsPassphrase(action)) asking = action else go(action, null)
    }

    Text(
        "An encrypted copy in a hidden folder of your own Google Drive. It is sealed with a passphrase " +
            "only you know; without it, no one can read it, Google included.",
        style = MaterialTheme.typography.bodyMedium, color = paper.muted,
    )
    Gap(12)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        BackupMode.entries.forEach { mode ->
            PaperChip(mode.name, settings.backupMode == mode, onClick = {
                when (mode) {
                    BackupMode.Weekly -> if (settings.backupMode != BackupMode.Weekly) start(BackupAction.EnableWeekly)
                    else -> viewModel.setMode(mode)
                }
            })
        }
    }
    Gap(8)
    Text(
        when (settings.backupMode) {
            BackupMode.Off -> "Nothing leaves this phone."
            BackupMode.Manual -> "Back up when you choose. You type the passphrase each time, and it is never stored."
            BackupMode.Weekly -> "Backs up once a week on Wi-Fi. To do that unattended, a key made from your passphrase " +
                "is kept on this phone, locked by Android's keystore. That is less private than Manual."
        },
        style = MaterialTheme.typography.bodySmall, color = paper.muted,
    )
    if (busy) {
        Gap(8)
        LinearProgressIndicator(Modifier.fillMaxWidth(), color = paper.accent, trackColor = paper.accentSoft)
    }
    if (settings.backupMode != BackupMode.Off) {
        settings.backupNote?.let { Gap(8); Text(it, style = MaterialTheme.typography.bodySmall, color = paper.accent) }
        SettingRow(
            "Back up now",
            if (settings.lastBackupAt > 0) "Last backup " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .format(Instant.ofEpochMilli(settings.lastBackupAt).atZone(ZoneId.systemDefault()))
            else "No backup yet.",
            onClick = if (busy) null else ({ start(BackupAction.BackUp) }),
        )
        SettingRow(
            "Restore from Drive",
            "Adds the backed-up pages to this journal. Where a day exists in both, the newer page is kept.",
            onClick = if (busy) null else ({ start(BackupAction.Restore) }),
        )
        SettingRow("Delete the backup from Drive", onClick = if (busy) null else ({ confirmDelete = true }))
    }

    asking?.let { action ->
        PassphraseDialog(
            creating = action != BackupAction.Restore,
            onDismiss = { asking = null },
            onConfirm = { pass -> asking = null; go(action, pass) },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete the backup from Drive?") },
            text = { Text("The pages on this phone stay as they are.") },
            confirmButton = { TextButton(onClick = { confirmDelete = false; go(BackupAction.Delete, null) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
            containerColor = paper.card,
        )
    }
    message?.let { text ->
        AlertDialog(
            onDismissRequest = viewModel::clearMessage,
            text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("OK") } },
            containerColor = paper.card,
        )
    }
}

@Composable
private fun PassphraseDialog(creating: Boolean, onDismiss: () -> Unit, onConfirm: (CharArray) -> Unit) {
    val paper = LyceumTheme.paper
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    val longEnough = first.length >= Envelope.MIN_PASSPHRASE
    val ok = if (creating) longEnough && first == second else first.isNotEmpty()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (creating) "Backup passphrase" else "Passphrase for the backup") },
        text = {
            Column {
                Text(
                    if (creating) "At least ${Envelope.MIN_PASSPHRASE} characters. Lyceum can't recover it: " +
                        "without it, the backup can't be opened."
                    else "The passphrase the backup was made with.",
                    style = MaterialTheme.typography.bodySmall, color = paper.muted,
                )
                Gap(12)
                OutlinedTextField(
                    first, { first = it }, singleLine = true, label = { Text("Passphrase") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), colors = paperFieldColors(),
                )
                if (creating) {
                    Gap(8)
                    OutlinedTextField(
                        second, { second = it }, singleLine = true, label = { Text("Once more") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = second.isNotEmpty() && second != first,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), colors = paperFieldColors(),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onConfirm(first.toCharArray()) }, enabled = ok) { Text("Continue") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = paper.card,
    )
}
