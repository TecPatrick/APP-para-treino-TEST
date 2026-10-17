package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.CardioLog
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.GpsRouteLog
import com.example.data.model.FoodRecord
import com.example.data.model.MealPlanItem
import com.example.data.model.ProgressLog
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutLog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Representa os dados consolidados da meta calórica sincronizados no Firestore.
 */
data class CaloricGoalData(
    val targetCalories: Int,
    val targetProteinGrams: Int,
    val targetCarbsGrams: Int,
    val targetFatGrams: Int,
    val targetWaterMl: Int,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

/**
 * Camada de dados e sincronização do Firebase Firestore com persistência offline garantida.
 * Utiliza o cache local persistente em disco (PersistentCacheSettings) para assegurar
 * que leitura e gravação funcionem mesmo sem conexão à internet.
 */
class FirestoreSyncManager(private val context: Context) {

    private val firestore: FirebaseFirestore by lazy {
        val databaseId = try {
            context.getString(R.string.firestore_database_id)
        } catch (_: Exception) {
            "(default)"
        }
        val fs = if (databaseId.isNotBlank() && databaseId != "(default)") {
            FirebaseFirestore.getInstance(databaseId)
        } else {
            FirebaseFirestore.getInstance()
        }

        try {
            // Habilita persistência offline permanente no disco
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            fs.firestoreSettings = settings
            Log.d("FirestoreSync", "Firestore inicializado com persistência offline ativa.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Configuração do Firestore já aplicada ou aviso: ${e.message}")
        }
        fs
    }

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    private fun resolveUserId(customUid: String? = null): String? {
        val authenticatedUid = currentUserId ?: return null
        return authenticatedUid.takeIf { customUid.isNullOrBlank() || customUid == it }
    }

    private suspend fun awaitCloudWrite(task: Task<Void>) {
        val confirmed = withTimeoutOrNull(5_000L) {
            task.await()
            true
        } ?: false
        if (!confirmed) throw PendingCloudWriteException()
    }

    // =========================================================================
    // 1. PERFIS DE USUÁRIO (User Profiles)
    // =========================================================================

    /**
     * Sincroniza o perfil do usuário para o Firestore.
     * Grava imediatamente no cache local persistente e enfileira para a nuvem.
     */
    suspend fun syncProfileToCloud(profile: UserProfile): Result<Unit> {
        val uid = resolveUserId(profile.firebaseUid)
            ?: return Result.failure(Exception("Usuário não autenticado ou sem UID válido"))

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
                "monthlyTargetWeightKg" to profile.monthlyTargetWeightKg.toDouble(),
                "monthlyTargetDurationWeeks" to profile.monthlyTargetDurationWeeks,
                "monthlyNotes" to profile.monthlyNotes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(userDoc.set(data, SetOptions.merge()))
            Log.d("FirestoreSync", "Perfil do usuário $uid salvo com sucesso (offline/online).")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao salvar perfil no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca o perfil do usuário no Firestore (utiliza cache offline se desconectado).
     */
    suspend fun fetchProfileFromCloud(customUid: String? = null): Result<UserProfile?> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val snapshot = firestore.collection("users").document(uid).get(Source.DEFAULT).await()
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            val data = snapshot.data ?: return Result.success(null)
            val profile = parseUserProfile(data, uid)
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao buscar perfil da nuvem: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa mudanças no perfil do usuário em tempo real, emitindo dados imediatamente
     * a partir do cache local mesmo offline.
     */
    fun observeProfileFromCloud(customUid: String? = null): Flow<UserProfile?> = callbackFlow {
        val uid = resolveUserId(customUid)
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSync", "Erro no listener do perfil: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val profile = parseUserProfile(snapshot.data ?: emptyMap(), uid)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    private fun parseUserProfile(data: Map<String, Any>, uid: String): UserProfile {
        return UserProfile(
            id = 1,
            name = data["name"] as? String ?: "",
            email = data["email"] as? String ?: "",
            firebaseUid = uid,
            gender = try { Gender.valueOf(data["gender"] as? String ?: "MASCULINO") } catch (_: Exception) { Gender.MASCULINO },
            age = (data["age"] as? Number)?.toInt() ?: 26,
            weightKg = (data["weightKg"] as? Number)?.toFloat() ?: 78.5f,
            heightCm = (data["heightCm"] as? Number)?.toFloat() ?: 178f,
            biotype = try { BodyType.valueOf(data["biotype"] as? String ?: "MESOMORPH") } catch (_: Exception) { BodyType.MESOMORPH },
            trainingDaysPerWeek = (data["trainingDaysPerWeek"] as? Number)?.toInt() ?: 5,
            activityLevel = try { ActivityLevel.valueOf(data["activityLevel"] as? String ?: "MODERATE") } catch (_: Exception) { ActivityLevel.MODERATE },
            goal = try { FitnessGoal.valueOf(data["goal"] as? String ?: "BULKING_CLEAN") } catch (_: Exception) { FitnessGoal.BULKING_CLEAN },
            targetCalories = (data["targetCalories"] as? Number)?.toInt() ?: 2650,
            targetProteinGrams = (data["targetProteinGrams"] as? Number)?.toInt() ?: 175,
            targetCarbsGrams = (data["targetCarbsGrams"] as? Number)?.toInt() ?: 320,
            targetFatGrams = (data["targetFatGrams"] as? Number)?.toInt() ?: 65,
            targetWaterMl = (data["targetWaterMl"] as? Number)?.toInt() ?: 3530,
            monthlyTargetWeightKg = (data["monthlyTargetWeightKg"] as? Number)?.toFloat() ?: 81.0f,
            monthlyTargetDurationWeeks = (data["monthlyTargetDurationWeeks"] as? Number)?.toInt() ?: 4,
            monthlyNotes = data["monthlyNotes"] as? String ?: "",
            isLoggedIn = true,
            hasCompletedOnboarding = true
        )
    }

    // =========================================================================
    // 2. METAS CALÓRICAS (Caloric Goals)
    // =========================================================================

    /**
     * Sincroniza e armazena as metas calóricas na coleção /users/{userId}/calorieGoals
     * e atualiza o documento raiz do usuário.
     */
    suspend fun syncCaloricGoalsToCloud(
        targetCalories: Int,
        targetProteinGrams: Int,
        targetCarbsGrams: Int,
        targetFatGrams: Int,
        targetWaterMl: Int,
        customUid: String? = null
    ): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val userRef = firestore.collection("users").document(uid)
            val goalRef = userRef.collection("calorieGoals").document("current")

            val goalData = mapOf(
                "userId" to uid,
                "targetCalories" to targetCalories,
                "targetProteinGrams" to targetProteinGrams,
                "targetCarbsGrams" to targetCarbsGrams,
                "targetFatGrams" to targetFatGrams,
                "targetWaterMl" to targetWaterMl,
                "updatedAt" to FieldValue.serverTimestamp()
            )


            // Atualiza resumo no documento do perfil
            val summaryData = mapOf(
                "targetCalories" to targetCalories,
                "targetProteinGrams" to targetProteinGrams,
                "targetCarbsGrams" to targetCarbsGrams,
                "targetFatGrams" to targetFatGrams,
                "targetWaterMl" to targetWaterMl,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            // Enqueue both documents atomically, even while offline.
            awaitCloudWrite(firestore.batch()
                .set(goalRef, goalData, SetOptions.merge())
                .set(userRef, summaryData, SetOptions.merge())
                .commit())

            Log.d("FirestoreSync", "Metas calóricas sincronizadas no Firestore (offline/online).")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao salvar metas calóricas: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca a meta calórica armazenada na nuvem / cache local.
     */
    suspend fun fetchCaloricGoalsFromCloud(customUid: String? = null): Result<CaloricGoalData?> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val doc = firestore.collection("users").document(uid)
                .collection("calorieGoals").document("current")
                .get(Source.DEFAULT).await()

            if (!doc.exists()) return Result.success(null)
            val d = doc.data ?: return Result.success(null)

            val result = CaloricGoalData(
                targetCalories = (d["targetCalories"] as? Number)?.toInt() ?: 2000,
                targetProteinGrams = (d["targetProteinGrams"] as? Number)?.toInt() ?: 150,
                targetCarbsGrams = (d["targetCarbsGrams"] as? Number)?.toInt() ?: 200,
                targetFatGrams = (d["targetFatGrams"] as? Number)?.toInt() ?: 50,
                targetWaterMl = (d["targetWaterMl"] as? Number)?.toInt() ?: 3000
            )
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao buscar meta calórica: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa a meta calórica em tempo real com suporte a leitura offline.
     */
    fun observeCaloricGoalsFromCloud(customUid: String? = null): Flow<CaloricGoalData?> = callbackFlow {
        val uid = resolveUserId(customUid)
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("users").document(uid)
            .collection("calorieGoals").document("current")
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSync", "Erro ao observar metas calóricas: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val d = snapshot.data ?: emptyMap()
                    val goal = CaloricGoalData(
                        targetCalories = (d["targetCalories"] as? Number)?.toInt() ?: 2000,
                        targetProteinGrams = (d["targetProteinGrams"] as? Number)?.toInt() ?: 150,
                        targetCarbsGrams = (d["targetCarbsGrams"] as? Number)?.toInt() ?: 200,
                        targetFatGrams = (d["targetFatGrams"] as? Number)?.toInt() ?: 50,
                        targetWaterMl = (d["targetWaterMl"] as? Number)?.toInt() ?: 3000
                    )
                    trySend(goal)
                } else {
                    trySend(null)
                }
            }

        awaitClose { reg.remove() }
    }

    // =========================================================================
    // 3. LOGS DE TREINO (Workout Logs & GPS Route Logs)
    // =========================================================================

    /**
     * Sincroniza um log de treino (WorkoutLog) no Firestore na coleção /users/{userId}/workoutLogs/{logId}.
     * Salva imediatamente no banco offline e sincroniza na nuvem.
     */
    suspend fun syncWorkoutLogToCloud(log: WorkoutLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val docId = if (log.id > 0) log.id.toString() else "workout_${log.dateMillis}"
            val docRef = firestore.collection("users").document(uid)
                .collection("workoutLogs").document(docId)

            val data = mapOf(
                "userId" to uid,
                "routineId" to log.routineId,
                "routineName" to log.routineName,
                "dateMillis" to log.dateMillis,
                "durationMinutes" to log.durationMinutes,
                "totalVolumeKg" to log.totalVolumeKg.toDouble(),
                "totalSetsCompleted" to log.totalSetsCompleted,
                "perceivedExertion" to log.perceivedExertion,
                "notes" to log.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            Log.d("FirestoreSync", "WorkoutLog $docId sincronizado com sucesso no Firestore.")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao salvar WorkoutLog no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Remove um log de treino do Firestore.
     */
    suspend fun deleteWorkoutLogFromCloud(logId: Long, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            awaitCloudWrite(firestore.collection("users").document(uid)
                .collection("workoutLogs").document(logId.toString()).delete())
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao excluir WorkoutLog do Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca todos os logs de treino armazenados na nuvem / cache local.
     */
    suspend fun fetchWorkoutLogsFromCloud(customUid: String? = null): Result<List<WorkoutLog>> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val querySnapshot = firestore.collection("users").document(uid)
                .collection("workoutLogs")
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .get(Source.DEFAULT).await()

            val logs = querySnapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                val idLong = doc.id.toLongOrNull() ?: 0L
                WorkoutLog(
                    id = idLong,
                    routineId = (d["routineId"] as? Number)?.toLong() ?: 0L,
                    routineName = d["routineName"] as? String ?: "Treino",
                    dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    durationMinutes = (d["durationMinutes"] as? Number)?.toInt() ?: 0,
                    totalVolumeKg = (d["totalVolumeKg"] as? Number)?.toFloat() ?: 0f,
                    totalSetsCompleted = (d["totalSetsCompleted"] as? Number)?.toInt() ?: 0,
                    perceivedExertion = (d["perceivedExertion"] as? Number)?.toInt() ?: 8,
                    notes = d["notes"] as? String ?: ""
                )
            }
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao buscar logs de treino: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Observa os logs de treino em tempo real com suporte total offline.
     */
    fun observeWorkoutLogsFromCloud(customUid: String? = null): Flow<List<WorkoutLog>> = callbackFlow {
        val uid = resolveUserId(customUid)
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg = firestore.collection("users").document(uid)
            .collection("workoutLogs")
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    Log.w("FirestoreSync", "Erro ao escutar workoutLogs: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { doc ->
                        val d = doc.data ?: return@mapNotNull null
                        val idLong = doc.id.toLongOrNull() ?: 0L
                        WorkoutLog(
                            id = idLong,
                            routineId = (d["routineId"] as? Number)?.toLong() ?: 0L,
                            routineName = d["routineName"] as? String ?: "Treino",
                            dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            durationMinutes = (d["durationMinutes"] as? Number)?.toInt() ?: 0,
                            totalVolumeKg = (d["totalVolumeKg"] as? Number)?.toFloat() ?: 0f,
                            totalSetsCompleted = (d["totalSetsCompleted"] as? Number)?.toInt() ?: 0,
                            perceivedExertion = (d["perceivedExertion"] as? Number)?.toInt() ?: 8,
                            notes = d["notes"] as? String ?: ""
                        )
                    }
                    trySend(logs)
                }
            }

        awaitClose { reg.remove() }
    }

    /**
     * Sincroniza logs de rota GPS (caminhada/corrida ao ar livre) para o Firestore.
     */
    suspend fun syncGpsRouteLogToCloud(log: GpsRouteLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val docId = if (log.id > 0) log.id.toString() else "gps_${log.dateMillis}"
            val docRef = firestore.collection("users").document(uid)
                .collection("gpsRouteLogs").document(docId)

            val data = mapOf(
                "userId" to uid,
                "routeName" to log.routeName,
                "activityType" to log.activityType,
                "dateMillis" to log.dateMillis,
                "dateDisplay" to log.dateDisplay,
                "durationSeconds" to log.durationSeconds,
                "distanceMeters" to log.distanceMeters.toDouble(),
                "stepsCount" to log.stepsCount,
                "caloriesBurned" to log.caloriesBurned.toDouble(),
                "averagePaceMinPerKm" to log.averagePaceMinPerKm.toDouble(),
                "routePointsJson" to log.routePointsJson,
                "notes" to log.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            Log.d("FirestoreSync", "GpsRouteLog $docId sincronizado no Firestore.")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao salvar GpsRouteLog no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun syncFoodRecordToCloud(record: FoodRecord, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = if (record.id > 0) record.id.toString() else "food_${record.dateMillis}"
            val data = mapOf(
                "userId" to uid, "dateString" to record.dateString, "dateMillis" to record.dateMillis,
                "mealType" to record.mealType.name, "foodItemId" to record.foodItemId,
                "foodName" to record.foodName, "amount" to record.amount.toDouble(), "unit" to record.unit,
                "calories" to record.calories.toDouble(), "protein" to record.protein.toDouble(),
                "carbs" to record.carbs.toDouble(), "fat" to record.fat.toDouble(),
                "fiber" to record.fiber.toDouble(), "notes" to record.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("foodRecords").document(docId).set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteFoodRecordFromCloud(recordId: Long, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            awaitCloudWrite(firestore.collection("users").document(uid).collection("foodRecords").document(recordId.toString()).delete())
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun syncCardioLogToCloud(log: CardioLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = if (log.id > 0) log.id.toString() else "cardio_${log.dateMillis}"
            val data = mapOf(
                "userId" to uid, "type" to log.type.name, "durationMinutes" to log.durationMinutes,
                "caloriesBurned" to log.caloriesBurned, "distanceKm" to (log.distanceKm?.toDouble() ?: 0.0),
                "dateMillis" to log.dateMillis, "notes" to log.notes,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("cardioLogs").document(docId).set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun syncDailyIntakeToCloud(intake: DailyIntakeHistory, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val data = mapOf(
                "userId" to uid, "dateString" to intake.dateString, "dateMillis" to intake.dateMillis,
                "totalCalories" to intake.totalCalories.toDouble(), "totalProtein" to intake.totalProtein.toDouble(),
                "totalCarbs" to intake.totalCarbs.toDouble(), "totalFat" to intake.totalFat.toDouble(),
                "targetCalories" to intake.targetCalories, "isAdhered" to intake.isAdhered,
                "waterDrankMl" to intake.waterDrankMl, "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("dailyIntake").document(intake.dateString).set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    // =========================================================================
    // 4. ITENS ADICIONAIS EXISTENTES (MealPlan, Shopping, Progress)
    // =========================================================================

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
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
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
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
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
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Erro ao sincronizar progresso: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // 5. SINCRONIZAÇÃO COMPLETA DE DADOS PENDENTES (Full Sync)
    // =========================================================================

    /**
     * Sincroniza em lote todos os dados locais para a camada do Firestore
     * (perfil, metas calóricas, logs de treino e progresso).
     */
    suspend fun syncAllData(
        profile: UserProfile?,
        workoutLogs: List<WorkoutLog>,
        progressLogs: List<ProgressLog> = emptyList(),
        gpsLogs: List<GpsRouteLog> = emptyList(),
        foodRecords: List<FoodRecord> = emptyList(),
        cardioLogs: List<CardioLog> = emptyList(),
        dailyIntakes: List<DailyIntakeHistory> = emptyList()
    ): Result<SyncReport> {
        val uid = resolveUserId(profile?.firebaseUid)
            ?: return Result.failure(IllegalStateException("Entre na sua conta para sincronizar. Os dados locais continuam no aparelho."))
        val operations = mutableListOf<suspend () -> Result<Unit>>()
        if (profile != null) {
            operations.add { syncProfileToCloud(profile) }
            operations.add {
                syncCaloricGoalsToCloud(profile.targetCalories, profile.targetProteinGrams,
                    profile.targetCarbsGrams, profile.targetFatGrams, profile.targetWaterMl, uid)
            }
        }
        workoutLogs.forEach { log -> operations.add { syncWorkoutLogToCloud(log, uid) } }
        progressLogs.forEach { log -> operations.add { syncProgressLogToCloud(log) } }
        gpsLogs.forEach { log -> operations.add { syncGpsRouteLogToCloud(log, uid) } }
        foodRecords.forEach { log -> operations.add { syncFoodRecordToCloud(log, uid) } }
        cardioLogs.forEach { log -> operations.add { syncCardioLogToCloud(log, uid) } }
        dailyIntakes.forEach { log -> operations.add { syncDailyIntakeToCloud(log, uid) } }
        return Result.success(collectSyncResults(operations))
    }
}
