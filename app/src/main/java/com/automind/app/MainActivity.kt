package com.automind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.automind.app.data.local.UserPreferences
import com.automind.app.data.local.VehiclePreferences
import com.automind.app.data.network.AutoMindApiService
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.navigation.AutoMindBottomBar
import com.automind.app.ui.navigation.AutoMindNavHost
import com.automind.app.ui.navigation.Screen
import com.automind.app.ui.theme.AutoMindTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
    companion object {
        // IMPORTANT: point this to the latest backend deployment that serves /state, /reset, /step.
        private const val BACKEND_BASE_URL = "https://automind-rl.onrender.com/"
    }

    // Simple manual DI for the hackathon
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BACKEND_BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(AutoMindApiService::class.java)
    private val repository = VehicleRepository(apiService)

    private lateinit var userPreferences: UserPreferences
    private lateinit var vehiclePreferences: VehiclePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(applicationContext)
        vehiclePreferences = VehiclePreferences(applicationContext)

        setContent {
            AutoMindTheme {
                val navController = rememberNavController()
                val startDestination = if (userPreferences.isLoggedIn()) Screen.Home.route else Screen.Login.route

                // Get current route to determine if we should poll
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // Continuous telemetry loop — only when logged in
                LaunchedEffect(currentRoute) {
                    if (currentRoute != null && currentRoute != Screen.Login.route) {
                        while (true) {
                            if (vehiclePreferences.hasVehicles()) {
                                if (repository.getActiveCarId() == "default") {
                                    val primary = vehiclePreferences.getPrimaryVehicle()
                                    if (primary != null) {
                                        repository.setActiveCarId(primary.licensePlate)
                                        try {
                                            repository.resetSession(primary.licensePlate)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                                repository.fetchCurrentState()
                            }
                            delay(2000L)
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { AutoMindBottomBar(navController) }
                ) { innerPadding ->
                    AutoMindNavHost(
                        navController = navController,
                        repository = repository,
                        userPreferences = userPreferences,
                        vehiclePreferences = vehiclePreferences,
                        startDestination = startDestination,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
