package com.example.ui.screens.achievements

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementTier
import com.example.data.model.AchievementsSummaryState
import com.example.ui.BaseVirtusDomainViewModel
import com.example.ui.components.GrowthHeader
import com.example.ui.screens.achievements.components.AchievementItemCard
import com.example.ui.screens.achievements.components.categoryIcon
import com.example.ui.screens.achievements.components.tierAccentColor
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow
import com.example.util.AchievementCatalogAndEngine

@Composable
fun AchievementsScreen(
    viewModel: BaseVirtusDomainViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val summary by viewModel.achievementsState.collectAsStateWithLifecycle()
    var selectedCategory by rememberSaveable { mutableStateOf(AchievementCategory.ALL) }
    var showOnlyUnlocked by rememberSaveable { mutableStateOf(false) }
    var showEquipTitleDialog by remember { mutableStateOf(false) }

    val filteredAchievements = remember(summary.achievements, selectedCategory, showOnlyUnlocked) {
        summary.achievements
            .filter { selectedCategory == AchievementCategory.ALL || it.definition.category == selectedCategory }
            .filter { !showOnlyUnlocked || it.isUnlocked }
            .sortedWith(
                compareByDescending<com.example.data.model.AchievementProgress> { it.isEquippedTitle }
                    .thenByDescending { it.isUnlocked }
                    .thenByDescending { it.progressFraction }
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("achievements_screen")
    ) {
        GrowthHeader(
            title = "Conquistas & Patentes Anime",
            subtitle = "Evolua sua consistência na dieta, músculos e cardio",
            badgeText = "SISTEMA DE RANKS",
            trailingContent = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("achievements_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hunter Status & Active Title Card
            item(key = "hunter_status_header", contentType = "header_card") {
                HunterStatusHeaderCard(
                    summary = summary,
                    onOpenTitleSelector = { showEquipTitleDialog = true }
                )
            }

            // 2. Category Filter Chips
            item(key = "category_filter_row", contentType = "filter_row") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(
                            items = AchievementCategory.entries,
                            key = { it.name },
                            contentType = { "category_chip" }
                        ) { category ->
                            val isSelected = selectedCategory == category
                            val countInCat = if (category == AchievementCategory.ALL) {
                                summary.achievements.count { it.isUnlocked } to summary.achievements.size
                            } else {
                                val list = summary.achievements.filter { it.definition.category == category }
                                list.count { it.isUnlocked } to list.size
                            }

                            Surface(
                                color = if (isSelected) GrowthOrange.copy(alpha = 0.22f) else CarbonCard,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) GrowthOrange else CarbonBorder
                                ),
                                modifier = Modifier
                                    .clickable { selectedCategory = category }
                                    .testTag("filter_cat_${category.name}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = categoryIcon(category),
                                        contentDescription = null,
                                        tint = if (isSelected) GrowthOrangeLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${category.shortLabel} (${countInCat.first}/${countInCat.second})",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GrowthOrangeLight,
                            letterSpacing = 0.9.sp
                        )
                        Surface(
                            color = if (showOnlyUnlocked) GrowthGreen.copy(alpha = 0.18f) else CarbonCard,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (showOnlyUnlocked) GrowthGreen else CarbonBorder
                            ),
                            modifier = Modifier
                                .clickable { showOnlyUnlocked = !showOnlyUnlocked }
                                .testTag("toggle_only_unlocked_btn")
                        ) {
                            Text(
                                text = if (showOnlyUnlocked) "Mostrando: Desbloqueadas ✓" else "Filtrar: Só Desbloqueadas",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showOnlyUnlocked) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            if (filteredAchievements.isEmpty()) {
                item(key = "empty_achievements_state", contentType = "empty_state") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CarbonCard),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CarbonBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GrowthOrangeLight,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Nenhuma medalha desbloqueada nesta categoria ainda",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Continue registrando seus treinos, refeições e sessões de cardio para liberar novos títulos!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(
                    items = filteredAchievements,
                    key = { it.definition.id },
                    contentType = { "achievement_item" }
                ) { achievement ->
                    AchievementItemCard(
                        progress = achievement,
                        onEquipTitle = { viewModel.equipAnimeTitle(it) }
                    )
                }
            }

            item(key = "bottom_spacer", contentType = "spacer") {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showEquipTitleDialog) {
        EquipAnimeTitleDialog(
            summary = summary,
            onSelectTitle = { id ->
                viewModel.equipAnimeTitle(id)
                showEquipTitleDialog = false
            },
            onDismiss = { showEquipTitleDialog = false }
        )
    }
}

@Composable
private fun HunterStatusHeaderCard(
    summary: AchievementsSummaryState,
    onOpenTitleSelector: () -> Unit
) {
    val tierColor = tierAccentColor(summary.activeTitleTier)
    val overallProgress = if (summary.totalAvailable > 0) {
        (summary.totalUnlocked.toFloat() / summary.totalAvailable.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                colors = listOf(tierColor, GrowthCyan)
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hunter_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(tierColor.copy(alpha = 0.16f), CarbonCard)
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(tierColor.copy(alpha = 0.22f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PATENTE EQUIPADA ATUAL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = GrowthCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = summary.activeTitleText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${summary.hunterRankLabel} • Nível ${summary.hunterLevel} (${summary.totalXp} XP)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tierColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tier counts row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AchievementTier.entries.forEach { tier ->
                    val tierTotal = summary.achievements.count { it.definition.tier == tier }
                    val tierUnlocked = summary.achievements.count { it.definition.tier == tier && it.isUnlocked }
                    val tColor = tierAccentColor(tier)

                    Surface(
                        color = CarbonCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, tColor.copy(alpha = 0.4f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$tierUnlocked/$tierTotal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = tColor
                            )
                            Text(
                                text = tier.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Domínio Geral de Medalhas",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${summary.totalUnlocked} de ${summary.totalAvailable} (${(overallProgress * 100).toInt()}%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = GrowthYellow
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GrowthYellow,
                trackColor = CarbonDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenTitleSelector,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = GrowthCyan.copy(alpha = 0.12f),
                    contentColor = GrowthCyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("open_title_selector_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Escolher Título de Anime Equipado (${summary.unlockedAchievements.size + 1} disponíveis)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EquipAnimeTitleDialog(
    summary: AchievementsSummaryState,
    onSelectTitle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CarbonCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = GrowthYellow,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Equipar Patente de Anime",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Escolha qual título desbloqueado será exibido abaixo do seu nome no Perfil e no Dashboard:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Starter title option
                val isStarterActive = summary.activeTitleId == AchievementCatalogAndEngine.STARTER_TITLE_ID
                Surface(
                    color = if (isStarterActive) GrowthCyan.copy(alpha = 0.18f) else CarbonCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, if (isStarterActive) GrowthCyan else CarbonBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectTitle(AchievementCatalogAndEngine.STARTER_TITLE_ID) }
                        .testTag("select_title_starter")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AchievementCatalogAndEngine.STARTER_TITLE_TEXT,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Solo Leveling • Patente Inicial",
                                fontSize = 10.sp,
                                color = GrowthCyan
                            )
                        }
                        if (isStarterActive) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = GrowthCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                summary.unlockedAchievements.forEach { unlocked ->
                    val def = unlocked.definition
                    val isSelected = summary.activeTitleId == def.id
                    val tColor = tierAccentColor(def.tier)

                    Surface(
                        color = if (isSelected) tColor.copy(alpha = 0.18f) else CarbonCardElevated,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (isSelected) tColor else CarbonBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTitle(def.id) }
                            .testTag("select_title_${def.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = def.animeTitleReward,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${def.animeUniverse.displayName} • ${def.title}",
                                    fontSize = 10.sp,
                                    color = tColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = tColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = GrowthCyan, fontWeight = FontWeight.Bold)
            }
        }
    )
}
