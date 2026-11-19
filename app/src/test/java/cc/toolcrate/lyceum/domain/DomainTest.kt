package cc.toolcrate.lyceum.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PromptRotationTest {
    private val prompts = listOf("A", "B", "C")

    @Test
    fun sameDaySamePrompt() {
        val d = LocalDate.of(2026, 5, 5)
        assertEquals(PromptRotation.promptFor(d, prompts), PromptRotation.promptFor(d, prompts))
    }

    @Test
    fun consecutiveDaysTakeTurns() {
        val start = LocalDate.of(2026, 5, 5)
        val picks = (0L until 6L).map { PromptRotation.promptFor(start.plusDays(it), prompts) }
        assertEquals(picks.take(3), picks.drop(3))
        assertEquals(setOf("A", "B", "C"), picks.toSet())
    }

    @Test
    fun worksBeforeTheEpoch() {
        val d = LocalDate.of(1960, 1, 1)
        assertEquals(prompts[Math.floorMod(d.toEpochDay(), 3L).toInt()], PromptRotation.promptFor(d, prompts))
    }

    @Test
    fun fallsBackWhenNothingIsEnabled() {
        assertEquals(PromptRotation.FALLBACK, PromptRotation.promptFor(LocalDate.of(2026, 1, 1), listOf(" ", "")))
    }
}

class TagsTest {
    @Test
    fun normalizes() {
        assertEquals(listOf("Family", "a b"), Tags.normalize(listOf(" #Family ", "family", "a   b", "#", "")))
    }

    @Test
    fun capsLength() {
        assertEquals(Tags.MAX_LENGTH, Tags.clean("x".repeat(100))!!.length)
    }
}

class SearchQueryTest {
    @Test
    fun quotesEachWordAsAPrefix() {
        assertEquals("kind* words*", SearchQuery.toMatch("  Kind, words! "))
    }

    @Test
    fun dropsOperatorsAndApostrophes() {
        assertEquals("dont* or*", SearchQuery.toMatch("don't OR \"*"))
        assertNull(SearchQuery.toMatch("*()\""))
    }
}

class MoodTest {
    @Test
    fun roundTripsByValue() {
        Mood.entries.forEach { assertEquals(it, Mood.of(it.value)) }
        assertNull(Mood.of(9))
        assertNull(Mood.of(null))
    }
}
