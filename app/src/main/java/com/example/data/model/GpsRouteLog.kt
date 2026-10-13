package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_route_logs",
    indices = [Index(value = ["dateMillis"])]
)
data class GpsRouteLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeName: String = "Caminhada ao Ar Livre",
    val activityType: String = "Caminhada",
    val dateMillis: Long = System.currentTimeMillis(),
    val dateDisplay: String,
    val durationSeconds: Int = 0,
    val distanceMeters: Float = 0f,
    val stepsCount: Int = 0,
    val caloriesBurned: Float = 0f,
    val averagePaceMinPerKm: Float = 0f,
    val routePointsJson: String = "[]",
    val notes: String = ""
)
