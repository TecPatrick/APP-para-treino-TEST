package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GrowthOrange,
    onPrimary = Color.White,
    primaryContainer = GrowthOrangeContainer,
    onPrimaryContainer = GrowthOnOrangeContainer,
    secondary = GrowthCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00363A),
    onSecondaryContainer = Color(0xFFB2EBF2),
    tertiary = GrowthGreen,
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF00391A),
    onTertiaryContainer = Color(0xFFB9F6CA),
    background = CarbonDark,
    onBackground = TextPrimaryDark,
    surface = CarbonCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = CarbonCardElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = CarbonBorder,
    error = GrowthRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = GrowthOrangeLightScheme,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDBCF),
    onPrimaryContainer = Color(0xFF380D00),
    secondary = Color(0xFF00838F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F7FA),
    onSecondaryContainer = Color(0xFF00363A),
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = Color(0xFF1B5E20),
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    error = GrowthRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek dark fitness aesthetic
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
