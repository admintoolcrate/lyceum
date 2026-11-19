package cc.toolcrate.lyceum.ui.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.data.settings.Settings
import cc.toolcrate.lyceum.data.settings.SettingsStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** App-wide state the activity needs: the settings (theme, lock). */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsStore: SettingsStore,
    prompts: PromptRepository,
) : ViewModel() {
    val settings: StateFlow<Settings?> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch { prompts.ensureSeeded() }
    }
}
