package com.chumakov123.gismeteoweather.data.model

// Режимы прогноза
enum class ForecastMode {
    HOURLY,
    DAILY
}

// Настройки города (по умолчанию Ростов-на-Дону и прогноз на один день)
data class CitySettings(
    val city: String = "rostov-na-donu-5110",
    val forecastMode: ForecastMode = ForecastMode.HOURLY
)