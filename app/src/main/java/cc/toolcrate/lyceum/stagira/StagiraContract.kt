package cc.toolcrate.lyceum.stagira

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContract
import cc.toolcrate.lyceum.domain.GoalRef

/**
 * How Lyceum and Stagira talk: plain Android intents on the phone, no server, no shared storage.
 * Lyceum keeps only a goal's id and a copy of its title. Documented in docs/stagira-intent.md.
 */
object StagiraContract {
    const val PACKAGE = "cc.toolcrate.stagira"

    /** Lyceum → Stagira: let the user choose a goal. Result extras: [EXTRA_GOAL_ID], [EXTRA_GOAL_TITLE]. */
    const val ACTION_PICK_GOAL = "cc.toolcrate.stagira.action.PICK_GOAL"

    /** Lyceum → Stagira: show the goal with [EXTRA_GOAL_ID]. */
    const val ACTION_VIEW_GOAL = "cc.toolcrate.stagira.action.VIEW_GOAL"

    /** Stagira → Lyceum: open today's page with this goal linked (optional extras below). Nothing is saved unseen. */
    const val ACTION_NEW_ENTRY = "cc.toolcrate.lyceum.action.NEW_ENTRY"

    const val EXTRA_GOAL_ID = "goal_id"
    const val EXTRA_GOAL_TITLE = "goal_title"

    const val MAX_ID = 128
    const val MAX_TITLE = 120

    /** Checks what another app sent: a usable id, a tidy title. Anything else is ignored. */
    fun parseGoal(id: String?, title: String?): GoalRef? {
        val cleanId = id?.trim()?.takeIf { it.isNotEmpty() && it.length <= MAX_ID && it.none(Char::isISOControl) } ?: return null
        val cleanTitle = title.orEmpty().filterNot(Char::isISOControl).trim().take(MAX_TITLE)
        return GoalRef(cleanId, cleanTitle)
    }

    fun goalFrom(intent: Intent?): GoalRef? =
        intent?.let { parseGoal(it.getStringExtra(EXTRA_GOAL_ID), it.getStringExtra(EXTRA_GOAL_TITLE)) }

    fun canPick(context: Context): Boolean = resolves(context, Intent(ACTION_PICK_GOAL).setPackage(PACKAGE))

    fun canView(context: Context): Boolean = resolves(context, Intent(ACTION_VIEW_GOAL).setPackage(PACKAGE))

    /** Opens [goal] in Stagira; false if Stagira isn't there to open it. */
    fun view(context: Context, goal: GoalRef): Boolean {
        if (!canView(context)) return false
        return try {
            context.startActivity(Intent(ACTION_VIEW_GOAL).setPackage(PACKAGE).putExtra(EXTRA_GOAL_ID, goal.id))
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun resolves(context: Context, intent: Intent): Boolean =
        context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).isNotEmpty()

    /** Asks Stagira for a goal; null if the user backs out. */
    class PickGoal : ActivityResultContract<Unit, GoalRef?>() {
        override fun createIntent(context: Context, input: Unit) = Intent(ACTION_PICK_GOAL).setPackage(PACKAGE)
        override fun parseResult(resultCode: Int, intent: Intent?): GoalRef? =
            if (resultCode == Activity.RESULT_OK) goalFrom(intent) else null
    }
}
