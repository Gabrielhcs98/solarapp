package com.example.solarapp

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

/**
 * Classe de dados que representa os dados principais do clima.
 *
 * @property temp A temperatura atual.
 */
data class Main(
    val temp: Double
)

/**
 * Classe de dados que representa uma condição climática.
 *
 * @property icon O ícone que representa a condição climática.
 */
data class Weather(
    val icon: String
)