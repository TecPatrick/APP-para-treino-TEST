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
        com.example.util.ExerciseGifMapper.init(this)
    }
}

typealias VirtusApplication = MainApplication
