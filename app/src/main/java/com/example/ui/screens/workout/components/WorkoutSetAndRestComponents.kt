package com.example.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorkoutExercise
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed

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
