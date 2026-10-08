package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.DietPlan
import com.example.data.model.FoodItem
import com.example.data.model.MealPlanItem
import com.example.data.model.ProgressLog
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        UserProfile::class,
        DietPlan::class,
        FoodItem::class,
        MealPlanItem::class,
        DailyIntakeHistory::class,
        ShoppingItem::class,
        WorkoutRoutine::class,
        WorkoutExercise::class,
        WorkoutLog::class,
        ProgressLog::class,
        PurchaseReceipt::class,
        PurchaseItemLog::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    fun growthDao(): GrowthDao = appDao()
    fun userProfileDao(): UserProfileDao = appDao()
    fun dietPlanDao(): DietPlanDao = appDao()
    fun workoutRoutineDao(): WorkoutRoutineDao = appDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Initial schema upgrade support
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Version 2 to 3 migration support
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN firebaseUid TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL(
                        "CREATE TABLE IF NOT EXISTS `diet_plans` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `goal` TEXT NOT NULL, `targetCalories` INTEGER NOT NULL, `targetProteinGrams` INTEGER NOT NULL, `targetCarbsGrams` INTEGER NOT NULL, `targetFatGrams` INTEGER NOT NULL, `targetWaterMl` INTEGER NOT NULL, `mealsCount` INTEGER NOT NULL, `cycleType` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAtMillis` INTEGER NOT NULL, `notes` TEXT NOT NULL)"
                    )
                } catch (_: Exception) {}

                try {
                    db.execSQL(
                        "ALTER TABLE `meal_plan_items` ADD COLUMN `dietPlanId` INTEGER NOT NULL DEFAULT 1"
                    )
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "growth_diet_fitness.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: AppDao) {
            // Seed Profile
            dao.insertOrUpdateProfile(UserProfile())

            // Seed Active Diet Plan
            dao.insertDietPlan(PrepopulatedData.defaultDietPlan)

            // Seed Foods
            dao.insertFoods(PrepopulatedData.defaultFoods)

            // Seed Meals
            dao.insertMealPlanItems(PrepopulatedData.defaultMealPlan)

            // Seed Routines and Exercises (AB, ABC, ABCDE)
            dao.insertRoutines(PrepopulatedData.defaultRoutines)
            dao.insertExercises(PrepopulatedData.defaultExercises)

            // Seed Receipts & Purchase price history
            dao.insertReceipts(PrepopulatedData.sampleReceipts)
            dao.insertPurchaseItemLogs(PrepopulatedData.samplePurchaseLogs)

            // Seed initial Progress Log history
            val now = System.currentTimeMillis()
            val dayMillis = 86400000L
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now - (21 * dayMillis),
                    dateDisplay = dateFormat.format(Date(now - (21 * dayMillis))),
                    weightKg = 79.8f,
                    bodyFatPercentage = 15.5f,
                    waistCm = 84f,
                    chestCm = 101f,
                    notes = "Início do protocolo Growth Bulking Limpo"
                )
            )
            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now - (7 * dayMillis),
                    dateDisplay = dateFormat.format(Date(now - (7 * dayMillis))),
                    weightKg = 79.1f,
                    bodyFatPercentage = 14.8f,
                    waistCm = 83f,
                    chestCm = 102f,
                    notes = "Semana 2: aumento de cargas no Supino e Agachamento"
                )
            )
            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now,
                    dateDisplay = dateFormat.format(Date(now)),
                    weightKg = 78.5f,
                    bodyFatPercentage = 14.2f,
                    waistCm = 82f,
                    chestCm = 103f,
                    notes = "Semana 3: definição muscular visível e excelente recuperação"
                )
            )
        }
    }
}

typealias GrowthDatabase = AppDatabase
