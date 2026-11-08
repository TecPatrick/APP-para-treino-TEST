package com.example.ui.sync

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthOrange

@Composable
fun OfflineSyncQueueBanner(
    isOnline: Boolean,
    isSyncing: Boolean,
    pendingItems: List<PendingSyncItem>,
    lastSyncNotification: String?,
    onSyncNow: () -> Unit,
    onDismissNotification: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShow = !isOnline || pendingItems.isNotEmpty() || isSyncing || lastSyncNotification != null
    var expanded by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = shouldShow,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        val accentColor = when {
            !isOnline || pendingItems.isNotEmpty() -> GrowthOrange
            else -> GrowthCyan
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .testTag("offline_sync_queue_banner"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = CarbonCard.copy(alpha = 0.96f)
            ),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = 0.85f), GrowthCyan.copy(alpha = 0.45f))
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = pendingItems.isNotEmpty()) { expanded = !expanded }
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isSyncing -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = GrowthCyan,
                                        strokeWidth = 2.dp
                                    )
                                }
                                !isOnline -> {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = "Modo Offline",
                                        tint = GrowthOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                pendingItems.isNotEmpty() -> {
                                    Icon(
                                        imageVector = Icons.Default.CloudSync,
                                        contentDescription = "Fila de Sincronização",
                                        tint = GrowthOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                else -> {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = "Sincronizado na Nuvem",
                                        tint = GrowthCyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val titleText = when {
                                isSyncing -> "Sincronizando fila com a nuvem..."
                                !isOnline && pendingItems.isNotEmpty() ->
                                    "Modo Offline • ${pendingItems.size} edição(ões) na fila"
                                !isOnline ->
                                    "Modo Offline Ativo • Edição local liberada"
                                pendingItems.isNotEmpty() ->
                                    "Fila de Sincronização • ${pendingItems.size} pendente(s)"
                                else ->
                                    "Sincronização na Nuvem Concluída"
                            }

                            val subtitleText = when {
                                isSyncing -> "Enviando alterações de treinos e dieta ao Firebase..."
                                !isOnline -> "Continue editando treinos e dieta. Enviaremos quando a internet voltar."
                                pendingItems.isNotEmpty() -> {
                                    val latest = pendingItems.last()
                                    "Última: [${latest.category}] ${latest.description} • Toque para ver a fila"
                                }
                                else -> lastSyncNotification ?: "Todos os dados estão salvos na nuvem."
                            }

                            Text(
                                text = titleText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = subtitleText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pendingItems.isNotEmpty() && !isSyncing) {
                            TextButton(
                                onClick = onSyncNow,
                                modifier = Modifier.testTag("offline_sync_now_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = "Sincronizar Agora",
                                    tint = GrowthCyan,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sincronizar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthCyan
                                )
                            }
                            IconButton(
                                onClick = { expanded = !expanded },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) "Recolher fila" else "Expandir fila",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (lastSyncNotification != null && pendingItems.isEmpty() && !isSyncing) {
                            IconButton(
                                onClick = onDismissNotification,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("dismiss_sync_notification_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fechar aviso de sincronização",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = expanded && pendingItems.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        HorizontalDivider(color = CarbonBorder, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "FILA DE ALTERAÇÕES LOCAIS (${pendingItems.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GrowthCyan,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        pendingItems.takeLast(6).reversed().forEach { item ->
                            val isWorkout = item.category.equals("Treino", ignoreCase = true)
                            val badgeColor = if (isWorkout) GrowthOrange else GrowthCyan
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isWorkout) Icons.Default.FitnessCenter else Icons.Default.Restaurant,
                                        contentDescription = item.category,
                                        tint = badgeColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = badgeColor.copy(alpha = 0.16f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.category.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.description,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = item.formattedTime(),
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
}
