package com.chumakov123.gismeteoweather.data.weather

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
    val windSpeed: Int,
)

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