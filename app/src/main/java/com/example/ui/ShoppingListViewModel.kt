package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.VirtusApplication
import com.example.data.local.PrepopulatedData
import com.example.data.model.FoodCategory
import com.example.data.model.MealPlanItem
import com.example.data.model.ShoppingItem
import com.example.data.repository.VirtusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * ShoppingListViewModel
 * 
 * Manages the user's grocery shopping list generated from their diet plan.
 * Features:
 * - Parses the user's active diet plan / meal items and calculates total grocery requirements
 *   for selectable intervals (7 days, 15 days, 30 days).
 * - Categorizes items cleanly by FoodCategory (Proteínas, Carboidratos, Suplementos, etc.).
 * - Allows toggling checked items as purchased.
 * - Allows adding manual extra products and grocery items.
 * - Supports partner/joint shopping synchronization.
 */
class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VirtusRepository =
        (application as VirtusApplication).repository

    private val _selectedPeriodDays = MutableStateFlow(30)
    val selectedPeriodDays: StateFlow<Int> = _selectedPeriodDays.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // Base flow of shopping items from local persistence
    val shoppingList: StateFlow<List<ShoppingItem>> = repository.shoppingList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Automatically categorized items by FoodCategory for organized grocery shopping
    val categorizedItems: StateFlow<Map<FoodCategory, List<ShoppingItem>>> = shoppingList
        .map { items -> items.groupBy { it.category } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val totalItemsCount: StateFlow<Int> = shoppingList
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val checkedItemsCount: StateFlow<Int> = shoppingList
        .map { items -> items.count { it.isChecked } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalEstimatedCost: StateFlow<Float> = shoppingList
        .map { items -> items.sumOf { it.estimatedPrice.toDouble() }.toFloat() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val progressPercentage: StateFlow<Float> = shoppingList
        .map { items ->
            if (items.isEmpty()) 0f
            else items.count { it.isChecked }.toFloat() / items.size
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setPeriodDays(days: Int) {
        _selectedPeriodDays.value = days
    }

    /**
     * Parses the user's diet plan meals and automatically generates a categorized
     * grocery list with quantities and units scaled for [periodDays] days.
     */
    fun parseDietPlanAndGenerateShoppingList(periodDays: Int = _selectedPeriodDays.value) {
        _selectedPeriodDays.value = periodDays
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch meal plan items; fallback to default diet plan if empty
                var currentMealPlans = repository.mealPlanItems.firstOrNull() ?: emptyList()
                if (currentMealPlans.isEmpty()) {
                    currentMealPlans = PrepopulatedData.defaultMealPlan
                }

                if (currentMealPlans.isEmpty()) {
                    _uiMessage.value = "Nenhum alimento cadastrado na sua dieta para gerar a lista."
                    _isLoading.value = false
                    return@launch
                }

                // Aggregate daily portions by food name and category
                val aggregatedDailyIntake = mutableMapOf<String, Pair<FoodCategory, Float>>()
                for (item in currentMealPlans) {
                    val existing = aggregatedDailyIntake[item.foodName]
                    val updatedAmount = (existing?.second ?: 0f) + item.amount
                    aggregatedDailyIntake[item.foodName] = Pair(item.foodCategory, updatedAmount)
                }

                // Clear previous auto-generated items (preserves manual extra products)
                repository.clearAutoGeneratedShoppingItems()

                val generatedItems = aggregatedDailyIntake.map { (foodName, categoryAndAmount) ->
                    val (category, dailyAmount) = categoryAndAmount
                    val totalPeriodAmount = dailyAmount * periodDays

                    // 💡 Smart Conversion: Convert tracked 'Cooked/Grilled' weights to 'Raw' shopping quantities
                    // Factors: Rice (0.4), Beans (0.5), Chicken/Meat (1.35), Potato (1.0)
                    val rawConversionFactor = when {
                        foodName.contains("Arroz", true) && foodName.contains("Cozido", true) -> 0.4f
                        foodName.contains("Feijão", true) && foodName.contains("Cozido", true) -> 0.5f
                        foodName.contains("Frango", true) && (foodName.contains("Grelhado", true) || foodName.contains("Cozido", true)) -> 1.35f
                        foodName.contains("Carne", true) && (foodName.contains("Grelhado", true) || foodName.contains("Assado", true)) -> 1.4f
                        foodName.contains("Patinho", true) && (foodName.contains("Grelhado", true) || foodName.contains("Moído", true)) -> 1.4f
                        foodName.contains("Tilápia", true) || foodName.contains("Peixe", true) -> 1.25f
                        else -> 1.0f
                    }

                    val adjustedTotalAmount = totalPeriodAmount * rawConversionFactor
                    val shoppingName = foodName
                        .replace("Cozido", "", ignoreCase = true)
                        .replace("Grelhado", "", ignoreCase = true)
                        .replace("Assado", "", ignoreCase = true)
                        .replace("Grelhada", "", ignoreCase = true)
                        .replace("Cozida", "", ignoreCase = true)
                        .replace("Moído", "", ignoreCase = true)
                        .replace("Moída", "", ignoreCase = true)
                        .trim()

                    val (displayQty, displayUnit) = when {
                        // Growth Supplements
                        foodName.contains("Whey", ignoreCase = true) -> {
                            val pots = ceil((adjustedTotalAmount / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                            pots to "pct (1kg)"
                        }
                        foodName.contains("Creatina", ignoreCase = true) -> {
                            val pots = ceil((adjustedTotalAmount / 250f).toDouble()).coerceAtLeast(1.0).toFloat()
                            pots to "pote (250g)"
                        }
                        foodName.contains("Pasta de Amendoim", ignoreCase = true) -> {
                            val pots = ceil((adjustedTotalAmount / 1000f).toDouble()).coerceAtLeast(1.0).toFloat()
                            pots to "pote (1kg)"
                        }
                        foodName.contains("Glutamina", ignoreCase = true) -> {
                            val pots = ceil((adjustedTotalAmount / 300f).toDouble()).coerceAtLeast(1.0).toFloat()
                            pots to "pote (300g)"
                        }
                        foodName.contains("Multivitamínico", ignoreCase = true) || foodName.contains("Ômega", ignoreCase = true) -> {
                            1f to "frasco (120 caps)"
                        }
                        // Eggs & Egg Whites
                        foodName.contains("Ovo", ignoreCase = true) -> {
                            val totalEggs = (adjustedTotalAmount / 50f).roundToInt()
                            val cartelas = ceil(totalEggs / 30.0).toFloat()
                            if (cartelas >= 1f) {
                                cartelas to "cartela (30un)"
                            } else {
                                totalEggs.toFloat() to "unidades"
                            }
                        }
                        foodName.contains("Clara", ignoreCase = true) -> {
                            val liters = ceil(adjustedTotalAmount / 1000f).coerceAtLeast(1f)
                            liters to "garrafa (1L)"
                        }
                        // Oils & Condiments
                        foodName.contains("Azeite", ignoreCase = true) -> {
                            val bottles = ceil((adjustedTotalAmount / 500f).toDouble()).coerceAtLeast(1.0).toFloat()
                            bottles to "garrafa (500ml)"
                        }
                        // Bulk Foods (over 1000g)
                        adjustedTotalAmount >= 1000f -> {
                            val kg = adjustedTotalAmount / 1000f
                            val roundedKg = (Math.round(kg * 10f) / 10f)
                            roundedKg to "kg"
                        }
                        // Small portions (under 1kg)
                        else -> {
                            val roundedGrams = (Math.round(adjustedTotalAmount / 10f) * 10).toFloat()
                            val unitLabel = if (category == FoodCategory.DAIRY) "L" else "g"
                            roundedGrams to unitLabel
                        }
                    }

                    // 💡 Refined Price Calculation: Consolidates price per KG and ensures precise conversion
                    val pricePerKgOrUnit = when {
                        foodName.contains("Whey", ignoreCase = true) -> 105.0f
                        foodName.contains("Creatina", ignoreCase = true) -> 89.0f
                        foodName.contains("Pasta de Amendoim", ignoreCase = true) -> 29.0f
                        foodName.contains("Glutamina", ignoreCase = true) -> 75.0f
                        foodName.contains("Multivitamínico", ignoreCase = true) || foodName.contains("Ômega", ignoreCase = true) -> 45.0f
                        foodName.contains("Ovo", ignoreCase = true) -> if (displayUnit.contains("cartela")) 21.0f else 0.75f
                        foodName.contains("Clara", ignoreCase = true) -> 18.0f
                        foodName.contains("Azeite", ignoreCase = true) -> 38.0f
                        else -> when (category) {
                            FoodCategory.PROTEIN -> 28.0f
                            FoodCategory.CARBOHYDRATE -> 6.5f
                            FoodCategory.FAT -> 45.0f
                            FoodCategory.FRUITS_VEGETABLES -> 9.5f
                            FoodCategory.DAIRY -> 7.5f
                            else -> 12.0f
                        }
                    }

                    // Final price calculation with gram-to-kilogram conversion where applicable
                    val estimatedPrice = if (displayUnit == "g" || displayUnit == "ml") {
                        (displayQty / 1000f) * pricePerKgOrUnit
                    } else {
                        displayQty * pricePerKgOrUnit
                    }

                    ShoppingItem(
                        name = shoppingName,
                        category = category,
                        quantity = displayQty,
                        unit = displayUnit,
                        periodDays = periodDays,
                        isChecked = false,
                        estimatedPrice = estimatedPrice,
                        myEstimatedPrice = estimatedPrice,
                        partnerEstimatedPrice = 0f,
                        isAutoGenerated = true,
                        notes = "Dieta: ${dailyAmount.toInt()}g/dia de item ${if (rawConversionFactor != 1.0f) "preparado" else "fresco"}"
                    )
                }

                repository.insertShoppingItems(generatedItems)
                _uiMessage.value = "Lista de compras gerada com sucesso para $periodDays dias!"
            } catch (e: Exception) {
                _uiMessage.value = "Erro ao processar plano alimentar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggles checked state for a shopping item.
     */
    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
        }
    }

    fun toggleShoppingItem(id: Long) {
        viewModelScope.launch {
            val item = shoppingList.value.firstOrNull { it.id == id }
            if (item != null) {
                repository.toggleShoppingItem(item)
            }
        }
    }

    /**
     * Adds an extra product manually to the grocery list.
     */
    fun addExtraProduct(
        name: String,
        category: FoodCategory,
        quantity: Float,
        unit: String,
        price: Float = 0f,
        notes: String = "Item extra adicionado"
    ) {
        viewModelScope.launch {
            repository.addShoppingItem(
                ShoppingItem(
                    name = name.trim(),
                    category = category,
                    quantity = quantity.coerceAtLeast(0.1f),
                    unit = unit.trim().ifBlank { "un" },
                    estimatedPrice = price.coerceAtLeast(0f),
                    myEstimatedPrice = price.coerceAtLeast(0f),
                    isAutoGenerated = false,
                    notes = notes
                )
            )
            _uiMessage.value = "Item extra \"${name.trim()}\" adicionado à lista!"
        }
    }

    /**
     * Compatibility alias for manual product creation.
     */
    fun addManualShoppingItem(
        name: String,
        category: FoodCategory,
        quantity: Float,
        unit: String,
        price: Float
    ) {
        addExtraProduct(name, category, quantity, unit, price)
    }

    /**
     * Removes an item from the grocery list.
     */
    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    /**
     * Clears all checked / purchased items from the shopping list.
     */
    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            repository.clearCheckedShoppingItems()
            _uiMessage.value = "Itens comprados removidos da lista."
        }
    }

    // --- Joint Shopping Code Sharing ---

    fun exportShoppingCode(authorName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.exportShoppingListAsShareCode(authorName)
            onResult(code)
        }
    }

    fun importAndMergePartnerShopping(code: String, partnerName: String) {
        viewModelScope.launch {
            val success = repository.importAndMergePartnerShoppingList(code, partnerName)
            if (success) {
                _uiMessage.value = "Lista do $partnerName fundida com sucesso!"
            } else {
                _uiMessage.value = "Código inválido. Verifique o texto copiado."
            }
        }
    }

    fun unlinkPartnerShopping() {
        viewModelScope.launch {
            repository.unlinkPartnerShoppingList()
            _uiMessage.value = "Lista desvinculada. Exibindo apenas seus itens."
        }
    }
}
