package com.example.ui.screens.diet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.MealType
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
fun FoodDatabaseSection(
    foods: List<FoodItem>,
    onAddFoodToMeal: (FoodItem, MealType, Float, String) -> Unit,
    onOpenCustomFoodDialog: () -> Unit,
    onReloadDefaults: () -> Unit,
    onToggleFavorite: (FoodItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var foodToAddToMeal by remember { mutableStateOf<FoodItem?>(null) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val filteredFoods = remember(foods, searchQuery, selectedCategory, showOnlyFavorites) {
        foods.filter { food ->
            val matchesQuery = searchQuery.isBlank() ||
                food.name.contains(searchQuery, ignoreCase = true) ||
                food.category.label.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || food.category == selectedCategory
            val matchesFavorite = !showOnlyFavorites || food.isFavorite
            matchesQuery && matchesCategory && matchesFavorite
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 🔍 Modern Real-time Search Bar
        Surface(
            color = CarbonCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { 
                    Text(
                        "Buscar por nome ou categoria (ex: frango, suplemento...)", 
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ) 
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GrowthOrange,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar busca",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_database_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GrowthOrange.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = GrowthOrange
                ),
                shape = RoundedCornerShape(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🏷️ Dynamic Category Filter Chips
        Text(
            text = "FILTRAR POR CATEGORIA",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            letterSpacing = 1.sp
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("Todos", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrowthOrange,
                        selectedLabelColor = Color.Black,
                        containerColor = CarbonCard,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = BorderStroke(1.dp, if (selectedCategory == null) GrowthOrange else CarbonBorder),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_all_foods")
                )
            }
            item {
                val favCount = foods.count { it.isFavorite }
                FilterChip(
                    selected = showOnlyFavorites,
                    onClick = { showOnlyFavorites = !showOnlyFavorites },
                    label = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (showOnlyFavorites) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (showOnlyFavorites) Color.White else GrowthOrange
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Favoritos ($favCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrowthOrange,
                        selectedLabelColor = Color.White,
                        containerColor = CarbonCard,
                        labelColor = GrowthOrange
                    ),
                    border = BorderStroke(1.dp, GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("filter_favorites")
                )
            }
            items(FoodCategory.values()) { cat ->
                val count = foods.count { it.category == cat }
                if (count > 0) {
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat.label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (cat == FoodCategory.GROWTH_SUPPLEMENT) GrowthOrange else GrowthCyan,
                            selectedLabelColor = Color.Black,
                            containerColor = CarbonCard,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, if (selectedCategory == cat) (if (cat == FoodCategory.GROWTH_SUPPLEMENT) GrowthOrange else GrowthCyan) else CarbonBorder),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("filter_category_${cat.name}")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📊 Results Summary & Action Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${filteredFoods.size} ITENS ENCONTRADOS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (filteredFoods.isEmpty()) GrowthOrange else GrowthGreen,
                    letterSpacing = 0.5.sp
                )
                if (searchQuery.isNotEmpty() || selectedCategory != null) {
                    Text(
                        text = "Filtro ativo: ${if (searchQuery.isNotEmpty()) "\"$searchQuery\" " else ""}${selectedCategory?.label ?: ""}",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Quick Action: Clear Filters
                if (searchQuery.isNotEmpty() || selectedCategory != null) {
                    TextButton(
                        onClick = { 
                            searchQuery = ""
                            selectedCategory = null
                            showOnlyFavorites = false
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Limpar", fontSize = 11.sp, color = GrowthOrange)
                    }
                }

                Button(
                    onClick = onOpenCustomFoodDialog,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan),
                    modifier = Modifier.height(32.dp).testTag("add_custom_food_top_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // List of food items
        if (filteredFoods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Nenhum alimento encontrado",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tente outra busca ou cadastre um novo alimento na base.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onReloadDefaults,
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                    ) {
                        Text("Recarregar 117+ Alimentos Padrão", color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredFoods, key = { it.id }) { food ->
                    FoodDatabaseItemCard(
                        food = food,
                        onAddToMealClick = { foodToAddToMeal = food },
                        onToggleFavorite = { onToggleFavorite(food) }
                    )
                }
            }
        }
    }

    // Modal to add selected food to a meal
    foodToAddToMeal?.let { food ->
        QuickAddFoodToMealDialog(
            food = food,
            onDismiss = { foodToAddToMeal = null },
            onConfirm = { mealType, amount, notes ->
                onAddFoodToMeal(food, mealType, amount, notes)
                foodToAddToMeal = null
            }
        )
    }
}

@Composable
fun FoodDatabaseItemCard(
    food: FoodItem,
    onAddToMealClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (food.isGrowthProduct) GrowthOrange.copy(alpha = 0.5f) else CarbonBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_card_${food.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (food.isGrowthProduct) {
                            Surface(
                                color = GrowthOrange,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "GROWTH SUPLEMENTOS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = CarbonDark,
                                shape = RoundedCornerShape(4.dp),
                                border = BorderStroke(1.dp, CarbonBorder)
                            ) {
                                Text(
                                    text = food.category.label.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = food.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Text(
                        text = "Porção de referência: ${food.baseServingAmount.toInt()} ${food.servingUnit}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp).testTag("toggle_favorite_${food.id}")
                    ) {
                        Icon(
                            imageVector = if (food.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (food.isFavorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                            tint = if (food.isFavorite) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onAddToMealClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.6f)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("add_to_meal_btn_${food.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GrowthOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Refeição",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Macro details grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CarbonDark, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Calorias
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CAL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${food.calories.toInt()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Proteína
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PROT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthCyan
                    )
                    Text(
                        text = "${food.protein}g",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GrowthCyan
                    )
                }

                // Carbos
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CARB",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthYellow
                    )
                    Text(
                        text = "${food.carbs}g",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GrowthYellow
                    )
                }

                // Gorduras
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GORD",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrange
                    )
                    Text(
                        text = "${food.fat}g",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GrowthOrange
                    )
                }

                // Fibras
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FIBRA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthGreen
                    )
                    Text(
                        text = "${food.fiber}g",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = GrowthGreen
                    )
                }
            }
        }
    }
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

                // Meal selector chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(MealType.values()) { meal ->
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

                // Live macro calculation preview
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
                            Text("${String.format("%.1f", calculatedProtein)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CARB", fontSize = 9.sp, color = GrowthYellow)
                            Text("${String.format("%.1f", calculatedCarbs)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthYellow)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("GORD", fontSize = 9.sp, color = GrowthOrange)
                            Text("${String.format("%.1f", calculatedFat)}g", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
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
