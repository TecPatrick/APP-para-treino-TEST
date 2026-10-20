package com.example.util

import android.content.Context
import com.example.service.WorkoutTtsService

/**
 * TextToSpeechManager delegating to WorkoutTtsService for full TTS features
 * and workout metric narration (Distance, Time, Pace).
 */
class TextToSpeechManager(context: Context) {
    val service = WorkoutTtsService(context)

    fun speak(text: String) {
        service.speak(text)
    }

    fun narrateWorkoutMetrics(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long) {
        service.narrateMetrics(distanceMeters, elapsedSeconds, paceSecondsPerKm)
    }

    fun narrateMilestone(kmNumber: Int, elapsedSeconds: Long, paceSecondsPerKm: Long) {
        service.narrateMilestone(kmNumber, elapsedSeconds, paceSecondsPerKm)
    }

    fun stop() {
        service.stop()
    }

    fun shutdown() {
        service.shutdown()
    }
}
