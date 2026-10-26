package com.example.ui.screens.workout

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.ui.theme.GrowthYellow
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseVisualGuide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    viewModel: VirtusViewModel,
    onNavigateToProgression: () -> Unit = {}
) {
    val context = LocalContext.current
    val allRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selectedDateMillis))
    val selectedWorkoutLogs = workoutLogs.filter {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(it.dateMillis)) == selectedDateKey
    }



    // Screen State: null = Routine List Screen; non-null = Viewing/Executing specific Routine Detail Screen
    var viewingRoutineDetail by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var showRoutineEditorDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var routineToDelete by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var presetLetterForNewRoutine by remember { mutableStateOf("A") }
    var presetSplitForNewRoutine by remember { mutableStateOf(WorkoutSplitType.CUSTOM) }

    var selectedExerciseGuide by remember { mutableStateOf<ExerciseVisualGuide?>(null) }
    var exerciseToSubstitute by remember { mutableStateOf<WorkoutExercise?>(null) }
    var showSingleExerciseAddDialog by remember { mutableStateOf(false) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showExerciseProgressionModal by remember { mutableStateOf(false) }

    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Handle system back button when inside a routine detail view
    BackHandler(enabled = viewingRoutineDetail != null) {
        viewingRoutineDetail = null
    }

    if (viewingRoutineDetail != null) {
        // --- VIEW 2: DEDICATED ROUTINE DETAIL & ACTIVE EXERCISES VIEW ---
        val currentRoutine = viewingRoutineDetail!!
        val routineExercises = allExercises.filter { it.routineId == currentRoutine.id }

        RoutineDetailView(
            routine = currentRoutine,
            exercises = routineExercises,
            activeWorkout = activeWorkout,
            gifImageLoader = gifImageLoader,
            onBackClick = { viewingRoutineDetail = null },
            onStartWorkout = {
                viewModel.startActiveWorkout(currentRoutine, routineExercises)
            },
            onFinishWorkout = { showFinishWorkoutDialog = true },
            onSkipRest = { viewModel.cancelRestTimer() },
            onToggleSet = { exercise, setIndex ->
                viewModel.toggleSetLog(exercise, setIndex)
            },
            onUpdateWeight = { exercise, newWeight ->
                viewModel.updateExerciseWeight(exercise, newWeight)
            },
            onOpenGuide = { guide -> selectedExerciseGuide = guide },
            onSubstituteExercise = { exercise -> exerciseToSubstitute = exercise },
            onAddExerciseClick = { showSingleExerciseAddDialog = true },
            onAdjustRest = { delta -> viewModel.adjustRestTimer(delta) }
        )
    } else {
        // --- VIEW 1: FICHAS DE TREINO LIST & DIVISIONS ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonDark),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Style
            item {
                GrowthHeader(
                    title = "Fichas de Treino",
                    subtitle = "Dia ativo: ${SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(selectedDateMillis))}",
                    badgeText = "FICHAS",
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { showHistoryDialog = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CarbonCardElevated)
                                    .testTag("workout_history_btn")
                            ) {
                                Icon(Icons.Default.History, contentDescription = "Histórico", tint = GrowthCyan)
                            }
                            IconButton(
                                onClick = {
                                    routineToEdit = null
                                    presetLetterForNewRoutine = "A"
                                    presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                                    showRoutineEditorDialog = true
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GrowthOrange.copy(alpha = 0.2f))
                                    .testTag("create_routine_icon_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Nova Ficha", tint = GrowthOrange)
                            }
                        }
                    }
                )
            }

            // Active Session Quick Banner (if session is currently running)
            if (activeWorkout.isRunning) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(2.dp, GrowthGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable {
                                activeWorkout.routine?.let { viewingRoutineDetail = it }
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("SESSÃO ATIVA EM ANDAMENTO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                    Text(
                                        activeWorkout.routine?.title ?: "Treino Livre",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    activeWorkout.routine?.let { viewingRoutineDetail = it }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Abrir", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            // Action Toolbar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            routineToEdit = null
                            presetLetterForNewRoutine = "A"
                            presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                            showRoutineEditorDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_routine_action_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova Ficha", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    Button(
                        onClick = {
                            viewModel.startFreeWorkout()
                            viewingRoutineDetail = activeWorkout.routine ?: WorkoutRoutine(
                                id = 99999L,
                                letter = "L",
                                title = "Treino Livre"
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("free_workout_action_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Treino Livre", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }

                    OutlinedButton(
                        onClick = { showHistoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan),
                        border = BorderStroke(1.dp, CarbonBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Histórico", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onNavigateToProgression,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthOrange),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_progression_chart_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = GrowthOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Visualizar Progressão de Carga dos Exercícios", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            
// ROUTINE CARDS / SLOTS LIST

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    if (allRoutines.isEmpty()) {
                        item {
                            EmptyWorkoutGroupCard(
                                title = "Nenhuma Ficha Cadastrada",
                                subtitle = "Crie fichas em branco personalizadas ou inicie com a divisão recomendada ABCDE completa.",
                                onCreateClick = {
                                    routineToEdit = null
                                    presetLetterForNewRoutine = "A"
                                    presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                                    showRoutineEditorDialog = true
                                },
                                onRestoreDefaultClick = {
                                    viewModel.restoreDefaultRoutines()
                                }
                            )
                        }
                    } else {
                        items(allRoutines) { routine ->
                            val exercisesForRoutine = allExercises.filter { it.routineId == routine.id }
                            RoutineCardItem(
                                routine = routine,
                                exerciseCount = exercisesForRoutine.size,
                                onOpenRoutine = {
                                    viewingRoutineDetail = routine
                                },
                                onStartClick = {
                                    viewModel.startActiveWorkout(routine, exercisesForRoutine)
                                    viewingRoutineDetail = routine
                                },
                                onEditClick = {
                                    routineToEdit = routine
                                    showRoutineEditorDialog = true
                                },
                                onShareClick = {
                                    val text = viewModel.formatRoutineForSharing(routine)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Ficha"))
                                },
                                onDeleteClick = { routineToDelete = routine }
                            )
                        }
                    }
}
// 1. Routine Editor Dialog
    if (showRoutineEditorDialog) {
        WorkoutRoutineEditorDialog(
            existingRoutine = routineToEdit,
            existingExercises = if (routineToEdit != null) allExercises.filter { it.routineId == routineToEdit!!.id } else emptyList(),
            initialSplit = presetSplitForNewRoutine,
            onDismiss = { showRoutineEditorDialog = false },
            onSave = { existingId, splitType, letter, title, dayOfWeek, muscles, durationMin, exercises ->
                viewModel.saveFullRoutineWithExercises(
                    existingRoutineId = existingId,
                    splitType = splitType,
                    letter = letter.ifBlank { presetLetterForNewRoutine },
                    title = title,
                    dayOfWeekLabel = dayOfWeek,
                    targetMuscleGroups = muscles,
                    estimatedDurationMinutes = durationMin,
                    exercises = exercises
                )
                showRoutineEditorDialog = false
            }
        )
    }

    // 2. Biomechanics Guide & GIF Modal
    if (selectedExerciseGuide != null) {
        ExerciseBiomechanicsGuideModal(
            guide = selectedExerciseGuide!!,
            gifImageLoader = gifImageLoader,
            onDismiss = { selectedExerciseGuide = null }
        )
    }

    // 3. Add Single Exercise to Current Routine
    if (showSingleExerciseAddDialog && viewingRoutineDetail != null) {
        AddExerciseDialog(
            routine = viewingRoutineDetail,
            onDismiss = { showSingleExerciseAddDialog = false },
            onConfirm = { name, group, sets, rMin, rMax, weight, rest, notes ->
                val guide = ExerciseGifMapper.getGuideForExercise(name)
                val newEx = WorkoutExercise(
                    routineId = viewingRoutineDetail!!.id,
                    exerciseName = name,
                    muscleGroup = group,
                    sets = sets,
                    repsMin = rMin,
                    repsMax = rMax,
                    currentWeightKg = weight,
                    restSeconds = rest,
                    notes = notes,
                    biomechanicsGuide = guide.postureTip,
                    gifDemonstrationUrl = guide.gifUrl
                )
                val currentList = allExercises.filter { it.routineId == viewingRoutineDetail!!.id }
                viewModel.saveFullRoutineWithExercises(
                    existingRoutineId = viewingRoutineDetail!!.id,
                    splitType = viewingRoutineDetail!!.splitType,
                    letter = viewingRoutineDetail!!.letter,
                    title = viewingRoutineDetail!!.title,
                    dayOfWeekLabel = viewingRoutineDetail!!.dayOfWeekLabel,
                    targetMuscleGroups = viewingRoutineDetail!!.targetMuscleGroups,
                    estimatedDurationMinutes = viewingRoutineDetail!!.estimatedDurationMinutes,
                    exercises = currentList + newEx
                )
                showSingleExerciseAddDialog = false
            }
        )
    }

    // 4. Finish Workout Modal
    if (showFinishWorkoutDialog) {
        FinishWorkoutModal(
            activeWorkout = activeWorkout,
            onDismiss = { showFinishWorkoutDialog = false },
            onConfirmFinish = { rpe, notes ->
                viewModel.finishActiveWorkout(rpe, notes)
                showFinishWorkoutDialog = false
                viewingRoutineDetail = null
            }
        )
    }

    // 5. Workout History Modal
    if (showHistoryDialog) {
        WorkoutHistoryModal(
            workoutLogs = selectedWorkoutLogs,
            onDismiss = { showHistoryDialog = false },
            onDeleteLog = { viewModel.deleteWorkoutLog(it) }
        )
    }

    // Exercise Progression Chart Modal
    if (showExerciseProgressionModal) {
        ExerciseProgressionModal(
            exercises = allExercises,
            workoutLogs = workoutLogs,
            onDismiss = { showExerciseProgressionModal = false }
        )
    }

    // 6. Delete Confirmation Dialog
    if (routineToDelete != null) {
        AlertDialog(
            onDismissRequest = { routineToDelete = null },
            containerColor = CarbonCard,
            title = { Text("Excluir Ficha de Treino?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text = { Text("Tem certeza que deseja excluir a ficha '${routineToDelete?.title}'? Esta ação não pode ser desfeita.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        routineToDelete?.let { viewModel.deleteWorkoutRoutine(it.id) }
                        if (viewingRoutineDetail?.id == routineToDelete?.id) {
                            viewingRoutineDetail = null
                        }
                        routineToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthRed)
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { routineToDelete = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // 7. Substitute Exercise Dialog (1-tap swap machine/exercise)
    if (exerciseToSubstitute != null && viewingRoutineDetail != null) {
        SubstituteExerciseDialog(
            currentExercise = exerciseToSubstitute!!,
            gifImageLoader = gifImageLoader,
            onDismiss = { exerciseToSubstitute = null },
            onSelectSubstitute = { newGuide ->
                val currentEx = exerciseToSubstitute!!
                val currentList = allExercises.filter { it.routineId == viewingRoutineDetail!!.id }
                val updatedList = currentList.map { ex ->
                    if (ex.id == currentEx.id) {
                        ex.copy(
                            exerciseName = newGuide.exerciseName,
                            muscleGroup = newGuide.muscleGroup,
                            biomechanicsGuide = newGuide.postureTip,
                            gifDemonstrationUrl = newGuide.gifUrl
                        )
                    } else {
                        ex
                    }
                }
                viewModel.saveFullRoutineWithExercises(
                    existingRoutineId = viewingRoutineDetail!!.id,
                    splitType = viewingRoutineDetail!!.splitType,
                    letter = viewingRoutineDetail!!.letter,
                    title = viewingRoutineDetail!!.title,
                    dayOfWeekLabel = viewingRoutineDetail!!.dayOfWeekLabel,
                    targetMuscleGroups = viewingRoutineDetail!!.targetMuscleGroups,
                    estimatedDurationMinutes = viewingRoutineDetail!!.estimatedDurationMinutes,
                    exercises = updatedList
                )
                exerciseToSubstitute = null
            }
        )
    }
}}
