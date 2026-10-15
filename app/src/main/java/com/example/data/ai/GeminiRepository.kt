package com.example.data.ai

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.CancellationException

/** Firebase AI Logic manages access without shipping a Gemini REST secret. */
object GeminiRepository {
    private val model by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-3.8-flash")
    }

    suspend fun getWorkoutAdvice(prompt: String): String {
        if (prompt.isBlank()) return "Informe os dados do treino."
        return try {
            model.generateContent(
                "Você é um treinador de corrida e ciclismo. Dê um conselho curto e motivador para este treino: ${prompt.take(4000)}"
            ).text?.takeIf { it.isNotBlank() } ?: "Nenhum conselho disponível no momento."
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            "Não foi possível consultar o treinador IA. Verifique a conexão e tente novamente."
        }
    }
}
