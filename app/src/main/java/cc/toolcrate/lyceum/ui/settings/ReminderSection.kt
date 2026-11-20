package cc.toolcrate.lyceum.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.text.format.DateFormat
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSection(settings: Settings, onChange: (enabled: Boolean, time: LocalTime) -> Unit) {
    val context = LocalContext.current
    val paper = LyceumTheme.paper
    var picking by remember { mutableStateOf(false) }
    var denied by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        denied = !granted
        if (granted) onChange(true, settings.reminderTime)
    }

    SettingSwitch(
        title = "Evening reminder",
        detail = if (denied) "Notifications are off for Lyceum in Android's settings."
        else "One gentle nudge, only on days you haven't written yet.",
        checked = settings.reminderEnabled,
        onChange = { on ->
            if (on && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onChange(on, settings.reminderTime)
            }
        },
    )
    if (settings.reminderEnabled) {
        SettingRow(
            title = "Time",
            detail = settings.reminderTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
            onClick = { picking = true },
        )
    }

    if (picking) {
        val state = rememberTimePickerState(
            initialHour = settings.reminderTime.hour,
            initialMinute = settings.reminderTime.minute,
            is24Hour = DateFormat.is24HourFormat(context),
        )
        AlertDialog(
            onDismissRequest = { picking = false },
            confirmButton = {
                TextButton(onClick = {
                    onChange(true, LocalTime.of(state.hour, state.minute))
                    picking = false
                }) { Text("Set") }
            },
            dismissButton = { TextButton(onClick = { picking = false }) { Text("Cancel") } },
            text = {
                TimePicker(
                    state,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = paper.accentSoft,
                        selectorColor = paper.accent,
                        timeSelectorSelectedContainerColor = paper.accentSoft,
                        timeSelectorUnselectedContainerColor = paper.page,
                    ),
                )
            },
            title = { Text("Reminder time", style = MaterialTheme.typography.titleLarge) },
            containerColor = paper.card,
        )
    }
}
