package com.example.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.FirestoreDeltaEngine
import com.example.data.firestore.WorkoutMigrationService
import kotlinx.coroutines.launch

class SyncViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    fun dismissSyncBannerNotification() = syncController.dismissSyncBannerNotification()

    fun syncAllDataToFirestore() = syncController.sync(isAutoFlush = false)

    fun triggerAutomaticWorkoutExerciseMigration() = shared.triggerAutomaticWorkoutExerciseMigration()

    fun uploadProfileToCloud() {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect(1) ?: userProfile.value
            if (current.firebaseUid.isBlank() || current.firebaseUid.startsWith("guest_")) {
                showToast("Vincule sua conta Google para enviar o perfil para a nuvem.")
                return@launch
            }
            shared.setFirestoreSyncing(true)
            try {
                val profileRes = firestoreSync.syncProfileToCloud(current)
                val goalsRes = firestoreSync.syncCaloricGoalsToCloud(
                    targetCalories = current.targetCalories,
                    targetProteinGrams = current.targetProteinGrams,
                    targetCarbsGrams = current.targetCarbsGrams,
                    targetFatGrams = current.targetFatGrams,
                    targetWaterMl = current.targetWaterMl,
                    customUid = current.firebaseUid
                )
                if (profileRes.isSuccess && goalsRes.isSuccess) {
                    showToast("Perfil e metas enviados para a nuvem com sucesso! ☁️")
                } else {
                    showToast("Perfil salvo no cache! Será enviado assim que conectar à internet.")
                }
            } catch (e: Exception) {
                showToast("Não foi possível enviar o perfil agora: ${e.message}")
            } finally {
                shared.setFirestoreSyncing(false)
            }
        }
    }

    fun downloadProfileFromCloud() {
        viewModelScope.launch {
            val current = repository.getUserProfileDirect(1) ?: userProfile.value
            val uid = authService.currentUser?.uid ?: current.firebaseUid
            if (uid.isBlank() || uid.startsWith("guest_")) {
                showToast("Vincule sua conta Google para baixar seu perfil da nuvem.")
                return@launch
            }
            shared.setFirestoreSyncing(true)
            try {
                val res = firestoreSync.fetchProfileSnapshotFromCloud(uid)
                res.onSuccess { remoteSnapshot ->
                    if (remoteSnapshot != null) {
                        val resolution = firestoreSync.reconcileRemoteProfileLWW(
                            localProfile = current,
                            remoteProfile = remoteSnapshot.profile,
                            remoteUpdatedAtMillis = remoteSnapshot.updatedAtMillis,
                            uid = uid
                        )
                        val merged = remoteSnapshot.profile.copy(
                            id = 1,
                            isLoggedIn = true,
                            firebaseUid = uid,
                            email = remoteSnapshot.profile.email.ifBlank { current.email },
                            name = remoteSnapshot.profile.name.ifBlank { current.name }
                        )
                        repository.saveUserProfile(merged)
                        firestoreSync.deltaLedger.markProfileSynced(
                            uid = uid,
                            canonicalMap = FirestoreDeltaEngine.profileToCanonicalMap(merged, uid),
                            updatedAtMillis = if (remoteSnapshot.updatedAtMillis > 0L) remoteSnapshot.updatedAtMillis else System.currentTimeMillis()
                        )
                        showToast(
                            if (resolution.changedFieldsCount == 0) {
                                "Seu perfil já está idêntico ao da nuvem! ⚡"
                            } else {
                                "Perfil baixado da nuvem com sucesso! ✅"
                            }
                        )
                    } else {
                        showToast("Nenhum perfil encontrado na nuvem ainda. Use 'Enviar Perfil' primeiro!")
                    }
                }.onFailure { err ->
                    showToast("Erro ao baixar perfil da nuvem: ${err.message}")
                }
            } catch (e: Exception) {
                showToast("Falha ao baixar perfil: ${e.message}")
            } finally {
                shared.setFirestoreSyncing(false)
            }
        }
    }

    fun runWorkoutSubcollectionMigration(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val service = WorkoutMigrationService(getApplication())
                val result = service.migrateUserWorkoutsAndExercises()
                result.onSuccess { report ->
                    val msg = "Migração concluída: ${report.workoutsProcessed} treinos e ${report.exercisesMigrated + report.legacyExercisesMoved} exercícios migrados."
                    showToast(msg)
                    onComplete(true, msg)
                }.onFailure { err ->
                    val msg = "Erro na migração: ${err.message}"
                    showToast(msg)
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = "Falha ao executar migração: ${e.message}"
                showToast(msg)
                onComplete(false, msg)
            }
        }
    }
}
