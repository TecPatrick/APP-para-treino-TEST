package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Gerencia os fluxos oficiais de autenticação Google via Jetpack Credential Manager
 * seguindo as diretrizes de segurança do Firebase Auth:
 * - 1 única opção por GetCredentialRequest (GetSignInWithGoogleOption interativo vs GetGoogleIdOption silencioso)
 * - Tratamento dedicado para GetCredentialCancellationException
 * - Limpeza completa de credenciais (ClearCredentialStateRequest) no logout
 */
object GoogleCredentialAuthHelper {

    fun resolveWebClientId(context: Context): String {
        return try {
            context.getString(R.string.default_web_client_id).trim()
        } catch (e: Exception) {
            Log.w("Auth", "default_web_client_id não encontrado: ${e.message}")
            ""
        }
    }

    /**
     * Fluxo 1: Auto-login silencioso na abertura do app para contas já autorizadas.
     */
    fun attemptSilentAutoSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onCredentialObtained: (AuthCredential) -> Unit,
        onAlreadyAuthenticated: () -> Unit = {},
        onUnauthenticated: () -> Unit = {}
    ) {
        if (Firebase.auth.currentUser != null) {
            onAlreadyAuthenticated()
            return
        }

        val clientId = resolveWebClientId(context)
        if (clientId.isBlank()) {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    onCredentialObtained(authCredential)
                } else {
                    onUnauthenticated()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Auto sign-in Google cancelado ou dispensado: ${e.message}", e)
                onUnauthenticated()
            } catch (e: Exception) {
                // Normal on clean devices without pre-authorized Google sessions
                onUnauthenticated()
            }
        }
    }

    /**
     * Fluxo 2: Login interativo via botão "Entrar com Conta Google" (GetSignInWithGoogleOption).
     */
    fun launchInteractiveGoogleSignIn(
        context: Context,
        credentialManager: CredentialManager,
        scope: CoroutineScope,
        onCredentialObtained: (AuthCredential) -> Unit,
        onCancelled: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        val clientId = resolveWebClientId(context)
        if (clientId.isBlank()) {
            onError("Configuração Google Sign-In ausente (default_web_client_id). Use Criar Perfil ou Visitante.")
            return
        }

        val activityContext = context as? Activity ?: context
        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activityContext, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    onCredentialObtained(authCredential)
                } else {
                    onError("Tipo de credencial Google inesperado. Tente novamente.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: GetCredentialCancellationException) {
                Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onCancelled()
            } catch (e: Exception) {
                Log.e("Auth", "Google Sign-In failed", e)
                val friendlyMsg = FirebaseAuthService.translateAuthError(e)
                onError(friendlyMsg)
            }
        }
    }

    /**
     * Fluxo 3: Logout completo limpando o estado do FirebaseAuth e do CredentialManager.
     */
    fun clearCredentialManagerState(
        context: Context,
        scope: CoroutineScope,
        onComplete: () -> Unit = {}
    ) {
        try {
            Firebase.auth.signOut()
        } catch (_: Exception) {}

        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("Auth", "Não foi possível limpar estado do CredentialManager: ${e.message}")
            } finally {
                onComplete()
            }
        }
    }
}
