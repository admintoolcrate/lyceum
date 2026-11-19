package cc.toolcrate.lyceum.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.reminder.ReminderScheduler
import cc.toolcrate.lyceum.backup.DocumentIo
import cc.toolcrate.lyceum.backup.JournalCodec
import cc.toolcrate.lyceum.backup.JournalPorter
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import cc.toolcrate.lyceum.security.AppLock
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
    private val lock: AppLock,
    private val porter: JournalPorter,
    private val files: DocumentIo,
) : ViewModel() {
    /** A short note about the last export, import or backup, shown until dismissed. */
    val message = MutableStateFlow<String?>(null)

    val settings: StateFlow<Settings> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { store.setTheme(mode) }

    fun setReminder(enabled: Boolean, time: LocalTime = settings.value.reminderTime) = viewModelScope.launch {
        store.setReminder(enabled, time)
        reminders.reschedule()
    }

    fun enableLock(pin: String, biometric: Boolean) = viewModelScope.launch { lock.enable(pin, biometric) }

    suspend fun disableLock(pin: String): Boolean {
        if (!lock.checkPin(pin)) return false
        lock.disable()
        return true
    }

    fun setBiometric(on: Boolean) = viewModelScope.launch { store.setLock(true, on) }

    fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }

    /** The next trip out of the app (a file picker, Google) shouldn't lock it on return. */
    fun expectReturn() = lock.expectReturn()

    fun export(uri: Uri) = viewModelScope.launch {
        message.value = runCatching { files.write(uri, porter.exportJson()) }
            .fold({ "Exported. Keep the file somewhere safe; it isn't encrypted." }, { "The export didn't finish: ${it.message}" })
    }

    fun import(uri: Uri) = viewModelScope.launch {
        message.value = try {
            porter.importJson(files.read(uri)).message
        } catch (e: JournalCodec.ImportException) {
            e.message
        } catch (e: Exception) {
            "The file couldn't be read."
        }
    }

    fun clearMessage() { message.value = null }

    fun setStagiraLink(on: Boolean) = viewModelScope.launch { store.setStagiraLink(on) }
}
