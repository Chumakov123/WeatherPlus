package com.chumakov123.gismeteoweather.presentation.features.weather.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo

@Composable
fun WeatherParamsRow(
    weather: WeatherInfo.Available,
    modifier: Modifier = Modifier
) {
    val shadowStyle = Shadow(
        color = Color.Black,
        offset = Offset(1f, 1f),
        blurRadius = 2f
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        WeatherParamItem(
            title = "Ветер",
            mainValue = if (weather.now.windDirection == "—") {
                "—"
            } else if (weather.now.windSpeed == weather.now.windGust) {
                "${weather.now.windSpeed}"
            } else {
                "${weather.now.windSpeed}-${weather.now.windGust}"
            },
            unit = if (weather.now.windDirection == "—") {
                null
            } else {
                weather.now.windDirection
            },
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Давление",
            mainValue = "${weather.now.pressure}",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Влажность",
            mainValue = "${weather.now.humidity}",
            unit = "%",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Осадки",
            mainValue = if (weather.now.precipitation != 0.0) {
                "${weather.now.precipitation}"
            } else {
                "—"
            },
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
        WeatherParamItem(
            title = "Вода",
            mainValue = "${if (weather.now.temperatureWater >= 0) "+" else "-"}${weather.now.temperatureWater}°",
            modifier = Modifier.weight(1f),
            shadowStyle = shadowStyle
        )
    }
}
