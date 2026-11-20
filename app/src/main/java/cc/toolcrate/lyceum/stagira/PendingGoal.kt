package cc.toolcrate.lyceum.stagira

import cc.toolcrate.lyceum.domain.GoalRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** A goal Stagira handed to Lyceum, waiting for today's page to pick it up. Never saved on its own. */
@Singleton
class PendingGoal @Inject constructor() {
    private val _goal = MutableStateFlow<GoalRef?>(null)
    val goal: StateFlow<GoalRef?> = _goal.asStateFlow()

    fun offer(goal: GoalRef) { _goal.value = goal }
    fun consume() { _goal.value = null }
}
