package com.example.ui.screens.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.GrowthViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: GrowthViewModel,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToShopping: () -> Unit,
    onNavigateToCalculator: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlanItems.collectAsStateWithLifecycle()
    val workoutRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val waterDrank by viewModel.waterDrankMl.collectAsStateWithLifecycle()

    // Daily Macro Consumption calculations
    val totalPlannedCalories = mealPlans.sumOf { it.calories.toDouble() }.toFloat()
    val totalPlannedProtein = mealPlans.sumOf { it.protein.toDouble() }.toFloat()
    val totalPlannedCarbs = mealPlans.sumOf { it.carbs.toDouble() }.toFloat()
    val totalPlannedFat = mealPlans.sumOf { it.fat.toDouble() }.toFloat()

    val completedMeals = mealPlans.filter { it.isCompletedToday }
    val consumedCalories = if (completedMeals.isNotEmpty()) {
        completedMeals.sumOf { it.calories.toDouble() }.toFloat()
    } else {
        totalPlannedCalories
    }
    val consumedProtein = if (completedMeals.isNotEmpty()) {
        completedMeals.sumOf { it.protein.toDouble() }.toFloat()
    } else {
        totalPlannedProtein
    }
    val consumedCarbs = if (completedMeals.isNotEmpty()) {
        completedMeals.sumOf { it.carbs.toDouble() }.toFloat()
    } else {
        totalPlannedCarbs
    }
    val consumedFat = if (completedMeals.isNotEmpty()) {
        completedMeals.sumOf { it.fat.toDouble() }.toFloat()
    } else {
        totalPlannedFat
    }

    val todayRoutine = workoutRoutines.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            val dateStr = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("pt", "BR")).format(Date())
            GrowthHeader(
                title = "Olá, ${profile.name}",
                subtitle = dateStr.replaceFirstChar { it.uppercase() },
                badgeText = "ESTILO GROWTH • PROTOCOLO ATIVO",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GrowthOrange.copy(alpha = 0.15f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onNavigateToCalculator() }
                                .testTag("profile_calc_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = "Calculadora",
                                    tint = GrowthOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.size(40.dp).testTag("logout_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Sair da Conta",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            )
        }

        // Athlete Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(GrowthOrange.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Surface(
                                color = GrowthOrange,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = profile.goal.label.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${profile.weightKg} kg  ➔  ${profile.monthlyTargetWeightKg} kg",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Meta do Mês: ${profile.monthlyNotes}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onNavigateToAnalytics() },
                            colors = ButtonDefaults.buttonColors(containerColor = CarbonCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier.testTag("view_analytics_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = GrowthGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Evolução", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Calorie and Macro Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "METAS DE MACRONUTRIENTES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Ajustar",
                            fontSize = 12.sp,
                            color = GrowthCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { onNavigateToCalculator() }
                                .testTag("adjust_macros_btn")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CircularCalorieGauge(
                            currentCalories = consumedCalories,
                            targetCalories = profile.targetCalories
                        )

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MacroProgressBar(
                                label = "Proteína",
                                currentValue = consumedProtein,
                                targetValue = profile.targetProteinGrams.toFloat(),
                                barColor = GrowthCyan
                            )
                            MacroProgressBar(
                                label = "Carboidratos",
                                currentValue = consumedCarbs,
                                targetValue = profile.targetCarbsGrams.toFloat(),
                                barColor = GrowthYellow
                            )
                            MacroProgressBar(
                                label = "Gorduras",
                                currentValue = consumedFat,
                                targetValue = profile.targetFatGrams.toFloat(),
                                barColor = GrowthOrange
                            )
                        }
                    }
                }
            }
        }

        // Water Hydration Tracker Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HIDRATAÇÃO DIÁRIA",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthCyan,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "$waterDrank / ${profile.targetWaterMl} ml",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val waterProgress = (waterDrank.toFloat() / profile.targetWaterMl.coerceAtLeast(1)).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { waterProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = GrowthCyan,
                        trackColor = CarbonCardElevated
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.addWater(250) },
                            modifier = Modifier.weight(1f).testTag("add_water_250_btn"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text("+250ml", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.addWater(500) },
                            modifier = Modifier.weight(1f).testTag("add_water_500_btn"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text("+500ml", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { viewModel.addWater(1000) },
                            modifier = Modifier.weight(1f).testTag("add_water_1000_btn"),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text("+1 Litro", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Today's Workout Routine Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = GrowthOrange,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "TREINO ${todayRoutine?.letter ?: "A"}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = todayRoutine?.title ?: "Treino A - Peitoral",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Foco: ${todayRoutine?.targetMuscleGroups ?: "Peito, Ombros, Tríceps"} • ~${todayRoutine?.estimatedDurationMinutes ?: 60} min",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onNavigateToWorkout() },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_workout_dashboard_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "INICIAR SESSÃO DE TREINO",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Quick Actions Grid (Export PDF, Grocery List, Diet Config)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "AÇÕES RÁPIDAS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // PDF Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.exportPdfProgressReport(context) }
                            .testTag("quick_export_pdf_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = "PDF",
                                tint = GrowthOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Exportar PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Shopping List Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToShopping() }
                            .testTag("quick_shopping_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Compras",
                                tint = GrowthGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Lista Compras", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Monthly Diet Button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToDiet() }
                            .testTag("quick_diet_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = "Dieta",
                                tint = GrowthCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dieta Mensal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Weight Progression Chart
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                WeightProgressionChart(
                    logs = progressLogs,
                    targetWeightKg = profile.monthlyTargetWeightKg
                )
            }
        }

        // Daily Meals Quick Checklist
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REFEIÇÕES DO DIA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ver Todas (${mealPlans.size})",
                    fontSize = 12.sp,
                    color = GrowthCyan,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateToDiet() }
                        .testTag("view_all_meals_btn")
                )
            }
        }

        items(mealPlans.take(4)) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { viewModel.toggleMealItemCompleted(item) }
                    .testTag("meal_item_${item.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (item.isCompletedToday) GrowthGreen else CarbonCardElevated,
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (item.isCompletedToday) GrowthGreen else CarbonBorder
                            ),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (item.isCompletedToday) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = item.foodName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${item.mealType.title} • ${item.amount.toInt()}${item.unit} (${item.calories.toInt()} kcal)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "P: ${item.protein.toInt()}g",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthCyan
                    )
                }
            }
        }
    }
}
