package com.skytycoon.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.skytycoon.app.ui.screens.achievements.AchievementsScreen
import com.skytycoon.app.ui.screens.dashboard.DashboardScreen
import com.skytycoon.app.ui.screens.purchaseaircraft.PurchaseAircraftScreen
import com.skytycoon.app.ui.screens.employees.EmployeesScreen
import com.skytycoon.app.ui.screens.fleet.FleetScreen
import com.skytycoon.app.ui.screens.map.MapScreen
import com.skytycoon.app.ui.screens.missions.MissionsScreen
import com.skytycoon.app.ui.screens.newgame.NewGameScreen
import com.skytycoon.app.ui.screens.schedule.ScheduleScreen
import com.skytycoon.app.ui.screens.settings.SettingsScreen
import com.skytycoon.app.ui.screens.splash.SplashScreen
import com.skytycoon.app.ui.screens.store.StoreScreen
import com.skytycoon.app.ui.theme.SkyAccentBlue
import com.skytycoon.app.ui.theme.SkyDarkBlue
import com.skytycoon.app.ui.theme.SkyTextSecondary

// ---------------------------------------------------------------------------
// Model
// ---------------------------------------------------------------------------

private data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Home),
    BottomNavItem(Screen.Fleet, "Frota", Icons.Filled.FlightTakeoff),
    BottomNavItem(Screen.Schedule, "Voos", Icons.Filled.Schedule),
    BottomNavItem(Screen.Employees, "Equipe", Icons.Filled.People),
    BottomNavItem(Screen.Achievements, "Conquistas", Icons.Filled.EmojiEvents),
    BottomNavItem(Screen.Map, "Mapa", Icons.Filled.Map),
)

// ---------------------------------------------------------------------------
// Root nav graph
// ---------------------------------------------------------------------------

/**
 * Root [NavHost] for the entire app.
 *
 * Splash and NewGame are full-screen with no bottom navigation.
 * Dashboard, Fleet, Schedule, Employees, Missions, and Map each include
 * [SkyBottomNavBar] inside their own Scaffold.
 */
@Composable
fun SkyTycoonNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.NewGame.route) {
            NewGameScreen(navController = navController)
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        composable(Screen.Fleet.route) {
            FleetScreen(navController = navController)
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController = navController)
        }
        composable(Screen.Employees.route) {
            EmployeesScreen(navController = navController)
        }
        composable(Screen.Missions.route) {
            MissionsScreen(navController = navController)
        }
        composable(Screen.Map.route) {
            MapScreen(navController = navController)
        }
        composable(Screen.Achievements.route) {
            AchievementsScreen(navController = navController)
        }
        composable(Screen.Missions.route) {
            MissionsScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Store.route) {
            StoreScreen(navController = navController)
        }
        composable(Screen.PurchaseAircraft.route) {
            PurchaseAircraftScreen(navController = navController)
        }
    }
}

// ---------------------------------------------------------------------------
// Shared bottom navigation bar
// ---------------------------------------------------------------------------

/**
 * Material 3 [NavigationBar] shared by all main-graph screens.
 *
 * Drop this into any screen's [Scaffold] `bottomBar` slot:
 * ```kotlin
 * Scaffold(bottomBar = { SkyBottomNavBar(navController) }) { … }
 * ```
 *
 * Navigation behaviour:
 * - Single-top launch so re-selecting the current tab is a no-op.
 * - Pops up to the graph's start destination with state saving, keeping
 *   the back stack clean while preserving each tab's scroll/UI state.
 */
@Composable
fun SkyBottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = SkyDarkBlue,
        contentColor = SkyAccentBlue
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SkyAccentBlue,
                    selectedTextColor = SkyAccentBlue,
                    indicatorColor = SkyAccentBlue.copy(alpha = 0.15f),
                    unselectedIconColor = SkyTextSecondary,
                    unselectedTextColor = SkyTextSecondary
                )
            )
        }
    }
}
