package com.example.ui.screens.receipts

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FoodCategory
import com.example.data.model.ItemPriceInflationStat
import com.example.data.model.PurchaseItemLog
import com.example.data.model.PurchaseReceipt
import com.example.ui.GrowthViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptsInflationScreen(
    viewModel: GrowthViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val receipts by viewModel.allReceipts.collectAsStateWithLifecycle()
    val purchaseLogs by viewModel.allPurchaseLogs.collectAsStateWithLifecycle()
    val inflationStats = remember(purchaseLogs) {
        viewModel.computeInflationStats(purchaseLogs)
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Inflação & Variação de Preços, 1: Notas Fiscais
    var showAddReceiptDialog by remember { mutableStateOf(false) }

    val totalSpentLifetime = receipts.sumOf { it.totalAmount.toDouble() }.toFloat()
    val avgInflationRate = if (inflationStats.isNotEmpty()) {
        inflationStats.map { it.inflationPercentage }.average().toFloat()
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CarbonDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            GrowthHeader(
                title = "Controle de Preços & NF",
                subtitle = "Acompanhamento da inflação dos alimentos mês a mês",
                badgeText = "HISTÓRICO FINANCEIRO",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { showAddReceiptDialog = true },
                            modifier = Modifier.testTag("add_receipt_top_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Compra", tint = GrowthOrange)
                        }
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("receipts_back_btn")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CarbonCard,
                contentColor = GrowthOrange,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GrowthOrange
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Inflação dos Alimentos",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Notas Fiscais Cadastradas (${receipts.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) GrowthOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {
                    // Inflation KPI Summary Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "ÍNDICE DE INFLAÇÃO FITNESS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GrowthOrangeLight,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "Variação Média dos Itens da Dieta",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        color = if (avgInflationRate > 0) GrowthRed.copy(alpha = 0.2f) else GrowthGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (avgInflationRate > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                                contentDescription = null,
                                                tint = if (avgInflationRate > 0) GrowthRed else GrowthGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${if (avgInflationRate > 0) "+" else ""}${String.format("%.1f", avgInflationRate)}%",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (avgInflationRate > 0) GrowthRed else GrowthGreen
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Baseado no comparativo de preços unitários registrados nas suas notas fiscais de compras anteriores vs atuais.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Item Price Evolution Cards
                    item {
                        Text(
                            text = "VARIAÇÃO DE PREÇO POR PRODUTO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrowthOrangeLight,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    items(inflationStats) { item ->
                        val isUp = item.inflationPercentage > 0
                        val isDown = item.inflationPercentage < 0

                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("inflation_item_${item.foodName}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.foodName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Anterior: R$ ${String.format("%.2f", item.previousAvgPrice)}  ➔  Atual: R$ ${String.format("%.2f", item.currentAvgPrice)} / ${item.unit}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    color = when {
                                        isUp -> GrowthRed.copy(alpha = 0.15f)
                                        isDown -> GrowthGreen.copy(alpha = 0.15f)
                                        else -> CarbonCardElevated
                                    },
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        when {
                                            isUp -> GrowthRed.copy(alpha = 0.4f)
                                            isDown -> GrowthGreen.copy(alpha = 0.4f)
                                            else -> CarbonBorder
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isUp) {
                                            Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = GrowthRed, modifier = Modifier.size(12.dp))
                                        } else if (isDown) {
                                            Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = GrowthGreen, modifier = Modifier.size(12.dp))
                                        }
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${if (isUp) "+" else ""}${String.format("%.1f", item.inflationPercentage)}%",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = when {
                                                isUp -> GrowthRed
                                                isDown -> GrowthGreen
                                                else -> Color.White
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = { showAddReceiptDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier.fillMaxWidth().testTag("add_receipt_button_inflation")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = GrowthCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cadastrar Nova Nota Fiscal / Compra", color = GrowthCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Tab 1: Receipts List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
                ) {
                    items(receipts) { receipt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CarbonCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("receipt_card_${receipt.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = receipt.storeName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "${receipt.dateDisplay} • ${receipt.receiptNumber.ifBlank { "Sem nº" }} • ${receipt.itemCount} itens",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "R$ ${String.format("%.2f", receipt.totalAmount)}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Black,
                                            color = GrowthGreen
                                        )
                                        IconButton(
                                            onClick = { viewModel.deletePurchaseReceipt(receipt.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                if (receipt.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Obs: ${receipt.notes}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showAddReceiptDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CADASTRAR NOVA COMPRA / NOTA FISCAL", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddReceiptDialog) {
        AddReceiptDialog(
            onDismiss = { showAddReceiptDialog = false },
            onSave = { store, nfNumber, items, notes ->
                viewModel.registerPurchaseReceipt(store, nfNumber, System.currentTimeMillis(), items, notes)
                showAddReceiptDialog = false
            }
        )
    }
}

@Composable
fun AddReceiptDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, List<PurchaseItemLog>, String) -> Unit
) {
    var storeName by remember { mutableStateOf("Supermercado Atacadão") }
    var nfNumber by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Temporary list of items being entered for this receipt
    val itemsList = remember {
        mutableStateListOf(
            PurchaseItemLog(foodName = "Peito de Frango", category = FoodCategory.PROTEIN, quantity = 6f, unit = "kg", unitPricePaid = 21.90f, totalPricePaid = 131.40f, dateMillis = System.currentTimeMillis(), monthYearKey = ""),
            PurchaseItemLog(foodName = "Ovos (Cartela 30un)", category = FoodCategory.PROTEIN, quantity = 4f, unit = "cartela", unitPricePaid = 21.50f, totalPricePaid = 86.00f, dateMillis = System.currentTimeMillis(), monthYearKey = "")
        )
    }

    var newItemName by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("1") }
    var newItemUnit by remember { mutableStateOf("kg") }
    var newItemUnitPrice by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Text("Lançar Nota Fiscal / Compra", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text("Estabelecimento (Mercado / Loja de Suplementos)") },
                        modifier = Modifier.fillMaxWidth().testTag("receipt_store_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = nfNumber,
                        onValueChange = { nfNumber = it },
                        label = { Text("Número / Chave da Nota Fiscal") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthOrange,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Itens Comprados na Nota:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GrowthOrangeLight)
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(itemsList) { item ->
                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.foodName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("${item.quantity} ${item.unit} x R$ ${String.format("%.2f", item.unitPricePaid)} = R$ ${String.format("%.2f", item.totalPricePaid)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = { itemsList.remove(item) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover", tint = GrowthRed, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("+ Adicionar Item à Nota:", fontSize = 11.sp, color = GrowthCyan)
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        label = { Text("Nome (Ex: Arroz, Whey, Ovos)") },
                        modifier = Modifier.fillMaxWidth().testTag("receipt_item_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GrowthCyan,
                            unfocusedBorderColor = CarbonBorder
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = newItemQty,
                            onValueChange = { newItemQty = it },
                            label = { Text("Qtd") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthCyan,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                        OutlinedTextField(
                            value = newItemUnitPrice,
                            onValueChange = { newItemUnitPrice = it },
                            label = { Text("Preço Unit. (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1.5f).testTag("receipt_item_price_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GrowthCyan,
                                unfocusedBorderColor = CarbonBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (newItemName.isNotBlank() && newItemUnitPrice.isNotBlank()) {
                                val q = newItemQty.toFloatOrNull() ?: 1f
                                val p = newItemUnitPrice.toFloatOrNull() ?: 0f
                                itemsList.add(
                                    PurchaseItemLog(
                                        foodName = newItemName,
                                        category = FoodCategory.PROTEIN,
                                        quantity = q,
                                        unit = newItemUnit,
                                        unitPricePaid = p,
                                        totalPricePaid = q * p,
                                        dateMillis = System.currentTimeMillis(),
                                        monthYearKey = ""
                                    )
                                )
                                newItemName = ""
                                newItemUnitPrice = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CarbonCardElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Incluir Item na Lista", color = GrowthCyan, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (storeName.isNotBlank() && itemsList.isNotEmpty()) {
                        onSave(storeName, nfNumber, itemsList.toList(), notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GrowthOrange),
                modifier = Modifier.testTag("save_receipt_confirm_btn")
            ) {
                Text("Salvar Nota Fiscal", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
