package com.example.util

import com.example.data.model.ActivityLevel
import com.example.data.model.BodyType
import com.example.data.model.FitnessGoal
import com.example.data.model.Gender
import kotlin.math.roundToInt

object MacroCalculator {

    /**
     * Calculates Basal Metabolic Rate (BMR/TMB) using the Mifflin-St Jeor Equation
     */
    fun calculateBMR(weightKg: Float, heightCm: Float, age: Int, gender: Gender): Int {
        val base = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
        val bmr = if (gender == Gender.MASCULINO) base + 5 else base - 161
        return bmr.roundToInt().coerceAtLeast(1000)
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE/GET)
     */
    fun calculateTDEE(bmr: Int, activityLevel: ActivityLevel): Int {
        return (bmr * activityLevel.multiplier).roundToInt()
    }

    /**
     * Calculates precise water intake based on exact age rules & physical activity:
     * - Jovens até 17 anos: 40 ml por kg.
     * - Adultos (18 a 55 anos): 35 ml por kg.
     * - Adultos (55 a 65 anos): 30 ml por kg.
     * - Idosos (66 anos ou mais): 25 ml por kg.
     * - Atividades físicas / Atletas ou climas quentes: 45 ml por kg (40-50 ml/kg).
     */
    fun calculateWaterIntakeMl(
        weightKg: Float,
        age: Int,
        isAthleteOrWarmClimate: Boolean = true
    ): Int {
        val mlPerKg = if (isAthleteOrWarmClimate) {
            45f // 40 to 50 ml/kg recommendation for athletes / training
        } else {
            when {
                age <= 17 -> 40f
                age in 18..55 -> 35f
                age in 56..65 -> 30f
                else -> 25f // 66 years or older
            }
        }
        return (weightKg * mlPerKg).roundToInt().coerceIn(1500, 7000)
    }

    data class CalculatedDietPlan(
        val bmr: Int,
        val tdee: Int,
        val targetCalories: Int,
        val proteinGrams: Int,
        val carbsGrams: Int,
        val fatGrams: Int,
        val waterMl: Int,
        val proteinGPerKg: Float,
        val carbsGPerKg: Float,
        val fatGPerKg: Float,
        val highCarbCalories: Int = 0,
        val highCarbGrams: Int = 0,
        val lowCarbCalories: Int = 0,
        val lowCarbGrams: Int = 0
    )

    fun computeFullPlan(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        gender: Gender,
        activityLevel: ActivityLevel,
        goal: FitnessGoal,
        biotype: BodyType = BodyType.MESOMORPH,
        customProteinGPerKg: Float = 2.2f,
        customFatGPerKg: Float = 0.8f,
        isAthleteOrWarmClimate: Boolean = true
    ): CalculatedDietPlan {
        val bmr = calculateBMR(weightKg, heightCm, age, gender)
        val tdee = calculateTDEE(bmr, activityLevel)
        
        // Calorie adjustment based on goal and biotype
        val biotypeCalorieDelta = when (biotype) {
            BodyType.ECTOMORPH -> 150 // Ectomorphs need a bit more calories to grow
            BodyType.MESOMORPH -> 0
            BodyType.ENDOMORPH -> -100 // Endomorphs need tighter caloric control
        }

        val targetCalories = (tdee + goal.calorieAdjustment + biotypeCalorieDelta).coerceAtLeast(1200)

        val proteinGrams = (weightKg * customProteinGPerKg).roundToInt()
        val fatGrams = (weightKg * customFatGPerKg).roundToInt()

        val proteinCalories = proteinGrams * 4
        val fatCalories = fatGrams * 9
        val remainingCaloriesForCarbs = (targetCalories - proteinCalories - fatCalories).coerceAtLeast(200)
        val carbsGrams = (remainingCaloriesForCarbs / 4f).roundToInt()

        val waterMl = calculateWaterIntakeMl(weightKg, age, isAthleteOrWarmClimate)

        // Carb cycling calculations
        val highCarbCals = targetCalories + 300
        val highCarbGrams = carbsGrams + 75
        val lowCarbCals = targetCalories - 300
        val lowCarbGrams = (carbsGrams - 75).coerceAtLeast(50)

        return CalculatedDietPlan(
            bmr = bmr,
            tdee = tdee,
            targetCalories = targetCalories,
            proteinGrams = proteinGrams,
            carbsGrams = carbsGrams,
            fatGrams = fatGrams,
            waterMl = waterMl,
            proteinGPerKg = customProteinGPerKg,
            carbsGPerKg = (carbsGrams / weightKg),
            fatGPerKg = customFatGPerKg,
            highCarbCalories = highCarbCals,
            highCarbGrams = highCarbGrams,
            lowCarbCalories = lowCarbCals,
            lowCarbGrams = lowCarbGrams
        )
    }
}
