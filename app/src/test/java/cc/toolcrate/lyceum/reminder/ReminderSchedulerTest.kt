package cc.toolcrate.lyceum.reminder

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.settings.SettingsStore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class ReminderSchedulerTest {
    @get:Rule val tmp = TemporaryFolder()

    private lateinit var workManager: WorkManager
    private lateinit var settings: SettingsStore
    private lateinit var scheduler: ReminderScheduler
    private val clock = TestClock(Instant.parse("2026-06-10T07:00:00Z"), ZoneId.of("Europe/London")) // 08:00 local

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context, Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )
        workManager = WorkManager.getInstance(context)
        settings = SettingsStore(PreferenceDataStoreFactory.create { tmp.newFile("s.preferences_pb") })
        scheduler = ReminderScheduler(workManager, settings, clock)
    }

    private fun pending(): List<WorkInfo> =
        workManager.getWorkInfosForUniqueWork(ReminderScheduler.WORK_NAME).get().filter { it.state == WorkInfo.State.ENQUEUED }

    @Test
    fun turningOnBooksOneReminderForTonight() = runTest {
        settings.setReminder(true, LocalTime.of(21, 0))
        scheduler.reschedule()
        scheduler.reschedule()
        val work = pending()
        assertEquals(1, work.size)
        assertEquals(TimeUnit.HOURS.toMillis(13), work.single().initialDelayMillis)
    }

    @Test
    fun turningOffCancels() = runTest {
        settings.setReminder(true, LocalTime.of(21, 0))
        scheduler.reschedule()
        settings.setReminder(false, LocalTime.of(21, 0))
        scheduler.reschedule()
        assertTrue(pending().isEmpty())
    }

    @Test
    fun changingTheTimeReplacesTheBooking() = runTest {
        settings.setReminder(true, LocalTime.of(21, 0))
        scheduler.reschedule()
        settings.setReminder(true, LocalTime.of(9, 0))
        scheduler.reschedule()
        val work = pending()
        assertEquals(1, work.size)
        assertEquals(TimeUnit.HOURS.toMillis(1), work.single().initialDelayMillis)
    }
}
