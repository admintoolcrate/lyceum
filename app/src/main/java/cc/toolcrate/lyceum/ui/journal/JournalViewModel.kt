package cc.toolcrate.lyceum.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.domain.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class JournalState(val month: YearMonth, val today: LocalDate, val entries: List<Entry> = emptyList()) {
    val written: Set<LocalDate> get() = entries.mapTo(HashSet()) { it.date }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class JournalViewModel @Inject constructor(private val repo: EntryRepository, clock: Clock) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val month = MutableStateFlow(YearMonth.from(today))

    val state: StateFlow<JournalState> = month.flatMapLatest { m ->
        repo.observeRange(m.atDay(1), m.atEndOfMonth()).map { JournalState(m, today, it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JournalState(YearMonth.from(today), today))

    fun shift(months: Long) = month.update { it.plusMonths(months) }
    fun thisMonth() = month.update { YearMonth.from(today) }
}
