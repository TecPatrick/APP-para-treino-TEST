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
                showMessage("Verificando alterações (Delta Sync)...")
                val favoriteFoods = try {
                    repository.allFoods.first().filter { it.isFavorite }
                } catch (_: Exception) {
                    emptyList()
                }
                val result = syncManager.syncAllData(
                    profile = repository.getUserProfileDirect(1),
                    workoutLogs = repository.workoutLogs.first(),
                    progressLogs = repository.progressLogs.first(),
                    gpsLogs = repository.allGpsRouteLogs.first(),
                    foodRecords = repository.allFoodRecords.first(),
                    cardioLogs = repository.allCardioLogs.first(),
                    dailyIntakes = repository.recentIntakeHistory.first(),
                    favoriteFoods = favoriteFoods
                )
                result.onSuccess { showMessage(it.shortMessage()) }
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
