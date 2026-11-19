package cc.toolcrate.lyceum.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class WeekReviewTest {
    private val week = WeekRange.of(LocalDate.of(2026, 11, 16))
    private fun e(day: Int, learned: String? = null, mood: Mood? = null, tags: List<String> = emptyList()) =
        Entry(date = LocalDate.of(2026, 11, day), prompt = "", body = "b$day", learned = learned, mood = mood, tags = tags)

    @Test
    fun gathersOnlyTheWeekInOrder() {
        val review = WeekReview.of(week, listOf(e(20), e(15), e(16), e(23)))
        assertEquals(listOf(16, 20), review.entries.map { it.date.dayOfMonth })
    }

    @Test
    fun collectsLearnedLinesMoodsAndTags() {
        val review = WeekReview.of(
            week,
            listOf(
                e(18, learned = "Rest helps", mood = Mood.Even, tags = listOf("work", "sleep")),
                e(16, learned = " ", mood = Mood.Low, tags = listOf("work")),
                e(17, tags = listOf("Family")),
            ),
        )
        assertEquals(listOf(LocalDate.of(2026, 11, 18) to "Rest helps"), review.learned)
        assertEquals(listOf(Mood.Low, Mood.Even), review.moods.map { it.second })
        assertEquals(listOf("work", "Family", "sleep"), review.tags)
    }
}
