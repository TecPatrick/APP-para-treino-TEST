package com.example.ui.screens.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.WorkoutViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.components.WeightProgressionChart
import com.example.ui.screens.analytics.components.AddMeasurementDialog
import com.example.ui.screens.analytics.components.AnalyticsPhotoComparisonCard
import com.example.ui.screens.analytics.components.ProgressLogItemCard
import com.example.ui.screens.workout.ExerciseProgressionContent
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class HistoryPeriod(val title: String, val pattern: String) {
    DAY("Dia", "yyyy-MM-dd"), MONTH("Mês", "yyyy-MM"), YEAR("Ano", "yyyy")
}

@Immutable
private data class ActivityHistoryBucket(
    val key: String,
    val caloriesConsumed: Int,
    val foodRecords: Int,
    val workouts: Int,
    val cardioSessions: Int,
    val exerciseCalories: Int,
    val waterMl: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: WorkoutViewModel,
    onNavigateToProgression: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val foodRecords by viewModel.allFoodRecords.collectAsStateWithLifecycle()
    val cardioLogs by viewModel.allCardioLogs.collectAsStateWithLifecycle()
    val gpsLogs by viewModel.allGpsRouteLogs.collectAsStateWithLifecycle()
    val intakeHistory by viewModel.recentIntakeHistory.collectAsStateWithLifecycle()

    var showAddMeasurementDialog by remember { mutableStateOf(false) }
    var historyPeriod by remember { mutableStateOf(HistoryPeriod.DAY) }

    val sortedLogs = remember(progressLogs) { progressLogs.sortedBy { it.dateMillis } }
    val descendingLogs = remember(progressLogs) { progressLogs.sortedByDescending { it.dateMillis } }
    val logsWithPhotos = remember(sortedLogs) { sortedLogs.filter { it.photoUri.isNotBlank() } }

    val initialWeight = sortedLogs.firstOrNull()?.weightKg ?: profile.weightKg
    val currentWeight = sortedLogs.lastOrNull()?.weightKg ?: profile.weightKg
    val weightDelta = currentWeight - initialWeight

    val totalVolumeLifted = remember(workoutLogs) {
        workoutLogs.sumOf { it.totalVolumeKg.toDouble() }.toFloat()
    }

    val activityHistory = remember(historyPeriod, foodRecords, workoutLogs, cardioLogs, gpsLogs, intakeHistory, profile.weightKg) {
        val formatter = SimpleDateFormat(historyPeriod.pattern, Locale.US)
        fun key(time: Long) = formatter.format(Date(time))
        val keys = (foodRecords.map { key(it.dateMillis) } + workoutLogs.map { key(it.dateMillis) } +
            cardioLogs.map { key(it.dateMillis) } + gpsLogs.map { key(it.dateMillis) } +
            intakeHistory.map { key(it.dateMillis) }).distinct().sortedDescending()
        keys.map { bucketKey ->
            val foods = foodRecords.filter { key(it.dateMillis) == bucketKey }
            val workoutsForPeriod = workoutLogs.filter { key(it.dateMillis) == bucketKey }
            val cardioForPeriod = cardioLogs.filter { key(it.dateMillis) == bucketKey }
            val gpsForPeriod = gpsLogs.filter { key(it.dateMillis) == bucketKey }
            val water = intakeHistory.filter { key(it.dateMillis) == bucketKey }.sumOf { it.waterDrankMl }
            val gymCalories = workoutsForPeriod.sumOf {
                (it.durationMinutes.coerceAtLeast(1) * (profile.weightKg * 0.08f)).toInt().coerceIn(80, 800)
            }
            ActivityHistoryBucket(
                key = bucketKey,
                caloriesConsumed = foods.sumOf { it.calories.toDouble() }.toInt(),
                foodRecords = foods.size,
                workouts = workoutsForPeriod.size,
                cardioSessions = cardioForPeriod.size + gpsForPeriod.size,
                exerciseCalories = gymCalories + cardioForPeriod.sumOf { it.caloriesBurned } + gpsForPeriod.sumOf { it.caloriesBurned.toDouble() }.toInt(),
                waterMl = water
            )
        }.take(24)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = "Análise de Desempenho",
                subtitle = "Evolução antropométrica, volume e relatório PDF",
                badgeText = "EVOLUÇÃO & ANALYTICS",
                trailingContent = {
                    IconButton(
                        onClick = { viewModel.exportPdfProgressReport(context) },
                        modifier = Modifier.testTag("analytics_export_pdf_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = GrowthOrange
                        )
                    }
                }
            )

            var selectedAnalyticsTab by remember { mutableIntStateOf(0) }

            // Segmented Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(CarbonCardElevated, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    onClick = { selectedAnalyticsTab = 0 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedAnalyticsTab == 0) GrowthOrange else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_compound_progression")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = if (selectedAnalyticsTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Cargas dos Compostos",
                            fontSize = 12.sp,
                            fontWeight = if (selectedAnalyticsTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedAnalyticsTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    onClick = { selectedAnalyticsTab = 1 },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedAnalyticsTab == 1) GrowthOrange else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_measurements_habits")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = if (selectedAnalyticsTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Medidas & Hábitos",
                            fontSize = 12.sp,
                            fontWeight = if (selectedAnalyticsTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedAnalyticsTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (selectedAnalyticsTab == 0) {
                ExerciseProgressionContent(
                    viewModel = viewModel,
                    showTopHeader = false
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
                ) {
                    item(key = "analytics_pdf_hero", contentType = "pdf_hero") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(GrowthOrange.copy(alpha = 0.15f), Color.Transparent)
                                        )
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "RELATÓRIO DE PROGRESSO EM PDF",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthOrangeLight,
                                                letterSpacing = 1.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Documento Completo de Evolução",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = null,
                                            tint = GrowthOrange,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Gera um relatório profissional diagramado em PDF contendo diagnóstico de macros, cardápio detalhado, divisão de treinos, lista de compras e histórico de medidas.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Button(
                                        onClick = { viewModel.exportPdfProgressReport(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("export_pdf_progress_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EXPORTAR / COMPARTILHAR RELATÓRIO PDF",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item(key = "analytics_metrics_row", contentType = "metrics_row") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricCard(
                                label = "Variação de Peso",
                                value = "${if (weightDelta >= 0) "+" else ""}${String.format("%.1f", weightDelta)} kg",
                                subtext = "Desde o início",
                                valueColor = if (weightDelta >= 0) GrowthGreen else GrowthCyan,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = "Volume Total",
                                value = "${(totalVolumeLifted / 1000f).toInt()} tons",
                                subtext = "${workoutLogs.size} treinos feitos",
                                valueColor = GrowthOrange,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = "Aderência",
                                value = "94%",
                                subtext = "Consistência macros",
                                valueColor = GrowthCyan,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item(key = "analytics_activity_history", contentType = "activity_history") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "HISTÓRICO DE ATIVIDADES",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthOrangeLight,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HistoryPeriod.values().forEach { period ->
                                        FilterChip(
                                            selected = historyPeriod == period,
                                            onClick = { historyPeriod = period },
                                            label = { Text(period.title) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                if (activityHistory.isEmpty()) {
                                    Text(
                                        "Nenhuma refeição ou atividade registrada ainda.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    activityHistory.forEach { item ->
                                        Surface(
                                            color = CarbonCardElevated,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(item.key, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                                Text("Alimentação: ${item.caloriesConsumed} kcal • ${item.foodRecords} registros", fontSize = 11.sp, color = GrowthCyan)
                                                Text("Musculação: ${item.workouts} • Cardio/corrida: ${item.cardioSessions} • Gasto: ${item.exerciseCalories} kcal", fontSize = 11.sp, color = GrowthOrangeLight)
                                                if (item.waterMl > 0) Text("Água: ${item.waterMl} ml", fontSize = 11.sp, color = GrowthCyan)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "analytics_progression_card", contentType = "progression_card") {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("analytics_progression_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = GrowthOrange,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "PROGRESSÃO DE CARGAS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = GrowthOrange.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "GRÁFICOS D3",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GrowthOrange,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Curva de Carga & Força dos Exercícios",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                Text(
                                    text = "Acompanhe a evolução de peso (kg), 1RM estimado e recordes pessoais (PRs) dos principais exercícios ao longo do tempo com gráficos interativos.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = onNavigateToProgression,
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("analytics_view_exercise_progression_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Visualizar Gráficos de Carga",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    item(key = "analytics_weight_chart", contentType = "weight_chart") {
                        WeightProgressionChart(
                            logs = progressLogs,
                            targetWeightKg = profile.monthlyTargetWeightKg,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    item(key = "analytics_photos_and_add", contentType = "photos_and_add") {
                        AnalyticsPhotoComparisonCard(logsWithPhotos = logsWithPhotos)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "HISTÓRICO DE MEDIDAS & PESAGENS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrangeLight,
                                letterSpacing = 1.sp
                            )

                            OutlinedButton(
                                onClick = { showAddMeasurementDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_measurement_btn")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = GrowthCyan,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Nova Medição",
                                    fontSize = 11.sp,
                                    color = GrowthCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    items(
                        items = descendingLogs,
                        key = { it.id },
                        contentType = { "progress_log" }
                    ) { log ->
                        ProgressLogItemCard(
                            log = log,
                            onDelete = { viewModel.deleteProgressLog(it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddMeasurementDialog) {
        AddMeasurementDialog(
            currentWeight = profile.weightKg,
            onDismiss = { showAddMeasurementDialog = false },
            onConfirm = { weight, bf, chest, waist, arm, thigh, notes, photoUri ->
                viewModel.addProgressLog(weight, bf, chest, waist, arm, thigh, notes, photoUri)
                showAddMeasurementDialog = false
            }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
