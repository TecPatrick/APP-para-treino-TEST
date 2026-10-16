package com.example.service

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

/**
 * Stable boundary between the running coach and the speech engine.
 *
 * Android TTS is the only enabled provider for now. A private VoiceStudio
 * client can implement this contract later without coupling GPS tracking or
 * the running screen to HTTP, API keys, audio caching, or a specific model.
 */
interface VoiceProvider {
    val providerId: String
    val isSpeaking: StateFlow<Boolean>
    val lastNarratedText: StateFlow<String?>

    fun speak(text: String, flush: Boolean = true)
    fun narrateMetrics(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long)
    fun narrateMilestone(kmNumber: Int, elapsedSeconds: Long, paceSecondsPerKm: Long)
    fun narrateDistanceMilestone(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long)
    fun formatDistanceSpeech(distanceMeters: Float): String
    fun formatTimeSpeech(elapsedSeconds: Long): String
    fun formatPaceSpeech(paceSecondsPerKm: Long): String
    fun formatFullWorkoutSpeech(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long): String
    fun formatMilestoneSpeech(kmNumber: Int, elapsedSeconds: Long, paceSecondsPerKm: Long): String
    fun formatDistanceMilestoneSpeech(distanceMeters: Float, elapsedSeconds: Long, paceSecondsPerKm: Long): String
    fun stop()
    fun shutdown()
}

enum class VoiceProviderType {
    ANDROID_TTS,
    VOICESTUDIO
}

data class VoiceProviderSettings(
    val provider: VoiceProviderType = VoiceProviderType.ANDROID_TTS,
    val voiceId: String? = null,
    val speechRate: Float = 1.03f,
    val pitch: Float = 1.0f,
    val voiceStudioBaseUrl: String? = null
)

/**
 * Reserved contract for the future private VoiceStudio integration.
 * Implementations must use HTTPS outside a trusted LAN, authenticate remote
 * calls, cache generated audio, and fall back to Android TTS on any failure.
 */
interface RemoteVoiceProvider : VoiceProvider {
    val fallback: VoiceProvider
    val isAvailable: StateFlow<Boolean>
}

object VoiceProviderFactory {
    /**
     * VoiceStudio deliberately falls back until its authenticated client,
     * audio cache, timeout policy, and user-facing opt-in are implemented.
     */
    fun create(
        context: Context,
        settings: VoiceProviderSettings = VoiceProviderSettings()
    ): VoiceProvider {
        return when (settings.provider) {
            VoiceProviderType.ANDROID_TTS -> WorkoutTtsService(context)
            VoiceProviderType.VOICESTUDIO -> WorkoutTtsService(context)
        }
    }
}
