package com.example.solarapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
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
    private lateinit var buttonBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        textViewResults = findViewById(R.id.textViewResults)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)
        buttonBack = findViewById(R.id.buttonBack)

        buttonBack.setOnClickListener {
            returnToMainActivity()
        }

        val city = intent.getStringExtra("city")
        try {
            if (!city.isNullOrEmpty()) {
                fetchWeatherData(city)
            } else {
                showDialog(getString(R.string.city_not_found))
            }
        } catch (e: IllegalArgumentException) {
            showDialog(getString(R.string.city_not_found))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun fetchWeatherData(city: String) {
        val weatherService = WeatherServiceImpl()
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val apiKey = "a77b6a1742276dd6fb74dc969b5d4380"
                val weatherData = weatherService.getWeather(city, apiKey)

                withContext(Dispatchers.Main) {
                    if (weatherData.name.isNotEmpty()) {
                        updateUI(weatherData)
                    } else {
                        showDialog(getString(R.string.city_not_found))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showDialog(getString(R.string.error_fetching_data))
                }
                Log.e("ResultsActivity", "Error fetching weather data", e)
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

    private fun showDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.attention)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
            finish()
        }

        val dialog = builder.create()
        val drawable = ResourcesCompat.getDrawable(resources, R.drawable.drawable, null)
        dialog.window?.setBackgroundDrawable(drawable)
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setTextColor(Color.WHITE)
        }
        dialog.show()
    }

    private fun returnToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)

        window.enterTransition = Slide(Gravity.END)
        startActivity(intent)
        finish()
    }
}
