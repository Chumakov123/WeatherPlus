package com.chumakov123.gismeteoweather.presentation.ui.components.application

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.Utils.plusMillis
import kotlinx.coroutines.delay

@Composable
fun WeatherContent(
    weather: WeatherInfo.Available,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val nowMillis by produceState(
        initialValue = System.currentTimeMillis(),
        key1 = Unit
    ) {
        while (true) {
            value = System.currentTimeMillis()
            delay(2_500L)
        }
    }

    val localDateTime = weather.localTime
        .plusMillis(nowMillis - weather.updateTime)

    Box(modifier = Modifier.fillMaxSize()) {
        WeatherBackground(iconWeather = weather.now.iconWeather)
        WeatherCurrentInfo(
            placeName      = weather.placeName,
            localDateTime  = localDateTime,
            astroTimes     = weather.astroTimes,
            temperature    = weather.now.temperature,
            heatIndex      = weather.now.temperatureHeatIndex,
            description    = weather.now.description,
            weather        = weather,
            onRefresh      = onRefresh,
            modifier = modifier
        )
    }
}