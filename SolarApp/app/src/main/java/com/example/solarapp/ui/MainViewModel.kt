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

    // Estado da UI: O que a tela deve exibir (Loading, Sucesso, etc)
    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Eventos únicos: Navegação ou Toasts (coisas que acontecem uma vez)
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

            // Tenta primeiro o bairro, se não existir/falhar, tenta a cidade
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
                // Se o bairro falhou, tentamos a cidade como "plano B"
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
