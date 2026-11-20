package cc.toolcrate.lyceum.ui.settings

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.backup.DriveBackup
import cc.toolcrate.lyceum.backup.Envelope
import cc.toolcrate.lyceum.backup.drive.DriveAccess
import cc.toolcrate.lyceum.backup.drive.DriveAuth
import cc.toolcrate.lyceum.data.settings.BackupMode
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.security.AppLock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class BackupAction { BackUp, Restore, Delete, EnableWeekly }

@HiltViewModel
class BackupViewModel @Inject constructor(
    settingsStore: SettingsStore,
    private val backup: DriveBackup,
    private val auth: DriveAuth,
    private val lock: AppLock,
) : ViewModel() {
    val settings: StateFlow<Settings> = settingsStore.settings.stateIn(viewModelScope, SharingStarted.Eagerly, Settings())
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    private var pending: BackupAction? = null
    /** Held only until the action runs, then wiped. Never written anywhere. */
    private var passphrase: CharArray? = null

    /** Whether [action] asks for the passphrase first (a manual backup, a restore, turning on weekly). */
    fun needsPassphrase(action: BackupAction): Boolean = when (action) {
        BackupAction.BackUp -> settings.value.backupMode != BackupMode.Weekly || backup.storedKey() == null
        BackupAction.Restore, BackupAction.EnableWeekly -> true
        BackupAction.Delete -> false
    }

    fun prepare(action: BackupAction, pass: CharArray?) {
        wipe()
        pending = action
        passphrase = pass
    }

    suspend fun authorize(activity: Activity): DriveAccess {
        busy.value = true
        lock.expectReturn()
        return auth.authorize(activity)
    }

    fun resultFromIntent(ok: Boolean, data: Intent?): DriveAccess =
        if (ok) auth.resultFromIntent(data) else DriveAccess.Failed("Google Drive wasn't connected.")

    fun onAccess(access: DriveAccess) {
        when (access) {
            is DriveAccess.Granted -> run(access.token)
            is DriveAccess.Failed -> { finish(access.reason) }
            is DriveAccess.NeedsUser -> Unit
        }
    }

    fun setMode(mode: BackupMode) = viewModelScope.launch {
        if (mode != BackupMode.Weekly) backup.setMode(mode)
    }

    private fun run(token: String) = viewModelScope.launch {
        busy.value = true
        val pass = passphrase
        val text = when (pending) {
            BackupAction.BackUp -> {
                val key = pass?.let { withContext(Dispatchers.Default) { Envelope.deriveKey(it) } } ?: backup.storedKey()
                if (key == null) "The passphrase is needed." else backup.backUp(token, key).message
            }
            BackupAction.Restore -> if (pass == null) "The passphrase is needed." else backup.restore(token, pass).message
            BackupAction.Delete -> backup.deleteFromDrive(token).message
            BackupAction.EnableWeekly -> {
                if (pass == null) "The passphrase is needed." else {
                    val key = withContext(Dispatchers.Default) { Envelope.deriveKey(pass) }
                    val outcome = backup.backUp(token, key)
                    if (outcome.ok) {
                        backup.setMode(BackupMode.Weekly, key)
                        "Weekly backup is on, and a first backup is done."
                    } else outcome.message
                }
            }
            null -> null
        }
        finish(text)
    }

    private fun finish(text: String?) {
        wipe()
        pending = null
        busy.value = false
        message.value = text
    }

    private fun wipe() {
        passphrase?.fill(' ')
        passphrase = null
    }

    fun clearMessage() { message.value = null }

    override fun onCleared() = wipe()
}
