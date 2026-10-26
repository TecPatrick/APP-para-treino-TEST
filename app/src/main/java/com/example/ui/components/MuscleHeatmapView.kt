package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Hevy-style fitness color palette: Blue (low) -> Cyan -> Yellow -> Orange -> Deep Red/Orange (high intensity)
fun getIntensityColor(ratio: Float): Color {
    return when {
        ratio <= 0f -> Color(0xFF222634) // Untrained / Rest
        ratio < 0.25f -> Color(0xFF1E88E5) // Blue
        ratio < 0.5f -> Color(0xFF00ACC1)  // Cyan
        ratio < 0.75f -> Color(0xFFFFB300) // Amber/Yellow
        else -> Color(0xFFFF5722)         // Flame Orange / Red
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12151C)),
        border = BorderStroke(1.dp, Color(0xFF252A38))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Foco Muscular",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Volume por grupamento",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // Intensity Legend Pill
                Surface(
                    color = Color(0xFF1A1F2C),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF2A3142))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF1E88E5), RoundedCornerShape(4.dp)))
                        Text("Baixo", fontSize = 10.sp, color = Color.Gray)
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5722), RoundedCornerShape(4.dp)))
                        Text("Alto", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF5722),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1A1F2C),
                            labelColor = Color.LightGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color(0xFFFF5722),
                            borderColor = Color(0xFF2A3142)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Body Silhouettes (Anterior & Posterior) Side by Side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Anterior Body
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Anterior", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(120.dp)
                    ) {
                        drawAnteriorSilhouette(muscleVolumes, maxVolume)
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight(0.75f)
                        .background(Color(0xFF252A38))
                )

                // Posterior Body
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Posterior", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(120.dp)
                    ) {
                        drawPosteriorSilhouette(muscleVolumes, maxVolume)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted Muscle Details Banner
            val activeMuscle = selectedMuscle ?: muscleVolumes.maxByOrNull { it.value }?.key ?: "Nenhum"
            val volume = muscleVolumes[activeMuscle] ?: 0f

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1A1F2C),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF2A3142))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(getIntensityColor(if (maxVolume > 0) volume / maxVolume else 0f), RoundedCornerShape(6.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Grupo Muscular Destaque",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = activeMuscle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Volume Total",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${volume.toInt()} kg",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Table Header: "Músculo" on left, "Volume / Séries" on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Músculo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray
                )
                Text(
                    text = "Volume (kg)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.LightGray
                )
            }

            val sortedMuscles = remember(muscleVolumes) {
                muscleVolumes.toList().filter { it.second > 0f }.sortedByDescending { it.second }
            }

            sortedMuscles.forEach { (muscle, vol) ->
                val fraction = if (maxVolume > 0f) (vol / maxVolume).coerceIn(0.06f, 1f) else 0.06f
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = muscle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.width(115.dp)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(getIntensityColor(fraction))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "${vol.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(42.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawAnteriorSilhouette(volumes: Map<String, Float>, maxVol: Float) {
    val cx = size.width / 2f
    
    // Head outline
    drawCircle(Color(0xFF222634), radius = 20f, center = Offset(cx, 28f))

    // Shoulders (Deltoids)
    val shoulderRatio = (volumes["Ombros"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawOval(getIntensityColor(shoulderRatio), topLeft = Offset(cx - 40f, 52f), size = Size(80f, 28f))

    // Chest (Peitoral)
    val chestRatio = (volumes["Peitoral"] ?: volumes["Peito"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(chestRatio), topLeft = Offset(cx - 32f, 82f), size = Size(64f, 44f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))

    // Biceps / Arms
    val bicepRatio = (volumes["Bíceps"] ?: volumes["Braços"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(bicepRatio), topLeft = Offset(cx - 52f, 85f), size = Size(16f, 52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
    drawRoundRect(getIntensityColor(bicepRatio), topLeft = Offset(cx + 36f, 85f), size = Size(16f, 52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))

    // Abs / Core
    val absRatio = (volumes["Abdominais"] ?: volumes["Core"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(absRatio), topLeft = Offset(cx - 22f, 128f), size = Size(44f, 55f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))

    // Quads / Legs
    val legRatio = (volumes["Pernas"] ?: volumes["Quadríceps"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(legRatio), topLeft = Offset(cx - 30f, 186f), size = Size(26f, 85f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    drawRoundRect(getIntensityColor(legRatio), topLeft = Offset(cx + 4f, 186f), size = Size(26f, 85f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
}

private fun DrawScope.drawPosteriorSilhouette(volumes: Map<String, Float>, maxVol: Float) {
    val cx = size.width / 2f
    
    // Head outline (back)
    drawCircle(Color(0xFF222634), radius = 20f, center = Offset(cx, 28f))

    // Upper Back / Lats
    val backRatio = (volumes["Costas"] ?: volumes["Dorsais"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(backRatio), topLeft = Offset(cx - 35f, 52f), size = Size(70f, 48f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))

    // Triceps / Arms back
    val tricepRatio = (volumes["Tríceps"] ?: volumes["Braços"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(tricepRatio), topLeft = Offset(cx - 52f, 85f), size = Size(16f, 52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))
    drawRoundRect(getIntensityColor(tricepRatio), topLeft = Offset(cx + 36f, 85f), size = Size(16f, 52f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f))

    // Glutes / Lower Back
    val gluteRatio = (volumes["Glúteos"] ?: volumes["Pernas"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(gluteRatio), topLeft = Offset(cx - 30f, 102f), size = Size(60f, 42f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))

    // Hamstrings / Calves
    val legRatio = (volumes["Pernas"] ?: volumes["Posterior"] ?: 0f) / maxVol.coerceAtLeast(1f)
    drawRoundRect(getIntensityColor(legRatio), topLeft = Offset(cx - 30f, 146f), size = Size(26f, 125f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    drawRoundRect(getIntensityColor(legRatio), topLeft = Offset(cx + 4f, 146f), size = Size(26f, 125f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
}
