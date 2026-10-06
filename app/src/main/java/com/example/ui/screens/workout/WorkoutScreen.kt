package com.example.ui.screens.workout

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.util.ExerciseGifMapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(viewModel: GrowthViewModel) {
    val context = LocalContext.current
    val allRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val activeWorkout by viewModel.activeWorkout.collectAsStateWithLifecycle()

    var selectedSplitType by remember { mutableStateOf(WorkoutSplitType.ABCDE) }

    val filteredRoutines = allRoutines.filter { it.splitType == selectedSplitType }
    var selectedRoutineId by remember {
        mutableStateOf(filteredRoutines.firstOrNull()?.id ?: 1L)
    }

    val currentRoutine = filteredRoutines.find { it.id == selectedRoutineId } ?: filteredRoutines.firstOrNull() ?: allRoutines.firstOrNull()
    val exercisesForCurrentRoutine = allExercises.filter { it.routineId == (currentRoutine?.id ?: 0L) }

    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<WorkoutExercise?>(null) }
    var selectedExerciseGuide by remember { mutableStateOf<WorkoutExercise?>(null) }
    var showFinishWorkoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = "Treinos Personalizados",
                subtitle = "Divisões AB, ABC, ABCDE & Biomecânica",
                badgeText = "FICHA DE TREINO",
                trailingContent = {
                    IconButton(
                        onClick = { showAddRoutineDialog = true },
                        modifier = Modifier.testTag("add_routine_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Criar Treino", tint = GrowthOrange)
                    }
                }
            )

            // Active Workout Live Bar (if workout running)
            if (activeWorkout.isRunning) {
                ActiveWorkoutFloatingBar(
                    session = activeWorkout,
                    onFinish = { showFinishWorkoutDialog = true },
                    onCancel = { viewModel.cancelActiveWorkout() },
                    onCancelRest = { viewModel.cancelRestTimer() }
                )
            }

            // Split Type Tabs (AB, ABC, ABCDE, Custom)
            TabRow(
                selectedTabIndex = WorkoutSplitType.values().indexOf(selectedSplitType),
                containerColor = CarbonCard,
                contentColor = GrowthOrange,
                indicator = { tabPositions ->
                    val idx = WorkoutSplitType.values().indexOf(selectedSplitType)
                    if (idx in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[idx]),
                            color = GrowthOrange
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                WorkoutSplitType.values().forEach { split ->
                    val isSelected = selectedSplitType == split
                    Tab(
                        selected = isSelected,
                        onClick = {
                            selectedSplitType = split
                            val firstInSplit = allRoutines.firstOrNull { it.splitType == split }
                            if (firstInSplit != null) {
                                selectedRoutineId = firstInSplit.id
                            }
                        },
                        text = {
                            Text(
                                text = when (split) {
                                    WorkoutSplitType.AB -> "Treino AB"
                                    WorkoutSplitType.ABC -> "Treino ABC"
                                    WorkoutSplitType.ABCDE -> "ABCDE"
                                    WorkoutSplitType.CUSTOM -> "Personalizado"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Routine Selector Chips for current split
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(filteredRoutines) { routine ->
                    FilterChip(
                        selected = (routine.id == currentRoutine?.id),
                        onClick = { selectedRoutineId = routine.id },
                        label = {
                            Text(
                                "Treino ${routine.letter}: ${routine.title.take(16)}...",
                                fontSize = 11.sp,
                                fontWeight = if (routine.id == currentRoutine?.id) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrowthOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("routine_chip_${routine.letter}")
                    )
                }

                item {
                    FilterChip(
                        selected = false,
                        onClick = { showAddRoutineDialog = true },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = GrowthOrange)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("＋ Criar Nova Ficha", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = CarbonCardElevated
                        )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp)
            ) {
                if (currentRoutine != null) {
                    // Routine Header Card with Action to Start Workout
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = GrowthOrange,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "TREINO ${currentRoutine.letter} • ${currentRoutine.dayOfWeekLabel.uppercase()}",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentRoutine.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Grupamentos: ${currentRoutine.targetMuscleGroups} • ${exercisesForCurrentRoutine.size} exercícios",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (currentRoutine.splitType == WorkoutSplitType.CUSTOM) {
                                        IconButton(
                                            onClick = { viewModel.deleteWorkoutRoutine(currentRoutine.id) }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir Ficha Customizada",
                                                tint = GrowthRed,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.startActiveWorkout(currentRoutine, exercisesForCurrentRoutine)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_routine_session_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "INICIAR SESSÃO DE TREINO (${currentRoutine.letter})",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Exercise List with Biomechanics Guide & GIF animation button
                    items(exercisesForCurrentRoutine) { exercise ->
                        val isSessionActive = activeWorkout.isRunning && activeWorkout.routine?.id == currentRoutine.id
                        val loggedSetsForThisEx = activeWorkout.loggedExerciseSets[exercise.id] ?: emptyList()

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("exercise_card_${exercise.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = exercise.exerciseName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Grupamento: ${exercise.muscleGroup}",
                                            fontSize = 11.sp,
                                            color = GrowthOrangeLight
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Biomechanics & GIF guide button
                                        IconButton(
                                            onClick = { selectedExerciseGuide = exercise },
                                            modifier = Modifier.size(28.dp).testTag("guide_btn_${exercise.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Guia Biomecânico",
                                                tint = GrowthCyan,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        IconButton(
                                            onClick = { exerciseToEdit = exercise },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteWorkoutExercise(exercise.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Deletar",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = CarbonCardElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${exercise.sets} séries x ${exercise.repsMin}-${exercise.repsMax} reps",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = GrowthCyan,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = CarbonCardElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Carga: ${exercise.currentWeightKg.toInt()} kg",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthGreen,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        color = CarbonCardElevated,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Descanso: ${exercise.restSeconds}s",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                if (exercise.biomechanicsGuide.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "🎯 Execução: ${exercise.biomechanicsGuide}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // If Workout in Progress, show quick set check buttons
                                if (isSessionActive) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        for (setIdx in 1..exercise.sets) {
                                            val isCompleted = setIdx <= loggedSetsForThisEx.size
                                            Button(
                                                onClick = {
                                                    if (!isCompleted) {
                                                        viewModel.logSetAndStartRest(exercise, exercise.repsMin, exercise.currentWeightKg)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isCompleted) GrowthGreen else CarbonCardElevated
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.weight(1f).height(36.dp).testTag("set_btn_${exercise.id}_$setIdx")
                                            ) {
                                                Text(
                                                    text = if (isCompleted) "✓ S$setIdx" else "S$setIdx",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isCompleted) Color.Black else Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Exercise Button
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showAddExerciseDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_exercise_to_routine_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Adicionar Exercício ao Treino ${currentRoutine.letter}", color = GrowthOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Crie sua Ficha Personalizada", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Monte seu próprio protocolo de treino do zero ou escolha um modelo rápido:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))

                                Spacer(modifier = Modifier.height(16.dp))

                                Text("🚀 Modelos Rápidos em 1-Clique:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan, modifier = Modifier.align(Alignment.Start))
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { viewModel.createTemplateCustomRoutine("CHEST_TRICEPS") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthOrange),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ Ficha P1: Peitoral & Tríceps (4 exercícios)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = { viewModel.createTemplateCustomRoutine("BACK_BICEPS") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthOrange),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ Ficha P2: Dorsal & Bíceps (3 exercícios)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedButton(
                                    onClick = { viewModel.createTemplateCustomRoutine("LEGS") },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthOrange),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("⚡ Ficha P3: Pernas Completo (4 exercícios)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { showAddRoutineDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Criar Ficha Personalizada em Branco", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

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

    if (showAddRoutineDialog) {
        AddRoutineDialog(
            initialSplit = selectedSplitType,
            onDismiss = { showAddRoutineDialog = false },
            onConfirm = { split, letter, title, day, muscles, duration ->
                viewModel.addWorkoutRoutineWithSplit(split, letter, title, day, muscles, duration)
                showAddRoutineDialog = false
            }
        )
    }

    if (exerciseToEdit != null) {
        EditExerciseLoadDialog(
            exercise = exerciseToEdit!!,
            onDismiss = { exerciseToEdit = null },
            onSave = { newWeight, sets ->
                viewModel.updateExerciseWeightAndReps(exerciseToEdit!!, newWeight, sets)
                exerciseToEdit = null
            }
        )
    }

    if (selectedExerciseGuide != null) {
        val ex = selectedExerciseGuide!!
        val guide = remember(ex.exerciseName) { ExerciseGifMapper.getGuideForExercise(ex.exerciseName) }
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

        AlertDialog(
            onDismissRequest = { selectedExerciseGuide = null },
            containerColor = CarbonCard,
            title = {
                Column {
                    Text(ex.exerciseName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Grupamento: ${guide.muscleGroup}", fontSize = 12.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Animated Exercise GIF Player
                    Surface(
                        color = Color.Black,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            SubcomposeAsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(guide.gifUrl)
                                    .crossfade(true)
                                    .build(),
                                imageLoader = gifImageLoader,
                                contentDescription = "Demonstração de ${ex.exerciseName}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                                loading = {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text("Carregando Animação 3D do Exercício...", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                },
                                error = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(32.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Animação Biomecânica", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("📋 Guia Biomecânico de Execução", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(ex.biomechanicsGuide, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground)

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("🔴 Fase Concêntrica:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                            Text(guide.concentricPhase, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)

                            Spacer(modifier = Modifier.height(5.dp))
                            Text("🟢 Fase Excêntrica:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                            Text(guide.eccentricPhase, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)

                            Spacer(modifier = Modifier.height(5.dp))
                            Text("💡 Dica de Postura:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                            Text(guide.postureTip, fontSize = 10.sp, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedExerciseGuide = null }) {
                    Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showFinishWorkoutDialog) {
        FinishWorkoutDialog(
            session = activeWorkout,
            onDismiss = { showFinishWorkoutDialog = false },
            onFinish = { rpe, notes ->
                viewModel.finishActiveWorkout(rpe, notes)
                showFinishWorkoutDialog = false
            }
        )
    }
}

@Composable
fun ActiveWorkoutFloatingBar(
    session: com.example.ui.ActiveWorkoutSession,
    onFinish: () -> Unit,
    onCancel: () -> Unit,
    onCancelRest: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = GrowthOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val mins = session.elapsedSeconds / 60
                    val secs = session.elapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", mins, secs),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${session.completedSetsCount} séries feitas",
                        fontSize = 12.sp,
                        color = GrowthCyan
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onFinish,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.testTag("finish_workout_session_btn")
                    ) {
                        Text("FINALIZAR", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.Black)
                    }
                    IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = GrowthRed, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // Rest Timer indicator
            if (session.isRestTimerRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⏳ Descanso: ${session.restTimerRemainingSeconds}s restantes",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthYellow
                    )
                    Text(
                        text = "Pular Descanso",
                        fontSize = 11.sp,
                        color = GrowthCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onCancelRest() }
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseDialog(
    routine: WorkoutRoutine,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, Int, Int, Float, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(routine.targetMuscleGroups) }
    var setsText by remember { mutableStateOf("4") }
    var repsMinText by remember { mutableStateOf("8") }
    var repsMaxText by remember { mutableStateOf("12") }
    var weightText by remember { mutableStateOf("30") }
    var restText by remember { mutableStateOf("75") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Novo Exercício no Treino ${routine.letter}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Exercício (Ex: Supino Inclinado)") },
                        modifier = Modifier.fillMaxWidth().testTag("exercise_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    OutlinedTextField(
                        value = group,
                        onValueChange = { group = it },
                        label = { Text("Grupamento Muscular (Ex: Peitoral Superior)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = setsText,
                            onValueChange = { setsText = it },
                            label = { Text("Séries") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = repsMinText,
                            onValueChange = { repsMinText = it },
                            label = { Text("Reps Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = repsMaxText,
                            onValueChange = { repsMaxText = it },
                            label = { Text("Reps Max") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Carga (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = restText,
                            onValueChange = { restText = it },
                            label = { Text("Descanso (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Instruções / Técnica") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val s = setsText.toIntOrNull() ?: 4
                        val rMin = repsMinText.toIntOrNull() ?: 8
                        val rMax = repsMaxText.toIntOrNull() ?: 12
                        val w = weightText.toFloatOrNull() ?: 30f
                        val rest = restText.toIntOrNull() ?: 60
                        onConfirm(name, group, s, rMin, rMax, w, rest, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_add_exercise_btn")
            ) {
                Text("Adicionar Exercício", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AddRoutineDialog(
    initialSplit: WorkoutSplitType = WorkoutSplitType.CUSTOM,
    onDismiss: () -> Unit,
    onConfirm: (WorkoutSplitType, String, String, String, String, Int) -> Unit
) {
    var splitType by remember { mutableStateOf(initialSplit) }
    var letter by remember { mutableStateOf("F") }
    var title by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf("Sábado") }
    var muscles by remember { mutableStateOf("Braços & Core") }
    var durationText by remember { mutableStateOf("60") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Criar Nova Ficha de Treino", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = letter,
                        onValueChange = { letter = it.take(2) },
                        label = { Text("Letra (A, B, C...)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    OutlinedTextField(
                        value = durationText,
                        onValueChange = { durationText = it },
                        label = { Text("Duração (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (Ex: Bíceps & Tríceps)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dayOfWeek,
                    onValueChange = { dayOfWeek = it },
                    label = { Text("Dia da Semana") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = muscles,
                    onValueChange = { muscles = it },
                    label = { Text("Músculos Alvo") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val dur = durationText.toIntOrNull() ?: 60
                        onConfirm(splitType, letter, title, dayOfWeek, muscles, dur)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
            ) {
                Text("Criar Treino", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun EditExerciseLoadDialog(
    exercise: WorkoutExercise,
    onDismiss: () -> Unit,
    onSave: (Float, Int) -> Unit
) {
    var weightText by remember { mutableStateOf(exercise.currentWeightKg.toString()) }
    var setsText by remember { mutableStateOf(exercise.sets.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Ajustar Carga / Séries", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(exercise.exerciseName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Carga Atual (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = setsText,
                    onValueChange = { setsText = it },
                    label = { Text("Número de Séries") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toFloatOrNull() ?: exercise.currentWeightKg
                    val s = setsText.toIntOrNull() ?: exercise.sets
                    onSave(w, s)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
            ) {
                Text("Salvar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun FinishWorkoutDialog(
    session: com.example.ui.ActiveWorkoutSession,
    onDismiss: () -> Unit,
    onFinish: (Int, String) -> Unit
) {
    var rpe by remember { mutableStateOf(8f) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Parabéns pelo Treino!", fontWeight = FontWeight.Bold, color = GrowthGreen)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Duração: ${session.elapsedSeconds / 60} minutos • ${session.completedSetsCount} séries concluídas",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Percepção de Esforço (RPE): ${rpe.toInt()} / 10",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight
                )
                Slider(
                    value = rpe,
                    onValueChange = { rpe = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthOrange,
                        activeTrackColor = GrowthOrange
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Como se sentiu? (Pump, cargas, cansaço)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onFinish(rpe.toInt(), notes) },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                modifier = Modifier.testTag("confirm_finish_workout_btn")
            ) {
                Text("Salvar e Registrar Volume", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Voltar ao Treino", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
