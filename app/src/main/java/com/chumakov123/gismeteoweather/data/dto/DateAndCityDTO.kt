package com.chumakov123.gismeteoweather.data.dto

import kotlinx.datetime.LocalDateTime

data class DateAndCityDTO(
    val localDateTime: LocalDateTime,
    val cityName: String,
    val cityKind: String,
)
