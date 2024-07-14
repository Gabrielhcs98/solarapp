package com.example.solarapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val logoImageView = findViewById<ImageView>(R.id.logo)
        startPulseAnim(logoImageView) {
            startExplodeAnim(logoImageView) {
                navigateToMainActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startPulseAnim(view: View, endAction: () -> Unit) {
        view.animate()
            .scaleX(PULSE_UP_SCALE)
            .scaleY(PULSE_UP_SCALE)
            .setDuration(PULSE_DURATION)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(PULSE_DURATION)
                    .withEndAction {
                        endAction()
                    }
                    .start()
            }
            .start()
    }

    private fun startExplodeAnim(view: View, endAction: () -> Unit) {
        shrink(view, endAction)
    }

    private fun shrink(view: View, endAction: () -> Unit) {
        view.animate()
            .scaleX(SHRINK_VALUE)
            .scaleY(SHRINK_VALUE)
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
        private const val PULSE_DURATION = 500L // Tempo de pulsação em milissegundos
        private const val PULSE_UP_SCALE = 1.5f // Valor de escala durante a pulsação
        private const val SHRINK_DURATION = 1450L // Tempo de encolhimento em milissegundos
        private const val EXPLODE_DURATION = 350L // Tempo de explosão em milissegundos
        private const val SHRINK_VALUE = 0.5f // Valor de escala durante o encolhimento
        private const val EXPLODE_VALUE = 3f // Valor de escala durante a explosão
        private const val EXPLODE_ALPHA = 0f // Valor de opacidade durante a explosão
    }
}
