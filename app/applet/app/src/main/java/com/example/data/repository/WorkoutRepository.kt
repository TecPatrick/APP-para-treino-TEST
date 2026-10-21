package com.example.data.repository

import android.content.Context
import com.example.data.model.Workout
import com.example.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class WorkoutRepository(private val db: FirebaseFirestore) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in.")
    }

    suspend fun addWorkout(workout: Workout) {
        val uid = requireUserId()
        db.collection("users").document(uid).collection("workouts")
            .add(workout.copy(userId = uid))
            .await()
    }
}
