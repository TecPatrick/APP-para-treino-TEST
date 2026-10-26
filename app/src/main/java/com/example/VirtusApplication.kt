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
        // App Check is currently disabled to prevent reCAPTCHA challenges during development.
        // initFirebaseAppCheck()

        try {
            com.example.util.AuthLogger.attachAuthStateListener()
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao inicializar AuthLogger", e)
        }

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
        AppCheckInitializer.initialize()
    }

    companion object {
        private const val TAG = "VirtusApplication"
    }
}

typealias VirtusApplication = MainApplication
