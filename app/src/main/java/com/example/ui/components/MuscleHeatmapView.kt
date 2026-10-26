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
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Neutral at rest; warm tones show relative training volume.
fun getIntensityColor(ratio: Float): Color {
    return when {
        !ratio.isFinite() || ratio <= 0f -> Color(0xFF526174) // Untrained / Rest
        ratio < 0.25f -> Color(0xFFFFD6AD) // Low
        ratio < 0.5f -> Color(0xFFFFAE73)  // Moderate
        ratio < 0.75f -> Color(0xFFFF854A) // High
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
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFD6AD), RoundedCornerShape(4.dp)))
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
                            .weight(1f)
                            .fillMaxWidth()
                            .semantics { contentDescription = "Mapa muscular: vista anterior. Valores na tabela abaixo." }
                    ) {
                        drawMuscleBody(AnteriorMuscles, muscleVolumes, maxVolume)
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
                            .weight(1f)
                            .fillMaxWidth()
                            .semantics { contentDescription = "Mapa muscular: vista posterior. Valores na tabela abaixo." }
                    ) {
                        drawMuscleBody(PosteriorMuscles, muscleVolumes, maxVolume)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Highlighted Muscle Details Banner
            val activeMuscle = muscleVolumes.filterValues { it > 0f }.maxByOrNull { it.value }?.key ?: "Nenhum"
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

            if (sortedMuscles.isEmpty()) {
                Text("Sem treinos neste período", color = Color(0xFF94A3B8), fontSize = 13.sp)
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

// Original vector artwork in a 160 x 360 viewport. Each left-side region is mirrored.
private class MuscleRegion(val names: List<String>, data: String) {
    val path by lazy { PathParser().parsePathString(data).toPath() }
}

private val BodyDetails = listOf(
    MuscleRegion(emptyList(), "M80 5 C65 5 63 15 65 27 C66 36 73 43 80 44 C87 43 94 36 95 27 C97 15 95 5 80 5 Z"),
    MuscleRegion(emptyList(), "M72 42 L72 50 L62 56 L80 63 L98 56 L88 50 L88 42 Q80 49 72 42 Z")
)
private val Extremities = listOf(
    MuscleRegion(emptyList(), "M23 163 L31 166 L29 178 L25 187 Q22 190 20 186 L21 180 Q16 187 14 183 L15 174 Z"),
    MuscleRegion(emptyList(), "M59 330 L70 331 L71 343 Q75 352 68 354 L51 354 Q46 352 50 347 L57 340 Z")
)
private val Shoulders = MuscleRegion(listOf("Ombros"), "M59 57 Q45 57 41 69 L37 85 Q47 89 54 78 L62 65 Z")
private val Forearms = MuscleRegion(listOf("Antebraços", "Braços"), "M32 119 Q38 122 40 128 L31 163 L23 160 Q25 140 32 119 Z")
private val Calves = MuscleRegion(listOf("Panturrilhas", "Pernas"), "M56 261 Q64 257 71 263 Q74 281 68 302 L68 328 L59 328 L57 306 Q50 284 56 261 Z")
private val AnteriorMuscles = listOf(
    Shoulders,
    MuscleRegion(listOf("Peitoral", "Peito"), "M63 63 Q70 61 78 65 L78 91 Q65 101 52 91 L55 77 Z"),
    MuscleRegion(listOf("Bíceps", "Braços"), "M38 88 Q45 91 51 86 L46 110 Q42 123 34 118 Q31 107 38 88 Z"),
    Forearms,
    MuscleRegion(listOf("Abdominais", "Core"), "M66 101 L78 100 L78 115 L65 115 Z"),
    MuscleRegion(listOf("Abdominais", "Core"), "M65 119 L78 119 L78 133 L65 133 Z"),
    MuscleRegion(listOf("Abdominais", "Core"), "M65 137 L78 137 L78 158 Q68 154 65 147 Z"),
    MuscleRegion(listOf("Abdominais", "Core"), "M52 98 L61 103 L61 147 L76 166 L64 174 Q52 159 54 140 Z"),
    MuscleRegion(listOf("Quadríceps", "Pernas"), "M56 169 L65 179 L76 171 Q79 194 74 218 L68 250 Q61 255 56 247 Q47 213 51 187 Z"),
    MuscleRegion(emptyList(), "M57 252 Q63 257 69 252 L70 260 Q63 266 56 259 Z"),
    Calves
)
private val PosteriorMuscles = listOf(
    Shoulders,
    MuscleRegion(listOf("Trapézio", "Costas", "Dorsais"), "M73 53 L78 59 L78 98 Q64 86 59 69 Z"),
    MuscleRegion(listOf("Costas", "Dorsais"), "M55 80 Q63 93 78 104 L78 145 L65 155 Q57 140 54 120 L50 98 Z"),
    MuscleRegion(listOf("Tríceps", "Braços"), "M38 88 Q45 91 51 86 L47 109 Q42 122 34 118 Q31 107 38 88 Z"),
    Forearms,
    MuscleRegion(listOf("Lombar", "Costas", "Dorsais"), "M78 149 L78 172 L62 161 L66 155 Z"),
    MuscleRegion(listOf("Glúteos", "Pernas"), "M59 166 Q67 168 78 177 L78 198 Q65 210 52 198 Q50 179 59 166 Z"),
    MuscleRegion(listOf("Isquiotibiais", "Posterior", "Posterior de Coxa", "Pernas"), "M52 204 Q65 214 77 204 L72 235 L68 254 L57 254 Q50 231 52 204 Z"),
    Calves
)

private fun DrawScope.drawMuscleBody(regions: List<MuscleRegion>, volumes: Map<String, Float>, maxVolume: Float) {
    val factor = minOf(size.width / 160f, size.height / 360f)
    withTransform({
        translate((size.width - 160f * factor) / 2f, (size.height - 360f * factor) / 2f)
        scale(factor, factor, pivot = Offset.Zero)
    }) {
        fun drawRegion(region: MuscleRegion) {
            val volume = region.names.firstNotNullOfOrNull { volumes[it] } ?: 0f
            val color = if (region.names.isEmpty()) Color(0xFF394657)
                else getIntensityColor(volume / maxVolume.coerceAtLeast(1f))
            drawPath(region.path, color)
            drawPath(region.path, Color(0xFF8995A5), style = Stroke(width = 0.65f))
        }
        BodyDetails.forEach { drawRegion(it) }
        for (side in listOf(1f, -1f)) {
            withTransform({ scale(side, 1f, pivot = Offset(80f, 0f)) }) {
                Extremities.forEach { drawRegion(it) }
                regions.forEach { drawRegion(it) }
            }
        }
    }
}
