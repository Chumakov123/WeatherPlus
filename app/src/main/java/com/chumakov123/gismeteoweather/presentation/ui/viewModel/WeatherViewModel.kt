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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class WeatherUiState(
    val selectedCityCode: String,
    val cityStates: Map<String, CityWeatherUiState>,
    val citiesOrder: List<String>
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
    private val _updatingCities = MutableStateFlow<Set<String>>(emptySet())
    val updatingCities = _updatingCities.asStateFlow()

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

    fun updateCityOrder(newOrder: List<String>) {
        viewModelScope.launch {
            WeatherCityRepository.updateOrder(newOrder)
        }
    }

    private val _uiState = MutableStateFlow(
        WeatherUiState(
            selectedCityCode = "",
            cityStates = emptyMap(),
            citiesOrder = emptyList()
        )
    )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            WeatherCityRepository.citySettingsFlow
                .collect { settings ->
                    val selected = settings.selectedCity.takeIf { it in settings.cityList }
                        ?: settings.cityList.firstOrNull()

                    if (selected != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                selectedCityCode = selected,
                                citiesOrder = settings.cityList,
                                cityStates = currentState.cityStates
                                    .filterKeys { it in settings.cityList }
                            )
                        }

                        if (isInitialLoad) {
                            loadWeatherForAllCities()
                            isInitialLoad = false
                        }
                    }
                }
        }
    }

    fun loadWeatherForAllCities() {
        viewModelScope.launch {
            uiState.value.citiesOrder.map { city ->
                async { loadCityWeatherSuspend(city) }
            }.awaitAll()
        }
    }

    private fun launchLoad(cityCode: String) {
        viewModelScope.launch {
            loadCityWeatherSuspend(cityCode)
        }
    }

    fun addCity(cityInfo: OptionItem.CityInfo) {
        viewModelScope.launch {
            WeatherCityRepository.addCity(cityInfo.cityCode)
            RecentCitiesRepository.save(cityInfo)

            loadCityWeatherSuspend(cityInfo.cityCode)
        }
    }

    fun removeCity(cityCode: String) {
        viewModelScope.launch {
            WeatherCityRepository.removeCity(cityCode)
        }
    }

    fun selectCity(cityCode: String) {
        if (cityCode !in uiState.value.citiesOrder) return

        _uiState.update {
            it.copy(selectedCityCode = cityCode)
        }

        viewModelScope.launch {
            WeatherCityRepository.updateSelectedCity(cityCode)
        }
    }

    fun retryCity(cityCode: String) {
        launchLoad(cityCode)
    }

    private suspend fun loadCityWeatherSuspend(cityCode: String) {
        withContext(Dispatchers.Main) {
            _uiState.update {
                it.copy(cityStates = it.cityStates + (cityCode to CityWeatherUiState.Loading))
            }
            _updatingCities.update { it + cityCode }
        }

        try {
            val cached = withContext(Dispatchers.IO) {
                repo.getWeatherInfo(cityCode, allowStale = true)
            }
            if (cached !is WeatherInfo.Available) {
                withContext(Dispatchers.Main) {
                    markCityError(cityCode, "Не удалось получить данные")
                }
                return
            }

            val hourly = withContext(Dispatchers.Default) {
                WeatherDataPreprocessor.preprocess(cached.hourly, ForecastMode.ByHours, cached.localTime)
            }
            val daily = withContext(Dispatchers.Default) {
                WeatherDataPreprocessor.preprocess(cached.daily, ForecastMode.ByDays, cached.localTime)
            }

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(cityStates = it.cityStates + (cityCode to
                            CityWeatherUiState.Success(cached, hourly, daily)))
                }
            }

            if (!WeatherRepo.isActual(cached.updateTime)) {
                val fresh = withContext(Dispatchers.IO) {
                    repo.getWeatherInfo(cityCode, allowStale = false)
                }
                if (fresh is WeatherInfo.Available && fresh.updateTime != cached.updateTime) {
                    val h2 = withContext(Dispatchers.Default) {
                        WeatherDataPreprocessor.preprocess(fresh.hourly, ForecastMode.ByHours, fresh.localTime)
                    }
                    val d2 = withContext(Dispatchers.Default) {
                        WeatherDataPreprocessor.preprocess(fresh.daily, ForecastMode.ByDays, fresh.localTime)
                    }
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(cityStates = it.cityStates + (cityCode to
                                    CityWeatherUiState.Success(fresh, h2, d2)))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                markCityError(cityCode, "Ошибка загрузки $cityCode")
            }
        } finally {
            withContext(Dispatchers.Main) {
                _updatingCities.update { it - cityCode }
            }
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