package com.example.ui

import android.app.Application
import android.location.Location
import androidx.lifecycle.viewModelScope
import com.example.data.model.CardioLog
import com.example.data.model.GpsRouteLog
import com.example.service.RunningForegroundService
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CardioViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    fun addCardioLog(log: CardioLog) {
        viewModelScope.launch {
            val datedLog = log.copy(dateMillis = shared.selectedDateWithCurrentTime())
            val insertedId = repository.addCardioLog(datedLog)
            firestoreSync.syncCardioLogToCloud(datedLog.copy(id = insertedId), userProfile.value.firebaseUid)
            showToast("Log de cardio salvo com sucesso!")
        }
    }

    fun startGpsSession(
        initialLat: Double? = null,
        initialLng: Double? = null,
        voiceEnabled: Boolean = true,
        narrationIntervalMeters: Float = 1000f,
        targetPaceSecondsPerKm: Long = 0L,
        isPaceAlertEnabled: Boolean = false
    ) {
        if (activeGpsSession.value.isRunning && !activeGpsSession.value.isPaused) return
        RunningForegroundService.start(
            getApplication(),
            initialLat,
            initialLng,
            voiceEnabled,
            narrationIntervalMeters,
            targetPaceSecondsPerKm,
            isPaceAlertEnabled
        )
        showToast("Rastreamento de corrida iniciado!")
    }

    fun pauseGpsSession() {
        RunningForegroundService.pause(getApplication())
        showToast("Corrida pausada.")
    }

    fun resumeGpsSession() {
        RunningForegroundService.resume(getApplication())
        showToast("Corrida retomada!")
    }

    fun addGpsCoordinate(latitude: Double, longitude: Double) {
        val current = activeGpsSession.value
        if (!current.isRunning || current.isPaused) return
        val lastPoint = current.routePoints.lastOrNull()
        val addedMeters = if (lastPoint != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                lastPoint.first, lastPoint.second,
                latitude, longitude,
                results
            )
            results[0]
        } else {
            0f
        }
        if (addedMeters > 1.5f || current.routePoints.isEmpty()) {
            val newPoints = current.routePoints + Pair(latitude, longitude)
            val newDist = current.distanceMeters + addedMeters
            val addedSteps = (addedMeters * 1.35f).toInt().coerceAtLeast(0)
            val newSteps = current.stepsCount + addedSteps
            val calories = newDist * 0.065f
            val paceSeconds = if (newDist > 20f && current.elapsedSeconds > 0) {
                ((current.elapsedSeconds.toDouble() / (newDist / 1000.0)).toLong()).coerceIn(60, 3600)
            } else 0L
            val speed = if (current.elapsedSeconds > 0 && newDist > 0) {
                ((newDist / 1000f) / (current.elapsedSeconds / 3600f)).coerceIn(0f, 60f)
            } else 0f

            shared.updateActiveGpsSession(
                current.copy(
                    distanceMeters = newDist,
                    stepsCount = newSteps,
                    caloriesBurned = calories,
                    currentSpeedKmh = speed,
                    paceSecondsPerKm = paceSeconds,
                    routePoints = newPoints
                )
            )
        }
    }

    fun stopAndSaveGpsSession(notes: String = "", routeName: String = "Corrida ao Ar Livre") {
        shared.gpsTimerJob?.cancel()
        shared.gpsTimerJob = null
        val session = activeGpsSession.value
        RunningForegroundService.stop(getApplication())
        if (session.elapsedSeconds < 2 && session.distanceMeters < 5f) {
            shared.updateActiveGpsSession(ActiveGpsSession())
            showToast("Sessão muito curta descartada.")
            return
        }

        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val now = System.currentTimeMillis()
            val dateLabel = dateFormat.format(Date(now))
            val pace = if (session.distanceMeters > 0) {
                (session.elapsedSeconds / 60.0f) / (session.distanceMeters / 1000.0f)
            } else 0f

            val coordsStr = session.routePoints.joinToString(";") { "${it.first},${it.second}" }

            val log = GpsRouteLog(
                routeName = routeName,
                activityType = "Corrida",
                dateMillis = now,
                dateDisplay = dateLabel,
                distanceMeters = session.distanceMeters,
                durationSeconds = session.elapsedSeconds,
                stepsCount = session.stepsCount,
                caloriesBurned = session.caloriesBurned,
                averagePaceMinPerKm = pace,
                routePointsJson = coordsStr,
                notes = notes
            )
            val insertedId = repository.insertGpsRouteLog(log)
            firestoreSync.syncGpsRouteLogToCloud(log.copy(id = insertedId), userProfile.value.firebaseUid)
            shared.updateActiveGpsSession(ActiveGpsSession())
            val kmText = String.format(Locale.getDefault(), "%.2f", log.distanceMeters / 1000f)
            showToast("Corrida de $kmText km salva com sucesso!")
        }
    }

    fun discardGpsSession() {
        shared.gpsTimerJob?.cancel()
        shared.gpsTimerJob = null
        shared.updateActiveGpsSession(ActiveGpsSession())
        RunningForegroundService.stop(getApplication())
        showToast("Sessão GPS cancelada.")
    }

    fun configureBackgroundRunningVoice(
        enabled: Boolean,
        intervalMeters: Float,
        targetPaceSecondsPerKm: Long = 0L,
        isPaceAlertEnabled: Boolean = false
    ) {
        if (activeGpsSession.value.isRunning) {
            RunningForegroundService.configureVoice(
                getApplication(),
                enabled,
                intervalMeters,
                targetPaceSecondsPerKm,
                isPaceAlertEnabled
            )
        }
    }

    fun deleteGpsRoute(id: Long) {
        viewModelScope.launch {
            repository.deleteGpsRouteLog(id)
            showToast("Registro de percurso excluído.")
        }
    }
}
