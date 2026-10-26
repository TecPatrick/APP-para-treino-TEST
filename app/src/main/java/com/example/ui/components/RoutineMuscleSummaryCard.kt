package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExercise

enum class TargetMuscle(
    val labelEn: String,
    val labelPt: String
) {
    SHOULDERS("Shoulders", "Ombros"),
    UPPER_BACK("Upper Back", "Costas Superior"),
    BICEPS("Biceps", "Bíceps"),
    TRICEPS("Triceps", "Tríceps"),
    LATS("Lats", "Dorsais"),
    HAMSTRINGS("Hamstrings", "Isquiotibiais"),
    ABDOMINALS("Abdominals", "Abdômen"),
    FOREARMS("Forearms", "Antebraços"),
    QUADRICEPS("Quadriceps", "Quadríceps"),
    GLUTES("Glutes", "Glúteos"),
    CHEST("Chest", "Peitoral"),
    TRAPS("Traps", "Trapézio"),
    LOWER_BACK("Lower Back", "Lombar"),
    CALVES("Calves", "Panturrilhas");

    val displayName: String
        get() = labelEn
}

fun normalizeExerciseMuscle(raw: String): TargetMuscle {
    val clean = raw.trim().lowercase()
    return when {
        clean.contains("ombro") || clean.contains("deltoid") || clean.contains("shoulder") -> TargetMuscle.SHOULDERS
        clean.contains("peit") || clean.contains("chest") -> TargetMuscle.CHEST
        clean.contains("bicep") || clean.contains("bícep") -> TargetMuscle.BICEPS
        clean.contains("tricep") || clean.contains("trícep") -> TargetMuscle.TRICEPS
        clean.contains("dorsal") || clean.contains("dorsais") || clean.contains("lat") -> TargetMuscle.LATS
        clean.contains("costa") || clean.contains("back") -> TargetMuscle.UPPER_BACK
        clean.contains("isquio") || clean.contains("posterior") || clean.contains("hamstring") -> TargetMuscle.HAMSTRINGS
        clean.contains("quad") || clean.contains("perna") -> TargetMuscle.QUADRICEPS
        clean.contains("glúteo") || clean.contains("glute") -> TargetMuscle.GLUTES
        clean.contains("abdom") || clean.contains("core") || clean.contains("abs") -> TargetMuscle.ABDOMINALS
        clean.contains("antebraço") || clean.contains("forearm") -> TargetMuscle.FOREARMS
        clean.contains("trapéz") || clean.contains("trap") -> TargetMuscle.TRAPS
        clean.contains("lombar") || clean.contains("lower back") -> TargetMuscle.LOWER_BACK
        clean.contains("panturrilha") || clean.contains("calf") || clean.contains("calves") -> TargetMuscle.CALVES
        else -> TargetMuscle.UPPER_BACK
    }
}

@Composable
fun RoutineMuscleSummaryCard(
    exercises: List<WorkoutExercise>,
    modifier: Modifier = Modifier
) {
    val totalExercises = exercises.size
    val totalSets = exercises.sumOf { it.sets }
    val totalSeconds = exercises.sumOf { it.sets * (45 + it.restSeconds) }
    val estimatedMinutes = remember(totalSeconds, totalExercises) {
        if (totalExercises == 0) 0 else (totalSeconds / 60).coerceAtLeast(totalExercises * 3)
    }

    val muscleSetCounts = remember(exercises) {
        val counts = mutableMapOf<TargetMuscle, Float>()
        for (ex in exercises) {
            val muscle = normalizeExerciseMuscle(ex.muscleGroup)
            counts[muscle] = (counts[muscle] ?: 0f) + ex.sets.toFloat()
        }
        counts.toList().sortedByDescending { it.second }
    }

    val maxSets = remember(muscleSetCounts) {
        muscleSetCounts.maxOfOrNull { it.second } ?: 1f
    }

    val muscleRatios = remember(muscleSetCounts, maxSets) {
        val map = mutableMapOf<TargetMuscle, Float>()
        for ((m, sets) in muscleSetCounts) {
            map[m] = if (maxSets > 0f) sets / maxSets else 0f
        }
        map
    }

    val activePrimaryColor = MaterialTheme.colorScheme.primary
    val neutralBodyColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    val cardBackground = MaterialTheme.colorScheme.surface
    val outlineBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.dp, outlineBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title
            Text(
                text = "Routine Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Stats Columns: Exercises, Total Sets, Estimated Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Exercises",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalExercises",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Total Sets",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalSets",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(modifier = Modifier.weight(1.2f)) {
                    Text(
                        text = "Estimated Duration",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${estimatedMinutes}m",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Anatomical Silhouettes: Anterior and Posterior side-by-side
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Front Body (Anterior)
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(130.dp)
                ) {
                    drawAnteriorHumanSilhouette(
                        muscleRatios = muscleRatios,
                        activeColor = activePrimaryColor,
                        neutralColor = neutralBodyColor
                    )
                }

                // Back Body (Posterior)
                Canvas(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(130.dp)
                ) {
                    drawPosteriorHumanSilhouette(
                        muscleRatios = muscleRatios,
                        activeColor = activePrimaryColor,
                        neutralColor = neutralBodyColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Table Header: "Muscle" on left, "Sets" on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Muscle",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sets",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Ranked Muscle Horizontal Bars
            if (muscleSetCounts.isEmpty()) {
                Text(
                    text = "Nenhum exercício cadastrado.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                muscleSetCounts.forEach { (muscle, sets) ->
                    val fraction = if (maxSets > 0f) (sets / maxSets).coerceIn(0.06f, 1f) else 0.06f
                    val displaySets = if (sets % 1f == 0f) "${sets.toInt()}" else "%.1f".format(sets)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Muscle Name Label
                        Text(
                            text = muscle.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(115.dp)
                        )

                        // Pill-shaped Horizontal Bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            // Rounded filled bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(activePrimaryColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Number of Sets
                        Text(
                            text = displaySets,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draws front body silhouette with muscular proportions and active muscle color highlights.
 */
private fun DrawScope.drawAnteriorHumanSilhouette(
    muscleRatios: Map<TargetMuscle, Float>,
    activeColor: Color,
    neutralColor: Color
) {
    val cx = size.width / 2f
    val scale = size.height / 240f

    fun colorFor(muscle: TargetMuscle): Color {
        val r = muscleRatios[muscle] ?: 0f
        return if (r > 0f) {
            activeColor.copy(alpha = 0.55f + 0.45f * r)
        } else {
            neutralColor
        }
    }

    // Head
    drawCircle(
        color = neutralColor,
        radius = 11f * scale,
        center = Offset(cx, 16f * scale)
    )

    // Neck & Traps
    drawRoundRect(
        color = colorFor(TargetMuscle.TRAPS),
        topLeft = Offset(cx - 10f * scale, 24f * scale),
        size = Size(20f * scale, 14f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )

    // Shoulders (Deltoids - Left & Right)
    val shoulderColor = colorFor(TargetMuscle.SHOULDERS)
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx - 38f * scale, 34f * scale),
        size = Size(15f * scale, 24f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx + 23f * scale, 34f * scale),
        size = Size(15f * scale, 24f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Chest (Pectorals - Left & Right plates)
    val chestColor = colorFor(TargetMuscle.CHEST)
    drawRoundRect(
        color = chestColor,
        topLeft = Offset(cx - 21f * scale, 36f * scale),
        size = Size(19f * scale, 22f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    drawRoundRect(
        color = chestColor,
        topLeft = Offset(cx + 2f * scale, 36f * scale),
        size = Size(19f * scale, 22f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Biceps / Upper Arms
    val bicepColor = colorFor(TargetMuscle.BICEPS)
    drawRoundRect(
        color = bicepColor,
        topLeft = Offset(cx - 41f * scale, 56f * scale),
        size = Size(13f * scale, 28f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    drawRoundRect(
        color = bicepColor,
        topLeft = Offset(cx + 28f * scale, 56f * scale),
        size = Size(13f * scale, 28f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Forearms
    val forearmColor = colorFor(TargetMuscle.FOREARMS)
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(cx - 44f * scale, 84f * scale),
        size = Size(11f * scale, 32f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(cx + 33f * scale, 84f * scale),
        size = Size(11f * scale, 32f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )

    // Hands
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx - 39f * scale, 120f * scale))
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx + 39f * scale, 120f * scale))

    // Abdominals (Rectus abdominis + core)
    val absColor = colorFor(TargetMuscle.ABDOMINALS)
    drawRoundRect(
        color = absColor,
        topLeft = Offset(cx - 15f * scale, 60f * scale),
        size = Size(30f * scale, 38f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Pelvis / Hips
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(cx - 17f * scale, 98f * scale),
        size = Size(34f * scale, 16f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Quadriceps (Left & Right)
    val quadColor = colorFor(TargetMuscle.QUADRICEPS)
    drawRoundRect(
        color = quadColor,
        topLeft = Offset(cx - 20f * scale, 115f * scale),
        size = Size(18f * scale, 54f * scale),
        cornerRadius = CornerRadius(7f * scale, 7f * scale)
    )
    drawRoundRect(
        color = quadColor,
        topLeft = Offset(cx + 2f * scale, 115f * scale),
        size = Size(18f * scale, 54f * scale),
        cornerRadius = CornerRadius(7f * scale, 7f * scale)
    )

    // Knees
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx - 11f * scale, 172f * scale))
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx + 11f * scale, 172f * scale))

    // Calves / Shins (Anterior)
    val calfColor = colorFor(TargetMuscle.CALVES)
    drawRoundRect(
        color = calfColor,
        topLeft = Offset(cx - 18f * scale, 177f * scale),
        size = Size(14f * scale, 45f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    drawRoundRect(
        color = calfColor,
        topLeft = Offset(cx + 4f * scale, 177f * scale),
        size = Size(14f * scale, 45f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Feet
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(cx - 19f * scale, 224f * scale),
        size = Size(14f * scale, 8f * scale),
        cornerRadius = CornerRadius(3f * scale, 3f * scale)
    )
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(cx + 5f * scale, 224f * scale),
        size = Size(14f * scale, 8f * scale),
        cornerRadius = CornerRadius(3f * scale, 3f * scale)
    )
}

/**
 * Draws posterior body silhouette with muscular proportions and active muscle color highlights.
 */
private fun DrawScope.drawPosteriorHumanSilhouette(
    muscleRatios: Map<TargetMuscle, Float>,
    activeColor: Color,
    neutralColor: Color
) {
    val cx = size.width / 2f
    val scale = size.height / 240f

    fun colorFor(muscle: TargetMuscle): Color {
        val r = muscleRatios[muscle] ?: 0f
        return if (r > 0f) {
            activeColor.copy(alpha = 0.55f + 0.45f * r)
        } else {
            neutralColor
        }
    }

    // Back of Head
    drawCircle(
        color = neutralColor,
        radius = 11f * scale,
        center = Offset(cx, 16f * scale)
    )

    // Traps (Diamond shaped upper back & neck)
    val trapsColor = colorFor(TargetMuscle.TRAPS)
    drawRoundRect(
        color = trapsColor,
        topLeft = Offset(cx - 16f * scale, 22f * scale),
        size = Size(32f * scale, 24f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Rear Deltoids
    val shoulderColor = colorFor(TargetMuscle.SHOULDERS)
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx - 38f * scale, 34f * scale),
        size = Size(15f * scale, 24f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = shoulderColor,
        topLeft = Offset(cx + 23f * scale, 34f * scale),
        size = Size(15f * scale, 24f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Upper Back / Rhomboids
    val upperBackColor = colorFor(TargetMuscle.UPPER_BACK)
    drawRoundRect(
        color = upperBackColor,
        topLeft = Offset(cx - 24f * scale, 38f * scale),
        size = Size(48f * scale, 22f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Lats (V-taper)
    val latsColor = colorFor(TargetMuscle.LATS)
    drawRoundRect(
        color = latsColor,
        topLeft = Offset(cx - 22f * scale, 58f * scale),
        size = Size(44f * scale, 26f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Triceps (Posterior arms)
    val tricepColor = colorFor(TargetMuscle.TRICEPS)
    drawRoundRect(
        color = tricepColor,
        topLeft = Offset(cx - 41f * scale, 56f * scale),
        size = Size(13f * scale, 28f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )
    drawRoundRect(
        color = tricepColor,
        topLeft = Offset(cx + 28f * scale, 56f * scale),
        size = Size(13f * scale, 28f * scale),
        cornerRadius = CornerRadius(5f * scale, 5f * scale)
    )

    // Forearms (Posterior)
    val forearmColor = colorFor(TargetMuscle.FOREARMS)
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(cx - 44f * scale, 84f * scale),
        size = Size(11f * scale, 32f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )
    drawRoundRect(
        color = forearmColor,
        topLeft = Offset(cx + 33f * scale, 84f * scale),
        size = Size(11f * scale, 32f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )

    // Hands
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx - 39f * scale, 120f * scale))
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx + 39f * scale, 120f * scale))

    // Lower Back (Erector Spinae)
    val lowerBackColor = colorFor(TargetMuscle.LOWER_BACK)
    drawRoundRect(
        color = lowerBackColor,
        topLeft = Offset(cx - 14f * scale, 82f * scale),
        size = Size(28f * scale, 18f * scale),
        cornerRadius = CornerRadius(4f * scale, 4f * scale)
    )

    // Glutes
    val glutesColor = colorFor(TargetMuscle.GLUTES)
    drawRoundRect(
        color = glutesColor,
        topLeft = Offset(cx - 20f * scale, 100f * scale),
        size = Size(19f * scale, 24f * scale),
        cornerRadius = CornerRadius(8f * scale, 8f * scale)
    )
    drawRoundRect(
        color = glutesColor,
        topLeft = Offset(cx + 1f * scale, 100f * scale),
        size = Size(19f * scale, 24f * scale),
        cornerRadius = CornerRadius(8f * scale, 8f * scale)
    )

    // Hamstrings (Posterior Thighs)
    val hamstringsColor = colorFor(TargetMuscle.HAMSTRINGS)
    drawRoundRect(
        color = hamstringsColor,
        topLeft = Offset(cx - 20f * scale, 125f * scale),
        size = Size(18f * scale, 44f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = hamstringsColor,
        topLeft = Offset(cx + 2f * scale, 125f * scale),
        size = Size(18f * scale, 44f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Back of Knees (Popliteal)
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx - 11f * scale, 172f * scale))
    drawCircle(color = neutralColor, radius = 5f * scale, center = Offset(cx + 11f * scale, 172f * scale))

    // Calves (Gastrocnemius / Soleus)
    val calfColor = colorFor(TargetMuscle.CALVES)
    drawRoundRect(
        color = calfColor,
        topLeft = Offset(cx - 19f * scale, 177f * scale),
        size = Size(16f * scale, 45f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )
    drawRoundRect(
        color = calfColor,
        topLeft = Offset(cx + 3f * scale, 177f * scale),
        size = Size(16f * scale, 45f * scale),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    // Feet (Heels)
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(cx - 17f * scale, 224f * scale),
        size = Size(12f * scale, 8f * scale),
        cornerRadius = CornerRadius(3f * scale, 3f * scale)
    )
    drawRoundRect(
        color = neutralColor,
        topLeft = Offset(cx + 5f * scale, 224f * scale),
        size = Size(12f * scale, 8f * scale),
        cornerRadius = CornerRadius(3f * scale, 3f * scale)
    )
}
