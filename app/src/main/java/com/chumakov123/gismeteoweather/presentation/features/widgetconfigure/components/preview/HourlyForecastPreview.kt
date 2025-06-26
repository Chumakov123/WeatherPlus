package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.Utils

@Composable
fun HourlyForecastPreview(
    weatherInfo: WeatherInfo.Available,
    appearance: WidgetAppearance,
    modifier: Modifier = Modifier
) {
    val startIndex = Utils.getIntervalIndexByHour(weatherInfo.localTime.hour)
    val visibleCount = 6

    val displayList = weatherInfo.hourly
        .drop(startIndex)
        .take(visibleCount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val hourLabel = Utils.getIntervalStartTime(startIndex + index)

            ForecastColumnPreview(
                weatherData = item,
                date = hourLabel,
                appearance = appearance,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
            )
        }
    }
}
