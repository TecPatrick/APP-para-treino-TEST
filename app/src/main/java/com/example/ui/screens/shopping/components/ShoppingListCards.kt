package com.example.ui.screens.shopping

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShoppingItem
import com.example.ui.MacronutrientAisle
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight

@Composable
fun JointShoppingSplitBannerCard(
    profileName: String,
    partnerName: String,
    myTotalCost: Float,
    partnerTotalCost: Float,
    totalEstimatedCost: Float,
    onUnlink: () -> Unit
) {
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
                        modifier = Modifier.clickable { onUnlink() }
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
                            Text("Sua Parte ($profileName)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
fun ShoppingPeriodAutoSyncCard(
    selectedPeriodDays: Int,
    onSelectPeriodDays: (Int) -> Unit,
    onRecalculateClick: () -> Unit
) {
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
                    onClick = { onSelectPeriodDays(7) },
                    label = { Text("7 Dias (Semanal)", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrowthOrange,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).testTag("period_7_days")
                )
                FilterChip(
                    selected = selectedPeriodDays == 15,
                    onClick = { onSelectPeriodDays(15) },
                    label = { Text("15 Dias", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GrowthOrange,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.weight(1f).testTag("period_15_days")
                )
                FilterChip(
                    selected = selectedPeriodDays == 30,
                    onClick = { onSelectPeriodDays(30) },
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
                onClick = onRecalculateClick,
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

@Composable
fun ShoppingAisleHeaderCard(
    aisle: MacronutrientAisle,
    aisleTotal: Int,
    aisleAcquired: Int,
    aisleSubtotal: Float,
    aisleProgress: Float,
    aisleAccentColor: Color
) {
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

@Composable
fun ShoppingAisleItemCard(
    item: ShoppingItem,
    partnerName: String,
    aisleAccentColor: Color,
    onToggleItem: () -> Unit
) {
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
            .clickable { onToggleItem() }
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
