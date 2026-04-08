package com.automind.app.data.repository

import android.util.Log
import com.automind.app.data.model.*
import com.automind.app.data.network.AutoMindApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class VehicleRepository(
    private val apiService: AutoMindApiService
) {
    companion object {
        private const val TAG = "VehicleRepository"
    }

    private val _uiState = MutableStateFlow(VehicleStateSummary())
    val uiState: StateFlow<VehicleStateSummary> = _uiState.asStateFlow()
    private val stateCache = mutableMapOf<String, VehicleStateSummary>()

    private val _alerts = MutableStateFlow<List<AlertItem>>(emptyList())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()
    private val alertsCache = mutableMapOf<String, List<AlertItem>>()

    private val _recommendation = MutableStateFlow(
        RecommendationItem(
            "Vehicle reading steady. Continue driving safely.",
            null
        )
    )
    val recommendation: StateFlow<RecommendationItem> = _recommendation.asStateFlow()
    private val recommendationCache = mutableMapOf<String, RecommendationItem>()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // Active car ID for backend calls
    private var _activeCarId: String = "default"

    // Disabled mock fallback to strictly test new render backend
    private var useMockFallback = false

    fun setActiveCarId(carId: String) {
        _activeCarId = carId
        _uiState.value = stateCache[carId] ?: VehicleStateSummary(carId = carId)
        _alerts.value = alertsCache[carId] ?: emptyList()
        _recommendation.value = recommendationCache[carId]
            ?: RecommendationItem(
                "Vehicle reading steady. Continue driving safely.",
                null
            )
        Log.d(TAG, "Active car ID set to: $carId")
    }

    fun getActiveCarId(): String = _activeCarId

    fun currentState(): VehicleStateSummary = _uiState.value

    fun clearCachedState() {
        stateCache.clear()
        alertsCache.clear()
        recommendationCache.clear()
        _activeCarId = "default"
        _uiState.value = VehicleStateSummary()
        _alerts.value = emptyList()
        _recommendation.value = RecommendationItem(
            "Vehicle reading steady. Continue driving safely.",
            null
        )
    }

    suspend fun fetchCurrentState() {
        try {
            val response = withContext(Dispatchers.IO) {
                apiService.getState(System.currentTimeMillis(), _activeCarId)
            }
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
            val response = withContext(Dispatchers.IO) {
                apiService.resetSession(carId, payload)
            }
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
            val response = withContext(Dispatchers.IO) {
                apiService.executeStep(
                    StepRequest(
                        actionType,
                        value,
                        reason
                    ),
                    _activeCarId
                )
            }
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

    private fun prettyRiskLabel(raw: String): String = raw.replace("_", " ")

    private fun nextLikelyRiskShift(predictions: MlPredictions?): String {
        val risks = listOfNotNull(
            predictions?.failureRisks?.engineOverheating?.let { "engine_overheating" to it },
            predictions?.failureRisks?.lowOil?.let { "low_oil" to it },
            predictions?.failureRisks?.batteryIssue?.let { "battery_issue" to it },
            predictions?.failureRisks?.brakeFailure?.let { "brake_failure" to it },
            predictions?.failureRisks?.collision?.let { "collision" to it },
        ).sortedByDescending { it.second }

        if (risks.isEmpty()) return "none"

        val dominant = predictions?.primaryFailure
        val next = risks.firstOrNull { it.first != dominant } ?: risks.first()
        return prettyRiskLabel(next.first)
    }

    private fun processBackendResponse(response: BackendStateResponse) {
        val obs = response.observation
        val met = response.metrics
        val inf = response.info
        val dashboard = response.dashboard
        val quick = dashboard?.quickTelemetry ?: response.quickTelemetry
        val trip = dashboard?.trip ?: response.trip
        val health = dashboard?.health ?: response.health
        val safety = dashboard?.safety
        val predictions = dashboard?.mlPredictions ?: response.mlPredictions ?: inf?.mlPredictions
        val maintenance = dashboard?.maintenance
        val booking = maintenance?.serviceBooking ?: inf?.serviceBooking
        val recommendation = maintenance?.serviceRecommended
        val identity = dashboard?.vehicle ?: response.vehicle

        val current = stateCache[_activeCarId] ?: _uiState.value
        val summary = current.copy(
            carId = identity?.carId ?: _activeCarId,
            vehicleDisplayName = identity?.name ?: current.vehicleDisplayName,
            vin = identity?.vin ?: current.vin,
            speed = quick?.speedKmph ?: obs?.speed ?: current.speed,
            engineTemp = quick?.engineTempC ?: obs?.engineTemp ?: current.engineTemp,
            rpm = obs?.rpm ?: current.rpm,
            throttle = obs?.throttle ?: current.throttle,
            gear = obs?.gear ?: current.gear,
            gearDisplay = trip?.gearDisplay ?: (obs?.gear?.let { "D${it} Auto" } ?: current.gearDisplay),
            forwardDistance = safety?.distanceToObstacleM ?: obs?.distanceToObstacle ?: current.forwardDistance,
            rangeKm = trip?.rangeKm ?: current.rangeKm,
            roadCondition = obs?.roadCondition ?: current.roadCondition,
            oilHealth = obs?.oilLevel ?: current.oilHealth,
            batteryHealth = health?.batteryHealth?.toDouble() ?: quick?.batteryPct?.toDouble() ?: obs?.batteryHealth ?: current.batteryHealth,
            engineLoad = obs?.engineLoad ?: current.engineLoad,
            driveMode = trip?.driveMode ?: obs?.driveMode ?: current.driveMode,
            driveModeDisplay = trip?.driveModeDisplay ?: obs?.driveMode ?: current.driveModeDisplay,
            brakeSystemStatus = obs?.failures?.brakeFailure ?: current.brakeSystemStatus,
            sensorSystemStatus = obs?.failures?.sensorFailure ?: current.sensorSystemStatus,
            engineHeatAlert = (health?.engineStatus == "CRITICAL" || health?.engineStatus == "ATTENTION")
                || obs?.failures?.engineOverheating == true
                || (quick?.engineTempC ?: obs?.engineTemp ?: 0.0) >= 105.0,
            oilWarning = obs?.failures?.lowOil ?: current.oilWarning,
            batteryAlert = obs?.failures?.batteryIssue ?: current.batteryAlert,
            engineStatus = health?.engineStatus ?: current.engineStatus,
            safetyScore = (safety?.drivingSafetyScore?.toDouble()?.div(100.0)) ?: met?.safetyScore ?: current.safetyScore,
            efficiencyScore = met?.efficiencyScore ?: current.efficiencyScore,
            diagnosticConfidence = met?.diagnosisScore ?: current.diagnosticConfidence,
            decisionStability = met?.sequenceScore ?: current.decisionStability,
            healthScore = health?.overallScore ?: inf?.healthScore ?: current.healthScore,
            collisionRisk = (safety?.collisionRiskPct?.toDouble()?.div(100.0)) ?: inf?.collisionRisk ?: current.collisionRisk,
            overrideDetected = inf?.overrideActive ?: current.overrideDetected,
            vehicleStatus = health?.status ?: inf?.outcome ?: current.vehicleStatus,
            fuelLevel = trip?.fuelLevelPct ?: current.fuelLevel,
            distanceDriven = trip?.odometerKm ?: current.distanceDriven,
            serviceDueNow = maintenance?.serviceDueNow ?: current.serviceDueNow,
            serviceBookingStatus = booking?.status,
            predictedFailure = (health?.predictedFailure ?: predictions?.primaryFailure ?: current.predictedFailure).replace("_", " "),
            predictedFailureRiskPct = health?.predictedFailureRiskPct ?: current.predictedFailureRiskPct,
            predictionConfidence = predictions?.confidence ?: current.predictionConfidence,
            failureHorizonKm = predictions?.failureHorizonKm ?: current.failureHorizonKm,
            predictiveModelName = predictions?.modelName ?: current.predictiveModelName,
            predictedEngineRisk = predictions?.failureRisks?.engineOverheating ?: current.predictedEngineRisk,
            predictedOilRisk = predictions?.failureRisks?.lowOil ?: current.predictedOilRisk,
            predictedBatteryRisk = predictions?.failureRisks?.batteryIssue ?: current.predictedBatteryRisk,
            predictedBrakeRisk = predictions?.failureRisks?.brakeFailure ?: current.predictedBrakeRisk,
            predictedCollisionRisk = predictions?.failureRisks?.collision ?: current.predictedCollisionRisk,
            currentFaultPhase = prettyRiskLabel(inf?.faultPhase ?: current.currentFaultPhase),
            nextLikelyRiskShift = nextLikelyRiskShift(predictions),
            serviceCenterName = booking?.centerName ?: recommendation?.name ?: "",
            serviceCenterAddress = booking?.centerAddress ?: recommendation?.address ?: "",
            serviceCenterPhone = booking?.centerPhone ?: recommendation?.phone ?: "",
            serviceDistanceKm = booking?.distanceKm ?: recommendation?.distanceKm ?: 0.0,
            serviceEtaMinutes = booking?.etaMinutes ?: recommendation?.etaMinutes ?: 0,
            serviceScheduledDate = booking?.scheduledDate ?: "",
            serviceScheduledTime = booking?.scheduledTime ?: "",
            serviceBookingId = booking?.bookingId ?: "",
            serviceUrgency = booking?.urgency ?: "",
            serviceRequestedDate = booking?.requestedDate ?: "",
            serviceRequestedTime = booking?.requestedTime ?: "",
            serviceBookingEditable = booking?.editable ?: false,
            vehicleLat = booking?.vehicleLat ?: recommendation?.vehicleLat ?: obs?.latitude ?: current.vehicleLat,
            vehicleLon = booking?.vehicleLon ?: recommendation?.vehicleLon ?: obs?.longitude ?: current.vehicleLon
        )

        _uiState.value = summary
        stateCache[summary.carId] = summary
        generateAlertsAndRecommendations(summary, dashboard?.activeAlerts ?: response.activeAlerts)
    }

    private fun generateAlertsAndRecommendations(state: VehicleStateSummary, backendAlerts: List<BackendAlert>? = null) {
        if (!backendAlerts.isNullOrEmpty()) {
            _alerts.value = backendAlerts.map { alert ->
                AlertItem(
                    id = UUID.randomUUID().toString(),
                    title = alert.title ?: "Vehicle Alert",
                    message = alert.message ?: "",
                    priority = when ((alert.severity ?: "").uppercase()) {
                        "CRITICAL" -> AlertPriority.CRITICAL
                        "WARN", "WARNING" -> AlertPriority.WARNING
                        "INFO" -> AlertPriority.INFO
                        else -> AlertPriority.SAFETY
                    }
                )
            }

            val topAlert = backendAlerts.first()
            _recommendation.value = RecommendationItem(
                message = topAlert.message ?: "Vehicle health update available.",
                actionText = when {
                    state.serviceBookingStatus != null -> "SERVICE SCHEDULED"
                    state.serviceDueNow -> "Schedule Service"
                    (topAlert.code ?: "").contains("COLLISION", ignoreCase = true) -> "Slow Down"
                    else -> "Inspect Vehicle"
                },
                isCritical = (topAlert.severity ?: "").equals("CRITICAL", ignoreCase = true)
            )
            alertsCache[state.carId] = _alerts.value
            recommendationCache[state.carId] = _recommendation.value
            return
        }

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
        alertsCache[state.carId] = _alerts.value
        recommendationCache[state.carId] = _recommendation.value
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
