package cc.toolcrate.lyceum.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [EntryEntity::class, EntryFts::class, TagEntity::class, EntryTagCrossRef::class, PromptEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class LyceumDatabase : RoomDatabase() {
    abstract fun entries(): EntryDao
    abstract fun tags(): TagDao
    abstract fun prompts(): PromptDao

    companion object {
        const val NAME = "lyceum.db"
    }
}
