package com.example

import com.example.data.model.AnimeUniverse
import com.example.data.model.CardioLog
import com.example.data.model.CardioType
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.FoodRecord
import com.example.data.model.GpsRouteLog
import com.example.data.model.MuscleFocusGroup
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import com.example.util.AchievementCatalogAndEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AchievementCatalogAndEngineTest {

    @Test
    fun catalog_includesAllRequestedAnimeUniverses() {
        val universesInCatalog = AchievementCatalogAndEngine.catalog.map { it.animeUniverse }.toSet()
        assertTrue(universesInCatalog.contains(AnimeUniverse.SOLO_LEVELING))
        assertTrue(universesInCatalog.contains(AnimeUniverse.DRAGON_BALL))
        assertTrue(universesInCatalog.contains(AnimeUniverse.BOKU_NO_HERO))
        assertTrue(universesInCatalog.contains(AnimeUniverse.DEMON_SLAYER))
        assertTrue(universesInCatalog.contains(AnimeUniverse.JUJUTSU_KAISEN))
        assertTrue(universesInCatalog.contains(AnimeUniverse.ATTACK_ON_TITAN))
        assertTrue(universesInCatalog.contains(AnimeUniverse.ONE_PUNCH_MAN))
        assertTrue(universesInCatalog.contains(AnimeUniverse.BAKI))
    }

    @Test
    fun evaluateAchievements_unlocksDietConsistencyAndStreakBadges() {
        val profile = UserProfile(targetProteinGrams = 150, targetWaterMl = 3000)
        val dates = listOf(
            "2026-11-01",
            "2026-11-02",
            "2026-11-03",
            "2026-11-04",
            "2026-11-05"
        )
        val foodRecords = dates.mapIndexed { idx, dateStr ->
            FoodRecord(
                id = (idx + 1).toLong(),
                dateString = dateStr,
                foodName = "Peito de Frango",
                amount = 200f,
                calories = 330f,
                protein = 135f,
                carbs = 0f,
                fat = 7f
            )
        }

        val summary = AchievementCatalogAndEngine.evaluateAchievements(
            profile = profile,
            foodRecords = foodRecords,
            intakeHistory = emptyList(),
            workoutLogs = emptyList(),
            workoutRoutines = emptyList(),
            allExercises = emptyList(),
            cardioLogs = emptyList(),
            gpsRouteLogs = emptyList(),
            equippedTitleId = null
        )

        val diet3 = summary.achievements.first { it.definition.id == "diet_days_3" }
        val streak5 = summary.achievements.first { it.definition.id == "diet_streak_5" }
        assertTrue(diet3.isUnlocked)
        assertTrue(streak5.isUnlocked)
        assertEquals(5, streak5.currentValue)
    }

    @Test
    fun evaluateAchievements_unlocksMuscleGroupsAndCardioAndHonorsEquippedTitle() {
        val profile = UserProfile()
        val backRoutine = WorkoutRoutine(
            id = 10L,
            letter = "B",
            title = "Costas e Bíceps",
            targetMuscleGroups = "Costas, Dorsal, Bíceps"
        )
        val backExercise = WorkoutExercise(
            id = 100L,
            routineId = 10L,
            exerciseName = "Puxada Frontal Aberta",
            muscleGroup = "Costas",
            sets = 4
        )
        val logs = (1..6).map { idx ->
            WorkoutLog(
                id = idx.toLong(),
                routineId = 10L,
                routineName = "Costas e Bíceps",
                dateMillis = 1700000000000L + (idx * 86_400_000L),
                durationMinutes = 50,
                totalVolumeKg = 5000f,
                totalSetsCompleted = 16
            )
        }
        val gpsLogs = listOf(
            GpsRouteLog(
                id = 1L,
                dateDisplay = "09/11/2026",
                distanceMeters = 12_500f,
                caloriesBurned = 850f
            ),
            GpsRouteLog(
                id = 2L,
                dateDisplay = "10/11/2026",
                distanceMeters = 5_000f,
                caloriesBurned = 350f
            )
        )
        val cardioLogs = listOf(
            CardioLog(
                id = 1L,
                type = CardioType.TREADMILL,
                durationMinutes = 30,
                caloriesBurned = 320,
                distanceKm = 4.5f
            )
        )

        val summary = AchievementCatalogAndEngine.evaluateAchievements(
            profile = profile,
            foodRecords = emptyList(),
            intakeHistory = listOf(
                DailyIntakeHistory(
                    dateString = "2026-11-09",
                    dateMillis = 1700000000000L,
                    totalCalories = 2400f,
                    totalProtein = 180f,
                    totalCarbs = 250f,
                    totalFat = 60f,
                    targetCalories = 2500
                )
            ),
            workoutLogs = logs,
            workoutRoutines = listOf(backRoutine),
            allExercises = listOf(backExercise),
            cardioLogs = cardioLogs,
            gpsRouteLogs = gpsLogs,
            equippedTitleId = "cardio_distance_10km"
        )

        val backBadge = summary.achievements.first { it.definition.id == "muscle_back_20" }
        val volumeBadge = summary.achievements.first { it.definition.id == "workout_volume_25000" }
        val saitamaBadge = summary.achievements.first { it.definition.id == "cardio_distance_10km" }

        assertTrue(backBadge.isUnlocked)
        assertTrue(volumeBadge.isUnlocked)
        assertTrue(saitamaBadge.isUnlocked)
        assertTrue(saitamaBadge.isEquippedTitle)
        assertEquals("Quebrador de Limitador [Saitama]", summary.activeTitleText)
    }

    @Test
    fun classifyMuscleFocusGroups_identifiesAllSixMuscleGroups() {
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Supino Reto Peitoral").contains(MuscleFocusGroup.CHEST))
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Remada Curvada Dorsal").contains(MuscleFocusGroup.BACK))
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Agachamento Livre Quadríceps e Glúteos").contains(MuscleFocusGroup.LEGS))
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Elevação Lateral Ombros").contains(MuscleFocusGroup.SHOULDERS))
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Rosca Direta Bíceps e Tríceps").contains(MuscleFocusGroup.ARMS))
        assertTrue(AchievementCatalogAndEngine.classifyMuscleFocusGroups("Prancha Abdominal Core").contains(MuscleFocusGroup.CORE))
    }
}
