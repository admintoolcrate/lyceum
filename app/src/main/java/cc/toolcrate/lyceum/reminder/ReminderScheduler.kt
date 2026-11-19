package cc.toolcrate.lyceum.reminder

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.domain.ReminderSchedule
import java.time.Clock
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Keeps exactly one pending reminder: the next evening's, or none when reminders are off. */
@Singleton
class ReminderScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val settings: SettingsStore,
    private val clock: Clock,
) {
    suspend fun reschedule() {
        val s = settings.current()
        if (s.reminderEnabled) schedule(s.reminderTime) else cancel()
    }

    fun schedule(time: LocalTime) {
        val delay = ReminderSchedule.delayUntilNext(clock.instant(), time, clock.zone)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .addTag(WORK_NAME)
            .build()
        workManager.enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel() {
        workManager.cancelUniqueWork(WORK_NAME)
    }

    companion object {
        const val WORK_NAME = "evening-reminder"
    }
}
