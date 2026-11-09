package com.example.ui.screens.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingBodyMetricsStepCard(
    gender: Gender,
    onGenderChange: (Gender) -> Unit,
    weightText: String,
    onWeightChange: (String) -> Unit,
    heightText: String,
    onHeightChange: (String) -> Unit,
    ageText: String,
    onAgeChange: (String) -> Unit,
    onNextStep: () -> Unit
) {
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
                    onClick = { onGenderChange(Gender.MASCULINO) },
                    label = { Text("Masculino", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrowthOrange,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = gender == Gender.FEMININO,
                    onClick = { onGenderChange(Gender.FEMININO) },
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
                    onValueChange = onWeightChange,
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
                    onValueChange = onHeightChange,
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
                    onValueChange = onAgeChange,
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
                onClick = onNextStep,
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
}

@Composable
fun OnboardingBiotypeStepCard(
    selectedBiotype: BodyType,
    onBiotypeSelected: (BodyType) -> Unit,
    onNextStep: () -> Unit
) {
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
                        .clickable { onBiotypeSelected(biotype) }
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
                onClick = onNextStep,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFrequencyAndGoalStepCard(
    trainingDays: Int,
    onTrainingDaysChange: (Int) -> Unit,
    goal: FitnessGoal,
    onGoalChange: (FitnessGoal) -> Unit,
    isAuthLoading: Boolean,
    onFinishOnboarding: () -> Unit
) {
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
                        onClick = { onTrainingDaysChange(days) },
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
                        .clickable { onGoalChange(fg) }
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
                onClick = onFinishOnboarding,
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

@Composable
fun LgpdTermsDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
            ) {
                Text("Aceitar & Continuar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
