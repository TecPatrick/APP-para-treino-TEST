package com.example.ui.screens.achievements.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementProgress
import com.example.data.model.AchievementTier
import com.example.data.model.AchievementsSummaryState
import com.example.data.model.AnimeUniverse
import com.example.data.model.MuscleFocusGroup
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonCardElevated
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.GrowthCyan
import com.example.ui.theme.GrowthGreen
import com.example.ui.theme.GrowthOrange
import com.example.ui.theme.GrowthOrangeLight
import com.example.ui.theme.GrowthYellow

fun tierAccentColor(tier: AchievementTier): Color = when (tier) {
    AchievementTier.BRONZE -> Color(0xFFD98A56)
    AchievementTier.SILVER -> GrowthCyan
    AchievementTier.GOLD -> GrowthYellow
    AchievementTier.LEGENDARY -> GrowthOrange
}

fun universeAccentColor(universe: AnimeUniverse): Color = when (universe) {
    AnimeUniverse.SOLO_LEVELING -> GrowthCyan
    AnimeUniverse.DRAGON_BALL -> GrowthOrange
    AnimeUniverse.BOKU_NO_HERO -> GrowthGreen
    AnimeUniverse.DEMON_SLAYER -> Color(0xFFFF6B8B)
    AnimeUniverse.JUJUTSU_KAISEN -> Color(0xFFA78BFA)
    AnimeUniverse.ATTACK_ON_TITAN -> GrowthYellow
    AnimeUniverse.ONE_PUNCH_MAN -> GrowthOrangeLight
    AnimeUniverse.BAKI -> Color(0xFFEF4444)
}

fun categoryIcon(category: AchievementCategory): ImageVector = when (category) {
    AchievementCategory.ALL -> Icons.Default.EmojiEvents
    AchievementCategory.DIET_CONSISTENCY -> Icons.Default.Restaurant
    AchievementCategory.WORKOUT_CONSISTENCY -> Icons.Default.LocalFireDepartment
    AchievementCategory.MUSCLE_GROUPS -> Icons.Default.FitnessCenter
    AchievementCategory.CARDIO -> Icons.AutoMirrored.Filled.DirectionsRun
}

@Composable
fun EquippedAnimeTitlePill(
    summary: AchievementsSummaryState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = tierAccentColor(summary.activeTitleTier)
    Surface(
        color = accent.copy(alpha = 0.16f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("equipped_anime_title_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bolt,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = summary.activeTitleText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                color = CarbonDark.copy(alpha = 0.75f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Nv.${summary.hunterLevel}",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AchievementsSummaryBannerCard(
    summary: AchievementsSummaryState,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tierColor = tierAccentColor(summary.activeTitleTier)
    val overallFraction = if (summary.totalAvailable > 0) {
        (summary.totalUnlocked.toFloat() / summary.totalAvailable.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Card(
        colors = CardDefaults.cardColors(containerColor = CarbonCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                colors = listOf(tierColor.copy(alpha = 0.7f), GrowthCyan.copy(alpha = 0.55f))
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenGallery() }
            .testTag("achievements_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(tierColor.copy(alpha = 0.14f), CarbonCard)
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(tierColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = tierColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "PATENTE & CONQUISTAS ANIME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = GrowthOrangeLight,
                                letterSpacing = 0.9.sp
                            )
                            Surface(
                                color = GrowthCyan.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, GrowthCyan.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "NV. ${summary.hunterLevel} • ${summary.totalXp} XP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GrowthCyan,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = summary.activeTitleText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${summary.hunterRankLabel} • ${summary.activeTitleUniverse.displayName}",
                            fontSize = 11.sp,
                            color = tierColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    color = CarbonCardElevated,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, CarbonBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${summary.totalUnlocked}/${summary.totalAvailable}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = GrowthYellow
                        )
                        Text(
                            text = "Medalhas",
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { overallFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = tierColor,
                trackColor = CarbonDark
            )

            summary.nextClosestAchievement?.let { next ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CarbonDark.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Próxima medalha: ${next.definition.title}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Título: ${next.definition.animeTitleReward} (${next.currentValue}/${next.definition.targetValue} ${next.definition.unitLabel})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${next.progressPercent}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = GrowthCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenGallery,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, tierColor.copy(alpha = 0.75f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = tierColor.copy(alpha = 0.12f),
                    contentColor = tierColor
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("open_achievements_gallery_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ver Galeria de Medalhas & Equipar Título",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AchievementItemCard(
    progress: AchievementProgress,
    onEquipTitle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val def = progress.definition
    val tierColor = tierAccentColor(def.tier)
    val universeColor = universeAccentColor(def.animeUniverse)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (progress.isUnlocked) CarbonCard else CarbonCard.copy(alpha = 0.72f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (progress.isEquippedTitle) 1.5.dp else 1.dp,
            color = when {
                progress.isEquippedTitle -> GrowthYellow
                progress.isUnlocked -> tierColor.copy(alpha = 0.65f)
                else -> CarbonBorder
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("achievement_card_${def.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top badges row: Anime Universe + Tier + Muscle Focus
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = universeColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, universeColor.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = def.animeUniverse.badgeTag,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = universeColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (def.muscleFocus != MuscleFocusGroup.NONE) {
                        Surface(
                            color = CarbonCardElevated,
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, CarbonBorder)
                        ) {
                            Text(
                                text = def.muscleFocus.label,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = tierColor.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.55f))
                ) {
                    Text(
                        text = "${def.tier.label.uppercase()} • ${def.tier.rankTag}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = tierColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main icon + Title + Description
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (progress.isUnlocked) tierColor.copy(alpha = 0.22f) else CarbonDark,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (progress.isUnlocked) categoryIcon(def.category) else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (progress.isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = def.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = if (progress.isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (progress.isUnlocked) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Desbloqueada",
                                tint = GrowthGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = def.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Anime Title Reward Box
            Surface(
                color = CarbonDark,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(
                    1.dp,
                    if (progress.isUnlocked) tierColor.copy(alpha = 0.45f) else CarbonBorder
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (progress.isUnlocked) GrowthYellow else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "TÍTULO DE ANIME RECOMPENSA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = def.animeTitleReward,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (progress.isUnlocked) tierColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "+${def.tier.xpReward} XP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (progress.isUnlocked) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar & numbers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progresso: ${progress.currentValue} / ${def.targetValue} ${def.unitLabel}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress.isUnlocked) GrowthGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${progress.progressPercent}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (progress.isUnlocked) GrowthGreen else tierColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress.isUnlocked) GrowthGreen else tierColor,
                trackColor = CarbonDark
            )

            if (progress.isUnlocked) {
                Spacer(modifier = Modifier.height(12.dp))
                if (progress.isEquippedTitle) {
                    Surface(
                        color = GrowthYellow.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, GrowthYellow.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("equipped_indicator_${def.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GrowthYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PATENTE EQUIPADA NO PERFIL E DASHBOARD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = GrowthYellow
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onEquipTitle(def.id) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = tierColor,
                            contentColor = CarbonDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("equip_title_${def.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Equipar Título: ${def.animeTitleReward}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
