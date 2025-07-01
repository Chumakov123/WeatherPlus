package com.chumakov123.gismeteoweather.presentation.features.widgetconfigure.components.preview

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.TemperatureGradation.interpolateTemperatureColor
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables
import com.chumakov123.gismeteoweather.domain.util.WindSpeedGradation.interpolateWindColor

@Composable
fun ForecastColumnPreview(
    modifier: Modifier = Modifier,
    weatherData: WeatherData,
    date: String,
    dateColor: Color = Color.White,
    appearance: WidgetAppearance,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Дата
        Text(
            text = date,
            color = dateColor,
            style = TextStyle(
                fontSize = 12.sp * appearance.textScale,
                color = Color.White,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Иконка погоды
        Image(
            painter = painterResource(id = WeatherDrawables.getWeatherIcon(weatherData.icon)),
            contentDescription = weatherData.description,
            modifier = Modifier.size(32.dp)
        )

        // Основная / минимальная температура
        val tempColor = if (appearance.useColorIndicators) {
            interpolateTemperatureColor(weatherData.temperature)
        } else {
            Color.White
        }

        if (weatherData.temperatureMin == null) {
            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    fontSize = 14.sp * appearance.textScale,
                    color = tempColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        } else {
            val tempMinColor = if (appearance.useColorIndicators) {
                interpolateTemperatureColor(weatherData.temperatureMin)
            } else {
                Color.White
            }

            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    fontSize = 12.sp * appearance.textScale,
                    color = tempColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${if (weatherData.temperatureMin > 0) "+" else ""}${weatherData.temperatureMin}°",
                style = TextStyle(
                    fontSize = 10.sp * appearance.textScale,
                    color = tempMinColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ветер
        if (appearance.showWind) {
            val windColor = if (appearance.useColorIndicators) {
                interpolateWindColor(weatherData.windGust)
            } else {
                Color.White
            }

            val windText = if (weatherData.windDirection != "—") {
                if (weatherData.windSpeed != weatherData.windGust) {
                    "${weatherData.windSpeed}—${weatherData.windGust}, ${weatherData.windDirection}"
                } else {
                    "${weatherData.windSpeed}, ${weatherData.windDirection}"
                }
            } else {
                "—"
            }

            Text(
                text = windText,
                style = TextStyle(
                    fontSize = 10.sp * appearance.textScale,
                    color = windColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Осадки
        if (appearance.showPrecipitation) {
            val precColor = if (weatherData.precipitation != 0.0) {
                Color(66, 165, 245)
            } else {
                Color.LightGray
            }

            Text(
                text = if (weatherData.precipitation != 0.0) {
                    "${weatherData.precipitation} мм"
                } else {
                    "—"
                },
                style = TextStyle(
                    fontSize = 10.sp * appearance.textScale,
                    color = precColor,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
