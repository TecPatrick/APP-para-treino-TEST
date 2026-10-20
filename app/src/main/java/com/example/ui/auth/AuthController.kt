package com.example.ui.auth

import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.*
import com.example.data.repository.VirtusRepository
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
    fun clearAuthError() { _authError.value = null }

    private fun launchAuth(block: suspend CoroutineScope.() -> Unit) {
        if (!_isAuthLoading.compareAndSet(false, true)) return
        scope.launch {
            try {
                _authError.value = null
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = "Não foi possível concluir a operação. Tente novamente."
                _authError.value = message
                showMessage(message)
            } finally {
                _isAuthLoading.value = false
            }
        }
    }

    fun loginWithGoogleCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit
    ) {
        launchAuth {
            _authError.value = null
            val result = authService.signInWithGoogle(credential)

            result.onSuccess { user ->
                val current = profile()
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
                showMessage("Login com Google realizado com sucesso! Bem-vindo, ${user.displayName ?: current.name}.")
                onSuccess()
            }.onFailure { ex ->
                val errorMsg = ex.message ?: "Erro ao realizar login com Google."
                _authError.value = errorMsg
                showMessage(errorMsg)
            }
        }
    }

    fun loginWithFirebase(
        emailInput: String,
        passwordInput: String,
        onSuccess: () -> Unit
    ) {
        launchAuth {
            _authError.value = null
            val result = authService.login(emailInput, passwordInput)
            
            result.onSuccess { user ->
                val current = profile()
                val hasOnboarded = true
                val fallbackName = if (current.name.isNotBlank() && current.name != "Atleta") {
                    current.name
                } else {
                    emailInput.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                }
                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = hasOnboarded,
                    email = user.email ?: emailInput,
                    name = user.displayName?.takeIf { it.isNotBlank() } ?: fallbackName,
                    firebaseUid = user.uid
                )
                repository.saveUserProfile(updated)
                firestoreSync.syncProfileToCloud(updated)
                showMessage("Login realizado com sucesso! Bem-vindo, ${updated.name}.")
                onSuccess()
            }.onFailure { ex ->
                val message = ex.message ?: "Não foi possível entrar. Verifique suas credenciais."
                _authError.value = message
                showMessage(message)
            }
        }
    }

    fun enterAsGuest(onSuccess: () -> Unit) {
        launchAuth {
            authService.signOut()
            val current = profile()
            val hasOnboarded = current.hasCompletedOnboarding || current.weightKg > 30f
            val updated = current.copy(
                isLoggedIn = true,
                hasCompletedOnboarding = hasOnboarded,
                email = if (current.email.isNotBlank() && current.email.contains("@")) current.email else "atleta@growth.local",
                name = if (current.name.isNotBlank() && current.name != "Atleta") current.name else "Atleta Growth",
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
                _authError.value = "Vincule uma conta com e-mail antes de ativar o login biométrico."
                showMessage("Vincule seu e-mail no Perfil para usar a biometria.")
                return@launchAuth
            }
            if (current.email.isBlank() || !current.email.contains("@")) {
                _authError.value = "Perfil sem e-mail válido vinculado. Todo perfil precisa ter um e-mail válido."
                showMessage("Erro: E-mail obrigatório para acesso via biometria.")
                return@launchAuth
            }
            val hasValidProfile = current.hasCompletedOnboarding && current.weightKg > 30f && current.name.isNotBlank() && current.name != "Atleta"
            if (!hasValidProfile) {
                _authError.value = "Perfil incompleto. Configure seu perfil e cadastre suas medidas antes de usar a biometria."
                showMessage("Por favor, conclua o cadastro do perfil.")
                return@launchAuth
            }
            val updated = current.copy(isLoggedIn = true)
            repository.saveUserProfile(updated)
            showMessage("Autenticação biométrica confirmada (${updated.email})! Bem-vindo(a), ${updated.name}.")
            onSuccess()
        }
    }

    fun registerWithFirebase(
        name: String,
        email: String,
        password: String,
        gender: Gender,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        biotype: BodyType,
        trainingDays: Int,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        isAthlete: Boolean = true,
        onSuccess: () -> Unit
    ) {
        launchAuth {
            _authError.value = null
            val result = authService.register(email, password, name)

            val user = result.getOrElse { error ->
                val message = error.message ?: "Não foi possível criar a conta."
                _authError.value = message
                showMessage(message)
                return@launchAuth
            }
            val plan = MacroCalculator.computeFullPlan(
                weightKg = weightKg,
                heightCm = heightCm,
                age = age,
                gender = gender,
                activityLevel = activityLevel,
                goal = goal,
                biotype = biotype,
                customProteinGPerKg = 2.2f,
                customFatGPerKg = 0.8f,
                isAthleteOrWarmClimate = isAthlete
            )

            val updated = UserProfile(
                id = 1,
                name = name.ifBlank { "Atleta" },
                email = user.email ?: email,
                firebaseUid = user.uid,
                passwordHash = "",
                isLoggedIn = true,
                hasCompletedOnboarding = true,
                lgpdConsentAccepted = true,
                lgpdConsentDate = System.currentTimeMillis(),
                gender = gender,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                biotype = biotype,
                trainingDaysPerWeek = trainingDays,
                activityLevel = activityLevel,
                goal = goal,
                targetCalories = plan.targetCalories,
                targetProteinGrams = plan.proteinGrams,
                targetCarbsGrams = plan.carbsGrams,
                targetFatGrams = plan.fatGrams,
                targetWaterMl = plan.waterMl,
                monthlyTargetWeightKg = if (goal.calorieAdjustment > 0) weightKg + 2.0f else if (goal.calorieAdjustment < 0) weightKg - 2.5f else weightKg
            )

            repository.saveUserProfile(updated)
            firestoreSync.syncProfileToCloud(updated)
            showMessage("Conta criada com sucesso! Protocolo gerado.")
            onSuccess()
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String) -> Unit) {
        launchAuth {
            val result = authService.sendPasswordReset(email)
            result.onSuccess {
                val msg = "E-mail de recuperação enviado com sucesso para $email!"
                showMessage(msg)
                onComplete(true, msg)
            }.onFailure { ex ->
                val errorMsg = ex.message ?: "Falha ao enviar e-mail de recuperação."
                showMessage(errorMsg)
                onComplete(false, errorMsg)
            }
        }
    }
}
