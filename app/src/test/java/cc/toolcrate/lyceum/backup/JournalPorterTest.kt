package cc.toolcrate.lyceum.backup

import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.DbTest
import cc.toolcrate.lyceum.data.db.SeedPrompts
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.Entry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class JournalPorterTest : DbTest() {
    private val clock = TestClock()

    @Test
    fun exportThenImportIntoAnEmptyJournalRestoresEverything() = runTest {
        val entries = EntryRepository(db, clock)
        val prompts = PromptRepository(db)
        prompts.ensureSeeded()
        prompts.add("My own?")
        entries.save(Entry(date = LocalDate.of(2026, 3, 1), prompt = "p", body = "One", tags = listOf("t")))
        entries.save(Entry(date = LocalDate.of(2026, 3, 2), prompt = "p", body = "Two"))
        val file = JournalPorter(entries, prompts, clock).exportJson()

        // A fresh phone.
        db.clearAllTables()
        prompts.ensureSeeded()
        val summary = JournalPorter(entries, prompts, clock).importJson(file)

        assertEquals(2, summary.added)
        assertEquals(listOf("One", "Two"), entries.all().map { it.body })
        assertEquals(listOf("t"), entries.all().first().tags)
        assertEquals(SeedPrompts.all + "My own?", prompts.observeAll().first().map { it.text })
    }

    @Test
    fun importingTheSameFileTwiceChangesNothing() = runTest {
        val entries = EntryRepository(db, clock)
        val prompts = PromptRepository(db)
        entries.save(Entry(date = LocalDate.of(2026, 3, 1), prompt = "p", body = "One"))
        val porter = JournalPorter(entries, prompts, clock)
        val file = porter.exportJson()
        val summary = porter.importJson(file)
        assertEquals(ImportSummary(0, 0, 1), summary)
        assertEquals("Everything in the file was already here.", summary.message)
    }
}
