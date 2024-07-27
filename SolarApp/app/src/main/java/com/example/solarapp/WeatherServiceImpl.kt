package com.example.solarapp

import com.google.gson.Gson
import java.net.URL

/**
 * Implementação do serviço de clima que obtém dados de clima da API OpenWeatherMap.
 */
class WeatherServiceImpl : WeatherService {

    /**
     * Obtém dados de clima para uma cidade específica usando a API OpenWeatherMap.
     *
     * @param city O nome da cidade para a qual obter os dados do clima.
     * @param apiKey A chave da API para autenticação com o serviço OpenWeatherMap.
     * @return Uma instância de [WeatherData] contendo os dados do clima.
     */
    override suspend fun getWeather(city: String, apiKey: String): WeatherData {
        // Faz uma requisição de rede para a API OpenWeatherMap
        val response =
            URL("https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey").readText()

        // Faz o parse da resposta JSON em um objeto WeatherData
        val gson = Gson()
        return gson.fromJson(response, WeatherData::class.java)
    }
}