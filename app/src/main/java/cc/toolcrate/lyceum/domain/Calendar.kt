package cc.toolcrate.lyceum.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

/** Monday to Sunday, or whichever day the week starts on. */
data class WeekRange(val start: LocalDate, val end: LocalDate) {
    val days: List<LocalDate> get() = (0L..6L).map { start.plusDays(it) }
    operator fun contains(date: LocalDate) = !date.isBefore(start) && !date.isAfter(end)
    fun shift(weeks: Long) = WeekRange(start.plusWeeks(weeks), end.plusWeeks(weeks))

    companion object {
        fun of(date: LocalDate, firstDay: DayOfWeek = DayOfWeek.MONDAY): WeekRange {
            val start = date.with(TemporalAdjusters.previousOrSame(firstDay))
            return WeekRange(start, start.plusDays(6))
        }
    }
}

object MonthGrid {
    /**
     * The cells of a month calendar, row by row: leading nulls up to the first day, then each date,
     * then trailing nulls to fill the last row.
     */
    fun cells(month: YearMonth, firstDay: DayOfWeek = DayOfWeek.MONDAY): List<LocalDate?> {
        val lead = Math.floorMod(month.atDay(1).dayOfWeek.value - firstDay.value, 7)
        val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
        val total = ((lead + days.size + 6) / 7) * 7
        return List(lead) { null } + days + List(total - lead - days.size) { null }
    }

    /** Day-of-week headers starting from [firstDay]. */
    fun weekdays(firstDay: DayOfWeek = DayOfWeek.MONDAY): List<DayOfWeek> = (0L..6L).map { firstDay.plus(it) }
}

/** A short excerpt of a page around the first word that matches a search. */
object Snippet {
    fun excerpt(text: String, query: String, max: Int = 160): String {
        val flat = text.replace(Regex("\\s+"), " ").trim()
        if (flat.length <= max) return flat
        val terms = terms(query)
        val lower = flat.lowercase()
        val hit = terms.mapNotNull { t -> Regex("\\b" + Regex.escape(t)).find(lower)?.range?.first }.minOrNull() ?: 0
        val start = (hit - max / 3).coerceAtLeast(0).let { s -> if (s == 0) 0 else (flat.indexOf(' ', s).takeIf { it in s until hit } ?: s) + 1 }
        val end = (start + max).coerceAtMost(flat.length)
        return (if (start > 0) "…" else "") + flat.substring(start, end).trim() + (if (end < flat.length) "…" else "")
    }

    /** The lower-cased words of a search, as the highlighter and excerpt use them. */
    fun terms(query: String): List<String> =
        query.lowercase().split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }
}
