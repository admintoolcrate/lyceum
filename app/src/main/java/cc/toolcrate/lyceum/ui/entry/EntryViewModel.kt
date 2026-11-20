package cc.toolcrate.lyceum.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.domain.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class EntryState(
    val date: LocalDate,
    val today: LocalDate,
    val entry: Entry? = null,
    val loaded: Boolean = false,
    /** The page just deleted, kept so it can be put back. */
    val deleted: Entry? = null,
)

@HiltViewModel
class EntryViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repo: EntryRepository,
    clock: Clock,
) : ViewModel() {
    private val date = LocalDate.parse(checkNotNull(savedState.get<String>("date")))
    private val today = LocalDate.now(clock)
    private val deleted = MutableStateFlow<Entry?>(null)

    val state: StateFlow<EntryState> = combine(repo.observe(date), deleted) { entry, gone ->
        EntryState(date, today, entry, loaded = true, deleted = gone)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryState(date, today))

    fun delete() {
        val entry = state.value.entry ?: return
        deleted.value = entry
        viewModelScope.launch { repo.delete(date) }
    }

    fun undoDelete() {
        val entry = deleted.value ?: return
        viewModelScope.launch {
            repo.put(entry)
            deleted.value = null
        }
    }
}
