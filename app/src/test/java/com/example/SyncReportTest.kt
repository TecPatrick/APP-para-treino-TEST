package com.example

import com.example.data.firestore.ConflictDecision
import com.example.data.firestore.DeltaSyncLedger
import com.example.data.firestore.FirestoreDeltaEngine
import com.example.data.firestore.PendingCloudWriteException
import com.example.data.firestore.SyncReport
import com.example.data.firestore.collectSyncResults
import com.example.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test fun profileFieldDiffExtractsOnlyModifiedFields() {
        val uid = "user_123"
        val baseProfile = UserProfile(name = "Atleta Virtus", weightKg = 80.0f, targetWaterMl = 3500)
        val updatedProfile = baseProfile.copy(weightKg = 81.5f, targetWaterMl = 3800)

        val baseMap = FirestoreDeltaEngine.profileToCanonicalMap(baseProfile, uid)
        val updatedMap = FirestoreDeltaEngine.profileToCanonicalMap(updatedProfile, uid)

        val noDiff = FirestoreDeltaEngine.computeFieldDiff(baseMap, baseMap)
        assertFalse(noDiff.isChanged)
        assertTrue(noDiff.changedFieldNames.isEmpty())

        val delta = FirestoreDeltaEngine.computeFieldDiff(baseMap, updatedMap)
        assertTrue(delta.isChanged)
        assertEquals(setOf("weightKg", "targetWaterMl"), delta.changedFieldNames)
        assertEquals(81.5, delta.changedFields["weightKg"])
        assertEquals(3800, delta.changedFields["targetWaterMl"])
        assertFalse(delta.changedFields.containsKey("name"))
    }

    @Test fun collectionRecordHashAndLedgerSkipUnchangedItems() {
        val ledger = DeltaSyncLedger(null)
        val uid = "user_123"
        val payloadA = mapOf("userId" to uid, "routineName" to "Treino A", "totalVolumeKg" to 4200.0)
        val payloadSame = mapOf("totalVolumeKg" to 4200.0, "userId" to uid, "routineName" to "Treino A", "updatedAtMillis" to 99999L)
        val payloadModified = mapOf("userId" to uid, "routineName" to "Treino A", "totalVolumeKg" to 4550.0)

        val hash1 = FirestoreDeltaEngine.computeRecordHash("workoutLogs", payloadA)
        val hash2 = FirestoreDeltaEngine.computeRecordHash("workoutLogs", payloadSame)
        val hash3 = FirestoreDeltaEngine.computeRecordHash("workoutLogs", payloadModified)

        assertEquals("Key order and volatile timestamps must not change content hash", hash1, hash2)
        assertTrue(ledger.hasEntityChanged(uid, "workoutLogs", "log_1", hash1))

        ledger.markEntitySynced(uid, "workoutLogs", "log_1", hash1, 1000L)
        assertFalse("Already synced hash must be skipped", ledger.hasEntityChanged(uid, "workoutLogs", "log_1", hash2))
        assertTrue("Modified record hash must be flagged as changed", ledger.hasEntityChanged(uid, "workoutLogs", "log_1", hash3))
    }

    @Test fun conflictResolutionLastWriteWinsByTimestamp() {
        val uid = "user_123"
        val localProfile = UserProfile(name = "Patrick", weightKg = 82.0f)
        val remoteProfile = UserProfile(name = "Patrick", weightKg = 79.0f)

        // Case 1: Local is newer (timestamp 2000 > 1000) -> Keep local
        val localWins = FirestoreDeltaEngine.resolveProfileConflictLWW(
            localProfile = localProfile,
            localUpdatedAtMillis = 2000L,
            remoteProfile = remoteProfile,
            remoteUpdatedAtMillis = 1000L,
            uid = uid
        )
        assertEquals(ConflictDecision.KEEP_LOCAL_NEWER, localWins.decision)
        assertEquals(82.0f, localWins.mergedProfile.weightKg)

        // Case 2: Remote is newer (timestamp 3000 > 2000) -> Apply remote delta
        val remoteWins = FirestoreDeltaEngine.resolveProfileConflictLWW(
            localProfile = localProfile,
            localUpdatedAtMillis = 2000L,
            remoteProfile = remoteProfile,
            remoteUpdatedAtMillis = 3000L,
            uid = uid
        )
        assertEquals(ConflictDecision.APPLY_REMOTE_DELTA, remoteWins.decision)
        assertEquals(79.0f, remoteWins.mergedProfile.weightKg)
    }
}
