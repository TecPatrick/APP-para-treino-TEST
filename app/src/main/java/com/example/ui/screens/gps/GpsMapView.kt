package com.example.ui.screens.gps

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight

enum class MapTileType(val id: String, val label: String, val icon: ImageVector) {
    CARTO_DARK("dark", "Dark", Icons.Default.DarkMode),
    OPEN_STREET_MAP("streets", "Ruas OSM", Icons.Default.Map),
    SATELLITE("satellite", "Satélite", Icons.Default.Satellite)
}

/**
 * Interactive Real-World Map powered by OpenStreetMap, Leaflet.js, and CartoDB Dark.
 * 100% Free, Open API with zero API keys required, plus one-tap Google Maps integration.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GpsInteractiveMapView(
    points: List<Pair<Double, Double>>,
    currentLocation: Pair<Double, Double>?,
    isTracking: Boolean,
    modifier: Modifier = Modifier,
    heightDp: Int = 260
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var selectedLayer by remember { mutableStateOf(MapTileType.CARTO_DARK) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Initial center point: current location -> last point -> São Paulo default
    val centerLat = currentLocation?.first ?: points.lastOrNull()?.first ?: -23.550520
    val centerLng = currentLocation?.second ?: points.lastOrNull()?.second ?: -46.633308

    val htmlData = remember {
        generateLeafletHtml(centerLat, centerLng, selectedLayer.id)
    }

    // Update coordinates in the Leaflet map when points change or new location arrives
    LaunchedEffect(points, currentLocation, isMapLoaded) {
        if (!isMapLoaded) return@LaunchedEffect
        val wv = webViewRef ?: return@LaunchedEffect

        if (points.isNotEmpty()) {
            val jsonArray = points.joinToString(prefix = "[", postfix = "]") { "[${it.first}, ${it.second}]" }
            val centerOnLast = isTracking
            wv.evaluateJavascript("updateRoute('$jsonArray', $centerOnLast);", null)
        } else if (currentLocation != null) {
            wv.evaluateJavascript("setCenter(${currentLocation.first}, ${currentLocation.second}, 16);", null)
        }
    }

    // Switch map tile layer
    LaunchedEffect(selectedLayer, isMapLoaded) {
        if (isMapLoaded) {
            webViewRef?.evaluateJavascript("setLayer('${selectedLayer.id}');", null)
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CarbonDark)
                .testTag("interactive_gps_map_container")
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.cacheMode = WebSettings.LOAD_DEFAULT
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        setBackgroundColor(android.graphics.Color.parseColor("#121316"))

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isMapLoaded = true
                                // Initial feed
                                if (points.isNotEmpty()) {
                                    val jsonArray = points.joinToString(prefix = "[", postfix = "]") { "[${it.first}, ${it.second}]" }
                                    view?.evaluateJavascript("updateRoute('$jsonArray', $isTracking);", null)
                                } else if (currentLocation != null) {
                                    view?.evaluateJavascript("setCenter(${currentLocation.first}, ${currentLocation.second}, 16);", null)
                                }
                            }
                        }
                        loadDataWithBaseURL("https://openstreetmap.org", htmlData, "text/html", "UTF-8", null)
                        webViewRef = this
                    }
                },
                update = { wv ->
                    webViewRef = wv
                }
            )

            // Top overlay bar: Quick actions (Recenter & Open in Google Maps)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live Badge
                Surface(
                    color = CarbonDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (isTracking) GrowthGreen else GrowthCyan,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isTracking) "GPS AO VIVO" else "OPENSTREETMAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTracking) GrowthGreen else GrowthCyan
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Center on GPS button
                    Surface(
                        modifier = Modifier
                            .clickable {
                                val target = currentLocation ?: points.lastOrNull()
                                if (target != null) {
                                    webViewRef?.evaluateJavascript("setCenter(${target.first}, ${target.second}, 17);", null)
                                }
                            }
                            .testTag("recenter_gps_map_btn"),
                        color = CarbonDark.copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Box(modifier = Modifier.padding(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Centralizar GPS",
                                tint = GrowthOrange,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Open directly in Google Maps application or browser
                    Surface(
                        modifier = Modifier
                            .clickable {
                                openInGoogleMaps(context, centerLat, centerLng)
                            }
                            .testTag("open_in_google_maps_btn"),
                        color = CarbonDark.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Google Maps",
                                tint = GrowthOrangeLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
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
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Layer Switcher Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Camada do Mapa:",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MapTileType.values().forEach { tile ->
                    val isSelected = selectedLayer == tile
                    Surface(
                        modifier = Modifier
                            .clickable { selectedLayer = tile }
                            .testTag("map_tile_${tile.id}"),
                        color = if (isSelected) GrowthOrange.copy(alpha = 0.2f) else CarbonCard,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) GrowthOrange else CarbonBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = tile.icon,
                                contentDescription = null,
                                tint = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tile.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Opens Google Maps app or browser at specified coordinates.
 */
fun openInGoogleMaps(context: Context, latitude: Double, longitude: Double) {
    try {
        val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude(Treino+Growth)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback to web browser Google Maps link
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    } catch (_: Exception) {
        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
    }
}

private fun generateLeafletHtml(lat: Double, lng: Double, initialLayer: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                html, body, #map {
                    margin: 0;
                    padding: 0;
                    width: 100%;
                    height: 100%;
                    background-color: #121316;
                    font-family: sans-serif;
                }
                .leaflet-container {
                    background: #121316;
                }
                .leaflet-control-attribution {
                    background: rgba(18, 19, 22, 0.7) !important;
                    color: #888888 !important;
                    font-size: 8px !important;
                    padding: 1px 4px !important;
                }
                .leaflet-control-attribution a {
                    color: #ff6b00 !important;
                    text-decoration: none !important;
                }
                .leaflet-bar a {
                    background-color: #1b1d22 !important;
                    color: #ffffff !important;
                    border-bottom: 1px solid #2a2e36 !important;
                }
                .leaflet-bar a:hover {
                    background-color: #2a2e36 !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map', {
                    zoomControl: true,
                    attributionControl: true
                }).setView([$lat, $lng], 16);

                var layers = {
                    dark: L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                        maxZoom: 19,
                        subdomains: 'abcd',
                        attribution: '&copy; CARTO &copy; OSM'
                    }),
                    streets: L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        maxZoom: 19,
                        attribution: '&copy; OpenStreetMap'
                    }),
                    satellite: L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
                        maxZoom: 19,
                        attribution: '&copy; Esri World Imagery'
                    })
                };

                var currentLayerName = '$initialLayer';
                var currentLayer = layers[currentLayerName] || layers.dark;
                currentLayer.addTo(map);

                function setLayer(name) {
                    if (layers[name] && name !== currentLayerName) {
                        map.removeLayer(currentLayer);
                        currentLayer = layers[name];
                        currentLayer.addTo(map);
                        currentLayerName = name;
                    }
                }

                // Glowing Route Polyline
                var polylineGlow = L.polyline([], {
                    color: '#FF6B00',
                    weight: 10,
                    opacity: 0.35,
                    lineCap: 'round',
                    lineJoin: 'round'
                }).addTo(map);

                var polyline = L.polyline([], {
                    color: '#FF6B00',
                    weight: 5,
                    opacity: 0.95,
                    lineCap: 'round',
                    lineJoin: 'round'
                }).addTo(map);

                var startMarker = null;
                var currentMarker = null;

                function updateRoute(pointsJson, centerOnLast) {
                    try {
                        var pts = JSON.parse(pointsJson);
                        if (!pts || pts.length === 0) return;

                        polyline.setLatLngs(pts);
                        polylineGlow.setLatLngs(pts);

                        // Start Marker (Green circle)
                        if (!startMarker && pts.length > 0) {
                            startMarker = L.circleMarker(pts[0], {
                                radius: 7,
                                color: '#00E676',
                                fillColor: '#FFFFFF',
                                fillOpacity: 1,
                                weight: 3
                            }).addTo(map).bindPopup('Início do Trajeto');
                        }

                        // Current Runner Marker (Orange pulsing circle)
                        var lastPt = pts[pts.length - 1];
                        if (!currentMarker) {
                            currentMarker = L.circleMarker(lastPt, {
                                radius: 8,
                                color: '#FF6B00',
                                fillColor: '#00E5FF',
                                fillOpacity: 1,
                                weight: 3
                            }).addTo(map).bindPopup('Localização Atual');
                        } else {
                            currentMarker.setLatLng(lastPt);
                        }

                        if (centerOnLast) {
                            map.panTo(lastPt);
                        } else if (pts.length > 1) {
                            map.fitBounds(polyline.getBounds(), { padding: [30, 30] });
                        }
                    } catch (e) {
                        console.error(e);
                    }
                }

                function setCenter(cLat, cLng, zoom) {
                    map.setView([cLat, cLng], zoom || 16);
                    if (!currentMarker) {
                        currentMarker = L.circleMarker([cLat, cLng], {
                            radius: 8,
                            color: '#FF6B00',
                            fillColor: '#00E5FF',
                            fillOpacity: 1,
                            weight: 3
                        }).addTo(map);
                    } else {
                        currentMarker.setLatLng([cLat, cLng]);
                    }
                }
            </script>
        </body>
        </html>
    """.trimIndent()
}
