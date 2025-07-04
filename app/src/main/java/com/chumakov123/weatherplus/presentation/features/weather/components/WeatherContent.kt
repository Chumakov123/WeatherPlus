package com.chumakov123.weatherplus.presentation.features.weather.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.chumakov123.weatherplus.domain.model.WeatherInfo
import com.chumakov123.weatherplus.domain.util.Utils.plusMillis
import kotlinx.coroutines.delay

@Composable
fun WeatherContent(
    weather: WeatherInfo.Available,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nowMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = Unit,
    ) {
        while (true) {
            value = System.currentTimeMillis()
            delay(2_500L)
        }
    }

    val localDateTime =
        weather.localTime
            .plusMillis(nowMillis - weather.updateTime)

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherCurrentInfo(
            placeKind = weather.placeKind,
            placeName = weather.placeName,
            localDateTime = localDateTime,
            astroTimes = weather.astroTimes,
            temperature = weather.now.temperature,
            heatIndex = weather.now.temperatureHeatIndex,
            description = weather.now.description,
            weather = weather,
            onRefresh = onRefresh,
            modifier = modifier,
        )
    }
}
