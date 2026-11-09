package com.example.ui.screens.gps

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import kotlinx.coroutines.delay
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.circleColor
import org.maplibre.android.style.layers.PropertyFactory.circleRadius
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeColor
import org.maplibre.android.style.layers.PropertyFactory.circleStrokeWidth
import org.maplibre.android.style.layers.PropertyFactory.lineColor
import org.maplibre.android.style.layers.PropertyFactory.lineOpacity
import org.maplibre.android.style.layers.PropertyFactory.lineWidth
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import kotlin.math.abs
import kotlin.math.hypot

private const val OPEN_FREE_MAP_STREETS = "https://tiles.openfreemap.org/styles/liberty"

private const val ROUTE_SOURCE = "virtus-route-source"
private const val ROUTE_HALO_LAYER = "virtus-route-halo"
private const val ROUTE_LAYER = "virtus-route-line"
private const val START_SOURCE = "virtus-start-source"
private const val START_LAYER = "virtus-start-point"
private const val CURRENT_SOURCE = "virtus-current-source"
private const val CURRENT_LAYER = "virtus-current-point"

private const val MAP_UPDATE_INTERVAL_MS = 1000L
internal const val MAX_RENDERED_POLYLINE_POINTS = 1000

/**
 * Simplifies a GPS polyline using the iterative Ramer-Douglas-Peucker algorithm
 * when the point count exceeds [maxPoints].
 */
internal fun simplifyPolylineRdp(
    points: List<Pair<Double, Double>>,
    maxPoints: Int = MAX_RENDERED_POLYLINE_POINTS,
    initialEpsilon: Double = 0.00003
): List<Pair<Double, Double>> {
    if (points.size <= maxPoints || points.size < 3) return points

    var epsilon = initialEpsilon
    var simplified = rdpPass(points, epsilon)
    var attempts = 0
    while (simplified.size > maxPoints && attempts < 6) {
        epsilon *= 2.0
        simplified = rdpPass(simplified, epsilon)
        attempts++
    }
    return simplified
}

private fun rdpPass(
    points: List<Pair<Double, Double>>,
    epsilon: Double
): List<Pair<Double, Double>> {
    val n = points.size
    if (n < 3) return points
    val keep = BooleanArray(n)
    keep[0] = true
    keep[n - 1] = true

    val stack = ArrayDeque<IntRange>()
    stack.addLast(0..(n - 1))

    while (stack.isNotEmpty()) {
        val range = stack.removeLast()
        val start = range.first
        val end = range.last
        if (end - start <= 1) continue

        var maxDist = 0.0
        var index = start
        val startPt = points[start]
        val endPt = points[end]

        for (i in (start + 1) until end) {
            val dist = perpendicularDistance(points[i], startPt, endPt)
            if (dist > maxDist) {
                index = i
                maxDist = dist
            }
        }

        if (maxDist > epsilon) {
            keep[index] = true
            stack.addLast(start..index)
            stack.addLast(index..end)
        }
    }

    val result = ArrayList<Pair<Double, Double>>(n)
    for (i in 0 until n) {
        if (keep[i]) result.add(points[i])
    }
    return result
}

private fun perpendicularDistance(
    pt: Pair<Double, Double>,
    lineStart: Pair<Double, Double>,
    lineEnd: Pair<Double, Double>
): Double {
    val dx = lineEnd.second - lineStart.second
    val dy = lineEnd.first - lineStart.first
    if (dx == 0.0 && dy == 0.0) {
        return hypot(pt.second - lineStart.second, pt.first - lineStart.first)
    }
    val numerator = abs(
        dy * pt.second - dx * pt.first +
            lineEnd.second * lineStart.first - lineEnd.first * lineStart.second
    )
    val denominator = hypot(dx, dy)
    return numerator / denominator
}

/** Native, keyless map powered by MapLibre and OpenFreeMap/OpenStreetMap. */
@Composable
fun NativeGpsMapView(
    points: List<Pair<Double, Double>>,
    currentLocation: Pair<Double, Double>?,
    isTracking: Boolean,
    modifier: Modifier = Modifier,
    heightDp: Int = 260
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    val simplifiedPoints = remember(points) {
        simplifyPolylineRdp(points)
    }
    val targetPair = currentLocation ?: simplifiedPoints.lastOrNull()
    var lastMapUpdateElapsedMs by remember { mutableLongStateOf(0L) }

    val mapView = remember(context) {
        MapLibre.getInstance(context.applicationContext)
        MapView(context).apply { onCreate(null) }
    }

    DisposableEffect(mapView, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(mapView) {
        mapView.getMapAsync { map ->
            mapLibreMap = map
            map.uiSettings.isLogoEnabled = true
            map.uiSettings.isAttributionEnabled = true
        }
    }

    LaunchedEffect(mapLibreMap) {
        mapLibreMap?.setStyle(OPEN_FREE_MAP_STREETS) { style ->
            renderGpsRoute(style, simplifiedPoints, targetPair)
        }
    }

    LaunchedEffect(mapLibreMap, simplifiedPoints, targetPair) {
        val map = mapLibreMap ?: return@LaunchedEffect
        val now = SystemClock.elapsedRealtime()
        val elapsedSinceLast = now - lastMapUpdateElapsedMs
        if (lastMapUpdateElapsedMs > 0L && elapsedSinceLast < MAP_UPDATE_INTERVAL_MS) {
            delay(MAP_UPDATE_INTERVAL_MS - elapsedSinceLast)
        }
        lastMapUpdateElapsedMs = SystemClock.elapsedRealtime()

        map.getStyle { style -> renderGpsRoute(style, simplifiedPoints, targetPair) }
        targetPair?.let { target ->
            map.animateCamera(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(LatLng(target.first, target.second))
                        .zoom(if (isTracking) 17.0 else 16.0)
                        .build()
                ),
                650
            )
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonDark)
                .testTag("native_gps_map_container")
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = CarbonDark.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).background(if (isTracking) GrowthGreen else GrowthCyan, CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text(if (isTracking) "GPS AO VIVO" else "MAPLIBRE", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        modifier = Modifier.clickable {
                            targetPair?.let { target ->
                                mapLibreMap?.animateCamera(
                                    CameraUpdateFactory.newLatLngZoom(LatLng(target.first, target.second), 17.0)
                                )
                            }
                        }.testTag("recenter_native_map_btn"),
                        color = CarbonDark.copy(alpha = 0.88f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Box(Modifier.padding(9.dp)) {
                            Icon(Icons.Default.MyLocation, "Centralizar GPS", tint = GrowthOrange, modifier = Modifier.size(18.dp))
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable {
                            targetPair?.let { openInExternalMap(context, it.first, it.second) }
                        }.testTag("open_external_map_btn"),
                        color = CarbonDark.copy(alpha = 0.88f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Box(Modifier.padding(9.dp)) {
                            Icon(Icons.Default.OpenInNew, "Abrir em outro mapa", tint = GrowthOrangeLight, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Text(
                text = "© OpenFreeMap © OpenStreetMap",
                fontSize = 8.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
            )
        }
    }
}

private fun renderGpsRoute(
    style: Style,
    points: List<Pair<Double, Double>>,
    currentLocation: Pair<Double, Double>?
) {
    val routePoints = points.map { Point.fromLngLat(it.second, it.first) }
    if (routePoints.size >= 2) {
        val routeFeature = Feature.fromGeometry(LineString.fromLngLats(routePoints))
        val source = style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE)
        if (source == null) {
            style.addSource(GeoJsonSource(ROUTE_SOURCE, routeFeature))
            style.addLayer(
                LineLayer(ROUTE_HALO_LAYER, ROUTE_SOURCE).withProperties(
                    lineColor("#A62916"), lineWidth(12f), lineOpacity(0.38f)
                )
            )
            style.addLayer(
                LineLayer(ROUTE_LAYER, ROUTE_SOURCE).withProperties(
                    lineColor("#FF6B35"), lineWidth(5f), lineOpacity(1f)
                )
            )
        } else {
            source.setGeoJson(routeFeature)
        }
    }

    points.firstOrNull()?.let { start ->
        upsertPoint(style, START_SOURCE, START_LAYER, start, "#22C55E", 6f)
    }
    currentLocation?.let { current ->
        upsertPoint(style, CURRENT_SOURCE, CURRENT_LAYER, current, "#22D3EE", 8f)
    }
}

private fun upsertPoint(
    style: Style,
    sourceId: String,
    layerId: String,
    coordinate: Pair<Double, Double>,
    color: String,
    radius: Float
) {
    val feature = Feature.fromGeometry(Point.fromLngLat(coordinate.second, coordinate.first))
    val source = style.getSourceAs<GeoJsonSource>(sourceId)
    if (source == null) {
        style.addSource(GeoJsonSource(sourceId, feature))
        style.addLayer(
            CircleLayer(layerId, sourceId).withProperties(
                circleColor(color),
                circleRadius(radius),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(2.5f)
            )
        )
    } else {
        source.setGeoJson(feature)
    }
}

/** Opens any installed maps app; browsers handle the URI when none is installed. */
fun openInExternalMap(context: Context, latitude: Double, longitude: Double) {
    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Treino+Virtus)")
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
    val intent = if (geoIntent.resolveActivity(context.packageManager) != null) {
        geoIntent
    } else {
        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=$latitude&mlon=$longitude#map=17/$latitude/$longitude"))
    }
    runCatching { context.startActivity(intent) }
}
