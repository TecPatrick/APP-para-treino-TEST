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
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // =========================================================================
    // 👤 UserProfile DAO Operations
    // =========================================================================
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET targetCalories = :calories, targetProteinGrams = :protein, targetCarbsGrams = :carbs, targetFatGrams = :fat, targetWaterMl = :water WHERE id = 1")
    suspend fun updateDietaryGoals(calories: Int, protein: Int, carbs: Int, fat: Int, water: Int)

    @Query("UPDATE user_profile SET targetWaterMl = :newTargetWater WHERE id = 1")
    suspend fun updateWaterGoal(newTargetWater: Int)

    @Query("UPDATE user_profile SET weightKg = :newWeight WHERE id = 1")
    suspend fun updateWeight(newWeight: Float)

    // =========================================================================
    // 🥗 DietPlan DAO Operations
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
    // 🏋️ WorkoutRoutine DAO Operations
    // =========================================================================
    @Query("SELECT * FROM workout_routines ORDER BY splitType ASC, orderIndex ASC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>

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
    // 🍎 FoodItem DAO Operations
    // =========================================================================
    @Query("SELECT * FROM food_items ORDER BY isGrowthProduct DESC, name ASC")
    fun getAllFoods(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :query || '%' ORDER BY isGrowthProduct DESC, name ASC")
    fun searchFoods(query: String): Flow<List<FoodItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(foodItem: FoodItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodItem>)

    // =========================================================================
    // 🍽️ MealPlanItem DAO Operations
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
    // 🛒 ShoppingItem DAO Operations
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
    // 🧾 Purchase Receipts & Price Logs DAO Operations
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
    // 💪 Workout Exercises & Workout Logs DAO Operations
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
    // 📈 Progress & Daily Intake DAO Operations
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
}

typealias GrowthDao = AppDao
typealias UserProfileDao = AppDao
typealias DietPlanDao = AppDao
typealias WorkoutRoutineDao = AppDao
