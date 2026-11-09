package com.example.ui.screens.gps.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import java.util.Locale

@Composable
fun GpsPermissionBanner(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = GrowthOrange.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = GrowthOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Permissão de GPS Necessária",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = GrowthOrangeLight
                )
                Text(
                    text = "Ative a localização precisa para traçar o percurso da corrida, calcular o ritmo e a distância em tempo real.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("request_gps_permission_btn")
            ) {
                Text("Ativar", fontSize = 11.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun GpsSummaryRow(
    todayDistanceKm: Double,
    todayRunsCount: Int,
    todayDurationSec: Int,
    todayCalories: Double,
    totalMonthlyDistanceKm: Double,
    totalMonthlyRunsCount: Int,
    totalMonthlyCalories: Double,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("today_running_card"),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORRIDA HOJE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthOrangeLight,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = GrowthOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.2f km", todayDistanceKm),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (todayDistanceKm > 0.05) {
                        String.format(
                            Locale.getDefault(),
                            "%d treino(s) • %02d:%02d",
                            todayRunsCount,
                            todayDurationSec / 60,
                            todayDurationSec % 60
                        )
                    } else {
                        "Nenhum treino hoje"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.0f kcal queimadas", todayCalories),
                    fontSize = 10.sp,
                    color = GrowthOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .testTag("monthly_running_card"),
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, CarbonBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CORRIDA NO MÊS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrowthCyan,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = GrowthCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.1f km", totalMonthlyDistanceKm),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$totalMonthlyRunsCount treinos registrados",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%.0f kcal acumuladas", totalMonthlyCalories),
                    fontSize = 10.sp,
                    color = GrowthCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun GpsVoiceCoachCard(
    isTtsVoiceCoachEnabled: Boolean,
    ttsIntervalMeters: Float,
    targetPaceSecondsPerKm: Long,
    isPaceAlertEnabled: Boolean,
    lastSpokenMessage: String?,
    isSessionRunning: Boolean,
    onToggleTts: (Boolean) -> Unit,
    onSpeakMetricsNow: () -> Unit,
    onIntervalSelected: (Float) -> Unit,
    onTogglePaceAlert: (Boolean) -> Unit,
    onTargetPaceSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tts_voice_coach_card"),
        colors = CardDefaults.cardColors(containerColor = CarbonDark),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isTtsVoiceCoachEnabled) GrowthGreen.copy(alpha = 0.5f) else CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isTtsVoiceCoachEnabled) GrowthGreen.copy(alpha = 0.18f) else CarbonCard,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isTtsVoiceCoachEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = "Voz do Treinador",
                                tint = if (isTtsVoiceCoachEnabled) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TREINADOR POR VOZ (TTS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTtsVoiceCoachEnabled) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isTtsVoiceCoachEnabled) "Narra distância, tempo e ritmo (pace) ao vivo" else "Voz desativada",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Switch(
                    checked = isTtsVoiceCoachEnabled,
                    onCheckedChange = onToggleTts,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GrowthGreen,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = CarbonCard
                    ),
                    modifier = Modifier.testTag("tts_toggle_switch")
                )
            }

            if (isTtsVoiceCoachEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = onSpeakMetricsNow,
                        border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.fillMaxWidth().testTag("speak_metrics_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Ouvir Métricas",
                            tint = GrowthGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "OUVIR MÉTRICAS AGORA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthGreen
                        )
                    }

                    if (isSessionRunning) {
                        Text(
                            text = "Auto split: ${(ttsIntervalMeters / 1000f).let { if (it >= 1f) "${it.toInt()} km" else "${ttsIntervalMeters.toInt()}m" }}",
                            fontSize = 10.sp,
                            color = GrowthCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Avisar por voz a cada:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(500f to "500m", 1000f to "1 km", 2000f to "2 km").forEach { (dist, label) ->
                            Surface(
                                color = if (ttsIntervalMeters == dist) GrowthCyan else CarbonCardElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onIntervalSelected(dist) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (ttsIntervalMeters == dist) FontWeight.Black else FontWeight.Normal,
                                    color = if (ttsIntervalMeters == dist) Color.Black else Color.White,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 7.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Alerta de Ritmo Mínimo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Avisa por voz quando o ritmo cair abaixo da meta",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPaceAlertEnabled,
                        onCheckedChange = onTogglePaceAlert,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = GrowthGreen,
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            uncheckedTrackColor = CarbonCard
                        ),
                        modifier = Modifier.testTag("pace_alert_toggle_switch")
                    )
                }

                if (isPaceAlertEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Meta de ritmo (alerta se correr mais devagar):",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            300L to "5:00",
                            330L to "5:30",
                            360L to "6:00",
                            390L to "6:30",
                            420L to "7:00"
                        ).forEach { (paceSec, label) ->
                            val isSelected = targetPaceSecondsPerKm == paceSec
                            Surface(
                                color = if (isSelected) GrowthGreen else CarbonCardElevated,
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onTargetPaceSelected(paceSec) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                    color = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 6.dp),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (lastSpokenMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"$lastSpokenMessage\"",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun SaveGpsSessionDialog(
    finishDistanceKm: Float,
    finishDurationMin: Int,
    finishDurationSec: Int,
    finishPace: Float,
    routeNameInput: String,
    routeNotesInput: String,
    onRouteNameChange: (String) -> Unit,
    onRouteNotesChange: (String) -> Unit,
    onConfirmSave: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text(
                text = "Salvar Treino de Corrida",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column {
                Text(
                    text = "Excelente treino! Você correu ${String.format(Locale.getDefault(), "%.2f", finishDistanceKm)} km em ${String.format(Locale.getDefault(), "%02d:%02d", finishDurationMin, finishDurationSec)}${if (finishPace > 0) " com ritmo de ${String.format(Locale.getDefault(), "%.1f", finishPace)} min/km" else ""}.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = routeNameInput,
                    onValueChange = onRouteNameChange,
                    label = { Text("Nome da Corrida") },
                    modifier = Modifier.fillMaxWidth().testTag("route_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = routeNotesInput,
                    onValueChange = onRouteNotesChange,
                    label = { Text("Observações (opcional)") },
                    modifier = Modifier.fillMaxWidth().testTag("route_notes_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSave,
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_save_gps_route_btn")
            ) {
                Text("Salvar Corrida", color = Color.White)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscard) {
                    Text("Descartar", color = Color(0xFFEF4444))
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
