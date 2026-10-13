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
import com.example.data.model.FoodRecord
import com.example.data.model.MealPlanItem
import com.example.data.model.ProgressLog
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.GpsRouteLog
import com.example.data.model.WeeklyDietPlan
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutPlan
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
        WeeklyDietPlan::class,
        FoodItem::class,
        FoodRecord::class,
        MealPlanItem::class,
        DailyIntakeHistory::class,
        ShoppingItem::class,
        WorkoutPlan::class,
        WorkoutRoutine::class,
        WorkoutExercise::class,
        WorkoutLog::class,
        ProgressLog::class,
        PurchaseReceipt::class,
        PurchaseItemLog::class,
        GpsRouteLog::class
    ],
    version = 11,
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

        private fun ensureUserProfileSchema(db: SupportSQLiteDatabase) {
            try {
                val cursor = db.query("PRAGMA table_info(user_profile)")
                val existingColumns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }
                cursor.close()

                val expectedColumns = listOf(
                    "id", "name", "email", "firebaseUid", "passwordHash", "isLoggedIn",
                    "hasCompletedOnboarding", "lgpdConsentAccepted", "lgpdConsentDate",
                    "gender", "age", "weightKg", "heightCm", "biotype", "trainingDaysPerWeek",
                    "activityLevel", "goal", "dietCycleType", "isAthleteOrWarmClimate",
                    "targetCalories", "targetProteinGrams", "targetCarbsGrams", "targetFatGrams",
                    "targetWaterMl", "monthlyTargetWeightKg", "monthlyTargetDurationWeeks", "monthlyNotes",
                    "isDarkMode"
                )

                if (!existingColumns.containsAll(expectedColumns) || existingColumns.size != expectedColumns.size) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `user_profile_temp` (
                            `id` INTEGER NOT NULL,
                            `name` TEXT NOT NULL,
                            `email` TEXT NOT NULL,
                            `firebaseUid` TEXT NOT NULL,
                            `passwordHash` TEXT NOT NULL,
                            `isLoggedIn` INTEGER NOT NULL,
                            `hasCompletedOnboarding` INTEGER NOT NULL,
                            `lgpdConsentAccepted` INTEGER NOT NULL,
                            `lgpdConsentDate` INTEGER NOT NULL,
                            `gender` TEXT NOT NULL,
                            `age` INTEGER NOT NULL,
                            `weightKg` REAL NOT NULL,
                            `heightCm` REAL NOT NULL,
                            `biotype` TEXT NOT NULL,
                            `trainingDaysPerWeek` INTEGER NOT NULL,
                            `activityLevel` TEXT NOT NULL,
                            `goal` TEXT NOT NULL,
                            `dietCycleType` TEXT NOT NULL,
                            `isAthleteOrWarmClimate` INTEGER NOT NULL,
                            `targetCalories` INTEGER NOT NULL,
                            `targetProteinGrams` INTEGER NOT NULL,
                            `targetCarbsGrams` INTEGER NOT NULL,
                            `targetFatGrams` INTEGER NOT NULL,
                            `targetWaterMl` INTEGER NOT NULL,
                            `monthlyTargetWeightKg` REAL NOT NULL,
                            `monthlyTargetDurationWeeks` INTEGER NOT NULL,
                            `monthlyNotes` TEXT NOT NULL,
                            `isDarkMode` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                    """.trimIndent())

                    if (existingColumns.isNotEmpty()) {
                        val defaultValues = mapOf(
                            "id" to "1",
                            "name" to "'Atleta'",
                            "email" to "'atleta@fitmail.com'",
                            "firebaseUid" to "''",
                            "passwordHash" to "'Atleta@2026'",
                            "isLoggedIn" to "0",
                            "hasCompletedOnboarding" to "0",
                            "lgpdConsentAccepted" to "0",
                            "lgpdConsentDate" to "${System.currentTimeMillis()}",
                            "gender" to "'MASCULINO'",
                            "age" to "26",
                            "weightKg" to "78.5",
                            "heightCm" to "178.0",
                            "biotype" to "'MESOMORPH'",
                            "trainingDaysPerWeek" to "5",
                            "activityLevel" to "'MODERATE'",
                            "goal" to "'BULKING_CLEAN'",
                            "dietCycleType" to "'FIXED_DAILY'",
                            "isAthleteOrWarmClimate" to "1",
                            "targetCalories" to "2650",
                            "targetProteinGrams" to "175",
                            "targetCarbsGrams" to "320",
                            "targetFatGrams" to "65",
                            "targetWaterMl" to "3530",
                            "monthlyTargetWeightKg" to "81.0",
                            "monthlyTargetDurationWeeks" to "4",
                            "monthlyNotes" to "'Fase de hipertrofia com foco em peitoral e dorsais.'",
                            "isDarkMode" to "1"
                        )
                        val selectExprs = expectedColumns.joinToString(", ") { col ->
                            if (existingColumns.contains(col)) "`$col`" else "${defaultValues[col]} AS `$col`"
                        }
                        val targetCols = expectedColumns.joinToString(", ") { "`$it`" }
                        db.execSQL("INSERT OR REPLACE INTO `user_profile_temp` ($targetCols) SELECT $selectExprs FROM `user_profile`")
                    }
                    db.execSQL("DROP TABLE IF EXISTS `user_profile`")
                    db.execSQL("ALTER TABLE `user_profile_temp` RENAME TO `user_profile`")
                }
            } catch (_: Exception) {}
        }

        private fun ensureWorkoutRoutinesSchema(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("DROP INDEX IF EXISTS `index_workout_routines_planId`")
                db.execSQL("DROP INDEX IF EXISTS `index_workout_routines_splitType`")

                val cursor = db.query("PRAGMA table_info(workout_routines)")
                val existingColumns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }
                cursor.close()

                val expectedColumns = listOf(
                    "id", "splitType", "letter", "title", "dayOfWeekLabel",
                    "targetMuscleGroups", "estimatedDurationMinutes", "orderIndex"
                )

                if (!existingColumns.containsAll(expectedColumns) || existingColumns.size != expectedColumns.size) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `workout_routines_temp` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `splitType` TEXT NOT NULL,
                            `letter` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `dayOfWeekLabel` TEXT NOT NULL,
                            `targetMuscleGroups` TEXT NOT NULL,
                            `estimatedDurationMinutes` INTEGER NOT NULL,
                            `orderIndex` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    if (existingColumns.isNotEmpty()) {
                        val selectExprs = expectedColumns.joinToString(", ") { col ->
                            if (existingColumns.contains(col)) "`$col`" else "'' AS `$col`"
                        }
                        val targetCols = expectedColumns.joinToString(", ") { "`$it`" }
                        db.execSQL("INSERT OR REPLACE INTO `workout_routines_temp` ($targetCols) SELECT $selectExprs FROM `workout_routines`")
                    }
                    db.execSQL("DROP TABLE IF EXISTS `workout_routines`")
                    db.execSQL("ALTER TABLE `workout_routines_temp` RENAME TO `workout_routines`")
                }
            } catch (_: Exception) {}
        }

        private fun ensureMealPlanItemsSchema(db: SupportSQLiteDatabase) {
            try {
                db.execSQL("DROP INDEX IF EXISTS `index_meal_plan_items_dayOfWeek`")

                val cursor = db.query("PRAGMA table_info(meal_plan_items)")
                val existingColumns = mutableSetOf<String>()
                while (cursor.moveToNext()) {
                    val nameIndex = cursor.getColumnIndex("name")
                    if (nameIndex >= 0) {
                        existingColumns.add(cursor.getString(nameIndex))
                    }
                }
                cursor.close()

                val expectedColumns = listOf(
                    "id", "dietPlanId", "mealType", "foodItemId", "foodName",
                    "foodCategory", "amount", "unit", "calories", "protein",
                    "carbs", "fat", "fiber", "isCompletedToday", "notes"
                )

                if (!existingColumns.containsAll(expectedColumns) || existingColumns.size != expectedColumns.size) {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `meal_plan_items_temp` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `dietPlanId` INTEGER NOT NULL,
                            `mealType` TEXT NOT NULL,
                            `foodItemId` INTEGER NOT NULL,
                            `foodName` TEXT NOT NULL,
                            `foodCategory` TEXT NOT NULL,
                            `amount` REAL NOT NULL,
                            `unit` TEXT NOT NULL,
                            `calories` REAL NOT NULL,
                            `protein` REAL NOT NULL,
                            `carbs` REAL NOT NULL,
                            `fat` REAL NOT NULL,
                            `fiber` REAL NOT NULL,
                            `isCompletedToday` INTEGER NOT NULL,
                            `notes` TEXT NOT NULL
                        )
                    """.trimIndent())

                    if (existingColumns.isNotEmpty()) {
                        val selectExprs = expectedColumns.joinToString(", ") { col ->
                            if (existingColumns.contains(col)) "`$col`" else "'' AS `$col`"
                        }
                        val targetCols = expectedColumns.joinToString(", ") { "`$it`" }
                        db.execSQL("INSERT OR REPLACE INTO `meal_plan_items_temp` ($targetCols) SELECT $selectExprs FROM `meal_plan_items`")
                    }
                    db.execSQL("DROP TABLE IF EXISTS `meal_plan_items`")
                    db.execSQL("ALTER TABLE `meal_plan_items_temp` RENAME TO `meal_plan_items`")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_plan_items_dietPlanId` ON `meal_plan_items` (`dietPlanId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_plan_items_mealType` ON `meal_plan_items` (`mealType`)")
                }
            } catch (_: Exception) {}
        }

        private fun ensureNewTables(db: SupportSQLiteDatabase) {
            try {
                // Tabela de registros de consumo alimentar diário
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `food_records` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateString` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `foodItemId` INTEGER NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `foodCategory` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `unit` TEXT NOT NULL,
                        `calories` REAL NOT NULL,
                        `protein` REAL NOT NULL,
                        `carbs` REAL NOT NULL,
                        `fat` REAL NOT NULL,
                        `fiber` REAL NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_records_dateString` ON `food_records` (`dateString`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_records_dateMillis` ON `food_records` (`dateMillis`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_records_mealType` ON `food_records` (`mealType`)")
            } catch (_: Exception) {}

            try {
                // Tabela de dietas semanais (dia a dia)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `weekly_diet_plans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dietPlanId` INTEGER NOT NULL,
                        `dayOfWeek` INTEGER NOT NULL,
                        `dayName` TEXT NOT NULL,
                        `targetCalories` INTEGER NOT NULL,
                        `targetProteinGrams` INTEGER NOT NULL,
                        `targetCarbsGrams` INTEGER NOT NULL,
                        `targetFatGrams` INTEGER NOT NULL,
                        `targetWaterMl` INTEGER NOT NULL,
                        `isTrainingDay` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weekly_diet_plans_dietPlanId` ON `weekly_diet_plans` (`dietPlanId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_weekly_diet_plans_dayOfWeek` ON `weekly_diet_plans` (`dayOfWeek`)")
            } catch (_: Exception) {}

            try {
                // Tabela de programas / planos de treino
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `workout_plans` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `splitType` TEXT NOT NULL,
                        `goal` TEXT NOT NULL,
                        `durationWeeks` INTEGER NOT NULL,
                        `daysPerWeek` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_plans_isActive` ON `workout_plans` (`isActive`)")
            } catch (_: Exception) {}

            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `gps_route_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `routeName` TEXT NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `dateDisplay` TEXT NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `distanceMeters` REAL NOT NULL,
                        `stepsCount` INTEGER NOT NULL,
                        `caloriesBurned` REAL NOT NULL,
                        `averagePaceMinPerKm` REAL NOT NULL,
                        `routePointsJson` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_gps_route_logs_dateMillis` ON `gps_route_logs` (`dateMillis`)")
            } catch (_: Exception) {}
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {}
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {}
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

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `progress_logs` ADD COLUMN `photoUri` TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_diet_plans_isActive` ON `diet_plans` (`isActive`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_plan_items_dietPlanId` ON `meal_plan_items` (`dietPlanId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_meal_plan_items_mealType` ON `meal_plan_items` (`mealType`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_intake_history_dateMillis` ON `daily_intake_history` (`dateMillis`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_daily_intake_history_dateString` ON `daily_intake_history` (`dateString`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_exercises_routineId` ON `workout_exercises` (`routineId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_logs_routineId` ON `workout_logs` (`routineId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_logs_dateMillis` ON `workout_logs` (`dateMillis`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_progress_logs_dateMillis` ON `progress_logs` (`dateMillis`)")
                } catch (_: Exception) {}
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_7_10 = object : Migration(7, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_8_10 = object : Migration(8, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_1_10 = object : Migration(1, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                ensureUserProfileSchema(db)
                ensureWorkoutRoutinesSchema(db)
                ensureMealPlanItemsSchema(db)
                ensureNewTables(db)
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `gps_route_logs` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `routeName` TEXT NOT NULL,
                            `activityType` TEXT NOT NULL,
                            `dateMillis` INTEGER NOT NULL,
                            `dateDisplay` TEXT NOT NULL,
                            `durationSeconds` INTEGER NOT NULL,
                            `distanceMeters` REAL NOT NULL,
                            `stepsCount` INTEGER NOT NULL,
                            `caloriesBurned` REAL NOT NULL,
                            `averagePaceMinPerKm` REAL NOT NULL,
                            `routePointsJson` TEXT NOT NULL,
                            `notes` TEXT NOT NULL
                        )
                    """.trimIndent())
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_gps_route_logs_dateMillis` ON `gps_route_logs` (`dateMillis`)")
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
                    "victus_clean_fitness.db"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_7_10,
                        MIGRATION_8_10,
                        MIGRATION_1_10,
                        MIGRATION_10_11
                    )
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
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

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            try {
                // Ensure no residual or dummy test users remain in database
                db.execSQL("DELETE FROM user_profile WHERE email = 'atleta@fitmail.com' OR name = 'Atleta' OR (isLoggedIn = 0 AND hasCompletedOnboarding = 0)")
            } catch (_: Exception) {}
        }

        private suspend fun populateDatabase(dao: AppDao) {
            // No default user profile seeded: database starts zeroed / clean for new user registration

            // Seed Master Diet Plan & Weekly Diet Plans (Segunda a Domingo)
            dao.insertDietPlan(PrepopulatedData.defaultDietPlan)
            dao.insertWeeklyDietPlans(PrepopulatedData.defaultWeeklyDietPlans)

            // Seed Food Catalog (incluindo ovos com azeite, suplementos e itens FatSecret)
            dao.insertFoods(PrepopulatedData.defaultFoods)

            // Seed Meal Plan Items
            dao.insertMealPlanItems(PrepopulatedData.defaultMealPlan)

            // Seed Initial Food Consumption Records (Diário de Alimentos)
            dao.insertFoodRecords(PrepopulatedData.defaultFoodRecords)

            // Seed Workout Programs (Planos de Treino)
            dao.insertWorkoutPlans(PrepopulatedData.defaultWorkoutPlans)

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
                    notes = "Início do protocolo Bulking Limpo"
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
