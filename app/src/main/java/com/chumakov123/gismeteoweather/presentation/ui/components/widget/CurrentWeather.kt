package com.chumakov123.gismeteoweather.presentation.ui.components.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables

@Composable
fun CurrentWeather(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier,
    forecastColumns: Int,
    forecastRows: Int,
) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Левая колонка: текущая температура и описание
        Column(
            modifier = modifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (forecastColumns < 2) {
                    Box(contentAlignment = Alignment.TopCenter) {
                        Image(
                            provider = ImageProvider(WeatherDrawables.getWeatherIcon(weatherInfo.now.icon)),
                            contentDescription = weatherInfo.now.description,
                            modifier = GlanceModifier.size(30.dp),
                        )
                    }
                        Text(
                        text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 16.sp
                        ),
                        maxLines = 1
                    )
            } else {
                Row(
                    modifier = modifier.defaultWeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 24.sp
                        ),
                        maxLines = 1
                    )
                    WeatherIcon(weatherInfo)
                }
            }
            Text(
                text = if (forecastColumns >= 3)
                    weatherInfo.now.description
                else
                    weatherInfo.placeName,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1
            )
        }

        if (forecastColumns >= 3) {
            Spacer(modifier = GlanceModifier.width(4.dp))

            Spacer(
                modifier = GlanceModifier
                    .height(48.dp)
                    .width(1.dp)
                    .background(ColorProvider(Color.Gray))
            )

            Spacer(modifier = GlanceModifier.width(4.dp))
            // Правая колонка: осадки, ветер, давление
            Column(
                modifier = modifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row {
                    Text(
                        text = "Осадки: ",
                        style = TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = if (weatherInfo.now.precipitation != 0.0)
                            "${weatherInfo.now.precipitation} мм"
                        else
                            "Нет",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Row(modifier = modifier.defaultWeight()) {
                    Text(
                        text = "Ветер: ",
                        style = TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = if (weatherInfo.now.windDirection != "—")
                            if (weatherInfo.now.windSpeed != weatherInfo.now.windGust)
                                "${weatherInfo.now.windSpeed}—${weatherInfo.now.windGust} м/c, ${weatherInfo.now.windDirection}"
                            else
                                "${weatherInfo.now.windSpeed} м/c, ${weatherInfo.now.windDirection}"
                        else
                            "—",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }

                Row {
                    Text(
                        text = "Давление: ",
                        style = TextStyle(
                            color = ColorProvider(Color.LightGray),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "${weatherInfo.now.pressure} мм рт. ст.",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = 11.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }

    }
}