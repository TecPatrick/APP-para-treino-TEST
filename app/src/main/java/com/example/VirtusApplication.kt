package com.example

import android.app.Application
import android.os.StrictMode
import com.example.data.local.AppDatabase
import com.example.data.repository.VirtusRepository
import com.example.util.AppLog
import com.example.util.AuthLogger
import com.example.util.ExerciseGifMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class principal do aplicativo Virtus Growth.
 * Gerencia o ciclo de vida global, política de StrictMode em debug e
 * inicialização assíncrona fora da Main Thread para otimizar o cold start.
 */
open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        VirtusRepository(database.appDao(), database::clearAllTables)
    }
    val sharedState by lazy {
        com.example.ui.VirtusSharedState(this, applicationScope, repository)
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        // Inicializações de I/O, JSON de exercícios e listener de Auth executadas fora da Main Thread
        applicationScope.launch(Dispatchers.IO) {
            try {
                AuthLogger.attachAuthStateListener()
            } catch (e: Exception) {
                AppLog.e(TAG, "Erro ao inicializar AuthLogger", e)
            }

            try {
                ExerciseGifMapper.init(this@MainApplication)
            } catch (e: Exception) {
                AppLog.e(TAG, "Erro ao inicializar ExerciseGifMapper", e)
            }
        }
    }

    /**
     * Configura e instala o provedor do Firebase App Check.
     */
    private fun initFirebaseAppCheck() {
        AppCheckInitializer.initialize()
    }

    companion object {
        private const val TAG = "VirtusApplication"
    }
}

typealias VirtusApplication = MainApplication
