package cc.toolcrate.lyceum.domain

import cc.toolcrate.lyceum.quotes.Quote
import cc.toolcrate.lyceum.quotes.Quotes
import cc.toolcrate.lyceum.quotes.Theme

/**
 * Chooses the passage shown after a page is saved. The theme follows what the page holds; the pick
 * within a theme follows the date, so reopening the same day shows the same passage.
 */
object QuoteChooser {
    private val keywords: List<Pair<Theme, List<String>>> = listOf(
        Theme.Balance to listOf("slipped", "slip", "too much", "too little", "overdid", "balance", "excess", "mistake"),
        Theme.Endurance to listOf("hard", "tired", "grief", "loss", "sick", "ill", "difficult", "anxious", "worried", "struggle"),
        Theme.Choice to listOf("decide", "decided", "choice", "chose", "choose", "refused", "said no"),
        Theme.Practice to listOf("practice", "practise", "learned", "learning", "studied", "training", "lesson"),
        Theme.Joy to listOf("enjoy", "enjoyed", "happy", "glad", "grateful", "delight", "laughed", "beautiful"),
        Theme.Habit to listOf("habit", "routine", "again", "every day", "daily"),
    )

    fun themeFor(entry: Entry): Theme {
        when (entry.mood) {
            Mood.Heavy, Mood.Low -> return Theme.Endurance
            else -> Unit
        }
        val text = (entry.body + " " + entry.learned.orEmpty() + " " + entry.tags.joinToString(" ")).lowercase()
        val words = text.split(Regex("[^\\p{L}']+")).toSet()
        val hit = keywords.firstOrNull { (_, keys) ->
            keys.any { key -> if (' ' in key) key in text else key in words }
        }
        if (hit != null) return hit.first
        return when {
            !entry.learned.isNullOrBlank() -> Theme.Practice
            entry.mood == Mood.Good || entry.mood == Mood.Bright -> Theme.Joy
            else -> Theme.Habit
        }
    }

    fun choose(entry: Entry): Quote {
        val pool = Quotes.ofTheme(themeFor(entry)).ifEmpty { Quotes.all }
        return pool[Math.floorMod(entry.date.toEpochDay(), pool.size.toLong()).toInt()]
    }
}
