package com.example.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExercise
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import java.util.Locale
import kotlin.math.roundToInt

data class WeeklyCompoundProgression(
    val weekNumber: Int,
    val weekLabel: String,
    val dateRangeDisplay: String,
    val peakWeightKg: Float,
    val totalVolumeKg: Float,
    val totalSets: Int,
    val totalReps: Int,
    val estimatedOneRepMaxKg: Float,
    val deltaVsPreviousWeekKg: Float = 0f,
    val notes: String = ""
)

enum class ChartDisplayMode(val title: String) {
    PEAK_WEIGHT("Carga Máxima (kg)"),
    WEEKLY_VOLUME("Volume Semanal (Tonelagem)")
}

@Composable
fun ProgressionKpiCardsSection(
    allTimePR: Float,
    currentOneRepMax: Float,
    totalGainKg: Float,
    totalGainPercent: Float,
    trackedWeeksCount: Int,
    totalTonnageSumKg: Float
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("RECORDE (PR)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${allTimePR.toInt()} kg", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                    Text("Pico registrado na nuvem", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("1RM ESTIMADO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${currentOneRepMax.toInt()} kg", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                    Text("Fórmula de Brzycki", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.35f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("EVOLUÇÃO TOTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                        Icon(
                            imageVector = if (totalGainKg >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (totalGainKg >= 0) GrowthGreen else GrowthRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val sign = if (totalGainKg >= 0) "+" else ""
                    Text(
                        text = "$sign${totalGainKg.toInt()} kg (${String.format(Locale.US, "%.1f", totalGainPercent)}%)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (totalGainKg >= 0) GrowthGreen else GrowthRed
                    )
                    Text("Ao longo das semanas", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SEMANAS RASTREADAS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$trackedWeeksCount semanas", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                    Text("${totalTonnageSumKg.toInt()} kg de volume", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun CompoundProgressionChartCard(
    selectedExerciseName: String,
    selectedChartMode: ChartDisplayMode,
    currentPeakWeight: Float,
    allTimePR: Float,
    weeklyProgressionList: List<WeeklyCompoundProgression>,
    selectedPointIndex: Int?,
    onSelectPointIndex: (Int?) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("compound_progression_chart_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = selectedExerciseName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (selectedChartMode == ChartDisplayMode.PEAK_WEIGHT)
                            "Carga Máxima Semanal (kg) ao Longo das Semanas"
                        else
                            "Volume Total de Treino Semanal (kg)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GrowthOrange.copy(alpha = 0.20f),
                    border = BorderStroke(1.dp, GrowthOrange)
                ) {
                    Text(
                        text = "${currentPeakWeight.toInt()} kg atual",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrange,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            selectedPointIndex?.let { idx ->
                val pt = weeklyProgressionList.getOrNull(idx)
                if (pt != null) {
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pt.weekLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pt.peakWeightKg.toInt()} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Volume Semanal", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${pt.totalVolumeKg.toInt()} kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("1RM Estimado", fontSize = 10.sp, color = GrowthCyan)
                                Text("${pt.estimatedOneRepMaxKg.toInt()} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                            }
                        }
                    }
                }
            }

            val weights = weeklyProgressionList.map { it.peakWeightKg }
            val minWeight = (weights.minOrNull() ?: 20f) - 5f
            val maxWeight = (weights.maxOrNull() ?: 100f) + 5f
            val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

            val volumes = weeklyProgressionList.map { it.totalVolumeKg }
            val maxVolume = (volumes.maxOrNull() ?: 1000f).coerceAtLeast(100f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(weeklyProgressionList) {
                            detectTapGestures { offset ->
                                val w = size.width
                                val count = weeklyProgressionList.size
                                if (count > 0) {
                                    val spacing = if (count > 1) w / (count - 1) else w / 2
                                    val closestIndex = if (count == 1) 0 else {
                                        ((offset.x / spacing).roundToInt()).coerceIn(0, count - 1)
                                    }
                                    onSelectPointIndex(closestIndex)
                                }
                            }
                        }
                        .pointerInput(weeklyProgressionList) {
                            detectDragGestures { change, _ ->
                                val w = size.width
                                val count = weeklyProgressionList.size
                                if (count > 0) {
                                    val spacing = if (count > 1) w / (count - 1) else w / 2
                                    val closestIndex = if (count == 1) 0 else {
                                        ((change.position.x / spacing).roundToInt()).coerceIn(0, count - 1)
                                    }
                                    onSelectPointIndex(closestIndex)
                                }
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val count = weeklyProgressionList.size

                    if (width <= 0f || height <= 0f || count == 0) return@Canvas

                    val gridLineSteps = 4
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    for (i in 0..gridLineSteps) {
                        val y = height * (i.toFloat() / gridLineSteps)
                        drawLine(
                            color = CarbonBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = dashedEffect
                        )
                    }

                    if (selectedChartMode == ChartDisplayMode.PEAK_WEIGHT) {
                        val prNormalizedY = (allTimePR - minWeight) / weightRange
                        val prY = height - (prNormalizedY * height).coerceIn(10f, height - 10f)
                        drawLine(
                            color = GrowthGreen.copy(alpha = 0.6f),
                            start = Offset(0f, prY),
                            end = Offset(width, prY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), 0f)
                        )

                        val pointSpacing = if (count > 1) width / (count - 1) else width / 2
                        val points = weeklyProgressionList.mapIndexed { index, pt ->
                            val x = if (count > 1) index * pointSpacing else width / 2
                            val normalizedY = (pt.peakWeightKg - minWeight) / weightRange
                            val y = height - (normalizedY * height).coerceIn(16f, height - 16f)
                            Offset(x, y)
                        }

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
                                    colors = listOf(
                                        GrowthOrange.copy(alpha = 0.38f),
                                        GrowthOrange.copy(alpha = 0.08f),
                                        Color.Transparent
                                    )
                                )
                            )

                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlX1 = p0.x + (p1.x - p0.x) / 2
                                    val controlY1 = p0.y
                                    val controlX2 = p0.x + (p1.x - p0.x) / 2
                                    val controlY2 = p1.y
                                    cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = GrowthOrange,
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        selectedPointIndex?.let { selIdx ->
                            if (selIdx in points.indices) {
                                val selPt = points[selIdx]
                                drawLine(
                                    color = GrowthOrange.copy(alpha = 0.7f),
                                    start = Offset(selPt.x, 0f),
                                    end = Offset(selPt.x, height),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                                drawCircle(
                                    color = GrowthOrange.copy(alpha = 0.25f),
                                    radius = 12.dp.toPx(),
                                    center = selPt
                                )
                            }
                        }

                        points.forEachIndexed { idx, pt ->
                            val isSelected = selectedPointIndex == idx
                            val isPR = weeklyProgressionList[idx].peakWeightKg >= allTimePR
                            val dotColor = if (isPR) GrowthGreen else GrowthOrange

                            drawCircle(
                                color = dotColor,
                                radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 3.5.dp.toPx() else 2.5.dp.toPx(),
                                center = pt
                            )
                        }
                    } else {
                        val barWidth = (width / count) * 0.55f
                        val slotWidth = width / count

                        weeklyProgressionList.forEachIndexed { index, pt ->
                            val isSelected = selectedPointIndex == index
                            val normVol = pt.totalVolumeKg / maxVolume
                            val barHeight = (normVol * height * 0.85f).coerceAtLeast(8f)
                            val x = (index * slotWidth) + (slotWidth - barWidth) / 2
                            val y = height - barHeight

                            drawRoundRect(
                                color = if (isSelected) GrowthOrange else GrowthOrange.copy(alpha = 0.6f),
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weeklyProgressionList.forEachIndexed { index, pt ->
                    val isSelected = selectedPointIndex == index
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectPointIndex(index) }
                    ) {
                        Text(
                            text = "${pt.peakWeightKg.toInt()}kg",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = pt.weekLabel,
                            fontSize = 9.sp,
                            color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecordCompoundWeightDialog(
    activeExercise: WorkoutExercise,
    onDismiss: () -> Unit,
    onConfirm: (newWeightKg: Float, sets: Int, reps: Int, notes: String) -> Unit
) {
    var inputWeight by remember { mutableStateOf(activeExercise.currentWeightKg.toString()) }
    var inputSets by remember { mutableStateOf(activeExercise.sets.toString()) }
    var inputReps by remember { mutableStateOf(activeExercise.repsMin.toString()) }
    var inputNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Registrar Carga do Composto",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = activeExercise.exerciseName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrange
                )

                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { inputWeight = it },
                    label = { Text("Carga Máxima (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        focusedLabelColor = GrowthOrange
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_new_compound_weight_kg")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputSets,
                        onValueChange = { inputSets = it },
                        label = { Text("Séries") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = inputReps,
                        onValueChange = { inputReps = it },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = inputNotes,
                    onValueChange = { inputNotes = it },
                    label = { Text("Observações (ex: RPE 9, com cinto)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedWeight = inputWeight.toFloatOrNull() ?: activeExercise.currentWeightKg
                    val parsedSets = inputSets.toIntOrNull() ?: activeExercise.sets
                    val parsedReps = inputReps.toIntOrNull() ?: activeExercise.repsMin
                    onConfirm(parsedWeight, parsedSets, parsedReps, inputNotes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_record_compound_weight_btn")
            ) {
                Text("Salvar & Sincronizar", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = CarbonCard
    )
}
