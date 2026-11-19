package cc.toolcrate.lyceum.store

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import cc.toolcrate.lyceum.data.db.SeedPrompts
import cc.toolcrate.lyceum.domain.PromptRotation
import cc.toolcrate.lyceum.ui.entry.EntryScreen
import cc.toolcrate.lyceum.ui.entry.EntryViewModel
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import cc.toolcrate.lyceum.R
import cc.toolcrate.lyceum.TestClock
import cc.toolcrate.lyceum.data.DbTest
import cc.toolcrate.lyceum.data.repo.EntryRepository
import cc.toolcrate.lyceum.data.repo.PromptRepository
import cc.toolcrate.lyceum.domain.Entry
import cc.toolcrate.lyceum.domain.Mood
import cc.toolcrate.lyceum.domain.QuoteChooser
import cc.toolcrate.lyceum.stagira.PendingGoal
import cc.toolcrate.lyceum.ui.editor.EditorScreen
import cc.toolcrate.lyceum.ui.editor.EditorViewModel
import cc.toolcrate.lyceum.ui.editor.QuoteContent
import cc.toolcrate.lyceum.ui.journal.JournalScreen
import cc.toolcrate.lyceum.ui.journal.JournalViewModel
import cc.toolcrate.lyceum.ui.search.SearchScreen
import cc.toolcrate.lyceum.ui.search.SearchViewModel
import cc.toolcrate.lyceum.ui.theme.Garamond
import cc.toolcrate.lyceum.ui.theme.Inter
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import cc.toolcrate.lyceum.ui.theme.ThemeMode
import cc.toolcrate.lyceum.ui.week.WeekScreen
import cc.toolcrate.lyceum.ui.week.WeekViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * The Play Store icon, feature graphic and phone screenshots, drawn from the real app with
 * made-up pages. Run with: ./gradlew testDebugUnitTest -Pstore=true
 * Output: app/build/store/ (copy what you want into docs/play-store/).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h640dp-xxhdpi") // 1080 × 1920
class StoreGraphics : DbTest() {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val clock = TestClock(Instant.parse("2026-11-19T19:30:00Z"), ZoneOffset.UTC)
    private val today = LocalDate.of(2026, 11, 19)

    private fun seed(): Pair<EntryRepository, PromptRepository> = runBlocking {
        val prompts = PromptRepository(db).also { it.ensureSeeded() }
        val entries = EntryRepository(db, clock)
        // Made-up pages: days back from today, the page, its mood, what was learned, its tags.
        data class Page(val back: Long, val body: String, val mood: Mood?, val learned: String?, val tags: List<String>)
        listOf(
            Page(1, "Dinner with my sister. We laughed about the old flat and the broken kettle.", Mood.Bright, "Call her more often", listOf("family")),
            Page(2, "Said no to the extra project. It felt awkward for an hour, then like relief.", Mood.Good, null, listOf("work")),
            Page(3, "A long walk by the river before work. Slipped in the evening: too much scrolling, too little sleep.", Mood.Even, "Walking first makes the day kinder", listOf("walk", "sleep")),
            Page(6, "Rain all morning. Baked bread and let the day be small.", Mood.Even, null, listOf("home")),
            Page(7, "Tired all day. Kept the promise to read twenty pages anyway.", Mood.Low, null, listOf("reading")),
            Page(9, "Fixed the bike chain myself, slowly, with a video and a lot of patience.", Mood.Good, "Slow is fine", listOf("craft")),
            Page(10, "Snapped at a colleague over nothing. Apologised at lunch; it went better than expected.", Mood.Low, "Say sorry sooner", listOf("work")),
            Page(13, "Walked to the market with a friend instead of driving.", Mood.Good, null, listOf("walk")),
            Page(16, "First cold morning of the year. Swam anyway.", Mood.Bright, null, listOf("swim")),
        ).forEach { p ->
            val date = today.minusDays(p.back)
            val prompt = PromptRotation.promptFor(date, SeedPrompts.all)
            entries.save(Entry(date, prompt, p.body, p.mood, p.learned, p.tags))
        }
        entries to prompts
    }

    private fun shoot(name: String, mode: ThemeMode = ThemeMode.Light, content: @Composable () -> Unit) {
        compose.mainClock.autoAdvance = false
        compose.setContent {
            LyceumTheme(mode) { Box(Modifier.fillMaxSize().background(LyceumTheme.paper.page)) { content() } }
        }
        repeat(30) {
            compose.mainClock.advanceTimeBy(100)
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100))
            Thread.sleep(20)
        }
        val view = compose.activity.window.decorView
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val dir = File("build/store").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    @Test
    fun s1_today() {
        val (entries, prompts) = seed()
        val vm = EditorViewModel(SavedStateHandle(), entries, prompts, PendingGoal(), clock)
        vm.setBody("Finished the hard conversation I had been putting off. My voice shook, but I said what I meant.")
        vm.toggleMood(Mood.Good)
        vm.setLearned("The dread is bigger than the thing")
        shoot("1-today") { EditorScreen(onBack = null, onOpenSettings = {}, viewModel = vm) }
    }

    @Test
    fun s2_quote() {
        // The sheet as it appears after saving: over the saved page, dimmed. (A real sheet is its own
        // window, which this capture can't see, so it is laid out by hand here.)
        val (entries, _) = seed()
        val date = today.minusDays(2)
        val saved = runBlocking { entries.get(date)!! }
        val vm = EntryViewModel(SavedStateHandle(mapOf("date" to date.toString())), entries, clock)
        shoot("2-quote") {
            Box(Modifier.fillMaxSize()) {
                EntryScreen(onBack = {}, onEdit = {}, viewModel = vm)
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)))
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                    Box(
                        Modifier.background(LyceumTheme.paper.card, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .padding(top = 36.dp, bottom = 24.dp),
                    ) { QuoteContent(QuoteChooser.choose(saved)) {} }
                }
            }
        }
    }

    @Test
    fun s3_journal() {
        val (entries, _) = seed()
        shoot("3-journal") { JournalScreen(onOpen = {}, onWrite = {}, onOpenSettings = {}, viewModel = JournalViewModel(entries, clock)) }
    }

    @Test
    fun s4_week_dark() {
        val (entries, _) = seed()
        shoot("4-week-dark", ThemeMode.Dark) { WeekScreen(onOpen = {}, viewModel = WeekViewModel(entries, clock)) }
    }

    @Test
    fun s5_search() {
        val (entries, _) = seed()
        val vm = SearchViewModel(entries, clock).apply { setQuery("walk") }
        shoot("5-search") { SearchScreen(onOpen = {}, viewModel = vm) }
    }

    @Test
    @Config(qualifiers = "w512dp-h512dp-mdpi")
    fun icon512() = shoot("icon-512") {
        Box(Modifier.fillMaxSize().background(LyceumTheme.paper.page), contentAlignment = Alignment.Center) {
            // The adaptive icon's foreground is drawn on a 108-unit canvas; show its safe zone large.
            // requiredSize lets the 108-unit canvas overflow the 512 box, so the book itself fills ~70%.
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.requiredSize(860.dp))
        }
    }

    @Test
    @Config(qualifiers = "w1024dp-h500dp-mdpi")
    fun featureGraphic() = shoot("feature-graphic-1024x500") {
        val paper = LyceumTheme.paper
        Row(Modifier.fillMaxSize().padding(horizontal = 72.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(300.dp))
            Spacer(Modifier.width(24.dp))
            Column(Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text("Lyceum", style = TextStyle(fontFamily = Garamond, fontSize = 92.sp), color = paper.ink)
                Text("A quiet daily reflection journal", style = TextStyle(fontFamily = Inter, fontSize = 26.sp), color = paper.muted)
                Spacer(Modifier.size(28.dp))
                Text(
                    "“…one swallow does not make spring,\nnor does one fine day…”",
                    style = TextStyle(fontFamily = Garamond, fontStyle = FontStyle.Italic, fontSize = 28.sp, lineHeight = 36.sp),
                    color = paper.accent,
                )
            }
        }
    }
}
