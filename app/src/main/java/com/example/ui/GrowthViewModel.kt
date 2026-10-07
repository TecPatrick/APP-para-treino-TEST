package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.GrowthApplication
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.DietCycleType
import com.example.data.model.FitnessGoal
import com.example.data.model.FoodCategory
import com.example.data.model.FoodItem
import com.example.data.model.Gender
import com.example.data.model.ItemPriceInflationStat
import com.example.data.model.MealPlanItem
import com.example.data.model.MealType
import com.example.data.model.ProgressLog
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.data.model.ShoppingItem
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
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
                val updated = current.copy(
                    isLoggedIn = true,
                    hasCompletedOnboarding = hasOnboarded,
                    email = user?.email ?: emailInput,
                    name = if (user?.displayName.isNullOrBlank()) current.name else user!!.displayName!!,
                    firebaseUid = user?.uid ?: current.firebaseUid.ifBlank { "local_${System.currentTimeMillis()}" }
                )
                repository.saveUserProfile(updated)
                if (user != null) firestoreSync.syncProfileToCloud(updated)
                _toastMessage.value = "Login realizado com sucesso! Bem-vindo, ${updated.name}."
                onSuccess()
            }.onFailure { ex ->
                val current = userProfile.value
                if (current.email.equals(emailInput.trim(), ignoreCase = true)) {
                    val updated = current.copy(isLoggedIn = true, hasCompletedOnboarding = true)
                    repository.saveUserProfile(updated)
                    _toastMessage.value = "Login realizado com sucesso! Bem-vindo, ${updated.name}."
                    onSuccess()
                } else {
                    val errorMsg = ex.message ?: "Erro ao realizar login."
                    _authError.value = errorMsg
                    _toastMessage.value = errorMsg
                }
            }
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
                name = name.ifBlank { "Atleta Growth" },
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
                onComplete(true, "E-mail de recuperação enviado com sucesso para $email!")
            }.onFailure { ex ->
                onComplete(false, ex.message ?: "Falha ao enviar e-mail de recuperação.")
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

    // --- Water Tracker ---
    fun addWater(amountMl: Int) {
        _waterDrankMl.value = (_waterDrankMl.value + amountMl).coerceIn(0, 10000)
    }

    fun resetWater() {
        _waterDrankMl.value = 0
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
        exercises: List<WorkoutExercise>
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
        }
    }

    fun createTemplateCustomRoutine(templateType: String) {
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
        notes: String
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
                notes = notes
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
}
