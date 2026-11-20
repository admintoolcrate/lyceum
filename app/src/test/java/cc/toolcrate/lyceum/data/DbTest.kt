package cc.toolcrate.lyceum.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import cc.toolcrate.lyceum.data.db.LyceumDatabase
import org.junit.After
import org.junit.Before

/** An in-memory database, fresh for each test. */
abstract class DbTest {
    protected lateinit var db: LyceumDatabase

    @Before
    fun openDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), LyceumDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() = db.close()
}
