package com.example.foodman

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotificationHistoryManager {
    private const val PREF_NAME = "alarm_history"
    private const val KEY_HISTORY = "history_list"

    private val gson = Gson()

    // 알림 저장 (추가)
    fun addAlarm(context: Context, alarm: AlarmNotification) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = getAllAlarms(context).toMutableList()
        current.add(0, alarm) // 최신순으로
        prefs.edit().putString(KEY_HISTORY, gson.toJson(current)).apply()
    }

    // 알림 전체 가져오기
    fun getAllAlarms(context: Context): List<AlarmNotification> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val type = object : TypeToken<List<AlarmNotification>>() {}.type
        return gson.fromJson(json, type)
    }

    // (선택) 알림 내역 지우기
    fun clearAlarms(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
