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
}
