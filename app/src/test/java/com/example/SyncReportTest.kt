package com.example

import com.example.data.firestore.PendingCloudWriteException
import com.example.data.firestore.SyncReport
import com.example.data.firestore.collectSyncResults
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SyncReportTest {
    @Test fun countsConfirmedPendingAndRejectedWritesSeparately() = runTest {
        val report = collectSyncResults(listOf(
            { Result.success(Unit) },
            { Result.failure(PendingCloudWriteException()) },
            { Result.failure(IllegalStateException("permission denied")) },
            { throw IllegalStateException("unexpected error") }
        ))
        assertEquals(SyncReport(successful = 1, pending = 1, failed = 2), report)
    }

    @Test fun cancellationStopsSyncInsteadOfReportingSuccess() = runTest {
        try {
            collectSyncResults(listOf({ throw CancellationException() }))
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
    }
}
