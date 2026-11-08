package com.example.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.ui.AuthViewModel
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.ui.theme.GrowthYellow
import com.example.data.auth.GoogleCredentialAuthHelper
import com.example.util.BiometricAuthHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthAndOnboardingScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
) {
    var onboardingStep by remember { mutableStateOf(0) } // 0: Hub de Acesso, 1: Dados Corporais, 2: Biotipo, 3: Treinos & Metas
    var showCreateProfileSetup by remember { mutableStateOf(false) }

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var lgpdAccepted by remember { mutableStateOf(true) }
    var showLgpdDialog by remember { mutableStateOf(false) }

    // Onboarding values
    var gender by remember { mutableStateOf(Gender.MASCULINO) }
    var ageText by remember { mutableStateOf("25") }
    var weightText by remember { mutableStateOf("78") }
    var heightText by remember { mutableStateOf("178") }
    var selectedBiotype by remember { mutableStateOf(BodyType.MESOMORPH) }
    var trainingDays by remember { mutableStateOf(5) }
    var goal by remember { mutableStateOf(FitnessGoal.BULKING_CLEAN) }
    var isAthlete by remember { mutableStateOf(true) }

    if (onboardingStep > 0) {
        BackHandler {
            onboardingStep -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        // High-impact Fitness Dark Premium Hero Background with Orange & Cyan Neon Rim Lighting
        Image(
            painter = painterResource(id = R.drawable.img_virtus_login_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            CarbonDark.copy(alpha = if (onboardingStep == 0) 0.42f else 0.78f),
                            CarbonDark.copy(alpha = 0.85f),
                            CarbonDark.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Crest & Identity
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (onboardingStep > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { onboardingStep -= 1 }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Surface(
                            color = GrowthOrange.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "PASSO $onboardingStep DE 3",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GrowthOrangeLight,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(40.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Virtus Metallic Neon Crest Emblem (1:1 aspect ratio, zero distortion)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (onboardingStep == 0) 92.dp else 64.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0D0F12))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(GrowthOrange, GrowthCyan)
                            ),
                            shape = RoundedCornerShape(22.dp)
                        )
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_virtus_brand_emblem),
                        contentDescription = "Emblema Virtus - Dieta & Treino",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = CarbonCardElevated.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "PERFORMANCE • HIPERTROFIA • NUTRIÇÃO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GrowthCyan,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Virtus - Dieta & Treino",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.5.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (onboardingStep) {
                        0 -> "Seu ecossistema inteligente de treinos, macros e evolução física."
                        1 -> "Passo 1/3: Medidas Biométricas para Cálculo de TMB"
                        2 -> "Passo 2/3: Guia Visual e Escolha do seu Biotipo"
                        else -> "Passo 3/3: Frequência Semanal e Meta Nutricional"
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dismissible Error Banner
            if (authError != null && onboardingStep == 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GrowthRed.copy(alpha = 0.16f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GrowthRed.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = GrowthRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = authError ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { viewModel.clearAuthError() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = GrowthRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (onboardingStep == 0) {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val credentialManager = remember { CredentialManager.create(context) }
                val fragmentActivity = context as? FragmentActivity
                val biometricSupport = remember(context) {
                    BiometricAuthHelper.checkBiometricSupport(context)
                }

                // Auto-login Google silencioso ao abrir se já houver sessão Firebase ou credencial autorizada
                LaunchedEffect(Unit) {
                    if (!userProfile.isLoggedIn) {
                        GoogleCredentialAuthHelper.attemptSilentAutoSignIn(
                            context = context,
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onCredentialObtained = { authCredential ->
                                viewModel.loginWithGoogleCredential(authCredential, onAuthSuccess)
                            },
                            onAlreadyAuthenticated = {
                                viewModel.restoreActiveFirebaseSession(onRestored = onAuthSuccess)
                            }
                        )
                    }
                }

                val hasConfiguredEmail = userProfile.email.isNotBlank() &&
                    userProfile.email.contains("@") &&
                    userProfile.email.contains(".") &&
                    !userProfile.email.endsWith("@growth.local") &&
                    !userProfile.firebaseUid.startsWith("guest_")
                val hasConfiguredProfile = userProfile.hasCompletedOnboarding &&
                    userProfile.weightKg > 30f &&
                    userProfile.name.isNotBlank() &&
                    userProfile.name != "Atleta"
                val isBiometricsReady = hasConfiguredEmail && hasConfiguredProfile

                // Main Access Hub Card (Google + Biometria + Criar Perfil + Visitante)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard.copy(alpha = 0.94f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GrowthOrange.copy(alpha = 0.65f), GrowthCyan.copy(alpha = 0.55f))
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 1. GOOGLE SIGN-IN (Ação Principal — Laranja #FF6D00 + Delta Sync Merge)
                        Button(
                            onClick = {
                                GoogleCredentialAuthHelper.launchInteractiveGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    scope = coroutineScope,
                                    onCredentialObtained = { authCredential ->
                                        viewModel.loginWithGoogleCredential(authCredential, onAuthSuccess)
                                    },
                                    onCancelled = {
                                        viewModel.clearAuthError()
                                    },
                                    onError = { friendlyError ->
                                        viewModel.showToast(friendlyError)
                                    }
                                )
                            },
                            enabled = !isAuthLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("google_signin_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GrowthOrange,
                                contentColor = Color.White
                            )
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = "Entrar com Conta Google",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Mescla seus dados locais com a nuvem (Delta Sync)",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White.copy(alpha = 0.88f)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. BIOMETRIC SIGN-IN (Ação Secundária — Ciano #00E5FF)
                        OutlinedButton(
                            onClick = {
                                if (!isBiometricsReady) {
                                    viewModel.showToast("Vincule sua conta Google ou conclua seu perfil primeiro para liberar a biometria.")
                                    return@OutlinedButton
                                }
                                if (fragmentActivity != null) {
                                    BiometricAuthHelper.promptBiometricAuth(
                                        activity = fragmentActivity,
                                        title = "Login Biométrico Virtus",
                                        subtitle = "Perfil: ${userProfile.email}",
                                        description = "Confirme sua digital ou reconhecimento facial para entrar.",
                                        onSuccess = {
                                            viewModel.loginWithBiometrics(onSuccess = onAuthSuccess)
                                        },
                                        onError = { errorMsg ->
                                            viewModel.showToast(errorMsg)
                                        }
                                    )
                                } else {
                                    viewModel.showToast("Sensor biométrico (${biometricSupport.label}) indisponível neste momento.")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("biometric_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isBiometricsReady) GrowthCyan else GrowthCyan.copy(alpha = 0.45f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isBiometricsReady) GrowthCyan.copy(alpha = 0.14f) else CarbonCardElevated,
                                contentColor = GrowthCyan
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometria",
                                    tint = GrowthCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isBiometricsReady) {
                                        "Entrar com Biometria (${userProfile.name})"
                                    } else {
                                        "Entrar com Biometria (Digital / Face)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = GrowthCyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CarbonBorder)
                            Text(
                                text = " OU INICIAR NO APARELHO ",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = CarbonBorder)
                        }

                        // 3. CRIAR PERFIL & CALCULAR METAS (3-Step Guided Onboarding)
                        Button(
                            onClick = {
                                if (!showCreateProfileSetup) {
                                    showCreateProfileSetup = true
                                } else if (lgpdAccepted) {
                                    onboardingStep = 1
                                } else {
                                    viewModel.showToast("Confirme os Termos de Privacidade (LGPD) para criar seu perfil.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showCreateProfileSetup) GrowthOrange else CarbonCardElevated,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (showCreateProfileSetup) Color.White else GrowthOrangeLight,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (showCreateProfileSetup) {
                                    "AVANÇAR PARA MEDIDAS & BIOTIPO (1/3)"
                                } else {
                                    "Criar Meu Perfil & Calcular Metas"
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = if (showCreateProfileSetup) Color.White else GrowthOrangeLight
                            )
                        }

                        AnimatedVisibility(visible = showCreateProfileSetup) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Como prefere ser chamado(a)?") },
                                    placeholder = { Text("Ex: Patrick") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = null,
                                            tint = GrowthOrange
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("auth_name_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GrowthOrange,
                                        unfocusedBorderColor = GrowthCyan.copy(alpha = 0.4f)
                                    )
                                )

                                Surface(
                                    color = CarbonCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, if (lgpdAccepted) GrowthCyan.copy(alpha = 0.5f) else CarbonBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = lgpdAccepted,
                                            onCheckedChange = { lgpdAccepted = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = GrowthOrange,
                                                checkmarkColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("lgpd_checkbox")
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Proteção de Dados (LGPD)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            Text(
                                                text = "Seus dados corporais são usados apenas para calcular suas metas de treino e dieta.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        TextButton(onClick = { showLgpdDialog = true }) {
                                            Text("Termos", fontSize = 10.sp, color = GrowthCyan)
                                        }
                                    }
                                }
                            }
                        }

                        // 4. CONTINUAR COMO VISITANTE (Modo Local com fusão futura via Delta Sync)
                        OutlinedButton(
                            onClick = { viewModel.enterAsGuest(onSuccess = onAuthSuccess) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("guest_login_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CarbonCardElevated.copy(alpha = 0.5f),
                                contentColor = GrowthCyan
                            ),
                            border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Explorar como Visitante (Mescla com Google depois)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthCyan
                            )
                        }
                    }
                }
            } else if (onboardingStep == 1) {
                // Step 1: Body Metrics (Altura, Peso, Idade, Gênero)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PASSO 1 DE 3: SUAS MEDIDAS ATUAIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Base para Taxa Metabólica Basal & Macros",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Sexo Biológico:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = gender == Gender.MASCULINO,
                                onClick = { gender = Gender.MASCULINO },
                                label = { Text("Masculino", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = gender == Gender.FEMININO,
                                onClick = { gender = Gender.FEMININO },
                                label = { Text("Feminino", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Peso (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboard_weight_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrowthOrange,
                                    unfocusedBorderColor = CarbonBorder
                                )
                            )
                            OutlinedTextField(
                                value = heightText,
                                onValueChange = { heightText = it },
                                label = { Text("Altura (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("onboard_height_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrowthOrange,
                                    unfocusedBorderColor = CarbonBorder
                                )
                            )
                            OutlinedTextField(
                                value = ageText,
                                onValueChange = { ageText = it },
                                label = { Text("Idade") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrowthOrange,
                                    unfocusedBorderColor = CarbonBorder
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onboardingStep = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("PRÓXIMO: ESCOLHER BIOTIPO", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else if (onboardingStep == 2) {
                // Step 2: Biotype Guide & Selection (Uncropped 16:9 Fit Illustration)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PASSO 2 DE 3: SEU BIOTIPO CORPORAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Guia Visual de Biotipos (Corpo Inteiro)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Uncropped 16:9 Biotype Guide Image with ContentScale.Fit
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0D0F12),
                            border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_virtus_biotypes_guide),
                                contentDescription = "Guia Ilustrado de Biotipos: Ectomorfo, Mesomorfo e Endomorfo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        BodyType.values().forEach { biotype ->
                            val isSelected = selectedBiotype == biotype
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GrowthOrange.copy(alpha = 0.15f) else CarbonCardElevated
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GrowthOrange else CarbonBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedBiotype = biotype }
                                    .testTag("biotype_card_${biotype.name}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = biotype.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onBackground
                                        )
                                        if (isSelected) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GrowthOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = biotype.subtitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GrowthCyan
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = biotype.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        color = CarbonCard,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💡 Estratégia: ${biotype.dietStrategy}",
                                            fontSize = 10.sp,
                                            color = GrowthYellow,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { onboardingStep = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("PRÓXIMO: FREQUÊNCIA E OBJETIVO", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Step 3: Training frequency & Goal
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard.copy(alpha = 0.95f)),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "PASSO 3 DE 3: FREQUÊNCIA & METAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sua Rotina de Treinos e Objetivo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Quantos dias você treina por semana?", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (3..6).forEach { days ->
                                FilterChip(
                                    selected = trainingDays == days,
                                    onClick = { trainingDays = days },
                                    label = { Text("${days}x/sem", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GrowthOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Objetivo Principal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))

                        FitnessGoal.values().forEach { fg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { goal = fg }
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (goal == fg) GrowthGreen else CarbonCardElevated,
                                    shape = CircleShape,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (goal == fg) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.Black,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = fg.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (goal == fg) FontWeight.Bold else FontWeight.Normal,
                                    color = if (goal == fg) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val w = weightText.replace(",", ".").toFloatOrNull() ?: 78f
                                val h = heightText.replace(",", ".").toFloatOrNull() ?: 178f
                                val a = ageText.toIntOrNull() ?: 25

                                viewModel.completeOnboardingProfile(
                                    name = name.ifBlank { "Atleta Virtus" },
                                    email = userProfile.email.ifBlank { "atleta@growth.local" },
                                    gender = gender,
                                    age = a,
                                    weightKg = w,
                                    heightCm = h,
                                    biotype = selectedBiotype,
                                    trainingDays = trainingDays,
                                    activityLevel = if (trainingDays >= 5) ActivityLevel.VERY_ACTIVE else ActivityLevel.MODERATE,
                                    goal = goal,
                                    isAthlete = isAthlete
                                )
                                onAuthSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isAuthLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("finish_onboarding_btn")
                        ) {
                            if (isAuthLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = "ATIVAR MEU PROTOCOLO VIRTUS 💪",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // LGPD Terms Dialog
        if (showLgpdDialog) {
            AlertDialog(
                onDismissRequest = { showLgpdDialog = false },
                containerColor = CarbonCard,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Termos de Privacidade & LGPD",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018 - LGPD):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight
                        )
                        Text(
                            text = "1. Finalidade: Seus dados biométricos (peso, altura, idade, biotipo e rotina) são utilizados exclusivamente para calcular suas metas calóricas, divisão de macronutrientes e evolução de cargas.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "2. Armazenamento & Delta Sync: Os dados ficam salvos no seu dispositivo e, quando vinculado à sua Conta Google, são sincronizados com segurança via Cloud Firestore.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "3. Controle Total: Você pode exportar relatórios ou excluir sua conta e todos os dados locais/remotos a qualquer momento na aba Perfil.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            lgpdAccepted = true
                            showLgpdDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                    ) {
                        Text("Aceitar & Continuar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLgpdDialog = false }) {
                        Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}
