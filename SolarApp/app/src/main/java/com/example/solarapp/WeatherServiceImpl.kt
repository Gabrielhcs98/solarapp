package com.example.solarapp

import com.google.gson.Gson
import java.net.URL

class WeatherServiceImpl : WeatherService {

    override suspend fun getWeather(city: String, apiKey: String): WeatherData {
        // Make a network request to the OpenWeatherMap API
        val response =
            URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey").readText()

        // Parse the JSON response into a WeatherData object
        val gson = Gson()
        return gson.fromJson(response, WeatherData::class.java)
    }

}