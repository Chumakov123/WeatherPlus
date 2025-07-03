package com.chumakov123.weatherplus.domain.model

data class CitySettings(
    val cityList: List<String>,
    val selectedCity: String
)