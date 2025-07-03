package com.chumakov123.weatherplus.presentation.features.weather.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.chumakov123.weatherplus.domain.model.WeatherDisplaySettings
import com.chumakov123.weatherplus.domain.model.WeatherRow
import com.chumakov123.weatherplus.domain.model.WeatherRowType
import com.chumakov123.weatherplus.presentation.common.components.WeatherTable

@Composable
fun PreviewWeatherTable(
    weatherRows: Map<WeatherRowType, WeatherRow>,
    displaySettings: WeatherDisplaySettings = WeatherDisplaySettings()
) {
    val rows = buildList {
        add(weatherRows[WeatherRowType.TIME_LABELS]!!)
        add(weatherRows[WeatherRowType.ICONS]!!)
        add(weatherRows[WeatherRowType.TEMP]!!)

        displaySettings.rowOrder.forEach { rowType ->
            if (displaySettings.enabledRows.contains(rowType) && weatherRows.containsKey(rowType)) {
                add(weatherRows[rowType]!!)
            }
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        WeatherTable(rows = rows)
    }
}
