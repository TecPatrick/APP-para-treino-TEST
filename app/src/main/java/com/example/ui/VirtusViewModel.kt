package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import com.example.BuildConfig
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import org.json.JSONArray
import org.json.JSONObject
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.VirtusApplication
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.DietCycleType
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.DietPlan
import com.example.data.model.DietaryProfile
import com.example.data.model.Macro
import com.example.data.model.WorkoutSchedule
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
import com.example.data.model.GpsRouteLog
import com.example.data.model.CardioLog
import com.example.data.repository.VirtusRepository
import com.example.util.MacroCalculator
import com.example.util.PdfExporter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.ai.GeminiRepository
import com.example.service.RunningForegroundService
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyEnergyExpenditure(
    val bmr: Int,                       // TMB em repouso (parado sem treino no dia)
    val gymWorkoutCalories: Int,        // Gasto adicional dos treinos de musculação hoje
    val runningWorkoutCalories: Int,    // Gasto adicional da corrida GPS hoje
    val cardioWorkoutCalories: Int,     // Gasto adicional de cardios manuais hoje
    val totalWorkoutCalories: Int,      // Total gasto com treinos realizados hoje
    val totalExpenditure: Int,          // Gasto diário total (TMB se parado; TMB + Treinos se treinou)
    val hasWorkoutToday: Boolean,       // Indica se houve treino no dia
    val gymWorkoutsCount: Int,
    val runningWorkoutsCount: Int,
    val cardioWorkoutsCount: Int
)

data class ActiveWorkoutSession(
    val routine: WorkoutRoutine? = null,
    val exercises: List<WorkoutExercise> = emptyList(),
    val isRunning: Boolean = false,
    val elapsedSeconds: Int = 0,
    val currentExerciseIndex: Int = 0,
    val completedSetsCount: Int = 0,
    val restTimerRemainingSeconds: Int = 0,
    val isRestTimerRunning: Boolean = false,
    val isRestTimerFinished: Boolean = false,
    val loggedExerciseSets: Map<Long, List<Pair<Int, Float>>> = emptyMap()
)

data class ActiveGpsSession(
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val isAutoPaused: Boolean = false,
    val elapsedSeconds: Int = 0,
    val distanceMeters: Float = 0f,
    val stepsCount: Int = 0,
    val caloriesBurned: Float = 0f,
    val currentSpeedKmh: Float = 0f,
    val paceSecondsPerKm: Long = 0L,
    val targetPaceSecondsPerKm: Long = 0L,
    val isPaceAlertEnabled: Boolean = false,
    val routePoints: List<Pair<Double, Double>> = emptyList()
)

data class DietAiPreferences(
    val restrictions: String = "",
    val preferences: String = "",
    val mealsCount: Int = 5,
    val includePreferredFoods: Boolean = true
)

data class AiDietSuggestion(
    val mealType: MealType,
    val foodName: String,
    val amount: Float,
    val unit: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val notes: String = ""
)

class VirtusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: VirtusRepository =
        (application as VirtusApplication).repository

    val authService: FirebaseAuthService = FirebaseAuthService()
    val firestoreSync: FirestoreSyncManager = FirestoreSyncManager(application)

    private val authController by lazy {
        com.example.ui.auth.AuthController(viewModelScope, repository, authService, firestoreSync,
            { userProfile.value }, ::showToast)
    }
    val isAuthLoading: StateFlow<Boolean> get() = authController.isAuthLoading
    val authError: StateFlow<String?> get() = authController.authError
    fun clearAuthError() = authController.clearAuthError()

    private val _aiAdvice = MutableStateFlow<String?>(null)
    val aiAdvice: StateFlow<String?> = _aiAdvice.asStateFlow()

    fun getWorkoutAdvice(prompt: String) {
        viewModelScope.launch {
            _aiAdvice.value = "Consultando treinador IA..."
            _aiAdvice.value = GeminiRepository.getWorkoutAdvice(prompt)
        }
    }
    
    fun clearAdvice() {
        _aiAdvice.value = null
    }

    private val _isAiDietLoading = MutableStateFlow(false)
    val isAiDietLoading: StateFlow<Boolean> = _isAiDietLoading.asStateFlow()

    private val _aiDietSuggestions = MutableStateFlow<List<AiDietSuggestion>>(emptyList())
    val aiDietSuggestions: StateFlow<List<AiDietSuggestion>> = _aiDietSuggestions.asStateFlow()

    fun generateAIDietPlan(preferences: DietAiPreferences) {
        viewModelScope.launch {
            _isAiDietLoading.value = true
            try {
                val profile = userProfile.value
                val foods = allFoods.value
                val favs = foods.filter { it.isFavorite }.joinToString { it.name }
                val contextFoods = if (preferences.includePreferredFoods && favs.isNotBlank()) {
                    "Preferidos: $favs. Outros: ${foods.take(30).joinToString { it.name }}"
                } else {
                    foods.take(50).joinToString { it.name }
                }

                val json = GeminiRepository.generateDietPlan(
                    calories = profile.targetCalories,
                    protein = profile.targetProteinGrams,
                    carbs = profile.targetCarbsGrams,
                    fat = profile.targetFatGrams,
                    mealsCount = preferences.mealsCount,
                    restrictions = preferences.restrictions,
                    preferences = preferences.preferences,
                    availableFoods = contextFoods
                )

                if (json != null) {
                    val list = mutableListOf<AiDietSuggestion>()
                    val array = JSONArray(json)
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(
                            AiDietSuggestion(
                                mealType = MealType.valueOf(obj.getString("mealType")),
                                foodName = obj.getString("foodName"),
                                amount = obj.getDouble("amount").toFloat(),
                                unit = obj.getString("unit"),
                                calories = obj.getDouble("calories").toFloat(),
                                protein = obj.getDouble("protein").toFloat(),
                                carbs = obj.getDouble("carbs").toFloat(),
                                fat = obj.getDouble("fat").toFloat(),
                                notes = obj.optString("notes")
                            )
                        )
                    }
                    _aiDietSuggestions.value = list
                } else {
                    _toastMessage.value = "Falha ao gerar dieta com IA. Tente novamente."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.value = "Erro na IA: ${e.message}"
            } finally {
                _isAiDietLoading.value = false
            }
        }
    }

    fun applyAiDiet() {
        viewModelScope.launch {
            val suggestions = _aiDietSuggestions.value
            if (suggestions.isEmpty()) return@launch
            
            // Opcionalmente podemos limpar a dieta atual ou apenas adicionar
            // Para "Gerar Dieta" faz sentido limpar e aplicar a nova.
            // Mas talvez o usuário queira confirmar item a item.
            // Por enquanto, vamos aplicar tudo.
            
            suggestions.forEach { suggestion ->
                val matchingFood = allFoods.value.find { it.name.contains(suggestion.foodName, ignoreCase = true) }
                val mealItem = MealPlanItem(
                    mealType = suggestion.mealType,
                    foodItemId = matchingFood?.id ?: 0,
                    foodName = suggestion.foodName,
                    foodCategory = matchingFood?.category ?: FoodCategory.OTHER,
                    amount = suggestion.amount,
                    unit = suggestion.unit,
                    calories = suggestion.calories,
                    protein = suggestion.protein,
                    carbs = suggestion.carbs,
                    fat = suggestion.fat,
                    notes = suggestion.notes
                )
                repository.addMealPlanItem(mealItem)
            }
            _aiDietSuggestions.value = emptyList()
            _toastMessage.value = "Plano alimentar IA aplicado com sucesso!"
        }
    }

    fun suggestAIMealItems(mealType: MealType, restrictions: String = "", preferences: String = "") {
        viewModelScope.launch {
            _isAiDietLoading.value = true
            try {
                val profile = userProfile.value
                // Estimativa calórica da refeição (ex: 20-30% do total)
                val mealTargetCals = (profile.targetCalories / 5) 
                
                val foods = allFoods.value
                val contextFoods = foods.take(40).joinToString { it.name }

                val json = GeminiRepository.suggestMealItems(
                    mealType = mealType.name,
                    targetCalories = mealTargetCals,
                    restrictions = restrictions,
                    preferences = preferences,
                    availableFoods = contextFoods
                )

                if (json != null) {
                    val array = JSONArray(json)
                    val list = mutableListOf<AiDietSuggestion>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        list.add(AiDietSuggestion(
                            mealType = mealType,
                            foodName = obj.getString("foodName"),
                            amount = obj.getDouble("amount").toFloat(),
                            unit = obj.getString("unit"),
                            calories = obj.getDouble("calories").toFloat(),
                            protein = obj.getDouble("protein").toFloat(),
                            carbs = obj.getDouble("carbs").toFloat(),
                            fat = obj.getDouble("fat").toFloat(),
                            notes = obj.optString("notes")
                        ))
                    }
                    _aiDietSuggestions.value = list
                }
            } catch (e: Exception) {
                _toastMessage.value = "Erro ao sugerir alimentos: ${e.message}"
            } finally {
                _isAiDietLoading.value = false
            }
        }
    }

    fun clearAiSuggestions() {
        _aiDietSuggestions.value = emptyList()
    }

    init {
        // Inicializar somente os catálogos ausentes, preservando os dados pessoais.
        viewModelScope.launch {
            // Inicializar base de dados alimentar com macronutrientes caso a tabela esteja vazia ou incompleta
            try {
                if (repository.getFoodsCount() < com.example.data.local.PrepopulatedData.defaultFoods.size) {
                    repository.insertFoods(com.example.data.local.PrepopulatedData.defaultFoods)
                }
                val activeDiet = repository.activeDietPlan.firstOrNull()
                if (activeDiet == null) {
                    repository.addDietPlan(com.example.data.local.PrepopulatedData.defaultDietPlan)
                    repository.insertWeeklyDietPlans(com.example.data.local.PrepopulatedData.defaultWeeklyDietPlans)
                }
                val currentMeals = repository.mealPlanItems.firstOrNull() ?: emptyList()
                if (currentMeals.isEmpty()) {
                    repository.insertMealPlanItems(com.example.data.local.PrepopulatedData.defaultMealPlan)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun reloadFoodDatabase() {
        viewModelScope.launch {
            try {
                repository.insertFoods(com.example.data.local.PrepopulatedData.defaultFoods)
                _toastMessage.value = "Tabela nutricional sincronizada com ${com.example.data.local.PrepopulatedData.defaultFoods.size} alimentos!"
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.value = "Erro ao restaurar base alimentar: ${e.message}"
            }
        }
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
                            if (profile.email == "atleta@fitmail.com" || (profile.name == "Atleta" && !profile.hasCompletedOnboarding)) {
                                fallback.value = UserProfile()
                                return@collect
                            }
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
                                viewModelScope.launch { syncDataFromCloud() }
                                fallback.value = syncedProfile
                            } else {
                                fallback.value = profile.copy(
                                    isLoggedIn = profile.isLoggedIn && profile.firebaseUid.startsWith("guest_")
                                )
                            }
                        } else {
                            fallback.value = UserProfile()
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

    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    private val _cloudWorkoutLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val cloudWorkoutLogs: StateFlow<List<WorkoutLog>> = _cloudWorkoutLogs.asStateFlow()

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

    val recentIntakeHistory: StateFlow<List<DailyIntakeHistory>> = repository.recentIntakeHistory
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

    val allGpsRouteLogs: StateFlow<List<GpsRouteLog>> = repository.allGpsRouteLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allCardioLogs: StateFlow<List<CardioLog>> = repository.allCardioLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDateMillis = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    fun selectAppDate(dateMillis: Long) {
        _selectedDateMillis.value = startOfDay(dateMillis)
    }

    fun selectedDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(_selectedDateMillis.value))

    private fun startOfDay(timeMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun selectedDateWithCurrentTime(): Long {
        val selected = Calendar.getInstance().apply { timeInMillis = _selectedDateMillis.value }
        val now = Calendar.getInstance()
        selected.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
        selected.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        selected.set(Calendar.SECOND, now.get(Calendar.SECOND))
        return selected.timeInMillis
    }

    val totalGpsDistanceMeters: StateFlow<Float> = repository.totalGpsDistanceMeters
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    val roomDietaryProfile: StateFlow<DietaryProfile?> = repository.dietaryProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val allWorkoutSchedules: StateFlow<List<WorkoutSchedule>> = repository.allWorkoutSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active GPS Tracking Session
    private val _activeGpsSession = MutableStateFlow(ActiveGpsSession())
    val activeGpsSession: StateFlow<ActiveGpsSession> = _activeGpsSession.asStateFlow()
    private var gpsTimerJob: Job? = null

    init {
        viewModelScope.launch {
            RunningForegroundService.session.collect { serviceSession ->
                if (serviceSession.isRunning || _activeGpsSession.value.isRunning) {
                    _activeGpsSession.value = serviceSession
                }
            }
        }
    }

    // Cálculo Dinâmico de Gasto Calórico Diário & TMB:
    // Sem o treino no dia, o valor permanece com o TMB de repouso (parado).
    // O gasto só é somado após realizar o treino (musculação ou corrida).
    val dailyEnergyExpenditure: StateFlow<DailyEnergyExpenditure> = combine(
        userProfile,
        workoutLogs,
        allGpsRouteLogs,
        allCardioLogs,
        combine(_activeGpsSession, _selectedDateMillis) { session, date -> session to date }
    ) { profile, gymLogs, gpsLogs, cardioLogs, sessionAndDate ->
        val (activeGps, selectedDate) = sessionAndDate
        val weight = if (profile.weightKg > 20f) profile.weightKg else 78.5f
        val height = if (profile.heightCm > 50f) profile.heightCm else 178f
        val age = if (profile.age > 10) profile.age else 26
        val bmr = MacroCalculator.calculateBMR(weight, height, age, profile.gender)

        val dayStart = startOfDay(selectedDate)
        val dayEnd = Calendar.getInstance().apply { timeInMillis = dayStart; add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis
        val selectedIsToday = dayStart == startOfDay(System.currentTimeMillis())

        // Treinos de musculação realizados hoje
        val todayGym = gymLogs.filter { it.dateMillis in dayStart until dayEnd }
        val gymCals = todayGym.sumOf { log ->
            val duration = log.durationMinutes.coerceAtLeast(1)
            (duration * (weight * 0.08f)).toInt().coerceIn(80, 800)
        }

        // Treinos de corrida / cardio GPS realizados hoje
        val todayRuns = gpsLogs.filter { it.dateMillis in dayStart until dayEnd }
        val runsCals = todayRuns.sumOf { it.caloriesBurned.toInt() } + if (selectedIsToday) activeGps.caloriesBurned.toInt() else 0

        // Cardios manuais realizados hoje
        val todayCardio = cardioLogs.filter { it.dateMillis in dayStart until dayEnd }
        val manualCardioCals = todayCardio.sumOf { it.caloriesBurned }

        val totalWorkoutBurned = gymCals + runsCals + manualCardioCals
        val hasWorkoutToday = totalWorkoutBurned > 0 || todayGym.isNotEmpty() || todayRuns.isNotEmpty() || todayCardio.isNotEmpty() || (selectedIsToday && activeGps.isRunning)

        // REGRA: Sem treino, TMB de parado. Com treino, TMB + calorias gastas no treino.
        val totalDailyExp = if (hasWorkoutToday) bmr + totalWorkoutBurned else bmr

        DailyEnergyExpenditure(
            bmr = bmr,
            gymWorkoutCalories = gymCals,
            runningWorkoutCalories = runsCals,
            cardioWorkoutCalories = manualCardioCals,
            totalWorkoutCalories = totalWorkoutBurned,
            totalExpenditure = totalDailyExp,
            hasWorkoutToday = hasWorkoutToday,
            gymWorkoutsCount = todayGym.size,
            runningWorkoutsCount = todayRuns.size + if (selectedIsToday && activeGps.isRunning) 1 else 0,
            cardioWorkoutsCount = todayCardio.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyEnergyExpenditure(
            bmr = 1800,
            gymWorkoutCalories = 0,
            runningWorkoutCalories = 0,
            cardioWorkoutCalories = 0,
            totalWorkoutCalories = 0,
            totalExpenditure = 1800,
            hasWorkoutToday = false,
            gymWorkoutsCount = 0,
            runningWorkoutsCount = 0,
            cardioWorkoutsCount = 0
        )
    )

    // Daily Water Tracker state
    private val _waterDrankMl = MutableStateFlow(0)
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    init {
        viewModelScope.launch {
            val preferences = getApplication<Application>().getSharedPreferences("daily_tracking", Context.MODE_PRIVATE)
            val today = currentDateKey()
            if (preferences.getString("meal_completion_date", null) != today) {
                repository.resetDailyMeals()
                autoLogFixedItems()
                preferences.edit().putString("meal_completion_date", today).apply()
            }
            autoLogFixedItems() // Also call on init to ensure everything is synced
            combine(repository.recentIntakeHistory, _selectedDateMillis) { history, _ -> history }
                .collect { history ->
                    _waterDrankMl.value = history.firstOrNull { it.dateString == selectedDateKey() }?.waterDrankMl ?: 0
                }
        }
    }

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
            runCatching {
                repository.clearAllLocalData()
            }.onSuccess {
                authService.signOut()
                _toastMessage.value =
                    "Você saiu da conta e os dados deste aparelho foram removidos."
            }.onFailure {
                _toastMessage.value =
                    "Não foi possível limpar os dados locais. " +
                        "A sessão foi mantida por segurança."
            }
        }
    }

    fun clearAllUsersAndResetDatabase(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.clearAllLocalData() }
                .onSuccess {
                    authService.signOut()
                    _toastMessage.value = "Todos os dados locais foram excluídos."
                    onComplete()
                }
                .onFailure {
                    _toastMessage.value = "Não foi possível excluir todos os dados locais."
                }
        }
    }

    fun loginWithGoogleCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onSuccess: () -> Unit
    ) = authController.loginWithGoogleCredential(credential, onSuccess)

    fun enterAsGuest(onSuccess: () -> Unit) = authController.enterAsGuest(onSuccess)

    fun saveRoomDietaryProfile(name: String, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            repository.saveDietaryProfile(
                DietaryProfile(
                    name = name,
                    targetCalories = calories,
                    proteinGoal = protein,
                    carbGoal = carbs,
                    fatGoal = fat
                )
            )
            _toastMessage.value = "Perfil dietético salvo no Room!"
        }
    }

    fun saveRoomMacro(date: Long, calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            repository.saveMacro(
                Macro(
                    date = date,
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat
                )
            )
            _toastMessage.value = "Macros salvos com sucesso!"
        }
    }

    fun saveRoomWorkoutSchedule(dayOfWeek: String, activityType: String, durationMinutes: Int, intensity: String) {
        viewModelScope.launch {
            repository.saveWorkoutSchedule(
                WorkoutSchedule(
                    dayOfWeek = dayOfWeek,
                    activityType = activityType,
                    durationMinutes = durationMinutes,
                    intensity = intensity
                )
            )
            _toastMessage.value = "Cronograma de treino agendado no Room!"
        }
    }

    /**
     * Authenticates user via device biometric prompt (fingerprint / face unlock)
     */
    fun loginWithBiometrics(onSuccess: () -> Unit) = authController.loginWithBiometrics(onSuccess)

    fun deleteAccountLgpd(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val cloudResult = firestoreSync.deleteAllUserData()
            if (cloudResult.isFailure) {
                _toastMessage.value = cloudResult.exceptionOrNull()?.message
                    ?: "Não foi possível excluir os dados do servidor."
                return@launch
            }

            val authResult = authService.deleteAccount()
            runCatching { repository.clearAllLocalData() }
            authService.signOut()

            if (authResult.isSuccess) {
                _toastMessage.value = "Conta e dados locais e remotos excluídos conforme a LGPD."
                onSuccess()
            } else {
                _toastMessage.value =
                    "Os dados foram apagados, mas o Firebase exige login recente para excluir a conta. Entre novamente e repita."
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
        targetCalories: Int,
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
                isAthleteOrWarmClimate = true,
                customTargetCalories = targetCalories
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
            repository.saveDietaryProfile(
                DietaryProfile(
                    id = 1,
                    name = name.ifBlank { "Dieta Principal" },
                    targetCalories = plan.targetCalories,
                    proteinGoal = plan.proteinGrams,
                    carbGoal = plan.carbsGrams,
                    fatGoal = plan.fatGrams
                )
            )
            repository.saveMacro(
                Macro(
                    date = System.currentTimeMillis(),
                    calories = plan.targetCalories,
                    protein = plan.proteinGrams,
                    carbs = plan.carbsGrams,
                    fat = plan.fatGrams
                )
            )
            _toastMessage.value = "Metas de macros e perfil atualizados e salvos localmente!"
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

    fun addCardioLog(log: CardioLog) {
        viewModelScope.launch {
            val datedLog = log.copy(dateMillis = selectedDateWithCurrentTime())
            val insertedId = repository.addCardioLog(datedLog)
            firestoreSync.syncCardioLogToCloud(datedLog.copy(id = insertedId), userProfile.value.firebaseUid)
            _toastMessage.value = "Log de cardio salvo com sucesso!"
        }
    }

    // --- Water Tracker ---

    fun addWater(amountMl: Int) {
        _waterDrankMl.value = (_waterDrankMl.value + amountMl).coerceIn(0, 10000)
        persistSelectedIntake()
    }

    fun resetWater() {
        _waterDrankMl.value = 0
        persistSelectedIntake()
    }

    fun updateDailyWaterGoal(newGoalMl: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(targetWaterMl = newGoalMl.coerceIn(500, 10000))
            repository.saveUserProfile(updated)
            firestoreSync.syncCaloricGoalsToCloud(
                targetCalories = updated.targetCalories,
                targetProteinGrams = updated.targetProteinGrams,
                targetCarbsGrams = updated.targetCarbsGrams,
                targetFatGrams = updated.targetFatGrams,
                targetWaterMl = updated.targetWaterMl,
                customUid = updated.firebaseUid
            )
            _toastMessage.value = "Meta diária de água atualizada para ${updated.targetWaterMl} ml!"
        }
    }

    fun updateCaloricGoals(calories: Int, protein: Int, carbs: Int, fat: Int, water: Int) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(
                targetCalories = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWaterMl = water
            )
            repository.saveUserProfile(updated)
            firestoreSync.syncCaloricGoalsToCloud(
                targetCalories = calories,
                targetProteinGrams = protein,
                targetCarbsGrams = carbs,
                targetFatGrams = fat,
                targetWaterMl = water,
                customUid = updated.firebaseUid
            )
            firestoreSync.syncProfileToCloud(updated)
            _toastMessage.value = "Metas salvas no aparelho. Consulte a sincronização para confirmar o envio à nuvem."
        }
    }

    private val syncController by lazy {
        com.example.ui.sync.SyncController(viewModelScope, repository, firestoreSync, ::showToast)
    }
    val isSyncing: StateFlow<Boolean> get() = syncController.isSyncing

    fun syncAllDataToFirestore() = syncController.sync()

    // --- Global Theme Toggle (Light / Dark Mode) ---
    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            val prof = userProfile.value
            val updated = prof.copy(isDarkMode = isDark)
            repository.saveUserProfile(updated)
        }
    }

    fun toggleDarkMode() {
        val current = userProfile.value.isDarkMode
        setDarkMode(!current)
    }

    // --- 🌍 GPS Outdoor Route & Daily/Monthly Steps Tracking ---
    fun startGpsSession(
        initialLat: Double? = null,
        initialLng: Double? = null,
        voiceEnabled: Boolean = true,
        narrationIntervalMeters: Float = 1000f,
        targetPaceSecondsPerKm: Long = 0L,
        isPaceAlertEnabled: Boolean = false
    ) {
        if (_activeGpsSession.value.isRunning && !_activeGpsSession.value.isPaused) return
        RunningForegroundService.start(
            getApplication(),
            initialLat,
            initialLng,
            voiceEnabled,
            narrationIntervalMeters,
            targetPaceSecondsPerKm,
            isPaceAlertEnabled
        )
        _toastMessage.value = "Rastreamento de corrida iniciado!"
    }

    fun pauseGpsSession() {
        RunningForegroundService.pause(getApplication())
        _toastMessage.value = "Corrida pausada."
    }

    fun resumeGpsSession() {
        RunningForegroundService.resume(getApplication())
        _toastMessage.value = "Corrida retomada!"
    }

    fun addGpsCoordinate(latitude: Double, longitude: Double) {
        val current = _activeGpsSession.value
        if (!current.isRunning || current.isPaused) return
        val lastPoint = current.routePoints.lastOrNull()
        val addedMeters = if (lastPoint != null) {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                lastPoint.first, lastPoint.second,
                latitude, longitude,
                results
            )
            results[0]
        } else {
            0f
        }
        // Only accept real movement (> 1.5m) to avoid stationary GPS jitter
        if (addedMeters > 1.5f || current.routePoints.isEmpty()) {
            val newPoints = current.routePoints + Pair(latitude, longitude)
            val newDist = current.distanceMeters + addedMeters
            val addedSteps = (addedMeters * 1.35f).toInt().coerceAtLeast(0)
            val newSteps = current.stepsCount + addedSteps
            val calories = newDist * 0.065f
            val paceSeconds = if (newDist > 20f && current.elapsedSeconds > 0) {
                ((current.elapsedSeconds.toDouble() / (newDist / 1000.0)).toLong()).coerceIn(60, 3600)
            } else 0L
            val speed = if (current.elapsedSeconds > 0 && newDist > 0) {
                ((newDist / 1000f) / (current.elapsedSeconds / 3600f)).coerceIn(0f, 60f)
            } else 0f

            _activeGpsSession.value = current.copy(
                distanceMeters = newDist,
                stepsCount = newSteps,
                caloriesBurned = calories,
                currentSpeedKmh = speed,
                paceSecondsPerKm = paceSeconds,
                routePoints = newPoints
            )
        }
    }

    fun stopAndSaveGpsSession(notes: String = "", routeName: String = "Corrida ao Ar Livre") {
        gpsTimerJob?.cancel()
        gpsTimerJob = null
        val session = _activeGpsSession.value
        RunningForegroundService.stop(getApplication())
        if (session.elapsedSeconds < 2 && session.distanceMeters < 5f) {
            _activeGpsSession.value = ActiveGpsSession()
            _toastMessage.value = "Sessão muito curta descartada."
            return
        }

        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
            val now = System.currentTimeMillis()
            val dateLabel = dateFormat.format(Date(now))
            val pace = if (session.distanceMeters > 0) {
                (session.elapsedSeconds / 60.0f) / (session.distanceMeters / 1000.0f)
            } else 0f

            val coordsStr = session.routePoints.joinToString(";") { "${it.first},${it.second}" }

            val log = GpsRouteLog(
                routeName = routeName,
                activityType = "Corrida",
                dateMillis = now,
                dateDisplay = dateLabel,
                distanceMeters = session.distanceMeters,
                durationSeconds = session.elapsedSeconds,
                stepsCount = session.stepsCount,
                caloriesBurned = session.caloriesBurned,
                averagePaceMinPerKm = pace,
                routePointsJson = coordsStr,
                notes = notes
            )
            val insertedId = repository.insertGpsRouteLog(log)
            firestoreSync.syncGpsRouteLogToCloud(log.copy(id = insertedId), userProfile.value.firebaseUid)
            _activeGpsSession.value = ActiveGpsSession()
            val kmText = String.format(Locale.getDefault(), "%.2f", log.distanceMeters / 1000f)
            _toastMessage.value = "Corrida de $kmText km salva com sucesso!"
        }
    }

    fun discardGpsSession() {
        gpsTimerJob?.cancel()
        gpsTimerJob = null
        _activeGpsSession.value = ActiveGpsSession()
        RunningForegroundService.stop(getApplication())
        _toastMessage.value = "Sessão GPS cancelada."
    }

    fun configureBackgroundRunningVoice(
        enabled: Boolean,
        intervalMeters: Float,
        targetPaceSecondsPerKm: Long = 0L,
        isPaceAlertEnabled: Boolean = false
    ) {
        if (_activeGpsSession.value.isRunning) {
            RunningForegroundService.configureVoice(
                getApplication(),
                enabled,
                intervalMeters,
                targetPaceSecondsPerKm,
                isPaceAlertEnabled
            )
        }
    }

    fun deleteGpsRoute(id: Long) {
        viewModelScope.launch {
            repository.deleteGpsRouteLog(id)
            _toastMessage.value = "Registro de percurso excluído."
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
            val normalized = record.copy(dateString = selectedDateKey(), dateMillis = selectedDateWithCurrentTime())
            val insertedId = repository.addFoodRecord(normalized)
            firestoreSync.syncFoodRecordToCloud(normalized.copy(id = insertedId), userProfile.value.firebaseUid)
            persistIntakeForDate(selectedDateKey())
            _toastMessage.value = "${record.foodName} registrado no seu diário alimentar!"
        }
    }

    fun deleteFoodRecord(id: Long) {
        viewModelScope.launch {
            val affectedDate = repository.allFoodRecords.firstOrNull().orEmpty()
                .firstOrNull { it.id == id }?.dateString ?: selectedDateKey()
            repository.deleteFoodRecord(id)
            firestoreSync.deleteFoodRecordFromCloud(id, userProfile.value.firebaseUid)
            persistIntakeForDate(affectedDate)
            _toastMessage.value = "Alimento removido do diário!"
        }
    }

    fun toggleMealItemCompleted(
        item: MealPlanItem,
        dateKey: String = currentDateKey(),
        isCompletedForDate: Boolean = item.isCompletedToday
    ) {
        viewModelScope.launch {
            if (!isCompletedForDate) {
                val recordDate = runCatching {
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)?.time
                }.getOrNull() ?: System.currentTimeMillis()
                val record = FoodRecord(
                    dateString = dateKey,
                    dateMillis = recordDate,
                    mealType = item.mealType,
                    foodItemId = item.foodItemId,
                    foodName = item.foodName,
                    foodCategory = item.foodCategory,
                    amount = item.amount,
                    unit = item.unit,
                    calories = item.calories,
                    protein = item.protein,
                    carbs = item.carbs,
                    fat = item.fat,
                    fiber = item.fiber,
                    notes = item.notes
                )
                val insertedId = repository.addFoodRecord(record)
                firestoreSync.syncFoodRecordToCloud(record.copy(id = insertedId), userProfile.value.firebaseUid)
            } else {
                val record = repository.allFoodRecords.firstOrNull().orEmpty()
                    .filter { it.dateString == dateKey && it.foodItemId == item.foodItemId && it.mealType == item.mealType }
                    .maxByOrNull { it.dateMillis }
                if (record != null) {
                    repository.deleteFoodRecord(record.id)
                    firestoreSync.deleteFoodRecordFromCloud(record.id, userProfile.value.firebaseUid)
                }
            }
            if (dateKey == currentDateKey()) {
                repository.toggleMealCompletion(item.id, isCompletedForDate)
            }
            persistIntakeForDate(dateKey)
        }
    }

    fun toggleMealItemFixed(item: MealPlanItem) {
        viewModelScope.launch {
            val nextState = !item.isFixed
            repository.toggleMealFixed(item.id, item.isFixed)
            
            // Se o usuário fixou o item HOJE, vamos logar ele imediatamente
            if (nextState) {
                autoLogFixedItems()
            } else {
                // Se o usuário DESFIXOU, talvez queiramos remover o log? 
                // Provavelmente não, deixa o registro de hoje. 
                // Mas para o futuro ele não será mais auto-logado.
            }
        }
    }

    private fun autoLogFixedItems() {
        viewModelScope.launch {
            val items = repository.mealPlanItems.firstOrNull().orEmpty()
            val fixedItems = items.filter { it.isFixed }
            if (fixedItems.isEmpty()) return@launch

            val today = currentDateKey()
            val recordsToday = repository.getFoodRecordsForDate(today).firstOrNull().orEmpty()
            val profile = userProfile.value

            var anyNewLog = false
            fixedItems.forEach { item ->
                val alreadyLogged = recordsToday.any { 
                    it.foodItemId == item.foodItemId && it.mealType == item.mealType && it.amount == item.amount 
                }
                
                if (!alreadyLogged) {
                    val recordDate = System.currentTimeMillis()
                    val record = FoodRecord(
                        dateString = today,
                        dateMillis = recordDate,
                        mealType = item.mealType,
                        foodItemId = item.foodItemId,
                        foodName = item.foodName,
                        foodCategory = item.foodCategory,
                        amount = item.amount,
                        unit = item.unit,
                        calories = item.calories,
                        protein = item.protein,
                        carbs = item.carbs,
                        fat = item.fat,
                        fiber = item.fiber,
                        notes = item.notes
                    )
                    val insertedId = repository.addFoodRecord(record)
                    firestoreSync.syncFoodRecordToCloud(record.copy(id = insertedId), profile.firebaseUid)
                    
                    // Mark the meal plan item as completed for today in UI
                    if (!item.isCompletedToday) {
                        repository.toggleMealCompletion(item.id, false)
                    }
                    anyNewLog = true
                }
            }
            
            if (anyNewLog) {
                persistIntakeForDate(today)
                _toastMessage.value = "Itens fixos registrados automaticamente para hoje!"
            }
        }
    }

    private fun currentDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun persistSelectedIntake() {
        viewModelScope.launch { persistIntakeForDate(selectedDateKey()) }
    }

    private suspend fun persistTodayIntakeNow() {
        persistIntakeForDate(currentDateKey())
    }

    private suspend fun persistIntakeForDate(dateKey: String) {
        val recordsForDate = repository.allFoodRecords.firstOrNull().orEmpty().filter { it.dateString == dateKey }
        val profile = userProfile.value
        val dateMillis = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateKey)?.time
        }.getOrNull() ?: System.currentTimeMillis()
        val savedWater = repository.recentIntakeHistory.firstOrNull().orEmpty()
            .firstOrNull { it.dateString == dateKey }?.waterDrankMl ?: 0
        val summary = DailyIntakeHistory(
            dateString = dateKey,
            dateMillis = dateMillis,
            totalCalories = recordsForDate.sumOf { it.calories.toDouble() }.toFloat(),
            totalProtein = recordsForDate.sumOf { it.protein.toDouble() }.toFloat(),
            totalCarbs = recordsForDate.sumOf { it.carbs.toDouble() }.toFloat(),
            totalFat = recordsForDate.sumOf { it.fat.toDouble() }.toFloat(),
            targetCalories = profile.targetCalories,
            isAdhered = recordsForDate.sumOf { it.calories.toDouble() } <= profile.targetCalories,
            waterDrankMl = if (dateKey == selectedDateKey()) _waterDrankMl.value else savedWater
        )
        val id = repository.saveDailyIntake(summary)
        firestoreSync.syncDailyIntakeToCloud(summary.copy(id = id), profile.firebaseUid)
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

    fun toggleFoodFavorite(food: FoodItem) {
        viewModelScope.launch {
            val nextState = !food.isFavorite
            repository.toggleFoodFavorite(food, userProfile.value.firebaseUid)
            
            // Sincroniza com a nuvem imediatamente
            firestoreSync.syncFavoriteFoodToCloud(food.copy(isFavorite = nextState), userProfile.value.firebaseUid)
            
            val msg = if (nextState) "adicionado aos favoritos" else "removido dos favoritos"
            _toastMessage.value = "'${food.name}' $msg"
        }
    }

    suspend fun syncDataFromCloud() {
        val uid = userProfile.value.firebaseUid
        if (uid.isNotBlank()) {
            // Sincroniza Alimentos Favoritos
            syncFavoritesFromCloud()
            // Sincroniza Plano de Refeições
            firestoreSync.fetchMealPlansFromCloud(uid).onSuccess { cloudItems ->
                repository.clearMealPlanItems()
                repository.insertMealPlanItems(cloudItems)
            }
            // Sincroniza Histórico de Alimentação
            firestoreSync.fetchFoodRecordsFromCloud(uid).onSuccess { cloudRecords ->
                repository.clearFoodRecords()
                repository.insertFoodRecords(cloudRecords)
            }
        }
    }

    fun syncFavoritesFromCloud() {
        viewModelScope.launch {
            val uid = userProfile.value.firebaseUid
            if (uid.isNotBlank()) {
                val result = firestoreSync.fetchFavoriteFoodsFromCloud(uid)
                result.onSuccess { favoriteIds ->
                    repository.clearAllFavorites()
                    val allFoodsList = allFoods.value
                    favoriteIds.forEach { id ->
                        val food = allFoodsList.find { it.id == id }
                        if (food != null) {
                            repository.toggleFoodFavorite(food, uid)
                        }
                    }
                    if (favoriteIds.isNotEmpty()) {
                        _toastMessage.value = "${favoriteIds.size} favoritos sincronizados da nuvem!"
                    }
                }.onFailure {
                    if (BuildConfig.DEBUG) Log.e("VirtusViewModel", "Erro ao sincronizar favoritos: ${it.message}")
                }
            }
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

    fun recordExerciseProgression(
        exercise: WorkoutExercise,
        newWeightKg: Float,
        sets: Int,
        reps: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(
                exercise.copy(currentWeightKg = newWeightKg, sets = sets, repsMin = reps, repsMax = reps)
            )
            val now = System.currentTimeMillis()
            val volume = sets * reps * newWeightKg
            val logId = repository.logCompletedWorkout(
                routineId = exercise.routineId,
                routineName = "Treino - Progressão de ${exercise.exerciseName}",
                durationMinutes = 45,
                totalVolumeKg = volume,
                totalSetsCompleted = sets,
                perceivedExertion = 8,
                notes = if (notes.isNotBlank()) notes else "Carga registrada: ${newWeightKg}kg ($sets x $reps)",
                dateMillis = now
            )
            val wLog = WorkoutLog(
                id = logId,
                routineId = exercise.routineId,
                routineName = "Treino - Progressão de ${exercise.exerciseName}",
                dateMillis = now,
                durationMinutes = 45,
                totalVolumeKg = volume,
                totalSetsCompleted = sets,
                perceivedExertion = 8,
                notes = if (notes.isNotBlank()) notes else "Carga registrada: ${newWeightKg}kg ($sets x $reps)"
            )
            firestoreSync.syncWorkoutLogToCloud(wLog, userProfile.value.firebaseUid)
            _toastMessage.value = "Carga de ${exercise.exerciseName} atualizada para ${newWeightKg} kg!"
        }
    }

    fun refreshWorkoutLogsFromFirestore() {
        viewModelScope.launch {
            _isFirestoreSyncing.value = true
            try {
                val res = firestoreSync.fetchWorkoutLogsFromCloud(userProfile.value.firebaseUid)
                res.onSuccess { list ->
                    _cloudWorkoutLogs.value = list
                    _toastMessage.value = "${list.size} treinos históricos carregados do Firestore (App Check verificado)."
                }.onFailure { err ->
                    android.util.Log.w("VirtusViewModel", "Erro ao carregar treinos do Firestore: ${err.message}")
                }
            } catch (e: Exception) {
                android.util.Log.e("VirtusViewModel", "Exceção ao buscar treinos do Firestore", e)
            } finally {
                _isFirestoreSyncing.value = false
            }
        }
    }

    fun shareExerciseProgression(
        context: android.content.Context,
        exerciseName: String,
        currentWeight: Float,
        prWeight: Float,
        delta: Float,
        oneRepMax: Float
    ) {
        try {
            val text = """
                💪 Progressão de Carga - Virtus Growth
                🏋️ Exercício: $exerciseName
                🔥 Carga Atual: ${currentWeight} kg
                🏆 Recorde Pessoal (PR): ${prWeight} kg
                📈 Evolução: ${if (delta >= 0) "+$delta" else "$delta"} kg
                ⚡ 1RM Estimado: ${oneRepMax.toInt()} kg
            """.trimIndent()
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = android.content.Intent.createChooser(sendIntent, "Compartilhar Progressão de Carga")
            shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            android.util.Log.e("VirtusViewModel", "Error sharing progression", e)
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
                
                // Sync to Cloud
                firestoreSync.syncWorkoutRoutineToCloud(updatedRoutine).onFailure { e ->
                    Log.e("VirtusViewModel", "Falha ao sincronizar rotina: ${e.message}")
                    _toastMessage.value = "Erro ao sincronizar rotina na nuvem."
                }
                
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
                val id = repository.addWorkoutRoutine(newRoutine)
                
                // Sync to Cloud
                firestoreSync.syncWorkoutRoutineToCloud(newRoutine.copy(id = id)).onFailure { e ->
                    Log.e("VirtusViewModel", "Falha ao sincronizar nova rotina: ${e.message}")
                    _toastMessage.value = "Erro ao sincronizar nova rotina na nuvem."
                }
                
                id
            }

            exercises.forEachIndexed { index, ex ->
                val guide = com.example.util.ExerciseGifMapper.getGuideForExercise(ex.exerciseName)
                val exWithId = ex.copy(
                    id = 0L,
                    routineId = routineId,
                    orderIndex = index,
                    biomechanicsGuide = ex.biomechanicsGuide.ifBlank { guide.postureTip },
                    gifDemonstrationUrl = ex.gifDemonstrationUrl.ifBlank { guide.gifUrl }
                )
                val id = repository.addWorkoutExercise(exWithId)
                
                // Sync to Cloud with nested path
                val workoutId = routineId.toString()
                Log.d("VirtusViewModel", "Sincronizando exercício (id: $id, workoutId: $workoutId) com Firestore: $exWithId")
                
                firestoreSync.syncWorkoutExerciseToCloud(exWithId.copy(id = id), workoutId = workoutId).onFailure { e ->
                    Log.e("VirtusViewModel", "Falha ao sincronizar exercício $id no caminho aninhado: ${e.message}")
                    _toastMessage.value = "Erro ao sincronizar exercício na nuvem."
                }
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
        
        // Ensure duration is based on exercise setting, otherwise default to 90s
        val duration = exercise.restSeconds.coerceAtLeast(30)
        
        _activeWorkout.value = current.copy(
            completedSetsCount = current.completedSetsCount + 1,
            loggedExerciseSets = updatedMap,
            restTimerRemainingSeconds = duration,
            isRestTimerRunning = true,
            isRestTimerFinished = false
        )
        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            var remaining = duration
            while (remaining > 0) {
                delay(1000)
                remaining--
                // Check if user manually updated or skipped
                val updatedSession = _activeWorkout.value
                if (!updatedSession.isRestTimerRunning && updatedSession.restTimerRemainingSeconds == remaining) {
                   break
                }
                _activeWorkout.value = updatedSession.copy(
                    restTimerRemainingSeconds = remaining
                )
            }
            _activeWorkout.value = _activeWorkout.value.copy(
                isRestTimerRunning = false,
                isRestTimerFinished = true
            )
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
            isRestTimerRunning = false,
            isRestTimerFinished = false,
            restTimerRemainingSeconds = 0
        )
    }

    fun toggleSetLog(exercise: WorkoutExercise, setIndex: Int) {
        val reps = exercise.repsMin
        val weight = exercise.currentWeightKg
        logSetAndStartRest(exercise, reps, weight)
    }

    fun updateExerciseWeight(exercise: WorkoutExercise, newWeight: Float) {
        viewModelScope.launch {
            repository.updateWorkoutExercise(exercise.copy(currentWeightKg = newWeight))
        }
    }

    fun adjustRestTimer(deltaSeconds: Int) {
        val current = _activeWorkout.value
        val newRemaining = (current.restTimerRemainingSeconds + deltaSeconds).coerceAtLeast(0)
        _activeWorkout.value = current.copy(
            restTimerRemainingSeconds = newRemaining,
            isRestTimerRunning = newRemaining > 0
        )
    }

    fun startFreeWorkout() {
        val freeRoutine = WorkoutRoutine(
            id = 99999L,
            letter = "L",
            title = "Treino Livre",
            dayOfWeekLabel = "Hoje",
            targetMuscleGroups = "Geral",
            estimatedDurationMinutes = 60
        )
        startActiveWorkout(freeRoutine, emptyList())
    }

    fun finishActiveWorkout(rpe: Int, notes: String) {
        val active = _activeWorkout.value
        if (!active.isRunning) return
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        viewModelScope.launch {
            val totalVolume = active.loggedExerciseSets.values.sumOf { setList ->
                setList.sumOf { (reps, weight) -> (reps * weight).toDouble() }
            }.toFloat()

            repository.logCompletedWorkout(
                routineId = active.routine?.id ?: 99999L,
                routineName = active.routine?.title ?: "Treino Livre",
                durationMinutes = (active.elapsedSeconds / 60).coerceAtLeast(1),
                totalVolumeKg = totalVolume,
                totalSetsCompleted = active.completedSetsCount,
                perceivedExertion = rpe,
                notes = notes
            )
            _activeWorkout.value = ActiveWorkoutSession()
            _toastMessage.value = "Treino finalizado e registrado com sucesso!"
        }
    }

    fun deleteWorkoutLog(log: WorkoutLog) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(log.id)
            _toastMessage.value = "Registro de treino removido"
        }
    }

    fun deleteWorkoutLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteWorkoutLog(logId)
            _toastMessage.value = "Registro de treino removido"
        }
    }

    fun formatRoutineForSharing(routine: WorkoutRoutine): String {
        val exercises = allExercises.value.filter { it.routineId == routine.id }
        val sb = StringBuilder()
        sb.append("🏋️ *Ficha de Treino VIRTUS*\n")
        sb.append("📋 *${routine.letter} - ${routine.title}*\n")
        if (routine.targetMuscleGroups.isNotBlank()) sb.append("🎯 ${routine.targetMuscleGroups}\n")
        sb.append("\n*Exercícios:*\n")
        exercises.forEachIndexed { i, ex ->
            sb.append("${i + 1}. *${ex.exerciseName}* - ${ex.sets}x${ex.repsMin}-${ex.repsMax} (${ex.currentWeightKg.toInt()}kg)\n")
            if (ex.biomechanicsGuide.isNotBlank()) sb.append("   💡 ${ex.biomechanicsGuide}\n")
        }
        sb.append("\n_Bons treinos! Gerado pelo VIRTUS Fit App_")
        return sb.toString()
    }

    fun addProgressLog(
        weightKg: Float,
        bodyFatPercentage: Float,
        chestCm: Float = 0f,
        waistCm: Float = 0f,
        armRightCm: Float = 0f,
        thighCm: Float = 0f,
        notes: String = "",
        photoUri: String = ""
    ) {
        viewModelScope.launch {
            val dateMillis = System.currentTimeMillis()
            val dateDisplay = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMillis))
            val log = ProgressLog(
                dateMillis = dateMillis,
                dateDisplay = dateDisplay,
                weightKg = weightKg,
                bodyFatPercentage = bodyFatPercentage,
                chestCm = chestCm,
                waistCm = waistCm,
                armLeftCm = armRightCm,
                armRightCm = armRightCm,
                thighCm = thighCm,
                notes = notes,
                photoUri = photoUri
            )
            repository.addProgressLog(log)
            _toastMessage.value = "Evolução salva com sucesso!"
        }
    }

    fun deleteProgressLog(id: Long) {
        viewModelScope.launch {
            repository.deleteProgressLog(id)
            _toastMessage.value = "Registro removido"
        }
    }

    fun exportPdfProgressReport(context: Context) {
        viewModelScope.launch {
            try {
                val prof = userProfile.value ?: return@launch
                val meals = mealPlanItems.value
                val routines = workoutRoutines.value
                val exercises = allExercises.value
                val progress = progressLogs.value
                val shopping = shoppingList.value

                PdfExporter.generateAndShareProgressReport(
                    context,
                    prof,
                    meals,
                    routines,
                    exercises,
                    progress,
                    shopping
                )
            } catch (e: Exception) {
                e.printStackTrace()
                _toastMessage.value = "Erro ao exportar PDF: ${e.localizedMessage}"
            }
        }
    }

    fun formatDietPlanForSharing(): String {
        val diet = activeDietPlan.value
        val meals = mealPlanItems.value
        val sb = StringBuilder()
        sb.append("🥗 *Plano Alimentar VIRTUS*\n")
        if (diet != null) {
            sb.append("🎯 *Metas Diárias:* ${diet.targetCalories} kcal | P: ${diet.targetProteinGrams}g | C: ${diet.targetCarbsGrams}g | G: ${diet.targetFatGrams}g\n\n")
        }
        sb.append("*Refeições:*\n")
        MealType.values().forEach { mealType ->
            val itemsForMeal = meals.filter { it.mealType == mealType }
            if (itemsForMeal.isNotEmpty()) {
                sb.append("\n📌 *${mealType.title}*\n")
                itemsForMeal.forEach { item ->
                    sb.append("• ${item.foodName} (${item.amount.toInt()}${item.unit}) - ${item.calories.toInt()} kcal\n")
                }
            }
        }
        sb.append("\n_Foco na dieta! Gerado pelo VIRTUS Fit App_")
        return sb.toString()
    }

    fun getMuscleVolume(days: Int): Map<String, Float> {
        val now = System.currentTimeMillis()
        val limit = if (days <= 0) 0L else now - (days.toLong() * 86400000L)
        
        return workoutLogs.value
            .asSequence()
            .filter { it.dateMillis >= limit }
            .mapNotNull { log ->
                workoutRoutines.value.find { it.id == log.routineId }?.let { routine ->
                    val muscles = routine.targetMuscleGroups.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    if (muscles.isEmpty()) emptyMap() else {
                        val volPerMuscle = log.totalVolumeKg / muscles.size
                        muscles.associateWith { volPerMuscle }
                    }
                }
            }
            .flatMap { it.entries }
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, volumes) -> volumes.sum() }
    }


    fun runWorkoutSubcollectionMigration(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            try {
                val service = com.example.data.firestore.WorkoutMigrationService(getApplication())
                val result = service.migrateUserWorkoutsAndExercises()
                result.onSuccess { report ->
                    val msg = "Migração concluída: ${report.workoutsProcessed} treinos e ${report.exercisesMigrated + report.legacyExercisesMoved} exercícios migrados."
                    _toastMessage.value = msg
                    onComplete(true, msg)
                }.onFailure { err ->
                    val msg = "Erro na migração: ${err.message}"
                    _toastMessage.value = msg
                    onComplete(false, msg)
                }
            } catch (e: Exception) {
                val msg = "Falha ao executar migração: ${e.message}"
                _toastMessage.value = msg
                onComplete(false, msg)
            }
        }
    }

}
