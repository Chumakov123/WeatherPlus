package com.chumakov123.gismeteoweather.presentation.viewModel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.gismeteoweather.data.city.RecentCitiesRepository
import com.chumakov123.gismeteoweather.data.city.WeatherCityRepository
import com.chumakov123.gismeteoweather.data.weather.WeatherRepository
import com.chumakov123.gismeteoweather.data.storage.SettingsRepository
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.LocationInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherDataPreprocessor
import com.chumakov123.gismeteoweather.domain.model.WeatherDisplaySettings
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WeatherRow
import com.chumakov123.gismeteoweather.domain.model.WeatherRowType
import com.chumakov123.gismeteoweather.presentation.widget.receiver.WeatherUpdateReceiver
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
    val citiesOrder: List<String>,
)

sealed class CityWeatherUiState {
    data object Loading : CityWeatherUiState()

    data class Success(
        val rawData: WeatherInfo.Available,
        val hourlyPreprocessedData: Map<WeatherRowType, WeatherRow>?,
        val dailyPreprocessedData: Map<WeatherRowType, WeatherRow>?,
    ) : CityWeatherUiState()

    data class Error(
        val message: String,
    ) : CityWeatherUiState()
}

class WeatherViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val _updatingCities = MutableStateFlow<Set<String>>(emptySet())
    val updatingCities = _updatingCities.asStateFlow()

    val settings: StateFlow<WeatherDisplaySettings> =
        SettingsRepository.settingsFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = WeatherDisplaySettings(),
            )

    fun onSettingsChanged(newSettings: WeatherDisplaySettings) {
        viewModelScope.launch {
            SettingsRepository.updateSettings(newSettings)
        }
    }

    fun updateCityOrder(newOrder: List<String>) {
        viewModelScope.launch {
            WeatherCityRepository.updateOrder(newOrder)
        }
    }

    private val _uiState =
        MutableStateFlow(
            WeatherUiState(
                selectedCityCode = "",
                cityStates = emptyMap(),
                citiesOrder = emptyList(),
            ),
        )
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var isInitialLoad = true

    init {
        viewModelScope.launch {
            WeatherCityRepository.citySettingsFlow
                .collect { settings ->
                    val selected =
                        settings.selectedCity.takeIf { it in settings.cityList }
                            ?: settings.cityList.firstOrNull()

                    if (selected != null) {
                        _uiState.update { currentState ->
                            currentState.copy(
                                selectedCityCode = selected,
                                citiesOrder = settings.cityList,
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
            uiState.value.citiesOrder
                .map { city ->
                    async { loadCityWeatherSuspend(city) }
                }.awaitAll()
            triggerWidgetUpdate()
        }
    }

    private fun launchLoad(cityCode: String) {
        viewModelScope.launch {
            loadCityWeatherSuspend(cityCode)
        }
    }

    fun addCity(cityCode: String) {
        viewModelScope.launch {
            WeatherCityRepository.addCity(cityCode)
            loadCityWeatherSuspend(cityCode)
        }
    }

    fun addCity(cityInfo: LocationInfo.CityInfo) {
        viewModelScope.launch {
            WeatherCityRepository.addCity(cityInfo.cityCode)
            RecentCitiesRepository.save(cityInfo)
            loadCityWeatherSuspend(cityInfo.cityCode)
        }
    }

    fun loadCityPreview(cityCode: String) {
        viewModelScope.launch {
            loadCityWeatherSuspend(cityCode)
        }
    }

    fun removeCity(cityCode: String) {
        viewModelScope.launch {
            WeatherCityRepository.removeCity(cityCode)

            _uiState.update { currentState ->
                val newCitiesOrder = currentState.citiesOrder - cityCode

                val newSelected =
                    when {
                        currentState.selectedCityCode == cityCode -> newCitiesOrder.firstOrNull() ?: ""
                        else -> currentState.selectedCityCode
                    }

                currentState.copy(
                    selectedCityCode = newSelected,
                    citiesOrder = newCitiesOrder,
                )
            }
        }
    }

    fun removeCities(cityCodes: Set<String>) {
        viewModelScope.launch {
            WeatherCityRepository.removeCities(cityCodes)

            _uiState.update { currentState ->
                val newCitiesOrder = currentState.citiesOrder.filter { it !in cityCodes }

                val newSelected =
                    when {
                        currentState.selectedCityCode in cityCodes -> newCitiesOrder.firstOrNull() ?: ""
                        else -> currentState.selectedCityCode
                    }

                currentState.copy(
                    selectedCityCode = newSelected,
                    citiesOrder = newCitiesOrder,
                )
            }
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
        val previousState = _uiState.value.cityStates[cityCode]
        val showLoadingState = previousState !is CityWeatherUiState.Success

        if (showLoadingState) {
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(cityStates = it.cityStates + (cityCode to CityWeatherUiState.Loading))
                }
            }
        }

        _updatingCities.update { it + cityCode }

        try {
            val cached =
                withContext(Dispatchers.IO) {
                    WeatherRepository.getWeatherInfo(cityCode, allowStale = true)
                }

            if (cached is WeatherInfo.Available) {
                if (showLoadingState) {
                    updateCityState(cityCode, cached)
                }
                if (WeatherRepository.isActual(cached.updateTime)) {
                    return
                }
            } else {
                withContext(Dispatchers.Main) {
                    markCityError(cityCode, "Ошибка загрузки $cityCode")
                }
                return
            }

            val fresh =
                withContext(Dispatchers.IO) {
                    WeatherRepository.getWeatherInfo(cityCode, allowStale = false)
                }

            if (fresh is WeatherInfo.Available) {
                updateCityState(cityCode, fresh)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                markCityError(cityCode, "Ошибка загрузки $cityCode")
            }
        } finally {
            _updatingCities.update { it - cityCode }
        }
    }

    private suspend fun updateCityState(
        cityCode: String,
        data: WeatherInfo.Available,
    ) {
        val hourly =
            withContext(Dispatchers.Default) {
                WeatherDataPreprocessor.preprocess(data.hourly, ForecastMode.ByHours, data.localTime)
            }
        val daily =
            withContext(Dispatchers.Default) {
                WeatherDataPreprocessor.preprocess(data.daily, ForecastMode.ByDays, data.localTime)
            }

        withContext(Dispatchers.Main) {
            _uiState.update {
                it.copy(
                    cityStates =
                        it.cityStates + (
                            cityCode to
                                    CityWeatherUiState.Success(data, hourly, daily)
                        ),
                )
            }
        }
    }

    private fun markCityError(
        cityCode: String,
        message: String,
    ) {
        if (_uiState.value.cityStates[cityCode] !is CityWeatherUiState.Success) {
            _uiState.update {
                it.copy(
                    cityStates = it.cityStates + (cityCode to CityWeatherUiState.Error(message)),
                )
            }
        }
    }

    private fun triggerWidgetUpdate() {
        val intent = Intent(getApplication(), WeatherUpdateReceiver::class.java)
        getApplication<Application>().sendBroadcast(intent)
    }
}
