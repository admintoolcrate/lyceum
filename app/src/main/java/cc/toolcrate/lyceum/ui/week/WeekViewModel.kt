package cc.toolcrate.lyceum.ui.week

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.domain.WeekRange
import cc.toolcrate.lyceum.domain.WeekReview
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WeekViewModel @Inject constructor(private val repo: EntryRepository, clock: Clock) : ViewModel() {
    val today: LocalDate = LocalDate.now(clock)
    private val thisWeek = WeekRange.of(today)
    private val week = MutableStateFlow(thisWeek)

    val review: StateFlow<WeekReview> = week.flatMapLatest { w ->
        repo.observeRange(w.start, w.end).map { WeekReview.of(w, it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeekReview.of(thisWeek, emptyList()))

    fun shift(weeks: Long) = week.update { it.shift(weeks) }
    fun isThisWeek(w: WeekRange) = w == thisWeek
}
