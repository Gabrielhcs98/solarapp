package com.example.solarapp.util

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.R

/**
 * Utilitário para exibir diálogos personalizados no aplicativo.
 */
object DialogUtils {

    /**
     * Exibe um diálogo de alerta personalizado.
     *
     * @param context O contexto onde o diálogo será exibido.
     * @param title O título do diálogo.
     * @param message A mensagem do diálogo.
     * @param positiveButtonText O texto do botão positivo.
     * @param negativeButtonText O texto do botão negativo (opcional).
     * @param onPositiveClick Função a ser chamada quando o botão positivo for clicado.
     * @param onNegativeClick Função a ser chamada quando o botão negativo for clicado (opcional).
     */
    fun showCustomAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String? = null,
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                onPositiveClick()
                dialog.dismiss()
            }
            .apply {
                if (negativeButtonText != null) {
                    setNegativeButton(negativeButtonText) { dialog, _ ->
                        onNegativeClick?.invoke()
                        dialog.dismiss()
                    }
                }
            }
            .create()
            .apply {
                val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.drawable, null)
                window?.setBackgroundDrawable(drawable)
                setOnShowListener {
                    val positiveButton = getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setTextColor(Color.WHITE)
                    negativeButtonText?.let {
                        val negativeButton = getButton(AlertDialog.BUTTON_NEGATIVE)
                        negativeButton.setTextColor(Color.WHITE)
                    }
                }
            }
            .show()
    }
}