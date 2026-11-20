package cc.toolcrate.lyceum.backup

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cc.toolcrate.lyceum.backup.drive.DriveClient
import cc.toolcrate.lyceum.backup.drive.DriveException
import cc.toolcrate.lyceum.data.settings.BackupMode
import cc.toolcrate.lyceum.data.settings.SettingsStore
import java.io.IOException
import java.time.Clock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** What happened, in words for the user. */
data class Outcome(val ok: Boolean, val message: String)

/** Sealed backups to, and restores from, the user's own Google Drive. */
@Singleton
class DriveBackup @Inject constructor(
    private val porter: JournalPorter,
    private val settings: SettingsStore,
    private val vault: KeyVault,
    private val workManager: WorkManager,
    private val clock: Clock,
) {
    /** Seals the whole journal with [key] and replaces the backup in Drive. Returns a note for the user. */
    suspend fun backUp(token: String, key: BackupKey): Outcome = guard {
        DriveClient(token).upload(Envelope.seal(porter.exportJson(), key))
        settings.setLastBackup(clock.millis())
        settings.setBackupNote(null)
        "Backed up to your Google Drive."
    }

    /** Opens the backup with [passphrase] and merges it into this journal. */
    suspend fun restore(token: String, passphrase: CharArray): Outcome = guard {
        val drive = DriveClient(token)
        val id = drive.findBackup() ?: throw Envelope.Unreadable("There's no Lyceum backup in this Google Drive yet.")
        val sealed = Envelope.read(drive.download(id))
        porter.importJson(Envelope.open(sealed, passphrase)).message
    }

    suspend fun deleteFromDrive(token: String): Outcome = guard {
        if (DriveClient(token).deleteBackup()) "The backup was deleted from your Google Drive." else "There was no backup to delete."
    }

    /** Switches between Off, Manual and Weekly, keeping the stored key and the schedule in step. */
    suspend fun setMode(mode: BackupMode, weeklyKey: BackupKey? = null) {
        if (mode == BackupMode.Weekly) {
            vault.save(requireNotNull(weeklyKey))
            val request = PeriodicWorkRequestBuilder<BackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).setRequiresBatteryNotLow(true).build())
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            vault.clear()
            workManager.cancelUniqueWork(WORK_NAME)
        }
        settings.setBackupMode(mode)
        settings.setBackupNote(null)
    }

    fun storedKey(): BackupKey? = vault.load()

    private suspend fun guard(block: suspend () -> String): Outcome = try {
        Outcome(true, block())
    } catch (e: Envelope.WrongPassphrase) {
        Outcome(false, e.message!!)
    } catch (e: Envelope.Unreadable) {
        Outcome(false, e.message!!)
    } catch (e: JournalCodec.ImportException) {
        Outcome(false, e.message!!)
    } catch (e: DriveException) {
        Outcome(
            false,
            if (e.code == 401 || e.code == 403) "Google Drive refused access. Try connecting again."
            else "Google Drive had a problem (${e.code}). Try again later.",
        )
    } catch (e: IOException) {
        Outcome(false, "Google Drive couldn't be reached. Check the connection and try again.")
    }

    companion object {
        const val WORK_NAME = "weekly-drive-backup"
    }
}
