package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.VirtusApplication
import com.example.data.auth.FirebaseAuthService
import com.example.data.firestore.ConflictDecision
import com.example.data.firestore.FirestoreSyncManager
import com.example.data.firestore.SyncReport
import com.example.data.firestore.WorkoutMigrationService
import com.example.data.local.PrepopulatedData
import com.example.data.model.CardioLog
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.DietPlan
import com.example.data.model.DietaryProfile
import com.example.data.model.FoodItem
import com.example.data.model.FoodRecord
import com.example.data.model.GpsRouteLog
import com.example.data.model.MealPlanItem
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
import com.example.data.model.WorkoutSchedule
import com.example.data.repository.VirtusRepository
import com.example.service.RunningForegroundService
import com.example.ui.auth.AuthController
import com.example.ui.sync.PendingSyncItem
import com.example.ui.sync.SyncController
import com.example.util.AppLog
import com.example.util.MacroCalculator
import com.example.util.PdfExporter
import com.example.util.WorkoutAudioHapticHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Shared application-level state holder (`StateFlow` single source of truth)
 * used across domain ViewModels (`AuthViewModel`, `WorkoutViewModel`, `DietViewModel`,
 * `ShoppingViewModel`, `CardioViewModel`, `SyncViewModel`) so user profile, theme,
 * active sessions, and Room flows are never duplicated.
 */
class VirtusSharedState(
    val application: Application,
    val scope: CoroutineScope,
    val repository: VirtusRepository
) {
    val authService: FirebaseAuthService by lazy { FirebaseAuthService() }
    val firestoreSync: FirestoreSyncManager by lazy { FirestoreSyncManager(application) }

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    val syncController: SyncController by lazy {
        SyncController(
            context = application,
            scope = scope,
            repository = repository,
            syncManager = firestoreSync,
            showMessage = ::showToast
        )
    }

    val authController: AuthController by lazy {
        AuthController(
            scope = scope,
            repository = repository,
            authService = authService,
            firestoreSync = firestoreSync,
            profile = { userProfile.value },
            showMessage = ::showToast,
            onTriggerDeltaSync = { syncController.sync(isAutoFlush = false) }
        )
    }

    private val _isFirestoreSyncing = MutableStateFlow(false)
    val isFirestoreSyncing: StateFlow<Boolean> = _isFirestoreSyncing.asStateFlow()

    fun setFirestoreSyncing(syncing: Boolean) {
        _isFirestoreSyncing.value = syncing
    }

    private val _cloudWorkoutLogs = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val cloudWorkoutLogs: StateFlow<List<WorkoutLog>> = _cloudWorkoutLogs.asStateFlow()

    fun setCloudWorkoutLogs(logs: List<WorkoutLog>) {
        _cloudWorkoutLogs.value = logs
    }

    private val _activeWorkout = MutableStateFlow(ActiveWorkoutSession())
    val activeWorkout: StateFlow<ActiveWorkoutSession> = _activeWorkout.asStateFlow()

    fun updateActiveWorkout(session: ActiveWorkoutSession) {
        _activeWorkout.value = session
    }

    var workoutTimerJob: Job? = null
    var restTimerJob: Job? = null

    private val _activeGpsSession = MutableStateFlow(ActiveGpsSession())
    val activeGpsSession: StateFlow<ActiveGpsSession> = _activeGpsSession.asStateFlow()

    fun updateActiveGpsSession(session: ActiveGpsSession) {
        _activeGpsSession.value = session
    }

    var gpsTimerJob: Job? = null

    private val _waterDrankMl = MutableStateFlow(0)
    val waterDrankMl: StateFlow<Int> = _waterDrankMl.asStateFlow()

    fun setWaterDrankMl(value: Int) {
        _waterDrankMl.value = value
    }

    private val _selectedDateMillis = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    fun selectAppDate(dateMillis: Long) {
        _selectedDateMillis.value = startOfDay(dateMillis)
    }

    fun selectedDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(_selectedDateMillis.value))

    fun currentDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun startOfDay(timeMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = timeMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun selectedDateWithCurrentTime(): Long {
        val selected = Calendar.getInstance().apply { timeInMillis = _selectedDateMillis.value }
        val now = Calendar.getInstance()
        selected.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY))
        selected.set(Calendar.MINUTE, now.get(Calendar.MINUTE))
        selected.set(Calendar.SECOND, now.get(Calendar.SECOND))
        return selected.timeInMillis
    }

    private var cloudProfileObserverJob: Job? = null
    private var observedCloudUid: String? = null

    fun stopCloudObservers() {
        cloudProfileObserverJob?.cancel()
        cloudProfileObserverJob = null
        observedCloudUid = null
        firestoreSync.removeAllListeners()
    }

    private fun startCloudProfileObserverIfNeeded(uid: String) {
        if (uid.isBlank() || uid.startsWith("guest_") || observedCloudUid == uid) return
        observedCloudUid = uid
        cloudProfileObserverJob?.cancel()
        cloudProfileObserverJob = scope.launch {
            firestoreSync.observeProfileSnapshotFromCloud(uid).collect { remoteSnapshot ->
                if (remoteSnapshot != null) {
                    val local = repository.getUserProfileDirect(1)
                    val resolution = firestoreSync.reconcileRemoteProfileLWW(
                        localProfile = local,
                        remoteProfile = remoteSnapshot.profile,
                        remoteUpdatedAtMillis = remoteSnapshot.updatedAtMillis,
                        uid = uid
                    )
                    when (resolution.decision) {
                        ConflictDecision.APPLY_REMOTE_DELTA -> {
                            repository.saveUserProfile(resolution.mergedProfile)
                        }
                        ConflictDecision.KEEP_LOCAL_NEWER -> {
                            if (local != null) {
                                firestoreSync.syncProfileToCloud(local)
                            }
                        }
                        ConflictDecision.NO_OP -> Unit
                    }
                }
            }
        }
    }

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        ).let { flow ->
            MutableStateFlow(UserProfile()).also { fallback ->
                scope.launch {
                    flow.collect { profile ->
                        if (profile != null) {
                            if (profile.email == "atleta@fitmail.com" || (profile.name == "Atleta" && !profile.hasCompletedOnboarding)) {
                                fallback.value = UserProfile()
                                return@collect
                            }
                            val firebaseUser = authService.currentUser
                            if (firebaseUser != null) {
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
                                startCloudProfileObserverIfNeeded(firebaseUser.uid)
                                scope.launch { syncDataFromCloud() }
                                fallback.value = syncedProfile
                            } else {
                                stopCloudObservers()
                                fallback.value = profile.copy(
                                    isLoggedIn = profile.isLoggedIn && profile.firebaseUid.startsWith("guest_")
                                )
                            }
                        } else {
                            stopCloudObservers()
                            fallback.value = UserProfile()
                        }
                    }
                }
            }
        }

    val allFoods: StateFlow<List<FoodItem>> = repository.allFoods
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDietPlans: StateFlow<List<DietPlan>> = repository.allDietPlans
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeDietPlan: StateFlow<DietPlan?> = repository.activeDietPlan
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val mealPlanItems: StateFlow<List<MealPlanItem>> = repository.mealPlanItems
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shoppingList: StateFlow<List<ShoppingItem>> = repository.shoppingList
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFoodRecords: StateFlow<List<FoodRecord>> = repository.allFoodRecords
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyDietPlans: StateFlow<List<WeeklyDietPlan>> = repository.getWeeklyDietPlans()
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allWorkoutPlans: StateFlow<List<WorkoutPlan>> = repository.allWorkoutPlans
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeWorkoutPlan: StateFlow<WorkoutPlan?> = repository.activeWorkoutPlan
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val workoutRoutines: StateFlow<List<WorkoutRoutine>> = repository.workoutRoutines
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<WorkoutExercise>> = repository.allExercises
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workoutLogs: StateFlow<List<WorkoutLog>> = repository.workoutLogs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWorkoutVolume: StateFlow<Float> = repository.totalWorkoutVolume
        .map { it ?: 0f }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0f)

    val totalWorkoutCount: StateFlow<Int> = repository.totalWorkoutCount
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0)

    val progressLogs: StateFlow<List<ProgressLog>> = repository.progressLogs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentIntakeHistory: StateFlow<List<DailyIntakeHistory>> = repository.recentIntakeHistory
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReceipts: StateFlow<List<PurchaseReceipt>> = repository.allReceipts
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchaseLogs: StateFlow<List<PurchaseItemLog>> = repository.allPurchaseLogs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGpsRouteLogs: StateFlow<List<GpsRouteLog>> = repository.allGpsRouteLogs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCardioLogs: StateFlow<List<CardioLog>> = repository.allCardioLogs
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalGpsDistanceMeters: StateFlow<Float> = repository.totalGpsDistanceMeters
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0f)

    val roomDietaryProfile: StateFlow<DietaryProfile?> = repository.dietaryProfile
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val allWorkoutSchedules: StateFlow<List<WorkoutSchedule>> = repository.allWorkoutSchedules
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

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

        val todayGym = gymLogs.filter { it.dateMillis in dayStart until dayEnd }
        val gymCals = todayGym.sumOf { log ->
            val duration = log.durationMinutes.coerceAtLeast(1)
            (duration * (weight * 0.08f)).toInt().coerceIn(80, 800)
        }

        val todayRuns = gpsLogs.filter { it.dateMillis in dayStart until dayEnd }
        val runsCals = todayRuns.sumOf { it.caloriesBurned.toInt() } + if (selectedIsToday) activeGps.caloriesBurned.toInt() else 0

        val todayCardio = cardioLogs.filter { it.dateMillis in dayStart until dayEnd }
        val manualCardioCals = todayCardio.sumOf { it.caloriesBurned }

        val totalWorkoutBurned = gymCals + runsCals + manualCardioCals
        val hasWorkoutToday = totalWorkoutBurned > 0 || todayGym.isNotEmpty() || todayRuns.isNotEmpty() || todayCardio.isNotEmpty() || (selectedIsToday && activeGps.isRunning)

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
        scope = scope,
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

    init {
        scope.launch(Dispatchers.IO) {
            try {
                WorkoutAudioHapticHelper.init(application)
            } catch (e: Exception) {
                AppLog.e("VirtusSharedState", "Erro ao inicializar WorkoutAudioHapticHelper", e)
            }
        }

        scope.launch(Dispatchers.IO) {
            try {
                if (repository.getFoodsCount() < PrepopulatedData.defaultFoods.size) {
                    repository.insertFoods(PrepopulatedData.defaultFoods)
                }
                val activeDiet = repository.activeDietPlan.firstOrNull()
                if (activeDiet == null) {
                    repository.addDietPlan(PrepopulatedData.defaultDietPlan)
                    repository.insertWeeklyDietPlans(PrepopulatedData.defaultWeeklyDietPlans)
                }
                val currentMeals = repository.mealPlanItems.firstOrNull() ?: emptyList()
                if (currentMeals.isEmpty()) {
                    repository.insertMealPlanItems(PrepopulatedData.defaultMealPlan)
                }
                val currentRoutines = repository.workoutRoutines.firstOrNull() ?: emptyList()
                val isOldDefaultRoutines = currentRoutines.any {
                    it.title.contains("Peitoral, Deltoide Anterior e Tríceps") ||
                        it.title.contains("D — Ombros Completo") ||
                        it.letter == "D"
                }
                if (currentRoutines.isEmpty() || isOldDefaultRoutines) {
                    for (r in currentRoutines) {
                        repository.deleteWorkoutRoutine(r.id)
                    }
                    for (routine in PrepopulatedData.defaultRoutines) {
                        repository.addWorkoutRoutine(routine)
                    }
                    for (exercise in PrepopulatedData.defaultExercises) {
                        repository.addWorkoutExercise(exercise)
                    }
                }
            } catch (e: Exception) {
                AppLog.e("VirtusSharedState", "Erro ao popular dados iniciais", e)
            }
        }

        scope.launch {
            RunningForegroundService.session.collect { serviceSession ->
                if (serviceSession.isRunning || _activeGpsSession.value.isRunning) {
                    _activeGpsSession.value = serviceSession
                }
            }
        }

        scope.launch(Dispatchers.IO) {
            val preferences = application.getSharedPreferences("daily_tracking", Context.MODE_PRIVATE)
            val today = currentDateKey()
            if (preferences.getString("meal_completion_date", null) != today) {
                repository.resetDailyMeals()
                autoLogFixedItems()
                preferences.edit().putString("meal_completion_date", today).apply()
            }
            autoLogFixedItems()
            combine(repository.recentIntakeHistory, _selectedDateMillis) { history, _ -> history }
                .collect { history ->
                    _waterDrankMl.value = history.firstOrNull { it.dateString == selectedDateKey() }?.waterDrankMl ?: 0
                }
        }
    }

    fun autoLogFixedItems() {
        scope.launch {
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

    fun persistSelectedIntake() {
        scope.launch { persistIntakeForDate(selectedDateKey()) }
    }

    suspend fun persistIntakeForDate(dateKey: String) {
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

    suspend fun syncDataFromCloud() {
        val uid = userProfile.value.firebaseUid
        if (uid.isNotBlank()) {
            syncFavoritesFromCloud()
            firestoreSync.fetchMealPlansFromCloud(uid).onSuccess { cloudItems ->
                repository.clearMealPlanItems()
                repository.insertMealPlanItems(cloudItems)
            }
            firestoreSync.fetchFoodRecordsFromCloud(uid).onSuccess { cloudRecords ->
                repository.clearFoodRecords()
                repository.insertFoodRecords(cloudRecords)
            }
        }
    }

    fun syncFavoritesFromCloud() {
        scope.launch {
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
                    AppLog.e("VirtusSharedState", "Erro ao sincronizar favoritos: ${it.message}", it)
                }
            }
        }
    }

    fun triggerAutomaticWorkoutExerciseMigration() {
        scope.launch {
            val uid = userProfile.value.firebaseUid.ifBlank { authService.currentUser?.uid.orEmpty() }
            if (uid.isNotBlank() && !uid.startsWith("guest_")) {
                val localExercises = repository.allExercises.firstOrNull().orEmpty()
                val migrationService = WorkoutMigrationService(application)
                migrationService.migrateUserWorkoutsAndExercises(uid, preserveLegacyDocuments = false)
                if (localExercises.isNotEmpty()) {
                    migrationService.migrateLocalExercisesToSubcollections(localExercises, uid)
                }
            }
        }
    }

    fun setDarkMode(isDark: Boolean) {
        scope.launch {
            val prof = userProfile.value
            val updated = prof.copy(isDarkMode = isDark)
            repository.saveUserProfile(updated)
            if (updated.firebaseUid.isNotBlank() && !updated.firebaseUid.startsWith("guest_")) {
                firestoreSync.recordLocalProfileMutation(updated.firebaseUid)
                firestoreSync.syncProfileToCloud(updated)
            }
        }
    }

    fun toggleDarkMode() {
        setDarkMode(!userProfile.value.isDarkMode)
    }

    fun exportPdfProgressReport(context: Context) {
        scope.launch {
            try {
                val prof = userProfile.value
                PdfExporter.generateAndShareProgressReport(
                    context,
                    prof,
                    mealPlanItems.value,
                    workoutRoutines.value,
                    allExercises.value,
                    progressLogs.value,
                    shoppingList.value
                )
            } catch (e: Exception) {
                AppLog.e("VirtusSharedState", "Erro ao exportar PDF", e)
                _toastMessage.value = "Erro ao exportar PDF: ${e.localizedMessage}"
            }
        }
    }
}

/**
 * Base class for domain ViewModels that provides access to the shared application state
 * without duplicating any `StateFlow` instances.
 */
abstract class BaseVirtusDomainViewModel(application: Application) : AndroidViewModel(application) {
    protected val shared: VirtusSharedState = (application as VirtusApplication).sharedState
    protected val repository: VirtusRepository get() = shared.repository
    val authService: FirebaseAuthService get() = shared.authService
    val firestoreSync: FirestoreSyncManager get() = shared.firestoreSync
    protected val syncController: SyncController get() = shared.syncController
    protected val authController: AuthController get() = shared.authController

    val userProfile: StateFlow<UserProfile> get() = shared.userProfile
    val toastMessage: StateFlow<String?> get() = shared.toastMessage
    val selectedDateMillis: StateFlow<Long> get() = shared.selectedDateMillis
    val dailyEnergyExpenditure: StateFlow<DailyEnergyExpenditure> get() = shared.dailyEnergyExpenditure
    val waterDrankMl: StateFlow<Int> get() = shared.waterDrankMl
    val activeWorkout: StateFlow<ActiveWorkoutSession> get() = shared.activeWorkout
    val activeGpsSession: StateFlow<ActiveGpsSession> get() = shared.activeGpsSession

    val allFoods: StateFlow<List<FoodItem>> get() = shared.allFoods
    val allDietPlans: StateFlow<List<DietPlan>> get() = shared.allDietPlans
    val activeDietPlan: StateFlow<DietPlan?> get() = shared.activeDietPlan
    val mealPlanItems: StateFlow<List<MealPlanItem>> get() = shared.mealPlanItems
    val shoppingList: StateFlow<List<ShoppingItem>> get() = shared.shoppingList
    val allFoodRecords: StateFlow<List<FoodRecord>> get() = shared.allFoodRecords
    val weeklyDietPlans: StateFlow<List<WeeklyDietPlan>> get() = shared.weeklyDietPlans
    val allWorkoutPlans: StateFlow<List<WorkoutPlan>> get() = shared.allWorkoutPlans
    val activeWorkoutPlan: StateFlow<WorkoutPlan?> get() = shared.activeWorkoutPlan
    val workoutRoutines: StateFlow<List<WorkoutRoutine>> get() = shared.workoutRoutines
    val allExercises: StateFlow<List<WorkoutExercise>> get() = shared.allExercises
    val workoutLogs: StateFlow<List<WorkoutLog>> get() = shared.workoutLogs
    val totalWorkoutVolume: StateFlow<Float> get() = shared.totalWorkoutVolume
    val totalWorkoutCount: StateFlow<Int> get() = shared.totalWorkoutCount
    val progressLogs: StateFlow<List<ProgressLog>> get() = shared.progressLogs
    val recentIntakeHistory: StateFlow<List<DailyIntakeHistory>> get() = shared.recentIntakeHistory
    val allReceipts: StateFlow<List<PurchaseReceipt>> get() = shared.allReceipts
    val allPurchaseLogs: StateFlow<List<PurchaseItemLog>> get() = shared.allPurchaseLogs
    val allGpsRouteLogs: StateFlow<List<GpsRouteLog>> get() = shared.allGpsRouteLogs
    val allCardioLogs: StateFlow<List<CardioLog>> get() = shared.allCardioLogs
    val totalGpsDistanceMeters: StateFlow<Float> get() = shared.totalGpsDistanceMeters
    val roomDietaryProfile: StateFlow<DietaryProfile?> get() = shared.roomDietaryProfile
    val allWorkoutSchedules: StateFlow<List<WorkoutSchedule>> get() = shared.allWorkoutSchedules

    val isFirestoreSyncing: StateFlow<Boolean> get() = shared.isFirestoreSyncing
    val cloudWorkoutLogs: StateFlow<List<WorkoutLog>> get() = shared.cloudWorkoutLogs
    val isSyncing: StateFlow<Boolean> get() = syncController.isSyncing
    val pendingSyncItems: StateFlow<List<PendingSyncItem>> get() = syncController.pendingItems
    val isNetworkOnline: StateFlow<Boolean> get() = syncController.isOnline
    val lastSyncBannerNotification: StateFlow<String?> get() = syncController.lastSyncBannerNotification
    val lastSyncReport: StateFlow<SyncReport?> get() = firestoreSync.lastSyncReport

    protected val viewModelListenerJobs = mutableListOf<Job>()

    fun clearToast() = shared.clearToast()
    fun showToast(message: String) = shared.showToast(message)
    fun selectAppDate(dateMillis: Long) = shared.selectAppDate(dateMillis)
    fun selectedDateKey(): String = shared.selectedDateKey()
    fun setDarkMode(isDark: Boolean) = shared.setDarkMode(isDark)
    fun toggleDarkMode() = shared.toggleDarkMode()
    fun exportPdfProgressReport(context: Context) = shared.exportPdfProgressReport(context)

    override fun onCleared() {
        viewModelListenerJobs.forEach { it.cancel() }
        viewModelListenerJobs.clear()
        super.onCleared()
    }
}
