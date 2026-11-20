package cc.toolcrate.lyceum.backup

import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * The journal as a plain JSON file: readable, and complete enough to rebuild the app from.
 *
 * Version 1:
 * ```
 * { "format": "lyceum", "version": 1, "exportedAt": <epoch ms>,
 *   "entries": [ { "date": "2026-03-10", "prompt": "…", "body": "…", "mood": "Good", "learned": "…",
 *                  "tags": ["walk"], "goal": { "id": "…", "title": "…" }, "createdAt": 0, "updatedAt": 0 } ],
 *   "prompts": [ { "text": "…", "enabled": true, "builtIn": true } ] }
 * ```
 * Newer app versions must keep reading every older version.
 */
object JournalCodec {
    const val FORMAT = "lyceum"
    const val VERSION = 1

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Serializable
    data class Document(
        val format: String = FORMAT,
        val version: Int = VERSION,
        val exportedAt: Long = 0,
        val entries: List<EntryJson> = emptyList(),
        val prompts: List<PromptJson> = emptyList(),
    )

    @Serializable
    data class EntryJson(
        val date: String,
        val prompt: String = "",
        val body: String = "",
        val mood: String? = null,
        val learned: String? = null,
        val tags: List<String> = emptyList(),
        val goal: GoalJson? = null,
        val createdAt: Long = 0,
        val updatedAt: Long = 0,
    )

    @Serializable
    data class GoalJson(val id: String, val title: String = "")

    @Serializable
    data class PromptJson(val text: String, val enabled: Boolean = true, @SerialName("builtIn") val builtIn: Boolean = false)

    data class Journal(val entries: List<Entry>, val prompts: List<PromptJson>)

    class ImportException(message: String) : Exception(message)

    fun encode(entries: List<Entry>, prompts: List<PromptJson>, exportedAt: Long): String =
        json.encodeToString(Document.serializer(), Document(exportedAt = exportedAt, entries = entries.map { it.toJson() }, prompts = prompts))

    fun decode(text: String): Journal {
        val doc = try {
            json.decodeFromString(Document.serializer(), text)
        } catch (e: Exception) {
            throw ImportException("This file isn't a Lyceum export.")
        }
        if (doc.format != FORMAT) throw ImportException("This file isn't a Lyceum export.")
        if (doc.version > VERSION) throw ImportException("This export comes from a newer Lyceum. Update the app, then import it.")
        val entries = doc.entries.mapNotNull { it.toEntry() }
        return Journal(entries, doc.prompts.filter { it.text.isNotBlank() })
    }

    private fun Entry.toJson() = EntryJson(
        date = date.toString(), prompt = prompt, body = body, mood = mood?.name, learned = learned, tags = tags,
        goal = goal?.let { GoalJson(it.id, it.title) }, createdAt = createdAt, updatedAt = updatedAt,
    )

    /** A row with an unreadable date is skipped rather than failing the whole import. */
    private fun EntryJson.toEntry(): Entry? {
        val day = runCatching { LocalDate.parse(date) }.getOrNull() ?: return null
        return Entry(
            date = day, prompt = prompt, body = body,
            mood = mood?.let { name -> Mood.entries.firstOrNull { it.name.equals(name, ignoreCase = true) } },
            learned = learned, tags = tags, goal = goal?.let { GoalRef(it.id, it.title) },
            createdAt = createdAt, updatedAt = updatedAt,
        )
    }
}
