package cc.toolcrate.lyceum.domain

import java.time.LocalDate

/** How the day felt, in words. Never a score, never charted. */
enum class Mood(val value: Int, val label: String) {
    Heavy(1, "Heavy"),
    Low(2, "Low"),
    Even(3, "Even"),
    Good(4, "Good"),
    Bright(5, "Bright");

    companion object {
        fun of(value: Int?): Mood? = entries.firstOrNull { it.value == value }
    }
}

/** A reference to a goal kept in the Stagira app. Only its id and a copy of its title are stored. */
data class GoalRef(val id: String, val title: String)

/** A journal page as the rest of the app sees it. */
data class Entry(
    val date: LocalDate,
    val prompt: String,
    val body: String,
    val mood: Mood? = null,
    val learned: String? = null,
    val tags: List<String> = emptyList(),
    val goal: GoalRef? = null,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

data class Prompt(val id: Long, val text: String, val enabled: Boolean, val builtIn: Boolean)
