package com.example.util

import android.util.Log

object FirestoreLogger {
    private const val TAG = "VirtusFirestore"

    fun logWriteAttempt(
        action: String,
        userId: String?,
        path: String,
        data: Any?
    ) {
        Log.d(TAG, "🔥 [FIRESTORE WRITE] Action: $action")
        Log.d(TAG, "   ├─ Auth UID: ${userId ?: "UNAUTHENTICATED"}")
        Log.d(TAG, "   ├─ Target Path: $path")
        Log.d(TAG, "   └─ Data Payload: $data")
    }

    fun logSuccess(action: String, path: String) {
        Log.d(TAG, "✅ [FIRESTORE SUCCESS] Action: $action | Path: $path")
    }

    fun logError(action: String, path: String, error: Throwable) {
        Log.e(TAG, "❌ [FIRESTORE ERROR] Action: $action | Path: $path", error)
    }
}
