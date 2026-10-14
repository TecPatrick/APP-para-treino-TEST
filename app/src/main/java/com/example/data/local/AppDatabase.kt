package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(entities = [
    DailyIntakeHistory::class,
    DietPlan::class,
    FoodItem::class,
    FoodRecord::class,
    MealPlanItem::class,
    ProgressLog::class,
    PurchaseItemLog::class,
    PurchaseReceipt::class,
    ShoppingItem::class,
    UserProfile::class,
    GpsRouteLog::class,
    WeeklyDietPlan::class,
    WorkoutExercise::class,
    WorkoutLog::class,
    WorkoutPlan::class,
    WorkoutRoutine::class,
    DietaryProfile::class,
    Macro::class,
    WorkoutSchedule::class,
    CardioLog::class
], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
