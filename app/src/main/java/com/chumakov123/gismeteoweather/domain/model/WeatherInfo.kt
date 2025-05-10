package com.chumakov123.gismeteoweather.domain.model

import androidx.annotation.DrawableRes
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
sealed interface WeatherInfo {
    @Serializable
    object Loading : WeatherInfo

    @Serializable
    data class Available(
        val placeName: String,
        val placeCode: String,
        val now: CurrentWeatherData,
        val hourly: List<WeatherData>,
        val daily: List<WeatherData>,
        val updateTime: Long,
        val localTime: LocalDateTime,
        val astroTimes: AstroTimes
    ) : WeatherInfo

    @Serializable
    data class Unavailable(val message: String) : WeatherInfo
}

@Serializable
data class WeatherData(
    val description: String,
    @DrawableRes val icon: Int,
    val temperature: Int,
    val temperatureMin: Int?,
    val windSpeed: Int,
    val windDirection: String,
    val windGust: Int,
    val precipitation: Double,
    val pressure: Int,
)

@Serializable
data class CurrentWeatherData(
    val date: String,
    val colorBackground: String,
    val description: String,
    val iconWeather: String,
    @DrawableRes val icon: Int,
    val temperature: Int,
    val humidity: Int,
    val windSpeed: Int,
    val windDirection: String,
    val windGust: Int,
    val precipitation: Double,
    val pressure: Int,
    val radiation: Int,
    val temperatureAir: Int,
    val temperatureHeatIndex: Int,
    val temperatureWater: Int,
    val geomagnetic: Int,
)

@Serializable
data class AstroTimes(
    val sunset: LocalTime,
    val sunrise: LocalTime,
)

data class WindData(
    val speed: Int,
    val direction: String,
    val gust: Int
)

data class WeatherIconInfo(
    val tooltip: String,
    val topLayer: String?,
    val bottomLayer: String?
)