package com.automind.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automind.app.data.local.UserPreferences
import com.automind.app.data.local.VehiclePreferences
import com.automind.app.data.model.VehicleInfo
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.components.*
import com.automind.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    repository: VehicleRepository,
    userPreferences: UserPreferences,
    vehiclePreferences: VehiclePreferences,
    onLogout: () -> Unit
) {
    val isConnected by repository.isConnected.collectAsState()
    val uiState by repository.uiState.collectAsState()
    val userName = userPreferences.getUserName()
    val userEmail = userPreferences.getUserEmail()
    var vehicles by remember { mutableStateOf(vehiclePreferences.getVehicles()) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // User Avatar and Info
            item {
                Spacer(modifier = Modifier.height(8.dp))
                UserAvatar(name = userName, size = 90.dp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                StatusChip(status = "VERIFIED", color = StatusGreen)
            }

            // Primary Vehicle Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    SectionHeader("Primary Vehicle")
                }
            }

            // Vehicle Cards
            if (vehicles.isEmpty()) {
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
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No vehicle added yet", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                            Text("Add your vehicle to start monitoring", color = TextSecondary.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            items(vehicles) { vehicle ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            repository.setActiveCarId(vehicle.licensePlate)
                            vehiclePreferences.setPrimaryVehicle(vehicle.id)
                            vehicles = vehiclePreferences.getVehicles()
                            coroutineScope.launch {
                                val fetched = repository.fetchCurrentState()
                                if (!fetched) {
                                    repository.resetSession(
                                        vehicle.licensePlate,
                                        mapOf(
                                            "vehicle_name" to "${vehicle.make} ${vehicle.model} ${vehicle.year}",
                                            "vehicle_maker" to vehicle.make
                                        )
                                    )
                                }
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "${vehicle.make} ${vehicle.model} ${vehicle.year}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    vehicle.licensePlate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AccentCyan
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusChip(
                                    status = if (isConnected && vehicle.isPrimary) "ACTIVE" else "OFFLINE",
                                    color = if (isConnected && vehicle.isPrimary) StatusGreen else StatusRed
                                )
                                IconButton(
                                    onClick = {
                                        val removingActiveVehicle =
                                            vehicle.licensePlate == repository.getActiveCarId() || vehicle.isPrimary
                                        vehiclePreferences.removeVehicle(vehicle.id)
                                        vehicles = vehiclePreferences.getVehicles()

                                        if (removingActiveVehicle) {
                                            val nextVehicle = vehiclePreferences.getPrimaryVehicle()
                                            if (nextVehicle != null) {
                                                repository.setActiveCarId(nextVehicle.licensePlate)
                                                coroutineScope.launch {
                                                    val fetched = repository.fetchCurrentState()
                                                    if (!fetched) {
                                                        repository.resetSession(
                                                            nextVehicle.licensePlate,
                                                            mapOf(
                                                                "vehicle_name" to "${nextVehicle.make} ${nextVehicle.model} ${nextVehicle.year}",
                                                                "vehicle_maker" to nextVehicle.make
                                                            )
                                                        )
                                                    }
                                                }
                                            } else {
                                                repository.setActiveCarId("default")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Remove vehicle",
                                        tint = StatusRed
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = AccentCyan.copy(alpha = 0.55f),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }

            // Add New Vehicle Button
            item {
                OutlinedCyanButton(
                    text = "ADD NEW VEHICLE",
                    onClick = { showAddVehicleDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Add
                )
            }

            // Settings
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.DeleteForever,
                            title = "Delete Account",
                            titleColor = StatusRed,
                            onClick = { showDeleteAccountDialog = true }
                        )
                        HorizontalDivider(color = DarkSurfaceVariant, thickness = 0.5.dp)
                        SettingsRow(
                            icon = Icons.Default.Logout,
                            title = "Logout",
                            titleColor = StatusRed,
                            onClick = {
                                userPreferences.logout()
                                onLogout()
                            }
                        )
                    }
                }
            }

            // App version
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "AutoMind v3.0.6",
                    color = TextSecondary.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AddVehicleDialog(
            onDismiss = { showAddVehicleDialog = false },
            onSave = { vehicle ->
                vehiclePreferences.saveVehicle(vehicle)
                vehicles = vehiclePreferences.getVehicles()
                showAddVehicleDialog = false
                
                repository.setActiveCarId(vehicle.licensePlate)
                coroutineScope.launch {
                    repository.resetSession(vehicle.licensePlate)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = "Delete Account?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "This will permanently remove $userEmail and all saved local data for this account, including vehicles. You will need to create the account again to use it.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val emailToDelete = userPreferences.getCurrentUserEmail() ?: userEmail
                        vehiclePreferences.deleteVehiclesForEmail(emailToDelete)
                        userPreferences.deleteCurrentAccount()
                        showDeleteAccountDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusRed,
                        contentColor = TextPrimary
                    )
                ) {
                    Text("DELETE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("CANCEL", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = TextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = titleColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, color = titleColor)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onSave: (VehicleInfo) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2023") }
    var plate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = {
            Text("Add New Vehicle", fontWeight = FontWeight.Bold, color = AccentCyan)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Make (e.g., Hyundai)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model (e.g., Creta)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Year") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it.uppercase() },
                    label = { Text("License Plate") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedLabelColor = AccentCyan,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (make.isNotBlank() && model.isNotBlank() && plate.isNotBlank()) {
                        onSave(
                            VehicleInfo(
                                make = make.trim(),
                                model = model.trim(),
                                year = year.toIntOrNull() ?: 2023,
                                licensePlate = plate.trim()
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("SAVE VEHICLE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        }
    )
}
