package com.chumakov123.gismeteoweather.data.model

import androidx.annotation.DrawableRes
import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.utils.WeatherDrawables
import com.chumakov123.gismeteoweather.widget.MyWeatherData
import kotlinx.serialization.Serializable

val windDirections = mapOf(
    0 to "-",
    1 to "С",
    2 to "СВ",
    3 to "В",
    4 to "ЮВ",
    5 to "Ю",
    6 to "ЮЗ",
    7 to "З",
    8 to "СЗ"
)

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

fun WeatherDTO.toWeatherData() = MyWeatherData(
    description = description,
    icon = WeatherDrawables.drawableMap[iconWeather] ?: R.drawable.c3,
    temperature = temperatureAir,
    temperatureMin = null,
    windSpeed = windSpeed,
    windDirection = windDirections[windDirection]!!,
    precipitation = precipitation,
    windGust = windGust,
)