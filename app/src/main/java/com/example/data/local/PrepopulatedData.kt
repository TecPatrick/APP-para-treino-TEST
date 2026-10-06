package com.example.data.local

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
        FoodItem(id = 1, name = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 2, name = "100% Whey Protein Isolado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 110f, protein = 27f, carbs = 0.5f, fat = 0.3f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 3, name = "Creatina Monohidratada 100% Pura Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 5f, calories = 0f, protein = 0f, carbs = 0f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 4, name = "Pasta de Amendoim Integral Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 15f, calories = 88f, protein = 4.2f, carbs = 2.7f, fat = 7.4f, fiber = 1.2f, isGrowthProduct = true),
        FoodItem(id = 5, name = "Albumina 80% Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 30f, calories = 114f, protein = 24f, carbs = 1.5f, fat = 0f, fiber = 0f, isGrowthProduct = true),
        FoodItem(id = 6, name = "Hipercalórico 7000 Growth", category = FoodCategory.GROWTH_SUPPLEMENT, servingUnit = "g", baseServingAmount = 120f, calories = 460f, protein = 30f, carbs = 76f, fat = 3.5f, fiber = 2.0f, isGrowthProduct = true),

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

    val defaultMealPlan = listOf(
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 9, foodName = "Ovo de Galinha Inteiro Cozido", foodCategory = FoodCategory.PROTEIN, amount = 150f, unit = "g", calories = 234f, protein = 18.9f, carbs = 1.8f, fat = 15.9f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 16, foodName = "Aveia em Flocos Finos", foodCategory = FoodCategory.CARBOHYDRATE, amount = 60f, unit = "g", calories = 233.4f, protein = 10.1f, carbs = 39.8f, fat = 4.1f),
        MealPlanItem(mealType = MealType.BREAKFAST, foodItemId = 23, foodName = "Banana Prata", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 120f, unit = "g", calories = 106.8f, protein = 1.3f, carbs = 27.4f, fat = 0.4f),

        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 7, foodName = "Peito de Frango Grelhado / Cozido", foodCategory = FoodCategory.PROTEIN, amount = 180f, unit = "g", calories = 297f, protein = 55.8f, carbs = 0f, fat = 6.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 13, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 220f, unit = "g", calories = 286f, protein = 5.9f, carbs = 62.0f, fat = 0.7f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 20, foodName = "Azeite de Oliva Extra Virgem", foodCategory = FoodCategory.FAT, amount = 12f, unit = "g", calories = 119f, protein = 0f, carbs = 0f, fat = 13.5f),
        MealPlanItem(mealType = MealType.LUNCH, foodItemId = 25, foodName = "Brócolis Cozido no Vapor", foodCategory = FoodCategory.FRUITS_VEGETABLES, amount = 100f, unit = "g", calories = 35f, protein = 2.4f, carbs = 7.2f, fat = 0.4f),

        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 1, foodName = "100% Whey Protein Concentrado Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 30f, unit = "g", calories = 120f, protein = 24f, carbs = 4.0f, fat = 1.6f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 3, foodName = "Creatina Monohidratada 100% Pura Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 5f, unit = "g", calories = 0f, protein = 0f, carbs = 0f, fat = 0f),
        MealPlanItem(mealType = MealType.PRE_WORKOUT, foodItemId = 15, foodName = "Batata Doce Cozida", foodCategory = FoodCategory.CARBOHYDRATE, amount = 180f, unit = "g", calories = 154.8f, protein = 2.9f, carbs = 36.2f, fat = 0.2f),

        MealPlanItem(mealType = MealType.DINNER, foodItemId = 8, foodName = "Carne Moída Patinho Grelhado", foodCategory = FoodCategory.PROTEIN, amount = 160f, unit = "g", calories = 350.4f, protein = 57.4f, carbs = 0f, fat = 11.7f),
        MealPlanItem(mealType = MealType.DINNER, foodItemId = 13, foodName = "Arroz Branco Cozido", foodCategory = FoodCategory.CARBOHYDRATE, amount = 200f, unit = "g", calories = 260f, protein = 5.4f, carbs = 56.4f, fat = 0.6f),

        MealPlanItem(mealType = MealType.SUPPER, foodItemId = 4, foodName = "Pasta de Amendoim Integral Growth", foodCategory = FoodCategory.GROWTH_SUPPLEMENT, amount = 20f, unit = "g", calories = 117.3f, protein = 5.6f, carbs = 3.6f, fat = 9.9f)
    )

    // Complete Pre-populated Routines for AB, ABC, and ABCDE
    val defaultRoutines = listOf(
        // Divisão AB (Upper / Lower)
        WorkoutRoutine(id = 101, splitType = WorkoutSplitType.AB, letter = "A", title = "Upper (Superior Completo)", dayOfWeekLabel = "Segunda & Quinta", targetMuscleGroups = "Peito, Costas, Ombros, Braços", estimatedDurationMinutes = 65, orderIndex = 0),
        WorkoutRoutine(id = 102, splitType = WorkoutSplitType.AB, letter = "B", title = "Lower (Inferior & Core)", dayOfWeekLabel = "Terça & Sexta", targetMuscleGroups = "Quadríceps, Posterior, Panturrilha, Abdômen", estimatedDurationMinutes = 65, orderIndex = 1),

        // Divisão ABC (Push / Pull / Legs)
        WorkoutRoutine(id = 201, splitType = WorkoutSplitType.ABC, letter = "A", title = "Push (Peito, Deltoide Frontal e Tríceps)", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral, Ombros, Tríceps", estimatedDurationMinutes = 60, orderIndex = 0),
        WorkoutRoutine(id = 202, splitType = WorkoutSplitType.ABC, letter = "B", title = "Pull (Dorsais, Deltoide Posterior e Bíceps)", dayOfWeekLabel = "Terça-feira", targetMuscleGroups = "Costas, Deltoide Posterior, Bíceps, Trapézio", estimatedDurationMinutes = 60, orderIndex = 1),
        WorkoutRoutine(id = 203, splitType = WorkoutSplitType.ABC, letter = "C", title = "Legs (Pernas Completo & Abdômen)", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Quadríceps, Isquiotibiais, Panturrilhas, Core", estimatedDurationMinutes = 65, orderIndex = 2),

        // Divisão ABCDE (Hipertrofia Growth)
        WorkoutRoutine(id = 1, splitType = WorkoutSplitType.ABCDE, letter = "A", title = "Peitoral & Deltoide Anterior", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peito Superior e Médio, Deltoide Frontal", estimatedDurationMinutes = 65, orderIndex = 0),
        WorkoutRoutine(id = 2, splitType = WorkoutSplitType.ABCDE, letter = "B", title = "Dorsal, Deltoide Posterior e Trapézio", dayOfWeekLabel = "Terça-feira", targetMuscleGroups = "Latíssimo, Romboides, Trapézio, Deltoide Posterior", estimatedDurationMinutes = 65, orderIndex = 1),
        WorkoutRoutine(id = 3, splitType = WorkoutSplitType.ABCDE, letter = "C", title = "Quadríceps & Panturrilhas", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Quadríceps, Adutores, Panturrilhas", estimatedDurationMinutes = 70, orderIndex = 2),
        WorkoutRoutine(id = 4, splitType = WorkoutSplitType.ABCDE, letter = "D", title = "Ombros Completo & Abdômen", dayOfWeekLabel = "Quinta-feira", targetMuscleGroups = "Deltoide Lateral, Frontal e Posterior, Core", estimatedDurationMinutes = 55, orderIndex = 3),
        WorkoutRoutine(id = 5, splitType = WorkoutSplitType.ABCDE, letter = "E", title = "Bíceps, Tríceps & Antebraço", dayOfWeekLabel = "Sexta-feira", targetMuscleGroups = "Bíceps Braquial, Tríceps (3 cabeças), Braquiorradial", estimatedDurationMinutes = 60, orderIndex = 4),

        // Divisão Personalizada (Custom)
        WorkoutRoutine(id = 301, splitType = WorkoutSplitType.CUSTOM, letter = "P1", title = "Minha Ficha Personalizada 1", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peito, Ombros, Tríceps", estimatedDurationMinutes = 60, orderIndex = 0)
    )

    val defaultExercises = listOf(
        // Custom Routine 301
        WorkoutExercise(routineId = 301, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, orderIndex = 0, biomechanicsGuide = "Escápulas retraídas e pés firmes no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"),
        WorkoutExercise(routineId = 301, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Deltoide Anterior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 22f, restSeconds = 75, orderIndex = 1, biomechanicsGuide = "Empurre na vertical no plano escapular.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif"),
        WorkoutExercise(routineId = 301, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, orderIndex = 2, biomechanicsGuide = "Abra a corda no final da extensão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif"),
        // ABCDE - Routine 1 (Peito)
        WorkoutExercise(routineId = 1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, orderIndex = 0, notes = "Aquecimento progressivo. Última série até falha concêntrica.", biomechanicsGuide = "Pés firmes no chão, escápulas em retração e depressão, descida controlada tocando o esterno médio.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-bench-press/"),
        WorkoutExercise(routineId = 1, exerciseName = "Supino Inclinado com Halteres (30°)", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, orderIndex = 1, notes = "Alongamento completo no ponto zero.", biomechanicsGuide = "Banco regulado em 30 graus para focar a porção clavicular sem recrutar excessivamente o deltoide.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/incline-dumbbell-press/"),
        WorkoutExercise(routineId = 1, exerciseName = "Crucifixo no Cross / Crossover", muscleGroup = "Peitoral", sets = 3, repsMin = 10, repsMax = 15, currentWeightKg = 20f, restSeconds = 60, orderIndex = 2, notes = "Pico de contração de 1s.", biomechanicsGuide = "Leve flexão nos cotovelos, aproxime os punhos esmagando o peitoral no centro.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/cable-crossover/"),
        WorkoutExercise(routineId = 1, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Deltoide Anterior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 22f, restSeconds = 75, orderIndex = 3, notes = "Escápulas travadas.", biomechanicsGuide = "Empurre na vertical no plano escapular (levemente à frente do tronco).", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/dumbbell-shoulder-press/"),

        // ABCDE - Routine 2 (Costas)
        WorkoutExercise(routineId = 2, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, orderIndex = 0, notes = "Puxar em direção ao peitoral superior.", biomechanicsGuide = "Inicie puxando com as escápulas para baixo antes de dobrar os cotovelos.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/lat-pulldown/"),
        WorkoutExercise(routineId = 2, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsais / Miolo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 90, orderIndex = 1, notes = "Tronco a 45 graus, coluna alinhada.", biomechanicsGuide = "Mantenha o core rígido e puxe a barra rente às coxas em direção ao umbigo.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-bent-over-row/"),
        WorkoutExercise(routineId = 2, exerciseName = "Crucifixo Inverso no Voador", muscleGroup = "Deltoide Posterior", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 40f, restSeconds = 60, orderIndex = 2, notes = "Controle de movimento.", biomechanicsGuide = "Cotovelos na altura dos ombros, afaste os braços focando na parte de trás dos ombros.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/rear-delt-fly/"),

        // ABCDE - Routine 3 (Pernas)
        WorkoutExercise(routineId = 3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps & Glúteos", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, orderIndex = 0, notes = "Profundidade abaixo de 90°.", biomechanicsGuide = "Joelhos acompanham a linha dos pés, peito aberto, respiração diafragmática travada (Valsalva).", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-squat/"),
        WorkoutExercise(routineId = 3, exerciseName = "Leg Press 45°", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 240f, restSeconds = 90, orderIndex = 1, notes = "Pés largura dos ombros.", biomechanicsGuide = "Desça até formar ângulo de 90 graus no joelho sem descolar a lombar do encosto.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/leg-press/"),
        WorkoutExercise(routineId = 3, exerciseName = "Cadeira Extensora", muscleGroup = "Quadríceps", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 65f, restSeconds = 60, orderIndex = 2, notes = "Drop-set na última série.", biomechanicsGuide = "Extensão completa sem hiperextensão abrupta, 1s de contração no topo.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/leg-extension/"),

        // AB - Routine 101 (Upper)
        WorkoutExercise(routineId = 101, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 75f, restSeconds = 90, orderIndex = 0, biomechanicsGuide = "Escápulas travadas, pegada média firme.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-bench-press/"),
        WorkoutExercise(routineId = 101, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 65f, restSeconds = 90, orderIndex = 1, biomechanicsGuide = "Tronco inclinado, coluna reta.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-bent-over-row/"),
        WorkoutExercise(routineId = 101, exerciseName = "Desenvolvimento Militar com Halteres", muscleGroup = "Ombros", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 20f, restSeconds = 75, orderIndex = 2, biomechanicsGuide = "Subida vertical controlada.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/dumbbell-shoulder-press/"),
        WorkoutExercise(routineId = 101, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 28f, restSeconds = 60, orderIndex = 3, biomechanicsGuide = "Cotovelos fixos ao lado do corpo.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/ez-bar-curl/"),
        WorkoutExercise(routineId = 101, exerciseName = "Tríceps Testa com Halteres", muscleGroup = "Tríceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 12f, restSeconds = 60, orderIndex = 4, biomechanicsGuide = "Flexão dos cotovelos mantendo os braços estáveis.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/dumbbell-lying-triceps-extension/"),

        // AB - Routine 102 (Lower)
        WorkoutExercise(routineId = 102, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps & Glúteos", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 90f, restSeconds = 120, orderIndex = 0, biomechanicsGuide = "Descida controlada e explosão na subida.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-squat/"),
        WorkoutExercise(routineId = 102, exerciseName = "Stiff com Barra / RDL", muscleGroup = "Isquiotibiais", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 60f, restSeconds = 90, orderIndex = 1, biomechanicsGuide = "Empurre o quadril para trás com joelhos levemente destravados.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-romanian-deadlift/"),
        WorkoutExercise(routineId = 102, exerciseName = "Leg Press 45°", muscleGroup = "Pernas", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 200f, restSeconds = 90, orderIndex = 2, biomechanicsGuide = "Amplitude segura e pés paralelos.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/leg-press/"),
        WorkoutExercise(routineId = 102, exerciseName = "Panturrilha no Smith em Pé", muscleGroup = "Panturrilhas", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 70f, restSeconds = 60, orderIndex = 3, biomechanicsGuide = "Alongamento total na descida.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/standing-calf-raises/"),

        // ABC - Routine 201 (Push)
        WorkoutExercise(routineId = 201, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 26f, restSeconds = 90, orderIndex = 0, biomechanicsGuide = "Banco em 30 graus, focar na parte de cima do peito.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/incline-dumbbell-press/"),
        WorkoutExercise(routineId = 201, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, orderIndex = 1, biomechanicsGuide = "Pegada firme, pés no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/barbell-bench-press/"),
        WorkoutExercise(routineId = 201, exerciseName = "Elevação Lateral com Halteres", muscleGroup = "Deltoide Lateral", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 12f, restSeconds = 60, orderIndex = 2, biomechanicsGuide = "Eleve até a linha dos ombros com cotovelos levemente flexionados.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/dumbbell-lateral-raise/"),
        WorkoutExercise(routineId = 201, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, orderIndex = 3, biomechanicsGuide = "Abra a corda no final da extensão.", gifDemonstrationUrl = "https://fitnessprogramer.com/exercise/cable-tricep-pushdown/")
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
        // Month 1 (Agosto)
        PurchaseItemLog(receiptId = 1, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 19.50f, totalPricePaid = 117.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 18.90f, totalPricePaid = 75.60f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 28.50f, totalPricePaid = 28.50f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 1, foodName = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 100.00f, totalPricePaid = 100.00f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Growth Suplementos"),
        PurchaseItemLog(receiptId = 1, foodName = "Pasta de Amendoim Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 24.70f, totalPricePaid = 24.70f, dateMillis = System.currentTimeMillis() - (45 * 86400000L), monthYearKey = "2026-08", storeName = "Growth Suplementos"),

        // Month 2 (Setembro) - with realistic inflation changes
        PurchaseItemLog(receiptId = 2, foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6.0f, unit = "kg", unitPricePaid = 21.90f, totalPricePaid = 131.40f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4.0f, unit = "cartela", unitPricePaid = 21.50f, totalPricePaid = 86.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "Arroz Branco (5kg)", category = FoodCategory.CARBOHYDRATE, quantity = 1.0f, unit = "pct", unitPricePaid = 29.90f, totalPricePaid = 29.90f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Atacadão"),
        PurchaseItemLog(receiptId = 2, foodName = "100% Whey Protein Concentrado Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pct (1kg)", unitPricePaid = 105.00f, totalPricePaid = 105.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Growth Suplementos"),
        PurchaseItemLog(receiptId = 2, foodName = "Pasta de Amendoim Growth", category = FoodCategory.GROWTH_SUPPLEMENT, quantity = 1.0f, unit = "pote (1kg)", unitPricePaid = 26.00f, totalPricePaid = 26.00f, dateMillis = System.currentTimeMillis() - (15 * 86400000L), monthYearKey = "2026-09", storeName = "Growth Suplementos")
    )
}
