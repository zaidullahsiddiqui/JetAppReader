package com.example.jetappreader

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ReaderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initFirebaseSafely()
    }

    private fun initFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val app = FirebaseApp.initializeApp(this)
                if (app == null) {
                    initializeFallbackFirebase()
                }
            }
        } catch (e: Exception) {
            Log.e("ReaderApplication", "Firebase default initialization failed: ${e.message}", e)
            initializeFallbackFirebase()
        }
    }

    private fun initializeFallbackFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDummyKeyForFallback1234567890")
                    .setApplicationId("1:1234567890:android:dummyappid12345")
                    .setProjectId("jetappreader-231b7")
                    .setStorageBucket("jetappreader-231b7.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.i("ReaderApplication", "Firebase initialized with fallback options.")
            }
        } catch (e: Exception) {
            Log.e("ReaderApplication", "Failed to initialize fallback Firebase: ${e.message}", e)
        }
    }
}
