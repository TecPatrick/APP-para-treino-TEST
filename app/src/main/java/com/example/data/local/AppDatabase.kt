package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
], version = 3, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
    fun growthDao(): AppDao = appDao()
    fun userProfileDao(): AppDao = appDao()
    fun dietPlanDao(): AppDao = appDao()
    fun workoutRoutineDao(): AppDao = appDao()

    companion object {
        // The last v1 commit and v2 have identical entity definitions.
        // Room validates the full schema after this migration; unknown older v1
        // layouts fail safely rather than destroying the user's database.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_profile SET passwordHash = '' WHERE passwordHash != ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isFixed to meal_plan_items (fixes Turn 1 error)
                db.execSQL("ALTER TABLE meal_plan_items ADD COLUMN isFixed INTEGER NOT NULL DEFAULT 0")
                // Add isFavorite to food_items (for New Feature)
                db.execSQL("ALTER TABLE food_items ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            // Older versions stored the raw password in this legacy column.
                            db.execSQL("UPDATE user_profile SET passwordHash = '' WHERE passwordHash != ''")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

typealias GrowthDatabase = AppDatabase

