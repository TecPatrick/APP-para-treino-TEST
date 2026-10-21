package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * High-performance, natural-sounding Text-to-Speech service for workout tracking.
 * Formats distance, elapsed time, and pace (ritmo) in Brazilian Portuguese.
 */
class WorkoutTtsService(private val context: Context) : TextToSpeech.OnInitListener, VoiceProvider {

    override val providerId: String = "android_tts"

    private val tag = "WorkoutTtsService"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastNarratedText = MutableStateFlow<String?>(null)
    override val lastNarratedText: StateFlow<String?> = _lastNarratedText.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("pt", "BR"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(tag, "Portuguese (Brazil) language missing/not supported, falling back to default locale.")
                tts?.language = Locale.getDefault()
            }
            tts?.setPitch(1.0f)
            tts?.setSpeechRate(1.03f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    _isSpeaking.value = false
                }
            })

            isInitialized = true
            Log.d(tag, "Workout TTS initialized successfully.")
        } else {
            Log.e(tag, "TTS initialization failed with status: $status")
            isInitialized = false
        }
    }

    /**
     * Speaks the specified text with QUEUE_FLUSH by default.
     */
    override fun speak(text: String, flush: Boolean) {
        _lastNarratedText.value = text
        if (!isInitialized || tts == null) {
            Log.w(tag, "TTS not initialized yet. Skipping phrase: $text")
            return
        }

        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = UUID.randomUUID().toString()
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }

        tts?.speak(text, queueMode, params, utteranceId)
    }

    /**
     * Narrates live workout metrics: Distance, Time, and Pace.
     */
    override fun narrateMetrics(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long) {
        val speech = formatFullWorkoutSpeech(distanceMeters, elapsedSeconds, paceSecondsPerKm)
        speak(speech)
    }

    /**
     * Narrates a split milestone (e.g. Kilometer 1, 2, 3...)
     */
    override fun narrateMilestone(kmNumber: Int, elapsedSeconds: Long, paceSecondsPerKm: Long) {
        val speech = formatMilestoneSpeech(kmNumber, elapsedSeconds, paceSecondsPerKm)
        speak(speech)
    }

    /**
     * Narrates milestone for any interval in meters (e.g. 500m, 1000m, 1500m, 2000m...)
     */
    override fun narrateDistanceMilestone(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long) {
        val speech = formatDistanceMilestoneSpeech(distanceMeters, elapsedSeconds, paceSecondsPerKm)
        speak(speech)
    }

    /**
     * Stop current speech immediately.
     */
    override fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    /**
     * Clean up and release TTS resources.
     */
    override fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e(tag, "Error shutting down TTS", e)
        }
    }

    // --- Portuguese Speech Formatting Helpers ---

    override fun formatDistanceSpeech(distanceMeters: Float): String {
        if (distanceMeters < 50f) {
            return "início da corrida"
        }
        if (distanceMeters < 1000f) {
            val rounded = ((distanceMeters / 50).toInt() * 50).coerceAtLeast(50)
            return "$rounded metros"
        }

        val totalKm = (distanceMeters / 1000f)
        val kmInt = totalKm.toInt()
        val remainingMeters = (distanceMeters % 1000f).toInt()

        val kmWord = if (kmInt == 1) "1 quilômetro" else "$kmInt quilômetros"

        return if (remainingMeters in 50..950) {
            val roundedMeters = (remainingMeters / 50) * 50
            "$kmWord e $roundedMeters metros"
        } else {
            kmWord
        }
    }

    override fun formatTimeSpeech(elapsedSeconds: Long): String {
        if (elapsedSeconds < 60) {
            return "$elapsedSeconds segundos"
        }

        val hours = elapsedSeconds / 3600
        val minutes = (elapsedSeconds % 3600) / 60
        val seconds = elapsedSeconds % 60

        return if (hours > 0) {
            val hourWord = if (hours == 1L) "1 hora" else "$hours horas"
            if (minutes > 0) "$hourWord e $minutes minutos" else hourWord
        } else {
            val minWord = if (minutes == 1L) "1 minuto" else "$minutes minutos"
            if (seconds > 0) {
                val secWord = if (seconds == 1L) "1 segundo" else "$seconds segundos"
                "$minWord e $secWord"
            } else {
                minWord
            }
        }
    }

    override fun formatPaceSpeech(paceSecondsPerKm: Long): String {
        if (paceSecondsPerKm <= 0 || paceSecondsPerKm > 3600) {
            return "ritmo em cálculo"
        }

        val minutes = paceSecondsPerKm / 60
        val seconds = paceSecondsPerKm % 60

        val minWord = if (minutes == 1L) "1 minuto" else "$minutes minutos"
        return if (seconds > 0) {
            val secWord = if (seconds == 1L) "1 segundo" else "$seconds segundos"
            "ritmo médio de $minWord e $secWord por quilômetro"
        } else {
            "ritmo médio de $minWord por quilômetro"
        }
    }

    override fun formatFullWorkoutSpeech(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long): String {
        val distText = formatDistanceSpeech(distanceMeters)
        val timeText = formatTimeSpeech(elapsedSeconds)
        val paceText = formatPaceSpeech(paceSecondsPerKm)

        return "Atenção: Distância percorrida, $distText. Tempo decorrido, $timeText. $paceText."
    }

    override fun formatMilestoneSpeech(kmNumber: Int, elapsedSeconds: Long, paceSecondsPerKm: Long): String {
        val kmWord = if (kmNumber == 1) "Primeiro quilômetro" else "Quilômetro $kmNumber"
        val timeText = formatTimeSpeech(elapsedSeconds)
        val paceText = formatPaceSpeech(paceSecondsPerKm)

        return "$kmWord completado! Tempo total: $timeText. $paceText."
    }

    override fun formatDistanceMilestoneSpeech(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long): String {
        val distText = formatDistanceSpeech(distanceMeters)
        val timeText = formatTimeSpeech(elapsedSeconds)
        val paceText = formatPaceSpeech(paceSecondsPerKm)

        return "$distText completados! Tempo total: $timeText. $paceText."
    }

    override fun narratePaceAlert(currentPaceSecondsPerKm: Long, targetPaceSecondsPerKm: Long) {
        val speech = formatPaceAlertSpeech(currentPaceSecondsPerKm, targetPaceSecondsPerKm)
        speak(speech)
    }

    override fun formatPaceAlertSpeech(currentPaceSecondsPerKm: Long, targetPaceSecondsPerKm: Long): String {
        val curMinutes = currentPaceSecondsPerKm / 60
        val curSeconds = currentPaceSecondsPerKm % 60
        val tarMinutes = targetPaceSecondsPerKm / 60
        val tarSeconds = targetPaceSecondsPerKm % 60

        val curText = if (curSeconds > 0) "$curMinutes minutos e $curSeconds segundos" else "$curMinutes minutos"
        val tarText = if (tarSeconds > 0) "$tarMinutes minutos e $tarSeconds segundos" else "$tarMinutes minutos"

        return "Atenção: ritmo abaixo da meta. Seu ritmo atual é de $curText por quilômetro, abaixo do limite de $tarText. Acelere o passo!"
    }
}
