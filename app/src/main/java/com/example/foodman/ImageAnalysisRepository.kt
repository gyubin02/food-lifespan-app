package com.example.foodman


import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object  ImageAnalysisRepository {

    interface ImageAnalysisApi {
        @POST("/api/image-analysis/analyze/base64")
        suspend fun analyzeImage(@Body request: Map<String, String>): Response<Map<String, String>>
    }

    private val api = Retrofit.Builder()
        .baseUrl("http://192.168.0.17:8080") // 서버 바꾸기!!!!! 기준 localhost
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImageAnalysisApi::class.java)

    suspend fun analyzeImageAndGetExpiryDateMap(base64Image: String): Map<String, String> {
        val expiryMap = getExpiryDaysFromServer(base64Image)
        return calculateExpiryDates(expiryMap)
    }


    private suspend fun getExpiryDaysFromServer(base64Image: String): Map<String, String> {
        return try {
            val request = mapOf("imageData" to base64Image)
            val response = api.analyzeImage(request)
            Log.d("GeminiTest", "서버 응답 코드: ${response.code()}")
            Log.d("GeminiTest", "서버 응답 메시지: ${response.message()}")
            if (response.isSuccessful) {
                response.body() ?: emptyMap()
            } else {
                Log.e("GeminiTest", "서버 응답 실패: ${response.errorBody()?.string()}")
                emptyMap()
            }
        } catch (e: Exception) {
            Log.e("GeminiTest", "서버 요청 실패: ${e.message}", e)
            emptyMap()
        }
    }

    private fun calculateExpiryDates(expiryDaysMap: Map<String, String>): Map<String, String> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val result = mutableMapOf<String, String>()
        for ((ingredient, daysStr) in expiryDaysMap) {
            val days = daysStr.toIntOrNull()
            val expiryDate = if (days != null) {
                today.plusDays(days.toLong()).format(formatter)
            } else {
                "유통기한 정보 없음"
            }
            result[ingredient] = expiryDate
        }
        return result
    }
}