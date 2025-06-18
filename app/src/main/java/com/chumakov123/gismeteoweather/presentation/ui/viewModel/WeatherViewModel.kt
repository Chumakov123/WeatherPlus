package com.chumakov123.gismeteoweather.presentation.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.gismeteoweather.OptionItem
import com.chumakov123.gismeteoweather.data.repo.RecentCitiesRepository
import com.chumakov123.gismeteoweather.data.repo.WeatherCityRepository
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val selectedCityCode: String,
    val cityStates: Map<String, CityWeatherUiState>
)

sealed class CityWeatherUiState {
    object Loading : CityWeatherUiState()
    data class Success(
        val rawData: WeatherInfo.Available,
        val hourlyPreprocessedData: Map<WeatherRowType, WeatherRow>?,
        val dailyPreprocessedData: Map<WeatherRowType, WeatherRow>?,
    ) : CityWeatherUiState()

    data class Error(val message: String) : CityWeatherUiState()
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

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            selectedCityCode = "",
            cityStates = emptyMap()
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentCities = listOf<String>()

    init {
        viewModelScope.launch {
            val settings = WeatherCityRepository.citySettingsFlow.first()
            currentCities = settings.cityList
            val selected = settings.selectedCity.takeIf { it in currentCities } ?: currentCities.firstOrNull()

            if (selected != null) {
                _uiState.value = _uiState.value.copy(selectedCityCode = selected)
                currentCities.forEach { city ->
                    loadCityWeather(city)
                }
            }
        }
    }

    fun addCity(cityInfo: OptionItem.CityInfo) {
        viewModelScope.launch {
            WeatherCityRepository.addCity(cityInfo.cityCode)
            RecentCitiesRepository.save(cityInfo)
            currentCities = currentCities.toMutableList().apply { add(cityInfo.cityCode) }
            loadCityWeather(cityInfo.cityCode)

            _uiState.update { state ->
                state.copy(
                    selectedCityCode = cityInfo.cityCode,
                    cityStates = state.cityStates
                )
            }
        }
    }

    fun removeCity(cityCode: String) {
        viewModelScope.launch {
            WeatherCityRepository.removeCity(cityCode)

            val newCityList = currentCities.toMutableList().apply { remove(cityCode) }
            currentCities = newCityList

            _uiState.update { state ->
                val newCityStates = state.cityStates - cityCode

                val newSelected = when {
                    state.selectedCityCode == cityCode -> newCityList.firstOrNull().orEmpty()
                    else -> state.selectedCityCode
                }

                state.copy(
                    selectedCityCode = newSelected,
                    cityStates = newCityStates
                )
            }
        }
    }


    fun selectCity(cityCode: String) {
        if (cityCode !in currentCities) return

        _uiState.update {
            it.copy(selectedCityCode = cityCode)
        }

        viewModelScope.launch {
            WeatherCityRepository.updateSelectedCity(cityCode)
        }
    }

    fun retryCity(cityCode: String) {
        loadCityWeather(cityCode)
    }

    private fun loadCityWeather(cityCode: String) {
        _uiState.update {
            it.copy(
                cityStates = it.cityStates + (cityCode to CityWeatherUiState.Loading)
            )
        }

        viewModelScope.launch {
            try {
                val cached = repo.getWeatherInfo(cityCode, allowStale = true)
                if (cached !is WeatherInfo.Available) {
                    markCityError(cityCode, "Нет данных")
                    return@launch
                }

                applyWeather(cityCode, cached)

                if (!WeatherRepo.isActual(cached.updateTime)) {
                    val fresh = repo.getWeatherInfo(cityCode, allowStale = false)
                    if (fresh is WeatherInfo.Available && fresh.updateTime != cached.updateTime) {
                        applyWeather(cityCode, fresh)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                markCityError(cityCode, "Ошибка загрузки $cityCode")
            }
        }
    }

    private fun applyWeather(cityCode: String, data: WeatherInfo.Available) {
        val hourly = WeatherDataPreprocessor.preprocess(data.hourly, ForecastMode.ByHours, data.localTime)
        val daily = WeatherDataPreprocessor.preprocess(data.daily, ForecastMode.ByDays, data.localTime)

        val cityState = CityWeatherUiState.Success(data, hourly, daily)

        _uiState.update {
            it.copy(
                cityStates = it.cityStates + (cityCode to cityState)
            )
        }
    }

    private fun markCityError(cityCode: String, message: String) {
        _uiState.update {
            it.copy(
                cityStates = it.cityStates + (cityCode to CityWeatherUiState.Error(message))
            )
        }
    }
}