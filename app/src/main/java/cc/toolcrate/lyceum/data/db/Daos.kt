package cc.toolcrate.lyceum.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Transaction
    @Query("SELECT * FROM entries WHERE date = :day")
    fun observeByDate(day: Long): Flow<EntryWithTags?>

    @Transaction
    @Query("SELECT * FROM entries WHERE date = :day")
    suspend fun getByDate(day: Long): EntryWithTags?

    @Transaction
    @Query("SELECT * FROM entries WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeRange(from: Long, to: Long): Flow<List<EntryWithTags>>

    @Query("SELECT date FROM entries WHERE date BETWEEN :from AND :to ORDER BY date ASC")
    fun observeDatesInRange(from: Long, to: Long): Flow<List<Long>>

    @Transaction
    @Query(
        """
        SELECT * FROM entries
        WHERE (id IN (SELECT rowid FROM entries_fts WHERE entries_fts MATCH :match)
               OR id IN (SELECT entryId FROM entry_tags JOIN tags ON tags.id = entry_tags.tagId
                         WHERE tags.name LIKE :tagPrefix || '%' ESCAPE '\'))
          AND (:tag IS NULL OR id IN (
                SELECT entryId FROM entry_tags JOIN tags ON tags.id = entry_tags.tagId WHERE tags.name = :tag))
        ORDER BY date DESC
        """
    )
    fun search(match: String, tagPrefix: String, tag: String?): Flow<List<EntryWithTags>>

    @Transaction
    @Query(
        """
        SELECT * FROM entries WHERE id IN (
            SELECT entryId FROM entry_tags JOIN tags ON tags.id = entry_tags.tagId WHERE tags.name = :tag)
        ORDER BY date DESC
        """
    )
    fun withTag(tag: String): Flow<List<EntryWithTags>>

    @Transaction
    @Query("SELECT * FROM entries ORDER BY date ASC")
    suspend fun all(): List<EntryWithTags>

    @Query("SELECT COUNT(*) FROM entries WHERE date = :day")
    suspend fun countOn(day: Long): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entry: EntryEntity): Long

    @Update
    suspend fun update(entry: EntryEntity)

    @Query("DELETE FROM entries WHERE date = :day")
    suspend fun deleteByDate(day: Long)
}

@Dao
interface TagDao {
    @Query(
        """
        SELECT tags.name AS name, COUNT(entry_tags.entryId) AS uses FROM tags
        LEFT JOIN entry_tags ON entry_tags.tagId = tags.id
        GROUP BY tags.id ORDER BY uses DESC, tags.name ASC
        """
    )
    fun observeUses(): Flow<List<TagUse>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name")
    suspend fun byName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun link(ref: EntryTagCrossRef)

    @Query("DELETE FROM entry_tags WHERE entryId = :entryId")
    suspend fun unlinkAll(entryId: Long)

    @Query("DELETE FROM tags WHERE id NOT IN (SELECT tagId FROM entry_tags)")
    suspend fun deleteUnused()
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY position ASC, id ASC")
    fun observeAll(): Flow<List<PromptEntity>>

    @Query("SELECT * FROM prompts ORDER BY position ASC, id ASC")
    suspend fun all(): List<PromptEntity>

    @Query("SELECT COUNT(*) FROM prompts")
    suspend fun count(): Int

    @Query("SELECT COALESCE(MAX(position), -1) FROM prompts")
    suspend fun maxPosition(): Int

    @Insert
    suspend fun insert(prompt: PromptEntity): Long

    @Insert
    suspend fun insertAll(prompts: List<PromptEntity>)

    @Update
    suspend fun update(prompt: PromptEntity)

    @Update
    suspend fun updateAll(prompts: List<PromptEntity>)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM prompts WHERE builtIn = 1")
    suspend fun deleteBuiltIn()
}
