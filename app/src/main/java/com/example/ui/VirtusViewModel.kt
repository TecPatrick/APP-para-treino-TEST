package com.example.ui

import android.app.Application
import com.example.data.model.MealType
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine

data class DailyEnergyExpenditure(
    val bmr: Int,
    val gymWorkoutCalories: Int,
    val runningWorkoutCalories: Int,
    val cardioWorkoutCalories: Int,
    val totalWorkoutCalories: Int,
    val totalExpenditure: Int,
    val hasWorkoutToday: Boolean,
    val gymWorkoutsCount: Int,
    val runningWorkoutsCount: Int,
    val cardioWorkoutsCount: Int
)

data class ActiveWorkoutSession(
    val routine: WorkoutRoutine? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentExerciseIndex: Int = 0,
    val completedSetsCount: Int = 0,
    val restTimerRemainingSeconds: Int = 0,
    val restTimerTotalSeconds: Int = 90,
    val isRestTimerRunning: Boolean = false,
    val isRestTimerFinished: Boolean = false,
    val isWorkoutSoundEnabled: Boolean = true,
    val isWorkoutHapticEnabled: Boolean = true,
    val loggedExerciseSets: Map<Long, List<Pair<Int, Float>>> = emptyMap()
)

data class ActiveGpsSession(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isAutoPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val distanceMeters: Float = 0f,
    val stepsCount: Int = 0,
    val caloriesBurned: Float = 0f,
    val currentSpeedKmh: Float = 0f,
    val paceSecondsPerKm: Long = 0L,
    val targetPaceSecondsPerKm: Long = 0L,
    val isPaceAlertEnabled: Boolean = false,
    val routePoints: List<Pair<Double, Double>> = emptyList()
)

data class DietAiPreferences(
    val restrictions: String = "",
    val preferences: String = "",
    val mealsCount: Int = 5,
    val includePreferredFoods: Boolean = true
)

data class AiDietSuggestion(
    val mealType: MealType,
    val foodName: String,
    val amount: Float,
    val unit: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val notes: String = ""
)

/**
 * Lightweight coordinator ViewModel kept for shared UI model types;
 * domain responsibilities are split across:
 * - [AuthViewModel]
 * - [WorkoutViewModel]
 * - [DietViewModel]
 * - [ShoppingViewModel]
 * - [CardioViewModel]
 * - [SyncViewModel]
 */
class VirtusViewModel(application: Application) : BaseVirtusDomainViewModel(application)
