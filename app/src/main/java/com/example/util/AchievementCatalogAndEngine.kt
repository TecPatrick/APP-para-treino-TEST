package com.example.util

import com.example.data.model.AchievementCategory
import com.example.data.model.AchievementDefinition
import com.example.data.model.AchievementMetricType
import com.example.data.model.AchievementProgress
import com.example.data.model.AchievementTier
import com.example.data.model.AchievementsSummaryState
import com.example.data.model.AnimeUniverse
import com.example.data.model.CardioLog
import com.example.data.model.DailyIntakeHistory
import com.example.data.model.FoodRecord
import com.example.data.model.GpsRouteLog
import com.example.data.model.MuscleFocusGroup
import com.example.data.model.UserProfile
import com.example.data.model.WorkoutExercise
import com.example.data.model.WorkoutLog
import com.example.data.model.WorkoutRoutine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object AchievementCatalogAndEngine {

    const val STARTER_TITLE_ID = "starter_rank_e"
    const val STARTER_TITLE_TEXT = "Recruta do Sistema [Rank E]"

    val catalog: List<AchievementDefinition> = listOf(
        // ==================== CONSISTÊNCIA NA DIETA ====================
        AchievementDefinition(
            id = "diet_days_3",
            title = "Ração de Missão Diária",
            animeTitleReward = "Sobrevivente da Dungeon [Rank E]",
            description = "Registre suas refeições no diário alimentar em pelo menos 3 dias diferentes.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.BRONZE,
            animeUniverse = AnimeUniverse.SOLO_LEVELING,
            metricType = AchievementMetricType.DIET_LOGGED_DAYS,
            targetValue = 3,
            unitLabel = "dias"
        ),
        AchievementDefinition(
            id = "diet_days_10",
            title = "Sementes dos Deuses (Senzu)",
            animeTitleReward = "Guardião das Sementes dos Deuses",
            description = "Acumule 10 dias com registro alimentar completo para recuperação muscular máxima.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.DRAGON_BALL,
            metricType = AchievementMetricType.DIET_LOGGED_DAYS,
            targetValue = 10,
            unitLabel = "dias"
        ),
        AchievementDefinition(
            id = "diet_days_25",
            title = "Banquete do Rei das Maldições",
            animeTitleReward = "Recipiente de Nutrição Absoluta",
            description = "Mantenha a disciplina registrando sua dieta em 25 dias diferentes.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.JUJUTSU_KAISEN,
            metricType = AchievementMetricType.DIET_LOGGED_DAYS,
            targetValue = 25,
            unitLabel = "dias"
        ),
        AchievementDefinition(
            id = "diet_streak_5",
            title = "Respiração Nutricional Constante",
            animeTitleReward = "Discípulo da Concentração Total",
            description = "Alcance uma sequência de 5 dias seguidos registrando sua alimentação sem falhar.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.DEMON_SLAYER,
            metricType = AchievementMetricType.DIET_STREAK_DAYS,
            targetValue = 5,
            unitLabel = "dias seguidos"
        ),
        AchievementDefinition(
            id = "diet_streak_14",
            title = "Metabolismo de Concentração Total 24/7",
            animeTitleReward = "Hashira da Disciplina Alimentar",
            description = "Mantenha 14 dias consecutivos de consistência no diário alimentar.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.DEMON_SLAYER,
            metricType = AchievementMetricType.DIET_STREAK_DAYS,
            targetValue = 14,
            unitLabel = "dias seguidos"
        ),
        AchievementDefinition(
            id = "diet_protein_7",
            title = "Síntese Proteica Saiyajin",
            animeTitleReward = "Sangue Saiyajin Anabólico",
            description = "Atingir pelo menos 80% da meta diária de proteínas em 7 dias.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.DRAGON_BALL,
            metricType = AchievementMetricType.DIET_PROTEIN_GOAL_DAYS,
            targetValue = 7,
            unitLabel = "dias na meta"
        ),
        AchievementDefinition(
            id = "diet_water_7",
            title = "Primeira Forma: Fluxo da Água",
            animeTitleReward = "Mestre da Respiração da Água",
            description = "Bata pelo menos 2.000 ml (ou 80% da meta de água) em 7 dias registrados.",
            category = AchievementCategory.DIET_CONSISTENCY,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.DEMON_SLAYER,
            metricType = AchievementMetricType.DIET_HYDRATION_DAYS,
            targetValue = 7,
            unitLabel = "dias hidratado"
        ),

        // ==================== CONSISTÊNCIA NO TREINAMENTO ====================
        AchievementDefinition(
            id = "workout_sessions_1",
            title = "Despertar do Jogador",
            animeTitleReward = "Caçador Desperto [Rank E]",
            description = "Conclua seu primeiro treino registrado no sistema Virtus.",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.BRONZE,
            animeUniverse = AnimeUniverse.SOLO_LEVELING,
            metricType = AchievementMetricType.WORKOUT_TOTAL_SESSIONS,
            targetValue = 1,
            unitLabel = "treino"
        ),
        AchievementDefinition(
            id = "workout_sessions_10",
            title = "Missão Diária: Força Bruta",
            animeTitleReward = "Caçador de Elite [Rank B]",
            description = "Complete 10 sessões de treinamento sem desistir da missão diária.",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.SOLO_LEVELING,
            metricType = AchievementMetricType.WORKOUT_TOTAL_SESSIONS,
            targetValue = 10,
            unitLabel = "treinos"
        ),
        AchievementDefinition(
            id = "workout_sessions_25",
            title = "Câmara do Tempo Hiperbólica",
            animeTitleReward = "Super Saiyajin Full Power",
            description = "Complete 25 treinos pesados superando seus próprios limites.",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.DRAGON_BALL,
            metricType = AchievementMetricType.WORKOUT_TOTAL_SESSIONS,
            targetValue = 25,
            unitLabel = "treinos"
        ),
        AchievementDefinition(
            id = "workout_sessions_50",
            title = "Trono do Monarca das Sombras",
            animeTitleReward = "Monarca das Sombras [Rank S]",
            description = "Atinja a marca histórica de 50 treinos concluídos com domínio absoluto.",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.SOLO_LEVELING,
            metricType = AchievementMetricType.WORKOUT_TOTAL_SESSIONS,
            targetValue = 50,
            unitLabel = "treinos"
        ),
        AchievementDefinition(
            id = "workout_streak_5",
            title = "Espírito Plus Ultra",
            animeTitleReward = "Herói Plus Ultra em Ascensão",
            description = "Mantenha 5 dias consecutivos de atividade física (musculação ou cardio).",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.BOKU_NO_HERO,
            metricType = AchievementMetricType.WORKOUT_STREAK_DAYS,
            targetValue = 5,
            unitLabel = "dias seguidos"
        ),
        AchievementDefinition(
            id = "workout_volume_25000",
            title = "Domínio do Instinto Superior",
            animeTitleReward = "Portador do Instinto Superior",
            description = "Mova um volume total acumulado de 25.000 kg somando todas as suas sessões.",
            category = AchievementCategory.WORKOUT_CONSISTENCY,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.DRAGON_BALL,
            metricType = AchievementMetricType.WORKOUT_TOTAL_VOLUME_KG,
            targetValue = 25000,
            unitLabel = "kg"
        ),

        // ==================== GRUPOS MUSCULARES ====================
        // PEITORAL
        AchievementDefinition(
            id = "muscle_chest_20",
            title = "Detroit Smash no Supino",
            animeTitleReward = "Discípulo do One For All",
            description = "Acumule 20 séries focadas em Peitoral nos seus treinos.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.BOKU_NO_HERO,
            metricType = AchievementMetricType.MUSCLE_CHEST_SETS,
            targetValue = 20,
            unitLabel = "séries de peito",
            muscleFocus = MuscleFocusGroup.CHEST
        ),
        AchievementDefinition(
            id = "muscle_chest_60",
            title = "United States of Smash (Peitoral de Aço)",
            animeTitleReward = "Símbolo da Paz — 100% One For All",
            description = "Domine o treino de empurrar acumulando 60 séries de Peitoral.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.BOKU_NO_HERO,
            metricType = AchievementMetricType.MUSCLE_CHEST_SETS,
            targetValue = 60,
            unitLabel = "séries de peito",
            muscleFocus = MuscleFocusGroup.CHEST
        ),

        // COSTAS / DORSAIS
        AchievementDefinition(
            id = "muscle_back_20",
            title = "Despertar da Dorsal",
            animeTitleReward = "Caçador de Dorsais [Sangue Hanma]",
            description = "Acumule 20 séries de puxadas e remadas para Costas & Dorsais.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.BAKI,
            metricType = AchievementMetricType.MUSCLE_BACK_SETS,
            targetValue = 20,
            unitLabel = "séries de costas",
            muscleFocus = MuscleFocusGroup.BACK
        ),
        AchievementDefinition(
            id = "muscle_back_60",
            title = "A Face do Ogro (Demon Back)",
            animeTitleReward = "Costas de Demônio — Ogro Hanma",
            description = "Esculpa uma dorsal lendária completando 60 séries de Costas.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.BAKI,
            metricType = AchievementMetricType.MUSCLE_BACK_SETS,
            targetValue = 60,
            unitLabel = "séries de costas",
            muscleFocus = MuscleFocusGroup.BACK
        ),

        // PERNAS / INFERIORES
        AchievementDefinition(
            id = "muscle_legs_20",
            title = "Investida da Muralha Maria",
            animeTitleReward = "Vanguarda do Titã Blindado",
            description = "Não pule o treino de pernas: acumule 20 séries de Quadríceps, Posterior e Glúteos.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.ATTACK_ON_TITAN,
            metricType = AchievementMetricType.MUSCLE_LEGS_SETS,
            targetValue = 20,
            unitLabel = "séries de pernas",
            muscleFocus = MuscleFocusGroup.LEGS
        ),
        AchievementDefinition(
            id = "muscle_legs_60",
            title = "Treinamento sob Gravidade 100x",
            animeTitleReward = "Príncipe Saiyajin — Gravidade 100x",
            description = "Suporte cargas esmagadoras acumulando 60 séries de membros inferiores.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.DRAGON_BALL,
            metricType = AchievementMetricType.MUSCLE_LEGS_SETS,
            targetValue = 60,
            unitLabel = "séries de pernas",
            muscleFocus = MuscleFocusGroup.LEGS
        ),

        // OMBROS / DELTOIDES
        AchievementDefinition(
            id = "muscle_shoulders_20",
            title = "Chama Incandescente de Rengoku",
            animeTitleReward = "Hashira das Chamas (Deltoides de Aço)",
            description = "Construa ombros largos acumulando 20 séries de Ombros e Trapézio.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.DEMON_SLAYER,
            metricType = AchievementMetricType.MUSCLE_SHOULDERS_SETS,
            targetValue = 20,
            unitLabel = "séries de ombros",
            muscleFocus = MuscleFocusGroup.SHOULDERS
        ),

        // BRAÇOS (BÍCEPS & TRÍCEPS)
        AchievementDefinition(
            id = "muscle_arms_25",
            title = "Impacto do Flash Negro (Kokusen)",
            animeTitleReward = "Punho do Flash Negro",
            description = "Acumule 25 séries de Bíceps, Tríceps e Antebraço com contração máxima.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.JUJUTSU_KAISEN,
            metricType = AchievementMetricType.MUSCLE_ARMS_SETS,
            targetValue = 25,
            unitLabel = "séries de braços",
            muscleFocus = MuscleFocusGroup.ARMS
        ),

        // CORE / ABDÔMEN & CORPO INTEIRO
        AchievementDefinition(
            id = "muscle_core_15",
            title = "Restrição Celestial Completa",
            animeTitleReward = "Anomalia Física — Toji Fushiguro",
            description = "Fortaleça o centro do corpo acumulando 15 séries de Core/Abdômen ou treinos completos.",
            category = AchievementCategory.MUSCLE_GROUPS,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.JUJUTSU_KAISEN,
            metricType = AchievementMetricType.MUSCLE_CORE_SETS,
            targetValue = 15,
            unitLabel = "séries de core",
            muscleFocus = MuscleFocusGroup.CORE
        ),

        // ==================== CARDIO & CORRIDA ====================
        AchievementDefinition(
            id = "cardio_sessions_3",
            title = "Recruta da Tropa de Exploração",
            animeTitleReward = "Soldado das Asas da Liberdade",
            description = "Conclua 3 sessões de cardio (Esteira, Bike ou Corrida GPS).",
            category = AchievementCategory.CARDIO,
            tier = AchievementTier.BRONZE,
            animeUniverse = AnimeUniverse.ATTACK_ON_TITAN,
            metricType = AchievementMetricType.CARDIO_TOTAL_SESSIONS,
            targetValue = 3,
            unitLabel = "sessões"
        ),
        AchievementDefinition(
            id = "cardio_sessions_12",
            title = "Respiração do Trovão: Seis Dobras",
            animeTitleReward = "Velocidade Divina do Trovão",
            description = "Complete 12 sessões de cardio mantendo fôlego e explosão cardiovascular.",
            category = AchievementCategory.CARDIO,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.DEMON_SLAYER,
            metricType = AchievementMetricType.CARDIO_TOTAL_SESSIONS,
            targetValue = 12,
            unitLabel = "sessões"
        ),
        AchievementDefinition(
            id = "cardio_distance_10km",
            title = "Protocolo Diário de Saitama (10 km)",
            animeTitleReward = "Quebrador de Limitador [Saitama]",
            description = "Acumule 10 km totais percorridos em corridas GPS ou sessões de cardio.",
            category = AchievementCategory.CARDIO,
            tier = AchievementTier.GOLD,
            animeUniverse = AnimeUniverse.ONE_PUNCH_MAN,
            metricType = AchievementMetricType.CARDIO_TOTAL_DISTANCE_KM,
            targetValue = 10,
            unitLabel = "km"
        ),
        AchievementDefinition(
            id = "cardio_distance_42km",
            title = "Shinzou wo Sasageyo! Além das Muralhas",
            animeTitleReward = "Capitão Levi — Mais Forte da Humanidade",
            description = "Ultrapasse 42 km acumulados em corridas e expedições aeróbicas.",
            category = AchievementCategory.CARDIO,
            tier = AchievementTier.LEGENDARY,
            animeUniverse = AnimeUniverse.ATTACK_ON_TITAN,
            metricType = AchievementMetricType.CARDIO_TOTAL_DISTANCE_KM,
            targetValue = 42,
            unitLabel = "km"
        ),
        AchievementDefinition(
            id = "cardio_calories_2500",
            title = "Motor de Vapor Colossal",
            animeTitleReward = "Força Térmica Implacável",
            description = "Queime um total acumulado de 2.500 kcal em atividades de cardio e corrida.",
            category = AchievementCategory.CARDIO,
            tier = AchievementTier.SILVER,
            animeUniverse = AnimeUniverse.ATTACK_ON_TITAN,
            metricType = AchievementMetricType.CARDIO_TOTAL_CALORIES,
            targetValue = 2500,
            unitLabel = "kcal"
        )
    )

    fun evaluateAchievements(
        profile: UserProfile,
        foodRecords: List<FoodRecord>,
        intakeHistory: List<DailyIntakeHistory>,
        workoutLogs: List<WorkoutLog>,
        workoutRoutines: List<WorkoutRoutine>,
        allExercises: List<WorkoutExercise>,
        cardioLogs: List<CardioLog>,
        gpsRouteLogs: List<GpsRouteLog>,
        equippedTitleId: String?
    ): AchievementsSummaryState {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        // 1. Diet metrics
        val foodDates = foodRecords.map { normalizeDateKey(it.dateString, it.dateMillis, dateFormatter) }.toSet()
        val intakeDates = intakeHistory.filter { it.totalCalories > 0f }.map { normalizeDateKey(it.dateString, it.dateMillis, dateFormatter) }.toSet()
        val allDietDates = (foodDates + intakeDates).filter { it.isNotBlank() }.toSet()
        val dietLoggedDays = allDietDates.size
        val dietStreakDays = calculateMaxConsecutiveDays(allDietDates)

        val proteinTargetThreshold = (profile.targetProteinGrams.coerceAtLeast(80) * 0.8f)
        val proteinByDateFromRecords = foodRecords
            .groupBy { normalizeDateKey(it.dateString, it.dateMillis, dateFormatter) }
            .mapValues { (_, records) -> records.sumOf { it.protein.toDouble() }.toFloat() }
        val proteinByDateFromHistory = intakeHistory.associate {
            normalizeDateKey(it.dateString, it.dateMillis, dateFormatter) to it.totalProtein
        }
        val allProteinDates = (proteinByDateFromRecords.keys + proteinByDateFromHistory.keys)
        val dietProteinGoalDays = allProteinDates.count { dateKey ->
            val prot = maxOf(proteinByDateFromRecords[dateKey] ?: 0f, proteinByDateFromHistory[dateKey] ?: 0f)
            prot >= proteinTargetThreshold
        }

        val waterTargetThreshold = minOf(2000, (profile.targetWaterMl.coerceAtLeast(2000) * 0.8f).toInt())
        val dietHydrationDays = intakeHistory.count { it.waterDrankMl >= waterTargetThreshold }

        // 2. Workout Consistency metrics
        val workoutTotalSessions = workoutLogs.size
        val workoutTotalVolumeKg = workoutLogs.sumOf { it.totalVolumeKg.toDouble() }.roundToInt()

        val activeTrainingDates = buildSet {
            workoutLogs.forEach { add(dateFormatter.format(Date(it.dateMillis))) }
            cardioLogs.forEach { add(dateFormatter.format(Date(it.dateMillis))) }
            gpsRouteLogs.forEach { add(dateFormatter.format(Date(it.dateMillis))) }
        }
        val workoutStreakDays = calculateMaxConsecutiveDays(activeTrainingDates)

        // 3. Muscle group sets calculation
        val muscleSetsMap = calculateMuscleGroupSets(
            workoutLogs = workoutLogs,
            workoutRoutines = workoutRoutines,
            allExercises = allExercises
        )

        // 4. Cardio metrics
        val cardioTotalSessions = cardioLogs.size + gpsRouteLogs.size
        val cardioDistanceFromManual = cardioLogs.sumOf { log ->
            (log.distanceKm ?: (log.durationMinutes * 0.12f)).toDouble()
        }
        val cardioDistanceFromGps = gpsRouteLogs.sumOf { (it.distanceMeters / 1000f).toDouble() }
        val cardioTotalDistanceKm = (cardioDistanceFromManual + cardioDistanceFromGps).roundToInt()

        val cardioTotalCalories = cardioLogs.sumOf { it.caloriesBurned } +
            gpsRouteLogs.sumOf { it.caloriesBurned.toDouble() }.roundToInt()

        // Map metric values
        val metricValues: Map<AchievementMetricType, Int> = mapOf(
            AchievementMetricType.DIET_LOGGED_DAYS to dietLoggedDays,
            AchievementMetricType.DIET_STREAK_DAYS to dietStreakDays,
            AchievementMetricType.DIET_PROTEIN_GOAL_DAYS to dietProteinGoalDays,
            AchievementMetricType.DIET_HYDRATION_DAYS to dietHydrationDays,
            AchievementMetricType.WORKOUT_TOTAL_SESSIONS to workoutTotalSessions,
            AchievementMetricType.WORKOUT_STREAK_DAYS to workoutStreakDays,
            AchievementMetricType.WORKOUT_TOTAL_VOLUME_KG to workoutTotalVolumeKg,
            AchievementMetricType.MUSCLE_CHEST_SETS to (muscleSetsMap[MuscleFocusGroup.CHEST] ?: 0),
            AchievementMetricType.MUSCLE_BACK_SETS to (muscleSetsMap[MuscleFocusGroup.BACK] ?: 0),
            AchievementMetricType.MUSCLE_LEGS_SETS to (muscleSetsMap[MuscleFocusGroup.LEGS] ?: 0),
            AchievementMetricType.MUSCLE_SHOULDERS_SETS to (muscleSetsMap[MuscleFocusGroup.SHOULDERS] ?: 0),
            AchievementMetricType.MUSCLE_ARMS_SETS to (muscleSetsMap[MuscleFocusGroup.ARMS] ?: 0),
            AchievementMetricType.MUSCLE_CORE_SETS to (muscleSetsMap[MuscleFocusGroup.CORE] ?: 0),
            AchievementMetricType.CARDIO_TOTAL_SESSIONS to cardioTotalSessions,
            AchievementMetricType.CARDIO_TOTAL_DISTANCE_KM to cardioTotalDistanceKm,
            AchievementMetricType.CARDIO_TOTAL_CALORIES to cardioTotalCalories
        )

        // First pass to determine unlocked achievements
        val rawProgress = catalog.map { def ->
            val current = (metricValues[def.metricType] ?: 0).coerceAtLeast(0)
            val unlocked = current >= def.targetValue
            val fraction = if (def.targetValue > 0) {
                (current.toFloat() / def.targetValue.toFloat()).coerceIn(0f, 1f)
            } else 1f
            AchievementProgress(
                definition = def,
                currentValue = current,
                isUnlocked = unlocked,
                progressFraction = fraction,
                isEquippedTitle = false
            )
        }

        // Resolve active equipped title:
        // If user selected a valid unlocked achievement ID, use it;
        // Otherwise if user selected STARTER_TITLE_ID explicitly, use starter;
        // Otherwise auto-equip the highest-tier unlocked achievement (or starter if none unlocked).
        val unlockedList = rawProgress.filter { it.isUnlocked }
        val explicitMatch = unlockedList.find { it.definition.id == equippedTitleId }
        val resolvedActiveAchievement = when {
            explicitMatch != null -> explicitMatch
            equippedTitleId == STARTER_TITLE_ID -> null
            else -> unlockedList.maxByOrNull { it.definition.tier.xpReward }
        }

        val activeId = resolvedActiveAchievement?.definition?.id ?: STARTER_TITLE_ID
        val activeText = resolvedActiveAchievement?.definition?.animeTitleReward ?: STARTER_TITLE_TEXT
        val activeUniverse = resolvedActiveAchievement?.definition?.animeUniverse ?: AnimeUniverse.SOLO_LEVELING
        val activeTier = resolvedActiveAchievement?.definition?.tier ?: AchievementTier.BRONZE

        val finalAchievements = rawProgress.map { item ->
            item.copy(isEquippedTitle = item.definition.id == activeId)
        }

        val totalXp = unlockedList.sumOf { it.definition.tier.xpReward }
        val hunterLevel = (1 + (totalXp / 250)).coerceAtLeast(1)
        val hunterRankLabel = when {
            totalXp >= 3500 -> "Monarca Soberano [Nível Nacional]"
            totalXp >= 2000 -> "Caçador Rank S [Hashira]"
            totalXp >= 1000 -> "Caçador Rank A [Saiyajin Elite]"
            totalXp >= 450 -> "Caçador Rank B [Plus Ultra]"
            totalXp >= 150 -> "Caçador Rank C [Guerreiro]"
            else -> "Caçador Rank E [Desperto]"
        }

        val nextClosest = finalAchievements
            .filter { !it.isUnlocked }
            .maxByOrNull { it.progressFraction }

        return AchievementsSummaryState(
            totalUnlocked = unlockedList.size,
            totalAvailable = finalAchievements.size,
            totalXp = totalXp,
            hunterLevel = hunterLevel,
            hunterRankLabel = hunterRankLabel,
            activeTitleId = activeId,
            activeTitleText = activeText,
            activeTitleUniverse = activeUniverse,
            activeTitleTier = activeTier,
            achievements = finalAchievements,
            nextClosestAchievement = nextClosest
        )
    }

    private fun calculateMuscleGroupSets(
        workoutLogs: List<WorkoutLog>,
        workoutRoutines: List<WorkoutRoutine>,
        allExercises: List<WorkoutExercise>
    ): Map<MuscleFocusGroup, Int> {
        val result = mutableMapOf(
            MuscleFocusGroup.CHEST to 0,
            MuscleFocusGroup.BACK to 0,
            MuscleFocusGroup.LEGS to 0,
            MuscleFocusGroup.SHOULDERS to 0,
            MuscleFocusGroup.ARMS to 0,
            MuscleFocusGroup.CORE to 0
        )

        workoutLogs.forEach { log ->
            val totalSets = log.totalSetsCompleted.coerceAtLeast(4)
            val routine = workoutRoutines.find {
                it.id == log.routineId ||
                    it.title.equals(log.routineName, ignoreCase = true) ||
                    log.routineName.contains(it.title, ignoreCase = true)
            }
            val routineExercises = allExercises.filter { it.routineId == (routine?.id ?: log.routineId) }

            val matchedGroups = mutableSetOf<MuscleFocusGroup>()

            // Check exercises of that routine
            routineExercises.forEach { ex ->
                classifyMuscleFocusGroups("${ex.muscleGroup} ${ex.exerciseName}").forEach {
                    matchedGroups.add(it)
                }
            }

            // Also check routine title, targetMuscleGroups and log routineName/notes
            val textContext = "${routine?.title.orEmpty()} ${routine?.targetMuscleGroups.orEmpty()} ${log.routineName} ${log.notes}"
            classifyMuscleFocusGroups(textContext).forEach {
                matchedGroups.add(it)
            }

            if (matchedGroups.isEmpty()) {
                // Full body / general workout distributes across main groups
                result[MuscleFocusGroup.CORE] = (result[MuscleFocusGroup.CORE] ?: 0) + (totalSets / 2).coerceAtLeast(2)
            } else {
                val setsPerGroup = (totalSets / matchedGroups.size).coerceAtLeast(3)
                matchedGroups.forEach { group ->
                    result[group] = (result[group] ?: 0) + setsPerGroup
                }
            }
        }

        return result
    }

    fun classifyMuscleFocusGroups(rawText: String): Set<MuscleFocusGroup> {
        val lower = rawText.lowercase(Locale.ROOT)
        val groups = mutableSetOf<MuscleFocusGroup>()

        if (lower.contains("peit") || lower.contains("supino") || lower.contains("crucifixo") || lower.contains("cross") || lower.contains("chest") || lower.contains("push")) {
            groups.add(MuscleFocusGroup.CHEST)
        }
        if (lower.contains("costa") || lower.contains("dorsal") || lower.contains("puxada") || lower.contains("remada") || lower.contains("barra fixa") || lower.contains("pull") || lower.contains("lombar")) {
            groups.add(MuscleFocusGroup.BACK)
        }
        if (lower.contains("perna") || lower.contains("quadr") || lower.contains("agachamento") || lower.contains("leg") || lower.contains("glút") || lower.contains("glut") || lower.contains("posterior") || lower.contains("stiff") || lower.contains("panturrilha") || lower.contains("extensora") || lower.contains("flexora") || lower.contains("inferior")) {
            groups.add(MuscleFocusGroup.LEGS)
        }
        if (lower.contains("ombro") || lower.contains("deltoide") || lower.contains("desenvolvimento") || lower.contains("elevação lateral") || lower.contains("elevacao lateral") || lower.contains("trapézio") || lower.contains("trapezio") || lower.contains("face pull")) {
            groups.add(MuscleFocusGroup.SHOULDERS)
        }
        if (lower.contains("bíceps") || lower.contains("biceps") || lower.contains("tríceps") || lower.contains("triceps") || lower.contains("rosca") || lower.contains("braço") || lower.contains("braco") || lower.contains("antebraço") || lower.contains("pulley") || lower.contains("testa") || lower.contains("francês")) {
            groups.add(MuscleFocusGroup.ARMS)
        }
        if (lower.contains("abd") || lower.contains("core") || lower.contains("prancha") || lower.contains("infra") || lower.contains("oblíquo") || lower.contains("obliquo") || lower.contains("full body") || lower.contains("corpo inteiro")) {
            groups.add(MuscleFocusGroup.CORE)
        }
        return groups
    }

    private fun normalizeDateKey(dateString: String, dateMillis: Long, formatter: SimpleDateFormat): String {
        val trimmed = dateString.trim()
        if (trimmed.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            return trimmed
        }
        if (dateMillis > 0L) {
            return formatter.format(Date(dateMillis))
        }
        return trimmed
    }

    fun calculateMaxConsecutiveDays(dateKeys: Set<String>): Int {
        if (dateKeys.isEmpty()) return 0
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sortedDayIndices = dateKeys.mapNotNull { key ->
            runCatching {
                val parsed = parser.parse(key) ?: return@mapNotNull null
                val cal = Calendar.getInstance().apply {
                    time = parsed
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                (cal.timeInMillis / 86_400_000L).toInt()
            }.getOrNull()
        }.distinct().sorted()

        if (sortedDayIndices.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1
        for (i in 1 until sortedDayIndices.size) {
            if (sortedDayIndices[i] == sortedDayIndices[i - 1] + 1) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else {
                currentStreak = 1
            }
        }
        return maxStreak
    }
}
