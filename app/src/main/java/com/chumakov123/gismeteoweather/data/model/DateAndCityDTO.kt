package com.chumakov123.gismeteoweather.data.model

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone


data class DateAndCityDTO(
    val localDateTime: LocalDateTime,
    val cityName: String,
)