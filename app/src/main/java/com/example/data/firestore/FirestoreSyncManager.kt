package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.MealPlanItem
import com.example.data.model.ProgressLog
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        val databaseId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(databaseId)
    }

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun syncProfileToCloud(profile: UserProfile): Result<Unit> {
        val uid = currentUserId ?: profile.firebaseUid.takeIf { it.isNotBlank() } ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val userDoc = firestore.collection("users").document(uid)
            val data = mapOf(
                "userId" to uid,
                "name" to profile.name,
                "email" to profile.email,
                "gender" to profile.gender.name,
                "age" to profile.age,
                "weightKg" to profile.weightKg.toDouble(),
                "heightCm" to profile.heightCm.toDouble(),
                "biotype" to profile.biotype.name,
                "trainingDaysPerWeek" to profile.trainingDaysPerWeek,
                "activityLevel" to profile.activityLevel.name,
                "goal" to profile.goal.name,
                "targetCalories" to profile.targetCalories,
                "targetProteinGrams" to profile.targetProteinGrams,
                "targetCarbsGrams" to profile.targetCarbsGrams,
                "targetFatGrams" to profile.targetFatGrams,
                "targetWaterMl" to profile.targetWaterMl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            userDoc.set(data, SetOptions.merge()).await()
            Log.d("FirestoreSync", "Perfil do usuário $uid sincronizado com sucesso na nuvem.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao sincronizar perfil: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncMealPlanToCloud(mealPlan: MealPlanItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("mealPlans").document(mealPlan.id.toString())
            val data = mapOf(
                "userId" to uid,
                "mealType" to mealPlan.mealType.name,
                "name" to mealPlan.foodName,
                "portionGrams" to mealPlan.amount.toDouble(),
                "calories" to mealPlan.calories.toDouble(),
                "protein" to mealPlan.protein.toDouble(),
                "carbs" to mealPlan.carbs.toDouble(),
                "fat" to mealPlan.fat.toDouble(),
                "isCompletedToday" to mealPlan.isCompletedToday,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            doc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao sincronizar refeição: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncShoppingItemToCloud(item: ShoppingItem): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("shoppingItems").document(item.id.toString())
            val data = mapOf(
                "userId" to uid,
                "name" to item.name,
                "category" to item.category.name,
                "totalQuantityGrams" to item.quantity.toDouble(),
                "unit" to item.unit,
                "estimatedPrice" to item.estimatedPrice.toDouble(),
                "isBought" to item.isChecked,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            doc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao sincronizar item de compra: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncProgressLogToCloud(log: ProgressLog): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("progressLogs").document(log.id.toString())
            val data = mapOf(
                "userId" to uid,
                "dateMillis" to log.dateMillis,
                "weightKg" to log.weightKg.toDouble(),
                "bodyFatPercent" to log.bodyFatPercentage.toDouble(),
                "notes" to log.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            doc.set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao sincronizar progresso: ${e.message}", e)
            Result.failure(e)
        }
    }
}
