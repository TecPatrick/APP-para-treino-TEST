package com.example.ui.sync

import com.example.data.firestore.FirestoreSyncManager
import com.example.data.repository.VirtusRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Reads Room directly so an unopened screen cannot produce an empty sync. */
class SyncController(
    private val scope: CoroutineScope,
    private val repository: VirtusRepository,
    private val syncManager: FirestoreSyncManager,
    private val showMessage: (String) -> Unit
) {
    private val busy = MutableStateFlow(false)
    val isSyncing = busy.asStateFlow()

    fun sync() {
        if (!busy.compareAndSet(false, true)) return
        scope.launch {
            try {
                showMessage("Verificando envios à nuvem...")
                val result = syncManager.syncAllData(
                    repository.getUserProfileDirect(1),
                    repository.workoutLogs.first(),
                    repository.progressLogs.first(),
                    repository.allGpsRouteLogs.first(),
                    repository.allFoodRecords.first(),
                    repository.allCardioLogs.first(),
                    repository.recentIntakeHistory.first()
                )
                result.onSuccess { showMessage(it.message()) }
                    .onFailure { showMessage(it.message ?: "Falha na sincronização. Tente novamente.") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showMessage("Não foi possível sincronizar. Os dados locais foram preservados.")
            } finally {
                busy.value = false
            }
        }
    }
}
