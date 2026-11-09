package com.example.ui.screens.dashboard

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.ui.DietViewModel
import com.example.ui.components.CircularCalorieGauge
import com.example.ui.components.DailyHistoryHeader
import com.example.ui.components.DailyHistorySummary
import com.example.ui.components.GrowthHeader
import com.example.ui.components.MacroProgressBar
import com.example.ui.components.WeightProgressionChart
import com.example.ui.screens.achievements.components.AchievementsSummaryBannerCard
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow
import com.example.util.MacroCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DietViewModel,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReceipts: () -> Unit = {},
    onNavigateToGps: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val achievementsSummary by viewModel.achievementsState.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlanItems.collectAsStateWithLifecycle()
    val foodRecords by viewModel.allFoodRecords.collectAsStateWithLifecycle()
    val workoutRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val waterDrank by viewModel.waterDrankMl.collectAsStateWithLifecycle()
    val dailyEnergy by viewModel.dailyEnergyExpenditure.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var newWaterGoalInput by remember { mutableStateOf("") }

    // The selected date is shared across the whole app.
    val selectedDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(selectedDateMillis))
    val todayFood = foodRecords.filter { it.dateString == selectedDateKey }
    val consumedCalories = todayFood.sumOf { it.calories.toDouble() }.toFloat()
    val consumedProtein = todayFood.sumOf { it.protein.toDouble() }.toFloat()
    val consumedCarbs = todayFood.sumOf { it.carbs.toDouble() }.toFloat()
    val consumedFat = todayFood.sumOf { it.fat.toDouble() }.toFloat()

    val todayRoutine = workoutRoutines.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item(key = "dashboard_header", contentType = "header") {
            val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")).format(Date(selectedDateMillis))
            GrowthHeader(
                title = "Olá, ${profile.name}",
                subtitle = dateStr.replaceFirstChar { it.uppercase() },
                badgeText = "⚡ ${achievementsSummary.activeTitleText.uppercase()}",
                trailingContent = {
                    Row {
                        IconButton(
                            onClick = onNavigateToAchievements,
                            modifier = Modifier.size(44.dp).testTag("achievements_header_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Conquistas e Títulos Anime",
                                tint = GrowthYellow
                            )
                        }
                        IconButton(
                            onClick = { showMoreDialog = true },
                            modifier = Modifier.size(44.dp).testTag("more_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Mais opções",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = onNavigateToProfile,
                            modifier = Modifier.size(44.dp).testTag("profile_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Abrir perfil e conta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }

        // --- Banner Promocional Fitness Dark Premium (Topo da Tela Inicial) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("home_promo_banner"),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            GrowthOrange.copy(alpha = 0.65f),
                            GrowthCyan.copy(alpha = 0.55f)
                        )
                    )
                ),
                colors = CardDefaults.cardColors(containerColor = CarbonCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_virtus_top_banner),
                        contentDescription = "Banner promocional Virtus Fitness Dark Premium",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xF0080B10),
                                        Color(0xCC080B10),
                                        Color(0x40080B10)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = GrowthOrange.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "ALTA PERFORMANCE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = GrowthOrangeLight,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                color = GrowthCyan.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "DIETA & TREINO",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthCyan,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.fillMaxWidth(0.76f)) {
                            Text(
                                text = "Domine sua rotina com precisão científica",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = todayRoutine?.let { "Treino ${it.letter}: ${it.title} • Meta ${profile.targetCalories} kcal" }
                                    ?: "Controle de cargas, macros e evolução diária em tempo real",
                                fontSize = 11.sp,
                                color = Color(0xFFD5DCE6),
                                maxLines = 2
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onNavigateToWorkout,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrowthOrange,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(34.dp).testTag("promo_workout_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Treinar Agora", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onNavigateToDiet,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan),
                                modifier = Modifier.height(34.dp).testTag("promo_diet_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restaurant,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Plano Alimentar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Resumo de Conquistas & Patente Anime ---
        item(key = "dashboard_achievements_summary", contentType = "achievements_banner") {
            AchievementsSummaryBannerCard(
                summary = achievementsSummary,
                onOpenGallery = onNavigateToAchievements,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // --- Diário & Calendário ---
        item {
            val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
            val cardioLogs by viewModel.allCardioLogs.collectAsStateWithLifecycle()
            val gpsLogs by viewModel.allGpsRouteLogs.collectAsStateWithLifecycle()
            val intakeHistory by viewModel.recentIntakeHistory.collectAsStateWithLifecycle()

            val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
            fun sameSelectedDay(time: Long): Boolean = dateFormatter.format(Date(time)) == selectedDateKey
            val selectedWorkouts = workoutLogs.filter { sameSelectedDay(it.dateMillis) }
            val selectedCardio = cardioLogs.filter { sameSelectedDay(it.dateMillis) }
            val selectedRuns = gpsLogs.filter { sameSelectedDay(it.dateMillis) }
            val selectedIntake = intakeHistory.firstOrNull { it.dateString == selectedDateKey }
            val selectedProgress = progressLogs.filter { sameSelectedDay(it.dateMillis) }.maxByOrNull { it.dateMillis }
            val gymCalories = selectedWorkouts.sumOf {
                (it.durationMinutes.coerceAtLeast(1) * (profile.weightKg * 0.08f)).toInt().coerceIn(80, 800)
            }
            val selectedExerciseCalories = gymCalories + selectedCardio.sumOf { it.caloriesBurned } +
                selectedRuns.sumOf { it.caloriesBurned.toDouble() }.toInt()

            val daysWithData = remember(foodRecords, workoutLogs, cardioLogs, gpsLogs, intakeHistory) {
                buildSet {
                    addAll(foodRecords.map { it.dateString })
                    addAll(workoutLogs.map { dateFormatter.format(Date(it.dateMillis)) })
                    addAll(cardioLogs.map { dateFormatter.format(Date(it.dateMillis)) })
                    addAll(gpsLogs.map { dateFormatter.format(Date(it.dateMillis)) })
                    addAll(intakeHistory.map { it.dateString })
                }
            }

            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                DailyHistoryHeader(
                    selectedDateMillis = selectedDateMillis,
                    onDateSelected = viewModel::selectAppDate,
                    daysWithData = daysWithData,
                    summary = DailyHistorySummary(
                        caloriesConsumed = consumedCalories.toInt(),
                        targetCalories = profile.targetCalories,
                        protein = consumedProtein.toInt(),
                        proteinTarget = profile.targetProteinGrams,
                        carbs = consumedCarbs.toInt(),
                        carbsTarget = profile.targetCarbsGrams,
                        fat = consumedFat.toInt(),
                        fatTarget = profile.targetFatGrams,
                        waterMl = selectedIntake?.waterDrankMl ?: 0,
                        waterTargetMl = profile.targetWaterMl,
                        workouts = selectedWorkouts.size,
                        cardioSessions = selectedCardio.size + selectedRuns.size,
                        exerciseCalories = selectedExerciseCalories,
                        weightKg = selectedProgress?.weightKg
                    )
                )
            }
        }

        // --- Core Dashboard Elements ---

        // 1. Calorie and Macro Goals Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "METAS DE MACRONUTRIENTES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularCalorieGauge(
                            currentCalories = consumedCalories,
                            targetCalories = profile.targetCalories
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MacroProgressBar("Proteína", consumedProtein, profile.targetProteinGrams.toFloat(), "g", GrowthCyan)
                            MacroProgressBar("Carboidratos", consumedCarbs, profile.targetCarbsGrams.toFloat(), "g", GrowthOrangeLight)
                            MacroProgressBar("Gorduras", consumedFat, profile.targetFatGrams.toFloat(), "g", GrowthOrange)
                        }
                    }
                }
            }
        }

        // 2. Gasto Calórico & TMB
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("GASTO CALÓRICO & TMB DO DIA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("${dailyEnergy.totalExpenditure} kcal", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                    Text("TMB ${dailyEnergy.bmr} + treino ${dailyEnergy.totalWorkoutCalories} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    val plannedBalance = profile.targetCalories - dailyEnergy.totalExpenditure
                    Text(
                        text = "Meta alimentar: ${profile.targetCalories} kcal  •  Saldo estimado: ${if (plannedBalance > 0) "+" else ""}$plannedBalance kcal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (plannedBalance <= 0) GrowthCyan else GrowthOrangeLight
                    )
                }
            }
        }

        // 3. Hidratação
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HIDRATAÇÃO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("$waterDrank / ${profile.targetWaterMl} ml", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    val waterProgress = (waterDrank.toFloat() / profile.targetWaterMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(progress = { waterProgress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)), color = GrowthCyan)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.addWater(250) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrowthOrange,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("add_water_250_btn")
                        ) { Text("+250 ml", fontWeight = FontWeight.Bold, maxLines = 1) }
                        OutlinedButton(
                            onClick = { viewModel.addWater(500) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = GrowthCyan.copy(alpha = 0.12f),
                                contentColor = GrowthCyan
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("add_water_500_btn")
                        ) { Text("+500 ml", fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.resetWater() }) {
                            Text("Zerar hoje", color = GrowthOrangeLight)
                        }
                        TextButton(onClick = {
                            newWaterGoalInput = profile.targetWaterMl.toString()
                            showWaterGoalDialog = true
                        }) {
                            Text("Ajustar meta", color = GrowthCyan)
                        }
                    }
                }
            }
        }

        // 4. Peso/Evolução
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("EVOLUÇÃO DO PESO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    WeightProgressionChart(logs = progressLogs, targetWeightKg = profile.monthlyTargetWeightKg)
                }
            }
        }

    }

    if (showWaterGoalDialog) {
        val recommendedWater = MacroCalculator.calculateWaterIntakeMl(profile.weightKg, profile.age, profile.isAthleteOrWarmClimate)
        AlertDialog(
            onDismissRequest = { showWaterGoalDialog = false },
            containerColor = CarbonCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalDrink, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajustar Meta Diária de Água", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                }
            },
            text = {
                Column {
                    TextButton(onClick = { onNavigateToProfile(); showMoreDialog = false }) {
                        Text("Perfil, conta e biometria", color = GrowthCyan)
                    }
                    Text(
                        text = "Estimativa-base: $recommendedWater ml por dia. Ela usa peso de referência e deve ser ajustada por sede, suor, clima e orientação profissional.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newWaterGoalInput,
                        onValueChange = { newWaterGoalInput = it },
                        label = { Text("Meta de Água (ml)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = GrowthCyan.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { newWaterGoalInput = "$recommendedWater" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.5f))
                        ) {
                            Text("Usar Sugestão ($recommendedWater ml)", fontSize = 10.sp, color = GrowthCyan)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = newWaterGoalInput.toIntOrNull() ?: profile.targetWaterMl
                        viewModel.updateDailyWaterGoal(parsed)
                        showWaterGoalDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Text("Salvar Meta", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWaterGoalDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    if (showMoreDialog) {
        AlertDialog(
            onDismissRequest = { showMoreDialog = false },
            containerColor = CarbonCard,
            title = { Text("Mais Opções", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
            text = {
                Column {
                    TextButton(onClick = { onNavigateToAchievements(); showMoreDialog = false }) {
                        Text("Medalhas & Títulos de Anime", color = GrowthYellow, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { onNavigateToShopping(); showMoreDialog = false }) {
                        Text("Lista de Compras", color = MaterialTheme.colorScheme.onBackground)
                    }
                    TextButton(onClick = { onNavigateToReceipts(); showMoreDialog = false }) {
                        Text("Preços & NF", color = MaterialTheme.colorScheme.onBackground)
                    }
                    TextButton(onClick = { viewModel.exportPdfProgressReport(context); showMoreDialog = false }) {
                        Text("Exportar Relatório PDF", color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoreDialog = false }) {
                    Text("Fechar", color = GrowthCyan)
                }
            }
        )
    }
}
