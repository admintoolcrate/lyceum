package cc.toolcrate.lyceum.ui.prompts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.Prompt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PromptsViewModel @Inject constructor(private val repo: PromptRepository) : ViewModel() {
    val prompts: StateFlow<List<Prompt>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(text: String) = launch { repo.add(text) }
    fun edit(id: Long, text: String) = launch { repo.edit(id, text) }
    fun setEnabled(id: Long, enabled: Boolean) = launch { repo.setEnabled(id, enabled) }
    fun delete(id: Long) = launch { repo.delete(id) }
    fun restoreDefaults() = launch { repo.restoreDefaults() }

    /** Moves the prompt at [index] one place up (-1) or down (+1). */
    fun move(index: Int, by: Int) {
        val ids = prompts.value.map { it.id }.toMutableList()
        val to = index + by
        if (index !in ids.indices || to !in ids.indices) return
        ids.add(to, ids.removeAt(index))
        launch { repo.reorder(ids) }
    }

    private fun launch(block: suspend () -> Unit) { viewModelScope.launch { block() } }
}
