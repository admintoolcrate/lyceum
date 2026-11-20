package cc.toolcrate.lyceum.domain

import java.time.LocalDate

/** What the weekly review gathers from a week's pages. No totals, no scores: just the words. */
data class WeekReview(
    val week: WeekRange,
    val entries: List<Entry>,
    /** Each "one thing I learned" line, by day, in order. */
    val learned: List<Pair<LocalDate, String>>,
    /** Tags the week used, most used first. */
    val tags: List<String>,
    /** The moods given, by day, in order. */
    val moods: List<Pair<LocalDate, Mood>>,
) {
    companion object {
        fun of(week: WeekRange, entries: List<Entry>): WeekReview {
            val inWeek = entries.filter { it.date in week }.sortedBy { it.date }
            val tagCounts = inWeek.flatMap { it.tags }.groupingBy { it }.eachCount()
            return WeekReview(
                week = week,
                entries = inWeek,
                learned = inWeek.mapNotNull { e -> e.learned?.takeIf { it.isNotBlank() }?.let { e.date to it } },
                tags = tagCounts.entries.sortedWith(compareBy({ -it.value }, { it.key.lowercase() })).map { it.key },
                moods = inWeek.mapNotNull { e -> e.mood?.let { e.date to it } },
            )
        }
    }
}
