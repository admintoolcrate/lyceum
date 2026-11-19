package cc.toolcrate.lyceum.backup

import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class JournalCodecTest {
    private val full = Entry(
        date = LocalDate.of(2026, 3, 10), prompt = "What went well?", body = "Line one\nLine \"two\" — ünïcode ✓",
        mood = Mood.Bright, learned = "Rest", tags = listOf("a", "b"), goal = GoalRef("g1", "Read"), createdAt = 5, updatedAt = 9,
    )
    private val bare = Entry(date = LocalDate.of(2026, 3, 11), prompt = "", body = "Only words")

    @Test
    fun roundTrips() {
        val prompts = listOf(JournalCodec.PromptJson("Mine?", enabled = false, builtIn = false))
        val text = JournalCodec.encode(listOf(full, bare), prompts, exportedAt = 42)
        val back = JournalCodec.decode(text)
        assertEquals(listOf(full, bare), back.entries)
        assertEquals(prompts, back.prompts)
    }

    @Test
    fun writesAReadableVersionedFile() {
        val text = JournalCodec.encode(listOf(full), emptyList(), 0)
        assertTrue(text.contains("\"format\": \"lyceum\""))
        assertTrue(text.contains("\"version\": 1"))
        assertTrue(text.contains("\"date\": \"2026-03-10\""))
        assertTrue(text.contains("\"mood\": \"Bright\""))
    }

    @Test
    fun toleratesUnknownFieldsAndSkipsBadDates() {
        val text = """
            {"format":"lyceum","version":1,"future":true,
             "entries":[{"date":"2026-02-30","body":"x"},{"date":"2026-02-01","body":"ok","mood":"good","extra":1}]}
        """.trimIndent()
        val back = JournalCodec.decode(text)
        assertEquals(1, back.entries.size)
        assertEquals(Mood.Good, back.entries.single().mood)
    }

    @Test
    fun refusesOtherFilesAndNewerVersions() {
        assertThrows(JournalCodec.ImportException::class.java) { JournalCodec.decode("not json") }
        assertThrows(JournalCodec.ImportException::class.java) { JournalCodec.decode("""{"format":"other","version":1}""") }
        val newer = assertThrows(JournalCodec.ImportException::class.java) { JournalCodec.decode("""{"format":"lyceum","version":2}""") }
        assertTrue(newer.message!!.contains("newer"))
    }
}
