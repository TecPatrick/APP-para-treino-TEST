package com.example.ui.screens.workout

import android.content.Intent
import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: GrowthViewModel) {
    val context = LocalContext.current
    val allRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()

    var selectedRoutineId by remember {
        mutableStateOf(allRoutines.firstOrNull()?.id ?: 0L)
    }

    LaunchedEffect(allRoutines) {
        if (allRoutines.isNotEmpty() && allRoutines.none { it.id == selectedRoutineId }) {
            selectedRoutineId = allRoutines.first().id
        }
    }

    val currentRoutine = allRoutines.find { it.id == selectedRoutineId } ?: allRoutines.firstOrNull()
    val exercisesForCurrentRoutine = allExercises.filter { it.routineId == (currentRoutine?.id ?: 0L) }

    var showRoutineEditorDialog by remember { mutableStateOf(false) }
    var routineToEdit by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var routineToDelete by remember { mutableStateOf<WorkoutRoutine?>(null) }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<WorkoutExercise?>(null) }

    var selectedExerciseGuide by remember { mutableStateOf<ExerciseVisualGuide?>(null) }
    var showAIGeneratorDialog by remember { mutableStateOf(false) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Header fitvibe style
        item {
            GrowthHeader(
                title = "Fichas de Treino",
                subtitle = "Gestão de Fichas de Treino & Biomecânica 3D",
                badgeText = "FITVIBE ENGINE",
                trailingContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showAIGeneratorDialog = true },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GrowthCyan.copy(alpha = 0.2f))
                                .testTag("workout_ai_gen_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Gerar com IA", tint = GrowthCyan)
                        }
                        IconButton(
                            onClick = {
                                routineToEdit = null
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

        // 2. Active Workout Progress Drawer / Strip (If session is running)
        if (activeWorkout.isRunning) {
            item {
                ActiveWorkoutSessionBanner(
                    activeWorkout = activeWorkout,
                    onFinishClick = { showFinishWorkoutDialog = true },
                    onSkipRestClick = { viewModel.cancelRestTimer() }
                )
            }
        }

        // 3. Quick Stats Ribbon
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                FitVibeStatsCard(
                    totalRoutines = allRoutines.size,
                    totalExercises = allExercises.size,
                    activeRoutineTitle = currentRoutine?.title ?: "Nenhum",
                    isSessionRunning = activeWorkout.isRunning
                )
            }
        }

        // 4. Quick Action Toolbar
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
                    onClick = { showAIGeneratorDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCardElevated),
                    border = BorderStroke(1.dp, GrowthCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("generate_ai_action_btn")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gerar c/ IA", fontWeight = FontWeight.Bold, color = GrowthCyan, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = {
                        viewModel.createTemplateCustomRoutine("CHEST_TRICEPS") { newId ->
                            selectedRoutineId = newId
                        }
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthYellow),
                    border = BorderStroke(1.dp, CarbonBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Modelo PPL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // 5. Routines Carousel / Tabs Selector
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUAS FICHAS DE TREINO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = GrowthOrangeLight
                    )
                    Text(
                        text = "${allRoutines.size} Ficha(s)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (allRoutines.isEmpty()) {
                    EmptyRoutinesStateCard(
                        onCreateManualClick = {
                            routineToEdit = null
                            showRoutineEditorDialog = true
                        },
                        onGenerateAIClick = { showAIGeneratorDialog = true }
                    )
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        items(allRoutines) { routine ->
                            val isSelected = (routine.id == currentRoutine?.id)
                            val countExercises = allExercises.count { it.routineId == routine.id }

                            Card(
                                onClick = { selectedRoutineId = routine.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CarbonCardElevated else CarbonCard
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) GrowthOrange else CarbonBorder
                                ),
                                modifier = Modifier
                                    .width(180.dp)
                                    .testTag("routine_card_${routine.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = if (isSelected) GrowthOrange else CarbonBorder,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = routine.letter,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = {
                                                    routineToEdit = routine
                                                    showRoutineEditorDialog = true
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                                            }
                                            IconButton(
                                                onClick = { routineToDelete = routine },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = GrowthRed, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = routine.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Text(
                                        text = routine.targetMuscleGroups,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = GrowthOrangeLight
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⏱️ ${routine.estimatedDurationMinutes} min",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            color = GrowthCyan.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "$countExercises ex",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthCyan,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Selected Active Routine Hero Card & Start Workout CTA
        if (currentRoutine != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = GrowthOrange,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(currentRoutine.letter, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = currentRoutine.title,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${currentRoutine.dayOfWeekLabel} • Músculos: ${currentRoutine.targetMuscleGroups}",
                                            fontSize = 12.sp,
                                            color = GrowthOrangeLight
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = {
                                    routineToEdit = currentRoutine
                                    showRoutineEditorDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar Ficha", tint = GrowthOrange)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Big Start Workout Button
                        Button(
                            onClick = {
                                viewModel.startActiveWorkout(currentRoutine, exercisesForCurrentRoutine)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_workout_hero_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (activeWorkout.isRunning && activeWorkout.routine?.id == currentRoutine.id) "TREINO EM ANDAMENTO" else "INICIAR ESTE TREINO",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            // 7. Exercise List Section Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCÍCIOS DA FICHA (${exercisesForCurrentRoutine.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Button(
                        onClick = { showAddExerciseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCardElevated),
                        border = BorderStroke(1.dp, GrowthOrange),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("add_exercise_to_routine_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("＋ Exercício", fontSize = 11.sp, color = GrowthOrange, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 8. Exercise Items Cards
            if (exercisesForCurrentRoutine.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Esta ficha ainda não possui exercícios.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showAddExerciseDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                            ) {
                                Text("Adicionar Exercício Agora", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(exercisesForCurrentRoutine) { index, exercise ->
                    val guide = remember(exercise.exerciseName) { ExerciseGifMapper.getGuideForExercise(exercise.exerciseName) }

                    FitVibeExerciseCard(
                        index = index + 1,
                        exercise = exercise,
                        guide = guide,
                        gifImageLoader = gifImageLoader,
                        isActiveWorkoutSession = activeWorkout.isRunning && activeWorkout.routine?.id == currentRoutine.id,
                        loggedSets = activeWorkout.loggedExerciseSets[exercise.id] ?: emptyList(),
                        onGifClick = { selectedExerciseGuide = guide },
                        onWeightChange = { newWeight ->
                            viewModel.updateExerciseWeightAndReps(exercise, newWeight, exercise.sets)
                        },
                        onSetCheckClick = { reps, weight ->
                            viewModel.logSetAndStartRest(exercise, reps, weight)
                        },
                        onDeleteClick = { viewModel.deleteWorkoutExercise(exercise.id) }
                    )
                }
            }
        }
    }

    // --- MODAL DIALOGS ---

    // 1. Routine Editor Dialog
    if (showRoutineEditorDialog) {
        com.example.ui.screens.workout.WorkoutRoutineEditorDialog(
            existingRoutine = routineToEdit,
            existingExercises = if (routineToEdit != null) allExercises.filter { it.routineId == routineToEdit!!.id } else emptyList(),
            initialSplit = WorkoutSplitType.CUSTOM,
            onDismiss = { showRoutineEditorDialog = false },
            onSave = { existingId, splitType, letter, title, dayOfWeek, muscles, durationMin, exercises ->
                viewModel.saveFullRoutineWithExercises(
                    existingRoutineId = existingId,
                    splitType = splitType,
                    letter = letter,
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

    // 2. Add Single Exercise Dialog
    if (showAddExerciseDialog && currentRoutine != null) {
        AddExerciseDialog(
            routine = currentRoutine,
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, group, sets, rMin, rMax, weight, rest, notes ->
                viewModel.addWorkoutExercise(
                    routineId = currentRoutine.id,
                    exerciseName = name,
                    muscleGroup = group,
                    sets = sets,
                    repsMin = rMin,
                    repsMax = rMax,
                    currentWeightKg = weight,
                    restSeconds = rest,
                    notes = notes
                )
                showAddExerciseDialog = false
            }
        )
    }

    // 3. Biomechanics Guide & GIF Modal
    if (selectedExerciseGuide != null) {
        ExerciseBiomechanicsGuideModal(
            guide = selectedExerciseGuide!!,
            gifImageLoader = gifImageLoader,
            onDismiss = { selectedExerciseGuide = null }
        )
    }

    // 4. AI Generator Modal
    if (showAIGeneratorDialog) {
        AIGeneratorWorkoutModal(
            onDismiss = { showAIGeneratorDialog = false },
            onGenerateProtocol = {
                viewModel.generateWorkoutProtocolForProfile { newId ->
                    selectedRoutineId = newId
                }
                showAIGeneratorDialog = false
            }
        )
    }

    // 5. Finish Workout Modal
    if (showFinishWorkoutDialog) {
        FinishWorkoutModal(
            activeWorkout = activeWorkout,
            onDismiss = { showFinishWorkoutDialog = false },
            onConfirmFinish = { rpe, notes ->
                viewModel.finishActiveWorkout(rpe, notes)
                showFinishWorkoutDialog = false
            }
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

// FitVibe Metrics Summary Ribbon
@Composable
fun FitVibeStatsCard(
    totalRoutines: Int,
    totalExercises: Int,
    activeRoutineTitle: String,
    isSessionRunning: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = GrowthOrange.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Painel Fichas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Text("$totalRoutines Ficha(s) • $totalExercises Exercícios", fontSize = 10.sp, color = GrowthOrangeLight)
                }
            }

            Surface(
                color = if (isSessionRunning) GrowthGreen.copy(alpha = 0.2f) else CarbonCardElevated,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, if (isSessionRunning) GrowthGreen else CarbonBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (isSessionRunning) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(6.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSessionRunning) "EM EXECUÇÃO" else "SELECIONADO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSessionRunning) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// Active Running Session Banner
@Composable
fun ActiveWorkoutSessionBanner(
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    onFinishClick: () -> Unit,
    onSkipRestClick: () -> Unit
) {
    val minutes = activeWorkout.elapsedSeconds / 60
    val seconds = activeWorkout.elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, GrowthGreen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GrowthGreen,
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TREINO EM ANDAMENTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GrowthGreen,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = activeWorkout.routine?.title ?: "Sessão Ativa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Surface(
                    color = CarbonDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "⏱️ $formattedTime",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeWorkout.isRestTimerRunning) {
                Surface(
                    color = GrowthOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GrowthOrange)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏸️ Descanso entre séries: ${activeWorkout.restTimerRemainingSeconds}s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrange
                        )
                        TextButton(onClick = onSkipRestClick) {
                            Text("Pular Descanso", fontSize = 11.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Séries concluídas: ${activeWorkout.completedSetsCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onFinishClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("finish_workout_btn")
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Finalizar Treino", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// Single Exercise Card in List
@Composable
fun FitVibeExerciseCard(
    index: Int,
    exercise: WorkoutExercise,
    guide: ExerciseVisualGuide,
    gifImageLoader: ImageLoader,
    isActiveWorkoutSession: Boolean,
    loggedSets: List<Pair<Int, Float>>,
    onGifClick: () -> Unit,
    onWeightChange: (Float) -> Unit,
    onSetCheckClick: (reps: Int, weight: Float) -> Unit,
    onDeleteClick: () -> Unit
) {
    var isExpandedGuide by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("exercise_item_${exercise.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    // GIF Thumbnail with tap gesture
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CarbonDark)
                            .border(1.dp, GrowthOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onGifClick() }
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(guide.gifUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = gifImageLoader,
                            contentDescription = "Demonstração 3D ${exercise.exerciseName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            loading = {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    CircularProgressIndicator(color = GrowthOrange, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                                }
                            },
                            error = {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange)
                                }
                            }
                        )

                        Surface(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "GIF",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GrowthCyan,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GrowthOrange,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("$index", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = exercise.exerciseName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Surface(
                            color = GrowthOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = exercise.muscleGroup,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrangeLight,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${exercise.sets} Séries • ${exercise.repsMin}-${exercise.repsMax} Reps • Descanso: ${exercise.restSeconds}s",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover Exercício", tint = GrowthRed, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Weight Adjuster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Carga Atual:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val w = (exercise.currentWeightKg - 2.5f).coerceAtLeast(0f)
                            onWeightChange(w)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(CarbonCardElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Diminuir Carga", tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${exercise.currentWeightKg.toInt()} kg",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthGreen,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = {
                            val w = exercise.currentWeightKg + 2.5f
                            onWeightChange(w)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(CarbonCardElevated, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Aumentar Carga", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Interactive Set Checkboxes during active workout session
            if (isActiveWorkoutSession) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Séries Deste Treino:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(exercise.sets) { setIdx ->
                        val isSetDone = setIdx < loggedSets.size

                        Button(
                            onClick = {
                                if (!isSetDone) {
                                    onSetCheckClick(exercise.repsMax, exercise.currentWeightKg)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSetDone) GrowthGreen else CarbonCardElevated
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (isSetDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = "Série ${setIdx + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSetDone) Color.Black else Color.White
                            )
                        }
                    }
                }
            }

            // Biomechanics Tip Accordion
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpandedGuide = !isExpandedGuide },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dica Biomecânica / Postura", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                Spacer(modifier = Modifier.weight(1f))
                Text(if (isExpandedGuide) "Ocultar" else "Ver mais", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            AnimatedVisibility(visible = isExpandedGuide) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .background(CarbonDark, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("💡 ${guide.postureTip}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("▶ Concêntrica: ${guide.concentricPhase}", fontSize = 10.sp, color = GrowthGreen)
                    Text("◀ Excêntrica: ${guide.eccentricPhase}", fontSize = 10.sp, color = GrowthCyan)
                }
            }
        }
    }
}

// Empty State Card when user has no workout routines saved
@Composable
fun EmptyRoutinesStateCard(
    onCreateManualClick: () -> Unit,
    onGenerateAIClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = GrowthOrange.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Nenhuma Ficha de Treino Cadastrada",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Crie suas fichas de treino personalizadas do zero ou gere um protocolo completo de hipertrofia com Inteligência Artificial.",
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCreateManualClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GrowthOrange),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Criar Ficha", color = GrowthOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onGenerateAIClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Gerar c/ IA", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// Modal for Adding Single Exercise to Routine
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    routine: WorkoutRoutine,
    onDismiss: () -> Unit,
    onConfirm: (name: String, group: String, sets: Int, rMin: Int, rMax: Int, weight: Float, rest: Int, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(routine.targetMuscleGroups) }
    var setsText by remember { mutableStateOf("4") }
    var repsMinText by remember { mutableStateOf("8") }
    var repsMaxText by remember { mutableStateOf("12") }
    var weightText by remember { mutableStateOf("30") }
    var restText by remember { mutableStateOf("90") }
    var notes by remember { mutableStateOf("Cadência 2-0-2, até a falha na última série.") }

    val presetExercises = remember {
        listOf(
            "Supino Reto com Barra",
            "Supino Inclinado com Halteres",
            "Crucifixo no Cross / Crossover",
            "Desenvolvimento com Halteres",
            "Puxada Alta Aberta no Pulley",
            "Remada Curvada com Barra",
            "Agachamento Livre com Barra",
            "Leg Press 45°",
            "Cadeira Extensora",
            "Elevação Lateral com Halteres",
            "Rosca Direta com Barra W",
            "Tríceps Pulley Corda"
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Text("Adicionar Exercício à Ficha", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Ficha: ${routine.title}", fontSize = 12.sp, color = GrowthOrangeLight)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Sugestões Rápidas:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presetExercises) { preset ->
                        Surface(
                            color = CarbonCardElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier.clickable {
                                name = preset
                                val guide = ExerciseGifMapper.getGuideForExercise(preset)
                                group = guide.muscleGroup
                            }
                        ) {
                            Text(preset, fontSize = 10.sp, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) {
                            val guide = ExerciseGifMapper.getGuideForExercise(it)
                            group = guide.muscleGroup
                        }
                    },
                    label = { Text("Nome do Exercício") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = group,
                    onValueChange = { group = it },
                    label = { Text("Grupo Muscular Alvo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = setsText,
                        onValueChange = { setsText = it },
                        label = { Text("Séries") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )
                    OutlinedTextField(
                        value = repsMinText,
                        onValueChange = { repsMinText = it },
                        label = { Text("Reps Mín") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )
                    OutlinedTextField(
                        value = repsMaxText,
                        onValueChange = { repsMaxText = it },
                        label = { Text("Reps Máx") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Carga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthGreen, unfocusedBorderColor = CarbonBorder)
                    )
                    OutlinedTextField(
                        value = restText,
                        onValueChange = { restText = it },
                        label = { Text("Descanso (s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val sets = setsText.toIntOrNull() ?: 4
                        val rMin = repsMinText.toIntOrNull() ?: 8
                        val rMax = repsMaxText.toIntOrNull() ?: 12
                        val weight = weightText.toFloatOrNull() ?: 30f
                        val rest = restText.toIntOrNull() ?: 90
                        onConfirm(name, group, sets, rMin, rMax, weight, rest, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
            ) {
                Text("Adicionar à Ficha", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// Biomechanics Guide & GIF Modal
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
            border = BorderStroke(1.dp, GrowthOrange),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(guide.exerciseName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        Text(guide.muscleGroup, fontSize = 12.sp, color = GrowthOrangeLight)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GIF Player
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonDark)
                        .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp))
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(guide.gifUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = gifImageLoader,
                        contentDescription = "Animação 3D ${guide.exerciseName}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(color = GrowthOrange)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = CarbonDark,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("💡 Postura & Dicas Biomecânicas:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(guide.postureTip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("▶ Fase Concêntrica:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                        Text(guide.concentricPhase, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text("◀ Fase Excêntrica:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                        Text(guide.eccentricPhase, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.fitnessProgrammerUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCardElevated),
                    border = BorderStroke(1.dp, GrowthCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Guia Completo no Navegador", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// AI Protocol Generator Modal
@Composable
fun AIGeneratorWorkoutModal(
    onDismiss: () -> Unit,
    onGenerateProtocol: () -> Unit
) {
    var isGenerating by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GrowthCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Gerador de Fichas com IA", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "A IA analisará seu perfil de treino e criará uma divisão completa de fichas personalizadas com exercícios, carga inicial recomendada e guia de biomecânica.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isGenerating) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = GrowthCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Montando suas Fichas com IA...", fontSize = 12.sp, color = GrowthCyan)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isGenerating = true
                    onGenerateProtocol()
                },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan)
            ) {
                Text("Gerar Fichas Agora", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isGenerating) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// Finish Active Workout Summary Modal
@Composable
fun FinishWorkoutModal(
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    onDismiss: () -> Unit,
    onConfirmFinish: (perceivedExertion: Int, notes: String) -> Unit
) {
    var rpe by remember { mutableFloatStateOf(8f) }
    var notes by remember { mutableStateOf("") }

    val minutes = activeWorkout.elapsedSeconds / 60
    val seconds = activeWorkout.elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("🏁 Finalizar Treino", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Parabéns por concluir a sessão!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthGreen
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("⏱️ Duração Total: $formattedTime", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)
                Text("📊 Séries Concluídas: ${activeWorkout.completedSetsCount}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Esforço Percebido (RPE: ${rpe.toInt()} / 10):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                Slider(
                    value = rpe,
                    onValueChange = { rpe = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthOrange,
                        activeTrackColor = GrowthOrange,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações do Treino") },
                    placeholder = { Text("Ex: Carga excelente no supino, boa progressão.") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmFinish(rpe.toInt(), notes) },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen)
            ) {
                Text("Salvar no Histórico", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
