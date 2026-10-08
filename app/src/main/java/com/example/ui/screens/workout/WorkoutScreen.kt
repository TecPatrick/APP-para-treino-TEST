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
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
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
import com.example.ui.GrowthViewModel
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

enum class WorkoutDivisionFilter(val label: String, val splitType: WorkoutSplitType?, val expectedLetters: List<String>) {
    ALL("Todas As Fichas", null, emptyList()),
    AB("Divisão AB", WorkoutSplitType.AB, listOf("A", "B")),
    ABC("Divisão ABC", WorkoutSplitType.ABC, listOf("A", "B", "C")),
    ABCDE("Divisão ABCDE", WorkoutSplitType.ABCDE, listOf("A", "B", "C", "D", "E")),
    CUSTOM("Personalizadas", WorkoutSplitType.CUSTOM, emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: GrowthViewModel) {
    val context = LocalContext.current
    val allRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(WorkoutDivisionFilter.ALL) }

    // Screen State: null = Routine List Screen; non-null = Viewing/Executing specific Routine Detail Screen
    var viewingRoutineDetail by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var showRoutineEditorDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var routineToDelete by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var presetLetterForNewRoutine by remember { mutableStateOf("A") }
    var presetSplitForNewRoutine by remember { mutableStateOf(WorkoutSplitType.CUSTOM) }

    var selectedExerciseGuide by remember { mutableStateOf<ExerciseVisualGuide?>(null) }
    var showSingleExerciseAddDialog by remember { mutableStateOf(false) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }

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
            onAddExerciseClick = { showSingleExerciseAddDialog = true }
        )
    } else {
        // --- VIEW 1: FICHAS DE TREINO LIST & DIVISIONS ---
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonDark),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header Growth Style
            item {
                GrowthHeader(
                    title = "Fichas de Treino",
                    subtitle = "Sua rotina personalizada de treinos",
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen)
                            ) {
                                Text("Abrir Ficha", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Action Toolbar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            .weight(1f)
                            .testTag("add_routine_action_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nova Ficha", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
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
                            .weight(1f)
                            .testTag("free_workout_action_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Treino Livre", fontWeight = FontWeight.ExtraBold, color = Color.Black, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showHistoryDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan),
                        border = BorderStroke(1.dp, CarbonBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Histórico", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Division Filter Selector (Todas, AB, ABC, ABCDE, Personalizadas)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Text(
                        text = "DIVISÕES DE TREINO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GrowthOrangeLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(WorkoutDivisionFilter.values()) { filter ->
                            val isSelected = selectedFilter == filter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        filter.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = CarbonCard,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = CarbonBorder,
                                    selectedBorderColor = GrowthOrange
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                        }
                    }
                }
            }

            // ROUTINE CARDS / SLOTS LIST
            when (selectedFilter) {
                WorkoutDivisionFilter.ALL -> {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    if (allRoutines.isEmpty()) {
                        item {
                            EmptyWorkoutGroupCard(
                                title = "Nenhuma Ficha Cadastrada",
                                subtitle = "Crie suas fichas de treino para visualizar os exercícios.",
                                onCreateClick = {
                                    routineToEdit = null
                                    presetLetterForNewRoutine = "A"
                                    presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                                    showRoutineEditorDialog = true
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
                                onDeleteClick = { routineToDelete = routine }
                            )
                        }
                    }
                }

                WorkoutDivisionFilter.AB, WorkoutDivisionFilter.ABC, WorkoutDivisionFilter.ABCDE -> {
                    val expectedType = selectedFilter.splitType!!
                    val expectedLetters = selectedFilter.expectedLetters
                    val routinesForFilter = allRoutines.filter { it.splitType == expectedType }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "GRUPO ${selectedFilter.label.uppercase()}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthCyan,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    expectedLetters.forEach { letter ->
                        val existingForLetter = routinesForFilter.find { it.letter.equals(letter, ignoreCase = true) }
                        if (existingForLetter != null) {
                            item(key = "routine_${existingForLetter.id}") {
                                val exercisesForRoutine = allExercises.filter { it.routineId == existingForLetter.id }
                                RoutineCardItem(
                                    routine = existingForLetter,
                                    exerciseCount = exercisesForRoutine.size,
                                    onOpenRoutine = { viewingRoutineDetail = existingForLetter },
                                    onStartClick = {
                                        viewModel.startActiveWorkout(existingForLetter, exercisesForRoutine)
                                        viewingRoutineDetail = existingForLetter
                                    },
                                    onEditClick = {
                                        routineToEdit = existingForLetter
                                        showRoutineEditorDialog = true
                                    },
                                    onDeleteClick = { routineToDelete = existingForLetter }
                                )
                            }
                        } else {
                            item(key = "empty_slot_$letter") {
                                EmptySlotRoutineCard(
                                    letter = letter,
                                    divisionName = selectedFilter.label,
                                    onCreateClick = {
                                        routineToEdit = null
                                        presetLetterForNewRoutine = letter
                                        presetSplitForNewRoutine = expectedType
                                        showRoutineEditorDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                WorkoutDivisionFilter.CUSTOM -> {
                    val customRoutines = allRoutines.filter { it.splitType == WorkoutSplitType.CUSTOM }
                    if (customRoutines.isEmpty()) {
                        item {
                            EmptyWorkoutGroupCard(
                                title = "Nenhum Treino Personalizado",
                                subtitle = "Crie fichas customizadas de acordo com suas preferências.",
                                onCreateClick = {
                                    routineToEdit = null
                                    presetLetterForNewRoutine = "A"
                                    presetSplitForNewRoutine = WorkoutSplitType.CUSTOM
                                    showRoutineEditorDialog = true
                                }
                            )
                        }
                    } else {
                        items(customRoutines) { routine ->
                            val exercisesForRoutine = allExercises.filter { it.routineId == routine.id }
                            RoutineCardItem(
                                routine = routine,
                                exerciseCount = exercisesForRoutine.size,
                                onOpenRoutine = { viewingRoutineDetail = routine },
                                onStartClick = {
                                    viewModel.startActiveWorkout(routine, exercisesForRoutine)
                                    viewingRoutineDetail = routine
                                },
                                onEditClick = {
                                    routineToEdit = routine
                                    showRoutineEditorDialog = true
                                },
                                onDeleteClick = { routineToDelete = routine }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- MODAL DIALOGS ---

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
            workoutLogs = workoutLogs,
            onDismiss = { showHistoryDialog = false },
            onDeleteLog = { viewModel.deleteWorkoutLog(it) }
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
}

// --- SCREEN VIEW 2: DEDICATED ROUTINE DETAIL & EXERCISES EXECUTION VIEW ---
@Composable
fun RoutineDetailView(
    routine: WorkoutRoutine,
    exercises: List<WorkoutExercise>,
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    gifImageLoader: ImageLoader,
    onBackClick: () -> Unit,
    onStartWorkout: () -> Unit,
    onFinishWorkout: () -> Unit,
    onSkipRest: () -> Unit,
    onToggleSet: (exercise: WorkoutExercise, setIndex: Int) -> Unit,
    onUpdateWeight: (exercise: WorkoutExercise, newWeight: Float) -> Unit,
    onOpenGuide: (ExerciseVisualGuide) -> Unit,
    onAddExerciseClick: () -> Unit
) {
    val isSessionRunningForThisRoutine = activeWorkout.isRunning && activeWorkout.routine?.id == routine.id

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Action Header inside Routine Detail View
        item {
            Surface(
                color = CarbonCard,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = MaterialTheme.colorScheme.onBackground)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            color = GrowthOrange,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(routine.letter, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(routine.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${routine.dayOfWeekLabel} • ${exercises.size} Exercícios", fontSize = 11.sp, color = GrowthOrangeLight)
                        }
                    }

                    Button(
                        onClick = onAddExerciseClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Exercício", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Active Session Status & Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, if (isSessionRunningForThisRoutine) GrowthGreen else GrowthOrange),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (isSessionRunningForThisRoutine) {
                        val mins = activeWorkout.elapsedSeconds / 60
                        val secs = activeWorkout.elapsedSeconds % 60
                        val timeStr = String.format("%02d:%02d", mins, secs)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("TREINO EM ANDAMENTO", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = GrowthGreen)
                            }

                            Text("⏱️ $timeStr", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Rest Timer Banner if active
                        if (activeWorkout.isRestTimerRunning) {
                            Surface(
                                color = GrowthOrange.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, GrowthOrange)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⏸️ Descanso: ${activeWorkout.restTimerRemainingSeconds}s", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = GrowthOrange)
                                    TextButton(onClick = onSkipRest) {
                                        Text("Pular Descanso", fontSize = 11.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Button(
                            onClick = onFinishWorkout,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("FINALIZAR E SALVAR TREINO", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ficha Pronta para Iniciar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Marque as séries conforme executa no treino.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Button(
                                onClick = onStartWorkout,
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("INICIAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: EXERCÍCIOS DA FICHA
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "EXERCÍCIOS DA FICHA (${exercises.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = GrowthOrangeLight
                )
            }
        }

        if (exercises.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Esta ficha está vazia.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Adicione os exercícios que você deseja realizar neste treino.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddExerciseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adicionar Primeiro Exercício", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            itemsIndexed(exercises) { index, exercise ->
                val loggedForThisEx = activeWorkout.loggedExerciseSets[exercise.id] ?: emptyList()
                val guide = remember(exercise.exerciseName) { ExerciseGifMapper.getGuideForExercise(exercise.exerciseName) }

                ExerciseExecutionCard(
                    index = index + 1,
                    exercise = exercise,
                    guide = guide,
                    isWorkoutRunning = isSessionRunningForThisRoutine,
                    completedSetsInSession = loggedForThisEx.size,
                    onToggleSet = { setIndex -> onToggleSet(exercise, setIndex) },
                    onUpdateWeight = { newW -> onUpdateWeight(exercise, newW) },
                    onOpenGuide = { onOpenGuide(guide) }
                )
            }
        }
    }
}

// Interactive Exercise Card inside Routine Detail View
@Composable
fun ExerciseExecutionCard(
    index: Int,
    exercise: WorkoutExercise,
    guide: ExerciseVisualGuide,
    isWorkoutRunning: Boolean,
    completedSetsInSession: Int,
    onToggleSet: (setIndex: Int) -> Unit,
    onUpdateWeight: (Float) -> Unit,
    onOpenGuide: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Exercise Title and Muscle Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GrowthOrange.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$index", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = exercise.exerciseName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = exercise.muscleGroup,
                            fontSize = 11.sp,
                            color = GrowthOrangeLight
                        )
                    }
                }

                // Biomechanics & GIF guide trigger button
                Surface(
                    color = GrowthCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable { onOpenGuide() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡 Postura & GIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weight Adjuster Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonDark, shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${exercise.sets} séries • ${exercise.repsMin}-${exercise.repsMax} reps",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Carga: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(
                        onClick = { onUpdateWeight((exercise.currentWeightKg - 2.5f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir", tint = GrowthOrange, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${exercise.currentWeightKg.toInt()} kg",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrange
                    )
                    IconButton(
                        onClick = { onUpdateWeight(exercise.currentWeightKg + 2.5f) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar", tint = GrowthOrange, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Set Checkboxes / Badges
            Text("Marcação de Séries:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercise.sets) { setIdx ->
                    val isSetDone = (setIdx + 1) <= completedSetsInSession

                    Surface(
                        color = if (isSetDone) GrowthGreen.copy(alpha = 0.2f) else CarbonDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSetDone) GrowthGreen else CarbonBorder),
                        modifier = Modifier.clickable {
                            if (isWorkoutRunning) {
                                onToggleSet(setIdx + 1)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSetDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Série ${setIdx + 1}",
                                fontSize = 11.sp,
                                fontWeight = if (isSetDone) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSetDone) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// User Workout Routine Card Item (in Main Fichas List)
@Composable
fun RoutineCardItem(
    routine: WorkoutRoutine,
    exerciseCount: Int,
    onOpenRoutine: () -> Unit,
    onStartClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onOpenRoutine() }
            .testTag("routine_item_${routine.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GrowthOrange,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(routine.letter, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = routine.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${routine.dayOfWeekLabel} • $exerciseCount Exercícios",
                            fontSize = 11.sp,
                            color = GrowthOrangeLight
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = GrowthRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenRoutine,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                    border = BorderStroke(1.dp, CarbonBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Text("VER EXERCÍCIOS", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }

                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("INICIAR", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color.Black)
                }
            }
        }
    }
}

// Card for an empty slot letter in a division (e.g., Ficha A, Ficha B in AB or ABC split)
@Composable
fun EmptySlotRoutineCard(
    letter: String,
    divisionName: String,
    onCreateClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.3f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GrowthOrange),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(letter, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = GrowthOrange)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Treino $letter ($divisionName)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("Slot vazio • Adicione seus exercícios", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Criar Treino $letter", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// General empty card when no workout routines exist in group
@Composable
fun EmptyWorkoutGroupCard(
    title: String,
    subtitle: String,
    onCreateClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Criar Ficha de Treino", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Biomechanics & GIF Guide Modal
@Composable
fun ExerciseBiomechanicsGuideModal(
    guide: ExerciseVisualGuide,
    gifImageLoader: ImageLoader,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(guide.exerciseName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonDark)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(guide.gifUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = gifImageLoader,
                        contentDescription = guide.exerciseName,
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(32.dp))
                            }
                        },
                        error = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(guide.muscleGroup, fontSize = 12.sp, color = GrowthOrangeLight)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("💡 Execução & Postura Correta:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text(guide.postureTip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.fitnessProgrammerUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Guia Completo na Web", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

// Finish Workout Modal with RPE Rating
@Composable
fun FinishWorkoutModal(
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    onDismiss: () -> Unit,
    onConfirmFinish: (rpe: Int, notes: String) -> Unit
) {
    var rpeValue by remember { mutableFloatStateOf(7f) }
    var notesInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("🏆 Concluir Treino", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Duração: ${activeWorkout.elapsedSeconds / 60} minutos • ${activeWorkout.completedSetsCount} Séries Concluídas",
                    fontSize = 12.sp,
                    color = GrowthGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("RPE — Percepção de Esforço (1 a 10): ${rpeValue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                Slider(
                    value = rpeValue,
                    onValueChange = { rpeValue = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthOrange,
                        activeTrackColor = GrowthOrange,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Observações do Treino") },
                    placeholder = { Text("Ex: Aumentei carga no Supino, recuperei bem") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmFinish(rpeValue.toInt(), notesInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                    ) {
                        Text("Salvar Treino", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Workout History Log Viewer Modal
@Composable
fun WorkoutHistoryModal(
    workoutLogs: List<WorkoutLog>,
    onDismiss: () -> Unit,
    onDeleteLog: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Histórico de Treinos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (workoutLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum treino registrado no histórico ainda.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workoutLogs) { log ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(log.dateMillis))
                                        Text(log.routineName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text("$dateStr • ${log.durationMinutes} min • RPE ${log.perceivedExertion}", fontSize = 11.sp, color = GrowthGreen)
                                        if (log.notes.isNotBlank()) {
                                            Text(log.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteLog(log.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir Log", tint = GrowthRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modal for Adding Exercise with GIF Library Catalog Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    routine: WorkoutRoutine? = null,
    onDismiss: () -> Unit,
    onConfirm: ((name: String, group: String, sets: Int, rMin: Int, rMax: Int, weight: Float, rest: Int, notes: String) -> Unit)? = null,
    onAdd: ((name: String, group: String, sets: Int, reps: Int, weight: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Biblioteca com GIFs, 1 = Personalizado

    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleFilter by remember { mutableStateOf("Todos") }

    var customName by remember { mutableStateOf("") }
    var customGroup by remember { mutableStateOf("Peito") }
    var setsText by remember { mutableStateOf("4") }
    var repsText by remember { mutableStateOf("10") }
    var weightText by remember { mutableStateOf("30") }
    var restText by remember { mutableStateOf("90") }

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

    val availableGuides = remember { ExerciseGifMapper.getAllAvailableExercises() }
    val muscleFilters = listOf("Todos", "Tríceps", "Bíceps", "Peitoral", "Dorsal", "Deltoide", "Quadríceps", "Posterior / Glúteos", "Panturrilha")

    val filteredCatalog = availableGuides.filter { guide ->
        val matchesSearch = guide.exerciseName.contains(searchQuery, ignoreCase = true) ||
                guide.muscleGroup.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedMuscleFilter) {
            "Todos" -> true
            "Tríceps" -> guide.muscleGroup.contains("Tríceps", ignoreCase = true) || guide.exerciseName.contains("Tríceps", ignoreCase = true)
            "Bíceps" -> guide.muscleGroup.contains("Bíceps", ignoreCase = true) || guide.exerciseName.contains("Rosca", ignoreCase = true)
            "Peitoral" -> guide.muscleGroup.contains("Peitoral", ignoreCase = true) || guide.exerciseName.contains("Supino", ignoreCase = true) || guide.exerciseName.contains("Crucifixo", ignoreCase = true) || guide.exerciseName.contains("Voador", ignoreCase = true)
            "Dorsal" -> guide.muscleGroup.contains("Dorsal", ignoreCase = true) || guide.muscleGroup.contains("Costas", ignoreCase = true) || guide.exerciseName.contains("Puxada", ignoreCase = true) || guide.exerciseName.contains("Remada", ignoreCase = true) || guide.exerciseName.contains("Barra Fixa", ignoreCase = true)
            "Deltoide" -> guide.muscleGroup.contains("Deltoide", ignoreCase = true) || guide.muscleGroup.contains("Ombro", ignoreCase = true) || guide.exerciseName.contains("Desenvolvimento", ignoreCase = true) || guide.exerciseName.contains("Lateral", ignoreCase = true) || guide.exerciseName.contains("Face Pull", ignoreCase = true)
            "Quadríceps" -> guide.muscleGroup.contains("Quadríceps", ignoreCase = true) || guide.exerciseName.contains("Agachamento", ignoreCase = true) || guide.exerciseName.contains("Leg Press", ignoreCase = true) || guide.exerciseName.contains("Extensora", ignoreCase = true)
            "Posterior / Glúteos" -> guide.muscleGroup.contains("Isquiotibiais", ignoreCase = true) || guide.muscleGroup.contains("Glúteo", ignoreCase = true) || guide.exerciseName.contains("Flexora", ignoreCase = true) || guide.exerciseName.contains("Stiff", ignoreCase = true) || guide.exerciseName.contains("Pélvica", ignoreCase = true) || guide.exerciseName.contains("Afundo", ignoreCase = true)
            "Panturrilha" -> guide.muscleGroup.contains("Panturrilha", ignoreCase = true) || guide.muscleGroup.contains("Gastrocnêmio", ignoreCase = true) || guide.exerciseName.contains("Panturrilha", ignoreCase = true)
            else -> guide.muscleGroup.contains(selectedMuscleFilter, ignoreCase = true) ||
                    guide.exerciseName.contains(selectedMuscleFilter, ignoreCase = true)
        }
        matchesSearch && matchesFilter
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Adicionar Exercício", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tab Selector (Biblioteca com GIFs vs Manual)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = if (selectedTab == 0) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("🎥 Biblioteca com GIFs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        color = if (selectedTab == 1) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("✏️ Outro / Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        text = {
            if (selectedTab == 0) {
                // GIF CATALOG TAB
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar exercício ou músculo...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        items(muscleFilters) { muscle ->
                            val isSel = selectedMuscleFilter == muscle
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedMuscleFilter = muscle },
                                label = { Text(muscle, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = CarbonCardElevated,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCatalog) { guide ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val s = setsText.toIntOrNull() ?: 4
                                        val r = repsText.toIntOrNull() ?: 10
                                        val w = weightText.toFloatOrNull() ?: 30f
                                        val rest = restText.toIntOrNull() ?: 90

                                        onConfirm?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip)
                                        onAdd?.invoke(guide.exerciseName, guide.muscleGroup, s, r, w)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // GIF Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(width = 64.dp, height = 52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CarbonDark)
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(guide.gifUrl)
                                                .crossfade(true)
                                                .build(),
                                            imageLoader = gifImageLoader,
                                            contentDescription = guide.exerciseName,
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(18.dp))
                                                }
                                            },
                                            error = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(guide.exerciseName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text(guide.muscleGroup, fontSize = 10.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                        Text(guide.postureTip, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }

                                    Button(
                                        onClick = {
                                            val s = setsText.toIntOrNull() ?: 4
                                            val r = repsText.toIntOrNull() ?: 10
                                            val w = weightText.toFloatOrNull() ?: 30f
                                            val rest = restText.toIntOrNull() ?: 90

                                            onConfirm?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip)
                                            onAdd?.invoke(guide.exerciseName, guide.muscleGroup, s, r, w)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // MANUAL INPUT TAB
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nome do Exercício") },
                        placeholder = { Text("Ex: Supino Reto com Barra") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                    )

                    OutlinedTextField(
                        value = customGroup,
                        onValueChange = { customGroup = it },
                        label = { Text("Grupo Muscular") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = setsText,
                            onValueChange = { setsText = it },
                            label = { Text("Séries") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Carga (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            val s = setsText.toIntOrNull() ?: 4
                            val r = repsText.toIntOrNull() ?: 10
                            val w = weightText.toFloatOrNull() ?: 20f
                            val rest = restText.toIntOrNull() ?: 90

                            onConfirm?.invoke(customName, customGroup, s, r, r, w, rest, "")
                            onAdd?.invoke(customName, customGroup, s, r, w)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Text("Adicionar Customizado", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
