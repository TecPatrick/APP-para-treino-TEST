package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.ui.ActiveGpsSession
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Owns the active outdoor run while the app is backgrounded or the screen is locked.
 * Location, timer and milestone narration must not depend on a Compose screen lifecycle.
 */
class RunningForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var voice: VoiceProvider
    private var locationCallback: LocationCallback? = null
    private var timerJob: Job? = null
    private var voiceEnabled = true
    private var narrationIntervalMeters = 1000f
    private var lastNarratedDistanceMeters = 0f

    // Auto-pause when stationary for > 10 seconds
    private var isAutoPaused = false
    private var lastMovementTimestamp: Long = 0L

    // Pace voice alert threshold
    private var targetPaceSecondsPerKm: Long = 0L
    private var isPaceAlertEnabled = false
    private var lastPaceAlertTimestamp: Long = 0L

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        voice = VoiceProviderFactory.create(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startRun(
                intent.getDoubleExtra(EXTRA_LATITUDE, Double.NaN),
                intent.getDoubleExtra(EXTRA_LONGITUDE, Double.NaN),
                intent.getBooleanExtra(EXTRA_VOICE_ENABLED, true),
                intent.getFloatExtra(EXTRA_NARRATION_INTERVAL, 1000f),
                intent.getLongExtra(EXTRA_TARGET_PACE, 0L),
                intent.getBooleanExtra(EXTRA_PACE_ALERT_ENABLED, false)
            )
            ACTION_PAUSE -> pauseRun()
            ACTION_RESUME -> resumeRun()
            ACTION_CONFIGURE_VOICE -> configureVoice(
                intent.getBooleanExtra(EXTRA_VOICE_ENABLED, true),
                intent.getFloatExtra(EXTRA_NARRATION_INTERVAL, 1000f),
                intent.getLongExtra(EXTRA_TARGET_PACE, 0L),
                intent.getBooleanExtra(EXTRA_PACE_ALERT_ENABLED, false)
            )
            ACTION_STOP -> stopRun()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRun(
        lat: Double,
        lng: Double,
        ttsEnabled: Boolean,
        intervalMeters: Float,
        targetPaceSeconds: Long,
        paceAlertEnabled: Boolean
    ) {
        if (_session.value.isRunning) return
        voiceEnabled = ttsEnabled
        narrationIntervalMeters = intervalMeters.coerceIn(100f, 10_000f)
        lastNarratedDistanceMeters = 0f
        targetPaceSecondsPerKm = targetPaceSeconds
        isPaceAlertEnabled = paceAlertEnabled
        lastPaceAlertTimestamp = 0L
        isAutoPaused = false
        lastMovementTimestamp = System.currentTimeMillis()

        val initialPoints = if (lat.isFinite() && lng.isFinite()) listOf(lat to lng) else emptyList()
        _session.value = ActiveGpsSession(
            isRunning = true,
            isPaused = false,
            isAutoPaused = false,
            targetPaceSecondsPerKm = targetPaceSecondsPerKm,
            isPaceAlertEnabled = isPaceAlertEnabled,
            routePoints = initialPoints
        )
        startForeground(NOTIFICATION_ID, buildNotification())
        startTimer()
        startLocationUpdates()
        if (voiceEnabled) voice.speak("Treino de corrida iniciado. Boa corrida!")
    }

    private fun pauseRun() {
        if (!_session.value.isRunning || _session.value.isPaused) return
        isAutoPaused = false
        _session.value = _session.value.copy(
            isPaused = true,
            isAutoPaused = false,
            currentSpeedKmh = 0f
        )
        stopLocationUpdates()
        updateNotification()
        if (voiceEnabled) voice.speak("Treino pausado.")
    }

    private fun resumeRun() {
        if (!_session.value.isRunning || !_session.value.isPaused) return
        isAutoPaused = false
        lastMovementTimestamp = System.currentTimeMillis()
        _session.value = _session.value.copy(
            isPaused = false,
            isAutoPaused = false
        )
        startLocationUpdates()
        updateNotification()
        if (voiceEnabled) voice.speak("Treino retomado. Vamos lá!")
    }

    private fun autoPauseRun() {
        val current = _session.value
        if (!current.isRunning || current.isPaused) return
        isAutoPaused = true
        _session.value = current.copy(
            isPaused = true,
            isAutoPaused = true,
            currentSpeedKmh = 0f
        )
        updateNotification()
        if (voiceEnabled) {
            voice.speak("Pausa automática. Movimento estacionário detectado.")
        }
    }

    private fun autoResumeRun() {
        val current = _session.value
        if (!current.isRunning || !current.isPaused || !isAutoPaused) return
        isAutoPaused = false
        lastMovementTimestamp = System.currentTimeMillis()
        _session.value = current.copy(
            isPaused = false,
            isAutoPaused = false
        )
        updateNotification()
        if (voiceEnabled) {
            voice.speak("Treino retomado automaticamente. Vamos lá!")
        }
    }

    private fun configureVoice(
        enabled: Boolean,
        intervalMeters: Float,
        targetPaceSeconds: Long,
        paceAlertEnabled: Boolean
    ) {
        voiceEnabled = enabled
        narrationIntervalMeters = intervalMeters.coerceIn(100f, 10_000f)
        targetPaceSecondsPerKm = targetPaceSeconds
        isPaceAlertEnabled = paceAlertEnabled
        _session.value = _session.value.copy(
            targetPaceSecondsPerKm = targetPaceSeconds,
            isPaceAlertEnabled = paceAlertEnabled
        )
        if (!enabled) voice.stop()
    }

    private fun stopRun() {
        stopLocationUpdates()
        timerJob?.cancel()
        timerJob = null
        voice.stop()
        isAutoPaused = false
        _session.value = ActiveGpsSession()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (_session.value.isRunning) {
                delay(1000L)
                val current = _session.value
                if (!current.isPaused) {
                    val timeSinceMovement = System.currentTimeMillis() - lastMovementTimestamp
                    if (timeSinceMovement >= 10_000L && current.elapsedSeconds >= 10) {
                        // Stationary movement detected for more than 10 seconds -> auto pause
                        autoPauseRun()
                    } else {
                        val seconds = current.elapsedSeconds + 1
                        val pace = calculatePace(current.distanceMeters, seconds)
                        val speed = if (seconds > 0) (current.distanceMeters / 1000f) / (seconds / 3600f) else 0f
                        _session.value = current.copy(
                            elapsedSeconds = seconds,
                            paceSecondsPerKm = pace,
                            currentSpeedKmh = speed.coerceIn(0f, 60f)
                        )
                        checkPaceAlert(pace, seconds)
                        if (seconds % 15 == 0) updateNotification()
                    }
                }
            }
        }
    }

    private fun checkPaceAlert(currentPaceSeconds: Long, elapsedSeconds: Int) {
        if (!voiceEnabled || !isPaceAlertEnabled || targetPaceSecondsPerKm <= 0L) return
        // Warmup period: allow 30 seconds for pace to stabilize and require at least 50m covered
        if (elapsedSeconds < 30 || _session.value.distanceMeters < 50f) return
        if (_session.value.isPaused) return
        if (currentPaceSeconds <= 0L || currentPaceSeconds > 3600L) return

        // In running, higher seconds/km indicates a slower pace.
        // Alert when pace drops below the target threshold (i.e. slower than target threshold)
        if (currentPaceSeconds > targetPaceSecondsPerKm) {
            val now = System.currentTimeMillis()
            if (now - lastPaceAlertTimestamp >= 30_000L) {
                lastPaceAlertTimestamp = now
                voice.narratePaceAlert(currentPaceSeconds, targetPaceSecondsPerKm)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (!hasLocationPermission() || locationCallback != null) return
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(2f)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    val current = _session.value
                    if (current.isRunning && current.isPaused && isAutoPaused) {
                        // While auto-paused, detect if significant movement resumes (> 2.5m displacement or speed > 0.8 m/s)
                        val last = current.routePoints.lastOrNull()
                        val movedMeters = if (last == null) 0f else FloatArray(1).also {
                            android.location.Location.distanceBetween(last.first, last.second, location.latitude, location.longitude, it)
                        }[0]
                        val hasSpeed = location.hasSpeed() && location.speed >= 0.8f
                        if (movedMeters >= 2.5f || hasSpeed) {
                            autoResumeRun()
                            addCoordinate(location.latitude, location.longitude)
                        }
                    } else {
                        addCoordinate(location.latitude, location.longitude)
                    }
                }
            }
        }
        locationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { locationClient.removeLocationUpdates(it) }
        locationCallback = null
    }

    private fun addCoordinate(latitude: Double, longitude: Double) {
        val current = _session.value
        if (!current.isRunning || current.isPaused) return
        val last = current.routePoints.lastOrNull()
        val addedMeters = if (last == null) 0f else FloatArray(1).also {
            android.location.Location.distanceBetween(last.first, last.second, latitude, longitude, it)
        }[0]
        if (addedMeters <= 1.5f && current.routePoints.isNotEmpty()) return

        // Significant movement detected!
        lastMovementTimestamp = System.currentTimeMillis()

        val distance = current.distanceMeters + addedMeters
        val pace = calculatePace(distance, current.elapsedSeconds)
        val speed = if (current.elapsedSeconds > 0) (distance / 1000f) / (current.elapsedSeconds / 3600f) else 0f
        _session.value = current.copy(
            distanceMeters = distance,
            stepsCount = current.stepsCount + (addedMeters * 1.35f).toInt().coerceAtLeast(0),
            caloriesBurned = distance * 0.065f,
            currentSpeedKmh = speed.coerceIn(0f, 60f),
            paceSecondsPerKm = pace,
            routePoints = current.routePoints + (latitude to longitude)
        )
        maybeNarrateMilestone(distance, current.elapsedSeconds, pace)
    }

    private fun maybeNarrateMilestone(distance: Float, seconds: Int, pace: Long) {
        if (!voiceEnabled) return
        val milestone = (distance / narrationIntervalMeters).toInt() * narrationIntervalMeters
        if (milestone < narrationIntervalMeters || milestone <= lastNarratedDistanceMeters) return
        lastNarratedDistanceMeters = milestone
        if (narrationIntervalMeters >= 1000f && milestone % 1000f == 0f) {
            voice.narrateMilestone((milestone / 1000f).toInt(), seconds.toLong(), pace)
        } else {
            voice.narrateDistanceMilestone(milestone, seconds.toLong(), pace)
        }
        updateNotification()
    }

    private fun calculatePace(distanceMeters: Float, seconds: Int): Long {
        if (distanceMeters <= 20f || seconds <= 0) return 0L
        return (seconds.toDouble() / (distanceMeters / 1000.0)).toLong().coerceIn(60, 3600)
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Corrida em andamento", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Mantém GPS e avisos de voz ativos durante a corrida"
                setSound(null, null)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): android.app.Notification {
        val state = _session.value
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val distance = String.format(java.util.Locale("pt", "BR"), "%.2f km", state.distanceMeters / 1000f)
        val time = "%02d:%02d:%02d".format(state.elapsedSeconds / 3600, (state.elapsedSeconds % 3600) / 60, state.elapsedSeconds % 60)
        val title = when {
            state.isAutoPaused -> "Corrida em pausa automática (parado)"
            state.isPaused -> "Corrida pausada"
            else -> "Corrida em andamento"
        }
        val targetInfo = if (isPaceAlertEnabled && targetPaceSecondsPerKm > 0L) {
            " • Meta: %02d:%02d/km".format(targetPaceSecondsPerKm / 60, targetPaceSecondsPerKm % 60)
        } else ""
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText("$distance • $time$targetInfo • GPS e voz ativos")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun updateNotification() {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        stopLocationUpdates()
        timerJob?.cancel()
        voice.shutdown()
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "active_running_session"
        private const val NOTIFICATION_ID = 2107
        private const val ACTION_START = "com.example.running.START"
        private const val ACTION_PAUSE = "com.example.running.PAUSE"
        private const val ACTION_RESUME = "com.example.running.RESUME"
        private const val ACTION_STOP = "com.example.running.STOP"
        private const val ACTION_CONFIGURE_VOICE = "com.example.running.CONFIGURE_VOICE"
        private const val EXTRA_LATITUDE = "latitude"
        private const val EXTRA_LONGITUDE = "longitude"
        private const val EXTRA_VOICE_ENABLED = "voice_enabled"
        private const val EXTRA_NARRATION_INTERVAL = "narration_interval"
        private const val EXTRA_TARGET_PACE = "target_pace_seconds"
        private const val EXTRA_PACE_ALERT_ENABLED = "pace_alert_enabled"

        private val _session = MutableStateFlow(ActiveGpsSession())
        val session: StateFlow<ActiveGpsSession> = _session.asStateFlow()

        fun start(
            context: Context,
            latitude: Double?,
            longitude: Double?,
            voiceEnabled: Boolean,
            intervalMeters: Float,
            targetPaceSecondsPerKm: Long = 0L,
            isPaceAlertEnabled: Boolean = false
        ) {
            val intent = Intent(context, RunningForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LATITUDE, latitude ?: Double.NaN)
                putExtra(EXTRA_LONGITUDE, longitude ?: Double.NaN)
                putExtra(EXTRA_VOICE_ENABLED, voiceEnabled)
                putExtra(EXTRA_NARRATION_INTERVAL, intervalMeters)
                putExtra(EXTRA_TARGET_PACE, targetPaceSecondsPerKm)
                putExtra(EXTRA_PACE_ALERT_ENABLED, isPaceAlertEnabled)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) = send(context, ACTION_PAUSE)
        fun resume(context: Context) = send(context, ACTION_RESUME)
        fun stop(context: Context) = send(context, ACTION_STOP)

        fun configureVoice(
            context: Context,
            enabled: Boolean,
            intervalMeters: Float,
            targetPaceSecondsPerKm: Long = 0L,
            isPaceAlertEnabled: Boolean = false
        ) {
            context.startService(Intent(context, RunningForegroundService::class.java).apply {
                action = ACTION_CONFIGURE_VOICE
                putExtra(EXTRA_VOICE_ENABLED, enabled)
                putExtra(EXTRA_NARRATION_INTERVAL, intervalMeters)
                putExtra(EXTRA_TARGET_PACE, targetPaceSecondsPerKm)
                putExtra(EXTRA_PACE_ALERT_ENABLED, isPaceAlertEnabled)
            })
        }

        private fun send(context: Context, actionName: String) {
            context.startService(Intent(context, RunningForegroundService::class.java).setAction(actionName))
        }
    }
}
