package com.chumakov123.gismeteoweather.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.chumakov123.gismeteoweather.domain.model.ForecastMode
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.Utils
import com.chumakov123.gismeteoweather.presentation.ui.components.preview.CurrentWeatherPreview
import com.chumakov123.gismeteoweather.presentation.ui.components.preview.DailyForecastPreview
import com.chumakov123.gismeteoweather.presentation.ui.components.preview.HourlyForecastPreview
import com.chumakov123.gismeteoweather.presentation.ui.components.preview.WidgetHeaderPreview

@Composable
fun WeatherWidgetPreview(weatherInfo: WeatherInfo.Available, appearance: WidgetAppearance, previewSizeDp: DpSize, forecastMode: ForecastMode = ForecastMode.ByDays) {
    WidgetPreviewContainer(transparencyPercent = appearance.backgroundTransparencyPercent, previewSizeDp = previewSizeDp) {
        WidgetHeaderPreview(
            placeName      = weatherInfo.placeName,
            updateTimeText = if (appearance.showUpdateTime) Utils.formatDateTime(weatherInfo.updateTime) else null,
            isLoading      = false,
        )
        if (appearance.showCurrentWeather) {
            CurrentWeatherPreview(weatherInfo, modifier = Modifier.fillMaxWidth())
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (forecastMode == ForecastMode.ByHours) {
                    HourlyForecastPreview(weatherInfo, modifier = Modifier.fillMaxSize(), appearance = appearance)
                } else {
                    DailyForecastPreview(weatherInfo, modifier = Modifier.fillMaxSize(), appearance = appearance)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (forecastMode == ForecastMode.ByHours) {
                    HourlyForecastPreview(weatherInfo, modifier = Modifier.fillMaxWidth(), appearance = appearance)
                    DailyForecastPreview(weatherInfo, modifier = Modifier.fillMaxWidth(), appearance = appearance)
                } else {
                    DailyForecastPreview(weatherInfo, modifier = Modifier.fillMaxWidth(), appearance = appearance)
                    HourlyForecastPreview(weatherInfo, modifier = Modifier.fillMaxWidth(), appearance = appearance)
                }
            }
        }

    }
}

@Composable
fun WidgetPreviewContainer(transparencyPercent: Int = 50, previewSizeDp: DpSize, content: @Composable ColumnScope.() -> Unit) {
    val cellWidth = 74.dp
    val cellHeight = 74.dp

    val widgetWidth = cellWidth * 5
    val widgetHeight = cellHeight * 3

    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = Modifier
            .size(previewSizeDp)
            .clip(shape)
            .background(
                Color(0, 0, 0, (255 * (100 - transparencyPercent) / 100).coerceIn(0, 255)),
                shape = shape
            )
            .border(1.dp, Color.Gray, shape = shape)
    ) {
        content()
    }
}