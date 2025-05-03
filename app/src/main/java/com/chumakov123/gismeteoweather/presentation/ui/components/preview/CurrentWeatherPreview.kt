package com.chumakov123.gismeteoweather.presentation.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.chumakov123.gismeteoweather.domain.model.WeatherInfo

@Composable
fun CurrentWeatherPreview(
    weatherInfo: WeatherInfo.Available,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,) {
                Text(
                    text = "${if (weatherInfo.now.temperature > 0) "+" else ""}${weatherInfo.now.temperature}°",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp
                    )
                )
                if (weatherInfo.now.temperatureMin != null) {
                    Row() {
                        Text(
                            text = "${weatherInfo.now.temperatureMin}°",
                            style = TextStyle(
                                color = Color.LightGray,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
                WeatherIconPreview(weatherInfo)
            }
            Text(
                text = weatherInfo.now.description,
                style = TextStyle(
                    color = Color.White,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Spacer(
            modifier = Modifier
                .height(48.dp)
                .width(1.dp)
                .background(Color.Gray)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Row{
                Text(
                    text = "Осадки: ",
                    style = TextStyle(
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.precipitation != 0.0)
                        "${weatherInfo.now.precipitation} мм"
                    else
                        "Нет",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }

            Row{
                Text(
                    text = "Ветер: ",
                    style = TextStyle(
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = if (weatherInfo.now.windDirection != "—")
                        if (weatherInfo.now.windSpeed != weatherInfo.now.windGust)
                            "${weatherInfo.now.windSpeed}—${weatherInfo.now.windGust} м/c, ${weatherInfo.now.windDirection}"
                        else
                            "${weatherInfo.now.windSpeed} м/c, ${weatherInfo.now.windDirection}"
                    else
                        "Нет",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }

            Row{
                Text(
                    text = "Давление: ",
                    style = TextStyle(
                        color = Color.LightGray,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "${weatherInfo.now.pressure} мм рт. ст.",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}