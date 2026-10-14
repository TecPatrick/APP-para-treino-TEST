package com.example.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.CardioLog
import com.example.data.model.CardioType
import com.example.ui.VirtusViewModel

@Composable
fun CardioLogScreen(
    viewModel: VirtusViewModel,
    onNavigateBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf(CardioType.TREADMILL) }
    var duration by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Registrar Cardio", style = MaterialTheme.typography.headlineMedium) }
        
        item {
            CardioType.entries.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedType == type,
                        onClick = { selectedType = type }
                    )
                    Text(type.title)
                }
            }
        }
        
        item {
            OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duração (min)") })
        }
        item {
            OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calorias") })
        }
        item {
            OutlinedTextField(value = distance, onValueChange = { distance = it }, label = { Text("Distância (km - opcional)") })
        }
        item {
            OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notas") })
        }
        item {
            Button(onClick = {
                val log = CardioLog(
                    type = selectedType,
                    durationMinutes = duration.toIntOrNull() ?: 0,
                    caloriesBurned = calories.toIntOrNull() ?: 0,
                    distanceKm = distance.toFloatOrNull(),
                    notes = notes
                )
                viewModel.addCardioLog(log)
                onNavigateBack()
            }) {
                Text("Salvar")
            }
        }
    }
}
