package com.example.ui.screens.shopping

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodCategory
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

@Composable
fun ExportShoppingCodeDialog(
    code: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Share, contentDescription = null, tint = GrowthOrange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Compartilhar com Irmão", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Envie este código para o seu irmão. Ele pode colar no aplicativo dele para somar as listas e dividir a compra coletiva automaticamente:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = code,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = GrowthCyan,
                        modifier = Modifier.padding(10.dp),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Código Lista de Compras", code)
                        clipboard.setPrimaryClip(clip)

                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Oi! Aqui está o código da minha dieta/lista de compras do mês para a gente somar no app Dieta & Treino:\n\n$code")
                        }
                        context.startActivity(Intent.createChooser(intent, "Enviar no WhatsApp"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("copy_and_share_whatsapp_btn")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copiar & Enviar no WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
fun ImportPartnerShoppingDialog(
    onDismiss: () -> Unit,
    onImport: (String, String) -> Unit
) {
    var partnerName by remember { mutableStateOf("Irmão") }
    var codeText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.GroupAdd, contentDescription = null, tint = GrowthCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Somar Lista do Irmão", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Cole aqui o código gerado pelo app do seu irmão para unificar as listas e somar as quantidades de Whey, Frango, Ovos, etc.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = partnerName,
                    onValueChange = { partnerName = it },
                    label = { Text("Nome do Irmão / Parceiro") },
                    modifier = Modifier.fillMaxWidth().testTag("partner_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthCyan,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = codeText,
                    onValueChange = { codeText = it },
                    label = { Text("Código Copiado (FIT-SHARE:...)") },
                    modifier = Modifier.fillMaxWidth().testTag("partner_code_input"),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthCyan,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (codeText.isNotBlank()) {
                        onImport(codeText, partnerName.ifBlank { "Irmão" })
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthCyan),
                modifier = Modifier.testTag("confirm_merge_list_btn")
            ) {
                Text("Fundir & Somar Listas", color = Color.Black, fontWeight = FontWeight.Bold)
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
fun AddManualShoppingItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, FoodCategory, Float, String, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("un") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(FoodCategory.PROTEIN) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Adicionar Produto ao Corredor", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Corredor de Macronutriente:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrowthOrangeLight
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = category == FoodCategory.PROTEIN,
                        onClick = { category = FoodCategory.PROTEIN },
                        label = { Text("Proteína", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrowthOrange,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = category == FoodCategory.CARBOHYDRATE,
                        onClick = { category = FoodCategory.CARBOHYDRATE },
                        label = { Text("Carbo", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrowthCyan,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = category == FoodCategory.FAT,
                        onClick = { category = FoodCategory.FAT },
                        label = { Text("Gordura", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GrowthYellow,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome do Produto (Ex: Filé de Tilápia, Aveia)") },
                    modifier = Modifier.fillMaxWidth().testTag("manual_item_name_input"),
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
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Qtd") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unidade (kg, un, pct)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Preço Estimado (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GrowthOrange,
                        unfocusedBorderColor = CarbonBorder
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val qty = quantityText.toFloatOrNull() ?: 1f
                        val price = priceText.toFloatOrNull() ?: 0f
                        onConfirm(name, category, qty, unit, price)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("confirm_add_manual_item_btn")
            ) {
                Text("Adicionar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CostDistributionBarChart(
    data: List<Pair<FoodCategory, Float>>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxCost = remember(data) { data.maxOf { it.second }.coerceAtLeast(1f) }
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground

    val categoryColors = data.associate { (category, _) ->
        category to when (category) {
            FoodCategory.PROTEIN -> GrowthCyan
            FoodCategory.CARBOHYDRATE -> GrowthYellow
            FoodCategory.FAT -> GrowthOrange
            FoodCategory.GROWTH_SUPPLEMENT -> GrowthOrangeLight
            FoodCategory.FRUITS_VEGETABLES -> GrowthGreen
            FoodCategory.DAIRY -> GrowthCyan.copy(alpha = 0.7f)
            else -> onSurfaceVariant
        }
    }

    Column(modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barSpacing = 24.dp.toPx()
                val barWidth = (canvasWidth - (data.size - 1) * barSpacing) / data.size

                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = canvasHeight - (i * (canvasHeight / gridLines))
                    drawLine(
                        color = onSurfaceVariant.copy(alpha = 0.1f),
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                data.forEachIndexed { index, (category, cost) ->
                    val barHeight = (cost / maxCost) * canvasHeight
                    val x = index * (barWidth + barSpacing)
                    val y = canvasHeight - barHeight

                    val color = categoryColors[category] ?: onSurfaceVariant

                    drawRoundRect(
                        color = color.copy(alpha = 0.05f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, canvasHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            data.forEach { (cat, cost) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryColors[cat] ?: onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = cat.label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceVariant
                        )
                        Text(
                            text = "R$ ${String.format("%.2f", cost)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = onBackground
                        )
                    }
                }
            }
        }
    }
}
