package com.automind.app.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.model.AlertPriority
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.components.*
import com.automind.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(repository: VehicleRepository) {
    val alerts by repository.alerts.collectAsState()
    val uiState by repository.uiState.collectAsState()

    val criticalAlerts = alerts.filter { it.priority == AlertPriority.CRITICAL }
    val warningAlerts = alerts.filter { it.priority == AlertPriority.WARNING }
    val safetyAlerts = alerts.filter { it.priority == AlertPriority.SAFETY }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                },
                actions = {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AccentCyan)
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Text(
                    "Smart Alerts",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Alert Counter Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        SectionHeader("System Diagnostics")
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "%02d".format(alerts.size),
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 48.sp),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Alerts",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (alerts.isEmpty()) "All systems operating within normal parameters."
                            else "Real-time telemetry analysis detected immediate attention requirements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Critical Priority Section
            if (criticalAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Critical Priority",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(criticalAlerts) { alert ->
                    val icon = when {
                        alert.title.contains("Engine", ignoreCase = true) -> Icons.Default.LocalFireDepartment
                        alert.title.contains("Brake", ignoreCase = true) -> Icons.Default.DirectionsCar
                        alert.title.contains("Obstacle", ignoreCase = true) -> Icons.Default.Warning
                        else -> Icons.Default.Error
                    }
                    AlertCard(
                        icon = icon,
                        title = alert.title,
                        description = alert.message,
                        priority = "CRITICAL",
                        priorityColor = StatusRed,
                        accentColor = StatusRed,
                        onLocateService = {},
                        onDismiss = {}
                    )
                }
            }

            // Warning Section
            if (warningAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Warnings",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(warningAlerts) { alert ->
                    val icon = when {
                        alert.title.contains("Oil", ignoreCase = true) -> Icons.Default.WaterDrop
                        alert.title.contains("Battery", ignoreCase = true) -> Icons.Default.BatteryAlert
                        else -> Icons.Default.Warning
                    }
                    AlertCard(
                        icon = icon,
                        title = alert.title,
                        description = alert.message,
                        priority = "WARNING",
                        priorityColor = StatusOrange,
                        accentColor = StatusOrange,
                        onLocateService = {},
                        onDismiss = {}
                    )
                }
            }

            // Safety notices
            if (safetyAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Safety Notices", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                items(safetyAlerts) { alert ->
                    AlertCard(
                        icon = Icons.Default.Shield,
                        title = alert.title,
                        description = alert.message,
                        priority = "SAFETY",
                        priorityColor = AccentCyan,
                        accentColor = AccentCyan
                    )
                }
            }

            // No alerts state
            if (alerts.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusGreen.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("All Clear", style = MaterialTheme.typography.titleMedium, color = StatusGreen, fontWeight = FontWeight.Bold)
                                Text("No active alerts. Vehicle is healthy.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                    }
                }
            }

            // Maintenance Schedule Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader("Maintenance Schedule")
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatusChip(status = "SERVICE RECOMMENDED", color = StatusGreen)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "30,000 Mile Tune-Up",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Your vehicle is due for its regular interval service. This includes spark plug replacement and fluid flush.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("SCHEDULE NOW", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
