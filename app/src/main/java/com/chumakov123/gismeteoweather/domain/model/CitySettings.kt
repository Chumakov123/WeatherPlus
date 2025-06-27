package com.chumakov123.gismeteoweather.domain.model

data class CitySettings(
    val cityList: List<String>,
    val selectedCity: String
)