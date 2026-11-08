package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.example.util.BiometricAuthHelper
import com.example.ui.theme.CarbonCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AuthViewModel
import com.example.ui.CardioViewModel
import com.example.ui.DietViewModel
import com.example.ui.ShoppingViewModel
import com.example.ui.SyncViewModel
import com.example.ui.WorkoutViewModel
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.AuthAndOnboardingScreen
import com.example.ui.screens.calculator.MacroCalculatorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.diet.DietScreen
import com.example.ui.screens.profile.UserProfileScreen
import com.example.ui.screens.receipts.ReceiptsInflationScreen
import com.example.ui.screens.shopping.ShoppingListScreen
import com.example.ui.screens.workout.WorkoutScreen
import com.example.ui.screens.workout.ExerciseProgressionScreen
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import com.example.ui.screens.gps.GpsTrackerScreen
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Brush

enum class AppNavigationScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Início", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "nav_dashboard"),
    DIET("Dieta", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_diet"),
    WORKOUT("Treinos", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_workout"),
    GPS("Corrida", Icons.AutoMirrored.Filled.DirectionsRun, Icons.AutoMirrored.Outlined.DirectionsRun, "nav_running"),
    SHOPPING("Mercado", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_shopping"),
    RECEIPTS("Preços/NF", Icons.AutoMirrored.Filled.ReceiptLong, Icons.AutoMirrored.Outlined.ReceiptLong, "nav_receipts"),
    ANALYTICS("Evolução", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp, "nav_analytics"),
    EXERCISE_PROGRESSION("Cargas", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp, "nav_exercise_progression"),
    CALCULATOR("Macros", Icons.Filled.Calculate, Icons.Filled.Calculate, "nav_calculator"),
    PROFILE("Perfil", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
    AUTH("Conta", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "nav_auth")
}

class MainActivity : FragmentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val workoutViewModel: WorkoutViewModel by viewModels()
    private val dietViewModel: DietViewModel by viewModels()
    private val shoppingViewModel: ShoppingViewModel by viewModels()
    private val cardioViewModel: CardioViewModel by viewModels()
    private val syncViewModel: SyncViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppScreen(
                    authViewModel = authViewModel,
                    workoutViewModel = workoutViewModel,
                    dietViewModel = dietViewModel,
                    shoppingViewModel = shoppingViewModel,
                    cardioViewModel = cardioViewModel,
                    syncViewModel = syncViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppScreen(
    authViewModel: AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    workoutViewModel: WorkoutViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    dietViewModel: DietViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    shoppingViewModel: ShoppingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    cardioViewModel: CardioViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    syncViewModel: SyncViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val profile by authViewModel.userProfile.collectAsStateWithLifecycle()
    var currentScreen by rememberSaveable { mutableStateOf(AppNavigationScreen.DASHBOARD) }
    var isWorkoutBiometricVerified by rememberSaveable { mutableStateOf(false) }
    var isDietBiometricVerified by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by authViewModel.toastMessage.collectAsStateWithLifecycle()
    val isOnline by syncViewModel.isNetworkOnline.collectAsStateWithLifecycle()
    val isSyncing by syncViewModel.isSyncing.collectAsStateWithLifecycle()
    val pendingSyncItems by syncViewModel.pendingSyncItems.collectAsStateWithLifecycle()
    val lastSyncBannerNotification by syncViewModel.lastSyncBannerNotification.collectAsStateWithLifecycle()

    // Auto-login Google + sincronização Delta ao iniciar caso sessão Firebase esteja ativa
    LaunchedEffect(Unit) {
        authViewModel.restoreActiveFirebaseSession()
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            authViewModel.clearToast()
        }
    }

    // If user is not logged in or hasn't finished onboarding, display Auth & Onboarding directly
    val isUserAuthenticated = profile.isLoggedIn && profile.hasCompletedOnboarding

    if (!isUserAuthenticated) {
        AuthAndOnboardingScreen(
            viewModel = authViewModel,
            onAuthSuccess = {
                currentScreen = AppNavigationScreen.DASHBOARD
            }
        )
        return
    }

    // Handle back button on sub-screens
    if (currentScreen != AppNavigationScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppNavigationScreen.DASHBOARD
        }
    }

    val showBottomBar = currentScreen != AppNavigationScreen.CALCULATOR &&
            currentScreen != AppNavigationScreen.AUTH &&
            currentScreen != AppNavigationScreen.PROFILE &&
            currentScreen != AppNavigationScreen.EXERCISE_PROGRESSION

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BoxWithConstraints {
                    val compactNavigation = maxWidth < 400.dp
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        val navItems = listOf(
                            AppNavigationScreen.DASHBOARD,
                            AppNavigationScreen.DIET,
                            AppNavigationScreen.WORKOUT,
                            AppNavigationScreen.GPS,
                            AppNavigationScreen.SHOPPING,
                            AppNavigationScreen.ANALYTICS
                        )

                        navItems.forEach { screen ->
                            val isSelected = currentScreen == screen
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentScreen = screen },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = if (compactNavigation) null else {
                                    {
                                        Text(
                                            text = screen.title,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                },
                                alwaysShowLabel = !compactNavigation,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = GrowthOrange,
                                    selectedTextColor = GrowthOrangeLight,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    indicatorColor = GrowthOrange.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.testTag(screen.testTag)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            com.example.ui.sync.OfflineSyncQueueBanner(
                isOnline = isOnline,
                isSyncing = isSyncing,
                pendingItems = pendingSyncItems,
                lastSyncNotification = lastSyncBannerNotification,
                onSyncNow = { syncViewModel.syncAllDataToFirestore() },
                onDismissNotification = { syncViewModel.dismissSyncBannerNotification() }
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { target ->
                when (target) {
                    AppNavigationScreen.DASHBOARD -> DashboardScreen(
                        viewModel = dietViewModel,
                        onNavigateToDiet = { currentScreen = AppNavigationScreen.DIET },
                        onNavigateToWorkout = { currentScreen = AppNavigationScreen.WORKOUT },
                        onNavigateToShopping = { currentScreen = AppNavigationScreen.SHOPPING },
                        onNavigateToCalculator = { currentScreen = AppNavigationScreen.CALCULATOR },
                        onNavigateToAnalytics = { currentScreen = AppNavigationScreen.ANALYTICS },
                        onNavigateToProfile = { currentScreen = AppNavigationScreen.PROFILE },
                        onNavigateToReceipts = { currentScreen = AppNavigationScreen.RECEIPTS },
                        onNavigateToGps = { currentScreen = AppNavigationScreen.GPS }
                    )
                    AppNavigationScreen.DIET -> {
                        val requiresLock = profile.isBiometricLockEnabled && !isDietBiometricVerified
                        if (requiresLock) {
                            BiometricDiaryLockScreen(
                                title = "Dieta",
                                subtitle = "Diário Nutricional",
                                onSuccess = { isDietBiometricVerified = true },
                                onFallbackToFirebase = { isDietBiometricVerified = true }
                            )
                        } else {
                            DietScreen(
                                viewModel = dietViewModel,
                                onNavigateToShopping = { currentScreen = AppNavigationScreen.SHOPPING }
                            )
                        }
                    }
                    AppNavigationScreen.WORKOUT -> {
                        val requiresLock = profile.isBiometricLockEnabled && !isWorkoutBiometricVerified
                        if (requiresLock) {
                            BiometricDiaryLockScreen(
                                title = "Treinos",
                                subtitle = "Diário de Treinos",
                                onSuccess = { isWorkoutBiometricVerified = true },
                                onFallbackToFirebase = { isWorkoutBiometricVerified = true }
                            )
                        } else {
                            WorkoutScreen(
                                viewModel = workoutViewModel,
                                onNavigateToProgression = { currentScreen = AppNavigationScreen.EXERCISE_PROGRESSION }
                            )
                        }
                    }
                    AppNavigationScreen.GPS -> GpsTrackerScreen(
                        viewModel = cardioViewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                    AppNavigationScreen.SHOPPING -> ShoppingListScreen(
                        viewModel = shoppingViewModel,
                        onNavigateToReceipts = { currentScreen = AppNavigationScreen.RECEIPTS }
                    )
                    AppNavigationScreen.RECEIPTS -> ReceiptsInflationScreen(
                        viewModel = shoppingViewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                    AppNavigationScreen.ANALYTICS -> AnalyticsScreen(
                        viewModel = workoutViewModel,
                        onNavigateToProgression = { currentScreen = AppNavigationScreen.EXERCISE_PROGRESSION }
                    )
                    AppNavigationScreen.EXERCISE_PROGRESSION -> ExerciseProgressionScreen(
                        viewModel = workoutViewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.WORKOUT }
                    )
                    AppNavigationScreen.CALCULATOR -> MacroCalculatorScreen(
                        viewModel = dietViewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                    AppNavigationScreen.PROFILE -> UserProfileScreen(
                        viewModel = authViewModel,
                        workoutViewModel = workoutViewModel,
                        syncViewModel = syncViewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.DASHBOARD },
                        onNavigateToAccount = { currentScreen = AppNavigationScreen.AUTH }
                    )
                    AppNavigationScreen.AUTH -> AuthAndOnboardingScreen(
                        viewModel = authViewModel,
                        onAuthSuccess = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                }
                }
            }
        }
    }
}

@Composable
fun BiometricDiaryLockScreen(
    title: String,
    subtitle: String,
    onSuccess: () -> Unit,
    onFallbackToFirebase: () -> Unit
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity

    LaunchedEffect(Unit) {
        if (fragmentActivity != null) {
            BiometricAuthHelper.promptBiometricAuth(
                activity = fragmentActivity,
                title = "Acesso Seguro ao $title",
                subtitle = subtitle,
                description = "Confirme sua digital ou reconhecimento facial para visualizar e editar seus registros.",
                onSuccess = onSuccess,
                onError = { /* fallback available via button */ }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(GrowthOrange.copy(alpha = 0.65f), GrowthCyan.copy(alpha = 0.55f))
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = null,
                    tint = GrowthCyan,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Diário Protegido por Biometria",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Para garantir a privacidade e segurança do seu $title, confirme sua identidade.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (fragmentActivity != null) {
                            BiometricAuthHelper.promptBiometricAuth(
                                activity = fragmentActivity,
                                title = "Acesso Seguro ao $title",
                                subtitle = subtitle,
                                description = "Confirme sua digital ou reconhecimento facial.",
                                onSuccess = onSuccess,
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Text("Desbloquear com Biometria", fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onFallbackToFirebase,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.55f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan)
                ) {
                    Text("Fallback: Autenticação Firebase / Google", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                }
            }
        }
    }
}

