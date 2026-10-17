package com.example.ui.screens.dashboard

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.ui.VirtusViewModel
import com.example.ui.components.CircularCalorieGauge
import com.example.ui.components.GrowthHeader
import com.example.ui.components.MacroProgressBar
import com.example.ui.components.WeightProgressionChart
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
    viewModel: VirtusViewModel,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToReceipts: () -> Unit = {},
    onNavigateToGps: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlanItems.collectAsStateWithLifecycle()
    val foodRecords by viewModel.allFoodRecords.collectAsStateWithLifecycle()
    val workoutRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val waterDrank by viewModel.waterDrankMl.collectAsStateWithLifecycle()
    val dailyEnergy by viewModel.dailyEnergyExpenditure.collectAsStateWithLifecycle()
    var showWaterGoalDialog by remember { mutableStateOf(false) }
    var showMoreDialog by remember { mutableStateOf(false) }
    var newWaterGoalInput by remember { mutableStateOf("") }

    // Only food actually checked/recorded today counts as consumed.
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayFood = foodRecords.filter { it.dateMillis >= todayStart }
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
        item {
            val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")).format(Date())
            GrowthHeader(
                title = "Olá, ${profile.name}",
                subtitle = dateStr.replaceFirstChar { it.uppercase() },
                badgeText = "VIRTUS PERFORMANCE",
                trailingContent = {
                    Row {
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
                            MacroProgressBar("Carboidratos", consumedCarbs, profile.targetCarbsGrams.toFloat(), "g", GrowthYellow)
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
                        color = if (plannedBalance <= 0) GrowthGreen else GrowthYellow
                    )
                }
            }
        }

        // 3. Hidratação
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
                        FilledTonalButton(
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f).testTag("add_water_250_btn")
                        ) { Text("+250 ml", maxLines = 1) }
                        FilledTonalButton(
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier.weight(1f).testTag("add_water_500_btn")
                        ) { Text("+500 ml", maxLines = 1) }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { viewModel.resetWater() }) {
                            Text("Zerar hoje")
                        }
                        TextButton(onClick = {
                            newWaterGoalInput = profile.targetWaterMl.toString()
                            showWaterGoalDialog = true
                        }) {
                            Text("Ajustar meta")
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
                    Text("EVOLUÇÃO DO PESO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthGreen, letterSpacing = 1.sp)
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
                        Text("Perfil, conta e biometria", color = MaterialTheme.colorScheme.onBackground)
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
                            focusedBorderColor = GrowthCyan,
                            unfocusedBorderColor = CarbonBorder
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
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan)
                ) {
                    Text("Salvar Meta", color = Color.Black, fontWeight = FontWeight.Bold)
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
