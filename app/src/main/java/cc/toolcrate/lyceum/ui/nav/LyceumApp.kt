package cc.toolcrate.lyceum.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import cc.toolcrate.lyceum.ui.prompts.PromptsScreen
import cc.toolcrate.lyceum.ui.settings.AboutScreen
import cc.toolcrate.lyceum.ui.settings.SettingsScreen
import cc.toolcrate.lyceum.ui.theme.LyceumTheme
import java.time.LocalDate

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
fun LyceumApp(nav: NavHostController = rememberNavController()) {
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
                EditorScreen(onBack = null, onOpenSettings = { nav.navigate(Routes.SETTINGS) })
            }
            composable(Tab.Journal.route) { Placeholder("Journal") }
            composable(Tab.Search.route) { Placeholder("Search") }
            composable(Tab.Week.route) { Placeholder("Week") }
            dated(Routes.EDIT) {
                EditorScreen(onBack = { nav.popBackStack() }, onOpenSettings = {})
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

@Composable
private fun Placeholder(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
