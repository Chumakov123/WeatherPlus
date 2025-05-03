package com.chumakov123.gismeteoweather.data.dto

import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.model.WeatherData
import com.chumakov123.gismeteoweather.domain.util.Utils.normalizeIconString
import com.chumakov123.gismeteoweather.domain.util.WeatherDrawables
import com.chumakov123.gismeteoweather.domain.util.WindDirections
import com.chumakov123.gismeteoweather.domain.util.WindDirections.windDirections
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
    icon = WeatherDrawables.drawableMap[normalizeIconString(iconWeather)] ?: R.drawable.c3,
    temperature = temperatureAir,
    temperatureMin = null,
    windSpeed = windSpeed,
    windDirection = windDirections[WindDirections.getWindDirectionIndex(windDirection, windSpeed)] ?: "—",
    precipitation = precipitation,
    windGust = windGust,
    pressure = pressure
)