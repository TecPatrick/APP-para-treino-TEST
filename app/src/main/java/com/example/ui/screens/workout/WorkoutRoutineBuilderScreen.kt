package com.example.ui.screens.workout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthRed
import com.example.util.ExerciseGifMapper

private val builderWeekdays = listOf("Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo")

/**
 * Montador de treino em tela inteira. O fluxo é inspirado na clareza do editor semanal
 * experimentado no FitVibe, mas usa o modelo, a biblioteca e o visual do Virtus.
 */
@Composable
fun WorkoutRoutineBuilderScreen(
    existingRoutine: WorkoutRoutine?,
    existingExercises: List<WorkoutExercise>,
    initialSplit: WorkoutSplitType,
    onBack: () -> Unit,
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
    var splitType by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.splitType ?: initialSplit) }
    var letter by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.letter ?: "A") }
    var title by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.title.orEmpty()) }
    var dayOfWeek by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.dayOfWeekLabel ?: builderWeekdays.first()) }
    var muscles by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.targetMuscleGroups.orEmpty()) }
    var durationText by remember(existingRoutine?.id) { mutableStateOf(existingRoutine?.estimatedDurationMinutes?.toString() ?: "60") }
    val exercises = remember(existingRoutine?.id, existingExercises) {
        mutableStateListOf<WorkoutExercise>().apply { addAll(existingExercises.sortedBy { it.orderIndex }) }
    }
    var showLibrary by remember { mutableStateOf(false) }
    var showCustomExercise by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val duration = durationText.toIntOrNull()
    val canSave = title.isNotBlank() && duration != null && duration > 0 && exercises.isNotEmpty()

    if (showLibrary) {
        WorkoutExercisePickerScreen(
            alreadyAdded = exercises.map { it.exerciseName }.toSet(),
            onBack = { showLibrary = false },
            onConfirm = { selectedNames ->
                selectedNames.forEach { name ->
                    if (exercises.none { it.exerciseName.equals(name, ignoreCase = true) }) {
                        val guide = ExerciseGifMapper.getGuideForExercise(name)
                        exercises.add(
                            WorkoutExercise(
                                routineId = existingRoutine?.id ?: 0L,
                                exerciseName = guide.exerciseName,
                                muscleGroup = guide.muscleGroup,
                                biomechanicsGuide = guide.postureTip,
                                gifDemonstrationUrl = guide.gifUrl,
                                orderIndex = exercises.size
                            )
                        )
                    }
                }
                showLibrary = false
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        Surface(color = CarbonCard, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (existingRoutine == null) "Montar treino" else "Editar treino",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Organize a ficha e adicione exercícios da sua biblioteca",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                BuilderSection(title = "1. Dados do treino", subtitle = "Nome, dia e divisão") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it.take(50) },
                        label = { Text("Nome do treino") },
                        placeholder = { Text("Ex.: Peito e tríceps") },
                        singleLine = true,
                        isError = showErrors && title.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = builderFieldColors()
                    )

                    Text("Dia da semana", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(builderWeekdays) { day ->
                            FilterChip(
                                selected = dayOfWeek == day,
                                onClick = { dayOfWeek = day },
                                label = { Text(day.take(3)) },
                                colors = builderChipColors()
                            )
                        }
                    }

                    Text("Divisão", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(WorkoutSplitType.values().toList()) { split ->
                            FilterChip(
                                selected = splitType == split,
                                onClick = { splitType = split },
                                label = { Text(splitLabel(split)) },
                                colors = builderChipColors()
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = letter,
                            onValueChange = { letter = it.uppercase().take(2) },
                            label = { Text("Ficha") },
                            placeholder = { Text("A") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = builderFieldColors()
                        )
                        OutlinedTextField(
                            value = durationText,
                            onValueChange = { durationText = it.filter(Char::isDigit).take(3) },
                            label = { Text("Duração (min)") },
                            singleLine = true,
                            isError = showErrors && (duration == null || duration <= 0),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = builderFieldColors()
                        )
                    }
                    OutlinedTextField(
                        value = muscles,
                        onValueChange = { muscles = it.take(80) },
                        label = { Text("Grupos musculares") },
                        placeholder = { Text("Ex.: Peitoral, deltoide, tríceps") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = builderFieldColors()
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("2. Exercícios", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${exercises.size} ${if (exercises.size == 1) "exercício" else "exercícios"} na ficha",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    OutlinedButton(onClick = { showCustomExercise = true }) {
                        Text("Manual", fontSize = 12.sp)
                    }
                    Spacer(Modifier.size(8.dp))
                    Button(
                        onClick = { showLibrary = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("Biblioteca")
                    }
                }
            }

            if (exercises.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        border = BorderStroke(1.dp, if (showErrors) GrowthRed else CarbonBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(38.dp))
                            Spacer(Modifier.height(10.dp))
                            Text("A ficha ainda está vazia", fontWeight = FontWeight.Bold)
                            Text("Adicione exercícios da biblioteca do Virtus", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(exercises, key = { index, exercise -> "${exercise.id}-${exercise.exerciseName}-$index" }) { index, exercise ->
                    BuilderExerciseCard(
                        position = index + 1,
                        exercise = exercise,
                        canMoveUp = index > 0,
                        canMoveDown = index < exercises.lastIndex,
                        onChange = { exercises[index] = it },
                        onMoveUp = {
                            val moved = exercises.removeAt(index)
                            exercises.add(index - 1, moved)
                        },
                        onMoveDown = {
                            val moved = exercises.removeAt(index)
                            exercises.add(index + 1, moved)
                        },
                        onDelete = { exercises.removeAt(index) }
                    )
                }
            }
        }

        Surface(color = CarbonCard, shadowElevation = 8.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                if (showErrors && !canSave) {
                    Text(
                        "Preencha o nome, a duração e adicione ao menos um exercício.",
                        color = GrowthRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = {
                        if (!canSave) {
                            showErrors = true
                        } else {
                            onSave(
                                existingRoutine?.id,
                                splitType,
                                letter.ifBlank { "A" },
                                title.trim(),
                                dayOfWeek,
                                muscles.trim(),
                                duration!!,
                                exercises.mapIndexed { index, exercise -> exercise.copy(orderIndex = index) }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Salvar ficha", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

    if (showCustomExercise) {
        AddExerciseDialog(
            routine = existingRoutine ?: WorkoutRoutine(splitType = splitType, letter = letter.ifBlank { "A" }, title = title.ifBlank { "Novo treino" }),
            onDismiss = { showCustomExercise = false },
            onConfirm = { name, group, sets, repsMin, repsMax, weight, rest, notes ->
                val guide = ExerciseGifMapper.getGuideForExercise(name)
                exercises.add(
                    WorkoutExercise(
                        routineId = existingRoutine?.id ?: 0L,
                        exerciseName = name,
                        muscleGroup = group,
                        sets = sets,
                        repsMin = repsMin,
                        repsMax = repsMax,
                        currentWeightKg = weight,
                        restSeconds = rest,
                        orderIndex = exercises.size,
                        notes = notes,
                        biomechanicsGuide = guide.postureTip,
                        gifDemonstrationUrl = guide.gifUrl
                    )
                )
                showCustomExercise = false
            }
        )
    }
}

@Composable
private fun WorkoutExercisePickerScreen(
    alreadyAdded: Set<String>,
    onBack: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf("Todos") }
    val selected = remember { mutableStateListOf<String>() }
    val catalog = remember { ExerciseGifMapper.getAllAvailableExercises() }
    val groups = remember(catalog) {
        listOf("Todos") + catalog.map { it.muscleGroup }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val visibleExercises = catalog.filter { guide ->
        val matchesQuery = query.isBlank() ||
            guide.exerciseName.contains(query, ignoreCase = true) ||
            guide.muscleGroup.contains(query, ignoreCase = true)
        val matchesGroup = selectedGroup == "Todos" || guide.muscleGroup == selectedGroup
        val isNew = alreadyAdded.none { it.equals(guide.exerciseName, ignoreCase = true) }
        matchesQuery && matchesGroup && isNew
    }

    Column(modifier = Modifier.fillMaxSize().background(CarbonDark)) {
        Surface(color = CarbonCard, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Biblioteca de exercícios", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Selecione um ou mais exercícios", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                placeholder = { Text("Buscar exercício ou músculo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = builderFieldColors()
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { selectedGroup = group },
                        label = { Text(group, maxLines = 1) },
                        colors = builderChipColors()
                    )
                }
            }
        }

        if (visibleExercises.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(42.dp))
                Spacer(Modifier.height(10.dp))
                Text("Nenhum exercício encontrado", fontWeight = FontWeight.Bold)
                Text("Tente outro nome ou filtro", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(visibleExercises, key = { index, guide -> "${guide.exerciseName}-${guide.muscleGroup}-$index" }) { _, guide ->
                    val isSelected = selected.contains(guide.exerciseName)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GrowthOrange.copy(alpha = 0.16f) else CarbonCard
                        ),
                        border = BorderStroke(1.dp, if (isSelected) GrowthOrange else CarbonBorder),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (isSelected) selected.remove(guide.exerciseName) else selected.add(guide.exerciseName)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(42.dp).background(CarbonCardElevated, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange)
                            }
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(guide.exerciseName, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(guide.muscleGroup, color = GrowthOrangeLight, fontSize = 12.sp)
                            }
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selected.add(guide.exerciseName) else selected.remove(guide.exerciseName)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = GrowthOrange)
                            )
                        }
                    }
                }
            }
        }

        Surface(color = CarbonCard, shadowElevation = 8.dp) {
            Button(
                onClick = { onConfirm(selected.toList()) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(12.dp).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (selected.isEmpty()) "Selecione os exercícios" else "Adicionar ${selected.size} à ficha",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BuilderSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, CarbonBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun BuilderExerciseCard(
    position: Int,
    exercise: WorkoutExercise,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onChange: (WorkoutExercise) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    var setsText by remember(exercise.id, exercise.exerciseName, position) { mutableStateOf(exercise.sets.toString()) }
    var repsMinText by remember(exercise.id, exercise.exerciseName, position) { mutableStateOf(exercise.repsMin.toString()) }
    var repsMaxText by remember(exercise.id, exercise.exerciseName, position) { mutableStateOf(exercise.repsMax.toString()) }
    var weightText by remember(exercise.id, exercise.exerciseName, position) {
        mutableStateOf(exercise.currentWeightKg.toString().removeSuffix(".0"))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        border = BorderStroke(1.dp, CarbonBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(GrowthOrange, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(position.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(exercise.exerciseName, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(exercise.muscleGroup, color = GrowthOrangeLight, fontSize = 12.sp)
                }
                IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Mover para cima")
                }
                IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Mover para baixo")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover exercício", tint = GrowthRed)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BuilderNumberField("Séries", setsText, Modifier.weight(1f)) { value ->
                    setsText = value.filter(Char::isDigit).take(2)
                    value.toIntOrNull()?.takeIf { it > 0 }?.let { onChange(exercise.copy(sets = it)) }
                }
                BuilderNumberField("Reps mín.", repsMinText, Modifier.weight(1f)) { value ->
                    repsMinText = value.filter(Char::isDigit).take(3)
                    value.toIntOrNull()?.takeIf { it > 0 }?.let { onChange(exercise.copy(repsMin = it, repsMax = maxOf(it, exercise.repsMax))) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BuilderNumberField("Reps máx.", repsMaxText, Modifier.weight(1f)) { value ->
                    repsMaxText = value.filter(Char::isDigit).take(3)
                    value.toIntOrNull()?.takeIf { it >= exercise.repsMin }?.let { onChange(exercise.copy(repsMax = it)) }
                }
                BuilderNumberField("Carga (kg)", weightText, Modifier.weight(1f), decimal = true) { value ->
                    weightText = value.filter { it.isDigit() || it == ',' || it == '.' }.take(6)
                    value.replace(',', '.').toFloatOrNull()?.takeIf { it >= 0f }?.let { onChange(exercise.copy(currentWeightKg = it)) }
                }
            }
        }
    }
}

@Composable
private fun BuilderNumberField(
    label: String,
    value: String,
    modifier: Modifier,
    decimal: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number),
        modifier = modifier,
        colors = builderFieldColors()
    )
}

@Composable
private fun builderFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GrowthOrange,
    unfocusedBorderColor = CarbonBorder,
    cursorColor = GrowthOrange
)

@Composable
private fun builderChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = GrowthOrange,
    selectedLabelColor = Color.White,
    containerColor = CarbonCardElevated,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

private fun splitLabel(split: WorkoutSplitType) = when (split) {
    WorkoutSplitType.AB -> "AB"
    WorkoutSplitType.ABC -> "ABC"
    WorkoutSplitType.ABCDE -> "ABCDE"
    WorkoutSplitType.CUSTOM -> "Livre"
}
