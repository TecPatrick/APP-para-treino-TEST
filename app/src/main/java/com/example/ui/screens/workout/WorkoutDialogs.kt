package com.example.ui.screens.workout

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSplitType
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
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseMuscleClassifier
import com.example.util.ExerciseVisualGuide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseBiomechanicsGuideModal(
    guide: ExerciseVisualGuide,
    gifImageLoader: ImageLoader,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(guide.exerciseName, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CarbonDark)
                ) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(guide.gifUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = gifImageLoader,
                        contentDescription = guide.exerciseName,
                        contentScale = ContentScale.Fit,
                        loading = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(32.dp))
                            }
                        },
                        error = {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(guide.muscleGroup, fontSize = 12.sp, color = GrowthOrangeLight)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("💡 Execução & Postura Correta:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthCyan)
                Spacer(modifier = Modifier.height(4.dp))
                Text(guide.postureTip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(guide.fitnessProgrammerUrl))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Guia Completo na Web", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

// Finish Workout Modal with RPE Rating
@Composable
fun FinishWorkoutModal(
    activeWorkout: com.example.ui.ActiveWorkoutSession,
    onDismiss: () -> Unit,
    onConfirmFinish: (rpe: Int, notes: String) -> Unit
) {
    var rpeValue by remember { mutableFloatStateOf(7f) }
    var notesInput by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("🏆 Concluir Treino", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Duração: ${activeWorkout.elapsedSeconds / 60} minutos • ${activeWorkout.completedSetsCount} Séries Concluídas",
                    fontSize = 12.sp,
                    color = GrowthGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("RPE — Percepção de Esforço (1 a 10): ${rpeValue.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrange)
                Slider(
                    value = rpeValue,
                    onValueChange = { rpeValue = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = GrowthOrange,
                        activeTrackColor = GrowthOrange,
                        inactiveTrackColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Observações do Treino") },
                    placeholder = { Text("Ex: Aumentei carga no Supino, recuperei bem") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmFinish(rpeValue.toInt(), notesInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                    ) {
                        Text("Salvar Treino", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Workout History Log Viewer Modal
@Composable
fun WorkoutHistoryModal(
    workoutLogs: List<WorkoutLog>,
    onDismiss: () -> Unit,
    onDeleteLog: (Long) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CarbonCard),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, CarbonBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Histórico de Treinos", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (workoutLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nenhum treino registrado no histórico ainda.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(workoutLogs) { log ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).format(Date(log.dateMillis))
                                        Text(log.routineName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text("$dateStr • ${log.durationMinutes} min • RPE ${log.perceivedExertion}", fontSize = 11.sp, color = GrowthGreen)
                                        if (log.notes.isNotBlank()) {
                                            Text(log.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteLog(log.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Excluir Log", tint = GrowthRed, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Modal for Adding Exercise with GIF Library Catalog Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    routine: WorkoutRoutine? = null,
    onDismiss: () -> Unit,
    onConfirm: ((name: String, group: String, sets: Int, rMin: Int, rMax: Int, weight: Float, rest: Int, notes: String) -> Unit)? = null,
    onAdd: ((name: String, group: String, sets: Int, reps: Int, weight: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = Biblioteca com GIFs, 1 = Personalizado

    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleFilter by remember { mutableStateOf("Todos") }

    var customName by remember { mutableStateOf("") }
    var customGroup by remember { mutableStateOf("Peito") }
    var setsText by remember { mutableStateOf("4") }
    var repsText by remember { mutableStateOf("10") }
    var weightText by remember { mutableStateOf("30") }
    var restText by remember { mutableStateOf("90") }

    val gifImageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val availableGuides = remember { ExerciseGifMapper.getAllAvailableExercises() }
    val favoriteGuides = remember { ExerciseGifMapper.getFavoriteAndTopExercises() }
    val muscleFilters = listOf("⭐ Populares", "Todos", "Tríceps", "Bíceps", "Peitoral", "Dorsal", "Deltoide", "Quadríceps", "Posterior / Glúteos", "Panturrilha", "Antebraço", "Trapézio", "Abdômen")

    val filteredCatalog = (if (selectedMuscleFilter == "⭐ Populares") favoriteGuides else availableGuides).filter { guide ->
        val matchesSearch = guide.exerciseName.contains(searchQuery, ignoreCase = true) ||
                guide.muscleGroup.contains(searchQuery, ignoreCase = true)
        val matchesFilter = ExerciseMuscleClassifier.matches(guide, selectedMuscleFilter)
        matchesSearch && matchesFilter
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Adicionar Exercício", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tab Selector (Biblioteca com GIFs vs Manual)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = if (selectedTab == 0) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("🎥 Biblioteca com GIFs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        color = if (selectedTab == 1) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("✏️ Outro / Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        text = {
            if (selectedTab == 0) {
                // GIF CATALOG TAB
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar exercício ou músculo...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange, unfocusedBorderColor = CarbonBorder)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                        items(muscleFilters) { muscle ->
                            val isSel = selectedMuscleFilter == muscle
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedMuscleFilter = muscle },
                                label = { Text(muscle, fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GrowthOrange,
                                    selectedLabelColor = Color.White,
                                    containerColor = CarbonCardElevated,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredCatalog) { guide ->
                            Surface(
                                color = CarbonCardElevated,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CarbonBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val s = setsText.toIntOrNull() ?: 4
                                        val r = repsText.toIntOrNull() ?: 10
                                        val w = weightText.toFloatOrNull() ?: 30f
                                        val rest = restText.toIntOrNull() ?: 90

                                        onConfirm?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip)
                                        onAdd?.invoke(guide.exerciseName, guide.muscleGroup, s, r, w)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // GIF Thumbnail
                                    Box(
                                        modifier = Modifier
                                            .size(width = 64.dp, height = 52.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CarbonDark)
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(guide.gifUrl)
                                                .crossfade(true)
                                                .build(),
                                            imageLoader = gifImageLoader,
                                            contentDescription = guide.exerciseName,
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(18.dp))
                                                }
                                            },
                                            error = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(guide.exerciseName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text(guide.muscleGroup, fontSize = 10.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                                        Text(guide.postureTip, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }

                                    Button(
                                        onClick = {
                                            val s = setsText.toIntOrNull() ?: 4
                                            val r = repsText.toIntOrNull() ?: 10
                                            val w = weightText.toFloatOrNull() ?: 30f
                                            val rest = restText.toIntOrNull() ?: 90

                                            onConfirm?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip)
                                            onAdd?.invoke(guide.exerciseName, guide.muscleGroup, s, r, w)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Add", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // MANUAL INPUT TAB
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nome do Exercício") },
                        placeholder = { Text("Ex: Supino Reto com Barra") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                    )

                    OutlinedTextField(
                        value = customGroup,
                        onValueChange = { customGroup = it },
                        label = { Text("Grupo Muscular") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = setsText,
                            onValueChange = { setsText = it },
                            label = { Text("Séries") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                        OutlinedTextField(
                            value = repsText,
                            onValueChange = { repsText = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Carga (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GrowthOrange)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1) {
                Button(
                    onClick = {
                        if (customName.isNotBlank()) {
                            val s = setsText.toIntOrNull() ?: 4
                            val r = repsText.toIntOrNull() ?: 10
                            val w = weightText.toFloatOrNull() ?: 20f
                            val rest = restText.toIntOrNull() ?: 90

                            onConfirm?.invoke(customName, customGroup, s, r, r, w, rest, "")
                            onAdd?.invoke(customName, customGroup, s, r, w)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Text("Adicionar Customizado", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

// Dialog for fast 1-tap exercise substitution during workout
@Composable
fun SubstituteExerciseDialog(
    currentExercise: WorkoutExercise,
    gifImageLoader: ImageLoader,
    onDismiss: () -> Unit,
    onSelectSubstitute: (ExerciseVisualGuide) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val allGuides = remember { ExerciseGifMapper.getAllAvailableExercises() }
    
    // Auto-detect target muscle group based on current exercise
    val matchingMuscleGuides = remember(currentExercise) {
        val currentGuide = ExerciseGifMapper.getGuideForExercise(currentExercise.exerciseName)
        val primaryFilter = ExerciseMuscleClassifier.primaryFilter(currentGuide)
        allGuides.filter { guide ->
            ExerciseMuscleClassifier.matches(guide, primaryFilter)
        }.ifEmpty { allGuides }
    }

    val filteredList = matchingMuscleGuides.filter {
        it.exerciseName != currentExercise.exerciseName &&
        (it.exerciseName.contains(searchQuery, ignoreCase = true) || it.muscleGroup.contains(searchQuery, ignoreCase = true))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Substituir Exercício", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aparelho ocupado? Escolha uma variação de mesmo músculo para trocar na ficha:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Banner showing current exercise
                Surface(
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Substituindo:", fontSize = 10.sp, color = GrowthOrangeLight, fontWeight = FontWeight.Bold)
                            Text(currentExercise.exerciseName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text("${currentExercise.muscleGroup} • Mantém ${currentExercise.sets} séries", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar outra variação...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrowthOrange) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredList) { guide ->
                        Surface(
                            color = CarbonCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSubstitute(guide) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 60.dp, height = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(CarbonDark)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(guide.gifUrl)
                                            .crossfade(true)
                                            .build(),
                                        imageLoader = gifImageLoader,
                                        contentDescription = guide.exerciseName,
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        error = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange.copy(alpha = 0.5f))
                                            }
                                        },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = guide.exerciseName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = guide.muscleGroup,
                                        fontSize = 10.sp,
                                        color = GrowthOrangeLight
                                    )
                                }

                                Button(
                                    onClick = { onSelectSubstitute(guide) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Trocar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

