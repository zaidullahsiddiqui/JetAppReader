package com.example.jetappreader

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.jetappreader.navigation.ReaderNavigation
import com.example.jetappreader.ui.theme.JetAppReaderTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Firebase initialization check failed: ${e.message}")
            try {
                if (FirebaseApp.getApps(this).isEmpty()) {
                    val options = FirebaseOptions.Builder()
                        .setApiKey("AIzaSyDPw2KyY-YEcuGcK_s38nCVmPCSR5vqwZ8")
                        .setApplicationId("1:416752737789:android:dd444a57c93feb8be48d94")
                        .setProjectId("jetappreader-231b7")
                        .setStorageBucket("jetappreader-231b7.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            } catch (fallbackException: Exception) {
                Log.e("MainActivity", "Fallback Firebase initialization failed: ${fallbackException.message}")
            }
        }

        enableEdgeToEdge()
        setContent {
            JetAppReaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    ReaderNavigation()
                }
            }
        }
    }
}
