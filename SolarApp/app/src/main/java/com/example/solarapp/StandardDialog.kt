package com.example.solarapp.util

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.R

object DialogUtils {

    fun showCustomAlertDialog(
        context: Context,
        message: String,
        positiveButtonText: String = "OK",
        negativeButtonText: String? = null,
        onPositiveClick: () -> Unit = {},
        onNegativeClick: () -> Unit = {}
    ) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.attention))
        builder.setMessage(message)

        builder.setPositiveButton(positiveButtonText) { dialog, _ ->
            onPositiveClick()
            dialog.dismiss()
        }

        negativeButtonText?.let {
            builder.setNegativeButton(it) { dialog, _ ->
                onNegativeClick()
                dialog.dismiss()
            }
        }

        val dialog = builder.create()
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.drawable, null)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.WHITE)
            negativeButtonText?.let {
                val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                negativeButton.setTextColor(Color.WHITE)
            }
        }
        dialog.show()
    }
}