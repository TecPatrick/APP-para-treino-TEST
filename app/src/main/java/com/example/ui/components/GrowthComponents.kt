package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProgressLog
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

@Composable
fun GrowthHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String = "FITNESS PRO",
    trailingContent: (@Composable () -> Unit)? = null
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val compact = maxWidth < 380.dp
        val text: @Composable (Modifier) -> Unit = { modifier ->
            Column(modifier = modifier) {
                Surface(
                    color = GrowthOrange.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = badgeText,
                        color = GrowthOrangeLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (compact && trailingContent != null) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    trailingContent()
                }
                text(Modifier.fillMaxWidth())
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                text(Modifier.weight(1f))
                trailingContent?.invoke()
            }
        }
    }
}

/**
 * Places two actions side by side when they fit and stacks them on narrow
 * phones or with enlarged display/font settings.
 */
@Composable
fun AdaptiveActions(
    modifier: Modifier = Modifier,
    breakpoint: Int = 380,
    first: @Composable (Modifier) -> Unit,
    second: @Composable (Modifier) -> Unit
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < breakpoint.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                first(Modifier.fillMaxWidth())
                second(Modifier.fillMaxWidth())
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                first(Modifier.weight(1f))
                second(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MacroProgressBar(
    label: String,
    currentValue: Float,
    targetValue: Float,
    unit: String = "g",
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (targetValue > 0) (currentValue / targetValue).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "macro_progress")

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${currentValue.toInt()} / ${targetValue.toInt()} $unit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun CircularCalorieGauge(
    currentCalories: Float,
    targetCalories: Int,
    burnedCalories: Int = 450,
    modifier: Modifier = Modifier
) {
    val progress = if (targetCalories > 0) (currentCalories / targetCalories).coerceIn(0f, 1.25f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "cal_progress")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(140.dp)
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Background Track Arc
            drawArc(
                color = Color(0xFF222730),
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress Arc
            val sweep = (animatedProgress.coerceAtMost(1f) * 270f)
            if (sweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(GrowthYellow, GrowthOrange, Color(0xFFFF3D00))
                    ),
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = GrowthOrange,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "${currentCalories.toInt()}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "/ ${targetCalories} kcal",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WeightProgressionChart(
    logs: List<ProgressLog>,
    targetWeightKg: Float,
    modifier: Modifier = Modifier
) {
    if (logs.isEmpty()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            modifier = modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Adicione registros de peso para ver seu gráfico de evolução",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        return
    }

    val sortedLogs = logs.sortedBy { it.dateMillis }.takeLast(10)
    val weights = sortedLogs.map { it.weightKg }
    val minWeight = (weights.minOrNull() ?: 60f) - 1.5f
    val maxWeight = (weights.maxOrNull() ?: 80f) + 1.5f
    val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "EVOLUÇÃO DE PESO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${weights.last()} kg",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Surface(
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                ) {
                    Text(
                        text = "Meta: ${targetWeightKg} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GrowthCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointSpacing = if (sortedLogs.size > 1) width / (sortedLogs.size - 1) else width / 2

                val points = sortedLogs.mapIndexed { index, log ->
                    val x = if (sortedLogs.size > 1) index * pointSpacing else width / 2
                    val normalizedY = (log.weightKg - minWeight) / weightRange
                    val y = height - (normalizedY * height)
                    Offset(x, y.coerceIn(10f, height - 10f))
                }

                // Draw Target horizontal dotted line
                val targetNorm = (targetWeightKg - minWeight) / weightRange
                val targetY = height - (targetNorm * height)
                if (targetY in 0f..height) {
                    drawLine(
                        color = GrowthCyan.copy(alpha = 0.4f),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Draw gradient fill under curve
                if (points.size > 1) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(points.last().x, height)
                        close()
                    }
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(GrowthOrange.copy(alpha = 0.35f), Color.Transparent)
                        )
                    )

                    // Draw line
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = linePath,
                        color = GrowthOrange,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw glowing points
                points.forEach { pt ->
                    drawCircle(color = GrowthOrange, radius = 5.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = pt)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = sortedLogs.firstOrNull()?.dateDisplay ?: "",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = sortedLogs.lastOrNull()?.dateDisplay ?: "",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
