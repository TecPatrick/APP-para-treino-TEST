package com.example.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.local.PrepopulatedData
import com.example.data.model.FoodCategory
import com.example.data.model.ItemPriceInflationStat
import com.example.data.model.PurchaseItemLog
import com.example.data.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Corredores de Supermercado agrupados exclusivamente por Macronutrientes
 * conforme preferência do usuário (Proteína, Carbo, Gordura).
 */
enum class MacronutrientAisle(
    val title: String,
    val subtitle: String,
    val badgeLabel: String
) {
    PROTEIN(
        title = "Corredor de Proteínas",
        subtitle = "Carnes, frango, ovos, laticínios e suplementos proteicos",
        badgeLabel = "PROTEÍNA"
    ),
    CARBOHYDRATE(
        title = "Corredor de Carboidratos",
        subtitle = "Arroz, aveia, tubérculos, pães, frutas e fibras",
        badgeLabel = "CARBO"
    ),
    FAT(
        title = "Corredor de Gorduras",
        subtitle = "Azeite, pasta de amendoim, castanhas e fontes lipídicas",
        badgeLabel = "GORDURA"
    );

    companion object {
        fun fromItem(item: ShoppingItem): MacronutrientAisle {
            val name = item.name.lowercase()
            return when (item.category) {
                FoodCategory.PROTEIN, FoodCategory.DAIRY -> PROTEIN
                FoodCategory.CARBOHYDRATE, FoodCategory.FRUITS_VEGETABLES -> CARBOHYDRATE
                FoodCategory.FAT -> FAT
                FoodCategory.GROWTH_SUPPLEMENT, FoodCategory.OTHER -> {
                    when {
                        name.contains("pasta de amendoim") ||
                            name.contains("azeite") ||
                            name.contains("ômega") ||
                            name.contains("omega") ||
                            name.contains("castanha") ||
                            name.contains("amêndoa") ||
                            name.contains("abacate") ||
                            name.contains("manteiga") -> FAT
                        name.contains("aveia") ||
                            name.contains("arroz") ||
                            name.contains("batata") ||
                            name.contains("macarrão") ||
                            name.contains("pão") ||
                            name.contains("banana") ||
                            name.contains("maltodextrina") ||
                            name.contains("tapioca") -> CARBOHYDRATE
                        else -> PROTEIN
                    }
                }
            }
        }
    }
}

/**
 * Unified ShoppingViewModel (merging ShoppingViewModel and ShoppingListViewModel).
 */
class ShoppingViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    private val _selectedPeriodDays = MutableStateFlow(30)
    val selectedPeriodDays: StateFlow<Int> = _selectedPeriodDays.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    val categorizedItems: StateFlow<Map<FoodCategory, List<ShoppingItem>>> = shoppingList
        .map { items -> items.groupBy { it.category } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val aisleGroupedItems: StateFlow<Map<MacronutrientAisle, List<ShoppingItem>>> = shoppingList
        .map { items ->
            val grouped = items.groupBy { MacronutrientAisle.fromItem(it) }
            val orderedMap = linkedMapOf<MacronutrientAisle, List<ShoppingItem>>()
            for (aisle in MacronutrientAisle.entries) {
                val aisleItems = grouped[aisle].orEmpty()
                    .sortedWith(compareBy<ShoppingItem> { it.isChecked }.thenBy { it.name.lowercase() })
                if (aisleItems.isNotEmpty()) {
                    orderedMap[aisle] = aisleItems
                }
            }
            orderedMap
        }
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

    init {
        viewModelScope.launch {
            repository.mealPlanItems.collectLatest { meals ->
                val currentShopping = repository.shoppingList.firstOrNull().orEmpty()
                if (meals.isNotEmpty() || currentShopping.none { it.isAutoGenerated }) {
                    syncShoppingListInternal(periodDays = _selectedPeriodDays.value, showToast = false)
                }
            }
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun setPeriodDays(days: Int) {
        _selectedPeriodDays.value = days
        viewModelScope.launch {
            syncShoppingListInternal(periodDays = days, showToast = true)
        }
    }

    fun generateShoppingList(periodDays: Int = 30) {
        parseDietPlanAndGenerateShoppingList(periodDays)
    }

    fun parseDietPlanAndGenerateShoppingList(periodDays: Int = _selectedPeriodDays.value) {
        _selectedPeriodDays.value = periodDays
        viewModelScope.launch {
            syncShoppingListInternal(periodDays = periodDays, showToast = true)
            syncController.enqueueChange("Dieta", "Lista de compras recalculada ($periodDays dias)")
        }
    }

    private suspend fun syncShoppingListInternal(periodDays: Int, showToast: Boolean) {
        _isLoading.value = true
        try {
            var currentMealPlans = repository.mealPlanItems.firstOrNull() ?: emptyList()
            if (currentMealPlans.isEmpty()) {
                currentMealPlans = PrepopulatedData.defaultMealPlan
            }

            if (currentMealPlans.isEmpty()) {
                if (showToast) {
                    _uiMessage.value = "Nenhum alimento cadastrado na sua dieta para gerar a lista."
                }
                return
            }

            val existingItems = repository.shoppingList.firstOrNull() ?: emptyList()
            val previouslyAcquiredNames = existingItems
                .filter { it.isChecked }
                .map { it.name.trim().lowercase() }
                .toSet()

            val aggregatedDailyIntake = mutableMapOf<String, Triple<FoodCategory, Float, String>>()
            for (item in currentMealPlans) {
                val key = item.foodName.trim()
                val existing = aggregatedDailyIntake[key]
                val updatedAmount = (existing?.second ?: 0f) + item.amount
                aggregatedDailyIntake[key] = Triple(item.foodCategory, updatedAmount, item.unit)
            }

            repository.clearAutoGeneratedShoppingItems()

            val generatedItems = aggregatedDailyIntake.map { (foodName, categoryAmountUnit) ->
                val (category, dailyAmount, originalUnit) = categoryAmountUnit
                val totalPeriodAmount = dailyAmount * periodDays

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
                    .ifBlank { foodName.trim() }

                val (displayQty, displayUnit) = when {
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
                    foodName.contains("Azeite", ignoreCase = true) -> {
                        val bottles = ceil((adjustedTotalAmount / 500f).toDouble()).coerceAtLeast(1.0).toFloat()
                        bottles to "garrafa (500ml)"
                    }
                    adjustedTotalAmount >= 1000f -> {
                        val kg = adjustedTotalAmount / 1000f
                        val roundedKg = (Math.round(kg * 10f) / 10f)
                        roundedKg to "kg"
                    }
                    else -> {
                        val roundedGrams = (Math.round(adjustedTotalAmount / 10f) * 10).toFloat().coerceAtLeast(10f)
                        val unitLabel = if (category == FoodCategory.DAIRY || originalUnit.equals("ml", ignoreCase = true)) "L" else "g"
                        roundedGrams to unitLabel
                    }
                }

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

                val estimatedPrice = if (displayUnit == "g" || displayUnit == "ml") {
                    (displayQty / 1000f) * pricePerKgOrUnit
                } else {
                    displayQty * pricePerKgOrUnit
                }

                val wasAcquired = previouslyAcquiredNames.contains(shoppingName.lowercase()) ||
                    previouslyAcquiredNames.contains(foodName.trim().lowercase())

                ShoppingItem(
                    name = shoppingName,
                    category = category,
                    quantity = displayQty,
                    unit = displayUnit,
                    periodDays = periodDays,
                    isChecked = wasAcquired,
                    estimatedPrice = estimatedPrice,
                    myEstimatedPrice = estimatedPrice,
                    partnerEstimatedPrice = 0f,
                    isAutoGenerated = true,
                    notes = "Dieta: ${dailyAmount.toInt()}${originalUnit}/dia • ${if (rawConversionFactor != 1.0f) "Convertido p/ cru" else "Peso integral"}"
                )
            }

            repository.insertShoppingItems(generatedItems)
            if (showToast) {
                _uiMessage.value = "Lista sincronizada com sua dieta para $periodDays dias!"
            }
        } catch (e: Exception) {
            if (showToast) {
                _uiMessage.value = "Erro ao processar plano alimentar: ${e.localizedMessage}"
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
            syncController.enqueueChange("Dieta", "Item mercado: ${item.name}")
        }
    }

    fun toggleShoppingItem(id: Long) {
        viewModelScope.launch {
            val item = shoppingList.value.firstOrNull { it.id == id }
            if (item != null) {
                repository.toggleShoppingItem(item)
                syncController.enqueueChange("Dieta", "Item mercado: ${item.name}")
            }
        }
    }

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
            syncController.enqueueChange("Dieta", "Novo item mercado: ${name.trim()}")
            _uiMessage.value = "Item extra \"${name.trim()}\" adicionado ao corredor!"
        }
    }

    fun addManualShoppingItem(
        name: String,
        category: FoodCategory,
        quantity: Float,
        unit: String,
        price: Float
    ) {
        addExtraProduct(name, category, quantity, unit, price)
    }

    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
            syncController.enqueueChange("Dieta", "Item removido da lista de compras")
        }
    }

    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            repository.clearCheckedShoppingItems()
            syncController.enqueueChange("Dieta", "Limpeza de itens comprados")
            _uiMessage.value = "Itens adquiridos removidos da lista."
        }
    }

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

    fun registerPurchaseReceipt(
        storeName: String,
        receiptNumber: String,
        dateMillis: Long,
        items: List<PurchaseItemLog>,
        notes: String
    ) {
        viewModelScope.launch {
            repository.registerPurchaseReceipt(storeName, receiptNumber, dateMillis, items, notes)
            showToast("Nota Fiscal cadastrada e preços atualizados no histórico!")
        }
    }

    fun deletePurchaseReceipt(id: Long) {
        viewModelScope.launch {
            repository.deleteReceipt(id)
            showToast("Nota Fiscal removida.")
        }
    }

    fun computeInflationStats(logs: List<PurchaseItemLog>): List<ItemPriceInflationStat> {
        return repository.computeInflationStats(logs)
    }
}
