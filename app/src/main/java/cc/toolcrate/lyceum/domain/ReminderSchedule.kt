package cc.toolcrate.lyceum.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * When the evening reminder fires, and whether it should say anything when it does.
 * Pure: everything it needs is passed in, so it is tested without Android.
 */
object ReminderSchedule {
    /** A reminder that runs this much later than asked (Doze, a dead battery) stays quiet. */
    val LATE_LIMIT: Duration = Duration.ofHours(3)

    /**
     * The next moment strictly after [now] when the clock in [zone] reads [time]. On a day when that
     * time doesn't exist (clocks go forward), it falls just after the gap; when it happens twice
     * (clocks go back), the first one is used.
     */
    fun nextFire(now: Instant, time: LocalTime, zone: ZoneId): ZonedDateTime {
        val local = now.atZone(zone)
        val today = ZonedDateTime.of(local.toLocalDate(), time, zone)
        return if (today.toInstant().isAfter(now)) today
        else ZonedDateTime.of(local.toLocalDate().plusDays(1), time, zone)
    }

    fun delayUntilNext(now: Instant, time: LocalTime, zone: ZoneId): Duration =
        Duration.between(now, nextFire(now, time, zone).toInstant())

    /**
     * Whether a reminder running at [now] should show. It stays quiet if reminders are off, if today's
     * page is already written, or if it is running so late that it would land after the evening it was
     * meant for. It never nags about a day already past.
     */
    fun shouldNotify(now: Instant, time: LocalTime, zone: ZoneId, enabled: Boolean, writtenToday: Boolean): Boolean {
        if (!enabled || writtenToday) return false
        val local = now.atZone(zone)
        val target = ZonedDateTime.of(local.toLocalDate(), time, zone)
        // A few minutes early is fine: WorkManager can run slightly ahead.
        val early = Duration.between(local, target)
        if (early > Duration.ofMinutes(10)) return false
        val late = Duration.between(target, local)
        return late < LATE_LIMIT
    }
}
