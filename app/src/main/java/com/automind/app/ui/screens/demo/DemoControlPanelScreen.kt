package com.automind.app.ui.screens.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.automind.app.data.repository.VehicleRepository
import com.automind.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoControlPanelScreen(repository: VehicleRepository) {
    val coroutineScope = rememberCoroutineScope()
    var selectedMode by remember { mutableStateOf("Diagnostics Mode") }
    var selectedDifficulty by remember { mutableStateOf("Normal Driving") }
    var running by remember { mutableStateOf(false) }

    val modes = listOf("Diagnostics Mode", "Safety Monitoring Mode", "Full AutoMind Protection Mode")
    val difficulties = listOf("Normal Driving", "Urban Traffic", "Critical Road Scenario")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demo Control Panel", color = StatusOrange) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("WARNING: Technical screen for Hackathon Demo only. Do not show to customers.", color = StatusRed, style = MaterialTheme.typography.labelSmall)
            
            Text("Mode", color = TextPrimary)
            modes.forEach { mode ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                    )
                    Text(mode, color = TextPrimary)
                }
            }

            Text("Scenario Difficulty", color = TextPrimary)
            difficulties.forEach { diff ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        colors = RadioButtonDefaults.colors(selectedColor = AccentCyan)
                    )
                    Text(diff, color = TextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        running = true
                        repository.resetSession(
                            payload = mapOf(
                                "mode" to selectedMode,
                                "difficulty" to selectedDifficulty
                            )
                        )
                        running = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentYellow, contentColor = DarkBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Demo Session", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        running = true
                        repository.executeAiCycle(
                            actionType = "monitor",
                            value = 0.5,
                            reason = "Auto-run AI cycle"
                        )
                        running = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = DarkBackground),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Run Next AI Cycle", fontWeight = FontWeight.Bold)
            }

            if (running) {
                CircularProgressIndicator(color = AccentCyan, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally))
            }
        }
    }
}
