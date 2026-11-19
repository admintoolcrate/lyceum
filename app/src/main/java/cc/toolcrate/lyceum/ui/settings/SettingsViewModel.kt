package cc.toolcrate.lyceum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.reminder.ReminderScheduler
import cc.toolcrate.lyceum.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val store: SettingsStore,
    private val reminders: ReminderScheduler,
) : ViewModel() {
    val settings: StateFlow<Settings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { store.setTheme(mode) }

    fun setReminder(enabled: Boolean, time: LocalTime = settings.value.reminderTime) = viewModelScope.launch {
        store.setReminder(enabled, time)
        reminders.reschedule()
    }
}
