package com.example.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.fragment.app.FragmentActivity
import com.example.util.BiometricAuthHelper
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
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
import com.example.util.MacroCalculator
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun UserProfileScreen(
    viewModel: VirtusViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    // Form inputs state
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

    // Sync state when profile is first loaded or changes
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

    // Derived numeric values for live diagnostics
    val parsedWeight = weightInputText.replace(",", ".").toFloatOrNull() ?: profile.weightKg
    val parsedHeight = heightInputText.replace(",", ".").toFloatOrNull() ?: profile.heightCm
    val parsedAge = ageInputText.toIntOrNull() ?: profile.age

    // Live calculated health metrics
    val heightMeters = (parsedHeight / 100f).coerceAtLeast(0.5f)
    val liveBmi = parsedWeight / (heightMeters * heightMeters)
    val idealMinWeight = 18.5f * heightMeters * heightMeters
    val idealMaxWeight = 24.9f * heightMeters * heightMeters
    val liveBmr = MacroCalculator.calculateBMR(parsedWeight, parsedHeight, parsedAge, selectedGender)
    val liveTdee = MacroCalculator.calculateTDEE(liveBmr, selectedActivityLevel)
    val liveWaterGoal = MacroCalculator.calculateWaterIntakeMl(parsedWeight, parsedAge, true)

    val (bmiLabel, bmiColor) = when {
        liveBmi < 18.5f -> "Abaixo do peso" to GrowthYellow
        liveBmi < 25.0f -> "Peso normal / Saudável" to GrowthGreen
        liveBmi < 30.0f -> "Sobrepeso" to GrowthOrange
        else -> "Obesidade" to GrowthRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        GrowthHeader(
            title = "Perfil & Biometria",
            subtitle = "Medidas corporais e persistência Room",
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
            // Hero: Current Biometric Overview
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biometrics_overview_card"),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = GrowthOrange.copy(alpha = 0.15f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AccessibilityNew,
                                            contentDescription = null,
                                            tint = GrowthOrange,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = nameInput.ifBlank { "Atleta" },
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${selectedGender.name.lowercase().replaceFirstChar { it.uppercase() }} • $parsedAge anos",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Storage Indicator
                            Surface(
                                color = GrowthGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(GrowthGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Room Local",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrowthGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4 Key metrics grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BiometricKpiBox(
                                modifier = Modifier.weight(1f),
                                label = "Peso Atual",
                                value = "${String.format(Locale.US, "%.1f", parsedWeight)} kg",
                                icon = Icons.Default.MonitorWeight,
                                tint = GrowthOrange
                            )
                            BiometricKpiBox(
                                modifier = Modifier.weight(1f),
                                label = "Altura",
                                value = "${parsedHeight.toInt()} cm",
                                icon = Icons.Default.Height,
                                tint = GrowthCyan
                            )
                            BiometricKpiBox(
                                modifier = Modifier.weight(1f),
                                label = "Idade",
                                value = "$parsedAge anos",
                                icon = Icons.Default.Person,
                                tint = GrowthYellow
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Diagnostics Banner (IMC, TMB, GET)
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = CarbonCardElevated,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Diagnóstico Corporal:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "IMC: ${String.format(Locale.US, "%.1f", liveBmi)} ($bmiLabel)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = bmiColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• TMB (Parado sem treino): $liveBmr kcal",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GrowthOrangeLight
                                    )
                                    Text(
                                        text = "• Treino: soma gasto dinamicamente",
                                        fontSize = 10.sp,
                                        color = GrowthCyan
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "• Faixa de peso normal: ${idealMinWeight.toInt()} a ${idealMaxWeight.toInt()} kg",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "• Água sugerida: $liveWaterGoal ml",
                                        fontSize = 10.sp,
                                        color = GrowthCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: Edit Biometrics Form
            item {
                Text(
                    text = "Editar Medidas & Biometria",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight
                )
            }

            // 1. Name Field
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Nome de Exibição",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            placeholder = { Text("Seu nome completo ou apelido") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_name_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = GrowthOrange)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                }
            }

            // 2. Gender Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Sexo Biológico (Fator da Fórmula Mifflin-St Jeor)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Gender.entries.forEach { g ->
                                val isSelected = selectedGender == g
                                val borderColor by animateColorAsState(
                                    targetValue = if (isSelected) GrowthOrange else CarbonBorder,
                                    label = "genderBorder"
                                )
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clickable { selectedGender = g }
                                        .testTag("gender_${g.name}"),
                                    color = if (isSelected) GrowthOrange.copy(alpha = 0.15f) else CarbonCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GrowthOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = g.name.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Weight Input with Quick Adjustments
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Peso Corporal (kg)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.1f", parsedWeight)} kg",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = weightInputText,
                            onValueChange = { weightInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_weight_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = GrowthOrange)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Steppers
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(-1.0f, -0.5f, 0.5f, 1.0f).forEach { delta ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clickable {
                                            val newW = (parsedWeight + delta).coerceIn(30f, 250f)
                                            weightInputText = String.format(Locale.US, "%.1f", newW)
                                        },
                                    color = CarbonCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CarbonBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (delta > 0) "+${delta}kg" else "${delta}kg",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Height Input with Slider
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Altura (cm)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${parsedHeight.toInt()} cm",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthCyan
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = heightInputText,
                            onValueChange = { heightInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_height_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Height, contentDescription = null, tint = GrowthCyan)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthCyan,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = parsedHeight.coerceIn(120f, 220f),
                            onValueChange = { heightInputText = it.roundToInt().toString() },
                            valueRange = 120f..220f,
                            colors = SliderDefaults.colors(
                                thumbColor = GrowthCyan,
                                activeTrackColor = GrowthCyan,
                                inactiveTrackColor = CarbonBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. Age Input with Steppers
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Idade (anos)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$parsedAge anos",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthYellow
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ageInputText,
                            onValueChange = { ageInputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("profile_age_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = GrowthYellow)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthYellow,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(-5, -1, 1, 5).forEach { delta ->
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clickable {
                                            val newAge = (parsedAge + delta).coerceIn(12, 100)
                                            ageInputText = newAge.toString()
                                        },
                                    color = CarbonCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, CarbonBorder)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = if (delta > 0) "+$delta" else "$delta",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Activity Level Selector (Critical Biometric Factor)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_activity_level_card"),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nível de Atividade Física",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Fator: x${selectedActivityLevel.multiplier}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GrowthOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ActivityLevel.entries.forEach { level ->
                                val isSelected = selectedActivityLevel == level
                                val borderColor by animateColorAsState(
                                    targetValue = if (isSelected) GrowthOrange else CarbonBorder,
                                    label = "activityBorder"
                                )

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedActivityLevel = level }
                                        .testTag("activity_level_${level.name}"),
                                    color = if (isSelected) GrowthOrange.copy(alpha = 0.12f) else CarbonCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DirectionsRun,
                                                contentDescription = null,
                                                tint = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = level.label.substringBefore(" ("),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = level.label.substringAfter(" (").removeSuffix(")"),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selecionado",
                                                tint = GrowthOrange,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Goal Selector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Objetivo Nutricional & Treino",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FitnessGoal.entries.forEach { g ->
                                val isSelected = selectedGoal == g
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedGoal = g }
                                        .testTag("goal_${g.name}"),
                                    color = if (isSelected) GrowthOrange.copy(alpha = 0.12f) else CarbonCardElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, if (isSelected) GrowthOrange else CarbonBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = g.label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onBackground
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = GrowthOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Auto-recalculate macros toggle
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Recalcular Metas Diárias",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Atualiza automaticamente calorias, proteínas, carboidratos e água com base nas novas medidas.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Switch(
                            checked = recomputeMacrosAuto,
                            onCheckedChange = { recomputeMacrosAuto = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GrowthOrange,
                                uncheckedTrackColor = CarbonBorder
                            ),
                            modifier = Modifier.testTag("recalculate_macros_switch")
                        )
                    }
                }
            }

            // 8b. Biometric Security Info & Test
            item {
                val context = LocalContext.current
                val fragmentActivity = context as? FragmentActivity
                val biometricStatus = remember(context) { BiometricAuthHelper.checkBiometricSupport(context) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("biometric_security_card"),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = GrowthCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Login com Biometria",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            Surface(
                                color = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE)
                                    GrowthGreen.copy(alpha = 0.15f)
                                else
                                    GrowthOrange.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE)
                                        GrowthGreen.copy(alpha = 0.4f)
                                    else
                                        GrowthOrange.copy(alpha = 0.4f)
                                )
                            ) {
                                Text(
                                    text = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE)
                                        "Ativo no Aparelho"
                                    else
                                        "Compatível",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (biometricStatus == BiometricAuthHelper.BiometricAvailability.AVAILABLE)
                                        GrowthGreen
                                    else
                                        GrowthOrangeLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Acesse seu treino e diário com rapidez usando o leitor ultrassônico de digital ou reconhecimento facial do seu Galaxy/smartphone Android.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                if (fragmentActivity != null) {
                                    BiometricAuthHelper.promptBiometricAuth(
                                        activity = fragmentActivity,
                                        title = "Teste de Biometria",
                                        subtitle = "Sensor biométrico verificado com sucesso",
                                        description = "Toque no sensor biométrico do aparelho.",
                                        onSuccess = {
                                            viewModel.showToast("Sensor biométrico reconhecido e validado com sucesso!")
                                        },
                                        onError = { error ->
                                            viewModel.showToast(error)
                                        }
                                    )
                                } else {
                                    viewModel.showToast("Biometria não disponível nesta sessão")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("test_biometric_btn"),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CarbonCardElevated,
                                contentColor = GrowthCyan
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Testar Sensor Biométrico (Digital / Face)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 9. Primary Save Button
            item {
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

            // 9.1 Firestore Cloud & Offline Persistence Sync Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firestore_sync_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonCard),
                    border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Sincronização na nuvem",
                                color = GrowthCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Seus dados são salvos no aparelho. Com uma conta conectada, os envios pendentes são retomados pelo Firebase ao reconectar. Confira abaixo a confirmação da nuvem.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = { viewModel.syncAllDataToFirestore() },
                            enabled = !viewModel.isSyncing.collectAsStateWithLifecycle().value,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("sync_firestore_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sincronizar perfil e históricos",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 10. Secondary Restore Button
            item {
                OutlinedButton(
                    onClick = {
                        nameInput = profile.name
                        weightInputText = profile.weightKg.toString()
                        heightInputText = profile.heightCm.toInt().toString()
                        ageInputText = profile.age.toString()
                        selectedGender = profile.gender
                        selectedActivityLevel = profile.activityLevel
                        selectedGoal = profile.goal
                        selectedBiotype = profile.biotype
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("restore_biometrics_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CarbonBorder),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Restaurar Medidas Originais",
                        fontSize = 12.sp
                    )
                }
            }

            // 11. Account Management & Database Wipe Actions
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("logout_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onBackground)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Sair da Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showResetDatabaseDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("reset_database_users_btn"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, GrowthRed.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthRed)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = GrowthRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Zerar Banco", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthRed)
                    }
                }
            }
        }

        if (showResetDatabaseDialog) {
            AlertDialog(
                onDismissRequest = { showResetDatabaseDialog = false },
                containerColor = CarbonCard,
                title = {
                    Text(
                        text = "Excluir Usuários & Zerar Banco?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Text(
                        text = "Isso excluirá todos os perfis e credenciais de usuários da base de dados, permitindo iniciar o aplicativo 100% zerado para novos cadastros.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAllUsersAndResetDatabase()
                            showResetDatabaseDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthRed),
                        modifier = Modifier.testTag("confirm_wipe_users_btn")
                    ) {
                        Text("Sim, Excluir Tudo", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDatabaseDialog = false }) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            )
        }
    }
}

@Composable
private fun BiometricKpiBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color
) {
    Surface(
        modifier = modifier.height(72.dp),
        color = CarbonCardElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

