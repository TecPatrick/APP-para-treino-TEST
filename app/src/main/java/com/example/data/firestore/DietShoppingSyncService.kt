package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.FoodCategory
import com.example.data.model.MealPlanItem
import com.example.data.model.ShoppingItem
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlin.math.ceil

/**
 * Serviço responsável por processar a dieta semanal e mensal do usuário salva no Firestore
 * e gerar automaticamente uma lista de compras inteligente organizada por categorias
 * (Proteínas, Carboidratos, Frutas & Vegetais, Gorduras, Suplementos Growth, Laticínios).
 */
class DietShoppingSyncService(private val context: Context) {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Processa os itens de dieta (meal plans) salvos no Firestore para o usuário [userId],
     * agrupa por categoria e período (ex: 7 dias para semana ou 30 dias para o mês),
     * e gera/sincroniza a lista de compras organizada no Firestore.
     */
    suspend fun generateAndSyncShoppingListFromCloudDiet(
        userId: String,
        periodDays: Int = 30
    ): Result<List<ShoppingItem>> {
        if (userId.isBlank()) {
            return Result.failure(Exception("UID de usuário inválido para sincronizar dieta e compras."))
        }

        return try {
            val dietSnapshot = firestore.collection("users")
                .document(userId)
                .collection("mealPlans")
                .get()
                .await()

            val mealPlans = mutableListOf<MealPlanItem>()
            for (doc in dietSnapshot.documents) {
                val item = doc.toObject(MealPlanItem::class.java)
                if (item != null) {
                    mealPlans.add(item)
                }
            }

            if (mealPlans.isEmpty()) {
                if (BuildConfig.DEBUG) Log.d("DietShoppingSync", "Nenhum plano de refeição encontrado no Firestore para o usuário $userId.")
                return Result.success(emptyList())
            }

            // Agrega o consumo diário por nome de alimento e categoria
            val aggregatedDaily = mutableMapOf<String, Pair<FoodCategory, Float>>()
            for (meal in mealPlans) {
                val key = meal.foodName.trim()
                val existing = aggregatedDaily[key]
                val currentAmount = existing?.second ?: 0f
                aggregatedDaily[key] = Pair(meal.foodCategory, currentAmount + meal.amount)
            }

            val shoppingItems = mutableListOf<ShoppingItem>()
            val shoppingCollection = firestore.collection("users")
                .document(userId)
                .collection("shoppingList")

            val batch = firestore.batch()

            aggregatedDaily.forEach { (foodName, categoryAndAmount) ->
                val (category, dailyGrams) = categoryAndAmount
                val totalPeriodGrams = dailyGrams * periodDays

                // 💡 Smart Conversion: Convert tracked 'Cooked/Grilled' weights to 'Raw' shopping quantities
                val rawConversionFactor = when {
                    foodName.contains("Arroz", true) && foodName.contains("Cozido", true) -> 0.4f
                    foodName.contains("Feijão", true) && foodName.contains("Cozido", true) -> 0.5f
                    foodName.contains("Frango", true) && (foodName.contains("Grelhado", true) || foodName.contains("Cozido", true)) -> 1.35f
                    foodName.contains("Carne", true) && (foodName.contains("Grelhado", true) || foodName.contains("Assado", true)) -> 1.4f
                    foodName.contains("Patinho", true) && (foodName.contains("Grelhado", true) || foodName.contains("Moído", true)) -> 1.4f
                    foodName.contains("Tilápia", true) || foodName.contains("Peixe", true) -> 1.25f
                    else -> 1.0f
                }

                val adjustedTotalGrams = totalPeriodGrams * rawConversionFactor
                val shoppingName = foodName
                    .replace("Cozido", "", ignoreCase = true)
                    .replace("Grelhado", "", ignoreCase = true)
                    .replace("Assado", "", ignoreCase = true)
                    .replace("Grelhada", "", ignoreCase = true)
                    .replace("Cozida", "", ignoreCase = true)
                    .replace("Moído", "", ignoreCase = true)
                    .replace("Moída", "", ignoreCase = true)
                    .trim()

                val (displayQty, unit) = when {
                    foodName.contains("Whey", ignoreCase = true) -> {
                        val pots = ceil((adjustedTotalGrams / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                        pots to "pct (1kg)"
                    }
                    foodName.contains("Creatina", ignoreCase = true) -> {
                        val pots = ceil((adjustedTotalGrams / 250f).toDouble()).coerceAtLeast(1.0).toFloat()
                        pots to "pote (250g)"
                    }
                    foodName.contains("Pasta de Amendoim", ignoreCase = true) -> {
                        val pots = ceil((adjustedTotalGrams / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                        pots to "pote (1kg)"
                    }
                    foodName.contains("Ovo", ignoreCase = true) -> {
                        val cartelas = ceil((adjustedTotalGrams / 1500f).toDouble()).coerceAtLeast(1.0).toFloat()
                        cartelas to "cartelas (30un)"
                    }
                    category == FoodCategory.FRUITS_VEGETABLES || category == FoodCategory.PROTEIN -> {
                        val kg = (adjustedTotalGrams / 1000f).coerceAtLeast(0.5f)
                        kg to "kg"
                    }
                    category == FoodCategory.CARBOHYDRATE -> {
                        val kg = (adjustedTotalGrams / 1000f).coerceAtLeast(0.5f)
                        kg to "kg"
                    }
                    else -> {
                        val kg = (adjustedTotalGrams / 1000f).coerceAtLeast(0.5f)
                        kg to "kg / un"
                    }
                }

                val pricePerKgOrUnit = when {
                    foodName.contains("Whey", ignoreCase = true) -> 105.0f
                    foodName.contains("Creatina", ignoreCase = true) -> 89.0f
                    foodName.contains("Pasta de Amendoim", ignoreCase = true) -> 29.0f
                    foodName.contains("Ovo", ignoreCase = true) -> 21.0f
                    else -> when (category) {
                        FoodCategory.PROTEIN -> 28.0f
                        FoodCategory.CARBOHYDRATE -> 6.5f
                        FoodCategory.FAT -> 45.0f
                        FoodCategory.FRUITS_VEGETABLES -> 9.5f
                        FoodCategory.DAIRY -> 7.5f
                        else -> 12.0f
                    }
                }

                val estimatedPrice = if (unit == "g" || unit == "ml") {
                    (displayQty / 1000f) * pricePerKgOrUnit
                } else {
                    displayQty * pricePerKgOrUnit
                }

                val shoppingItemDoc = shoppingCollection.document()
                val shoppingItemMap = mapOf(
                    "id" to shoppingItemDoc.id.hashCode().toLong(),
                    "name" to shoppingName,
                    "category" to category.name,
                    "quantity" to displayQty,
                    "unit" to unit,
                    "periodDays" to periodDays,
                    "isChecked" to false,
                    "estimatedPrice" to estimatedPrice,
                    "myEstimatedPrice" to estimatedPrice,
                    "isAutoGenerated" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                batch.set(shoppingItemDoc, shoppingItemMap, SetOptions.merge())

                shoppingItems.add(
                    ShoppingItem(
                        id = shoppingItemDoc.id.hashCode().toLong(),
                        name = foodName,
                        category = category,
                        quantity = displayQty,
                        unit = unit,
                        periodDays = periodDays,
                        estimatedPrice = estimatedPrice,
                        isAutoGenerated = true
                    )
                )
            }

            batch.commit().await()
            if (BuildConfig.DEBUG) Log.d("DietShoppingSync", "Lista de compras gerada e sincronizada com sucesso para ${shoppingItems.size} itens.")
            Result.success(shoppingItems)
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) Log.e("DietShoppingSync", "Erro ao processar dieta e gerar lista de compras: ${e.message}", e)
            Result.failure(e)
        }
    }
}
