package com.example

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory

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
            Log.d(TAG, "Firebase App Check initialized successfully with ${if (BuildConfig.DEBUG) "Debug" else "Play Integrity"} provider.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
        }
    }
}
