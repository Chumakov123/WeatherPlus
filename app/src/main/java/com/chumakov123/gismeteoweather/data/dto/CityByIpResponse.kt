package com.chumakov123.gismeteoweather.data.dto
import kotlinx.serialization.Serializable

@Serializable
data class CityByIpResponse(
    val id: Int,
    val slug: String,
    val country: Country,
    val translations: Translations
)