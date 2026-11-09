package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Escala Térmica Virtus (Fitness Dark Premium — Ciano e Laranja):
 * - 0% (Sem treino / Descanso): Cinza-carbono anatômico (#252D3D)
 * - 1% a 25% (Estímulo Leve): Ciano Suave (#00B8D4)
 * - 25% a 50% (Estímulo Moderado): Ciano Brilhante (#00E5FF)
 * - 50% a 75% (Estímulo Alto): Laranja Amber (#FF9100)
 * - 75% a 100% (Foco Máximo): Laranja Virtus Flamejante (#FF6D00)
 */
fun getIntensityColor(ratio: Float): Color {
    return when {
        !ratio.isFinite() || ratio <= 0f -> Color(0xFF252D3D) // Untrained / Rest
        ratio < 0.25f -> Color(0xFF00B8D4) // Low (Deep Cyan)
        ratio < 0.5f -> Color(0xFF00E5FF)  // Moderate (Bright Cyan)
        ratio < 0.75f -> Color(0xFFFF9100) // High (Warm Amber-Orange)
        else -> Color(0xFFFF6D00)          // Peak Volume (Virtus Flame Orange)
    }
}

private fun getIntensityLabel(ratio: Float): String {
    return when {
        !ratio.isFinite() || ratio <= 0f -> "EM DESCANSO"
        ratio < 0.25f -> "ESTÍMULO LEVE"
        ratio < 0.5f -> "ESTÍMULO MODERADO"
        ratio < 0.75f -> "ESTÍMULO ALTO"
        else -> "FOCO MÁXIMO 🔥"
    }
}

@Immutable
private data class RenderedMuscleRegionState(
    val region: AnatomicalMuscleRegion,
    val ratio: Float,
    val isSelected: Boolean,
    val fillColor: Color,
    val fiberColor: Color,
    val strokeColor: Color
)

private val SilhouetteStroke = Stroke(width = 1.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val BoneLineStroke = Stroke(width = 0.9f, cap = StrokeCap.Round)
private val FiberStroke = Stroke(width = 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val SelectedGlowStroke = Stroke(width = 3.0f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val ActiveGlowStroke = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val SelectedOutlineStroke = Stroke(width = 1.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val DefaultOutlineStroke = Stroke(width = 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val MirroredSides = floatArrayOf(1f, -1f)

@Composable
fun MuscleHeatmapView(
    muscleVolumes: Map<String, Float>,
    selectedTimeframeDays: Int,
    onTimeframeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVolume = remember(muscleVolumes) {
        (muscleVolumes.values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    }
    val totalPeriodVolume = remember(muscleVolumes) {
        muscleVolumes.values.sum().coerceAtLeast(1f)
    }

    val topMuscle = remember(muscleVolumes) {
        muscleVolumes.filterValues { it > 0f }.maxByOrNull { it.value }?.key ?: "Peitoral"
    }
    var selectedMuscle by remember(topMuscle) { mutableStateOf(topMuscle) }
    var viewMode by remember { mutableIntStateOf(0) } // 0 = Lado a Lado, 1 = Frontal, 2 = Posterior

    val selectedVolume = remember(selectedMuscle, muscleVolumes) {
        resolveMuscleVolume(listOf(selectedMuscle), muscleVolumes)
    }
    val selectedRatio = if (maxVolume > 0f && selectedVolume > 0f) {
        (selectedVolume / maxVolume).coerceIn(0f, 1f)
    } else {
        0f
    }
    val selectedSharePercent = if (selectedVolume > 0f) {
        ((selectedVolume / totalPeriodVolume) * 100f).toInt().coerceIn(1, 100)
    } else {
        0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("muscle_heatmap_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117)),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFFF6D00).copy(alpha = 0.45f),
                    Color(0xFF00E5FF).copy(alpha = 0.35f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mapa Anatômico de Volume",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Toque em um músculo no corpo ou na lista para inspecionar",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    color = Color(0xFF151B26),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color(0xFF263042))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF00E5FF), CircleShape)
                        )
                        Text("Leve", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFFFF6D00), CircleShape)
                        )
                        Text("Intenso", fontSize = 10.sp, color = Color(0xFFFF6D00), fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val timeframes = remember { listOf(7 to "7 Dias", 30 to "30 Dias", 0 to "Todo Período") }
                timeframes.forEach { (days, label) ->
                    val isSelected = selectedTimeframeDays == days
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTimeframeSelected(days) },
                        label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFF6D00),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF151B26),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = Color(0xFFFF6D00),
                            borderColor = Color(0xFF263042)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color(0xFF141B26),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF243044)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val modes = remember {
                        listOf(
                            Triple(0, "Lado a Lado", "heatmap_toggle_both"),
                            Triple(1, "Frontal", "heatmap_toggle_front"),
                            Triple(2, "Posterior", "heatmap_toggle_back")
                        )
                    }
                    modes.forEach { (modeIndex, modeLabel, tag) ->
                        val isModeSelected = viewMode == modeIndex
                        val activeAccent = if (modeIndex == 1) Color(0xFF00E5FF) else Color(0xFFFF6D00)
                        Surface(
                            onClick = { viewMode = modeIndex },
                            color = if (isModeSelected) activeAccent.copy(alpha = 0.22f) else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            border = if (isModeSelected) BorderStroke(1.dp, activeAccent.copy(alpha = 0.7f)) else null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag(tag)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = modeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = if (isModeSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isModeSelected) {
                                        if (modeIndex == 1) Color(0xFF00E5FF) else Color(0xFFFF9E40)
                                    } else {
                                        Color(0xFF94A3B8)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = Color(0xFF121822),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color(0xFF1F293A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (viewMode == 0) 320.dp else 350.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (viewMode == 0 || viewMode == 1) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF1A2232),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.28f))
                                ) {
                                    Text(
                                        text = "ANTERIOR (FRENTE)",
                                        fontSize = 10.sp,
                                        color = Color(0xFF00E5FF),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                AnatomicalBodyCanvas(
                                    silhouetteRegions = AnteriorSilhouette,
                                    muscleRegions = AnteriorMuscles,
                                    anatomicalLines = AnteriorJointAndBoneLines,
                                    muscleVolumes = muscleVolumes,
                                    maxVolume = maxVolume,
                                    selectedMuscle = selectedMuscle,
                                    onMuscleTapped = { tapped -> selectedMuscle = tapped },
                                    contentDesc = "Mapa muscular anatômico: vista anterior. Toque para selecionar um músculo.",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .testTag("heatmap_anterior_canvas")
                                )
                            }
                        }

                        if (viewMode == 0) {
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight(0.82f)
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0xFF2B374B),
                                                Color(0xFF00E5FF).copy(alpha = 0.35f),
                                                Color(0xFF2B374B),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )
                        }

                        if (viewMode == 0 || viewMode == 2) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF1A2232),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF6D00).copy(alpha = 0.32f))
                                ) {
                                    Text(
                                        text = "POSTERIOR (COSTAS)",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFF9E40),
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.8.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                AnatomicalBodyCanvas(
                                    silhouetteRegions = PosteriorSilhouette,
                                    muscleRegions = PosteriorMuscles,
                                    anatomicalLines = PosteriorJointAndBoneLines,
                                    muscleVolumes = muscleVolumes,
                                    maxVolume = maxVolume,
                                    selectedMuscle = selectedMuscle,
                                    onMuscleTapped = { tapped -> selectedMuscle = tapped },
                                    contentDesc = "Mapa muscular anatômico: vista posterior. Toque para selecionar um músculo.",
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                        .testTag("heatmap_posterior_canvas")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF252D3D),
                                            Color(0xFF00B8D4),
                                            Color(0xFF00E5FF),
                                            Color(0xFFFF9100),
                                            Color(0xFFFF6D00)
                                        )
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Descanso", fontSize = 9.sp, color = Color(0xFF64748B))
                            Text("Ativação Ciano", fontSize = 9.sp, color = Color(0xFF00E5FF))
                            Text("Hipertrofia Laranja", fontSize = 9.sp, color = Color(0xFFFF9100))
                            Text("Pico Máximo", fontSize = 9.sp, color = Color(0xFFFF6D00), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val highlightColor = getIntensityColor(selectedRatio)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_muscle_inspection_card"),
                color = Color(0xFF151B26),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (selectedRatio > 0f) highlightColor.copy(alpha = 0.65f) else Color(0xFF263042)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    color = highlightColor.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                tint = if (selectedRatio > 0f) highlightColor else Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Músculo Inspecionado",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Surface(
                                    color = highlightColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = getIntensityLabel(selectedRatio),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (selectedRatio > 0f) highlightColor else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = selectedMuscle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (selectedSharePercent > 0) "$selectedSharePercent% do volume total" else "Volume Acumulado",
                            fontSize = 10.sp,
                            color = Color(0xFF00E5FF)
                        )
                        Text(
                            text = "${selectedVolume.toInt()} kg",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = if (selectedRatio >= 0.5f) Color(0xFFFF6D00) else Color(0xFF00E5FF)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Grupamento Muscular",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "Volume (kg)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
            }

            val sortedMuscles = remember(muscleVolumes) {
                muscleVolumes.toList().filter { it.second > 0f }.sortedByDescending { it.second }
            }

            if (sortedMuscles.isEmpty()) {
                Text(
                    text = "Sem treinos registrados neste período. Toque nos músculos acima para explorar a anatomia.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            sortedMuscles.forEach { (muscle, vol) ->
                val fraction = if (maxVolume > 0f) (vol / maxVolume).coerceIn(0.08f, 1f) else 0.08f
                val isRowSelected = muscle.equals(selectedMuscle, ignoreCase = true)
                val barColor = getIntensityColor(fraction)

                Surface(
                    color = if (isRowSelected) Color(0xFF1A2333) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedMuscle = muscle }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = muscle,
                            fontSize = 13.sp,
                            fontWeight = if (isRowSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isRowSelected) Color(0xFF00E5FF) else Color.White,
                            modifier = Modifier.width(115.dp)
                        )

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1A202C)),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                barColor.copy(alpha = 0.75f),
                                                barColor
                                            )
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "${vol.toInt()}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isRowSelected) Color.White else Color(0xFFCBD5E1),
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(46.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnatomicalBodyCanvas(
    silhouetteRegions: List<Path>,
    muscleRegions: List<AnatomicalMuscleRegion>,
    anatomicalLines: List<Path>,
    muscleVolumes: Map<String, Float>,
    maxVolume: Float,
    selectedMuscle: String,
    onMuscleTapped: (String) -> Unit,
    contentDesc: String,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val cachedStates = remember(muscleRegions, muscleVolumes, maxVolume, selectedMuscle) {
        muscleRegions.map { region ->
            val volume = resolveMuscleVolume(region.aliases, muscleVolumes)
            val ratio = if (maxVolume > 0f && volume > 0f) {
                (volume / maxVolume).coerceIn(0f, 1f)
            } else {
                0f
            }
            val isSelected = region.primaryName.equals(selectedMuscle, ignoreCase = true) ||
                region.aliases.any { it.equals(selectedMuscle, ignoreCase = true) }
            val fillColor = getIntensityColor(ratio)
            val fiberColor = when {
                ratio >= 0.5f -> Color(0xFF431407).copy(alpha = 0.42f)
                ratio > 0f -> Color(0xFF083344).copy(alpha = 0.45f)
                else -> Color(0xFF0F172A).copy(alpha = 0.55f)
            }
            val strokeColor = when {
                isSelected -> Color.White
                ratio >= 0.5f -> Color(0xFFFFB74D)
                ratio > 0f -> Color(0xFF80D8FF)
                else -> Color(0xFF475569)
            }
            RenderedMuscleRegionState(
                region = region,
                ratio = ratio,
                isSelected = isSelected,
                fillColor = fillColor,
                fiberColor = fiberColor,
                strokeColor = strokeColor
            )
        }
    }

    Canvas(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .pointerInput(muscleRegions) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        val factor = minOf(
                            canvasSize.width / HEATMAP_VIEWPORT_WIDTH,
                            canvasSize.height / HEATMAP_VIEWPORT_HEIGHT
                        )
                        val offsetX = (canvasSize.width - HEATMAP_VIEWPORT_WIDTH * factor) / 2f
                        val offsetY = (canvasSize.height - HEATMAP_VIEWPORT_HEIGHT * factor) / 2f
                        val bodyX = (tapOffset.x - offsetX) / factor
                        val bodyY = (tapOffset.y - offsetY) / factor

                        if (bodyX in 0f..HEATMAP_VIEWPORT_WIDTH && bodyY in 0f..HEATMAP_VIEWPORT_HEIGHT) {
                            val hit = muscleRegions.firstOrNull { region ->
                                region.containsPoint(bodyX, bodyY)
                            }
                            if (hit != null) {
                                onMuscleTapped(hit.primaryName)
                            }
                        }
                    }
                }
            }
    ) {
        canvasSize = IntSize(size.width.toInt(), size.height.toInt())
        drawAnatomicalHumanBody(
            silhouetteRegions = silhouetteRegions,
            renderedStates = cachedStates,
            anatomicalLines = anatomicalLines
        )
    }
}

private fun DrawScope.drawAnatomicalHumanBody(
    silhouetteRegions: List<Path>,
    renderedStates: List<RenderedMuscleRegionState>,
    anatomicalLines: List<Path>
) {
    val factor = minOf(size.width / HEATMAP_VIEWPORT_WIDTH, size.height / HEATMAP_VIEWPORT_HEIGHT)
    val translateX = (size.width - HEATMAP_VIEWPORT_WIDTH * factor) / 2f
    val translateY = (size.height - HEATMAP_VIEWPORT_HEIGHT * factor) / 2f

    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF00E5FF).copy(alpha = 0.07f),
                Color(0xFFFF6D00).copy(alpha = 0.04f),
                Color.Transparent
            ),
            center = Offset(size.width / 2f, size.height * 0.45f),
            radius = size.minDimension * 0.55f
        ),
        radius = size.minDimension * 0.55f,
        center = Offset(size.width / 2f, size.height * 0.45f)
    )

    withTransform({
        translate(translateX, translateY)
        scale(factor, factor, pivot = Offset.Zero)
    }) {
        for (side in MirroredSides) {
            withTransform({ scale(side, 1f, pivot = Offset(HEATMAP_CENTER_X, 0f)) }) {
                silhouetteRegions.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFF1B2230)
                    )
                    drawPath(
                        path = path,
                        color = Color(0xFF38465E),
                        style = SilhouetteStroke
                    )
                }

                anatomicalLines.forEach { linePath ->
                    drawPath(
                        path = linePath,
                        color = Color(0xFF334155),
                        style = BoneLineStroke
                    )
                }
            }
        }

        for (side in MirroredSides) {
            withTransform({ scale(side, 1f, pivot = Offset(HEATMAP_CENTER_X, 0f)) }) {
                renderedStates.forEach { state ->
                    val region = state.region
                    val ratio = state.ratio
                    val isSelected = state.isSelected
                    val fillColor = state.fillColor

                    if (ratio > 0f || isSelected) {
                        drawPath(
                            path = region.outlinePath,
                            color = if (isSelected && ratio <= 0f) {
                                Color(0xFF00E5FF).copy(alpha = 0.25f)
                            } else {
                                fillColor.copy(alpha = 0.28f)
                            },
                            style = if (isSelected) SelectedGlowStroke else ActiveGlowStroke
                        )
                    }

                    drawPath(
                        path = region.outlinePath,
                        color = if (isSelected && ratio <= 0f) {
                            Color(0xFF2B3950)
                        } else {
                            fillColor.copy(alpha = if (ratio > 0f) 0.92f else 0.88f)
                        }
                    )

                    region.fiberPaths.forEach { fiberPath ->
                        drawPath(
                            path = fiberPath,
                            color = state.fiberColor,
                            style = FiberStroke
                        )
                    }

                    drawPath(
                        path = region.outlinePath,
                        color = state.strokeColor,
                        style = if (isSelected) SelectedOutlineStroke else DefaultOutlineStroke
                    )
                }
            }
        }
    }
}
