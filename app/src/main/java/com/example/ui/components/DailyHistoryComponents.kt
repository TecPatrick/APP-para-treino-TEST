package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthYellow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyHistorySummary(
    val caloriesConsumed: Int,
    val targetCalories: Int,
    val protein: Int,
    val proteinTarget: Int,
    val carbs: Int,
    val carbsTarget: Int,
    val fat: Int,
    val fatTarget: Int,
    val waterMl: Int,
    val waterTargetMl: Int,
    val workouts: Int,
    val cardioSessions: Int,
    val exerciseCalories: Int,
    val weightKg: Float?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyHistoryHeader(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit,
    daysWithData: Set<String>,
    summary: DailyHistorySummary
) {
    var showPicker by remember { mutableStateOf(false) }
    val selectedCalendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    }
    val weekStart = remember(selectedDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
    }
    val keyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val titleFormatter = remember { SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, CarbonBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("DIÁRIO", color = GrowthOrange, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text(
                        titleFormatter.format(Date(selectedDateMillis)).replaceFirstChar { it.uppercase() },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    onClick = { showPicker = true },
                    color = CarbonDark,
                    border = BorderStroke(1.dp, GrowthOrange.copy(alpha = .45f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(modifier = Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Abrir calendário", tint = GrowthOrange)
                        Spacer(Modifier.width(5.dp))
                        Text("Calendário", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(7) { offset ->
                    val day = (weekStart.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, offset) }
                    val millis = day.timeInMillis
                    val selected = keyFormatter.format(Date(millis)) == keyFormatter.format(Date(selectedDateMillis))
                    val hasData = keyFormatter.format(Date(millis)) in daysWithData
                    Surface(
                        color = if (selected) GrowthOrange else CarbonDark,
                        border = BorderStroke(1.dp, if (selected) GrowthOrange else CarbonBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).clickable { onDateSelected(millis) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 7.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                SimpleDateFormat("EEE", Locale("pt", "BR")).format(day.time).take(3).uppercase(),
                                fontSize = 8.sp,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                day.get(Calendar.DAY_OF_MONTH).toString(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                            Text(if (hasData) "●" else " ", color = GrowthGreen, fontSize = 8.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            val remaining = (summary.targetCalories - summary.caloriesConsumed).coerceAtLeast(0)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                DailyMetric("Consumidas", "${summary.caloriesConsumed}", "kcal", GrowthOrange, Modifier.weight(1f))
                DailyMetric("Restantes", "$remaining", "kcal", GrowthGreen, Modifier.weight(1f))
                DailyMetric("Atividade", "${summary.exerciseCalories}", "kcal", GrowthCyan, Modifier.weight(1f))
            }
            Spacer(Modifier.height(7.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                DailyMetric("Proteína", "${summary.protein}/${summary.proteinTarget}", "g", GrowthCyan, Modifier.weight(1f))
                DailyMetric("Carbo", "${summary.carbs}/${summary.carbsTarget}", "g", GrowthYellow, Modifier.weight(1f))
                DailyMetric("Gordura", "${summary.fat}/${summary.fatTarget}", "g", GrowthOrange, Modifier.weight(1f))
            }
            Spacer(Modifier.height(7.dp))
            Text(
                "Água ${summary.waterMl}/${summary.waterTargetMl} ml  •  ${summary.workouts} treino(s)  •  ${summary.cardioSessions} cardio/corrida",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            summary.weightKg?.let {
                Text("Peso registrado: ${"%.1f".format(it)} kg", fontSize = 10.sp, color = GrowthGreen)
            }
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utcMillis ->
                        val utc = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
                        val local = Calendar.getInstance().apply {
                            set(Calendar.YEAR, utc.get(Calendar.YEAR))
                            set(Calendar.MONTH, utc.get(Calendar.MONTH))
                            set(Calendar.DAY_OF_MONTH, utc.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, 12)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateSelected(local.timeInMillis)
                    }
                    showPicker = false
                }) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun DailyMetric(title: String, value: String, unit: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = CarbonDark, shape = RoundedCornerShape(9.dp), border = BorderStroke(1.dp, CarbonBorder)) {
        Column(modifier = Modifier.padding(7.dp)) {
            Text(title, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color, maxLines = 1)
            Text(unit, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
