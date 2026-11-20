package cc.toolcrate.lyceum.stagira

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import cc.toolcrate.lyceum.domain.GoalRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StagiraContractTest {
    @Test
    fun acceptsATidyGoal() {
        assertEquals(GoalRef("g-1", "Read daily"), StagiraContract.parseGoal(" g-1 ", "  Read daily "))
        assertEquals(GoalRef("g-1", ""), StagiraContract.parseGoal("g-1", null))
    }

    @Test
    fun ignoresWhatItCannotTrust() {
        assertNull(StagiraContract.parseGoal(null, "x"))
        assertNull(StagiraContract.parseGoal("   ", "x"))
        assertNull(StagiraContract.parseGoal("a".repeat(StagiraContract.MAX_ID + 1), "x"))
        assertNull(StagiraContract.parseGoal("a\nb", "x"))
        assertEquals("ab", StagiraContract.parseGoal("id", "a\u0000b")!!.title)
        assertEquals(StagiraContract.MAX_TITLE, StagiraContract.parseGoal("id", "t".repeat(500))!!.title.length)
    }

    @Test
    fun pickResultIsReadFromTheIntent() {
        val contract = StagiraContract.PickGoal()
        val data = Intent().putExtra(StagiraContract.EXTRA_GOAL_ID, "g-7").putExtra(StagiraContract.EXTRA_GOAL_TITLE, "Walk")
        assertEquals(GoalRef("g-7", "Walk"), contract.parseResult(Activity.RESULT_OK, data))
        assertNull(contract.parseResult(Activity.RESULT_CANCELED, data))
        assertNull(contract.parseResult(Activity.RESULT_OK, null))
    }

    @Test
    fun withoutStagiraInstalledTheLinkIsHidden() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        assertFalse(StagiraContract.canPick(context))
        assertFalse(StagiraContract.view(context, GoalRef("g", "t")))
    }
}
