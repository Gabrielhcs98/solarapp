package com.example.solarapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity de splash screen que exibe uma animação de logo antes de navegar para a MainActivity.
 */
class SplashScreenActivity : AppCompatActivity() {

    /**
     * Chamado quando a activity é criada. Configura o layout e inicia a animação do logo.
     *
     * @param savedInstanceState Se a activity está sendo reinicializada após ser fechada, este parâmetro contém os dados fornecidos mais recentemente; caso contrário, é nulo.
     */
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

    /**
     * Navega para a MainActivity e finaliza a SplashScreenActivity.
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    /**
     * Inicia a animação de pulsação no view fornecido.
     *
     * @param view O view que sofrerá a animação de pulsação.
     * @param endAction A ação a ser executada no final da animação de pulsação.
     */
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

    /**
     * Inicia a animação de explosão no view fornecido.
     *
     * @param view O view que sofrerá a animação de explosão.
     * @param endAction A ação a ser executada no final da animação de explosão.
     */
    private fun startExplodeAnim(view: View, endAction: () -> Unit) {
        shrink(view, endAction)
    }

    /**
     * Inicia a animação de encolhimento no view fornecido.
     *
     * @param view O view que sofrerá a animação de encolhimento.
     * @param endAction A ação a ser executada no final da animação de encolhimento.
     */
    private fun shrink(view: View, endAction: () -> Unit) {
        view.animate()
            .scaleX(SHRINK_VALUE)
            .scaleY(SHRINK_VALUE)
            .setDuration(SHRINK_DURATION)
            .withEndAction { explode(view, endAction) }
            .start()
    }

    /**
     * Inicia a animação de explosão no view fornecido após o encolhimento.
     *
     * @param view O view que sofrerá a animação de explosão.
     * @param endAction A ação a ser executada no final da animação de explosão.
     */
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