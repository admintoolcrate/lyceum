package cc.toolcrate.lyceum.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.security.AppLock
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
) : ViewModel() {
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
