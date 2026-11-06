package com.example.ui

import androidx.compose.ui.graphics.Color
import com.example.ui.components.getIntensityColor
import org.junit.Assert.assertEquals
import org.junit.Test

class MuscleHeatmapColorTest {
    @Test
    fun `rest and invalid values stay neutral`() {
        for (ratio in listOf(0f, -1f, Float.NaN, Float.POSITIVE_INFINITY)) {
            assertEquals(Color(0xFF252D3D), getIntensityColor(ratio))
        }
    }

    @Test
    fun `volume thresholds match the Virtus Cyan and Orange thermal legend`() {
        assertEquals(Color(0xFF00B8D4), getIntensityColor(0.01f))
        assertEquals(Color(0xFF00E5FF), getIntensityColor(0.25f))
        assertEquals(Color(0xFFFF9100), getIntensityColor(0.5f))
        assertEquals(Color(0xFFFF6D00), getIntensityColor(0.75f))
        assertEquals(Color(0xFFFF6D00), getIntensityColor(1f))
    }
}
