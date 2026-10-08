package com.example.ui.screens.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.util.ExerciseGifMapper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutRoutineEditorDialog(
    existingRoutine: WorkoutRoutine?,
    existingExercises: List<WorkoutExercise>,
    initialSplit: WorkoutSplitType = WorkoutSplitType.CUSTOM,
    onDismiss: () -> Unit,
    onSave: (
        existingId: Long?,
        splitType: WorkoutSplitType,
        letter: String,
        title: String,
        dayOfWeek: String,
        muscles: String,
        durationMin: Int,
        exercises: List<WorkoutExercise>
    ) -> Unit
) {
    var splitType by remember { mutableStateOf(existingRoutine?.splitType ?: initialSplit) }
    var letter by remember { mutableStateOf(existingRoutine?.letter ?: "A") }
    var title by remember { mutableStateOf(existingRoutine?.title ?: "Treino ABC - Peitoral & Tríceps") }
    var dayOfWeek by remember { mutableStateOf(existingRoutine?.dayOfWeekLabel ?: "Segunda-feira") }
    var muscles by remember { mutableStateOf(existingRoutine?.targetMuscleGroups ?: "Peitoral, Deltoide, Tríceps") }
    var durationText by remember { mutableStateOf(existingRoutine?.estimatedDurationMinutes?.toString() ?: "60") }

    val exerciseList = remember {
        mutableStateListOf<WorkoutExercise>().apply {
            addAll(existingExercises)
        }
    }

    var showExerciseLibraryPicker by remember { mutableStateOf(false) }
    var showCustomExerciseAddDialog by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(CarbonDark),
            color = CarbonDark
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Header
                Surface(
                    color = CarbonCard,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (existingRoutine != null) "Editar Ficha de Treino" else "Criar Ficha de Treino",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val dur = durationText.toIntOrNull() ?: 60
                                    onSave(
                                        existingRoutine?.id,
                                        splitType,
                                        letter,
                                        title,
                                        dayOfWeek,
                                        muscles,
                                        dur,
                                        exerciseList.toList()
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("save_full_routine_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar Ficha", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 12.dp, bottom = 32.dp)
                ) {
                    // Section 1: Informações Gerais da Ficha
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("📋 Dados Gerais do Treino", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Divisão do Treino:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                                Spacer(modifier = Modifier.height(4.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(WorkoutSplitType.values()) { st ->
                                        FilterChip(
                                            selected = (splitType == st),
                                            onClick = { splitType = st },
                                            label = {
                                                Text(
                                                    text = when (st) {
                                                        WorkoutSplitType.AB -> "Modelo AB"
                                                        WorkoutSplitType.ABC -> "Modelo ABC"
                                                        WorkoutSplitType.ABCDE -> "Modelo ABCDE"
                                                        WorkoutSplitType.CUSTOM -> "Livre / Customizado"
                                                    },
                                                    fontSize = 11.sp
                                                )
                                            },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = GrowthOrange,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = letter,
                                        onValueChange = { letter = it.take(2) },
                                        label = { Text("Sigla/Letra (A, B...)") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GrowthOrange,
                                            unfocusedBorderColor = CarbonBorder
                                        )
                                    )
                                    OutlinedTextField(
                                        value = durationText,
                                        onValueChange = { durationText = it },
                                        label = { Text("Duração (min)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GrowthOrange,
                                            unfocusedBorderColor = CarbonBorder
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Nome da Rotina / Ficha (Ex: Treino ABC)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GrowthOrange,
                                        unfocusedBorderColor = CarbonBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = dayOfWeek,
                                        onValueChange = { dayOfWeek = it },
                                        label = { Text("Dia da Semana") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = GrowthOrange,
                                            unfocusedBorderColor = CarbonBorder
                                        )
                                    )
                                    OutlinedTextField(
                                        value = muscles,
                                        onValueChange = { muscles = it },
                                        label = { Text("Músculos Alvo") },
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

                    // Section 2: Seleção e Configuração dos Exercícios
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "🏋️ Exercícios da Ficha (${exerciseList.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showExerciseLibraryPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Biblioteca", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                OutlinedButton(
                                    onClick = { showCustomExerciseAddDialog = true },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GrowthCyan)
                                ) {
                                    Text("＋ Custom", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (exerciseList.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nenhum exercício adicionado a esta ficha.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { showExerciseLibraryPicker = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                                    ) {
                                        Text("Selecionar da Biblioteca de Exercícios", color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        itemsIndexed(exerciseList) { index, exercise ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCard),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = GrowthOrange,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = exercise.exerciseName,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                                Text(
                                                    text = exercise.muscleGroup,
                                                    fontSize = 11.sp,
                                                    color = GrowthOrangeLight
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { exerciseList.removeAt(index) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remover", tint = GrowthRed, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = exercise.sets.toString(),
                                            onValueChange = { text ->
                                                val s = text.toIntOrNull() ?: 1
                                                exerciseList[index] = exercise.copy(sets = s)
                                            },
                                            label = { Text("Séries") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GrowthOrange,
                                                unfocusedBorderColor = CarbonBorder
                                            )
                                        )

                                        OutlinedTextField(
                                            value = exercise.repsMin.toString(),
                                            onValueChange = { text ->
                                                val rMin = text.toIntOrNull() ?: 1
                                                exerciseList[index] = exercise.copy(repsMin = rMin)
                                            },
                                            label = { Text("Reps Mín") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GrowthOrange,
                                                unfocusedBorderColor = CarbonBorder
                                            )
                                        )

                                        OutlinedTextField(
                                            value = exercise.repsMax.toString(),
                                            onValueChange = { text ->
                                                val rMax = text.toIntOrNull() ?: 1
                                                exerciseList[index] = exercise.copy(repsMax = rMax)
                                            },
                                            label = { Text("Reps Máx") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier.weight(1f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GrowthOrange,
                                                unfocusedBorderColor = CarbonBorder
                                            )
                                        )

                                        OutlinedTextField(
                                            value = exercise.currentWeightKg.toInt().toString(),
                                            onValueChange = { text ->
                                                val w = text.toFloatOrNull() ?: 0f
                                                exerciseList[index] = exercise.copy(currentWeightKg = w)
                                            },
                                            label = { Text("Carga (kg)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            modifier = Modifier.weight(1.2f),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = GrowthGreen,
                                                unfocusedBorderColor = CarbonBorder
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal de Seleção Múltipla da Biblioteca de Exercícios
    if (showExerciseLibraryPicker) {
        MultiExerciseLibraryPickerModal(
            onDismiss = { showExerciseLibraryPicker = false },
            onConfirmSelection = { selectedNames ->
                selectedNames.forEach { name ->
                    if (exerciseList.none { it.exerciseName.equals(name, ignoreCase = true) }) {
                        val guide = ExerciseGifMapper.getGuideForExercise(name)
                        exerciseList.add(
                            WorkoutExercise(
                                routineId = existingRoutine?.id ?: 0L,
                                exerciseName = guide.exerciseName,
                                muscleGroup = guide.muscleGroup,
                                sets = 4,
                                repsMin = 8,
                                repsMax = 12,
                                currentWeightKg = 30f,
                                restSeconds = 90,
                                biomechanicsGuide = guide.postureTip,
                                gifDemonstrationUrl = guide.gifUrl
                            )
                        )
                    }
                }
                showExerciseLibraryPicker = false
            }
        )
    }

    // Modal de Exercício Customizado Individual
    if (showCustomExerciseAddDialog) {
        AddExerciseDialog(
            routine = existingRoutine ?: WorkoutRoutine(splitType = splitType, letter = letter, title = title),
            onDismiss = { showCustomExerciseAddDialog = false },
            onConfirm = { name, group, sets, rMin, rMax, weight, rest, notes ->
                val guide = ExerciseGifMapper.getGuideForExercise(name)
                exerciseList.add(
                    WorkoutExercise(
                        routineId = existingRoutine?.id ?: 0L,
                        exerciseName = name,
                        muscleGroup = group,
                        sets = sets,
                        repsMin = rMin,
                        repsMax = rMax,
                        currentWeightKg = weight,
                        restSeconds = rest,
                        notes = notes,
                        biomechanicsGuide = guide.postureTip,
                        gifDemonstrationUrl = guide.gifUrl
                    )
                )
                showCustomExerciseAddDialog = false
            }
        )
    }
}

// Modal de Seleção Múltipla da Biblioteca de Exercícios
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiExerciseLibraryPickerModal(
    onDismiss: () -> Unit,
    onConfirmSelection: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val catalog = remember {
        listOf(
            "Supino Reto com Barra",
            "Supino Inclinado com Halteres",
            "Crucifixo no Cross / Crossover",
            "Desenvolvimento com Halteres",
            "Puxada Alta Aberta no Pulley",
            "Remada Curvada com Barra",
            "Crucifixo Inverso no Voador",
            "Agachamento Livre com Barra",
            "Leg Press 45°",
            "Cadeira Extensora",
            "Desenvolvimento Militar com Halteres",
            "Elevação Lateral com Halteres",
            "Rosca Direta com Barra W",
            "Tríceps Testa com Halteres",
            "Tríceps Pulley Corda",
            "Stiff com Barra / RDL",
            "Panturrilha no Smith em Pé"
        )
    }

    val selectedItems = remember { mutableStateListOf<String>() }

    val filteredCatalog = catalog.filter {
        it.contains(searchQuery, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Text("Biblioteca de Exercícios", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text("Selecione um ou mais exercícios para adicionar à ficha:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar exercício...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCatalog) { exerciseName ->
                        val isChecked = selectedItems.contains(exerciseName)
                        val guide = remember(exerciseName) { ExerciseGifMapper.getGuideForExercise(exerciseName) }

                        Surface(
                            color = if (isChecked) GrowthOrange.copy(alpha = 0.15f) else CarbonCardElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedItems.remove(exerciseName) else selectedItems.add(exerciseName)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked == true) selectedItems.add(exerciseName) else selectedItems.remove(exerciseName)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GrowthOrange,
                                        uncheckedColor = CarbonBorder
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(exerciseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                    Text(guide.muscleGroup, fontSize = 10.sp, color = GrowthOrangeLight)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmSelection(selectedItems.toList()) },
                enabled = selectedItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
            ) {
                Text("Adicionar (${selectedItems.size}) Exercício(s)", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
