package com.example.ui.auth

import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.*
import com.example.data.repository.VirtusRepository
import com.example.util.AuthValidator
import com.example.util.MacroCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
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
    private val showMessage: (String) -> Unit
) {
    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

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
                val message =
                    "Não foi possível concluir a operação. Tente novamente."

                _authError.value = message
                showMessage(message)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    // Login state is persisted before notifying the UI.
    fun loginWithGoogleCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit
    ) {
        launchAuth {
            _authError.value = null

            val result = authService.signInWithGoogle(credential)

            result.onSuccess { user ->
                val current = localProfileForUser(user.uid)

                val hasOnboarded = true

                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = hasOnboarded,
                    email = user.email ?: current.email,
                    name = user.displayName ?: current.name,
                    firebaseUid = user.uid
                )

                repository.saveUserProfile(updated)
                firestoreSync.syncProfileToCloud(updated)

                showMessage(
                    "Login com Google realizado com sucesso! " +
                        "Bem-vindo, ${user.displayName ?: current.name}."
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

            showMessage("Bem-vindo! Modo local ativado.")
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

            showMessage(
                "Autenticação biométrica confirmada " +
                    "(${updated.email})! " +
                    "Bem-vindo(a), ${updated.name}."
            )

            onSuccess()
        }
    }
}