package com.automind.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendStateResponse(
    @Json(name = "observation") val observation: Observation? = null,
    @Json(name = "metrics") val metrics: Metrics? = null,
    @Json(name = "info") val info: StepInfo? = null,
    @Json(name = "reward") val reward: Double? = null,
    @Json(name = "done") val done: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Observation(
    @Json(name = "speed") val speed: Double? = null,
    @Json(name = "rpm") val rpm: Double? = null,
    @Json(name = "throttle") val throttle: Double? = null,
    @Json(name = "gear") val gear: Int? = null,
    @Json(name = "engine_temp") val engineTemp: Double? = null,
    @Json(name = "distance_to_obstacle") val distanceToObstacle: Double? = null,
    @Json(name = "road_condition") val roadCondition: String? = null,
    @Json(name = "oil_level") val oilLevel: Double? = null,
    @Json(name = "battery_health") val batteryHealth: Double? = null,
    @Json(name = "engine_load") val engineLoad: Double? = null,
    @Json(name = "drive_mode") val driveMode: String? = null,
    @Json(name = "failures") val failures: FailureFlags? = null,
    @Json(name = "history") val history: List<HistoryItem>? = null
)

@JsonClass(generateAdapter = true)
data class HistoryItem(
    @Json(name = "state_summary") val stateSummary: Map<String, Double>? = null,
    @Json(name = "action_taken") val actionTaken: String? = null
)

@JsonClass(generateAdapter = true)
data class FailureFlags(
    @Json(name = "brake_failure") val brakeFailure: Boolean? = null,
    @Json(name = "sensor_failure") val sensorFailure: Boolean? = null,
    @Json(name = "engine_overheating") val engineOverheating: Boolean? = null,
    @Json(name = "low_oil") val lowOil: Boolean? = null,
    @Json(name = "battery_issue") val batteryIssue: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class Metrics(
    @Json(name = "safety_score") val safetyScore: Double? = null,
    @Json(name = "efficiency_score") val efficiencyScore: Double? = null,
    @Json(name = "diagnosis_score") val diagnosisScore: Double? = null,
    @Json(name = "sequence_score") val sequenceScore: Double? = null
)

@JsonClass(generateAdapter = true)
data class StepInfo(
    @Json(name = "outcome") val outcome: String? = null,
    @Json(name = "collision_risk") val collisionRisk: Double? = null,
    @Json(name = "override_active") val overrideActive: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class StepRequest(
    @Json(name = "action_type") val actionType: String,
    @Json(name = "value") val value: Double,
    @Json(name = "reason") val reason: String
)

// UI Friendly Data Models
data class VehicleStateSummary(
    val speed: Double = 0.0,
    val engineTemp: Double = 0.0,
    val rpm: Double = 0.0,
    val throttle: Double = 0.0,
    val gear: Int = 1,
    val forwardDistance: Double = 0.0,
    val roadCondition: String = "Unknown",
    val oilHealth: Double = 100.0,
    val batteryHealth: Double = 100.0,
    val engineLoad: Double = 0.0,
    val driveMode: String = "Unknown",
    
    val brakeSystemStatus: Boolean = false,
    val sensorSystemStatus: Boolean = false,
    val engineHeatAlert: Boolean = false,
    val oilWarning: Boolean = false,
    val batteryAlert: Boolean = false,
    
    val safetyScore: Double = 1.0,
    val efficiencyScore: Double = 1.0,
    val diagnosticConfidence: Double = 1.0,
    val decisionStability: Double = 1.0,
    
    val collisionRisk: Double = 0.0,
    val overrideDetected: Boolean = false,
    val vehicleStatus: String = "Stable"
)

enum class AlertPriority { CRITICAL, WARNING, SAFETY }

data class AlertItem(
    val id: String,
    val title: String,
    val message: String,
    val priority: AlertPriority,
    val timestamp: String = "Just now"
)

data class RecommendationItem(
    val message: String,
    val actionText: String?,
    val isCritical: Boolean = false
)

// User & Vehicle Management Models
data class UserProfile(
    val name: String = "",
    val email: String = "",
    val isLoggedIn: Boolean = false
)

data class VehicleInfo(
    val id: String = "",
    val make: String = "",
    val model: String = "",
    val year: Int = 2023,
    val licensePlate: String = "",
    val isPrimary: Boolean = false,
    val fuelLevel: Double = 84.0,
    val distanceDriven: Double = 12450.0
)
