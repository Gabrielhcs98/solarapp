package com.example.solarapp.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.solarapp.R
import com.example.solarapp.data.WeatherData
import com.example.solarapp.data.WeatherRepository
import com.example.solarapp.data.WeatherServiceImpl
import com.example.solarapp.util.DialogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ResultsActivity exibe os resultados climáticos baseados na cidade fornecida.
 */
class ResultsActivity : AppCompatActivity() {

    private val viewModel: ResultsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ResultsViewModel::class.java)) {
                    val service = WeatherServiceImpl()
                    val repo = WeatherRepository(service, Dispatchers.IO)
                    @Suppress("UNCHECKED_CAST")
                    return ResultsViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    private lateinit var textViewResults: TextView
    private lateinit var textViewTemperature: TextView
    private lateinit var imageViewWeatherIcon: ImageView
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var buttonBack: Button
    private lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        initViews()
        setupObservers()

        val city = intent.getStringExtra("city")?.trim()
        val apiKey = getString(R.string.api_key)

        if (city != null) {
            viewModel.fetchWeather(city, apiKey)
        } else {
            showDialog(getString(R.string.city_not_found))
        }
    }

    private fun initViews() {
        textViewResults = findViewById(R.id.textViewResults)
        textViewTemperature = findViewById(R.id.textViewTemperature)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)
        progressBarLoading = findViewById(R.id.progressBarLoading)
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
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is ResultsUiState.Loading -> {
                            progressBarLoading.visibility = View.VISIBLE
                            imageViewWeatherIcon.visibility = View.GONE
                        }
                        is ResultsUiState.Success -> {
                            progressBarLoading.visibility = View.GONE
                            updateUI(state.data)
                        }
                        is ResultsUiState.Error -> {
                            progressBarLoading.visibility = View.GONE
                            showDialog(state.message)
                        }
                        is ResultsUiState.Idle -> {
                            progressBarLoading.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(weatherData: WeatherData) {
        val celsiusTemperature = (weatherData.main.temp - 273.15).toInt()
        textViewResults.text = weatherData.name
        textViewTemperature.text = getString(R.string.temperature_format, celsiusTemperature)
        
        val iconUrl = "https://openweathermap.org/img/wn/${weatherData.weather[0].icon}@2x.png"

        val request = ImageRequest.Builder(this)
            .data(iconUrl)
            .size(150, 150)
            .target(imageViewWeatherIcon)
            .build()
        imageLoader.enqueue(request)
        imageViewWeatherIcon.visibility = View.VISIBLE
    }

    private fun showDialog(message: String) {
        buttonBack.visibility = View.INVISIBLE
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
