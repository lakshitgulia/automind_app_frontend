package com.automind.app.ui.screens.alerts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.model.AlertPriority
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.components.AlertCard
import com.automind.app.ui.components.SectionHeader
import com.automind.app.ui.components.StatusChip
import com.automind.app.ui.theme.AccentCyan
import com.automind.app.ui.theme.DarkBackground
import com.automind.app.ui.theme.DarkSurface
import com.automind.app.ui.theme.DarkSurfaceVariant
import com.automind.app.ui.theme.StatusGreen
import com.automind.app.ui.theme.StatusOrange
import com.automind.app.ui.theme.StatusRed
import com.automind.app.ui.theme.TextPrimary
import com.automind.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(repository: VehicleRepository) {
    val alerts by repository.alerts.collectAsState()
    val uiState by repository.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by remember { mutableStateOf(false) }
    var requestedDate by remember { mutableStateOf("") }
    var requestedTime by remember { mutableStateOf("") }
    var serviceActionInFlight by remember { mutableStateOf(false) }
    val liveVehicleLocation = remember(uiState.vehicleLat, uiState.vehicleLon) {
        formatCoordinates(uiState.vehicleLat, uiState.vehicleLon)
    }

    LaunchedEffect(uiState.carId) {
        repository.fetchCurrentState(force = true)
    }

    LaunchedEffect(uiState.serviceScheduledDate, uiState.serviceScheduledTime, uiState.serviceRequestedDate, uiState.serviceRequestedTime) {
        requestedDate = uiState.serviceScheduledDate.ifBlank { uiState.serviceRequestedDate }
        requestedTime = uiState.serviceScheduledTime.ifBlank { uiState.serviceRequestedTime }
    }

    val criticalAlerts = alerts.filter { it.priority == AlertPriority.CRITICAL }
    val warningAlerts = alerts.filter { it.priority == AlertPriority.WARNING }
    val safetyAlerts = alerts.filter { it.priority == AlertPriority.SAFETY }
    val infoAlerts = alerts.filter { it.priority == AlertPriority.INFO }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Smart Alerts",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 26.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontSize = 48.sp),
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Active Alerts",
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (alerts.isEmpty()) {
                                "No active backend alerts for ${uiState.vehicleDisplayName}."
                            } else {
                                "Backend currently reports ${alerts.size} live alert(s) for this vehicle."
                            },
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            if (criticalAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = StatusRed, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Critical Priority",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
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
                        accentColor = StatusRed
                    )
                }
            }

            if (warningAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Warnings",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
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
                        accentColor = StatusOrange
                    )
                }
            }

            if (safetyAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Safety Notices",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
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

            if (infoAlerts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Service / Info",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(infoAlerts) { alert ->
                    AlertCard(
                        icon = Icons.Default.CalendarMonth,
                        title = alert.title,
                        description = alert.message,
                        priority = "INFO",
                        priorityColor = AccentCyan,
                        accentColor = AccentCyan
                    )
                }
            }

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
                                Text(
                                    "All Clear",
                                    style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                                    color = StatusGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "No live alerts are coming from the backend right now.",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

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
                        val hasBooking = uiState.serviceBookingStatus != null && uiState.serviceBookingId.isNotBlank()
                        StatusChip(
                            status = if (hasBooking) "SERVICE SCHEDULED" else if (uiState.serviceDueNow) "SERVICE DUE" else "SERVICE RECOMMENDED",
                            color = if (hasBooking) AccentCyan else StatusGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (hasBooking) "Service Booking Active" else "Maintenance Planning",
                            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (hasBooking) "A service slot has been reserved for this vehicle based on current diagnostics."
                            else if (uiState.serviceDueNow) "Backend diagnostics say this vehicle is due for service soon."
                            else "Next backend maintenance window is in ${uiState.serviceRemainingKm} km.",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        if (hasBooking) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Center: ${uiState.serviceCenterName}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (uiState.serviceCenterAddress.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.serviceCenterAddress,
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Date: ${uiState.serviceScheduledDate.ifBlank { "Pending" }}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "Time: ${uiState.serviceScheduledTime.ifBlank { "Pending" }}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "ETA: ${uiState.serviceEtaMinutes} min | Distance: ${"%.1f".format(uiState.serviceDistanceKm)} km",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            if (uiState.serviceBookingId.isNotBlank()) {
                                Text(
                                    text = "Booking ID: ${uiState.serviceBookingId}",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            if (uiState.serviceCenterPhone.isNotBlank()) {
                                Text(
                                    text = "Contact: ${uiState.serviceCenterPhone}",
                                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            Text(
                                text = "Live vehicle location: $liveVehicleLocation",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        } else if (uiState.serviceCenterName.isNotBlank()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nearest center: ${uiState.serviceCenterName}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = AccentCyan
                            )
                            Text(
                                text = "ETA ${uiState.serviceEtaMinutes} min | ${"%.1f".format(uiState.serviceDistanceKm)} km away",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Maintenance due in ${uiState.serviceRemainingKm} km",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    when {
                                        serviceActionInFlight -> Unit
                                        hasBooking && uiState.serviceBookingEditable -> showEditDialog = true
                                        hasBooking -> {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Booking details are already shown here")
                                            }
                                        }
                                        else -> {
                                            coroutineScope.launch {
                                                serviceActionInFlight = true
                                                val success = repository.executeAiCycle(
                                                    actionType = "request_service",
                                                    value = 0.99,
                                                    reason = "User requested service from alerts screen"
                                                )
                                                if (success) {
                                                    repository.fetchCurrentState(force = true)
                                                    snackbarHostState.showSnackbar("Service scheduled successfully")
                                                } else {
                                                    snackbarHostState.showSnackbar("Unable to schedule service right now")
                                                }
                                                serviceActionInFlight = false
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (hasBooking) "VIEW" else if (serviceActionInFlight) "WORKING..." else "SCHEDULE NOW",
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    maxLines = 1
                                )
                                if (!hasBooking) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                            if (hasBooking && uiState.serviceBookingEditable) {
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (!serviceActionInFlight) {
                                            showEditDialog = true
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Text("EDIT", fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                                OutlinedButton(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (!serviceActionInFlight) {
                                            coroutineScope.launch {
                                                serviceActionInFlight = true
                                                val success = repository.executeAiCycle(
                                                actionType = "cancel_service",
                                                value = 0.99,
                                                reason = "User cancelled service booking"
                                            )
                                                if (success) {
                                                    repository.fetchCurrentState(force = true)
                                                    snackbarHostState.showSnackbar("Service cancelled successfully")
                                                } else {
                                                    snackbarHostState.showSnackbar("Unable to cancel service right now")
                                                }
                                                serviceActionInFlight = false
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed)
                                ) {
                                    Text(
                                        if (serviceActionInFlight) "CANCELLING..." else "CANCEL",
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
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
                    serviceActionInFlight = true
                    val success = repository.executeAiCycle(
                        actionType = "reschedule_service",
                        value = 0.99,
                        reason = "requested_date=$date;requested_time=$time"
                    )
                    if (success) {
                        repository.fetchCurrentState(force = true)
                        snackbarHostState.showSnackbar("Service rescheduled successfully")
                    } else {
                        snackbarHostState.showSnackbar("Unable to reschedule service right now")
                    }
                    serviceActionInFlight = false
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

private fun formatCoordinates(latitude: Double, longitude: Double): String {
    if (latitude == 0.0 && longitude == 0.0) return "Unavailable"
    return "${"%.5f".format(latitude)}, ${"%.5f".format(longitude)}"
}
