package com.skytycoon.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object NewGame : Screen("new_game")
    object Dashboard : Screen("dashboard")
    object Fleet : Screen("fleet")
    object Schedule : Screen("schedule")
    object Employees : Screen("employees")
    object Missions : Screen("missions")
    object Map : Screen("map")
}
