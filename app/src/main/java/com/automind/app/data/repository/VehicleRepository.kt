package com.automind.app.data.repository

import android.util.Log
import com.automind.app.data.model.*
import com.automind.app.data.network.AutoMindApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class VehicleRepository(
    private val apiService: AutoMindApiService
) {
    companion object {
        private const val TAG = "VehicleRepository"
    }

    private val _uiState = MutableStateFlow(VehicleStateSummary())
    val uiState: StateFlow<VehicleStateSummary> = _uiState.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _recommendation = MutableStateFlow(
        RecommendationItem(
            "Vehicle reading steady. Continue driving safely.",
            null
        )
    )
    val recommendation: StateFlow<RecommendationItem> = _recommendation.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Active car ID for backend calls
    private var _activeCarId: String = "default"

    // Disabled mock fallback to strictly test new render backend
    private var useMockFallback = false

    fun setActiveCarId(carId: String) {
        _activeCarId = carId
        Log.d(TAG, "Active car ID set to: $carId")
    }

    fun getActiveCarId(): String = _activeCarId

    suspend fun fetchCurrentState() {
        try {
            val response = apiService.getState(System.currentTimeMillis(), _activeCarId)
            processBackendResponse(response)
            _isConnected.value = true
            Log.d(TAG, "fetchCurrentState success (carId=$_activeCarId)")
        } catch (e: Exception) {
            Log.e(TAG, "fetchCurrentState failed", e)
            _isConnected.value = false
            if (useMockFallback) {
                applyMockState(jitter = true)
            }
        }
    }

    suspend fun resetSession(carId: String = _activeCarId, payload: Map<String, String> = emptyMap()) {
        try {
            val response = apiService.resetSession(carId, payload)
            processBackendResponse(response)
            _isConnected.value = true
            Log.d(TAG, "resetSession success: carId=$carId")
        } catch (e: Exception) {
            Log.e(TAG, "resetSession failed", e)
            _isConnected.value = false
            if (useMockFallback) {
                applyMockState(jitter = true)
            }
        }
    }

    suspend fun executeAiCycle(
        actionType: String,
        value: Double = 0.0,
        reason: String = "Auto-run AI cycle"
    ) {
        try {
            val response = apiService.executeStep(
                StepRequest(
                    actionType,
                    value,
                    reason
                ),
                _activeCarId
            )
            processBackendResponse(response)
            _isConnected.value = true
            Log.d(TAG, "executeAiCycle success: actionType=$actionType value=$value carId=$_activeCarId")
        } catch (e: Exception) {
            Log.e(TAG, "executeAiCycle failed", e)
            _isConnected.value = false
            if (useMockFallback) {
                applyMockState(jitter = true)
            }
        }
    }

    fun setMockFallback(enabled: Boolean) {
        useMockFallback = enabled
    }

    private fun processBackendResponse(response: BackendStateResponse) {
        val obs = response.observation
        val met = response.metrics
        val inf = response.info

        val current = _uiState.value
        val summary = current.copy(
            speed = obs?.speed ?: current.speed,
            engineTemp = obs?.engineTemp ?: current.engineTemp,
            rpm = obs?.rpm ?: current.rpm,
            throttle = obs?.throttle ?: current.throttle,
            gear = obs?.gear ?: current.gear,
            forwardDistance = obs?.distanceToObstacle ?: current.forwardDistance,
            roadCondition = obs?.roadCondition ?: current.roadCondition,
            oilHealth = obs?.oilLevel ?: current.oilHealth,
            batteryHealth = obs?.batteryHealth ?: current.batteryHealth,
            engineLoad = obs?.engineLoad ?: current.engineLoad,
            driveMode = obs?.driveMode ?: current.driveMode,
            brakeSystemStatus = obs?.failures?.brakeFailure ?: current.brakeSystemStatus,
            sensorSystemStatus = obs?.failures?.sensorFailure ?: current.sensorSystemStatus,
            engineHeatAlert = obs?.failures?.engineOverheating ?: current.engineHeatAlert,
            oilWarning = obs?.failures?.lowOil ?: current.oilWarning,
            batteryAlert = obs?.failures?.batteryIssue ?: current.batteryAlert,
            safetyScore = met?.safetyScore ?: current.safetyScore,
            efficiencyScore = met?.efficiencyScore ?: current.efficiencyScore,
            diagnosticConfidence = met?.diagnosisScore ?: current.diagnosticConfidence,
            decisionStability = met?.sequenceScore ?: current.decisionStability,
            collisionRisk = inf?.collisionRisk ?: current.collisionRisk,
            overrideDetected = inf?.overrideActive ?: current.overrideDetected,
            vehicleStatus = inf?.outcome ?: current.vehicleStatus
        )

        _uiState.value = summary
        generateAlertsAndRecommendations(summary)
    }

    private fun generateAlertsAndRecommendations(state: VehicleStateSummary) {
        val newAlerts = mutableListOf<AlertItem>()
        var recMsg = "Vehicle reading steady. Continue driving safely."
        var action: String? = null
        var isCrit = false

        if (state.engineHeatAlert || state.engineTemp > 100.0) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "High Engine Temperature",
                    "Engine heat exceeds safe limits.",
                    AlertPriority.CRITICAL
                )
            )
            recMsg = "High engine temperature detected. Stop the vehicle safely and inspect immediately."
            isCrit = true
            action = "Request Service"
        } else if (state.brakeSystemStatus) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "Brake System Issue",
                    "Braking efficiency compromised.",
                    AlertPriority.CRITICAL
                )
            )
            recMsg = "Brake system anomaly detected. Reduce speed and stop vehicle safely."
            isCrit = true
            action = "Emergency Contact"
        } else if (state.collisionRisk > 0.7) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "Obstacle Risk",
                    "High probability of forward collision.",
                    AlertPriority.CRITICAL
                )
            )
            recMsg = "Obstacle risk detected. Reduce speed and maintain braking readiness."
            isCrit = true
        } else if (state.batteryAlert || state.batteryHealth < 30.0) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "Battery Inspection",
                    "Output voltage irregular.",
                    AlertPriority.WARNING
                )
            )
            recMsg = "Battery health needs inspection. Avoid long trips until checked."
            action = "Book Service"
        } else if (state.oilWarning || state.oilHealth < 20.0) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "Low Oil Level",
                    "Engine oil below optimal threshold.",
                    AlertPriority.WARNING
                )
            )
            recMsg = "Oil level is low. Service is recommended soon."
            action = "Book Maintenance"
        }

        if (state.overrideDetected) {
            newAlerts.add(
                AlertItem(
                    UUID.randomUUID().toString(),
                    "Driver Override Active",
                    "Manual control inputs overriding AI.",
                    AlertPriority.SAFETY
                )
            )
        }

        val combined = (newAlerts + _alerts.value).distinctBy { it.title }.take(10)
        _alerts.value = combined
        _recommendation.value = RecommendationItem(recMsg, action, isCrit)
    }

    private fun applyMockState(jitter: Boolean = false) {
        val s = _uiState.value
        val speedBump = if (jitter) (Math.random() * 10 - 5) else 0.0
        val tempBump = if (jitter) (Math.random() * 5) else 0.0
        val loadBump = if (jitter) (Math.random() * 15) else 0.0
        val newSpeed = (s.speed + speedBump).coerceIn(0.0, 140.0).takeIf { it > 0 } ?: 65.0

        val mockState = VehicleStateSummary(
            speed = newSpeed,
            engineTemp = (s.engineTemp + tempBump).takeIf { it > 0 } ?: 90.0,
            rpm = newSpeed * 25.4 + (if (jitter) Math.random() * 200 else 0.0),
            throttle = (15.0 + loadBump).coerceIn(0.0, 100.0),
            gear = if (newSpeed > 80.0) 5 else if (newSpeed > 40.0) 4 else 3,
            engineLoad = (30.0 + loadBump).coerceIn(0.0, 100.0),
            forwardDistance = 120.0,
            batteryHealth = 85.0,
            oilHealth = 72.0,
            safetyScore = 0.94,
            efficiencyScore = 0.88,
            diagnosticConfidence = 0.99,
            decisionStability = 0.99
        )

        _uiState.value = mockState
        generateAlertsAndRecommendations(mockState)
    }
}