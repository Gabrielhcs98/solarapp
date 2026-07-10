package com.example.solarapp.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(
    private val weatherService: WeatherService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    suspend fun fetchWeather(city: String, apiKey: String): Result<WeatherData> {
        return withContext(ioDispatcher) {
            try {
                val data = weatherService.getWeather(city, apiKey)
                if (data.name.isNotEmpty()) {
                    Result.success(data)
                } else {
                    Result.failure(Exception("Cidade não encontrada"))
                }
            } catch (_: java.io.FileNotFoundException) {
                Result.failure(Exception("Cidade não encontrada"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
