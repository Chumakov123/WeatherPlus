package com.chumakov123.gismeteoweather.presentation.ui.components.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.model.WidgetAppearance
import com.chumakov123.gismeteoweather.domain.util.TemperatureGradation.interpolateTemperatureColor
import com.chumakov123.gismeteoweather.domain.util.WindSpeedGradation.interpolateWindColor

@Composable
fun ForecastColumn(
    weatherData: WeatherData,
    date: String,
    dateColor: Color = Color.White,
    appearance: WidgetAppearance,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier.padding(0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Дата
        Text(
            text = date,
            style = TextStyle(
                color = ColorProvider(dateColor),
                fontSize = 12.sp,
            ),
            maxLines = 1,
            modifier = GlanceModifier.height(14.dp)
        )

        // Иконка
        Image(
            provider = ImageProvider(weatherData.icon),
            contentDescription = weatherData.description,
            modifier = GlanceModifier.size(32.dp),
        )

        // Температура
        val temperatureColor = if (appearance.useColorIndicators)
            interpolateTemperatureColor(weatherData.temperature)
        else
            Color.White

        if (weatherData.temperatureMin == null) {
            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColor),
                    fontSize = 14.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.height(16.dp)
            )
        } else {
            val temperatureColorMin = if (appearance.useColorIndicators)
                interpolateTemperatureColor(weatherData.temperatureMin)
            else
                Color.White

            Text(
                text = "${if (weatherData.temperature > 0) "+" else ""}${weatherData.temperature}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColor),
                    fontSize = 12.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.height(14.dp)
            )
            Text(
                text = "${if (weatherData.temperatureMin > 0) "+" else ""}${weatherData.temperatureMin}°",
                style = TextStyle(
                    color = ColorProvider(temperatureColorMin),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.height(12.dp)
            )
        }

        // Ветер
        if (appearance.showWind) {
            val windColor = if (appearance.useColorIndicators)
                interpolateWindColor(weatherData.windGust)
            else
                Color.White

            Text(
                text = if (weatherData.windDirection != "—")
                    if (weatherData.windSpeed != weatherData.windGust)
                        "${weatherData.windSpeed}—${weatherData.windGust}, ${weatherData.windDirection}"
                    else
                        "${weatherData.windSpeed}, ${weatherData.windDirection}"
                else
                    "—",
                style = TextStyle(
                    color = ColorProvider(windColor),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.height(12.dp)
            )
        }

        // Осадки
        if (appearance.showPrecipitation) {
            Text(
                text = if (weatherData.precipitation != 0.0)
                    "${weatherData.precipitation} мм"
                else
                    "—",
                style = TextStyle(
                    color = if (weatherData.precipitation != 0.0)
                        ColorProvider(Color(66, 165, 245, 255))
                    else
                        ColorProvider(Color.LightGray),
                    fontSize = 10.sp
                ),
                maxLines = 1,
                modifier = GlanceModifier.height(12.dp)
            )
        }
    }
}