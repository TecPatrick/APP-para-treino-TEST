package com.example.ui.screens.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseVisualGuide

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
    onAdjustRest: (Int) -> Unit,
    onAddSet: (WorkoutExercise) -> Unit = {},
    onRemoveSet: (WorkoutExercise) -> Unit = {},
    onToggleSound: () -> Unit = {},
    onUpdateLoggedSet: (exercise: WorkoutExercise, setIndex: Int, reps: Int, weight: Float) -> Unit = { _, _, _, _ -> },
    onRemoveLoggedSet: (exercise: WorkoutExercise, setIndex: Int) -> Unit = { _, _ -> }
) {
    val isSessionRunningForThisRoutine = activeWorkout.isRunning && activeWorkout.routine?.id == routine.id
    var editingSetInfo by remember { mutableStateOf<SetEditInfo?>(null) }

    if (editingSetInfo != null) {
        val info = editingSetInfo!!
        EditSetLogDialog(
            info = info,
            onDismiss = { editingSetInfo = null },
            onSave = { reps, weight ->
                onUpdateLoggedSet(info.exercise, info.setIndex, reps, weight)
            },
            onUnlog = {
                onRemoveLoggedSet(info.exercise, info.setIndex)
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonDark),
            contentPadding = PaddingValues(bottom = if (isSessionRunningForThisRoutine && (activeWorkout.isRestTimerRunning || activeWorkout.isRestTimerFinished)) 130.dp else 90.dp)
        ) {
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(routine.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${routine.dayOfWeekLabel} • ${exercises.size} exercícios", fontSize = 11.sp, color = GrowthOrangeLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onAddExerciseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("header_add_exercise_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Exercício", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

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

                            if (activeWorkout.isRestTimerRunning || activeWorkout.isRestTimerFinished) {
                                Surface(
                                    color = if (activeWorkout.isRestTimerFinished) GrowthGreen.copy(alpha = 0.2f) else GrowthOrange.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (activeWorkout.isRestTimerFinished) GrowthGreen else GrowthOrange)
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
                                            if (activeWorkout.isRestTimerFinished) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("⏱️ Descanso concluído! Bora pra próxima série 💪", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                                }
                                            } else {
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
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            TextButton(onClick = onSkipRest) {
                                                Text(if (activeWorkout.isRestTimerFinished) "Dispensar" else "Pular Descanso", fontSize = 11.sp, color = if (activeWorkout.isRestTimerFinished) GrowthGreen else GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onAddExerciseClick,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, GrowthOrange),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .testTag("add_exercise_active_session_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Exercício", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GrowthOrange, maxLines = 1)
                                }

                                Button(
                                    onClick = onFinishWorkout,
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1.4f)
                                        .height(40.dp)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("FINALIZAR TREINO", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White, maxLines = 1)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Tudo pronto pro treino!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text("Inicie a sessão para marcar séries e controlar o descanso.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

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

            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text(
                        text = "EXERCÍCIOS DO TREINO (${exercises.size})",
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
                            Text("Nenhum exercício nesta sessão ainda", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Adicione os exercícios que você vai fazer hoje para acompanhar suas séries e cargas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
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
                        loggedSets = loggedForThisEx,
                        onToggleSet = { setIndex -> onToggleSet(exercise, setIndex) },
                        onEditLoggedSet = { setIndex, reps, weight ->
                            editingSetInfo = SetEditInfo(exercise, setIndex, reps, weight)
                        },
                        onAddSet = { onAddSet(exercise) },
                        onRemoveSet = { onRemoveSet(exercise) },
                        onUpdateWeight = { newW -> onUpdateWeight(exercise, newW) },
                        onOpenGuide = { onOpenGuide(guide) },
                        onSubstitute = { onSubstituteExercise(exercise) }
                    )
                }

                item {
                    OutlinedButton(
                        onClick = onAddExerciseClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GrowthOrange),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .testTag("add_exercise_bottom_list_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INCLUIR EXERCÍCIO NESTE TREINO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrange
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = isSessionRunningForThisRoutine && (activeWorkout.isRestTimerRunning || activeWorkout.isRestTimerFinished),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            FloatingRestBottomBar(
                activeWorkout = activeWorkout,
                onAdjustRest = onAdjustRest,
                onSkipRest = onSkipRest,
                onToggleSound = onToggleSound
            )
        }
    }
}

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

@Composable
fun EmptyWorkoutGroupCard(
    title: String,
    subtitle: String,
    onCreateClick: () -> Unit,
    onRestoreDefaultClick: (() -> Unit)? = null
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
            if (onRestoreDefaultClick != null) {
                Button(
                    onClick = onRestoreDefaultClick,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Carregar Divisão ABCDE Completa", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground),
                    border = BorderStroke(1.dp, CarbonBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Criar Ficha em Branco", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
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
}
