package cc.toolcrate.lyceum.data.repo

import androidx.room.withTransaction
import cc.toolcrate.lyceum.data.db.LyceumDatabase
import cc.toolcrate.lyceum.data.db.PromptEntity
import cc.toolcrate.lyceum.data.db.SeedPrompts
import cc.toolcrate.lyceum.domain.Prompt
import cc.toolcrate.lyceum.domain.PromptRotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepository @Inject constructor(private val db: LyceumDatabase) {
    private val dao = db.prompts()

    fun observeAll(): Flow<List<Prompt>> = dao.observeAll().map { list -> list.map { it.toPrompt() } }

    fun observeToday(date: LocalDate): Flow<String> = dao.observeAll().map { list ->
        PromptRotation.promptFor(date, list.filter { it.enabled }.map { it.text })
    }

    /** Fills in the built-in prompts the first time the app runs. */
    suspend fun ensureSeeded() {
        db.withTransaction {
            if (dao.count() == 0) dao.insertAll(seed(startAt = 0))
        }
    }

    suspend fun add(text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        db.withTransaction { dao.insert(PromptEntity(text = clean, position = dao.maxPosition() + 1)) }
    }

    suspend fun edit(id: Long, text: String) {
        val clean = text.trim()
        if (clean.isEmpty()) return
        dao.all().firstOrNull { it.id == id }?.let { dao.update(it.copy(text = clean)) }
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.all().firstOrNull { it.id == id }?.let { dao.update(it.copy(enabled = enabled)) }
    }

    suspend fun delete(id: Long) = dao.delete(id)

    /** Puts the prompts in the order of [ids]; any not listed keep their relative order after them. */
    suspend fun reorder(ids: List<Long>) {
        db.withTransaction {
            val all = dao.all()
            val ordered = ids.mapNotNull { id -> all.firstOrNull { it.id == id } } + all.filter { it.id !in ids }
            dao.updateAll(ordered.mapIndexed { i, p -> p.copy(position = i) })
        }
    }

    /** Brings back the shipped prompts as they were, keeping the user's own after them. */
    suspend fun restoreDefaults() {
        db.withTransaction {
            dao.deleteBuiltIn()
            val own = dao.all()
            dao.insertAll(seed(startAt = 0))
            dao.updateAll(own.mapIndexed { i, p -> p.copy(position = SeedPrompts.all.size + i) })
        }
    }

    /** Adds prompts from an import, skipping any whose text is already present. */
    suspend fun merge(texts: List<String>) {
        db.withTransaction {
            val have = dao.all().map { it.text.trim().lowercase() }.toHashSet()
            var position = dao.maxPosition()
            for (text in texts.map { it.trim() }.filter { it.isNotEmpty() }) {
                if (have.add(text.lowercase())) dao.insert(PromptEntity(text = text, position = ++position))
            }
        }
    }

    private fun seed(startAt: Int) = SeedPrompts.all.mapIndexed { i, text ->
        PromptEntity(text = text, position = startAt + i, builtIn = true)
    }
}

private fun PromptEntity.toPrompt() = Prompt(id, text, enabled, builtIn)
