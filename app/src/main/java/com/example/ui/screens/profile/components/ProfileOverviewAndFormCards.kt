package com.example.ui.screens.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.ActivityLevel
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.data.model.UserProfile
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import kotlin.math.roundToInt

@Composable
fun ProfileAccountCard(
    profile: UserProfile,
    onNavigateToAccount: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("account_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(GrowthCyan.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = profile.name.ifBlank { "Atleta Virtus" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (profile.email.isNotBlank() && !profile.email.endsWith("@growth.local")) {
                            profile.email
                        } else {
                            "Conta Local • Toque para vincular Google"
                        },
                        fontSize = 11.sp,
                        color = GrowthCyan
                    )
                }
            }
            OutlinedButton(
                onClick = onNavigateToAccount,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GrowthCyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan),
                modifier = Modifier.testTag("manage_account_btn")
            ) {
                Text("Conta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BiometricsOverviewCard(
    profile: UserProfile,
    nameInput: String,
    selectedGender: Gender,
    parsedWeight: Float,
    parsedHeight: Float,
    parsedAge: Int,
    liveBmi: Float,
    bmiLabel: String,
    bmiColor: Color,
    liveBmr: Int,
    idealMinWeight: Float,
    idealMaxWeight: Float,
    liveWaterGoal: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("biometrics_overview_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(GrowthOrange.copy(alpha = 0.16f), Color.Transparent)
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(GrowthOrange.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GrowthOrange,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = nameInput.ifBlank { profile.name },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            val genderLabel = if (selectedGender == Gender.MASCULINO) "Masculino" else "Feminino"
                            Text(
                                text = "$genderLabel • $parsedAge anos • ${parsedHeight.toInt()} cm",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        color = bmiColor.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, bmiColor.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "IMC ${String.format("%.1f", liveBmi)}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = bmiColor
                            )
                            Text(
                                text = bmiLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = bmiColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BiometricMiniPill(
                        label = "TMB Basal",
                        value = "$liveBmr kcal",
                        color = GrowthOrangeLight,
                        modifier = Modifier.weight(1f)
                    )
                    BiometricMiniPill(
                        label = "Peso Ideal (IMC)",
                        value = "${idealMinWeight.toInt()}–${idealMaxWeight.toInt()} kg",
                        color = GrowthGreen,
                        modifier = Modifier.weight(1f)
                    )
                    BiometricMiniPill(
                        label = "Água / Dia",
                        value = "${String.format("%.1f", liveWaterGoal / 1000f)} L",
                        color = GrowthCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileMeasurementsFormCards(
    nameInput: String,
    onNameChange: (String) -> Unit,
    selectedGender: Gender,
    onGenderChange: (Gender) -> Unit,
    weightInputText: String,
    parsedWeight: Float,
    onWeightTextChange: (String) -> Unit,
    heightInputText: String,
    parsedHeight: Float,
    onHeightTextChange: (String) -> Unit,
    ageInputText: String,
    parsedAge: Int,
    onAgeTextChange: (String) -> Unit,
    selectedActivityLevel: ActivityLevel,
    onActivityLevelChange: (ActivityLevel) -> Unit,
    selectedGoal: FitnessGoal,
    onGoalChange: (FitnessGoal) -> Unit,
    recomputeMacrosAuto: Boolean,
    onRecomputeMacrosChange: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Name & Gender Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "IDENTIFICAÇÃO E SEXO BIOLÓGICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = onNameChange,
                    label = { Text("Nome do Atleta") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sexo Biológico (fórmula Mifflin-St Jeor):",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Gender.values().forEach { g ->
                        val isSelected = selectedGender == g
                        Surface(
                            color = if (isSelected) GrowthOrange.copy(alpha = 0.2f) else CarbonCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GrowthOrange else CarbonBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onGenderChange(g) }
                                .testTag("gender_${g.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (g == Gender.MASCULINO) "Masculino" else "Feminino",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        // Weight, Height & Age Interactive Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "MEDIDAS CORPORAIS (PESO, ALTURA E IDADE)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = weightInputText,
                        onValueChange = onWeightTextChange,
                        label = { Text("Peso (kg)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        leadingIcon = {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_weight_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    OutlinedTextField(
                        value = heightInputText,
                        onValueChange = onHeightTextChange,
                        label = { Text("Altura (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.Height, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(18.dp))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("profile_height_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthCyan,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    OutlinedTextField(
                        value = ageInputText,
                        onValueChange = onAgeTextChange,
                        label = { Text("Idade") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(0.8f)
                            .testTag("profile_age_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthGreen,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ajuste Rápido de Peso", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.1f", parsedWeight)} kg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                }
                Slider(
                    value = parsedWeight.coerceIn(40f, 160f),
                    onValueChange = {
                        val rounded = (it * 2).roundToInt() / 2f
                        onWeightTextChange(rounded.toString())
                    },
                    valueRange = 40f..160f,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthOrange,
                        activeTrackColor = GrowthOrange
                    )
                )

                // Height Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ajuste Rápido de Altura", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${parsedHeight.toInt()} cm", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                }
                Slider(
                    value = parsedHeight.coerceIn(140f, 220f),
                    onValueChange = {
                        onHeightTextChange(it.roundToInt().toString())
                    },
                    valueRange = 140f..220f,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthCyan,
                        activeTrackColor = GrowthCyan
                    )
                )

                // Age Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Ajuste Rápido de Idade", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$parsedAge anos", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                }
                Slider(
                    value = parsedAge.toFloat().coerceIn(14f, 85f),
                    onValueChange = {
                        onAgeTextChange(it.roundToInt().toString())
                    },
                    valueRange = 14f..85f,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthGreen,
                        activeTrackColor = GrowthGreen
                    )
                )
            }
        }

        // Activity Level Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_activity_level_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NÍVEL DE ATIVIDADE FÍSICA (FATOR TDEE)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthCyan,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActivityLevel.values().forEach { level ->
                        val isSelected = selectedActivityLevel == level
                        Surface(
                            color = if (isSelected) GrowthCyan.copy(alpha = 0.16f) else CarbonCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GrowthCyan else CarbonBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onActivityLevelChange(level) }
                                .testTag("activity_level_${level.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = level.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) GrowthCyan else MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Multiplicador metabólico: x${level.multiplier}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GrowthCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fitness Goal Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = GrowthOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OBJETIVO PRINCIPAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FitnessGoal.values().forEach { g ->
                        val isSelected = selectedGoal == g
                        Surface(
                            color = if (isSelected) GrowthOrange.copy(alpha = 0.16f) else CarbonCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GrowthOrange else CarbonBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGoalChange(g) }
                                .testTag("goal_${g.name}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = g.label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) GrowthOrange else MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Ajuste calórico: ${if (g.calorieAdjustment >= 0) "+" else ""}${g.calorieAdjustment} kcal",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = GrowthOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto recalculate macros switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonDark, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = GrowthGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recalcular metas de calorias e macros",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Atualiza automaticamente proteínas, carbos, gorduras e água ao salvar",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = recomputeMacrosAuto,
                        onCheckedChange = onRecomputeMacrosChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GrowthGreen
                        ),
                        modifier = Modifier.testTag("recalculate_macros_switch")
                    )
                }
            }
        }
    }
}

@Composable
private fun BiometricMiniPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CarbonCardElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}
