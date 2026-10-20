package com.example.ui.screens.analytics

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.ProgressLog
import com.example.ui.VirtusViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.components.WeightProgressionChart
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: VirtusViewModel) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
    val workoutLogs by viewModel.workoutLogs.collectAsStateWithLifecycle()

    var showAddMeasurementDialog by remember { mutableStateOf(false) }

    val sortedLogs = progressLogs.sortedBy { it.dateMillis }
    val initialWeight = sortedLogs.firstOrNull()?.weightKg ?: profile.weightKg
    val currentWeight = sortedLogs.lastOrNull()?.weightKg ?: profile.weightKg
    val weightDelta = currentWeight - initialWeight

    val totalVolumeLifted = workoutLogs.sumOf { it.totalVolumeKg.toDouble() }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = "Análise de Desempenho",
                subtitle = "Evolução antropométrica, volume e relatório PDF",
                badgeText = "EVOLUÇÃO & ANALYTICS",
                trailingContent = {
                    IconButton(
                        onClick = { viewModel.exportPdfProgressReport(context) },
                        modifier = Modifier.testTag("analytics_export_pdf_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Exportar PDF",
                            tint = GrowthOrange
                        )
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
            ) {
                // PDF Hero Banner Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(GrowthOrange.copy(alpha = 0.15f), Color.Transparent)
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
                                    Column {
                                        Text(
                                            text = "RELATÓRIO DE PROGRESSO EM PDF",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Documento Completo de Evolução",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = GrowthOrange,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Gera um relatório profissional diagramado em PDF contendo diagnóstico de macros, cardápio detalhado, divisão de treinos, lista de compras e histórico de medidas.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = { viewModel.exportPdfProgressReport(context) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("export_pdf_progress_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "EXPORTAR / COMPARTILHAR RELATÓRIO PDF",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Stats Grid Summary
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            label = "Variação de Peso",
                            value = "${if (weightDelta >= 0) "+" else ""}${String.format("%.1f", weightDelta)} kg",
                            subtext = "Desde o início",
                            valueColor = if (weightDelta >= 0) GrowthGreen else GrowthCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MetricCard(
                            label = "Volume Total",
                            value = "${(totalVolumeLifted / 1000f).toInt()} tons",
                            subtext = "${workoutLogs.size} treinos feitos",
                            valueColor = GrowthOrange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        MetricCard(
                            label = "Aderência",
                            value = "94%",
                            subtext = "Consistência macros",
                            valueColor = GrowthCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Interactive Weight Chart
                item {
                    WeightProgressionChart(
                        logs = progressLogs,
                        targetWeightKg = profile.monthlyTargetWeightKg,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Antropometric measurements header & add button
                item {
                    val logsWithPhotos = sortedLogs.filter { it.photoUri.isNotBlank() }
                    if (logsWithPhotos.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Compare, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("EVOLUÇÃO VISUAL (FOTOS)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthCyan, letterSpacing = 1.sp)
                                    }
                                    Text("${logsWithPhotos.size} registros", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                if (logsWithPhotos.size >= 2) {
                                    val oldest = logsWithPhotos.first()
                                    val newest = logsWithPhotos.last()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Oldest (Before)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                color = CarbonDark,
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            ) {
                                                Text("ANTES (${oldest.dateDisplay})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            SubcomposeAsyncImage(
                                                model = oldest.photoUri,
                                                contentDescription = "Antes",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            Text("${oldest.weightKg} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        }

                                        // Newest (Now/After)
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Surface(
                                                color = GrowthGreen.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp),
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            ) {
                                                Text("ATUAL (${newest.dateDisplay})", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GrowthGreen, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                            SubcomposeAsyncImage(
                                                model = newest.photoUri,
                                                contentDescription = "Atual",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(140.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                            Text("${newest.weightKg} kg", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                        }
                                    }
                                } else {
                                    val single = logsWithPhotos.first()
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SubcomposeAsyncImage(
                                            model = single.photoUri,
                                            contentDescription = "Foto recente",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 80.dp, height = 90.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text("Primeira foto registrada!", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                            Text("${single.dateDisplay} • ${single.weightKg} kg", fontSize = 11.sp, color = GrowthOrangeLight)
                                            Text("Adicione fotos nas próximas medições para gerar a comparação Antes e Depois lado a lado.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "HISTÓRICO DE MEDIDAS & PESAGENS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp
                        )

                        OutlinedButton(
                            onClick = { showAddMeasurementDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_measurement_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nova Medição", fontSize = 11.sp, color = GrowthCyan, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Antropometric log items
                items(progressLogs.sortedByDescending { it.dateMillis }) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("progress_log_${log.id}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = GrowthOrange.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = log.dateDisplay,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${log.weightKg} kg",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    if (log.bodyFatPercentage > 0f) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "(${log.bodyFatPercentage}% BF)",
                                            fontSize = 12.sp,
                                            color = GrowthGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteProgressLog(log.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }

                            if (log.photoUri.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CarbonDark, shape = RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = log.photoUri,
                                        contentDescription = "Foto do registro",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Foto corporal anexada", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                        Text("Registro visual salvo nesta pesagem", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (log.chestCm > 0f) Text("Tórax: ${log.chestCm}cm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (log.waistCm > 0f) Text("Cintura: ${log.waistCm}cm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (log.armRightCm > 0f) Text("Braço: ${log.armRightCm}cm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (log.thighCm > 0f) Text("Coxa: ${log.thighCm}cm", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (log.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Obs: ${log.notes}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddMeasurementDialog) {
        AddMeasurementDialog(
            currentWeight = profile.weightKg,
            onDismiss = { showAddMeasurementDialog = false },
            onConfirm = { weight, bf, chest, waist, arm, thigh, notes, photoUri ->
                viewModel.addProgressLog(weight, bf, chest, waist, arm, thigh, notes, photoUri)
                showAddMeasurementDialog = false
            }
        )
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    subtext: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtext, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AddMeasurementDialog(
    currentWeight: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, Float, Float, Float, Float, String, String) -> Unit
) {
    var weightText by remember { mutableStateOf(currentWeight.toString()) }
    var bfText by remember { mutableStateOf("") }
    var chestText by remember { mutableStateOf("") }
    var waistText by remember { mutableStateOf("") }
    var armText by remember { mutableStateOf("") }
    var thighText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Registrar Pesagem & Medidas", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    // Photo Upload Button & Preview
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (selectedPhotoUri.isNotBlank()) GrowthGreen else CarbonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (selectedPhotoUri.isNotBlank()) {
                                    SubcomposeAsyncImage(
                                        model = selectedPhotoUri,
                                        contentDescription = "Foto selecionada",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Foto Anexada com Sucesso!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthGreen)
                                        Text("Toque para trocar de imagem", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    Surface(
                                        color = GrowthOrange.copy(alpha = 0.15f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Foto de Comparação (Antes/Depois)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text("Toque para escolher da galeria", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }

                            if (selectedPhotoUri.isNotBlank()) {
                                IconButton(
                                    onClick = { selectedPhotoUri = "" },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remover", tint = GrowthRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Peso (kg) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f).testTag("progress_weight_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = bfText,
                            onValueChange = { bfText = it },
                            label = { Text("BF Estimado (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chestText,
                            onValueChange = { chestText = it },
                            label = { Text("Tórax (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = waistText,
                            onValueChange = { waistText = it },
                            label = { Text("Cintura (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = armText,
                            onValueChange = { armText = it },
                            label = { Text("Braço (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = thighText,
                            onValueChange = { thighText = it },
                            label = { Text("Coxa (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthOrange,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        label = { Text("Observações") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = weightText.toFloatOrNull() ?: currentWeight
                    val bf = bfText.toFloatOrNull() ?: 0f
                    val chest = chestText.toFloatOrNull() ?: 0f
                    val waist = waistText.toFloatOrNull() ?: 0f
                    val arm = armText.toFloatOrNull() ?: 0f
                    val thigh = thighText.toFloatOrNull() ?: 0f
                    onConfirm(w, bf, chest, waist, arm, thigh, notesText, selectedPhotoUri)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_save_measurement_btn")
            ) {
                Text("Salvar Registro", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
