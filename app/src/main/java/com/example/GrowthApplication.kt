package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.GrowthDatabase
import com.example.data.repository.GrowthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

open class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GrowthRepository(database.appDao()) }

    override fun onCreate() {
        super.onCreate()
        com.example.util.ExerciseGifMapper.init(this)
    }
}

typealias GrowthApplication = MainApplication
