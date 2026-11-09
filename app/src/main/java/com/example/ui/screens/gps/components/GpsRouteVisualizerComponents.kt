package com.example.ui.screens.gps.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GpsRouteLog
import com.example.ui.screens.gps.NativeGpsMapView
import com.example.ui.screens.gps.openInExternalMap
import com.example.ui.screens.gps.simplifyPolylineRdp
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import java.util.Locale

private val RouteGlowStroke = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
private val RouteMainStroke = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)

@Composable
fun RoutePathCanvas(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    val simplifiedPoints = remember(points) {
        simplifyPolylineRdp(points)
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val gridLines = 8
        for (i in 0..gridLines) {
            val y = (h / gridLines) * i
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            val x = (w / gridLines) * i
            drawLine(
                color = Color.White.copy(alpha = 0.04f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
        }

        if (simplifiedPoints.isEmpty()) {
            drawCircle(
                color = GrowthOrange.copy(alpha = 0.2f),
                radius = 30f,
                center = Offset(w / 2f, h / 2f)
            )
            drawCircle(
                color = GrowthOrange,
                radius = 8f,
                center = Offset(w / 2f, h / 2f)
            )
            return@Canvas
        }

        val minLat = simplifiedPoints.minOf { it.first }
        val maxLat = simplifiedPoints.maxOf { it.first }
        val minLng = simplifiedPoints.minOf { it.second }
        val maxLng = simplifiedPoints.maxOf { it.second }

        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

        val padding = 36f
        val usableWidth = (w - (padding * 2)).coerceAtLeast(10f)
        val usableHeight = (h - (padding * 2)).coerceAtLeast(10f)

        fun projectPoint(lat: Double, lng: Double): Offset {
            val x = padding + (((lng - minLng) / lngSpan) * usableWidth).toFloat()
            val y = padding + ((1.0 - ((lat - minLat) / latSpan)) * usableHeight).toFloat()
            return Offset(x, y)
        }

        if (simplifiedPoints.size == 1) {
            val pt = projectPoint(simplifiedPoints[0].first, simplifiedPoints[0].second)
            drawCircle(color = GrowthGreen.copy(alpha = 0.35f), radius = 24f, center = pt)
            drawCircle(color = GrowthGreen, radius = 10f, center = pt)
            return@Canvas
        }

        val path = Path()
        val firstPt = projectPoint(simplifiedPoints[0].first, simplifiedPoints[0].second)
        path.moveTo(firstPt.x, firstPt.y)

        for (i in 1 until simplifiedPoints.size) {
            val pt = projectPoint(simplifiedPoints[i].first, simplifiedPoints[i].second)
            path.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = path,
            color = GrowthOrange.copy(alpha = 0.3f),
            style = RouteGlowStroke
        )
        drawPath(
            path = path,
            color = GrowthOrange,
            style = RouteMainStroke
        )

        drawCircle(color = GrowthGreen, radius = 7f, center = firstPt)
        drawCircle(color = Color.White, radius = 3f, center = firstPt)

        val lastPt = projectPoint(simplifiedPoints.last().first, simplifiedPoints.last().second)
        drawCircle(color = GrowthCyan.copy(alpha = 0.4f), radius = 16f, center = lastPt)
        drawCircle(color = GrowthCyan, radius = 8f, center = lastPt)
        drawCircle(color = Color.White, radius = 3.5f, center = lastPt)
    }
}

@Composable
fun GpsRouteLogCard(
    log: GpsRouteLog,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
            .testTag("route_log_${log.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CarbonCardElevated else CarbonCard
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isSelected) GrowthOrange else CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.routeName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = log.dateDisplay,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val distKm = log.distanceMeters / 1000f
                    Surface(
                        color = GrowthOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f km", distKm),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("delete_route_${log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Excluir rota",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val distanceKm = log.distanceMeters / 1000f
                val minutes = log.durationSeconds / 60
                val seconds = log.durationSeconds % 60

                Text(
                    text = String.format(Locale.getDefault(), "📏 %.2f km", distanceKm),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = String.format(Locale.getDefault(), "⏱️ %02d:%02d", minutes, seconds),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = String.format(Locale.getDefault(), "🔥 %.0f kcal", log.caloriesBurned),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (log.averagePaceMinPerKm > 0) {
                    Text(
                        text = String.format(Locale.getDefault(), "⚡ %.1f min/km", log.averagePaceMinPerKm),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GrowthCyan
                    )
                }
            }

            if (isSelected) {
                val context = LocalContext.current
                Spacer(modifier = Modifier.height(10.dp))
                val coords = remember(log.routePointsJson) { parseCoordinates(log.routePointsJson) }
                if (coords.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Trajeto (${coords.size} pontos GPS):",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        val firstPt = coords.firstOrNull()
                        if (firstPt != null) {
                            Surface(
                                modifier = Modifier
                                    .clickable { openInExternalMap(context, firstPt.first, firstPt.second) }
                                    .testTag("open_log_in_gmaps_${log.id}"),
                                color = CarbonDark,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = GrowthOrangeLight,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Google Maps",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrowthOrangeLight
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    NativeGpsMapView(
                        points = coords,
                        currentLocation = null,
                        isTracking = false,
                        modifier = Modifier.fillMaxWidth(),
                        heightDp = 180
                    )
                }
                if (log.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notas: ${log.notes}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

internal fun parseCoordinates(pointsJson: String): List<Pair<Double, Double>> {
    if (pointsJson.isBlank()) return emptyList()
    return try {
        pointsJson.split(";").mapNotNull { coordStr ->
            val parts = coordStr.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) Pair(lat, lng) else null
            } else null
        }
    } catch (_: Exception) {
        emptyList()
    }
}
