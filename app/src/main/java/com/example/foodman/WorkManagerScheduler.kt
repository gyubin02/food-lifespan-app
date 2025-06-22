package com.example.foodman

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.foodmanMore.ExpirationAlertWorker
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    fun scheduleExpirationAlerts(context: Context, foodName: String, expirationDate: String) {
        val daysBeforeList = listOf(5, 3, 1, 0)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val expDate = try {
            dateFormat.parse(expirationDate)
        } catch (e: Exception) {
            null
        } ?: return

        for (offset in daysBeforeList) {
            val alertTime = Calendar.getInstance().apply {
                time = expDate
                add(Calendar.DATE, -offset)
            }.timeInMillis

            val delayMillis = alertTime - System.currentTimeMillis()
            if (delayMillis <= 0) continue  // 이미 지난 시간은 무시
            Log.d("WorkManagerTest", "푸시 예약: $foodName, $offset, delay=$delayMillis(ms)")
            val data = Data.Builder()
                .putString("foodName", foodName)
                .putString("expiration", expirationDate) // ← 새로 추가!
                .putInt("dayOffset", offset)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ExpirationAlertWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}