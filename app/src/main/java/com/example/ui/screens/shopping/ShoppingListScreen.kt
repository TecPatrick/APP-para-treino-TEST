package com.example.ui.screens.shopping

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MacronutrientAisle
import com.example.ui.ShoppingListViewModel
import com.example.ui.ShoppingViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel,
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
                if (isJointShoppingActive) {
                    item {
                        JointShoppingSplitBannerCard(
                            profileName = profile.name,
                            partnerName = partnerName,
                            myTotalCost = myTotalCost,
                            partnerTotalCost = partnerTotalCost,
                            totalEstimatedCost = totalEstimatedCost,
                            onUnlink = { shoppingListViewModel.unlinkPartnerShopping() }
                        )
                    }
                }

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

                item {
                    ShoppingPeriodAutoSyncCard(
                        selectedPeriodDays = selectedPeriodDays,
                        onSelectPeriodDays = { shoppingListViewModel.setPeriodDays(it) },
                        onRecalculateClick = { shoppingListViewModel.parseDietPlanAndGenerateShoppingList(selectedPeriodDays) }
                    )
                }

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
                            ShoppingAisleHeaderCard(
                                aisle = aisle,
                                aisleTotal = aisleTotal,
                                aisleAcquired = aisleAcquired,
                                aisleSubtotal = aisleSubtotal,
                                aisleProgress = aisleProgress,
                                aisleAccentColor = aisleAccentColor
                            )
                        }

                        items(
                            items = aisleItems,
                            key = { item -> item.id }
                        ) { item ->
                            ShoppingAisleItemCard(
                                item = item,
                                partnerName = partnerName,
                                aisleAccentColor = aisleAccentColor,
                                onToggleItem = { shoppingListViewModel.toggleShoppingItem(item) }
                            )
                        }
                    }
                }

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
