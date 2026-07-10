package com.example.solarapp.data

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.FileNotFoundException

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherRepositoryTest {

    private val weatherService = mockk<WeatherService>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository = WeatherRepository(weatherService, testDispatcher)

    @Test
    fun `fetchWeather returns success when service returns valid data`() = runTest {
        val mockData = WeatherData(
            name = "São Paulo",
            main = Main(temp = 293.15, pressure = 1013, humidity = 70, tempMin = 290.15, tempMax = 295.15),
            weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d"))
        )
        coEvery { weatherService.getWeather("São Paulo", "any_key") } returns mockData

        val result = repository.fetchWeather("São Paulo", "any_key")

        assertTrue(result.isSuccess)
        assertEquals("São Paulo", result.getOrNull()?.name)
    }

    @Test
    fun `fetchWeather returns failure when city name is empty`() = runTest {
        val mockData = WeatherData(
            name = "",
            main = Main(temp = 0.0, pressure = 0, humidity = 0, tempMin = 0.0, tempMax = 0.0),
            weather = emptyList()
        )
        coEvery { weatherService.getWeather("Unknown", "any_key") } returns mockData

        val result = repository.fetchWeather("Unknown", "any_key")

        assertTrue(result.isFailure)
        assertEquals("Cidade não encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchWeather returns failure when service throws FileNotFoundException`() = runTest {
        coEvery { weatherService.getWeather("A", "any_key") } throws FileNotFoundException()

        val result = repository.fetchWeather("A", "any_key")

        assertTrue(result.isFailure)
        assertEquals("Cidade não encontrada", result.exceptionOrNull()?.message)
    }

    @Test
    fun `fetchWeather returns failure when service throws generic exception`() = runTest {
        coEvery { weatherService.getWeather("Any", "any_key") } throws Exception("Network Error")

        val result = repository.fetchWeather("Any", "any_key")

        assertTrue(result.isFailure)
        assertEquals("Network Error", result.exceptionOrNull()?.message)
    }
}
