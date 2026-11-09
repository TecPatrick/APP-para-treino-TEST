package com.example.ui.screens.diet

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MealType
import com.example.ui.DietViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.screens.diet.components.AIDietSetupDialog
import com.example.ui.screens.diet.components.AIPlanPreviewDialog
import com.example.ui.screens.diet.components.AddFoodToMealDialog
import com.example.ui.screens.diet.components.CustomFoodRegistrationDialog
import com.example.ui.screens.diet.components.DietDailyEnergyCard
import com.example.ui.screens.diet.components.DietNutritionHeroBanner
import com.example.ui.screens.diet.components.DietQuickLinksAndSummaryCards
import com.example.ui.screens.diet.components.MealSectionCard
import com.example.ui.screens.diet.components.MonthlyDietConfigSection
import com.example.ui.screens.diet.components.MonthlyDietOverviewSection
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.util.MacroCalculator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(
    viewModel: DietViewModel,
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
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val isAiDietLoading by viewModel.isAiDietLoading.collectAsStateWithLifecycle()
    val aiDietSuggestions by viewModel.aiDietSuggestions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddFoodDialog by remember { mutableStateOf(false) }
    var selectedMealTypeForAdd by remember { mutableStateOf(MealType.BREAKFAST) }
    var showCustomFoodDialog by remember { mutableStateOf(false) }
    var showAiDietSetupDialog by remember { mutableStateOf(false) }
    var showAiPlanPreviewDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val selectedDateKey = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }
    val todayKey = remember { dateFormatter.format(Date()) }

    val selectedFoods by remember(foodRecords, selectedDateKey) {
        derivedStateOf { foodRecords.filter { it.dateString == selectedDateKey } }
    }
    val displayEnergy = remember(
        selectedDateKey,
        todayKey,
        dailyEnergy,
        workoutLogs,
        cardioLogs,
        gpsLogs,
        profile.weightKg
    ) {
        if (selectedDateKey == todayKey) {
            dailyEnergy
        } else {
            val selectedWorkouts = workoutLogs.filter { dateFormatter.format(Date(it.dateMillis)) == selectedDateKey }
            val selectedCardio = cardioLogs.filter { dateFormatter.format(Date(it.dateMillis)) == selectedDateKey }
            val selectedRuns = gpsLogs.filter { dateFormatter.format(Date(it.dateMillis)) == selectedDateKey }
            val gymCalories = selectedWorkouts.sumOf {
                (it.durationMinutes.coerceAtLeast(1) * (profile.weightKg * 0.08f)).toInt().coerceIn(80, 800)
            }
            val selectedExerciseCalories = gymCalories +
                selectedCardio.sumOf { it.caloriesBurned } +
                selectedRuns.sumOf { it.caloriesBurned.toDouble() }.toInt()
            dailyEnergy.copy(
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
        }
    }

    val groupedMeals = remember(mealPlans) {
        mealPlans.groupBy { it.mealType }
    }
    val groupedConsumed = remember(selectedFoods) {
        selectedFoods.groupBy { it.mealType }
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
                    item(key = "diet_hero_banner", contentType = "banner") {
                        DietNutritionHeroBanner(profile = profile)
                    }

                    item(key = "diet_daily_energy_card", contentType = "energy_card") {
                        DietDailyEnergyCard(displayEnergy = displayEnergy)
                    }

                    item(key = "diet_quick_links_and_summary", contentType = "summary") {
                        DietQuickLinksAndSummaryCards(
                            profile = profile,
                            selectedFoods = selectedFoods,
                            onOpenFoodDatabaseTab = { selectedTab = 1 },
                            onSyncToShopping = {
                                viewModel.generateShoppingList(30)
                                onNavigateToShopping()
                            }
                        )
                    }

                    items(
                        items = MealType.entries,
                        key = { "meal_type_${it.name}" },
                        contentType = { "meal_section_card" }
                    ) { type ->
                        MealSectionCard(
                            type = type,
                            itemsForType = groupedMeals[type] ?: emptyList(),
                            consumedForType = groupedConsumed[type] ?: emptyList(),
                            selectedDateKey = selectedDateKey,
                            onAiSuggestMeal = { mealType ->
                                viewModel.suggestAIMealItems(mealType)
                                showAiPlanPreviewDialog = true
                            },
                            onAddFoodClick = { mealType ->
                                selectedMealTypeForAdd = mealType
                                showAddFoodDialog = true
                            },
                            onToggleFixed = { item -> viewModel.toggleMealItemFixed(item) },
                            onToggleCompleted = { item, dateKey, completed ->
                                viewModel.toggleMealItemCompleted(item, dateKey, completed)
                            },
                            onRemoveMealItem = { id -> viewModel.removeMealItem(id) }
                        )
                    }

                    item(key = "diet_add_custom_food_footer", contentType = "footer_button") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showCustomFoodDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_custom_food_dialog_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CarbonBorder)
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
                MonthlyDietConfigSection(
                    profile = profile,
                    onSave = { cycleType, notes, targetWeight, _ ->
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
