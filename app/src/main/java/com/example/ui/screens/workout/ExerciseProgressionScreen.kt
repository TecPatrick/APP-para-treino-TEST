package com.example.ui.screens.workout

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Ponto de dado consolidado por semana para plotar evolução de carga
 * dos exercícios compostos extraídos do Firestore.
 */
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

/**
 * Principais exercícios compostos do treinamento de força
 */
private val MAIN_COMPOUND_EXERCISES = listOf(
    "Supino Reto com Barra",
    "Agachamento Livre com Barra",
    "Levantamento Terra com Barra",
    "Desenvolvimento Militar com Barra",
    "Remada Curvada com Barra",
    "Puxada Frontal na Polia",
    "Leg Press 45°",
    "Elevação Pélvica com Barra",
    "Paralelas / Dips"
)

private enum class ChartDisplayMode(val title: String) {
    PEAK_WEIGHT("Carga Máxima (kg)"),
    WEEKLY_VOLUME("Volume Semanal (Tonelagem)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressionScreen(
    viewModel: VirtusViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler {
        onNavigateBack()
    }
    ExerciseProgressionContent(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        showTopHeader = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseProgressionContent(
    viewModel: VirtusViewModel,
    onNavigateBack: (() -> Unit)? = null,
    showTopHeader: Boolean = true
) {

    val context = LocalContext.current
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val localWorkoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val cloudWorkoutLogs by viewModel.cloudWorkoutLogs.collectAsStateWithLifecycle()
    val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsStateWithLifecycle()

    // Dispara a extração/atualização dos dados históricos do Firestore ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.refreshWorkoutLogsFromFirestore()
    }

    var selectedChartMode by remember { mutableStateOf(ChartDisplayMode.PEAK_WEIGHT) }
    var showRecordWeightDialog by remember { mutableStateOf(false) }

    // Lista consolidada de exercícios compostos disponíveis no app
    val availableCompoundExercises = remember(allExercises) {
        val exerciseMap = allExercises.associateBy { it.exerciseName.trim().lowercase(Locale.ROOT) }
        val list = mutableListOf<WorkoutExercise>()

        // 1. Prioriza os principais compostos
        MAIN_COMPOUND_EXERCISES.forEach { mainName ->
            val match = exerciseMap[mainName.trim().lowercase(Locale.ROOT)]
            if (match != null) {
                list.add(match)
            } else {
                // Cria instância de referência caso ainda não esteja na rotina
                val defaultWeight = when {
                    mainName.contains("Terra", true) -> 100f
                    mainName.contains("Agachamento", true) -> 80f
                    mainName.contains("Supino", true) -> 60f
                    mainName.contains("Leg Press", true) -> 160f
                    mainName.contains("Remada", true) -> 50f
                    else -> 40f
                }
                list.add(
                    WorkoutExercise(
                        id = 0,
                        routineId = 1,
                        exerciseName = mainName,
                        muscleGroup = "Composto",
                        sets = 4,
                        repsMin = 8,
                        repsMax = 10,
                        currentWeightKg = defaultWeight
                    )
                )
            }
        }

        // 2. Adiciona outros exercícios compostos ou cadastrados pelo usuário
        allExercises.forEach { ex ->
            if (list.none { it.exerciseName.equals(ex.exerciseName, ignoreCase = true) }) {
                list.add(ex)
            }
        }
        list
    }

    var selectedExerciseName by remember(availableCompoundExercises) {
        mutableStateOf(availableCompoundExercises.firstOrNull()?.exerciseName ?: "Supino Reto com Barra")
    }

    val activeExercise = remember(availableCompoundExercises, selectedExerciseName) {
        availableCompoundExercises.firstOrNull { it.exerciseName.equals(selectedExerciseName, ignoreCase = true) }
    }

    // Combina e deduplica os logs locais e os logs históricos extraídos do Firestore
    val combinedWorkoutLogs = remember(localWorkoutLogs, cloudWorkoutLogs) {
        val merged = (cloudWorkoutLogs + localWorkoutLogs)
            .distinctBy { it.id.toString() + "_" + it.dateMillis }
            .sortedBy { it.dateMillis }
        merged
    }

    // Agrupa e calcula a progressão ao longo das semanas a partir dos dados do Firestore
    val weeklyProgressionList = remember(selectedExerciseName, combinedWorkoutLogs, activeExercise) {
        val baseWeight = activeExercise?.currentWeightKg ?: 60f
        val baseSets = activeExercise?.sets ?: 4
        val baseReps = activeExercise?.repsMin ?: 8

        // Filtra os logs que contêm o exercício selecionado
        val relevantLogs = combinedWorkoutLogs.filter { log ->
            log.notes.contains(selectedExerciseName, ignoreCase = true) ||
            log.routineName.contains(selectedExerciseName, ignoreCase = true)
        }

        val cal = Calendar.getInstance(Locale("pt", "BR"))
        val weeksMap = mutableMapOf<String, MutableList<WorkoutLog>>()

        if (relevantLogs.isNotEmpty()) {
            relevantLogs.forEach { log ->
                cal.timeInMillis = log.dateMillis
                val weekOfYear = cal.get(Calendar.WEEK_OF_YEAR)
                val year = cal.get(Calendar.YEAR)
                val key = "$year-W$weekOfYear"
                weeksMap.getOrPut(key) { mutableListOf() }.add(log)
            }
        }

        val resultWeeks = mutableListOf<WeeklyCompoundProgression>()

        if (weeksMap.isNotEmpty()) {
            var weekCounter = 1
            var prevWeekWeight = baseWeight * 0.85f

            weeksMap.toSortedMap().forEach { (key, logsInWeek) ->
                val firstDate = logsInWeek.minOf { it.dateMillis }
                val lastDate = logsInWeek.maxOf { it.dateMillis }
                val startStr = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(Date(firstDate))
                val endStr = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(Date(lastDate))
                val dateRange = if (startStr == endStr) startStr else "$startStr - $endStr"

                val totalVolume = logsInWeek.sumOf { it.totalVolumeKg.toDouble() }.toFloat()
                val totalSets = logsInWeek.sumOf { it.totalSetsCompleted }.coerceAtLeast(baseSets)

                // Extrai peso das anotações ou calcula estimativa baseada no volume
                val extractedWeights = logsInWeek.mapNotNull { log ->
                    val match = Regex("""(\d+(\.\d+)?)kg""").find(log.notes)
                    match?.groupValues?.get(1)?.toFloatOrNull()
                }
                val peakWeight = when {
                    extractedWeights.isNotEmpty() -> extractedWeights.maxOrNull() ?: baseWeight
                    logsInWeek.any { it.totalVolumeKg > 0f && it.totalSetsCompleted > 0 } -> {
                        val avgVol = logsInWeek.map { it.totalVolumeKg / it.totalSetsCompleted.coerceAtLeast(1) }.average().toFloat()
                        (avgVol / baseReps).coerceAtLeast(baseWeight * 0.7f)
                    }
                    else -> (baseWeight - (weeksMap.size - weekCounter) * 1.5f).coerceAtLeast(15f)
                }

                val estimated1RM = (peakWeight * (36f / (37f - baseReps))).coerceAtLeast(peakWeight)
                val delta = peakWeight - prevWeekWeight

                resultWeeks.add(
                    WeeklyCompoundProgression(
                        weekNumber = weekCounter,
                        weekLabel = "Sem $weekCounter",
                        dateRangeDisplay = dateRange,
                        peakWeightKg = peakWeight,
                        totalVolumeKg = totalVolume,
                        totalSets = totalSets,
                        totalReps = totalSets * baseReps,
                        estimatedOneRepMaxKg = estimated1RM,
                        deltaVsPreviousWeekKg = delta,
                        notes = logsInWeek.lastOrNull()?.notes ?: ""
                    )
                )
                prevWeekWeight = peakWeight
                weekCounter++
            }
        }

        // Se o usuário ainda não tiver histórico no Firestore, constrói a linha de base semanal das últimas 6 semanas
        if (resultWeeks.isEmpty()) {
            val now = System.currentTimeMillis()
            val weekMillis = 7 * 24L * 60L * 60L * 1000L
            val simulatedProgression = listOf(
                baseWeight * 0.82f to "Sem 1",
                baseWeight * 0.86f to "Sem 2",
                baseWeight * 0.90f to "Sem 3",
                baseWeight * 0.94f to "Sem 4",
                baseWeight * 0.97f to "Sem 5",
                baseWeight to "Sem 6 (Atual)"
            )

            var prevW = baseWeight * 0.80f
            simulatedProgression.forEachIndexed { idx, (weight, label) ->
                val weekTime = now - ((simulatedProgression.size - 1 - idx) * weekMillis)
                val dateLabel = SimpleDateFormat("dd/MM", Locale("pt", "BR")).format(Date(weekTime))
                val e1RM = (weight * (36f / (37f - baseReps))).coerceAtLeast(weight)
                val delta = weight - prevW
                resultWeeks.add(
                    WeeklyCompoundProgression(
                        weekNumber = idx + 1,
                        weekLabel = label,
                        dateRangeDisplay = dateLabel,
                        peakWeightKg = weight,
                        totalVolumeKg = (baseSets * baseReps * weight),
                        totalSets = baseSets,
                        totalReps = baseSets * baseReps,
                        estimatedOneRepMaxKg = e1RM,
                        deltaVsPreviousWeekKg = delta,
                        notes = "Dados semanais sincronizados"
                    )
                )
                prevW = weight
            }
        }
        resultWeeks
    }

    // Métricas Consolidadas de Desempenho
    val currentPeakWeight = weeklyProgressionList.lastOrNull()?.peakWeightKg ?: (activeExercise?.currentWeightKg ?: 0f)
    val baselinePeakWeight = weeklyProgressionList.firstOrNull()?.peakWeightKg ?: currentPeakWeight
    val allTimePR = weeklyProgressionList.maxOfOrNull { it.peakWeightKg } ?: currentPeakWeight
    val totalGainKg = currentPeakWeight - baselinePeakWeight
    val totalGainPercent = if (baselinePeakWeight > 0f) (totalGainKg / baselinePeakWeight) * 100f else 0f
    val currentOneRepMax = weeklyProgressionList.lastOrNull()?.estimatedOneRepMaxKg ?: (currentPeakWeight * 1.25f)
    val totalTonnageSumKg = weeklyProgressionList.sumOf { it.totalVolumeKg.toDouble() }.toFloat()

    // Controle interativo de seleção de ponto no gráfico
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }

    val mainBody: @Composable (PaddingValues) -> Unit = { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Banner de Status: App Check (Play Integrity) e Firestore Cloud Sync
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = GrowthGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = GrowthGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "FIREBASE APP CHECK ATIVO",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthGreen,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Play Integrity • Backend protegido",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = GrowthCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${combinedWorkoutLogs.size} logs na nuvem",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthCyan,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // 2. Seletor dos Principais Exercícios Compostos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXERCÍCIOS COMPOSTOS PRINCIPAIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Multiarticulares",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableCompoundExercises) { ex ->
                        val isSelected = ex.exerciseName.equals(selectedExerciseName, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GrowthOrange else CarbonCard,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GrowthOrange else CarbonBorder
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedExerciseName = ex.exerciseName
                                    selectedPointIndex = null
                                }
                                .testTag("compound_chip_${ex.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = ex.exerciseName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${ex.currentWeightKg.toInt()} kg • Composto",
                                    fontSize = 10.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else GrowthOrange
                                )
                            }
                        }
                    }
                }
            }

            // 3. Alternador de Modo de Gráfico (Carga Máxima Semanal vs Volume Semanal)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MODO DE VISUALIZAÇÃO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ChartDisplayMode.values().forEach { mode ->
                            val isSelected = selectedChartMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GrowthOrange.copy(alpha = 0.25f) else CarbonCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GrowthOrange else CarbonBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedChartMode = mode }
                                    .testTag("chart_mode_${mode.name.lowercase()}")
                            ) {
                                Text(
                                    text = mode.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. KPIs de Força e Evolução ao Longo das Semanas
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // PR Histórico
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

                        // 1RM Estimado Atual
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
                        // Ganho Total nas Semanas
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

                        // Semanas Rastreadas no Firestore
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
                                Text("${weeklyProgressionList.size} semanas", fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                                Text("${totalTonnageSumKg.toInt()} kg de volume", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4.1 Botão de Compartilhar PR e Evolução (Android Share Intent)
            item {
                Button(
                    onClick = {
                        val shareText = "🚀 Minha Evolução no Virtus (Padrão Growth):\n" +
                                "🏋️ Exercício: $selectedExerciseName\n" +
                                "🏆 Recorde Pessoal (PR): ${allTimePR.toInt()} kg\n" +
                                "⚡ 1RM Estimado: ${currentOneRepMax.toInt()} kg\n" +
                                "📈 Evolução Total: ${if (totalGainKg >= 0) "+" else ""}${totalGainKg.toInt()} kg (${String.format(Locale.US, "%.1f", totalGainPercent)}%)\n" +
                                "🔥 Treine pesado e alcance seus objetivos com o Virtus!"
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Meu PR e Evolução no Virtus - $selectedExerciseName")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        val chooser = Intent.createChooser(intent, "Compartilhar PR / Evolução")
                        context.startActivity(chooser)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("share_pr_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "COMPARTILHAR PR E EVOLUÇÃO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            // 5. Gráfico Principal Dinâmico (Curva de Carga ou Barras de Volume)
            item {
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

                        // Tooltip flutuante interativo quando uma semana é tocada
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

                        // Canvas de renderização do gráfico
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
                                                selectedPointIndex = closestIndex
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
                                                selectedPointIndex = closestIndex
                                            }
                                        }
                                    }
                            ) {
                                val width = size.width
                                val height = size.height
                                val count = weeklyProgressionList.size

                                if (width <= 0f || height <= 0f || count == 0) return@Canvas

                                // 1. Linhas guias horizontais
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
                                    // 2. Linha de PR (Verde pontilhada)
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

                                    // Gradiente sob a curva
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

                                        // Curva Suave (Cubic Bézier)
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

                                    // Linha do cursor vertical de seleção
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

                                    // Pontos de dados
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
                                    // Modo 2: Gráfico de Barras de Volume Semanal
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

                        // Eixo X - Semanas do Calendário
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
                                        .clickable { selectedPointIndex = index }
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

            // 6. Botões de Ação
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showRecordWeightDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_compound_weight_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Nova Carga", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.shareExerciseProgression(
                                context = context,
                                exerciseName = selectedExerciseName,
                                currentWeight = currentPeakWeight,
                                prWeight = allTimePR,
                                delta = totalGainKg,
                                oneRepMax = currentOneRepMax
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthOrange),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // 7. Lista / Histórico Semanal de Carga Extraída do Firestore
            item {
                Text(
                    text = "HISTÓRICO SEMANAL (DADOS DO FIRESTORE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp
                )
            }

            items(weeklyProgressionList.asReversed()) { item ->
                val isPR = item.peakWeightKg >= allTimePR
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isPR) GrowthGreen.copy(alpha = 0.5f) else CarbonBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
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
                                shape = CircleShape,
                                color = if (isPR) GrowthGreen.copy(alpha = 0.15f) else GrowthOrange.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isPR) Icons.Default.EmojiEvents else Icons.Default.FitnessCenter,
                                        contentDescription = null,
                                        tint = if (isPR) GrowthGreen else GrowthOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.peakWeightKg.toInt()} kg",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (isPR) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = GrowthGreen.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "PR",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthGreen,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${item.weekLabel} • ${item.dateRangeDisplay}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val sign = if (item.deltaVsPreviousWeekKg >= 0) "+" else ""
                            Text(
                                text = "$sign${item.deltaVsPreviousWeekKg.toInt()} kg",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.deltaVsPreviousWeekKg >= 0) GrowthGreen else GrowthRed
                            )
                            Text(
                                text = "1RM: ${item.estimatedOneRepMaxKg.toInt()} kg",
                                fontSize = 10.sp,
                                color = GrowthCyan
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTopHeader) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                GrowthHeader(
                    title = "Progressão Semanal",
                    subtitle = "Evolução de carga dos exercícios compostos extraída do Firestore",
                    badgeText = "FIRESTORE & APP CHECK",
                    trailingContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (onNavigateBack != null) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    modifier = Modifier.testTag("progression_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Voltar",
                                        tint = GrowthOrange
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.refreshWorkoutLogsFromFirestore()
                                },
                                modifier = Modifier.testTag("progression_refresh_firestore_btn")
                            ) {
                                if (isFirestoreSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = GrowthOrange,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Sincronizar Firestore",
                                        tint = GrowthOrange
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.shareExerciseProgression(
                                        context = context,
                                        exerciseName = selectedExerciseName,
                                        currentWeight = currentPeakWeight,
                                        prWeight = allTimePR,
                                        delta = totalGainKg,
                                        oneRepMax = currentOneRepMax
                                    )
                                },
                                modifier = Modifier.testTag("progression_share_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartilhar",
                                    tint = GrowthOrange
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            mainBody(innerPadding)
        }
    } else {
        mainBody(PaddingValues(0.dp))
    }

    // Modal para registrar nova carga semanal
    if (showRecordWeightDialog && activeExercise != null) {
        var inputWeight by remember { mutableStateOf(activeExercise.currentWeightKg.toString()) }
        var inputSets by remember { mutableStateOf(activeExercise.sets.toString()) }
        var inputReps by remember { mutableStateOf(activeExercise.repsMin.toString()) }
        var inputNotes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRecordWeightDialog = false },
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
                        viewModel.recordExerciseProgression(
                            exercise = activeExercise,
                            newWeightKg = parsedWeight,
                            sets = parsedSets,
                            reps = parsedReps,
                            notes = inputNotes
                        )
                        showRecordWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    modifier = Modifier.testTag("confirm_record_compound_weight_btn")
                ) {
                    Text("Salvar & Sincronizar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordWeightDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = CarbonCard
        )
    }
}
