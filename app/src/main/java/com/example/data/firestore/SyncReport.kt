package com.example.data.firestore

import kotlinx.coroutines.CancellationException

class PendingCloudWriteException : Exception("Envio aguardando confirmação da nuvem.")

data class SyncReport(
    val successful: Int = 0,
    val pending: Int = 0,
    val failed: Int = 0,
    val skipped: Int = 0,
    val updatedFieldsCount: Int = 0
) {
    fun message(): String = shortMessage()

    /**
     * Mensagem simples e amigável para o toast/barra principal de sucesso.
     */
    fun shortMessage(): String {
        return when {
            failed > 0 -> "Sincronização parcial: $successful enviados, $failed falharam."
            pending > 0 && successful == 0 -> "Salvo localmente! Será enviado assim que conectar à internet."
            successful == 0 && skipped > 0 -> "Tudo já está atualizado na nuvem! ⚡"
            else -> "Sincronização concluída com sucesso! ✅"
        }
    }

    /**
     * Resumo técnico detalhado exibido ao tocar no ícone de informações do Delta Sync.
     */
    fun detailedDeltaSummary(): String {
        return "$successful enviados (modificados) • $skipped ignorados (já atualizados) • $updatedFieldsCount campos de perfil alterados" +
            if (pending > 0 || failed > 0) " • $pending pendentes • $failed falhas" else ""
    }
}

internal suspend fun collectSyncResults(operations: List<suspend () -> Result<Unit>>): SyncReport {
    var successful = 0
    var pending = 0
    var failed = 0
    for (operation in operations) {
        val result = try {
            operation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
        when {
            result.isSuccess -> successful++
            result.exceptionOrNull() is PendingCloudWriteException -> pending++
            else -> failed++
        }
    }
    return SyncReport(successful = successful, pending = pending, failed = failed)
}
