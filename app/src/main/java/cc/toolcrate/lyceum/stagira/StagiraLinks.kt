package cc.toolcrate.lyceum.stagira

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.settings.SettingsStore
import cc.toolcrate.lyceum.security.AppLock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Whether the user wants Stagira links, and keeps the lock from closing when Stagira hands back. */
@HiltViewModel
class StagiraLinks @Inject constructor(settings: SettingsStore, private val lock: AppLock) : ViewModel() {
    val enabled: StateFlow<Boolean> = settings.settings.map { it.stagiraLinkEnabled }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun expectReturn() = lock.expectReturn()
}
