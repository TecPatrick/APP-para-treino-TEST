package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.VirtusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { VirtusRepository(database.appDao()) }

    override fun onCreate() {
        super.onCreate()
        if (com.google.firebase.FirebaseApp.initializeApp(this) != null) {
            AppCheckInitializer.initialize()
        }
        try {
            com.example.util.ExerciseGifMapper.init(this)
        } catch (e: Exception) {
            android.util.Log.e("MainApplication", "Error initializing ExerciseGifMapper", e)
        }
    }
}

typealias VirtusApplication = MainApplication

