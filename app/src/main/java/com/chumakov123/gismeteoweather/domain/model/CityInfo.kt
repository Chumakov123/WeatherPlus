package com.chumakov123.gismeteoweather.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CityInfo(
    val id: Int,
    val slug: String,
    val kind: String,
    val countryCode: String,
    val cityName: String,
    val countryName: String,
    val districtName: String? = null,
    val subdistrictName: String? = null
)