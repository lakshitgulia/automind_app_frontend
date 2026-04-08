package com.automind.app.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.local.UserPreferences
import com.automind.app.data.local.VehiclePreferences
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.components.*
import com.automind.app.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: VehicleRepository,
    userPreferences: UserPreferences,
    vehiclePreferences: VehiclePreferences,
    onNavigateToVehicle: () -> Unit = {}
) {
    val uiState by repository.uiState.collectAsState()
    val isConnected by repository.isConnected.collectAsState()
    val recommendation by repository.recommendation.collectAsState()
    val userName = userPreferences.getUserName()
    var vehicles by remember { mutableStateOf(vehiclePreferences.getVehicles()) }
    val primaryVehicle = vehicles.firstOrNull { it.isPrimary } ?: vehicles.firstOrNull()
    val hasVehicle = primaryVehicle != null
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..20 -> "Good Evening"
        else -> "Good Night"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    Text(
                        "AutoMind",
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$greeting, $userName",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (hasVehicle) "System operational. Live monitoring is active." else "Add a vehicle to start monitoring.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            if (!hasVehicle) {
                // No Vehicle â€” Prompt Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = AccentCyan.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No Vehicle Connected",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Go to Profile and add your vehicle to start receiving real-time telemetry, diagnostics, and AI-powered insights.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedCyanButton(
                                text = "ADD VEHICLE",
                                onClick = { showAddVehicleDialog = true },
                                icon = Icons.Default.Add
                            )
                        }
                    }
                }
            }

            if (hasVehicle) {
                // Vehicle Connection Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${primaryVehicle!!.make} ${primaryVehicle.model} ${primaryVehicle.year}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = primaryVehicle.licensePlate,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AccentCyan
                                    )
                                }
                                StatusChip(
                                    status = if (isConnected) "CONNECTED" else "OFFLINE",
                                    color = if (isConnected) StatusGreen else StatusRed
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Drive mode + distance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "DRIVE MODE",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = uiState.driveModeDisplay.uppercase(),
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "RANGE",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${uiState.rangeKm} km",
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp),
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Car icon area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = AccentCyan.copy(alpha = 0.6f),
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                // Continuous Analysis
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PulsingDot(color = AccentCyan, size = 10.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Continuous Analysis",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Live telemetry is flowing through the predictive maintenance model to forecast failures before breakdown.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // AI Recommendation
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (recommendation.isCritical) StatusRed.copy(alpha = 0.15f) else AccentCyan.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "\"${recommendation.message}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Predicted failure: ${uiState.predictedFailure.uppercase()} | Risk ${uiState.predictedFailureRiskPct}% | Horizon ${uiState.failureHorizonKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current phase: ${uiState.currentFaultPhase.uppercase()} | Next shift: ${uiState.nextLikelyRiskShift.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "AUTOMIND AI INSIGHT JUST NOW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                            recommendation.actionText?.let { actionText ->
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        if (actionText.contains("Inspect Vehicle", ignoreCase = true)) {
                                            onNavigateToVehicle()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (recommendation.isCritical) StatusRed else AccentCyan,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(actionText, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Quick Telemetry
                item {
                    SectionHeader("Quick Telemetry")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LargeMetricCard(
                            title = "Speed",
                            value = "%.1f km/h".format(uiState.speed),
                            icon = Icons.Default.Speed,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LargeMetricCard(
                            title = "Eng Temp",
                            value = "%.1f °C".format(uiState.engineTemp),
                            icon = Icons.Default.Thermostat,
                            modifier = Modifier.fillMaxWidth()
                        )
                        LargeMetricCard(
                            title = "Battery",
                            value = "${uiState.batteryHealth.toInt()} %",
                            icon = Icons.Default.BatteryChargingFull,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }

        // Add Vehicle Dialog
        if (showAddVehicleDialog) {
            com.automind.app.ui.screens.profile.AddVehicleDialog(
                onDismiss = { showAddVehicleDialog = false },
                onSave = { vehicle ->
                    vehiclePreferences.saveVehicle(vehicle)
                    vehicles = vehiclePreferences.getVehicles()
                    showAddVehicleDialog = false
                    
                    // Activate on backend
                    repository.setActiveCarId(vehicle.licensePlate)
                    coroutineScope.launch {
                        repository.resetSession(vehicle.licensePlate, mapOf("vehicle_name" to "${vehicle.make} ${vehicle.model} ${vehicle.year}", "vehicle_maker" to vehicle.make))
                    }
                }
            )
        }
    }
}
