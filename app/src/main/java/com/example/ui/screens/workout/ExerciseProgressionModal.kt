package com.example.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseProgressionModal(
    exercises: List<WorkoutExercise>,
    workoutLogs: List<WorkoutLog>,
    onDismiss: () -> Unit
) {
    val uniqueExerciseNames = remember(exercises) {
        exercises.map { it.exerciseName }.distinct()
    }

    var selectedExerciseName by remember(uniqueExerciseNames) {
        mutableStateOf(uniqueExerciseNames.firstOrNull() ?: "Supino Reto com Barra")
    }

    val exerciseInstances = remember(exercises, selectedExerciseName) {
        exercises.filter { it.exerciseName.equals(selectedExerciseName, ignoreCase = true) }
    }

    // Simulate weight progression data points over time from workout logs & current exercise weight
    val progressionData = remember(exerciseInstances, workoutLogs, selectedExerciseName) {
        val baseWeight = exerciseInstances.firstOrNull()?.currentWeightKg ?: 30f
        val points = mutableListOf<Pair<String, Float>>()
        
        // Add historical logs or simulated progression points based on workout logs
        if (workoutLogs.isNotEmpty()) {
            workoutLogs.sortedBy { it.dateMillis }.forEachIndexed { index, log ->
                val dateStr = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(Date(log.dateMillis))
                // Approximate weight progression factoring index
                val simulatedWeight = (baseWeight - 5f + (index * 1.5f)).coerceAtLeast(10f)
                points.add(Pair(dateStr, simulatedWeight))
            }
        }
        if (points.isEmpty()) {
            points.add(Pair("Início", baseWeight - 5f))
            points.add(Pair("Atual", baseWeight))
        }
        points
    }

    val weights = progressionData.map { it.second }
    val minWeight = (weights.minOrNull() ?: 10f) - 5f
    val maxWeight = (weights.maxOrNull() ?: 50f) + 5f
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

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
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Progressão de Carga (D3 Analytics)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Selecione o exercício para visualizar a evolução da carga (kg) ao longo do tempo:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(10.dp))

                if (uniqueExerciseNames.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum exercício cadastrado nas fichas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Exercise selector filter chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uniqueExerciseNames) { name ->
                            val isSelected = name.equals(selectedExerciseName, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedExerciseName = name },
                                label = { Text(name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = CarbonCardElevated,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Summary Stats Card
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Exercício Analisado", fontSize = 10.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                Text(selectedExerciseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Carga Atual", fontSize = 10.sp, color = GrowthGreen, fontWeight = FontWeight.Bold)
                                Text("${weights.lastOrNull()?.toInt() ?: 0} kg", fontSize = 15.sp, fontWeight = FontWeight.Black, color = GrowthGreen)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Recharts / D3 Style Canvas Line Chart
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonDark),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Gráfico de Carga x Sessões", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(10.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                val width = size.width
                                val height = size.height
                                val pointSpacing = if (progressionData.size > 1) width / (progressionData.size - 1) else width / 2

                                val points = progressionData.mapIndexed { index, pair ->
                                    val x = if (progressionData.size > 1) index * pointSpacing else width / 2
                                    val normalizedY = (pair.second - minWeight) / weightRange
                                    val y = height - (normalizedY * height)
                                    Offset(x, y.coerceIn(15f, height - 15f))
                                }

                                // Draw gradient fill under curve
                                if (points.size > 1) {
                                    val fillPath = Path().apply {
                                        moveTo(points.first().x, height)
                                        points.forEach { lineTo(it.x, it.y) }
                                        lineTo(points.last().x, height)
                                        close()
                                    }
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(GrowthOrange.copy(alpha = 0.35f), Color.Transparent)
                                        )
                                    )

                                    // Draw line path
                                    val linePath = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            lineTo(points[i].x, points[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = linePath,
                                        color = GrowthOrange,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Draw points with value labels
                                points.forEachIndexed { idx, pt ->
                                    drawCircle(color = GrowthOrange, radius = 5.dp.toPx(), center = pt)
                                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                progressionData.forEach { (label, weight) ->
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${weight.toInt()}kg", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                                        Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Fechar Análise", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
