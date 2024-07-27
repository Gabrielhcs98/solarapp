package com.example.solarapp

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface de serviço para obter dados climáticos usando Retrofit.
 */
interface WeatherService {

    /**
     * Obtém os dados climáticos para uma cidade específica.
     *
     * @param city O nome da cidade para a qual obter os dados climáticos.
     * @param apiKey A chave da API para autenticação.
     * @return Os dados climáticos da cidade especificada.
     */
    @GET("solarirradiation")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): WeatherData
}