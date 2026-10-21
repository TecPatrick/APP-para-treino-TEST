package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Diet(
    @DocumentId val id: String = "",
    val userId: String = "",
    @ServerTimestamp val date: Timestamp? = null,
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val description: String = ""
)
