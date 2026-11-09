package com.example.data.model

import androidx.compose.runtime.Immutable

enum class AchievementCategory(val label: String, val shortLabel: String) {
    ALL("Todas as Conquistas", "Todas"),
    DIET_CONSISTENCY("Consistência na Dieta", "Dieta"),
    WORKOUT_CONSISTENCY("Consistência no Treino", "Treino"),
    MUSCLE_GROUPS("Grupos Musculares", "Músculos"),
    CARDIO("Cardio & Corrida", "Cardio")
}

enum class AchievementTier(
    val label: String,
    val rankTag: String,
    val xpReward: Int
) {
    BRONZE("Bronze", "RANK E", 100),
    SILVER("Prata", "RANK B", 250),
    GOLD("Ouro", "RANK S", 500),
    LEGENDARY("Lendário", "MONARCA", 1000)
}

enum class AnimeUniverse(val displayName: String, val badgeTag: String) {
    SOLO_LEVELING("Solo Leveling", "SOLO LEVELING"),
    DRAGON_BALL("Dragon Ball", "DRAGON BALL"),
    BOKU_NO_HERO("Boku no Hero", "MY HERO ACADEMIA"),
    DEMON_SLAYER("Demon Slayer", "KIMETSU NO YAIBA"),
    JUJUTSU_KAISEN("Jujutsu Kaisen", "JUJUTSU KAISEN"),
    ATTACK_ON_TITAN("Attack on Titan", "SHINGEKI NO KYOJIN"),
    ONE_PUNCH_MAN("One Punch Man", "ONE PUNCH MAN"),
    BAKI("Baki", "BAKI THE GRAPPLER")
}

enum class MuscleFocusGroup(val label: String) {
    NONE("Geral"),
    CHEST("Peitoral"),
    BACK("Costas & Dorsais"),
    LEGS("Pernas & Glúteos"),
    SHOULDERS("Ombros & Trapézio"),
    ARMS("Bíceps & Tríceps"),
    CORE("Core & Abdômen")
}

enum class AchievementMetricType {
    DIET_LOGGED_DAYS,
    DIET_STREAK_DAYS,
    DIET_PROTEIN_GOAL_DAYS,
    DIET_HYDRATION_DAYS,
    WORKOUT_TOTAL_SESSIONS,
    WORKOUT_STREAK_DAYS,
    WORKOUT_TOTAL_VOLUME_KG,
    MUSCLE_CHEST_SETS,
    MUSCLE_BACK_SETS,
    MUSCLE_LEGS_SETS,
    MUSCLE_SHOULDERS_SETS,
    MUSCLE_ARMS_SETS,
    MUSCLE_CORE_SETS,
    CARDIO_TOTAL_SESSIONS,
    CARDIO_TOTAL_DISTANCE_KM,
    CARDIO_TOTAL_CALORIES
}

@Immutable
data class AchievementDefinition(
    val id: String,
    val title: String,
    val animeTitleReward: String,
    val description: String,
    val category: AchievementCategory,
    val tier: AchievementTier,
    val animeUniverse: AnimeUniverse,
    val metricType: AchievementMetricType,
    val targetValue: Int,
    val unitLabel: String,
    val muscleFocus: MuscleFocusGroup = MuscleFocusGroup.NONE
)

@Immutable
data class AchievementProgress(
    val definition: AchievementDefinition,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val progressFraction: Float,
    val isEquippedTitle: Boolean
) {
    val progressPercent: Int
        get() = (progressFraction * 100f).toInt().coerceIn(0, 100)
}

@Immutable
data class AchievementsSummaryState(
    val totalUnlocked: Int = 0,
    val totalAvailable: Int = 0,
    val totalXp: Int = 0,
    val hunterLevel: Int = 1,
    val hunterRankLabel: String = "Caçador Rank E",
    val activeTitleId: String = "starter_rank_e",
    val activeTitleText: String = "Recruta do Sistema [Rank E]",
    val activeTitleUniverse: AnimeUniverse = AnimeUniverse.SOLO_LEVELING,
    val activeTitleTier: AchievementTier = AchievementTier.BRONZE,
    val achievements: List<AchievementProgress> = emptyList(),
    val nextClosestAchievement: AchievementProgress? = null
) {
    val unlockedAchievements: List<AchievementProgress>
        get() = achievements.filter { it.isUnlocked }
}
