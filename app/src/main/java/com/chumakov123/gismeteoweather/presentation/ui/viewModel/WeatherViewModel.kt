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
                val rawInfo = repo.getWeatherInfo(cityCode)
                if (rawInfo !is WeatherInfo.Available) {
                    _uiState.value = WeatherUiState.Error("Нет данных")
                    return@launch
                }
                _uiState.value = WeatherUiState.Success(rawInfo)
                val hourlyPreprocessed = WeatherDataPreprocessor.preprocess(
                    weather = rawInfo.hourly,
                    forecastMode = ForecastMode.ByHours,
                    localDateTime = rawInfo.localTime
                )
                _uiState.value = WeatherUiState.Success(rawInfo, hourlyPreprocessed)
                val dailyPreprocessed = WeatherDataPreprocessor.preprocess(
                    weather = rawInfo.daily,
                    forecastMode = ForecastMode.ByDays,
                    localDateTime = rawInfo.localTime
                )
                _uiState.value = WeatherUiState.Success(rawInfo, hourlyPreprocessed, dailyPreprocessed)
                lastAvailable = rawInfo

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