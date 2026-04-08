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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(repository: VehicleRepository) {
    val alerts by repository.alerts.collectAsState()
    val uiState by repository.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var requestedDate by remember { mutableStateOf("") }
    var requestedTime by remember { mutableStateOf("") }

    LaunchedEffect(uiState.serviceScheduledDate, uiState.serviceScheduledTime, uiState.serviceRequestedDate, uiState.serviceRequestedTime) {
        requestedDate = uiState.serviceScheduledDate.ifBlank { uiState.serviceRequestedDate }
        requestedTime = uiState.serviceScheduledTime.ifBlank { uiState.serviceRequestedTime }
    }

    val criticalAlerts = alerts.filter { it.priority == AlertPriority.CRITICAL }
    val warningAlerts = alerts.filter { it.priority == AlertPriority.WARNING }
    val safetyAlerts = alerts.filter { it.priority == AlertPriority.SAFETY }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
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
                        StatusChip(
                            status = if (uiState.serviceBookingStatus != null) "SERVICE SCHEDULED" else if (uiState.serviceDueNow) "SERVICE DUE" else "SERVICE RECOMMENDED",
                            color = if (uiState.serviceBookingStatus != null) AccentCyan else StatusGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (uiState.serviceBookingStatus != null) "Service Booking Active" else "Maintenance Planning",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (uiState.serviceBookingStatus != null) "A service slot has been reserved for this vehicle based on current diagnostics."
                            else if (uiState.serviceDueNow) "Backend diagnostics say this vehicle is due for service soon."
                            else "Track maintenance windows proactively based on live diagnostics and health degradation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        if (uiState.serviceBookingStatus != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Center: ${uiState.serviceCenterName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (uiState.serviceCenterAddress.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.serviceCenterAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Date: ${uiState.serviceScheduledDate.ifBlank { "Pending" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "Time: ${uiState.serviceScheduledTime.ifBlank { "Pending" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "ETA: ${uiState.serviceEtaMinutes} min | Distance: ${"%.1f".format(uiState.serviceDistanceKm)} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (uiState.serviceBookingId.isNotBlank()) {
                                Text(
                                    text = "Booking ID: ${uiState.serviceBookingId}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (uiState.serviceCenterPhone.isNotBlank()) {
                                Text(
                                    text = "Contact: ${uiState.serviceCenterPhone}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "Live vehicle location: ${"%.5f".format(uiState.vehicleLat)}, ${"%.5f".format(uiState.vehicleLon)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else if (uiState.serviceCenterName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nearest center: ${uiState.serviceCenterName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "ETA ${uiState.serviceEtaMinutes} min | ${"%.1f".format(uiState.serviceDistanceKm)} km away",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(`r`n                                modifier = Modifier.weight(1f),
                                onClick = {
                                    coroutineScope.launch {
                                        repository.executeAiCycle(
                                            actionType = "request_service",
                                            value = 1.0,
                                            reason = "User requested service from alerts screen"
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(if (uiState.serviceBookingStatus != null) "SCHEDULED" else "SCHEDULE NOW", fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            if (uiState.serviceBookingStatus != null && uiState.serviceBookingEditable) {
                                OutlinedButton(`r`n                                    modifier = Modifier.weight(1f),
                                    onClick = { showEditDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Text("EDIT", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(`r`n                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        coroutineScope.launch {
                                            repository.executeAiCycle(
                                                actionType = "cancel_service",
                                                value = 1.0,
                                                reason = "User cancelled service booking"
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                                ) {
                                    Text("CANCEL", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showEditDialog) {
        EditServiceDialog(
            initialDate = requestedDate,
            initialTime = requestedTime,
            onDismiss = { showEditDialog = false },
            onConfirm = { date, time ->
                showEditDialog = false
                coroutineScope.launch {
                    repository.executeAiCycle(
                        actionType = "reschedule_service",
                        value = 1.0,
                        reason = "requested_date=$date;requested_time=$time"
                    )
                }
            }
        )
    }
}

@Composable
private fun EditServiceDialog(
    initialDate: String,
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
) {
    var date by remember(initialDate) { mutableStateOf(initialDate) }
    var time by remember(initialTime) { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Edit Service Slot", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Preferred Date") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                    )
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Preferred Time") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(date.trim(), time.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}