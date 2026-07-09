package com.example.mastg_demo

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // --- START THREAD EXECUTION HERE ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            // 1. Launch a coroutine block bound to the Activity lifecycle
            lifecycleScope.launch {
                Log.d("MASTG-TEST", "Spawning background thread...")

                // 2. Dispatchers.IO forces the demo code to run safely on a background thread.
                // This prevents CountDownLatch from freezing your main application UI.
                val testOutput = withContext(Dispatchers.IO) {
                    val demo = MastgTest(this@MainActivity)
                    demo.mastgTest() // Calls the original, unmodified function
                }

                // 3. Once the background latch releases, execution falls back here to the Main thread
                Log.d("MASTG-TEST", "--- FINAL DEMO LOG OUTPUT ---")
                Log.d("MASTG-TEST", testOutput)
            }

        } else {
            Log.e("MASTG-TEST", "Device API version must be Android 11 (API 30) or higher to run this biometric test.")
        }
    }
}
