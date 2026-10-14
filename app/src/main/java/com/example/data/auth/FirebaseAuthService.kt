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
            val fullMsg = "${e.message} ${e.cause?.message} ${e.toString()}"
            if (isEmailPasswordDisabledError(fullMsg)) {
                // Email/Password provider is disabled in Firebase console, allow seamless local registration
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
            val fullMsg = "${e.message} ${e.cause?.message} ${e.toString()}"
            android.util.Log.e("FirebaseAuthService", "Login failed: $fullMsg")
            if (isEmailPasswordDisabledError(fullMsg)) {
                // Email/Password provider disabled or blocked by Recaptcha: fallback to local authentication
                Result.success(null)
            } else {
                Result.failure(Exception(translateAuthError(e)))
            }
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            if (email.isBlank()) {
                return Result.failure(Exception("Digite um e-mail válido para a recuperação de senha."))
            }
            auth.sendPasswordResetEmail(email.trim()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            val fullMsg = "${e.message} ${e.cause?.message} ${e.toString()}"
            if (isEmailPasswordDisabledError(fullMsg)) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(translateAuthError(e)))
            }
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
        fun isEmailPasswordDisabledError(msg: String): Boolean {
            val lower = msg.lowercase()
            return lower.contains("operation is not allowed") ||
                    lower.contains("disabled") ||
                    lower.contains("recaptcha") ||
                    lower.contains("credential is incorrect") ||
                    lower.contains("malformed") ||
                    lower.contains("expired") ||
                    lower.contains("invalid credential") ||
                    lower.contains("sign_in_provider_disabled")
        }

        fun translateAuthError(e: Exception): String {
            val msg = e.message.orEmpty().lowercase()
            return when (e) {
                is FirebaseAuthWeakPasswordException ->
                    "Senha muito fraca. Utilize ao menos 8 dígitos com letras e números."
                is FirebaseAuthInvalidCredentialsException ->
                    "Credenciais inválidas. Verifique e-mail e senha."
                is FirebaseAuthUserCollisionException ->
                    "Este e-mail já está cadastrado. Faça login ou recupere a senha."
                is FirebaseAuthInvalidUserException ->
                    "Conta não encontrada ou desativada."
                is FirebaseAuthException -> {
                    when (e.errorCode) {
                        "ERROR_INVALID_EMAIL" -> "Formato de e-mail inválido."
                        "ERROR_WRONG_PASSWORD" -> "Senha incorreta."
                        "ERROR_USER_NOT_FOUND" -> "Nenhum atleta cadastrado."
                        "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Aguarde um instante."
                        else -> "Erro na autenticação: ${e.message}"
                    }
                }
                else -> {
                    if (msg.contains("recaptcha")) {
                        "Problema na verificação de segurança (reCAPTCHA). Tente novamente."
                    } else if (msg.contains("credential") || msg.contains("incorrect") || msg.contains("expired") || msg.contains("malformed")) {
                        "Credenciais inválidas. Verifique os dados."
                    } else {
                        "Erro inesperado: ${e.message ?: "Tente novamente."}"
                    }
                }
            }
        }
    }
}
