package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseMigrationTest {

    @Test
    fun upgradesIdenticalV1SchemaWithoutLosingProfileAndRemovesLegacyPassword() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-regression.db"
        context.deleteDatabase(name)
        // The entities in the last v1 commit are byte-for-byte identical to v2.
        val old = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        old.appDao().insertOrUpdateProfile(UserProfile(name = "Patrick", weightKg = 82f, passwordHash = "legacy-secret"))
        old.appDao().insertWorkoutLog(
            WorkoutLog(
                routineId = 1L,
                routineName = "Treino A",
                dateMillis = 1000L,
                durationMinutes = 30,
                totalVolumeKg = 200f,
                totalSetsCompleted = 5
            )
        )
        old.close()
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { sqlite ->
            sqlite.version = 1
            sqlite.execSQL("DELETE FROM room_master_table")
        }
        val upgraded = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
        try {
            val profile = upgraded.appDao().getUserProfileDirect(1)!!
            assertEquals("Patrick", profile.name)
            assertEquals(82f, profile.weightKg, 0f)
            assertEquals("", profile.passwordHash)
            assertEquals("Treino A", upgraded.appDao().getAllWorkoutLogs().first().single().routineName)
        } finally {
            upgraded.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun migration2To3PreservesMealPlansAndFoodsAndAddsColumns() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-2-to-3.db"
        context.deleteDatabase(name)

        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        db.appDao().insertMealPlanItem(
            MealPlanItem(
                mealType = MealType.BREAKFAST,
                foodItemId = 10L,
                foodName = "Aveia em Flocos",
                foodCategory = FoodCategory.CARBOHYDRATE,
                amount = 60f,
                unit = "g",
                calories = 230f,
                protein = 8f,
                carbs = 40f,
                fat = 4f,
                isFixed = false
            )
        )
        db.appDao().insertFoods(
            listOf(
                FoodItem(
                    id = 10L,
                    name = "Aveia em Flocos",
                    category = FoodCategory.CARBOHYDRATE,
                    servingUnit = "g",
                    calories = 380f,
                    protein = 14f,
                    carbs = 66f,
                    fat = 7f,
                    isFavorite = false
                )
            )
        )
        db.close()

        // Simulate v2 schema (downgrade version number and run MIGRATION_2_3 + MIGRATION_3_4)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { sqlite ->
            sqlite.version = 2
            sqlite.execSQL("DELETE FROM room_master_table")
        }

        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
            .build()
        try {
            val meals = migrated.appDao().getAllMealPlanItems().first()
            assertEquals(1, meals.size)
            assertEquals("Aveia em Flocos", meals.single().foodName)
            assertFalse(meals.single().isFixed)

            val foods = migrated.appDao().getAllFoods().first()
            assertTrue(foods.any { it.name == "Aveia em Flocos" && !it.isFavorite })
        } finally {
            migrated.close()
            context.deleteDatabase(name)
        }
    }

    @Test
    fun migration3To4PreservesUserProfileAndAddsBiometricLockColumn() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "migration-3-to-4.db"
        context.deleteDatabase(name)

        val db = Room.databaseBuilder(context, AppDatabase::class.java, name).build()
        db.appDao().insertOrUpdateProfile(
            UserProfile(
                id = 1,
                name = "Atleta Virtus",
                weightKg = 79.5f,
                targetCalories = 2800,
                isBiometricLockEnabled = true
            )
        )
        db.close()

        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { sqlite ->
            sqlite.version = 3
            sqlite.execSQL("DELETE FROM room_master_table")
        }

        val migrated = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.MIGRATION_3_4)
            .build()
        try {
            val profile = migrated.appDao().getUserProfileDirect(1)!!
            assertEquals("Atleta Virtus", profile.name)
            assertEquals(79.5f, profile.weightKg, 0f)
            assertEquals(2800, profile.targetCalories)
            assertTrue(profile.isBiometricLockEnabled)
        } finally {
            migrated.close()
            context.deleteDatabase(name)
        }
    }
}
