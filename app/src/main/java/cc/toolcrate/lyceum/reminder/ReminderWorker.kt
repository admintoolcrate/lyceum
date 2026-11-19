package cc.toolcrate.lyceum.reminder

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.domain.ReminderSchedule
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Clock
import java.time.LocalDate

/** Runs once each evening: maybe shows the reminder, then books the next one. */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val settings: SettingsStore,
    private val entries: EntryRepository,
    private val scheduler: ReminderScheduler,
    private val notifier: ReminderNotifier,
    private val clock: Clock,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val s = settings.current()
        val written = entries.hasEntry(LocalDate.now(clock))
        if (ReminderSchedule.shouldNotify(clock.instant(), s.reminderTime, clock.zone, s.reminderEnabled, written)) {
            notifier.show()
        }
        scheduler.reschedule()
        return Result.success()
    }
}
