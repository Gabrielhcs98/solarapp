package com.example.solarapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        // Simulate loading time
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val intent =
                Intent(
                    this@SplashScreenActivity,
                    MainActivity::class.java
                )
            startActivity(intent)
            finish()
        }, 2000) // 2 seconds delay
    }
}