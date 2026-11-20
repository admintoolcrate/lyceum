package cc.toolcrate.lyceum.data

import cc.toolcrate.lyceum.data.db.SeedPrompts
import cc.toolcrate.lyceum.data.repo.PromptRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class PromptRepositoryTest : DbTest() {
    private lateinit var repo: PromptRepository

    @Before
    fun setUp() {
        repo = PromptRepository(db)
    }

    @Test
    fun seedsOnceOnly() = runTest {
        repo.ensureSeeded()
        repo.ensureSeeded()
        val all = repo.observeAll().first()
        assertEquals(SeedPrompts.all, all.map { it.text })
        assertTrue(all.all { it.builtIn && it.enabled })
    }

    @Test
    fun todayRotatesThroughEnabledPromptsOnly() = runTest {
        repo.ensureSeeded()
        repo.observeAll().first().drop(1).forEach { repo.setEnabled(it.id, false) }
        val day = LocalDate.of(2026, 1, 1)
        repeat(3) { assertEquals(SeedPrompts.all.first(), repo.observeToday(day.plusDays(it.toLong())).first()) }
    }

    @Test
    fun addEditReorderAndDelete() = runTest {
        repo.add("  Mine?  ")
        repo.add("   ")
        repo.add("Second?")
        var list = repo.observeAll().first()
        assertEquals(listOf("Mine?", "Second?"), list.map { it.text })

        repo.edit(list[0].id, "Mine, edited?")
        repo.reorder(listOf(list[1].id))
        list = repo.observeAll().first()
        assertEquals(listOf("Second?", "Mine, edited?"), list.map { it.text })

        repo.delete(list[0].id)
        assertEquals(listOf("Mine, edited?"), repo.observeAll().first().map { it.text })
    }

    @Test
    fun restoreDefaultsKeepsTheUsersOwnAfterThem() = runTest {
        repo.ensureSeeded()
        val first = repo.observeAll().first().first()
        repo.edit(first.id, "Changed")
        repo.setEnabled(first.id, false)
        repo.add("My own?")

        repo.restoreDefaults()
        assertEquals(SeedPrompts.all + "My own?", repo.observeAll().first().map { it.text })
    }

    @Test
    fun mergeSkipsPromptsAlreadyPresent() = runTest {
        repo.ensureSeeded()
        repo.merge(listOf(SeedPrompts.all[0].uppercase(), "New one?", "New one?", ""))
        assertEquals(SeedPrompts.all + "New one?", repo.observeAll().first().map { it.text })
    }
}
