package cc.toolcrate.lyceum.data

import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class EntryRepositoryTest : DbTest() {
    private val clock = TestClock()
    private lateinit var repo: EntryRepository
    private val day = LocalDate.of(2026, 3, 10)

    @Before
    fun setUp() {
        repo = EntryRepository(db, clock)
    }

    private fun page(date: LocalDate = day, body: String = "A calm morning.", tags: List<String> = emptyList()) =
        Entry(date = date, prompt = "What went well?", body = body, tags = tags)

    @Test
    fun saveCreatesThenUpdatesTheSameDay() = runTest {
        val first = repo.save(page(body = "First"))
        clock.advanceMinutes(30)
        val second = repo.save(page(body = "Second", tags = listOf("walk")).copy(mood = Mood.Good, learned = " patience "))

        assertEquals(first.createdAt, second.createdAt)
        assertTrue(second.updatedAt > first.updatedAt)
        val stored = repo.get(day)!!
        assertEquals("Second", stored.body)
        assertEquals(Mood.Good, stored.mood)
        assertEquals("patience", stored.learned)
        assertEquals(listOf("walk"), stored.tags)
        assertEquals(1, repo.all().size)
    }

    @Test
    fun blankLearnedIsStoredAsNothing() = runTest {
        repo.save(page().copy(learned = "   "))
        assertNull(repo.get(day)!!.learned)
    }

    @Test
    fun tagsAreTidiedAndUnusedOnesDisappear() = runTest {
        repo.save(page(tags = listOf("#Family", "family", "  long   walk ", "")))
        assertEquals(listOf("Family", "long walk"), repo.get(day)!!.tags)

        repo.save(page(tags = listOf("work")))
        assertEquals(listOf("work"), repo.observeTags().first().map { it.name })
    }

    @Test
    fun tagMatchingIgnoresCase() = runTest {
        repo.save(page(date = day, tags = listOf("Family")))
        repo.save(page(date = day.plusDays(1), tags = listOf("family")))
        val uses = repo.observeTags().first()
        assertEquals(1, uses.size)
        assertEquals(2, uses.single().uses)
    }

    @Test
    fun deleteRemovesPageAndItsTags() = runTest {
        repo.save(page(tags = listOf("solo")))
        repo.delete(day)
        assertNull(repo.get(day))
        assertFalse(repo.hasEntry(day))
        assertTrue(repo.observeTags().first().isEmpty())
    }

    @Test
    fun rangeAndDatesCoverOnlyTheGivenDays() = runTest {
        (0L..9L).forEach { repo.save(page(date = day.plusDays(it), body = "Day $it")) }
        val week = repo.observeRange(day.plusDays(2), day.plusDays(4)).first()
        assertEquals(listOf("Day 2", "Day 3", "Day 4"), week.map { it.body })
        assertEquals(setOf(day, day.plusDays(1)), repo.observeDates(day, day.plusDays(1)).first())
    }

    @Test
    fun searchFindsWordPrefixesNewestFirst() = runTest {
        repo.save(page(date = day, body = "Kindness from a stranger."))
        repo.save(page(date = day.plusDays(1), body = "Unkind words at work.").copy(learned = "Kind words cost nothing"))
        repo.save(page(date = day.plusDays(2), body = "Nothing of note."))

        assertEquals(listOf(day.plusDays(1), day), repo.search("kind", null).first().map { it.date })
        assertTrue(repo.search("  ", null).first().isEmpty())
        assertTrue(repo.search("\"(*", null).first().isEmpty())
    }

    @Test
    fun searchCanBeNarrowedToATag() = runTest {
        repo.save(page(date = day, body = "Walked by the river.", tags = listOf("outside")))
        repo.save(page(date = day.plusDays(1), body = "Walked to the shop."))

        assertEquals(listOf(day), repo.search("walked", "outside").first().map { it.date })
        assertEquals(listOf(day), repo.search("", "outside").first().map { it.date })
    }

    @Test
    fun searchSeesEditsToTheBody() = runTest {
        repo.save(page(body = "apples"))
        repo.save(page(body = "pears"))
        assertTrue(repo.search("apples", null).first().isEmpty())
        assertEquals(1, repo.search("pears", null).first().size)
    }

    @Test
    fun goalReferenceRoundTrips() = runTest {
        repo.save(page().copy(goal = GoalRef("g-42", "Read daily")))
        assertEquals(GoalRef("g-42", "Read daily"), repo.get(day)!!.goal)
    }

    @Test
    fun putKeepsTheGivenTimestamps() = runTest {
        repo.put(page().copy(createdAt = 111, updatedAt = 222, tags = listOf("imported")))
        val stored = repo.get(day)!!
        assertEquals(111, stored.createdAt)
        assertEquals(222, stored.updatedAt)
        assertEquals(listOf("imported"), stored.tags)
    }

    @Test
    fun searchAlsoFindsPagesByTheStartOfATagName() = runTest {
        repo.save(page(date = day, body = "Said no to the extra project.", tags = listOf("work")))
        repo.save(page(date = day.plusDays(1), body = "Quiet day.", tags = listOf("workshop")))
        repo.save(page(date = day.plusDays(2), body = "Walked to work.", tags = listOf("walk")))
        repo.save(page(date = day.plusDays(3), body = "Nothing here."))

        assertEquals(listOf(day.plusDays(2), day.plusDays(1), day), repo.search("work", null).first().map { it.date })
        // Narrowed to the exact tag "work", "workshop" and "walk" drop out.
        assertEquals(listOf(day), repo.search("#work", "work").first().map { it.date })
    }

    @Test
    fun tagSearchTreatsWildcardsAsPlainText() = runTest {
        repo.save(page(date = day, body = "A", tags = listOf("100%")))
        repo.save(page(date = day.plusDays(1), body = "B", tags = listOf("1000")))
        assertEquals(listOf(day), repo.search("100%", null).first().map { it.date })
    }
}
