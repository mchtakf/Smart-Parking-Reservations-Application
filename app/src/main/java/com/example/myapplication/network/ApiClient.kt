package com.example.myapplication.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit API İstemcisi - Singleton
 *
 * BASE_URL'i kendi sunucu adresinize göre değiştirin:
 *   - Emülatör:  http://10.0.2.2:8000/
 *   - Fiziksel:  http://<SUNUCU_IP>:8000/
 *   - Production: https://api.example.com/
 */
object ApiClient {

    // ⚠️ Bu URL'yi kendi backend sunucunuzun adresine göre güncelleyin
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val predictionApi: PredictionApi = retrofit.create(PredictionApi::class.java)
}
