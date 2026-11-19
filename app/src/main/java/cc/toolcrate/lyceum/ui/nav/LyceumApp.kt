package cc.toolcrate.lyceum.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cc.toolcrate.lyceum.ui.theme.LyceumTheme

/** The four places on the bottom bar. */
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Edit),
    Journal("journal", "Journal", Icons.Outlined.DateRange),
    Search("search", "Search", Icons.Outlined.Search),
    Week("week", "Week", Icons.Outlined.Menu),
}

@Composable
fun LyceumApp() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    val paper = LyceumTheme.paper
    Scaffold(
        containerColor = paper.page,
        bottomBar = {
            Column {
                HorizontalDivider(color = paper.hairline)
                NavigationBar(containerColor = paper.page, tonalElevation = 0.dp) {
                    Tab.entries.forEach { tab ->
                        val selected = current?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
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
        },
    ) { padding ->
        NavHost(nav, startDestination = Tab.Today.route, modifier = Modifier.padding(padding)) {
            Tab.entries.forEach { tab ->
                composable(tab.route) { Placeholder(tab.label) }
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
