package com.chumakov123.gismeteoweather.presentation.ui.components.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.Utils

@Composable
fun HourlyForecast(
    weatherInfo: WeatherInfo.Available,
    appearance: WidgetAppearance,
    modifier: GlanceModifier = GlanceModifier
) {
    val startIndex = Utils.getIntervalIndexByHour(weatherInfo.localTime.hour)
    val visibleCount = 6

    val displayList = weatherInfo.hourly
        .drop(startIndex)
        .take(visibleCount)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        displayList.forEachIndexed { index, item ->
            val hourLabel = Utils.getIntervalStartTime(startIndex + index)
            ForecastColumn(
                weatherData = item,
                date = hourLabel,
                modifier = GlanceModifier.defaultWeight(),
                appearance = appearance)
        }
    }
}