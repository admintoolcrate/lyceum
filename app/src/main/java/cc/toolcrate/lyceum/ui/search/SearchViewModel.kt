package cc.toolcrate.lyceum.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.domain.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class SearchResults(val query: String, val tag: String?, val entries: List<Entry>, val searched: Boolean)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(private val repo: EntryRepository, clock: Clock) : ViewModel() {
    val today: LocalDate = LocalDate.now(clock)
    val query = MutableStateFlow("")
    val tag = MutableStateFlow<String?>(null)

    val tags: StateFlow<List<String>> = repo.observeTags().map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val results: StateFlow<SearchResults> = combine(query.debounce(200), tag) { q, t -> q to t }
        .flatMapLatest { (q, t) ->
            repo.search(q, t).map { SearchResults(q, t, it, searched = q.isNotBlank() || t != null) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SearchResults("", null, emptyList(), false))

    fun setQuery(text: String) { query.value = text }
    fun toggleTag(name: String) { tag.value = if (tag.value == name) null else name }
}
