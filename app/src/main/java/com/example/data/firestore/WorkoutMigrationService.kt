package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.WorkoutExercise
import com.example.util.FirestoreLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Relatório do processo de migração de treinos e exercícios.
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
 * Caminhos de destino e compatibilidade retroativa para um exercício no Firestore.
 */
data class ExerciseFirestorePaths(
    val normalizedWorkoutId: String,
    val routineWorkoutExercisesPath: String,
    val workoutsWorkoutExercisesPath: String,
    val workoutsExercisesPath: String,
    val legacyRootWorkoutExercisesPath: String
)

/**
 * Serviço responsável por percorrer os documentos de treino e exercícios do usuário no Firestore e
 * migrar/copiar de forma não-destrutiva para as subcoleções aninhadas compatíveis:
 *   - /users/{userId}/workoutRoutines/{workoutId}/workoutExercises/{exerciseId}
 *   - /users/{userId}/workouts/{workoutId}/workoutExercises/{exerciseId}
 *   - /users/{userId}/workouts/{workoutId}/exercises/{exerciseId}
 * preservando a estrutura atual (/users/{userId}/workoutExercises/{exerciseId}) para retrocompatibilidade.
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
         * Calcula todos os caminhos compatíveis (novas subcoleções aninhadas + caminho legado preservado).
         */
        fun resolveCompatiblePaths(
            userId: String,
            rawWorkoutId: String?,
            exerciseId: String
        ): ExerciseFirestorePaths {
            val wid = normalizeWorkoutId(rawWorkoutId)
            val exId = exerciseId.trim().ifBlank { UUID.randomUUID().toString() }
            return ExerciseFirestorePaths(
                normalizedWorkoutId = wid,
                routineWorkoutExercisesPath = "users/$userId/workoutRoutines/$wid/workoutExercises/$exId",
                workoutsWorkoutExercisesPath = "users/$userId/workouts/$wid/workoutExercises/$exId",
                workoutsExercisesPath = "users/$userId/workouts/$wid/exercises/$exId",
                legacyRootWorkoutExercisesPath = "users/$userId/workoutExercises/${wid}_$exId"
            )
        }
    }

    /**
     * Resolve o ID do usuário autenticado no Firebase.
     */
    private fun resolveUserId(customUid: String?): String? {
        val uid = customUid?.trim()?.takeIf { it.isNotBlank() } ?: auth.currentUser?.uid
        return uid?.takeIf { it.isNotBlank() && !it.startsWith("guest_") }
    }

    private suspend fun writeToCompatibleNestedPaths(
        userDocRef: DocumentReference,
        workoutId: String,
        exerciseId: String,
        payload: Map<String, Any?>,
        alsoMirrorLegacyRoot: Boolean = true
    ) {
        val routineWorkoutExercisesRef = userDocRef
            .collection("workoutRoutines")
            .document(workoutId)
            .collection("workoutExercises")
            .document(exerciseId)

        val workoutsWorkoutExercisesRef = userDocRef
            .collection("workouts")
            .document(workoutId)
            .collection("workoutExercises")
            .document(exerciseId)

        val workoutsExercisesRef = userDocRef
            .collection("workouts")
            .document(workoutId)
            .collection("exercises")
            .document(exerciseId)

        val batch = firestore.batch()
        batch.set(routineWorkoutExercisesRef, payload, SetOptions.merge())
        batch.set(workoutsWorkoutExercisesRef, payload, SetOptions.merge())
        batch.set(workoutsExercisesRef, payload, SetOptions.merge())
        if (alsoMirrorLegacyRoot) {
            val legacyRef = userDocRef
                .collection("workoutExercises")
                .document("${workoutId}_$exerciseId")
            batch.set(legacyRef, payload, SetOptions.merge())
        }
        batch.commit().await()
    }

    /**
     * Executa a migração completa dos treinos e exercícios do usuário no Firestore.
     *
     * Por padrão ([preserveLegacyDocuments] = true), preserva os documentos existentes na coleção
     * atual `/users/{userId}/workoutExercises` enquanto copia todos os registros para as
     * subcoleções aninhadas `workoutRoutines/{id}/workoutExercises` e `workouts/{id}/exercises`.
     */
    suspend fun migrateUserWorkoutsAndExercises(
        customUid: String? = null,
        preserveLegacyDocuments: Boolean = true
    ): Result<WorkoutMigrationReport> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(IllegalStateException("Usuário não autenticado no Firebase para migração."))

        Log.i(TAG, "Iniciando migração não-destrutiva de exercícios para o usuário: $uid")
        val errors = mutableListOf<String>()
        var workoutsProcessed = 0
        var exercisesMigrated = 0
        var legacyExercisesCopied = 0
        var failedCount = 0

        val userDocRef = firestore.collection("users").document(uid)

        // =========================================================================
        // 1. Percorrer documentos na coleção /users/{userId}/workouts e /workoutRoutines
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
                                FirestoreLogger.logWriteAttempt("migrateEmbeddedExercise", uid, paths.routineWorkoutExercisesPath, exMap)

                                val payload = HashMap<String, Any?>(exMap).apply {
                                    put("userId", uid)
                                    put("workoutId", paths.normalizedWorkoutId)
                                    put("id", exerciseId)
                                    put("updatedAt", FieldValue.serverTimestamp())
                                }

                                writeToCompatibleNestedPaths(
                                    userDocRef = userDocRef,
                                    workoutId = paths.normalizedWorkoutId,
                                    exerciseId = exerciseId,
                                    payload = payload,
                                    alsoMirrorLegacyRoot = true
                                )
                                FirestoreLogger.logSuccess("migrateEmbeddedExercise", paths.routineWorkoutExercisesPath)
                                exercisesMigrated++
                            } catch (e: Exception) {
                                failedCount++
                                val msg = "Erro ao migrar exercício embutido no treino $workoutId: ${e.message}"
                                Log.e(TAG, msg, e)
                                errors.add(msg)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                val msg = "Falha ao ler coleção /users/$uid/$parentCollection: ${e.message}"
                Log.e(TAG, msg, e)
                errors.add(msg)
            }
        }

        // =========================================================================
        // 2. Percorrer coleção /users/{userId}/workoutExercises e copiar para as subcoleções aninhadas
        //    (workoutRoutines/{id}/workoutExercises e workouts/{id}/exercises)
        //    preservando os documentos originais para retrocompatibilidade
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
                    FirestoreLogger.logWriteAttempt("copyLegacyWorkoutExercise", uid, paths.routineWorkoutExercisesPath, data)

                    val updatedData = HashMap<String, Any?>(data).apply {
                        put("userId", uid)
                        put("workoutId", paths.normalizedWorkoutId)
                        put("updatedAt", FieldValue.serverTimestamp())
                    }

                    writeToCompatibleNestedPaths(
                        userDocRef = userDocRef,
                        workoutId = paths.normalizedWorkoutId,
                        exerciseId = exerciseId,
                        payload = updatedData,
                        alsoMirrorLegacyRoot = false
                    )
                    FirestoreLogger.logSuccess("copyLegacyWorkoutExercise", paths.routineWorkoutExercisesPath)

                    if (!preserveLegacyDocuments) {
                        legacyDoc.reference.delete().await()
                    }
                    legacyExercisesCopied++
                } catch (e: Exception) {
                    failedCount++
                    val msg = "Erro ao copiar exercício legado ${legacyDoc.id}: ${e.message}"
                    Log.e(TAG, msg, e)
                    errors.add(msg)
                }
            }
        } catch (e: Exception) {
            val msg = "Falha ao ler coleção workoutExercises: ${e.message}"
            Log.e(TAG, msg, e)
            errors.add(msg)
        }

        val report = WorkoutMigrationReport(
            userId = uid,
            workoutsProcessed = workoutsProcessed,
            exercisesMigrated = exercisesMigrated,
            legacyExercisesMoved = legacyExercisesCopied,
            failedCount = failedCount,
            errors = errors
        )

        Log.i(TAG, "Migração concluída: $workoutsProcessed treinos processados, $exercisesMigrated embutidos migrados, $legacyExercisesCopied exercícios copiados para subcoleções aninhadas, $failedCount falhas.")
        return Result.success(report)
    }

    /**
     * Migra uma lista de entidades locais de exercício para as subcoleções aninhadas compatíveis do Firestore
     * (`workoutRoutines/{id}/workoutExercises` e `workouts/{id}/exercises`),
     * preservando também o espelho em `/users/{uid}/workoutExercises/{wid}_{exerciseId}`.
     */
    suspend fun migrateLocalExercisesToSubcollections(
        exercises: List<WorkoutExercise>,
        customUid: String? = null
    ): Result<Int> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(IllegalStateException("Usuário não autenticado no Firebase."))

        var count = 0
        val userDocRef = firestore.collection("users").document(uid)

        for (ex in exercises) {
            if (ex.id <= 0L) continue
            val rawWorkoutId = if (ex.routineId > 0L) ex.routineId.toString() else DEFAULT_WORKOUT_ID
            val exerciseId = ex.id.toString()
            val paths = resolveCompatiblePaths(uid, rawWorkoutId, exerciseId)

            try {
                FirestoreLogger.logWriteAttempt("migrateLocalExercise", uid, paths.routineWorkoutExercisesPath, ex)

                val data = mapOf(
                    "userId" to uid,
                    "workoutId" to paths.normalizedWorkoutId,
                    "routineId" to ex.routineId,
                    "exerciseName" to ex.exerciseName,
                    "muscleGroup" to ex.muscleGroup,
                    "sets" to ex.sets,
                    "repsMin" to ex.repsMin,
                    "repsMax" to ex.repsMax,
                    "currentWeightKg" to ex.currentWeightKg.toDouble(),
                    "restSeconds" to ex.restSeconds,
                    "orderIndex" to ex.orderIndex,
                    "notes" to ex.notes,
                    "biomechanicsGuide" to ex.biomechanicsGuide,
                    "updatedAt" to FieldValue.serverTimestamp()
                )

                writeToCompatibleNestedPaths(
                    userDocRef = userDocRef,
                    workoutId = paths.normalizedWorkoutId,
                    exerciseId = exerciseId,
                    payload = data,
                    alsoMirrorLegacyRoot = true
                )

                FirestoreLogger.logSuccess("migrateLocalExercise", paths.routineWorkoutExercisesPath)
                count++
            } catch (e: Exception) {
                FirestoreLogger.logError("migrateLocalExercise", paths.routineWorkoutExercisesPath, e)
                Log.e(TAG, "Erro ao migrar exercício local $exerciseId: ${e.message}", e)
            }
        }
        return Result.success(count)
    }
}
