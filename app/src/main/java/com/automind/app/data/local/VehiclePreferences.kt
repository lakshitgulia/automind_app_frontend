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

    companion object {
        private const val KEY_VEHICLES = "vehicles_json"
    }

    fun saveVehicle(vehicle: VehicleInfo) {
        val vehicles = getVehicles().toMutableList()
        val hash = vehicle.licensePlate.uppercase().hashCode().toLong().let { kotlin.math.abs(it) }
        val seededFuel = 35.0 + (hash % 55).toDouble()
        val seededDistance = 8000.0 + (hash % 145000).toDouble()
        // If this is the first vehicle, make it primary
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
        val json = prefs.getString(KEY_VEHICLES, null) ?: return emptyList()
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
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getPrimaryVehicle(): VehicleInfo? {
        return getVehicles().firstOrNull { it.isPrimary } ?: getVehicles().firstOrNull()
    }

    fun removeVehicle(id: String) {
        val vehicles = getVehicles().filter { it.id != id }
        saveVehicleList(vehicles)
    }

    fun setPrimaryVehicle(id: String) {
        val vehicles = getVehicles().map {
            it.copy(isPrimary = it.id == id)
        }
        saveVehicleList(vehicles)
    }

    fun hasVehicles(): Boolean = getVehicles().isNotEmpty()

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
        prefs.edit().putString(KEY_VEHICLES, array.toString()).apply()
    }
}
