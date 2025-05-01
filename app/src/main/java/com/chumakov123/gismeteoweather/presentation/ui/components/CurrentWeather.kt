package com.chumakov123.gismeteoweather.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.chumakov123.gismeteoweather.domain.model.WeatherInfo

@Composable
fun CurrentWeather(
    weatherInfo: WeatherInfo.Available,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = modifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val defaultWeight = GlanceModifier.wrapContentSize()
            Row(
                modifier = modifier.defaultWeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally,) {
                Text(
                    text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 24.sp
                    )
                )
                if (weatherInfo.now.temperatureMin != null) {
                    Row(modifier = modifier.defaultWeight()) {
                        Text(
                            text = "${weatherInfo.now.temperatureMin}°",
                            style = TextStyle(
                                color = ColorProvider(Color.LightGray),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                WeatherIcon(weatherInfo)
            }
            Text(
                text = weatherInfo.now.description,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        Spacer(modifier = GlanceModifier.width(4.dp))
        Spacer(
            modifier = GlanceModifier
                .height(48.dp)
                .width(1.dp)
                .background(ColorProvider(Color.Gray))
        )
        Spacer(modifier = GlanceModifier.width(4.dp))
        Column(
            modifier = modifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row{
                Text(
                    text = "Осадки: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.precipitation != 0.0)
                        "${weatherInfo.now.precipitation} мм"
                    else
                        "Нет",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }

            Row(modifier = modifier.defaultWeight(),){
                Text(
                    text = "Ветер: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.windDirection != "—")
                        "${weatherInfo.now.windSpeed}—${weatherInfo.now.windGust} м/c, ${weatherInfo.now.windDirection}"
                    else
                        "Нет",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }

            Row{
                Text(
                    text = "Давление: ",
                    style = TextStyle(
                        color = ColorProvider(Color.LightGray),
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${weatherInfo.now.pressure} мм рт. ст.",
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}