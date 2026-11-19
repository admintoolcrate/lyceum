package cc.toolcrate.lyceum.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduleTest {
    private val london = ZoneId.of("Europe/London")
    private val nine = LocalTime.of(21, 0)
    private fun at(text: String, zone: ZoneId = london) = LocalDateTime.parse(text).atZone(zone).toInstant()

    @Test
    fun laterTodayWhenTheTimeIsStillAhead() {
        val next = ReminderSchedule.nextFire(at("2026-06-10T08:00"), nine, london)
        assertEquals(LocalDateTime.parse("2026-06-10T21:00"), next.toLocalDateTime())
        assertEquals(Duration.ofHours(13), ReminderSchedule.delayUntilNext(at("2026-06-10T08:00"), nine, london))
    }

    @Test
    fun tomorrowOnceTheTimeHasPassedOrIsNow() {
        assertEquals(LocalDateTime.parse("2026-06-11T21:00"), ReminderSchedule.nextFire(at("2026-06-10T21:30"), nine, london).toLocalDateTime())
        assertEquals(LocalDateTime.parse("2026-06-11T21:00"), ReminderSchedule.nextFire(at("2026-06-10T21:00"), nine, london).toLocalDateTime())
    }

    @Test
    fun aDayWhenClocksGoForwardIsAnHourShorter() {
        // UK clocks go forward at 01:00 on 29 March 2026.
        val delay = ReminderSchedule.delayUntilNext(at("2026-03-28T21:30"), nine, london)
        assertEquals(Duration.ofHours(22).plusMinutes(30), delay)
    }

    @Test
    fun aTimeInsideTheGapFallsJustAfterIt() {
        val next = ReminderSchedule.nextFire(at("2026-03-28T23:00"), LocalTime.of(1, 30), london)
        assertEquals(LocalDateTime.parse("2026-03-29T02:30"), next.toLocalDateTime())
    }

    @Test
    fun aRepeatedTimeUsesTheFirstOne() {
        // UK clocks go back at 02:00 on 25 October 2026; 01:30 happens twice.
        val next = ReminderSchedule.nextFire(at("2026-10-24T22:00"), LocalTime.of(1, 30), london)
        assertEquals(1, next.offset.totalSeconds / 3600)
    }

    @Test
    fun quietWhenOffOrAlreadyWritten() {
        val now = at("2026-06-10T21:00")
        assertFalse(ReminderSchedule.shouldNotify(now, nine, london, enabled = false, writtenToday = false))
        assertFalse(ReminderSchedule.shouldNotify(now, nine, london, enabled = true, writtenToday = true))
        assertTrue(ReminderSchedule.shouldNotify(now, nine, london, enabled = true, writtenToday = false))
    }

    @Test
    fun aSlightlyEarlyRunStillCounts() {
        assertTrue(ReminderSchedule.shouldNotify(at("2026-06-10T20:55"), nine, london, true, false))
        assertFalse(ReminderSchedule.shouldNotify(at("2026-06-10T15:00"), nine, london, true, false))
    }

    @Test
    fun aVeryLateRunStaysQuietRatherThanNaggingAboutYesterday() {
        assertTrue(ReminderSchedule.shouldNotify(at("2026-06-10T23:30"), nine, london, true, false))
        assertFalse(ReminderSchedule.shouldNotify(at("2026-06-11T00:30"), nine, london, true, false))
        assertFalse(ReminderSchedule.shouldNotify(at("2026-06-11T09:00"), nine, london, true, false))
    }
}
