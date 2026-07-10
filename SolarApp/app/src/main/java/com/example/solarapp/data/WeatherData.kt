package com.example.solarapp.data

import com.google.gson.annotations.SerializedName

/**
 * Classe de dados que representa a resposta da API de clima.
 *
 * @property name O nome da cidade.
 * @property main Dados principais do clima, como temperatura.
 * @property weather Lista de condições climáticas.
 */
data class WeatherData(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(
    val temp: Double,
    val pressure: Int,
    val humidity: Int,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)
