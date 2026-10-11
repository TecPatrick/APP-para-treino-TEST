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
        // Growth Supplements & Sports Nutrition
        FoodItem(id = 1, name = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 2, name = "100% Whey Protein Isolado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 110f, protein = 27f, carbs = 0.5f, fat = 0.3f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 3, name = "Creatina Monohidratada 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 4, name = "Pasta de Amendoim Integral Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 15f, calories = 88f, protein = 4.2f, carbs = 2.7f, fat = 7.4f, fiber = 1.2f, isGrowthProduct = true),
        FoodItem(id = 5, name = "Albumina 80% Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 114f, protein = 24f, carbs = 1.5f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 6, name = "Hipercalórico 7000 Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 120f, calories = 460f, protein = 30f, carbs = 76f, fat = 3.5f, fiber = 2.0f, isGrowthProduct = true),
        FoodItem(id = 601, name = "Glutamina 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 20f, protein = 5f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 602, name = "BCAA Powder Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 6f, calories = 24f, protein = 6f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 603, name = "Multivitamínico Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "cap", baseServingAmount = 1f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),

        // Proteins & Eggs (FatSecret Brazil Standard)
        FoodItem(id = 7, name = "Peito de Frango Grelhado / Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 165f, protein = 31f, carbs = 0f, fat = 3.6f, fiber = 0f),
        FoodItem(id = 8, name = "Carne Moída Patinho Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 219f, protein = 35.9f, carbs = 0f, fat = 7.3f, fiber = 0f),
        FoodItem(id = 9, name = "Ovo de Galinha Inteiro Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 50f, calories = 78f, protein = 6.3f, carbs = 0.6f, fat = 5.3f, fiber = 0f),
        FoodItem(id = 1201, name = "Ovo Frito no Azeite de Oliva (1 unidade)", category = FoodCategory.PROTEIN, servingUnit = "unidade", baseServingAmount = 1f, calories = 95f, protein = 6.3f, carbs = 0.6f, fat = 7.5f, fiber = 0f),
        FoodItem(id = 1202, name = "Ovo Frito na Manteiga (1 unidade)", category = FoodCategory.PROTEIN, servingUnit = "unidade", baseServingAmount = 1f, calories = 102f, protein = 6.3f, carbs = 0.6f, fat = 8.3f, fiber = 0f),
        FoodItem(id = 1203, name = "Ovos Mexidos com Azeite", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 172f, protein = 12.0f, carbs = 0.8f, fat = 13.2f, fiber = 0f),
        FoodItem(id = 10, name = "Clara de Ovo Cozida / Pasteurizada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 11f, carbs = 0.7f, fat = 0.2f, fiber = 0f),
        FoodItem(id = 11, name = "Filé de Tilápia Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 128f, protein = 26f, carbs = 0f, fat = 2.7f, fiber = 0f),
        FoodItem(id = 701, name = "Alcatra Bovina Grelhada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 222f, protein = 31.5f, carbs = 0f, fat = 9.8f, fiber = 0f),
        FoodItem(id = 702, name = "Contrafilé Bovino Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 251f, protein = 29.8f, carbs = 0f, fat = 13.8f, fiber = 0f),
        FoodItem(id = 703, name = "Coxão Mole Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 210f, protein = 32.0f, carbs = 0f, fat = 8.5f, fiber = 0f),
        FoodItem(id = 704, name = "Salmão Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 206f, protein = 22.1f, carbs = 0f, fat = 12.3f, fiber = 0f),
        FoodItem(id = 705, name = "Atum Enlatado em Água (Drenado)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 103f, protein = 23.6f, carbs = 0f, fat = 0.9f, fiber = 0f),
        FoodItem(id = 706, name = "Sardinha em Lata (Óleo/Água)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 164f, protein = 24.6f, carbs = 0f, fat = 6.8f, fiber = 0f),
        FoodItem(id = 707, name = "Lombo Suíno Assado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 185f, protein = 30.5f, carbs = 0f, fat = 6.2f, fiber = 0f),
        FoodItem(id = 708, name = "Frango Desfiado Temperado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 158f, protein = 29.5f, carbs = 0.5f, fat = 3.8f, fiber = 0f),
        FoodItem(id = 709, name = "Strogonoff de Frango Caseiro", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 195f, protein = 18.0f, carbs = 4.2f, fat = 11.5f, fiber = 0.4f),
        FoodItem(id = 710, name = "Hambúrguer Caseiro Bovino Grelhado (100g)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 235f, protein = 26.0f, carbs = 1.0f, fat = 13.5f, fiber = 0f),

        // Dairy & Eggs (Laticínios)
        FoodItem(id = 12, name = "Queijo Cottage", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 98f, protein = 11f, carbs = 3.4f, fat = 4.3f, fiber = 0f),
        FoodItem(id = 801, name = "Queijo Minas Frescal", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 245f, protein = 17.4f, carbs = 3.2f, fat = 18.0f, fiber = 0f),
        FoodItem(id = 802, name = "Queijo Mussarela Fatiado", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 330f, protein = 22.6f, carbs = 2.2f, fat = 25.5f, fiber = 0f),
        FoodItem(id = 803, name = "Ricota Fresca", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 140f, protein = 11.3f, carbs = 3.5f, fat = 9.0f, fiber = 0f),
        FoodItem(id = 804, name = "Iogurte Natural Desnatado", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 41f, protein = 5.0f, carbs = 6.0f, fat = 0.4f, fiber = 0f),
        FoodItem(id = 805, name = "Iogurte Grego Tradicional", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 97f, protein = 5.3f, carbs = 4.0f, fat = 6.8f, fiber = 0f),
        FoodItem(id = 806, name = "Leite Desnatado Vaca", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 68f, protein = 6.6f, carbs = 10.0f, fat = 0.4f, fiber = 0f),
        FoodItem(id = 807, name = "Leite Integral Vaca", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 120f, protein = 6.2f, carbs = 9.4f, fat = 6.4f, fiber = 0f),
        FoodItem(id = 808, name = "Requeijão Light", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 30f, calories = 55f, protein = 3.5f, carbs = 1.2f, fat = 4.0f, fiber = 0f),
        FoodItem(id = 809, name = "Leite Condensado", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 30f, calories = 98f, protein = 2.1f, carbs = 17.0f, fat = 2.4f, fiber = 0f),

        // Carbohydrates (Carboidratos & Pratos Brasileiros)
        FoodItem(id = 13, name = "Arroz Branco Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 130f, protein = 2.7f, carbs = 28.2f, fat = 0.3f, fiber = 0.4f),
        FoodItem(id = 14, name = "Arroz Integral Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 2.6f, carbs = 25.8f, fat = 1.0f, fiber = 2.7f),
        FoodItem(id = 15, name = "Batata Doce Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 86f, protein = 1.6f, carbs = 20.1f, fat = 0.1f, fiber = 3.0f),
        FoodItem(id = 901, name = "Batata Inglesa Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 87f, protein = 1.9f, carbs = 20.0f, fat = 0.1f, fiber = 1.8f),
        FoodItem(id = 1301, name = "Purê de Batata Inglesa Caseiro", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 105f, protein = 2.0f, carbs = 16.5f, fat = 3.8f, fiber = 1.4f),
        FoodItem(id = 1302, name = "Batata Frita na Airfryer", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 165f, protein = 2.5f, carbs = 32.0f, fat = 3.5f, fiber = 3.0f),
        FoodItem(id = 902, name = "Mandioca / Aipim Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 125f, protein = 0.6f, carbs = 30.1f, fat = 0.3f, fiber = 1.6f),
        FoodItem(id = 903, name = "Inhame Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 118f, protein = 1.5f, carbs = 27.9f, fat = 0.2f, fiber = 4.1f),
        FoodItem(id = 16, name = "Aveia em Flocos Finos / Grossos", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 389f, protein = 16.9f, carbs = 66.3f, fat = 6.9f, fiber = 10.6f),
        FoodItem(id = 17, name = "Pão 100% Integral", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 122f, protein = 5.2f, carbs = 23f, fat = 1.4f, fiber = 3.8f),
        FoodItem(id = 904, name = "Pão Francês Tradicional", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 135f, protein = 4.0f, carbs = 28.5f, fat = 0.5f, fiber = 1.2f),
        FoodItem(id = 1303, name = "Pão Francês com Manteiga (1 unidade)", category = FoodCategory.CARBOHYDRATE, servingUnit = "unidade", baseServingAmount = 1f, calories = 185f, protein = 4.1f, carbs = 28.6f, fat = 6.5f, fiber = 1.2f),
        FoodItem(id = 18, name = "Macarrão Cozido (Branco / Integral)", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 4.5f, carbs = 25f, fat = 0.9f, fiber = 2.5f),
        FoodItem(id = 19, name = "Tapioca / Goma Hidratada", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 240f, protein = 0f, carbs = 60f, fat = 0f, fiber = 0.2f),
        FoodItem(id = 1304, name = "Tapioca com Queijo e Frango", category = FoodCategory.CARBOHYDRATE, servingUnit = "unidade", baseServingAmount = 1f, calories = 310f, protein = 18.5f, carbs = 38.0f, fat = 9.2f, fiber = 0.5f),
        FoodItem(id = 905, name = "Cuscuz de Milho Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 112f, protein = 2.5f, carbs = 25.0f, fat = 0.4f, fiber = 1.5f),
        FoodItem(id = 906, name = "Granola Tradicional", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 431f, protein = 10.0f, carbs = 71.0f, fat = 12.0f, fiber = 7.0f),
        FoodItem(id = 907, name = "Milho Verde Cozido / Enlatado", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 98f, protein = 3.2f, carbs = 21.0f, fat = 1.5f, fiber = 2.4f),
        FoodItem(id = 1305, name = "Pão de Queijo Tradicional (Unidade média)", category = FoodCategory.CARBOHYDRATE, servingUnit = "unidade", baseServingAmount = 1f, calories = 90f, protein = 2.0f, carbs = 11.0f, fat = 4.5f, fiber = 0.3f),
        FoodItem(id = 1306, name = "Bolo de Fubá Caseiro (Fatia)", category = FoodCategory.CARBOHYDRATE, servingUnit = "fatia", baseServingAmount = 1f, calories = 210f, protein = 3.5f, carbs = 32.0f, fat = 8.0f, fiber = 1.0f),

        // Legumes e Feijões
        FoodItem(id = 950, name = "Feijão Carioca Cozido com Caldo", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 76f, protein = 4.8f, carbs = 13.6f, fat = 0.5f, fiber = 4.7f),
        FoodItem(id = 951, name = "Feijão Preto Cozido com Caldo", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 77f, protein = 4.5f, carbs = 14.0f, fat = 0.5f, fiber = 5.5f),
        FoodItem(id = 952, name = "Lentilha Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 116f, protein = 9.0f, carbs = 20.1f, fat = 0.4f, fiber = 7.9f),
        FoodItem(id = 953, name = "Grão de Bico Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 164f, protein = 8.9f, carbs = 27.4f, fat = 2.6f, fiber = 7.6f),

        // Fats (Gorduras e Oleaginosas)
        FoodItem(id = 20, name = "Azeite de Oliva Extra Virgem", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 13f, calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f, fiber = 0f),
        FoodItem(id = 21, name = "Castanha do Pará / Caju", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 656f, protein = 14.3f, carbs = 12.3f, fat = 66.4f, fiber = 7.5f),
        FoodItem(id = 22, name = "Abacate Fresco", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 160f, protein = 2.0f, carbs = 8.5f, fat = 14.7f, fiber = 6.7f),
        FoodItem(id = 1001, name = "Amendoim Torrado Sem Sal", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 588f, protein = 23.7f, carbs = 17.5f, fat = 49.2f, fiber = 8.5f),
        FoodItem(id = 1002, name = "Manteiga Tradicional com Sal", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 10f, calories = 74f, protein = 0.1f, carbs = 0.1f, fat = 8.2f, fiber = 0f),

        // Fruits & Vegetables (Frutas e Vegetais)
        FoodItem(id = 23, name = "Banana Prata / Nanica", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 89f, protein = 1.1f, carbs = 22.8f, fat = 0.3f, fiber = 2.6f),
        FoodItem(id = 24, name = "Maçã Gala / Fuji com Casca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 0.3f, carbs = 13.8f, fat = 0.2f, fiber = 2.4f),
        FoodItem(id = 25, name = "Brócolis Cozido no Vapor", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f, fiber = 3.3f),
        FoodItem(id = 1101, name = "Mamão Papaia", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 43f, protein = 0.5f, carbs = 10.8f, fat = 0.3f, fiber = 1.7f),
        FoodItem(id = 1102, name = "Melancia Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 30f, protein = 0.6f, carbs = 7.6f, fat = 0.2f, fiber = 0.4f),
        FoodItem(id = 1103, name = "Morango Fresco", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 32f, protein = 0.7f, carbs = 7.7f, fat = 0.3f, fiber = 2.0f),
        FoodItem(id = 1104, name = "Abacaxi Pérola", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 50f, protein = 0.5f, carbs = 13.1f, fat = 0.1f, fiber = 1.4f),
        FoodItem(id = 1105, name = "Alface Crespa / Lisa", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 15f, protein = 1.3f, carbs = 2.9f, fat = 0.2f, fiber = 1.3f),
        FoodItem(id = 1106, name = "Tomate Italiano / Salada", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 18f, protein = 0.9f, carbs = 3.9f, fat = 0.2f, fiber = 1.2f),
        FoodItem(id = 1107, name = "Cenoura Cozida / Crua", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 34f, protein = 0.8f, carbs = 7.7f, fat = 0.2f, fiber = 2.6f),
        FoodItem(id = 1108, name = "Couve Refogada no Azeite", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 70f, protein = 2.5f, carbs = 7.0f, fat = 3.5f, fiber = 3.1f),
        FoodItem(id = 1109, name = "Abobrinha Italiana Cozida", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 19f, protein = 1.2f, carbs = 3.5f, fat = 0.2f, fiber = 1.2f),
        FoodItem(id = 1110, name = "Açaí na Tigela (com Xarope e Banana)", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 110f, protein = 1.5f, carbs = 22.0f, fat = 2.5f, fiber = 2.0f)
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
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 1201, foodName = "Ovo Frito no Azeite de Oliva (1 unidade)", foodCategory = FoodCategory.PROTEIN, amount = 2f, unit = "unidade", calories = 190f, protein = 12.6f, carbs = 1.2f, fat = 15.0f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 16, foodName = "Aveia em Flocos Finos / Grossos", foodCategory = FoodCategory.CARBOHYDRATE, amount = 60f, unit = "g", calories = 233.4f, protein = 10.1f, carbs = 39.8f, fat = 4.1f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 23, foodName = "Banana Prata / Nanica", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 120f, unit = "g", calories = 106.8f, protein = 1.3f, carbs = 27.4f, fat = 0.4f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 7, foodName = "Peito de Frango Grelhado / Cozido", foodCategory = FoodCategory.PROTEIN, amount = 180f, unit = "g", calories = 297f, protein = 55.8f, carbs = 0f, fat = 6.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 13, foodName = "Arroz Branco Cozido", category = FoodCategory.CARBOHYDRATE, amount = 220f, unit = "g", calories = 286f, protein = 5.9f, carbs = 62.0f, fat = 0.7f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 20, foodName = "Azeite de Oliva Extra Virgem", category = FoodCategory.FAT, amount = 12f, unit = "g", calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 25, foodName = "Brócolis Cozido no Vapor", category = FoodCategory.FRUITS_VEGETABLES, amount = 100f, unit = "g", calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 1, foodName = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, amount = 30f, unit = "g", calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 3, foodName = "Creatina Monohidratada 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, amount = 5f, unit = "g", calories = 0f, protein = 0f, carbs = 0f, fat = 0f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 15, foodName = "Batata Doce Cozida", category = FoodCategory.CARBOHYDRATE, amount = 180f, unit = "g", calories = 154.8f, protein = 2.9f, carbs = 36.2f, fat = 0.2f),
        MealPlanItem(mealType = MealType.DINNER, foodItemId = 8, foodName = "Carne Moída Patinho Grelhado", category = FoodCategory.PROTEIN, amount = 160f, unit = "g", calories = 350.4f, protein = 57.4f, carbs = 0f, fat = 11.7f),
        MealPlanItem(mealType = MealType.DINNER, foodItemId = 13, foodName = "Arroz Branco Cozido", category = FoodCategory.CARBOHYDRATE, amount = 200f, unit = "g", calories = 260f, protein = 5.4f, carbs = 56.4f, fat = 0.6f),
        MealPlanItem(mealType = MealType.SUPPER, foodItemId = 4, foodName = "Pasta de Amendoim Integral Growth", category = FoodCategory.GROWTH_SUPPLEMENT, amount = 20f, unit = "g", calories = 117.3f, protein = 5.6f, carbs = 3.6f, fat = 9.9f)
    )

    val defaultRoutines = emptyList<WorkoutRoutine>()
    val defaultExercises = emptyList<WorkoutExercise>()

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
            storeName = "Supermercado Atacadão & Growth",
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
        PurchaseItemLog(receiptId = 1, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 19.50f, totalPricePaid = 117.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 18.90f, totalPricePaid = 75.60f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 28.50f, totalPricePaid = 28.50f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 100.00f, totalPricePaid = 100.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Growth Suplementos"),
        PurchaseItemLog(receiptId = 1, foodName = "Pasta de Amendoim Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 24.70f, totalPricePaid = 24.70f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Growth Suplementos"),

        PurchaseItemLog(receiptId = 2, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 21.90f, totalPricePaid = 131.40f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 21.50f, totalPricePaid = 86.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 29.90f, totalPricePaid = 29.90f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 105.00f, totalPricePaid = 105.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Growth Suplementos"),
        PurchaseItemLog(receiptId = 2, foodName = "Pasta de Amendoim Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 26.00f, totalPricePaid = 26.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Growth Suplementos")
    )
}
