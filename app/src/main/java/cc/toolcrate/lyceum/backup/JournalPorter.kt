package cc.toolcrate.lyceum.backup

import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.MergePolicy
import kotlinx.coroutines.flow.first
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

data class ImportSummary(val added: Int, val updated: Int, val kept: Int) {
    val message: String get() = when {
        added == 0 && updated == 0 -> "Everything in the file was already here."
        else -> buildList {
            if (added > 0) add("$added ${if (added == 1) "page" else "pages"} added")
            if (updated > 0) add("$updated updated")
            if (kept > 0) add("$kept already here")
        }.joinToString(", ") + "."
    }
}

/** Turns the whole journal into JSON and back, merging rather than replacing. */
@Singleton
class JournalPorter @Inject constructor(
    private val entries: EntryRepository,
    private val prompts: PromptRepository,
    private val clock: Clock,
) {
    suspend fun exportJson(): String {
        val promptRows = prompts.observeAll().first().map { JournalCodec.PromptJson(it.text, it.enabled, it.builtIn) }
        return JournalCodec.encode(entries.all(), promptRows, clock.millis())
    }

    /** Throws [JournalCodec.ImportException] for a file that isn't a readable Lyceum export. */
    suspend fun importJson(text: String): ImportSummary {
        val journal = JournalCodec.decode(text)
        val plan = MergePolicy.plan(entries.all(), journal.entries)
        plan.write.forEach { entries.put(it) }
        prompts.merge(journal.prompts.filter { !it.builtIn }.map { it.text })
        return ImportSummary(plan.added, plan.updated, plan.kept)
    }
}
