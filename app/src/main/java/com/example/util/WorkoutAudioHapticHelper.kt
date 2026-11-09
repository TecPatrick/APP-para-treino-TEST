package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R

/**
 * Dedicated sound and haptic feedback manager for workout sessions.
 * Uses high-performance low-latency SoundPool with custom crafted audio effects
 * and graceful fallback to ToneGenerator.
 */
object WorkoutAudioHapticHelper {

    private var soundPool: SoundPool? = null
    private var toneGenerator: ToneGenerator? = null

    private var soundRestStartId: Int = 0
    private var soundRestFinishId: Int = 0
    private var soundRestTickId: Int = 0
    private var isSoundPoolLoaded: Boolean = false

    fun init(context: Context) {
        if (soundPool != null) return
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build().apply {
                    setOnLoadCompleteListener { _, _, status ->
                        if (status == 0) {
                            isSoundPoolLoaded = true
                        }
                    }
                    soundRestStartId = load(context.applicationContext, R.raw.sound_rest_start, 1)
                    soundRestFinishId = load(context.applicationContext, R.raw.sound_rest_finish, 1)
                    soundRestTickId = load(context.applicationContext, R.raw.sound_rest_tick, 1)
                }
        } catch (e: Exception) {
            soundPool = null
        }

        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    /**
     * Efeito sonoro e tátil para o INÍCIO do cronômetro de descanso
     */
    fun playRestStarted(context: Context, soundEnabled: Boolean, hapticEnabled: Boolean) {
        init(context)
        if (soundEnabled) {
            var played = false
            try {
                if (soundPool != null && soundRestStartId != 0) {
                    val streamId = soundPool?.play(soundRestStartId, 0.9f, 0.9f, 1, 0, 1.0f) ?: 0
                    played = streamId != 0
                }
            } catch (e: Exception) {
                played = false
            }

            if (!played) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 200)
                } catch (e: Exception) {
                    AppLog.e("WorkoutAudioHaptic", "Erro ao tocar som de início de descanso", e)
                }
            }
        }

        if (hapticEnabled) {
            vibrate(context, longArrayOf(0, 80), -1)
        }
    }

    /**
     * Efeito sonoro e tátil sutil durante a contagem regressiva final (3, 2, 1)
     */
    fun playCountdownTick(context: Context, soundEnabled: Boolean, hapticEnabled: Boolean) {
        init(context)
        if (soundEnabled) {
            var played = false
            try {
                if (soundPool != null && soundRestTickId != 0) {
                    val streamId = soundPool?.play(soundRestTickId, 0.7f, 0.7f, 1, 0, 1.0f) ?: 0
                    played = streamId != 0
                }
            } catch (e: Exception) {
                played = false
            }

            if (!played) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
                } catch (e: Exception) {
                    AppLog.e("WorkoutAudioHaptic", "Erro ao tocar som de contagem regressiva", e)
                }
            }
        }

        if (hapticEnabled) {
            vibrate(context, longArrayOf(0, 50), -1)
        }
    }

    /**
     * Efeito sonoro e tátil para o FIM do cronômetro de descanso (0s)
     */
    fun playRestFinished(context: Context, soundEnabled: Boolean, hapticEnabled: Boolean) {
        init(context)
        if (soundEnabled) {
            var played = false
            try {
                if (soundPool != null && soundRestFinishId != 0) {
                    val streamId = soundPool?.play(soundRestFinishId, 1.0f, 1.0f, 1, 0, 1.0f) ?: 0
                    played = streamId != 0
                }
            } catch (e: Exception) {
                played = false
            }

            if (!played) {
                try {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 350)
                } catch (e: Exception) {
                    AppLog.e("WorkoutAudioHaptic", "Erro ao tocar som de fim de descanso", e)
                }
            }
        }

        if (hapticEnabled) {
            vibrate(context, longArrayOf(0, 300, 150, 400), -1)
        }
    }

    /**
     * Toque háptico rápido de confirmação
     */
    fun playSetLoggedTick(context: Context, hapticEnabled: Boolean) {
        if (hapticEnabled) {
            vibrate(context, longArrayOf(0, 40), -1)
        }
    }

    private fun vibrate(context: Context, pattern: LongArray, repeat: Int) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(pattern, repeat)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(pattern, repeat)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, repeat)
                }
            }
        } catch (e: Exception) {
            AppLog.e("WorkoutAudioHaptic", "Erro ao acionar vibração", e)
        }
    }
}
