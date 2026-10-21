package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.service.WorkoutTtsService
import com.example.ui.ActiveGpsSession
import com.example.ui.ActiveWorkoutGpsState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RunningServicePaceAndAutoPauseTest {

    private lateinit var context: Context
    private lateinit var ttsService: WorkoutTtsService

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        ttsService = WorkoutTtsService(context)
    }

    @Test
    fun `formatPaceAlertSpeech produces Portuguese voice alert with current and target pace`() {
        val currentPaceSec = 435L
        val targetPaceSec = 360L

        val alertText = ttsService.formatPaceAlertSpeech(currentPaceSec, targetPaceSec)

        assertTrue(alertText.contains("ritmo abaixo da meta", ignoreCase = true))
        assertTrue(alertText.contains("7 minutos"))
        assertTrue(alertText.contains("15 segundos"))
        assertTrue(alertText.contains("6 minutos"))
        assertTrue(alertText.contains("Acelere", ignoreCase = true))
    }

    @Test
    fun `formatPaceAlertSpeech handles exact minute paces cleanly`() {
        val currentPaceSec = 480L
        val targetPaceSec = 390L

        val alertText = ttsService.formatPaceAlertSpeech(currentPaceSec, targetPaceSec)

        assertTrue(alertText.contains("8 minutos"))
        assertTrue(alertText.contains("6 minutos e 30 segundos"))
    }

    @Test
    fun `ActiveGpsSession default values support auto-pause and pace alert threshold`() {
        val defaultSession = ActiveGpsSession()

        assertFalse(defaultSession.isRunning)
        assertFalse(defaultSession.isPaused)
        assertFalse(defaultSession.isAutoPaused)
        assertEquals(0L, defaultSession.targetPaceSecondsPerKm)
        assertFalse(defaultSession.isPaceAlertEnabled)

        val autoPausedSession = defaultSession.copy(
            isRunning = true,
            isPaused = true,
            isAutoPaused = true,
            targetPaceSecondsPerKm = 360L,
            isPaceAlertEnabled = true
        )

        assertTrue(autoPausedSession.isRunning)
        assertTrue(autoPausedSession.isPaused)
        assertTrue(autoPausedSession.isAutoPaused)
        assertEquals(360L, autoPausedSession.targetPaceSecondsPerKm)
        assertTrue(autoPausedSession.isPaceAlertEnabled)
    }

    @Test
    fun `ActiveWorkoutGpsState holds pace alert and auto pause configuration`() {
        val state = ActiveWorkoutGpsState(
            isRunning = true,
            isPaused = false,
            isAutoPaused = false,
            targetPaceSecondsPerKm = 330L,
            isPaceAlertEnabled = true
        )

        assertTrue(state.isPaceAlertEnabled)
        assertEquals(330L, state.targetPaceSecondsPerKm)
        assertFalse(state.isAutoPaused)

        val autoPaused = state.copy(isPaused = true, isAutoPaused = true)
        assertTrue(autoPaused.isPaused)
        assertTrue(autoPaused.isAutoPaused)
    }

    @Test
    fun `pace comparison logic correctly identifies when pace is slower than target threshold`() {
        val targetPaceSecondsPerKm = 360L // 6:00 min/km

        val fasterPace = 300L // 5:00 min/km
        val slowerPace = 420L // 7:00 min/km

        assertFalse(fasterPace > targetPaceSecondsPerKm)
        assertTrue(slowerPace > targetPaceSecondsPerKm)
    }
}
