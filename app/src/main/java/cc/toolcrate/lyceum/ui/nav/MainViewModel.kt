package cc.toolcrate.lyceum.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.security.AppLock
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.stagira.PendingGoal
import kotlinx.coroutines.flow.MutableStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** App-wide state the activity needs: the settings (theme, lock) and whether the journal is locked. */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsStore: SettingsStore,
    prompts: PromptRepository,
    val lock: AppLock,
    private val pendingGoal: PendingGoal,
) : ViewModel() {
    /** Set when something outside (Stagira) asked for today's page. */
    val openToday = MutableStateFlow(false)

    fun offerGoal(goal: GoalRef?) {
        if (goal != null) pendingGoal.offer(goal)
        openToday.value = true
    }

    fun openedToday() { openToday.value = false }

    val settings: StateFlow<Settings?> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val locked: StateFlow<Boolean> = lock.locked

    init {
        viewModelScope.launch { prompts.ensureSeeded() }
        viewModelScope.launch { lock.start() }
    }

    fun onBackground() = lock.onBackground()
    fun onForeground() = viewModelScope.launch { lock.onForeground() }
}
