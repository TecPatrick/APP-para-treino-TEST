package com.example

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Utilitário auxiliar para configuração do Firebase App Check
 * com estratégia de Play Integrity em produção e Debug em desenvolvimento.
 */
object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"

    fun initialize() {
        try {
            val firebaseAppCheck = Firebase.appCheck
            val providerFactory = if (BuildConfig.DEBUG) {
                DebugAppCheckProviderFactory.getInstance()
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            firebaseAppCheck.setTokenAutoRefreshEnabled(true)
            Log.i(TAG, "Firebase App Check configurado com sucesso [${if (BuildConfig.DEBUG) "DebugProvider" else "PlayIntegrity"}].")
        } catch (e: Exception) {
            Log.e(TAG, "Falha ao configurar Firebase App Check", e)
        }
    }
}
