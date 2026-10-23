package com.example.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CancellationException

/** Firebase AI Logic manages access without shipping a Gemini REST secret. */
object GeminiRepository {
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-1.5-flash")
    }

    suspend fun getWorkoutAdvice(prompt: String): String {
        if (prompt.isBlank()) return "Informe os dados do treino."
        return try {
            model.generateContent(
                "Você é um treinador de bodybuilding. Dê um conselho curto, técnico e motivador para este treino: ${prompt.take(4000)}"
            ).text?.takeIf { it.isNotBlank() } ?: "Nenhum conselho disponível no momento."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            "Não foi possível consultar o treinador IA. Verifique a conexão e tente novamente."
        }
    }

    suspend fun generateDietPlan(
        calories: Int,
        protein: Int,
        carbs: Int,
        fat: Int,
        mealsCount: Int,
        restrictions: String,
        preferences: String,
        availableFoods: String
    ): String? {
        val prompt = """
            Você é um Nutricionista Esportivo especialista em dietas flexíveis e bodybuilding.
            Crie um plano alimentar completo para um usuário com as seguintes metas diárias:
            - Calorias: $calories kcal
            - Proteína: $protein g
            - Carboidratos: $carbs g
            - Gordura: $fat g
            
            Configurações:
            - Número de refeições: $mealsCount
            - Restrições: $restrictions
            - Preferências: $preferences
            
            Base de Alimentos sugerida (use preferencialmente estes ou similares):
            $availableFoods
            
            Retorne APENAS um JSON no seguinte formato (sem blocos de código Markdown, apenas o texto JSON puro):
            [
              {
                "mealType": "BREAKFAST",
                "foodName": "Ovo Cozido",
                "amount": 150.0,
                "unit": "g",
                "calories": 232.0,
                "protein": 19.5,
                "carbs": 1.6,
                "fat": 15.9,
                "notes": "Rico em colina"
              },
              ...
            ]
            
            Tipos de refeição válidos: BREAKFAST, MORNING_SNACK, LUNCH, PRE_WORKOUT, POST_WORKOUT, DINNER, SUPPER.
            Certifique-se que a SOMA de todos os itens aproxime-se das metas de macros fornecidas.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text?.trim()?.removeSurrounding("```json", "```")?.trim()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    suspend fun suggestMealItems(
        mealType: String,
        targetCalories: Int,
        restrictions: String,
        preferences: String,
        availableFoods: String
    ): String? {
        val prompt = """
            Sugira 2 ou 3 alimentos para a refeição: $mealType.
            Alvo calórico da refeição: $targetCalories kcal.
            Restrições: $restrictions.
            Preferências: $preferences.
            
            Base de Alimentos: $availableFoods
            
            Retorne APENAS um JSON (formato lista de objetos) com:
            foodName, amount, unit, calories, protein, carbs, fat, notes.
            Sem markdown.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            response.text?.trim()?.removeSurrounding("```json", "```")?.trim()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
