package com.example.ui.screens.gps

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.ui.WorkoutTrackerViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.GpsRouteLog
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.text.SimpleDateFormat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun GpsTrackerScreen(
    viewModel: VirtusViewModel,
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

    // Check location permissions
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
                        ttsIntervalMeters
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

    // Pre-fetch the last fused location as soon as permission is granted.
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

    // Compute Daily & Monthly Running Statistics
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Top Header
        item {
            GrowthHeader(
                title = "Corrida GPS",
                subtitle = "Dados de ${SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(selectedDateMillis))}",
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

        // Permission warning banner if missing
        if (!hasLocationPermission) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = GrowthOrange.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GrowthOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Permissão de GPS Necessária",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GrowthOrangeLight
                            )
                            Text(
                                text = "Ative a localização precisa para traçar o percurso da corrida, calcular o ritmo e a distância em tempo real.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("request_gps_permission_btn")
                        ) {
                            Text("Ativar", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Running Summary Cards: Today vs Month (Uncut, Clean and Balanced)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Today's Running Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("today_running_card"),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORRIDA HOJE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrangeLight,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = GrowthOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f km", todayDistanceKm),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (todayDistanceKm > 0.05) {
                                String.format(Locale.getDefault(), "%d treino(s) • %02d:%02d", todayRunsCount, todayDurationSec / 60, todayDurationSec % 60)
                            } else {
                                "Nenhum treino hoje"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f kcal queimadas", todayCalories),
                            fontSize = 10.sp,
                            color = GrowthOrange,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Month's Running Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("monthly_running_card"),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CORRIDA NO MÊS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthCyan,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f km", totalMonthlyDistanceKm),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$totalMonthlyRunsCount treinos registrados",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "%.0f kcal acumuladas", totalMonthlyCalories),
                            fontSize = 10.sp,
                            color = GrowthCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Live Route Visualizer & Running Telemetry
        item {
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
                    val displayPoints = if (activeSession.isRunning) activeSession.routePoints else selectedLogForDetails?.let { log ->
                        parseCoordinates(log.routePointsJson)
                    } ?: emptyList()

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
                            Surface(
                                color = if (activeSession.isPaused) GrowthYellow.copy(alpha = 0.2f) else GrowthGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (activeSession.isPaused) GrowthYellow else GrowthGreen,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (activeSession.isPaused) "PAUSADO" else "GRAVANDO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeSession.isPaused) GrowthYellow else GrowthGreen,
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

                    // Compact submenu prevents map actions from being squeezed on phones.
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

                    // Map Container: Real OpenStreetMap Leaflet View OR High-Tech Vector Canvas
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

                    // Running Metrics: Distance, Time, Pace (Ritmo), Calories
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

                    // Text-to-Speech (TTS) Voice Coach Card: Distance, Time and Pace Real-time Narration
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tts_voice_coach_card"),
                        colors = CardDefaults.cardColors(containerColor = CarbonDark),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isTtsVoiceCoachEnabled) GrowthGreen.copy(alpha = 0.5f) else CarbonBorder)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        color = if (isTtsVoiceCoachEnabled) GrowthGreen.copy(alpha = 0.18f) else CarbonCard,
                                        shape = CircleShape,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (isTtsVoiceCoachEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                                contentDescription = "Voz do Treinador",
                                                tint = if (isTtsVoiceCoachEnabled) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "TREINADOR POR VOZ (TTS)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTtsVoiceCoachEnabled) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = if (isTtsVoiceCoachEnabled) "Narra distância, tempo e ritmo (pace) ao vivo" else "Voz desativada",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Switch(
                                    checked = isTtsVoiceCoachEnabled,
                                    onCheckedChange = { enabled ->
                                        workoutTrackerViewModel.setTtsEnabled(enabled)
                                        viewModel.configureBackgroundRunningVoice(enabled, ttsIntervalMeters)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = GrowthGreen,
                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        uncheckedTrackColor = CarbonCard
                                    ),
                                    modifier = Modifier.testTag("tts_toggle_switch")
                                )
                            }

                            if (isTtsVoiceCoachEnabled) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
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
                                        border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.8f)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.fillMaxWidth().testTag("speak_metrics_now_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = "Ouvir Métricas",
                                            tint = GrowthGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "OUVIR MÉTRICAS AGORA",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthGreen
                                        )
                                    }

                                    if (activeSession.isRunning) {
                                        Text(
                                            text = "Auto split: ${(ttsIntervalMeters / 1000f).let { if (it >= 1f) "${it.toInt()} km" else "${ttsIntervalMeters.toInt()}m" }}",
                                            fontSize = 10.sp,
                                            color = GrowthCyan,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Avisar por voz a cada:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(500f to "500m", 1000f to "1 km", 2000f to "2 km").forEach { (dist, label) ->
                                            Surface(
                                                color = if (ttsIntervalMeters == dist) GrowthCyan else CarbonCardElevated,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        workoutTrackerViewModel.setNarrationIntervalMeters(dist)
                                                        viewModel.configureBackgroundRunningVoice(isTtsVoiceCoachEnabled, dist)
                                                    }
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (ttsIntervalMeters == dist) FontWeight.Black else FontWeight.Normal,
                                                    color = if (ttsIntervalMeters == dist) Color.Black else Color.White,
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                if (lastSpokenMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "\"$lastSpokenMessage\"",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Session Controls (Start / Pause / Resume / Stop)
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
                                    onClick = {
                                        viewModel.pauseGpsSession()
                                    },
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
                                    onClick = {
                                        viewModel.resumeGpsSession()
                                    },
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
                                onClick = {
                                    showFinishDialog = true
                                },
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

        // Section: Histórico de Corridas
        item {
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
            item {
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(allLogs, key = { it.id }) { log ->
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

    // Dialog: Finish & Save Running Session
    if (showFinishDialog) {
        val finishDistanceKm = activeSession.distanceMeters / 1000f
        val finishDurationMin = activeSession.elapsedSeconds / 60
        val finishDurationSec = activeSession.elapsedSeconds % 60
        val finishPace = if (finishDistanceKm > 0.05f) {
            (activeSession.elapsedSeconds / 60.0f) / finishDistanceKm
        } else 0f

        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            containerColor = CarbonCard,
            title = {
                Text(
                    text = "Salvar Treino de Corrida",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column {
                    Text(
                        text = "Excelente treino! Você correu ${String.format(Locale.getDefault(), "%.2f", finishDistanceKm)} km em ${String.format(Locale.getDefault(), "%02d:%02d", finishDurationMin, finishDurationSec)}${if (finishPace > 0) " com ritmo de ${String.format(Locale.getDefault(), "%.1f", finishPace)} min/km" else ""}.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = routeNameInput,
                        onValueChange = { routeNameInput = it },
                        label = { Text("Nome da Corrida") },
                        modifier = Modifier.fillMaxWidth().testTag("route_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = routeNotesInput,
                        onValueChange = { routeNotesInput = it },
                        label = { Text("Observações (opcional)") },
                        modifier = Modifier.fillMaxWidth().testTag("route_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopAndSaveGpsSession(
                            notes = routeNotesInput.trim(),
                            routeName = routeNameInput.ifBlank { "Corrida ao Ar Livre" }
                        )
                        showFinishDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    modifier = Modifier.testTag("confirm_save_gps_route_btn")
                ) {
                    Text("Salvar Corrida", color = Color.White)
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.discardGpsSession()
                            showFinishDialog = false
                        }
                    ) {
                        Text("Descartar", color = Color(0xFFEF4444))
                    }
                    TextButton(onClick = { showFinishDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}

@Composable
private fun MetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun RoutePathCanvas(
    points: List<Pair<Double, Double>>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Draw topographic background grid
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

        if (points.isEmpty()) {
            // Draw empty placeholder indicator
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

        val minLat = points.minOf { it.first }
        val maxLat = points.maxOf { it.first }
        val minLng = points.minOf { it.second }
        val maxLng = points.maxOf { it.second }

        val latSpan = (maxLat - minLat).coerceAtLeast(0.0001)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.0001)

        val padding = 36f
        val usableWidth = (w - (padding * 2)).coerceAtLeast(10f)
        val usableHeight = (h - (padding * 2)).coerceAtLeast(10f)

        fun projectPoint(lat: Double, lng: Double): Offset {
            val x = padding + (((lng - minLng) / lngSpan) * usableWidth).toFloat()
            // Invert Y axis for coordinates
            val y = padding + ((1.0 - ((lat - minLat) / latSpan)) * usableHeight).toFloat()
            return Offset(x, y)
        }

        if (points.size == 1) {
            val pt = projectPoint(points[0].first, points[0].second)
            drawCircle(color = GrowthGreen.copy(alpha = 0.35f), radius = 24f, center = pt)
            drawCircle(color = GrowthGreen, radius = 10f, center = pt)
            return@Canvas
        }

        // Draw Route Polyline
        val path = Path()
        val firstPt = projectPoint(points[0].first, points[0].second)
        path.moveTo(firstPt.x, firstPt.y)

        for (i in 1 until points.size) {
            val pt = projectPoint(points[i].first, points[i].second)
            path.lineTo(pt.x, pt.y)
        }

        // Outer glow
        drawPath(
            path = path,
            color = GrowthOrange.copy(alpha = 0.3f),
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Main path line
        drawPath(
            path = path,
            color = GrowthOrange,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Starting point marker (Green)
        drawCircle(color = GrowthGreen, radius = 7f, center = firstPt)
        drawCircle(color = Color.White, radius = 3f, center = firstPt)

        // Current / End point marker (Pulsing Cyan)
        val lastPt = projectPoint(points.last().first, points.last().second)
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

            // Running Metrics Row
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
                val coords = parseCoordinates(log.routePointsJson)
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

private fun parseCoordinates(pointsJson: String): List<Pair<Double, Double>> {
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
