package com.example.data.firestore

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.UserProfile
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale
import kotlin.math.abs

/**
 * Resultado da comparação campo a campo (Field-Level Diffing) do Perfil ou Metas.
 */
data class FieldDeltaResult(
    val changedFields: Map<String, Any>,
    val changedFieldNames: Set<String>
) {
    val isChanged: Boolean
        get() = changedFields.isNotEmpty()
}

/**
 * Decisão da resolução de conflitos Last-Write-Wins (LWW) baseada em Timestamp.
 */
enum class ConflictDecision {
    APPLY_REMOTE_DELTA,
    KEEP_LOCAL_NEWER,
    NO_OP
}

data class ProfileConflictResolution(
    val decision: ConflictDecision,
    val mergedProfile: UserProfile,
    val changedFieldsCount: Int
)

/**
 * Motor puro de Diffing e Hash determinístico para Sincronização Delta no Firestore.
 * - Calcula diffs campo a campo para o UserProfile e CaloricGoalData.
 * - Calcula hashes SHA-256 determinísticos para registros de histórico (Treinos, Dieta, Progresso, etc.).
 * - Resolve conflitos entre Local e Nuvem usando a regra "Mais recente vence (Timestamp)".
 */
object FirestoreDeltaEngine {

    private val VOLATILE_KEYS = setOf("updatedAt", "updatedAtMillis", "createdAt")

    /**
     * Converte o [UserProfile] em um mapa canônico de campos persistidos na nuvem.
     */
    fun profileToCanonicalMap(profile: UserProfile, uid: String): Map<String, Any> {
        return linkedMapOf(
            "userId" to uid,
            "name" to profile.name,
            "email" to profile.email,
            "gender" to profile.gender.name,
            "age" to profile.age,
            "weightKg" to roundDouble(profile.weightKg.toDouble()),
            "heightCm" to roundDouble(profile.heightCm.toDouble()),
            "biotype" to profile.biotype.name,
            "trainingDaysPerWeek" to profile.trainingDaysPerWeek,
            "activityLevel" to profile.activityLevel.name,
            "goal" to profile.goal.name,
            "dietCycleType" to profile.dietCycleType.name,
            "isAthleteOrWarmClimate" to profile.isAthleteOrWarmClimate,
            "targetCalories" to profile.targetCalories,
            "targetProteinGrams" to profile.targetProteinGrams,
            "targetCarbsGrams" to profile.targetCarbsGrams,
            "targetFatGrams" to profile.targetFatGrams,
            "targetWaterMl" to profile.targetWaterMl,
            "monthlyTargetWeightKg" to roundDouble(profile.monthlyTargetWeightKg.toDouble()),
            "monthlyTargetDurationWeeks" to profile.monthlyTargetDurationWeeks,
            "monthlyNotes" to profile.monthlyNotes,
            "isDarkMode" to profile.isDarkMode,
            "isBiometricLockEnabled" to profile.isBiometricLockEnabled,
            "hasCompletedOnboarding" to profile.hasCompletedOnboarding
        )
    }

    /**
     * Converte as metas calóricas em um mapa canônico.
     */
    fun caloricGoalsToCanonicalMap(
        uid: String,
        targetCalories: Int,
        targetProteinGrams: Int,
        targetCarbsGrams: Int,
        targetFatGrams: Int,
        targetWaterMl: Int
    ): Map<String, Any> {
        return linkedMapOf(
            "userId" to uid,
            "targetCalories" to targetCalories,
            "targetProteinGrams" to targetProteinGrams,
            "targetCarbsGrams" to targetCarbsGrams,
            "targetFatGrams" to targetFatGrams,
            "targetWaterMl" to targetWaterMl
        )
    }

    /**
     * Calcula apenas os campos que mudaram entre o mapa anterior (`previous`) e o atual (`current`).
     */
    fun computeFieldDiff(
        previous: Map<String, Any?>?,
        current: Map<String, Any>
    ): FieldDeltaResult {
        if (previous.isNullOrEmpty()) {
            val nonStructuralKeys = current.keys - "userId"
            return FieldDeltaResult(
                changedFields = current,
                changedFieldNames = nonStructuralKeys
            )
        }

        val changed = linkedMapOf<String, Any>()
        val changedNames = linkedSetOf<String>()

        for ((key, currentValue) in current) {
            if (key in VOLATILE_KEYS) continue
            if (key == "userId") continue
            val prevValue = previous[key]
            if (!valuesAreEqual(prevValue, currentValue)) {
                changed[key] = currentValue
                changedNames.add(key)
            }
        }

        if (changed.isNotEmpty() && current.containsKey("userId")) {
            changed["userId"] = current.getValue("userId")
        }

        return FieldDeltaResult(
            changedFields = changed,
            changedFieldNames = changedNames
        )
    }

    /**
     * Gera um hash SHA-256 determinístico para qualquer registro de coleção,
     * ignorando campos de timestamp voláteis do servidor e ordenando as chaves.
     */
    fun computeRecordHash(collection: String, payload: Map<String, Any?>): String {
        val sortedEntries = payload.entries
            .filter { it.key !in VOLATILE_KEYS }
            .sortedBy { it.key }
            .joinToString(separator = "|") { (k, v) -> "$k=${normalizeValueForHash(v)}" }

        val canonical = "$collection::$sortedEntries"
        val digest = MessageDigest.getInstance("SHA-256").digest(canonical.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }.take(32)
    }

    /**
     * Resolve conflitos entre o Perfil Local e o Perfil Remoto usando a regra
     * "Mais recente vence (Timestamp)" (Last-Write-Wins).
     */
    fun resolveProfileConflictLWW(
        localProfile: UserProfile?,
        localUpdatedAtMillis: Long,
        remoteProfile: UserProfile,
        remoteUpdatedAtMillis: Long,
        uid: String
    ): ProfileConflictResolution {
        if (localProfile == null) {
            val normalized = remoteProfile.copy(id = 1, isLoggedIn = true, firebaseUid = uid)
            return ProfileConflictResolution(
                decision = ConflictDecision.APPLY_REMOTE_DELTA,
                mergedProfile = normalized,
                changedFieldsCount = 23
            )
        }

        val localMap = profileToCanonicalMap(localProfile, uid)
        val remoteMap = profileToCanonicalMap(remoteProfile, uid)
        val diff = computeFieldDiff(localMap, remoteMap)

        if (!diff.isChanged) {
            return ProfileConflictResolution(
                decision = ConflictDecision.NO_OP,
                mergedProfile = localProfile,
                changedFieldsCount = 0
            )
        }

        // Se o dado local possui um timestamp mais recente que o remoto, o local vence.
        if (localUpdatedAtMillis > 0L && remoteUpdatedAtMillis > 0L && localUpdatedAtMillis > remoteUpdatedAtMillis) {
            return ProfileConflictResolution(
                decision = ConflictDecision.KEEP_LOCAL_NEWER,
                mergedProfile = localProfile,
                changedFieldsCount = diff.changedFieldNames.size
            )
        }

        // Caso contrário (remoto mais recente ou inicialização sem timestamp local), aplica o delta remoto
        val merged = remoteProfile.copy(
            id = 1,
            isLoggedIn = true,
            firebaseUid = uid,
            email = remoteProfile.email.ifBlank { localProfile.email },
            name = remoteProfile.name.ifBlank { localProfile.name }
        )
        return ProfileConflictResolution(
            decision = ConflictDecision.APPLY_REMOTE_DELTA,
            mergedProfile = merged,
            changedFieldsCount = diff.changedFieldNames.size
        )
    }

    private fun valuesAreEqual(a: Any?, b: Any?): Boolean {
        if (a == null && b == null) return true
        if (a == null || b == null) return false
        if (a is Number && b is Number) {
            return abs(a.toDouble() - b.toDouble()) < 0.0001
        }
        return a.toString() == b.toString()
    }

    private fun normalizeValueForHash(value: Any?): String {
        return when (value) {
            null -> "null"
            is Float -> String.format(Locale.US, "%.3f", value)
            is Double -> String.format(Locale.US, "%.3f", value)
            else -> value.toString().trim()
        }
    }

    private fun roundDouble(value: Double): Double {
        return String.format(Locale.US, "%.3f", value).toDouble()
    }
}

/**
 * Armazena localmente (em SharedPreferences + cache em memória) os hashes e snapshots
 * já sincronizados por usuário para evitar reenvios desnecessários ao Firestore.
 */
class DeltaSyncLedger(context: Context? = null) {

    private val prefs: SharedPreferences? = try {
        context?.applicationContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    } catch (_: Exception) {
        null
    }

    private val memoryStore = mutableMapOf<String, String>()
    private val memoryTimestamps = mutableMapOf<String, Long>()

    @Synchronized
    fun getLastProfileMap(uid: String): Map<String, Any?>? {
        val raw = readString(keyProfileSnapshot(uid)) ?: return null
        return jsonToMap(raw)
    }

    @Synchronized
    fun markProfileSynced(uid: String, canonicalMap: Map<String, Any>, updatedAtMillis: Long) {
        writeString(keyProfileSnapshot(uid), mapToJson(canonicalMap))
        writeLong(keyProfileSyncedAt(uid), updatedAtMillis)
    }

    @Synchronized
    fun getLastCaloricGoalsMap(uid: String): Map<String, Any?>? {
        val raw = readString(keyGoalsSnapshot(uid)) ?: return null
        return jsonToMap(raw)
    }

    @Synchronized
    fun markCaloricGoalsSynced(uid: String, canonicalMap: Map<String, Any>, updatedAtMillis: Long) {
        writeString(keyGoalsSnapshot(uid), mapToJson(canonicalMap))
        writeLong(keyGoalsSyncedAt(uid), updatedAtMillis)
    }

    @Synchronized
    fun recordLocalProfileMutation(uid: String, timestampMillis: Long = System.currentTimeMillis()) {
        if (uid.isBlank()) return
        writeLong(keyProfileLocalModifiedAt(uid), timestampMillis)
    }

    @Synchronized
    fun getLocalProfileModifiedAt(uid: String): Long {
        if (uid.isBlank()) return 0L
        return maxOf(
            readLong(keyProfileLocalModifiedAt(uid)),
            readLong(keyProfileSyncedAt(uid))
        )
    }

    @Synchronized
    fun hasEntityChanged(uid: String, collection: String, docId: String, currentHash: String): Boolean {
        val previousHash = readString(keyEntityHash(uid, collection, docId))
        return previousHash != currentHash
    }

    @Synchronized
    fun markEntitySynced(
        uid: String,
        collection: String,
        docId: String,
        currentHash: String,
        updatedAtMillis: Long = System.currentTimeMillis()
    ) {
        writeString(keyEntityHash(uid, collection, docId), currentHash)
        writeLong(keyEntityTimestamp(uid, collection, docId), updatedAtMillis)
    }

    @Synchronized
    fun removeEntity(uid: String, collection: String, docId: String) {
        removeKey(keyEntityHash(uid, collection, docId))
        removeKey(keyEntityTimestamp(uid, collection, docId))
    }

    @Synchronized
    fun clearUserLedger(uid: String) {
        val prefix = "uid_${uid}_"
        memoryStore.keys.removeAll { it.startsWith(prefix) }
        memoryTimestamps.keys.removeAll { it.startsWith(prefix) }
        prefs?.let { sp ->
            val editor = sp.edit()
            sp.all.keys.filter { it.startsWith(prefix) }.forEach { editor.remove(it) }
            editor.apply()
        }
    }

    private fun keyProfileSnapshot(uid: String) = "uid_${uid}_profile_snapshot"
    private fun keyProfileSyncedAt(uid: String) = "uid_${uid}_profile_synced_at"
    private fun keyProfileLocalModifiedAt(uid: String) = "uid_${uid}_profile_local_modified_at"
    private fun keyGoalsSnapshot(uid: String) = "uid_${uid}_goals_snapshot"
    private fun keyGoalsSyncedAt(uid: String) = "uid_${uid}_goals_synced_at"
    private fun keyEntityHash(uid: String, collection: String, docId: String) =
        "uid_${uid}_hash_${collection}_$docId"
    private fun keyEntityTimestamp(uid: String, collection: String, docId: String) =
        "uid_${uid}_ts_${collection}_$docId"

    private fun readString(key: String): String? {
        return memoryStore[key] ?: prefs?.getString(key, null)?.also { memoryStore[key] = it }
    }

    private fun writeString(key: String, value: String) {
        memoryStore[key] = value
        prefs?.edit()?.putString(key, value)?.apply()
    }

    private fun readLong(key: String): Long {
        return memoryTimestamps[key] ?: (prefs?.getLong(key, 0L) ?: 0L).also {
            if (it > 0L) memoryTimestamps[key] = it
        }
    }

    private fun writeLong(key: String, value: Long) {
        memoryTimestamps[key] = value
        prefs?.edit()?.putLong(key, value)?.apply()
    }

    private fun removeKey(key: String) {
        memoryStore.remove(key)
        memoryTimestamps.remove(key)
        prefs?.edit()?.remove(key)?.apply()
    }

    private fun mapToJson(map: Map<String, Any>): String {
        val json = JSONObject()
        for ((k, v) in map) {
            json.put(k, v)
        }
        return json.toString()
    }

    private fun jsonToMap(raw: String): Map<String, Any?>? {
        return try {
            val json = JSONObject(raw)
            val result = linkedMapOf<String, Any?>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                result[k] = json.opt(k)
            }
            result
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val PREFS_NAME = "virtus_firestore_delta_ledger"
    }
}
