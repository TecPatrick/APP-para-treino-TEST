package com.example.ui.components

import android.graphics.RectF
import android.graphics.Region
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
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

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

@Composable
fun MuscleHeatmapView(
    muscleVolumes: Map<String, Float>,
    selectedTimeframeDays: Int,
    onTimeframeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxVolume = (muscleVolumes.values.maxOrNull() ?: 1f).coerceAtLeast(1f)
    val totalPeriodVolume = muscleVolumes.values.sum().coerceAtLeast(1f)

    // Default selected muscle is the highest volume trained muscle, or "Peitoral" if none trained
    val topMuscle = remember(muscleVolumes) {
        muscleVolumes.filterValues { it > 0f }.maxByOrNull { it.value }?.key ?: "Peitoral"
    }
    var selectedMuscle by remember(topMuscle) { mutableStateOf(topMuscle) }
    var viewMode by remember { mutableStateOf(0) } // 0 = Lado a Lado, 1 = Frontal, 2 = Posterior

    val selectedVolume = remember(selectedMuscle, muscleVolumes) {
        resolveMuscleVolume( listOf(selectedMuscle), muscleVolumes)
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
            // Header & Thermal Scale Pill
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

                // Thermal Scale Legend Pill (Ciano -> Laranja)
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

            // View Mode Switcher (Lado a Lado | Frontal | Posterior)
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
                    val modes = listOf(
                        Triple(0, "Lado a Lado", "heatmap_toggle_both"),
                        Triple(1, "Frontal", "heatmap_toggle_front"),
                        Triple(2, "Posterior", "heatmap_toggle_back")
                    )
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

            // Hevy-Style Anatomical Viewport (Lado a Lado, Frontal ampliado ou Posterior ampliado)
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
                            // ANTERIOR (FRENTE)
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
                            // Subtle vertical divider with gradient
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
                            // POSTERIOR (COSTAS)
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

                    // Continuous Thermal Scale Bar Footer inside viewport
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

            // Interactive Selected Muscle Inspection Card
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

            // Table Header: "Músculo" on left, "Volume (kg)" on right
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

private fun resolveMuscleVolume(names: List<String>, volumes: Map<String, Float>): Float {
    if (names.isEmpty()) return 0f
    // Direct match first
    names.firstNotNullOfOrNull { name ->
        volumes.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
    }?.let { return it }
    // Partial match fallback
    names.firstNotNullOfOrNull { name ->
        volumes.entries.firstOrNull {
            it.key.contains(name, ignoreCase = true) || name.contains(it.key, ignoreCase = true)
        }?.value
    }?.let { return it }
    return 0f
}

/**
 * Representa uma região anatômica espelhada no espaço 200 x 440 (centro X = 100f).
 * Inclui o caminho principal do músculo (`outlinePath`) e feixes de fibras internas (`fiberPaths`)
 * para conferir estética anatômica estilo Hevy App.
 */
private class AnatomicalMuscleRegion(
    val primaryName: String,
    val aliases: List<String>,
    outlineSvg: String,
    fiberSvgs: List<String> = emptyList()
) {
    val outlinePath: Path by lazy { PathParser().parsePathString(outlineSvg).toPath() }
    val fiberPaths: List<Path> by lazy { fiberSvgs.map { PathParser().parsePathString(it).toPath() } }
    private val hitRegion: Region by lazy {
        val androidPath = outlinePath.asAndroidPath()
        val bounds = RectF()
        androidPath.computeBounds(bounds, true)
        Region().apply {
            setPath(
                androidPath,
                Region(
                    bounds.left.toInt() - 2,
                    bounds.top.toInt() - 2,
                    bounds.right.toInt() + 2,
                    bounds.bottom.toInt() + 2
                )
            )
        }
    }

    fun containsPoint(x: Float, y: Float): Boolean {
        // Since left side is mirrored around X = 100f, map right-side touches (x > 100f) to left-side (200f - x)
        val canonicalX = if (x > 100f) 200f - x else x
        return hitRegion.contains(canonicalX.toInt(), y.toInt())
    }
}

private const val VIEWPORT_WIDTH = 200f
private const val VIEWPORT_HEIGHT = 440f
private const val CENTER_X = 100f

/**
 * Silhueta Corporal Base (Cabeça, Pescoço, Tronco, Braços, Mãos, Pernas e Pés)
 * Desenhada no lado esquerdo (X <= 100) e espelhada simetricamente para formar um corpo humano contínuo.
 */
private val CommonHeadAndNeckSilhouette = listOf(
    // Cabeça e crânio com mandíbula anatômica
    "M100 10 C86 10 79 20 80 33 C81 43 85 51 91 56 L90 66 L100 69 L100 10 Z",
    // Orelha sutil
    "M80 32 C77 32 77 39 81 41 Z"
)

private val BaseLimbAndTorsoSilhouette = listOf(
    // Tronco base, cintura, pelve, braço, antebraço, mão, coxa, joelho, perna e pé
    "M100 66 L89 65 L74 71 C58 73 48 81 44 96 L38 126 L29 164 L22 196 " +
        "C17 203 15 212 19 218 C22 222 27 219 30 212 L33 198 L45 166 L54 132 " +
        "L58 114 L61 142 L65 172 L62 198 C55 224 54 256 59 286 L62 308 " +
        "C57 328 57 352 63 376 L65 398 L56 412 C53 418 57 423 67 423 L78 421 " +
        "L80 398 L81 374 C86 350 86 328 83 308 L85 286 L94 234 L100 226 Z"
)

private val AnteriorSilhouette: List<Path> by lazy {
    (CommonHeadAndNeckSilhouette + BaseLimbAndTorsoSilhouette).map {
        PathParser().parsePathString(it).toPath()
    }
}

private val PosteriorSilhouette: List<Path> by lazy {
    (CommonHeadAndNeckSilhouette + BaseLimbAndTorsoSilhouette).map {
        PathParser().parsePathString(it).toPath()
    }
}

// Linhas anatômicas estruturais (clavícula, esterno, joelho/patela, tornozelo, punho, face/pescoço)
private val AnteriorJointAndBoneLines: List<Path> by lazy {
    listOf(
        // Clavícula e fúrcula esternal
        "M98 73 Q82 72 64 76",
        // Esterno central e linha alba
        "M100 76 L100 198",
        // Patela (Joelho)
        "M66 290 Q73 286 80 290 Q81 301 73 305 Q65 301 66 290",
        // Dedos/Mão e Pé
        "M23 202 L20 214",
        "M26 203 L24 215",
        "M61 416 Q70 414 77 417"
    ).map { PathParser().parsePathString(it).toPath() }
}

private val PosteriorJointAndBoneLines: List<Path> by lazy {
    listOf(
        // Coluna vertebral torácica e lombar
        "M100 66 L100 202",
        // Fossa poplítea (atrás do joelho)
        "M64 296 Q73 301 82 296",
        // Tendão de Aquiles e calcâneo
        "M70 374 L71 406",
        "M75 374 L74 406"
    ).map { PathParser().parsePathString(it).toPath() }
}

/**
 * GRUPAMENTOS MUSCULARES — VISTA ANTERIOR (FRENTE)
 * Coordenadas no espaço 200 x 440 (lado esquerdo X <= 100, espelhado automaticamente para o direito).
 */
private val AnteriorMuscles = listOf(
    // 1. Trapézio Superior (Vista Frontal)
    AnatomicalMuscleRegion(
        primaryName = "Trapézio",
        aliases = listOf("Trapézio", "Costas"),
        outlineSvg = "M89 58 L71 71 Q81 73 91 71 L91 61 Z",
        fiberSvgs = listOf("M88 63 L77 70")
    ),
    // 2. Deltoide Anterior & Lateral (Ombros)
    AnatomicalMuscleRegion(
        primaryName = "Ombros",
        aliases = listOf("Ombros", "Deltoides", "Deltoide"),
        outlineSvg = "M70 72 C56 73 46 82 43 97 C42 105 45 113 51 116 C56 111 61 101 64 91 L72 77 Z",
        fiberSvgs = listOf(
            "M64 76 C54 83 49 94 48 108",
            "M68 79 C59 87 55 97 53 111"
        )
    ),
    // 3. Peitoral Maior (Clavicular, Esternal e Costal)
    AnatomicalMuscleRegion(
        primaryName = "Peitoral",
        aliases = listOf("Peitoral", "Peito"),
        outlineSvg = "M72 75 Q85 73 98 76 L98 115 C88 121 72 120 62 111 C59 103 61 91 66 83 Z",
        fiberSvgs = listOf(
            "M65 88 Q81 86 97 87",
            "M62 98 Q80 99 97 97",
            "M64 107 Q81 111 97 106"
        )
    ),
    // 4. Bíceps Braquial & Braquial
    AnatomicalMuscleRegion(
        primaryName = "Bíceps",
        aliases = listOf("Bíceps", "Braços"),
        outlineSvg = "M44 102 C49 106 55 104 58 99 L55 129 C52 138 43 139 39 131 C37 121 39 110 44 102 Z",
        fiberSvgs = listOf(
            "M49 105 C47 115 46 125 46 134",
            "M44 109 C42 117 42 124 43 131"
        )
    ),
    // 5. Antebraços (Braquiorradial e Flexores)
    AnatomicalMuscleRegion(
        primaryName = "Antebraços",
        aliases = listOf("Antebraços", "Braços"),
        outlineSvg = "M38 134 C44 139 51 138 53 133 C51 151 45 172 34 195 L25 193 C28 171 31 151 38 134 Z",
        fiberSvgs = listOf(
            "M45 138 C42 155 37 173 31 191",
            "M39 142 C36 157 32 173 28 189"
        )
    ),
    // 6. Reto Abdominal & Oblíquos Externos (Core)
    AnatomicalMuscleRegion(
        primaryName = "Abdominais",
        aliases = listOf("Abdominais", "Core", "Abdômen"),
        outlineSvg = "M76 120 L98 119 L98 196 C88 194 81 182 78 168 L75 138 Z",
        fiberSvgs = listOf(
            // Divisões horizontais dos gomos abdominais (6-pack)
            "M77 137 Q88 139 98 136",
            "M77 154 Q88 156 98 153",
            "M79 171 Q89 173 98 170"
        )
    ),
    // Oblíquos Externos & Serrátil Anterior
    AnatomicalMuscleRegion(
        primaryName = "Abdominais",
        aliases = listOf("Abdominais", "Core", "Abdômen"),
        outlineSvg = "M62 114 C67 119 72 121 74 122 L76 168 L83 192 C73 187 66 175 64 159 L61 132 Z",
        fiberSvgs = listOf(
            "M63 125 L73 133",
            "M63 138 L74 147",
            "M65 152 L75 162"
        )
    ),
    // 7. Quadríceps (Vasto Lateral, Reto Femoral, Vasto Medial e Adutores)
    AnatomicalMuscleRegion(
        primaryName = "Quadríceps",
        aliases = listOf("Quadríceps", "Pernas", "Coxas"),
        outlineSvg = "M64 186 C74 194 85 201 97 204 L94 235 L85 283 C79 289 67 288 60 281 C54 250 55 215 64 186 Z",
        fiberSvgs = listOf(
            // Reto femoral central e gota do vasto medial
            "M76 196 C74 224 73 253 72 282",
            "M66 202 C61 228 61 255 65 278",
            "M89 208 C88 232 85 256 79 280",
            "M83 254 Q78 268 83 281"
        )
    ),
    // 8. Panturrilhas Anteriores (Tibial Anterior e Gastrocnêmio Medial)
    AnatomicalMuscleRegion(
        primaryName = "Panturrilhas",
        aliases = listOf("Panturrilhas", "Pernas"),
        outlineSvg = "M62 309 C69 306 77 306 82 310 C86 332 85 356 80 384 L66 384 C58 358 57 332 62 309 Z",
        fiberSvgs = listOf(
            "M72 311 C71 335 72 360 73 382",
            "M65 318 C62 338 63 358 67 378"
        )
    )
)

/**
 * GRUPAMENTOS MUSCULARES — VISTA POSTERIOR (COSTAS)
 * Coordenadas no espaço 200 x 440 (lado esquerdo X <= 100, espelhado automaticamente para o direito).
 */
private val PosteriorMuscles = listOf(
    // 1. Trapézio (Superior, Médio e Inferior em formato diamante anatômico)
    AnatomicalMuscleRegion(
        primaryName = "Trapézio",
        aliases = listOf("Trapézio", "Costas", "Dorsais"),
        outlineSvg = "M90 56 L70 71 C74 84 83 102 99 125 L99 56 Z",
        fiberSvgs = listOf(
            "M75 74 L98 78",
            "M80 88 L98 94",
            "M87 103 L98 110"
        )
    ),
    // 2. Deltoide Posterior (Ombros)
    AnatomicalMuscleRegion(
        primaryName = "Ombros",
        aliases = listOf("Ombros", "Deltoides", "Deltoide"),
        outlineSvg = "M69 72 C56 73 46 82 43 96 C43 104 47 110 55 112 C62 104 68 92 72 80 Z",
        fiberSvgs = listOf(
            "M66 78 C58 85 51 94 48 105",
            "M69 84 C62 91 56 99 53 108"
        )
    ),
    // 3. Grande Dorsal (Latíssimo do Dorso & Redondo Maior - V-Taper)
    AnatomicalMuscleRegion(
        primaryName = "Costas",
        aliases = listOf("Costas", "Dorsais", "Grande Dorsal"),
        outlineSvg = "M71 84 C65 96 60 106 58 115 L62 146 C67 160 77 169 87 174 L98 152 L98 126 C84 108 76 95 71 84 Z",
        fiberSvgs = listOf(
            "M61 108 C71 116 83 125 97 135",
            "M62 125 C72 133 84 141 97 148",
            "M65 142 C74 150 84 157 93 163"
        )
    ),
    // 4. Lombar (Eretores da Espinha / Fáscia Toracolombar)
    AnatomicalMuscleRegion(
        primaryName = "Lombar",
        aliases = listOf("Lombar", "Costas", "Dorsais"),
        outlineSvg = "M87 156 L98 153 L98 188 L76 178 C79 169 83 162 87 156 Z",
        fiberSvgs = listOf(
            "M91 159 L90 182",
            "M84 166 L83 180"
        )
    ),
    // 5. Tríceps Braquial (Cabeças Longa e Lateral em Ferradura)
    AnatomicalMuscleRegion(
        primaryName = "Tríceps",
        aliases = listOf("Tríceps", "Braços"),
        outlineSvg = "M44 101 C50 106 56 105 58 100 L55 130 C51 138 43 139 39 131 C37 121 39 110 44 101 Z",
        fiberSvgs = listOf(
            // Desenho em ferradura do tríceps
            "M43 124 Q48 112 53 125",
            "M48 105 L48 118"
        )
    ),
    // 6. Extensores do Antebraço
    AnatomicalMuscleRegion(
        primaryName = "Antebraços",
        aliases = listOf("Antebraços", "Braços"),
        outlineSvg = "M38 134 C44 139 51 138 53 133 C51 151 45 172 34 195 L25 193 C28 171 31 151 38 134 Z",
        fiberSvgs = listOf(
            "M45 138 C42 155 37 173 31 191",
            "M39 142 C36 157 32 173 28 189"
        )
    ),
    // 7. Glúteos (Glúteo Médio e Glúteo Máximo)
    AnatomicalMuscleRegion(
        primaryName = "Glúteos",
        aliases = listOf("Glúteos", "Pernas"),
        outlineSvg = "M65 174 C75 176 87 182 98 190 L98 228 C84 238 66 234 59 220 C56 204 58 187 65 174 Z",
        fiberSvgs = listOf(
            "M66 184 Q81 189 96 199",
            "M61 198 Q78 205 96 213",
            "M62 213 Q78 221 95 224"
        )
    ),
    // 8. Isquiotibiais (Bíceps Femoral e Semitendíneo / Posterior de Coxa)
    AnatomicalMuscleRegion(
        primaryName = "Isquiotibiais",
        aliases = listOf("Isquiotibiais", "Posterior", "Posterior de Coxa", "Pernas"),
        outlineSvg = "M59 224 C70 236 84 239 95 231 L85 288 C78 294 67 294 61 288 C55 266 55 244 59 224 Z",
        fiberSvgs = listOf(
            // Divisão central entre bíceps femoral (lateral) e semitendíneo (medial)
            "M76 235 C75 253 74 272 73 291",
            "M65 236 C63 254 63 271 65 286",
            "M86 237 C84 254 82 271 79 286"
        )
    ),
    // 9. Panturrilhas Posteriores (Gastrocnêmio Cabeça Lateral/Medial e Sóleo)
    AnatomicalMuscleRegion(
        primaryName = "Panturrilhas",
        aliases = listOf("Panturrilhas", "Pernas"),
        outlineSvg = "M62 304 C69 299 77 299 83 305 C87 325 86 347 80 374 L66 374 C57 348 56 325 62 304 Z",
        fiberSvgs = listOf(
            // Divisão clássica em diamante dos gêmeos da panturrilha
            "M73 303 L73 345",
            "M61 338 Q67 349 73 342 Q79 349 84 338"
        )
    )
)

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

    Canvas(
        modifier = modifier
            .semantics { contentDescription = contentDesc }
            .pointerInput(muscleRegions) {
                detectTapGestures { tapOffset ->
                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                        val factor = minOf(
                            canvasSize.width / VIEWPORT_WIDTH,
                            canvasSize.height / VIEWPORT_HEIGHT
                        )
                        val offsetX = (canvasSize.width - VIEWPORT_WIDTH * factor) / 2f
                        val offsetY = (canvasSize.height - VIEWPORT_HEIGHT * factor) / 2f
                        val bodyX = (tapOffset.x - offsetX) / factor
                        val bodyY = (tapOffset.y - offsetY) / factor

                        if (bodyX in 0f..VIEWPORT_WIDTH && bodyY in 0f..VIEWPORT_HEIGHT) {
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
            muscleRegions = muscleRegions,
            anatomicalLines = anatomicalLines,
            volumes = muscleVolumes,
            maxVolume = maxVolume,
            selectedMuscle = selectedMuscle
        )
    }
}

private fun DrawScope.drawAnatomicalHumanBody(
    silhouetteRegions: List<Path>,
    muscleRegions: List<AnatomicalMuscleRegion>,
    anatomicalLines: List<Path>,
    volumes: Map<String, Float>,
    maxVolume: Float,
    selectedMuscle: String
) {
    val factor = minOf(size.width / VIEWPORT_WIDTH, size.height / VIEWPORT_HEIGHT)
    val translateX = (size.width - VIEWPORT_WIDTH * factor) / 2f
    val translateY = (size.height - VIEWPORT_HEIGHT * factor) / 2f

    // Subtle ambient aura behind the body silhouette
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
        // 1. Draw both mirrored sides of the base human silhouette (head, neck, torso, hands, knees, feet)
        for (side in listOf(1f, -1f)) {
            withTransform({ scale(side, 1f, pivot = Offset(CENTER_X, 0f)) }) {
                silhouetteRegions.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFF1B2230)
                    )
                    drawPath(
                        path = path,
                        color = Color(0xFF38465E),
                        style = Stroke(
                            width = 1.1f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Structural joint & bone contour lines (clavicles, patella, achilles, etc.)
                anatomicalLines.forEach { linePath ->
                    drawPath(
                        path = linePath,
                        color = Color(0xFF334155),
                        style = Stroke(width = 0.9f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // 2. Draw sculpted anatomical muscle bellies + internal muscle fiber striations
        for (side in listOf(1f, -1f)) {
            withTransform({ scale(side, 1f, pivot = Offset(CENTER_X, 0f)) }) {
                muscleRegions.forEach { region ->
                    val volume = resolveMuscleVolume(region.aliases, volumes)
                    val ratio = if (maxVolume > 0f && volume > 0f) {
                        (volume / maxVolume).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                    val isSelected = region.primaryName.equals(selectedMuscle, ignoreCase = true) ||
                        region.aliases.any { it.equals(selectedMuscle, ignoreCase = true) }

                    val fillColor = getIntensityColor(ratio)

                    // Outer glow when muscle has training volume or is actively selected
                    if (ratio > 0f || isSelected) {
                        drawPath(
                            path = region.outlinePath,
                            color = if (isSelected && ratio <= 0f) {
                                Color(0xFF00E5FF).copy(alpha = 0.25f)
                            } else {
                                fillColor.copy(alpha = 0.28f)
                            },
                            style = Stroke(
                                width = if (isSelected) 3.0f else 1.8f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Main anatomical muscle belly fill
                    drawPath(
                        path = region.outlinePath,
                        color = if (isSelected && ratio <= 0f) {
                            Color(0xFF2B3950)
                        } else {
                            fillColor.copy(alpha = if (ratio > 0f) 0.92f else 0.88f)
                        }
                    )

                    // Internal muscle fiber striations (Hevy anatomical style)
                    val fiberColor = when {
                        ratio >= 0.5f -> Color(0xFF431407).copy(alpha = 0.42f)
                        ratio > 0f -> Color(0xFF083344).copy(alpha = 0.45f)
                        else -> Color(0xFF0F172A).copy(alpha = 0.55f)
                    }
                    region.fiberPaths.forEach { fiberPath ->
                        drawPath(
                            path = fiberPath,
                            color = fiberColor,
                            style = Stroke(
                                width = 0.85f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // Crisp anatomical fascial outline around each muscle group
                    val strokeColor = when {
                        isSelected -> Color.White
                        ratio >= 0.5f -> Color(0xFFFFB74D)
                        ratio > 0f -> Color(0xFF80D8FF)
                        else -> Color(0xFF475569)
                    }
                    drawPath(
                        path = region.outlinePath,
                        color = strokeColor,
                        style = Stroke(
                            width = if (isSelected) 1.5f else 0.85f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
