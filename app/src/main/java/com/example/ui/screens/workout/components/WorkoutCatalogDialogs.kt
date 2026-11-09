package com.example.ui.screens.workout

import android.os.Build
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutRoutine
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.util.ExerciseGifMapper
import com.example.util.ExerciseMuscleClassifier
import com.example.util.ExerciseVisualGuide

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    routine: WorkoutRoutine? = null,
    isWorkoutActive: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: ((name: String, group: String, sets: Int, rMin: Int, rMax: Int, weight: Float, rest: Int, notes: String) -> Unit)? = null,
    onConfirmWithScope: ((name: String, group: String, sets: Int, rMin: Int, rMax: Int, weight: Float, rest: Int, notes: String, savePermanently: Boolean) -> Unit)? = null,
    onAdd: ((name: String, group: String, sets: Int, reps: Int, weight: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    val isFreeWorkout = routine?.id == 99999L
    var selectedTab by remember { mutableStateOf(0) }
    var savePermanently by remember(isWorkoutActive, isFreeWorkout) {
        mutableStateOf(!isWorkoutActive && !isFreeWorkout)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleFilter by remember { mutableStateOf("Todos") }

    var customName by remember { mutableStateOf("") }
    var customGroup by remember { mutableStateOf("Peitoral") }
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
                    Text(
                        text = if (isWorkoutActive) "Incluir Exercício no Treino" else "Adicionar Exercício",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (isWorkoutActive && !isFreeWorkout) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Como deseja salvar este exercício?",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (!savePermanently) GrowthGreen.copy(alpha = 0.2f) else CarbonDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (!savePermanently) GrowthGreen else CarbonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { savePermanently = false }
                                .testTag("scope_today_only_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                                Text(
                                    text = "⚡ Só para hoje",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!savePermanently) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            color = if (savePermanently) GrowthOrange.copy(alpha = 0.2f) else CarbonDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (savePermanently) GrowthOrange else CarbonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { savePermanently = true }
                                .testTag("scope_permanent_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)) {
                                Text(
                                    text = "📌 Fixar na ficha",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (savePermanently) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else if (isFreeWorkout) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = GrowthGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚡ Treino Livre: o exercício entra direto na sua sessão de hoje.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GrowthGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        color = if (selectedTab == 0) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                            Text("🎥 Catálogo com GIFs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Surface(
                        color = if (selectedTab == 1) GrowthOrange else CarbonDark,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                            Text("✏️ Criar Manual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        text = {
            if (selectedTab == 0) {
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
                        items(items = muscleFilters, key = { it }, contentType = { "muscle_filter" }) { muscle ->
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
                            .height(250.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(items = filteredCatalog, key = { it.exerciseName }, contentType = { "exercise_guide" }) { guide ->
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

                                        onConfirmWithScope?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip, savePermanently)
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
                                    Box(
                                        modifier = Modifier
                                            .size(58.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF0D0F12))
                                            .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(guide.gifUrl)
                                                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                                                .crossfade(true)
                                                .build(),
                                            imageLoader = gifImageLoader,
                                            contentDescription = guide.exerciseName,
                                            contentScale = ContentScale.Fit,
                                            loading = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                                }
                                            },
                                            error = {
                                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(18.dp))
                                                        Text(
                                                            text = guide.muscleGroup.take(4).uppercase(),
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = GrowthCyan
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(3.dp)
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

                                            onConfirmWithScope?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip, savePermanently)
                                            onConfirm?.invoke(guide.exerciseName, guide.muscleGroup, s, r, r, w, rest, guide.postureTip)
                                            onAdd?.invoke(guide.exerciseName, guide.muscleGroup, s, r, w)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ Incluir", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
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

                            onConfirmWithScope?.invoke(customName, customGroup, s, r, r, w, rest, "", savePermanently)
                            onConfirm?.invoke(customName, customGroup, s, r, r, w, rest, "")
                            onAdd?.invoke(customName, customGroup, s, r, w)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange)
                ) {
                    Text("Incluir Exercício", color = Color.White, fontWeight = FontWeight.Bold)
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

@Composable
fun SubstituteExerciseDialog(
    currentExercise: WorkoutExercise,
    gifImageLoader: ImageLoader,
    isWorkoutActive: Boolean = false,
    isFreeWorkout: Boolean = false,
    onDismiss: () -> Unit,
    onSelectSubstitute: (ExerciseVisualGuide) -> Unit = {},
    onSelectSubstituteWithScope: ((ExerciseVisualGuide, Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var savePermanently by remember(isWorkoutActive, isFreeWorkout) {
        mutableStateOf(!isWorkoutActive && !isFreeWorkout)
    }
    val allGuides = remember { ExerciseGifMapper.getAllAvailableExercises() }

    val matchingMuscleGuides = remember(currentExercise) {
        val currentGuide = ExerciseGifMapper.getGuideForExercise(currentExercise.exerciseName)
        val primaryFilter = ExerciseMuscleClassifier.primaryFilter(currentGuide)
        allGuides.filter { guide ->
            ExerciseMuscleClassifier.matches(guide, primaryFilter)
        }.ifEmpty { allGuides }
    }

    val filteredList = remember(matchingMuscleGuides, currentExercise.exerciseName, searchQuery) {
        matchingMuscleGuides.filter {
            it.exerciseName != currentExercise.exerciseName &&
                (it.exerciseName.contains(searchQuery, ignoreCase = true) || it.muscleGroup.contains(searchQuery, ignoreCase = true))
        }
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
                        Text("Trocar Exercício", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Aparelho ocupado? Escolha uma variação do mesmo músculo:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isWorkoutActive && !isFreeWorkout) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = if (!savePermanently) GrowthGreen.copy(alpha = 0.2f) else CarbonDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (!savePermanently) GrowthGreen else CarbonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { savePermanently = false }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                                Text("⚡ Trocar só hoje", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (!savePermanently) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Surface(
                            color = if (savePermanently) GrowthOrange.copy(alpha = 0.2f) else CarbonDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, if (savePermanently) GrowthOrange else CarbonBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { savePermanently = true }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                                Text("📌 Trocar na ficha", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (savePermanently) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
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
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items = filteredList, key = { it.exerciseName }, contentType = { "substitute_guide" }) { guide ->
                        Surface(
                            color = CarbonCardElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectSubstituteWithScope?.invoke(guide, savePermanently)
                                    onSelectSubstitute(guide)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0D0F12))
                                        .border(1.dp, CarbonBorder, RoundedCornerShape(10.dp))
                                ) {
                                    SubcomposeAsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(guide.gifUrl)
                                            .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
                                            .crossfade(true)
                                            .build(),
                                        imageLoader = gifImageLoader,
                                        contentDescription = guide.exerciseName,
                                        contentScale = ContentScale.Fit,
                                        loading = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                CircularProgressIndicator(color = GrowthOrange, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                            }
                                        },
                                        error = {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                                                    Text(
                                                        text = guide.muscleGroup.take(4).uppercase(),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = GrowthCyan
                                                    )
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(3.dp)
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
                                    onClick = {
                                        onSelectSubstituteWithScope?.invoke(guide, savePermanently)
                                        onSelectSubstitute(guide)
                                    },
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
