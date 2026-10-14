package com.example.ui

import android.annotation.SuppressLint
import android.app.Application
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.service.WorkoutTtsService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class ActiveWorkoutGpsState(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val distanceMeters: Float = 0f,
    val caloriesBurned: Float = 0f,
    val stepsCount: Int = 0,
    val routePoints: List<Pair<Double, Double>> = emptyList(),
    val currentSpeedKmh: Float = 0f,
    val paceSecondsPerKm: Long = 0L,
    val formattedPace: String = "--'--\" /km",
    val isTtsEnabled: Boolean = true,
    val narrationIntervalMeters: Float = 1000f,
    val lastNarratedDistanceMeters: Float = 0f,
    val lastNarratedKm: Int = 0,
    val lastNarratedMessage: String? = null,
    val isSpeaking: Boolean = false
)

class WorkoutTrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ActiveWorkoutGpsState())
    val uiState: StateFlow<ActiveWorkoutGpsState> = _uiState.asStateFlow()

    private val ttsService = WorkoutTtsService(application)

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private var locationCallback: LocationCallback? = null
    private var timerJob: Job? = null

    init {
        // Collect TTS status and update UI state
        viewModelScope.launch {
            ttsService.isSpeaking.collect { speaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = speaking)
            }
        }
        viewModelScope.launch {
            ttsService.lastNarratedText.collect { text ->
                if (text != null) {
                    _uiState.value = _uiState.value.copy(lastNarratedMessage = text)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startWorkout(initialLat: Double = -23.550520, initialLng: Double = -46.633308) {
        if (_uiState.value.isRunning && !_uiState.value.isPaused) return

        val initialPoints = listOf(Pair(initialLat, initialLng))
        _uiState.value = ActiveWorkoutGpsState(
            isRunning = true,
            isPaused = false,
            elapsedSeconds = 0L,
            distanceMeters = 0f,
            caloriesBurned = 0f,
            stepsCount = 0,
            routePoints = initialPoints,
            currentSpeedKmh = 0f,
            paceSecondsPerKm = 0L,
            formattedPace = "--'--\" /km",
            isTtsEnabled = _uiState.value.isTtsEnabled,
            lastNarratedKm = 0,
            lastNarratedMessage = "Treino iniciado."
        )

        if (_uiState.value.isTtsEnabled) {
            ttsService.speak("Treino de corrida iniciado. Boa corrida!")
        }

        startTimer()
        startLocationUpdates()
    }

    fun pauseWorkout() {
        if (!_uiState.value.isRunning || _uiState.value.isPaused) return
        _uiState.value = _uiState.value.copy(isPaused = true)
        stopLocationUpdates()

        if (_uiState.value.isTtsEnabled) {
            ttsService.speak("Treino pausado.")
        }
    }

    @SuppressLint("MissingPermission")
    fun resumeWorkout() {
        if (!_uiState.value.isRunning || !_uiState.value.isPaused) return
        _uiState.value = _uiState.value.copy(isPaused = false)

        if (_uiState.value.isTtsEnabled) {
            ttsService.speak("Treino retomado. Vamos lá!")
        }

        startLocationUpdates()
    }

    fun stopWorkout() {
        stopLocationUpdates()
        timerJob?.cancel()
        timerJob = null

        val finishedState = _uiState.value
        if (finishedState.isTtsEnabled && finishedState.distanceMeters > 20f) {
            val dist = ttsService.formatDistanceSpeech(finishedState.distanceMeters)
            val time = ttsService.formatTimeSpeech(finishedState.elapsedSeconds)
            val pace = ttsService.formatPaceSpeech(finishedState.paceSecondsPerKm)
            ttsService.speak("Treino finalizado! Você percorreu $dist em $time, com $pace. Parabéns pelo esforço!")
        }

        _uiState.value = ActiveWorkoutGpsState(isTtsEnabled = finishedState.isTtsEnabled)
    }

    /**
     * Toggles Text-to-Speech narration on or off.
     */
    fun toggleTts() {
        val newState = !_uiState.value.isTtsEnabled
        _uiState.value = _uiState.value.copy(isTtsEnabled = newState)
        if (newState) {
            ttsService.speak("Treinador por voz ativado.")
        } else {
            ttsService.stop()
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isTtsEnabled = enabled)
        if (!enabled) {
            ttsService.stop()
        }
    }

    fun setNarrationIntervalMeters(intervalMeters: Float) {
        val validInterval = intervalMeters.coerceIn(100f, 10000f)
        _uiState.value = _uiState.value.copy(narrationIntervalMeters = validInterval)
        val intervalLabel = if (validInterval >= 1000f) {
            "${(validInterval / 1000f).toInt()} quilômetro${if (validInterval >= 2000f) "s" else ""}"
        } else {
            "${validInterval.toInt()} metros"
        }
        ttsService.speak("Intervalo de voz ajustado para a cada $intervalLabel.")
    }

    /**
     * Immediately narrates the current Distance, Elapsed Time, and Pace.
     */
    fun speakCurrentMetrics() {
        val current = _uiState.value
        val paceSec = calculatePaceSecondsPerKm(current.distanceMeters, current.elapsedSeconds)
        ttsService.narrateMetrics(current.distanceMeters, current.elapsedSeconds, paceSec)
    }

    /**
     * Custom speech announcement helper.
     */
    fun speak(text: String) {
        ttsService.speak(text)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
                .setMinUpdateDistanceMeters(2f)
                .setWaitForAccurateLocation(false)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {
                        addGpsCoordinate(location.latitude, location.longitude)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
        } catch (_: SecurityException) {
        } catch (_: Exception) {}
    }

    private fun stopLocationUpdates() {
        locationCallback?.let {
            try {
                fusedLocationClient.removeLocationUpdates(it)
            } catch (_: Exception) {}
        }
        locationCallback = null
    }

    fun addGpsCoordinate(lat: Double, lng: Double) {
        val current = _uiState.value
        if (!current.isRunning || current.isPaused) return

        val updatedPoints = current.routePoints + Pair(lat, lng)
        var addedMeters = 0f

        if (current.routePoints.isNotEmpty()) {
            val last = current.routePoints.last()
            addedMeters = calculateDistanceMeters(last.first, last.second, lat, lng).toFloat()
        }

        if (addedMeters > 0.5f || current.routePoints.size <= 1) {
            val newTotalDistance = current.distanceMeters + addedMeters
            val newSteps = current.stepsCount + (addedMeters * 1.35f).toInt().coerceAtLeast(1)
            val newCalories = (newTotalDistance / 1000f) * 65f
            val speed = if (current.elapsedSeconds > 0) {
                (newTotalDistance / 1000f) / (current.elapsedSeconds / 3600f)
            } else 0f

            val paceSeconds = calculatePaceSecondsPerKm(newTotalDistance, current.elapsedSeconds)
            val formattedPace = formatPaceString(paceSeconds)

            // Check milestone announcement (e.g. at each 500m, 1km, 2km configurable)
            val interval = current.narrationIntervalMeters.coerceAtLeast(100f)
            val currentMilestoneIndex = (newTotalDistance / interval).toInt()
            val lastMilestoneIndex = (current.lastNarratedDistanceMeters / interval).toInt()
            var newLastNarratedDistance = current.lastNarratedDistanceMeters
            var newLastNarratedKm = current.lastNarratedKm

            if (current.isTtsEnabled && currentMilestoneIndex > lastMilestoneIndex && newTotalDistance >= interval) {
                newLastNarratedDistance = currentMilestoneIndex * interval
                newLastNarratedKm = (newTotalDistance / 1000f).toInt()
                if (interval >= 1000f && (newTotalDistance % 1000f) < 80f) {
                    val km = (newTotalDistance / 1000f).toInt()
                    ttsService.narrateMilestone(km, current.elapsedSeconds, paceSeconds)
                } else {
                    ttsService.narrateDistanceMilestone(newTotalDistance, current.elapsedSeconds, paceSeconds)
                }
            }

            _uiState.value = current.copy(
                routePoints = updatedPoints,
                distanceMeters = newTotalDistance,
                stepsCount = newSteps,
                caloriesBurned = newCalories,
                currentSpeedKmh = speed.coerceAtMost(60f),
                paceSecondsPerKm = paceSeconds,
                formattedPace = formattedPace,
                lastNarratedDistanceMeters = newLastNarratedDistance,
                lastNarratedKm = newLastNarratedKm
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning) {
                delay(1000L)
                val current = _uiState.value
                if (!current.isPaused) {
                    val newSeconds = current.elapsedSeconds + 1
                    val paceSeconds = calculatePaceSecondsPerKm(current.distanceMeters, newSeconds)
                    val formattedPace = formatPaceString(paceSeconds)

                    _uiState.value = current.copy(
                        elapsedSeconds = newSeconds,
                        paceSecondsPerKm = paceSeconds,
                        formattedPace = formattedPace
                    )
                }
            }
        }
    }

    private fun calculatePaceSecondsPerKm(distanceMeters: Float, elapsedSeconds: Long): Long {
        if (distanceMeters < 15f || elapsedSeconds <= 0) return 0L
        val paceSec = (elapsedSeconds.toDouble() / (distanceMeters / 1000.0)).toLong()
        return paceSec.coerceIn(60, 3600)
    }

    private fun formatPaceString(paceSeconds: Long): String {
        if (paceSeconds <= 0) return "--'--\" /km"
        val min = paceSeconds / 60
        val sec = paceSeconds % 60
        return String.format(Locale.getDefault(), "%d'%02d\" /km", min, sec)
    }

    private fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
        timerJob?.cancel()
        ttsService.shutdown()
    }
}
