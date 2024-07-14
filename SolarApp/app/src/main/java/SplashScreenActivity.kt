package com.example.solarapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logoImageView = findViewById<ImageView>(R.id.logo)
        startExplodeAnim(logoImageView) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startExplodeAnim(view: View, endAction: () -> Unit) {
        shrink(view, endAction)
    }

    private fun shrink(view: View, endAction: () -> Unit) {
        view.animate()
            .scaleX(SHRINK_VALUE)
            .scaleY(SHRINK_VALUE)
            .alpha(SHRINK_ALPHA)
            .setDuration(SHRINK_DURATION)
            .withEndAction { explode(view, endAction) }
            .start()
    }

    private fun explode(view: View, endAction: () -> Unit) {
        view.animate()
            .scaleX(EXPLODE_VALUE)
            .scaleY(EXPLODE_VALUE)
            .alpha(EXPLODE_ALPHA)
            .setDuration(EXPLODE_DURATION)
            .withEndAction { endAction() }
            .start()
    }

    companion object {
        private const val SHRINK_DURATION = 1450L // Tempo de encolhimento em milissegundos
        private const val EXPLODE_DURATION = 450L // Tempo de explosão em milissegundos
        private const val SHRINK_VALUE = 0.5f // Valor de escala durante o encolhimento
        private const val EXPLODE_VALUE = 3f // Valor de escala durante a explosão
        private const val SHRINK_ALPHA = 0.5f // Valor de opacidade durante o encolhimento
        private const val EXPLODE_ALPHA = 0f // Valor de opacidade durante a explosão
    }
}
