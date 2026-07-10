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
class MainViewModelTest {

    private val repository = mockk<WeatherRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = MainViewModel(repository)
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
    fun `validateAndSubmit returns error when location is empty`() = runTest {
        viewModel.uiState.test {
            viewModel.validateAndSubmit("", "key")

            assertEquals(MainUiState.Idle, awaitItem())
            val errorState = awaitItem() as MainUiState.Error
            assertEquals("Nome de local inválido. Use apenas letras.", errorState.message)
        }
    }

    @Test
    fun `validateAndSubmit returns error when location has numbers`() = runTest {
        viewModel.uiState.test {
            viewModel.validateAndSubmit("Sã0 Paulo", "key")

            assertEquals(MainUiState.Idle, awaitItem())
            val errorState = awaitItem() as MainUiState.Error
            assertEquals("Nome de local inválido. Use apenas letras.", errorState.message)
        }
    }

    @Test
    fun `validateAndSubmit emits navigation event on success`() = runTest {
        val mockData = createMockWeatherData("São Paulo")
        coEvery { repository.fetchWeather("São Paulo", "key") } returns Result.success(mockData)

        viewModel.navigationEvent.test {
            viewModel.validateAndSubmit("São Paulo", "key")

            assertEquals("São Paulo", awaitItem())
        }
    }

    @Test
    fun `searchByCoordinates tries neighborhood then city`() = runTest {
        coEvery { repository.fetchWeather("Bairro", "key") } returns Result.failure(Exception("Not found"))
        val mockCityData = createMockWeatherData("Cidade")
        coEvery { repository.fetchWeather("Cidade", "key") } returns Result.success(mockCityData)

        viewModel.navigationEvent.test {
            viewModel.searchByCoordinates("Bairro", "Cidade", "key")

            assertEquals("Cidade", awaitItem())
        }
    }
}
