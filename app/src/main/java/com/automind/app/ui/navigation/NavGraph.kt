package com.automind.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.automind.app.data.local.UserPreferences
import com.automind.app.data.local.VehiclePreferences
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.screens.alerts.AlertsScreen
import com.automind.app.ui.screens.home.HomeScreen
import com.automind.app.ui.screens.login.LoginScreen
import com.automind.app.ui.screens.profile.ProfileScreen
import com.automind.app.ui.screens.vehicle.VehicleScreen

@Composable
fun AutoMindNavHost(
    navController: NavHostController,
    repository: VehicleRepository,
    userPreferences: UserPreferences,
    vehiclePreferences: VehiclePreferences,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                userPreferences = userPreferences,
                onLoginSuccess = {
                    repository.clearCachedState()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                repository = repository,
                userPreferences = userPreferences,
                vehiclePreferences = vehiclePreferences,
                onNavigateToVehicle = {
                    navController.navigate(Screen.Vehicle.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Screen.Vehicle.route) {
            VehicleScreen(repository, vehiclePreferences)
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(repository)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                repository = repository,
                userPreferences = userPreferences,
                vehiclePreferences = vehiclePreferences,
                onLogout = {
                    repository.clearCachedState()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun AutoMindBottomBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Vehicle,
        Screen.Alerts,
        Screen.Profile
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Don't show bottom bar on Login
    if (currentRoute == Screen.Login.route) return

    NavigationBar(
        containerColor = com.automind.app.ui.theme.DarkSurface,
        contentColor = com.automind.app.ui.theme.TextPrimary
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    val icon = when(screen) {
                        Screen.Home -> Icons.Default.Home
                        Screen.Vehicle -> Icons.Filled.DirectionsCar
                        Screen.Alerts -> Icons.Default.Warning
                        Screen.Profile -> Icons.Default.Person
                        else -> Icons.Default.Home
                    }
                    Icon(icon, contentDescription = screen.title)
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = com.automind.app.ui.theme.AccentCyan,
                    unselectedIconColor = com.automind.app.ui.theme.TextSecondary,
                    selectedTextColor = com.automind.app.ui.theme.AccentCyan,
                    unselectedTextColor = com.automind.app.ui.theme.TextSecondary,
                    indicatorColor = com.automind.app.ui.theme.DarkSurfaceVariant
                )
            )
        }
    }
}
