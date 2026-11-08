package com.example.ui.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.repository.VirtusRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class PendingSyncItem(
    val id: String = UUID.randomUUID().toString(),
    val category: String, // "Treino" or "Dieta"
    val description: String,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    fun formattedTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestampMillis))
    }
}

/**
 * Manages local-first offline mutation queuing and automatic Delta Sync to Firebase Firestore
 * when network connectivity is available or restored.
 */
class SyncController(
    private val context: Context,
    private val scope: CoroutineScope,
    private val repository: VirtusRepository,
    private val syncManager: FirestoreSyncManager,
    private val showMessage: (String) -> Unit
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val busy = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = busy.asStateFlow()

    private val _pendingItems = MutableStateFlow<List<PendingSyncItem>>(loadPersistedQueue())
    val pendingItems: StateFlow<List<PendingSyncItem>> = _pendingItems.asStateFlow()

    private val _isOnline = MutableStateFlow(checkCurrentConnectivity())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _lastSyncBannerNotification = MutableStateFlow<String?>(null)
    val lastSyncBannerNotification: StateFlow<String?> = _lastSyncBannerNotification.asStateFlow()

    private var autoSyncJob: Job? = null
    private var bannerAutoDismissJob: Job? = null

    init {
        registerNetworkCallback()
    }

    private fun checkCurrentConnectivity(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return true
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (_: Exception) {
            true
        }
    }

    private fun registerNetworkCallback() {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val wasOffline = !_isOnline.value
                    _isOnline.value = true
                    if (wasOffline && _pendingItems.value.isNotEmpty()) {
                        scope.launch {
                            delay(1200)
                            if (_isOnline.value && _pendingItems.value.isNotEmpty()) {
                                sync(isAutoFlush = true)
                            }
                        }
                    }
                }

                override fun onLost(network: Network) {
                    _isOnline.value = checkCurrentConnectivity()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val validated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    val wasOffline = !_isOnline.value
                    _isOnline.value = validated
                    if (wasOffline && validated && _pendingItems.value.isNotEmpty()) {
                        scope.launch {
                            delay(1000)
                            if (_isOnline.value && _pendingItems.value.isNotEmpty()) {
                                sync(isAutoFlush = true)
                            }
                        }
                    }
                }
            })
        } catch (_: Exception) {
            // Fallback in unit test or restricted context
        }
    }

    fun enqueueChange(category: String, description: String) {
        val onlineNow = checkCurrentConnectivity()
        _isOnline.value = onlineNow

        val newItem = PendingSyncItem(
            category = category,
            description = description,
            timestampMillis = System.currentTimeMillis()
        )
        val updated = (_pendingItems.value + newItem).takeLast(50)
        _pendingItems.value = updated
        persistQueue(updated)

        if (onlineNow) {
            autoSyncJob?.cancel()
            autoSyncJob = scope.launch {
                delay(2200)
                sync(isAutoFlush = true)
            }
        }
    }

    fun dismissSyncBannerNotification() {
        bannerAutoDismissJob?.cancel()
        _lastSyncBannerNotification.value = null
    }

    private fun postSyncBannerNotification(message: String) {
        _lastSyncBannerNotification.value = message
        bannerAutoDismissJob?.cancel()
        bannerAutoDismissJob = scope.launch {
            delay(6500)
            _lastSyncBannerNotification.value = null
        }
    }

    fun sync(isAutoFlush: Boolean = false) {
        val onlineNow = checkCurrentConnectivity()
        _isOnline.value = onlineNow
        if (!onlineNow) {
            val count = _pendingItems.value.size
            showMessage(
                if (count > 0) {
                    "Sem internet no momento. $count alteração(ões) aguardando na fila de sincronização."
                } else {
                    "Sem conexão com a internet. Suas edições permanecem salvas no aparelho."
                }
            )
            return
        }

        if (!busy.compareAndSet(false, true)) return
        val queuedSnapshot = _pendingItems.value
        val queuedCount = queuedSnapshot.size

        scope.launch {
            try {
                if (!isAutoFlush) {
                    showMessage("Sincronizando fila com a nuvem...")
                }
                val profile = repository.getUserProfileDirect(1)
                val uid = profile?.firebaseUid.orEmpty()
                val isGuest = uid.isBlank() || uid.startsWith("guest_")

                if (isGuest) {
                    if (queuedCount > 0) {
                        clearQueueIds(queuedSnapshot.map { it.id }.toSet())
                        val msg = "Dados salvos localmente ($queuedCount item(ns)). Conecte sua conta Google para backup em nuvem."
                        postSyncBannerNotification(msg)
                        if (!isAutoFlush) showMessage(msg)
                    } else if (!isAutoFlush) {
                        showMessage("Modo local ativo. Faça login no Google para sincronizar na nuvem.")
                    }
                    return@launch
                }

                val favoriteFoods = try {
                    repository.allFoods.first().filter { it.isFavorite }
                } catch (_: Exception) {
                    emptyList()
                }
                val result = syncManager.syncAllData(
                    profile = profile,
                    workoutLogs = repository.workoutLogs.first(),
                    progressLogs = repository.progressLogs.first(),
                    gpsLogs = repository.allGpsRouteLogs.first(),
                    foodRecords = repository.allFoodRecords.first(),
                    cardioLogs = repository.allCardioLogs.first(),
                    dailyIntakes = repository.recentIntakeHistory.first(),
                    favoriteFoods = favoriteFoods,
                    workoutRoutines = repository.workoutRoutines.first(),
                    workoutExercises = repository.allExercises.first(),
                    mealPlanItems = repository.mealPlanItems.first(),
                    shoppingItems = repository.shoppingList.first()
                )
                result.onSuccess { report ->
                    clearQueueIds(queuedSnapshot.map { it.id }.toSet())
                    val summaryMessage = if (queuedCount > 0) {
                        "☁️ Fila sincronizada! $queuedCount alteração(ões) de treino/dieta enviada(s) para a nuvem."
                    } else {
                        report.shortMessage()
                    }
                    postSyncBannerNotification(summaryMessage)
                    showMessage(summaryMessage)
                }.onFailure { err ->
                    if (!isAutoFlush) {
                        showMessage(err.message ?: "Falha na sincronização. Os itens continuam na fila.")
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (!isAutoFlush) {
                    showMessage("Não foi possível sincronizar agora. Os dados seguem protegidos na fila local.")
                }
            } finally {
                busy.value = false
            }
        }
    }

    private fun clearQueueIds(ids: Set<String>) {
        if (ids.isEmpty()) return
        val remaining = _pendingItems.value.filterNot { it.id in ids }
        _pendingItems.value = remaining
        persistQueue(remaining)
    }

    private fun persistQueue(items: List<PendingSyncItem>) {
        try {
            val array = JSONArray()
            items.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("category", item.category)
                    put("description", item.description)
                    put("timestampMillis", item.timestampMillis)
                }
                array.put(obj)
            }
            prefs.edit().putString(KEY_QUEUE_JSON, array.toString()).apply()
        } catch (_: Exception) {
        }
    }

    private fun loadPersistedQueue(): List<PendingSyncItem> {
        return try {
            val raw = prefs.getString(KEY_QUEUE_JSON, null) ?: return emptyList()
            val array = JSONArray(raw)
            val list = mutableListOf<PendingSyncItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PendingSyncItem(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        category = obj.optString("category", "Treino"),
                        description = obj.optString("description", "Alteração local"),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS_NAME = "virtus_offline_sync_queue"
        private const val KEY_QUEUE_JSON = "pending_sync_items_json"
    }
}
