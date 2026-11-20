package cc.toolcrate.lyceum.ui

import android.os.Looper
import androidx.lifecycle.SavedStateHandle
import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.DbTest
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.stagira.PendingGoal
import cc.toolcrate.lyceum.ui.editor.EditorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import java.time.Instant
import java.time.ZoneOffset

/** A half-written page survives Android stopping the app in the background. */
@RunWith(RobolectricTestRunner::class)
class EditorDraftTest : DbTest() {
    private val clock = TestClock(Instant.parse("2026-11-19T20:00:00Z"), ZoneOffset.UTC)

    private fun editor(saved: SavedStateHandle) =
        EditorViewModel(saved, EntryRepository(db, clock), PromptRepository(db), PendingGoal(), clock)

    private fun settle(until: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!until()) {
            check(System.currentTimeMillis() < deadline) { "timed out" }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun anUnsavedDraftComesBackAfterTheProcessIsRecreated() {
        val saved = SavedStateHandle()
        val first = editor(saved)
        settle { first.state.value.loaded }
        first.setBody("Half a thought")
        first.toggleMood(Mood.Low)
        first.setLearned("Rest")
        first.addTag("night")
        first.setGoal(GoalRef("g1", "Sleep earlier"))
        settle { saved.get<String>("draft_body") == "Half a thought" && saved.get<String>("draft_goal_id") != null }

        // Android keeps only the saved state; the view model is built anew.
        val second = editor(SavedStateHandle(saved.keys().associateWith { saved.get<Any>(it) }))
        settle { second.state.value.loaded }
        val s = second.state.value
        assertEquals("Half a thought", s.body)
        assertEquals(Mood.Low, s.mood)
        assertEquals("Rest", s.learned)
        assertEquals(listOf("night"), s.tags)
        assertEquals(GoalRef("g1", "Sleep earlier"), s.goal)
        assertTrue(s.dirty)
    }

    @Test
    fun savingClearsTheDraft() {
        val saved = SavedStateHandle()
        val vm = editor(saved)
        settle { vm.state.value.loaded }
        vm.setBody("Done for today")
        settle { saved.get<String>("draft_body") != null }
        vm.save()
        settle { vm.state.value.quote != null }
        settle { saved.get<String>("draft_body") == null }
        assertNull(saved.get<String>("draft_body"))
        assertFalse(vm.state.value.dirty)
    }
}
