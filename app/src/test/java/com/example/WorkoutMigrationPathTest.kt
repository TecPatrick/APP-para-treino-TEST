package com.example

import com.example.data.firestore.WorkoutMigrationService
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutMigrationPathTest {

    @Test
    fun `resolveCompatiblePaths generates nested workoutExercises and workouts exercises paths while preserving legacy path`() {
        val paths = WorkoutMigrationService.resolveCompatiblePaths(
            userId = "user_123",
            rawWorkoutId = "42",
            exerciseId = "101"
        )

        assertEquals("42", paths.normalizedWorkoutId)
        assertEquals(
            "users/user_123/workoutRoutines/42/workoutExercises/101",
            paths.routineWorkoutExercisesPath
        )
        assertEquals(
            "users/user_123/workouts/42/workoutExercises/101",
            paths.workoutsWorkoutExercisesPath
        )
        assertEquals(
            "users/user_123/workouts/42/exercises/101",
            paths.workoutsExercisesPath
        )
        assertEquals(
            "users/user_123/workoutExercises/42_101",
            paths.legacyRootWorkoutExercisesPath
        )
    }

    @Test
    fun `resolveCompatiblePaths falls back to free workoutId when routineId is zero or blank`() {
        val zeroPaths = WorkoutMigrationService.resolveCompatiblePaths(
            userId = "user_abc",
            rawWorkoutId = "0",
            exerciseId = "7"
        )
        assertEquals("free", zeroPaths.normalizedWorkoutId)
        assertEquals(
            "users/user_abc/workoutRoutines/free/workoutExercises/7",
            zeroPaths.routineWorkoutExercisesPath
        )
        assertEquals(
            "users/user_abc/workouts/free/exercises/7",
            zeroPaths.workoutsExercisesPath
        )
    }
}
