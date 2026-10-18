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
     * Provides a conservative hydration starting point. It is intentionally capped
     * and remains user-editable because sweat, climate and medical conditions vary.
     */
    fun calculateWaterIntakeMl(
        weightKg: Float,
        age: Int,
        isAthleteOrWarmClimate: Boolean = true
    ): Int {
        // Using total body weight without a ceiling can greatly overestimate hydration
        // for people with a high BMI. Use an adjusted reference weight instead.
        val referenceWeight = weightKg.coerceIn(45f, 100f)
        val ageBase = when {
            age <= 17 -> 35f
            age <= 55 -> 30f
            age <= 65 -> 28f
            else -> 25f
        }
        val activityExtra = if (isAthleteOrWarmClimate) 500 else 0
        return ((referenceWeight * ageBase).roundToInt() + activityExtra).coerceIn(1500, 5000)
    }

    /**
     * Weight used only as a reference for macro calculations. For high BMI values,
     * adjusted body weight avoids prescribing macros from total body weight alone.
     */
    fun calculateMacroReferenceWeight(weightKg: Float, heightCm: Float): Float {
        val heightMeters = (heightCm / 100f).coerceAtLeast(0.5f)
        val bmi25Weight = 25f * heightMeters * heightMeters
        return if (weightKg > bmi25Weight) {
            bmi25Weight + 0.25f * (weightKg - bmi25Weight)
        } else {
            weightKg
        }.coerceAtLeast(35f)
    }

    data class CalculatedDietPlan(
        val bmr: Int,
        val tdee: Int,
        val targetCalories: Int,
        val proteinGrams: Int,
        val carbsGrams: Int,
        val fatGrams: Int,
        val waterMl: Int,
        val macroReferenceWeightKg: Float,
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
        isAthleteOrWarmClimate: Boolean = true,
        customTargetCalories: Int? = null
    ): CalculatedDietPlan {
        val bmr = calculateBMR(weightKg, heightCm, age, gender)
        // Kept as an optional theoretical estimate. It is not used as the food goal.
        val tdee = calculateTDEE(bmr, activityLevel)

        // Food target is independent from estimated activity. Recorded exercise is
        // added to daily expenditure by VirtusViewModel, never to this target.
        val targetCalories = (customTargetCalories ?: (bmr + goal.calorieAdjustment))
            .coerceIn(1200, 6000)

        // Body type is retained in the profile for compatibility, but it no longer
        // changes calories or macros: somatotypes are not a clinical calculation.
        @Suppress("UNUSED_VARIABLE") val ignoredBodyType = biotype
        val macroReferenceWeight = calculateMacroReferenceWeight(weightKg, heightCm)
        val proteinGrams = (macroReferenceWeight * customProteinGPerKg).roundToInt()
        val fatGrams = (macroReferenceWeight * customFatGPerKg).roundToInt()

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
            carbsGPerKg = (carbsGrams / macroReferenceWeight),
            fatGPerKg = customFatGPerKg,
            macroReferenceWeightKg = macroReferenceWeight,
            highCarbCalories = highCarbCals,
            highCarbGrams = highCarbGrams,
            lowCarbCalories = lowCarbCals,
            lowCarbGrams = lowCarbGrams
        )
    }
}
