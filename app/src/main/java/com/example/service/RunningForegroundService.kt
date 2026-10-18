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
                intent.getFloatExtra(EXTRA_NARRATION_INTERVAL, 1000f)
            )
            ACTION_PAUSE -> pauseRun()
            ACTION_RESUME -> resumeRun()
            ACTION_CONFIGURE_VOICE -> configureVoice(
                intent.getBooleanExtra(EXTRA_VOICE_ENABLED, true),
                intent.getFloatExtra(EXTRA_NARRATION_INTERVAL, 1000f)
            )
            ACTION_STOP -> stopRun()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRun(lat: Double, lng: Double, ttsEnabled: Boolean, intervalMeters: Float) {
        if (_session.value.isRunning) return
        voiceEnabled = ttsEnabled
        narrationIntervalMeters = intervalMeters.coerceIn(100f, 10_000f)
        lastNarratedDistanceMeters = 0f
        val initialPoints = if (lat.isFinite() && lng.isFinite()) listOf(lat to lng) else emptyList()
        _session.value = ActiveGpsSession(isRunning = true, routePoints = initialPoints)
        startForeground(NOTIFICATION_ID, buildNotification())
        startTimer()
        startLocationUpdates()
        if (voiceEnabled) voice.speak("Treino de corrida iniciado. Boa corrida!")
    }

    private fun pauseRun() {
        if (!_session.value.isRunning || _session.value.isPaused) return
        _session.value = _session.value.copy(isPaused = true, currentSpeedKmh = 0f)
        stopLocationUpdates()
        updateNotification()
        if (voiceEnabled) voice.speak("Treino pausado.")
    }

    private fun resumeRun() {
        if (!_session.value.isRunning || !_session.value.isPaused) return
        _session.value = _session.value.copy(isPaused = false)
        startLocationUpdates()
        updateNotification()
        if (voiceEnabled) voice.speak("Treino retomado. Vamos lá!")
    }

    private fun configureVoice(enabled: Boolean, intervalMeters: Float) {
        voiceEnabled = enabled
        narrationIntervalMeters = intervalMeters.coerceIn(100f, 10_000f)
        if (!enabled) voice.stop()
    }

    private fun stopRun() {
        stopLocationUpdates()
        timerJob?.cancel()
        timerJob = null
        voice.stop()
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
                    val seconds = current.elapsedSeconds + 1
                    val pace = calculatePace(current.distanceMeters, seconds)
                    val speed = if (seconds > 0) (current.distanceMeters / 1000f) / (seconds / 3600f) else 0f
                    _session.value = current.copy(
                        elapsedSeconds = seconds,
                        paceSecondsPerKm = pace,
                        currentSpeedKmh = speed.coerceIn(0f, 60f)
                    )
                    if (seconds % 15 == 0) updateNotification()
                }
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
                result.locations.forEach { addCoordinate(it.latitude, it.longitude) }
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
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(if (state.isPaused) "Corrida pausada" else "Corrida em andamento")
            .setContentText("$distance • $time • GPS e voz ativos")
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

        private val _session = MutableStateFlow(ActiveGpsSession())
        val session: StateFlow<ActiveGpsSession> = _session.asStateFlow()

        fun start(context: Context, latitude: Double?, longitude: Double?, voiceEnabled: Boolean, intervalMeters: Float) {
            val intent = Intent(context, RunningForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_LATITUDE, latitude ?: Double.NaN)
                putExtra(EXTRA_LONGITUDE, longitude ?: Double.NaN)
                putExtra(EXTRA_VOICE_ENABLED, voiceEnabled)
                putExtra(EXTRA_NARRATION_INTERVAL, intervalMeters)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun pause(context: Context) = send(context, ACTION_PAUSE)
        fun resume(context: Context) = send(context, ACTION_RESUME)
        fun stop(context: Context) = send(context, ACTION_STOP)

        fun configureVoice(context: Context, enabled: Boolean, intervalMeters: Float) {
            context.startService(Intent(context, RunningForegroundService::class.java).apply {
                action = ACTION_CONFIGURE_VOICE
                putExtra(EXTRA_VOICE_ENABLED, enabled)
                putExtra(EXTRA_NARRATION_INTERVAL, intervalMeters)
            })
        }

        private fun send(context: Context, actionName: String) {
            context.startService(Intent(context, RunningForegroundService::class.java).setAction(actionName))
        }
    }
}
