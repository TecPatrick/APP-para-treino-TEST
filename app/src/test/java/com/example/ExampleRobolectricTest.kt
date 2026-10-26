package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AppDao
import com.example.data.model.ActivityLevel
import com.example.data.model.DietPlan
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.Gender
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.util.MacroCalculator
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseMuscleClassifier
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

  private lateinit var db: AppDatabase

  @Before
  fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
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
    assertEquals("Growth Diet", appName)
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

    assertEquals("Food target uses BMR plus the selected goal, not activity TDEE", plan.bmr + 350, plan.targetCalories)
    assertEquals("Protein grams for 80kg at 2.2g/kg should be 176g", 176, plan.proteinGrams)
    assertEquals("Fat grams for 80kg at 0.8g/kg should be 64g", 64, plan.fatGrams)
    assertTrue("Water intake should be calculated correctly", plan.waterMl >= 2500)
  }

  @Test
  fun `high body weight uses adjusted reference for macros and safe hydration`() {
    val plan = MacroCalculator.computeFullPlan(
      weightKg = 170f,
      heightCm = 184f,
      age = 28,
      gender = Gender.MASCULINO,
      activityLevel = ActivityLevel.MODERATE,
      goal = FitnessGoal.MAINTENANCE,
      customProteinGPerKg = 2.0f,
      customFatGPerKg = 0.8f,
      customTargetCalories = 2500
    )

    assertEquals(2715, plan.bmr)
    assertEquals(2500, plan.targetCalories)
    assertTrue(plan.macroReferenceWeightKg < 110f)
    assertTrue(plan.proteinGrams < 220)
    assertTrue(plan.waterMl in 3000..5000)
  }

  @Test
  fun `leg press is quadriceps and never deltoid`() {
    val legPress = ExerciseGifMapper.getGuideForExercise("Leg Press 45°")
    assertTrue(ExerciseMuscleClassifier.matches(legPress, "Quadríceps"))
    assertTrue(!ExerciseMuscleClassifier.matches(legPress, "Deltoide"))
  }

  @Test
  fun `reverse fly is not classified as chest`() {
    val reverseFly = ExerciseGifMapper.getGuideForExercise("Crucifixo Inverso com Halteres")
    assertTrue(ExerciseMuscleClassifier.matches(reverseFly, "Deltoide"))
    assertTrue(!ExerciseMuscleClassifier.matches(reverseFly, "Peitoral"))
  }

  @Test
  fun `test room dao stores diet plan and workout routines`() = runBlocking {
    val dao = db.appDao()

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

  @Test
  fun `test room dao stores and updates user profile biometrics`() = runBlocking {
    val dao = db.appDao()

    val profile = UserProfile(
      id = 1,
      name = "Atleta",
      weightKg = 82.5f,
      heightCm = 182f,
      goal = FitnessGoal.BULKING_CLEAN,
      targetCalories = 2900,
      targetProteinGrams = 180,
      targetCarbsGrams = 350,
      targetFatGrams = 70
    )
    dao.insertOrUpdateProfile(profile)

    val loaded = dao.getUserProfile().first()
    assertTrue("UserProfile should be retrieved", loaded != null)
    assertEquals(82.5f, loaded?.weightKg)
    assertEquals(182f, loaded?.heightCm)
    assertEquals(FitnessGoal.BULKING_CLEAN, loaded?.goal)
    assertEquals(2900, loaded?.targetCalories)

    // Update weight and goal
    val updatedProfile = loaded!!.copy(
      weightKg = 81.0f,
      goal = FitnessGoal.CUTTING_MODERATE
    )
    dao.insertOrUpdateProfile(updatedProfile)

    val updatedLoaded = dao.getUserProfile().first()
    assertEquals(81.0f, updatedLoaded?.weightKg)
    assertEquals(FitnessGoal.CUTTING_MODERATE, updatedLoaded?.goal)
  }

  @Test
  fun `test room dao updates biometrics like weight height age and activity level`() = runBlocking {
    val dao = db.appDao()

    val initialProfile = UserProfile(
      id = 1,
      name = "Atleta Pro",
      weightKg = 75.0f,
      heightCm = 175f,
      age = 25,
      activityLevel = ActivityLevel.LIGHT,
      goal = FitnessGoal.MAINTENANCE
    )
    dao.insertOrUpdateProfile(initialProfile)

    val loaded = dao.getUserProfile().first()
    assertEquals(75.0f, loaded?.weightKg)
    assertEquals(175f, loaded?.heightCm)
    assertEquals(25, loaded?.age)
    assertEquals(ActivityLevel.LIGHT, loaded?.activityLevel)

    // Edit biometrics
    val updatedBiometrics = loaded!!.copy(
      weightKg = 77.8f,
      heightCm = 176f,
      age = 26,
      activityLevel = ActivityLevel.VERY_ACTIVE,
      goal = FitnessGoal.BULKING_CLEAN
    )
    dao.insertOrUpdateProfile(updatedBiometrics)

    val reloaded = dao.getUserProfile().first()
    assertEquals(77.8f, reloaded?.weightKg)
    assertEquals(176f, reloaded?.heightCm)
    assertEquals(26, reloaded?.age)
    assertEquals(ActivityLevel.VERY_ACTIVE, reloaded?.activityLevel)
    assertEquals(FitnessGoal.BULKING_CLEAN, reloaded?.goal)
  }

  @Test
  fun `test room dao stores and queries diet plans`() = runBlocking {
    val dao = db.appDao()

    val dietPlan = DietPlan(
      name = "Protocolo Hipertrofia",
      description = "Plano focado em ganho muscular limpo",
      goal = FitnessGoal.BULKING_CLEAN,
      targetCalories = 2800,
      targetProteinGrams = 180,
      targetCarbsGrams = 330,
      targetFatGrams = 65,
      targetWaterMl = 3600,
      isActive = true
    )
    val planId = dao.insertDietPlan(dietPlan)
    assertTrue("DietPlan ID should be generated", planId > 0)

    val activePlan = dao.getActiveDietPlan().first()
    assertTrue("Active diet plan should be found", activePlan != null)
    assertEquals("Protocolo Hipertrofia", activePlan?.name)
    assertEquals(2800, activePlan?.targetCalories)

    val allPlans = dao.getAllDietPlans().first()
    assertEquals(1, allPlans.size)
  }

  @Test
  fun `test room dao workout routine management`() = runBlocking {
    val dao = db.appDao()

    val routine = WorkoutRoutine(
      splitType = WorkoutSplitType.ABCDE,
      letter = "B",
      title = "Costas e Bíceps",
      dayOfWeekLabel = "Terça-feira",
      targetMuscleGroups = "Dorsais, Trapézio, Bíceps",
      estimatedDurationMinutes = 55
    )
    val routineId = dao.insertRoutine(routine)
    assertTrue("Workout routine should be inserted", routineId > 0)

    val fetched = dao.getRoutineById(routineId).first()
    assertTrue("Routine should exist", fetched != null)
    assertEquals("Costas e Bíceps", fetched?.title)

    val updated = fetched!!.copy(title = "Dorsais Completo e Bíceps")
    dao.updateRoutine(updated)

    val fetchedUpdated = dao.getRoutineById(routineId).first()
    assertEquals("Dorsais Completo e Bíceps", fetchedUpdated?.title)

    dao.deleteRoutine(routineId)
    val afterDelete = dao.getRoutineById(routineId).first()
    assertTrue("Routine should be deleted", afterDelete == null)
  }

  @Test
  fun `test room stores and queries workout logs with volume metrics`() = runBlocking {
    val dao = db.appDao()

    val log1 = WorkoutLog(
      routineId = 101L,
      routineName = "Treino A - Peito & Tríceps",
      dateMillis = 1000000L,
      durationMinutes = 62,
      totalVolumeKg = 8500f,
      totalSetsCompleted = 20,
      perceivedExertion = 9,
      notes = "Supino pesado, excelente pump"
    )
    val log2 = WorkoutLog(
      routineId = 102L,
      routineName = "Treino B - Costas & Bíceps",
      dateMillis = 2000000L,
      durationMinutes = 55,
      totalVolumeKg = 9200f,
      totalSetsCompleted = 18,
      perceivedExertion = 8,
      notes = "Puxada frontal e remada curvada"
    )

    val id1 = dao.insertWorkoutLog(log1)
    val id2 = dao.insertWorkoutLog(log2)
    assertTrue("Log 1 inserted", id1 > 0)
    assertTrue("Log 2 inserted", id2 > 0)

    val allLogs = dao.getAllWorkoutLogs().first()
    assertEquals("Should have 2 logs", 2, allLogs.size)
    // Ordered by dateMillis DESC: log2 should be first
    assertEquals("Treino B - Costas & Bíceps", allLogs[0].routineName)

    val logsForRoutine101 = dao.getWorkoutLogsForRoutine(101L).first()
    assertEquals(1, logsForRoutine101.size)
    assertEquals(8500f, logsForRoutine101[0].totalVolumeKg)

    val totalVolume = dao.getTotalWorkoutVolume().first()
    assertEquals(17700f, totalVolume)

    val totalCount = dao.getTotalWorkoutCount().first()
    assertEquals(2, totalCount)
  }

  @Test
  fun `test room updates dietary goals and switches active diet plans`() = runBlocking {
    val dao = db.appDao()

    // 1. Initial user profile
    dao.insertOrUpdateProfile(
      UserProfile(
        id = 1,
        name = "Atleta",
        targetCalories = 2500,
        targetProteinGrams = 160,
        targetCarbsGrams = 300,
        targetFatGrams = 60,
        targetWaterMl = 3000
      )
    )

    // 2. Targeted update of dietary goals
    dao.updateDietaryGoals(
      calories = 2800,
      protein = 180,
      carbs = 340,
      fat = 65,
      water = 3500
    )

    val profile = dao.getUserProfile().first()
    assertEquals(2800, profile?.targetCalories)
    assertEquals(180, profile?.targetProteinGrams)
    assertEquals(340, profile?.targetCarbsGrams)
    assertEquals(65, profile?.targetFatGrams)
    assertEquals(3500, profile?.targetWaterMl)

    // 3. Switch active diet plans atomically
    val planA = DietPlan(name = "Bulking", targetCalories = 3000, isActive = true)
    val planB = DietPlan(name = "Cutting", targetCalories = 2200, isActive = false)
    val idA = dao.insertDietPlan(planA)
    val idB = dao.insertDietPlan(planB)

    assertEquals(idA, dao.getActiveDietPlan().first()?.id)

    dao.switchActiveDietPlan(idB)
    assertEquals(idB, dao.getActiveDietPlan().first()?.id)
  }

  @Test
  fun `test offline persistence for food records daily journal`() = runBlocking {
    val dao = db.appDao()

    val record1 = com.example.data.model.FoodRecord(
      dateString = "2026-10-11",
      dateMillis = 1760000000000L,
      mealType = MealType.BREAKFAST,
      foodItemId = 9L,
      foodName = "Ovo Frito no Azeite de Oliva",
      foodCategory = FoodCategory.PROTEIN,
      amount = 120f,
      unit = "g",
      calories = 230f,
      protein = 12.8f,
      carbs = 1.0f,
      fat = 19.6f
    )
    val record2 = com.example.data.model.FoodRecord(
      dateString = "2026-10-11",
      dateMillis = 1760000010000L,
      mealType = MealType.LUNCH,
      foodItemId = 15L,
      foodName = "Peito de Frango Grelhado",
      foodCategory = FoodCategory.PROTEIN,
      amount = 180f,
      unit = "g",
      calories = 297f,
      protein = 55.8f,
      carbs = 0f,
      fat = 6.5f
    )

    val id1 = dao.insertFoodRecord(record1)
    val id2 = dao.insertFoodRecord(record2)
    assertTrue("Food record 1 inserted", id1 > 0)
    assertTrue("Food record 2 inserted", id2 > 0)

    val todayRecords = dao.getFoodRecordsForDate("2026-10-11").first()
    assertEquals(2, todayRecords.size)
    assertEquals("Ovo Frito no Azeite de Oliva", todayRecords[0].foodName)

    val breakfastRecords = dao.getFoodRecordsForMeal("2026-10-11", MealType.BREAKFAST).first()
    assertEquals(1, breakfastRecords.size)
    assertEquals("Ovo Frito no Azeite de Oliva", breakfastRecords[0].foodName)

    dao.deleteFoodRecord(id1)
    val afterDelete = dao.getFoodRecordsForDate("2026-10-11").first()
    assertEquals(1, afterDelete.size)
    assertEquals("Peito de Frango Grelhado", afterDelete[0].foodName)
  }

  @Test
  fun `test offline persistence for weekly diet plans`() = runBlocking {
    val dao = db.appDao()

    val weeklyPlanMonday = com.example.data.model.WeeklyDietPlan(
      dietPlanId = 1L,
      dayOfWeek = 1,
      dayName = "Segunda-feira",
      targetCalories = 2800,
      targetProteinGrams = 185,
      targetCarbsGrams = 350,
      targetFatGrams = 65,
      isTrainingDay = true,
      notes = "Treino pesado de Peito e Tríceps"
    )
    val weeklyPlanSunday = com.example.data.model.WeeklyDietPlan(
      dietPlanId = 1L,
      dayOfWeek = 7,
      dayName = "Domingo",
      targetCalories = 2400,
      targetProteinGrams = 175,
      targetCarbsGrams = 250,
      targetFatGrams = 70,
      isTrainingDay = false,
      notes = "Descanso total e carbo reduzido"
    )

    dao.insertWeeklyDietPlans(listOf(weeklyPlanMonday, weeklyPlanSunday))

    val plans = dao.getWeeklyDietPlans(1L).first()
    assertEquals(2, plans.size)
    assertEquals("Segunda-feira", plans[0].dayName)
    assertEquals(2800, plans[0].targetCalories)
    assertTrue(plans[0].isTrainingDay)

    val sundayPlan = dao.getWeeklyDietPlanForDay(1L, 7).first()
    assertTrue(sundayPlan != null)
    assertEquals("Domingo", sundayPlan?.dayName)
    assertEquals(false, sundayPlan?.isTrainingDay)
  }

  @Test
  fun `test offline persistence for workout programs and switching active plan`() = runBlocking {
    val dao = db.appDao()

    val program1 = com.example.data.model.WorkoutPlan(
      name = "Plano Hipertrofia ABCDE",
      description = "5 dias isolados",
      splitType = WorkoutSplitType.ABCDE,
      durationWeeks = 12,
      daysPerWeek = 5,
      isActive = true
    )
    val program2 = com.example.data.model.WorkoutPlan(
      name = "Plano Força & Volume ABC",
      description = "3 dias de alta intensidade",
      splitType = WorkoutSplitType.ABC,
      durationWeeks = 8,
      daysPerWeek = 3,
      isActive = false
    )

    val p1Id = dao.insertWorkoutPlan(program1)
    val p2Id = dao.insertWorkoutPlan(program2)

    val active = dao.getActiveWorkoutPlan().first()
    assertEquals(p1Id, active?.id)
    assertEquals("Plano Hipertrofia ABCDE", active?.name)

    // Switch active workout plan
    dao.switchActiveWorkoutPlan(p2Id)
    val newActive = dao.getActiveWorkoutPlan().first()
    assertEquals(p2Id, newActive?.id)
    assertEquals("Plano Força & Volume ABC", newActive?.name)
  }
}
