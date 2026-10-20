package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.DietPlan
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.FoodRecord
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
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
import com.example.data.model.DietaryProfile
import com.example.data.model.Macro
import com.example.data.model.WorkoutSchedule
import com.example.data.model.CardioLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // =========================================================================
    // 👤 UserProfile DAO Operations (Perfis de Usuários)
    // =========================================================================
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile ORDER BY id ASC")
    fun getAllUserProfiles(): Flow<List<UserProfile>>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    fun getUserProfileById(id: Int): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = :id LIMIT 1")
    suspend fun getUserProfileDirect(id: Int = 1): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile): Long

    @Query("UPDATE user_profile SET targetCalories = :calories, targetProteinGrams = :protein, targetCarbsGrams = :carbs, targetFatGrams = :fat, targetWaterMl = :water WHERE id = 1")
    suspend fun updateDietaryGoals(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        water: Int
    )

    @Query("UPDATE user_profile SET targetWaterMl = :newTargetWater WHERE id = 1")
    suspend fun updateWaterGoal(newTargetWater: Int)

    @Query("UPDATE user_profile SET weightKg = :newWeight WHERE id = 1")
    suspend fun updateWeight(newWeight: Float)

    @Query("DELETE FROM user_profile WHERE id = :id")
    suspend fun deleteUserProfile(id: Int)

    @Query("DELETE FROM user_profile")
    suspend fun deleteAllUserProfiles()

    // =========================================================================
    // 🥗 DietPlan DAO Operations (Planos Gerais de Dieta)
    // =========================================================================
    @Query("SELECT * FROM diet_plans ORDER BY isActive DESC, createdAtMillis DESC")
    fun getAllDietPlans(): Flow<List<DietPlan>>

    @Query("SELECT * FROM diet_plans WHERE isActive = 1 LIMIT 1")
    fun getActiveDietPlan(): Flow<DietPlan?>

    @Query("SELECT * FROM diet_plans WHERE id = :id")
    fun getDietPlanById(id: Long): Flow<DietPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietPlan(plan: DietPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietPlans(plans: List<DietPlan>)

    @Update
    suspend fun updateDietPlan(plan: DietPlan)

    @Query("DELETE FROM diet_plans WHERE id = :id")
    suspend fun deleteDietPlan(id: Long)

    @Query("UPDATE diet_plans SET isActive = 0")
    suspend fun deactivateAllDietPlans()

    @Query("UPDATE diet_plans SET isActive = 1 WHERE id = :planId")
    suspend fun activateDietPlan(planId: Long)

    @Transaction
    suspend fun switchActiveDietPlan(planId: Long) {
        deactivateAllDietPlans()
        activateDietPlan(planId)
    }

    @Query("UPDATE diet_plans SET targetCalories = :calories, targetProteinGrams = :protein, targetCarbsGrams = :carbs, targetFatGrams = :fat, targetWaterMl = :water WHERE id = :planId")
    suspend fun updateDietPlanGoals(planId: Long, calories: Int, protein: Int, carbs: Int, fat: Int, water: Int)

    // =========================================================================
    // 📅 WeeklyDietPlan DAO Operations (Dietas Semanais dia a dia)
    // =========================================================================
    @Query("SELECT * FROM weekly_diet_plans WHERE dietPlanId = :dietPlanId ORDER BY dayOfWeek ASC")
    fun getWeeklyDietPlans(dietPlanId: Long): Flow<List<WeeklyDietPlan>>

    @Query("SELECT * FROM weekly_diet_plans WHERE dietPlanId = :dietPlanId AND dayOfWeek = :dayOfWeek LIMIT 1")
    fun getWeeklyDietPlanForDay(dietPlanId: Long, dayOfWeek: Int): Flow<WeeklyDietPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyDietPlan(plan: WeeklyDietPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyDietPlans(plans: List<WeeklyDietPlan>)

    @Update
    suspend fun updateWeeklyDietPlan(plan: WeeklyDietPlan)

    @Query("DELETE FROM weekly_diet_plans WHERE id = :id")
    suspend fun deleteWeeklyDietPlan(id: Long)

    @Query("DELETE FROM weekly_diet_plans WHERE dietPlanId = :dietPlanId")
    suspend fun deleteWeeklyDietPlansForDiet(dietPlanId: Long)

    // =========================================================================
    // 🍽️ MealPlanItem DAO Operations (Refeições planejadas da dieta)
    // =========================================================================
    @Query("SELECT * FROM meal_plan_items ORDER BY id ASC")
    fun getAllMealPlanItems(): Flow<List<MealPlanItem>>

    @Query("SELECT * FROM meal_plan_items WHERE dietPlanId = :dietPlanId ORDER BY id ASC")
    fun getMealPlanItemsForDiet(dietPlanId: Long): Flow<List<MealPlanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanItem(item: MealPlanItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlanItems(items: List<MealPlanItem>)

    @Update
    suspend fun updateMealPlanItem(item: MealPlanItem)

    @Query("DELETE FROM meal_plan_items WHERE id = :id")
    suspend fun deleteMealPlanItem(id: Long)

    @Query("UPDATE meal_plan_items SET isCompletedToday = :completed WHERE id = :id")
    suspend fun setMealCompleted(id: Long, completed: Boolean)

    @Query("UPDATE meal_plan_items SET isCompletedToday = 0")
    suspend fun resetDailyMealCompletions()

    // =========================================================================
    // 🍎 FoodItem DAO Operations (Catálogo de Alimentos)
    // =========================================================================
    @Query("SELECT * FROM food_items ORDER BY isGrowthProduct DESC, name ASC")
    fun getAllFoods(): Flow<List<FoodItem>>

    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodsCount(): Int

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY isGrowthProduct DESC, name ASC")
    fun searchFoods(query: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: FoodCategory): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    fun getFoodById(id: Long): Flow<FoodItem?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(foodItem: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodItem>)

    @Update
    suspend fun updateFood(foodItem: FoodItem)

    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFood(id: Long)

    // =========================================================================
    // 📝 FoodRecord DAO Operations (Registros de Consumo Diário de Alimentos)
    // =========================================================================
    @Query("SELECT * FROM food_records ORDER BY dateMillis DESC")
    fun getAllFoodRecords(): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE dateString = :dateString ORDER BY dateMillis ASC")
    fun getFoodRecordsForDate(dateString: String): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE dateString = :dateString AND mealType = :mealType ORDER BY dateMillis ASC")
    fun getFoodRecordsForMeal(dateString: String, mealType: MealType): Flow<List<FoodRecord>>

    @Query("SELECT * FROM food_records WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getFoodRecordsBetween(startMillis: Long, endMillis: Long): Flow<List<FoodRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodRecord(record: FoodRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodRecords(records: List<FoodRecord>)

    @Update
    suspend fun updateFoodRecord(record: FoodRecord)

    @Query("DELETE FROM food_records WHERE id = :id")
    suspend fun deleteFoodRecord(id: Long)

    @Query("DELETE FROM food_records WHERE dateString = :dateString")
    suspend fun deleteFoodRecordsForDate(dateString: String)

    // =========================================================================
    // 📋 WorkoutPlan DAO Operations (Planos de Treino)
    // =========================================================================
    @Query("SELECT * FROM workout_plans ORDER BY isActive DESC, createdAtMillis DESC")
    fun getAllWorkoutPlans(): Flow<List<WorkoutPlan>>

    @Query("SELECT * FROM workout_plans WHERE isActive = 1 LIMIT 1")
    fun getActiveWorkoutPlan(): Flow<WorkoutPlan?>

    @Query("SELECT * FROM workout_plans WHERE id = :id LIMIT 1")
    fun getWorkoutPlanById(id: Long): Flow<WorkoutPlan?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlan(plan: WorkoutPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutPlans(plans: List<WorkoutPlan>)

    @Update
    suspend fun updateWorkoutPlan(plan: WorkoutPlan)

    @Query("DELETE FROM workout_plans WHERE id = :id")
    suspend fun deleteWorkoutPlan(id: Long)

    @Query("UPDATE workout_plans SET isActive = 0")
    suspend fun deactivateAllWorkoutPlans()

    @Query("UPDATE workout_plans SET isActive = 1 WHERE id = :planId")
    suspend fun activateWorkoutPlan(planId: Long)

    @Transaction
    suspend fun switchActiveWorkoutPlan(planId: Long) {
        deactivateAllWorkoutPlans()
        activateWorkoutPlan(planId)
    }

    // =========================================================================
    // 🏋️ WorkoutRoutine DAO Operations (Rotinas de Treino)
    // =========================================================================
    @Query("SELECT * FROM workout_routines ORDER BY splitType ASC, orderIndex ASC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>

    @Query("SELECT * FROM workout_routines WHERE splitType = :splitType ORDER BY orderIndex ASC")
    fun getRoutinesForSplit(splitType: com.example.data.model.WorkoutSplitType): Flow<List<WorkoutRoutine>>

    @Query("SELECT * FROM workout_routines WHERE id = :routineId")
    fun getRoutineById(routineId: Long): Flow<WorkoutRoutine?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutine): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutines(routines: List<WorkoutRoutine>)

    @Update
    suspend fun updateRoutine(routine: WorkoutRoutine)

    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutine)

    @Query("DELETE FROM workout_routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: Long)

    // =========================================================================
    // 💪 Workout Exercises & Workout Logs DAO Operations (Exercícios e Logs)
    // =========================================================================
    @Query("SELECT * FROM workout_exercises WHERE routineId = :routineId ORDER BY orderIndex ASC")
    fun getExercisesForRoutine(routineId: Long): Flow<List<WorkoutExercise>>

    @Query("SELECT * FROM workout_exercises ORDER BY routineId ASC, orderIndex ASC")
    fun getAllExercises(): Flow<List<WorkoutExercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: WorkoutExercise): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<WorkoutExercise>)

    @Update
    suspend fun updateExercise(exercise: WorkoutExercise)

    @Query("DELETE FROM workout_exercises WHERE id = :exerciseId")
    suspend fun deleteExercise(exerciseId: Long)

    @Query("DELETE FROM workout_exercises WHERE routineId = :routineId")
    suspend fun deleteExercisesForRoutine(routineId: Long)

    @Query("SELECT * FROM workout_logs ORDER BY dateMillis DESC")
    fun getAllWorkoutLogs(): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE routineId = :routineId ORDER BY dateMillis DESC")
    fun getWorkoutLogsForRoutine(routineId: Long): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentWorkoutLogs(limit: Int = 10): Flow<List<WorkoutLog>>

    @Query("SELECT * FROM workout_logs WHERE id = :id LIMIT 1")
    fun getWorkoutLogById(id: Long): Flow<WorkoutLog?>

    @Query("SELECT SUM(totalVolumeKg) FROM workout_logs")
    fun getTotalWorkoutVolume(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM workout_logs")
    fun getTotalWorkoutCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutLog(log: WorkoutLog): Long

    @Query("DELETE FROM workout_logs WHERE id = :id")
    suspend fun deleteWorkoutLog(id: Long)

    @Query("DELETE FROM workout_logs WHERE routineId = :routineId")
    suspend fun deleteWorkoutLogsForRoutine(routineId: Long)

    // =========================================================================
    // 📈 Progress & Daily Intake DAO Operations (Progresso & Histórico Diário)
    // =========================================================================
    @Query("SELECT * FROM progress_logs ORDER BY dateMillis DESC")
    fun getAllProgressLogs(): Flow<List<ProgressLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressLog(log: ProgressLog): Long

    @Query("DELETE FROM progress_logs WHERE id = :id")
    suspend fun deleteProgressLog(id: Long)

    @Query("SELECT * FROM daily_intake_history ORDER BY dateMillis DESC LIMIT 30")
    fun getRecentIntakeHistory(): Flow<List<DailyIntakeHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyIntake(intake: DailyIntakeHistory): Long

    @Query("SELECT * FROM daily_intake_history WHERE dateString = :dateString LIMIT 1")
    suspend fun getDailyIntakeForDate(dateString: String): DailyIntakeHistory?

    // =========================================================================
    // 🛒 ShoppingItem DAO Operations (Lista de Compras Offline)
    // =========================================================================
    @Query("SELECT * FROM shopping_items ORDER BY isChecked ASC, category ASC, name ASC")
    fun getShoppingList(): Flow<List<ShoppingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItems(items: List<ShoppingItem>)

    @Update
    suspend fun updateShoppingItem(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE id = :id")
    suspend fun deleteShoppingItem(id: Long)

    @Query("DELETE FROM shopping_items WHERE isChecked = 1")
    suspend fun deleteCheckedShoppingItems()

    @Query("DELETE FROM shopping_items WHERE isAutoGenerated = 1")
    suspend fun clearAutoGeneratedShoppingItems()

    // =========================================================================
    // 🧾 Purchase Receipts & Price Logs DAO Operations (Notas Fiscais & Preços)
    // =========================================================================
    @Query("SELECT * FROM purchase_receipts ORDER BY dateMillis DESC")
    fun getAllReceipts(): Flow<List<PurchaseReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PurchaseReceipt): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipts(receipts: List<PurchaseReceipt>)

    @Query("DELETE FROM purchase_receipts WHERE id = :id")
    suspend fun deleteReceipt(id: Long)

    @Query("SELECT * FROM purchase_item_logs ORDER BY dateMillis DESC")
    fun getAllPurchaseLogs(): Flow<List<PurchaseItemLog>>

    @Query("SELECT * FROM purchase_item_logs WHERE foodName = :foodName ORDER BY dateMillis DESC")
    fun getPriceHistoryForFood(foodName: String): Flow<List<PurchaseItemLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItemLog(itemLog: PurchaseItemLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItemLogs(itemLogs: List<PurchaseItemLog>)

    @Query("DELETE FROM purchase_item_logs WHERE receiptId = :receiptId")
    suspend fun deletePurchaseLogsForReceipt(receiptId: Long)

    // =========================================================================
    // 🌍 GPS Route & Step Tracking DAO Operations
    // =========================================================================
    @Query("SELECT * FROM gps_route_logs ORDER BY dateMillis DESC")
    fun getAllGpsRouteLogs(): Flow<List<GpsRouteLog>>

    @Query("SELECT SUM(distanceMeters) FROM gps_route_logs")
    fun getTotalGpsDistanceMeters(): Flow<Float?>

    @Query("SELECT SUM(stepsCount) FROM gps_route_logs")
    fun getTotalGpsSteps(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpsRouteLog(log: GpsRouteLog): Long

    @Query("DELETE FROM gps_route_logs WHERE id = :id")
    suspend fun deleteGpsRouteLog(id: Long)

    // =========================================================================
    // 🏃 Cardio DAO Operations
    // =========================================================================
    @Query("SELECT * FROM cardio_logs ORDER BY dateMillis DESC")
    fun getAllCardioLogs(): Flow<List<CardioLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCardioLog(log: CardioLog): Long

    @Query("DELETE FROM cardio_logs WHERE id = :id")
    suspend fun deleteCardioLog(id: Long)
    // =========================================================================
    // 🥗 Dietary Profile, Macros, and Workout Schedule DAO Operations
    // =========================================================================
    @Query("SELECT * FROM dietary_profiles LIMIT 1")
    fun getDietaryProfile(): Flow<DietaryProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDietaryProfile(profile: DietaryProfile)

    @Query("SELECT * FROM macros WHERE date = :date")
    fun getMacrosByDate(date: Long): Flow<Macro?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: Macro)

    @Query("SELECT * FROM workout_schedules")
    fun getAllWorkoutSchedules(): Flow<List<WorkoutSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSchedule(schedule: WorkoutSchedule)
}

typealias GrowthDao = AppDao
typealias UserProfileDao = AppDao
typealias DietPlanDao = AppDao
typealias WorkoutRoutineDao = AppDao
