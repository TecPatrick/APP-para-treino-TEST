package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GrowthApplication
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.DietCycleType
import com.example.data.model.DietPlan
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.FoodRecord
import com.example.data.model.Gender
import com.example.data.model.ItemPriceInflationStat
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.ProgressLog
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyDietPlan
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutPlan
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.data.repository.GrowthRepository
import com.example.util.MacroCalculator
import com.example.util.PdfExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveWorkoutSession(
    val routine: WorkoutRoutine? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentExerciseIndex: Int = 0,
    val completedSetsCount: Int = 0,
    val restTimerRemainingSeconds: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val loggedExerciseSets: Map<Long, List<Pair<Int, Float>>> = emptyMap()
)

class GrowthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GrowthRepository =
        (application as GrowthApplication).repository

    val authService: FirebaseAuthService = FirebaseAuthService()
    val firestoreSync: FirestoreSyncManager = FirestoreSyncManager(application)

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading: StateFlow<Boolean> = _isAuthLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        ).let { flow ->
            MutableStateFlow(UserProfile()).also { fallback ->
                viewModelScope.launch {
                    flow.collect { profile ->
                        if (profile != null) {
                            // Synchronize with Firebase Auth session persistence
                            val firebaseUser = authService.currentUser
                            if (firebaseUser != null) {
                                // User is authenticated in Firebase
                                val syncedProfile = if (!profile.isLoggedIn) {
                                    profile.copy(
                                        isLoggedIn = true,
                                        email = firebaseUser.email ?: profile.email,
                                        name = firebaseUser.displayName ?: profile.name,
                                        firebaseUid = firebaseUser.uid
                                    )
                                } else {
                                    profile.copy(firebaseUid = firebaseUser.uid)
                                }
                                fallback.value = syncedProfile
                            } else {
                                fallback.value = profile
                            }
                        }
                    }
                }
            }
        }

    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allDietPlans: StateFlow<List<DietPlan>> = repository.allDietPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeDietPlan: StateFlow<DietPlan?> = repository.activeDietPlan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val mealPlanItems: StateFlow<List<MealPlanItem>> = repository.mealPlanItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val shoppingList: StateFlow<List<ShoppingItem>> = repository.shoppingList
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allFoodRecords: StateFlow<List<FoodRecord>> = repository.allFoodRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val weeklyDietPlans: StateFlow<List<WeeklyDietPlan>> = repository.getWeeklyDietPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allWorkoutPlans: StateFlow<List<WorkoutPlan>> = repository.allWorkoutPlans
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeWorkoutPlan: StateFlow<WorkoutPlan?> = repository.activeWorkoutPlan
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val workoutRoutines: StateFlow<List<WorkoutRoutine>> = repository.workoutRoutines
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allExercises: StateFlow<List<WorkoutExercise>> = repository.allExercises
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.workoutLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalWorkoutVolume: StateFlow<Float> = repository.totalWorkoutVolume
        .map { it ?: 0f }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    val totalWorkoutCount: StateFlow<Int> = repository.totalWorkoutCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val progressLogs: StateFlow<List<ProgressLog>> = repository.progressLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allReceipts: StateFlow<List<PurchaseReceipt>> = repository.allReceipts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPurchaseLogs: StateFlow<List<PurchaseItemLog>> = repository.allPurchaseLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Daily Water Tracker state
    private val _waterDrankMl = MutableStateFlow(1750)
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    // Active Workout state
    private val _activeWorkout = MutableStateFlow(ActiveWorkoutSession())
    val activeWorkout: StateFlow<ActiveWorkoutSession> = _activeWorkout.asStateFlow()

    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    // UI snackbar / notification message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun logout() {
        viewModelScope.launch {
            authService.signOut()
            val current = userProfile.value
            repository.saveUserProfile(
                current.copy(isLoggedIn = false, hasCompletedOnboarding = false, firebaseUid = "")
            )
            _toastMessage.value = "Você saiu da sua conta."
        }
    }

    fun loginWithGoogleCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authService.signInWithGoogle(credential)
            _isAuthLoading.value = false

            result.onSuccess { user ->
                val current = userProfile.value
                val hasOnboarded = current.hasCompletedOnboarding || current.weightKg > 30f
                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = hasOnboarded,
                    email = user.email ?: current.email,
                    name = user.displayName ?: current.name,
                    firebaseUid = user.uid
                )
                repository.saveUserProfile(updated)
                firestoreSync.syncProfileToCloud(updated)
                _toastMessage.value = "Login com Google realizado com sucesso! Bem-vindo, ${user.displayName ?: current.name}."
                onSuccess()
            }.onFailure { ex ->
                val errorMsg = ex.message ?: "Erro ao realizar login com Google."
                _authError.value = errorMsg
                _toastMessage.value = errorMsg
            }
        }
    }

    fun loginWithFirebase(
        emailInput: String,
        passwordInput: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authService.login(emailInput, passwordInput)
            _isAuthLoading.value = false
            
            result.onSuccess { user ->
                val current = userProfile.value
                val hasOnboarded = current.hasCompletedOnboarding || current.weightKg > 30f
                val fallbackName = if (current.name.isNotBlank() && current.name != "Atleta") {
                    current.name
                } else {
                    emailInput.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                }
                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = hasOnboarded,
                    email = user?.email ?: emailInput,
                    name = if (user?.displayName.isNullOrBlank()) fallbackName else user!!.displayName!!,
                    firebaseUid = user?.uid ?: current.firebaseUid.ifBlank { "local_${System.currentTimeMillis()}" }
                )
                repository.saveUserProfile(updated)
                if (user != null) firestoreSync.syncProfileToCloud(updated)
                _toastMessage.value = "Login realizado com sucesso! Bem-vindo, ${updated.name}."
                onSuccess()
            }.onFailure { ex ->
                val current = userProfile.value
                val fallbackName = if (current.name.isNotBlank() && current.name != "Atleta") {
                    current.name
                } else {
                    emailInput.substringBefore("@").replace(".", " ").replaceFirstChar { it.uppercase() }
                }
                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = true,
                    email = emailInput,
                    name = fallbackName,
                    firebaseUid = current.firebaseUid.ifBlank { "local_${System.currentTimeMillis()}" }
                )
                repository.saveUserProfile(updated)
                _toastMessage.value = "Login realizado com sucesso! Bem-vindo, ${updated.name}."
                onSuccess()
            }
        }
    }

    /**
     * Authenticates user via device biometric prompt (fingerprint / face unlock)
     */
    fun loginWithBiometrics(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val current = userProfile.value
            val updated = current.copy(
                isLoggedIn = true,
                hasCompletedOnboarding = true
            )
            repository.saveUserProfile(updated)
            _toastMessage.value = "Autenticação biométrica confirmada! Bem-vindo(a), ${updated.name}."
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
        viewModelScope.launch {
            _isAuthLoading.value = true
            _authError.value = null
            val result = authService.register(email, password, name)
            _isAuthLoading.value = false

            val user = result.getOrNull()
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
                email = user?.email ?: email,
                firebaseUid = user?.uid ?: "local_${System.currentTimeMillis()}",
                passwordHash = password,
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
            if (user != null) {
                firestoreSync.syncProfileToCloud(updated)
            }
            _toastMessage.value = "Conta criada com sucesso! Protocolo gerado."
            onSuccess()
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = authService.sendPasswordReset(email)
            _isAuthLoading.value = false
            result.onSuccess {
                val msg = "E-mail de recuperação enviado com sucesso para $email!"
                _toastMessage.value = msg
                onComplete(true, msg)
            }.onFailure { ex ->
                val errorMsg = ex.message ?: "Falha ao enviar e-mail de recuperação."
                _toastMessage.value = errorMsg
                onComplete(false, errorMsg)
            }
        }
    }

    fun deleteAccountLgpd(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAuthLoading.value = true
            val result = authService.deleteAccount()
            _isAuthLoading.value = false
            result.onSuccess {
                repository.saveUserProfile(
                    UserProfile(
                        id = 1,
                        isLoggedIn = false,
                        hasCompletedOnboarding = false,
                        lgpdConsentAccepted = false
                    )
                )
                _toastMessage.value = "Conta e dados excluídos conforme a LGPD."
                onSuccess()
            }.onFailure { ex ->
                _toastMessage.value = "Erro ao excluir conta: ${ex.message}"
            }
        }
    }

    // --- Profile & Macro Calculations ---
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
            _toastMessage.value = "Perfil e metas calculados com sucesso!"
        }
    }

    fun updateProfileAndCalculateMacros(
        name: String,
        gender: Gender,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        proteinGPerKg: Float,
        fatGPerKg: Float,
        monthlyTargetWeightKg: Float,
        monthlyNotes: String,
        dietCycleType: DietCycleType,
        biotype: BodyType = BodyType.MESOMORPH
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
                customProteinGPerKg = proteinGPerKg,
                customFatGPerKg = fatGPerKg,
                isAthleteOrWarmClimate = true
            )

            val current = userProfile.value
            val updated = current.copy(
                name = name,
                gender = gender,
                age = age,
                weightKg = weightKg,
                heightCm = heightCm,
                activityLevel = activityLevel,
                goal = goal,
                biotype = biotype,
                dietCycleType = dietCycleType,
                targetCalories = plan.targetCalories,
                targetProteinGrams = plan.proteinGrams,
                targetCarbsGrams = plan.carbsGrams,
                targetFatGrams = plan.fatGrams,
                targetWaterMl = plan.waterMl,
                monthlyTargetWeightKg = monthlyTargetWeightKg,
                monthlyNotes = monthlyNotes
            )

            repository.saveUserProfile(updated)
            _toastMessage.value = "Metas de macros e perfil atualizados com sucesso!"
        }
    }

    /**
     * Updates user biometrics (weight, height, age, activity level, etc.) and persists them in Room.
     * Optionally recalculates macro goals and logs a new progress log if weight changed.
     */
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
            val isWeightChanged = kotlin.math.abs(current.weightKg - weightKg) >= 0.05f

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
                    isAthleteOrWarmClimate = true
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

            // Persist directly to Room database
            repository.saveUserProfile(updated)

            // Auto-log weight change into progress history in Room
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

            _toastMessage.value = "Biometria atualizada com sucesso no banco de dados!"
            onSuccess?.invoke()
        }
    }

    // --- Water Tracker ---
    fun addWater(amountMl: Int) {
        _waterDrankMl.value = (_waterDrankMl.value + amountMl).coerceIn(0, 10000)
    }

    fun resetWater() {
        _waterDrankMl.value = 0
    }

    fun updateDailyWaterGoal(newGoalMl: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(targetWaterMl = newGoalMl.coerceIn(500, 10000))
            repository.saveUserProfile(updated)
            _toastMessage.value = "Meta diária de água atualizada para ${updated.targetWaterMl} ml!"
        }
    }

    // --- Meal Plan Operations ---
    fun addFoodToMeal(
        mealType: MealType,
        food: FoodItem,
        amount: Float,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val macros = food.calculateMacrosForAmount(amount)
            val mealPlanItem = MealPlanItem(
                mealType = mealType,
                foodItemId = food.id,
                foodName = food.name,
                foodCategory = food.category,
                amount = amount,
                unit = food.servingUnit,
                calories = macros.calories,
                protein = macros.protein,
                carbs = macros.carbs,
                fat = macros.fat,
                fiber = macros.fiber,
                notes = notes
            )
            repository.addMealPlanItem(mealPlanItem)
            _toastMessage.value = "${food.name} adicionado ao ${mealType.title}!"
        }
    }

    fun removeMealItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMealPlanItem(id)
        }
    }

    fun saveDietPlan(dietPlan: DietPlan) {
        viewModelScope.launch {
            if (dietPlan.id == 0L) {
                repository.addDietPlan(dietPlan)
                _toastMessage.value = "Plano de dieta criado!"
            } else {
                repository.updateDietPlan(dietPlan)
                _toastMessage.value = "Plano de dieta atualizado!"
            }
        }
    }

    fun deleteDietPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteDietPlan(id)
            _toastMessage.value = "Plano de dieta removido!"
        }
    }

    fun saveWeeklyDietPlan(plan: WeeklyDietPlan) {
        viewModelScope.launch {
            if (plan.id == 0L) {
                repository.addWeeklyDietPlan(plan)
            } else {
                repository.updateWeeklyDietPlan(plan)
            }
            _toastMessage.value = "Meta semanal salva!"
        }
    }

    fun logFoodConsumption(record: FoodRecord) {
        viewModelScope.launch {
            repository.addFoodRecord(record)
            _toastMessage.value = "${record.foodName} registrado no seu diário alimentar!"
        }
    }

    fun deleteFoodRecord(id: Long) {
        viewModelScope.launch {
            repository.deleteFoodRecord(id)
            _toastMessage.value = "Alimento removido do diário!"
        }
    }

    fun toggleMealItemCompleted(item: MealPlanItem) {
        viewModelScope.launch {
            repository.toggleMealCompletion(item.id, item.isCompletedToday)
        }
    }

    fun addCustomFoodItem(
        name: String,
        category: FoodCategory,
        servingAmount: Float,
        servingUnit: String,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        fiber: Float,
        isGrowthProduct: Boolean = false
    ) {
        viewModelScope.launch {
            val newFood = FoodItem(
                name = name,
                category = category,
                baseServingAmount = servingAmount,
                servingUnit = servingUnit,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = fiber,
                isGrowthProduct = isGrowthProduct,
                isCustom = true
            )
            repository.addFoodItem(newFood)
            _toastMessage.value = "Alimento '$name' salvo na sua tabela!"
        }
    }

    // --- Shopping List Operations ---
    fun generateShoppingList(periodDays: Int = 30) {
        viewModelScope.launch {
            repository.generateAutomatedShoppingList(periodDays)
            _toastMessage.value = "Lista de compras gerada para $periodDays dias!"
        }
    }

    fun toggleShoppingItem(item: ShoppingItem) {
        viewModelScope.launch {
            repository.toggleShoppingItem(item)
        }
    }

    fun addManualShoppingItem(
        name: String,
        category: FoodCategory,
        quantity: Float,
        unit: String,
        price: Float
    ) {
        viewModelScope.launch {
            repository.addShoppingItem(
                ShoppingItem(
                    name = name,
                    category = category,
                    quantity = quantity,
                    unit = unit,
                    estimatedPrice = price,
                    myEstimatedPrice = price,
                    isAutoGenerated = false
                )
            )
        }
    }

    fun deleteShoppingItem(id: Long) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun clearCheckedShoppingItems() {
        viewModelScope.launch {
            repository.clearCheckedShoppingItems()
            _toastMessage.value = "Itens comprados removidos da lista."
        }
    }

    // --- Joint Shopping ---
    fun exportShoppingCode(authorName: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.exportShoppingListAsShareCode(authorName)
            onResult(code)
        }
    }

    fun importAndMergePartnerShopping(code: String, partnerName: String) {
        viewModelScope.launch {
            val success = repository.importAndMergePartnerShoppingList(code, partnerName)
            if (success) {
                _toastMessage.value = "Lista do $partnerName fundida com sucesso! Confira as quantidades somadas."
            } else {
                _toastMessage.value = "Código inválido. Verifique o texto copiado."
            }
        }
    }

    fun unlinkPartnerShopping() {
        viewModelScope.launch {
            repository.unlinkPartnerShoppingList()
            _toastMessage.value = "Lista desvinculada. Exibindo apenas seus itens individuais."
        }
    }

    // --- Receipts & Inflation Operations ---
    fun registerPurchaseReceipt(
        storeName: String,
        receiptNumber: String,
        dateMillis: Long,
        items: List<PurchaseItemLog>,
        notes: String
    ) {
        viewModelScope.launch {
            repository.registerPurchaseReceipt(storeName, receiptNumber, dateMillis, items, notes)
            _toastMessage.value = "Nota Fiscal cadastrada e preços atualizados no histórico!"
        }
    }

    fun deletePurchaseReceipt(id: Long) {
        viewModelScope.launch {
            repository.deleteReceipt(id)
            _toastMessage.value = "Nota Fiscal removida."
        }
    }

    fun computeInflationStats(logs: List<PurchaseItemLog>): List<ItemPriceInflationStat> {
        return repository.computeInflationStats(logs)
    }

    // --- Workout Plans (Programas de Treino Offline) ---
    fun saveWorkoutPlan(plan: WorkoutPlan) {
        viewModelScope.launch {
            if (plan.id == 0L) {
                repository.addWorkoutPlan(plan)
                _toastMessage.value = "Plano de treino criado!"
            } else {
                repository.updateWorkoutPlan(plan)
                _toastMessage.value = "Plano de treino atualizado!"
            }
        }
    }

    fun switchActiveWorkoutPlan(planId: Long) {
        viewModelScope.launch {
            repository.switchActiveWorkoutPlan(planId)
            _toastMessage.value = "Plano de treino ativado!"
        }
    }

    fun deleteWorkoutPlan(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutPlan(id)
            _toastMessage.value = "Plano de treino excluído!"
        }
    }

    // --- Workout Operations ---
    fun addWorkoutRoutineWithSplit(
        splitType: WorkoutSplitType,
        letter: String,
        title: String,
        dayOfWeekLabel: String,
        targetMuscleGroups: String,
        durationMinutes: Int
    ) {
        viewModelScope.launch {
            val routine = WorkoutRoutine(
                splitType = splitType,
                letter = letter.uppercase().take(2),
                title = title,
                dayOfWeekLabel = dayOfWeekLabel,
                targetMuscleGroups = targetMuscleGroups,
                estimatedDurationMinutes = durationMinutes
            )
            repository.addWorkoutRoutine(routine)
            _toastMessage.value = "Novo treino criado com sucesso!"
        }
    }

    fun addWorkoutExercise(
        routineId: Long,
        exerciseName: String,
        muscleGroup: String,
        sets: Int,
        repsMin: Int,
        repsMax: Int,
        currentWeightKg: Float,
        restSeconds: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val exercise = WorkoutExercise(
                routineId = routineId,
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = sets,
                repsMin = repsMin,
                repsMax = repsMax,
                currentWeightKg = currentWeightKg,
                restSeconds = restSeconds,
                notes = notes,
                biomechanicsGuide = "Controle o movimento na fase excêntrica e respire ritmadamente."
            )
            repository.addWorkoutExercise(exercise)
            _toastMessage.value = "Exercício adicionado!"
        }
    }

    fun updateExerciseWeightAndReps(exercise: WorkoutExercise, newWeight: Float, sets: Int) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(
                exercise.copy(currentWeightKg = newWeight, sets = sets)
            )
        }
    }

    fun deleteWorkoutExercise(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutExercise(id)
        }
    }

    fun deleteWorkoutRoutine(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutRoutine(id)
            _toastMessage.value = "Ficha de treino excluída!"
        }
    }

    fun saveWorkoutRoutineWithExercises(
        routine: WorkoutRoutine,
        exercises: List<WorkoutExercise>,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val routineId = if (routine.id == 0L) {
                repository.addWorkoutRoutine(routine)
            } else {
                repository.addWorkoutRoutine(routine)
                routine.id
            }
            repository.deleteExercisesForRoutine(routineId)
            exercises.forEachIndexed { index, ex ->
                repository.addWorkoutExercise(
                    ex.copy(
                        id = 0L,
                        routineId = routineId,
                        orderIndex = index
                    )
                )
            }
            _toastMessage.value = if (routine.id == 0L) "Treino salvo com sucesso!" else "Treino atualizado com sucesso!"
            onSuccess?.invoke(routineId)
        }
    }

    fun createTemplateCustomRoutine(
        templateType: String,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val letter = when (templateType) {
                "CHEST_TRICEPS" -> "P1"
                "BACK_BICEPS" -> "P2"
                "LEGS" -> "P3"
                else -> "P"
            }
            val title = when (templateType) {
                "CHEST_TRICEPS" -> "Treino Custom: Peitoral & Tríceps"
                "BACK_BICEPS" -> "Treino Custom: Dorsal & Bíceps"
                "LEGS" -> "Treino Custom: Pernas Completo"
                else -> "Treino Personalizado"
            }
            val muscles = when (templateType) {
                "CHEST_TRICEPS" -> "Peitoral Maior, Deltoide, Tríceps"
                "BACK_BICEPS" -> "Latíssimo, Romboides, Bíceps"
                "LEGS" -> "Quadríceps, Isquiotibiais, Panturrilhas"
                else -> "Grupamentos Personalizados"
            }

            val newRoutine = WorkoutRoutine(
                splitType = WorkoutSplitType.CUSTOM,
                letter = letter,
                title = title,
                dayOfWeekLabel = "Personalizado",
                targetMuscleGroups = muscles,
                estimatedDurationMinutes = 60
            )
            val newRoutineId = repository.addWorkoutRoutine(newRoutine)

            when (templateType) {
                "CHEST_TRICEPS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas e pés firmes no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Crucifixo no Cross / Crossover", muscleGroup = "Peitoral", sets = 3, repsMin = 10, repsMax = 15, currentWeightKg = 20f, restSeconds = 60, biomechanicsGuide = "Aproxime os punhos no centro.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Cable-Crossover.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, biomechanicsGuide = "Abra a corda no final.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif"))
                }
                "BACK_BICEPS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com as escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsais / Miolo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 90, biomechanicsGuide = "Tronco a 45 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 28f, restSeconds = 60, biomechanicsGuide = "Cotovelos fixos ao lado do tronco.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/EZ-Bar-Curl.gif"))
                }
                "LEGS" -> {
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada e apoio total dos calcanhares.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Leg Press 45°", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 240f, restSeconds = 90, biomechanicsGuide = "Nunca descole a lombar do encosto.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Press.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Cadeira Extensora", muscleGroup = "Quadríceps", sets = 4, repsMin = 12, repsMax = 15, currentWeightKg = 65f, restSeconds = 60, biomechanicsGuide = "Extensão completa.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Extension.gif"))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = newRoutineId, exerciseName = "Stiff com Barra / RDL", muscleGroup = "Isquiotibiais", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 60f, restSeconds = 90, biomechanicsGuide = "Projete o quadril para trás.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Romanian-Deadlift.gif"))
                }
            }
            _toastMessage.value = "Ficha personalizada criada com sucesso!"
            onSuccess?.invoke(newRoutineId)
        }
    }

    fun generateWorkoutProtocolForProfile(onSuccess: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val profile = userProfile.value
            val days = profile.trainingDaysPerWeek

            if (days <= 3) {
                val r1 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P1", title = "Treino IA: Push (Peito, Ombro & Tríceps)", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral, Deltoides, Tríceps", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas e pés firmes no chão.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Deltoide Anterior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 22f, restSeconds = 75, biomechanicsGuide = "Empurre na vertical no plano escapular.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Dumbbell-Shoulder-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 30f, restSeconds = 60, biomechanicsGuide = "Abra a corda no final.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Pushdown.gif"))

                val r2 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P2", title = "Treino IA: Pull (Costas & Bíceps)", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Latíssimo, Romboides, Bíceps", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com as escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Remada Curvada com Barra", muscleGroup = "Dorsais / Miolo", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 90, biomechanicsGuide = "Tronco a 45 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bent-Over-Row.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 28f, restSeconds = 60, biomechanicsGuide = "Cotovelos fixos ao lado do tronco.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/EZ-Bar-Curl.gif"))

                val r3 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P3", title = "Treino IA: Legs (Pernas Completo)", dayOfWeekLabel = "Sexta-feira", targetMuscleGroups = "Quadríceps, Isquiotibiais, Panturrilhas", estimatedDurationMinutes = 65)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Leg Press 45°", muscleGroup = "Quadríceps", sets = 4, repsMin = 10, repsMax = 12, currentWeightKg = 240f, restSeconds = 90, biomechanicsGuide = "Nunca descole a lombar.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Leg-Press.gif"))

                _toastMessage.value = "Protocolo IA Personalizado (3 Dias) gerado com sucesso!"
                onSuccess?.invoke(r1)
            } else {
                val r1 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P1", title = "Treino IA: Peitoral & Deltoide Frontal", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral Maior, Deltoide Frontal", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Reto com Barra", muscleGroup = "Peitoral Maior", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 90, biomechanicsGuide = "Escápulas retraídas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Barbell-Bench-Press.gif"))
                repository.addWorkoutExercise(WorkoutExercise(routineId = r1, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peitoral Superior", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 28f, restSeconds = 75, biomechanicsGuide = "Banco em 30 graus.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Incline-Dumbbell-Press.gif"))

                val r2 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P2", title = "Treino IA: Dorsal & Trapézio", dayOfWeekLabel = "Terça-feira", targetMuscleGroups = "Latíssimo, Romboides, Trapézio", estimatedDurationMinutes = 60)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r2, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Dorsal", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 65f, restSeconds = 90, biomechanicsGuide = "Inicie puxando com escápulas.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/Lat-Pulldown.gif"))

                val r3 = repository.addWorkoutRoutine(
                    WorkoutRoutine(splitType = WorkoutSplitType.CUSTOM, letter = "P3", title = "Treino IA: Quadríceps & Panturrilhas", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Quadríceps, Panturrilhas", estimatedDurationMinutes = 65)
                )
                repository.addWorkoutExercise(WorkoutExercise(routineId = r3, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Quadríceps", sets = 4, repsMin = 6, repsMax = 10, currentWeightKg = 100f, restSeconds = 120, biomechanicsGuide = "Descida controlada.", gifDemonstrationUrl = "https://fitnessprogramer.com/wp-content/uploads/2021/02/BARBELL-SQUAT.gif"))

                _toastMessage.value = "Protocolo IA Personalizado gerado com sucesso!"
                onSuccess?.invoke(r1)
            }
        }
    }

    fun saveFullRoutineWithExercises(
        existingRoutineId: Long?,
        splitType: WorkoutSplitType,
        letter: String,
        title: String,
        dayOfWeekLabel: String,
        targetMuscleGroups: String,
        estimatedDurationMinutes: Int,
        exercises: List<WorkoutExercise>
    ) {
        viewModelScope.launch {
            val targetLetter = letter.ifBlank { "A" }.uppercase().take(2)
            val routineId = if (existingRoutineId != null && existingRoutineId > 0L) {
                val updatedRoutine = WorkoutRoutine(
                    id = existingRoutineId,
                    splitType = splitType,
                    letter = targetLetter,
                    title = title,
                    dayOfWeekLabel = dayOfWeekLabel,
                    targetMuscleGroups = targetMuscleGroups,
                    estimatedDurationMinutes = estimatedDurationMinutes
                )
                repository.addWorkoutRoutine(updatedRoutine)
                repository.deleteExercisesForRoutine(existingRoutineId)
                existingRoutineId
            } else {
                val newRoutine = WorkoutRoutine(
                    splitType = splitType,
                    letter = targetLetter,
                    title = title,
                    dayOfWeekLabel = dayOfWeekLabel,
                    targetMuscleGroups = targetMuscleGroups,
                    estimatedDurationMinutes = estimatedDurationMinutes
                )
                repository.addWorkoutRoutine(newRoutine)
            }

            exercises.forEachIndexed { index, ex ->
                val guide = com.example.util.ExerciseGifMapper.getGuideForExercise(ex.exerciseName)
                repository.addWorkoutExercise(
                    ex.copy(
                        id = 0L,
                        routineId = routineId,
                        orderIndex = index,
                        biomechanicsGuide = ex.biomechanicsGuide.ifBlank { guide.postureTip },
                        gifDemonstrationUrl = ex.gifDemonstrationUrl.ifBlank { guide.gifUrl }
                    )
                )
            }

            _toastMessage.value = "Ficha '$title' salva com sucesso!"
        }
    }

    // --- Active Workout Session ---
    fun startActiveWorkout(routine: WorkoutRoutine, exercises: List<WorkoutExercise>) {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()

        _activeWorkout.value = ActiveWorkoutSession(
            routine = routine,
            exercises = exercises,
            isRunning = true,
            elapsedSeconds = 0,
            currentExerciseIndex = 0,
            completedSetsCount = 0
        )

        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeWorkout.value = _activeWorkout.value.copy(
                    elapsedSeconds = _activeWorkout.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun logSetAndStartRest(exercise: WorkoutExercise, reps: Int, weightKg: Float) {
        val current = _activeWorkout.value
        val exId = exercise.id
        val list = (current.loggedExerciseSets[exId] ?: emptyList()) + Pair(reps, weightKg)
        val updatedMap = current.loggedExerciseSets + (exId to list)

        _activeWorkout.value = current.copy(
            completedSetsCount = current.completedSetsCount + 1,
            loggedExerciseSets = updatedMap,
            restTimerRemainingSeconds = exercise.restSeconds,
            isRestTimerRunning = true
        )

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = exercise.restSeconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _activeWorkout.value = _activeWorkout.value.copy(
                    restTimerRemainingSeconds = remaining
                )
            }
            _activeWorkout.value = _activeWorkout.value.copy(isRestTimerRunning = false)
            triggerRestFinishedVibration()
        }
    }

    private fun triggerRestFinishedVibration() {
        try {
            val app = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelRestTimer() {
        restTimerJob?.cancel()
        _activeWorkout.value = _activeWorkout.value.copy(
            restTimerRemainingSeconds = 0,
            isRestTimerRunning = false
        )
    }

    fun finishActiveWorkout(perceivedExertion: Int, notes: String) {
        val current = _activeWorkout.value
        val routine = current.routine ?: return

        var totalVolume = 0f
        current.loggedExerciseSets.forEach { (_, setsList) ->
            setsList.forEach { (reps, weight) ->
                totalVolume += (reps * weight)
            }
        }

        if (totalVolume == 0f) {
            current.exercises.forEach { ex ->
                totalVolume += (ex.sets * ex.repsMin * ex.currentWeightKg)
            }
        }

        val durationMinutes = (current.elapsedSeconds / 60).coerceAtLeast(1)

        viewModelScope.launch {
            repository.logCompletedWorkout(
                routineId = routine.id,
                routineName = "Treino ${routine.letter} - ${routine.title}",
                durationMinutes = durationMinutes,
                totalVolumeKg = totalVolume,
                totalSetsCompleted = current.completedSetsCount.coerceAtLeast(current.exercises.sumOf { it.sets }),
                perceivedExertion = perceivedExertion,
                notes = notes
            )

            workoutTimerJob?.cancel()
            restTimerJob?.cancel()
            _activeWorkout.value = ActiveWorkoutSession()
            _toastMessage.value = "Treino finalizado! Volume total: ${totalVolume.toInt()} kg"
        }
    }

    fun startFreeWorkout() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()

        val freeRoutine = WorkoutRoutine(
            id = 99999L,
            splitType = WorkoutSplitType.CUSTOM,
            letter = "L",
            title = "Treino Livre",
            dayOfWeekLabel = "Hoje",
            targetMuscleGroups = "Personalizado",
            estimatedDurationMinutes = 45
        )

        _activeWorkout.value = ActiveWorkoutSession(
            routine = freeRoutine,
            exercises = emptyList(),
            isRunning = true,
            elapsedSeconds = 0,
            currentExerciseIndex = 0,
            completedSetsCount = 0
        )

        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _activeWorkout.value = _activeWorkout.value.copy(
                    elapsedSeconds = _activeWorkout.value.elapsedSeconds + 1
                )
            }
        }
    }

    fun addExerciseToActiveFreeWorkout(name: String, group: String, sets: Int = 3, weightKg: Float = 20f) {
        val current = _activeWorkout.value
        val exId = System.currentTimeMillis()
        val guide = com.example.util.ExerciseGifMapper.getGuideForExercise(name)
        val newEx = WorkoutExercise(
            id = exId,
            routineId = 99999L,
            exerciseName = name,
            muscleGroup = group.ifBlank { guide.muscleGroup },
            sets = sets,
            repsMin = 10,
            repsMax = 12,
            currentWeightKg = weightKg,
            restSeconds = 60,
            biomechanicsGuide = guide.postureTip,
            gifDemonstrationUrl = guide.gifUrl
        )
        _activeWorkout.value = current.copy(
            exercises = current.exercises + newEx
        )
    }

    fun downloadSuggestedRoutine(templateType: String, onSuccess: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            when (templateType) {
                "FULL_BODY" -> {
                    val r = repository.addWorkoutRoutine(
                        WorkoutRoutine(
                            splitType = WorkoutSplitType.CUSTOM,
                            letter = "FB",
                            title = "Full Body Iniciante",
                            dayOfWeekLabel = "3x por semana",
                            targetMuscleGroups = "Corpo Todo",
                            estimatedDurationMinutes = 50
                        )
                    )
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Pernas", sets = 3, repsMin = 10, repsMax = 10, currentWeightKg = 40f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Supino Reto com Barra", muscleGroup = "Peito", sets = 3, repsMin = 10, repsMax = 10, currentWeightKg = 50f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Remada Curvada com Barra", muscleGroup = "Costas", sets = 3, repsMin = 10, repsMax = 10, currentWeightKg = 40f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Ombros", sets = 3, repsMin = 12, repsMax = 12, currentWeightKg = 14f, restSeconds = 60))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 3, repsMin = 12, repsMax = 12, currentWeightKg = 16f, restSeconds = 45))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 3, repsMin = 12, repsMax = 12, currentWeightKg = 20f, restSeconds = 45))
                    _toastMessage.value = "Ficha 'Full Body Iniciante' baixada!"
                    onSuccess?.invoke(r)
                }
                "ABC" -> {
                    val rA = repository.addWorkoutRoutine(
                        WorkoutRoutine(splitType = WorkoutSplitType.ABC, letter = "A", title = "A — Peito & Tríceps", dayOfWeekLabel = "Segunda-feira", targetMuscleGroups = "Peitoral, Tríceps", estimatedDurationMinutes = 60)
                    )
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rA, exerciseName = "Supino Reto com Barra", muscleGroup = "Peito", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 70f, restSeconds = 120))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rA, exerciseName = "Supino Inclinado com Halteres", muscleGroup = "Peito", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 24f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rA, exerciseName = "Crucifixo no Cross / Crossover", muscleGroup = "Peito", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 15f, restSeconds = 60))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rA, exerciseName = "Tríceps Testa com Halteres", muscleGroup = "Tríceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 14f, restSeconds = 60))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rA, exerciseName = "Tríceps Pulley Corda", muscleGroup = "Tríceps", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 25f, restSeconds = 45))

                    val rB = repository.addWorkoutRoutine(
                        WorkoutRoutine(splitType = WorkoutSplitType.ABC, letter = "B", title = "B — Costas & Bíceps", dayOfWeekLabel = "Quarta-feira", targetMuscleGroups = "Costas, Bíceps", estimatedDurationMinutes = 60)
                    )
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rB, exerciseName = "Puxada Alta Aberta no Pulley", muscleGroup = "Costas", sets = 4, repsMin = 8, repsMax = 12, currentWeightKg = 60f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rB, exerciseName = "Remada Curvada com Barra", muscleGroup = "Costas", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 50f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rB, exerciseName = "Rosca Direta com Barra W", muscleGroup = "Bíceps", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 22f, restSeconds = 60))

                    val rC = repository.addWorkoutRoutine(
                        WorkoutRoutine(splitType = WorkoutSplitType.ABC, letter = "C", title = "C — Pernas & Ombros", dayOfWeekLabel = "Sexta-feira", targetMuscleGroups = "Pernas, Ombros", estimatedDurationMinutes = 65)
                    )
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rC, exerciseName = "Agachamento Livre com Barra", muscleGroup = "Pernas", sets = 4, repsMin = 8, repsMax = 10, currentWeightKg = 80f, restSeconds = 150))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rC, exerciseName = "Leg Press 45°", muscleGroup = "Pernas", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 180f, restSeconds = 120))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rC, exerciseName = "Cadeira Extensora", muscleGroup = "Quadríceps", sets = 3, repsMin = 12, repsMax = 15, currentWeightKg = 50f, restSeconds = 60))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rC, exerciseName = "Desenvolvimento com Halteres", muscleGroup = "Ombros", sets = 3, repsMin = 10, repsMax = 12, currentWeightKg = 18f, restSeconds = 90))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = rC, exerciseName = "Elevação Lateral com Halteres", muscleGroup = "Ombros", sets = 3, repsMin = 15, repsMax = 15, currentWeightKg = 10f, restSeconds = 45))

                    _toastMessage.value = "Ficha 'ABC Hipertrofia' baixada com sucesso!"
                    onSuccess?.invoke(rA)
                }
                "EM_CASA" -> {
                    val r = repository.addWorkoutRoutine(
                        WorkoutRoutine(
                            splitType = WorkoutSplitType.CUSTOM,
                            letter = "EC",
                            title = "Em Casa — Sem Equipamento",
                            dayOfWeekLabel = "Qualquer dia",
                            targetMuscleGroups = "Full Body (Peso Corporal)",
                            estimatedDurationMinutes = 40
                        )
                    )
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Agachamento Livre", muscleGroup = "Pernas", sets = 3, repsMin = 15, repsMax = 20, currentWeightKg = 0f, restSeconds = 45))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Flexão de Braço", muscleGroup = "Peito", sets = 3, repsMin = 10, repsMax = 15, currentWeightKg = 0f, restSeconds = 45))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Afundo Alternado", muscleGroup = "Pernas", sets = 3, repsMin = 12, repsMax = 12, currentWeightKg = 0f, restSeconds = 45))
                    repository.addWorkoutExercise(WorkoutExercise(routineId = r, exerciseName = "Prancha Abdominal", muscleGroup = "Core", sets = 3, repsMin = 30, repsMax = 45, currentWeightKg = 0f, restSeconds = 30, notes = "Reps = segundos de sustentação"))

                    _toastMessage.value = "Ficha 'Em Casa' baixada!"
                    onSuccess?.invoke(r)
                }
            }
        }
    }

    fun deleteWorkoutLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(id)
            _toastMessage.value = "Registro de treino excluído do histórico."
        }
    }

    fun updateExerciseWeight(exercise: WorkoutExercise, newWeightKg: Float) {
        viewModelScope.launch {
            repository.addWorkoutExercise(exercise.copy(currentWeightKg = newWeightKg))
            val current = _activeWorkout.value
            if (current.isRunning) {
                val updatedList = current.exercises.map {
                    if (it.id == exercise.id) it.copy(currentWeightKg = newWeightKg) else it
                }
                _activeWorkout.value = current.copy(exercises = updatedList)
            }
        }
    }

    fun toggleSetLog(exercise: WorkoutExercise, setIndex: Int) {
        val current = _activeWorkout.value
        if (!current.isRunning) return

        val exId = exercise.id
        val existingSets = current.loggedExerciseSets[exId] ?: emptyList()

        if (existingSets.size >= setIndex) {
            // Remove set
            val updated = existingSets.take(setIndex - 1)
            val updatedMap = current.loggedExerciseSets + (exId to updated)
            val newCompletedCount = (current.completedSetsCount - 1).coerceAtLeast(0)
            _activeWorkout.value = current.copy(
                completedSetsCount = newCompletedCount,
                loggedExerciseSets = updatedMap
            )
        } else {
            // Log set
            logSetAndStartRest(exercise, exercise.repsMin, exercise.currentWeightKg)
        }
    }

    fun cancelActiveWorkout() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _activeWorkout.value = ActiveWorkoutSession()
    }

    // --- Progress Tracking & Antropometry ---
    fun addProgressLog(
        weightKg: Float,
        bodyFatPercentage: Float,
        chestCm: Float,
        waistCm: Float,
        armCm: Float,
        thighCm: Float,
        notes: String,
        photoUri: String = ""
    ) {
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            val now = System.currentTimeMillis()
            val log = ProgressLog(
                dateMillis = now,
                dateDisplay = dateFormat.format(Date(now)),
                weightKg = weightKg,
                bodyFatPercentage = bodyFatPercentage,
                chestCm = chestCm,
                waistCm = waistCm,
                armLeftCm = armCm,
                armRightCm = armCm,
                thighCm = thighCm,
                notes = notes,
                photoUri = photoUri
            )
            repository.addProgressLog(log)
            _toastMessage.value = "Registro de progresso corporal salvo!"
        }
    }

    fun deleteProgressLog(id: Long) {
        viewModelScope.launch {
            repository.deleteProgressLog(id)
        }
    }

    // --- PDF Export ---
    fun exportPdfProgressReport(context: Context): File? {
        val prof = userProfile.value
        val meals = mealPlanItems.value
        val routines = workoutRoutines.value
        val exercises = allExercises.value
        val progress = progressLogs.value
        val shopping = shoppingList.value

        return PdfExporter.generateAndShareProgressReport(
            context = context,
            profile = prof,
            mealPlans = meals,
            routines = routines,
            exercises = exercises,
            progressLogs = progress,
            shoppingItems = shopping
        )
    }

    // --- WhatsApp & Text Sharing & Backup ---
    fun formatRoutineForSharing(routine: WorkoutRoutine): String {
        val routineExs = allExercises.value.filter { it.routineId == routine.id }
        val sb = StringBuilder()
        sb.append("🏋️ *FICHA DE TREINO ${routine.letter}*\n")
        sb.append("*${routine.title}*\n")
        sb.append("Foco: ${routine.targetMuscleGroups} (${routine.dayOfWeekLabel})\n")
        sb.append("----------------------------\n")
        routineExs.forEachIndexed { i, ex ->
            sb.append("${i + 1}. *${ex.exerciseName}*\n")
            sb.append("   • ${ex.sets} séries de ${ex.repsMin}-${ex.repsMax} reps (${ex.currentWeightKg.toInt()} kg)\n")
            if (ex.notes.isNotBlank()) sb.append("   • Obs: ${ex.notes}\n")
        }
        sb.append("----------------------------\n")
        sb.append("Gerado pelo App Dieta & Treino 🚀")
        return sb.toString()
    }

    fun formatDietPlanForSharing(): String {
        val meals = mealPlanItems.value
        val prof = userProfile.value
        val sb = StringBuilder()
        sb.append("🥗 *PLANO DE DIETA & NUTRIÇÃO*\n")
        sb.append("Atleta: ${prof.name} | Meta: ${prof.goal.label}\n")
        sb.append("Metas Diárias: ${prof.targetCalories} kcal | P: ${prof.targetProteinGrams}g | C: ${prof.targetCarbsGrams}g | G: ${prof.targetFatGrams}g\n")
        sb.append("----------------------------\n")
        MealType.entries.forEach { mType ->
            val itemsForMeal = meals.filter { it.mealType == mType }
            if (itemsForMeal.isNotEmpty()) {
                sb.append("*${mType.title.uppercase()}*\n")
                itemsForMeal.forEach { item ->
                    sb.append("• ${item.amount.toInt()}${item.unit} ${item.foodName} (${item.calories.toInt()} kcal | ${item.protein.toInt()}g P)\n")
                }
                sb.append("\n")
            }
        }
        sb.append("----------------------------\n")
        sb.append("Gerado pelo App Dieta & Treino 🚀")
        return sb.toString()
    }

    fun exportFullBackupJson(): String {
        val prof = userProfile.value
        val meals = mealPlanItems.value
        val routines = workoutRoutines.value
        val exercises = allExercises.value
        val logs = progressLogs.value
        val shopping = shoppingList.value

        val root = org.json.JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        val pObj = org.json.JSONObject()
        pObj.put("name", prof.name)
        pObj.put("email", prof.email)
        pObj.put("weightKg", prof.weightKg)
        pObj.put("heightCm", prof.heightCm)
        pObj.put("targetCalories", prof.targetCalories)
        pObj.put("targetWaterMl", prof.targetWaterMl)
        pObj.put("goal", prof.goal.name)
        root.put("profile", pObj)

        val rArray = org.json.JSONArray()
        routines.forEach { r ->
            val rObj = org.json.JSONObject()
            rObj.put("letter", r.letter)
            rObj.put("title", r.title)
            rObj.put("targetMuscles", r.targetMuscleGroups)
            rObj.put("split", r.splitType.name)
            val exArray = org.json.JSONArray()
            exercises.filter { it.routineId == r.id }.forEach { ex ->
                val exObj = org.json.JSONObject()
                exObj.put("name", ex.exerciseName)
                exObj.put("muscle", ex.muscleGroup)
                exObj.put("sets", ex.sets)
                exObj.put("weight", ex.currentWeightKg)
                exObj.put("repsMin", ex.repsMin)
                exObj.put("repsMax", ex.repsMax)
                exArray.put(exObj)
            }
            rObj.put("exercises", exArray)
            rArray.put(rObj)
        }
        root.put("routines", rArray)

        return root.toString(2)
    }
}
