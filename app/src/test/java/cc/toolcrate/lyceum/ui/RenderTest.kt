package cc.toolcrate.lyceum.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.graphics.Canvas
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.robolectric.Shadows.shadowOf
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.DbTest
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.stagira.PendingGoal
import cc.toolcrate.lyceum.ui.editor.EditorScreen
import cc.toolcrate.lyceum.ui.editor.EditorViewModel
import cc.toolcrate.lyceum.ui.journal.JournalScreen
import cc.toolcrate.lyceum.ui.journal.JournalViewModel
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.ThemeMode
import cc.toolcrate.lyceum.ui.week.WeekScreen
import cc.toolcrate.lyceum.ui.week.WeekViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Draws real screens with sample pages, in both modes, to check the theme and fonts load.
 * Images land in app/build/screens/ for a person to look at; the test fails only if drawing fails.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class RenderTest : DbTest() {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val clock = TestClock(Instant.parse("2026-11-19T18:00:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 19)

    private fun seed() = runBlocking {
        val prompts = PromptRepository(db).also { it.ensureSeeded() }
        val entries = EntryRepository(db, clock)
        val pages = listOf(
            Entry(today.minusDays(3), "What went well? What slipped? Why?", "A long walk by the river before work. Slipped on the evening: too much scrolling, too little sleep.", Mood.Even, "Walking first makes the day kinder", listOf("walk", "sleep")),
            Entry(today.minusDays(2), "What took more courage than it looked?", "Said no to the extra project. It felt awkward for an hour, then like relief.", Mood.Good, null, listOf("work")),
            Entry(today.minusDays(1), "Who did you spend time with, and how did it feel?", "Dinner with my sister. We laughed about the old flat.", Mood.Bright, "Call her more often", listOf("family")),
        )
        pages.forEach { entries.save(it) }
        entries to prompts
    }

    /** Lets the main looper (where viewModelScope runs) and Room's threads work until [done]. */
    private fun pumpUntil(done: () -> Boolean) {
        val deadline = System.currentTimeMillis() + 5_000
        while (!done()) {
            check(System.currentTimeMillis() < deadline) { "Timed out waiting for the screen to load" }
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(10)
        }
    }

    private fun shoot(name: String, mode: ThemeMode, content: @Composable () -> Unit) {
        // The same approach as Stagira's screenshots: advance the clock by hand, then draw the window.
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LyceumTheme(mode) { Box(Modifier.fillMaxSize().background(LyceumTheme.paper.page)) { content() } }
        }
        // Flows start once the screen subscribes; give Room's threads a moment, frame by frame.
        repeat(30) {
            compose.mainClock.advanceTimeBy(100)
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(20)
        }
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/screens").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun todayLight() {
        val (entries, prompts) = seed()
        val vm = EditorViewModel(SavedStateHandle(), entries, prompts, PendingGoal(), clock)
        pumpUntil { vm.state.value.loaded }
        shoot("today-light", ThemeMode.Light) { EditorScreen(onBack = null, onOpenSettings = {}, viewModel = vm) }
        compose.onNodeWithText("Today").assertExists()
        // Today's prompt follows the date: 19 Nov 2026 is epoch day 20776, and 20776 mod 12 = 4.
        compose.onNodeWithText(cc.toolcrate.lyceum.data.db.SeedPrompts.all[4]).assertExists()
    }

    @Test
    fun todayDark() {
        val (entries, prompts) = seed()
        val vm = EditorViewModel(SavedStateHandle(), entries, prompts, PendingGoal(), clock)
        pumpUntil { vm.state.value.loaded }
        shoot("today-dark", ThemeMode.Dark) { EditorScreen(onBack = null, onOpenSettings = {}, viewModel = vm) }
    }

    @Test
    fun journalLight() {
        val (entries, _) = seed()
        shoot("journal-light", ThemeMode.Light) {
            JournalScreen(onOpen = {}, onWrite = {}, onOpenSettings = {}, viewModel = JournalViewModel(entries, clock))
        }
    }

    @Test
    fun weekDark() {
        val (entries, _) = seed()
        shoot("week-dark", ThemeMode.Dark) { WeekScreen(onOpen = {}, viewModel = WeekViewModel(entries, clock)) }
        compose.onNodeWithText("What you learned".uppercase()).assertExists()
    }
}
