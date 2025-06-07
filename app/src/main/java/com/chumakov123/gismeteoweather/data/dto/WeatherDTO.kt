package com.chumakov123.gismeteoweather.data.dto

import com.chumakov123.gismeteoweather.R
import com.chumakov123.gismeteoweather.domain.model.CurrentWeatherData
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
    val windSpeed: Int,
)

fun WeatherDTO.toWeatherData() = WeatherData(
    description = description,
    icon = normalizeIconString(iconWeather),
    temperature = temperatureAir,
    temperatureMin = null,
    temperatureHeatIndex = temperatureHeatIndex,
    temperatureHeatIndexMin = null,
    temperatureAvg = 0,
    windSpeed = windSpeed,
    windDirection = windDirections[WindDirections.getWindDirectionIndex(windDirection, windSpeed)] ?: "—",
    precipitation = precipitation,
    windGust = windGust,
    pressure = pressure,
    pressureMin = null,
    humidity = humidity,
    pollenBirch = -1,
    pollenGrass = -1,
    radiation = radiation,
    geomagnetic = -1,
    snowHeight = 0.0,
    fallingSnow = 0.0
)

fun WeatherDTO.toCurrentWeatherData(geomagnetic: Int = 0): CurrentWeatherData {
    return CurrentWeatherData(
        date = this.date,
        colorBackground = this.colorBackground,
        description = this.description,
        iconWeather = this.iconWeather,
        icon = normalizeIconString(iconWeather),
        temperature = this.temperatureAir,
        humidity = this.humidity,
        windSpeed = this.windSpeed,
        windDirection = windDirections[WindDirections.getWindDirectionIndex(this.windDirection, this.windSpeed)] ?: "—",
        windGust = this.windGust,
        precipitation = this.precipitation,
        pressure = this.pressure,
        radiation = this.radiation,
        temperatureAir = this.temperatureAir,
        temperatureHeatIndex = this.temperatureHeatIndex,
        temperatureWater = this.temperatureWater,
        geomagnetic = geomagnetic
    )
}