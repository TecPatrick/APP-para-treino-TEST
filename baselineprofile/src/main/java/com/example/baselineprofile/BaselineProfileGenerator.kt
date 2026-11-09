package com.example.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a Baseline Profile covering critical user journeys:
 * 1. Cold Startup (MainApplication, MainActivity, VirtusSharedState)
 * 2. Guest / Onboarding entry and Dashboard rendering (DashboardScreen)
 * 3. Workout navigation and routine list rendering (WorkoutScreen)
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() {
        rule.collect(
            packageName = TARGET_PACKAGE,
            includeInStartupProfile = true
        ) {
            pressHome()
            startActivityAndWait()

            // Enter as guest if on the initial Auth & Onboarding screen
            val guestButton = device.findObject(By.res("guest_login_btn"))
                ?: device.findObject(By.textContains("Explorar como Visitante"))
            if (guestButton != null) {
                guestButton.click()
                device.waitForIdle()
            }

            // Wait for Dashboard bottom navigation to be visible
            device.wait(Until.hasObject(By.text("Treinos")), 5_000)

            // Navigate to Workout screen to include WorkoutScreen and ExerciseGifMapper in profile
            val workoutTab = device.findObject(By.res("nav_workout"))
                ?: device.findObject(By.text("Treinos"))
            if (workoutTab != null) {
                workoutTab.click()
                device.waitForIdle()
            }

            // Return to Dashboard
            val dashboardTab = device.findObject(By.res("nav_dashboard"))
                ?: device.findObject(By.text("Início"))
            if (dashboardTab != null) {
                dashboardTab.click()
                device.waitForIdle()
            }
        }
    }

    companion object {
        private const val TARGET_PACKAGE = "com.aistudio.growthdiet.kxmfzq"
    }
}
