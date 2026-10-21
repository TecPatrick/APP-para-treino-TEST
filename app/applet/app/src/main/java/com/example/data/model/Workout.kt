package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Workout(
    @DocumentId val id: String = "",
    val userId: String = "",
    @ServerTimestamp val date: Timestamp? = null,
    val exerciseName: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Double = 0.0
)
