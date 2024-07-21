package com.example.solarapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.solarapp.util.DialogUtils

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
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val message = if (networkCapabilities == null) {
            context.getString(R.string.no_network)
        } else {
            context.getString(R.string.error_network)
        }

        DialogUtils.showCustomAlertDialog(
            context = context,
            title = context.getString(R.string.attention),
            message = message,
            positiveButtonText = "OK",
            onPositiveClick = {
                // Fecha o diálogo
            }
        )
    }
}