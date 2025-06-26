package com.chumakov123.gismeteoweather.data.mapper

import com.chumakov123.gismeteoweather.data.dto.CityByIpResponse
import com.chumakov123.gismeteoweather.domain.model.CityInfo
import com.chumakov123.gismeteoweather.domain.util.JsonConfig.AppJson

fun CityByIpResponse.toCityInfo(): CityInfo {
    val ru = translations.ru
    return CityInfo(
        id = id,
        slug = slug,
        kind = kind,
        countryCode = country.code,
        cityName = ru.city.name,
        countryName = ru.country.name,
        districtName = ru.district?.name,
        subdistrictName = ru.subdistrict?.name
    )
}

fun parseCityJsonKxSafely(jsonString: String): CityInfo {
    val dto = AppJson.decodeFromString<CityByIpResponse>(jsonString)
    return dto.toCityInfo()
}
