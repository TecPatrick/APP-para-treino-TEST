package com.example

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Utilitário auxiliar para inicialização e diagnóstico do Firebase App Check
 * com estratégia de Play Integrity em produção e Debug Provider em desenvolvimento.
 */
object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"

    fun initialize() {
        try {
            val firebaseAppCheck = Firebase.appCheck
            val providerFactory = if (BuildConfig.DEBUG) {
                Log.d(TAG, "🛡️ [APP_CHECK] Inicializando com DebugAppCheckProviderFactory...")
                DebugAppCheckProviderFactory.getInstance()
            } else {
                Log.i(TAG, "🛡️ [APP_CHECK] Inicializando com PlayIntegrityAppCheckProviderFactory...")
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            firebaseAppCheck.setTokenAutoRefreshEnabled(true)
            Log.i(TAG, "✅ [APP_CHECK] Configurado com sucesso [${if (BuildConfig.DEBUG) "DebugProvider" else "PlayIntegrity"}].")
        } catch (e: Exception) {
            Log.e(TAG, "❌ [APP_CHECK] Falha ao configurar Firebase App Check", e)
        }
    }
}
