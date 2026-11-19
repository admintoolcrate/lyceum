package cc.toolcrate.lyceum.domain

import java.time.LocalDate

/**
 * Picks the day's prompt from the enabled ones. The choice depends only on the date, so it stays
 * the same all day and the prompts take turns in the order the user set.
 */
object PromptRotation {
    const val FALLBACK = "What went well? What slipped? Why?"

    fun promptFor(date: LocalDate, enabled: List<String>): String {
        val usable = enabled.map { it.trim() }.filter { it.isNotEmpty() }
        if (usable.isEmpty()) return FALLBACK
        return usable[Math.floorMod(date.toEpochDay(), usable.size.toLong()).toInt()]
    }
}
