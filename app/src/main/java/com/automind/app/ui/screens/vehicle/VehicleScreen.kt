package com.automind.app.ui.screens.vehicle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.local.VehiclePreferences
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.components.*
import com.automind.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleScreen(repository: VehicleRepository, vehiclePreferences: VehiclePreferences) {
    val uiState by repository.uiState.collectAsState()
    val hasVehicle = vehiclePreferences.hasVehicles()
    val selectedVehicle = vehiclePreferences.getPrimaryVehicle() ?: vehiclePreferences.getVehicles().firstOrNull()

    LaunchedEffect(selectedVehicle?.id, selectedVehicle?.licensePlate, hasVehicle) {
        if (!hasVehicle) return@LaunchedEffect

        val activeCarId = selectedVehicle?.licensePlate ?: repository.getActiveCarId()
        if (repository.getActiveCarId() != activeCarId) {
            repository.setActiveCarId(activeCarId)
        }
        repository.fetchCurrentState()
    }

    val healthScore = uiState.healthScore
    val statusLabel = when {
        healthScore > 80 -> "ALL SYSTEMS NOMINAL"
        healthScore > 50 -> "⚠ ATTENTION NEEDED"
        else -> "⚠ CRITICAL"
    }
    val statusColor = when {
        healthScore > 80 -> StatusGreen
        healthScore > 50 -> StatusOrange
        else -> StatusRed
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    Text("AutoMind", color = AccentCyan, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (!hasVehicle) {
            // No vehicle state
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = AccentCyan.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Vehicle Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add a vehicle from your Profile to view health scores, diagnostics, and real-time telemetry data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Health Score
                item {
                    SectionHeader("Overall Health Score")
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$healthScore",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 56.sp),
                            color = AccentCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/100",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "PREDICTIVE MAINTENANCE MODEL",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = uiState.predictedFailure.uppercase(),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Risk ${uiState.predictedFailureRiskPct}% • Confidence ${(uiState.predictionConfidence * 100).toInt()}% • Horizon ${uiState.failureHorizonKm} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Current phase: ${uiState.currentFaultPhase.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "Next likely risk shift: ${uiState.nextLikelyRiskShift.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Telemetry Grid 2x2
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LargeMetricCard(
                            title = "RPM",
                            value = "%.0f".format(uiState.rpm),
                            icon = Icons.Default.Speed,
                            modifier = Modifier.weight(1f)
                        )
                        LargeMetricCard(
                            title = "Throttle",
                            value = "${uiState.throttle.toInt()} %",
                            icon = Icons.Default.Tune,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LargeMetricCard(
                            title = "Gear",
                            value = uiState.gearDisplay,
                            icon = Icons.Default.SettingsInputComponent,
                            modifier = Modifier.weight(1f)
                        )
                        LargeMetricCard(
                            title = "Engine Load",
                            value = "${uiState.engineLoad.toInt()} %",
                            icon = Icons.Default.LocalGasStation,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Driving Safety
                item {
                    SectionHeader("Driving Safety")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ProgressBarItem("Driving Safety Score", uiState.safetyScore.toFloat().coerceIn(0f, 1f), StatusGreen)
                        }
                    }
                }

                item {
                    SectionHeader("Failure Risk Forecast")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            ProgressBarItem("Engine Overheat Risk", uiState.predictedEngineRisk.toFloat().coerceIn(0f, 1f), StatusRed)
                            ProgressBarItem("Oil System Risk", uiState.predictedOilRisk.toFloat().coerceIn(0f, 1f), StatusOrange)
                            ProgressBarItem("Battery Failure Risk", uiState.predictedBatteryRisk.toFloat().coerceIn(0f, 1f), AccentCyan)
                            ProgressBarItem("Brake Degradation Risk", uiState.predictedBrakeRisk.toFloat().coerceIn(0f, 1f), StatusOrange)
                        }
                    }
                }

                // Diagnostic Integrity
                item {
                    SectionHeader("Diagnostic Integrity")
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DiagnosticRow(
                            icon = Icons.Default.DirectionsCar,
                            title = "Brake System",
                            subtitle = "Collision ${(uiState.collisionRisk * 100).toInt()}% • DTC ${uiState.dtcCount}",
                            status = if (uiState.brakeSystemStatus) "WARNING" else "NORMAL",
                            statusColor = if (uiState.brakeSystemStatus) StatusRed else StatusGreen
                        )
                        DiagnosticRow(
                            icon = Icons.Default.Sensors,
                            title = "Sensor Array",
                            subtitle = "Heading ${uiState.heading.toInt()}° • MIL ${if (uiState.milActive) "ACTIVE" else "CLEAR"}",
                            status = if (uiState.sensorSystemStatus) "WARNING" else "NORMAL",
                            statusColor = if (uiState.sensorSystemStatus) StatusRed else StatusGreen
                        )
                        DiagnosticRow(
                            icon = Icons.Default.LocalFireDepartment,
                            title = "Engine Performance",
                            subtitle = "TEMP ${uiState.engineTemp.toInt()}°C • LOAD ${uiState.engineLoad.toInt()}%",
                            status = uiState.engineStatus,
                            statusColor = when (uiState.engineStatus.uppercase()) {
                                "CRITICAL", "WARNING", "ATTENTION" -> StatusRed
                                else -> StatusGreen
                            }
                        )
                        DiagnosticRow(
                            icon = Icons.Default.WaterDrop,
                            title = "Oil Life",
                            subtitle = "LEVEL ${uiState.oilHealth.toInt()}% • RATE ${"%.2f".format(uiState.fuelRate)} L/h",
                            status = if (uiState.oilWarning) "LOW" else "NORMAL",
                            statusColor = if (uiState.oilWarning) StatusOrange else StatusGreen
                        )
                        DiagnosticRow(
                            icon = Icons.Default.BatteryChargingFull,
                            title = "Battery Health",
                            subtitle = "CHARGE ${uiState.batteryHealth.toInt()}% • ${"%.2f".format(uiState.batteryVoltage)} V",
                            status = if (uiState.batteryAlert) "WARNING" else "NORMAL",
                            statusColor = if (uiState.batteryAlert) StatusOrange else StatusGreen
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}
