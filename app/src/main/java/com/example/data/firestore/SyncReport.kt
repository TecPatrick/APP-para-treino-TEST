package com.example.data.firestore

import kotlinx.coroutines.CancellationException

class PendingCloudWriteException : Exception("Envio aguardando confirmação da nuvem.")

data class SyncReport(val successful: Int = 0, val pending: Int = 0, val failed: Int = 0) {
    fun message(): String = "$successful confirmados na nuvem; $pending aguardando confirmação; $failed falharam."
}

internal suspend fun collectSyncResults(operations: List<suspend () -> Result<Unit>>): SyncReport {
    var successful = 0
    var pending = 0
    var failed = 0
    for (operation in operations) {
        val result = try {
            operation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
        when {
            result.isSuccess -> successful++
            result.exceptionOrNull() is PendingCloudWriteException -> pending++
            else -> failed++
        }
    }
    return SyncReport(successful, pending, failed)
}
