package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiRepository
import com.example.data.local.PrepopulatedData
import com.example.data.model.ProgressLog
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSchedule
import com.example.data.model.WorkoutSplitType
import com.example.util.AppLog
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseVisualGuide
import com.example.util.WorkoutAudioHapticHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    fun getWorkoutAdvice(prompt: String) {
        viewModelScope.launch {
            _aiAdvice.value = "Consultando treinador IA..."
            _aiAdvice.value = GeminiRepository.getWorkoutAdvice(prompt)
        }
    }

    fun clearAdvice() {
        _aiAdvice.value = null
    }

    fun restoreDefaultRoutines() {
        viewModelScope.launch {
            try {
                val currentRoutines = repository.workoutRoutines.firstOrNull() ?: emptyList()
                for (r in currentRoutines) {
                    repository.deleteWorkoutRoutine(r.id)
                }
                for (routine in PrepopulatedData.defaultRoutines) {
                    repository.addWorkoutRoutine(routine)
                }
                for (exercise in PrepopulatedData.defaultExercises) {
                    repository.addWorkoutExercise(exercise)
                }
                showToast("Fichas padrão (Treinos A, B, C e E) restauradas com sucesso!")
            } catch (e: Exception) {
                AppLog.e("WorkoutViewModel", "Erro ao restaurar fichas", e)
                showToast("Erro ao restaurar fichas: ${e.message}")
            }
        }
    }

    fun createBlankRoutine(letter: String, title: String, splitType: WorkoutSplitType = WorkoutSplitType.ABCDE) {
        viewModelScope.launch {
            try {
                val currentList = repository.workoutRoutines.firstOrNull() ?: emptyList()
                val nextOrder = (currentList.maxOfOrNull { it.orderIndex } ?: 0) + 1
                val newRoutine = WorkoutRoutine(
                    letter = letter,
                    title = title,
                    splitType = splitType,
                    targetMuscleGroups = "Geral",
                    estimatedDurationMinutes = 60,
                    orderIndex = nextOrder
                )
                repository.addWorkoutRoutine(newRoutine)
                showToast("Ficha $letter criada! Agora é só incluir os exercícios 💪")
            } catch (e: Exception) {
                AppLog.e("WorkoutViewModel", "Erro ao criar ficha", e)
                showToast("Erro ao criar ficha: ${e.message}")
            }
        }
    }

    fun saveRoomWorkoutSchedule(dayOfWeek: String, activityType: String, durationMinutes: Int, intensity: String) {
        viewModelScope.launch {
            repository.saveWorkoutSchedule(
                WorkoutSchedule(
                    dayOfWeek = dayOfWeek,
                    activityType = activityType,
                    durationMinutes = durationMinutes,
                    intensity = intensity
                )
            )
            showToast("Cronograma de treino agendado no Room!")
        }
    }

    fun saveWorkoutPlan(plan: WorkoutPlan) {
        viewModelScope.launch {
            if (plan.id == 0L) {
                repository.addWorkoutPlan(plan)
                showToast("Plano de treino criado!")
            } else {
                repository.updateWorkoutPlan(plan)
                showToast("Plano de treino atualizado!")
            }
        }
    }

    fun switchActiveWorkoutPlan(planId: Long) {
        viewModelScope.launch {
            repository.switchActiveWorkoutPlan(planId)
            showToast("Plano de treino ativado!")
        }
    }

    fun deleteWorkoutPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutPlan(id)
            showToast("Plano de treino excluído!")
        }
    }

    fun addWorkoutRoutineWithSplit(
        splitType: WorkoutSplitType,
        letter: String,
        title: String,
        dayOfWeekLabel: String,
        targetMuscleGroups: String,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val routine = WorkoutRoutine(
                splitType = splitType,
                letter = letter.uppercase().take(2),
                title = title,
                dayOfWeekLabel = dayOfWeekLabel,
                targetMuscleGroups = targetMuscleGroups,
                estimatedDurationMinutes = durationMinutes
            )
            repository.addWorkoutRoutine(routine)
            showToast("Novo treino criado com sucesso!")
        }
    }

    fun addWorkoutExercise(
        routineId: Long,
        exerciseName: String,
        muscleGroup: String,
        sets: Int,
        repsMin: Int,
        repsMax: Int,
        currentWeightKg: Float,
        restSeconds: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val exercise = WorkoutExercise(
                routineId = routineId,
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = sets,
                repsMin = repsMin,
                repsMax = repsMax,
                currentWeightKg = currentWeightKg,
                restSeconds = restSeconds,
                notes = notes,
                biomechanicsGuide = "Controle o movimento na fase excêntrica e respire ritmadamente."
            )
            repository.addWorkoutExercise(exercise)
            showToast("Exercício adicionado!")
        }
    }

    fun updateExerciseWeightAndReps(exercise: WorkoutExercise, newWeight: Float, sets: Int) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(
                exercise.copy(currentWeightKg = newWeight, sets = sets)
            )
        }
    }

    fun recordExerciseProgression(
        exercise: WorkoutExercise,
        newWeightKg: Float,
        sets: Int,
        reps: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(
                exercise.copy(currentWeightKg = newWeightKg, sets = sets, repsMin = reps, repsMax = reps)
            )
            val now = System.currentTimeMillis()
            val volume = sets * reps * newWeightKg
            val logId = repository.logCompletedWorkout(
                routineId = exercise.routineId,
                routineName = "Treino - Progressão de ${exercise.exerciseName}",
                durationMinutes = 45,
                totalVolumeKg = volume,
                totalSetsCompleted = sets,
                perceivedExertion = 8,
                notes = if (notes.isNotBlank()) notes else "Carga registrada: ${newWeightKg}kg ($sets x $reps)",
                dateMillis = now
            )
            val wLog = WorkoutLog(
                id = logId,
                routineId = exercise.routineId,
                routineName = "Treino - Progressão de ${exercise.exerciseName}",
                dateMillis = now,
                durationMinutes = 45,
                totalVolumeKg = volume,
                totalSetsCompleted = sets,
                perceivedExertion = 8,
                notes = if (notes.isNotBlank()) notes else "Carga registrada: ${newWeightKg}kg ($sets x $reps)"
            )
            firestoreSync.syncWorkoutLogToCloud(wLog, userProfile.value.firebaseUid)
            syncController.enqueueChange("Treino", "Progressão: ${exercise.exerciseName} (${newWeightKg} kg)")
            showToast("Carga de ${exercise.exerciseName} atualizada para ${newWeightKg} kg!")
        }
    }

    fun refreshWorkoutLogsFromFirestore() {
        viewModelScope.launch {
            shared.setFirestoreSyncing(true)
            try {
                val res = firestoreSync.fetchWorkoutLogsFromCloud(userProfile.value.firebaseUid)
                res.onSuccess { list ->
                    shared.setCloudWorkoutLogs(list)
                    showToast("${list.size} treinos históricos carregados do Firestore (App Check verificado).")
                }.onFailure { err ->
                    AppLog.w("WorkoutViewModel", "Erro ao carregar treinos do Firestore: ${err.message}")
                }
            } catch (e: Exception) {
                AppLog.e("WorkoutViewModel", "Exceção ao buscar treinos do Firestore", e)
            } finally {
                shared.setFirestoreSyncing(false)
            }
        }
    }

    fun shareExerciseProgression(
        context: Context,
        exerciseName: String,
        currentWeight: Float,
        prWeight: Float,
        delta: Float,
        oneRepMax: Float
    ) {
        try {
            val text = """
                💪 Progressão de Carga - Virtus Growth
                🏋️ Exercício: $exerciseName
                🔥 Carga Atual: ${currentWeight} kg
                🏆 Recorde Pessoal (PR): ${prWeight} kg
                📈 Evolução: ${if (delta >= 0) "+$delta" else "$delta"} kg
                ⚡ 1RM Estimado: ${oneRepMax.toInt()} kg
            """.trimIndent()
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Progressão de Carga")
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            AppLog.e("WorkoutViewModel", "Error sharing progression", e)
        }
    }

    fun deleteWorkoutExercise(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutExercise(id)
        }
    }

    fun deleteWorkoutRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutRoutine(id)
            syncController.enqueueChange("Treino", "Exclusão de ficha de treino")
            showToast("Ficha de treino excluída!")
        }
    }

    fun saveWorkoutRoutineWithExercises(
        routine: WorkoutRoutine,
        exercises: List<WorkoutExercise>,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val routineId = if (routine.id == 0L) {
                repository.addWorkoutRoutine(routine)
            } else {
                repository.addWorkoutRoutine(routine)
                routine.id
            }
            repository.deleteExercisesForRoutine(routineId)
            exercises.forEachIndexed { index, ex ->
                repository.addWorkoutExercise(
                    ex.copy(
                        id = 0L,
                        routineId = routineId,
                        orderIndex = index
                    )
                )
            }
            syncController.enqueueChange("Treino", "Ficha ${routine.letter}: ${routine.title}")
            showToast(if (routine.id == 0L) "Treino salvo com sucesso!" else "Treino atualizado com sucesso!")
            onSuccess?.invoke(routineId)
        }
    }

    fun createTemplateCustomRoutine(
        templateType: String,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val letter = when (templateType) {
                "CHEST_TRICEPS" -> "P1"
                "BACK_BICEPS" -> "P2"
                "LEGS" -> "P3"
                else -> "P"
            }
            val title = when (templateType) {
                "CHEST_TRICEPS" -> "Treino Custom: Peitoral & Tríceps"
                "BACK_BICEPS" -> "Treino Custom: Dorsal & Bíceps"
                "LEGS" -> "Treino Custom: Pernas Completo"
                else -> "Treino Personalizado"
            }
            val muscles = when (templateType) {
                "CHEST_TRICEPS" -> "Peitoral Maior, Deltoide, Tríceps"
                "BACK_BICEPS" -> "Latíssimo, Romboides, Bíceps"
                "LEGS" -> "Quadríceps, Isquiotibiais, Panturrilhas"
                else -> "Grupamentos Personalizados"
            }

            val newRoutine = WorkoutRoutine(
                splitType = WorkoutSplitType.CUSTOM,
                letter = letter,
                title = title,
                dayOfWeekLabel = "Personalizado",
                targetMuscleGroups = muscles,
                estimatedDurationMinutes = 60
            )
            val newRoutineId = repository.addWorkoutRoutine(newRoutine)

            when (templateType) {
                "CHEST_TRICEPS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas e pés firmes no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Crucifixo no Cross / Crossover", muscleGroup = "Peitoral", sets = 3, repsMin = 10, repsMax = 15, currentWeightKg = 20f, restSeconds = 60, biomechanicsGuide = "Aproxime os punhos no centro.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Cable-Crossover.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, biomechanicsGuide = "Abra a corda no final.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif"))
                }
                "BACK_BICEPS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com as escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsais / Miolo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 90, biomechanicsGuide = "Tronco a 45 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 28f, restSeconds = 60, biomechanicsGuide = "Cotovelos fixos ao lado do tronco.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/EZ-Bar-Curl.gif"))
                }
                "LEGS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada e apoio total dos calcanhares.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Leg Press 45°", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 240f, restSeconds = 90, biomechanicsGuide = "Nunca descole a lombar do encosto.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Cadeira Extensora", muscleGroup = "Quadríceps", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 65f, restSeconds = 60, biomechanicsGuide = "Extensão completa.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Extension.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Stiff com Barra / RDL", muscleGroup = "Isquiotibiais", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 60f, restSeconds = 90, biomechanicsGuide = "Projete o quadril para trás.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Romanian-Deadlift.gif"))
                }
            }
            showToast("Ficha personalizada criada com sucesso!")
            onSuccess?.invoke(newRoutineId)
        }
    }

    fun generateWorkoutProtocolForProfile(onSuccess: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val days = userProfile.value.trainingDaysPerWeek
            if (days <= 3) {
                val r1 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P1", title = "Treino IA: Push (Peito, Ombro & Tríceps)", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral, Deltoides, Tríceps", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas e pés firmes no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Deltoide Anterior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 22f, restSeconds = 75, biomechanicsGuide = "Empurre na vertical no plano escapular.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, biomechanicsGuide = "Abra a corda no final.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif"))

                val r2 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P2", title = "Treino IA: Pull (Costas & Bíceps)", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Latíssimo, Romboides, Bíceps", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com as escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsais / Miolo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 90, biomechanicsGuide = "Tronco a 45 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 28f, restSeconds = 60, biomechanicsGuide = "Cotovelos fixos ao lado do tronco.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/EZ-Bar-Curl.gif"))

                val r3 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P3", title = "Treino IA: Legs (Pernas Completo)", dayOfWeekLabel = "Sexta-feira", targetMuscleGroups = "Quadríceps, Isquiotibiais, Panturrilhas", estimatedDurationMinutes = 65)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Leg Press 45°", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 240f, restSeconds = 90, biomechanicsGuide = "Nunca descole a lombar.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Press.gif"))

                showToast("Protocolo IA Personalizado (3 Dias) gerado com sucesso!")
                onSuccess?.invoke(r1)
            } else {
                val r1 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P1", title = "Treino IA: Peitoral & Deltoide Frontal", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral Maior, Deltoide Frontal", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))

                val r2 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P2", title = "Treino IA: Dorsal & Trapézio", dayOfWeekLabel = "Terça-feira", targetMuscleGroups = "Latíssimo, Romboides, Trapézio", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))

                val r3 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P3", title = "Treino IA: Quadríceps & Panturrilhas", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Quadríceps, Panturrilhas", estimatedDurationMinutes = 65)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))

                showToast("Protocolo IA Personalizado gerado com sucesso!")
                onSuccess?.invoke(r1)
            }
        }
    }

    fun saveFullRoutineWithExercises(
        existingRoutineId: Long?,
        splitType: WorkoutSplitType,
        letter: String,
        title: String,
        dayOfWeekLabel: String,
        targetMuscleGroups: String,
        estimatedDurationMinutes: Int,
        exercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch {
            val targetLetter = letter.ifBlank { "A" }.uppercase().take(2)
            val routineId = if (existingRoutineId != null && existingRoutineId > 0L) {
                val updatedRoutine = WorkoutRoutine(
                    id = existingRoutineId,
                    splitType = splitType,
                    letter = targetLetter,
                    title = title,
                    dayOfWeekLabel = dayOfWeekLabel,
                    targetMuscleGroups = targetMuscleGroups,
                    estimatedDurationMinutes = estimatedDurationMinutes
                )
                repository.addWorkoutRoutine(updatedRoutine)
                repository.deleteExercisesForRoutine(existingRoutineId)

                firestoreSync.syncWorkoutRoutineToCloud(updatedRoutine).onFailure { e ->
                    AppLog.e("WorkoutViewModel", "Falha ao sincronizar rotina: ${e.message}", e)
                    showToast("Erro ao sincronizar rotina na nuvem.")
                }

                existingRoutineId
            } else {
                val newRoutine = WorkoutRoutine(
                    splitType = splitType,
                    letter = targetLetter,
                    title = title,
                    dayOfWeekLabel = dayOfWeekLabel,
                    targetMuscleGroups = targetMuscleGroups,
                    estimatedDurationMinutes = estimatedDurationMinutes
                )
                val id = repository.addWorkoutRoutine(newRoutine)

                firestoreSync.syncWorkoutRoutineToCloud(newRoutine.copy(id = id)).onFailure { e ->
                    AppLog.e("WorkoutViewModel", "Falha ao sincronizar nova rotina: ${e.message}", e)
                    showToast("Erro ao sincronizar nova rotina na nuvem.")
                }

                id
            }

            exercises.forEachIndexed { index, ex ->
                val guide = ExerciseGifMapper.getGuideForExercise(ex.exerciseName)
                val exWithId = ex.copy(
                    id = 0L,
                    routineId = routineId,
                    orderIndex = index,
                    biomechanicsGuide = ex.biomechanicsGuide.ifBlank { guide.postureTip },
                    gifDemonstrationUrl = ex.gifDemonstrationUrl.ifBlank { guide.gifUrl }
                )
                val id = repository.addWorkoutExercise(exWithId)
                val workoutId = routineId.toString()

                firestoreSync.syncWorkoutExerciseToCloud(exWithId.copy(id = id), workoutId = workoutId).onFailure { e ->
                    AppLog.e("WorkoutViewModel", "Falha ao sincronizar exercício $id no caminho aninhado: ${e.message}", e)
                    showToast("Erro ao sincronizar exercício na nuvem.")
                }
            }

            syncController.enqueueChange("Treino", "Ficha '$title' (${exercises.size} exercícios)")
            showToast("Ficha '$title' salva com sucesso!")
        }
    }

    fun startActiveWorkout(routine: WorkoutRoutine, exercises: List<WorkoutExercise>) {
        shared.workoutTimerJob?.cancel()
        shared.restTimerJob?.cancel()

        shared.updateActiveWorkout(
            ActiveWorkoutSession(
                routine = routine,
                exercises = exercises,
                isRunning = true,
                elapsedSeconds = 0,
                currentExerciseIndex = 0,
                completedSetsCount = 0
            )
        )

        shared.workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = activeWorkout.value
                shared.updateActiveWorkout(
                    current.copy(elapsedSeconds = current.elapsedSeconds + 1)
                )
            }
        }
    }

    fun logSetAndStartRest(exercise: WorkoutExercise, reps: Int, weightKg: Float) {
        val current = activeWorkout.value
        val exId = exercise.id
        val list = (current.loggedExerciseSets[exId] ?: emptyList()) + Pair(reps, weightKg)
        val updatedMap = current.loggedExerciseSets + (exId to list)
        val duration = exercise.restSeconds.coerceAtLeast(30)

        WorkoutAudioHapticHelper.playRestStarted(
            getApplication(),
            current.isWorkoutSoundEnabled,
            current.isWorkoutHapticEnabled
        )

        shared.updateActiveWorkout(
            current.copy(
                completedSetsCount = current.completedSetsCount + 1,
                loggedExerciseSets = updatedMap,
                restTimerRemainingSeconds = duration,
                restTimerTotalSeconds = duration,
                isRestTimerRunning = true,
                isRestTimerFinished = false
            )
        )
        shared.restTimerJob?.cancel()
        shared.restTimerJob = viewModelScope.launch {
            var remaining = duration
            while (remaining > 0) {
                delay(1000)
                remaining--
                val updatedSession = activeWorkout.value
                if (!updatedSession.isRestTimerRunning && updatedSession.restTimerRemainingSeconds == remaining) {
                    break
                }
                shared.updateActiveWorkout(
                    updatedSession.copy(restTimerRemainingSeconds = remaining)
                )
                if (remaining in 1..3) {
                    WorkoutAudioHapticHelper.playCountdownTick(
                        getApplication(),
                        updatedSession.isWorkoutSoundEnabled,
                        updatedSession.isWorkoutHapticEnabled
                    )
                }
            }
            val finishedSession = activeWorkout.value
            shared.updateActiveWorkout(
                finishedSession.copy(
                    isRestTimerRunning = false,
                    isRestTimerFinished = true
                )
            )
            WorkoutAudioHapticHelper.playRestFinished(
                getApplication(),
                activeWorkout.value.isWorkoutSoundEnabled,
                activeWorkout.value.isWorkoutHapticEnabled
            )
        }
    }

    fun toggleWorkoutSound() {
        val current = activeWorkout.value
        val newVal = !current.isWorkoutSoundEnabled
        shared.updateActiveWorkout(current.copy(isWorkoutSoundEnabled = newVal))
        showToast(if (newVal) "Sons de treino ativados 🔊" else "Sons de treino silenciados 🔇")
    }

    fun toggleWorkoutHaptic() {
        val current = activeWorkout.value
        val newVal = !current.isWorkoutHapticEnabled
        shared.updateActiveWorkout(current.copy(isWorkoutHapticEnabled = newVal))
        showToast(if (newVal) "Vibrações táteis ativadas 📳" else "Vibrações táteis desativadas")
    }

    fun cancelRestTimer() {
        shared.restTimerJob?.cancel()
        shared.updateActiveWorkout(
            activeWorkout.value.copy(
                isRestTimerRunning = false,
                isRestTimerFinished = false,
                restTimerRemainingSeconds = 0
            )
        )
    }

    fun toggleSetLog(exercise: WorkoutExercise, setIndex: Int) {
        logSetAndStartRest(exercise, exercise.repsMin, exercise.currentWeightKg)
    }

    fun updateLoggedSetDetails(exerciseId: Long, setIndex: Int, reps: Int, weightKg: Float) {
        val current = activeWorkout.value
        val list = current.loggedExerciseSets[exerciseId]?.toMutableList() ?: return
        val zeroBasedIdx = setIndex - 1
        if (zeroBasedIdx in list.indices) {
            list[zeroBasedIdx] = Pair(reps.coerceAtLeast(1), weightKg.coerceAtLeast(0f))
            val updatedMap = current.loggedExerciseSets + (exerciseId to list.toList())
            shared.updateActiveWorkout(current.copy(loggedExerciseSets = updatedMap))
            showToast("Série $setIndex ajustada: $reps reps @ ${weightKg.toInt()} kg")
        }
    }

    fun removeLoggedSet(exerciseId: Long, setIndex: Int) {
        val current = activeWorkout.value
        val list = current.loggedExerciseSets[exerciseId]?.toMutableList() ?: return
        val zeroBasedIdx = setIndex - 1
        if (zeroBasedIdx in list.indices) {
            list.removeAt(zeroBasedIdx)
            val updatedMap = current.loggedExerciseSets + (exerciseId to list.toList())
            shared.updateActiveWorkout(
                current.copy(
                    completedSetsCount = (current.completedSetsCount - 1).coerceAtLeast(0),
                    loggedExerciseSets = updatedMap
                )
            )
            showToast("Série $setIndex desmarcada")
        }
    }

    fun addExerciseToRoutineOrSession(
        routine: WorkoutRoutine,
        newExercise: WorkoutExercise,
        savePermanently: Boolean
    ) {
        viewModelScope.launch {
            val currentActive = activeWorkout.value
            val isThisRoutineRunning = currentActive.isRunning && currentActive.routine?.id == routine.id
            val isFreeWorkout = routine.id == 99999L

            if (savePermanently && !isFreeWorkout) {
                val nextIndex = allExercises.value.count { it.routineId == routine.id }
                val exToInsert = newExercise.copy(
                    id = 0L,
                    routineId = routine.id,
                    orderIndex = nextIndex
                )
                val insertedId = repository.addWorkoutExercise(exToInsert)
                val persistedEx = exToInsert.copy(id = insertedId)
                firestoreSync.syncWorkoutExerciseToCloud(persistedEx, workoutId = routine.id.toString())
                syncController.enqueueChange("Treino", "Novo exercício: ${newExercise.exerciseName} (${routine.letter})")

                if (isThisRoutineRunning) {
                    shared.updateActiveWorkout(
                        currentActive.copy(exercises = currentActive.exercises + persistedEx)
                    )
                    showToast("${newExercise.exerciseName} salvo na ficha e incluído no treino de hoje! 💪")
                } else {
                    showToast("${newExercise.exerciseName} adicionado na ficha ${routine.letter}! 💪")
                }
            } else {
                val tempSessionId = -(System.currentTimeMillis() % 1_000_000_000L).coerceAtLeast(1L)
                val sessionEx = newExercise.copy(
                    id = tempSessionId,
                    routineId = routine.id,
                    orderIndex = currentActive.exercises.size
                )
                if (isThisRoutineRunning) {
                    shared.updateActiveWorkout(
                        currentActive.copy(exercises = currentActive.exercises + sessionEx)
                    )
                } else {
                    val baseExercises = if (isFreeWorkout) emptyList() else allExercises.value.filter { it.routineId == routine.id }
                    startActiveWorkout(routine, baseExercises + sessionEx)
                }
                showToast("${newExercise.exerciseName} incluído no treino de hoje! 🔥")
            }
        }
    }

    fun substituteExerciseInRoutineOrSession(
        routine: WorkoutRoutine,
        currentExercise: WorkoutExercise,
        newGuide: ExerciseVisualGuide,
        savePermanently: Boolean
    ) {
        viewModelScope.launch {
            val currentActive = activeWorkout.value
            val isThisRoutineRunning = currentActive.isRunning && currentActive.routine?.id == routine.id
            val isFreeWorkout = routine.id == 99999L

            val replaced = currentExercise.copy(
                exerciseName = newGuide.exerciseName,
                muscleGroup = newGuide.muscleGroup,
                biomechanicsGuide = newGuide.postureTip,
                gifDemonstrationUrl = newGuide.gifUrl
            )

            if (savePermanently && !isFreeWorkout && currentExercise.id > 0L) {
                repository.updateWorkoutExercise(replaced)
                firestoreSync.syncWorkoutExerciseToCloud(replaced, workoutId = routine.id.toString())
                syncController.enqueueChange("Treino", "Substituição: ${newGuide.exerciseName}")
            }

            if (isThisRoutineRunning) {
                val updatedList = currentActive.exercises.map { ex ->
                    if (ex.id == currentExercise.id) replaced else ex
                }
                shared.updateActiveWorkout(currentActive.copy(exercises = updatedList))
            }

            showToast(
                if (savePermanently && !isFreeWorkout) {
                    "Trocado para ${newGuide.exerciseName} na ficha! 🔄"
                } else {
                    "Trocado para ${newGuide.exerciseName} no treino de hoje! ⚡"
                }
            )
        }
    }

    fun updateExerciseWeight(exercise: WorkoutExercise, newWeight: Float) {
        viewModelScope.launch {
            if (exercise.id > 0L) {
                repository.updateWorkoutExercise(exercise.copy(currentWeightKg = newWeight))
                syncController.enqueueChange("Treino", "Carga ${exercise.exerciseName}: ${newWeight.toInt()} kg")
            }
            val currentActive = activeWorkout.value
            if (currentActive.isRunning) {
                val updatedExercises = currentActive.exercises.map { ex ->
                    if (ex.id == exercise.id) ex.copy(currentWeightKg = newWeight) else ex
                }
                shared.updateActiveWorkout(currentActive.copy(exercises = updatedExercises))
            }
        }
    }

    fun addSetToExercise(exercise: WorkoutExercise) {
        val newSets = exercise.sets + 1
        viewModelScope.launch {
            if (exercise.id > 0L) {
                repository.updateWorkoutExercise(exercise.copy(sets = newSets))
                syncController.enqueueChange("Treino", "${exercise.exerciseName}: $newSets séries")
            }
            val currentActive = activeWorkout.value
            WorkoutAudioHapticHelper.playSetLoggedTick(getApplication(), currentActive.isWorkoutHapticEnabled)
            if (currentActive.isRunning) {
                val updatedExercises = currentActive.exercises.map { ex ->
                    if (ex.id == exercise.id) ex.copy(sets = newSets) else ex
                }
                shared.updateActiveWorkout(currentActive.copy(exercises = updatedExercises))
            }
            showToast("+1 série em ${exercise.exerciseName}! Total: $newSets séries 💪")
        }
    }

    fun removeSetFromExercise(exercise: WorkoutExercise) {
        if (exercise.sets <= 1) return
        val newSets = exercise.sets - 1
        viewModelScope.launch {
            if (exercise.id > 0L) {
                repository.updateWorkoutExercise(exercise.copy(sets = newSets))
                syncController.enqueueChange("Treino", "${exercise.exerciseName}: $newSets séries")
            }
            val currentActive = activeWorkout.value
            if (currentActive.isRunning) {
                val updatedExercises = currentActive.exercises.map { ex ->
                    if (ex.id == exercise.id) ex.copy(sets = newSets) else ex
                }
                shared.updateActiveWorkout(currentActive.copy(exercises = updatedExercises))
            }
            showToast("Série removida de ${exercise.exerciseName}. Total: $newSets séries")
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        val current = activeWorkout.value
        val newRemaining = (current.restTimerRemainingSeconds + deltaSeconds).coerceAtLeast(0)
        shared.updateActiveWorkout(
            current.copy(
                restTimerRemainingSeconds = newRemaining,
                isRestTimerRunning = newRemaining > 0
            )
        )
    }

    fun startFreeWorkout() {
        val freeRoutine = WorkoutRoutine(
            id = 99999L,
            letter = "L",
            title = "Treino Livre",
            dayOfWeekLabel = "Hoje",
            targetMuscleGroups = "Livre • Escolha seus exercícios na hora",
            estimatedDurationMinutes = 60
        )
        startActiveWorkout(freeRoutine, emptyList())
    }

    fun finishActiveWorkout(rpe: Int, notes: String) {
        val active = activeWorkout.value
        if (!active.isRunning) return
        shared.workoutTimerJob?.cancel()
        shared.restTimerJob?.cancel()
        viewModelScope.launch {
            val totalVolume = active.loggedExerciseSets.values.sumOf { setList ->
                setList.sumOf { (reps, weight) -> (reps * weight).toDouble() }
            }.toFloat()

            val now = System.currentTimeMillis()
            val durationMin = (active.elapsedSeconds / 60).coerceAtLeast(1)
            val routineId = active.routine?.id ?: 99999L
            val routineName = active.routine?.title ?: "Treino Livre"

            val logId = repository.logCompletedWorkout(
                routineId = routineId,
                routineName = routineName,
                durationMinutes = durationMin,
                totalVolumeKg = totalVolume,
                totalSetsCompleted = active.completedSetsCount,
                perceivedExertion = rpe,
                notes = notes,
                dateMillis = now
            )

            val uid = userProfile.value.firebaseUid
            if (uid.isNotBlank() && !uid.startsWith("guest_")) {
                firestoreSync.syncWorkoutLogToCloud(
                    WorkoutLog(
                        id = logId,
                        routineId = routineId,
                        routineName = routineName,
                        dateMillis = now,
                        durationMinutes = durationMin,
                        totalVolumeKg = totalVolume,
                        totalSetsCompleted = active.completedSetsCount,
                        perceivedExertion = rpe,
                        notes = notes
                    ),
                    uid
                )
            }

            shared.updateActiveWorkout(ActiveWorkoutSession())
            syncController.enqueueChange("Treino", "Treino concluído: $routineName (${totalVolume.toInt()} kg)")
            showToast("Treino concluído! Mais um pra conta 🏆💪")
        }
    }

    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(log.id)
            showToast("Registro de treino removido")
        }
    }

    fun deleteWorkoutLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(logId)
            showToast("Registro de treino removido")
        }
    }

    fun formatRoutineForSharing(routine: WorkoutRoutine): String {
        val exercises = allExercises.value.filter { it.routineId == routine.id }
        val sb = StringBuilder()
        sb.append("🏋️ *Ficha de Treino VIRTUS*\n")
        sb.append("📋 *${routine.letter} - ${routine.title}*\n")
        if (routine.targetMuscleGroups.isNotBlank()) sb.append("🎯 ${routine.targetMuscleGroups}\n")
        sb.append("\n*Exercícios:*\n")
        exercises.forEachIndexed { i, ex ->
            sb.append("${i + 1}. *${ex.exerciseName}* - ${ex.sets}x${ex.repsMin}-${ex.repsMax} (${ex.currentWeightKg.toInt()}kg)\n")
            if (ex.biomechanicsGuide.isNotBlank()) sb.append("   💡 ${ex.biomechanicsGuide}\n")
        }
        sb.append("\n_Bons treinos! Gerado pelo VIRTUS Fit App_")
        return sb.toString()
    }

    fun addProgressLog(
        weightKg: Float,
        bodyFatPercentage: Float,
        chestCm: Float = 0f,
        waistCm: Float = 0f,
        armRightCm: Float = 0f,
        thighCm: Float = 0f,
        notes: String = "",
        photoUri: String = ""
    ) {
        viewModelScope.launch {
            val dateMillis = System.currentTimeMillis()
            val dateDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMillis))
            val log = ProgressLog(
                dateMillis = dateMillis,
                dateDisplay = dateDisplay,
                weightKg = weightKg,
                bodyFatPercentage = bodyFatPercentage,
                chestCm = chestCm,
                waistCm = waistCm,
                armLeftCm = armRightCm,
                armRightCm = armRightCm,
                thighCm = thighCm,
                notes = notes,
                photoUri = photoUri
            )
            repository.addProgressLog(log)
            showToast("Evolução salva com sucesso!")
        }
    }

    fun deleteProgressLog(id: Long) {
        viewModelScope.launch {
            repository.deleteProgressLog(id)
            showToast("Registro removido")
        }
    }

    fun getMuscleVolume(days: Int): Map<String, Float> {
        val now = System.currentTimeMillis()
        val limit = if (days <= 0) 0L else now - (days.toLong() * 86400000L)

        val logsInPeriod = workoutLogs.value.filter { it.dateMillis >= limit }
        val loggedVolumes = logsInPeriod
            .mapNotNull { log ->
                val routine = workoutRoutines.value.find {
                    it.id == log.routineId ||
                        it.title.equals(log.routineName, ignoreCase = true) ||
                        log.routineName.contains(it.title, ignoreCase = true)
                }
                val musclesFromRoutine = routine?.targetMuscleGroups
                    ?.split(",", "•", "/", "e", "&")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    .orEmpty()

                val routineExercises = allExercises.value.filter { it.routineId == (routine?.id ?: log.routineId) }
                val musclesFromExercises = routineExercises
                    .map { it.muscleGroup.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()

                val targetMuscles = when {
                    musclesFromExercises.isNotEmpty() -> musclesFromExercises
                    musclesFromRoutine.isNotEmpty() -> musclesFromRoutine
                    else -> emptyList()
                }

                if (targetMuscles.isEmpty()) {
                    null
                } else {
                    val effectiveVolume = if (log.totalVolumeKg > 0f) log.totalVolumeKg else 1200f
                    val volPerMuscle = effectiveVolume / targetMuscles.size
                    targetMuscles.associateWith { volPerMuscle }
                }
            }
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, volumes) -> volumes.sum() }

        if (loggedVolumes.isNotEmpty()) {
            return loggedVolumes
        }

        return allExercises.value
            .filter { it.muscleGroup.isNotBlank() }
            .groupBy { it.muscleGroup.trim() }
            .mapValues { (_, exercises) ->
                exercises.sumOf { ex ->
                    val avgReps = ((ex.repsMin + ex.repsMax) / 2f).coerceAtLeast(8f)
                    val weight = ex.currentWeightKg.coerceAtLeast(10f)
                    (ex.sets.coerceAtLeast(1) * avgReps * weight).toDouble()
                }.toFloat()
            }
    }
}
