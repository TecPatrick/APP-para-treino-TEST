package com.example.data.firestore

import android.content.Context
import com.example.R
import com.example.data.model.WorkoutExercise
import com.example.util.AppLog
import com.example.util.FirestoreLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Relatório do processo de migração de treinos, exercícios e coleções legadas.
 */
data class WorkoutMigrationReport(
    val userId: String,
    val workoutsProcessed: Int = 0,
    val exercisesMigrated: Int = 0,
    val legacyExercisesMoved: Int = 0,
    val failedCount: Int = 0,
    val errors: List<String> = emptyList()
)

/**
 * Caminhos de destino canônico e caminhos legados para um exercício no Firestore.
 */
data class ExerciseFirestorePaths(
    val normalizedWorkoutId: String,
    val canonicalRoutineExercisesPath: String,
    val routineWorkoutExercisesPath: String,
    val workoutsWorkoutExercisesPath: String,
    val workoutsExercisesPath: String,
    val legacyRootWorkoutExercisesPath: String
)

/**
 * Serviço responsável por migrar documentos das coleções legadas do usuário no Firestore
 * para os nomes canônicos em camelCase:
 *   - /users/{userId}/workoutRoutines/{workoutId}/exercises/{exerciseId}
 *   - /users/{userId}/workoutLogs/{logId}
 *   - /users/{userId}/cardioLogs/{logId}
 *   - /users/{userId}/shoppingItems/{itemId}
 * A leitura dos caminhos antigos fica restrita à migração e apaga os documentos legados após migrar.
 */
class WorkoutMigrationService(
    private val context: Context,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
    ),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "WorkoutMigration"
        const val DEFAULT_WORKOUT_ID = "free"
        const val MAX_BATCH_OPERATIONS = 450

        private val ALLOWED_EXERCISE_KEYS = setOf(
            "userId", "id", "workoutId", "routineId", "exerciseName", "muscleGroup",
            "sets", "repsMin", "repsMax", "currentWeightKg", "restSeconds",
            "orderIndex", "notes", "biomechanicsGuide", "gifDemonstrationUrl",
            "updatedAtMillis", "updatedAt", "createdAt"
        )

        /**
         * Normaliza o identificador do treino/rotina para uso seguro em subcoleções do Firestore.
         */
        fun normalizeWorkoutId(rawWorkoutId: String?): String {
            val trimmed = rawWorkoutId?.trim().orEmpty()
            return if (trimmed.isNotBlank() && trimmed != "null" && trimmed != "0") {
                trimmed
            } else {
                DEFAULT_WORKOUT_ID
            }
        }

        /**
         * Calcula o caminho canônico (`workoutRoutines/{id}/exercises/{exerciseId}`) e os caminhos legados.
         */
        fun resolveCompatiblePaths(
            userId: String,
            rawWorkoutId: String?,
            exerciseId: String
        ): ExerciseFirestorePaths {
            val wid = normalizeWorkoutId(rawWorkoutId)
            val exId = exerciseId.trim().ifBlank { UUID.randomUUID().toString() }
            val canonicalPath = "users/$userId/workoutRoutines/$wid/exercises/$exId"
            return ExerciseFirestorePaths(
                normalizedWorkoutId = wid,
                canonicalRoutineExercisesPath = canonicalPath,
                routineWorkoutExercisesPath = canonicalPath,
                workoutsWorkoutExercisesPath = "users/$userId/workouts/$wid/workoutExercises/$exId",
                workoutsExercisesPath = "users/$userId/workouts/$wid/exercises/$exId",
                legacyRootWorkoutExercisesPath = "users/$userId/workoutExercises/${wid}_$exId"
            )
        }
    }

    private fun resolveUserId(customUid: String?): String? {
        val uid = customUid?.trim()?.takeIf { it.isNotBlank() } ?: auth.currentUser?.uid
        return uid?.takeIf { it.isNotBlank() && !it.startsWith("guest_") }
    }

    private fun sanitizeExercisePayload(
        raw: Map<String, Any?>,
        uid: String,
        workoutId: String,
        exerciseId: String
    ): Map<String, Any> {
        val filtered = LinkedHashMap<String, Any>()
        for ((k, v) in raw) {
            if (k in ALLOWED_EXERCISE_KEYS && v != null) {
                filtered[k] = v
            }
        }
        filtered["userId"] = uid
        filtered["workoutId"] = workoutId
        filtered["id"] = exerciseId
        val exName = (filtered["exerciseName"] as? String)?.trim().orEmpty()
        filtered["exerciseName"] = exName.ifBlank { "Exercício" }.take(200)
        filtered["updatedAtMillis"] = System.currentTimeMillis()
        filtered["updatedAt"] = FieldValue.serverTimestamp()
        return filtered
    }

    private suspend fun writeToCanonicalExercisePath(
        userDocRef: DocumentReference,
        workoutId: String,
        exerciseId: String,
        payload: Map<String, Any>
    ) {
        val canonicalRef = userDocRef
            .collection("workoutRoutines")
            .document(workoutId)
            .collection("exercises")
            .document(exerciseId)

        canonicalRef.set(payload, SetOptions.merge()).await()
    }

    /**
     * Executa a migração completa dos treinos, exercícios e coleções legadas do usuário no Firestore
     * para os caminhos canônicos em camelCase e remove os documentos legados após migrar
     * quando [preserveLegacyDocuments] for `false` (padrão).
     */
    suspend fun migrateUserWorkoutsAndExercises(
        customUid: String? = null,
        preserveLegacyDocuments: Boolean = false
    ): Result<WorkoutMigrationReport> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(IllegalStateException("Usuário não autenticado no Firebase para migração."))

        AppLog.i(TAG, "Iniciando migração de exercícios e coleções legadas para o usuário: ${AppLog.maskUid(uid)}")
        val errors = mutableListOf<String>()
        var workoutsProcessed = 0
        var exercisesMigrated = 0
        var legacyExercisesCopied = 0
        var failedCount = 0

        val userDocRef = firestore.collection("users").document(uid)

        // =========================================================================
        // 1. Percorrer documentos nas coleções /workouts (legado) e /workoutRoutines
        // =========================================================================
        for (parentCollection in listOf("workouts", "workoutRoutines")) {
            try {
                val workoutsSnapshot = userDocRef.collection(parentCollection).get().await()
                for (workoutDoc in workoutsSnapshot.documents) {
                    workoutsProcessed++
                    val rawId = workoutDoc.id.trim()
                    val candidateId = if (rawId.isBlank() || rawId == "null") {
                        workoutDoc.getString("workoutId") ?: workoutDoc.getString("id")
                    } else {
                        rawId
                    }
                    val workoutId = normalizeWorkoutId(candidateId)

                    @Suppress("UNCHECKED_CAST")
                    val embeddedExercises = workoutDoc.get("exercises") as? List<Map<String, Any?>>
                    if (!embeddedExercises.isNullOrEmpty()) {
                        for (exMap in embeddedExercises) {
                            try {
                                val rawExId = (exMap["id"] ?: exMap["exerciseId"])?.toString()?.trim()
                                val exerciseId = if (!rawExId.isNullOrBlank() && rawExId != "null") {
                                    rawExId
                                } else {
                                    UUID.randomUUID().toString()
                                }

                                val paths = resolveCompatiblePaths(uid, workoutId, exerciseId)
                                FirestoreLogger.logWriteAttempt("migrateEmbeddedExercise", uid, paths.canonicalRoutineExercisesPath, exMap)

                                val payload = sanitizeExercisePayload(exMap, uid, paths.normalizedWorkoutId, exerciseId)
                                writeToCanonicalExercisePath(
                                    userDocRef = userDocRef,
                                    workoutId = paths.normalizedWorkoutId,
                                    exerciseId = exerciseId,
                                    payload = payload
                                )
                                FirestoreLogger.logSuccess("migrateEmbeddedExercise", paths.canonicalRoutineExercisesPath)
                                exercisesMigrated++
                            } catch (e: Exception) {
                                failedCount++
                                val msg = "Erro ao migrar exercício embutido no treino $workoutId: ${e.message}"
                                AppLog.e(TAG, msg, e)
                                errors.add(msg)
                            }
                        }
                    }

                    // Migrar subcoleções legadas dentro do treino (workoutExercises e workouts/{id}/exercises)
                    val legacySubcollections = if (parentCollection == "workouts") {
                        listOf("workoutExercises", "exercises")
                    } else {
                        listOf("workoutExercises")
                    }
                    for (legacySub in legacySubcollections) {
                        runCatching {
                            val subSnap = workoutDoc.reference.collection(legacySub).get().await()
                            for (subDoc in subSnap.documents) {
                                val data = subDoc.data ?: continue
                                val exerciseId = subDoc.id
                                val paths = resolveCompatiblePaths(uid, workoutId, exerciseId)
                                val payload = sanitizeExercisePayload(data, uid, paths.normalizedWorkoutId, exerciseId)
                                writeToCanonicalExercisePath(userDocRef, paths.normalizedWorkoutId, exerciseId, payload)
                                if (!preserveLegacyDocuments) {
                                    subDoc.reference.delete().await()
                                }
                                legacyExercisesCopied++
                            }
                        }
                    }

                    if (parentCollection == "workouts" && !preserveLegacyDocuments) {
                        runCatching { workoutDoc.reference.delete().await() }
                    }
                }
            } catch (e: Exception) {
                val msg = "Falha ao ler coleção /users/${AppLog.maskUid(uid)}/$parentCollection: ${e.message}"
                AppLog.e(TAG, msg, e)
                errors.add(msg)
            }
        }

        // =========================================================================
        // 2. Migrar coleção legada /users/{userId}/workoutExercises -> workoutRoutines/{id}/exercises
        // =========================================================================
        try {
            val legacySnapshot = userDocRef.collection("workoutExercises").get().await()
            for (legacyDoc in legacySnapshot.documents) {
                try {
                    val rawDocId = legacyDoc.id
                    val data = legacyDoc.data ?: continue

                    val rawWorkoutId = (data["workoutId"] ?: data["routineId"])?.toString()?.trim()
                    val workoutId = normalizeWorkoutId(rawWorkoutId)
                    val exerciseId = if (rawDocId.startsWith("${workoutId}_")) {
                        rawDocId.removePrefix("${workoutId}_").ifBlank { rawDocId }
                    } else {
                        rawDocId
                    }

                    val paths = resolveCompatiblePaths(uid, workoutId, exerciseId)
                    FirestoreLogger.logWriteAttempt("copyLegacyWorkoutExercise", uid, paths.canonicalRoutineExercisesPath, data)

                    val updatedData = sanitizeExercisePayload(data, uid, paths.normalizedWorkoutId, exerciseId)
                    writeToCanonicalExercisePath(
                        userDocRef = userDocRef,
                        workoutId = paths.normalizedWorkoutId,
                        exerciseId = exerciseId,
                        payload = updatedData
                    )
                    FirestoreLogger.logSuccess("copyLegacyWorkoutExercise", paths.canonicalRoutineExercisesPath)

                    if (!preserveLegacyDocuments) {
                        legacyDoc.reference.delete().await()
                    }
                    legacyExercisesCopied++
                } catch (e: Exception) {
                    failedCount++
                    val msg = "Erro ao migrar exercício legado ${legacyDoc.id}: ${e.message}"
                    AppLog.e(TAG, msg, e)
                    errors.add(msg)
                }
            }
        } catch (e: Exception) {
            val msg = "Falha ao ler coleção workoutExercises: ${e.message}"
            AppLog.e(TAG, msg, e)
            errors.add(msg)
        }

        // =========================================================================
        // 3. Migrar coleções legadas workout_logs -> workoutLogs, cardio_logs -> cardioLogs, shoppingList -> shoppingItems
        // =========================================================================
        migrateSimpleLegacyCollection(
            userDocRef = userDocRef,
            uid = uid,
            legacyName = "workout_logs",
            canonicalName = "workoutLogs",
            allowedKeys = setOf(
                "userId", "routineId", "routineName", "dateMillis", "durationMinutes",
                "totalVolumeKg", "totalSetsCompleted", "perceivedExertion", "notes",
                "updatedAtMillis", "updatedAt", "createdAt"
            ),
            defaults = mapOf("dateMillis" to System.currentTimeMillis()),
            preserveLegacy = preserveLegacyDocuments
        )
        migrateSimpleLegacyCollection(
            userDocRef = userDocRef,
            uid = uid,
            legacyName = "cardio_logs",
            canonicalName = "cardioLogs",
            allowedKeys = setOf(
                "userId", "type", "durationMinutes", "caloriesBurned", "distanceKm",
                "dateMillis", "notes", "updatedAtMillis", "updatedAt", "createdAt"
            ),
            defaults = mapOf("type" to "CARDIO", "dateMillis" to System.currentTimeMillis()),
            preserveLegacy = preserveLegacyDocuments
        )
        migrateSimpleLegacyCollection(
            userDocRef = userDocRef,
            uid = uid,
            legacyName = "shoppingList",
            canonicalName = "shoppingItems",
            allowedKeys = setOf(
                "userId", "id", "name", "category", "totalQuantityGrams", "quantity",
                "unit", "periodDays", "estimatedPrice", "myEstimatedPrice",
                "isBought", "isChecked", "isAutoGenerated", "notes",
                "updatedAtMillis", "updatedAt", "createdAt"
            ),
            defaults = mapOf("name" to "Item"),
            preserveLegacy = preserveLegacyDocuments
        )

        val report = WorkoutMigrationReport(
            userId = uid,
            workoutsProcessed = workoutsProcessed,
            exercisesMigrated = exercisesMigrated,
            legacyExercisesMoved = legacyExercisesCopied,
            failedCount = failedCount,
            errors = errors
        )

        AppLog.i(TAG, "Migração concluída: $workoutsProcessed treinos processados, $exercisesMigrated embutidos migrados, $legacyExercisesCopied exercícios migrados, $failedCount falhas.")
        return Result.success(report)
    }

    private suspend fun migrateSimpleLegacyCollection(
        userDocRef: DocumentReference,
        uid: String,
        legacyName: String,
        canonicalName: String,
        allowedKeys: Set<String>,
        defaults: Map<String, Any>,
        preserveLegacy: Boolean
    ) {
        runCatching {
            val snap = userDocRef.collection(legacyName).limit(MAX_BATCH_OPERATIONS.toLong()).get().await()
            if (snap.isEmpty) return
            val opsPerDoc = if (preserveLegacy) 1 else 2
            val maxDocsPerBatch = (MAX_BATCH_OPERATIONS / opsPerDoc).coerceAtLeast(1)

            for (chunk in snap.documents.chunked(maxDocsPerBatch)) {
                val batch = firestore.batch()
                var opsInBatch = 0
                for (doc in chunk) {
                    val raw = doc.data ?: continue
                    val payload = LinkedHashMap<String, Any>()
                    for ((k, v) in raw) {
                        if (k in allowedKeys && v != null) {
                            payload[k] = v
                        }
                    }
                    for ((defKey, defVal) in defaults) {
                        if (!payload.containsKey(defKey)) {
                            payload[defKey] = defVal
                        }
                    }
                    payload["userId"] = uid
                    payload["updatedAtMillis"] = System.currentTimeMillis()
                    payload["updatedAt"] = FieldValue.serverTimestamp()

                    val destRef = userDocRef.collection(canonicalName).document(doc.id)
                    batch.set(destRef, payload, SetOptions.merge())
                    opsInBatch++
                    if (!preserveLegacy) {
                        batch.delete(doc.reference)
                        opsInBatch++
                    }
                }
                if (opsInBatch > 0) {
                    batch.commit().await()
                }
            }
        }
    }

    /**
     * Sincroniza uma lista de entidades locais de exercício para a subcoleção canônica
     * `workoutRoutines/{id}/exercises/{exerciseId}` usando `WriteBatch` (máximo 450 operações por lote).
     */
    suspend fun migrateLocalExercisesToSubcollections(
        exercises: List<WorkoutExercise>,
        customUid: String? = null
    ): Result<Int> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(IllegalStateException("Usuário não autenticado no Firebase."))

        val validExercises = exercises.filter { it.id > 0L }
        if (validExercises.isEmpty()) return Result.success(0)

        var count = 0
        val userDocRef = firestore.collection("users").document(uid)

        for (chunk in validExercises.chunked(MAX_BATCH_OPERATIONS)) {
            try {
                val batch = firestore.batch()
                val nowMillis = System.currentTimeMillis()
                for (ex in chunk) {
                    val rawWorkoutId = if (ex.routineId > 0L) ex.routineId.toString() else DEFAULT_WORKOUT_ID
                    val exerciseId = ex.id.toString()
                    val paths = resolveCompatiblePaths(uid, rawWorkoutId, exerciseId)
                    FirestoreLogger.logWriteAttempt("migrateLocalExercise", uid, paths.canonicalRoutineExercisesPath, ex)

                    val data = mapOf(
                        "userId" to uid,
                        "workoutId" to paths.normalizedWorkoutId,
                        "routineId" to ex.routineId,
                        "exerciseName" to ex.exerciseName.take(200),
                        "muscleGroup" to ex.muscleGroup.take(120),
                        "sets" to ex.sets,
                        "repsMin" to ex.repsMin,
                        "repsMax" to ex.repsMax,
                        "currentWeightKg" to ex.currentWeightKg.toDouble(),
                        "restSeconds" to ex.restSeconds,
                        "orderIndex" to ex.orderIndex,
                        "notes" to ex.notes.take(2000),
                        "biomechanicsGuide" to ex.biomechanicsGuide.take(2000),
                        "updatedAtMillis" to nowMillis,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )

                    val canonicalRef = userDocRef
                        .collection("workoutRoutines")
                        .document(paths.normalizedWorkoutId)
                        .collection("exercises")
                        .document(exerciseId)
                    batch.set(canonicalRef, data, SetOptions.merge())
                }
                batch.commit().await()
                count += chunk.size
            } catch (e: Exception) {
                AppLog.e(TAG, "Erro ao migrar lote de exercícios locais: ${e.message}", e)
            }
        }
        return Result.success(count)
    }
}
