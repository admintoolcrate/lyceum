package cc.toolcrate.lyceum.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import cc.toolcrate.lyceum.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/** How the optional Drive backup runs. */
enum class BackupMode { Off, Manual, Weekly }

data class Settings(
    val theme: ThemeMode = ThemeMode.System,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = LocalTime.of(21, 0),
    val lockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val backupMode: BackupMode = BackupMode.Off,
    val lastBackupAt: Long = 0,
    /** Why the last automatic backup didn't happen, if it didn't. */
    val backupNote: String? = null,
    val stagiraLinkEnabled: Boolean = true,
)

@Singleton
class SettingsStore @Inject constructor(private val store: DataStore<Preferences>) {

    val settings: Flow<Settings> = store.data.map { p ->
        Settings(
            theme = p[THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System,
            reminderEnabled = p[REMINDER_ON] ?: false,
            reminderTime = LocalTime.ofSecondOfDay(((p[REMINDER_MINUTE] ?: (21 * 60)) * 60).toLong()),
            lockEnabled = p[LOCK_ON] ?: false,
            biometricEnabled = p[BIOMETRIC_ON] ?: false,
            backupMode = p[BACKUP_MODE]?.let { runCatching { BackupMode.valueOf(it) }.getOrNull() } ?: BackupMode.Off,
            lastBackupAt = p[LAST_BACKUP] ?: 0,
            backupNote = p[BACKUP_NOTE],
            stagiraLinkEnabled = p[STAGIRA_ON] ?: true,
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setTheme(mode: ThemeMode) = store.edit { it[THEME] = mode.name }

    suspend fun setReminder(enabled: Boolean, time: LocalTime) = store.edit {
        it[REMINDER_ON] = enabled
        it[REMINDER_MINUTE] = time.hour * 60 + time.minute
    }

    suspend fun setLock(enabled: Boolean, biometric: Boolean) = store.edit {
        it[LOCK_ON] = enabled
        it[BIOMETRIC_ON] = enabled && biometric
    }

    suspend fun setBackupMode(mode: BackupMode) = store.edit { it[BACKUP_MODE] = mode.name }

    suspend fun setLastBackup(at: Long) = store.edit { it[LAST_BACKUP] = at }

    suspend fun setBackupNote(note: String?) = store.edit { if (note == null) it.remove(BACKUP_NOTE) else it[BACKUP_NOTE] = note }

    suspend fun setStagiraLink(enabled: Boolean) = store.edit { it[STAGIRA_ON] = enabled }

    // The PIN's salted hash and the lockout state live here too, read only by security/.
    suspend fun pinRecord(): String? = store.data.first()[PIN_RECORD]
    suspend fun setPinRecord(record: String?) = store.edit {
        if (record == null) it.remove(PIN_RECORD) else it[PIN_RECORD] = record
    }
    suspend fun pinFailures(): Pair<Int, Long> = store.data.first().let { (it[PIN_FAILS] ?: 0) to (it[PIN_LAST_FAIL] ?: 0L) }
    suspend fun setPinFailures(count: Int, at: Long) = store.edit {
        it[PIN_FAILS] = count
        it[PIN_LAST_FAIL] = at
    }

    private companion object {
        val THEME = stringPreferencesKey("theme")
        val REMINDER_ON = booleanPreferencesKey("reminder_on")
        val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
        val LOCK_ON = booleanPreferencesKey("lock_on")
        val BIOMETRIC_ON = booleanPreferencesKey("biometric_on")
        val BACKUP_MODE = stringPreferencesKey("backup_mode")
        val LAST_BACKUP = longPreferencesKey("last_backup")
        val BACKUP_NOTE = stringPreferencesKey("backup_note")
        val STAGIRA_ON = booleanPreferencesKey("stagira_on")
        val PIN_RECORD = stringPreferencesKey("pin_record")
        val PIN_FAILS = intPreferencesKey("pin_fails")
        val PIN_LAST_FAIL = longPreferencesKey("pin_last_fail")
    }
}
