package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Color palette: Blue (low/rest) -> Cyan -> Green -> Yellow -> Orange (high intensity)
fun getIntensityColor(ratio: Float): Color {
    return when {
        ratio <= 0f -> Color(0xFF2A2E3D) // Untrained / Rest
        ratio < 0.25f -> Color(0xFF1E88E5) // Blue (Low)
        ratio < 0.5f -> Color(0xFF00ACC1)  // Cyan
        ratio < 0.75f -> Color(0xFFFFA726) // Orange/Yellow
        else -> Color(0xFFFF5722)         // Deep Orange / Red-Orange (High)
    }
}

@Composable
fun MuscleHeatmapView(
    muscleVolumes: Map<String, Float>,
    selectedTimeframeDays: Int,
    onTimeframeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMuscle by remember { mutableStateOf<String?>(null) }
    val maxVolume = muscleVolumes.values.maxOrNull() ?: 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
        border = BorderStroke(1.dp, Color(0xFF2A2E3D))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mapa de Calor Muscular",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFF1E88E5), RoundedCornerShape(2.dp)))
                    Text("Baixo", fontSize = 10.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFFF5722), RoundedCornerShape(2.dp)))
                    Text("Alto", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeframe Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val timeframes = listOf(7 to "7 Dias", 30 to "30 Dias", 0 to "Todo Período")
                timeframes.forEach { (days, label) ->
                    val isSelected = selectedTimeframeDays == days
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeframeSelected(days) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF5722),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E222E),
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color(0xFFFF5722),
                            borderColor = Color(0xFF2A2E3D)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Canvas & Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Body (Anterior)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anterior", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(100.dp)
                    ) {
                        drawAnteriorBody(muscleVolumes, maxVolume, selectedMuscle) { clicked ->
                            selectedMuscle = clicked
                        }
                    }
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.8f)
                        .background(Color(0xFF2A2E3D))
                )

                // Right Body (Posterior)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Posterior", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(100.dp)
                    ) {
                        drawPosteriorBody(muscleVolumes, maxVolume, selectedMuscle) { clicked ->
                            selectedMuscle = clicked
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Muscle Info Card
            val activeMuscle = selectedMuscle ?: muscleVolumes.maxByOrNull { it.value }?.key ?: "Nenhum"
            val volume = muscleVolumes[activeMuscle] ?: 0f

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1E222E),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Músculo em Destaque",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = activeMuscle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Volume Total",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${volume.toInt()} kg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawAnteriorBody(
    volumes: Map<String, Float>,
    maxVolume: Float,
    selected: String?,
    onClick: (String) -> Unit
) {
    val centerX = size.width / 2f
    
    // Head
    drawCircle(Color(0xFF2A2E3D), radius = 18f, center = Offset(centerX, 25f))
    
    // Shoulders / Deltoids
    val shoulderRatio = (volumes["Ombros"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawOval(
        color = getIntensityColor(shoulderRatio),
        topLeft = Offset(centerX - 35f, 48f),
        size = Size(70f, 25f)
    )
    
    // Chest / Peitoral
    val chestRatio = (volumes["Peitoral"] ?: volumes["Peito"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(chestRatio),
        topLeft = Offset(centerX - 28f, 75f),
        size = Size(56f, 40f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    
    // Biceps / Arms
    val armRatio = (volumes["Bíceps"] ?: volumes["Braços"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRect(color = getIntensityColor(armRatio), topLeft = Offset(centerX - 46f, 78f), size = Size(14f, 45f))
    drawRect(color = getIntensityColor(armRatio), topLeft = Offset(centerX + 32f, 78f), size = Size(14f, 45f))

    // Abs / Core
    val absRatio = (volumes["Abstrôminal"] ?: volumes["Core"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(absRatio),
        topLeft = Offset(centerX - 20f, 118f),
        size = Size(40f, 50f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Quads / Legs
    val legRatio = (volumes["Pernas"] ?: volumes["Quadríceps"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(legRatio),
        topLeft = Offset(centerX - 26f, 172f),
        size = Size(22f, 75f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = getIntensityColor(legRatio),
        topLeft = Offset(centerX + 4f, 172f),
        size = Size(22f, 75f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
}

private fun DrawScope.drawPosteriorBody(
    volumes: Map<String, Float>,
    maxVolume: Float,
    selected: String?,
    onClick: (String) -> Unit
) {
    val centerX = size.width / 2f
    
    // Head (Back)
    drawCircle(Color(0xFF2A2E3D), radius = 18f, center = Offset(centerX, 25f))
    
    // Upper Back / Trapezius
    val backRatio = (volumes["Costas"] ?: volumes["Dorsais"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(backRatio),
        topLeft = Offset(centerX - 30f, 48f),
        size = Size(60f, 45f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    
    // Triceps / Arms Back
    val tricepRatio = (volumes["Tríceps"] ?: volumes["Braços"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRect(color = getIntensityColor(tricepRatio), topLeft = Offset(centerX - 46f, 78f), size = Size(14f, 45f))
    drawRect(color = getIntensityColor(tricepRatio), topLeft = Offset(centerX + 32f, 78f), size = Size(14f, 45f))

    // Glutes / Lower Back
    val gluteRatio = (volumes["Glúteos"] ?: volumes["Pernas"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(gluteRatio),
        topLeft = Offset(centerX - 26f, 96f),
        size = Size(52f, 40f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Hamstrings / Calves
    val legRatio = (volumes["Pernas"] ?: volumes["Posterior"] ?: 0f) / maxVolume.coerceAtLeast(1f)
    drawRoundRect(
        color = getIntensityColor(legRatio),
        topLeft = Offset(centerX - 26f, 139f),
        size = Size(22f, 108f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = getIntensityColor(legRatio),
        topLeft = Offset(centerX + 4f, 139f),
        size = Size(22f, 108f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
}
