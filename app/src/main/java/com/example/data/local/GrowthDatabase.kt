package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyIntakeHistory
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
    version = 4,
    exportSchema = false
)
abstract class GrowthDatabase : RoomDatabase() {

    abstract fun growthDao(): GrowthDao

    companion object {
        @Volatile
        private var INSTANCE: GrowthDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE user_profile ADD COLUMN firebaseUid TEXT NOT NULL DEFAULT ''")
                } catch (_: Exception) {}
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): GrowthDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GrowthDatabase::class.java,
                    "growth_diet_fitness.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .addCallback(GrowthDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class GrowthDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.growthDao())
                }
            }
        }

        private suspend fun populateDatabase(dao: GrowthDao) {
            // Seed Profile
            dao.insertOrUpdateProfile(UserProfile())

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
                    weightKg = 77.0f,
                    bodyFatPercentage = 13.5f,
                    chestCm = 101.0f,
                    waistCm = 80.0f,
                    armLeftCm = 37.5f,
                    armRightCm = 37.5f,
                    thighCm = 58.0f,
                    notes = "Início do protocolo de hipertrofia Growth"
                )
            )
            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now - (14 * dayMillis),
                    dateDisplay = dateFormat.format(Date(now - (14 * dayMillis))),
                    weightKg = 77.6f,
                    bodyFatPercentage = 13.2f,
                    chestCm = 101.5f,
                    waistCm = 79.8f,
                    armLeftCm = 38.0f,
                    armRightCm = 38.0f,
                    thighCm = 58.5f,
                    notes = "Boa recuperação muscular e aumento de força no supino"
                )
            )
            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now - (7 * dayMillis),
                    dateDisplay = dateFormat.format(Date(now - (7 * dayMillis))),
                    weightKg = 78.1f,
                    bodyFatPercentage = 13.0f,
                    chestCm = 102.5f,
                    waistCm = 80.0f,
                    armLeftCm = 38.5f,
                    armRightCm = 38.5f,
                    thighCm = 59.0f,
                    notes = "Consistência de 100% nos macros da dieta"
                )
            )
            dao.insertProgressLog(
                ProgressLog(
                    dateMillis = now,
                    dateDisplay = dateFormat.format(Date(now)),
                    weightKg = 78.5f,
                    bodyFatPercentage = 12.8f,
                    chestCm = 103.0f,
                    waistCm = 79.5f,
                    armLeftCm = 39.0f,
                    armRightCm = 39.0f,
                    thighCm = 59.5f,
                    notes = "Evolução sólida: +1.5kg com redução de dobra abdominal"
                )
            )

            // Seed initial workout logs
            dao.insertWorkoutLog(
                WorkoutLog(
                    routineId = 1,
                    routineName = "Peitoral & Deltoide Anterior",
                    dateMillis = now - (2 * dayMillis),
                    durationMinutes = 62,
                    totalVolumeKg = 14200f,
                    totalSetsCompleted = 22,
                    perceivedExertion = 9,
                    notes = "Cargas progredidas no supino reto e inclinado"
                )
            )
            dao.insertWorkoutLog(
                WorkoutLog(
                    routineId = 2,
                    routineName = "Dorsal, Deltoide Posterior e Trapézio",
                    dateMillis = now - (1 * dayMillis),
                    durationMinutes = 58,
                    totalVolumeKg = 15800f,
                    totalSetsCompleted = 21,
                    perceivedExertion = 8,
                    notes = "Pumps excelentes e execução limpa nas remadas"
                )
            )
        }
    }
}
