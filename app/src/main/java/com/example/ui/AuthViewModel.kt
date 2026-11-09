package com.example.ui

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.data.auth.GoogleCredentialAuthHelper
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.ProgressLog
import com.example.data.model.UserProfile
import com.example.util.AppLog
import com.example.util.MacroCalculator
import com.google.firebase.auth.AuthCredential
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class AuthViewModel(application: Application) : BaseVirtusDomainViewModel(application) {

    val isAuthLoading: StateFlow<Boolean> get() = authController.isAuthLoading
    val authError: StateFlow<String?> get() = authController.authError

    fun clearAuthError() = authController.clearAuthError()

    fun restoreActiveFirebaseSession(onRestored: () -> Unit = {}) =
        authController.restoreActiveFirebaseSession {
            shared.triggerAutomaticWorkoutExerciseMigration()
            onRestored()
        }

    fun loginWithGoogleCredential(
        credential: AuthCredential,
        onSuccess: () -> Unit
    ) = authController.loginWithGoogleCredential(credential, onSuccess)

    fun enterAsGuest(onSuccess: () -> Unit) = authController.enterAsGuest(onSuccess)

    fun loginWithBiometrics(onSuccess: () -> Unit) = authController.loginWithBiometrics(onSuccess)

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val current = userProfile.value
                val updated = current.copy(isBiometricLockEnabled = enabled)
                repository.saveUserProfile(updated)
                if (updated.firebaseUid.isNotBlank() && !updated.firebaseUid.startsWith("guest_")) {
                    firestoreSync.recordLocalProfileMutation(updated.firebaseUid)
                    firestoreSync.syncProfileToCloud(updated)
                }
                showToast(
                    if (enabled) "Bloqueio por biometria ativado nos diários 🔒"
                    else "Bloqueio por biometria desativado"
                )
            } catch (e: Exception) {
                AppLog.e("AuthViewModel", "Não foi possível atualizar a biometria", e)
                showToast("Não foi possível atualizar a biometria: ${e.message}")
            }
        }
    }

    fun completeOnboardingProfile(
        name: String,
        email: String,
        gender: Gender,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        biotype: BodyType,
        trainingDays: Int,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        isAthlete: Boolean = true
    ) {
        viewModelScope.launch {
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
                name = name,
                email = email,
                isLoggedIn = true,
                hasCompletedOnboarding = true,
                lgpdConsentAccepted = true,
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
            showToast("Perfil e metas calculados com sucesso!")
        }
    }

    fun updateUserBiometrics(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        activityLevel: ActivityLevel,
        name: String = userProfile.value.name,
        gender: Gender = userProfile.value.gender,
        goal: FitnessGoal = userProfile.value.goal,
        biotype: BodyType = userProfile.value.biotype,
        recomputeMacros: Boolean = true,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val current = userProfile.value
            val isWeightChanged = abs(current.weightKg - weightKg) >= 0.05f

            val updated = if (recomputeMacros) {
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
                    isAthleteOrWarmClimate = true,
                    customTargetCalories = current.targetCalories
                )
                current.copy(
                    name = name.ifBlank { current.name },
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    gender = gender,
                    activityLevel = activityLevel,
                    goal = goal,
                    biotype = biotype,
                    targetCalories = plan.targetCalories,
                    targetProteinGrams = plan.proteinGrams,
                    targetCarbsGrams = plan.carbsGrams,
                    targetFatGrams = plan.fatGrams,
                    targetWaterMl = plan.waterMl
                )
            } else {
                current.copy(
                    name = name.ifBlank { current.name },
                    weightKg = weightKg,
                    heightCm = heightCm,
                    age = age,
                    gender = gender,
                    activityLevel = activityLevel,
                    goal = goal,
                    biotype = biotype
                )
            }

            repository.saveUserProfile(updated)

            if (updated.firebaseUid.isNotBlank() && !updated.firebaseUid.startsWith("guest_")) {
                firestoreSync.recordLocalProfileMutation(updated.firebaseUid)
                firestoreSync.syncProfileToCloud(updated)
                firestoreSync.syncCaloricGoalsToCloud(
                    targetCalories = updated.targetCalories,
                    targetProteinGrams = updated.targetProteinGrams,
                    targetCarbsGrams = updated.targetCarbsGrams,
                    targetFatGrams = updated.targetFatGrams,
                    targetWaterMl = updated.targetWaterMl,
                    customUid = updated.firebaseUid
                )
            }

            if (isWeightChanged) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                val now = System.currentTimeMillis()
                val log = ProgressLog(
                    dateMillis = now,
                    dateDisplay = dateFormat.format(Date(now)),
                    weightKg = weightKg,
                    notes = "Atualização de medidas biométricas"
                )
                repository.addProgressLog(log)
            }

            showToast(
                if (updated.firebaseUid.isNotBlank() && !updated.firebaseUid.startsWith("guest_")) {
                    "Medidas salvas e sincronizadas na nuvem! ☁️"
                } else {
                    "Medidas atualizadas com sucesso! 💪"
                }
            )
            onSuccess?.invoke()
        }
    }

    fun logout() {
        viewModelScope.launch {
            shared.stopCloudObservers()
            runCatching {
                repository.clearAllLocalData()
            }.onSuccess {
                authService.signOut()
                GoogleCredentialAuthHelper.clearCredentialManagerState(
                    context = getApplication(),
                    scope = viewModelScope
                )
                showToast("Você saiu da conta e as credenciais deste aparelho foram desconectadas.")
            }.onFailure {
                showToast(
                    "Não foi possível limpar os dados locais. " +
                        "A sessão foi mantida por segurança."
                )
            }
        }
    }

    fun clearAllUsersAndResetDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            shared.stopCloudObservers()
            runCatching { repository.clearAllLocalData() }
                .onSuccess {
                    authService.signOut()
                    showToast("Todos os dados locais foram excluídos.")
                    onComplete()
                }
                .onFailure {
                    showToast("Não foi possível excluir todos os dados locais.")
                }
        }
    }

    fun deleteAccountLgpd(onSuccess: () -> Unit) {
        viewModelScope.launch {
            shared.stopCloudObservers()
            val cloudResult = firestoreSync.deleteAllUserData()
            if (cloudResult.isFailure) {
                showToast(
                    cloudResult.exceptionOrNull()?.message
                        ?: "Não foi possível excluir os dados do servidor."
                )
                return@launch
            }

            val authResult = authService.deleteAccount()
            runCatching { repository.clearAllLocalData() }
            authService.signOut()

            if (authResult.isSuccess) {
                showToast("Conta e dados locais e remotos excluídos conforme a LGPD.")
                onSuccess()
            } else {
                showToast(
                    "Os dados foram apagados, mas o Firebase exige login recente para excluir a conta. Entre novamente e repita."
                )
            }
        }
    }
}
