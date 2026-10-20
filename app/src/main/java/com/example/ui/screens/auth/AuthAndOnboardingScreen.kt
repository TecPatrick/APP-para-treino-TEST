package com.example.ui.screens.auth

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricAuthHelper
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.ui.VirtusViewModel
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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthAndOnboardingScreen(
    viewModel: VirtusViewModel,
    onAuthSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var onboardingStep by remember { mutableStateOf(0) } // 0: Login/Register, 1: Dados Corporais, 2: Biotipo, 3: Treinos & Metas

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var lgpdAccepted by remember { mutableStateOf(false) }
    var showLgpdDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }

    // Onboarding values
    var gender by remember { mutableStateOf(Gender.MASCULINO) }
    var ageText by remember { mutableStateOf("25") }
    var weightText by remember { mutableStateOf("78") }
    var heightText by remember { mutableStateOf("178") }
    var selectedBiotype by remember { mutableStateOf(BodyType.MESOMORPH) }
    var trainingDays by remember { mutableStateOf(5) }
    var activityLevel by remember { mutableStateOf(ActivityLevel.MODERATE) }
    var goal by remember { mutableStateOf(FitnessGoal.BULKING_CLEAN) }
    var isAthlete by remember { mutableStateOf(true) }

    // Password criteria computation
    val hasMinLength = password.length >= 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword

    val strengthScore = listOf(hasMinLength, hasUpper, hasLower, hasDigit, hasSpecial).count { it }
    val (strengthLabel, strengthColor) = when (strengthScore) {
        0, 1 -> "Muito Fraca" to GrowthRed
        2 -> "Fraca" to GrowthOrange
        3 -> "Média" to GrowthYellow
        4 -> "Forte" to GrowthCyan
        else -> "Excelente & Segura" to GrowthGreen
    }

    // Handle back button when inside onboarding steps
    if (onboardingStep > 0) {
        BackHandler {
            onboardingStep -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
            .safeDrawingPadding()
    ) {
        if (onboardingStep == 0) {
            Image(
                painter = painterResource(id = R.drawable.login_gym_bg),
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
                                CarbonDark.copy(alpha = 0.80f),
                                CarbonDark.copy(alpha = 0.95f)
                            )
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Branding
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                    if (onboardingStep > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onboardingStep -= 1 }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            Text(
                                text = "Etapa $onboardingStep de 3",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrangeLight
                            )
                        }
                    }

                    Surface(
                        color = GrowthOrange,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Victus Dieta & Treino",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = when (onboardingStep) {
                            0 -> if (isRegisterMode) "Cadastro de Nova Conta" else ""
                            1 -> "Passo 1/3: Informações Biométricas"
                            2 -> "Passo 2/3: Guia e Escolha de Biotipo"
                            else -> "Passo 3/3: Rotina e Metas Nutricionais"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                }

            // Error banner if any auth error occurs
            if (authError != null && onboardingStep == 0) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GrowthRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GrowthRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GrowthRed, modifier = Modifier.size(20.dp))
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
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = GrowthRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            if (onboardingStep == 0) {
                // Step 0: Firebase Auth Login / Registration
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isRegisterMode) "CRIAR CONTA" else "ENTRAR",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthOrangeLight,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            val context = LocalContext.current
                            val coroutineScope = rememberCoroutineScope()
                            val credentialManager = remember { CredentialManager.create(context) }
                            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

                            // Biometric Sign In Option (Fingerprint / Face Unlock)
                            if (!isRegisterMode) {
                                val fragmentActivity = context as? FragmentActivity
                                val hasConfiguredEmail = userProfile.email.isNotBlank() &&
                                    userProfile.email.contains("@") &&
                                    userProfile.email.contains(".") &&
                                    !userProfile.email.endsWith("@growth.local") &&
                                    !userProfile.firebaseUid.startsWith("guest_")
                                val hasConfiguredProfile = userProfile.hasCompletedOnboarding && userProfile.weightKg > 30f && userProfile.name.isNotBlank() && userProfile.name != "Atleta"
                                val isBiometricsReady = hasConfiguredEmail && hasConfiguredProfile

                                Button(
                                    onClick = {
                                        if (!isBiometricsReady) {
                                            viewModel.showToast("Nenhum perfil configurado com e-mail válido. Cadastre-se ou entre com seu e-mail primeiro.")
                                            return@Button
                                        }
                                        if (fragmentActivity != null) {
                                            BiometricAuthHelper.promptBiometricAuth(
                                                activity = fragmentActivity,
                                                title = "Login com Biometria",
                                                subtitle = "Perfil: ${userProfile.email}",
                                                description = "Confirme sua digital ou Face para entrar no seu plano.",
                                                onSuccess = {
                                                    viewModel.loginWithBiometrics(onSuccess = onAuthSuccess)
                                                },
                                                onError = { errorMsg ->
                                                    viewModel.showToast(errorMsg)
                                                }
                                            )
                                        } else {
                                            viewModel.showToast("Sensor biométrico não suportado neste ambiente.")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("biometric_login_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBiometricsReady) GrowthOrange else CarbonCardElevated,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Fingerprint,
                                            contentDescription = "Biometria",
                                            tint = if (isBiometricsReady) Color.White else GrowthOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (isBiometricsReady) "Entrar com Biometria (${userProfile.email})" else "Entrar com Biometria",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                            }

                            OutlinedButton(
                                onClick = {
                                    val clientId = try {
                                        context.getString(R.string.default_web_client_id)
                                    } catch (_: Exception) { "" }
                                    if (clientId.isNotBlank()) {
                                        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                                        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
                                        coroutineScope.launch {
                                            try {
                                                val result = credentialManager.getCredential(context as Activity, request)
                                                val cred = result.credential
                                                if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                                    val googleIdToken = GoogleIdTokenCredential.createFrom(cred.data).idToken
                                                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                                                    viewModel.loginWithGoogleCredential(authCredential, onAuthSuccess)
                                                }
                                            } catch (_: GetCredentialCancellationException) {
                                                // user cancelled
                                            } catch (e: Exception) {
                                                val detail = e.message?.take(120)?.takeIf { it.isNotBlank() }
                                                viewModel.showToast(
                                                    if (detail != null) "Falha no Google Sign-In: $detail"
                                                    else "Google Sign-In indisponível. Verifique a configuração Firebase."
                                                )
                                            }
                                        }
                                    } else {
                                        viewModel.showToast("Google Sign-In não configurado. Utilize seu e-mail/senha ou o Acesso Rápido abaixo.")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("google_signin_btn"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = CarbonCardElevated,
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = GrowthCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Entrar com Conta Google",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(modifier = Modifier.weight(1f), color = CarbonBorder)
                                Text(
                                    text = " OU E-MAIL ",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                HorizontalDivider(modifier = Modifier.weight(1f), color = CarbonBorder)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (isRegisterMode) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Nome Completo") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GrowthOrange) },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_name_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GrowthOrange,
                                        unfocusedBorderColor = CarbonBorder
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            OutlinedTextField(
                                value = email,
                                onValueChange = { 
                                    email = it
                                    if (authError != null) viewModel.clearAuthError()
                                },
                                label = { Text("E-mail do Atleta") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrowthOrange,
                                    unfocusedBorderColor = CarbonBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = password,
                                onValueChange = { 
                                    password = it
                                    if (authError != null) viewModel.clearAuthError()
                                },
                                label = { Text(if (isRegisterMode) "Senha Forte" else "Senha") },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GrowthOrange,
                                    unfocusedBorderColor = CarbonBorder
                                )
                            )

                            // Registration Mode: Confirm Password & Password Strength Checklist
                            if (isRegisterMode) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Confirmar Senha") },
                                    visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                            Icon(
                                                imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().testTag("auth_confirm_password_input"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (confirmPassword.isNotEmpty() && !passwordsMatch) GrowthRed else GrowthOrange,
                                        unfocusedBorderColor = CarbonBorder
                                    )
                                )

                                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "As senhas não coincidem.",
                                        fontSize = 11.sp,
                                        color = GrowthRed
                                    )
                                }

                                // Password Strength Meter Bar
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Força da Senha:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(strengthLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = strengthColor)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (strengthScore / 5f).coerceIn(0.05f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = strengthColor,
                                    trackColor = CarbonCardElevated
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Itemized requirements checklist
                                Surface(
                                    color = CarbonCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Requisitos de Segurança da Senha:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        PasswordRuleItem(isValid = hasMinLength, label = "Mínimo 8 caracteres")
                                        PasswordRuleItem(isValid = hasUpper, label = "Pelo menos 1 letra maiúscula (A-Z)")
                                        PasswordRuleItem(isValid = hasLower, label = "Pelo menos 1 letra minúscula (a-z)")
                                        PasswordRuleItem(isValid = hasDigit, label = "Pelo menos 1 número (0-9)")
                                        PasswordRuleItem(isValid = hasSpecial, label = "Pelo menos 1 caractere especial (!@#$...)")
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // LGPD Consent Checkbox Box
                                Surface(
                                    color = CarbonCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (lgpdAccepted) GrowthGreen else CarbonBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
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
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Shield, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Termos de Privacidade & LGPD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                            }
                                            Text(
                                                text = "Concordo com a coleta e tratamento de dados corporais, nutricionais e treinos conforme a Lei 13.709/18.",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        TextButton(
                                            onClick = { showLgpdDialog = true },
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("Ler termos", fontSize = 10.sp, color = GrowthCyan)
                                        }
                                    }
                                }
                            } else {
                                // Login Mode: Forgot password link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { 
                                        forgotEmailInput = email
                                        showForgotPasswordDialog = true 
                                    }) {
                                        Text("Esqueceu a senha?", fontSize = 11.sp, color = GrowthCyan)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Submit Button
                            val isValidEmail = email.contains("@") && email.contains(".") && email.length > 5
                            val isSubmitEnabled = if (isRegisterMode) {
                                name.isNotBlank() && email.isNotBlank() && isValidEmail && strengthScore >= 4 && passwordsMatch && lgpdAccepted && !isAuthLoading
                            } else {
                                email.isNotBlank() && password.isNotBlank() && isValidEmail && !isAuthLoading
                            }

                            Button(
                                onClick = {
                                    if (isRegisterMode) {
                                        onboardingStep = 1 // Proceed to biometric onboarding
                                    } else {
                                        viewModel.loginWithFirebase(
                                            emailInput = email,
                                            passwordInput = password,
                                            onSuccess = onAuthSuccess
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GrowthOrange,
                                    disabledContainerColor = GrowthOrange.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                enabled = isSubmitEnabled,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("auth_submit_btn")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = if (isRegisterMode) "AVANÇAR PARA DADOS CORPORAIS (1/3)" else "ENTRAR NO APP",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            TextButton(
                                onClick = { 
                                    isRegisterMode = !isRegisterMode
                                    viewModel.clearAuthError()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (isRegisterMode) "Já tem conta? Entrar com e-mail" else "Não tem conta? Cadastre-se com segurança LGPD",
                                    color = GrowthCyan,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                            }

                            if (!isRegisterMode) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = { viewModel.enterAsGuest(onSuccess = onAuthSuccess) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .testTag("guest_login_btn"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = Color.LightGray
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                                ) {
                                    Text("ACESSO RÁPIDO (MODO LOCAL OFFLINE)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
            } else if (onboardingStep == 1) {
                // Step 1: Body Metrics (Altura, Peso, Idade, Gênero)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "PASSO 1 DE 3: DADOS BIOMÉTRICOS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrangeLight,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Informações para Cálculo de TMB & Macros",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Gender selection
                            Text("Gênero Biológico:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    modifier = Modifier.weight(1f).testTag("onboard_weight_input"),
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
                                    modifier = Modifier.weight(1f).testTag("onboard_height_input"),
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("PRÓXIMO: ESCOLHER BIOTIPO", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
            } else if (onboardingStep == 2) {
                // Step 2: Biotype Guide & Selection (Ectomorfo, Mesomorfo, Endomorfo)
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
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
                                text = "Guia Visual de Biotipos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Infographic Image Banner
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CarbonCardElevated,
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.body_types_guide_1791310055094),
                                    contentDescription = "Guia Ilustrado de Biotipos",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            BodyType.values().forEach { biotype ->
                                val isSelected = selectedBiotype == biotype
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) GrowthOrange.copy(alpha = 0.15f) else CarbonCardElevated
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(
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
                                                Icon(Icons.Default.Check, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
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
                                                text = "💡 Dieta: ${biotype.dietStrategy}",
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
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("PRÓXIMO: ROTINA DE TREINOS", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
            } else {
                // Step 3: Training frequency & Goal & Firebase Creation
                Card(
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
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
                                text = "Sua Rotina de Treinos",
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
                            Text("Objetivo Nutricional Principal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))

                            FitnessGoal.values().forEach { fg ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { goal = fg }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (goal == fg) GrowthGreen else CarbonCardElevated,
                                        shape = CircleShape,
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            if (goal == fg) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
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

                            if (authError != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = authError ?: "",
                                    fontSize = 12.sp,
                                    color = GrowthRed
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val w = weightText.toFloatOrNull() ?: 78f
                                    val h = heightText.toFloatOrNull() ?: 178f
                                    val a = ageText.toIntOrNull() ?: 25

                                    viewModel.registerWithFirebase(
                                        name = name.ifBlank { "Atleta" },
                                        email = email.ifBlank { "atleta@fitmail.com" },
                                        password = password,
                                        gender = gender,
                                        age = a,
                                        weightKg = w,
                                        heightCm = h,
                                        biotype = selectedBiotype,
                                        trainingDays = trainingDays,
                                        activityLevel = if (trainingDays >= 5) ActivityLevel.VERY_ACTIVE else ActivityLevel.MODERATE,
                                        goal = goal,
                                        isAthlete = isAthlete,
                                        onSuccess = onAuthSuccess
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isAuthLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("finish_onboarding_btn")
                            ) {
                                if (isAuthLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                                } else {
                                    Text("CRIAR CONTA NO FIREBASE & ATIVAR PROTOCOLO", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
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
                        Text("Termos de Privacidade & LGPD", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Em estrita conformidade com a Lei Geral de Proteção de Dados (Lei nº 13.709/2018 - LGPD):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1. Finalidade: Os dados informados (nome, e-mail, idade, peso, altura, gênero, frequência de treino e compras de alimentos) são utilizados exclusivamente para o cálculo científico de taxas metabólicas, macronutrientes, periodização de treinos e listas de compras.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "2. Segurança & Armazenamento: A autenticação é provida via Firebase Auth com padrões de criptografia de ponta a ponta. Seus dados corporais ficam persistidos em banco de dados seguro no seu dispositivo.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "3. Direitos do Titular: Você possui o direito de exportar seus dados em formato PDF e revogar o consentimento ou excluir sua conta e registros a qualquer momento através do aplicativo.",
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
                        Text("Aceitar & Prosseguir", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLgpdDialog = false }) {
                        Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }

        // Forgot Password Dialog
        if (showForgotPasswordDialog) {
            AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                containerColor = CarbonCard,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recuperar Senha", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Digite o e-mail cadastrado. O Firebase enviará um link de redefinição.\n\n" +
                                    "Atenção:\n" +
                                    "• O e-mail só é enviado se a conta estiver cadastrada no Firebase.\n" +
                                    "• Verifique sua caixa de Spam ou Lixo Eletrônico caso não encontre na Caixa de Entrada.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = forgotEmailInput,
                            onValueChange = { forgotEmailInput = it },
                            label = { Text("Seu E-mail") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthCyan,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (forgotEmailInput.isNotBlank()) {
                                viewModel.sendPasswordResetEmail(forgotEmailInput) { success, msg ->
                                    if (success) {
                                        showForgotPasswordDialog = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan),
                        enabled = forgotEmailInput.isNotBlank() && !isAuthLoading
                    ) {
                        if (isAuthLoading) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Enviar Link de Redefinição", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showForgotPasswordDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
private fun PasswordRuleItem(isValid: Boolean, label: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = if (isValid) GrowthGreen else GrowthRed.copy(alpha = 0.6f),
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isValid) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
