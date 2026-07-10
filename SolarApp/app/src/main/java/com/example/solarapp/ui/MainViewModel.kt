package com.example.solarapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarapp.data.WeatherRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val regex = Regex("^[A-Za-zÁÉÍÓÚÂÊÎÔÛÃÕÇáéíóúâêîôûãõç\\s]+$")

    fun validateAndSubmit(location: String, apiKey: String) {
        if (location.isEmpty() || !regex.matches(location)) {
            _uiState.value = MainUiState.Error("Nome de local inválido. Use apenas letras.")
            return
        }

        _uiState.value = MainUiState.Loading

        viewModelScope.launch {
            val result = repository.fetchWeather(location, apiKey)
            result.onSuccess {
                _navigationEvent.emit(location)
                _uiState.value = MainUiState.Idle
            }.onFailure { error ->
                val errorMessage = when(error) {
                    is java.net.UnknownHostException -> "Sem conexão com a internet"
                    else -> error.message ?: "Erro desconhecido"
                }
                _uiState.value = MainUiState.Error(errorMessage)
            }
        }
    }

    fun searchByCoordinates(neighborhood: String?, city: String?, apiKey: String) {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            val locationToTry = neighborhood ?: city
            if (locationToTry == null) {
                _uiState.value = MainUiState.Error("Localização não encontrada")
                return@launch
            }

            val result = repository.fetchWeather(locationToTry, apiKey)
            result.onSuccess {
                _navigationEvent.emit(locationToTry)
                _uiState.value = MainUiState.Idle
            }.onFailure {
                if (neighborhood != null && city != null && locationToTry != city) {
                    val cityResult = repository.fetchWeather(city, apiKey)
                    cityResult.onSuccess {
                        _navigationEvent.emit(city)
                        _uiState.value = MainUiState.Idle
                    }.onFailure { error ->
                        _uiState.value = MainUiState.Error("Erro ao buscar dados: ${error.message}")
                    }
                } else {
                    _uiState.value = MainUiState.Error("Erro ao buscar dados: ${it.message}")
                }
            }
        }
    }
}

sealed class MainUiState {
    object Idle : MainUiState()
    object Loading : MainUiState()
    data class Error(val message: String) : MainUiState()
}
