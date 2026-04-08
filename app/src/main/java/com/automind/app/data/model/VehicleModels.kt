package com.automind.app.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackendStateResponse(
    @Json(name = "vehicle") val vehicle: VehicleIdentity? = null,
    @Json(name = "dashboard") val dashboard: DashboardPayload? = null,
    @Json(name = "quick_telemetry") val quickTelemetry: QuickTelemetry? = null,
    @Json(name = "trip") val trip: TripSummary? = null,
    @Json(name = "health") val health: HealthSummary? = null,
    @Json(name = "ml_predictions") val mlPredictions: MlPredictions? = null,
    @Json(name = "active_alerts") val activeAlerts: List<BackendAlert>? = null,
    @Json(name = "observation") val observation: Observation? = null,
    @Json(name = "metrics") val metrics: Metrics? = null,
    @Json(name = "info") val info: StepInfo? = null,
    @Json(name = "reward") val reward: Double? = null,
    @Json(name = "done") val done: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class VehicleIdentity(
    @Json(name = "car_id") val carId: String? = null,
    @Json(name = "vin") val vin: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "maker") val maker: String? = null,
    @Json(name = "status") val status: String? = null
)

@JsonClass(generateAdapter = true)
data class DashboardPayload(
    @Json(name = "vehicle") val vehicle: VehicleIdentity? = null,
    @Json(name = "quick_telemetry") val quickTelemetry: QuickTelemetry? = null,
    @Json(name = "trip") val trip: TripSummary? = null,
    @Json(name = "health") val health: HealthSummary? = null,
    @Json(name = "safety") val safety: SafetySummary? = null,
    @Json(name = "ml_predictions") val mlPredictions: MlPredictions? = null,
    @Json(name = "maintenance") val maintenance: MaintenanceSummary? = null,
    @Json(name = "active_alerts") val activeAlerts: List<BackendAlert>? = null
)

@JsonClass(generateAdapter = true)
data class QuickTelemetry(
    @Json(name = "speed_kmph") val speedKmph: Double? = null,
    @Json(name = "engine_temp_c") val engineTempC: Double? = null,
    @Json(name = "battery_pct") val batteryPct: Int? = null,
    @Json(name = "battery_v") val batteryV: Double? = null
)

@JsonClass(generateAdapter = true)
data class TripSummary(
    @Json(name = "drive_mode") val driveMode: String? = null,
    @Json(name = "drive_mode_display") val driveModeDisplay: String? = null,
    @Json(name = "range_km") val rangeKm: Int? = null,
    @Json(name = "fuel_level_pct") val fuelLevelPct: Double? = null,
    @Json(name = "odometer_km") val odometerKm: Double? = null,
    @Json(name = "gear_display") val gearDisplay: String? = null
)

@JsonClass(generateAdapter = true)
data class HealthSummary(
    @Json(name = "overall_score") val overallScore: Int? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "engine_health") val engineHealth: Int? = null,
    @Json(name = "battery_health") val batteryHealth: Int? = null,
    @Json(name = "safety_health") val safetyHealth: Int? = null,
    @Json(name = "maintenance_health") val maintenanceHealth: Int? = null,
    @Json(name = "engine_status") val engineStatus: String? = null,
    @Json(name = "predicted_failure") val predictedFailure: String? = null,
    @Json(name = "predicted_failure_risk_pct") val predictedFailureRiskPct: Int? = null
)

@JsonClass(generateAdapter = true)
data class MlPredictions(
    @Json(name = "model_name") val modelName: String? = null,
    @Json(name = "confidence") val confidence: Double? = null,
    @Json(name = "primary_failure") val primaryFailure: String? = null,
    @Json(name = "failure_horizon_km") val failureHorizonKm: Int? = null,
    @Json(name = "failure_risks") val failureRisks: FailureRiskMap? = null,
    @Json(name = "health") val health: PredictedHealth? = null
)

@JsonClass(generateAdapter = true)
data class FailureRiskMap(
    @Json(name = "engine_overheating") val engineOverheating: Double? = null,
    @Json(name = "low_oil") val lowOil: Double? = null,
    @Json(name = "battery_issue") val batteryIssue: Double? = null,
    @Json(name = "brake_failure") val brakeFailure: Double? = null,
    @Json(name = "collision") val collision: Double? = null
)

@JsonClass(generateAdapter = true)
data class PredictedHealth(
    @Json(name = "overall") val overall: Int? = null,
    @Json(name = "engine") val engine: Int? = null,
    @Json(name = "battery") val battery: Int? = null,
    @Json(name = "safety") val safety: Int? = null,
    @Json(name = "maintenance") val maintenance: Int? = null
)

@JsonClass(generateAdapter = true)
data class SafetySummary(
    @Json(name = "driving_safety_score") val drivingSafetyScore: Int? = null,
    @Json(name = "collision_risk_pct") val collisionRiskPct: Int? = null,
    @Json(name = "distance_to_obstacle_m") val distanceToObstacleM: Double? = null
)

@JsonClass(generateAdapter = true)
data class MaintenanceSummary(
    @Json(name = "service_due_now") val serviceDueNow: Boolean? = null,
    @Json(name = "remaining_km") val remainingKm: Int? = null,
    @Json(name = "next_due_km") val nextDueKm: Int? = null,
    @Json(name = "service_recommended") val serviceRecommended: ServicePayload? = null,
    @Json(name = "service_booking") val serviceBooking: ServiceBooking? = null
)

@JsonClass(generateAdapter = true)
data class ServicePayload(
    @Json(name = "name") val name: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "phone") val phone: String? = null,
    @Json(name = "lat") val lat: Double? = null,
    @Json(name = "lon") val lon: Double? = null,
    @Json(name = "distance_km") val distanceKm: Double? = null,
    @Json(name = "eta_minutes") val etaMinutes: Int? = null,
    @Json(name = "remaining_km") val remainingKm: Int? = null,
    @Json(name = "vehicle_lat") val vehicleLat: Double? = null,
    @Json(name = "vehicle_lon") val vehicleLon: Double? = null
)

@JsonClass(generateAdapter = true)
data class ServiceBooking(
    @Json(name = "status") val status: String? = null,
    @Json(name = "booking_id") val bookingId: String? = null,
    @Json(name = "center_name") val centerName: String? = null,
    @Json(name = "center_address") val centerAddress: String? = null,
    @Json(name = "center_phone") val centerPhone: String? = null,
    @Json(name = "distance_km") val distanceKm: Double? = null,
    @Json(name = "eta_minutes") val etaMinutes: Int? = null,
    @Json(name = "urgency") val urgency: String? = null,
    @Json(name = "scheduled_at") val scheduledAt: String? = null,
    @Json(name = "scheduled_date") val scheduledDate: String? = null,
    @Json(name = "scheduled_time") val scheduledTime: String? = null,
    @Json(name = "requested_date") val requestedDate: String? = null,
    @Json(name = "requested_time") val requestedTime: String? = null,
    @Json(name = "editable") val editable: Boolean? = null,
    @Json(name = "car_id") val carId: String? = null,
    @Json(name = "vehicle_name") val vehicleName: String? = null,
    @Json(name = "service_center_lat") val serviceCenterLat: Double? = null,
    @Json(name = "service_center_lon") val serviceCenterLon: Double? = null,
    @Json(name = "vehicle_lat") val vehicleLat: Double? = null,
    @Json(name = "vehicle_lon") val vehicleLon: Double? = null
)

@JsonClass(generateAdapter = true)
data class BackendAlert(
    @Json(name = "code") val code: String? = null,
    @Json(name = "severity") val severity: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "component") val component: String? = null
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
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null,
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
    @Json(name = "override_active") val overrideActive: Boolean? = null,
    @Json(name = "health_score") val healthScore: Int? = null,
    @Json(name = "alerts") val alerts: List<String>? = null,
    @Json(name = "service_booking") val serviceBooking: ServiceBooking? = null,
    @Json(name = "ml_predictions") val mlPredictions: MlPredictions? = null,
    @Json(name = "fault_phase") val faultPhase: String? = null
)

@JsonClass(generateAdapter = true)
data class StepRequest(
    @Json(name = "action_type") val actionType: String,
    @Json(name = "value") val value: Double,
    @Json(name = "reason") val reason: String
)

// UI Friendly Data Models
data class VehicleStateSummary(
    val carId: String = "default",
    val vehicleDisplayName: String = "Vehicle",
    val vin: String = "",
    val speed: Double = 0.0,
    val engineTemp: Double = 0.0,
    val rpm: Double = 0.0,
    val throttle: Double = 0.0,
    val gear: Int = 1,
    val gearDisplay: String = "D1 Auto",
    val forwardDistance: Double = 0.0,
    val rangeKm: Int = 0,
    val roadCondition: String = "Unknown",
    val oilHealth: Double = 100.0,
    val batteryHealth: Double = 100.0,
    val engineLoad: Double = 0.0,
    val driveMode: String = "Unknown",
    val driveModeDisplay: String = "Unknown",
    
    val brakeSystemStatus: Boolean = false,
    val sensorSystemStatus: Boolean = false,
    val engineHeatAlert: Boolean = false,
    val oilWarning: Boolean = false,
    val batteryAlert: Boolean = false,
    val engineStatus: String = "NORMAL",
    
    val safetyScore: Double = 1.0,
    val efficiencyScore: Double = 1.0,
    val diagnosticConfidence: Double = 1.0,
    val decisionStability: Double = 1.0,
    val healthScore: Int = 100,
    
    val collisionRisk: Double = 0.0,
    val overrideDetected: Boolean = false,
    val vehicleStatus: String = "Stable",
    val fuelLevel: Double = 0.0,
    val distanceDriven: Double = 0.0,
    val serviceDueNow: Boolean = false,
    val serviceBookingStatus: String? = null,
    val predictedFailure: String = "stable",
    val predictedFailureRiskPct: Int = 0,
    val predictionConfidence: Double = 0.0,
    val failureHorizonKm: Int = 0,
    val predictiveModelName: String = "",
    val predictedEngineRisk: Double = 0.0,
    val predictedOilRisk: Double = 0.0,
    val predictedBatteryRisk: Double = 0.0,
    val predictedBrakeRisk: Double = 0.0,
    val predictedCollisionRisk: Double = 0.0,
    val currentFaultPhase: String = "steady state",
    val nextLikelyRiskShift: String = "none",
    val serviceCenterName: String = "",
    val serviceCenterAddress: String = "",
    val serviceCenterPhone: String = "",
    val serviceDistanceKm: Double = 0.0,
    val serviceEtaMinutes: Int = 0,
    val serviceScheduledDate: String = "",
    val serviceScheduledTime: String = "",
    val serviceBookingId: String = "",
    val serviceUrgency: String = "",
    val serviceRequestedDate: String = "",
    val serviceRequestedTime: String = "",
    val serviceBookingEditable: Boolean = false,
    val vehicleLat: Double = 0.0,
    val vehicleLon: Double = 0.0
)

enum class AlertPriority { CRITICAL, WARNING, SAFETY, INFO }

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
