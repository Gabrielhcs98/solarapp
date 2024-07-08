package com.example.solarapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import android.graphics.Color
import com.example.solarapp.R

class NetworkChecker(private val context: Context) {

    fun checkNetworkQuality() {
        if (isNetworkQualityPoor()) {
            showDialog()
        }
    }

    fun isNetworkQualityPoor(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)

        return networkCapabilities?.let {
            when {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    it.linkDownstreamBandwidthKbps < 500 // 0,5 Mbps
                }
                else -> false
            }
        } ?: true
    }

    private fun showDialog() {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.attention))
        builder.setMessage(R.string.error_network)
        builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        builder.setOnDismissListener {}

        val dialog = builder.create()
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.drawable, null)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.WHITE)
        }
        dialog.show()
    }
}
