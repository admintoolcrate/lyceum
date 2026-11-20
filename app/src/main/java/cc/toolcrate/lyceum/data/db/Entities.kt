package cc.toolcrate.lyceum.data.db

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation

/** One page of the journal. There is at most one per calendar day. */
@Entity(tableName = "entries", indices = [Index(value = ["date"], unique = true)])
data class EntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** The day this page belongs to, as [java.time.LocalDate.toEpochDay]. */
    val date: Long,
    /** The prompt as it read when the page was written. */
    val prompt: String,
    val body: String,
    /** 1 (Heavy) to 5 (Bright), or null when not given. */
    val mood: Int? = null,
    /** The optional one line: "one thing I learned". */
    val learned: String? = null,
    /** An optional reference to a goal in the Stagira app. */
    val goalId: String? = null,
    val goalTitle: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
)

/** Full-text index over the words of each page. Room keeps it in step with [EntryEntity]. */
@Fts4(contentEntity = EntryEntity::class)
@Entity(tableName = "entries_fts")
data class EntryFts(
    val body: String,
    val learned: String?,
)

@Entity(tableName = "tags", indices = [Index(value = ["name"], unique = true)])
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val name: String,
)

@Entity(
    tableName = "entry_tags",
    primaryKeys = ["entryId", "tagId"],
    indices = [Index("tagId")],
    foreignKeys = [
        ForeignKey(EntryEntity::class, ["id"], ["entryId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(TagEntity::class, ["id"], ["tagId"], onDelete = ForeignKey.CASCADE),
    ],
)
data class EntryTagCrossRef(val entryId: Long, val tagId: Long)

@Entity(tableName = "prompts")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val position: Int,
    val enabled: Boolean = true,
    /** Shipped with the app; can be disabled or edited, and restored. */
    val builtIn: Boolean = false,
)

data class EntryWithTags(
    @Embedded val entry: EntryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(EntryTagCrossRef::class, parentColumn = "entryId", entityColumn = "tagId"),
    )
    val tags: List<TagEntity>,
)

data class TagUse(val name: String, val uses: Int)
