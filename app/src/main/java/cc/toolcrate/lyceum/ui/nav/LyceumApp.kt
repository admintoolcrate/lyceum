package cc.toolcrate.lyceum.ui.nav

import cc.toolcrate.lyceum.stagira.StagiraLinks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cc.toolcrate.lyceum.ui.components.Hairline
import cc.toolcrate.lyceum.ui.editor.EditorScreen
import cc.toolcrate.lyceum.ui.entry.EntryScreen
import cc.toolcrate.lyceum.ui.journal.JournalScreen
import cc.toolcrate.lyceum.ui.search.SearchScreen
import cc.toolcrate.lyceum.ui.week.WeekScreen
import cc.toolcrate.lyceum.ui.prompts.PromptsScreen
import cc.toolcrate.lyceum.ui.settings.AboutScreen
import cc.toolcrate.lyceum.ui.settings.SettingsScreen
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cc.toolcrate.lyceum.domain.GoalRef
import cc.toolcrate.lyceum.stagira.StagiraContract
import cc.toolcrate.lyceum.stagira.StagiraGoalSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** The four places on the bottom bar. */
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Edit),
    Journal("journal", "Journal", Icons.Outlined.DateRange),
    Search("search", "Search", Icons.Outlined.Search),
    Week("week", "Week", Icons.Outlined.Menu),
}

object Routes {
    const val SETTINGS = "settings"
    const val PROMPTS = "prompts"
    const val ABOUT = "about"
    const val ENTRY = "entry/{date}"
    const val EDIT = "edit/{date}"
    fun entry(date: LocalDate) = "entry/$date"
    fun edit(date: LocalDate) = "edit/$date"
}

@Composable
fun LyceumApp(
    openToday: StateFlow<Boolean> = MutableStateFlow(false),
    onOpenedToday: () -> Unit = {},
    today: StateFlow<LocalDate> = MutableStateFlow(LocalDate.now()),
    nav: NavHostController = rememberNavController(),
    links: StagiraLinks = hiltViewModel(),
) {
    val goToday by openToday.collectAsStateWithLifecycle()
    // Screens that depend on the date get fresh view models when the day changes.
    val day by today.collectAsStateWithLifecycle()
    val stagiraOn by links.enabled.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(goToday) {
        if (goToday) {
            nav.navigate(Tab.Today.route) {
                popUpTo(nav.graph.findStartDestination().id) { inclusive = false }
                launchSingleTop = true
            }
            onOpenedToday()
        }
    }
    val goalSlot: @Composable (GoalRef?, (GoalRef?) -> Unit) -> Unit = { goal, onPick ->
        StagiraGoalSlot(goal, onPick, enabled = stagiraOn, beforeLeaving = links::expectReturn)
    }
    val openGoal: ((GoalRef) -> Unit)? = if (stagiraOn) { goal -> links.expectReturn(); StagiraContract.view(context, goal) } else null
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val onTab = Tab.entries.any { tab -> current?.hierarchy?.any { it.route == tab.route } == true }
    val paper = LyceumTheme.paper
    Scaffold(
        containerColor = paper.page,
        bottomBar = { if (onTab) BottomBar(nav, current?.hierarchy?.mapNotNull { it.route }?.toSet().orEmpty()) },
    ) { padding ->
        NavHost(nav, startDestination = Tab.Today.route, modifier = Modifier.padding(padding)) {
            composable(Tab.Today.route) {
                EditorScreen(
                    onBack = null, onOpenSettings = { nav.navigate(Routes.SETTINGS) }, goalSlot = goalSlot,
                    viewModel = hiltViewModel(key = "today-$day"),
                )
            }
            composable(Tab.Journal.route) {
                JournalScreen(
                    onOpen = { nav.navigate(Routes.entry(it)) },
                    onWrite = { nav.navigate(Routes.edit(it)) },
                    onOpenSettings = { nav.navigate(Routes.SETTINGS) },
                    viewModel = hiltViewModel(key = "journal-$day"),
                )
            }
            composable(Tab.Search.route) { SearchScreen(onOpen = { nav.navigate(Routes.entry(it)) }) }
            composable(Tab.Week.route) {
                WeekScreen(onOpen = { nav.navigate(Routes.entry(it)) }, viewModel = hiltViewModel(key = "week-$day"))
            }
            dated(Routes.ENTRY) {
                EntryScreen(onBack = { nav.popBackStack() }, onEdit = { nav.navigate(Routes.edit(it)) }, onOpenGoal = openGoal)
            }
            dated(Routes.EDIT) {
                EditorScreen(onBack = { nav.popBackStack() }, onOpenSettings = {}, goalSlot = goalSlot)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onBack = { nav.popBackStack() },
                    onOpenPrompts = { nav.navigate(Routes.PROMPTS) },
                    onOpenAbout = { nav.navigate(Routes.ABOUT) },
                )
            }
            composable(Routes.PROMPTS) { PromptsScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ABOUT) { AboutScreen(onBack = { nav.popBackStack() }) }
        }
    }
}

private fun NavGraphBuilder.dated(route: String, content: @Composable () -> Unit) =
    composable(route, arguments = listOf(navArgument("date") { type = NavType.StringType })) { content() }

@Composable
private fun BottomBar(nav: NavHostController, routes: Set<String>) {
    val paper = LyceumTheme.paper
    Column {
        Hairline()
        NavigationBar(containerColor = paper.page, tonalElevation = 0.dp) {
            Tab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = tab.route in routes,
                    onClick = {
                        nav.navigate(tab.route) {
                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = paper.ink,
                        selectedTextColor = paper.ink,
                        indicatorColor = paper.accentSoft,
                        unselectedIconColor = paper.muted,
                        unselectedTextColor = paper.muted,
                    ),
                )
            }
        }
    }
}
