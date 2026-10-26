package com.example

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

/**
 * Utilitário auxiliar para inicialização e diagnóstico do Firebase App Check
 * com estratégia de Play Integrity em produção e Debug Provider em desenvolvimento.
 *
 * Configuração de Certificados do Projeto:
 * - Package Name: com.aistudio.growthdiet.kxmfzq
 * - Firebase Project ID: gen-lang-client-0338831300
 * - SHA-1: 5C:F2:E9:92:D0:DC:08:2F:95:4A:26:14:55:AE:9A:0F:78:5F:44:D3
 * - SHA-256: 79:67:6D:0E:34:49:21:D3:0C:34:4D:B7:7A:F0:9A:0B:93:0A:BE:DA:36:4D:AF:3F:7D:44:FC:33:F8:B2:94:FF
 */
object AppCheckInitializer {
    private const val TAG = "AppCheckInitializer"

    // Constants for testing and explicit identification
    const val DETERMINISTIC_DEBUG_SECRET = "aistudio-debug-secret-0000-1111"

    fun initialize() {
        try {
            val firebaseAppCheck = Firebase.appCheck
            val providerFactory = if (BuildConfig.DEBUG) {
                Log.d(TAG, "🛡️ [APP_CHECK] Inicializando com DebugAppCheckProviderFactory...")
                Log.d(TAG, "   ├─ Package: ${BuildConfig.APPLICATION_ID}")
                Log.d(TAG, "   └─ Instrução: Em ambiente debug, adicione o Debug Secret emitido no Logcat em Firebase Console -> App Check -> Manage debug tokens")
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
