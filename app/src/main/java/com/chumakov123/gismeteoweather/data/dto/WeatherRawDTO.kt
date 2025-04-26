package com.chumakov123.gismeteoweather.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WeatherRawDTO(
    val colorBackground: List<String>,
    val date: List<String>,
    val description: List<String>,
    val humidity: List<Int>,
    val iconWeather: List<String>,
    val precipitation: List<Double>,
    val pressure: List<Int>,
    val radiation: List<Int>,
    val range: List<Int>,
    val temperatureAir: List<Int>,
    val temperatureHeatIndex: List<Int>,
    val temperatureWater: List<Int>,
    val windDirection: List<Int>,
    val windGust: List<Int>,
    val windSpeed: List<Int>
)

fun WeatherRawDTO.toWeatherDTO() = WeatherDTO(
    colorBackground = colorBackground.firstOrNull() ?: "",
    date = date.firstOrNull() ?: "",
    description = description.firstOrNull() ?: "",
    humidity = humidity.firstOrNull() ?: 0,
    iconWeather = iconWeather.firstOrNull() ?: "",
    precipitation = precipitation.firstOrNull() ?: 0.0,
    pressure = pressure.firstOrNull() ?: 0,
    radiation = radiation.firstOrNull() ?: 0,
    range = range,
    temperatureAir = temperatureAir.firstOrNull() ?: 0,
    temperatureHeatIndex = temperatureHeatIndex.firstOrNull() ?: 0,
    temperatureWater = temperatureWater.firstOrNull() ?: 0,
    windDirection = windDirection.firstOrNull() ?: 0,
    windGust = windGust.firstOrNull() ?: 0,
    windSpeed = windSpeed.firstOrNull() ?: 0
)
