package com.example.data.firestore

import com.example.util.AppLog
import com.example.util.FirestoreLogger
import android.content.Context
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
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutExercise
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CopyOnWriteArraySet

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
 * Envelope de perfil remoto acompanhado do seu carimbo de tempo para resolução Last-Write-Wins (LWW).
 */
data class RemoteProfileSnapshot(
    val profile: UserProfile,
    val updatedAtMillis: Long
)

/**
 * Camada de dados e sincronização Delta (Diffing) do Firebase Firestore com persistência offline garantida.
 * - Perfil e Metas: envia apenas os campos alterados (Field-Level Diffing).
 * - Históricos: calcula hash SHA-256 determinístico por registro e envia apenas novos/modificados.
 * - Conflitos: aplica regra "Mais recente vence (Timestamp)" (Last-Write-Wins).
 */
open class FirestoreSyncManager(private val context: Context) {

    companion object {
        const val MAX_BATCH_OPERATIONS = 450
        const val DEFAULT_LOG_WINDOW_DAYS = 90L
        const val DEFAULT_LOG_PAGE_LIMIT = 100L
        private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }

    val deltaLedger: DeltaSyncLedger = DeltaSyncLedger(context)

    private val _lastSyncReport = MutableStateFlow<SyncReport?>(null)
    val lastSyncReport: StateFlow<SyncReport?> = _lastSyncReport.asStateFlow()

    private val migratedUsersInSession = mutableSetOf<String>()
    private val activeListeners = CopyOnWriteArraySet<ListenerRegistration>()

    private fun trackListener(registration: ListenerRegistration): ListenerRegistration {
        activeListeners.add(registration)
        return registration
    }

    private fun untrackAndRemoveListener(registration: ListenerRegistration) {
        activeListeners.remove(registration)
        runCatching { registration.remove() }
    }

    /**
     * Removes all active Firestore snapshot listeners immediately (called on logout and onCleared).
     */
    open fun removeAllListeners() {
        val listeners = activeListeners ?: return
        val snapshot = listeners.toList()
        listeners.clear()
        snapshot.forEach { reg ->
            runCatching { reg.remove() }
        }
    }

    private val firestore: FirebaseFirestore by lazy {
        val databaseId = context.applicationContext.getString(R.string.firestore_database_id)
        val fs = FirebaseFirestore.getInstance(databaseId)

        try {
            // Habilita persistência offline permanente no disco
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build()
            fs.firestoreSettings = settings
            AppLog.d("FirestoreSync", "Firestore inicializado com persistência offline ativa.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.w("FirestoreSync", "Configuração do Firestore já aplicada ou aviso: ${e.message}")
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

    fun recordLocalProfileMutation(uid: String, timestampMillis: Long = System.currentTimeMillis()) {
        if (uid.isNotBlank() && !uid.startsWith("guest_")) {
            deltaLedger.recordLocalProfileMutation(uid, timestampMillis)
        }
    }

    /**
     * Reconcilia o perfil remoto com o local aplicando a regra "Mais recente vence (Timestamp)".
     */
    fun reconcileRemoteProfileLWW(
        localProfile: UserProfile?,
        remoteProfile: UserProfile,
        remoteUpdatedAtMillis: Long,
        uid: String
    ): ProfileConflictResolution {
        val localModifiedAt = deltaLedger.getLocalProfileModifiedAt(uid)
        val resolution = FirestoreDeltaEngine.resolveProfileConflictLWW(
            localProfile = localProfile,
            localUpdatedAtMillis = localModifiedAt,
            remoteProfile = remoteProfile,
            remoteUpdatedAtMillis = remoteUpdatedAtMillis,
            uid = uid
        )
        if (resolution.decision == ConflictDecision.APPLY_REMOTE_DELTA) {
            val canonical = FirestoreDeltaEngine.profileToCanonicalMap(resolution.mergedProfile, uid)
            val effectiveTs = if (remoteUpdatedAtMillis > 0L) remoteUpdatedAtMillis else System.currentTimeMillis()
            deltaLedger.markProfileSynced(uid, canonical, effectiveTs)
        }
        return resolution
    }

    // =========================================================================
    // 1. PERFIS DE USUÁRIO (User Profiles - Field-Level Diffing)
    // =========================================================================

    /**
     * Sincroniza o perfil do usuário para o Firestore enviando apenas os campos alterados (Delta Sync).
     */
    suspend fun syncProfileToCloud(profile: UserProfile): Result<Unit> {
        val uid = resolveUserId(profile.firebaseUid)
            ?: return Result.failure(Exception("Usuário não autenticado ou sem UID válido"))

        return try {
            val canonicalMap = FirestoreDeltaEngine.profileToCanonicalMap(profile, uid)
            val previousMap = deltaLedger.getLastProfileMap(uid)
            val delta = FirestoreDeltaEngine.computeFieldDiff(previousMap, canonicalMap)

            if (!delta.isChanged) {
                AppLog.d("FirestoreSync", "Delta Sync: Perfil do usuário ${AppLog.maskUid(uid)} sem alterações (ignorado).")
                _lastSyncReport.value = SyncReport(
                    successful = 0,
                    skipped = 1,
                    updatedFieldsCount = 0
                )
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val userDoc = firestore.collection("users").document(uid)
            val deltaPayload = LinkedHashMap<String, Any>(delta.changedFields).apply {
                put("userId", uid)
                put("updatedAtMillis", nowMillis)
                put("updatedAt", FieldValue.serverTimestamp())
            }

            awaitCloudWrite(userDoc.set(deltaPayload, SetOptions.merge()))
            deltaLedger.markProfileSynced(uid, canonicalMap, nowMillis)
            _lastSyncReport.value = SyncReport(
                successful = 1,
                skipped = 0,
                updatedFieldsCount = delta.changedFieldNames.size
            )
            AppLog.d("FirestoreSync", "Delta Sync: Perfil ${AppLog.maskUid(uid)} enviou ${delta.changedFieldNames.size} campo(s): ${delta.changedFieldNames}")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao salvar perfil no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca o perfil do usuário no Firestore com timestamp para resolução de conflito.
     */
    suspend fun fetchProfileSnapshotFromCloud(customUid: String? = null): Result<RemoteProfileSnapshot?> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val snapshot = firestore.collection("users").document(uid).get(Source.DEFAULT).await()
            if (!snapshot.exists()) {
                return Result.success(null)
            }
            val data = snapshot.data ?: return Result.success(null)
            val profile = parseUserProfile(data, uid)
            val remoteTs = extractTimestampMillis(data)
            Result.success(RemoteProfileSnapshot(profile = profile, updatedAtMillis = remoteTs))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao buscar perfil da nuvem: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Busca o perfil do usuário no Firestore (utiliza cache offline se desconectado).
     */
    suspend fun fetchProfileFromCloud(customUid: String? = null): Result<UserProfile?> {
        return fetchProfileSnapshotFromCloud(customUid).map { it?.profile }
    }

    /**
     * Observa mudanças no perfil do usuário em tempo real acompanhadas do timestamp remoto.
     */
    fun observeProfileSnapshotFromCloud(customUid: String? = null): Flow<RemoteProfileSnapshot?> = callbackFlow {
        val uid = resolveUserId(customUid)
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = trackListener(
            firestore.collection("users").document(uid)
                .addSnapshotListener(MetadataChanges.EXCLUDE) { snapshot, error ->
                    if (error != null) {
                        AppLog.w("FirestoreSync", "Erro no listener do perfil: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data ?: emptyMap()
                        val profile = parseUserProfile(data, uid)
                        val remoteTs = extractTimestampMillis(data)
                        trySend(RemoteProfileSnapshot(profile = profile, updatedAtMillis = remoteTs))
                    } else {
                        trySend(null)
                    }
                }
        )

        awaitClose { untrackAndRemoveListener(registration) }
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

        val registration = trackListener(
            firestore.collection("users").document(uid)
                .addSnapshotListener(MetadataChanges.EXCLUDE) { snapshot, error ->
                    if (error != null) {
                        AppLog.w("FirestoreSync", "Erro no listener do perfil: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val profile = parseUserProfile(snapshot.data ?: emptyMap(), uid)
                        trySend(profile)
                    } else {
                        trySend(null)
                    }
                }
        )

        awaitClose { untrackAndRemoveListener(registration) }
    }

    private fun extractTimestampMillis(data: Map<String, Any>): Long {
        val directMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: 0L
        if (directMillis > 0L) return directMillis
        val ts = data["updatedAt"] as? Timestamp
        return ts?.toDate()?.time ?: 0L
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
            dietCycleType = try { com.example.data.model.DietCycleType.valueOf(data["dietCycleType"] as? String ?: "FIXED_DAILY") } catch (_: Exception) { com.example.data.model.DietCycleType.FIXED_DAILY },
            isAthleteOrWarmClimate = (data["isAthleteOrWarmClimate"] as? Boolean) ?: true,
            targetCalories = (data["targetCalories"] as? Number)?.toInt() ?: 2650,
            targetProteinGrams = (data["targetProteinGrams"] as? Number)?.toInt() ?: 175,
            targetCarbsGrams = (data["targetCarbsGrams"] as? Number)?.toInt() ?: 320,
            targetFatGrams = (data["targetFatGrams"] as? Number)?.toInt() ?: 65,
            targetWaterMl = (data["targetWaterMl"] as? Number)?.toInt() ?: 3530,
            monthlyTargetWeightKg = (data["monthlyTargetWeightKg"] as? Number)?.toFloat() ?: 81.0f,
            monthlyTargetDurationWeeks = (data["monthlyTargetDurationWeeks"] as? Number)?.toInt() ?: 4,
            monthlyNotes = data["monthlyNotes"] as? String ?: "",
            isDarkMode = (data["isDarkMode"] as? Boolean) ?: true,
            isBiometricLockEnabled = (data["isBiometricLockEnabled"] as? Boolean) ?: false,
            isLoggedIn = true,
            hasCompletedOnboarding = (data["hasCompletedOnboarding"] as? Boolean) ?: true
        )
    }

    // =========================================================================
    // 2. METAS CALÓRICAS (Caloric Goals - Field-Level Diffing)
    // =========================================================================

    /**
     * Sincroniza e armazena as metas calóricas na coleção /users/{userId}/calorieGoals
     * enviando apenas os campos alterados (Delta Sync).
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
            val canonicalGoals = FirestoreDeltaEngine.caloricGoalsToCanonicalMap(
                uid = uid,
                targetCalories = targetCalories,
                targetProteinGrams = targetProteinGrams,
                targetCarbsGrams = targetCarbsGrams,
                targetFatGrams = targetFatGrams,
                targetWaterMl = targetWaterMl
            )
            val previousGoals = deltaLedger.getLastCaloricGoalsMap(uid)
            val delta = FirestoreDeltaEngine.computeFieldDiff(previousGoals, canonicalGoals)

            if (!delta.isChanged) {
                AppLog.d("FirestoreSync", "Delta Sync: Metas calóricas sem alterações (ignorado).")
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val userRef = firestore.collection("users").document(uid)
            val goalRef = userRef.collection("calorieGoals").document("current")

            val deltaPayload = LinkedHashMap<String, Any>(delta.changedFields).apply {
                put("userId", uid)
                put("updatedAtMillis", nowMillis)
                put("updatedAt", FieldValue.serverTimestamp())
            }

            awaitCloudWrite(
                firestore.batch()
                    .set(goalRef, deltaPayload, SetOptions.merge())
                    .set(userRef, deltaPayload, SetOptions.merge())
                    .commit()
            )
            deltaLedger.markCaloricGoalsSynced(uid, canonicalGoals, nowMillis)

            AppLog.d("FirestoreSync", "Delta Sync: Metas calóricas sincronizadas (${delta.changedFieldNames.size} campos).")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao salvar metas calóricas: ${e.message}", e)
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
                targetWaterMl = (d["targetWaterMl"] as? Number)?.toInt() ?: 3000,
                updatedAtMillis = extractTimestampMillis(d)
            )
            Result.success(result)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao buscar meta calórica: ${e.message}", e)
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

        val reg = trackListener(
            firestore.collection("users").document(uid)
                .collection("calorieGoals").document("current")
                .addSnapshotListener(MetadataChanges.EXCLUDE) { snapshot, error ->
                    if (error != null) {
                        AppLog.w("FirestoreSync", "Erro ao observar metas calóricas: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val d = snapshot.data ?: emptyMap()
                        val goal = CaloricGoalData(
                            targetCalories = (d["targetCalories"] as? Number)?.toInt() ?: 2000,
                            targetProteinGrams = (d["targetProteinGrams"] as? Number)?.toInt() ?: 150,
                            targetCarbsGrams = (d["targetCarbsGrams"] as? Number)?.toInt() ?: 200,
                            targetFatGrams = (d["targetFatGrams"] as? Number)?.toInt() ?: 50,
                            targetWaterMl = (d["targetWaterMl"] as? Number)?.toInt() ?: 3000,
                            updatedAtMillis = extractTimestampMillis(d)
                        )
                        trySend(goal)
                    } else {
                        trySend(null)
                    }
                }
        )

        awaitClose { untrackAndRemoveListener(reg) }
    }

    // =========================================================================
    // 3. LOGS DE HISTÓRICO (Hash-Based Diffing por Entidade)
    // =========================================================================

    private fun workoutLogPayload(uid: String, log: WorkoutLog): Map<String, Any> = mapOf(
        "userId" to uid,
        "routineId" to log.routineId,
        "routineName" to log.routineName,
        "dateMillis" to log.dateMillis,
        "durationMinutes" to log.durationMinutes,
        "totalVolumeKg" to log.totalVolumeKg.toDouble(),
        "totalSetsCompleted" to log.totalSetsCompleted,
        "perceivedExertion" to log.perceivedExertion,
        "notes" to log.notes
    )

    suspend fun syncWorkoutLogToCloud(log: WorkoutLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val docId = if (log.id > 0) log.id.toString() else "workout_${log.dateMillis}"
            val baseData = workoutLogPayload(uid, log)
            val hash = FirestoreDeltaEngine.computeRecordHash("workoutLogs", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "workoutLogs", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val docRef = firestore.collection("users").document(uid)
                .collection("workoutLogs").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "workoutLogs", docId, hash, nowMillis)
            AppLog.d("FirestoreSync", "Delta Sync: WorkoutLog $docId enviado.")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao salvar WorkoutLog no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun deleteWorkoutLogFromCloud(logId: Long, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val docId = logId.toString()
            awaitCloudWrite(firestore.collection("users").document(uid)
                .collection("workoutLogs").document(docId).delete())
            deltaLedger.removeEntity(uid, "workoutLogs", docId)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao excluir WorkoutLog do Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun buildScopedDateQuery(
        userId: String,
        collectionName: String,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT,
        beforeDateMillis: Long? = null
    ): Query {
        val base = firestore.collection("users").document(userId).collection(collectionName)
        val filtered = if (beforeDateMillis != null && beforeDateMillis > 0L) {
            base.whereLessThan("dateMillis", beforeDateMillis)
        } else if (daysBack > 0L) {
            val cutoffMillis = System.currentTimeMillis() - (daysBack * MILLIS_PER_DAY)
            base.whereGreaterThanOrEqualTo("dateMillis", cutoffMillis)
        } else {
            base
        }
        return filtered
            .orderBy("dateMillis", Query.Direction.DESCENDING)
            .limit(limitCount.coerceIn(1L, 500L))
    }

    suspend fun fetchWorkoutLogsFromCloud(
        customUid: String? = null,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT,
        beforeDateMillis: Long? = null
    ): Result<List<WorkoutLog>> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val querySnapshot = buildScopedDateQuery(
                userId = uid,
                collectionName = "workoutLogs",
                daysBack = daysBack,
                limitCount = limitCount,
                beforeDateMillis = beforeDateMillis
            ).get(Source.DEFAULT).await()

            val logs = querySnapshot.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                val idLong = doc.id.substringAfter("_", doc.id).toLongOrNull() ?: 0L
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
            AppLog.e("FirestoreSync", "Erro ao buscar logs de treino: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun observeWorkoutLogsFromCloud(
        customUid: String? = null,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT
    ): Flow<List<WorkoutLog>> = callbackFlow {
        val uid = resolveUserId(customUid)
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val reg = trackListener(
            buildScopedDateQuery(
                userId = uid,
                collectionName = "workoutLogs",
                daysBack = daysBack,
                limitCount = limitCount
            ).addSnapshotListener(MetadataChanges.EXCLUDE) { snapshot, error ->
                if (error != null) {
                    AppLog.w("FirestoreSync", "Erro ao escutar workoutLogs: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val logs = snapshot.documents.mapNotNull { doc ->
                        val d = doc.data ?: return@mapNotNull null
                        val idLong = doc.id.substringAfter("_", doc.id).toLongOrNull() ?: 0L
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
        )

        awaitClose { untrackAndRemoveListener(reg) }
    }

    private fun gpsRouteLogPayload(uid: String, log: GpsRouteLog): Map<String, Any> = mapOf(
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
        "notes" to log.notes
    )

    suspend fun syncGpsRouteLogToCloud(log: GpsRouteLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(Exception("Usuário não autenticado"))

        return try {
            val docId = if (log.id > 0) log.id.toString() else "gps_${log.dateMillis}"
            val baseData = gpsRouteLogPayload(uid, log)
            val hash = FirestoreDeltaEngine.computeRecordHash("gpsRouteLogs", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "gpsRouteLogs", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val docRef = firestore.collection("users").document(uid)
                .collection("gpsRouteLogs").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "gpsRouteLogs", docId, hash, nowMillis)
            AppLog.d("FirestoreSync", "Delta Sync: GpsRouteLog $docId enviado.")
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao salvar GpsRouteLog no Firestore: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun fetchGpsRouteLogsFromCloud(
        customUid: String? = null,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT,
        beforeDateMillis: Long? = null
    ): Result<List<GpsRouteLog>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snap = buildScopedDateQuery(uid, "gpsRouteLogs", daysBack, limitCount, beforeDateMillis)
                .get(Source.DEFAULT).await()
            val logs = snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                GpsRouteLog(
                    id = doc.id.substringAfter("_", doc.id).toLongOrNull() ?: 0L,
                    routeName = d["routeName"] as? String ?: "Corrida GPS",
                    activityType = d["activityType"] as? String ?: "RUNNING",
                    dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: 0L,
                    dateDisplay = d["dateDisplay"] as? String ?: "",
                    durationSeconds = (d["durationSeconds"] as? Number)?.toInt() ?: 0,
                    distanceMeters = (d["distanceMeters"] as? Number)?.toFloat() ?: 0f,
                    stepsCount = (d["stepsCount"] as? Number)?.toInt() ?: 0,
                    caloriesBurned = (d["caloriesBurned"] as? Number)?.toFloat() ?: 0f,
                    averagePaceMinPerKm = (d["averagePaceMinPerKm"] as? Number)?.toFloat() ?: 0f,
                    routePointsJson = d["routePointsJson"] as? String ?: "[]",
                    notes = d["notes"] as? String ?: ""
                )
            }
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchCardioLogsFromCloud(
        customUid: String? = null,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT,
        beforeDateMillis: Long? = null
    ): Result<List<CardioLog>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snap = buildScopedDateQuery(uid, "cardioLogs", daysBack, limitCount, beforeDateMillis)
                .get(Source.DEFAULT).await()
            val logs = snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                CardioLog(
                    id = doc.id.substringAfter("_", doc.id).toLongOrNull() ?: 0L,
                    type = runCatching { com.example.data.model.CardioType.valueOf(d["type"] as? String ?: "TREADMILL") }
                        .getOrDefault(com.example.data.model.CardioType.TREADMILL),
                    durationMinutes = (d["durationMinutes"] as? Number)?.toInt() ?: 0,
                    caloriesBurned = (d["caloriesBurned"] as? Number)?.toInt() ?: 0,
                    distanceKm = (d["distanceKm"] as? Number)?.toFloat(),
                    dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: 0L,
                    notes = d["notes"] as? String ?: ""
                )
            }
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProgressLogsFromCloud(
        customUid: String? = null,
        daysBack: Long = DEFAULT_LOG_WINDOW_DAYS,
        limitCount: Long = DEFAULT_LOG_PAGE_LIMIT,
        beforeDateMillis: Long? = null
    ): Result<List<ProgressLog>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snap = buildScopedDateQuery(uid, "progressLogs", daysBack, limitCount, beforeDateMillis)
                .get(Source.DEFAULT).await()
            val logs = snap.documents.mapNotNull { doc ->
                val d = doc.data ?: return@mapNotNull null
                ProgressLog(
                    id = doc.id.toLongOrNull() ?: 0L,
                    dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: 0L,
                    dateDisplay = d["dateDisplay"] as? String ?: "",
                    weightKg = (d["weightKg"] as? Number)?.toFloat() ?: 0f,
                    bodyFatPercentage = (d["bodyFatPercent"] as? Number)?.toFloat() ?: 0f,
                    notes = d["notes"] as? String ?: ""
                )
            }
            Result.success(logs)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun foodRecordPayload(uid: String, record: FoodRecord): Map<String, Any> = mapOf(
        "userId" to uid, "dateString" to record.dateString, "dateMillis" to record.dateMillis,
        "mealType" to record.mealType.name, "foodItemId" to record.foodItemId,
        "foodName" to record.foodName, "amount" to record.amount.toDouble(), "unit" to record.unit,
        "calories" to record.calories.toDouble(), "protein" to record.protein.toDouble(),
        "carbs" to record.carbs.toDouble(), "fat" to record.fat.toDouble(),
        "fiber" to record.fiber.toDouble(), "notes" to record.notes
    )

    suspend fun syncFoodRecordToCloud(record: FoodRecord, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = if (record.id > 0) record.id.toString() else "food_${record.dateMillis}"
            val baseData = foodRecordPayload(uid, record)
            val hash = FirestoreDeltaEngine.computeRecordHash("foodRecords", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "foodRecords", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("foodRecords").document(docId).set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "foodRecords", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteFoodRecordFromCloud(recordId: Long, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = recordId.toString()
            awaitCloudWrite(firestore.collection("users").document(uid).collection("foodRecords").document(docId).delete())
            deltaLedger.removeEntity(uid, "foodRecords", docId)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun fetchFoodRecordsFromCloud(customUid: String? = null): Result<List<FoodRecord>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snapshot = firestore.collection("users").document(uid).collection("foodRecords").get(Source.DEFAULT).await()
            val records = snapshot.documents.map { doc ->
                val d = doc.data ?: emptyMap()
                FoodRecord(
                    id = doc.id.toLongOrNull() ?: 0L,
                    dateString = d["dateString"] as? String ?: "",
                    dateMillis = (d["dateMillis"] as? Number)?.toLong() ?: 0L,
                    mealType = com.example.data.model.MealType.valueOf(d["mealType"] as? String ?: "BREAKFAST"),
                    foodItemId = (d["foodItemId"] as? Number)?.toLong() ?: 0L,
                    foodName = d["foodName"] as? String ?: "",
                    amount = (d["amount"] as? Number)?.toFloat() ?: 0f,
                    unit = d["unit"] as? String ?: "g",
                    calories = (d["calories"] as? Number)?.toFloat() ?: 0f,
                    protein = (d["protein"] as? Number)?.toFloat() ?: 0f,
                    carbs = (d["carbs"] as? Number)?.toFloat() ?: 0f,
                    fat = (d["fat"] as? Number)?.toFloat() ?: 0f,
                    fiber = (d["fiber"] as? Number)?.toFloat() ?: 0f,
                    notes = d["notes"] as? String ?: ""
                )
            }
            Result.success(records)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun cardioLogPayload(uid: String, log: CardioLog): Map<String, Any> = mapOf(
        "userId" to uid, "type" to log.type.name, "durationMinutes" to log.durationMinutes,
        "caloriesBurned" to log.caloriesBurned, "distanceKm" to (log.distanceKm?.toDouble() ?: 0.0),
        "dateMillis" to log.dateMillis, "notes" to log.notes
    )

    suspend fun syncCardioLogToCloud(log: CardioLog, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = if (log.id > 0) log.id.toString() else "cardio_${log.dateMillis}"
            val baseData = cardioLogPayload(uid, log)
            val hash = FirestoreDeltaEngine.computeRecordHash("cardioLogs", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "cardioLogs", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("cardioLogs").document(docId).set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "cardioLogs", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    private fun dailyIntakePayload(uid: String, intake: DailyIntakeHistory): Map<String, Any> = mapOf(
        "userId" to uid, "dateString" to intake.dateString, "dateMillis" to intake.dateMillis,
        "totalCalories" to intake.totalCalories.toDouble(), "totalProtein" to intake.totalProtein.toDouble(),
        "totalCarbs" to intake.totalCarbs.toDouble(), "totalFat" to intake.totalFat.toDouble(),
        "targetCalories" to intake.targetCalories, "isAdhered" to intake.isAdhered,
        "waterDrankMl" to intake.waterDrankMl
    )

    suspend fun syncDailyIntakeToCloud(intake: DailyIntakeHistory, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = intake.dateString
            val baseData = dailyIntakePayload(uid, intake)
            val hash = FirestoreDeltaEngine.computeRecordHash("dailyIntake", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "dailyIntake", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(firestore.collection("users").document(uid).collection("dailyIntake").document(docId).set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "dailyIntake", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    private fun favoriteFoodPayload(uid: String, food: com.example.data.model.FoodItem): Map<String, Any> = mapOf(
        "userId" to uid,
        "foodId" to food.id,
        "foodName" to food.name,
        "category" to food.category.name,
        "isFavorite" to food.isFavorite
    )

    suspend fun syncFavoriteFoodToCloud(food: com.example.data.model.FoodItem, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = food.id.toString()
            val baseData = favoriteFoodPayload(uid, food)
            val hash = FirestoreDeltaEngine.computeRecordHash("favoriteFoods", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "favoriteFoods", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val docRef = firestore.collection("users").document(uid).collection("favoriteFoods").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "favoriteFoods", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteFavoriteFoodFromCloud(foodId: Long, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = foodId.toString()
            awaitCloudWrite(firestore.collection("users").document(uid).collection("favoriteFoods").document(docId).delete())
            deltaLedger.removeEntity(uid, "favoriteFoods", docId)
            Result.success(Unit)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun fetchFavoriteFoodsFromCloud(customUid: String? = null): Result<List<Long>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snapshot = firestore.collection("users").document(uid).collection("favoriteFoods")
                .whereEqualTo("isFavorite", true)
                .get(Source.DEFAULT).await()
            val ids = snapshot.documents.mapNotNull { (it.data?.get("foodId") as? Number)?.toLong() }
            Result.success(ids)
        } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }
    }

    // =========================================================================
    // 4. ITENS ADICIONAIS EXISTENTES (MealPlan, Shopping, Progress, Routines)
    // =========================================================================

    private fun mealPlanPayload(uid: String, mealPlan: MealPlanItem): Map<String, Any> = mapOf(
        "userId" to uid,
        "mealType" to mealPlan.mealType.name,
        "name" to mealPlan.foodName,
        "portionGrams" to mealPlan.amount.toDouble(),
        "calories" to mealPlan.calories.toDouble(),
        "protein" to mealPlan.protein.toDouble(),
        "carbs" to mealPlan.carbs.toDouble(),
        "fat" to mealPlan.fat.toDouble(),
        "isCompletedToday" to mealPlan.isCompletedToday,
        "isFixed" to mealPlan.isFixed
    )

    suspend fun syncMealPlanToCloud(mealPlan: MealPlanItem, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val docId = mealPlan.id.toString()
            val baseData = mealPlanPayload(uid, mealPlan)
            val hash = FirestoreDeltaEngine.computeRecordHash("mealPlans", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "mealPlans", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val doc = firestore.collection("users").document(uid)
                .collection("mealPlans").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "mealPlans", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMealPlansFromCloud(customUid: String? = null): Result<List<MealPlanItem>> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val snapshot = firestore.collection("users").document(uid).collection("mealPlans").get(Source.DEFAULT).await()
            val mealPlans = snapshot.documents.map { doc ->
                val d = doc.data ?: emptyMap()
                MealPlanItem(
                    id = doc.id.toLongOrNull() ?: 0L,
                    mealType = com.example.data.model.MealType.valueOf(d["mealType"] as? String ?: "BREAKFAST"),
                    foodItemId = (d["foodItemId"] as? Number)?.toLong() ?: 0L,
                    foodName = d["name"] as? String ?: "",
                    foodCategory = com.example.data.model.FoodCategory.OTHER,
                    amount = (d["portionGrams"] as? Number)?.toFloat() ?: 0f,
                    unit = "g",
                    calories = (d["calories"] as? Number)?.toFloat() ?: 0f,
                    protein = (d["protein"] as? Number)?.toFloat() ?: 0f,
                    carbs = (d["carbs"] as? Number)?.toFloat() ?: 0f,
                    fat = (d["fat"] as? Number)?.toFloat() ?: 0f,
                    isFixed = (d["isFixed"] as? Boolean) ?: false
                )
            }
            Result.success(mealPlans)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun shoppingItemPayload(uid: String, item: ShoppingItem): Map<String, Any> = mapOf(
        "userId" to uid,
        "name" to item.name,
        "category" to item.category.name,
        "totalQuantityGrams" to item.quantity.toDouble(),
        "unit" to item.unit,
        "estimatedPrice" to item.estimatedPrice.toDouble(),
        "isBought" to item.isChecked
    )

    suspend fun syncShoppingItemToCloud(item: ShoppingItem, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val docId = item.id.toString()
            val baseData = shoppingItemPayload(uid, item)
            val hash = FirestoreDeltaEngine.computeRecordHash("shoppingItems", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "shoppingItems", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val doc = firestore.collection("users").document(uid)
                .collection("shoppingItems").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "shoppingItems", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao sincronizar item de compra: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun progressLogPayload(uid: String, log: ProgressLog): Map<String, Any> = mapOf(
        "userId" to uid,
        "dateMillis" to log.dateMillis,
        "weightKg" to log.weightKg.toDouble(),
        "bodyFatPercent" to log.bodyFatPercentage.toDouble(),
        "notes" to log.notes
    )

    suspend fun syncProgressLogToCloud(log: ProgressLog): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("Usuário não autenticado no Firebase"))
        return try {
            val docId = log.id.toString()
            val baseData = progressLogPayload(uid, log)
            val hash = FirestoreDeltaEngine.computeRecordHash("progressLogs", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "progressLogs", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val doc = firestore.collection("users").document(uid)
                .collection("progressLogs").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(doc.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "progressLogs", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AppLog.e("FirestoreSync", "Erro ao sincronizar progresso: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun workoutRoutinePayload(uid: String, routine: WorkoutRoutine): Map<String, Any> = mapOf(
        "userId" to uid,
        "splitType" to routine.splitType.name,
        "letter" to routine.letter,
        "title" to routine.title,
        "dayOfWeekLabel" to routine.dayOfWeekLabel,
        "targetMuscleGroups" to routine.targetMuscleGroups,
        "estimatedDurationMinutes" to routine.estimatedDurationMinutes
    )

    suspend fun syncWorkoutRoutineToCloud(routine: WorkoutRoutine, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        return try {
            val docId = routine.id.toString()
            val baseData = workoutRoutinePayload(uid, routine)
            val hash = FirestoreDeltaEngine.computeRecordHash("workoutRoutines", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "workoutRoutines", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val docRef = firestore.collection("users").document(uid)
                .collection("workoutRoutines").document(docId)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "workoutRoutines", docId, hash, nowMillis)
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun workoutExercisePayload(uid: String, wid: String, exercise: WorkoutExercise): Map<String, Any> = mapOf(
        "userId" to uid,
        "workoutId" to wid,
        "routineId" to exercise.routineId,
        "exerciseName" to exercise.exerciseName,
        "muscleGroup" to exercise.muscleGroup,
        "sets" to exercise.sets,
        "repsMin" to exercise.repsMin,
        "repsMax" to exercise.repsMax,
        "currentWeightKg" to exercise.currentWeightKg.toDouble(),
        "restSeconds" to exercise.restSeconds,
        "orderIndex" to exercise.orderIndex
    )

    suspend fun syncWorkoutExerciseToCloud(exercise: WorkoutExercise, workoutId: String? = null, customUid: String? = null): Result<Unit> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        val wid = workoutId ?: if (exercise.routineId > 0) exercise.routineId.toString() else "free"
        val exerciseIdStr = exercise.id.toString()
        val docId = "${wid}_$exerciseIdStr"
        val path = "users/$uid/workoutRoutines/$wid/exercises/$exerciseIdStr"
        FirestoreLogger.logWriteAttempt("syncWorkoutExerciseToCloud", uid, path, exercise)
        return try {
            val baseData = workoutExercisePayload(uid, wid, exercise)
            val hash = FirestoreDeltaEngine.computeRecordHash("workoutExercises", baseData)
            if (!deltaLedger.hasEntityChanged(uid, "workoutExercises", docId, hash)) {
                return Result.success(Unit)
            }

            val nowMillis = System.currentTimeMillis()
            val userDocRef = firestore.collection("users").document(uid)
            val data = baseData + mapOf(
                "updatedAtMillis" to nowMillis,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val docRef = userDocRef.collection("workoutRoutines").document(wid)
                .collection("exercises").document(exerciseIdStr)

            awaitCloudWrite(docRef.set(data, SetOptions.merge()))
            deltaLedger.markEntitySynced(uid, "workoutExercises", docId, hash, nowMillis)
            FirestoreLogger.logSuccess("syncWorkoutExerciseToCloud", path)
            Result.success(Unit)
        } catch (e: Exception) {
            FirestoreLogger.logError("syncWorkoutExerciseToCloud", path, e)
            Result.failure(e)
        }
    }

    suspend fun migrateExistingExercises(exercises: List<WorkoutExercise>, customUid: String? = null): Result<Int> {
        val uid = resolveUserId(customUid) ?: return Result.failure(Exception("Usuário não autenticado"))
        var successCount = 0
        exercises.forEach { ex ->
            val wid = if (ex.routineId > 0) ex.routineId.toString() else "free"
            val res = syncWorkoutExerciseToCloud(ex, wid, uid)
            if (res.isSuccess) successCount++
        }
        return Result.success(successCount)
    }

    private val userOwnedCollections = listOf(
        "calorieGoals",
        "workoutLogs",
        "gpsRouteLogs",
        "mealPlans",
        "shoppingItems",
        "progressLogs",
        "foodRecords",
        "cardioLogs",
        "dailyIntake",
        "favoriteFoods",
        "workoutRoutines",
        "workout_logs",
        "cardio_logs",
        "shoppingList",
        "workoutExercises",
        "workouts"
    )

    private suspend fun deleteCollectionFromServer(
        userRef: com.google.firebase.firestore.DocumentReference,
        collectionName: String
    ) {
        while (true) {
            val page = userRef.collection(collectionName)
                .limit(450)
                .get(Source.SERVER)
                .await()
            if (page.isEmpty) return

            val batch = firestore.batch()
            page.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    /**
     * Deletes the complete known Firestore subtree while the Firebase session is
     * still valid. Authentication is removed only after this operation succeeds.
     */
    suspend fun deleteAllUserData(): Result<Unit> {
        val uid = currentUserId
            ?: return Result.failure(IllegalStateException("Usuário não autenticado."))

        return try {
            val userRef = firestore.collection("users").document(uid)
            userOwnedCollections.forEach { collectionName ->
                deleteCollectionFromServer(userRef, collectionName)
            }
            userRef.delete().await()
            deltaLedger.clearUserLedger(uid)
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(IllegalStateException(
                "Não foi possível confirmar a exclusão dos dados no servidor. Tente novamente com internet.",
                e
            ))
        }
    }

    // =========================================================================
    // 5. SINCRONIZAÇÃO DELTA COMPLETA (Delta Batch Sync)
    // =========================================================================

    /**
     * Sincroniza para o Firestore exclusivamente os itens que sofreram alterações
     * (Diff por campo no Perfil/Metas + Diff por hash determinístico nos Históricos).
     */
    suspend fun syncAllData(
        profile: UserProfile?,
        workoutLogs: List<WorkoutLog>,
        progressLogs: List<ProgressLog> = emptyList(),
        gpsLogs: List<GpsRouteLog> = emptyList(),
        foodRecords: List<FoodRecord> = emptyList(),
        cardioLogs: List<CardioLog> = emptyList(),
        dailyIntakes: List<DailyIntakeHistory> = emptyList(),
        favoriteFoods: List<com.example.data.model.FoodItem> = emptyList(),
        workoutRoutines: List<WorkoutRoutine> = emptyList(),
        workoutExercises: List<WorkoutExercise> = emptyList(),
        mealPlanItems: List<MealPlanItem> = emptyList(),
        shoppingItems: List<ShoppingItem> = emptyList()
    ): Result<SyncReport> {
        val uid = resolveUserId(profile?.firebaseUid)
            ?: return Result.failure(IllegalStateException("Entre na sua conta para sincronizar. Os dados locais continuam no aparelho."))

        // Executa automaticamente a migração de coleções legadas para o formato canônico camelCase e apaga o legado
        if (migratedUsersInSession.add(uid)) {
            runCatching {
                val migrationService = WorkoutMigrationService(context, firestore, auth)
                migrationService.migrateUserWorkoutsAndExercises(uid, preserveLegacyDocuments = false)
                if (workoutExercises.isNotEmpty()) {
                    migrationService.migrateLocalExercisesToSubcollections(workoutExercises, uid)
                }
            }
        }

        data class QueuedBatchEntityWrite(
            val docRef: DocumentReference,
            val payload: Map<String, Any>,
            val collectionKey: String,
            val docId: String,
            val hash: String,
            val timestampMillis: Long
        )

        val operations = mutableListOf<suspend () -> Result<Unit>>()
        val queuedEntityWrites = mutableListOf<QueuedBatchEntityWrite>()
        var skippedCount = 0
        var updatedFieldsCount = 0
        val userDocRef = firestore.collection("users").document(uid)
        val batchTimestampMillis = System.currentTimeMillis()

        fun enqueueEntityWriteIfChanged(
            collectionKey: String,
            docRef: DocumentReference,
            docId: String,
            basePayload: Map<String, Any>
        ) {
            val hash = FirestoreDeltaEngine.computeRecordHash(collectionKey, basePayload)
            if (deltaLedger.hasEntityChanged(uid, collectionKey, docId, hash)) {
                val payloadWithMeta = basePayload + mapOf(
                    "updatedAtMillis" to batchTimestampMillis,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                queuedEntityWrites.add(
                    QueuedBatchEntityWrite(
                        docRef = docRef,
                        payload = payloadWithMeta,
                        collectionKey = collectionKey,
                        docId = docId,
                        hash = hash,
                        timestampMillis = batchTimestampMillis
                    )
                )
            } else {
                skippedCount++
            }
        }

        if (profile != null) {
            val canonicalProfile = FirestoreDeltaEngine.profileToCanonicalMap(profile, uid)
            val profileDiff = FirestoreDeltaEngine.computeFieldDiff(deltaLedger.getLastProfileMap(uid), canonicalProfile)
            if (profileDiff.isChanged) {
                updatedFieldsCount += profileDiff.changedFieldNames.size
                operations.add { syncProfileToCloud(profile) }
            } else {
                skippedCount++
            }

            val canonicalGoals = FirestoreDeltaEngine.caloricGoalsToCanonicalMap(
                uid = uid,
                targetCalories = profile.targetCalories,
                targetProteinGrams = profile.targetProteinGrams,
                targetCarbsGrams = profile.targetCarbsGrams,
                targetFatGrams = profile.targetFatGrams,
                targetWaterMl = profile.targetWaterMl
            )
            val goalsDiff = FirestoreDeltaEngine.computeFieldDiff(deltaLedger.getLastCaloricGoalsMap(uid), canonicalGoals)
            if (goalsDiff.isChanged) {
                operations.add {
                    syncCaloricGoalsToCloud(
                        profile.targetCalories,
                        profile.targetProteinGrams,
                        profile.targetCarbsGrams,
                        profile.targetFatGrams,
                        profile.targetWaterMl,
                        uid
                    )
                }
            } else {
                skippedCount++
            }
        }

        workoutRoutines.forEach { routine ->
            if (routine.id > 0L) {
                val docId = routine.id.toString()
                enqueueEntityWriteIfChanged(
                    collectionKey = "workoutRoutines",
                    docRef = userDocRef.collection("workoutRoutines").document(docId),
                    docId = docId,
                    basePayload = workoutRoutinePayload(uid, routine)
                )
            }
        }

        workoutExercises.forEach { exercise ->
            if (exercise.id > 0L) {
                val wid = if (exercise.routineId > 0L) exercise.routineId.toString() else "free"
                val exerciseIdStr = exercise.id.toString()
                val docId = "${wid}_$exerciseIdStr"
                enqueueEntityWriteIfChanged(
                    collectionKey = "workoutExercises",
                    docRef = userDocRef.collection("workoutRoutines").document(wid).collection("exercises").document(exerciseIdStr),
                    docId = docId,
                    basePayload = workoutExercisePayload(uid, wid, exercise)
                )
            }
        }

        mealPlanItems.forEach { item ->
            if (item.id > 0L) {
                val docId = item.id.toString()
                enqueueEntityWriteIfChanged(
                    collectionKey = "mealPlans",
                    docRef = userDocRef.collection("mealPlans").document(docId),
                    docId = docId,
                    basePayload = mealPlanPayload(uid, item)
                )
            }
        }

        shoppingItems.forEach { item ->
            if (item.id > 0L) {
                val docId = item.id.toString()
                enqueueEntityWriteIfChanged(
                    collectionKey = "shoppingItems",
                    docRef = userDocRef.collection("shoppingItems").document(docId),
                    docId = docId,
                    basePayload = shoppingItemPayload(uid, item)
                )
            }
        }

        workoutLogs.forEach { log ->
            val docId = if (log.id > 0) log.id.toString() else "workout_${log.dateMillis}"
            enqueueEntityWriteIfChanged(
                collectionKey = "workoutLogs",
                docRef = userDocRef.collection("workoutLogs").document(docId),
                docId = docId,
                basePayload = workoutLogPayload(uid, log)
            )
        }

        progressLogs.forEach { log ->
            val docId = log.id.toString()
            enqueueEntityWriteIfChanged(
                collectionKey = "progressLogs",
                docRef = userDocRef.collection("progressLogs").document(docId),
                docId = docId,
                basePayload = progressLogPayload(uid, log)
            )
        }

        gpsLogs.forEach { log ->
            val docId = if (log.id > 0) log.id.toString() else "gps_${log.dateMillis}"
            enqueueEntityWriteIfChanged(
                collectionKey = "gpsRouteLogs",
                docRef = userDocRef.collection("gpsRouteLogs").document(docId),
                docId = docId,
                basePayload = gpsRouteLogPayload(uid, log)
            )
        }

        foodRecords.forEach { record ->
            val docId = if (record.id > 0) record.id.toString() else "food_${record.dateMillis}"
            enqueueEntityWriteIfChanged(
                collectionKey = "foodRecords",
                docRef = userDocRef.collection("foodRecords").document(docId),
                docId = docId,
                basePayload = foodRecordPayload(uid, record)
            )
        }

        cardioLogs.forEach { log ->
            val docId = if (log.id > 0) log.id.toString() else "cardio_${log.dateMillis}"
            enqueueEntityWriteIfChanged(
                collectionKey = "cardioLogs",
                docRef = userDocRef.collection("cardioLogs").document(docId),
                docId = docId,
                basePayload = cardioLogPayload(uid, log)
            )
        }

        dailyIntakes.forEach { intake ->
            val docId = intake.dateString
            enqueueEntityWriteIfChanged(
                collectionKey = "dailyIntake",
                docRef = userDocRef.collection("dailyIntake").document(docId),
                docId = docId,
                basePayload = dailyIntakePayload(uid, intake)
            )
        }

        favoriteFoods.forEach { food ->
            val docId = food.id.toString()
            enqueueEntityWriteIfChanged(
                collectionKey = "favoriteFoods",
                docRef = userDocRef.collection("favoriteFoods").document(docId),
                docId = docId,
                basePayload = favoriteFoodPayload(uid, food)
            )
        }

        // Commit entity writes in WriteBatch chunks of up to MAX_BATCH_OPERATIONS (450 < 500 Firestore limit)
        queuedEntityWrites.chunked(MAX_BATCH_OPERATIONS).forEach { chunk ->
            operations.add {
                try {
                    val batch = firestore.batch()
                    chunk.forEach { item ->
                        batch.set(item.docRef, item.payload, SetOptions.merge())
                    }
                    awaitCloudWrite(batch.commit())
                    chunk.forEach { item ->
                        deltaLedger.markEntitySynced(
                            uid,
                            item.collectionKey,
                            item.docId,
                            item.hash,
                            item.timestampMillis
                        )
                    }
                    Result.success(Unit)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        }

        val baseReport = collectSyncResults(operations)
        val deltaReport = baseReport.copy(
            skipped = skippedCount,
            updatedFieldsCount = updatedFieldsCount
        )
        _lastSyncReport.value = deltaReport
        return Result.success(deltaReport)
    }
}
