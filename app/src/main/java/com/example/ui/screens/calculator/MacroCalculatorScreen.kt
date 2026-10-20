package com.example.ui.screens.calculator

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ActivityLevel
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
import com.example.ui.theme.GrowthYellow
import com.example.util.MacroCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroCalculatorScreen(
    viewModel: VirtusViewModel,
    onNavigateBack: () -> Unit
) {
    val currentProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(currentProfile.name) }
    var gender by remember { mutableStateOf(currentProfile.gender) }
    var ageText by remember { mutableStateOf(currentProfile.age.toString()) }
    var weightText by remember { mutableStateOf(currentProfile.weightKg.toString()) }
    var heightText by remember { mutableStateOf(currentProfile.heightCm.toInt().toString()) }
    var activityLevel by remember { mutableStateOf(currentProfile.activityLevel) }
    var goal by remember { mutableStateOf(currentProfile.goal) }

    var proteinGPerKg by remember {
        mutableFloatStateOf((currentProfile.targetProteinGrams / currentProfile.weightKg).coerceIn(1.6f, 3.0f))
    }
    var fatGPerKg by remember {
        mutableFloatStateOf((currentProfile.targetFatGrams / currentProfile.weightKg).coerceIn(0.6f, 1.5f))
    }

    val currentWeight = weightText.toFloatOrNull() ?: 78.5f
    val currentHeight = heightText.toFloatOrNull() ?: 178f
    val currentAge = ageText.toIntOrNull() ?: 26

    // Live Calculation
    val calculatedPlan = remember(currentWeight, currentHeight, currentAge, gender, activityLevel, goal, proteinGPerKg, fatGPerKg) {
        MacroCalculator.computeFullPlan(
            weightKg = currentWeight,
            heightCm = currentHeight,
            age = currentAge,
            gender = gender,
            activityLevel = activityLevel,
            goal = goal,
            customProteinGPerKg = proteinGPerKg,
            customFatGPerKg = fatGPerKg
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(CarbonDark),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            GrowthHeader(
                title = "Calculadora de Macros",
                subtitle = "Algoritmo científico Mifflin-St Jeor & Harris-Benedict",
                badgeText = "SISTEMA CIENTÍFICO DE MACROS"
            )
        }

        // Live Calculated Targets Result Card (Top Sticky Preview)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "METAS ESTIMADAS (DIÁRIAS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${calculatedPlan.targetCalories} kcal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MacroResultBadge(
                            title = "Proteína",
                            grams = calculatedPlan.proteinGrams,
                            gPerKg = String.format("%.1f", proteinGPerKg),
                            color = GrowthCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MacroResultBadge(
                            title = "Carbos",
                            grams = calculatedPlan.carbsGrams,
                            gPerKg = String.format("%.1f", calculatedPlan.carbsGPerKg),
                            color = GrowthYellow,
                            modifier = Modifier.weight(1f)
                        )
                        MacroResultBadge(
                            title = "Gorduras",
                            grams = calculatedPlan.fatGrams,
                            gPerKg = String.format("%.1f", fatGPerKg),
                            color = GrowthOrange,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TMB (Parado sem treino): ${calculatedPlan.bmr} kcal", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                        Text("Água: ${calculatedPlan.waterMl} ml/dia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ℹ️ Regra do App: Em dias sem treino, seu gasto permanece no valor basal do TMB (${calculatedPlan.bmr} kcal). O gasto calórico adicional só é somado após realizar o treino no dia.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Biological & Body Data Inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Dados Corporais do Atleta",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nome do Atleta") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gender selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = gender == Gender.MASCULINO,
                            onClick = { gender = Gender.MASCULINO },
                            label = { Text("Masculino", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrowthOrange,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = gender == Gender.FEMININO,
                            onClick = { gender = Gender.FEMININO },
                            label = { Text("Feminino", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GrowthOrange,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Peso (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("calc_weight_input"),
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
                            modifier = Modifier.weight(1f).testTag("calc_height_input"),
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
                }
            }
        }

        // Activity Level & Goal
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Nível de Atividade & Objetivo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Frequência de Atividade Física:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

                    ActivityLevel.values().forEach { level ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activityLevel = level }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (activityLevel == level) GrowthOrange else CarbonCardElevated,
                                shape = CircleShape,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (activityLevel == level) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = level.label,
                                fontSize = 12.sp,
                                color = if (activityLevel == level) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Objetivo Nutricional Principal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))

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
                }
            }
        }

        // Custom Macro Sliders (Protein g/kg & Fat g/kg)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Personalização de g/kg de Macros",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Protein Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Proteína por kg de peso corporal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%.1f", proteinGPerKg)} g/kg (${calculatedPlan.proteinGrams}g)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                    }
                    Slider(
                        value = proteinGPerKg,
                        onValueChange = { proteinGPerKg = it },
                        valueRange = 1.6f..3.0f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = GrowthCyan,
                            activeTrackColor = GrowthCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fat Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Gordura por kg de peso corporal:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%.1f", fatGPerKg)} g/kg (${calculatedPlan.fatGrams}g)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                    }
                    Slider(
                        value = fatGPerKg,
                        onValueChange = { fatGPerKg = it },
                        valueRange = 0.6f..1.5f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = GrowthOrange,
                            activeTrackColor = GrowthOrange
                        )
                    )
                }
            }
        }

        // Apply and Save Button
        item {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        viewModel.updateProfileAndCalculateMacros(
                            name = name,
                            gender = gender,
                            age = currentAge,
                            weightKg = currentWeight,
                            heightCm = currentHeight,
                            activityLevel = activityLevel,
                            goal = goal,
                            proteinGPerKg = proteinGPerKg,
                            fatGPerKg = fatGPerKg,
                            monthlyTargetWeightKg = currentProfile.monthlyTargetWeightKg,
                            monthlyNotes = currentProfile.monthlyNotes,
                            dietCycleType = currentProfile.dietCycleType
                        )
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("apply_calculated_macros_btn")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "APLICAR E SALVAR METAS NO APLICATIVO",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun MacroResultBadge(
    title: String,
    grams: Int,
    gPerKg: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CarbonCardElevated,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${grams}g",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = "${gPerKg} g/kg",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
