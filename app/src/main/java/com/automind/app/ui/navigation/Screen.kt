package com.automind.app.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: Int = 0) {
    object Login : Screen("login", "Login")
    object Home : Screen("home", "Home")
    object Vehicle : Screen("vehicle", "Vehicle")
    object Alerts : Screen("alerts", "Alerts")
    object Profile : Screen("profile", "Profile")
    object DemoControlPanel : Screen("demo_control", "Demo Control")
}
