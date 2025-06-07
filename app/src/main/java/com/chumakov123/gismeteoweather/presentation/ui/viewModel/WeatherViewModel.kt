package com.chumakov123.gismeteoweather.presentation.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.gismeteoweather.data.repo.WeatherRepo
import com.chumakov123.gismeteoweather.data.repo.WeatherSettingsRepository
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.WeatherDataPreprocessor
import com.chumakov123.gismeteoweather.domain.model.WeatherDisplaySettings
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherRow
import com.chumakov123.gismeteoweather.domain.model.WeatherRowType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val rawData: WeatherInfo.Available,
        val hourlyPreprocessedData: Map<WeatherRowType, WeatherRow>? = null,
        val dailyPreprocessedData: Map<WeatherRowType, WeatherRow>? = null
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(
    private val repo: WeatherRepo = WeatherRepo
) : ViewModel() {

    val settings: StateFlow<WeatherDisplaySettings> =
        WeatherSettingsRepository.settingsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = WeatherDisplaySettings()
            )

    fun onSettingsChanged(newSettings: WeatherDisplaySettings) {
        viewModelScope.launch {
            WeatherSettingsRepository.updateSettings(newSettings)
        }
    }

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        loadWeather("sankt-peterburg-4079")
    }

    fun loadWeather(cityCode: String) {
        viewModelScope.launch {
            try {
                // 1. Получаем кешированные данные
                val cached = repo.getWeatherInfo(cityCode, allowStale = true)

                if (cached !is WeatherInfo.Available) {
                    _uiState.value = WeatherUiState.Error("Нет данных")
                    return@launch
                }

                applyWeatherInfo(cached)

                // 2. Загружаем актуальные, если нужно
                if (!WeatherRepo.isActual(cached.updateTime)) {
                    val fresh = repo.getWeatherInfo(cityCode, allowStale = false)

                    if (fresh is WeatherInfo.Available && fresh.updateTime != cached.updateTime) {
                        applyWeatherInfo(fresh)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = WeatherUiState.Error("Ошибка загрузки: ${e.message}")
            }
        }
    }

    private fun applyWeatherInfo(info: WeatherInfo.Available) {
        val hourly = WeatherDataPreprocessor.preprocess(
            weather = info.hourly,
            forecastMode = ForecastMode.ByHours,
            localDateTime = info.localTime
        )

        val daily = WeatherDataPreprocessor.preprocess(
            weather = info.daily,
            forecastMode = ForecastMode.ByDays,
            localDateTime = info.localTime
        )

        _uiState.value = WeatherUiState.Success(
            rawData = info,
            hourlyPreprocessedData = hourly,
            dailyPreprocessedData = daily
        )
    }
}