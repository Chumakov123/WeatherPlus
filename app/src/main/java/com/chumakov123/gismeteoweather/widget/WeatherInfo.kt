package com.chumakov123.gismeteoweather.widget

import androidx.annotation.DrawableRes
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
sealed interface WeatherInfo {
    @Serializable
    object Loading : WeatherInfo

    @Serializable
    data class Available(
        //val mode: ForecastMode, //TODO для этого отдельный класс
        val placeName: String,
        val placeCode: String,
        val now: WeatherData,
        val hourly: List<WeatherData>,
        val daily: List<WeatherData>,
        val updateTime: Long,
        val localTime: LocalDateTime,
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