package cc.toolcrate.lyceum.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.domain.QuoteChooser
import cc.toolcrate.lyceum.domain.Tags
import cc.toolcrate.lyceum.quotes.Quote
import cc.toolcrate.lyceum.stagira.PendingGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class EditorState(
    val date: LocalDate,
    val today: LocalDate,
    val loaded: Boolean = false,
    val existing: Boolean = false,
    val prompt: String = "",
    val body: String = "",
    val mood: Mood? = null,
    val learned: String = "",
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val goal: GoalRef? = null,
    val dirty: Boolean = false,
    val saving: Boolean = false,
    /** Set after a save: the passage to show. */
    val quote: Quote? = null,
) {
    val canSave: Boolean get() = loaded && !saving && (body.isNotBlank() || learned.isNotBlank())
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val entries: EntryRepository,
    private val prompts: PromptRepository,
    private val pendingGoal: PendingGoal,
    clock: Clock,
) : ViewModel() {
    private val today = LocalDate.now(clock)
    private val date: LocalDate = savedState.get<String>("date")?.let(LocalDate::parse) ?: today

    private val _state = MutableStateFlow(EditorState(date = date, today = today))
    val state: StateFlow<EditorState> = _state.asStateFlow()

    /** Tags used before, most used first, for one-tap adding. */
    val knownTags: StateFlow<List<String>> = entries.observeTags()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val existing = entries.get(date)
            val prompt = existing?.prompt ?: prompts.observeToday(date).first()
            _state.update {
                val loaded = if (existing == null) it.copy(loaded = true, prompt = prompt)
                else it.copy(
                    loaded = true, existing = true, prompt = prompt, body = existing.body, mood = existing.mood,
                    learned = existing.learned.orEmpty(), tags = existing.tags, goal = existing.goal,
                )
                restoreDraft(loaded)
            }
            // From here on, unsaved changes are kept in the saved state, so a page half-written when
            // Android stops the app in the background is still there when it comes back.
            _state.collect { keepDraft(it) }
        }
        // A goal handed over by Stagira opens on today's page, ready to keep or remove.
        if (date == today) {
            viewModelScope.launch {
                pendingGoal.goal.collect { goal ->
                    if (goal != null) {
                        _state.update { it.copy(goal = goal, dirty = true) }
                        pendingGoal.consume()
                    }
                }
            }
        }
    }

    private fun keepDraft(s: EditorState) {
        if (!s.loaded) return
        if (!s.dirty) {
            DRAFT_KEYS.forEach { savedState.remove<Any>(it) }
            return
        }
        savedState[K_BODY] = s.body
        savedState[K_LEARNED] = s.learned
        savedState[K_MOOD] = s.mood?.value ?: 0
        savedState[K_TAGS] = ArrayList(s.tags)
        savedState[K_GOAL_ID] = s.goal?.id
        savedState[K_GOAL_TITLE] = s.goal?.title
    }

    private fun restoreDraft(s: EditorState): EditorState {
        val body = savedState.get<String>(K_BODY) ?: return s
        val goalId = savedState.get<String>(K_GOAL_ID)
        return s.copy(
            body = body,
            learned = savedState.get<String>(K_LEARNED).orEmpty(),
            mood = Mood.of(savedState.get<Int>(K_MOOD)),
            tags = savedState.get<ArrayList<String>>(K_TAGS).orEmpty(),
            goal = goalId?.let { GoalRef(it, savedState.get<String>(K_GOAL_TITLE).orEmpty()) },
            dirty = true,
        )
    }

    private companion object {
        const val K_BODY = "draft_body"
        const val K_LEARNED = "draft_learned"
        const val K_MOOD = "draft_mood"
        const val K_TAGS = "draft_tags"
        const val K_GOAL_ID = "draft_goal_id"
        const val K_GOAL_TITLE = "draft_goal_title"
        val DRAFT_KEYS = listOf(K_BODY, K_LEARNED, K_MOOD, K_TAGS, K_GOAL_ID, K_GOAL_TITLE)
    }

    fun setBody(text: String) = _state.update { it.copy(body = text, dirty = true) }
    fun setLearned(text: String) = _state.update { it.copy(learned = text.replace('\n', ' '), dirty = true) }
    fun toggleMood(mood: Mood) = _state.update { it.copy(mood = if (it.mood == mood) null else mood, dirty = true) }
    fun setTagInput(text: String) {
        // A comma or Enter finishes a tag.
        if (text.endsWith(",") || text.endsWith("\n")) addTag(text.dropLast(1)) else _state.update { it.copy(tagInput = text) }
    }
    fun addTag(raw: String = state.value.tagInput) = _state.update {
        val name = Tags.clean(raw)
        if (name == null) it.copy(tagInput = "")
        else it.copy(tags = Tags.normalize(it.tags + name), tagInput = "", dirty = true)
    }
    fun removeTag(name: String) = _state.update { it.copy(tags = it.tags - name, dirty = true) }
    fun setGoal(goal: GoalRef?) = _state.update { it.copy(goal = goal, dirty = true) }

    fun save() {
        val s = state.value
        if (!s.canSave) return
        val tags = if (s.tagInput.isNotBlank()) Tags.normalize(s.tags + s.tagInput) else s.tags
        _state.update { it.copy(saving = true, tags = tags, tagInput = "") }
        viewModelScope.launch {
            val saved = entries.save(
                Entry(
                    date = s.date, prompt = s.prompt, body = s.body.trimEnd(), mood = s.mood,
                    learned = s.learned.trim().ifEmpty { null }, tags = tags, goal = s.goal,
                ),
            )
            _state.update { it.copy(saving = false, existing = true, dirty = false, quote = QuoteChooser.choose(saved)) }
        }
    }

    fun dismissQuote() = _state.update { it.copy(quote = null) }
}
