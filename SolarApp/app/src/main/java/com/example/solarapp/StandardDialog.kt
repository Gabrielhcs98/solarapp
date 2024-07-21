package com.example.solarapp.util

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.example.solarapp.R

object DialogUtils {
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