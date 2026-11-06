package com.example.ui.auth

import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.UserProfile
import com.example.data.repository.VirtusRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Authentication UI state is independent from workouts and nutrition. */
class AuthController(
    private val scope: CoroutineScope,
    private val repository: VirtusRepository,
    private val authService: FirebaseAuthService,
    private val firestoreSync: FirestoreSyncManager,
    private val profile: () -> UserProfile,
    private val showMessage: (String) -> Unit,
    private val onTriggerDeltaSync: () -> Unit = {}
) {
    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    /**
     * Preserves local Room records when transitioning from Guest/Local mode to Google Sign-In
     * so Delta Sync can merge them with the cloud. Only clears local cache if switching between
     * two distinct authenticated Google UIDs.
     */
    private suspend fun localProfileForUser(uid: String): UserProfile {
        val current = profile()
        val previousUid = current.firebaseUid

        return if (
            previousUid.isNotBlank() &&
            !previousUid.startsWith("guest_") &&
            previousUid != uid
        ) {
            repository.clearAllLocalData()
            UserProfile()
        } else {
            current
        }
    }

    private suspend fun localProfileForGuest(): UserProfile {
        val current = profile()

        return if (
            current.firebaseUid.isNotBlank() &&
            !current.firebaseUid.startsWith("guest_")
        ) {
            repository.clearAllLocalData()
            UserProfile()
        } else {
            current
        }
    }

    private fun launchAuth(block: suspend CoroutineScope.() -> Unit) {
        if (!_isAuthLoading.compareAndSet(false, true)) return

        scope.launch {
            try {
                _authError.value = null
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                val message =
                    "Não foi possível concluir a operação. Tente novamente."

                _authError.value = message
                showMessage(message)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    /**
     * Auto-login Google: Restaura automaticamente a sessão ativa do Firebase Auth ao abrir o app
     * e dispara a sincronização incremental (Delta Sync) em segundo plano.
     */
    fun restoreActiveFirebaseSession(onRestored: () -> Unit = {}) {
        val firebaseUser = authService.currentUser ?: return
        val current = profile()
        if (current.isLoggedIn && current.firebaseUid == firebaseUser.uid && current.hasCompletedOnboarding) {
            onTriggerDeltaSync()
            onRestored()
            return
        }

        scope.launch {
            try {
                val baseProfile = localProfileForUser(firebaseUser.uid)
                val remoteProfile = runCatching {
                    firestoreSync.fetchProfileFromCloud(firebaseUser.uid).getOrNull()
                }.getOrNull()

                val merged = mergeLocalAndCloudProfile(
                    local = baseProfile,
                    remote = remoteProfile,
                    uid = firebaseUser.uid,
                    email = firebaseUser.email,
                    displayName = firebaseUser.displayName
                )
                repository.saveUserProfile(merged)
                onTriggerDeltaSync()
                onRestored()
            } catch (_: Exception) {
                // Keep current state if background restoration fails
            }
        }
    }

    private fun mergeLocalAndCloudProfile(
        local: UserProfile,
        remote: UserProfile?,
        uid: String,
        email: String?,
        displayName: String?
    ): UserProfile {
        val resolvedEmail = email?.takeIf { it.isNotBlank() }
            ?: remote?.email?.takeIf { it.isNotBlank() && !it.endsWith("@growth.local") }
            ?: local.email
        val resolvedName = remote?.name?.takeIf { it.isNotBlank() && it != "Atleta" && it != "Atleta Growth" }
            ?: local.name.takeIf { it.isNotBlank() && it != "Atleta" && it != "Atleta Growth" }
            ?: displayName?.takeIf { it.isNotBlank() }
            ?: "Atleta Virtus"

        return if (remote != null) {
            remote.copy(
                id = 1,
                isLoggedIn = true,
                hasCompletedOnboarding = true,
                email = resolvedEmail,
                name = resolvedName,
                firebaseUid = uid,
                isBiometricLockEnabled = local.isBiometricLockEnabled || remote.isBiometricLockEnabled
            )
        } else {
            local.copy(
                id = 1,
                isLoggedIn = true,
                hasCompletedOnboarding = true,
                email = resolvedEmail,
                name = resolvedName,
                firebaseUid = uid
            )
        }
    }

    // Login state is persisted and local data is merged via Delta Sync before notifying the UI.
    fun loginWithGoogleCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit
    ) {
        launchAuth {
            _authError.value = null

            val result = authService.signInWithGoogle(credential)

            result.onSuccess { user ->
                val current = localProfileForUser(user.uid)
                val remoteProfile = runCatching {
                    firestoreSync.fetchProfileFromCloud(user.uid)?.getOrNull()
                }.getOrNull()

                val updated = mergeLocalAndCloudProfile(
                    local = current,
                    remote = remoteProfile,
                    uid = user.uid,
                    email = user.email,
                    displayName = user.displayName
                )

                repository.saveUserProfile(updated)
                runCatching {
                    firestoreSync.recordLocalProfileMutation(user.uid)
                    firestoreSync.syncProfileToCloud(updated)
                }
                onTriggerDeltaSync()

                showMessage(
                    "Login Google confirmado! Dados sincronizados via Delta Sync — " +
                        "Bem-vindo(a), ${updated.name.ifBlank { user.displayName ?: "Atleta" }} 💪"
                )

                onSuccess()
            }.onFailure { ex ->
                val errorMsg =
                    ex.message ?: "Erro ao realizar login com Google."

                _authError.value = errorMsg
                showMessage(errorMsg)
            }
        }
    }

    fun enterAsGuest(onSuccess: () -> Unit) {
        launchAuth {
            val current = localProfileForGuest()

            authService.signOut()

            val hasOnboarded =
                current.hasCompletedOnboarding || current.weightKg > 30f

            val updated = current.copy(
                isLoggedIn = true,
                hasCompletedOnboarding = hasOnboarded,
                email = if (
                    current.email.isNotBlank() &&
                    current.email.contains("@")
                ) {
                    current.email
                } else {
                    "atleta@growth.local"
                },
                name = if (
                    current.name.isNotBlank() &&
                    current.name != "Atleta"
                ) {
                    current.name
                } else {
                    "Atleta Growth"
                },
                firebaseUid = "guest_local",
                passwordHash = ""
            )

            repository.saveUserProfile(updated)

            showMessage("Bem-vindo! Modo local ativado (dados serão mesclados ao conectar o Google).")
            onSuccess()
        }
    }

    fun loginWithBiometrics(onSuccess: () -> Unit) {
        launchAuth {
            val current = profile()

            if (current.firebaseUid.startsWith("guest_")) {
                _authError.value =
                    "Vincule uma conta com Google antes " +
                        "de ativar o login biométrico."

                showMessage(
                    "Vincule sua conta Google no Perfil " +
                        "para usar a biometria."
                )

                return@launchAuth
            }

            if (
                current.email.isBlank() ||
                !current.email.contains("@")
            ) {
                _authError.value =
                    "Perfil sem e-mail válido vinculado."

                showMessage(
                    "Erro: E-mail obrigatório para acesso via biometria."
                )

                return@launchAuth
            }

            val hasValidProfile =
                current.hasCompletedOnboarding &&
                    current.weightKg > 30f &&
                    current.name.isNotBlank() &&
                    current.name != "Atleta"

            if (!hasValidProfile) {
                _authError.value =
                    "Perfil incompleto. Configure seu perfil " +
                        "e cadastre suas medidas antes de usar a biometria."

                showMessage(
                    "Por favor, conclua o cadastro do perfil."
                )

                return@launchAuth
            }

            val updated = current.copy(isLoggedIn = true)

            repository.saveUserProfile(updated)
            onTriggerDeltaSync()

            showMessage(
                "Autenticação biométrica confirmada " +
                    "(${updated.email})! " +
                    "Bem-vindo(a), ${updated.name}."
            )

            onSuccess()
        }
    }
}