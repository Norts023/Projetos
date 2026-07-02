package com.skytycoon.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.skytycoon.app.ui.screens.dashboard.DashboardScreen
import com.skytycoon.app.ui.screens.employees.EmployeesScreen
import com.skytycoon.app.ui.screens.fleet.FleetScreen
import com.skytycoon.app.ui.screens.map.MapScreen
import com.skytycoon.app.ui.screens.missions.MissionsScreen
import com.skytycoon.app.ui.screens.newgame.NewGameScreen
import com.skytycoon.app.ui.screens.schedule.ScheduleScreen
import com.skytycoon.app.ui.screens.splash.SplashScreen

@Composable
fun SkyTycoonNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.NewGame.route) { NewGameScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        composable(Screen.Fleet.route) { FleetScreen(navController) }
        composable(Screen.Schedule.route) { ScheduleScreen(navController) }
        composable(Screen.Employees.route) { EmployeesScreen(navController) }
        composable(Screen.Missions.route) { MissionsScreen(navController) }
        composable(Screen.Map.route) { MapScreen(navController) }
    }
}
