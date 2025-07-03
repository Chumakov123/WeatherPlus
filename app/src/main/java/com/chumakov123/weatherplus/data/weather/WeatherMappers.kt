package com.chumakov123.weatherplus.data.weather

import com.chumakov123.weatherplus.domain.model.CurrentWeatherData
import com.chumakov123.weatherplus.domain.util.Utils.normalizeIconString
import com.chumakov123.weatherplus.domain.util.WindDirections
import com.chumakov123.weatherplus.domain.util.WindDirections.windDirections

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
