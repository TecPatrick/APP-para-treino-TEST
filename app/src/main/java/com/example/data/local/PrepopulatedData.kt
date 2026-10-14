package com.example.data.local

import com.example.data.model.DietCycleType
import com.example.data.model.DietPlan
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.FoodRecord
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.WeeklyDietPlan
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrepopulatedData {

    val defaultFoods = listOf(
        // --- Suplementos Growth ---
        FoodItem(id = 1, name = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 2, name = "100% Whey Protein Isolado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 110f, protein = 27f, carbs = 0.5f, fat = 0.3f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 3, name = "Creatina Monohidratada 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 4, name = "Pasta de Amendoim Integral Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 15f, calories = 88f, protein = 4.2f, carbs = 2.7f, fat = 7.4f, fiber = 1.2f, isGrowthProduct = true),
        FoodItem(id = 5, name = "Albumina 80% Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 114f, protein = 24f, carbs = 1.5f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 6, name = "Hipercalórico 7000 Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 120f, calories = 460f, protein = 30f, carbs = 76f, fat = 3.5f, fiber = 2.0f, isGrowthProduct = true),
        FoodItem(id = 7, name = "Glutamina 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 20f, protein = 5f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 8, name = "Multivitamínico Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 1f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 9, name = "100% Whey Protein Hidrolisado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 112f, protein = 26f, carbs = 0.6f, fat = 0.4f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 10, name = "Medium Whey Protein Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 125f, protein = 18f, carbs = 8.5f, fat = 2.0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 11, name = "Soy Protein Isolada Growth (Soja)", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 115f, protein = 27f, carbs = 0.5f, fat = 0.4f, fiber = 0.5f, isGrowthProduct = true),
        FoodItem(id = 12, name = "Creatina Creapure 100% Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 13, name = "Beta-Alanina 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 3f, calories = 12f, protein = 3f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 14, name = "BCAA 2:1:1 Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 20f, protein = 5f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 15, name = "Óleo de Peixe Ômega 3 Growth (2 caps)", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "un", baseServingAmount = 2f, calories = 18f, protein = 0.4f, carbs = 0f, fat = 2.0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 16, name = "Cafeína 200mg Growth (1 caps)", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "un", baseServingAmount = 1f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 17, name = "ZMA Quelato Growth (1 caps)", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "un", baseServingAmount = 1f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 18, name = "Pré-Treino Hórus Growth (dose 8g)", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 8f, calories = 15f, protein = 3f, carbs = 1.0f, fat = 0f, fiber = 0f, isGrowthProduct = true),

        // --- Ovos e Claras ---
        FoodItem(id = 19, name = "Ovo Frito no Azeite de Oliva", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 60f, calories = 115f, protein = 6.4f, carbs = 0.5f, fat = 9.8f, fiber = 0f),
        FoodItem(id = 20, name = "Ovo Frito na Manteiga", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 60f, calories = 120f, protein = 6.3f, carbs = 0.6f, fat = 10.3f, fiber = 0f),
        FoodItem(id = 21, name = "Ovo de Galinha Inteiro Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 50f, calories = 78f, protein = 6.3f, carbs = 0.6f, fat = 5.3f, fiber = 0f),
        FoodItem(id = 22, name = "Ovo Mexido Simples (1 un)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 55f, calories = 92f, protein = 6.3f, carbs = 0.8f, fat = 7.1f, fiber = 0f),
        FoodItem(id = 23, name = "Clara de Ovo Cozida / Pasteurizada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 11f, carbs = 0.7f, fat = 0.2f, fiber = 0f),
        FoodItem(id = 24, name = "Omelete de Claras com Aveia", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 150f, calories = 160f, protein = 18f, carbs = 12f, fat = 3.5f, fiber = 2f),

        // --- Carnes, Peixes e Aves ---
        FoodItem(id = 25, name = "Peito de Frango Grelhado / Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 165f, protein = 31.5f, carbs = 0f, fat = 3.6f, fiber = 0f),
        FoodItem(id = 26, name = "Peito de Frango Desfiado Cozido", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 160f, protein = 32f, carbs = 0f, fat = 3.2f, fiber = 0f),
        FoodItem(id = 27, name = "Sobrecoxa de Frango sem Pele Assada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 210f, protein = 25f, carbs = 0f, fat = 11.5f, fiber = 0f),
        FoodItem(id = 28, name = "Carne Moída Patinho Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 219f, protein = 35.9f, carbs = 0f, fat = 7.3f, fiber = 0f),
        FoodItem(id = 29, name = "Bife de Patinho Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 215f, protein = 36f, carbs = 0f, fat = 6.8f, fiber = 0f),
        FoodItem(id = 30, name = "Alcatra Bovina Grelhada", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 241f, protein = 31.9f, carbs = 0f, fat = 11.6f, fiber = 0f),
        FoodItem(id = 31, name = "Contrafilé Grelhado sem Gordura", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 205f, protein = 31.5f, carbs = 0f, fat = 8.0f, fiber = 0f),
        FoodItem(id = 32, name = "Filé Mignon Bovino Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 192f, protein = 32.8f, carbs = 0f, fat = 6.0f, fiber = 0f),
        FoodItem(id = 33, name = "Fígado Bovino Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 175f, protein = 29f, carbs = 3.8f, fat = 4.8f, fiber = 0f),
        FoodItem(id = 34, name = "Filé de Tilápia Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 128f, protein = 26f, carbs = 0f, fat = 2.7f, fiber = 0f),
        FoodItem(id = 35, name = "Filé de Salmão Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 208f, protein = 22f, carbs = 0f, fat = 13.0f, fiber = 0f),
        FoodItem(id = 36, name = "Atum em Pedaços ao Natural (Lata)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 116f, protein = 26f, carbs = 0f, fat = 1.0f, fiber = 0f),
        FoodItem(id = 37, name = "Sardinha em Conserva Escorrida", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 208f, protein = 24.6f, carbs = 0f, fat = 11.5f, fiber = 0f),
        FoodItem(id = 38, name = "Camarão Cozido / Grelhado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 99f, protein = 24f, carbs = 0.2f, fat = 0.3f, fiber = 0f),
        FoodItem(id = 39, name = "Lombo Suíno Assado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 210f, protein = 31f, carbs = 0f, fat = 8.8f, fiber = 0f),
        FoodItem(id = 40, name = "Peito de Peru Fatiado Defumado", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 40f, calories = 44f, protein = 8.5f, carbs = 0.8f, fat = 0.7f, fiber = 0f),
        FoodItem(id = 41, name = "Tofu Fresco Firme", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 100f, calories = 76f, protein = 8.2f, carbs = 1.9f, fat = 4.8f, fiber = 0.3f),
        FoodItem(id = 42, name = "Proteína de Soja Texturizada (PTS)", category = FoodCategory.PROTEIN, servingUnit = "g", baseServingAmount = 50f, calories = 145f, protein = 25f, carbs = 15f, fat = 0.5f, fiber = 9f),

        // --- Laticínios ---
        FoodItem(id = 43, name = "Queijo Cottage", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 98f, protein = 11.5f, carbs = 3.4f, fat = 4.3f, fiber = 0f),
        FoodItem(id = 44, name = "Queijo Minas Frescal", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 50f, calories = 132f, protein = 8.7f, carbs = 1.6f, fat = 10.1f, fiber = 0f),
        FoodItem(id = 45, name = "Ricota Fresca Tradicional", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 50f, calories = 70f, protein = 5.5f, carbs = 1.9f, fat = 4.4f, fiber = 0f),
        FoodItem(id = 46, name = "Queijo Muçarela Fatiado", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 30f, calories = 96f, protein = 6.6f, carbs = 0.9f, fat = 7.5f, fiber = 0f),
        FoodItem(id = 47, name = "Iogurte Natural Desnatado", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 170f, calories = 70f, protein = 8.5f, carbs = 9.0f, fat = 0.5f, fiber = 0f),
        FoodItem(id = 48, name = "Iogurte Natural Integral", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 170f, calories = 110f, protein = 6.8f, carbs = 9.2f, fat = 5.4f, fiber = 0f),
        FoodItem(id = 49, name = "Iogurte Grego Zero Açúcar", category = FoodCategory.DAIRY, servingUnit = "g", baseServingAmount = 100f, calories = 62f, protein = 7.2f, carbs = 5.0f, fat = 1.4f, fiber = 0f),
        FoodItem(id = 50, name = "Leite Desnatado", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 70f, protein = 6.4f, carbs = 10.0f, fat = 0.2f, fiber = 0f),
        FoodItem(id = 51, name = "Leite Semidesnatado", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 90f, protein = 6.2f, carbs = 9.6f, fat = 2.6f, fiber = 0f),
        FoodItem(id = 52, name = "Leite Integral", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 120f, protein = 6.0f, carbs = 9.6f, fat = 6.2f, fiber = 0f),
        FoodItem(id = 53, name = "Bebida Vegetal de Amêndoas", category = FoodCategory.DAIRY, servingUnit = "ml", baseServingAmount = 200f, calories = 38f, protein = 1.2f, carbs = 2.2f, fat = 2.8f, fiber = 0.8f),

        // --- Carboidratos e Tubérculos ---
        FoodItem(id = 54, name = "Arroz Branco Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 130f, protein = 2.7f, carbs = 28.2f, fat = 0.3f, fiber = 0.4f),
        FoodItem(id = 55, name = "Arroz Integral Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 2.6f, carbs = 25.8f, fat = 1.0f, fiber = 2.7f),
        FoodItem(id = 56, name = "Arroz Parboilizado Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 128f, protein = 2.7f, carbs = 27.5f, fat = 0.4f, fiber = 1.2f),
        FoodItem(id = 57, name = "Batata Doce Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 86f, protein = 1.6f, carbs = 20.1f, fat = 0.1f, fiber = 3.0f),
        FoodItem(id = 58, name = "Batata Doce Assada com Casca", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 90f, protein = 2.0f, carbs = 21.0f, fat = 0.2f, fiber = 3.3f),
        FoodItem(id = 59, name = "Batata Inglesa Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 87f, protein = 1.9f, carbs = 20.1f, fat = 0.1f, fiber = 1.8f),
        FoodItem(id = 60, name = "Purê de Batata Inglesa com Leite", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 106f, protein = 2.1f, carbs = 18.5f, fat = 2.5f, fiber = 1.5f),
        FoodItem(id = 61, name = "Mandioca / Aipim Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 125f, protein = 0.6f, carbs = 30.1f, fat = 0.3f, fiber = 1.6f),
        FoodItem(id = 62, name = "Mandioquinha / Batata Baroa Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 80f, protein = 0.9f, carbs = 18.9f, fat = 0.2f, fiber = 2.1f),
        FoodItem(id = 63, name = "Aveia em Flocos Finos", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 389f, protein = 16.9f, carbs = 66.3f, fat = 6.9f, fiber = 10.6f),
        FoodItem(id = 64, name = "Farelo de Aveia", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 30f, calories = 104f, protein = 5.2f, carbs = 17.5f, fat = 2.1f, fiber = 4.5f),
        FoodItem(id = 65, name = "Farinha de Aveia Integral", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 185f, protein = 7.5f, carbs = 32.5f, fat = 3.5f, fiber = 5.0f),
        FoodItem(id = 66, name = "Pão 100% Integral", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 122f, protein = 5.2f, carbs = 23f, fat = 1.4f, fiber = 3.8f),
        FoodItem(id = 67, name = "Pão Francês Tradicional (1 un)", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 150f, protein = 4.5f, carbs = 29.5f, fat = 1.5f, fiber = 1.1f),
        FoodItem(id = 68, name = "Pão Sírio Integral", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 50f, calories = 135f, protein = 5.0f, carbs = 26f, fat = 1.0f, fiber = 3.0f),
        FoodItem(id = 69, name = "Macarrão Tradicional Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 140f, protein = 4.8f, carbs = 28.5f, fat = 0.7f, fiber = 1.5f),
        FoodItem(id = 70, name = "Macarrão Integral Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 124f, protein = 5.3f, carbs = 25f, fat = 0.9f, fiber = 3.9f),
        FoodItem(id = 71, name = "Tapioca / Goma Hidratada", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 240f, protein = 0f, carbs = 60f, fat = 0f, fiber = 0.2f),
        FoodItem(id = 72, name = "Cuscuz de Milho Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 112f, protein = 2.2f, carbs = 25.1f, fat = 0.7f, fiber = 1.8f),
        FoodItem(id = 73, name = "Feijão Carioca Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 76f, protein = 4.8f, carbs = 13.6f, fat = 0.5f, fiber = 8.5f),
        FoodItem(id = 74, name = "Feijão Preto Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 77f, protein = 4.5f, carbs = 14.0f, fat = 0.5f, fiber = 8.4f),
        FoodItem(id = 75, name = "Lentilha Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 116f, protein = 9.0f, carbs = 20.1f, fat = 0.4f, fiber = 7.9f),
        FoodItem(id = 76, name = "Grão de Bico Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 164f, protein = 8.9f, carbs = 27.4f, fat = 2.6f, fiber = 7.6f),
        FoodItem(id = 77, name = "Quinoa em Grãos Cozida", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 120f, protein = 4.4f, carbs = 21.3f, fat = 1.9f, fiber = 2.8f),
        FoodItem(id = 78, name = "Milho Verde Cozido", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 100f, calories = 98f, protein = 3.2f, carbs = 19.5f, fat = 1.4f, fiber = 2.4f),
        FoodItem(id = 79, name = "Granola Integral sem Açúcar", category = FoodCategory.CARBOHYDRATE, servingUnit = "g", baseServingAmount = 40f, calories = 160f, protein = 4.8f, carbs = 25f, fat = 5.2f, fiber = 3.5f),

        // --- Gorduras Saudáveis e Oleaginosas ---
        FoodItem(id = 80, name = "Azeite de Oliva Extra Virgem", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 13f, calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f, fiber = 0f),
        FoodItem(id = 81, name = "Castanha do Pará (Brasil)", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 15f, calories = 98f, protein = 2.1f, carbs = 1.8f, fat = 10.0f, fiber = 1.1f),
        FoodItem(id = 82, name = "Castanha de Caju Torrada", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 20f, calories = 116f, protein = 3.6f, carbs = 6.0f, fat = 9.0f, fiber = 0.7f),
        FoodItem(id = 83, name = "Nozes Chilenas", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 15f, calories = 98f, protein = 2.2f, carbs = 2.0f, fat = 9.8f, fiber = 1.0f),
        FoodItem(id = 84, name = "Amêndoas Cruas / Torradas", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 20f, calories = 118f, protein = 4.2f, carbs = 4.3f, fat = 10.1f, fiber = 2.4f),
        FoodItem(id = 85, name = "Amendoim Torrado sem Sal", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 30f, calories = 175f, protein = 7.8f, carbs = 4.8f, fat = 14.8f, fiber = 2.5f),
        FoodItem(id = 86, name = "Abacate Fresco", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 100f, calories = 160f, protein = 2.0f, carbs = 8.5f, fat = 14.7f, fiber = 6.7f),
        FoodItem(id = 87, name = "Semente de Chia", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 15f, calories = 73f, protein = 2.5f, carbs = 6.3f, fat = 4.6f, fiber = 5.1f),
        FoodItem(id = 88, name = "Semente de Linhaça Dourada", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 15f, calories = 74f, protein = 2.8f, carbs = 4.3f, fat = 6.3f, fiber = 4.0f),
        FoodItem(id = 89, name = "Manteiga com Sal", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 10f, calories = 72f, protein = 0.1f, carbs = 0.1f, fat = 8.1f, fiber = 0f),
        FoodItem(id = 90, name = "Manteiga Ghee Clarificada", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 10f, calories = 88f, protein = 0f, carbs = 0f, fat = 9.9f, fiber = 0f),
        FoodItem(id = 91, name = "Óleo de Coco Extra Virgem", category = FoodCategory.FAT, servingUnit = "g", baseServingAmount = 10f, calories = 86f, protein = 0f, carbs = 0f, fat = 9.9f, fiber = 0f),

        // --- Frutas e Vegetais ---
        FoodItem(id = 92, name = "Banana Prata", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 89f, protein = 1.1f, carbs = 22.8f, fat = 0.3f, fiber = 2.6f),
        FoodItem(id = 93, name = "Banana Nanica", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 92f, protein = 1.4f, carbs = 23.8f, fat = 0.1f, fiber = 1.9f),
        FoodItem(id = 94, name = "Maçã Gala com Casca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 52f, protein = 0.3f, carbs = 13.8f, fat = 0.2f, fiber = 2.4f),
        FoodItem(id = 95, name = "Mamão Papaya", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 43f, protein = 0.5f, carbs = 10.8f, fat = 0.2f, fiber = 1.7f),
        FoodItem(id = 96, name = "Morango Fresco", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 32f, protein = 0.7f, carbs = 7.7f, fat = 0.3f, fiber = 2.0f),
        FoodItem(id = 97, name = "Melancia Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 30f, protein = 0.6f, carbs = 7.5f, fat = 0.1f, fiber = 0.4f),
        FoodItem(id = 98, name = "Abacaxi Fresco", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 50f, protein = 0.5f, carbs = 13.1f, fat = 0.1f, fiber = 1.4f),
        FoodItem(id = 99, name = "Laranja Pêra Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 46f, protein = 0.9f, carbs = 11.5f, fat = 0.1f, fiber = 2.0f),
        FoodItem(id = 100, name = "Pêra Williams Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 57f, protein = 0.4f, carbs = 15.2f, fat = 0.1f, fiber = 3.1f),
        FoodItem(id = 101, name = "Uva Niágara / Thompson", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 67f, protein = 0.6f, carbs = 17.5f, fat = 0.2f, fiber = 0.9f),
        FoodItem(id = 102, name = "Manga Palmer Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 60f, protein = 0.8f, carbs = 15.0f, fat = 0.4f, fiber = 1.6f),
        FoodItem(id = 103, name = "Kiwi Fresco", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 61f, protein = 1.1f, carbs = 14.7f, fat = 0.5f, fiber = 3.0f),
        FoodItem(id = 104, name = "Açaí Puro Polpa (sem xarope)", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 65f, protein = 1.3f, carbs = 6.2f, fat = 3.8f, fiber = 2.6f),
        FoodItem(id = 105, name = "Brócolis Cozido no Vapor", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f, fiber = 3.3f),
        FoodItem(id = 106, name = "Couve Manteiga Refogada", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 45f, protein = 2.9f, carbs = 5.6f, fat = 1.9f, fiber = 3.1f),
        FoodItem(id = 107, name = "Couve-flor Cozida", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 25f, protein = 1.9f, carbs = 5.0f, fat = 0.3f, fiber = 2.0f),
        FoodItem(id = 108, name = "Espinafre Cozido", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 23f, protein = 3.0f, carbs = 3.6f, fat = 0.4f, fiber = 2.2f),
        FoodItem(id = 109, name = "Alface Crespa Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 15f, protein = 1.4f, carbs = 2.8f, fat = 0.2f, fiber = 1.3f),
        FoodItem(id = 110, name = "Rúcula Fresca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 25f, protein = 2.6f, carbs = 3.7f, fat = 0.7f, fiber = 1.6f),
        FoodItem(id = 111, name = "Tomate Italiano Fresco", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 18f, protein = 0.9f, carbs = 3.9f, fat = 0.2f, fiber = 1.2f),
        FoodItem(id = 112, name = "Cenoura Ralada Crua", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 41f, protein = 0.9f, carbs = 9.6f, fat = 0.2f, fiber = 2.8f),
        FoodItem(id = 113, name = "Beterraba Cozida", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 44f, protein = 1.7f, carbs = 10.0f, fat = 0.2f, fiber = 2.8f),
        FoodItem(id = 114, name = "Pepino com Casca", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 15f, protein = 0.7f, carbs = 3.6f, fat = 0.1f, fiber = 0.5f),
        FoodItem(id = 115, name = "Abobrinha Italiana Cozida", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 17f, protein = 1.2f, carbs = 3.1f, fat = 0.3f, fiber = 1.0f),
        FoodItem(id = 116, name = "Berinjela Grelhada / Cozida", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 25f, protein = 1.0f, carbs = 5.9f, fat = 0.2f, fiber = 3.0f),
        FoodItem(id = 117, name = "Chuchu Cozido", category = FoodCategory.FRUITS_VEGETABLES, servingUnit = "g", baseServingAmount = 100f, calories = 19f, protein = 0.8f, carbs = 4.5f, fat = 0.1f, fiber = 1.7f)
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
        notes = "Foco em bater metas diárias de proteína e hidratação com fontes limpas."
    )

    // Dietas semanais dia a dia (Segunda a Domingo)
    val defaultWeeklyDietPlans = listOf(
        WeeklyDietPlan(id = 1, dietPlanId = 1, dayOfWeek = 1, dayName = "Segunda-feira", targetCalories = 2750, targetProteinGrams = 180, targetCarbsGrams = 340, targetFatGrams = 65, targetWaterMl = 3600, isTrainingDay = true, notes = "Dia de Peito & Tríceps - carbo alto"),
        WeeklyDietPlan(id = 2, dietPlanId = 1, dayOfWeek = 2, dayName = "Terça-feira", targetCalories = 2750, targetProteinGrams = 180, targetCarbsGrams = 340, targetFatGrams = 65, targetWaterMl = 3600, isTrainingDay = true, notes = "Dia de Costas & Bíceps - carbo alto"),
        WeeklyDietPlan(id = 3, dietPlanId = 1, dayOfWeek = 3, dayName = "Quarta-feira", targetCalories = 2800, targetProteinGrams = 185, targetCarbsGrams = 350, targetFatGrams = 65, targetWaterMl = 3800, isTrainingDay = true, notes = "Dia de Pernas Pesado - superávit máximo"),
        WeeklyDietPlan(id = 4, dietPlanId = 1, dayOfWeek = 4, dayName = "Quinta-feira", targetCalories = 2650, targetProteinGrams = 175, targetCarbsGrams = 320, targetFatGrams = 65, targetWaterMl = 3500, isTrainingDay = true, notes = "Dia de Ombros & Abdômen"),
        WeeklyDietPlan(id = 5, dietPlanId = 1, dayOfWeek = 5, dayName = "Sexta-feira", targetCalories = 2750, targetProteinGrams = 180, targetCarbsGrams = 340, targetFatGrams = 65, targetWaterMl = 3600, isTrainingDay = true, notes = "Dia de Braços & Isquiotibiais"),
        WeeklyDietPlan(id = 6, dietPlanId = 1, dayOfWeek = 6, dayName = "Sábado", targetCalories = 2450, targetProteinGrams = 175, targetCarbsGrams = 260, targetFatGrams = 70, targetWaterMl = 3200, isTrainingDay = false, notes = "Descanso ativo / cardio leve - carbo moderado"),
        WeeklyDietPlan(id = 7, dietPlanId = 1, dayOfWeek = 7, dayName = "Domingo", targetCalories = 2400, targetProteinGrams = 175, targetCarbsGrams = 250, targetFatGrams = 70, targetWaterMl = 3200, isTrainingDay = false, notes = "Descanso total e recuperação muscular")
    )

    val defaultMealPlan = listOf(
        MealPlanItem(id = 1, dietPlanId = 1, mealType = MealType.BREAKFAST, foodItemId = 9, foodName = "Ovo Frito no Azeite de Oliva", foodCategory = FoodCategory.PROTEIN, amount = 120f, unit = "g (2 ovos)", calories = 230f, protein = 12.8f, carbs = 1.0f, fat = 19.6f, notes = "Frito com 1 colher de chá de azeite"),
        MealPlanItem(id = 2, dietPlanId = 1, mealType = MealType.BREAKFAST, foodItemId = 36, foodName = "Aveia em Flocos Finos", foodCategory = FoodCategory.CARBOHYDRATE, amount = 60f, unit = "g", calories = 233.4f, protein = 10.1f, carbs = 39.8f, fat = 4.1f),
        MealPlanItem(id = 3, dietPlanId = 1, mealType = MealType.BREAKFAST, foodItemId = 50, foodName = "Banana Prata", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 120f, unit = "g", calories = 106.8f, protein = 1.3f, carbs = 27.4f, fat = 0.4f),

        MealPlanItem(id = 4, dietPlanId = 1, mealType = MealType.LUNCH, foodItemId = 15, foodName = "Peito de Frango Grelhado / Cozido", foodCategory = FoodCategory.PROTEIN, amount = 180f, unit = "g", calories = 297f, protein = 55.8f, carbs = 0f, fat = 6.5f),
        MealPlanItem(id = 5, dietPlanId = 1, mealType = MealType.LUNCH, foodItemId = 31, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 220f, unit = "g", calories = 286f, protein = 5.9f, carbs = 62.0f, fat = 0.7f),
        MealPlanItem(id = 6, dietPlanId = 1, mealType = MealType.LUNCH, foodItemId = 42, foodName = "Feijão Carioca Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 100f, unit = "g", calories = 76f, protein = 4.8f, carbs = 13.6f, fat = 0.5f),
        MealPlanItem(id = 7, dietPlanId = 1, mealType = MealType.LUNCH, foodItemId = 45, foodName = "Azeite de Oliva Extra Virgem", foodCategory = FoodCategory.FAT, amount = 12f, unit = "g", calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f),
        MealPlanItem(id = 8, dietPlanId = 1, mealType = MealType.LUNCH, foodItemId = 56, foodName = "Brócolis Cozido no Vapor", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 100f, unit = "g", calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f),

        MealPlanItem(id = 9, dietPlanId = 1, mealType = MealType.PRE_WORKOUT, foodItemId = 1, foodName = "100% Whey Protein Concentrado Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 30f, unit = "g", calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f),
        MealPlanItem(id = 10, dietPlanId = 1, mealType = MealType.PRE_WORKOUT, foodItemId = 3, foodName = "Creatina Monohidratada 100% Pura Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 5f, unit = "g", calories = 0f, protein = 0f, carbs = 0f, fat = 0f),
        MealPlanItem(id = 11, dietPlanId = 1, mealType = MealType.PRE_WORKOUT, foodItemId = 33, foodName = "Batata Doce Cozida", foodCategory = FoodCategory.CARBOHYDRATE, amount = 180f, unit = "g", calories = 154.8f, protein = 2.9f, carbs = 36.2f, fat = 0.2f),

        MealPlanItem(id = 12, dietPlanId = 1, mealType = MealType.DINNER, foodItemId = 17, foodName = "Carne Moída Patinho Grelhado", foodCategory = FoodCategory.PROTEIN, amount = 160f, unit = "g", calories = 350.4f, protein = 57.4f, carbs = 0f, fat = 11.7f),
        MealPlanItem(id = 13, dietPlanId = 1, mealType = MealType.DINNER, foodItemId = 31, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 200f, unit = "g", calories = 260f, protein = 5.4f, carbs = 56.4f, fat = 0.6f),

        MealPlanItem(id = 14, dietPlanId = 1, mealType = MealType.SUPPER, foodItemId = 4, foodName = "Pasta de Amendoim Integral Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 20f, unit = "g", calories = 117.3f, protein = 5.6f, carbs = 3.6f, fat = 9.9f)
    )

    // Exemplos de registros de alimentos consumidos (Diário offline)
    val defaultFoodRecords: List<FoodRecord> by lazy {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        listOf(
            FoodRecord(id = 1, dateString = today, mealType = MealType.BREAKFAST, foodItemId = 9, foodName = "Ovo Frito no Azeite de Oliva", foodCategory = FoodCategory.PROTEIN, amount = 120f, unit = "g", calories = 230f, protein = 12.8f, carbs = 1.0f, fat = 19.6f, notes = "Café da manhã reforçado"),
            FoodRecord(id = 2, dateString = today, mealType = MealType.BREAKFAST, foodItemId = 36, foodName = "Aveia em Flocos Finos", foodCategory = FoodCategory.CARBOHYDRATE, amount = 60f, unit = "g", calories = 233.4f, protein = 10.1f, carbs = 39.8f, fat = 4.1f),
            FoodRecord(id = 3, dateString = today, mealType = MealType.LUNCH, foodItemId = 15, foodName = "Peito de Frango Grelhado / Cozido", foodCategory = FoodCategory.PROTEIN, amount = 180f, unit = "g", calories = 297f, protein = 55.8f, carbs = 0f, fat = 6.5f),
            FoodRecord(id = 4, dateString = today, mealType = MealType.LUNCH, foodItemId = 31, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 220f, unit = "g", calories = 286f, protein = 5.9f, carbs = 62.0f, fat = 0.7f)
        )
    }

    // Planos de Treino (Training Programs)
    val defaultWorkoutPlans = listOf(
        WorkoutPlan(
            id = 1,
            name = "Plano Hipertrofia Clássico ABCDE",
            description = "Foco intenso em volume e hipertrofia por grupamento muscular isolado com recuperação otimizada.",
            splitType = WorkoutSplitType.ABCDE,
            goal = "Hipertrofia Muscular",
            durationWeeks = 12,
            daysPerWeek = 5,
            isActive = true,
            notes = "Progressão de cargas semanal e descanso estrito de 60-90 segundos."
        ),
        WorkoutPlan(
            id = 2,
            name = "Plano Push / Pull / Legs ABC",
            description = "Divisão clássica e altamente eficiente de 3 dias alternados ou frequência 2x/semana.",
            splitType = WorkoutSplitType.ABC,
            goal = "Força & Volume",
            durationWeeks = 8,
            daysPerWeek = 3,
            isActive = false,
            notes = "Excelente para conciliar trabalho e treinos de alta densidade."
        )
    )

    // Rotinas completas do Plano ABCDE
    val defaultRoutines = listOf(
        WorkoutRoutine(
            id = 1,
            splitType = WorkoutSplitType.ABCDE,
            letter = "A",
            title = "A — Peitoral, Deltoide Anterior e Tríceps",
            dayOfWeekLabel = "Segunda-feira",
            targetMuscleGroups = "Peito, Ombros (anterior), Tríceps",
            estimatedDurationMinutes = 60,
            orderIndex = 1
        ),
        WorkoutRoutine(
            id = 2,
            splitType = WorkoutSplitType.ABCDE,
            letter = "B",
            title = "B — Dorsais, Deltoide Posterior e Trapézio",
            dayOfWeekLabel = "Terça-feira",
            targetMuscleGroups = "Costas, Deltoides (posterior), Trapézio",
            estimatedDurationMinutes = 60,
            orderIndex = 2
        ),
        WorkoutRoutine(
            id = 3,
            splitType = WorkoutSplitType.ABCDE,
            letter = "C",
            title = "C — Quadríceps, Adutores e Panturrilhas",
            dayOfWeekLabel = "Quarta-feira",
            targetMuscleGroups = "Pernas (anterior), Panturrilhas",
            estimatedDurationMinutes = 65,
            orderIndex = 3
        ),
        WorkoutRoutine(
            id = 4,
            splitType = WorkoutSplitType.ABCDE,
            letter = "D",
            title = "D — Ombros Completo e Abdômen",
            dayOfWeekLabel = "Quinta-feira",
            targetMuscleGroups = "Deltoides (lateral/frontal), Core",
            estimatedDurationMinutes = 55,
            orderIndex = 4
        ),
        WorkoutRoutine(
            id = 5,
            splitType = WorkoutSplitType.ABCDE,
            letter = "E",
            title = "E — Braços (Bíceps, Tríceps) & Isquiotibiais",
            dayOfWeekLabel = "Sexta-feira",
            targetMuscleGroups = "Bíceps, Tríceps, Posteriores de Coxa",
            estimatedDurationMinutes = 60,
            orderIndex = 5
        )
    )

    // Exercícios completos com biomecânica, séries, repetições e cargas
    val defaultExercises = listOf(
        // Treino A: Peito, Deltoide Anterior e Tríceps
        WorkoutExercise(id = 1, routineId = 1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 60f, restSeconds = 90, orderIndex = 1, notes = "Escápulas retraídas, toque suave no esterno", biomechanicsGuide = "Pés firmes no chão, glúteo apoiado no banco e trajetória em leve arco."),
        WorkoutExercise(id = 2, routineId = 1, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 24f, restSeconds = 75, orderIndex = 2, notes = "Inclinação a 30 graus para focar na porção clavicular", biomechanicsGuide = "Alongue bem na descida sem hiperestender os ombros."),
        WorkoutExercise(id = 3, routineId = 1, exerciseName = "Crucifixo na Polia (Crossover)", muscleGroup = "Peitoral", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 20f, restSeconds = 60, orderIndex = 3, notes = "Pico de contração de 1 segundo no centro", biomechanicsGuide = "Cotovelos levemente flexionados durante todo o movimento."),
        WorkoutExercise(id = 4, routineId = 1, exerciseName = "Tríceps Pulley com Barra Reta", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 35f, restSeconds = 60, orderIndex = 4, notes = "Cotovelos colados ao tronco", biomechanicsGuide = "Extensão completa sem balanço lombar."),
        WorkoutExercise(id = 5, routineId = 1, exerciseName = "Tríceps Testa com Halteres", muscleGroup = "Tríceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 12f, restSeconds = 60, orderIndex = 5, notes = "Descida controlada em direção à testa/têmporas", biomechanicsGuide = "Mantenha os cotovelos apontados para o teto."),

        // Treino B: Costas, Deltoide Posterior e Trapézio
        WorkoutExercise(id = 6, routineId = 2, exerciseName = "Puxada Frontal na Polia", muscleGroup = "Costas / Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 55f, restSeconds = 90, orderIndex = 1, notes = "Puxe com os cotovelos, estufando o peito", biomechanicsGuide = "Depressão escapular no início da puxada."),
        WorkoutExercise(id = 7, routineId = 2, exerciseName = "Remada Curvada com Barra", muscleGroup = "Costas / Meio", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 50f, restSeconds = 90, orderIndex = 2, notes = "Tronco inclinado a 45 graus, coluna neutra", biomechanicsGuide = "Barra sobe em direção ao umbigo."),
        WorkoutExercise(id = 8, routineId = 2, exerciseName = "Remada Baixa no Triângulo", muscleGroup = "Dorsal", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 45f, restSeconds = 75, orderIndex = 3, notes = "Alongamento completo das dorsais na volta", biomechanicsGuide = "Não hiperestenda a coluna para trás."),
        WorkoutExercise(id = 9, routineId = 2, exerciseName = "Crucifixo Inverso no Peck Deck", muscleGroup = "Deltoide Posterior", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 30f, restSeconds = 60, orderIndex = 4, notes = "Foco total na cabeça posterior do ombro", biomechanicsGuide = "Braços paralelos ao solo."),
        WorkoutExercise(id = 10, routineId = 2, exerciseName = "Encolhimento de Ombros com Halteres", muscleGroup = "Trapézio", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 26f, restSeconds = 60, orderIndex = 5, notes = "Movimento estritamente vertical, sem girar os ombros", biomechanicsGuide = "Segure 1 segundo no topo."),

        // Treino C: Pernas (Quadríceps e Panturrilhas)
        WorkoutExercise(id = 11, routineId = 3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps / Glúteo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 120, orderIndex = 1, notes = "Profundidade abaixo de 90 graus", biomechanicsGuide = "Joelhos na linha das pontas dos pés, tronco firme."),
        WorkoutExercise(id = 12, routineId = 3, exerciseName = "Leg Press 45 Graus", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 160f, restSeconds = 90, orderIndex = 2, notes = "Não tire o quadril do encosto no ponto mais baixo", biomechanicsGuide = "Pés na largura dos ombros."),
        WorkoutExercise(id = 13, routineId = 3, exerciseName = "Cadeira Extensora", muscleGroup = "Quadríceps", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 45f, restSeconds = 60, orderIndex = 3, notes = "Pausa isométrica de 1 segundo no topo", biomechanicsGuide = "Eixo do joelho alinhado com o eixo da máquina."),
        WorkoutExercise(id = 14, routineId = 3, exerciseName = "Gêmeos em Pé (Panturrilhas)", muscleGroup = "Panturrilhas", sets = 4, repsMin = 15, repsMax = 20, currentWeightKg = 50f, restSeconds = 60, orderIndex = 4, notes = "Amplitude máxima: desça até alongar bem o tendão", biomechanicsGuide = "Suba na ponta dos pés estendendo ao máximo."),

        // Treino D: Ombros e Abdômen
        WorkoutExercise(id = 15, routineId = 4, exerciseName = "Desenvolvimento Militar com Halteres", muscleGroup = "Ombros", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 18f, restSeconds = 90, orderIndex = 1, notes = "Costas bem apoiadas no encosto", biomechanicsGuide = "Cotovelos levemente à frente do plano coronal."),
        WorkoutExercise(id = 16, routineId = 4, exerciseName = "Elevação Lateral com Halteres", muscleGroup = "Deltoide Lateral", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 10f, restSeconds = 60, orderIndex = 2, notes = "Cotovelos ligeiramente flexionados, polegar nivelado", biomechanicsGuide = "Não use impulso com o tronco."),
        WorkoutExercise(id = 17, routineId = 4, exerciseName = "Elevação Frontal na Polia", muscleGroup = "Deltoide Anterior", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 15f, restSeconds = 60, orderIndex = 3, notes = "Tensão contínua do cabo", biomechanicsGuide = "Suba até a linha dos olhos."),
        WorkoutExercise(id = 18, routineId = 4, exerciseName = "Abdominal Supra na Polia (Crunch)", muscleGroup = "Abdômen", sets = 4, repsMin = 15, repsMax = 20, currentWeightKg = 35f, restSeconds = 60, orderIndex = 4, notes = "Flexione a coluna lombar aproximando costelas da bacia", biomechanicsGuide = "Mantenha os braços fixos, movendo o tronco."),

        // Treino E: Bíceps, Tríceps & Isquiotibiais
        WorkoutExercise(id = 19, routineId = 5, exerciseName = "Stiff com Barra", muscleGroup = "Isquiotibiais / Glúteo", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 50f, restSeconds = 90, orderIndex = 1, notes = "Coluna ereta, quadril para trás, joelhos semiflexionados", biomechanicsGuide = "Sinta o alongamento intenso dos posteriores."),
        WorkoutExercise(id = 20, routineId = 5, exerciseName = "Mesa Flexora", muscleGroup = "Isquiotibiais", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 40f, restSeconds = 60, orderIndex = 2, notes = "Quadril colado no banco", biomechanicsGuide = "Flexão completa sem soltar o peso no final."),
        WorkoutExercise(id = 21, routineId = 5, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 25f, restSeconds = 60, orderIndex = 3, notes = "Punhos firmes e cotovelos fixos ao lado do corpo", biomechanicsGuide = "Controle total na descida excêntrica."),
        WorkoutExercise(id = 22, routineId = 5, exerciseName = "Rosca Martelo com Halteres", muscleGroup = "Braquiorradial / Bíceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 14f, restSeconds = 60, orderIndex = 4, notes = "Pegada neutra para fortalecer braquial e antebraço", biomechanicsGuide = "Movimento alternado ou simultâneo."),
        WorkoutExercise(id = 23, routineId = 5, exerciseName = "Tríceps Corda na Polia", muscleGroup = "Tríceps", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 25f, restSeconds = 60, orderIndex = 5, notes = "Abra a corda na parte inferior para contração máxima", biomechanicsGuide = "Extensão com rotação externa dos punhos.")
    )

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
