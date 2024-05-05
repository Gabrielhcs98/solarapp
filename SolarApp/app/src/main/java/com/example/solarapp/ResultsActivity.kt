package com.example.solarapp

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultsActivity : AppCompatActivity() {

    private lateinit var textViewResults: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var imageViewWeatherIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        textViewResults = findViewById(R.id.textViewResults)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)

        val city = intent.getStringExtra("city")
        try {
            if (!city.isNullOrEmpty()) {
                fetchWeatherData(city)
            } else {
                throw IllegalArgumentException(getString(R.string.city_not_found))
            }
        } catch (e: IllegalArgumentException) {
            textViewResults.text = getString(R.string.city_not_found)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchWeatherData(city: String){
        val weatherService = WeatherServiceImpl()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val apiKey = "a77b6a1742276dd6fb74dc969b5d4380"
                val weatherData = weatherService.getWeather(city, apiKey)

                withContext(Dispatchers.Main) {
                    updateUI(weatherData)
                }
            } catch (e: Exception) {
                Log.e("ResultsActivity", "Error fetching weather data")
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        val celsiusTemperature = (weatherData.main.temp - 273.15).toInt()
        textViewResults.text = weatherData.name
        textViewTemperature.text = "${celsiusTemperature}°C"
        val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"
        Glide.with(this)
            .load(iconUrl)
            .into(imageViewWeatherIcon)
    }
}
