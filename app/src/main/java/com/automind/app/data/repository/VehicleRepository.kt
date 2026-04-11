package com.automind.app.data.repository

import android.os.SystemClock
import android.util.Log
import com.automind.app.data.model.*
import com.automind.app.data.network.AutoMindApiService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

class VehicleRepository(
    private val apiService: AutoMindApiService
) {
    companion object {
        private const val TAG = "VehicleRepository"
        private const val MIN_FETCH_GAP_MS = 3000L
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
    private val requestMutex = Mutex()
    private var lastFetchCarId: String? = null
    private var lastFetchAtMs: Long = 0L

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

    suspend fun fetchCurrentState(force: Boolean = false): Boolean {
        return requestMutex.withLock {
            val currentCarId = _activeCarId
            val now = SystemClock.elapsedRealtime()
            if (!force && currentCarId == lastFetchCarId && now - lastFetchAtMs < MIN_FETCH_GAP_MS) {
                return@withLock _isConnected.value
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getState(System.currentTimeMillis(), currentCarId)
                }
                processBackendResponse(response)
                lastFetchCarId = currentCarId
                lastFetchAtMs = SystemClock.elapsedRealtime()
                _isConnected.value = true
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "fetchCurrentState failed", e)
                _isConnected.value = false
                if (useMockFallback) {
                    applyMockState(jitter = true)
                }
                false
            }
        }
    }

    suspend fun resetSession(carId: String = _activeCarId, payload: Map<String, String> = emptyMap()): Boolean {
        return requestMutex.withLock {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.resetSession(carId, payload)
                }
                processBackendResponse(response)
                lastFetchCarId = carId
                lastFetchAtMs = SystemClock.elapsedRealtime()
                _isConnected.value = true
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "resetSession failed", e)
                _isConnected.value = false
                if (useMockFallback) {
                    applyMockState(jitter = true)
                }
                false
            }
        }
    }

    suspend fun executeAiCycle(
        actionType: String,
        value: Double = 0.0,
        reason: String = "Auto-run AI cycle"
    ): Boolean {
        return requestMutex.withLock {
            try {
                val normalizedValue = value.coerceIn(0.0, 0.99)
                val response = withContext(Dispatchers.IO) {
                    apiService.executeStep(
                        StepRequest(
                            actionType,
                            normalizedValue,
                            reason
                        ),
                        _activeCarId
                    )
                }
                processBackendResponse(response)
                lastFetchCarId = _activeCarId
                lastFetchAtMs = SystemClock.elapsedRealtime()
                _isConnected.value = true
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "executeAiCycle failed", e)
                _isConnected.value = false
                if (useMockFallback) {
                    applyMockState(jitter = true)
                }
                false
            }
        }
    }

    fun setMockFallback(enabled: Boolean) {
        useMockFallback = enabled
    }

    private fun cleanGearDisplay(raw: String): String =
        raw.replace(" auto", "", ignoreCase = true).trim()

    private fun prettyRiskLabel(raw: String): String = raw.replace("_", " ")

    private fun backendPercent(raw: Double?): Int? =
        raw?.let { value ->
            if (value <= 1.0) {
                (value * 100).toInt()
            } else {
                value.toInt()
            }
        }?.coerceIn(0, 100)

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
        val signals = obs?.vehicleSignals
        val events = obs?.vehicleEvents
        val met = response.metrics
        val inf = response.info
        val dashboard = response.dashboard
        val quick = dashboard?.quickTelemetry ?: response.quickTelemetry
        val trip = dashboard?.trip ?: response.trip
        val health = dashboard?.health ?: response.health
        val safety = dashboard?.safety ?: response.safety
        val predictions = dashboard?.mlPredictions ?: response.mlPredictions ?: inf?.mlPredictions
        val maintenance = dashboard?.maintenance ?: response.maintenance
        val booking = maintenance?.serviceBooking ?: inf?.serviceBooking
        val recommendation = maintenance?.serviceRecommended ?: inf?.serviceRecommended
        val identity = dashboard?.vehicle ?: response.vehicle
        val ecuObd = response.ecu?.obd
        val resolvedCollisionRisk = safety?.collisionRiskPct?.div(100.0) ?: inf?.collisionRisk
        val resolvedHealthScore = predictions?.health?.overall
            ?: backendPercent(health?.overallScore)
        val resolvedSafetyScore = resolvedCollisionRisk?.let { (1.0 - it).coerceIn(0.0, 1.0) }
            ?: safety?.drivingSafetyScore?.let { if (it > 1.0) it / 100.0 else it }
            ?: predictions?.health?.safety?.div(100.0)

        val current = stateCache[_activeCarId] ?: _uiState.value
        val summary = current.copy(
            carId = identity?.carId ?: _activeCarId,
            vehicleDisplayName = identity?.name ?: current.vehicleDisplayName,
            vin = identity?.vin ?: current.vin,
            speed = quick?.speedKmph ?: signals?.speed ?: obs?.speed ?: current.speed,
            engineTemp = quick?.engineTempC ?: signals?.coolantTemp ?: obs?.engineTemp ?: current.engineTemp,
            rpm = signals?.rpm ?: obs?.rpm ?: current.rpm,
            throttle = signals?.throttle ?: obs?.throttle ?: current.throttle,
            gear = signals?.gear ?: obs?.gear ?: current.gear,
            gearDisplay = trip?.gearDisplay?.let(::cleanGearDisplay)
                ?: ((signals?.gear ?: obs?.gear)?.let { "D$it" } ?: current.gearDisplay),
            forwardDistance = safety?.distanceToObstacleM ?: signals?.distanceToObstacle ?: obs?.distanceToObstacle ?: current.forwardDistance,
            rangeKm = trip?.rangeKm ?: current.rangeKm,
            roadCondition = signals?.roadCondition ?: obs?.roadCondition ?: current.roadCondition,
            oilHealth = signals?.oilLevel ?: obs?.oilLevel ?: current.oilHealth,
            batteryHealth = predictions?.health?.battery?.toDouble()
                ?: health?.batteryHealth?.toDouble()
                ?: quick?.batteryPct?.toDouble()
                ?: signals?.batteryHealth
                ?: obs?.batteryHealth
                ?: current.batteryHealth,
            engineLoad = signals?.engineLoad ?: obs?.engineLoad ?: current.engineLoad,
            transmissionLoad = signals?.transmissionLoad ?: obs?.transmissionLoad ?: current.transmissionLoad,
            fuelRate = signals?.fuelRate ?: obs?.fuelRate ?: current.fuelRate,
            acceleration = signals?.acceleration ?: obs?.acceleration ?: current.acceleration,
            batteryVoltage = quick?.batteryV ?: signals?.batteryVoltage ?: current.batteryVoltage,
            oilTempC = ecuObd?.oilTempC?.toDouble() ?: signals?.oilTemp ?: current.oilTempC,
            oilPressureKpa = ecuObd?.oilPressureKpa?.toDouble() ?: signals?.oilPressure ?: current.oilPressureKpa,
            heading = signals?.heading ?: obs?.heading ?: current.heading,
            driveMode = trip?.driveMode ?: signals?.driveMode ?: obs?.driveMode ?: current.driveMode,
            driveModeDisplay = trip?.driveModeDisplay ?: signals?.driveMode ?: obs?.driveMode ?: current.driveModeDisplay,
            brakeSystemStatus = events?.brakeSystemWarning ?: obs?.failures?.brakeFailure ?: current.brakeSystemStatus,
            sensorSystemStatus = events?.sensorFaultEvent ?: obs?.failures?.sensorFailure ?: current.sensorSystemStatus,
            engineHeatAlert = (health?.engineStatus == "CRITICAL" || health?.engineStatus == "ATTENTION")
                || events?.engineOverheatWarning == true
                || obs?.failures?.engineOverheating == true
                || (quick?.engineTempC ?: signals?.coolantTemp ?: obs?.engineTemp ?: 0.0) >= 105.0,
            oilWarning = events?.lowOilWarning ?: obs?.failures?.lowOil ?: current.oilWarning,
            batteryAlert = events?.lowBatteryEvent ?: obs?.failures?.batteryIssue ?: current.batteryAlert,
            engineStatus = health?.engineStatus ?: current.engineStatus,
            safetyScore = resolvedSafetyScore ?: met?.safetyScore ?: current.safetyScore,
            efficiencyScore = met?.efficiencyScore ?: current.efficiencyScore,
            diagnosticConfidence = met?.diagnosisScore ?: current.diagnosticConfidence,
            decisionStability = met?.sequenceScore ?: current.decisionStability,
            healthScore = resolvedHealthScore ?: backendPercent(inf?.healthScore) ?: current.healthScore,
            collisionRisk = resolvedCollisionRisk ?: current.collisionRisk,
            overrideDetected = inf?.overrideActive ?: current.overrideDetected,
            vehicleStatus = identity?.status ?: health?.status ?: inf?.outcome ?: current.vehicleStatus,
            fuelLevel = trip?.fuelLevelPct ?: signals?.fuelLevel ?: current.fuelLevel,
            distanceDriven = trip?.odometerKm ?: signals?.odometerKm ?: current.distanceDriven,
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
            serviceRemainingKm = maintenance?.remainingKm ?: recommendation?.remainingKm ?: current.serviceRemainingKm,
            serviceEtaMinutes = booking?.etaMinutes ?: recommendation?.etaMinutes ?: 0,
            serviceScheduledDate = booking?.scheduledDate ?: "",
            serviceScheduledTime = booking?.scheduledTime ?: "",
            serviceBookingId = booking?.bookingId ?: "",
            serviceUrgency = booking?.urgency ?: "",
            serviceRequestedDate = booking?.requestedDate ?: "",
            serviceRequestedTime = booking?.requestedTime ?: "",
            serviceBookingEditable = booking?.editable ?: false,
            vehicleLat = booking?.vehicleLat ?: recommendation?.vehicleLat ?: signals?.latitude ?: obs?.latitude ?: current.vehicleLat,
            vehicleLon = booking?.vehicleLon ?: recommendation?.vehicleLon ?: signals?.longitude ?: obs?.longitude ?: current.vehicleLon,
            dtcCount = ecuObd?.dtcCount ?: events?.dtcCount ?: current.dtcCount,
            milActive = (ecuObd?.milStatus == 1) || events?.milStatus == true,
            ignitionOn = (ecuObd?.ignitionStatus == 1) || signals?.ignitionOn == true,
            chargingActive = (ecuObd?.chargingStatus == 1) || signals?.chargingActive == true
        )

        _uiState.value = summary
        stateCache[_activeCarId] = summary
        stateCache[summary.carId] = summary
        generateAlertsAndRecommendations(
            summary,
            dashboard?.activeAlerts ?: response.activeAlerts ?: inf?.activeAlerts
        )
    }

    private fun generateAlertsAndRecommendations(state: VehicleStateSummary, backendAlerts: List<BackendAlert>? = null) {
        val mappedAlerts = backendAlerts.orEmpty().map { alert ->
            AlertItem(
                id = listOfNotNull(
                    alert.code,
                    alert.severity,
                    alert.title,
                    alert.message
                ).joinToString("|").ifBlank { "backend-alert" },
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

        val topAlert = backendAlerts.orEmpty().firstOrNull()
        val nextRecommendation = when {
            topAlert != null -> RecommendationItem(
                message = topAlert.message ?: "Backend reported a live vehicle alert.",
                actionText = when {
                    state.serviceBookingStatus != null -> "SERVICE SCHEDULED"
                    state.serviceDueNow -> "SCHEDULE SERVICE"
                    (topAlert.code ?: "").contains("COLLISION", ignoreCase = true) -> "SLOW DOWN"
                    else -> "INSPECT VEHICLE"
                },
                isCritical = (topAlert.severity ?: "").equals("CRITICAL", ignoreCase = true)
            )
            state.serviceBookingId.isNotBlank() -> RecommendationItem(
                message = "Service booking ${state.serviceBookingId} is active for ${state.serviceCenterName}.",
                actionText = "VIEW BOOKING"
            )
            state.serviceDueNow -> RecommendationItem(
                message = "Backend maintenance window is due within ${state.serviceRemainingKm} km.",
                actionText = "SCHEDULE SERVICE"
            )
            else -> RecommendationItem(
                message = "No live backend alerts. Drive mode ${state.driveModeDisplay} with ${state.rangeKm} km range remaining.",
                actionText = null
            )
        }

        if (_alerts.value != mappedAlerts) {
            _alerts.value = mappedAlerts
        }
        if (_recommendation.value != nextRecommendation) {
            _recommendation.value = nextRecommendation
        }
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
