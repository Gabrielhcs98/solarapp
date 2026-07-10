package com.example.solarapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solarapp.data.WeatherData
import com.example.solarapp.data.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultsViewModel(private val repository: WeatherRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultsUiState>(ResultsUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun fetchWeather(city: String, apiKey: String) {
        if (city.isEmpty()) {
            _uiState.value = ResultsUiState.Error("Cidade não informada")
            return
        }

        _uiState.value = ResultsUiState.Loading

        viewModelScope.launch {
            val result = repository.fetchWeather(city, apiKey)
            result.onSuccess { data ->
                if (data.name.isNotEmpty()) {
                    _uiState.value = ResultsUiState.Success(data)
                } else {
                    _uiState.value = ResultsUiState.Error("Cidade não encontrada")
                }
            }.onFailure { error ->
                val message = if (error.message?.contains("http") == true) {
                    "Cidade não encontrada"
                } else {
                    error.message ?: "Erro ao buscar dados"
                }
                _uiState.value = ResultsUiState.Error(message)
            }
        }
    }
}

sealed class ResultsUiState {
    object Idle : ResultsUiState()
    object Loading : ResultsUiState()
    data class Success(val data: WeatherData) : ResultsUiState()
    data class Error(val message: String) : ResultsUiState()
}
