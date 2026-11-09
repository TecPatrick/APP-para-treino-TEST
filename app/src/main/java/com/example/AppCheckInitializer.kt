package com.example

import com.example.util.AppLog
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck

/**
 * Utilitário auxiliar para inicialização do Firebase App Check
 * com estratégia de Play Integrity em produção e Debug Provider em desenvolvimento.
 */
object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"

    fun initialize() {
        try {
            val firebaseAppCheck = Firebase.appCheck
            val providerFactory = AppCheckProvider.factory()
            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            firebaseAppCheck.setTokenAutoRefreshEnabled(true)
            AppLog.i(
                TAG,
                "✅ [APP_CHECK] Configurado com sucesso [${if (BuildConfig.DEBUG) "DebugProvider" else "PlayIntegrity"}]."
            )
        } catch (e: Exception) {
            AppLog.e(TAG, "❌ [APP_CHECK] Falha ao configurar Firebase App Check", e)
        }
    }
}
