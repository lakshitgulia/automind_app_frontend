package com.automind.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.automind.app.data.model.VehicleInfo
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class VehiclePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("automind_vehicle_prefs", Context.MODE_PRIVATE)

    private val userPrefs: SharedPreferences =
        context.getSharedPreferences("automind_user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_EMAIL_KEY = "current_email"
        private const val KEY_ACCOUNTS = "accounts_json"
    }

    fun saveVehicle(vehicle: VehicleInfo) {
        val vehicles = getVehicles().toMutableList()
        val hash = vehicle.licensePlate.uppercase().hashCode().toLong().let { kotlin.math.abs(it) }
        val seededFuel = 35.0 + (hash % 55).toDouble()
        val seededDistance = 8000.0 + (hash % 145000).toDouble()
        val vehicleToSave = if (vehicles.isEmpty()) {
            vehicle.copy(
                isPrimary = true,
                id = UUID.randomUUID().toString(),
                fuelLevel = seededFuel,
                distanceDriven = seededDistance
            )
        } else {
            vehicle.copy(
                id = UUID.randomUUID().toString(),
                fuelLevel = seededFuel,
                distanceDriven = seededDistance
            )
        }
        vehicles.add(vehicleToSave)
        saveVehicleList(vehicles)
    }

    fun getVehicles(): List<VehicleInfo> {
        val json = prefs.getString(activeVehiclesKey(), null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                VehicleInfo(
                    id = obj.optString("id", ""),
                    make = obj.optString("make", ""),
                    model = obj.optString("model", ""),
                    year = obj.optInt("year", 2023),
                    licensePlate = obj.optString("licensePlate", ""),
                    isPrimary = obj.optBoolean("isPrimary", false),
                    fuelLevel = obj.optDouble("fuelLevel", 84.0),
                    distanceDriven = obj.optDouble("distanceDriven", 12450.0)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getPrimaryVehicle(): VehicleInfo? {
        return getVehicles().firstOrNull { it.isPrimary } ?: getVehicles().firstOrNull()
    }

    fun removeVehicle(id: String) {
        val remainingVehicles = getVehicles().filter { it.id != id }
        val normalizedVehicles = if (remainingVehicles.isNotEmpty() && remainingVehicles.none { it.isPrimary }) {
            remainingVehicles.mapIndexed { index, vehicle ->
                vehicle.copy(isPrimary = index == 0)
            }
        } else {
            remainingVehicles
        }
        saveVehicleList(normalizedVehicles)
    }

    fun setPrimaryVehicle(id: String) {
        val vehicles = getVehicles().map {
            it.copy(isPrimary = it.id == id)
        }
        saveVehicleList(vehicles)
    }

    fun hasVehicles(): Boolean = getVehicles().isNotEmpty()

    fun deleteVehiclesForEmail(email: String?) {
        val normalizedEmail = email?.trim()?.lowercase().orEmpty()
        val key = if (normalizedEmail.isBlank()) {
            "vehicles_guest"
        } else {
            "vehicles_${sanitizeEmail(normalizedEmail)}"
        }
        prefs.edit().remove(key).apply()
    }

    private fun saveVehicleList(vehicles: List<VehicleInfo>) {
        val array = JSONArray()
        vehicles.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("make", v.make)
                put("model", v.model)
                put("year", v.year)
                put("licensePlate", v.licensePlate)
                put("isPrimary", v.isPrimary)
                put("fuelLevel", v.fuelLevel)
                put("distanceDriven", v.distanceDriven)
            }
            array.put(obj)
        }
        prefs.edit().putString(activeVehiclesKey(), array.toString()).apply()
        pruneStaleVehicleBuckets()
    }

    private fun pruneStaleVehicleBuckets() {
        val accountsJson = userPrefs.getString(KEY_ACCOUNTS, null) ?: return
        val allowed = mutableSetOf("vehicles_guest")
        try {
            val array = JSONArray(accountsJson)
            for (index in 0 until array.length()) {
                val email = array.getJSONObject(index).optString("email", "")
                if (email.isNotBlank()) {
                    val sanitized = sanitizeEmail(email)
                    allowed.add("vehicles_$sanitized")
                }
            }
        } catch (_: Exception) {
            return
        }

        val editor = prefs.edit()
        prefs.all.keys
            .filter { it.startsWith("vehicles_") && it !in allowed }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    private fun activeVehiclesKey(): String {
        val email = userPrefs.getString(USER_EMAIL_KEY, null)?.trim()?.lowercase().orEmpty()
        if (email.isBlank()) return "vehicles_guest"
        return "vehicles_${sanitizeEmail(email)}"
    }

    private fun sanitizeEmail(email: String): String = buildString {
        email.forEach { ch ->
            append(if (ch.isLetterOrDigit()) ch else '_')
        }
    }
}
