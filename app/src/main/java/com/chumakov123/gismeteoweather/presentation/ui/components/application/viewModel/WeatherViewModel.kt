package com.chumakov123.gismeteoweather.presentation.ui.components.application.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.gismeteoweather.data.repo.WeatherRepo
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherInfo.Available) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repo: WeatherRepo = WeatherRepo
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var lastAvailable: WeatherInfo.Available? = null

    init {
        loadWeather("auto")
    }

    fun loadWeather(cityCode: String) {
        viewModelScope.launch {
            _uiState.value = if (lastAvailable == null) {
                WeatherUiState.Loading
            } else {
                WeatherUiState.Success(lastAvailable!!)
            }

            try {
                val info = repo.getWeatherInfo(cityCode)
                if (info is WeatherInfo.Available) {
                    lastAvailable = info
                    _uiState.value = WeatherUiState.Success(info)
                } else {
                    _uiState.value = WeatherUiState.Error("Нет данных")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (lastAvailable != null) {
                    _uiState.value = WeatherUiState.Success(lastAvailable!!)
                } else {
                    _uiState.value = WeatherUiState.Error("Не удалось загрузить: ${e.message}")
                }
            }
        }
    }
}