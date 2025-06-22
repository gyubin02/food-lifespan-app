package com.example.foodman

data class AlarmNotification(

    val foodName: String,
    val expiration: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)