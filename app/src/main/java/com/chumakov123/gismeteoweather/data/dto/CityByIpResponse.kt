package com.chumakov123.gismeteoweather.data.dto
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import kotlinx.serialization.json.Json

@Serializable
data class CityByIpResponse(
    val id: Int,
    val slug: String,
    val country: Country,
    val translations: Translations
)

@Serializable
data class Country(
    val code: String
)

@Serializable
data class Translations(
    @SerialName("ru")
    val ru: RuTranslation
)

@Serializable
data class RuTranslation(
    val city: NameField,
    val country: NameField,
    val district: NameField? = null,      // nullable
    val subdistrict: NameField? = null    // nullable
)

@Serializable
data class NameField(val name: String)

@Serializable
data class CityInfo(
    val id: Int,
    val slug: String,
    val countryCode: String,
    val cityName: String,
    val countryName: String,
    val districtName: String? = null,
    val subdistrictName: String? = null
)

fun CityByIpResponse.toCityInfo(): CityInfo {
    val ru = translations.ru
    return CityInfo(
        id             = id,
        slug           = slug,
        countryCode    = country.code,
        cityName       = ru.city.name,
        countryName    = ru.country.name,
        districtName   = ru.district?.name,
        subdistrictName= ru.subdistrict?.name
    )
}

fun parseCityJsonKxSafely(jsonString: String): CityInfo {
    val dto = AppJson.decodeFromString<CityByIpResponse>(jsonString)
    return dto.toCityInfo()
}
internal val AppJson: Json = Json {
    ignoreUnknownKeys = true
    prettyPrint       = false
    isLenient         = true
}