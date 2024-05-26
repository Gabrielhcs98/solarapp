package com.example.solarapp

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("solarirradiation")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String
    ): WeatherData
}