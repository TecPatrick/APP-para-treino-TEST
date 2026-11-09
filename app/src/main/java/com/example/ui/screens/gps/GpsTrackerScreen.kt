package com.example.ui.screens.gps

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GpsRouteLog
import com.example.ui.CardioViewModel
import com.example.ui.WorkoutTrackerViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.screens.gps.components.GpsPermissionBanner
import com.example.ui.screens.gps.components.GpsRouteLogCard
import com.example.ui.screens.gps.components.GpsSummaryRow
import com.example.ui.screens.gps.components.GpsVoiceCoachCard
import com.example.ui.screens.gps.components.MetricBox
import com.example.ui.screens.gps.components.RoutePathCanvas
import com.example.ui.screens.gps.components.SaveGpsSessionDialog
import com.example.ui.screens.gps.components.parseCoordinates
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun GpsTrackerScreen(
    viewModel: CardioViewModel,
    workoutTrackerViewModel: WorkoutTrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeGpsSession.collectAsStateWithLifecycle()
    val allLogs by viewModel.allGpsRouteLogs.collectAsStateWithLifecycle()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val voiceState by workoutTrackerViewModel.uiState.collectAsStateWithLifecycle()
    val ttsIntervalMeters = voiceState.narrationIntervalMeters
    val isTtsVoiceCoachEnabled = voiceState.isTtsEnabled
    val targetPaceSecondsPerKm = voiceState.targetPaceSecondsPerKm
    val isPaceAlertEnabled = voiceState.isPaceAlertEnabled
    val lastSpokenMessage = voiceState.lastNarratedMessage
    var showFinishDialog by remember { mutableStateOf(false) }
    var routeNameInput by remember { mutableStateOf("Corrida ao Ar Livre") }
    var routeNotesInput by remember { mutableStateOf("") }
    var selectedLogForDetails by remember { mutableStateOf<GpsRouteLog?>(null) }
    var currentGpsLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var useInteractiveMap by remember { mutableStateOf(true) }
    var showMapOptions by remember { mutableStateOf(false) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var startAfterPermission by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val startGpsWithCurrentLocation: () -> Unit = {
        val tokenSource = CancellationTokenSource()
        try {
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, tokenSource.token)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        viewModel.showToast("Não foi possível obter o GPS. Ative a localização e tente novamente.")
                        return@addOnSuccessListener
                    }
                    currentGpsLocation = location.latitude to location.longitude
                    viewModel.startGpsSession(
                        location.latitude,
                        location.longitude,
                        isTtsVoiceCoachEnabled,
                        ttsIntervalMeters,
                        targetPaceSecondsPerKm,
                        isPaceAlertEnabled
                    )
                }
                .addOnFailureListener {
                    viewModel.showToast("Falha ao localizar o aparelho. Verifique o GPS e tente novamente.")
                }
        } catch (_: SecurityException) {
            viewModel.showToast("A permissão de localização foi removida.")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fine || coarse
        if (hasLocationPermission) {
            viewModel.showToast("Permissão de GPS concedida!")
            if (startAfterPermission) {
                startAfterPermission = false
                startGpsWithCurrentLocation()
            }
        } else {
            startAfterPermission = false
            viewModel.showToast("A corrida precisa da permissão de localização.")
        }
    }

    DisposableEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let { currentGpsLocation = it.latitude to it.longitude }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {}
        }
        onDispose {}
    }

    LaunchedEffect(activeSession.routePoints.lastOrNull()) {
        activeSession.routePoints.lastOrNull()?.let { currentGpsLocation = it }
    }

    val todayStartMillis = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val selectedDayEndMillis = remember(todayStartMillis) {
        Calendar.getInstance().apply { timeInMillis = todayStartMillis; add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
    }
    val monthStartMillis = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }
    val monthEndMillis = remember(monthStartMillis) {
        Calendar.getInstance().apply { timeInMillis = monthStartMillis; add(Calendar.MONTH, 1) }.timeInMillis
    }
    val selectedIsToday = remember(todayStartMillis) {
        val current = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        todayStartMillis == current
    }

    val dailyLogs = remember(allLogs, todayStartMillis, selectedDayEndMillis) {
        allLogs.filter { it.dateMillis in todayStartMillis until selectedDayEndMillis }
    }
    val monthlyLogs = remember(allLogs, monthStartMillis, monthEndMillis) {
        allLogs.filter { it.dateMillis in monthStartMillis until monthEndMillis }
    }

    val activeDistance = if (selectedIsToday) activeSession.distanceMeters else 0f
    val activeDuration = if (selectedIsToday) activeSession.elapsedSeconds else 0
    val activeCalories = if (selectedIsToday) activeSession.caloriesBurned else 0f
    val todayDistanceKm = (dailyLogs.sumOf { it.distanceMeters.toDouble() } + activeDistance) / 1000.0
    val todayDurationSec = dailyLogs.sumOf { it.durationSeconds } + activeDuration
    val todayCalories = dailyLogs.sumOf { it.caloriesBurned.toDouble() } + activeCalories
    val todayRunsCount = dailyLogs.size + if (selectedIsToday && activeSession.isRunning) 1 else 0

    val totalMonthlyDistanceKm = (monthlyLogs.sumOf { it.distanceMeters.toDouble() } + activeDistance) / 1000.0
    val totalMonthlyCalories = monthlyLogs.sumOf { it.caloriesBurned.toDouble() } + activeCalories
    val totalMonthlyRunsCount = monthlyLogs.size + if (selectedIsToday && activeSession.isRunning) 1 else 0

    val subtitleDate = remember(selectedDateMillis) {
        SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(selectedDateMillis))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item(key = "gps_header", contentType = "header") {
            GrowthHeader(
                title = "Corrida GPS",
                subtitle = "Dados de $subtitleDate",
                badgeText = "TREINO DE CORRIDA",
                trailingContent = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("gps_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        }

        if (!hasLocationPermission) {
            item(key = "gps_permission_banner", contentType = "banner") {
                GpsPermissionBanner(
                    onRequestPermission = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }

        item(key = "gps_summary_row", contentType = "summary") {
            GpsSummaryRow(
                todayDistanceKm = todayDistanceKm,
                todayRunsCount = todayRunsCount,
                todayDurationSec = todayDurationSec,
                todayCalories = todayCalories,
                totalMonthlyDistanceKm = totalMonthlyDistanceKm,
                totalMonthlyRunsCount = totalMonthlyRunsCount,
                totalMonthlyCalories = totalMonthlyCalories
            )
        }

        item(key = "gps_live_route_card", contentType = "route_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("route_canvas_card"),
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CarbonBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    val displayPoints = remember(activeSession.isRunning, activeSession.routePoints, selectedLogForDetails) {
                        if (activeSession.isRunning) {
                            activeSession.routePoints
                        } else {
                            selectedLogForDetails?.let { log -> parseCoordinates(log.routePointsJson) } ?: emptyList()
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = null,
                                tint = GrowthOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (activeSession.isRunning) "CORRIDA EM TEMPO REAL" else "MAPA DO PERCURSO",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeSession.isRunning) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.6.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        if (activeSession.isRunning) {
                            val badgeBg = when {
                                activeSession.isAutoPaused -> GrowthOrange.copy(alpha = 0.25f)
                                activeSession.isPaused -> GrowthYellow.copy(alpha = 0.2f)
                                else -> GrowthGreen.copy(alpha = 0.2f)
                            }
                            val badgeColor = when {
                                activeSession.isAutoPaused -> GrowthOrangeLight
                                activeSession.isPaused -> GrowthYellow
                                else -> GrowthGreen
                            }
                            Surface(
                                color = badgeBg,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(badgeColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = when {
                                            activeSession.isAutoPaused -> "PAUSA AUTOMÁTICA"
                                            activeSession.isPaused -> "PAUSADO"
                                            else -> "GRAVANDO"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        } else if (currentGpsLocation != null) {
                            Surface(
                                color = GrowthCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "SINAL GPS OK",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthCyan,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMapOptions = !showMapOptions }
                            .testTag("toggle_map_options_btn"),
                        color = CarbonDark,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, CarbonBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, null, tint = GrowthOrangeLight, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("OPÇÕES DO MAPA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                            }
                            Text(if (showMapOptions) "OCULTAR" else "MOSTRAR", fontSize = 9.sp, color = GrowthCyan)
                        }
                    }

                    if (showMapOptions) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { useInteractiveMap = true }
                                        .testTag("toggle_interactive_map_btn"),
                                    color = if (useInteractiveMap) GrowthOrange.copy(alpha = 0.22f) else CarbonDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (useInteractiveMap) GrowthOrange else CarbonBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Map,
                                            contentDescription = null,
                                            tint = if (useInteractiveMap) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Mapa de ruas",
                                            fontSize = 10.sp,
                                            fontWeight = if (useInteractiveMap) FontWeight.Bold else FontWeight.Normal,
                                            color = if (useInteractiveMap) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { useInteractiveMap = false }
                                        .testTag("toggle_vector_grid_btn"),
                                    color = if (!useInteractiveMap) GrowthCyan.copy(alpha = 0.22f) else CarbonDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (!useInteractiveMap) GrowthCyan else CarbonBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GridOn,
                                            contentDescription = null,
                                            tint = if (!useInteractiveMap) GrowthCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Traçado offline",
                                            fontSize = 10.sp,
                                            fontWeight = if (!useInteractiveMap) FontWeight.Bold else FontWeight.Normal,
                                            color = if (!useInteractiveMap) GrowthCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            val activeCoord = currentGpsLocation ?: displayPoints.lastOrNull()
                            if (activeCoord != null) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            openInExternalMap(context, activeCoord.first, activeCoord.second)
                                        }
                                        .testTag("open_google_maps_top_btn"),
                                    color = CarbonDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CarbonBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInNew,
                                            contentDescription = "Abrir Google Maps",
                                            tint = GrowthOrangeLight,
                                            modifier = Modifier.size(12.dp)
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (useInteractiveMap) {
                        NativeGpsMapView(
                            points = displayPoints,
                            currentLocation = currentGpsLocation,
                            isTracking = activeSession.isRunning && !activeSession.isPaused,
                            modifier = Modifier.fillMaxWidth(),
                            heightDp = 260
                        )
                    } else {
                        RoutePathCanvas(
                            points = displayPoints,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CarbonDark)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val distanceKm = activeSession.distanceMeters / 1000f
                        val durationMin = activeSession.elapsedSeconds / 60
                        val durationSec = activeSession.elapsedSeconds % 60
                        val paceText = if (distanceKm > 0.05f) {
                            val paceDecimal = (activeSession.elapsedSeconds / 60.0f) / distanceKm
                            val pMin = paceDecimal.toInt()
                            val pSec = (((paceDecimal - pMin) * 60).toInt()).coerceIn(0, 59)
                            String.format(Locale.getDefault(), "%d'%02d\" /km", pMin, pSec)
                        } else {
                            "--'--\" /km"
                        }

                        MetricBox(
                            label = "DISTÂNCIA",
                            value = String.format(Locale.getDefault(), "%.2f km", distanceKm),
                            color = GrowthOrange,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "TEMPO",
                            value = String.format(Locale.getDefault(), "%02d:%02d", durationMin, durationSec),
                            color = GrowthCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "RITMO",
                            value = paceText,
                            color = GrowthGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBox(
                            label = "CALORIAS",
                            value = String.format(Locale.getDefault(), "%.0f kcal", activeSession.caloriesBurned),
                            color = GrowthYellow,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    GpsVoiceCoachCard(
                        isTtsVoiceCoachEnabled = isTtsVoiceCoachEnabled,
                        ttsIntervalMeters = ttsIntervalMeters,
                        targetPaceSecondsPerKm = targetPaceSecondsPerKm,
                        isPaceAlertEnabled = isPaceAlertEnabled,
                        lastSpokenMessage = lastSpokenMessage,
                        isSessionRunning = activeSession.isRunning,
                        onToggleTts = { enabled ->
                            workoutTrackerViewModel.setTtsEnabled(enabled)
                            viewModel.configureBackgroundRunningVoice(
                                enabled,
                                ttsIntervalMeters,
                                targetPaceSecondsPerKm,
                                isPaceAlertEnabled
                            )
                        },
                        onSpeakMetricsNow = {
                            val paceSec = if (activeSession.distanceMeters > 20f && activeSession.elapsedSeconds > 0) {
                                (activeSession.elapsedSeconds / (activeSession.distanceMeters / 1000f)).toLong().coerceIn(60, 3600)
                            } else 0L
                            val message = workoutTrackerViewModel.formatFullWorkoutSpeech(
                                distanceMeters = activeSession.distanceMeters,
                                elapsedSeconds = activeSession.elapsedSeconds.toLong(),
                                paceSecondsPerKm = paceSec
                            )
                            workoutTrackerViewModel.speak(message)
                        },
                        onIntervalSelected = { dist ->
                            workoutTrackerViewModel.setNarrationIntervalMeters(dist)
                            viewModel.configureBackgroundRunningVoice(
                                isTtsVoiceCoachEnabled,
                                dist,
                                targetPaceSecondsPerKm,
                                isPaceAlertEnabled
                            )
                        },
                        onTogglePaceAlert = { enabled ->
                            workoutTrackerViewModel.setPaceAlertEnabled(enabled)
                            viewModel.configureBackgroundRunningVoice(
                                isTtsVoiceCoachEnabled,
                                ttsIntervalMeters,
                                targetPaceSecondsPerKm,
                                enabled
                            )
                        },
                        onTargetPaceSelected = { paceSec ->
                            workoutTrackerViewModel.setTargetPaceSecondsPerKm(paceSec)
                            viewModel.configureBackgroundRunningVoice(
                                isTtsVoiceCoachEnabled,
                                ttsIntervalMeters,
                                paceSec,
                                isPaceAlertEnabled
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!activeSession.isRunning) {
                            Button(
                                onClick = {
                                    if (!hasLocationPermission) {
                                        startAfterPermission = true
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    } else {
                                        startGpsWithCurrentLocation()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_gps_run_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("INICIAR CORRIDA COM GPS", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            if (!activeSession.isPaused) {
                                OutlinedButton(
                                    onClick = { viewModel.pauseGpsSession() },
                                    border = BorderStroke(1.dp, GrowthYellow),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("pause_gps_btn")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = GrowthYellow)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PAUSAR", color = GrowthYellow, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.resumeGpsSession() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("resume_gps_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RETOMAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                }
                            }

                            Button(
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("finish_gps_btn")
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("FINALIZAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        item(key = "gps_history_header", contentType = "section_header") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HISTÓRICO DE CORRIDAS (${allLogs.size})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp
                )
            }
        }

        if (allLogs.isEmpty() && !activeSession.isRunning) {
            item(key = "gps_empty_state", contentType = "empty_state") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nenhuma corrida gravada ainda",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Clique em 'Iniciar Corrida com GPS' para registrar seu trajeto, ritmo, distância e gasto calórico.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = allLogs,
                key = { it.id },
                contentType = { "gps_route_log" }
            ) { log ->
                GpsRouteLogCard(
                    log = log,
                    isSelected = selectedLogForDetails?.id == log.id,
                    onClick = {
                        selectedLogForDetails = if (selectedLogForDetails?.id == log.id) null else log
                    },
                    onDelete = { viewModel.deleteGpsRoute(log.id) }
                )
            }
        }
    }

    if (showFinishDialog) {
        val finishDistanceKm = activeSession.distanceMeters / 1000f
        val finishDurationMin = activeSession.elapsedSeconds / 60
        val finishDurationSec = activeSession.elapsedSeconds % 60
        val finishPace = if (finishDistanceKm > 0.05f) {
            (activeSession.elapsedSeconds / 60.0f) / finishDistanceKm
        } else 0f

        SaveGpsSessionDialog(
            finishDistanceKm = finishDistanceKm,
            finishDurationMin = finishDurationMin,
            finishDurationSec = finishDurationSec,
            finishPace = finishPace,
            routeNameInput = routeNameInput,
            routeNotesInput = routeNotesInput,
            onRouteNameChange = { routeNameInput = it },
            onRouteNotesChange = { routeNotesInput = it },
            onConfirmSave = {
                viewModel.stopAndSaveGpsSession(
                    notes = routeNotesInput.trim(),
                    routeName = routeNameInput.ifBlank { "Corrida ao Ar Livre" }
                )
                showFinishDialog = false
            },
            onDiscard = {
                viewModel.discardGpsSession()
                showFinishDialog = false
            },
            onDismiss = { showFinishDialog = false }
        )
    }
}
