package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender { MASCULINO, FEMININO }

enum class BodyType(
    val title: String,
    val subtitle: String,
    val description: String,
    val metabolismTrait: String,
    val dietStrategy: String,
    val carbRatio: String
) {
    ECTOMORPH(
        title = "Ectomorfo",
        subtitle = "Estrutura óssea fina, metabolismo acelerado",
        description = "Dificuldade para ganhar peso e massa muscular. Queima calorias rapidamente, membros longos e baixo percentual de gordura natural.",
        metabolismTrait = "Metabolismo Ultra-Rápido",
        dietStrategy = "Superávit calórico alto (+400 a 600 kcal), alto teor de carboidratos complexos e densidade energética.",
        carbRatio = "55% Carbos • 25% Proteína • 20% Gordura"
    ),
    MESOMORPH(
        title = "Mesomorfo",
        subtitle = "Estrutura atlética, ombros largos e cintura fina",
        description = "Facilidade para hipertrofia muscular e queima de gordura. Boa resposta a treinos intensos e equilíbrio hormonal favorável.",
        metabolismTrait = "Metabolismo Eficiente e Equilibrado",
        dietStrategy = "Calorias moderadas, proporção balanceada de macronutrientes com foco em alta proteína para sustentação.",
        carbRatio = "45% Carbos • 30% Proteína • 25% Gordura"
    ),
    ENDOMORPH(
        title = "Endomorfo",
        subtitle = "Estrutura óssea larga, metabolismo mais lento",
        description = "Tendência a acumular gordura corporal com facilidade, mas ganha força e volume muscular rapidamente. Maior sensibilidade a carboidratos simples.",
        metabolismTrait = "Metabolismo Lento / Conservador",
        dietStrategy = "Controle rigoroso de carboidratos, maior proporção de proteínas e gorduras boas, déficit calórico controlado.",
        carbRatio = "35% Carbos • 35% Proteína • 30% Gordura"
    )
}

enum class ActivityLevel(val multiplier: Float, val label: String) {
    SEDENTARY(1.2f, "Sedentário (Pouco ou nenhum exercício)"),
    LIGHT(1.375f, "Leve (Exercício leve 1-3 dias/sem)"),
    MODERATE(1.55f, "Moderado (Treino 3-5 dias/sem)"),
    VERY_ACTIVE(1.725f, "Muito Ativo (Treino pesado 6-7 dias/sem)"),
    EXTRA_ACTIVE(1.9f, "Extremamente Ativo (Atleta/Trabalho braçal)")
}

enum class FitnessGoal(val label: String, val calorieAdjustment: Int) {
    BULKING_CLEAN("Bulking Limpo (Ganho de Massa)", 350),
    BULKING_AGGRESSIVE("Bulking Agressivo (+Massa Rápido)", 600),
    MAINTENANCE("Manutenção & Recomposição", 0),
    CUTTING_MODERATE("Cutting Moderado (Queima de Gordura)", -450),
    CUTTING_AGGRESSIVE("Cutting Agressivo (Definição Extrema)", -750)
}

enum class DietCycleType(val label: String) {
    FIXED_DAILY("Plano Fixo Diário (Mesmos macros todo dia)"),
    CARB_CYCLING("Ciclo de Carboidratos (Alto / Médio / Baixo)"),
    TRAINING_VS_REST("Dias de Treino vs Descanso (Carbo alto em treino)")
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Atleta",
    val email: String = "atleta@fitmail.com",
    val firebaseUid: String = "",
    val passwordHash: String = "Atleta@2026",
    val isLoggedIn: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
    val lgpdConsentAccepted: Boolean = false,
    val lgpdConsentDate: Long = System.currentTimeMillis(),

    // Biometric & Fitness parameters
    val gender: Gender = Gender.MASCULINO,
    val age: Int = 26,
    val weightKg: Float = 78.5f,
    val heightCm: Float = 178f,
    val biotype: BodyType = BodyType.MESOMORPH,
    val trainingDaysPerWeek: Int = 5,
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val goal: FitnessGoal = FitnessGoal.BULKING_CLEAN,
    val dietCycleType: DietCycleType = DietCycleType.FIXED_DAILY,
    val isAthleteOrWarmClimate: Boolean = true,
    
    // Macro targets
    val targetCalories: Int = 2650,
    val targetProteinGrams: Int = 175,
    val targetCarbsGrams: Int = 320,
    val targetFatGrams: Int = 65,
    val targetWaterMl: Int = 3530, // calculated from age & weight
    
    // Monthly configurations
    val monthlyTargetWeightKg: Float = 81.0f,
    val monthlyTargetDurationWeeks: Int = 4,
    val monthlyNotes: String = "Fase de hipertrofia com foco em peitoral e dorsais."
)
