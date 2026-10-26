package com.example.ui

import androidx.compose.ui.graphics.Color
import com.example.ui.components.getIntensityColor
import org.junit.Assert.assertEquals
import org.junit.Test

class MuscleHeatmapColorTest {
    @Test
    fun `rest and invalid values stay neutral`() {
        for (ratio in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertEquals(Color(0xFF526174), getIntensityColor(ratio))
        }
    }

    @Test
    fun `volume thresholds match the warm legend`() {
        assertEquals(Color(0xFFFFD6AD), getIntensityColor(0.01f))
        assertEquals(Color(0xFFFFAE73), getIntensityColor(0.25f))
        assertEquals(Color(0xFFFF854A), getIntensityColor(0.5f))
        assertEquals(Color(0xFFFF5722), getIntensityColor(0.75f))
        assertEquals(Color(0xFFFF5722), getIntensityColor(1f))
    }
}
