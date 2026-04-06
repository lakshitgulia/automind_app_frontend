

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.automind.app.data.model.BackendStateResponse

fun main() {
    val json = """
{
  "observation": {
    "speed": 17.35, "rpm": 1633.35, "throttle": 16.56, "gear": 1,
    "engine_load": 18.38, "transmission_load": 13.66, "fuel_rate": 1.46,
    "acceleration": 0.52, "engine_temp": 86.82, "distance_to_obstacle": 83.65,
    "road_condition": "dry", "drive_mode": "city", "oil_level": 81.82,
    "battery_health": 87.97, "latitude": 28.614325, "longitude": 77.208985,
    "heading": 358.2,
    "failures": {
      "brake_failure": false, "sensor_failure": false, "engine_overheating": false,
      "low_oil": false, "battery_issue": false
    },
    "history": [
      {
        "state_summary": {
          "speed": 18.0, "rpm": 1200.0, "engine_temp": 86.0,
          "distance_to_obstacle": 85.0, "oil_level": 82.0, "battery_health": 88.0
        },
        "action_taken": "continue"
      }
    ]
  },
  "reward": 0.669, "done": false,
  "metrics": {
    "safety_score": 0.976, "efficiency_score": 0.193, "diagnosis_score": 0.5, "sequence_score": 0.1
  },
  "info": {
    "outcome": "in_progress", "collision_risk": 0.024, "override_active": false,
    "health_score": 100, "alerts": [], "service_recommended": null, "service_booking": null
  }
}
"""
    try {
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter = moshi.adapter(BackendStateResponse::class.java)
        val response = adapter.fromJson(json)
        println("Success: " + response)
    } catch(e: Exception) {
        println("Exception: " + e)
        e.printStackTrace()
    }
}
