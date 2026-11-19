package cc.toolcrate.lyceum.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import cc.toolcrate.lyceum.data.db.LyceumDatabase
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Every schema version Lyceum has shipped, built from `app/schemas/`. When the schema changes:
 * bump the version, add a `Migration`, and add a test here that writes rows in the old version,
 * runs `helper.runMigrationsAndValidate(NAME, new, true, MIGRATION)`, and reads them back.
 */
@RunWith(RobolectricTestRunner::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), LyceumDatabase::class.java)

    @Test
    fun version1MatchesItsExportedSchemaAndHoldsAPage() {
        helper.createDatabase(NAME, 1).use { db ->
            db.execSQL(
                "INSERT INTO entries (date, prompt, body, mood, learned, goalId, goalTitle, createdAt, updatedAt) " +
                    "VALUES (20000, 'p', 'A page', 3, NULL, NULL, NULL, 1, 2)",
            )
            db.query("SELECT body FROM entries WHERE date = 20000").use { c ->
                c.moveToFirst()
                assertEquals("A page", c.getString(0))
            }
        }
        // Reopening validates the database against version 1's exported schema.
        helper.runMigrationsAndValidate(NAME, 1, true).close()
    }

    private companion object {
        const val NAME = "migration-test.db"
    }
}
