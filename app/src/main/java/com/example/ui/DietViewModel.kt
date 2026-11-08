package com.example.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiRepository
import com.example.data.local.PrepopulatedData
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.DietCycleType
import com.example.data.model.DietPlan
import com.example.data.model.DietaryProfile
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.FoodRecord
import com.example.data.model.Gender
import com.example.data.model.Macro
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.WeeklyDietPlan
import com.example.util.MacroCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

class DietViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    private val _isAiDietLoading = MutableStateFlow(false)
    val isAiDietLoading: StateFlow<Boolean> = _isAiDietLoading.asStateFlow()

    private val _aiDietSuggestions = MutableStateFlow<List<AiDietSuggestion>>(emptyList())
    val aiDietSuggestions: StateFlow<List<AiDietSuggestion>> = _aiDietSuggestions.asStateFlow()

    fun generateAIDietPlan(preferences: DietAiPreferences) {
        viewModelScope.launch {
            _isAiDietLoading.value = true
            try {
                val profile = userProfile.value
                val foods = allFoods.value
                val favs = foods.filter { it.isFavorite }.joinToString { it.name }
                val contextFoods = if (preferences.includePreferredFoods && favs.isNotBlank()) {
                    "Preferidos: $favs. Outros: ${foods.take(30).joinToString { it.name }}"
                } else {
                    foods.take(50).joinToString { it.name }
                }

                val json = GeminiRepository.generateDietPlan(
                    calories = profile.targetCalories,
                    protein = profile.targetProteinGrams,
                    carbs = profile.targetCarbsGrams,
                    fat = profile.targetFatGrams,
                    mealsCount = preferences.mealsCount,
                    restrictions = preferences.restrictions,
                    preferences = preferences.preferences,
                    availableFoods = contextFoods
                )

                if (json != null) {
                    val list = mutableListOf<AiDietSuggestion>()
                    val array = JSONArray(json)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            AiDietSuggestion(
                                mealType = MealType.valueOf(obj.getString("mealType")),
                                foodName = obj.getString("foodName"),
                                amount = obj.getDouble("amount").toFloat(),
                                unit = obj.getString("unit"),
                                calories = obj.getDouble("calories").toFloat(),
                                protein = obj.getDouble("protein").toFloat(),
                                carbs = obj.getDouble("carbs").toFloat(),
                                fat = obj.getDouble("fat").toFloat(),
                                notes = obj.optString("notes")
                            )
                        )
                    }
                    _aiDietSuggestions.value = list
                } else {
                    showToast("Falha ao gerar dieta com IA. Tente novamente.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Erro na IA: ${e.message}")
            } finally {
                _isAiDietLoading.value = false
            }
        }
    }

    fun applyAiDiet() {
        viewModelScope.launch {
            val suggestions = _aiDietSuggestions.value
            if (suggestions.isEmpty()) return@launch

            suggestions.forEach { suggestion ->
                val matchingFood = allFoods.value.find { it.name.contains(suggestion.foodName, ignoreCase = true) }
                val mealItem = MealPlanItem(
                    mealType = suggestion.mealType,
                    foodItemId = matchingFood?.id ?: 0,
                    foodName = suggestion.foodName,
                    foodCategory = matchingFood?.category ?: FoodCategory.OTHER,
                    amount = suggestion.amount,
                    unit = suggestion.unit,
                    calories = suggestion.calories,
                    protein = suggestion.protein,
                    carbs = suggestion.carbs,
                    fat = suggestion.fat,
                    notes = suggestion.notes
                )
                repository.addMealPlanItem(mealItem)
            }
            repository.generateAutomatedShoppingList()
            _aiDietSuggestions.value = emptyList()
            syncController.enqueueChange("Dieta", "Plano alimentar IA (${suggestions.size} itens)")
            showToast("Plano alimentar IA aplicado e Lista de Compras sincronizada!")
        }
    }

    fun suggestAIMealItems(mealType: MealType, restrictions: String = "", preferences: String = "") {
        viewModelScope.launch {
            _isAiDietLoading.value = true
            try {
                val profile = userProfile.value
                val mealTargetCals = (profile.targetCalories / 5)
                val foods = allFoods.value
                val contextFoods = foods.take(40).joinToString { it.name }

                val json = GeminiRepository.suggestMealItems(
                    mealType = mealType.name,
                    targetCalories = mealTargetCals,
                    restrictions = restrictions,
                    preferences = preferences,
                    availableFoods = contextFoods
                )

                if (json != null) {
                    val array = JSONArray(json)
                    val list = mutableListOf<AiDietSuggestion>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            AiDietSuggestion(
                                mealType = mealType,
                                foodName = obj.getString("foodName"),
                                amount = obj.getDouble("amount").toFloat(),
                                unit = obj.getString("unit"),
                                calories = obj.getDouble("calories").toFloat(),
                                protein = obj.getDouble("protein").toFloat(),
                                carbs = obj.getDouble("carbs").toFloat(),
                                fat = obj.getDouble("fat").toFloat(),
                                notes = obj.optString("notes")
                            )
                        )
                    }
                    _aiDietSuggestions.value = list
                }
            } catch (e: Exception) {
                showToast("Erro ao sugerir alimentos: ${e.message}")
            } finally {
                _isAiDietLoading.value = false
            }
        }
    }

    fun clearAiSuggestions() {
        _aiDietSuggestions.value = emptyList()
    }

    fun reloadFoodDatabase() {
        viewModelScope.launch {
            try {
                repository.insertFoods(PrepopulatedData.defaultFoods)
                showToast("Catálogo atualizado com ${PrepopulatedData.defaultFoods.size} alimentos!")
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Erro ao restaurar base alimentar: ${e.message}")
            }
        }
    }

    fun saveRoomDietaryProfile(name: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            repository.saveDietaryProfile(
                DietaryProfile(
                    name = name,
                    targetCalories = calories,
                    proteinGoal = protein,
                    carbGoal = carbs,
                    fatGoal = fat
                )
            )
            showToast("Perfil dietético salvo no Room!")
        }
    }

    fun saveRoomMacro(date: Long, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            repository.saveMacro(
                Macro(
                    date = date,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            )
            showToast("Macros salvos com sucesso!")
        }
    }

    fun updateProfileAndCalculateMacros(
        name: String,
        gender: Gender,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        proteinGPerKg: Float,
        fatGPerKg: Float,
        monthlyTargetWeightKg: Float,
        monthlyNotes: String,
        dietCycleType: DietCycleType,
        targetCalories: Int,
        biotype: BodyType = BodyType.MESOMORPH
    ) {
        viewModelScope.launch {
            val plan = MacroCalculator.computeFullPlan(
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                goal = goal,
                biotype = biotype,
                customProteinGPerKg = proteinGPerKg,
                customFatGPerKg = fatGPerKg,
                isAthleteOrWarmClimate = true,
                customTargetCalories = targetCalories
            )

            val current = userProfile.value
            val updated = current.copy(
                name = name,
                gender = gender,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                activityLevel = activityLevel,
                goal = goal,
                biotype = biotype,
                dietCycleType = dietCycleType,
                targetCalories = plan.targetCalories,
                targetProteinGrams = plan.proteinGrams,
                targetCarbsGrams = plan.carbsGrams,
                targetFatGrams = plan.fatGrams,
                targetWaterMl = plan.waterMl,
                monthlyTargetWeightKg = monthlyTargetWeightKg,
                monthlyNotes = monthlyNotes
            )

            repository.saveUserProfile(updated)
            if (updated.firebaseUid.isNotBlank() && !updated.firebaseUid.startsWith("guest_")) {
                firestoreSync.recordLocalProfileMutation(updated.firebaseUid)
                firestoreSync.syncProfileToCloud(updated)
                firestoreSync.syncCaloricGoalsToCloud(
                    targetCalories = updated.targetCalories,
                    targetProteinGrams = updated.targetProteinGrams,
                    targetCarbsGrams = updated.targetCarbsGrams,
                    targetFatGrams = updated.targetFatGrams,
                    targetWaterMl = updated.targetWaterMl,
                    customUid = updated.firebaseUid
                )
            }
            repository.saveDietaryProfile(
                DietaryProfile(
                    id = 1,
                    name = name.ifBlank { "Dieta Principal" },
                    targetCalories = plan.targetCalories,
                    proteinGoal = plan.proteinGrams,
                    carbGoal = plan.carbsGrams,
                    fatGoal = plan.fatGrams
                )
            )
            repository.saveMacro(
                Macro(
                    date = System.currentTimeMillis(),
                    calories = plan.targetCalories,
                    protein = plan.proteinGrams,
                    carbs = plan.carbsGrams,
                    fat = plan.fatGrams
                )
            )
            showToast("Metas e perfil atualizados com sucesso! 💪")
        }
    }

    fun addWater(amountMl: Int) {
        shared.setWaterDrankMl((waterDrankMl.value + amountMl).coerceIn(0, 10000))
        shared.persistSelectedIntake()
    }

    fun resetWater() {
        shared.setWaterDrankMl(0)
        shared.persistSelectedIntake()
    }

    fun updateDailyWaterGoal(newGoalMl: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(targetWaterMl = newGoalMl.coerceIn(500, 10000))
            repository.saveUserProfile(updated)
            firestoreSync.syncCaloricGoalsToCloud(
                targetCalories = updated.targetCalories,
                targetProteinGrams = updated.targetProteinGrams,
                targetCarbsGrams = updated.targetCarbsGrams,
                targetFatGrams = updated.targetFatGrams,
                targetWaterMl = updated.targetWaterMl,
                customUid = updated.firebaseUid
            )
            showToast("Meta diária de água atualizada para ${updated.targetWaterMl} ml!")
        }
    }

    fun updateCaloricGoals(calories: Int, protein: Int, carbs: Int, fat: Int, water: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(
                targetCalories = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWaterMl = water
            )
            repository.saveUserProfile(updated)
            firestoreSync.syncCaloricGoalsToCloud(
                targetCalories = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWaterMl = water,
                customUid = updated.firebaseUid
            )
            firestoreSync.syncProfileToCloud(updated)
            syncController.enqueueChange("Dieta", "Metas calóricas (${calories} kcal)")
            showToast("Metas salvas no aparelho e adicionadas à fila de sincronização.")
        }
    }

    fun addFoodToMeal(
        mealType: MealType,
        food: FoodItem,
        amount: Float,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val macros = food.calculateMacrosForAmount(amount)
            val mealPlanItem = MealPlanItem(
                mealType = mealType,
                foodItemId = food.id,
                foodName = food.name,
                foodCategory = food.category,
                amount = amount,
                unit = food.servingUnit,
                calories = macros.calories,
                protein = macros.protein,
                carbs = macros.carbs,
                fat = macros.fat,
                fiber = macros.fiber,
                notes = notes
            )
            repository.addMealPlanItem(mealPlanItem)
            repository.generateAutomatedShoppingList()
            syncController.enqueueChange("Dieta", "${food.name} (${amount.toInt()}${food.servingUnit}) em ${mealType.title}")
            showToast("${food.name} adicionado ao ${mealType.title} e sincronizado na Lista de Compras!")
        }
    }

    fun removeMealItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMealPlanItem(id)
            repository.generateAutomatedShoppingList()
            syncController.enqueueChange("Dieta", "Item removido do plano alimentar")
        }
    }

    fun saveDietPlan(dietPlan: DietPlan) {
        viewModelScope.launch {
            if (dietPlan.id == 0L) {
                repository.addDietPlan(dietPlan)
                showToast("Plano de dieta criado!")
            } else {
                repository.updateDietPlan(dietPlan)
                showToast("Plano de dieta atualizado!")
            }
        }
    }

    fun deleteDietPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteDietPlan(id)
            showToast("Plano de dieta removido!")
        }
    }

    fun saveWeeklyDietPlan(plan: WeeklyDietPlan) {
        viewModelScope.launch {
            if (plan.id == 0L) {
                repository.addWeeklyDietPlan(plan)
            } else {
                repository.updateWeeklyDietPlan(plan)
            }
            showToast("Meta semanal salva!")
        }
    }

    fun logFoodConsumption(record: FoodRecord) {
        viewModelScope.launch {
            val normalized = record.copy(
                dateString = selectedDateKey(),
                dateMillis = shared.selectedDateWithCurrentTime()
            )
            val insertedId = repository.addFoodRecord(normalized)
            firestoreSync.syncFoodRecordToCloud(normalized.copy(id = insertedId), userProfile.value.firebaseUid)
            shared.persistIntakeForDate(selectedDateKey())
            syncController.enqueueChange("Dieta", "Registro: ${record.foodName} (${record.calories.toInt()} kcal)")
            showToast("${record.foodName} registrado no seu diário alimentar!")
        }
    }

    fun deleteFoodRecord(id: Long) {
        viewModelScope.launch {
            val affectedDate = repository.allFoodRecords.firstOrNull().orEmpty()
                .firstOrNull { it.id == id }?.dateString ?: selectedDateKey()
            repository.deleteFoodRecord(id)
            firestoreSync.deleteFoodRecordFromCloud(id, userProfile.value.firebaseUid)
            shared.persistIntakeForDate(affectedDate)
            syncController.enqueueChange("Dieta", "Remoção de registro no diário alimentar")
            showToast("Alimento removido do diário!")
        }
    }

    fun toggleMealItemCompleted(
        item: MealPlanItem,
        dateKey: String = shared.currentDateKey(),
        isCompletedForDate: Boolean = item.isCompletedToday
    ) {
        viewModelScope.launch {
            if (!isCompletedForDate) {
                val recordDate = runCatching {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)?.time
                }.getOrNull() ?: System.currentTimeMillis()
                val record = FoodRecord(
                    dateString = dateKey,
                    dateMillis = recordDate,
                    mealType = item.mealType,
                    foodItemId = item.foodItemId,
                    foodName = item.foodName,
                    foodCategory = item.foodCategory,
                    amount = item.amount,
                    unit = item.unit,
                    calories = item.calories,
                    protein = item.protein,
                    carbs = item.carbs,
                    fat = item.fat,
                    fiber = item.fiber,
                    notes = item.notes
                )
                val insertedId = repository.addFoodRecord(record)
                firestoreSync.syncFoodRecordToCloud(record.copy(id = insertedId), userProfile.value.firebaseUid)
            } else {
                val record = repository.allFoodRecords.firstOrNull().orEmpty()
                    .filter { it.dateString == dateKey && it.foodItemId == item.foodItemId && it.mealType == item.mealType }
                    .maxByOrNull { it.dateMillis }
                if (record != null) {
                    repository.deleteFoodRecord(record.id)
                    firestoreSync.deleteFoodRecordFromCloud(record.id, userProfile.value.firebaseUid)
                }
            }
            if (dateKey == shared.currentDateKey()) {
                repository.toggleMealCompletion(item.id, isCompletedForDate)
            }
            shared.persistIntakeForDate(dateKey)
            syncController.enqueueChange("Dieta", "Check refeição: ${item.foodName}")
        }
    }

    fun toggleMealItemFixed(item: MealPlanItem) {
        viewModelScope.launch {
            val nextState = !item.isFixed
            repository.toggleMealFixed(item.id, item.isFixed)
            if (nextState) {
                shared.autoLogFixedItems()
            }
        }
    }

    fun addCustomFoodItem(
        name: String,
        category: FoodCategory,
        servingAmount: Float,
        servingUnit: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        fiber: Float,
        isGrowthProduct: Boolean = false
    ) {
        viewModelScope.launch {
            val newFood = FoodItem(
                name = name,
                category = category,
                baseServingAmount = servingAmount,
                servingUnit = servingUnit,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = fiber,
                isGrowthProduct = isGrowthProduct,
                isCustom = true
            )
            repository.addFoodItem(newFood)
            showToast("Alimento '$name' salvo na sua tabela!")
        }
    }

    fun toggleFoodFavorite(food: FoodItem) {
        viewModelScope.launch {
            val nextState = !food.isFavorite
            repository.toggleFoodFavorite(food, userProfile.value.firebaseUid)
            firestoreSync.syncFavoriteFoodToCloud(food.copy(isFavorite = nextState), userProfile.value.firebaseUid)
            val msg = if (nextState) "adicionado aos favoritos" else "removido dos favoritos"
            showToast("'${food.name}' $msg")
        }
    }

    fun generateShoppingList(periodDays: Int = 30) {
        viewModelScope.launch {
            repository.generateAutomatedShoppingList(periodDays)
            syncController.enqueueChange("Dieta", "Lista de compras recalculada ($periodDays dias)")
            showToast("Lista de compras gerada para $periodDays dias!")
        }
    }

    fun formatDietPlanForSharing(): String {
        val diet = activeDietPlan.value
        val meals = mealPlanItems.value
        val sb = StringBuilder()
        sb.append("🥗 *Plano Alimentar VIRTUS*\n")
        if (diet != null) {
            sb.append("🎯 *Metas Diárias:* ${diet.targetCalories} kcal | P: ${diet.targetProteinGrams}g | C: ${diet.targetCarbsGrams}g | G: ${diet.targetFatGrams}g\n\n")
        }
        sb.append("*Refeições:*\n")
        MealType.values().forEach { mealType ->
            val itemsForMeal = meals.filter { it.mealType == mealType }
            if (itemsForMeal.isNotEmpty()) {
                sb.append("\n📌 *${mealType.title}*\n")
                itemsForMeal.forEach { item ->
                    sb.append("• ${item.foodName} (${item.amount.toInt()}${item.unit}) - ${item.calories.toInt()} kcal\n")
                }
            }
        }
        sb.append("\n_Foco na dieta! Gerado pelo VIRTUS Fit App_")
        return sb.toString()
    }
}
