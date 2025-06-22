package com.example.foodman

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class ExpirationAlertWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val foodName = inputData.getString("foodName") ?: return Result.failure()
        val dayOffset = inputData.getInt("dayOffset", 0)

        val message = when (dayOffset) {
            5 -> "유통기한 5일 전입니다: $foodName"
            3 -> "유통기한 3일 전입니다: $foodName"
            1 -> "유통기한 1일 전입니다: $foodName"
            0 -> "오늘이 유통기한입니다: $foodName"
            else -> "$foodName 유통기한 알림"
        }

        sendNotification("식재료 유통기한 알림", message)
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "expiration_alert_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "유통기한 알림", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}