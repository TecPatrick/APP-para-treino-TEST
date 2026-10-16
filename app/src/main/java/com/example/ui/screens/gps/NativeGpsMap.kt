package com.example.ui.screens.gps

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
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

private const val OPEN_FREE_MAP_STREETS = "https://tiles.openfreemap.org/styles/liberty"

private const val ROUTE_SOURCE = "virtus-route-source"
private const val ROUTE_HALO_LAYER = "virtus-route-halo"
private const val ROUTE_LAYER = "virtus-route-line"
private const val START_SOURCE = "virtus-start-source"
private const val START_LAYER = "virtus-start-point"
private const val CURRENT_SOURCE = "virtus-current-source"
private const val CURRENT_LAYER = "virtus-current-point"

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
    val targetPair = currentLocation ?: points.lastOrNull()

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
            renderGpsRoute(style, points, targetPair)
        }
    }

    LaunchedEffect(mapLibreMap, points, targetPair) {
        val map = mapLibreMap ?: return@LaunchedEffect
        map.getStyle { style -> renderGpsRoute(style, points, targetPair) }
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
