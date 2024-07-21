package com.example.solarapp

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.solarapp.util.DialogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsActivity : AppCompatActivity() {

    private lateinit var textViewResults: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private lateinit var buttonBack: Button
    private lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        textViewResults = findViewById(R.id.textViewResults)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            finish()
        }

        imageLoader = ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()

        val city = intent.getStringExtra("city")?.trim()
        try {
            if (!city.isNullOrEmpty()) {
                fetchWeatherData(city)
            } else {
                showDialog(getString(R.string.city_not_found), true)
            }
        } catch (e: IllegalArgumentException) {
            showDialog(getString(R.string.city_not_found), true)
        }
    }

    private fun fetchWeatherData(city: String) {
        val weatherService = WeatherServiceImpl()
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val apiKey = "a77b6a1742276dd6fb74dc969b5d4380"
                val weatherData = weatherService.getWeather(city, apiKey)

                withContext(Dispatchers.Main) {
                    if (weatherData.name.isNotEmpty()) {
                        updateUI(weatherData)
                    } else {
                        showDialog(getString(R.string.city_not_found), true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showDialog(getString(R.string.error_fetching_data), true)
                }
                Log.e("ResultsActivity", "Error fetching weather data", e)
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        val celsiusTemperature = (weatherData.main.temp - 273.15).toInt()
        textViewResults.text = weatherData.name
        textViewTemperature.text = "${celsiusTemperature}°C"
        // Versão 2x para melhor qualidade
        val iconUrl = "https://openweathermap.org/img/wn/${weatherData.weather[0].icon}@2x.png"

        val progressBarLoading = findViewById<View>(R.id.progressBarLoading)
        progressBarLoading.visibility = View.VISIBLE

        val request = ImageRequest.Builder(this)
            .data(iconUrl)
            .size(150, 150) // Redimensiona a imagem para 150x150 pixels
            .target(imageViewWeatherIcon)
            .build()
        imageLoader.enqueue(request)
        imageViewWeatherIcon.visibility = View.VISIBLE
        progressBarLoading.visibility = View.GONE
    }

    private fun showDialog(message: String, ocultaBotaoVoltar: Boolean) {
        if (ocultaBotaoVoltar) {
            buttonBack.visibility = View.INVISIBLE
        }

        DialogUtils.showCustomAlertDialog(
            context = this,
            title = getString(R.string.attention),
            message = message,
            positiveButtonText = "OK",
            onPositiveClick = {
                finish()
            }
        )
    }
}