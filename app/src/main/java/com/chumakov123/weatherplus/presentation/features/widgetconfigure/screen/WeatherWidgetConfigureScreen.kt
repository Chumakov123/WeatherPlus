package com.chumakov123.weatherplus.presentation.features.widgetconfigure.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.chumakov123.weatherplus.data.city.RecentCitiesRepository
import com.chumakov123.weatherplus.data.network.GismeteoApi
import com.chumakov123.weatherplus.domain.model.ForecastMode
import com.chumakov123.weatherplus.domain.model.LocationInfo
import com.chumakov123.weatherplus.domain.model.WidgetAppearance
import com.chumakov123.weatherplus.domain.model.WidgetState
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.components.AppearanceSettings
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.components.ConfigureScreenTopBar
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.components.LocationSelectionDialog
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.components.LocationSelectionRow
import com.chumakov123.weatherplus.presentation.features.widgetconfigure.components.WeatherPreviewSection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WeatherWidgetConfigureScreen(
    initialState: WidgetState,
    onConfirm: (LocationInfo, WidgetAppearance, ForecastMode) -> Unit,
    previewWeatherState: WidgetState,
    initialCity: String? = null,
    modifier: Modifier = Modifier,
) {
    var previewState by remember { mutableStateOf(previewWeatherState) }
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp.dp
    val screenWidthDp = configuration.screenWidthDp.dp

    // Preview size calculations
    val previewPadding = 32.dp
    val previewRatio = 0.64f
    val previewWidth = screenWidthDp - previewPadding
    val previewHeight = previewWidth * previewRatio
    val previewSizeDp = DpSize(previewWidth, previewHeight)

    // Location selection state
    var showLocationDialog by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var options by remember { mutableStateOf<List<LocationInfo>>(emptyList()) }
    var selected by remember { mutableStateOf<LocationInfo>(LocationInfo.Auto) }
    var ipCity by remember { mutableStateOf<LocationInfo.CityInfo?>(null) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var initialized by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Effects
    LaunchedEffect(Unit) {
        runCatching { GismeteoApi.fetchCityByIp() }
            .onSuccess { city ->
                ipCity = LocationInfo.CityInfo(
                    code = "${city.slug}-${city.id}",
                    kind = city.kind,
                    name = city.cityName,
                    info = listOfNotNull(city.countryName, city.districtName).joinToString(", "),
                )
            }
        options = buildDefaultOptions(ipCity)
    }

    LaunchedEffect(options, initialState.cityCode, initialCity) {
        if (!initialized) {
            val targetCityCode = initialCity ?: initialState.cityCode
            options.firstOrNull { it.cityCode == targetCityCode }?.let {
                selected = it
                initialized = true
            }
        }
    }

    LaunchedEffect(query) {
        searchJob?.cancel()
        searchJob = coroutineScope.launch {
            delay(300)
            options = if (query.isBlank()) {
                buildDefaultOptions(ipCity)
            } else {
                GismeteoApi
                    .searchCitiesByName(query.trim(), limit = 10)
                    .filter { ci -> "${ci.slug}-${ci.id}" != ipCity?.code }
                    .map { ci ->
                        LocationInfo.CityInfo(
                            code = "${ci.slug}-${ci.id}",
                            kind = ci.kind,
                            name = ci.cityName,
                            info = listOfNotNull(ci.countryName, ci.districtName).joinToString(", "),
                        )
                    }
            }
        }
    }

    Scaffold(
        topBar = {
            ConfigureScreenTopBar(
                forecastMode = previewState.forecastMode,
                onConfirm = {
                    onConfirm(selected, previewState.appearance, previewState.forecastMode)
                },
                onForecastModeChange = { newMode ->
                    previewState = previewState.copy(forecastMode = newMode)
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            WeatherPreviewSection(
                previewState = previewState,
                previewSizeDp = previewSizeDp,
                screenHeightDp = screenHeightDp
            )

            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background),
            ) {
                item {
                    LocationSelectionRow(
                        selectedLocation = selected,
                        onLocationClick = { showLocationDialog = true }
                    )
                }

                item {
                    AppearanceSettings(
                        appearance = previewState.appearance,
                        onAppearanceChange = { newAppearance ->
                            previewState = previewState.copy(appearance = newAppearance)
                        }
                    )
                }
            }
        }
    }

    LocationSelectionDialog(
        showDialog = showLocationDialog,
        query = query,
        options = options,
        onDismiss = { showLocationDialog = false },
        onQueryChange = { query = it },
        onLocationSelected = { location ->
            selected = location
            showLocationDialog = false
            query = ""
        }
    )
}

private fun buildDefaultOptions(ipCity: LocationInfo.CityInfo?): List<LocationInfo> =
    buildList {
        add(LocationInfo.Auto)
        ipCity?.let { add(it) }
        addAll(RecentCitiesRepository.loadRecent(ipCity))
    }
