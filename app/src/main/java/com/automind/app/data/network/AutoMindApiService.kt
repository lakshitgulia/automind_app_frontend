package com.automind.app.data.network

import com.automind.app.data.model.BackendStateResponse
import com.automind.app.data.model.StepRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Headers

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RootResponse(val status: String? = null)

interface AutoMindApiService {
    
    @GET("/")
    suspend fun getRoot(): RootResponse

    @POST("/reset")
    suspend fun resetSession(
        @Query("car_id") carId: String = "default",
        @Body payload: Map<String, String> = emptyMap()
    ): BackendStateResponse

    @POST("/step")
    suspend fun executeStep(
        @Body request: StepRequest,
        @Query("car_id") carId: String = "default"
    ): BackendStateResponse

    @Headers("Cache-Control: no-cache, no-store, must-revalidate", "Pragma: no-cache", "Expires: 0")
    @GET("/state")
    suspend fun getState(
        @Query("t") timestamp: Long,
        @Query("car_id") carId: String = "default"
    ): BackendStateResponse

    @GET("/health")
    suspend fun getHealth(
        @Query("car_id") carId: String = "default"
    ): Any

    @GET("/tasks")
    suspend fun getTasks(): List<String>
    
    @GET("/schema")
    suspend fun getSchema(): Any
}
