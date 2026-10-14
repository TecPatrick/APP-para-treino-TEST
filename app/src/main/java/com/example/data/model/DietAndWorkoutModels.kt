package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dietary_profiles")
data class DietaryProfile(
    @PrimaryKey val id: Int = 0,
    val name: String,
    val targetCalories: Int,
    val proteinGoal: Int,
    val carbGoal: Int,
    val fatGoal: Int
)

@Entity(tableName = "macros")
data class Macro(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long, 
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

@Entity(tableName = "workout_schedules")
data class WorkoutSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String,
    val activityType: String,
    val durationMinutes: Int,
    val intensity: String
)
