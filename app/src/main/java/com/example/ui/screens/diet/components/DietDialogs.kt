package com.example.ui.screens.diet.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealType
import com.example.ui.AiDietSuggestion
import com.example.ui.DietAiPreferences
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow
import java.util.Locale

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

    val filteredFoods by remember(foods, searchQuery, selectedCategory, showOnlyFavorites) {
        derivedStateOf {
            foods.filter { food ->
                val matchesQuery = searchQuery.isBlank() || food.name.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategory == null || food.category == selectedCategory
                val matchesFavorite = !showOnlyFavorites || food.isFavorite
                matchesQuery && matchesCategory && matchesFavorite
            }
        }
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item(key = "filter_all") {
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
                        item(key = "filter_fav") {
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
                        items(
                            items = FoodCategory.entries,
                            key = { it.name },
                            contentType = { "category_chip" }
                        ) { cat ->
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
                        items(
                            items = filteredFoods,
                            key = { it.id },
                            contentType = { "food_option" }
                        ) { food ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
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
                item(key = "custom_food_name") {
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

                item(key = "custom_food_serving") {
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

                item(key = "custom_food_cals_prot") {
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

                item(key = "custom_food_carbs_fat") {
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
    onConfirm: (DietAiPreferences) -> Unit
) {
    var restrictions by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var mealsCount by remember { mutableIntStateOf(5) }
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
                Text(
                    "A IA criará um plano baseado no seu objetivo atual e nas suas preferências abaixo.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                Slider(
                    value = mealsCount.toFloat(),
                    onValueChange = { mealsCount = it.toInt() },
                    valueRange = 3f..7f,
                    steps = 3,
                    colors = SliderDefaults.colors(thumbColor = GrowthCyan, activeTrackColor = GrowthCyan)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = includeFavs,
                        onCheckedChange = { includeFavs = it },
                        colors = CheckboxDefaults.colors(checkedColor = GrowthCyan)
                    )
                    Text("Priorizar meus Alimentos Favoritos", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(DietAiPreferences(restrictions, preferences, mealsCount, includeFavs)) },
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
    suggestions: List<AiDietSuggestion>,
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GrowthCyan
                    )
                } else if (suggestions.isEmpty()) {
                    Text("Nenhuma sugestão gerada. Tente novamente.", modifier = Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = suggestions,
                            key = { "${it.mealType.name}_${it.foodName}_${it.amount}" },
                            contentType = { "ai_diet_suggestion" }
                        ) { item ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(item.mealType.title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                                    Text(item.foodName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("${item.amount.toInt()} ${item.unit} • ${item.calories.toInt()} kcal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (item.notes.isNotBlank()) {
                                        Text(item.notes, fontSize = 10.sp, color = GrowthOrangeLight, fontStyle = FontStyle.Italic)
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

@Composable
fun QuickAddFoodToMealDialog(
    food: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (MealType, Float, String) -> Unit
) {
    var selectedMealType by remember { mutableStateOf(MealType.LUNCH) }
    var amountText by remember { mutableStateOf(food.baseServingAmount.toInt().toString()) }
    var notesText by remember { mutableStateOf("") }

    val amount = amountText.toFloatOrNull() ?: food.baseServingAmount
    val ratio = if (food.baseServingAmount > 0f) amount / food.baseServingAmount else 1f

    val calculatedCalories = food.calories * ratio
    val calculatedProtein = food.protein * ratio
    val calculatedCarbs = food.carbs * ratio
    val calculatedFat = food.fat * ratio

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Text(
                    text = "Adicionar à Refeição",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = food.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GrowthOrange
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Selecione a refeição do dia:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = MealType.entries,
                        key = { it.name },
                        contentType = { "meal_chip" }
                    ) { meal ->
                        FilterChip(
                            selected = selectedMealType == meal,
                            onClick = { selectedMealType = meal },
                            label = { Text(meal.title, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrowthOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Quantidade (${food.servingUnit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_food_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CarbonDark,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CAL", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${calculatedCalories.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PROT", fontSize = 9.sp, color = GrowthCyan)
                            Text("${String.format(Locale.getDefault(), "%.1f", calculatedProtein)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CARB", fontSize = 9.sp, color = GrowthYellow)
                            Text("${String.format(Locale.getDefault(), "%.1f", calculatedCarbs)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthYellow)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GORD", fontSize = 9.sp, color = GrowthOrange)
                            Text("${String.format(Locale.getDefault(), "%.1f", calculatedFat)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Observação (opcional)") },
                    placeholder = { Text("Ex: pós-treino, grelhado sem óleo...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amountText.toFloatOrNull() ?: food.baseServingAmount
                    if (finalAmount > 0f) {
                        onConfirm(selectedMealType, finalAmount, notesText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_quick_add_food_btn")
            ) {
                Text("Adicionar ao Cardápio", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
