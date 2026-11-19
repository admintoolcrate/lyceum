package cc.toolcrate.lyceum.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cc.toolcrate.lyceum.backup.drive.DriveAccess
import cc.toolcrate.lyceum.backup.drive.DriveAuth
import cc.toolcrate.lyceum.data.settings.BackupMode
import cc.toolcrate.lyceum.data.settings.SettingsStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** The weekly backup, on Wi-Fi with enough battery. It never shows a notification. */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settings: SettingsStore,
    private val backup: DriveBackup,
    private val auth: DriveAuth,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (settings.current().backupMode != BackupMode.Weekly) return Result.success()
        val key = backup.storedKey() ?: run {
            settings.setBackupNote("Weekly backup needs the passphrase again. Open Settings → Backup.")
            return Result.success()
        }
        return when (val access = auth.authorizeSilently()) {
            is DriveAccess.Granted -> {
                val outcome = backup.backUp(access.token, key)
                if (!outcome.ok) settings.setBackupNote(outcome.message)
                Result.success()
            }
            is DriveAccess.Failed -> { settings.setBackupNote(access.reason); Result.success() }
            is DriveAccess.NeedsUser -> Result.success()
        }
    }
}
