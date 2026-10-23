package com.example.ui.screens.diet

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalMall
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DietCycleType
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
import com.example.util.MacroCalculator
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
fun DietScreen(
    viewModel: VirtusViewModel,
    onNavigateToShopping: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val mealPlans by viewModel.mealPlanItems.collectAsStateWithLifecycle()
    val allFoods by viewModel.allFoods.collectAsStateWithLifecycle()
    val dailyEnergy by viewModel.dailyEnergyExpenditure.collectAsStateWithLifecycle()
    val foodRecords by viewModel.allFoodRecords.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val cardioLogs by viewModel.allCardioLogs.collectAsStateWithLifecycle()
    val gpsLogs by viewModel.allGpsRouteLogs.collectAsStateWithLifecycle()
    val intakeHistory by viewModel.recentIntakeHistory.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val isAiDietLoading by viewModel.isAiDietLoading.collectAsStateWithLifecycle()
    val aiDietSuggestions by viewModel.aiDietSuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddFoodDialog by remember { mutableStateOf(false) }
    var selectedMealTypeForAdd by remember { mutableStateOf(MealType.BREAKFAST) }
    var showCustomFoodDialog by remember { mutableStateOf(false) }
    var showAiDietSetupDialog by remember { mutableStateOf(false) }
    var showAiPlanPreviewDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Refeições Diárias, 1: Base de Alimentos, 2: Dieta Mensal, 3: Configuração
    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val selectedDateKey = dateFormatter.format(Date(selectedDateMillis))
    val selectedFoods = foodRecords.filter { it.dateString == selectedDateKey }
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
    val todayKey = dateFormatter.format(Date())
    val displayEnergy = if (selectedDateKey == todayKey) dailyEnergy else dailyEnergy.copy(
        gymWorkoutCalories = gymCalories,
        runningWorkoutCalories = selectedRuns.sumOf { it.caloriesBurned.toDouble() }.toInt(),
        cardioWorkoutCalories = selectedCardio.sumOf { it.caloriesBurned },
        totalWorkoutCalories = selectedExerciseCalories,
        totalExpenditure = dailyEnergy.bmr + selectedExerciseCalories,
        hasWorkoutToday = selectedWorkouts.isNotEmpty() || selectedRuns.isNotEmpty() || selectedCardio.isNotEmpty(),
        gymWorkoutsCount = selectedWorkouts.size,
        runningWorkoutsCount = selectedRuns.size,
        cardioWorkoutsCount = selectedCardio.size
    )
    val daysWithData = remember(foodRecords, workoutLogs, cardioLogs, gpsLogs, intakeHistory) {
        buildSet {
            addAll(foodRecords.map { it.dateString })
            addAll(workoutLogs.map { dateFormatter.format(Date(it.dateMillis)) })
            addAll(cardioLogs.map { dateFormatter.format(Date(it.dateMillis)) })
            addAll(gpsLogs.map { dateFormatter.format(Date(it.dateMillis)) })
            addAll(intakeHistory.map { it.dateString })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = "Dieta & Nutrição",
                subtitle = "Planejamento alimentar e distribuição de macros",
                badgeText = "CARDÁPIO DIÁRIO",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val text = viewModel.formatDietPlanForSharing()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Compartilhar Cardápio"))
                            },
                            modifier = Modifier.testTag("diet_share_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartilhar Cardápio",
                                tint = GrowthOrangeLight
                            )
                        }

                        IconButton(
                            onClick = {
                                viewModel.generateShoppingList(30)
                                onNavigateToShopping()
                            },
                            modifier = Modifier.testTag("diet_to_shopping_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Lista de Compras",
                                tint = GrowthGreen
                            )
                        }
                    }
                }
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = CarbonCard,
                contentColor = GrowthOrange,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GrowthOrange
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Refeições Diárias",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Base de Alimentos",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (allFoods.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = if (selectedTab == 1) GrowthOrange else CarbonDark,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${allFoods.size}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedTab == 1) Color.White else GrowthOrangeLight,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Dieta Mensal",
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            "Configuração",
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {

                    // Gasto Calórico Diário & TMB Card (Regra: Parado = TMB; Treino soma gasto)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                                .testTag("diet_daily_energy_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = GrowthOrange,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GASTO ENERGÉTICO & TMB DO DIA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier.align(Alignment.End),
                                        color = if (displayEnergy.hasWorkoutToday) GrowthGreen.copy(alpha = 0.2f) else GrowthCyan.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (displayEnergy.hasWorkoutToday) "TREINO REGISTRADO" else "PARADO (SEM TREINO)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (displayEnergy.hasWorkoutToday) GrowthGreen else GrowthCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CarbonDark,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("TMB (PARADO)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                            Text("${displayEnergy.bmr} kcal", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                                            Text("Metabolismo basal", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CarbonDark,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("GASTO TREINO", fontSize = 9.sp, color = if (displayEnergy.hasWorkoutToday) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                            Text(if (displayEnergy.hasWorkoutToday) "+${displayEnergy.totalWorkoutCalories} kcal" else "0 kcal", fontSize = 14.sp, fontWeight = FontWeight.Black, color = if (displayEnergy.hasWorkoutToday) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(if (displayEnergy.hasWorkoutToday) "${displayEnergy.gymWorkoutsCount} musc • ${displayEnergy.runningWorkoutsCount} corr" else "Sem treino neste dia", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CarbonCardElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("GASTO TOTAL", fontSize = 9.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                            Text("${displayEnergy.totalExpenditure} kcal", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
                                            Text(if (displayEnergy.hasWorkoutToday) "TMB + Treino" else "Valor TMB parado", fontSize = 8.sp, color = GrowthCyan)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (displayEnergy.hasWorkoutToday) {
                                        "⚡ Treino realizado neste dia: o gasto dos treinos (${displayEnergy.totalWorkoutCalories} kcal) foi somado à sua TMB (${displayEnergy.bmr} kcal)."
                                    } else {
                                        "ℹ️ Nenhum treino realizado neste dia: o gasto permanece na Taxa Metabólica Basal (${displayEnergy.bmr} kcal)."
                                    },
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Botão de Acesso Rápido à Tabela de Alimentos & Macros
                    item {
                        Surface(
                            onClick = { selectedTab = 1 },
                            color = CarbonDark,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("open_food_database_tab_btn")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = GrowthOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "TABELA DE ALIMENTOS & MACROS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Consultar 117+ alimentos e suplementos Growth",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Surface(
                                    color = GrowthOrange,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Ver Base",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Macro Summary Header Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CONSUMIDO NO DIA",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrowthOrangeLight,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "${selectedFoods.sumOf { it.calories.toDouble() }.toInt()} / ${profile.targetCalories} kcal",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MacroPill(
                                        title = "Proteína",
                                        current = selectedFoods.sumOf { it.protein.toDouble() }.toFloat(),
                                        target = profile.targetProteinGrams.toFloat(),
                                        color = GrowthCyan,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroPill(
                                        title = "Carboidratos",
                                        current = selectedFoods.sumOf { it.carbs.toDouble() }.toFloat(),
                                        target = profile.targetCarbsGrams.toFloat(),
                                        color = GrowthYellow,
                                        modifier = Modifier.weight(1f)
                                    )
                                    MacroPill(
                                        title = "Gorduras",
                                        current = selectedFoods.sumOf { it.fat.toDouble() }.toFloat(),
                                        target = profile.targetFatGrams.toFloat(),
                                        color = GrowthOrange,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Group by Meal Type
                    val groupedMeals = mealPlans.groupBy { it.mealType }

                    MealType.values().forEach { type ->
                        val itemsForType = groupedMeals[type] ?: emptyList()
                        val mealCalories = itemsForType.sumOf { it.calories.toDouble() }.toFloat()
                        val mealProtein = itemsForType.sumOf { it.protein.toDouble() }.toFloat()
                        val consumedForType = selectedFoods.filter { it.mealType == type }

                        item(key = "meal_type_${type.name}") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = type.title,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "${consumedForType.sumOf { it.calories.toDouble() }.toInt()} kcal consumidas • ${itemsForType.size} item(ns) planejados",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    viewModel.suggestAIMealItems(type)
                                                    showAiPlanPreviewDialog = true
                                                },
                                                modifier = Modifier.testTag("ai_suggest_meal_btn_${type.name}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Sugestão IA",
                                                    tint = GrowthCyan
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    selectedMealTypeForAdd = type
                                                    showAddFoodDialog = true
                                                },
                                                modifier = Modifier.testTag("add_food_btn_${type.name}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Adicionar Alimento",
                                                    tint = GrowthOrange
                                                )
                                            }
                                        }
                                    }

                                    if (itemsForType.isEmpty()) {
                                        Text(
                                            text = "Nenhum alimento cadastrado nesta refeição.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        itemsForType.forEach { item ->
                                            val completedForDate = consumedForType.any {
                                                it.foodItemId == item.foodItemId && it.mealType == item.mealType
                                            }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        if (item.isFixed) {
                                                            Icon(
                                                                imageVector = Icons.Default.PushPin,
                                                                contentDescription = "Item Fixo",
                                                                tint = GrowthOrange,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                        }
                                                        Text(
                                                            text = item.foodName,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = MaterialTheme.colorScheme.onBackground
                                                        )
                                                    }
                                                    Text(
                                                        text = "${item.amount.toInt()} ${item.unit} • ${item.calories.toInt()} kcal (P: ${item.protein.toInt()}g, C: ${item.carbs.toInt()}g, G: ${item.fat.toInt()}g)",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }

                                                Row {
                                                    IconButton(
                                                        onClick = { viewModel.toggleMealItemFixed(item) },
                                                        modifier = Modifier.size(32.dp).testTag("fix_meal_${item.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PushPin,
                                                            contentDescription = if (item.isFixed) "Desafixar item" else "Fixar item (repetir todo dia)",
                                                            tint = if (item.isFixed) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.toggleMealItemCompleted(item, selectedDateKey, completedForDate)
                                                        },
                                                        modifier = Modifier.size(32.dp).testTag("consume_meal_${item.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = if (completedForDate) "Desmarcar consumo" else "Marcar como consumido",
                                                            tint = if (completedForDate) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.removeMealItem(item.id) },
                                                        modifier = Modifier.size(32.dp).testTag("delete_meal_${item.id}")
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Remover do plano",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    val extraRecords = consumedForType.filter { record ->
                                        itemsForType.none { it.foodItemId == record.foodItemId }
                                    }
                                    if (extraRecords.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "OUTROS REGISTROS DO DIA",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthCyan
                                        )
                                        extraRecords.forEach { record ->
                                            Text(
                                                text = "• ${record.foodName} — ${record.amount.toInt()} ${record.unit} — ${record.calories.toInt()} kcal",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Button to add custom food item to database
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showCustomFoodDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_custom_food_dialog_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cadastrar Novo Alimento na Tabela",
                                color = GrowthCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                FoodDatabaseSection(
                    foods = allFoods,
                    onAddFoodToMeal = { food, mealType, amount, notes ->
                        viewModel.addFoodToMeal(mealType, food, amount, notes)
                    },
                    onOpenCustomFoodDialog = { showCustomFoodDialog = true },
                    onReloadDefaults = { viewModel.reloadFoodDatabase() },
                    onToggleFavorite = { viewModel.toggleFoodFavorite(it) }
                )
            } else if (selectedTab == 2) {
                MonthlyDietOverviewSection(
                    profile = profile,
                    onNavigateToConfig = { selectedTab = 3 },
                    onGenerateAiDiet = { showAiDietSetupDialog = true }
                )
            } else {
                // Monthly Diet Configuration Screen
                MonthlyDietConfigSection(
                    profile = profile,
                    onSave = { cycleType, notes, targetWeight, trainingDays ->
                        val macroReferenceWeight = MacroCalculator.calculateMacroReferenceWeight(
                            profile.weightKg,
                            profile.heightCm
                        )
                        viewModel.updateProfileAndCalculateMacros(
                            name = profile.name,
                            gender = profile.gender,
                            age = profile.age,
                            weightKg = profile.weightKg,
                            heightCm = profile.heightCm,
                            activityLevel = profile.activityLevel,
                            goal = profile.goal,
                            proteinGPerKg = (profile.targetProteinGrams / macroReferenceWeight),
                            fatGPerKg = (profile.targetFatGrams / macroReferenceWeight),
                            monthlyTargetWeightKg = targetWeight,
                            monthlyNotes = notes,
                            dietCycleType = cycleType,
                            targetCalories = profile.targetCalories
                        )
                        selectedTab = 2
                    }
                )
            }
        }

        if (showAiDietSetupDialog) {
            AIDietSetupDialog(
                onDismiss = { showAiDietSetupDialog = false },
                onConfirm = { prefs ->
                    showAiDietSetupDialog = false
                    viewModel.generateAIDietPlan(prefs)
                    showAiPlanPreviewDialog = true
                }
            )
        }

        if (showAiPlanPreviewDialog) {
            AIPlanPreviewDialog(
                isLoading = isAiDietLoading,
                suggestions = aiDietSuggestions,
                onDismiss = { 
                    showAiPlanPreviewDialog = false
                    viewModel.clearAiSuggestions()
                },
                onApply = {
                    viewModel.applyAiDiet()
                    showAiPlanPreviewDialog = false
                }
            )
        }
    }

    // Add Food Dialog
    if (showAddFoodDialog) {
        AddFoodToMealDialog(
            mealType = selectedMealTypeForAdd,
            foods = allFoods,
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { food, amount, notes ->
                viewModel.addFoodToMeal(selectedMealTypeForAdd, food, amount, notes)
                showAddFoodDialog = false
            }
        )
    }

    // Custom Food Registration Dialog
    if (showCustomFoodDialog) {
        CustomFoodRegistrationDialog(
            onDismiss = { showCustomFoodDialog = false },
            onSave = { name, cat, amount, unit, cals, prot, carbs, fat, fiber, isGrowth ->
                viewModel.addCustomFoodItem(name, cat, amount, unit, cals, prot, carbs, fat, fiber, isGrowth)
                showCustomFoodDialog = false
            }
        )
    }
}

// Monthly Diet Overview Section composable
@Composable
fun MonthlyDietOverviewSection(
    profile: com.example.data.model.UserProfile,
    onNavigateToConfig: () -> Unit,
    onGenerateAiDiet: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Text(
                text = "RESUMO DA DIETA MENSAL (30 DIAS)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GrowthOrangeLight,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Estratégia Atual",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profile.dietCycleType.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GrowthOrange
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Peso Alvo", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${profile.monthlyTargetWeightKg} kg", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Column {
                            Text(text = "Dias de Treino", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${profile.trainingDaysPerWeek} dias/sem", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Observações da Periodização:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = profile.monthlyNotes.ifEmpty { "Nenhuma observação cadastrada." },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onGenerateAiDiet,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("generate_ai_diet_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("GERAR DIETA COMPLETA COM IA", fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateToConfig,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("EDITAR CONFIGURAÇÃO MENSAL", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun MacroPill(
    title: String,
    current: Float,
    target: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CarbonCardElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${current.toInt()}g",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "meta: ${target.toInt()}g",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthlyDietConfigSection(
    profile: com.example.data.model.UserProfile,
    onSave: (DietCycleType, String, Float, Int) -> Unit
) {
    var selectedCycle by remember { mutableStateOf(profile.dietCycleType) }
    var notes by remember { mutableStateOf(profile.monthlyNotes) }
    var targetWeightText by remember { mutableStateOf(profile.monthlyTargetWeightKg.toString()) }
    var trainingDaysText by remember { mutableStateOf(profile.trainingDaysPerWeek.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Text(
                text = "ESTRATÉGIA NUTRICIONAL DO MÊS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GrowthOrangeLight,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Modelo de Distribuição Calórica",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DietCycleType.values().forEach { cycle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCycle = cycle }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (selectedCycle == cycle) GrowthOrange else CarbonCardElevated,
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedCycle == cycle) GrowthOrange else CarbonBorder
                                ),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (selectedCycle == cycle) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cycle.label,
                                fontSize = 12.sp,
                                color = if (selectedCycle == cycle) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Metas Antropométricas para 30 Dias",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = targetWeightText,
                        onValueChange = { targetWeightText = it },
                        label = { Text("Peso Alvo ao Final do Mês (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("monthly_target_weight_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = trainingDaysText,
                        onValueChange = { trainingDaysText = it },
                        label = { Text("Dias de Treino por Semana") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Observações da Fase / Periodização") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val weight = targetWeightText.toFloatOrNull() ?: profile.monthlyTargetWeightKg
                    val days = trainingDaysText.toIntOrNull() ?: profile.trainingDaysPerWeek
                    onSave(selectedCycle, notes, weight, days)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_monthly_diet_config_btn")
            ) {
                Text("SALVAR CONFIGURAÇÃO MENSAL", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun AddFoodToMealDialog(
    mealType: MealType,
    foods: List<FoodItem>,
    onDismiss: () -> Unit,
    onConfirm: (FoodItem, Float, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var amountText by remember { mutableStateOf("100") }
    var notesText by remember { mutableStateOf("") }

    val filteredFoods = foods.filter { food ->
        val matchesQuery = food.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == null || food.category == selectedCategory
        val matchesFavorite = !showOnlyFavorites || food.isFavorite
        matchesQuery && matchesCategory && matchesFavorite
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text(
                text = "Adicionar a ${mealType.title}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedFood == null) {
                    // Search box
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar frango, whey, arroz...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                        modifier = Modifier.fillMaxWidth().testTag("search_food_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Categories Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Todos", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = showOnlyFavorites,
                                onClick = { showOnlyFavorites = !showOnlyFavorites },
                                label = { Text("Favoritos", fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(FoodCategory.values()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.label, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        items(filteredFoods) { food ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable {
                                        selectedFood = food
                                        amountText = food.baseServingAmount.toInt().toString()
                                    }
                                    .testTag("food_option_${food.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = food.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${food.calories.toInt()} kcal | P: ${food.protein}g | C: ${food.carbs}g | G: ${food.fat}g (por ${food.baseServingAmount.toInt()}${food.servingUnit})",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val food = selectedFood!!
                    val amount = amountText.toFloatOrNull() ?: 100f
                    val calculated = food.calculateMacrosForAmount(amount)

                    Text(
                        text = food.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val amountVal = amountText.toFloatOrNull()
                    val isAmountValid = amountVal != null && amountVal > 0f
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Quantidade (${food.servingUnit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = !isAmountValid && amountText.isNotBlank(),
                        supportingText = {
                            if (!isAmountValid && amountText.isNotBlank()) {
                                Text("Digite um valor numérico válido (> 0)", color = Color(0xFFEF4444), fontSize = 10.sp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("food_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real-time macro computed box
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Macros calculados para ${amount.toInt()} ${food.servingUnit}:",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${calculated.calories.toInt()} kcal  •  P: ${calculated.protein.toInt()}g  •  C: ${calculated.carbs.toInt()}g  •  G: ${calculated.fat.toInt()}g",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { selectedFood = null }) {
                        Text("← Escolher outro alimento", color = GrowthCyan, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            if (selectedFood != null) {
                val amountVal = amountText.toFloatOrNull()
                val isAmountValid = amountVal != null && amountVal > 0f
                Button(
                    onClick = {
                        val amount = amountVal ?: 100f
                        onConfirm(selectedFood!!, amount, notesText)
                    },
                    enabled = isAmountValid,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    modifier = Modifier.testTag("confirm_add_food_btn")
                ) {
                    Text("Adicionar à Dieta", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun CustomFoodRegistrationDialog(
    onDismiss: () -> Unit,
    onSave: (String, FoodCategory, Float, String, Float, Float, Float, Float, Float, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(FoodCategory.PROTEIN) }
    var servingAmount by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("g") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var isGrowth by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text(
                "Cadastrar Alimento na Tabela",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Alimento") },
                        modifier = Modifier.fillMaxWidth().testTag("custom_food_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = servingAmount,
                            onValueChange = { servingAmount = it },
                            label = { Text("Porção Base") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unidade (g, kg, ml, L)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("g", "kg", "ml", "L").forEach { u ->
                            OutlinedButton(
                                onClick = { unit = u },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (unit == u) GrowthOrange.copy(alpha = 0.2f) else Color.Transparent
                                ),
                                border = BorderStroke(1.dp, if (unit == u) GrowthOrange else CarbonBorder),
                                modifier = Modifier.weight(1f).height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(u, color = if (unit == u) GrowthOrange else MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calorias (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("custom_food_cals_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Proteína (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("custom_food_prot_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            label = { Text("Carbos (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = fat,
                            onValueChange = { fat = it },
                            label = { Text("Gorduras (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            val servingVal = servingAmount.toFloatOrNull()
            val calsVal = calories.toFloatOrNull()
            val protVal = protein.toFloatOrNull()
            val carbsVal = carbs.toFloatOrNull()
            val fatVal = fat.toFloatOrNull()
            val isFormValid = name.isNotBlank() &&
                servingVal != null && servingVal > 0f &&
                calsVal != null && calsVal >= 0f &&
                protVal != null && protVal >= 0f &&
                carbsVal != null && carbsVal >= 0f &&
                fatVal != null && fatVal >= 0f

            Button(
                onClick = {
                    if (isFormValid) {
                        onSave(
                            name.trim(),
                            category,
                            servingVal!!,
                            unit,
                            calsVal!!,
                            protVal!!,
                            carbsVal!!,
                            fatVal!!,
                            0f,
                            isGrowth
                        )
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("save_custom_food_btn")
            ) {
                Text("Salvar Alimento", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AIDietSetupDialog(
    onDismiss: () -> Unit,
    onConfirm: (com.example.ui.DietAiPreferences) -> Unit
) {
    var restrictions by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var mealsCount by remember { mutableStateOf(5) }
    var includeFavs by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GrowthCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Configurar Dieta IA", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("A IA criará um plano baseado no seu objetivo atual e nas suas preferências abaixo.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = restrictions,
                    onValueChange = { restrictions = it },
                    label = { Text("Restrições (Ex: Vegano, Sem Lactose)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthCyan, unfocusedBorderColor = CarbonBorder)
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = preferences,
                    onValueChange = { preferences = it },
                    label = { Text("Preferências (Ex: Gosto de Aveia, Ovos)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthCyan, unfocusedBorderColor = CarbonBorder)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("Número de Refeições: $mealsCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                androidx.compose.material3.Slider(
                    value = mealsCount.toFloat(),
                    onValueChange = { mealsCount = it.toInt() },
                    valueRange = 3f..7f,
                    steps = 3,
                    colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = GrowthCyan, activeTrackColor = GrowthCyan)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(
                        checked = includeFavs,
                        onCheckedChange = { includeFavs = it },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = GrowthCyan)
                    )
                    Text("Priorizar meus Alimentos Favoritos", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(com.example.ui.DietAiPreferences(restrictions, preferences, mealsCount, includeFavs)) },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan)
            ) {
                Text("Gerar Dieta", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun AIPlanPreviewDialog(
    isLoading: Boolean,
    suggestions: List<com.example.ui.AiDietSuggestion>,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GrowthCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLoading) "Gerando Plano..." else "Sugestão da IA", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GrowthCyan
                    )
                } else if (suggestions.isEmpty()) {
                    Text("Nenhuma sugestão gerada. Tente novamente.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(suggestions) { item ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(item.mealType.title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                                    Text(item.foodName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${item.amount.toInt()} ${item.unit} • ${item.calories.toInt()} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.notes.isNotBlank()) {
                                        Text(item.notes, fontSize = 10.sp, color = GrowthOrangeLight, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isLoading && suggestions.isNotEmpty()) {
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen)
                ) {
                    Text("Aplicar Dieta", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isLoading) "Cancelar" else "Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
