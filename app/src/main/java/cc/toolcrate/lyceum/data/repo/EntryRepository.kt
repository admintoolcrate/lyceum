package cc.toolcrate.lyceum.data.repo

import androidx.room.withTransaction
import cc.toolcrate.lyceum.data.db.EntryEntity
import cc.toolcrate.lyceum.data.db.EntryTagCrossRef
import cc.toolcrate.lyceum.data.db.EntryWithTags
import cc.toolcrate.lyceum.data.db.LyceumDatabase
import cc.toolcrate.lyceum.data.db.TagEntity
import cc.toolcrate.lyceum.data.db.TagUse
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.domain.SearchQuery
import cc.toolcrate.lyceum.domain.Tags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val db: LyceumDatabase,
    private val clock: Clock,
) {
    private val entries = db.entries()
    private val tags = db.tags()

    fun observe(date: LocalDate): Flow<Entry?> =
        entries.observeByDate(date.toEpochDay()).map { it?.toEntry() }

    suspend fun get(date: LocalDate): Entry? = entries.getByDate(date.toEpochDay())?.toEntry()

    suspend fun hasEntry(date: LocalDate): Boolean = entries.countOn(date.toEpochDay()) > 0

    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<Entry>> =
        entries.observeRange(from.toEpochDay(), to.toEpochDay()).map { list -> list.map { it.toEntry() } }

    fun observeDates(from: LocalDate, to: LocalDate): Flow<Set<LocalDate>> =
        entries.observeDatesInRange(from.toEpochDay(), to.toEpochDay())
            .map { days -> days.mapTo(HashSet()) { LocalDate.ofEpochDay(it) } }

    /** Entries matching the words in [text], optionally limited to one [tag]; newest first. */
    fun search(text: String, tag: String?): Flow<List<Entry>> {
        val match = SearchQuery.toMatch(text)
        val source = when {
            match != null -> entries.search(match, tag)
            tag != null -> entries.withTag(tag)
            else -> return flowOf(emptyList())
        }
        return source.map { list -> list.map { it.toEntry() } }
    }

    fun observeTags(): Flow<List<TagUse>> = tags.observeUses()

    suspend fun all(): List<Entry> = entries.all().map { it.toEntry() }

    /**
     * Writes the page for [entry]'s date, creating or replacing it, and sets its tags.
     * Returns the saved entry with its timestamps.
     */
    suspend fun save(entry: Entry): Entry {
        val now = clock.millis()
        return db.withTransaction {
            val existing = entries.getByDate(entry.date.toEpochDay())?.entry
            val row = entry.toEntity(
                id = existing?.id ?: 0,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
            val id = if (existing == null) entries.insert(row) else row.id.also { entries.update(row) }
            setTags(id, entry.tags)
            entries.getByDate(entry.date.toEpochDay())!!.toEntry()
        }
    }

    /**
     * Writes [entry] exactly as given, keeping its own timestamps. Used by import and restore,
     * which have already decided this version should win.
     */
    suspend fun put(entry: Entry) {
        db.withTransaction {
            val existing = entries.getByDate(entry.date.toEpochDay())?.entry
            val row = entry.toEntity(existing?.id ?: 0, entry.createdAt, entry.updatedAt)
            val id = if (existing == null) entries.insert(row) else row.id.also { entries.update(row) }
            setTags(id, entry.tags)
        }
    }

    suspend fun delete(date: LocalDate) {
        db.withTransaction {
            entries.deleteByDate(date.toEpochDay())
            tags.deleteUnused()
        }
    }

    private suspend fun setTags(entryId: Long, names: List<String>) {
        tags.unlinkAll(entryId)
        for (name in Tags.normalize(names)) {
            val tagId = tags.byName(name)?.id ?: tags.insert(TagEntity(name = name))
            tags.link(EntryTagCrossRef(entryId, tagId))
        }
        tags.deleteUnused()
    }
}

internal fun EntryWithTags.toEntry() = Entry(
    date = LocalDate.ofEpochDay(entry.date),
    prompt = entry.prompt,
    body = entry.body,
    mood = Mood.of(entry.mood),
    learned = entry.learned,
    tags = tags.map { it.name }.sortedBy { it.lowercase() },
    goal = if (entry.goalId != null) GoalRef(entry.goalId, entry.goalTitle.orEmpty()) else null,
    createdAt = entry.createdAt,
    updatedAt = entry.updatedAt,
)

private fun Entry.toEntity(id: Long, createdAt: Long, updatedAt: Long) = EntryEntity(
    id = id,
    date = date.toEpochDay(),
    prompt = prompt,
    body = body,
    mood = mood?.value,
    learned = learned?.trim()?.ifEmpty { null },
    goalId = goal?.id,
    goalTitle = goal?.title,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
