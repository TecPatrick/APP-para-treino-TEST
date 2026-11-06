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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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

data class SetEditInfo(
    val exercise: WorkoutExercise,
    val setIndex: Int,
    val initialReps: Int,
    val initialWeight: Float
)

@Composable
fun EditSetLogDialog(
    info: SetEditInfo,
    onDismiss: () -> Unit,
    onSave: (reps: Int, weight: Float) -> Unit,
    onUnlog: () -> Unit
) {
    var reps by remember { mutableStateOf(info.initialReps) }
    var weight by remember { mutableFloatStateOf(info.initialWeight) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CarbonCard,
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ajustar Série ${info.setIndex}",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = info.exercise.exerciseName,
                            fontSize = 12.sp,
                            color = GrowthOrange,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Repetitions Controller
                Text("Repetições Realizadas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { reps = (reps - 1).coerceAtLeast(1) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-1", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                    }

                    Text(
                        text = "$reps reps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    IconButton(
                        onClick = { reps += 1 },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+1", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Weight Controller
                Text("Carga Utilizada:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonDark, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { weight = (weight - 5f).coerceAtLeast(0f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-5", fontSize = 11.sp, color = GrowthOrange)
                        }
                        IconButton(
                            onClick = { weight = (weight - 1f).coerceAtLeast(0f) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("-1", fontSize = 11.sp, color = GrowthOrangeLight)
                        }
                    }

                    Text(
                        text = "${weight.toInt()} kg",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrange
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { weight += 1f },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+1", fontSize = 11.sp, color = GrowthOrangeLight)
                        }
                        IconButton(
                            onClick = { weight += 5f },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Text("+5", fontSize = 11.sp, color = GrowthOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = {
                        onSave(reps, weight)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_set_edit_btn")
                ) {
                    Text("SALVAR AJUSTES", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        onUnlog()
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthRed),
                    border = BorderStroke(1.dp, GrowthRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("unlog_set_btn")
                ) {
                    Text("Desmarcar Série", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthRed)
                }
            }
        }
    }
}

@Composable
fun FloatingRestBottomBar(
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    onAdjustRest: (Int) -> Unit,
    onSkipRest: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, if (activeWorkout.isRestTimerFinished) GrowthGreen else GrowthOrange),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("floating_rest_bottom_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Status
            if (activeWorkout.isRestTimerFinished) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GrowthGreen.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Pronto!", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = GrowthGreen)
                        Text("Próxima série", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = GrowthOrange.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${activeWorkout.restTimerRemainingSeconds}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrange
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Descanso", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                        Text("${activeWorkout.restTimerRemainingSeconds}s restantes", fontSize = 10.sp, color = GrowthOrangeLight)
                    }
                }
            }

            // Quick Adjusters (-15s, +15s) if still running
            if (!activeWorkout.isRestTimerFinished) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = CarbonDark,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .clickable { onAdjustRest(-15) }
                            .testTag("floating_minus_15s_btn")
                    ) {
                        Text(
                            text = "-15s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }

                    Surface(
                        color = CarbonDark,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .clickable { onAdjustRest(15) }
                            .testTag("floating_plus_15s_btn")
                    ) {
                        Text(
                            text = "+15s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrange,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Right controls: Sound Toggle and Skip
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleSound,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("floating_sound_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (activeWorkout.isWorkoutSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Alternar Som",
                        tint = if (activeWorkout.isWorkoutSoundEnabled) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onSkipRest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeWorkout.isRestTimerFinished) GrowthGreen else CarbonDark
                    ),
                    border = BorderStroke(1.dp, if (activeWorkout.isRestTimerFinished) GrowthGreen else CarbonBorder),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp).testTag("floating_skip_rest_btn")
                ) {
                    Text(
                        text = if (activeWorkout.isRestTimerFinished) "OK" else "Pular",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeWorkout.isRestTimerFinished) Color.Black else GrowthOrangeLight
                    )
                }
            }
        }
    }
}

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

                        // Rest Timer Banner if active or finished
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

        // Section Title: EXERCÍCIOS DA FICHA
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

    // Floating Rest Bottom Bar
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

// Interactive Exercise Card inside Routine Detail View
@Composable
fun ExerciseExecutionCard(
    index: Int,
    exercise: WorkoutExercise,
    guide: ExerciseVisualGuide,
    isWorkoutRunning: Boolean,
    completedSetsInSession: Int,
    loggedSets: List<Pair<Int, Float>> = emptyList(),
    onToggleSet: (setIndex: Int) -> Unit,
    onEditLoggedSet: ((setIndex: Int, reps: Int, weight: Float) -> Unit)? = null,
    onAddSet: () -> Unit = {},
    onRemoveSet: (() -> Unit)? = null,
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = exercise.muscleGroup,
                                fontSize = 11.sp,
                                color = GrowthOrangeLight
                            )
                            if (exercise.id < 0L) {
                                Surface(
                                    color = GrowthGreen.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(4.dp),
                                    border = BorderStroke(0.5.dp, GrowthGreen)
                                ) {
                                    Text(
                                        text = "⚡ SÓ HOJE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GrowthGreen,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
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
                    
                    // Increment 1kg
                    IconButton(
                        onClick = { onUpdateWeight((exercise.currentWeightKg - 1f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-1", fontSize = 10.sp, color = GrowthOrangeLight)
                    }
                    
                    // Increment 5kg
                    IconButton(
                        onClick = { onUpdateWeight((exercise.currentWeightKg - 5f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-5", fontSize = 10.sp, color = GrowthOrange)
                    }

                    Text(
                        text = "${exercise.currentWeightKg.toInt()} kg",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrange,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { onUpdateWeight(exercise.currentWeightKg + 5f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("+5", fontSize = 10.sp, color = GrowthOrange)
                    }

                    IconButton(
                        onClick = { onUpdateWeight(exercise.currentWeightKg + 1f) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("+1", fontSize = 10.sp, color = GrowthOrangeLight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Set Checkboxes / Badges & Add Set Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Marcação de Séries:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = CarbonDark,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Text(
                            text = "$completedSetsInSession/${exercise.sets}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (completedSetsInSession >= exercise.sets && exercise.sets > 0) GrowthGreen else GrowthOrange,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Excluir Série (-)
                    if (onRemoveSet != null && exercise.sets > 1) {
                        Surface(
                            color = CarbonDark,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .clickable { onRemoveSet() }
                                .testTag("remove_set_button_${exercise.id}")
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Excluir Série",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Botão Adicionar Série (+)
                    Surface(
                        color = GrowthOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onAddSet() }
                            .testTag("add_set_button_${exercise.id}")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Adicionar Série",
                                tint = GrowthOrange,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(exercise.sets) { setIdx ->
                    val isSetDone = (setIdx + 1) <= completedSetsInSession
                    val logged = loggedSets.getOrNull(setIdx)

                    Surface(
                        color = if (isSetDone) GrowthGreen.copy(alpha = 0.2f) else CarbonDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (isSetDone) GrowthGreen else CarbonBorder),
                        modifier = Modifier
                            .clickable {
                                if (isWorkoutRunning) {
                                    if (isSetDone && logged != null && onEditLoggedSet != null) {
                                        onEditLoggedSet(setIdx + 1, logged.first, logged.second)
                                    } else {
                                        onToggleSet(setIdx + 1)
                                    }
                                }
                            }
                            .testTag("set_item_${exercise.id}_${setIdx + 1}")
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
                                text = if (isSetDone && logged != null) "Série ${setIdx + 1} (${logged.first}x${logged.second.toInt()}k)" else "Série ${setIdx + 1}",
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

// Biomechanics & GIF Guide Modal
