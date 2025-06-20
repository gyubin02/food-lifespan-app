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

    /*private suspend fun getExpiryDaysFromServer(base64Image: String): Map<String, String> {
        return try {
            val request = mapOf("imageData" to base64Image)
            val response = api.analyzeImage(request)
            if (response.isSuccessful) {
                response.body() ?: emptyMap()
            } else {

                emptyMap()
            }
        } catch (e: Exception) {

            emptyMap()
        }
    }*/
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

/*import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume

object ImageAnalysisRepository {

    // Retrofit API 인터페이스 정의
    interface ImageAnalysisApi {
        @POST("/api/image-analysis/analyze/base64")
        suspend fun analyzeImage(@Body request: Map<String, String>): Response<List<String>>
    }

    // Retrofit 설정
    private val api = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080") // ← 실제 서버 주소
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ImageAnalysisApi::class.java)

    private val firebaseRef = FirebaseDatabase.getInstance().getReference("food")

    suspend fun analyzeImageAndGetExpiryMap(base64Image: String): Map<String, String> {
        val ingredients = getIngredientsFromServer(base64Image)
        return getCalculatedExpiryDates(ingredients)
    }

    private suspend fun getIngredientsFromServer(base64Image: String): List<String> {
        return try {
            val request = mapOf("imageData" to base64Image)
            val response = api.analyzeImage(request)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun getCalculatedExpiryDates(ingredients: List<String>): Map<String, String> {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return suspendCancellableCoroutine { continuation ->
            firebaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val result = mutableMapOf<String, String>()

                    for (ingredient in ingredients) {
                        val days = snapshot.child(ingredient).getValue(String::class.java)?.toIntOrNull()
                        val expiryDate = if (days != null) {
                            today.plusDays(days.toLong()).format(formatter)
                        } else {
                            "유통기한 정보 없음"
                        }
                        result[ingredient] = expiryDate
                    }

                    continuation.resume(result)
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resume(emptyMap())
                }
            })
        }
    }
}
*/