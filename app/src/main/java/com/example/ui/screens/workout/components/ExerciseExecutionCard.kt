package com.example.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExercise
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.util.ExerciseVisualGuide

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
                        onClick = { onUpdateWeight((exercise.currentWeightKg - 1f).coerceAtLeast(0f)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("-1", fontSize = 10.sp, color = GrowthOrangeLight)
                    }

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
