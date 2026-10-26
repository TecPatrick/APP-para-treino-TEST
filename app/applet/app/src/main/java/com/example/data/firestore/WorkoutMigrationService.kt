package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.R
import com.example.data.model.WorkoutExercise
import com.example.util.FirestoreLogger
import com.google.firebase.auth.FirebaseAuth
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
 * Serviço responsável por percorrer os documentos de treino do usuário no Firestore,
 * mover os exercícios existentes para a nova estrutura de subcoleção:
 *   /users/{userId}/workouts/{workoutId}/exercises/{exerciseId}
 * Tratando treinos sem ID válido usando o identificador padrão 'free'.
 */
class WorkoutMigrationService(
    private val context: Context,
    private val firestore: FirebaseFirestore = try {
        val dbId = context.getString(R.string.firestore_database_id)
        if (dbId.isNotBlank() && dbId != "(default)") {
            FirebaseFirestore.getInstance(dbId)
        } else {
            FirebaseFirestore.getInstance()
        }
    } catch (_: Exception) {
        FirebaseFirestore.getInstance()
    },
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    companion object {
        private const val TAG = "WorkoutMigration"
        const val DEFAULT_WORKOUT_ID = "free"
    }

    /**
     * Resolve o ID do usuário autenticado no Firebase.
     */
    private fun resolveUserId(customUid: String?): String? {
        val uid = customUid?.trim()?.takeIf { it.isNotBlank() } ?: auth.currentUser?.uid
        return uid?.takeIf { it.isNotBlank() && !it.startsWith("guest_") }
    }

    /**
     * Executa a migração completa dos treinos e exercícios do usuário no Firestore.
     *
     * 1. Percorre /users/{userId}/workouts e migra exercícios embutidos no documento
     *    para /users/{userId}/workouts/{workoutId}/exercises/{exerciseId}.
     * 2. Percorre a coleção legada /users/{userId}/workoutExercises e move cada documento
     *    para a nova subcoleção do treino correspondente (ou 'free').
     */
    suspend fun migrateUserWorkoutsAndExercises(customUid: String? = null): Result<WorkoutMigrationReport> {
        val uid = resolveUserId(customUid)
            ?: return Result.failure(IllegalStateException("Usuário não autenticado no Firebase para migração."))

        Log.i(TAG, "Iniciando migração de exercícios para o usuário: $uid")
        val errors = mutableListOf<String>()
        var workoutsProcessed = 0
        var exercisesMigrated = 0
        var legacyExercisesMoved = 0
        var failedCount = 0

        val userDocRef = firestore.collection("users").document(uid)

        // =========================================================================
        // 1. Percorrer documentos na coleção /users/{userId}/workouts
        // =========================================================================
        try {
            val workoutsSnapshot = userDocRef.collection("workouts").get().await()
            for (workoutDoc in workoutsSnapshot.documents) {
                workoutsProcessed++
                val rawId = workoutDoc.id.trim()
                val workoutId = if (rawId.isBlank() || rawId == "null") {
                    val fieldId = workoutDoc.getString("workoutId") ?: workoutDoc.getString("id")
                    if (!fieldId.isNullOrBlank() && fieldId != "null") fieldId.trim() else DEFAULT_WORKOUT_ID
                } else {
                    rawId
                }

                // Verifica se há lista de exercícios embutidos no documento do treino
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

                            val targetPath = "users/$uid/workouts/$workoutId/exercises/$exerciseId"
                            FirestoreLogger.logWriteAttempt("migrateEmbeddedExercise", uid, targetPath, exMap)

                            val exerciseDocRef = userDocRef
                                .collection("workouts")
                                .document(workoutId)
                                .collection("exercises")
                                .document(exerciseId)

                            val payload = HashMap<String, Any?>(exMap).apply {
                                put("userId", uid)
                                put("workoutId", workoutId)
                                put("id", exerciseId)
                                put("updatedAt", FieldValue.serverTimestamp())
                            }

                            exerciseDocRef.set(payload, SetOptions.merge()).await()
                            FirestoreLogger.logSuccess("migrateEmbeddedExercise", targetPath)
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
            val msg = "Falha ao ler coleção /users/$uid/workouts: ${e.message}"
            Log.e(TAG, msg, e)
            errors.add(msg)
        }

        // =========================================================================
        // 2. Percorrer e mover coleção legada /users/{userId}/workoutExercises
        // =========================================================================
        try {
            val legacySnapshot = userDocRef.collection("workoutExercises").get().await()
            for (legacyDoc in legacySnapshot.documents) {
                try {
                    val exerciseId = legacyDoc.id
                    val data = legacyDoc.data ?: continue

                    // Identificar workoutId ou usar DEFAULT_WORKOUT_ID ("free")
                    val rawWorkoutId = (data["workoutId"] ?: data["routineId"])?.toString()?.trim()
                    val workoutId = if (!rawWorkoutId.isNullOrBlank() && rawWorkoutId != "null" && rawWorkoutId != "0") {
                        rawWorkoutId
                    } else {
                        DEFAULT_WORKOUT_ID
                    }

                    val targetPath = "users/$uid/workouts/$workoutId/exercises/$exerciseId"
                    FirestoreLogger.logWriteAttempt("moveLegacyWorkoutExercise", uid, targetPath, data)

                    val targetDocRef = userDocRef
                        .collection("workouts")
                        .document(workoutId)
                        .collection("exercises")
                        .document(exerciseId)

                    val updatedData = HashMap<String, Any?>(data).apply {
                        put("userId", uid)
                        put("workoutId", workoutId)
                        put("updatedAt", FieldValue.serverTimestamp())
                    }

                    // Escreve na nova subcoleção
                    targetDocRef.set(updatedData, SetOptions.merge()).await()
                    FirestoreLogger.logSuccess("moveLegacyWorkoutExercise", targetPath)

                    // Remove do local legado
                    legacyDoc.reference.delete().await()
                    legacyExercisesMoved++
                } catch (e: Exception) {
                    failedCount++
                    val msg = "Erro ao mover exercício legado ${legacyDoc.id}: ${e.message}"
                    Log.e(TAG, msg, e)
                    errors.add(msg)
                }
            }
        } catch (e: Exception) {
            val msg = "Falha ao ler coleção legada workoutExercises: ${e.message}"
            Log.e(TAG, msg, e)
            errors.add(msg)
        }

        val report = WorkoutMigrationReport(
            userId = uid,
            workoutsProcessed = workoutsProcessed,
            exercisesMigrated = exercisesMigrated,
            legacyExercisesMoved = legacyExercisesMoved,
            failedCount = failedCount,
            errors = errors
        )

        Log.i(TAG, "Migração concluída: $workoutsProcessed treinos processados, $exercisesMigrated exercícios migrados, $legacyExercisesMoved legados movidos, $failedCount falhas.")
        return Result.success(report)
    }

    /**
     * Migra uma lista de entidades locais de exercício para a subcoleção do Firestore.
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
            val workoutId = if (ex.routineId > 0) ex.routineId.toString() else DEFAULT_WORKOUT_ID
            val exerciseId = ex.id.toString()
            val path = "users/$uid/workouts/$workoutId/exercises/$exerciseId"

            try {
                FirestoreLogger.logWriteAttempt("migrateLocalExercise", uid, path, ex)
                val docRef = userDocRef
                    .collection("workouts")
                    .document(workoutId)
                    .collection("exercises")
                    .document(exerciseId)

                val data = mapOf(
                    "userId" to uid,
                    "workoutId" to workoutId,
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

                docRef.set(data, SetOptions.merge()).await()
                FirestoreLogger.logSuccess("migrateLocalExercise", path)
                count++
            } catch (e: Exception) {
                FirestoreLogger.logError("migrateLocalExercise", path, e)
                Log.e(TAG, "Erro ao migrar exercício local $exerciseId: ${e.message}", e)
            }
        }
        return Result.success(count)
    }
}
