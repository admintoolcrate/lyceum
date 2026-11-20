package cc.toolcrate.lyceum.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class WeekRangeTest {
    @Test
    fun mondayToSunday() {
        val w = WeekRange.of(LocalDate.of(2026, 11, 19)) // a Thursday
        assertEquals(LocalDate.of(2026, 11, 16), w.start)
        assertEquals(LocalDate.of(2026, 11, 22), w.end)
        assertEquals(7, w.days.size)
    }

    @Test
    fun aMondayStartsItsOwnWeekAndSundayEndsIt() {
        assertEquals(LocalDate.of(2026, 11, 16), WeekRange.of(LocalDate.of(2026, 11, 16)).start)
        assertEquals(LocalDate.of(2026, 11, 16), WeekRange.of(LocalDate.of(2026, 11, 22)).start)
    }

    @Test
    fun crossesYearsAndShifts() {
        val w = WeekRange.of(LocalDate.of(2027, 1, 1)) // a Friday
        assertEquals(LocalDate.of(2026, 12, 28), w.start)
        assertTrue(LocalDate.of(2027, 1, 3) in w)
        assertFalse(LocalDate.of(2027, 1, 4) in w)
        assertEquals(LocalDate.of(2026, 12, 21), w.shift(-1).start)
    }

    @Test
    fun sundayFirstWeeks() {
        assertEquals(LocalDate.of(2026, 11, 15), WeekRange.of(LocalDate.of(2026, 11, 19), DayOfWeek.SUNDAY).start)
    }
}

class MonthGridTest {
    @Test
    fun padsToWholeWeeks() {
        val cells = MonthGrid.cells(YearMonth.of(2026, 11)) // 1 Nov 2026 is a Sunday
        assertEquals(6, cells.takeWhile { it == null }.size)
        assertEquals(LocalDate.of(2026, 11, 1), cells[6])
        assertEquals(0, cells.size % 7)
        assertEquals(30, cells.count { it != null })
    }

    @Test
    fun aMonthStartingOnMondayHasNoLead() {
        val cells = MonthGrid.cells(YearMonth.of(2026, 6)) // 1 June 2026 is a Monday
        assertEquals(LocalDate.of(2026, 6, 1), cells.first())
        assertEquals(35, cells.size)
    }

    @Test
    fun februaryOfALeapYear() {
        assertEquals(29, MonthGrid.cells(YearMonth.of(2028, 2)).count { it != null })
    }
}

class SnippetTest {
    @Test
    fun shortTextIsUnchanged() {
        assertEquals("A short page.", Snippet.excerpt("A  short\npage.", "page"))
    }

    @Test
    fun longTextCentresOnTheMatch() {
        val text = "word ".repeat(80) + "the river was calm " + "more ".repeat(80)
        val out = Snippet.excerpt(text, "riv", max = 60)
        assertTrue(out, out.contains("river"))
        assertTrue(out.startsWith("…") && out.endsWith("…"))
        assertTrue(out.length <= 62)
    }

    @Test
    fun termsAreLowerCaseWords() {
        assertEquals(listOf("kind", "words"), Snippet.terms(" Kind, WORDS! "))
    }
}
