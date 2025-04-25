package com.chumakov123.gismeteoweather.data.model

import androidx.annotation.DrawableRes
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.utils.WeatherDrawables
import com.chumakov123.gismeteoweather.utils.WindDirections
import com.chumakov123.gismeteoweather.utils.WindDirections.windDirections
import com.chumakov123.gismeteoweather.widget.WeatherData
import kotlinx.serialization.Serializable

@Serializable
data class WeatherDTO(
    val colorBackground: String,
    val date: String,
    val description: String,
    val humidity: Int,
    val iconWeather: String,
    val precipitation: Double,
    val pressure: Int,
    val radiation: Int,
    val range: List<Int>,
    val temperatureAir: Int,
    val temperatureHeatIndex: Int,
    val temperatureWater: Int,
    val windDirection: Int,
    val windGust: Int,
    val windSpeed: Int
)

fun WeatherDTO.toWeatherData() = WeatherData(
    description = description,
    icon = WeatherDrawables.drawableMap[iconWeather] ?: R.drawable.c3,
    temperature = temperatureAir,
    temperatureMin = null,
    windSpeed = windSpeed,
    windDirection = windDirections[WindDirections.getWindDirectionIndex(windDirection, windSpeed)] ?: "—",
    precipitation = precipitation,
    windGust = windGust,
    pressure = pressure
)