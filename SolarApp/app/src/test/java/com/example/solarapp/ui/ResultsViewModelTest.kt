package com.example.solarapp.ui

import app.cash.turbine.test
import com.example.solarapp.data.Main
import com.example.solarapp.data.Weather
import com.example.solarapp.data.WeatherData
import com.example.solarapp.data.WeatherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResultsViewModelTest {

    private val repository = mockk<WeatherRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: ResultsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ResultsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createMockWeatherData(cityName: String) = WeatherData(
        name = cityName,
        main = Main(temp = 293.15, pressure = 1013, humidity = 70, tempMin = 290.15, tempMax = 295.15),
        weather = listOf(Weather(id = 800, main = "Clear", description = "clear sky", icon = "01d"))
    )

    @Test
    fun `fetchWeather emits success when repository returns data`() = runTest {
        val city = "Londrina"
        val mockData = createMockWeatherData(city)
        coEvery { repository.fetchWeather(city, "key") } returns Result.success(mockData)

        viewModel.uiState.test {
            viewModel.fetchWeather(city, "key")

            assertEquals(ResultsUiState.Idle, awaitItem())
            assertEquals(ResultsUiState.Loading, awaitItem())
            val successState = awaitItem() as ResultsUiState.Success
            assertEquals(city, successState.data.name)
        }
    }

    @Test
    fun `fetchWeather emits success for another city`() = runTest {
        val city = "Curitiba"
        val mockData = createMockWeatherData(city)
        coEvery { repository.fetchWeather(city, "key") } returns Result.success(mockData)

        viewModel.uiState.test {
            viewModel.fetchWeather(city, "key")

            assertEquals(ResultsUiState.Idle, awaitItem())
            assertEquals(ResultsUiState.Loading, awaitItem())
            val successState = awaitItem() as ResultsUiState.Success
            assertEquals(city, successState.data.name)
        }
    }

    @Test
    fun `fetchWeather emits error when repository fails`() = runTest {
        coEvery { repository.fetchWeather("Unknown", "key") } returns Result.failure(Exception("Net error"))

        viewModel.uiState.test {
            viewModel.fetchWeather("Unknown", "key")

            assertEquals(ResultsUiState.Idle, awaitItem())
            assertEquals(ResultsUiState.Loading, awaitItem())
            val errorState = awaitItem() as ResultsUiState.Error
            assertEquals("Net error", errorState.message)
        }
    }

    @Test
    fun `fetchWeather filters out URL from error message`() = runTest {
        coEvery { repository.fetchWeather("A", "key") } returns Result.failure(Exception("https://api.error"))

        viewModel.uiState.test {
            viewModel.fetchWeather("A", "key")

            assertEquals(ResultsUiState.Idle, awaitItem())
            assertEquals(ResultsUiState.Loading, awaitItem())
            val errorState = awaitItem() as ResultsUiState.Error
            assertEquals("Cidade não encontrada", errorState.message)
        }
    }
}
