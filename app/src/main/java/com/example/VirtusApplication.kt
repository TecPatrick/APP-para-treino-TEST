package com.example

import android.app.Application
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.repository.VirtusRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Application class principal do aplicativo Virtus Growth.
 * Gerencia o ciclo de vida global e a inicialização de segurança
 * do Firebase App Check utilizando a estratégia de Play Integrity.
 */
open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        VirtusRepository(database.appDao(), database::clearAllTables)
    }

    override fun onCreate() {
        super.onCreate()
        // Inicialização prioritária do Firebase App Check com Play Integrity
        initFirebaseAppCheck()

        try {
            com.example.util.ExerciseGifMapper.init(this)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar ExerciseGifMapper", e)
        }
    }

    /**
     * Configura e instala o provedor do Firebase App Check.
     * Utiliza PlayIntegrityAppCheckProviderFactory em builds de produção/release
     * e DebugAppCheckProviderFactory em desenvolvimento (debug).
     */
    private fun initFirebaseAppCheck() {
        try {
            // Garante inicialização do Firebase Core
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }

            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            val providerFactory = if (BuildConfig.DEBUG) {
                Log.d(TAG, "Configurando App Check com Provedor de Debug...")
                DebugAppCheckProviderFactory.getInstance()
            } else {
                Log.d(TAG, "Configurando App Check com Estratégia Play Integrity...")
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }

            // Instala o provedor e ativa a renovação automática de tokens
            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            firebaseAppCheck.setTokenAutoRefreshEnabled(true)

            Log.i(TAG, "Firebase App Check inicializado com sucesso [${if (BuildConfig.DEBUG) "DebugProvider" else "PlayIntegrity"}].")
        } catch (e: Exception) {
            Log.e(TAG, "Falha na inicialização do Firebase App Check", e)
        }
    }

    companion object {
        private const val TAG = "VirtusApplication"
    }
}

typealias VirtusApplication = MainApplication
