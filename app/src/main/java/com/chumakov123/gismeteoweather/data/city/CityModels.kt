package com.chumakov123.gismeteoweather.data.city

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.LocalDateTime

@Serializable
data class CityByIpResponse(
    val id: Int,
    val slug: String,
    val kind: String,
    val country: Country,
    val translations: Translations
)

@Serializable
data class Country(
    val code: String
)

data class DateAndCityDTO(
    val localDateTime: LocalDateTime,
    val cityName: String,
    val cityKind: String,
)

@Serializable
data class NameField(val name: String)

@Serializable
data class RuTranslation(
    val city: NameField,
    val country: NameField,
    val district: NameField? = null,
    val subdistrict: NameField? = null
)

@Serializable
data class Translations(
    @SerialName("ru")
    val ru: RuTranslation
)