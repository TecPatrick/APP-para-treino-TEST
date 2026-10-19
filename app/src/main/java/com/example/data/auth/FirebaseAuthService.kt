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
import kotlinx.coroutines.CancellationException
import java.util.Locale

class FirebaseAuthService(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null

    private fun normalizeEmail(email: String): String =
        email.trim().lowercase(Locale.ROOT)

    private fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Digite um e-mail válido."
        email.length > 254 -> "Digite um e-mail válido."
        !EMAIL_REGEX.matches(email) -> "Digite um e-mail válido."
        else -> null
    }

    private fun validatePassword(password: String, registering: Boolean): String? = when {
        password.isBlank() || password.length > 128 -> "E-mail ou senha inválidos."
        registering && password.length < 8 ->
            "A senha deve ter pelo menos 8 caracteres."
        else -> null
    }

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

    suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<FirebaseUser> {
        val normalizedEmail = normalizeEmail(email)
        validateEmail(normalizedEmail)?.let { return Result.failure(IllegalArgumentException(it)) }
        validatePassword(password, registering = true)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (displayName.trim().length !in 1..100) {
            return Result.failure(IllegalArgumentException("Informe um nome válido."))
        }
        return try {
            val result = auth.createUserWithEmailAndPassword(normalizedEmail, password).await()
            val user = requireNotNull(result.user) { "O Firebase não retornou um usuário autenticado." }
            
            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }
            Result.success(user)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e), e))
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        val normalizedEmail = normalizeEmail(email)
        validateEmail(normalizedEmail)?.let { return Result.failure(IllegalArgumentException(it)) }
        validatePassword(password, registering = false)?.let { return Result.failure(IllegalArgumentException(it)) }
        return try {
            val result = auth.signInWithEmailAndPassword(normalizedEmail, password).await()
            Result.success(requireNotNull(result.user) { "O Firebase não retornou um usuário autenticado." })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(Exception(translateAuthError(e), e))
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        val normalizedEmail = normalizeEmail(email)
        validateEmail(normalizedEmail)?.let { return Result.failure(IllegalArgumentException(it)) }
        return try {
            auth.sendPasswordResetEmail(normalizedEmail).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (_: FirebaseAuthInvalidUserException) {
            // Do not disclose whether an account exists for this address.
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Não foi possível enviar a recuperação agora. Tente novamente mais tarde.", e))
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
        private val EMAIL_REGEX = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+[.][A-Z]{2,63}$", RegexOption.IGNORE_CASE)

        fun translateAuthError(e: Exception): String {
            val message = e.message.orEmpty().lowercase(Locale.ROOT)
            return when (e) {
                is FirebaseAuthWeakPasswordException ->
                    "A senha deve ter pelo menos 8 caracteres."
                is FirebaseAuthUserCollisionException ->
                    "Não foi possível criar a conta. Tente entrar ou recuperar a senha."
                is FirebaseAuthInvalidCredentialsException,
                is FirebaseAuthInvalidUserException ->
                    "E-mail ou senha inválidos."
                is FirebaseAuthException -> when (e.errorCode) {
                    "ERROR_TOO_MANY_REQUESTS" ->
                        "Muitas tentativas. Aguarde alguns minutos e tente novamente."
                    "ERROR_NETWORK_REQUEST_FAILED" ->
                        "Sem conexão segura com o serviço de autenticação."
                    else ->
                        "Não foi possível autenticar. Tente novamente."
                }
                else -> when {
                    message.contains("recaptcha") ->
                        "A verificação de segurança falhou. Tente novamente."
                    message.contains("network") ->
                        "Sem conexão segura com o serviço de autenticação."
                    else ->
                        "Não foi possível autenticar. Tente novamente."
                }
            }
        }
    }
}

