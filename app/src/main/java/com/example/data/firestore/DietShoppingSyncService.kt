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

                val (displayQty, unit, estimatedPrice) = when {
                    foodName.contains("Whey", ignoreCase = true) -> {
                        val pots = ceil((totalPeriodGrams / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                        Triple(pots, "pct (1kg)", pots * 105f)
                    }
                    foodName.contains("Creatina", ignoreCase = true) -> {
                        val pots = ceil((totalPeriodGrams / 250f).toDouble()).coerceAtLeast(1.0).toFloat()
                        Triple(pots, "pote (250g)", pots * 89f)
                    }
                    foodName.contains("Pasta de Amendoim", ignoreCase = true) -> {
                        val pots = ceil((totalPeriodGrams / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                        Triple(pots, "pote (1kg)", pots * 29f)
                    }
                    foodName.contains("Ovo", ignoreCase = true) -> {
                        val dozens = ceil((totalPeriodGrams / 600f).toDouble()).coerceAtLeast(1.0).toFloat()
                        Triple(dozens, "cartelas (30un)", dozens * 22f)
                    }
                    category == FoodCategory.FRUITS_VEGETABLES || category == FoodCategory.PROTEIN -> {
                        val kg = (totalPeriodGrams / 1000f).coerceAtLeast(0.5f)
                        Triple(kg, "kg", kg * (if (category == FoodCategory.PROTEIN) 35f else 10f))
                    }
                    else -> {
                        val kg = (totalPeriodGrams / 1000f).coerceAtLeast(0.5f)
                        Triple(kg, "kg / un", kg * 12f)
                    }
                }

                val shoppingItemDoc = shoppingCollection.document()
                val shoppingItemMap = mapOf(
                    "id" to shoppingItemDoc.id.hashCode().toLong(),
                    "name" to foodName,
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
