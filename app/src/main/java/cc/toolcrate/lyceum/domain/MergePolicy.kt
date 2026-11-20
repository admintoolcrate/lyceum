package cc.toolcrate.lyceum.domain

import java.time.LocalDate

/**
 * How an import or a restore meets what is already on the phone: day by day, the more recently
 * changed page wins, tags and all. Nothing already here is ever deleted.
 */
object MergePolicy {
    data class Plan(val write: List<Entry>, val added: Int, val updated: Int, val kept: Int)

    fun plan(existing: List<Entry>, incoming: List<Entry>): Plan {
        val have: Map<LocalDate, Entry> = existing.associateBy { it.date }
        // If a file holds the same day twice, its newest copy speaks for it.
        val newest = incoming.groupBy { it.date }.mapValues { (_, list) -> list.maxBy { it.updatedAt } }.values
        var added = 0
        var updated = 0
        var kept = 0
        val write = newest.filter { theirs ->
            val ours = have[theirs.date]
            when {
                ours == null -> { added++; true }
                theirs.updatedAt > ours.updatedAt && !theirs.sameContentAs(ours) -> { updated++; true }
                else -> { kept++; false }
            }
        }.sortedBy { it.date }
        return Plan(write, added, updated, kept)
    }

    private fun Entry.sameContentAs(other: Entry) =
        copy(createdAt = 0, updatedAt = 0) == other.copy(createdAt = 0, updatedAt = 0)
}
