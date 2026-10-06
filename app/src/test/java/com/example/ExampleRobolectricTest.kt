package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GrowthDatabase
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.Gender
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.util.MacroCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var db: GrowthDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, GrowthDatabase::class.java)
      .allowMainThreadQueries()
      .build()
  }

  @After
  fun closeDb() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Growth Dieta", appName)
  }

  @Test
  fun `test macro calculation formulas`() {
    val plan = MacroCalculator.computeFullPlan(
      weightKg = 80f,
      heightCm = 180f,
      age = 25,
      gender = Gender.MASCULINO,
      activityLevel = ActivityLevel.MODERATE,
      goal = FitnessGoal.BULKING_CLEAN,
      customProteinGPerKg = 2.2f,
      customFatGPerKg = 0.8f
    )

    assertTrue("Target calories should be greater than BMR", plan.targetCalories > plan.bmr)
    assertEquals("Protein grams for 80kg at 2.2g/kg should be 176g", 176, plan.proteinGrams)
    assertEquals("Fat grams for 80kg at 0.8g/kg should be 64g", 64, plan.fatGrams)
    assertTrue("Water intake should be calculated correctly", plan.waterMl >= 2500)
  }

  @Test
  fun `test room dao stores diet plan and workout routines`() = runBlocking {
    val dao = db.growthDao()

    // 1. Insert and retrieve meal plan item
    val meal = MealPlanItem(
      mealType = MealType.BREAKFAST,
      foodItemId = 1,
      foodName = "Ovos Mexidos com Aveia",
      foodCategory = FoodCategory.PROTEIN,
      amount = 150f,
      calories = 320f,
      protein = 24f,
      carbs = 30f,
      fat = 12f
    )
    val mealId = dao.insertMealPlanItem(meal)
    assertTrue("Meal ID should be generated", mealId > 0)

    val allMeals = dao.getAllMealPlanItems().first()
    assertEquals(1, allMeals.size)
    assertEquals("Ovos Mexidos com Aveia", allMeals[0].foodName)

    // 2. Insert and retrieve workout routine and exercises
    val routine = WorkoutRoutine(
      splitType = WorkoutSplitType.ABC,
      letter = "A",
      title = "Peito, Ombros e Tríceps",
      dayOfWeekLabel = "Segunda-feira",
      targetMuscleGroups = "Peitoral, Deltoides, Tríceps",
      estimatedDurationMinutes = 60
    )
    val routineId = dao.insertRoutine(routine)
    assertTrue("Routine ID should be generated", routineId > 0)

    val exercise = WorkoutExercise(
      routineId = routineId,
      exerciseName = "Supino Reto com Barra",
      muscleGroup = "Peitoral Maior",
      sets = 4,
      repsMin = 8,
      repsMax = 12,
      currentWeightKg = 80f
    )
    dao.insertExercise(exercise)

    val exercises = dao.getExercisesForRoutine(routineId).first()
    assertEquals(1, exercises.size)
    assertEquals("Supino Reto com Barra", exercises[0].exerciseName)
    assertEquals(80f, exercises[0].currentWeightKg)
  }
}
