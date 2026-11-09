package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuthViewModel
import com.example.ui.SyncViewModel
import com.example.ui.WorkoutViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.components.MuscleHeatmapView
import com.example.ui.screens.achievements.components.AchievementsSummaryBannerCard
import com.example.ui.screens.profile.components.BiometricSecurityCard
import com.example.ui.screens.profile.components.BiometricsOverviewCard
import com.example.ui.screens.profile.components.FirestoreSyncCard
import com.example.ui.screens.profile.components.ProfileAccountCard
import com.example.ui.screens.profile.components.ProfileFooterAndActionsSection
import com.example.ui.screens.profile.components.ProfileMeasurementsFormCards
import com.example.ui.screens.profile.components.ResetDatabaseConfirmDialog
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthRed
import com.example.ui.theme.GrowthYellow
import com.example.util.MacroCalculator

@Composable
fun UserProfileScreen(
    viewModel: AuthViewModel,
    workoutViewModel: WorkoutViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    syncViewModel: SyncViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {},
    onNavigateToAchievements: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val achievementsSummary by viewModel.achievementsState.collectAsStateWithLifecycle()

    var nameInput by remember { mutableStateOf(profile.name) }
    var weightInputText by remember { mutableStateOf(profile.weightKg.toString()) }
    var heightInputText by remember { mutableStateOf(profile.heightCm.toInt().toString()) }
    var ageInputText by remember { mutableStateOf(profile.age.toString()) }
    var selectedGender by remember { mutableStateOf(profile.gender) }
    var selectedActivityLevel by remember { mutableStateOf(profile.activityLevel) }
    var selectedGoal by remember { mutableStateOf(profile.goal) }
    var selectedBiotype by remember { mutableStateOf(profile.biotype) }
    var recomputeMacrosAuto by remember { mutableStateOf(true) }
    var showResetDatabaseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile) {
        nameInput = profile.name
        weightInputText = profile.weightKg.toString()
        heightInputText = profile.heightCm.toInt().toString()
        ageInputText = profile.age.toString()
        selectedGender = profile.gender
        selectedActivityLevel = profile.activityLevel
        selectedGoal = profile.goal
        selectedBiotype = profile.biotype
    }

    val parsedWeight = weightInputText.replace(",", ".").toFloatOrNull() ?: profile.weightKg
    val parsedHeight = heightInputText.replace(",", ".").toFloatOrNull() ?: profile.heightCm
    val parsedAge = ageInputText.toIntOrNull() ?: profile.age

    val heightMeters = (parsedHeight / 100f).coerceAtLeast(0.5f)
    val liveBmi = parsedWeight / (heightMeters * heightMeters)
    val idealMinWeight = 18.5f * heightMeters * heightMeters
    val idealMaxWeight = 24.9f * heightMeters * heightMeters
    val liveBmr = MacroCalculator.calculateBMR(parsedWeight, parsedHeight, parsedAge, selectedGender)
    val liveWaterGoal = MacroCalculator.calculateWaterIntakeMl(parsedWeight, parsedAge, true)

    val (bmiLabel, bmiColor) = when {
        liveBmi < 18.5f -> "Abaixo do peso" to GrowthYellow
        liveBmi < 25.0f -> "Peso normal / Saudável" to GrowthGreen
        liveBmi < 30.0f -> "Sobrepeso" to GrowthOrange
        else -> "Obesidade" to GrowthRed
    }

    var heatmapDays by remember { mutableIntStateOf(30) }
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()
    val workoutRoutines by viewModel.workoutRoutines.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val muscleVolume = remember(heatmapDays, workoutLogs, workoutRoutines, allExercises) {
        workoutViewModel.getMuscleVolume(heatmapDays)
    }

    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val isFirestoreSyncing by viewModel.isFirestoreSyncing.collectAsStateWithLifecycle()
    val isSyncBusy = isSyncing || isFirestoreSyncing
    val lastSyncReport by viewModel.lastSyncReport.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        GrowthHeader(
            title = "Perfil & Biometria",
            subtitle = "Suas medidas, metas e evolução corporal",
            badgeText = "DADOS PESSOAIS",
            trailingContent = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("profile_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "profile_muscle_heatmap", contentType = "heatmap") {
                MuscleHeatmapView(
                    muscleVolumes = muscleVolume,
                    selectedTimeframeDays = heatmapDays,
                    onTimeframeSelected = { heatmapDays = it }
                )
            }

            item(key = "profile_account_card", contentType = "account_card") {
                ProfileAccountCard(
                    profile = profile,
                    onNavigateToAccount = onNavigateToAccount
                )
            }

            item(key = "profile_achievements_summary", contentType = "achievements_card") {
                AchievementsSummaryBannerCard(
                    summary = achievementsSummary,
                    onOpenGallery = onNavigateToAchievements
                )
            }

            item(key = "profile_biometrics_overview", contentType = "overview_card") {
                BiometricsOverviewCard(
                    profile = profile,
                    nameInput = nameInput,
                    selectedGender = selectedGender,
                    parsedWeight = parsedWeight,
                    parsedHeight = parsedHeight,
                    parsedAge = parsedAge,
                    liveBmi = liveBmi,
                    bmiLabel = bmiLabel,
                    bmiColor = bmiColor,
                    liveBmr = liveBmr,
                    idealMinWeight = idealMinWeight,
                    idealMaxWeight = idealMaxWeight,
                    liveWaterGoal = liveWaterGoal,
                    achievementsSummary = achievementsSummary,
                    onOpenAchievements = onNavigateToAchievements
                )
            }

            item(key = "profile_measurements_form", contentType = "form_cards") {
                ProfileMeasurementsFormCards(
                    nameInput = nameInput,
                    onNameChange = { nameInput = it },
                    selectedGender = selectedGender,
                    onGenderChange = { selectedGender = it },
                    weightInputText = weightInputText,
                    parsedWeight = parsedWeight,
                    onWeightTextChange = { weightInputText = it },
                    heightInputText = heightInputText,
                    parsedHeight = parsedHeight,
                    onHeightTextChange = { heightInputText = it },
                    ageInputText = ageInputText,
                    parsedAge = parsedAge,
                    onAgeTextChange = { ageInputText = it },
                    selectedActivityLevel = selectedActivityLevel,
                    onActivityLevelChange = { selectedActivityLevel = it },
                    selectedGoal = selectedGoal,
                    onGoalChange = { selectedGoal = it },
                    recomputeMacrosAuto = recomputeMacrosAuto,
                    onRecomputeMacrosChange = { recomputeMacrosAuto = it }
                )
            }

            item(key = "profile_biometric_security", contentType = "security_card") {
                BiometricSecurityCard(
                    isBiometricLockEnabled = profile.isBiometricLockEnabled,
                    onToggleBiometricLock = { viewModel.toggleBiometricLock(it) },
                    onRestoreDefaultRoutines = { workoutViewModel.restoreDefaultRoutines() },
                    onShowToast = { viewModel.showToast(it) }
                )
            }

            item(key = "profile_save_btn", contentType = "save_button") {
                Button(
                    onClick = {
                        viewModel.updateUserBiometrics(
                            weightKg = parsedWeight,
                            heightCm = parsedHeight,
                            age = parsedAge,
                            activityLevel = selectedActivityLevel,
                            name = nameInput.ifBlank { profile.name },
                            gender = selectedGender,
                            goal = selectedGoal,
                            biotype = selectedBiotype,
                            recomputeMacros = recomputeMacrosAuto,
                            onSuccess = onNavigateBack
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_biometrics_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Salvar Perfil e Metas",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            item(key = "profile_firestore_sync", contentType = "sync_card") {
                FirestoreSyncCard(
                    profile = profile,
                    isSyncBusy = isSyncBusy,
                    lastSyncReport = lastSyncReport,
                    onUploadProfile = { syncViewModel.uploadProfileToCloud() },
                    onDownloadProfile = { syncViewModel.downloadProfileFromCloud() },
                    onSyncAllData = { syncViewModel.syncAllDataToFirestore() }
                )
            }

            item(key = "profile_footer_and_actions", contentType = "footer_actions") {
                ProfileFooterAndActionsSection(
                    onRestoreBiometrics = {
                        nameInput = profile.name
                        weightInputText = profile.weightKg.toString()
                        heightInputText = profile.heightCm.toInt().toString()
                        ageInputText = profile.age.toString()
                        selectedGender = profile.gender
                        selectedActivityLevel = profile.activityLevel
                        selectedGoal = profile.goal
                        selectedBiotype = profile.biotype
                    },
                    onLogout = { viewModel.logout() },
                    onRequestResetDatabase = { showResetDatabaseDialog = true }
                )
            }
        }

        if (showResetDatabaseDialog) {
            ResetDatabaseConfirmDialog(
                onConfirm = {
                    viewModel.clearAllUsersAndResetDatabase()
                    showResetDatabaseDialog = false
                },
                onDismiss = { showResetDatabaseDialog = false }
            )
        }
    }
}
