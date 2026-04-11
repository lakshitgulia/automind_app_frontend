package com.automind.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainActivity : ComponentActivity() {
    companion object {
        // IMPORTANT: point this to the latest backend deployment that serves /state, /reset, /step.
        private const val BACKEND_BASE_URL = "https://khushi1811-automind-rl.hf.space/"
    }

    // Simple manual DI for the hackathon
    private val moshi = Moshi.Builder()
        .build()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BACKEND_BASE_URL)
        .client(httpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(AutoMindApiService::class.java)
    private val repository = VehicleRepository(apiService)

    private lateinit var userPreferences: UserPreferences
    private lateinit var vehiclePreferences: VehiclePreferences
    private var pollingJob: Job? = null

    private fun vehicleSeedPayload() = vehiclePreferences.getPrimaryVehicle()?.let { vehicle ->
        mapOf(
            "vehicle_name" to "${vehicle.make} ${vehicle.model} ${vehicle.year}",
            "vehicle_maker" to vehicle.make
        )
    }.orEmpty()

    private fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                if (vehiclePreferences.hasVehicles()) {
                    val primary = vehiclePreferences.getPrimaryVehicle()
                    if (primary != null && repository.getActiveCarId() == "default") {
                        repository.setActiveCarId(primary.licensePlate)
                    }

                    val fetched = repository.fetchCurrentState()
                    if (!fetched) {
                        val activeCarId = repository.getActiveCarId()
                        if (activeCarId != "default") {
                            repository.resetSession(activeCarId, vehicleSeedPayload())
                        }
                    }
                }
                delay(2000L)
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

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
                        startPolling()
                    } else {
                        stopPolling()
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

    override fun onDestroy() {
        stopPolling()
        super.onDestroy()
    }
}
