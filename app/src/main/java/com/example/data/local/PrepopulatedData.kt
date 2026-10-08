package com.example.data.local

import com.example.data.model.DietCycleType
import com.example.data.model.DietPlan
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrepopulatedData {

    val defaultFoods = listOf(
        FoodItem(id = 1, name = "100% Whey Protein Concentrado", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f, fiber = 0f, isGrowthProduct = false),
        FoodItem(id = 2, name = "100% Whey Protein Isolado", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 110f, protein = 27f, carbs = 0.5f, fat = 0.3f, fiber = 0f, isGrowthProduct = false),
        FoodItem(id = 3, name = "Creatina Monohidratada 100% Pura", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = false),
        FoodItem(id = 4, name = "Pasta de Amendoim Integral", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 15f, calories = 88f, protein = 4.2f, carbs = 2.7f, fat = 7.4f, fiber = 1.2f, isGrowthProduct = false),
        FoodItem(id = 5, name = "Albumina 80%", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 114f, protein = 24f, carbs = 1.5f, fat = 0f, fiber = 0f, isGrowthProduct = false),
        FoodItem(id = 6, name = "Hipercalórico 7000", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 120f, calories = 460f, protein = 30f, carbs = 76f, fat = 3.5f, fiber = 2.0f, isGrowthProduct = false),

        FoodItem(id = 7, name = "Peito de Frango Grelhado / Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 165f, protein = 31f, carbs = 0f, fat = 3.6f, fiber = 0f),
        FoodItem(id = 8, name = "Carne Moída Patinho Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 219f, protein = 35.9f, carbs = 0f, fat = 7.3f, fiber = 0f),
        FoodItem(id = 9, name = "Ovo de Galinha Inteiro Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 50f, calories = 78f, protein = 6.3f, carbs = 0.6f, fat = 5.3f, fiber = 0f),
        FoodItem(id = 10, name = "Clara de Ovo Cozida / Pasteurizada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 11f, carbs = 0.7f, fat = 0.2f, fiber = 0f),
        FoodItem(id = 11, name = "Filé de Tilápia Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 128f, protein = 26f, carbs = 0f, fat = 2.7f, fiber = 0f),
        FoodItem(id = 12, name = "Queijo Cottage", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 98f, protein = 11f, carbs = 3.4f, fat = 4.3f, fiber = 0f),

        FoodItem(id = 13, name = "Arroz Branco Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 130f, protein = 2.7f, carbs = 28.2f, fat = 0.3f, fiber = 0.4f),
        FoodItem(id = 14, name = "Arroz Integral Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 2.6f, carbs = 25.8f, fat = 1.0f, fiber = 2.7f),
        FoodItem(id = 15, name = "Batata Doce Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 86f, protein = 1.6f, carbs = 20.1f, fat = 0.1f, fiber = 3.0f),
        FoodItem(id = 16, name = "Aveia em Flocos Finos", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 389f, protein = 16.9f, carbs = 66.3f, fat = 6.9f, fiber = 10.6f),
        FoodItem(id = 17, name = "Pão 100% Integral", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 122f, protein = 5.2f, carbs = 23f, fat = 1.4f, fiber = 3.8f),
        FoodItem(id = 18, name = "Macarrão Integral Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 5.3f, carbs = 25f, fat = 0.9f, fiber = 3.9f),
        FoodItem(id = 19, name = "Tapioca / Goma Hidratada", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 240f, protein = 0f, carbs = 60f, fat = 0f, fiber = 0.2f),

        FoodItem(id = 20, name = "Azeite de Oliva Extra Virgem", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 13f, calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f, fiber = 0f),
        FoodItem(id = 21, name = "Castanha do Pará / Caju", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 656f, protein = 14.3f, carbs = 12.3f, fat = 66.4f, fiber = 7.5f),
        FoodItem(id = 22, name = "Abacate Fresco", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 160f, protein = 2.0f, carbs = 8.5f, fat = 14.7f, fiber = 6.7f),

        FoodItem(id = 23, name = "Banana Prata", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 89f, protein = 1.1f, carbs = 22.8f, fat = 0.3f, fiber = 2.6f),
        FoodItem(id = 24, name = "Maçã Gala com Casca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 0.3f, carbs = 13.8f, fat = 0.2f, fiber = 2.4f),
        FoodItem(id = 25, name = "Brócolis Cozido no Vapor", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f, fiber = 3.3f)
    )

    val defaultDietPlan = DietPlan(
        id = 1,
        name = "Plano Hipertrofia (Bulking Limpo)",
        description = "Plano nutricional de alta performance com superávit calórico controlado e foco em hipertrofia.",
        goal = FitnessGoal.BULKING_CLEAN,
        targetCalories = 2650,
        targetProteinGrams = 175,
        targetCarbsGrams = 320,
        targetFatGrams = 65,
        targetWaterMl = 3530,
        mealsCount = 5,
        cycleType = DietCycleType.FIXED_DAILY,
        isActive = true,
        notes = "Foco em bater metas diárias de proteína e água."
    )

    val defaultMealPlan = listOf(
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 9, foodName = "Ovo de Galinha Inteiro Cozido", foodCategory = FoodCategory.PROTEIN, amount = 150f, unit = "g", calories = 234f, protein = 18.9f, carbs = 1.8f, fat = 15.9f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 16, foodName = "Aveia em Flocos Finos", foodCategory = FoodCategory.CARBOHYDRATE, amount = 60f, unit = "g", calories = 233.4f, protein = 10.1f, carbs = 39.8f, fat = 4.1f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 23, foodName = "Banana Prata", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 120f, unit = "g", calories = 106.8f, protein = 1.3f, carbs = 27.4f, fat = 0.4f),

        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 7, foodName = "Peito de Frango Grelhado / Cozido", foodCategory = FoodCategory.PROTEIN, amount = 180f, unit = "g", calories = 297f, protein = 55.8f, carbs = 0f, fat = 6.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 13, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 220f, unit = "g", calories = 286f, protein = 5.9f, carbs = 62.0f, fat = 0.7f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 20, foodName = "Azeite de Oliva Extra Virgem", foodCategory = FoodCategory.FAT, amount = 12f, unit = "g", calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 25, foodName = "Brócolis Cozido no Vapor", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 100f, unit = "g", calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f),

        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 1, foodName = "100% Whey Protein Concentrado", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 30f, unit = "g", calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 3, foodName = "Creatina Monohidratada 100% Pura", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 5f, unit = "g", calories = 0f, protein = 0f, carbs = 0f, fat = 0f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 15, foodName = "Batata Doce Cozida", foodCategory = FoodCategory.CARBOHYDRATE, amount = 180f, unit = "g", calories = 154.8f, protein = 2.9f, carbs = 36.2f, fat = 0.2f),

        MealPlanItem(mealType = MealType.DINNER, foodItemId = 8, foodName = "Carne Moída Patinho Grelhado", foodCategory = FoodCategory.PROTEIN, amount = 160f, unit = "g", calories = 350.4f, protein = 57.4f, carbs = 0f, fat = 11.7f),
        MealPlanItem(mealType = MealType.DINNER, foodItemId = 13, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 200f, unit = "g", calories = 260f, protein = 5.4f, carbs = 56.4f, fat = 0.6f),

        MealPlanItem(mealType = MealType.SUPPER, foodItemId = 4, foodName = "Pasta de Amendoim Integral", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 20f, unit = "g", calories = 117.3f, protein = 5.6f, carbs = 3.6f, fat = 9.9f)
    )

    // Complete Pre-populated Routines for AB, ABC, and ABCDE
    val defaultRoutines = emptyList<WorkoutRoutine>()

    val defaultExercises = emptyList<WorkoutExercise>()

    // Prepopulated Price History to show inflation tracking
    val sampleReceipts = listOf(
        PurchaseReceipt(
            id = 1,
            storeName = "Supermercado Atacadão",
            receiptNumber = "NFe-352608-4921",
            dateMillis = System.currentTimeMillis() - (45 * 86400000L),
            dateDisplay = "22/08/2026",
            monthYearKey = "2026-08",
            totalAmount = 345.80f,
            itemCount = 5,
            notes = "Compras do mês de Agosto (Preço Base)"
        ),
        PurchaseReceipt(
            id = 2,
            storeName = "Supermercado Atacadão & Suplementos",
            receiptNumber = "NFe-352609-8812",
            dateMillis = System.currentTimeMillis() - (15 * 86400000L),
            dateDisplay = "21/09/2026",
            monthYearKey = "2026-09",
            totalAmount = 372.40f,
            itemCount = 5,
            notes = "Compras de Setembro com variação de preços"
        )
    )

    val samplePurchaseLogs = listOf(
        // Month 1 (Agosto)
        PurchaseItemLog(receiptId = 1, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 19.50f, totalPricePaid = 117.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 18.90f, totalPricePaid = 75.60f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 28.50f, totalPricePaid = 28.50f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "100% Whey Protein Concentrado", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 100.00f, totalPricePaid = 100.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Loja de Suplementos"),
        PurchaseItemLog(receiptId = 1, foodName = "Pasta de Amendoim", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 24.70f, totalPricePaid = 24.70f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Loja de Suplementos"),

        // Month 2 (Setembro) - with realistic inflation changes
        PurchaseItemLog(receiptId = 2, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 21.90f, totalPricePaid = 131.40f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 21.50f, totalPricePaid = 86.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 29.90f, totalPricePaid = 29.90f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "100% Whey Protein Concentrado", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 105.00f, totalPricePaid = 105.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Loja de Suplementos"),
        PurchaseItemLog(receiptId = 2, foodName = "Pasta de Amendoim", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 26.00f, totalPricePaid = 26.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Loja de Suplementos")
    )
}
