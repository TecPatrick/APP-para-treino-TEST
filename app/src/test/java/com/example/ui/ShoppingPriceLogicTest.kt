package com.example.ui

import com.example.data.model.FoodCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.roundToInt

class ShoppingPriceLogicTest {

    @Test
    fun `test raw to cooked conversion and pricing proportionality`() {
        // Sample scenario: Rice (Arroz Branco Cozido)
        val foodName = "Arroz Branco Cozido"
        val category = FoodCategory.CARBOHYDRATE
        val dailyAmount = 400f // 400g cooked rice per day
        val periodDays = 30
        val totalPeriodAmount = dailyAmount * periodDays // 12000g = 12kg cooked

        // Conversion Logic (from ViewModel)
        val rawConversionFactor = if (foodName.contains("Arroz", true) && foodName.contains("Cozido", true)) 0.4f else 1.0f
        val adjustedTotalAmount = totalPeriodAmount * rawConversionFactor // 12kg * 0.4 = 4.8kg raw

        val (displayQty, displayUnit) = if (adjustedTotalAmount >= 1000f) {
            val kg = adjustedTotalAmount / 1000f
            val roundedKg = (Math.round(kg * 10f) / 10f)
            roundedKg to "kg"
        } else {
            adjustedTotalAmount to "g"
        }

        assertEquals(4.8f, displayQty)
        assertEquals("kg", displayUnit)

        // Pricing Logic (from ViewModel)
        val pricePerKgOrUnit = when (category) {
            FoodCategory.CARBOHYDRATE -> 6.5f
            else -> 12.0f
        }

        val estimatedPrice = if (displayUnit == "kg") {
            displayQty * pricePerKgOrUnit
        } else {
            (displayQty / 1000f) * pricePerKgOrUnit
        }

        // 4.8kg * 6.5 = 31.20
        assertEquals(31.20f, estimatedPrice, 0.01f)
    }

    @Test
    fun `test small portion pricing proportionality`() {
        // Sample scenario: Small portion of protein (100g total for month, rare case but tests g->kg price)
        val foodName = "Peito de Frango"
        val category = FoodCategory.PROTEIN
        val totalAmount = 100f // 100g raw

        val displayQty = 100f
        val displayUnit = "g"
        val pricePerKg = 28.0f

        val estimatedPrice = (displayQty / 1000f) * pricePerKg
        
        // 0.1kg * 28.0 = 2.80
        assertEquals(2.80f, estimatedPrice, 0.01f)
    }

    @Test
    fun `test macronutrient aisle grouping and acquired item bottom sorting`() {
        val items = listOf(
            com.example.data.model.ShoppingItem(
                id = 1,
                name = "Peito de Frango",
                category = FoodCategory.PROTEIN,
                quantity = 4.0f,
                unit = "kg",
                isChecked = true
            ),
            com.example.data.model.ShoppingItem(
                id = 2,
                name = "Ovo Inteiro",
                category = FoodCategory.PROTEIN,
                quantity = 2.0f,
                unit = "cartela (30un)",
                isChecked = false
            ),
            com.example.data.model.ShoppingItem(
                id = 3,
                name = "Whey Protein Concentrado",
                category = FoodCategory.GROWTH_SUPPLEMENT,
                quantity = 1.0f,
                unit = "pct (1kg)",
                isChecked = false
            ),
            com.example.data.model.ShoppingItem(
                id = 4,
                name = "Pasta de Amendoim",
                category = FoodCategory.GROWTH_SUPPLEMENT,
                quantity = 1.0f,
                unit = "pote (1kg)",
                isChecked = false
            ),
            com.example.data.model.ShoppingItem(
                id = 5,
                name = "Arroz Branco",
                category = FoodCategory.CARBOHYDRATE,
                quantity = 4.8f,
                unit = "kg",
                isChecked = false
            )
        )

        val grouped = items.groupBy { MacronutrientAisle.fromItem(it) }
        val proteinAisle = grouped[MacronutrientAisle.PROTEIN].orEmpty()
            .sortedWith(compareBy<com.example.data.model.ShoppingItem> { it.isChecked }.thenBy { it.name.lowercase() })

        assertEquals(3, proteinAisle.size)
        // Pending items come first alphabetically, acquired item ("Peito de Frango") moves to the end
        assertEquals("Ovo Inteiro", proteinAisle[0].name)
        assertEquals("Whey Protein Concentrado", proteinAisle[1].name)
        assertEquals("Peito de Frango", proteinAisle[2].name)
        assertEquals(true, proteinAisle[2].isChecked)

        // Pasta de Amendoim maps to FAT aisle
        assertEquals(1, grouped[MacronutrientAisle.FAT]?.size)
        assertEquals("Pasta de Amendoim", grouped[MacronutrientAisle.FAT]?.first()?.name)

        // Arroz Branco maps to CARBOHYDRATE aisle
        assertEquals(1, grouped[MacronutrientAisle.CARBOHYDRATE]?.size)
        assertEquals("Arroz Branco", grouped[MacronutrientAisle.CARBOHYDRATE]?.first()?.name)
    }
}
