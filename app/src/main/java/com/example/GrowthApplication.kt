package com.example

import android.app.Application
import com.example.data.local.GrowthDatabase
import com.example.data.repository.GrowthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class GrowthApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { GrowthDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GrowthRepository(database.growthDao()) }

    override fun onCreate() {
        super.onCreate()
    }
}
