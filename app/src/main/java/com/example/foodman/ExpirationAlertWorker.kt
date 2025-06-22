package com.example.foodmanMore

import com.example.foodman.R



import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.foodman.AlarmNotification
import com.example.foodman.MainActivity
import com.example.foodman.NotificationHistoryManager

class ExpirationAlertWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val foodName = inputData.getString("foodName") ?: return Result.failure()
        val dayOffset = inputData.getInt("dayOffset", 0)
        val expiration = inputData.getString("expiration") ?: ""

        val message = when (dayOffset) {
            5 -> "유통/소비기한이 5일 남은 상품이 있습니다.: $foodName"
            3 -> "유통/소비기한이 3일 남은 상품이 있습니다.: $foodName"
            1 -> "유통/소비기한 1일 남은 상품이 있습니다.: $foodName"
            0 -> "유통/소비기한이 금일까지인 상품이 있습니다.: $foodName"
            else -> "$foodName 유통기한 알림"
        }

        sendNotification("식재료 유통기한 알림", message)

        // (여기!) 알림 기록 저장
        val alarmData = AlarmNotification(

            foodName = foodName,
            expiration = expiration, // 만약 같이 전달되면 넣기
            message = message
        )
        NotificationHistoryManager.addAlarm(applicationContext, alarmData)


        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = "expiration_alert_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "유통기한 알림", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        // === 여기서부터 추가! ===
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 만약 특정 탭(알람화면)으로 이동하고 싶다면 intent.putExtra("navigateTo", "alarm") 추가
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // === 여기까지 추가! ===



        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent) //  클릭 시 앱으로!
            .setAutoCancel(true)             // 클릭 시 알림 제거
            .build()

        manager.notify((System.currentTimeMillis() % 100000).toInt(), notification)
    }
}