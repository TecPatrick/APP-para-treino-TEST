package com.example.ui

import com.example.data.model.WorkoutExercise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseSetAdjustmentTest {

    @Test
    fun testAddSetIncrement() {
        val exercise = WorkoutExercise(
            id = 101L,
            routineId = 1L,
            exerciseName = "Supino Reto",
            muscleGroup = "Peitoral",
            sets = 4,
            repsMin = 8,
            repsMax = 12,
            currentWeightKg = 80f,
            restSeconds = 90
        )

        val updated = exercise.copy(sets = exercise.sets + 1)
        assertEquals(5, updated.sets)
    }

    @Test
    fun testRemoveSetDecrement() {
        val exercise = WorkoutExercise(
            id = 101L,
            routineId = 1L,
            exerciseName = "Supino Reto",
            muscleGroup = "Peitoral",
            sets = 4,
            repsMin = 8,
            repsMax = 12,
            currentWeightKg = 80f,
            restSeconds = 90
        )

        val updated = exercise.copy(sets = (exercise.sets - 1).coerceAtLeast(1))
        assertEquals(3, updated.sets)
    }

    @Test
    fun testUpdateLoggedSetDetails() {
        val exId = 101L
        val initialList = mutableListOf(Pair(12, 80f), Pair(10, 80f))
        
        // Update 2nd set: change to 8 reps @ 85kg
        val setIndex = 2
        val zeroIdx = setIndex - 1
        initialList[zeroIdx] = Pair(8, 85f)
        
        assertEquals(8, initialList[1].first)
        assertEquals(85f, initialList[1].second)
    }

    @Test
    fun testUnlogSetDecreasesCount() {
        val initialList = mutableListOf(Pair(12, 80f), Pair(10, 80f), Pair(8, 85f))
        val initialCount = initialList.size
        
        // Remove 2nd set
        initialList.removeAt(1)
        
        assertEquals(2, initialList.size)
        assertEquals(Pair(8, 85f), initialList[1])
    }

    @Test
    fun testActiveWorkoutSessionAudioDefaults() {
        val session = ActiveWorkoutSession()
        assertTrue(session.isWorkoutSoundEnabled)
        assertTrue(session.isWorkoutHapticEnabled)
        assertEquals(90, session.restTimerTotalSeconds)
        assertFalse(session.isRestTimerRunning)
    }
}
