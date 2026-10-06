package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GrowthViewModel
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.AuthAndOnboardingScreen
import com.example.ui.screens.calculator.MacroCalculatorScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.diet.DietScreen
import com.example.ui.screens.receipts.ReceiptsInflationScreen
import com.example.ui.screens.shopping.ShoppingListScreen
import com.example.ui.screens.workout.WorkoutScreen
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.MyApplicationTheme

enum class AppNavigationScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Início", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "nav_dashboard"),
    DIET("Dieta", Icons.Filled.Restaurant, Icons.Outlined.Restaurant, "nav_diet"),
    WORKOUT("Treinos", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter, "nav_workout"),
    SHOPPING("Mercado", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_shopping"),
    RECEIPTS("Preços/NF", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_receipts"),
    ANALYTICS("Evolução", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp, "nav_analytics"),
    CALCULATOR("Macros", Icons.Filled.Calculate, Icons.Filled.Calculate, "nav_calculator"),
    AUTH("Conta", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "nav_auth")
}

class MainActivity : ComponentActivity() {

    private val viewModel: GrowthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: GrowthViewModel) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AppNavigationScreen.DASHBOARD) }
    val snackbarHostState = remember { SnackbarHostState() }
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // If user is not logged in or hasn't finished onboarding, display Auth & Onboarding directly
    val isUserAuthenticated = profile.isLoggedIn && profile.hasCompletedOnboarding

    if (!isUserAuthenticated) {
        AuthAndOnboardingScreen(
            viewModel = viewModel,
            onAuthSuccess = {
                currentScreen = AppNavigationScreen.DASHBOARD
            }
        )
        return
    }

    // Handle back button on sub-screens
    if (currentScreen != AppNavigationScreen.DASHBOARD && currentScreen != AppNavigationScreen.AUTH) {
        BackHandler {
            currentScreen = AppNavigationScreen.DASHBOARD
        }
    }

    val showBottomBar = currentScreen != AppNavigationScreen.CALCULATOR && currentScreen != AppNavigationScreen.AUTH

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CarbonDark,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = CarbonCard,
                    tonalElevation = 8.dp
                ) {
                    val navItems = listOf(
                        AppNavigationScreen.DASHBOARD,
                        AppNavigationScreen.DIET,
                        AppNavigationScreen.WORKOUT,
                        AppNavigationScreen.SHOPPING,
                        AppNavigationScreen.RECEIPTS,
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
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GrowthOrange,
                                selectedTextColor = GrowthOrangeLight,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = GrowthOrange.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.testTag(screen.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    AppNavigationScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToDiet = { currentScreen = AppNavigationScreen.DIET },
                        onNavigateToWorkout = { currentScreen = AppNavigationScreen.WORKOUT },
                        onNavigateToShopping = { currentScreen = AppNavigationScreen.SHOPPING },
                        onNavigateToCalculator = { currentScreen = AppNavigationScreen.CALCULATOR },
                        onNavigateToAnalytics = { currentScreen = AppNavigationScreen.ANALYTICS }
                    )
                    AppNavigationScreen.DIET -> DietScreen(
                        viewModel = viewModel,
                        onNavigateToShopping = { currentScreen = AppNavigationScreen.SHOPPING }
                    )
                    AppNavigationScreen.WORKOUT -> WorkoutScreen(
                        viewModel = viewModel
                    )
                    AppNavigationScreen.SHOPPING -> ShoppingListScreen(
                        viewModel = viewModel
                    )
                    AppNavigationScreen.RECEIPTS -> ReceiptsInflationScreen(
                        viewModel = viewModel
                    )
                    AppNavigationScreen.ANALYTICS -> AnalyticsScreen(
                        viewModel = viewModel
                    )
                    AppNavigationScreen.CALCULATOR -> MacroCalculatorScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                    AppNavigationScreen.AUTH -> AuthAndOnboardingScreen(
                        viewModel = viewModel,
                        onAuthSuccess = { currentScreen = AppNavigationScreen.DASHBOARD }
                    )
                }
            }
        }
    }
}
