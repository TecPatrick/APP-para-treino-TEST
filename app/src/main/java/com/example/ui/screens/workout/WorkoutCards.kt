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
    onSubstituteExercise: (WorkoutExercise) -> Unit,
    onAddExerciseClick: () -> Unit,
    onAdjustRest: (Int) -> Unit
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
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⏸️ Descanso: ${activeWorkout.restTimerRemainingSeconds}s", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = GrowthOrange)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            OutlinedButton(
                                                onClick = { onAdjustRest(-15) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("-15s", fontSize = 10.sp, color = GrowthOrange)
                                            }
                                            OutlinedButton(
                                                onClick = { onAdjustRest(15) },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("+15s", fontSize = 10.sp, color = GrowthOrange)
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        TextButton(onClick = onSkipRest) {
                                            Text("Pular Descanso", fontSize = 11.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                        }
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
                    onOpenGuide = { onOpenGuide(guide) },
                    onSubstitute = { onSubstituteExercise(exercise) }
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
    onOpenGuide: () -> Unit,
    onSubstitute: () -> Unit
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
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exercise.exerciseName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 3
                        )
                        Text(
                            text = exercise.muscleGroup,
                            fontSize = 11.sp,
                            color = GrowthOrangeLight
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Substitute button (1-tap swap)
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier.weight(1f).clickable { onSubstitute() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = "Substituir", tint = GrowthOrange, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Trocar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    // Biomechanics & GIF guide trigger button
                    Surface(
                        color = GrowthCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).clickable { onOpenGuide() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡 Postura & GIF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                        }
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
    onShareClick: () -> Unit,
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

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShareClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = GrowthGreen, modifier = Modifier.size(16.dp))
                    }
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
