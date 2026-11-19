package cc.toolcrate.lyceum.domain

import cc.toolcrate.lyceum.quotes.Quotes
import cc.toolcrate.lyceum.quotes.Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class QuoteChooserTest {
    private val day = LocalDate.of(2026, 4, 2)
    private fun entry(body: String = "An ordinary day.", mood: Mood? = null, learned: String? = null) =
        Entry(date = day, prompt = "", body = body, mood = mood, learned = learned)

    @Test
    fun heavyDaysGetEndurance() {
        assertEquals(Theme.Endurance, QuoteChooser.themeFor(entry(mood = Mood.Heavy, body = "I enjoyed lunch")))
        assertEquals(Theme.Endurance, QuoteChooser.themeFor(entry(mood = Mood.Low)))
    }

    @Test
    fun wordsInThePageChooseTheTheme() {
        assertEquals(Theme.Balance, QuoteChooser.themeFor(entry(body = "I slipped on the diet")))
        assertEquals(Theme.Balance, QuoteChooser.themeFor(entry(body = "Ate too much at dinner")))
        assertEquals(Theme.Joy, QuoteChooser.themeFor(entry(body = "Laughed with my sister", mood = Mood.Even)))
        assertEquals(Theme.Choice, QuoteChooser.themeFor(entry(body = "I said no to the extra shift")))
    }

    @Test
    fun wholeWordsOnly() {
        // "hardware" must not count as "hard".
        assertEquals(Theme.Habit, QuoteChooser.themeFor(entry(body = "Bought hardware")))
    }

    @Test
    fun fallbacksFollowLearnedThenMood() {
        assertEquals(Theme.Practice, QuoteChooser.themeFor(entry(learned = "Patience pays")))
        assertEquals(Theme.Joy, QuoteChooser.themeFor(entry(mood = Mood.Bright)))
        assertEquals(Theme.Habit, QuoteChooser.themeFor(entry()))
    }

    @Test
    fun sameDaySameQuoteAndAlwaysFromTheTheme() {
        val e = entry(mood = Mood.Low)
        assertEquals(QuoteChooser.choose(e), QuoteChooser.choose(e))
        assertEquals(Theme.Endurance, QuoteChooser.choose(e).theme)
    }

    @Test
    fun everyThemeHasPassagesAndTheyAreShort() {
        Theme.entries.forEach { assertTrue("$it is empty", Quotes.ofTheme(it).isNotEmpty()) }
        Quotes.all.forEach { assertTrue(it.ref, it.text.length <= 240) }
    }
}
