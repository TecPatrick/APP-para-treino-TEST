package com.example.ui.screens.diet.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FoodRecord
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.UserProfile
import com.example.ui.DailyEnergyExpenditure
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

@Composable
fun DietNutritionHeroBanner(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .height(106.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_virtus_nutrition_banner),
                contentDescription = "Banner de Nutrição Virtus",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                CarbonDark.copy(alpha = 0.90f),
                                CarbonDark.copy(alpha = 0.55f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = GrowthCyan.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = "NUTRIÇÃO DE PRECISÃO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GrowthCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Meta: ${profile.targetCalories} kcal • ${profile.targetProteinGrams}g Proteína",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Balanço energético dinâmico ajustado ao seu biotipo.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun DietDailyEnergyCard(
    displayEnergy: DailyEnergyExpenditure,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
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
                    border = BorderStroke(1.dp, CarbonBorder)
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
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            "GASTO TREINO",
                            fontSize = 9.sp,
                            color = if (displayEnergy.hasWorkoutToday) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (displayEnergy.hasWorkoutToday) "+${displayEnergy.totalWorkoutCalories} kcal" else "0 kcal",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = if (displayEnergy.hasWorkoutToday) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (displayEnergy.hasWorkoutToday) "${displayEnergy.gymWorkoutsCount} musc • ${displayEnergy.runningWorkoutsCount} corr" else "Sem treino neste dia",
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f))
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

@Composable
fun DietQuickLinksAndSummaryCards(
    profile: UserProfile,
    selectedFoods: List<FoodRecord>,
    onOpenFoodDatabaseTab: () -> Unit,
    onSyncToShopping: () -> Unit
) {
    val consumedCalories = remember(selectedFoods) { selectedFoods.sumOf { it.calories.toDouble() }.toInt() }
    val consumedProtein = remember(selectedFoods) { selectedFoods.sumOf { it.protein.toDouble() }.toFloat() }
    val consumedCarbs = remember(selectedFoods) { selectedFoods.sumOf { it.carbs.toDouble() }.toFloat() }
    val consumedFat = remember(selectedFoods) { selectedFoods.sumOf { it.fat.toDouble() }.toFloat() }

    Surface(
        onClick = onOpenFoodDatabaseTab,
        color = CarbonDark,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.35f)),
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
                Column(modifier = Modifier.weight(1f)) {
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

    Surface(
        onClick = onSyncToShopping,
        color = CarbonCard,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .testTag("sync_diet_to_shopping_card")
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
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = GrowthCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SINCRONIZADO COM LISTA DE COMPRAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthCyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Agrupamento automático por Corredor de Macros (Proteína, Carbo, Gordura)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                color = GrowthCyan,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Abrir Lista",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonBorder),
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
                    text = "$consumedCalories / ${profile.targetCalories} kcal",
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
                    current = consumedProtein,
                    target = profile.targetProteinGrams.toFloat(),
                    color = GrowthCyan,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    title = "Carboidratos",
                    current = consumedCarbs,
                    target = profile.targetCarbsGrams.toFloat(),
                    color = GrowthYellow,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    title = "Gorduras",
                    current = consumedFat,
                    target = profile.targetFatGrams.toFloat(),
                    color = GrowthOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MealSectionCard(
    type: MealType,
    itemsForType: List<MealPlanItem>,
    consumedForType: List<FoodRecord>,
    selectedDateKey: String,
    onAiSuggestMeal: (MealType) -> Unit,
    onAddFoodClick: (MealType) -> Unit,
    onToggleFixed: (MealPlanItem) -> Unit,
    onToggleCompleted: (MealPlanItem, String, Boolean) -> Unit,
    onRemoveMealItem: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val consumedCals = remember(consumedForType) { consumedForType.sumOf { it.calories.toDouble() }.toInt() }
    val extraRecords = remember(consumedForType, itemsForType) {
        consumedForType.filter { record ->
            itemsForType.none { it.foodItemId == record.foodItemId }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
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
                        text = "$consumedCals kcal consumidas • ${itemsForType.size} item(ns) planejados",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAiSuggestMeal(type) },
                        modifier = Modifier.testTag("ai_suggest_meal_btn_${type.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sugestão IA",
                            tint = GrowthCyan
                        )
                    }

                    IconButton(
                        onClick = { onAddFoodClick(type) },
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
                                onClick = { onToggleFixed(item) },
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
                                    onToggleCompleted(item, selectedDateKey, completedForDate)
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
                                onClick = { onRemoveMealItem(item.id) },
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
