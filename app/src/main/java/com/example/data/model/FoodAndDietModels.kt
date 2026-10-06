package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FoodCategory(val label: String) {
    PROTEIN("Proteínas"),
    CARBOHYDRATE("Carboidratos"),
    FAT("Gorduras & Oleaginosas"),
    GROWTH_SUPPLEMENT("Suplementos Growth"),
    FRUITS_VEGETABLES("Frutas & Vegetais"),
    DAIRY("Laticínios"),
    OTHER("Outros")
}

enum class MealType(val title: String, val defaultTime: String, val orderIndex: Int) {
    BREAKFAST("Café da Manhã", "07:30", 1),
    MORNING_SNACK("Lanche da Manhã", "10:30", 2),
    LUNCH("Almoço", "13:00", 3),
    PRE_WORKOUT("Pré-Treino", "16:00", 4),
    POST_WORKOUT("Pós-Treino", "18:00", 5),
    DINNER("Jantar", "20:30", 6),
    SUPPER("Ceia", "22:30", 7)
}

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: FoodCategory,
    val servingUnit: String = "g", // "g", "ml", "un", "scoop"
    val baseServingAmount: Float = 100f, // standard reference (100g)
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val isGrowthProduct: Boolean = false,
    val isCustom: Boolean = false
) {
    fun calculateMacrosForAmount(amount: Float): MacroValues {
        val factor = if (baseServingAmount > 0f) amount / baseServingAmount else 1f
        return MacroValues(
            calories = (calories * factor),
            protein = (protein * factor),
            carbs = (carbs * factor),
            fat = (fat * factor),
            fiber = (fiber * factor)
        )
    }
}

data class MacroValues(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f
)

@Entity(tableName = "meal_plan_items")
data class MealPlanItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: MealType,
    val foodItemId: Long,
    val foodName: String,
    val foodCategory: FoodCategory,
    val amount: Float, // in grams / units / scoops
    val unit: String = "g",
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val isCompletedToday: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "daily_intake_history")
data class DailyIntakeHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // "YYYY-MM-DD"
    val dateMillis: Long,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val targetCalories: Int,
    val isAdhered: Boolean = true,
    val waterDrankMl: Int = 0
)
