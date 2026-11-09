package com.example.util

object FirestoreLogger {
    private const val TAG = "VirtusFirestore"

    fun logWriteAttempt(
        action: String,
        userId: String?,
        path: String,
        data: Any?
    ) {
        val maskedUid = if (userId.isNullOrBlank()) "UNAUTHENTICATED" else AppLog.maskUid(userId)
        val maskedPath = userId?.takeIf { it.isNotBlank() }?.let { path.replace(it, maskedUid) } ?: path
        AppLog.d(TAG, "🔥 [FIRESTORE WRITE] Action: $action")
        AppLog.d(TAG, "   ├─ Auth UID: $maskedUid")
        AppLog.d(TAG, "   ├─ Target Path: $maskedPath")
        AppLog.d(TAG, "   └─ Data Payload: [redacted]")
    }

    fun logSuccess(action: String, path: String) {
        AppLog.d(TAG, "✅ [FIRESTORE SUCCESS] Action: $action | Path: $path")
    }

    fun logError(action: String, path: String, error: Throwable) {
        AppLog.e(TAG, "❌ [FIRESTORE ERROR] Action: $action | Path: $path", error)
    }
}
