package com.example.data.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CancellationException
import java.util.Locale

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    suspend fun signInWithGoogle(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Falha ao autenticar com o Google.")
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(IllegalStateException("Nenhuma sessão autenticada."))
            user.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (_: Exception) {}
    }

    companion object {
        fun translateAuthError(e: Exception): String {
            val message = e.message.orEmpty().lowercase(Locale.ROOT)
            if (message.contains("malformed") || 
                message.contains("expired") ||
                message.contains("auth_credential_incorrect")
            ) {
                return "Falha na verificação de segurança ou credencial malformada."
            }
            return when (e) {
                is FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Aguarde alguns minutos."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Sem conexão segura com o serviço de autenticação."
                    else -> "Não foi possível autenticar. Tente novamente."
                }
                else -> "Não foi possível autenticar. Tente novamente."
            }
        }
    }
}
