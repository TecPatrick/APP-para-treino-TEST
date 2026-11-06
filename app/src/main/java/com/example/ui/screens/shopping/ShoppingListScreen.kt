package com.example.ui.screens.shopping

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodCategory
import com.example.data.model.ShoppingItem
import com.example.ui.MacronutrientAisle
import com.example.ui.ShoppingListViewModel
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
import com.example.ui.theme.GrowthYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: VirtusViewModel,
    shoppingListViewModel: ShoppingListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToReceipts: () -> Unit = {}
) {
    val context = LocalContext.current
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val shoppingList by shoppingListViewModel.shoppingList.collectAsStateWithLifecycle()
    val aisleGroupedItems by shoppingListViewModel.aisleGroupedItems.collectAsStateWithLifecycle()
    val selectedPeriodDays by shoppingListViewModel.selectedPeriodDays.collectAsStateWithLifecycle()
    val uiMessage by shoppingListViewModel.uiMessage.collectAsStateWithLifecycle()

    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            shoppingListViewModel.clearUiMessage()
        }
    }

    var showAddItemDialog by remember { mutableStateOf(false) }

    var showExportCodeDialog by remember { mutableStateOf(false) }
    var generatedExportCode by remember { mutableStateOf("") }
    var showImportCodeDialog by remember { mutableStateOf(false) }

    val totalItems = shoppingList.size
    val completedItems = shoppingList.count { it.isChecked }
    val isJointShoppingActive = shoppingList.any { it.isJointItem }
    val partnerName = shoppingList.firstOrNull { it.isJointItem }?.partnerName.orEmpty().ifBlank { "Irmão" }

    val totalEstimatedCost = shoppingList.sumOf { it.estimatedPrice.toDouble() }.toFloat()
    val myTotalCost = shoppingList.sumOf { it.myEstimatedPrice.toDouble() }.toFloat()
    val partnerTotalCost = shoppingList.sumOf { it.partnerEstimatedPrice.toDouble() }.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = if (isJointShoppingActive) "Compra Conjunta 🤝" else "Lista de Compras",
                subtitle = if (isJointShoppingActive) "Lista somada com $partnerName" else "Sincronizada com sua dieta • Agrupada por Corredor de Macros",
                badgeText = if (isJointShoppingActive) "MODO COMPRA COLETIVA" else "CORREDORES DE MACRONUTRIENTES",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Receipts & Price Inflation shortcut
                        IconButton(
                            onClick = onNavigateToReceipts,
                            modifier = Modifier.testTag("nav_to_receipts_from_shopping_btn")
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                contentDescription = "Preços e NF",
                                tint = GrowthCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Export Code Button
                        IconButton(
                            onClick = {
                                shoppingListViewModel.exportShoppingCode(profile.name) { code ->
                                    generatedExportCode = code
                                    showExportCodeDialog = true
                                }
                            },
                            modifier = Modifier.testTag("export_shopping_code_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar Código", tint = GrowthOrange)
                        }

                        // Import & Link List Button
                        IconButton(
                            onClick = { showImportCodeDialog = true },
                            modifier = Modifier.testTag("import_partner_list_btn")
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = "Somar com Irmão", tint = GrowthCyan)
                        }

                        if (completedItems > 0) {
                            IconButton(
                                onClick = { shoppingListViewModel.clearCheckedShoppingItems() },
                                modifier = Modifier.testTag("clear_checked_shopping_btn")
                            ) {
                                Icon(Icons.Default.DeleteSweep, contentDescription = "Limpar Adquiridos", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
            ) {
                // Joint Shopping Split Dashboard Banner (when linked)
                if (isJointShoppingActive) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(GrowthCyan.copy(alpha = 0.12f), Color.Transparent)
                                        )
                                    )
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Group, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "DIVISÃO DE CUSTOS (VOCÊ & $partnerName)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GrowthCyan,
                                                letterSpacing = 1.sp
                                            )
                                        }

                                        Text(
                                            text = "Desvincular",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.clickable { shoppingListViewModel.unlinkPartnerShopping() }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Surface(
                                            color = CarbonCardElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Sua Parte (${profile.name})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "R$ ${String.format("%.2f", myTotalCost)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        Surface(
                                            color = CarbonCardElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Parte de $partnerName", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "R$ ${String.format("%.2f", partnerTotalCost)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = GrowthOrange
                                                )
                                            }
                                        }

                                        Surface(
                                            color = CarbonCardElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GrowthGreen.copy(alpha = 0.5f)),
                                            modifier = Modifier.weight(1.1f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Total Conjunto", fontSize = 10.sp, color = GrowthGreen)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    "R$ ${String.format("%.2f", totalEstimatedCost)}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = GrowthGreen
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Gráfico de Distribuição de Custos (Native Bar Chart)
                if (shoppingList.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("cost_distribution_chart_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "DISTRIBUIÇÃO DE CUSTOS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            "Investimento estimado para $selectedPeriodDays dias",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GrowthOrange, modifier = Modifier.size(16.dp))
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                val categoryCosts = remember(shoppingList) {
                                    shoppingList.groupBy { it.category }
                                        .mapValues { entry -> entry.value.sumOf { it.estimatedPrice.toDouble() }.toFloat() }
                                        .toList()
                                        .sortedByDescending { it.second }
                                }

                                if (categoryCosts.isNotEmpty()) {
                                    CostDistributionBarChart(
                                        data = categoryCosts,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Prompt banner explaining joint shopping with brother
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GroupAdd,
                                        contentDescription = null,
                                        tint = GrowthCyan,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Comprar junto com irmão / parceiro?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Junte as duas dietas para somar quantidades e economizar no frete",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { showImportCodeDialog = true },
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan),
                                    modifier = Modifier.testTag("open_join_list_prompt_btn")
                                ) {
                                    Text("Somar", fontSize = 11.sp, color = GrowthCyan, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Period Selector & Auto Generate Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthOrange.copy(alpha = 0.4f)),
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
                                Text(
                                    text = "SINCRONIZAÇÃO AUTOMÁTICA COM A DIETA",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthOrangeLight,
                                    letterSpacing = 1.sp
                                )
                                Surface(
                                    color = GrowthCyan.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f))
                                ) {
                                    Text(
                                        text = "AUTO-SYNC ATIVO",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GrowthCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = selectedPeriodDays == 7,
                                    onClick = { shoppingListViewModel.setPeriodDays(7) },
                                    label = { Text("7 Dias (Semanal)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GrowthOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("period_7_days")
                                )
                                FilterChip(
                                    selected = selectedPeriodDays == 15,
                                    onClick = { shoppingListViewModel.setPeriodDays(15) },
                                    label = { Text("15 Dias", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GrowthOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("period_15_days")
                                )
                                FilterChip(
                                    selected = selectedPeriodDays == 30,
                                    onClick = { shoppingListViewModel.setPeriodDays(30) },
                                    label = { Text("30 Dias (Mensal)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GrowthOrange,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f).testTag("period_30_days")
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { shoppingListViewModel.parseDietPlanAndGenerateShoppingList(selectedPeriodDays) },
                                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("generate_auto_shopping_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "RECALCULAR LISTA DA DIETA AGORA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Summary Cost & Progress Card
                if (shoppingList.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
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
                                    Column {
                                        Text(
                                            text = "PROGRESSO DOS ITENS ADQUIRIDOS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "$completedItems de $totalItems produtos marcados como Adquirido",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Estimativa Total",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "R$ ${String.format("%.2f", totalEstimatedCost)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GrowthCyan
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GrowthCyan,
                                    trackColor = CarbonCardElevated
                                )
                            }
                        }
                    }
                }

                if (shoppingList.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Sua lista de compras está vazia",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toque em 'Recalcular Lista da Dieta Agora' acima para gerar automaticamente os mantimentos da sua dieta para $selectedPeriodDays dias.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    // Grouped strictly by Macronutrient Aisles (Proteínas, Carboidratos, Gorduras)
                    // Inside each aisle, pending items are at the top and 'Adquirido' items move to the bottom.
                    aisleGroupedItems.forEach { (aisle, aisleItems) ->
                        val aisleTotal = aisleItems.size
                        val aisleAcquired = aisleItems.count { it.isChecked }
                        val aisleSubtotal = aisleItems.sumOf { it.estimatedPrice.toDouble() }.toFloat()
                        val aisleProgress = if (aisleTotal > 0) aisleAcquired.toFloat() / aisleTotal else 0f
                        val aisleAccentColor = when (aisle) {
                            MacronutrientAisle.PROTEIN -> GrowthOrange
                            MacronutrientAisle.CARBOHYDRATE -> GrowthCyan
                            MacronutrientAisle.FAT -> GrowthYellow
                        }

                        item(key = "aisle_header_${aisle.name}") {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CarbonCardElevated),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    aisleAccentColor.copy(alpha = 0.45f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp, bottom = 6.dp)
                                    .testTag("aisle_header_${aisle.name.lowercase()}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = aisleAccentColor.copy(alpha = 0.2f),
                                                    shape = RoundedCornerShape(5.dp),
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        1.dp,
                                                        aisleAccentColor.copy(alpha = 0.55f)
                                                    )
                                                ) {
                                                    Text(
                                                        text = aisle.badgeLabel,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = aisleAccentColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = aisle.title.uppercase(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    letterSpacing = 0.8.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = aisle.subtitle,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Surface(
                                                color = if (aisleAcquired == aisleTotal && aisleTotal > 0) {
                                                    GrowthGreen.copy(alpha = 0.2f)
                                                } else {
                                                    GrowthCyan.copy(alpha = 0.16f)
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "$aisleAcquired/$aisleTotal Adquiridos",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (aisleAcquired == aisleTotal && aisleTotal > 0) GrowthGreen else GrowthCyan,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "~R$ ${String.format("%.2f", aisleSubtotal)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = aisleAccentColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { aisleProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = if (aisleAcquired == aisleTotal && aisleTotal > 0) GrowthGreen else aisleAccentColor,
                                        trackColor = CarbonDark
                                    )
                                }
                            }
                        }

                        items(
                            items = aisleItems,
                            key = { item -> item.id }
                        ) { item ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (item.isChecked) CarbonDark.copy(alpha = 0.72f) else CarbonCard
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (item.isChecked) GrowthCyan.copy(alpha = 0.35f) else CarbonBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clickable { shoppingListViewModel.toggleShoppingItem(item) }
                                    .testTag("shopping_item_${item.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = if (item.isChecked) GrowthCyan else CarbonCardElevated,
                                            shape = CircleShape,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.5.dp,
                                                if (item.isChecked) GrowthCyan else aisleAccentColor.copy(alpha = 0.7f)
                                            ),
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (item.isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Adquirido",
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(15.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = item.name,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                                                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                                                )

                                                if (item.isChecked) {
                                                    Surface(
                                                        color = GrowthCyan.copy(alpha = 0.18f),
                                                        shape = RoundedCornerShape(4.dp),
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            1.dp,
                                                            GrowthCyan.copy(alpha = 0.5f)
                                                        ),
                                                        modifier = Modifier.testTag("acquired_badge_${item.id}")
                                                    ) {
                                                        Text(
                                                            text = "ADQUIRIDO",
                                                            fontSize = 8.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = GrowthCyan,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Combined breakdown pill if joint item
                                            if (item.isJointItem) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "👤 Você: ${item.quantity}${item.unit} | 🤝 $partnerName: ${item.partnerQuantity}${item.unit}",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = GrowthCyan
                                                    )
                                                }
                                            } else if (item.notes.isNotBlank()) {
                                                Text(
                                                    text = item.notes,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${item.totalCombinedQuantity} ${item.unit}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (item.isChecked) GrowthCyan else if (item.isJointItem) GrowthCyan else GrowthOrangeLight
                                        )
                                        if (item.estimatedPrice > 0f) {
                                            Text(
                                                text = "~R$ ${String.format("%.2f", item.estimatedPrice)}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Add item button
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showAddItemDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_manual_shopping_item_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Adicionar Produto Extra ao Corredor", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        AddManualShoppingItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, cat, qty, unit, price ->
                shoppingListViewModel.addExtraProduct(name, cat, qty, unit, price)
                showAddItemDialog = false
            }
        )
    }

    if (showExportCodeDialog) {
        ExportShoppingCodeDialog(
            code = generatedExportCode,
            onDismiss = { showExportCodeDialog = false }
        )
    }

    if (showImportCodeDialog) {
        ImportPartnerShoppingDialog(
            onDismiss = { showImportCodeDialog = false },
            onImport = { code, name ->
                shoppingListViewModel.importAndMergePartnerShopping(code, name)
                showImportCodeDialog = false
            }
        )
    }
}

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
                
                // Draw Y-axis grid lines (optional but adds "recharts" feel)
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

                    // Draw Bar Background (Track)
                    drawRoundRect(
                        color = color.copy(alpha = 0.05f),
                        topLeft = Offset(x, 0f),
                        size = Size(barWidth, canvasHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )

                    // Draw Active Bar with Gradient-like effect (simulated)
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                    )
                    
                    // Value label on top of bar if it's large enough
                    if (barHeight > 30.dp.toPx()) {
                        // Drawing text in Canvas is tricky without NativeCanvas, 
                        // so we usually overlay with Box or just skip for simplicity in Canvas.
                        // But for "polish", let's focus on the legend below.
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Legend with refined layout
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}

