package cc.toolcrate.lyceum

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/** A clock the test moves by hand. */
class TestClock(var now: Instant = Instant.parse("2026-03-10T09:00:00Z"), private val zone: ZoneId = ZoneOffset.UTC) : Clock() {
    override fun getZone(): ZoneId = zone
    override fun withZone(zone: ZoneId): Clock = TestClock(now, zone)
    override fun instant(): Instant = now
    fun advanceMinutes(minutes: Long) { now = now.plusSeconds(minutes * 60) }
}
