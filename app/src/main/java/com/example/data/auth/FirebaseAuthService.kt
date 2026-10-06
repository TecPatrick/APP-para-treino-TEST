package com.example.data.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

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
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val user = result.user
            
            if (user != null && displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            Result.success(user)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("operation is not allowed", ignoreCase = true) || msg.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true)) {
                // Email/Password provider is disabled in Firebase console, fallback to local storage
                Result.success(null)
            } else {
                Result.failure(Exception(translateAuthError(e)))
            }
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(result.user)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            if (msg.contains("operation is not allowed", ignoreCase = true) || msg.contains("ERROR_OPERATION_NOT_ALLOWED", ignoreCase = true)) {
                // Fallback to local profile check
                Result.success(null)
            } else {
                Result.failure(Exception(translateAuthError(e)))
            }
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e)))
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = auth.currentUser
            if (user != null) {
                user.delete().await()
            }
            Result.success(Unit)
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
            return when (e) {
                is FirebaseAuthWeakPasswordException ->
                    "Senha muito fraca. De acordo com as diretrizes de segurança, utilize ao menos 8 dígitos com letras maiúsculas, minúsculas, números e símbolos."
                is FirebaseAuthInvalidCredentialsException ->
                    "Credenciais inválidas. Verifique se o e-mail está correto e se a senha confere."
                is FirebaseAuthUserCollisionException ->
                    "Este e-mail já está cadastrado em nosso sistema. Faça login ou solicite a recuperação de senha."
                is FirebaseAuthInvalidUserException ->
                    "Conta não encontrada ou desativada. Verifique o e-mail digitado ou realize um novo cadastro."
                is FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_INVALID_EMAIL" -> "O formato do e-mail inserido é inválido."
                        "ERROR_WRONG_PASSWORD" -> "Senha incorreta. Tente novamente."
                        "ERROR_USER_NOT_FOUND" -> "Nenhum atleta cadastrado com este e-mail."
                        "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas sem sucesso. Por segurança e proteção de dados (LGPD), aguarde alguns instantes."
                        "ERROR_NETWORK_REQUEST_FAILED" -> "Falha de conexão com a rede. Verifique seu Wi-Fi/dados móveis."
                        "ERROR_OPERATION_NOT_ALLOWED" -> "Autenticação via Google Sign-In disponível. Use o botão Entrar com Google."
                        else -> e.localizedMessage ?: "Erro na autenticação. Verifique os dados e tente novamente."
                    }
                }
                else -> {
                    val msg = e.message.orEmpty()
                    if (msg.contains("network", ignoreCase = true) || msg.contains("connection", ignoreCase = true)) {
                        "Sem conexão com a internet. Verifique sua rede para autenticar no Firebase."
                    } else if (msg.contains("operation is not allowed", ignoreCase = true)) {
                        "Autenticação via Google Sign-In disponível. Use o botão Entrar com Google."
                    } else {
                        msg.ifBlank { "Erro inesperado ao processar autenticação. Tente novamente." }
                    }
                }
            }
        }
    }
}
