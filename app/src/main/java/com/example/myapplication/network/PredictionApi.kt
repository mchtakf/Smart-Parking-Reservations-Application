package com.example.myapplication.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Otopark Yoğunluk Tahmin API İstek Modeli
 */
data class PredictionRequest(
    val date: String,   // dd/MM/yyyy
    val hour: Int       // 0-23
)

/**
 * Otopark Yoğunluk Tahmin API Yanıt Modeli
 */
data class PredictionApiResponse(
    val vehicle_count: Int,
    val avg_park_minutes: Int,
    val density: String,
    val date: String,
    val hour: Int
)

/**
 * Sağlık Kontrolü Yanıt Modeli
 */
data class HealthResponse(
    val status: String,
    val model_loaded: Boolean
)

/**
 * Retrofit API Arayüzü
 */
interface PredictionApi {

    @POST("predict")
    suspend fun predict(@Body request: PredictionRequest): Response<PredictionApiResponse>

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}
